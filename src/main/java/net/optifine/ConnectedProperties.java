package net.optifine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeInt;
import net.optifine.config.RangeListInt;
import net.optifine.util.MathUtils;
import net.optifine.util.TextureUtils;

public class ConnectedProperties
{
    public String name = null;
    public String basePath = null;
    public MatchBlock[] matchBlocks = null;
    public int[] metadatas = null;
    public String[] matchTiles = null;
    public int method = 0;
    public String[] tiles = null;
    public int connect = 0;
    public int faces = 63;
    public BiomeGenBase[] biomes = null;
    public RangeListInt heights = null;
    public int renderPass = 0;
    public boolean innerSeams = false;
    public int[] ctmTileIndexes = null;
    public int width = 0;
    public int height = 0;
    public int[] weights = null;
    public int randomLoops = 0;
    public int symmetry = 1;
    public boolean linked = false;
    public NbtTagValue nbtName = null;
    public int[] sumWeights = null;
    public int sumAllWeights = 1;
    public TextureAtlasSprite[] matchTileIcons = null;
    public TextureAtlasSprite[] tileIcons = null;
    public MatchBlock[] connectBlocks = null;
    public String[] connectTiles = null;
    public TextureAtlasSprite[] connectTileIcons = null;
    public int tintIndex = -1;
    public IBlockState tintBlockState = Blocks.air.getDefaultState();
    public EnumWorldBlockLayer layer = null;
    public static final int METHOD_NONE = 0;
    public static final int METHOD_CTM = 1;
    public static final int METHOD_HORIZONTAL = 2;
    public static final int METHOD_TOP = 3;
    public static final int METHOD_RANDOM = 4;
    public static final int METHOD_REPEAT = 5;
    public static final int METHOD_VERTICAL = 6;
    public static final int METHOD_FIXED = 7;
    public static final int METHOD_HORIZONTAL_VERTICAL = 8;
    public static final int METHOD_VERTICAL_HORIZONTAL = 9;
    public static final int METHOD_CTM_COMPACT = 10;
    public static final int METHOD_OVERLAY = 11;
    public static final int METHOD_OVERLAY_FIXED = 12;
    public static final int METHOD_OVERLAY_RANDOM = 13;
    public static final int METHOD_OVERLAY_REPEAT = 14;
    public static final int METHOD_OVERLAY_CTM = 15;
    public static final int CONNECT_NONE = 0;
    public static final int CONNECT_BLOCK = 1;
    public static final int CONNECT_TILE = 2;
    public static final int CONNECT_MATERIAL = 3;
    public static final int CONNECT_UNKNOWN = 128;
    public static final int FACE_BOTTOM = 1;
    public static final int FACE_TOP = 2;
    public static final int FACE_NORTH = 4;
    public static final int FACE_SOUTH = 8;
    public static final int FACE_WEST = 16;
    public static final int FACE_EAST = 32;
    public static final int FACE_SIDES = 60;
    public static final int FACE_ALL = 63;
    public static final int FACE_UNKNOWN = 128;
    public static final int SYMMETRY_NONE = 1;
    public static final int SYMMETRY_OPPOSITE = 2;
    public static final int SYMMETRY_ALL = 6;
    public static final int SYMMETRY_UNKNOWN = 128;
    public static final String TILE_SKIP_PNG = "<skip>.png";
    public static final String TILE_DEFAULT_PNG = "<default>.png";

    public ConnectedProperties(Properties props, String path)
    {
        ConnectedParser connectedParser = new ConnectedParser("ConnectedTextures");
        this.name = connectedParser.parseName(path);
        this.basePath = connectedParser.parseBasePath(path);
        this.matchBlocks = connectedParser.parseMatchBlocks(props.getProperty("matchBlocks"));
        this.metadatas = connectedParser.parseIntList(props.getProperty("metadata"));
        this.matchTiles = this.parseMatchTiles(props.getProperty("matchTiles"));
        this.method = parseMethod(props.getProperty("method"));
        this.tiles = this.parseTileNames(props.getProperty("tiles"));
        this.connect = parseConnect(props.getProperty("connect"));
        this.faces = parseFaces(props.getProperty("faces"));
        this.biomes = connectedParser.parseBiomes(props.getProperty("biomes"));
        this.heights = connectedParser.parseRangeListInt(props.getProperty("heights"));

        if (this.heights == null)
        {
            int i = connectedParser.parseInt(props.getProperty("minHeight"), -1);
            int j = connectedParser.parseInt(props.getProperty("maxHeight"), 1024);

            if (i != -1 || j != 1024)
            {
                this.heights = new RangeListInt(new RangeInt(i, j));
            }
        }

        this.renderPass = connectedParser.parseInt(props.getProperty("renderPass"), -1);
        this.innerSeams = connectedParser.parseBoolean(props.getProperty("innerSeams"), false);
        this.ctmTileIndexes = this.parseCtmTileIndexes(props);
        this.width = connectedParser.parseInt(props.getProperty("width"), -1);
        this.height = connectedParser.parseInt(props.getProperty("height"), -1);
        this.weights = connectedParser.parseIntList(props.getProperty("weights"));
        this.randomLoops = connectedParser.parseInt(props.getProperty("randomLoops"), 0);
        this.symmetry = parseSymmetry(props.getProperty("symmetry"));
        this.linked = connectedParser.parseBoolean(props.getProperty("linked"), false);
        this.nbtName = connectedParser.parseNbtTagValue("name", props.getProperty("name"));
        this.connectBlocks = connectedParser.parseMatchBlocks(props.getProperty("connectBlocks"));
        this.connectTiles = this.parseMatchTiles(props.getProperty("connectTiles"));
        this.tintIndex = connectedParser.parseInt(props.getProperty("tintIndex"), -1);
        this.tintBlockState = connectedParser.parseBlockState(props.getProperty("tintBlock"), Blocks.air.getDefaultState());
        this.layer = connectedParser.parseBlockRenderLayer(props.getProperty("layer"), EnumWorldBlockLayer.CUTOUT_MIPPED);
    }

    private int[] parseCtmTileIndexes(Properties props)
    {
        if (this.tiles == null)
        {
            return null;
        }
        else
        {
            Map<Integer, Integer> ctmIndexMap = new HashMap();

            for (Object object : props.keySet())
            {
                if (object instanceof String)
                {
                    String propertyName = (String)object;
                    String ctmPrefix = "ctm.";

                    if (propertyName.startsWith(ctmPrefix))
                    {
                        String ctmIndexText = propertyName.substring(ctmPrefix.length());
                        String ctmTileValue = props.getProperty(propertyName);

                        if (ctmTileValue != null)
                        {
                            ctmTileValue = ctmTileValue.trim();
                            int ctmIndex = Config.parseInt(ctmIndexText, -1);

                            if (ctmIndex >= 0 && ctmIndex <= 46)
                            {
                                int tileIndex = Config.parseInt(ctmTileValue, -1);

                                if (tileIndex >= 0 && tileIndex < this.tiles.length)
                                {
                                    ctmIndexMap.put(Integer.valueOf(ctmIndex), Integer.valueOf(tileIndex));
                                }
                                else
                                {
                                    Config.warn("Invalid CTM tile index: " + ctmTileValue);
                                }
                            }
                            else
                            {
                                Config.warn("Invalid CTM index: " + ctmIndexText);
                            }
                        }
                    }
                }
            }

            if (ctmIndexMap.isEmpty())
            {
                return null;
            }
            else
            {
                int[] aint = new int[47];
                Arrays.fill(aint, -1);

                for (Map.Entry<Integer, Integer> entry : ctmIndexMap.entrySet())
                {
                    aint[entry.getKey().intValue()] = entry.getValue().intValue();
                }

                return aint;
            }
        }
    }

    private String[] parseMatchTiles(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            String[] astring = Config.tokenize(str, " ");

            for (int i = 0; i < astring.length; ++i)
            {
                String s = astring[i];

                if (s.endsWith(".png"))
                {
                    s = s.substring(0, s.length() - 4);
                }

                s = TextureUtils.fixResourcePath(s, this.basePath);
                astring[i] = s;
            }

            return astring;
        }
    }

    private static String parseName(String path)
    {
        String s = path;
        int i = path.lastIndexOf(47);

        if (i >= 0)
        {
            s = path.substring(i + 1);
        }

        int j = s.lastIndexOf(46);

        if (j >= 0)
        {
            s = s.substring(0, j);
        }

        return s;
    }

    private static String parseBasePath(String path)
    {
        int i = path.lastIndexOf(47);
        return i < 0 ? "" : path.substring(0, i);
    }

    private String[] parseTileNames(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            List list = new ArrayList();
            String[] tileTokens = Config.tokenize(str, " ,");
            nextToken:

            for (int i = 0; i < tileTokens.length; ++i)
            {
                String tileToken = tileTokens[i];

                if (tileToken.contains("-"))
                {
                    String[] rangeTokens = Config.tokenize(tileToken, "-");

                    if (rangeTokens.length == 2)
                    {
                        int firstTileIndex = Config.parseInt(rangeTokens[0], -1);
                        int lastTileIndex = Config.parseInt(rangeTokens[1], -1);

                        if (firstTileIndex >= 0 && lastTileIndex >= 0)
                        {
                            if (firstTileIndex > lastTileIndex)
                            {
                                Config.warn("Invalid interval: " + tileToken + ", when parsing: " + str);
                                continue;
                            }

                            int tileIndex = firstTileIndex;

                            while (true)
                            {
                                if (tileIndex > lastTileIndex)
                                {
                                    continue nextToken;
                                }

                                list.add(String.valueOf(tileIndex));
                                ++tileIndex;
                            }
                        }
                    }
                }

                list.add(tileToken);
            }

            String[] parsedTileNames = (String[])((String[])list.toArray(new String[list.size()]));

            for (int tileNameIndex = 0; tileNameIndex < parsedTileNames.length; ++tileNameIndex)
            {
                String tileName = parsedTileNames[tileNameIndex];
                tileName = TextureUtils.fixResourcePath(tileName, this.basePath);

                if (!tileName.startsWith(this.basePath) && !tileName.startsWith("textures/") && !tileName.startsWith("mcpatcher/"))
                {
                    tileName = this.basePath + "/" + tileName;
                }

                if (tileName.endsWith(".png"))
                {
                    tileName = tileName.substring(0, tileName.length() - 4);
                }

                if (tileName.startsWith("/"))
                {
                    tileName = tileName.substring(1);
                }

                parsedTileNames[tileNameIndex] = tileName;
            }

            return parsedTileNames;
        }
    }

    private static int parseSymmetry(String str)
    {
        if (str == null)
        {
            return 1;
        }
        else
        {
            str = str.trim();

            if (str.equals("opposite"))
            {
                return 2;
            }
            else if (str.equals("all"))
            {
                return 6;
            }
            else
            {
                Config.warn("Unknown symmetry: " + str);
                return 1;
            }
        }
    }

    private static int parseFaces(String str)
    {
        if (str == null)
        {
            return 63;
        }
        else
        {
            String[] astring = Config.tokenize(str, " ,");
            int i = 0;

            for (int j = 0; j < astring.length; ++j)
            {
                String s = astring[j];
                int k = parseFace(s);
                i |= k;
            }

            return i;
        }
    }

    private static int parseFace(String str)
    {
        str = str.toLowerCase();

        if (!str.equals("bottom") && !str.equals("down"))
        {
            if (!str.equals("top") && !str.equals("up"))
            {
                if (str.equals("north"))
                {
                    return 4;
                }
                else if (str.equals("south"))
                {
                    return 8;
                }
                else if (str.equals("east"))
                {
                    return 32;
                }
                else if (str.equals("west"))
                {
                    return 16;
                }
                else if (str.equals("sides"))
                {
                    return 60;
                }
                else if (str.equals("all"))
                {
                    return 63;
                }
                else
                {
                    Config.warn("Unknown face: " + str);
                    return 128;
                }
            }
            else
            {
                return 2;
            }
        }
        else
        {
            return 1;
        }
    }

    private static int parseConnect(String str)
    {
        if (str == null)
        {
            return 0;
        }
        else
        {
            str = str.trim();

            if (str.equals("block"))
            {
                return 1;
            }
            else if (str.equals("tile"))
            {
                return 2;
            }
            else if (str.equals("material"))
            {
                return 3;
            }
            else
            {
                Config.warn("Unknown connect: " + str);
                return 128;
            }
        }
    }

    public static IProperty getProperty(String key, Collection properties)
    {
        for (Object o : properties)
        {
            IProperty iproperty = (IProperty) o;
            if (key.equals(iproperty.getName()))
            {
                return iproperty;
            }
        }

        return null;
    }

    private static int parseMethod(String str)
    {
        if (str == null)
        {
            return 1;
        }
        else
        {
            str = str.trim();

            if (!str.equals("ctm") && !str.equals("glass"))
            {
                if (str.equals("ctm_compact"))
                {
                    return 10;
                }
                else if (!str.equals("horizontal") && !str.equals("bookshelf"))
                {
                    if (str.equals("vertical"))
                    {
                        return 6;
                    }
                    else if (str.equals("top"))
                    {
                        return 3;
                    }
                    else if (str.equals("random"))
                    {
                        return 4;
                    }
                    else if (str.equals("repeat"))
                    {
                        return 5;
                    }
                    else if (str.equals("fixed"))
                    {
                        return 7;
                    }
                    else if (!str.equals("horizontal+vertical") && !str.equals("h+v"))
                    {
                        if (!str.equals("vertical+horizontal") && !str.equals("v+h"))
                        {
                            if (str.equals("overlay"))
                            {
                                return 11;
                            }
                            else if (str.equals("overlay_fixed"))
                            {
                                return 12;
                            }
                            else if (str.equals("overlay_random"))
                            {
                                return 13;
                            }
                            else if (str.equals("overlay_repeat"))
                            {
                                return 14;
                            }
                            else if (str.equals("overlay_ctm"))
                            {
                                return 15;
                            }
                            else
                            {
                                Config.warn("Unknown method: " + str);
                                return 0;
                            }
                        }
                        else
                        {
                            return 9;
                        }
                    }
                    else
                    {
                        return 8;
                    }
                }
                else
                {
                    return 2;
                }
            }
            else
            {
                return 1;
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
            else
            {
                if (this.matchBlocks == null)
                {
                    this.matchBlocks = this.detectMatchBlocks();
                }

                if (this.matchTiles == null && this.matchBlocks == null)
                {
                    this.matchTiles = this.detectMatchTiles();
                }

                if (this.matchBlocks == null && this.matchTiles == null)
                {
                    Config.warn("No matchBlocks or matchTiles specified: " + path);
                    return false;
                }
                else if (this.method == 0)
                {
                    Config.warn("No method: " + path);
                    return false;
                }
                else if (this.tiles != null && this.tiles.length > 0)
                {
                    if (this.connect == 0)
                    {
                        this.connect = this.detectConnect();
                    }

                    if (this.connect == 128)
                    {
                        Config.warn("Invalid connect in: " + path);
                        return false;
                    }
                    else if (this.renderPass > 0)
                    {
                        Config.warn("Render pass not supported: " + this.renderPass);
                        return false;
                    }
                    else if ((this.faces & 128) != 0)
                    {
                        Config.warn("Invalid faces in: " + path);
                        return false;
                    }
                    else if ((this.symmetry & 128) != 0)
                    {
                        Config.warn("Invalid symmetry in: " + path);
                        return false;
                    }
                    else
                    {
                        switch (this.method)
                        {
                            case 1:
                                return this.isValidCtm(path);

                            case 2:
                                return this.isValidHorizontal(path);

                            case 3:
                                return this.isValidTop(path);

                            case 4:
                                return this.isValidRandom(path);

                            case 5:
                                return this.isValidRepeat(path);

                            case 6:
                                return this.isValidVertical(path);

                            case 7:
                                return this.isValidFixed(path);

                            case 8:
                                return this.isValidHorizontalVertical(path);

                            case 9:
                                return this.isValidVerticalHorizontal(path);

                            case 10:
                                return this.isValidCtmCompact(path);

                            case 11:
                                return this.isValidOverlay(path);

                            case 12:
                                return this.isValidOverlayFixed(path);

                            case 13:
                                return this.isValidOverlayRandom(path);

                            case 14:
                                return this.isValidOverlayRepeat(path);

                            case 15:
                                return this.isValidOverlayCtm(path);

                            default:
                                Config.warn("Unknown method: " + path);
                                return false;
                        }
                    }
                }
                else
                {
                    Config.warn("No tiles specified: " + path);
                    return false;
                }
            }
        }
        else
        {
            Config.warn("No name found: " + path);
            return false;
        }
    }

    private int detectConnect()
    {
        return this.matchBlocks != null ? 1 : (this.matchTiles != null ? 2 : 128);
    }

    private MatchBlock[] detectMatchBlocks()
    {
        int[] aint = this.detectMatchBlockIds();

        if (aint == null)
        {
            return null;
        }
        else
        {
            MatchBlock[] amatchblock = new MatchBlock[aint.length];

            for (int i = 0; i < amatchblock.length; ++i)
            {
                amatchblock[i] = new MatchBlock(aint[i]);
            }

            return amatchblock;
        }
    }

    private int[] detectMatchBlockIds()
    {
        if (!this.name.startsWith("block"))
        {
            return null;
        }
        else
        {
            int i = "block".length();
            int j;

            for (j = i; j < this.name.length(); ++j)
            {
                char character = this.name.charAt(j);

                if (character < 48 || character > 57)
                {
                    break;
                }
            }

            if (j == i)
            {
                return null;
            }
            else
            {
                String s = this.name.substring(i, j);
                int k = Config.parseInt(s, -1);
                return k < 0 ? null : new int[] {k};
            }
        }
    }

    private String[] detectMatchTiles()
    {
        TextureAtlasSprite textureAtlasSprite = getIcon(this.name);
        return textureAtlasSprite == null ? null : new String[] {this.name};
    }

    private static TextureAtlasSprite getIcon(String iconName)
    {
        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite textureAtlasSprite = textureMap.getSpriteSafe(iconName);

        if (textureAtlasSprite != null)
        {
            return textureAtlasSprite;
        }
        else
        {
            textureAtlasSprite = textureMap.getSpriteSafe("blocks/" + iconName);
            return textureAtlasSprite;
        }
    }

    private boolean isValidCtm(String path)
    {
        if (this.tiles == null)
        {
            this.tiles = this.parseTileNames("0-11 16-27 32-43 48-58");
        }

        if (this.tiles.length < 47)
        {
            Config.warn("Invalid tiles, must be at least 47: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidCtmCompact(String path)
    {
        if (this.tiles == null)
        {
            this.tiles = this.parseTileNames("0-4");
        }

        if (this.tiles.length < 5)
        {
            Config.warn("Invalid tiles, must be at least 5: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidOverlay(String path)
    {
        if (this.tiles == null)
        {
            this.tiles = this.parseTileNames("0-16");
        }

        if (this.tiles.length < 17)
        {
            Config.warn("Invalid tiles, must be at least 17: " + path);
            return false;
        }
        else if (this.layer != null && this.layer != EnumWorldBlockLayer.SOLID)
        {
            return true;
        }
        else
        {
            Config.warn("Invalid overlay layer: " + this.layer);
            return false;
        }
    }

    private boolean isValidOverlayFixed(String path)
    {
        if (!this.isValidFixed(path))
        {
            return false;
        }
        else if (this.layer != null && this.layer != EnumWorldBlockLayer.SOLID)
        {
            return true;
        }
        else
        {
            Config.warn("Invalid overlay layer: " + this.layer);
            return false;
        }
    }

    private boolean isValidOverlayRandom(String path)
    {
        if (!this.isValidRandom(path))
        {
            return false;
        }
        else if (this.layer != null && this.layer != EnumWorldBlockLayer.SOLID)
        {
            return true;
        }
        else
        {
            Config.warn("Invalid overlay layer: " + this.layer);
            return false;
        }
    }

    private boolean isValidOverlayRepeat(String path)
    {
        if (!this.isValidRepeat(path))
        {
            return false;
        }
        else if (this.layer != null && this.layer != EnumWorldBlockLayer.SOLID)
        {
            return true;
        }
        else
        {
            Config.warn("Invalid overlay layer: " + this.layer);
            return false;
        }
    }

    private boolean isValidOverlayCtm(String path)
    {
        if (!this.isValidCtm(path))
        {
            return false;
        }
        else if (this.layer != null && this.layer != EnumWorldBlockLayer.SOLID)
        {
            return true;
        }
        else
        {
            Config.warn("Invalid overlay layer: " + this.layer);
            return false;
        }
    }

    private boolean isValidHorizontal(String path)
    {
        if (this.tiles == null)
        {
            this.tiles = this.parseTileNames("12-15");
        }

        if (this.tiles.length != 4)
        {
            Config.warn("Invalid tiles, must be exactly 4: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidVertical(String path)
    {
        if (this.tiles == null)
        {
            Config.warn("No tiles defined for vertical: " + path);
            return false;
        }
        else if (this.tiles.length != 4)
        {
            Config.warn("Invalid tiles, must be exactly 4: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidHorizontalVertical(String path)
    {
        if (this.tiles == null)
        {
            Config.warn("No tiles defined for horizontal+vertical: " + path);
            return false;
        }
        else if (this.tiles.length != 7)
        {
            Config.warn("Invalid tiles, must be exactly 7: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidVerticalHorizontal(String path)
    {
        if (this.tiles == null)
        {
            Config.warn("No tiles defined for vertical+horizontal: " + path);
            return false;
        }
        else if (this.tiles.length != 7)
        {
            Config.warn("Invalid tiles, must be exactly 7: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidRandom(String path)
    {
        if (this.tiles != null && this.tiles.length > 0)
        {
            if (this.weights != null)
            {
                if (this.weights.length > this.tiles.length)
                {
                    Config.warn("More weights defined than tiles, trimming weights: " + path);
                    int[] aint = new int[this.tiles.length];
                    System.arraycopy(this.weights, 0, aint, 0, aint.length);
                    this.weights = aint;
                }

                if (this.weights.length < this.tiles.length)
                {
                    Config.warn("Less weights defined than tiles, expanding weights: " + path);
                    int[] aint1 = new int[this.tiles.length];
                    System.arraycopy(this.weights, 0, aint1, 0, this.weights.length);
                    int i = MathUtils.getAverage(this.weights);

                    for (int j = this.weights.length; j < aint1.length; ++j)
                    {
                        aint1[j] = i;
                    }

                    this.weights = aint1;
                }

                this.sumWeights = new int[this.weights.length];
                int k = 0;

                for (int l = 0; l < this.weights.length; ++l)
                {
                    k += this.weights[l];
                    this.sumWeights[l] = k;
                }

                this.sumAllWeights = k;

                if (this.sumAllWeights <= 0)
                {
                    Config.warn("Invalid sum of all weights: " + k);
                    this.sumAllWeights = 1;
                }
            }

            if (this.randomLoops >= 0 && this.randomLoops <= 9)
            {
                return true;
            }
            else
            {
                Config.warn("Invalid randomLoops: " + this.randomLoops);
                return false;
            }
        }
        else
        {
            Config.warn("Tiles not defined: " + path);
            return false;
        }
    }

    private boolean isValidRepeat(String path)
    {
        if (this.tiles == null)
        {
            Config.warn("Tiles not defined: " + path);
            return false;
        }
        else if (this.width <= 0)
        {
            Config.warn("Invalid width: " + path);
            return false;
        }
        else if (this.height <= 0)
        {
            Config.warn("Invalid height: " + path);
            return false;
        }
        else if (this.tiles.length != this.width * this.height)
        {
            Config.warn("Number of tiles does not equal width x height: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidFixed(String path)
    {
        if (this.tiles == null)
        {
            Config.warn("Tiles not defined: " + path);
            return false;
        }
        else if (this.tiles.length != 1)
        {
            Config.warn("Number of tiles should be 1 for method: fixed.");
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean isValidTop(String path)
    {
        if (this.tiles == null)
        {
            this.tiles = this.parseTileNames("66");
        }

        if (this.tiles.length != 1)
        {
            Config.warn("Invalid tiles, must be exactly 1: " + path);
            return false;
        }
        else
        {
            return true;
        }
    }

    public void updateIcons(TextureMap textureMap)
    {
        if (this.matchTiles != null)
        {
            this.matchTileIcons = registerIcons(this.matchTiles, textureMap, false, false);
        }

        if (this.connectTiles != null)
        {
            this.connectTileIcons = registerIcons(this.connectTiles, textureMap, false, false);
        }

        if (this.tiles != null)
        {
            this.tileIcons = registerIcons(this.tiles, textureMap, true, !isMethodOverlay(this.method));
        }
    }

    private static boolean isMethodOverlay(int method)
    {
        switch (method)
        {
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return true;

            default:
                return false;
        }
    }

    private static TextureAtlasSprite[] registerIcons(String[] tileNames, TextureMap textureMap, boolean skipTiles, boolean defaultTiles)
    {
        if (tileNames == null)
        {
            return null;
        }
        else
        {
            List list = new ArrayList();

            for (int i = 0; i < tileNames.length; ++i)
            {
                String tileName = tileNames[i];
                ResourceLocation tileLocation = new ResourceLocation(tileName);
                String resourceDomain = tileLocation.getResourceDomain();
                String resourcePath = tileLocation.getResourcePath();

                if (!resourcePath.contains("/"))
                {
                    resourcePath = "textures/blocks/" + resourcePath;
                }

                String pngPath = resourcePath + ".png";

                if (skipTiles && pngPath.endsWith("<skip>.png"))
                {
                    list.add(null);
                }
                else if (defaultTiles && pngPath.endsWith("<default>.png"))
                {
                    list.add(ConnectedTextures.SPRITE_DEFAULT);
                }
                else
                {
                    ResourceLocation pngLocation = new ResourceLocation(resourceDomain, pngPath);
                    boolean hasPngResource = Config.hasResource(pngLocation);

                    if (!hasPngResource)
                    {
                        Config.warn("File not found: " + pngPath);
                    }

                    String texturesPrefix = "textures/";
                    String spritePath = resourcePath;

                    if (resourcePath.startsWith(texturesPrefix))
                    {
                        spritePath = resourcePath.substring(texturesPrefix.length());
                    }

                    ResourceLocation spriteLocation = new ResourceLocation(resourceDomain, spritePath);
                    TextureAtlasSprite sprite = textureMap.registerSprite(spriteLocation);
                    list.add(sprite);
                }
            }

            TextureAtlasSprite[] sprites = (TextureAtlasSprite[])((TextureAtlasSprite[])list.toArray(new TextureAtlasSprite[list.size()]));
            return sprites;
        }
    }

    public boolean matchesBlockId(int blockId)
    {
        return Matches.blockId(blockId, this.matchBlocks);
    }

    public boolean matchesBlock(int blockId, int metadata)
    {
        return !Matches.block(blockId, metadata, this.matchBlocks) ? false : Matches.metadata(metadata, this.metadatas);
    }

    public boolean matchesIcon(TextureAtlasSprite icon)
    {
        return Matches.sprite(icon, this.matchTileIcons);
    }

    public String toString()
    {
        return "CTM name: " + this.name + ", basePath: " + this.basePath + ", matchBlocks: " + Config.arrayToString((Object[])this.matchBlocks) + ", matchTiles: " + Config.arrayToString((Object[])this.matchTiles);
    }

    public boolean matchesBiome(BiomeGenBase biome)
    {
        return Matches.biome(biome, this.biomes);
    }

    public int getMetadataMax()
    {
        int i = -1;
        i = this.getMax(this.metadatas, i);

        if (this.matchBlocks != null)
        {
            for (int j = 0; j < this.matchBlocks.length; ++j)
            {
                MatchBlock matchblock = this.matchBlocks[j];
                i = this.getMax(matchblock.getMetadatas(), i);
            }
        }

        return i;
    }

    private int getMax(int[] mds, int max)
    {
        if (mds == null)
        {
            return max;
        }
        else
        {
            for (int i = 0; i < mds.length; ++i)
            {
                int j = mds[i];

                if (j > max)
                {
                    max = j;
                }
            }

            return max;
        }
    }
}
