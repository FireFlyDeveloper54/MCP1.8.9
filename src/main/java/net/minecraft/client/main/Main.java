package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class Main
{
    public static void main(String[] args)
    {
        System.setProperty("java.net.preferIPv4Stack", "true");
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpec<String> serverSpec = optionParser.accepts("server").withRequiredArg();
        OptionSpec<Integer> portSpec = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.valueOf(25565), new Integer[0]);
        OptionSpec<File> gameDirSpec = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), new File[0]);
        OptionSpec<File> assetsDirSpec = optionParser.accepts("assetsDir").withRequiredArg().<File>ofType(File.class);
        OptionSpec<File> resourcePackDirSpec = optionParser.accepts("resourcePackDir").withRequiredArg().<File>ofType(File.class);
        OptionSpec<String> proxyHostSpec = optionParser.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> proxyPortSpec = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", new String[0]).<Integer>ofType(Integer.class);
        OptionSpec<String> proxyUserSpec = optionParser.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> proxyPassSpec = optionParser.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> usernameSpec = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + Minecraft.getSystemTime() % 1000L, new String[0]);
        OptionSpec<String> uuidSpec = optionParser.accepts("uuid").withRequiredArg();
        OptionSpec<String> accessTokenSpec = optionParser.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> versionSpec = optionParser.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> widthSpec = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.valueOf(854), new Integer[0]);
        OptionSpec<Integer> heightSpec = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.valueOf(480), new Integer[0]);
        OptionSpec<String> userPropertiesSpec = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}", new String[0]);
        OptionSpec<String> profilePropertiesSpec = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}", new String[0]);
        OptionSpec<String> assetIndexSpec = optionParser.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> userTypeSpec = optionParser.accepts("userType").withRequiredArg().defaultsTo("legacy", new String[0]);
        OptionSpec<String> nonOptionArgsSpec = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(args);
        List<String> ignoredArgs = optionSet.valuesOf(nonOptionArgsSpec);

        if (!ignoredArgs.isEmpty())
        {
            System.out.println("Completely ignored arguments: " + ignoredArgs);
        }

        String proxyHost = (String)optionSet.valueOf(proxyHostSpec);
        Proxy proxy = Proxy.NO_PROXY;

        if (proxyHost != null)
        {
            try
            {
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(proxyHost, ((Integer)optionSet.valueOf(proxyPortSpec)).intValue()));
            }
            catch (Exception caughtException)
            {
                ;
            }
        }

        final String proxyUser = (String)optionSet.valueOf(proxyUserSpec);
        final String proxyPass = (String)optionSet.valueOf(proxyPassSpec);

        if (!proxy.equals(Proxy.NO_PROXY) && isNotNullOrEmpty(proxyUser) && isNotNullOrEmpty(proxyPass))
        {
            Authenticator.setDefault(new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                }
            });
        }

        int width = ((Integer)optionSet.valueOf(widthSpec)).intValue();
        int height = ((Integer)optionSet.valueOf(heightSpec)).intValue();
        boolean fullscreen = optionSet.has("fullscreen");
        boolean checkGlErrors = optionSet.has("checkGlErrors");
        boolean demoMode = optionSet.has("demo");
        String version = (String)optionSet.valueOf(versionSpec);
        Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap userProperties = (PropertyMap)gson.fromJson((String)optionSet.valueOf(userPropertiesSpec), PropertyMap.class);
        PropertyMap profileProperties = (PropertyMap)gson.fromJson((String)optionSet.valueOf(profilePropertiesSpec), PropertyMap.class);
        File gameDir = (File)optionSet.valueOf(gameDirSpec);
        File assetsDir = optionSet.has(assetsDirSpec) ? (File)optionSet.valueOf(assetsDirSpec) : new File(gameDir, "assets/");
        File resourcePacksDir = optionSet.has(resourcePackDirSpec) ? (File)optionSet.valueOf(resourcePackDirSpec) : new File(gameDir, "resourcepacks/");
        String uuid = optionSet.has(uuidSpec) ? (String)uuidSpec.value(optionSet) : (String)usernameSpec.value(optionSet);
        String assetIndex = optionSet.has(assetIndexSpec) ? (String)assetIndexSpec.value(optionSet) : null;
        String serverName = (String)optionSet.valueOf(serverSpec);
        Integer serverPort = (Integer)optionSet.valueOf(portSpec);
        Session session = new Session((String)usernameSpec.value(optionSet), uuid, (String)accessTokenSpec.value(optionSet), (String)userTypeSpec.value(optionSet));
        GameConfiguration gameConfiguration = new GameConfiguration(new GameConfiguration.UserInformation(session, userProperties, profileProperties, proxy), new GameConfiguration.DisplayInformation(width, height, fullscreen, checkGlErrors), new GameConfiguration.FolderInformation(gameDir, resourcePacksDir, assetsDir, assetIndex), new GameConfiguration.GameInformation(demoMode, version), new GameConfiguration.ServerInformation(serverName, serverPort.intValue()));
        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread")
        {
            public void run()
            {
                Minecraft.stopIntegratedServer();
            }
        });
        Thread.currentThread().setName("Client thread");
        (new Minecraft(gameConfiguration)).run();
    }

    private static boolean isNotNullOrEmpty(String str)
    {
        return str != null && !str.isEmpty();
    }
}
