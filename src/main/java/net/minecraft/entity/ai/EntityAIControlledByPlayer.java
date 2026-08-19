package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.pathfinder.WalkNodeProcessor;

public class EntityAIControlledByPlayer extends EntityAIBase
{
    private final EntityLiving thisEntity;
    private final float maxSpeed;
    private float currentSpeed;
    private boolean speedBoosted;
    private int speedBoostTime;
    private int maxSpeedBoostTime;
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();

    public EntityAIControlledByPlayer(EntityLiving entitylivingIn, float maxspeed)
    {
        this.thisEntity = entitylivingIn;
        this.maxSpeed = maxspeed;
        this.setMutexBits(7);
    }

    public void startExecuting()
    {
        this.currentSpeed = 0.0F;
    }

    public void resetTask()
    {
        this.speedBoosted = false;
        this.currentSpeed = 0.0F;
    }

    public boolean shouldExecute()
    {
        return this.thisEntity.isEntityAlive() && this.thisEntity.riddenByEntity != null && this.thisEntity.riddenByEntity instanceof EntityPlayer && (this.speedBoosted || this.thisEntity.canBeSteered());
    }

    public void updateTask()
    {
        EntityPlayer entityPlayer = (EntityPlayer)this.thisEntity.riddenByEntity;
        EntityCreature entityCreature = (EntityCreature)this.thisEntity;
        float yawDelta = MathHelper.wrapAngleTo180_float(entityPlayer.rotationYaw - this.thisEntity.rotationYaw) * 0.5F;

        if (yawDelta > 5.0F)
        {
            yawDelta = 5.0F;
        }

        if (yawDelta < -5.0F)
        {
            yawDelta = -5.0F;
        }

        this.thisEntity.rotationYaw = MathHelper.wrapAngleTo180_float(this.thisEntity.rotationYaw + yawDelta);

        if (this.currentSpeed < this.maxSpeed)
        {
            this.currentSpeed += (this.maxSpeed - this.currentSpeed) * 0.01F;
        }

        if (this.currentSpeed > this.maxSpeed)
        {
            this.currentSpeed = this.maxSpeed;
        }

        int entityX = MathHelper.floor_double(this.thisEntity.posX);
        int entityY = MathHelper.floor_double(this.thisEntity.posY);
        int entityZ = MathHelper.floor_double(this.thisEntity.posZ);
        float adjustedSpeed = this.currentSpeed;

        if (this.speedBoosted)
        {
            if (this.speedBoostTime++ > this.maxSpeedBoostTime)
            {
                this.speedBoosted = false;
            }

            adjustedSpeed += adjustedSpeed * 1.15F * MathHelper.sin((float)this.speedBoostTime / (float)this.maxSpeedBoostTime * (float)Math.PI);
        }

        float friction = 0.91F;

        if (this.thisEntity.onGround)
        {
            friction = this.thisEntity.worldObj.getBlockState(this.blockSamplePos.set(MathHelper.floor_float((float)entityX), MathHelper.floor_float((float)entityY) - 1, MathHelper.floor_float((float)entityZ))).getBlock().slipperiness * 0.91F;
        }

        float movementFactor = 0.16277136F / (friction * friction * friction);
        float yawSin = MathHelper.sin(entityCreature.rotationYaw * (float)Math.PI / 180.0F);
        float yawCos = MathHelper.cos(entityCreature.rotationYaw * (float)Math.PI / 180.0F);
        float scaledMoveSpeed = entityCreature.getAIMoveSpeed() * movementFactor;
        float speedScale = Math.max(adjustedSpeed, 1.0F);
        speedScale = scaledMoveSpeed / speedScale;
        float speedStep = adjustedSpeed * speedScale;
        float offsetX = -(speedStep * yawSin);
        float offsetZ = speedStep * yawCos;

        if (MathHelper.abs(offsetX) > MathHelper.abs(offsetZ))
        {
            if (offsetX < 0.0F)
            {
                offsetX -= this.thisEntity.width / 2.0F;
            }

            if (offsetX > 0.0F)
            {
                offsetX += this.thisEntity.width / 2.0F;
            }

            offsetZ = 0.0F;
        }
        else
        {
            offsetX = 0.0F;

            if (offsetZ < 0.0F)
            {
                offsetZ -= this.thisEntity.width / 2.0F;
            }

            if (offsetZ > 0.0F)
            {
                offsetZ += this.thisEntity.width / 2.0F;
            }
        }

        int nextX = MathHelper.floor_double(this.thisEntity.posX + (double)offsetX);
        int nextZ = MathHelper.floor_double(this.thisEntity.posZ + (double)offsetZ);
        int pathWidth = MathHelper.floor_float(this.thisEntity.width + 1.0F);
        int pathHeight = MathHelper.floor_float(this.thisEntity.height + entityPlayer.height + 1.0F);
        int pathDepth = MathHelper.floor_float(this.thisEntity.width + 1.0F);

        if (entityX != nextX || entityZ != nextZ)
        {
            Block block = this.thisEntity.worldObj.getBlockState(this.blockSamplePos.set(entityX, entityY, entityZ)).getBlock();
            boolean shouldCheckStepUp = !this.isStairOrSlab(block) && (block.getMaterial() != Material.air || !this.isStairOrSlab(this.thisEntity.worldObj.getBlockState(this.blockSamplePos.set(entityX, entityY - 1, entityZ)).getBlock()));

            if (shouldCheckStepUp && 0 == WalkNodeProcessor.getPathNodeType(this.thisEntity.worldObj, this.thisEntity, nextX, entityY, nextZ, pathWidth, pathHeight, pathDepth, false, false, true) && 1 == WalkNodeProcessor.getPathNodeType(this.thisEntity.worldObj, this.thisEntity, entityX, entityY + 1, entityZ, pathWidth, pathHeight, pathDepth, false, false, true) && 1 == WalkNodeProcessor.getPathNodeType(this.thisEntity.worldObj, this.thisEntity, nextX, entityY + 1, nextZ, pathWidth, pathHeight, pathDepth, false, false, true))
            {
                entityCreature.getJumpHelper().setJumping();
            }
        }

        if (!entityPlayer.capabilities.isCreativeMode && this.currentSpeed >= this.maxSpeed * 0.5F && this.thisEntity.getRNG().nextFloat() < 0.006F && !this.speedBoosted)
        {
            ItemStack itemStack = entityPlayer.getHeldItem();

            if (itemStack != null && itemStack.getItem() == Items.carrot_on_a_stick)
            {
                itemStack.damageItem(1, entityPlayer);

                if (itemStack.stackSize == 0)
                {
                    ItemStack replacementRod = new ItemStack(Items.fishing_rod);
                    replacementRod.setTagCompound(itemStack.getTagCompound());
                    entityPlayer.inventory.mainInventory[entityPlayer.inventory.currentItem] = replacementRod;
                }
            }
        }

        this.thisEntity.moveEntityWithHeading(0.0F, adjustedSpeed);
    }

    private boolean isStairOrSlab(Block blockIn)
    {
        return blockIn instanceof BlockStairs || blockIn instanceof BlockSlab;
    }

    public boolean isSpeedBoosted()
    {
        return this.speedBoosted;
    }

    public void boostSpeed()
    {
        this.speedBoosted = true;
        this.speedBoostTime = 0;
        this.maxSpeedBoostTime = this.thisEntity.getRNG().nextInt(841) + 140;
    }

    public boolean isControlledByPlayer()
    {
        return !this.isSpeedBoosted() && this.currentSpeed > this.maxSpeed * 0.3F;
    }
}
