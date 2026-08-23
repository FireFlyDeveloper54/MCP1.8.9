package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderArrow extends Render<EntityArrow>
{
    private static final ResourceLocation arrowTextures = new ResourceLocation("textures/entity/arrow.png");

    public RenderArrow(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    public void doRender(EntityArrow entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        this.bindEntityTexture(entity);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        int textureRow = 0;
        float shaftMinU = 0.0F;
        float shaftMaxU = 0.5F;
        float shaftMinV = (float)(textureRow * 10) / 32.0F;
        float shaftMaxV = (float)(5 + textureRow * 10) / 32.0F;
        float headMinU = 0.0F;
        float headMaxU = 0.15625F;
        float headMinV = (float)(5 + textureRow * 10) / 32.0F;
        float headMaxV = (float)(10 + textureRow * 10) / 32.0F;
        float modelScale = 0.05625F;
        GlStateManager.enableRescaleNormal();
        float shakeTime = (float)entity.arrowShake - partialTicks;

        if (shakeTime > 0.0F)
        {
            float shakeRotation = -MathHelper.sin(shakeTime * 3.0F) * shakeTime;
            GlStateManager.rotate(shakeRotation, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(modelScale, modelScale, modelScale);
        GlStateManager.translate(-4.0F, 0.0F, 0.0F);
        GlStateManager.normal(modelScale, 0.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(-7.0D, -2.0D, -2.0D).tex((double)headMinU, (double)headMinV).endVertex();
        worldRenderer.pos(-7.0D, -2.0D, 2.0D).tex((double)headMaxU, (double)headMinV).endVertex();
        worldRenderer.pos(-7.0D, 2.0D, 2.0D).tex((double)headMaxU, (double)headMaxV).endVertex();
        worldRenderer.pos(-7.0D, 2.0D, -2.0D).tex((double)headMinU, (double)headMaxV).endVertex();
        tessellator.draw();
        GlStateManager.normal(-modelScale, 0.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(-7.0D, 2.0D, -2.0D).tex((double)headMinU, (double)headMinV).endVertex();
        worldRenderer.pos(-7.0D, 2.0D, 2.0D).tex((double)headMaxU, (double)headMinV).endVertex();
        worldRenderer.pos(-7.0D, -2.0D, 2.0D).tex((double)headMaxU, (double)headMaxV).endVertex();
        worldRenderer.pos(-7.0D, -2.0D, -2.0D).tex((double)headMinU, (double)headMaxV).endVertex();
        tessellator.draw();

        for (int sideIndex = 0; sideIndex < 4; ++sideIndex)
        {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.normal(0.0F, 0.0F, modelScale);
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(-8.0D, -2.0D, 0.0D).tex((double)shaftMinU, (double)shaftMinV).endVertex();
            worldRenderer.pos(8.0D, -2.0D, 0.0D).tex((double)shaftMaxU, (double)shaftMinV).endVertex();
            worldRenderer.pos(8.0D, 2.0D, 0.0D).tex((double)shaftMaxU, (double)shaftMaxV).endVertex();
            worldRenderer.pos(-8.0D, 2.0D, 0.0D).tex((double)shaftMinU, (double)shaftMaxV).endVertex();
            tessellator.draw();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityArrow entity)
    {
        return arrowTextures;
    }
}
