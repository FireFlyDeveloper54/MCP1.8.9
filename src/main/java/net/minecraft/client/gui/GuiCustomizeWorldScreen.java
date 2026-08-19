package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.primitives.Floats;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.ChunkProviderSettings;

public class GuiCustomizeWorldScreen extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder
{
    private GuiCreateWorld parentScreen;
    protected String title = "Customize World Settings";
    protected String pageIndicatorText = "Page 1 of 3";
    protected String currentPageTitle = "Basic Settings";
    protected String[] pageTitles = new String[4];
    private GuiPageButtonList settingsList;
    private GuiButton doneButton;
    private GuiButton randomizeButton;
    private GuiButton defaultsButton;
    private GuiButton previousPageButton;
    private GuiButton nextPageButton;
    private GuiButton confirmYesButton;
    private GuiButton confirmNoButton;
    private GuiButton presetsButton;
    private boolean hasChanged = false;
    private int pendingConfirmationButtonId = 0;
    private boolean ignoreNextMouseRelease = false;
    private Predicate<String> nonNegativeFloatValidator = new Predicate<String>()
    {
        public boolean apply(String text)
        {
            Float f = Floats.tryParse(text);
            return text.length() == 0 || f != null && Floats.isFinite(f.floatValue()) && f.floatValue() >= 0.0F;
        }
    };
    private ChunkProviderSettings.Factory defaultSettings = new ChunkProviderSettings.Factory();
    private ChunkProviderSettings.Factory settings;
    private Random random = new Random();

    public GuiCustomizeWorldScreen(GuiScreen parentScreenIn, String settingsJson)
    {
        this.parentScreen = (GuiCreateWorld)parentScreenIn;
        this.loadSettingsJson(settingsJson);
    }

    public void initGui()
    {
        int i = 0;
        int j = 0;

        if (this.settingsList != null)
        {
            i = this.settingsList.getCurrentPage();
            j = this.settingsList.getAmountScrolled();
        }

        this.title = I18n.format("options.customizeTitle", new Object[0]);
        this.buttonList.clear();
        this.buttonList.add(this.previousPageButton = new GuiButton(302, 20, 5, 80, 20, I18n.format("createWorld.customize.custom.prev", new Object[0])));
        this.buttonList.add(this.nextPageButton = new GuiButton(303, this.width - 100, 5, 80, 20, I18n.format("createWorld.customize.custom.next", new Object[0])));
        this.buttonList.add(this.defaultsButton = new GuiButton(304, this.width / 2 - 187, this.height - 27, 90, 20, I18n.format("createWorld.customize.custom.defaults", new Object[0])));
        this.buttonList.add(this.randomizeButton = new GuiButton(301, this.width / 2 - 92, this.height - 27, 90, 20, I18n.format("createWorld.customize.custom.randomize", new Object[0])));
        this.buttonList.add(this.presetsButton = new GuiButton(305, this.width / 2 + 3, this.height - 27, 90, 20, I18n.format("createWorld.customize.custom.presets", new Object[0])));
        this.buttonList.add(this.doneButton = new GuiButton(300, this.width / 2 + 98, this.height - 27, 90, 20, I18n.format("gui.done", new Object[0])));
        this.defaultsButton.enabled = this.hasChanged;
        this.confirmYesButton = new GuiButton(306, this.width / 2 - 55, 160, 50, 20, I18n.format("gui.yes", new Object[0]));
        this.confirmYesButton.visible = false;
        this.buttonList.add(this.confirmYesButton);
        this.confirmNoButton = new GuiButton(307, this.width / 2 + 5, 160, 50, 20, I18n.format("gui.no", new Object[0]));
        this.confirmNoButton.visible = false;
        this.buttonList.add(this.confirmNoButton);

        if (this.pendingConfirmationButtonId != 0)
        {
            this.confirmYesButton.visible = true;
            this.confirmNoButton.visible = true;
        }

        this.createSettingsList();

        if (i != 0)
        {
            this.settingsList.setPage(i);
            this.settingsList.scrollBy(j);
            this.updatePageButtons();
        }
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.settingsList.handleMouseInput();
    }

    private void createSettingsList()
    {
        GuiPageButtonList.GuiListEntry[] basicEntries = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiSlideEntry(160, I18n.format("createWorld.customize.custom.seaLevel", new Object[0]), true, this, 1.0F, 255.0F, (float)this.settings.seaLevel), new GuiPageButtonList.GuiButtonEntry(148, I18n.format("createWorld.customize.custom.useCaves", new Object[0]), true, this.settings.useCaves), new GuiPageButtonList.GuiButtonEntry(150, I18n.format("createWorld.customize.custom.useStrongholds", new Object[0]), true, this.settings.useStrongholds), new GuiPageButtonList.GuiButtonEntry(151, I18n.format("createWorld.customize.custom.useVillages", new Object[0]), true, this.settings.useVillages), new GuiPageButtonList.GuiButtonEntry(152, I18n.format("createWorld.customize.custom.useMineShafts", new Object[0]), true, this.settings.useMineShafts), new GuiPageButtonList.GuiButtonEntry(153, I18n.format("createWorld.customize.custom.useTemples", new Object[0]), true, this.settings.useTemples), new GuiPageButtonList.GuiButtonEntry(210, I18n.format("createWorld.customize.custom.useMonuments", new Object[0]), true, this.settings.useMonuments), new GuiPageButtonList.GuiButtonEntry(154, I18n.format("createWorld.customize.custom.useRavines", new Object[0]), true, this.settings.useRavines), new GuiPageButtonList.GuiButtonEntry(149, I18n.format("createWorld.customize.custom.useDungeons", new Object[0]), true, this.settings.useDungeons), new GuiPageButtonList.GuiSlideEntry(157, I18n.format("createWorld.customize.custom.dungeonChance", new Object[0]), true, this, 1.0F, 100.0F, (float)this.settings.dungeonChance), new GuiPageButtonList.GuiButtonEntry(155, I18n.format("createWorld.customize.custom.useWaterLakes", new Object[0]), true, this.settings.useWaterLakes), new GuiPageButtonList.GuiSlideEntry(158, I18n.format("createWorld.customize.custom.waterLakeChance", new Object[0]), true, this, 1.0F, 100.0F, (float)this.settings.waterLakeChance), new GuiPageButtonList.GuiButtonEntry(156, I18n.format("createWorld.customize.custom.useLavaLakes", new Object[0]), true, this.settings.useLavaLakes), new GuiPageButtonList.GuiSlideEntry(159, I18n.format("createWorld.customize.custom.lavaLakeChance", new Object[0]), true, this, 10.0F, 100.0F, (float)this.settings.lavaLakeChance), new GuiPageButtonList.GuiButtonEntry(161, I18n.format("createWorld.customize.custom.useLavaOceans", new Object[0]), true, this.settings.useLavaOceans), new GuiPageButtonList.GuiSlideEntry(162, I18n.format("createWorld.customize.custom.fixedBiome", new Object[0]), true, this, -1.0F, 37.0F, (float)this.settings.fixedBiome), new GuiPageButtonList.GuiSlideEntry(163, I18n.format("createWorld.customize.custom.biomeSize", new Object[0]), true, this, 1.0F, 8.0F, (float)this.settings.biomeSize), new GuiPageButtonList.GuiSlideEntry(164, I18n.format("createWorld.customize.custom.riverSize", new Object[0]), true, this, 1.0F, 5.0F, (float)this.settings.riverSize)};
        GuiPageButtonList.GuiListEntry[] oreEntries = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiLabelEntry(416, I18n.format("tile.dirt.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(165, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.dirtSize), new GuiPageButtonList.GuiSlideEntry(166, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.dirtCount), new GuiPageButtonList.GuiSlideEntry(167, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.dirtMinHeight), new GuiPageButtonList.GuiSlideEntry(168, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.dirtMaxHeight), new GuiPageButtonList.GuiLabelEntry(417, I18n.format("tile.gravel.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(169, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.gravelSize), new GuiPageButtonList.GuiSlideEntry(170, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.gravelCount), new GuiPageButtonList.GuiSlideEntry(171, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.gravelMinHeight), new GuiPageButtonList.GuiSlideEntry(172, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.gravelMaxHeight), new GuiPageButtonList.GuiLabelEntry(418, I18n.format("tile.stone.granite.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(173, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.graniteSize), new GuiPageButtonList.GuiSlideEntry(174, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.graniteCount), new GuiPageButtonList.GuiSlideEntry(175, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.graniteMinHeight), new GuiPageButtonList.GuiSlideEntry(176, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.graniteMaxHeight), new GuiPageButtonList.GuiLabelEntry(419, I18n.format("tile.stone.diorite.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(177, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.dioriteSize), new GuiPageButtonList.GuiSlideEntry(178, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.dioriteCount), new GuiPageButtonList.GuiSlideEntry(179, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.dioriteMinHeight), new GuiPageButtonList.GuiSlideEntry(180, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.dioriteMaxHeight), new GuiPageButtonList.GuiLabelEntry(420, I18n.format("tile.stone.andesite.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(181, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.andesiteSize), new GuiPageButtonList.GuiSlideEntry(182, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.andesiteCount), new GuiPageButtonList.GuiSlideEntry(183, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.andesiteMinHeight), new GuiPageButtonList.GuiSlideEntry(184, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.andesiteMaxHeight), new GuiPageButtonList.GuiLabelEntry(421, I18n.format("tile.oreCoal.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(185, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.coalSize), new GuiPageButtonList.GuiSlideEntry(186, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.coalCount), new GuiPageButtonList.GuiSlideEntry(187, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.coalMinHeight), new GuiPageButtonList.GuiSlideEntry(189, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.coalMaxHeight), new GuiPageButtonList.GuiLabelEntry(422, I18n.format("tile.oreIron.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(190, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.ironSize), new GuiPageButtonList.GuiSlideEntry(191, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.ironCount), new GuiPageButtonList.GuiSlideEntry(192, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.ironMinHeight), new GuiPageButtonList.GuiSlideEntry(193, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.ironMaxHeight), new GuiPageButtonList.GuiLabelEntry(423, I18n.format("tile.oreGold.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(194, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.goldSize), new GuiPageButtonList.GuiSlideEntry(195, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.goldCount), new GuiPageButtonList.GuiSlideEntry(196, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.goldMinHeight), new GuiPageButtonList.GuiSlideEntry(197, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.goldMaxHeight), new GuiPageButtonList.GuiLabelEntry(424, I18n.format("tile.oreRedstone.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(198, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.redstoneSize), new GuiPageButtonList.GuiSlideEntry(199, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.redstoneCount), new GuiPageButtonList.GuiSlideEntry(200, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.redstoneMinHeight), new GuiPageButtonList.GuiSlideEntry(201, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.redstoneMaxHeight), new GuiPageButtonList.GuiLabelEntry(425, I18n.format("tile.oreDiamond.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(202, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.diamondSize), new GuiPageButtonList.GuiSlideEntry(203, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.diamondCount), new GuiPageButtonList.GuiSlideEntry(204, I18n.format("createWorld.customize.custom.minHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.diamondMinHeight), new GuiPageButtonList.GuiSlideEntry(205, I18n.format("createWorld.customize.custom.maxHeight", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.diamondMaxHeight), new GuiPageButtonList.GuiLabelEntry(426, I18n.format("tile.oreLapis.name", new Object[0]), false), null, new GuiPageButtonList.GuiSlideEntry(206, I18n.format("createWorld.customize.custom.size", new Object[0]), false, this, 1.0F, 50.0F, (float)this.settings.lapisSize), new GuiPageButtonList.GuiSlideEntry(207, I18n.format("createWorld.customize.custom.count", new Object[0]), false, this, 0.0F, 40.0F, (float)this.settings.lapisCount), new GuiPageButtonList.GuiSlideEntry(208, I18n.format("createWorld.customize.custom.center", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.lapisCenterHeight), new GuiPageButtonList.GuiSlideEntry(209, I18n.format("createWorld.customize.custom.spread", new Object[0]), false, this, 0.0F, 255.0F, (float)this.settings.lapisSpread)};
        GuiPageButtonList.GuiListEntry[] noiseSliderEntries = new GuiPageButtonList.GuiListEntry[] {new GuiPageButtonList.GuiSlideEntry(100, I18n.format("createWorld.customize.custom.mainNoiseScaleX", new Object[0]), false, this, 1.0F, 5000.0F, this.settings.mainNoiseScaleX), new GuiPageButtonList.GuiSlideEntry(101, I18n.format("createWorld.customize.custom.mainNoiseScaleY", new Object[0]), false, this, 1.0F, 5000.0F, this.settings.mainNoiseScaleY), new GuiPageButtonList.GuiSlideEntry(102, I18n.format("createWorld.customize.custom.mainNoiseScaleZ", new Object[0]), false, this, 1.0F, 5000.0F, this.settings.mainNoiseScaleZ), new GuiPageButtonList.GuiSlideEntry(103, I18n.format("createWorld.customize.custom.depthNoiseScaleX", new Object[0]), false, this, 1.0F, 2000.0F, this.settings.depthNoiseScaleX), new GuiPageButtonList.GuiSlideEntry(104, I18n.format("createWorld.customize.custom.depthNoiseScaleZ", new Object[0]), false, this, 1.0F, 2000.0F, this.settings.depthNoiseScaleZ), new GuiPageButtonList.GuiSlideEntry(105, I18n.format("createWorld.customize.custom.depthNoiseScaleExponent", new Object[0]), false, this, 0.01F, 20.0F, this.settings.depthNoiseScaleExponent), new GuiPageButtonList.GuiSlideEntry(106, I18n.format("createWorld.customize.custom.baseSize", new Object[0]), false, this, 1.0F, 25.0F, this.settings.baseSize), new GuiPageButtonList.GuiSlideEntry(107, I18n.format("createWorld.customize.custom.coordinateScale", new Object[0]), false, this, 1.0F, 6000.0F, this.settings.coordinateScale), new GuiPageButtonList.GuiSlideEntry(108, I18n.format("createWorld.customize.custom.heightScale", new Object[0]), false, this, 1.0F, 6000.0F, this.settings.heightScale), new GuiPageButtonList.GuiSlideEntry(109, I18n.format("createWorld.customize.custom.stretchY", new Object[0]), false, this, 0.01F, 50.0F, this.settings.stretchY), new GuiPageButtonList.GuiSlideEntry(110, I18n.format("createWorld.customize.custom.upperLimitScale", new Object[0]), false, this, 1.0F, 5000.0F, this.settings.upperLimitScale), new GuiPageButtonList.GuiSlideEntry(111, I18n.format("createWorld.customize.custom.lowerLimitScale", new Object[0]), false, this, 1.0F, 5000.0F, this.settings.lowerLimitScale), new GuiPageButtonList.GuiSlideEntry(112, I18n.format("createWorld.customize.custom.biomeDepthWeight", new Object[0]), false, this, 1.0F, 20.0F, this.settings.biomeDepthWeight), new GuiPageButtonList.GuiSlideEntry(113, I18n.format("createWorld.customize.custom.biomeDepthOffset", new Object[0]), false, this, 0.0F, 20.0F, this.settings.biomeDepthOffset), new GuiPageButtonList.GuiSlideEntry(114, I18n.format("createWorld.customize.custom.biomeScaleWeight", new Object[0]), false, this, 1.0F, 20.0F, this.settings.biomeScaleWeight), new GuiPageButtonList.GuiSlideEntry(115, I18n.format("createWorld.customize.custom.biomeScaleOffset", new Object[0]), false, this, 0.0F, 20.0F, this.settings.biomeScaleOffset)};
        GuiPageButtonList.GuiListEntry[] editBoxEntries = new GuiPageButtonList.GuiListEntry[] {
            new GuiPageButtonList.GuiLabelEntry(400, I18n.format("createWorld.customize.custom.mainNoiseScaleX", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(132, this.formatValue(132, this.settings.mainNoiseScaleX), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(401, I18n.format("createWorld.customize.custom.mainNoiseScaleY", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(133, this.formatValue(133, this.settings.mainNoiseScaleY), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(402, I18n.format("createWorld.customize.custom.mainNoiseScaleZ", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(134, this.formatValue(134, this.settings.mainNoiseScaleZ), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(403, I18n.format("createWorld.customize.custom.depthNoiseScaleX", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(135, this.formatValue(135, this.settings.depthNoiseScaleX), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(404, I18n.format("createWorld.customize.custom.depthNoiseScaleZ", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(136, this.formatValue(136, this.settings.depthNoiseScaleZ), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(405, I18n.format("createWorld.customize.custom.depthNoiseScaleExponent", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(137, this.formatValue(137, this.settings.depthNoiseScaleExponent), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(406, I18n.format("createWorld.customize.custom.baseSize", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(138, this.formatValue(138, this.settings.baseSize), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(407, I18n.format("createWorld.customize.custom.coordinateScale", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(139, this.formatValue(139, this.settings.coordinateScale), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(408, I18n.format("createWorld.customize.custom.heightScale", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(140, this.formatValue(140, this.settings.heightScale), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(409, I18n.format("createWorld.customize.custom.stretchY", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(141, this.formatValue(141, this.settings.stretchY), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(410, I18n.format("createWorld.customize.custom.upperLimitScale", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(142, this.formatValue(142, this.settings.upperLimitScale), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(411, I18n.format("createWorld.customize.custom.lowerLimitScale", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(143, this.formatValue(143, this.settings.lowerLimitScale), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(412, I18n.format("createWorld.customize.custom.biomeDepthWeight", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(144, this.formatValue(144, this.settings.biomeDepthWeight), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(413, I18n.format("createWorld.customize.custom.biomeDepthOffset", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(145, this.formatValue(145, this.settings.biomeDepthOffset), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(414, I18n.format("createWorld.customize.custom.biomeScaleWeight", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(146, this.formatValue(146, this.settings.biomeScaleWeight), false, this.nonNegativeFloatValidator),
            new GuiPageButtonList.GuiLabelEntry(415, I18n.format("createWorld.customize.custom.biomeScaleOffset", new Object[0]) + ":", false),
            new GuiPageButtonList.EditBoxEntry(147, this.formatValue(147, this.settings.biomeScaleOffset), false, this.nonNegativeFloatValidator)};
        this.settingsList = new GuiPageButtonList(this.mc, this.width, this.height, 32, this.height - 32, 25, this, new GuiPageButtonList.GuiListEntry[][] {basicEntries, oreEntries, noiseSliderEntries, editBoxEntries});

        for (int i = 0; i < 4; ++i)
        {
            this.pageTitles[i] = I18n.format("createWorld.customize.custom.page" + i, new Object[0]);
        }

        this.updatePageButtons();
    }

    public String getSettingsJson()
    {
        return this.settings.toString().replace("\n", "");
    }

    public void loadSettingsJson(String settingsJson)
    {
        if (settingsJson != null && settingsJson.length() != 0)
        {
            this.settings = ChunkProviderSettings.Factory.jsonToFactory(settingsJson);
        }
        else
        {
            this.settings = new ChunkProviderSettings.Factory();
        }
    }

    public void setEntryValue(int id, String text)
    {
        float f = 0.0F;

        try
        {
            f = Float.parseFloat(text);
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            ;
        }

        float secondFloatValue = 0.0F;

        switch (id)
        {
            case 132:
                secondFloatValue = this.settings.mainNoiseScaleX = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 133:
                secondFloatValue = this.settings.mainNoiseScaleY = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 134:
                secondFloatValue = this.settings.mainNoiseScaleZ = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 135:
                secondFloatValue = this.settings.depthNoiseScaleX = MathHelper.clamp_float(f, 1.0F, 2000.0F);
                break;

            case 136:
                secondFloatValue = this.settings.depthNoiseScaleZ = MathHelper.clamp_float(f, 1.0F, 2000.0F);
                break;

            case 137:
                secondFloatValue = this.settings.depthNoiseScaleExponent = MathHelper.clamp_float(f, 0.01F, 20.0F);
                break;

            case 138:
                secondFloatValue = this.settings.baseSize = MathHelper.clamp_float(f, 1.0F, 25.0F);
                break;

            case 139:
                secondFloatValue = this.settings.coordinateScale = MathHelper.clamp_float(f, 1.0F, 6000.0F);
                break;

            case 140:
                secondFloatValue = this.settings.heightScale = MathHelper.clamp_float(f, 1.0F, 6000.0F);
                break;

            case 141:
                secondFloatValue = this.settings.stretchY = MathHelper.clamp_float(f, 0.01F, 50.0F);
                break;

            case 142:
                secondFloatValue = this.settings.upperLimitScale = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 143:
                secondFloatValue = this.settings.lowerLimitScale = MathHelper.clamp_float(f, 1.0F, 5000.0F);
                break;

            case 144:
                secondFloatValue = this.settings.biomeDepthWeight = MathHelper.clamp_float(f, 1.0F, 20.0F);
                break;

            case 145:
                secondFloatValue = this.settings.biomeDepthOffset = MathHelper.clamp_float(f, 0.0F, 20.0F);
                break;

            case 146:
                secondFloatValue = this.settings.biomeScaleWeight = MathHelper.clamp_float(f, 1.0F, 20.0F);
                break;

            case 147:
                secondFloatValue = this.settings.biomeScaleOffset = MathHelper.clamp_float(f, 0.0F, 20.0F);
        }

        if (secondFloatValue != f && f != 0.0F)
        {
            ((GuiTextField)this.settingsList.getControlById(id)).setText(this.formatValue(id, secondFloatValue));
        }

        ((GuiSlider)this.settingsList.getControlById(id - 132 + 100)).setSliderValue(secondFloatValue, false);

        if (!this.settings.equals(this.defaultSettings))
        {
            this.setChanged(true);
        }
    }

    private void setChanged(boolean changed)
    {
        this.hasChanged = changed;
        this.defaultsButton.enabled = changed;
    }

    public String getText(int id, String name, float value)
    {
        return name + ": " + this.formatValue(id, value);
    }

    private String formatValue(int id, float value)
    {
        switch (id)
        {
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 107:
            case 108:
            case 110:
            case 111:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 139:
            case 140:
            case 142:
            case 143:
                return String.format(Locale.ROOT, "%5.3f", Float.valueOf(value));

            case 105:
            case 106:
            case 109:
            case 112:
            case 113:
            case 114:
            case 115:
            case 137:
            case 138:
            case 141:
            case 144:
            case 145:
            case 146:
            case 147:
                return String.format(Locale.ROOT, "%2.3f", Float.valueOf(value));

            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            default:
                return Integer.toString((int)value);

            case 162:
                if (value < 0.0F)
                {
                    return I18n.format("gui.all", new Object[0]);
                }
                else if ((int)value >= BiomeGenBase.hell.biomeID)
                {
                    BiomeGenBase biomegenbase1 = BiomeGenBase.getBiomeGenArray()[(int)value + 2];
                    return biomegenbase1 != null ? biomegenbase1.biomeName : "?";
                }
                else
                {
                    BiomeGenBase biomegenbase = BiomeGenBase.getBiomeGenArray()[(int)value];
                    return biomegenbase != null ? biomegenbase.biomeName : "?";
                }
        }
    }

    public void setEntryValue(int id, boolean value)
    {
        switch (id)
        {
            case 148:
                this.settings.useCaves = value;
                break;

            case 149:
                this.settings.useDungeons = value;
                break;

            case 150:
                this.settings.useStrongholds = value;
                break;

            case 151:
                this.settings.useVillages = value;
                break;

            case 152:
                this.settings.useMineShafts = value;
                break;

            case 153:
                this.settings.useTemples = value;
                break;

            case 154:
                this.settings.useRavines = value;
                break;

            case 155:
                this.settings.useWaterLakes = value;
                break;

            case 156:
                this.settings.useLavaLakes = value;
                break;

            case 161:
                this.settings.useLavaOceans = value;
                break;

            case 210:
                this.settings.useMonuments = value;
        }

        if (!this.settings.equals(this.defaultSettings))
        {
            this.setChanged(true);
        }
    }

    public void onTick(int id, float value)
    {
        switch (id)
        {
            case 100:
                this.settings.mainNoiseScaleX = value;
                break;

            case 101:
                this.settings.mainNoiseScaleY = value;
                break;

            case 102:
                this.settings.mainNoiseScaleZ = value;
                break;

            case 103:
                this.settings.depthNoiseScaleX = value;
                break;

            case 104:
                this.settings.depthNoiseScaleZ = value;
                break;

            case 105:
                this.settings.depthNoiseScaleExponent = value;
                break;

            case 106:
                this.settings.baseSize = value;
                break;

            case 107:
                this.settings.coordinateScale = value;
                break;

            case 108:
                this.settings.heightScale = value;
                break;

            case 109:
                this.settings.stretchY = value;
                break;

            case 110:
                this.settings.upperLimitScale = value;
                break;

            case 111:
                this.settings.lowerLimitScale = value;
                break;

            case 112:
                this.settings.biomeDepthWeight = value;
                break;

            case 113:
                this.settings.biomeDepthOffset = value;
                break;

            case 114:
                this.settings.biomeScaleWeight = value;
                break;

            case 115:
                this.settings.biomeScaleOffset = value;

            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 161:
            case 188:
            default:
                break;

            case 157:
                this.settings.dungeonChance = (int)value;
                break;

            case 158:
                this.settings.waterLakeChance = (int)value;
                break;

            case 159:
                this.settings.lavaLakeChance = (int)value;
                break;

            case 160:
                this.settings.seaLevel = (int)value;
                break;

            case 162:
                this.settings.fixedBiome = (int)value;
                break;

            case 163:
                this.settings.biomeSize = (int)value;
                break;

            case 164:
                this.settings.riverSize = (int)value;
                break;

            case 165:
                this.settings.dirtSize = (int)value;
                break;

            case 166:
                this.settings.dirtCount = (int)value;
                break;

            case 167:
                this.settings.dirtMinHeight = (int)value;
                break;

            case 168:
                this.settings.dirtMaxHeight = (int)value;
                break;

            case 169:
                this.settings.gravelSize = (int)value;
                break;

            case 170:
                this.settings.gravelCount = (int)value;
                break;

            case 171:
                this.settings.gravelMinHeight = (int)value;
                break;

            case 172:
                this.settings.gravelMaxHeight = (int)value;
                break;

            case 173:
                this.settings.graniteSize = (int)value;
                break;

            case 174:
                this.settings.graniteCount = (int)value;
                break;

            case 175:
                this.settings.graniteMinHeight = (int)value;
                break;

            case 176:
                this.settings.graniteMaxHeight = (int)value;
                break;

            case 177:
                this.settings.dioriteSize = (int)value;
                break;

            case 178:
                this.settings.dioriteCount = (int)value;
                break;

            case 179:
                this.settings.dioriteMinHeight = (int)value;
                break;

            case 180:
                this.settings.dioriteMaxHeight = (int)value;
                break;

            case 181:
                this.settings.andesiteSize = (int)value;
                break;

            case 182:
                this.settings.andesiteCount = (int)value;
                break;

            case 183:
                this.settings.andesiteMinHeight = (int)value;
                break;

            case 184:
                this.settings.andesiteMaxHeight = (int)value;
                break;

            case 185:
                this.settings.coalSize = (int)value;
                break;

            case 186:
                this.settings.coalCount = (int)value;
                break;

            case 187:
                this.settings.coalMinHeight = (int)value;
                break;

            case 189:
                this.settings.coalMaxHeight = (int)value;
                break;

            case 190:
                this.settings.ironSize = (int)value;
                break;

            case 191:
                this.settings.ironCount = (int)value;
                break;

            case 192:
                this.settings.ironMinHeight = (int)value;
                break;

            case 193:
                this.settings.ironMaxHeight = (int)value;
                break;

            case 194:
                this.settings.goldSize = (int)value;
                break;

            case 195:
                this.settings.goldCount = (int)value;
                break;

            case 196:
                this.settings.goldMinHeight = (int)value;
                break;

            case 197:
                this.settings.goldMaxHeight = (int)value;
                break;

            case 198:
                this.settings.redstoneSize = (int)value;
                break;

            case 199:
                this.settings.redstoneCount = (int)value;
                break;

            case 200:
                this.settings.redstoneMinHeight = (int)value;
                break;

            case 201:
                this.settings.redstoneMaxHeight = (int)value;
                break;

            case 202:
                this.settings.diamondSize = (int)value;
                break;

            case 203:
                this.settings.diamondCount = (int)value;
                break;

            case 204:
                this.settings.diamondMinHeight = (int)value;
                break;

            case 205:
                this.settings.diamondMaxHeight = (int)value;
                break;

            case 206:
                this.settings.lapisSize = (int)value;
                break;

            case 207:
                this.settings.lapisCount = (int)value;
                break;

            case 208:
                this.settings.lapisCenterHeight = (int)value;
                break;

            case 209:
                this.settings.lapisSpread = (int)value;
        }

        if (id >= 100 && id < 116)
        {
            Gui gui = this.settingsList.getControlById(id - 100 + 132);

            if (gui != null)
            {
                ((GuiTextField)gui).setText(this.formatValue(id, value));
            }
        }

        if (!this.settings.equals(this.defaultSettings))
        {
            this.setChanged(true);
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id)
            {
                case 300:
                    this.parentScreen.chunkProviderSettingsJson = this.settings.toString();
                    this.mc.displayGuiScreen(this.parentScreen);
                    break;

                case 301:
                    for (int i = 0; i < this.settingsList.getSize(); ++i)
                    {
                        GuiPageButtonList.GuiEntry guiEntry = this.settingsList.getListEntry(i);
                        Gui gui = guiEntry.getLeftControl();

                        if (gui instanceof GuiButton)
                        {
                            GuiButton guibutton = (GuiButton)gui;

                            if (guibutton instanceof GuiSlider)
                            {
                                float f = ((GuiSlider)guibutton).getSliderPosition() * (0.75F + this.random.nextFloat() * 0.5F) + (this.random.nextFloat() * 0.1F - 0.05F);
                                ((GuiSlider)guibutton).setSliderPosition(MathHelper.clamp_float(f, 0.0F, 1.0F));
                            }
                            else if (guibutton instanceof GuiListButton)
                            {
                                ((GuiListButton)guibutton).setValue(this.random.nextBoolean());
                            }
                        }

                        Gui secondGui = guiEntry.getRightControl();

                        if (secondGui instanceof GuiButton)
                        {
                            GuiButton guibutton1 = (GuiButton)secondGui;

                            if (guibutton1 instanceof GuiSlider)
                            {
                                float thirdFloatValue = ((GuiSlider)guibutton1).getSliderPosition() * (0.75F + this.random.nextFloat() * 0.5F) + (this.random.nextFloat() * 0.1F - 0.05F);
                                ((GuiSlider)guibutton1).setSliderPosition(MathHelper.clamp_float(thirdFloatValue, 0.0F, 1.0F));
                            }
                            else if (guibutton1 instanceof GuiListButton)
                            {
                                ((GuiListButton)guibutton1).setValue(this.random.nextBoolean());
                            }
                        }
                    }

                    return;

                case 302:
                    this.settingsList.previousPage();
                    this.updatePageButtons();
                    break;

                case 303:
                    this.settingsList.nextPage();
                    this.updatePageButtons();
                    break;

                case 304:
                    if (this.hasChanged)
                    {
                        this.showConfirmation(304);
                    }

                    break;

                case 305:
                    this.mc.displayGuiScreen(new GuiScreenCustomizePresets(this));
                    break;

                case 306:
                    this.handleConfirmation();
                    break;

                case 307:
                    this.pendingConfirmationButtonId = 0;
                    this.handleConfirmation();
            }
        }
    }

    private void resetToDefaults()
    {
        this.settings.resetDefaults();
        this.createSettingsList();
        this.setChanged(false);
    }

    private void showConfirmation(int buttonId)
    {
        this.pendingConfirmationButtonId = buttonId;
        this.setConfirmationVisible(true);
    }

    private void handleConfirmation() throws IOException
    {
        switch (this.pendingConfirmationButtonId)
        {
            case 300:
                this.actionPerformed((GuiListButton)this.settingsList.getControlById(300));
                break;

            case 304:
                this.resetToDefaults();
        }

        this.pendingConfirmationButtonId = 0;
        this.ignoreNextMouseRelease = true;
        this.setConfirmationVisible(false);
    }

    private void setConfirmationVisible(boolean visible)
    {
        this.confirmYesButton.visible = visible;
        this.confirmNoButton.visible = visible;
        this.randomizeButton.enabled = !visible;
        this.doneButton.enabled = !visible;
        this.previousPageButton.enabled = !visible;
        this.nextPageButton.enabled = !visible;
        this.defaultsButton.enabled = this.hasChanged && !visible;
        this.presetsButton.enabled = !visible;
        this.settingsList.setAllButtonsEnabled(!visible);
    }

    private void updatePageButtons()
    {
        this.previousPageButton.enabled = this.settingsList.getCurrentPage() != 0;
        this.nextPageButton.enabled = this.settingsList.getCurrentPage() != this.settingsList.getPageCount() - 1;
        this.pageIndicatorText = I18n.format("book.pageIndicator", new Object[] {Integer.valueOf(this.settingsList.getCurrentPage() + 1), Integer.valueOf(this.settingsList.getPageCount())});
        this.currentPageTitle = this.pageTitles[this.settingsList.getCurrentPage()];
        this.randomizeButton.enabled = this.settingsList.getCurrentPage() != this.settingsList.getPageCount() - 1;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (this.pendingConfirmationButtonId == 0)
        {
            switch (keyCode)
            {
                case 200:
                    this.adjustFocusedTextField(1.0F);
                    break;

                case 208:
                    this.adjustFocusedTextField(-1.0F);
                    break;

                default:
                    this.settingsList.keyTyped(typedChar, keyCode);
            }
        }
    }

    private void adjustFocusedTextField(float amount)
    {
        Gui gui = this.settingsList.getFocusedControl();

        if (gui instanceof GuiTextField)
        {
            float f = amount;

            if (GuiScreen.isShiftKeyDown())
            {
                f = amount * 0.1F;

                if (GuiScreen.isCtrlKeyDown())
                {
                    f *= 0.1F;
                }
            }
            else if (GuiScreen.isCtrlKeyDown())
            {
                f = amount * 10.0F;

                if (GuiScreen.isAltKeyDown())
                {
                    f *= 10.0F;
                }
            }

            GuiTextField guitextfield = (GuiTextField)gui;
            Float secondFloat = Floats.tryParse(guitextfield.getText());

            if (secondFloat != null)
            {
                secondFloat = Float.valueOf(secondFloat.floatValue() + f);
                int i = guitextfield.getId();
                String s = this.formatValue(guitextfield.getId(), secondFloat.floatValue());
                guitextfield.setText(s);
                this.setEntryValue(i, s);
            }
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.pendingConfirmationButtonId == 0 && !this.ignoreNextMouseRelease)
        {
            this.settingsList.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.ignoreNextMouseRelease)
        {
            this.ignoreNextMouseRelease = false;
        }
        else if (this.pendingConfirmationButtonId == 0)
        {
            this.settingsList.mouseReleased(mouseX, mouseY, state);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.settingsList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 2, 16777215);
        this.drawCenteredString(this.fontRendererObj, this.pageIndicatorText, this.width / 2, 12, 16777215);
        this.drawCenteredString(this.fontRendererObj, this.currentPageTitle, this.width / 2, 22, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.pendingConfirmationButtonId != 0)
        {
            drawRect(0, 0, this.width, this.height, Integer.MIN_VALUE);
            this.drawHorizontalLine(this.width / 2 - 91, this.width / 2 + 90, 99, -2039584);
            this.drawHorizontalLine(this.width / 2 - 91, this.width / 2 + 90, 185, -6250336);
            this.drawVerticalLine(this.width / 2 - 91, 99, 185, -2039584);
            this.drawVerticalLine(this.width / 2 + 90, 99, 185, -6250336);
            float f = 85.0F;
            float floatValue2 = 180.0F;
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            this.mc.getTextureManager().bindTexture(optionsBackground);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float floatValue3 = 32.0F;
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldRenderer.pos((double)(this.width / 2 - 90), 185.0D, 0.0D).tex(0.0D, 2.65625D).color(64, 64, 64, 64).endVertex();
            worldRenderer.pos((double)(this.width / 2 + 90), 185.0D, 0.0D).tex(5.625D, 2.65625D).color(64, 64, 64, 64).endVertex();
            worldRenderer.pos((double)(this.width / 2 + 90), 100.0D, 0.0D).tex(5.625D, 0.0D).color(64, 64, 64, 64).endVertex();
            worldRenderer.pos((double)(this.width / 2 - 90), 100.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 64).endVertex();
            tessellator.draw();
            this.drawCenteredString(this.fontRendererObj, I18n.format("createWorld.customize.custom.confirmTitle", new Object[0]), this.width / 2, 105, 16777215);
            this.drawCenteredString(this.fontRendererObj, I18n.format("createWorld.customize.custom.confirm1", new Object[0]), this.width / 2, 125, 16777215);
            this.drawCenteredString(this.fontRendererObj, I18n.format("createWorld.customize.custom.confirm2", new Object[0]), this.width / 2, 135, 16777215);
            this.confirmYesButton.drawButton(this.mc, mouseX, mouseY);
            this.confirmNoButton.drawButton(this.mc, mouseX, mouseY);
        }
    }
}
