package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.optifine.SmartAnimations;
import net.optifine.shaders.Shaders;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;

public class TextureAtlasSprite
{
    private final String iconName;
    protected List<int[][]> framesTextureData = Lists.<int[][]>newArrayList();
    protected int[][] interpolatedFrameData;
    private AnimationMetadataSection animationMetadata;
    protected boolean rotated;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    protected int frameCounter;
    protected int tickCounter;
    private static String locationNameClock = "builtin/clock";
    private static String locationNameCompass = "builtin/compass";
    private int indexInMap = -1;
    public float baseU;
    public float baseV;
    public int sheetWidth;
    public int sheetHeight;
    public int glSpriteTextureId = -1;
    public TextureAtlasSprite spriteSingle = null;
    public boolean isSpriteSingle = false;
    public int mipmapLevels = 0;
    public TextureAtlasSprite spriteNormal = null;
    public TextureAtlasSprite spriteSpecular = null;
    public boolean isShadersSprite = false;
    public boolean isEmissive = false;
    public TextureAtlasSprite spriteEmissive = null;
    private int animationIndex = -1;
    private boolean animationActive = false;

    private TextureAtlasSprite(String spriteName, boolean isSpriteSingleIn)
    {
        this.iconName = spriteName;
        this.isSpriteSingle = isSpriteSingleIn;
    }

    public TextureAtlasSprite(String spriteName)
    {
        this.iconName = spriteName;

        if (Config.isMultiTexture())
        {
            this.spriteSingle = new TextureAtlasSprite(this.getIconName() + ".spriteSingle", true);
        }
    }

    protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation spriteResourceLocation)
    {
        String spriteName = spriteResourceLocation.toString();
        return (TextureAtlasSprite)(locationNameClock.equals(spriteName) ? new TextureClock(spriteName) : (locationNameCompass.equals(spriteName) ? new TextureCompass(spriteName) : new TextureAtlasSprite(spriteName)));
    }

    public static void setLocationNameClock(String clockName)
    {
        locationNameClock = clockName;
    }

    public static void setLocationNameCompass(String compassName)
    {
        locationNameCompass = compassName;
    }

    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
    {
        this.originX = originInX;
        this.originY = originInY;
        this.rotated = rotatedIn;
        float uEpsilon = (float)(0.009999999776482582D / (double)inX);
        float vEpsilon = (float)(0.009999999776482582D / (double)inY);
        this.minU = (float)originInX / (float)((double)inX) + uEpsilon;
        this.maxU = (float)(originInX + this.width) / (float)((double)inX) - uEpsilon;
        this.minV = (float)originInY / (float)inY + vEpsilon;
        this.maxV = (float)(originInY + this.height) / (float)inY - vEpsilon;
        this.baseU = Math.min(this.minU, this.maxU);
        this.baseV = Math.min(this.minV, this.maxV);

        if (this.spriteSingle != null)
        {
            this.spriteSingle.initSprite(this.width, this.height, 0, 0, false);
        }

        if (this.spriteNormal != null)
        {
            this.spriteNormal.copyFrom(this);
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.copyFrom(this);
        }
    }

    public void copyFrom(TextureAtlasSprite atlasSpirit)
    {
        this.originX = atlasSpirit.originX;
        this.originY = atlasSpirit.originY;
        this.width = atlasSpirit.width;
        this.height = atlasSpirit.height;
        this.rotated = atlasSpirit.rotated;
        this.minU = atlasSpirit.minU;
        this.maxU = atlasSpirit.maxU;
        this.minV = atlasSpirit.minV;
        this.maxV = atlasSpirit.maxV;

        if (atlasSpirit != Config.getTextureMap().getMissingSprite())
        {
            this.indexInMap = atlasSpirit.indexInMap;
        }

        this.baseU = atlasSpirit.baseU;
        this.baseV = atlasSpirit.baseV;
        this.sheetWidth = atlasSpirit.sheetWidth;
        this.sheetHeight = atlasSpirit.sheetHeight;
        this.glSpriteTextureId = atlasSpirit.glSpriteTextureId;
        this.mipmapLevels = atlasSpirit.mipmapLevels;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.initSprite(this.width, this.height, 0, 0, false);
        }

        this.animationIndex = atlasSpirit.animationIndex;
    }

    public int getOriginX()
    {
        return this.originX;
    }

    public int getOriginY()
    {
        return this.originY;
    }

    public int getIconWidth()
    {
        return this.width;
    }

    public int getIconHeight()
    {
        return this.height;
    }

    public float getMinU()
    {
        return this.minU;
    }

    public float getMaxU()
    {
        return this.maxU;
    }

    public float getInterpolatedU(double u)
    {
        float uRange = this.maxU - this.minU;
        return this.minU + uRange * (float)u / 16.0F;
    }

    public float getMinV()
    {
        return this.minV;
    }

    public float getMaxV()
    {
        return this.maxV;
    }

    public float getInterpolatedV(double v)
    {
        float vRange = this.maxV - this.minV;
        return this.minV + vRange * ((float)v / 16.0F);
    }

    public String getIconName()
    {
        return this.iconName;
    }

    public void updateAnimation()
    {
        if (this.animationMetadata != null)
        {
            this.animationActive = SmartAnimations.isActive() ? SmartAnimations.isSpriteRendered(this.animationIndex) : true;
            ++this.tickCounter;

            if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter))
            {
                int previousFrameIndex = this.animationMetadata.getFrameIndex(this.frameCounter);
                int frameCount = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
                this.frameCounter = (this.frameCounter + 1) % frameCount;
                this.tickCounter = 0;
                int nextFrameIndex = this.animationMetadata.getFrameIndex(this.frameCounter);
                boolean blur = false;
                boolean clamp = this.isSpriteSingle;

                if (!this.animationActive)
                {
                    return;
                }

                if (previousFrameIndex != nextFrameIndex && nextFrameIndex >= 0 && nextFrameIndex < this.framesTextureData.size())
                {
                    TextureUtil.uploadTextureMipmap(this.framesTextureData.get(nextFrameIndex), this.width, this.height, this.originX, this.originY, blur, clamp);
                }
            }
            else if (this.animationMetadata.isInterpolate())
            {
                if (!this.animationActive)
                {
                    return;
                }

                this.updateAnimationInterpolated();
            }
        }
    }

    private void updateAnimationInterpolated()
    {
        double interpolationWeight = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
        int currentFrameIndex = this.animationMetadata.getFrameIndex(this.frameCounter);
        int frameCount = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
        int nextFrameIndex = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % frameCount);

        if (currentFrameIndex != nextFrameIndex && nextFrameIndex >= 0 && nextFrameIndex < this.framesTextureData.size())
        {
            int[][] currentFrameData = this.framesTextureData.get(currentFrameIndex);
            int[][] nextFrameData = this.framesTextureData.get(nextFrameIndex);

            if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != currentFrameData.length)
            {
                this.interpolatedFrameData = new int[currentFrameData.length][];
            }

            for (int mipLevel = 0; mipLevel < currentFrameData.length; ++mipLevel)
            {
                if (this.interpolatedFrameData[mipLevel] == null)
                {
                    this.interpolatedFrameData[mipLevel] = new int[currentFrameData[mipLevel].length];
                }

                if (mipLevel < nextFrameData.length && nextFrameData[mipLevel].length == currentFrameData[mipLevel].length)
                {
                    for (int pixelIndex = 0; pixelIndex < currentFrameData[mipLevel].length; ++pixelIndex)
                    {
                        int currentPixel = currentFrameData[mipLevel][pixelIndex];
                        int nextPixel = nextFrameData[mipLevel][pixelIndex];
                        int blendedRed = (int)((double)((currentPixel & 16711680) >> 16) * interpolationWeight + (double)((nextPixel & 16711680) >> 16) * (1.0D - interpolationWeight));
                        int blendedGreen = (int)((double)((currentPixel & 65280) >> 8) * interpolationWeight + (double)((nextPixel & 65280) >> 8) * (1.0D - interpolationWeight));
                        int blendedBlue = (int)((double)(currentPixel & 255) * interpolationWeight + (double)(nextPixel & 255) * (1.0D - interpolationWeight));
                        this.interpolatedFrameData[mipLevel][pixelIndex] = currentPixel & -16777216 | blendedRed << 16 | blendedGreen << 8 | blendedBlue;
                    }
                }
            }

            TextureUtil.uploadTextureMipmap(this.interpolatedFrameData, this.width, this.height, this.originX, this.originY, false, false);
        }
    }

    public int[][] getFrameTextureData(int index)
    {
        return this.framesTextureData.get(index);
    }

    public int getFrameCount()
    {
        return this.framesTextureData.size();
    }

    public void setIconWidth(int newWidth)
    {
        this.width = newWidth;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setIconWidth(this.width);
        }
    }

    public void setIconHeight(int newHeight)
    {
        this.height = newHeight;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setIconHeight(this.height);
        }
    }

    public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta) throws IOException
    {
        this.resetSprite();
        int imageWidth = images[0].getWidth();
        int imageHeight = images[0].getHeight();
        this.width = imageWidth;
        this.height = imageHeight;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.width = this.width;
            this.spriteSingle.height = this.height;
        }

        int[][] imageDataByMipLevel = new int[images.length][];

        for (int mipLevel = 0; mipLevel < images.length; ++mipLevel)
        {
            BufferedImage mipImage = images[mipLevel];

            if (mipImage != null)
            {
                if (this.width >> mipLevel != mipImage.getWidth())
                {
                    mipImage = TextureUtils.scaleImage(mipImage, this.width >> mipLevel);
                }

                if (mipLevel > 0 && (mipImage.getWidth() != imageWidth >> mipLevel || mipImage.getHeight() != imageHeight >> mipLevel))
                {
                    throw new RuntimeException("Unable to load miplevel: " + mipLevel + ", image is size: " + mipImage.getWidth() + "x" + mipImage.getHeight() + ", expected " + (imageWidth >> mipLevel) + "x" + (imageHeight >> mipLevel));
                }

                imageDataByMipLevel[mipLevel] = new int[mipImage.getWidth() * mipImage.getHeight()];
                mipImage.getRGB(0, 0, mipImage.getWidth(), mipImage.getHeight(), imageDataByMipLevel[mipLevel], 0, mipImage.getWidth());
            }
        }

        if (meta == null)
        {
            if (imageHeight != imageWidth)
            {
                throw new RuntimeException("broken aspect ratio and not an animation");
            }

            this.framesTextureData.add(imageDataByMipLevel);
        }
        else
        {
            int frameCount = imageHeight / imageWidth;
            int frameWidth = imageWidth;
            int frameHeight = imageWidth;
            this.height = this.width;

            if (meta.getFrameCount() > 0)
            {
                for (Integer frameIndexValue : meta.getFrameIndexSet())
                {
                    int frameIndex = frameIndexValue.intValue();

                    if (frameIndex >= frameCount)
                    {
                        throw new RuntimeException("invalid frameindex " + frameIndex);
                    }

                    this.allocateFrameTextureData(frameIndex);
                    this.framesTextureData.set(frameIndex, getFrameTextureData(imageDataByMipLevel, frameWidth, frameHeight, frameIndex));
                }

                this.animationMetadata = meta;
            }
            else
            {
                List<AnimationFrame> animationFrames = Lists.<AnimationFrame>newArrayList();

                for (int frameIndex = 0; frameIndex < frameCount; ++frameIndex)
                {
                    this.framesTextureData.add(getFrameTextureData(imageDataByMipLevel, frameWidth, frameHeight, frameIndex));
                    animationFrames.add(new AnimationFrame(frameIndex, -1));
                }

                this.animationMetadata = new AnimationMetadataSection(animationFrames, this.width, this.height, meta.getFrameTime(), meta.isInterpolate());
            }
        }

        if (!this.isShadersSprite)
        {
            if (Config.isShaders())
            {
                this.loadShadersSprites();
            }

            for (int frameIndex = 0; frameIndex < this.framesTextureData.size(); ++frameIndex)
            {
                int[][] frameData = this.framesTextureData.get(frameIndex);

                if (frameData != null && !this.iconName.startsWith("minecraft:blocks/leaves_"))
                {
                    for (int mipLevel = 0; mipLevel < frameData.length; ++mipLevel)
                    {
                        int[] mipData = frameData[mipLevel];
                        this.fixTransparentColor(mipData);
                    }
                }
            }

            if (this.spriteSingle != null)
            {
                this.spriteSingle.loadSprite(images, meta);
            }
        }
    }

    public void generateMipmaps(int level)
    {
        List<int[][]> mipmapFrames = Lists.<int[][]>newArrayList();

        for (int frameIndex = 0; frameIndex < this.framesTextureData.size(); ++frameIndex)
        {
            final int[][] frameData = this.framesTextureData.get(frameIndex);

            if (frameData != null)
            {
                try
                {
                    mipmapFrames.add(TextureUtil.generateMipmapData(level, this.width, frameData));
                }
                catch (Throwable throwable)
                {
                    CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Generating mipmaps for frame");
                    CrashReportCategory crashReportCategory = crashReport.makeCategory("Frame being iterated");
                    crashReportCategory.addCrashSection("Frame index", Integer.valueOf(frameIndex));
                    crashReportCategory.addCrashSectionCallable("Frame sizes", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            StringBuilder stringBuilder = new StringBuilder();

                            for (int[] mipData : frameData)
                            {
                                if (stringBuilder.length() > 0)
                                {
                                    stringBuilder.append(", ");
                                }

                                stringBuilder.append(mipData == null ? "null" : Integer.valueOf(mipData.length));
                            }

                            return stringBuilder.toString();
                        }
                    });
                    throw new ReportedException(crashReport);
                }
            }
        }

        this.setFramesTextureData(mipmapFrames);

        if (this.spriteSingle != null)
        {
            this.spriteSingle.generateMipmaps(level);
        }
    }

    private void allocateFrameTextureData(int index)
    {
        if (this.framesTextureData.size() <= index)
        {
            for (int frameIndex = this.framesTextureData.size(); frameIndex <= index; ++frameIndex)
            {
                this.framesTextureData.add((int[][])null);
            }
        }

        if (this.spriteSingle != null)
        {
            this.spriteSingle.allocateFrameTextureData(index);
        }
    }

    private static int[][] getFrameTextureData(int[][] data, int rows, int columns, int frameIndex)
    {
        int[][] frameData = new int[data.length][];

        for (int mipLevel = 0; mipLevel < data.length; ++mipLevel)
        {
            int[] sourceMipData = data[mipLevel];

            if (sourceMipData != null)
            {
                frameData[mipLevel] = new int[(rows >> mipLevel) * (columns >> mipLevel)];
                System.arraycopy(sourceMipData, frameIndex * frameData[mipLevel].length, frameData[mipLevel], 0, frameData[mipLevel].length);
            }
        }

        return frameData;
    }

    public void clearFramesTextureData()
    {
        this.framesTextureData.clear();

        if (this.spriteSingle != null)
        {
            this.spriteSingle.clearFramesTextureData();
        }
    }

    public boolean hasAnimationMetadata()
    {
        return this.animationMetadata != null;
    }

    public void setFramesTextureData(List<int[][]> newFramesTextureData)
    {
        this.framesTextureData = newFramesTextureData;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setFramesTextureData(newFramesTextureData);
        }
    }

    private void resetSprite()
    {
        this.animationMetadata = null;
        this.setFramesTextureData(Lists.<int[][]>newArrayList());
        this.frameCounter = 0;
        this.tickCounter = 0;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.resetSprite();
        }
    }

    public String toString()
    {
        return "TextureAtlasSprite{name=\'" + this.iconName + '\'' + ", frameCount=" + this.framesTextureData.size() + ", rotated=" + this.rotated + ", x=" + this.originX + ", y=" + this.originY + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.minU + ", u1=" + this.maxU + ", v0=" + this.minV + ", v1=" + this.maxV + '}';
    }

    public boolean hasCustomLoader(IResourceManager resourceManager, ResourceLocation location)
    {
        return false;
    }

    public boolean load(IResourceManager resourceManager, ResourceLocation location)
    {
        return true;
    }

    public int getIndexInMap()
    {
        return this.indexInMap;
    }

    public void setIndexInMap(int indexInMap)
    {
        this.indexInMap = indexInMap;
    }

    public void updateIndexInMap(CounterInt counter)
    {
        if (this.indexInMap < 0)
        {
            this.indexInMap = counter.nextValue();
        }
    }

    public int getAnimationIndex()
    {
        return this.animationIndex;
    }

    public void setAnimationIndex(int animationIndexIn)
    {
        this.animationIndex = animationIndexIn;

        if (this.spriteNormal != null)
        {
            this.spriteNormal.setAnimationIndex(animationIndexIn);
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.setAnimationIndex(animationIndexIn);
        }
    }

    public boolean isAnimationActive()
    {
        return this.animationActive;
    }

    private void fixTransparentColor(int[] textureData)
    {
        if (textureData != null)
        {
            long redSum = 0L;
            long greenSum = 0L;
            long blueSum = 0L;
            long opaquePixelCount = 0L;

            for (int pixelIndex = 0; pixelIndex < textureData.length; ++pixelIndex)
            {
                int pixel = textureData[pixelIndex];
                int alpha = pixel >> 24 & 255;

                if (alpha >= 16)
                {
                    int red = pixel >> 16 & 255;
                    int green = pixel >> 8 & 255;
                    int blue = pixel & 255;
                    redSum += (long)red;
                    greenSum += (long)green;
                    blueSum += (long)blue;
                    ++opaquePixelCount;
                }
            }

            if (opaquePixelCount > 0L)
            {
                int averageRed = (int)(redSum / opaquePixelCount);
                int averageGreen = (int)(greenSum / opaquePixelCount);
                int averageBlue = (int)(blueSum / opaquePixelCount);
                int averageColor = averageRed << 16 | averageGreen << 8 | averageBlue;

                for (int pixelIndex = 0; pixelIndex < textureData.length; ++pixelIndex)
                {
                    int pixel = textureData[pixelIndex];
                    int alpha = pixel >> 24 & 255;

                    if (alpha <= 16)
                    {
                        textureData[pixelIndex] = averageColor;
                    }
                }
            }
        }
    }

    public double getSpriteU16(float u)
    {
        float uRange = this.maxU - this.minU;
        return (double)((u - this.minU) / uRange * 16.0F);
    }

    public double getSpriteV16(float v)
    {
        float vRange = this.maxV - this.minV;
        return (double)((v - this.minV) / vRange * 16.0F);
    }

    public void bindSpriteTexture()
    {
        if (this.glSpriteTextureId < 0)
        {
            this.glSpriteTextureId = TextureUtil.glGenTextures();
            TextureUtil.allocateTextureImpl(this.glSpriteTextureId, this.mipmapLevels, this.width, this.height);
            TextureUtils.applyAnisotropicLevel();
        }

        TextureUtils.bindTexture(this.glSpriteTextureId);
    }

    public void deleteSpriteTexture()
    {
        if (this.glSpriteTextureId >= 0)
        {
            TextureUtil.deleteTexture(this.glSpriteTextureId);
            this.glSpriteTextureId = -1;
        }
    }

    public float toSingleU(float u)
    {
        u = u - this.baseU;
        float sheetToSpriteScale = (float)this.sheetWidth / (float)this.width;
        u = u * sheetToSpriteScale;
        return u;
    }

    public float toSingleV(float v)
    {
        v = v - this.baseV;
        float sheetToSpriteScale = (float)this.sheetHeight / (float)this.height;
        v = v * sheetToSpriteScale;
        return v;
    }

    public List<int[][]> getFramesTextureData()
    {
        List<int[][]> framesCopy = new ArrayList<int[][]>(this.framesTextureData.size());
        framesCopy.addAll(this.framesTextureData);
        return framesCopy;
    }

    public AnimationMetadataSection getAnimationMetadata()
    {
        return this.animationMetadata;
    }

    public void setAnimationMetadata(AnimationMetadataSection animationMetadataIn)
    {
        this.animationMetadata = animationMetadataIn;
    }

    private void loadShadersSprites()
    {
        if (Shaders.configNormalMap)
        {
            String normalSpriteName = this.iconName + "_n";
            ResourceLocation normalResourceLocation = new ResourceLocation(normalSpriteName);
            normalResourceLocation = Config.getTextureMap().completeResourceLocation(normalResourceLocation);

            if (Config.hasResource(normalResourceLocation))
            {
                this.spriteNormal = new TextureAtlasSprite(normalSpriteName);
                this.spriteNormal.isShadersSprite = true;
                this.spriteNormal.copyFrom(this);
                this.spriteNormal.generateMipmaps(this.mipmapLevels);
            }
        }

        if (Shaders.configSpecularMap)
        {
            String specularSpriteName = this.iconName + "_s";
            ResourceLocation specularResourceLocation = new ResourceLocation(specularSpriteName);
            specularResourceLocation = Config.getTextureMap().completeResourceLocation(specularResourceLocation);

            if (Config.hasResource(specularResourceLocation))
            {
                this.spriteSpecular = new TextureAtlasSprite(specularSpriteName);
                this.spriteSpecular.isShadersSprite = true;
                this.spriteSpecular.copyFrom(this);
                this.spriteSpecular.generateMipmaps(this.mipmapLevels);
            }
        }
    }
}
