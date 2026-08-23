package net.minecraft.client.gui.achievement;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.GameWindow;

public class GuiStats extends GuiScreen implements IProgressMeter
{
    protected GuiScreen parentScreen;
    protected String screenTitle = "Select world";
    private GuiStats.StatsGeneral generalStats;
    private GuiStats.StatsItem itemStats;
    private GuiStats.StatsBlock blockStats;
    private GuiStats.StatsMobsList mobStats;
    private StatFileWriter statFileWriter;
    private GuiSlot displaySlot;
    private boolean doesGuiPauseGame = true;

    public GuiStats(GuiScreen parentScreen, StatFileWriter statFileWriter)
    {
        this.parentScreen = parentScreen;
        this.statFileWriter = statFileWriter;
    }

    public void initGui()
    {
        this.screenTitle = I18n.format("gui.stats", new Object[0]);
        this.doesGuiPauseGame = true;
        this.mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        if (this.displaySlot != null)
        {
            this.displaySlot.handleMouseInput();
        }
    }

    public void createStatsLists()
    {
        this.generalStats = new GuiStats.StatsGeneral(this.mc);
        this.generalStats.registerScrollButtons(1, 1);
        this.itemStats = new GuiStats.StatsItem(this.mc);
        this.itemStats.registerScrollButtons(1, 1);
        this.blockStats = new GuiStats.StatsBlock(this.mc);
        this.blockStats.registerScrollButtons(1, 1);
        this.mobStats = new GuiStats.StatsMobsList(this.mc);
        this.mobStats.registerScrollButtons(1, 1);
    }

    public void createButtons()
    {
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 160, this.height - 52, 80, 20, I18n.format("stat.generalButton", new Object[0])));
        GuiButton blocksButton;
        this.buttonList.add(blocksButton = new GuiButton(2, this.width / 2 - 80, this.height - 52, 80, 20, I18n.format("stat.blocksButton", new Object[0])));
        GuiButton itemsButton;
        this.buttonList.add(itemsButton = new GuiButton(3, this.width / 2, this.height - 52, 80, 20, I18n.format("stat.itemsButton", new Object[0])));
        GuiButton mobsButton;
        this.buttonList.add(mobsButton = new GuiButton(4, this.width / 2 + 80, this.height - 52, 80, 20, I18n.format("stat.mobsButton", new Object[0])));

        if (this.blockStats.getSize() == 0)
        {
            blocksButton.enabled = false;
        }

        if (this.itemStats.getSize() == 0)
        {
            itemsButton.enabled = false;
        }

        if (this.mobStats.getSize() == 0)
        {
            mobsButton.enabled = false;
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 1)
            {
                this.displaySlot = this.generalStats;
            }
            else if (button.id == 3)
            {
                this.displaySlot = this.itemStats;
            }
            else if (button.id == 2)
            {
                this.displaySlot = this.blockStats;
            }
            else if (button.id == 4)
            {
                this.displaySlot = this.mobStats;
            }
            else
            {
                this.displaySlot.actionPerformed(button);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (this.doesGuiPauseGame)
        {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingStats", new Object[0]), this.width / 2, this.height / 2, 16777215);
            this.drawCenteredString(this.fontRendererObj, lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % (long)lanSearchStates.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 16777215);
        }
        else
        {
            this.displaySlot.drawScreen(mouseX, mouseY, partialTicks);
            this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 20, 16777215);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void doneLoading()
    {
        if (this.doesGuiPauseGame)
        {
            this.createStatsLists();
            this.createButtons();
            this.displaySlot = this.generalStats;
            this.doesGuiPauseGame = false;
        }
    }

    public boolean doesGuiPauseGame()
    {
        return !this.doesGuiPauseGame;
    }

    private void drawStatsScreen(int x, int y, Item item)
    {
        this.drawButtonBackground(x + 1, y + 1);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemIntoGUI(new ItemStack(item, 1, 0), x + 2, y + 2);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void drawButtonBackground(int x, int y)
    {
        this.drawSprite(x, y, 0, 0);
    }

    private void drawSprite(int x, int y, int u, int v)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(statIcons);
        float textureScaleU = 0.0078125F;
        float textureScaleV = 0.0078125F;
        int spriteWidth = 18;
        int spriteHeight = 18;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos((double)x, (double)(y + spriteHeight), (double)this.zLevel).tex((double)((float)u * textureScaleU), (double)((float)(v + spriteHeight) * textureScaleV)).endVertex();
        worldRenderer.pos((double)(x + spriteWidth), (double)(y + spriteHeight), (double)this.zLevel).tex((double)((float)(u + spriteWidth) * textureScaleU), (double)((float)(v + spriteHeight) * textureScaleV)).endVertex();
        worldRenderer.pos((double)(x + spriteWidth), (double)y, (double)this.zLevel).tex((double)((float)(u + spriteWidth) * textureScaleU), (double)((float)v * textureScaleV)).endVertex();
        worldRenderer.pos((double)x, (double)y, (double)this.zLevel).tex((double)((float)u * textureScaleU), (double)((float)v * textureScaleV)).endVertex();
        tessellator.draw();
    }

    abstract class Stats extends GuiSlot
    {
        protected int hoveredColumnIndex = -1;
        protected List<StatCrafting> statsHolder;
        protected Comparator<StatCrafting> statSorter;
        protected int sortedColumnIndex = -1;
        protected int sortDirection;

        protected Stats(Minecraft mcIn)
        {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 20);
            this.setShowSelectionBox(false);
            this.setHasListHeader(true, 20);
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
            GuiStats.this.drawDefaultBackground();
        }

        protected void drawListHeader(int x, int y, Tessellator tessellator)
        {
            if (!GameWindow.isButtonDown(0))
            {
                this.hoveredColumnIndex = -1;
            }

            if (this.hoveredColumnIndex == 0)
            {
                GuiStats.this.drawSprite(x + 115 - 18, y + 1, 0, 0);
            }
            else
            {
                GuiStats.this.drawSprite(x + 115 - 18, y + 1, 0, 18);
            }

            if (this.hoveredColumnIndex == 1)
            {
                GuiStats.this.drawSprite(x + 165 - 18, y + 1, 0, 0);
            }
            else
            {
                GuiStats.this.drawSprite(x + 165 - 18, y + 1, 0, 18);
            }

            if (this.hoveredColumnIndex == 2)
            {
                GuiStats.this.drawSprite(x + 215 - 18, y + 1, 0, 0);
            }
            else
            {
                GuiStats.this.drawSprite(x + 215 - 18, y + 1, 0, 18);
            }

            if (this.sortedColumnIndex != -1)
            {
                int sortArrowX = 79;
                int sortArrowU = 18;

                if (this.sortedColumnIndex == 1)
                {
                    sortArrowX = 129;
                }
                else if (this.sortedColumnIndex == 2)
                {
                    sortArrowX = 179;
                }

                if (this.sortDirection == 1)
                {
                    sortArrowU = 36;
                }

                GuiStats.this.drawSprite(x + sortArrowX, y + 1, sortArrowU, 0);
            }
        }

        protected void handleHeaderClick(int mouseX, int mouseY)
        {
            this.hoveredColumnIndex = -1;

            if (mouseX >= 79 && mouseX < 115)
            {
                this.hoveredColumnIndex = 0;
            }
            else if (mouseX >= 129 && mouseX < 165)
            {
                this.hoveredColumnIndex = 1;
            }
            else if (mouseX >= 179 && mouseX < 215)
            {
                this.hoveredColumnIndex = 2;
            }

            if (this.hoveredColumnIndex >= 0)
            {
                this.setSortColumn(this.hoveredColumnIndex);
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            }
        }

        protected final int getSize()
        {
            return this.statsHolder.size();
        }

        protected final StatCrafting getStatCrafting(int index)
        {
            return (StatCrafting)this.statsHolder.get(index);
        }

        protected abstract String getColumnHeaderText(int column);

        protected void drawStat(StatBase stat, int x, int y, boolean highlight)
        {
            if (stat != null)
            {
                String statText = stat.format(GuiStats.this.statFileWriter.readStat(stat));
                GuiStats.this.drawString(GuiStats.this.fontRendererObj, statText, x - GuiStats.this.fontRendererObj.getStringWidth(statText), y + 5, highlight ? 16777215 : 9474192);
            }
            else
            {
                String fallbackText = "-";
                GuiStats.this.drawString(GuiStats.this.fontRendererObj, fallbackText, x - GuiStats.this.fontRendererObj.getStringWidth(fallbackText), y + 5, highlight ? 16777215 : 9474192);
            }
        }

        protected void drawScreenPostRender(int mouseX, int mouseY)
        {
            if (mouseY >= this.top && mouseY <= this.bottom)
            {
                int hoveredSlotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
                int listLeft = this.width / 2 - 92 - 16;

                if (hoveredSlotIndex >= 0)
                {
                    if (mouseX < listLeft + 40 || mouseX > listLeft + 40 + 20)
                    {
                        return;
                    }

                    StatCrafting hoveredStat = this.getStatCrafting(hoveredSlotIndex);
                    this.drawItemTooltip(hoveredStat, mouseX, mouseY);
                }
                else
                {
                    String tooltipText = "";

                    if (mouseX >= listLeft + 115 - 18 && mouseX <= listLeft + 115)
                    {
                        tooltipText = this.getColumnHeaderText(0);
                    }
                    else if (mouseX >= listLeft + 165 - 18 && mouseX <= listLeft + 165)
                    {
                        tooltipText = this.getColumnHeaderText(1);
                    }
                    else
                    {
                        if (mouseX < listLeft + 215 - 18 || mouseX > listLeft + 215)
                        {
                            return;
                        }

                        tooltipText = this.getColumnHeaderText(2);
                    }

                    tooltipText = ("" + I18n.format(tooltipText, new Object[0])).trim();

                    if (tooltipText.length() > 0)
                    {
                        int tooltipX = mouseX + 12;
                        int tooltipY = mouseY - 12;
                        int tooltipWidth = GuiStats.this.fontRendererObj.getStringWidth(tooltipText);
                        GuiStats.this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + 8 + 3, -1073741824, -1073741824);
                        GuiStats.this.fontRendererObj.drawStringWithShadow(tooltipText, (float)tooltipX, (float)tooltipY, -1);
                    }
                }
            }
        }

        protected void drawItemTooltip(StatCrafting stat, int mouseX, int mouseY)
        {
            if (stat != null)
            {
                Item item = stat.getItem();
                ItemStack itemStack = new ItemStack(item);
                String translationKey = itemStack.getUnlocalizedName();
                String itemName = ("" + I18n.format(translationKey + ".name", new Object[0])).trim();

                if (itemName.length() > 0)
                {
                    int tooltipX = mouseX + 12;
                    int tooltipY = mouseY - 12;
                    int tooltipWidth = GuiStats.this.fontRendererObj.getStringWidth(itemName);
                    GuiStats.this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + 8 + 3, -1073741824, -1073741824);
                    GuiStats.this.fontRendererObj.drawStringWithShadow(itemName, (float)tooltipX, (float)tooltipY, -1);
                }
            }
        }

        protected void setSortColumn(int column)
        {
            if (column != this.sortedColumnIndex)
            {
                this.sortedColumnIndex = column;
                this.sortDirection = -1;
            }
            else if (this.sortDirection == -1)
            {
                this.sortDirection = 1;
            }
            else
            {
                this.sortedColumnIndex = -1;
                this.sortDirection = 0;
            }

            Collections.sort(this.statsHolder, this.statSorter);
        }
    }

    class StatsBlock extends GuiStats.Stats
    {
        public StatsBlock(Minecraft mcIn)
        {
            super(mcIn);
            this.statsHolder = Lists.<StatCrafting>newArrayList();

            for (StatCrafting statCrafting : StatList.objectMineStats)
            {
                boolean hasRelevantStat = false;
                int itemId = Item.getIdFromItem(statCrafting.getItem());

                if (GuiStats.this.statFileWriter.readStat(statCrafting) > 0)
                {
                    hasRelevantStat = true;
                }
                else if (StatList.objectUseStats[itemId] != null && GuiStats.this.statFileWriter.readStat(StatList.objectUseStats[itemId]) > 0)
                {
                    hasRelevantStat = true;
                }
                else if (StatList.objectCraftStats[itemId] != null && GuiStats.this.statFileWriter.readStat(StatList.objectCraftStats[itemId]) > 0)
                {
                    hasRelevantStat = true;
                }

                if (hasRelevantStat)
                {
                    this.statsHolder.add(statCrafting);
                }
            }

            this.statSorter = new Comparator<StatCrafting>()
            {
                public int compare(StatCrafting first, StatCrafting second)
                {
                    int firstItemId = Item.getIdFromItem(first.getItem());
                    int secondItemId = Item.getIdFromItem(second.getItem());
                    StatBase firstStat = null;
                    StatBase secondStat = null;

                    if (StatsBlock.this.sortedColumnIndex == 2)
                    {
                        firstStat = StatList.mineBlockStatArray[firstItemId];
                        secondStat = StatList.mineBlockStatArray[secondItemId];
                    }
                    else if (StatsBlock.this.sortedColumnIndex == 0)
                    {
                        firstStat = StatList.objectCraftStats[firstItemId];
                        secondStat = StatList.objectCraftStats[secondItemId];
                    }
                    else if (StatsBlock.this.sortedColumnIndex == 1)
                    {
                        firstStat = StatList.objectUseStats[firstItemId];
                        secondStat = StatList.objectUseStats[secondItemId];
                    }

                    if (firstStat != null || secondStat != null)
                    {
                        if (firstStat == null)
                        {
                            return 1;
                        }

                        if (secondStat == null)
                        {
                            return -1;
                        }

                        int firstValue = GuiStats.this.statFileWriter.readStat(firstStat);
                        int secondValue = GuiStats.this.statFileWriter.readStat(secondStat);

                        if (firstValue != secondValue)
                        {
                            return (firstValue - secondValue) * StatsBlock.this.sortDirection;
                        }
                    }

                    return firstItemId - secondItemId;
                }
            };
        }

        protected void drawListHeader(int x, int y, Tessellator tessellator)
        {
            super.drawListHeader(x, y, tessellator);

            if (this.hoveredColumnIndex == 0)
            {
                GuiStats.this.drawSprite(x + 115 - 18 + 1, y + 1 + 1, 18, 18);
            }
            else
            {
                GuiStats.this.drawSprite(x + 115 - 18, y + 1, 18, 18);
            }

            if (this.hoveredColumnIndex == 1)
            {
                GuiStats.this.drawSprite(x + 165 - 18 + 1, y + 1 + 1, 36, 18);
            }
            else
            {
                GuiStats.this.drawSprite(x + 165 - 18, y + 1, 36, 18);
            }

            if (this.hoveredColumnIndex == 2)
            {
                GuiStats.this.drawSprite(x + 215 - 18 + 1, y + 1 + 1, 54, 18);
            }
            else
            {
                GuiStats.this.drawSprite(x + 215 - 18, y + 1, 54, 18);
            }
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            StatCrafting statCrafting = this.getStatCrafting(entryID);
            Item item = statCrafting.getItem();
            GuiStats.this.drawStatsScreen(x + 40, y, item);
            int itemId = Item.getIdFromItem(item);
            this.drawStat(StatList.objectCraftStats[itemId], x + 115, y, entryID % 2 == 0);
            this.drawStat(StatList.objectUseStats[itemId], x + 165, y, entryID % 2 == 0);
            this.drawStat(statCrafting, x + 215, y, entryID % 2 == 0);
        }

        protected String getColumnHeaderText(int column)
        {
            return column == 0 ? "stat.crafted" : (column == 1 ? "stat.used" : "stat.mined");
        }
    }

    class StatsGeneral extends GuiSlot
    {
        public StatsGeneral(Minecraft mcIn)
        {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 10);
            this.setShowSelectionBox(false);
        }

        protected int getSize()
        {
            return StatList.generalStats.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected int getContentHeight()
        {
            return this.getSize() * 10;
        }

        protected void drawBackground()
        {
            GuiStats.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            StatBase statBase = (StatBase)StatList.generalStats.get(entryID);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, statBase.getStatName().getUnformattedText(), x + 2, y + 1, entryID % 2 == 0 ? 16777215 : 9474192);
            String statText = statBase.format(GuiStats.this.statFileWriter.readStat(statBase));
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, statText, x + 2 + 213 - GuiStats.this.fontRendererObj.getStringWidth(statText), y + 1, entryID % 2 == 0 ? 16777215 : 9474192);
        }
    }

    class StatsItem extends GuiStats.Stats
    {
        public StatsItem(Minecraft mcIn)
        {
            super(mcIn);
            this.statsHolder = Lists.<StatCrafting>newArrayList();

            for (StatCrafting statCrafting : StatList.itemStats)
            {
                boolean hasRelevantStat = false;
                int itemId = Item.getIdFromItem(statCrafting.getItem());

                if (GuiStats.this.statFileWriter.readStat(statCrafting) > 0)
                {
                    hasRelevantStat = true;
                }
                else if (StatList.objectBreakStats[itemId] != null && GuiStats.this.statFileWriter.readStat(StatList.objectBreakStats[itemId]) > 0)
                {
                    hasRelevantStat = true;
                }
                else if (StatList.objectCraftStats[itemId] != null && GuiStats.this.statFileWriter.readStat(StatList.objectCraftStats[itemId]) > 0)
                {
                    hasRelevantStat = true;
                }

                if (hasRelevantStat)
                {
                    this.statsHolder.add(statCrafting);
                }
            }

            this.statSorter = new Comparator<StatCrafting>()
            {
                public int compare(StatCrafting first, StatCrafting second)
                {
                    int firstItemId = Item.getIdFromItem(first.getItem());
                    int secondItemId = Item.getIdFromItem(second.getItem());
                    StatBase firstStat = null;
                    StatBase secondStat = null;

                    if (StatsItem.this.sortedColumnIndex == 0)
                    {
                        firstStat = StatList.objectBreakStats[firstItemId];
                        secondStat = StatList.objectBreakStats[secondItemId];
                    }
                    else if (StatsItem.this.sortedColumnIndex == 1)
                    {
                        firstStat = StatList.objectCraftStats[firstItemId];
                        secondStat = StatList.objectCraftStats[secondItemId];
                    }
                    else if (StatsItem.this.sortedColumnIndex == 2)
                    {
                        firstStat = StatList.objectUseStats[firstItemId];
                        secondStat = StatList.objectUseStats[secondItemId];
                    }

                    if (firstStat != null || secondStat != null)
                    {
                        if (firstStat == null)
                        {
                            return 1;
                        }

                        if (secondStat == null)
                        {
                            return -1;
                        }

                        int firstValue = GuiStats.this.statFileWriter.readStat(firstStat);
                        int secondValue = GuiStats.this.statFileWriter.readStat(secondStat);

                        if (firstValue != secondValue)
                        {
                            return (firstValue - secondValue) * StatsItem.this.sortDirection;
                        }
                    }

                    return firstItemId - secondItemId;
                }
            };
        }

        protected void drawListHeader(int x, int y, Tessellator tessellator)
        {
            super.drawListHeader(x, y, tessellator);

            if (this.hoveredColumnIndex == 0)
            {
                GuiStats.this.drawSprite(x + 115 - 18 + 1, y + 1 + 1, 72, 18);
            }
            else
            {
                GuiStats.this.drawSprite(x + 115 - 18, y + 1, 72, 18);
            }

            if (this.hoveredColumnIndex == 1)
            {
                GuiStats.this.drawSprite(x + 165 - 18 + 1, y + 1 + 1, 18, 18);
            }
            else
            {
                GuiStats.this.drawSprite(x + 165 - 18, y + 1, 18, 18);
            }

            if (this.hoveredColumnIndex == 2)
            {
                GuiStats.this.drawSprite(x + 215 - 18 + 1, y + 1 + 1, 36, 18);
            }
            else
            {
                GuiStats.this.drawSprite(x + 215 - 18, y + 1, 36, 18);
            }
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            StatCrafting statCrafting = this.getStatCrafting(entryID);
            Item item = statCrafting.getItem();
            GuiStats.this.drawStatsScreen(x + 40, y, item);
            int itemId = Item.getIdFromItem(item);
            this.drawStat(StatList.objectBreakStats[itemId], x + 115, y, entryID % 2 == 0);
            this.drawStat(StatList.objectCraftStats[itemId], x + 165, y, entryID % 2 == 0);
            this.drawStat(statCrafting, x + 215, y, entryID % 2 == 0);
        }

        protected String getColumnHeaderText(int column)
        {
            return column == 1 ? "stat.crafted" : (column == 2 ? "stat.used" : "stat.depleted");
        }
    }

    class StatsMobsList extends GuiSlot
    {
        private final List<EntityList.EntityEggInfo> mobEggInfoList = Lists.<EntityList.EntityEggInfo>newArrayList();

        public StatsMobsList(Minecraft mcIn)
        {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, GuiStats.this.fontRendererObj.FONT_HEIGHT * 4);
            this.setShowSelectionBox(false);

            for (EntityList.EntityEggInfo mobInfo : EntityList.entityEggs.values())
            {
                if (GuiStats.this.statFileWriter.readStat(mobInfo.killEntityStat) > 0 || GuiStats.this.statFileWriter.readStat(mobInfo.killedByEntityStat) > 0)
                {
                    this.mobEggInfoList.add(mobInfo);
                }
            }
        }

        protected int getSize()
        {
            return this.mobEggInfoList.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
        }

        protected boolean isSelected(int slotIndex)
        {
            return false;
        }

        protected int getContentHeight()
        {
            return this.getSize() * GuiStats.this.fontRendererObj.FONT_HEIGHT * 4;
        }

        protected void drawBackground()
        {
            GuiStats.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            EntityList.EntityEggInfo mobInfo = (EntityList.EntityEggInfo)this.mobEggInfoList.get(entryID);
            String mobName = I18n.format("entity." + EntityList.getStringFromID(mobInfo.spawnedID) + ".name", new Object[0]);
            int killCount = GuiStats.this.statFileWriter.readStat(mobInfo.killEntityStat);
            int killedByCount = GuiStats.this.statFileWriter.readStat(mobInfo.killedByEntityStat);
            String killText = I18n.format("stat.entityKills", new Object[] {Integer.valueOf(killCount), mobName});
            String killedByText = I18n.format("stat.entityKilledBy", new Object[] {mobName, Integer.valueOf(killedByCount)});

            if (killCount == 0)
            {
                killText = I18n.format("stat.entityKills.none", new Object[] {mobName});
            }

            if (killedByCount == 0)
            {
                killedByText = I18n.format("stat.entityKilledBy.none", new Object[] {mobName});
            }

            GuiStats.this.drawString(GuiStats.this.fontRendererObj, mobName, x + 2 - 10, y + 1, 16777215);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, killText, x + 2, y + 1 + GuiStats.this.fontRendererObj.FONT_HEIGHT, killCount == 0 ? 6316128 : 9474192);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, killedByText, x + 2, y + 1 + GuiStats.this.fontRendererObj.FONT_HEIGHT * 2, killedByCount == 0 ? 6316128 : 9474192);
        }
    }
}
