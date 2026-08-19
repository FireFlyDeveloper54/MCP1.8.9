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

public class EntityAliases
{
    private static int[] entityAliases = null;
    private static boolean updateOnResourcesReloaded;

    public static int getEntityAliasId(int entityId)
    {
        if (entityAliases == null)
        {
            return -1;
        }
        else if (entityId >= 0 && entityId < entityAliases.length)
        {
            int aliasId = entityAliases[entityId];
            return aliasId;
        }
        else
        {
            return -1;
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
            String path = "/shaders/entity.properties";
            InputStream inputStream = shaderPack.getResourceAsStream(path);

            if (inputStream != null)
            {
                loadEntityAliases(inputStream, path, aliasList);
            }

            loadModEntityAliases(aliasList);

            if (((List)aliasList).size() > 0)
            {
                entityAliases = toArray(aliasList);
            }
        
        }
    }

    private static void loadModEntityAliases(List<Integer> listEntityAliases)
    {
        String[] modIds = new String[0];

        for (int modIndex = 0; modIndex < modIds.length; ++modIndex)
        {
            String modId = modIds[modIndex];

            try
            {
                ResourceLocation resourceLocation = new ResourceLocation(modId, "shaders/entity.properties");
                InputStream inputStream = Config.getResourceStream(resourceLocation);
                loadEntityAliases(inputStream, resourceLocation.toString(), listEntityAliases);
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }
    }

    private static void loadEntityAliases(InputStream in, String path, List<Integer> listEntityAliases)
    {
        if (in != null)
        {
            try
            {
                in = MacroProcessor.process(in, path);
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                Config.dbg("[Shaders] Parsing entity mappings: " + path);
                ConnectedParser connectedParser = new ConnectedParser("Shaders");

                for (Object propertyKey : properties.keySet())
                {
                    String propertyName = (String)propertyKey;
                    String propertyValue = properties.getProperty(propertyName);
                    String entityPrefix = "entity.";

                    if (!propertyName.startsWith(entityPrefix))
                    {
                        Config.warn("[Shaders] Invalid entity ID: " + propertyName);
                    }
                    else
                    {
                        String aliasIdText = StrUtils.removePrefix(propertyName, entityPrefix);
                        int aliasEntityId = Config.parseInt(aliasIdText, -1);

                        if (aliasEntityId < 0)
                        {
                            Config.warn("[Shaders] Invalid entity alias ID: " + aliasEntityId);
                        }
                        else
                        {
                            int[] entityIds = connectedParser.parseEntities(propertyValue);

                            if (entityIds != null && entityIds.length >= 1)
                            {
                                for (int entityIndex = 0; entityIndex < entityIds.length; ++entityIndex)
                                {
                                    int entityId = entityIds[entityIndex];
                                    addToList(listEntityAliases, entityId, aliasEntityId);
                                }
                            }
                            else
                            {
                                Config.warn("[Shaders] Invalid entity ID mapping: " + propertyName + "=" + propertyValue);
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

    private static void addToList(List<Integer> aliasList, int sourceEntityId, int aliasEntityId)
    {
        while (aliasList.size() <= sourceEntityId)
        {
            aliasList.add(Integer.valueOf(-1));
        }

        aliasList.set(sourceEntityId, Integer.valueOf(aliasEntityId));
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
        entityAliases = null;
    }
}
