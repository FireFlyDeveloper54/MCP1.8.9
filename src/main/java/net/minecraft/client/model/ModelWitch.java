package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelWitch extends ModelVillager
{
    public boolean holdingItem;
    private ModelRenderer witchMole = (new ModelRenderer(this)).setTextureSize(64, 128);
    private ModelRenderer witchHat;

    public ModelWitch(float modelSize)
    {
        super(modelSize, 0.0F, 64, 128);
        this.witchMole.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.witchMole.setTextureOffset(0, 0).addBox(0.0F, 3.0F, -6.75F, 1, 1, 1, -0.25F);
        this.villagerNose.addChild(this.witchMole);
        this.witchHat = (new ModelRenderer(this)).setTextureSize(64, 128);
        this.witchHat.setRotationPoint(-5.0F, -10.03125F, -5.0F);
        this.witchHat.setTextureOffset(0, 64).addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
        this.villagerHead.addChild(this.witchHat);
        ModelRenderer modelRenderer = (new ModelRenderer(this)).setTextureSize(64, 128);
        modelRenderer.setRotationPoint(1.75F, -4.0F, 2.0F);
        modelRenderer.setTextureOffset(0, 76).addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
        modelRenderer.rotateAngleX = -0.05235988F;
        modelRenderer.rotateAngleZ = 0.02617994F;
        this.witchHat.addChild(modelRenderer);
        ModelRenderer modelrenderer1 = (new ModelRenderer(this)).setTextureSize(64, 128);
        modelrenderer1.setRotationPoint(1.75F, -4.0F, 2.0F);
        modelrenderer1.setTextureOffset(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
        modelrenderer1.rotateAngleX = -0.10471976F;
        modelrenderer1.rotateAngleZ = 0.05235988F;
        modelRenderer.addChild(modelrenderer1);
        ModelRenderer modelrenderer2 = (new ModelRenderer(this)).setTextureSize(64, 128);
        modelrenderer2.setRotationPoint(1.75F, -2.0F, 2.0F);
        modelrenderer2.setTextureOffset(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
        modelrenderer2.rotateAngleX = -0.20943952F;
        modelrenderer2.rotateAngleZ = 0.10471976F;
        modelrenderer1.addChild(modelrenderer2);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        this.villagerNose.offsetX = this.villagerNose.offsetY = this.villagerNose.offsetZ = 0.0F;
        float noseWiggleSpeed = 0.01F * (float)(entityIn.getEntityId() % 10);
        this.villagerNose.rotateAngleX = MathHelper.sin((float)entityIn.ticksExisted * noseWiggleSpeed) * 4.5F * (float)Math.PI / 180.0F;
        this.villagerNose.rotateAngleY = 0.0F;
        this.villagerNose.rotateAngleZ = MathHelper.cos((float)entityIn.ticksExisted * noseWiggleSpeed) * 2.5F * (float)Math.PI / 180.0F;

        if (this.holdingItem)
        {
            this.villagerNose.rotateAngleX = -0.9F;
            this.villagerNose.offsetZ = -0.09375F;
            this.villagerNose.offsetY = 0.1875F;
        }
    }
}
