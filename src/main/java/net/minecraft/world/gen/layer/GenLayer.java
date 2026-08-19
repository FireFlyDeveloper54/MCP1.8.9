package net.minecraft.world.gen.layer;

import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.ChunkProviderSettings;

public abstract class GenLayer
{
    private long worldGenSeed;
    protected GenLayer parent;
    private long chunkSeed;
    protected long baseSeed;

    public static GenLayer[] initializeAllBiomeGenerators(long seed, WorldType worldType, String generatorOptions)
    {
        GenLayer genLayer = new GenLayerIsland(1L);
        genLayer = new GenLayerFuzzyZoom(2000L, genLayer);
        GenLayerAddIsland genLayerAddIsland = new GenLayerAddIsland(1L, genLayer);
        GenLayerZoom genLayerZoom = new GenLayerZoom(2001L, genLayerAddIsland);
        GenLayerAddIsland genlayeraddisland1 = new GenLayerAddIsland(2L, genLayerZoom);
        genlayeraddisland1 = new GenLayerAddIsland(50L, genlayeraddisland1);
        genlayeraddisland1 = new GenLayerAddIsland(70L, genlayeraddisland1);
        GenLayerRemoveTooMuchOcean genLayerRemoveTooMuchOcean = new GenLayerRemoveTooMuchOcean(2L, genlayeraddisland1);
        GenLayerAddSnow genLayerAddSnow = new GenLayerAddSnow(2L, genLayerRemoveTooMuchOcean);
        GenLayerAddIsland genlayeraddisland2 = new GenLayerAddIsland(3L, genLayerAddSnow);
        GenLayerEdge genLayerEdge = new GenLayerEdge(2L, genlayeraddisland2, GenLayerEdge.Mode.COOL_WARM);
        genLayerEdge = new GenLayerEdge(2L, genLayerEdge, GenLayerEdge.Mode.HEAT_ICE);
        genLayerEdge = new GenLayerEdge(3L, genLayerEdge, GenLayerEdge.Mode.SPECIAL);
        GenLayerZoom genlayerzoom1 = new GenLayerZoom(2002L, genLayerEdge);
        genlayerzoom1 = new GenLayerZoom(2003L, genlayerzoom1);
        GenLayerAddIsland genlayeraddisland3 = new GenLayerAddIsland(4L, genlayerzoom1);
        GenLayerAddMushroomIsland genLayerAddMushroomIsland = new GenLayerAddMushroomIsland(5L, genlayeraddisland3);
        GenLayerDeepOcean genLayerDeepOcean = new GenLayerDeepOcean(4L, genLayerAddMushroomIsland);
        GenLayer genlayer4 = GenLayerZoom.magnify(1000L, genLayerDeepOcean, 0);
        ChunkProviderSettings chunkProviderSettings = null;
        int biomeSize = 4;
        int riverSize = biomeSize;

        if (worldType == WorldType.CUSTOMIZED && generatorOptions.length() > 0)
        {
            chunkProviderSettings = ChunkProviderSettings.Factory.jsonToFactory(generatorOptions).build();
            biomeSize = chunkProviderSettings.biomeSize;
            riverSize = chunkProviderSettings.riverSize;
        }

        if (worldType == WorldType.LARGE_BIOMES)
        {
            biomeSize = 6;
        }

        GenLayer riverInitLayer = GenLayerZoom.magnify(1000L, genlayer4, 0);
        GenLayerRiverInit genLayerRiverInit = new GenLayerRiverInit(100L, riverInitLayer);
        GenLayerBiome biomeLayer = new GenLayerBiome(200L, genlayer4, worldType, generatorOptions);
        GenLayer genlayer6 = GenLayerZoom.magnify(1000L, biomeLayer, 2);
        GenLayerBiomeEdge genLayerBiomeEdge = new GenLayerBiomeEdge(1000L, genlayer6);
        GenLayer riverLayerForHills = GenLayerZoom.magnify(1000L, genLayerRiverInit, 2);
        GenLayer genlayerhills = new GenLayerHills(1000L, genLayerBiomeEdge, riverLayerForHills);
        GenLayer genlayer5 = GenLayerZoom.magnify(1000L, genLayerRiverInit, 2);
        genlayer5 = GenLayerZoom.magnify(1000L, genlayer5, riverSize);
        GenLayerRiver genLayerRiver = new GenLayerRiver(1L, genlayer5);
        GenLayerSmooth genLayerSmooth = new GenLayerSmooth(1000L, genLayerRiver);
        genlayerhills = new GenLayerRareBiome(1001L, genlayerhills);

        for (int zoomIndex = 0; zoomIndex < biomeSize; ++zoomIndex)
        {
            genlayerhills = new GenLayerZoom((long)(1000 + zoomIndex), genlayerhills);

            if (zoomIndex == 0)
            {
                genlayerhills = new GenLayerAddIsland(3L, genlayerhills);
            }

            if (zoomIndex == 1 || biomeSize == 1)
            {
                genlayerhills = new GenLayerShore(1000L, genlayerhills);
            }
        }

        GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerhills);
        GenLayerRiverMix genLayerRiverMix = new GenLayerRiverMix(100L, genlayersmooth1, genLayerSmooth);
        GenLayer genlayer3 = new GenLayerVoronoiZoom(10L, genLayerRiverMix);
        genLayerRiverMix.initWorldGenSeed(seed);
        genlayer3.initWorldGenSeed(seed);
        return new GenLayer[] {genLayerRiverMix, genlayer3, genLayerRiverMix};
    }

    public GenLayer(long baseSeedIn)
    {
        this.baseSeed = baseSeedIn;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += baseSeedIn;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += baseSeedIn;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += baseSeedIn;
    }

    public void initWorldGenSeed(long seed)
    {
        this.worldGenSeed = seed;

        if (this.parent != null)
        {
            this.parent.initWorldGenSeed(seed);
        }

        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
    }

    public void initChunkSeed(long chunkX, long chunkZ)
    {
        this.chunkSeed = this.worldGenSeed;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += chunkX;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += chunkZ;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += chunkX;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += chunkZ;
    }

    protected int nextInt(int bound)
    {
        int randomValue = (int)((this.chunkSeed >> 24) % (long)bound);

        if (randomValue < 0)
        {
            randomValue += bound;
        }

        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += this.worldGenSeed;
        return randomValue;
    }

    public abstract int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight);

    protected static boolean biomesEqualOrMesaPlateau(int biomeIDA, int biomeIDB)
    {
        if (biomeIDA == biomeIDB)
        {
            return true;
        }
        else if (biomeIDA != BiomeGenBase.mesaPlateau_F.biomeID && biomeIDA != BiomeGenBase.mesaPlateau.biomeID)
        {
            final BiomeGenBase biomeGenBase = BiomeGenBase.getBiome(biomeIDA);
            final BiomeGenBase biomegenbase1 = BiomeGenBase.getBiome(biomeIDB);

            try
            {
                return biomeGenBase != null && biomegenbase1 != null ? biomeGenBase.isEqualTo(biomegenbase1) : false;
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Comparing biomes");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Biomes being compared");
                crashReportCategory.addCrashSection("Biome A ID", Integer.valueOf(biomeIDA));
                crashReportCategory.addCrashSection("Biome B ID", Integer.valueOf(biomeIDB));
                crashReportCategory.addCrashSectionCallable("Biome A", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf((Object)biomeGenBase);
                    }
                });
                crashReportCategory.addCrashSectionCallable("Biome B", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf((Object)biomegenbase1);
                    }
                });
                throw new ReportedException(crashReport);
            }
        }
        else
        {
            return biomeIDB == BiomeGenBase.mesaPlateau_F.biomeID || biomeIDB == BiomeGenBase.mesaPlateau.biomeID;
        }
    }

    protected static boolean isBiomeOceanic(int biomeId)
    {
        return biomeId == BiomeGenBase.ocean.biomeID || biomeId == BiomeGenBase.deepOcean.biomeID || biomeId == BiomeGenBase.frozenOcean.biomeID;
    }

    protected int selectRandom(int... values)
    {
        return values[this.nextInt(values.length)];
    }

    protected int selectModeOrRandom(int first, int second, int third, int fourth)
    {
        return second == third && third == fourth ? second : (first == second && first == third ? first : (first == second && first == fourth ? first : (first == third && first == fourth ? first : (first == second && third != fourth ? first : (first == third && second != fourth ? first : (first == fourth && second != third ? first : (second == third && first != fourth ? second : (second == fourth && first != third ? second : (third == fourth && first != second ? third : this.selectRandom(new int[] {first, second, third, fourth}))))))))));
    }
}
