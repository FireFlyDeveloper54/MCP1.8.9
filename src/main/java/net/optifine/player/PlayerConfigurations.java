package net.optifine.player;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;

public class PlayerConfigurations
{
    private static Map mapConfigurations = null;
    private static boolean reloadPlayerItems = Boolean.getBoolean("player.models.reload");
    private static long timeReloadPlayerItemsMs = System.currentTimeMillis();

    public static void renderPlayerItems(ModelBiped modelBiped, AbstractClientPlayer player, float scale, float partialTicks)
    {
        PlayerConfiguration playerConfiguration = getPlayerConfiguration(player);

        if (playerConfiguration != null)
        {
            playerConfiguration.renderPlayerItems(modelBiped, player, scale, partialTicks);
        }
    }

    public static synchronized PlayerConfiguration getPlayerConfiguration(AbstractClientPlayer player)
    {
        if (reloadPlayerItems && System.currentTimeMillis() > timeReloadPlayerItemsMs + 5000L)
        {
            AbstractClientPlayer currentPlayer = Minecraft.getMinecraft().thePlayer;

            if (currentPlayer != null)
            {
                setPlayerConfiguration(currentPlayer.getNameClear(), (PlayerConfiguration)null);
                timeReloadPlayerItemsMs = System.currentTimeMillis();
            }
        }

        String playerName = player.getNameClear();

        if (playerName == null)
        {
            return null;
        }
        else
        {
            PlayerConfiguration playerConfiguration = (PlayerConfiguration)getMapConfigurations().get(playerName);

            if (playerConfiguration == null)
            {
                playerConfiguration = new PlayerConfiguration();
                getMapConfigurations().put(playerName, playerConfiguration);

            }

            return playerConfiguration;
        }
    }

    public static synchronized void setPlayerConfiguration(String player, PlayerConfiguration pc)
    {
        getMapConfigurations().put(player, pc);
    }

    private static Map getMapConfigurations()
    {
        if (mapConfigurations == null)
        {
            mapConfigurations = new HashMap();
        }

        return mapConfigurations;
    }
}
