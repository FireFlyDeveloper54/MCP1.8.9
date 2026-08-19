package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.MathHelper;

public class ModelWither extends ModelBase
{
    private ModelRenderer[] bodyParts;
    private ModelRenderer[] heads;

    public ModelWither(float modelSize)
    {
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.bodyParts = new ModelRenderer[3];
        this.bodyParts[0] = new ModelRenderer(this, 0, 16);
        this.bodyParts[0].addBox(-10.0F, 3.9F, -0.5F, 20, 3, 3, modelSize);
        this.bodyParts[1] = (new ModelRenderer(this)).setTextureSize(this.textureWidth, this.textureHeight);
        this.bodyParts[1].setRotationPoint(-2.0F, 6.9F, -0.5F);
        this.bodyParts[1].setTextureOffset(0, 22).addBox(0.0F, 0.0F, 0.0F, 3, 10, 3, modelSize);
        this.bodyParts[1].setTextureOffset(24, 22).addBox(-4.0F, 1.5F, 0.5F, 11, 2, 2, modelSize);
        this.bodyParts[1].setTextureOffset(24, 22).addBox(-4.0F, 4.0F, 0.5F, 11, 2, 2, modelSize);
        this.bodyParts[1].setTextureOffset(24, 22).addBox(-4.0F, 6.5F, 0.5F, 11, 2, 2, modelSize);
        this.bodyParts[2] = new ModelRenderer(this, 12, 22);
        this.bodyParts[2].addBox(0.0F, 0.0F, 0.0F, 3, 6, 3, modelSize);
        this.heads = new ModelRenderer[3];
        this.heads[0] = new ModelRenderer(this, 0, 0);
        this.heads[0].addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, modelSize);
        this.heads[1] = new ModelRenderer(this, 32, 0);
        this.heads[1].addBox(-4.0F, -4.0F, -4.0F, 6, 6, 6, modelSize);
        this.heads[1].rotationPointX = -8.0F;
        this.heads[1].rotationPointY = 4.0F;
        this.heads[2] = new ModelRenderer(this, 32, 0);
        this.heads[2].addBox(-4.0F, -4.0F, -4.0F, 6, 6, 6, modelSize);
        this.heads[2].rotationPointX = 10.0F;
        this.heads[2].rotationPointY = 4.0F;
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        for (ModelRenderer modelRenderer : this.heads)
        {
            modelRenderer.render(scale);
        }

        for (ModelRenderer bodyPart : this.bodyParts)
        {
            bodyPart.render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        float bodyWave = MathHelper.cos(ageInTicks * 0.1F);
        this.bodyParts[1].rotateAngleX = (0.065F + 0.05F * bodyWave) * (float)Math.PI;
        this.bodyParts[2].setRotationPoint(-2.0F, 6.9F + MathHelper.cos(this.bodyParts[1].rotateAngleX) * 10.0F, -0.5F + MathHelper.sin(this.bodyParts[1].rotateAngleX) * 10.0F);
        this.bodyParts[2].rotateAngleX = (0.265F + 0.1F * bodyWave) * (float)Math.PI;
        this.heads[0].rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.heads[0].rotateAngleX = headPitch / (180F / (float)Math.PI);
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        EntityWither entityWither = (EntityWither)entitylivingbaseIn;

        for (int headIndex = 1; headIndex < 3; ++headIndex)
        {
            this.heads[headIndex].rotateAngleY = (entityWither.getHeadYRotation(headIndex - 1) - entitylivingbaseIn.renderYawOffset) / (180F / (float)Math.PI);
            this.heads[headIndex].rotateAngleX = entityWither.getHeadXRotation(headIndex - 1) / (180F / (float)Math.PI);
        }
    }
}
