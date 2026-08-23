package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLAllocation
{
    private static int nextDisplayList = 1;

    public static synchronized int generateDisplayLists(int range)
    {
        int displayList = nextDisplayList;
        nextDisplayList += Math.max(range, 1);
        return displayList;
    }

    public static synchronized void deleteDisplayLists(int list, int range)
    {
    }

    public static synchronized void deleteDisplayLists(int list)
    {
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
