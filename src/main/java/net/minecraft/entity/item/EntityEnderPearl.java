package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityEnderPearl extends EntityThrowable
{
    private EntityLivingBase thrower;

    public EntityEnderPearl(World worldIn)
    {
        super(worldIn);
    }

    public EntityEnderPearl(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
        this.thrower = throwerIn;
    }

    public EntityEnderPearl(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    protected void onImpact(MovingObjectPosition hitResult)
    {
        EntityLivingBase entityLivingBase = this.getThrower();

        if (hitResult.entityHit != null)
        {
            if (hitResult.entityHit == this.thrower)
            {
                return;
            }

            hitResult.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, entityLivingBase), 0.0F);
        }

        for (int i = 0; i < 32; ++i)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ, this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian(), EnumParticleTypes.EMPTY_ARGS);
        }

        if (!this.worldObj.isRemote)
        {
            if (entityLivingBase instanceof EntityPlayerMP)
            {
                EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityLivingBase;

                if (entityPlayerMP.playerNetServerHandler.getNetworkManager().isChannelOpen() && entityPlayerMP.worldObj == this.worldObj && !entityPlayerMP.isPlayerSleeping())
                {
                    if (this.rand.nextFloat() < 0.05F && this.worldObj.getGameRules().getBoolean("doMobSpawning"))
                    {
                        EntityEndermite entityEndermite = new EntityEndermite(this.worldObj);
                        entityEndermite.setSpawnedByPlayer(true);
                        entityEndermite.setLocationAndAngles(entityLivingBase.posX, entityLivingBase.posY, entityLivingBase.posZ, entityLivingBase.rotationYaw, entityLivingBase.rotationPitch);
                        this.worldObj.spawnEntityInWorld(entityEndermite);
                    }

                    if (entityLivingBase.isRiding())
                    {
                        entityLivingBase.mountEntity((Entity)null);
                    }

                    entityLivingBase.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                    entityLivingBase.fallDistance = 0.0F;
                    entityLivingBase.attackEntityFrom(DamageSource.fall, 5.0F);
                }
            }
            else if (entityLivingBase != null)
            {
                entityLivingBase.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                entityLivingBase.fallDistance = 0.0F;
            }

            this.setDead();
        }
    }

    public void onUpdate()
    {
        EntityLivingBase entityLivingBase = this.getThrower();

        if (entityLivingBase != null && entityLivingBase instanceof EntityPlayer && !entityLivingBase.isEntityAlive())
        {
            this.setDead();
        }
        else
        {
            super.onUpdate();
        }
    }
}
