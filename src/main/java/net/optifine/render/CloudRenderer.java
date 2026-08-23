package net.optifine.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class CloudRenderer
{
    private static CloudRenderer capturing;
    private Minecraft minecraft;
    private boolean updated = false;
    private boolean renderFancy = false;
    int cloudTickCounter;
    private Vec3 cloudColor;
    float partialTicks;
    private boolean lastRenderFancy = false;
    private int lastCloudTickCounter = 0;
    private Vec3 lastCloudColor = new Vec3(-1.0D, -1.0D, -1.0D);
    private double lastPlayerX = 0.0D;
    private double lastPlayerY = 0.0D;
    private double lastPlayerZ = 0.0D;
    private VertexBuffer cloudBuffer;
    private VertexFormat cloudFormat;
    private int cloudMode;
    private int cloudVertexCount;
    private ByteBuffer captureBytes;

    public CloudRenderer(Minecraft mc)
    {
        this.minecraft = mc;
    }

    public void prepareToRender(boolean renderFancy, int cloudTickCounter, float partialTicks, Vec3 cloudColor)
    {
        this.renderFancy = renderFancy;
        this.cloudTickCounter = cloudTickCounter;
        this.partialTicks = partialTicks;
        this.cloudColor = cloudColor;
    }

    public boolean shouldUpdateGlList()
    {
        Entity entity = this.minecraft.getRenderViewEntity();

        if (entity == null)
        {
            return false;
        }

        if (!this.updated)
        {
            return true;
        }
        else if (this.renderFancy != this.lastRenderFancy)
        {
            return true;
        }
        else if (this.cloudTickCounter >= this.lastCloudTickCounter + 20)
        {
            return true;
        }
        else if (Math.abs(this.cloudColor.xCoord - this.lastCloudColor.xCoord) > 0.003D)
        {
            return true;
        }
        else if (Math.abs(this.cloudColor.yCoord - this.lastCloudColor.yCoord) > 0.003D)
        {
            return true;
        }
        else if (Math.abs(this.cloudColor.zCoord - this.lastCloudColor.zCoord) > 0.003D)
        {
            return true;
        }
        else
        {
            boolean lastEyeAboveClouds = this.lastPlayerY + (double)entity.getEyeHeight() < 128.0D + (double)(this.minecraft.gameSettings.ofCloudsHeight * 128.0F);
            boolean currentEyeAboveClouds = entity.prevPosY + (double)entity.getEyeHeight() < 128.0D + (double)(this.minecraft.gameSettings.ofCloudsHeight * 128.0F);
            return currentEyeAboveClouds != lastEyeAboveClouds;
        }
    }

    public static boolean isCapturing()
    {
        return capturing != null;
    }

    public void startUpdateGlList()
    {
        capturing = this;
        this.cloudVertexCount = 0;
        this.cloudFormat = null;

        if (this.captureBytes != null)
        {
            this.captureBytes.clear();
        }
    }

    public static boolean captureDraw(WorldRenderer worldRenderer)
    {
        CloudRenderer renderer = capturing;

        if (renderer == null)
        {
            return false;
        }

        try
        {
            worldRenderer.finishDrawing();
            renderer.append(worldRenderer);
            worldRenderer.reset();
            return true;
        }
        catch (Throwable throwable)
        {
            renderer.abortCapture();

            if (throwable instanceof RuntimeException)
            {
                throw (RuntimeException)throwable;
            }

            throw new RuntimeException(throwable);
        }
    }

    private void append(WorldRenderer worldRenderer)
    {
        ByteBuffer source = worldRenderer.getByteBuffer();
        int remaining = source.remaining();

        if (remaining <= 0)
        {
            return;
        }

        this.cloudFormat = worldRenderer.getVertexFormat();
        this.cloudMode = worldRenderer.getDrawMode();
        this.ensureCaptureCapacity(remaining);
        this.captureBytes.put(source);
        this.cloudVertexCount += worldRenderer.getVertexCount();
    }

    private void ensureCaptureCapacity(int extra)
    {
        if (this.captureBytes == null)
        {
            this.captureBytes = ByteBuffer.allocateDirect(Math.max(262144, extra)).order(ByteOrder.nativeOrder());
            return;
        }

        if (this.captureBytes.remaining() < extra)
        {
            ByteBuffer grown = ByteBuffer.allocateDirect(this.captureBytes.position() + extra + this.captureBytes.capacity()).order(ByteOrder.nativeOrder());
            this.captureBytes.flip();
            grown.put(this.captureBytes);
            this.captureBytes = grown;
        }
    }

    public void endUpdateGlList()
    {
        try
        {
            this.flushWorldRenderer();

            if (this.captureBytes != null && this.cloudVertexCount > 0 && this.cloudFormat != null)
            {
                this.captureBytes.flip();

                if (this.cloudBuffer != null)
                {
                    this.cloudBuffer.deleteGlBuffers();
                }

                this.cloudBuffer = new VertexBuffer(this.cloudFormat);
                this.cloudBuffer.bufferData(this.captureBytes);
            }

            Entity entity = this.minecraft.getRenderViewEntity();
            this.lastRenderFancy = this.renderFancy;
            this.lastCloudTickCounter = this.cloudTickCounter;
            this.lastCloudColor = this.cloudColor;

            if (entity != null)
            {
                this.lastPlayerX = entity.prevPosX;
                this.lastPlayerY = entity.prevPosY;
                this.lastPlayerZ = entity.prevPosZ;
            }

            this.updated = true;
            GlStateManager.resetColor();
        }
        finally
        {
            capturing = null;
        }
    }

    public void abortCapture()
    {
        capturing = null;
        this.cloudVertexCount = 0;
        this.cloudFormat = null;
        this.updated = false;

        if (this.captureBytes != null)
        {
            this.captureBytes.clear();
        }

        this.flushWorldRenderer();
    }

    private void flushWorldRenderer()
    {
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        if (!worldRenderer.isDrawing())
        {
            return;
        }

        try
        {
            worldRenderer.finishDrawing();
            this.append(worldRenderer);
            worldRenderer.reset();
        }
        catch (Throwable ignored)
        {
            worldRenderer.cancelDrawing();
        }
    }

    public void renderGlList()
    {
        Entity entity = this.minecraft.getRenderViewEntity();

        if (entity == null)
        {
            return;
        }

        double playerX = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)this.partialTicks;
        double playerY = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)this.partialTicks;
        double playerZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)this.partialTicks;
        double tickDelta = (double)((float)(this.cloudTickCounter - this.lastCloudTickCounter) + this.partialTicks);
        float offsetX = (float)(playerX - this.lastPlayerX + tickDelta * 0.03D);
        float offsetY = (float)(playerY - this.lastPlayerY);
        float offsetZ = (float)(playerZ - this.lastPlayerZ);
        GlStateManager.pushMatrix();

        if (this.renderFancy)
        {
            GlStateManager.translate(-offsetX / 12.0F, -offsetY, -offsetZ / 12.0F);
        }
        else
        {
            GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
        }

        if (this.cloudBuffer != null && this.cloudVertexCount > 0 && this.cloudFormat != null)
        {
            this.cloudBuffer.bindDrawState();
            net.minecraft.client.renderer.CorePipeline.prepareDraw(false, false);
            GlStateManager.glDrawArrays(this.cloudMode, 0, this.cloudVertexCount);
        }

        GlStateManager.popMatrix();
        GlStateManager.resetColor();
    }

    public void reset()
    {
        this.updated = false;
    }
}
