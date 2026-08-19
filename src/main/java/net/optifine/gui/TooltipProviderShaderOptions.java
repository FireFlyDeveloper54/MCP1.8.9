package net.optifine.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.gui.GuiButtonShaderOption;
import net.optifine.util.StrUtils;

public class TooltipProviderShaderOptions extends TooltipProviderOptions
{
    public String[] getTooltipLines(GuiButton btn, int width)
    {
        if (!(btn instanceof GuiButtonShaderOption))
        {
            return null;
        }
        else
        {
            GuiButtonShaderOption shaderOptionButton = (GuiButtonShaderOption)btn;
            ShaderOption shaderOption = shaderOptionButton.getShaderOption();
            String[] tooltipLines = this.makeTooltipLines(shaderOption, width);
            return tooltipLines;
        }
    }

    private String[] makeTooltipLines(ShaderOption so, int width)
    {
        String nameText = so.getNameText();
        String descriptionText = Config.normalize(so.getDescriptionText()).trim();
        String[] descriptionLines = this.splitDescription(descriptionText);
        GameSettings gameSettings = Config.getGameSettings();
        String idLine = null;

        if (!nameText.equals(so.getName()) && gameSettings.advancedItemTooltips)
        {
            idLine = "\u00a78" + Lang.get("of.general.id") + ": " + so.getName();
        }

        String sourceLine = null;

        if (so.getPaths() != null && gameSettings.advancedItemTooltips)
        {
            sourceLine = "\u00a78" + Lang.get("of.general.from") + ": " + Config.arrayToString((Object[])so.getPaths());
        }

        String defaultLine = null;

        if (so.getValueDefault() != null && gameSettings.advancedItemTooltips)
        {
            String defaultValueText = so.isEnabled() ? so.getValueText(so.getValueDefault()) : Lang.get("of.general.ambiguous");
            defaultLine = "\u00a78" + Lang.getDefault() + ": " + defaultValueText;
        }

        List<String> lines = new ArrayList();
        lines.add(nameText);
        lines.addAll(Arrays.<String>asList(descriptionLines));

        if (idLine != null)
        {
            lines.add(idLine);
        }

        if (sourceLine != null)
        {
            lines.add(sourceLine);
        }

        if (defaultLine != null)
        {
            lines.add(defaultLine);
        }

        String[] tooltipLines = this.makeTooltipLines(width, lines);
        return tooltipLines;
    }

    private String[] splitDescription(String desc)
    {
        if (desc.length() <= 0)
        {
            return new String[0];
        }
        else
        {
            desc = StrUtils.removePrefix(desc, "//");
            String[] descriptionParts = desc.split("\\. ");

            for (int partIndex = 0; partIndex < descriptionParts.length; ++partIndex)
            {
                descriptionParts[partIndex] = "- " + descriptionParts[partIndex].trim();
                descriptionParts[partIndex] = StrUtils.removeSuffix(descriptionParts[partIndex], ".");
            }

            return descriptionParts;
        }
    }

    private String[] makeTooltipLines(int width, List<String> args)
    {
        FontRenderer fontRenderer = Config.getMinecraft().fontRendererObj;
        List<String> tooltipLines = new ArrayList();

        for (int lineIndex = 0; lineIndex < args.size(); ++lineIndex)
        {
            String line = (String)args.get(lineIndex);

            if (line != null && line.length() > 0)
            {
                for (String wrappedLine : fontRenderer.listFormattedStringToWidth(line, width))
                {
                    tooltipLines.add(wrappedLine);
                }
            }
        }

        String[] tooltipLineArray = (String[])((String[])tooltipLines.toArray(new String[tooltipLines.size()]));
        return tooltipLineArray;
    }
}
