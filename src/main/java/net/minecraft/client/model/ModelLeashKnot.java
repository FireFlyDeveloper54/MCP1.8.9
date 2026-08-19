package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelLeashKnot extends ModelBase
{
    public ModelRenderer knotRenderer;

    public ModelLeashKnot()
    {
        this(0, 0, 32, 32);
    }

    public ModelLeashKnot(int textureOffsetX, int textureOffsetY, int textureWidthIn, int textureHeightIn)
    {
        this.textureWidth = textureWidthIn;
        this.textureHeight = textureHeightIn;
        this.knotRenderer = new ModelRenderer(this, textureOffsetX, textureOffsetY);
        this.knotRenderer.addBox(-3.0F, -6.0F, -3.0F, 6, 8, 6, 0.0F);
        this.knotRenderer.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.knotRenderer.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        this.knotRenderer.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.knotRenderer.rotateAngleX = headPitch / (180F / (float)Math.PI);
    }
}
