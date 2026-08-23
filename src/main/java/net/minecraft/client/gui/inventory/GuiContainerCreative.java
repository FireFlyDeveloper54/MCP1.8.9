package net.minecraft.client.gui.inventory;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.GameWindow;

public class GuiContainerCreative extends InventoryEffectRenderer
{
    private static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static InventoryBasic creativeInventory = new InventoryBasic("tmp", true, 45);
    private static int selectedTabIndex = CreativeTabs.tabBlock.getTabIndex();
    private float currentScroll;
    private boolean isScrolling;
    private boolean wasClicking;
    private GuiTextField searchField;
    private List<Slot> originalSlots;
    private Slot destroyItemSlot;
    private boolean clearSearch;
    private CreativeCrafting listener;

    public GuiContainerCreative(EntityPlayer player)
    {
        super(new GuiContainerCreative.ContainerCreative(player));
        player.openContainer = this.inventorySlots;
        this.allowUserInput = true;
        this.ySize = 136;
        this.xSize = 195;
    }

    public void updateScreen()
    {
        if (!this.mc.playerController.isInCreativeMode())
        {
            this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
        }

        this.updateActivePotionEffects();
    }

    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType)
    {
        this.clearSearch = true;
        boolean flag = clickType == 1;
        clickType = slotId == -999 && clickType == 0 ? 4 : clickType;

        if (slotIn == null && selectedTabIndex != CreativeTabs.tabInventory.getTabIndex() && clickType != 5)
        {
            InventoryPlayer inventoryplayer1 = this.mc.thePlayer.inventory;

            if (inventoryplayer1.getItemStack() != null)
            {
                if (clickedButton == 0)
                {
                    this.mc.thePlayer.dropPlayerItemWithRandomChoice(inventoryplayer1.getItemStack(), true);
                    this.mc.playerController.sendPacketDropItem(inventoryplayer1.getItemStack());
                    inventoryplayer1.setItemStack((ItemStack)null);
                }

                if (clickedButton == 1)
                {
                    ItemStack itemstack5 = inventoryplayer1.getItemStack().splitStack(1);
                    this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack5, true);
                    this.mc.playerController.sendPacketDropItem(itemstack5);

                    if (inventoryplayer1.getItemStack().stackSize == 0)
                    {
                        inventoryplayer1.setItemStack((ItemStack)null);
                    }
                }
            }
        }
        else if (slotIn == this.destroyItemSlot && flag)
        {
            for (int j = 0; j < this.mc.thePlayer.inventoryContainer.getInventory().size(); ++j)
            {
                this.mc.playerController.sendSlotPacket((ItemStack)null, j);
            }
        }
        else if (selectedTabIndex == CreativeTabs.tabInventory.getTabIndex())
        {
            if (slotIn == this.destroyItemSlot)
            {
                this.mc.thePlayer.inventory.setItemStack((ItemStack)null);
            }
            else if (clickType == 4 && slotIn != null && slotIn.getHasStack())
            {
                ItemStack itemstack = slotIn.decrStackSize(clickedButton == 0 ? 1 : slotIn.getStack().getMaxStackSize());
                this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack, true);
                this.mc.playerController.sendPacketDropItem(itemstack);
            }
            else if (clickType == 4 && this.mc.thePlayer.inventory.getItemStack() != null)
            {
                this.mc.thePlayer.dropPlayerItemWithRandomChoice(this.mc.thePlayer.inventory.getItemStack(), true);
                this.mc.playerController.sendPacketDropItem(this.mc.thePlayer.inventory.getItemStack());
                this.mc.thePlayer.inventory.setItemStack((ItemStack)null);
            }
            else
            {
                this.mc.thePlayer.inventoryContainer.slotClick(slotIn == null ? slotId : ((GuiContainerCreative.CreativeSlot)slotIn).slot.slotNumber, clickedButton, clickType, this.mc.thePlayer);
                this.mc.thePlayer.inventoryContainer.detectAndSendChanges();
            }
        }
        else if (clickType != 5 && slotIn.inventory == creativeInventory)
        {
            InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
            ItemStack itemstack1 = inventoryplayer.getItemStack();
            ItemStack itemstack2 = slotIn.getStack();

            if (clickType == 2)
            {
                if (itemstack2 != null && clickedButton >= 0 && clickedButton < 9)
                {
                    ItemStack itemstack7 = itemstack2.copy();
                    itemstack7.stackSize = itemstack7.getMaxStackSize();
                    this.mc.thePlayer.inventory.setInventorySlotContents(clickedButton, itemstack7);
                    this.mc.thePlayer.inventoryContainer.detectAndSendChanges();
                }

                return;
            }

            if (clickType == 3)
            {
                if (inventoryplayer.getItemStack() == null && slotIn.getHasStack())
                {
                    ItemStack itemStack6 = slotIn.getStack().copy();
                    itemStack6.stackSize = itemStack6.getMaxStackSize();
                    inventoryplayer.setItemStack(itemStack6);
                }

                return;
            }

            if (clickType == 4)
            {
                if (itemstack2 != null)
                {
                    ItemStack itemstack3 = itemstack2.copy();
                    itemstack3.stackSize = clickedButton == 0 ? 1 : itemstack3.getMaxStackSize();
                    this.mc.thePlayer.dropPlayerItemWithRandomChoice(itemstack3, true);
                    this.mc.playerController.sendPacketDropItem(itemstack3);
                }

                return;
            }

            if (itemstack1 != null && itemstack2 != null && itemstack1.isItemEqual(itemstack2))
            {
                if (clickedButton == 0)
                {
                    if (flag)
                    {
                        itemstack1.stackSize = itemstack1.getMaxStackSize();
                    }
                    else if (itemstack1.stackSize < itemstack1.getMaxStackSize())
                    {
                        ++itemstack1.stackSize;
                    }
                }
                else if (itemstack1.stackSize <= 1)
                {
                    inventoryplayer.setItemStack((ItemStack)null);
                }
                else
                {
                    --itemstack1.stackSize;
                }
            }
            else if (itemstack2 != null && itemstack1 == null)
            {
                inventoryplayer.setItemStack(ItemStack.copyItemStack(itemstack2));
                itemstack1 = inventoryplayer.getItemStack();

                if (flag)
                {
                    itemstack1.stackSize = itemstack1.getMaxStackSize();
                }
            }
            else
            {
                inventoryplayer.setItemStack((ItemStack)null);
            }
        }
        else
        {
            this.inventorySlots.slotClick(slotIn == null ? slotId : slotIn.slotNumber, clickedButton, clickType, this.mc.thePlayer);

            if (Container.getDragEvent(clickedButton) == 2)
            {
                for (int i = 0; i < 9; ++i)
                {
                    this.mc.playerController.sendSlotPacket(this.inventorySlots.getSlot(45 + i).getStack(), 36 + i);
                }
            }
            else if (slotIn != null)
            {
                ItemStack itemstack4 = this.inventorySlots.getSlot(slotIn.slotNumber).getStack();
                this.mc.playerController.sendSlotPacket(itemstack4, slotIn.slotNumber - this.inventorySlots.inventorySlots.size() + 9 + 36);
            }
        }
    }

    protected void updateActivePotionEffects()
    {
        int i = this.guiLeft;
        super.updateActivePotionEffects();

        if (this.searchField != null && this.guiLeft != i)
        {
            this.searchField.xPosition = this.guiLeft + 82;
        }
    }

    public void initGui()
    {
        if (this.mc.playerController.isInCreativeMode())
        {
            super.initGui();
            this.buttonList.clear();
            GameWindow.setRepeatEvents(true);
            this.searchField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 82, this.guiTop + 6, 89, this.fontRendererObj.FONT_HEIGHT);
            this.searchField.setMaxStringLength(15);
            this.searchField.setEnableBackgroundDrawing(false);
            this.searchField.setVisible(false);
            this.searchField.setTextColor(16777215);
            int i = selectedTabIndex;
            selectedTabIndex = -1;
            this.setCurrentCreativeTab(CreativeTabs.creativeTabArray[i]);
            this.listener = new CreativeCrafting(this.mc);
            this.mc.thePlayer.inventoryContainer.onCraftGuiOpened(this.listener);
        }
        else
        {
            this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
        }
    }

    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (this.mc.thePlayer != null && this.mc.thePlayer.inventory != null)
        {
            this.mc.thePlayer.inventoryContainer.removeCraftingFromCrafters(this.listener);
        }

        GameWindow.setRepeatEvents(false);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (selectedTabIndex != CreativeTabs.tabAllSearch.getTabIndex())
        {
            if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindChat))
            {
                this.setCurrentCreativeTab(CreativeTabs.tabAllSearch);
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
        else
        {
            if (this.clearSearch)
            {
                this.clearSearch = false;
                this.searchField.setText("");
            }

            if (!this.checkHotbarKeys(keyCode))
            {
                if (this.searchField.textboxKeyTyped(typedChar, keyCode))
                {
                    this.updateCreativeSearch();
                }
                else
                {
                    super.keyTyped(typedChar, keyCode);
                }
            }
        }
    }

    private void updateCreativeSearch()
    {
        GuiContainerCreative.ContainerCreative containerCreative = (GuiContainerCreative.ContainerCreative)this.inventorySlots;
        containerCreative.itemList.clear();

        for (Item item : Item.itemRegistry)
        {
            if (item != null && item.getCreativeTab() != null)
            {
                item.getSubItems(item, (CreativeTabs)null, containerCreative.itemList);
            }
        }

        for (Enchantment enchantment : Enchantment.enchantmentsBookList)
        {
            if (enchantment != null && enchantment.type != null)
            {
                Items.enchanted_book.getAll(enchantment, containerCreative.itemList);
            }
        }

        Iterator<ItemStack> iterator = containerCreative.itemList.iterator();
        String text = this.searchField.getText().toLowerCase(Locale.ROOT);

        while (iterator.hasNext())
        {
            ItemStack itemStack = (ItemStack)iterator.next();
            boolean flag = false;

            for (String s : itemStack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips))
            {
                if (EnumChatFormatting.getTextWithoutFormattingCodes(s).toLowerCase(Locale.ROOT).contains(text))
                {
                    flag = true;
                    break;
                }
            }

            if (!flag)
            {
                iterator.remove();
            }
        }

        this.currentScroll = 0.0F;
        containerCreative.scrollTo(0.0F);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        CreativeTabs creativetabs = CreativeTabs.creativeTabArray[selectedTabIndex];

        if (creativetabs.drawInForegroundOfTab())
        {
            GlStateManager.disableBlend();
            this.fontRendererObj.drawString(I18n.format(creativetabs.getTranslatedTabLabel(), new Object[0]), 8, 6, 4210752);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            int i = mouseX - this.guiLeft;
            int j = mouseY - this.guiTop;

            for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray)
            {
                if (this.isMouseOverTab(creativetabs, i, j))
                {
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state == 0)
        {
            int i = mouseX - this.guiLeft;
            int j = mouseY - this.guiTop;

            for (CreativeTabs creativeTabs : CreativeTabs.creativeTabArray)
            {
                if (this.isMouseOverTab(creativeTabs, i, j))
                {
                    this.setCurrentCreativeTab(creativeTabs);
                    return;
                }
            }
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    private boolean needsScrollBars()
    {
        return selectedTabIndex != CreativeTabs.tabInventory.getTabIndex() && CreativeTabs.creativeTabArray[selectedTabIndex].shouldHidePlayerInventory() && ((GuiContainerCreative.ContainerCreative)this.inventorySlots).canScroll();
    }

    private void setCurrentCreativeTab(CreativeTabs tab)
    {
        int i = selectedTabIndex;
        selectedTabIndex = tab.getTabIndex();
        GuiContainerCreative.ContainerCreative containercreative = (GuiContainerCreative.ContainerCreative)this.inventorySlots;
        this.dragSplittingSlots.clear();
        containercreative.itemList.clear();
        tab.displayAllReleventItems(containercreative.itemList);

        if (tab == CreativeTabs.tabInventory)
        {
            Container container = this.mc.thePlayer.inventoryContainer;

            if (this.originalSlots == null)
            {
                this.originalSlots = containercreative.inventorySlots;
            }

            containercreative.inventorySlots = Lists.<Slot>newArrayList();

            for (int j = 0; j < container.inventorySlots.size(); ++j)
            {
                Slot slot = new GuiContainerCreative.CreativeSlot(container.inventorySlots.get(j), j);
                containercreative.inventorySlots.add(slot);

                if (j >= 5 && j < 9)
                {
                    int intValue = j - 5;
                    int secondIntValue = intValue / 2;
                    int thirdIntValue = intValue % 2;
                    slot.xDisplayPosition = 9 + secondIntValue * 54;
                    slot.yDisplayPosition = 6 + thirdIntValue * 27;
                }
                else if (j >= 0 && j < 5)
                {
                    slot.yDisplayPosition = -2000;
                    slot.xDisplayPosition = -2000;
                }
                else if (j < container.inventorySlots.size())
                {
                    int k = j - 9;
                    int l = k % 9;
                    int fourthIntValue = k / 9;
                    slot.xDisplayPosition = 9 + l * 18;

                    if (j >= 36)
                    {
                        slot.yDisplayPosition = 112;
                    }
                    else
                    {
                        slot.yDisplayPosition = 54 + fourthIntValue * 18;
                    }
                }
            }

            this.destroyItemSlot = new Slot(creativeInventory, 0, 173, 112);
            containercreative.inventorySlots.add(this.destroyItemSlot);
        }
        else if (i == CreativeTabs.tabInventory.getTabIndex())
        {
            containercreative.inventorySlots = this.originalSlots;
            this.originalSlots = null;
        }

        if (this.searchField != null)
        {
            if (tab == CreativeTabs.tabAllSearch)
            {
                this.searchField.setVisible(true);
                this.searchField.setCanLoseFocus(false);
                this.searchField.setFocused(true);
                this.searchField.setText("");
                this.updateCreativeSearch();
            }
            else
            {
                this.searchField.setVisible(false);
                this.searchField.setCanLoseFocus(true);
                this.searchField.setFocused(false);
            }
        }

        this.currentScroll = 0.0F;
        containercreative.scrollTo(0.0F);
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = GameWindow.getEventDWheel();

        if (i != 0 && this.needsScrollBars())
        {
            int j = ((GuiContainerCreative.ContainerCreative)this.inventorySlots).itemList.size() / 9 - 5;

            if (i > 0)
            {
                i = 1;
            }

            if (i < 0)
            {
                i = -1;
            }

            this.currentScroll = (float)((double)this.currentScroll - (double)i / (double)j);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative)this.inventorySlots).scrollTo(this.currentScroll);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        boolean flag = GameWindow.isButtonDown(0);
        int i = this.guiLeft;
        int j = this.guiTop;
        int k = i + 175;
        int l = j + 18;
        int fifthIntValue = k + 14;
        int eighthIntValue = l + 112;

        if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < fifthIntValue && mouseY < eighthIntValue)
        {
            this.isScrolling = this.needsScrollBars();
        }

        if (!flag)
        {
            this.isScrolling = false;
        }

        this.wasClicking = flag;

        if (this.isScrolling)
        {
            this.currentScroll = ((float)(mouseY - l) - 7.5F) / ((float)(eighthIntValue - l) - 15.0F);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative)this.inventorySlots).scrollTo(this.currentScroll);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        for (CreativeTabs creativetabs : CreativeTabs.creativeTabArray)
        {
            if (this.renderCreativeInventoryHoveringText(creativetabs, mouseX, mouseY))
            {
                break;
            }
        }

        if (this.destroyItemSlot != null && selectedTabIndex == CreativeTabs.tabInventory.getTabIndex() && this.isPointInRegion(this.destroyItemSlot.xDisplayPosition, this.destroyItemSlot.yDisplayPosition, 16, 16, mouseX, mouseY))
        {
            this.drawCreativeTabHoveringText(I18n.format("inventory.binSlot", new Object[0]), mouseX, mouseY);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
    }

    protected void renderToolTip(ItemStack stack, int x, int y)
    {
        if (selectedTabIndex == CreativeTabs.tabAllSearch.getTabIndex())
        {
            List<String> list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
            CreativeTabs creativetabs = stack.getItem().getCreativeTab();

            if (creativetabs == null && stack.getItem() == Items.enchanted_book)
            {
                Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(stack);

                if (map.size() == 1)
                {
                    Enchantment enchantment = Enchantment.getEnchantmentById(((Integer)map.keySet().iterator().next()).intValue());

                    for (CreativeTabs creativetabs1 : CreativeTabs.creativeTabArray)
                    {
                        if (creativetabs1.hasRelevantEnchantmentType(enchantment.type))
                        {
                            creativetabs = creativetabs1;
                            break;
                        }
                    }
                }
            }

            if (creativetabs != null)
            {
                list.add(1, "" + EnumChatFormatting.BOLD + EnumChatFormatting.BLUE + I18n.format(creativetabs.getTranslatedTabLabel(), new Object[0]));
            }

            for (int i = 0; i < list.size(); ++i)
            {
                if (i == 0)
                {
                    list.set(i, stack.getRarity().rarityColor + list.get(i));
                }
                else
                {
                    list.set(i, EnumChatFormatting.GRAY + list.get(i));
                }
            }

            this.drawHoveringText(list, x, y);
        }
        else
        {
            super.renderToolTip(stack, x, y);
        }
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        CreativeTabs creativeTabs = CreativeTabs.creativeTabArray[selectedTabIndex];

        for (CreativeTabs creativetabs1 : CreativeTabs.creativeTabArray)
        {
            this.mc.getTextureManager().bindTexture(creativeInventoryTabs);

            if (creativetabs1.getTabIndex() != selectedTabIndex)
            {
                this.renderTabIcon(creativetabs1);
            }
        }

        this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + creativeTabs.getBackgroundImageName()));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.guiLeft + 175;
        int j = this.guiTop + 18;
        int k = j + 112;
        this.mc.getTextureManager().bindTexture(creativeInventoryTabs);

        if (creativeTabs.shouldHidePlayerInventory())
        {
            this.drawTexturedModalRect(i, j + (int)((float)(k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
        }

        this.renderTabIcon(creativeTabs);

        if (creativeTabs == CreativeTabs.tabInventory)
        {
            GuiInventory.drawEntityOnScreen(this.guiLeft + 43, this.guiTop + 45, 20, (float)(this.guiLeft + 43 - mouseX), (float)(this.guiTop + 45 - 30 - mouseY), this.mc.thePlayer);
        }
    }

    protected boolean isMouseOverTab(CreativeTabs tab, int mouseX, int mouseY)
    {
        int i = tab.getTabColumn();
        int j = 28 * i;
        int k = 0;

        if (i == 5)
        {
            j = this.xSize - 28 + 2;
        }
        else if (i > 0)
        {
            j += i;
        }

        if (tab.isTabInFirstRow())
        {
            k = k - 32;
        }
        else
        {
            k = k + this.ySize;
        }

        return mouseX >= j && mouseX <= j + 28 && mouseY >= k && mouseY <= k + 32;
    }

    protected boolean renderCreativeInventoryHoveringText(CreativeTabs tab, int mouseX, int mouseY)
    {
        int i = tab.getTabColumn();
        int j = 28 * i;
        int k = 0;

        if (i == 5)
        {
            j = this.xSize - 28 + 2;
        }
        else if (i > 0)
        {
            j += i;
        }

        if (tab.isTabInFirstRow())
        {
            k = k - 32;
        }
        else
        {
            k = k + this.ySize;
        }

        if (this.isPointInRegion(j + 3, k + 3, 23, 27, mouseX, mouseY))
        {
            this.drawCreativeTabHoveringText(I18n.format(tab.getTranslatedTabLabel(), new Object[0]), mouseX, mouseY);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void renderTabIcon(CreativeTabs tab)
    {
        boolean flag = tab.getTabIndex() == selectedTabIndex;
        boolean flag1 = tab.isTabInFirstRow();
        int i = tab.getTabColumn();
        int j = i * 28;
        int k = 0;
        int l = this.guiLeft + 28 * i;
        int sixthIntValue = this.guiTop;
        int seventhIntValue = 32;

        if (flag)
        {
            k += 32;
        }

        if (i == 5)
        {
            l = this.guiLeft + this.xSize - 28;
        }
        else if (i > 0)
        {
            l += i;
        }

        if (flag1)
        {
            sixthIntValue = sixthIntValue - 28;
        }
        else
        {
            k += 64;
            sixthIntValue = sixthIntValue + (this.ySize - 4);
        }

        GlStateManager.disableLighting();
        this.drawTexturedModalRect(l, sixthIntValue, j, k, 28, seventhIntValue);
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        l = l + 6;
        sixthIntValue = sixthIntValue + 8 + (flag1 ? 1 : -1);
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        ItemStack itemstack = tab.getIconItemStack();
        this.itemRender.renderItemAndEffectIntoGUI(itemstack, l, sixthIntValue);
        this.itemRender.renderItemOverlays(this.fontRendererObj, itemstack, l, sixthIntValue);
        GlStateManager.disableLighting();
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
        }

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
        }
    }

    public int getSelectedTabIndex()
    {
        return selectedTabIndex;
    }

    static class ContainerCreative extends Container
    {
        public List<ItemStack> itemList = Lists.<ItemStack>newArrayList();

        public ContainerCreative(EntityPlayer player)
        {
            InventoryPlayer inventoryplayer = player.inventory;

            for (int i = 0; i < 5; ++i)
            {
                for (int j = 0; j < 9; ++j)
                {
                    this.addSlotToContainer(new Slot(GuiContainerCreative.creativeInventory, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(inventoryplayer, k, 9 + k * 18, 112));
            }

            this.scrollTo(0.0F);
        }

        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return true;
        }

        public void scrollTo(float currentScroll)
        {
            int i = (this.itemList.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(currentScroll * (float)i) + 0.5D);

            if (j < 0)
            {
                j = 0;
            }

            for (int k = 0; k < 5; ++k)
            {
                for (int l = 0; l < 9; ++l)
                {
                    int intValue = l + (k + j) * 9;

                    if (intValue >= 0 && intValue < this.itemList.size())
                    {
                        GuiContainerCreative.creativeInventory.setInventorySlotContents(l + k * 9, this.itemList.get(intValue));
                    }
                    else
                    {
                        GuiContainerCreative.creativeInventory.setInventorySlotContents(l + k * 9, (ItemStack)null);
                    }
                }
            }
        }

        public boolean canScroll()
        {
            return this.itemList.size() > 45;
        }

        protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn)
        {
        }

        public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
        {
            if (index >= this.inventorySlots.size() - 9 && index < this.inventorySlots.size())
            {
                Slot slot = this.inventorySlots.get(index);

                if (slot != null && slot.getHasStack())
                {
                    slot.putStack((ItemStack)null);
                }
            }

            return null;
        }

        public boolean canMergeSlot(ItemStack stack, Slot slotIn)
        {
            return slotIn.yDisplayPosition > 90;
        }

        public boolean canDragIntoSlot(Slot slotIn)
        {
            return slotIn.inventory instanceof InventoryPlayer || slotIn.yDisplayPosition > 90 && slotIn.xDisplayPosition <= 162;
        }
    }

    class CreativeSlot extends Slot
    {
        private final Slot slot;

        public CreativeSlot(Slot slotIn, int slotNumber)
        {
            super(slotIn.inventory, slotNumber, 0, 0);
            this.slot = slotIn;
        }

        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
        {
            this.slot.onPickupFromSlot(playerIn, stack);
        }

        public boolean isItemValid(ItemStack stack)
        {
            return this.slot.isItemValid(stack);
        }

        public ItemStack getStack()
        {
            return this.slot.getStack();
        }

        public boolean getHasStack()
        {
            return this.slot.getHasStack();
        }

        public void putStack(ItemStack stack)
        {
            this.slot.putStack(stack);
        }

        public void onSlotChanged()
        {
            this.slot.onSlotChanged();
        }

        public int getSlotStackLimit()
        {
            return this.slot.getSlotStackLimit();
        }

        public int getItemStackLimit(ItemStack stack)
        {
            return this.slot.getItemStackLimit(stack);
        }

        public String getSlotTexture()
        {
            return this.slot.getSlotTexture();
        }

        public ItemStack decrStackSize(int amount)
        {
            return this.slot.decrStackSize(amount);
        }

        public boolean isHere(IInventory inv, int slotIn)
        {
            return this.slot.isHere(inv, slotIn);
        }
    }
}
