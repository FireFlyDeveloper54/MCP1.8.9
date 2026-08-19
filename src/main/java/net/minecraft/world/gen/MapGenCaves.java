package net.minecraft.world.gen;

import com.google.common.base.MoreObjects;
import java.util.Random;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenCaves extends MapGenBase
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

            if (!isRoom && start == branchStep && width > 1.0F && end > 0)
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
                                boolean foundSurfaceBlock = false;

                                if (normalizedX * normalizedX + normalizedZ * normalizedZ < 1.0D)
                                {
                                    for (int localY = maxY; localY > minY; --localY)
                                    {
                                        double normalizedY = ((double)(localY - 1) + 0.5D - y) / verticalRadius;

                                        if (normalizedY > -0.7D && normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ < 1.0D)
                                        {
                                            IBlockState currentState = primer.getBlockState(localX, localY, localZ);
                                            IBlockState aboveState = (IBlockState)MoreObjects.firstNonNull(primer.getBlockState(localX, localY + 1, localZ), Blocks.air.getDefaultState());

                                            if (currentState.getBlock() == Blocks.grass || currentState.getBlock() == Blocks.mycelium)
                                            {
                                                foundSurfaceBlock = true;
                                            }

                                            if (this.canReplaceBlock(currentState, aboveState))
                                            {
                                                if (localY - 1 < 10)
                                                {
                                                    primer.setBlockState(localX, localY, localZ, Blocks.lava.getDefaultState());
                                                }
                                                else
                                                {
                                                    primer.setBlockState(localX, localY, localZ, Blocks.air.getDefaultState());

                                                    if (aboveState.getBlock() == Blocks.sand)
                                                    {
                                                        primer.setBlockState(localX, localY + 1, localZ, aboveState.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? Blocks.red_sandstone.getDefaultState() : Blocks.sandstone.getDefaultState());
                                                    }

                                                    if (foundSurfaceBlock && primer.getBlockState(localX, localY - 1, localZ).getBlock() == Blocks.dirt)
                                                    {
                                                        biomeLookupPos.set(localX + chunkX * 16, 0, localZ + chunkZ * 16);
                                                        primer.setBlockState(localX, localY - 1, localZ, this.worldObj.getBiomeGenForCoords(biomeLookupPos).topBlock.getBlock().getDefaultState());
                                                    }
                                                }
                                            }
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

    protected boolean canReplaceBlock(IBlockState state, IBlockState aboveState)
    {
        return state.getBlock() == Blocks.stone ? true : (state.getBlock() == Blocks.dirt ? true : (state.getBlock() == Blocks.grass ? true : (state.getBlock() == Blocks.hardened_clay ? true : (state.getBlock() == Blocks.stained_hardened_clay ? true : (state.getBlock() == Blocks.sandstone ? true : (state.getBlock() == Blocks.red_sandstone ? true : (state.getBlock() == Blocks.mycelium ? true : (state.getBlock() == Blocks.snow_layer ? true : (state.getBlock() == Blocks.sand || state.getBlock() == Blocks.gravel) && aboveState.getBlock().getMaterial() != Material.water))))))));
    }

    protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalChunkX, int originalChunkZ, ChunkPrimer chunkPrimerIn)
    {
        int caveCount = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(7) != 0)
        {
            caveCount = 0;
        }

        for (int caveIndex = 0; caveIndex < caveCount; ++caveIndex)
        {
            double caveX = (double)(chunkX * 16 + this.rand.nextInt(16));
            double caveY = (double)this.rand.nextInt(this.rand.nextInt(120) + 8);
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

                if (this.rand.nextInt(10) == 0)
                {
                    tunnelWidth *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }

                this.addTunnel(this.rand.nextLong(), originalChunkX, originalChunkZ, chunkPrimerIn, caveX, caveY, caveZ, tunnelWidth, tunnelYaw, tunnelPitch, 0, 0, 1.0D);
            }
        }
    }
}
