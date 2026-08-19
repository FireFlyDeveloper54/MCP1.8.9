package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenCavesHell extends MapGenBase
{
    protected void addRoom(long seed, int chunkX, int chunkZ, ChunkPrimer primer, double x, double y, double z)
    {
        this.addTunnel(seed, chunkX, chunkZ, primer, x, y, z, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void addTunnel(long seed, int chunkX, int chunkZ, ChunkPrimer primer, double x, double y, double z, float width, float yaw, float pitch, int start, int end, double heightScale)
    {
        double chunkCenterX = (double)(chunkX * 16 + 8);
        double chunkCenterZ = (double)(chunkZ * 16 + 8);
        float yawDelta = 0.0F;
        float pitchDelta = 0.0F;
        Random tunnelRandom = new Random(seed);

        if (end <= 0)
        {
            int maxTunnelLength = this.range * 16 - 16;
            end = maxTunnelLength - tunnelRandom.nextInt(maxTunnelLength / 4);
        }

        boolean isRoom = false;

        if (start == -1)
        {
            start = end / 2;
            isRoom = true;
        }

        int branchStep = tunnelRandom.nextInt(end / 2) + end / 4;

        for (boolean slowPitchChange = tunnelRandom.nextInt(6) == 0; start < end; ++start)
        {
            double horizontalRadius = 1.5D + (double)(MathHelper.sin((float)start * (float)Math.PI / (float)end) * width * 1.0F);
            double verticalRadius = horizontalRadius * heightScale;
            float cosPitch = MathHelper.cos(pitch);
            float sinPitch = MathHelper.sin(pitch);
            x += (double)(MathHelper.cos(yaw) * cosPitch);
            y += (double)sinPitch;
            z += (double)(MathHelper.sin(yaw) * cosPitch);

            if (slowPitchChange)
            {
                pitch = pitch * 0.92F;
            }
            else
            {
                pitch = pitch * 0.7F;
            }

            pitch = pitch + pitchDelta * 0.1F;
            yaw += yawDelta * 0.1F;
            pitchDelta = pitchDelta * 0.9F;
            yawDelta = yawDelta * 0.75F;
            pitchDelta = pitchDelta + (tunnelRandom.nextFloat() - tunnelRandom.nextFloat()) * tunnelRandom.nextFloat() * 2.0F;
            yawDelta = yawDelta + (tunnelRandom.nextFloat() - tunnelRandom.nextFloat()) * tunnelRandom.nextFloat() * 4.0F;

            if (!isRoom && start == branchStep && width > 1.0F)
            {
                this.addTunnel(tunnelRandom.nextLong(), chunkX, chunkZ, primer, x, y, z, tunnelRandom.nextFloat() * 0.5F + 0.5F, yaw - ((float)Math.PI / 2F), pitch / 3.0F, start, end, 1.0D);
                this.addTunnel(tunnelRandom.nextLong(), chunkX, chunkZ, primer, x, y, z, tunnelRandom.nextFloat() * 0.5F + 0.5F, yaw + ((float)Math.PI / 2F), pitch / 3.0F, start, end, 1.0D);
                return;
            }

            if (isRoom || tunnelRandom.nextInt(4) != 0)
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

                    if (maxY > 120)
                    {
                        maxY = 120;
                    }

                    if (minZ < 0)
                    {
                        minZ = 0;
                    }

                    if (maxZ > 16)
                    {
                        maxZ = 16;
                    }

                    boolean foundLava = false;

                    for (int checkX = minX; !foundLava && checkX < maxX; ++checkX)
                    {
                        for (int checkZ = minZ; !foundLava && checkZ < maxZ; ++checkZ)
                        {
                            for (int checkY = maxY + 1; !foundLava && checkY >= minY - 1; --checkY)
                            {
                                if (checkY >= 0 && checkY < 128)
                                {
                                    IBlockState checkState = primer.getBlockState(checkX, checkY, checkZ);

                                    if (checkState.getBlock() == Blocks.flowing_lava || checkState.getBlock() == Blocks.lava)
                                    {
                                        foundLava = true;
                                    }

                                    if (checkY != minY - 1 && checkX != minX && checkX != maxX - 1 && checkZ != minZ && checkZ != maxZ - 1)
                                    {
                                        checkY = minY;
                                    }
                                }
                            }
                        }
                    }

                    if (!foundLava)
                    {
                        for (int localX = minX; localX < maxX; ++localX)
                        {
                            double normalizedX = ((double)(localX + chunkX * 16) + 0.5D - x) / horizontalRadius;

                            for (int localZ = minZ; localZ < maxZ; ++localZ)
                            {
                                double normalizedZ = ((double)(localZ + chunkZ * 16) + 0.5D - z) / horizontalRadius;

                                for (int localY = maxY; localY > minY; --localY)
                                {
                                    double normalizedY = ((double)(localY - 1) + 0.5D - y) / verticalRadius;

                                    if (normalizedY > -0.7D && normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ < 1.0D)
                                    {
                                        IBlockState currentState = primer.getBlockState(localX, localY, localZ);

                                        if (currentState.getBlock() == Blocks.netherrack || currentState.getBlock() == Blocks.dirt || currentState.getBlock() == Blocks.grass)
                                        {
                                            primer.setBlockState(localX, localY, localZ, Blocks.air.getDefaultState());
                                        }
                                    }
                                }
                            }
                        }

                        if (isRoom)
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
        int caveCount = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(10) + 1) + 1);

        if (this.rand.nextInt(5) != 0)
        {
            caveCount = 0;
        }

        for (int caveIndex = 0; caveIndex < caveCount; ++caveIndex)
        {
            double caveX = (double)(chunkX * 16 + this.rand.nextInt(16));
            double caveY = (double)this.rand.nextInt(128);
            double caveZ = (double)(chunkZ * 16 + this.rand.nextInt(16));
            int tunnelCount = 1;

            if (this.rand.nextInt(4) == 0)
            {
                this.addRoom(this.rand.nextLong(), originalChunkX, originalChunkZ, chunkPrimerIn, caveX, caveY, caveZ);
                tunnelCount += this.rand.nextInt(4);
            }

            for (int tunnelIndex = 0; tunnelIndex < tunnelCount; ++tunnelIndex)
            {
                float tunnelYaw = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float tunnelPitch = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float tunnelWidth = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
                this.addTunnel(this.rand.nextLong(), originalChunkX, originalChunkZ, chunkPrimerIn, caveX, caveY, caveZ, tunnelWidth * 2.0F, tunnelYaw, tunnelPitch, 0, 0, 0.5D);
            }
        }
    }
}
