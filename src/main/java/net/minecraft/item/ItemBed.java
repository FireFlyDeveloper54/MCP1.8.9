package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemBed extends Item
{
    public ItemBed()
    {
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else if (side != EnumFacing.UP)
        {
            return false;
        }
        else
        {
            IBlockState blockState = worldIn.getBlockState(pos);
            Block block = blockState.getBlock();
            boolean isReplaceable = block.isReplaceable(worldIn, pos);

            if (!isReplaceable)
            {
                pos = pos.up();
            }

            int directionIndex = MathHelper.floor_double((double)(playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing bedFacing = EnumFacing.getHorizontal(directionIndex);
            BlockPos headPos = pos.offset(bedFacing);

            if (playerIn.canPlayerEdit(pos, side, stack) && playerIn.canPlayerEdit(headPos, side, stack))
            {
                boolean isHeadReplaceable = worldIn.getBlockState(headPos).getBlock().isReplaceable(worldIn, headPos);
                boolean canPlaceFoot = isReplaceable || worldIn.isAirBlock(pos);
                boolean canPlaceHead = isHeadReplaceable || worldIn.isAirBlock(headPos);

                if (canPlaceFoot && canPlaceHead && World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && World.doesBlockHaveSolidTopSurface(worldIn, headPos.down()))
                {
                    IBlockState footState = Blocks.bed.getDefaultState().withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)).withProperty(BlockBed.FACING, bedFacing).withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT);

                    if (worldIn.setBlockState(pos, footState, 3))
                    {
                        IBlockState headState = footState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
                        worldIn.setBlockState(headPos, headState, 3);
                    }

                    --stack.stackSize;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
    }
}
