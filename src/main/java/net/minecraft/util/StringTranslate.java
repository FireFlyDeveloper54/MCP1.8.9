package net.minecraft.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class StringTranslate
{
    private static final Pattern numericVariablePattern = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    private static final Splitter equalSignSplitter = Splitter.on('=').limit(2);
    private static StringTranslate instance = new StringTranslate();
    private final Map<String, String> languageList = Maps.<String, String>newHashMap();
    private long lastUpdateTimeInMilliseconds;

    public StringTranslate()
    {
        try
        {
            InputStream inputstream = StringTranslate.class.getResourceAsStream("/assets/minecraft/lang/en_US.lang");

            if (inputstream == null)
            {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

                if (contextClassLoader != null)
                {
                    inputstream = contextClassLoader.getResourceAsStream("assets/minecraft/lang/en_US.lang");
                }
            }

            if (inputstream == null)
            {
                ClassLoader classLoader = StringTranslate.class.getClassLoader();

                if (classLoader != null)
                {
                    inputstream = classLoader.getResourceAsStream("assets/minecraft/lang/en_US.lang");
                }
            }

            if (inputstream == null)
            {
                this.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
                return;
            }

            try (InputStream stream = inputstream)
            {
                for (String line : IOUtils.readLines(stream, Charsets.UTF_8))
                {
                    if (!line.isEmpty() && line.charAt(0) != 35)
                    {
                        String[] keyValue = (String[])Iterables.toArray(equalSignSplitter.split(line), String.class);

                        if (keyValue != null && keyValue.length == 2)
                        {
                            String translationKey = keyValue[0];
                            String translationValue = numericVariablePattern.matcher(keyValue[1]).replaceAll("%$1s");
                            this.languageList.put(translationKey, translationValue);
                        }
                    }
                }
            }

            this.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
        }
        catch (IOException | RuntimeException ignored)
        {
            ;
        }
    }

    static StringTranslate getInstance()
    {
        return instance;
    }

    public static synchronized void replaceWith(Map<String, String> translations)
    {
        instance.languageList.clear();
        instance.languageList.putAll(translations);
        instance.lastUpdateTimeInMilliseconds = System.currentTimeMillis();
    }

    public synchronized String translateKey(String key)
    {
        return this.tryTranslateKey(key);
    }

    public synchronized String translateKeyFormat(String key, Object... format)
    {
        String translation = this.tryTranslateKey(key);

        try
        {
            return String.format(translation, format);
        }
        catch (IllegalFormatException caughtIllegalFormatException)
        {
            return "Format error: " + translation;
        }
    }

    private String tryTranslateKey(String key)
    {
        String translation = this.languageList.get(key);
        return translation == null ? key : translation;
    }

    public synchronized boolean isKeyTranslated(String key)
    {
        return this.languageList.containsKey(key);
    }

    public long getLastUpdateTimeInMilliseconds()
    {
        return this.lastUpdateTimeInMilliseconds;
    }
}
