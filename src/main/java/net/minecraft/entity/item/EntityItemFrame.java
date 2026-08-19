package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class EntityItemFrame extends EntityHanging
{
    private float itemDropChance = 1.0F;

    public EntityItemFrame(World worldIn)
    {
        super(worldIn);
    }

    public EntityItemFrame(World worldIn, BlockPos hangingPositionIn, EnumFacing facing)
    {
        super(worldIn, hangingPositionIn);
        this.updateFacingWithBoundingBox(facing);
    }

    protected void entityInit()
    {
        this.getDataWatcher().addObjectByDataType(8, 5);
        this.getDataWatcher().addObject(9, Byte.valueOf((byte)0));
    }

    public float getCollisionBorderSize()
    {
        return 0.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (!source.isExplosion() && this.getDisplayedItem() != null)
        {
            if (!this.worldObj.isRemote)
            {
                this.dropItemOrSelf(source.getEntity(), false);
                this.setDisplayedItem((ItemStack)null);
            }

            return true;
        }
        else
        {
            return super.attackEntityFrom(source, amount);
        }
    }

    public int getWidthPixels()
    {
        return 12;
    }

    public int getHeightPixels()
    {
        return 12;
    }

    public boolean isInRangeToRenderDist(double distance)
    {
        double maxRenderDistance = 16.0D;
        maxRenderDistance = maxRenderDistance * 64.0D * this.renderDistanceWeight;
        return distance < maxRenderDistance * maxRenderDistance;
    }

    public void onBroken(Entity brokenEntity)
    {
        this.dropItemOrSelf(brokenEntity, true);
    }

    public void dropItemOrSelf(Entity breaker, boolean dropFrame)
    {
        if (this.worldObj.getGameRules().getBoolean("doEntityDrops"))
        {
            ItemStack itemStack = this.getDisplayedItem();

            if (breaker instanceof EntityPlayer)
            {
                EntityPlayer entityPlayer = (EntityPlayer)breaker;

                if (entityPlayer.capabilities.isCreativeMode)
                {
                    this.removeFrameFromMap(itemStack);
                    return;
                }
            }

            if (dropFrame)
            {
                this.entityDropItem(new ItemStack(Items.item_frame), 0.0F);
            }

            if (itemStack != null && this.rand.nextFloat() < this.itemDropChance)
            {
                itemStack = itemStack.copy();
                this.removeFrameFromMap(itemStack);
                this.entityDropItem(itemStack, 0.0F);
            }
        }
    }

    private void removeFrameFromMap(ItemStack stack)
    {
        if (stack != null)
        {
            if (stack.getItem() == Items.filled_map)
            {
                MapData mapData = ((ItemMap)stack.getItem()).getMapData(stack, this.worldObj);
                mapData.mapDecorations.remove("frame-" + this.getEntityId());
            }

            stack.setItemFrame((EntityItemFrame)null);
        }
    }

    public ItemStack getDisplayedItem()
    {
        return this.getDataWatcher().getWatchableObjectItemStack(8);
    }

    public void setDisplayedItem(ItemStack stack)
    {
        this.setDisplayedItemWithUpdate(stack, true);
    }

    private void setDisplayedItemWithUpdate(ItemStack stack, boolean updateComparator)
    {
        if (stack != null)
        {
            stack = stack.copy();
            stack.stackSize = 1;
            stack.setItemFrame(this);
        }

        this.getDataWatcher().updateObject(8, stack);
        this.getDataWatcher().setObjectWatched(8);

        if (updateComparator && this.hangingPosition != null)
        {
            this.worldObj.updateComparatorOutputLevel(this.hangingPosition, Blocks.air);
        }
    }

    public int getRotation()
    {
        return this.getDataWatcher().getWatchableObjectByte(9);
    }

    public void setItemRotation(int rotation)
    {
        this.setItemRotationWithUpdate(rotation, true);
    }

    private void setItemRotationWithUpdate(int rotation, boolean updateComparator)
    {
        this.getDataWatcher().updateObject(9, Byte.valueOf((byte)(rotation % 8)));

        if (updateComparator && this.hangingPosition != null)
        {
            this.worldObj.updateComparatorOutputLevel(this.hangingPosition, Blocks.air);
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        if (this.getDisplayedItem() != null)
        {
            tagCompound.setTag("Item", this.getDisplayedItem().writeToNBT(new NBTTagCompound()));
            tagCompound.setByte("ItemRotation", (byte)this.getRotation());
            tagCompound.setFloat("ItemDropChance", this.itemDropChance);
        }

        super.writeEntityToNBT(tagCompound);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        NBTTagCompound nBTTagCompound = tagCompund.getCompoundTag("Item");

        if (nBTTagCompound != null && !nBTTagCompound.hasNoTags())
        {
            this.setDisplayedItemWithUpdate(ItemStack.loadItemStackFromNBT(nBTTagCompound), false);
            this.setItemRotationWithUpdate(tagCompund.getByte("ItemRotation"), false);

            if (tagCompund.hasKey("ItemDropChance", 99))
            {
                this.itemDropChance = tagCompund.getFloat("ItemDropChance");
            }

            if (tagCompund.hasKey("Direction"))
            {
                this.setItemRotationWithUpdate(this.getRotation() * 2, false);
            }
        }

        super.readEntityFromNBT(tagCompund);
    }

    public boolean interactFirst(EntityPlayer playerIn)
    {
        if (this.getDisplayedItem() == null)
        {
            ItemStack itemStack = playerIn.getHeldItem();

            if (itemStack != null && !this.worldObj.isRemote)
            {
                this.setDisplayedItem(itemStack);

                if (!playerIn.capabilities.isCreativeMode && --itemStack.stackSize <= 0)
                {
                    playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, (ItemStack)null);
                }
            }
        }
        else if (!this.worldObj.isRemote)
        {
            this.setItemRotation(this.getRotation() + 1);
        }

        return true;
    }

    public int getComparatorSignal()
    {
        return this.getDisplayedItem() == null ? 0 : this.getRotation() % 8 + 1;
    }
}
