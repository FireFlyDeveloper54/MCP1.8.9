package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemLead extends Item
{
    public ItemLead()
    {
        this.setCreativeTab(CreativeTabs.tabTools);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block instanceof BlockFence)
        {
            if (worldIn.isRemote)
            {
                return true;
            }
            else
            {
                attachToFence(playerIn, worldIn, pos);
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public static boolean attachToFence(EntityPlayer player, World worldIn, BlockPos fence)
    {
        EntityLeashKnot entityLeashKnot = EntityLeashKnot.getKnotForPosition(worldIn, fence);
        boolean attachedAny = false;
        double leashSearchRange = 7.0D;
        int fenceX = fence.getX();
        int fenceY = fence.getY();
        int fenceZ = fence.getZ();

        for (EntityLiving entityLiving : worldIn.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB((double)fenceX - leashSearchRange, (double)fenceY - leashSearchRange, (double)fenceZ - leashSearchRange, (double)fenceX + leashSearchRange, (double)fenceY + leashSearchRange, (double)fenceZ + leashSearchRange)))
        {
            if (entityLiving.getLeashed() && entityLiving.getLeashedToEntity() == player)
            {
                if (entityLeashKnot == null)
                {
                    entityLeashKnot = EntityLeashKnot.createKnot(worldIn, fence);
                }

                entityLiving.setLeashedToEntity(entityLeashKnot, true);
                attachedAny = true;
            }
        }

        return attachedAny;
    }
}
