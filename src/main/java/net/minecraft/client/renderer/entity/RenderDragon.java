package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelDragon;
import optimization.FastTrig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonDeath;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonEyes;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderDragon extends RenderLiving<EntityDragon>
{
    private static final ResourceLocation enderDragonCrystalBeamTextures = new ResourceLocation("textures/entity/endercrystal/endercrystal_beam.png");
    private static final ResourceLocation enderDragonExplodingTextures = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation enderDragonTextures = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    protected ModelDragon modelDragon;

    public RenderDragon(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelDragon(0.0F), 0.5F);
        this.modelDragon = (ModelDragon)this.mainModel;
        this.addLayer(new LayerEnderDragonEyes(this));
        this.addLayer(new LayerEnderDragonDeath());
    }

    protected void rotateCorpse(EntityDragon bat, float ageInTicks, float rotationYaw, float partialTicks)
    {
        float bodyYaw = (float)bat.getMovementOffsets(7, partialTicks)[0];
        float pitchDelta = (float)(bat.getMovementOffsets(5, partialTicks)[1] - bat.getMovementOffsets(10, partialTicks)[1]);
        GlStateManager.rotate(-bodyYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitchDelta * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

        if (bat.deathTime > 0)
        {
            float deathRotationProgress = ((float)bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            deathRotationProgress = MathHelper.sqrt_float(deathRotationProgress);

            if (deathRotationProgress > 1.0F)
            {
                deathRotationProgress = 1.0F;
            }

            GlStateManager.rotate(deathRotationProgress * this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
        }
    }

    protected void renderModel(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
    {
        if (entitylivingbaseIn.deathTicks > 0)
        {
            float deathAlpha = (float)entitylivingbaseIn.deathTicks / 200.0F;
            GlStateManager.depthFunc(515);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, deathAlpha);
            this.bindTexture(enderDragonExplodingTextures);
            this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.depthFunc(514);
        }

        this.bindEntityTexture(entitylivingbaseIn);
        this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

        if (entitylivingbaseIn.hurtTime > 0)
        {
            GlStateManager.depthFunc(514);
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.color(1.0F, 0.0F, 0.0F, 0.5F);
            this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.depthFunc(515);
        }
    }

    public void doRender(EntityDragon entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        BossStatus.setBossStatus(entity, false);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        if (entity.healingEnderCrystal != null)
        {
            this.drawRechargeRay(entity, x, y, z, partialTicks);
        }
    }

    protected void drawRechargeRay(EntityDragon dragon, double x, double y, double z, float partialTicks)
    {
        float crystalRotation = (float)dragon.healingEnderCrystal.innerRotation + partialTicks;
        float crystalBobOffset = MathHelper.sin(crystalRotation * 0.2F) / 2.0F + 0.5F;
        crystalBobOffset = (crystalBobOffset * crystalBobOffset + crystalBobOffset) * 0.2F;
        float beamX = (float)(dragon.healingEnderCrystal.posX - dragon.posX - (dragon.prevPosX - dragon.posX) * (double)(1.0F - partialTicks));
        float beamY = (float)((double)crystalBobOffset + dragon.healingEnderCrystal.posY - 1.0D - dragon.posY - (dragon.prevPosY - dragon.posY) * (double)(1.0F - partialTicks));
        float beamZ = (float)(dragon.healingEnderCrystal.posZ - dragon.posZ - (dragon.prevPosZ - dragon.posZ) * (double)(1.0F - partialTicks));
        float horizontalDistance = MathHelper.sqrt_float(beamX * beamX + beamZ * beamZ);
        float beamLength = MathHelper.sqrt_float(beamX * beamX + beamY * beamY + beamZ * beamZ);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y + 2.0F, (float)z);
        GlStateManager.rotate((float)(-FastTrig.atan2((double)beamZ, (double)beamX)) * 180.0F / (float)Math.PI - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float)(-FastTrig.atan2((double)horizontalDistance, (double)beamY)) * 180.0F / (float)Math.PI - 90.0F, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        this.bindTexture(enderDragonCrystalBeamTextures);
        GlStateManager.shadeModel(7425);
        float textureStartV = 0.0F - ((float)dragon.ticksExisted + partialTicks) * 0.01F;
        float textureEndV = beamLength / 32.0F - ((float)dragon.ticksExisted + partialTicks) * 0.01F;
        worldRenderer.begin(5, DefaultVertexFormats.POSITION_TEX_COLOR);
        int segmentCount = 8;

        for (int segmentIndex = 0; segmentIndex <= segmentCount; ++segmentIndex)
        {
            float ringX = MathHelper.sin((float)(segmentIndex % segmentCount) * (float)Math.PI * 2.0F / (float)segmentCount) * 0.75F;
            float ringY = MathHelper.cos((float)(segmentIndex % segmentCount) * (float)Math.PI * 2.0F / (float)segmentCount) * 0.75F;
            float textureU = (float)(segmentIndex % segmentCount) * 1.0F / (float)segmentCount;
            worldRenderer.pos((double)(ringX * 0.2F), (double)(ringY * 0.2F), 0.0D).tex((double)textureU, (double)textureEndV).color(0, 0, 0, 255).endVertex();
            worldRenderer.pos((double)ringX, (double)ringY, (double)beamLength).tex((double)textureU, (double)textureStartV).color(255, 255, 255, 255).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(7424);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    protected ResourceLocation getEntityTexture(EntityDragon entity)
    {
        return enderDragonTextures;
    }
}
