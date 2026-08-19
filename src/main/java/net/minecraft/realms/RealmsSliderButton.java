package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class RealmsSliderButton extends RealmsButton
{
    public float value;
    public boolean sliding;
    private final float minValue;
    private final float maxValue;
    private int steps;

    public RealmsSliderButton(int id, int x, int y, int width, int value, int maxValue)
    {
        this(id, x, y, width, maxValue, 0, 1.0F, (float)value);
    }

    public RealmsSliderButton(int id, int x, int y, int width, int initialValue, int steps, float minValue, float maxValue)
    {
        super(id, x, y, width, 20, "");
        this.value = 1.0F;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.steps = steps;
        this.value = this.toPct((float)initialValue);
        this.getProxy().displayString = this.getMessage();
    }

    public String getMessage()
    {
        return "";
    }

    public float toPct(float value)
    {
        return MathHelper.clamp_float((this.clamp(value) - this.minValue) / (this.maxValue - this.minValue), 0.0F, 1.0F);
    }

    public float toValue(float percentage)
    {
        return this.clamp(this.minValue + (this.maxValue - this.minValue) * MathHelper.clamp_float(percentage, 0.0F, 1.0F));
    }

    public float clamp(float value)
    {
        value = this.clampSteps(value);
        return MathHelper.clamp_float(value, this.minValue, this.maxValue);
    }

    protected float clampSteps(float value)
    {
        if (this.steps > 0)
        {
            value = (float)(this.steps * Math.round(value / (float)this.steps));
        }

        return value;
    }

    public int getYImage(boolean mouseOver)
    {
        return 0;
    }

    public void renderBg(int mouseX, int mouseY)
    {
        if (this.getProxy().visible)
        {
            if (this.sliding)
            {
                this.value = (float)(mouseX - (this.getProxy().xPosition + 4)) / (float)(this.getProxy().getButtonWidth() - 8);
                this.value = MathHelper.clamp_float(this.value, 0.0F, 1.0F);
                float f = this.toValue(this.value);
                this.clicked(f);
                this.value = this.toPct(f);
                this.getProxy().displayString = this.getMessage();
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_LOCATION);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(this.getProxy().xPosition + (int)(this.value * (float)(this.getProxy().getButtonWidth() - 8)), this.getProxy().yPosition, 0, 66, 4, 20);
            this.blit(this.getProxy().xPosition + (int)(this.value * (float)(this.getProxy().getButtonWidth() - 8)) + 4, this.getProxy().yPosition, 196, 66, 4, 20);
        }
    }

    public void clicked(int mouseX, int mouseY)
    {
        this.value = (float)(mouseX - (this.getProxy().xPosition + 4)) / (float)(this.getProxy().getButtonWidth() - 8);
        this.value = MathHelper.clamp_float(this.value, 0.0F, 1.0F);
        this.clicked(this.toValue(this.value));
        this.getProxy().displayString = this.getMessage();
        this.sliding = true;
    }

    public void clicked(float value)
    {
    }

    public void released(int mouseX, int mouseY)
    {
        this.sliding = false;
    }
}
