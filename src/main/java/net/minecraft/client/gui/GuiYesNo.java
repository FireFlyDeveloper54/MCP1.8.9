package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.I18n;

public class GuiYesNo extends GuiScreen
{
    protected GuiYesNoCallback parentScreen;
    protected String messageLine1;
    private String messageLine2;
    private final List<String> multilineMessage = Lists.<String>newArrayList();
    protected String confirmButtonText;
    protected String cancelButtonText;
    protected int parentButtonClickedId;
    private int ticksUntilEnable;

    public GuiYesNo(GuiYesNoCallback parentScreenIn, String messageLine1In, String messageLine2In, int parentButtonClickedIdIn)
    {
        this.parentScreen = parentScreenIn;
        this.messageLine1 = messageLine1In;
        this.messageLine2 = messageLine2In;
        this.parentButtonClickedId = parentButtonClickedIdIn;
        this.confirmButtonText = I18n.format("gui.yes", new Object[0]);
        this.cancelButtonText = I18n.format("gui.no", new Object[0]);
    }

    public GuiYesNo(GuiYesNoCallback parentScreenIn, String messageLine1In, String messageLine2In, String confirmButtonTextIn, String cancelButtonTextIn, int parentButtonClickedIdIn)
    {
        this.parentScreen = parentScreenIn;
        this.messageLine1 = messageLine1In;
        this.messageLine2 = messageLine2In;
        this.confirmButtonText = confirmButtonTextIn;
        this.cancelButtonText = cancelButtonTextIn;
        this.parentButtonClickedId = parentButtonClickedIdIn;
    }

    public void initGui()
    {
        this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 155, this.height / 6 + 96, this.confirmButtonText));
        this.buttonList.add(new GuiOptionButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.cancelButtonText));
        this.multilineMessage.clear();
        this.multilineMessage.addAll(this.fontRendererObj.listFormattedStringToWidth(this.messageLine2, this.width - 50));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        this.parentScreen.confirmClicked(button.id == 0, this.parentButtonClickedId);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.messageLine1, this.width / 2, 70, 16777215);
        int messageY = 90;

        for (String messageLine : this.multilineMessage)
        {
            this.drawCenteredString(this.fontRendererObj, messageLine, this.width / 2, messageY, 16777215);
            messageY += this.fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void setButtonDelay(int ticksUntilEnableIn)
    {
        this.ticksUntilEnable = ticksUntilEnableIn;

        for (GuiButton guiButton : this.buttonList)
        {
            guiButton.enabled = false;
        }
    }

    public void updateScreen()
    {
        super.updateScreen();

        if (--this.ticksUntilEnable == 0)
        {
            for (GuiButton guiButton : this.buttonList)
            {
                guiButton.enabled = true;
            }
        }
    }
}
