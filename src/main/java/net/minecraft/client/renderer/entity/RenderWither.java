package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelWither;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerWitherAura;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.ResourceLocation;

public class RenderWither extends RenderLiving<EntityWither>
{
    private static final ResourceLocation invulnerableWitherTextures = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation witherTextures = new ResourceLocation("textures/entity/wither/wither.png");

    public RenderWither(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelWither(0.0F), 1.0F);
        this.addLayer(new LayerWitherAura(this));
    }

    public void doRender(EntityWither entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        BossStatus.setBossStatus(entity, true);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityWither entity)
    {
        int invulnerabilityTime = entity.getInvulTime();
        return invulnerabilityTime > 0 && (invulnerabilityTime > 80 || invulnerabilityTime / 5 % 2 != 1) ? invulnerableWitherTextures : witherTextures;
    }

    protected void preRenderCallback(EntityWither entitylivingbaseIn, float partialTickTime)
    {
        float witherScale = 2.0F;
        int invulnerabilityTime = entitylivingbaseIn.getInvulTime();

        if (invulnerabilityTime > 0)
        {
            witherScale -= ((float)invulnerabilityTime - partialTickTime) / 220.0F * 0.5F;
        }

        GlStateManager.scale(witherScale, witherScale, witherScale);
    }
}
