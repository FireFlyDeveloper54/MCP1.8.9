package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBlaze extends ModelBase
{
    private ModelRenderer[] blazeSticks = new ModelRenderer[12];
    private ModelRenderer blazeHead;

    public ModelBlaze()
    {
        for (int stickIndex = 0; stickIndex < this.blazeSticks.length; ++stickIndex)
        {
            this.blazeSticks[stickIndex] = new ModelRenderer(this, 0, 16);
            this.blazeSticks[stickIndex].addBox(0.0F, 0.0F, 0.0F, 2, 8, 2);
        }

        this.blazeHead = new ModelRenderer(this, 0, 0);
        this.blazeHead.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.blazeHead.render(scale);

        for (int stickIndex = 0; stickIndex < this.blazeSticks.length; ++stickIndex)
        {
            this.blazeSticks[stickIndex].render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        float orbitAngle = ageInTicks * (float)Math.PI * -0.1F;

        for (int stickIndex = 0; stickIndex < 4; ++stickIndex)
        {
            this.blazeSticks[stickIndex].rotationPointY = -2.0F + MathHelper.cos(((float)(stickIndex * 2) + ageInTicks) * 0.25F);
            this.blazeSticks[stickIndex].rotationPointX = MathHelper.cos(orbitAngle) * 9.0F;
            this.blazeSticks[stickIndex].rotationPointZ = MathHelper.sin(orbitAngle) * 9.0F;
            ++orbitAngle;
        }

        orbitAngle = ((float)Math.PI / 4F) + ageInTicks * (float)Math.PI * 0.03F;

        for (int stickIndex = 4; stickIndex < 8; ++stickIndex)
        {
            this.blazeSticks[stickIndex].rotationPointY = 2.0F + MathHelper.cos(((float)(stickIndex * 2) + ageInTicks) * 0.25F);
            this.blazeSticks[stickIndex].rotationPointX = MathHelper.cos(orbitAngle) * 7.0F;
            this.blazeSticks[stickIndex].rotationPointZ = MathHelper.sin(orbitAngle) * 7.0F;
            ++orbitAngle;
        }

        orbitAngle = 0.47123894F + ageInTicks * (float)Math.PI * -0.05F;

        for (int stickIndex = 8; stickIndex < 12; ++stickIndex)
        {
            this.blazeSticks[stickIndex].rotationPointY = 11.0F + MathHelper.cos(((float)stickIndex * 1.5F + ageInTicks) * 0.5F);
            this.blazeSticks[stickIndex].rotationPointX = MathHelper.cos(orbitAngle) * 5.0F;
            this.blazeSticks[stickIndex].rotationPointZ = MathHelper.sin(orbitAngle) * 5.0F;
            ++orbitAngle;
        }

        this.blazeHead.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.blazeHead.rotateAngleX = headPitch / (180F / (float)Math.PI);
    }
}
