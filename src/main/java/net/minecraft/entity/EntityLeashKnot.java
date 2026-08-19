package net.minecraft.entity;

import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class EntityLeashKnot extends EntityHanging
{
    public EntityLeashKnot(World worldIn)
    {
        super(worldIn);
    }

    public EntityLeashKnot(World worldIn, BlockPos hangingPositionIn)
    {
        super(worldIn, hangingPositionIn);
        this.setPosition((double)hangingPositionIn.getX() + 0.5D, (double)hangingPositionIn.getY() + 0.5D, (double)hangingPositionIn.getZ() + 0.5D);
        float f = 0.125F;
        float floatValue2 = 0.1875F;
        float floatValue3 = 0.25F;
        this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.1875D, this.posY - 0.25D + 0.125D, this.posZ - 0.1875D, this.posX + 0.1875D, this.posY + 0.25D + 0.125D, this.posZ + 0.1875D));
    }

    protected void entityInit()
    {
        super.entityInit();
    }

    public void updateFacingWithBoundingBox(EnumFacing facingDirectionIn)
    {
    }

    public int getWidthPixels()
    {
        return 9;
    }

    public int getHeightPixels()
    {
        return 9;
    }

    public float getEyeHeight()
    {
        return -0.0625F;
    }

    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 1024.0D;
    }

    public void onBroken(Entity brokenEntity)
    {
    }

    public boolean writeToNBTOptional(NBTTagCompound tagCompund)
    {
        return false;
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
    }

    public boolean interactFirst(EntityPlayer playerIn)
    {
        ItemStack itemStack = playerIn.getHeldItem();
        boolean flag = false;

        if (itemStack != null && itemStack.getItem() == Items.lead && !this.worldObj.isRemote)
        {
            double doubleValue = 7.0D;

            for (EntityLiving entityLiving : this.worldObj.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - doubleValue, this.posY - doubleValue, this.posZ - doubleValue, this.posX + doubleValue, this.posY + doubleValue, this.posZ + doubleValue)))
            {
                if (entityLiving.getLeashed() && entityLiving.getLeashedToEntity() == playerIn)
                {
                    entityLiving.setLeashedToEntity(this, true);
                    flag = true;
                }
            }
        }

        if (!this.worldObj.isRemote && !flag)
        {
            this.setDead();

            if (playerIn.capabilities.isCreativeMode)
            {
                double doubleValue2 = 7.0D;

                for (EntityLiving entityliving1 : this.worldObj.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - doubleValue2, this.posY - doubleValue2, this.posZ - doubleValue2, this.posX + doubleValue2, this.posY + doubleValue2, this.posZ + doubleValue2)))
                {
                    if (entityliving1.getLeashed() && entityliving1.getLeashedToEntity() == this)
                    {
                        entityliving1.clearLeashed(true, false);
                    }
                }
            }
        }

        return true;
    }

    public boolean onValidSurface()
    {
        return this.worldObj.getBlockState(this.hangingPosition).getBlock() instanceof BlockFence;
    }

    public static EntityLeashKnot createKnot(World worldIn, BlockPos fence)
    {
        EntityLeashKnot entityLeashKnot = new EntityLeashKnot(worldIn, fence);
        entityLeashKnot.forceSpawn = true;
        worldIn.spawnEntityInWorld(entityLeashKnot);
        return entityLeashKnot;
    }

    public static EntityLeashKnot getKnotForPosition(World worldIn, BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (EntityLeashKnot entityLeashKnot : worldIn.getEntitiesWithinAABB(EntityLeashKnot.class, new AxisAlignedBB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)))
        {
            if (entityLeashKnot.getHangingPosition().equals(pos))
            {
                return entityLeashKnot;
            }
        }

        return null;
    }
}
