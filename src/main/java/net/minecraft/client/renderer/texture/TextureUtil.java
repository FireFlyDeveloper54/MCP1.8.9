package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.Mipmaps;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL12;

public class TextureUtil
{
    private static final Logger logger = LogManager.getLogger();
    private static final IntBuffer dataBuffer = GLAllocation.createDirectIntBuffer(4194304);
    public static final DynamicTexture missingTexture = new DynamicTexture(16, 16);
    public static final int[] missingTextureData = missingTexture.getTextureData();
    private static final int[] mipmapBuffer;
    private static int[] dataArray = new int[4194304];

    public static int glGenTextures()
    {
        return GlStateManager.generateTexture();
    }

    public static void deleteTexture(int textureId)
    {
        GlStateManager.deleteTexture(textureId);
    }

    public static int uploadTextureImage(int textureId, BufferedImage image)
    {
        return uploadTextureImageAllocate(textureId, image, false, false);
    }

    public static void uploadTexture(int textureId, int[] pixels, int width, int height)
    {
        bindTexture(textureId);
        uploadTextureSub(0, pixels, width, height, 0, 0, false, false, false);
    }

    public static int[][] generateMipmapData(int mipmapLevels, int width, int[][] mipmapData)
    {
        int[][] generatedMipmaps = new int[mipmapLevels + 1][];
        generatedMipmaps[0] = mipmapData[0];

        if (mipmapLevels > 0)
        {
            boolean hasTransparentPixels = false;

            for (int pixelIndex = 0; pixelIndex < mipmapData[0].length; ++pixelIndex)
            {
                if (mipmapData[0][pixelIndex] >> 24 == 0)
                {
                    hasTransparentPixels = true;
                    break;
                }
            }

            for (int mipLevel = 1; mipLevel <= mipmapLevels; ++mipLevel)
            {
                if (mipmapData[mipLevel] != null)
                {
                    generatedMipmaps[mipLevel] = mipmapData[mipLevel];
                }
                else
                {
                    int[] previousMipPixels = generatedMipmaps[mipLevel - 1];
                    int[] mipPixels = new int[previousMipPixels.length >> 2];
                    int mipWidth = width >> mipLevel;
                    int mipHeight = mipPixels.length / mipWidth;
                    int previousMipRowStride = mipWidth << 1;

                    for (int mipX = 0; mipX < mipWidth; ++mipX)
                    {
                        for (int mipY = 0; mipY < mipHeight; ++mipY)
                        {
                            int previousPixelIndex = 2 * (mipX + mipY * previousMipRowStride);
                            mipPixels[mipX + mipY * mipWidth] = blendColors(previousMipPixels[previousPixelIndex + 0], previousMipPixels[previousPixelIndex + 1], previousMipPixels[previousPixelIndex + 0 + previousMipRowStride], previousMipPixels[previousPixelIndex + 1 + previousMipRowStride], hasTransparentPixels);
                        }
                    }

                    generatedMipmaps[mipLevel] = mipPixels;
                }
            }
        }

        return generatedMipmaps;
    }

    private static int blendColors(int firstColor, int secondColor, int thirdColor, int fourthColor, boolean alpha)
    {
        return Mipmaps.alphaBlend(firstColor, secondColor, thirdColor, fourthColor);
    }

    private static int blendColorComponent(int firstColor, int secondColor, int thirdColor, int fourthColor, int componentShift)
    {
        float firstLinear = (float)Math.pow((double)((float)(firstColor >> componentShift & 255) / 255.0F), 2.2D);
        float secondLinear = (float)Math.pow((double)((float)(secondColor >> componentShift & 255) / 255.0F), 2.2D);
        float thirdLinear = (float)Math.pow((double)((float)(thirdColor >> componentShift & 255) / 255.0F), 2.2D);
        float fourthLinear = (float)Math.pow((double)((float)(fourthColor >> componentShift & 255) / 255.0F), 2.2D);
        float blendedLinear = (float)Math.pow((double)(firstLinear + secondLinear + thirdLinear + fourthLinear) * 0.25D, 0.45454545454545453D);
        return (int)((double)blendedLinear * 255.0D);
    }

    public static void uploadTextureMipmap(int[][] mipmapData, int width, int height, int xOffset, int yOffset, boolean blur, boolean clamp)
    {
        for (int mipLevel = 0; mipLevel < mipmapData.length; ++mipLevel)
        {
            int[] mipPixels = mipmapData[mipLevel];
            uploadTextureSub(mipLevel, mipPixels, width >> mipLevel, height >> mipLevel, xOffset >> mipLevel, yOffset >> mipLevel, blur, clamp, mipmapData.length > 1);
        }
    }

    private static void uploadTextureSub(int level, int[] pixels, int width, int height, int xOffset, int yOffset, boolean blur, boolean clamp, boolean mipmap)
    {
        int maxRowsPerUpload = 4194304 / width;
        setTextureBlurMipmap(blur, mipmap);
        setTextureClamped(clamp);
        int uploadRows;

        for (int pixelOffset = 0; pixelOffset < width * height; pixelOffset += width * uploadRows)
        {
            int uploadY = pixelOffset / width;
            uploadRows = Math.min(maxRowsPerUpload, height - uploadY);
            int uploadPixelCount = width * uploadRows;
            copyToBufferPos(pixels, pixelOffset, uploadPixelCount);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, xOffset, yOffset + uploadY, width, uploadRows, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)dataBuffer);
        }
    }

    public static int uploadTextureImageAllocate(int textureId, BufferedImage image, boolean blur, boolean clamp)
    {
        allocateTexture(textureId, image.getWidth(), image.getHeight());
        return uploadTextureImageSub(textureId, image, 0, 0, blur, clamp);
    }

    public static void allocateTexture(int textureId, int width, int height)
    {
        allocateTextureImpl(textureId, 0, width, height);
    }

    public static void allocateTextureImpl(int textureId, int mipmapLevels, int width, int height)
    {
        Object splashLock = TextureUtil.class;

        
        synchronized (splashLock)
        {
            deleteTexture(textureId);
            bindTexture(textureId);
        }

        if (mipmapLevels >= 0)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipmapLevels);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, (float)mipmapLevels);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
        }

        for (int mipLevel = 0; mipLevel <= mipmapLevels; ++mipLevel)
        {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, mipLevel, GL11.GL_RGBA, width >> mipLevel, height >> mipLevel, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)((IntBuffer)null));
        }
    }

    public static int uploadTextureImageSub(int textureId, BufferedImage image, int xOffset, int yOffset, boolean blur, boolean clamp)
    {
        bindTexture(textureId);
        uploadTextureImageSubImpl(image, xOffset, yOffset, blur, clamp);
        return textureId;
    }

    private static void uploadTextureImageSubImpl(BufferedImage image, int xOffset, int yOffset, boolean blur, boolean clamp)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int maxRowsPerUpload = 4194304 / imageWidth;
        int[] uploadBuffer = dataArray;
        setTextureBlurred(blur);
        setTextureClamped(clamp);

        for (int pixelOffset = 0; pixelOffset < imageWidth * imageHeight; pixelOffset += imageWidth * maxRowsPerUpload)
        {
            int uploadY = pixelOffset / imageWidth;
            int uploadRows = Math.min(maxRowsPerUpload, imageHeight - uploadY);
            int uploadPixelCount = imageWidth * uploadRows;
            image.getRGB(0, uploadY, imageWidth, uploadRows, uploadBuffer, 0, imageWidth);
            copyToBuffer(uploadBuffer, uploadPixelCount);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, xOffset, yOffset + uploadY, imageWidth, uploadRows, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)dataBuffer);
        }
    }

    public static void setTextureClamped(boolean clamp)
    {
        if (clamp)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
    }

    private static void setTextureBlurred(boolean blur)
    {
        setTextureBlurMipmap(blur, false);
    }

    public static void setTextureBlurMipmap(boolean blur, boolean mipmap)
    {
        if (blur)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }
        else
        {
            int mipmapFilter = Config.getMipmapType();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? mipmapFilter : GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    private static void copyToBuffer(int[] pixels, int length)
    {
        copyToBufferPos(pixels, 0, length);
    }

    private static void copyToBufferPos(int[] pixels, int offset, int length)
    {
        int[] bufferPixels = pixels;

        if (Minecraft.getMinecraft().gameSettings.anaglyph)
        {
            bufferPixels = updateAnaglyph(pixels);
        }

        dataBuffer.clear();
        dataBuffer.put(bufferPixels, offset, length);
        dataBuffer.position(0).limit(length);
    }

    static void bindTexture(int textureId)
    {
        GlStateManager.bindTexture(textureId);
    }

    public static int[] readImageData(IResourceManager resourceManager, ResourceLocation imageLocation) throws IOException
    {
        BufferedImage image = readBufferedImage(resourceManager.getResource(imageLocation).getInputStream());

        if (image == null)
        {
            return null;
        }
        else
        {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int[] imagePixels = new int[imageWidth * imageHeight];
            image.getRGB(0, 0, imageWidth, imageHeight, imagePixels, 0, imageWidth);
            return imagePixels;
        }
    }

    public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException
    {
        if (imageStream == null)
        {
            return null;
        }
        else
        {
            BufferedImage image;

            try
            {
                image = ImageIO.read(imageStream);
            }
            finally
            {
                IOUtils.closeQuietly(imageStream);
            }

            return image;
        }
    }

    public static int[] updateAnaglyph(int[] pixels)
    {
        int[] anaglyphPixels = new int[pixels.length];

        for (int pixelIndex = 0; pixelIndex < pixels.length; ++pixelIndex)
        {
            anaglyphPixels[pixelIndex] = anaglyphColor(pixels[pixelIndex]);
        }

        return anaglyphPixels;
    }

    public static int anaglyphColor(int color)
    {
        int alpha = color >> 24 & 255;
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        int anaglyphRed = (red * 30 + green * 59 + blue * 11) / 100;
        int anaglyphGreen = (red * 30 + green * 70) / 100;
        int anaglyphBlue = (red * 30 + blue * 70) / 100;
        return alpha << 24 | anaglyphRed << 16 | anaglyphGreen << 8 | anaglyphBlue;
    }

    public static void processPixelValues(int[] pixels, int width, int height)
    {
        int[] rowBuffer = new int[width];
        int halfHeight = height / 2;

        for (int row = 0; row < halfHeight; ++row)
        {
            System.arraycopy(pixels, row * width, rowBuffer, 0, width);
            System.arraycopy(pixels, (height - 1 - row) * width, pixels, row * width, width);
            System.arraycopy(rowBuffer, 0, pixels, (height - 1 - row) * width, width);
        }
    }

    static
    {
        int opaqueBlack = -16777216;
        int missingTextureGray = -524040;
        int[] grayBlock = new int[] {missingTextureGray, missingTextureGray, missingTextureGray, missingTextureGray, missingTextureGray, missingTextureGray, missingTextureGray, missingTextureGray};
        int[] blackBlock = new int[] {opaqueBlack, opaqueBlack, opaqueBlack, opaqueBlack, opaqueBlack, opaqueBlack, opaqueBlack, opaqueBlack};
        int blockWidth = grayBlock.length;

        for (int row = 0; row < 16; ++row)
        {
            System.arraycopy(row < blockWidth ? grayBlock : blackBlock, 0, missingTextureData, 16 * row, blockWidth);
            System.arraycopy(row < blockWidth ? blackBlock : grayBlock, 0, missingTextureData, 16 * row + blockWidth, blockWidth);
        }

        missingTexture.updateDynamicTexture();
        mipmapBuffer = new int[4];
    }
}
