package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityReddustFX extends EntityFX
{
    float reddustParticleScale;

    protected EntityReddustFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float redIn, float greenIn, float blueIn)
    {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn, 1.0F, redIn, greenIn, blueIn);
    }

    protected EntityReddustFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float scale, float redIn, float greenIn, float blueIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;

        if (redIn == 0.0F)
        {
            redIn = 1.0F;
        }

        float colorRandomizer = (float)this.rand.nextDouble() * 0.4F + 0.6F;
        this.particleRed = ((float)(this.rand.nextDouble() * 0.20000000298023224D) + 0.8F) * redIn * colorRandomizer;
        this.particleGreen = ((float)(this.rand.nextDouble() * 0.20000000298023224D) + 0.8F) * greenIn * colorRandomizer;
        this.particleBlue = ((float)(this.rand.nextDouble() * 0.20000000298023224D) + 0.8F) * blueIn * colorRandomizer;
        this.particleScale *= 0.75F;
        this.particleScale *= scale;
        this.reddustParticleScale = this.particleScale;
        this.particleMaxAge = (int)(8.0D / (this.rand.nextDouble() * 0.8D + 0.2D));
        this.particleMaxAge = (int)((float)this.particleMaxAge * scale);
        this.noClip = false;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float scaleProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
        scaleProgress = MathHelper.clamp_float(scaleProgress, 0.0F, 1.0F);
        this.particleScale = this.reddustParticleScale * scaleProgress;
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

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.posY == this.prevPosY)
        {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

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
            return new EntityReddustFX(worldIn, xCoordIn, yCoordIn, zCoordIn, (float)xSpeedIn, (float)ySpeedIn, (float)zSpeedIn);
        }
    }
}
