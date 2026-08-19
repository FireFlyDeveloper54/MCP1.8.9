package net.minecraft.item.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipeBookCloning implements IRecipe
{
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        int blankBookCount = 0;
        ItemStack writtenBookStack = null;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                if (slotStack.getItem() == Items.written_book)
                {
                    if (writtenBookStack != null)
                    {
                        return false;
                    }

                    writtenBookStack = slotStack;
                }
                else
                {
                    if (slotStack.getItem() != Items.writable_book)
                    {
                        return false;
                    }

                    ++blankBookCount;
                }
            }
        }

        return writtenBookStack != null && blankBookCount > 0;
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        int blankBookCount = 0;
        ItemStack writtenBookStack = null;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                if (slotStack.getItem() == Items.written_book)
                {
                    if (writtenBookStack != null)
                    {
                        return null;
                    }

                    writtenBookStack = slotStack;
                }
                else
                {
                    if (slotStack.getItem() != Items.writable_book)
                    {
                        return null;
                    }

                    ++blankBookCount;
                }
            }
        }

        if (writtenBookStack != null && blankBookCount >= 1 && ItemEditableBook.getGeneration(writtenBookStack) < 2)
        {
            ItemStack clonedBookStack = new ItemStack(Items.written_book, blankBookCount);
            clonedBookStack.setTagCompound((NBTTagCompound)writtenBookStack.getTagCompound().copy());
            clonedBookStack.getTagCompound().setInteger("generation", ItemEditableBook.getGeneration(writtenBookStack) + 1);

            if (writtenBookStack.hasDisplayName())
            {
                clonedBookStack.setStackDisplayName(writtenBookStack.getDisplayName());
            }

            return clonedBookStack;
        }
        else
        {
            return null;
        }
    }

    public int getRecipeSize()
    {
        return 9;
    }

    public ItemStack getRecipeOutput()
    {
        return null;
    }

    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];

        for (int slotIndex = 0; slotIndex < remainingItems.length; ++slotIndex)
        {
            ItemStack itemStack = inv.getStackInSlot(slotIndex);

            if (itemStack != null && itemStack.getItem() instanceof ItemEditableBook)
            {
                remainingItems[slotIndex] = itemStack;
                break;
            }
        }

        return remainingItems;
    }
}
