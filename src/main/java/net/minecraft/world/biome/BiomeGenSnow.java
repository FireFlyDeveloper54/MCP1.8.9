package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import net.minecraft.world.gen.feature.WorldGenIceSpike;
import net.minecraft.world.gen.feature.WorldGenTaiga2;

public class BiomeGenSnow extends BiomeGenBase
{
    private boolean spikesEnabled;
    private WorldGenIceSpike iceSpikeGen = new WorldGenIceSpike();
    private WorldGenIcePath icePathGen = new WorldGenIcePath(4);

    public BiomeGenSnow(int id, boolean spikesEnabledIn)
    {
        super(id);
        this.spikesEnabled = spikesEnabledIn;

        if (spikesEnabledIn)
        {
            this.topBlock = Blocks.snow.getDefaultState();
        }

        this.spawnableCreatureList.clear();
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        if (this.spikesEnabled)
        {
            for (int spikeIndex = 0; spikeIndex < 3; ++spikeIndex)
            {
                int spikeXOffset = rand.nextInt(16) + 8;
                int spikeZOffset = rand.nextInt(16) + 8;
                this.iceSpikeGen.generate(worldIn, rand, worldIn.getHeight(pos.add(spikeXOffset, 0, spikeZOffset)));
            }

            for (int pathIndex = 0; pathIndex < 2; ++pathIndex)
            {
                int pathXOffset = rand.nextInt(16) + 8;
                int pathZOffset = rand.nextInt(16) + 8;
                this.icePathGen.generate(worldIn, rand, worldIn.getHeight(pos.add(pathXOffset, 0, pathZOffset)));
            }
        }

        super.decorate(worldIn, rand, pos);
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return new WorldGenTaiga2(false);
    }

    protected BiomeGenBase createMutatedBiome(int newBiomeId)
    {
        BiomeGenBase mutatedBiome = (new BiomeGenSnow(newBiomeId, true)).setColor(13828095, true).setBiomeName(this.biomeName + " Spikes").setEnableSnow().setTemperatureRainfall(0.0F, 0.5F).setHeight(new BiomeGenBase.Height(this.minHeight + 0.1F, this.maxHeight + 0.1F));
        mutatedBiome.minHeight = this.minHeight + 0.3F;
        mutatedBiome.maxHeight = this.maxHeight + 0.4F;
        return mutatedBiome;
    }
}
