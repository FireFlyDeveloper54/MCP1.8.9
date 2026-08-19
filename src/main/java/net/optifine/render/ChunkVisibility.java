package net.optifine.render;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ChunkVisibility
{
    public static final int MASK_FACINGS = 63;
    public static final EnumFacing[][] enumFacingArrays = makeEnumFacingArrays(false);
    public static final EnumFacing[][] enumFacingOppositeArrays = makeEnumFacingArrays(true);
    private static int counter = 0;
    private static int iMaxStatic = -1;
    private static int iMaxStaticFinal = 16;
    private static World worldLast = null;
    private static int pcxLast = Integer.MIN_VALUE;
    private static int pczLast = Integer.MIN_VALUE;

    public static int getMaxChunkY(World world, Entity viewEntity, int renderDistanceChunks)
    {
        int chunkX = MathHelper.floor_double(viewEntity.posX) >> 4;
        int chunkY = MathHelper.floor_double(viewEntity.posY) >> 4;
        int chunkZ = MathHelper.floor_double(viewEntity.posZ) >> 4;
        Chunk viewChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int minChunkX = chunkX - renderDistanceChunks;
        int maxChunkX = chunkX + renderDistanceChunks;
        int minChunkZ = chunkZ - renderDistanceChunks;
        int maxChunkZ = chunkZ + renderDistanceChunks;

        if (world != worldLast || chunkX != pcxLast || chunkZ != pczLast)
        {
            counter = 0;
            iMaxStaticFinal = 16;
            worldLast = world;
            pcxLast = chunkX;
            pczLast = chunkZ;
        }

        if (counter == 0)
        {
            iMaxStatic = -1;
        }

        int maxChunkY = iMaxStatic;

        switch (counter)
        {
            case 0:
                maxChunkX = chunkX;
                maxChunkZ = chunkZ;
                break;

            case 1:
                minChunkX = chunkX;
                maxChunkZ = chunkZ;
                break;

            case 2:
                maxChunkX = chunkX;
                minChunkZ = chunkZ;
                break;

            case 3:
                minChunkX = chunkX;
                minChunkZ = chunkZ;
        }

        for (int scanChunkX = minChunkX; scanChunkX < maxChunkX; ++scanChunkX)
        {
            for (int scanChunkZ = minChunkZ; scanChunkZ < maxChunkZ; ++scanChunkZ)
            {
                Chunk scanChunk = world.getChunkFromChunkCoords(scanChunkX, scanChunkZ);

                if (!scanChunk.isEmpty())
                {
                    ExtendedBlockStorage[] blockStorages = scanChunk.getBlockStorageArray();

                    for (int storageY = blockStorages.length - 1; storageY > maxChunkY; --storageY)
                    {
                        ExtendedBlockStorage blockStorage = blockStorages[storageY];

                        if (blockStorage != null && !blockStorage.isEmpty())
                        {
                            if (storageY > maxChunkY)
                            {
                                maxChunkY = storageY;
                            }

                            break;
                        }
                    }

                    try
                    {
                        Map<BlockPos, TileEntity> tileEntityMap = scanChunk.getTileEntityMap();

                        if (!tileEntityMap.isEmpty())
                        {
                            for (BlockPos tileEntityPos : tileEntityMap.keySet())
                            {
                                int tileEntityChunkY = tileEntityPos.getY() >> 4;

                                if (tileEntityChunkY > maxChunkY)
                                {
                                    maxChunkY = tileEntityChunkY;
                                }
                            }
                        }
                    }
                    catch (ConcurrentModificationException caughtConcurrentModificationException)
                    {
                        ;
                    }

                    ClassInheritanceMultiMap<Entity>[] entityLists = scanChunk.getEntityLists();

                    for (int entityChunkY = entityLists.length - 1; entityChunkY > maxChunkY; --entityChunkY)
                    {
                        ClassInheritanceMultiMap<Entity> entityList = entityLists[entityChunkY];

                        if (!entityList.isEmpty() && (scanChunk != viewChunk || entityChunkY != chunkY || entityList.size() != 1))
                        {
                            if (entityChunkY > maxChunkY)
                            {
                                maxChunkY = entityChunkY;
                            }

                            break;
                        }
                    }
                }
            }
        }

        if (counter < 3)
        {
            iMaxStatic = maxChunkY;
            maxChunkY = iMaxStaticFinal;
        }
        else
        {
            iMaxStaticFinal = maxChunkY;
            iMaxStatic = -1;
        }

        counter = (counter + 1) % 4;
        return maxChunkY << 4;
    }

    public static boolean isFinished()
    {
        return counter == 0;
    }

    private static EnumFacing[][] makeEnumFacingArrays(boolean opposite)
    {
        int maskCount = 64;
        EnumFacing[][] facingArrays = new EnumFacing[maskCount][];

        for (int mask = 0; mask < maskCount; ++mask)
        {
            List<EnumFacing> facings = new ArrayList();

            for (int facingIndex = 0; facingIndex < EnumFacing.VALUES.length; ++facingIndex)
            {
                EnumFacing facing = EnumFacing.VALUES[facingIndex];
                EnumFacing maskFacing = opposite ? facing.getOpposite() : facing;
                int facingMask = 1 << maskFacing.ordinal();

                if ((mask & facingMask) != 0)
                {
                    facings.add(facing);
                }
            }

            EnumFacing[] facingArray = (EnumFacing[])facings.toArray(new EnumFacing[facings.size()]);
            facingArrays[mask] = facingArray;
        }

        return facingArrays;
    }

    public static EnumFacing[] getFacingsNotOpposite(int setDisabled)
    {
        int enabledMask = ~setDisabled & 63;
        return enumFacingOppositeArrays[enabledMask];
    }

    public static void reset()
    {
        worldLast = null;
    }
}
