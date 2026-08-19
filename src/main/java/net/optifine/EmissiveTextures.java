package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.PropertiesOrdered;

public class EmissiveTextures
{
    private static String suffixEmissive = null;
    private static String suffixEmissivePng = null;
    private static boolean active = false;
    private static boolean render = false;
    private static boolean hasEmissive = false;
    private static boolean renderEmissive = false;
    private static float lightMapX;
    private static float lightMapY;
    private static final String SUFFIX_PNG = ".png";
    private static final ResourceLocation LOCATION_EMPTY = new ResourceLocation("mcpatcher/ctm/default/empty.png");

    public static boolean isActive()
    {
        return active;
    }

    public static String getSuffixEmissive()
    {
        return suffixEmissive;
    }

    public static void beginRender()
    {
        render = true;
        hasEmissive = false;
    }

    public static ITextureObject getEmissiveTexture(ITextureObject texture, Map<ResourceLocation, ITextureObject> mapTextures)
    {
        if (!render)
        {
            return texture;
        }
        else if (!(texture instanceof SimpleTexture))
        {
            return texture;
        }
        else
        {
            SimpleTexture simpleTexture = (SimpleTexture)texture;
            ResourceLocation emissiveLocation = simpleTexture.locationEmissive;

            if (!renderEmissive)
            {
                if (emissiveLocation != null)
                {
                    hasEmissive = true;
                }

                return texture;
            }
            else
            {
                if (emissiveLocation == null)
                {
                    emissiveLocation = LOCATION_EMPTY;
                }

                ITextureObject emissiveTexture = (ITextureObject)mapTextures.get(emissiveLocation);

                if (emissiveTexture == null)
                {
                    emissiveTexture = new SimpleTexture(emissiveLocation);
                    TextureManager textureManager = Config.getTextureManager();
                    textureManager.loadTexture(emissiveLocation, emissiveTexture);
                }

                return emissiveTexture;
            }
        }
    }

    public static boolean hasEmissive()
    {
        return hasEmissive;
    }

    public static void beginRenderEmissive()
    {
        lightMapX = OpenGlHelper.lastBrightnessX;
        lightMapY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, lightMapY);
        renderEmissive = true;
    }

    public static void endRenderEmissive()
    {
        renderEmissive = false;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightMapX, lightMapY);
    }

    public static void endRender()
    {
        render = false;
        hasEmissive = false;
    }

    public static void update()
    {
        active = false;
        suffixEmissive = null;
        suffixEmissivePng = null;

        if (Config.isEmissiveTextures())
        {
            try
            {
                String propertiesPath = "optifine/emissive.properties";
                ResourceLocation propertiesLocation = new ResourceLocation(propertiesPath);
                InputStream inputStream = Config.getResourceStream(propertiesLocation);

                if (inputStream == null)
                {
                    return;
                }

                dbg("Loading " + propertiesPath);
                Properties properties = new PropertiesOrdered();
                properties.load(inputStream);
                inputStream.close();
                suffixEmissive = properties.getProperty("suffix.emissive");

                if (suffixEmissive != null)
                {
                    suffixEmissivePng = suffixEmissive + ".png";
                }

                active = suffixEmissive != null;
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                return;
            }
            catch (IOException ioException)
            {
                net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
            }
        }
    }

    private static void dbg(String str)
    {
        Config.dbg("EmissiveTextures: " + str);
    }

    private static void warn(String str)
    {
        Config.warn("EmissiveTextures: " + str);
    }

    public static boolean isEmissive(ResourceLocation loc)
    {
        return suffixEmissivePng == null ? false : loc.getResourcePath().endsWith(suffixEmissivePng);
    }

    public static void loadTexture(ResourceLocation loc, SimpleTexture tex)
    {
        if (loc != null && tex != null)
        {
            tex.isEmissive = false;
            tex.locationEmissive = null;

            if (suffixEmissivePng != null)
            {
                String texturePath = loc.getResourcePath();

                if (texturePath.endsWith(".png"))
                {
                    if (texturePath.endsWith(suffixEmissivePng))
                    {
                        tex.isEmissive = true;
                    }
                    else
                    {
                        String emissivePath = texturePath.substring(0, texturePath.length() - ".png".length()) + suffixEmissivePng;
                        ResourceLocation emissiveLocation = new ResourceLocation(loc.getResourceDomain(), emissivePath);

                        if (Config.hasResource(emissiveLocation))
                        {
                            tex.locationEmissive = emissiveLocation;
                        }
                    }
                }
            }
        }
    }
}
