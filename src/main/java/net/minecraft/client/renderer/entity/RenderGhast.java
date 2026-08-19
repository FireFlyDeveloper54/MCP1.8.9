package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelGhast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.ResourceLocation;

public class RenderGhast extends RenderLiving<EntityGhast>
{
    private static final ResourceLocation ghastTextures = new ResourceLocation("textures/entity/ghast/ghast.png");
    private static final ResourceLocation ghastShootingTextures = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

    public RenderGhast(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelGhast(), 0.5F);
    }

    protected ResourceLocation getEntityTexture(EntityGhast entity)
    {
        return entity.isAttacking() ? ghastShootingTextures : ghastTextures;
    }

    protected void preRenderCallback(EntityGhast entitylivingbaseIn, float partialTickTime)
    {
        float baseScale = 1.0F;
        float verticalScale = (8.0F + baseScale) / 2.0F;
        float horizontalScale = (8.0F + 1.0F / baseScale) / 2.0F;
        GlStateManager.scale(horizontalScale, verticalScale, horizontalScale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
