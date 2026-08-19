package net.optifine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.src.Config;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CustomLoadingScreens
{
    private static CustomLoadingScreen[] screens = null;
    private static int screensMinDimensionId = 0;

    public static CustomLoadingScreen getCustomLoadingScreen()
    {
        if (screens == null)
        {
            return null;
        }
        else
        {
            int dimensionId = PacketThreadUtil.lastDimensionId;
            int screenIndex = dimensionId - screensMinDimensionId;
            CustomLoadingScreen loadingScreen = null;

            if (screenIndex >= 0 && screenIndex < screens.length)
            {
                loadingScreen = screens[screenIndex];
            }

            return loadingScreen;
        }
    }

    public static void update()
    {
        screens = null;
        screensMinDimensionId = 0;
        Pair<CustomLoadingScreen[], Integer> pair = parseScreens();
        screens = (CustomLoadingScreen[])pair.getLeft();
        screensMinDimensionId = ((Integer)pair.getRight()).intValue();
    }

    private static Pair<CustomLoadingScreen[], Integer> parseScreens()
    {
        String pathPrefix = "optifine/gui/loading/background";
        String pathSuffix = ".png";
        String[] screenPaths = ResUtils.collectFiles(pathPrefix, pathSuffix);
        Map<Integer, String> pathsByDimension = new HashMap();

        for (int pathIndex = 0; pathIndex < screenPaths.length; ++pathIndex)
        {
            String screenPath = screenPaths[pathIndex];
            String dimensionIdText = StrUtils.removePrefixSuffix(screenPath, pathPrefix, pathSuffix);
            int dimensionId = Config.parseInt(dimensionIdText, Integer.MIN_VALUE);

            if (dimensionId == Integer.MIN_VALUE)
            {
                warn("Invalid dimension ID: " + dimensionIdText + ", path: " + screenPath);
            }
            else
            {
                pathsByDimension.put(Integer.valueOf(dimensionId), screenPath);
            }
        }

        Set<Integer> dimensions = pathsByDimension.keySet();
        Integer[] sortedDimensions = (Integer[])dimensions.toArray(new Integer[dimensions.size()]);
        Arrays.sort((Object[])sortedDimensions);

        if (sortedDimensions.length <= 0)
        {
            return new ImmutablePair((Object)null, Integer.valueOf(0));
        }
        else
        {
            String propertiesPath = "optifine/gui/loading/loading.properties";
            Properties properties = ResUtils.readProperties(propertiesPath, "CustomLoadingScreens");
            int minDimensionId = sortedDimensions[0].intValue();
            int maxDimensionId = sortedDimensions[sortedDimensions.length - 1].intValue();
            int screenCount = maxDimensionId - minDimensionId + 1;
            CustomLoadingScreen[] parsedScreens = new CustomLoadingScreen[screenCount];

            for (int dimensionIndex = 0; dimensionIndex < sortedDimensions.length; ++dimensionIndex)
            {
                Integer dimensionId = sortedDimensions[dimensionIndex];
                String screenPath = (String)pathsByDimension.get(dimensionId);
                parsedScreens[dimensionId.intValue() - minDimensionId] = CustomLoadingScreen.parseScreen(screenPath, dimensionId.intValue(), properties);
            }

            return new ImmutablePair(parsedScreens, Integer.valueOf(minDimensionId));
        }
    }

    public static void warn(String str)
    {
        Config.warn("CustomLoadingScreen: " + str);
    }

    public static void dbg(String str)
    {
        Config.dbg("CustomLoadingScreen: " + str);
    }
}
