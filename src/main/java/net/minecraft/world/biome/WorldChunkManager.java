package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class WorldChunkManager
{
    private GenLayer genBiomes;
    private GenLayer biomeIndexLayer;
    private BiomeCache biomeCache;
    private List<BiomeGenBase> biomesToSpawnIn;
    private String generatorOptions;

    protected WorldChunkManager()
    {
        this.biomeCache = new BiomeCache(this);
        this.generatorOptions = "";
        this.biomesToSpawnIn = Lists.<BiomeGenBase>newArrayList();
        this.biomesToSpawnIn.add(BiomeGenBase.forest);
        this.biomesToSpawnIn.add(BiomeGenBase.plains);
        this.biomesToSpawnIn.add(BiomeGenBase.taiga);
        this.biomesToSpawnIn.add(BiomeGenBase.taigaHills);
        this.biomesToSpawnIn.add(BiomeGenBase.forestHills);
        this.biomesToSpawnIn.add(BiomeGenBase.jungle);
        this.biomesToSpawnIn.add(BiomeGenBase.jungleHills);
    }

    public WorldChunkManager(long seed, WorldType worldTypeIn, String options)
    {
        this();
        this.generatorOptions = options;
        GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(seed, worldTypeIn, options);
        this.genBiomes = agenlayer[0];
        this.biomeIndexLayer = agenlayer[1];
    }

    public WorldChunkManager(World worldIn)
    {
        this(worldIn.getSeed(), worldIn.getWorldInfo().getTerrainType(), worldIn.getWorldInfo().getGeneratorOptions());
    }

    public List<BiomeGenBase> getBiomesToSpawnIn()
    {
        return this.biomesToSpawnIn;
    }

    public BiomeGenBase getBiomeGenerator(BlockPos pos)
    {
        return this.getBiomeGenerator(pos, (BiomeGenBase)null);
    }

    public BiomeGenBase getBiomeGenerator(BlockPos pos, BiomeGenBase biomeGenBaseIn)
    {
        return this.biomeCache.getBiomeGenAtWithFallback(pos.getX(), pos.getZ(), biomeGenBaseIn);
    }

    public float[] getRainfall(float[] listToReuse, int x, int z, int width, int length)
    {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length)
        {
            listToReuse = new float[width * length];
        }

        int[] biomeIds = this.biomeIndexLayer.getInts(x, z, width, length);

        for (int areaIndex = 0; areaIndex < width * length; ++areaIndex)
        {
            try
            {
                float rainfall = (float)BiomeGenBase.getBiomeFromBiomeList(biomeIds[areaIndex], BiomeGenBase.DEFAULT_BIOME).getIntRainfall() / 65536.0F;

                if (rainfall > 1.0F)
                {
                    rainfall = 1.0F;
                }

                listToReuse[areaIndex] = rainfall;
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("DownfallBlock");
                crashReportCategory.addCrashSection("biome id", Integer.valueOf(areaIndex));
                crashReportCategory.addCrashSection("downfalls[] size", Integer.valueOf(listToReuse.length));
                crashReportCategory.addCrashSection("x", Integer.valueOf(x));
                crashReportCategory.addCrashSection("z", Integer.valueOf(z));
                crashReportCategory.addCrashSection("w", Integer.valueOf(width));
                crashReportCategory.addCrashSection("h", Integer.valueOf(length));
                throw new ReportedException(crashReport);
            }
        }

        return listToReuse;
    }

    public float getTemperatureAtHeight(float temperature, int height)
    {
        return temperature;
    }

    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int x, int z, int width, int height)
    {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * height)
        {
            biomes = new BiomeGenBase[width * height];
        }

        int[] biomeIds = this.genBiomes.getInts(x, z, width, height);

        try
        {
            for (int areaIndex = 0; areaIndex < width * height; ++areaIndex)
            {
                biomes[areaIndex] = BiomeGenBase.getBiomeFromBiomeList(biomeIds[areaIndex], BiomeGenBase.DEFAULT_BIOME);
            }

            return biomes;
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("RawBiomeBlock");
            crashReportCategory.addCrashSection("biomes[] size", Integer.valueOf(biomes.length));
            crashReportCategory.addCrashSection("x", Integer.valueOf(x));
            crashReportCategory.addCrashSection("z", Integer.valueOf(z));
            crashReportCategory.addCrashSection("w", Integer.valueOf(width));
            crashReportCategory.addCrashSection("h", Integer.valueOf(height));
            throw new ReportedException(crashReport);
        }
    }

    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] oldBiomeList, int x, int z, int width, int depth)
    {
        return this.getBiomeGenAt(oldBiomeList, x, z, width, depth, true);
    }

    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] listToReuse, int x, int z, int width, int length, boolean cacheFlag)
    {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length)
        {
            listToReuse = new BiomeGenBase[width * length];
        }

        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0)
        {
            BiomeGenBase[] cachedBiomes = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(cachedBiomes, 0, listToReuse, 0, width * length);
            return listToReuse;
        }
        else
        {
            int[] biomeIds = this.biomeIndexLayer.getInts(x, z, width, length);

            for (int areaIndex = 0; areaIndex < width * length; ++areaIndex)
            {
                listToReuse[areaIndex] = BiomeGenBase.getBiomeFromBiomeList(biomeIds[areaIndex], BiomeGenBase.DEFAULT_BIOME);
            }

            return listToReuse;
        }
    }

    public boolean areBiomesViable(int x, int z, int radius, List<BiomeGenBase> allowedBiomes)
    {
        IntCache.resetIntCache();
        int minLayerX = x - radius >> 2;
        int minLayerZ = z - radius >> 2;
        int maxLayerX = x + radius >> 2;
        int maxLayerZ = z + radius >> 2;
        int layerWidth = maxLayerX - minLayerX + 1;
        int layerLength = maxLayerZ - minLayerZ + 1;
        int[] biomeIds = this.genBiomes.getInts(minLayerX, minLayerZ, layerWidth, layerLength);

        try
        {
            for (int layerIndex = 0; layerIndex < layerWidth * layerLength; ++layerIndex)
            {
                BiomeGenBase biomeGenBase = BiomeGenBase.getBiome(biomeIds[layerIndex]);

                if (!allowedBiomes.contains(biomeGenBase))
                {
                    return false;
                }
            }

            return true;
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Layer");
            crashReportCategory.addCrashSection("Layer", this.genBiomes.toString());
            crashReportCategory.addCrashSection("x", Integer.valueOf(x));
            crashReportCategory.addCrashSection("z", Integer.valueOf(z));
            crashReportCategory.addCrashSection("radius", Integer.valueOf(radius));
            crashReportCategory.addCrashSection("allowed", allowedBiomes);
            throw new ReportedException(crashReport);
        }
    }

    public BlockPos findBiomePosition(int x, int z, int range, List<BiomeGenBase> biomes, Random random)
    {
        IntCache.resetIntCache();
        int minLayerX = x - range >> 2;
        int minLayerZ = z - range >> 2;
        int maxLayerX = x + range >> 2;
        int maxLayerZ = z + range >> 2;
        int layerWidth = maxLayerX - minLayerX + 1;
        int layerLength = maxLayerZ - minLayerZ + 1;
        int[] biomeIds = this.genBiomes.getInts(minLayerX, minLayerZ, layerWidth, layerLength);
        BlockPos selectedBiomePos = null;
        int matchingBiomeCount = 0;

        for (int layerIndex = 0; layerIndex < layerWidth * layerLength; ++layerIndex)
        {
            int blockX = minLayerX + layerIndex % layerWidth << 2;
            int blockZ = minLayerZ + layerIndex / layerWidth << 2;
            BiomeGenBase biomeGenBase = BiomeGenBase.getBiome(biomeIds[layerIndex]);

            if (biomes.contains(biomeGenBase) && (selectedBiomePos == null || random.nextInt(matchingBiomeCount + 1) == 0))
            {
                selectedBiomePos = new BlockPos(blockX, 0, blockZ);
                ++matchingBiomeCount;
            }
        }

        return selectedBiomePos;
    }

    public void cleanupCache()
    {
        this.biomeCache.cleanupCache();
    }
}
