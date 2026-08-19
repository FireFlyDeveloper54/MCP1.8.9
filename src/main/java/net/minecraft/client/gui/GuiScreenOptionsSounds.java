package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class GuiScreenOptionsSounds extends GuiScreen
{
    private final GuiScreen parentScreen;
    private final GameSettings gameSettings;
    protected String title = "Options";
    private String offDisplayString;

    public GuiScreenOptionsSounds(GuiScreen parentScreen, GameSettings gameSettings)
    {
        this.parentScreen = parentScreen;
        this.gameSettings = gameSettings;
    }

    public void initGui()
    {
        int buttonIndex = 0;
        this.title = I18n.format("options.sounds.title", new Object[0]);
        this.offDisplayString = I18n.format("options.off", new Object[0]);
        this.buttonList.add(new GuiScreenOptionsSounds.Button(SoundCategory.MASTER.getCategoryId(), this.width / 2 - 155 + buttonIndex % 2 * 160, this.height / 6 - 12 + 24 * (buttonIndex >> 1), SoundCategory.MASTER, true));
        buttonIndex = buttonIndex + 2;

        for (SoundCategory soundCategory : SoundCategory.VALUES)
        {
            if (soundCategory != SoundCategory.MASTER)
            {
                this.buttonList.add(new GuiScreenOptionsSounds.Button(soundCategory.getCategoryId(), this.width / 2 - 155 + buttonIndex % 2 * 160, this.height / 6 - 12 + 24 * (buttonIndex >> 1), soundCategory, false));
                ++buttonIndex;
            }
        }

        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done", new Object[0])));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected String getSoundVolume(SoundCategory soundCategory)
    {
        float soundLevel = this.gameSettings.getSoundLevel(soundCategory);
        return soundLevel == 0.0F ? this.offDisplayString : (int)(soundLevel * 100.0F) + "%";
    }

    class Button extends GuiButton
    {
        private final SoundCategory soundCategory;
        private final String categoryName;
        public float sliderPosition = 1.0F;
        public boolean dragging;

        public Button(int id, int x, int y, SoundCategory soundCategory, boolean wide)
        {
            super(id, x, y, wide ? 310 : 150, 20, "");
            this.soundCategory = soundCategory;
            this.categoryName = I18n.format("soundCategory." + soundCategory.getCategoryName(), new Object[0]);
            this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getSoundVolume(soundCategory);
            this.sliderPosition = GuiScreenOptionsSounds.this.gameSettings.getSoundLevel(soundCategory);
        }

        protected int getHoverState(boolean mouseOver)
        {
            return 0;
        }

        protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                if (this.dragging)
                {
                    this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                    this.sliderPosition = MathHelper.clamp_float(this.sliderPosition, 0.0F, 1.0F);
                    mc.gameSettings.setSoundLevel(this.soundCategory, this.sliderPosition);
                    mc.gameSettings.saveOptions();
                    this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getSoundVolume(this.soundCategory);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
                this.drawTexturedModalRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
            }
        }

        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
        {
            if (super.mousePressed(mc, mouseX, mouseY))
            {
                this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                this.sliderPosition = MathHelper.clamp_float(this.sliderPosition, 0.0F, 1.0F);
                mc.gameSettings.setSoundLevel(this.soundCategory, this.sliderPosition);
                mc.gameSettings.saveOptions();
                this.displayString = this.categoryName + ": " + GuiScreenOptionsSounds.this.getSoundVolume(this.soundCategory);
                this.dragging = true;
                return true;
            }
            else
            {
                return false;
            }
        }

        public void playPressSound(SoundHandler soundHandlerIn)
        {
        }

        public void mouseReleased(int mouseX, int mouseY)
        {
            if (this.dragging)
            {
                if (this.soundCategory == SoundCategory.MASTER)
                {
                    float previewVolume = 1.0F;
                }
                else
                {
                    GuiScreenOptionsSounds.this.gameSettings.getSoundLevel(this.soundCategory);
                }

                GuiScreenOptionsSounds.this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            }

            this.dragging = false;
        }
    }
}
