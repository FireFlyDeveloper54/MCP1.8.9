package net.minecraft.client.gui.inventory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

public class GuiFurnace extends GuiContainer
{
    private static final ResourceLocation furnaceGuiTextures = new ResourceLocation("textures/gui/container/furnace.png");
    private final InventoryPlayer playerInventory;
    private IInventory tileFurnace;

    public GuiFurnace(InventoryPlayer playerInv, IInventory furnaceInv)
    {
        super(new ContainerFurnace(playerInv, furnaceInv));
        this.playerInventory = playerInv;
        this.tileFurnace = furnaceInv;
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String displayName = this.tileFurnace.getDisplayName().getUnformattedText();
        this.fontRendererObj.drawString(displayName, this.xSize / 2 - this.fontRendererObj.getStringWidth(displayName) / 2, 6, 4210752);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(furnaceGuiTextures);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

        if (TileEntityFurnace.isBurning(this.tileFurnace))
        {
            int burnHeight = this.getBurnLeftScaled(13);
            this.drawTexturedModalRect(guiLeft + 56, guiTop + 36 + 12 - burnHeight, 176, 12 - burnHeight, 14, burnHeight + 1);
        }

        int cookWidth = this.getCookProgressScaled(24);
        this.drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 14, cookWidth + 1, 16);
    }

    private int getCookProgressScaled(int pixels)
    {
        int cookTime = this.tileFurnace.getField(2);
        int totalCookTime = this.tileFurnace.getField(3);
        return totalCookTime != 0 && cookTime != 0 ? cookTime * pixels / totalCookTime : 0;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int currentItemBurnTime = this.tileFurnace.getField(1);

        if (currentItemBurnTime == 0)
        {
            currentItemBurnTime = 200;
        }

        return this.tileFurnace.getField(0) * pixels / currentItemBurnTime;
    }
}
