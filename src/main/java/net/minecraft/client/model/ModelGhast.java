package net.minecraft.client.model;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelGhast extends ModelBase
{
    ModelRenderer body;
    ModelRenderer[] tentacles = new ModelRenderer[9];

    public ModelGhast()
    {
        int yOffset = -16;
        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
        this.body.rotationPointY += (float)(24 + yOffset);
        Random random = new Random(1660L);

        for (int tentacleIndex = 0; tentacleIndex < this.tentacles.length; ++tentacleIndex)
        {
            this.tentacles[tentacleIndex] = new ModelRenderer(this, 0, 0);
            float tentacleX = (((float)(tentacleIndex % 3) - (float)(tentacleIndex / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float tentacleZ = ((float)(tentacleIndex / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int tentacleLength = random.nextInt(7) + 8;
            this.tentacles[tentacleIndex].addBox(-1.0F, 0.0F, -1.0F, 2, tentacleLength, 2);
            this.tentacles[tentacleIndex].rotationPointX = tentacleX;
            this.tentacles[tentacleIndex].rotationPointZ = tentacleZ;
            this.tentacles[tentacleIndex].rotationPointY = (float)(31 + yOffset);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        for (int tentacleIndex = 0; tentacleIndex < this.tentacles.length; ++tentacleIndex)
        {
            this.tentacles[tentacleIndex].rotateAngleX = 0.2F * MathHelper.sin(ageInTicks * 0.3F + (float)tentacleIndex) + 0.4F;
        }
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.6F, 0.0F);
        this.body.render(scale);

        for (ModelRenderer modelRenderer : this.tentacles)
        {
            modelRenderer.render(scale);
        }

        GlStateManager.popMatrix();
    }
}
