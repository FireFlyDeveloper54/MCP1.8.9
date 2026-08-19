package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiAnimationSettingsOF;
import net.optifine.gui.GuiDetailSettingsOF;
import net.optifine.gui.GuiOptionButtonOF;
import net.optifine.gui.GuiOptionSliderOF;
import net.optifine.gui.GuiOtherSettingsOF;
import net.optifine.gui.GuiPerformanceSettingsOF;
import net.optifine.gui.GuiQualitySettingsOF;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderOptions;
import net.optifine.shaders.gui.GuiShaders;

public class GuiVideoSettings extends GuiScreenOF
{
    private GuiScreen parentGuiScreen;
    protected String screenTitle = "Video Settings";
    private GameSettings guiGameSettings;
    private static GameSettings.Options[] videoOptions = new GameSettings.Options[] {GameSettings.Options.GRAPHICS, GameSettings.Options.RENDER_DISTANCE, GameSettings.Options.AMBIENT_OCCLUSION, GameSettings.Options.FRAMERATE_LIMIT, GameSettings.Options.AO_LEVEL, GameSettings.Options.VIEW_BOBBING, GameSettings.Options.GUI_SCALE, GameSettings.Options.USE_VBO, GameSettings.Options.GAMMA, GameSettings.Options.BLOCK_ALTERNATIVES, GameSettings.Options.DYNAMIC_LIGHTS, GameSettings.Options.DYNAMIC_FOV};
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiVideoSettings(GuiScreen parentScreenIn, GameSettings gameSettingsIn)
    {
        this.parentGuiScreen = parentScreenIn;
        this.guiGameSettings = gameSettingsIn;
    }

    public void initGui()
    {
        this.screenTitle = I18n.format("options.videoTitle", new Object[0]);
        this.buttonList.clear();

        for (int i = 0; i < videoOptions.length; ++i)
        {
            GameSettings.Options gamesettings$options = videoOptions[i];

            if (gamesettings$options != null)
            {
                int j = this.width / 2 - 155 + i % 2 * 160;
                int k = this.height / 6 + 21 * (i / 2) - 12;

                if (gamesettings$options.getEnumFloat())
                {
                    this.buttonList.add(new GuiOptionSliderOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options));
                }
                else
                {
                    this.buttonList.add(new GuiOptionButtonOF(gamesettings$options.returnEnumOrdinal(), j, k, gamesettings$options, this.guiGameSettings.getKeyBinding(gamesettings$options)));
                }
            }
        }

        int buttonY = this.height / 6 + 21 * (videoOptions.length / 2) - 12;
        int buttonX = 0;
        buttonX = this.width / 2 - 155 + 0;
        this.buttonList.add(new GuiOptionButton(231, buttonX, buttonY, Lang.get("of.options.shaders")));
        buttonX = this.width / 2 - 155 + 160;
        this.buttonList.add(new GuiOptionButton(202, buttonX, buttonY, Lang.get("of.options.quality")));
        buttonY = buttonY + 21;
        buttonX = this.width / 2 - 155 + 0;
        this.buttonList.add(new GuiOptionButton(201, buttonX, buttonY, Lang.get("of.options.details")));
        buttonX = this.width / 2 - 155 + 160;
        this.buttonList.add(new GuiOptionButton(212, buttonX, buttonY, Lang.get("of.options.performance")));
        buttonY = buttonY + 21;
        buttonX = this.width / 2 - 155 + 0;
        this.buttonList.add(new GuiOptionButton(211, buttonX, buttonY, Lang.get("of.options.animations")));
        buttonX = this.width / 2 - 155 + 160;
        this.buttonList.add(new GuiOptionButton(222, buttonX, buttonY, Lang.get("of.options.other")));
        buttonY = buttonY + 21;
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        this.actionPerformed(button, 1);
    }

    protected void actionPerformedRightClick(GuiButton button)
    {
        if (button.id == GameSettings.Options.GUI_SCALE.ordinal())
        {
            this.actionPerformed(button, -1);
        }
    }

    private void actionPerformed(GuiButton button, int step)
    {
        if (button.enabled)
        {
            int i = this.guiGameSettings.guiScale;

            if (button.id < 200 && button instanceof GuiOptionButton)
            {
                this.guiGameSettings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), step);
                button.displayString = this.guiGameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
            }

            if (button.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }

            if (this.guiGameSettings.guiScale != i)
            {
                ScaledResolution scaledResolution = new ScaledResolution(this.mc);
                int j = scaledResolution.getScaledWidth();
                int k = scaledResolution.getScaledHeight();
                this.setWorldAndResolution(this.mc, j, k);
            }

            if (button.id == 201)
            {
                this.mc.gameSettings.saveOptions();
                GuiDetailSettingsOF guiDetailSettingsOF = new GuiDetailSettingsOF(this, this.guiGameSettings);
                this.mc.displayGuiScreen(guiDetailSettingsOF);
            }

            if (button.id == 202)
            {
                this.mc.gameSettings.saveOptions();
                GuiQualitySettingsOF guiQualitySettingsOF = new GuiQualitySettingsOF(this, this.guiGameSettings);
                this.mc.displayGuiScreen(guiQualitySettingsOF);
            }

            if (button.id == 211)
            {
                this.mc.gameSettings.saveOptions();
                GuiAnimationSettingsOF guiAnimationSettingsOF = new GuiAnimationSettingsOF(this, this.guiGameSettings);
                this.mc.displayGuiScreen(guiAnimationSettingsOF);
            }

            if (button.id == 212)
            {
                this.mc.gameSettings.saveOptions();
                GuiPerformanceSettingsOF guiPerformanceSettingsOF = new GuiPerformanceSettingsOF(this, this.guiGameSettings);
                this.mc.displayGuiScreen(guiPerformanceSettingsOF);
            }

            if (button.id == 222)
            {
                this.mc.gameSettings.saveOptions();
                GuiOtherSettingsOF guiOtherSettingsOF = new GuiOtherSettingsOF(this, this.guiGameSettings);
                this.mc.displayGuiScreen(guiOtherSettingsOF);
            }

            if (button.id == 231)
            {
                if (Config.isAntialiasing() || Config.isAntialiasingConfigured())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                    return;
                }

                if (Config.isAnisotropicFiltering())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.af1"), Lang.get("of.message.shaders.af2"));
                    return;
                }

                if (Config.isFastRender())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.fr1"), Lang.get("of.message.shaders.fr2"));
                    return;
                }

                if (Config.getGameSettings().anaglyph)
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.an1"), Lang.get("of.message.shaders.an2"));
                    return;
                }

                this.mc.gameSettings.saveOptions();
                GuiShaders guiShaders = new GuiShaders(this, this.guiGameSettings);
                this.mc.displayGuiScreen(guiShaders);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        String optifineVersionText = Config.getVersion();
        String edition = "HD_U";

        if (edition.equals("HD"))
        {
            optifineVersionText = "OptiFine HD M6_pre2";
        }

        if (edition.equals("HD_U"))
        {
            optifineVersionText = "OptiFine HD M6_pre2 Ultra";
        }

        if (edition.equals("L"))
        {
            optifineVersionText = "OptiFine M6_pre2 Light";
        }

        this.drawString(this.fontRendererObj, optifineVersionText, 2, this.height - 10, 8421504);
        String minecraftVersionText = "Minecraft 1.8.9";
        int minecraftVersionWidth = this.fontRendererObj.getStringWidth(minecraftVersionText);
        this.drawString(this.fontRendererObj, minecraftVersionText, this.width - minecraftVersionWidth - 2, this.height - 10, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.tooltipManager.drawTooltips(mouseX, mouseY, this.buttonList);
    }

    public static int getButtonWidth(GuiButton button)
    {
        return button.width;
    }

    public static int getButtonHeight(GuiButton button)
    {
        return button.height;
    }

    public static void drawGradientRect(GuiScreen guiScreen, int left, int top, int right, int bottom, int startColor, int endColor)
    {
        guiScreen.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public static String getGuiChatText(GuiChat guiChat)
    {
        return guiChat.inputField.getText();
    }
}
