package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.client.GameWindow;

public class GuiRenameWorld extends GuiScreen
{
    private GuiScreen parentScreen;
    private GuiTextField worldNameField;
    private final String saveName;

    public GuiRenameWorld(GuiScreen parentScreenIn, String saveNameIn)
    {
        this.parentScreen = parentScreenIn;
        this.saveName = saveNameIn;
    }

    public void updateScreen()
    {
        this.worldNameField.updateCursorCounter();
    }

    public void initGui()
    {
        GameWindow.setRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, I18n.format("selectWorld.renameButton", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
        ISaveFormat saveFormat = this.mc.getSaveLoader();
        WorldInfo worldInfo = saveFormat.getWorldInfo(this.saveName);
        String worldName = worldInfo.getWorldName();
        this.worldNameField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.worldNameField.setFocused(true);
        this.worldNameField.setText(worldName);
    }

    public void onGuiClosed()
    {
        GameWindow.setRepeatEvents(false);
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 0)
            {
                ISaveFormat saveFormat = this.mc.getSaveLoader();
                saveFormat.renameWorld(this.saveName, this.worldNameField.getText().trim());
                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.worldNameField.textboxKeyTyped(typedChar, keyCode);
        this.buttonList.get(0).enabled = this.worldNameField.getText().trim().length() > 0;

        if (keyCode == 28 || keyCode == 156)
        {
            this.actionPerformed(this.buttonList.get(0));
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.worldNameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("selectWorld.renameTitle", new Object[0]), this.width / 2, 20, 16777215);
        this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName", new Object[0]), this.width / 2 - 100, 47, 10526880);
        this.worldNameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
