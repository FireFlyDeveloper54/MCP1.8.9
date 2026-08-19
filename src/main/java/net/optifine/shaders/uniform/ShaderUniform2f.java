package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform2f extends ShaderUniformBase
{
    private float[][] programValues;
    private static final float VALUE_UNKNOWN = -3.4028235E38F;

    public ShaderUniform2f(String name)
    {
        super(name);
        this.resetValue();
    }

    public void setValue(float x, float y)
    {
        int programId = this.getProgram();
        float[] values = this.programValues[programId];

        if (values[0] != x || values[1] != y)
        {
            values[0] = x;
            values[1] = y;
            int uniformLocation = this.getLocation();

            if (uniformLocation >= 0)
            {
                ARBShaderObjects.glUniform2fARB(uniformLocation, x, y);
                this.checkGLError();
            }
        }
    }

    public float[] getValue()
    {
        int programId = this.getProgram();
        float[] values = this.programValues[programId];
        return values;
    }

    protected void onProgramSet(int program)
    {
        if (program >= this.programValues.length)
        {
            float[][] oldProgramValues = this.programValues;
            float[][] newProgramValues = new float[program + 10][];
            System.arraycopy(oldProgramValues, 0, newProgramValues, 0, oldProgramValues.length);
            this.programValues = newProgramValues;
        }

        if (this.programValues[program] == null)
        {
            this.programValues[program] = new float[] { -3.4028235E38F, -3.4028235E38F};
        }
    }

    protected void resetValue()
    {
        this.programValues = new float[][] {{ -3.4028235E38F, -3.4028235E38F}};
    }
}
