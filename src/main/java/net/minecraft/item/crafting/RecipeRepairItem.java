package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipeRepairItem implements IRecipe
{
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        List<ItemStack> repairInputs = Lists.<ItemStack>newArrayList();

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack itemStack = inv.getStackInSlot(slotIndex);

            if (itemStack != null)
            {
                repairInputs.add(itemStack);

                if (repairInputs.size() > 1)
                {
                    ItemStack firstInput = (ItemStack)repairInputs.get(0);

                    if (itemStack.getItem() != firstInput.getItem() || firstInput.stackSize != 1 || itemStack.stackSize != 1 || !firstInput.getItem().isDamageable())
                    {
                        return false;
                    }
                }
            }
        }

        return repairInputs.size() == 2;
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        List<ItemStack> repairInputs = Lists.<ItemStack>newArrayList();

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                repairInputs.add(slotStack);

                if (repairInputs.size() > 1)
                {
                    ItemStack firstInput = (ItemStack)repairInputs.get(0);

                    if (slotStack.getItem() != firstInput.getItem() || firstInput.stackSize != 1 || slotStack.stackSize != 1 || !firstInput.getItem().isDamageable())
                    {
                        return null;
                    }
                }
            }
        }

        if (repairInputs.size() == 2)
        {
            ItemStack firstInput = (ItemStack)repairInputs.get(0);
            ItemStack secondInput = (ItemStack)repairInputs.get(1);

            if (firstInput.getItem() == secondInput.getItem() && firstInput.stackSize == 1 && secondInput.stackSize == 1 && firstInput.getItem().isDamageable())
            {
                Item item = firstInput.getItem();
                int firstRemainingDurability = item.getMaxDamage() - firstInput.getItemDamage();
                int secondRemainingDurability = item.getMaxDamage() - secondInput.getItemDamage();
                int repairedDurability = firstRemainingDurability + secondRemainingDurability + item.getMaxDamage() * 5 / 100;
                int repairedItemDamage = item.getMaxDamage() - repairedDurability;

                if (repairedItemDamage < 0)
                {
                    repairedItemDamage = 0;
                }

                return new ItemStack(firstInput.getItem(), 1, repairedItemDamage);
            }
        }

        return null;
    }

    public int getRecipeSize()
    {
        return 4;
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

            if (itemStack != null && itemStack.getItem().hasContainerItem())
            {
                remainingItems[slotIndex] = new ItemStack(itemStack.getItem().getContainerItem());
            }
        }

        return remainingItems;
    }
}
