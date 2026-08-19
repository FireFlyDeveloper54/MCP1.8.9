package net.minecraft.item;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemMinecart extends Item
{
    private static final IBehaviorDispenseItem dispenserMinecartBehavior = new BehaviorDefaultDispenseItem()
    {
        private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
        {
            EnumFacing facing = BlockDispenser.getFacing(source.getBlockMetadata());
            World world = source.getWorld();
            double xCoordinate = source.getX() + (double)facing.getFrontOffsetX() * 1.125D;
            double yCoordinate = Math.floor(source.getY()) + (double)facing.getFrontOffsetY();
            double zCoordinate = source.getZ() + (double)facing.getFrontOffsetZ() * 1.125D;
            BlockPos blockPos = source.getBlockPos().offset(facing);
            IBlockState railState = world.getBlockState(blockPos);
            BlockRailBase.EnumRailDirection railDirection = railState.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)railState.getValue(((BlockRailBase)railState.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            double yOffset;

            if (BlockRailBase.isRailBlock(railState))
            {
                if (railDirection.isAscending())
                {
                    yOffset = 0.6D;
                }
                else
                {
                    yOffset = 0.1D;
                }
            }
            else
            {
                if (railState.getBlock().getMaterial() != Material.air || !BlockRailBase.isRailBlock(world.getBlockState(blockPos.down())))
                {
                    return this.behaviourDefaultDispenseItem.dispense(source, stack);
                }

                IBlockState lowerRailState = world.getBlockState(blockPos.down());
                BlockRailBase.EnumRailDirection lowerRailDirection = lowerRailState.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)lowerRailState.getValue(((BlockRailBase)lowerRailState.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;

                if (facing != EnumFacing.DOWN && lowerRailDirection.isAscending())
                {
                    yOffset = -0.4D;
                }
                else
                {
                    yOffset = -0.9D;
                }
            }

            EntityMinecart minecart = EntityMinecart.getMinecart(world, xCoordinate, yCoordinate + yOffset, zCoordinate, ((ItemMinecart)stack.getItem()).minecartType);

            if (stack.hasDisplayName())
            {
                minecart.setCustomNameTag(stack.getDisplayName());
            }

            world.spawnEntityInWorld(minecart);
            stack.splitStack(1);
            return stack;
        }
        protected void playDispenseSound(IBlockSource source)
        {
            source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
        }
    };
    private final EntityMinecart.EnumMinecartType minecartType;

    public ItemMinecart(EntityMinecart.EnumMinecartType type)
    {
        this.maxStackSize = 1;
        this.minecartType = type;
        this.setCreativeTab(CreativeTabs.tabTransport);
        BlockDispenser.dispenseBehaviorRegistry.putObject(this, dispenserMinecartBehavior);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        IBlockState railState = worldIn.getBlockState(pos);

        if (BlockRailBase.isRailBlock(railState))
        {
            if (!worldIn.isRemote)
            {
                BlockRailBase.EnumRailDirection railDirection = railState.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)railState.getValue(((BlockRailBase)railState.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                double yOffset = 0.0D;

                if (railDirection.isAscending())
                {
                    yOffset = 0.5D;
                }

                EntityMinecart minecart = EntityMinecart.getMinecart(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.0625D + yOffset, (double)pos.getZ() + 0.5D, this.minecartType);

                if (stack.hasDisplayName())
                {
                    minecart.setCustomNameTag(stack.getDisplayName());
                }

                worldIn.spawnEntityInWorld(minecart);
            }

            --stack.stackSize;
            return true;
        }
        else
        {
            return false;
        }
    }
}
