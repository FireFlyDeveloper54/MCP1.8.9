package net.optifine.shaders.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.optifine.Lang;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderOptionProfile;
import net.optifine.shaders.config.ShaderOptionScreen;

public class GuiShaderOptions extends GuiScreenOF
{
    private GuiScreen prevScreen;
    protected String title;
    private GameSettings settings;
    private TooltipManager tooltipManager;
    private String screenName;
    private String screenText;
    private boolean changed;
    public static final String OPTION_PROFILE = "<profile>";
    public static final String OPTION_EMPTY = "<empty>";
    public static final String OPTION_REST = "*";

    public GuiShaderOptions(GuiScreen guiScreen, GameSettings gameSettings)
    {
        this.tooltipManager = new TooltipManager(this, new TooltipProviderShaderOptions());
        this.screenName = null;
        this.screenText = null;
        this.changed = false;
        this.title = "Shader Options";
        this.prevScreen = guiScreen;
        this.settings = gameSettings;
    }

    public GuiShaderOptions(GuiScreen guiScreen, GameSettings gameSettings, String screenName)
    {
        this(guiScreen, gameSettings);
        this.screenName = screenName;

        if (screenName != null)
        {
            this.screenText = Shaders.translate("screen." + screenName, screenName);
        }
    }

    public void initGui()
    {
        this.title = I18n.format("of.options.shaderOptionsTitle", new Object[0]);
        int buttonIdBase = 100;
        int gridLeft = 0;
        int topY = 30;
        int rowHeight = 20;
        int bottomButtonWidth = 120;
        int buttonHeight = 20;
        int columnCount = Shaders.getShaderPackColumns(this.screenName, 2);
        ShaderOption[] shaderOptions = Shaders.getShaderPackOptions(this.screenName);

        if (shaderOptions != null)
        {
            int minColumnCount = MathHelper.ceiling_double_int((double)shaderOptions.length / 9.0D);

            if (columnCount < minColumnCount)
            {
                columnCount = minColumnCount;
            }

            for (int optionIndex = 0; optionIndex < shaderOptions.length; ++optionIndex)
            {
                ShaderOption shaderOption = shaderOptions[optionIndex];

                if (shaderOption != null && shaderOption.isVisible())
                {
                    int column = optionIndex % columnCount;
                    int row = optionIndex / columnCount;
                    int optionWidth = Math.min(this.width / columnCount, 200);
                    gridLeft = (this.width - optionWidth * columnCount) / 2;
                    int buttonX = column * optionWidth + 5 + gridLeft;
                    int buttonY = topY + row * rowHeight;
                    int buttonWidth = optionWidth - 10;
                    String buttonText = getButtonText(shaderOption, buttonWidth);
                    GuiButtonShaderOption optionButton;

                    if (Shaders.isShaderPackOptionSlider(shaderOption.getName()))
                    {
                        optionButton = new GuiSliderShaderOption(buttonIdBase + optionIndex, buttonX, buttonY, buttonWidth, buttonHeight, shaderOption, buttonText);
                    }
                    else
                    {
                        optionButton = new GuiButtonShaderOption(buttonIdBase + optionIndex, buttonX, buttonY, buttonWidth, buttonHeight, shaderOption, buttonText);
                    }

                    optionButton.enabled = shaderOption.isEnabled();
                    this.buttonList.add(optionButton);
                }
            }
        }

        this.buttonList.add(new GuiButton(201, this.width / 2 - bottomButtonWidth - 20, this.height / 6 + 168 + 11, bottomButtonWidth, buttonHeight, I18n.format("controls.reset", new Object[0])));
        this.buttonList.add(new GuiButton(200, this.width / 2 + 20, this.height / 6 + 168 + 11, bottomButtonWidth, buttonHeight, I18n.format("gui.done", new Object[0])));
    }

    public static String getButtonText(ShaderOption shaderOption, int btnWidth)
    {
        String label = shaderOption.getNameText();

        if (shaderOption instanceof ShaderOptionScreen)
        {
            ShaderOptionScreen shaderOptionScreen = (ShaderOptionScreen)shaderOption;
            return label + "...";
        }
        else
        {
            FontRenderer fontRenderer = Config.getMinecraft().fontRendererObj;

            for (int suffixWidth = fontRenderer.getStringWidth(": " + Lang.getOff()) + 5; fontRenderer.getStringWidth(label) + suffixWidth >= btnWidth && label.length() > 0; label = label.substring(0, label.length() - 1))
            {
                ;
            }

            String valueColor = shaderOption.isChanged() ? shaderOption.getValueColor(shaderOption.getValue()) : "";
            String valueText = shaderOption.getValueText(shaderOption.getValue());
            return label + ": " + valueColor + valueText;
        }
    }

    protected void actionPerformed(GuiButton guiButton)
    {
        if (guiButton.enabled)
        {
            if (guiButton.id < 200 && guiButton instanceof GuiButtonShaderOption)
            {
                GuiButtonShaderOption optionButton = (GuiButtonShaderOption)guiButton;
                ShaderOption shaderOption = optionButton.getShaderOption();

                if (shaderOption instanceof ShaderOptionScreen)
                {
                    String screenName = shaderOption.getName();
                    GuiShaderOptions shaderOptionsScreen = new GuiShaderOptions(this, this.settings, screenName);
                    this.mc.displayGuiScreen(shaderOptionsScreen);
                    return;
                }

                if (isShiftKeyDown())
                {
                    shaderOption.resetValue();
                }
                else if (optionButton.isSwitchable())
                {
                    shaderOption.nextValue();
                }

                this.updateAllButtons();
                this.changed = true;
            }

            if (guiButton.id == 201)
            {
                ShaderOption[] changedOptions = Shaders.getChangedOptions(Shaders.getShaderPackOptions());

                for (int optionIndex = 0; optionIndex < changedOptions.length; ++optionIndex)
                {
                    ShaderOption changedOption = changedOptions[optionIndex];
                    changedOption.resetValue();
                    this.changed = true;
                }

                this.updateAllButtons();
            }

            if (guiButton.id == 200)
            {
                if (this.changed)
                {
                    Shaders.saveShaderPackOptions();
                    this.changed = false;
                    Shaders.uninit();
                }

                this.mc.displayGuiScreen(this.prevScreen);
            }
        }
    }

    protected void actionPerformedRightClick(GuiButton btn)
    {
        if (btn instanceof GuiButtonShaderOption)
        {
            GuiButtonShaderOption optionButton = (GuiButtonShaderOption)btn;
            ShaderOption shaderOption = optionButton.getShaderOption();

            if (isShiftKeyDown())
            {
                shaderOption.resetValue();
            }
            else if (optionButton.isSwitchable())
            {
                shaderOption.prevValue();
            }

            this.updateAllButtons();
            this.changed = true;
        }
    }

    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (this.changed)
        {
            Shaders.saveShaderPackOptions();
            this.changed = false;
            Shaders.uninit();
        }
    }

    private void updateAllButtons()
    {
        for (GuiButton guiButton : this.buttonList)
        {
            if (guiButton instanceof GuiButtonShaderOption)
            {
                GuiButtonShaderOption optionButton = (GuiButtonShaderOption)guiButton;
                ShaderOption shaderOption = optionButton.getShaderOption();

                if (shaderOption instanceof ShaderOptionProfile)
                {
                    ShaderOptionProfile shaderOptionProfile = (ShaderOptionProfile)shaderOption;
                    shaderOptionProfile.updateProfile();
                }

                optionButton.displayString = getButtonText(shaderOption, optionButton.getButtonWidth());
                optionButton.valueChanged();
            }
        }
    }

    public void drawScreen(int x, int y, float partialTicks)
    {
        this.drawDefaultBackground();

        if (this.screenText != null)
        {
            this.drawCenteredString(this.fontRendererObj, this.screenText, this.width / 2, 15, 16777215);
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        }

        super.drawScreen(x, y, partialTicks);
        this.tooltipManager.drawTooltips(x, y, this.buttonList);
    }
}
