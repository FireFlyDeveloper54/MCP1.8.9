package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMagmaCube;

public class ModelMagmaCube extends ModelBase
{
    ModelRenderer[] segments = new ModelRenderer[8];
    ModelRenderer core;

    public ModelMagmaCube()
    {
        for (int segmentIndex = 0; segmentIndex < this.segments.length; ++segmentIndex)
        {
            int textureX = 0;
            int textureY = segmentIndex;

            if (segmentIndex == 2)
            {
                textureX = 24;
                textureY = 10;
            }
            else if (segmentIndex == 3)
            {
                textureX = 24;
                textureY = 19;
            }

            this.segments[segmentIndex] = new ModelRenderer(this, textureX, textureY);
            this.segments[segmentIndex].addBox(-4.0F, (float)(16 + segmentIndex), -4.0F, 8, 1, 8);
        }

        this.core = new ModelRenderer(this, 0, 16);
        this.core.addBox(-2.0F, 18.0F, -2.0F, 4, 4, 4);
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        EntityMagmaCube entityMagmaCube = (EntityMagmaCube)entitylivingbaseIn;
        float squish = entityMagmaCube.prevSquishFactor + (entityMagmaCube.squishFactor - entityMagmaCube.prevSquishFactor) * partialTickTime;

        if (squish < 0.0F)
        {
            squish = 0.0F;
        }

        for (int segmentIndex = 0; segmentIndex < this.segments.length; ++segmentIndex)
        {
            this.segments[segmentIndex].rotationPointY = (float)(-(4 - segmentIndex)) * squish * 1.7F;
        }
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.core.render(scale);

        for (int segmentIndex = 0; segmentIndex < this.segments.length; ++segmentIndex)
        {
            this.segments[segmentIndex].render(scale);
        }
    }
}
