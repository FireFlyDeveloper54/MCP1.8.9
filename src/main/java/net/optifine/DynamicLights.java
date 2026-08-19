package net.optifine;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.optifine.config.ConnectedParser;
import net.optifine.config.EntityClassLocator;
import net.optifine.config.IObjectLocator;
import net.optifine.config.ItemLocator;
import net.optifine.util.PropertiesOrdered;

public class DynamicLights
{
    private static DynamicLightsMap mapDynamicLights = new DynamicLightsMap();
    private static Map<Class, Integer> mapEntityLightLevels = new HashMap();
    private static Map<Item, Integer> mapItemLightLevels = new HashMap();
    private static long timeUpdateMs = 0L;
    private static final double MAX_DIST = 7.5D;
    private static final double MAX_DIST_SQ = 56.25D;
    private static final int LIGHT_LEVEL_MAX = 15;
    private static final int LIGHT_LEVEL_FIRE = 15;
    private static final int LIGHT_LEVEL_BLAZE = 10;
    private static final int LIGHT_LEVEL_MAGMA_CUBE = 8;
    private static final int LIGHT_LEVEL_MAGMA_CUBE_CORE = 13;
    private static final int LIGHT_LEVEL_GLOWSTONE_DUST = 8;
    private static final int LIGHT_LEVEL_PRISMARINE_CRYSTALS = 8;
    private static boolean initialized;

    public static void entityAdded(Entity entityIn, RenderGlobal renderGlobal)
    {
    }

    public static void entityRemoved(Entity entityIn, RenderGlobal renderGlobal)
    {
        synchronized (mapDynamicLights)
        {
            DynamicLight dynamicLight = mapDynamicLights.remove(entityIn.getEntityId());

            if (dynamicLight != null)
            {
                dynamicLight.updateLitChunks(renderGlobal);
            }
        }
    }

    public static void update(RenderGlobal renderGlobal)
    {
        long currentTimeMs = System.currentTimeMillis();

        if (currentTimeMs >= timeUpdateMs + 50L)
        {
            timeUpdateMs = currentTimeMs;

            if (!initialized)
            {
                initialize();
            }

            synchronized (mapDynamicLights)
            {
                updateMapDynamicLights(renderGlobal);

                if (mapDynamicLights.size() > 0)
                {
                    List<DynamicLight> dynamicLights = mapDynamicLights.valueList();

                    for (int lightIndex = 0; lightIndex < dynamicLights.size(); ++lightIndex)
                    {
                        DynamicLight dynamicLight = (DynamicLight)dynamicLights.get(lightIndex);
                        dynamicLight.update(renderGlobal);
                    }
                }
            }
        }
    }

    private static void initialize()
    {
        initialized = true;
        mapEntityLightLevels.clear();
        mapItemLightLevels.clear();
        String[] modIds = new String[0];

        for (int modIndex = 0; modIndex < modIds.length; ++modIndex)
        {
            String modId = modIds[modIndex];

            try
            {
                ResourceLocation resourceLocation = new ResourceLocation(modId, "optifine/dynamic_lights.properties");
                InputStream inputStream = Config.getResourceStream(resourceLocation);
                loadModConfiguration(inputStream, resourceLocation.toString(), modId);
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }

        if (mapEntityLightLevels.size() > 0)
        {
            Config.dbg("DynamicLights entities: " + mapEntityLightLevels.size());
        }

        if (mapItemLightLevels.size() > 0)
        {
            Config.dbg("DynamicLights items: " + mapItemLightLevels.size());
        }
    }

    private static void loadModConfiguration(InputStream in, String path, String modId)
    {
        if (in != null)
        {
            try
            {
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                Config.dbg("DynamicLights: Parsing " + path);
                ConnectedParser connectedParser = new ConnectedParser("DynamicLights");
                loadModLightLevels(properties.getProperty("entities"), mapEntityLightLevels, new EntityClassLocator(), connectedParser, path, modId);
                loadModLightLevels(properties.getProperty("items"), mapItemLightLevels, new ItemLocator(), connectedParser, path, modId);
            }
            catch (IOException caughtIoException)
            {
                Config.warn("DynamicLights: Error reading " + path);
            }
        }
    }

    private static void loadModLightLevels(String prop, Map mapLightLevels, IObjectLocator ol, ConnectedParser cp, String path, String modId)
    {
        if (prop != null)
        {
            String[] lightLevelEntries = Config.tokenize(prop, " ");

            for (int entryIndex = 0; entryIndex < lightLevelEntries.length; ++entryIndex)
            {
                String entry = lightLevelEntries[entryIndex];
                String[] entryParts = Config.tokenize(entry, ":");

                if (entryParts.length != 2)
                {
                    cp.warn("Invalid entry: " + entry + ", in:" + path);
                }
                else
                {
                    String objectName = entryParts[0];
                    String lightLevelText = entryParts[1];
                    String objectResourceName = modId + ":" + objectName;
                    ResourceLocation objectLocation = new ResourceLocation(objectResourceName);
                    Object object = ol.getObject(objectLocation);

                    if (object == null)
                    {
                        cp.warn("Object not found: " + objectResourceName);
                    }
                    else
                    {
                        int lightLevel = cp.parseInt(lightLevelText, -1);

                        if (lightLevel >= 0 && lightLevel <= 15)
                        {
                            mapLightLevels.put(object, Integer.valueOf(lightLevel));
                        }
                        else
                        {
                            cp.warn("Invalid light level: " + entry);
                        }
                    }
                }
            }
        }
    }

    private static void updateMapDynamicLights(RenderGlobal renderGlobal)
    {
        World world = renderGlobal.getWorld();

        if (world != null)
        {
            for (Entity entity : world.getLoadedEntityList())
            {
                int lightLevel = getLightLevel(entity);

                if (lightLevel > 0)
                {
                    int entityId = entity.getEntityId();
                    DynamicLight dynamicLight = mapDynamicLights.get(entityId);

                    if (dynamicLight == null)
                    {
                        dynamicLight = new DynamicLight(entity);
                        mapDynamicLights.put(entityId, dynamicLight);
                    }
                }
                else
                {
                    int entityId = entity.getEntityId();
                    DynamicLight dynamicLight = mapDynamicLights.remove(entityId);

                    if (dynamicLight != null)
                    {
                        dynamicLight.updateLitChunks(renderGlobal);
                    }
                }
            }
        }
    }

    public static int getCombinedLight(BlockPos pos, int combinedLight)
    {
        double dynamicLightLevel = getLightLevel(pos);
        combinedLight = getCombinedLight(dynamicLightLevel, combinedLight);
        return combinedLight;
    }

    public static int getCombinedLight(Entity entity, int combinedLight)
    {
        double dynamicLightLevel = (double)getLightLevel(entity);
        combinedLight = getCombinedLight(dynamicLightLevel, combinedLight);
        return combinedLight;
    }

    public static int getCombinedLight(double lightPlayer, int combinedLight)
    {
        if (lightPlayer > 0.0D)
        {
            int dynamicLightValue = (int)(lightPlayer * 16.0D);
            int blockLightValue = combinedLight & 255;

            if (dynamicLightValue > blockLightValue)
            {
                combinedLight = combinedLight & -256;
                combinedLight = combinedLight | dynamicLightValue;
            }
        }

        return combinedLight;
    }

    public static double getLightLevel(BlockPos pos)
    {
        double maxLightLevel = 0.0D;

        synchronized (mapDynamicLights)
        {
            List<DynamicLight> dynamicLights = mapDynamicLights.valueList();
            int dynamicLightCount = dynamicLights.size();

            for (int index = 0; index < dynamicLightCount; ++index)
            {
                DynamicLight dynamicLight = (DynamicLight)dynamicLights.get(index);
                int lightLevel = dynamicLight.getLastLightLevel();

                if (lightLevel > 0)
                {
                    double lightX = dynamicLight.getLastPosX();
                    double lightY = dynamicLight.getLastPosY();
                    double lightZ = dynamicLight.getLastPosZ();
                    double deltaX = (double)pos.getX() - lightX;
                    double deltaY = (double)pos.getY() - lightY;
                    double deltaZ = (double)pos.getZ() - lightZ;
                    double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

                    if (dynamicLight.isUnderwater() && !Config.isClearWater())
                    {
                        lightLevel = Config.limit(lightLevel - 2, 0, 15);
                        distanceSq *= 2.0D;
                    }

                    if (distanceSq <= 56.25D)
                    {
                        double distance = MathHelper.fastSqrt_double(distanceSq);
                        double falloff = 1.0D - distance / 7.5D;
                        double lightAtPos = falloff * (double)lightLevel;

                        if (lightAtPos > maxLightLevel)
                        {
                            maxLightLevel = lightAtPos;
                        }
                    }
                }
            }
        }

        double clampedLightLevel = Config.limit(maxLightLevel, 0.0D, 15.0D);
        return clampedLightLevel;
    }

    public static int getLightLevel(ItemStack itemStack)
    {
        if (itemStack == null)
        {
            return 0;
        }
        else
        {
            Item item = itemStack.getItem();

            if (item instanceof ItemBlock)
            {
                ItemBlock itemBlock = (ItemBlock)item;
                Block block = itemBlock.getBlock();

                if (block != null)
                {
                    return block.getLightValue();
                }
            }

            if (item == Items.lava_bucket)
            {
                return Blocks.lava.getLightValue();
            }
            else if (item != Items.blaze_rod && item != Items.blaze_powder)
            {
                if (item == Items.glowstone_dust)
                {
                    return 8;
                }
                else if (item == Items.prismarine_crystals)
                {
                    return 8;
                }
                else if (item == Items.magma_cream)
                {
                    return 8;
                }
                else if (item == Items.nether_star)
                {
                    return Blocks.beacon.getLightValue() / 2;
                }
                else
                {
                    if (!mapItemLightLevels.isEmpty())
                    {
                        Integer integer = (Integer)mapItemLightLevels.get(item);

                        if (integer != null)
                        {
                            return integer.intValue();
                        }
                    }

                    return 0;
                }
            }
            else
            {
                return 10;
            }
        }
    }

    public static int getLightLevel(Entity entity)
    {
        if (entity == Config.getMinecraft().getRenderViewEntity() && !Config.isDynamicHandLight())
        {
            return 0;
        }
        else
        {
            if (entity instanceof EntityPlayer)
            {
                EntityPlayer entityPlayer = (EntityPlayer)entity;

                if (entityPlayer.isSpectator())
                {
                    return 0;
                }
            }

            if (entity.isBurning())
            {
                return 15;
            }
            else
            {
                if (!mapEntityLightLevels.isEmpty())
                {
                    Integer configuredLightLevel = (Integer)mapEntityLightLevels.get(entity.getClass());

                    if (configuredLightLevel != null)
                    {
                        return configuredLightLevel.intValue();
                    }
                }

                if (entity instanceof EntityFireball)
                {
                    return 15;
                }
                else if (entity instanceof EntityTNTPrimed)
                {
                    return 15;
                }
                else if (entity instanceof EntityBlaze)
                {
                    EntityBlaze entityBlaze = (EntityBlaze)entity;
                    return entityBlaze.isCharged() ? 15 : 10;
                }
                else if (entity instanceof EntityMagmaCube)
                {
                    EntityMagmaCube entityMagmaCube = (EntityMagmaCube)entity;
                    return (double)entityMagmaCube.squishFactor > 0.6D ? 13 : 8;
                }
                else
                {
                    if (entity instanceof EntityCreeper)
                    {
                        EntityCreeper entityCreeper = (EntityCreeper)entity;

                        if ((double)entityCreeper.getCreeperFlashIntensity(0.0F) > 0.001D)
                        {
                            return 15;
                        }
                    }

                    if (entity instanceof EntityLivingBase)
                    {
                        EntityLivingBase livingEntity = (EntityLivingBase)entity;
                        ItemStack heldItemStack = livingEntity.getHeldItem();
                        int heldLightLevel = getLightLevel(heldItemStack);
                        ItemStack helmetItemStack = livingEntity.getEquipmentInSlot(4);
                        int helmetLightLevel = getLightLevel(helmetItemStack);
                        return Math.max(heldLightLevel, helmetLightLevel);
                    }
                    else if (entity instanceof EntityItem)
                    {
                        EntityItem entityItem = (EntityItem)entity;
                        ItemStack itemStack = getItemStack(entityItem);
                        return getLightLevel(itemStack);
                    }
                    else
                    {
                        return 0;
                    }
                }
            }
        }
    }

    public static void removeLights(RenderGlobal renderGlobal)
    {
        synchronized (mapDynamicLights)
        {
            List<DynamicLight> dynamicLights = mapDynamicLights.valueList();

            for (int lightIndex = 0; lightIndex < dynamicLights.size(); ++lightIndex)
            {
                DynamicLight dynamicLight = (DynamicLight)dynamicLights.get(lightIndex);
                dynamicLight.updateLitChunks(renderGlobal);
            }

            mapDynamicLights.clear();
        }
    }

    public static void clear()
    {
        synchronized (mapDynamicLights)
        {
            mapDynamicLights.clear();
        }
    }

    public static int getCount()
    {
        synchronized (mapDynamicLights)
        {
            return mapDynamicLights.size();
        }
    }

    public static ItemStack getItemStack(EntityItem entityItem)
    {
        ItemStack itemStack = entityItem.getDataWatcher().getWatchableObjectItemStack(10);
        return itemStack;
    }
}
