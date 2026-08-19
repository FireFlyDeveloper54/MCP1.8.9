package net.minecraft.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class ModelGuardian extends ModelBase
{
    private ModelRenderer guardianBody;
    private ModelRenderer guardianEye;
    private ModelRenderer[] guardianSpines;
    private ModelRenderer[] guardianTail;

    public ModelGuardian()
    {
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.guardianSpines = new ModelRenderer[12];
        this.guardianBody = new ModelRenderer(this);
        this.guardianBody.setTextureOffset(0, 0).addBox(-6.0F, 10.0F, -8.0F, 12, 12, 16);
        this.guardianBody.setTextureOffset(0, 28).addBox(-8.0F, 10.0F, -6.0F, 2, 12, 12);
        this.guardianBody.setTextureOffset(0, 28).addBox(6.0F, 10.0F, -6.0F, 2, 12, 12, true);
        this.guardianBody.setTextureOffset(16, 40).addBox(-6.0F, 8.0F, -6.0F, 12, 2, 12);
        this.guardianBody.setTextureOffset(16, 40).addBox(-6.0F, 22.0F, -6.0F, 12, 2, 12);

        for (int spineIndex = 0; spineIndex < this.guardianSpines.length; ++spineIndex)
        {
            this.guardianSpines[spineIndex] = new ModelRenderer(this, 0, 0);
            this.guardianSpines[spineIndex].addBox(-1.0F, -4.5F, -1.0F, 2, 9, 2);
            this.guardianBody.addChild(this.guardianSpines[spineIndex]);
        }

        this.guardianEye = new ModelRenderer(this, 8, 0);
        this.guardianEye.addBox(-1.0F, 15.0F, 0.0F, 2, 2, 1);
        this.guardianBody.addChild(this.guardianEye);
        this.guardianTail = new ModelRenderer[3];
        this.guardianTail[0] = new ModelRenderer(this, 40, 0);
        this.guardianTail[0].addBox(-2.0F, 14.0F, 7.0F, 4, 4, 8);
        this.guardianTail[1] = new ModelRenderer(this, 0, 54);
        this.guardianTail[1].addBox(0.0F, 14.0F, 0.0F, 3, 3, 7);
        this.guardianTail[2] = new ModelRenderer(this);
        this.guardianTail[2].setTextureOffset(41, 32).addBox(0.0F, 14.0F, 0.0F, 2, 2, 6);
        this.guardianTail[2].setTextureOffset(25, 19).addBox(1.0F, 10.5F, 3.0F, 1, 9, 9);
        this.guardianBody.addChild(this.guardianTail[0]);
        this.guardianTail[0].addChild(this.guardianTail[1]);
        this.guardianTail[1].addChild(this.guardianTail[2]);
    }

    public int getModelVersion()
    {
        return 54;
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.guardianBody.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        EntityGuardian entityGuardian = (EntityGuardian)entityIn;
        float partialTicks = ageInTicks - (float)entityGuardian.ticksExisted;
        float degreesPerRadian = (180F / (float)Math.PI);
        this.guardianBody.rotateAngleY = netHeadYaw / degreesPerRadian;
        this.guardianBody.rotateAngleX = headPitch / degreesPerRadian;
        float[] spineRotateX = new float[] {1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
        float[] spineRotateY = new float[] {0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
        float[] spineRotateZ = new float[] {0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
        float[] spinePointX = new float[] {0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
        float[] spinePointY = new float[] { -8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
        float[] spinePointZ = new float[] {8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
        float spikeRetraction = (1.0F - entityGuardian.getSpikesAnimation(partialTicks)) * 0.55F;

        for (int spineIndex = 0; spineIndex < 12; ++spineIndex)
        {
            this.guardianSpines[spineIndex].rotateAngleX = (float)Math.PI * spineRotateX[spineIndex];
            this.guardianSpines[spineIndex].rotateAngleY = (float)Math.PI * spineRotateY[spineIndex];
            this.guardianSpines[spineIndex].rotateAngleZ = (float)Math.PI * spineRotateZ[spineIndex];
            this.guardianSpines[spineIndex].rotationPointX = spinePointX[spineIndex] * (1.0F + MathHelper.cos(ageInTicks * 1.5F + (float)spineIndex) * 0.01F - spikeRetraction);
            this.guardianSpines[spineIndex].rotationPointY = 16.0F + spinePointY[spineIndex] * (1.0F + MathHelper.cos(ageInTicks * 1.5F + (float)spineIndex) * 0.01F - spikeRetraction);
            this.guardianSpines[spineIndex].rotationPointZ = spinePointZ[spineIndex] * (1.0F + MathHelper.cos(ageInTicks * 1.5F + (float)spineIndex) * 0.01F - spikeRetraction);
        }

        this.guardianEye.rotationPointZ = -8.25F;
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

        if (entityGuardian.hasTargetedEntity())
        {
            entity = entityGuardian.getTargetedEntity();
        }

        if (entity != null)
        {
            Vec3 targetEyes = entity.getPositionEyes(0.0F);
            Vec3 guardianEyes = entityIn.getPositionEyes(0.0F);
            double eyeYOffset = targetEyes.yCoord - guardianEyes.yCoord;

            if (eyeYOffset > 0.0D)
            {
                this.guardianEye.rotationPointY = 0.0F;
            }
            else
            {
                this.guardianEye.rotationPointY = 1.0F;
            }

            Vec3 lookVector = entityIn.getLook(0.0F);
            lookVector = new Vec3(lookVector.xCoord, 0.0D, lookVector.zCoord);
            Vec3 sideVectorToTarget = (new Vec3(guardianEyes.xCoord - targetEyes.xCoord, 0.0D, guardianEyes.zCoord - targetEyes.zCoord)).normalize().rotateYaw(((float)Math.PI / 2F));
            double eyeSideOffset = lookVector.dotProduct(sideVectorToTarget);
            this.guardianEye.rotationPointX = MathHelper.sqrt_float((float)Math.abs(eyeSideOffset)) * 2.0F * (float)Math.signum(eyeSideOffset);
        }

        this.guardianEye.showModel = true;
        float tailAnimation = entityGuardian.getTailAnimation(partialTicks);
        this.guardianTail[0].rotateAngleY = MathHelper.sin(tailAnimation) * (float)Math.PI * 0.05F;
        this.guardianTail[1].rotateAngleY = MathHelper.sin(tailAnimation) * (float)Math.PI * 0.1F;
        this.guardianTail[1].rotationPointX = -1.5F;
        this.guardianTail[1].rotationPointY = 0.5F;
        this.guardianTail[1].rotationPointZ = 14.0F;
        this.guardianTail[2].rotateAngleY = MathHelper.sin(tailAnimation) * (float)Math.PI * 0.15F;
        this.guardianTail[2].rotationPointX = 0.5F;
        this.guardianTail[2].rotationPointY = 0.5F;
        this.guardianTail[2].rotationPointZ = 6.0F;
    }
}
