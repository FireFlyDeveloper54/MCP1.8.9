package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelZombie extends ModelBiped
{
    public ModelZombie()
    {
        this(0.0F, false);
    }

    protected ModelZombie(float modelSize, float yOffset, int textureWidthIn, int textureHeightIn)
    {
        super(modelSize, yOffset, textureWidthIn, textureHeightIn);
    }

    public ModelZombie(float modelSize, boolean armorModel)
    {
        super(modelSize, 0.0F, 64, armorModel ? 32 : 64);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        float swingSin = MathHelper.sin(this.swingProgress * (float)Math.PI);
        float swingEase = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedRightArm.rotateAngleY = -(0.1F - swingSin * 0.6F);
        this.bipedLeftArm.rotateAngleY = 0.1F - swingSin * 0.6F;
        this.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F);
        this.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F);
        this.bipedRightArm.rotateAngleX -= swingSin * 1.2F - swingEase * 0.4F;
        this.bipedLeftArm.rotateAngleX -= swingSin * 1.2F - swingEase * 0.4F;
        this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }
}
