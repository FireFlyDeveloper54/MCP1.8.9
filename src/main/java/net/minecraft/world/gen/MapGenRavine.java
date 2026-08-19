package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenRavine extends MapGenBase
{
    private float[] ravineWidthFactors = new float[1024];

    protected void addTunnel(long seed, int chunkX, int chunkZ, ChunkPrimer primer, double x, double y, double z, float width, float yaw, float pitch, int start, int end, double heightScale)
    {
        Random ravineRandom = new Random(seed);
        double chunkCenterX = (double)(chunkX * 16 + 8);
        double chunkCenterZ = (double)(chunkZ * 16 + 8);
        float yawDelta = 0.0F;
        float pitchDelta = 0.0F;

        if (end <= 0)
        {
            int maxTunnelLength = this.range * 16 - 16;
            end = maxTunnelLength - ravineRandom.nextInt(maxTunnelLength / 4);
        }

        boolean singleNode = false;

        if (start == -1)
        {
            start = end / 2;
            singleNode = true;
        }

        float widthFactor = 1.0F;

        for (int yIndex = 0; yIndex < 256; ++yIndex)
        {
            if (yIndex == 0 || ravineRandom.nextInt(3) == 0)
            {
                widthFactor = 1.0F + ravineRandom.nextFloat() * ravineRandom.nextFloat() * 1.0F;
            }

            this.ravineWidthFactors[yIndex] = widthFactor * widthFactor;
        }

        for (; start < end; ++start)
        {
            double horizontalRadius = 1.5D + (double)(MathHelper.sin((float)start * (float)Math.PI / (float)end) * width * 1.0F);
            double verticalRadius = horizontalRadius * heightScale;
            horizontalRadius = horizontalRadius * ((double)ravineRandom.nextFloat() * 0.25D + 0.75D);
            verticalRadius = verticalRadius * ((double)ravineRandom.nextFloat() * 0.25D + 0.75D);
            float cosPitch = MathHelper.cos(pitch);
            float sinPitch = MathHelper.sin(pitch);
            x += (double)(MathHelper.cos(yaw) * cosPitch);
            y += (double)sinPitch;
            z += (double)(MathHelper.sin(yaw) * cosPitch);
            pitch = pitch * 0.7F;
            pitch = pitch + pitchDelta * 0.05F;
            yaw += yawDelta * 0.05F;
            pitchDelta = pitchDelta * 0.8F;
            yawDelta = yawDelta * 0.5F;
            pitchDelta = pitchDelta + (ravineRandom.nextFloat() - ravineRandom.nextFloat()) * ravineRandom.nextFloat() * 2.0F;
            yawDelta = yawDelta + (ravineRandom.nextFloat() - ravineRandom.nextFloat()) * ravineRandom.nextFloat() * 4.0F;

            if (singleNode || ravineRandom.nextInt(4) != 0)
            {
                double deltaXFromChunkCenter = x - chunkCenterX;
                double deltaZFromChunkCenter = z - chunkCenterZ;
                double remainingSteps = (double)(end - start);
                double maxReach = (double)(width + 2.0F + 16.0F);

                if (deltaXFromChunkCenter * deltaXFromChunkCenter + deltaZFromChunkCenter * deltaZFromChunkCenter - remainingSteps * remainingSteps > maxReach * maxReach)
                {
                    return;
                }

                if (x >= chunkCenterX - 16.0D - horizontalRadius * 2.0D && z >= chunkCenterZ - 16.0D - horizontalRadius * 2.0D && x <= chunkCenterX + 16.0D + horizontalRadius * 2.0D && z <= chunkCenterZ + 16.0D + horizontalRadius * 2.0D)
                {
                    int minX = MathHelper.floor_double(x - horizontalRadius) - chunkX * 16 - 1;
                    int maxX = MathHelper.floor_double(x + horizontalRadius) - chunkX * 16 + 1;
                    int minY = MathHelper.floor_double(y - verticalRadius) - 1;
                    int maxY = MathHelper.floor_double(y + verticalRadius) + 1;
                    int minZ = MathHelper.floor_double(z - horizontalRadius) - chunkZ * 16 - 1;
                    int maxZ = MathHelper.floor_double(z + horizontalRadius) - chunkZ * 16 + 1;

                    if (minX < 0)
                    {
                        minX = 0;
                    }

                    if (maxX > 16)
                    {
                        maxX = 16;
                    }

                    if (minY < 1)
                    {
                        minY = 1;
                    }

                    if (maxY > 248)
                    {
                        maxY = 248;
                    }

                    if (minZ < 0)
                    {
                        minZ = 0;
                    }

                    if (maxZ > 16)
                    {
                        maxZ = 16;
                    }

                    boolean foundWater = false;

                    for (int checkX = minX; !foundWater && checkX < maxX; ++checkX)
                    {
                        for (int checkZ = minZ; !foundWater && checkZ < maxZ; ++checkZ)
                        {
                            for (int checkY = maxY + 1; !foundWater && checkY >= minY - 1; --checkY)
                            {
                                if (checkY >= 0 && checkY < 256)
                                {
                                    IBlockState checkState = primer.getBlockState(checkX, checkY, checkZ);

                                    if (checkState.getBlock() == Blocks.flowing_water || checkState.getBlock() == Blocks.water)
                                    {
                                        foundWater = true;
                                    }

                                    if (checkY != minY - 1 && checkX != minX && checkX != maxX - 1 && checkZ != minZ && checkZ != maxZ - 1)
                                    {
                                        checkY = minY;
                                    }
                                }
                            }
                        }
                    }

                    if (!foundWater)
                    {
                        BlockPos.MutableBlockPos biomeLookupPos = new BlockPos.MutableBlockPos();

                        for (int localX = minX; localX < maxX; ++localX)
                        {
                            double normalizedX = ((double)(localX + chunkX * 16) + 0.5D - x) / horizontalRadius;

                            for (int localZ = minZ; localZ < maxZ; ++localZ)
                            {
                                double normalizedZ = ((double)(localZ + chunkZ * 16) + 0.5D - z) / horizontalRadius;
                                boolean foundSurfaceGrass = false;

                                if (normalizedX * normalizedX + normalizedZ * normalizedZ < 1.0D)
                                {
                                    for (int localY = maxY; localY > minY; --localY)
                                    {
                                        double normalizedY = ((double)(localY - 1) + 0.5D - y) / verticalRadius;

                                        if ((normalizedX * normalizedX + normalizedZ * normalizedZ) * (double)this.ravineWidthFactors[localY - 1] + normalizedY * normalizedY / 6.0D < 1.0D)
                                        {
                                            IBlockState currentState = primer.getBlockState(localX, localY, localZ);

                                            if (currentState.getBlock() == Blocks.grass)
                                            {
                                                foundSurfaceGrass = true;
                                            }

                                            if (currentState.getBlock() == Blocks.stone || currentState.getBlock() == Blocks.dirt || currentState.getBlock() == Blocks.grass)
                                            {
                                                if (localY - 1 < 10)
                                                {
                                                    primer.setBlockState(localX, localY, localZ, Blocks.flowing_lava.getDefaultState());
                                                }
                                                else
                                                {
                                                    primer.setBlockState(localX, localY, localZ, Blocks.air.getDefaultState());

                                                    if (foundSurfaceGrass && primer.getBlockState(localX, localY - 1, localZ).getBlock() == Blocks.dirt)
                                                    {
                                                        biomeLookupPos.set(localX + chunkX * 16, 0, localZ + chunkZ * 16);
                                                        primer.setBlockState(localX, localY - 1, localZ, this.worldObj.getBiomeGenForCoords(biomeLookupPos).topBlock);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (singleNode)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalChunkX, int originalChunkZ, ChunkPrimer chunkPrimerIn)
    {
        if (this.rand.nextInt(50) == 0)
        {
            double ravineX = (double)(chunkX * 16 + this.rand.nextInt(16));
            double ravineY = (double)(this.rand.nextInt(this.rand.nextInt(40) + 8) + 20);
            double ravineZ = (double)(chunkZ * 16 + this.rand.nextInt(16));
            int ravineCount = 1;

            for (int ravineIndex = 0; ravineIndex < ravineCount; ++ravineIndex)
            {
                float ravineYaw = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float ravinePitch = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float ravineWidth = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
                this.addTunnel(this.rand.nextLong(), originalChunkX, originalChunkZ, chunkPrimerIn, ravineX, ravineY, ravineZ, ravineWidth, ravineYaw, ravinePitch, 0, 0, 3.0D);
            }
        }
    }
}
