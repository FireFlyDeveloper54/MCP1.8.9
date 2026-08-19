package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderCreeper extends RenderLiving<EntityCreeper>
{
    private static final ResourceLocation creeperTextures = new ResourceLocation("textures/entity/creeper/creeper.png");

    public RenderCreeper(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelCreeper(), 0.5F);
        this.addLayer(new LayerCreeperCharge(this));
    }

    protected void preRenderCallback(EntityCreeper entitylivingbaseIn, float partialTickTime)
    {
        float flashIntensity = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);
        float flashScale = 1.0F + MathHelper.sin(flashIntensity * 100.0F) * flashIntensity * 0.01F;
        flashIntensity = MathHelper.clamp_float(flashIntensity, 0.0F, 1.0F);
        flashIntensity = flashIntensity * flashIntensity;
        flashIntensity = flashIntensity * flashIntensity;
        float horizontalScale = (1.0F + flashIntensity * 0.4F) * flashScale;
        float verticalScale = (1.0F + flashIntensity * 0.1F) / flashScale;
        GlStateManager.scale(horizontalScale, verticalScale, horizontalScale);
    }

    protected int getColorMultiplier(EntityCreeper entitylivingbaseIn, float lightBrightness, float partialTickTime)
    {
        float flashIntensity = entitylivingbaseIn.getCreeperFlashIntensity(partialTickTime);

        if ((int)(flashIntensity * 10.0F) % 2 == 0)
        {
            return 0;
        }
        else
        {
            int flashAlpha = (int)(flashIntensity * 0.2F * 255.0F);
            flashAlpha = MathHelper.clamp_int(flashAlpha, 0, 255);
            return flashAlpha << 24 | 16777215;
        }
    }

    protected ResourceLocation getEntityTexture(EntityCreeper entity)
    {
        return creeperTextures;
    }
}
