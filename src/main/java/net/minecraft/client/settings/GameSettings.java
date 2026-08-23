package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import optimization.betterfps.BetterFps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.optifine.ClearWater;
import net.optifine.CustomColors;
import net.optifine.CustomGuis;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.Lang;
import net.optifine.NaturalTextures;
import net.optifine.RandomEntities;
import net.optifine.shaders.Shaders;
import net.optifine.util.KeyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.GameWindow;
import net.minecraft.client.input.Keyboard;

public class GameSettings
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
    private static final String[] GUISCALES = new String[] {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[] {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[] {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] CLOUDS_TYPES = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;
    public int clouds = 2;
    public boolean fancyGraphics = true;
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.<String>newArrayList();
    public List<String> incompatibleResourcePacks = Lists.<String>newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo = true;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo = false;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.VALUES);
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    private Map<SoundCategory, Float> mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    public boolean useNativeTransport = true;
    public boolean entityShadows = true;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding[] keyBindsHotbar = new KeyBinding[] {new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    protected Minecraft mc;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean showLagometer;
    public String lastServer;
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;
    public int guiScale;
    public int particleSetting;
    public String language;
    public boolean forceUnicodeFont;
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public boolean ofRenderRegions = false;
    public boolean ofSmartAnimations = false;
    public float ofAoLevel = 1.0F;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofRain = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public boolean ofBetterSnow = false;
    public String ofFullscreenMode = "Default";
    public boolean ofSwampColors = true;
    public boolean ofRandomEntities = true;
    public boolean ofSmoothBiomes = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofCustomItems = true;
    public boolean ofNaturalTextures = false;
    public boolean ofEmissiveTextures = true;
    public String ofFastMath = "none";
    public boolean ofFastRender = false;
    public boolean ofEntityCulling = true;
    public boolean ofCullArmorStands = true;
    public boolean ofLimitChunkUpdates = true;
    public int ofCullTracingDistance = 128;
    public int ofCullSleepDelay = 10;
    public int ofCullHitboxLimit = 50;
    public boolean ofCullInterpolateCamera = true;
    public boolean ofCullTileBounds = true;
    public boolean ofCullLoadedChunks = true;
    public int ofChunkUpdateLimit = 50;
    public int ofParticlesLimit = 400;
    public int ofTranslucentBlocks = 0;
    public boolean ofDynamicFov = true;
    public boolean ofAlternateBlocks = true;
    public int ofDynamicLights = 3;
    public boolean ofCustomEntityModels = true;
    public boolean ofCustomGuis = true;
    public boolean ofShowGlErrors = true;
    public int ofScreenshotSize = 1;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofFireworkParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedTextures = true;
    public static final int DEFAULT = 0;
    public static final int FAST = 1;
    public static final int FANCY = 2;
    public static final int OFF = 3;
    public static final int SMART = 4;
    public static final int ANIM_ON = 0;
    public static final int ANIM_GENERATED = 1;
    public static final int ANIM_OFF = 2;
    public static final String DEFAULT_STR = "Default";
    private static final int[] OF_TREES_VALUES = new int[] {0, 1, 4, 2};
    private static final int[] OF_DYNAMIC_LIGHTS = new int[] {3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public KeyBinding ofKeyBindZoom;
    private File optionsFileOF;

    public GameSettings(Minecraft mcIn, File optionsFileIn)
    {
        this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.addAll(new KeyBinding[] {this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines}, this.keyBindsHotbar));
        this.difficulty = EnumDifficulty.NORMAL;
        this.lastServer = "";
        this.fovSetting = 70.0F;
        this.language = "en_US";
        this.forceUnicodeFont = false;
        this.mc = mcIn;
        this.optionsFile = new File(optionsFileIn, "options.txt");

        if (mcIn.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L)
        {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
            long i = 1000000L;

            if (Runtime.getRuntime().maxMemory() >= 1500L * i)
            {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(48.0F);
            }

            if (Runtime.getRuntime().maxMemory() >= 2500L * i)
            {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(64.0F);
            }
        }
        else
        {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        this.renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
        this.optionsFileOF = new File(optionsFileIn, "optionsof.txt");
        this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        this.ofKeyBindZoom = new KeyBinding("of.key.zoom", 46, "key.categories.misc");
        this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.add(this.keyBindings, this.ofKeyBindZoom));
        KeyUtils.fixKeyConflicts(this.keyBindings, new KeyBinding[] {this.ofKeyBindZoom});
        this.renderDistanceChunks = 8;
        this.loadOptions();
        this.applyFastMathMode();
        Config.initGameSettings(this);
    }

    public GameSettings()
    {
        this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.addAll(new KeyBinding[] {this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines}, this.keyBindsHotbar));
        this.difficulty = EnumDifficulty.NORMAL;
        this.lastServer = "";
        this.fovSetting = 70.0F;
        this.language = "en_US";
        this.forceUnicodeFont = false;
    }

    private static int migrateKeyCode(int code)
    {
        return code >= 256 ? Keyboard.toLwjglKey(code) : code;
    }

    public static String getKeyDisplayString(int key)
    {
        if (key < 0)
        {
            return I18n.format("key.mouseButton", new Object[] {Integer.valueOf(key + 101)});
        }

        return GameWindow.getKeyName(key);
    }

    public static boolean isKeyDown(KeyBinding key)
    {
        return key.getKeyCode() == 0 ? false : (key.getKeyCode() < 0 ? GameWindow.isButtonDown(key.getKeyCode() + 100) : GameWindow.isKeyDown(key.getKeyCode()));
    }

    public void setOptionKeyBinding(KeyBinding key, int keyCode)
    {
        key.setKeyCode(keyCode);
        this.saveOptions();
    }

    public void setOptionFloatValue(GameSettings.Options settingsOption, float value)
    {
        this.setOptionFloatValueOF(settingsOption, value);

        if (settingsOption == GameSettings.Options.SENSITIVITY)
        {
            this.mouseSensitivity = value;
        }

        if (settingsOption == GameSettings.Options.FOV)
        {
            this.fovSetting = value;
        }

        if (settingsOption == GameSettings.Options.GAMMA)
        {
            this.gammaSetting = value;
        }

        if (settingsOption == GameSettings.Options.FRAMERATE_LIMIT)
        {
            this.limitFramerate = (int)value;
            this.enableVsync = false;

            if (this.limitFramerate <= 0)
            {
                this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                this.enableVsync = true;
            }

            this.updateVSync();
        }

        if (settingsOption == GameSettings.Options.CHAT_OPACITY)
        {
            this.chatOpacity = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED)
        {
            this.chatHeightFocused = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED)
        {
            this.chatHeightUnfocused = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_WIDTH)
        {
            this.chatWidth = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.CHAT_SCALE)
        {
            this.chatScale = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (settingsOption == GameSettings.Options.MIPMAP_LEVELS)
        {
            int i = this.mipmapLevels;
            this.mipmapLevels = (int)value;

            if ((float)i != value)
            {
                this.mc.getTextureMapBlocks().setMipmapLevels(this.mipmapLevels);
                this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                this.mc.getTextureMapBlocks().setBlurMipmapDirect(false, this.mipmapLevels > 0);
                this.mc.scheduleResourcesRefresh();
            }
        }

        if (settingsOption == GameSettings.Options.BLOCK_ALTERNATIVES)
        {
            this.allowBlockAlternatives = !this.allowBlockAlternatives;
            this.mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.RENDER_DISTANCE)
        {
            this.renderDistanceChunks = (int)value;
            this.mc.renderGlobal.setDisplayListEntitiesDirty();
        }

    }

    public void applyFastMathMode()
    {
        String mode = this.ofFastMath == null ? "none" : this.ofFastMath.trim();

        if ("true".equalsIgnoreCase(mode))
        {
            mode = "hybrid";
        }
        else if ("false".equalsIgnoreCase(mode) || "off".equalsIgnoreCase(mode))
        {
            mode = "none";
        }

        this.ofFastMath = mode;

        if ("none".equals(mode))
        {
            MathHelper.fastMath = false;
            BetterFps.setMode(BetterFps.Mode.JAVA);
            return;
        }

        MathHelper.fastMath = !"vanilla".equals(mode) && !"java".equals(mode);

        try
        {
            BetterFps.setMode(BetterFps.Mode.valueOf(mode.toUpperCase().replace("-", "_")));
        }
        catch (IllegalArgumentException invalidMode)
        {
            this.ofFastMath = "none";
            MathHelper.fastMath = false;
            BetterFps.setMode(BetterFps.Mode.JAVA);
        }
    }

    public void setOptionValue(GameSettings.Options settingsOption, int value)
    {
        this.setOptionValueOF(settingsOption, value);

        if (settingsOption == GameSettings.Options.INVERT_MOUSE)
        {
            this.invertMouse = !this.invertMouse;
        }

        if (settingsOption == GameSettings.Options.GUI_SCALE)
        {
            this.guiScale += value;

            if (GuiScreen.isShiftKeyDown())
            {
                this.guiScale = 0;
            }

            GameWindow.VideoMode displaymode = Config.getLargestDisplayMode();
            int i = displaymode.getWidth() / 320;
            int j = displaymode.getHeight() / 240;
            int k = Math.min(i, j);

            if (this.guiScale < 0)
            {
                this.guiScale = k - 1;
            }

            if (this.mc.isUnicode() && this.guiScale % 2 != 0)
            {
                this.guiScale += value;
            }

            if (this.guiScale < 0 || this.guiScale >= k)
            {
                this.guiScale = 0;
            }
        }

        if (settingsOption == GameSettings.Options.PARTICLES)
        {
            this.particleSetting = (this.particleSetting + value) % 3;
        }

        if (settingsOption == GameSettings.Options.VIEW_BOBBING)
        {
            this.viewBobbing = !this.viewBobbing;
        }

        if (settingsOption == GameSettings.Options.RENDER_CLOUDS)
        {
            this.clouds = (this.clouds + value) % 3;
        }

        if (settingsOption == GameSettings.Options.FORCE_UNICODE_FONT)
        {
            this.forceUnicodeFont = !this.forceUnicodeFont;
            this.mc.fontRendererObj.setUnicodeFlag(this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.forceUnicodeFont);
        }

        if (settingsOption == GameSettings.Options.FBO_ENABLE)
        {
            this.fboEnable = !this.fboEnable;
        }

        if (settingsOption == GameSettings.Options.ANAGLYPH)
        {
            if (!this.anaglyph && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.an.shaders1"), Lang.get("of.message.an.shaders2"));
                return;
            }

            this.anaglyph = !this.anaglyph;
            this.mc.refreshResources();
        }

        if (settingsOption == GameSettings.Options.GRAPHICS)
        {
            this.fancyGraphics = !this.fancyGraphics;
            this.updateRenderClouds();
            this.mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.AMBIENT_OCCLUSION)
        {
            this.ambientOcclusion = (this.ambientOcclusion + value) % 3;
            this.mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.CHAT_VISIBILITY)
        {
            this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((this.chatVisibility.getChatVisibility() + value) % 3);
        }

        if (settingsOption == GameSettings.Options.CHAT_COLOR)
        {
            this.chatColours = !this.chatColours;
        }

        if (settingsOption == GameSettings.Options.CHAT_LINKS)
        {
            this.chatLinks = !this.chatLinks;
        }

        if (settingsOption == GameSettings.Options.CHAT_LINKS_PROMPT)
        {
            this.chatLinksPrompt = !this.chatLinksPrompt;
        }

        if (settingsOption == GameSettings.Options.SNOOPER_ENABLED)
        {
            this.snooperEnabled = !this.snooperEnabled;
        }

        if (settingsOption == GameSettings.Options.TOUCHSCREEN)
        {
            this.touchscreen = !this.touchscreen;
        }

        if (settingsOption == GameSettings.Options.USE_FULLSCREEN)
        {
            this.fullScreen = !this.fullScreen;

            if (this.mc.isFullScreen() != this.fullScreen)
            {
                this.mc.toggleFullscreen();
            }
        }

        if (settingsOption == GameSettings.Options.ENABLE_VSYNC)
        {
            this.enableVsync = !this.enableVsync;
            GameWindow.setVsync(this.enableVsync);
        }

        if (settingsOption == GameSettings.Options.USE_VBO)
        {
            this.useVbo = !this.useVbo;
            this.mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.BLOCK_ALTERNATIVES)
        {
            this.allowBlockAlternatives = !this.allowBlockAlternatives;
            this.mc.renderGlobal.loadRenderers();
        }

        if (settingsOption == GameSettings.Options.REDUCED_DEBUG_INFO)
        {
            this.reducedDebugInfo = !this.reducedDebugInfo;
        }

        if (settingsOption == GameSettings.Options.ENTITY_SHADOWS)
        {
            this.entityShadows = !this.entityShadows;
        }

        this.saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options settingOption)
    {
        float f = this.getOptionFloatValueOF(settingOption);
        return f != Float.MAX_VALUE ? f : (settingOption == GameSettings.Options.FOV ? this.fovSetting : (settingOption == GameSettings.Options.GAMMA ? this.gammaSetting : (settingOption == GameSettings.Options.SATURATION ? this.saturation : (settingOption == GameSettings.Options.SENSITIVITY ? this.mouseSensitivity : (settingOption == GameSettings.Options.CHAT_OPACITY ? this.chatOpacity : (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? this.chatHeightFocused : (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? this.chatHeightUnfocused : (settingOption == GameSettings.Options.CHAT_SCALE ? this.chatScale : (settingOption == GameSettings.Options.CHAT_WIDTH ? this.chatWidth : (settingOption == GameSettings.Options.FRAMERATE_LIMIT ? (float)this.limitFramerate : (settingOption == GameSettings.Options.MIPMAP_LEVELS ? (float)this.mipmapLevels : (settingOption == GameSettings.Options.RENDER_DISTANCE ? (float)this.renderDistanceChunks : 0.0F))))))))))));
    }

    public boolean getOptionOrdinalValue(GameSettings.Options settingOption)
    {
        switch (settingOption)
        {
            case INVERT_MOUSE:
                return this.invertMouse;

            case VIEW_BOBBING:
                return this.viewBobbing;

            case ANAGLYPH:
                return this.anaglyph;

            case FBO_ENABLE:
                return this.fboEnable;

            case CHAT_COLOR:
                return this.chatColours;

            case CHAT_LINKS:
                return this.chatLinks;

            case CHAT_LINKS_PROMPT:
                return this.chatLinksPrompt;

            case SNOOPER_ENABLED:
                return this.snooperEnabled;

            case USE_FULLSCREEN:
                return this.fullScreen;

            case ENABLE_VSYNC:
                return this.enableVsync;

            case USE_VBO:
                return this.useVbo;

            case TOUCHSCREEN:
                return this.touchscreen;

            case FORCE_UNICODE_FONT:
                return this.forceUnicodeFont;

            case BLOCK_ALTERNATIVES:
                return this.allowBlockAlternatives;

            case REDUCED_DEBUG_INFO:
                return this.reducedDebugInfo;

            case ENTITY_SHADOWS:
                return this.entityShadows;

            default:
                return false;
        }
    }

    private static String getTranslation(String[] strArray, int index)
    {
        if (index < 0 || index >= strArray.length)
        {
            index = 0;
        }

        return I18n.format(strArray[index], new Object[0]);
    }

    public String getKeyBinding(GameSettings.Options settingOption)
    {
        String s = this.getKeyBindingOF(settingOption);

        if (s != null)
        {
            return s;
        }
        else
        {
            String stringValue = I18n.format(settingOption.getEnumString(), new Object[0]) + ": ";

            if (settingOption.getEnumFloat())
            {
                float floatValue = this.getOptionFloatValue(settingOption);
                float f = settingOption.normalizeValue(floatValue);
                return settingOption == GameSettings.Options.MIPMAP_LEVELS && (double)floatValue >= 4.0D ? stringValue + Lang.get("of.general.max") : (settingOption == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? stringValue + I18n.format("options.sensitivity.min", new Object[0]) : (f == 1.0F ? stringValue + I18n.format("options.sensitivity.max", new Object[0]) : stringValue + (int)(f * 200.0F) + "%")) : (settingOption == GameSettings.Options.FOV ? (floatValue == 70.0F ? stringValue + I18n.format("options.fov.min", new Object[0]) : (floatValue == 110.0F ? stringValue + I18n.format("options.fov.max", new Object[0]) : stringValue + (int)floatValue)) : (settingOption == GameSettings.Options.FRAMERATE_LIMIT ? (floatValue == settingOption.valueMax ? stringValue + I18n.format("options.framerateLimit.max", new Object[0]) : stringValue + (int)floatValue + " fps") : (settingOption == GameSettings.Options.RENDER_CLOUDS ? (floatValue == settingOption.valueMin ? stringValue + I18n.format("options.cloudHeight.min", new Object[0]) : stringValue + ((int)floatValue + 128)) : (settingOption == GameSettings.Options.GAMMA ? (f == 0.0F ? stringValue + I18n.format("options.gamma.min", new Object[0]) : (f == 1.0F ? stringValue + I18n.format("options.gamma.max", new Object[0]) : stringValue + "+" + (int)(f * 100.0F) + "%")) : (settingOption == GameSettings.Options.SATURATION ? stringValue + (int)(f * 400.0F) + "%" : (settingOption == GameSettings.Options.CHAT_OPACITY ? stringValue + (int)(f * 90.0F + 10.0F) + "%" : (settingOption == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? stringValue + GuiNewChat.calculateChatboxHeight(f) + "px" : (settingOption == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? stringValue + GuiNewChat.calculateChatboxHeight(f) + "px" : (settingOption == GameSettings.Options.CHAT_WIDTH ? stringValue + GuiNewChat.calculateChatboxWidth(f) + "px" : (settingOption == GameSettings.Options.RENDER_DISTANCE ? stringValue + (int)floatValue + " chunks" : (settingOption == GameSettings.Options.MIPMAP_LEVELS ? (floatValue == 0.0F ? stringValue + I18n.format("options.off", new Object[0]) : stringValue + (int)floatValue) : (f == 0.0F ? stringValue + I18n.format("options.off", new Object[0]) : stringValue + (int)(f * 100.0F) + "%")))))))))))));
            }
            else if (settingOption.getEnumBoolean())
            {
                boolean flag = this.getOptionOrdinalValue(settingOption);
                return flag ? stringValue + I18n.format("options.on", new Object[0]) : stringValue + I18n.format("options.off", new Object[0]);
            }
            else if (settingOption == GameSettings.Options.GUI_SCALE)
            {
                return this.guiScale >= GUISCALES.length ? stringValue + this.guiScale + "x" : stringValue + getTranslation(GUISCALES, this.guiScale);
            }
            else if (settingOption == GameSettings.Options.CHAT_VISIBILITY)
            {
                return stringValue + I18n.format(this.chatVisibility.getResourceKey(), new Object[0]);
            }
            else if (settingOption == GameSettings.Options.PARTICLES)
            {
                return stringValue + getTranslation(PARTICLES, this.particleSetting);
            }
            else if (settingOption == GameSettings.Options.AMBIENT_OCCLUSION)
            {
                return stringValue + getTranslation(AMBIENT_OCCLUSIONS, this.ambientOcclusion);
            }
            else if (settingOption == GameSettings.Options.RENDER_CLOUDS)
            {
                return stringValue + getTranslation(CLOUDS_TYPES, this.clouds);
            }
            else if (settingOption == GameSettings.Options.GRAPHICS)
            {
                if (this.fancyGraphics)
                {
                    return stringValue + I18n.format("options.graphics.fancy", new Object[0]);
                }
                else
                {
                    String secondStringValue = "options.graphics.fast";
                    return stringValue + I18n.format("options.graphics.fast", new Object[0]);
                }
            }
            else
            {
                return stringValue;
            }
        }
    }

    public void loadOptions()
    {
        FileInputStream fileinputstream = null;
        label2:
        {
            try
            {
                if (this.optionsFile.exists())
                {
                    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(fileinputstream = new FileInputStream(this.optionsFile)));
                    String s = "";
                    this.mapSoundLevels.clear();

                    while ((s = bufferedreader.readLine()) != null)
                    {
                        try
                        {
                            String[] astring = s.split(":");

                            if (astring[0].equals("mouseSensitivity"))
                            {
                                this.mouseSensitivity = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("fov"))
                            {
                                this.fovSetting = this.parseFloat(astring[1]) * 40.0F + 70.0F;
                            }

                            if (astring[0].equals("gamma"))
                            {
                                this.gammaSetting = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("saturation"))
                            {
                                this.saturation = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("invertYMouse"))
                            {
                                this.invertMouse = astring[1].equals("true");
                            }

                            if (astring[0].equals("renderDistance"))
                            {
                                this.renderDistanceChunks = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("guiScale"))
                            {
                                this.guiScale = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("particles"))
                            {
                                this.particleSetting = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("bobView"))
                            {
                                this.viewBobbing = astring[1].equals("true");
                            }

                            if (astring[0].equals("anaglyph3d"))
                            {
                                this.anaglyph = astring[1].equals("true");
                            }

                            if (astring[0].equals("maxFps"))
                            {
                                this.limitFramerate = Integer.parseInt(astring[1]);

                                if (this.enableVsync)
                                {
                                    this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                if (this.limitFramerate <= 0)
                                {
                                    this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }
                            }

                            if (astring[0].equals("fboEnable"))
                            {
                                this.fboEnable = astring[1].equals("true");
                            }

                            if (astring[0].equals("difficulty"))
                            {
                                this.difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("fancyGraphics"))
                            {
                                this.fancyGraphics = astring[1].equals("true");
                                this.updateRenderClouds();
                            }

                            if (astring[0].equals("ao"))
                            {
                                if (astring[1].equals("true"))
                                {
                                    this.ambientOcclusion = 2;
                                }
                                else if (astring[1].equals("false"))
                                {
                                    this.ambientOcclusion = 0;
                                }
                                else
                                {
                                    this.ambientOcclusion = Integer.parseInt(astring[1]);
                                }
                            }

                            if (astring[0].equals("renderClouds"))
                            {
                                if (astring[1].equals("true"))
                                {
                                    this.clouds = 2;
                                }
                                else if (astring[1].equals("false"))
                                {
                                    this.clouds = 0;
                                }
                                else if (astring[1].equals("fast"))
                                {
                                    this.clouds = 1;
                                }
                            }

                            if (astring[0].equals("resourcePacks"))
                            {
                                this.resourcePacks = (List)gson.fromJson((String)s.substring(s.indexOf(58) + 1), typeListString);

                                if (this.resourcePacks == null)
                                {
                                    this.resourcePacks = Lists.<String>newArrayList();
                                }
                            }

                            if (astring[0].equals("incompatibleResourcePacks"))
                            {
                                this.incompatibleResourcePacks = (List)gson.fromJson((String)s.substring(s.indexOf(58) + 1), typeListString);

                                if (this.incompatibleResourcePacks == null)
                                {
                                    this.incompatibleResourcePacks = Lists.<String>newArrayList();
                                }
                            }

                            if (astring[0].equals("lastServer") && astring.length >= 2)
                            {
                                this.lastServer = s.substring(s.indexOf(58) + 1);
                            }

                            if (astring[0].equals("lang") && astring.length >= 2)
                            {
                                this.language = astring[1];
                            }

                            if (astring[0].equals("chatVisibility"))
                            {
                                this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("chatColors"))
                            {
                                this.chatColours = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinks"))
                            {
                                this.chatLinks = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinksPrompt"))
                            {
                                this.chatLinksPrompt = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatOpacity"))
                            {
                                this.chatOpacity = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("snooperEnabled"))
                            {
                                this.snooperEnabled = astring[1].equals("true");
                            }

                            if (astring[0].equals("fullscreen"))
                            {
                                this.fullScreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("enableVsync"))
                            {
                                this.enableVsync = astring[1].equals("true");

                                if (this.enableVsync)
                                {
                                    this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                this.updateVSync();
                            }

                            if (astring[0].equals("useVbo"))
                            {
                                this.useVbo = astring[1].equals("true");
                            }

                            if (astring[0].equals("hideServerAddress"))
                            {
                                this.hideServerAddress = astring[1].equals("true");
                            }

                            if (astring[0].equals("advancedItemTooltips"))
                            {
                                this.advancedItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("pauseOnLostFocus"))
                            {
                                this.pauseOnLostFocus = astring[1].equals("true");
                            }

                            if (astring[0].equals("touchscreen"))
                            {
                                this.touchscreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("overrideHeight"))
                            {
                                this.overrideHeight = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("overrideWidth"))
                            {
                                this.overrideWidth = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("heldItemTooltips"))
                            {
                                this.heldItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatHeightFocused"))
                            {
                                this.chatHeightFocused = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatHeightUnfocused"))
                            {
                                this.chatHeightUnfocused = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatScale"))
                            {
                                this.chatScale = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatWidth"))
                            {
                                this.chatWidth = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("showInventoryAchievementHint"))
                            {
                                this.showInventoryAchievementHint = astring[1].equals("true");
                            }

                            if (astring[0].equals("mipmapLevels"))
                            {
                                this.mipmapLevels = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("forceUnicodeFont"))
                            {
                                this.forceUnicodeFont = astring[1].equals("true");
                            }

                            if (astring[0].equals("allowBlockAlternatives"))
                            {
                                this.allowBlockAlternatives = astring[1].equals("true");
                            }

                            if (astring[0].equals("reducedDebugInfo"))
                            {
                                this.reducedDebugInfo = astring[1].equals("true");
                            }

                            if (astring[0].equals("useNativeTransport"))
                            {
                                this.useNativeTransport = astring[1].equals("true");
                            }

                            if (astring[0].equals("entityShadows"))
                            {
                                this.entityShadows = astring[1].equals("true");
                            }

                            for (KeyBinding keybinding : this.keyBindings)
                            {
                                if (astring[0].equals("key_" + keybinding.getKeyDescription()))
                                {
                                    keybinding.setKeyCode(migrateKeyCode(Integer.parseInt(astring[1])));
                                }
                            }

                            for (SoundCategory soundcategory : SoundCategory.VALUES)
                            {
                                if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName()))
                                {
                                    this.mapSoundLevels.put(soundcategory, Float.valueOf(this.parseFloat(astring[1])));
                                }
                            }

                            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.VALUES)
                            {
                                if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName()))
                                {
                                    this.setModelPartEnabled(enumplayermodelparts, astring[1].equals("true"));
                                }
                            }
                        }
                        catch (Exception exception)
                        {
                            logger.warn("Skipping bad option: " + s);
                            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
                        }
                    }

                    KeyBinding.resetKeyBindingArrayAndHash();
                    bufferedreader.close();
                    break label2;
                }
            }
            catch (Exception exception1)
            {
                logger.error((String)"Failed to load options", (Throwable)exception1);
                break label2;
            }
            finally
            {
                IOUtils.closeQuietly((InputStream)fileinputstream);
            }

            return;
        }
        this.loadOfOptions();
    }

    private float parseFloat(String str)
    {
        return str.equals("true") ? 1.0F : (str.equals("false") ? 0.0F : Float.parseFloat(str));
    }

    public void saveOptions()
    {
        
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(this.optionsFile));
            printwriter.println("invertYMouse:" + this.invertMouse);
            printwriter.println("mouseSensitivity:" + this.mouseSensitivity);
            printwriter.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + this.gammaSetting);
            printwriter.println("saturation:" + this.saturation);
            printwriter.println("renderDistance:" + this.renderDistanceChunks);
            printwriter.println("guiScale:" + this.guiScale);
            printwriter.println("particles:" + this.particleSetting);
            printwriter.println("bobView:" + this.viewBobbing);
            printwriter.println("anaglyph3d:" + this.anaglyph);
            printwriter.println("maxFps:" + this.limitFramerate);
            printwriter.println("fboEnable:" + this.fboEnable);
            printwriter.println("difficulty:" + this.difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + this.fancyGraphics);
            printwriter.println("ao:" + this.ambientOcclusion);

            switch (this.clouds)
            {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;

                case 1:
                    printwriter.println("renderClouds:fast");
                    break;

                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + gson.toJson((Object)this.resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + gson.toJson((Object)this.incompatibleResourcePacks));
            printwriter.println("lastServer:" + this.lastServer);
            printwriter.println("lang:" + this.language);
            printwriter.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + this.chatColours);
            printwriter.println("chatLinks:" + this.chatLinks);
            printwriter.println("chatLinksPrompt:" + this.chatLinksPrompt);
            printwriter.println("chatOpacity:" + this.chatOpacity);
            printwriter.println("snooperEnabled:" + this.snooperEnabled);
            printwriter.println("fullscreen:" + this.fullScreen);
            printwriter.println("enableVsync:" + this.enableVsync);
            printwriter.println("useVbo:" + this.useVbo);
            printwriter.println("hideServerAddress:" + this.hideServerAddress);
            printwriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            printwriter.println("touchscreen:" + this.touchscreen);
            printwriter.println("overrideWidth:" + this.overrideWidth);
            printwriter.println("overrideHeight:" + this.overrideHeight);
            printwriter.println("heldItemTooltips:" + this.heldItemTooltips);
            printwriter.println("chatHeightFocused:" + this.chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            printwriter.println("chatScale:" + this.chatScale);
            printwriter.println("chatWidth:" + this.chatWidth);
            printwriter.println("showInventoryAchievementHint:" + this.showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + this.mipmapLevels);
            printwriter.println("forceUnicodeFont:" + this.forceUnicodeFont);
            printwriter.println("allowBlockAlternatives:" + this.allowBlockAlternatives);
            printwriter.println("reducedDebugInfo:" + this.reducedDebugInfo);
            printwriter.println("useNativeTransport:" + this.useNativeTransport);
            printwriter.println("entityShadows:" + this.entityShadows);

            for (KeyBinding keybinding : this.keyBindings)
            {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.VALUES)
            {
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + this.getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.VALUES)
            {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + this.setModelParts.contains(enumplayermodelparts));
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.error((String)"Failed to save options", (Throwable)exception);
        }

        this.saveOfOptions();
        this.sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory sndCategory)
    {
        Float soundLevel = this.mapSoundLevels.get(sndCategory);
        return soundLevel == null ? 1.0F : soundLevel.floatValue();
    }

    public void setSoundLevel(SoundCategory sndCategory, float soundLevel)
    {
        this.mc.getSoundHandler().setSoundLevel(sndCategory, soundLevel);
        this.mapSoundLevels.put(sndCategory, Float.valueOf(soundLevel));
    }

    public void sendSettingsToServer()
    {
        if (this.mc.thePlayer != null)
        {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : this.setModelParts)
            {
                i |= enumplayermodelparts.getPartMask();
            }

            this.mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(this.language, this.renderDistanceChunks, this.chatVisibility, this.chatColours, i));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts()
    {
        return ImmutableSet.copyOf(this.setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable)
    {
        if (enable)
        {
            this.setModelParts.add(modelPart);
        }
        else
        {
            this.setModelParts.remove(modelPart);
        }

        this.sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts modelPart)
    {
        if (!this.getModelParts().contains(modelPart))
        {
            this.setModelParts.add(modelPart);
        }
        else
        {
            this.setModelParts.remove(modelPart);
        }

        this.sendSettingsToServer();
    }

    public int shouldRenderClouds()
    {
        return this.renderDistanceChunks >= 4 ? this.clouds : 0;
    }

    public boolean isUsingNativeTransport()
    {
        return this.useNativeTransport;
    }

    private void setOptionFloatValueOF(GameSettings.Options option, float value)
    {
        if (option == GameSettings.Options.CLOUD_HEIGHT)
        {
            this.ofCloudsHeight = value;
            this.mc.renderGlobal.resetClouds();
        }

        if (option == GameSettings.Options.AO_LEVEL)
        {
            this.ofAoLevel = value;
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.AA_LEVEL)
        {
            int i = (int)value;

            if (i > 0 && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.aa.shaders1"), Lang.get("of.message.aa.shaders2"));
                return;
            }

            int[] aint = new int[] {0, 2, 4, 6, 8, 12, 16};
            this.ofAaLevel = 0;

            for (int j = 0; j < aint.length; ++j)
            {
                if (i >= aint[j])
                {
                    this.ofAaLevel = aint[j];
                }
            }

            this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
        }

        if (option == GameSettings.Options.AF_LEVEL)
        {
            int k = (int)value;

            if (k > 1 && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.af.shaders1"), Lang.get("of.message.af.shaders2"));
                return;
            }

            for (this.ofAfLevel = 1; this.ofAfLevel * 2 <= k; this.ofAfLevel *= 2)
            {
                ;
            }

            this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
            this.mc.refreshResources();
        }

        if (option == GameSettings.Options.MIPMAP_TYPE)
        {
            int l = (int)value;
            this.ofMipmapType = Config.limit(l, 0, 3);
            this.mc.refreshResources();
        }

        if (option == GameSettings.Options.FULLSCREEN_MODE)
        {
            int thirdIntValue = (int)value - 1;
            String[] astring = Config.getDisplayModeNames();

            if (thirdIntValue < 0 || thirdIntValue >= astring.length)
            {
                this.ofFullscreenMode = "Default";
                return;
            }

            this.ofFullscreenMode = astring[thirdIntValue];
        }

        if (option == GameSettings.Options.CULL_TRACING_DISTANCE)
        {
            this.ofCullTracingDistance = (int)value;
        }

        if (option == GameSettings.Options.CULL_SLEEP_DELAY)
        {
            this.ofCullSleepDelay = (int)value;
        }

        if (option == GameSettings.Options.CULL_HITBOX_LIMIT)
        {
            this.ofCullHitboxLimit = (int)value;
        }

        if (option == GameSettings.Options.CHUNK_UPDATE_LIMIT)
        {
            this.ofChunkUpdateLimit = (int)value;
        }

        if (option == GameSettings.Options.PARTICLES_LIMIT)
        {
            this.ofParticlesLimit = (int)value;
        }
    }

    private float getOptionFloatValueOF(GameSettings.Options option)
    {
        if (option == GameSettings.Options.CLOUD_HEIGHT)
        {
            return this.ofCloudsHeight;
        }
        else if (option == GameSettings.Options.AO_LEVEL)
        {
            return this.ofAoLevel;
        }
        else if (option == GameSettings.Options.AA_LEVEL)
        {
            return (float)this.ofAaLevel;
        }
        else if (option == GameSettings.Options.AF_LEVEL)
        {
            return (float)this.ofAfLevel;
        }
        else if (option == GameSettings.Options.MIPMAP_TYPE)
        {
            return (float)this.ofMipmapType;
        }
        else if (option == GameSettings.Options.FRAMERATE_LIMIT)
        {
            return (float)this.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() && this.enableVsync ? 0.0F : (float)this.limitFramerate;
        }
        else if (option == GameSettings.Options.FULLSCREEN_MODE)
        {
            if (this.ofFullscreenMode.equals("Default"))
            {
                return 0.0F;
            }
            else
            {
                List list = Arrays.asList(Config.getDisplayModeNames());
                int i = list.indexOf(this.ofFullscreenMode);
                return i < 0 ? 0.0F : (float)(i + 1);
            }
        }
        else if (option == GameSettings.Options.CULL_TRACING_DISTANCE)
        {
            return (float)this.ofCullTracingDistance;
        }
        else if (option == GameSettings.Options.CULL_SLEEP_DELAY)
        {
            return (float)this.ofCullSleepDelay;
        }
        else if (option == GameSettings.Options.CULL_HITBOX_LIMIT)
        {
            return (float)this.ofCullHitboxLimit;
        }
        else if (option == GameSettings.Options.CHUNK_UPDATE_LIMIT)
        {
            return (float)this.ofChunkUpdateLimit;
        }
        else if (option == GameSettings.Options.PARTICLES_LIMIT)
        {
            return (float)this.ofParticlesLimit;
        }
        else
        {
            return Float.MAX_VALUE;
        }
    }

    private void setOptionValueOF(GameSettings.Options option, int increment)
    {
        if (option == GameSettings.Options.FOG_FANCY)
        {
            switch (this.ofFogType)
            {
                case 1:
                    this.ofFogType = 2;

                    if (!Config.isFancyFogAvailable())
                    {
                        this.ofFogType = 3;
                    }

                    break;

                case 2:
                    this.ofFogType = 3;
                    break;

                case 3:
                    this.ofFogType = 1;
                    break;

                default:
                    this.ofFogType = 1;
            }
        }

        if (option == GameSettings.Options.FOG_START)
        {
            this.ofFogStart += 0.2F;

            if (this.ofFogStart > 0.81F)
            {
                this.ofFogStart = 0.2F;
            }
        }

        if (option == GameSettings.Options.SMOOTH_FPS)
        {
            this.ofSmoothFps = !this.ofSmoothFps;
        }

        if (option == GameSettings.Options.SMOOTH_WORLD)
        {
            this.ofSmoothWorld = !this.ofSmoothWorld;
            Config.updateThreadPriorities();
        }

        if (option == GameSettings.Options.CLOUDS)
        {
            ++this.ofClouds;

            if (this.ofClouds > 3)
            {
                this.ofClouds = 0;
            }

            this.updateRenderClouds();
            this.mc.renderGlobal.resetClouds();
        }

        if (option == GameSettings.Options.TREES)
        {
            this.ofTrees = nextValue(this.ofTrees, OF_TREES_VALUES);
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.DROPPED_ITEMS)
        {
            ++this.ofDroppedItems;

            if (this.ofDroppedItems > 2)
            {
                this.ofDroppedItems = 0;
            }
        }

        if (option == GameSettings.Options.RAIN)
        {
            ++this.ofRain;

            if (this.ofRain > 3)
            {
                this.ofRain = 0;
            }
        }

        if (option == GameSettings.Options.ANIMATED_WATER)
        {
            ++this.ofAnimatedWater;

            if (this.ofAnimatedWater == 1)
            {
                ++this.ofAnimatedWater;
            }

            if (this.ofAnimatedWater > 2)
            {
                this.ofAnimatedWater = 0;
            }
        }

        if (option == GameSettings.Options.ANIMATED_LAVA)
        {
            ++this.ofAnimatedLava;

            if (this.ofAnimatedLava == 1)
            {
                ++this.ofAnimatedLava;
            }

            if (this.ofAnimatedLava > 2)
            {
                this.ofAnimatedLava = 0;
            }
        }

        if (option == GameSettings.Options.ANIMATED_FIRE)
        {
            this.ofAnimatedFire = !this.ofAnimatedFire;
        }

        if (option == GameSettings.Options.ANIMATED_PORTAL)
        {
            this.ofAnimatedPortal = !this.ofAnimatedPortal;
        }

        if (option == GameSettings.Options.ANIMATED_REDSTONE)
        {
            this.ofAnimatedRedstone = !this.ofAnimatedRedstone;
        }

        if (option == GameSettings.Options.ANIMATED_EXPLOSION)
        {
            this.ofAnimatedExplosion = !this.ofAnimatedExplosion;
        }

        if (option == GameSettings.Options.ANIMATED_FLAME)
        {
            this.ofAnimatedFlame = !this.ofAnimatedFlame;
        }

        if (option == GameSettings.Options.ANIMATED_SMOKE)
        {
            this.ofAnimatedSmoke = !this.ofAnimatedSmoke;
        }

        if (option == GameSettings.Options.VOID_PARTICLES)
        {
            this.ofVoidParticles = !this.ofVoidParticles;
        }

        if (option == GameSettings.Options.WATER_PARTICLES)
        {
            this.ofWaterParticles = !this.ofWaterParticles;
        }

        if (option == GameSettings.Options.PORTAL_PARTICLES)
        {
            this.ofPortalParticles = !this.ofPortalParticles;
        }

        if (option == GameSettings.Options.POTION_PARTICLES)
        {
            this.ofPotionParticles = !this.ofPotionParticles;
        }

        if (option == GameSettings.Options.FIREWORK_PARTICLES)
        {
            this.ofFireworkParticles = !this.ofFireworkParticles;
        }

        if (option == GameSettings.Options.DRIPPING_WATER_LAVA)
        {
            this.ofDrippingWaterLava = !this.ofDrippingWaterLava;
        }

        if (option == GameSettings.Options.ANIMATED_TERRAIN)
        {
            this.ofAnimatedTerrain = !this.ofAnimatedTerrain;
        }

        if (option == GameSettings.Options.ANIMATED_TEXTURES)
        {
            this.ofAnimatedTextures = !this.ofAnimatedTextures;
        }

        if (option == GameSettings.Options.RAIN_SPLASH)
        {
            this.ofRainSplash = !this.ofRainSplash;
        }

        if (option == GameSettings.Options.LAGOMETER)
        {
            this.ofLagometer = !this.ofLagometer;
        }

        if (option == GameSettings.Options.SHOW_FPS)
        {
            this.ofShowFps = !this.ofShowFps;
        }

        if (option == GameSettings.Options.AUTOSAVE_TICKS)
        {
            int i = 900;
            this.ofAutoSaveTicks = Math.max(this.ofAutoSaveTicks / i * i, i);
            this.ofAutoSaveTicks *= 2;

            if (this.ofAutoSaveTicks > 32 * i)
            {
                this.ofAutoSaveTicks = i;
            }
        }

        if (option == GameSettings.Options.BETTER_GRASS)
        {
            ++this.ofBetterGrass;

            if (this.ofBetterGrass > 3)
            {
                this.ofBetterGrass = 1;
            }

            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CONNECTED_TEXTURES)
        {
            ++this.ofConnectedTextures;

            if (this.ofConnectedTextures > 3)
            {
                this.ofConnectedTextures = 1;
            }

            if (this.ofConnectedTextures == 2)
            {
                this.mc.renderGlobal.loadRenderers();
            }
            else
            {
                this.mc.refreshResources();
            }
        }

        if (option == GameSettings.Options.WEATHER)
        {
            this.ofWeather = !this.ofWeather;
        }

        if (option == GameSettings.Options.SKY)
        {
            this.ofSky = !this.ofSky;
        }

        if (option == GameSettings.Options.STARS)
        {
            this.ofStars = !this.ofStars;
        }

        if (option == GameSettings.Options.SUN_MOON)
        {
            this.ofSunMoon = !this.ofSunMoon;
        }

        if (option == GameSettings.Options.VIGNETTE)
        {
            ++this.ofVignette;

            if (this.ofVignette > 2)
            {
                this.ofVignette = 0;
            }
        }

        if (option == GameSettings.Options.CHUNK_UPDATES)
        {
            ++this.ofChunkUpdates;

            if (this.ofChunkUpdates > 5)
            {
                this.ofChunkUpdates = 1;
            }
        }

        if (option == GameSettings.Options.CHUNK_UPDATES_DYNAMIC)
        {
            this.ofChunkUpdatesDynamic = !this.ofChunkUpdatesDynamic;
        }

        if (option == GameSettings.Options.TIME)
        {
            ++this.ofTime;

            if (this.ofTime > 2)
            {
                this.ofTime = 0;
            }
        }

        if (option == GameSettings.Options.CLEAR_WATER)
        {
            this.ofClearWater = !this.ofClearWater;
            this.updateWaterOpacity();
        }

        if (option == GameSettings.Options.PROFILER)
        {
            this.ofProfiler = !this.ofProfiler;
        }

        if (option == GameSettings.Options.BETTER_SNOW)
        {
            this.ofBetterSnow = !this.ofBetterSnow;
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.SWAMP_COLORS)
        {
            this.ofSwampColors = !this.ofSwampColors;
            CustomColors.updateUseDefaultGrassFoliageColors();
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.RANDOM_ENTITIES)
        {
            this.ofRandomEntities = !this.ofRandomEntities;
            RandomEntities.update();
        }

        if (option == GameSettings.Options.SMOOTH_BIOMES)
        {
            this.ofSmoothBiomes = !this.ofSmoothBiomes;
            CustomColors.updateUseDefaultGrassFoliageColors();
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CUSTOM_FONTS)
        {
            this.ofCustomFonts = !this.ofCustomFonts;
            this.mc.fontRendererObj.onResourceManagerReload(Config.getResourceManager());
            this.mc.standardGalacticFontRenderer.onResourceManagerReload(Config.getResourceManager());
        }

        if (option == GameSettings.Options.CUSTOM_COLORS)
        {
            this.ofCustomColors = !this.ofCustomColors;
            CustomColors.update();
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CUSTOM_ITEMS)
        {
            this.ofCustomItems = !this.ofCustomItems;
            this.mc.refreshResources();
        }

        if (option == GameSettings.Options.CUSTOM_SKY)
        {
            this.ofCustomSky = !this.ofCustomSky;
            CustomSky.update();
        }

        if (option == GameSettings.Options.SHOW_CAPES)
        {
            this.ofShowCapes = !this.ofShowCapes;
        }

        if (option == GameSettings.Options.NATURAL_TEXTURES)
        {
            this.ofNaturalTextures = !this.ofNaturalTextures;
            NaturalTextures.update();
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.EMISSIVE_TEXTURES)
        {
            this.ofEmissiveTextures = !this.ofEmissiveTextures;
            this.mc.refreshResources();
        }

        if (option == GameSettings.Options.FAST_MATH)
        {
            String[] modes = {"none", "hybrid", "rivens", "libgdx", "rivens-full", "rivens-half", "vanilla", "java"};
            int idx = 0;
            for (int m = 0; m < modes.length; m++) { if (modes[m].equals(this.ofFastMath)) { idx = m; break; } }
            idx = (idx + 1) % modes.length;
            this.ofFastMath = modes[idx];
            this.applyFastMathMode();
        }

        if (option == GameSettings.Options.FAST_RENDER)
        {
            if (!this.ofFastRender && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.fr.shaders1"), Lang.get("of.message.fr.shaders2"));
                return;
            }

            this.ofFastRender = !this.ofFastRender;

            if (this.ofFastRender)
            {
                this.mc.entityRenderer.stopUseShader();
            }

            Config.updateFramebufferSize();
        }
        if (option == GameSettings.Options.ENTITY_CULLING)
        {
            this.ofEntityCulling = !this.ofEntityCulling;
        }

        if (option == GameSettings.Options.CULL_ARMOR_STANDS)
        {
            this.ofCullArmorStands = !this.ofCullArmorStands;
        }

        if (option == GameSettings.Options.CULL_INTERPOLATE_CAMERA)
        {
            this.ofCullInterpolateCamera = !this.ofCullInterpolateCamera;
        }

        if (option == GameSettings.Options.CULL_TILE_BOUNDS)
        {
            this.ofCullTileBounds = !this.ofCullTileBounds;
        }

        if (option == GameSettings.Options.CULL_LOADED_CHUNKS)
        {
            this.ofCullLoadedChunks = !this.ofCullLoadedChunks;
        }

        if (option == GameSettings.Options.LIMIT_CHUNK_UPDATES)
        {
            this.ofLimitChunkUpdates = !this.ofLimitChunkUpdates;
        }


        if (option == GameSettings.Options.TRANSLUCENT_BLOCKS)
        {
            if (this.ofTranslucentBlocks == 0)
            {
                this.ofTranslucentBlocks = 1;
            }
            else if (this.ofTranslucentBlocks == 1)
            {
                this.ofTranslucentBlocks = 2;
            }
            else if (this.ofTranslucentBlocks == 2)
            {
                this.ofTranslucentBlocks = 0;
            }
            else
            {
                this.ofTranslucentBlocks = 0;
            }

            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.LAZY_CHUNK_LOADING)
        {
            this.ofLazyChunkLoading = !this.ofLazyChunkLoading;
        }

        if (option == GameSettings.Options.RENDER_REGIONS)
        {
            this.ofRenderRegions = !this.ofRenderRegions;
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.SMART_ANIMATIONS)
        {
            this.ofSmartAnimations = !this.ofSmartAnimations;
            this.mc.renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.DYNAMIC_FOV)
        {
            this.ofDynamicFov = !this.ofDynamicFov;
        }

        if (option == GameSettings.Options.ALTERNATE_BLOCKS)
        {
            this.ofAlternateBlocks = !this.ofAlternateBlocks;
            this.mc.refreshResources();
        }

        if (option == GameSettings.Options.DYNAMIC_LIGHTS)
        {
            this.ofDynamicLights = nextValue(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
            DynamicLights.removeLights(this.mc.renderGlobal);
        }

        if (option == GameSettings.Options.SCREENSHOT_SIZE)
        {
            ++this.ofScreenshotSize;

            if (this.ofScreenshotSize > 4)
            {
                this.ofScreenshotSize = 1;
            }

            if (!OpenGlHelper.isFramebufferEnabled())
            {
                this.ofScreenshotSize = 1;
            }
        }

        if (option == GameSettings.Options.CUSTOM_ENTITY_MODELS)
        {
            this.ofCustomEntityModels = !this.ofCustomEntityModels;
            this.mc.refreshResources();
        }

        if (option == GameSettings.Options.CUSTOM_GUIS)
        {
            this.ofCustomGuis = !this.ofCustomGuis;
            CustomGuis.update();
        }

        if (option == GameSettings.Options.SHOW_GL_ERRORS)
        {
            this.ofShowGlErrors = !this.ofShowGlErrors;
        }

        if (option == GameSettings.Options.HELD_ITEM_TOOLTIPS)
        {
            this.heldItemTooltips = !this.heldItemTooltips;
        }

        if (option == GameSettings.Options.ADVANCED_TOOLTIPS)
        {
            this.advancedItemTooltips = !this.advancedItemTooltips;
        }
    }

    private String getKeyBindingOF(GameSettings.Options option)
    {
        String s = I18n.format(option.getEnumString(), new Object[0]) + ": ";

        if (s == null)
        {
            s = option.getEnumString();
        }

        if (option == GameSettings.Options.RENDER_DISTANCE)
        {
            int secondIntValue = (int)this.getOptionFloatValue(option);
            String thirdStringValue = I18n.format("options.renderDistance.tiny", new Object[0]);
            int i = 2;

            if (secondIntValue >= 4)
            {
                thirdStringValue = I18n.format("options.renderDistance.short", new Object[0]);
                i = 4;
            }

            if (secondIntValue >= 8)
            {
                thirdStringValue = I18n.format("options.renderDistance.normal", new Object[0]);
                i = 8;
            }

            if (secondIntValue >= 16)
            {
                thirdStringValue = I18n.format("options.renderDistance.far", new Object[0]);
                i = 16;
            }

            if (secondIntValue >= 32)
            {
                thirdStringValue = Lang.get("of.options.renderDistance.extreme");
                i = 32;
            }

            if (secondIntValue >= 48)
            {
                thirdStringValue = Lang.get("of.options.renderDistance.insane");
                i = 48;
            }

            if (secondIntValue >= 64)
            {
                thirdStringValue = Lang.get("of.options.renderDistance.ludicrous");
                i = 64;
            }

            int j = this.renderDistanceChunks - i;
            String fourthStringValue = thirdStringValue;

            if (j > 0)
            {
                fourthStringValue = thirdStringValue + "+";
            }

            return s + secondIntValue + " " + fourthStringValue + "";
        }
        else if (option == GameSettings.Options.FOG_FANCY)
        {
            switch (this.ofFogType)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (option == GameSettings.Options.FOG_START)
        {
            return s + this.ofFogStart;
        }
        else if (option == GameSettings.Options.MIPMAP_TYPE)
        {
            switch (this.ofMipmapType)
            {
                case 0:
                    return s + Lang.get("of.options.mipmap.nearest");

                case 1:
                    return s + Lang.get("of.options.mipmap.linear");

                case 2:
                    return s + Lang.get("of.options.mipmap.bilinear");

                case 3:
                    return s + Lang.get("of.options.mipmap.trilinear");

                default:
                    return s + "of.options.mipmap.nearest";
            }
        }
        else if (option == GameSettings.Options.SMOOTH_FPS)
        {
            return this.ofSmoothFps ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SMOOTH_WORLD)
        {
            return this.ofSmoothWorld ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CLOUDS)
        {
            switch (this.ofClouds)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (option == GameSettings.Options.TREES)
        {
            switch (this.ofTrees)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                default:
                    return s + Lang.getDefault();

                case 4:
                    return s + Lang.get("of.general.smart");
            }
        }
        else if (option == GameSettings.Options.DROPPED_ITEMS)
        {
            switch (this.ofDroppedItems)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (option == GameSettings.Options.RAIN)
        {
            switch (this.ofRain)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (option == GameSettings.Options.ANIMATED_WATER)
        {
            switch (this.ofAnimatedWater)
            {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");

                case 2:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOn();
            }
        }
        else if (option == GameSettings.Options.ANIMATED_LAVA)
        {
            switch (this.ofAnimatedLava)
            {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");

                case 2:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOn();
            }
        }
        else if (option == GameSettings.Options.ANIMATED_FIRE)
        {
            return this.ofAnimatedFire ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_PORTAL)
        {
            return this.ofAnimatedPortal ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_REDSTONE)
        {
            return this.ofAnimatedRedstone ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_EXPLOSION)
        {
            return this.ofAnimatedExplosion ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_FLAME)
        {
            return this.ofAnimatedFlame ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_SMOKE)
        {
            return this.ofAnimatedSmoke ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.VOID_PARTICLES)
        {
            return this.ofVoidParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.WATER_PARTICLES)
        {
            return this.ofWaterParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.PORTAL_PARTICLES)
        {
            return this.ofPortalParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.POTION_PARTICLES)
        {
            return this.ofPotionParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.FIREWORK_PARTICLES)
        {
            return this.ofFireworkParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.DRIPPING_WATER_LAVA)
        {
            return this.ofDrippingWaterLava ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_TERRAIN)
        {
            return this.ofAnimatedTerrain ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ANIMATED_TEXTURES)
        {
            return this.ofAnimatedTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.RAIN_SPLASH)
        {
            return this.ofRainSplash ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.LAGOMETER)
        {
            return this.ofLagometer ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SHOW_FPS)
        {
            return this.ofShowFps ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.AUTOSAVE_TICKS)
        {
            int l = 900;
            return this.ofAutoSaveTicks <= l ? s + Lang.get("of.options.save.45s") : (this.ofAutoSaveTicks <= 2 * l ? s + Lang.get("of.options.save.90s") : (this.ofAutoSaveTicks <= 4 * l ? s + Lang.get("of.options.save.3min") : (this.ofAutoSaveTicks <= 8 * l ? s + Lang.get("of.options.save.6min") : (this.ofAutoSaveTicks <= 16 * l ? s + Lang.get("of.options.save.12min") : s + Lang.get("of.options.save.24min")))));
        }
        else if (option == GameSettings.Options.BETTER_GRASS)
        {
            switch (this.ofBetterGrass)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (option == GameSettings.Options.CONNECTED_TEXTURES)
        {
            switch (this.ofConnectedTextures)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (option == GameSettings.Options.WEATHER)
        {
            return this.ofWeather ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SKY)
        {
            return this.ofSky ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.STARS)
        {
            return this.ofStars ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SUN_MOON)
        {
            return this.ofSunMoon ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.VIGNETTE)
        {
            switch (this.ofVignette)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (option == GameSettings.Options.CHUNK_UPDATES)
        {
            return s + this.ofChunkUpdates;
        }
        else if (option == GameSettings.Options.CHUNK_UPDATES_DYNAMIC)
        {
            return this.ofChunkUpdatesDynamic ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.TIME)
        {
            return this.ofTime == 1 ? s + Lang.get("of.options.time.dayOnly") : (this.ofTime == 2 ? s + Lang.get("of.options.time.nightOnly") : s + Lang.getDefault());
        }
        else if (option == GameSettings.Options.CLEAR_WATER)
        {
            return this.ofClearWater ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.AA_LEVEL)
        {
            String fifthStringValue = "";

            if (this.ofAaLevel != Config.getAntialiasingLevel())
            {
                fifthStringValue = " (" + Lang.get("of.general.restart") + ")";
            }

            return this.ofAaLevel == 0 ? s + Lang.getOff() + fifthStringValue : s + this.ofAaLevel + fifthStringValue;
        }
        else if (option == GameSettings.Options.AF_LEVEL)
        {
            return this.ofAfLevel == 1 ? s + Lang.getOff() : s + this.ofAfLevel;
        }
        else if (option == GameSettings.Options.PROFILER)
        {
            return this.ofProfiler ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.BETTER_SNOW)
        {
            return this.ofBetterSnow ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SWAMP_COLORS)
        {
            return this.ofSwampColors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.RANDOM_ENTITIES)
        {
            return this.ofRandomEntities ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SMOOTH_BIOMES)
        {
            return this.ofSmoothBiomes ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CUSTOM_FONTS)
        {
            return this.ofCustomFonts ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CUSTOM_COLORS)
        {
            return this.ofCustomColors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CUSTOM_SKY)
        {
            return this.ofCustomSky ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SHOW_CAPES)
        {
            return this.ofShowCapes ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CUSTOM_ITEMS)
        {
            return this.ofCustomItems ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.NATURAL_TEXTURES)
        {
            return this.ofNaturalTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.EMISSIVE_TEXTURES)
        {
            return this.ofEmissiveTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.FAST_MATH)
        {
            return s + this.ofFastMath;
        }
        else if (option == GameSettings.Options.ENTITY_CULLING)
        {
            return this.ofEntityCulling ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CULL_ARMOR_STANDS)
        {
            return this.ofCullArmorStands ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CULL_INTERPOLATE_CAMERA)
        {
            return this.ofCullInterpolateCamera ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CULL_TILE_BOUNDS)
        {
            return this.ofCullTileBounds ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CULL_LOADED_CHUNKS)
        {
            return this.ofCullLoadedChunks ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.LIMIT_CHUNK_UPDATES)
        {
            return this.ofLimitChunkUpdates ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CULL_TRACING_DISTANCE)
        {
            return s + this.ofCullTracingDistance;
        }
        else if (option == GameSettings.Options.CULL_SLEEP_DELAY)
        {
            return s + this.ofCullSleepDelay;
        }
        else if (option == GameSettings.Options.CULL_HITBOX_LIMIT)
        {
            return s + this.ofCullHitboxLimit;
        }
        else if (option == GameSettings.Options.CHUNK_UPDATE_LIMIT)
        {
            return s + this.ofChunkUpdateLimit;
        }
        else if (option == GameSettings.Options.PARTICLES_LIMIT)
        {
            return s + this.ofParticlesLimit;
        }
        else if (option == GameSettings.Options.FAST_RENDER)
        {
            return this.ofFastRender ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.TRANSLUCENT_BLOCKS)
        {
            return this.ofTranslucentBlocks == 1 ? s + Lang.getFast() : (this.ofTranslucentBlocks == 2 ? s + Lang.getFancy() : s + Lang.getDefault());
        }
        else if (option == GameSettings.Options.LAZY_CHUNK_LOADING)
        {
            return this.ofLazyChunkLoading ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.RENDER_REGIONS)
        {
            return this.ofRenderRegions ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SMART_ANIMATIONS)
        {
            return this.ofSmartAnimations ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.DYNAMIC_FOV)
        {
            return this.ofDynamicFov ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ALTERNATE_BLOCKS)
        {
            return this.ofAlternateBlocks ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.DYNAMIC_LIGHTS)
        {
            int k = indexOf(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
            return s + getTranslation(KEYS_DYNAMIC_LIGHTS, k);
        }
        else if (option == GameSettings.Options.SCREENSHOT_SIZE)
        {
            return this.ofScreenshotSize <= 1 ? s + Lang.getDefault() : s + this.ofScreenshotSize + "x";
        }
        else if (option == GameSettings.Options.CUSTOM_ENTITY_MODELS)
        {
            return this.ofCustomEntityModels ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.CUSTOM_GUIS)
        {
            return this.ofCustomGuis ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.SHOW_GL_ERRORS)
        {
            return this.ofShowGlErrors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.FULLSCREEN_MODE)
        {
            return this.ofFullscreenMode.equals("Default") ? s + Lang.getDefault() : s + this.ofFullscreenMode;
        }
        else if (option == GameSettings.Options.HELD_ITEM_TOOLTIPS)
        {
            return this.heldItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.ADVANCED_TOOLTIPS)
        {
            return this.advancedItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (option == GameSettings.Options.FRAMERATE_LIMIT)
        {
            float f = this.getOptionFloatValue(option);
            return f == 0.0F ? s + Lang.get("of.options.framerateLimit.vsync") : (f == option.valueMax ? s + I18n.format("options.framerateLimit.max", new Object[0]) : s + (int)f + " fps");
        }
        else
        {
            return null;
        }
    }

    public void loadOfOptions()
    {
        try
        {
            File file1 = this.optionsFileOF;

            if (!file1.exists())
            {
                file1 = this.optionsFile;
            }

            if (!file1.exists())
            {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), "UTF-8"));
            String s = "";

            while ((s = bufferedreader.readLine()) != null)
            {
                try
                {
                    String[] astring = s.split(":");

                    if (astring[0].equals("ofRenderDistanceChunks") && astring.length >= 2)
                    {
                        this.renderDistanceChunks = Integer.valueOf(astring[1]).intValue();
                        this.renderDistanceChunks = Config.limit(this.renderDistanceChunks, 2, 1024);
                    }

                    if (astring[0].equals("ofFogType") && astring.length >= 2)
                    {
                        this.ofFogType = Integer.valueOf(astring[1]).intValue();
                        this.ofFogType = Config.limit(this.ofFogType, 1, 3);
                    }

                    if (astring[0].equals("ofFogStart") && astring.length >= 2)
                    {
                        this.ofFogStart = Float.valueOf(astring[1]).floatValue();

                        if (this.ofFogStart < 0.2F)
                        {
                            this.ofFogStart = 0.2F;
                        }

                        if (this.ofFogStart > 0.81F)
                        {
                            this.ofFogStart = 0.8F;
                        }
                    }

                    if (astring[0].equals("ofMipmapType") && astring.length >= 2)
                    {
                        this.ofMipmapType = Integer.valueOf(astring[1]).intValue();
                        this.ofMipmapType = Config.limit(this.ofMipmapType, 0, 3);
                    }

                    if (astring[0].equals("ofOcclusionFancy") && astring.length >= 2)
                    {
                        this.ofOcclusionFancy = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmoothFps") && astring.length >= 2)
                    {
                        this.ofSmoothFps = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmoothWorld") && astring.length >= 2)
                    {
                        this.ofSmoothWorld = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAoLevel") && astring.length >= 2)
                    {
                        this.ofAoLevel = Float.valueOf(astring[1]).floatValue();
                        this.ofAoLevel = Config.limit(this.ofAoLevel, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofClouds") && astring.length >= 2)
                    {
                        this.ofClouds = Integer.valueOf(astring[1]).intValue();
                        this.ofClouds = Config.limit(this.ofClouds, 0, 3);
                        this.updateRenderClouds();
                    }

                    if (astring[0].equals("ofCloudsHeight") && astring.length >= 2)
                    {
                        this.ofCloudsHeight = Float.valueOf(astring[1]).floatValue();
                        this.ofCloudsHeight = Config.limit(this.ofCloudsHeight, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofTrees") && astring.length >= 2)
                    {
                        this.ofTrees = Integer.valueOf(astring[1]).intValue();
                        this.ofTrees = limit(this.ofTrees, OF_TREES_VALUES);
                    }

                    if (astring[0].equals("ofDroppedItems") && astring.length >= 2)
                    {
                        this.ofDroppedItems = Integer.valueOf(astring[1]).intValue();
                        this.ofDroppedItems = Config.limit(this.ofDroppedItems, 0, 2);
                    }

                    if (astring[0].equals("ofRain") && astring.length >= 2)
                    {
                        this.ofRain = Integer.valueOf(astring[1]).intValue();
                        this.ofRain = Config.limit(this.ofRain, 0, 3);
                    }

                    if (astring[0].equals("ofAnimatedWater") && astring.length >= 2)
                    {
                        this.ofAnimatedWater = Integer.valueOf(astring[1]).intValue();
                        this.ofAnimatedWater = Config.limit(this.ofAnimatedWater, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedLava") && astring.length >= 2)
                    {
                        this.ofAnimatedLava = Integer.valueOf(astring[1]).intValue();
                        this.ofAnimatedLava = Config.limit(this.ofAnimatedLava, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedFire") && astring.length >= 2)
                    {
                        this.ofAnimatedFire = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedPortal") && astring.length >= 2)
                    {
                        this.ofAnimatedPortal = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedRedstone") && astring.length >= 2)
                    {
                        this.ofAnimatedRedstone = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedExplosion") && astring.length >= 2)
                    {
                        this.ofAnimatedExplosion = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedFlame") && astring.length >= 2)
                    {
                        this.ofAnimatedFlame = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedSmoke") && astring.length >= 2)
                    {
                        this.ofAnimatedSmoke = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofVoidParticles") && astring.length >= 2)
                    {
                        this.ofVoidParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofWaterParticles") && astring.length >= 2)
                    {
                        this.ofWaterParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofPortalParticles") && astring.length >= 2)
                    {
                        this.ofPortalParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofPotionParticles") && astring.length >= 2)
                    {
                        this.ofPotionParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofFireworkParticles") && astring.length >= 2)
                    {
                        this.ofFireworkParticles = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofDrippingWaterLava") && astring.length >= 2)
                    {
                        this.ofDrippingWaterLava = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedTerrain") && astring.length >= 2)
                    {
                        this.ofAnimatedTerrain = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAnimatedTextures") && astring.length >= 2)
                    {
                        this.ofAnimatedTextures = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofRainSplash") && astring.length >= 2)
                    {
                        this.ofRainSplash = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofLagometer") && astring.length >= 2)
                    {
                        this.ofLagometer = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofShowFps") && astring.length >= 2)
                    {
                        this.ofShowFps = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAutoSaveTicks") && astring.length >= 2)
                    {
                        this.ofAutoSaveTicks = Integer.valueOf(astring[1]).intValue();
                        this.ofAutoSaveTicks = Config.limit(this.ofAutoSaveTicks, 40, 40000);
                    }

                    if (astring[0].equals("ofBetterGrass") && astring.length >= 2)
                    {
                        this.ofBetterGrass = Integer.valueOf(astring[1]).intValue();
                        this.ofBetterGrass = Config.limit(this.ofBetterGrass, 1, 3);
                    }

                    if (astring[0].equals("ofConnectedTextures") && astring.length >= 2)
                    {
                        this.ofConnectedTextures = Integer.valueOf(astring[1]).intValue();
                        this.ofConnectedTextures = Config.limit(this.ofConnectedTextures, 1, 3);
                    }

                    if (astring[0].equals("ofWeather") && astring.length >= 2)
                    {
                        this.ofWeather = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSky") && astring.length >= 2)
                    {
                        this.ofSky = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofStars") && astring.length >= 2)
                    {
                        this.ofStars = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSunMoon") && astring.length >= 2)
                    {
                        this.ofSunMoon = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofVignette") && astring.length >= 2)
                    {
                        this.ofVignette = Integer.valueOf(astring[1]).intValue();
                        this.ofVignette = Config.limit(this.ofVignette, 0, 2);
                    }

                    if (astring[0].equals("ofChunkUpdates") && astring.length >= 2)
                    {
                        this.ofChunkUpdates = Integer.valueOf(astring[1]).intValue();
                        this.ofChunkUpdates = Config.limit(this.ofChunkUpdates, 1, 5);
                    }

                    if (astring[0].equals("ofChunkUpdatesDynamic") && astring.length >= 2)
                    {
                        this.ofChunkUpdatesDynamic = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofTime") && astring.length >= 2)
                    {
                        this.ofTime = Integer.valueOf(astring[1]).intValue();
                        this.ofTime = Config.limit(this.ofTime, 0, 2);
                    }

                    if (astring[0].equals("ofClearWater") && astring.length >= 2)
                    {
                        this.ofClearWater = Boolean.valueOf(astring[1]).booleanValue();
                        this.updateWaterOpacity();
                    }

                    if (astring[0].equals("ofAaLevel") && astring.length >= 2)
                    {
                        this.ofAaLevel = Integer.valueOf(astring[1]).intValue();
                        this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
                    }

                    if (astring[0].equals("ofAfLevel") && astring.length >= 2)
                    {
                        this.ofAfLevel = Integer.valueOf(astring[1]).intValue();
                        this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
                    }

                    if (astring[0].equals("ofProfiler") && astring.length >= 2)
                    {
                        this.ofProfiler = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofBetterSnow") && astring.length >= 2)
                    {
                        this.ofBetterSnow = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSwampColors") && astring.length >= 2)
                    {
                        this.ofSwampColors = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofRandomEntities") && astring.length >= 2)
                    {
                        this.ofRandomEntities = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmoothBiomes") && astring.length >= 2)
                    {
                        this.ofSmoothBiomes = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomFonts") && astring.length >= 2)
                    {
                        this.ofCustomFonts = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomColors") && astring.length >= 2)
                    {
                        this.ofCustomColors = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomItems") && astring.length >= 2)
                    {
                        this.ofCustomItems = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomSky") && astring.length >= 2)
                    {
                        this.ofCustomSky = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofShowCapes") && astring.length >= 2)
                    {
                        this.ofShowCapes = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofNaturalTextures") && astring.length >= 2)
                    {
                        this.ofNaturalTextures = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofEmissiveTextures") && astring.length >= 2)
                    {
                        this.ofEmissiveTextures = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofLazyChunkLoading") && astring.length >= 2)
                    {
                        this.ofLazyChunkLoading = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofRenderRegions") && astring.length >= 2)
                    {
                        this.ofRenderRegions = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofSmartAnimations") && astring.length >= 2)
                    {
                        this.ofSmartAnimations = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofDynamicFov") && astring.length >= 2)
                    {
                        this.ofDynamicFov = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofAlternateBlocks") && astring.length >= 2)
                    {
                        this.ofAlternateBlocks = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofDynamicLights") && astring.length >= 2)
                    {
                        this.ofDynamicLights = Integer.valueOf(astring[1]).intValue();
                        this.ofDynamicLights = limit(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
                    }

                    if (astring[0].equals("ofScreenshotSize") && astring.length >= 2)
                    {
                        this.ofScreenshotSize = Integer.valueOf(astring[1]).intValue();
                        this.ofScreenshotSize = Config.limit(this.ofScreenshotSize, 1, 4);
                    }

                    if (astring[0].equals("ofCustomEntityModels") && astring.length >= 2)
                    {
                        this.ofCustomEntityModels = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCustomGuis") && astring.length >= 2)
                    {
                        this.ofCustomGuis = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofShowGlErrors") && astring.length >= 2)
                    {
                        this.ofShowGlErrors = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofFullscreenMode") && astring.length >= 2)
                    {
                        this.ofFullscreenMode = astring[1];
                    }

                    if (astring[0].equals("ofEntityCulling") && astring.length >= 2)
                    {
                        this.ofEntityCulling = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCullArmorStands") && astring.length >= 2)
                    {
                        this.ofCullArmorStands = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCullInterpolateCamera") && astring.length >= 2)
                    {
                        this.ofCullInterpolateCamera = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCullTileBounds") && astring.length >= 2)
                    {
                        this.ofCullTileBounds = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCullLoadedChunks") && astring.length >= 2)
                    {
                        this.ofCullLoadedChunks = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofLimitChunkUpdates") && astring.length >= 2)
                    {
                        this.ofLimitChunkUpdates = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofCullTracingDistance") && astring.length >= 2)
                    {
                        this.ofCullTracingDistance = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("ofCullSleepDelay") && astring.length >= 2)
                    {
                        this.ofCullSleepDelay = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("ofCullHitboxLimit") && astring.length >= 2)
                    {
                        this.ofCullHitboxLimit = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("ofChunkUpdateLimit") && astring.length >= 2)
                    {
                        this.ofChunkUpdateLimit = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("ofParticlesLimit") && astring.length >= 2)
                    {
                        this.ofParticlesLimit = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("ofFastMath") && astring.length >= 2)
                    {
                        this.ofFastMath = astring[1];
                        this.applyFastMathMode();
                    }

                    if (astring[0].equals("ofFastRender") && astring.length >= 2)
                    {
                        this.ofFastRender = Boolean.valueOf(astring[1]).booleanValue();
                    }

                    if (astring[0].equals("ofTranslucentBlocks") && astring.length >= 2)
                    {
                        this.ofTranslucentBlocks = Integer.valueOf(astring[1]).intValue();
                        this.ofTranslucentBlocks = Config.limit(this.ofTranslucentBlocks, 0, 2);
                    }

                    if (astring[0].equals("key_" + this.ofKeyBindZoom.getKeyDescription()))
                    {
                        this.ofKeyBindZoom.setKeyCode(migrateKeyCode(Integer.parseInt(astring[1])));
                    }
                }
                catch (Exception exception)
                {
                    Config.dbg("Skipping bad option: " + s);
                    net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
                }
            }

            KeyUtils.fixKeyConflicts(this.keyBindings, new KeyBinding[] {this.ofKeyBindZoom});
            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
            this.applyFastMathMode();
        }
        catch (Exception exception1)
        {
            Config.warn("Failed to load options");
            net.minecraft.src.Config.warn(exception1.getClass().getName() + ": " + exception1.getMessage(), exception1);
            this.applyFastMathMode();
        }
    }

    public void saveOfOptions()
    {
        try
        {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFileOF), "UTF-8"));
            printWriter.println("ofFogType:" + this.ofFogType);
            printWriter.println("ofFogStart:" + this.ofFogStart);
            printWriter.println("ofMipmapType:" + this.ofMipmapType);
            printWriter.println("ofOcclusionFancy:" + this.ofOcclusionFancy);
            printWriter.println("ofSmoothFps:" + this.ofSmoothFps);
            printWriter.println("ofSmoothWorld:" + this.ofSmoothWorld);
            printWriter.println("ofAoLevel:" + this.ofAoLevel);
            printWriter.println("ofClouds:" + this.ofClouds);
            printWriter.println("ofCloudsHeight:" + this.ofCloudsHeight);
            printWriter.println("ofTrees:" + this.ofTrees);
            printWriter.println("ofDroppedItems:" + this.ofDroppedItems);
            printWriter.println("ofRain:" + this.ofRain);
            printWriter.println("ofAnimatedWater:" + this.ofAnimatedWater);
            printWriter.println("ofAnimatedLava:" + this.ofAnimatedLava);
            printWriter.println("ofAnimatedFire:" + this.ofAnimatedFire);
            printWriter.println("ofAnimatedPortal:" + this.ofAnimatedPortal);
            printWriter.println("ofAnimatedRedstone:" + this.ofAnimatedRedstone);
            printWriter.println("ofAnimatedExplosion:" + this.ofAnimatedExplosion);
            printWriter.println("ofAnimatedFlame:" + this.ofAnimatedFlame);
            printWriter.println("ofAnimatedSmoke:" + this.ofAnimatedSmoke);
            printWriter.println("ofVoidParticles:" + this.ofVoidParticles);
            printWriter.println("ofWaterParticles:" + this.ofWaterParticles);
            printWriter.println("ofPortalParticles:" + this.ofPortalParticles);
            printWriter.println("ofPotionParticles:" + this.ofPotionParticles);
            printWriter.println("ofFireworkParticles:" + this.ofFireworkParticles);
            printWriter.println("ofDrippingWaterLava:" + this.ofDrippingWaterLava);
            printWriter.println("ofAnimatedTerrain:" + this.ofAnimatedTerrain);
            printWriter.println("ofAnimatedTextures:" + this.ofAnimatedTextures);
            printWriter.println("ofRainSplash:" + this.ofRainSplash);
            printWriter.println("ofLagometer:" + this.ofLagometer);
            printWriter.println("ofShowFps:" + this.ofShowFps);
            printWriter.println("ofAutoSaveTicks:" + this.ofAutoSaveTicks);
            printWriter.println("ofBetterGrass:" + this.ofBetterGrass);
            printWriter.println("ofConnectedTextures:" + this.ofConnectedTextures);
            printWriter.println("ofWeather:" + this.ofWeather);
            printWriter.println("ofSky:" + this.ofSky);
            printWriter.println("ofStars:" + this.ofStars);
            printWriter.println("ofSunMoon:" + this.ofSunMoon);
            printWriter.println("ofVignette:" + this.ofVignette);
            printWriter.println("ofChunkUpdates:" + this.ofChunkUpdates);
            printWriter.println("ofChunkUpdatesDynamic:" + this.ofChunkUpdatesDynamic);
            printWriter.println("ofTime:" + this.ofTime);
            printWriter.println("ofClearWater:" + this.ofClearWater);
            printWriter.println("ofAaLevel:" + this.ofAaLevel);
            printWriter.println("ofAfLevel:" + this.ofAfLevel);
            printWriter.println("ofProfiler:" + this.ofProfiler);
            printWriter.println("ofBetterSnow:" + this.ofBetterSnow);
            printWriter.println("ofSwampColors:" + this.ofSwampColors);
            printWriter.println("ofRandomEntities:" + this.ofRandomEntities);
            printWriter.println("ofSmoothBiomes:" + this.ofSmoothBiomes);
            printWriter.println("ofCustomFonts:" + this.ofCustomFonts);
            printWriter.println("ofCustomColors:" + this.ofCustomColors);
            printWriter.println("ofCustomItems:" + this.ofCustomItems);
            printWriter.println("ofCustomSky:" + this.ofCustomSky);
            printWriter.println("ofShowCapes:" + this.ofShowCapes);
            printWriter.println("ofNaturalTextures:" + this.ofNaturalTextures);
            printWriter.println("ofEmissiveTextures:" + this.ofEmissiveTextures);
            printWriter.println("ofLazyChunkLoading:" + this.ofLazyChunkLoading);
            printWriter.println("ofRenderRegions:" + this.ofRenderRegions);
            printWriter.println("ofSmartAnimations:" + this.ofSmartAnimations);
            printWriter.println("ofDynamicFov:" + this.ofDynamicFov);
            printWriter.println("ofAlternateBlocks:" + this.ofAlternateBlocks);
            printWriter.println("ofDynamicLights:" + this.ofDynamicLights);
            printWriter.println("ofScreenshotSize:" + this.ofScreenshotSize);
            printWriter.println("ofCustomEntityModels:" + this.ofCustomEntityModels);
            printWriter.println("ofCustomGuis:" + this.ofCustomGuis);
            printWriter.println("ofShowGlErrors:" + this.ofShowGlErrors);
            printWriter.println("ofFullscreenMode:" + this.ofFullscreenMode);
            printWriter.println("ofFastMath:" + this.ofFastMath);
            printWriter.println("ofFastRender:" + this.ofFastRender);
            printWriter.println("ofEntityCulling:" + this.ofEntityCulling);
            printWriter.println("ofCullArmorStands:" + this.ofCullArmorStands);
            printWriter.println("ofCullInterpolateCamera:" + this.ofCullInterpolateCamera);
            printWriter.println("ofCullTileBounds:" + this.ofCullTileBounds);
            printWriter.println("ofCullLoadedChunks:" + this.ofCullLoadedChunks);
            printWriter.println("ofLimitChunkUpdates:" + this.ofLimitChunkUpdates);
            printWriter.println("ofCullTracingDistance:" + this.ofCullTracingDistance);
            printWriter.println("ofCullSleepDelay:" + this.ofCullSleepDelay);
            printWriter.println("ofCullHitboxLimit:" + this.ofCullHitboxLimit);
            printWriter.println("ofChunkUpdateLimit:" + this.ofChunkUpdateLimit);
            printWriter.println("ofParticlesLimit:" + this.ofParticlesLimit);
            printWriter.println("ofTranslucentBlocks:" + this.ofTranslucentBlocks);
            printWriter.println("key_" + this.ofKeyBindZoom.getKeyDescription() + ":" + this.ofKeyBindZoom.getKeyCode());
            printWriter.close();
        }
        catch (Exception exception)
        {
            Config.warn("Failed to save options");
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    private void updateRenderClouds()
    {
        switch (this.ofClouds)
        {
            case 1:
                this.clouds = 1;
                break;

            case 2:
                this.clouds = 2;
                break;

            case 3:
                this.clouds = 0;
                break;

            default:
                if (this.fancyGraphics)
                {
                    this.clouds = 2;
                }
                else
                {
                    this.clouds = 1;
                }
        }
    }

    public void resetSettings()
    {
        this.renderDistanceChunks = 8;
        this.viewBobbing = true;
        this.anaglyph = false;
        this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        this.enableVsync = false;
        this.updateVSync();
        this.mipmapLevels = 4;
        this.fancyGraphics = true;
        this.ambientOcclusion = 2;
        this.clouds = 2;
        this.fovSetting = 70.0F;
        this.gammaSetting = 0.0F;
        this.guiScale = 0;
        this.particleSetting = 0;
        this.heldItemTooltips = true;
        this.useVbo = true;
        this.forceUnicodeFont = false;
        this.ofFogType = 1;
        this.ofFogStart = 0.8F;
        this.ofMipmapType = 0;
        this.ofOcclusionFancy = false;
        this.ofSmartAnimations = false;
        this.ofSmoothFps = false;
        Config.updateAvailableProcessors();
        this.ofSmoothWorld = Config.isSingleProcessor();
        this.ofLazyChunkLoading = false;
        this.ofRenderRegions = false;
        this.ofFastMath = "none";
        this.ofEntityCulling = true;
        this.ofCullArmorStands = true;
        this.ofCullInterpolateCamera = true;
        this.ofCullTileBounds = true;
        this.ofCullLoadedChunks = true;
        this.ofLimitChunkUpdates = true;
        this.ofCullTracingDistance = 128;
        this.ofCullSleepDelay = 10;
        this.ofCullHitboxLimit = 50;
        this.ofChunkUpdateLimit = 50;
        this.ofParticlesLimit = 400;
        this.ofFastRender = false;
        this.ofTranslucentBlocks = 0;
        this.ofDynamicFov = true;
        this.ofAlternateBlocks = true;
        this.ofDynamicLights = 3;
        this.ofScreenshotSize = 1;
        this.ofCustomEntityModels = true;
        this.ofCustomGuis = true;
        this.ofShowGlErrors = true;
        this.ofAoLevel = 1.0F;
        this.ofAaLevel = 0;
        this.ofAfLevel = 1;
        this.ofClouds = 0;
        this.ofCloudsHeight = 0.0F;
        this.ofTrees = 0;
        this.ofRain = 0;
        this.ofBetterGrass = 3;
        this.ofAutoSaveTicks = 4000;
        this.ofLagometer = false;
        this.ofShowFps = false;
        this.ofProfiler = false;
        this.ofWeather = true;
        this.ofSky = true;
        this.ofStars = true;
        this.ofSunMoon = true;
        this.ofVignette = 0;
        this.ofChunkUpdates = 1;
        this.ofChunkUpdatesDynamic = false;
        this.ofTime = 0;
        this.ofClearWater = false;
        this.ofBetterSnow = false;
        this.ofFullscreenMode = "Default";
        this.ofSwampColors = true;
        this.ofRandomEntities = true;
        this.ofSmoothBiomes = true;
        this.ofCustomFonts = true;
        this.ofCustomColors = true;
        this.ofCustomItems = true;
        this.ofCustomSky = true;
        this.ofShowCapes = true;
        this.ofConnectedTextures = 2;
        this.ofNaturalTextures = false;
        this.ofEmissiveTextures = true;
        this.ofAnimatedWater = 0;
        this.ofAnimatedLava = 0;
        this.ofAnimatedFire = true;
        this.ofAnimatedPortal = true;
        this.ofAnimatedRedstone = true;
        this.ofAnimatedExplosion = true;
        this.ofAnimatedFlame = true;
        this.ofAnimatedSmoke = true;
        this.ofVoidParticles = true;
        this.ofWaterParticles = true;
        this.ofRainSplash = true;
        this.ofPortalParticles = true;
        this.ofPotionParticles = true;
        this.ofFireworkParticles = true;
        this.ofDrippingWaterLava = true;
        this.ofAnimatedTerrain = true;
        this.ofAnimatedTextures = true;
        Shaders.setShaderPack("OFF");
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        this.updateWaterOpacity();
        this.mc.refreshResources();
        this.saveOptions();
    }

    public void updateVSync()
    {
        GameWindow.setVsync(this.enableVsync);
    }

    private void updateWaterOpacity()
    {
        if (Config.isIntegratedServerRunning())
        {
            Config.waterOpacityChanged = true;
        }

        ClearWater.updateWaterOpacity(this, this.mc.theWorld);
    }

    public void setAllAnimations(boolean enabled)
    {
        int i = enabled ? 0 : 2;
        this.ofAnimatedWater = i;
        this.ofAnimatedLava = i;
        this.ofAnimatedFire = enabled;
        this.ofAnimatedPortal = enabled;
        this.ofAnimatedRedstone = enabled;
        this.ofAnimatedExplosion = enabled;
        this.ofAnimatedFlame = enabled;
        this.ofAnimatedSmoke = enabled;
        this.ofVoidParticles = enabled;
        this.ofWaterParticles = enabled;
        this.ofRainSplash = enabled;
        this.ofPortalParticles = enabled;
        this.ofPotionParticles = enabled;
        this.ofFireworkParticles = enabled;
        this.particleSetting = enabled ? 0 : 2;
        this.ofDrippingWaterLava = enabled;
        this.ofAnimatedTerrain = enabled;
        this.ofAnimatedTextures = enabled;
    }

    private static int nextValue(int value, int[] values)
    {
        int i = indexOf(value, values);

        if (i < 0)
        {
            return values[0];
        }
        else
        {
            ++i;

            if (i >= values.length)
            {
                i = 0;
            }

            return values[i];
        }
    }

    private static int limit(int value, int[] values)
    {
        int i = indexOf(value, values);
        return i < 0 ? values[0] : value;
    }

    private static int indexOf(int value, int[] values)
    {
        for (int i = 0; i < values.length; ++i)
        {
            if (values[i] == value)
            {
                return i;
            }
        }

        return -1;
    }

    public static enum Options
    {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 0.0F, 260.0F, 5.0F),
        FBO_ENABLE("options.fboEnable", false, true),
        RENDER_CLOUDS("options.renderClouds", false, false),
        GRAPHICS("options.graphics", false, false),
        AMBIENT_OCCLUSION("options.ao", false, false),
        GUI_SCALE("options.guiScale", false, false),
        PARTICLES("options.particles", false, false),
        CHAT_VISIBILITY("options.chat.visibility", false, false),
        CHAT_COLOR("options.chat.color", false, true),
        CHAT_LINKS("options.chat.links", false, true),
        CHAT_OPACITY("options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        TOUCHSCREEN("options.touchscreen", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true),
        FOG_FANCY("of.options.FOG_FANCY", false, false),
        FOG_START("of.options.FOG_START", false, false),
        MIPMAP_TYPE("of.options.MIPMAP_TYPE", true, false, 0.0F, 3.0F, 1.0F),
        SMOOTH_FPS("of.options.SMOOTH_FPS", false, false),
        CLOUDS("of.options.CLOUDS", false, false),
        CLOUD_HEIGHT("of.options.CLOUD_HEIGHT", true, false),
        TREES("of.options.TREES", false, false),
        RAIN("of.options.RAIN", false, false),
        ANIMATED_WATER("of.options.ANIMATED_WATER", false, false),
        ANIMATED_LAVA("of.options.ANIMATED_LAVA", false, false),
        ANIMATED_FIRE("of.options.ANIMATED_FIRE", false, false),
        ANIMATED_PORTAL("of.options.ANIMATED_PORTAL", false, false),
        AO_LEVEL("of.options.AO_LEVEL", true, false),
        LAGOMETER("of.options.LAGOMETER", false, false),
        SHOW_FPS("of.options.SHOW_FPS", false, false),
        AUTOSAVE_TICKS("of.options.AUTOSAVE_TICKS", false, false),
        BETTER_GRASS("of.options.BETTER_GRASS", false, false),
        ANIMATED_REDSTONE("of.options.ANIMATED_REDSTONE", false, false),
        ANIMATED_EXPLOSION("of.options.ANIMATED_EXPLOSION", false, false),
        ANIMATED_FLAME("of.options.ANIMATED_FLAME", false, false),
        ANIMATED_SMOKE("of.options.ANIMATED_SMOKE", false, false),
        WEATHER("of.options.WEATHER", false, false),
        SKY("of.options.SKY", false, false),
        STARS("of.options.STARS", false, false),
        SUN_MOON("of.options.SUN_MOON", false, false),
        VIGNETTE("of.options.VIGNETTE", false, false),
        CHUNK_UPDATES("of.options.CHUNK_UPDATES", false, false),
        CHUNK_UPDATES_DYNAMIC("of.options.CHUNK_UPDATES_DYNAMIC", false, false),
        TIME("of.options.TIME", false, false),
        CLEAR_WATER("of.options.CLEAR_WATER", false, false),
        SMOOTH_WORLD("of.options.SMOOTH_WORLD", false, false),
        VOID_PARTICLES("of.options.VOID_PARTICLES", false, false),
        WATER_PARTICLES("of.options.WATER_PARTICLES", false, false),
        RAIN_SPLASH("of.options.RAIN_SPLASH", false, false),
        PORTAL_PARTICLES("of.options.PORTAL_PARTICLES", false, false),
        POTION_PARTICLES("of.options.POTION_PARTICLES", false, false),
        FIREWORK_PARTICLES("of.options.FIREWORK_PARTICLES", false, false),
        PROFILER("of.options.PROFILER", false, false),
        DRIPPING_WATER_LAVA("of.options.DRIPPING_WATER_LAVA", false, false),
        BETTER_SNOW("of.options.BETTER_SNOW", false, false),
        FULLSCREEN_MODE("of.options.FULLSCREEN_MODE", true, false, 0.0F, (float)Config.getDisplayModes().length, 1.0F),
        ANIMATED_TERRAIN("of.options.ANIMATED_TERRAIN", false, false),
        SWAMP_COLORS("of.options.SWAMP_COLORS", false, false),
        RANDOM_ENTITIES("of.options.RANDOM_ENTITIES", false, false),
        SMOOTH_BIOMES("of.options.SMOOTH_BIOMES", false, false),
        CUSTOM_FONTS("of.options.CUSTOM_FONTS", false, false),
        CUSTOM_COLORS("of.options.CUSTOM_COLORS", false, false),
        SHOW_CAPES("of.options.SHOW_CAPES", false, false),
        CONNECTED_TEXTURES("of.options.CONNECTED_TEXTURES", false, false),
        CUSTOM_ITEMS("of.options.CUSTOM_ITEMS", false, false),
        AA_LEVEL("of.options.AA_LEVEL", true, false, 0.0F, 16.0F, 1.0F),
        AF_LEVEL("of.options.AF_LEVEL", true, false, 1.0F, 16.0F, 1.0F),
        ANIMATED_TEXTURES("of.options.ANIMATED_TEXTURES", false, false),
        NATURAL_TEXTURES("of.options.NATURAL_TEXTURES", false, false),
        EMISSIVE_TEXTURES("of.options.EMISSIVE_TEXTURES", false, false),
        HELD_ITEM_TOOLTIPS("of.options.HELD_ITEM_TOOLTIPS", false, false),
        DROPPED_ITEMS("of.options.DROPPED_ITEMS", false, false),
        LAZY_CHUNK_LOADING("of.options.LAZY_CHUNK_LOADING", false, false),
        CUSTOM_SKY("of.options.CUSTOM_SKY", false, false),
        FAST_MATH("Fast Math", false, false),
        FAST_RENDER("of.options.FAST_RENDER", false, false),
        TRANSLUCENT_BLOCKS("of.options.TRANSLUCENT_BLOCKS", false, false),
        DYNAMIC_FOV("of.options.DYNAMIC_FOV", false, false),
        DYNAMIC_LIGHTS("of.options.DYNAMIC_LIGHTS", false, false),
        ALTERNATE_BLOCKS("of.options.ALTERNATE_BLOCKS", false, false),
        CUSTOM_ENTITY_MODELS("of.options.CUSTOM_ENTITY_MODELS", false, false),
        ADVANCED_TOOLTIPS("of.options.ADVANCED_TOOLTIPS", false, false),
        SCREENSHOT_SIZE("of.options.SCREENSHOT_SIZE", false, false),
        CUSTOM_GUIS("of.options.CUSTOM_GUIS", false, false),
        RENDER_REGIONS("of.options.RENDER_REGIONS", false, false),
        SHOW_GL_ERRORS("of.options.SHOW_GL_ERRORS", false, false),
        SMART_ANIMATIONS("of.options.SMART_ANIMATIONS", false, false),
        ENTITY_CULLING("Entity Culling", false, true),
        CULL_ARMOR_STANDS("Cull Armor Stands", false, true),
        CULL_INTERPOLATE_CAMERA("Cull Interpolate Camera", false, true),
        CULL_TILE_BOUNDS("Cull Tile Bounds", false, true),
        CULL_LOADED_CHUNKS("Cull Loaded Chunks", false, true),
        LIMIT_CHUNK_UPDATES("Limit Chunk Updates", false, true),
        CULL_TRACING_DISTANCE("Cull Tracing Distance", true, false, 64.0F, 256.0F, 1.0F),
        CULL_SLEEP_DELAY("Cull Sleep Delay", true, false, 5.0F, 20.0F, 1.0F),
        CULL_HITBOX_LIMIT("Cull Hitbox Limit", true, false, 30.0F, 80.0F, 1.0F),
        CHUNK_UPDATE_LIMIT("Chunk Update Limit", true, false, 1.0F, 250.0F, 1.0F),
        PARTICLES_LIMIT("Particles Limit", true, false, 1.0F, 2000.0F, 1.0F);

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private float valueMin;
        private float valueMax;
        private static final GameSettings.Options[] VALUES = values();

        public static GameSettings.Options getEnumOptions(int ordinal)
        {
            return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : null;
        }

        private Options(String str, boolean isFloat, boolean isBoolean)
        {
            this(str, isFloat, isBoolean, 0.0F, 1.0F, 0.0F);
        }

        private Options(String str, boolean isFloat, boolean isBoolean, float valMin, float valMax, float valStep)
        {
            this.enumString = str;
            this.enumFloat = isFloat;
            this.enumBoolean = isBoolean;
            this.valueMin = valMin;
            this.valueMax = valMax;
            this.valueStep = valStep;
        }

        public boolean getEnumFloat()
        {
            return this.enumFloat;
        }

        public boolean getEnumBoolean()
        {
            return this.enumBoolean;
        }

        public int returnEnumOrdinal()
        {
            return this.ordinal();
        }

        public String getEnumString()
        {
            return this.enumString;
        }

        public float getValueMax()
        {
            return this.valueMax;
        }

        public void setValueMax(float value)
        {
            this.valueMax = value;
        }

        public float normalizeValue(float value)
        {
            return MathHelper.clamp_float((this.snapToStepClamp(value) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float value)
        {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(value, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float value)
        {
            value = this.snapToStep(value);
            return MathHelper.clamp_float(value, this.valueMin, this.valueMax);
        }

        protected float snapToStep(float value)
        {
            if (this.valueStep > 0.0F)
            {
                value = this.valueStep * (float)Math.round(value / this.valueStep);
            }

            return value;
        }
    }
}
