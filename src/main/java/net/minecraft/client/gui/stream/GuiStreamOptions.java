package net.minecraft.client.gui.stream;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;

public class GuiStreamOptions extends GuiScreen
{
    private static final GameSettings.Options[] STREAM_OPTIONS = new GameSettings.Options[] {GameSettings.Options.STREAM_BYTES_PER_PIXEL, GameSettings.Options.STREAM_FPS, GameSettings.Options.STREAM_KBPS, GameSettings.Options.STREAM_SEND_METADATA, GameSettings.Options.STREAM_VOLUME_MIC, GameSettings.Options.STREAM_VOLUME_SYSTEM, GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR, GameSettings.Options.STREAM_COMPRESSION};
    private static final GameSettings.Options[] CHAT_OPTIONS = new GameSettings.Options[] {GameSettings.Options.STREAM_CHAT_ENABLED, GameSettings.Options.STREAM_CHAT_USER_FILTER};
    private final GuiScreen parentScreen;
    private final GameSettings gameSettings;
    private String streamTitle;
    private String chatTitle;
    private int chatOptionsY;
    private boolean restartRequired = false;

    public GuiStreamOptions(GuiScreen parentScreenIn, GameSettings gameSettingsIn)
    {
        this.parentScreen = parentScreenIn;
        this.gameSettings = gameSettingsIn;
    }

    public void initGui()
    {
        int i = 0;
        this.streamTitle = I18n.format("options.stream.title", new Object[0]);
        this.chatTitle = I18n.format("options.stream.chat.title", new Object[0]);

        for (GameSettings.Options gamesettings$options : STREAM_OPTIONS)
        {
            if (gamesettings$options.getEnumFloat())
            {
                this.buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), gamesettings$options));
            }
            else
            {
                this.buttonList.add(new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), gamesettings$options, this.gameSettings.getKeyBinding(gamesettings$options)));
            }

            ++i;
        }

        if (i % 2 == 1)
        {
            ++i;
        }

        this.chatOptionsY = this.height / 6 + 24 * (i >> 1) + 6;
        i = i + 2;

        for (GameSettings.Options gamesettings$options1 : CHAT_OPTIONS)
        {
            if (gamesettings$options1.getEnumFloat())
            {
                this.buttonList.add(new GuiOptionSlider(gamesettings$options1.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), gamesettings$options1));
            }
            else
            {
                this.buttonList.add(new GuiOptionButton(gamesettings$options1.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), gamesettings$options1, this.gameSettings.getKeyBinding(gamesettings$options1)));
            }

            ++i;
        }

        this.buttonList.add(new GuiButton(200, this.width / 2 - 155, this.height / 6 + 168, 150, 20, I18n.format("gui.done", new Object[0])));
        GuiButton guibutton = new GuiButton(201, this.width / 2 + 5, this.height / 6 + 168, 150, 20, I18n.format("options.stream.ingestSelection", new Object[0]));
        guibutton.enabled = this.mc.getTwitchStream().isReadyToBroadcast() && this.mc.getTwitchStream().getIngestServers().length > 0 || this.mc.getTwitchStream().isIngestTesting();
        this.buttonList.add(guibutton);
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id < 100 && button instanceof GuiOptionButton)
            {
                GameSettings.Options gamesettings$options = ((GuiOptionButton)button).returnEnumOptions();
                this.gameSettings.setOptionValue(gamesettings$options, 1);
                button.displayString = this.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));

                if (this.mc.getTwitchStream().isBroadcasting() && gamesettings$options != GameSettings.Options.STREAM_CHAT_ENABLED && gamesettings$options != GameSettings.Options.STREAM_CHAT_USER_FILTER)
                {
                    this.restartRequired = true;
                }
            }
            else if (button instanceof GuiOptionSlider)
            {
                if (button.id == GameSettings.Options.STREAM_VOLUME_MIC.returnEnumOrdinal())
                {
                    this.mc.getTwitchStream().updateStreamVolume();
                }
                else if (button.id == GameSettings.Options.STREAM_VOLUME_SYSTEM.returnEnumOrdinal())
                {
                    this.mc.getTwitchStream().updateStreamVolume();
                }
                else if (this.mc.getTwitchStream().isBroadcasting())
                {
                    this.restartRequired = true;
                }
            }

            if (button.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 201)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiIngestServers(this));
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.streamTitle, this.width / 2, 20, 16777215);
        this.drawCenteredString(this.fontRendererObj, this.chatTitle, this.width / 2, this.chatOptionsY, 16777215);

        if (this.restartRequired)
        {
            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.RED + I18n.format("options.stream.changes", new Object[0]), this.width / 2, 20 + this.fontRendererObj.FONT_HEIGHT, 16777215);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
