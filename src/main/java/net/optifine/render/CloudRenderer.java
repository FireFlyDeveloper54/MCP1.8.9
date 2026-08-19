package net.optifine.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class CloudRenderer
{
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
    private int cloudDisplayList = -1;

    public CloudRenderer(Minecraft mc)
    {
        this.minecraft = mc;
        this.cloudDisplayList = GLAllocation.generateDisplayLists(1);
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
            Entity entity = this.minecraft.getRenderViewEntity();
            boolean lastEyeAboveClouds = this.lastPlayerY + (double)entity.getEyeHeight() < 128.0D + (double)(this.minecraft.gameSettings.ofCloudsHeight * 128.0F);
            boolean currentEyeAboveClouds = entity.prevPosY + (double)entity.getEyeHeight() < 128.0D + (double)(this.minecraft.gameSettings.ofCloudsHeight * 128.0F);
            return currentEyeAboveClouds != lastEyeAboveClouds;
        }
    }

    public void startUpdateGlList()
    {
        GL11.glNewList(this.cloudDisplayList, GL11.GL_COMPILE);
    }

    public void endUpdateGlList()
    {
        GL11.glEndList();
        this.lastRenderFancy = this.renderFancy;
        this.lastCloudTickCounter = this.cloudTickCounter;
        this.lastCloudColor = this.cloudColor;
        this.lastPlayerX = this.minecraft.getRenderViewEntity().prevPosX;
        this.lastPlayerY = this.minecraft.getRenderViewEntity().prevPosY;
        this.lastPlayerZ = this.minecraft.getRenderViewEntity().prevPosZ;
        this.updated = true;
        GlStateManager.resetColor();
    }

    public void renderGlList()
    {
        Entity entity = this.minecraft.getRenderViewEntity();
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

        GlStateManager.callList(this.cloudDisplayList);
        GlStateManager.popMatrix();
        GlStateManager.resetColor();
    }

    public void reset()
    {
        this.updated = false;
    }
}
