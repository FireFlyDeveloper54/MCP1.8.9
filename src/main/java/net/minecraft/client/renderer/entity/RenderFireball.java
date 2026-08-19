package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

public class RenderFireball extends Render<EntityFireball>
{
    private float scale;

    public RenderFireball(RenderManager renderManagerIn, float scaleIn)
    {
        super(renderManagerIn);
        this.scale = scaleIn;
    }

    public void doRender(EntityFireball entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        this.bindEntityTexture(entity);
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(this.scale, this.scale, this.scale);
        TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.fire_charge);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        float minU = textureAtlasSprite.getMinU();
        float maxU = textureAtlasSprite.getMaxU();
        float minV = textureAtlasSprite.getMinV();
        float maxV = textureAtlasSprite.getMaxV();
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        worldRenderer.pos(-0.5D, -0.25D, 0.0D).tex((double)minU, (double)maxV).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(0.5D, -0.25D, 0.0D).tex((double)maxU, (double)maxV).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(0.5D, 0.75D, 0.0D).tex((double)maxU, (double)minV).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(-0.5D, 0.75D, 0.0D).tex((double)minU, (double)minV).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityFireball entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
