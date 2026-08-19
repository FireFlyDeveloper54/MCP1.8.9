package net.minecraft.village;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MerchantRecipe
{
    private ItemStack itemToBuy;
    private ItemStack secondItemToBuy;
    private ItemStack itemToSell;
    private int toolUses;
    private int maxTradeUses;
    private boolean rewardsExp;

    public MerchantRecipe(NBTTagCompound tagCompound)
    {
        this.readFromTags(tagCompound);
    }

    public MerchantRecipe(ItemStack secondItemStack, ItemStack sixthItemStack, ItemStack sell)
    {
        this(secondItemStack, sixthItemStack, sell, 0, 7);
    }

    public MerchantRecipe(ItemStack thirdItemStack, ItemStack fifthItemStack, ItemStack sell, int toolUsesIn, int maxTradeUsesIn)
    {
        this.itemToBuy = thirdItemStack;
        this.secondItemToBuy = fifthItemStack;
        this.itemToSell = sell;
        this.toolUses = toolUsesIn;
        this.maxTradeUses = maxTradeUsesIn;
        this.rewardsExp = true;
    }

    public MerchantRecipe(ItemStack fourthItemStack, ItemStack sell)
    {
        this(fourthItemStack, (ItemStack)null, sell);
    }

    public MerchantRecipe(ItemStack itemStack, Item sellItem)
    {
        this(itemStack, new ItemStack(sellItem));
    }

    public ItemStack getItemToBuy()
    {
        return this.itemToBuy;
    }

    public ItemStack getSecondItemToBuy()
    {
        return this.secondItemToBuy;
    }

    public boolean hasSecondItemToBuy()
    {
        return this.secondItemToBuy != null;
    }

    public ItemStack getItemToSell()
    {
        return this.itemToSell;
    }

    public int getToolUses()
    {
        return this.toolUses;
    }

    public int getMaxTradeUses()
    {
        return this.maxTradeUses;
    }

    public void incrementToolUses()
    {
        ++this.toolUses;
    }

    public void increaseMaxTradeUses(int increment)
    {
        this.maxTradeUses += increment;
    }

    public boolean isRecipeDisabled()
    {
        return this.toolUses >= this.maxTradeUses;
    }

    public void compensateToolUses()
    {
        this.toolUses = this.maxTradeUses;
    }

    public boolean getRewardsExp()
    {
        return this.rewardsExp;
    }

    public void readFromTags(NBTTagCompound tagCompound)
    {
        NBTTagCompound nBTTagCompound = tagCompound.getCompoundTag("buy");
        this.itemToBuy = ItemStack.loadItemStackFromNBT(nBTTagCompound);
        NBTTagCompound nbttagcompound1 = tagCompound.getCompoundTag("sell");
        this.itemToSell = ItemStack.loadItemStackFromNBT(nbttagcompound1);

        if (tagCompound.hasKey("buyB", 10))
        {
            this.secondItemToBuy = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("buyB"));
        }

        if (tagCompound.hasKey("uses", 99))
        {
            this.toolUses = tagCompound.getInteger("uses");
        }

        if (tagCompound.hasKey("maxUses", 99))
        {
            this.maxTradeUses = tagCompound.getInteger("maxUses");
        }
        else
        {
            this.maxTradeUses = 7;
        }

        if (tagCompound.hasKey("rewardExp", 1))
        {
            this.rewardsExp = tagCompound.getBoolean("rewardExp");
        }
        else
        {
            this.rewardsExp = true;
        }
    }

    public NBTTagCompound writeToTags()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        nBTTagCompound.setTag("buy", this.itemToBuy.writeToNBT(new NBTTagCompound()));
        nBTTagCompound.setTag("sell", this.itemToSell.writeToNBT(new NBTTagCompound()));

        if (this.secondItemToBuy != null)
        {
            nBTTagCompound.setTag("buyB", this.secondItemToBuy.writeToNBT(new NBTTagCompound()));
        }

        nBTTagCompound.setInteger("uses", this.toolUses);
        nBTTagCompound.setInteger("maxUses", this.maxTradeUses);
        nBTTagCompound.setBoolean("rewardExp", this.rewardsExp);
        return nBTTagCompound;
    }
}
