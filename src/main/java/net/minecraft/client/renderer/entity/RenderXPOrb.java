package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;

public class RenderXPOrb extends Render<EntityXPOrb>
{
    private static final ResourceLocation experienceOrbTextures = new ResourceLocation("textures/entity/experience_orb.png");

    public RenderXPOrb(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    public void doRender(EntityXPOrb entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        this.bindEntityTexture(entity);
        int textureIndex = entity.getTextureByXP();
        float minU = (float)(textureIndex % 4 * 16) / 64.0F;
        float maxU = (float)(textureIndex % 4 * 16 + 16) / 64.0F;
        float minV = (float)(textureIndex / 4 * 16) / 64.0F;
        float maxV = (float)(textureIndex / 4 * 16 + 16) / 64.0F;
        float quadSize = 1.0F;
        float halfQuadSize = 0.5F;
        float quadYOffset = 0.25F;
        int brightness = entity.getBrightnessForRender(partialTicks);
        int lightmapX = brightness % 65536;
        int lightmapY = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightmapX / 1.0F, (float)lightmapY / 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float colorTimer = ((float)entity.xpColor + partialTicks) / 2.0F;

        if (Config.isCustomColors())
        {
            colorTimer = CustomColors.getXpOrbTimer(colorTimer);
        }

        int redPulse = (int)((MathHelper.sin(colorTimer + 0.0F) + 1.0F) * 0.5F * 255.0F);
        int bluePulse = (int)((MathHelper.sin(colorTimer + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        float orbScale = 0.3F;
        GlStateManager.scale(orbScale, orbScale, orbScale);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        int red = redPulse;
        int green = 255;
        int blue = bluePulse;

        if (Config.isCustomColors())
        {
            int customOrbColor = CustomColors.getXpOrbColor(colorTimer);

            if (customOrbColor >= 0)
            {
                red = customOrbColor >> 16 & 255;
                green = customOrbColor >> 8 & 255;
                blue = customOrbColor >> 0 & 255;
            }
        }

        worldRenderer.pos((double)(0.0F - halfQuadSize), (double)(0.0F - quadYOffset), 0.0D).tex((double)minU, (double)maxV).color(red, green, blue, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos((double)(quadSize - halfQuadSize), (double)(0.0F - quadYOffset), 0.0D).tex((double)maxU, (double)maxV).color(red, green, blue, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos((double)(quadSize - halfQuadSize), (double)(1.0F - quadYOffset), 0.0D).tex((double)maxU, (double)minV).color(red, green, blue, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos((double)(0.0F - halfQuadSize), (double)(1.0F - quadYOffset), 0.0D).tex((double)minU, (double)minV).color(red, green, blue, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityXPOrb entity)
    {
        return experienceOrbTextures;
    }
}
