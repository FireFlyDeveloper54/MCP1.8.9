package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.optifine.render.Blender;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.TextureUtils;

public class CustomSky
{
    private static CustomSkyLayer[][] worldSkyLayers = (CustomSkyLayer[][])null;

    public static void reset()
    {
        worldSkyLayers = (CustomSkyLayer[][])null;
    }

    public static void update()
    {
        reset();

        if (Config.isCustomSky())
        {
            worldSkyLayers = readCustomSkies();
        }
    }

    private static CustomSkyLayer[][] readCustomSkies()
    {
        CustomSkyLayer[][] skyLayersByWorld = new CustomSkyLayer[10][0];
        String worldSkyPathPrefix = "mcpatcher/sky/world";
        int lastWorldId = -1;

        for (int worldId = 0; worldId < skyLayersByWorld.length; ++worldId)
        {
            String skyPathPrefix = worldSkyPathPrefix + worldId + "/sky";
            List skyLayers = new ArrayList();

            for (int layerIndex = 1; layerIndex < 1000; ++layerIndex)
            {
                String propertiesPath = skyPathPrefix + layerIndex + ".properties";

                try
                {
                    ResourceLocation propertiesLocation = new ResourceLocation(propertiesPath);
                    InputStream inputStream = Config.getResourceStream(propertiesLocation);

                    if (inputStream == null)
                    {
                        break;
                    }

                    Properties properties = new PropertiesOrdered();
                    properties.load(inputStream);
                    inputStream.close();
                    Config.dbg("CustomSky properties: " + propertiesPath);
                    String texturePath = skyPathPrefix + layerIndex + ".png";
                    CustomSkyLayer skyLayer = new CustomSkyLayer(properties, texturePath);

                    if (skyLayer.isValid(propertiesPath))
                    {
                        ResourceLocation textureLocation = new ResourceLocation(skyLayer.source);
                        ITextureObject textureObject = TextureUtils.getTexture(textureLocation);

                        if (textureObject == null)
                        {
                            Config.log("CustomSky: Texture not found: " + textureLocation);
                        }
                        else
                        {
                            skyLayer.textureId = textureObject.getGlTextureId();
                            skyLayers.add(skyLayer);
                            inputStream.close();
                        }
                    }
                }
                catch (FileNotFoundException caughtFileNotFoundException)
                {
                    break;
                }
                catch (IOException ioException)
                {
                    net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
                }
            }

            if (skyLayers.size() > 0)
            {
                CustomSkyLayer[] worldSkyLayerArray = (CustomSkyLayer[])((CustomSkyLayer[])skyLayers.toArray(new CustomSkyLayer[skyLayers.size()]));
                skyLayersByWorld[worldId] = worldSkyLayerArray;
                lastWorldId = worldId;
            }
        }

        if (lastWorldId < 0)
        {
            return (CustomSkyLayer[][])null;
        }
        else
        {
            int worldCount = lastWorldId + 1;
            CustomSkyLayer[][] trimmedSkyLayersByWorld = new CustomSkyLayer[worldCount][0];
            System.arraycopy(skyLayersByWorld, 0, trimmedSkyLayersByWorld, 0, worldCount);

            return trimmedSkyLayersByWorld;
        }
    }

    public static void renderSky(World world, TextureManager re, float partialTicks)
    {
        if (worldSkyLayers != null)
        {
            int dimensionId = world.provider.getDimensionId();

            if (dimensionId >= 0 && dimensionId < worldSkyLayers.length)
            {
                CustomSkyLayer[] skyLayers = worldSkyLayers[dimensionId];

                if (skyLayers != null)
                {
                    long worldTime = world.getWorldTime();
                    int timeOfDay = (int)(worldTime % 24000L);
                    float celestialAngle = world.getCelestialAngle(partialTicks);
                    float rainStrength = world.getRainStrength(partialTicks);
                    float thunderStrength = world.getThunderStrength(partialTicks);

                    if (rainStrength > 0.0F)
                    {
                        thunderStrength /= rainStrength;
                    }

                    for (int layerIndex = 0; layerIndex < skyLayers.length; ++layerIndex)
                    {
                        CustomSkyLayer skyLayer = skyLayers[layerIndex];

                        if (skyLayer.isActive(world, timeOfDay))
                        {
                            skyLayer.render(world, timeOfDay, celestialAngle, rainStrength, thunderStrength);
                        }
                    }

                    float clearBlendStrength = 1.0F - rainStrength;
                    Blender.clearBlend(clearBlendStrength);
                }
            }
        }
    }

    public static boolean hasSkyLayers(World world)
    {
        if (worldSkyLayers == null)
        {
            return false;
        }
        else
        {
            int dimensionId = world.provider.getDimensionId();

            if (dimensionId >= 0 && dimensionId < worldSkyLayers.length)
            {
                CustomSkyLayer[] skyLayers = worldSkyLayers[dimensionId];
                return skyLayers == null ? false : skyLayers.length > 0;
            }
            else
            {
                return false;
            }
        }
    }
}
