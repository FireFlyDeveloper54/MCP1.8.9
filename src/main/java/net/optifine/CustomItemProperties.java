package net.optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.src.Config;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.optifine.config.IParserInt;
import net.optifine.config.NbtTagValue;
import net.optifine.config.ParserEnchantmentId;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.render.Blender;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;

public class CustomItemProperties
{
    public String name = null;
    public String basePath = null;
    public int type = 1;
    public int[] items = null;
    public String texture = null;
    public Map<String, String> mapTextures = null;
    public String model = null;
    public Map<String, String> mapModels = null;
    public RangeListInt damage = null;
    public boolean damagePercent = false;
    public int damageMask = 0;
    public RangeListInt stackSize = null;
    public RangeListInt enchantmentIds = null;
    public RangeListInt enchantmentLevels = null;
    public NbtTagValue[] nbtTagValues = null;
    public int hand = 0;
    public int blend = 1;
    public float speed = 0.0F;
    public float rotation = 0.0F;
    public int layer = 0;
    public float duration = 1.0F;
    public int weight = 0;
    public ResourceLocation textureLocation = null;
    public Map mapTextureLocations = null;
    public TextureAtlasSprite sprite = null;
    public Map mapSprites = null;
    public IBakedModel bakedModelTexture = null;
    public Map<String, IBakedModel> mapBakedModelsTexture = null;
    public IBakedModel bakedModelFull = null;
    public Map<String, IBakedModel> mapBakedModelsFull = null;
    private int textureWidth = 0;
    private int textureHeight = 0;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_ENCHANTMENT = 2;
    public static final int TYPE_ARMOR = 3;
    public static final int HAND_ANY = 0;
    public static final int HAND_MAIN = 1;
    public static final int HAND_OFF = 2;
    public static final String INVENTORY = "inventory";

    public CustomItemProperties(Properties props, String path)
    {
        this.name = parseName(path);
        this.basePath = parseBasePath(path);
        this.type = this.parseType(props.getProperty("type"));
        this.items = this.parseItems(props.getProperty("items"), props.getProperty("matchItems"));
        this.mapModels = parseModels(props, this.basePath);
        this.model = parseModel(props.getProperty("model"), path, this.basePath, this.type, this.mapModels);
        this.mapTextures = parseTextures(props, this.basePath);
        boolean textureFromPath = this.mapModels == null && this.model == null;
        this.texture = parseTexture(props.getProperty("texture"), props.getProperty("tile"), props.getProperty("source"), path, this.basePath, this.type, this.mapTextures, textureFromPath);
        String damageText = props.getProperty("damage");

        if (damageText != null)
        {
            this.damagePercent = damageText.contains("%");
            damageText = damageText.replace("%", "");
            this.damage = this.parseRangeListInt(damageText);
            this.damageMask = this.parseInt(props.getProperty("damageMask"), 0);
        }

        this.stackSize = this.parseRangeListInt(props.getProperty("stackSize"));
        this.enchantmentIds = this.parseRangeListInt(props.getProperty("enchantmentIDs"), new ParserEnchantmentId());
        this.enchantmentLevels = this.parseRangeListInt(props.getProperty("enchantmentLevels"));
        this.nbtTagValues = this.parseNbtTagValues(props);
        this.hand = this.parseHand(props.getProperty("hand"));
        this.blend = Blender.parseBlend(props.getProperty("blend"));
        this.speed = this.parseFloat(props.getProperty("speed"), 0.0F);
        this.rotation = this.parseFloat(props.getProperty("rotation"), 0.0F);
        this.layer = this.parseInt(props.getProperty("layer"), 0);
        this.weight = this.parseInt(props.getProperty("weight"), 0);
        this.duration = this.parseFloat(props.getProperty("duration"), 1.0F);
    }

    private static String parseName(String path)
    {
        String name = path;
        int slashIndex = path.lastIndexOf(47);

        if (slashIndex >= 0)
        {
            name = path.substring(slashIndex + 1);
        }

        int extensionIndex = name.lastIndexOf(46);

        if (extensionIndex >= 0)
        {
            name = name.substring(0, extensionIndex);
        }

        return name;
    }

    private static String parseBasePath(String path)
    {
        int slashIndex = path.lastIndexOf(47);
        return slashIndex < 0 ? "" : path.substring(0, slashIndex);
    }

    private int parseType(String str)
    {
        if (str == null)
        {
            return 1;
        }
        else if (str.equals("item"))
        {
            return 1;
        }
        else if (str.equals("enchantment"))
        {
            return 2;
        }
        else if (str.equals("armor"))
        {
            return 3;
        }
        else
        {
            Config.warn("Unknown method: " + str);
            return 0;
        }
    }

    private int[] parseItems(String str, String fallbackItems)
    {
        if (str == null)
        {
            str = fallbackItems;
        }

        if (str == null)
        {
            return null;
        }
        else
        {
            str = str.trim();
            Set itemIds = new TreeSet();
            String[] itemTokens = Config.tokenize(str, " ");
            itemTokenLoop:

            for (int itemIndex = 0; itemIndex < itemTokens.length; ++itemIndex)
            {
                String itemToken = itemTokens[itemIndex];
                int itemId = Config.parseInt(itemToken, -1);

                if (itemId >= 0)
                {
                    itemIds.add(Integer.valueOf(itemId));
                }
                else
                {
                    if (itemToken.contains("-"))
                    {
                        String[] rangeTokens = Config.tokenize(itemToken, "-");

                        if (rangeTokens.length == 2)
                        {
                            int rangeStart = Config.parseInt(rangeTokens[0], -1);
                            int rangeEnd = Config.parseInt(rangeTokens[1], -1);

                            if (rangeStart >= 0 && rangeEnd >= 0)
                            {
                                int minId = Math.min(rangeStart, rangeEnd);
                                int maxId = Math.max(rangeStart, rangeEnd);
                                int rangeId = minId;

                                while (true)
                                {
                                    if (rangeId > maxId)
                                    {
                                        continue itemTokenLoop;
                                    }

                                    itemIds.add(Integer.valueOf(rangeId));
                                    ++rangeId;
                                }
                            }
                        }
                    }

                    Item item = Item.getByNameOrId(itemToken);

                    if (item == null)
                    {
                        Config.warn("Item not found: " + itemToken);
                    }
                    else
                    {
                        int resolvedItemId = Item.getIdFromItem(item);

                        if (resolvedItemId <= 0)
                        {
                            Config.warn("Item not found: " + itemToken);
                        }
                        else
                        {
                            itemIds.add(Integer.valueOf(resolvedItemId));
                        }
                    }
                }
            }

            Integer[] itemIdObjects = (Integer[])((Integer[])itemIds.toArray(new Integer[itemIds.size()]));
            int[] itemIdArray = new int[itemIdObjects.length];

            for (int itemIndex = 0; itemIndex < itemIdArray.length; ++itemIndex)
            {
                itemIdArray[itemIndex] = itemIdObjects[itemIndex].intValue();
            }

            return itemIdArray;
        }
    }

    private static String parseTexture(String texStr, String texStr2, String texStr3, String path, String basePath, int type, Map<String, String> mapTexs, boolean textureFromPath)
    {
        if (texStr == null)
        {
            texStr = texStr2;
        }

        if (texStr == null)
        {
            texStr = texStr3;
        }

        if (texStr != null)
        {
            String pngSuffix = ".png";

            if (texStr.endsWith(pngSuffix))
            {
                texStr = texStr.substring(0, texStr.length() - pngSuffix.length());
            }

            texStr = fixTextureName(texStr, basePath);
            return texStr;
        }
        else if (type == 3)
        {
            return null;
        }
        else
        {
            if (mapTexs != null)
            {
                String bowStandbyTexture = (String)mapTexs.get("texture.bow_standby");

                if (bowStandbyTexture != null)
                {
                    return bowStandbyTexture;
                }
            }

            if (!textureFromPath)
            {
                return null;
            }
            else
            {
                String textureName = path;
                int slashIndex = path.lastIndexOf(47);

                if (slashIndex >= 0)
                {
                    textureName = path.substring(slashIndex + 1);
                }

                int extensionIndex = textureName.lastIndexOf(46);

                if (extensionIndex >= 0)
                {
                    textureName = textureName.substring(0, extensionIndex);
                }

                textureName = fixTextureName(textureName, basePath);
                return textureName;
            }
        }
    }

    private static Map parseTextures(Properties props, String basePath)
    {
        String texturePrefix = "texture.";
        Map matchingTextures = getMatchingProperties(props, texturePrefix);

        if (matchingTextures.size() <= 0)
        {
            return null;
        }
        else
        {
            Set textureKeys = matchingTextures.keySet();
            Map textures = new LinkedHashMap();

            for (Object o : textureKeys)
            {
                String textureKey = (String) o;
                String textureName = (String)matchingTextures.get(textureKey);
                textureName = fixTextureName(textureName, basePath);
                textures.put(textureKey, textureName);
            }

            return textures;
        }
    }

    private static String fixTextureName(String iconName, String basePath)
    {
        iconName = TextureUtils.fixResourcePath(iconName, basePath);

        if (!iconName.startsWith(basePath) && !iconName.startsWith("textures/") && !iconName.startsWith("mcpatcher/"))
        {
            iconName = basePath + "/" + iconName;
        }

        if (iconName.endsWith(".png"))
        {
            iconName = iconName.substring(0, iconName.length() - 4);
        }

        if (iconName.startsWith("/"))
        {
            iconName = iconName.substring(1);
        }

        return iconName;
    }

    private static String parseModel(String modelStr, String path, String basePath, int type, Map<String, String> mapModelNames)
    {
        if (modelStr != null)
        {
            String jsonSuffix = ".json";

            if (modelStr.endsWith(jsonSuffix))
            {
                modelStr = modelStr.substring(0, modelStr.length() - jsonSuffix.length());
            }

            modelStr = fixModelName(modelStr, basePath);
            return modelStr;
        }
        else if (type == 3)
        {
            return null;
        }
        else
        {
            if (mapModelNames != null)
            {
                String bowStandbyModel = (String)mapModelNames.get("model.bow_standby");

                if (bowStandbyModel != null)
                {
                    return bowStandbyModel;
                }
            }

            return modelStr;
        }
    }

    private static Map parseModels(Properties props, String basePath)
    {
        String modelPrefix = "model.";
        Map matchingModels = getMatchingProperties(props, modelPrefix);

        if (matchingModels.size() <= 0)
        {
            return null;
        }
        else
        {
            Set modelKeys = matchingModels.keySet();
            Map models = new LinkedHashMap();

            for (Object o : modelKeys)
            {
                String modelKey = (String) o;
                String modelName = (String)matchingModels.get(modelKey);
                modelName = fixModelName(modelName, basePath);
                models.put(modelKey, modelName);
            }

            return models;
        }
    }

    private static String fixModelName(String modelName, String basePath)
    {
        modelName = TextureUtils.fixResourcePath(modelName, basePath);
        boolean vanillaModelPath = modelName.startsWith("block/") || modelName.startsWith("item/");

        if (!modelName.startsWith(basePath) && !vanillaModelPath && !modelName.startsWith("mcpatcher/"))
        {
            modelName = basePath + "/" + modelName;
        }

        String jsonSuffix = ".json";

        if (modelName.endsWith(jsonSuffix))
        {
            modelName = modelName.substring(0, modelName.length() - jsonSuffix.length());
        }

        if (modelName.startsWith("/"))
        {
            modelName = modelName.substring(1);
        }

        return modelName;
    }

    private int parseInt(String str, int defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            str = str.trim();
            int value = Config.parseInt(str, Integer.MIN_VALUE);

            if (value == Integer.MIN_VALUE)
            {
                Config.warn("Invalid integer: " + str);
                return defVal;
            }
            else
            {
                return value;
            }
        }
    }

    private float parseFloat(String str, float defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            str = str.trim();
            float value = Config.parseFloat(str, Float.MIN_VALUE);

            if (value == Float.MIN_VALUE)
            {
                Config.warn("Invalid float: " + str);
                return defVal;
            }
            else
            {
                return value;
            }
        }
    }

    private RangeListInt parseRangeListInt(String str)
    {
        return this.parseRangeListInt(str, (IParserInt)null);
    }

    private RangeListInt parseRangeListInt(String str, IParserInt parser)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            String[] rangeTokens = Config.tokenize(str, " ");
            RangeListInt ranges = new RangeListInt();

            for (int rangeIndex = 0; rangeIndex < rangeTokens.length; ++rangeIndex)
            {
                String rangeToken = rangeTokens[rangeIndex];

                if (parser != null)
                {
                    int parsedValue = parser.parse(rangeToken, Integer.MIN_VALUE);

                    if (parsedValue != Integer.MIN_VALUE)
                    {
                        ranges.addRange(new RangeInt(parsedValue, parsedValue));
                        continue;
                    }
                }

                RangeInt range = this.parseRangeInt(rangeToken);

                if (range == null)
                {
                    Config.warn("Invalid range list: " + str);
                    return null;
                }

                ranges.addRange(range);
            }

            return ranges;
        }
    }

    private RangeInt parseRangeInt(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            str = str.trim();
            int hyphenCount = str.length() - str.replace("-", "").length();

            if (hyphenCount > 1)
            {
                Config.warn("Invalid range: " + str);
                return null;
            }
            else
            {
                String[] rangeTokens = Config.tokenize(str, "- ");
                int[] rangeValues = new int[rangeTokens.length];

                for (int rangeIndex = 0; rangeIndex < rangeTokens.length; ++rangeIndex)
                {
                    String rangeToken = rangeTokens[rangeIndex];
                    int rangeValue = Config.parseInt(rangeToken, -1);

                    if (rangeValue < 0)
                    {
                        Config.warn("Invalid range: " + str);
                        return null;
                    }

                    rangeValues[rangeIndex] = rangeValue;
                }

                if (rangeValues.length == 1)
                {
                    int value = rangeValues[0];

                    if (str.startsWith("-"))
                    {
                        return new RangeInt(0, value);
                    }
                    else if (str.endsWith("-"))
                    {
                        return new RangeInt(value, 65535);
                    }
                    else
                    {
                        return new RangeInt(value, value);
                    }
                }
                else if (rangeValues.length == 2)
                {
                    int minValue = Math.min(rangeValues[0], rangeValues[1]);
                    int maxValue = Math.max(rangeValues[0], rangeValues[1]);
                    return new RangeInt(minValue, maxValue);
                }
                else
                {
                    Config.warn("Invalid range: " + str);
                    return null;
                }
            }
        }
    }

    private NbtTagValue[] parseNbtTagValues(Properties props)
    {
        String nbtPrefix = "nbt.";
        Map matchingNbtProperties = getMatchingProperties(props, nbtPrefix);

        if (matchingNbtProperties.size() <= 0)
        {
            return null;
        }
        else
        {
            List nbtTagValues = new ArrayList();

            for (Object o : matchingNbtProperties.keySet())
            {
                String propertyKey = (String) o;
                String propertyValue = (String)matchingNbtProperties.get(propertyKey);
                String tagName = propertyKey.substring(nbtPrefix.length());
                NbtTagValue nbtTagValue = new NbtTagValue(tagName, propertyValue);
                nbtTagValues.add(nbtTagValue);
            }

            NbtTagValue[] nbtTagValueArray = (NbtTagValue[])((NbtTagValue[])nbtTagValues.toArray(new NbtTagValue[nbtTagValues.size()]));
            return nbtTagValueArray;
        }
    }

    private static Map getMatchingProperties(Properties props, String keyPrefix)
    {
        Map matchingProperties = new LinkedHashMap();

        for (Object o: props.keySet())
        {
            String key = (String) o;
            String value = props.getProperty(key);

            if (key.startsWith(keyPrefix))
            {
                matchingProperties.put(key, value);
            }
        }

        return matchingProperties;
    }

    private int parseHand(String str)
    {
        if (str == null)
        {
            return 0;
        }
        else
        {
            str = str.toLowerCase();

            if (str.equals("any"))
            {
                return 0;
            }
            else if (str.equals("main"))
            {
                return 1;
            }
            else if (str.equals("off"))
            {
                return 2;
            }
            else
            {
                Config.warn("Invalid hand: " + str);
                return 0;
            }
        }
    }

    public boolean isValid(String path)
    {
        if (this.name != null && this.name.length() > 0)
        {
            if (this.basePath == null)
            {
                Config.warn("No base path found: " + path);
                return false;
            }
            else if (this.type == 0)
            {
                Config.warn("No type defined: " + path);
                return false;
            }
            else
            {
                if (this.type == 1 || this.type == 3)
                {
                    if (this.items == null)
                    {
                        this.items = this.detectItems();
                    }

                    if (this.items == null)
                    {
                        Config.warn("No items defined: " + path);
                        return false;
                    }
                }

                if (this.texture == null && this.mapTextures == null && this.model == null && this.mapModels == null)
                {
                    Config.warn("No texture or model specified: " + path);
                    return false;
                }
                else if (this.type == 2 && this.enchantmentIds == null)
                {
                    Config.warn("No enchantmentIDs specified: " + path);
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }
        else
        {
            Config.warn("No name found: " + path);
            return false;
        }
    }

    private int[] detectItems()
    {
        Item item = Item.getByNameOrId(this.name);

        if (item == null)
        {
            return null;
        }
        else
        {
            int itemId = Item.getIdFromItem(item);
            return itemId <= 0 ? null : new int[] {itemId};
        }
    }

    public void updateIcons(TextureMap textureMap)
    {
        if (this.texture != null)
        {
            this.textureLocation = this.getTextureLocation(this.texture);

            if (this.type == 1)
            {
                ResourceLocation spriteLocation = this.getSpriteLocation(this.textureLocation);
                this.sprite = textureMap.registerSprite(spriteLocation);
            }
        }

        if (this.mapTextures != null)
        {
            this.mapTextureLocations = new HashMap();
            this.mapSprites = new HashMap();

            for (String textureKey : this.mapTextures.keySet())
            {
                String textureName = (String)this.mapTextures.get(textureKey);
                ResourceLocation textureLocation = this.getTextureLocation(textureName);
                this.mapTextureLocations.put(textureKey, textureLocation);

                if (this.type == 1)
                {
                    ResourceLocation spriteLocation = this.getSpriteLocation(textureLocation);
                    TextureAtlasSprite sprite = textureMap.registerSprite(spriteLocation);
                    this.mapSprites.put(textureKey, sprite);
                }
            }
        }
    }

    private ResourceLocation getTextureLocation(String texName)
    {
        if (texName == null)
        {
            return null;
        }
        else
        {
            ResourceLocation resourceLocation = new ResourceLocation(texName);
            String resourceDomain = resourceLocation.getResourceDomain();
            String resourcePath = resourceLocation.getResourcePath();

            if (!resourcePath.contains("/"))
            {
                resourcePath = "textures/items/" + resourcePath;
            }

            String pngPath = resourcePath + ".png";
            ResourceLocation textureLocation = new ResourceLocation(resourceDomain, pngPath);
            boolean resourceExists = Config.hasResource(textureLocation);

            if (!resourceExists)
            {
                Config.warn("File not found: " + pngPath);
            }

            return textureLocation;
        }
    }

    private ResourceLocation getSpriteLocation(ResourceLocation resLoc)
    {
        String resourcePath = resLoc.getResourcePath();
        resourcePath = StrUtils.removePrefix(resourcePath, "textures/");
        resourcePath = StrUtils.removeSuffix(resourcePath, ".png");
        ResourceLocation spriteLocation = new ResourceLocation(resLoc.getResourceDomain(), resourcePath);
        return spriteLocation;
    }

    public void updateModelTexture(TextureMap textureMap, ItemModelGenerator itemModelGenerator)
    {
        if (this.texture != null || this.mapTextures != null)
        {
            String[] modelTextures = this.getModelTextures();
            boolean useTint = this.isUseTint();
            this.bakedModelTexture = makeBakedModel(textureMap, itemModelGenerator, modelTextures, useTint);

            if (this.type == 1 && this.mapTextures != null)
            {
                for (String textureKey : this.mapTextures.keySet())
                {
                    String textureName = (String)this.mapTextures.get(textureKey);
                    String modelKey = StrUtils.removePrefix(textureKey, "texture.");

                    if (modelKey.startsWith("bow") || modelKey.startsWith("fishing_rod") || modelKey.startsWith("shield"))
                    {
                        String[] textureNames = new String[] {textureName};
                        IBakedModel bakedModel = makeBakedModel(textureMap, itemModelGenerator, textureNames, useTint);

                        if (this.mapBakedModelsTexture == null)
                        {
                            this.mapBakedModelsTexture = new HashMap();
                        }

                        this.mapBakedModelsTexture.put(modelKey, bakedModel);
                    }
                }
            }
        }
    }

    private boolean isUseTint()
    {
        return true;
    }

    private static IBakedModel makeBakedModel(TextureMap textureMap, ItemModelGenerator itemModelGenerator, String[] textures, boolean useTint)
    {
        String[] modelTextures = new String[textures.length];

        for (int textureIndex = 0; textureIndex < modelTextures.length; ++textureIndex)
        {
            String textureName = textures[textureIndex];
            modelTextures[textureIndex] = StrUtils.removePrefix(textureName, "textures/");
        }

        ModelBlock modelBlock = makeModelBlock(modelTextures);
        ModelBlock generatedModelBlock = itemModelGenerator.makeItemModel(textureMap, modelBlock);
        IBakedModel bakedModel = bakeModel(textureMap, generatedModelBlock, useTint);
        return bakedModel;
    }

    private String[] getModelTextures()
    {
        if (this.type == 1 && this.items.length == 1)
        {
            Item item = Item.getItemById(this.items[0]);

            if (item == Items.potionitem && this.damage != null && this.damage.getCountRanges() > 0)
            {
                RangeInt rangeInt = this.damage.getRange(0);
                int potionDamage = rangeInt.getMin();
                boolean splashPotion = (potionDamage & 16384) != 0;
                String overlayTexture = this.getMapTexture(this.mapTextures, "texture.potion_overlay", "items/potion_overlay");
                String bottleTexture = null;

                if (splashPotion)
                {
                    bottleTexture = this.getMapTexture(this.mapTextures, "texture.potion_bottle_splash", "items/potion_bottle_splash");
                }
                else
                {
                    bottleTexture = this.getMapTexture(this.mapTextures, "texture.potion_bottle_drinkable", "items/potion_bottle_drinkable");
                }

                return new String[] {overlayTexture, bottleTexture};
            }

            if (item instanceof ItemArmor)
            {
                ItemArmor armorItem = (ItemArmor)item;

                if (armorItem.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER)
                {
                    String armorMaterialName = "leather";
                    String armorTypeName = "helmet";

                    if (armorItem.armorType == 0)
                    {
                        armorTypeName = "helmet";
                    }

                    if (armorItem.armorType == 1)
                    {
                        armorTypeName = "chestplate";
                    }

                    if (armorItem.armorType == 2)
                    {
                        armorTypeName = "leggings";
                    }

                    if (armorItem.armorType == 3)
                    {
                        armorTypeName = "boots";
                    }

                    String textureBase = armorMaterialName + "_" + armorTypeName;
                    String armorTexture = this.getMapTexture(this.mapTextures, "texture." + textureBase, "items/" + textureBase);
                    String armorOverlayTexture = this.getMapTexture(this.mapTextures, "texture." + textureBase + "_overlay", "items/" + textureBase + "_overlay");
                    return new String[] {armorTexture, armorOverlayTexture};
                }
            }
        }

        return new String[] {this.texture};
    }

    private String getMapTexture(Map<String, String> map, String key, String def)
    {
        if (map == null)
        {
            return def;
        }
        else
        {
            String texture = (String)map.get(key);
            return texture == null ? def : texture;
        }
    }

    private static ModelBlock makeModelBlock(String[] modelTextures)
    {
        StringBuffer modelJson = new StringBuffer();
        modelJson.append("{\"parent\": \"builtin/generated\",\"textures\": {");

        for (int textureIndex = 0; textureIndex < modelTextures.length; ++textureIndex)
        {
            String texture = modelTextures[textureIndex];

            if (textureIndex > 0)
            {
                modelJson.append(", ");
            }

            modelJson.append("\"layer" + textureIndex + "\": \"" + texture + "\"");
        }

        modelJson.append("}}");
        String modelJsonText = modelJson.toString();
        ModelBlock modelBlock = ModelBlock.deserialize(modelJsonText);
        return modelBlock;
    }

    private static IBakedModel bakeModel(TextureMap textureMap, ModelBlock modelBlockIn, boolean useTint)
    {
        ModelRotation modelRotation = ModelRotation.X0_Y0;
        boolean uvLocked = false;
        String particleTexture = modelBlockIn.resolveTextureName("particle");
        TextureAtlasSprite particleSprite = textureMap.getAtlasSprite((new ResourceLocation(particleTexture)).toString());
        SimpleBakedModel.Builder modelBuilder = (new SimpleBakedModel.Builder(modelBlockIn)).setTexture(particleSprite);

        for (BlockPart blockPart : modelBlockIn.getElements())
        {
            for (EnumFacing facing : blockPart.mapFaces.keySet())
            {
                BlockPartFace blockPartFace = (BlockPartFace)blockPart.mapFaces.get(facing);

                if (!useTint)
                {
                    blockPartFace = new BlockPartFace(blockPartFace.cullFace, -1, blockPartFace.texture, blockPartFace.blockFaceUV);
                }

                String faceTexture = modelBlockIn.resolveTextureName(blockPartFace.texture);
                TextureAtlasSprite faceSprite = textureMap.getAtlasSprite((new ResourceLocation(faceTexture)).toString());
                BakedQuad bakedQuad = makeBakedQuad(blockPart, blockPartFace, faceSprite, facing, modelRotation, uvLocked);

                if (blockPartFace.cullFace == null)
                {
                    modelBuilder.addGeneralQuad(bakedQuad);
                }
                else
                {
                    modelBuilder.addFaceQuad(modelRotation.rotateFace(blockPartFace.cullFace), bakedQuad);
                }
            }
        }

        return modelBuilder.makeBakedModel();
    }

    private static BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace blockPartFace, TextureAtlasSprite textureAtlasSprite, EnumFacing enumFacing, ModelRotation modelRotation, boolean uvLocked)
    {
        FaceBakery faceBakery = new FaceBakery();
        return faceBakery.makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, blockPartFace, textureAtlasSprite, enumFacing, modelRotation, blockPart.partRotation, uvLocked, blockPart.shade);
    }

    public String toString()
    {
        return "" + this.basePath + "/" + this.name + ", type: " + this.type + ", items: [" + Config.arrayToString(this.items) + "], textture: " + this.texture;
    }

    public float getTextureWidth(TextureManager textureManager)
    {
        if (this.textureWidth <= 0)
        {
            if (this.textureLocation != null)
            {
                ITextureObject textureObject = textureManager.getTexture(this.textureLocation);
                int textureId = textureObject.getGlTextureId();
                int boundTexture = GlStateManager.getBoundTexture();
                GlStateManager.bindTexture(textureId);
                this.textureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                GlStateManager.bindTexture(boundTexture);
            }

            if (this.textureWidth <= 0)
            {
                this.textureWidth = 16;
            }
        }

        return (float)this.textureWidth;
    }

    public float getTextureHeight(TextureManager textureManager)
    {
        if (this.textureHeight <= 0)
        {
            if (this.textureLocation != null)
            {
                ITextureObject textureObject = textureManager.getTexture(this.textureLocation);
                int textureId = textureObject.getGlTextureId();
                int boundTexture = GlStateManager.getBoundTexture();
                GlStateManager.bindTexture(textureId);
                this.textureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                GlStateManager.bindTexture(boundTexture);
            }

            if (this.textureHeight <= 0)
            {
                this.textureHeight = 16;
            }
        }

        return (float)this.textureHeight;
    }

    public IBakedModel getBakedModel(ResourceLocation modelLocation, boolean fullModel)
    {
        IBakedModel bakedModel;
        Map<String, IBakedModel> bakedModels;

        if (fullModel)
        {
            bakedModel = this.bakedModelFull;
            bakedModels = this.mapBakedModelsFull;
        }
        else
        {
            bakedModel = this.bakedModelTexture;
            bakedModels = this.mapBakedModelsTexture;
        }

        if (modelLocation != null && bakedModels != null)
        {
            String modelPath = modelLocation.getResourcePath();
            IBakedModel mappedBakedModel = (IBakedModel)bakedModels.get(modelPath);

            if (mappedBakedModel != null)
            {
                return mappedBakedModel;
            }
        }

        return bakedModel;
    }

    public void loadModels(ModelBakery modelBakery)
    {
        if (this.model != null)
        {
            loadItemModel(modelBakery, this.model);
        }

        if (this.type == 1 && this.mapModels != null)
        {
            for (String modelKey : this.mapModels.keySet())
            {
                String modelName = (String)this.mapModels.get(modelKey);
                String modelVariant = StrUtils.removePrefix(modelKey, "model.");

                if (modelVariant.startsWith("bow") || modelVariant.startsWith("fishing_rod") || modelVariant.startsWith("shield"))
                {
                    loadItemModel(modelBakery, modelName);
                }
            }
        }
    }

    public void updateModelsFull()
    {
        ModelManager modelManager = Config.getModelManager();
        IBakedModel missingModel = modelManager.getMissingModel();

        if (this.model != null)
        {
            ResourceLocation modelResourceLocation = getModelLocation(this.model);
            ModelResourceLocation inventoryModelLocation = new ModelResourceLocation(modelResourceLocation, "inventory");
            this.bakedModelFull = modelManager.getModel(inventoryModelLocation);

            if (this.bakedModelFull == missingModel)
            {
                Config.warn("Custom Items: Model not found " + inventoryModelLocation.getResourcePath());
                this.bakedModelFull = null;
            }
        }

        if (this.type == 1 && this.mapModels != null)
        {
            for (String modelKey : this.mapModels.keySet())
            {
                String modelName = (String)this.mapModels.get(modelKey);
                String modelVariant = StrUtils.removePrefix(modelKey, "model.");

                if (modelVariant.startsWith("bow") || modelVariant.startsWith("fishing_rod") || modelVariant.startsWith("shield"))
                {
                    ResourceLocation modelResourceLocation = getModelLocation(modelName);
                    ModelResourceLocation inventoryModelLocation = new ModelResourceLocation(modelResourceLocation, "inventory");
                    IBakedModel mappedBakedModel = modelManager.getModel(inventoryModelLocation);

                    if (mappedBakedModel == missingModel)
                    {
                        Config.warn("Custom Items: Model not found " + inventoryModelLocation.getResourcePath());
                    }
                    else
                    {
                        if (this.mapBakedModelsFull == null)
                        {
                            this.mapBakedModelsFull = new HashMap();
                        }

                        this.mapBakedModelsFull.put(modelVariant, mappedBakedModel);
                    }
                }
            }
        }
    }

    private static void loadItemModel(ModelBakery modelBakery, String model)
    {
        ResourceLocation modelResourceLocation = getModelLocation(model);
        ModelResourceLocation inventoryModelLocation = new ModelResourceLocation(modelResourceLocation, "inventory");

        
        modelBakery.loadItemModel(modelResourceLocation.toString(), inventoryModelLocation, modelResourceLocation);
    
    }

    private static void checkNull(Object obj, String msg) throws NullPointerException
    {
        if (obj == null)
        {
            throw new NullPointerException(msg);
        }
    }

    private static ResourceLocation getModelLocation(String modelName)
    {
        return new ResourceLocation(modelName);
    }
}
