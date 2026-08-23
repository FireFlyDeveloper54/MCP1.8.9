package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.client.GameWindow;

public abstract class GuiSlot
{
    protected final Minecraft mc;
    protected int width;
    protected int height;
    protected int top;
    protected int bottom;
    protected int right;
    protected int left;
    protected final int slotHeight;
    private int scrollUpButtonID;
    private int scrollDownButtonID;
    protected int mouseX;
    protected int mouseY;
    protected boolean canBeScrolled = true;
    protected int initialClickY = -2;
    protected float scrollMultiplier;
    protected float amountScrolled;
    protected int selectedElement = -1;
    protected long lastClicked;
    protected boolean visible = true;
    protected boolean showSelectionBox = true;
    protected boolean hasListHeader;
    protected int headerPadding;
    private boolean enabled = true;

    public GuiSlot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn)
    {
        this.mc = mcIn;
        this.width = width;
        this.height = height;
        this.top = topIn;
        this.bottom = bottomIn;
        this.slotHeight = slotHeightIn;
        this.left = 0;
        this.right = width;
    }

    public void setDimensions(int widthIn, int heightIn, int topIn, int bottomIn)
    {
        this.width = widthIn;
        this.height = heightIn;
        this.top = topIn;
        this.bottom = bottomIn;
        this.left = 0;
        this.right = widthIn;
    }

    public void setShowSelectionBox(boolean showSelectionBoxIn)
    {
        this.showSelectionBox = showSelectionBoxIn;
    }

    protected void setHasListHeader(boolean hasListHeaderIn, int headerPaddingIn)
    {
        this.hasListHeader = hasListHeaderIn;
        this.headerPadding = headerPaddingIn;

        if (!hasListHeaderIn)
        {
            this.headerPadding = 0;
        }
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY);

    protected abstract boolean isSelected(int slotIndex);

    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.headerPadding;
    }

    protected abstract void drawBackground();

    protected void onSlotOutOfRange(int slotIndex, int x, int y)
    {
    }

    protected abstract void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn);

    protected void drawListHeader(int x, int y, Tessellator tessellator)
    {
    }

    protected void clickedHeader(int x, int y)
    {
    }

    protected void drawScreenPostRender(int mouseX, int mouseY)
    {
    }

    public int getSlotIndexFromScreenCoords(int mouseX, int mouseY)
    {
        int i = this.left + this.width / 2 - this.getListWidth() / 2;
        int j = this.left + this.width / 2 + this.getListWidth() / 2;
        int k = mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
        int l = k / this.slotHeight;
        return mouseX < this.getScrollBarX() && mouseX >= i && mouseX <= j && l >= 0 && k >= 0 && l < this.getSize() ? l : -1;
    }

    public void registerScrollButtons(int scrollUpButtonIDIn, int scrollDownButtonIDIn)
    {
        this.scrollUpButtonID = scrollUpButtonIDIn;
        this.scrollDownButtonID = scrollDownButtonIDIn;
    }

    protected void bindAmountScrolled()
    {
        this.amountScrolled = MathHelper.clamp_float(this.amountScrolled, 0.0F, (float)this.getMaxScrollAmount());
    }

    public int getMaxScrollAmount()
    {
        return Math.max(0, this.getContentHeight() - (this.bottom - this.top - 4));
    }

    public int getAmountScrolled()
    {
        return (int)this.amountScrolled;
    }

    public boolean isMouseYWithinSlotBounds(int mouseY)
    {
        return mouseY >= this.top && mouseY <= this.bottom && this.mouseX >= this.left && this.mouseX <= this.right;
    }

    public void scrollBy(int amount)
    {
        this.amountScrolled += (float)amount;
        this.bindAmountScrolled();
        this.initialClickY = -2;
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == this.scrollUpButtonID)
            {
                this.amountScrolled -= (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2;
                this.bindAmountScrolled();
            }
            else if (button.id == this.scrollDownButtonID)
            {
                this.amountScrolled += (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2;
                this.bindAmountScrolled();
            }
        }
    }

    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.drawBackground();
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            this.drawContainerBackground(tessellator);
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader)
            {
                this.drawListHeader(k, l, tessellator);
            }

            this.drawSelectionBox(k, l, mouseXIn, mouseYIn);
            GlStateManager.disableDepth();
            int intValue = 4;
            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double)this.left, (double)(this.top + intValue), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos((double)this.right, (double)(this.top + intValue), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double)this.right, (double)(this.bottom - intValue), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos((double)this.left, (double)(this.bottom - intValue), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            int secondIntValue = this.getMaxScrollAmount();

            if (secondIntValue > 0)
            {
                int thirdIntValue = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                thirdIntValue = MathHelper.clamp_int(thirdIntValue, 32, this.bottom - this.top - 8);
                int fourthIntValue = (int)this.amountScrolled * (this.bottom - this.top - thirdIntValue) / secondIntValue + this.top;

                if (fourthIntValue < this.top)
                {
                    fourthIntValue = this.top;
                }

                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos((double)i, (double)(fourthIntValue + thirdIntValue), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos((double)j, (double)(fourthIntValue + thirdIntValue), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos((double)j, (double)fourthIntValue, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos((double)i, (double)fourthIntValue, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos((double)i, (double)(fourthIntValue + thirdIntValue - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos((double)(j - 1), (double)(fourthIntValue + thirdIntValue - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos((double)(j - 1), (double)fourthIntValue, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos((double)i, (double)fourthIntValue, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.drawScreenPostRender(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    public void handleMouseInput()
    {
        if (this.isMouseYWithinSlotBounds(this.mouseY))
        {
            if (GameWindow.getEventButton() == 0 && GameWindow.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom)
            {
                int i = (this.width - this.getListWidth()) / 2;
                int j = (this.width + this.getListWidth()) / 2;
                int k = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
                int l = k / this.slotHeight;

                if (l < this.getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0)
                {
                    this.elementClicked(l, false, this.mouseX, this.mouseY);
                    this.selectedElement = l;
                }
                else if (this.mouseX >= i && this.mouseX <= j && k < 0)
                {
                    this.clickedHeader(this.mouseX - i, this.mouseY - this.top + (int)this.amountScrolled - 4);
                }
            }

            if (GameWindow.isButtonDown(0) && this.getEnabled())
            {
                if (this.initialClickY != -1)
                {
                    if (this.initialClickY >= 0)
                    {
                        this.amountScrolled -= (float)(this.mouseY - this.initialClickY) * this.scrollMultiplier;
                        this.initialClickY = this.mouseY;
                    }
                }
                else
                {
                    boolean flag1 = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom)
                    {
                        int intValue = (this.width - this.getListWidth()) / 2;
                        int secondIntValue = (this.width + this.getListWidth()) / 2;
                        int thirdIntValue = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
                        int fourthIntValue = thirdIntValue / this.slotHeight;

                        if (fourthIntValue < this.getSize() && this.mouseX >= intValue && this.mouseX <= secondIntValue && fourthIntValue >= 0 && thirdIntValue >= 0)
                        {
                            boolean flag = fourthIntValue == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                            this.elementClicked(fourthIntValue, flag, this.mouseX, this.mouseY);
                            this.selectedElement = fourthIntValue;
                            this.lastClicked = Minecraft.getSystemTime();
                        }
                        else if (this.mouseX >= intValue && this.mouseX <= secondIntValue && thirdIntValue < 0)
                        {
                            this.clickedHeader(this.mouseX - intValue, this.mouseY - this.top + (int)this.amountScrolled - 4);
                            flag1 = false;
                        }

                        int eighthIntValue = this.getScrollBarX();
                        int fifthIntValue = eighthIntValue + 6;

                        if (this.mouseX >= eighthIntValue && this.mouseX <= fifthIntValue)
                        {
                            this.scrollMultiplier = -1.0F;
                            int sixthIntValue = this.getMaxScrollAmount();

                            if (sixthIntValue < 1)
                            {
                                sixthIntValue = 1;
                            }

                            int ninthIntValue = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());
                            ninthIntValue = MathHelper.clamp_int(ninthIntValue, 32, this.bottom - this.top - 8);
                            this.scrollMultiplier /= (float)(this.bottom - this.top - ninthIntValue) / (float)sixthIntValue;
                        }
                        else
                        {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (flag1)
                        {
                            this.initialClickY = this.mouseY;
                        }
                        else
                        {
                            this.initialClickY = -2;
                        }
                    }
                    else
                    {
                        this.initialClickY = -2;
                    }
                }
            }
            else
            {
                this.initialClickY = -1;
            }

            int seventhIntValue = GameWindow.getEventDWheel();

            if (seventhIntValue != 0)
            {
                if (seventhIntValue > 0)
                {
                    seventhIntValue = -1;
                }
                else if (seventhIntValue < 0)
                {
                    seventhIntValue = 1;
                }

                this.amountScrolled += (float)(seventhIntValue * this.slotHeight / 2);
            }
        }
    }

    public void setEnabled(boolean enabledIn)
    {
        this.enabled = enabledIn;
    }

    public boolean getEnabled()
    {
        return this.enabled;
    }

    public int getListWidth()
    {
        return 220;
    }

    protected void drawSelectionBox(int x, int y, int mouseXIn, int mouseYIn)
    {
        int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        for (int j = 0; j < i; ++j)
        {
            int k = y + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight - 4;

            if (k > this.bottom || k + l < this.top)
            {
                this.onSlotOutOfRange(j, x, k);
            }

            if (this.showSelectionBox && this.isSelected(j))
            {
                int intValue2 = this.left + (this.width / 2 - this.getListWidth() / 2);
                int secondIntValue = this.left + this.width / 2 + this.getListWidth() / 2;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldRenderer.pos((double)intValue2, (double)(k + l + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldRenderer.pos((double)secondIntValue, (double)(k + l + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldRenderer.pos((double)secondIntValue, (double)(k - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                worldRenderer.pos((double)intValue2, (double)(k - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                worldRenderer.pos((double)(intValue2 + 1), (double)(k + l + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldRenderer.pos((double)(secondIntValue - 1), (double)(k + l + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldRenderer.pos((double)(secondIntValue - 1), (double)(k - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                worldRenderer.pos((double)(intValue2 + 1), (double)(k - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            if (!(this instanceof GuiResourcePackList) || k >= this.top - this.slotHeight && k <= this.bottom)
            {
                this.drawSlot(j, x, k, l, mouseXIn, mouseYIn);
            }
        }
    }

    protected int getScrollBarX()
    {
        return this.width / 2 + 124;
    }

    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRenderer.pos((double)this.left, (double)endY, 0.0D).tex(0.0D, (double)((float)endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
        worldRenderer.pos((double)(this.left + this.width), (double)endY, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
        worldRenderer.pos((double)(this.left + this.width), (double)startY, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
        worldRenderer.pos((double)this.left, (double)startY, 0.0D).tex(0.0D, (double)((float)startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
        tessellator.draw();
    }

    public void setSlotXBoundsFromLeft(int leftIn)
    {
        this.left = leftIn;
        this.right = leftIn + this.width;
    }

    public int getSlotHeight()
    {
        return this.slotHeight;
    }

    protected void drawContainerBackground(Tessellator tessellator)
    {
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRenderer.pos((double)this.left, (double)this.bottom, 0.0D).tex((double)((float)this.left / f), (double)((float)(this.bottom + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
        worldRenderer.pos((double)this.right, (double)this.bottom, 0.0D).tex((double)((float)this.right / f), (double)((float)(this.bottom + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
        worldRenderer.pos((double)this.right, (double)this.top, 0.0D).tex((double)((float)this.right / f), (double)((float)(this.top + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
        worldRenderer.pos((double)this.left, (double)this.top, 0.0D).tex((double)((float)this.left / f), (double)((float)(this.top + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
    }
}
