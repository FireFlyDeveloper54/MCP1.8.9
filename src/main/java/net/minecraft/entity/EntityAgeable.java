package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAgeable extends EntityCreature
{
    protected int growingAge;
    protected int forcedAge;
    protected int forcedAgeTimer;
    private float ageWidth = -1.0F;
    private float ageHeight;

    public EntityAgeable(World worldIn)
    {
        super(worldIn);
    }

    public abstract EntityAgeable createChild(EntityAgeable ageable);

    public boolean interact(EntityPlayer player)
    {
        ItemStack itemStack = player.inventory.getCurrentItem();

        if (itemStack != null && itemStack.getItem() == Items.spawn_egg)
        {
            if (!this.worldObj.isRemote)
            {
                Class <? extends Entity > oclass = EntityList.getClassFromID(itemStack.getMetadata());

                if (oclass != null && this.getClass() == oclass)
                {
                    EntityAgeable entityAgeable = this.createChild(this);

                    if (entityAgeable != null)
                    {
                        entityAgeable.setGrowingAge(-24000);
                        entityAgeable.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
                        this.worldObj.spawnEntityInWorld(entityAgeable);

                        if (itemStack.hasDisplayName())
                        {
                            entityAgeable.setCustomNameTag(itemStack.getDisplayName());
                        }

                        if (!player.capabilities.isCreativeMode)
                        {
                            --itemStack.stackSize;

                            if (itemStack.stackSize <= 0)
                            {
                                player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
                            }
                        }
                    }
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(12, Byte.valueOf((byte)0));
    }

    public int getGrowingAge()
    {
        return this.worldObj.isRemote ? this.dataWatcher.getWatchableObjectByte(12) : this.growingAge;
    }

    public void addGrowth(int growth, boolean updateForcedAge)
    {
        int i = this.getGrowingAge();
        int j = i;
        i = i + growth * 20;

        if (i > 0)
        {
            i = 0;

            if (j < 0)
            {
                this.onGrowingAdult();
            }
        }

        int k = i - j;
        this.setGrowingAge(i);

        if (updateForcedAge)
        {
            this.forcedAge += k;

            if (this.forcedAgeTimer == 0)
            {
                this.forcedAgeTimer = 40;
            }
        }

        if (this.getGrowingAge() == 0)
        {
            this.setGrowingAge(this.forcedAge);
        }
    }

    public void addGrowth(int growth)
    {
        this.addGrowth(growth, false);
    }

    public void setGrowingAge(int age)
    {
        this.dataWatcher.updateObject(12, Byte.valueOf((byte)MathHelper.clamp_int(age, -1, 1)));
        this.growingAge = age;
        this.setScaleForAge(this.isChild());
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Age", this.getGrowingAge());
        tagCompound.setInteger("ForcedAge", this.forcedAge);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        this.setGrowingAge(tagCompund.getInteger("Age"));
        this.forcedAge = tagCompund.getInteger("ForcedAge");
    }

    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        if (this.worldObj.isRemote)
        {
            if (this.forcedAgeTimer > 0)
            {
                if (this.forcedAgeTimer % 4 == 0)
                {
                    this.worldObj.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }

                --this.forcedAgeTimer;
            }

            this.setScaleForAge(this.isChild());
        }
        else
        {
            int i = this.getGrowingAge();

            if (i < 0)
            {
                ++i;
                this.setGrowingAge(i);

                if (i == 0)
                {
                    this.onGrowingAdult();
                }
            }
            else if (i > 0)
            {
                --i;
                this.setGrowingAge(i);
            }
        }
    }

    protected void onGrowingAdult()
    {
    }

    public boolean isChild()
    {
        return this.getGrowingAge() < 0;
    }

    public void setScaleForAge(boolean isChild)
    {
        this.setScale(isChild ? 0.5F : 1.0F);
    }

    protected final void setSize(float width, float height)
    {
        boolean flag = this.ageWidth > 0.0F;
        this.ageWidth = width;
        this.ageHeight = height;

        if (!flag)
        {
            this.setScale(1.0F);
        }
    }

    protected final void setScale(float scale)
    {
        super.setSize(this.ageWidth * scale, this.ageHeight * scale);
    }
}
