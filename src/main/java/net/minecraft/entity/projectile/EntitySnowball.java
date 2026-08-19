package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntitySnowball extends EntityThrowable
{
    public EntitySnowball(World worldIn)
    {
        super(worldIn);
    }

    public EntitySnowball(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
    }

    public EntitySnowball(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    protected void onImpact(MovingObjectPosition movingObject)
    {
        if (movingObject.entityHit != null)
        {
            int damageAmount = 0;

            if (movingObject.entityHit instanceof EntityBlaze)
            {
                damageAmount = 3;
            }

            movingObject.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)damageAmount);
        }

        for (int particleIndex = 0; particleIndex < 8; ++particleIndex)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }

        if (!this.worldObj.isRemote)
        {
            this.setDead();
        }
    }
}
