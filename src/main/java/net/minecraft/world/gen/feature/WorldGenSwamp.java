package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenSwamp extends WorldGenAbstractTree
{
    private static final IBlockState OAK_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState OAK_LEAVES = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));

    public WorldGenSwamp()
    {
        super(false);
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight;

        for (treeHeight = rand.nextInt(4) + 5; worldIn.getBlockState(position.down()).getBlock().getMaterial() == Material.water; position = position.down())
        {
            ;
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
                    checkRadius = 3;
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
                                if (block != Blocks.water && block != Blocks.flowing_water)
                                {
                                    canGenerate = false;
                                }
                                else if (checkY > position.getY())
                                {
                                    canGenerate = false;
                                }
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

                if ((groundBlock == Blocks.grass || groundBlock == Blocks.dirt) && position.getY() < 256 - treeHeight - 1)
                {
                    this.setDirtAt(worldIn, position.down());
                    BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();

                    for (int leafY = position.getY() - 3 + treeHeight; leafY <= position.getY() + treeHeight; ++leafY)
                    {
                        int leafYOffset = leafY - (position.getY() + treeHeight);
                        int leafRadius = 2 - leafYOffset / 2;

                        for (int leafX = position.getX() - leafRadius; leafX <= position.getX() + leafRadius; ++leafX)
                        {
                            int leafXOffset = leafX - position.getX();

                            for (int leafZ = position.getZ() - leafRadius; leafZ <= position.getZ() + leafRadius; ++leafZ)
                            {
                                int leafZOffset = leafZ - position.getZ();

                                if (Math.abs(leafXOffset) != leafRadius || Math.abs(leafZOffset) != leafRadius || rand.nextInt(2) != 0 && leafYOffset != 0)
                                {
                                    leafPos.set(leafX, leafY, leafZ);

                                    if (!worldIn.getBlockState(leafPos).getBlock().isFullBlock())
                                    {
                                        this.setBlockAndNotifyAdequately(worldIn, leafPos, OAK_LEAVES);
                                    }
                                }
                            }
                        }
                    }

                    for (int trunkOffset = 0; trunkOffset < treeHeight; ++trunkOffset)
                    {
                        BlockPos trunkPos = position.up(trunkOffset);
                        Block trunkBlock = worldIn.getBlockState(trunkPos).getBlock();

                        if (trunkBlock.getMaterial() == Material.air || trunkBlock.getMaterial() == Material.leaves || trunkBlock == Blocks.flowing_water || trunkBlock == Blocks.water)
                        {
                            this.setBlockAndNotifyAdequately(worldIn, trunkPos, OAK_LOG);
                        }
                    }

                    for (int leafY = position.getY() - 3 + treeHeight; leafY <= position.getY() + treeHeight; ++leafY)
                    {
                        int leafYOffset = leafY - (position.getY() + treeHeight);
                        int vineScanRadius = 2 - leafYOffset / 2;
                        BlockPos.MutableBlockPos mutableLeafPos = new BlockPos.MutableBlockPos();

                        for (int leafX = position.getX() - vineScanRadius; leafX <= position.getX() + vineScanRadius; ++leafX)
                        {
                            for (int leafZ = position.getZ() - vineScanRadius; leafZ <= position.getZ() + vineScanRadius; ++leafZ)
                            {
                                mutableLeafPos.set(leafX, leafY, leafZ);

                                if (worldIn.getBlockState(mutableLeafPos).getBlock().getMaterial() == Material.leaves)
                                {
                                    BlockPos westVinePos = mutableLeafPos.west();
                                    BlockPos eastVinePos = mutableLeafPos.east();
                                    BlockPos northVinePos = mutableLeafPos.north();
                                    BlockPos southVinePos = mutableLeafPos.south();

                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(westVinePos).getBlock().getMaterial() == Material.air)
                                    {
                                        this.growVines(worldIn, westVinePos, BlockVine.EAST);
                                    }

                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(eastVinePos).getBlock().getMaterial() == Material.air)
                                    {
                                        this.growVines(worldIn, eastVinePos, BlockVine.WEST);
                                    }

                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(northVinePos).getBlock().getMaterial() == Material.air)
                                    {
                                        this.growVines(worldIn, northVinePos, BlockVine.SOUTH);
                                    }

                                    if (rand.nextInt(4) == 0 && worldIn.getBlockState(southVinePos).getBlock().getMaterial() == Material.air)
                                    {
                                        this.growVines(worldIn, southVinePos, BlockVine.NORTH);
                                    }
                                }
                            }
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

    private void growVines(World worldIn, BlockPos pos, PropertyBool side)
    {
        IBlockState vineState = Blocks.vine.getDefaultState().withProperty(side, Boolean.valueOf(true));
        this.setBlockAndNotifyAdequately(worldIn, pos, vineState);
        int remainingLength = 4;

        for (pos = pos.down(); worldIn.getBlockState(pos).getBlock().getMaterial() == Material.air && remainingLength > 0; --remainingLength)
        {
            this.setBlockAndNotifyAdequately(worldIn, pos, vineState);
            pos = pos.down();
        }
    }
}
