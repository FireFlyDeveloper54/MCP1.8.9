package net.optifine.shaders.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.EnumShaderOption;

public class GuiButtonEnumShaderOption extends GuiButton
{
    private EnumShaderOption enumShaderOption = null;

    public GuiButtonEnumShaderOption(EnumShaderOption enumShaderOption, int x, int y, int widthIn, int heightIn)
    {
        super(enumShaderOption.ordinal(), x, y, widthIn, heightIn, getButtonText(enumShaderOption));
        this.enumShaderOption = enumShaderOption;
    }

    public EnumShaderOption getEnumShaderOption()
    {
        return this.enumShaderOption;
    }

    private static String getButtonText(EnumShaderOption eso)
    {
        String optionPrefix = I18n.format(eso.getResourceKey(), new Object[0]) + ": ";

        switch (eso)
        {
            case ANTIALIASING:
                return optionPrefix + GuiShaders.toStringAa(Shaders.configAntialiasingLevel);

            case NORMAL_MAP:
                return optionPrefix + GuiShaders.toStringOnOff(Shaders.configNormalMap);

            case SPECULAR_MAP:
                return optionPrefix + GuiShaders.toStringOnOff(Shaders.configSpecularMap);

            case RENDER_RES_MUL:
                return optionPrefix + GuiShaders.toStringQuality(Shaders.configRenderResMul);

            case SHADOW_RES_MUL:
                return optionPrefix + GuiShaders.toStringQuality(Shaders.configShadowResMul);

            case HAND_DEPTH_MUL:
                return optionPrefix + GuiShaders.toStringHandDepth(Shaders.configHandDepthMul);

            case CLOUD_SHADOW:
                return optionPrefix + GuiShaders.toStringOnOff(Shaders.configCloudShadow);

            case OLD_HAND_LIGHT:
                return optionPrefix + Shaders.configOldHandLight.getUserValue();

            case OLD_LIGHTING:
                return optionPrefix + Shaders.configOldLighting.getUserValue();

            case SHADOW_CLIP_FRUSTRUM:
                return optionPrefix + GuiShaders.toStringOnOff(Shaders.configShadowClipFrustrum);

            case TWEAK_BLOCK_DAMAGE:
                return optionPrefix + GuiShaders.toStringOnOff(Shaders.configTweakBlockDamage);

            default:
                return optionPrefix + Shaders.getEnumShaderOption(eso);
        }
    }

    public void updateButtonText()
    {
        this.displayString = getButtonText(this.enumShaderOption);
    }
}
