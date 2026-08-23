package net.optifine.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerMooshroomMushroom;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.BetterGrass;
import net.optifine.BetterSnow;
import net.optifine.CustomBlockLayers;
import net.optifine.CustomColors;
import net.optifine.CustomGuis;
import net.optifine.CustomItems;
import net.optifine.CustomLoadingScreens;
import net.optifine.CustomPanorama;
import net.optifine.CustomSky;
import net.optifine.Lang;
import net.optifine.NaturalTextures;
import net.optifine.RandomEntities;
import net.optifine.SmartLeaves;
import net.optifine.TextureAnimations;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.shaders.MultiTexID;
import net.optifine.shaders.Shaders;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL;

public class TextureUtils
{
    public static final String texGrassTop = "grass_top";
    public static final String texStone = "stone";
    public static final String texDirt = "dirt";
    public static final String texCoarseDirt = "coarse_dirt";
    public static final String texGrassSide = "grass_side";
    public static final String texStoneslabSide = "stone_slab_side";
    public static final String texStoneslabTop = "stone_slab_top";
    public static final String texBedrock = "bedrock";
    public static final String texSand = "sand";
    public static final String texGravel = "gravel";
    public static final String texLogOak = "log_oak";
    public static final String texLogBigOak = "log_big_oak";
    public static final String texLogAcacia = "log_acacia";
    public static final String texLogSpruce = "log_spruce";
    public static final String texLogBirch = "log_birch";
    public static final String texLogJungle = "log_jungle";
    public static final String texLogOakTop = "log_oak_top";
    public static final String texLogBigOakTop = "log_big_oak_top";
    public static final String texLogAcaciaTop = "log_acacia_top";
    public static final String texLogSpruceTop = "log_spruce_top";
    public static final String texLogBirchTop = "log_birch_top";
    public static final String texLogJungleTop = "log_jungle_top";
    public static final String texLeavesOak = "leaves_oak";
    public static final String texLeavesBigOak = "leaves_big_oak";
    public static final String texLeavesAcacia = "leaves_acacia";
    public static final String texLeavesBirch = "leaves_birch";
    public static final String texLeavesSpuce = "leaves_spruce";
    public static final String texLeavesJungle = "leaves_jungle";
    public static final String texGoldOre = "gold_ore";
    public static final String texIronOre = "iron_ore";
    public static final String texCoalOre = "coal_ore";
    public static final String texObsidian = "obsidian";
    public static final String texGrassSideOverlay = "grass_side_overlay";
    public static final String texSnow = "snow";
    public static final String texGrassSideSnowed = "grass_side_snowed";
    public static final String texMyceliumSide = "mycelium_side";
    public static final String texMyceliumTop = "mycelium_top";
    public static final String texDiamondOre = "diamond_ore";
    public static final String texRedstoneOre = "redstone_ore";
    public static final String texLapisOre = "lapis_ore";
    public static final String texCactusSide = "cactus_side";
    public static final String texClay = "clay";
    public static final String texFarmlandWet = "farmland_wet";
    public static final String texFarmlandDry = "farmland_dry";
    public static final String texNetherrack = "netherrack";
    public static final String texSoulSand = "soul_sand";
    public static final String texGlowstone = "glowstone";
    public static final String texLeavesSpruce = "leaves_spruce";
    public static final String texLeavesSpruceOpaque = "leaves_spruce_opaque";
    public static final String texEndStone = "end_stone";
    public static final String texSandstoneTop = "sandstone_top";
    public static final String texSandstoneBottom = "sandstone_bottom";
    public static final String texRedstoneLampOff = "redstone_lamp_off";
    public static final String texRedstoneLampOn = "redstone_lamp_on";
    public static final String texWaterStill = "water_still";
    public static final String texWaterFlow = "water_flow";
    public static final String texLavaStill = "lava_still";
    public static final String texLavaFlow = "lava_flow";
    public static final String texFireLayer0 = "fire_layer_0";
    public static final String texFireLayer1 = "fire_layer_1";
    public static final String texPortal = "portal";
    public static final String texGlass = "glass";
    public static final String texGlassPaneTop = "glass_pane_top";
    public static final String texCompass = "compass";
    public static final String texClock = "clock";
    public static TextureAtlasSprite iconGrassTop;
    public static TextureAtlasSprite iconGrassSide;
    public static TextureAtlasSprite iconGrassSideOverlay;
    public static TextureAtlasSprite iconSnow;
    public static TextureAtlasSprite iconGrassSideSnowed;
    public static TextureAtlasSprite iconMyceliumSide;
    public static TextureAtlasSprite iconMyceliumTop;
    public static TextureAtlasSprite iconWaterStill;
    public static TextureAtlasSprite iconWaterFlow;
    public static TextureAtlasSprite iconLavaStill;
    public static TextureAtlasSprite iconLavaFlow;
    public static TextureAtlasSprite iconPortal;
    public static TextureAtlasSprite iconFireLayer0;
    public static TextureAtlasSprite iconFireLayer1;
    public static TextureAtlasSprite iconGlass;
    public static TextureAtlasSprite iconGlassPaneTop;
    public static TextureAtlasSprite iconCompass;
    public static TextureAtlasSprite iconClock;
    public static final String SPRITE_PREFIX_BLOCKS = "minecraft:blocks/";
    public static final String SPRITE_PREFIX_ITEMS = "minecraft:items/";
    private static IntBuffer staticBuffer = GLAllocation.createDirectIntBuffer(256);

    public static void update()
    {
        TextureMap textureMap = getTextureMapBlocks();

        if (textureMap != null)
        {
            String blockSpritePrefix = "minecraft:blocks/";
            iconGrassTop = textureMap.getSpriteSafe(blockSpritePrefix + "grass_top");
            iconGrassSide = textureMap.getSpriteSafe(blockSpritePrefix + "grass_side");
            iconGrassSideOverlay = textureMap.getSpriteSafe(blockSpritePrefix + "grass_side_overlay");
            iconSnow = textureMap.getSpriteSafe(blockSpritePrefix + "snow");
            iconGrassSideSnowed = textureMap.getSpriteSafe(blockSpritePrefix + "grass_side_snowed");
            iconMyceliumSide = textureMap.getSpriteSafe(blockSpritePrefix + "mycelium_side");
            iconMyceliumTop = textureMap.getSpriteSafe(blockSpritePrefix + "mycelium_top");
            iconWaterStill = textureMap.getSpriteSafe(blockSpritePrefix + "water_still");
            iconWaterFlow = textureMap.getSpriteSafe(blockSpritePrefix + "water_flow");
            iconLavaStill = textureMap.getSpriteSafe(blockSpritePrefix + "lava_still");
            iconLavaFlow = textureMap.getSpriteSafe(blockSpritePrefix + "lava_flow");
            iconFireLayer0 = textureMap.getSpriteSafe(blockSpritePrefix + "fire_layer_0");
            iconFireLayer1 = textureMap.getSpriteSafe(blockSpritePrefix + "fire_layer_1");
            iconPortal = textureMap.getSpriteSafe(blockSpritePrefix + "portal");
            iconGlass = textureMap.getSpriteSafe(blockSpritePrefix + "glass");
            iconGlassPaneTop = textureMap.getSpriteSafe(blockSpritePrefix + "glass_pane_top");
            String itemSpritePrefix = "minecraft:items/";
            iconCompass = textureMap.getSpriteSafe(itemSpritePrefix + "compass");
            iconClock = textureMap.getSpriteSafe(itemSpritePrefix + "clock");
        }
    }

    public static BufferedImage fixTextureDimensions(String name, BufferedImage bi)
    {
        if (name.startsWith("/mob/zombie") || name.startsWith("/mob/pigzombie"))
        {
            int i = bi.getWidth();
            int j = bi.getHeight();

            if (i == j * 2)
            {
                BufferedImage bufferedImage = new BufferedImage(i, j * 2, 2);
                Graphics2D graphics2d = bufferedImage.createGraphics();
                graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics2d.drawImage(bi, 0, 0, i, j, (ImageObserver)null);
                return bufferedImage;
            }
        }

        return bi;
    }

    public static int ceilPowerOfTwo(int val)
    {
        int i;

        for (i = 1; i < val; i *= 2)
        {
            ;
        }

        return i;
    }

    public static int getPowerOfTwo(int val)
    {
        int i = 1;
        int j;

        for (j = 0; i < val; ++j)
        {
            i *= 2;
        }

        return j;
    }

    public static int twoToPower(int power)
    {
        int i = 1;

        for (int j = 0; j < power; ++j)
        {
            i *= 2;
        }

        return i;
    }

    public static ITextureObject getTexture(ResourceLocation loc)
    {
        ITextureObject itextureobject = Config.getTextureManager().getTexture(loc);

        if (itextureobject != null)
        {
            return itextureobject;
        }
        else if (!Config.hasResource(loc))
        {
            return null;
        }
        else
        {
            SimpleTexture simpleTexture = new SimpleTexture(loc);
            Config.getTextureManager().loadTexture(loc, simpleTexture);
            return simpleTexture;
        }
    }

    public static void resourcesReloaded(IResourceManager rm)
    {
        if (getTextureMapBlocks() != null)
        {
            Config.dbg("*** Reloading custom textures ***");
            CustomSky.reset();
            TextureAnimations.reset();
            update();
            NaturalTextures.update();
            BetterGrass.update();
            BetterSnow.update();
            TextureAnimations.update();
            CustomColors.update();
            CustomSky.update();
            RandomEntities.update();
            CustomItems.updateModels();
            CustomEntityModels.update();
            Shaders.resourcesReloaded();
            Lang.resourcesReloaded();
            Config.updateTexturePackClouds();
            SmartLeaves.updateLeavesModels();
            CustomPanorama.update();
            CustomGuis.update();
            LayerMooshroomMushroom.update();
            CustomLoadingScreens.update();
            CustomBlockLayers.update();
            Config.getTextureManager().tick();
        }
    }

    public static TextureMap getTextureMapBlocks()
    {
        return Minecraft.getMinecraft().getTextureMapBlocks();
    }

    public static void registerResourceListener()
    {
        IResourceManager iresourcemanager = Config.getResourceManager();

        if (iresourcemanager instanceof IReloadableResourceManager)
        {
            IReloadableResourceManager ireloadableresourcemanager = (IReloadableResourceManager)iresourcemanager;
            IResourceManagerReloadListener iresourcemanagerreloadlistener = new IResourceManagerReloadListener()
            {
                public void onResourceManagerReload(IResourceManager resourceManager)
                {
                    TextureUtils.resourcesReloaded(resourceManager);
                }
            };
            ireloadableresourcemanager.registerReloadListener(iresourcemanagerreloadlistener);
        }

        ITickableTextureObject itickabletextureobject = new ITickableTextureObject()
        {
            public void tick()
            {
                TextureAnimations.updateAnimations();
            }
            public void loadTexture(IResourceManager resourceManager) throws IOException
            {
            }
            public int getGlTextureId()
            {
                return 0;
            }
            public void setBlurMipmap(boolean blur, boolean mipmap)
            {
            }
            public void restoreLastBlurMipmap()
            {
            }
            public MultiTexID getMultiTexID()
            {
                return null;
            }
        };
        ResourceLocation resourceLocation = new ResourceLocation("optifine/TickableTextures");
        Config.getTextureManager().loadTickableTexture(resourceLocation, itickabletextureobject);
    }

    public static ResourceLocation fixResourceLocation(ResourceLocation loc, String basePath)
    {
        if (!loc.getResourceDomain().equals("minecraft"))
        {
            return loc;
        }
        else
        {
            String resourcePath = loc.getResourcePath();
            String fixedResourcePath = fixResourcePath(resourcePath, basePath);

            if (fixedResourcePath != resourcePath)
            {
                loc = new ResourceLocation(loc.getResourceDomain(), fixedResourcePath);
            }

            return loc;
        }
    }

    public static String fixResourcePath(String path, String basePath)
    {
        String minecraftAssetsPrefix = "assets/minecraft/";

        if (path.startsWith(minecraftAssetsPrefix))
        {
            path = path.substring(minecraftAssetsPrefix.length());
            return path;
        }
        else if (path.startsWith("./"))
        {
            path = path.substring(2);

            if (!basePath.endsWith("/"))
            {
                basePath = basePath + "/";
            }

            path = basePath + path;
            return path;
        }
        else
        {
            if (path.startsWith("/~"))
            {
                path = path.substring(1);
            }

            String mcpatcherPrefix = "mcpatcher/";

            if (path.startsWith("~/"))
            {
                path = path.substring(2);
                path = mcpatcherPrefix + path;
                return path;
            }
            else if (path.startsWith("/"))
            {
                path = mcpatcherPrefix + path.substring(1);
                return path;
            }
            else
            {
                return path;
            }
        }
    }

    public static String getBasePath(String path)
    {
        int i = path.lastIndexOf(47);
        return i < 0 ? "" : path.substring(0, i);
    }

    public static void applyAnisotropicLevel()
    {
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic)
        {
            float maxAnisotropicLevel = GL11.glGetFloat(34047);
            float anisotropicLevel = (float)Config.getAnisotropicFilterLevel();
            anisotropicLevel = Math.min(anisotropicLevel, maxAnisotropicLevel);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 34046, anisotropicLevel);
        }
    }

    public static void bindTexture(int glTexId)
    {
        GlStateManager.bindTexture(glTexId);
    }

    public static boolean isPowerOfTwo(int x)
    {
        int i = MathHelper.roundUpToPowerOfTwo(x);
        return i == x;
    }

    public static BufferedImage scaleImage(BufferedImage bi, int intValue)
    {
        int i = bi.getWidth();
        int j = bi.getHeight();
        int k = j * intValue / i;
        BufferedImage bufferedImage = new BufferedImage(intValue, k, 2);
        Graphics2D graphics2d = bufferedImage.createGraphics();
        Object object = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

        if (intValue < i || intValue % i != 0)
        {
            object = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        }

        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, object);
        graphics2d.drawImage(bi, 0, 0, intValue, k, (ImageObserver)null);
        return bufferedImage;
    }

    public static int scaleToGrid(int size, int sizeGrid)
    {
        if (size == sizeGrid)
        {
            return size;
        }
        else
        {
            int i;

            for (i = size / sizeGrid * sizeGrid; i < size; i += sizeGrid)
            {
                ;
            }

            return i;
        }
    }

    public static int scaleToMin(int size, int sizeMin)
    {
        if (size >= sizeMin)
        {
            return size;
        }
        else
        {
            int i;

            for (i = sizeMin / size * size; i < sizeMin; i += size)
            {
                ;
            }

            return i;
        }
    }

    public static Dimension getImageSize(InputStream in, String suffix)
    {
        Iterator iterator = ImageIO.getImageReadersBySuffix(suffix);

        while (true)
        {
            if (iterator.hasNext())
            {
                ImageReader imageReader = (ImageReader)iterator.next();
                Dimension dimension;

                try
                {
                    ImageInputStream imageInputStream = ImageIO.createImageInputStream(in);
                    imageReader.setInput(imageInputStream);
                    int i = imageReader.getWidth(imageReader.getMinIndex());
                    int j = imageReader.getHeight(imageReader.getMinIndex());
                    dimension = new Dimension(i, j);
                }
                catch (IOException exception)
                {
                    continue;
                }
                finally
                {
                    imageReader.dispose();
                }

                return dimension;
            }

            return null;
        }
    }

    public static void dbgMipmaps(TextureAtlasSprite textureatlassprite)
    {
        int[][] aint = textureatlassprite.getFrameTextureData(0);

        for (int i = 0; i < aint.length; ++i)
        {
            int[] aint1 = aint[i];

            if (aint1 == null)
            {
                Config.dbg("" + i + ": " + aint1);
            }
            else
            {
                Config.dbg("" + i + ": " + aint1.length);
            }
        }
    }

    public static void saveGlTexture(String name, int textureId, int mipmapLevels, int width, int height)
    {
        bindTexture(textureId);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        File outputFile = new File(name);
        File outputDir = outputFile.getParentFile();

        if (outputDir != null)
        {
            outputDir.mkdirs();
        }

        for (int oldMipmapLevel = 0; oldMipmapLevel < 16; ++oldMipmapLevel)
        {
            File oldMipmapFile = new File(name + "_" + oldMipmapLevel + ".png");
            oldMipmapFile.delete();
        }

        for (int mipmapLevel = 0; mipmapLevel <= mipmapLevels; ++mipmapLevel)
        {
            File mipmapFile = new File(name + "_" + mipmapLevel + ".png");
            int mipmapWidth = width >> mipmapLevel;
            int mipmapHeight = height >> mipmapLevel;
            int pixelCount = mipmapWidth * mipmapHeight;
            IntBuffer pixelBuffer = BufferUtils.createIntBuffer(pixelCount);
            int[] pixels = new int[pixelCount];
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, mipmapLevel, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)pixelBuffer);
            pixelBuffer.get(pixels);
            BufferedImage image = new BufferedImage(mipmapWidth, mipmapHeight, 2);
            image.setRGB(0, 0, mipmapWidth, mipmapHeight, pixels, 0, mipmapWidth);

            try
            {
                ImageIO.write(image, "png", (File)mipmapFile);
                Config.dbg("Exported: " + mipmapFile);
            }
            catch (Exception exception)
            {
                Config.warn("Error writing: " + mipmapFile);
                Config.warn("" + exception.getClass().getName() + ": " + exception.getMessage());
            }
        }
    }

    public static void generateCustomMipmaps(TextureAtlasSprite tas, int mipmaps)
    {
        int iconWidth = tas.getIconWidth();
        int iconHeight = tas.getIconHeight();

        if (tas.getFrameCount() < 1)
        {
            List<int[][]> placeholderFrames = new ArrayList();
            int[][] placeholderMipmaps = new int[mipmaps + 1][];
            int[] placeholderFrameData = new int[iconWidth * iconHeight];
            placeholderMipmaps[0] = placeholderFrameData;
            placeholderFrames.add(placeholderMipmaps);
            tas.setFramesTextureData(placeholderFrames);
        }

        List<int[][]> framesTextureData = new ArrayList();
        int frameCount = tas.getFrameCount();

        for (int frameIndex = 0; frameIndex < frameCount; ++frameIndex)
        {
            int[] frameData = getFrameData(tas, frameIndex, 0);

            if (frameData == null || frameData.length < 1)
            {
                frameData = new int[iconWidth * iconHeight];
            }

            if (frameData.length != iconWidth * iconHeight)
            {
                int frameSize = (int)Math.round(Math.sqrt((double)frameData.length));

                if (frameSize * frameSize != frameData.length)
                {
                    frameData = new int[1];
                    frameSize = 1;
                }

                BufferedImage frameImage = new BufferedImage(frameSize, frameSize, 2);
                frameImage.setRGB(0, 0, frameSize, frameSize, frameData, 0, frameSize);
                BufferedImage scaledFrameImage = scaleImage(frameImage, iconWidth);
                int[] scaledFrameData = new int[iconWidth * iconHeight];
                scaledFrameImage.getRGB(0, 0, iconWidth, iconHeight, scaledFrameData, 0, iconWidth);
                frameData = scaledFrameData;
            }

            int[][] mipmapData = new int[mipmaps + 1][];
            mipmapData[0] = frameData;
            framesTextureData.add(mipmapData);
        }

        tas.setFramesTextureData(framesTextureData);
        tas.generateMipmaps(mipmaps);
    }

    public static int[] getFrameData(TextureAtlasSprite tas, int frame, int level)
    {
        List<int[][]> list = tas.getFramesTextureData();

        if (list.size() <= frame)
        {
            return null;
        }
        else
        {
            int[][] aint = (int[][])list.get(frame);

            if (aint != null && aint.length > level)
            {
                int[] aint1 = aint[level];
                return aint1;
            }
            else
            {
                return null;
            }
        }
    }

    public static int getGLMaximumTextureSize()
    {
        for (int i = 65536; i > 0; i >>= 1)
        {
            GlStateManager.glTexImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, (IntBuffer)null);
            int j = GL11.glGetError();
            int k = GlStateManager.glGetTexLevelParameteri(32868, 0, 4096);

            if (k != 0)
            {
                return i;
            }
        }

        return -1;
    }
}
