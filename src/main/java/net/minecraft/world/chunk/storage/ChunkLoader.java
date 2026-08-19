package net.minecraft.world.chunk.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.NibbleArray;

public class ChunkLoader
{
    public static ChunkLoader.AnvilConverterData load(NBTTagCompound nbt)
    {
        int i = nbt.getInteger("xPos");
        int j = nbt.getInteger("zPos");
        ChunkLoader.AnvilConverterData chunkloader$anvilconverterdata = new ChunkLoader.AnvilConverterData(i, j);
        chunkloader$anvilconverterdata.blocks = nbt.getByteArray("Blocks");
        chunkloader$anvilconverterdata.data = new NibbleArrayReader(nbt.getByteArray("Data"), 7);
        chunkloader$anvilconverterdata.skyLight = new NibbleArrayReader(nbt.getByteArray("SkyLight"), 7);
        chunkloader$anvilconverterdata.blockLight = new NibbleArrayReader(nbt.getByteArray("BlockLight"), 7);
        chunkloader$anvilconverterdata.heightmap = nbt.getByteArray("HeightMap");
        chunkloader$anvilconverterdata.terrainPopulated = nbt.getBoolean("TerrainPopulated");
        chunkloader$anvilconverterdata.entities = nbt.getTagList("Entities", 10);
        chunkloader$anvilconverterdata.tileEntities = nbt.getTagList("TileEntities", 10);
        chunkloader$anvilconverterdata.tileTicks = nbt.getTagList("TileTicks", 10);

        try
        {
            chunkloader$anvilconverterdata.lastUpdated = nbt.getLong("LastUpdate");
        }
        catch (ClassCastException caughtClassCastException)
        {
            chunkloader$anvilconverterdata.lastUpdated = (long)nbt.getInteger("LastUpdate");
        }

        return chunkloader$anvilconverterdata;
    }

    public static void convertToAnvilFormat(ChunkLoader.AnvilConverterData converterData, NBTTagCompound compound, WorldChunkManager worldChunkManager)
    {
        compound.setInteger("xPos", converterData.x);
        compound.setInteger("zPos", converterData.z);
        compound.setLong("LastUpdate", converterData.lastUpdated);
        int[] aint = new int[converterData.heightmap.length];

        for (int i = 0; i < converterData.heightmap.length; ++i)
        {
            aint[i] = converterData.heightmap[i];
        }

        compound.setIntArray("HeightMap", aint);
        compound.setBoolean("TerrainPopulated", converterData.terrainPopulated);
        NBTTagList nBTTagList = new NBTTagList();

        for (int sectionY = 0; sectionY < 8; ++sectionY)
        {
            boolean emptySection = true;

            for (int x = 0; x < 16 && emptySection; ++x)
            {
                for (int z = 0; z < 16 && emptySection; ++z)
                {
                    for (int y = 0; y < 16; ++y)
                    {
                        int blockIndex = x << 11 | y << 7 | z + (sectionY << 4);
                        int blockId = converterData.blocks[blockIndex];

                        if (blockId != 0)
                        {
                            emptySection = false;
                            break;
                        }
                    }
                }
            }

            if (!emptySection)
            {
                byte[] blocks = new byte[4096];
                NibbleArray data = new NibbleArray();
                NibbleArray skyLight = new NibbleArray();
                NibbleArray blockLight = new NibbleArray();

                for (int x = 0; x < 16; ++x)
                {
                    for (int z = 0; z < 16; ++z)
                    {
                        for (int y = 0; y < 16; ++y)
                        {
                            int blockIndex = x << 11 | y << 7 | z + (sectionY << 4);
                            int blockId = converterData.blocks[blockIndex];
                            blocks[z << 8 | y << 4 | x] = (byte)(blockId & 255);
                            data.set(x, z, y, converterData.data.get(x, z + (sectionY << 4), y));
                            skyLight.set(x, z, y, converterData.skyLight.get(x, z + (sectionY << 4), y));
                            blockLight.set(x, z, y, converterData.blockLight.get(x, z + (sectionY << 4), y));
                        }
                    }
                }

                NBTTagCompound sectionTag = new NBTTagCompound();
                sectionTag.setByte("Y", (byte)(sectionY & 255));
                sectionTag.setByteArray("Blocks", blocks);
                sectionTag.setByteArray("Data", data.getData());
                sectionTag.setByteArray("SkyLight", skyLight.getData());
                sectionTag.setByteArray("BlockLight", blockLight.getData());
                nBTTagList.appendTag(sectionTag);
            }
        }

        compound.setTag("Sections", nBTTagList);
        byte[] biomeArray = new byte[256];
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; ++x)
        {
            for (int z = 0; z < 16; ++z)
            {
                mutableBlockPos.set(converterData.x << 4 | x, 0, converterData.z << 4 | z);
                biomeArray[z << 4 | x] = (byte)(worldChunkManager.getBiomeGenerator(mutableBlockPos, BiomeGenBase.DEFAULT_BIOME).biomeID & 255);
            }
        }

        compound.setByteArray("Biomes", biomeArray);
        compound.setTag("Entities", converterData.entities);
        compound.setTag("TileEntities", converterData.tileEntities);

        if (converterData.tileTicks != null)
        {
            compound.setTag("TileTicks", converterData.tileTicks);
        }
    }

    public static class AnvilConverterData
    {
        public long lastUpdated;
        public boolean terrainPopulated;
        public byte[] heightmap;
        public NibbleArrayReader blockLight;
        public NibbleArrayReader skyLight;
        public NibbleArrayReader data;
        public byte[] blocks;
        public NBTTagList entities;
        public NBTTagList tileEntities;
        public NBTTagList tileTicks;
        public final int x;
        public final int z;

        public AnvilConverterData(int xIn, int zIn)
        {
            this.x = xIn;
            this.z = zIn;
        }
    }
}
