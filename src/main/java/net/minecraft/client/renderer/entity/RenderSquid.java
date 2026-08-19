package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;

public class RenderSquid extends RenderLiving<EntitySquid>
{
    private static final ResourceLocation squidTextures = new ResourceLocation("textures/entity/squid.png");

    public RenderSquid(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn)
    {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    protected ResourceLocation getEntityTexture(EntitySquid entity)
    {
        return squidTextures;
    }

    protected void rotateCorpse(EntitySquid squid, float ageInTicks, float rotationYaw, float partialTicks)
    {
        float interpolatedSquidPitch = squid.prevSquidPitch + (squid.squidPitch - squid.prevSquidPitch) * partialTicks;
        float interpolatedSquidYaw = squid.prevSquidYaw + (squid.squidYaw - squid.prevSquidYaw) * partialTicks;
        GlStateManager.translate(0.0F, 0.5F, 0.0F);
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(interpolatedSquidPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(interpolatedSquidYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -1.2F, 0.0F);
    }

    protected float handleRotationFloat(EntitySquid livingBase, float partialTicks)
    {
        return livingBase.lastTentacleAngle + (livingBase.tentacleAngle - livingBase.lastTentacleAngle) * partialTicks;
    }
}
