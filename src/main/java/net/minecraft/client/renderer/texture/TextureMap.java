package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.optifine.BetterGrass;
import net.optifine.ConnectedTextures;
import net.optifine.CustomItems;
import net.optifine.EmissiveTextures;
import net.optifine.SmartAnimations;
import net.optifine.shaders.ShadersTex;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureMap extends AbstractTexture implements ITickableTextureObject
{
    private static final boolean ENABLE_SKIP = Boolean.parseBoolean(System.getProperty("fml.skipFirstTextureLoad", "true"));
    private static final Logger logger = LogManager.getLogger();
    public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
    public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
    private final List<TextureAtlasSprite> listAnimatedSprites;
    private final Map<String, TextureAtlasSprite> mapRegisteredSprites;
    private final Map<String, TextureAtlasSprite> mapUploadedSprites;
    private final String basePath;
    private final IIconCreator iconCreator;
    private int mipmapLevels;
    private final TextureAtlasSprite missingImage;
    private boolean skipFirst;
    private TextureAtlasSprite[] iconGrid;
    private int iconGridSize;
    private int iconGridCountX;
    private int iconGridCountY;
    private double iconGridSizeU;
    private double iconGridSizeV;
    private CounterInt counterIndexInMap;
    public int atlasWidth;
    public int atlasHeight;
    private int countAnimationsActive;
    private int frameCountAnimations;

    public TextureMap(String basePathIn)
    {
        this(basePathIn, (IIconCreator)null);
    }

    public TextureMap(String basePathIn, boolean skipFirstIn)
    {
        this(basePathIn, (IIconCreator)null, skipFirstIn);
    }

    public TextureMap(String basePathIn, IIconCreator iconCreatorIn)
    {
        this(basePathIn, iconCreatorIn, false);
    }

    public TextureMap(String basePathIn, IIconCreator iconCreatorIn, boolean skipFirstIn)
    {
        this.skipFirst = false;
        this.iconGrid = null;
        this.iconGridSize = -1;
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGridSizeU = -1.0D;
        this.iconGridSizeV = -1.0D;
        this.counterIndexInMap = new CounterInt(0);
        this.atlasWidth = 0;
        this.atlasHeight = 0;
        this.listAnimatedSprites = Lists.<TextureAtlasSprite>newArrayList();
        this.mapRegisteredSprites = Maps.<String, TextureAtlasSprite>newHashMap();
        this.mapUploadedSprites = Maps.<String, TextureAtlasSprite>newHashMap();
        this.missingImage = new TextureAtlasSprite("missingno");
        this.basePath = basePathIn;
        this.iconCreator = iconCreatorIn;
        this.skipFirst = skipFirstIn && ENABLE_SKIP;
    }

    private void initMissingImage()
    {
        int missingSize = this.getMinSpriteSize();
        int[] missingPixels = this.getMissingImageData(missingSize);
        this.missingImage.setIconWidth(missingSize);
        this.missingImage.setIconHeight(missingSize);
        int[][] missingFrames = new int[this.mipmapLevels + 1][];
        missingFrames[0] = missingPixels;
        this.missingImage.setFramesTextureData(Lists.newArrayList(new int[][][] {missingFrames}));
        this.missingImage.setIndexInMap(this.counterIndexInMap.nextValue());
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        if (this.iconCreator != null)
        {
            this.loadSprites(resourceManager, this.iconCreator);
        }
    }

    public void loadSprites(IResourceManager resourceManager, IIconCreator iconCreatorIn)
    {
        this.mapRegisteredSprites.clear();
        this.counterIndexInMap.reset();
        iconCreatorIn.registerSprites(this);

        if (this.mipmapLevels >= 4)
        {
            this.mipmapLevels = this.detectMaxMipmapLevel(this.mapRegisteredSprites, resourceManager);
            Config.log("Mipmap levels: " + this.mipmapLevels);
        }

        this.initMissingImage();
        this.deleteGlTexture();
        this.loadTextureAtlas(resourceManager);
    }

    public void loadTextureAtlas(IResourceManager resourceManager)
    {
        Config.dbg("Multitexture: " + Config.isMultiTexture());

        if (Config.isMultiTexture())
        {
            for (TextureAtlasSprite uploadedSprite : this.mapUploadedSprites.values())
            {
                uploadedSprite.deleteSpriteTexture();
            }
        }

        ConnectedTextures.updateIcons(this);
        CustomItems.updateIcons(this);
        BetterGrass.updateIcons(this);
        int maxTextureSize = TextureUtils.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(maxTextureSize, maxTextureSize, true, 0, this.mipmapLevels);
        this.mapUploadedSprites.clear();
        this.listAnimatedSprites.clear();
        int minSpriteDimension = Integer.MAX_VALUE;
        int minSpriteSize = this.getMinSpriteSize();
        this.iconGridSize = minSpriteSize;
        int minMipmapSize = 1 << this.mipmapLevels;
        int customLoaderCount = 0;
        int skippedCustomLoaderCount = 0;
        Iterator<Entry<String, TextureAtlasSprite>> registeredSpriteIterator = this.mapRegisteredSprites.entrySet().iterator();

        while (true)
        {
            if (registeredSpriteIterator.hasNext())
            {
                Entry<String, TextureAtlasSprite> registeredEntry = registeredSpriteIterator.next();

                if (!this.skipFirst)
                {
                    TextureAtlasSprite registeredSprite = registeredEntry.getValue();
                    ResourceLocation spriteLocation = new ResourceLocation(registeredSprite.getIconName());
                    ResourceLocation spriteResourceLocation = this.completeResourceLocation(spriteLocation, 0);
                    registeredSprite.updateIndexInMap(this.counterIndexInMap);

                    if (registeredSprite.hasCustomLoader(resourceManager, spriteLocation))
                    {
                        if (!registeredSprite.load(resourceManager, spriteLocation))
                        {
                            minSpriteDimension = Math.min(minSpriteDimension, Math.min(registeredSprite.getIconWidth(), registeredSprite.getIconHeight()));
                            stitcher.addSprite(registeredSprite);
                            Config.detail("Custom loader (skipped): " + registeredSprite);
                            ++skippedCustomLoaderCount;
                        }

                        Config.detail("Custom loader: " + registeredSprite);
                        ++customLoaderCount;
                        continue;
                    }

                    try
                    {
                        IResource spriteResource = resourceManager.getResource(spriteResourceLocation);
                        BufferedImage[] mipmapImages = new BufferedImage[1 + this.mipmapLevels];
                        mipmapImages[0] = TextureUtil.readBufferedImage(spriteResource.getInputStream());
                        int spriteWidth = mipmapImages[0].getWidth();
                        int spriteHeight = mipmapImages[0].getHeight();

                        if (spriteWidth < 1 || spriteHeight < 1)
                        {
                            Config.warn("Invalid sprite size: " + registeredSprite);
                            continue;
                        }

                        if (spriteWidth < minSpriteSize || this.mipmapLevels > 0)
                        {
                            int scaledWidth = this.mipmapLevels > 0 ? TextureUtils.scaleToGrid(spriteWidth, minSpriteSize) : TextureUtils.scaleToMin(spriteWidth, minSpriteSize);

                            if (scaledWidth != spriteWidth)
                            {
                                if (!TextureUtils.isPowerOfTwo(spriteWidth))
                                {
                                    Config.log("Scaled non power of 2: " + registeredSprite.getIconName() + ", " + spriteWidth + " -> " + scaledWidth);
                                }
                                else
                                {
                                    Config.log("Scaled too small texture: " + registeredSprite.getIconName() + ", " + spriteWidth + " -> " + scaledWidth);
                                }

                                int scaledHeight = spriteHeight * scaledWidth / spriteWidth;
                                mipmapImages[0] = TextureUtils.scaleImage(mipmapImages[0], scaledWidth);
                            }
                        }

                        TextureMetadataSection textureMetadata = (TextureMetadataSection)spriteResource.getMetadata("texture");

                        if (textureMetadata != null)
                        {
                            List<Integer> requestedMipmapLevels = textureMetadata.getListMipmaps();

                            if (!requestedMipmapLevels.isEmpty())
                            {
                                int mipmapWidth = mipmapImages[0].getWidth();
                                int mipmapHeight = mipmapImages[0].getHeight();

                                if (MathHelper.roundUpToPowerOfTwo(mipmapWidth) != mipmapWidth || MathHelper.roundUpToPowerOfTwo(mipmapHeight) != mipmapHeight)
                                {
                                    throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
                                }
                            }

                            Iterator mipmapIterator = requestedMipmapLevels.iterator();

                            while (mipmapIterator.hasNext())
                            {
                                int mipmapLevel = ((Integer)mipmapIterator.next()).intValue();

                                if (mipmapLevel > 0 && mipmapLevel < mipmapImages.length - 1 && mipmapImages[mipmapLevel] == null)
                                {
                                    ResourceLocation mipmapResourceLocation = this.completeResourceLocation(spriteLocation, mipmapLevel);

                                    try
                                    {
                                        mipmapImages[mipmapLevel] = TextureUtil.readBufferedImage(resourceManager.getResource(mipmapResourceLocation).getInputStream());
                                    }
                                    catch (IOException ioException)
                                    {
                                        logger.error("Unable to load miplevel {} from: {}", new Object[] {Integer.valueOf(mipmapLevel), mipmapResourceLocation, ioException});
                                    }
                                }
                            }
                        }

                        AnimationMetadataSection animationMetadata = (AnimationMetadataSection)spriteResource.getMetadata("animation");
                        registeredSprite.loadSprite(mipmapImages, animationMetadata);
                    }
                    catch (RuntimeException runtimeException)
                    {
                        logger.error((String)("Unable to parse metadata from " + spriteResourceLocation), (Throwable)runtimeException);
                        continue;
                    }
                    catch (IOException ioException)
                    {
                        logger.error("Using missing texture, unable to load " + spriteResourceLocation + ", " + ioException.getClass().getName());
                        continue;
                    }

                    minSpriteDimension = Math.min(minSpriteDimension, Math.min(registeredSprite.getIconWidth(), registeredSprite.getIconHeight()));
                    int spriteMinPowerOfTwo = Math.min(Integer.lowestOneBit(registeredSprite.getIconWidth()), Integer.lowestOneBit(registeredSprite.getIconHeight()));

                    if (spriteMinPowerOfTwo < minMipmapSize)
                    {
                        logger.warn("Texture {} with size {}x{} limits mip level from {} to {}", new Object[] {spriteResourceLocation, Integer.valueOf(registeredSprite.getIconWidth()), Integer.valueOf(registeredSprite.getIconHeight()), Integer.valueOf(MathHelper.calculateLogBaseTwo(minMipmapSize)), Integer.valueOf(MathHelper.calculateLogBaseTwo(spriteMinPowerOfTwo))});
                        minMipmapSize = spriteMinPowerOfTwo;
                    }

                    stitcher.addSprite(registeredSprite);
                    continue;
                }
            }

            if (customLoaderCount > 0)
            {
                Config.dbg("Custom loader sprites: " + customLoaderCount);
            }

            if (skippedCustomLoaderCount > 0)
            {
                Config.dbg("Custom loader sprites (skipped): " + skippedCustomLoaderCount);
            }

            int minimumPowerOfTwo = Math.min(minSpriteDimension, minMipmapSize);
            int targetMipmapLevel = MathHelper.calculateLogBaseTwo(minimumPowerOfTwo);

            if (targetMipmapLevel < 0)
            {
                targetMipmapLevel = 0;
            }

            if (targetMipmapLevel < this.mipmapLevels)
            {
                logger.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[] {this.basePath, Integer.valueOf(this.mipmapLevels), Integer.valueOf(targetMipmapLevel), Integer.valueOf(minimumPowerOfTwo)});
                this.mipmapLevels = targetMipmapLevel;
            }

            for (final TextureAtlasSprite mipmapSprite : this.mapRegisteredSprites.values())
            {
                if (this.skipFirst)
                {
                    break;
                }

                try
                {
                    mipmapSprite.generateMipmaps(this.mipmapLevels);
                }
                catch (Throwable throwable)
                {
                    CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Applying mipmap");
                    CrashReportCategory crashReportCategory = crashReport.makeCategory("Sprite being mipmapped");
                    crashReportCategory.addCrashSectionCallable("Sprite name", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return mipmapSprite.getIconName();
                        }
                    });
                    crashReportCategory.addCrashSectionCallable("Sprite size", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return mipmapSprite.getIconWidth() + " x " + mipmapSprite.getIconHeight();
                        }
                    });
                    crashReportCategory.addCrashSectionCallable("Sprite frames", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return mipmapSprite.getFrameCount() + " frames";
                        }
                    });
                    crashReportCategory.addCrashSection("Mipmap levels", Integer.valueOf(this.mipmapLevels));
                    throw new ReportedException(crashReport);
                }
            }

            this.missingImage.generateMipmaps(this.mipmapLevels);
            stitcher.addSprite(this.missingImage);
            this.skipFirst = false;

            try
            {
                stitcher.doStitch();
            }
            catch (StitcherException stitcherexception)
            {
                throw stitcherexception;
            }

            logger.info("Created: {}x{} {}-atlas", new Object[] {Integer.valueOf(stitcher.getCurrentWidth()), Integer.valueOf(stitcher.getCurrentHeight()), this.basePath});

            if (Config.isShaders())
            {
                ShadersTex.allocateTextureMap(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), stitcher, this);
            }
            else
            {
                TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
            }

            Map<String, TextureAtlasSprite> missingSprites = Maps.<String, TextureAtlasSprite>newHashMap(this.mapRegisteredSprites);

            for (TextureAtlasSprite stitchedSprite : stitcher.getStitchSlots())
            {
                String spriteName = stitchedSprite.getIconName();
                missingSprites.remove(spriteName);
                this.mapUploadedSprites.put(spriteName, stitchedSprite);

                try
                {
                    if (Config.isShaders())
                    {
                        ShadersTex.uploadTexSubForLoadAtlas(this, stitchedSprite.getIconName(), stitchedSprite.getFrameTextureData(0), stitchedSprite.getIconWidth(), stitchedSprite.getIconHeight(), stitchedSprite.getOriginX(), stitchedSprite.getOriginY(), false, false);
                    }
                    else
                    {
                        TextureUtil.uploadTextureMipmap(stitchedSprite.getFrameTextureData(0), stitchedSprite.getIconWidth(), stitchedSprite.getIconHeight(), stitchedSprite.getOriginX(), stitchedSprite.getOriginY(), false, false);
                    }
                }
                catch (Throwable throwable)
                {
                    CrashReport uploadCrashReport = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
                    CrashReportCategory uploadCrashCategory = uploadCrashReport.makeCategory("Texture being stitched together");
                    uploadCrashCategory.addCrashSection("Atlas path", this.basePath);
                    uploadCrashCategory.addCrashSection("Sprite", stitchedSprite);
                    throw new ReportedException(uploadCrashReport);
                }

                if (stitchedSprite.hasAnimationMetadata())
                {
                    stitchedSprite.setAnimationIndex(this.listAnimatedSprites.size());
                    this.listAnimatedSprites.add(stitchedSprite);
                }
            }

            for (TextureAtlasSprite missingSprite : missingSprites.values())
            {
                missingSprite.copyFrom(this.missingImage);
            }

            Config.log("Animated sprites: " + this.listAnimatedSprites.size());

            if (Config.isMultiTexture())
            {
                int atlasSheetWidth = stitcher.getCurrentWidth();
                int atlasSheetHeight = stitcher.getCurrentHeight();

                for (TextureAtlasSprite atlasSprite : stitcher.getStitchSlots())
                {
                    atlasSprite.sheetWidth = atlasSheetWidth;
                    atlasSprite.sheetHeight = atlasSheetHeight;
                    atlasSprite.mipmapLevels = this.mipmapLevels;
                    TextureAtlasSprite singleSprite = atlasSprite.spriteSingle;

                    if (singleSprite != null)
                    {
                        if (singleSprite.getIconWidth() <= 0)
                        {
                            singleSprite.setIconWidth(atlasSprite.getIconWidth());
                            singleSprite.setIconHeight(atlasSprite.getIconHeight());
                            singleSprite.initSprite(atlasSprite.getIconWidth(), atlasSprite.getIconHeight(), 0, 0, false);
                            singleSprite.clearFramesTextureData();
                            List<int[][]> frameTextureData = atlasSprite.getFramesTextureData();
                            singleSprite.setFramesTextureData(frameTextureData);
                            singleSprite.setAnimationMetadata(atlasSprite.getAnimationMetadata());
                        }

                        singleSprite.sheetWidth = atlasSheetWidth;
                        singleSprite.sheetHeight = atlasSheetHeight;
                        singleSprite.mipmapLevels = this.mipmapLevels;
                        singleSprite.setAnimationIndex(atlasSprite.getAnimationIndex());
                        atlasSprite.bindSpriteTexture();
                        boolean blurTexture = false;
                        boolean clampTexture = true;

                        try
                        {
                            TextureUtil.uploadTextureMipmap(singleSprite.getFrameTextureData(0), singleSprite.getIconWidth(), singleSprite.getIconHeight(), singleSprite.getOriginX(), singleSprite.getOriginY(), blurTexture, clampTexture);
                        }
                        catch (Exception exception)
                        {
                            Config.dbg("Error uploading sprite single: " + singleSprite + ", parent: " + atlasSprite);
                            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
                        }
                    }
                }

                Config.getMinecraft().getTextureManager().bindTexture(locationBlocksTexture);
            }

            this.updateIconGrid(stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

            if (Config.equals(System.getProperty("saveTextureMap"), "true"))
            {
                Config.dbg("Exporting texture map: " + this.basePath);
                TextureUtils.saveGlTexture("debug/" + this.basePath.replace('/', '_'), this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
            }

            return;
        }
    }

    public ResourceLocation completeResourceLocation(ResourceLocation location)
    {
        return this.completeResourceLocation(location, 0);
    }

    public ResourceLocation completeResourceLocation(ResourceLocation location, int mipmapLevel)
    {
        if (this.isAbsoluteLocation(location))
        {
            return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".png");
        }

        return mipmapLevel == 0
            ? new ResourceLocation(location.getResourceDomain(), this.basePath + "/" + location.getResourcePath() + ".png")
            : new ResourceLocation(location.getResourceDomain(), this.basePath + "/mipmaps/" + location.getResourcePath() + "." + mipmapLevel + ".png");
    }

    public TextureAtlasSprite getAtlasSprite(String iconName)
    {
        TextureAtlasSprite atlasSprite = this.mapUploadedSprites.get(iconName);

        if (atlasSprite == null)
        {
            atlasSprite = this.missingImage;
        }

        return atlasSprite;
    }

    public void updateAnimations()
    {
        boolean hasNormalAnimation = false;
        boolean hasSpecularAnimation = false;
        TextureUtil.bindTexture(this.getGlTextureId());
        int activeAnimationCount = 0;

        for (TextureAtlasSprite animatedSprite : this.listAnimatedSprites)
        {
            if (this.isTerrainAnimationActive(animatedSprite))
            {
                animatedSprite.updateAnimation();

                if (animatedSprite.isAnimationActive())
                {
                    ++activeAnimationCount;
                }

                if (animatedSprite.spriteNormal != null)
                {
                    hasNormalAnimation = true;
                }

                if (animatedSprite.spriteSpecular != null)
                {
                    hasSpecularAnimation = true;
                }
            }
        }

        if (Config.isMultiTexture())
        {
            for (TextureAtlasSprite animatedSprite : this.listAnimatedSprites)
            {
                if (this.isTerrainAnimationActive(animatedSprite))
                {
                    TextureAtlasSprite singleSprite = animatedSprite.spriteSingle;

                    if (singleSprite != null)
                    {
                        if (animatedSprite == TextureUtils.iconClock || animatedSprite == TextureUtils.iconCompass)
                        {
                            singleSprite.frameCounter = animatedSprite.frameCounter;
                        }

                        animatedSprite.bindSpriteTexture();
                        singleSprite.updateAnimation();

                        if (singleSprite.isAnimationActive())
                        {
                            ++activeAnimationCount;
                        }
                    }
                }
            }

            TextureUtil.bindTexture(this.getGlTextureId());
        }

        if (Config.isShaders())
        {
            if (hasNormalAnimation)
            {
                TextureUtil.bindTexture(this.getMultiTexID().norm);

                for (TextureAtlasSprite normalSpriteOwner : this.listAnimatedSprites)
                {
                    if (normalSpriteOwner.spriteNormal != null && this.isTerrainAnimationActive(normalSpriteOwner))
                    {
                        if (normalSpriteOwner == TextureUtils.iconClock || normalSpriteOwner == TextureUtils.iconCompass)
                        {
                            normalSpriteOwner.spriteNormal.frameCounter = normalSpriteOwner.frameCounter;
                        }

                        normalSpriteOwner.spriteNormal.updateAnimation();

                        if (normalSpriteOwner.spriteNormal.isAnimationActive())
                        {
                            ++activeAnimationCount;
                        }
                    }
                }
            }

            if (hasSpecularAnimation)
            {
                TextureUtil.bindTexture(this.getMultiTexID().spec);

                for (TextureAtlasSprite specularSpriteOwner : this.listAnimatedSprites)
                {
                    if (specularSpriteOwner.spriteSpecular != null && this.isTerrainAnimationActive(specularSpriteOwner))
                    {
                        if (specularSpriteOwner == TextureUtils.iconClock || specularSpriteOwner == TextureUtils.iconCompass)
                        {
                            specularSpriteOwner.spriteNormal.frameCounter = specularSpriteOwner.frameCounter;
                        }

                        specularSpriteOwner.spriteSpecular.updateAnimation();

                        if (specularSpriteOwner.spriteSpecular.isAnimationActive())
                        {
                            ++activeAnimationCount;
                        }
                    }
                }
            }

            if (hasNormalAnimation || hasSpecularAnimation)
            {
                TextureUtil.bindTexture(this.getGlTextureId());
            }
        }

        int currentFrameCount = Config.getMinecraft().entityRenderer.frameCount;

        if (currentFrameCount != this.frameCountAnimations)
        {
            this.countAnimationsActive = activeAnimationCount;
            this.frameCountAnimations = currentFrameCount;
        }

        if (SmartAnimations.isActive())
        {
            SmartAnimations.resetSpritesRendered();
        }
    }

    public TextureAtlasSprite registerSprite(ResourceLocation location)
    {
        if (location == null)
        {
            throw new IllegalArgumentException("Location cannot be null!");
        }
        else
        {
            TextureAtlasSprite textureSprite = this.mapRegisteredSprites.get(location.toString());

            if (textureSprite == null)
            {
                textureSprite = TextureAtlasSprite.makeAtlasSprite(location);
                this.mapRegisteredSprites.put(location.toString(), textureSprite);
                textureSprite.updateIndexInMap(this.counterIndexInMap);

                if (Config.isEmissiveTextures())
                {
                    this.checkEmissive(location, textureSprite);
                }
            }

            return textureSprite;
        }
    }

    public void tick()
    {
        this.updateAnimations();
    }

    public void setMipmapLevels(int mipmapLevelsIn)
    {
        this.mipmapLevels = mipmapLevelsIn;
    }

    public TextureAtlasSprite getMissingSprite()
    {
        return this.missingImage;
    }

    public TextureAtlasSprite getTextureExtry(String textureName)
    {
        return this.mapRegisteredSprites.get(textureName);
    }

    public boolean setTextureEntry(String textureName, TextureAtlasSprite textureSprite)
    {
        if (!this.mapRegisteredSprites.containsKey(textureName))
        {
            this.mapRegisteredSprites.put(textureName, textureSprite);
            textureSprite.updateIndexInMap(this.counterIndexInMap);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean setTextureEntry(TextureAtlasSprite textureSprite)
    {
        return this.setTextureEntry(textureSprite.getIconName(), textureSprite);
    }

    public String getBasePath()
    {
        return this.basePath;
    }

    public int getMipmapLevels()
    {
        return this.mipmapLevels;
    }

    private boolean isAbsoluteLocation(ResourceLocation location)
    {
        String resourcePath = location.getResourcePath();
        return this.isAbsoluteLocationPath(resourcePath);
    }

    private boolean isAbsoluteLocationPath(String path)
    {
        String lowerPath = path.toLowerCase(Locale.ROOT);
        return lowerPath.startsWith("mcpatcher/") || lowerPath.startsWith("optifine/");
    }

    public TextureAtlasSprite getSpriteSafe(String spriteName)
    {
        ResourceLocation spriteLocation = new ResourceLocation(spriteName);
        return this.mapRegisteredSprites.get(spriteLocation.toString());
    }

    public TextureAtlasSprite getRegisteredSprite(ResourceLocation location)
    {
        return this.mapRegisteredSprites.get(location.toString());
    }

    private boolean isTerrainAnimationActive(TextureAtlasSprite textureSprite)
    {
        return textureSprite != TextureUtils.iconWaterStill && textureSprite != TextureUtils.iconWaterFlow ? (textureSprite != TextureUtils.iconLavaStill && textureSprite != TextureUtils.iconLavaFlow ? (textureSprite != TextureUtils.iconFireLayer0 && textureSprite != TextureUtils.iconFireLayer1 ? (textureSprite == TextureUtils.iconPortal ? Config.isAnimatedPortal() : (textureSprite != TextureUtils.iconClock && textureSprite != TextureUtils.iconCompass ? Config.isAnimatedTerrain() : true)) : Config.isAnimatedFire()) : Config.isAnimatedLava()) : Config.isAnimatedWater();
    }

    public int getCountRegisteredSprites()
    {
        return this.counterIndexInMap.getValue();
    }

    private int detectMaxMipmapLevel(Map<String, TextureAtlasSprite> sprites, IResourceManager resourceManager)
    {
        int detectedSpriteSize = this.detectMinimumSpriteSize(sprites, resourceManager, 20);

        if (detectedSpriteSize < 16)
        {
            detectedSpriteSize = 16;
        }

        detectedSpriteSize = MathHelper.roundUpToPowerOfTwo(detectedSpriteSize);

        if (detectedSpriteSize > 16)
        {
            Config.log("Sprite size: " + detectedSpriteSize);
        }

        int mipmapLevel = MathHelper.calculateLogBaseTwo(detectedSpriteSize);

        if (mipmapLevel < 4)
        {
            mipmapLevel = 4;
        }

        return mipmapLevel;
    }

    private int detectMinimumSpriteSize(Map<String, TextureAtlasSprite> sprites, IResourceManager resourceManager, int minimumSizePercent)
    {
        Map<Integer, Integer> sizeCounts = new HashMap<Integer, Integer>();

        for (Entry<String, TextureAtlasSprite> spriteEntry : sprites.entrySet())
        {
            TextureAtlasSprite textureSprite = spriteEntry.getValue();
            ResourceLocation spriteLocation = new ResourceLocation(textureSprite.getIconName());
            ResourceLocation spriteResourceLocation = this.completeResourceLocation(spriteLocation);

            if (!textureSprite.hasCustomLoader(resourceManager, spriteLocation))
            {
                try
                {
                    IResource spriteResource = resourceManager.getResource(spriteResourceLocation);

                    if (spriteResource != null)
                    {
                        InputStream inputStream = spriteResource.getInputStream();

                        if (inputStream != null)
                        {
                            Dimension dimension = TextureUtils.getImageSize(inputStream, "png");
                            inputStream.close();

                            if (dimension != null)
                            {
                                int imageWidth = dimension.width;
                                int roundedWidth = MathHelper.roundUpToPowerOfTwo(imageWidth);

                                if (!sizeCounts.containsKey(Integer.valueOf(roundedWidth)))
                                {
                                    sizeCounts.put(Integer.valueOf(roundedWidth), Integer.valueOf(1));
                                }
                                else
                                {
                                    int countForWidth = sizeCounts.get(Integer.valueOf(roundedWidth)).intValue();
                                    sizeCounts.put(Integer.valueOf(roundedWidth), Integer.valueOf(countForWidth + 1));
                                }
                            }
                        }
                    }
                }
                catch (Exception caughtException)
                {
                    ;
                }
            }
        }

        int totalSprites = 0;
        Set<Integer> spriteSizes = sizeCounts.keySet();
        Set<Integer> sortedSpriteSizes = new TreeSet<Integer>(spriteSizes);
        int sizeCount;

        for (Iterator<Integer> sizeIterator = sortedSpriteSizes.iterator(); sizeIterator.hasNext(); totalSprites += sizeCount)
        {
            int spriteSize = sizeIterator.next().intValue();
            sizeCount = sizeCounts.get(Integer.valueOf(spriteSize)).intValue();
        }

        int selectedSize = 16;
        int cumulativeCount = 0;
        int thresholdCount = totalSprites * minimumSizePercent / 100;
        Iterator<Integer> sortedSizeIterator = sortedSpriteSizes.iterator();

        while (sortedSizeIterator.hasNext())
        {
            int spriteSize = sortedSizeIterator.next().intValue();
            int spriteCount = sizeCounts.get(Integer.valueOf(spriteSize)).intValue();
            cumulativeCount += spriteCount;

            if (spriteSize > selectedSize)
            {
                selectedSize = spriteSize;
            }

            if (cumulativeCount > thresholdCount)
            {
                return selectedSize;
            }
        }

        return selectedSize;
    }

    private int getMinSpriteSize()
    {
        int minSize = 1 << this.mipmapLevels;

        if (minSize < 8)
        {
            minSize = 8;
        }

        return minSize;
    }

    private int[] getMissingImageData(int size)
    {
        BufferedImage bufferedImage = new BufferedImage(16, 16, 2);
        bufferedImage.setRGB(0, 0, 16, 16, TextureUtil.missingTextureData, 0, 16);
        BufferedImage scaledImage = TextureUtils.scaleImage(bufferedImage, size);
        int[] pixels = new int[size * size];
        scaledImage.getRGB(0, 0, size, size, pixels, 0, size);
        return pixels;
    }

    public boolean isTextureBound()
    {
        int boundTextureId = GlStateManager.getBoundTexture();
        int textureId = this.getGlTextureId();
        return boundTextureId == textureId;
    }

    private void updateIconGrid(int atlasWidth, int atlasHeight)
    {
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGrid = null;

        if (this.iconGridSize > 0)
        {
            this.iconGridCountX = atlasWidth / this.iconGridSize;
            this.iconGridCountY = atlasHeight / this.iconGridSize;
            this.iconGrid = new TextureAtlasSprite[this.iconGridCountX * this.iconGridCountY];
            this.iconGridSizeU = 1.0D / (double)this.iconGridCountX;
            this.iconGridSizeV = 1.0D / (double)this.iconGridCountY;

            for (TextureAtlasSprite uploadedSprite : this.mapUploadedSprites.values())
            {
                double halfTexelU = 0.5D / (double)atlasWidth;
                double halfTexelV = 0.5D / (double)atlasHeight;
                double minU = (double)Math.min(uploadedSprite.getMinU(), uploadedSprite.getMaxU()) + halfTexelU;
                double minV = (double)Math.min(uploadedSprite.getMinV(), uploadedSprite.getMaxV()) + halfTexelV;
                double maxU = (double)Math.max(uploadedSprite.getMinU(), uploadedSprite.getMaxU()) - halfTexelU;
                double maxV = (double)Math.max(uploadedSprite.getMinV(), uploadedSprite.getMaxV()) - halfTexelV;
                int minGridX = (int)(minU / this.iconGridSizeU);
                int minGridY = (int)(minV / this.iconGridSizeV);
                int maxGridX = (int)(maxU / this.iconGridSizeU);
                int maxGridY = (int)(maxV / this.iconGridSizeV);

                for (int gridX = minGridX; gridX <= maxGridX; ++gridX)
                {
                    if (gridX >= 0 && gridX < this.iconGridCountX)
                    {
                        for (int gridY = minGridY; gridY <= maxGridY; ++gridY)
                        {
                            if (gridY >= 0 && gridY < this.iconGridCountX)
                            {
                                int gridIndex = gridY * this.iconGridCountX + gridX;
                                this.iconGrid[gridIndex] = uploadedSprite;
                            }
                            else
                            {
                                Config.warn("Invalid grid V: " + gridY + ", icon: " + uploadedSprite.getIconName());
                            }
                        }
                    }
                    else
                    {
                        Config.warn("Invalid grid U: " + gridX + ", icon: " + uploadedSprite.getIconName());
                    }
                }
            }
        }
    }

    public TextureAtlasSprite getIconByUV(double u, double v)
    {
        if (this.iconGrid == null)
        {
            return null;
        }
        else
        {
            int gridX = (int)(u / this.iconGridSizeU);
            int gridY = (int)(v / this.iconGridSizeV);
            int gridIndex = gridY * this.iconGridCountX + gridX;
            return gridIndex >= 0 && gridIndex <= this.iconGrid.length ? this.iconGrid[gridIndex] : null;
        }
    }

    private void checkEmissive(ResourceLocation location, TextureAtlasSprite textureSprite)
    {
        String emissiveSuffix = EmissiveTextures.getSuffixEmissive();

        if (emissiveSuffix != null)
        {
            if (!location.getResourcePath().endsWith(emissiveSuffix))
            {
                ResourceLocation emissiveLocation = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + emissiveSuffix);
                ResourceLocation emissiveTextureLocation = this.completeResourceLocation(emissiveLocation);

                if (Config.hasResource(emissiveTextureLocation))
                {
                    TextureAtlasSprite emissiveSprite = this.registerSprite(emissiveLocation);
                    emissiveSprite.isEmissive = true;
                    textureSprite.spriteEmissive = emissiveSprite;
                }
            }
        }
    }

    public int getCountAnimations()
    {
        return this.listAnimatedSprites.size();
    }

    public int getCountAnimationsActive()
    {
        return this.countAnimationsActive;
    }
}
