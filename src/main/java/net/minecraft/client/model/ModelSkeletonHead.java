package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelSkeletonHead extends ModelBase
{
    public ModelRenderer skeletonHead;

    public ModelSkeletonHead()
    {
        this(0, 35, 64, 64);
    }

    public ModelSkeletonHead(int textureOffsetX, int textureOffsetY, int textureWidthIn, int textureHeightIn)
    {
        this.textureWidth = textureWidthIn;
        this.textureHeight = textureHeightIn;
        this.skeletonHead = new ModelRenderer(this, textureOffsetX, textureOffsetY);
        this.skeletonHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
        this.skeletonHead.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.skeletonHead.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        this.skeletonHead.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.skeletonHead.rotateAngleX = headPitch / (180F / (float)Math.PI);
    }
}
