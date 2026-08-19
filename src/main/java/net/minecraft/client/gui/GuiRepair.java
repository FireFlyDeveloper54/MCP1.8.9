package net.minecraft.client.gui;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class GuiRepair extends GuiContainer implements ICrafting
{
    private static final ResourceLocation anvilResource = new ResourceLocation("textures/gui/container/anvil.png");
    private ContainerRepair anvil;
    private GuiTextField nameField;
    private InventoryPlayer playerInventory;

    public GuiRepair(InventoryPlayer inventoryIn, World worldIn)
    {
        super(new ContainerRepair(inventoryIn, worldIn, Minecraft.getMinecraft().thePlayer));
        this.playerInventory = inventoryIn;
        this.anvil = (ContainerRepair)this.inventorySlots;
    }

    public void initGui()
    {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.nameField = new GuiTextField(0, this.fontRendererObj, guiLeft + 62, guiTop + 24, 103, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(30);
        this.inventorySlots.removeCraftingFromCrafters(this);
        this.inventorySlots.onCraftGuiOpened(this);
    }

    public void onGuiClosed()
    {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.inventorySlots.removeCraftingFromCrafters(this);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.fontRendererObj.drawString(I18n.format("container.repair", new Object[0]), 60, 6, 4210752);

        if (this.anvil.maximumCost > 0)
        {
            int costColor = 8453920;
            boolean canDrawCost = true;
            String costText = I18n.format("container.repair.cost", new Object[] {Integer.valueOf(this.anvil.maximumCost)});

            if (this.anvil.maximumCost >= 40 && !this.mc.thePlayer.capabilities.isCreativeMode)
            {
                costText = I18n.format("container.repair.expensive", new Object[0]);
                costColor = 16736352;
            }
            else if (!this.anvil.getSlot(2).getHasStack())
            {
                canDrawCost = false;
            }
            else if (!this.anvil.getSlot(2).canTakeStack(this.playerInventory.player))
            {
                costColor = 16736352;
            }

            if (canDrawCost)
            {
                int shadowColor = -16777216 | (costColor & 16579836) >> 2 | costColor & -16777216;
                int costTextX = this.xSize - 8 - this.fontRendererObj.getStringWidth(costText);
                int costTextY = 67;

                if (this.fontRendererObj.getUnicodeFlag())
                {
                    drawRect(costTextX - 3, costTextY - 2, this.xSize - 7, costTextY + 10, -16777216);
                    drawRect(costTextX - 2, costTextY - 1, this.xSize - 8, costTextY + 9, -12895429);
                }
                else
                {
                    this.fontRendererObj.drawString(costText, costTextX, costTextY + 1, shadowColor);
                    this.fontRendererObj.drawString(costText, costTextX + 1, costTextY, shadowColor);
                    this.fontRendererObj.drawString(costText, costTextX + 1, costTextY + 1, shadowColor);
                }

                this.fontRendererObj.drawString(costText, costTextX, costTextY, costColor);
            }
        }

        GlStateManager.enableLighting();
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode))
        {
            this.renameItem();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void renameItem()
    {
        String itemName = this.nameField.getText();
        Slot slot = this.anvil.getSlot(0);

        if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName() && itemName.equals(slot.getStack().getDisplayName()))
        {
            itemName = "";
        }

        this.anvil.updateItemName(itemName);
        this.mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("MC|ItemName", (new PacketBuffer(Unpooled.buffer())).writeString(itemName)));
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.nameField.drawTextBox();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(anvilResource);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(guiLeft + 59, guiTop + 20, 0, this.ySize + (this.anvil.getSlot(0).getHasStack() ? 0 : 16), 110, 16);

        if ((this.anvil.getSlot(0).getHasStack() || this.anvil.getSlot(1).getHasStack()) && !this.anvil.getSlot(2).getHasStack())
        {
            this.drawTexturedModalRect(guiLeft + 99, guiTop + 45, this.xSize, 0, 28, 21);
        }
    }

    public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList)
    {
        this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
    }

    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
    {
        if (slotInd == 0)
        {
            this.nameField.setText(stack == null ? "" : stack.getDisplayName());
            this.nameField.setEnabled(stack != null);

            if (stack != null)
            {
                this.renameItem();
            }
        }
    }

    public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue)
    {
    }

    public void sendAllWindowProperties(Container containerIn, IInventory inventory)
    {
    }
}
