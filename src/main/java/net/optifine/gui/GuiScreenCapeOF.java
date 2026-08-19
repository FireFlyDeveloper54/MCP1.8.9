package net.optifine.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.src.Config;
import net.optifine.Lang;

public class GuiScreenCapeOF extends GuiScreenOF
{
    private final GuiScreen parentScreen;
    private String title;
    private String message;
    private long messageHideTimeMs;
    private String linkUrl;
    private GuiButtonOF buttonCopyLink;
    private FontRenderer fontRenderer;

    public GuiScreenCapeOF(GuiScreen parentScreenIn)
    {
        this.fontRenderer = Config.getMinecraft().fontRendererObj;
        this.parentScreen = parentScreenIn;
    }

    public void initGui()
    {
        int rowIndex = 0;
        this.title = I18n.format("of.options.capeOF.title", new Object[0]);
        rowIndex = rowIndex + 2;
        this.buttonList.add(new GuiButtonOF(210, this.width / 2 - 155, this.height / 6 + 24 * (rowIndex >> 1), 150, 20, I18n.format("of.options.capeOF.openEditor", new Object[0])));
        this.buttonList.add(new GuiButtonOF(220, this.width / 2 - 155 + 160, this.height / 6 + 24 * (rowIndex >> 1), 150, 20, I18n.format("of.options.capeOF.reloadCape", new Object[0])));
        rowIndex = rowIndex + 6;
        this.buttonCopyLink = new GuiButtonOF(230, this.width / 2 - 100, this.height / 6 + 24 * (rowIndex >> 1), 200, 20, I18n.format("of.options.capeOF.copyEditorLink", new Object[0]));
        this.buttonCopyLink.visible = this.linkUrl != null;
        this.buttonList.add(this.buttonCopyLink);
        rowIndex = rowIndex + 4;
        this.buttonList.add(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 24 * (rowIndex >> 1), I18n.format("gui.done", new Object[0])));
    }

    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 200)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }

            if (button.id == 210)
            {
                // OptiFine online cape editor links are disabled for this local MCP project.
                this.showMessage(I18n.format("of.message.capeOF.openEditorError", new Object[0]), 10000L);
            }

            if (button.id == 220)
            {
                this.showMessage(Lang.get("of.message.capeOF.reloadCape"), 15000L);

                if (this.mc.thePlayer != null)
                {
                    long delayMs = 15000L;
                    long reloadTimeMs = System.currentTimeMillis() + delayMs;
                    this.mc.thePlayer.setReloadCapeTimeMs(reloadTimeMs);
                }
            }

            if (button.id == 230 && this.linkUrl != null)
            {
                setClipboardString(this.linkUrl);
            }
        }
    }

    private void showMessage(String msg, long timeMs)
    {
        this.message = msg;
        this.messageHideTimeMs = System.currentTimeMillis() + timeMs;
        this.setLinkUrl((String)null);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);

        if (this.message != null)
        {
            this.drawCenteredString(this.fontRenderer, this.message, this.width / 2, this.height / 6 + 60, 16777215);

            if (System.currentTimeMillis() > this.messageHideTimeMs)
            {
                this.message = null;
                this.setLinkUrl((String)null);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void setLinkUrl(String linkUrl)
    {
        this.linkUrl = linkUrl;
        this.buttonCopyLink.visible = linkUrl != null;
    }
}
