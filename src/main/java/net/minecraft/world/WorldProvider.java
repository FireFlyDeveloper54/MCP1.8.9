package net.minecraft.world;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.FlatGeneratorInfo;

public abstract class WorldProvider
{
    public static final float[] moonPhaseFactors = new float[] {1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    protected World worldObj;
    private WorldType terrainType;
    private String generatorSettings;
    protected WorldChunkManager worldChunkMgr;
    protected boolean isHellWorld;
    protected boolean hasNoSky;
    protected final float[] lightBrightnessTable = new float[16];
    protected int dimensionId;
    private final float[] colorsSunriseSunset = new float[4];
    protected final BlockPos.MutableBlockPos spawnCheckPos = new BlockPos.MutableBlockPos();

    public final void registerWorld(World worldIn)
    {
        this.worldObj = worldIn;
        this.terrainType = worldIn.getWorldInfo().getTerrainType();
        this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
        this.registerWorldChunkManager();
        this.generateLightBrightnessTable();
    }

    protected void generateLightBrightnessTable()
    {
        float minimumBrightness = 0.0F;

        for (int lightLevel = 0; lightLevel <= 15; ++lightLevel)
        {
            float inverseLight = 1.0F - (float)lightLevel / 15.0F;
            this.lightBrightnessTable[lightLevel] = (1.0F - inverseLight) / (inverseLight * 3.0F + 1.0F) * (1.0F - minimumBrightness) + minimumBrightness;
        }
    }

    protected void registerWorldChunkManager()
    {
        WorldType worldType = this.worldObj.getWorldInfo().getTerrainType();

        if (worldType == WorldType.FLAT)
        {
            FlatGeneratorInfo flatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(this.worldObj.getWorldInfo().getGeneratorOptions());
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.getBiomeFromBiomeList(flatGeneratorInfo.getBiome(), BiomeGenBase.DEFAULT_BIOME), 0.5F);
        }
        else if (worldType == WorldType.DEBUG_WORLD)
        {
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.plains, 0.0F);
        }
        else
        {
            this.worldChunkMgr = new WorldChunkManager(this.worldObj);
        }
    }

    public IChunkProvider createChunkGenerator()
    {
        return (IChunkProvider)(this.terrainType == WorldType.FLAT ? new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings) : (this.terrainType == WorldType.DEBUG_WORLD ? new ChunkProviderDebug(this.worldObj) : (this.terrainType == WorldType.CUSTOMIZED ? new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings) : new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings))));
    }

    public boolean canCoordinateBeSpawn(int x, int z)
    {
        return this.worldObj.getGroundAboveSeaLevel(this.spawnCheckPos.set(x, 0, z)) == Blocks.grass;
    }

    public float calculateCelestialAngle(long worldTime, float partialTicks)
    {
        int dayTime = (int)(worldTime % 24000L);
        float celestialAngle = ((float)dayTime + partialTicks) / 24000.0F - 0.25F;

        if (celestialAngle < 0.0F)
        {
            ++celestialAngle;
        }

        if (celestialAngle > 1.0F)
        {
            --celestialAngle;
        }

        float cosineAngle = 1.0F - (float)((Math.cos((double)celestialAngle * Math.PI) + 1.0D) / 2.0D);
        return celestialAngle + (cosineAngle - celestialAngle) / 3.0F;
    }

    public int getMoonPhase(long worldTime)
    {
        return (int)(worldTime / 24000L % 8L + 8L) % 8;
    }

    public boolean isSurfaceWorld()
    {
        return true;
    }

    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks)
    {
        float sunriseRange = 0.4F;
        float angleCosine = MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) - 0.0F;
        float sunriseCenter = -0.0F;

        if (angleCosine >= sunriseCenter - sunriseRange && angleCosine <= sunriseCenter + sunriseRange)
        {
            float colorBlend = (angleCosine - sunriseCenter) / sunriseRange * 0.5F + 0.5F;
            float alpha = 1.0F - (1.0F - MathHelper.sin(colorBlend * (float)Math.PI)) * 0.99F;
            alpha = alpha * alpha;
            this.colorsSunriseSunset[0] = colorBlend * 0.3F + 0.7F;
            this.colorsSunriseSunset[1] = colorBlend * colorBlend * 0.7F + 0.2F;
            this.colorsSunriseSunset[2] = colorBlend * colorBlend * 0.0F + 0.2F;
            this.colorsSunriseSunset[3] = alpha;
            return this.colorsSunriseSunset;
        }
        else
        {
            return null;
        }
    }

    public Vec3 getFogColor(float celestialAngle, float partialTicks)
    {
        float brightness = MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
        brightness = MathHelper.clamp_float(brightness, 0.0F, 1.0F);
        float red = 0.7529412F;
        float green = 0.84705883F;
        float blue = 1.0F;
        red = red * (brightness * 0.94F + 0.06F);
        green = green * (brightness * 0.94F + 0.06F);
        blue = blue * (brightness * 0.91F + 0.09F);
        return new Vec3((double)red, (double)green, (double)blue);
    }

    public boolean canRespawnHere()
    {
        return true;
    }

    public static WorldProvider getProviderForDimension(int dimension)
    {
        return (WorldProvider)(dimension == -1 ? new WorldProviderHell() : (dimension == 0 ? new WorldProviderSurface() : (dimension == 1 ? new WorldProviderEnd() : null)));
    }

    public float getCloudHeight()
    {
        return 128.0F;
    }

    public boolean isSkyColored()
    {
        return true;
    }

    public BlockPos getSpawnCoordinate()
    {
        return null;
    }

    public int getAverageGroundLevel()
    {
        return this.terrainType == WorldType.FLAT ? 4 : this.worldObj.getSeaLevel() + 1;
    }

    public double getVoidFogYFactor()
    {
        return this.terrainType == WorldType.FLAT ? 1.0D : 0.03125D;
    }

    public boolean doesXZShowFog(int x, int z)
    {
        return false;
    }

    public abstract String getDimensionName();

    public abstract String getInternalNameSuffix();

    public WorldChunkManager getWorldChunkManager()
    {
        return this.worldChunkMgr;
    }

    public boolean doesWaterVaporize()
    {
        return this.isHellWorld;
    }

    public boolean getHasNoSky()
    {
        return this.hasNoSky;
    }

    public float[] getLightBrightnessTable()
    {
        return this.lightBrightnessTable;
    }

    public int getDimensionId()
    {
        return this.dimensionId;
    }

    public WorldBorder getWorldBorder()
    {
        return new WorldBorder();
    }
}
