package net.minecraft.world.gen.layer;

import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.ChunkProviderSettings;

public class GenLayerBiome extends GenLayer
{
    private BiomeGenBase[] warmBiomes = new BiomeGenBase[] {BiomeGenBase.desert, BiomeGenBase.desert, BiomeGenBase.desert, BiomeGenBase.savanna, BiomeGenBase.savanna, BiomeGenBase.plains};
    private BiomeGenBase[] mediumBiomes = new BiomeGenBase[] {BiomeGenBase.forest, BiomeGenBase.roofedForest, BiomeGenBase.extremeHills, BiomeGenBase.plains, BiomeGenBase.birchForest, BiomeGenBase.swampland};
    private BiomeGenBase[] coldBiomes = new BiomeGenBase[] {BiomeGenBase.forest, BiomeGenBase.extremeHills, BiomeGenBase.taiga, BiomeGenBase.plains};
    private BiomeGenBase[] iceBiomes = new BiomeGenBase[] {BiomeGenBase.icePlains, BiomeGenBase.icePlains, BiomeGenBase.icePlains, BiomeGenBase.coldTaiga};
    private final ChunkProviderSettings settings;

    public GenLayerBiome(long baseSeedIn, GenLayer parent, WorldType worldType, String generatorOptions)
    {
        super(baseSeedIn);
        this.parent = parent;

        if (worldType == WorldType.DEFAULT_1_1)
        {
            this.warmBiomes = new BiomeGenBase[] {BiomeGenBase.desert, BiomeGenBase.forest, BiomeGenBase.extremeHills, BiomeGenBase.swampland, BiomeGenBase.plains, BiomeGenBase.taiga};
            this.settings = null;
        }
        else if (worldType == WorldType.CUSTOMIZED)
        {
            this.settings = ChunkProviderSettings.Factory.jsonToFactory(generatorOptions).build();
        }
        else
        {
            this.settings = null;
        }
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] parentValues = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] biomeValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));
                int parentBiomeId = parentValues[localX + localY * areaWidth];
                int specialVariant = (parentBiomeId & 3840) >> 8;
                parentBiomeId = parentBiomeId & -3841;

                if (this.settings != null && this.settings.fixedBiome >= 0)
                {
                    biomeValues[localX + localY * areaWidth] = this.settings.fixedBiome;
                }
                else if (isBiomeOceanic(parentBiomeId))
                {
                    biomeValues[localX + localY * areaWidth] = parentBiomeId;
                }
                else if (parentBiomeId == BiomeGenBase.mushroomIsland.biomeID)
                {
                    biomeValues[localX + localY * areaWidth] = parentBiomeId;
                }
                else if (parentBiomeId == 1)
                {
                    if (specialVariant > 0)
                    {
                        if (this.nextInt(3) == 0)
                        {
                            biomeValues[localX + localY * areaWidth] = BiomeGenBase.mesaPlateau.biomeID;
                        }
                        else
                        {
                            biomeValues[localX + localY * areaWidth] = BiomeGenBase.mesaPlateau_F.biomeID;
                        }
                    }
                    else
                    {
                        biomeValues[localX + localY * areaWidth] = this.warmBiomes[this.nextInt(this.warmBiomes.length)].biomeID;
                    }
                }
                else if (parentBiomeId == 2)
                {
                    if (specialVariant > 0)
                    {
                        biomeValues[localX + localY * areaWidth] = BiomeGenBase.jungle.biomeID;
                    }
                    else
                    {
                        biomeValues[localX + localY * areaWidth] = this.mediumBiomes[this.nextInt(this.mediumBiomes.length)].biomeID;
                    }
                }
                else if (parentBiomeId == 3)
                {
                    if (specialVariant > 0)
                    {
                        biomeValues[localX + localY * areaWidth] = BiomeGenBase.megaTaiga.biomeID;
                    }
                    else
                    {
                        biomeValues[localX + localY * areaWidth] = this.coldBiomes[this.nextInt(this.coldBiomes.length)].biomeID;
                    }
                }
                else if (parentBiomeId == 4)
                {
                    biomeValues[localX + localY * areaWidth] = this.iceBiomes[this.nextInt(this.iceBiomes.length)].biomeID;
                }
                else
                {
                    biomeValues[localX + localY * areaWidth] = BiomeGenBase.mushroomIsland.biomeID;
                }
            }
        }

        return biomeValues;
    }
}
