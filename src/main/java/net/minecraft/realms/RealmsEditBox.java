package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class RealmsEditBox
{
    private final GuiTextField editBox;

    public RealmsEditBox(int id, int x, int y, int width, int height)
    {
        this.editBox = new GuiTextField(id, Minecraft.getMinecraft().fontRendererObj, x, y, width, height);
    }

    public String getValue()
    {
        return this.editBox.getText();
    }

    public void tick()
    {
        this.editBox.updateCursorCounter();
    }

    public void setFocus(boolean focused)
    {
        this.editBox.setFocused(focused);
    }

    public void setValue(String value)
    {
        this.editBox.setText(value);
    }

    public void keyPressed(char typedChar, int keyCode)
    {
        this.editBox.textboxKeyTyped(typedChar, keyCode);
    }

    public boolean isFocused()
    {
        return this.editBox.isFocused();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.editBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void render()
    {
        this.editBox.drawTextBox();
    }

    public void setMaxLength(int maxLength)
    {
        this.editBox.setMaxStringLength(maxLength);
    }

    public void setIsEditable(boolean editable)
    {
        this.editBox.setEnabled(editable);
    }
}
