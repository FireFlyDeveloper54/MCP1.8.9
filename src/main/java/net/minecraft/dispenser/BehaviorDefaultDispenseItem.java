package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviorDefaultDispenseItem implements IBehaviorDispenseItem
{
    public final ItemStack dispense(IBlockSource source, ItemStack stack)
    {
        ItemStack itemStack = this.dispenseStack(source, stack);
        this.playDispenseSound(source);
        this.spawnDispenseParticles(source, BlockDispenser.getFacing(source.getBlockMetadata()));
        return itemStack;
    }

    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
    {
        EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
        IPosition iposition = BlockDispenser.getDispensePosition(source);
        ItemStack itemstack = stack.splitStack(1);
        doDispense(source.getWorld(), itemstack, 6, enumfacing, iposition);
        return stack;
    }

    public static void doDispense(World worldIn, ItemStack stack, int speed, EnumFacing facing, IPosition position)
    {
        double xCoordinate = position.getX();
        double yCoordinate = position.getY();
        double zCoordinate = position.getZ();

        if (facing.getAxis() == EnumFacing.Axis.Y)
        {
            yCoordinate = yCoordinate - 0.125D;
        }
        else
        {
            yCoordinate = yCoordinate - 0.15625D;
        }

        EntityItem entityItem = new EntityItem(worldIn, xCoordinate, yCoordinate, zCoordinate, stack);
        double doubleValue = worldIn.rand.nextDouble() * 0.1D + 0.2D;
        entityItem.motionX = (double)facing.getFrontOffsetX() * doubleValue;
        entityItem.motionY = 0.20000000298023224D;
        entityItem.motionZ = (double)facing.getFrontOffsetZ() * doubleValue;
        entityItem.motionX += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
        entityItem.motionY += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
        entityItem.motionZ += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
        worldIn.spawnEntityInWorld(entityItem);
    }

    protected void playDispenseSound(IBlockSource source)
    {
        source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
    }

    protected void spawnDispenseParticles(IBlockSource source, EnumFacing facingIn)
    {
        source.getWorld().playAuxSFX(2000, source.getBlockPos(), this.getDispenseParticleData(facingIn));
    }

    private int getDispenseParticleData(EnumFacing facingIn)
    {
        return facingIn.getFrontOffsetX() + 1 + (facingIn.getFrontOffsetZ() + 1) * 3;
    }
}
