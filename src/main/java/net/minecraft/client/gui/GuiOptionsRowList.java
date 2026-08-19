package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class GuiOptionsRowList extends GuiListExtended
{
    private final List<GuiOptionsRowList.Row> options = Lists.<GuiOptionsRowList.Row>newArrayList();

    public GuiOptionsRowList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GameSettings.Options... settingsOptions)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.canBeScrolled = false;

        for (int optionIndex = 0; optionIndex < settingsOptions.length; optionIndex += 2)
        {
            GameSettings.Options leftOption = settingsOptions[optionIndex];
            GameSettings.Options rightOption = optionIndex < settingsOptions.length - 1 ? settingsOptions[optionIndex + 1] : null;
            GuiButton leftButton = this.createButton(mcIn, widthIn / 2 - 155, 0, leftOption);
            GuiButton rightButton = this.createButton(mcIn, widthIn / 2 - 155 + 160, 0, rightOption);
            this.options.add(new GuiOptionsRowList.Row(leftButton, rightButton));
        }
    }

    private GuiButton createButton(Minecraft mcIn, int x, int y, GameSettings.Options option)
    {
        if (option == null)
        {
            return null;
        }
        else
        {
            int optionId = option.returnEnumOrdinal();
            return (GuiButton)(option.getEnumFloat() ? new GuiOptionSlider(optionId, x, y, option) : new GuiOptionButton(optionId, x, y, option, mcIn.gameSettings.getKeyBinding(option)));
        }
    }

    public GuiOptionsRowList.Row getListEntry(int index)
    {
        return (GuiOptionsRowList.Row)this.options.get(index);
    }

    protected int getSize()
    {
        return this.options.size();
    }

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
        private final GuiButton buttonA;
        private final GuiButton buttonB;

        public Row(GuiButton buttonAIn, GuiButton buttonBIn)
        {
            this.buttonA = buttonAIn;
            this.buttonB = buttonBIn;
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            if (this.buttonA != null)
            {
                this.buttonA.yPosition = y;
                this.buttonA.drawButton(this.minecraft, mouseX, mouseY);
            }

            if (this.buttonB != null)
            {
                this.buttonB.yPosition = y;
                this.buttonB.drawButton(this.minecraft, mouseX, mouseY);
            }
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            if (this.buttonA.mousePressed(this.minecraft, mouseX, mouseY))
            {
                if (this.buttonA instanceof GuiOptionButton)
                {
                    this.minecraft.gameSettings.setOptionValue(((GuiOptionButton)this.buttonA).returnEnumOptions(), 1);
                    this.buttonA.displayString = this.minecraft.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.buttonA.id));
                }

                return true;
            }
            else if (this.buttonB != null && this.buttonB.mousePressed(this.minecraft, mouseX, mouseY))
            {
                if (this.buttonB instanceof GuiOptionButton)
                {
                    this.minecraft.gameSettings.setOptionValue(((GuiOptionButton)this.buttonB).returnEnumOptions(), 1);
                    this.buttonB.displayString = this.minecraft.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(this.buttonB.id));
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
            if (this.buttonA != null)
            {
                this.buttonA.mouseReleased(x, y);
            }

            if (this.buttonB != null)
            {
                this.buttonB.mouseReleased(x, y);
            }
        }

        public void setSelected(int slotIndex, int x, int y)
        {
        }
    }
}
