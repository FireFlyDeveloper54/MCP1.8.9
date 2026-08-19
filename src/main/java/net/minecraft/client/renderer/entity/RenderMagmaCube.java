package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelMagmaCube;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.util.ResourceLocation;

public class RenderMagmaCube extends RenderLiving<EntityMagmaCube>
{
    private static final ResourceLocation magmaCubeTextures = new ResourceLocation("textures/entity/slime/magmacube.png");

    public RenderMagmaCube(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelMagmaCube(), 0.25F);
    }

    protected ResourceLocation getEntityTexture(EntityMagmaCube entity)
    {
        return magmaCubeTextures;
    }

    protected void preRenderCallback(EntityMagmaCube entitylivingbaseIn, float partialTickTime)
    {
        int slimeSize = entitylivingbaseIn.getSlimeSize();
        float squishFactor = (entitylivingbaseIn.prevSquishFactor + (entitylivingbaseIn.squishFactor - entitylivingbaseIn.prevSquishFactor) * partialTickTime) / ((float)slimeSize * 0.5F + 1.0F);
        float inverseSquishFactor = 1.0F / (squishFactor + 1.0F);
        float cubeSize = (float)slimeSize;
        GlStateManager.scale(inverseSquishFactor * cubeSize, 1.0F / inverseSquishFactor * cubeSize, inverseSquishFactor * cubeSize);
    }
}
