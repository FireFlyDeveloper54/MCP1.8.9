package net.minecraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;

public class ItemWritableBook extends Item
{
    public ItemWritableBook()
    {
        this.setMaxStackSize(1);
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        playerIn.displayGUIBook(itemStackIn);
        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
        return itemStackIn;
    }

    public static boolean isNBTValid(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            return false;
        }
        else if (!nbt.hasKey("pages", 9))
        {
            return false;
        }
        else
        {
            NBTTagList pageList = nbt.getTagList("pages", 8);

            for (int pageIndex = 0; pageIndex < pageList.tagCount(); ++pageIndex)
            {
                String pageText = pageList.getStringTagAt(pageIndex);

                if (pageText == null)
                {
                    return false;
                }

                if (pageText.length() > 32767)
                {
                    return false;
                }
            }

            return true;
        }
    }
}
