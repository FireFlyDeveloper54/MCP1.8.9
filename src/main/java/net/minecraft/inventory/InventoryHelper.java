package net.minecraft.inventory;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class InventoryHelper
{
    private static final Random RANDOM = new Random();

    public static void dropInventoryItems(World worldIn, BlockPos pos, IInventory inventory)
    {
        dropInventoryItems(worldIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), inventory);
    }

    public static void dropInventoryItems(World worldIn, Entity entityAt, IInventory inventory)
    {
        dropInventoryItems(worldIn, entityAt.posX, entityAt.posY, entityAt.posZ, inventory);
    }

    private static void dropInventoryItems(World worldIn, double x, double y, double z, IInventory inventory)
    {
        for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); ++slotIndex)
        {
            ItemStack itemStack = inventory.getStackInSlot(slotIndex);

            if (itemStack != null)
            {
                spawnItemStack(worldIn, x, y, z, itemStack);
            }
        }
    }

    private static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        float xOffset = RANDOM.nextFloat() * 0.8F + 0.1F;
        float yOffset = RANDOM.nextFloat() * 0.8F + 0.1F;
        float zOffset = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0)
        {
            int droppedCount = RANDOM.nextInt(21) + 10;

            if (droppedCount > stack.stackSize)
            {
                droppedCount = stack.stackSize;
            }

            stack.stackSize -= droppedCount;
            EntityItem entityItem = new EntityItem(worldIn, x + (double)xOffset, y + (double)yOffset, z + (double)zOffset, new ItemStack(stack.getItem(), droppedCount, stack.getMetadata()));

            if (stack.hasTagCompound())
            {
                entityItem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
            }

            float motionScale = 0.05F;
            entityItem.motionX = RANDOM.nextGaussian() * (double)motionScale;
            entityItem.motionY = RANDOM.nextGaussian() * (double)motionScale + 0.20000000298023224D;
            entityItem.motionZ = RANDOM.nextGaussian() * (double)motionScale;
            worldIn.spawnEntityInWorld(entityItem);
        }
    }
}
