package net.optifine;

import java.util.Properties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.config.VillagerProfession;
import net.optifine.config.Weather;
import net.optifine.reflect.Reflector;
import net.optifine.util.ArrayUtils;
import net.optifine.util.MathUtils;

public class RandomEntityRule
{
    private String pathProps = null;
    private ResourceLocation baseResLoc = null;
    private int index;
    private int[] textures = null;
    private ResourceLocation[] resourceLocations = null;
    private int[] weights = null;
    private BiomeGenBase[] biomes = null;
    private RangeListInt heights = null;
    private RangeListInt healthRange = null;
    private boolean healthPercent = false;
    private NbtTagValue nbtName = null;
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    private VillagerProfession[] professions = null;
    private EnumDyeColor[] collarColors = null;
    private Boolean baby = null;
    private RangeListInt moonPhases = null;
    private RangeListInt dayTimes = null;
    private Weather[] weatherList = null;

    public RandomEntityRule(Properties props, String pathProps, ResourceLocation baseResLoc, int index, String valTextures, ConnectedParser cp)
    {
        this.pathProps = pathProps;
        this.baseResLoc = baseResLoc;
        this.index = index;
        this.textures = cp.parseIntList(valTextures);
        this.weights = cp.parseIntList(props.getProperty("weights." + index));
        this.biomes = cp.parseBiomes(props.getProperty("biomes." + index));
        this.heights = cp.parseRangeListInt(props.getProperty("heights." + index));

        if (this.heights == null)
        {
            this.heights = this.parseMinMaxHeight(props, index);
        }

        String healthText = props.getProperty("health." + index);

        if (healthText != null)
        {
            this.healthPercent = healthText.contains("%");
            healthText = healthText.replace("%", "");
            this.healthRange = cp.parseRangeListInt(healthText);
        }

        this.nbtName = cp.parseNbtTagValue("name", props.getProperty("name." + index));
        this.professions = cp.parseProfessions(props.getProperty("professions." + index));
        this.collarColors = cp.parseDyeColors(props.getProperty("collarColors." + index), "collar color", ConnectedParser.DYE_COLORS_INVALID);
        this.baby = cp.parseBooleanObject(props.getProperty("baby." + index));
        this.moonPhases = cp.parseRangeListInt(props.getProperty("moonPhase." + index));
        this.dayTimes = cp.parseRangeListInt(props.getProperty("dayTime." + index));
        this.weatherList = cp.parseWeather(props.getProperty("weather." + index), "weather." + index, (Weather[])null);
    }

    private RangeListInt parseMinMaxHeight(Properties props, int index)
    {
        String minHeightText = props.getProperty("minHeight." + index);
        String maxHeightText = props.getProperty("maxHeight." + index);

        if (minHeightText == null && maxHeightText == null)
        {
            return null;
        }
        else
        {
            int minHeight = 0;

            if (minHeightText != null)
            {
                minHeight = Config.parseInt(minHeightText, -1);

                if (minHeight < 0)
                {
                    Config.warn("Invalid minHeight: " + minHeightText);
                    return null;
                }
            }

            int maxHeight = 256;

            if (maxHeightText != null)
            {
                maxHeight = Config.parseInt(maxHeightText, -1);

                if (maxHeight < 0)
                {
                    Config.warn("Invalid maxHeight: " + maxHeightText);
                    return null;
                }
            }

            if (maxHeight < 0)
            {
                Config.warn("Invalid minHeight, maxHeight: " + minHeightText + ", " + maxHeightText);
                return null;
            }
            else
            {
                RangeListInt heightRanges = new RangeListInt();
                heightRanges.addRange(new RangeInt(minHeight, maxHeight));
                return heightRanges;
            }
        }
    }

    public boolean isValid(String path)
    {
        if (this.textures != null && this.textures.length != 0)
        {
            if (this.resourceLocations != null)
            {
                return true;
            }
            else
            {
                this.resourceLocations = new ResourceLocation[this.textures.length];
                boolean useMcpatcherPath = this.pathProps.startsWith("mcpatcher/mob/");
                ResourceLocation randomBaseLocation = RandomEntities.getLocationRandom(this.baseResLoc, useMcpatcherPath);

                if (randomBaseLocation == null)
                {
                    Config.warn("Invalid path: " + this.baseResLoc.getResourcePath());
                    return false;
                }
                else
                {
                    for (int resourceIndex = 0; resourceIndex < this.resourceLocations.length; ++resourceIndex)
                    {
                        int textureIndex = this.textures[resourceIndex];

                        if (textureIndex <= 1)
                        {
                            this.resourceLocations[resourceIndex] = this.baseResLoc;
                        }
                        else
                        {
                            ResourceLocation indexedResourceLocation = RandomEntities.getLocationIndexed(randomBaseLocation, textureIndex);

                            if (indexedResourceLocation == null)
                            {
                                Config.warn("Invalid path: " + this.baseResLoc.getResourcePath());
                                return false;
                            }

                            if (!Config.hasResource(indexedResourceLocation))
                            {
                                Config.warn("Texture not found: " + indexedResourceLocation.getResourcePath());
                                return false;
                            }

                            this.resourceLocations[resourceIndex] = indexedResourceLocation;
                        }
                    }

                    if (this.weights != null)
                    {
                        if (this.weights.length > this.resourceLocations.length)
                        {
                            Config.warn("More weights defined than skins, trimming weights: " + path);
                            int[] trimmedWeights = new int[this.resourceLocations.length];
                            System.arraycopy(this.weights, 0, trimmedWeights, 0, trimmedWeights.length);
                            this.weights = trimmedWeights;
                        }

                        if (this.weights.length < this.resourceLocations.length)
                        {
                            Config.warn("Less weights defined than skins, expanding weights: " + path);
                            int[] expandedWeights = new int[this.resourceLocations.length];
                            System.arraycopy(this.weights, 0, expandedWeights, 0, this.weights.length);
                            int averageWeight = MathUtils.getAverage(this.weights);

                            for (int weightIndex = this.weights.length; weightIndex < expandedWeights.length; ++weightIndex)
                            {
                                expandedWeights[weightIndex] = averageWeight;
                            }

                            this.weights = expandedWeights;
                        }

                        this.sumWeights = new int[this.weights.length];
                        int weightSum = 0;

                        for (int weightIndex = 0; weightIndex < this.weights.length; ++weightIndex)
                        {
                            if (this.weights[weightIndex] < 0)
                            {
                                Config.warn("Invalid weight: " + this.weights[weightIndex]);
                                return false;
                            }

                            weightSum += this.weights[weightIndex];
                            this.sumWeights[weightIndex] = weightSum;
                        }

                        this.sumAllWeights = weightSum;

                        if (this.sumAllWeights <= 0)
                        {
                            Config.warn("Invalid sum of all weights: " + weightSum);
                            this.sumAllWeights = 1;
                        }
                    }

                    if (this.professions == ConnectedParser.PROFESSIONS_INVALID)
                    {
                        Config.warn("Invalid professions or careers: " + path);
                        return false;
                    }
                    else if (this.collarColors == ConnectedParser.DYE_COLORS_INVALID)
                    {
                        Config.warn("Invalid collar colors: " + path);
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            Config.warn("Invalid skins for rule: " + this.index);
            return false;
        }
    }

    public boolean matches(IRandomEntity randomEntity)
    {
        if (this.biomes != null && !Matches.biome(randomEntity.getSpawnBiome(), this.biomes))
        {
            return false;
        }
        else
        {
            if (this.heights != null)
            {
                BlockPos spawnPosition = randomEntity.getSpawnPosition();

                if (spawnPosition != null && !this.heights.isInRange(spawnPosition.getY()))
                {
                    return false;
                }
            }

            if (this.healthRange != null)
            {
                int health = randomEntity.getHealth();

                if (this.healthPercent)
                {
                    int maxHealth = randomEntity.getMaxHealth();

                    if (maxHealth > 0)
                    {
                        health = (int)((double)(health * 100) / (double)maxHealth);
                    }
                }

                if (!this.healthRange.isInRange(health))
                {
                    return false;
                }
            }

            if (this.nbtName != null)
            {
                String entityName = randomEntity.getName();

                if (!this.nbtName.matchesValue(entityName))
                {
                    return false;
                }
            }

            if (this.professions != null && randomEntity instanceof RandomEntity)
            {
                RandomEntity randomEntityWrapper = (RandomEntity)randomEntity;
                Entity entity = randomEntityWrapper.getEntity();

                if (entity instanceof EntityVillager)
                {
                    EntityVillager villager = (EntityVillager)entity;
                    int professionId = villager.getProfession();
                    int careerId = Reflector.getFieldValueInt(villager, Reflector.EntityVillager_careerId, -1);

                    if (professionId < 0 || careerId < 0)
                    {
                        return false;
                    }

                    boolean professionMatched = false;

                    for (int professionIndex = 0; professionIndex < this.professions.length; ++professionIndex)
                    {
                        VillagerProfession profession = this.professions[professionIndex];

                        if (profession.matches(professionId, careerId))
                        {
                            professionMatched = true;
                            break;
                        }
                    }

                    if (!professionMatched)
                    {
                        return false;
                    }
                }
            }

            if (this.collarColors != null && randomEntity instanceof RandomEntity)
            {
                RandomEntity randomEntityWrapper = (RandomEntity)randomEntity;
                Entity entity = randomEntityWrapper.getEntity();

                if (entity instanceof EntityWolf)
                {
                    EntityWolf wolf = (EntityWolf)entity;

                    if (!wolf.isTamed())
                    {
                        return false;
                    }

                    EnumDyeColor collarColor = wolf.getCollarColor();

                    if (!Config.equalsOne(collarColor, this.collarColors))
                    {
                        return false;
                    }
                }
            }

            if (this.baby != null && randomEntity instanceof RandomEntity)
            {
                RandomEntity randomEntityWrapper = (RandomEntity)randomEntity;
                Entity entity = randomEntityWrapper.getEntity();

                if (entity instanceof EntityLiving)
                {
                    EntityLiving livingEntity = (EntityLiving)entity;

                    if (livingEntity.isChild() != this.baby.booleanValue())
                    {
                        return false;
                    }
                }
            }

            if (this.moonPhases != null)
            {
                World world = Config.getMinecraft().theWorld;

                if (world != null)
                {
                    int moonPhase = world.getMoonPhase();

                    if (!this.moonPhases.isInRange(moonPhase))
                    {
                        return false;
                    }
                }
            }

            if (this.dayTimes != null)
            {
                World world1 = Config.getMinecraft().theWorld;

                if (world1 != null)
                {
                    int dayTime = (int)world1.getWorldInfo().getWorldTime();

                    if (!this.dayTimes.isInRange(dayTime))
                    {
                        return false;
                    }
                }
            }

            if (this.weatherList != null)
            {
                World world2 = Config.getMinecraft().theWorld;

                if (world2 != null)
                {
                    Weather weather = Weather.getWeather(world2, 0.0F);

                    if (!ArrayUtils.contains(this.weatherList, weather))
                    {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public ResourceLocation getTextureLocation(ResourceLocation loc, int randomId)
    {
        if (this.resourceLocations != null && this.resourceLocations.length != 0)
        {
            int resourceIndex = 0;

            if (this.weights == null)
            {
                resourceIndex = randomId % this.resourceLocations.length;
            }
            else
            {
                int randomWeight = randomId % this.sumAllWeights;

                for (int weightIndex = 0; weightIndex < this.sumWeights.length; ++weightIndex)
                {
                    if (this.sumWeights[weightIndex] > randomWeight)
                    {
                        resourceIndex = weightIndex;
                        break;
                    }
                }
            }

            return this.resourceLocations[resourceIndex];
        }
        else
        {
            return loc;
        }
    }
}
