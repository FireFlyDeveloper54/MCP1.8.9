package net.optifine.gui;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;

public class GuiScreenOF extends GuiScreen
{
    protected void actionPerformedRightClick(GuiButton button) throws IOException
    {
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 1)
        {
            GuiButton guibutton = getSelectedButton(mouseX, mouseY, this.buttonList);

            if (guibutton != null && guibutton.enabled)
            {
                guibutton.playPressSound(this.mc.getSoundHandler());
                this.actionPerformedRightClick(guibutton);
            }
        }
    }

    public static GuiButton getSelectedButton(int x, int y, List<GuiButton> listButtons)
    {
        for (int buttonIndex = 0; buttonIndex < listButtons.size(); ++buttonIndex)
        {
            GuiButton guiButton = (GuiButton)listButtons.get(buttonIndex);

            if (guiButton.visible)
            {
                int buttonWidth = GuiVideoSettings.getButtonWidth(guiButton);
                int buttonHeight = GuiVideoSettings.getButtonHeight(guiButton);

                if (x >= guiButton.xPosition && y >= guiButton.yPosition && x < guiButton.xPosition + buttonWidth && y < guiButton.yPosition + buttonHeight)
                {
                    return guiButton;
                }
            }
        }

        return null;
    }
}
