package net.minecraft.src;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.optifine.DynamicLights;
import net.optifine.GlErrors;
import net.optifine.config.GlVersion;
import net.optifine.gui.GuiMessage;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import net.optifine.util.DisplayModeComparator;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.TextureUtils;
import net.optifine.util.TimedEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.GlUtil;
import net.minecraft.client.GameWindow;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;

public class Config
{
    public static final String OF_NAME = "OptiFine";
    public static final String MC_VERSION = "1.8.9";
    public static final String OF_EDITION = "HD_U";
    public static final String OF_RELEASE = "M6_pre2";
    public static final String VERSION = "OptiFine_1.8.9_HD_U_M6_pre2";
    private static String build = null;
    private static String newRelease = null;
    private static boolean notify64BitJava = false;
    public static String openGlVersion = null;
    public static String openGlRenderer = null;
    public static String openGlVendor = null;
    public static String[] openGlExtensions = null;
    public static GlVersion glVersion = null;
    public static GlVersion glslVersion = null;
    public static int minecraftVersionInt = -1;
    public static boolean fancyFogAvailable = false;
    public static boolean occlusionAvailable = false;
    private static GameSettings gameSettings = null;
    private static Minecraft minecraft = Minecraft.getMinecraft();
    private static boolean initialized = false;
    private static Thread minecraftThread = null;
    private static GameWindow.VideoMode desktopDisplayMode = null;
    private static GameWindow.VideoMode[] displayModes = null;
    private static int antialiasingLevel = 0;
    private static int availableProcessors = 0;
    public static boolean zoomMode = false;
    public static boolean zoomSmoothCamera = false;
    private static int texturePackClouds = 0;
    public static boolean waterOpacityChanged = false;
    private static boolean fullscreenModeChecked = false;
    private static boolean desktopModeChecked = false;
    private static DefaultResourcePack defaultResourcePackLazy = null;
    public static final Float DEF_ALPHA_FUNC_LEVEL = Float.valueOf(0.1F);
    private static final Logger LOGGER = LogManager.getLogger();
    public static final boolean logDetail = System.getProperty("log.detail", "false").equals("true");
    private static String mcDebugLast = null;
    private static int fpsMinLast = 0;
    public static float renderPartialTicks;

    public static String getVersion()
    {
        return "OptiFine_1.8.9_HD_U_M6_pre2";
    }

    public static String getVersionDebug()
    {
        StringBuffer stringBuffer = new StringBuffer(32);

        if (isDynamicLights())
        {
            stringBuffer.append("DL: ");
            stringBuffer.append(String.valueOf(DynamicLights.getCount()));
            stringBuffer.append(", ");
        }

        stringBuffer.append("OptiFine_1.8.9_HD_U_M6_pre2");
        String s = Shaders.getShaderPackName();

        if (s != null)
        {
            stringBuffer.append(", ");
            stringBuffer.append(s);
        }

        return stringBuffer.toString();
    }

    public static void initGameSettings(GameSettings settings)
    {
        if (gameSettings == null)
        {
            gameSettings = settings;
            desktopDisplayMode = GameWindow.getDesktopMode();
            updateAvailableProcessors();
        }
    }

    public static void initDisplay()
    {
        checkInitialized();
        antialiasingLevel = gameSettings.ofAaLevel;
        checkDisplaySettings();
        checkDisplayMode();
        minecraftThread = Thread.currentThread();
        updateThreadPriorities();
        Shaders.startup(Minecraft.getMinecraft());
    }

    public static void checkInitialized()
    {
        if (!initialized)
        {
            if (GameWindow.isCreated())
            {
                initialized = true;
                checkOpenGlCaps();
                startVersionCheckThread();
            }
        }
    }

    private static void checkOpenGlCaps()
    {
        log("");
        log(getVersion());
        log("Build: " + getBuild());
        log("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        log("Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        log("VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        log("LWJGL: " + GlUtil.getVersion());
        openGlVersion = GL11.glGetString(GL11.GL_VERSION);
        openGlRenderer = GL11.glGetString(GL11.GL_RENDERER);
        openGlVendor = GL11.glGetString(GL11.GL_VENDOR);
        log("OpenGL: " + openGlRenderer + ", version " + openGlVersion + ", " + openGlVendor);
        log("OpenGL Version: " + getOpenGlVersionString());

        if (!GL.getCapabilities().OpenGL12)
        {
            log("OpenGL Mipmap levels: Not available (GL12.GL_TEXTURE_MAX_LEVEL)");
        }

        fancyFogAvailable = GL.getCapabilities().GL_NV_fog_distance;

        if (!fancyFogAvailable)
        {
            log("OpenGL Fancy fog: Not available (GL_NV_fog_distance)");
        }

        occlusionAvailable = GL.getCapabilities().GL_ARB_occlusion_query;

        if (!occlusionAvailable)
        {
            log("OpenGL Occlussion culling: Not available (GL_ARB_occlusion_query)");
        }

        int i = TextureUtils.getGLMaximumTextureSize();
        dbg("Maximum texture size: " + i + "x" + i);
    }

    public static String getBuild()
    {
        if (build == null)
        {
            try
            {
                InputStream inputStream = Config.class.getResourceAsStream("/buildof.txt");

                if (inputStream == null)
                {
                    return null;
                }

                build = readLines(inputStream)[0];
            }
            catch (Exception exception)
            {
                warn("" + exception.getClass().getName() + ": " + exception.getMessage());
                build = "";
            }
        }

        return build;
    }

    public static boolean isFancyFogAvailable()
    {
        return fancyFogAvailable;
    }

    public static boolean isOcclusionAvailable()
    {
        return occlusionAvailable;
    }

    public static int getMinecraftVersionInt()
    {
        if (minecraftVersionInt < 0)
        {
            String[] astring = tokenize("1.8.9", ".");
            int i = 0;

            if (astring.length > 0)
            {
                i += 10000 * parseInt(astring[0], 0);
            }

            if (astring.length > 1)
            {
                i += 100 * parseInt(astring[1], 0);
            }

            if (astring.length > 2)
            {
                i += 1 * parseInt(astring[2], 0);
            }

            minecraftVersionInt = i;
        }

        return minecraftVersionInt;
    }

    public static String getOpenGlVersionString()
    {
        GlVersion glVersion = getGlVersion();
        String s = "" + glVersion.getMajor() + "." + glVersion.getMinor() + "." + glVersion.getRelease();
        return s;
    }

    private static GlVersion getGlVersionLwjgl()
    {
        return GL.getCapabilities().OpenGL44 ? new GlVersion(4, 4) : (GL.getCapabilities().OpenGL43 ? new GlVersion(4, 3) : (GL.getCapabilities().OpenGL42 ? new GlVersion(4, 2) : (GL.getCapabilities().OpenGL41 ? new GlVersion(4, 1) : (GL.getCapabilities().OpenGL40 ? new GlVersion(4, 0) : (GL.getCapabilities().OpenGL33 ? new GlVersion(3, 3) : (GL.getCapabilities().OpenGL32 ? new GlVersion(3, 2) : (GL.getCapabilities().OpenGL31 ? new GlVersion(3, 1) : (GL.getCapabilities().OpenGL30 ? new GlVersion(3, 0) : (GL.getCapabilities().OpenGL21 ? new GlVersion(2, 1) : (GL.getCapabilities().OpenGL20 ? new GlVersion(2, 0) : (GL.getCapabilities().OpenGL15 ? new GlVersion(1, 5) : (GL.getCapabilities().OpenGL14 ? new GlVersion(1, 4) : (GL.getCapabilities().OpenGL13 ? new GlVersion(1, 3) : (GL.getCapabilities().OpenGL12 ? new GlVersion(1, 2) : (GL.getCapabilities().OpenGL11 ? new GlVersion(1, 1) : new GlVersion(1, 0))))))))))))))));
    }

    public static GlVersion getGlVersion()
    {
        if (glVersion == null)
        {
            String s = GL11.glGetString(GL11.GL_VERSION);
            glVersion = parseGlVersion(s, (GlVersion)null);

            if (glVersion == null)
            {
                glVersion = getGlVersionLwjgl();
            }

            if (glVersion == null)
            {
                glVersion = new GlVersion(1, 0);
            }
        }

        return glVersion;
    }

    public static GlVersion getGlslVersion()
    {
        if (glslVersion == null)
        {
            String s = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
            glslVersion = parseGlVersion(s, (GlVersion)null);

            if (glslVersion == null)
            {
                glslVersion = new GlVersion(1, 10);
            }
        }

        return glslVersion;
    }

    public static GlVersion parseGlVersion(String versionString, GlVersion defaultVersion)
    {
        try
        {
            if (versionString == null)
            {
                return defaultVersion;
            }
            else
            {
                Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)(\\.([0-9]+))?(.+)?");
                Matcher matcher = pattern.matcher(versionString);

                if (!matcher.matches())
                {
                    return defaultVersion;
                }
                else
                {
                    int i = Integer.parseInt(matcher.group(1));
                    int j = Integer.parseInt(matcher.group(2));
                    int k = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
                    String s = matcher.group(5);
                    return new GlVersion(i, j, k, s);
                }
            }
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            return defaultVersion;
        }
    }

    public static String[] getOpenGlExtensions()
    {
        if (openGlExtensions == null)
        {
            openGlExtensions = detectOpenGlExtensions();
        }

        return openGlExtensions;
    }

    private static String[] detectOpenGlExtensions()
    {
        try
        {
            GlVersion glversion = getGlVersion();

            if (glversion.getMajor() >= 3)
            {
                int i = GL11.glGetInteger(33309);

                if (i > 0)
                {
                    String[] astring = new String[i];

                    for (int j = 0; j < i; ++j)
                    {
                        astring[j] = GL30.glGetStringi(7939, j);
                    }

                    return astring;
                }
            }
        }
        catch (Exception exception1)
        {
            net.minecraft.src.Config.warn(exception1.getClass().getName() + ": " + exception1.getMessage(), exception1);
        }

        try
        {
            String s = GL11.glGetString(GL11.GL_EXTENSIONS);
            String[] astring1 = s.split(" ");
            return astring1;
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            return new String[0];
        }
    }

    public static void updateThreadPriorities()
    {
        updateAvailableProcessors();
        int i = 8;

        if (isSingleProcessor())
        {
            if (isSmoothWorld())
            {
                minecraftThread.setPriority(10);
                setThreadPriority("Server thread", 1);
            }
            else
            {
                minecraftThread.setPriority(5);
                setThreadPriority("Server thread", 5);
            }
        }
        else
        {
            minecraftThread.setPriority(10);
            setThreadPriority("Server thread", 5);
        }
    }

    private static void setThreadPriority(String threadNamePrefix, int priority)
    {
        try
        {
            ThreadGroup threadgroup = Thread.currentThread().getThreadGroup();

            if (threadgroup == null)
            {
                return;
            }

            int i = (threadgroup.activeCount() + 10) * 2;
            Thread[] athread = new Thread[i];
            threadgroup.enumerate(athread, false);

            for (int j = 0; j < athread.length; ++j)
            {
                Thread thread = athread[j];

                if (thread != null && thread.getName().startsWith(threadNamePrefix))
                {
                    thread.setPriority(priority);
                }
            }
        }
        catch (Throwable throwable)
        {
            warn(throwable.getClass().getName() + ": " + throwable.getMessage());
        }
    }

    public static boolean isMinecraftThread()
    {
        return Thread.currentThread() == minecraftThread;
    }

    private static void startVersionCheckThread()
    {
        // OptiFine online version checks are disabled for this local MCP project.
    }

    public static boolean isMipmaps()
    {
        return gameSettings.mipmapLevels > 0;
    }

    public static int getMipmapLevels()
    {
        return gameSettings.mipmapLevels;
    }

    public static int getMipmapType()
    {
        switch (gameSettings.ofMipmapType)
        {
            case 0:
                return 9986;

            case 1:
                return 9986;

            case 2:
                if (isMultiTexture())
                {
                    return 9985;
                }

                return 9986;

            case 3:
                if (isMultiTexture())
                {
                    return 9987;
                }

                return 9986;

            default:
                return 9986;
        }
    }

    public static boolean isUseAlphaFunc()
    {
        float f = getAlphaFuncLevel();
        return f > DEF_ALPHA_FUNC_LEVEL.floatValue() + 1.0E-5F;
    }

    public static float getAlphaFuncLevel()
    {
        return DEF_ALPHA_FUNC_LEVEL.floatValue();
    }

    public static boolean isFogFancy()
    {
        return !isFancyFogAvailable() ? false : gameSettings.ofFogType == 2;
    }

    public static boolean isFogFast()
    {
        return gameSettings.ofFogType == 1;
    }

    public static boolean isFogOff()
    {
        return gameSettings.ofFogType == 3;
    }

    public static boolean isFogOn()
    {
        return gameSettings.ofFogType != 3;
    }

    public static float getFogStart()
    {
        return gameSettings.ofFogStart;
    }

    public static void detail(String message)
    {
        if (logDetail)
        {
            LOGGER.info("[OptiFine] " + message);
        }
    }

    public static void dbg(String message)
    {
        LOGGER.info("[OptiFine] " + message);
    }

    public static void warn(String message)
    {
        LOGGER.warn("[OptiFine] " + message);
    }

    public static void warn(String message, Throwable throwable)
    {
        LOGGER.warn("[OptiFine] " + message, throwable);
    }

    public static void error(String message)
    {
        LOGGER.error("[OptiFine] " + message);
    }

    public static void error(String message, Throwable throwable)
    {
        LOGGER.error("[OptiFine] " + message, throwable);
    }

    public static void log(String message)
    {
        dbg(message);
    }

    public static int getUpdatesPerFrame()
    {
        return gameSettings.ofChunkUpdates;
    }

    public static boolean isDynamicUpdates()
    {
        return gameSettings.ofChunkUpdatesDynamic;
    }

    public static boolean isRainFancy()
    {
        return gameSettings.ofRain == 0 ? gameSettings.fancyGraphics : gameSettings.ofRain == 2;
    }

    public static boolean isRainOff()
    {
        return gameSettings.ofRain == 3;
    }

    public static boolean isCloudsFancy()
    {
        return gameSettings.ofClouds != 0 ? gameSettings.ofClouds == 2 : (isShaders() && !Shaders.shaderPackClouds.isDefault() ? Shaders.shaderPackClouds.isFancy() : (texturePackClouds != 0 ? texturePackClouds == 2 : gameSettings.fancyGraphics));
    }

    public static boolean isCloudsOff()
    {
        return gameSettings.ofClouds != 0 ? gameSettings.ofClouds == 3 : (isShaders() && !Shaders.shaderPackClouds.isDefault() ? Shaders.shaderPackClouds.isOff() : (texturePackClouds != 0 ? texturePackClouds == 3 : false));
    }

    public static void updateTexturePackClouds()
    {
        texturePackClouds = 0;
        IResourceManager iresourcemanager = getResourceManager();

        if (iresourcemanager != null)
        {
            try
            {
                InputStream inputStream = iresourcemanager.getResource(new ResourceLocation("mcpatcher/color.properties")).getInputStream();

                if (inputStream == null)
                {
                    return;
                }

                Properties properties = new PropertiesOrdered();
                properties.load(inputStream);
                inputStream.close();
                String s = properties.getProperty("clouds");

                if (s == null)
                {
                    return;
                }

                dbg("Texture pack clouds: " + s);
                s = s.toLowerCase();

                if (s.equals("fast"))
                {
                    texturePackClouds = 1;
                }

                if (s.equals("fancy"))
                {
                    texturePackClouds = 2;
                }

                if (s.equals("off"))
                {
                    texturePackClouds = 3;
                }
            }
            catch (Exception caughtException)
            {
                ;
            }
        }
    }

    public static ModelManager getModelManager()
    {
        return minecraft.getRenderItem().modelManager;
    }

    public static boolean isTreesFancy()
    {
        return gameSettings.ofTrees == 0 ? gameSettings.fancyGraphics : gameSettings.ofTrees != 1;
    }

    public static boolean isTreesSmart()
    {
        return gameSettings.ofTrees == 4;
    }

    public static boolean isCullFacesLeaves()
    {
        return gameSettings.ofTrees == 0 ? !gameSettings.fancyGraphics : gameSettings.ofTrees == 4;
    }

    public static boolean isDroppedItemsFancy()
    {
        return gameSettings.ofDroppedItems == 0 ? gameSettings.fancyGraphics : gameSettings.ofDroppedItems == 2;
    }

    public static int limit(int value, int min, int max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    public static float limit(float value, float min, float max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    public static double limit(double value, double min, double max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    public static float limitTo1(float value)
    {
        return value < 0.0F ? 0.0F : (value > 1.0F ? 1.0F : value);
    }

    public static boolean isAnimatedWater()
    {
        return gameSettings.ofAnimatedWater != 2;
    }

    public static boolean isGeneratedWater()
    {
        return gameSettings.ofAnimatedWater == 1;
    }

    public static boolean isAnimatedPortal()
    {
        return gameSettings.ofAnimatedPortal;
    }

    public static boolean isAnimatedLava()
    {
        return gameSettings.ofAnimatedLava != 2;
    }

    public static boolean isGeneratedLava()
    {
        return gameSettings.ofAnimatedLava == 1;
    }

    public static boolean isAnimatedFire()
    {
        return gameSettings.ofAnimatedFire;
    }

    public static boolean isAnimatedRedstone()
    {
        return gameSettings.ofAnimatedRedstone;
    }

    public static boolean isAnimatedExplosion()
    {
        return gameSettings.ofAnimatedExplosion;
    }

    public static boolean isAnimatedFlame()
    {
        return gameSettings.ofAnimatedFlame;
    }

    public static boolean isAnimatedSmoke()
    {
        return gameSettings.ofAnimatedSmoke;
    }

    public static boolean isVoidParticles()
    {
        return gameSettings.ofVoidParticles;
    }

    public static boolean isWaterParticles()
    {
        return gameSettings.ofWaterParticles;
    }

    public static boolean isRainSplash()
    {
        return gameSettings.ofRainSplash;
    }

    public static boolean isPortalParticles()
    {
        return gameSettings.ofPortalParticles;
    }

    public static boolean isPotionParticles()
    {
        return gameSettings.ofPotionParticles;
    }

    public static boolean isFireworkParticles()
    {
        return gameSettings.ofFireworkParticles;
    }

    public static float getAmbientOcclusionLevel()
    {
        return isShaders() && Shaders.aoLevel >= 0.0F ? Shaders.aoLevel : gameSettings.ofAoLevel;
    }

    public static String listToString(List list)
    {
        return listToString(list, ", ");
    }

    public static String listToString(List list, String separator)
    {
        if (list == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(list.size() * 5);

            for (int i = 0; i < list.size(); ++i)
            {
                Object object = list.get(i);

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(object));
            }

            return stringBuffer.toString();
        }
    }

    public static String arrayToString(Object[] array)
    {
        return arrayToString(array, ", ");
    }

    public static String arrayToString(Object[] array, String separator)
    {
        if (array == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(array.length * 5);

            for (int i = 0; i < array.length; ++i)
            {
                Object object = array[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(object));
            }

            return stringBuffer.toString();
        }
    }

    public static String arrayToString(int[] array)
    {
        return arrayToString(array, ", ");
    }

    public static String arrayToString(int[] array, String separator)
    {
        if (array == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(array.length * 5);

            for (int i = 0; i < array.length; ++i)
            {
                int j = array[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(j));
            }

            return stringBuffer.toString();
        }
    }

    public static String arrayToString(float[] array)
    {
        return arrayToString(array, ", ");
    }

    public static String arrayToString(float[] array, String separator)
    {
        if (array == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(array.length * 5);

            for (int i = 0; i < array.length; ++i)
            {
                float f = array[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(f));
            }

            return stringBuffer.toString();
        }
    }

    public static Minecraft getMinecraft()
    {
        return minecraft;
    }

    public static TextureManager getTextureManager()
    {
        return minecraft.getTextureManager();
    }

    public static IResourceManager getResourceManager()
    {
        return minecraft.getResourceManager();
    }

    public static InputStream getResourceStream(ResourceLocation resourceLocation) throws IOException
    {
        return getResourceStream(minecraft.getResourceManager(), resourceLocation);
    }

    public static InputStream getResourceStream(IResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException
    {
        IResource iresource = resourceManager.getResource(resourceLocation);
        return iresource == null ? null : iresource.getInputStream();
    }

    public static IResource getResource(ResourceLocation resourceLocation) throws IOException
    {
        return minecraft.getResourceManager().getResource(resourceLocation);
    }

    public static boolean hasResource(ResourceLocation resourceLocation)
    {
        if (resourceLocation == null)
        {
            return false;
        }
        else
        {
            IResourcePack iresourcepack = getDefiningResourcePack(resourceLocation);
            return iresourcepack != null;
        }
    }

    public static boolean hasResource(IResourceManager resourceManager, ResourceLocation resourceLocation)
    {
        try
        {
            IResource iresource = resourceManager.getResource(resourceLocation);
            return iresource != null;
        }
        catch (IOException caughtIoException)
        {
            return false;
        }
    }

    public static IResourcePack[] getResourcePacks()
    {
        ResourcePackRepository resourcePackRepository = minecraft.getResourcePackRepository();
        List list = resourcePackRepository.getRepositoryEntries();
        List list1 = new ArrayList();

        for (Object o: list)
        {
            ResourcePackRepository.Entry resourcepackrepository$entry = (ResourcePackRepository.Entry) o;
            list1.add(resourcepackrepository$entry.getResourcePack());
        }

        if (resourcePackRepository.getResourcePackInstance() != null)
        {
            list1.add(resourcePackRepository.getResourcePackInstance());
        }

        IResourcePack[] airesourcepack = (IResourcePack[])((IResourcePack[])list1.toArray(new IResourcePack[list1.size()]));
        return airesourcepack;
    }

    public static String getResourcePackNames()
    {
        if (minecraft.getResourcePackRepository() == null)
        {
            return "";
        }
        else
        {
            IResourcePack[] airesourcepack = getResourcePacks();

            if (airesourcepack.length <= 0)
            {
                return getDefaultResourcePack().getPackName();
            }
            else
            {
                String[] astring = new String[airesourcepack.length];

                for (int i = 0; i < airesourcepack.length; ++i)
                {
                    astring[i] = airesourcepack[i].getPackName();
                }

                String s = arrayToString((Object[])astring);
                return s;
            }
        }
    }

    public static DefaultResourcePack getDefaultResourcePack()
    {
        if (defaultResourcePackLazy == null)
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            defaultResourcePackLazy = (DefaultResourcePack)Reflector.getFieldValue(minecraft, Reflector.Minecraft_defaultResourcePack);

            if (defaultResourcePackLazy == null)
            {
                ResourcePackRepository resourcePackRepository = minecraft.getResourcePackRepository();

                if (resourcePackRepository != null)
                {
                    defaultResourcePackLazy = (DefaultResourcePack)resourcePackRepository.rprDefaultResourcePack;
                }
            }
        }

        return defaultResourcePackLazy;
    }

    public static boolean isFromDefaultResourcePack(ResourceLocation resourceLocation)
    {
        IResourcePack iresourcepack = getDefiningResourcePack(resourceLocation);
        return iresourcepack == getDefaultResourcePack();
    }

    public static IResourcePack getDefiningResourcePack(ResourceLocation resourceLocation)
    {
        ResourcePackRepository resourcePackRepository = minecraft.getResourcePackRepository();
        IResourcePack iresourcepack = resourcePackRepository.getResourcePackInstance();

        if (iresourcepack != null && iresourcepack.resourceExists(resourceLocation))
        {
            return iresourcepack;
        }
        else
        {
            List<ResourcePackRepository.Entry> list = resourcePackRepository.repositoryEntries;

            for (int i = list.size() - 1; i >= 0; --i)
            {
                ResourcePackRepository.Entry resourcepackrepository$entry = (ResourcePackRepository.Entry)list.get(i);
                IResourcePack iresourcepack1 = resourcepackrepository$entry.getResourcePack();

                if (iresourcepack1.resourceExists(resourceLocation))
                {
                    return iresourcepack1;
                }
            }

            if (getDefaultResourcePack().resourceExists(resourceLocation))
            {
                return getDefaultResourcePack();
            }
            else
            {
                return null;
            }
        }
    }

    public static RenderGlobal getRenderGlobal()
    {
        return minecraft.renderGlobal;
    }

    public static boolean isBetterGrass()
    {
        return gameSettings.ofBetterGrass != 3;
    }

    public static boolean isBetterGrassFancy()
    {
        return gameSettings.ofBetterGrass == 2;
    }

    public static boolean isWeatherEnabled()
    {
        return gameSettings.ofWeather;
    }

    public static boolean isSkyEnabled()
    {
        return gameSettings.ofSky;
    }

    public static boolean isSunMoonEnabled()
    {
        return gameSettings.ofSunMoon;
    }

    public static boolean isSunTexture()
    {
        return !isSunMoonEnabled() ? false : !isShaders() || Shaders.isSun();
    }

    public static boolean isMoonTexture()
    {
        return !isSunMoonEnabled() ? false : !isShaders() || Shaders.isMoon();
    }

    public static boolean isVignetteEnabled()
    {
        return isShaders() && !Shaders.isVignette() ? false : (gameSettings.ofVignette == 0 ? gameSettings.fancyGraphics : gameSettings.ofVignette == 2);
    }

    public static boolean isStarsEnabled()
    {
        return gameSettings.ofStars;
    }

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException interruptedException)
        {
            net.minecraft.src.Config.warn(interruptedException.getClass().getName() + ": " + interruptedException.getMessage(), interruptedException);
        }
    }

    public static boolean isTimeDayOnly()
    {
        return gameSettings.ofTime == 1;
    }

    public static boolean isTimeDefault()
    {
        return gameSettings.ofTime == 0;
    }

    public static boolean isTimeNightOnly()
    {
        return gameSettings.ofTime == 2;
    }

    public static boolean isClearWater()
    {
        return gameSettings.ofClearWater;
    }

    public static int getAnisotropicFilterLevel()
    {
        return gameSettings.ofAfLevel;
    }

    public static boolean isAnisotropicFiltering()
    {
        return getAnisotropicFilterLevel() > 1;
    }

    public static int getAntialiasingLevel()
    {
        return antialiasingLevel;
    }

    public static boolean isAntialiasing()
    {
        return getAntialiasingLevel() > 0;
    }

    public static boolean isAntialiasingConfigured()
    {
        return getGameSettings().ofAaLevel > 0;
    }

    public static boolean isMultiTexture()
    {
        return getAnisotropicFilterLevel() > 1 ? true : getAntialiasingLevel() > 0;
    }

    public static boolean between(int value, int min, int max)
    {
        return value >= min && value <= max;
    }

    public static boolean between(float value, float min, float max)
    {
        return value >= min && value <= max;
    }

    public static boolean isDrippingWaterLava()
    {
        return gameSettings.ofDrippingWaterLava;
    }

    public static boolean isBetterSnow()
    {
        return gameSettings.ofBetterSnow;
    }

    public static Dimension getFullscreenDimension()
    {
        if (desktopDisplayMode == null)
        {
            return null;
        }
        else if (gameSettings == null)
        {
            return new Dimension(desktopDisplayMode.getWidth(), desktopDisplayMode.getHeight());
        }
        else
        {
            String s = gameSettings.ofFullscreenMode;

            if (s.equals("Default"))
            {
                return new Dimension(desktopDisplayMode.getWidth(), desktopDisplayMode.getHeight());
            }
            else
            {
                String[] astring = tokenize(s, " x");
                return astring.length < 2 ? new Dimension(desktopDisplayMode.getWidth(), desktopDisplayMode.getHeight()) : new Dimension(parseInt(astring[0], -1), parseInt(astring[1], -1));
            }
        }
    }

    public static int parseInt(String valueString, int defaultValue)
    {
        try
        {
            if (valueString == null)
            {
                return defaultValue;
            }
            else
            {
                valueString = valueString.trim();
                return Integer.parseInt(valueString);
            }
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            return defaultValue;
        }
    }

    public static float parseFloat(String valueString, float defaultValue)
    {
        try
        {
            if (valueString == null)
            {
                return defaultValue;
            }
            else
            {
                valueString = valueString.trim();
                return Float.parseFloat(valueString);
            }
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            return defaultValue;
        }
    }

    public static boolean parseBoolean(String valueString, boolean defaultValue)
    {
        try
        {
            if (valueString == null)
            {
                return defaultValue;
            }
            else
            {
                valueString = valueString.trim();
                return Boolean.parseBoolean(valueString);
            }
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            return defaultValue;
        }
    }

    public static Boolean parseBoolean(String valueString, Boolean defaultValue)
    {
        try
        {
            if (valueString == null)
            {
                return defaultValue;
            }
            else
            {
                valueString = valueString.trim().toLowerCase();
                return valueString.equals("true") ? Boolean.TRUE : (valueString.equals("false") ? Boolean.FALSE : defaultValue);
            }
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            return defaultValue;
        }
    }

    public static String[] tokenize(String text, String delimiters)
    {
        StringTokenizer stringTokenizer = new StringTokenizer(text, delimiters);
        List list = new ArrayList();

        while (stringTokenizer.hasMoreTokens())
        {
            String s = stringTokenizer.nextToken();
            list.add(s);
        }

        String[] astring = (String[])((String[])list.toArray(new String[list.size()]));
        return astring;
    }

    public static GameWindow.VideoMode getDesktopDisplayMode()
    {
        return desktopDisplayMode;
    }

    public static GameWindow.VideoMode[] getDisplayModes()
    {
        if (displayModes == null)
        {
            try
            {
                GameWindow.VideoMode[] adisplaymode = GameWindow.getAvailableModes();
                Set<Dimension> set = getDisplayModeDimensions(adisplaymode);
                List list = new ArrayList();

                for (Dimension dimension : set)
                {
                    GameWindow.VideoMode[] adisplaymode1 = getDisplayModes(adisplaymode, dimension);
                    GameWindow.VideoMode displayMode = getDisplayMode(adisplaymode1, desktopDisplayMode);

                    if (displayMode != null)
                    {
                        list.add(displayMode);
                    }
                }

                GameWindow.VideoMode[] adisplaymode2 = (GameWindow.VideoMode[])((GameWindow.VideoMode[])list.toArray(new GameWindow.VideoMode[list.size()]));
                Arrays.sort(adisplaymode2, new DisplayModeComparator());
                return adisplaymode2;
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
                displayModes = new GameWindow.VideoMode[] {desktopDisplayMode};
            }
        }

        return displayModes;
    }

    public static GameWindow.VideoMode getLargestDisplayMode()
    {
        GameWindow.VideoMode[] adisplaymode = getDisplayModes();

        if (adisplaymode != null && adisplaymode.length >= 1)
        {
            GameWindow.VideoMode displayMode = adisplaymode[adisplaymode.length - 1];
            return desktopDisplayMode.getWidth() > displayMode.getWidth() ? desktopDisplayMode : (desktopDisplayMode.getWidth() == displayMode.getWidth() && desktopDisplayMode.getHeight() > displayMode.getHeight() ? desktopDisplayMode : displayMode);
        }
        else
        {
            return desktopDisplayMode;
        }
    }

    private static Set<Dimension> getDisplayModeDimensions(GameWindow.VideoMode[] displayModes)
    {
        Set<Dimension> set = new HashSet();

        for (int i = 0; i < displayModes.length; ++i)
        {
            GameWindow.VideoMode displayMode = displayModes[i];
            Dimension dimension = new Dimension(displayMode.getWidth(), displayMode.getHeight());
            set.add(dimension);
        }

        return set;
    }

    private static GameWindow.VideoMode[] getDisplayModes(GameWindow.VideoMode[] displayModes, Dimension dimension)
    {
        List list = new ArrayList();

        for (int i = 0; i < displayModes.length; ++i)
        {
            GameWindow.VideoMode displayMode = displayModes[i];

            if ((double)displayMode.getWidth() == dimension.getWidth() && (double)displayMode.getHeight() == dimension.getHeight())
            {
                list.add(displayMode);
            }
        }

        GameWindow.VideoMode[] adisplaymode = (GameWindow.VideoMode[])((GameWindow.VideoMode[])list.toArray(new GameWindow.VideoMode[list.size()]));
        return adisplaymode;
    }

    private static GameWindow.VideoMode getDisplayMode(GameWindow.VideoMode[] displayModes, GameWindow.VideoMode defaultDisplayMode)
    {
        if (defaultDisplayMode != null)
        {
            for (int i = 0; i < displayModes.length; ++i)
            {
                GameWindow.VideoMode displayMode = displayModes[i];

                if (displayMode.getBitsPerPixel() == defaultDisplayMode.getBitsPerPixel() && displayMode.getFrequency() == defaultDisplayMode.getFrequency())
                {
                    return displayMode;
                }
            }
        }

        if (displayModes.length <= 0)
        {
            return null;
        }
        else
        {
            Arrays.sort(displayModes, new DisplayModeComparator());
            return displayModes[displayModes.length - 1];
        }
    }

    public static String[] getDisplayModeNames()
    {
        GameWindow.VideoMode[] adisplaymode = getDisplayModes();
        String[] astring = new String[adisplaymode.length];

        for (int i = 0; i < adisplaymode.length; ++i)
        {
            GameWindow.VideoMode displayMode = adisplaymode[i];
            String s = "" + displayMode.getWidth() + "x" + displayMode.getHeight();
            astring[i] = s;
        }

        return astring;
    }

    public static GameWindow.VideoMode getDisplayMode(Dimension dimension) 
    {
        GameWindow.VideoMode[] adisplaymode = getDisplayModes();

        for (int i = 0; i < adisplaymode.length; ++i)
        {
            GameWindow.VideoMode displaymode = adisplaymode[i];

            if (displaymode.getWidth() == dimension.width && displaymode.getHeight() == dimension.height)
            {
                return displaymode;
            }
        }

        return desktopDisplayMode;
    }

    public static boolean isAnimatedTerrain()
    {
        return gameSettings.ofAnimatedTerrain;
    }

    public static boolean isAnimatedTextures()
    {
        return gameSettings.ofAnimatedTextures;
    }

    public static boolean isSwampColors()
    {
        return gameSettings.ofSwampColors;
    }

    public static boolean isRandomEntities()
    {
        return gameSettings.ofRandomEntities;
    }

    public static void checkGlError(String location)
    {
        int i = GlStateManager.glGetError();

        if (i != 0 && GlErrors.isEnabled(i))
        {
            String s = getGlErrorString(i);
            String stringValue = String.format("OpenGL error: %s (%s), at: %s", new Object[] {Integer.valueOf(i), s, location});
            error(stringValue);

            if (isShowGlErrors() && TimedEvent.isActive("ShowGlError", 10000L))
            {
                String secondStringValue = I18n.format("of.message.openglError", new Object[] {Integer.valueOf(i), s});
                minecraft.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(secondStringValue));
            }
        }
    }

    public static boolean isSmoothBiomes()
    {
        return gameSettings.ofSmoothBiomes;
    }

    public static boolean isCustomColors()
    {
        return gameSettings.ofCustomColors;
    }

    public static boolean isCustomSky()
    {
        return gameSettings.ofCustomSky;
    }

    public static boolean isCustomFonts()
    {
        return gameSettings.ofCustomFonts;
    }

    public static boolean isShowCapes()
    {
        return gameSettings.ofShowCapes;
    }

    public static boolean isConnectedTextures()
    {
        return gameSettings.ofConnectedTextures != 3;
    }

    public static boolean isNaturalTextures()
    {
        return gameSettings.ofNaturalTextures;
    }

    public static boolean isEmissiveTextures()
    {
        return gameSettings.ofEmissiveTextures;
    }

    public static boolean isConnectedTexturesFancy()
    {
        return gameSettings.ofConnectedTextures == 2;
    }

    public static boolean isFastRender()
    {
        return gameSettings.ofFastRender;
    }

    public static boolean isTranslucentBlocksFancy()
    {
        return gameSettings.ofTranslucentBlocks == 0 ? gameSettings.fancyGraphics : gameSettings.ofTranslucentBlocks == 2;
    }

    public static boolean isShaders()
    {
        return Shaders.shaderPackLoaded;
    }

    public static String[] readLines(File source) throws IOException
    {
        FileInputStream fileinputstream = new FileInputStream(source);
        return readLines((InputStream)fileinputstream);
    }

    public static String[] readLines(InputStream source) throws IOException
    {
        List list = new ArrayList();
        InputStreamReader inputstreamreader = new InputStreamReader(source, "ASCII");
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

        while (true)
        {
            String s = bufferedreader.readLine();

            if (s == null)
            {
                String[] astring = (String[])((String[])list.toArray(new String[list.size()]));
                return astring;
            }

            list.add(s);
        }
    }

    public static String readFile(File file) throws IOException
    {
        FileInputStream fileinputstream = new FileInputStream(file);
        return readInputStream(fileinputstream, "ASCII");
    }

    public static String readInputStream(InputStream inputStream) throws IOException
    {
        return readInputStream(inputStream, "ASCII");
    }

    public static String readInputStream(InputStream inputStream, String charset) throws IOException
    {
        InputStreamReader inputstreamreader = new InputStreamReader(inputStream, charset);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        StringBuffer stringbuffer = new StringBuffer();

        while (true)
        {
            String s = bufferedreader.readLine();

            if (s == null)
            {
                return stringbuffer.toString();
            }

            stringbuffer.append(s);
            stringbuffer.append("\n");
        }
    }

    public static byte[] readAll(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] abyte = new byte[1024];

        while (true)
        {
            int i = inputStream.read(abyte);

            if (i < 0)
            {
                inputStream.close();
                byte[] abyte1 = bytearrayoutputstream.toByteArray();
                return abyte1;
            }

            bytearrayoutputstream.write(abyte, 0, i);
        }
    }

    public static GameSettings getGameSettings()
    {
        return gameSettings;
    }

    public static String getNewRelease()
    {
        return newRelease;
    }

    public static void setNewRelease(String release)
    {
        newRelease = release;
    }

    public static int compareRelease(String release1, String release2)
    {
        String[] astring = splitRelease(release1);
        String[] astring1 = splitRelease(release2);
        String s = astring[0];
        String text2 = astring1[0];

        if (!s.equals(text2))
        {
            return s.compareTo(text2);
        }
        else
        {
            int i = parseInt(astring[1], -1);
            int j = parseInt(astring1[1], -1);

            if (i != j)
            {
                return i - j;
            }
            else
            {
                String text3 = astring[2];
                String text4 = astring1[2];

                if (!text3.equals(text4))
                {
                    if (text3.isEmpty())
                    {
                        return 1;
                    }

                    if (text4.isEmpty())
                    {
                        return -1;
                    }
                }

                return text3.compareTo(text4);
            }
        }
    }

    private static String[] splitRelease(String release)
    {
        if (release != null && release.length() > 0)
        {
            Pattern pattern = Pattern.compile("([A-Z])([0-9]+)(.*)");
            Matcher matcher = pattern.matcher(release);

            if (!matcher.matches())
            {
                return new String[] {"", "", ""};
            }
            else
            {
                String s = normalize(matcher.group(1));
                String text2 = normalize(matcher.group(2));
                String text3 = normalize(matcher.group(3));
                return new String[] {s, text2, text3};
            }
        }
        else
        {
            return new String[] {"", "", ""};
        }
    }

    public static int intHash(int value)
    {
        value = value ^ 61 ^ value >> 16;
        value = value + (value << 3);
        value = value ^ value >> 4;
        value = value * 668265261;
        value = value ^ value >> 15;
        return value;
    }

    public static int getRandom(BlockPos pos, int seed)
    {
        int i = intHash(seed + 37);
        i = intHash(i + pos.getX());
        i = intHash(i + pos.getZ());
        i = intHash(i + pos.getY());
        return i;
    }

    public static int getAvailableProcessors()
    {
        return availableProcessors;
    }

    public static void updateAvailableProcessors()
    {
        availableProcessors = Runtime.getRuntime().availableProcessors();
    }

    public static boolean isSingleProcessor()
    {
        return getAvailableProcessors() <= 1;
    }

    public static boolean isSmoothWorld()
    {
        return gameSettings.ofSmoothWorld;
    }

    public static boolean isLazyChunkLoading()
    {
        return gameSettings.ofLazyChunkLoading;
    }

    public static boolean isDynamicFov()
    {
        return gameSettings.ofDynamicFov;
    }

    public static boolean isAlternateBlocks()
    {
        return gameSettings.ofAlternateBlocks;
    }

    public static int getChunkViewDistance()
    {
        if (gameSettings == null)
        {
            return 10;
        }
        else
        {
            int i = gameSettings.renderDistanceChunks;
            return i;
        }
    }

    public static boolean equals(Object left, Object right)
    {
        return left == right ? true : (left == null ? false : left.equals(right));
    }

    public static boolean equalsOne(Object value, Object[] candidates)
    {
        if (candidates == null)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < candidates.length; ++i)
            {
                Object object = candidates[i];

                if (equals(value, object))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean equalsOne(int value, int[] candidates)
    {
        for (int i = 0; i < candidates.length; ++i)
        {
            if (candidates[i] == value)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isSameOne(Object value, Object[] candidates)
    {
        if (candidates == null)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < candidates.length; ++i)
            {
                Object object = candidates[i];

                if (value == object)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static String normalize(String text)
    {
        return text == null ? "" : text;
    }

    public static void checkDisplaySettings()
    {
        int i = getAntialiasingLevel();

        if (i > 0)
        {
            if (minecraft != null && minecraft.getFramebuffer() != null)
            {
                dbg("FSAA Samples: " + i + " (window already initialized, restart to apply)");
                return;
            }

            GameWindow.VideoMode displayMode = GameWindow.getVideoMode();
            dbg("FSAA Samples: " + i);

            try
            {
                GameWindow.destroy();
                GameWindow.setVideoMode(displayMode);
                GameWindow.create(displayMode.getWidth(), displayMode.getHeight(), "Minecraft 1.8.9", 24, i);

                if (Util.getOSType() == Util.EnumOS.WINDOWS)
                {
                    GameWindow.setResizable(false);
                    GameWindow.setResizable(true);
                }
            }
            catch (RuntimeException lwjglexception2)
            {
                warn("Error setting FSAA: " + i + "x");
                net.minecraft.src.Config.warn(lwjglexception2.getClass().getName() + ": " + lwjglexception2.getMessage(), lwjglexception2);

                try
                {
                    GameWindow.setVideoMode(displayMode);
                    GameWindow.create(displayMode.getWidth(), displayMode.getHeight(), "Minecraft 1.8.9", 24, 0);

                    if (Util.getOSType() == Util.EnumOS.WINDOWS)
                    {
                        GameWindow.setResizable(false);
                        GameWindow.setResizable(true);
                    }
                }
                catch (RuntimeException lwjglexception1)
                {
                    net.minecraft.src.Config.warn(lwjglexception1.getClass().getName() + ": " + lwjglexception1.getMessage(), lwjglexception1);

                    try
                    {
                        GameWindow.setVideoMode(displayMode);
                        GameWindow.create(displayMode.getWidth(), displayMode.getHeight(), "Minecraft 1.8.9", 0, 0);

                        if (Util.getOSType() == Util.EnumOS.WINDOWS)
                        {
                            GameWindow.setResizable(false);
                            GameWindow.setResizable(true);
                        }
                    }
                    catch (RuntimeException lwjglexception)
                    {
                        net.minecraft.src.Config.warn(lwjglexception.getClass().getName() + ": " + lwjglexception.getMessage(), lwjglexception);
                    }
                }
            }

            if (!Minecraft.isRunningOnMac && getDefaultResourcePack() != null)
            {
                InputStream inputStream = null;
                InputStream inputstream1 = null;

                try
                {
                    inputStream = getDefaultResourcePack().getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
                    inputstream1 = getDefaultResourcePack().getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));

                    if (inputStream != null && inputstream1 != null)
                    {
                        GameWindow.setIcon(new ByteBuffer[] {readIconImage(inputStream), readIconImage(inputstream1)});
                    }
                }
                catch (IOException iOException)
                {
                    warn("Error setting window icon: " + iOException.getClass().getName() + ": " + iOException.getMessage());
                }
                finally
                {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(inputstream1);
                }
            }
        }
    }

    private static ByteBuffer readIconImage(InputStream inputStream) throws IOException
    {
        BufferedImage bufferedimage = ImageIO.read(inputStream);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), (int[])null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

        for (int i : aint)
        {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    public static void checkDisplayMode()
    {
        try
        {
            if (minecraft.isFullScreen())
            {
                if (fullscreenModeChecked)
                {
                    return;
                }

                fullscreenModeChecked = true;
                desktopModeChecked = false;
                GameWindow.VideoMode displayMode = GameWindow.getVideoMode();
                Dimension dimension = getFullscreenDimension();

                if (dimension == null)
                {
                    return;
                }

                if (displayMode.getWidth() == dimension.width && displayMode.getHeight() == dimension.height)
                {
                    return;
                }

                GameWindow.VideoMode displaymode1 = getDisplayMode(dimension);

                if (displaymode1 == null)
                {
                    return;
                }

                GameWindow.setVideoMode(displaymode1);
                minecraft.displayWidth = GameWindow.getVideoMode().getWidth();
                minecraft.displayHeight = GameWindow.getVideoMode().getHeight();

                if (minecraft.displayWidth <= 0)
                {
                    minecraft.displayWidth = 1;
                }

                if (minecraft.displayHeight <= 0)
                {
                    minecraft.displayHeight = 1;
                }

                if (minecraft.currentScreen != null)
                {
                    ScaledResolution scaledResolution = new ScaledResolution(minecraft);
                    int i = scaledResolution.getScaledWidth();
                    int j = scaledResolution.getScaledHeight();
                    minecraft.currentScreen.setWorldAndResolution(minecraft, i, j);
                }

                updateFramebufferSize();
                GameWindow.setFullscreen(true);
                minecraft.gameSettings.updateVSync();
                GlStateManager.enableTexture2D();
            }
            else
            {
                if (desktopModeChecked)
                {
                    return;
                }

                desktopModeChecked = true;
                fullscreenModeChecked = false;
                minecraft.gameSettings.updateVSync();
                GameWindow.update();
                GlStateManager.enableTexture2D();
            }
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            gameSettings.ofFullscreenMode = "Default";
            gameSettings.saveOfOptions();
        }
    }

    public static void updateFramebufferSize()
    {
        minecraft.getFramebuffer().createBindFramebuffer(minecraft.displayWidth, minecraft.displayHeight);

        if (minecraft.entityRenderer != null)
        {
            minecraft.entityRenderer.updateShaderGroupSize(minecraft.displayWidth, minecraft.displayHeight);
        }

        minecraft.loadingScreen = new LoadingScreenRenderer(minecraft);
    }

    public static Object[] addObjectToArray(Object[] array, Object object)
    {
        if (array == null)
        {
            throw new NullPointerException("The given array is NULL");
        }
        else
        {
            int i = array.length;
            int j = i + 1;
            Object[] aobject = (Object[])((Object[])Array.newInstance(array.getClass().getComponentType(), j));
            System.arraycopy(array, 0, aobject, 0, i);
            aobject[i] = object;
            return aobject;
        }
    }

    public static Object[] addObjectToArray(Object[] array, Object object, int index)
    {
        List list = new ArrayList(Arrays.asList(array));
        list.add(index, object);
        Object[] aobject = (Object[])((Object[])Array.newInstance(array.getClass().getComponentType(), list.size()));
        return list.toArray(aobject);
    }

    public static Object[] addObjectsToArray(Object[] array, Object[] objectsToAdd)
    {
        if (array == null)
        {
            throw new NullPointerException("The given array is NULL");
        }
        else if (objectsToAdd.length == 0)
        {
            return array;
        }
        else
        {
            int i = array.length;
            int j = i + objectsToAdd.length;
            Object[] aobject = (Object[])((Object[])Array.newInstance(array.getClass().getComponentType(), j));
            System.arraycopy(array, 0, aobject, 0, i);
            System.arraycopy(objectsToAdd, 0, aobject, i, objectsToAdd.length);
            return aobject;
        }
    }

    public static Object[] removeObjectFromArray(Object[] array, Object object)
    {
        List list = new ArrayList(Arrays.asList(array));
        list.remove(object);
        Object[] aobject = collectionToArray(list, array.getClass().getComponentType());
        return aobject;
    }

    public static Object[] collectionToArray(Collection collection, Class elementClass)
    {
        if (collection == null)
        {
            return null;
        }
        else if (elementClass == null)
        {
            return null;
        }
        else if (elementClass.isPrimitive())
        {
            throw new IllegalArgumentException("Can not make arrays with primitive elements (int, double), element class: " + elementClass);
        }
        else
        {
            Object[] aobject = (Object[])((Object[])Array.newInstance(elementClass, collection.size()));
            return collection.toArray(aobject);
        }
    }

    public static boolean isCustomItems()
    {
        return gameSettings.ofCustomItems;
    }

    public static void drawFps()
    {
        int i = Minecraft.getDebugFPS();
        String s = getUpdates(minecraft.debug);
        int j = minecraft.renderGlobal.getCountActiveRenderers();
        int k = minecraft.renderGlobal.getCountEntitiesRendered();
        int l = minecraft.renderGlobal.getCountTileEntitiesRendered();
        String text2 = "" + i + "/" + getFpsMin() + " fps, C: " + j + ", E: " + k + "+" + l + ", U: " + s;
        minecraft.fontRendererObj.drawString(text2, 2, 2, -2039584);
    }

    public static int getFpsMin()
    {
        if (minecraft.debug == mcDebugLast)
        {
            return fpsMinLast;
        }
        else
        {
            mcDebugLast = minecraft.debug;
            FrameTimer frameTimer = minecraft.getFrameTimer();
            long[] along = frameTimer.getFrames();
            int i = frameTimer.getIndex();
            int j = frameTimer.getLastIndex();

            if (i == j)
            {
                return fpsMinLast;
            }
            else
            {
                int k = Minecraft.getDebugFPS();

                if (k <= 0)
                {
                    k = 1;
                }

                long l = (long)(1.0D / (double)k * 1.0E9D);
                long longValue2 = l;
                long longValue3 = 0L;

                for (int nestedIndex = MathHelper.normalizeAngle(i - 1, along.length); nestedIndex != j && (double)longValue3 < 1.0E9D; nestedIndex = MathHelper.normalizeAngle(nestedIndex - 1, along.length))
                {
                    long longValue4 = along[nestedIndex];

                    if (longValue4 > longValue2)
                    {
                        longValue2 = longValue4;
                    }

                    longValue3 += longValue4;
                }

                if (longValue2 <= 0L)
                {
                    fpsMinLast = k;
                    return fpsMinLast;
                }

                double doubleValue = (double)longValue2 / 1.0E9D;
                int minFps = (int)(1.0D / doubleValue);

                if (minFps < 1)
                {
                    minFps = 1;
                }

                if (minFps > k)
                {
                    minFps = k;
                }

                fpsMinLast = minFps;
                return fpsMinLast;
            }
        }
    }

    private static String getUpdates(String release)
    {
        int i = release.indexOf(40);

        if (i < 0)
        {
            return "";
        }
        else
        {
            int j = release.indexOf(32, i);
            return j < 0 ? "" : release.substring(i + 1, j);
        }
    }

    public static int getBitsOs()
    {
        String s = System.getenv("ProgramFiles(X86)");
        return s != null ? 64 : 32;
    }

    public static int getBitsJre()
    {
        String[] astring = new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (int i = 0; i < astring.length; ++i)
        {
            String s = astring[i];
            String propertyValue = System.getProperty(s);

            if (propertyValue != null && propertyValue.contains("64"))
            {
                return 64;
            }
        }

        return 32;
    }

    public static boolean isNotify64BitJava()
    {
        return notify64BitJava;
    }

    public static void setNotify64BitJava(boolean notify)
    {
        notify64BitJava = notify;
    }

    public static boolean isConnectedModels()
    {
        return false;
    }

    public static void showGuiMessage(String line1, String line2)
    {
        GuiMessage guiMessage = new GuiMessage(minecraft.currentScreen, line1, line2);
        minecraft.displayGuiScreen(guiMessage);
    }

    public static int[] addIntToArray(int[] array, int value)
    {
        return addIntsToArray(array, new int[] {value});
    }

    public static int[] addIntsToArray(int[] array, int[] values)
    {
        if (array != null && values != null)
        {
            int i = array.length;
            int j = i + values.length;
            int[] aint = new int[j];
            System.arraycopy(array, 0, aint, 0, i);
            System.arraycopy(values, 0, aint, i, values.length);

            return aint;
        }
        else
        {
            throw new NullPointerException("The given array is NULL");
        }
    }

    public static DynamicTexture getMojangLogoTexture(DynamicTexture texture)
    {
        try
        {
            ResourceLocation resourceLocation = new ResourceLocation("textures/gui/title/mojang.png");
            InputStream inputStream = getResourceStream(resourceLocation);

            if (inputStream == null)
            {
                return texture;
            }
            else
            {
                BufferedImage bufferedImage = ImageIO.read(inputStream);

                if (bufferedImage == null)
                {
                    return texture;
                }
                else
                {
                    DynamicTexture dynamicTexture = new DynamicTexture(bufferedImage);
                    return dynamicTexture;
                }
            }
        }
        catch (Exception exception)
        {
            warn(exception.getClass().getName() + ": " + exception.getMessage());
            return texture;
        }
    }

    public static void writeFile(File file, String contents) throws IOException
    {
        FileOutputStream fileoutputstream = new FileOutputStream(file);
        byte[] abyte = contents.getBytes("ASCII");
        fileoutputstream.write(abyte);
        fileoutputstream.close();
    }

    public static TextureMap getTextureMap()
    {
        return getMinecraft().getTextureMapBlocks();
    }

    public static boolean isDynamicLights()
    {
        return gameSettings.ofDynamicLights != 3;
    }

    public static boolean isDynamicLightsFast()
    {
        return gameSettings.ofDynamicLights == 1;
    }

    public static boolean isDynamicHandLight()
    {
        return !isDynamicLights() ? false : (isShaders() ? Shaders.isDynamicHandLight() : true);
    }

    public static boolean isCustomEntityModels()
    {
        return gameSettings.ofCustomEntityModels;
    }

    public static boolean isCustomGuis()
    {
        return gameSettings.ofCustomGuis;
    }

    public static int getScreenshotSize()
    {
        return gameSettings.ofScreenshotSize;
    }

    public static int[] toPrimitive(Integer[] array)
    {
        if (array == null)
        {
            return null;
        }
        else if (array.length == 0)
        {
            return new int[0];
        }
        else
        {
            int[] aint = new int[array.length];

            for (int i = 0; i < aint.length; ++i)
            {
                aint[i] = array[i].intValue();
            }

            return aint;
        }
    }

    public static boolean isRenderRegions()
    {
        return gameSettings.ofRenderRegions;
    }

    public static boolean isVbo()
    {
        return OpenGlHelper.useVbo();
    }

    public static boolean isSmoothFps()
    {
        return gameSettings.ofSmoothFps;
    }

    public static boolean openWebLink(URI uri)
    {
        try
        {
            Desktop.getDesktop().browse(uri);
            return true;
        }
        catch (Exception exception)
        {
            warn("Error opening link: " + uri);
            warn(exception.getClass().getName() + ": " + exception.getMessage());
            return false;
        }
    }

    public static boolean isShowGlErrors()
    {
        return gameSettings.ofShowGlErrors;
    }

    public static String arrayToString(boolean[] array, String separator)
    {
        if (array == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(array.length * 5);

            for (int i = 0; i < array.length; ++i)
            {
                boolean flag = array[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(flag));
            }

            return stringBuffer.toString();
        }
    }

    public static boolean isIntegratedServerRunning()
    {
        return minecraft.getIntegratedServer() == null ? false : minecraft.isIntegratedServerRunning();
    }

    public static IntBuffer createDirectIntBuffer(int capacity)
    {
        return GLAllocation.createDirectByteBuffer(capacity << 2).asIntBuffer();
    }

    public static String getGlErrorString(int errorCode)
    {
        switch (errorCode)
        {
            case 0:
                return "No error";

            case 1280:
                return "Invalid enum";

            case 1281:
                return "Invalid value";

            case 1282:
                return "Invalid operation";

            case 1283:
                return "Stack overflow";

            case 1284:
                return "Stack underflow";

            case 1285:
                return "Out of memory";

            case 1286:
                return "Invalid framebuffer operation";

            default:
                return "Unknown";
        }
    }

    public static boolean isTrue(Boolean value)
    {
        return value != null && value.booleanValue();
    }

    public static boolean isQuadsToTriangles()
    {
        return !isShaders() ? false : !Shaders.canRenderQuads();
    }

    public static void checkNull(Object object, String message) throws NullPointerException
    {
        if (object == null)
        {
            throw new NullPointerException(message);
        }
    }
}
