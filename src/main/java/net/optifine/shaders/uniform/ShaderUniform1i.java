package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform1i extends ShaderUniformBase
{
    private int[] programValues;
    private static final int VALUE_UNKNOWN = Integer.MIN_VALUE;

    public ShaderUniform1i(String name)
    {
        super(name);
        this.resetValue();
    }

    public void setValue(int valueNew)
    {
        int programId = this.getProgram();
        int oldValue = this.programValues[programId];

        if (valueNew != oldValue)
        {
            this.programValues[programId] = valueNew;
            int uniformLocation = this.getLocation();

            if (uniformLocation >= 0)
            {
                ARBShaderObjects.glUniform1iARB(uniformLocation, valueNew);
                this.checkGLError();
            }
        }
    }

    public int getValue()
    {
        int programId = this.getProgram();
        int value = this.programValues[programId];
        return value;
    }

    protected void onProgramSet(int program)
    {
        if (program >= this.programValues.length)
        {
            int[] oldProgramValues = this.programValues;
            int[] newProgramValues = new int[program + 10];
            System.arraycopy(oldProgramValues, 0, newProgramValues, 0, oldProgramValues.length);

            for (int programIndex = oldProgramValues.length; programIndex < newProgramValues.length; ++programIndex)
            {
                newProgramValues[programIndex] = Integer.MIN_VALUE;
            }

            this.programValues = newProgramValues;
        }
    }

    protected void resetValue()
    {
        this.programValues = new int[] {Integer.MIN_VALUE};
    }
}
