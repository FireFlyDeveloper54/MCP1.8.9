package net.minecraft.client.gui;

import net.minecraft.client.settings.GameSettings;

public class GuiOptionButton extends GuiButton
{
    private final GameSettings.Options enumOptions;

    public GuiOptionButton(int id, int x, int y, String displayString)
    {
        this(id, x, y, (GameSettings.Options)null, displayString);
    }

    public GuiOptionButton(int id, int x, int y, int widthIn, int heightIn, String displayString)
    {
        super(id, x, y, widthIn, heightIn, displayString);
        this.enumOptions = null;
    }

    public GuiOptionButton(int id, int x, int y, GameSettings.Options option, String displayString)
    {
        super(id, x, y, 150, 20, displayString);
        this.enumOptions = option;
    }

    public GameSettings.Options returnEnumOptions()
    {
        return this.enumOptions;
    }
}
