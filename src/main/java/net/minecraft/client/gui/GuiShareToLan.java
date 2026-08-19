package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

public class GuiShareToLan extends GuiScreen
{
    private final GuiScreen parentScreen;
    private GuiButton allowCommandsButton;
    private GuiButton gameModeButton;
    private String gameMode = "survival";
    private boolean allowCommands;

    public GuiShareToLan(GuiScreen parentScreen)
    {
        this.parentScreen = parentScreen;
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(101, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("lanServer.start", new Object[0])));
        this.buttonList.add(new GuiButton(102, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.buttonList.add(this.gameModeButton = new GuiButton(104, this.width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.gameMode", new Object[0])));
        this.buttonList.add(this.allowCommandsButton = new GuiButton(103, this.width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.allowCommands", new Object[0])));
        this.updateDisplayTexts();
    }

    private void updateDisplayTexts()
    {
        this.gameModeButton.displayString = I18n.format("selectWorld.gameMode", new Object[0]) + " " + I18n.format("selectWorld.gameMode." + this.gameMode, new Object[0]);
        this.allowCommandsButton.displayString = I18n.format("selectWorld.allowCommands", new Object[0]) + " ";

        if (this.allowCommands)
        {
            this.allowCommandsButton.displayString = this.allowCommandsButton.displayString + I18n.format("options.on", new Object[0]);
        }
        else
        {
            this.allowCommandsButton.displayString = this.allowCommandsButton.displayString + I18n.format("options.off", new Object[0]);
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 102)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 104)
        {
            if (this.gameMode.equals("spectator"))
            {
                this.gameMode = "creative";
            }
            else if (this.gameMode.equals("creative"))
            {
                this.gameMode = "adventure";
            }
            else if (this.gameMode.equals("adventure"))
            {
                this.gameMode = "survival";
            }
            else
            {
                this.gameMode = "spectator";
            }

            this.updateDisplayTexts();
        }
        else if (button.id == 103)
        {
            this.allowCommands = !this.allowCommands;
            this.updateDisplayTexts();
        }
        else if (button.id == 101)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            String lanPort = this.mc.getIntegratedServer().shareToLAN(WorldSettings.GameType.getByName(this.gameMode), this.allowCommands);
            IChatComponent chatComponent;

            if (lanPort != null)
            {
                chatComponent = new ChatComponentTranslation("commands.publish.started", new Object[] {lanPort});
            }
            else
            {
                chatComponent = new ChatComponentText("commands.publish.failed");
            }

            this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("lanServer.title", new Object[0]), this.width / 2, 50, 16777215);
        this.drawCenteredString(this.fontRendererObj, I18n.format("lanServer.otherPlayers", new Object[0]), this.width / 2, 82, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
