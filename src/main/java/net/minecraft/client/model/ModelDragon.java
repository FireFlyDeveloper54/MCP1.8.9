package net.minecraft.client.model;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;

public class ModelDragon extends ModelBase
{
    private ModelRenderer head;
    private ModelRenderer spine;
    private ModelRenderer jaw;
    private ModelRenderer body;
    private ModelRenderer rearLeg;
    private ModelRenderer frontLeg;
    private ModelRenderer rearLegTip;
    private ModelRenderer frontLegTip;
    private ModelRenderer rearFoot;
    private ModelRenderer frontFoot;
    private ModelRenderer wing;
    private ModelRenderer wingTip;
    private float partialTicks;

    public ModelDragon(float modelSize)
    {
        this.textureWidth = 256;
        this.textureHeight = 256;
        this.setTextureOffset("body.body", 0, 0);
        this.setTextureOffset("wing.skin", -56, 88);
        this.setTextureOffset("wingtip.skin", -56, 144);
        this.setTextureOffset("rearleg.main", 0, 0);
        this.setTextureOffset("rearfoot.main", 112, 0);
        this.setTextureOffset("rearlegtip.main", 196, 0);
        this.setTextureOffset("head.upperhead", 112, 30);
        this.setTextureOffset("wing.bone", 112, 88);
        this.setTextureOffset("head.upperlip", 176, 44);
        this.setTextureOffset("jaw.jaw", 176, 65);
        this.setTextureOffset("frontleg.main", 112, 104);
        this.setTextureOffset("wingtip.bone", 112, 136);
        this.setTextureOffset("frontfoot.main", 144, 104);
        this.setTextureOffset("neck.box", 192, 104);
        this.setTextureOffset("frontlegtip.main", 226, 138);
        this.setTextureOffset("body.scale", 220, 53);
        this.setTextureOffset("head.scale", 0, 0);
        this.setTextureOffset("neck.scale", 48, 0);
        this.setTextureOffset("head.nostril", 112, 0);
        float headOffsetZ = -16.0F;
        this.head = new ModelRenderer(this, "head");
        this.head.addBox("upperlip", -6.0F, -1.0F, -8.0F + headOffsetZ, 12, 5, 16);
        this.head.addBox("upperhead", -8.0F, -8.0F, 6.0F + headOffsetZ, 16, 16, 16);
        this.head.mirror = true;
        this.head.addBox("scale", -5.0F, -12.0F, 12.0F + headOffsetZ, 2, 4, 6);
        this.head.addBox("nostril", -5.0F, -3.0F, -6.0F + headOffsetZ, 2, 2, 4);
        this.head.mirror = false;
        this.head.addBox("scale", 3.0F, -12.0F, 12.0F + headOffsetZ, 2, 4, 6);
        this.head.addBox("nostril", 3.0F, -3.0F, -6.0F + headOffsetZ, 2, 2, 4);
        this.jaw = new ModelRenderer(this, "jaw");
        this.jaw.setRotationPoint(0.0F, 4.0F, 8.0F + headOffsetZ);
        this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16);
        this.head.addChild(this.jaw);
        this.spine = new ModelRenderer(this, "neck");
        this.spine.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10);
        this.spine.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6);
        this.body = new ModelRenderer(this, "body");
        this.body.setRotationPoint(0.0F, 4.0F, 8.0F);
        this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64);
        this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12);
        this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12);
        this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12);
        this.wing = new ModelRenderer(this, "wing");
        this.wing.setRotationPoint(-12.0F, 5.0F, 2.0F);
        this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8);
        this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
        this.wingTip = new ModelRenderer(this, "wingtip");
        this.wingTip.setRotationPoint(-56.0F, 0.0F, 0.0F);
        this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4);
        this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
        this.wing.addChild(this.wingTip);
        this.frontLeg = new ModelRenderer(this, "frontleg");
        this.frontLeg.setRotationPoint(-12.0F, 20.0F, 2.0F);
        this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8);
        this.frontLegTip = new ModelRenderer(this, "frontlegtip");
        this.frontLegTip.setRotationPoint(0.0F, 20.0F, -1.0F);
        this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6);
        this.frontLeg.addChild(this.frontLegTip);
        this.frontFoot = new ModelRenderer(this, "frontfoot");
        this.frontFoot.setRotationPoint(0.0F, 23.0F, 0.0F);
        this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16);
        this.frontLegTip.addChild(this.frontFoot);
        this.rearLeg = new ModelRenderer(this, "rearleg");
        this.rearLeg.setRotationPoint(-16.0F, 16.0F, 42.0F);
        this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16);
        this.rearLegTip = new ModelRenderer(this, "rearlegtip");
        this.rearLegTip.setRotationPoint(0.0F, 32.0F, -4.0F);
        this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12);
        this.rearLeg.addChild(this.rearLegTip);
        this.rearFoot = new ModelRenderer(this, "rearfoot");
        this.rearFoot.setRotationPoint(0.0F, 31.0F, 4.0F);
        this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24);
        this.rearLegTip.addChild(this.rearFoot);
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        this.partialTicks = partialTickTime;
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        GlStateManager.pushMatrix();
        EntityDragon entityDragon = (EntityDragon)entityIn;
        float animTime = entityDragon.prevAnimTime + (entityDragon.animTime - entityDragon.prevAnimTime) * this.partialTicks;
        this.jaw.rotateAngleX = (float)(Math.sin((double)(animTime * (float)Math.PI * 2.0F)) + 1.0D) * 0.2F;
        float bobOffset = (float)(Math.sin((double)(animTime * (float)Math.PI * 2.0F - 1.0F)) + 1.0D);
        bobOffset = (bobOffset * bobOffset * 1.0F + bobOffset * 2.0F) * 0.05F;
        GlStateManager.translate(0.0F, bobOffset - 2.0F, -3.0F);
        GlStateManager.rotate(bobOffset * 2.0F, 1.0F, 0.0F, 0.0F);
        float segmentY = -30.0F;
        float segmentX = 0.0F;
        float segmentRotationScale = 1.5F;
        double[] bodyOffsets = entityDragon.getMovementOffsets(6, this.partialTicks);
        float bodyYawDelta = this.updateRotations(entityDragon.getMovementOffsets(5, this.partialTicks)[0] - entityDragon.getMovementOffsets(10, this.partialTicks)[0]);
        float baseYaw = this.updateRotations(entityDragon.getMovementOffsets(5, this.partialTicks)[0] + (double)(bodyYawDelta / 2.0F));
        segmentY = segmentY + 2.0F;
        float animationPhase = animTime * (float)Math.PI * 2.0F;
        segmentY = 20.0F;
        float segmentZ = -12.0F;

        for (int neckIndex = 0; neckIndex < 5; ++neckIndex)
        {
            double[] neckOffsets = entityDragon.getMovementOffsets(5 - neckIndex, this.partialTicks);
            float neckWave = (float)Math.cos((double)((float)neckIndex * 0.45F + animationPhase)) * 0.15F;
            this.spine.rotateAngleY = this.updateRotations(neckOffsets[0] - bodyOffsets[0]) * (float)Math.PI / 180.0F * segmentRotationScale;
            this.spine.rotateAngleX = neckWave + (float)(neckOffsets[1] - bodyOffsets[1]) * (float)Math.PI / 180.0F * segmentRotationScale * 5.0F;
            this.spine.rotateAngleZ = -this.updateRotations(neckOffsets[0] - (double)baseYaw) * (float)Math.PI / 180.0F * segmentRotationScale;
            this.spine.rotationPointY = segmentY;
            this.spine.rotationPointZ = segmentZ;
            this.spine.rotationPointX = segmentX;
            segmentY = (float)((double)segmentY + Math.sin((double)this.spine.rotateAngleX) * 10.0D);
            segmentZ = (float)((double)segmentZ - Math.cos((double)this.spine.rotateAngleY) * Math.cos((double)this.spine.rotateAngleX) * 10.0D);
            segmentX = (float)((double)segmentX - Math.sin((double)this.spine.rotateAngleY) * Math.cos((double)this.spine.rotateAngleX) * 10.0D);
            this.spine.render(scale);
        }

        this.head.rotationPointY = segmentY;
        this.head.rotationPointZ = segmentZ;
        this.head.rotationPointX = segmentX;
        double[] headOffsets = entityDragon.getMovementOffsets(0, this.partialTicks);
        this.head.rotateAngleY = this.updateRotations(headOffsets[0] - bodyOffsets[0]) * (float)Math.PI / 180.0F * 1.0F;
        this.head.rotateAngleZ = -this.updateRotations(headOffsets[0] - (double)baseYaw) * (float)Math.PI / 180.0F * 1.0F;
        this.head.render(scale);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-bodyYawDelta * segmentRotationScale * 1.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.0F, 0.0F);
        this.body.rotateAngleZ = 0.0F;
        this.body.render(scale);

        for (int sideIndex = 0; sideIndex < 2; ++sideIndex)
        {
            GlStateManager.enableCull();
            float wingPhase = animTime * (float)Math.PI * 2.0F;
            this.wing.rotateAngleX = 0.125F - (float)Math.cos((double)wingPhase) * 0.2F;
            this.wing.rotateAngleY = 0.25F;
            this.wing.rotateAngleZ = (float)(Math.sin((double)wingPhase) + 0.125D) * 0.8F;
            this.wingTip.rotateAngleZ = -((float)(Math.sin((double)(wingPhase + 2.0F)) + 0.5D)) * 0.75F;
            this.rearLeg.rotateAngleX = 1.0F + bobOffset * 0.1F;
            this.rearLegTip.rotateAngleX = 0.5F + bobOffset * 0.1F;
            this.rearFoot.rotateAngleX = 0.75F + bobOffset * 0.1F;
            this.frontLeg.rotateAngleX = 1.3F + bobOffset * 0.1F;
            this.frontLegTip.rotateAngleX = -0.5F - bobOffset * 0.1F;
            this.frontFoot.rotateAngleX = 0.75F + bobOffset * 0.1F;
            this.wing.render(scale);
            this.frontLeg.render(scale);
            this.rearLeg.render(scale);
            GlStateManager.scale(-1.0F, 1.0F, 1.0F);

            if (sideIndex == 0)
            {
                GlStateManager.cullFace(1028);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.cullFace(1029);
        GlStateManager.disableCull();
        float tailWave = -((float)Math.sin((double)(animTime * (float)Math.PI * 2.0F))) * 0.0F;
        animationPhase = animTime * (float)Math.PI * 2.0F;
        segmentY = 10.0F;
        segmentZ = 60.0F;
        segmentX = 0.0F;
        double[] tailBaseOffsets = entityDragon.getMovementOffsets(11, this.partialTicks);

        for (int tailIndex = 0; tailIndex < 12; ++tailIndex)
        {
            double[] tailOffsets = entityDragon.getMovementOffsets(12 + tailIndex, this.partialTicks);
            tailWave = (float)((double)tailWave + Math.sin((double)((float)tailIndex * 0.45F + animationPhase)) * 0.05000000074505806D);
            this.spine.rotateAngleY = (this.updateRotations(tailOffsets[0] - tailBaseOffsets[0]) * segmentRotationScale + 180.0F) * (float)Math.PI / 180.0F;
            this.spine.rotateAngleX = tailWave + (float)(tailOffsets[1] - tailBaseOffsets[1]) * (float)Math.PI / 180.0F * segmentRotationScale * 5.0F;
            this.spine.rotateAngleZ = this.updateRotations(tailOffsets[0] - (double)baseYaw) * (float)Math.PI / 180.0F * segmentRotationScale;
            this.spine.rotationPointY = segmentY;
            this.spine.rotationPointZ = segmentZ;
            this.spine.rotationPointX = segmentX;
            segmentY = (float)((double)segmentY + Math.sin((double)this.spine.rotateAngleX) * 10.0D);
            segmentZ = (float)((double)segmentZ - Math.cos((double)this.spine.rotateAngleY) * Math.cos((double)this.spine.rotateAngleX) * 10.0D);
            segmentX = (float)((double)segmentX - Math.sin((double)this.spine.rotateAngleY) * Math.cos((double)this.spine.rotateAngleX) * 10.0D);
            this.spine.render(scale);
        }

        GlStateManager.popMatrix();
    }

    private float updateRotations(double angle)
    {
        while (angle >= 180.0D)
        {
            angle -= 360.0D;
        }

        while (angle < -180.0D)
        {
            angle += 360.0D;
        }

        return (float)angle;
    }
}
