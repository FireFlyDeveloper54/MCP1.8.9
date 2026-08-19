package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform2i extends ShaderUniformBase
{
    private int[][] programValues;
    private static final int VALUE_UNKNOWN = Integer.MIN_VALUE;

    public ShaderUniform2i(String name)
    {
        super(name);
        this.resetValue();
    }

    public void setValue(int x, int y)
    {
        int programId = this.getProgram();
        int[] values = this.programValues[programId];

        if (values[0] != x || values[1] != y)
        {
            values[0] = x;
            values[1] = y;
            int uniformLocation = this.getLocation();

            if (uniformLocation >= 0)
            {
                ARBShaderObjects.glUniform2iARB(uniformLocation, x, y);
                this.checkGLError();
            }
        }
    }

    public int[] getValue()
    {
        int programId = this.getProgram();
        int[] values = this.programValues[programId];
        return values;
    }

    protected void onProgramSet(int program)
    {
        if (program >= this.programValues.length)
        {
            int[][] oldProgramValues = this.programValues;
            int[][] newProgramValues = new int[program + 10][];
            System.arraycopy(oldProgramValues, 0, newProgramValues, 0, oldProgramValues.length);
            this.programValues = newProgramValues;
        }

        if (this.programValues[program] == null)
        {
            this.programValues[program] = new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE};
        }
    }

    protected void resetValue()
    {
        this.programValues = new int[][] {{Integer.MIN_VALUE, Integer.MIN_VALUE}};
    }
}
