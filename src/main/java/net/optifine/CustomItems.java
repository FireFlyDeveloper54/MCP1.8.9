package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.config.NbtTagValue;
import net.optifine.render.Blender;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;

public class CustomItems
{
    private static CustomItemProperties[][] itemProperties = (CustomItemProperties[][])null;
    private static CustomItemProperties[][] enchantmentProperties = (CustomItemProperties[][])null;
    private static Map mapPotionIds = null;
    private static ItemModelGenerator itemModelGenerator = new ItemModelGenerator();
    private static boolean useGlint = true;
    private static boolean renderOffHand = false;
    public static final int MASK_POTION_SPLASH = 16384;
    public static final int MASK_POTION_NAME = 63;
    public static final int MASK_POTION_EXTENDED = 64;
    public static final String KEY_TEXTURE_OVERLAY = "texture.potion_overlay";
    public static final String KEY_TEXTURE_SPLASH = "texture.potion_bottle_splash";
    public static final String KEY_TEXTURE_DRINKABLE = "texture.potion_bottle_drinkable";
    public static final String DEFAULT_TEXTURE_OVERLAY = "items/potion_overlay";
    public static final String DEFAULT_TEXTURE_SPLASH = "items/potion_bottle_splash";
    public static final String DEFAULT_TEXTURE_DRINKABLE = "items/potion_bottle_drinkable";
    private static final int[][] EMPTY_INT2_ARRAY = new int[0][];
    private static final String TYPE_POTION_NORMAL = "normal";
    private static final String TYPE_POTION_SPLASH = "splash";
    private static final String TYPE_POTION_LINGER = "linger";

    public static void update()
    {
        itemProperties = (CustomItemProperties[][])null;
        enchantmentProperties = (CustomItemProperties[][])null;
        useGlint = true;

        if (Config.isCustomItems())
        {
            readCitProperties("mcpatcher/cit.properties");
            IResourcePack[] resourcePacks = Config.getResourcePacks();

            for (int packIndex = resourcePacks.length - 1; packIndex >= 0; --packIndex)
            {
                IResourcePack resourcePack = resourcePacks[packIndex];
                update(resourcePack);
            }

            update(Config.getDefaultResourcePack());

            if (itemProperties.length <= 0)
            {
                itemProperties = (CustomItemProperties[][])null;
            }

            if (enchantmentProperties.length <= 0)
            {
                enchantmentProperties = (CustomItemProperties[][])null;
            }
        }
    }

    private static void readCitProperties(String fileName)
    {
        try
        {
            ResourceLocation propertiesLocation = new ResourceLocation(fileName);
            InputStream inputStream = Config.getResourceStream(propertiesLocation);

            if (inputStream == null)
            {
                return;
            }

            Config.dbg("CustomItems: Loading " + fileName);
            Properties properties = new PropertiesOrdered();
            properties.load(inputStream);
            inputStream.close();
            useGlint = Config.parseBoolean(properties.getProperty("useGlint"), true);
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            return;
        }
        catch (IOException ioException)
        {
            net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
        }
    }

    private static void update(IResourcePack rp)
    {
        String[] propertyPaths = ResUtils.collectFiles(rp, (String)"mcpatcher/cit/", (String)".properties", (String[])null);
        Map autoProperties = makeAutoImageProperties(rp);

        if (autoProperties.size() > 0)
        {
            Set propertyPathSet = autoProperties.keySet();
            String[] autoPropertyPaths = (String[])((String[])propertyPathSet.toArray(new String[propertyPathSet.size()]));
            propertyPaths = (String[])((String[])Config.addObjectsToArray(propertyPaths, autoPropertyPaths));
        }

        Arrays.sort((Object[])propertyPaths);
        List itemList = makePropertyList(itemProperties);
        List enchantmentList = makePropertyList(enchantmentProperties);

        for (int pathIndex = 0; pathIndex < propertyPaths.length; ++pathIndex)
        {
            String propertyPath = propertyPaths[pathIndex];
            Config.dbg("CustomItems: " + propertyPath);

            try
            {
                CustomItemProperties properties = null;

                if (autoProperties.containsKey(propertyPath))
                {
                    properties = (CustomItemProperties)autoProperties.get(propertyPath);
                }

                if (properties == null)
                {
                    ResourceLocation propertyLocation = new ResourceLocation(propertyPath);
                    InputStream inputStream = rp.getInputStream(propertyLocation);

                    if (inputStream == null)
                    {
                        Config.warn("CustomItems file not found: " + propertyPath);
                        continue;
                    }

                    Properties propertyValues = new PropertiesOrdered();
                    propertyValues.load(inputStream);
                    inputStream.close();
                    properties = new CustomItemProperties(propertyValues, propertyPath);
                }

                if (properties.isValid(propertyPath))
                {
                    addToItemList(properties, itemList);
                    addToEnchantmentList(properties, enchantmentList);
                }
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                Config.warn("CustomItems file not found: " + propertyPath);
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }

        itemProperties = propertyListToArray(itemList);
        enchantmentProperties = propertyListToArray(enchantmentList);
        Comparator comparator = getPropertiesComparator();

        for (int itemIndex = 0; itemIndex < itemProperties.length; ++itemIndex)
        {
            CustomItemProperties[] propertiesArray = itemProperties[itemIndex];

            if (propertiesArray != null)
            {
                Arrays.sort(propertiesArray, comparator);
            }
        }

        for (int enchantmentIndex = 0; enchantmentIndex < enchantmentProperties.length; ++enchantmentIndex)
        {
            CustomItemProperties[] propertiesArray = enchantmentProperties[enchantmentIndex];

            if (propertiesArray != null)
            {
                Arrays.sort(propertiesArray, comparator);
            }
        }
    }

    private static Comparator getPropertiesComparator()
    {
        Comparator comparator = new Comparator()
        {
            public int compare(Object leftObject, Object rightObject)
            {
                CustomItemProperties left = (CustomItemProperties)leftObject;
                CustomItemProperties right = (CustomItemProperties)rightObject;
                return left.layer != right.layer ? left.layer - right.layer : (left.weight != right.weight ? right.weight - left.weight : (!left.basePath.equals(right.basePath) ? left.basePath.compareTo(right.basePath) : left.name.compareTo(right.name)));
            }
        };
        return comparator;
    }

    public static void updateIcons(TextureMap textureMap)
    {
        for (CustomItemProperties properties : getAllProperties())
        {
            properties.updateIcons(textureMap);
        }
    }

    public static void loadModels(ModelBakery modelBakery)
    {
        for (CustomItemProperties properties : getAllProperties())
        {
            properties.loadModels(modelBakery);
        }
    }

    public static void updateModels()
    {
        for (CustomItemProperties properties : getAllProperties())
        {
            if (properties.type == 1)
            {
                TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
                properties.updateModelTexture(textureMap, itemModelGenerator);
                properties.updateModelsFull();
            }
        }
    }

    private static List<CustomItemProperties> getAllProperties()
    {
        List<CustomItemProperties> propertiesList = new ArrayList();
        addAll(itemProperties, propertiesList);
        addAll(enchantmentProperties, propertiesList);
        return propertiesList;
    }

    private static void addAll(CustomItemProperties[][] cipsArr, List<CustomItemProperties> list)
    {
        if (cipsArr != null)
        {
            for (int propertiesIndex = 0; propertiesIndex < cipsArr.length; ++propertiesIndex)
            {
                CustomItemProperties[] propertiesArray = cipsArr[propertiesIndex];

                if (propertiesArray != null)
                {
                    for (int propertyIndex = 0; propertyIndex < propertiesArray.length; ++propertyIndex)
                    {
                        CustomItemProperties properties = propertiesArray[propertyIndex];

                        if (properties != null)
                        {
                            list.add(properties);
                        }
                    }
                }
            }
        }
    }

    private static Map makeAutoImageProperties(IResourcePack rp)
    {
        Map propertiesByPath = new HashMap();
        propertiesByPath.putAll(makePotionImageProperties(rp, "normal", Item.getIdFromItem(Items.potionitem)));
        propertiesByPath.putAll(makePotionImageProperties(rp, "splash", Item.getIdFromItem(Items.potionitem)));
        propertiesByPath.putAll(makePotionImageProperties(rp, "linger", Item.getIdFromItem(Items.potionitem)));
        return propertiesByPath;
    }

    private static Map makePotionImageProperties(IResourcePack rp, String type, int itemId)
    {
        Map propertiesByPath = new HashMap();
        String typePrefix = type + "/";
        String[] searchPrefixes = new String[] {"mcpatcher/cit/potion/" + typePrefix, "mcpatcher/cit/Potion/" + typePrefix};
        String[] suffixes = new String[] {".png"};
        String[] imagePaths = ResUtils.collectFiles(rp, searchPrefixes, suffixes);

        for (int pathIndex = 0; pathIndex < imagePaths.length; ++pathIndex)
        {
            String imagePath = imagePaths[pathIndex];
            String name = StrUtils.removePrefixSuffix(imagePath, searchPrefixes, suffixes);
            Properties properties = makePotionProperties(name, type, itemId, imagePath);

            if (properties != null)
            {
                String propertyPath = StrUtils.removeSuffix(imagePath, suffixes) + ".properties";
                CustomItemProperties customProperties = new CustomItemProperties(properties, propertyPath);
                propertiesByPath.put(propertyPath, customProperties);
            }
        }

        return propertiesByPath;
    }

    private static Properties makePotionProperties(String name, String type, int itemId, String path)
    {
        if (StrUtils.endsWith(name, new String[] {"_n", "_s"}))
        {
            return null;
        }
        else if (name.equals("empty") && type.equals("normal"))
        {
            itemId = Item.getIdFromItem(Items.glass_bottle);
            Properties properties = new PropertiesOrdered();
            properties.put("type", "item");
            properties.put("items", "" + itemId);
            return properties;
        }
        else
        {
            int[] potionIds = (int[])((int[])getMapPotionIds().get(name));

            if (potionIds == null)
            {
                Config.warn("Potion not found for image: " + path);
                return null;
            }
            else
            {
                StringBuffer damageBuffer = new StringBuffer();

                for (int potionIndex = 0; potionIndex < potionIds.length; ++potionIndex)
                {
                    int damage = potionIds[potionIndex];

                    if (type.equals("splash"))
                    {
                        damage |= 16384;
                    }

                    if (potionIndex > 0)
                    {
                        damageBuffer.append(" ");
                    }

                    damageBuffer.append(damage);
                }

                int damageMask = 16447;

                if (name.equals("water") || name.equals("mundane"))
                {
                    damageMask |= 64;
                }

                Properties generatedProperties = new PropertiesOrdered();
                generatedProperties.put("type", "item");
                generatedProperties.put("items", "" + itemId);
                generatedProperties.put("damage", "" + damageBuffer.toString());
                generatedProperties.put("damageMask", "" + damageMask);

                if (type.equals("splash"))
                {
                    generatedProperties.put("texture.potion_bottle_splash", name);
                }
                else
                {
                    generatedProperties.put("texture.potion_bottle_drinkable", name);
                }

                return generatedProperties;
            }
        }
    }

    private static Map getMapPotionIds()
    {
        if (mapPotionIds == null)
        {
            mapPotionIds = new LinkedHashMap();
            mapPotionIds.put("water", getPotionId(0, 0));
            mapPotionIds.put("awkward", getPotionId(0, 1));
            mapPotionIds.put("thick", getPotionId(0, 2));
            mapPotionIds.put("potent", getPotionId(0, 3));
            mapPotionIds.put("regeneration", getPotionIds(1));
            mapPotionIds.put("movespeed", getPotionIds(2));
            mapPotionIds.put("fireresistance", getPotionIds(3));
            mapPotionIds.put("poison", getPotionIds(4));
            mapPotionIds.put("heal", getPotionIds(5));
            mapPotionIds.put("nightvision", getPotionIds(6));
            mapPotionIds.put("clear", getPotionId(7, 0));
            mapPotionIds.put("bungling", getPotionId(7, 1));
            mapPotionIds.put("charming", getPotionId(7, 2));
            mapPotionIds.put("rank", getPotionId(7, 3));
            mapPotionIds.put("weakness", getPotionIds(8));
            mapPotionIds.put("damageboost", getPotionIds(9));
            mapPotionIds.put("moveslowdown", getPotionIds(10));
            mapPotionIds.put("leaping", getPotionIds(11));
            mapPotionIds.put("harm", getPotionIds(12));
            mapPotionIds.put("waterbreathing", getPotionIds(13));
            mapPotionIds.put("invisibility", getPotionIds(14));
            mapPotionIds.put("thin", getPotionId(15, 0));
            mapPotionIds.put("debonair", getPotionId(15, 1));
            mapPotionIds.put("sparkling", getPotionId(15, 2));
            mapPotionIds.put("stinky", getPotionId(15, 3));
            mapPotionIds.put("mundane", getPotionId(0, 4));
            mapPotionIds.put("speed", mapPotionIds.get("movespeed"));
            mapPotionIds.put("fire_resistance", mapPotionIds.get("fireresistance"));
            mapPotionIds.put("instant_health", mapPotionIds.get("heal"));
            mapPotionIds.put("night_vision", mapPotionIds.get("nightvision"));
            mapPotionIds.put("strength", mapPotionIds.get("damageboost"));
            mapPotionIds.put("slowness", mapPotionIds.get("moveslowdown"));
            mapPotionIds.put("instant_damage", mapPotionIds.get("harm"));
            mapPotionIds.put("water_breathing", mapPotionIds.get("waterbreathing"));
        }

        return mapPotionIds;
    }

    private static int[] getPotionIds(int baseId)
    {
        return new int[] {baseId, baseId + 16, baseId + 32, baseId + 48};
    }

    private static int[] getPotionId(int baseId, int subId)
    {
        return new int[] {baseId + subId * 16};
    }

    private static int getPotionNameDamage(String name)
    {
        String potionName = "potion." + name;
        Potion[] potions = Potion.potionTypes;

        for (int potionIndex = 0; potionIndex < potions.length; ++potionIndex)
        {
            Potion potion = potions[potionIndex];

            if (potion != null)
            {
                String currentPotionName = potion.getName();

                if (potionName.equals(currentPotionName))
                {
                    return potion.getId();
                }
            }
        }

        return -1;
    }

    private static List makePropertyList(CustomItemProperties[][] propsArr)
    {
        List propertyLists = new ArrayList();

        if (propsArr != null)
        {
            for (int propertyIndex = 0; propertyIndex < propsArr.length; ++propertyIndex)
            {
                CustomItemProperties[] propertiesArray = propsArr[propertyIndex];
                List propertyList = null;

                if (propertiesArray != null)
                {
                    propertyList = new ArrayList(Arrays.asList(propertiesArray));
                }

                propertyLists.add(propertyList);
            }
        }

        return propertyLists;
    }

    private static CustomItemProperties[][] propertyListToArray(List lists)
    {
        CustomItemProperties[][] propertyArrays = new CustomItemProperties[lists.size()][];

        for (int listIndex = 0; listIndex < lists.size(); ++listIndex)
        {
            List propertyList = (List)lists.get(listIndex);

            if (propertyList != null)
            {
                CustomItemProperties[] propertiesArray = (CustomItemProperties[])((CustomItemProperties[])propertyList.toArray(new CustomItemProperties[propertyList.size()]));
                Arrays.sort(propertiesArray, new CustomItemsComparator());
                propertyArrays[listIndex] = propertiesArray;
            }
        }

        return propertyArrays;
    }

    private static void addToItemList(CustomItemProperties cp, List itemList)
    {
        if (cp.items != null)
        {
            for (int itemIndex = 0; itemIndex < cp.items.length; ++itemIndex)
            {
                int itemId = cp.items[itemIndex];

                if (itemId <= 0)
                {
                    Config.warn("Invalid item ID: " + itemId);
                }
                else
                {
                    addToList(cp, itemList, itemId);
                }
            }
        }
    }

    private static void addToEnchantmentList(CustomItemProperties cp, List enchantmentList)
    {
        if (cp.type == 2)
        {
            if (cp.enchantmentIds != null)
            {
                for (int enchantmentId = 0; enchantmentId < 256; ++enchantmentId)
                {
                    if (cp.enchantmentIds.isInRange(enchantmentId))
                    {
                        addToList(cp, enchantmentList, enchantmentId);
                    }
                }
            }
        }
    }

    private static void addToList(CustomItemProperties cp, List lists, int id)
    {
        while (id >= lists.size())
        {
            lists.add(null);
        }

        List propertyList = (List)lists.get(id);

        if (propertyList == null)
        {
            propertyList = new ArrayList();
            lists.set(id, propertyList);
        }

        propertyList.add(cp);
    }

    public static IBakedModel getCustomItemModel(ItemStack itemStack, IBakedModel model, ResourceLocation modelLocation, boolean fullModel)
    {
        if (!fullModel && model.isGui3d())
        {
            return model;
        }
        else if (itemProperties == null)
        {
            return model;
        }
        else
        {
            CustomItemProperties properties = getCustomItemProperties(itemStack, 1);

            if (properties == null)
            {
                return model;
            }
            else
            {
                IBakedModel bakedModel = properties.getBakedModel(modelLocation, fullModel);
                return bakedModel != null ? bakedModel : model;
            }
        }
    }

    public static boolean bindCustomArmorTexture(ItemStack itemStack, int layer, String overlay)
    {
        if (itemProperties == null)
        {
            return false;
        }
        else
        {
            ResourceLocation armorLocation = getCustomArmorLocation(itemStack, layer, overlay);

            if (armorLocation == null)
            {
                return false;
            }
            else
            {
                Config.getTextureManager().bindTexture(armorLocation);
                return true;
            }
        }
    }

    private static ResourceLocation getCustomArmorLocation(ItemStack itemStack, int layer, String overlay)
    {
        CustomItemProperties properties = getCustomItemProperties(itemStack, 3);

        if (properties == null)
        {
            return null;
        }
        else if (properties.mapTextureLocations == null)
        {
            return properties.textureLocation;
        }
        else
        {
            Item stackItem = itemStack.getItem();

            if (!(stackItem instanceof ItemArmor))
            {
                return null;
            }
            else
            {
                ItemArmor itemArmor = (ItemArmor)stackItem;
                String materialName = itemArmor.getArmorMaterial().getName();
                StringBuffer textureKeyBuffer = new StringBuffer();
                textureKeyBuffer.append("texture.");
                textureKeyBuffer.append(materialName);
                textureKeyBuffer.append("_layer_");
                textureKeyBuffer.append(layer);

                if (overlay != null)
                {
                    textureKeyBuffer.append("_");
                    textureKeyBuffer.append(overlay);
                }

                String textureKey = textureKeyBuffer.toString();
                ResourceLocation armorLocation = (ResourceLocation)properties.mapTextureLocations.get(textureKey);
                return armorLocation == null ? properties.textureLocation : armorLocation;
            }
        }
    }

    private static CustomItemProperties getCustomItemProperties(ItemStack itemStack, int type)
    {
        if (itemProperties == null)
        {
            return null;
        }
        else if (itemStack == null)
        {
            return null;
        }
        else
        {
            Item stackItem = itemStack.getItem();
            int itemId = Item.getIdFromItem(stackItem);

            if (itemId >= 0 && itemId < itemProperties.length)
            {
                CustomItemProperties[] propertiesArray = itemProperties[itemId];

                if (propertiesArray != null)
                {
                    for (int propertyIndex = 0; propertyIndex < propertiesArray.length; ++propertyIndex)
                    {
                        CustomItemProperties properties = propertiesArray[propertyIndex];

                        if (properties.type == type && matchesProperties(properties, itemStack, (int[][])null))
                        {
                            return properties;
                        }
                    }
                }
            }

            return null;
        }
    }

    private static boolean matchesProperties(CustomItemProperties cip, ItemStack itemStack, int[][] enchantmentIdLevels)
    {
        Item stackItem = itemStack.getItem();

        if (cip.damage != null)
        {
            int damage = itemStack.getItemDamage();

            if (cip.damageMask != 0)
            {
                damage &= cip.damageMask;
            }

            if (cip.damagePercent)
            {
                int maxDamage = stackItem.getMaxDamage();
                damage = (int)((double)(damage * 100) / (double)maxDamage);
            }

            if (!cip.damage.isInRange(damage))
            {
                return false;
            }
        }

        if (cip.stackSize != null && !cip.stackSize.isInRange(itemStack.stackSize))
        {
            return false;
        }
        else
        {
            int[][] enchantments = enchantmentIdLevels;

            if (cip.enchantmentIds != null)
            {
                if (enchantmentIdLevels == null)
                {
                    enchantments = getEnchantmentIdLevels(itemStack);
                }

                boolean hasMatchingEnchantmentId = false;

                for (int enchantmentIndex = 0; enchantmentIndex < enchantments.length; ++enchantmentIndex)
                {
                    int enchantmentId = enchantments[enchantmentIndex][0];

                    if (cip.enchantmentIds.isInRange(enchantmentId))
                    {
                        hasMatchingEnchantmentId = true;
                        break;
                    }
                }

                if (!hasMatchingEnchantmentId)
                {
                    return false;
                }
            }

            if (cip.enchantmentLevels != null)
            {
                if (enchantments == null)
                {
                    enchantments = getEnchantmentIdLevels(itemStack);
                }

                boolean hasMatchingEnchantmentLevel = false;

                for (int enchantmentIndex = 0; enchantmentIndex < enchantments.length; ++enchantmentIndex)
                {
                    int enchantmentLevel = enchantments[enchantmentIndex][1];

                    if (cip.enchantmentLevels.isInRange(enchantmentLevel))
                    {
                        hasMatchingEnchantmentLevel = true;
                        break;
                    }
                }

                if (!hasMatchingEnchantmentLevel)
                {
                    return false;
                }
            }

            if (cip.nbtTagValues != null)
            {
                NBTTagCompound tagCompound = itemStack.getTagCompound();

                for (int tagIndex = 0; tagIndex < cip.nbtTagValues.length; ++tagIndex)
                {
                    NbtTagValue tagValue = cip.nbtTagValues[tagIndex];

                    if (!tagValue.matches(tagCompound))
                    {
                        return false;
                    }
                }
            }

            if (cip.hand != 0)
            {
                if (cip.hand == 1 && renderOffHand)
                {
                    return false;
                }

                if (cip.hand == 2 && !renderOffHand)
                {
                    return false;
                }
            }

            return true;
        }
    }

    private static int[][] getEnchantmentIdLevels(ItemStack itemStack)
    {
        Item stackItem = itemStack.getItem();
        NBTTagList enchantmentList = stackItem == Items.enchanted_book ? Items.enchanted_book.getEnchantments(itemStack) : itemStack.getEnchantmentTagList();

        if (enchantmentList != null && enchantmentList.tagCount() > 0)
        {
            int[][] enchantments = new int[enchantmentList.tagCount()][2];

            for (int tagIndex = 0; tagIndex < enchantmentList.tagCount(); ++tagIndex)
            {
                NBTTagCompound tagCompound = enchantmentList.getCompoundTagAt(tagIndex);
                int enchantmentId = tagCompound.getShort("id");
                int enchantmentLevel = tagCompound.getShort("lvl");
                enchantments[tagIndex][0] = enchantmentId;
                enchantments[tagIndex][1] = enchantmentLevel;
            }

            return enchantments;
        }
        else
        {
            return EMPTY_INT2_ARRAY;
        }
    }

    private static boolean isEnchantmentIdProcessed(int[][] enchantments, int currentIndex, int enchantmentId)
    {
        for (int previousIndex = 0; previousIndex < currentIndex; ++previousIndex)
        {
            int previousEnchantmentId = enchantments[previousIndex][0];

            if (previousEnchantmentId == enchantmentId && previousEnchantmentId >= 0 && previousEnchantmentId < enchantmentProperties.length)
            {
                CustomItemProperties[] previousProperties = enchantmentProperties[previousEnchantmentId];

                if (previousProperties != null && previousProperties.length > 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean renderCustomEffect(RenderItem renderItem, ItemStack itemStack, IBakedModel model)
    {
        if (enchantmentProperties == null)
        {
            return false;
        }
        else if (itemStack == null)
        {
            return false;
        }
        else
        {
            int[][] enchantments = getEnchantmentIdLevels(itemStack);

            if (enchantments.length <= 0)
            {
                return false;
            }
            else
            {
                boolean rendered = false;
                TextureManager textureManager = Config.getTextureManager();

                for (int enchantmentIndex = 0; enchantmentIndex < enchantments.length; ++enchantmentIndex)
                {
                    int enchantmentId = enchantments[enchantmentIndex][0];

                    if (enchantmentId >= 0 && enchantmentId < enchantmentProperties.length)
                    {
                        CustomItemProperties[] propertiesArray = enchantmentProperties[enchantmentId];

                        if (propertiesArray != null)
                        {
                            for (int propertyIndex = 0; propertyIndex < propertiesArray.length; ++propertyIndex)
                            {
                                CustomItemProperties properties = propertiesArray[propertyIndex];

                                if (propertyIndex == 0 && !isEnchantmentIdProcessed(enchantments, enchantmentIndex, enchantmentId) && matchesProperties(properties, itemStack, enchantments) && properties.textureLocation != null)
                                {
                                    textureManager.bindTexture(properties.textureLocation);
                                    float textureWidth = properties.getTextureWidth(textureManager);

                                    if (!rendered)
                                    {
                                        rendered = true;
                                        GlStateManager.depthMask(false);
                                        GlStateManager.depthFunc(514);
                                        GlStateManager.disableLighting();
                                        GlStateManager.matrixMode(5890);
                                    }

                                    Blender.setupBlend(properties.blend, 1.0F);
                                    GlStateManager.pushMatrix();
                                    GlStateManager.scale(textureWidth / 2.0F, textureWidth / 2.0F, textureWidth / 2.0F);
                                    float textureOffset = properties.speed * (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
                                    GlStateManager.translate(textureOffset, 0.0F, 0.0F);
                                    GlStateManager.rotate(properties.rotation, 0.0F, 0.0F, 1.0F);
                                    renderItem.renderModel(model, -1);
                                    GlStateManager.popMatrix();
                                }
                            }
                        }
                    }
                }

                if (rendered)
                {
                    GlStateManager.enableAlpha();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.matrixMode(5888);
                    GlStateManager.enableLighting();
                    GlStateManager.depthFunc(515);
                    GlStateManager.depthMask(true);
                    textureManager.bindTexture(TextureMap.locationBlocksTexture);
                }

                return rendered;
            }
        }
    }

    public static boolean renderCustomArmorEffect(EntityLivingBase entity, ItemStack itemStack, ModelBase model, float limbSwing, float prevLimbSwing, float partialTicks, float timeLimbSwing, float yaw, float pitch, float scale)
    {
        if (enchantmentProperties == null)
        {
            return false;
        }
        else if (Config.isShaders() && Shaders.isShadowPass)
        {
            return false;
        }
        else if (itemStack == null)
        {
            return false;
        }
        else
        {
            int[][] enchantments = getEnchantmentIdLevels(itemStack);

            if (enchantments.length <= 0)
            {
                return false;
            }
            else
            {
                boolean rendered = false;
                TextureManager textureManager = Config.getTextureManager();

                for (int enchantmentIndex = 0; enchantmentIndex < enchantments.length; ++enchantmentIndex)
                {
                    int enchantmentId = enchantments[enchantmentIndex][0];

                    if (enchantmentId >= 0 && enchantmentId < enchantmentProperties.length)
                    {
                        CustomItemProperties[] propertiesArray = enchantmentProperties[enchantmentId];

                        if (propertiesArray != null)
                        {
                            for (int propertyIndex = 0; propertyIndex < propertiesArray.length; ++propertyIndex)
                            {
                                CustomItemProperties properties = propertiesArray[propertyIndex];

                                if (propertyIndex == 0 && !isEnchantmentIdProcessed(enchantments, enchantmentIndex, enchantmentId) && matchesProperties(properties, itemStack, enchantments) && properties.textureLocation != null)
                                {
                                    textureManager.bindTexture(properties.textureLocation);
                                    float textureWidth = properties.getTextureWidth(textureManager);

                                    if (!rendered)
                                    {
                                        rendered = true;

                                        if (Config.isShaders())
                                        {
                                            ShadersRender.renderEnchantedGlintBegin();
                                        }

                                        GlStateManager.enableBlend();
                                        GlStateManager.depthFunc(514);
                                        GlStateManager.depthMask(false);
                                    }

                                    Blender.setupBlend(properties.blend, 1.0F);
                                    GlStateManager.disableLighting();
                                    GlStateManager.matrixMode(5890);
                                    GlStateManager.loadIdentity();
                                    GlStateManager.rotate(properties.rotation, 0.0F, 0.0F, 1.0F);
                                    float textureScale = textureWidth / 8.0F;
                                    GlStateManager.scale(textureScale, textureScale / 2.0F, textureScale);
                                    float textureOffset = properties.speed * (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
                                    GlStateManager.translate(0.0F, textureOffset, 0.0F);
                                    GlStateManager.matrixMode(5888);
                                    model.render(entity, limbSwing, prevLimbSwing, timeLimbSwing, yaw, pitch, scale);
                                }
                            }
                        }
                    }
                }

                if (rendered)
                {
                    GlStateManager.enableAlpha();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.matrixMode(5890);
                    GlStateManager.loadIdentity();
                    GlStateManager.matrixMode(5888);
                    GlStateManager.enableLighting();
                    GlStateManager.depthMask(true);
                    GlStateManager.depthFunc(515);
                    GlStateManager.disableBlend();

                    if (Config.isShaders())
                    {
                        ShadersRender.renderEnchantedGlintEnd();
                    }
                }

                return rendered;
            }
        }
    }

    public static boolean isUseGlint()
    {
        return useGlint;
    }
}
