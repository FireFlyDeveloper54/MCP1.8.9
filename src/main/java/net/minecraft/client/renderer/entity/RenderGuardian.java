package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelGuardian;
import optimization.FastTrig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class RenderGuardian extends RenderLiving<EntityGuardian>
{
    private static final ResourceLocation GUARDIAN_TEXTURE = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_ELDER_TEXTURE = new ResourceLocation("textures/entity/guardian_elder.png");
    private static final ResourceLocation GUARDIAN_BEAM_TEXTURE = new ResourceLocation("textures/entity/guardian_beam.png");
    int modelVersion;

    public RenderGuardian(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelGuardian(), 0.5F);
        this.modelVersion = ((ModelGuardian)this.mainModel).getModelVersion();
    }

    public boolean shouldRender(EntityGuardian livingEntity, ICamera camera, double camX, double camY, double camZ)
    {
        if (super.shouldRender(livingEntity, camera, camX, camY, camZ))
        {
            return true;
        }
        else
        {
            if (livingEntity.hasTargetedEntity())
            {
                EntityLivingBase targetedEntity = livingEntity.getTargetedEntity();

                if (targetedEntity != null)
                {
                    Vec3 targetPosition = this.getPosition(targetedEntity, (double)targetedEntity.height * 0.5D, 1.0F);
                    Vec3 guardianPosition = this.getPosition(livingEntity, (double)livingEntity.getEyeHeight(), 1.0F);

                    if (camera.isBoundingBoxInFrustum(AxisAlignedBB.fromBounds(guardianPosition.xCoord, guardianPosition.yCoord, guardianPosition.zCoord, targetPosition.xCoord, targetPosition.yCoord, targetPosition.zCoord)))
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private Vec3 getPosition(EntityLivingBase entityLivingBaseIn, double yOffset, float partialTicks)
    {
        double interpolatedX = entityLivingBaseIn.lastTickPosX + (entityLivingBaseIn.posX - entityLivingBaseIn.lastTickPosX) * (double)partialTicks;
        double interpolatedY = yOffset + entityLivingBaseIn.lastTickPosY + (entityLivingBaseIn.posY - entityLivingBaseIn.lastTickPosY) * (double)partialTicks;
        double interpolatedZ = entityLivingBaseIn.lastTickPosZ + (entityLivingBaseIn.posZ - entityLivingBaseIn.lastTickPosZ) * (double)partialTicks;
        return new Vec3(interpolatedX, interpolatedY, interpolatedZ);
    }

    public void doRender(EntityGuardian entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (this.modelVersion != ((ModelGuardian)this.mainModel).getModelVersion())
        {
            this.mainModel = new ModelGuardian();
            this.modelVersion = ((ModelGuardian)this.mainModel).getModelVersion();
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        EntityLivingBase targetedEntity = entity.getTargetedEntity();

        if (targetedEntity != null)
        {
            float attackScale = entity.getAttackAnimationScale(partialTicks);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            this.bindTexture(GUARDIAN_BEAM_TEXTURE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            float fullBrightness = 240.0F;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, fullBrightness, fullBrightness);
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            float worldTime = (float)entity.worldObj.getTotalWorldTime() + partialTicks;
            float textureScroll = worldTime * 0.5F % 1.0F;
            float eyeHeight = entity.getEyeHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x, (float)y + eyeHeight, (float)z);
            Vec3 targetPosition = this.getPosition(targetedEntity, (double)targetedEntity.height * 0.5D, partialTicks);
            Vec3 guardianEyePosition = this.getPosition(entity, (double)eyeHeight, partialTicks);
            Vec3 beamVector = targetPosition.subtract(guardianEyePosition);
            double beamLength = beamVector.lengthVector() + 1.0D;
            beamVector = beamVector.normalize();
            float beamPitchRadians = (float)Math.acos(beamVector.yCoord);
            float beamYawRadians = (float)FastTrig.atan2(beamVector.zCoord, beamVector.xCoord);
            GlStateManager.rotate((((float)Math.PI / 2F) + -beamYawRadians) * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(beamPitchRadians * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            int beamPass = 1;
            double beamRotation = (double)worldTime * 0.05D * (1.0D - (double)(beamPass & 1) * 2.5D);
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            float attackScaleSquared = attackScale * attackScale;
            int red = 64 + (int)(attackScaleSquared * 240.0F);
            int green = 32 + (int)(attackScaleSquared * 192.0F);
            int blue = 128 - (int)(attackScaleSquared * 64.0F);
            double beamRadius = (double)beamPass * 0.2D;
            double diagonalRadius = beamRadius * 1.41D;
            double diagonalX1 = 0.0D + Math.cos(beamRotation + 2.356194490192345D) * diagonalRadius;
            double diagonalZ1 = 0.0D + Math.sin(beamRotation + 2.356194490192345D) * diagonalRadius;
            double diagonalX2 = 0.0D + Math.cos(beamRotation + (Math.PI / 4D)) * diagonalRadius;
            double diagonalZ2 = 0.0D + Math.sin(beamRotation + (Math.PI / 4D)) * diagonalRadius;
            double diagonalX3 = 0.0D + Math.cos(beamRotation + 3.9269908169872414D) * diagonalRadius;
            double diagonalZ3 = 0.0D + Math.sin(beamRotation + 3.9269908169872414D) * diagonalRadius;
            double diagonalX4 = 0.0D + Math.cos(beamRotation + 5.497787143782138D) * diagonalRadius;
            double diagonalZ4 = 0.0D + Math.sin(beamRotation + 5.497787143782138D) * diagonalRadius;
            double sideX1 = 0.0D + Math.cos(beamRotation + Math.PI) * beamRadius;
            double sideZ1 = 0.0D + Math.sin(beamRotation + Math.PI) * beamRadius;
            double sideX2 = 0.0D + Math.cos(beamRotation + 0.0D) * beamRadius;
            double sideZ2 = 0.0D + Math.sin(beamRotation + 0.0D) * beamRadius;
            double sideX3 = 0.0D + Math.cos(beamRotation + (Math.PI / 2D)) * beamRadius;
            double sideZ3 = 0.0D + Math.sin(beamRotation + (Math.PI / 2D)) * beamRadius;
            double sideX4 = 0.0D + Math.cos(beamRotation + (Math.PI * 3D / 2D)) * beamRadius;
            double sideZ4 = 0.0D + Math.sin(beamRotation + (Math.PI * 3D / 2D)) * beamRadius;
            double textureMinV = (double)(-1.0F + textureScroll);
            double textureMaxV = beamLength * (0.5D / beamRadius) + textureMinV;
            worldRenderer.pos(sideX1, beamLength, sideZ1).tex(0.4999D, textureMaxV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX1, 0.0D, sideZ1).tex(0.4999D, textureMinV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX2, 0.0D, sideZ2).tex(0.0D, textureMinV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX2, beamLength, sideZ2).tex(0.0D, textureMaxV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX3, beamLength, sideZ3).tex(0.4999D, textureMaxV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX3, 0.0D, sideZ3).tex(0.4999D, textureMinV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX4, 0.0D, sideZ4).tex(0.0D, textureMinV).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(sideX4, beamLength, sideZ4).tex(0.0D, textureMaxV).color(red, green, blue, 255).endVertex();
            double textureVOffset = 0.0D;

            if (entity.ticksExisted % 2 == 0)
            {
                textureVOffset = 0.5D;
            }

            worldRenderer.pos(diagonalX1, beamLength, diagonalZ1).tex(0.5D, textureVOffset + 0.5D).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(diagonalX2, beamLength, diagonalZ2).tex(1.0D, textureVOffset + 0.5D).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(diagonalX4, beamLength, diagonalZ4).tex(1.0D, textureVOffset).color(red, green, blue, 255).endVertex();
            worldRenderer.pos(diagonalX3, beamLength, diagonalZ3).tex(0.5D, textureVOffset).color(red, green, blue, 255).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }
    }

    protected void preRenderCallback(EntityGuardian entitylivingbaseIn, float partialTickTime)
    {
        if (entitylivingbaseIn.isElder())
        {
            GlStateManager.scale(2.35F, 2.35F, 2.35F);
        }
    }

    protected ResourceLocation getEntityTexture(EntityGuardian entity)
    {
        return entity.isElder() ? GUARDIAN_ELDER_TEXTURE : GUARDIAN_TEXTURE;
    }
}
