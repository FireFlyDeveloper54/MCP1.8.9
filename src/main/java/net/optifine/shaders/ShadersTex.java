package net.optifine.shaders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ShadersTex
{
    public static final int initialBufferSize = 1048576;
    public static ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4194304);
    public static IntBuffer intBuffer = byteBuffer.asIntBuffer();
    public static int[] intArray = new int[1048576];
    public static final int defBaseTexColor = 0;
    public static final int defNormTexColor = -8421377;
    public static final int defSpecTexColor = 0;
    public static Map<Integer, MultiTexID> multiTexMap = new HashMap();

    public static IntBuffer getIntBuffer(int size)
    {
        if (intBuffer.capacity() < size)
        {
            int i = roundUpPOT(size);
            byteBuffer = BufferUtils.createByteBuffer(i * 4);
            intBuffer = byteBuffer.asIntBuffer();
        }

        return intBuffer;
    }

    public static int[] getIntArray(int size)
    {
        if (intArray == null)
        {
            intArray = new int[1048576];
        }

        if (intArray.length < size)
        {
            intArray = new int[roundUpPOT(size)];
        }

        return intArray;
    }

    public static int roundUpPOT(int x)
    {
        int i = x - 1;
        i = i | i >> 1;
        i = i | i >> 2;
        i = i | i >> 4;
        i = i | i >> 8;
        i = i | i >> 16;
        return i + 1;
    }

    public static int log2(int x)
    {
        int i = 0;

        if ((x & -65536) != 0)
        {
            i += 16;
            x >>= 16;
        }

        if ((x & 65280) != 0)
        {
            i += 8;
            x >>= 8;
        }

        if ((x & 240) != 0)
        {
            i += 4;
            x >>= 4;
        }

        if ((x & 6) != 0)
        {
            i += 2;
            x >>= 2;
        }

        if ((x & 2) != 0)
        {
            ++i;
        }

        return i;
    }

    public static IntBuffer fillIntBuffer(int size, int value)
    {
        int[] aint = getIntArray(size);
        IntBuffer intbuffer = getIntBuffer(size);
        Arrays.fill((int[])intArray, 0, size, (int)value);
        intBuffer.put(intArray, 0, size);
        return intBuffer;
    }

    public static int[] createAIntImage(int size)
    {
        int[] aint = new int[size * 3];
        Arrays.fill((int[])aint, 0, size, (int)0);
        Arrays.fill(aint, size, size * 2, -8421377);
        Arrays.fill((int[])aint, size * 2, size * 3, (int)0);
        return aint;
    }

    public static int[] createAIntImage(int size, int color)
    {
        int[] aint = new int[size * 3];
        Arrays.fill((int[])aint, 0, size, (int)color);
        Arrays.fill(aint, size, size * 2, -8421377);
        Arrays.fill((int[])aint, size * 2, size * 3, (int)0);
        return aint;
    }

    public static MultiTexID getMultiTexID(AbstractTexture tex)
    {
        MultiTexID multitexid = tex.multiTex;

        if (multitexid == null)
        {
            int i = tex.getGlTextureId();
            multitexid = (MultiTexID)multiTexMap.get(Integer.valueOf(i));

            if (multitexid == null)
            {
                multitexid = new MultiTexID(i, GL11.glGenTextures(), GL11.glGenTextures());
                multiTexMap.put(Integer.valueOf(i), multitexid);
            }

            tex.multiTex = multitexid;
        }

        return multitexid;
    }

    public static void deleteTextures(AbstractTexture atex, int texid)
    {
        MultiTexID multiTexID = atex.multiTex;

        if (multiTexID != null)
        {
            atex.multiTex = null;
            multiTexMap.remove(Integer.valueOf(multiTexID.base));
            GlStateManager.deleteTexture(multiTexID.norm);
            GlStateManager.deleteTexture(multiTexID.spec);

            if (multiTexID.base != texid)
            {
                SMCLog.warning("Error : MultiTexID.base mismatch: " + multiTexID.base + ", texid: " + texid);
                GlStateManager.deleteTexture(multiTexID.base);
            }
        }
    }

    public static void bindNSTextures(int normTex, int specTex)
    {
        if (Shaders.isRenderingWorld && GlStateManager.getActiveTextureUnit() == 33984)
        {
            GlStateManager.setActiveTexture(33986);
            GlStateManager.bindTexture(normTex);
            GlStateManager.setActiveTexture(33987);
            GlStateManager.bindTexture(specTex);
            GlStateManager.setActiveTexture(33984);
        }
    }

    public static void bindNSTextures(MultiTexID multiTex)
    {
        bindNSTextures(multiTex.norm, multiTex.spec);
    }

    public static void bindTextures(int baseTex, int normTex, int specTex)
    {
        if (Shaders.isRenderingWorld && GlStateManager.getActiveTextureUnit() == 33984)
        {
            GlStateManager.setActiveTexture(33986);
            GlStateManager.bindTexture(normTex);
            GlStateManager.setActiveTexture(33987);
            GlStateManager.bindTexture(specTex);
            GlStateManager.setActiveTexture(33984);
        }

        GlStateManager.bindTexture(baseTex);
    }

    public static void bindTextures(MultiTexID multiTex)
    {
        if (Shaders.isRenderingWorld && GlStateManager.getActiveTextureUnit() == 33984)
        {
            if (Shaders.configNormalMap)
            {
                GlStateManager.setActiveTexture(33986);
                GlStateManager.bindTexture(multiTex.norm);
            }

            if (Shaders.configSpecularMap)
            {
                GlStateManager.setActiveTexture(33987);
                GlStateManager.bindTexture(multiTex.spec);
            }

            GlStateManager.setActiveTexture(33984);
        }

        GlStateManager.bindTexture(multiTex.base);
    }

    public static void bindTexture(ITextureObject tex)
    {
        int i = tex.getGlTextureId();
        bindTextures(tex.getMultiTexID());

        if (GlStateManager.getActiveTextureUnit() == 33984)
        {
            int j = Shaders.atlasSizeX;
            int k = Shaders.atlasSizeY;

            if (tex instanceof TextureMap)
            {
                Shaders.atlasSizeX = ((TextureMap)tex).atlasWidth;
                Shaders.atlasSizeY = ((TextureMap)tex).atlasHeight;
            }
            else
            {
                Shaders.atlasSizeX = 0;
                Shaders.atlasSizeY = 0;
            }

            if (Shaders.atlasSizeX != j || Shaders.atlasSizeY != k)
            {
                Shaders.uniform_atlasSize.setValue(Shaders.atlasSizeX, Shaders.atlasSizeY);
            }
        }
    }

    public static void bindTextures(int baseTex)
    {
        MultiTexID multiTexID = (MultiTexID)multiTexMap.get(Integer.valueOf(baseTex));
        bindTextures(multiTexID);
    }

    public static void initDynamicTexture(int texID, int width, int height, DynamicTexture tex)
    {
        MultiTexID multitexid = tex.getMultiTexID();
        int[] aint = tex.getTextureData();
        int i = width * height;
        Arrays.fill(aint, i, i * 2, -8421377);
        Arrays.fill((int[])aint, i * 2, i * 3, (int)0);
        TextureUtil.allocateTexture(multitexid.base, width, height);
        TextureUtil.setTextureBlurMipmap(false, false);
        TextureUtil.setTextureClamped(false);
        TextureUtil.allocateTexture(multitexid.norm, width, height);
        TextureUtil.setTextureBlurMipmap(false, false);
        TextureUtil.setTextureClamped(false);
        TextureUtil.allocateTexture(multitexid.spec, width, height);
        TextureUtil.setTextureBlurMipmap(false, false);
        TextureUtil.setTextureClamped(false);
        GlStateManager.bindTexture(multitexid.base);
    }

    public static void updateDynamicTexture(int texID, int[] src, int width, int height, DynamicTexture tex)
    {
        MultiTexID multiTexID = tex.getMultiTexID();
        GlStateManager.bindTexture(multiTexID.base);
        updateDynTexSubImage1(src, width, height, 0, 0, 0);
        GlStateManager.bindTexture(multiTexID.norm);
        updateDynTexSubImage1(src, width, height, 0, 0, 1);
        GlStateManager.bindTexture(multiTexID.spec);
        updateDynTexSubImage1(src, width, height, 0, 0, 2);
        GlStateManager.bindTexture(multiTexID.base);
    }

    public static void updateDynTexSubImage1(int[] src, int width, int height, int posX, int posY, int page)
    {
        int i = width * height;
        IntBuffer intBuffer = getIntBuffer(i);
        intBuffer.clear();
        int j = page * i;

        if (src.length >= j + i)
        {
            intBuffer.put(src, j, i).position(0).limit(i);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, posX, posY, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);
            intBuffer.clear();
        }
    }

    public static ITextureObject createDefaultTexture()
    {
        DynamicTexture dynamicTexture = new DynamicTexture(1, 1);
        dynamicTexture.getTextureData()[0] = -1;
        dynamicTexture.updateDynamicTexture();
        return dynamicTexture;
    }

    public static void allocateTextureMap(int texID, int mipmapLevels, int width, int height, Stitcher stitcher, TextureMap tex)
    {
        SMCLog.info("allocateTextureMap " + mipmapLevels + " " + width + " " + height + " ");
        tex.atlasWidth = width;
        tex.atlasHeight = height;
        MultiTexID multiTexID = getMultiTexID(tex);
        TextureUtil.allocateTextureImpl(multiTexID.base, mipmapLevels, width, height);

        if (Shaders.configNormalMap)
        {
            TextureUtil.allocateTextureImpl(multiTexID.norm, mipmapLevels, width, height);
        }

        if (Shaders.configSpecularMap)
        {
            TextureUtil.allocateTextureImpl(multiTexID.spec, mipmapLevels, width, height);
        }

        GlStateManager.bindTexture(texID);
    }

    public static void uploadTexSubForLoadAtlas(TextureMap textureMap, String iconName, int[][] data, int width, int height, int xoffset, int yoffset, boolean linear, boolean clamp)
    {
        MultiTexID multiTexID = textureMap.multiTex;
        TextureUtil.uploadTextureMipmap(data, width, height, xoffset, yoffset, linear, clamp);
        boolean flag = false;

        if (Shaders.configNormalMap)
        {
            int[][] aint = readImageAndMipmaps(textureMap, iconName + "_n", width, height, data.length, flag, -8421377);
            GlStateManager.bindTexture(multiTexID.norm);
            TextureUtil.uploadTextureMipmap(aint, width, height, xoffset, yoffset, linear, clamp);
        }

        if (Shaders.configSpecularMap)
        {
            int[][] aint1 = readImageAndMipmaps(textureMap, iconName + "_s", width, height, data.length, flag, 0);
            GlStateManager.bindTexture(multiTexID.spec);
            TextureUtil.uploadTextureMipmap(aint1, width, height, xoffset, yoffset, linear, clamp);
        }

        GlStateManager.bindTexture(multiTexID.base);
    }

    public static int[][] readImageAndMipmaps(TextureMap updatingTextureMap, String name, int width, int height, int numLevels, boolean border, int defColor)
    {
        MultiTexID multitexid = updatingTextureMap.multiTex;
        int[][] aint = new int[numLevels][];
        int[] aint1;
        aint[0] = aint1 = new int[width * height];
        boolean flag = false;
        BufferedImage bufferedimage = readImage(updatingTextureMap.completeResourceLocation(new ResourceLocation(name)));

        if (bufferedimage != null)
        {
            int i = bufferedimage.getWidth();
            int j = bufferedimage.getHeight();

            if (i + (border ? 16 : 0) == width)
            {
                flag = true;
                bufferedimage.getRGB(0, 0, i, i, aint1, 0, i);
            }
        }

        if (!flag)
        {
            Arrays.fill(aint1, defColor);
        }

        GlStateManager.bindTexture(multitexid.spec);
        aint = genMipmapsSimple(aint.length - 1, width, aint);
        return aint;
    }

    public static BufferedImage readImage(ResourceLocation resLoc)
    {
        try
        {
            if (!Config.hasResource(resLoc))
            {
                return null;
            }
            else
            {
                InputStream inputStream = Config.getResourceStream(resLoc);

                if (inputStream == null)
                {
                    return null;
                }
                else
                {
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    inputStream.close();
                    return bufferedImage;
                }
            }
        }
        catch (IOException caughtIoException)
        {
            return null;
        }
    }

    public static int[][] genMipmapsSimple(int maxLevel, int width, int[][] data)
    {
        for (int i = 1; i <= maxLevel; ++i)
        {
            if (data[i] == null)
            {
                int j = width >> i;
                int k = j * 2;
                int[] aint = data[i - 1];
                int[] aint1 = data[i] = new int[j * j];

                for (int row = 0; row < j; ++row)
                {
                    for (int l = 0; l < j; ++l)
                    {
                        int parentIndex = row * 2 * k + l * 2;
                        aint1[row * j + l] = blend4Simple(aint[parentIndex], aint[parentIndex + 1], aint[parentIndex + k], aint[parentIndex + k + 1]);
                    }
                }
            }
        }

        return data;
    }

    public static void uploadTexSub1(int[][] src, int width, int height, int posX, int posY, int page)
    {
        int i = width * height;
        IntBuffer intBuffer = getIntBuffer(i);
        int j = src.length;
        int k = 0;
        int l = width;
        int mipHeight = height;
        int mipX = posX;

        for (int mipY = posY; l > 0 && mipHeight > 0 && k < j; ++k)
        {
            int mipSize = l * mipHeight;
            int[] aint = src[k];
            intBuffer.clear();

            if (aint.length >= mipSize * (page + 1))
            {
                intBuffer.put(aint, mipSize * page, mipSize).position(0).limit(mipSize);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, k, mipX, mipY, l, mipHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);
            }

            l >>= 1;
            mipHeight >>= 1;
            mipX >>= 1;
            mipY >>= 1;
        }

        intBuffer.clear();
    }

    public static int blend4Alpha(int secondIntValue, int thirdIntValue, int fifthIntValue, int seventhIntValue)
    {
        int alpha0 = secondIntValue >>> 24 & 255;
        int alpha1 = thirdIntValue >>> 24 & 255;
        int alpha2 = fifthIntValue >>> 24 & 255;
        int alpha3 = seventhIntValue >>> 24 & 255;
        int alphaSum = alpha0 + alpha1 + alpha2 + alpha3;
        int alphaAverage = (alphaSum + 2) / 4;
        int alphaWeightSum;

        if (alphaSum != 0)
        {
            alphaWeightSum = alphaSum;
        }
        else
        {
            alphaWeightSum = 4;
            alpha0 = 1;
            alpha1 = 1;
            alpha2 = 1;
            alpha3 = 1;
        }

        int halfWeight = (alphaWeightSum + 1) / 2;
        int blendedColor = alphaAverage << 24 | ((secondIntValue >>> 16 & 255) * alpha0 + (thirdIntValue >>> 16 & 255) * alpha1 + (fifthIntValue >>> 16 & 255) * alpha2 + (seventhIntValue >>> 16 & 255) * alpha3 + halfWeight) / alphaWeightSum << 16 | ((secondIntValue >>> 8 & 255) * alpha0 + (thirdIntValue >>> 8 & 255) * alpha1 + (fifthIntValue >>> 8 & 255) * alpha2 + (seventhIntValue >>> 8 & 255) * alpha3 + halfWeight) / alphaWeightSum << 8 | ((secondIntValue >>> 0 & 255) * alpha0 + (thirdIntValue >>> 0 & 255) * alpha1 + (fifthIntValue >>> 0 & 255) * alpha2 + (seventhIntValue >>> 0 & 255) * alpha3 + halfWeight) / alphaWeightSum << 0;
        return blendedColor;
    }

    public static int blend4Simple(int intValue, int fourthIntValue, int sixthIntValue, int eighthIntValue)
    {
        int i = ((intValue >>> 24 & 255) + (fourthIntValue >>> 24 & 255) + (sixthIntValue >>> 24 & 255) + (eighthIntValue >>> 24 & 255) + 2) / 4 << 24 | ((intValue >>> 16 & 255) + (fourthIntValue >>> 16 & 255) + (sixthIntValue >>> 16 & 255) + (eighthIntValue >>> 16 & 255) + 2) / 4 << 16 | ((intValue >>> 8 & 255) + (fourthIntValue >>> 8 & 255) + (sixthIntValue >>> 8 & 255) + (eighthIntValue >>> 8 & 255) + 2) / 4 << 8 | ((intValue >>> 0 & 255) + (fourthIntValue >>> 0 & 255) + (sixthIntValue >>> 0 & 255) + (eighthIntValue >>> 0 & 255) + 2) / 4 << 0;
        return i;
    }

    public static void genMipmapAlpha(int[] aint, int offset, int width, int height)
    {
        Math.min(width, height);
        int sixteenthIntValue = offset;
        int nineteenthIntValue = width;
        int eleventhIntValue = height;
        int thirteenthIntValue = 0;
        int eighteenthIntValue = 0;
        int ninthIntValue = 0;
        int i;

        for (i = 0; nineteenthIntValue > 1 && eleventhIntValue > 1; sixteenthIntValue = thirteenthIntValue)
        {
            thirteenthIntValue = sixteenthIntValue + nineteenthIntValue * eleventhIntValue;
            eighteenthIntValue = nineteenthIntValue / 2;
            ninthIntValue = eleventhIntValue / 2;

            for (int row = 0; row < ninthIntValue; ++row)
            {
                int dstRowOffset = thirteenthIntValue + row * eighteenthIntValue;
                int srcRowOffset = sixteenthIntValue + row * 2 * nineteenthIntValue;

                for (int col = 0; col < eighteenthIntValue; ++col)
                {
                    aint[dstRowOffset + col] = blend4Alpha(aint[srcRowOffset + col * 2], aint[srcRowOffset + col * 2 + 1], aint[srcRowOffset + nineteenthIntValue + col * 2], aint[srcRowOffset + nineteenthIntValue + col * 2 + 1]);
                }
            }

            ++i;
            nineteenthIntValue = eighteenthIntValue;
            eleventhIntValue = ninthIntValue;
        }

        while (i > 0)
        {
            --i;
            nineteenthIntValue = width >> i;
            eleventhIntValue = height >> i;
            sixteenthIntValue = thirteenthIntValue - nineteenthIntValue * eleventhIntValue;
            int fillIndex = sixteenthIntValue;

            for (int row = 0; row < eleventhIntValue; ++row)
            {
                for (int col = 0; col < nineteenthIntValue; ++col)
                {
                    if (aint[fillIndex] == 0)
                    {
                        aint[fillIndex] = aint[thirteenthIntValue + row / 2 * eighteenthIntValue + col / 2] & 16777215;
                    }

                    ++fillIndex;
                }
            }

            thirteenthIntValue = sixteenthIntValue;
            eighteenthIntValue = nineteenthIntValue;
        }
    }

    public static void genMipmapSimple(int[] aint, int offset, int width, int height)
    {
        Math.min(width, height);
        int fifteenthIntValue = offset;
        int twentiethIntValue = width;
        int twelfthIntValue = height;
        int fourteenthIntValue = 0;
        int seventeenthIntValue = 0;
        int tenthIntValue = 0;
        int i;

        for (i = 0; twentiethIntValue > 1 && twelfthIntValue > 1; fifteenthIntValue = fourteenthIntValue)
        {
            fourteenthIntValue = fifteenthIntValue + twentiethIntValue * twelfthIntValue;
            seventeenthIntValue = twentiethIntValue / 2;
            tenthIntValue = twelfthIntValue / 2;

            for (int row = 0; row < tenthIntValue; ++row)
            {
                int dstRowOffset = fourteenthIntValue + row * seventeenthIntValue;
                int srcRowOffset = fifteenthIntValue + row * 2 * twentiethIntValue;

                for (int col = 0; col < seventeenthIntValue; ++col)
                {
                    aint[dstRowOffset + col] = blend4Simple(aint[srcRowOffset + col * 2], aint[srcRowOffset + col * 2 + 1], aint[srcRowOffset + twentiethIntValue + col * 2], aint[srcRowOffset + twentiethIntValue + col * 2 + 1]);
                }
            }

            ++i;
            twentiethIntValue = seventeenthIntValue;
            twelfthIntValue = tenthIntValue;
        }

        while (i > 0)
        {
            --i;
            twentiethIntValue = width >> i;
            twelfthIntValue = height >> i;
            fifteenthIntValue = fourteenthIntValue - twentiethIntValue * twelfthIntValue;
            int fillIndex = fifteenthIntValue;

            for (int row = 0; row < twelfthIntValue; ++row)
            {
                for (int col = 0; col < twentiethIntValue; ++col)
                {
                    if (aint[fillIndex] == 0)
                    {
                        aint[fillIndex] = aint[fourteenthIntValue + row / 2 * seventeenthIntValue + col / 2] & 16777215;
                    }

                    ++fillIndex;
                }
            }

            fourteenthIntValue = fifteenthIntValue;
            seventeenthIntValue = twentiethIntValue;
        }
    }

    public static boolean isSemiTransparent(int[] aint, int width, int height)
    {
        int i = width * height;

        if (aint[0] >>> 24 == 255 && aint[i - 1] == 0)
        {
            return true;
        }
        else
        {
            for (int j = 0; j < i; ++j)
            {
                int k = aint[j] >>> 24;

                if (k != 0 && k != 255)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static void updateSubTex1(int[] src, int width, int height, int posX, int posY)
    {
        int i = 0;
        int j = width;
        int k = height;
        int l = posX;

        for (int mipY = posY; j > 0 && k > 0; mipY /= 2)
        {
            GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, i, l, mipY, 0, 0, j, k);
            ++i;
            j /= 2;
            k /= 2;
            l /= 2;
        }
    }

    public static void setupTexture(MultiTexID multiTex, int[] src, int width, int height, boolean linear, boolean clamp)
    {
        int i = linear ? 9729 : 9728;
        int j = clamp ? 33071 : 10497;
        int k = width * height;
        IntBuffer intBuffer = getIntBuffer(k);
        intBuffer.clear();
        intBuffer.put(src, 0, k).position(0).limit(k);
        GlStateManager.bindTexture(multiTex.base);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, i);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, i);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, j);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, j);
        intBuffer.put(src, k, k).position(0).limit(k);
        GlStateManager.bindTexture(multiTex.norm);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, i);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, i);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, j);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, j);
        intBuffer.put(src, k * 2, k).position(0).limit(k);
        GlStateManager.bindTexture(multiTex.spec);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, i);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, i);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, j);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, j);
        GlStateManager.bindTexture(multiTex.base);
    }

    public static void updateSubImage(MultiTexID multiTex, int[] src, int width, int height, int posX, int posY, boolean linear, boolean clamp)
    {
        int i = width * height;
        IntBuffer intBuffer = getIntBuffer(i);
        intBuffer.clear();
        intBuffer.put(src, 0, i);
        intBuffer.position(0).limit(i);
        GlStateManager.bindTexture(multiTex.base);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, posX, posY, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);

        if (src.length == i * 3)
        {
            intBuffer.clear();
            intBuffer.put(src, i, i).position(0);
            intBuffer.position(0).limit(i);
        }

        GlStateManager.bindTexture(multiTex.norm);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, posX, posY, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);

        if (src.length == i * 3)
        {
            intBuffer.clear();
            intBuffer.put(src, i * 2, i);
            intBuffer.position(0).limit(i);
        }

        GlStateManager.bindTexture(multiTex.spec);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, posX, posY, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)intBuffer);
        GlStateManager.setActiveTexture(33984);
    }

    public static ResourceLocation getNSMapLocation(ResourceLocation location, String mapName)
    {
        if (location == null)
        {
            return null;
        }
        else
        {
            String s = location.getResourcePath();
            String[] astring = s.split(".png");
            String basePath = astring[0];
            return new ResourceLocation(location.getResourceDomain(), basePath + "_" + mapName + ".png");
        }
    }

    public static void loadNSMap(IResourceManager manager, ResourceLocation location, int width, int height, int[] aint)
    {
        if (Shaders.configNormalMap)
        {
            loadNSMap1(manager, getNSMapLocation(location, "n"), width, height, aint, width * height, -8421377);
        }

        if (Shaders.configSpecularMap)
        {
            loadNSMap1(manager, getNSMapLocation(location, "s"), width, height, aint, width * height * 2, 0);
        }
    }

    private static void loadNSMap1(IResourceManager manager, ResourceLocation location, int width, int height, int[] aint, int offset, int defaultColor)
    {
        if (!loadNSMapFile(manager, location, width, height, aint, offset))
        {
            Arrays.fill(aint, offset, offset + width * height, defaultColor);
        }
    }

    private static boolean loadNSMapFile(IResourceManager manager, ResourceLocation location, int width, int height, int[] aint, int offset)
    {
        if (location == null)
        {
            return false;
        }
        else
        {
            try
            {
                IResource iresource = manager.getResource(location);
                BufferedImage bufferedImage = ImageIO.read(iresource.getInputStream());

                if (bufferedImage == null)
                {
                    return false;
                }
                else if (bufferedImage.getWidth() == width && bufferedImage.getHeight() == height)
                {
                    bufferedImage.getRGB(0, 0, width, height, aint, offset, width);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch (IOException caughtIoException)
            {
                return false;
            }
        }
    }

    public static int loadSimpleTexture(int textureID, BufferedImage bufferedimage, boolean linear, boolean clamp, IResourceManager resourceManager, ResourceLocation location, MultiTexID multiTex)
    {
        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int k = i * j;
        int[] aint = getIntArray(k * 3);
        bufferedimage.getRGB(0, 0, i, j, aint, 0, i);
        loadNSMap(resourceManager, location, i, j, aint);
        setupTexture(multiTex, aint, i, j, linear, clamp);
        return textureID;
    }

    public static void mergeImage(int[] aint, int dstoff, int srcoff, int size)
    {
    }

    public static int blendColor(int color1, int color2, int factor1)
    {
        int i = 255 - factor1;
        return ((color1 >>> 24 & 255) * factor1 + (color2 >>> 24 & 255) * i) / 255 << 24 | ((color1 >>> 16 & 255) * factor1 + (color2 >>> 16 & 255) * i) / 255 << 16 | ((color1 >>> 8 & 255) * factor1 + (color2 >>> 8 & 255) * i) / 255 << 8 | ((color1 >>> 0 & 255) * factor1 + (color2 >>> 0 & 255) * i) / 255 << 0;
    }

    public static void loadLayeredTexture(LayeredTexture tex, IResourceManager manager, List list)
    {
        int i = 0;
        int j = 0;
        int k = 0;
        int[] aint = null;

        for (Object o : list)
        {
            String s = (String) o;
            if (s != null)
            {
                try
                {
                    ResourceLocation resourcelocation = new ResourceLocation(s);
                    InputStream inputstream = manager.getResource(resourcelocation).getInputStream();
                    BufferedImage bufferedimage = ImageIO.read(inputstream);

                    if (k == 0)
                    {
                        i = bufferedimage.getWidth();
                        j = bufferedimage.getHeight();
                        k = i * j;
                        aint = createAIntImage(k, 0);
                    }

                    int[] aint1 = getIntArray(k * 3);
                    bufferedimage.getRGB(0, 0, i, j, aint1, 0, i);
                    loadNSMap(manager, resourcelocation, i, j, aint1);

                    for (int l = 0; l < k; ++l)
                    {
                        int alpha = aint1[l] >>> 24 & 255;
                        aint[k * 0 + l] = blendColor(aint1[k * 0 + l], aint[k * 0 + l], alpha);
                        aint[k * 1 + l] = blendColor(aint1[k * 1 + l], aint[k * 1 + l], alpha);
                        aint[k * 2 + l] = blendColor(aint1[k * 2 + l], aint[k * 2 + l], alpha);
                    }
                }
                catch (IOException ioexception)
                {
                    net.minecraft.src.Config.warn(ioexception.getClass().getName() + ": " + ioexception.getMessage(), ioexception);
                }
            }
        }

        setupTexture(tex.getMultiTexID(), aint, i, j, false, false);
    }

    public static void updateTextureMinMagFilter()
    {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject itextureobject = textureManager.getTexture(TextureMap.locationBlocksTexture);

        if (itextureobject != null)
        {
            MultiTexID multiTexID = itextureobject.getMultiTexID();
            GlStateManager.bindTexture(multiTexID.base);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.texMinFilValue[Shaders.configTexMinFilB]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, Shaders.texMagFilValue[Shaders.configTexMagFilB]);
            GlStateManager.bindTexture(multiTexID.norm);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.texMinFilValue[Shaders.configTexMinFilN]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, Shaders.texMagFilValue[Shaders.configTexMagFilN]);
            GlStateManager.bindTexture(multiTexID.spec);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.texMinFilValue[Shaders.configTexMinFilS]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, Shaders.texMagFilValue[Shaders.configTexMagFilS]);
            GlStateManager.bindTexture(0);
        }
    }

    public static int[][] getFrameTexData(int[][] src, int width, int height, int frameIndex)
    {
        int i = src.length;
        int[][] aint = new int[i][];

        for (int j = 0; j < i; ++j)
        {
            int[] aint1 = src[j];

            if (aint1 != null)
            {
                int k = (width >> j) * (height >> j);
                int[] aint2 = new int[k * 3];
                aint[j] = aint2;
                int l = aint1.length / 3;
                int sourceOffset = k * frameIndex;
                int destOffset = 0;
                System.arraycopy(aint1, sourceOffset, aint2, destOffset, k);
                sourceOffset = sourceOffset + l;
                destOffset = destOffset + k;
                System.arraycopy(aint1, sourceOffset, aint2, destOffset, k);
                sourceOffset = sourceOffset + l;
                destOffset = destOffset + k;
                System.arraycopy(aint1, sourceOffset, aint2, destOffset, k);
            }
        }

        return aint;
    }

    public static int[][] prepareAF(TextureAtlasSprite tas, int[][] src, int width, int height)
    {
        boolean flag = true;
        return src;
    }

    public static void fixTransparentColor(TextureAtlasSprite tas, int[] aint)
    {
    }
}
