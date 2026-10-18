package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class GuiOptionsRowList extends GuiListExtended
{
    private final List<GuiOptionsRowList.Row> options = Lists.<GuiOptionsRowList.Row>newArrayList();

    public GuiOptionsRowList(Minecraft mcIn, int listWidth, int listHeight, int top, int bottom, int slotHeight, GameSettings.Options... options)
    {
        super(mcIn, listWidth, listHeight, top, bottom, slotHeight);
        this.canBeScrolled = false;

        for (int i = 0; i < options.length; i += 2)
        {
            GameSettings.Options gamesettings$options = options[i];
            GameSettings.Options gamesettings$options1 = i < options.length - 1 ? options[i + 1] : null;
            GuiButton guibutton = this.createOptionButton(mcIn, listWidth / 2 - 155, 0, gamesettings$options);
            GuiButton guibutton1 = this.createOptionButton(mcIn, listWidth / 2 - 155 + 160, 0, gamesettings$options1);
            this.options.add(new GuiOptionsRowList.Row(guibutton, guibutton1));
        }
    }

    private GuiButton createOptionButton(Minecraft mcIn, int x, int y, GameSettings.Options options)
    {
        if (options == null)
        {
            return null;
        }
        else
        {
            int i = options.returnEnumOrdinal();
            return (GuiButton)(options.getEnumFloat() ? new GuiOptionSlider(i, x, y, options) : new GuiOptionButton(i, x, y, options, mcIn.gameSettings.getKeyBinding(options)));
        }
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    public GuiOptionsRowList.Row getListEntry(int index)
    {
        return (GuiOptionsRowList.Row)this.options.get(index);
    }

    protected int getSize()
    {
        return this.options.size();
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return 400;
    }

    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 32;
    }

    public static class Row implements GuiListExtended.IGuiListEntry
    {
        private final Minecraft minecraft = Minecraft.getMinecraft();
        private final GuiButton leftButton;
        private final GuiButton rightButton;

        public Row(GuiButton buttonAIn, GuiButton buttonBIn)
        {
            this.leftButton = buttonAIn;
            this.rightButton = buttonBIn;
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            if (this.leftButton != null)
            {
                this.leftButton.yPosition = y;
                this.leftButton.drawButton(this.minecraft, mouseX, mouseY);
            }

            if (this.rightButton != null)
            {
                this.rightButton.yPosition = y;
                this.rightButton.drawButton(this.minecraft, mouseX, mouseY);
            }
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            if (this.leftButton.mousePressed(this.minecraft, mouseX, mouseY))
            {
                if (this.leftButton instanceof GuiOptionButton)
                {
                    this.minecraft.gameSettings.setOptionValue(((GuiOptionButton)this.leftButton).returnEnumOptions(), 1);
                    this.leftButton.displayString = this.minecraft.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.leftButton.id));
                }

                return true;
            }
            else if (this.rightButton != null && this.rightButton.mousePressed(this.minecraft, mouseX, mouseY))
            {
                if (this.rightButton instanceof GuiOptionButton)
                {
                    this.minecraft.gameSettings.setOptionValue(((GuiOptionButton)this.rightButton).returnEnumOptions(), 1);
                    this.rightButton.displayString = this.minecraft.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.rightButton.id));
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            if (this.leftButton != null)
            {
                this.leftButton.mouseReleased(x, y);
            }

            if (this.rightButton != null)
            {
                this.rightButton.mouseReleased(x, y);
            }
        }

        public void setSelected(int slotIndex, int x, int y)
        {
        }
    }
}
