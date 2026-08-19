package net.optifine.shaders;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class HFNoiseTexture implements ICustomTexture
{
    private int texID = GL11.glGenTextures();
    private int textureUnit = 15;

    public HFNoiseTexture(int width, int height)
    {
        byte[] noisePixels = this.genHFNoiseImage(width, height);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(noisePixels.length);
        byteBuffer.put(noisePixels);
        byteBuffer.flip();
        GlStateManager.bindTexture(this.texID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)byteBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.bindTexture(0);
    }

    public int getID()
    {
        return this.texID;
    }

    public void deleteTexture()
    {
        GlStateManager.deleteTexture(this.texID);
        this.texID = 0;
    }

    private int random(int seed)
    {
        seed = seed ^ seed << 13;
        seed = seed ^ seed >> 17;
        seed = seed ^ seed << 5;
        return seed;
    }

    private byte random(int x, int y, int z)
    {
        int seed = (this.random(x) + this.random(y * 19)) * this.random(z * 23) - z;
        return (byte)(this.random(seed) % 128);
    }

    private byte[] genHFNoiseImage(int width, int height)
    {
        byte[] noisePixels = new byte[width * height * 3];
        int pixelIndex = 0;

        for (int yIndex = 0; yIndex < height; ++yIndex)
        {
            for (int xIndex = 0; xIndex < width; ++xIndex)
            {
                for (int channelIndex = 1; channelIndex < 4; ++channelIndex)
                {
                    noisePixels[pixelIndex++] = this.random(xIndex, yIndex, channelIndex);
                }
            }
        }

        return noisePixels;
    }

    public int getTextureId()
    {
        return this.texID;
    }

    public int getTextureUnit()
    {
        return this.textureUnit;
    }

    public int getTarget()
    {
        return 3553;
    }
}
