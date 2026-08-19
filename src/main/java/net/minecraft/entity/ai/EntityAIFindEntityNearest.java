package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAIFindEntityNearest extends EntityAIBase
{
    private static final Logger LOGGER = LogManager.getLogger();
    private EntityLiving mob;
    private final Predicate<EntityLivingBase> targetEntitySelector;
    private final EntityAINearestAttackableTarget.Sorter sorter;
    private EntityLivingBase target;
    private Class <? extends EntityLivingBase > targetClass;

    public EntityAIFindEntityNearest(EntityLiving mobIn, Class <? extends EntityLivingBase > targetClassIn)
    {
        this.mob = mobIn;
        this.targetClass = targetClassIn;

        if (mobIn instanceof EntityCreature)
        {
            LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
        }

        this.targetEntitySelector = new Predicate<EntityLivingBase>()
        {
            public boolean apply(EntityLivingBase candidate)
            {
                double followRange = EntityAIFindEntityNearest.this.getFollowRange();

                if (candidate.isSneaking())
                {
                    followRange *= 0.800000011920929D;
                }

                return candidate.isInvisible() ? false : ((double)candidate.getDistanceToEntity(EntityAIFindEntityNearest.this.mob) > followRange ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearest.this.mob, candidate, false, true));
            }
        };
        this.sorter = new EntityAINearestAttackableTarget.Sorter(mobIn);
    }

    public boolean shouldExecute()
    {
        double followRange = this.getFollowRange();
        List<EntityLivingBase> candidates = this.mob.worldObj.<EntityLivingBase>getEntitiesWithinAABB(this.targetClass, this.mob.getEntityBoundingBox().expand(followRange, 4.0D, followRange), this.targetEntitySelector);
        Collections.sort(candidates, this.sorter);

        if (candidates.isEmpty())
        {
            return false;
        }
        else
        {
            this.target = (EntityLivingBase)candidates.get(0);
            return true;
        }
    }

    public boolean continueExecuting()
    {
        EntityLivingBase attackTarget = this.mob.getAttackTarget();

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
            double followRange = this.getFollowRange();
            return this.mob.getDistanceSqToEntity(attackTarget) > followRange * followRange ? false : !(attackTarget instanceof EntityPlayerMP) || !((EntityPlayerMP)attackTarget).theItemInWorldManager.isCreative();
        }
    }

    public void startExecuting()
    {
        this.mob.setAttackTarget(this.target);
        super.startExecuting();
    }

    public void resetTask()
    {
        this.mob.setAttackTarget((EntityLivingBase)null);
        super.startExecuting();
    }

    protected double getFollowRange()
    {
        IAttributeInstance followRangeAttribute = this.mob.getEntityAttribute(SharedMonsterAttributes.followRange);
        return followRangeAttribute == null ? 16.0D : followRangeAttribute.getAttributeValue();
    }
}
