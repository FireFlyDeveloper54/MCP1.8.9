package net.minecraft.client;

import java.awt.Desktop;
import java.net.URI;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public final class GlUtil
{
    private static final float[] IDENTITY = new float[] {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
    private static final float[] in = new float[4];
    private static final float[] out = new float[4];
    private static final float[] inverse = new float[16];
    private static final float[] temp = new float[16];

    private GlUtil()
    {
    }

    public static String getVersion()
    {
        return Version.getVersion();
    }

    public static long getTimeMillis()
    {
        long frequency = GLFW.glfwGetTimerFrequency();

        if (frequency == 0L)
        {
            return System.currentTimeMillis();
        }

        return GLFW.glfwGetTimerValue() * 1000L / frequency;
    }

    public static boolean openURL(String url)
    {
        try
        {
            if (Desktop.isDesktopSupported())
            {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        }
        catch (Exception ignored)
        {
        }

        return false;
    }

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar)
    {
        float ymax = zNear * (float)Math.tan(fovy * Math.PI / 360.0D);
        float ymin = -ymax;
        net.minecraft.client.renderer.GlStateManager.frustum((double)(ymin * aspect), (double)(ymax * aspect), (double)ymin, (double)ymax, (double)zNear, (double)zFar);
    }

    public static String gluErrorString(int error)
    {
        switch (error)
        {
            case GL11.GL_NO_ERROR:
                return "No error";
            case GL11.GL_INVALID_ENUM:
                return "Invalid enum";
            case GL11.GL_INVALID_VALUE:
                return "Invalid value";
            case GL11.GL_INVALID_OPERATION:
                return "Invalid operation";
            case GL11.GL_STACK_OVERFLOW:
                return "Stack overflow";
            case GL11.GL_STACK_UNDERFLOW:
                return "Stack underflow";
            case GL11.GL_OUT_OF_MEMORY:
                return "Out of memory";
            default:
                return "Unknown error";
        }
    }

    public static boolean gluUnProject(float winx, float winy, float winz, FloatBuffer model, FloatBuffer projection, IntBuffer viewport, FloatBuffer obj)
    {
        float[] a = new float[16];
        float[] b = new float[16];
        model.position(0);
        projection.position(0);
        model.get(a);
        projection.get(b);
        model.position(0);
        projection.position(0);
        multiply(b, a, temp);

        if (!invert(temp, inverse))
        {
            return false;
        }

        in[0] = (winx - (float)viewport.get(viewport.position() + 0)) / (float)viewport.get(viewport.position() + 2) * 2.0F - 1.0F;
        in[1] = (winy - (float)viewport.get(viewport.position() + 1)) / (float)viewport.get(viewport.position() + 3) * 2.0F - 1.0F;
        in[2] = 2.0F * winz - 1.0F;
        in[3] = 1.0F;
        transform(inverse, in, out);

        if (out[3] == 0.0F)
        {
            return false;
        }

        obj.put(0, out[0] / out[3]);
        obj.put(1, out[1] / out[3]);
        obj.put(2, out[2] / out[3]);
        return true;
    }

    private static void multiply(float[] a, float[] b, float[] result)
    {
        for (int i = 0; i < 4; ++i)
        {
            for (int j = 0; j < 4; ++j)
            {
                result[i * 4 + j] = a[i * 4] * b[j] + a[i * 4 + 1] * b[4 + j] + a[i * 4 + 2] * b[8 + j] + a[i * 4 + 3] * b[12 + j];
            }
        }
    }

    private static void transform(float[] m, float[] v, float[] result)
    {
        for (int i = 0; i < 4; ++i)
        {
            result[i] = v[0] * m[i] + v[1] * m[4 + i] + v[2] * m[8 + i] + v[3] * m[12 + i];
        }
    }

    private static boolean invert(float[] src, float[] inverseOut)
    {
        System.arraycopy(src, 0, temp, 0, 16);
        System.arraycopy(IDENTITY, 0, inverseOut, 0, 16);

        for (int i = 0; i < 4; ++i)
        {
            int swap = i;

            for (int j = i + 1; j < 4; ++j)
            {
                if (Math.abs(temp[j * 4 + i]) > Math.abs(temp[i * 4 + i]))
                {
                    swap = j;
                }
            }

            if (swap != i)
            {
                for (int k = 0; k < 4; ++k)
                {
                    float t = temp[i * 4 + k];
                    temp[i * 4 + k] = temp[swap * 4 + k];
                    temp[swap * 4 + k] = t;
                    t = inverseOut[i * 4 + k];
                    inverseOut[i * 4 + k] = inverseOut[swap * 4 + k];
                    inverseOut[swap * 4 + k] = t;
                }
            }

            if (temp[i * 4 + i] == 0.0F)
            {
                return false;
            }

            float t = temp[i * 4 + i];

            for (int k = 0; k < 4; ++k)
            {
                temp[i * 4 + k] /= t;
                inverseOut[i * 4 + k] /= t;
            }

            for (int j = 0; j < 4; ++j)
            {
                if (j != i)
                {
                    t = temp[j * 4 + i];

                    for (int k = 0; k < 4; ++k)
                    {
                        temp[j * 4 + k] -= temp[i * 4 + k] * t;
                        inverseOut[j * 4 + k] -= inverseOut[i * 4 + k] * t;
                    }
                }
            }
        }

        return true;
    }
}
