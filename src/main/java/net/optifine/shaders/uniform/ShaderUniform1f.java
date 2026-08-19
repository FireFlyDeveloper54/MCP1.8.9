package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform1f extends ShaderUniformBase
{
    private float[] programValues;
    private static final float VALUE_UNKNOWN = -3.4028235E38F;

    public ShaderUniform1f(String name)
    {
        super(name);
        this.resetValue();
    }

    public void setValue(float valueNew)
    {
        int programId = this.getProgram();
        float oldValue = this.programValues[programId];

        if (valueNew != oldValue)
        {
            this.programValues[programId] = valueNew;
            int uniformLocation = this.getLocation();

            if (uniformLocation >= 0)
            {
                ARBShaderObjects.glUniform1fARB(uniformLocation, valueNew);
                this.checkGLError();
            }
        }
    }

    public float getValue()
    {
        int programId = this.getProgram();
        float value = this.programValues[programId];
        return value;
    }

    protected void onProgramSet(int program)
    {
        if (program >= this.programValues.length)
        {
            float[] oldProgramValues = this.programValues;
            float[] newProgramValues = new float[program + 10];
            System.arraycopy(oldProgramValues, 0, newProgramValues, 0, oldProgramValues.length);

            for (int programIndex = oldProgramValues.length; programIndex < newProgramValues.length; ++programIndex)
            {
                newProgramValues[programIndex] = -3.4028235E38F;
            }

            this.programValues = newProgramValues;
        }
    }

    protected void resetValue()
    {
        this.programValues = new float[] { -3.4028235E38F};
    }
}
