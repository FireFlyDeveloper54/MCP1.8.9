package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityHugeExplodeFX extends EntityFX
{
    private int timeSinceStart;
    private int maximumTime = 8;

    protected EntityHugeExplodeFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
    }

    public void onUpdate()
    {
        for (int particleIndex = 0; particleIndex < 6; ++particleIndex)
        {
            double particleX = this.posX + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
            double particleY = this.posY + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
            double particleZ = this.posZ + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, particleX, particleY, particleZ, (double)((float)this.timeSinceStart / (float)this.maximumTime), 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }

        ++this.timeSinceStart;

        if (this.timeSinceStart == this.maximumTime)
        {
            this.setDead();
        }
    }

    public int getFXLayer()
    {
        return 1;
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityHugeExplodeFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
