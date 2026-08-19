package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.util.MathHelper;

public class EntityAIArrowAttack extends EntityAIBase
{
    private final EntityLiving entityHost;
    private final IRangedAttackMob rangedAttackEntityHost;
    private EntityLivingBase attackTarget;
    private int rangedAttackTime;
    private double entityMoveSpeed;
    private int seeTime;
    private int minRangedAttackTime;
    private int maxRangedAttackTime;
    private float attackRadius;
    private float maxAttackDistance;

    public EntityAIArrowAttack(IRangedAttackMob attacker, double movespeed, int attackInterval, float maxAttackDistanceIn)
    {
        this(attacker, movespeed, attackInterval, attackInterval, maxAttackDistanceIn);
    }

    public EntityAIArrowAttack(IRangedAttackMob attacker, double movespeed, int minAttackTime, int maxAttackTime, float maxAttackDistanceIn)
    {
        this.rangedAttackTime = -1;

        if (!(attacker instanceof EntityLivingBase))
        {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        else
        {
            this.rangedAttackEntityHost = attacker;
            this.entityHost = (EntityLiving)attacker;
            this.entityMoveSpeed = movespeed;
            this.minRangedAttackTime = minAttackTime;
            this.maxRangedAttackTime = maxAttackTime;
            this.attackRadius = maxAttackDistanceIn;
            this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
            this.setMutexBits(3);
        }
    }

    public boolean shouldExecute()
    {
        EntityLivingBase currentTarget = this.entityHost.getAttackTarget();

        if (currentTarget == null)
        {
            return false;
        }
        else
        {
            this.attackTarget = currentTarget;
            return true;
        }
    }

    public boolean continueExecuting()
    {
        return this.shouldExecute() || !this.entityHost.getNavigator().noPath();
    }

    public void resetTask()
    {
        this.attackTarget = null;
        this.seeTime = 0;
        this.rangedAttackTime = -1;
    }

    public void updateTask()
    {
        double targetDistanceSq = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
        boolean canSeeTarget = this.entityHost.getEntitySenses().canSee(this.attackTarget);

        if (canSeeTarget)
        {
            ++this.seeTime;
        }
        else
        {
            this.seeTime = 0;
        }

        if (targetDistanceSq <= (double)this.maxAttackDistance && this.seeTime >= 20)
        {
            this.entityHost.getNavigator().clearPathEntity();
        }
        else
        {
            this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, this.entityMoveSpeed);
        }

        this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);

        if (--this.rangedAttackTime == 0)
        {
            if (targetDistanceSq > (double)this.maxAttackDistance || !canSeeTarget)
            {
                return;
            }

            float distanceFactor = MathHelper.sqrt_double(targetDistanceSq) / this.attackRadius;
            float clampedDistanceFactor = MathHelper.clamp_float(distanceFactor, 0.1F, 1.0F);
            this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, clampedDistanceFactor);
            this.rangedAttackTime = MathHelper.floor_float(distanceFactor * (float)(this.maxRangedAttackTime - this.minRangedAttackTime) + (float)this.minRangedAttackTime);
        }
        else if (this.rangedAttackTime < 0)
        {
            float distanceFactor = MathHelper.sqrt_double(targetDistanceSq) / this.attackRadius;
            this.rangedAttackTime = MathHelper.floor_float(distanceFactor * (float)(this.maxRangedAttackTime - this.minRangedAttackTime) + (float)this.minRangedAttackTime);
        }
    }
}
