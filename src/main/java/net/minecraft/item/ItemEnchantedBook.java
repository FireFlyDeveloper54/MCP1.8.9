package net.minecraft.item;

import java.util.List;
import java.util.Random;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandomChestContent;

public class ItemEnchantedBook extends Item
{
    public boolean hasEffect(ItemStack stack)
    {
        return true;
    }

    public boolean isItemTool(ItemStack stack)
    {
        return false;
    }

    public EnumRarity getRarity(ItemStack stack)
    {
        return this.getEnchantments(stack).tagCount() > 0 ? EnumRarity.UNCOMMON : super.getRarity(stack);
    }

    public NBTTagList getEnchantments(ItemStack stack)
    {
        NBTTagCompound stackTag = stack.getTagCompound();
        return stackTag != null && stackTag.hasKey("StoredEnchantments", 9) ? (NBTTagList)stackTag.getTag("StoredEnchantments") : new NBTTagList();
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, playerIn, tooltip, advanced);
        NBTTagList enchantmentList = this.getEnchantments(stack);

        if (enchantmentList != null)
        {
            for (int enchantmentIndex = 0; enchantmentIndex < enchantmentList.tagCount(); ++enchantmentIndex)
            {
                int enchantmentId = enchantmentList.getCompoundTagAt(enchantmentIndex).getShort("id");
                int enchantmentLevel = enchantmentList.getCompoundTagAt(enchantmentIndex).getShort("lvl");

                if (Enchantment.getEnchantmentById(enchantmentId) != null)
                {
                    tooltip.add(Enchantment.getEnchantmentById(enchantmentId).getTranslatedName(enchantmentLevel));
                }
            }
        }
    }

    public void addEnchantment(ItemStack stack, EnchantmentData enchantment)
    {
        NBTTagList enchantmentList = this.getEnchantments(stack);
        boolean shouldAddEnchantment = true;

        for (int enchantmentIndex = 0; enchantmentIndex < enchantmentList.tagCount(); ++enchantmentIndex)
        {
            NBTTagCompound storedEnchantment = enchantmentList.getCompoundTagAt(enchantmentIndex);

            if (storedEnchantment.getShort("id") == enchantment.enchantmentobj.effectId)
            {
                if (storedEnchantment.getShort("lvl") < enchantment.enchantmentLevel)
                {
                    storedEnchantment.setShort("lvl", (short)enchantment.enchantmentLevel);
                }

                shouldAddEnchantment = false;
                break;
            }
        }

        if (shouldAddEnchantment)
        {
            NBTTagCompound newEnchantmentTag = new NBTTagCompound();
            newEnchantmentTag.setShort("id", (short)enchantment.enchantmentobj.effectId);
            newEnchantmentTag.setShort("lvl", (short)enchantment.enchantmentLevel);
            enchantmentList.appendTag(newEnchantmentTag);
        }

        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setTag("StoredEnchantments", enchantmentList);
    }

    public ItemStack getEnchantedItemStack(EnchantmentData data)
    {
        ItemStack itemStack = new ItemStack(this);
        this.addEnchantment(itemStack, data);
        return itemStack;
    }

    public void getAll(Enchantment enchantment, List<ItemStack> enchantedBooks)
    {
        for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); ++level)
        {
            enchantedBooks.add(this.getEnchantedItemStack(new EnchantmentData(enchantment, level)));
        }
    }

    public WeightedRandomChestContent getRandom(Random rand)
    {
        return this.getRandom(rand, 1, 1, 1);
    }

    public WeightedRandomChestContent getRandom(Random rand, int minChance, int maxChance, int weight)
    {
        ItemStack itemStack = new ItemStack(Items.book, 1, 0);
        EnchantmentHelper.addRandomEnchantment(rand, itemStack, 30);
        return new WeightedRandomChestContent(itemStack, minChance, maxChance, weight);
    }
}
