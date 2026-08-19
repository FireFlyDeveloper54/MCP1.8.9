package net.optifine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiPerformanceSettingsOF extends GuiScreen
{
    private GuiScreen prevScreen;
    protected String title;
    private GameSettings settings;
    private static GameSettings.Options[] enumOptions = new GameSettings.Options[] {GameSettings.Options.SMOOTH_FPS, GameSettings.Options.SMOOTH_WORLD, GameSettings.Options.FAST_RENDER, GameSettings.Options.FAST_MATH, GameSettings.Options.CHUNK_UPDATES, GameSettings.Options.CHUNK_UPDATES_DYNAMIC, GameSettings.Options.RENDER_REGIONS, GameSettings.Options.LAZY_CHUNK_LOADING, GameSettings.Options.SMART_ANIMATIONS, GameSettings.Options.ENTITY_CULLING, GameSettings.Options.CULL_ARMOR_STANDS, GameSettings.Options.LIMIT_CHUNK_UPDATES, GameSettings.Options.CULL_TRACING_DISTANCE, GameSettings.Options.CULL_SLEEP_DELAY, GameSettings.Options.CULL_HITBOX_LIMIT, GameSettings.Options.CHUNK_UPDATE_LIMIT, GameSettings.Options.PARTICLES_LIMIT};
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiPerformanceSettingsOF(GuiScreen guiScreen, GameSettings gameSettings)
    {
        this.prevScreen = guiScreen;
        this.settings = gameSettings;
    }

    public void initGui()
    {
        this.title = I18n.format("of.options.performanceTitle", new Object[0]);
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
        }
    }

    public void drawScreen(int x, int y, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(x, y, partialTicks);
        this.tooltipManager.drawTooltips(x, y, this.buttonList);
    }
}
