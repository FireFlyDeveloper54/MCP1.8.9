package net.minecraft.client.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.OpenGlHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

public class ShaderUniform
{
    private static final Logger logger = LogManager.getLogger();
    private int uniformLocation;
    private final int uniformCount;
    private final int uniformType;
    private final IntBuffer uniformIntBuffer;
    private final FloatBuffer uniformFloatBuffer;
    private final String shaderName;
    private boolean dirty;
    private final ShaderManager shaderManager;

    public ShaderUniform(String name, int type, int count, ShaderManager manager)
    {
        this.shaderName = name;
        this.uniformCount = count;
        this.uniformType = type;
        this.shaderManager = manager;

        if (type <= 3)
        {
            this.uniformIntBuffer = BufferUtils.createIntBuffer(count);
            this.uniformFloatBuffer = null;
        }
        else
        {
            this.uniformIntBuffer = null;
            this.uniformFloatBuffer = BufferUtils.createFloatBuffer(count);
        }

        this.uniformLocation = -1;
        this.markDirty();
    }

    private void markDirty()
    {
        this.dirty = true;

        if (this.shaderManager != null)
        {
            this.shaderManager.markDirty();
        }
    }

    public static int parseType(String typeName)
    {
        int parsedType = -1;

        if (typeName.equals("int"))
        {
            parsedType = 0;
        }
        else if (typeName.equals("float"))
        {
            parsedType = 4;
        }
        else if (typeName.startsWith("matrix"))
        {
            if (typeName.endsWith("2x2"))
            {
                parsedType = 8;
            }
            else if (typeName.endsWith("3x3"))
            {
                parsedType = 9;
            }
            else if (typeName.endsWith("4x4"))
            {
                parsedType = 10;
            }
        }

        return parsedType;
    }

    public void setUniformLocation(int location)
    {
        this.uniformLocation = location;
    }

    public String getShaderName()
    {
        return this.shaderName;
    }

    public void set(float value)
    {
        this.uniformFloatBuffer.position(0);
        this.uniformFloatBuffer.put(0, value);
        this.markDirty();
    }

    public void set(float value1, float value2)
    {
        this.uniformFloatBuffer.position(0);
        this.uniformFloatBuffer.put(0, value1);
        this.uniformFloatBuffer.put(1, value2);
        this.markDirty();
    }

    public void set(float value1, float value2, float value3)
    {
        this.uniformFloatBuffer.position(0);
        this.uniformFloatBuffer.put(0, value1);
        this.uniformFloatBuffer.put(1, value2);
        this.uniformFloatBuffer.put(2, value3);
        this.markDirty();
    }

    public void set(float value1, float value2, float value3, float value4)
    {
        this.uniformFloatBuffer.position(0);
        this.uniformFloatBuffer.put(value1);
        this.uniformFloatBuffer.put(value2);
        this.uniformFloatBuffer.put(value3);
        this.uniformFloatBuffer.put(value4);
        this.uniformFloatBuffer.flip();
        this.markDirty();
    }

    public void setFloatValues(float value1, float value2, float value3, float value4)
    {
        this.uniformFloatBuffer.position(0);

        if (this.uniformType >= 4)
        {
            this.uniformFloatBuffer.put(0, value1);
        }

        if (this.uniformType >= 5)
        {
            this.uniformFloatBuffer.put(1, value2);
        }

        if (this.uniformType >= 6)
        {
            this.uniformFloatBuffer.put(2, value3);
        }

        if (this.uniformType >= 7)
        {
            this.uniformFloatBuffer.put(3, value4);
        }

        this.markDirty();
    }

    public void set(int value1, int value2, int value3, int value4)
    {
        this.uniformIntBuffer.position(0);

        if (this.uniformType >= 0)
        {
            this.uniformIntBuffer.put(0, value1);
        }

        if (this.uniformType >= 1)
        {
            this.uniformIntBuffer.put(1, value2);
        }

        if (this.uniformType >= 2)
        {
            this.uniformIntBuffer.put(2, value3);
        }

        if (this.uniformType >= 3)
        {
            this.uniformIntBuffer.put(3, value4);
        }

        this.markDirty();
    }

    public void set(float[] values)
    {
        if (values.length < this.uniformCount)
        {
            logger.warn("Uniform.set called with a too-small value array (expected " + this.uniformCount + ", got " + values.length + "). Ignoring.");
        }
        else
        {
            this.uniformFloatBuffer.position(0);
            this.uniformFloatBuffer.put(values);
            this.uniformFloatBuffer.position(0);
            this.markDirty();
        }
    }

    public void set(float matrix00, float matrix01, float matrix02, float matrix03, float matrix10, float matrix11, float matrix12, float matrix13, float matrix20, float matrix21, float matrix22, float matrix23, float matrix30, float matrix31, float matrix32, float matrix33)
    {
        this.uniformFloatBuffer.position(0);
        this.uniformFloatBuffer.put(0, matrix00);
        this.uniformFloatBuffer.put(1, matrix01);
        this.uniformFloatBuffer.put(2, matrix02);
        this.uniformFloatBuffer.put(3, matrix03);
        this.uniformFloatBuffer.put(4, matrix10);
        this.uniformFloatBuffer.put(5, matrix11);
        this.uniformFloatBuffer.put(6, matrix12);
        this.uniformFloatBuffer.put(7, matrix13);
        this.uniformFloatBuffer.put(8, matrix20);
        this.uniformFloatBuffer.put(9, matrix21);
        this.uniformFloatBuffer.put(10, matrix22);
        this.uniformFloatBuffer.put(11, matrix23);
        this.uniformFloatBuffer.put(12, matrix30);
        this.uniformFloatBuffer.put(13, matrix31);
        this.uniformFloatBuffer.put(14, matrix32);
        this.uniformFloatBuffer.put(15, matrix33);
        this.markDirty();
    }

    public void set(Matrix4f matrix)
    {
        this.set(matrix.m00, matrix.m01, matrix.m02, matrix.m03, matrix.m10, matrix.m11, matrix.m12, matrix.m13, matrix.m20, matrix.m21, matrix.m22, matrix.m23, matrix.m30, matrix.m31, matrix.m32, matrix.m33);
    }

    public void upload()
    {
        if (!this.dirty)
        {
            ;
        }

        this.dirty = false;

        if (this.uniformType <= 3)
        {
            this.uploadInt();
        }
        else if (this.uniformType <= 7)
        {
            this.uploadFloat();
        }
        else
        {
            if (this.uniformType > 10)
            {
                logger.warn("Uniform.upload called, but type value (" + this.uniformType + ") is not " + "a valid type. Ignoring.");
                return;
            }

            this.uploadFloatMatrix();
        }
    }

    private void uploadInt()
    {
        switch (this.uniformType)
        {
            case 0:
                OpenGlHelper.glUniform1(this.uniformLocation, this.uniformIntBuffer);
                break;

            case 1:
                OpenGlHelper.glUniform2(this.uniformLocation, this.uniformIntBuffer);
                break;

            case 2:
                OpenGlHelper.glUniform3(this.uniformLocation, this.uniformIntBuffer);
                break;

            case 3:
                OpenGlHelper.glUniform4(this.uniformLocation, this.uniformIntBuffer);
                break;

            default:
                logger.warn("Uniform.upload called, but count value (" + this.uniformCount + ") is " + " not in the range of 1 to 4. Ignoring.");
        }
    }

    private void uploadFloat()
    {
        switch (this.uniformType)
        {
            case 4:
                OpenGlHelper.glUniform1(this.uniformLocation, this.uniformFloatBuffer);
                break;

            case 5:
                OpenGlHelper.glUniform2(this.uniformLocation, this.uniformFloatBuffer);
                break;

            case 6:
                OpenGlHelper.glUniform3(this.uniformLocation, this.uniformFloatBuffer);
                break;

            case 7:
                OpenGlHelper.glUniform4(this.uniformLocation, this.uniformFloatBuffer);
                break;

            default:
                logger.warn("Uniform.upload called, but count value (" + this.uniformCount + ") is " + "not in the range of 1 to 4. Ignoring.");
        }
    }

    private void uploadFloatMatrix()
    {
        switch (this.uniformType)
        {
            case 8:
                OpenGlHelper.glUniformMatrix2(this.uniformLocation, true, this.uniformFloatBuffer);
                break;

            case 9:
                OpenGlHelper.glUniformMatrix3(this.uniformLocation, true, this.uniformFloatBuffer);
                break;

            case 10:
                OpenGlHelper.glUniformMatrix4(this.uniformLocation, true, this.uniformFloatBuffer);
        }
    }
}
