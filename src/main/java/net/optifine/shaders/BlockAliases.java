package net.optifine.shaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.config.MacroProcessor;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.StrUtils;

public class BlockAliases
{
    private static BlockAlias[][] blockAliases = (BlockAlias[][])null;
    private static PropertiesOrdered blockLayerPropertes = null;
    private static boolean updateOnResourcesReloaded;

    public static int getBlockAliasId(int blockId, int metadata)
    {
        if (blockAliases == null)
        {
            return blockId;
        }
        else if (blockId >= 0 && blockId < blockAliases.length)
        {
            BlockAlias[] aliasesForBlock = blockAliases[blockId];

            if (aliasesForBlock == null)
            {
                return blockId;
            }
            else
            {
                for (int aliasIndex = 0; aliasIndex < aliasesForBlock.length; ++aliasIndex)
                {
                    BlockAlias blockAlias = aliasesForBlock[aliasIndex];

                    if (blockAlias.matches(blockId, metadata))
                    {
                        return blockAlias.getBlockAliasId();
                    }
                }

                return blockId;
            }
        }
        else
        {
            return blockId;
        }
    }

    public static void resourcesReloaded()
    {
        if (updateOnResourcesReloaded)
        {
            updateOnResourcesReloaded = false;
            update(Shaders.getShaderPack());
        }
    }

    public static void update(IShaderPack shaderPack)
    {
        reset();

        if (shaderPack != null)
        {
            
            List<List<BlockAlias>> blockAliasLists = new ArrayList();
            String path = "/shaders/block.properties";
            InputStream inputStream = shaderPack.getResourceAsStream(path);

            if (inputStream != null)
            {
                loadBlockAliases(inputStream, path, blockAliasLists);
            }

            loadModBlockAliases(blockAliasLists);

            if (((List)blockAliasLists).size() > 0)
            {
                blockAliases = toArrays(blockAliasLists);
            }
        
        }
    }

    private static void loadModBlockAliases(List<List<BlockAlias>> listBlockAliases)
    {
        String[] modIds = new String[0];

        for (int modIndex = 0; modIndex < modIds.length; ++modIndex)
        {
            String modId = modIds[modIndex];

            try
            {
                ResourceLocation resourceLocation = new ResourceLocation(modId, "shaders/block.properties");
                InputStream inputStream = Config.getResourceStream(resourceLocation);
                loadBlockAliases(inputStream, resourceLocation.toString(), listBlockAliases);
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }
    }

    private static void loadBlockAliases(InputStream in, String path, List<List<BlockAlias>> listBlockAliases)
    {
        if (in != null)
        {
            try
            {
                in = MacroProcessor.process(in, path);
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                Config.dbg("[Shaders] Parsing block mappings: " + path);
                ConnectedParser connectedParser = new ConnectedParser("Shaders");

                for (Object propertyKey : properties.keySet())
                {
                    String propertyName = (String)propertyKey;
                    String propertyValue = properties.getProperty(propertyName);

                    if (propertyName.startsWith("layer."))
                    {
                        if (blockLayerPropertes == null)
                        {
                            blockLayerPropertes = new PropertiesOrdered();
                        }

                        blockLayerPropertes.put(propertyName, propertyValue);
                    }
                    else
                    {
                        String blockPrefix = "block.";

                        if (!propertyName.startsWith(blockPrefix))
                        {
                            Config.warn("[Shaders] Invalid block ID: " + propertyName);
                        }
                        else
                        {
                            String aliasIdText = StrUtils.removePrefix(propertyName, blockPrefix);
                            int aliasBlockId = Config.parseInt(aliasIdText, -1);

                            if (aliasBlockId < 0)
                            {
                                Config.warn("[Shaders] Invalid block ID: " + propertyName);
                            }
                            else
                            {
                                MatchBlock[] matchBlocks = connectedParser.parseMatchBlocks(propertyValue);

                                if (matchBlocks != null && matchBlocks.length >= 1)
                                {
                                    BlockAlias blockAlias = new BlockAlias(aliasBlockId, matchBlocks);
                                    addToList(listBlockAliases, blockAlias);
                                }
                                else
                                {
                                    Config.warn("[Shaders] Invalid block ID mapping: " + propertyName + "=" + propertyValue);
                                }
                            }
                        }
                    }
                }
            }
            catch (IOException caughtIoException)
            {
                Config.warn("[Shaders] Error reading: " + path);
            }
        }
    }

    private static void addToList(List<List<BlockAlias>> blocksAliases, BlockAlias blockAlias)
    {
        int[] matchBlockIds = blockAlias.getMatchBlockIds();

        for (int matchIndex = 0; matchIndex < matchBlockIds.length; ++matchIndex)
        {
            int blockId = matchBlockIds[matchIndex];

            while (blockId >= blocksAliases.size())
            {
                blocksAliases.add(null);
            }

            List<BlockAlias> aliasesForBlock = (List)blocksAliases.get(blockId);

            if (aliasesForBlock == null)
            {
                aliasesForBlock = new ArrayList();
                blocksAliases.set(blockId, aliasesForBlock);
            }

            BlockAlias matchedBlockAlias = new BlockAlias(blockAlias.getBlockAliasId(), blockAlias.getMatchBlocks(blockId));
            aliasesForBlock.add(matchedBlockAlias);
        }
    }

    private static BlockAlias[][] toArrays(List<List<BlockAlias>> listBlocksAliases)
    {
        BlockAlias[][] aliasArrays = new BlockAlias[listBlocksAliases.size()][];

        for (int blockId = 0; blockId < aliasArrays.length; ++blockId)
        {
            List<BlockAlias> aliasesForBlock = (List)listBlocksAliases.get(blockId);

            if (aliasesForBlock != null)
            {
                aliasArrays[blockId] = (BlockAlias[])((BlockAlias[])aliasesForBlock.toArray(new BlockAlias[aliasesForBlock.size()]));
            }
        }

        return aliasArrays;
    }

    public static PropertiesOrdered getBlockLayerPropertes()
    {
        return blockLayerPropertes;
    }

    public static void reset()
    {
        blockAliases = (BlockAlias[][])null;
        blockLayerPropertes = null;
    }
}
