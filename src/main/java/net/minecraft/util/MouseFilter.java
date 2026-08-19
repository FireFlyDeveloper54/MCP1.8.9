package net.minecraft.util;

public class MouseFilter
{
    private float cumulativeInput;
    private float smoothedInput;
    private float smoothingDelta;

    public float smooth(float input, float factor)
    {
        this.cumulativeInput += input;
        input = (this.cumulativeInput - this.smoothedInput) * factor;
        this.smoothingDelta += (input - this.smoothingDelta) * 0.5F;

        if (input > 0.0F && input > this.smoothingDelta || input < 0.0F && input < this.smoothingDelta)
        {
            input = this.smoothingDelta;
        }

        this.smoothedInput += input;
        return input;
    }

    public void reset()
    {
        this.cumulativeInput = 0.0F;
        this.smoothedInput = 0.0F;
        this.smoothingDelta = 0.0F;
    }
}
