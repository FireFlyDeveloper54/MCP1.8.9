package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityPortalFX extends EntityFX
{
    private float portalParticleScale;
    private double portalPosX;
    private double portalPosY;
    private double portalPosZ;

    protected EntityPortalFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        this.portalPosX = this.posX = xCoordIn;
        this.portalPosY = this.posY = yCoordIn;
        this.portalPosZ = this.posZ = zCoordIn;
        float colorScale = this.rand.nextFloat() * 0.6F + 0.4F;
        this.portalParticleScale = this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F * colorScale;
        this.particleGreen *= 0.3F;
        this.particleRed *= 0.9F;
        this.particleMaxAge = (int)(this.rand.nextDouble() * 10.0D) + 40;
        this.noClip = true;
        this.setParticleTextureIndex((int)(this.rand.nextDouble() * 8.0D));
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float scaleProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        scaleProgress = 1.0F - scaleProgress;
        scaleProgress = scaleProgress * scaleProgress;
        scaleProgress = 1.0F - scaleProgress;
        this.particleScale = this.portalParticleScale * scaleProgress;
        super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    public int getBrightnessForRender(float partialTicks)
    {
        int packedBrightness = super.getBrightnessForRender(partialTicks);
        float ageBrightness = (float)this.particleAge / (float)this.particleMaxAge;
        ageBrightness = ageBrightness * ageBrightness;
        ageBrightness = ageBrightness * ageBrightness;
        int lightmapU = packedBrightness & 255;
        int lightmapV = packedBrightness >> 16 & 255;
        lightmapV = lightmapV + (int)(ageBrightness * 15.0F * 16.0F);

        if (lightmapV > 240)
        {
            lightmapV = 240;
        }

        return lightmapU | lightmapV << 16;
    }

    public float getBrightness(float partialTicks)
    {
        float baseBrightness = super.getBrightness(partialTicks);
        float ageBrightnessFactor = (float)this.particleAge / (float)this.particleMaxAge;
        ageBrightnessFactor = ageBrightnessFactor * ageBrightnessFactor * ageBrightnessFactor * ageBrightnessFactor;
        return baseBrightness * (1.0F - ageBrightnessFactor) + ageBrightnessFactor;
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        float motionProgress = (float)this.particleAge / (float)this.particleMaxAge;
        motionProgress = -motionProgress + motionProgress * motionProgress * 2.0F;
        motionProgress = 1.0F - motionProgress;
        this.posX = this.portalPosX + this.motionX * (double)motionProgress;
        this.posY = this.portalPosY + this.motionY * (double)motionProgress + (double)(1.0F - motionProgress);
        this.posZ = this.portalPosZ + this.motionZ * (double)motionProgress;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityPortalFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
