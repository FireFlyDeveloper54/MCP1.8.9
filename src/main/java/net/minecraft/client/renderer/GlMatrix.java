package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public final class GlMatrix
{
    public static final int MODELVIEW = 5888;
    public static final int PROJECTION = 5889;
    public static final int TEXTURE = 5890;
    public static final int MODELVIEW_MATRIX = 2982;
    public static final int PROJECTION_MATRIX = 2983;
    public static final int TEXTURE_MATRIX = 2984;

    private static final int STACK_LIMIT = 32;
    private static int mode = MODELVIEW;
    private static final ArrayList<float[]> modelView = new ArrayList<float[]>();
    private static final ArrayList<float[]> projection = new ArrayList<float[]>();
    private static final ArrayList<float[]> texture = new ArrayList<float[]>();
    private static final float[] multiplyTemp = new float[16];

    static
    {
        modelView.add(identity());
        projection.add(identity());
        texture.add(identity());
    }

    private GlMatrix()
    {
    }

    public static void matrixMode(int matrixMode)
    {
        if (matrixMode == MODELVIEW || matrixMode == PROJECTION || matrixMode == TEXTURE)
        {
            mode = matrixMode;
        }
    }

    public static void loadIdentity()
    {
        identity(current());
    }

    public static void pushMatrix()
    {
        ArrayList<float[]> stack = stack();

        if (stack.size() >= STACK_LIMIT)
        {
            return;
        }

        stack.add(copy(current()));
    }

    public static void popMatrix()
    {
        ArrayList<float[]> stack = stack();

        if (stack.size() > 1)
        {
            stack.remove(stack.size() - 1);
        }
    }

    public static void getFloat(int pname, FloatBuffer params)
    {
        float[] matrix = pname == PROJECTION_MATRIX ? top(projection) : (pname == TEXTURE_MATRIX ? top(texture) : top(modelView));
        params.mark();

        for (int i = 0; i < 16; ++i)
        {
            params.put(matrix[i]);
        }

        params.reset();
    }

    public static float[] getModelView()
    {
        return top(modelView);
    }

    public static float[] getProjection()
    {
        return top(projection);
    }

    public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        float[] ortho = identity();
        float invRl = (float)(1.0D / (right - left));
        float invTb = (float)(1.0D / (top - bottom));
        float invFn = (float)(1.0D / (zFar - zNear));
        ortho[0] = 2.0F * invRl;
        ortho[5] = 2.0F * invTb;
        ortho[10] = -2.0F * invFn;
        ortho[12] = (float)(-(right + left) * invRl);
        ortho[13] = (float)(-(top + bottom) * invTb);
        ortho[14] = (float)(-(zFar + zNear) * invFn);
        multiply(current(), ortho);
    }

    public static void frustum(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        float[] frustum = new float[16];
        float invRl = (float)(1.0D / (right - left));
        float invTb = (float)(1.0D / (top - bottom));
        float invFn = (float)(1.0D / (zFar - zNear));
        frustum[0] = (float)(2.0D * zNear * invRl);
        frustum[5] = (float)(2.0D * zNear * invTb);
        frustum[8] = (float)((right + left) * invRl);
        frustum[9] = (float)((top + bottom) * invTb);
        frustum[10] = (float)(-(zFar + zNear) * invFn);
        frustum[11] = -1.0F;
        frustum[14] = (float)(-2.0D * zFar * zNear * invFn);
        multiply(current(), frustum);
    }

    public static void translate(float x, float y, float z)
    {
        float[] current = current();
        current[12] += current[0] * x + current[4] * y + current[8] * z;
        current[13] += current[1] * x + current[5] * y + current[9] * z;
        current[14] += current[2] * x + current[6] * y + current[10] * z;
        current[15] += current[3] * x + current[7] * y + current[11] * z;
    }

    public static void scale(float x, float y, float z)
    {
        float[] current = current();

        for (int i = 0; i < 4; ++i)
        {
            current[i] *= x;
            current[4 + i] *= y;
            current[8 + i] *= z;
        }
    }

    public static void rotate(float angle, float x, float y, float z)
    {
        float len = (float)Math.sqrt((double)(x * x + y * y + z * z));

        if (len == 0.0F)
        {
            return;
        }

        x /= len;
        y /= len;
        z /= len;
        float radians = angle * (float)Math.PI / 180.0F;
        float c = (float)Math.cos((double)radians);
        float s = (float)Math.sin((double)radians);
        float c1 = 1.0F - c;
        float[] rotate = identity();
        rotate[0] = x * x * c1 + c;
        rotate[1] = y * x * c1 + z * s;
        rotate[2] = x * z * c1 - y * s;
        rotate[4] = x * y * c1 - z * s;
        rotate[5] = y * y * c1 + c;
        rotate[6] = y * z * c1 + x * s;
        rotate[8] = x * z * c1 + y * s;
        rotate[9] = y * z * c1 - x * s;
        rotate[10] = z * z * c1 + c;
        multiply(current(), rotate);
    }

    public static void multMatrix(FloatBuffer matrix)
    {
        float[] rhs = new float[16];
        int position = matrix.position();

        for (int i = 0; i < 16; ++i)
        {
            rhs[i] = matrix.get(position + i);
        }

        multiply(current(), rhs);
    }

    private static ArrayList<float[]> stack()
    {
        return mode == PROJECTION ? projection : (mode == TEXTURE ? texture : modelView);
    }

    private static float[] current()
    {
        return top(stack());
    }

    private static float[] top(ArrayList<float[]> stack)
    {
        return stack.get(stack.size() - 1);
    }

    private static float[] identity()
    {
        float[] matrix = new float[16];
        identity(matrix);
        return matrix;
    }

    private static void identity(float[] matrix)
    {
        for (int i = 0; i < 16; ++i)
        {
            matrix[i] = 0.0F;
        }

        matrix[0] = matrix[5] = matrix[10] = matrix[15] = 1.0F;
    }

    private static float[] copy(float[] source)
    {
        float[] copy = new float[16];
        System.arraycopy(source, 0, copy, 0, 16);
        return copy;
    }

    private static void multiply(float[] left, float[] right)
    {
        for (int column = 0; column < 4; ++column)
        {
            float r0 = right[column * 4];
            float r1 = right[column * 4 + 1];
            float r2 = right[column * 4 + 2];
            float r3 = right[column * 4 + 3];

            for (int row = 0; row < 4; ++row)
            {
                multiplyTemp[column * 4 + row] = left[row] * r0 + left[4 + row] * r1 + left[8 + row] * r2 + left[12 + row] * r3;
            }
        }

        System.arraycopy(multiplyTemp, 0, left, 0, 16);
    }
}
