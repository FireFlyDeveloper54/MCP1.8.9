package net.optifine;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.render.RenderEnv;
import net.optifine.util.EntityUtils;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CustomColors
{
    private static String paletteFormatDefault = "vanilla";
    private static CustomColormap waterColors = null;
    private static CustomColormap foliagePineColors = null;
    private static CustomColormap foliageBirchColors = null;
    private static CustomColormap swampFoliageColors = null;
    private static CustomColormap swampGrassColors = null;
    private static CustomColormap[] colorsBlockColormaps = null;
    private static CustomColormap[][] blockColormaps = (CustomColormap[][])null;
    private static CustomColormap skyColors = null;
    private static CustomColorFader skyColorFader = new CustomColorFader();
    private static CustomColormap fogColors = null;
    private static CustomColorFader fogColorFader = new CustomColorFader();
    private static CustomColormap underwaterColors = null;
    private static CustomColorFader underwaterColorFader = new CustomColorFader();
    private static CustomColormap underlavaColors = null;
    private static CustomColorFader underlavaColorFader = new CustomColorFader();
    private static LightMapPack[] lightMapPacks = null;
    private static int lightmapMinDimensionId = 0;
    private static CustomColormap redstoneColors = null;
    private static CustomColormap xpOrbColors = null;
    private static int xpOrbTime = -1;
    private static CustomColormap durabilityColors = null;
    private static CustomColormap stemColors = null;
    private static CustomColormap stemMelonColors = null;
    private static CustomColormap stemPumpkinColors = null;
    private static CustomColormap myceliumParticleColors = null;
    private static boolean useDefaultGrassFoliageColors = true;
    private static int particleWaterColor = -1;
    private static int particlePortalColor = -1;
    private static int lilyPadColor = -1;
    private static int expBarTextColor = -1;
    private static int bossTextColor = -1;
    private static int signTextColor = -1;
    private static Vec3 fogColorNether = null;
    private static Vec3 fogColorEnd = null;
    private static Vec3 skyColorEnd = null;
    private static int[] spawnEggPrimaryColors = null;
    private static int[] spawnEggSecondaryColors = null;
    private static float[][] wolfCollarColors = (float[][])null;
    private static float[][] sheepColors = (float[][])null;
    private static int[] textColors = null;
    private static int[] mapColorsOriginal = null;
    private static int[] potionColors = null;
    private static final IBlockState BLOCK_STATE_DIRT = Blocks.dirt.getDefaultState();
    private static final IBlockState BLOCK_STATE_WATER = Blocks.water.getDefaultState();
    public static Random random = new Random();
    private static final CustomColors.IColorizer COLORIZER_GRASS = new CustomColors.IColorizer()
    {
        public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos)
        {
            BiomeGenBase biomeGenBase = CustomColors.getColorBiome(blockAccess, blockPos);
            return CustomColors.swampGrassColors != null && biomeGenBase == BiomeGenBase.swampland ? CustomColors.swampGrassColors.getColor(biomeGenBase, blockPos) : biomeGenBase.getGrassColorAtPos(blockPos);
        }
        public boolean isColorConstant()
        {
            return false;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_FOLIAGE = new CustomColors.IColorizer()
    {
        public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos)
        {
            BiomeGenBase biomeGenBase = CustomColors.getColorBiome(blockAccess, blockPos);
            return CustomColors.swampFoliageColors != null && biomeGenBase == BiomeGenBase.swampland ? CustomColors.swampFoliageColors.getColor(biomeGenBase, blockPos) : biomeGenBase.getFoliageColorAtPos(blockPos);
        }
        public boolean isColorConstant()
        {
            return false;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_FOLIAGE_PINE = new CustomColors.IColorizer()
    {
        public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos)
        {
            return CustomColors.foliagePineColors != null ? CustomColors.foliagePineColors.getColor(blockAccess, blockPos) : ColorizerFoliage.getFoliageColorPine();
        }
        public boolean isColorConstant()
        {
            return CustomColors.foliagePineColors == null;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_FOLIAGE_BIRCH = new CustomColors.IColorizer()
    {
        public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos)
        {
            return CustomColors.foliageBirchColors != null ? CustomColors.foliageBirchColors.getColor(blockAccess, blockPos) : ColorizerFoliage.getFoliageColorBirch();
        }
        public boolean isColorConstant()
        {
            return CustomColors.foliageBirchColors == null;
        }
    };
    private static final CustomColors.IColorizer COLORIZER_WATER = new CustomColors.IColorizer()
    {
        public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos)
        {
            BiomeGenBase biomegenbase = CustomColors.getColorBiome(blockAccess, blockPos);
            return CustomColors.waterColors != null ? CustomColors.waterColors.getColor(biomegenbase, blockPos) : biomegenbase.waterColorMultiplier;
        }
        public boolean isColorConstant()
        {
            return false;
        }
    };

    public static void update()
    {
        paletteFormatDefault = "vanilla";
        waterColors = null;
        foliageBirchColors = null;
        foliagePineColors = null;
        swampGrassColors = null;
        swampFoliageColors = null;
        skyColors = null;
        fogColors = null;
        underwaterColors = null;
        underlavaColors = null;
        redstoneColors = null;
        xpOrbColors = null;
        xpOrbTime = -1;
        durabilityColors = null;
        stemColors = null;
        myceliumParticleColors = null;
        lightMapPacks = null;
        particleWaterColor = -1;
        particlePortalColor = -1;
        lilyPadColor = -1;
        expBarTextColor = -1;
        bossTextColor = -1;
        signTextColor = -1;
        fogColorNether = null;
        fogColorEnd = null;
        skyColorEnd = null;
        colorsBlockColormaps = null;
        blockColormaps = (CustomColormap[][])null;
        useDefaultGrassFoliageColors = true;
        spawnEggPrimaryColors = null;
        spawnEggSecondaryColors = null;
        wolfCollarColors = (float[][])null;
        sheepColors = (float[][])null;
        textColors = null;
        setMapColors(mapColorsOriginal);
        potionColors = null;
        paletteFormatDefault = getValidProperty("mcpatcher/color.properties", "palette.format", CustomColormap.FORMAT_STRINGS, "vanilla");
        String colormapBasePath = "mcpatcher/colormap/";
        String[] waterColorPaths = new String[] {"water.png", "watercolorX.png"};
        waterColors = getCustomColors(colormapBasePath, waterColorPaths, 256, 256);
        updateUseDefaultGrassFoliageColors();

        if (Config.isCustomColors())
        {
            String[] pineColorPaths = new String[] {"pine.png", "pinecolor.png"};
            foliagePineColors = getCustomColors(colormapBasePath, pineColorPaths, 256, 256);
            String[] birchColorPaths = new String[] {"birch.png", "birchcolor.png"};
            foliageBirchColors = getCustomColors(colormapBasePath, birchColorPaths, 256, 256);
            String[] swampGrassColorPaths = new String[] {"swampgrass.png", "swampgrasscolor.png"};
            swampGrassColors = getCustomColors(colormapBasePath, swampGrassColorPaths, 256, 256);
            String[] swampFoliageColorPaths = new String[] {"swampfoliage.png", "swampfoliagecolor.png"};
            swampFoliageColors = getCustomColors(colormapBasePath, swampFoliageColorPaths, 256, 256);
            String[] skyColorPaths = new String[] {"sky0.png", "skycolor0.png"};
            skyColors = getCustomColors(colormapBasePath, skyColorPaths, 256, 256);
            String[] fogColorPaths = new String[] {"fog0.png", "fogcolor0.png"};
            fogColors = getCustomColors(colormapBasePath, fogColorPaths, 256, 256);
            String[] underwaterColorPaths = new String[] {"underwater.png", "underwatercolor.png"};
            underwaterColors = getCustomColors(colormapBasePath, underwaterColorPaths, 256, 256);
            String[] underlavaColorPaths = new String[] {"underlava.png", "underlavacolor.png"};
            underlavaColors = getCustomColors(colormapBasePath, underlavaColorPaths, 256, 256);
            String[] redstoneColorPaths = new String[] {"redstone.png", "redstonecolor.png"};
            redstoneColors = getCustomColors(colormapBasePath, redstoneColorPaths, 16, 1);
            xpOrbColors = getCustomColors(colormapBasePath + "xporb.png", -1, -1);
            durabilityColors = getCustomColors(colormapBasePath + "durability.png", -1, -1);
            String[] stemColorPaths = new String[] {"stem.png", "stemcolor.png"};
            stemColors = getCustomColors(colormapBasePath, stemColorPaths, 8, 1);
            stemPumpkinColors = getCustomColors(colormapBasePath + "pumpkinstem.png", 8, 1);
            stemMelonColors = getCustomColors(colormapBasePath + "melonstem.png", 8, 1);
            String[] myceliumParticleColorPaths = new String[] {"myceliumparticle.png", "myceliumparticlecolor.png"};
            myceliumParticleColors = getCustomColors(colormapBasePath, myceliumParticleColorPaths, -1, -1);
            Pair<LightMapPack[], Integer> pair = parseLightMapPacks();
            lightMapPacks = (LightMapPack[])pair.getLeft();
            lightmapMinDimensionId = ((Integer)pair.getRight()).intValue();
            readColorProperties("mcpatcher/color.properties");
            blockColormaps = readBlockColormaps(new String[] {colormapBasePath + "custom/", colormapBasePath + "blocks/"}, colorsBlockColormaps, 256, 256);
            updateUseDefaultGrassFoliageColors();
        }
    }

    private static String getValidProperty(String fileName, String key, String[] validValues, String valDef)
    {
        try
        {
            ResourceLocation resourceLocation = new ResourceLocation(fileName);
            InputStream inputStream = Config.getResourceStream(resourceLocation);

            if (inputStream == null)
            {
                return valDef;
            }
            else
            {
                Properties properties = new PropertiesOrdered();
                properties.load(inputStream);
                inputStream.close();
                String s = properties.getProperty(key);

                if (s == null)
                {
                    return valDef;
                }
                else
                {
                    List<String> list = Arrays.<String>asList(validValues);

                    if (!list.contains(s))
                    {
                        warn("Invalid value: " + key + "=" + s);
                        warn("Expected values: " + Config.arrayToString((Object[])validValues));
                        return valDef;
                    }
                    else
                    {
                        dbg("" + key + "=" + s);
                        return s;
                    }
                }
            }
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            return valDef;
        }
        catch (IOException iOException)
        {
            net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
            return valDef;
        }
    }

    private static Pair<LightMapPack[], Integer> parseLightMapPacks()
    {
        String lightmapPathPrefix = "mcpatcher/lightmap/world";
        String pngExtension = ".png";
        String[] lightmapPaths = ResUtils.collectFiles(lightmapPathPrefix, pngExtension);
        Map<Integer, String> pathsByDimensionId = new HashMap();

        for (int i = 0; i < lightmapPaths.length; ++i)
        {
            String lightmapPath = lightmapPaths[i];
            String dimensionIdText = StrUtils.removePrefixSuffix(lightmapPath, lightmapPathPrefix, pngExtension);
            int j = Config.parseInt(dimensionIdText, Integer.MIN_VALUE);

            if (j == Integer.MIN_VALUE)
            {
                warn("Invalid dimension ID: " + dimensionIdText + ", path: " + lightmapPath);
            }
            else
            {
                pathsByDimensionId.put(Integer.valueOf(j), lightmapPath);
            }
        }

        Set<Integer> dimensionIdSet = pathsByDimensionId.keySet();
        Integer[] dimensionIds = (Integer[])dimensionIdSet.toArray(new Integer[dimensionIdSet.size()]);
        Arrays.sort((Object[])dimensionIds);

        if (dimensionIds.length <= 0)
        {
            return new ImmutablePair((Object)null, Integer.valueOf(0));
        }
        else
        {
            int minDimensionId = dimensionIds[0].intValue();
            int maxDimensionId = dimensionIds[dimensionIds.length - 1].intValue();
            int dimensionCount = maxDimensionId - minDimensionId + 1;
            CustomColormap[] lightmapColormaps = new CustomColormap[dimensionCount];

            for (int l = 0; l < dimensionIds.length; ++l)
            {
                Integer dimensionId = dimensionIds[l];
                String lightmapPath = (String)pathsByDimensionId.get(dimensionId);
                CustomColormap lightmapColormap = getCustomColors(lightmapPath, -1, -1);

                if (lightmapColormap != null)
                {
                    if (lightmapColormap.getWidth() < 16)
                    {
                        warn("Invalid lightmap width: " + lightmapColormap.getWidth() + ", path: " + lightmapPath);
                    }
                    else
                    {
                        int dimensionIndex = dimensionId.intValue() - minDimensionId;
                        lightmapColormaps[dimensionIndex] = lightmapColormap;
                    }
                }
            }

            LightMapPack[] lightMapPacks = new LightMapPack[lightmapColormaps.length];

            for (int secondIntValue = 0; secondIntValue < lightmapColormaps.length; ++secondIntValue)
            {
                CustomColormap lightmapColormap = lightmapColormaps[secondIntValue];

                if (lightmapColormap != null)
                {
                    String lightmapName = lightmapColormap.name;
                    String lightmapBasePath = lightmapColormap.basePath;
                    CustomColormap rainColormap = getCustomColors(lightmapBasePath + "/" + lightmapName + "_rain.png", -1, -1);
                    CustomColormap thunderColormap = getCustomColors(lightmapBasePath + "/" + lightmapName + "_thunder.png", -1, -1);
                    LightMap baseLightMap = new LightMap(lightmapColormap);
                    LightMap rainLightMap = rainColormap != null ? new LightMap(rainColormap) : null;
                    LightMap thunderLightMap = thunderColormap != null ? new LightMap(thunderColormap) : null;
                    LightMapPack lightMapPack = new LightMapPack(baseLightMap, rainLightMap, thunderLightMap);
                    lightMapPacks[secondIntValue] = lightMapPack;
                }
            }

            return new ImmutablePair(lightMapPacks, Integer.valueOf(minDimensionId));
        }
    }

    private static int getTextureHeight(String path, int defHeight)
    {
        try
        {
            InputStream inputStream = Config.getResourceStream(new ResourceLocation(path));

            if (inputStream == null)
            {
                return defHeight;
            }
            else
            {
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                inputStream.close();
                return bufferedImage == null ? defHeight : bufferedImage.getHeight();
            }
        }
        catch (IOException caughtIoException)
        {
            return defHeight;
        }
    }

    private static void readColorProperties(String fileName)
    {
        try
        {
            ResourceLocation resourceLocation = new ResourceLocation(fileName);
            InputStream inputStream = Config.getResourceStream(resourceLocation);

            if (inputStream == null)
            {
                return;
            }

            dbg("Loading " + fileName);
            Properties properties = new PropertiesOrdered();
            properties.load(inputStream);
            inputStream.close();
            particleWaterColor = readColor(properties, new String[] {"particle.water", "drop.water"});
            particlePortalColor = readColor(properties, "particle.portal");
            lilyPadColor = readColor(properties, "lilypad");
            expBarTextColor = readColor(properties, "text.xpbar");
            bossTextColor = readColor(properties, "text.boss");
            signTextColor = readColor(properties, "text.sign");
            fogColorNether = readColorVec3(properties, "fog.nether");
            fogColorEnd = readColorVec3(properties, "fog.end");
            skyColorEnd = readColorVec3(properties, "sky.end");
            colorsBlockColormaps = readCustomColormaps(properties, fileName);
            spawnEggPrimaryColors = readSpawnEggColors(properties, fileName, "egg.shell.", "Spawn egg shell");
            spawnEggSecondaryColors = readSpawnEggColors(properties, fileName, "egg.spots.", "Spawn egg spot");
            wolfCollarColors = readDyeColors(properties, fileName, "collar.", "Wolf collar");
            sheepColors = readDyeColors(properties, fileName, "sheep.", "Sheep");
            textColors = readTextColors(properties, fileName, "text.code.", "Text");
            int[] mapColorValues = readMapColors(properties, fileName, "map.", "Map");

            if (mapColorValues != null)
            {
                if (mapColorsOriginal == null)
                {
                    mapColorsOriginal = getMapColors();
                }

                setMapColors(mapColorValues);
            }

            potionColors = readPotionColors(properties, fileName, "potion.", "Potion");
            xpOrbTime = Config.parseInt(properties.getProperty("xporb.time"), -1);
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            return;
        }
        catch (IOException iOException)
        {
            net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
        }
    }

    private static CustomColormap[] readCustomColormaps(Properties props, String fileName)
    {
        List list = new ArrayList();
        String palettePrefix = "palette.block.";
        Map map = new HashMap();

        for (Object o: props.keySet())
        {
            String propertyKey = (String) o;
            String propertyValue = props.getProperty(propertyKey);

            if (propertyKey.startsWith(palettePrefix))
            {
                map.put(propertyKey, propertyValue);
            }
        }

        String[] paletteKeys = (String[])((String[])map.keySet().toArray(new String[map.size()]));

        for (int j = 0; j < paletteKeys.length; ++j)
        {
            String paletteKey = paletteKeys[j];
            String paletteValue = props.getProperty(paletteKey);
            dbg("Block palette: " + paletteKey + " = " + paletteValue);
            String palettePath = paletteKey.substring(palettePrefix.length());
            String basePath = TextureUtils.getBasePath(fileName);
            palettePath = TextureUtils.fixResourcePath(palettePath, basePath);
            CustomColormap customColormap = getCustomColors(palettePath, 256, 256);

            if (customColormap == null)
            {
                warn("Colormap not found: " + palettePath);
            }
            else
            {
                ConnectedParser connectedParser = new ConnectedParser("CustomColors");
                MatchBlock[] matchBlocks = connectedParser.parseMatchBlocks(paletteValue);

                if (matchBlocks != null && matchBlocks.length > 0)
                {
                    for (int i = 0; i < matchBlocks.length; ++i)
                    {
                        MatchBlock matchBlock = matchBlocks[i];
                        customColormap.addMatchBlock(matchBlock);
                    }

                    list.add(customColormap);
                }
                else
                {
                    warn("Invalid match blocks: " + paletteValue);
                }
            }
        }

        if (list.size() <= 0)
        {
            return null;
        }
        else
        {
            CustomColormap[] colormaps = (CustomColormap[])((CustomColormap[])list.toArray(new CustomColormap[list.size()]));
            return colormaps;
        }
    }

    private static CustomColormap[][] readBlockColormaps(String[] basePaths, CustomColormap[] basePalettes, int width, int height)
    {
        String[] propertyPaths = ResUtils.collectFiles(basePaths, new String[] {".properties"});
        Arrays.sort((Object[])propertyPaths);
        List list = new ArrayList();

        for (int i = 0; i < propertyPaths.length; ++i)
        {
            String propertyPath = propertyPaths[i];
            dbg("Block colormap: " + propertyPath);

            try
            {
                ResourceLocation resourceLocation = new ResourceLocation("minecraft", propertyPath);
                InputStream inputStream = Config.getResourceStream(resourceLocation);

                if (inputStream == null)
                {
                    warn("File not found: " + propertyPath);
                }
                else
                {
                    Properties properties = new PropertiesOrdered();
                    properties.load(inputStream);
                    inputStream.close();
                    CustomColormap customColormap = new CustomColormap(properties, propertyPath, width, height, paletteFormatDefault);

                    if (customColormap.isValid(propertyPath) && customColormap.isValidMatchBlocks(propertyPath))
                    {
                        addToBlockList(customColormap, list);
                    }
                }
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                warn("File not found: " + propertyPath);
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }

        if (basePalettes != null)
        {
            for (int j = 0; j < basePalettes.length; ++j)
            {
                CustomColormap basePalette = basePalettes[j];
                addToBlockList(basePalette, list);
            }
        }

        if (list.size() <= 0)
        {
            return (CustomColormap[][])null;
        }
        else
        {
            CustomColormap[][] colormaps = blockListToArray(list);
            return colormaps;
        }
    }

    private static void addToBlockList(CustomColormap cm, List blockList)
    {
        int[] matchBlockIds = cm.getMatchBlockIds();

        if (matchBlockIds != null && matchBlockIds.length > 0)
        {
            for (int i = 0; i < matchBlockIds.length; ++i)
            {
                int blockId = matchBlockIds[i];

                if (blockId < 0)
                {
                    warn("Invalid block ID: " + blockId);
                }
                else
                {
                    addToList(cm, blockList, blockId);
                }
            }
        }
        else
        {
            warn("No match blocks: " + Config.arrayToString(matchBlockIds));
        }
    }

    private static void addToList(CustomColormap cm, List lists, int id)
    {
        while (id >= lists.size())
        {
            lists.add(null);
        }

        List list = (List)lists.get(id);

        if (list == null)
        {
            list = new ArrayList();
            lists.set(id, list);
        }

        list.add(cm);
    }

    private static CustomColormap[][] blockListToArray(List lists)
    {
        CustomColormap[][] colormapLists = new CustomColormap[lists.size()][];

        for (int i = 0; i < lists.size(); ++i)
        {
            List list = (List)lists.get(i);

            if (list != null)
            {
                CustomColormap[] colormaps = (CustomColormap[])((CustomColormap[])list.toArray(new CustomColormap[list.size()]));
                colormapLists[i] = colormaps;
            }
        }

        return colormapLists;
    }

    private static int readColor(Properties props, String[] names)
    {
        for (int i = 0; i < names.length; ++i)
        {
            String s = names[i];
            int j = readColor(props, s);

            if (j >= 0)
            {
                return j;
            }
        }

        return -1;
    }

    private static int readColor(Properties props, String name)
    {
        String s = props.getProperty(name);

        if (s == null)
        {
            return -1;
        }
        else
        {
            s = s.trim();
            int i = parseColor(s);

            if (i < 0)
            {
                warn("Invalid color: " + name + " = " + s);
                return i;
            }
            else
            {
                dbg(name + " = " + s);
                return i;
            }
        }
    }

    private static int parseColor(String str)
    {
        if (str == null)
        {
            return -1;
        }
        else
        {
            str = str.trim();

            try
            {
                int i = Integer.parseInt(str, 16) & 16777215;
                return i;
            }
            catch (NumberFormatException caughtNumberFormatException)
            {
                return -1;
            }
        }
    }

    private static Vec3 readColorVec3(Properties props, String name)
    {
        int color = readColor(props, name);

        if (color < 0)
        {
            return null;
        }
        else
        {
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            return new Vec3((double)redFactor, (double)greenFactor, (double)blueFactor);
        }
    }

    private static CustomColormap getCustomColors(String basePath, String[] paths, int width, int height)
    {
        for (int i = 0; i < paths.length; ++i)
        {
            String s = paths[i];
            s = basePath + s;
            CustomColormap customColormap = getCustomColors(s, width, height);

            if (customColormap != null)
            {
                return customColormap;
            }
        }

        return null;
    }

    public static CustomColormap getCustomColors(String pathImage, int width, int height)
    {
        try
        {
            ResourceLocation resourceLocation = new ResourceLocation(pathImage);

            if (!Config.hasResource(resourceLocation))
            {
                return null;
            }
            else
            {
                dbg("Colormap " + pathImage);
                Properties properties = new PropertiesOrdered();
                String s = StrUtils.replaceSuffix(pathImage, ".png", ".properties");
                ResourceLocation resourcelocation1 = new ResourceLocation(s);

                if (Config.hasResource(resourcelocation1))
                {
                    InputStream inputStream = Config.getResourceStream(resourcelocation1);
                    properties.load(inputStream);
                    inputStream.close();
                    dbg("Colormap properties: " + s);
                }
                else
                {
                    properties.put("format", paletteFormatDefault);
                    properties.put("source", pathImage);
                    s = pathImage;
                }

                CustomColormap customcolormap = new CustomColormap(properties, s, width, height, paletteFormatDefault);
                return !customcolormap.isValid(s) ? null : customcolormap;
            }
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            return null;
        }
    }

    public static void updateUseDefaultGrassFoliageColors()
    {
        useDefaultGrassFoliageColors = foliageBirchColors == null && foliagePineColors == null && swampGrassColors == null && swampFoliageColors == null && Config.isSwampColors() && Config.isSmoothBiomes();
    }

    public static int getColorMultiplier(BakedQuad quad, IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, RenderEnv renderEnv)
    {
        Block block = blockState.getBlock();
        IBlockState colorBlockState = renderEnv.getBlockState();

        if (blockColormaps != null)
        {
            if (!quad.hasTintIndex())
            {
                if (block == Blocks.grass)
                {
                    colorBlockState = BLOCK_STATE_DIRT;
                }

                if (block == Blocks.redstone_wire)
                {
                    return -1;
                }
            }

            if (block == Blocks.double_plant && renderEnv.getMetadata() >= 8)
            {
                blockPos = blockPos.down();
                colorBlockState = blockAccess.getBlockState(blockPos);
            }

            CustomColormap customcolormap = getBlockColormap(colorBlockState);

            if (customcolormap != null)
            {
                if (Config.isSmoothBiomes() && !customcolormap.isColorConstant())
                {
                    return getSmoothColorMultiplier(blockState, blockAccess, blockPos, customcolormap, renderEnv.getColorizerBlockPosM());
                }

                return customcolormap.getColor(blockAccess, blockPos);
            }
        }

        if (!quad.hasTintIndex())
        {
            return -1;
        }
        else if (block == Blocks.waterlily)
        {
            return getLilypadColorMultiplier(blockAccess, blockPos);
        }
        else if (block == Blocks.redstone_wire)
        {
            return getRedstoneColor(renderEnv.getBlockState());
        }
        else if (block instanceof BlockStem)
        {
            return getStemColorMultiplier(block, blockAccess, blockPos, renderEnv);
        }
        else if (useDefaultGrassFoliageColors)
        {
            return -1;
        }
        else
        {
            int metadata = renderEnv.getMetadata();
            CustomColors.IColorizer colorizer;

            if (block != Blocks.grass && block != Blocks.tallgrass && block != Blocks.double_plant)
            {
                if (block == Blocks.double_plant)
                {
                    colorizer = COLORIZER_GRASS;

                    if (metadata >= 8)
                    {
                        blockPos = blockPos.down();
                    }
                }
                else if (block == Blocks.leaves)
                {
                    switch (metadata & 3)
                    {
                        case 0:
                            colorizer = COLORIZER_FOLIAGE;
                            break;

                        case 1:
                            colorizer = COLORIZER_FOLIAGE_PINE;
                            break;

                        case 2:
                            colorizer = COLORIZER_FOLIAGE_BIRCH;
                            break;

                        default:
                            colorizer = COLORIZER_FOLIAGE;
                    }
                }
                else if (block == Blocks.leaves2)
                {
                    colorizer = COLORIZER_FOLIAGE;
                }
                else
                {
                    if (block != Blocks.vine)
                    {
                        return -1;
                    }

                    colorizer = COLORIZER_FOLIAGE;
                }
            }
            else
            {
                colorizer = COLORIZER_GRASS;
            }

            return Config.isSmoothBiomes() && !colorizer.isColorConstant() ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, colorizer, renderEnv.getColorizerBlockPosM()) : colorizer.getColor(colorBlockState, blockAccess, blockPos);
        }
    }

    protected static BiomeGenBase getColorBiome(IBlockAccess blockAccess, BlockPos blockPos)
    {
        BiomeGenBase biomeGenBase = blockAccess.getBiomeGenForCoords(blockPos);

        if (biomeGenBase == BiomeGenBase.swampland && !Config.isSwampColors())
        {
            biomeGenBase = BiomeGenBase.plains;
        }

        return biomeGenBase;
    }

    private static CustomColormap getBlockColormap(IBlockState blockState)
    {
        if (blockColormaps == null)
        {
            return null;
        }
        else if (!(blockState instanceof BlockStateBase))
        {
            return null;
        }
        else
        {
            BlockStateBase blockStateBase = (BlockStateBase)blockState;
            int blockId = blockStateBase.getBlockId();

            if (blockId >= 0 && blockId < blockColormaps.length)
            {
                CustomColormap[] colormaps = blockColormaps[blockId];

                if (colormaps == null)
                {
                    return null;
                }
                else
                {
                    for (int j = 0; j < colormaps.length; ++j)
                    {
                        CustomColormap customColormap = colormaps[j];

                        if (customColormap.matchesBlock(blockStateBase))
                        {
                            return customColormap;
                        }
                    }

                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    private static int getSmoothColorMultiplier(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, CustomColors.IColorizer colorizer, BlockPosM blockPosM)
    {
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int baseX = blockPos.getX();
        int baseY = blockPos.getY();
        int baseZ = blockPos.getZ();
        BlockPosM mutableBlockPos = blockPosM;

        for (int offsetX = baseX - 1; offsetX <= baseX + 1; ++offsetX)
        {
            for (int offsetZ = baseZ - 1; offsetZ <= baseZ + 1; ++offsetZ)
            {
                mutableBlockPos.setXyz(offsetX, baseY, offsetZ);
                int sampleColor = colorizer.getColor(blockState, blockAccess, mutableBlockPos);
                redSum += sampleColor >> 16 & 255;
                greenSum += sampleColor >> 8 & 255;
                blueSum += sampleColor & 255;
            }
        }

        int redAverage = redSum / 9;
        int greenAverage = greenSum / 9;
        int blueAverage = blueSum / 9;
        return redAverage << 16 | greenAverage << 8 | blueAverage;
    }

    public static int getFluidColor(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, RenderEnv renderEnv)
    {
        Block block = blockState.getBlock();
        CustomColors.IColorizer colorizer = getBlockColormap(blockState);

        if (colorizer == null && blockState.getBlock().getMaterial() == Material.water)
        {
            colorizer = COLORIZER_WATER;
        }

        return colorizer == null ? block.colorMultiplier(blockAccess, blockPos, 0) : (Config.isSmoothBiomes() && !colorizer.isColorConstant() ? getSmoothColorMultiplier(blockState, blockAccess, blockPos, colorizer, renderEnv.getColorizerBlockPosM()) : colorizer.getColor(blockState, blockAccess, blockPos));
    }

    public static void updatePortalFX(EntityFX fx)
    {
        if (particlePortalColor >= 0)
        {
            int color = particlePortalColor;
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            fx.setRBGColorF(redFactor, greenFactor, blueFactor);
        }
    }

    public static void updateMyceliumFX(EntityFX fx)
    {
        if (myceliumParticleColors != null)
        {
            int color = myceliumParticleColors.getColorRandom();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            fx.setRBGColorF(redFactor, greenFactor, blueFactor);
        }
    }

    private static int getRedstoneColor(IBlockState blockState)
    {
        if (redstoneColors == null)
        {
            return -1;
        }
        else
        {
            int powerLevel = getRedstoneLevel(blockState, 15);
            int color = redstoneColors.getColor(powerLevel);
            return color;
        }
    }

    public static void updateReddustFX(EntityFX fx, IBlockAccess blockAccess, double x, double y, double z)
    {
        if (redstoneColors != null)
        {
            IBlockState redstoneState = blockAccess.getBlockState(new BlockPos(x, y, z));
            int powerLevel = getRedstoneLevel(redstoneState, 15);
            int color = redstoneColors.getColor(powerLevel);
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            fx.setRBGColorF(redFactor, greenFactor, blueFactor);
        }
    }

    private static int getRedstoneLevel(IBlockState state, int def)
    {
        Block block = state.getBlock();

        if (!(block instanceof BlockRedstoneWire))
        {
            return def;
        }
        else
        {
            Object object = state.getValue(BlockRedstoneWire.POWER);

            if (!(object instanceof Integer))
            {
                return def;
            }
            else
            {
                Integer integer = (Integer)object;
                return integer.intValue();
            }
        }
    }

    public static float getXpOrbTimer(float timer)
    {
        if (xpOrbTime <= 0)
        {
            return timer;
        }
        else
        {
            float f = 628.0F / (float)xpOrbTime;
            return timer * f;
        }
    }

    public static int getXpOrbColor(float timer)
    {
        if (xpOrbColors == null)
        {
            return -1;
        }
        else
        {
            int i = (int)Math.round((double)((MathHelper.sin(timer) + 1.0F) * (float)(xpOrbColors.getLength() - 1)) / 2.0D);
            int j = xpOrbColors.getColor(i);
            return j;
        }
    }

    public static int getDurabilityColor(int thirdIntValue)
    {
        if (durabilityColors == null)
        {
            return -1;
        }
        else
        {
            int i = thirdIntValue * durabilityColors.getLength() / 255;
            int j = durabilityColors.getColor(i);
            return j;
        }
    }

    public static void updateWaterFX(EntityFX fx, IBlockAccess blockAccess, double x, double y, double z, RenderEnv renderEnv)
    {
        if (waterColors != null || blockColormaps != null || particleWaterColor >= 0)
        {
            BlockPos waterPos = new BlockPos(x, y, z);
            renderEnv.reset(BLOCK_STATE_WATER, waterPos);
            int fluidColor = getFluidColor(blockAccess, BLOCK_STATE_WATER, waterPos, renderEnv);
            int red = fluidColor >> 16 & 255;
            int green = fluidColor >> 8 & 255;
            int blue = fluidColor & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;

            if (particleWaterColor >= 0)
            {
                int particleRed = particleWaterColor >> 16 & 255;
                int particleGreen = particleWaterColor >> 8 & 255;
                int particleBlue = particleWaterColor & 255;
                redFactor *= (float)particleRed / 255.0F;
                greenFactor *= (float)particleGreen / 255.0F;
                blueFactor *= (float)particleBlue / 255.0F;
            }

            fx.setRBGColorF(redFactor, greenFactor, blueFactor);
        }
    }

    private static int getLilypadColorMultiplier(IBlockAccess blockAccess, BlockPos blockPos)
    {
        return lilyPadColor < 0 ? Blocks.waterlily.colorMultiplier(blockAccess, blockPos) : lilyPadColor;
    }

    private static Vec3 getFogColorNether(Vec3 col)
    {
        return fogColorNether == null ? col : fogColorNether;
    }

    private static Vec3 getFogColorEnd(Vec3 col)
    {
        return fogColorEnd == null ? col : fogColorEnd;
    }

    private static Vec3 getSkyColorEnd(Vec3 col)
    {
        return skyColorEnd == null ? col : skyColorEnd;
    }

    public static Vec3 getSkyColor(Vec3 skyColor3d, IBlockAccess blockAccess, double x, double y, double z)
    {
        if (skyColors == null)
        {
            return skyColor3d;
        }
        else
        {
            int color = skyColors.getColorSmooth(blockAccess, x, y, z, 3);
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            float skyRedFactor = (float)skyColor3d.xCoord / 0.5F;
            float skyGreenFactor = (float)skyColor3d.yCoord / 0.66275F;
            float skyBlueFactor = (float)skyColor3d.zCoord;
            redFactor = redFactor * skyRedFactor;
            greenFactor = greenFactor * skyGreenFactor;
            blueFactor = blueFactor * skyBlueFactor;
            Vec3 localValue = skyColorFader.getColor((double)redFactor, (double)greenFactor, (double)blueFactor);
            return localValue;
        }
    }

    private static Vec3 getFogColor(Vec3 fogColor3d, IBlockAccess blockAccess, double x, double y, double z)
    {
        if (fogColors == null)
        {
            return fogColor3d;
        }
        else
        {
            int color = fogColors.getColorSmooth(blockAccess, x, y, z, 3);
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            float fogRedFactor = (float)fogColor3d.xCoord / 0.753F;
            float fogGreenFactor = (float)fogColor3d.yCoord / 0.8471F;
            float fogBlueFactor = (float)fogColor3d.zCoord;
            redFactor = redFactor * fogRedFactor;
            greenFactor = greenFactor * fogGreenFactor;
            blueFactor = blueFactor * fogBlueFactor;
            Vec3 localValue = fogColorFader.getColor((double)redFactor, (double)greenFactor, (double)blueFactor);
            return localValue;
        }
    }

    public static Vec3 getUnderwaterColor(IBlockAccess blockAccess, double x, double y, double z)
    {
        return getUnderFluidColor(blockAccess, x, y, z, underwaterColors, underwaterColorFader);
    }

    public static Vec3 getUnderlavaColor(IBlockAccess blockAccess, double x, double y, double z)
    {
        return getUnderFluidColor(blockAccess, x, y, z, underlavaColors, underlavaColorFader);
    }

    public static Vec3 getUnderFluidColor(IBlockAccess blockAccess, double x, double y, double z, CustomColormap underFluidColors, CustomColorFader underFluidColorFader)
    {
        if (underFluidColors == null)
        {
            return null;
        }
        else
        {
            int color = underFluidColors.getColorSmooth(blockAccess, x, y, z, 3);
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            float redFactor = (float)red / 255.0F;
            float greenFactor = (float)green / 255.0F;
            float blueFactor = (float)blue / 255.0F;
            Vec3 localValue = underFluidColorFader.getColor((double)redFactor, (double)greenFactor, (double)blueFactor);
            return localValue;
        }
    }

    private static int getStemColorMultiplier(Block blockStem, IBlockAccess blockAccess, BlockPos blockPos, RenderEnv renderEnv)
    {
        CustomColormap customColormap = stemColors;

        if (blockStem == Blocks.pumpkin_stem && stemPumpkinColors != null)
        {
            customColormap = stemPumpkinColors;
        }

        if (blockStem == Blocks.melon_stem && stemMelonColors != null)
        {
            customColormap = stemMelonColors;
        }

        if (customColormap == null)
        {
            return -1;
        }
        else
        {
            int i = renderEnv.getMetadata();
            return customColormap.getColor(i);
        }
    }

    public static boolean updateLightmap(World world, float torchFlickerX, int[] lmColors, boolean nightvision, float partialTicks)
    {
        if (world == null)
        {
            return false;
        }
        else if (lightMapPacks == null)
        {
            return false;
        }
        else
        {
            int i = world.provider.getDimensionId();
            int j = i - lightmapMinDimensionId;

            if (j >= 0 && j < lightMapPacks.length)
            {
                LightMapPack lightMapPack = lightMapPacks[j];
                return lightMapPack == null ? false : lightMapPack.updateLightmap(world, torchFlickerX, lmColors, nightvision, partialTicks);
            }
            else
            {
                return false;
            }
        }
    }

    public static Vec3 getWorldFogColor(Vec3 fogVec, World world, Entity renderViewEntity, float partialTicks)
    {
        int i = world.provider.getDimensionId();

        switch (i)
        {
            case -1:
                fogVec = getFogColorNether(fogVec);
                break;

            case 0:
                Minecraft minecraft = Minecraft.getMinecraft();
                fogVec = getFogColor(fogVec, minecraft.theWorld, renderViewEntity.posX, renderViewEntity.posY + 1.0D, renderViewEntity.posZ);
                break;

            case 1:
                fogVec = getFogColorEnd(fogVec);
        }

        return fogVec;
    }

    public static Vec3 getWorldSkyColor(Vec3 skyVec, World world, Entity renderViewEntity, float partialTicks)
    {
        int i = world.provider.getDimensionId();

        switch (i)
        {
            case 0:
                Minecraft minecraft = Minecraft.getMinecraft();
                skyVec = getSkyColor(skyVec, minecraft.theWorld, renderViewEntity.posX, renderViewEntity.posY + 1.0D, renderViewEntity.posZ);
                break;

            case 1:
                skyVec = getSkyColorEnd(skyVec);
        }

        return skyVec;
    }

    private static int[] readSpawnEggColors(Properties props, String fileName, String prefix, String logName)
    {
        List<Integer> list = new ArrayList();
        Set set = props.keySet();
        int colorCount = 0;

        for (Object o : set)
        {
            String propertyKey = (String) o;
            String propertyValue = props.getProperty(propertyKey);

            if (propertyKey.startsWith(prefix))
            {
                String entityName = StrUtils.removePrefix(propertyKey, prefix);
                int entityId = EntityUtils.getEntityIdByName(entityName);

                if (entityId < 0)
                {
                    warn("Invalid spawn egg name: " + propertyKey);
                }
                else
                {
                    int color = parseColor(propertyValue);

                    if (color < 0)
                    {
                        warn("Invalid spawn egg color: " + propertyKey + " = " + propertyValue);
                    }
                    else
                    {
                        while (((List)list).size() <= entityId)
                        {
                            list.add(Integer.valueOf(-1));
                        }

                        list.set(entityId, Integer.valueOf(color));
                        ++colorCount;
                    }
                }
            }
        }

        if (colorCount <= 0)
        {
            return null;
        }
        else
        {
            dbg(logName + " colors: " + colorCount);
            int[] colors = new int[list.size()];

            for (int i = 0; i < colors.length; ++i)
            {
                colors[i] = ((Integer)list.get(i)).intValue();
            }

            return colors;
        }
    }

    private static int getSpawnEggColor(ItemMonsterPlacer item, ItemStack itemStack, int layer, int color)
    {
        int metadata = itemStack.getMetadata();
        int[] spawnEggColors = layer == 0 ? spawnEggPrimaryColors : spawnEggSecondaryColors;

        if (spawnEggColors == null)
        {
            return color;
        }
        else if (metadata >= 0 && metadata < spawnEggColors.length)
        {
            int spawnEggColor = spawnEggColors[metadata];
            return spawnEggColor < 0 ? color : spawnEggColor;
        }
        else
        {
            return color;
        }
    }

    public static int getColorFromItemStack(ItemStack itemStack, int layer, int color)
    {
        if (itemStack == null)
        {
            return color;
        }
        else
        {
            Item item = itemStack.getItem();
            return item == null ? color : (item instanceof ItemMonsterPlacer ? getSpawnEggColor((ItemMonsterPlacer)item, itemStack, layer, color) : color);
        }
    }

    private static float[][] readDyeColors(Properties props, String fileName, String prefix, String logName)
    {
        EnumDyeColor[] dyeColors = EnumDyeColor.VALUES;
        Map<String, EnumDyeColor> map = new HashMap();

        for (int i = 0; i < dyeColors.length; ++i)
        {
            EnumDyeColor dyeColor = dyeColors[i];
            map.put(dyeColor.getName(), dyeColor);
        }

        float[][] dyeColorValues = new float[dyeColors.length][];
        int colorCount = 0;

        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String propertyValue = props.getProperty(propertyKey);

            if (propertyKey.startsWith(prefix))
            {
                String dyeName = StrUtils.removePrefix(propertyKey, prefix);

                if (dyeName.equals("lightBlue"))
                {
                    dyeName = "light_blue";
                }

                EnumDyeColor dyeColor = (EnumDyeColor)map.get(dyeName);
                int color = parseColor(propertyValue);

                if (dyeColor != null && color >= 0)
                {
                    float[] colorValues = new float[] {(float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F};
                    dyeColorValues[dyeColor.ordinal()] = colorValues;
                    ++colorCount;
                }
                else
                {
                    warn("Invalid color: " + propertyKey + " = " + propertyValue);
                }
            }
        }

        if (colorCount <= 0)
        {
            return (float[][])null;
        }
        else
        {
            dbg(logName + " colors: " + colorCount);
            return dyeColorValues;
        }
    }

    private static float[] getDyeColors(EnumDyeColor dye, float[][] dyeColors, float[] colors)
    {
        if (dyeColors == null)
        {
            return colors;
        }
        else if (dye == null)
        {
            return colors;
        }
        else
        {
            float[] dyeColor = dyeColors[dye.ordinal()];
            return dyeColor == null ? colors : dyeColor;
        }
    }

    public static float[] getWolfCollarColors(EnumDyeColor dye, float[] colors)
    {
        return getDyeColors(dye, wolfCollarColors, colors);
    }

    public static float[] getSheepColors(EnumDyeColor dye, float[] colors)
    {
        return getDyeColors(dye, sheepColors, colors);
    }

    private static int[] readTextColors(Properties props, String fileName, String prefix, String logName)
    {
        int[] colors = new int[32];
        Arrays.fill((int[])colors, (int) - 1);
        int colorCount = 0;

        for (Object o: props.keySet())
        {
            String propertyKey = (String) o;
            String propertyValue = props.getProperty(propertyKey);

            if (propertyKey.startsWith(prefix))
            {
                String colorIndexText = StrUtils.removePrefix(propertyKey, prefix);
                int colorIndex = Config.parseInt(colorIndexText, -1);
                int color = parseColor(propertyValue);

                if (colorIndex >= 0 && colorIndex < colors.length && color >= 0)
                {
                    colors[colorIndex] = color;
                    ++colorCount;
                }
                else
                {
                    warn("Invalid color: " + propertyKey + " = " + propertyValue);
                }
            }
        }

        if (colorCount <= 0)
        {
            return null;
        }
        else
        {
            dbg(logName + " colors: " + colorCount);
            return colors;
        }
    }

    public static int getTextColor(int index, int color)
    {
        if (textColors == null)
        {
            return color;
        }
        else if (index >= 0 && index < textColors.length)
        {
            int i = textColors[index];
            return i < 0 ? color : i;
        }
        else
        {
            return color;
        }
    }

    private static int[] readMapColors(Properties props, String fileName, String prefix, String logName)
    {
        int[] colors = new int[MapColor.mapColorArray.length];
        Arrays.fill((int[])colors, (int) - 1);
        int colorCount = 0;

        for (Object o : props.keySet())
        {
            String propertyKey = (String)o;
            String propertyValue = props.getProperty(propertyKey);

            if (propertyKey.startsWith(prefix))
            {
                String mapColorName = StrUtils.removePrefix(propertyKey, prefix);
                int mapColorIndex = getMapColorIndex(mapColorName);
                int color = parseColor(propertyValue);

                if (mapColorIndex >= 0 && mapColorIndex < colors.length && color >= 0)
                {
                    colors[mapColorIndex] = color;
                    ++colorCount;
                }
                else
                {
                    warn("Invalid color: " + propertyKey + " = " + propertyValue);
                }
            }
        }

        if (colorCount <= 0)
        {
            return null;
        }
        else
        {
            dbg(logName + " colors: " + colorCount);
            return colors;
        }
    }

    private static int[] readPotionColors(Properties props, String fileName, String prefix, String logName)
    {
        int[] colors = new int[Potion.potionTypes.length];
        Arrays.fill((int[])colors, (int) - 1);
        int colorCount = 0;

        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String propertyValue = props.getProperty(propertyKey);

            if (propertyKey.startsWith(prefix))
            {
                int potionId = getPotionId(propertyKey);
                int color = parseColor(propertyValue);

                if (potionId >= 0 && potionId < colors.length && color >= 0)
                {
                    colors[potionId] = color;
                    ++colorCount;
                }
                else
                {
                    warn("Invalid color: " + propertyKey + " = " + propertyValue);
                }
            }
        }

        if (colorCount <= 0)
        {
            return null;
        }
        else
        {
            dbg(logName + " colors: " + colorCount);
            return colors;
        }
    }

    private static int getPotionId(String name)
    {
        if (name.equals("potion.water"))
        {
            return 0;
        }
        else
        {
            Potion[] potions = Potion.potionTypes;

            for (int i = 0; i < potions.length; ++i)
            {
                Potion potion = potions[i];

                if (potion != null && potion.getName().equals(name))
                {
                    return potion.getId();
                }
            }

            return -1;
        }
    }

    public static int getPotionColor(int potionId, int color)
    {
        if (potionColors == null)
        {
            return color;
        }
        else if (potionId >= 0 && potionId < potionColors.length)
        {
            int i = potionColors[potionId];
            return i < 0 ? color : i;
        }
        else
        {
            return color;
        }
    }

    private static int getMapColorIndex(String name)
    {
        return name == null ? -1 : (name.equals("air") ? MapColor.airColor.colorIndex : (name.equals("grass") ? MapColor.grassColor.colorIndex : (name.equals("sand") ? MapColor.sandColor.colorIndex : (name.equals("cloth") ? MapColor.clothColor.colorIndex : (name.equals("tnt") ? MapColor.tntColor.colorIndex : (name.equals("ice") ? MapColor.iceColor.colorIndex : (name.equals("iron") ? MapColor.ironColor.colorIndex : (name.equals("foliage") ? MapColor.foliageColor.colorIndex : (name.equals("clay") ? MapColor.clayColor.colorIndex : (name.equals("dirt") ? MapColor.dirtColor.colorIndex : (name.equals("stone") ? MapColor.stoneColor.colorIndex : (name.equals("water") ? MapColor.waterColor.colorIndex : (name.equals("wood") ? MapColor.woodColor.colorIndex : (name.equals("quartz") ? MapColor.quartzColor.colorIndex : (name.equals("gold") ? MapColor.goldColor.colorIndex : (name.equals("diamond") ? MapColor.diamondColor.colorIndex : (name.equals("lapis") ? MapColor.lapisColor.colorIndex : (name.equals("emerald") ? MapColor.emeraldColor.colorIndex : (name.equals("podzol") ? MapColor.obsidianColor.colorIndex : (name.equals("netherrack") ? MapColor.netherrackColor.colorIndex : (!name.equals("snow") && !name.equals("white") ? (!name.equals("adobe") && !name.equals("orange") ? (name.equals("magenta") ? MapColor.magentaColor.colorIndex : (!name.equals("light_blue") && !name.equals("lightBlue") ? (name.equals("yellow") ? MapColor.yellowColor.colorIndex : (name.equals("lime") ? MapColor.limeColor.colorIndex : (name.equals("pink") ? MapColor.pinkColor.colorIndex : (name.equals("gray") ? MapColor.grayColor.colorIndex : (name.equals("silver") ? MapColor.silverColor.colorIndex : (name.equals("cyan") ? MapColor.cyanColor.colorIndex : (name.equals("purple") ? MapColor.purpleColor.colorIndex : (name.equals("blue") ? MapColor.blueColor.colorIndex : (name.equals("brown") ? MapColor.brownColor.colorIndex : (name.equals("green") ? MapColor.greenColor.colorIndex : (name.equals("red") ? MapColor.redColor.colorIndex : (name.equals("black") ? MapColor.blackColor.colorIndex : -1)))))))))))) : MapColor.lightBlueColor.colorIndex)) : MapColor.adobeColor.colorIndex) : MapColor.snowColor.colorIndex)))))))))))))))))))));
    }

    private static int[] getMapColors()
    {
        MapColor[] mapColors = MapColor.mapColorArray;
        int[] colorValues = new int[mapColors.length];
        Arrays.fill((int[])colorValues, (int) - 1);

        for (int i = 0; i < mapColors.length && i < colorValues.length; ++i)
        {
            MapColor mapColor = mapColors[i];

            if (mapColor != null)
            {
                colorValues[i] = mapColor.colorValue;
            }
        }

        return colorValues;
    }

    private static void setMapColors(int[] colors)
    {
        if (colors != null)
        {
            MapColor[] mapColors = MapColor.mapColorArray;
            boolean hasChanged = false;

            for (int i = 0; i < mapColors.length && i < colors.length; ++i)
            {
                MapColor mapColor = mapColors[i];

                if (mapColor != null)
                {
                    int color = colors[i];

                    if (color >= 0 && mapColor.colorValue != color)
                    {
                        mapColor.colorValue = color;
                        hasChanged = true;
                    }
                }
            }

            if (hasChanged)
            {
                Minecraft.getMinecraft().getTextureManager().reloadBannerTextures();
            }
        }
    }

    private static void dbg(String str)
    {
        Config.dbg("CustomColors: " + str);
    }

    private static void warn(String str)
    {
        Config.warn("CustomColors: " + str);
    }

    public static int getExpBarTextColor(int color)
    {
        return expBarTextColor < 0 ? color : expBarTextColor;
    }

    public static int getBossTextColor(int color)
    {
        return bossTextColor < 0 ? color : bossTextColor;
    }

    public static int getSignTextColor(int color)
    {
        return signTextColor < 0 ? color : signTextColor;
    }

    public interface IColorizer
    {
        int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos);

        boolean isColorConstant();
    }
}
