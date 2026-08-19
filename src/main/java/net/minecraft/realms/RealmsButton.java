package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonRealmsProxy;
import net.minecraft.util.ResourceLocation;

public class RealmsButton
{
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private GuiButtonRealmsProxy proxy;

    public RealmsButton(int buttonId, int x, int y, String text)
    {
        this.proxy = new GuiButtonRealmsProxy(this, buttonId, x, y, text);
    }

    public RealmsButton(int buttonId, int x, int y, int widthIn, int heightIn, String text)
    {
        this.proxy = new GuiButtonRealmsProxy(this, buttonId, x, y, text, widthIn, heightIn);
    }

    public GuiButton getProxy()
    {
        return this.proxy;
    }

    public int getId()
    {
        return this.proxy.getId();
    }

    public boolean isActive()
    {
        return this.proxy.getEnabled();
    }

    public void setActive(boolean active)
    {
        this.proxy.setEnabled(active);
    }

    public void setText(String text)
    {
        this.proxy.setText(text);
    }

    public int getWidth()
    {
        return this.proxy.getButtonWidth();
    }

    public int getHeight()
    {
        return this.proxy.getHeight();
    }

    public int getY()
    {
        return this.proxy.getPositionY();
    }

    public void render(int mouseX, int mouseY)
    {
        this.proxy.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    public void clicked(int mouseX, int mouseY)
    {
    }

    public void released(int mouseX, int mouseY)
    {
    }

    public void blit(int x, int y, int textureX, int textureY, int width, int height)
    {
        this.proxy.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    public void renderBg(int mouseX, int mouseY)
    {
    }

    public int getYImage(boolean mouseOver)
    {
        return this.proxy.getBaseHoverState(mouseOver);
    }
}
