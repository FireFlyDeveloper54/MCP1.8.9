package net.optifine.config;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.optifine.ConnectedProperties;
import net.optifine.util.EntityUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.src.Config;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

public class ConnectedParser
{
    private String context = null;
    public static final VillagerProfession[] PROFESSIONS_INVALID = new VillagerProfession[0];
    public static final EnumDyeColor[] DYE_COLORS_INVALID = new EnumDyeColor[0];
    private static final INameGetter<Enum> NAME_GETTER_ENUM = new INameGetter<Enum>()
    {
        public String getName(Enum en)
        {
            return en.name();
        }
    };
    private static final INameGetter<EnumDyeColor> NAME_GETTER_DYE_COLOR = new INameGetter<EnumDyeColor>()
    {
        public String getName(EnumDyeColor col)
        {
            return col.getName();
        }
    };

    public ConnectedParser(String context)
    {
        this.context = context;
    }

    public String parseName(String path)
    {
        String fileName = path;
        int slashIndex = path.lastIndexOf(47);

        if (slashIndex >= 0)
        {
            fileName = path.substring(slashIndex + 1);
        }

        int dotIndex = fileName.lastIndexOf(46);

        if (dotIndex >= 0)
        {
            fileName = fileName.substring(0, dotIndex);
        }

        return fileName;
    }

    public String parseBasePath(String path)
    {
        int slashIndex = path.lastIndexOf(47);
        return slashIndex < 0 ? "" : path.substring(0, slashIndex);
    }

    public MatchBlock[] parseMatchBlocks(String propMatchBlocks)
    {
        if (propMatchBlocks == null)
        {
            return null;
        }
        else
        {
            List matchBlockList = new ArrayList();
            String[] tokens = Config.tokenize(propMatchBlocks, " ");

            for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
            {
                String token = tokens[tokenIndex];
                MatchBlock[] parsedMatchBlocks = this.parseMatchBlock(token);

                if (parsedMatchBlocks != null)
                {
                    matchBlockList.addAll(Arrays.asList(parsedMatchBlocks));
                }
            }

            MatchBlock[] matchBlocks = (MatchBlock[])((MatchBlock[])matchBlockList.toArray(new MatchBlock[matchBlockList.size()]));
            return matchBlocks;
        }
    }

    public IBlockState parseBlockState(String str, IBlockState def)
    {
        MatchBlock[] matchBlocks = this.parseMatchBlock(str);

        if (matchBlocks == null)
        {
            return def;
        }
        else if (matchBlocks.length != 1)
        {
            return def;
        }
        else
        {
            MatchBlock matchBlock = matchBlocks[0];
            int blockId = matchBlock.getBlockId();
            Block block = Block.getBlockById(blockId);
            return block.getDefaultState();
        }
    }

    public MatchBlock[] parseMatchBlock(String blockStr)
    {
        if (blockStr == null)
        {
            return null;
        }
        else
        {
            blockStr = blockStr.trim();

            if (blockStr.length() <= 0)
            {
                return null;
            }
            else
            {
                String[] parts = Config.tokenize(blockStr, ":");
                String domain = "minecraft";
                int blockNameIndex = 0;

                if (parts.length > 1 && this.isFullBlockName(parts))
                {
                    domain = parts[0];
                    blockNameIndex = 1;
                }
                else
                {
                    domain = "minecraft";
                    blockNameIndex = 0;
                }

                String blockName = parts[blockNameIndex];
                String[] metadataParams = (String[])Arrays.copyOfRange(parts, blockNameIndex + 1, parts.length);
                Block[] blocks = this.parseBlockPart(domain, blockName);

                if (blocks == null)
                {
                    return null;
                }
                else
                {
                    MatchBlock[] matchBlocks = new MatchBlock[blocks.length];

                    for (int blockIndex = 0; blockIndex < blocks.length; ++blockIndex)
                    {
                        Block block = blocks[blockIndex];
                        int blockId = Block.getIdFromBlock(block);
                        int[] metadatas = null;

                        if (metadataParams.length > 0)
                        {
                            metadatas = this.parseBlockMetadatas(block, metadataParams);

                            if (metadatas == null)
                            {
                                return null;
                            }
                        }

                        MatchBlock matchBlock = new MatchBlock(blockId, metadatas);
                        matchBlocks[blockIndex] = matchBlock;
                    }

                    return matchBlocks;
                }
            }
        }
    }

    public boolean isFullBlockName(String[] parts)
    {
        if (parts.length < 2)
        {
            return false;
        }
        else
        {
            String blockName = parts[1];
            return blockName.length() < 1 ? false : (this.startsWithDigit(blockName) ? false : !blockName.contains("="));
        }
    }

    public boolean startsWithDigit(String str)
    {
        if (str == null)
        {
            return false;
        }
        else if (str.length() < 1)
        {
            return false;
        }
        else
        {
            char character = str.charAt(0);
            return Character.isDigit(character);
        }
    }

    public Block[] parseBlockPart(String domain, String blockPart)
    {
        if (this.startsWithDigit(blockPart))
        {
            int[] blockIds = this.parseIntList(blockPart);

            if (blockIds == null)
            {
                return null;
            }
            else
            {
                Block[] blocks = new Block[blockIds.length];

                for (int blockIndex = 0; blockIndex < blockIds.length; ++blockIndex)
                {
                    int blockId = blockIds[blockIndex];
                    Block blockById = Block.getBlockById(blockId);

                    if (blockById == null)
                    {
                        this.warn("Block not found for id: " + blockId);
                        return null;
                    }

                    blocks[blockIndex] = blockById;
                }

                return blocks;
            }
        }
        else
        {
            String blockName = domain + ":" + blockPart;
            Block block = Block.getBlockFromName(blockName);

            if (block == null)
            {
                this.warn("Block not found for name: " + blockName);
                return null;
            }
            else
            {
                Block[] blocks = new Block[] {block};
                return blocks;
            }
        }
    }

    public int[] parseBlockMetadatas(Block block, String[] params)
    {
        if (params.length <= 0)
        {
            return null;
        }
        else
        {
            String firstParam = params[0];

            if (this.startsWithDigit(firstParam))
            {
                int[] metadatas = this.parseIntList(firstParam);
                return metadatas;
            }
            else
            {
                IBlockState blockState = block.getDefaultState();
                Collection propertyNames = blockState.getPropertyNames();
                Map<IProperty, List<Comparable>> propertyValuesMap = new HashMap();

                for (int paramIndex = 0; paramIndex < params.length; ++paramIndex)
                {
                    String propertySpec = params[paramIndex];

                    if (propertySpec.length() > 0)
                    {
                        String[] propertyParts = Config.tokenize(propertySpec, "=");

                        if (propertyParts.length != 2)
                        {
                            this.warn("Invalid block property: " + propertySpec);
                            return null;
                        }

                        String propertyName = propertyParts[0];
                        String propertyValueList = propertyParts[1];
                        IProperty property = ConnectedProperties.getProperty(propertyName, propertyNames);

                        if (property == null)
                        {
                            this.warn("Property not found: " + propertyName + ", block: " + block);
                            return null;
                        }

                        List<Comparable> propertyValues = (List)propertyValuesMap.get(propertyName);

                        if (propertyValues == null)
                        {
                            propertyValues = new ArrayList();
                            propertyValuesMap.put(property, propertyValues);
                        }

                        String[] valueTokens = Config.tokenize(propertyValueList, ",");

                        for (int valueIndex = 0; valueIndex < valueTokens.length; ++valueIndex)
                        {
                            String propertyValueName = valueTokens[valueIndex];
                            Comparable comparable = parsePropertyValue(property, propertyValueName);

                            if (comparable == null)
                            {
                                this.warn("Property value not found: " + propertyValueName + ", property: " + propertyName + ", block: " + block);
                                return null;
                            }

                            propertyValues.add(comparable);
                        }
                    }
                }

                if (propertyValuesMap.isEmpty())
                {
                    return null;
                }
                else
                {
                    List<Integer> matchingMetadatas = new ArrayList();

                    for (int metadata = 0; metadata < 16; ++metadata)
                    {
                        try
                        {
                            IBlockState state = this.getStateFromMeta(block, metadata);

                            if (this.matchState(state, propertyValuesMap))
                            {
                                matchingMetadatas.add(Integer.valueOf(metadata));
                            }
                        }
                        catch (IllegalArgumentException caughtIllegalArgumentException)
                        {
                            ;
                        }
                    }

                    if (matchingMetadatas.size() == 16)
                    {
                        return null;
                    }
                    else
                    {
                        int[] metadataArray = new int[matchingMetadatas.size()];

                        for (int metadataIndex = 0; metadataIndex < metadataArray.length; ++metadataIndex)
                        {
                            metadataArray[metadataIndex] = ((Integer)matchingMetadatas.get(metadataIndex)).intValue();
                        }

                        return metadataArray;
                    }
                }
            }
        }
    }

    private IBlockState getStateFromMeta(Block block, int md)
    {
        try
        {
            IBlockState blockState = block.getStateFromMeta(md);

            if (block == Blocks.double_plant && md > 7)
            {
                IBlockState lowerHalfState = block.getStateFromMeta(md & 7);
                blockState = blockState.withProperty(BlockDoublePlant.VARIANT, lowerHalfState.getValue(BlockDoublePlant.VARIANT));
            }

            return blockState;
        }
        catch (IllegalArgumentException caughtIllegalArgumentException)
        {
            return block.getDefaultState();
        }
    }

    public static Comparable parsePropertyValue(IProperty prop, String valStr)
    {
        Class valueClass = prop.getValueClass();
        Comparable comparable = parseValue(valStr, valueClass);

        if (comparable == null)
        {
            Collection allowedValues = prop.getAllowedValues();
            comparable = getPropertyValue(valStr, allowedValues);
        }

        return comparable;
    }

    public static Comparable getPropertyValue(String value, Collection propertyValues)
    {
        for (Object o : propertyValues)
        {
            Comparable comparable = (Comparable) o;
            if (getValueName(comparable).equals(value))
            {
                return comparable;
            }
        }

        return null;
    }

    private static Object getValueName(Comparable obj)
    {
        if (obj instanceof IStringSerializable)
        {
            IStringSerializable stringSerializable = (IStringSerializable)obj;
            return stringSerializable.getName();
        }
        else
        {
            return obj.toString();
        }
    }


    public static Comparable parseValue(String str, Class<?> cls) {
        if (cls == String.class) {
            return str;
        }
        if (cls == Boolean.class) {
            return Boolean.valueOf(str);
        }
        if (cls == Float.class) {
            return Float.valueOf(str);
        }
        if (cls == Double.class) {
            return Double.valueOf(str);
        }
        if (cls == Integer.class) {
            return Integer.valueOf(str);
        }
        if (cls == Long.class) {
            return Long.valueOf(str);
        }
        return null;
    }


    public boolean matchState(IBlockState bs, Map<IProperty, List<Comparable>> mapPropValues)
    {
        for (IProperty property : mapPropValues.keySet())
        {
            List<Comparable> allowedValues = (List)mapPropValues.get(property);
            Comparable value = bs.getValue(property);

            if (value == null)
            {
                return false;
            }

            if (!allowedValues.contains(value))
            {
                return false;
            }
        }

        return true;
    }

    public BiomeGenBase[] parseBiomes(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            str = str.trim();
            boolean inverted = false;

            if (str.startsWith("!"))
            {
                inverted = true;
                str = str.substring(1);
            }

            String[] biomeNames = Config.tokenize(str, " ");
            List biomes = new ArrayList();

            for (int biomeIndex = 0; biomeIndex < biomeNames.length; ++biomeIndex)
            {
                String biomeName = biomeNames[biomeIndex];
                BiomeGenBase biome = this.findBiome(biomeName);

                if (biome == null)
                {
                    this.warn("Biome not found: " + biomeName);
                }
                else
                {
                    biomes.add(biome);
                }
            }

            if (inverted)
            {
                List<BiomeGenBase> invertedBiomes = new ArrayList(Arrays.asList(BiomeGenBase.getBiomeGenArray()));
                invertedBiomes.removeAll(biomes);
                biomes = invertedBiomes;
            }

            BiomeGenBase[] biomeArray = (BiomeGenBase[])((BiomeGenBase[])biomes.toArray(new BiomeGenBase[biomes.size()]));
            return biomeArray;
        }
    }

    public BiomeGenBase findBiome(String biomeName)
    {
        biomeName = biomeName.toLowerCase();

        if (biomeName.equals("nether"))
        {
            return BiomeGenBase.hell;
        }
        else
        {
            BiomeGenBase[] biomeArray = BiomeGenBase.getBiomeGenArray();

            for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex)
            {
                BiomeGenBase biome = biomeArray[biomeIndex];

                if (biome != null)
                {
                    String normalizedBiomeName = biome.biomeName.replace(" ", "").toLowerCase();

                    if (normalizedBiomeName.equals(biomeName))
                    {
                        return biome;
                    }
                }
            }

            return null;
        }
    }

    public int parseInt(String str, int defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            str = str.trim();
            int parsedValue = Config.parseInt(str, -1);

            if (parsedValue < 0)
            {
                this.warn("Invalid number: " + str);
                return defVal;
            }
            else
            {
                return parsedValue;
            }
        }
    }

    public int[] parseIntList(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            List<Integer> values = new ArrayList();
            String[] tokens = Config.tokenize(str, " ,");

            for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
            {
                String token = tokens[tokenIndex];

                if (token.contains("-"))
                {
                    String[] rangeParts = Config.tokenize(token, "-");

                    if (rangeParts.length != 2)
                    {
                        this.warn("Invalid interval: " + token + ", when parsing: " + str);
                    }
                    else
                    {
                        int minValue = Config.parseInt(rangeParts[0], -1);
                        int maxValue = Config.parseInt(rangeParts[1], -1);

                        if (minValue >= 0 && maxValue >= 0 && minValue <= maxValue)
                        {
                            for (int value = minValue; value <= maxValue; ++value)
                            {
                                values.add(Integer.valueOf(value));
                            }
                        }
                        else
                        {
                            this.warn("Invalid interval: " + token + ", when parsing: " + str);
                        }
                    }
                }
                else
                {
                    int value = Config.parseInt(token, -1);

                    if (value < 0)
                    {
                        this.warn("Invalid number: " + token + ", when parsing: " + str);
                    }
                    else
                    {
                        values.add(Integer.valueOf(value));
                    }
                }
            }

            int[] valueArray = new int[values.size()];

            for (int valueIndex = 0; valueIndex < valueArray.length; ++valueIndex)
            {
                valueArray[valueIndex] = ((Integer)values.get(valueIndex)).intValue();
            }

            return valueArray;
        }
    }

    public boolean[] parseFaces(String str, boolean[] defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            EnumSet faces = EnumSet.allOf(EnumFacing.class);
            String[] tokens = Config.tokenize(str, " ,");

            for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
            {
                String faceName = tokens[tokenIndex];

                if (faceName.equals("sides"))
                {
                    faces.add(EnumFacing.NORTH);
                    faces.add(EnumFacing.SOUTH);
                    faces.add(EnumFacing.WEST);
                    faces.add(EnumFacing.EAST);
                }
                else if (faceName.equals("all"))
                {
                    faces.addAll(Arrays.asList(EnumFacing.VALUES));
                }
                else
                {
                    EnumFacing face = this.parseFace(faceName);

                    if (face != null)
                    {
                        faces.add(face);
                    }
                }
            }

            boolean[] faceFlags = new boolean[EnumFacing.VALUES.length];

            for (int faceIndex = 0; faceIndex < faceFlags.length; ++faceIndex)
            {
                faceFlags[faceIndex] = faces.contains(EnumFacing.VALUES[faceIndex]);
            }

            return faceFlags;
        }
    }

    public EnumFacing parseFace(String str)
    {
        str = str.toLowerCase();

        if (!str.equals("bottom") && !str.equals("down"))
        {
            if (!str.equals("top") && !str.equals("up"))
            {
                if (str.equals("north"))
                {
                    return EnumFacing.NORTH;
                }
                else if (str.equals("south"))
                {
                    return EnumFacing.SOUTH;
                }
                else if (str.equals("east"))
                {
                    return EnumFacing.EAST;
                }
                else if (str.equals("west"))
                {
                    return EnumFacing.WEST;
                }
                else
                {
                    Config.warn("Unknown face: " + str);
                    return null;
                }
            }
            else
            {
                return EnumFacing.UP;
            }
        }
        else
        {
            return EnumFacing.DOWN;
        }
    }

    public void dbg(String str)
    {
        Config.dbg("" + this.context + ": " + str);
    }

    public void warn(String str)
    {
        Config.warn("" + this.context + ": " + str);
    }

    public RangeListInt parseRangeListInt(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            RangeListInt rangeList = new RangeListInt();
            String[] tokens = Config.tokenize(str, " ,");

            for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
            {
                String token = tokens[tokenIndex];
                RangeInt range = this.parseRangeInt(token);

                if (range == null)
                {
                    return null;
                }

                rangeList.addRange(range);
            }

            return rangeList;
        }
    }

    private RangeInt parseRangeInt(String str)
    {
        if (str == null)
        {
            return null;
        }
        else if (str.indexOf(45) >= 0)
        {
            String[] rangeParts = Config.tokenize(str, "-");

            if (rangeParts.length != 2)
            {
                this.warn("Invalid range: " + str);
                return null;
            }
            else
            {
                int minValue = Config.parseInt(rangeParts[0], -1);
                int maxValue = Config.parseInt(rangeParts[1], -1);

                if (minValue >= 0 && maxValue >= 0)
                {
                    return new RangeInt(minValue, maxValue);
                }
                else
                {
                    this.warn("Invalid range: " + str);
                    return null;
                }
            }
        }
        else
        {
            int value = Config.parseInt(str, -1);

            if (value < 0)
            {
                this.warn("Invalid integer: " + str);
                return null;
            }
            else
            {
                return new RangeInt(value, value);
            }
        }
    }

    public boolean parseBoolean(String str, boolean defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            String normalized = str.toLowerCase().trim();

            if (normalized.equals("true"))
            {
                return true;
            }
            else if (normalized.equals("false"))
            {
                return false;
            }
            else
            {
                this.warn("Invalid boolean: " + str);
                return defVal;
            }
        }
    }

    public Boolean parseBooleanObject(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            String normalized = str.toLowerCase().trim();

            if (normalized.equals("true"))
            {
                return Boolean.TRUE;
            }
            else if (normalized.equals("false"))
            {
                return Boolean.FALSE;
            }
            else
            {
                this.warn("Invalid boolean: " + str);
                return null;
            }
        }
    }

    public static int parseColor(String str, int defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            str = str.trim();

            try
            {
                int color = Integer.parseInt(str, 16) & 16777215;
                return color;
            }
            catch (NumberFormatException caughtNumberFormatException)
            {
                return defVal;
            }
        }
    }

    public static int parseColor4(String str, int defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            str = str.trim();

            try
            {
                int color = (int)(Long.parseLong(str, 16) & -1L);
                return color;
            }
            catch (NumberFormatException caughtNumberFormatException)
            {
                return defVal;
            }
        }
    }

    public EnumWorldBlockLayer parseBlockRenderLayer(String str, EnumWorldBlockLayer def)
    {
        if (str == null)
        {
            return def;
        }
        else
        {
            str = str.toLowerCase().trim();
            EnumWorldBlockLayer[] blockLayers = EnumWorldBlockLayer.VALUES;

            for (int layerIndex = 0; layerIndex < blockLayers.length; ++layerIndex)
            {
                EnumWorldBlockLayer blockLayer = blockLayers[layerIndex];

                if (str.equals(blockLayer.name().toLowerCase()))
                {
                    return blockLayer;
                }
            }

            return def;
        }
    }

    public <T> T parseObject(String str, T[] objs, INameGetter nameGetter, String property)
    {
        if (str == null)
        {
            return (T)null;
        }
        else
        {
            String normalized = str.toLowerCase().trim();

            for (int objectIndex = 0; objectIndex < objs.length; ++objectIndex)
            {
                T object = objs[objectIndex];
                String objectName = nameGetter.getName(object);

                if (objectName != null && objectName.toLowerCase().equals(normalized))
                {
                    return object;
                }
            }

            this.warn("Invalid " + property + ": " + str);
            return (T)null;
        }
    }

    public <T> T[] parseObjects(String str, T[] objs, INameGetter nameGetter, String property, T[] errValue)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            str = str.toLowerCase().trim();
            String[] tokens = Config.tokenize(str, " ");
            T[] parsedObjects = (T[]) Array.newInstance(objs.getClass().getComponentType(), tokens.length);

            for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
            {
                String token = tokens[tokenIndex];
                T object = this.parseObject(token, objs, nameGetter, property);

                if (object == null)
                {
                    return (T[])errValue;
                }

                parsedObjects[tokenIndex] = object;
            }

            return parsedObjects;
        }
    }

    public Enum parseEnum(String str, Enum[] enums, String property)
    {
        return (Enum)this.parseObject(str, enums, NAME_GETTER_ENUM, property);
    }

    public Enum[] parseEnums(String str, Enum[] enums, String property, Enum[] errValue)
    {
        return (Enum[])this.parseObjects(str, enums, NAME_GETTER_ENUM, property, errValue);
    }

    public EnumDyeColor[] parseDyeColors(String str, String property, EnumDyeColor[] errValue)
    {
        return (EnumDyeColor[])this.parseObjects(str, EnumDyeColor.VALUES, NAME_GETTER_DYE_COLOR, property, errValue);
    }

    public Weather[] parseWeather(String str, String property, Weather[] errValue)
    {
        return (Weather[])this.parseObjects(str, Weather.VALUES, NAME_GETTER_ENUM, property, errValue);
    }

    public NbtTagValue parseNbtTagValue(String path, String value)
    {
        return path != null && value != null ? new NbtTagValue(path, value) : null;
    }

    public VillagerProfession[] parseProfessions(String profStr)
    {
        if (profStr == null)
        {
            return null;
        }
        else
        {
            List<VillagerProfession> professions = new ArrayList();
            String[] tokens = Config.tokenize(profStr, " ");

            for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
            {
                String token = tokens[tokenIndex];
                VillagerProfession profession = this.parseProfession(token);

                if (profession == null)
                {
                    this.warn("Invalid profession: " + token);
                    return PROFESSIONS_INVALID;
                }

                professions.add(profession);
            }

            if (professions.isEmpty())
            {
                return null;
            }
            else
            {
                VillagerProfession[] professionArray = (VillagerProfession[])((VillagerProfession[])professions.toArray(new VillagerProfession[professions.size()]));
                return professionArray;
            }
        }
    }

    private VillagerProfession parseProfession(String str)
    {
        str = str.toLowerCase();
        String[] parts = Config.tokenize(str, ":");

        if (parts.length > 2)
        {
            return null;
        }
        else
        {
            String professionName = parts[0];
            String careerNames = null;

            if (parts.length > 1)
            {
                careerNames = parts[1];
            }

            int professionId = parseProfessionId(professionName);

            if (professionId < 0)
            {
                return null;
            }
            else
            {
                int[] careerIds = null;

                if (careerNames != null)
                {
                    careerIds = parseCareerIds(professionId, careerNames);

                    if (careerIds == null)
                    {
                        return null;
                    }
                }

                return new VillagerProfession(professionId, careerIds);
            }
        }
    }

    private static int parseProfessionId(String str)
    {
        int professionId = Config.parseInt(str, -1);
        return professionId >= 0 ? professionId : (str.equals("farmer") ? 0 : (str.equals("librarian") ? 1 : (str.equals("priest") ? 2 : (str.equals("blacksmith") ? 3 : (str.equals("butcher") ? 4 : (str.equals("nitwit") ? 5 : -1))))));
    }

    private static int[] parseCareerIds(int prof, String str)
    {
        Set<Integer> careerIdSet = new HashSet();
        String[] tokens = Config.tokenize(str, ",");

        for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
        {
            String token = tokens[tokenIndex];
            int careerId = parseCareerId(prof, token);

            if (careerId < 0)
            {
                return null;
            }

            careerIdSet.add(Integer.valueOf(careerId));
        }

        Integer[] careerIds = (Integer[])((Integer[])careerIdSet.toArray(new Integer[careerIdSet.size()]));
        int[] careerIdArray = new int[careerIds.length];

        for (int careerIndex = 0; careerIndex < careerIdArray.length; ++careerIndex)
        {
            careerIdArray[careerIndex] = careerIds[careerIndex].intValue();
        }

        return careerIdArray;
    }

    private static int parseCareerId(int prof, String str)
    {
        int careerId = Config.parseInt(str, -1);

        if (careerId >= 0)
        {
            return careerId;
        }
        else
        {
            if (prof == 0)
            {
                if (str.equals("farmer"))
                {
                    return 1;
                }

                if (str.equals("fisherman"))
                {
                    return 2;
                }

                if (str.equals("shepherd"))
                {
                    return 3;
                }

                if (str.equals("fletcher"))
                {
                    return 4;
                }
            }

            if (prof == 1)
            {
                if (str.equals("librarian"))
                {
                    return 1;
                }

                if (str.equals("cartographer"))
                {
                    return 2;
                }
            }

            if (prof == 2 && str.equals("cleric"))
            {
                return 1;
            }
            else
            {
                if (prof == 3)
                {
                    if (str.equals("armor"))
                    {
                        return 1;
                    }

                    if (str.equals("weapon"))
                    {
                        return 2;
                    }

                    if (str.equals("tool"))
                    {
                        return 3;
                    }
                }

                if (prof == 4)
                {
                    if (str.equals("butcher"))
                    {
                        return 1;
                    }

                    if (str.equals("leather"))
                    {
                        return 2;
                    }
                }

                return prof == 5 && str.equals("nitwit") ? 1 : -1;
            }
        }
    }

    public int[] parseItems(String str)
    {
        str = str.trim();
        Set<Integer> itemIds = new TreeSet();
        String[] tokens = Config.tokenize(str, " ");

        for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
        {
            String token = tokens[tokenIndex];
            ResourceLocation itemLocation = new ResourceLocation(token);
            Item item = (Item)Item.itemRegistry.getObject(itemLocation);

            if (item == null)
            {
                this.warn("Item not found: " + token);
            }
            else
            {
                int itemId = Item.getIdFromItem(item);

                if (itemId < 0)
                {
                    this.warn("Item has no ID: " + item + ", name: " + token);
                }
                else
                {
                    itemIds.add(Integer.valueOf(itemId));
                }
            }
        }

        Integer[] itemIdArray = (Integer[])((Integer[])itemIds.toArray(new Integer[itemIds.size()]));
        int[] primitiveItemIds = Config.toPrimitive(itemIdArray);
        return primitiveItemIds;
    }

    public int[] parseEntities(String str)
    {
        str = str.trim();
        Set<Integer> entityIds = new TreeSet();
        String[] tokens = Config.tokenize(str, " ");

        for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
        {
            String token = tokens[tokenIndex];
            int entityId = EntityUtils.getEntityIdByName(token);

            if (entityId < 0)
            {
                this.warn("Entity not found: " + token);
            }
            else
            {
                entityIds.add(Integer.valueOf(entityId));
            }
        }

        Integer[] entityIdArray = (Integer[])((Integer[])entityIds.toArray(new Integer[entityIds.size()]));
        int[] primitiveEntityIds = Config.toPrimitive(entityIdArray);
        return primitiveEntityIds;
    }
}
