package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BlockTallGrass;
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
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import org.lwjgl.input.Keyboard;

public class GuiFlatPresets extends GuiScreen
{
    private static final List<GuiFlatPresets.LayerItem> FLAT_WORLD_PRESETS = Lists.<GuiFlatPresets.LayerItem>newArrayList();
    private final GuiCreateFlatWorld parentScreen;
    private String presetsTitle;
    private String presetsShare;
    private String presetsListTitle;
    private GuiFlatPresets.ListSlot presetList;
    private GuiButton selectButton;
    private GuiTextField exportTextField;

    public GuiFlatPresets(GuiCreateFlatWorld parentScreen)
    {
        this.parentScreen = parentScreen;
    }

    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        this.presetsTitle = I18n.format("createWorld.customize.presets.title", new Object[0]);
        this.presetsShare = I18n.format("createWorld.customize.presets.share", new Object[0]);
        this.presetsListTitle = I18n.format("createWorld.customize.presets.list", new Object[0]);
        this.exportTextField = new GuiTextField(2, this.fontRendererObj, 50, 40, this.width - 100, 20);
        this.presetList = new GuiFlatPresets.ListSlot();
        this.exportTextField.setMaxStringLength(1230);
        this.exportTextField.setText(this.parentScreen.getFlatGeneratorInfo());
        this.buttonList.add(this.selectButton = new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("createWorld.customize.presets.select", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.updateSelectButton();
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.presetList.handleMouseInput();
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        this.exportTextField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!this.exportTextField.textboxKeyTyped(typedChar, keyCode))
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0 && this.canSelectPreset())
        {
            this.parentScreen.setFlatGeneratorInfo(this.exportTextField.getText());
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 1)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.presetList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.presetsTitle, this.width / 2, 8, 16777215);
        this.drawString(this.fontRendererObj, this.presetsShare, 50, 30, 10526880);
        this.drawString(this.fontRendererObj, this.presetsListTitle, 50, 70, 10526880);
        this.exportTextField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void updateScreen()
    {
        this.exportTextField.updateCursorCounter();
        super.updateScreen();
    }

    public void updateSelectButton()
    {
        boolean canSelectPreset = this.canSelectPreset();
        this.selectButton.enabled = canSelectPreset;
    }

    private boolean canSelectPreset()
    {
        return this.presetList.selectedIndex > -1 && this.presetList.selectedIndex < FLAT_WORLD_PRESETS.size() || this.exportTextField.getText().length() > 1;
    }

    private static void addPreset(String name, Item iconItem, BiomeGenBase biome, FlatLayerInfo... layers)
    {
        addPreset(name, iconItem, 0, biome, (List<String>)null, layers);
    }

    private static void addPreset(String name, Item iconItem, BiomeGenBase biome, List<String> features, FlatLayerInfo... layers)
    {
        addPreset(name, iconItem, 0, biome, features, layers);
    }

    private static void addPreset(String name, Item iconItem, int iconMetadata, BiomeGenBase biome, List<String> features, FlatLayerInfo... layers)
    {
        FlatGeneratorInfo flatGeneratorInfo = new FlatGeneratorInfo();

        for (int layerIndex = layers.length - 1; layerIndex >= 0; --layerIndex)
        {
            flatGeneratorInfo.getFlatLayers().add(layers[layerIndex]);
        }

        flatGeneratorInfo.setBiome(biome.biomeID);
        flatGeneratorInfo.updateLayerMinY();

        if (features != null)
        {
            for (String featureName : features)
            {
                flatGeneratorInfo.getWorldFeatures().put(featureName, Maps.<String, String>newHashMap());
            }
        }

        FLAT_WORLD_PRESETS.add(new GuiFlatPresets.LayerItem(iconItem, iconMetadata, name, flatGeneratorInfo.toString()));
    }

    static
    {
        addPreset("Classic Flat", Item.getItemFromBlock(Blocks.grass), BiomeGenBase.plains, Arrays.<String>asList(new String[] {"village"}), new FlatLayerInfo[] {new FlatLayerInfo(1, Blocks.grass), new FlatLayerInfo(2, Blocks.dirt), new FlatLayerInfo(1, Blocks.bedrock)});
        addPreset("Tunnelers\' Dream", Item.getItemFromBlock(Blocks.stone), BiomeGenBase.extremeHills, Arrays.<String>asList(new String[] {"biome_1", "dungeon", "decoration", "stronghold", "mineshaft"}), new FlatLayerInfo[] {new FlatLayerInfo(1, Blocks.grass), new FlatLayerInfo(5, Blocks.dirt), new FlatLayerInfo(230, Blocks.stone), new FlatLayerInfo(1, Blocks.bedrock)});
        addPreset("Water World", Items.water_bucket, BiomeGenBase.deepOcean, Arrays.<String>asList(new String[] {"biome_1", "oceanmonument"}), new FlatLayerInfo[] {new FlatLayerInfo(90, Blocks.water), new FlatLayerInfo(5, Blocks.sand), new FlatLayerInfo(5, Blocks.dirt), new FlatLayerInfo(5, Blocks.stone), new FlatLayerInfo(1, Blocks.bedrock)});
        addPreset("Overworld", Item.getItemFromBlock(Blocks.tallgrass), BlockTallGrass.EnumType.GRASS.getMeta(), BiomeGenBase.plains, Arrays.<String>asList(new String[] {"village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon", "lake", "lava_lake"}), new FlatLayerInfo[] {new FlatLayerInfo(1, Blocks.grass), new FlatLayerInfo(3, Blocks.dirt), new FlatLayerInfo(59, Blocks.stone), new FlatLayerInfo(1, Blocks.bedrock)});
        addPreset("Snowy Kingdom", Item.getItemFromBlock(Blocks.snow_layer), BiomeGenBase.icePlains, Arrays.<String>asList(new String[] {"village", "biome_1"}), new FlatLayerInfo[] {new FlatLayerInfo(1, Blocks.snow_layer), new FlatLayerInfo(1, Blocks.grass), new FlatLayerInfo(3, Blocks.dirt), new FlatLayerInfo(59, Blocks.stone), new FlatLayerInfo(1, Blocks.bedrock)});
        addPreset("Bottomless Pit", Items.feather, BiomeGenBase.plains, Arrays.<String>asList(new String[] {"village", "biome_1"}), new FlatLayerInfo[] {new FlatLayerInfo(1, Blocks.grass), new FlatLayerInfo(3, Blocks.dirt), new FlatLayerInfo(2, Blocks.cobblestone)});
        addPreset("Desert", Item.getItemFromBlock(Blocks.sand), BiomeGenBase.desert, Arrays.<String>asList(new String[] {"village", "biome_1", "decoration", "stronghold", "mineshaft", "dungeon"}), new FlatLayerInfo[] {new FlatLayerInfo(8, Blocks.sand), new FlatLayerInfo(52, Blocks.sandstone), new FlatLayerInfo(3, Blocks.stone), new FlatLayerInfo(1, Blocks.bedrock)});
        addPreset("Redstone Ready", Items.redstone, BiomeGenBase.desert, new FlatLayerInfo[] {new FlatLayerInfo(52, Blocks.sandstone), new FlatLayerInfo(3, Blocks.stone), new FlatLayerInfo(1, Blocks.bedrock)});
    }

    static class LayerItem
    {
        public Item iconItem;
        public int iconMetadata;
        public String name;
        public String generatorInfo;

        public LayerItem(Item iconItem, int iconMetadata, String name, String generatorInfo)
        {
            this.iconItem = iconItem;
            this.iconMetadata = iconMetadata;
            this.name = name;
            this.generatorInfo = generatorInfo;
        }
    }

    class ListSlot extends GuiSlot
    {
        public int selectedIndex = -1;

        public ListSlot()
        {
            super(GuiFlatPresets.this.mc, GuiFlatPresets.this.width, GuiFlatPresets.this.height, 80, GuiFlatPresets.this.height - 37, 24);
        }

        private void drawItemIcon(int x, int y, Item item, int metadata)
        {
            this.drawIconBackground(x + 1, y + 1);
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            GuiFlatPresets.this.itemRender.renderItemIntoGUI(new ItemStack(item, 1, metadata), x + 2, y + 2);
            RenderHelper.disableStandardItemLighting();
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
            float textureScaleU = 0.0078125F;
            float textureScaleV = 0.0078125F;
            int slotTextureWidth = 18;
            int slotTextureHeight = 18;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos((double)(x + 0), (double)(y + slotTextureHeight), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(u + 0) * textureScaleU), (double)((float)(v + slotTextureHeight) * textureScaleV)).endVertex();
            worldrenderer.pos((double)(x + slotTextureWidth), (double)(y + slotTextureHeight), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(u + slotTextureWidth) * textureScaleU), (double)((float)(v + slotTextureHeight) * textureScaleV)).endVertex();
            worldrenderer.pos((double)(x + slotTextureWidth), (double)(y + 0), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(u + slotTextureWidth) * textureScaleU), (double)((float)(v + 0) * textureScaleV)).endVertex();
            worldrenderer.pos((double)(x + 0), (double)(y + 0), (double)GuiFlatPresets.this.zLevel).tex((double)((float)(u + 0) * textureScaleU), (double)((float)(v + 0) * textureScaleV)).endVertex();
            tessellator.draw();
        }

        protected int getSize()
        {
            return GuiFlatPresets.FLAT_WORLD_PRESETS.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            this.selectedIndex = slotIndex;
            GuiFlatPresets.this.updateSelectButton();
            GuiFlatPresets.this.exportTextField.setText(((GuiFlatPresets.LayerItem)GuiFlatPresets.FLAT_WORLD_PRESETS.get(GuiFlatPresets.this.presetList.selectedIndex)).generatorInfo);
        }

        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == this.selectedIndex;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn)
        {
            GuiFlatPresets.LayerItem guiflatpresets$layeritem = (GuiFlatPresets.LayerItem)GuiFlatPresets.FLAT_WORLD_PRESETS.get(entryID);
            this.drawItemIcon(x, y, guiflatpresets$layeritem.iconItem, guiflatpresets$layeritem.iconMetadata);
            GuiFlatPresets.this.fontRendererObj.drawString(guiflatpresets$layeritem.name, x + 18 + 5, y + 6, 16777215);
        }
    }
}
