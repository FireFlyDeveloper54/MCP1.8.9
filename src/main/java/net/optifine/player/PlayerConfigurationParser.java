package net.optifine.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.awt.image.BufferedImage;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.Json;

public class PlayerConfigurationParser
{
    private String player = null;
    public static final String CONFIG_ITEMS = "items";
    public static final String ITEM_TYPE = "type";
    public static final String ITEM_ACTIVE = "active";

    public PlayerConfigurationParser(String player)
    {
        this.player = player;
    }

    public PlayerConfiguration parsePlayerConfiguration(JsonElement je)
    {
        if (je == null)
        {
            throw new JsonParseException("JSON object is null, player: " + this.player);
        }
        else
        {
            JsonObject rootObject = (JsonObject)je;
            PlayerConfiguration playerConfiguration = new PlayerConfiguration();
            JsonArray itemArray = (JsonArray)rootObject.get("items");

            if (itemArray != null)
            {
                for (int itemIndex = 0; itemIndex < itemArray.size(); ++itemIndex)
                {
                    JsonObject itemObject = (JsonObject)itemArray.get(itemIndex);
                    boolean active = Json.getBoolean(itemObject, "active", true);

                    if (active)
                    {
                        String itemType = Json.getString(itemObject, "type");

                        if (itemType == null)
                        {
                            Config.warn("Item type is null, player: " + this.player);
                        }
                        else
                        {
                            String modelPath = Json.getString(itemObject, "model");

                            if (modelPath == null)
                            {
                                modelPath = "items/" + itemType + "/model.cfg";
                            }

                            PlayerItemModel itemModel = this.downloadModel(modelPath);

                            if (itemModel != null)
                            {
                                if (!itemModel.isUsePlayerTexture())
                                {
                                    String texturePath = Json.getString(itemObject, "texture");

                                    if (texturePath == null)
                                    {
                                        texturePath = "items/" + itemType + "/users/" + this.player + ".png";
                                    }

                                    BufferedImage textureImage = this.downloadTextureImage(texturePath);

                                    if (textureImage == null)
                                    {
                                        continue;
                                    }

                                    itemModel.setTextureImage(textureImage);
                                    ResourceLocation textureLocation = new ResourceLocation("optifine.local", texturePath);
                                    itemModel.setTextureLocation(textureLocation);
                                }

                                playerConfiguration.addPlayerItemModel(itemModel);
                            }
                        }
                    }
                }
            }

            return playerConfiguration;
        }
    }

    private BufferedImage downloadTextureImage(String texturePath)
    {
        // OptiFine online player-item texture downloads are disabled for this local MCP project.
        return null;
    }

    private PlayerItemModel downloadModel(String modelPath)
    {
        // OptiFine online player-item model downloads are disabled for this local MCP project.
        return null;
    }
}
