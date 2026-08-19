package net.optifine.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelSprite
{
    private ModelRenderer modelRenderer = null;
    private int textureOffsetX = 0;
    private int textureOffsetY = 0;
    private float posX = 0.0F;
    private float posY = 0.0F;
    private float posZ = 0.0F;
    private int sizeX = 0;
    private int sizeY = 0;
    private int sizeZ = 0;
    private float sizeAdd = 0.0F;
    private float minU = 0.0F;
    private float minV = 0.0F;
    private float maxU = 0.0F;
    private float maxV = 0.0F;

    public ModelSprite(ModelRenderer modelRenderer, int textureOffsetX, int textureOffsetY, float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd)
    {
        this.modelRenderer = modelRenderer;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeAdd = sizeAdd;
        this.minU = (float)textureOffsetX / modelRenderer.textureWidth;
        this.minV = (float)textureOffsetY / modelRenderer.textureHeight;
        this.maxU = (float)(textureOffsetX + sizeX) / modelRenderer.textureWidth;
        this.maxV = (float)(textureOffsetY + sizeY) / modelRenderer.textureHeight;
    }

    public void render(Tessellator tessellator, float scale)
    {
        GlStateManager.translate(this.posX * scale, this.posY * scale, this.posZ * scale);
        float renderMinU = this.minU;
        float renderMaxU = this.maxU;
        float renderMinV = this.minV;
        float renderMaxV = this.maxV;

        if (this.modelRenderer.mirror)
        {
            renderMinU = this.maxU;
            renderMaxU = this.minU;
        }

        if (this.modelRenderer.mirrorV)
        {
            renderMinV = this.maxV;
            renderMaxV = this.minV;
        }

        renderItemIn2D(tessellator, renderMinU, renderMinV, renderMaxU, renderMaxV, this.sizeX, this.sizeY, scale * (float)this.sizeZ, this.modelRenderer.textureWidth, this.modelRenderer.textureHeight);
        GlStateManager.translate(-this.posX * scale, -this.posY * scale, -this.posZ * scale);
    }

    public static void renderItemIn2D(Tessellator tess, float minU, float minV, float maxU, float maxV, int sizeX, int sizeY, float width, float texWidth, float texHeight)
    {
        if (width < 6.25E-4F)
        {
            width = 6.25E-4F;
        }

        float deltaU = maxU - minU;
        float deltaV = maxV - minV;
        double renderWidth = (double)(MathHelper.abs(deltaU) * (texWidth / 16.0F));
        double renderHeight = (double)(MathHelper.abs(deltaV) * (texHeight / 16.0F));
        WorldRenderer worldRenderer = tess.getWorldRenderer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(0.0D, renderHeight, 0.0D).tex((double)minU, (double)maxV).endVertex();
        worldRenderer.pos(renderWidth, renderHeight, 0.0D).tex((double)maxU, (double)maxV).endVertex();
        worldRenderer.pos(renderWidth, 0.0D, 0.0D).tex((double)maxU, (double)minV).endVertex();
        worldRenderer.pos(0.0D, 0.0D, 0.0D).tex((double)minU, (double)minV).endVertex();
        tess.draw();
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(0.0D, 0.0D, (double)width).tex((double)minU, (double)minV).endVertex();
        worldRenderer.pos(renderWidth, 0.0D, (double)width).tex((double)maxU, (double)minV).endVertex();
        worldRenderer.pos(renderWidth, renderHeight, (double)width).tex((double)maxU, (double)maxV).endVertex();
        worldRenderer.pos(0.0D, renderHeight, (double)width).tex((double)minU, (double)maxV).endVertex();
        tess.draw();
        float halfTexelU = 0.5F * deltaU / (float)sizeX;
        float halfTexelV = 0.5F * deltaV / (float)sizeY;
        GL11.glNormal3f(-1.0F, 0.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int x = 0; x < sizeX; ++x)
        {
            float xFraction = (float)x / (float)sizeX;
            float texU = minU + deltaU * xFraction + halfTexelU;
            worldRenderer.pos((double)xFraction * renderWidth, renderHeight, (double)width).tex((double)texU, (double)maxV).endVertex();
            worldRenderer.pos((double)xFraction * renderWidth, renderHeight, 0.0D).tex((double)texU, (double)maxV).endVertex();
            worldRenderer.pos((double)xFraction * renderWidth, 0.0D, 0.0D).tex((double)texU, (double)minV).endVertex();
            worldRenderer.pos((double)xFraction * renderWidth, 0.0D, (double)width).tex((double)texU, (double)minV).endVertex();
        }

        tess.draw();
        GL11.glNormal3f(1.0F, 0.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int x = 0; x < sizeX; ++x)
        {
            float xFraction = (float)x / (float)sizeX;
            float texU = minU + deltaU * xFraction + halfTexelU;
            float nextXFraction = xFraction + 1.0F / (float)sizeX;
            worldRenderer.pos((double)nextXFraction * renderWidth, 0.0D, (double)width).tex((double)texU, (double)minV).endVertex();
            worldRenderer.pos((double)nextXFraction * renderWidth, 0.0D, 0.0D).tex((double)texU, (double)minV).endVertex();
            worldRenderer.pos((double)nextXFraction * renderWidth, renderHeight, 0.0D).tex((double)texU, (double)maxV).endVertex();
            worldRenderer.pos((double)nextXFraction * renderWidth, renderHeight, (double)width).tex((double)texU, (double)maxV).endVertex();
        }

        tess.draw();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int y = 0; y < sizeY; ++y)
        {
            float yFraction = (float)y / (float)sizeY;
            float texV = minV + deltaV * yFraction + halfTexelV;
            float nextYFraction = yFraction + 1.0F / (float)sizeY;
            worldRenderer.pos(0.0D, (double)nextYFraction * renderHeight, (double)width).tex((double)minU, (double)texV).endVertex();
            worldRenderer.pos(renderWidth, (double)nextYFraction * renderHeight, (double)width).tex((double)maxU, (double)texV).endVertex();
            worldRenderer.pos(renderWidth, (double)nextYFraction * renderHeight, 0.0D).tex((double)maxU, (double)texV).endVertex();
            worldRenderer.pos(0.0D, (double)nextYFraction * renderHeight, 0.0D).tex((double)minU, (double)texV).endVertex();
        }

        tess.draw();
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int y = 0; y < sizeY; ++y)
        {
            float yFraction = (float)y / (float)sizeY;
            float texV = minV + deltaV * yFraction + halfTexelV;
            worldRenderer.pos(renderWidth, (double)yFraction * renderHeight, (double)width).tex((double)maxU, (double)texV).endVertex();
            worldRenderer.pos(0.0D, (double)yFraction * renderHeight, (double)width).tex((double)minU, (double)texV).endVertex();
            worldRenderer.pos(0.0D, (double)yFraction * renderHeight, 0.0D).tex((double)minU, (double)texV).endVertex();
            worldRenderer.pos(renderWidth, (double)yFraction * renderHeight, 0.0D).tex((double)maxU, (double)texV).endVertex();
        }

        tess.draw();
    }
}
