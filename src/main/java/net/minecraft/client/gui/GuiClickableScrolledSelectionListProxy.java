package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsClickableScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import org.lwjgl.input.Mouse;

public class GuiClickableScrolledSelectionListProxy extends GuiSlot
{
    private final RealmsClickableScrolledSelectionList selectionList;

    public GuiClickableScrolledSelectionListProxy(RealmsClickableScrolledSelectionList selectionList, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(Minecraft.getMinecraft(), widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.selectionList = selectionList;
    }

    protected int getSize()
    {
        return this.selectionList.getItemCount();
    }

    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
    {
        this.selectionList.selectItem(slotIndex, isDoubleClick, mouseX, mouseY);
    }

    protected boolean isSelected(int slotIndex)
    {
        return this.selectionList.isSelectedItem(slotIndex);
    }

    protected void drawBackground()
    {
        this.selectionList.renderBackground();
    }

    protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
    {
        this.selectionList.renderItem(entryID, x, y, slotHeight, mouseXIn, mouseYIn);
    }

    public int getWidth()
    {
        return super.width;
    }

    public int getMouseY()
    {
        return super.mouseY;
    }

    public int getMouseX()
    {
        return super.mouseX;
    }

    protected int getContentHeight()
    {
        return this.selectionList.getMaxPosition();
    }

    protected int getScrollBarX()
    {
        return this.selectionList.getScrollbarPosition();
    }

    public void handleMouseInput()
    {
        super.handleMouseInput();

        if (this.scrollMultiplier > 0.0F && Mouse.getEventButtonState())
        {
            this.selectionList.customMouseEvent(this.top, this.bottom, this.headerPadding, this.amountScrolled, this.slotHeight);
        }
    }

    public void renderSelected(int width, int y, int slotHeight, Tezzelator tezzelator)
    {
        this.selectionList.renderSelected(width, y, slotHeight, tezzelator);
    }

    protected void drawSelectionBox(int x, int y, int mouseXIn, int mouseYIn)
    {
        int entryCount = this.getSize();

        for (int entryIndex = 0; entryIndex < entryCount; ++entryIndex)
        {
            int slotY = y + entryIndex * this.slotHeight + this.headerPadding;
            int slotContentHeight = this.slotHeight - 4;

            if (slotY > this.bottom || slotY + slotContentHeight < this.top)
            {
                this.onSlotOutOfRange(entryIndex, x, slotY);
            }

            if (this.showSelectionBox && this.isSelected(entryIndex))
            {
                this.renderSelected(this.width, slotY, slotContentHeight, Tezzelator.instance);
            }

            this.drawSlot(entryIndex, x, slotY, slotContentHeight, mouseXIn, mouseYIn);
        }
    }
}
