package net.optifine.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Config;

public class HttpUtils
{
    private static String playerItemsUrl = null;
    public static final String SERVER_URL = "";
    public static final String POST_URL = "";

    public static byte[] get(String urlStr) throws IOException
    {
        throw new IOException("OptiFine online downloads are disabled");
    }

    public static String post(String urlStr, Map headers, byte[] content) throws IOException
    {
        return "";
    }

    public static synchronized String getPlayerItemsUrl()
    {
        if (playerItemsUrl == null)
        {
            try
            {
                boolean localPlayerModels = Config.parseBoolean(System.getProperty("player.models.local"), false);

                if (localPlayerModels)
                {
                    File minecraftDir = Minecraft.getMinecraft().mcDataDir;
                    File playerModelsDir = new File(minecraftDir, "playermodels");
                    playerItemsUrl = playerModelsDir.toURI().toURL().toExternalForm();
                }
            }
            catch (Exception exception)
            {
                Config.warn("" + exception.getClass().getName() + ": " + exception.getMessage());
            }

            if (playerItemsUrl == null)
            {
                playerItemsUrl = "";
            }
        }

        return playerItemsUrl;
    }
}
