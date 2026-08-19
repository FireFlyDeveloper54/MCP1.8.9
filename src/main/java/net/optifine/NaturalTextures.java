package net.optifine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.TextureUtils;

public class NaturalTextures
{
    private static NaturalProperties[] propertiesByIndex = new NaturalProperties[0];

    public static void update()
    {
        propertiesByIndex = new NaturalProperties[0];

        if (Config.isNaturalTextures())
        {
            String propertiesPath = "optifine/natural.properties";

            try
            {
                ResourceLocation propertiesLocation = new ResourceLocation(propertiesPath);

                if (!Config.hasResource(propertiesLocation))
                {
                    Config.dbg("NaturalTextures: configuration \"" + propertiesPath + "\" not found");
                    return;
                }

                boolean fromDefaultResourcePack = Config.isFromDefaultResourcePack(propertiesLocation);
                InputStream inputStream = Config.getResourceStream(propertiesLocation);
                ArrayList naturalPropertiesList = new ArrayList(256);
                String propertiesText = Config.readInputStream(inputStream);
                inputStream.close();
                String[] propertyLines = Config.tokenize(propertiesText, "\n\r");

                if (fromDefaultResourcePack)
                {
                    Config.dbg("Natural Textures: Parsing default configuration \"" + propertiesPath + "\"");
                    Config.dbg("Natural Textures: Valid only for textures from default resource pack");
                }
                else
                {
                    Config.dbg("Natural Textures: Parsing configuration \"" + propertiesPath + "\"");
                }

                TextureMap textureMap = TextureUtils.getTextureMapBlocks();

                for (int lineIndex = 0; lineIndex < propertyLines.length; ++lineIndex)
                {
                    String line = propertyLines[lineIndex].trim();

                    if (!line.startsWith("#"))
                    {
                        String[] keyValue = Config.tokenize(line, "=");

                        if (keyValue.length != 2)
                        {
                            Config.warn("Natural Textures: Invalid \"" + propertiesPath + "\" line: " + line);
                        }
                        else
                        {
                            String textureName = keyValue[0].trim();
                            String naturalPropertiesText = keyValue[1].trim();
                            TextureAtlasSprite textureSprite = textureMap.getSpriteSafe("minecraft:blocks/" + textureName);

                            if (textureSprite == null)
                            {
                                Config.warn("Natural Textures: Texture not found: \"" + propertiesPath + "\" line: " + line);
                            }
                            else
                            {
                                int textureIndex = textureSprite.getIndexInMap();

                                if (textureIndex < 0)
                                {
                                    Config.warn("Natural Textures: Invalid \"" + propertiesPath + "\" line: " + line);
                                }
                                else
                                {
                                    if (fromDefaultResourcePack && !Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/" + textureName + ".png")))
                                    {
                                        return;
                                    }

                                    NaturalProperties naturalProperties = new NaturalProperties(naturalPropertiesText);

                                    if (naturalProperties.isValid())
                                    {
                                        while (naturalPropertiesList.size() <= textureIndex)
                                        {
                                            naturalPropertiesList.add(null);
                                        }

                                        naturalPropertiesList.set(textureIndex, naturalProperties);
                                        Config.dbg("NaturalTextures: " + textureName + " = " + naturalPropertiesText);
                                    }
                                }
                            }
                        }
                    }
                }

                propertiesByIndex = (NaturalProperties[])((NaturalProperties[])naturalPropertiesList.toArray(new NaturalProperties[naturalPropertiesList.size()]));
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                Config.warn("NaturalTextures: configuration \"" + propertiesPath + "\" not found");
                return;
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }
    }

    public static BakedQuad getNaturalTexture(BlockPos blockPosIn, BakedQuad quad)
    {
        TextureAtlasSprite textureSprite = quad.getSprite();

        if (textureSprite == null)
        {
            return quad;
        }
        else
        {
            NaturalProperties naturalProperties = getNaturalProperties(textureSprite);

            if (naturalProperties == null)
            {
                return quad;
            }
            else
            {
                int side = ConnectedTextures.getSide(quad.getFace());
                int random = Config.getRandom(blockPosIn, side);
                int rotation = 0;
                boolean flip = false;

                if (naturalProperties.rotation > 1)
                {
                    rotation = random & 3;
                }

                if (naturalProperties.rotation == 2)
                {
                    rotation = rotation / 2 * 2;
                }

                if (naturalProperties.flip)
                {
                    flip = (random & 4) != 0;
                }

                return naturalProperties.getQuad(quad, rotation, flip);
            }
        }
    }

    public static NaturalProperties getNaturalProperties(TextureAtlasSprite icon)
    {
        if (!(icon instanceof TextureAtlasSprite))
        {
            return null;
        }
        else
        {
            int textureIndex = icon.getIndexInMap();

            if (textureIndex >= 0 && textureIndex < propertiesByIndex.length)
            {
                NaturalProperties naturalProperties = propertiesByIndex[textureIndex];
                return naturalProperties;
            }
            else
            {
                return null;
            }
        }
    }
}
