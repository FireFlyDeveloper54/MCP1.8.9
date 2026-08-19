package net.minecraft.world.chunk.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.lighting.LightingHooks;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilChunkLoader implements IChunkLoader, IThreadedFileIO
{
    private static final Logger logger = LogManager.getLogger();
    private Map<ChunkCoordIntPair, NBTTagCompound> chunksToRemove = new ConcurrentHashMap();
    private Set<ChunkCoordIntPair> pendingAnvilChunksCoordinates = Collections.<ChunkCoordIntPair>newSetFromMap(new ConcurrentHashMap());
    private final File chunkSaveLocation;
    private boolean savingExtraData = false;

    public AnvilChunkLoader(File chunkSaveLocationIn)
    {
        this.chunkSaveLocation = chunkSaveLocationIn;
    }

    public Chunk loadChunk(World worldIn, int x, int z) throws IOException
    {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x, z);
        NBTTagCompound nbttagcompound = (NBTTagCompound)this.chunksToRemove.get(chunkcoordintpair);

        if (nbttagcompound == null)
        {
            DataInputStream datainputstream = RegionFileCache.getChunkInputStream(this.chunkSaveLocation, x, z);

            if (datainputstream == null)
            {
                return null;
            }

            nbttagcompound = CompressedStreamTools.read(datainputstream);
        }

        return this.checkedReadChunkFromNBT(worldIn, x, z, nbttagcompound);
    }

    protected Chunk checkedReadChunkFromNBT(World worldIn, int x, int z, NBTTagCompound chunkTag)
    {
        if (!chunkTag.hasKey("Level", 10))
        {
            logger.error("Chunk file at " + x + "," + z + " is missing level data, skipping");
            return null;
        }
        else
        {
            NBTTagCompound nBTTagCompound = chunkTag.getCompoundTag("Level");

            if (!nBTTagCompound.hasKey("Sections", 9))
            {
                logger.error("Chunk file at " + x + "," + z + " is missing block data, skipping");
                return null;
            }
            else
            {
                Chunk chunk = this.readChunkFromNBT(worldIn, nBTTagCompound);

                if (!chunk.isAtLocation(x, z))
                {
                    logger.error("Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk.xPosition + ", " + chunk.zPosition + ")");
                    nBTTagCompound.setInteger("xPos", x);
                    nBTTagCompound.setInteger("zPos", z);
                    chunk = this.readChunkFromNBT(worldIn, nBTTagCompound);
                }

                return chunk;
            }
        }
    }

    public void saveChunk(World worldIn, Chunk chunkIn) throws MinecraftException, IOException
    {
        worldIn.checkSessionLock();

        try
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound.setTag("Level", nbttagcompound1);
            this.writeChunkToNBT(chunkIn, worldIn, nbttagcompound1);
            this.addChunkToPending(chunkIn.getChunkCoordIntPair(), nbttagcompound);
        }
        catch (Exception exception)
        {
            logger.error((String)"Failed to save chunk", (Throwable)exception);
        }
    }

    protected void addChunkToPending(ChunkCoordIntPair chunkCoord, NBTTagCompound chunkTag)
    {
        if (!this.pendingAnvilChunksCoordinates.contains(chunkCoord))
        {
            this.chunksToRemove.put(chunkCoord, chunkTag);
        }

        ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
    }

    public boolean writeNextIO()
    {
        if (this.chunksToRemove.isEmpty())
        {
            if (this.savingExtraData)
            {
                logger.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", new Object[] {this.chunkSaveLocation.getName()});
            }

            return false;
        }
        else
        {
            ChunkCoordIntPair chunkCoordIntPair = (ChunkCoordIntPair)this.chunksToRemove.keySet().iterator().next();
            boolean wroteChunk;

            try
            {
                this.pendingAnvilChunksCoordinates.add(chunkCoordIntPair);
                NBTTagCompound nBTTagCompound = (NBTTagCompound)this.chunksToRemove.remove(chunkCoordIntPair);

                if (nBTTagCompound != null)
                {
                    try
                    {
                        this.writeChunkData(chunkCoordIntPair, nBTTagCompound);
                    }
                    catch (Exception exception)
                    {
                        logger.error((String)"Failed to save chunk", (Throwable)exception);
                    }
                }

                wroteChunk = true;
            }
            finally
            {
                this.pendingAnvilChunksCoordinates.remove(chunkCoordIntPair);
            }

            return wroteChunk;
        }
    }

    private void writeChunkData(ChunkCoordIntPair chunkCoord, NBTTagCompound chunkTag) throws IOException
    {
        DataOutputStream dataoutputstream = RegionFileCache.getChunkOutputStream(this.chunkSaveLocation, chunkCoord.chunkXPos, chunkCoord.chunkZPos);
        CompressedStreamTools.write(chunkTag, dataoutputstream);
        dataoutputstream.close();
    }

    public void saveExtraChunkData(World worldIn, Chunk chunkIn) throws IOException
    {
    }

    public void chunkTick()
    {
    }

    public void saveExtraData()
    {
        try
        {
            this.savingExtraData = true;

            while (true)
            {
                if (this.writeNextIO())
                {
                    continue;
                }
            }
        }
        finally
        {
            this.savingExtraData = false;
        }
    }

    private void writeChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound levelTag)
    {
        levelTag.setByte("V", (byte)1);
        levelTag.setInteger("xPos", chunkIn.xPosition);
        levelTag.setInteger("zPos", chunkIn.zPosition);
        levelTag.setLong("LastUpdate", worldIn.getTotalWorldTime());
        levelTag.setIntArray("HeightMap", chunkIn.getHeightMap());
        levelTag.setBoolean("TerrainPopulated", chunkIn.isTerrainPopulated());
        levelTag.setBoolean("LightPopulated", chunkIn.isLightPopulated());
        levelTag.setBoolean("LightInitialized", chunkIn.isLightInitialized());
        levelTag.setLong("InhabitedTime", chunkIn.getInhabitedTime());
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        NBTTagList nbttaglist = new NBTTagList();
        boolean flag = !worldIn.provider.getHasNoSky();

        for (ExtendedBlockStorage extendedblockstorage : aextendedblockstorage)
        {
            if (extendedblockstorage != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Y", (byte)(extendedblockstorage.getYLocation() >> 4 & 255));
                byte[] abyte = new byte[extendedblockstorage.getData().length];
                NibbleArray nibblearray = new NibbleArray();
                NibbleArray nibblearray1 = null;

                for (int i = 0; i < extendedblockstorage.getData().length; ++i)
                {
                    char character = extendedblockstorage.getData()[i];
                    int j = i & 15;
                    int k = i >> 8 & 15;
                    int l = i >> 4 & 15;

                    if (character >> 12 != 0)
                    {
                        if (nibblearray1 == null)
                        {
                            nibblearray1 = new NibbleArray();
                        }

                        nibblearray1.set(j, k, l, character >> 12);
                    }

                    abyte[i] = (byte)(character >> 4 & 255);
                    nibblearray.set(j, k, l, character & 15);
                }

                nbttagcompound.setByteArray("Blocks", abyte);
                nbttagcompound.setByteArray("Data", nibblearray.getData());

                if (nibblearray1 != null)
                {
                    nbttagcompound.setByteArray("Add", nibblearray1.getData());
                }

                nbttagcompound.setByteArray("BlockLight", extendedblockstorage.getBlocklightArray().getData());

                if (flag)
                {
                    nbttagcompound.setByteArray("SkyLight", extendedblockstorage.getSkylightArray().getData());
                }
                else
                {
                    nbttagcompound.setByteArray("SkyLight", new byte[extendedblockstorage.getBlocklightArray().getData().length]);
                }

                nbttaglist.appendTag(nbttagcompound);
            }
        }

        levelTag.setTag("Sections", nbttaglist);
        levelTag.setByteArray("Biomes", chunkIn.getBiomeArray());
        chunkIn.setHasEntities(false);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (int intValue = 0; intValue < chunkIn.getEntityLists().length; ++intValue)
        {
            for (Entity entity : chunkIn.getEntityLists()[intValue])
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                if (entity.writeToNBTOptional(nbttagcompound1))
                {
                    chunkIn.setHasEntities(true);
                    nbttaglist1.appendTag(nbttagcompound1);
                }
            }
        }

        levelTag.setTag("Entities", nbttaglist1);
        NBTTagList nbttaglist2 = new NBTTagList();

        for (TileEntity tileentity : chunkIn.getTileEntityMap().values())
        {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            tileentity.writeToNBT(nbttagcompound2);
            nbttaglist2.appendTag(nbttagcompound2);
        }

        levelTag.setTag("TileEntities", nbttaglist2);
        List<NextTickListEntry> list = worldIn.getPendingBlockUpdates(chunkIn, false);

        if (list != null)
        {
            long longValue = worldIn.getTotalWorldTime();
            NBTTagList nbttaglist3 = new NBTTagList();

            for (NextTickListEntry nextticklistentry : list)
            {
                NBTTagCompound nbttagcompound3 = new NBTTagCompound();
                ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(nextticklistentry.getBlock());
                nbttagcompound3.setString("i", resourcelocation == null ? "" : resourcelocation.toString());
                nbttagcompound3.setInteger("x", nextticklistentry.position.getX());
                nbttagcompound3.setInteger("y", nextticklistentry.position.getY());
                nbttagcompound3.setInteger("z", nextticklistentry.position.getZ());
                nbttagcompound3.setInteger("t", (int)(nextticklistentry.scheduledTime - longValue));
                nbttagcompound3.setInteger("p", nextticklistentry.priority);
                nbttaglist3.appendTag(nbttagcompound3);
            }

            levelTag.setTag("TileTicks", nbttaglist3);
        }

        LightingHooks.writeNeighborLightChecksToNBT(chunkIn, levelTag);
    }

    private Chunk readChunkFromNBT(World worldIn, NBTTagCompound levelTag)
    {
        int i = levelTag.getInteger("xPos");
        int j = levelTag.getInteger("zPos");
        Chunk chunk = new Chunk(worldIn, i, j);
        chunk.setHeightMap(levelTag.getIntArray("HeightMap"));
        chunk.setTerrainPopulated(levelTag.getBoolean("TerrainPopulated"));
        chunk.setLightPopulated(levelTag.getBoolean("LightPopulated"));
        chunk.setInhabitedTime(levelTag.getLong("InhabitedTime"));
        NBTTagList nbttaglist = levelTag.getTagList("Sections", 10);
        int k = 16;
        ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[k];
        boolean flag = !worldIn.provider.getHasNoSky();

        for (int l = 0; l < nbttaglist.tagCount(); ++l)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(l);
            int secondIntValue = nbttagcompound.getByte("Y");
            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(secondIntValue << 4, flag);
            byte[] abyte = nbttagcompound.getByteArray("Blocks");
            NibbleArray nibblearray = new NibbleArray(nbttagcompound.getByteArray("Data"));
            NibbleArray nibblearray1 = nbttagcompound.hasKey("Add", 7) ? new NibbleArray(nbttagcompound.getByteArray("Add")) : null;
            char[] achar = new char[abyte.length];

            for (int fifthIntValue = 0; fifthIntValue < achar.length; ++fifthIntValue)
            {
                int seventhIntValue = fifthIntValue & 15;
                int ninthIntValue = fifthIntValue >> 8 & 15;
                int thirdIntValue = fifthIntValue >> 4 & 15;
                int sixthIntValue = nibblearray1 != null ? nibblearray1.get(seventhIntValue, ninthIntValue, thirdIntValue) : 0;
                achar[fifthIntValue] = (char)(sixthIntValue << 12 | (abyte[fifthIntValue] & 255) << 4 | nibblearray.get(seventhIntValue, ninthIntValue, thirdIntValue));
            }

            extendedblockstorage.setData(achar);
            extendedblockstorage.setBlocklightArray(new NibbleArray(nbttagcompound.getByteArray("BlockLight")));

            if (flag)
            {
                extendedblockstorage.setSkylightArray(new NibbleArray(nbttagcompound.getByteArray("SkyLight")));
            }

            extendedblockstorage.removeInvalidBlocks();
            aextendedblockstorage[secondIntValue] = extendedblockstorage;
        }

        chunk.setStorageArrays(aextendedblockstorage);

        if (levelTag.hasKey("Biomes", 7))
        {
            chunk.setBiomeArray(levelTag.getByteArray("Biomes"));
        }

        NBTTagList nbttaglist1 = levelTag.getTagList("Entities", 10);

        if (nbttaglist1 != null)
        {
            for (int eighthIntValue = 0; eighthIntValue < nbttaglist1.tagCount(); ++eighthIntValue)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist1.getCompoundTagAt(eighthIntValue);
                Entity entity = EntityList.createEntityFromNBT(nbttagcompound1, worldIn);
                chunk.setHasEntities(true);

                if (entity != null)
                {
                    chunk.addEntity(entity);
                    Entity entity1 = entity;

                    for (NBTTagCompound nbttagcompound4 = nbttagcompound1; nbttagcompound4.hasKey("Riding", 10); nbttagcompound4 = nbttagcompound4.getCompoundTag("Riding"))
                    {
                        Entity entity2 = EntityList.createEntityFromNBT(nbttagcompound4.getCompoundTag("Riding"), worldIn);

                        if (entity2 != null)
                        {
                            chunk.addEntity(entity2);
                            entity1.mountEntity(entity2);
                        }

                        entity1 = entity2;
                    }
                }
            }
        }

        NBTTagList nbttaglist2 = levelTag.getTagList("TileEntities", 10);

        if (nbttaglist2 != null)
        {
            for (int tenthIntValue = 0; tenthIntValue < nbttaglist2.tagCount(); ++tenthIntValue)
            {
                NBTTagCompound nbttagcompound2 = nbttaglist2.getCompoundTagAt(tenthIntValue);
                TileEntity tileentity = TileEntity.createAndLoadEntity(nbttagcompound2);

                if (tileentity != null)
                {
                    chunk.addTileEntity(tileentity);
                }
            }
        }

        if (levelTag.hasKey("TileTicks", 9))
        {
            NBTTagList nbttaglist3 = levelTag.getTagList("TileTicks", 10);

            if (nbttaglist3 != null)
            {
                for (int fourthIntValue = 0; fourthIntValue < nbttaglist3.tagCount(); ++fourthIntValue)
                {
                    NBTTagCompound nbttagcompound3 = nbttaglist3.getCompoundTagAt(fourthIntValue);
                    Block block;

                    if (nbttagcompound3.hasKey("i", 8))
                    {
                        block = Block.getBlockFromName(nbttagcompound3.getString("i"));
                    }
                    else
                    {
                        block = Block.getBlockById(nbttagcompound3.getInteger("i"));
                    }

                    worldIn.scheduleBlockUpdate(new BlockPos(nbttagcompound3.getInteger("x"), nbttagcompound3.getInteger("y"), nbttagcompound3.getInteger("z")), block, nbttagcompound3.getInteger("t"), nbttagcompound3.getInteger("p"));
                }
            }
        }

        LightingHooks.readNeighborLightChecksFromNBT(chunk, levelTag);
        chunk.setLightInitialized(levelTag.hasKey("LightInitialized", 1) ? levelTag.getBoolean("LightInitialized") : levelTag.getBoolean("LightPopulated"));

        return chunk;
    }
}
