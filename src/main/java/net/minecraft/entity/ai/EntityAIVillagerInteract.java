package net.minecraft.entity.ai;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public class EntityAIVillagerInteract extends EntityAIWatchClosest2
{
    private int interactionDelay;
    private EntityVillager villager;

    public EntityAIVillagerInteract(EntityVillager villagerIn)
    {
        super(villagerIn, EntityVillager.class, 3.0F, 0.02F);
        this.villager = villagerIn;
    }

    public void startExecuting()
    {
        super.startExecuting();

        if (this.villager.canAbondonItems() && this.closestEntity instanceof EntityVillager && ((EntityVillager)this.closestEntity).wantsMoreFood())
        {
            this.interactionDelay = 10;
        }
        else
        {
            this.interactionDelay = 0;
        }
    }

    public void updateTask()
    {
        super.updateTask();

        if (this.interactionDelay > 0)
        {
            --this.interactionDelay;

            if (this.interactionDelay == 0)
            {
                InventoryBasic inventory = this.villager.getVillagerInventory();

                for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); ++slotIndex)
                {
                    ItemStack stack = inventory.getStackInSlot(slotIndex);
                    ItemStack thrownStack = null;

                    if (stack != null)
                    {
                        Item item = stack.getItem();

                        if ((item == Items.bread || item == Items.potato || item == Items.carrot) && stack.stackSize > 3)
                        {
                            int splitStackSize = stack.stackSize / 2;
                            stack.stackSize -= splitStackSize;
                            thrownStack = new ItemStack(item, splitStackSize, stack.getMetadata());
                        }
                        else if (item == Items.wheat && stack.stackSize > 5)
                        {
                            int wheatUsed = stack.stackSize / 2 / 3 * 3;
                            int breadCount = wheatUsed / 3;
                            stack.stackSize -= wheatUsed;
                            thrownStack = new ItemStack(Items.bread, breadCount, 0);
                        }

                        if (stack.stackSize <= 0)
                        {
                            inventory.setInventorySlotContents(slotIndex, (ItemStack)null);
                        }
                    }

                    if (thrownStack != null)
                    {
                        double itemY = this.villager.posY - 0.30000001192092896D + (double)this.villager.getEyeHeight();
                        EntityItem entityItem = new EntityItem(this.villager.worldObj, this.villager.posX, itemY, this.villager.posZ, thrownStack);
                        float throwSpeed = 0.3F;
                        float yaw = this.villager.rotationYawHead;
                        float pitch = this.villager.rotationPitch;
                        entityItem.motionX = (double)(-MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * throwSpeed);
                        entityItem.motionZ = (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * throwSpeed);
                        entityItem.motionY = (double)(-MathHelper.sin(pitch / 180.0F * (float)Math.PI) * throwSpeed + 0.1F);
                        entityItem.setDefaultPickupDelay();
                        this.villager.worldObj.spawnEntityInWorld(entityItem);
                        break;
                    }
                }
            }
        }
    }
}
