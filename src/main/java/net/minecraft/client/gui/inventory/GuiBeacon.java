package net.minecraft.client.gui.inventory;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiBeacon extends GuiContainer
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation beaconGuiTextures = new ResourceLocation("textures/gui/container/beacon.png");
    private IInventory tileBeacon;
    private GuiBeacon.ConfirmButton beaconConfirmButton;
    private boolean buttonsNotDrawn;

    public GuiBeacon(InventoryPlayer playerInventory, IInventory tileBeaconIn)
    {
        super(new ContainerBeacon(playerInventory, tileBeaconIn));
        this.tileBeacon = tileBeaconIn;
        this.xSize = 230;
        this.ySize = 219;
    }

    public void initGui()
    {
        super.initGui();
        this.buttonList.add(this.beaconConfirmButton = new GuiBeacon.ConfirmButton(-1, this.guiLeft + 164, this.guiTop + 107));
        this.buttonList.add(new GuiBeacon.CancelButton(-2, this.guiLeft + 190, this.guiTop + 107));
        this.buttonsNotDrawn = true;
        this.beaconConfirmButton.enabled = false;
    }

    public void updateScreen()
    {
        super.updateScreen();
        int i = this.tileBeacon.getField(0);
        int j = this.tileBeacon.getField(1);
        int k = this.tileBeacon.getField(2);

        if (this.buttonsNotDrawn && i >= 0)
        {
            this.buttonsNotDrawn = false;

            for (int l = 0; l <= 2; ++l)
            {
                int count = TileEntityBeacon.effectsList[l].length;
                int secondIntValue2 = count * 22 + (count - 1) * 2;

                for (int nestedIndex = 0; nestedIndex < count; ++nestedIndex)
                {
                    int fourthIntValue = TileEntityBeacon.effectsList[l][nestedIndex].id;
                    GuiBeacon.PowerButton guibeacon$powerbutton = new GuiBeacon.PowerButton(l << 8 | fourthIntValue, this.guiLeft + 76 + nestedIndex * 24 - secondIntValue2 / 2, this.guiTop + 22 + l * 25, fourthIntValue, l);
                    this.buttonList.add(guibeacon$powerbutton);

                    if (l >= i)
                    {
                        guibeacon$powerbutton.enabled = false;
                    }
                    else if (fourthIntValue == j)
                    {
                        guibeacon$powerbutton.setSelected(true);
                    }
                }
            }

            int intValue = 3;
            int count2 = TileEntityBeacon.effectsList[intValue].length + 1;
            int intValue3 = count2 * 22 + (count2 - 1) * 2;

            for (int index = 0; index < count2 - 1; ++index)
            {
                int intValue4 = TileEntityBeacon.effectsList[intValue][index].id;
                GuiBeacon.PowerButton guibeacon$powerbutton2 = new GuiBeacon.PowerButton(intValue << 8 | intValue4, this.guiLeft + 167 + index * 24 - intValue3 / 2, this.guiTop + 47, intValue4, intValue);
                this.buttonList.add(guibeacon$powerbutton2);

                if (intValue >= i)
                {
                    guibeacon$powerbutton2.enabled = false;
                }
                else if (intValue4 == k)
                {
                    guibeacon$powerbutton2.setSelected(true);
                }
            }

            if (j > 0)
            {
                GuiBeacon.PowerButton guibeacon$powerbutton1 = new GuiBeacon.PowerButton(intValue << 8 | j, this.guiLeft + 167 + (count2 - 1) * 24 - intValue3 / 2, this.guiTop + 47, j, intValue);
                this.buttonList.add(guibeacon$powerbutton1);

                if (intValue >= i)
                {
                    guibeacon$powerbutton1.enabled = false;
                }
                else if (j == k)
                {
                    guibeacon$powerbutton1.setSelected(true);
                }
            }
        }

        this.beaconConfirmButton.enabled = this.tileBeacon.getStackInSlot(0) != null && j > 0;
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == -2)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (button.id == -1)
        {
            String s = "MC|Beacon";
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(this.tileBeacon.getField(1));
            packetbuffer.writeInt(this.tileBeacon.getField(2));
            this.mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(s, packetbuffer));
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (button instanceof GuiBeacon.PowerButton)
        {
            if (((GuiBeacon.PowerButton)button).isSelected())
            {
                return;
            }

            int j = button.id;
            int k = j & 255;
            int i = j >> 8;

            if (i < 3)
            {
                this.tileBeacon.setField(1, k);
            }
            else
            {
                this.tileBeacon.setField(2, k);
            }

            this.buttonList.clear();
            this.initGui();
            this.updateScreen();
        }
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        RenderHelper.disableStandardItemLighting();
        this.drawCenteredString(this.fontRendererObj, I18n.format("tile.beacon.primary", new Object[0]), 62, 10, 14737632);
        this.drawCenteredString(this.fontRendererObj, I18n.format("tile.beacon.secondary", new Object[0]), 169, 10, 14737632);

        for (GuiButton guiButton : this.buttonList)
        {
            if (guiButton.isMouseOver())
            {
                guiButton.drawButtonForegroundLayer(mouseX - this.guiLeft, mouseY - this.guiTop);
                break;
            }
        }

        RenderHelper.enableGUIStandardItemLighting();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(beaconGuiTextures);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        this.itemRender.zLevel = 100.0F;
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.emerald), i + 42, j + 109);
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.diamond), i + 42 + 22, j + 109);
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.gold_ingot), i + 42 + 44, j + 109);
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.iron_ingot), i + 42 + 66, j + 109);
        this.itemRender.zLevel = 0.0F;
    }

    static class Button extends GuiButton
    {
        private final ResourceLocation iconTexture;
        private final int iconTextureX;
        private final int iconTextureY;
        private boolean selected;

        protected Button(int buttonId, int x, int y, ResourceLocation iconTextureIn, int iconTextureXIn, int iconTextureYIn)
        {
            super(buttonId, x, y, 22, 22, "");
            this.iconTexture = iconTextureIn;
            this.iconTextureX = iconTextureXIn;
            this.iconTextureY = iconTextureYIn;
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                mc.getTextureManager().bindTexture(GuiBeacon.beaconGuiTextures);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                int i = 219;
                int j = 0;

                if (!this.enabled)
                {
                    j += this.width * 2;
                }
                else if (this.selected)
                {
                    j += this.width * 1;
                }
                else if (this.hovered)
                {
                    j += this.width * 3;
                }

                this.drawTexturedModalRect(this.xPosition, this.yPosition, j, i, this.width, this.height);

                if (!GuiBeacon.beaconGuiTextures.equals(this.iconTexture))
                {
                    mc.getTextureManager().bindTexture(this.iconTexture);
                }

                this.drawTexturedModalRect(this.xPosition + 2, this.yPosition + 2, this.iconTextureX, this.iconTextureY, 18, 18);
            }
        }

        public boolean isSelected()
        {
            return this.selected;
        }

        public void setSelected(boolean selectedIn)
        {
            this.selected = selectedIn;
        }
    }

    class CancelButton extends GuiBeacon.Button
    {
        public CancelButton(int buttonId, int x, int y)
        {
            super(buttonId, x, y, GuiBeacon.beaconGuiTextures, 112, 220);
        }

        public void drawButtonForegroundLayer(int mouseX, int mouseY)
        {
            GuiBeacon.this.drawCreativeTabHoveringText(I18n.format("gui.cancel", new Object[0]), mouseX, mouseY);
        }
    }

    class ConfirmButton extends GuiBeacon.Button
    {
        public ConfirmButton(int buttonId, int x, int y)
        {
            super(buttonId, x, y, GuiBeacon.beaconGuiTextures, 90, 220);
        }

        public void drawButtonForegroundLayer(int mouseX, int mouseY)
        {
            GuiBeacon.this.drawCreativeTabHoveringText(I18n.format("gui.done", new Object[0]), mouseX, mouseY);
        }
    }

    class PowerButton extends GuiBeacon.Button
    {
        private final int potionId;
        private final int tier;

        public PowerButton(int buttonId, int x, int y, int potionIdIn, int tierIn)
        {
            super(buttonId, x, y, GuiContainer.inventoryBackground, 0 + Potion.potionTypes[potionIdIn].getStatusIconIndex() % 8 * 18, 198 + Potion.potionTypes[potionIdIn].getStatusIconIndex() / 8 * 18);
            this.potionId = potionIdIn;
            this.tier = tierIn;
        }

        public void drawButtonForegroundLayer(int mouseX, int mouseY)
        {
            String s = I18n.format(Potion.potionTypes[this.potionId].getName(), new Object[0]);

            if (this.tier >= 3 && this.potionId != Potion.regeneration.id)
            {
                s = s + " II";
            }

            GuiBeacon.this.drawCreativeTabHoveringText(s, mouseX, mouseY);
        }
    }
}
