package net.minecraft.client.gui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiSelectWorld extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private final DateFormat dateFormatter = new SimpleDateFormat();
    protected GuiScreen parentScreen;
    protected String screenTitle = "Select world";
    private boolean deleting;
    private int selectedIndex;
    private java.util.List<SaveFormatComparator> saveList;
    private GuiSelectWorld.List availableWorlds;
    private String worldVersus;
    private String conversionWarning;
    private String[] gameModeNames = new String[4];
    private boolean confirmingDelete;
    private GuiButton deleteButton;
    private GuiButton selectButton;
    private GuiButton renameButton;
    private GuiButton recreateButton;

    public GuiSelectWorld(GuiScreen parentScreenIn)
    {
        this.parentScreen = parentScreenIn;
    }

    public void initGui()
    {
        this.screenTitle = I18n.format("selectWorld.title", new Object[0]);

        try
        {
            this.loadLevelList();
        }
        catch (AnvilConverterException anvilConverterException)
        {
            logger.error((String)"Couldn\'t load level list", (Throwable)anvilConverterException);
            this.mc.displayGuiScreen(new GuiErrorScreen("Unable to load worlds", anvilConverterException.getMessage()));
            return;
        }

        this.worldVersus = I18n.format("selectWorld.world", new Object[0]);
        this.conversionWarning = I18n.format("selectWorld.conversion", new Object[0]);
        this.gameModeNames[WorldSettings.GameType.SURVIVAL.getID()] = I18n.format("gameMode.survival", new Object[0]);
        this.gameModeNames[WorldSettings.GameType.CREATIVE.getID()] = I18n.format("gameMode.creative", new Object[0]);
        this.gameModeNames[WorldSettings.GameType.ADVENTURE.getID()] = I18n.format("gameMode.adventure", new Object[0]);
        this.gameModeNames[WorldSettings.GameType.SPECTATOR.getID()] = I18n.format("gameMode.spectator", new Object[0]);
        this.availableWorlds = new GuiSelectWorld.List(this.mc);
        this.availableWorlds.registerScrollButtons(4, 5);
        this.addWorldSelectionButtons();
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.availableWorlds.handleMouseInput();
    }

    private void loadLevelList() throws AnvilConverterException
    {
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        this.saveList = isaveformat.getSaveList();
        Collections.sort(this.saveList);
        this.selectedIndex = -1;
    }

    protected String getSaveFileName(int index)
    {
        return ((SaveFormatComparator)this.saveList.get(index)).getFileName();
    }

    protected String getSaveName(int index)
    {
        String s = ((SaveFormatComparator)this.saveList.get(index)).getDisplayName();

        if (StringUtils.isEmpty(s))
        {
            s = I18n.format("selectWorld.world", new Object[0]) + " " + (index + 1);
        }

        return s;
    }

    public void addWorldSelectionButtons()
    {
        this.buttonList.add(this.selectButton = new GuiButton(1, this.width / 2 - 154, this.height - 52, 150, 20, I18n.format("selectWorld.select", new Object[0])));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("selectWorld.create", new Object[0])));
        this.buttonList.add(this.renameButton = new GuiButton(6, this.width / 2 - 154, this.height - 28, 72, 20, I18n.format("selectWorld.rename", new Object[0])));
        this.buttonList.add(this.deleteButton = new GuiButton(2, this.width / 2 - 76, this.height - 28, 72, 20, I18n.format("selectWorld.delete", new Object[0])));
        this.buttonList.add(this.recreateButton = new GuiButton(7, this.width / 2 + 4, this.height - 28, 72, 20, I18n.format("selectWorld.recreate", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 82, this.height - 28, 72, 20, I18n.format("gui.cancel", new Object[0])));
        this.selectButton.enabled = false;
        this.deleteButton.enabled = false;
        this.renameButton.enabled = false;
        this.recreateButton.enabled = false;
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 2)
            {
                String s = this.getSaveName(this.selectedIndex);

                if (s != null)
                {
                    this.confirmingDelete = true;
                    GuiYesNo guiyesno = makeDeleteWorldYesNo(this, s, this.selectedIndex);
                    this.mc.displayGuiScreen(guiyesno);
                }
            }
            else if (button.id == 1)
            {
                this.selectWorld(this.selectedIndex);
            }
            else if (button.id == 3)
            {
                this.mc.displayGuiScreen(new GuiCreateWorld(this));
            }
            else if (button.id == 6)
            {
                this.mc.displayGuiScreen(new GuiRenameWorld(this, this.getSaveFileName(this.selectedIndex)));
            }
            else if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 7)
            {
                GuiCreateWorld guicreateworld = new GuiCreateWorld(this);
                ISaveHandler isavehandler = this.mc.getSaveLoader().getSaveLoader(this.getSaveFileName(this.selectedIndex), false);
                WorldInfo worldinfo = isavehandler.loadWorldInfo();
                isavehandler.flush();
                guicreateworld.recreateFromExistingWorld(worldinfo);
                this.mc.displayGuiScreen(guicreateworld);
            }
            else
            {
                this.availableWorlds.actionPerformed(button);
            }
        }
    }

    public void selectWorld(int index)
    {
        this.mc.displayGuiScreen((GuiScreen)null);

        if (!this.deleting)
        {
            this.deleting = true;
            String s = this.getSaveFileName(index);

            if (s == null)
            {
                s = "World" + index;
            }

            String stringValue = this.getSaveName(index);

            if (stringValue == null)
            {
                stringValue = "World" + index;
            }

            if (this.mc.getSaveLoader().canLoadWorld(s))
            {
                this.mc.launchIntegratedServer(s, stringValue, (WorldSettings)null);
            }
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (this.confirmingDelete)
        {
            this.confirmingDelete = false;

            if (result)
            {
                ISaveFormat isaveformat = this.mc.getSaveLoader();
                isaveformat.flushCache();
                isaveformat.deleteWorldDirectory(this.getSaveFileName(id));

                try
                {
                    this.loadLevelList();
                }
                catch (AnvilConverterException anvilConverterException)
                {
                    logger.error((String)"Couldn\'t load level list", (Throwable)anvilConverterException);
                }
            }

            this.mc.displayGuiScreen(this);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.availableWorlds.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static GuiYesNo makeDeleteWorldYesNo(GuiYesNoCallback selectWorld, String name, int id)
    {
        String s = I18n.format("selectWorld.deleteQuestion", new Object[0]);
        String message = "\'" + name + "\' " + I18n.format("selectWorld.deleteWarning", new Object[0]);
        String text2 = I18n.format("selectWorld.deleteButton", new Object[0]);
        String text3 = I18n.format("gui.cancel", new Object[0]);
        GuiYesNo guiYesNo = new GuiYesNo(selectWorld, s, message, text2, text3, id);
        return guiYesNo;
    }

    class List extends GuiSlot
    {
        public List(Minecraft mcIn)
        {
            super(mcIn, GuiSelectWorld.this.width, GuiSelectWorld.this.height, 32, GuiSelectWorld.this.height - 64, 36);
        }

        protected int getSize()
        {
            return GuiSelectWorld.this.saveList.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            GuiSelectWorld.this.selectedIndex = slotIndex;
            boolean flag = GuiSelectWorld.this.selectedIndex >= 0 && GuiSelectWorld.this.selectedIndex < this.getSize();
            GuiSelectWorld.this.selectButton.enabled = flag;
            GuiSelectWorld.this.deleteButton.enabled = flag;
            GuiSelectWorld.this.renameButton.enabled = flag;
            GuiSelectWorld.this.recreateButton.enabled = flag;

            if (isDoubleClick && flag)
            {
                GuiSelectWorld.this.selectWorld(slotIndex);
            }
        }

        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == GuiSelectWorld.this.selectedIndex;
        }

        protected int getContentHeight()
        {
            return GuiSelectWorld.this.saveList.size() * 36;
        }

        protected void drawBackground()
        {
            GuiSelectWorld.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            SaveFormatComparator saveFormatComparator = (SaveFormatComparator)GuiSelectWorld.this.saveList.get(entryID);
            String s = saveFormatComparator.getDisplayName();

            if (StringUtils.isEmpty(s))
            {
                s = GuiSelectWorld.this.worldVersus + " " + (entryID + 1);
            }

            String text2 = saveFormatComparator.getFileName();
            text2 = text2 + " (" + GuiSelectWorld.this.dateFormatter.format(new Date(saveFormatComparator.getLastTimePlayed()));
            text2 = text2 + ")";
            String secondStringValue = "";

            if (saveFormatComparator.requiresConversion())
            {
                secondStringValue = GuiSelectWorld.this.conversionWarning + " " + secondStringValue;
            }
            else
            {
                secondStringValue = GuiSelectWorld.this.gameModeNames[saveFormatComparator.getEnumGameType().getID()];

                if (saveFormatComparator.isHardcoreModeEnabled())
                {
                    secondStringValue = EnumChatFormatting.DARK_RED + I18n.format("gameMode.hardcore", new Object[0]) + EnumChatFormatting.RESET;
                }

                if (saveFormatComparator.getCheatsEnabled())
                {
                    secondStringValue = secondStringValue + ", " + I18n.format("selectWorld.cheats", new Object[0]);
                }
            }

            GuiSelectWorld.this.drawString(GuiSelectWorld.this.fontRendererObj, s, x + 2, y + 1, 16777215);
            GuiSelectWorld.this.drawString(GuiSelectWorld.this.fontRendererObj, text2, x + 2, y + 12, 8421504);
            GuiSelectWorld.this.drawString(GuiSelectWorld.this.fontRendererObj, secondStringValue, x + 2, y + 22, 8421504);
        }
    }
}
