package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

public class GuiScreenRealmsProxy extends GuiScreen
{
    private RealmsScreen realmsScreen;

    public GuiScreenRealmsProxy(RealmsScreen realmsScreenIn)
    {
        this.realmsScreen = realmsScreenIn;
        super.buttonList = Collections.<GuiButton>synchronizedList(Lists.<GuiButton>newArrayList());
    }

    public RealmsScreen getRealmsScreen()
    {
        return this.realmsScreen;
    }

    public void initGui()
    {
        this.realmsScreen.init();
        super.initGui();
    }

    public void drawCenteredString(String text, int x, int y, int color)
    {
        super.drawCenteredString(this.fontRendererObj, text, x, y, color);
    }

    public void drawString(String text, int x, int y, int color, boolean shadow)
    {
        if (shadow)
        {
            super.drawString(this.fontRendererObj, text, x, y, color);
        }
        else
        {
            this.fontRendererObj.drawString(text, x, y, color);
        }
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        this.realmsScreen.blit(x, y, textureX, textureY, width, height);
        super.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public void drawDefaultBackground()
    {
        super.drawDefaultBackground();
    }

    public boolean doesGuiPauseGame()
    {
        return super.doesGuiPauseGame();
    }

    public void drawWorldBackground(int tint)
    {
        super.drawWorldBackground(tint);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.realmsScreen.render(mouseX, mouseY, partialTicks);
    }

    public void renderToolTip(ItemStack stack, int x, int y)
    {
        super.renderToolTip(stack, x, y);
    }

    public void drawCreativeTabHoveringText(String tabName, int mouseX, int mouseY)
    {
        super.drawCreativeTabHoveringText(tabName, mouseX, mouseY);
    }

    public void drawHoveringText(List<String> textLines, int x, int y)
    {
        super.drawHoveringText(textLines, x, y);
    }

    public void updateScreen()
    {
        this.realmsScreen.tick();
        super.updateScreen();
    }

    public int getFontHeight()
    {
        return this.fontRendererObj.FONT_HEIGHT;
    }

    public int getStringWidth(String text)
    {
        return this.fontRendererObj.getStringWidth(text);
    }

    public void drawStringWithShadow(String text, int x, int y, int color)
    {
        this.fontRendererObj.drawStringWithShadow(text, (float)x, (float)y, color);
    }

    public List<String> listFormattedStringToWidth(String text, int width)
    {
        return this.fontRendererObj.listFormattedStringToWidth(text, width);
    }

    public final void actionPerformed(GuiButton button) throws IOException
    {
        this.realmsScreen.buttonClicked(((GuiButtonRealmsProxy)button).getRealmsButton());
    }

    public void clearButtons()
    {
        super.buttonList.clear();
    }

    public void addButton(RealmsButton button)
    {
        super.buttonList.add(button.getProxy());
    }

    public List<RealmsButton> getButtonList()
    {
        List<RealmsButton> list = Lists.<RealmsButton>newArrayListWithExpectedSize(super.buttonList.size());

        for (GuiButton guibutton : super.buttonList)
        {
            list.add(((GuiButtonRealmsProxy)guibutton).getRealmsButton());
        }

        return list;
    }

    public void removeButton(RealmsButton button)
    {
        super.buttonList.remove(button.getProxy());
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        this.realmsScreen.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void handleMouseInput() throws IOException
    {
        this.realmsScreen.mouseEvent();
        super.handleMouseInput();
    }

    public void handleKeyboardInput() throws IOException
    {
        this.realmsScreen.keyboardEvent();
        super.handleKeyboardInput();
    }

    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.realmsScreen.mouseReleased(mouseX, mouseY, state);
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        this.realmsScreen.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.realmsScreen.keyPressed(typedChar, keyCode);
    }

    public void confirmClicked(boolean result, int id)
    {
        this.realmsScreen.confirmResult(result, id);
    }

    public void onGuiClosed()
    {
        this.realmsScreen.removed();
        super.onGuiClosed();
    }
}
