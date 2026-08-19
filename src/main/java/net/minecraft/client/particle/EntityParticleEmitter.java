package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityParticleEmitter extends EntityFX
{
    private Entity attachedEntity;
    private int age;
    private int lifetime;
    private EnumParticleTypes particleTypes;

    public EntityParticleEmitter(World worldIn, Entity attachedEntityIn, EnumParticleTypes particleTypesIn)
    {
        super(worldIn, attachedEntityIn.posX, attachedEntityIn.getEntityBoundingBox().minY + (double)(attachedEntityIn.height / 2.0F), attachedEntityIn.posZ, attachedEntityIn.motionX, attachedEntityIn.motionY, attachedEntityIn.motionZ);
        this.attachedEntity = attachedEntityIn;
        this.lifetime = 3;
        this.particleTypes = particleTypesIn;
        this.onUpdate();
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
    }

    public void onUpdate()
    {
        for (int particleIndex = 0; particleIndex < 16; ++particleIndex)
        {
            double offsetX = (double)(this.rand.nextFloat() * 2.0F - 1.0F);
            double offsetY = (double)(this.rand.nextFloat() * 2.0F - 1.0F);
            double offsetZ = (double)(this.rand.nextFloat() * 2.0F - 1.0F);

            if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= 1.0D)
            {
                double particleX = this.attachedEntity.posX + offsetX * (double)this.attachedEntity.width / 4.0D;
                double particleY = this.attachedEntity.getEntityBoundingBox().minY + (double)(this.attachedEntity.height / 2.0F) + offsetY * (double)this.attachedEntity.height / 4.0D;
                double particleZ = this.attachedEntity.posZ + offsetZ * (double)this.attachedEntity.width / 4.0D;
                this.worldObj.spawnParticle(this.particleTypes, false, particleX, particleY, particleZ, offsetX, offsetY + 0.2D, offsetZ, EnumParticleTypes.EMPTY_ARGS);
            }
        }

        ++this.age;

        if (this.age >= this.lifetime)
        {
            this.setDead();
        }
    }

    public int getFXLayer()
    {
        return 3;
    }
}
