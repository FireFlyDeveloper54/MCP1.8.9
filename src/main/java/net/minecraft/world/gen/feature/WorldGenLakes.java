package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class WorldGenLakes extends WorldGenerator
{
    private Block block;

    public WorldGenLakes(Block blockIn)
    {
        this.block = blockIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (position = position.add(-8, 0, -8); position.getY() > 5 && worldIn.isAirBlock(position); position = position.down())
        {
            ;
        }

        if (position.getY() <= 4)
        {
            return false;
        }
        else
        {
            position = position.down(4);
            boolean[] lakeShape = new boolean[2048];
            int blobCount = rand.nextInt(4) + 4;

            for (int blobIndex = 0; blobIndex < blobCount; ++blobIndex)
            {
                double diameterX = rand.nextDouble() * 6.0D + 3.0D;
                double diameterY = rand.nextDouble() * 4.0D + 2.0D;
                double diameterZ = rand.nextDouble() * 6.0D + 3.0D;
                double centerX = rand.nextDouble() * (16.0D - diameterX - 2.0D) + 1.0D + diameterX / 2.0D;
                double centerY = rand.nextDouble() * (8.0D - diameterY - 4.0D) + 2.0D + diameterY / 2.0D;
                double centerZ = rand.nextDouble() * (16.0D - diameterZ - 2.0D) + 1.0D + diameterZ / 2.0D;

                for (int localX = 1; localX < 15; ++localX)
                {
                    for (int localZ = 1; localZ < 15; ++localZ)
                    {
                        for (int localY = 1; localY < 7; ++localY)
                        {
                            double normalizedX = ((double)localX - centerX) / (diameterX / 2.0D);
                            double normalizedY = ((double)localY - centerY) / (diameterY / 2.0D);
                            double normalizedZ = ((double)localZ - centerZ) / (diameterZ / 2.0D);
                            double distanceSq = normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ;

                            if (distanceSq < 1.0D)
                            {
                                lakeShape[(localX * 16 + localZ) * 8 + localY] = true;
                            }
                        }
                    }
                }
            }

            for (int localX = 0; localX < 16; ++localX)
            {
                for (int localZ = 0; localZ < 16; ++localZ)
                {
                    for (int localY = 0; localY < 8; ++localY)
                    {
                        boolean isBoundary = !lakeShape[(localX * 16 + localZ) * 8 + localY] && (localX < 15 && lakeShape[((localX + 1) * 16 + localZ) * 8 + localY] || localX > 0 && lakeShape[((localX - 1) * 16 + localZ) * 8 + localY] || localZ < 15 && lakeShape[(localX * 16 + localZ + 1) * 8 + localY] || localZ > 0 && lakeShape[(localX * 16 + (localZ - 1)) * 8 + localY] || localY < 7 && lakeShape[(localX * 16 + localZ) * 8 + localY + 1] || localY > 0 && lakeShape[(localX * 16 + localZ) * 8 + (localY - 1)]);

                        if (isBoundary)
                        {
                            Material material = worldIn.getBlockState(position.add(localX, localY, localZ)).getBlock().getMaterial();

                            if (localY >= 4 && material.isLiquid())
                            {
                                return false;
                            }

                            if (localY < 4 && !material.isSolid() && worldIn.getBlockState(position.add(localX, localY, localZ)).getBlock() != this.block)
                            {
                                return false;
                            }
                        }
                    }
                }
            }

            for (int localX = 0; localX < 16; ++localX)
            {
                for (int localZ = 0; localZ < 16; ++localZ)
                {
                    for (int localY = 0; localY < 8; ++localY)
                    {
                        if (lakeShape[(localX * 16 + localZ) * 8 + localY])
                        {
                            worldIn.setBlockState(position.add(localX, localY, localZ), localY >= 4 ? Blocks.air.getDefaultState() : this.block.getDefaultState(), 2);
                        }
                    }
                }
            }

            for (int localX = 0; localX < 16; ++localX)
            {
                for (int localZ = 0; localZ < 16; ++localZ)
                {
                    for (int localY = 4; localY < 8; ++localY)
                    {
                        if (lakeShape[(localX * 16 + localZ) * 8 + localY])
                        {
                            BlockPos surfacePos = position.add(localX, localY - 1, localZ);

                            if (worldIn.getBlockState(surfacePos).getBlock() == Blocks.dirt && worldIn.getLightFor(EnumSkyBlock.SKY, position.add(localX, localY, localZ)) > 0)
                            {
                                BiomeGenBase biome = worldIn.getBiomeGenForCoords(surfacePos);

                                if (biome.topBlock.getBlock() == Blocks.mycelium)
                                {
                                    worldIn.setBlockState(surfacePos, Blocks.mycelium.getDefaultState(), 2);
                                }
                                else
                                {
                                    worldIn.setBlockState(surfacePos, Blocks.grass.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }
            }

            if (this.block.getMaterial() == Material.lava)
            {
                for (int localX = 0; localX < 16; ++localX)
                {
                    for (int localZ = 0; localZ < 16; ++localZ)
                    {
                        for (int localY = 0; localY < 8; ++localY)
                        {
                            boolean isLavaBoundary = !lakeShape[(localX * 16 + localZ) * 8 + localY] && (localX < 15 && lakeShape[((localX + 1) * 16 + localZ) * 8 + localY] || localX > 0 && lakeShape[((localX - 1) * 16 + localZ) * 8 + localY] || localZ < 15 && lakeShape[(localX * 16 + localZ + 1) * 8 + localY] || localZ > 0 && lakeShape[(localX * 16 + (localZ - 1)) * 8 + localY] || localY < 7 && lakeShape[(localX * 16 + localZ) * 8 + localY + 1] || localY > 0 && lakeShape[(localX * 16 + localZ) * 8 + (localY - 1)]);

                            if (isLavaBoundary && (localY < 4 || rand.nextInt(2) != 0) && worldIn.getBlockState(position.add(localX, localY, localZ)).getBlock().getMaterial().isSolid())
                            {
                                worldIn.setBlockState(position.add(localX, localY, localZ), Blocks.stone.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }

            if (this.block.getMaterial() == Material.water)
            {
                for (int localX = 0; localX < 16; ++localX)
                {
                    for (int localZ = 0; localZ < 16; ++localZ)
                    {
                        int surfaceY = 4;

                        if (worldIn.canBlockFreezeWater(position.add(localX, surfaceY, localZ)))
                        {
                            worldIn.setBlockState(position.add(localX, surfaceY, localZ), Blocks.ice.getDefaultState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }
}
