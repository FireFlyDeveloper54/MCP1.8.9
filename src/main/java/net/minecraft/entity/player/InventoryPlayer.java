package net.minecraft.entity.player;

import java.util.Arrays;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;

public class InventoryPlayer implements IInventory
{
    public ItemStack[] mainInventory = new ItemStack[36];
    public ItemStack[] armorInventory = new ItemStack[4];
    public int currentItem;
    public EntityPlayer player;
    private ItemStack itemStack;
    public boolean inventoryChanged;

    public InventoryPlayer(EntityPlayer playerIn)
    {
        this.player = playerIn;
    }

    public ItemStack getCurrentItem()
    {
        return this.currentItem < 9 && this.currentItem >= 0 ? this.mainInventory[this.currentItem] : null;
    }

    public static int getHotbarSize()
    {
        return 9;
    }

    private int getInventorySlotContainItem(Item itemIn)
    {
        for (int slotIndex = 0; slotIndex < this.mainInventory.length; ++slotIndex)
        {
            if (this.mainInventory[slotIndex] != null && this.mainInventory[slotIndex].getItem() == itemIn)
            {
                return slotIndex;
            }
        }

        return -1;
    }

    private int getInventorySlotContainItemAndDamage(Item itemIn, int metadataIn)
    {
        for (int slotIndex = 0; slotIndex < this.mainInventory.length; ++slotIndex)
        {
            if (this.mainInventory[slotIndex] != null && this.mainInventory[slotIndex].getItem() == itemIn && this.mainInventory[slotIndex].getMetadata() == metadataIn)
            {
                return slotIndex;
            }
        }

        return -1;
    }

    private int storeItemStack(ItemStack itemStackIn)
    {
        for (int slotIndex = 0; slotIndex < this.mainInventory.length; ++slotIndex)
        {
            if (this.mainInventory[slotIndex] != null && this.mainInventory[slotIndex].getItem() == itemStackIn.getItem() && this.mainInventory[slotIndex].isStackable() && this.mainInventory[slotIndex].stackSize < this.mainInventory[slotIndex].getMaxStackSize() && this.mainInventory[slotIndex].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[slotIndex].getHasSubtypes() || this.mainInventory[slotIndex].getMetadata() == itemStackIn.getMetadata()) && ItemStack.areItemStackTagsEqual(this.mainInventory[slotIndex], itemStackIn))
            {
                return slotIndex;
            }
        }

        return -1;
    }

    public int getFirstEmptyStack()
    {
        for (int slotIndex = 0; slotIndex < this.mainInventory.length; ++slotIndex)
        {
            if (this.mainInventory[slotIndex] == null)
            {
                return slotIndex;
            }
        }

        return -1;
    }

    public void setCurrentItem(Item itemIn, int metadataIn, boolean isMetaSpecific, boolean createIfMissing)
    {
        ItemStack itemStack = this.getCurrentItem();
        int matchingSlot = isMetaSpecific ? this.getInventorySlotContainItemAndDamage(itemIn, metadataIn) : this.getInventorySlotContainItem(itemIn);

        if (matchingSlot >= 0 && matchingSlot < 9)
        {
            this.currentItem = matchingSlot;
        }
        else if (createIfMissing && itemIn != null)
        {
            int emptySlot = this.getFirstEmptyStack();

            if (emptySlot >= 0 && emptySlot < 9)
            {
                this.currentItem = emptySlot;
            }

            if (itemStack == null || !itemStack.isItemEnchantable() || this.getInventorySlotContainItemAndDamage(itemStack.getItem(), itemStack.getItemDamage()) != this.currentItem)
            {
                int existingSlot = this.getInventorySlotContainItemAndDamage(itemIn, metadataIn);
                int stackSize;

                if (existingSlot >= 0)
                {
                    stackSize = this.mainInventory[existingSlot].stackSize;
                    this.mainInventory[existingSlot] = this.mainInventory[this.currentItem];
                }
                else
                {
                    stackSize = 1;
                }

                this.mainInventory[this.currentItem] = new ItemStack(itemIn, stackSize, metadataIn);
            }
        }
    }

    public void changeCurrentItem(int direction)
    {
        if (direction > 0)
        {
            direction = 1;
        }

        if (direction < 0)
        {
            direction = -1;
        }

        for (this.currentItem -= direction; this.currentItem < 0; this.currentItem += 9)
        {
            ;
        }

        while (this.currentItem >= 9)
        {
            this.currentItem -= 9;
        }
    }

    public int clearMatchingItems(Item itemIn, int metadataIn, int removeCount, NBTTagCompound itemNBT)
    {
        int removedCount = 0;

        for (int mainSlot = 0; mainSlot < this.mainInventory.length; ++mainSlot)
        {
            ItemStack mainStack = this.mainInventory[mainSlot];

            if (mainStack != null && (itemIn == null || mainStack.getItem() == itemIn) && (metadataIn <= -1 || mainStack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.compareTags(itemNBT, mainStack.getTagCompound(), true)))
            {
                int removedFromMainStack = removeCount <= 0 ? mainStack.stackSize : Math.min(removeCount - removedCount, mainStack.stackSize);
                removedCount += removedFromMainStack;

                if (removeCount != 0)
                {
                    this.mainInventory[mainSlot].stackSize -= removedFromMainStack;

                    if (this.mainInventory[mainSlot].stackSize == 0)
                    {
                        this.mainInventory[mainSlot] = null;
                    }

                    if (removeCount > 0 && removedCount >= removeCount)
                    {
                        return removedCount;
                    }
                }
            }
        }

        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            ItemStack armorStack = this.armorInventory[armorSlot];

            if (armorStack != null && (itemIn == null || armorStack.getItem() == itemIn) && (metadataIn <= -1 || armorStack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.compareTags(itemNBT, armorStack.getTagCompound(), false)))
            {
                int removedFromArmorStack = removeCount <= 0 ? armorStack.stackSize : Math.min(removeCount - removedCount, armorStack.stackSize);
                removedCount += removedFromArmorStack;

                if (removeCount != 0)
                {
                    this.armorInventory[armorSlot].stackSize -= removedFromArmorStack;

                    if (this.armorInventory[armorSlot].stackSize == 0)
                    {
                        this.armorInventory[armorSlot] = null;
                    }

                    if (removeCount > 0 && removedCount >= removeCount)
                    {
                        return removedCount;
                    }
                }
            }
        }

        if (this.itemStack != null)
        {
            if (itemIn != null && this.itemStack.getItem() != itemIn)
            {
                return removedCount;
            }

            if (metadataIn > -1 && this.itemStack.getMetadata() != metadataIn)
            {
                return removedCount;
            }

            if (itemNBT != null && !NBTUtil.compareTags(itemNBT, this.itemStack.getTagCompound(), false))
            {
                return removedCount;
            }

            int removedFromHeldStack = removeCount <= 0 ? this.itemStack.stackSize : Math.min(removeCount - removedCount, this.itemStack.stackSize);
            removedCount += removedFromHeldStack;

            if (removeCount != 0)
            {
                this.itemStack.stackSize -= removedFromHeldStack;

                if (this.itemStack.stackSize == 0)
                {
                    this.itemStack = null;
                }

                if (removeCount > 0 && removedCount >= removeCount)
                {
                    return removedCount;
                }
            }
        }

        return removedCount;
    }

    private int storePartialItemStack(ItemStack itemStackIn)
    {
        Item item = itemStackIn.getItem();
        int remainingCount = itemStackIn.stackSize;
        int targetSlot = this.storeItemStack(itemStackIn);

        if (targetSlot < 0)
        {
            targetSlot = this.getFirstEmptyStack();
        }

        if (targetSlot < 0)
        {
            return remainingCount;
        }
        else
        {
            if (this.mainInventory[targetSlot] == null)
            {
                this.mainInventory[targetSlot] = new ItemStack(item, 0, itemStackIn.getMetadata());

                if (itemStackIn.hasTagCompound())
                {
                    this.mainInventory[targetSlot].setTagCompound((NBTTagCompound)itemStackIn.getTagCompound().copy());
                }
            }

            int transferCount = remainingCount;

            if (remainingCount > this.mainInventory[targetSlot].getMaxStackSize() - this.mainInventory[targetSlot].stackSize)
            {
                transferCount = this.mainInventory[targetSlot].getMaxStackSize() - this.mainInventory[targetSlot].stackSize;
            }

            if (transferCount > this.getInventoryStackLimit() - this.mainInventory[targetSlot].stackSize)
            {
                transferCount = this.getInventoryStackLimit() - this.mainInventory[targetSlot].stackSize;
            }

            if (transferCount == 0)
            {
                return remainingCount;
            }
            else
            {
                remainingCount = remainingCount - transferCount;
                this.mainInventory[targetSlot].stackSize += transferCount;
                this.mainInventory[targetSlot].animationsToGo = 5;
                return remainingCount;
            }
        }
    }

    public void decrementAnimations()
    {
        for (int slotIndex = 0; slotIndex < this.mainInventory.length; ++slotIndex)
        {
            if (this.mainInventory[slotIndex] != null)
            {
                this.mainInventory[slotIndex].updateAnimation(this.player.worldObj, this.player, slotIndex, this.currentItem == slotIndex);
            }
        }
    }

    public boolean consumeInventoryItem(Item itemIn)
    {
        int slotIndex = this.getInventorySlotContainItem(itemIn);

        if (slotIndex < 0)
        {
            return false;
        }
        else
        {
            if (--this.mainInventory[slotIndex].stackSize <= 0)
            {
                this.mainInventory[slotIndex] = null;
            }

            return true;
        }
    }

    public boolean hasItem(Item itemIn)
    {
        int slotIndex = this.getInventorySlotContainItem(itemIn);
        return slotIndex >= 0;
    }

    public boolean addItemStackToInventory(final ItemStack itemStackIn)
    {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null)
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    int emptySlot = this.getFirstEmptyStack();

                    if (emptySlot >= 0)
                    {
                        this.mainInventory[emptySlot] = ItemStack.copyItemStack(itemStackIn);
                        this.mainInventory[emptySlot].animationsToGo = 5;
                        itemStackIn.stackSize = 0;
                        return true;
                    }
                    else if (this.player.capabilities.isCreativeMode)
                    {
                        itemStackIn.stackSize = 0;
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    int previousStackSize;

                    while (true)
                    {
                        previousStackSize = itemStackIn.stackSize;
                        itemStackIn.stackSize = this.storePartialItemStack(itemStackIn);

                        if (itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= previousStackSize)
                        {
                            break;
                        }
                    }

                    if (itemStackIn.stackSize == previousStackSize && this.player.capabilities.isCreativeMode)
                    {
                        itemStackIn.stackSize = 0;
                        return true;
                    }
                    else
                    {
                        return itemStackIn.stackSize < previousStackSize;
                    }
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Item being added");
                crashReportCategory.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
                crashReportCategory.addCrashSection("Item data", Integer.valueOf(itemStackIn.getMetadata()));
                crashReportCategory.addCrashSectionCallable("Item name", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return itemStackIn.getDisplayName();
                    }
                });
                throw new ReportedException(crashReport);
            }
        }
        else
        {
            return false;
        }
    }

    public ItemStack decrStackSize(int index, int count)
    {
        ItemStack[] targetInventory = this.mainInventory;

        if (index >= this.mainInventory.length)
        {
            targetInventory = this.armorInventory;
            index -= this.mainInventory.length;
        }

        if (targetInventory[index] != null)
        {
            if (targetInventory[index].stackSize <= count)
            {
                ItemStack removedStack = targetInventory[index];
                targetInventory[index] = null;
                return removedStack;
            }
            else
            {
                ItemStack splitStack = targetInventory[index].splitStack(count);

                if (targetInventory[index].stackSize == 0)
                {
                    targetInventory[index] = null;
                }

                return splitStack;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack[] targetInventory = this.mainInventory;

        if (index >= this.mainInventory.length)
        {
            targetInventory = this.armorInventory;
            index -= this.mainInventory.length;
        }

        if (targetInventory[index] != null)
        {
            ItemStack itemStack = targetInventory[index];
            targetInventory[index] = null;
            return itemStack;
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack)
    {
        ItemStack[] targetInventory = this.mainInventory;

        if (index >= targetInventory.length)
        {
            index -= targetInventory.length;
            targetInventory = this.armorInventory;
        }

        targetInventory[index] = stack;
    }

    public float getStrVsBlock(Block blockIn)
    {
        float strength = 1.0F;

        if (this.mainInventory[this.currentItem] != null)
        {
            strength *= this.mainInventory[this.currentItem].getStrVsBlock(blockIn);
        }

        return strength;
    }

    public NBTTagList writeToNBT(NBTTagList nbtTagListIn)
    {
        for (int mainSlot = 0; mainSlot < this.mainInventory.length; ++mainSlot)
        {
            if (this.mainInventory[mainSlot] != null)
            {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("Slot", (byte)mainSlot);
                this.mainInventory[mainSlot].writeToNBT(slotTag);
                nbtTagListIn.appendTag(slotTag);
            }
        }

        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            if (this.armorInventory[armorSlot] != null)
            {
                NBTTagCompound armorSlotTag = new NBTTagCompound();
                armorSlotTag.setByte("Slot", (byte)(armorSlot + 100));
                this.armorInventory[armorSlot].writeToNBT(armorSlotTag);
                nbtTagListIn.appendTag(armorSlotTag);
            }
        }

        return nbtTagListIn;
    }

    public void readFromNBT(NBTTagList nbtTagListIn)
    {
        this.mainInventory = new ItemStack[36];
        this.armorInventory = new ItemStack[4];

        for (int tagIndex = 0; tagIndex < nbtTagListIn.tagCount(); ++tagIndex)
        {
            NBTTagCompound slotTag = nbtTagListIn.getCompoundTagAt(tagIndex);
            int slotId = slotTag.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(slotTag);

            if (itemStack != null)
            {
                if (slotId >= 0 && slotId < this.mainInventory.length)
                {
                    this.mainInventory[slotId] = itemStack;
                }

                if (slotId >= 100 && slotId < this.armorInventory.length + 100)
                {
                    this.armorInventory[slotId - 100] = itemStack;
                }
            }
        }
    }

    public int getSizeInventory()
    {
        return this.mainInventory.length + 4;
    }

    public ItemStack getStackInSlot(int index)
    {
        ItemStack[] targetInventory = this.mainInventory;

        if (index >= targetInventory.length)
        {
            index -= targetInventory.length;
            targetInventory = this.armorInventory;
        }

        return targetInventory[index];
    }

    public String getName()
    {
        return "container.inventory";
    }

    public boolean hasCustomName()
    {
        return false;
    }

    public IChatComponent getDisplayName()
    {
        return (IChatComponent)(this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName(), new Object[0]));
    }

    public int getInventoryStackLimit()
    {
        return 64;
    }

    public boolean canHeldItemHarvest(Block blockIn)
    {
        if (blockIn.getMaterial().isToolNotRequired())
        {
            return true;
        }
        else
        {
            ItemStack itemStack = this.getStackInSlot(this.currentItem);
            return itemStack != null ? itemStack.canHarvestBlock(blockIn) : false;
        }
    }

    public ItemStack armorItemInSlot(int slotIn)
    {
        return this.armorInventory[slotIn];
    }

    public int getTotalArmorValue()
    {
        int totalArmor = 0;

        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            if (this.armorInventory[armorSlot] != null && this.armorInventory[armorSlot].getItem() instanceof ItemArmor)
            {
                int armorPoints = ((ItemArmor)this.armorInventory[armorSlot].getItem()).damageReduceAmount;
                totalArmor += armorPoints;
            }
        }

        return totalArmor;
    }

    public void damageArmor(float damage)
    {
        damage = damage / 4.0F;

        if (damage < 1.0F)
        {
            damage = 1.0F;
        }

        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            if (this.armorInventory[armorSlot] != null && this.armorInventory[armorSlot].getItem() instanceof ItemArmor)
            {
                this.armorInventory[armorSlot].damageItem((int)damage, this.player);

                if (this.armorInventory[armorSlot].stackSize == 0)
                {
                    this.armorInventory[armorSlot] = null;
                }
            }
        }
    }

    public void dropAllItems()
    {
        for (int mainSlot = 0; mainSlot < this.mainInventory.length; ++mainSlot)
        {
            if (this.mainInventory[mainSlot] != null)
            {
                this.player.dropItem(this.mainInventory[mainSlot], true, false);
                this.mainInventory[mainSlot] = null;
            }
        }

        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            if (this.armorInventory[armorSlot] != null)
            {
                this.player.dropItem(this.armorInventory[armorSlot], true, false);
                this.armorInventory[armorSlot] = null;
            }
        }
    }

    public void markDirty()
    {
        this.inventoryChanged = true;
    }

    public void setItemStack(ItemStack itemStackIn)
    {
        this.itemStack = itemStackIn;
    }

    public ItemStack getItemStack()
    {
        return this.itemStack;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.player.isDead ? false : player.getDistanceSqToEntity(this.player) <= 64.0D;
    }

    public boolean hasItemStack(ItemStack itemStackIn)
    {
        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            if (this.armorInventory[armorSlot] != null && this.armorInventory[armorSlot].isItemEqual(itemStackIn))
            {
                return true;
            }
        }

        for (int mainSlot = 0; mainSlot < this.mainInventory.length; ++mainSlot)
        {
            if (this.mainInventory[mainSlot] != null && this.mainInventory[mainSlot].isItemEqual(itemStackIn))
            {
                return true;
            }
        }

        return false;
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    public void copyInventory(InventoryPlayer playerInventory)
    {
        for (int mainSlot = 0; mainSlot < this.mainInventory.length; ++mainSlot)
        {
            this.mainInventory[mainSlot] = ItemStack.copyItemStack(playerInventory.mainInventory[mainSlot]);
        }

        for (int armorSlot = 0; armorSlot < this.armorInventory.length; ++armorSlot)
        {
            this.armorInventory[armorSlot] = ItemStack.copyItemStack(playerInventory.armorInventory[armorSlot]);
        }

        this.currentItem = playerInventory.currentItem;
    }

    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value)
    {
    }

    public int getFieldCount()
    {
        return 0;
    }

    public void clear()
    {
        Arrays.fill(this.mainInventory, null);
        Arrays.fill(this.armorInventory, null);
    }
}
