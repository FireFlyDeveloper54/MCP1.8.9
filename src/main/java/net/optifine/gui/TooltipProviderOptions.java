package net.optifine.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.optifine.Lang;

public class TooltipProviderOptions implements TooltipProvider
{
    public Rectangle getTooltipBounds(GuiScreen guiScreen, int x, int y)
    {
        int left = guiScreen.width / 2 - 150;
        int top = guiScreen.height / 6 - 7;

        if (y <= top + 98)
        {
            top += 105;
        }

        int right = left + 150 + 150;
        int bottom = top + 84 + 10;
        return new Rectangle(left, top, right - left, bottom - top);
    }

    public boolean isRenderBorder()
    {
        return false;
    }

    public String[] getTooltipLines(GuiButton btn, int width)
    {
        if (!(btn instanceof IOptionControl))
        {
            return null;
        }
        else
        {
            IOptionControl optionControl = (IOptionControl)btn;
            GameSettings.Options option = optionControl.getOption();
            String[] tooltipLines = getTooltipLines(option.getEnumString());
            return tooltipLines;
        }
    }

    public static String[] getTooltipLines(String key)
    {
        List<String> tooltipLines = new ArrayList();

        for (int lineIndex = 0; lineIndex < 10; ++lineIndex)
        {
            String tooltipKey = key + ".tooltip." + (lineIndex + 1);
            String tooltipLine = Lang.get(tooltipKey, (String)null);

            if (tooltipLine == null)
            {
                break;
            }

            tooltipLines.add(tooltipLine);
        }

        if (tooltipLines.size() <= 0)
        {
            return null;
        }
        else
        {
            String[] tooltipLineArray = (String[])((String[])tooltipLines.toArray(new String[tooltipLines.size()]));
            return tooltipLineArray;
        }
    }
}
