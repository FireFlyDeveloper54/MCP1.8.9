package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

public class FlatGeneratorInfo
{
    private final List<FlatLayerInfo> flatLayers = Lists.<FlatLayerInfo>newArrayList();
    private final Map<String, Map<String, String>> worldFeatures = Maps.<String, Map<String, String>>newHashMap();
    private int biomeToUse;

    public int getBiome()
    {
        return this.biomeToUse;
    }

    public void setBiome(int biome)
    {
        this.biomeToUse = biome;
    }

    public Map<String, Map<String, String>> getWorldFeatures()
    {
        return this.worldFeatures;
    }

    public List<FlatLayerInfo> getFlatLayers()
    {
        return this.flatLayers;
    }

    public void updateLayerMinY()
    {
        int nextMinY = 0;

        for (FlatLayerInfo layerInfo : this.flatLayers)
        {
            layerInfo.setMinY(nextMinY);
            nextMinY += layerInfo.getLayerCount();
        }
    }

    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((int)3);
        stringBuilder.append(";");

        for (int layerIndex = 0; layerIndex < this.flatLayers.size(); ++layerIndex)
        {
            if (layerIndex > 0)
            {
                stringBuilder.append(",");
            }

            stringBuilder.append(((FlatLayerInfo)this.flatLayers.get(layerIndex)).toString());
        }

        stringBuilder.append(";");
        stringBuilder.append(this.biomeToUse);

        if (!this.worldFeatures.isEmpty())
        {
            stringBuilder.append(";");
            int featureIndex = 0;

            for (Entry<String, Map<String, String>> featureEntry : this.worldFeatures.entrySet())
            {
                if (featureIndex++ > 0)
                {
                    stringBuilder.append(",");
                }

                stringBuilder.append(((String)featureEntry.getKey()).toLowerCase(Locale.ROOT));
                Map<String, String> featureOptions = (Map)featureEntry.getValue();

                if (!featureOptions.isEmpty())
                {
                    stringBuilder.append("(");
                    int optionIndex = 0;

                    for (Entry<String, String> optionEntry : featureOptions.entrySet())
                    {
                        if (optionIndex++ > 0)
                        {
                            stringBuilder.append(" ");
                        }

                        stringBuilder.append((String)optionEntry.getKey());
                        stringBuilder.append("=");
                        stringBuilder.append((String)optionEntry.getValue());
                    }

                    stringBuilder.append(")");
                }
            }
        }
        else
        {
            stringBuilder.append(";");
        }

        return stringBuilder.toString();
    }

    private static FlatLayerInfo parseLayer(int version, String layerString, int minY)
    {
        String[] layerParts = version >= 3 ? layerString.split("\\*", 2) : layerString.split("x", 2);
        int layerCount = 1;
        int metadata = 0;

        if (layerParts.length == 2)
        {
            try
            {
                layerCount = Integer.parseInt(layerParts[0]);

                if (minY + layerCount >= 256)
                {
                    layerCount = 256 - minY;
                }

                if (layerCount < 0)
                {
                    layerCount = 0;
                }
            }
            catch (Throwable caughtThrowable)
            {
                return null;
            }
        }

        Block block = null;

        try
        {
            String blockString = layerParts[layerParts.length - 1];

            if (version < 3)
            {
                String[] blockParts = blockString.split(":", 2);

                if (blockParts.length > 1)
                {
                    metadata = Integer.parseInt(blockParts[1]);
                }

                block = Block.getBlockById(Integer.parseInt(blockParts[0]));
            }
            else
            {
                String[] blockParts = blockString.split(":", 3);
                block = blockParts.length > 1 ? Block.getBlockFromName(blockParts[0] + ":" + blockParts[1]) : null;

                if (block != null)
                {
                    metadata = blockParts.length > 2 ? Integer.parseInt(blockParts[2]) : 0;
                }
                else
                {
                    block = Block.getBlockFromName(blockParts[0]);

                    if (block != null)
                    {
                        metadata = blockParts.length > 1 ? Integer.parseInt(blockParts[1]) : 0;
                    }
                }

                if (block == null)
                {
                    return null;
                }
            }

            if (block == Blocks.air)
            {
                metadata = 0;
            }

            if (metadata < 0 || metadata > 15)
            {
                metadata = 0;
            }
        }
        catch (Throwable caughtThrowable)
        {
            return null;
        }

        FlatLayerInfo layerInfo = new FlatLayerInfo(version, layerCount, block, metadata);
        layerInfo.setMinY(minY);
        return layerInfo;
    }

    private static List<FlatLayerInfo> parseLayers(int version, String layersString)
    {
        if (layersString != null && layersString.length() >= 1)
        {
            List<FlatLayerInfo> layers = Lists.<FlatLayerInfo>newArrayList();
            String[] layerStrings = layersString.split(",");
            int nextMinY = 0;

            for (String layerString : layerStrings)
            {
                FlatLayerInfo flatLayerInfo = parseLayer(version, layerString, nextMinY);

                if (flatLayerInfo == null)
                {
                    return null;
                }

                layers.add(flatLayerInfo);
                nextMinY += flatLayerInfo.getLayerCount();
            }

            return layers;
        }
        else
        {
            return null;
        }
    }

    public static FlatGeneratorInfo createFlatGeneratorFromString(String flatGeneratorSettings)
    {
        if (flatGeneratorSettings == null)
        {
            return getDefaultFlatGenerator();
        }
        else
        {
            String[] settingsParts = flatGeneratorSettings.split(";", -1);
            int version = settingsParts.length == 1 ? 0 : MathHelper.parseIntWithDefault(settingsParts[0], 0);

            if (version >= 0 && version <= 3)
            {
                FlatGeneratorInfo flatGeneratorInfo = new FlatGeneratorInfo();
                int partIndex = settingsParts.length == 1 ? 0 : 1;
                List<FlatLayerInfo> layers = parseLayers(version, settingsParts[partIndex++]);

                if (layers != null && !layers.isEmpty())
                {
                    flatGeneratorInfo.getFlatLayers().addAll(layers);
                    flatGeneratorInfo.updateLayerMinY();
                    int biomeId = BiomeGenBase.plains.biomeID;

                    if (version > 0 && settingsParts.length > partIndex)
                    {
                        biomeId = MathHelper.parseIntWithDefault(settingsParts[partIndex++], biomeId);
                    }

                    flatGeneratorInfo.setBiome(biomeId);

                    if (version > 0 && settingsParts.length > partIndex)
                    {
                        String[] featureStrings = settingsParts[partIndex++].toLowerCase(Locale.ROOT).split(",");

                        for (String featureString : featureStrings)
                        {
                            String[] featureParts = featureString.split("\\(", 2);
                            Map<String, String> featureOptions = Maps.<String, String>newHashMap();

                            if (featureParts[0].length() > 0)
                            {
                                flatGeneratorInfo.getWorldFeatures().put(featureParts[0], featureOptions);

                                if (featureParts.length > 1 && featureParts[1].endsWith(")") && featureParts[1].length() > 1)
                                {
                                    String[] optionStrings = featureParts[1].substring(0, featureParts[1].length() - 1).split(" ");

                                    for (int optionIndex = 0; optionIndex < optionStrings.length; ++optionIndex)
                                    {
                                        String[] optionParts = optionStrings[optionIndex].split("=", 2);

                                        if (optionParts.length == 2)
                                        {
                                            featureOptions.put(optionParts[0], optionParts[1]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        flatGeneratorInfo.getWorldFeatures().put("village", Maps.<String, String>newHashMap());
                    }

                    return flatGeneratorInfo;
                }
                else
                {
                    return getDefaultFlatGenerator();
                }
            }
            else
            {
                return getDefaultFlatGenerator();
            }
        }
    }

    public static FlatGeneratorInfo getDefaultFlatGenerator()
    {
        FlatGeneratorInfo flatGeneratorInfo = new FlatGeneratorInfo();
        flatGeneratorInfo.setBiome(BiomeGenBase.plains.biomeID);
        flatGeneratorInfo.getFlatLayers().add(new FlatLayerInfo(1, Blocks.bedrock));
        flatGeneratorInfo.getFlatLayers().add(new FlatLayerInfo(2, Blocks.dirt));
        flatGeneratorInfo.getFlatLayers().add(new FlatLayerInfo(1, Blocks.grass));
        flatGeneratorInfo.updateLayerMinY();
        flatGeneratorInfo.getWorldFeatures().put("village", Maps.<String, String>newHashMap());
        return flatGeneratorInfo;
    }
}
