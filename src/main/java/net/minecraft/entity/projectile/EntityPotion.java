package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityPotion extends EntityThrowable
{
    private ItemStack potionDamage;

    public EntityPotion(World worldIn)
    {
        super(worldIn);
    }

    public EntityPotion(World worldIn, EntityLivingBase throwerIn, int meta)
    {
        this(worldIn, throwerIn, new ItemStack(Items.potionitem, 1, meta));
    }

    public EntityPotion(World worldIn, EntityLivingBase throwerIn, ItemStack potionDamageIn)
    {
        super(worldIn, throwerIn);
        this.potionDamage = potionDamageIn;
    }

    public EntityPotion(World worldIn, double x, double y, double z, int potionMetadata)
    {
        this(worldIn, x, y, z, new ItemStack(Items.potionitem, 1, potionMetadata));
    }

    public EntityPotion(World worldIn, double x, double y, double z, ItemStack potionDamageIn)
    {
        super(worldIn, x, y, z);
        this.potionDamage = potionDamageIn;
    }

    protected float getGravityVelocity()
    {
        return 0.05F;
    }

    protected float getVelocity()
    {
        return 0.5F;
    }

    protected float getInaccuracy()
    {
        return -20.0F;
    }

    public void setPotionDamage(int potionId)
    {
        if (this.potionDamage == null)
        {
            this.potionDamage = new ItemStack(Items.potionitem, 1, 0);
        }

        this.potionDamage.setItemDamage(potionId);
    }

    public int getPotionDamage()
    {
        if (this.potionDamage == null)
        {
            this.potionDamage = new ItemStack(Items.potionitem, 1, 0);
        }

        return this.potionDamage.getMetadata();
    }

    protected void onImpact(MovingObjectPosition impactResult)
    {
        if (!this.worldObj.isRemote)
        {
            List<PotionEffect> list = Items.potionitem.getEffects(this.potionDamage);

            if (list != null && !list.isEmpty())
            {
                AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
                List<EntityLivingBase> list1 = this.worldObj.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

                if (!list1.isEmpty())
                {
                    for (EntityLivingBase entityLivingBase : list1)
                    {
                        double distanceSqToTarget = this.getDistanceSqToEntity(entityLivingBase);

                        if (distanceSqToTarget < 16.0D)
                        {
                            double splashIntensity = 1.0D - MathHelper.fastSqrt_double(distanceSqToTarget) / 4.0D;

                            if (entityLivingBase == impactResult.entityHit)
                            {
                                splashIntensity = 1.0D;
                            }

                            for (PotionEffect potionEffect : list)
                            {
                                int potionId = potionEffect.getPotionID();

                                if (Potion.potionTypes[potionId].isInstant())
                                {
                                    Potion.potionTypes[potionId].affectEntity(this, this.getThrower(), entityLivingBase, potionEffect.getAmplifier(), splashIntensity);
                                }
                                else
                                {
                                    int adjustedDuration = (int)(splashIntensity * (double)potionEffect.getDuration() + 0.5D);

                                    if (adjustedDuration > 20)
                                    {
                                        entityLivingBase.addPotionEffect(new PotionEffect(potionId, adjustedDuration, potionEffect.getAmplifier()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.worldObj.playAuxSFX(2002, new BlockPos(this), this.getPotionDamage());
            this.setDead();
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("Potion", 10))
        {
            this.potionDamage = ItemStack.loadItemStackFromNBT(tagCompund.getCompoundTag("Potion"));
        }
        else
        {
            this.setPotionDamage(tagCompund.getInteger("potionValue"));
        }

        if (this.potionDamage == null)
        {
            this.setDead();
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);

        if (this.potionDamage != null)
        {
            tagCompound.setTag("Potion", this.potionDamage.writeToNBT(new NBTTagCompound()));
        }
    }
}
