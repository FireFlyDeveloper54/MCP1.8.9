package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelSpider extends ModelBase
{
    public ModelRenderer spiderHead;
    public ModelRenderer spiderNeck;
    public ModelRenderer spiderBody;
    public ModelRenderer spiderLeg1;
    public ModelRenderer spiderLeg2;
    public ModelRenderer spiderLeg3;
    public ModelRenderer spiderLeg4;
    public ModelRenderer spiderLeg5;
    public ModelRenderer spiderLeg6;
    public ModelRenderer spiderLeg7;
    public ModelRenderer spiderLeg8;

    public ModelSpider()
    {
        float modelScale = 0.0F;
        int bodyYOffset = 15;
        this.spiderHead = new ModelRenderer(this, 32, 4);
        this.spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, modelScale);
        this.spiderHead.setRotationPoint(0.0F, (float)bodyYOffset, -3.0F);
        this.spiderNeck = new ModelRenderer(this, 0, 0);
        this.spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, modelScale);
        this.spiderNeck.setRotationPoint(0.0F, (float)bodyYOffset, 0.0F);
        this.spiderBody = new ModelRenderer(this, 0, 12);
        this.spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, modelScale);
        this.spiderBody.setRotationPoint(0.0F, (float)bodyYOffset, 9.0F);
        this.spiderLeg1 = new ModelRenderer(this, 18, 0);
        this.spiderLeg1.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg1.setRotationPoint(-4.0F, (float)bodyYOffset, 2.0F);
        this.spiderLeg2 = new ModelRenderer(this, 18, 0);
        this.spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg2.setRotationPoint(4.0F, (float)bodyYOffset, 2.0F);
        this.spiderLeg3 = new ModelRenderer(this, 18, 0);
        this.spiderLeg3.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg3.setRotationPoint(-4.0F, (float)bodyYOffset, 1.0F);
        this.spiderLeg4 = new ModelRenderer(this, 18, 0);
        this.spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg4.setRotationPoint(4.0F, (float)bodyYOffset, 1.0F);
        this.spiderLeg5 = new ModelRenderer(this, 18, 0);
        this.spiderLeg5.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg5.setRotationPoint(-4.0F, (float)bodyYOffset, 0.0F);
        this.spiderLeg6 = new ModelRenderer(this, 18, 0);
        this.spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg6.setRotationPoint(4.0F, (float)bodyYOffset, 0.0F);
        this.spiderLeg7 = new ModelRenderer(this, 18, 0);
        this.spiderLeg7.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg7.setRotationPoint(-4.0F, (float)bodyYOffset, -1.0F);
        this.spiderLeg8 = new ModelRenderer(this, 18, 0);
        this.spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, modelScale);
        this.spiderLeg8.setRotationPoint(4.0F, (float)bodyYOffset, -1.0F);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.spiderHead.render(scale);
        this.spiderNeck.render(scale);
        this.spiderBody.render(scale);
        this.spiderLeg1.render(scale);
        this.spiderLeg2.render(scale);
        this.spiderLeg3.render(scale);
        this.spiderLeg4.render(scale);
        this.spiderLeg5.render(scale);
        this.spiderLeg6.render(scale);
        this.spiderLeg7.render(scale);
        this.spiderLeg8.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        this.spiderHead.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.spiderHead.rotateAngleX = headPitch / (180F / (float)Math.PI);
        float baseRollAngle = ((float)Math.PI / 4F);
        this.spiderLeg1.rotateAngleZ = -baseRollAngle;
        this.spiderLeg2.rotateAngleZ = baseRollAngle;
        this.spiderLeg3.rotateAngleZ = -baseRollAngle * 0.74F;
        this.spiderLeg4.rotateAngleZ = baseRollAngle * 0.74F;
        this.spiderLeg5.rotateAngleZ = -baseRollAngle * 0.74F;
        this.spiderLeg6.rotateAngleZ = baseRollAngle * 0.74F;
        this.spiderLeg7.rotateAngleZ = -baseRollAngle;
        this.spiderLeg8.rotateAngleZ = baseRollAngle;
        float neutralYawOffset = -0.0F;
        float baseYawAngle = 0.3926991F;
        this.spiderLeg1.rotateAngleY = baseYawAngle * 2.0F + neutralYawOffset;
        this.spiderLeg2.rotateAngleY = -baseYawAngle * 2.0F - neutralYawOffset;
        this.spiderLeg3.rotateAngleY = baseYawAngle * 1.0F + neutralYawOffset;
        this.spiderLeg4.rotateAngleY = -baseYawAngle * 1.0F - neutralYawOffset;
        this.spiderLeg5.rotateAngleY = -baseYawAngle * 1.0F + neutralYawOffset;
        this.spiderLeg6.rotateAngleY = baseYawAngle * 1.0F - neutralYawOffset;
        this.spiderLeg7.rotateAngleY = -baseYawAngle * 2.0F + neutralYawOffset;
        this.spiderLeg8.rotateAngleY = baseYawAngle * 2.0F - neutralYawOffset;
        float yawSwingPhase0 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
        float yawSwingPhasePi = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * limbSwingAmount;
        float yawSwingPhaseHalfPi = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float yawSwingPhaseThreeHalfPi = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + ((float)Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        float rollSwingPhase0 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + 0.0F) * 0.4F) * limbSwingAmount;
        float rollSwingPhasePi = Math.abs(MathHelper.sin(limbSwing * 0.6662F + (float)Math.PI) * 0.4F) * limbSwingAmount;
        float rollSwingPhaseHalfPi = Math.abs(MathHelper.sin(limbSwing * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float rollSwingPhaseThreeHalfPi = Math.abs(MathHelper.sin(limbSwing * 0.6662F + ((float)Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        this.spiderLeg1.rotateAngleY += yawSwingPhase0;
        this.spiderLeg2.rotateAngleY += -yawSwingPhase0;
        this.spiderLeg3.rotateAngleY += yawSwingPhasePi;
        this.spiderLeg4.rotateAngleY += -yawSwingPhasePi;
        this.spiderLeg5.rotateAngleY += yawSwingPhaseHalfPi;
        this.spiderLeg6.rotateAngleY += -yawSwingPhaseHalfPi;
        this.spiderLeg7.rotateAngleY += yawSwingPhaseThreeHalfPi;
        this.spiderLeg8.rotateAngleY += -yawSwingPhaseThreeHalfPi;
        this.spiderLeg1.rotateAngleZ += rollSwingPhase0;
        this.spiderLeg2.rotateAngleZ += -rollSwingPhase0;
        this.spiderLeg3.rotateAngleZ += rollSwingPhasePi;
        this.spiderLeg4.rotateAngleZ += -rollSwingPhasePi;
        this.spiderLeg5.rotateAngleZ += rollSwingPhaseHalfPi;
        this.spiderLeg6.rotateAngleZ += -rollSwingPhaseHalfPi;
        this.spiderLeg7.rotateAngleZ += rollSwingPhaseThreeHalfPi;
        this.spiderLeg8.rotateAngleZ += -rollSwingPhaseThreeHalfPi;
    }
}
