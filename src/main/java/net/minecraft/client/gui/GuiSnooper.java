package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiSnooper extends GuiScreen
{
    private final GuiScreen parentScreen;
    private final GameSettings gameSettings;
    private final java.util.List<String> snooperKeys = Lists.<String>newArrayList();
    private final java.util.List<String> snooperValues = Lists.<String>newArrayList();
    private String title;
    private String[] descriptionLines;
    private GuiSnooper.List snooperList;
    private GuiButton toggleButton;

    public GuiSnooper(GuiScreen parentScreenIn, GameSettings gameSettingsIn)
    {
        this.parentScreen = parentScreenIn;
        this.gameSettings = gameSettingsIn;
    }

    public void initGui()
    {
        this.title = I18n.format("options.snooper.title", new Object[0]);
        String descriptionText = I18n.format("options.snooper.desc", new Object[0]);
        java.util.List<String> wrappedDescriptionLines = Lists.<String>newArrayList();

        for (String descriptionLine : this.fontRendererObj.listFormattedStringToWidth(descriptionText, this.width - 30))
        {
            wrappedDescriptionLines.add(descriptionLine);
        }

        this.descriptionLines = (String[])wrappedDescriptionLines.toArray(new String[wrappedDescriptionLines.size()]);
        this.snooperKeys.clear();
        this.snooperValues.clear();
        this.buttonList.add(this.toggleButton = new GuiButton(1, this.width / 2 - 152, this.height - 30, 150, 20, this.gameSettings.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED)));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 2, this.height - 30, 150, 20, I18n.format("gui.done", new Object[0])));
        boolean hasIntegratedServerSnooper = this.mc.getIntegratedServer() != null && this.mc.getIntegratedServer().getPlayerUsageSnooper() != null;

        for (Entry<String, String> clientEntry : (new TreeMap<String, String>(this.mc.getPlayerUsageSnooper().getCurrentStats())).entrySet())
        {
            this.snooperKeys.add((hasIntegratedServerSnooper ? "C " : "") + clientEntry.getKey());
            this.snooperValues.add(this.fontRendererObj.trimStringToWidth(clientEntry.getValue(), this.width - 220));
        }

        if (hasIntegratedServerSnooper)
        {
            for (Entry<String, String> serverEntry : (new TreeMap<String, String>(this.mc.getIntegratedServer().getPlayerUsageSnooper().getCurrentStats())).entrySet())
            {
                this.snooperKeys.add("S " + serverEntry.getKey());
                this.snooperValues.add(this.fontRendererObj.trimStringToWidth(serverEntry.getValue(), this.width - 220));
            }
        }

        this.snooperList = new GuiSnooper.List();
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.snooperList.handleMouseInput();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 2)
            {
                this.gameSettings.saveOptions();
                this.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.parentScreen);
            }

            if (button.id == 1)
            {
                this.gameSettings.setOptionValue(GameSettings.Options.SNOOPER_ENABLED, 1);
                this.toggleButton.displayString = this.gameSettings.getKeyBinding(GameSettings.Options.SNOOPER_ENABLED);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.snooperList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
        int descriptionY = 22;

        for (String descriptionLine : this.descriptionLines)
        {
            this.drawCenteredString(this.fontRendererObj, descriptionLine, this.width / 2, descriptionY, 8421504);
            descriptionY += this.fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot
    {
        public List()
        {
            super(GuiSnooper.this.mc, GuiSnooper.this.width, GuiSnooper.this.height, 80, GuiSnooper.this.height - 40, GuiSnooper.this.fontRendererObj.FONT_HEIGHT + 1);
        }

        protected int getSize()
        {
            return GuiSnooper.this.snooperKeys.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            GuiSnooper.this.fontRendererObj.drawString(GuiSnooper.this.snooperKeys.get(entryID), 10, y, 16777215);
            GuiSnooper.this.fontRendererObj.drawString(GuiSnooper.this.snooperValues.get(entryID), 230, y, 16777215);
        }

        protected int getScrollBarX()
        {
            return this.width - 10;
        }
    }
}
