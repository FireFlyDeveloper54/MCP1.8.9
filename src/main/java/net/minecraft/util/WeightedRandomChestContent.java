package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;

public class WeightedRandomChestContent extends WeightedRandom.Item
{
    private ItemStack itemStack;
    private int minStackSize;
    private int maxStackSize;

    public WeightedRandomChestContent(Item item, int metadata, int minimumChance, int maximumChance, int itemWeightIn)
    {
        super(itemWeightIn);
        this.itemStack = new ItemStack(item, 1, metadata);
        this.minStackSize = minimumChance;
        this.maxStackSize = maximumChance;
    }

    public WeightedRandomChestContent(ItemStack stack, int minimumChance, int maximumChance, int itemWeightIn)
    {
        super(itemWeightIn);
        this.itemStack = stack;
        this.minStackSize = minimumChance;
        this.maxStackSize = maximumChance;
    }

    public static void generateChestContents(Random random, List<WeightedRandomChestContent> listIn, IInventory inv, int max)
    {
        for (int rollIndex = 0; rollIndex < max; ++rollIndex)
        {
            WeightedRandomChestContent weightedRandomChestContent = (WeightedRandomChestContent)WeightedRandom.getRandomItem(random, listIn);
            int stackSize = weightedRandomChestContent.minStackSize + random.nextInt(weightedRandomChestContent.maxStackSize - weightedRandomChestContent.minStackSize + 1);

            if (weightedRandomChestContent.itemStack.getMaxStackSize() >= stackSize)
            {
                ItemStack itemstack1 = weightedRandomChestContent.itemStack.copy();
                itemstack1.stackSize = stackSize;
                inv.setInventorySlotContents(random.nextInt(inv.getSizeInventory()), itemstack1);
            }
            else
            {
                for (int itemIndex = 0; itemIndex < stackSize; ++itemIndex)
                {
                    ItemStack itemstack = weightedRandomChestContent.itemStack.copy();
                    itemstack.stackSize = 1;
                    inv.setInventorySlotContents(random.nextInt(inv.getSizeInventory()), itemstack);
                }
            }
        }
    }

    public static void generateDispenserContents(Random random, List<WeightedRandomChestContent> listIn, TileEntityDispenser dispenser, int max)
    {
        for (int rollIndex = 0; rollIndex < max; ++rollIndex)
        {
            WeightedRandomChestContent weightedRandomChestContent = (WeightedRandomChestContent)WeightedRandom.getRandomItem(random, listIn);
            int stackSize = weightedRandomChestContent.minStackSize + random.nextInt(weightedRandomChestContent.maxStackSize - weightedRandomChestContent.minStackSize + 1);

            if (weightedRandomChestContent.itemStack.getMaxStackSize() >= stackSize)
            {
                ItemStack itemstack1 = weightedRandomChestContent.itemStack.copy();
                itemstack1.stackSize = stackSize;
                dispenser.setInventorySlotContents(random.nextInt(dispenser.getSizeInventory()), itemstack1);
            }
            else
            {
                for (int itemIndex = 0; itemIndex < stackSize; ++itemIndex)
                {
                    ItemStack itemstack = weightedRandomChestContent.itemStack.copy();
                    itemstack.stackSize = 1;
                    dispenser.setInventorySlotContents(random.nextInt(dispenser.getSizeInventory()), itemstack);
                }
            }
        }
    }

    public static List<WeightedRandomChestContent> addRandomChestContents(List<WeightedRandomChestContent> baseContents, WeightedRandomChestContent... extraContents)
    {
        List<WeightedRandomChestContent> list = Lists.newArrayList(baseContents);
        Collections.addAll(list, extraContents);
        return list;
    }
}
