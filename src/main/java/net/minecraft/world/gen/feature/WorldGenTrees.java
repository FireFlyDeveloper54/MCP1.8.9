package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldGenTrees extends WorldGenAbstractTree
{
    private static final IBlockState OAK_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState OAK_LEAVES = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private final int minTreeHeight;
    private final boolean vinesGrow;
    private final IBlockState metaWood;
    private final IBlockState metaLeaves;

    public WorldGenTrees(boolean notify)
    {
        this(notify, 4, OAK_LOG, OAK_LEAVES, false);
    }

    public WorldGenTrees(boolean notify, int minTreeHeightIn, IBlockState woodMetadataIn, IBlockState leavesMetadataIn, boolean vinesGrowIn)
    {
        super(notify);
        this.minTreeHeight = minTreeHeightIn;
        this.metaWood = woodMetadataIn;
        this.metaLeaves = leavesMetadataIn;
        this.vinesGrow = vinesGrowIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight = rand.nextInt(3) + this.minTreeHeight;
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
                    int leafCrownDepth = 3;
                    int baseLeafRadius = 0;
                    BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();

                    for (int leafY = position.getY() - leafCrownDepth + treeHeight; leafY <= position.getY() + treeHeight; ++leafY)
                    {
                        int leafYOffset = leafY - (position.getY() + treeHeight);
                        int leafRadius = baseLeafRadius + 1 - leafYOffset / 2;

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

                                    if (block.getMaterial() == Material.air || block.getMaterial() == Material.leaves || block.getMaterial() == Material.vine)
                                    {
                                        this.setBlockAndNotifyAdequately(worldIn, leafPos, this.metaLeaves);
                                    }
                                }
                            }
                        }
                    }

                    for (int trunkOffset = 0; trunkOffset < treeHeight; ++trunkOffset)
                    {
                        BlockPos trunkPos = position.up(trunkOffset);
                        Block trunkBlock = worldIn.getBlockState(trunkPos).getBlock();

                        if (trunkBlock.getMaterial() == Material.air || trunkBlock.getMaterial() == Material.leaves || trunkBlock.getMaterial() == Material.vine)
                        {
                            this.setBlockAndNotifyAdequately(worldIn, trunkPos, this.metaWood);

                            if (this.vinesGrow && trunkOffset > 0)
                            {
                                if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(-1, trunkOffset, 0)))
                                {
                                    this.placeVine(worldIn, position.add(-1, trunkOffset, 0), BlockVine.EAST);
                                }

                                if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(1, trunkOffset, 0)))
                                {
                                    this.placeVine(worldIn, position.add(1, trunkOffset, 0), BlockVine.WEST);
                                }

                                if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(0, trunkOffset, -1)))
                                {
                                    this.placeVine(worldIn, position.add(0, trunkOffset, -1), BlockVine.SOUTH);
                                }

                                if (rand.nextInt(3) > 0 && worldIn.isAirBlock(position.add(0, trunkOffset, 1)))
                                {
                                    this.placeVine(worldIn, position.add(0, trunkOffset, 1), BlockVine.NORTH);
                                }
                            }
                        }
                    }

                    if (this.vinesGrow)
                    {
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

                        if (rand.nextInt(5) == 0 && treeHeight > 5)
                        {
                            for (int cocoaLayer = 0; cocoaLayer < 2; ++cocoaLayer)
                            {
                                for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                                {
                                    if (rand.nextInt(4 - cocoaLayer) == 0)
                                    {
                                        EnumFacing oppositeFacing = facing.getOpposite();
                                        this.placeCocoa(worldIn, rand.nextInt(3), position.add(oppositeFacing.getFrontOffsetX(), treeHeight - 5 + cocoaLayer, oppositeFacing.getFrontOffsetZ()), facing);
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

    private void placeCocoa(World worldIn, int age, BlockPos pos, EnumFacing facing)
    {
        this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.cocoa.getDefaultState().withProperty(BlockCocoa.AGE, Integer.valueOf(age)).withProperty(BlockCocoa.FACING, facing));
    }

    private void placeVine(World worldIn, BlockPos pos, PropertyBool side)
    {
        this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.vine.getDefaultState().withProperty(side, Boolean.valueOf(true)));
    }

    private void growVines(World worldIn, BlockPos pos, PropertyBool side)
    {
        this.placeVine(worldIn, pos, side);
        int remainingLength = 4;

        for (pos = pos.down(); worldIn.getBlockState(pos).getBlock().getMaterial() == Material.air && remainingLength > 0; --remainingLength)
        {
            this.placeVine(worldIn, pos, side);
            pos = pos.down();
        }
    }
}
