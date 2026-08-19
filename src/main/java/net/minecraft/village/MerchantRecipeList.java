package net.minecraft.village;

import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;

public class MerchantRecipeList extends ArrayList<MerchantRecipe>
{
    public MerchantRecipeList()
    {
    }

    public MerchantRecipeList(NBTTagCompound compound)
    {
        this.readRecipiesFromTags(compound);
    }

    public MerchantRecipe canRecipeBeUsed(ItemStack firstBuyStack, ItemStack secondBuyStack, int recipeIndex)
    {
        if (recipeIndex > 0 && recipeIndex < this.size())
        {
            MerchantRecipe merchantrecipe1 = (MerchantRecipe)this.get(recipeIndex);
            return !this.matchesBuyItem(firstBuyStack, merchantrecipe1.getItemToBuy()) || (secondBuyStack != null || merchantrecipe1.hasSecondItemToBuy()) && (!merchantrecipe1.hasSecondItemToBuy() || !this.matchesBuyItem(secondBuyStack, merchantrecipe1.getSecondItemToBuy())) || firstBuyStack.stackSize < merchantrecipe1.getItemToBuy().stackSize || merchantrecipe1.hasSecondItemToBuy() && secondBuyStack.stackSize < merchantrecipe1.getSecondItemToBuy().stackSize ? null : merchantrecipe1;
        }
        else
        {
            for (int i = 0; i < this.size(); ++i)
            {
                MerchantRecipe merchantrecipe = (MerchantRecipe)this.get(i);

                if (this.matchesBuyItem(firstBuyStack, merchantrecipe.getItemToBuy()) && firstBuyStack.stackSize >= merchantrecipe.getItemToBuy().stackSize && (!merchantrecipe.hasSecondItemToBuy() && secondBuyStack == null || merchantrecipe.hasSecondItemToBuy() && this.matchesBuyItem(secondBuyStack, merchantrecipe.getSecondItemToBuy()) && secondBuyStack.stackSize >= merchantrecipe.getSecondItemToBuy().stackSize))
                {
                    return merchantrecipe;
                }
            }

            return null;
        }
    }

    private boolean matchesBuyItem(ItemStack offeredStack, ItemStack requiredStack)
    {
        return ItemStack.areItemsEqual(offeredStack, requiredStack) && (!requiredStack.hasTagCompound() || offeredStack.hasTagCompound() && NBTUtil.compareTags(requiredStack.getTagCompound(), offeredStack.getTagCompound(), false));
    }

    public void writeToBuf(PacketBuffer buffer)
    {
        buffer.writeByte((byte)(this.size() & 255));

        for (int i = 0; i < this.size(); ++i)
        {
            MerchantRecipe merchantRecipe = (MerchantRecipe)this.get(i);
            buffer.writeItemStackToBuffer(merchantRecipe.getItemToBuy());
            buffer.writeItemStackToBuffer(merchantRecipe.getItemToSell());
            ItemStack itemStack = merchantRecipe.getSecondItemToBuy();
            buffer.writeBoolean(itemStack != null);

            if (itemStack != null)
            {
                buffer.writeItemStackToBuffer(itemStack);
            }

            buffer.writeBoolean(merchantRecipe.isRecipeDisabled());
            buffer.writeInt(merchantRecipe.getToolUses());
            buffer.writeInt(merchantRecipe.getMaxTradeUses());
        }
    }

    public static MerchantRecipeList readFromBuf(PacketBuffer buffer) throws IOException
    {
        MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
        int i = buffer.readByte() & 255;

        for (int j = 0; j < i; ++j)
        {
            ItemStack itemstack = buffer.readItemStackFromBuffer();
            ItemStack itemstack1 = buffer.readItemStackFromBuffer();
            ItemStack itemstack2 = null;

            if (buffer.readBoolean())
            {
                itemstack2 = buffer.readItemStackFromBuffer();
            }

            boolean flag = buffer.readBoolean();
            int k = buffer.readInt();
            int l = buffer.readInt();
            MerchantRecipe merchantrecipe = new MerchantRecipe(itemstack, itemstack2, itemstack1, k, l);

            if (flag)
            {
                merchantrecipe.compensateToolUses();
            }

            merchantrecipelist.add(merchantrecipe);
        }

        return merchantrecipelist;
    }

    public void readRecipiesFromTags(NBTTagCompound compound)
    {
        NBTTagList nBTTagList = compound.getTagList("Recipes", 10);

        for (int i = 0; i < nBTTagList.tagCount(); ++i)
        {
            NBTTagCompound nBTTagCompound = nBTTagList.getCompoundTagAt(i);
            this.add(new MerchantRecipe(nBTTagCompound));
        }
    }

    public NBTTagCompound getRecipiesAsTags()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        NBTTagList nBTTagList = new NBTTagList();

        for (int i = 0; i < this.size(); ++i)
        {
            MerchantRecipe merchantRecipe = (MerchantRecipe)this.get(i);
            nBTTagList.appendTag(merchantRecipe.writeToTags());
        }

        nBTTagCompound.setTag("Recipes", nBTTagList);
        return nBTTagCompound;
    }
}
