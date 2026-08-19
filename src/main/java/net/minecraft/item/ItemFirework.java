package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemFirework extends Item
{
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            EntityFireworkRocket fireworkRocket = new EntityFireworkRocket(worldIn, (double)((float)pos.getX() + hitX), (double)((float)pos.getY() + hitY), (double)((float)pos.getZ() + hitZ), stack);
            worldIn.spawnEntityInWorld(fireworkRocket);

            if (!playerIn.capabilities.isCreativeMode)
            {
                --stack.stackSize;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if (stack.hasTagCompound())
        {
            NBTTagCompound fireworksTag = stack.getTagCompound().getCompoundTag("Fireworks");

            if (fireworksTag != null)
            {
                if (fireworksTag.hasKey("Flight", 99))
                {
                    tooltip.add(StatCollector.translateToLocal("item.fireworks.flight") + " " + fireworksTag.getByte("Flight"));
                }

                NBTTagList explosionList = fireworksTag.getTagList("Explosions", 10);

                if (explosionList != null && explosionList.tagCount() > 0)
                {
                    for (int explosionIndex = 0; explosionIndex < explosionList.tagCount(); ++explosionIndex)
                    {
                        NBTTagCompound explosionTag = explosionList.getCompoundTagAt(explosionIndex);
                        List<String> explosionTooltip = Lists.<String>newArrayList();
                        ItemFireworkCharge.addExplosionInfo(explosionTag, explosionTooltip);

                        if (explosionTooltip.size() > 0)
                        {
                            for (int tooltipLineIndex = 1; tooltipLineIndex < explosionTooltip.size(); ++tooltipLineIndex)
                            {
                                explosionTooltip.set(tooltipLineIndex, "  " + explosionTooltip.get(tooltipLineIndex));
                            }

                            tooltip.addAll(explosionTooltip);
                        }
                    }
                }
            }
        }
    }
}
