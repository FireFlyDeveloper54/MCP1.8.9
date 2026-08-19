package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeGenHills extends BiomeGenBase
{
    private WorldGenerator theWorldGenerator = new WorldGenMinable(Blocks.monster_egg.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE), 9);
    private WorldGenTaiga2 spruceTreeGenerator = new WorldGenTaiga2(false);
    private int normalHillsType = 0;
    private int extraTreesType = 1;
    private int mutatedHillsType = 2;
    private int hillType;

    protected BiomeGenHills(int id, boolean extraTreesIn)
    {
        super(id);
        this.hillType = this.normalHillsType;

        if (extraTreesIn)
        {
            this.theBiomeDecorator.treesPerChunk = 3;
            this.hillType = this.extraTreesType;
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return (WorldGenAbstractTree)(rand.nextInt(3) > 0 ? this.spruceTreeGenerator : super.genBigTreeChance(rand));
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        super.decorate(worldIn, rand, pos);
        int emeraldCount = 3 + rand.nextInt(6);

        for (int emeraldIndex = 0; emeraldIndex < emeraldCount; ++emeraldIndex)
        {
            int emeraldXOffset = rand.nextInt(16);
            int emeraldYOffset = rand.nextInt(28) + 4;
            int emeraldZOffset = rand.nextInt(16);
            BlockPos emeraldPos = pos.add(emeraldXOffset, emeraldYOffset, emeraldZOffset);

            if (worldIn.getBlockState(emeraldPos).getBlock() == Blocks.stone)
            {
                worldIn.setBlockState(emeraldPos, Blocks.emerald_ore.getDefaultState(), 2);
            }
        }

        for (int silverfishIndex = 0; silverfishIndex < 7; ++silverfishIndex)
        {
            int silverfishXOffset = rand.nextInt(16);
            int silverfishYOffset = rand.nextInt(64);
            int silverfishZOffset = rand.nextInt(16);
            this.theWorldGenerator.generate(worldIn, rand, pos.add(silverfishXOffset, silverfishYOffset, silverfishZOffset));
        }
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal)
    {
        this.topBlock = Blocks.grass.getDefaultState();
        this.fillerBlock = Blocks.dirt.getDefaultState();

        if ((noiseVal < -1.0D || noiseVal > 2.0D) && this.hillType == this.mutatedHillsType)
        {
            this.topBlock = Blocks.gravel.getDefaultState();
            this.fillerBlock = Blocks.gravel.getDefaultState();
        }
        else if (noiseVal > 1.0D && this.hillType != this.extraTreesType)
        {
            this.topBlock = Blocks.stone.getDefaultState();
            this.fillerBlock = Blocks.stone.getDefaultState();
        }

        this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }

    private BiomeGenHills mutateHills(BiomeGenBase sourceBiome)
    {
        this.hillType = this.mutatedHillsType;
        this.setColor(sourceBiome.color, true);
        this.setBiomeName(sourceBiome.biomeName + " M");
        this.setHeight(new BiomeGenBase.Height(sourceBiome.minHeight, sourceBiome.maxHeight));
        this.setTemperatureRainfall(sourceBiome.temperature, sourceBiome.rainfall);
        return this;
    }

    protected BiomeGenBase createMutatedBiome(int newBiomeId)
    {
        return (new BiomeGenHills(newBiomeId, false)).mutateHills(this);
    }
}
