package net.optifine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.config.Matches;
import net.optifine.util.TextureUtils;

public class CustomColormap implements CustomColors.IColorizer
{
    public String name = null;
    public String basePath = null;
    private int format = -1;
    private MatchBlock[] matchBlocks = null;
    private String source = null;
    private int color = -1;
    private int yVariance = 0;
    private int yOffset = 0;
    private int width = 0;
    private int height = 0;
    private int[] colors = null;
    private float[][] colorsRgb = (float[][])null;
    private static final int FORMAT_UNKNOWN = -1;
    private static final int FORMAT_VANILLA = 0;
    private static final int FORMAT_GRID = 1;
    private static final int FORMAT_FIXED = 2;
    public static final String FORMAT_VANILLA_STRING = "vanilla";
    public static final String FORMAT_GRID_STRING = "grid";
    public static final String FORMAT_FIXED_STRING = "fixed";
    public static final String[] FORMAT_STRINGS = new String[] {"vanilla", "grid", "fixed"};
    public static final String KEY_FORMAT = "format";
    public static final String KEY_BLOCKS = "blocks";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_COLOR = "color";
    public static final String KEY_Y_VARIANCE = "yVariance";
    public static final String KEY_Y_OFFSET = "yOffset";

    public CustomColormap(Properties props, String path, int width, int height, String formatDefault)
    {
        ConnectedParser connectedParser = new ConnectedParser("Colormap");
        this.name = connectedParser.parseName(path);
        this.basePath = connectedParser.parseBasePath(path);
        this.format = this.parseFormat(props.getProperty("format", formatDefault));
        this.matchBlocks = connectedParser.parseMatchBlocks(props.getProperty("blocks"));
        this.source = parseTexture(props.getProperty("source"), path, this.basePath);
        this.color = ConnectedParser.parseColor(props.getProperty("color"), -1);
        this.yVariance = connectedParser.parseInt(props.getProperty("yVariance"), 0);
        this.yOffset = connectedParser.parseInt(props.getProperty("yOffset"), 0);
        this.width = width;
        this.height = height;
    }

    private int parseFormat(String str)
    {
        if (str == null)
        {
            return 0;
        }
        else
        {
            str = str.trim();

            if (str.equals("vanilla"))
            {
                return 0;
            }
            else if (str.equals("grid"))
            {
                return 1;
            }
            else if (str.equals("fixed"))
            {
                return 2;
            }
            else
            {
                warn("Unknown format: " + str);
                return -1;
            }
        }
    }

    public boolean isValid(String path)
    {
        if (this.format != 0 && this.format != 1)
        {
            if (this.format != 2)
            {
                return false;
            }

            if (this.color < 0)
            {
                this.color = 16777215;
            }
        }
        else
        {
            if (this.source == null)
            {
                warn("Source not defined: " + path);
                return false;
            }

            this.readColors();

            if (this.colors == null)
            {
                return false;
            }

            if (this.color < 0)
            {
                if (this.format == 0)
                {
                    this.color = this.getColor(127, 127);
                }

                if (this.format == 1)
                {
                    this.color = this.getColorGrid(BiomeGenBase.plains, new BlockPos(0, 64, 0));
                }
            }
        }

        return true;
    }

    public boolean isValidMatchBlocks(String path)
    {
        if (this.matchBlocks == null)
        {
            this.matchBlocks = this.detectMatchBlocks();

            if (this.matchBlocks == null)
            {
                warn("Match blocks not defined: " + path);
                return false;
            }
        }

        return true;
    }

    private MatchBlock[] detectMatchBlocks()
    {
        Block block = Block.getBlockFromName(this.name);

        if (block != null)
        {
            return new MatchBlock[] {new MatchBlock(Block.getIdFromBlock(block))};
        }
        else
        {
            Pattern pattern = Pattern.compile("^block([0-9]+).*$");
            Matcher matcher = pattern.matcher(this.name);

            if (matcher.matches())
            {
                String blockIdText = matcher.group(1);
                int blockId = Config.parseInt(blockIdText, -1);

                if (blockId >= 0)
                {
                    return new MatchBlock[] {new MatchBlock(blockId)};
                }
            }

            ConnectedParser connectedparser = new ConnectedParser("Colormap");
            MatchBlock[] parsedMatchBlocks = connectedparser.parseMatchBlock(this.name);
            return parsedMatchBlocks != null ? parsedMatchBlocks : null;
        }
    }

    private void readColors()
    {
        try
        {
            this.colors = null;

            if (this.source == null)
            {
                return;
            }

            String texturePath = this.source + ".png";
            ResourceLocation textureLocation = new ResourceLocation(texturePath);
            InputStream inputStream = Config.getResourceStream(textureLocation);

            if (inputStream == null)
            {
                return;
            }

            BufferedImage image = TextureUtil.readBufferedImage(inputStream);

            if (image == null)
            {
                return;
            }

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            boolean widthMatches = this.width < 0 || this.width == imageWidth;
            boolean heightMatches = this.height < 0 || this.height == imageHeight;

            if (!widthMatches || !heightMatches)
            {
                dbg("Non-standard palette size: " + imageWidth + "x" + imageHeight + ", should be: " + this.width + "x" + this.height + ", path: " + texturePath);
            }

            this.width = imageWidth;
            this.height = imageHeight;

            if (this.width <= 0 || this.height <= 0)
            {
                warn("Invalid palette size: " + imageWidth + "x" + imageHeight + ", path: " + texturePath);
                return;
            }

            this.colors = new int[imageWidth * imageHeight];
            image.getRGB(0, 0, imageWidth, imageHeight, this.colors, 0, imageWidth);
        }
        catch (IOException ioException)
        {
            net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
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

    private static String parseTexture(String texStr, String path, String basePath)
    {
        if (texStr != null)
        {
            texStr = texStr.trim();
            String pngSuffix = ".png";

            if (texStr.endsWith(pngSuffix))
            {
                texStr = texStr.substring(0, texStr.length() - pngSuffix.length());
            }

            texStr = fixTextureName(texStr, basePath);
            return texStr;
        }
        else
        {
            String textureName = path;
            int slashIndex = path.lastIndexOf(47);

            if (slashIndex >= 0)
            {
                textureName = path.substring(slashIndex + 1);
            }

            int dotIndex = textureName.lastIndexOf(46);

            if (dotIndex >= 0)
            {
                textureName = textureName.substring(0, dotIndex);
            }

            textureName = fixTextureName(textureName, basePath);
            return textureName;
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

        String blockTexturePrefix = "textures/blocks/";

        if (iconName.startsWith(blockTexturePrefix))
        {
            iconName = iconName.substring(blockTexturePrefix.length());
        }

        if (iconName.startsWith("/"))
        {
            iconName = iconName.substring(1);
        }

        return iconName;
    }

    public boolean matchesBlock(BlockStateBase blockState)
    {
        return Matches.block(blockState, this.matchBlocks);
    }

    public int getColorRandom()
    {
        if (this.format == 2)
        {
            return this.color;
        }
        else
        {
            int colorIndex = CustomColors.random.nextInt(this.colors.length);
            return this.colors[colorIndex];
        }
    }

    public int getColor(int index)
    {
        index = Config.limit(index, 0, this.colors.length - 1);
        return this.colors[index] & 16777215;
    }

    public int getColor(int cx, int cy)
    {
        cx = Config.limit(cx, 0, this.width - 1);
        cy = Config.limit(cy, 0, this.height - 1);
        return this.colors[cy * this.width + cx] & 16777215;
    }

    public float[][] getColorsRgb()
    {
        if (this.colorsRgb == null)
        {
            this.colorsRgb = toRgb(this.colors);
        }

        return this.colorsRgb;
    }

    public int getColor(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos)
    {
        return this.getColor(blockAccess, blockPos);
    }

    public int getColor(IBlockAccess blockAccess, BlockPos blockPos)
    {
        BiomeGenBase biome = CustomColors.getColorBiome(blockAccess, blockPos);
        return this.getColor(biome, blockPos);
    }

    public boolean isColorConstant()
    {
        return this.format == 2;
    }

    public int getColor(BiomeGenBase biome, BlockPos blockPos)
    {
        return this.format == 0 ? this.getColorVanilla(biome, blockPos) : (this.format == 1 ? this.getColorGrid(biome, blockPos) : this.color);
    }

    public int getColorSmooth(IBlockAccess blockAccess, double x, double y, double z, int radius)
    {
        if (this.format == 2)
        {
            return this.color;
        }
        else
        {
            int centerX = MathHelper.floor_double(x);
            int centerY = MathHelper.floor_double(y);
            int centerZ = MathHelper.floor_double(z);
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            int sampleCount = 0;
            BlockPosM samplePos = new BlockPosM(0, 0, 0);

            for (int sampleX = centerX - radius; sampleX <= centerX + radius; ++sampleX)
            {
                for (int sampleZ = centerZ - radius; sampleZ <= centerZ + radius; ++sampleZ)
                {
                    samplePos.setXyz(sampleX, centerY, sampleZ);
                    int sampleColor = this.getColor((IBlockAccess)blockAccess, samplePos);
                    redSum += sampleColor >> 16 & 255;
                    greenSum += sampleColor >> 8 & 255;
                    blueSum += sampleColor & 255;
                    ++sampleCount;
                }
            }

            int red = redSum / sampleCount;
            int green = greenSum / sampleCount;
            int blue = blueSum / sampleCount;
            return red << 16 | green << 8 | blue;
        }
    }

    private int getColorVanilla(BiomeGenBase biome, BlockPos blockPos)
    {
        double temperature = (double)MathHelper.clamp_float(biome.getFloatTemperature(blockPos), 0.0F, 1.0F);
        double rainfall = (double)MathHelper.clamp_float(biome.getFloatRainfall(), 0.0F, 1.0F);
        rainfall = rainfall * temperature;
        int colorX = (int)((1.0D - temperature) * (double)(this.width - 1));
        int colorY = (int)((1.0D - rainfall) * (double)(this.height - 1));
        return this.getColor(colorX, colorY);
    }

    private int getColorGrid(BiomeGenBase biome, BlockPos blockPos)
    {
        int biomeId = biome.biomeID;
        int colorY = blockPos.getY() - this.yOffset;

        if (this.yVariance > 0)
        {
            int hashInput = blockPos.getX() << 16 + blockPos.getZ();
            int hash = Config.intHash(hashInput);
            int varianceRange = this.yVariance * 2 + 1;
            int yOffsetRandom = (hash & 255) % varianceRange - this.yVariance;
            colorY += yOffsetRandom;
        }

        return this.getColor(biomeId, colorY);
    }

    public int getLength()
    {
        return this.format == 2 ? 1 : this.colors.length;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    private static float[][] toRgb(int[] cols)
    {
        float[][] rgbColors = new float[cols.length][3];

        for (int colorIndex = 0; colorIndex < cols.length; ++colorIndex)
        {
            int color = cols[colorIndex];
            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            float[] rgb = rgbColors[colorIndex];
            rgb[0] = red;
            rgb[1] = green;
            rgb[2] = blue;
        }

        return rgbColors;
    }

    public void addMatchBlock(MatchBlock mb)
    {
        if (this.matchBlocks == null)
        {
            this.matchBlocks = new MatchBlock[0];
        }

        this.matchBlocks = (MatchBlock[])((MatchBlock[])Config.addObjectToArray(this.matchBlocks, mb));
    }

    public void addMatchBlock(int blockId, int metadata)
    {
        MatchBlock matchBlock = this.getMatchBlock(blockId);

        if (matchBlock != null)
        {
            if (metadata >= 0)
            {
                matchBlock.addMetadata(metadata);
            }
        }
        else
        {
            this.addMatchBlock(new MatchBlock(blockId, metadata));
        }
    }

    private MatchBlock getMatchBlock(int blockId)
    {
        if (this.matchBlocks == null)
        {
            return null;
        }
        else
        {
            for (int matchBlockIndex = 0; matchBlockIndex < this.matchBlocks.length; ++matchBlockIndex)
            {
                MatchBlock matchBlock = this.matchBlocks[matchBlockIndex];

                if (matchBlock.getBlockId() == blockId)
                {
                    return matchBlock;
                }
            }

            return null;
        }
    }

    public int[] getMatchBlockIds()
    {
        if (this.matchBlocks == null)
        {
            return null;
        }
        else
        {
            Set blockIds = new HashSet();

            for (int matchBlockIndex = 0; matchBlockIndex < this.matchBlocks.length; ++matchBlockIndex)
            {
                MatchBlock matchBlock = this.matchBlocks[matchBlockIndex];

                if (matchBlock.getBlockId() >= 0)
                {
                    blockIds.add(Integer.valueOf(matchBlock.getBlockId()));
                }
            }

            Integer[] blockIdObjects = (Integer[])((Integer[])blockIds.toArray(new Integer[blockIds.size()]));
            int[] blockIdArray = new int[blockIdObjects.length];

            for (int blockIdIndex = 0; blockIdIndex < blockIdObjects.length; ++blockIdIndex)
            {
                blockIdArray[blockIdIndex] = blockIdObjects[blockIdIndex].intValue();
            }

            return blockIdArray;
        }
    }

    public String toString()
    {
        return "" + this.basePath + "/" + this.name + ", blocks: " + Config.arrayToString((Object[])this.matchBlocks) + ", source: " + this.source;
    }
}
