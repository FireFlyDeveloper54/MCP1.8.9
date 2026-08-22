package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;

public class GuiOptions extends GuiScreen implements GuiYesNoCallback
{
    private static final GameSettings.Options[] SCREEN_OPTIONS = new GameSettings.Options[] {GameSettings.Options.FOV};
    private final GuiScreen parentScreen;
    private final GameSettings game_settings_1;
    private GuiButton difficultyButton;
    private GuiLockIconButton difficultyLockButton;
    protected String title = "Options";

    public GuiOptions(GuiScreen parentScreenIn, GameSettings gameSettingsIn)
    {
        this.parentScreen = parentScreenIn;
        this.game_settings_1 = gameSettingsIn;
    }

    public void initGui()
    {
        int optionIndex = 0;
        this.title = I18n.format("options.title", new Object[0]);

        for (GameSettings.Options option : SCREEN_OPTIONS)
        {
            if (option.getEnumFloat())
            {
                this.buttonList.add(new GuiOptionSlider(option.returnEnumOrdinal(), this.width / 2 - 155 + optionIndex % 2 * 160, this.height / 6 - 12 + 24 * (optionIndex >> 1), option));
            }
            else
            {
                GuiOptionButton optionButton = new GuiOptionButton(option.returnEnumOrdinal(), this.width / 2 - 155 + optionIndex % 2 * 160, this.height / 6 - 12 + 24 * (optionIndex >> 1), option, this.game_settings_1.getKeyBinding(option));
                this.buttonList.add(optionButton);
            }

            ++optionIndex;
        }

        if (this.mc.theWorld != null)
        {
            EnumDifficulty difficulty = this.mc.theWorld.getDifficulty();
            this.difficultyButton = new GuiButton(108, this.width / 2 - 155 + optionIndex % 2 * 160, this.height / 6 - 12 + 24 * (optionIndex >> 1), 150, 20, this.getDifficultyText(difficulty));
            this.buttonList.add(this.difficultyButton);

            if (this.mc.isSingleplayer() && !this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
            {
                this.difficultyButton.setWidth(this.difficultyButton.getButtonWidth() - 20);
                this.difficultyLockButton = new GuiLockIconButton(109, this.difficultyButton.xPosition + this.difficultyButton.getButtonWidth(), this.difficultyButton.yPosition);
                this.buttonList.add(this.difficultyLockButton);
                this.difficultyLockButton.setLocked(this.mc.theWorld.getWorldInfo().isDifficultyLocked());
                this.difficultyLockButton.enabled = !this.difficultyLockButton.isLocked();
                this.difficultyButton.enabled = !this.difficultyLockButton.isLocked();
            }
            else
            {
                this.difficultyButton.enabled = false;
            }
        }

        this.buttonList.add(new GuiButton(110, this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, I18n.format("options.skinCustomisation", new Object[0])));
        this.buttonList.add(new GuiButton(8675309, this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, "Super Secret Settings...")
        {
            public void playPressSound(SoundHandler soundHandlerIn)
            {
                SoundEventAccessorComposite soundEventAccessorComposite = soundHandlerIn.getRandomSoundFromCategories(new SoundCategory[] {SoundCategory.ANIMALS, SoundCategory.BLOCKS, SoundCategory.MOBS, SoundCategory.PLAYERS, SoundCategory.WEATHER});

                if (soundEventAccessorComposite != null)
                {
                    soundHandlerIn.playSound(PositionedSoundRecord.create(soundEventAccessorComposite.getSoundEventLocation(), 0.5F));
                }
            }
        });
        this.buttonList.add(new GuiButton(106, this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.sounds", new Object[0])));
        this.buttonList.add(new GuiButton(101, this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.video", new Object[0])));
        this.buttonList.add(new GuiButton(100, this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.controls", new Object[0])));
        this.buttonList.add(new GuiButton(102, this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.language", new Object[0])));
        this.buttonList.add(new GuiButton(103, this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.chat.title", new Object[0])));
        this.buttonList.add(new GuiButton(105, this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.resourcepack", new Object[0])));
        this.buttonList.add(new GuiButton(104, this.width / 2 - 155, this.height / 6 + 144 - 6, 150, 20, I18n.format("options.snooper.view", new Object[0])));
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done", new Object[0])));
    }

    public String getDifficultyText(EnumDifficulty difficulty)
    {
        IChatComponent difficultyText = new ChatComponentText("");
        difficultyText.appendSibling(new ChatComponentTranslation("options.difficulty", new Object[0]));
        difficultyText.appendText(": ");
        difficultyText.appendSibling(new ChatComponentTranslation(difficulty.getDifficultyResourceKey(), new Object[0]));
        return difficultyText.getFormattedText();
    }

    public void confirmClicked(boolean result, int id)
    {
        this.mc.displayGuiScreen(this);

        if (id == 109 && result && this.mc.theWorld != null)
        {
            this.mc.theWorld.getWorldInfo().setDifficultyLocked(true);
            this.difficultyLockButton.setLocked(true);
            this.difficultyLockButton.enabled = false;
            this.difficultyButton.enabled = false;
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id < 100 && button instanceof GuiOptionButton)
            {
                GameSettings.Options option = ((GuiOptionButton)button).returnEnumOptions();
                this.game_settings_1.setOptionValue(option, 1);
                button.displayString = this.game_settings_1.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
            }

            if (button.id == 108)
            {
                this.mc.theWorld.getWorldInfo().setDifficulty(EnumDifficulty.getDifficultyEnum(this.mc.theWorld.getDifficulty().getDifficultyId() + 1));
                this.difficultyButton.displayString = this.getDifficultyText(this.mc.theWorld.getDifficulty());
            }

            if (button.id == 109)
            {
                this.mc.displayGuiScreen(new GuiYesNo(this, (new ChatComponentTranslation("difficulty.lock.title", new Object[0])).getFormattedText(), (new ChatComponentTranslation("difficulty.lock.question", new Object[] {new ChatComponentTranslation(this.mc.theWorld.getWorldInfo().getDifficulty().getDifficultyResourceKey(), new Object[0])})).getFormattedText(), 109));
            }

            if (button.id == 110)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiCustomizeSkin(this));
            }

            if (button.id == 8675309)
            {
                this.mc.entityRenderer.activateNextShader();
            }

            if (button.id == 101)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiVideoSettings(this, this.game_settings_1));
            }

            if (button.id == 100)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiControls(this, this.game_settings_1));
            }

            if (button.id == 102)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiLanguage(this, this.game_settings_1, this.mc.getLanguageManager()));
            }

            if (button.id == 103)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new ScreenChatOptions(this, this.game_settings_1));
            }

            if (button.id == 104)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiSnooper(this, this.game_settings_1));
            }

            if (button.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentScreen);
            }

            if (button.id == 105)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiScreenResourcePacks(this));
            }

            if (button.id == 106)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiScreenOptionsSounds(this, this.game_settings_1));
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
