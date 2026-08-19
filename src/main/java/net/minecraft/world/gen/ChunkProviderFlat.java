package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkProviderFlat implements IChunkProvider
{
    private World worldObj;
    private Random random;
    private final IBlockState[] cachedBlockIDs = new IBlockState[256];
    private final FlatGeneratorInfo flatWorldGenInfo;
    private final List<MapGenStructure> structureGenerators = Lists.<MapGenStructure>newArrayList();
    private final boolean hasDecoration;
    private final boolean hasDungeons;
    private WorldGenLakes waterLakeGenerator;
    private WorldGenLakes lavaLakeGenerator;

    public ChunkProviderFlat(World worldIn, long seed, boolean generateStructures, String flatGeneratorSettings)
    {
        this.worldObj = worldIn;
        this.random = new Random(seed);
        this.flatWorldGenInfo = FlatGeneratorInfo.createFlatGeneratorFromString(flatGeneratorSettings);

        if (generateStructures)
        {
            Map<String, Map<String, String>> featureMap = this.flatWorldGenInfo.getWorldFeatures();

            if (featureMap.containsKey("village"))
            {
                Map<String, String> villageOptions = (Map)featureMap.get("village");

                if (!villageOptions.containsKey("size"))
                {
                    villageOptions.put("size", "1");
                }

                this.structureGenerators.add(new MapGenVillage(villageOptions));
            }

            if (featureMap.containsKey("biome_1"))
            {
                this.structureGenerators.add(new MapGenScatteredFeature((Map)featureMap.get("biome_1")));
            }

            if (featureMap.containsKey("mineshaft"))
            {
                this.structureGenerators.add(new MapGenMineshaft((Map)featureMap.get("mineshaft")));
            }

            if (featureMap.containsKey("stronghold"))
            {
                this.structureGenerators.add(new MapGenStronghold((Map)featureMap.get("stronghold")));
            }

            if (featureMap.containsKey("oceanmonument"))
            {
                this.structureGenerators.add(new StructureOceanMonument((Map)featureMap.get("oceanmonument")));
            }
        }

        if (this.flatWorldGenInfo.getWorldFeatures().containsKey("lake"))
        {
            this.waterLakeGenerator = new WorldGenLakes(Blocks.water);
        }

        if (this.flatWorldGenInfo.getWorldFeatures().containsKey("lava_lake"))
        {
            this.lavaLakeGenerator = new WorldGenLakes(Blocks.lava);
        }

        this.hasDungeons = this.flatWorldGenInfo.getWorldFeatures().containsKey("dungeon");
        int seaLevel = 0;
        int pendingAirLayers = 0;
        boolean allLayersAir = true;

        for (FlatLayerInfo flatLayerInfo : this.flatWorldGenInfo.getFlatLayers())
        {
            for (int y = flatLayerInfo.getMinY(); y < flatLayerInfo.getMinY() + flatLayerInfo.getLayerCount(); ++y)
            {
                IBlockState layerState = flatLayerInfo.getLayerMaterial();

                if (layerState.getBlock() != Blocks.air)
                {
                    allLayersAir = false;
                    this.cachedBlockIDs[y] = layerState;
                }
            }

            if (flatLayerInfo.getLayerMaterial().getBlock() == Blocks.air)
            {
                pendingAirLayers += flatLayerInfo.getLayerCount();
            }
            else
            {
                seaLevel += flatLayerInfo.getLayerCount() + pendingAirLayers;
                pendingAirLayers = 0;
            }
        }

        worldIn.setSeaLevel(seaLevel);
        this.hasDecoration = allLayersAir ? false : this.flatWorldGenInfo.getWorldFeatures().containsKey("decoration");
    }

    public Chunk provideChunk(int x, int z)
    {
        ChunkPrimer chunkPrimer = new ChunkPrimer();

        for (int y = 0; y < this.cachedBlockIDs.length; ++y)
        {
            IBlockState cachedState = this.cachedBlockIDs[y];

            if (cachedState != null)
            {
                for (int localX = 0; localX < 16; ++localX)
                {
                    for (int localZ = 0; localZ < 16; ++localZ)
                    {
                        chunkPrimer.setBlockState(localX, y, localZ, cachedState);
                    }
                }
            }
        }

        for (MapGenBase mapGenBase : this.structureGenerators)
        {
            mapGenBase.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        Chunk chunk = new Chunk(this.worldObj, chunkPrimer, x, z);
        BiomeGenBase[] biomes = this.worldObj.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, x * 16, z * 16, 16, 16);
        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex)
        {
            biomeArray[biomeIndex] = (byte)biomes[biomeIndex].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    public boolean chunkExists(int x, int z)
    {
        return true;
    }

    public void populate(IChunkProvider chunkProvider, int x, int z)
    {
        int blockX = x * 16;
        int blockZ = z * 16;
        BlockPos chunkOrigin = new BlockPos(blockX, 0, blockZ);
        BiomeGenBase biomeGenBase = this.worldObj.getBiomeGenForCoords(new BlockPos(blockX + 16, 0, blockZ + 16));
        boolean villageGenerated = false;
        this.random.setSeed(this.worldObj.getSeed());
        long xSeedMultiplier = this.random.nextLong() / 2L * 2L + 1L;
        long zSeedMultiplier = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long)x * xSeedMultiplier + (long)z * zSeedMultiplier ^ this.worldObj.getSeed());
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(x, z);

        for (MapGenStructure mapGenStructure : this.structureGenerators)
        {
            boolean generatedStructure = mapGenStructure.generateStructure(this.worldObj, this.random, chunkCoordIntPair);

            if (mapGenStructure instanceof MapGenVillage)
            {
                villageGenerated |= generatedStructure;
            }
        }

        if (this.waterLakeGenerator != null && !villageGenerated && this.random.nextInt(4) == 0)
        {
            this.waterLakeGenerator.generate(this.worldObj, this.random, chunkOrigin.add(this.random.nextInt(16) + 8, this.random.nextInt(256), this.random.nextInt(16) + 8));
        }

        if (this.lavaLakeGenerator != null && !villageGenerated && this.random.nextInt(8) == 0)
        {
            BlockPos lavaLakePos = chunkOrigin.add(this.random.nextInt(16) + 8, this.random.nextInt(this.random.nextInt(248) + 8), this.random.nextInt(16) + 8);

            if (lavaLakePos.getY() < this.worldObj.getSeaLevel() || this.random.nextInt(10) == 0)
            {
                this.lavaLakeGenerator.generate(this.worldObj, this.random, lavaLakePos);
            }
        }

        if (this.hasDungeons)
        {
            for (int dungeonAttempt = 0; dungeonAttempt < 8; ++dungeonAttempt)
            {
                (new WorldGenDungeons()).generate(this.worldObj, this.random, chunkOrigin.add(this.random.nextInt(16) + 8, this.random.nextInt(256), this.random.nextInt(16) + 8));
            }
        }

        if (this.hasDecoration)
        {
            biomeGenBase.decorate(this.worldObj, this.random, chunkOrigin);
        }
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z)
    {
        return false;
    }

    public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback)
    {
        return true;
    }

    public void saveExtraData()
    {
    }

    public boolean unloadQueuedChunks()
    {
        return false;
    }

    public boolean canSave()
    {
        return true;
    }

    public String makeString()
    {
        return "FlatLevelSource";
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        BiomeGenBase biomeGenBase = this.worldObj.getBiomeGenForCoords(pos);
        return biomeGenBase.getSpawnableList(creatureType);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        if ("Stronghold".equals(structureName))
        {
            for (MapGenStructure mapGenStructure : this.structureGenerators)
            {
                if (mapGenStructure instanceof MapGenStronghold)
                {
                    return mapGenStructure.getClosestStrongholdPos(worldIn, position);
                }
            }
        }

        return null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        for (MapGenStructure mapGenStructure : this.structureGenerators)
        {
            mapGenStructure.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
        }
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
