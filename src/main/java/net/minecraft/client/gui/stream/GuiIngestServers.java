package net.minecraft.client.gui.stream;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.IngestServerTester;
import net.minecraft.util.EnumChatFormatting;
import tv.twitch.broadcast.IngestServer;

public class GuiIngestServers extends GuiScreen
{
    private final GuiScreen parentScreen;
    private String title;
    private GuiIngestServers.ServerList serverList;

    public GuiIngestServers(GuiScreen parentScreenIn)
    {
        this.parentScreen = parentScreenIn;
    }

    public void initGui()
    {
        this.title = I18n.format("options.stream.ingest.title", new Object[0]);
        this.serverList = new GuiIngestServers.ServerList(this.mc);

        if (!this.mc.getTwitchStream().isIngestTesting())
        {
            this.mc.getTwitchStream().startIngestTest();
        }

        this.buttonList.add(new GuiButton(1, this.width / 2 - 155, this.height - 24 - 6, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 5, this.height - 24 - 6, 150, 20, I18n.format("options.stream.ingest.reset", new Object[0])));
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.serverList.handleMouseInput();
    }

    public void onGuiClosed()
    {
        if (this.mc.getTwitchStream().isIngestTesting())
        {
            this.mc.getTwitchStream().getIngestServerTester().cancel();
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else
            {
                this.mc.gameSettings.streamPreferredServer = "";
                this.mc.gameSettings.saveOptions();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.serverList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class ServerList extends GuiSlot
    {
        public ServerList(Minecraft mcIn)
        {
            super(mcIn, GuiIngestServers.this.width, GuiIngestServers.this.height, 32, GuiIngestServers.this.height - 35, (int)((double)mcIn.fontRendererObj.FONT_HEIGHT * 3.5D));
            this.setShowSelectionBox(false);
        }

        protected int getSize()
        {
            return this.mc.getTwitchStream().getIngestServers().length;
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            this.mc.gameSettings.streamPreferredServer = this.mc.getTwitchStream().getIngestServers()[slotIndex].serverUrl;
            this.mc.gameSettings.saveOptions();
        }

        protected boolean isSelected(int slotIndex)
        {
            return this.mc.getTwitchStream().getIngestServers()[slotIndex].serverUrl.equals(this.mc.gameSettings.streamPreferredServer);
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            IngestServer ingestserver = this.mc.getTwitchStream().getIngestServers()[entryID];
            String s = ingestserver.serverUrl.replace("{stream_key}", "");
            String bitrateText = (int)ingestserver.bitrateKbps + " kbps";
            String statusText = null;
            IngestServerTester ingestservertester = this.mc.getTwitchStream().getIngestServerTester();

            if (ingestservertester != null)
            {
                if (ingestserver == ingestservertester.getCurrentServer())
                {
                    s = EnumChatFormatting.GREEN + s;
                    bitrateText = (int)(ingestservertester.getServerProgress() * 100.0F) + "%";
                }
                else if (entryID < ingestservertester.getCurrentServerIndex())
                {
                    if (ingestserver.bitrateKbps == 0.0F)
                    {
                        bitrateText = EnumChatFormatting.RED + "Down!";
                    }
                }
                else
                {
                    bitrateText = EnumChatFormatting.OBFUSCATED + "1234" + EnumChatFormatting.RESET + " kbps";
                }
            }
            else if (ingestserver.bitrateKbps == 0.0F)
            {
                bitrateText = EnumChatFormatting.RED + "Down!";
            }

            x = x - 15;

            if (this.isSelected(entryID))
            {
                statusText = EnumChatFormatting.BLUE + "(Preferred)";
            }
            else if (ingestserver.defaultServer)
            {
                statusText = EnumChatFormatting.GREEN + "(Default)";
            }

            GuiIngestServers.this.drawString(GuiIngestServers.this.fontRendererObj, ingestserver.serverName, x + 2, y + 5, 16777215);
            GuiIngestServers.this.drawString(GuiIngestServers.this.fontRendererObj, s, x + 2, y + GuiIngestServers.this.fontRendererObj.FONT_HEIGHT + 5 + 3, 3158064);
            GuiIngestServers.this.drawString(GuiIngestServers.this.fontRendererObj, bitrateText, this.getScrollBarX() - 5 - GuiIngestServers.this.fontRendererObj.getStringWidth(bitrateText), y + 5, 8421504);

            if (statusText != null)
            {
                GuiIngestServers.this.drawString(GuiIngestServers.this.fontRendererObj, statusText, this.getScrollBarX() - 5 - GuiIngestServers.this.fontRendererObj.getStringWidth(statusText), y + 5 + 3 + GuiIngestServers.this.fontRendererObj.FONT_HEIGHT, 8421504);
            }
        }

        protected int getScrollBarX()
        {
            return super.getScrollBarX() + 15;
        }
    }
}
