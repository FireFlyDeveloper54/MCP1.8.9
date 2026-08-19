package net.optifine.gui;

import java.awt.Rectangle;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.optifine.shaders.config.EnumShaderOption;
import net.optifine.shaders.gui.GuiButtonDownloadShaders;
import net.optifine.shaders.gui.GuiButtonEnumShaderOption;

public class TooltipProviderEnumShaderOptions implements TooltipProvider
{
    public Rectangle getTooltipBounds(GuiScreen guiScreen, int x, int y)
    {
        int tooltipX = guiScreen.width - 450;
        int tooltipY = 35;

        if (tooltipX < 10)
        {
            tooltipX = 10;
        }

        if (y <= tooltipY + 94)
        {
            tooltipY += 100;
        }

        int tooltipRight = tooltipX + 150 + 150;
        int tooltipBottom = tooltipY + 84 + 10;
        return new Rectangle(tooltipX, tooltipY, tooltipRight - tooltipX, tooltipBottom - tooltipY);
    }

    public boolean isRenderBorder()
    {
        return true;
    }

    public String[] getTooltipLines(GuiButton btn, int width)
    {
        if (btn instanceof GuiButtonDownloadShaders)
        {
            return TooltipProviderOptions.getTooltipLines("of.options.shaders.DOWNLOAD");
        }
        else if (!(btn instanceof GuiButtonEnumShaderOption))
        {
            return null;
        }
        else
        {
            GuiButtonEnumShaderOption guiButtonEnumShaderOption = (GuiButtonEnumShaderOption)btn;
            EnumShaderOption shaderOption = guiButtonEnumShaderOption.getEnumShaderOption();
            String[] tooltipLines = this.getTooltipLines(shaderOption);
            return tooltipLines;
        }
    }

    private String[] getTooltipLines(EnumShaderOption option)
    {
        return TooltipProviderOptions.getTooltipLines(option.getResourceKey());
    }
}
