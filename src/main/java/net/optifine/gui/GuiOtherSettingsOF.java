package net.optifine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiOtherSettingsOF extends GuiScreen implements GuiYesNoCallback
{
    private GuiScreen prevScreen;
    protected String title;
    private GameSettings settings;
    private static GameSettings.Options[] enumOptions = new GameSettings.Options[] {GameSettings.Options.LAGOMETER, GameSettings.Options.PROFILER, GameSettings.Options.SHOW_FPS, GameSettings.Options.ADVANCED_TOOLTIPS, GameSettings.Options.WEATHER, GameSettings.Options.TIME, GameSettings.Options.USE_FULLSCREEN, GameSettings.Options.FULLSCREEN_MODE, GameSettings.Options.ANAGLYPH, GameSettings.Options.AUTOSAVE_TICKS, GameSettings.Options.SCREENSHOT_SIZE, GameSettings.Options.SHOW_GL_ERRORS};
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiOtherSettingsOF(GuiScreen guiScreen, GameSettings gameSettings)
    {
        this.prevScreen = guiScreen;
        this.settings = gameSettings;
    }

    public void initGui()
    {
        this.title = I18n.format("of.options.otherTitle", new Object[0]);
        this.buttonList.clear();

        for (int optionIndex = 0; optionIndex < enumOptions.length; ++optionIndex)
        {
            GameSettings.Options option = enumOptions[optionIndex];
            int buttonX = this.width / 2 - 155 + optionIndex % 2 * 160;
            int buttonY = this.height / 6 + 21 * (optionIndex / 2) - 12;

            if (!option.getEnumFloat())
            {
                this.buttonList.add(new GuiOptionButtonOF(option.returnEnumOrdinal(), buttonX, buttonY, option, this.settings.getKeyBinding(option)));
            }
            else
            {
                this.buttonList.add(new GuiOptionSliderOF(option.returnEnumOrdinal(), buttonX, buttonY, option));
            }
        }

        this.buttonList.add(new GuiButton(210, this.width / 2 - 100, this.height / 6 + 168 + 11 - 44, I18n.format("of.options.other.reset", new Object[0])));
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
    }

    protected void actionPerformed(GuiButton guiButton)
    {
        if (guiButton.enabled)
        {
            if (guiButton.id < 200 && guiButton instanceof GuiOptionButton)
            {
                this.settings.setOptionValue(((GuiOptionButton)guiButton).returnEnumOptions(), 1);
                guiButton.displayString = this.settings.getKeyBinding(GameSettings.Options.getEnumOptions(guiButton.id));
            }

            if (guiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.prevScreen);
            }

            if (guiButton.id == 210)
            {
                this.mc.gameSettings.saveOptions();
                GuiYesNo guiYesNo = new GuiYesNo(this, I18n.format("of.message.other.reset", new Object[0]), "", 9999);
                this.mc.displayGuiScreen(guiYesNo);
            }
        }
    }

    public void confirmClicked(boolean confirmed, int buttonId)
    {
        if (confirmed)
        {
            this.mc.gameSettings.resetSettings();
        }

        this.mc.displayGuiScreen(this);
    }

    public void drawScreen(int x, int y, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(x, y, partialTicks);
        this.tooltipManager.drawTooltips(x, y, this.buttonList);
    }
}
