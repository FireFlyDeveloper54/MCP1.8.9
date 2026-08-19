package net.minecraft.item;

import java.util.List;
import java.util.Random;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Rotations;
import net.minecraft.world.World;

public class ItemArmorStand extends Item
{
    public ItemArmorStand()
    {
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (side == EnumFacing.DOWN)
        {
            return false;
        }
        else
        {
            boolean isReplaceable = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
            BlockPos standPos = isReplaceable ? pos : pos.offset(side);

            if (!playerIn.canPlayerEdit(standPos, side, stack))
            {
                return false;
            }
            else
            {
                BlockPos headPos = standPos.up();
                boolean isPlacementBlocked = !worldIn.isAirBlock(standPos) && !worldIn.getBlockState(standPos).getBlock().isReplaceable(worldIn, standPos);
                isPlacementBlocked = isPlacementBlocked | (!worldIn.isAirBlock(headPos) && !worldIn.getBlockState(headPos).getBlock().isReplaceable(worldIn, headPos));

                if (isPlacementBlocked)
                {
                    return false;
                }
                else
                {
                    double xCoordinate = (double)standPos.getX();
                    double yCoordinate = (double)standPos.getY();
                    double zCoordinate = (double)standPos.getZ();
                    List<Entity> entitiesInPlacementArea = worldIn.getEntitiesWithinAABBExcludingEntity((Entity)null, AxisAlignedBB.fromBounds(xCoordinate, yCoordinate, zCoordinate, xCoordinate + 1.0D, yCoordinate + 2.0D, zCoordinate + 1.0D));

                    if (entitiesInPlacementArea.size() > 0)
                    {
                        return false;
                    }
                    else
                    {
                        if (!worldIn.isRemote)
                        {
                            worldIn.setBlockToAir(standPos);
                            worldIn.setBlockToAir(headPos);
                            EntityArmorStand entityArmorStand = new EntityArmorStand(worldIn, xCoordinate + 0.5D, yCoordinate, zCoordinate + 0.5D);
                            float yaw = (float)MathHelper.floor_float((MathHelper.wrapAngleTo180_float(playerIn.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                            entityArmorStand.setLocationAndAngles(xCoordinate + 0.5D, yCoordinate, zCoordinate + 0.5D, yaw, 0.0F);
                            this.applyRandomRotations(entityArmorStand, worldIn.rand);
                            NBTTagCompound stackTag = stack.getTagCompound();

                            if (stackTag != null && stackTag.hasKey("EntityTag", 10))
                            {
                                NBTTagCompound entityTag = new NBTTagCompound();
                                entityArmorStand.writeToNBTOptional(entityTag);
                                entityTag.merge(stackTag.getCompoundTag("EntityTag"));
                                entityArmorStand.readFromNBT(entityTag);
                            }

                            worldIn.spawnEntityInWorld(entityArmorStand);
                        }

                        --stack.stackSize;
                        return true;
                    }
                }
            }
        }
    }

    private void applyRandomRotations(EntityArmorStand armorStand, Random rand)
    {
        Rotations rotations = armorStand.getHeadRotation();
        float headPitchOffset = rand.nextFloat() * 5.0F;
        float headYawOffset = rand.nextFloat() * 20.0F - 10.0F;
        Rotations randomizedRotations = new Rotations(rotations.getX() + headPitchOffset, rotations.getY() + headYawOffset, rotations.getZ());
        armorStand.setHeadRotation(randomizedRotations);
        rotations = armorStand.getBodyRotation();
        float bodyYawOffset = rand.nextFloat() * 10.0F - 5.0F;
        randomizedRotations = new Rotations(rotations.getX(), rotations.getY() + bodyYawOffset, rotations.getZ());
        armorStand.setBodyRotation(randomizedRotations);
    }
}
