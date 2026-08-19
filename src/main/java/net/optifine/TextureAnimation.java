package net.optifine;

import java.nio.ByteBuffer;
import java.util.Properties;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;

public class TextureAnimation
{
    private String srcTex = null;
    private String dstTex = null;
    ResourceLocation dstTexLoc = null;
    private int dstTextId = -1;
    private int dstX = 0;
    private int dstY = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private TextureAnimationFrame[] frames = null;
    private int currentFrameIndex = 0;
    private boolean interpolate = false;
    private int interpolateSkip = 0;
    private ByteBuffer interpolateData = null;
    byte[] srcData = null;
    private ByteBuffer imageData = null;
    private boolean active = true;
    private boolean valid = true;

    public TextureAnimation(String texFrom, byte[] srcData, String texTo, ResourceLocation locTexTo, int dstX, int dstY, int frameWidth, int frameHeight, Properties props)
    {
        this.srcTex = texFrom;
        this.dstTex = texTo;
        this.dstTexLoc = locTexTo;
        this.dstX = dstX;
        this.dstY = dstY;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        int frameSize = frameWidth * frameHeight * 4;

        if (srcData.length % frameSize != 0)
        {
            Config.warn("Invalid animated texture length: " + srcData.length + ", frameWidth: " + frameWidth + ", frameHeight: " + frameHeight);
        }

        this.srcData = srcData;
        int frameCount = srcData.length / frameSize;

        if (props.get("tile.0") != null)
        {
            for (int tileIndex = 0; props.get("tile." + tileIndex) != null; ++tileIndex)
            {
                frameCount = tileIndex + 1;
            }
        }

        String defaultDurationText = (String)props.get("duration");
        int defaultDuration = Math.max(Config.parseInt(defaultDurationText, 1), 1);
        this.frames = new TextureAnimationFrame[frameCount];

        for (int frameIndex = 0; frameIndex < this.frames.length; ++frameIndex)
        {
            String tileText = (String)props.get("tile." + frameIndex);
            int tileIndex = Config.parseInt(tileText, frameIndex);
            String durationText = (String)props.get("duration." + frameIndex);
            int duration = Math.max(Config.parseInt(durationText, defaultDuration), 1);
            TextureAnimationFrame frame = new TextureAnimationFrame(tileIndex, duration);
            this.frames[frameIndex] = frame;
        }

        this.interpolate = Config.parseBoolean(props.getProperty("interpolate"), false);
        this.interpolateSkip = Config.parseInt(props.getProperty("skip"), 0);

        if (this.interpolate)
        {
            this.interpolateData = GLAllocation.createDirectByteBuffer(frameSize);
        }
    }

    public boolean nextFrame()
    {
        TextureAnimationFrame currentFrame = this.getCurrentFrame();

        if (currentFrame == null)
        {
            return false;
        }
        else
        {
            ++currentFrame.counter;

            if (currentFrame.counter < currentFrame.duration)
            {
                return this.interpolate;
            }
            else
            {
                currentFrame.counter = 0;
                ++this.currentFrameIndex;

                if (this.currentFrameIndex >= this.frames.length)
                {
                    this.currentFrameIndex = 0;
                }

                return true;
            }
        }
    }

    public TextureAnimationFrame getCurrentFrame()
    {
        return this.getFrame(this.currentFrameIndex);
    }

    public TextureAnimationFrame getFrame(int index)
    {
        if (this.frames.length <= 0)
        {
            return null;
        }
        else
        {
            if (index < 0 || index >= this.frames.length)
            {
                index = 0;
            }

            TextureAnimationFrame frame = this.frames[index];
            return frame;
        }
    }

    public int getFrameCount()
    {
        return this.frames.length;
    }

    public void updateTexture()
    {
        if (this.valid)
        {
            if (this.dstTextId < 0)
            {
                ITextureObject textureObject = TextureUtils.getTexture(this.dstTexLoc);

                if (textureObject == null)
                {
                    this.valid = false;
                    return;
                }

                this.dstTextId = textureObject.getGlTextureId();
            }

            if (this.imageData == null)
            {
                this.imageData = GLAllocation.createDirectByteBuffer(this.srcData.length);
                this.imageData.put(this.srcData);
                this.imageData.flip();
                this.srcData = null;
            }

            this.active = SmartAnimations.isActive() ? SmartAnimations.isTextureRendered(this.dstTextId) : true;

            if (this.nextFrame())
            {
                if (this.active)
                {
                    int frameSize = this.frameWidth * this.frameHeight * 4;
                    TextureAnimationFrame currentFrame = this.getCurrentFrame();

                    if (currentFrame != null)
                    {
                        int frameOffset = frameSize * currentFrame.index;

                        if (frameOffset + frameSize <= this.imageData.limit())
                        {
                            if (this.interpolate && currentFrame.counter > 0)
                            {
                                if (this.interpolateSkip <= 1 || currentFrame.counter % this.interpolateSkip == 0)
                                {
                                    TextureAnimationFrame nextFrame = this.getFrame(this.currentFrameIndex + 1);
                                    double blendFactor = 1.0D * (double)currentFrame.counter / (double)currentFrame.duration;
                                    this.updateTextureInerpolate(currentFrame, nextFrame, blendFactor);
                                }
                            }
                            else
                            {
                                this.imageData.position(frameOffset);
                                GlStateManager.bindTexture(this.dstTextId);
                                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, this.dstX, this.dstY, this.frameWidth, this.frameHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)this.imageData);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateTextureInerpolate(TextureAnimationFrame frame1, TextureAnimationFrame frame2, double blendFactor) {
        int frameSize = this.frameWidth * this.frameHeight * 4;
        int frame1Offset = frameSize * frame1.index;

        if (frame1Offset + frameSize <= this.imageData.limit())
        {
            int frame2Offset = frameSize * frame2.index;

            if (frame2Offset + frameSize <= this.imageData.limit())
            {
                this.interpolateData.clear();

                for (int byteIndex = 0; byteIndex < frameSize; ++byteIndex)
                {
                    int frame1Value = this.imageData.get(frame1Offset + byteIndex) & 255;
                    int frame2Value = this.imageData.get(frame2Offset + byteIndex) & 255;
                    int blendedValue = this.mix(frame1Value, frame2Value, blendFactor);
                    byte blendedByte = (byte)blendedValue;
                    this.interpolateData.put(blendedByte);
                }

                this.interpolateData.flip();
                GlStateManager.bindTexture(this.dstTextId);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, this.dstX, this.dstY, this.frameWidth, this.frameHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)this.interpolateData);
            }
        }
    }

    private int mix(int intValue, int secondIntValue, double blendFactor)
    {
        return (int)((double)intValue * (1.0D - blendFactor) + (double)secondIntValue * blendFactor);
    }

    public String getSrcTex()
    {
        return this.srcTex;
    }

    public String getDstTex()
    {
        return this.dstTex;
    }

    public ResourceLocation getDstTexLoc()
    {
        return this.dstTexLoc;
    }

    public boolean isActive()
    {
        return this.active;
    }
}
