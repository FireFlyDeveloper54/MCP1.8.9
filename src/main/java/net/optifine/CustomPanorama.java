package net.optifine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.MathUtils;
import net.optifine.util.PropertiesOrdered;

public class CustomPanorama
{
    private static CustomPanoramaProperties customPanoramaProperties = null;
    private static final Random random = new Random();

    public static CustomPanoramaProperties getCustomPanoramaProperties()
    {
        return customPanoramaProperties;
    }

    public static void update()
    {
        customPanoramaProperties = null;
        String[] panoramaFolders = getPanoramaFolders();

        if (panoramaFolders.length > 1)
        {
            Properties[] panoramaProperties = getPanoramaProperties(panoramaFolders);
            int[] weights = getWeights(panoramaProperties);
            int selectedIndex = getRandomIndex(weights);
            String selectedFolder = panoramaFolders[selectedIndex];
            Properties properties = panoramaProperties[selectedIndex];

            if (properties == null)
            {
                properties = panoramaProperties[0];
            }

            if (properties == null)
            {
                properties = new PropertiesOrdered();
            }

            CustomPanoramaProperties selectedPanoramaProperties = new CustomPanoramaProperties(selectedFolder, properties);
            customPanoramaProperties = selectedPanoramaProperties;
        }
    }

    private static String[] getPanoramaFolders()
    {
        List<String> folders = new ArrayList();
        folders.add("textures/gui/title/background");

        for (int folderIndex = 0; folderIndex < 100; ++folderIndex)
        {
            String folder = "optifine/gui/background" + folderIndex;
            String firstPanoramaPath = folder + "/panorama_0.png";
            ResourceLocation resourceLocation = new ResourceLocation(firstPanoramaPath);

            if (Config.hasResource(resourceLocation))
            {
                folders.add(folder);
            }
        }

        String[] panoramaFolders = (String[])((String[])folders.toArray(new String[folders.size()]));
        return panoramaFolders;
    }

    private static Properties[] getPanoramaProperties(String[] folders)
    {
        Properties[] propertiesByFolder = new Properties[folders.length];

        for (int folderIndex = 0; folderIndex < folders.length; ++folderIndex)
        {
            String propertiesFolder = folders[folderIndex];

            if (folderIndex == 0)
            {
                propertiesFolder = "optifine/gui";
            }
            else
            {
                Config.dbg("CustomPanorama: " + propertiesFolder);
            }

            ResourceLocation propertiesLocation = new ResourceLocation(propertiesFolder + "/background.properties");

            try
            {
                InputStream inputStream = Config.getResourceStream(propertiesLocation);

                if (inputStream != null)
                {
                    Properties properties = new PropertiesOrdered();
                    properties.load(inputStream);
                    Config.dbg("CustomPanorama: " + propertiesLocation.getResourcePath());
                    propertiesByFolder[folderIndex] = properties;
                    inputStream.close();
                }
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }

        return propertiesByFolder;
    }

    private static int[] getWeights(Properties[] propertiesByFolder)
    {
        int[] weights = new int[propertiesByFolder.length];

        for (int weightIndex = 0; weightIndex < weights.length; ++weightIndex)
        {
            Properties properties = propertiesByFolder[weightIndex];

            if (properties == null)
            {
                properties = propertiesByFolder[0];
            }

            if (properties == null)
            {
                weights[weightIndex] = 1;
            }
            else
            {
                String weightText = properties.getProperty("weight", (String)null);
                weights[weightIndex] = Config.parseInt(weightText, 1);
            }
        }

        return weights;
    }

    private static int getRandomIndex(int[] weights)
    {
        int totalWeight = MathUtils.getSum(weights);
        int selectedWeight = random.nextInt(totalWeight);
        int runningWeight = 0;

        for (int weightIndex = 0; weightIndex < weights.length; ++weightIndex)
        {
            runningWeight += weights[weightIndex];

            if (runningWeight > selectedWeight)
            {
                return weightIndex;
            }
        }

        return weights.length - 1;
    }
}
