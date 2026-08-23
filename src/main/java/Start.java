import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.main.Main;

public class Start
{
    private static final String MODULES_OPENED_PROPERTY = "mcp.modules.opened";
    private static final String CLASSPATH_PATCHED_PROPERTY = "mcp.classpath.patched";
    private static final int MIN_SUPPORTED_JAVA = 8;
    private static final int MAX_SUPPORTED_JAVA = 25;

    public static void main(String[] args)
    {
        relaunchIfNeeded(args);

        Main.main(buildLaunchArguments(args));
    }

    private static void relaunchIfNeeded(String[] args)
    {
        int javaVersion = getJavaMajorVersion();

        if (javaVersion < MIN_SUPPORTED_JAVA)
        {
            System.err.println("This project requires Java " + MIN_SUPPORTED_JAVA + "-" + MAX_SUPPORTED_JAVA + " (found " + javaVersion + ").");
            System.exit(1);
        }

        if (javaVersion > MAX_SUPPORTED_JAVA)
        {
            System.err.println("Warning: Java " + javaVersion + " is newer than the supported range " + MIN_SUPPORTED_JAVA + "-" + MAX_SUPPORTED_JAVA + ".");
        }

        boolean missingLibraries = !isClassAvailable("joptsimple.OptionSpec") || !isClassAvailable("org.lwjgl.glfw.GLFW");
        boolean needModuleOpens = javaVersion >= 16 && !isModulePatchApplied();

        if (!missingLibraries && !needModuleOpens)
        {
            return;
        }

        if (missingLibraries && isClasspathPatched())
        {
            System.err.println("Required libraries are still missing after classpath rebuild (joptsimple / LWJGL).");
            System.err.println("java.class.path=" + System.getProperty("java.class.path"));
            System.exit(1);
        }

        if (!missingLibraries && needModuleOpens && isDebuggerAttached())
        {
            return;
        }

        try
        {
            List<String> command = new ArrayList<String>();
            command.add(getJavaExecutable());

            if (needModuleOpens)
            {
                addModuleOpens(command);

                if (javaVersion >= 22)
                {
                    command.add("--enable-native-access=ALL-UNNAMED");
                }

                command.add("-D" + MODULES_OPENED_PROPERTY + "=true");
            }

            command.add("-D" + CLASSPATH_PATCHED_PROPERTY + "=true");

            List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (int i = 0; i < inputArguments.size(); ++i)
            {
                String argument = inputArguments.get(i);
                if (shouldSkipInheritedArgument(argument))
                {
                    continue;
                }
                command.add(argument);
            }

            command.add("-cp");
            command.add(buildFullClasspath());
            command.add("Start");

            for (int i = 0; i < args.length; ++i)
            {
                command.add(args[i]);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            processBuilder.directory(new File(System.getProperty("user.dir", ".")));
            System.exit(processBuilder.start().waitFor());
        }
        catch (Exception exception)
        {
            System.err.println("Unable to relaunch with a complete classpath.");
            exception.printStackTrace();
            if (missingLibraries)
            {
                System.exit(1);
            }
        }
    }

    private static boolean shouldSkipInheritedArgument(String argument)
    {
        return argument.startsWith("-Djava.class.path=")
            || argument.startsWith("-D" + MODULES_OPENED_PROPERTY + "=")
            || argument.startsWith("-D" + CLASSPATH_PATCHED_PROPERTY + "=")
            || argument.startsWith("-cp")
            || argument.startsWith("-classpath");
    }

    private static boolean isClasspathPatched()
    {
        return "true".equalsIgnoreCase(System.getProperty(CLASSPATH_PATCHED_PROPERTY));
    }

    private static boolean isClassAvailable(String className)
    {
        try
        {
            Class.forName(className, false, Start.class.getClassLoader());
            return true;
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    private static String buildFullClasspath()
    {
        LinkedHashSet<String> entries = new LinkedHashSet<String>();
        String current = System.getProperty("java.class.path", "");
        if (current.length() > 0)
        {
            String[] parts = current.split(File.pathSeparator);
            for (int i = 0; i < parts.length; ++i)
            {
                if (parts[i].length() > 0 && !isSkippedJar(parts[i]))
                {
                    entries.add(new File(parts[i]).getAbsolutePath());
                }
            }
        }

        File root = findProjectRoot();
        collectJars(new File(root, "libraries"), entries);

        File production = new File(root, "out" + File.separator + "production" + File.separator + "MCP1.8.9");
        if (production.isDirectory())
        {
            entries.add(production.getAbsolutePath());
        }

        StringBuilder builder = new StringBuilder();
        for (String entry : entries)
        {
            if (builder.length() > 0)
            {
                builder.append(File.pathSeparatorChar);
            }
            builder.append(entry);
        }
        return builder.toString();
    }

    private static File findProjectRoot()
    {
        File current = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        File found = findProjectRootFrom(current);
        if (found != null)
        {
            return found;
        }

        String classpath = System.getProperty("java.class.path", "");
        String[] parts = classpath.split(File.pathSeparator);
        for (int i = 0; i < parts.length; ++i)
        {
            if (parts[i].length() == 0)
            {
                continue;
            }
            File entry = new File(parts[i]).getAbsoluteFile();
            File directory = entry.isFile() ? entry.getParentFile() : entry;
            found = findProjectRootFrom(directory);
            if (found != null)
            {
                return found;
            }
        }

        return current;
    }

    private static File findProjectRootFrom(File start)
    {
        File probe = start;
        for (int i = 0; i < 8 && probe != null; ++i)
        {
            if (new File(probe, "libraries").isDirectory() && new File(probe, "src").isDirectory())
            {
                return probe;
            }
            probe = probe.getParentFile();
        }
        return null;
    }

    private static void collectJars(File directory, Set<String> entries)
    {
        File[] children = directory == null ? null : directory.listFiles();
        if (children == null)
        {
            return;
        }

        for (int i = 0; i < children.length; ++i)
        {
            File child = children[i];
            if (child.isDirectory())
            {
                collectJars(child, entries);
            }
            else if (child.getName().endsWith(".jar") && !isSkippedJar(child.getName()))
            {
                entries.add(child.getAbsolutePath());
            }
        }
    }

    private static boolean isSkippedJar(String path)
    {
        String name = new File(path).getName();
        return name.startsWith("librarylwjglopenal");
    }

    private static void addModuleOpens(List<String> command)
    {
        String[] opens = new String[] {
            "java.base/java.lang=ALL-UNNAMED",
            "java.base/java.lang.reflect=ALL-UNNAMED",
            "java.base/java.lang.invoke=ALL-UNNAMED",
            "java.base/java.io=ALL-UNNAMED",
            "java.base/java.nio=ALL-UNNAMED",
            "java.base/java.util=ALL-UNNAMED",
            "java.base/java.util.concurrent=ALL-UNNAMED",
            "java.base/java.net=ALL-UNNAMED",
            "java.base/sun.nio.ch=ALL-UNNAMED",
            "java.base/jdk.internal.misc=ALL-UNNAMED",
            "java.desktop/java.awt=ALL-UNNAMED",
            "java.desktop/sun.awt=ALL-UNNAMED",
            "java.desktop/sun.java2d=ALL-UNNAMED"
        };

        for (int i = 0; i < opens.length; ++i)
        {
            command.add("--add-opens");
            command.add(opens[i]);
        }

        command.add("--add-exports");
        command.add("java.base/jdk.internal.misc=ALL-UNNAMED");
    }

    private static boolean isModulePatchApplied()
    {
        return "true".equalsIgnoreCase(System.getProperty(MODULES_OPENED_PROPERTY));
    }

    private static boolean isDebuggerAttached()
    {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < inputArguments.size(); ++i)
        {
            String argument = inputArguments.get(i);
            if (argument.startsWith("-agentlib:jdwp") || argument.startsWith("-Xrunjdwp"))
            {
                return true;
            }
        }
        return false;
    }

    private static int getJavaMajorVersion()
    {
        String specification = System.getProperty("java.specification.version", "1.8");
        try
        {
            if (specification.startsWith("1."))
            {
                return Integer.parseInt(specification.substring(2));
            }
            int dot = specification.indexOf('.');
            return Integer.parseInt(dot < 0 ? specification : specification.substring(0, dot));
        }
        catch (NumberFormatException ignored)
        {
            return MIN_SUPPORTED_JAVA;
        }
    }

    private static String getJavaExecutable()
    {
        String javaHome = System.getProperty("java.home");
        boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        File binary = new File(new File(javaHome, "bin"), windows ? "java.exe" : "java");
        return binary.isFile() ? binary.getAbsolutePath() : "java";
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
