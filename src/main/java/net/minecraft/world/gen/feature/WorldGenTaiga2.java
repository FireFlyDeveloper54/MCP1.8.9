package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenTaiga2 extends WorldGenAbstractTree
{
    private static final IBlockState SPRUCE_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState SPRUCE_LEAVES = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    public WorldGenTaiga2(boolean notify)
    {
        super(notify);
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight = rand.nextInt(4) + 6;
        int trunkClearHeight = 1 + rand.nextInt(2);
        int leafColumnHeight = treeHeight - trunkClearHeight;
        int maxLeafRadius = 2 + rand.nextInt(2);
        boolean canGenerate = true;

        if (position.getY() >= 1 && position.getY() + treeHeight + 1 <= 256)
        {
            for (int checkY = position.getY(); checkY <= position.getY() + 1 + treeHeight && canGenerate; ++checkY)
            {
                int checkRadius = 1;

                if (checkY - position.getY() < trunkClearHeight)
                {
                    checkRadius = 0;
                }
                else
                {
                    checkRadius = maxLeafRadius;
                }

                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

                for (int checkX = position.getX() - checkRadius; checkX <= position.getX() + checkRadius && canGenerate; ++checkX)
                {
                    for (int checkZ = position.getZ() - checkRadius; checkZ <= position.getZ() + checkRadius && canGenerate; ++checkZ)
                    {
                        if (checkY >= 0 && checkY < 256)
                        {
                            Block block = worldIn.getBlockState(mutablePos.set(checkX, checkY, checkZ)).getBlock();

                            if (block.getMaterial() != Material.air && block.getMaterial() != Material.leaves)
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
                    int leafRadius = rand.nextInt(2);
                    int radiusLimit = 1;
                    int resetRadius = 0;
                    BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();

                    for (int leafLayer = 0; leafLayer <= leafColumnHeight; ++leafLayer)
                    {
                        int leafY = position.getY() + treeHeight - leafLayer;

                        for (int leafX = position.getX() - leafRadius; leafX <= position.getX() + leafRadius; ++leafX)
                        {
                            int leafXOffset = leafX - position.getX();

                            for (int leafZ = position.getZ() - leafRadius; leafZ <= position.getZ() + leafRadius; ++leafZ)
                            {
                                int leafZOffset = leafZ - position.getZ();

                                if (Math.abs(leafXOffset) != leafRadius || Math.abs(leafZOffset) != leafRadius || leafRadius <= 0)
                                {
                                    leafPos.set(leafX, leafY, leafZ);

                                    if (!worldIn.getBlockState(leafPos).getBlock().isFullBlock())
                                    {
                                        this.setBlockAndNotifyAdequately(worldIn, leafPos, SPRUCE_LEAVES);
                                    }
                                }
                            }
                        }

                        if (leafRadius >= radiusLimit)
                        {
                            leafRadius = resetRadius;
                            resetRadius = 1;
                            ++radiusLimit;

                            if (radiusLimit > maxLeafRadius)
                            {
                                radiusLimit = maxLeafRadius;
                            }
                        }
                        else
                        {
                            ++leafRadius;
                        }
                    }

                    int trunkTopSkip = rand.nextInt(3);

                    for (int trunkOffset = 0; trunkOffset < treeHeight - trunkTopSkip; ++trunkOffset)
                    {
                        BlockPos trunkPos = position.up(trunkOffset);
                        Block trunkBlock = worldIn.getBlockState(trunkPos).getBlock();

                        if (trunkBlock.getMaterial() == Material.air || trunkBlock.getMaterial() == Material.leaves)
                        {
                            this.setBlockAndNotifyAdequately(worldIn, trunkPos, SPRUCE_LOG);
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
