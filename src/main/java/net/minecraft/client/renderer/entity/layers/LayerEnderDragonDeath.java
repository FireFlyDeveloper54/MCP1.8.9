package net.minecraft.client.renderer.entity.layers;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.boss.EntityDragon;

public class LayerEnderDragonDeath implements LayerRenderer<EntityDragon>
{
    private final Random random = new Random();

    public void doRenderLayer(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (entitylivingbaseIn.deathTicks > 0)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            RenderHelper.disableStandardItemLighting();
            float deathProgress = ((float)entitylivingbaseIn.deathTicks + partialTicks) / 200.0F;
            float fadeProgress = 0.0F;

            if (deathProgress > 0.8F)
            {
                fadeProgress = (deathProgress - 0.8F) / 0.2F;
            }

            Random random = this.random;
            random.setSeed(432L);
            GlStateManager.disableTexture2D();
            GlStateManager.shadeModel(7425);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 1);
            GlStateManager.disableAlpha();
            GlStateManager.enableCull();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -1.0F, -2.0F);

            for (int rayIndex = 0; (float)rayIndex < (deathProgress + deathProgress * deathProgress) / 2.0F * 60.0F; ++rayIndex)
            {
                GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F + deathProgress * 90.0F, 0.0F, 0.0F, 1.0F);
                float rayLength = random.nextFloat() * 20.0F + 5.0F + fadeProgress * 10.0F;
                float rayWidth = random.nextFloat() * 2.0F + 1.0F + fadeProgress * 2.0F;
                worldRenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldRenderer.pos(0.0D, 0.0D, 0.0D).color(255, 255, 255, (int)(255.0F * (1.0F - fadeProgress))).endVertex();
                worldRenderer.pos(-0.866D * (double)rayWidth, (double)rayLength, (double)(-0.5F * rayWidth)).color(255, 0, 255, 0).endVertex();
                worldRenderer.pos(0.866D * (double)rayWidth, (double)rayLength, (double)(-0.5F * rayWidth)).color(255, 0, 255, 0).endVertex();
                worldRenderer.pos(0.0D, (double)rayLength, (double)(1.0F * rayWidth)).color(255, 0, 255, 0).endVertex();
                worldRenderer.pos(-0.866D * (double)rayWidth, (double)rayLength, (double)(-0.5F * rayWidth)).color(255, 0, 255, 0).endVertex();
                tessellator.draw();
            }

            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            GlStateManager.shadeModel(7424);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
