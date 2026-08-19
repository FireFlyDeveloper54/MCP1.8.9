package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

public class ModelArmorStandArmor extends ModelBiped
{
    public ModelArmorStandArmor()
    {
        this(0.0F);
    }

    public ModelArmorStandArmor(float modelSize)
    {
        this(modelSize, 64, 32);
    }

    protected ModelArmorStandArmor(float modelSize, int textureWidthIn, int textureHeightIn)
    {
        super(modelSize, 0.0F, textureWidthIn, textureHeightIn);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        if (entityIn instanceof EntityArmorStand)
        {
            EntityArmorStand entityArmorStand = (EntityArmorStand)entityIn;
            this.bipedHead.rotateAngleX = 0.017453292F * entityArmorStand.getHeadRotation().getX();
            this.bipedHead.rotateAngleY = 0.017453292F * entityArmorStand.getHeadRotation().getY();
            this.bipedHead.rotateAngleZ = 0.017453292F * entityArmorStand.getHeadRotation().getZ();
            this.bipedHead.setRotationPoint(0.0F, 1.0F, 0.0F);
            this.bipedBody.rotateAngleX = 0.017453292F * entityArmorStand.getBodyRotation().getX();
            this.bipedBody.rotateAngleY = 0.017453292F * entityArmorStand.getBodyRotation().getY();
            this.bipedBody.rotateAngleZ = 0.017453292F * entityArmorStand.getBodyRotation().getZ();
            this.bipedLeftArm.rotateAngleX = 0.017453292F * entityArmorStand.getLeftArmRotation().getX();
            this.bipedLeftArm.rotateAngleY = 0.017453292F * entityArmorStand.getLeftArmRotation().getY();
            this.bipedLeftArm.rotateAngleZ = 0.017453292F * entityArmorStand.getLeftArmRotation().getZ();
            this.bipedRightArm.rotateAngleX = 0.017453292F * entityArmorStand.getRightArmRotation().getX();
            this.bipedRightArm.rotateAngleY = 0.017453292F * entityArmorStand.getRightArmRotation().getY();
            this.bipedRightArm.rotateAngleZ = 0.017453292F * entityArmorStand.getRightArmRotation().getZ();
            this.bipedLeftLeg.rotateAngleX = 0.017453292F * entityArmorStand.getLeftLegRotation().getX();
            this.bipedLeftLeg.rotateAngleY = 0.017453292F * entityArmorStand.getLeftLegRotation().getY();
            this.bipedLeftLeg.rotateAngleZ = 0.017453292F * entityArmorStand.getLeftLegRotation().getZ();
            this.bipedLeftLeg.setRotationPoint(1.9F, 11.0F, 0.0F);
            this.bipedRightLeg.rotateAngleX = 0.017453292F * entityArmorStand.getRightLegRotation().getX();
            this.bipedRightLeg.rotateAngleY = 0.017453292F * entityArmorStand.getRightLegRotation().getY();
            this.bipedRightLeg.rotateAngleZ = 0.017453292F * entityArmorStand.getRightLegRotation().getZ();
            this.bipedRightLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);
            copyModelAngles(this.bipedHead, this.bipedHeadwear);
        }
    }
}
