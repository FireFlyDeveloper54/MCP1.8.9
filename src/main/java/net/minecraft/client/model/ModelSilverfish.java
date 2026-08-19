package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelSilverfish extends ModelBase
{
    private ModelRenderer[] silverfishBodyParts = new ModelRenderer[7];
    private ModelRenderer[] silverfishWings;
    private float[] bodyPartZOffsets = new float[7];
    private static final int[][] silverfishBoxLength = new int[][] {{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] silverfishTexturePositions = new int[][] {{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    public ModelSilverfish()
    {
        float partZOffset = -3.5F;

        for (int partIndex = 0; partIndex < this.silverfishBodyParts.length; ++partIndex)
        {
            this.silverfishBodyParts[partIndex] = new ModelRenderer(this, silverfishTexturePositions[partIndex][0], silverfishTexturePositions[partIndex][1]);
            this.silverfishBodyParts[partIndex].addBox((float)silverfishBoxLength[partIndex][0] * -0.5F, 0.0F, (float)silverfishBoxLength[partIndex][2] * -0.5F, silverfishBoxLength[partIndex][0], silverfishBoxLength[partIndex][1], silverfishBoxLength[partIndex][2]);
            this.silverfishBodyParts[partIndex].setRotationPoint(0.0F, (float)(24 - silverfishBoxLength[partIndex][1]), partZOffset);
            this.bodyPartZOffsets[partIndex] = partZOffset;

            if (partIndex < this.silverfishBodyParts.length - 1)
            {
                partZOffset += (float)(silverfishBoxLength[partIndex][2] + silverfishBoxLength[partIndex + 1][2]) * 0.5F;
            }
        }

        this.silverfishWings = new ModelRenderer[3];
        this.silverfishWings[0] = new ModelRenderer(this, 20, 0);
        this.silverfishWings[0].addBox(-5.0F, 0.0F, (float)silverfishBoxLength[2][2] * -0.5F, 10, 8, silverfishBoxLength[2][2]);
        this.silverfishWings[0].setRotationPoint(0.0F, 16.0F, this.bodyPartZOffsets[2]);
        this.silverfishWings[1] = new ModelRenderer(this, 20, 11);
        this.silverfishWings[1].addBox(-3.0F, 0.0F, (float)silverfishBoxLength[4][2] * -0.5F, 6, 4, silverfishBoxLength[4][2]);
        this.silverfishWings[1].setRotationPoint(0.0F, 20.0F, this.bodyPartZOffsets[4]);
        this.silverfishWings[2] = new ModelRenderer(this, 20, 18);
        this.silverfishWings[2].addBox(-3.0F, 0.0F, (float)silverfishBoxLength[4][2] * -0.5F, 6, 5, silverfishBoxLength[1][2]);
        this.silverfishWings[2].setRotationPoint(0.0F, 19.0F, this.bodyPartZOffsets[1]);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        for (int partIndex = 0; partIndex < this.silverfishBodyParts.length; ++partIndex)
        {
            this.silverfishBodyParts[partIndex].render(scale);
        }

        for (int wingIndex = 0; wingIndex < this.silverfishWings.length; ++wingIndex)
        {
            this.silverfishWings[wingIndex].render(scale);
        }
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        for (int partIndex = 0; partIndex < this.silverfishBodyParts.length; ++partIndex)
        {
            this.silverfishBodyParts[partIndex].rotateAngleY = MathHelper.cos(ageInTicks * 0.9F + (float)partIndex * 0.15F * (float)Math.PI) * (float)Math.PI * 0.05F * (float)(1 + Math.abs(partIndex - 2));
            this.silverfishBodyParts[partIndex].rotationPointX = MathHelper.sin(ageInTicks * 0.9F + (float)partIndex * 0.15F * (float)Math.PI) * (float)Math.PI * 0.2F * (float)Math.abs(partIndex - 2);
        }

        this.silverfishWings[0].rotateAngleY = this.silverfishBodyParts[2].rotateAngleY;
        this.silverfishWings[1].rotateAngleY = this.silverfishBodyParts[4].rotateAngleY;
        this.silverfishWings[1].rotationPointX = this.silverfishBodyParts[4].rotationPointX;
        this.silverfishWings[2].rotateAngleY = this.silverfishBodyParts[1].rotateAngleY;
        this.silverfishWings[2].rotationPointX = this.silverfishBodyParts[1].rotationPointX;
    }
}
