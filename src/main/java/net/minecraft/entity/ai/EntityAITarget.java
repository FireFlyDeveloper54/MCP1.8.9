package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

public abstract class EntityAITarget extends EntityAIBase
{
    protected final EntityCreature taskOwner;
    protected boolean shouldCheckSight;
    private boolean nearbyOnly;
    private int targetSearchStatus;
    private int targetSearchDelay;
    private int targetUnseenTicks;

    public EntityAITarget(EntityCreature creature, boolean checkSight)
    {
        this(creature, checkSight, false);
    }

    public EntityAITarget(EntityCreature creature, boolean checkSight, boolean onlyNearby)
    {
        this.taskOwner = creature;
        this.shouldCheckSight = checkSight;
        this.nearbyOnly = onlyNearby;
    }

    public boolean continueExecuting()
    {
        EntityLivingBase attackTarget = this.taskOwner.getAttackTarget();

        if (attackTarget == null)
        {
            return false;
        }
        else if (!attackTarget.isEntityAlive())
        {
            return false;
        }
        else
        {
            Team team = this.taskOwner.getTeam();
            Team targetTeam = attackTarget.getTeam();

            if (team != null && targetTeam == team)
            {
                return false;
            }
            else
            {
                double targetDistance = this.getTargetDistance();

                if (this.taskOwner.getDistanceSqToEntity(attackTarget) > targetDistance * targetDistance)
                {
                    return false;
                }
                else
                {
                    if (this.shouldCheckSight)
                    {
                        if (this.taskOwner.getEntitySenses().canSee(attackTarget))
                        {
                            this.targetUnseenTicks = 0;
                        }
                        else if (++this.targetUnseenTicks > 60)
                        {
                            return false;
                        }
                    }

                    return !(attackTarget instanceof EntityPlayer) || !((EntityPlayer)attackTarget).capabilities.disableDamage;
                }
            }
        }
    }

    protected double getTargetDistance()
    {
        IAttributeInstance followRangeAttribute = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.followRange);
        return followRangeAttribute == null ? 16.0D : followRangeAttribute.getAttributeValue();
    }

    public void startExecuting()
    {
        this.targetSearchStatus = 0;
        this.targetSearchDelay = 0;
        this.targetUnseenTicks = 0;
    }

    public void resetTask()
    {
        this.taskOwner.setAttackTarget((EntityLivingBase)null);
    }

    public static boolean isSuitableTarget(EntityLiving attacker, EntityLivingBase target, boolean includeInvincibles, boolean checkSight)
    {
        if (target == null)
        {
            return false;
        }
        else if (target == attacker)
        {
            return false;
        }
        else if (!target.isEntityAlive())
        {
            return false;
        }
        else if (!attacker.canAttackClass(target.getClass()))
        {
            return false;
        }
        else
        {
            Team team = attacker.getTeam();
            Team targetTeam = target.getTeam();

            if (team != null && targetTeam == team)
            {
                return false;
            }
            else
            {
                if (attacker instanceof IEntityOwnable && StringUtils.isNotEmpty(((IEntityOwnable)attacker).getOwnerId()))
                {
                    if (target instanceof IEntityOwnable && ((IEntityOwnable)attacker).getOwnerId().equals(((IEntityOwnable)target).getOwnerId()))
                    {
                        return false;
                    }

                    if (target == ((IEntityOwnable)attacker).getOwner())
                    {
                        return false;
                    }
                }
                else if (target instanceof EntityPlayer && !includeInvincibles && ((EntityPlayer)target).capabilities.disableDamage)
                {
                    return false;
                }

                return !checkSight || attacker.getEntitySenses().canSee(target);
            }
        }
    }

    protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles)
    {
        if (!isSuitableTarget(this.taskOwner, target, includeInvincibles, this.shouldCheckSight))
        {
            return false;
        }
        else if (!this.taskOwner.isWithinHomeDistanceFromPosition(new BlockPos(target)))
        {
            return false;
        }
        else
        {
            if (this.nearbyOnly)
            {
                if (--this.targetSearchDelay <= 0)
                {
                    this.targetSearchStatus = 0;
                }

                if (this.targetSearchStatus == 0)
                {
                    this.targetSearchStatus = this.canEasilyReach(target) ? 1 : 2;
                }

                if (this.targetSearchStatus == 2)
                {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean canEasilyReach(EntityLivingBase target)
    {
        this.targetSearchDelay = 10 + this.taskOwner.getRNG().nextInt(5);
        PathEntity path = this.taskOwner.getNavigator().getPathToEntityLiving(target);

        if (path == null)
        {
            return false;
        }
        else
        {
            PathPoint finalPathPoint = path.getFinalPathPoint();

            if (finalPathPoint == null)
            {
                return false;
            }
            else
            {
                int deltaX = finalPathPoint.xCoord - MathHelper.floor_double(target.posX);
                int deltaZ = finalPathPoint.zCoord - MathHelper.floor_double(target.posZ);
                return (double)(deltaX * deltaX + deltaZ * deltaZ) <= 2.25D;
            }
        }
    }
}
