package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelEnderman extends ModelBiped
{
    public boolean isCarrying;
    public boolean isAttacking;

    public ModelEnderman(float modelSize)
    {
        super(0.0F, -14.0F, 64, 32);
        float verticalOffset = -14.0F;
        this.bipedHeadwear = new ModelRenderer(this, 0, 16);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize - 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F + verticalOffset, 0.0F);
        this.bipedBody = new ModelRenderer(this, 32, 16);
        this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + verticalOffset, 0.0F);
        this.bipedRightArm = new ModelRenderer(this, 56, 0);
        this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, modelSize);
        this.bipedRightArm.setRotationPoint(-3.0F, 2.0F + verticalOffset, 0.0F);
        this.bipedLeftArm = new ModelRenderer(this, 56, 0);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 30, 2, modelSize);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + verticalOffset, 0.0F);
        this.bipedRightLeg = new ModelRenderer(this, 56, 0);
        this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, modelSize);
        this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F + verticalOffset, 0.0F);
        this.bipedLeftLeg = new ModelRenderer(this, 56, 0);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 30, 2, modelSize);
        this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F + verticalOffset, 0.0F);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        this.bipedHead.showModel = true;
        float verticalOffset = -14.0F;
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedBody.rotationPointY = verticalOffset;
        this.bipedBody.rotationPointZ = -0.0F;
        this.bipedRightLeg.rotateAngleX -= 0.0F;
        this.bipedLeftLeg.rotateAngleX -= 0.0F;
        this.bipedRightArm.rotateAngleX = (float)((double)this.bipedRightArm.rotateAngleX * 0.5D);
        this.bipedLeftArm.rotateAngleX = (float)((double)this.bipedLeftArm.rotateAngleX * 0.5D);
        this.bipedRightLeg.rotateAngleX = (float)((double)this.bipedRightLeg.rotateAngleX * 0.5D);
        this.bipedLeftLeg.rotateAngleX = (float)((double)this.bipedLeftLeg.rotateAngleX * 0.5D);
        float maxLimbAngle = 0.4F;

        if (this.bipedRightArm.rotateAngleX > maxLimbAngle)
        {
            this.bipedRightArm.rotateAngleX = maxLimbAngle;
        }

        if (this.bipedLeftArm.rotateAngleX > maxLimbAngle)
        {
            this.bipedLeftArm.rotateAngleX = maxLimbAngle;
        }

        if (this.bipedRightArm.rotateAngleX < -maxLimbAngle)
        {
            this.bipedRightArm.rotateAngleX = -maxLimbAngle;
        }

        if (this.bipedLeftArm.rotateAngleX < -maxLimbAngle)
        {
            this.bipedLeftArm.rotateAngleX = -maxLimbAngle;
        }

        if (this.bipedRightLeg.rotateAngleX > maxLimbAngle)
        {
            this.bipedRightLeg.rotateAngleX = maxLimbAngle;
        }

        if (this.bipedLeftLeg.rotateAngleX > maxLimbAngle)
        {
            this.bipedLeftLeg.rotateAngleX = maxLimbAngle;
        }

        if (this.bipedRightLeg.rotateAngleX < -maxLimbAngle)
        {
            this.bipedRightLeg.rotateAngleX = -maxLimbAngle;
        }

        if (this.bipedLeftLeg.rotateAngleX < -maxLimbAngle)
        {
            this.bipedLeftLeg.rotateAngleX = -maxLimbAngle;
        }

        if (this.isCarrying)
        {
            this.bipedRightArm.rotateAngleX = -0.5F;
            this.bipedLeftArm.rotateAngleX = -0.5F;
            this.bipedRightArm.rotateAngleZ = 0.05F;
            this.bipedLeftArm.rotateAngleZ = -0.05F;
        }

        this.bipedRightArm.rotationPointZ = 0.0F;
        this.bipedLeftArm.rotationPointZ = 0.0F;
        this.bipedRightLeg.rotationPointZ = 0.0F;
        this.bipedLeftLeg.rotationPointZ = 0.0F;
        this.bipedRightLeg.rotationPointY = 9.0F + verticalOffset;
        this.bipedLeftLeg.rotationPointY = 9.0F + verticalOffset;
        this.bipedHead.rotationPointZ = -0.0F;
        this.bipedHead.rotationPointY = verticalOffset + 1.0F;
        this.bipedHeadwear.rotationPointX = this.bipedHead.rotationPointX;
        this.bipedHeadwear.rotationPointY = this.bipedHead.rotationPointY;
        this.bipedHeadwear.rotationPointZ = this.bipedHead.rotationPointZ;
        this.bipedHeadwear.rotateAngleX = this.bipedHead.rotateAngleX;
        this.bipedHeadwear.rotateAngleY = this.bipedHead.rotateAngleY;
        this.bipedHeadwear.rotateAngleZ = this.bipedHead.rotateAngleZ;

        if (this.isAttacking)
        {
            float attackHeadOffsetScale = 1.0F;
            this.bipedHead.rotationPointY -= attackHeadOffsetScale * 5.0F;
        }
    }
}
