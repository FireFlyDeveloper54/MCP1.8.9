package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelEnderMite extends ModelBase
{
    private static final int[][] BODY_SIZES = new int[][] {{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
    private static final int[][] BODY_TEX_OFFSETS = new int[][] {{0, 0}, {0, 5}, {0, 14}, {0, 18}};
    private static final int BODY_PART_COUNT = BODY_SIZES.length;
    private final ModelRenderer[] bodyParts;

    public ModelEnderMite()
    {
        this.bodyParts = new ModelRenderer[BODY_PART_COUNT];
        float partZOffset = -3.5F;

        for (int partIndex = 0; partIndex < this.bodyParts.length; ++partIndex)
        {
            this.bodyParts[partIndex] = new ModelRenderer(this, BODY_TEX_OFFSETS[partIndex][0], BODY_TEX_OFFSETS[partIndex][1]);
            this.bodyParts[partIndex].addBox((float)BODY_SIZES[partIndex][0] * -0.5F, 0.0F, (float)BODY_SIZES[partIndex][2] * -0.5F, BODY_SIZES[partIndex][0], BODY_SIZES[partIndex][1], BODY_SIZES[partIndex][2]);
            this.bodyParts[partIndex].setRotationPoint(0.0F, (float)(24 - BODY_SIZES[partIndex][1]), partZOffset);

            if (partIndex < this.bodyParts.length - 1)
            {
                partZOffset += (float)(BODY_SIZES[partIndex][2] + BODY_SIZES[partIndex + 1][2]) * 0.5F;
            }
        }
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        for (int partIndex = 0; partIndex < this.bodyParts.length; ++partIndex)
        {
            this.bodyParts[partIndex].render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        for (int partIndex = 0; partIndex < this.bodyParts.length; ++partIndex)
        {
            this.bodyParts[partIndex].rotateAngleY = MathHelper.cos(ageInTicks * 0.9F + (float)partIndex * 0.15F * (float)Math.PI) * (float)Math.PI * 0.01F * (float)(1 + Math.abs(partIndex - 2));
            this.bodyParts[partIndex].rotationPointX = MathHelper.sin(ageInTicks * 0.9F + (float)partIndex * 0.15F * (float)Math.PI) * (float)Math.PI * 0.1F * (float)Math.abs(partIndex - 2);
        }
    }
}
