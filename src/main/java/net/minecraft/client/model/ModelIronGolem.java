package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;

public class ModelIronGolem extends ModelBase
{
    public ModelRenderer ironGolemHead;
    public ModelRenderer ironGolemBody;
    public ModelRenderer ironGolemRightArm;
    public ModelRenderer ironGolemLeftArm;
    public ModelRenderer ironGolemLeftLeg;
    public ModelRenderer ironGolemRightLeg;

    public ModelIronGolem()
    {
        this(0.0F);
    }

    public ModelIronGolem(float modelSize)
    {
        this(modelSize, -7.0F);
    }

    public ModelIronGolem(float modelSize, float yOffset)
    {
        int textureWidth = 128;
        int textureHeight = 128;
        this.ironGolemHead = (new ModelRenderer(this)).setTextureSize(textureWidth, textureHeight);
        this.ironGolemHead.setRotationPoint(0.0F, 0.0F + yOffset, -2.0F);
        this.ironGolemHead.setTextureOffset(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8, 10, 8, modelSize);
        this.ironGolemHead.setTextureOffset(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2, 4, 2, modelSize);
        this.ironGolemBody = (new ModelRenderer(this)).setTextureSize(textureWidth, textureHeight);
        this.ironGolemBody.setRotationPoint(0.0F, 0.0F + yOffset, 0.0F);
        this.ironGolemBody.setTextureOffset(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18, 12, 11, modelSize);
        this.ironGolemBody.setTextureOffset(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9, 5, 6, modelSize + 0.5F);
        this.ironGolemRightArm = (new ModelRenderer(this)).setTextureSize(textureWidth, textureHeight);
        this.ironGolemRightArm.setRotationPoint(0.0F, -7.0F, 0.0F);
        this.ironGolemRightArm.setTextureOffset(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4, 30, 6, modelSize);
        this.ironGolemLeftArm = (new ModelRenderer(this)).setTextureSize(textureWidth, textureHeight);
        this.ironGolemLeftArm.setRotationPoint(0.0F, -7.0F, 0.0F);
        this.ironGolemLeftArm.setTextureOffset(60, 58).addBox(9.0F, -2.5F, -3.0F, 4, 30, 6, modelSize);
        this.ironGolemLeftLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(textureWidth, textureHeight);
        this.ironGolemLeftLeg.setRotationPoint(-4.0F, 18.0F + yOffset, 0.0F);
        this.ironGolemLeftLeg.setTextureOffset(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, modelSize);
        this.ironGolemRightLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(textureWidth, textureHeight);
        this.ironGolemRightLeg.mirror = true;
        this.ironGolemRightLeg.setTextureOffset(60, 0).setRotationPoint(5.0F, 18.0F + yOffset, 0.0F);
        this.ironGolemRightLeg.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, modelSize);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.ironGolemHead.render(scale);
        this.ironGolemBody.render(scale);
        this.ironGolemLeftLeg.render(scale);
        this.ironGolemRightLeg.render(scale);
        this.ironGolemRightArm.render(scale);
        this.ironGolemLeftArm.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        this.ironGolemHead.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.ironGolemHead.rotateAngleX = headPitch / (180F / (float)Math.PI);
        this.ironGolemLeftLeg.rotateAngleX = -1.5F * this.getTriangleWave(limbSwing, 13.0F) * limbSwingAmount;
        this.ironGolemRightLeg.rotateAngleX = 1.5F * this.getTriangleWave(limbSwing, 13.0F) * limbSwingAmount;
        this.ironGolemLeftLeg.rotateAngleY = 0.0F;
        this.ironGolemRightLeg.rotateAngleY = 0.0F;
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        EntityIronGolem entityIronGolem = (EntityIronGolem)entitylivingbaseIn;
        int attackTimer = entityIronGolem.getAttackTimer();

        if (attackTimer > 0)
        {
            this.ironGolemRightArm.rotateAngleX = -2.0F + 1.5F * this.getTriangleWave((float)attackTimer - partialTickTime, 10.0F);
            this.ironGolemLeftArm.rotateAngleX = -2.0F + 1.5F * this.getTriangleWave((float)attackTimer - partialTickTime, 10.0F);
        }
        else
        {
            int holdRoseTicks = entityIronGolem.getHoldRoseTick();

            if (holdRoseTicks > 0)
            {
                this.ironGolemRightArm.rotateAngleX = -0.8F + 0.025F * this.getTriangleWave((float)holdRoseTicks, 70.0F);
                this.ironGolemLeftArm.rotateAngleX = 0.0F;
            }
            else
            {
                this.ironGolemRightArm.rotateAngleX = (-0.2F + 1.5F * this.getTriangleWave(limbSwing, 13.0F)) * limbSwingAmount;
                this.ironGolemLeftArm.rotateAngleX = (-0.2F - 1.5F * this.getTriangleWave(limbSwing, 13.0F)) * limbSwingAmount;
            }
        }
    }

    private float getTriangleWave(float value, float period)
    {
        return (Math.abs(value % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }
}
