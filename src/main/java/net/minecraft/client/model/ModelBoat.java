package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelBoat extends ModelBase
{
    public ModelRenderer[] boatSides = new ModelRenderer[5];

    public ModelBoat()
    {
        this.boatSides[0] = new ModelRenderer(this, 0, 8);
        this.boatSides[1] = new ModelRenderer(this, 0, 0);
        this.boatSides[2] = new ModelRenderer(this, 0, 0);
        this.boatSides[3] = new ModelRenderer(this, 0, 0);
        this.boatSides[4] = new ModelRenderer(this, 0, 0);
        int boatWidth = 24;
        int sideHeight = 6;
        int boatLength = 20;
        int baseY = 4;
        this.boatSides[0].addBox((float)(-boatWidth / 2), (float)(-boatLength / 2 + 2), -3.0F, boatWidth, boatLength - 4, 4, 0.0F);
        this.boatSides[0].setRotationPoint(0.0F, (float)baseY, 0.0F);
        this.boatSides[1].addBox((float)(-boatWidth / 2 + 2), (float)(-sideHeight - 1), -1.0F, boatWidth - 4, sideHeight, 2, 0.0F);
        this.boatSides[1].setRotationPoint((float)(-boatWidth / 2 + 1), (float)baseY, 0.0F);
        this.boatSides[2].addBox((float)(-boatWidth / 2 + 2), (float)(-sideHeight - 1), -1.0F, boatWidth - 4, sideHeight, 2, 0.0F);
        this.boatSides[2].setRotationPoint((float)(boatWidth / 2 - 1), (float)baseY, 0.0F);
        this.boatSides[3].addBox((float)(-boatWidth / 2 + 2), (float)(-sideHeight - 1), -1.0F, boatWidth - 4, sideHeight, 2, 0.0F);
        this.boatSides[3].setRotationPoint(0.0F, (float)baseY, (float)(-boatLength / 2 + 1));
        this.boatSides[4].addBox((float)(-boatWidth / 2 + 2), (float)(-sideHeight - 1), -1.0F, boatWidth - 4, sideHeight, 2, 0.0F);
        this.boatSides[4].setRotationPoint(0.0F, (float)baseY, (float)(boatLength / 2 - 1));
        this.boatSides[0].rotateAngleX = ((float)Math.PI / 2F);
        this.boatSides[1].rotateAngleY = ((float)Math.PI * 3F / 2F);
        this.boatSides[2].rotateAngleY = ((float)Math.PI / 2F);
        this.boatSides[3].rotateAngleY = (float)Math.PI;
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        for (int sideIndex = 0; sideIndex < 5; ++sideIndex)
        {
            this.boatSides[sideIndex].render(scale);
        }
    }
}
