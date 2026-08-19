package net.optifine.shaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.config.ConnectedParser;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.config.MacroProcessor;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.StrUtils;

public class ItemAliases
{
    private static int[] itemAliases = null;
    private static boolean updateOnResourcesReloaded;
    private static final int NO_ALIAS = Integer.MIN_VALUE;

    public static int getItemAliasId(int itemId)
    {
        if (itemAliases == null)
        {
            return itemId;
        }
        else if (itemId >= 0 && itemId < itemAliases.length)
        {
            int aliasId = itemAliases[itemId];
            return aliasId == Integer.MIN_VALUE ? itemId : aliasId;
        }
        else
        {
            return itemId;
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
            
            List<Integer> aliasList = new ArrayList();
            String path = "/shaders/item.properties";
            InputStream inputStream = shaderPack.getResourceAsStream(path);

            if (inputStream != null)
            {
                loadItemAliases(inputStream, path, aliasList);
            }

            loadModItemAliases(aliasList);

            if (((List)aliasList).size() > 0)
            {
                itemAliases = toArray(aliasList);
            }
        
        }
    }

    private static void loadModItemAliases(List<Integer> listItemAliases)
    {
        String[] modIds = new String[0];

        for (int modIndex = 0; modIndex < modIds.length; ++modIndex)
        {
            String modId = modIds[modIndex];

            try
            {
                ResourceLocation resourceLocation = new ResourceLocation(modId, "shaders/item.properties");
                InputStream inputStream = Config.getResourceStream(resourceLocation);
                loadItemAliases(inputStream, resourceLocation.toString(), listItemAliases);
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }
    }

    private static void loadItemAliases(InputStream in, String path, List<Integer> listItemAliases)
    {
        if (in != null)
        {
            try
            {
                in = MacroProcessor.process(in, path);
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                Config.dbg("[Shaders] Parsing item mappings: " + path);
                ConnectedParser connectedParser = new ConnectedParser("Shaders");

                for (Object propertyKey : properties.keySet())
                {
                    String propertyName = (String)propertyKey;
                    String propertyValue = properties.getProperty(propertyName);
                    String itemPrefix = "item.";

                    if (!propertyName.startsWith(itemPrefix))
                    {
                        Config.warn("[Shaders] Invalid item ID: " + propertyName);
                    }
                    else
                    {
                        String aliasIdText = StrUtils.removePrefix(propertyName, itemPrefix);
                        int aliasItemId = Config.parseInt(aliasIdText, -1);

                        if (aliasItemId < 0)
                        {
                            Config.warn("[Shaders] Invalid item alias ID: " + aliasItemId);
                        }
                        else
                        {
                            int[] itemIds = connectedParser.parseItems(propertyValue);

                            if (itemIds != null && itemIds.length >= 1)
                            {
                                for (int itemIndex = 0; itemIndex < itemIds.length; ++itemIndex)
                                {
                                    int itemId = itemIds[itemIndex];
                                    addToList(listItemAliases, itemId, aliasItemId);
                                }
                            }
                            else
                            {
                                Config.warn("[Shaders] Invalid item ID mapping: " + propertyName + "=" + propertyValue);
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

    private static void addToList(List<Integer> aliasList, int sourceItemId, int aliasItemId)
    {
        while (aliasList.size() <= sourceItemId)
        {
            aliasList.add(Integer.valueOf(Integer.MIN_VALUE));
        }

        aliasList.set(sourceItemId, Integer.valueOf(aliasItemId));
    }

    private static int[] toArray(List<Integer> aliasList)
    {
        int[] aliases = new int[aliasList.size()];

        for (int index = 0; index < aliases.length; ++index)
        {
            aliases[index] = ((Integer)aliasList.get(index)).intValue();
        }

        return aliases;
    }

    public static void reset()
    {
        itemAliases = null;
    }
}
