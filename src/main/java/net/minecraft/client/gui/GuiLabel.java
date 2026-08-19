package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class GuiLabel extends Gui
{
    protected int width = 200;
    protected int height = 20;
    public int xPosition;
    public int yPosition;
    private List<String> lines;
    public int id;
    private boolean centered;
    public boolean visible = true;
    private boolean labelBgEnabled;
    private int textColor;
    private int backgroundColor;
    private int topBorderColor;
    private int bottomBorderColor;
    private FontRenderer fontRenderer;
    private int padding;

    public GuiLabel(FontRenderer fontRendererObj, int idIn, int xIn, int yIn, int widthIn, int heightIn, int colorIn)
    {
        this.fontRenderer = fontRendererObj;
        this.id = idIn;
        this.xPosition = xIn;
        this.yPosition = yIn;
        this.width = widthIn;
        this.height = heightIn;
        this.lines = Lists.<String>newArrayList();
        this.centered = false;
        this.labelBgEnabled = false;
        this.textColor = colorIn;
        this.backgroundColor = -1;
        this.topBorderColor = -1;
        this.bottomBorderColor = -1;
        this.padding = 0;
    }

    public void addLine(String translationKey)
    {
        this.lines.add(I18n.format(translationKey, new Object[0]));
    }

    public GuiLabel setCentered()
    {
        this.centered = true;
        return this;
    }

    public void drawLabel(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            this.drawLabelBackground(mc, mouseX, mouseY);
            int i = this.yPosition + this.height / 2 + this.padding / 2;
            int j = i - this.lines.size() * 10 / 2;

            for (int k = 0; k < this.lines.size(); ++k)
            {
                if (this.centered)
                {
                    this.drawCenteredString(this.fontRenderer, this.lines.get(k), this.xPosition + this.width / 2, j + k * 10, this.textColor);
                }
                else
                {
                    this.drawString(this.fontRenderer, this.lines.get(k), this.xPosition, j + k * 10, this.textColor);
                }
            }
        }
    }

    protected void drawLabelBackground(Minecraft mcIn, int mouseX, int mouseY)
    {
        if (this.labelBgEnabled)
        {
            int i = this.width + this.padding * 2;
            int j = this.height + this.padding * 2;
            int k = this.xPosition - this.padding;
            int l = this.yPosition - this.padding;
            drawRect(k, l, k + i, l + j, this.backgroundColor);
            this.drawHorizontalLine(k, k + i, l, this.topBorderColor);
            this.drawHorizontalLine(k, k + i, l + j, this.bottomBorderColor);
            this.drawVerticalLine(k, l, l + j, this.topBorderColor);
            this.drawVerticalLine(k + i, l, l + j, this.bottomBorderColor);
        }
    }
}
