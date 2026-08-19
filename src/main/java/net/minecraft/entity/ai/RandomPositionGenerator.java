package net.minecraft.entity.ai;

import java.util.Random;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class RandomPositionGenerator
{
    private static Vec3 staticVector = new Vec3(0.0D, 0.0D, 0.0D);

    public static Vec3 findRandomTarget(EntityCreature entitycreatureIn, int xz, int y)
    {
        return findRandomTargetBlock(entitycreatureIn, xz, y, (Vec3)null);
    }

    public static Vec3 findRandomTargetBlockTowards(EntityCreature entitycreatureIn, int xz, int y, Vec3 targetVec3)
    {
        staticVector = targetVec3.subtract(entitycreatureIn.posX, entitycreatureIn.posY, entitycreatureIn.posZ);
        return findRandomTargetBlock(entitycreatureIn, xz, y, staticVector);
    }

    public static Vec3 findRandomTargetBlockAwayFrom(EntityCreature entitycreatureIn, int xz, int y, Vec3 targetVec3)
    {
        staticVector = (new Vec3(entitycreatureIn.posX, entitycreatureIn.posY, entitycreatureIn.posZ)).subtract(targetVec3);
        return findRandomTargetBlock(entitycreatureIn, xz, y, staticVector);
    }

    private static Vec3 findRandomTargetBlock(EntityCreature entitycreatureIn, int xz, int y, Vec3 targetVec3)
    {
        Random random = entitycreatureIn.getRNG();
        boolean foundPosition = false;
        int bestX = 0;
        int bestY = 0;
        int bestZ = 0;
        float bestWeight = -99999.0F;
        boolean withinHomeRange;
        BlockPos.MutableBlockPos candidatePos = new BlockPos.MutableBlockPos();

        if (entitycreatureIn.hasHome())
        {
            double distanceToHomeSq = entitycreatureIn.getHomePosition().distanceSq((double)MathHelper.floor_double(entitycreatureIn.posX), (double)MathHelper.floor_double(entitycreatureIn.posY), (double)MathHelper.floor_double(entitycreatureIn.posZ)) + 4.0D;
            double maxDistance = (double)(entitycreatureIn.getMaximumHomeDistance() + (float)xz);
            withinHomeRange = distanceToHomeSq < maxDistance * maxDistance;
        }
        else
        {
            withinHomeRange = false;
        }

        for (int attempt = 0; attempt < 10; ++attempt)
        {
            int candidateX = random.nextInt(2 * xz + 1) - xz;
            int candidateY = random.nextInt(2 * y + 1) - y;
            int candidateZ = random.nextInt(2 * xz + 1) - xz;

            if (targetVec3 == null || (double)candidateX * targetVec3.xCoord + (double)candidateZ * targetVec3.zCoord >= 0.0D)
            {
                if (entitycreatureIn.hasHome() && xz > 1)
                {
                    BlockPos homePos = entitycreatureIn.getHomePosition();

                    if (entitycreatureIn.posX > (double)homePos.getX())
                    {
                        candidateX -= random.nextInt(xz / 2);
                    }
                    else
                    {
                        candidateX += random.nextInt(xz / 2);
                    }

                    if (entitycreatureIn.posZ > (double)homePos.getZ())
                    {
                        candidateZ -= random.nextInt(xz / 2);
                    }
                    else
                    {
                        candidateZ += random.nextInt(xz / 2);
                    }
                }

                candidateX = candidateX + MathHelper.floor_double(entitycreatureIn.posX);
                candidateY = candidateY + MathHelper.floor_double(entitycreatureIn.posY);
                candidateZ = candidateZ + MathHelper.floor_double(entitycreatureIn.posZ);

                if (!withinHomeRange || entitycreatureIn.isWithinHomeDistanceFromPosition(candidatePos.set(candidateX, candidateY, candidateZ)))
                {
                    float pathWeight = entitycreatureIn.getBlockPathWeight(candidatePos);

                    if (pathWeight > bestWeight)
                    {
                        bestWeight = pathWeight;
                        bestX = candidateX;
                        bestY = candidateY;
                        bestZ = candidateZ;
                        foundPosition = true;
                    }
                }
            }
        }

        if (foundPosition)
        {
            return new Vec3((double)bestX, (double)bestY, (double)bestZ);
        }
        else
        {
            return null;
        }
    }
}
