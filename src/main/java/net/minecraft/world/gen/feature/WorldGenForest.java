package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenForest extends WorldGenAbstractTree
{
    private static final IBlockState BIRCH_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
    private static final IBlockState BIRCH_LEAVES = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));
    private boolean useExtraRandomHeight;

    public WorldGenForest(boolean notify, boolean useExtraRandomHeightIn)
    {
        super(notify);
        this.useExtraRandomHeight = useExtraRandomHeightIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight = rand.nextInt(3) + 5;

        if (this.useExtraRandomHeight)
        {
            treeHeight += rand.nextInt(7);
        }

        boolean canGenerate = true;

        if (position.getY() >= 1 && position.getY() + treeHeight + 1 <= 256)
        {
            for (int checkY = position.getY(); checkY <= position.getY() + 1 + treeHeight; ++checkY)
            {
                int checkRadius = 1;

                if (checkY == position.getY())
                {
                    checkRadius = 0;
                }

                if (checkY >= position.getY() + 1 + treeHeight - 2)
                {
                    checkRadius = 2;
                }

                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

                for (int checkX = position.getX() - checkRadius; checkX <= position.getX() + checkRadius && canGenerate; ++checkX)
                {
                    for (int checkZ = position.getZ() - checkRadius; checkZ <= position.getZ() + checkRadius && canGenerate; ++checkZ)
                    {
                        if (checkY >= 0 && checkY < 256)
                        {
                            if (!this.canGrowInto(worldIn.getBlockState(mutablePos.set(checkX, checkY, checkZ)).getBlock()))
                            {
                                canGenerate = false;
                            }
                        }
                        else
                        {
                            canGenerate = false;
                        }
                    }
                }
            }

            if (!canGenerate)
            {
                return false;
            }
            else
            {
                Block groundBlock = worldIn.getBlockState(position.down()).getBlock();

                if ((groundBlock == Blocks.grass || groundBlock == Blocks.dirt || groundBlock == Blocks.farmland) && position.getY() < 256 - treeHeight - 1)
                {
                    this.setDirtAt(worldIn, position.down());
                    BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();

                    for (int leafY = position.getY() - 3 + treeHeight; leafY <= position.getY() + treeHeight; ++leafY)
                    {
                        int leafYOffset = leafY - (position.getY() + treeHeight);
                        int leafRadius = 1 - leafYOffset / 2;

                        for (int leafX = position.getX() - leafRadius; leafX <= position.getX() + leafRadius; ++leafX)
                        {
                            int leafXOffset = leafX - position.getX();

                            for (int leafZ = position.getZ() - leafRadius; leafZ <= position.getZ() + leafRadius; ++leafZ)
                            {
                                int leafZOffset = leafZ - position.getZ();

                                if (Math.abs(leafXOffset) != leafRadius || Math.abs(leafZOffset) != leafRadius || rand.nextInt(2) != 0 && leafYOffset != 0)
                                {
                                    leafPos.set(leafX, leafY, leafZ);
                                    Block block = worldIn.getBlockState(leafPos).getBlock();

                                    if (block.getMaterial() == Material.air || block.getMaterial() == Material.leaves)
                                    {
                                        this.setBlockAndNotifyAdequately(worldIn, leafPos, BIRCH_LEAVES);
                                    }
                                }
                            }
                        }
                    }

                    for (int trunkOffset = 0; trunkOffset < treeHeight; ++trunkOffset)
                    {
                        BlockPos trunkPos = position.up(trunkOffset);
                        Block trunkBlock = worldIn.getBlockState(trunkPos).getBlock();

                        if (trunkBlock.getMaterial() == Material.air || trunkBlock.getMaterial() == Material.leaves)
                        {
                            this.setBlockAndNotifyAdequately(worldIn, trunkPos, BIRCH_LOG);
                        }
                    }

                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
    }
}
