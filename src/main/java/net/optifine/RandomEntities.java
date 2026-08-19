package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorRaw;
import net.optifine.util.IntegratedServerUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;

public class RandomEntities
{
    private static Map<String, RandomEntityProperties> mapProperties = new HashMap();
    private static boolean active = false;
    private static RenderGlobal renderGlobal;
    private static RandomEntity randomEntity = new RandomEntity();
    private static TileEntityRendererDispatcher tileEntityRendererDispatcher;
    private static RandomTileEntity randomTileEntity = new RandomTileEntity();
    private static boolean working = false;
    public static final String SUFFIX_PNG = ".png";
    public static final String SUFFIX_PROPERTIES = ".properties";
    public static final String PREFIX_TEXTURES_ENTITY = "textures/entity/";
    public static final String PREFIX_TEXTURES_PAINTING = "textures/painting/";
    public static final String PREFIX_TEXTURES = "textures/";
    public static final String PREFIX_OPTIFINE_RANDOM = "optifine/random/";
    public static final String PREFIX_MCPATCHER_MOB = "mcpatcher/mob/";
    private static final String[] DEPENDANT_SUFFIXES = new String[] {"_armor", "_eyes", "_exploding", "_shooting", "_fur", "_eyes", "_invulnerable", "_angry", "_tame", "_collar"};
    private static final String PREFIX_DYNAMIC_TEXTURE_HORSE = "horse/";
    private static final String[] HORSE_TEXTURES = (String[])((String[])ReflectorRaw.getFieldValue((Object)null, EntityHorse.class, String[].class, 2));
    private static final String[] HORSE_TEXTURES_ABBR = (String[])((String[])ReflectorRaw.getFieldValue((Object)null, EntityHorse.class, String[].class, 3));

    public static void entityLoaded(Entity entity, World world)
    {
        if (world != null)
        {
            DataWatcher dataWatcher = entity.getDataWatcher();
            dataWatcher.spawnPosition = entity.getPosition();
            dataWatcher.spawnBiome = world.getBiomeGenForCoords(dataWatcher.spawnPosition);
            UUID uUID = entity.getUniqueID();

            if (entity instanceof EntityVillager)
            {
                updateEntityVillager(uUID, (EntityVillager)entity);
            }
        }
    }

    public static void entityUnloaded(Entity entity, World world)
    {
    }

    private static void updateEntityVillager(UUID uuid, EntityVillager ev)
    {
        Entity entity = IntegratedServerUtils.getEntity(uuid);

        if (entity instanceof EntityVillager)
        {
            EntityVillager serverVillager = (EntityVillager)entity;
            int profession = serverVillager.getProfession();
            ev.setProfession(profession);
            int careerId = Reflector.getFieldValueInt(serverVillager, Reflector.EntityVillager_careerId, 0);
            Reflector.setFieldValueInt(ev, Reflector.EntityVillager_careerId, careerId);
            int careerLevel = Reflector.getFieldValueInt(serverVillager, Reflector.EntityVillager_careerLevel, 0);
            Reflector.setFieldValueInt(ev, Reflector.EntityVillager_careerLevel, careerLevel);
        }
    }

    public static void worldChanged(World oldWorld, World newWorld)
    {
        if (newWorld != null)
        {
            List loadedEntities = newWorld.getLoadedEntityList();

            for (int entityIndex = 0; entityIndex < loadedEntities.size(); ++entityIndex)
            {
                Entity entity = (Entity)loadedEntities.get(entityIndex);
                entityLoaded(entity, newWorld);
            }
        }

        randomEntity.setEntity((Entity)null);
        randomTileEntity.setTileEntity((TileEntity)null);
    }

    public static ResourceLocation getTextureLocation(ResourceLocation loc)
    {
        if (!active)
        {
            return loc;
        }
        else if (working)
        {
            return loc;
        }
        else
        {
            ResourceLocation name;

            try
            {
                working = true;
                IRandomEntity renderedRandomEntity = getRandomEntityRendered();

                if (renderedRandomEntity != null)
                {
                    String resourcePath = loc.getResourcePath();

                    if (resourcePath.startsWith("horse/"))
                    {
                        resourcePath = getHorseTexturePath(resourcePath, "horse/".length());
                    }

                    if (!resourcePath.startsWith("textures/entity/") && !resourcePath.startsWith("textures/painting/"))
                    {
                        ResourceLocation originalLocation = loc;
                        return originalLocation;
                    }

                    RandomEntityProperties properties = (RandomEntityProperties)mapProperties.get(resourcePath);

                    if (properties == null)
                    {
                        ResourceLocation originalLocation = loc;
                        return originalLocation;
                    }

                    ResourceLocation textureLocation = properties.getTextureLocation(loc, renderedRandomEntity);
                    return textureLocation;
                }

                name = loc;
            }
            finally
            {
                working = false;
            }

            return name;
        }
    }

    private static String getHorseTexturePath(String path, int pos)
    {
        if (HORSE_TEXTURES != null && HORSE_TEXTURES_ABBR != null)
        {
            for (int textureIndex = 0; textureIndex < HORSE_TEXTURES_ABBR.length; ++textureIndex)
            {
                String textureAbbr = HORSE_TEXTURES_ABBR[textureIndex];

                if (path.startsWith(textureAbbr, pos))
                {
                    return HORSE_TEXTURES[textureIndex];
                }
            }

            return path;
        }
        else
        {
            return path;
        }
    }

    private static IRandomEntity getRandomEntityRendered()
    {
        if (renderGlobal.renderedEntity != null)
        {
            randomEntity.setEntity(renderGlobal.renderedEntity);
            return randomEntity;
        }
        else
        {
            if (tileEntityRendererDispatcher.tileEntityRendered != null)
            {
                TileEntity renderedTileEntity = tileEntityRendererDispatcher.tileEntityRendered;

                if (renderedTileEntity.getWorld() != null)
                {
                    randomTileEntity.setTileEntity(renderedTileEntity);
                    return randomTileEntity;
                }
            }

            return null;
        }
    }

    private static RandomEntityProperties makeProperties(ResourceLocation loc, boolean mcpatcher)
    {
        String resourcePath = loc.getResourcePath();
        ResourceLocation propertiesLocation = getLocationProperties(loc, mcpatcher);

        if (propertiesLocation != null)
        {
            RandomEntityProperties properties = parseProperties(propertiesLocation, loc);

            if (properties != null)
            {
                return properties;
            }
        }

        ResourceLocation[] variantLocations = getLocationsVariants(loc, mcpatcher);
        return variantLocations == null ? null : new RandomEntityProperties(resourcePath, variantLocations);
    }

    private static RandomEntityProperties parseProperties(ResourceLocation propLoc, ResourceLocation resLoc)
    {
        try
        {
            String propertiesPath = propLoc.getResourcePath();
            dbg(resLoc.getResourcePath() + ", properties: " + propertiesPath);
            InputStream inputStream = Config.getResourceStream(propLoc);

            if (inputStream == null)
            {
                warn("Properties not found: " + propertiesPath);
                return null;
            }
            else
            {
                Properties properties = new PropertiesOrdered();
                properties.load(inputStream);
                inputStream.close();
                RandomEntityProperties randomProperties = new RandomEntityProperties(properties, propertiesPath, resLoc);
                return !randomProperties.isValid(propertiesPath) ? null : randomProperties;
            }
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            warn("File not found: " + resLoc.getResourcePath());
            return null;
        }
        catch (IOException ioException)
        {
            net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
            return null;
        }
    }

    private static ResourceLocation getLocationProperties(ResourceLocation loc, boolean mcpatcher)
    {
        ResourceLocation randomLocation = getLocationRandom(loc, mcpatcher);

        if (randomLocation == null)
        {
            return null;
        }
        else
        {
            String domain = randomLocation.getResourceDomain();
            String randomPath = randomLocation.getResourcePath();
            String pathNoPng = StrUtils.removeSuffix(randomPath, ".png");
            String propertiesPath = pathNoPng + ".properties";
            ResourceLocation propertiesLocation = new ResourceLocation(domain, propertiesPath);

            if (Config.hasResource(propertiesLocation))
            {
                return propertiesLocation;
            }
            else
            {
                String parentPath = getParentTexturePath(pathNoPng);

                if (parentPath == null)
                {
                    return null;
                }
                else
                {
                    ResourceLocation parentPropertiesLocation = new ResourceLocation(domain, parentPath + ".properties");
                    return Config.hasResource(parentPropertiesLocation) ? parentPropertiesLocation : null;
                }
            }
        }
    }

    protected static ResourceLocation getLocationRandom(ResourceLocation loc, boolean mcpatcher)
    {
        String domain = loc.getResourceDomain();
        String path = loc.getResourcePath();
        String sourcePrefix = "textures/";
        String randomPrefix = "optifine/random/";

        if (mcpatcher)
        {
            sourcePrefix = "textures/entity/";
            randomPrefix = "mcpatcher/mob/";
        }

        if (!path.startsWith(sourcePrefix))
        {
            return null;
        }
        else
        {
            String randomPath = StrUtils.replacePrefix(path, sourcePrefix, randomPrefix);
            return new ResourceLocation(domain, randomPath);
        }
    }

    private static String getPathBase(String pathRandom)
    {
        return pathRandom.startsWith("optifine/random/") ? StrUtils.replacePrefix(pathRandom, "optifine/random/", "textures/") : (pathRandom.startsWith("mcpatcher/mob/") ? StrUtils.replacePrefix(pathRandom, "mcpatcher/mob/", "textures/entity/") : null);
    }

    protected static ResourceLocation getLocationIndexed(ResourceLocation loc, int index)
    {
        if (loc == null)
        {
            return null;
        }
        else
        {
            String path = loc.getResourcePath();
            int extensionIndex = path.lastIndexOf(46);

            if (extensionIndex < 0)
            {
                return null;
            }
            else
            {
                String pathPrefix = path.substring(0, extensionIndex);
                String pathSuffix = path.substring(extensionIndex);
                String indexedPath = pathPrefix + index + pathSuffix;
                ResourceLocation indexedLocation = new ResourceLocation(loc.getResourceDomain(), indexedPath);
                return indexedLocation;
            }
        }
    }

    private static String getParentTexturePath(String path)
    {
        for (int suffixIndex = 0; suffixIndex < DEPENDANT_SUFFIXES.length; ++suffixIndex)
        {
            String suffix = DEPENDANT_SUFFIXES[suffixIndex];

            if (path.endsWith(suffix))
            {
                String parentPath = StrUtils.removeSuffix(path, suffix);
                return parentPath;
            }
        }

        return null;
    }

    private static ResourceLocation[] getLocationsVariants(ResourceLocation loc, boolean mcpatcher)
    {
        List variantLocations = new ArrayList();
        variantLocations.add(loc);
        ResourceLocation randomLocation = getLocationRandom(loc, mcpatcher);

        if (randomLocation == null)
        {
            return null;
        }
        else
        {
            for (int variantIndex = 1; variantIndex < ((List)variantLocations).size() + 10; ++variantIndex)
            {
                int textureIndex = variantIndex + 1;
                ResourceLocation variantLocation = getLocationIndexed(randomLocation, textureIndex);

                if (Config.hasResource(variantLocation))
                {
                    variantLocations.add(variantLocation);
                }
            }

            if (variantLocations.size() <= 1)
            {
                return null;
            }
            else
            {
                ResourceLocation[] variantLocationArray = (ResourceLocation[])((ResourceLocation[])variantLocations.toArray(new ResourceLocation[variantLocations.size()]));
                dbg(loc.getResourcePath() + ", variants: " + variantLocationArray.length);
                return variantLocationArray;
            }
        }
    }

    public static void update()
    {
        mapProperties.clear();
        active = false;

        if (Config.isRandomEntities())
        {
            initialize();
        }
    }

    private static void initialize()
    {
        renderGlobal = Config.getRenderGlobal();
        tileEntityRendererDispatcher = TileEntityRendererDispatcher.instance;
        String[] randomPrefixes = new String[] {"optifine/random/", "mcpatcher/mob/"};
        String[] fileSuffixes = new String[] {".png", ".properties"};
        String[] collectedPaths = ResUtils.collectFiles(randomPrefixes, fileSuffixes);
        Set basePaths = new HashSet();

        for (int pathIndex = 0; pathIndex < collectedPaths.length; ++pathIndex)
        {
            String path = collectedPaths[pathIndex];
            path = StrUtils.removeSuffix(path, fileSuffixes);
            path = StrUtils.trimTrailing(path, "0123456789");
            path = path + ".png";
            String basePath = getPathBase(path);

            if (!basePaths.contains(basePath))
            {
                basePaths.add(basePath);
                ResourceLocation baseLocation = new ResourceLocation(basePath);

                if (Config.hasResource(baseLocation))
                {
                    RandomEntityProperties properties = (RandomEntityProperties)mapProperties.get(basePath);

                    if (properties == null)
                    {
                        properties = makeProperties(baseLocation, false);

                        if (properties == null)
                        {
                            properties = makeProperties(baseLocation, true);
                        }

                        if (properties != null)
                        {
                            mapProperties.put(basePath, properties);
                        }
                    }
                }
            }
        }

        active = !mapProperties.isEmpty();
    }

    public static void dbg(String str)
    {
        Config.dbg("RandomEntities: " + str);
    }

    public static void warn(String str)
    {
        Config.warn("RandomEntities: " + str);
    }
}
