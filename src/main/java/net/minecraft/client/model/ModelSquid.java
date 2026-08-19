package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelSquid extends ModelBase
{
    ModelRenderer squidBody;
    ModelRenderer[] squidTentacles = new ModelRenderer[8];

    public ModelSquid()
    {
        int yOffset = -16;
        this.squidBody = new ModelRenderer(this, 0, 0);
        this.squidBody.addBox(-6.0F, -8.0F, -6.0F, 12, 16, 12);
        this.squidBody.rotationPointY += (float)(24 + yOffset);

        for (int tentacleIndex = 0; tentacleIndex < this.squidTentacles.length; ++tentacleIndex)
        {
            this.squidTentacles[tentacleIndex] = new ModelRenderer(this, 48, 0);
            double tentacleAngle = (double)tentacleIndex * Math.PI * 2.0D / (double)this.squidTentacles.length;
            float tentacleX = (float)Math.cos(tentacleAngle) * 5.0F;
            float tentacleZ = (float)Math.sin(tentacleAngle) * 5.0F;
            this.squidTentacles[tentacleIndex].addBox(-1.0F, 0.0F, -1.0F, 2, 18, 2);
            this.squidTentacles[tentacleIndex].rotationPointX = tentacleX;
            this.squidTentacles[tentacleIndex].rotationPointZ = tentacleZ;
            this.squidTentacles[tentacleIndex].rotationPointY = (float)(31 + yOffset);
            tentacleAngle = (double)tentacleIndex * Math.PI * -2.0D / (double)this.squidTentacles.length + (Math.PI / 2D);
            this.squidTentacles[tentacleIndex].rotateAngleY = (float)tentacleAngle;
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        for (ModelRenderer modelRenderer : this.squidTentacles)
        {
            modelRenderer.rotateAngleX = ageInTicks;
        }
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.squidBody.render(scale);

        for (int tentacleIndex = 0; tentacleIndex < this.squidTentacles.length; ++tentacleIndex)
        {
            this.squidTentacles[tentacleIndex].render(scale);
        }
    }
}
