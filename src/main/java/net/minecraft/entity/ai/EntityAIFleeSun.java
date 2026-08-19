package net.minecraft.entity.ai;

import java.util.Random;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityAIFleeSun extends EntityAIBase
{
    private EntityCreature theCreature;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private double movementSpeed;
    private World theWorld;
    private final BlockPos.MutableBlockPos creaturePos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos shelterCheckPos = new BlockPos.MutableBlockPos();

    public EntityAIFleeSun(EntityCreature theCreatureIn, double movementSpeedIn)
    {
        this.theCreature = theCreatureIn;
        this.movementSpeed = movementSpeedIn;
        this.theWorld = theCreatureIn.worldObj;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        if (!this.theWorld.isDaytime())
        {
            return false;
        }
        else if (!this.theCreature.isBurning())
        {
            return false;
        }
        else if (!this.theWorld.canSeeSky(this.creaturePos.set(MathHelper.floor_double(this.theCreature.posX), MathHelper.floor_double(this.theCreature.getEntityBoundingBox().minY), MathHelper.floor_double(this.theCreature.posZ))))
        {
            return false;
        }
        else
        {
            Vec3 shelterPos = this.findPossibleShelter();

            if (shelterPos == null)
            {
                return false;
            }
            else
            {
                this.shelterX = shelterPos.xCoord;
                this.shelterY = shelterPos.yCoord;
                this.shelterZ = shelterPos.zCoord;
                return true;
            }
        }
    }

    public boolean continueExecuting()
    {
        return !this.theCreature.getNavigator().noPath();
    }

    public void startExecuting()
    {
        this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
    }

    private Vec3 findPossibleShelter()
    {
        Random random = this.theCreature.getRNG();
        int baseX = MathHelper.floor_double(this.theCreature.posX);
        int baseY = MathHelper.floor_double(this.theCreature.getEntityBoundingBox().minY);
        int baseZ = MathHelper.floor_double(this.theCreature.posZ);

        for (int attempt = 0; attempt < 10; ++attempt)
        {
            BlockPos candidatePos = this.shelterCheckPos.set(baseX + random.nextInt(20) - 10, baseY + random.nextInt(6) - 3, baseZ + random.nextInt(20) - 10);

            if (!this.theWorld.canSeeSky(candidatePos) && this.theCreature.getBlockPathWeight(candidatePos) < 0.0F)
            {
                return new Vec3((double)candidatePos.getX(), (double)candidatePos.getY(), (double)candidatePos.getZ());
            }
        }

        return null;
    }
}
