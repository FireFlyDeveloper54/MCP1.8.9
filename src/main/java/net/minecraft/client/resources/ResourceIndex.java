package net.minecraft.client.resources;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceIndex
{
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, File> resourceMap = Maps.<String, File>newHashMap();

    public ResourceIndex(File assetsDir, String assetIndex)
    {
        if (assetIndex != null)
        {
            File file1 = new File(assetsDir, "objects");
            File file2 = new File(assetsDir, "indexes/" + assetIndex + ".json");
            BufferedReader bufferedreader = null;

            try
            {
                bufferedreader = Files.newReader(file2, Charsets.UTF_8);
                JsonObject jsonobject = (new JsonParser()).parse((Reader)bufferedreader).getAsJsonObject();
                JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "objects", (JsonObject)null);

                if (jsonobject1 != null)
                {
                    for (Entry<String, JsonElement> entry : jsonobject1.entrySet())
                    {
                        JsonObject jsonobject2 = (JsonObject)entry.getValue();
                        String resourcePath = (String)entry.getKey();
                        int separatorIndex = resourcePath.indexOf('/');
                        String resourceName = separatorIndex == -1 ? resourcePath : resourcePath.substring(0, separatorIndex) + ":" + resourcePath.substring(separatorIndex + 1);
                        String resourceHash = JsonUtils.getString(jsonobject2, "hash");
                        File file3 = new File(file1, resourceHash.substring(0, 2) + "/" + resourceHash);
                        this.resourceMap.put(resourceName, file3);
                    }
                }
            }
            catch (JsonParseException caughtJsonParseException)
            {
                logger.error("Unable to parse resource index file: " + file2);
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                logger.error("Can\'t find the resource index file: " + file2);
            }
            finally
            {
                IOUtils.closeQuietly((Reader)bufferedreader);
            }
        }
    }

    public Map<String, File> getResourceMap()
    {
        return this.resourceMap;
    }
}
