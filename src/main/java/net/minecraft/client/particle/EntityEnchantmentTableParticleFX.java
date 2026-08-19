package net.minecraft.client.particle;

import net.minecraft.world.World;

public class EntityEnchantmentTableParticleFX extends EntityFX
{
    private float initialScale;
    private double coordX;
    private double coordY;
    private double coordZ;

    protected EntityEnchantmentTableParticleFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        this.coordX = xCoordIn;
        this.coordY = yCoordIn;
        this.coordZ = zCoordIn;
        this.posX = this.prevPosX = xCoordIn + xSpeedIn;
        this.posY = this.prevPosY = yCoordIn + ySpeedIn;
        this.posZ = this.prevPosZ = zCoordIn + zSpeedIn;
        float colorScale = this.rand.nextFloat() * 0.6F + 0.4F;
        this.initialScale = this.particleScale = this.rand.nextFloat() * 0.5F + 0.2F;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F * colorScale;
        this.particleGreen *= 0.9F;
        this.particleRed *= 0.9F;
        this.particleMaxAge = (int)(this.rand.nextDouble() * 10.0D) + 30;
        this.noClip = true;
        this.setParticleTextureIndex((int)(this.rand.nextDouble() * 26.0D + 1.0D + 224.0D));
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
        float ageProgress = (float)this.particleAge / (float)this.particleMaxAge;
        ageProgress = ageProgress * ageProgress;
        ageProgress = ageProgress * ageProgress;
        return baseBrightness * (1.0F - ageProgress) + ageProgress;
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        float motionProgress = (float)this.particleAge / (float)this.particleMaxAge;
        motionProgress = 1.0F - motionProgress;
        float fallProgress = 1.0F - motionProgress;
        fallProgress = fallProgress * fallProgress;
        fallProgress = fallProgress * fallProgress;
        this.posX = this.coordX + this.motionX * (double)motionProgress;
        this.posY = this.coordY + this.motionY * (double)motionProgress - (double)(fallProgress * 1.2F);
        this.posZ = this.coordZ + this.motionZ * (double)motionProgress;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
    }

    public static class EnchantmentTable implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityEnchantmentTableParticleFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
