package net.optifine;

import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

public class ClearWater
{
    public static void updateWaterOpacity(GameSettings settings, World world)
    {
        if (settings != null)
        {
            int waterLightOpacity = 3;

            if (settings.ofClearWater)
            {
                waterLightOpacity = 1;
            }

            BlockAir.setLightOpacity(Blocks.water, waterLightOpacity);
            BlockAir.setLightOpacity(Blocks.flowing_water, waterLightOpacity);
        }

        if (world != null)
        {
            IChunkProvider ichunkprovider = world.getChunkProvider();

            if (ichunkprovider != null)
            {
                Entity entity = Config.getMinecraft().getRenderViewEntity();

                if (entity != null)
                {
                    int entityChunkX = (int)entity.posX / 16;
                    int entityChunkZ = (int)entity.posZ / 16;
                    int minChunkX = entityChunkX - 512;
                    int maxChunkX = entityChunkX + 512;
                    int minChunkZ = entityChunkZ - 512;
                    int maxChunkZ = entityChunkZ + 512;
                    int relightCount = 0;

                    for (int chunkX = minChunkX; chunkX < maxChunkX; ++chunkX)
                    {
                        for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; ++chunkZ)
                        {
                            if (ichunkprovider.chunkExists(chunkX, chunkZ))
                            {
                                Chunk chunk = ichunkprovider.provideChunk(chunkX, chunkZ);

                                if (chunk != null && !(chunk instanceof EmptyChunk))
                                {
                                    int minBlockX = chunkX << 4;
                                    int minBlockZ = chunkZ << 4;
                                    int maxBlockX = minBlockX + 16;
                                    int maxBlockZ = minBlockZ + 16;
                                    BlockPosM precipitationQueryPos = new BlockPosM(0, 0, 0);
                                    BlockPosM waterCheckPos = new BlockPosM(0, 0, 0);

                                    for (int blockX = minBlockX; blockX < maxBlockX; ++blockX)
                                    {
                                        for (int blockZ = minBlockZ; blockZ < maxBlockZ; ++blockZ)
                                        {
                                            precipitationQueryPos.setXyz(blockX, 0, blockZ);
                                            BlockPos precipitationHeight = world.getPrecipitationHeight(precipitationQueryPos);

                                            for (int blockY = 0; blockY < precipitationHeight.getY(); ++blockY)
                                            {
                                                waterCheckPos.setXyz(blockX, blockY, blockZ);
                                                IBlockState blockState = world.getBlockState(waterCheckPos);

                                                if (blockState.getBlock().getMaterial() == Material.water)
                                                {
                                                    world.markBlocksDirtyVertical(blockX, blockZ, waterCheckPos.getY(), precipitationHeight.getY());
                                                    ++relightCount;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (relightCount > 0)
                    {
                        String sideName = "server";

                        if (Config.isMinecraftThread())
                        {
                            sideName = "client";
                        }

                        Config.dbg("ClearWater (" + sideName + ") relighted " + relightCount + " chunks");
                    }
                }
            }
        }
    }
}
