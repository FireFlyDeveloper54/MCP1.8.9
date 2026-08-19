package net.optifine;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.TextureUtils;

public class TextureAnimations
{
    private static TextureAnimation[] textureAnimations = null;
    private static int countAnimationsActive = 0;
    private static int frameCountAnimations = 0;

    public static void reset()
    {
        textureAnimations = null;
    }

    public static void update()
    {
        textureAnimations = null;
        countAnimationsActive = 0;
        IResourcePack[] resourcePacks = Config.getResourcePacks();
        textureAnimations = getTextureAnimations(resourcePacks);
        updateAnimations();
    }

    public static void updateAnimations()
    {
        if (textureAnimations != null && Config.isAnimatedTextures())
        {
            int activeCount = 0;

            for (int animationIndex = 0; animationIndex < textureAnimations.length; ++animationIndex)
            {
                TextureAnimation textureAnimation = textureAnimations[animationIndex];
                textureAnimation.updateTexture();

                if (textureAnimation.isActive())
                {
                    ++activeCount;
                }
            }

            int frameCount = Config.getMinecraft().entityRenderer.frameCount;

            if (frameCount != frameCountAnimations)
            {
                countAnimationsActive = activeCount;
                frameCountAnimations = frameCount;
            }

            if (SmartAnimations.isActive())
            {
                SmartAnimations.resetTexturesRendered();
            }
        }
        else
        {
            countAnimationsActive = 0;
        }
    }

    private static TextureAnimation[] getTextureAnimations(IResourcePack[] rps)
    {
        List animations = new ArrayList();

        for (int packIndex = 0; packIndex < rps.length; ++packIndex)
        {
            IResourcePack resourcePack = rps[packIndex];
            TextureAnimation[] packAnimations = getTextureAnimations(resourcePack);

            if (packAnimations != null)
            {
                animations.addAll(Arrays.asList(packAnimations));
            }
        }

        TextureAnimation[] allAnimations = (TextureAnimation[])((TextureAnimation[])animations.toArray(new TextureAnimation[animations.size()]));
        return allAnimations;
    }

    private static TextureAnimation[] getTextureAnimations(IResourcePack rp)
    {
        String[] propertyPaths = ResUtils.collectFiles(rp, (String)"mcpatcher/anim/", (String)".properties", (String[])null);

        if (propertyPaths.length <= 0)
        {
            return null;
        }
        else
        {
            List animations = new ArrayList();

            for (int pathIndex = 0; pathIndex < propertyPaths.length; ++pathIndex)
            {
                String path = propertyPaths[pathIndex];
                Config.dbg("Texture animation: " + path);

                try
                {
                    ResourceLocation propertyLocation = new ResourceLocation(path);
                    InputStream inputStream = rp.getInputStream(propertyLocation);
                    Properties properties = new PropertiesOrdered();
                    properties.load(inputStream);
                    inputStream.close();
                    TextureAnimation textureAnimation = makeTextureAnimation(properties, propertyLocation);

                    if (textureAnimation != null)
                    {
                        ResourceLocation targetLocation = new ResourceLocation(textureAnimation.getDstTex());

                        if (Config.getDefiningResourcePack(targetLocation) != rp)
                        {
                            Config.dbg("Skipped: " + path + ", target texture not loaded from same resource pack");
                        }
                        else
                        {
                            animations.add(textureAnimation);
                        }
                    }
                }
                catch (FileNotFoundException fileNotFoundException)
                {
                    Config.warn("File not found: " + fileNotFoundException.getMessage());
                }
                catch (IOException ioException)
                {
                    net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
                }
            }

            TextureAnimation[] animationArray = (TextureAnimation[])((TextureAnimation[])animations.toArray(new TextureAnimation[animations.size()]));
            return animationArray;
        }
    }

    private static TextureAnimation makeTextureAnimation(Properties props, ResourceLocation propLoc)
    {
        String sourcePath = props.getProperty("from");
        String targetPath = props.getProperty("to");
        int dstX = Config.parseInt(props.getProperty("x"), -1);
        int dstY = Config.parseInt(props.getProperty("y"), -1);
        int tileWidth = Config.parseInt(props.getProperty("w"), -1);
        int tileHeight = Config.parseInt(props.getProperty("h"), -1);

        if (sourcePath != null && targetPath != null)
        {
            if (dstX >= 0 && dstY >= 0 && tileWidth >= 0 && tileHeight >= 0)
            {
                sourcePath = sourcePath.trim();
                targetPath = targetPath.trim();
                String basePath = TextureUtils.getBasePath(propLoc.getResourcePath());
                sourcePath = TextureUtils.fixResourcePath(sourcePath, basePath);
                targetPath = TextureUtils.fixResourcePath(targetPath, basePath);
                byte[] sourceData = getCustomTextureData(sourcePath, tileWidth);

                if (sourceData == null)
                {
                    Config.warn("TextureAnimation: Source texture not found: " + targetPath);
                    return null;
                }
                else
                {
                    int pixelCount = sourceData.length / 4;
                    int frameCount = pixelCount / (tileWidth * tileHeight);
                    int expectedPixelCount = frameCount * tileWidth * tileHeight;

                    if (pixelCount != expectedPixelCount)
                    {
                        Config.warn("TextureAnimation: Source texture has invalid number of frames: " + sourcePath + ", frames: " + (float)pixelCount / (float)(tileWidth * tileHeight));
                        return null;
                    }
                    else
                    {
                        ResourceLocation targetLocation = new ResourceLocation(targetPath);

                        try
                        {
                            InputStream inputStream = Config.getResourceStream(targetLocation);

                            if (inputStream == null)
                            {
                                Config.warn("TextureAnimation: Target texture not found: " + targetPath);
                                return null;
                            }
                            else
                            {
                                BufferedImage targetImage = readTextureImage(inputStream);

                                if (dstX + tileWidth <= targetImage.getWidth() && dstY + tileHeight <= targetImage.getHeight())
                                {
                                    TextureAnimation textureAnimation = new TextureAnimation(sourcePath, sourceData, targetPath, targetLocation, dstX, dstY, tileWidth, tileHeight, props);
                                    return textureAnimation;
                                }
                                else
                                {
                                    Config.warn("TextureAnimation: Animation coordinates are outside the target texture: " + targetPath);
                                    return null;
                                }
                            }
                        }
                        catch (IOException caughtIoException)
                        {
                            Config.warn("TextureAnimation: Target texture not found: " + targetPath);
                            return null;
                        }
                    }
                }
            }
            else
            {
                Config.warn("TextureAnimation: Invalid coordinates");
                return null;
            }
        }
        else
        {
            Config.warn("TextureAnimation: Source or target texture not specified");
            return null;
        }
    }

    private static byte[] getCustomTextureData(String imagePath, int tileWidth)
    {
        byte[] imageData = loadImage(imagePath, tileWidth);

        if (imageData == null)
        {
            imageData = loadImage("/anim" + imagePath, tileWidth);
        }

        return imageData;
    }

    private static byte[] loadImage(String name, int targetWidth)
    {
        GameSettings gameSettings = Config.getGameSettings();

        try
        {
            ResourceLocation resourceLocation = new ResourceLocation(name);
            InputStream inputStream = Config.getResourceStream(resourceLocation);

            if (inputStream == null)
            {
                return null;
            }
            else
            {
                BufferedImage image = readTextureImage(inputStream);
                inputStream.close();

                if (image == null)
                {
                    return null;
                }
                else
                {
                    if (targetWidth > 0 && image.getWidth() != targetWidth)
                    {
                        double aspectRatio = (double)(image.getHeight() / image.getWidth());
                        int scaledHeight = (int)((double)targetWidth * aspectRatio);
                        image = scaleBufferedImage(image, targetWidth, scaledHeight);
                    }

                    int imageWidth = image.getWidth();
                    int imageHeight = image.getHeight();
                    int[] pixels = new int[imageWidth * imageHeight];
                    byte[] imageData = new byte[imageWidth * imageHeight * 4];
                    image.getRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);

                    for (int pixelIndex = 0; pixelIndex < pixels.length; ++pixelIndex)
                    {
                        int alpha = pixels[pixelIndex] >> 24 & 255;
                        int red = pixels[pixelIndex] >> 16 & 255;
                        int green = pixels[pixelIndex] >> 8 & 255;
                        int blue = pixels[pixelIndex] & 255;

                        if (gameSettings != null && gameSettings.anaglyph)
                        {
                            int anaglyphRed = (red * 30 + green * 59 + blue * 11) / 100;
                            int anaglyphGreen = (red * 30 + green * 70) / 100;
                            int anaglyphBlue = (red * 30 + blue * 70) / 100;
                            red = anaglyphRed;
                            green = anaglyphGreen;
                            blue = anaglyphBlue;
                        }

                        imageData[pixelIndex * 4 + 0] = (byte)red;
                        imageData[pixelIndex * 4 + 1] = (byte)green;
                        imageData[pixelIndex * 4 + 2] = (byte)blue;
                        imageData[pixelIndex * 4 + 3] = (byte)alpha;
                    }

                    return imageData;
                }
            }
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            return null;
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            return null;
        }
    }

    private static BufferedImage readTextureImage(InputStream inputStream) throws IOException
    {
        BufferedImage image = ImageIO.read(inputStream);
        inputStream.close();
        return image;
    }

    private static BufferedImage scaleBufferedImage(BufferedImage image, int width, int height)
    {
        BufferedImage scaledImage = new BufferedImage(width, height, 2);
        Graphics2D graphics = scaledImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, width, height, (ImageObserver)null);
        return scaledImage;
    }

    public static int getCountAnimations()
    {
        return textureAnimations == null ? 0 : textureAnimations.length;
    }

    public static int getCountAnimationsActive()
    {
        return countAnimationsActive;
    }
}
