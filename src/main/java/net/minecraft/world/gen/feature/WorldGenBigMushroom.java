package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenBigMushroom extends WorldGenerator
{
    private Block mushroomType;

    public WorldGenBigMushroom(Block mushroomTypeIn)
    {
        super(true);
        this.mushroomType = mushroomTypeIn;
    }

    public WorldGenBigMushroom()
    {
        super(false);
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if (this.mushroomType == null)
        {
            this.mushroomType = rand.nextBoolean() ? Blocks.brown_mushroom_block : Blocks.red_mushroom_block;
        }

        int mushroomHeight = rand.nextInt(3) + 4;
        boolean canGenerate = true;

        if (position.getY() >= 1 && position.getY() + mushroomHeight + 1 < 256)
        {
            for (int checkY = position.getY(); checkY <= position.getY() + 1 + mushroomHeight; ++checkY)
            {
                int checkRadius = 3;

                if (checkY <= position.getY() + 3)
                {
                    checkRadius = 0;
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

                if (groundBlock != Blocks.dirt && groundBlock != Blocks.grass && groundBlock != Blocks.mycelium)
                {
                    return false;
                }
                else
                {
                    int capStartY = position.getY() + mushroomHeight;

                    if (this.mushroomType == Blocks.red_mushroom_block)
                    {
                        capStartY = position.getY() + mushroomHeight - 3;
                    }

                    BlockPos.MutableBlockPos capPos = new BlockPos.MutableBlockPos();

                    for (int capY = capStartY; capY <= position.getY() + mushroomHeight; ++capY)
                    {
                        int capRadius = 1;

                        if (capY < position.getY() + mushroomHeight)
                        {
                            ++capRadius;
                        }

                        if (this.mushroomType == Blocks.brown_mushroom_block)
                        {
                            capRadius = 3;
                        }

                        int minCapX = position.getX() - capRadius;
                        int maxCapX = position.getX() + capRadius;
                        int minCapZ = position.getZ() - capRadius;
                        int maxCapZ = position.getZ() + capRadius;

                        for (int capX = minCapX; capX <= maxCapX; ++capX)
                        {
                            for (int capZ = minCapZ; capZ <= maxCapZ; ++capZ)
                            {
                                int capMetadata = 5;

                                if (capX == minCapX)
                                {
                                    --capMetadata;
                                }
                                else if (capX == maxCapX)
                                {
                                    ++capMetadata;
                                }

                                if (capZ == minCapZ)
                                {
                                    capMetadata -= 3;
                                }
                                else if (capZ == maxCapZ)
                                {
                                    capMetadata += 3;
                                }

                                BlockHugeMushroom.EnumType capVariant = BlockHugeMushroom.EnumType.byMetadata(capMetadata);

                                if (this.mushroomType == Blocks.brown_mushroom_block || capY < position.getY() + mushroomHeight)
                                {
                                    if ((capX == minCapX || capX == maxCapX) && (capZ == minCapZ || capZ == maxCapZ))
                                    {
                                        continue;
                                    }

                                    if (capX == position.getX() - (capRadius - 1) && capZ == minCapZ)
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.NORTH_WEST;
                                    }

                                    if (capX == minCapX && capZ == position.getZ() - (capRadius - 1))
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.NORTH_WEST;
                                    }

                                    if (capX == position.getX() + (capRadius - 1) && capZ == minCapZ)
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.NORTH_EAST;
                                    }

                                    if (capX == maxCapX && capZ == position.getZ() - (capRadius - 1))
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.NORTH_EAST;
                                    }

                                    if (capX == position.getX() - (capRadius - 1) && capZ == maxCapZ)
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.SOUTH_WEST;
                                    }

                                    if (capX == minCapX && capZ == position.getZ() + (capRadius - 1))
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.SOUTH_WEST;
                                    }

                                    if (capX == position.getX() + (capRadius - 1) && capZ == maxCapZ)
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.SOUTH_EAST;
                                    }

                                    if (capX == maxCapX && capZ == position.getZ() + (capRadius - 1))
                                    {
                                        capVariant = BlockHugeMushroom.EnumType.SOUTH_EAST;
                                    }
                                }

                                if (capVariant == BlockHugeMushroom.EnumType.CENTER && capY < position.getY() + mushroomHeight)
                                {
                                    capVariant = BlockHugeMushroom.EnumType.ALL_INSIDE;
                                }

                                if (position.getY() >= position.getY() + mushroomHeight - 1 || capVariant != BlockHugeMushroom.EnumType.ALL_INSIDE)
                                {
                                    capPos.set(capX, capY, capZ);

                                    if (!worldIn.getBlockState(capPos).getBlock().isFullBlock())
                                    {
                                        this.setBlockAndNotifyAdequately(worldIn, capPos, this.mushroomType.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, capVariant));
                                    }
                                }
                            }
                        }
                    }

                    for (int stemHeight = 0; stemHeight < mushroomHeight; ++stemHeight)
                    {
                        BlockPos stemPos = position.up(stemHeight);
                        Block stemBlock = worldIn.getBlockState(stemPos).getBlock();

                        if (!stemBlock.isFullBlock())
                        {
                            this.setBlockAndNotifyAdequately(worldIn, stemPos, this.mushroomType.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM));
                        }
                    }

                    return true;
                }
            }
        }
        else
        {
            return false;
        }
    }
}
