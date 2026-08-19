package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenNetherBridge;

public class ChunkProviderHell implements IChunkProvider
{
    private final World worldObj;
    private final boolean generateStructures;
    private final Random hellRNG;
    private double[] slowsandNoise = new double[256];
    private double[] gravelNoise = new double[256];
    private double[] netherrackExclusivityNoise = new double[256];
    private double[] noiseField;
    private final NoiseGeneratorOctaves netherNoiseGen1;
    private final NoiseGeneratorOctaves netherNoiseGen2;
    private final NoiseGeneratorOctaves netherNoiseGen3;
    private final NoiseGeneratorOctaves slowsandGravelNoiseGen;
    private final NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
    public final NoiseGeneratorOctaves netherNoiseGen6;
    public final NoiseGeneratorOctaves netherNoiseGen7;
    private final WorldGenFire fireGenerator = new WorldGenFire();
    private final WorldGenGlowStone1 glowStoneGenerator1 = new WorldGenGlowStone1();
    private final WorldGenGlowStone2 glowStoneGenerator2 = new WorldGenGlowStone2();
    private final WorldGenerator quartzOreGenerator = new WorldGenMinable(Blocks.quartz_ore.getDefaultState(), 14, BlockHelper.forBlock(Blocks.netherrack));
    private final WorldGenHellLava lavaSpringGenerator1 = new WorldGenHellLava(Blocks.flowing_lava, true);
    private final WorldGenHellLava lavaSpringGenerator2 = new WorldGenHellLava(Blocks.flowing_lava, false);
    private final GeneratorBushFeature brownMushroomGenerator = new GeneratorBushFeature(Blocks.brown_mushroom);
    private final GeneratorBushFeature redMushroomGenerator = new GeneratorBushFeature(Blocks.red_mushroom);
    private final MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();
    private final MapGenBase netherCaveGenerator = new MapGenCavesHell();
    double[] noiseData1;
    double[] noiseData2;
    double[] noiseData3;
    double[] noiseData4;
    double[] noiseData5;

    public ChunkProviderHell(World worldIn, boolean generateStructures, long seed)
    {
        this.worldObj = worldIn;
        this.generateStructures = generateStructures;
        this.hellRNG = new Random(seed);
        this.netherNoiseGen1 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        this.netherNoiseGen2 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        this.netherNoiseGen3 = new NoiseGeneratorOctaves(this.hellRNG, 8);
        this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(this.hellRNG, 4);
        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(this.hellRNG, 4);
        this.netherNoiseGen6 = new NoiseGeneratorOctaves(this.hellRNG, 10);
        this.netherNoiseGen7 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        worldIn.setSeaLevel(63);
    }

    public void buildBaseTerrain(int chunkX, int chunkZ, ChunkPrimer primer)
    {
        int noiseCellCount = 4;
        int lavaSeaLevel = this.worldObj.getSeaLevel() / 2 + 1;
        int noiseSizeX = noiseCellCount + 1;
        int noiseSizeY = 17;
        int noiseSizeZ = noiseCellCount + 1;
        this.noiseField = this.initializeNoiseField(this.noiseField, chunkX * noiseCellCount, 0, chunkZ * noiseCellCount, noiseSizeX, noiseSizeY, noiseSizeZ);

        for (int noiseX = 0; noiseX < noiseCellCount; ++noiseX)
        {
            for (int noiseZ = 0; noiseZ < noiseCellCount; ++noiseZ)
            {
                for (int noiseY = 0; noiseY < 16; ++noiseY)
                {
                    double yStep = 0.125D;
                    double densityX0Z0 = this.noiseField[((noiseX + 0) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 0];
                    double densityX0Z1 = this.noiseField[((noiseX + 0) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 0];
                    double densityX1Z0 = this.noiseField[((noiseX + 1) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 0];
                    double densityX1Z1 = this.noiseField[((noiseX + 1) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 0];
                    double yStepX0Z0 = (this.noiseField[((noiseX + 0) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 1] - densityX0Z0) * yStep;
                    double yStepX0Z1 = (this.noiseField[((noiseX + 0) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 1] - densityX0Z1) * yStep;
                    double yStepX1Z0 = (this.noiseField[((noiseX + 1) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 1] - densityX1Z0) * yStep;
                    double yStepX1Z1 = (this.noiseField[((noiseX + 1) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 1] - densityX1Z1) * yStep;

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
                            double density = densityZ0;
                            double zDensityStep = (densityZ1 - densityZ0) * zStep;

                            for (int subZ = 0; subZ < 4; ++subZ)
                            {
                                IBlockState blockState = null;

                                if (noiseY * 8 + subY < lavaSeaLevel)
                                {
                                    blockState = Blocks.lava.getDefaultState();
                                }

                                if (density > 0.0D)
                                {
                                    blockState = Blocks.netherrack.getDefaultState();
                                }

                                int blockX = subX + noiseX * 4;
                                int blockY = subY + noiseY * 8;
                                int blockZ = subZ + noiseZ * 4;
                                primer.setBlockState(blockX, blockY, blockZ, blockState);
                                density += zDensityStep;
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

    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer)
    {
        int seaLevel = this.worldObj.getSeaLevel() + 1;
        double surfaceNoiseScale = 0.03125D;
        this.slowsandNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(this.slowsandNoise, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, surfaceNoiseScale, surfaceNoiseScale, 1.0D);
        this.gravelNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(this.gravelNoise, chunkX * 16, 109, chunkZ * 16, 16, 1, 16, surfaceNoiseScale, 1.0D, surfaceNoiseScale);
        this.netherrackExclusivityNoise = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(this.netherrackExclusivityNoise, chunkX * 16, chunkZ * 16, 0, 16, 16, 1, surfaceNoiseScale * 2.0D, surfaceNoiseScale * 2.0D, surfaceNoiseScale * 2.0D);

        for (int localZ = 0; localZ < 16; ++localZ)
        {
            for (int localX = 0; localX < 16; ++localX)
            {
                boolean generateSoulSand = this.slowsandNoise[localZ + localX * 16] + this.hellRNG.nextDouble() * 0.2D > 0.0D;
                boolean generateGravel = this.gravelNoise[localZ + localX * 16] + this.hellRNG.nextDouble() * 0.2D > 0.0D;
                int surfaceDepth = (int)(this.netherrackExclusivityNoise[localZ + localX * 16] / 3.0D + 3.0D + this.hellRNG.nextDouble() * 0.25D);
                int remainingDepth = -1;
                IBlockState topState = Blocks.netherrack.getDefaultState();
                IBlockState fillerState = Blocks.netherrack.getDefaultState();

                for (int y = 127; y >= 0; --y)
                {
                    if (y < 127 - this.hellRNG.nextInt(5) && y > this.hellRNG.nextInt(5))
                    {
                        IBlockState currentState = primer.getBlockState(localX, y, localZ);

                        if (currentState.getBlock() != null && currentState.getBlock().getMaterial() != Material.air)
                        {
                            if (currentState.getBlock() == Blocks.netherrack)
                            {
                                if (remainingDepth == -1)
                                {
                                    if (surfaceDepth <= 0)
                                    {
                                        topState = null;
                                        fillerState = Blocks.netherrack.getDefaultState();
                                    }
                                    else if (y >= seaLevel - 4 && y <= seaLevel + 1)
                                    {
                                        topState = Blocks.netherrack.getDefaultState();
                                        fillerState = Blocks.netherrack.getDefaultState();

                                        if (generateGravel)
                                        {
                                            topState = Blocks.gravel.getDefaultState();
                                            fillerState = Blocks.netherrack.getDefaultState();
                                        }

                                        if (generateSoulSand)
                                        {
                                            topState = Blocks.soul_sand.getDefaultState();
                                            fillerState = Blocks.soul_sand.getDefaultState();
                                        }
                                    }

                                    if (y < seaLevel && (topState == null || topState.getBlock().getMaterial() == Material.air))
                                    {
                                        topState = Blocks.lava.getDefaultState();
                                    }

                                    remainingDepth = surfaceDepth;

                                    if (y >= seaLevel - 1)
                                    {
                                        primer.setBlockState(localX, y, localZ, topState);
                                    }
                                    else
                                    {
                                        primer.setBlockState(localX, y, localZ, fillerState);
                                    }
                                }
                                else if (remainingDepth > 0)
                                {
                                    --remainingDepth;
                                    primer.setBlockState(localX, y, localZ, fillerState);
                                }
                            }
                        }
                        else
                        {
                            remainingDepth = -1;
                        }
                    }
                    else
                    {
                        primer.setBlockState(localX, y, localZ, Blocks.bedrock.getDefaultState());
                    }
                }
            }
        }
    }

    public Chunk provideChunk(int x, int z)
    {
        this.hellRNG.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        this.buildBaseTerrain(x, z, chunkPrimer);
        this.replaceBlocksForBiome(x, z, chunkPrimer);
        this.netherCaveGenerator.generate(this, this.worldObj, x, z, chunkPrimer);

        if (this.generateStructures)
        {
            this.genNetherBridge.generate(this, this.worldObj, x, z, chunkPrimer);
        }

        Chunk chunk = new Chunk(this.worldObj, chunkPrimer, x, z);
        BiomeGenBase[] biomes = this.worldObj.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, x * 16, z * 16, 16, 16);
        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex)
        {
            biomeArray[biomeIndex] = (byte)biomes[biomeIndex].biomeID;
        }

        chunk.resetRelightChecks();
        return chunk;
    }

    private double[] initializeNoiseField(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize)
    {
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }

        double horizontalScale = 684.412D;
        double verticalScale = 2053.236D;
        this.noiseData4 = this.netherNoiseGen6.generateNoiseOctaves(this.noiseData4, xOffset, yOffset, zOffset, xSize, 1, zSize, 1.0D, 0.0D, 1.0D);
        this.noiseData5 = this.netherNoiseGen7.generateNoiseOctaves(this.noiseData5, xOffset, yOffset, zOffset, xSize, 1, zSize, 100.0D, 0.0D, 100.0D);
        this.noiseData1 = this.netherNoiseGen3.generateNoiseOctaves(this.noiseData1, xOffset, yOffset, zOffset, xSize, ySize, zSize, horizontalScale / 80.0D, verticalScale / 60.0D, horizontalScale / 80.0D);
        this.noiseData2 = this.netherNoiseGen1.generateNoiseOctaves(this.noiseData2, xOffset, yOffset, zOffset, xSize, ySize, zSize, horizontalScale, verticalScale, horizontalScale);
        this.noiseData3 = this.netherNoiseGen2.generateNoiseOctaves(this.noiseData3, xOffset, yOffset, zOffset, xSize, ySize, zSize, horizontalScale, verticalScale, horizontalScale);
        int noiseIndex = 0;
        double[] heightCurve = new double[ySize];

        for (int y = 0; y < ySize; ++y)
        {
            heightCurve[y] = Math.cos((double)y * Math.PI * 6.0D / (double)ySize) * 2.0D;
            double distanceFromCeilingOrFloor = (double)y;

            if (y > ySize / 2)
            {
                distanceFromCeilingOrFloor = (double)(ySize - 1 - y);
            }

            if (distanceFromCeilingOrFloor < 4.0D)
            {
                distanceFromCeilingOrFloor = 4.0D - distanceFromCeilingOrFloor;
                heightCurve[y] -= distanceFromCeilingOrFloor * distanceFromCeilingOrFloor * distanceFromCeilingOrFloor * 10.0D;
            }
        }

        for (int x = 0; x < xSize; ++x)
        {
            for (int z = 0; z < zSize; ++z)
            {
                double lowerFadeStart = 0.0D;

                for (int y = 0; y < ySize; ++y)
                {
                    double density = 0.0D;
                    double heightBias = heightCurve[y];
                    double densityLow = this.noiseData2[noiseIndex] / 512.0D;
                    double densityHigh = this.noiseData3[noiseIndex] / 512.0D;
                    double densityBlend = (this.noiseData1[noiseIndex] / 10.0D + 1.0D) / 2.0D;

                    if (densityBlend < 0.0D)
                    {
                        density = densityLow;
                    }
                    else if (densityBlend > 1.0D)
                    {
                        density = densityHigh;
                    }
                    else
                    {
                        density = densityLow + (densityHigh - densityLow) * densityBlend;
                    }

                    density = density - heightBias;

                    if (y > ySize - 4)
                    {
                        double topFade = (double)((float)(y - (ySize - 4)) / 3.0F);
                        density = density * (1.0D - topFade) + -10.0D * topFade;
                    }

                    if ((double)y < lowerFadeStart)
                    {
                        double bottomFade = (lowerFadeStart - (double)y) / 4.0D;
                        bottomFade = MathHelper.clamp_double(bottomFade, 0.0D, 1.0D);
                        density = density * (1.0D - bottomFade) + -10.0D * bottomFade;
                    }

                    noiseArray[noiseIndex] = density;
                    ++noiseIndex;
                }
            }
        }

        return noiseArray;
    }

    public boolean chunkExists(int x, int z)
    {
        return true;
    }

    public void populate(IChunkProvider chunkProvider, int x, int z)
    {
        BlockFalling.fallInstantly = true;
        BlockPos chunkOrigin = new BlockPos(x * 16, 0, z * 16);
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(x, z);
        this.genNetherBridge.generateStructure(this.worldObj, this.hellRNG, chunkCoordIntPair);

        for (int lavaFallAttempt = 0; lavaFallAttempt < 8; ++lavaFallAttempt)
        {
            this.lavaSpringGenerator2.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16) + 8, this.hellRNG.nextInt(120) + 4, this.hellRNG.nextInt(16) + 8));
        }

        for (int fireAttempt = 0; fireAttempt < this.hellRNG.nextInt(this.hellRNG.nextInt(10) + 1) + 1; ++fireAttempt)
        {
            this.fireGenerator.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16) + 8, this.hellRNG.nextInt(120) + 4, this.hellRNG.nextInt(16) + 8));
        }

        for (int glowstoneClusterAttempt = 0; glowstoneClusterAttempt < this.hellRNG.nextInt(this.hellRNG.nextInt(10) + 1); ++glowstoneClusterAttempt)
        {
            this.glowStoneGenerator1.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16) + 8, this.hellRNG.nextInt(120) + 4, this.hellRNG.nextInt(16) + 8));
        }

        for (int glowstoneAttempt = 0; glowstoneAttempt < 10; ++glowstoneAttempt)
        {
            this.glowStoneGenerator2.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16) + 8, this.hellRNG.nextInt(128), this.hellRNG.nextInt(16) + 8));
        }

        if (this.hellRNG.nextBoolean())
        {
            this.brownMushroomGenerator.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16) + 8, this.hellRNG.nextInt(128), this.hellRNG.nextInt(16) + 8));
        }

        if (this.hellRNG.nextBoolean())
        {
            this.redMushroomGenerator.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16) + 8, this.hellRNG.nextInt(128), this.hellRNG.nextInt(16) + 8));
        }

        for (int quartzAttempt = 0; quartzAttempt < 16; ++quartzAttempt)
        {
            this.quartzOreGenerator.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16), this.hellRNG.nextInt(108) + 10, this.hellRNG.nextInt(16)));
        }

        for (int lavaSpringAttempt = 0; lavaSpringAttempt < 16; ++lavaSpringAttempt)
        {
            this.lavaSpringGenerator1.generate(this.worldObj, this.hellRNG, chunkOrigin.add(this.hellRNG.nextInt(16), this.hellRNG.nextInt(108) + 10, this.hellRNG.nextInt(16)));
        }

        BlockFalling.fallInstantly = false;
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
        return "HellRandomLevelSource";
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        if (creatureType == EnumCreatureType.MONSTER)
        {
            if (this.genNetherBridge.isPositionInAnyStructure(pos))
            {
                return this.genNetherBridge.getSpawnList();
            }

            if (this.genNetherBridge.isPositionInStructure(this.worldObj, pos) && this.worldObj.getBlockState(pos.down()).getBlock() == Blocks.nether_brick)
            {
                return this.genNetherBridge.getSpawnList();
            }
        }

        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(pos);
        return biomegenbase.getSpawnableList(creatureType);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        this.genNetherBridge.generate(this, this.worldObj, x, z, (ChunkPrimer)null);
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
