package net.optifine.util;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class IntegratedServerUtils
{
    public static WorldServer getWorldServer()
    {
        Minecraft minecraft = Config.getMinecraft();
        World world = minecraft.theWorld;

        if (world == null)
        {
            return null;
        }
        else if (!minecraft.isIntegratedServerRunning())
        {
            return null;
        }
        else
        {
            IntegratedServer integratedServer = minecraft.getIntegratedServer();

            if (integratedServer == null)
            {
                return null;
            }
            else
            {
                WorldProvider worldProvider = world.provider;

                if (worldProvider == null)
                {
                    return null;
                }
                else
                {
                    int dimensionId = worldProvider.getDimensionId();

                    try
                    {
                        WorldServer worldServer = integratedServer.worldServerForDimension(dimensionId);
                        return worldServer;
                    }
                    catch (NullPointerException caughtNullPointerException)
                    {
                        return null;
                    }
                }
            }
        }
    }

    public static Entity getEntity(UUID uUID)
    {
        WorldServer worldServer = getWorldServer();

        if (worldServer == null)
        {
            return null;
        }
        else
        {
            Entity entity = worldServer.getEntityFromUuid(uUID);
            return entity;
        }
    }

    public static TileEntity getTileEntity(BlockPos pos)
    {
        WorldServer worldServer = getWorldServer();

        if (worldServer == null)
        {
            return null;
        }
        else
        {
            Chunk chunk = worldServer.getChunkProvider().provideChunk(pos.getX() >> 4, pos.getZ() >> 4);

            if (chunk == null)
            {
                return null;
            }
            else
            {
                TileEntity tileEntity = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
                return tileEntity;
            }
        }
    }
}
