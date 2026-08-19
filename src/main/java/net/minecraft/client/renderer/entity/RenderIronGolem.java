package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerIronGolemFlower;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;

public class RenderIronGolem extends RenderLiving<EntityIronGolem>
{
    private static final ResourceLocation ironGolemTextures = new ResourceLocation("textures/entity/iron_golem.png");

    public RenderIronGolem(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelIronGolem(), 0.5F);
        this.addLayer(new LayerIronGolemFlower(this));
    }

    protected ResourceLocation getEntityTexture(EntityIronGolem entity)
    {
        return ironGolemTextures;
    }

    protected void rotateCorpse(EntityIronGolem golem, float ageInTicks, float rotationYaw, float partialTicks)
    {
        super.rotateCorpse(golem, ageInTicks, rotationYaw, partialTicks);

        if ((double)golem.limbSwingAmount >= 0.01D)
        {
            float stridePeriod = 13.0F;
            float swingPhase = golem.limbSwing - golem.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
            float swingOffset = (Math.abs(swingPhase % stridePeriod - stridePeriod * 0.5F) - stridePeriod * 0.25F) / (stridePeriod * 0.25F);
            GlStateManager.rotate(6.5F * swingOffset, 0.0F, 0.0F, 1.0F);
        }
    }
}
