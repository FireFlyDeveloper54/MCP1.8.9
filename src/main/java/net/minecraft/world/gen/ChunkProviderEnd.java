package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkProviderEnd implements IChunkProvider
{
    private Random random;
    private NoiseGeneratorOctaves minNoiseGen;
    private NoiseGeneratorOctaves maxNoiseGen;
    private NoiseGeneratorOctaves detailNoiseGen;
    public NoiseGeneratorOctaves surfaceNoiseGen;
    public NoiseGeneratorOctaves islandNoiseGen;
    private World world;
    private double[] densities;
    private BiomeGenBase[] biomesForGeneration;
    double[] noiseData1;
    double[] noiseData2;
    double[] noiseData3;
    double[] noiseData4;
    double[] noiseData5;

    public ChunkProviderEnd(World worldIn, long seed)
    {
        this.world = worldIn;
        this.random = new Random(seed);
        this.minNoiseGen = new NoiseGeneratorOctaves(this.random, 16);
        this.maxNoiseGen = new NoiseGeneratorOctaves(this.random, 16);
        this.detailNoiseGen = new NoiseGeneratorOctaves(this.random, 8);
        this.surfaceNoiseGen = new NoiseGeneratorOctaves(this.random, 10);
        this.islandNoiseGen = new NoiseGeneratorOctaves(this.random, 16);
    }

    public void buildBaseTerrain(int chunkX, int chunkZ, ChunkPrimer primer)
    {
        int noiseCellCount = 2;
        int noiseSizeX = noiseCellCount + 1;
        int noiseSizeY = 33;
        int noiseSizeZ = noiseCellCount + 1;
        this.densities = this.initializeNoiseField(this.densities, chunkX * noiseCellCount, 0, chunkZ * noiseCellCount, noiseSizeX, noiseSizeY, noiseSizeZ);

        for (int noiseX = 0; noiseX < noiseCellCount; ++noiseX)
        {
            for (int noiseZ = 0; noiseZ < noiseCellCount; ++noiseZ)
            {
                for (int noiseY = 0; noiseY < 32; ++noiseY)
                {
                    double yStep = 0.25D;
                    double densityX0Z0 = this.densities[((noiseX + 0) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 0];
                    double densityX0Z1 = this.densities[((noiseX + 0) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 0];
                    double densityX1Z0 = this.densities[((noiseX + 1) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 0];
                    double densityX1Z1 = this.densities[((noiseX + 1) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 0];
                    double yStepX0Z0 = (this.densities[((noiseX + 0) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 1] - densityX0Z0) * yStep;
                    double yStepX0Z1 = (this.densities[((noiseX + 0) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 1] - densityX0Z1) * yStep;
                    double yStepX1Z0 = (this.densities[((noiseX + 1) * noiseSizeZ + noiseZ + 0) * noiseSizeY + noiseY + 1] - densityX1Z0) * yStep;
                    double yStepX1Z1 = (this.densities[((noiseX + 1) * noiseSizeZ + noiseZ + 1) * noiseSizeY + noiseY + 1] - densityX1Z1) * yStep;

                    for (int subY = 0; subY < 4; ++subY)
                    {
                        double xStep = 0.125D;
                        double densityZ0 = densityX0Z0;
                        double densityZ1 = densityX0Z1;
                        double xStepZ0 = (densityX1Z0 - densityX0Z0) * xStep;
                        double xStepZ1 = (densityX1Z1 - densityX0Z1) * xStep;

                        for (int subX = 0; subX < 8; ++subX)
                        {
                            double zStep = 0.125D;
                            double density = densityZ0;
                            double zDensityStep = (densityZ1 - densityZ0) * zStep;

                            for (int subZ = 0; subZ < 8; ++subZ)
                            {
                                IBlockState blockState = null;

                                if (density > 0.0D)
                                {
                                    blockState = Blocks.end_stone.getDefaultState();
                                }

                                int blockX = subX + noiseX * 8;
                                int blockY = subY + noiseY * 4;
                                int blockZ = subZ + noiseZ * 8;
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

    public void replaceBlocks(ChunkPrimer primer)
    {
        for (int localX = 0; localX < 16; ++localX)
        {
            for (int localZ = 0; localZ < 16; ++localZ)
            {
                int surfaceDepth = 1;
                int remainingDepth = -1;
                IBlockState topState = Blocks.end_stone.getDefaultState();
                IBlockState fillerState = Blocks.end_stone.getDefaultState();

                for (int y = 127; y >= 0; --y)
                {
                    IBlockState currentState = primer.getBlockState(localX, y, localZ);

                    if (currentState.getBlock().getMaterial() == Material.air)
                    {
                        remainingDepth = -1;
                    }
                    else if (currentState.getBlock() == Blocks.stone)
                    {
                        if (remainingDepth == -1)
                        {
                            if (surfaceDepth <= 0)
                            {
                                topState = Blocks.air.getDefaultState();
                                fillerState = Blocks.end_stone.getDefaultState();
                            }

                            remainingDepth = surfaceDepth;

                            if (y >= 0)
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
            }
        }
    }

    public Chunk provideChunk(int x, int z)
    {
        this.random.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        this.biomesForGeneration = this.world.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.buildBaseTerrain(x, z, chunkPrimer);
        this.replaceBlocks(chunkPrimer);
        Chunk chunk = new Chunk(this.world, chunkPrimer, x, z);
        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex)
        {
            biomeArray[biomeIndex] = (byte)this.biomesForGeneration[biomeIndex].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private double[] initializeNoiseField(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize)
    {
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }

        double horizontalScale = 684.412D;
        double verticalScale = 684.412D;
        this.noiseData4 = this.surfaceNoiseGen.generateNoiseOctaves(this.noiseData4, xOffset, zOffset, xSize, zSize, 1.121D, 1.121D, 0.5D);
        this.noiseData5 = this.islandNoiseGen.generateNoiseOctaves(this.noiseData5, xOffset, zOffset, xSize, zSize, 200.0D, 200.0D, 0.5D);
        horizontalScale = horizontalScale * 2.0D;
        this.noiseData1 = this.detailNoiseGen.generateNoiseOctaves(this.noiseData1, xOffset, yOffset, zOffset, xSize, ySize, zSize, horizontalScale / 80.0D, verticalScale / 160.0D, horizontalScale / 80.0D);
        this.noiseData2 = this.minNoiseGen.generateNoiseOctaves(this.noiseData2, xOffset, yOffset, zOffset, xSize, ySize, zSize, horizontalScale, verticalScale, horizontalScale);
        this.noiseData3 = this.maxNoiseGen.generateNoiseOctaves(this.noiseData3, xOffset, yOffset, zOffset, xSize, ySize, zSize, horizontalScale, verticalScale, horizontalScale);
        int noiseIndex = 0;

        for (int x = 0; x < xSize; ++x)
        {
            for (int z = 0; z < zSize; ++z)
            {
                float distanceX = (float)(x + xOffset) / 1.0F;
                float distanceZ = (float)(z + zOffset) / 1.0F;
                float islandHeight = 100.0F - MathHelper.sqrt_float(distanceX * distanceX + distanceZ * distanceZ) * 8.0F;

                if (islandHeight > 80.0F)
                {
                    islandHeight = 80.0F;
                }

                if (islandHeight < -100.0F)
                {
                    islandHeight = -100.0F;
                }

                for (int y = 0; y < ySize; ++y)
                {
                    double density = 0.0D;
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

                    density = density - 8.0D;
                    density = density + (double)islandHeight;
                    int topFadeOffset = 2;

                    if (y > ySize / 2 - topFadeOffset)
                    {
                        double topFade = (double)((float)(y - (ySize / 2 - topFadeOffset)) / 64.0F);
                        topFade = MathHelper.clamp_double(topFade, 0.0D, 1.0D);
                        density = density * (1.0D - topFade) + -3000.0D * topFade;
                    }

                    int bottomFadeHeight = 8;

                    if (y < bottomFadeHeight)
                    {
                        double bottomFade = (double)((float)(bottomFadeHeight - y) / ((float)bottomFadeHeight - 1.0F));
                        density = density * (1.0D - bottomFade) + -30.0D * bottomFade;
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
        this.world.getBiomeGenForCoords(chunkOrigin.add(16, 0, 16)).decorate(this.world, this.world.rand, chunkOrigin);
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
        return "RandomLevelSource";
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        return this.world.getBiomeGenForCoords(pos).getSpawnableList(creatureType);
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
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}
