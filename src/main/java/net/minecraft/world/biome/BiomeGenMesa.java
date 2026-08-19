package net.minecraft.world.biome;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeGenMesa extends BiomeGenBase
{
    private IBlockState[] clayBands;
    private long clayBandsSeed;
    private NoiseGeneratorPerlin pillarNoise;
    private NoiseGeneratorPerlin pillarRoofNoise;
    private NoiseGeneratorPerlin clayBandOffsetNoise;
    private boolean hasBrycePillars;
    private boolean hasForest;

    public BiomeGenMesa(int id, boolean hasBrycePillarsIn, boolean hasForestIn)
    {
        super(id);
        this.hasBrycePillars = hasBrycePillarsIn;
        this.hasForest = hasForestIn;
        this.setDisableRain();
        this.setTemperatureRainfall(2.0F, 0.0F);
        this.spawnableCreatureList.clear();
        this.topBlock = Blocks.sand.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
        this.fillerBlock = Blocks.stained_hardened_clay.getDefaultState();
        this.theBiomeDecorator.treesPerChunk = -999;
        this.theBiomeDecorator.deadBushPerChunk = 20;
        this.theBiomeDecorator.reedsPerChunk = 3;
        this.theBiomeDecorator.cactiPerChunk = 5;
        this.theBiomeDecorator.flowersPerChunk = 0;
        this.spawnableCreatureList.clear();

        if (hasForestIn)
        {
            this.theBiomeDecorator.treesPerChunk = 5;
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return this.worldGeneratorTrees;
    }

    public int getFoliageColorAtPos(BlockPos pos)
    {
        return 10387789;
    }

    public int getGrassColorAtPos(BlockPos pos)
    {
        return 9470285;
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        super.decorate(worldIn, rand, pos);
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal)
    {
        if (this.clayBands == null || this.clayBandsSeed != worldIn.getSeed())
        {
            this.initializeClayBands(worldIn.getSeed());
        }

        if (this.pillarNoise == null || this.pillarRoofNoise == null || this.clayBandsSeed != worldIn.getSeed())
        {
            Random random = new Random(this.clayBandsSeed);
            this.pillarNoise = new NoiseGeneratorPerlin(random, 4);
            this.pillarRoofNoise = new NoiseGeneratorPerlin(random, 1);
        }

        this.clayBandsSeed = worldIn.getSeed();
        double brycePillarHeight = 0.0D;

        if (this.hasBrycePillars)
        {
            int pillarNoiseX = (x & -16) + (z & 15);
            int pillarNoiseZ = (z & -16) + (x & 15);
            double pillarNoiseValue = Math.min(Math.abs(noiseVal), this.pillarNoise.getValue((double)pillarNoiseX * 0.25D, (double)pillarNoiseZ * 0.25D));

            if (pillarNoiseValue > 0.0D)
            {
                double roofNoiseScale = 0.001953125D;
                double roofNoiseValue = Math.abs(this.pillarRoofNoise.getValue((double)pillarNoiseX * roofNoiseScale, (double)pillarNoiseZ * roofNoiseScale));
                brycePillarHeight = pillarNoiseValue * pillarNoiseValue * 2.5D;
                double maxPillarHeight = Math.ceil(roofNoiseValue * 50.0D) + 14.0D;

                if (brycePillarHeight > maxPillarHeight)
                {
                    brycePillarHeight = maxPillarHeight;
                }

                brycePillarHeight = brycePillarHeight + 64.0D;
            }
        }

        int localX = x & 15;
        int localZ = z & 15;
        int seaLevel = worldIn.getSeaLevel();
        IBlockState topState = Blocks.stained_hardened_clay.getDefaultState();
        IBlockState fillerState = this.fillerBlock;
        int surfaceDepth = (int)(noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        boolean usePlainClay = Math.cos(noiseVal / 3.0D * Math.PI) > 0.0D;
        int remainingFillerDepth = -1;
        boolean usedRedSandTop = false;

        for (int index = 255; index >= 0; --index)
        {
            if (chunkPrimerIn.getBlockState(localZ, index, localX).getBlock().getMaterial() == Material.air && index < (int)brycePillarHeight)
            {
                chunkPrimerIn.setBlockState(localZ, index, localX, Blocks.stone.getDefaultState());
            }

            if (index <= rand.nextInt(5))
            {
                chunkPrimerIn.setBlockState(localZ, index, localX, Blocks.bedrock.getDefaultState());
            }
            else
            {
                IBlockState currentState = chunkPrimerIn.getBlockState(localZ, index, localX);

                if (currentState.getBlock().getMaterial() == Material.air)
                {
                    remainingFillerDepth = -1;
                }
                else if (currentState.getBlock() == Blocks.stone)
                {
                    if (remainingFillerDepth == -1)
                    {
                        usedRedSandTop = false;

                        if (surfaceDepth <= 0)
                        {
                            topState = null;
                            fillerState = Blocks.stone.getDefaultState();
                        }
                        else if (index >= seaLevel - 4 && index <= seaLevel + 1)
                        {
                            topState = Blocks.stained_hardened_clay.getDefaultState();
                            fillerState = this.fillerBlock;
                        }

                        if (index < seaLevel && (topState == null || topState.getBlock().getMaterial() == Material.air))
                        {
                            topState = Blocks.water.getDefaultState();
                        }

                        remainingFillerDepth = surfaceDepth + Math.max(0, index - seaLevel);

                        if (index < seaLevel - 1)
                        {
                            chunkPrimerIn.setBlockState(localZ, index, localX, fillerState);

                            if (fillerState.getBlock() == Blocks.stained_hardened_clay)
                            {
                                chunkPrimerIn.setBlockState(localZ, index, localX, fillerState.getBlock().getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE));
                            }
                        }
                        else if (this.hasForest && index > 86 + surfaceDepth * 2)
                        {
                            if (usePlainClay)
                            {
                                chunkPrimerIn.setBlockState(localZ, index, localX, Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT));
                            }
                            else
                            {
                                chunkPrimerIn.setBlockState(localZ, index, localX, Blocks.grass.getDefaultState());
                            }
                        }
                        else if (index <= seaLevel + 3 + surfaceDepth)
                        {
                            chunkPrimerIn.setBlockState(localZ, index, localX, this.topBlock);
                            usedRedSandTop = true;
                        }
                        else
                        {
                            IBlockState clayBandState;

                            if (index >= 64 && index <= 127)
                            {
                                if (usePlainClay)
                                {
                                    clayBandState = Blocks.hardened_clay.getDefaultState();
                                }
                                else
                                {
                                    clayBandState = this.getClayBand(x, index, z);
                                }
                            }
                            else
                            {
                                clayBandState = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
                            }

                            chunkPrimerIn.setBlockState(localZ, index, localX, clayBandState);
                        }
                    }
                    else if (remainingFillerDepth > 0)
                    {
                        --remainingFillerDepth;

                        if (usedRedSandTop)
                        {
                            chunkPrimerIn.setBlockState(localZ, index, localX, Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE));
                        }
                        else
                        {
                            IBlockState clayBandState = this.getClayBand(x, index, z);
                            chunkPrimerIn.setBlockState(localZ, index, localX, clayBandState);
                        }
                    }
                }
            }
        }
    }

    private void initializeClayBands(long seed)
    {
        this.clayBands = new IBlockState[64];
        Arrays.fill(this.clayBands, Blocks.hardened_clay.getDefaultState());
        Random random = new Random(seed);
        this.clayBandOffsetNoise = new NoiseGeneratorPerlin(random, 1);

        for (int orangeBandIndex = 0; orangeBandIndex < 64; ++orangeBandIndex)
        {
            orangeBandIndex += random.nextInt(5) + 1;

            if (orangeBandIndex < 64)
            {
                this.clayBands[orangeBandIndex] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
            }
        }

        int yellowBandCount = random.nextInt(4) + 2;

        for (int bandIndex = 0; bandIndex < yellowBandCount; ++bandIndex)
        {
            int bandLength = random.nextInt(3) + 1;
            int bandStart = random.nextInt(64);

            for (int bandOffset = 0; bandStart + bandOffset < 64 && bandOffset < bandLength; ++bandOffset)
            {
                this.clayBands[bandStart + bandOffset] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW);
            }
        }

        int brownBandCount = random.nextInt(4) + 2;

        for (int bandIndex = 0; bandIndex < brownBandCount; ++bandIndex)
        {
            int bandLength = random.nextInt(3) + 2;
            int bandStart = random.nextInt(64);

            for (int bandOffset = 0; bandStart + bandOffset < 64 && bandOffset < bandLength; ++bandOffset)
            {
                this.clayBands[bandStart + bandOffset] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BROWN);
            }
        }

        int redBandCount = random.nextInt(4) + 2;

        for (int bandIndex = 0; bandIndex < redBandCount; ++bandIndex)
        {
            int bandLength = random.nextInt(3) + 1;
            int bandStart = random.nextInt(64);

            for (int bandOffset = 0; bandStart + bandOffset < 64 && bandOffset < bandLength; ++bandOffset)
            {
                this.clayBands[bandStart + bandOffset] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED);
            }
        }

        int whiteBandCount = random.nextInt(3) + 3;
        int whiteBandStart = 0;

        for (int bandIndex = 0; bandIndex < whiteBandCount; ++bandIndex)
        {
            int bandLength = 1;
            whiteBandStart += random.nextInt(16) + 4;

            for (int bandOffset = 0; whiteBandStart + bandOffset < 64 && bandOffset < bandLength; ++bandOffset)
            {
                this.clayBands[whiteBandStart + bandOffset] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);

                if (whiteBandStart + bandOffset > 1 && random.nextBoolean())
                {
                    this.clayBands[whiteBandStart + bandOffset - 1] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
                }

                if (whiteBandStart + bandOffset < 63 && random.nextBoolean())
                {
                    this.clayBands[whiteBandStart + bandOffset + 1] = Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
                }
            }
        }
    }

    private IBlockState getClayBand(int x, int y, int z)
    {
        int bandOffset = (int)Math.round(this.clayBandOffsetNoise.getValue((double)x * 1.0D / 512.0D, (double)x * 1.0D / 512.0D) * 2.0D);
        return this.clayBands[(y + bandOffset + 64) % 64];
    }

    protected BiomeGenBase createMutatedBiome(int newBiomeId)
    {
        boolean isBaseMesa = this.biomeID == BiomeGenBase.mesa.biomeID;
        BiomeGenMesa biomeGenMesa = new BiomeGenMesa(newBiomeId, isBaseMesa, this.hasForest);

        if (!isBaseMesa)
        {
            biomeGenMesa.setHeight(height_LowHills);
            biomeGenMesa.setBiomeName(this.biomeName + " M");
        }
        else
        {
            biomeGenMesa.setBiomeName(this.biomeName + " (Bryce)");
        }

        biomeGenMesa.setColor(this.color, true);
        return biomeGenMesa;
    }
}
