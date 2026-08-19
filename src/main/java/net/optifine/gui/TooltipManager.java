package net.optifine.gui;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class TooltipManager
{
    private GuiScreen guiScreen;
    private TooltipProvider tooltipProvider;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    public TooltipManager(GuiScreen guiScreen, TooltipProvider tooltipProvider)
    {
        this.guiScreen = guiScreen;
        this.tooltipProvider = tooltipProvider;
    }

    public void drawTooltips(int x, int y, List buttonList)
    {
        if (Math.abs(x - this.lastMouseX) <= 5 && Math.abs(y - this.lastMouseY) <= 5)
        {
            int tooltipDelayMs = 700;

            if (System.currentTimeMillis() >= this.mouseStillTime + (long)tooltipDelayMs)
            {
                GuiButton selectedButton = GuiScreenOF.getSelectedButton(x, y, buttonList);

                if (selectedButton != null)
                {
                    Rectangle rectangle = this.tooltipProvider.getTooltipBounds(this.guiScreen, x, y);
                    String[] tooltipLines = this.tooltipProvider.getTooltipLines(selectedButton, rectangle.width);

                    if (tooltipLines != null)
                    {
                        if (tooltipLines.length > 8)
                        {
                            tooltipLines = (String[])Arrays.copyOf(tooltipLines, 8);
                            tooltipLines[tooltipLines.length - 1] = tooltipLines[tooltipLines.length - 1] + " ...";
                        }

                        if (this.tooltipProvider.isRenderBorder())
                        {
                            int borderColor = -528449408;
                            this.drawRectBorder(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, borderColor);
                        }

                        Gui.drawRect(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, -536870912);

                        for (int lineIndex = 0; lineIndex < tooltipLines.length; ++lineIndex)
                        {
                            String line = tooltipLines[lineIndex];
                            int textColor = 14540253;

                            if (line.endsWith("!"))
                            {
                                textColor = 16719904;
                            }

                            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                            fontRenderer.drawStringWithShadow(line, (float)(rectangle.x + 5), (float)(rectangle.y + 5 + lineIndex * 11), textColor);
                        }
                    }
                }
            }
        }
        else
        {
            this.lastMouseX = x;
            this.lastMouseY = y;
            this.mouseStillTime = System.currentTimeMillis();
        }
    }

    private void drawRectBorder(int left, int top, int right, int bottom, int color)
    {
        Gui.drawRect(left, top - 1, right, top, color);
        Gui.drawRect(left, bottom, right, bottom + 1, color);
        Gui.drawRect(left - 1, top, left, bottom, color);
        Gui.drawRect(right, top, right + 1, bottom, color);
    }
}
