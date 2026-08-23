package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.GameWindow;

public class GuiMultiplayer extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private final OldServerPinger oldServerPinger = new OldServerPinger();
    private GuiScreen parentScreen;
    private ServerSelectionList serverListSelector;
    private ServerList savedServerList;
    private GuiButton btnEditServer;
    private GuiButton btnSelectServer;
    private GuiButton btnDeleteServer;
    private boolean deletingServer;
    private boolean addingServer;
    private boolean editingServer;
    private boolean directConnect;
    private String hoveringText;
    private ServerData selectedServer;
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;
    private boolean initialized;

    public GuiMultiplayer(GuiScreen parentScreen)
    {
        this.parentScreen = parentScreen;
    }

    public void initGui()
    {
        GameWindow.setRepeatEvents(true);
        this.buttonList.clear();

        if (!this.initialized)
        {
            this.initialized = true;
            this.savedServerList = new ServerList(this.mc);
            this.savedServerList.loadServerList();
            this.lanServerList = new LanServerDetector.LanServerList();

            try
            {
                this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
                this.lanServerDetector.start();
            }
            catch (Exception exception)
            {
                logger.warn("Unable to start LAN server detection: " + exception.getMessage());
            }

            this.serverListSelector = new ServerSelectionList(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
            this.serverListSelector.updateOnlineServers(this.savedServerList);
        }
        else
        {
            this.serverListSelector.setDimensions(this.width, this.height, 32, this.height - 64);
        }

        this.createButtons();
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
    }

    public void createButtons()
    {
        this.buttonList.add(this.btnEditServer = new GuiButton(7, this.width / 2 - 154, this.height - 28, 70, 20, I18n.format("selectServer.edit", new Object[0])));
        this.buttonList.add(this.btnDeleteServer = new GuiButton(2, this.width / 2 - 74, this.height - 28, 70, 20, I18n.format("selectServer.delete", new Object[0])));
        this.buttonList.add(this.btnSelectServer = new GuiButton(1, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("selectServer.select", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("selectServer.direct", new Object[0])));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, I18n.format("selectServer.add", new Object[0])));
        this.buttonList.add(new GuiButton(8, this.width / 2 + 4, this.height - 28, 70, 20, I18n.format("selectServer.refresh", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 76, this.height - 28, 75, 20, I18n.format("gui.cancel", new Object[0])));
        this.selectServer(this.serverListSelector.getSelectedSlotIndex());
    }

    public void updateScreen()
    {
        super.updateScreen();

        if (this.lanServerList.getWasUpdated())
        {
            List<LanServerDetector.LanServer> list = this.lanServerList.getLanServers();
            this.lanServerList.setWasNotUpdated();
            this.serverListSelector.updateNetworkServers(list);
        }

        this.oldServerPinger.pingPendingNetworks();
    }

    public void onGuiClosed()
    {
        GameWindow.setRepeatEvents(false);

        if (this.lanServerDetector != null)
        {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }

        this.oldServerPinger.clearPendingNetworks();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            GuiListExtended.IGuiListEntry selectedEntry = this.serverListSelector.getSelectedSlotIndex() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.getSelectedSlotIndex());

            if (button.id == 2 && selectedEntry instanceof ServerListEntryNormal)
            {
                String serverName = ((ServerListEntryNormal)selectedEntry).getServerData().serverName;

                if (serverName != null)
                {
                    this.deletingServer = true;
                    String deleteQuestion = I18n.format("selectServer.deleteQuestion", new Object[0]);
                    String deleteWarning = "\'" + serverName + "\' " + I18n.format("selectServer.deleteWarning", new Object[0]);
                    String deleteButton = I18n.format("selectServer.deleteButton", new Object[0]);
                    String cancelButton = I18n.format("gui.cancel", new Object[0]);
                    GuiYesNo guiYesNo = new GuiYesNo(this, deleteQuestion, deleteWarning, deleteButton, cancelButton, this.serverListSelector.getSelectedSlotIndex());
                    this.mc.displayGuiScreen(guiYesNo);
                }
            }
            else if (button.id == 1)
            {
                this.connectToSelected();
            }
            else if (button.id == 4)
            {
                this.directConnect = true;
                this.mc.displayGuiScreen(new GuiScreenServerList(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName", new Object[0]), "", false)));
            }
            else if (button.id == 3)
            {
                this.addingServer = true;
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName", new Object[0]), "", false)));
            }
            else if (button.id == 7 && selectedEntry instanceof ServerListEntryNormal)
            {
                this.editingServer = true;
                ServerData serverData = ((ServerListEntryNormal)selectedEntry).getServerData();
                this.selectedServer = new ServerData(serverData.serverName, serverData.serverIP, false);
                this.selectedServer.copyFrom(serverData);
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer));
            }
            else if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 8)
            {
                this.refreshServerList();
            }
        }
    }

    private void refreshServerList()
    {
        this.mc.displayGuiScreen(new GuiMultiplayer(this.parentScreen));
    }

    public void confirmClicked(boolean result, int id)
    {
        GuiListExtended.IGuiListEntry selectedEntry = this.serverListSelector.getSelectedSlotIndex() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.getSelectedSlotIndex());

        if (this.deletingServer)
        {
            this.deletingServer = false;

            if (result && selectedEntry instanceof ServerListEntryNormal)
            {
                this.savedServerList.removeServerData(this.serverListSelector.getSelectedSlotIndex());
                this.savedServerList.saveServerList();
                this.serverListSelector.setSelectedSlotIndex(-1);
                this.serverListSelector.updateOnlineServers(this.savedServerList);
            }

            this.mc.displayGuiScreen(this);
        }
        else if (this.directConnect)
        {
            this.directConnect = false;

            if (result)
            {
                this.connectToServer(this.selectedServer);
            }
            else
            {
                this.mc.displayGuiScreen(this);
            }
        }
        else if (this.addingServer)
        {
            this.addingServer = false;

            if (result)
            {
                this.savedServerList.addServerData(this.selectedServer);
                this.savedServerList.saveServerList();
                this.serverListSelector.setSelectedSlotIndex(-1);
                this.serverListSelector.updateOnlineServers(this.savedServerList);
            }

            this.mc.displayGuiScreen(this);
        }
        else if (this.editingServer)
        {
            this.editingServer = false;

            if (result && selectedEntry instanceof ServerListEntryNormal)
            {
                ServerData serverData = ((ServerListEntryNormal)selectedEntry).getServerData();
                serverData.serverName = this.selectedServer.serverName;
                serverData.serverIP = this.selectedServer.serverIP;
                serverData.copyFrom(this.selectedServer);
                this.savedServerList.saveServerList();
                this.serverListSelector.updateOnlineServers(this.savedServerList);
            }

            this.mc.displayGuiScreen(this);
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        int i = this.serverListSelector.getSelectedSlotIndex();
        GuiListExtended.IGuiListEntry selectedEntry = i < 0 ? null : this.serverListSelector.getListEntry(i);

        if (keyCode == 63)
        {
            this.refreshServerList();
        }
        else
        {
            if (i >= 0)
            {
                if (keyCode == 200)
                {
                    if (isShiftKeyDown())
                    {
                        if (i > 0 && selectedEntry instanceof ServerListEntryNormal)
                        {
                            this.savedServerList.swapServers(i, i - 1);
                            this.selectServer(this.serverListSelector.getSelectedSlotIndex() - 1);
                            this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());
                            this.serverListSelector.updateOnlineServers(this.savedServerList);
                        }
                    }
                    else if (i > 0)
                    {
                        this.selectServer(this.serverListSelector.getSelectedSlotIndex() - 1);
                        this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());

                        if (this.serverListSelector.getListEntry(this.serverListSelector.getSelectedSlotIndex()) instanceof ServerListEntryLanScan)
                        {
                            if (this.serverListSelector.getSelectedSlotIndex() > 0)
                            {
                                this.selectServer(this.serverListSelector.getSize() - 1);
                                this.serverListSelector.scrollBy(-this.serverListSelector.getSlotHeight());
                            }
                            else
                            {
                                this.selectServer(-1);
                            }
                        }
                    }
                    else
                    {
                        this.selectServer(-1);
                    }
                }
                else if (keyCode == 208)
                {
                    if (isShiftKeyDown())
                    {
                        if (i < this.savedServerList.countServers() - 1)
                        {
                            this.savedServerList.swapServers(i, i + 1);
                            this.selectServer(i + 1);
                            this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());
                            this.serverListSelector.updateOnlineServers(this.savedServerList);
                        }
                    }
                    else if (i < this.serverListSelector.getSize())
                    {
                        this.selectServer(this.serverListSelector.getSelectedSlotIndex() + 1);
                        this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());

                        if (this.serverListSelector.getListEntry(this.serverListSelector.getSelectedSlotIndex()) instanceof ServerListEntryLanScan)
                        {
                            if (this.serverListSelector.getSelectedSlotIndex() < this.serverListSelector.getSize() - 1)
                            {
                                this.selectServer(this.serverListSelector.getSize() + 1);
                                this.serverListSelector.scrollBy(this.serverListSelector.getSlotHeight());
                            }
                            else
                            {
                                this.selectServer(-1);
                            }
                        }
                    }
                    else
                    {
                        this.selectServer(-1);
                    }
                }
                else if (keyCode != 28 && keyCode != 156)
                {
                    super.keyTyped(typedChar, keyCode);
                }
                else
                {
                    this.actionPerformed(this.buttonList.get(2));
                }
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.hoveringText = null;
        this.drawDefaultBackground();
        this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.title", new Object[0]), this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.hoveringText != null)
        {
            this.drawHoveringText(Lists.newArrayList(Splitter.on("\n").split(this.hoveringText)), mouseX, mouseY);
        }
    }

    public void connectToSelected()
    {
        GuiListExtended.IGuiListEntry selectedEntry = this.serverListSelector.getSelectedSlotIndex() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.getSelectedSlotIndex());

        if (selectedEntry instanceof ServerListEntryNormal)
        {
            this.connectToServer(((ServerListEntryNormal)selectedEntry).getServerData());
        }
        else if (selectedEntry instanceof ServerListEntryLanDetected)
        {
            LanServerDetector.LanServer lanServer = ((ServerListEntryLanDetected)selectedEntry).getLanServer();
            this.connectToServer(new ServerData(lanServer.getServerMotd(), lanServer.getServerIpPort(), true));
        }
    }

    private void connectToServer(ServerData server)
    {
        this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, server));
    }

    public void selectServer(int index)
    {
        this.serverListSelector.setSelectedSlotIndex(index);
        GuiListExtended.IGuiListEntry selectedEntry = index < 0 ? null : this.serverListSelector.getListEntry(index);
        this.btnSelectServer.enabled = false;
        this.btnEditServer.enabled = false;
        this.btnDeleteServer.enabled = false;

        if (selectedEntry != null && !(selectedEntry instanceof ServerListEntryLanScan))
        {
            this.btnSelectServer.enabled = true;

            if (selectedEntry instanceof ServerListEntryNormal)
            {
                this.btnEditServer.enabled = true;
                this.btnDeleteServer.enabled = true;
            }
        }
    }

    public OldServerPinger getOldServerPinger()
    {
        return this.oldServerPinger;
    }

    public void setHoveringText(String hoveringTextIn)
    {
        this.hoveringText = hoveringTextIn;
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.serverListSelector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.serverListSelector.mouseReleased(mouseX, mouseY, state);
    }

    public ServerList getServerList()
    {
        return this.savedServerList;
    }

    public boolean canMoveUp(ServerListEntryNormal serverEntry, int index)
    {
        return index > 0;
    }

    public boolean canMoveDown(ServerListEntryNormal serverEntry, int index)
    {
        return index < this.savedServerList.countServers() - 1;
    }

    public void moveServerUp(ServerListEntryNormal serverEntry, int index, boolean moveToTop)
    {
        int targetIndex = moveToTop ? 0 : index - 1;
        this.savedServerList.swapServers(index, targetIndex);

        if (this.serverListSelector.getSelectedSlotIndex() == index)
        {
            this.selectServer(targetIndex);
        }

        this.serverListSelector.updateOnlineServers(this.savedServerList);
    }

    public void moveServerDown(ServerListEntryNormal serverEntry, int index, boolean moveToBottom)
    {
        int targetIndex = moveToBottom ? this.savedServerList.countServers() - 1 : index + 1;
        this.savedServerList.swapServers(index, targetIndex);

        if (this.serverListSelector.getSelectedSlotIndex() == index)
        {
            this.selectServer(targetIndex);
        }

        this.serverListSelector.updateOnlineServers(this.savedServerList);
    }
}
