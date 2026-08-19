package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityCrit2FX extends EntityFX
{
    float baseScale;

    protected EntityCrit2FX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, 1.0F);
    }

    protected EntityCrit2FX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float scale)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += xSpeedIn * 0.4D;
        this.motionY += ySpeedIn * 0.4D;
        this.motionZ += zSpeedIn * 0.4D;
        this.particleRed = this.particleGreen = this.particleBlue = (float)(this.rand.nextDouble() * 0.30000001192092896D + 0.6000000238418579D);
        this.particleScale *= 0.75F;
        this.particleScale *= scale;
        this.baseScale = this.particleScale;
        this.particleMaxAge = (int)(6.0D / (this.rand.nextDouble() * 0.8D + 0.6D));
        this.particleMaxAge = (int)((float)this.particleMaxAge * scale);
        this.noClip = false;
        this.setParticleTextureIndex(65);
        this.onUpdate();
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float scaleProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
        scaleProgress = MathHelper.clamp_float(scaleProgress, 0.0F, 1.0F);
        this.particleScale = this.baseScale * scaleProgress;
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

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.particleGreen = (float)((double)this.particleGreen * 0.96D);
        this.particleBlue = (float)((double)this.particleBlue * 0.9D);
        this.motionX *= 0.699999988079071D;
        this.motionY *= 0.699999988079071D;
        this.motionZ *= 0.699999988079071D;
        this.motionY -= 0.019999999552965164D;

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
            return new EntityCrit2FX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }

    public static class MagicFactory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            EntityFX entityFX = new EntityCrit2FX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
            entityFX.setRBGColorF(entityFX.getRedColorF() * 0.3F, entityFX.getGreenColorF() * 0.8F, entityFX.getBlueColorF());
            entityFX.nextTextureIndexX();
            return entityFX;
        }
    }
}
