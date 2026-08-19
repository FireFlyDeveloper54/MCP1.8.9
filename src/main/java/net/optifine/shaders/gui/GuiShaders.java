package net.optifine.shaders.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderEnumShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.config.EnumShaderOption;
import org.lwjgl.Sys;

public class GuiShaders extends GuiScreenOF
{
    protected GuiScreen parentGui;
    protected String screenTitle = "Shaders";
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderEnumShaderOptions());
    private int updateTimer = -1;
    private GuiSlotShaders shaderList;
    private boolean saved = false;
    private static float[] QUALITY_MULTIPLIERS = new float[] {0.5F, 0.6F, 0.6666667F, 0.75F, 0.8333333F, 0.9F, 1.0F, 1.1666666F, 1.3333334F, 1.5F, 1.6666666F, 1.8F, 2.0F};
    private static String[] QUALITY_MULTIPLIER_NAMES = new String[] {"0.5x", "0.6x", "0.66x", "0.75x", "0.83x", "0.9x", "1x", "1.16x", "1.33x", "1.5x", "1.66x", "1.8x", "2x"};
    private static float QUALITY_MULTIPLIER_DEFAULT = 1.0F;
    private static float[] HAND_DEPTH_VALUES = new float[] {0.0625F, 0.125F, 0.25F};
    private static String[] HAND_DEPTH_NAMES = new String[] {"0.5x", "1x", "2x"};
    private static float HAND_DEPTH_DEFAULT = 0.125F;
    public static final int EnumOS_UNKNOWN = 0;
    public static final int EnumOS_WINDOWS = 1;
    public static final int EnumOS_OSX = 2;
    public static final int EnumOS_SOLARIS = 3;
    public static final int EnumOS_LINUX = 4;

    public GuiShaders(GuiScreen parentGui, GameSettings gameSettings)
    {
        this.parentGui = parentGui;
    }

    public void initGui()
    {
        this.screenTitle = I18n.format("of.options.shadersTitle", new Object[0]);

        if (Shaders.shadersConfig == null)
        {
            Shaders.loadConfig();
        }

        int buttonWidth = 120;
        int buttonHeight = 20;
        int rightColumnX = this.width - buttonWidth - 10;
        int topY = 30;
        int rowHeight = 20;
        int listWidth = this.width - buttonWidth - 20;
        this.shaderList = new GuiSlotShaders(this, listWidth, this.height, topY, this.height - 50, 16);
        this.shaderList.registerScrollButtons(7, 8);
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.ANTIALIASING, rightColumnX, 0 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.NORMAL_MAP, rightColumnX, 1 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.SPECULAR_MAP, rightColumnX, 2 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.RENDER_RES_MUL, rightColumnX, 3 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.SHADOW_RES_MUL, rightColumnX, 4 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.HAND_DEPTH_MUL, rightColumnX, 5 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_HAND_LIGHT, rightColumnX, 6 * rowHeight + topY, buttonWidth, buttonHeight));
        this.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_LIGHTING, rightColumnX, 7 * rowHeight + topY, buttonWidth, buttonHeight));
        int bottomButtonWidth = Math.min(150, listWidth / 2 - 10);
        int folderButtonX = listWidth / 4 - bottomButtonWidth / 2;
        int bottomButtonY = this.height - 25;
        this.buttonList.add(new GuiButton(201, folderButtonX, bottomButtonY, bottomButtonWidth - 22 + 1, buttonHeight, Lang.get("of.options.shaders.shadersFolder")));
        this.buttonList.add(new GuiButtonDownloadShaders(210, folderButtonX + bottomButtonWidth - 22 - 1, bottomButtonY));
        this.buttonList.add(new GuiButton(202, listWidth / 4 * 3 - bottomButtonWidth / 2, this.height - 25, bottomButtonWidth, buttonHeight, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(203, rightColumnX, this.height - 25, buttonWidth, buttonHeight, Lang.get("of.options.shaders.shaderOptions")));
        this.updateButtons();
    }

    public void updateButtons()
    {
        boolean shadersEnabled = Config.isShaders();

        for (GuiButton guiButton : this.buttonList)
        {
            if (guiButton.id != 201 && guiButton.id != 202 && guiButton.id != 210 && guiButton.id != EnumShaderOption.ANTIALIASING.ordinal())
            {
                guiButton.enabled = shadersEnabled;
            }
        }
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.shaderList.handleMouseInput();
    }

    protected void actionPerformed(GuiButton button)
    {
        this.actionPerformed(button, false);
    }

    protected void actionPerformedRightClick(GuiButton button)
    {
        this.actionPerformed(button, true);
    }

    private void actionPerformed(GuiButton button, boolean rightClick)
    {
        if (button.enabled)
        {
            if (!(button instanceof GuiButtonEnumShaderOption))
            {
                if (!rightClick)
                {
                    switch (button.id)
                    {
                        case 201:
                            switch (getOSType())
                            {
                                case 1:
                                    String openCommand = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[] {Shaders.shaderPacksDir.getAbsolutePath()});

                                    try
                                    {
                                        Runtime.getRuntime().exec(openCommand);
                                        return;
                                    }
                                    catch (IOException openCommandException)
                                    {
                                        net.minecraft.src.Config.warn(openCommandException.getClass().getName() + ": " + openCommandException.getMessage(), openCommandException);
                                        break;
                                    }

                                case 2:
                                    try
                                    {
                                        Runtime.getRuntime().exec(new String[] {"/usr/bin/open", Shaders.shaderPacksDir.getAbsolutePath()});
                                        return;
                                    }
                                    catch (IOException openException)
                                    {
                                        net.minecraft.src.Config.warn(openException.getClass().getName() + ": " + openException.getMessage(), openException);
                                    }
                            }

                            boolean fallbackToSystemOpen = false;

                            try
                            {
                                Class desktopClass = Class.forName("java.awt.Desktop");
                                Object desktop = desktopClass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
                                desktopClass.getMethod("browse", new Class[] {URI.class}).invoke(desktop, new Object[] {(new File(this.mc.mcDataDir, "shaderpacks")).toURI()});
                            }
                            catch (Throwable desktopError)
                            {
                                net.minecraft.src.Config.warn(desktopError.getClass().getName() + ": " + desktopError.getMessage(), desktopError);
                                fallbackToSystemOpen = true;
                            }

                            if (fallbackToSystemOpen)
                            {
                                Config.dbg("Opening via system class!");
                                Sys.openURL("file://" + Shaders.shaderPacksDir.getAbsolutePath());
                            }

                            break;

                        case 202:
                            Shaders.storeConfig();
                            this.saved = true;
                            this.mc.displayGuiScreen(this.parentGui);
                            break;

                        case 203:
                            GuiShaderOptions shaderOptionsScreen = new GuiShaderOptions(this, Config.getGameSettings());
                            Config.getMinecraft().displayGuiScreen(shaderOptionsScreen);
                            break;

                        case 210:
                            try
                            {
                                Class<?> desktopClass = Class.forName("java.awt.Desktop");
                                Object desktop = desktopClass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
                                desktopClass.getMethod("browse", new Class[] {URI.class}).invoke(desktop, new Object[] {new URI("http://optifine.net/shaderPacks")});
                            }
                            catch (Throwable desktopError)
                            {
                                net.minecraft.src.Config.warn(desktopError.getClass().getName() + ": " + desktopError.getMessage(), desktopError);
                            }

                            break;

                        case 204:
                        case 205:
                        case 206:
                        case 207:
                        case 208:
                        case 209:
                        default:
                            this.shaderList.actionPerformed(button);
                    }
                }
            }
            else
            {
                GuiButtonEnumShaderOption shaderOptionButton = (GuiButtonEnumShaderOption)button;

                switch (shaderOptionButton.getEnumShaderOption())
                {
                    case ANTIALIASING:
                        Shaders.nextAntialiasingLevel(!rightClick);

                        if (this.hasShiftDown())
                        {
                            Shaders.configAntialiasingLevel = 0;
                        }

                        Shaders.uninit();
                        break;

                    case NORMAL_MAP:
                        Shaders.configNormalMap = !Shaders.configNormalMap;

                        if (this.hasShiftDown())
                        {
                            Shaders.configNormalMap = true;
                        }

                        Shaders.uninit();
                        this.mc.scheduleResourcesRefresh();
                        break;

                    case SPECULAR_MAP:
                        Shaders.configSpecularMap = !Shaders.configSpecularMap;

                        if (this.hasShiftDown())
                        {
                            Shaders.configSpecularMap = true;
                        }

                        Shaders.uninit();
                        this.mc.scheduleResourcesRefresh();
                        break;

                    case RENDER_RES_MUL:
                        Shaders.configRenderResMul = this.getNextValue(Shaders.configRenderResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, this.hasShiftDown());
                        Shaders.uninit();
                        Shaders.scheduleResize();
                        break;

                    case SHADOW_RES_MUL:
                        Shaders.configShadowResMul = this.getNextValue(Shaders.configShadowResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, this.hasShiftDown());
                        Shaders.uninit();
                        Shaders.scheduleResizeShadow();
                        break;

                    case HAND_DEPTH_MUL:
                        Shaders.configHandDepthMul = this.getNextValue(Shaders.configHandDepthMul, HAND_DEPTH_VALUES, HAND_DEPTH_DEFAULT, !rightClick, this.hasShiftDown());
                        Shaders.uninit();
                        break;

                    case OLD_HAND_LIGHT:
                        Shaders.configOldHandLight.nextValue(!rightClick);

                        if (this.hasShiftDown())
                        {
                            Shaders.configOldHandLight.resetValue();
                        }

                        Shaders.uninit();
                        break;

                    case OLD_LIGHTING:
                        Shaders.configOldLighting.nextValue(!rightClick);

                        if (this.hasShiftDown())
                        {
                            Shaders.configOldLighting.resetValue();
                        }

                        Shaders.updateBlockLightLevel();
                        Shaders.uninit();
                        this.mc.scheduleResourcesRefresh();
                        break;

                    case TWEAK_BLOCK_DAMAGE:
                        Shaders.configTweakBlockDamage = !Shaders.configTweakBlockDamage;
                        break;

                    case CLOUD_SHADOW:
                        Shaders.configCloudShadow = !Shaders.configCloudShadow;
                        break;

                    case TEX_MIN_FIL_B:
                        Shaders.configTexMinFilB = (Shaders.configTexMinFilB + 1) % 3;
                        Shaders.configTexMinFilN = Shaders.configTexMinFilS = Shaders.configTexMinFilB;
                        button.displayString = "Tex Min: " + Shaders.texMinFilDesc[Shaders.configTexMinFilB];
                        ShadersTex.updateTextureMinMagFilter();
                        break;

                    case TEX_MAG_FIL_N:
                        Shaders.configTexMagFilN = (Shaders.configTexMagFilN + 1) % 2;
                        button.displayString = "Tex_n Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilN];
                        ShadersTex.updateTextureMinMagFilter();
                        break;

                    case TEX_MAG_FIL_S:
                        Shaders.configTexMagFilS = (Shaders.configTexMagFilS + 1) % 2;
                        button.displayString = "Tex_s Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilS];
                        ShadersTex.updateTextureMinMagFilter();
                        break;

                    case SHADOW_CLIP_FRUSTRUM:
                        Shaders.configShadowClipFrustrum = !Shaders.configShadowClipFrustrum;
                        button.displayString = "ShadowClipFrustrum: " + toStringOnOff(Shaders.configShadowClipFrustrum);
                        ShadersTex.updateTextureMinMagFilter();
                }

                shaderOptionButton.updateButtonText();
            }
        }
    }

    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (!this.saved)
        {
            Shaders.storeConfig();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.shaderList.drawScreen(mouseX, mouseY, partialTicks);

        if (this.updateTimer <= 0)
        {
            this.shaderList.updateList();
            this.updateTimer += 20;
        }

        this.drawCenteredString(this.fontRendererObj, this.screenTitle + " ", this.width / 2, 15, 16777215);
        String glInfo = "OpenGL: " + Shaders.glVersionString + ", " + Shaders.glVendorString + ", " + Shaders.glRendererString;
        int glInfoWidth = this.fontRendererObj.getStringWidth(glInfo);

        if (glInfoWidth < this.width - 5)
        {
            this.drawCenteredString(this.fontRendererObj, glInfo, this.width / 2, this.height - 40, 8421504);
        }
        else
        {
            this.drawString(this.fontRendererObj, glInfo, 5, this.height - 40, 8421504);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.tooltipManager.drawTooltips(mouseX, mouseY, this.buttonList);
    }

    public void updateScreen()
    {
        super.updateScreen();
        --this.updateTimer;
    }

    public Minecraft getMc()
    {
        return this.mc;
    }

    public void drawCenteredString(String text, int x, int y, int color)
    {
        this.drawCenteredString(this.fontRendererObj, text, x, y, color);
    }

    public static String toStringOnOff(boolean value)
    {
        String onText = Lang.getOn();
        String offText = Lang.getOff();
        return value ? onText : offText;
    }

    public static String toStringAa(int value)
    {
        return value == 2 ? "FXAA 2x" : (value == 4 ? "FXAA 4x" : Lang.getOff());
    }

    public static String toStringValue(float val, float[] values, String[] names)
    {
        int valueIndex = getValueIndex(val, values);
        return names[valueIndex];
    }

    private float getNextValue(float val, float[] values, float valDef, boolean forward, boolean reset)
    {
        if (reset)
        {
            return valDef;
        }
        else
        {
            int valueIndex = getValueIndex(val, values);

            if (forward)
            {
                ++valueIndex;

                if (valueIndex >= values.length)
                {
                    valueIndex = 0;
                }
            }
            else
            {
                --valueIndex;

                if (valueIndex < 0)
                {
                    valueIndex = values.length - 1;
                }
            }

            return values[valueIndex];
        }
    }

    public static int getValueIndex(float val, float[] values)
    {
        for (int valueIndex = 0; valueIndex < values.length; ++valueIndex)
        {
            float candidateValue = values[valueIndex];

            if (candidateValue >= val)
            {
                return valueIndex;
            }
        }

        return values.length - 1;
    }

    public static String toStringQuality(float val)
    {
        return toStringValue(val, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_NAMES);
    }

    public static String toStringHandDepth(float val)
    {
        return toStringValue(val, HAND_DEPTH_VALUES, HAND_DEPTH_NAMES);
    }

    public static int getOSType()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win") ? 1 : (osName.contains("mac") ? 2 : (osName.contains("solaris") ? 3 : (osName.contains("sunos") ? 3 : (osName.contains("linux") ? 4 : (osName.contains("unix") ? 4 : 0)))));
    }

    public boolean hasShiftDown()
    {
        return isShiftKeyDown();
    }
}
