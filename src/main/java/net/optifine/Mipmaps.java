package net.optifine;

import java.awt.Dimension;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.src.Config;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class Mipmaps
{
    private final String iconName;
    private final int width;
    private final int height;
    private final int[] data;
    private final boolean direct;
    private int[][] mipmapDatas;
    private IntBuffer[] mipmapBuffers;
    private Dimension[] mipmapDimensions;

    public Mipmaps(String iconName, int width, int height, int[] data, boolean direct)
    {
        this.iconName = iconName;
        this.width = width;
        this.height = height;
        this.data = data;
        this.direct = direct;
        this.mipmapDimensions = makeMipmapDimensions(width, height, iconName);
        this.mipmapDatas = generateMipMapData(data, width, height, this.mipmapDimensions);

        if (direct)
        {
            this.mipmapBuffers = makeMipmapBuffers(this.mipmapDimensions, this.mipmapDatas);
        }
    }

    public static Dimension[] makeMipmapDimensions(int width, int height, String iconName)
    {
        int mipmapWidth = TextureUtils.ceilPowerOfTwo(width);
        int mipmapHeight = TextureUtils.ceilPowerOfTwo(height);

        if (mipmapWidth == width && mipmapHeight == height)
        {
            List dimensions = new ArrayList();
            int levelWidth = mipmapWidth;
            int levelHeight = mipmapHeight;

            while (true)
            {
                levelWidth /= 2;
                levelHeight /= 2;

                if (levelWidth <= 0 && levelHeight <= 0)
                {
                    Dimension[] dimensionArray = (Dimension[])((Dimension[])dimensions.toArray(new Dimension[dimensions.size()]));
                    return dimensionArray;
                }

                if (levelWidth <= 0)
                {
                    levelWidth = 1;
                }

                if (levelHeight <= 0)
                {
                    levelHeight = 1;
                }

                int levelSizeBytes = levelWidth * levelHeight * 4;
                Dimension dimension = new Dimension(levelWidth, levelHeight);
                dimensions.add(dimension);
            }
        }
        else
        {
            Config.warn("Mipmaps not possible (power of 2 dimensions needed), texture: " + iconName + ", dim: " + width + "x" + height);
            return new Dimension[0];
        }
    }

    public static int[][] generateMipMapData(int[] data, int width, int height, Dimension[] mipmapDimensions)
    {
        int[] sourceData = data;
        int sourceWidth = width;
        boolean canBlend = true;
        int[][] mipmapData = new int[mipmapDimensions.length][];

        for (int levelIndex = 0; levelIndex < mipmapDimensions.length; ++levelIndex)
        {
            Dimension dimension = mipmapDimensions[levelIndex];
            int levelWidth = dimension.width;
            int levelHeight = dimension.height;
            int[] levelData = new int[levelWidth * levelHeight];
            mipmapData[levelIndex] = levelData;
            int levelNumber = levelIndex + 1;

            if (canBlend)
            {
                for (int x = 0; x < levelWidth; ++x)
                {
                    for (int y = 0; y < levelHeight; ++y)
                    {
                        int pixel00 = sourceData[x * 2 + 0 + (y * 2 + 0) * sourceWidth];
                        int pixel10 = sourceData[x * 2 + 1 + (y * 2 + 0) * sourceWidth];
                        int pixel11 = sourceData[x * 2 + 1 + (y * 2 + 1) * sourceWidth];
                        int pixel01 = sourceData[x * 2 + 0 + (y * 2 + 1) * sourceWidth];
                        int blendedPixel = alphaBlend(pixel00, pixel10, pixel11, pixel01);
                        levelData[x + y * levelWidth] = blendedPixel;
                    }
                }
            }

            sourceData = levelData;
            sourceWidth = levelWidth;

            if (levelWidth <= 1 || levelHeight <= 1)
            {
                canBlend = false;
            }
        }

        return mipmapData;
    }

    public static int alphaBlend(int color1, int color2, int color3, int color4)
    {
        int blendedTop = alphaBlend(color1, color2);
        int blendedBottom = alphaBlend(color3, color4);
        int blendedColor = alphaBlend(blendedTop, blendedBottom);
        return blendedColor;
    }

    private static int alphaBlend(int color1, int color2)
    {
        int alpha1 = (color1 & -16777216) >> 24 & 255;
        int alpha2 = (color2 & -16777216) >> 24 & 255;
        int alpha = (alpha1 + alpha2) / 2;

        if (alpha1 == 0 && alpha2 == 0)
        {
            alpha1 = 1;
            alpha2 = 1;
        }
        else
        {
            if (alpha1 == 0)
            {
                color1 = color2;
                alpha /= 2;
            }

            if (alpha2 == 0)
            {
                color2 = color1;
                alpha /= 2;
            }
        }

        int red1 = (color1 >> 16 & 255) * alpha1;
        int green1 = (color1 >> 8 & 255) * alpha1;
        int blue1 = (color1 & 255) * alpha1;
        int red2 = (color2 >> 16 & 255) * alpha2;
        int green2 = (color2 >> 8 & 255) * alpha2;
        int blue2 = (color2 & 255) * alpha2;
        int red = (red1 + red2) / (alpha1 + alpha2);
        int green = (green1 + green2) / (alpha1 + alpha2);
        int blue = (blue1 + blue2) / (alpha1 + alpha2);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private int averageColor(int color1, int color2)
    {
        int alpha1 = (color1 & -16777216) >> 24 & 255;
        int alpha2 = (color2 & -16777216) >> 24 & 255;
        return (alpha1 + color2 >> 1 << 24) + ((alpha1 & 16711422) + (alpha2 & 16711422) >> 1);
    }

    public static IntBuffer[] makeMipmapBuffers(Dimension[] mipmapDimensions, int[][] mipmapDatas)
    {
        if (mipmapDimensions == null)
        {
            return null;
        }
        else
        {
            IntBuffer[] mipmapBuffers = new IntBuffer[mipmapDimensions.length];

            for (int levelIndex = 0; levelIndex < mipmapDimensions.length; ++levelIndex)
            {
                Dimension dimension = mipmapDimensions[levelIndex];
                int pixelCount = dimension.width * dimension.height;
                IntBuffer mipmapBuffer = GLAllocation.createDirectIntBuffer(pixelCount);
                int[] mipmapData = mipmapDatas[levelIndex];
                mipmapBuffer.clear();
                mipmapBuffer.put(mipmapData);
                mipmapBuffer.clear();
                mipmapBuffers[levelIndex] = mipmapBuffer;
            }

            return mipmapBuffers;
        }
    }

    public static void allocateMipmapTextures(int width, int height, String name)
    {
        Dimension[] mipmapDimensions = makeMipmapDimensions(width, height, name);

        for (int levelIndex = 0; levelIndex < mipmapDimensions.length; ++levelIndex)
        {
            Dimension dimension = mipmapDimensions[levelIndex];
            int levelWidth = dimension.width;
            int levelHeight = dimension.height;
            int mipmapLevel = levelIndex + 1;
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, mipmapLevel, GL11.GL_RGBA, levelWidth, levelHeight, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)((IntBuffer)null));
        }
    }
}
