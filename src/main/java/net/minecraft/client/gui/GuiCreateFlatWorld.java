package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;

public class GuiCreateFlatWorld extends GuiScreen
{
    private final GuiCreateWorld createWorldGui;
    private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();
    private String flatWorldTitle;
    private String tileLabel;
    private String heightLabel;
    private GuiCreateFlatWorld.Details createFlatWorldListSlotGui;
    private GuiButton addLayerButton;
    private GuiButton editLayerButton;
    private GuiButton removeLayerButton;

    public GuiCreateFlatWorld(GuiCreateWorld createWorldGuiIn, String generatorInfoString)
    {
        this.createWorldGui = createWorldGuiIn;
        this.setFlatGeneratorInfo(generatorInfoString);
    }

    public String getFlatGeneratorInfo()
    {
        return this.theFlatGeneratorInfo.toString();
    }

    public void setFlatGeneratorInfo(String generatorInfoString)
    {
        this.theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(generatorInfoString);
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.flatWorldTitle = I18n.format("createWorld.customize.flat.title", new Object[0]);
        this.tileLabel = I18n.format("createWorld.customize.flat.tile", new Object[0]);
        this.heightLabel = I18n.format("createWorld.customize.flat.height", new Object[0]);
        this.createFlatWorldListSlotGui = new GuiCreateFlatWorld.Details();
        this.buttonList.add(this.addLayerButton = new GuiButton(2, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer", new Object[0]) + " (NYI)"));
        this.buttonList.add(this.editLayerButton = new GuiButton(3, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer", new Object[0]) + " (NYI)"));
        this.buttonList.add(this.removeLayerButton = new GuiButton(4, this.width / 2 - 155, this.height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 + 5, this.height - 52, 150, 20, I18n.format("createWorld.customize.presets", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.addLayerButton.visible = this.editLayerButton.visible = false;
        this.theFlatGeneratorInfo.updateLayerMinY();
        this.updateButtonStates();
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.createFlatWorldListSlotGui.handleMouseInput();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        int i = this.theFlatGeneratorInfo.getFlatLayers().size() - this.createFlatWorldListSlotGui.selectedLayer - 1;

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(this.createWorldGui);
        }
        else if (button.id == 0)
        {
            this.createWorldGui.chunkProviderSettingsJson = this.getFlatGeneratorInfo();
            this.mc.displayGuiScreen(this.createWorldGui);
        }
        else if (button.id == 5)
        {
            this.mc.displayGuiScreen(new GuiFlatPresets(this));
        }
        else if (button.id == 4 && this.hasSelectedLayer())
        {
            this.theFlatGeneratorInfo.getFlatLayers().remove(i);
            this.createFlatWorldListSlotGui.selectedLayer = Math.min(this.createFlatWorldListSlotGui.selectedLayer, this.theFlatGeneratorInfo.getFlatLayers().size() - 1);
        }

        this.theFlatGeneratorInfo.updateLayerMinY();
        this.updateButtonStates();
    }

    public void updateButtonStates()
    {
        boolean flag = this.hasSelectedLayer();
        this.removeLayerButton.enabled = flag;
        this.editLayerButton.enabled = flag;
        this.editLayerButton.enabled = false;
        this.addLayerButton.enabled = false;
    }

    private boolean hasSelectedLayer()
    {
        return this.createFlatWorldListSlotGui.selectedLayer > -1 && this.createFlatWorldListSlotGui.selectedLayer < this.theFlatGeneratorInfo.getFlatLayers().size();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.flatWorldTitle, this.width / 2, 8, 16777215);
        int i = this.width / 2 - 92 - 16;
        this.drawString(this.fontRendererObj, this.tileLabel, i, 32, 16777215);
        this.drawString(this.fontRendererObj, this.heightLabel, i + 2 + 213 - this.fontRendererObj.getStringWidth(this.heightLabel), 32, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class Details extends GuiSlot
    {
        public int selectedLayer = -1;

        public Details()
        {
            super(GuiCreateFlatWorld.this.mc, GuiCreateFlatWorld.this.width, GuiCreateFlatWorld.this.height, 43, GuiCreateFlatWorld.this.height - 60, 24);
        }

        private void drawItemIcon(int x, int y, ItemStack stack)
        {
            this.drawIconBackground(x + 1, y + 1);
            GlStateManager.enableRescaleNormal();

            if (stack != null && stack.getItem() != null)
            {
                RenderHelper.enableGUIStandardItemLighting();
                GuiCreateFlatWorld.this.itemRender.renderItemIntoGUI(stack, x + 2, y + 2);
                RenderHelper.disableStandardItemLighting();
            }

            GlStateManager.disableRescaleNormal();
        }

        private void drawIconBackground(int x, int y)
        {
            this.drawSlotTexture(x, y, 0, 0);
        }

        private void drawSlotTexture(int x, int y, int u, int v)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(Gui.statIcons);
            float f = 0.0078125F;
            float vScale = 0.0078125F;
            int i = 18;
            int j = 18;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos((double)(x + 0), (double)(y + 18), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(u + 0) * 0.0078125F), (double)((float)(v + 18) * 0.0078125F)).endVertex();
            worldrenderer.pos((double)(x + 18), (double)(y + 18), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(u + 18) * 0.0078125F), (double)((float)(v + 18) * 0.0078125F)).endVertex();
            worldrenderer.pos((double)(x + 18), (double)(y + 0), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(u + 18) * 0.0078125F), (double)((float)(v + 0) * 0.0078125F)).endVertex();
            worldrenderer.pos((double)(x + 0), (double)(y + 0), (double)GuiCreateFlatWorld.this.zLevel).tex((double)((float)(u + 0) * 0.0078125F), (double)((float)(v + 0) * 0.0078125F)).endVertex();
            tessellator.draw();
        }

        protected int getSize()
        {
            return GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            this.selectedLayer = slotIndex;
            GuiCreateFlatWorld.this.updateButtonStates();
        }

        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == this.selectedLayer;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            FlatLayerInfo flatLayerInfo = (FlatLayerInfo)GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().get(GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
            IBlockState iblockstate = flatLayerInfo.getLayerMaterial();
            Block block = iblockstate.getBlock();
            Item item = Item.getItemFromBlock(block);
            ItemStack itemStack = block != Blocks.air && item != null ? new ItemStack(item, 1, block.getMetaFromState(iblockstate)) : null;
            String s = itemStack == null ? "Air" : item.getItemStackDisplayName(itemStack);

            if (item == null)
            {
                if (block != Blocks.water && block != Blocks.flowing_water)
                {
                    if (block == Blocks.lava || block == Blocks.flowing_lava)
                    {
                        item = Items.lava_bucket;
                    }
                }
                else
                {
                    item = Items.water_bucket;
                }

                if (item != null)
                {
                    itemStack = new ItemStack(item, 1, block.getMetaFromState(iblockstate));
                    s = block.getLocalizedName();
                }
            }

            this.drawItemIcon(x, y, itemStack);
            GuiCreateFlatWorld.this.fontRendererObj.drawString(s, x + 18 + 5, y + 3, 16777215);
            String layerHeightText;

            if (entryID == 0)
            {
                layerHeightText = I18n.format("createWorld.customize.flat.layer.top", new Object[] {Integer.valueOf(flatLayerInfo.getLayerCount())});
            }
            else if (entryID == GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - 1)
            {
                layerHeightText = I18n.format("createWorld.customize.flat.layer.bottom", new Object[] {Integer.valueOf(flatLayerInfo.getLayerCount())});
            }
            else
            {
                layerHeightText = I18n.format("createWorld.customize.flat.layer", new Object[] {Integer.valueOf(flatLayerInfo.getLayerCount())});
            }

            GuiCreateFlatWorld.this.fontRendererObj.drawString(layerHeightText, x + 2 + 213 - GuiCreateFlatWorld.this.fontRendererObj.getStringWidth(layerHeightText), y + 3, 16777215);
        }

        protected int getScrollBarX()
        {
            return this.width - 70;
        }
    }
}
