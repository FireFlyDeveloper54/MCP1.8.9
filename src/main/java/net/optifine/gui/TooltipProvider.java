package net.optifine.gui;

import java.awt.Rectangle;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface TooltipProvider
{
    Rectangle getTooltipBounds(GuiScreen guiScreen, int x, int y);

    String[] getTooltipLines(GuiButton button, int width);

    boolean isRenderBorder();
}
