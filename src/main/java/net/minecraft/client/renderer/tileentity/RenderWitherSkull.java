package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.ResourceLocation;

public class RenderWitherSkull extends Render<EntityWitherSkull>
{
    private static final ResourceLocation invulnerableWitherTextures = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation witherTextures = new ResourceLocation("textures/entity/wither/wither.png");
    private final ModelSkeletonHead skeletonHeadModel = new ModelSkeletonHead();

    public RenderWitherSkull(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    private float interpolateRotation(float previousRotation, float currentRotation, float partialTicks)
    {
        float rotationDelta;

        for (rotationDelta = currentRotation - previousRotation; rotationDelta < -180.0F; rotationDelta += 360.0F)
        {
            ;
        }

        while (rotationDelta >= 180.0F)
        {
            rotationDelta -= 360.0F;
        }

        return previousRotation + partialTicks * rotationDelta;
    }

    public void doRender(EntityWitherSkull entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        float interpolatedYaw = this.interpolateRotation(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
        float interpolatedPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        GlStateManager.translate((float)x, (float)y, (float)z);
        float modelScale = 0.0625F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.enableAlpha();
        this.bindEntityTexture(entity);
        this.skeletonHeadModel.render(entity, 0.0F, 0.0F, 0.0F, interpolatedYaw, interpolatedPitch, modelScale);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityWitherSkull entity)
    {
        return entity.isInvulnerable() ? invulnerableWitherTextures : witherTextures;
    }
}
