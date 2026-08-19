package net.optifine;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class Lang
{
    private static final Splitter splitter = Splitter.on('=').limit(2);
    private static final Pattern pattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

    public static void resourcesReloaded()
    {
        Map localeProperties = I18n.getLocaleProperties();
        List<String> languageFiles = new ArrayList();
        String languagePath = "optifine/lang/";
        String defaultLanguage = "en_US";
        String languageSuffix = ".lang";
        languageFiles.add(languagePath + defaultLanguage + languageSuffix);

        if (!Config.getGameSettings().language.equals(defaultLanguage))
        {
            languageFiles.add(languagePath + Config.getGameSettings().language + languageSuffix);
        }

        String[] languageFileArray = (String[])((String[])languageFiles.toArray(new String[languageFiles.size()]));
        loadResources(Config.getDefaultResourcePack(), languageFileArray, localeProperties);
        IResourcePack[] resourcePacks = Config.getResourcePacks();

        for (int packIndex = 0; packIndex < resourcePacks.length; ++packIndex)
        {
            IResourcePack resourcePack = resourcePacks[packIndex];
            loadResources(resourcePack, languageFileArray, localeProperties);
        }
    }

    private static void loadResources(IResourcePack resourcePack, String[] files, Map localeProperties)
    {
        try
        {
            for (int fileIndex = 0; fileIndex < files.length; ++fileIndex)
            {
                String file = files[fileIndex];
                ResourceLocation resourceLocation = new ResourceLocation(file);

                if (resourcePack.resourceExists(resourceLocation))
                {
                    InputStream inputStream = resourcePack.getInputStream(resourceLocation);

                    if (inputStream != null)
                    {
                        loadLocaleData(inputStream, localeProperties);
                    }
                }
            }
        }
        catch (IOException ioException)
        {
            net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
        }
    }

    public static void loadLocaleData(InputStream inputStream, Map localeProperties) throws IOException
    {
        Iterator lineIterator = IOUtils.readLines(inputStream, Charsets.UTF_8).iterator();
        inputStream.close();

        while (lineIterator.hasNext())
        {
            String line = (String)lineIterator.next();

            if (!line.isEmpty() && line.charAt(0) != 35)
            {
                String[] keyValue = (String[])((String[])Iterables.toArray(splitter.split(line), String.class));

                if (keyValue != null && keyValue.length == 2)
                {
                    String key = keyValue[0];
                    String value = pattern.matcher(keyValue[1]).replaceAll("%$1s");
                    localeProperties.put(key, value);
                }
            }
        }
    }

    public static String get(String key)
    {
        return I18n.format(key, new Object[0]);
    }

    public static String get(String key, String def)
    {
        String translated = I18n.format(key, new Object[0]);
        return translated != null && !translated.equals(key) ? translated : def;
    }

    public static String getOn()
    {
        return I18n.format("options.on", new Object[0]);
    }

    public static String getOff()
    {
        return I18n.format("options.off", new Object[0]);
    }

    public static String getFast()
    {
        return I18n.format("options.graphics.fast", new Object[0]);
    }

    public static String getFancy()
    {
        return I18n.format("options.graphics.fancy", new Object[0]);
    }

    public static String getDefault()
    {
        return I18n.format("generator.default", new Object[0]);
    }
}
