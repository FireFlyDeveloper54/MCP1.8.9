package net.minecraft.item.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class RecipesMapExtending extends ShapedRecipes
{
    public RecipesMapExtending()
    {
        super(3, 3, new ItemStack[] {new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.filled_map, 0, 32767), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper)}, new ItemStack(Items.map, 0, 0));
    }

    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        if (!super.matches(inv, worldIn))
        {
            return false;
        }
        else
        {
            ItemStack itemStack = null;

            for (int slotIndex = 0; slotIndex < inv.getSizeInventory() && itemStack == null; ++slotIndex)
            {
                ItemStack slotStack = inv.getStackInSlot(slotIndex);

                if (slotStack != null && slotStack.getItem() == Items.filled_map)
                {
                    itemStack = slotStack;
                }
            }

            if (itemStack == null)
            {
                return false;
            }
            else
            {
                MapData mapData = Items.filled_map.getMapData(itemStack, worldIn);
                return mapData == null ? false : mapData.scale < 4;
            }
        }
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack itemStack = null;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory() && itemStack == null; ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null && slotStack.getItem() == Items.filled_map)
            {
                itemStack = slotStack;
            }
        }

        itemStack = itemStack.copy();
        itemStack.stackSize = 1;

        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setBoolean("map_is_scaling", true);
        return itemStack;
    }
}
