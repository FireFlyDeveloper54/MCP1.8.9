package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAIMoveToBlock extends EntityAIBase
{
    private final EntityCreature theEntity;
    private final double movementSpeed;
    protected int runDelay;
    private int timeoutCounter;
    private int maxStayTicks;
    protected BlockPos destinationBlock = BlockPos.ORIGIN;
    private boolean isAboveDestination;
    private int searchLength;

    public EntityAIMoveToBlock(EntityCreature creature, double speedIn, int length)
    {
        this.theEntity = creature;
        this.movementSpeed = speedIn;
        this.searchLength = length;
        this.setMutexBits(5);
    }

    public boolean shouldExecute()
    {
        if (this.runDelay > 0)
        {
            --this.runDelay;
            return false;
        }
        else
        {
            this.runDelay = 200 + this.theEntity.getRNG().nextInt(200);
            return this.searchForDestination();
        }
    }

    public boolean continueExecuting()
    {
        return this.timeoutCounter >= -this.maxStayTicks && this.timeoutCounter <= 1200 && this.shouldMoveTo(this.theEntity.worldObj, this.destinationBlock);
    }

    public void startExecuting()
    {
        this.theEntity.getNavigator().tryMoveToXYZ((double)((float)this.destinationBlock.getX()) + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)((float)this.destinationBlock.getZ()) + 0.5D, this.movementSpeed);
        this.timeoutCounter = 0;
        this.maxStayTicks = this.theEntity.getRNG().nextInt(this.theEntity.getRNG().nextInt(1200) + 1200) + 1200;
    }

    public void resetTask()
    {
    }

    public void updateTask()
    {
        if (this.theEntity.getDistanceSqToCenter(this.destinationBlock.up()) > 1.0D)
        {
            this.isAboveDestination = false;
            ++this.timeoutCounter;

            if (this.timeoutCounter % 40 == 0)
            {
                this.theEntity.getNavigator().tryMoveToXYZ((double)((float)this.destinationBlock.getX()) + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)((float)this.destinationBlock.getZ()) + 0.5D, this.movementSpeed);
            }
        }
        else
        {
            this.isAboveDestination = true;
            --this.timeoutCounter;
        }
    }

    protected boolean getIsAboveDestination()
    {
        return this.isAboveDestination;
    }

    private boolean searchForDestination()
    {
        int searchRange = this.searchLength;
        int verticalSearchRange = 1;
        int entityX = MathHelper.floor_double(this.theEntity.posX);
        int entityY = MathHelper.floor_double(this.theEntity.posY);
        int entityZ = MathHelper.floor_double(this.theEntity.posZ);
        BlockPos.MutableBlockPos candidatePos = new BlockPos.MutableBlockPos();

        for (int verticalOffset = 0; verticalOffset <= 1; verticalOffset = verticalOffset > 0 ? -verticalOffset : 1 - verticalOffset)
        {
            for (int horizontalRadius = 0; horizontalRadius < searchRange; ++horizontalRadius)
            {
                for (int xOffset = 0; xOffset <= horizontalRadius; xOffset = xOffset > 0 ? -xOffset : 1 - xOffset)
                {
                    for (int zOffset = xOffset < horizontalRadius && xOffset > -horizontalRadius ? horizontalRadius : 0; zOffset <= horizontalRadius; zOffset = zOffset > 0 ? -zOffset : 1 - zOffset)
                    {
                        candidatePos.set(entityX + xOffset, entityY + verticalOffset - 1, entityZ + zOffset);

                        if (this.theEntity.isWithinHomeDistanceFromPosition(candidatePos) && this.shouldMoveTo(this.theEntity.worldObj, candidatePos))
                        {
                            this.destinationBlock = new BlockPos(candidatePos);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected abstract boolean shouldMoveTo(World worldIn, BlockPos pos);
}
