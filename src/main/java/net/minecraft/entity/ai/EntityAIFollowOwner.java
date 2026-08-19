package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwner extends EntityAIBase
{
    private EntityTameable thePet;
    private EntityLivingBase theOwner;
    World theWorld;
    private double followSpeed;
    private PathNavigate petPathfinder;
    private int timeToRecalcPath;
    float maxDist;
    float minDist;
    private boolean oldWaterCost;

    public EntityAIFollowOwner(EntityTameable thePetIn, double followSpeedIn, float minDistIn, float maxDistIn)
    {
        this.thePet = thePetIn;
        this.theWorld = thePetIn.worldObj;
        this.followSpeed = followSpeedIn;
        this.petPathfinder = thePetIn.getNavigator();
        this.minDist = minDistIn;
        this.maxDist = maxDistIn;
        this.setMutexBits(3);

        if (!(thePetIn.getNavigator() instanceof PathNavigateGround))
        {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean shouldExecute()
    {
        EntityLivingBase entityLivingBase = this.thePet.getOwner();

        if (entityLivingBase == null)
        {
            return false;
        }
        else if (entityLivingBase instanceof EntityPlayer && ((EntityPlayer)entityLivingBase).isSpectator())
        {
            return false;
        }
        else if (this.thePet.isSitting())
        {
            return false;
        }
        else if (this.thePet.getDistanceSqToEntity(entityLivingBase) < (double)(this.minDist * this.minDist))
        {
            return false;
        }
        else
        {
            this.theOwner = entityLivingBase;
            return true;
        }
    }

    public boolean continueExecuting()
    {
        return !this.petPathfinder.noPath() && this.thePet.getDistanceSqToEntity(this.theOwner) > (double)(this.maxDist * this.maxDist) && !this.thePet.isSitting();
    }

    public void startExecuting()
    {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = ((PathNavigateGround)this.thePet.getNavigator()).getAvoidsWater();
        ((PathNavigateGround)this.thePet.getNavigator()).setAvoidsWater(false);
    }

    public void resetTask()
    {
        this.theOwner = null;
        this.petPathfinder.clearPathEntity();
        ((PathNavigateGround)this.thePet.getNavigator()).setAvoidsWater(this.oldWaterCost);
    }

    private boolean isTeleportFriendlyBlock(BlockPos pos)
    {
        IBlockState blockState = this.theWorld.getBlockState(pos);
        Block block = blockState.getBlock();
        return block == Blocks.air ? true : !block.isFullCube();
    }

    public void updateTask()
    {
        this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());

        if (!this.thePet.isSitting())
        {
            if (--this.timeToRecalcPath <= 0)
            {
                this.timeToRecalcPath = 10;

                if (!this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.followSpeed))
                {
                    if (!this.thePet.getLeashed())
                    {
                        if (this.thePet.getDistanceSqToEntity(this.theOwner) >= 144.0D)
                        {
                            int baseX = MathHelper.floor_double(this.theOwner.posX) - 2;
                            int baseZ = MathHelper.floor_double(this.theOwner.posZ) - 2;
                            int baseY = MathHelper.floor_double(this.theOwner.getEntityBoundingBox().minY);
                            BlockPos.MutableBlockPos teleportCheckPos = new BlockPos.MutableBlockPos();

                            for (int xOffset = 0; xOffset <= 4; ++xOffset)
                            {
                                for (int zOffset = 0; zOffset <= 4; ++zOffset)
                                {
                                    int targetX = baseX + xOffset;
                                    int targetZ = baseZ + zOffset;

                                    if ((xOffset < 1 || zOffset < 1 || xOffset > 3 || zOffset > 3) && World.doesBlockHaveSolidTopSurface(this.theWorld, teleportCheckPos.set(targetX, baseY - 1, targetZ)) && this.isTeleportFriendlyBlock(teleportCheckPos.set(targetX, baseY, targetZ)) && this.isTeleportFriendlyBlock(teleportCheckPos.set(targetX, baseY + 1, targetZ)))
                                    {
                                        this.thePet.setLocationAndAngles((double)((float)targetX + 0.5F), (double)baseY, (double)((float)targetZ + 0.5F), this.thePet.rotationYaw, this.thePet.rotationPitch);
                                        this.petPathfinder.clearPathEntity();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
