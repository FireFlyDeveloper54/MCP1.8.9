package net.optifine.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

public class FontUtils
{
    public static Properties readFontProperties(ResourceLocation locationFontTexture)
    {
        String texturePath = locationFontTexture.getResourcePath();
        Properties properties = new PropertiesOrdered();
        String pngSuffix = ".png";

        if (!texturePath.endsWith(pngSuffix))
        {
            return properties;
        }
        else
        {
            String propertiesPath = texturePath.substring(0, texturePath.length() - pngSuffix.length()) + ".properties";

            try
            {
                ResourceLocation propertiesLocation = new ResourceLocation(locationFontTexture.getResourceDomain(), propertiesPath);
                InputStream inputStream = Config.getResourceStream(Config.getResourceManager(), propertiesLocation);

                if (inputStream == null)
                {
                    return properties;
                }

                Config.log("Loading " + propertiesPath);
                properties.load(inputStream);
                inputStream.close();
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                ;
            }
            catch (IOException ioException)
            {
                net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
            }

            return properties;
        }
    }

    public static void readCustomCharWidths(Properties props, float[] charWidth)
    {
        for (Object o : props.keySet())
        {
            String propertyName = (String) o;
            String widthPrefix = "width.";

            if (propertyName.startsWith(widthPrefix))
            {
                String charIndexText = propertyName.substring(widthPrefix.length());
                int charIndex = Config.parseInt(charIndexText, -1);

                if (charIndex >= 0 && charIndex < charWidth.length)
                {
                    String widthText = props.getProperty(propertyName);
                    float width = Config.parseFloat(widthText, -1.0F);

                    if (width >= 0.0F)
                    {
                        charWidth[charIndex] = width;
                    }
                }
            }
        }
    }

    public static float readFloat(Properties props, String key, float defOffset)
    {
        String valueText = props.getProperty(key);

        if (valueText == null)
        {
            return defOffset;
        }
        else
        {
            float value = Config.parseFloat(valueText, Float.MIN_VALUE);

            if (value == Float.MIN_VALUE)
            {
                Config.warn("Invalid value for " + key + ": " + valueText);
                return defOffset;
            }
            else
            {
                return value;
            }
        }
    }

    public static boolean readBoolean(Properties props, String key, boolean defVal)
    {
        String valueText = props.getProperty(key);

        if (valueText == null)
        {
            return defVal;
        }
        else
        {
            String normalizedValue = valueText.toLowerCase().trim();

            if (!normalizedValue.equals("true") && !normalizedValue.equals("on"))
            {
                if (!normalizedValue.equals("false") && !normalizedValue.equals("off"))
                {
                    Config.warn("Invalid value for " + key + ": " + valueText);
                    return defVal;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
    }

    public static ResourceLocation getHdFontLocation(ResourceLocation fontLoc)
    {
        if (!Config.isCustomFonts())
        {
            return fontLoc;
        }
        else if (fontLoc == null)
        {
            return fontLoc;
        }
        else if (!Config.isMinecraftThread())
        {
            return fontLoc;
        }
        else
        {
            String fontPath = fontLoc.getResourcePath();
            String texturesPrefix = "textures/";
            String mcpatcherPrefix = "mcpatcher/";

            if (!fontPath.startsWith(texturesPrefix))
            {
                return fontLoc;
            }
            else
            {
                String hdFontPath = fontPath.substring(texturesPrefix.length());
                hdFontPath = mcpatcherPrefix + hdFontPath;
                ResourceLocation hdFontLocation = new ResourceLocation(fontLoc.getResourceDomain(), hdFontPath);
                return Config.hasResource(Config.getResourceManager(), hdFontLocation) ? hdFontLocation : fontLoc;
            }
        }
    }
}
