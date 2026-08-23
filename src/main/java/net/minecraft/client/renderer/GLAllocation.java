package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.GlUtil;

public class GLAllocation
{

    public static synchronized int generateDisplayLists(int range)
    {
        int displayList = GL11.glGenLists(range);

        if (displayList == 0)
        {
            int glErrorCode = GL11.glGetError();
            String glErrorMessage = "No error code reported";

            if (glErrorCode != 0)
            {
                glErrorMessage = GlUtil.gluErrorString(glErrorCode);
            }

            throw new IllegalStateException("glGenLists returned an ID of 0 for a count of " + range + ", GL error (" + glErrorCode + "): " + glErrorMessage);
        }
        else
        {
            return displayList;
        }
    }

    public static synchronized void deleteDisplayLists(int list, int range)
    {
        GL11.glDeleteLists(list, range);
    }

    public static synchronized void deleteDisplayLists(int list)
    {
        GL11.glDeleteLists(list, 1);
    }

    public static synchronized ByteBuffer createDirectByteBuffer(int capacity)
    {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    public static IntBuffer createDirectIntBuffer(int capacity)
    {
        return createDirectByteBuffer(capacity << 2).asIntBuffer();
    }

    public static FloatBuffer createDirectFloatBuffer(int capacity)
    {
        return createDirectByteBuffer(capacity << 2).asFloatBuffer();
    }
}
