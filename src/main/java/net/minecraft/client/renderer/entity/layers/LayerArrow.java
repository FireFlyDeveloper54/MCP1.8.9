package net.minecraft.client.renderer.entity.layers;

import java.util.Random;
import optimization.FastTrig;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MathHelper;

public class LayerArrow implements LayerRenderer<EntityLivingBase>
{
    private final RendererLivingEntity renderer;
    private final Random random = new Random();

    public LayerArrow(RendererLivingEntity rendererIn)
    {
        this.renderer = rendererIn;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        int arrowCount = entitylivingbaseIn.getArrowCountInEntity();

        if (arrowCount > 0)
        {
            Entity arrowEntity = new EntityArrow(entitylivingbaseIn.worldObj, entitylivingbaseIn.posX, entitylivingbaseIn.posY, entitylivingbaseIn.posZ);
            Random random = this.random;
            random.setSeed((long)entitylivingbaseIn.getEntityId());
            RenderHelper.disableStandardItemLighting();

            for (int arrowIndex = 0; arrowIndex < arrowCount; ++arrowIndex)
            {
                GlStateManager.pushMatrix();
                ModelRenderer modelRenderer = this.renderer.getMainModel().getRandomModelBox(random);
                ModelBox modelBox = modelRenderer.cubeList.get(random.nextInt(modelRenderer.cubeList.size()));
                modelRenderer.postRender(0.0625F);
                float randomX = random.nextFloat();
                float randomY = random.nextFloat();
                float randomZ = random.nextFloat();
                float arrowX = (modelBox.posX1 + (modelBox.posX2 - modelBox.posX1) * randomX) / 16.0F;
                float arrowY = (modelBox.posY1 + (modelBox.posY2 - modelBox.posY1) * randomY) / 16.0F;
                float arrowZ = (modelBox.posZ1 + (modelBox.posZ2 - modelBox.posZ1) * randomZ) / 16.0F;
                GlStateManager.translate(arrowX, arrowY, arrowZ);
                float directionX = randomX * 2.0F - 1.0F;
                float directionY = randomY * 2.0F - 1.0F;
                float directionZ = randomZ * 2.0F - 1.0F;
                directionX = directionX * -1.0F;
                directionY = directionY * -1.0F;
                directionZ = directionZ * -1.0F;
                float horizontalLength = MathHelper.sqrt_float(directionX * directionX + directionZ * directionZ);
                arrowEntity.prevRotationYaw = arrowEntity.rotationYaw = (float)(FastTrig.atan2((double)directionX, (double)directionZ) * 180.0D / Math.PI);
                arrowEntity.prevRotationPitch = arrowEntity.rotationPitch = (float)(FastTrig.atan2((double)directionY, (double)horizontalLength) * 180.0D / Math.PI);
                this.renderer.getRenderManager().renderEntityWithPosYaw(arrowEntity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks);
                GlStateManager.popMatrix();
            }

            RenderHelper.enableStandardItemLighting();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
