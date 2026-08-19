package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkProviderGenerate implements IChunkProvider
{
    private Random rand;
    private NoiseGeneratorOctaves lowerLimitNoiseGen;
    private NoiseGeneratorOctaves upperLimitNoiseGen;
    private NoiseGeneratorOctaves mainNoiseGen;
    private NoiseGeneratorPerlin surfaceNoiseGen;
    public NoiseGeneratorOctaves noiseGen5;
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorOctaves mobSpawnerNoise;
    private World worldObj;
    private final boolean mapFeaturesEnabled;
    private WorldType worldType;
    private final double[] densityField;
    private final float[] parabolicField;
    private ChunkProviderSettings settings;
    private Block oceanBlockTmpl = Blocks.water;
    private double[] stoneNoise = new double[256];
    private MapGenBase caveGenerator = new MapGenCaves();
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    private MapGenBase ravineGenerator = new MapGenRavine();
    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    private BiomeGenBase[] biomesForGeneration;
    double[] mainNoiseArray;
    double[] lowerLimitNoiseArray;
    double[] upperLimitNoiseArray;
    double[] depthNoiseArray;

    public ChunkProviderGenerate(World worldIn, long seed, boolean generateStructures, String structuresJson)
    {
        this.worldObj = worldIn;
        this.mapFeaturesEnabled = generateStructures;
        this.worldType = worldIn.getWorldInfo().getTerrainType();
        this.rand = new Random(seed);
        this.lowerLimitNoiseGen = new NoiseGeneratorOctaves(this.rand, 16);
        this.upperLimitNoiseGen = new NoiseGeneratorOctaves(this.rand, 16);
        this.mainNoiseGen = new NoiseGeneratorOctaves(this.rand, 8);
        this.surfaceNoiseGen = new NoiseGeneratorPerlin(this.rand, 4);
        this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
        this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
        this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
        this.densityField = new double[825];
        this.parabolicField = new float[25];

        for (int offsetX = -2; offsetX <= 2; ++offsetX)
        {
            for (int offsetZ = -2; offsetZ <= 2; ++offsetZ)
            {
                float weight = 10.0F / MathHelper.sqrt_float((float)(offsetX * offsetX + offsetZ * offsetZ) + 0.2F);
                this.parabolicField[offsetX + 2 + (offsetZ + 2) * 5] = weight;
            }
        }

        if (structuresJson != null)
        {
            this.settings = ChunkProviderSettings.Factory.jsonToFactory(structuresJson).build();
            this.oceanBlockTmpl = this.settings.useLavaOceans ? Blocks.lava : Blocks.water;
            worldIn.setSeaLevel(this.settings.seaLevel);
        }
    }

    public void setBlocksInChunk(int x, int z, ChunkPrimer primer)
    {
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        this.initializeNoiseField(x * 4, 0, z * 4);

        for (int noiseX = 0; noiseX < 4; ++noiseX)
        {
            int noiseX0Offset = noiseX * 5;
            int noiseX1Offset = (noiseX + 1) * 5;

            for (int noiseZ = 0; noiseZ < 4; ++noiseZ)
            {
                int densityIndexX0Z0 = (noiseX0Offset + noiseZ) * 33;
                int densityIndexX0Z1 = (noiseX0Offset + noiseZ + 1) * 33;
                int densityIndexX1Z0 = (noiseX1Offset + noiseZ) * 33;
                int densityIndexX1Z1 = (noiseX1Offset + noiseZ + 1) * 33;

                for (int noiseY = 0; noiseY < 32; ++noiseY)
                {
                    double yStep = 0.125D;
                    double densityX0Z0 = this.densityField[densityIndexX0Z0 + noiseY];
                    double densityX0Z1 = this.densityField[densityIndexX0Z1 + noiseY];
                    double densityX1Z0 = this.densityField[densityIndexX1Z0 + noiseY];
                    double densityX1Z1 = this.densityField[densityIndexX1Z1 + noiseY];
                    double yStepX0Z0 = (this.densityField[densityIndexX0Z0 + noiseY + 1] - densityX0Z0) * yStep;
                    double yStepX0Z1 = (this.densityField[densityIndexX0Z1 + noiseY + 1] - densityX0Z1) * yStep;
                    double yStepX1Z0 = (this.densityField[densityIndexX1Z0 + noiseY + 1] - densityX1Z0) * yStep;
                    double yStepX1Z1 = (this.densityField[densityIndexX1Z1 + noiseY + 1] - densityX1Z1) * yStep;

                    for (int subY = 0; subY < 8; ++subY)
                    {
                        double xStep = 0.25D;
                        double densityZ0 = densityX0Z0;
                        double densityZ1 = densityX0Z1;
                        double xStepZ0 = (densityX1Z0 - densityX0Z0) * xStep;
                        double xStepZ1 = (densityX1Z1 - densityX0Z1) * xStep;

                        for (int subX = 0; subX < 4; ++subX)
                        {
                            double zStep = 0.25D;
                            double zDensityStep = (densityZ1 - densityZ0) * zStep;
                            double density = densityZ0 - zDensityStep;

                            for (int subZ = 0; subZ < 4; ++subZ)
                            {
                                int blockX = noiseX * 4 + subX;
                                int blockY = noiseY * 8 + subY;
                                int blockZ = noiseZ * 4 + subZ;

                                if ((density += zDensityStep) > 0.0D)
                                {
                                    primer.setBlockState(blockX, blockY, blockZ, Blocks.stone.getDefaultState());
                                }
                                else if (blockY < this.settings.seaLevel)
                                {
                                    primer.setBlockState(blockX, blockY, blockZ, this.oceanBlockTmpl.getDefaultState());
                                }
                            }

                            densityZ0 += xStepZ0;
                            densityZ1 += xStepZ1;
                        }

                        densityX0Z0 += yStepX0Z0;
                        densityX0Z1 += yStepX0Z1;
                        densityX1Z0 += yStepX1Z0;
                        densityX1Z1 += yStepX1Z1;
                    }
                }
            }
        }
    }

    public void replaceBlocksForBiome(int x, int z, ChunkPrimer primer, BiomeGenBase[] biomeGens)
    {
        double surfaceNoiseScale = 0.03125D;
        this.stoneNoise = this.surfaceNoiseGen.getRegion(this.stoneNoise, (double)(x * 16), (double)(z * 16), 16, 16, surfaceNoiseScale * 2.0D, surfaceNoiseScale * 2.0D, 1.0D);

        for (int localX = 0; localX < 16; ++localX)
        {
            for (int localZ = 0; localZ < 16; ++localZ)
            {
                BiomeGenBase biome = biomeGens[localZ + localX * 16];
                biome.genTerrainBlocks(this.worldObj, this.rand, primer, x * 16 + localX, z * 16 + localZ, this.stoneNoise[localZ + localX * 16]);
            }
        }
    }

    public Chunk provideChunk(int x, int z)
    {
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        this.setBlocksInChunk(x, z, chunkPrimer);
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceBlocksForBiome(x, z, chunkPrimer, this.biomesForGeneration);

        if (this.settings.useCaves)
        {
            this.caveGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        if (this.settings.useRavines)
        {
            this.ravineGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        if (this.settings.useMineShafts && this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled)
        {
            this.villageGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled)
        {
            this.strongholdGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled)
        {
            this.scatteredFeatureGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled)
        {
            this.oceanMonumentGenerator.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        Chunk chunk = new Chunk(this.worldObj, chunkPrimer, x, z);
        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex)
        {
            biomeArray[biomeIndex] = (byte)this.biomesForGeneration[biomeIndex].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private void initializeNoiseField(int x, int y, int z)
    {
        this.depthNoiseArray = this.noiseGen6.generateNoiseOctaves(this.depthNoiseArray, x, z, 5, 5, (double)this.settings.depthNoiseScaleX, (double)this.settings.depthNoiseScaleZ, (double)this.settings.depthNoiseScaleExponent);
        float coordinateScale = this.settings.coordinateScale;
        float heightScale = this.settings.heightScale;
        this.mainNoiseArray = this.mainNoiseGen.generateNoiseOctaves(this.mainNoiseArray, x, y, z, 5, 33, 5, (double)(coordinateScale / this.settings.mainNoiseScaleX), (double)(heightScale / this.settings.mainNoiseScaleY), (double)(coordinateScale / this.settings.mainNoiseScaleZ));
        this.lowerLimitNoiseArray = this.lowerLimitNoiseGen.generateNoiseOctaves(this.lowerLimitNoiseArray, x, y, z, 5, 33, 5, (double)coordinateScale, (double)heightScale, (double)coordinateScale);
        this.upperLimitNoiseArray = this.upperLimitNoiseGen.generateNoiseOctaves(this.upperLimitNoiseArray, x, y, z, 5, 33, 5, (double)coordinateScale, (double)heightScale, (double)coordinateScale);
        int densityIndex = 0;
        int depthNoiseIndex = 0;

        for (int noiseX = 0; noiseX < 5; ++noiseX)
        {
            for (int noiseZ = 0; noiseZ < 5; ++noiseZ)
            {
                float biomeScale = 0.0F;
                float biomeDepth = 0.0F;
                float totalWeight = 0.0F;
                int biomeRadius = 2;
                BiomeGenBase centerBiome = this.biomesForGeneration[noiseX + 2 + (noiseZ + 2) * 10];

                for (int biomeOffsetX = -biomeRadius; biomeOffsetX <= biomeRadius; ++biomeOffsetX)
                {
                    for (int biomeOffsetZ = -biomeRadius; biomeOffsetZ <= biomeRadius; ++biomeOffsetZ)
                    {
                        BiomeGenBase nearbyBiome = this.biomesForGeneration[noiseX + biomeOffsetX + 2 + (noiseZ + biomeOffsetZ + 2) * 10];
                        float nearbyDepth = this.settings.biomeDepthOffSet + nearbyBiome.minHeight * this.settings.biomeDepthWeight;
                        float nearbyScale = this.settings.biomeScaleOffset + nearbyBiome.maxHeight * this.settings.biomeScaleWeight;

                        if (this.worldType == WorldType.AMPLIFIED && nearbyDepth > 0.0F)
                        {
                            nearbyDepth = 1.0F + nearbyDepth * 2.0F;
                            nearbyScale = 1.0F + nearbyScale * 4.0F;
                        }

                        float biomeWeight = this.parabolicField[biomeOffsetX + 2 + (biomeOffsetZ + 2) * 5] / (nearbyDepth + 2.0F);

                        if (nearbyBiome.minHeight > centerBiome.minHeight)
                        {
                            biomeWeight /= 2.0F;
                        }

                        biomeScale += nearbyScale * biomeWeight;
                        biomeDepth += nearbyDepth * biomeWeight;
                        totalWeight += biomeWeight;
                    }
                }

                biomeScale = biomeScale / totalWeight;
                biomeDepth = biomeDepth / totalWeight;
                biomeScale = biomeScale * 0.9F + 0.1F;
                biomeDepth = (biomeDepth * 4.0F - 1.0F) / 8.0F;
                double depthNoise = this.depthNoiseArray[depthNoiseIndex] / 8000.0D;

                if (depthNoise < 0.0D)
                {
                    depthNoise = -depthNoise * 0.3D;
                }

                depthNoise = depthNoise * 3.0D - 2.0D;

                if (depthNoise < 0.0D)
                {
                    depthNoise = depthNoise / 2.0D;

                    if (depthNoise < -1.0D)
                    {
                        depthNoise = -1.0D;
                    }

                    depthNoise = depthNoise / 1.4D;
                    depthNoise = depthNoise / 2.0D;
                }
                else
                {
                    if (depthNoise > 1.0D)
                    {
                        depthNoise = 1.0D;
                    }

                    depthNoise = depthNoise / 8.0D;
                }

                ++depthNoiseIndex;
                double terrainOffset = (double)biomeDepth;
                double terrainScale = (double)biomeScale;
                terrainOffset = terrainOffset + depthNoise * 0.2D;
                terrainOffset = terrainOffset * (double)this.settings.baseSize / 8.0D;
                double baseHeight = (double)this.settings.baseSize + terrainOffset * 4.0D;

                for (int noiseY = 0; noiseY < 33; ++noiseY)
                {
                    double densityOffset = ((double)noiseY - baseHeight) * (double)this.settings.stretchY * 128.0D / 256.0D / terrainScale;

                    if (densityOffset < 0.0D)
                    {
                        densityOffset *= 4.0D;
                    }

                    double lowerDensity = this.lowerLimitNoiseArray[densityIndex] / (double)this.settings.lowerLimitScale;
                    double upperDensity = this.upperLimitNoiseArray[densityIndex] / (double)this.settings.upperLimitScale;
                    double densityBlend = (this.mainNoiseArray[densityIndex] / 10.0D + 1.0D) / 2.0D;
                    double density = MathHelper.denormalizeClamp(lowerDensity, upperDensity, densityBlend) - densityOffset;

                    if (noiseY > 29)
                    {
                        double topFade = (double)((float)(noiseY - 29) / 3.0F);
                        density = density * (1.0D - topFade) + -10.0D * topFade;
                    }

                    this.densityField[densityIndex] = density;
                    ++densityIndex;
                }
            }
        }
    }

    public boolean chunkExists(int x, int z)
    {
        return true;
    }

    public void populate(IChunkProvider chunkProvider, int x, int z)
    {
        BlockFalling.fallInstantly = true;
        int chunkBlockX = x * 16;
        int chunkBlockZ = z * 16;
        BlockPos chunkOrigin = new BlockPos(chunkBlockX, 0, chunkBlockZ);
        BiomeGenBase biome = this.worldObj.getBiomeGenForCoords(chunkOrigin.add(16, 0, 16));
        this.rand.setSeed(this.worldObj.getSeed());
        long xSeedFactor = this.rand.nextLong() / 2L * 2L + 1L;
        long zSeedFactor = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)x * xSeedFactor + (long)z * zSeedFactor ^ this.worldObj.getSeed());
        boolean villageGenerated = false;
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(x, z);

        if (this.settings.useMineShafts && this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator.generateStructure(this.worldObj, this.rand, chunkCoords);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled)
        {
            villageGenerated = this.villageGenerator.generateStructure(this.worldObj, this.rand, chunkCoords);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled)
        {
            this.strongholdGenerator.generateStructure(this.worldObj, this.rand, chunkCoords);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled)
        {
            this.scatteredFeatureGenerator.generateStructure(this.worldObj, this.rand, chunkCoords);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled)
        {
            this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, chunkCoords);
        }

        if (biome != BiomeGenBase.desert && biome != BiomeGenBase.desertHills && this.settings.useWaterLakes && !villageGenerated && this.rand.nextInt(this.settings.waterLakeChance) == 0)
        {
            int waterLakeX = this.rand.nextInt(16) + 8;
            int waterLakeY = this.rand.nextInt(256);
            int waterLakeZ = this.rand.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.rand, chunkOrigin.add(waterLakeX, waterLakeY, waterLakeZ));
        }

        if (!villageGenerated && this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes)
        {
            int lavaLakeX = this.rand.nextInt(16) + 8;
            int lavaLakeY = this.rand.nextInt(this.rand.nextInt(248) + 8);
            int lavaLakeZ = this.rand.nextInt(16) + 8;

            if (lavaLakeY < this.worldObj.getSeaLevel() || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0)
            {
                (new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.rand, chunkOrigin.add(lavaLakeX, lavaLakeY, lavaLakeZ));
            }
        }

        if (this.settings.useDungeons)
        {
            for (int dungeonAttempt = 0; dungeonAttempt < this.settings.dungeonChance; ++dungeonAttempt)
            {
                int dungeonX = this.rand.nextInt(16) + 8;
                int dungeonY = this.rand.nextInt(256);
                int dungeonZ = this.rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.worldObj, this.rand, chunkOrigin.add(dungeonX, dungeonY, dungeonZ));
            }
        }

        biome.decorate(this.worldObj, this.rand, new BlockPos(chunkBlockX, 0, chunkBlockZ));
        SpawnerAnimals.performWorldGenSpawning(this.worldObj, biome, chunkBlockX + 8, chunkBlockZ + 8, 16, 16, this.rand);
        BlockPos chunkCenter = chunkOrigin.add(8, 0, 8);

        for (int localX = 0; localX < 16; ++localX)
        {
            for (int localZ = 0; localZ < 16; ++localZ)
            {
                BlockPos precipitationPos = this.worldObj.getPrecipitationHeight(chunkCenter.add(localX, 0, localZ));
                BlockPos freezePos = precipitationPos.down();

                if (this.worldObj.canBlockFreezeWater(freezePos))
                {
                    this.worldObj.setBlockState(freezePos, Blocks.ice.getDefaultState(), 2);
                }

                if (this.worldObj.canSnowAt(precipitationPos, true))
                {
                    this.worldObj.setBlockState(precipitationPos, Blocks.snow_layer.getDefaultState(), 2);
                }
            }
        }

        BlockFalling.fallInstantly = false;
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z)
    {
        boolean generatedStructure = false;

        if (this.settings.useMonuments && this.mapFeaturesEnabled && chunkIn.getInhabitedTime() < 3600L)
        {
            generatedStructure |= this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, new ChunkCoordIntPair(x, z));
        }

        return generatedStructure;
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
        return "RandomLevelSource";
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        BiomeGenBase biome = this.worldObj.getBiomeGenForCoords(pos);

        if (this.mapFeaturesEnabled)
        {
            if (creatureType == EnumCreatureType.MONSTER && this.scatteredFeatureGenerator.isSwampHutAt(pos))
            {
                return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
            }

            if (creatureType == EnumCreatureType.MONSTER && this.settings.useMonuments && this.oceanMonumentGenerator.isPositionInStructure(this.worldObj, pos))
            {
                return this.oceanMonumentGenerator.getScatteredFeatureSpawnList();
            }
        }

        return biome.getSpawnableList(creatureType);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return "Stronghold".equals(structureName) && this.strongholdGenerator != null ? this.strongholdGenerator.getClosestStrongholdPos(worldIn, position) : null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        if (this.settings.useMineShafts && this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled)
        {
            this.villageGenerator.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled)
        {
            this.strongholdGenerator.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled)
        {
            this.scatteredFeatureGenerator.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled)
        {
            this.oceanMonumentGenerator.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
        }
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
