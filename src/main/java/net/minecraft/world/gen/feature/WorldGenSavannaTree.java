package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldGenSavannaTree extends WorldGenAbstractTree
{
    private static final IBlockState ACACIA_LOG = Blocks.log2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
    private static final IBlockState ACACIA_LEAVES = Blocks.leaves2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    public WorldGenSavannaTree(boolean notify)
    {
        super(notify);
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight = rand.nextInt(3) + rand.nextInt(3) + 5;
        boolean canGenerate = true;

        if (position.getY() >= 1 && position.getY() + treeHeight + 1 <= 256)
        {
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

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

                if ((groundBlock == Blocks.grass || groundBlock == Blocks.dirt) && position.getY() < 256 - treeHeight - 1)
                {
                    this.setDirtAt(worldIn, position.down());
                    EnumFacing primaryDirection = EnumFacing.Plane.HORIZONTAL.random(rand);
                    int primaryBendStart = treeHeight - rand.nextInt(4) - 1;
                    int primaryBendLength = 3 - rand.nextInt(3);
                    int trunkX = position.getX();
                    int trunkZ = position.getZ();
                    int topY = 0;

                    for (int trunkOffset = 0; trunkOffset < treeHeight; ++trunkOffset)
                    {
                        int trunkY = position.getY() + trunkOffset;

                        if (trunkOffset >= primaryBendStart && primaryBendLength > 0)
                        {
                            trunkX += primaryDirection.getFrontOffsetX();
                            trunkZ += primaryDirection.getFrontOffsetZ();
                            --primaryBendLength;
                        }

                        BlockPos trunkPos = new BlockPos(trunkX, trunkY, trunkZ);
                        Material material = worldIn.getBlockState(trunkPos).getBlock().getMaterial();

                        if (material == Material.air || material == Material.leaves)
                        {
                            this.placeLogAt(worldIn, trunkPos);
                            topY = trunkY;
                        }
                    }

                    BlockPos crownPos = new BlockPos(trunkX, topY, trunkZ);

                    for (int leafXOffset = -3; leafXOffset <= 3; ++leafXOffset)
                    {
                        for (int leafZOffset = -3; leafZOffset <= 3; ++leafZOffset)
                        {
                            if (Math.abs(leafXOffset) != 3 || Math.abs(leafZOffset) != 3)
                            {
                                this.placeLeafAt(worldIn, crownPos.add(leafXOffset, 0, leafZOffset));
                            }
                        }
                    }

                    crownPos = crownPos.up();

                    for (int leafXOffset = -1; leafXOffset <= 1; ++leafXOffset)
                    {
                        for (int leafZOffset = -1; leafZOffset <= 1; ++leafZOffset)
                        {
                            this.placeLeafAt(worldIn, crownPos.add(leafXOffset, 0, leafZOffset));
                        }
                    }

                    this.placeLeafAt(worldIn, crownPos.east(2));
                    this.placeLeafAt(worldIn, crownPos.west(2));
                    this.placeLeafAt(worldIn, crownPos.south(2));
                    this.placeLeafAt(worldIn, crownPos.north(2));
                    trunkX = position.getX();
                    trunkZ = position.getZ();
                    EnumFacing secondaryDirection = EnumFacing.Plane.HORIZONTAL.random(rand);

                    if (secondaryDirection != primaryDirection)
                    {
                        int secondaryBendStart = primaryBendStart - rand.nextInt(2) - 1;
                        int secondaryBendLength = 1 + rand.nextInt(3);
                        topY = 0;

                        for (int trunkOffset = secondaryBendStart; trunkOffset < treeHeight && secondaryBendLength > 0; --secondaryBendLength)
                        {
                            if (trunkOffset >= 1)
                            {
                                int trunkY = position.getY() + trunkOffset;
                                trunkX += secondaryDirection.getFrontOffsetX();
                                trunkZ += secondaryDirection.getFrontOffsetZ();
                                BlockPos trunkPos = new BlockPos(trunkX, trunkY, trunkZ);
                                Material material = worldIn.getBlockState(trunkPos).getBlock().getMaterial();

                                if (material == Material.air || material == Material.leaves)
                                {
                                    this.placeLogAt(worldIn, trunkPos);
                                    topY = trunkY;
                                }
                            }

                            ++trunkOffset;
                        }

                        if (topY > 0)
                        {
                            BlockPos secondaryCrownPos = new BlockPos(trunkX, topY, trunkZ);

                            for (int leafXOffset = -2; leafXOffset <= 2; ++leafXOffset)
                            {
                                for (int leafZOffset = -2; leafZOffset <= 2; ++leafZOffset)
                                {
                                    if (Math.abs(leafXOffset) != 2 || Math.abs(leafZOffset) != 2)
                                    {
                                        this.placeLeafAt(worldIn, secondaryCrownPos.add(leafXOffset, 0, leafZOffset));
                                    }
                                }
                            }

                            secondaryCrownPos = secondaryCrownPos.up();

                            for (int leafXOffset = -1; leafXOffset <= 1; ++leafXOffset)
                            {
                                for (int leafZOffset = -1; leafZOffset <= 1; ++leafZOffset)
                                {
                                    this.placeLeafAt(worldIn, secondaryCrownPos.add(leafXOffset, 0, leafZOffset));
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

    private void placeLogAt(World worldIn, BlockPos pos)
    {
        this.setBlockAndNotifyAdequately(worldIn, pos, ACACIA_LOG);
    }

    private void placeLeafAt(World worldIn, BlockPos pos)
    {
        Material material = worldIn.getBlockState(pos).getBlock().getMaterial();

        if (material == Material.air || material == Material.leaves)
        {
            this.setBlockAndNotifyAdequately(worldIn, pos, ACACIA_LEAVES);
        }
    }
}
