package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class GuiListButton extends GuiButton
{
    private boolean value;
    private String localizationStr;
    private final GuiPageButtonList.GuiResponder guiResponder;

    public GuiListButton(GuiPageButtonList.GuiResponder responder, int id, int x, int y, String localizationKey, boolean initialValue)
    {
        super(id, x, y, 150, 20, "");
        this.localizationStr = localizationKey;
        this.value = initialValue;
        this.displayString = this.buildDisplayString();
        this.guiResponder = responder;
    }

    private String buildDisplayString()
    {
        return I18n.format(this.localizationStr, new Object[0]) + ": " + (this.value ? I18n.format("gui.yes", new Object[0]) : I18n.format("gui.no", new Object[0]));
    }

    public void setValue(boolean value)
    {
        this.value = value;
        this.displayString = this.buildDisplayString();
        this.guiResponder.setEntryValue(this.id, value);
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            this.value = !this.value;
            this.displayString = this.buildDisplayString();
            this.guiResponder.setEntryValue(this.id, this.value);
            return true;
        }
        else
        {
            return false;
        }
    }
}
