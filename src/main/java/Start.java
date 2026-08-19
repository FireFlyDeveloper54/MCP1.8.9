import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.main.Main;

public class Start
{
    private static final String LWJGL_LIBRARY_PATH_PROPERTY = "org.lwjgl.librarypath";
    private static final String JINPUT_LIBRARY_PATH_PROPERTY = "net.java.games.input.librarypath";
    private static final String VERSION_NATIVES_DIRECTORY = "versions/1.8.9";
    private static final String RUN_LOCAL_NATIVES_DIRECTORY = "../natives";
    private static final String PROJECT_LOCAL_NATIVES_DIRECTORY = "natives";

    public static void main(String[] args)
    {
        configureNativeLibraries();

        Main.main(buildLaunchArguments(args));
    }

    private static void configureNativeLibraries()
    {
        File nativeDirectory = getExistingNativeSystemDirectory();

        if (nativeDirectory == null)
        {
            nativeDirectory = getConfiguredNativeDirectory();
        }

        if (nativeDirectory == null)
        {
            nativeDirectory = findVersionNativeDirectory();
        }

        if (nativeDirectory == null)
        {
            nativeDirectory = getLocalNativeDirectory(RUN_LOCAL_NATIVES_DIRECTORY);
        }

        if (nativeDirectory == null)
        {
            nativeDirectory = getLocalNativeDirectory(PROJECT_LOCAL_NATIVES_DIRECTORY);
        }

        if (nativeDirectory != null && nativeDirectory.isDirectory())
        {
            String libraryPath = nativeDirectory.getAbsolutePath();
            System.out.println("Setting LWJGL Library Path to: " + libraryPath);
            System.setProperty(LWJGL_LIBRARY_PATH_PROPERTY, libraryPath);
            System.setProperty(JINPUT_LIBRARY_PATH_PROPERTY, libraryPath);
        }
        else
        {
            System.err.println("Unable to find LWJGL natives directory for this platform.");
        }
    }

    private static File findVersionNativeDirectory()
    {
        File versionDirectory = new File(VERSION_NATIVES_DIRECTORY);
        if (versionDirectory.exists() && versionDirectory.isDirectory())
        {
            File[] nativeCandidates = versionDirectory.listFiles();
            if (nativeCandidates != null)
            {
                for (File candidate : nativeCandidates)
                {
                    if (candidate.isDirectory() && candidate.getName().startsWith("native"))
                    {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private static File getExistingNativeSystemDirectory()
    {
        File lwjglDirectory = getSystemPropertyDirectory(LWJGL_LIBRARY_PATH_PROPERTY);

        if (lwjglDirectory != null)
        {
            return lwjglDirectory;
        }

        return getSystemPropertyDirectory(JINPUT_LIBRARY_PATH_PROPERTY);
    }

    private static File getSystemPropertyDirectory(String propertyName)
    {
        String propertyValue = System.getProperty(propertyName);
        return propertyValue != null && propertyValue.length() > 0 ? getExistingDirectory(new File(propertyValue)) : null;
    }

    private static File getConfiguredNativeDirectory()
    {
        String configuredPath = getLaunchProperty("nativesDir", "NATIVES_DIR", null);
        return configuredPath != null ? getExistingDirectory(new File(configuredPath)) : null;
    }

    private static File getLocalNativeDirectory(String baseDirectory)
    {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("windows"))
        {
            File platformDirectory = getExistingDirectory(new File(baseDirectory, "windows"));
            if (platformDirectory != null)
            {
                return platformDirectory;
            }
        }

        if (osName.startsWith("linux"))
        {
            File platformDirectory = getExistingDirectory(new File(baseDirectory, "linux"));
            if (platformDirectory != null)
            {
                return platformDirectory;
            }
        }

        // Some distributions keep platform natives directly in the base directory.
        return getExistingDirectory(new File(baseDirectory));
    }

    private static File getExistingDirectory(File directory)
    {
        return directory.isDirectory() ? directory : null;
    }

    private static String[] buildLaunchArguments(String[] userArgs)
    {
        List<String> launchArgs = new ArrayList<String>();
        addOptionIfAbsent(launchArgs, userArgs, "--version", getLaunchProperty("version", "VERSION", "mcp"));
        addOptionIfAbsent(launchArgs, userArgs, "--username", getLaunchProperty("username", "USERNAME", null));
        addOptionIfAbsent(launchArgs, userArgs, "--uuid", getLaunchProperty("uuid", "UUID", null));
        addOptionIfAbsent(launchArgs, userArgs, "--accessToken", getLaunchProperty("accessToken", "ACCESS_TOKEN", "0"));
        addOptionIfAbsent(launchArgs, userArgs, "--assetsDir", getLaunchProperty("assetsDir", "ASSETS_DIR", "assets"));
        addOptionIfAbsent(launchArgs, userArgs, "--assetIndex", getLaunchProperty("assetIndex", "ASSET_INDEX", "1.8"));
        addOptionIfAbsent(launchArgs, userArgs, "--userProperties", getLaunchProperty("userProperties", "USER_PROPERTIES", "{}"));

        for (String userArg : userArgs)
        {
            launchArgs.add(userArg);
        }

        return launchArgs.toArray(new String[launchArgs.size()]);
    }

    private static void addOptionIfAbsent(List<String> launchArgs, String[] userArgs, String option, String value)
    {
        if (value != null && value.length() > 0 && !hasOption(userArgs, option))
        {
            launchArgs.add(option);
            launchArgs.add(value);
        }
    }

    private static boolean hasOption(String[] args, String option)
    {
        String optionWithValue = option + "=";

        for (String arg : args)
        {
            if (option.equals(arg) || arg.startsWith(optionWithValue))
            {
                return true;
            }
        }

        return false;
    }

    private static String getLaunchProperty(String propertyName, String environmentName, String defaultValue)
    {
        String value = System.getProperty("mcp." + propertyName);

        if (value == null || value.length() == 0)
        {
            value = System.getenv("MCP_" + environmentName);
        }

        return value != null && value.length() > 0 ? value : defaultValue;
    }
}