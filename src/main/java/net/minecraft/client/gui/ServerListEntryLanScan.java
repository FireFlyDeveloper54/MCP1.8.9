package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class ServerListEntryLanScan implements GuiListExtended.IGuiListEntry
{
    private final Minecraft mc = Minecraft.getMinecraft();

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        int textY = y + slotHeight / 2 - this.mc.fontRendererObj.FONT_HEIGHT / 2;
        this.mc.fontRendererObj.drawString(I18n.format("lanServer.scanning", new Object[0]), this.mc.currentScreen.width / 2 - this.mc.fontRendererObj.getStringWidth(I18n.format("lanServer.scanning", new Object[0])) / 2, textY, 16777215);
        String loadingText;

        switch ((int)(Minecraft.getSystemTime() / 300L % 4L))
        {
            case 0:
            default:
                loadingText = "O o o";
                break;

            case 1:
            case 3:
                loadingText = "o O o";
                break;

            case 2:
                loadingText = "o o O";
        }

        this.mc.fontRendererObj.drawString(loadingText, this.mc.currentScreen.width / 2 - this.mc.fontRendererObj.getStringWidth(loadingText) / 2, textY + this.mc.fontRendererObj.FONT_HEIGHT, 8421504);
    }

    public void setSelected(int slotIndex, int x, int y)
    {
    }

    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        return false;
    }

    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }
}
