package net.optifine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.optifine.Lang;

public class GuiAnimationSettingsOF extends GuiScreen
{
    private GuiScreen prevScreen;
    protected String title;
    private GameSettings settings;
    private static GameSettings.Options[] enumOptions = new GameSettings.Options[] {GameSettings.Options.ANIMATED_WATER, GameSettings.Options.ANIMATED_LAVA, GameSettings.Options.ANIMATED_FIRE, GameSettings.Options.ANIMATED_PORTAL, GameSettings.Options.ANIMATED_REDSTONE, GameSettings.Options.ANIMATED_EXPLOSION, GameSettings.Options.ANIMATED_FLAME, GameSettings.Options.ANIMATED_SMOKE, GameSettings.Options.VOID_PARTICLES, GameSettings.Options.WATER_PARTICLES, GameSettings.Options.RAIN_SPLASH, GameSettings.Options.PORTAL_PARTICLES, GameSettings.Options.POTION_PARTICLES, GameSettings.Options.DRIPPING_WATER_LAVA, GameSettings.Options.ANIMATED_TERRAIN, GameSettings.Options.ANIMATED_TEXTURES, GameSettings.Options.FIREWORK_PARTICLES, GameSettings.Options.PARTICLES};

    public GuiAnimationSettingsOF(GuiScreen guiScreen, GameSettings gameSettings)
    {
        this.prevScreen = guiScreen;
        this.settings = gameSettings;
    }

    public void initGui()
    {
        this.title = I18n.format("of.options.animationsTitle", new Object[0]);
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

        this.buttonList.add(new GuiButton(210, this.width / 2 - 155, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOn")));
        this.buttonList.add(new GuiButton(211, this.width / 2 - 155 + 80, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOff")));
        this.buttonList.add(new GuiOptionButton(200, this.width / 2 + 5, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
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
                this.mc.gameSettings.setAllAnimations(true);
            }

            if (guiButton.id == 211)
            {
                this.mc.gameSettings.setAllAnimations(false);
            }

            ScaledResolution scaledResolution = new ScaledResolution(this.mc);
            this.setWorldAndResolution(this.mc, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        }
    }

    public void drawScreen(int x, int y, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(x, y, partialTicks);
    }
}
