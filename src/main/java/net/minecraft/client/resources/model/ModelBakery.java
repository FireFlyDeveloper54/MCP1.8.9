package net.minecraft.client.resources.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.block.model.ITransformation;
import net.optifine.CustomItems;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.texture.IIconCreator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;

public class ModelBakery
{
    private static final Set<ResourceLocation> LOCATIONS_BUILTIN_TEXTURES = Sets.newHashSet(new ResourceLocation[] {new ResourceLocation("blocks/water_flow"), new ResourceLocation("blocks/water_still"), new ResourceLocation("blocks/lava_flow"), new ResourceLocation("blocks/lava_still"), new ResourceLocation("blocks/destroy_stage_0"), new ResourceLocation("blocks/destroy_stage_1"), new ResourceLocation("blocks/destroy_stage_2"), new ResourceLocation("blocks/destroy_stage_3"), new ResourceLocation("blocks/destroy_stage_4"), new ResourceLocation("blocks/destroy_stage_5"), new ResourceLocation("blocks/destroy_stage_6"), new ResourceLocation("blocks/destroy_stage_7"), new ResourceLocation("blocks/destroy_stage_8"), new ResourceLocation("blocks/destroy_stage_9"), new ResourceLocation("items/empty_armor_slot_helmet"), new ResourceLocation("items/empty_armor_slot_chestplate"), new ResourceLocation("items/empty_armor_slot_leggings"), new ResourceLocation("items/empty_armor_slot_boots")});
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final ModelResourceLocation MODEL_MISSING = new ModelResourceLocation("builtin/missing", "missing");
    private static final Map<String, String> BUILT_IN_MODELS = Maps.<String, String>newHashMap();
    private static final Joiner JOINER = Joiner.on(" -> ");
    private final IResourceManager resourceManager;
    private final Map<ResourceLocation, TextureAtlasSprite> sprites = Maps.<ResourceLocation, TextureAtlasSprite>newHashMap();
    private final Map<ResourceLocation, ModelBlock> models = Maps.<ResourceLocation, ModelBlock>newLinkedHashMap();
    private final Map<ModelResourceLocation, ModelBlockDefinition.Variants> variants = Maps.<ModelResourceLocation, ModelBlockDefinition.Variants>newLinkedHashMap();
    private final TextureMap textureMap;
    private final BlockModelShapes blockModelShapes;
    private final FaceBakery faceBakery = new FaceBakery();
    private final ItemModelGenerator itemModelGenerator = new ItemModelGenerator();
    private RegistrySimple<ModelResourceLocation, IBakedModel> bakedRegistry = new RegistrySimple();
    private static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final ModelBlock MODEL_COMPASS = ModelBlock.deserialize("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final ModelBlock MODEL_CLOCK = ModelBlock.deserialize("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private static final ModelBlock MODEL_ENTITY = ModelBlock.deserialize("{\"elements\":[{  \"from\": [0, 0, 0],   \"to\": [16, 16, 16],   \"faces\": {       \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
    private Map<String, ResourceLocation> itemLocations = Maps.<String, ResourceLocation>newLinkedHashMap();
    private final Map<ResourceLocation, ModelBlockDefinition> blockDefinitions = Maps.<ResourceLocation, ModelBlockDefinition>newHashMap();
    private Map<Item, List<String>> variantNames = Maps.<Item, List<String>>newIdentityHashMap();
    private static final Map<Item, Set<String>> customVariantNames = Maps.<Item, Set<String>>newHashMap();

    public ModelBakery(IResourceManager resourceManager, TextureMap textureMap, BlockModelShapes blockModelShapes)
    {
        this.resourceManager = resourceManager;
        this.textureMap = textureMap;
        this.blockModelShapes = blockModelShapes;
    }

    public IRegistry<ModelResourceLocation, IBakedModel> setupModelRegistry()
    {
        this.loadVariantItemModels();
        this.loadModelsCheck();
        this.loadSprites();
        this.bakeItemModels();
        this.bakeBlockModels();
        return this.bakedRegistry;
    }

    private void loadVariantItemModels()
    {
        this.loadVariants(this.blockModelShapes.getBlockStateMapper().putAllStateModelLocations().values());
        this.variants.put(MODEL_MISSING, new ModelBlockDefinition.Variants(MODEL_MISSING.getVariant(), Lists.newArrayList(new ModelBlockDefinition.Variant[] {new ModelBlockDefinition.Variant(new ResourceLocation(MODEL_MISSING.getResourcePath()), ModelRotation.X0_Y0, false, 1)})));
        ResourceLocation resourceLocation = new ResourceLocation("item_frame");
        ModelBlockDefinition modelBlockDefinition = this.getModelBlockDefinition(resourceLocation);
        this.registerVariant(modelBlockDefinition, new ModelResourceLocation(resourceLocation, "normal"));
        this.registerVariant(modelBlockDefinition, new ModelResourceLocation(resourceLocation, "map"));
        this.loadVariantModels();
        this.loadItemModels();
    }

    private void loadVariants(Collection<ModelResourceLocation> modelLocations)
    {
        for (ModelResourceLocation modelResourceLocation : modelLocations)
        {
            try
            {
                ModelBlockDefinition modelBlockDefinition = this.getModelBlockDefinition(modelResourceLocation);

                try
                {
                    this.registerVariant(modelBlockDefinition, modelResourceLocation);
                }
                catch (Exception exception)
                {
                    LOGGER.warn((String)("Unable to load variant: " + modelResourceLocation.getVariant() + " from " + modelResourceLocation), (Throwable)exception);
                }
            }
            catch (Exception exception1)
            {
                LOGGER.warn((String)("Unable to load definition " + modelResourceLocation), (Throwable)exception1);
            }
        }
    }

    private void registerVariant(ModelBlockDefinition definition, ModelResourceLocation modelLocation)
    {
        this.variants.put(modelLocation, definition.getVariants(modelLocation.getVariant()));
    }

    private ModelBlockDefinition getModelBlockDefinition(ResourceLocation location)
    {
        ResourceLocation resourceLocation = this.getBlockStateLocation(location);
        ModelBlockDefinition modelBlockDefinition = this.blockDefinitions.get(resourceLocation);

        if (modelBlockDefinition == null)
        {
            List<ModelBlockDefinition> list = Lists.<ModelBlockDefinition>newArrayList();

            try
            {
                for (IResource iresource : this.resourceManager.getAllResources(resourceLocation))
                {
                    InputStream inputStream = null;

                    try
                    {
                        inputStream = iresource.getInputStream();
                        ModelBlockDefinition modelblockdefinition1 = ModelBlockDefinition.parseFromReader(new InputStreamReader(inputStream, Charsets.UTF_8));
                        list.add(modelblockdefinition1);
                    }
                    catch (Exception exception)
                    {
                        throw new RuntimeException("Encountered an exception when loading model definition of \'" + location + "\' from: \'" + iresource.getResourceLocation() + "\' in resourcepack: \'" + iresource.getResourcePackName() + "\'", exception);
                    }
                    finally
                    {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
            }
            catch (IOException iOException)
            {
                throw new RuntimeException("Encountered an exception when loading model definition of model " + resourceLocation.toString(), iOException);
            }

            modelBlockDefinition = new ModelBlockDefinition(list);
            this.blockDefinitions.put(resourceLocation, modelBlockDefinition);
        }

        return modelBlockDefinition;
    }

    private ResourceLocation getBlockStateLocation(ResourceLocation location)
    {
        return new ResourceLocation(location.getResourceDomain(), "blockstates/" + location.getResourcePath() + ".json");
    }

    private void loadVariantModels()
    {
        for (ModelResourceLocation modelResourceLocation : this.variants.keySet())
        {
            for (ModelBlockDefinition.Variant modelblockdefinition$variant : this.variants.get(modelResourceLocation).getVariants())
            {
                ResourceLocation resourceLocation = modelblockdefinition$variant.getModelLocation();

                if (this.models.get(resourceLocation) == null)
                {
                    try
                    {
                        ModelBlock modelBlock = this.loadModel(resourceLocation);
                        this.models.put(resourceLocation, modelBlock);
                    }
                    catch (Exception exception)
                    {
                        LOGGER.warn((String)("Unable to load block model: \'" + resourceLocation + "\' for variant: \'" + modelResourceLocation + "\'"), (Throwable)exception);
                    }
                }
            }
        }
    }

    private ModelBlock loadModel(ResourceLocation location) throws IOException
    {
        String s = location.getResourcePath();

        if ("builtin/generated".equals(s))
        {
            return MODEL_GENERATED;
        }
        else if ("builtin/compass".equals(s))
        {
            return MODEL_COMPASS;
        }
        else if ("builtin/clock".equals(s))
        {
            return MODEL_CLOCK;
        }
        else if ("builtin/entity".equals(s))
        {
            return MODEL_ENTITY;
        }
        else
        {
            Reader reader;

            if (s.startsWith("builtin/"))
            {
                String builtinName = s.substring("builtin/".length());
                String builtinModelJson = BUILT_IN_MODELS.get(builtinName);

                if (builtinModelJson == null)
                {
                    throw new FileNotFoundException(location.toString());
                }

                reader = new StringReader(builtinModelJson);
            }
            else
            {
                location = this.getModelLocation(location);
                IResource iresource = this.resourceManager.getResource(location);
                reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
            }

            ModelBlock modelblock;

            try
            {
                ModelBlock modelblock1 = ModelBlock.deserialize(reader);
                modelblock1.name = location.toString();
                modelblock = modelblock1;
                String fifthStringValue = TextureUtils.getBasePath(location.getResourcePath());
                fixModelLocations(modelblock1, fifthStringValue);
            }
            finally
            {
                reader.close();
            }

            return modelblock;
        }
    }

    private ResourceLocation getModelLocation(ResourceLocation location)
    {
        ResourceLocation resourceLocation = location;
        String s = location.getResourcePath();

        if (!s.startsWith("mcpatcher") && !s.startsWith("optifine"))
        {
            return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
        }
        else
        {
            if (!s.endsWith(".json"))
            {
                resourceLocation = new ResourceLocation(location.getResourceDomain(), s + ".json");
            }

            return resourceLocation;
        }
    }

    private void loadItemModels()
    {
        this.registerVariantNames();

        for (Item item : Item.itemRegistry)
        {
            for (String s : this.getVariantNames(item))
            {
                ResourceLocation resourceLocation = this.getItemLocation(s);
                this.itemLocations.put(s, resourceLocation);

                if (this.models.get(resourceLocation) == null)
                {
                    try
                    {
                        ModelBlock modelBlock = this.loadModel(resourceLocation);
                        this.models.put(resourceLocation, modelBlock);
                    }
                    catch (Exception exception)
                    {
                        LOGGER.warn((String)("Unable to load item model: \'" + resourceLocation + "\' for item: \'" + Item.itemRegistry.getNameForObject(item) + "\'"), (Throwable)exception);
                    }
                }
            }
        }
    }

    public void loadItemModel(String name, ResourceLocation itemLocation, ResourceLocation itemResource)
    {
        this.itemLocations.put(name, itemLocation);

        if (this.models.get(itemLocation) == null)
        {
            try
            {
                ModelBlock modelBlock = this.loadModel(itemLocation);
                this.models.put(itemLocation, modelBlock);
            }
            catch (Exception exception)
            {
                LOGGER.warn("Unable to load item model: \'{}\' for item: \'{}\'", new Object[] {itemLocation, itemResource});
                LOGGER.warn(exception.getClass().getName() + ": " + exception.getMessage());
            }
        }
    }

    private void registerVariantNames()
    {
        this.variantNames.clear();
        this.variantNames.put(Item.getItemFromBlock(Blocks.stone), Lists.newArrayList(new String[] {"stone", "granite", "granite_smooth", "diorite", "diorite_smooth", "andesite", "andesite_smooth"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.dirt), Lists.newArrayList(new String[] {"dirt", "coarse_dirt", "podzol"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.planks), Lists.newArrayList(new String[] {"oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.sapling), Lists.newArrayList(new String[] {"oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.sand), Lists.newArrayList(new String[] {"sand", "red_sand"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.log), Lists.newArrayList(new String[] {"oak_log", "spruce_log", "birch_log", "jungle_log"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.leaves), Lists.newArrayList(new String[] {"oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.sponge), Lists.newArrayList(new String[] {"sponge", "sponge_wet"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.sandstone), Lists.newArrayList(new String[] {"sandstone", "chiseled_sandstone", "smooth_sandstone"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.red_sandstone), Lists.newArrayList(new String[] {"red_sandstone", "chiseled_red_sandstone", "smooth_red_sandstone"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.tallgrass), Lists.newArrayList(new String[] {"dead_bush", "tall_grass", "fern"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.deadbush), Lists.newArrayList(new String[] {"dead_bush"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.wool), Lists.newArrayList(new String[] {"black_wool", "red_wool", "green_wool", "brown_wool", "blue_wool", "purple_wool", "cyan_wool", "silver_wool", "gray_wool", "pink_wool", "lime_wool", "yellow_wool", "light_blue_wool", "magenta_wool", "orange_wool", "white_wool"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.yellow_flower), Lists.newArrayList(new String[] {"dandelion"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.red_flower), Lists.newArrayList(new String[] {"poppy", "blue_orchid", "allium", "houstonia", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.stone_slab), Lists.newArrayList(new String[] {"stone_slab", "sandstone_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.stone_slab2), Lists.newArrayList(new String[] {"red_sandstone_slab"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.stained_glass), Lists.newArrayList(new String[] {"black_stained_glass", "red_stained_glass", "green_stained_glass", "brown_stained_glass", "blue_stained_glass", "purple_stained_glass", "cyan_stained_glass", "silver_stained_glass", "gray_stained_glass", "pink_stained_glass", "lime_stained_glass", "yellow_stained_glass", "light_blue_stained_glass", "magenta_stained_glass", "orange_stained_glass", "white_stained_glass"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.monster_egg), Lists.newArrayList(new String[] {"stone_monster_egg", "cobblestone_monster_egg", "stone_brick_monster_egg", "mossy_brick_monster_egg", "cracked_brick_monster_egg", "chiseled_brick_monster_egg"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.stonebrick), Lists.newArrayList(new String[] {"stonebrick", "mossy_stonebrick", "cracked_stonebrick", "chiseled_stonebrick"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.wooden_slab), Lists.newArrayList(new String[] {"oak_slab", "spruce_slab", "birch_slab", "jungle_slab", "acacia_slab", "dark_oak_slab"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.cobblestone_wall), Lists.newArrayList(new String[] {"cobblestone_wall", "mossy_cobblestone_wall"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.anvil), Lists.newArrayList(new String[] {"anvil_intact", "anvil_slightly_damaged", "anvil_very_damaged"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.quartz_block), Lists.newArrayList(new String[] {"quartz_block", "chiseled_quartz_block", "quartz_column"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.stained_hardened_clay), Lists.newArrayList(new String[] {"black_stained_hardened_clay", "red_stained_hardened_clay", "green_stained_hardened_clay", "brown_stained_hardened_clay", "blue_stained_hardened_clay", "purple_stained_hardened_clay", "cyan_stained_hardened_clay", "silver_stained_hardened_clay", "gray_stained_hardened_clay", "pink_stained_hardened_clay", "lime_stained_hardened_clay", "yellow_stained_hardened_clay", "light_blue_stained_hardened_clay", "magenta_stained_hardened_clay", "orange_stained_hardened_clay", "white_stained_hardened_clay"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.stained_glass_pane), Lists.newArrayList(new String[] {"black_stained_glass_pane", "red_stained_glass_pane", "green_stained_glass_pane", "brown_stained_glass_pane", "blue_stained_glass_pane", "purple_stained_glass_pane", "cyan_stained_glass_pane", "silver_stained_glass_pane", "gray_stained_glass_pane", "pink_stained_glass_pane", "lime_stained_glass_pane", "yellow_stained_glass_pane", "light_blue_stained_glass_pane", "magenta_stained_glass_pane", "orange_stained_glass_pane", "white_stained_glass_pane"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.leaves2), Lists.newArrayList(new String[] {"acacia_leaves", "dark_oak_leaves"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.log2), Lists.newArrayList(new String[] {"acacia_log", "dark_oak_log"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.prismarine), Lists.newArrayList(new String[] {"prismarine", "prismarine_bricks", "dark_prismarine"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.carpet), Lists.newArrayList(new String[] {"black_carpet", "red_carpet", "green_carpet", "brown_carpet", "blue_carpet", "purple_carpet", "cyan_carpet", "silver_carpet", "gray_carpet", "pink_carpet", "lime_carpet", "yellow_carpet", "light_blue_carpet", "magenta_carpet", "orange_carpet", "white_carpet"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.double_plant), Lists.newArrayList(new String[] {"sunflower", "syringa", "double_grass", "double_fern", "double_rose", "paeonia"}));
        this.variantNames.put(Items.bow, Lists.newArrayList(new String[] {"bow", "bow_pulling_0", "bow_pulling_1", "bow_pulling_2"}));
        this.variantNames.put(Items.coal, Lists.newArrayList(new String[] {"coal", "charcoal"}));
        this.variantNames.put(Items.fishing_rod, Lists.newArrayList(new String[] {"fishing_rod", "fishing_rod_cast"}));
        this.variantNames.put(Items.fish, Lists.newArrayList(new String[] {"cod", "salmon", "clownfish", "pufferfish"}));
        this.variantNames.put(Items.cooked_fish, Lists.newArrayList(new String[] {"cooked_cod", "cooked_salmon"}));
        this.variantNames.put(Items.dye, Lists.newArrayList(new String[] {"dye_black", "dye_red", "dye_green", "dye_brown", "dye_blue", "dye_purple", "dye_cyan", "dye_silver", "dye_gray", "dye_pink", "dye_lime", "dye_yellow", "dye_light_blue", "dye_magenta", "dye_orange", "dye_white"}));
        this.variantNames.put(Items.potionitem, Lists.newArrayList(new String[] {"bottle_drinkable", "bottle_splash"}));
        this.variantNames.put(Items.skull, Lists.newArrayList(new String[] {"skull_skeleton", "skull_wither", "skull_zombie", "skull_char", "skull_creeper"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.oak_fence_gate), Lists.newArrayList(new String[] {"oak_fence_gate"}));
        this.variantNames.put(Item.getItemFromBlock(Blocks.oak_fence), Lists.newArrayList(new String[] {"oak_fence"}));
        this.variantNames.put(Items.oak_door, Lists.newArrayList(new String[] {"oak_door"}));

        for (Entry<Item, Set<String>> entry : customVariantNames.entrySet())
        {
            this.variantNames.put(entry.getKey(), Lists.newArrayList(entry.getValue().iterator()));
        }

        CustomItems.update();
        CustomItems.loadModels(this);
    }

    private List<String> getVariantNames(Item item)
    {
        List<String> list = (List)this.variantNames.get(item);

        if (list == null)
        {
            list = Collections.<String>singletonList(((ResourceLocation)Item.itemRegistry.getNameForObject(item)).toString());
        }

        return list;
    }

    private ResourceLocation getItemLocation(String path)
    {
        ResourceLocation resourceLocation = new ResourceLocation(path);
        return new ResourceLocation(resourceLocation.getResourceDomain(), "item/" + resourceLocation.getResourcePath());
    }

    private void bakeBlockModels()
    {
        for (ModelResourceLocation modelresourcelocation : this.variants.keySet())
        {
            WeightedBakedModel.Builder weightedbakedmodel$builder = new WeightedBakedModel.Builder();
            int i = 0;

            for (ModelBlockDefinition.Variant modelblockdefinition$variant : this.variants.get(modelresourcelocation).getVariants())
            {
                ModelBlock modelblock = this.models.get(modelblockdefinition$variant.getModelLocation());

                if (modelblock != null && modelblock.isResolved())
                {
                    ++i;
                    weightedbakedmodel$builder.add(this.bakeModel(modelblock, modelblockdefinition$variant.getRotation(), modelblockdefinition$variant.isUvLocked()), modelblockdefinition$variant.getWeight());
                }
                else
                {
                    LOGGER.warn("Missing model for: " + modelresourcelocation);
                }
            }

            if (i == 0)
            {
                LOGGER.warn("No weighted models for: " + modelresourcelocation);
            }
            else if (i == 1)
            {
                this.bakedRegistry.putObject(modelresourcelocation, weightedbakedmodel$builder.first());
            }
            else
            {
                this.bakedRegistry.putObject(modelresourcelocation, weightedbakedmodel$builder.build());
            }
        }

        for (Entry<String, ResourceLocation> entry : this.itemLocations.entrySet())
        {
            ResourceLocation resourcelocation = (ResourceLocation)entry.getValue();
            ModelResourceLocation modelresourcelocation1 = new ModelResourceLocation((String)entry.getKey(), "inventory");

            ModelBlock modelblock1 = this.models.get(resourcelocation);

            if (modelblock1 != null && modelblock1.isResolved())
            {
                if (this.isCustomRenderer(modelblock1))
                {
                    this.bakedRegistry.putObject(modelresourcelocation1, new BuiltInModel(modelblock1.getAllTransforms()));
                }
                else
                {
                    this.bakedRegistry.putObject(modelresourcelocation1, this.bakeModel(modelblock1, ModelRotation.X0_Y0, false));
                }
            }
            else
            {
                LOGGER.warn("Missing model for: " + resourcelocation);
            }
        }
    }

    private Set<ResourceLocation> getVariantsTextureLocations()
    {
        Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
        List<ModelResourceLocation> list = Lists.newArrayList(this.variants.keySet());
        Collections.sort(list, new Comparator<ModelResourceLocation>()
        {
            public int compare(ModelResourceLocation first, ModelResourceLocation second)
            {
                return first.toString().compareTo(second.toString());
            }
        });

        for (ModelResourceLocation modelResourceLocation : list)
        {
            ModelBlockDefinition.Variants modelblockdefinition$variants = this.variants.get(modelResourceLocation);

            for (ModelBlockDefinition.Variant modelblockdefinition$variant : modelblockdefinition$variants.getVariants())
            {
                ModelBlock modelBlock = this.models.get(modelblockdefinition$variant.getModelLocation());

                if (modelBlock == null)
                {
                    LOGGER.warn("Missing model for: " + modelResourceLocation);
                }
                else
                {
                    set.addAll(this.getTextureLocations(modelBlock));
                }
            }
        }

        set.addAll(LOCATIONS_BUILTIN_TEXTURES);
        return set;
    }

    public IBakedModel bakeModel(ModelBlock modelBlockIn, ModelRotation modelRotationIn, boolean uvLocked)
    {
        return this.bakeModel(modelBlockIn, (ITransformation) modelRotationIn, uvLocked);
    }

    protected IBakedModel bakeModel(ModelBlock model, ITransformation transform, boolean uvLocked)
    {
        TextureAtlasSprite textureatlassprite = this.sprites.get(new ResourceLocation(model.resolveTextureName("particle")));
        SimpleBakedModel.Builder simplebakedmodel$builder = (new SimpleBakedModel.Builder(model)).setTexture(textureatlassprite);

        for (BlockPart blockpart : model.getElements())
        {
            for (EnumFacing enumfacing : blockpart.mapFaces.keySet())
            {
                BlockPartFace blockpartface = blockpart.mapFaces.get(enumfacing);
                TextureAtlasSprite textureatlassprite1 = this.sprites.get(new ResourceLocation(model.resolveTextureName(blockpartface.texture)));
                boolean flag = true;

                if (blockpartface.cullFace != null && flag)
                {
                    simplebakedmodel$builder.addFaceQuad(transform.rotate(blockpartface.cullFace), this.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing, transform, uvLocked));
                }
                else
                {
                    simplebakedmodel$builder.addGeneralQuad(this.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing, transform, uvLocked));
                }
            }
        }

        return simplebakedmodel$builder.makeBakedModel();
    }

    private BakedQuad makeBakedQuad(BlockPart part, BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, ModelRotation rotation, boolean uvLocked)
    {
        return this.faceBakery.makeBakedQuad(part.positionFrom, part.positionTo, face, sprite, facing, rotation, part.partRotation, uvLocked, part.shade);
    }

    protected BakedQuad makeBakedQuad(BlockPart part, BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, ITransformation transform, boolean uvLocked)
    {
        return this.faceBakery.makeBakedQuad(part.positionFrom, part.positionTo, face, sprite, facing, transform, part.partRotation, uvLocked, part.shade);
    }

    private void loadModelsCheck()
    {
        this.loadModels();

        for (ModelBlock modelBlock : this.models.values())
        {
            modelBlock.getParentFromMap(this.models);
        }

        ModelBlock.checkModelHierarchy(this.models);
    }

    private void loadModels()
    {
        Deque<ResourceLocation> deque = Queues.<ResourceLocation>newArrayDeque();
        Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

        for (ResourceLocation resourceLocation : this.models.keySet())
        {
            set.add(resourceLocation);
            ResourceLocation resourcelocation1 = this.models.get(resourceLocation).getParentLocation();

            if (resourcelocation1 != null)
            {
                deque.add(resourcelocation1);
            }
        }

        while (!deque.isEmpty())
        {
            ResourceLocation resourcelocation2 = deque.pop();

            try
            {
                if (this.models.get(resourcelocation2) != null)
                {
                    continue;
                }

                ModelBlock modelblock = this.loadModel(resourcelocation2);
                this.models.put(resourcelocation2, modelblock);
                ResourceLocation resourcelocation3 = modelblock.getParentLocation();

                if (resourcelocation3 != null && !set.contains(resourcelocation3))
                {
                    deque.add(resourcelocation3);
                }
            }
            catch (Exception caughtException)
            {
                LOGGER.warn("In parent chain: " + JOINER.join(this.getParentPath(resourcelocation2)) + "; unable to load model: \'" + resourcelocation2 + "\'");
            }

            set.add(resourcelocation2);
        }
    }

    private List<ResourceLocation> getParentPath(ResourceLocation location)
    {
        List<ResourceLocation> list = Lists.newArrayList(new ResourceLocation[] {location});
        ResourceLocation resourceLocation = location;

        while ((resourceLocation = this.getParentLocation(resourceLocation)) != null)
        {
            list.add(0, resourceLocation);
        }

        return list;
    }

    private ResourceLocation getParentLocation(ResourceLocation location)
    {
        for (Entry<ResourceLocation, ModelBlock> entry : this.models.entrySet())
        {
            ModelBlock modelBlock = entry.getValue();

            if (modelBlock != null && location.equals(modelBlock.getParentLocation()))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    private Set<ResourceLocation> getTextureLocations(ModelBlock model)
    {
        Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

        for (BlockPart blockPart : model.getElements())
        {
            for (BlockPartFace blockPartFace : blockPart.mapFaces.values())
            {
                ResourceLocation resourceLocation = new ResourceLocation(model.resolveTextureName(blockPartFace.texture));
                set.add(resourceLocation);
            }
        }

        set.add(new ResourceLocation(model.resolveTextureName("particle")));
        return set;
    }

    private void loadSprites()
    {
        final Set<ResourceLocation> set = this.getVariantsTextureLocations();
        set.addAll(this.getItemsTextureLocations());
        set.remove(TextureMap.LOCATION_MISSING_TEXTURE);
        IIconCreator iiconcreator = new IIconCreator()
        {
            public void registerSprites(TextureMap iconRegistry)
            {
                for (ResourceLocation resourceLocation : set)
                {
                    TextureAtlasSprite textureAtlasSprite = iconRegistry.registerSprite(resourceLocation);
                    ModelBakery.this.sprites.put(resourceLocation, textureAtlasSprite);
                }
            }
        };
        this.textureMap.loadSprites(this.resourceManager, iiconcreator);
        this.sprites.put(new ResourceLocation("missingno"), this.textureMap.getMissingSprite());
    }

    private Set<ResourceLocation> getItemsTextureLocations()
    {
        Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

        for (ResourceLocation resourceLocation : this.itemLocations.values())
        {
            ModelBlock modelBlock = this.models.get(resourceLocation);

            if (modelBlock != null)
            {
                set.add(new ResourceLocation(modelBlock.resolveTextureName("particle")));

                if (this.hasItemModel(modelBlock))
                {
                    for (String s : ItemModelGenerator.LAYERS)
                    {
                        ResourceLocation resourceLocation2 = new ResourceLocation(modelBlock.resolveTextureName(s));

                        if (modelBlock.getRootModel() == MODEL_COMPASS && !TextureMap.LOCATION_MISSING_TEXTURE.equals(resourceLocation2))
                        {
                            TextureAtlasSprite.setLocationNameCompass(resourceLocation2.toString());
                        }
                        else if (modelBlock.getRootModel() == MODEL_CLOCK && !TextureMap.LOCATION_MISSING_TEXTURE.equals(resourceLocation2))
                        {
                            TextureAtlasSprite.setLocationNameClock(resourceLocation2.toString());
                        }

                        set.add(resourceLocation2);
                    }
                }
                else if (!this.isCustomRenderer(modelBlock))
                {
                    for (BlockPart blockPart : modelBlock.getElements())
                    {
                        for (BlockPartFace blockPartFace : blockPart.mapFaces.values())
                        {
                            ResourceLocation resourcelocation1 = new ResourceLocation(modelBlock.resolveTextureName(blockPartFace.texture));
                            set.add(resourcelocation1);
                        }
                    }
                }
            }
        }

        return set;
    }

    private boolean hasItemModel(ModelBlock model)
    {
        if (model == null)
        {
            return false;
        }
        else
        {
            ModelBlock modelBlock = model.getRootModel();
            return modelBlock == MODEL_GENERATED || modelBlock == MODEL_COMPASS || modelBlock == MODEL_CLOCK;
        }
    }

    private boolean isCustomRenderer(ModelBlock model)
    {
        if (model == null)
        {
            return false;
        }
        else
        {
            ModelBlock modelBlock = model.getRootModel();
            return modelBlock == MODEL_ENTITY;
        }
    }

    private void bakeItemModels()
    {
        for (ResourceLocation resourceLocation : this.itemLocations.values())
        {
            ModelBlock modelBlock = this.models.get(resourceLocation);

            if (this.hasItemModel(modelBlock))
            {
                ModelBlock modelblock1 = this.makeItemModel(modelBlock);

                if (modelblock1 != null)
                {
                    modelblock1.name = resourceLocation.toString();
                }

                this.models.put(resourceLocation, modelblock1);
            }
            else if (this.isCustomRenderer(modelBlock))
            {
                this.models.put(resourceLocation, modelBlock);
            }
        }

        for (TextureAtlasSprite textureAtlasSprite : this.sprites.values())
        {
            if (!textureAtlasSprite.hasAnimationMetadata())
            {
                textureAtlasSprite.clearFramesTextureData();
            }
        }
    }

    private ModelBlock makeItemModel(ModelBlock model)
    {
        return this.itemModelGenerator.makeItemModel(this.textureMap, model);
    }

    public ModelBlock getModelBlock(ResourceLocation location)
    {
        ModelBlock modelBlock = this.models.get(location);
        return modelBlock;
    }

    public static void fixModelLocations(ModelBlock model, String basePath)
    {
        ResourceLocation resourcelocation = fixModelLocation(model.getParentLocation(), basePath);

        if (resourcelocation != model.getParentLocation())
        {
            model.parentLocation = resourcelocation;
        }

        Map<String, String> map = model.textures;

        if (map != null)
        {
            for (Entry<String, String> entry : map.entrySet())
            {
                String s = (String)entry.getValue();
                String stringValue = fixResourcePath(s, basePath);

                if (!stringValue.equals(s))
                {
                    entry.setValue(stringValue);
                }
            }
        }
    }

    public static ResourceLocation fixModelLocation(ResourceLocation location, String basePath)
    {
        if (location != null && basePath != null)
        {
            if (!location.getResourceDomain().equals("minecraft"))
            {
                return location;
            }
            else
            {
                String s = location.getResourcePath();
                String secondStringValue = fixResourcePath(s, basePath);

                if (secondStringValue != s)
                {
                    location = new ResourceLocation(location.getResourceDomain(), secondStringValue);
                }

                return location;
            }
        }
        else
        {
            return location;
        }
    }

    private static String fixResourcePath(String path, String basePath)
    {
        path = TextureUtils.fixResourcePath(path, basePath);
        path = StrUtils.removeSuffix(path, ".json");
        path = StrUtils.removeSuffix(path, ".png");
        return path;
    }

    @Deprecated
    public static void addVariantName(Item item, String... names)
    {
        Set<String> variants = customVariantNames.get(item);

        if (variants != null)
        {
            variants.addAll(Lists.newArrayList(names));
        }
        else
        {
            customVariantNames.put(item, Sets.newHashSet(names));
        }
    }

    public static <T extends ResourceLocation> void registerItemVariants(Item item, T... variants)
    {
        if (!customVariantNames.containsKey(item))
        {
            customVariantNames.put(item, Sets.<String>newHashSet());
        }

        Set<String> variantNames = customVariantNames.get(item);

        for (ResourceLocation resourcelocation : variants)
        {
            variantNames.add(resourcelocation.toString());
        }
    }

    static
    {
        BUILT_IN_MODELS.put("missing", "{ \"textures\": {   \"particle\": \"missingno\",   \"missingno\": \"missingno\"}, \"elements\": [ {     \"from\": [ 0, 0, 0 ],     \"to\": [ 16, 16, 16 ],     \"faces\": {         \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"down\", \"texture\": \"#missingno\" },         \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"up\", \"texture\": \"#missingno\" },         \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"north\", \"texture\": \"#missingno\" },         \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"south\", \"texture\": \"#missingno\" },         \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"west\", \"texture\": \"#missingno\" },         \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"cullface\": \"east\", \"texture\": \"#missingno\" }    }}]}");
        MODEL_GENERATED.name = "generation marker";
        MODEL_COMPASS.name = "compass generation marker";
        MODEL_CLOCK.name = "class generation marker";
        MODEL_ENTITY.name = "block entity marker";
    }
}
