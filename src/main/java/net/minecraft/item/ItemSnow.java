package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemSnow extends ItemBlock
{
    public ItemSnow(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (stack.stackSize == 0)
        {
            return false;
        }
        else if (!playerIn.canPlayerEdit(pos, side, stack))
        {
            return false;
        }
        else
        {
            IBlockState blockState = worldIn.getBlockState(pos);
            Block block = blockState.getBlock();
            BlockPos targetPos = pos;

            if ((side != EnumFacing.UP || block != this.block) && !block.isReplaceable(worldIn, pos))
            {
                targetPos = pos.offset(side);
                blockState = worldIn.getBlockState(targetPos);
                block = blockState.getBlock();
            }

            if (block == this.block)
            {
                int layers = ((Integer)blockState.getValue(BlockSnow.LAYERS));

                if (layers <= 7)
                {
                    IBlockState newBlockState = blockState.withProperty(BlockSnow.LAYERS, Integer.valueOf(layers + 1));
                    AxisAlignedBB collisionBox = this.block.getCollisionBoundingBox(worldIn, targetPos, newBlockState);

                    if (collisionBox != null && worldIn.checkNoEntityCollision(collisionBox) && worldIn.setBlockState(targetPos, newBlockState, 2))
                    {
                        worldIn.playSoundEffect((double)((float)targetPos.getX() + 0.5F), (double)((float)targetPos.getY() + 0.5F), (double)((float)targetPos.getZ() + 0.5F), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
                        --stack.stackSize;
                        return true;
                    }
                }
            }

            return super.onItemUse(stack, playerIn, worldIn, targetPos, side, hitX, hitY, hitZ);
        }
    }

    public int getMetadata(int damage)
    {
        return damage;
    }
}
