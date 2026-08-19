package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityLavaFX extends EntityFX
{
    private float lavaParticleScale;

    protected EntityLavaFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.800000011920929D;
        this.motionY *= 0.800000011920929D;
        this.motionZ *= 0.800000011920929D;
        this.motionY = (double)(this.rand.nextFloat() * 0.4F + 0.05F);
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleScale *= this.rand.nextFloat() * 2.0F + 0.2F;
        this.lavaParticleScale = this.particleScale;
        this.particleMaxAge = (int)(16.0D / (this.rand.nextDouble() * 0.8D + 0.2D));
        this.noClip = false;
        this.setParticleTextureIndex(49);
    }

    public int getBrightnessForRender(float partialTicks)
    {
        float ageProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        ageProgress = MathHelper.clamp_float(ageProgress, 0.0F, 1.0F);
        int packedBrightness = super.getBrightnessForRender(partialTicks);
        int lightmapU = 240;
        int lightmapV = packedBrightness >> 16 & 255;
        return lightmapU | lightmapV << 16;
    }

    public float getBrightness(float partialTicks)
    {
        return 1.0F;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float ageProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        this.particleScale = this.lavaParticleScale * (1.0F - ageProgress * ageProgress);
        super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        float ageProgress = (float)this.particleAge / (float)this.particleMaxAge;

        if (this.rand.nextFloat() > ageProgress)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
        }

        this.motionY -= 0.03D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9990000128746033D;
        this.motionY *= 0.9990000128746033D;
        this.motionZ *= 0.9990000128746033D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityLavaFX(worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}
