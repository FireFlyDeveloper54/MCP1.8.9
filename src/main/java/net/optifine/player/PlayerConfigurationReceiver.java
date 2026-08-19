package net.optifine.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.src.Config;
import net.optifine.http.IFileDownloadListener;

public class PlayerConfigurationReceiver implements IFileDownloadListener
{
    private String player = null;

    public PlayerConfigurationReceiver(String player)
    {
        this.player = player;
    }

    public void fileDownloadFinished(String url, byte[] bytes, Throwable exception)
    {
        if (bytes != null)
        {
            try
            {
                String configText = new String(bytes, "ASCII");
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(configText);
                PlayerConfigurationParser playerConfigurationParser = new PlayerConfigurationParser(this.player);
                PlayerConfiguration playerConfiguration = playerConfigurationParser.parsePlayerConfiguration(jsonElement);

                if (playerConfiguration != null)
                {
                    playerConfiguration.setInitialized(true);
                    PlayerConfigurations.setPlayerConfiguration(this.player, playerConfiguration);
                }
            }
            catch (Exception exception1)
            {
                Config.dbg("Error parsing configuration: " + url + ", " + exception1.getClass().getName() + ": " + exception1.getMessage());
            }
        }
    }
}
