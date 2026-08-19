package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemDoor extends Item
{
    private Block block;

    public ItemDoor(Block block)
    {
        this.block = block;
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (side != EnumFacing.UP)
        {
            return false;
        }
        else
        {
            IBlockState blockState = worldIn.getBlockState(pos);
            Block block = blockState.getBlock();

            if (!block.isReplaceable(worldIn, pos))
            {
                pos = pos.offset(side);
            }

            if (!playerIn.canPlayerEdit(pos, side, stack))
            {
                return false;
            }
            else if (!this.block.canPlaceBlockAt(worldIn, pos))
            {
                return false;
            }
            else
            {
                placeDoor(worldIn, pos, EnumFacing.fromAngle((double)playerIn.rotationYaw), this.block);
                --stack.stackSize;
                return true;
            }
        }
    }

    public static void placeDoor(World worldIn, BlockPos pos, EnumFacing facing, Block door)
    {
        BlockPos rightPos = pos.offset(facing.rotateY());
        BlockPos leftPos = pos.offset(facing.rotateYCCW());
        int leftSolidBlocks = (worldIn.getBlockState(leftPos).getBlock().isNormalCube() ? 1 : 0) + (worldIn.getBlockState(leftPos.up()).getBlock().isNormalCube() ? 1 : 0);
        int rightSolidBlocks = (worldIn.getBlockState(rightPos).getBlock().isNormalCube() ? 1 : 0) + (worldIn.getBlockState(rightPos.up()).getBlock().isNormalCube() ? 1 : 0);
        boolean hasDoorOnLeft = worldIn.getBlockState(leftPos).getBlock() == door || worldIn.getBlockState(leftPos.up()).getBlock() == door;
        boolean hasDoorOnRight = worldIn.getBlockState(rightPos).getBlock() == door || worldIn.getBlockState(rightPos.up()).getBlock() == door;
        boolean useRightHinge = false;

        if (hasDoorOnLeft && !hasDoorOnRight || rightSolidBlocks > leftSolidBlocks)
        {
            useRightHinge = true;
        }

        BlockPos upperPos = pos.up();
        IBlockState doorState = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, useRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT);
        worldIn.setBlockState(pos, doorState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
        worldIn.setBlockState(upperPos, doorState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
        worldIn.notifyNeighborsOfStateChange(pos, door);
        worldIn.notifyNeighborsOfStateChange(upperPos, door);
    }
}
