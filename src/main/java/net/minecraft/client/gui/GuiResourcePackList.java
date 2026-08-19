package net.minecraft.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.util.EnumChatFormatting;

public abstract class GuiResourcePackList extends GuiListExtended
{
    protected final Minecraft mc;
    protected final List<ResourcePackListEntry> resourcePackEntries;

    public GuiResourcePackList(Minecraft mcIn, int width, int height, List<ResourcePackListEntry> resourcePackEntries)
    {
        super(mcIn, width, height, 32, height - 55 + 4, 36);
        this.mc = mcIn;
        this.resourcePackEntries = resourcePackEntries;
        this.canBeScrolled = false;
        this.setHasListHeader(true, (int)((float)mcIn.fontRendererObj.FONT_HEIGHT * 1.5F));
    }

    protected void drawListHeader(int x, int y, Tessellator tessellator)
    {
        String headerText = EnumChatFormatting.UNDERLINE + "" + EnumChatFormatting.BOLD + this.getListHeader();
        this.mc.fontRendererObj.drawString(headerText, x + this.width / 2 - this.mc.fontRendererObj.getStringWidth(headerText) / 2, Math.min(this.top + 3, y), 16777215);
    }

    protected abstract String getListHeader();

    public List<ResourcePackListEntry> getList()
    {
        return this.resourcePackEntries;
    }

    protected int getSize()
    {
        return this.getList().size();
    }

    public ResourcePackListEntry getListEntry(int index)
    {
        return (ResourcePackListEntry)this.getList().get(index);
    }

    public int getListWidth()
    {
        return this.width;
    }

    protected int getScrollBarX()
    {
        return this.right - 6;
    }
}
