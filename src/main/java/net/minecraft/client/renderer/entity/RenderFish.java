package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class RenderFish extends Render<EntityFishHook>
{
    private static final ResourceLocation FISH_PARTICLES = new ResourceLocation("textures/particle/particles.png");
    private static final Vec3 ROD_OFFSET = new Vec3(-0.36D, 0.03D, 0.35D);

    public RenderFish(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    public void doRender(EntityFishHook entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(entity);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        worldRenderer.pos(-0.5D, -0.5D, 0.0D).tex(0.0625D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(0.5D, -0.5D, 0.0D).tex(0.125D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(0.5D, 0.5D, 0.0D).tex(0.125D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(-0.5D, 0.5D, 0.0D).tex(0.0625D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        if (entity.angler != null)
        {
            float swingProgress = entity.angler.getSwingProgress(partialTicks);
            float swingOffset = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
            Vec3 rodOffset = ROD_OFFSET;
            rodOffset = rodOffset.rotatePitch(-(entity.angler.prevRotationPitch + (entity.angler.rotationPitch - entity.angler.prevRotationPitch) * partialTicks) * (float)Math.PI / 180.0F);
            rodOffset = rodOffset.rotateYaw(-(entity.angler.prevRotationYaw + (entity.angler.rotationYaw - entity.angler.prevRotationYaw) * partialTicks) * (float)Math.PI / 180.0F);
            rodOffset = rodOffset.rotateYaw(swingOffset * 0.5F);
            rodOffset = rodOffset.rotatePitch(-swingOffset * 0.7F);
            double lineStartX = entity.angler.prevPosX + (entity.angler.posX - entity.angler.prevPosX) * (double)partialTicks + rodOffset.xCoord;
            double lineStartY = entity.angler.prevPosY + (entity.angler.posY - entity.angler.prevPosY) * (double)partialTicks + rodOffset.yCoord;
            double lineStartZ = entity.angler.prevPosZ + (entity.angler.posZ - entity.angler.prevPosZ) * (double)partialTicks + rodOffset.zCoord;
            double lineStartYOffset = (double)entity.angler.getEyeHeight();

            if (this.renderManager.options != null && this.renderManager.options.thirdPersonView > 0 || entity.angler != Minecraft.getMinecraft().thePlayer)
            {
                float anglerBodyYawRadians = (entity.angler.prevRenderYawOffset + (entity.angler.renderYawOffset - entity.angler.prevRenderYawOffset) * partialTicks) * (float)Math.PI / 180.0F;
                double yawSin = (double)MathHelper.sin(anglerBodyYawRadians);
                double yawCos = (double)MathHelper.cos(anglerBodyYawRadians);
                lineStartX = entity.angler.prevPosX + (entity.angler.posX - entity.angler.prevPosX) * (double)partialTicks - yawCos * 0.35D - yawSin * 0.8D;
                lineStartY = entity.angler.prevPosY + lineStartYOffset + (entity.angler.posY - entity.angler.prevPosY) * (double)partialTicks - 0.45D;
                lineStartZ = entity.angler.prevPosZ + (entity.angler.posZ - entity.angler.prevPosZ) * (double)partialTicks - yawSin * 0.35D + yawCos * 0.8D;
                lineStartYOffset = entity.angler.isSneaking() ? -0.1875D : 0.0D;
            }

            double hookX = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
            double hookY = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + 0.25D;
            double hookZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
            double lineDeltaX = (double)((float)(lineStartX - hookX));
            double lineDeltaY = (double)((float)(lineStartY - hookY)) + lineStartYOffset;
            double lineDeltaZ = (double)((float)(lineStartZ - hookZ));
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            int lineSegments = 16;

            for (int segmentIndex = 0; segmentIndex <= lineSegments; ++segmentIndex)
            {
                float segmentProgress = (float)segmentIndex / (float)lineSegments;
                worldRenderer.pos(x + lineDeltaX * (double)segmentProgress, y + lineDeltaY * (double)(segmentProgress * segmentProgress + segmentProgress) * 0.5D + 0.25D, z + lineDeltaZ * (double)segmentProgress).color(0, 0, 0, 255).endVertex();
            }

            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    protected ResourceLocation getEntityTexture(EntityFishHook entity)
    {
        return FISH_PARTICLES;
    }
}
