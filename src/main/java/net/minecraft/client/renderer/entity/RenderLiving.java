package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;

public abstract class RenderLiving<T extends EntityLiving> extends RendererLivingEntity<T>
{
    public RenderLiving(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn)
    {
        super(rendermanagerIn, modelbaseIn, shadowsizeIn);
    }

    protected boolean canRenderName(T entity)
    {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

    public boolean shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ)
    {
        if (super.shouldRender(livingEntity, camera, camX, camY, camZ))
        {
            return true;
        }
        else if (livingEntity.getLeashed() && livingEntity.getLeashedToEntity() != null)
        {
            Entity entity = livingEntity.getLeashedToEntity();
            return camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox());
        }
        else
        {
            return false;
        }
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        this.renderLeash(entity, x, y, z, entityYaw, partialTicks);
    }

    public void setLightmap(T entityLivingIn, float partialTicks)
    {
        int packedBrightness = entityLivingIn.getBrightnessForRender(partialTicks);
        int lightmapU = packedBrightness % 65536;
        int lightmapV = packedBrightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightmapU / 1.0F, (float)lightmapV / 1.0F);
    }

    private double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }

    protected void renderLeash(T entityLivingIn, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!Config.isShaders() || !Shaders.isShadowPass)
        {
            Entity entity = entityLivingIn.getLeashedToEntity();

            if (entity != null)
            {
                y = y - (1.6D - (double)entityLivingIn.height) * 0.5D;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                double holderYawRadians = this.interpolateValue((double)entity.prevRotationYaw, (double)entity.rotationYaw, (double)(partialTicks * 0.5F)) * 0.01745329238474369D;
                double holderPitchRadians = this.interpolateValue((double)entity.prevRotationPitch, (double)entity.rotationPitch, (double)(partialTicks * 0.5F)) * 0.01745329238474369D;
                double holderYawCos = Math.cos(holderYawRadians);
                double holderYawSin = Math.sin(holderYawRadians);
                double holderPitchSin = Math.sin(holderPitchRadians);

                if (entity instanceof EntityHanging)
                {
                    holderYawCos = 0.0D;
                    holderYawSin = 0.0D;
                    holderPitchSin = -1.0D;
                }

                double holderPitchCos = Math.cos(holderPitchRadians);
                double holderAnchorX = this.interpolateValue(entity.prevPosX, entity.posX, (double)partialTicks) - holderYawCos * 0.7D - holderYawSin * 0.5D * holderPitchCos;
                double holderAnchorY = this.interpolateValue(entity.prevPosY + (double)entity.getEyeHeight() * 0.7D, entity.posY + (double)entity.getEyeHeight() * 0.7D, (double)partialTicks) - holderPitchSin * 0.5D - 0.25D;
                double holderAnchorZ = this.interpolateValue(entity.prevPosZ, entity.posZ, (double)partialTicks) - holderYawSin * 0.7D + holderYawCos * 0.5D * holderPitchCos;
                double livingYawRadians = this.interpolateValue((double)entityLivingIn.prevRenderYawOffset, (double)entityLivingIn.renderYawOffset, (double)partialTicks) * 0.01745329238474369D + (Math.PI / 2D);
                double livingLeashOffsetX = Math.cos(livingYawRadians) * (double)entityLivingIn.width * 0.4D;
                double livingLeashOffsetZ = Math.sin(livingYawRadians) * (double)entityLivingIn.width * 0.4D;
                double livingAnchorX = this.interpolateValue(entityLivingIn.prevPosX, entityLivingIn.posX, (double)partialTicks) + livingLeashOffsetX;
                double livingAnchorY = this.interpolateValue(entityLivingIn.prevPosY, entityLivingIn.posY, (double)partialTicks);
                double livingAnchorZ = this.interpolateValue(entityLivingIn.prevPosZ, entityLivingIn.posZ, (double)partialTicks) + livingLeashOffsetZ;
                x = x + livingLeashOffsetX;
                z = z + livingLeashOffsetZ;
                double leashDeltaX = (double)((float)(holderAnchorX - livingAnchorX));
                double leashDeltaY = (double)((float)(holderAnchorY - livingAnchorY));
                double leashDeltaZ = (double)((float)(holderAnchorZ - livingAnchorZ));
                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.disableCull();

                if (Config.isShaders())
                {
                    Shaders.beginLeash();
                }

                int segmentCount = 24;
                double leashThickness = 0.025D;
                worldRenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);

                for (int segmentIndex = 0; segmentIndex <= segmentCount; ++segmentIndex)
                {
                    float red = 0.5F;
                    float green = 0.4F;
                    float blue = 0.3F;

                    if (segmentIndex % 2 == 0)
                    {
                        red *= 0.7F;
                        green *= 0.7F;
                        blue *= 0.7F;
                    }

                    float progress = (float)segmentIndex / (float)segmentCount;
                    worldRenderer.pos(x + leashDeltaX * (double)progress + 0.0D, y + leashDeltaY * (double)(progress * progress + progress) * 0.5D + (double)(((float)segmentCount - (float)segmentIndex) / 18.0F + 0.125F), z + leashDeltaZ * (double)progress).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + leashDeltaX * (double)progress + leashThickness, y + leashDeltaY * (double)(progress * progress + progress) * 0.5D + (double)(((float)segmentCount - (float)segmentIndex) / 18.0F + 0.125F) + leashThickness, z + leashDeltaZ * (double)progress).color(red, green, blue, 1.0F).endVertex();
                }

                tessellator.draw();
                worldRenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);

                for (int segmentIndex = 0; segmentIndex <= segmentCount; ++segmentIndex)
                {
                    float red = 0.5F;
                    float green = 0.4F;
                    float blue = 0.3F;

                    if (segmentIndex % 2 == 0)
                    {
                        red *= 0.7F;
                        green *= 0.7F;
                        blue *= 0.7F;
                    }

                    float progress = (float)segmentIndex / (float)segmentCount;
                    worldRenderer.pos(x + leashDeltaX * (double)progress + 0.0D, y + leashDeltaY * (double)(progress * progress + progress) * 0.5D + (double)(((float)segmentCount - (float)segmentIndex) / 18.0F + 0.125F) + leashThickness, z + leashDeltaZ * (double)progress).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + leashDeltaX * (double)progress + leashThickness, y + leashDeltaY * (double)(progress * progress + progress) * 0.5D + (double)(((float)segmentCount - (float)segmentIndex) / 18.0F + 0.125F), z + leashDeltaZ * (double)progress + leashThickness).color(red, green, blue, 1.0F).endVertex();
                }

                tessellator.draw();

                if (Config.isShaders())
                {
                    Shaders.endLeash();
                }

                GlStateManager.enableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.enableCull();
            }
        }
    }
}
