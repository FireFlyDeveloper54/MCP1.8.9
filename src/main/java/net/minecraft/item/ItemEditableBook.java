package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentProcessor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class ItemEditableBook extends Item
{
    public ItemEditableBook()
    {
        this.setMaxStackSize(1);
    }

    public static boolean validBookTagContents(NBTTagCompound nbt)
    {
        if (!ItemWritableBook.isNBTValid(nbt))
        {
            return false;
        }
        else if (!nbt.hasKey("title", 8))
        {
            return false;
        }
        else
        {
            String title = nbt.getString("title");
            return title != null && title.length() <= 32 ? nbt.hasKey("author", 8) : false;
        }
    }

    public static int getGeneration(ItemStack book)
    {
        return book.getTagCompound().getInteger("generation");
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        if (stack.hasTagCompound())
        {
            NBTTagCompound bookTag = stack.getTagCompound();
            String title = bookTag.getString("title");

            if (!StringUtils.isNullOrEmpty(title))
            {
                return title;
            }
        }

        return super.getItemStackDisplayName(stack);
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if (stack.hasTagCompound())
        {
            NBTTagCompound bookTag = stack.getTagCompound();
            String author = bookTag.getString("author");

            if (!StringUtils.isNullOrEmpty(author))
            {
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("book.byAuthor", new Object[] {author}));
            }

            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("book.generation." + bookTag.getInteger("generation")));
        }
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        if (!worldIn.isRemote)
        {
            this.resolveContents(itemStackIn, playerIn);
        }

        playerIn.displayGUIBook(itemStackIn);
        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
        return itemStackIn;
    }

    private void resolveContents(ItemStack stack, EntityPlayer player)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            NBTTagCompound bookTag = stack.getTagCompound();

            if (!bookTag.getBoolean("resolved"))
            {
                bookTag.setBoolean("resolved", true);

                if (validBookTagContents(bookTag))
                {
                    NBTTagList pageList = bookTag.getTagList("pages", 8);

                    for (int pageIndex = 0; pageIndex < pageList.tagCount(); ++pageIndex)
                    {
                        String pageJson = pageList.getStringTagAt(pageIndex);
                        IChatComponent chatComponent;

                        try
                        {
                            chatComponent = IChatComponent.Serializer.jsonToComponent(pageJson);
                            chatComponent = ChatComponentProcessor.processComponent(player, chatComponent, player);
                        }
                        catch (Exception caughtException)
                        {
                            chatComponent = new ChatComponentText(pageJson);
                        }

                        pageList.set(pageIndex, new NBTTagString(IChatComponent.Serializer.componentToJson(chatComponent)));
                    }

                    bookTag.setTag("pages", pageList);

                    if (player instanceof EntityPlayerMP && player.getCurrentEquippedItem() == stack)
                    {
                        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
                        ((EntityPlayerMP)player).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(0, slot.slotNumber, stack));
                    }
                }
            }
        }
    }

    public boolean hasEffect(ItemStack stack)
    {
        return true;
    }
}
