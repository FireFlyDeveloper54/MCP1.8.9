package net.minecraft.realms;

import net.minecraft.client.gui.GuiSimpleScrolledSelectionListProxy;

public class RealmsSimpleScrolledSelectionList
{
    private final GuiSimpleScrolledSelectionListProxy proxy;

    public RealmsSimpleScrolledSelectionList(int width, int height, int top, int bottom, int slotHeight)
    {
        this.proxy = new GuiSimpleScrolledSelectionListProxy(this, width, height, top, bottom, slotHeight);
    }

    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.proxy.drawScreen(mouseX, mouseY, partialTicks);
    }

    public int width()
    {
        return this.proxy.getWidth();
    }

    public int getMouseY()
    {
        return this.proxy.getMouseY();
    }

    public int getMouseX()
    {
        return this.proxy.getMouseX();
    }

    protected void renderItem(int index, int x, int y, int height, Tezzelator tezzelator, int mouseX, int mouseY)
    {
    }

    public void renderItem(int index, int x, int y, int height, int mouseX, int mouseY)
    {
        this.renderItem(index, x, y, height, Tezzelator.instance, mouseX, mouseY);
    }

    public int getItemCount()
    {
        return 0;
    }

    public void selectItem(int index, boolean doubleClick, int mouseX, int mouseY)
    {
    }

    public boolean isSelectedItem(int index)
    {
        return false;
    }

    public void renderBackground()
    {
    }

    public int getMaxPosition()
    {
        return 0;
    }

    public int getScrollbarPosition()
    {
        return this.proxy.getWidth() / 2 + 124;
    }

    public void mouseEvent()
    {
        this.proxy.handleMouseInput();
    }

    public void scroll(int amount)
    {
        this.proxy.scrollBy(amount);
    }

    public int getScroll()
    {
        return this.proxy.getAmountScrolled();
    }

    protected void renderList(int x, int y, int mouseX, int mouseY)
    {
    }
}
