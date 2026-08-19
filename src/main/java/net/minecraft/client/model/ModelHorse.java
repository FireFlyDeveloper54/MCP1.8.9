package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.MathHelper;

public class ModelHorse extends ModelBase
{
    private ModelRenderer head;
    private ModelRenderer upperMouth;
    private ModelRenderer lowerMouth;
    private ModelRenderer horseLeftEar;
    private ModelRenderer horseRightEar;
    private ModelRenderer muleLeftEar;
    private ModelRenderer muleRightEar;
    private ModelRenderer neck;
    private ModelRenderer horseFaceRopes;
    private ModelRenderer mane;
    private ModelRenderer body;
    private ModelRenderer tailBase;
    private ModelRenderer tailMiddle;
    private ModelRenderer tailTip;
    private ModelRenderer backLeftLeg;
    private ModelRenderer backLeftShin;
    private ModelRenderer backLeftHoof;
    private ModelRenderer backRightLeg;
    private ModelRenderer backRightShin;
    private ModelRenderer backRightHoof;
    private ModelRenderer frontLeftLeg;
    private ModelRenderer frontLeftShin;
    private ModelRenderer frontLeftHoof;
    private ModelRenderer frontRightLeg;
    private ModelRenderer frontRightShin;
    private ModelRenderer frontRightHoof;
    private ModelRenderer muleLeftChest;
    private ModelRenderer muleRightChest;
    private ModelRenderer horseSaddleBottom;
    private ModelRenderer horseSaddleFront;
    private ModelRenderer horseSaddleBack;
    private ModelRenderer horseLeftSaddleRope;
    private ModelRenderer horseLeftSaddleMetal;
    private ModelRenderer horseRightSaddleRope;
    private ModelRenderer horseRightSaddleMetal;
    private ModelRenderer horseLeftFaceMetal;
    private ModelRenderer horseRightFaceMetal;
    private ModelRenderer horseLeftRein;
    private ModelRenderer horseRightRein;

    public ModelHorse()
    {
        this.textureWidth = 128;
        this.textureHeight = 128;
        this.body = new ModelRenderer(this, 0, 34);
        this.body.addBox(-5.0F, -8.0F, -19.0F, 10, 10, 24);
        this.body.setRotationPoint(0.0F, 11.0F, 9.0F);
        this.tailBase = new ModelRenderer(this, 44, 0);
        this.tailBase.addBox(-1.0F, -1.0F, 0.0F, 2, 2, 3);
        this.tailBase.setRotationPoint(0.0F, 3.0F, 14.0F);
        this.setBoxRotation(this.tailBase, -1.134464F, 0.0F, 0.0F);
        this.tailMiddle = new ModelRenderer(this, 38, 7);
        this.tailMiddle.addBox(-1.5F, -2.0F, 3.0F, 3, 4, 7);
        this.tailMiddle.setRotationPoint(0.0F, 3.0F, 14.0F);
        this.setBoxRotation(this.tailMiddle, -1.134464F, 0.0F, 0.0F);
        this.tailTip = new ModelRenderer(this, 24, 3);
        this.tailTip.addBox(-1.5F, -4.5F, 9.0F, 3, 4, 7);
        this.tailTip.setRotationPoint(0.0F, 3.0F, 14.0F);
        this.setBoxRotation(this.tailTip, -1.40215F, 0.0F, 0.0F);
        this.backLeftLeg = new ModelRenderer(this, 78, 29);
        this.backLeftLeg.addBox(-2.5F, -2.0F, -2.5F, 4, 9, 5);
        this.backLeftLeg.setRotationPoint(4.0F, 9.0F, 11.0F);
        this.backLeftShin = new ModelRenderer(this, 78, 43);
        this.backLeftShin.addBox(-2.0F, 0.0F, -1.5F, 3, 5, 3);
        this.backLeftShin.setRotationPoint(4.0F, 16.0F, 11.0F);
        this.backLeftHoof = new ModelRenderer(this, 78, 51);
        this.backLeftHoof.addBox(-2.5F, 5.1F, -2.0F, 4, 3, 4);
        this.backLeftHoof.setRotationPoint(4.0F, 16.0F, 11.0F);
        this.backRightLeg = new ModelRenderer(this, 96, 29);
        this.backRightLeg.addBox(-1.5F, -2.0F, -2.5F, 4, 9, 5);
        this.backRightLeg.setRotationPoint(-4.0F, 9.0F, 11.0F);
        this.backRightShin = new ModelRenderer(this, 96, 43);
        this.backRightShin.addBox(-1.0F, 0.0F, -1.5F, 3, 5, 3);
        this.backRightShin.setRotationPoint(-4.0F, 16.0F, 11.0F);
        this.backRightHoof = new ModelRenderer(this, 96, 51);
        this.backRightHoof.addBox(-1.5F, 5.1F, -2.0F, 4, 3, 4);
        this.backRightHoof.setRotationPoint(-4.0F, 16.0F, 11.0F);
        this.frontLeftLeg = new ModelRenderer(this, 44, 29);
        this.frontLeftLeg.addBox(-1.9F, -1.0F, -2.1F, 3, 8, 4);
        this.frontLeftLeg.setRotationPoint(4.0F, 9.0F, -8.0F);
        this.frontLeftShin = new ModelRenderer(this, 44, 41);
        this.frontLeftShin.addBox(-1.9F, 0.0F, -1.6F, 3, 5, 3);
        this.frontLeftShin.setRotationPoint(4.0F, 16.0F, -8.0F);
        this.frontLeftHoof = new ModelRenderer(this, 44, 51);
        this.frontLeftHoof.addBox(-2.4F, 5.1F, -2.1F, 4, 3, 4);
        this.frontLeftHoof.setRotationPoint(4.0F, 16.0F, -8.0F);
        this.frontRightLeg = new ModelRenderer(this, 60, 29);
        this.frontRightLeg.addBox(-1.1F, -1.0F, -2.1F, 3, 8, 4);
        this.frontRightLeg.setRotationPoint(-4.0F, 9.0F, -8.0F);
        this.frontRightShin = new ModelRenderer(this, 60, 41);
        this.frontRightShin.addBox(-1.1F, 0.0F, -1.6F, 3, 5, 3);
        this.frontRightShin.setRotationPoint(-4.0F, 16.0F, -8.0F);
        this.frontRightHoof = new ModelRenderer(this, 60, 51);
        this.frontRightHoof.addBox(-1.6F, 5.1F, -2.1F, 4, 3, 4);
        this.frontRightHoof.setRotationPoint(-4.0F, 16.0F, -8.0F);
        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-2.5F, -10.0F, -1.5F, 5, 5, 7);
        this.head.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.head, 0.5235988F, 0.0F, 0.0F);
        this.upperMouth = new ModelRenderer(this, 24, 18);
        this.upperMouth.addBox(-2.0F, -10.0F, -7.0F, 4, 3, 6);
        this.upperMouth.setRotationPoint(0.0F, 3.95F, -10.0F);
        this.setBoxRotation(this.upperMouth, 0.5235988F, 0.0F, 0.0F);
        this.lowerMouth = new ModelRenderer(this, 24, 27);
        this.lowerMouth.addBox(-2.0F, -7.0F, -6.5F, 4, 2, 5);
        this.lowerMouth.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.lowerMouth, 0.5235988F, 0.0F, 0.0F);
        this.head.addChild(this.upperMouth);
        this.head.addChild(this.lowerMouth);
        this.horseLeftEar = new ModelRenderer(this, 0, 0);
        this.horseLeftEar.addBox(0.45F, -12.0F, 4.0F, 2, 3, 1);
        this.horseLeftEar.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.horseLeftEar, 0.5235988F, 0.0F, 0.0F);
        this.horseRightEar = new ModelRenderer(this, 0, 0);
        this.horseRightEar.addBox(-2.45F, -12.0F, 4.0F, 2, 3, 1);
        this.horseRightEar.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.horseRightEar, 0.5235988F, 0.0F, 0.0F);
        this.muleLeftEar = new ModelRenderer(this, 0, 12);
        this.muleLeftEar.addBox(-2.0F, -16.0F, 4.0F, 2, 7, 1);
        this.muleLeftEar.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.muleLeftEar, 0.5235988F, 0.0F, 0.2617994F);
        this.muleRightEar = new ModelRenderer(this, 0, 12);
        this.muleRightEar.addBox(0.0F, -16.0F, 4.0F, 2, 7, 1);
        this.muleRightEar.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.muleRightEar, 0.5235988F, 0.0F, -0.2617994F);
        this.neck = new ModelRenderer(this, 0, 12);
        this.neck.addBox(-2.05F, -9.8F, -2.0F, 4, 14, 8);
        this.neck.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.neck, 0.5235988F, 0.0F, 0.0F);
        this.muleLeftChest = new ModelRenderer(this, 0, 34);
        this.muleLeftChest.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3);
        this.muleLeftChest.setRotationPoint(-7.5F, 3.0F, 10.0F);
        this.setBoxRotation(this.muleLeftChest, 0.0F, ((float)Math.PI / 2F), 0.0F);
        this.muleRightChest = new ModelRenderer(this, 0, 47);
        this.muleRightChest.addBox(-3.0F, 0.0F, 0.0F, 8, 8, 3);
        this.muleRightChest.setRotationPoint(4.5F, 3.0F, 10.0F);
        this.setBoxRotation(this.muleRightChest, 0.0F, ((float)Math.PI / 2F), 0.0F);
        this.horseSaddleBottom = new ModelRenderer(this, 80, 0);
        this.horseSaddleBottom.addBox(-5.0F, 0.0F, -3.0F, 10, 1, 8);
        this.horseSaddleBottom.setRotationPoint(0.0F, 2.0F, 2.0F);
        this.horseSaddleFront = new ModelRenderer(this, 106, 9);
        this.horseSaddleFront.addBox(-1.5F, -1.0F, -3.0F, 3, 1, 2);
        this.horseSaddleFront.setRotationPoint(0.0F, 2.0F, 2.0F);
        this.horseSaddleBack = new ModelRenderer(this, 80, 9);
        this.horseSaddleBack.addBox(-4.0F, -1.0F, 3.0F, 8, 1, 2);
        this.horseSaddleBack.setRotationPoint(0.0F, 2.0F, 2.0F);
        this.horseLeftSaddleMetal = new ModelRenderer(this, 74, 0);
        this.horseLeftSaddleMetal.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
        this.horseLeftSaddleMetal.setRotationPoint(5.0F, 3.0F, 2.0F);
        this.horseLeftSaddleRope = new ModelRenderer(this, 70, 0);
        this.horseLeftSaddleRope.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
        this.horseLeftSaddleRope.setRotationPoint(5.0F, 3.0F, 2.0F);
        this.horseRightSaddleMetal = new ModelRenderer(this, 74, 4);
        this.horseRightSaddleMetal.addBox(-0.5F, 6.0F, -1.0F, 1, 2, 2);
        this.horseRightSaddleMetal.setRotationPoint(-5.0F, 3.0F, 2.0F);
        this.horseRightSaddleRope = new ModelRenderer(this, 80, 0);
        this.horseRightSaddleRope.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1);
        this.horseRightSaddleRope.setRotationPoint(-5.0F, 3.0F, 2.0F);
        this.horseLeftFaceMetal = new ModelRenderer(this, 74, 13);
        this.horseLeftFaceMetal.addBox(1.5F, -8.0F, -4.0F, 1, 2, 2);
        this.horseLeftFaceMetal.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.horseLeftFaceMetal, 0.5235988F, 0.0F, 0.0F);
        this.horseRightFaceMetal = new ModelRenderer(this, 74, 13);
        this.horseRightFaceMetal.addBox(-2.5F, -8.0F, -4.0F, 1, 2, 2);
        this.horseRightFaceMetal.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.horseRightFaceMetal, 0.5235988F, 0.0F, 0.0F);
        this.horseLeftRein = new ModelRenderer(this, 44, 10);
        this.horseLeftRein.addBox(2.6F, -6.0F, -6.0F, 0, 3, 16);
        this.horseLeftRein.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.horseRightRein = new ModelRenderer(this, 44, 5);
        this.horseRightRein.addBox(-2.6F, -6.0F, -6.0F, 0, 3, 16);
        this.horseRightRein.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.mane = new ModelRenderer(this, 58, 0);
        this.mane.addBox(-1.0F, -11.5F, 5.0F, 2, 16, 4);
        this.mane.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.mane, 0.5235988F, 0.0F, 0.0F);
        this.horseFaceRopes = new ModelRenderer(this, 80, 12);
        this.horseFaceRopes.addBox(-2.5F, -10.1F, -7.0F, 5, 5, 12, 0.2F);
        this.horseFaceRopes.setRotationPoint(0.0F, 4.0F, -10.0F);
        this.setBoxRotation(this.horseFaceRopes, 0.5235988F, 0.0F, 0.0F);
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        EntityHorse entityHorse = (EntityHorse)entityIn;
        int horseType = entityHorse.getHorseType();
        float grassEatingAmount = entityHorse.getGrassEatingAmount(0.0F);
        boolean isAdultHorse = entityHorse.isAdultHorse();
        boolean isSaddledAdult = isAdultHorse && entityHorse.isHorseSaddled();
        boolean isChestedAdult = isAdultHorse && entityHorse.isChested();
        boolean isMuleOrDonkey = horseType == 1 || horseType == 2;
        float horseSize = entityHorse.getHorseSize();
        boolean isBeingRidden = entityHorse.riddenByEntity != null;

        if (isSaddledAdult)
        {
            this.horseFaceRopes.render(scale);
            this.horseSaddleBottom.render(scale);
            this.horseSaddleFront.render(scale);
            this.horseSaddleBack.render(scale);
            this.horseLeftSaddleRope.render(scale);
            this.horseLeftSaddleMetal.render(scale);
            this.horseRightSaddleRope.render(scale);
            this.horseRightSaddleMetal.render(scale);
            this.horseLeftFaceMetal.render(scale);
            this.horseRightFaceMetal.render(scale);

            if (isBeingRidden)
            {
                this.horseLeftRein.render(scale);
                this.horseRightRein.render(scale);
            }
        }

        if (!isAdultHorse)
        {
            GlStateManager.pushMatrix();
            GlStateManager.scale(horseSize, 0.5F + horseSize * 0.5F, horseSize);
            GlStateManager.translate(0.0F, 0.95F * (1.0F - horseSize), 0.0F);
        }

        this.backLeftLeg.render(scale);
        this.backLeftShin.render(scale);
        this.backLeftHoof.render(scale);
        this.backRightLeg.render(scale);
        this.backRightShin.render(scale);
        this.backRightHoof.render(scale);
        this.frontLeftLeg.render(scale);
        this.frontLeftShin.render(scale);
        this.frontLeftHoof.render(scale);
        this.frontRightLeg.render(scale);
        this.frontRightShin.render(scale);
        this.frontRightHoof.render(scale);

        if (!isAdultHorse)
        {
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(horseSize, horseSize, horseSize);
            GlStateManager.translate(0.0F, 1.35F * (1.0F - horseSize), 0.0F);
        }

        this.body.render(scale);
        this.tailBase.render(scale);
        this.tailMiddle.render(scale);
        this.tailTip.render(scale);
        this.neck.render(scale);
        this.mane.render(scale);

        if (!isAdultHorse)
        {
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            float foalHeadScale = 0.5F + horseSize * horseSize * 0.5F;
            GlStateManager.scale(foalHeadScale, foalHeadScale, foalHeadScale);

            if (grassEatingAmount <= 0.0F)
            {
                GlStateManager.translate(0.0F, 1.35F * (1.0F - horseSize), 0.0F);
            }
            else
            {
                GlStateManager.translate(0.0F, 0.9F * (1.0F - horseSize) * grassEatingAmount + 1.35F * (1.0F - horseSize) * (1.0F - grassEatingAmount), 0.15F * (1.0F - horseSize) * grassEatingAmount);
            }
        }

        if (isMuleOrDonkey)
        {
            this.muleLeftEar.render(scale);
            this.muleRightEar.render(scale);
        }
        else
        {
            this.horseLeftEar.render(scale);
            this.horseRightEar.render(scale);
        }

        this.head.render(scale);

        if (!isAdultHorse)
        {
            GlStateManager.popMatrix();
        }

        if (isChestedAdult)
        {
            this.muleLeftChest.render(scale);
            this.muleRightChest.render(scale);
        }
    }

    private void setBoxRotation(ModelRenderer renderer, float xRotation, float yRotation, float zRotation)
    {
        renderer.rotateAngleX = xRotation;
        renderer.rotateAngleY = yRotation;
        renderer.rotateAngleZ = zRotation;
    }

    private float updateHorseRotation(float currentAngle, float targetAngle, float partialTicks)
    {
        float wrappedDelta;

        for (wrappedDelta = targetAngle - currentAngle; wrappedDelta < -180.0F; wrappedDelta += 360.0F)
        {
            ;
        }

        while (wrappedDelta >= 180.0F)
        {
            wrappedDelta -= 360.0F;
        }

        return currentAngle + partialTicks * wrappedDelta;
    }

    public void setLivingAnimations(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        super.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTickTime);
        float bodyYaw = this.updateHorseRotation(entityLivingBaseIn.prevRenderYawOffset, entityLivingBaseIn.renderYawOffset, partialTickTime);
        float headYaw = this.updateHorseRotation(entityLivingBaseIn.prevRotationYawHead, entityLivingBaseIn.rotationYawHead, partialTickTime);
        float headPitchDegrees = entityLivingBaseIn.prevRotationPitch + (entityLivingBaseIn.rotationPitch - entityLivingBaseIn.prevRotationPitch) * partialTickTime;
        float netHeadYaw = headYaw - bodyYaw;
        float headPitchRadians = headPitchDegrees / (180F / (float)Math.PI);

        if (netHeadYaw > 20.0F)
        {
            netHeadYaw = 20.0F;
        }

        if (netHeadYaw < -20.0F)
        {
            netHeadYaw = -20.0F;
        }

        if (limbSwingAmount > 0.2F)
        {
            headPitchRadians += MathHelper.cos(limbSwing * 0.4F) * 0.15F * limbSwingAmount;
        }

        EntityHorse entityHorse = (EntityHorse)entityLivingBaseIn;
        float grassEatingAmount = entityHorse.getGrassEatingAmount(partialTickTime);
        float rearingAmount = entityHorse.getRearingAmount(partialTickTime);
        float notRearingAmount = 1.0F - rearingAmount;
        float mouthOpenness = entityHorse.getMouthOpennessAngle(partialTickTime);
        boolean isTailSwishing = entityHorse.tailCounter != 0;
        boolean isHorseSaddled = entityHorse.isHorseSaddled();
        boolean isBeingRidden = entityHorse.riddenByEntity != null;
        float animationTicks = (float)entityLivingBaseIn.ticksExisted + partialTickTime;
        float limbSwingCos = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI);
        float limbSwingRotation = limbSwingCos * 0.8F * limbSwingAmount;
        this.head.rotationPointY = 4.0F;
        this.head.rotationPointZ = -10.0F;
        this.tailBase.rotationPointY = 3.0F;
        this.tailMiddle.rotationPointZ = 14.0F;
        this.muleRightChest.rotationPointY = 3.0F;
        this.muleRightChest.rotationPointZ = 10.0F;
        this.body.rotateAngleX = 0.0F;
        this.head.rotateAngleX = 0.5235988F + headPitchRadians;
        this.head.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.head.rotateAngleX = rearingAmount * (0.2617994F + headPitchRadians) + grassEatingAmount * 2.18166F + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * this.head.rotateAngleX;
        this.head.rotateAngleY = rearingAmount * netHeadYaw / (180F / (float)Math.PI) + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * this.head.rotateAngleY;
        this.head.rotationPointY = rearingAmount * -6.0F + grassEatingAmount * 11.0F + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * this.head.rotationPointY;
        this.head.rotationPointZ = rearingAmount * -1.0F + grassEatingAmount * -10.0F + (1.0F - Math.max(rearingAmount, grassEatingAmount)) * this.head.rotationPointZ;
        this.tailBase.rotationPointY = rearingAmount * 9.0F + notRearingAmount * this.tailBase.rotationPointY;
        this.tailMiddle.rotationPointZ = rearingAmount * 18.0F + notRearingAmount * this.tailMiddle.rotationPointZ;
        this.muleRightChest.rotationPointY = rearingAmount * 5.5F + notRearingAmount * this.muleRightChest.rotationPointY;
        this.muleRightChest.rotationPointZ = rearingAmount * 15.0F + notRearingAmount * this.muleRightChest.rotationPointZ;
        this.body.rotateAngleX = rearingAmount * -45.0F / (180F / (float)Math.PI) + notRearingAmount * this.body.rotateAngleX;
        this.horseLeftEar.rotationPointY = this.head.rotationPointY;
        this.horseRightEar.rotationPointY = this.head.rotationPointY;
        this.muleLeftEar.rotationPointY = this.head.rotationPointY;
        this.muleRightEar.rotationPointY = this.head.rotationPointY;
        this.neck.rotationPointY = this.head.rotationPointY;
        this.upperMouth.rotationPointY = 0.02F;
        this.lowerMouth.rotationPointY = 0.0F;
        this.mane.rotationPointY = this.head.rotationPointY;
        this.horseLeftEar.rotationPointZ = this.head.rotationPointZ;
        this.horseRightEar.rotationPointZ = this.head.rotationPointZ;
        this.muleLeftEar.rotationPointZ = this.head.rotationPointZ;
        this.muleRightEar.rotationPointZ = this.head.rotationPointZ;
        this.neck.rotationPointZ = this.head.rotationPointZ;
        this.upperMouth.rotationPointZ = 0.02F - mouthOpenness * 1.0F;
        this.lowerMouth.rotationPointZ = 0.0F + mouthOpenness * 1.0F;
        this.mane.rotationPointZ = this.head.rotationPointZ;
        this.horseLeftEar.rotateAngleX = this.head.rotateAngleX;
        this.horseRightEar.rotateAngleX = this.head.rotateAngleX;
        this.muleLeftEar.rotateAngleX = this.head.rotateAngleX;
        this.muleRightEar.rotateAngleX = this.head.rotateAngleX;
        this.neck.rotateAngleX = this.head.rotateAngleX;
        this.upperMouth.rotateAngleX = 0.0F - 0.09424778F * mouthOpenness;
        this.lowerMouth.rotateAngleX = 0.0F + 0.15707964F * mouthOpenness;
        this.mane.rotateAngleX = this.head.rotateAngleX;
        this.horseLeftEar.rotateAngleY = this.head.rotateAngleY;
        this.horseRightEar.rotateAngleY = this.head.rotateAngleY;
        this.muleLeftEar.rotateAngleY = this.head.rotateAngleY;
        this.muleRightEar.rotateAngleY = this.head.rotateAngleY;
        this.neck.rotateAngleY = this.head.rotateAngleY;
        this.upperMouth.rotateAngleY = 0.0F;
        this.lowerMouth.rotateAngleY = 0.0F;
        this.mane.rotateAngleY = this.head.rotateAngleY;
        this.muleLeftChest.rotateAngleX = limbSwingRotation / 5.0F;
        this.muleRightChest.rotateAngleX = -limbSwingRotation / 5.0F;
        float rearingRotation = 0.2617994F * rearingAmount;
        float alternateLegSwing = MathHelper.cos(animationTicks * 0.6F + (float)Math.PI);
        this.frontLeftLeg.rotationPointY = -2.0F * rearingAmount + 9.0F * notRearingAmount;
        this.frontLeftLeg.rotationPointZ = -2.0F * rearingAmount + -8.0F * notRearingAmount;
        this.frontRightLeg.rotationPointY = this.frontLeftLeg.rotationPointY;
        this.frontRightLeg.rotationPointZ = this.frontLeftLeg.rotationPointZ;
        this.backLeftShin.rotationPointY = this.backLeftLeg.rotationPointY + MathHelper.sin(((float)Math.PI / 2F) + rearingRotation + notRearingAmount * -limbSwingCos * 0.5F * limbSwingAmount) * 7.0F;
        this.backLeftShin.rotationPointZ = this.backLeftLeg.rotationPointZ + MathHelper.cos(((float)Math.PI * 3F / 2F) + rearingRotation + notRearingAmount * -limbSwingCos * 0.5F * limbSwingAmount) * 7.0F;
        this.backRightShin.rotationPointY = this.backRightLeg.rotationPointY + MathHelper.sin(((float)Math.PI / 2F) + rearingRotation + notRearingAmount * limbSwingCos * 0.5F * limbSwingAmount) * 7.0F;
        this.backRightShin.rotationPointZ = this.backRightLeg.rotationPointZ + MathHelper.cos(((float)Math.PI * 3F / 2F) + rearingRotation + notRearingAmount * limbSwingCos * 0.5F * limbSwingAmount) * 7.0F;
        float frontLeftLegRotation = (-1.0471976F + alternateLegSwing) * rearingAmount + limbSwingRotation * notRearingAmount;
        float frontRightLegRotation = (-1.0471976F + -alternateLegSwing) * rearingAmount + -limbSwingRotation * notRearingAmount;
        this.frontLeftShin.rotationPointY = this.frontLeftLeg.rotationPointY + MathHelper.sin(((float)Math.PI / 2F) + frontLeftLegRotation) * 7.0F;
        this.frontLeftShin.rotationPointZ = this.frontLeftLeg.rotationPointZ + MathHelper.cos(((float)Math.PI * 3F / 2F) + frontLeftLegRotation) * 7.0F;
        this.frontRightShin.rotationPointY = this.frontRightLeg.rotationPointY + MathHelper.sin(((float)Math.PI / 2F) + frontRightLegRotation) * 7.0F;
        this.frontRightShin.rotationPointZ = this.frontRightLeg.rotationPointZ + MathHelper.cos(((float)Math.PI * 3F / 2F) + frontRightLegRotation) * 7.0F;
        this.backLeftLeg.rotateAngleX = rearingRotation + -limbSwingCos * 0.5F * limbSwingAmount * notRearingAmount;
        this.backLeftShin.rotateAngleX = -0.08726646F * rearingAmount + (-limbSwingCos * 0.5F * limbSwingAmount - Math.max(0.0F, limbSwingCos * 0.5F * limbSwingAmount)) * notRearingAmount;
        this.backLeftHoof.rotateAngleX = this.backLeftShin.rotateAngleX;
        this.backRightLeg.rotateAngleX = rearingRotation + limbSwingCos * 0.5F * limbSwingAmount * notRearingAmount;
        this.backRightShin.rotateAngleX = -0.08726646F * rearingAmount + (limbSwingCos * 0.5F * limbSwingAmount - Math.max(0.0F, -limbSwingCos * 0.5F * limbSwingAmount)) * notRearingAmount;
        this.backRightHoof.rotateAngleX = this.backRightShin.rotateAngleX;
        this.frontLeftLeg.rotateAngleX = frontLeftLegRotation;
        this.frontLeftShin.rotateAngleX = (this.frontLeftLeg.rotateAngleX + (float)Math.PI * Math.max(0.0F, 0.2F + alternateLegSwing * 0.2F)) * rearingAmount + (limbSwingRotation + Math.max(0.0F, limbSwingCos * 0.5F * limbSwingAmount)) * notRearingAmount;
        this.frontLeftHoof.rotateAngleX = this.frontLeftShin.rotateAngleX;
        this.frontRightLeg.rotateAngleX = frontRightLegRotation;
        this.frontRightShin.rotateAngleX = (this.frontRightLeg.rotateAngleX + (float)Math.PI * Math.max(0.0F, 0.2F - alternateLegSwing * 0.2F)) * rearingAmount + (-limbSwingRotation + Math.max(0.0F, -limbSwingCos * 0.5F * limbSwingAmount)) * notRearingAmount;
        this.frontRightHoof.rotateAngleX = this.frontRightShin.rotateAngleX;
        this.backLeftHoof.rotationPointY = this.backLeftShin.rotationPointY;
        this.backLeftHoof.rotationPointZ = this.backLeftShin.rotationPointZ;
        this.backRightHoof.rotationPointY = this.backRightShin.rotationPointY;
        this.backRightHoof.rotationPointZ = this.backRightShin.rotationPointZ;
        this.frontLeftHoof.rotationPointY = this.frontLeftShin.rotationPointY;
        this.frontLeftHoof.rotationPointZ = this.frontLeftShin.rotationPointZ;
        this.frontRightHoof.rotationPointY = this.frontRightShin.rotationPointY;
        this.frontRightHoof.rotationPointZ = this.frontRightShin.rotationPointZ;

        if (isHorseSaddled)
        {
            this.horseSaddleBottom.rotationPointY = rearingAmount * 0.5F + notRearingAmount * 2.0F;
            this.horseSaddleBottom.rotationPointZ = rearingAmount * 11.0F + notRearingAmount * 2.0F;
            this.horseSaddleFront.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseSaddleBack.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseLeftSaddleRope.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseRightSaddleRope.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseLeftSaddleMetal.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.horseRightSaddleMetal.rotationPointY = this.horseSaddleBottom.rotationPointY;
            this.muleLeftChest.rotationPointY = this.muleRightChest.rotationPointY;
            this.horseSaddleFront.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseSaddleBack.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseLeftSaddleRope.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseRightSaddleRope.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseLeftSaddleMetal.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.horseRightSaddleMetal.rotationPointZ = this.horseSaddleBottom.rotationPointZ;
            this.muleLeftChest.rotationPointZ = this.muleRightChest.rotationPointZ;
            this.horseSaddleBottom.rotateAngleX = this.body.rotateAngleX;
            this.horseSaddleFront.rotateAngleX = this.body.rotateAngleX;
            this.horseSaddleBack.rotateAngleX = this.body.rotateAngleX;
            this.horseLeftRein.rotationPointY = this.head.rotationPointY;
            this.horseRightRein.rotationPointY = this.head.rotationPointY;
            this.horseFaceRopes.rotationPointY = this.head.rotationPointY;
            this.horseLeftFaceMetal.rotationPointY = this.head.rotationPointY;
            this.horseRightFaceMetal.rotationPointY = this.head.rotationPointY;
            this.horseLeftRein.rotationPointZ = this.head.rotationPointZ;
            this.horseRightRein.rotationPointZ = this.head.rotationPointZ;
            this.horseFaceRopes.rotationPointZ = this.head.rotationPointZ;
            this.horseLeftFaceMetal.rotationPointZ = this.head.rotationPointZ;
            this.horseRightFaceMetal.rotationPointZ = this.head.rotationPointZ;
            this.horseLeftRein.rotateAngleX = headPitchRadians;
            this.horseRightRein.rotateAngleX = headPitchRadians;
            this.horseFaceRopes.rotateAngleX = this.head.rotateAngleX;
            this.horseLeftFaceMetal.rotateAngleX = this.head.rotateAngleX;
            this.horseRightFaceMetal.rotateAngleX = this.head.rotateAngleX;
            this.horseFaceRopes.rotateAngleY = this.head.rotateAngleY;
            this.horseLeftFaceMetal.rotateAngleY = this.head.rotateAngleY;
            this.horseLeftRein.rotateAngleY = this.head.rotateAngleY;
            this.horseRightFaceMetal.rotateAngleY = this.head.rotateAngleY;
            this.horseRightRein.rotateAngleY = this.head.rotateAngleY;

            if (isBeingRidden)
            {
                this.horseLeftSaddleRope.rotateAngleX = -1.0471976F;
                this.horseLeftSaddleMetal.rotateAngleX = -1.0471976F;
                this.horseRightSaddleRope.rotateAngleX = -1.0471976F;
                this.horseRightSaddleMetal.rotateAngleX = -1.0471976F;
                this.horseLeftSaddleRope.rotateAngleZ = 0.0F;
                this.horseLeftSaddleMetal.rotateAngleZ = 0.0F;
                this.horseRightSaddleRope.rotateAngleZ = 0.0F;
                this.horseRightSaddleMetal.rotateAngleZ = 0.0F;
            }
            else
            {
                this.horseLeftSaddleRope.rotateAngleX = limbSwingRotation / 3.0F;
                this.horseLeftSaddleMetal.rotateAngleX = limbSwingRotation / 3.0F;
                this.horseRightSaddleRope.rotateAngleX = limbSwingRotation / 3.0F;
                this.horseRightSaddleMetal.rotateAngleX = limbSwingRotation / 3.0F;
                this.horseLeftSaddleRope.rotateAngleZ = limbSwingRotation / 5.0F;
                this.horseLeftSaddleMetal.rotateAngleZ = limbSwingRotation / 5.0F;
                this.horseRightSaddleRope.rotateAngleZ = -limbSwingRotation / 5.0F;
                this.horseRightSaddleMetal.rotateAngleZ = -limbSwingRotation / 5.0F;
            }
        }

        float tailBaseAngle = -1.3089F + limbSwingAmount * 1.5F;

        if (tailBaseAngle > 0.0F)
        {
            tailBaseAngle = 0.0F;
        }

        if (isTailSwishing)
        {
            this.tailBase.rotateAngleY = MathHelper.cos(animationTicks * 0.7F);
            tailBaseAngle = 0.0F;
        }
        else
        {
            this.tailBase.rotateAngleY = 0.0F;
        }

        this.tailMiddle.rotateAngleY = this.tailBase.rotateAngleY;
        this.tailTip.rotateAngleY = this.tailBase.rotateAngleY;
        this.tailMiddle.rotationPointY = this.tailBase.rotationPointY;
        this.tailTip.rotationPointY = this.tailBase.rotationPointY;
        this.tailMiddle.rotationPointZ = this.tailBase.rotationPointZ;
        this.tailTip.rotationPointZ = this.tailBase.rotationPointZ;
        this.tailBase.rotateAngleX = tailBaseAngle;
        this.tailMiddle.rotateAngleX = tailBaseAngle;
        this.tailTip.rotateAngleX = -0.2618F + tailBaseAngle;
    }
}
