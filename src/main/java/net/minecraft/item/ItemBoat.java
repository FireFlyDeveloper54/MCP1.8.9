package net.minecraft.item;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemBoat extends Item
{
    public ItemBoat()
    {
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.tabTransport);
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        float partialTicks = 1.0F;
        float pitch = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) * partialTicks;
        float yaw = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) * partialTicks;
        double playerX = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX) * (double)partialTicks;
        double playerY = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) * (double)partialTicks + (double)playerIn.getEyeHeight();
        double playerZ = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ) * (double)partialTicks;
        Vec3 eyePos = new Vec3(playerX, playerY, playerZ);
        float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
        float pitchSin = MathHelper.sin(-pitch * 0.017453292F);
        float lookX = yawSin * pitchCos;
        float lookZ = yawCos * pitchCos;
        double reachDistance = 5.0D;
        Vec3 reachEnd = eyePos.addVector((double)lookX * reachDistance, (double)pitchSin * reachDistance, (double)lookZ * reachDistance);
        MovingObjectPosition hitResult = worldIn.rayTraceBlocks(eyePos, reachEnd, true);

        if (hitResult == null)
        {
            return itemStackIn;
        }
        else
        {
            Vec3 lookVec = playerIn.getLook(partialTicks);
            boolean isInsideEntity = false;
            float collisionExpansion = 1.0F;
            List<Entity> nearbyEntities = worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, playerIn.getEntityBoundingBox().addCoord(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance).expand((double)collisionExpansion, (double)collisionExpansion, (double)collisionExpansion));

            for (int entityIndex = 0; entityIndex < nearbyEntities.size(); ++entityIndex)
            {
                Entity entity = (Entity)nearbyEntities.get(entityIndex);

                if (entity.canBeCollidedWith())
                {
                    float collisionBorder = entity.getCollisionBorderSize();
                    AxisAlignedBB expandedBox = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);

                    if (expandedBox.isVecInside(eyePos))
                    {
                        isInsideEntity = true;
                    }
                }
            }

            if (isInsideEntity)
            {
                return itemStackIn;
            }
            else
            {
                if (hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    BlockPos boatPos = hitResult.getBlockPos();

                    if (worldIn.getBlockState(boatPos).getBlock() == Blocks.snow_layer)
                    {
                        boatPos = boatPos.down();
                    }

                    EntityBoat boat = new EntityBoat(worldIn, (double)((float)boatPos.getX() + 0.5F), (double)((float)boatPos.getY() + 1.0F), (double)((float)boatPos.getZ() + 0.5F));
                    boat.rotationYaw = (float)(((MathHelper.floor_double((double)(playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) - 1) * 90);

                    if (!worldIn.getCollidingBoundingBoxes(boat, boat.getEntityBoundingBox().expand(-0.1D, -0.1D, -0.1D)).isEmpty())
                    {
                        return itemStackIn;
                    }

                    if (!worldIn.isRemote)
                    {
                        worldIn.spawnEntityInWorld(boat);
                    }

                    if (!playerIn.capabilities.isCreativeMode)
                    {
                        --itemStackIn.stackSize;
                    }

                    playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                }

                return itemStackIn;
            }
        }
    }
}
