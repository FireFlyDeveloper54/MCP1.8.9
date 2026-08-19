package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelMinecart extends ModelBase
{
    public ModelRenderer[] sideModels = new ModelRenderer[7];

    public ModelMinecart()
    {
        this.sideModels[0] = new ModelRenderer(this, 0, 10);
        this.sideModels[1] = new ModelRenderer(this, 0, 0);
        this.sideModels[2] = new ModelRenderer(this, 0, 0);
        this.sideModels[3] = new ModelRenderer(this, 0, 0);
        this.sideModels[4] = new ModelRenderer(this, 0, 0);
        this.sideModels[5] = new ModelRenderer(this, 44, 10);
        int cartWidth = 20;
        int wallHeight = 8;
        int cartDepth = 16;
        int baseY = 4;
        this.sideModels[0].addBox((float)(-cartWidth / 2), (float)(-cartDepth / 2), -1.0F, cartWidth, cartDepth, 2, 0.0F);
        this.sideModels[0].setRotationPoint(0.0F, (float)baseY, 0.0F);
        this.sideModels[5].addBox((float)(-cartWidth / 2 + 1), (float)(-cartDepth / 2 + 1), -1.0F, cartWidth - 2, cartDepth - 2, 1, 0.0F);
        this.sideModels[5].setRotationPoint(0.0F, (float)baseY, 0.0F);
        this.sideModels[1].addBox((float)(-cartWidth / 2 + 2), (float)(-wallHeight - 1), -1.0F, cartWidth - 4, wallHeight, 2, 0.0F);
        this.sideModels[1].setRotationPoint((float)(-cartWidth / 2 + 1), (float)baseY, 0.0F);
        this.sideModels[2].addBox((float)(-cartWidth / 2 + 2), (float)(-wallHeight - 1), -1.0F, cartWidth - 4, wallHeight, 2, 0.0F);
        this.sideModels[2].setRotationPoint((float)(cartWidth / 2 - 1), (float)baseY, 0.0F);
        this.sideModels[3].addBox((float)(-cartWidth / 2 + 2), (float)(-wallHeight - 1), -1.0F, cartWidth - 4, wallHeight, 2, 0.0F);
        this.sideModels[3].setRotationPoint(0.0F, (float)baseY, (float)(-cartDepth / 2 + 1));
        this.sideModels[4].addBox((float)(-cartWidth / 2 + 2), (float)(-wallHeight - 1), -1.0F, cartWidth - 4, wallHeight, 2, 0.0F);
        this.sideModels[4].setRotationPoint(0.0F, (float)baseY, (float)(cartDepth / 2 - 1));
        this.sideModels[0].rotateAngleX = ((float)Math.PI / 2F);
        this.sideModels[1].rotateAngleY = ((float)Math.PI * 3F / 2F);
        this.sideModels[2].rotateAngleY = ((float)Math.PI / 2F);
        this.sideModels[3].rotateAngleY = (float)Math.PI;
        this.sideModels[5].rotateAngleX = -((float)Math.PI / 2F);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.sideModels[5].rotationPointY = 4.0F - ageInTicks;

        for (int sideIndex = 0; sideIndex < 6; ++sideIndex)
        {
            this.sideModels[sideIndex].render(scale);
        }
    }
}
