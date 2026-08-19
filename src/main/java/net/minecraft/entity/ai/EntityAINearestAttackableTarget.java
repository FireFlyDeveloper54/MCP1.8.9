package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class EntityAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAITarget
{
    protected final Class<T> targetClass;
    private final int targetChance;
    protected final EntityAINearestAttackableTarget.Sorter nearestTargetSorter;
    protected Predicate <? super T > targetEntitySelector;
    protected EntityLivingBase targetEntity;

    public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight)
    {
        this(creature, classTarget, checkSight, false);
    }

    public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby)
    {
        this(creature, classTarget, 10, checkSight, onlyNearby, (Predicate <? super T >)null);
    }

    public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, final Predicate <? super T > targetSelector)
    {
        super(creature, checkSight, onlyNearby);
        this.targetClass = classTarget;
        this.targetChance = chance;
        this.nearestTargetSorter = new EntityAINearestAttackableTarget.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>()
        {
            public boolean apply(T candidate)
            {
                if (targetSelector != null && !targetSelector.apply(candidate))
                {
                    return false;
                }
                else
                {
                    if (candidate instanceof EntityPlayer)
                    {
                        double targetDistance = EntityAINearestAttackableTarget.this.getTargetDistance();

                        if (candidate.isSneaking())
                        {
                            targetDistance *= 0.800000011920929D;
                        }

                        if (candidate.isInvisible())
                        {
                            float armorVisibility = ((EntityPlayer)candidate).getArmorVisibility();

                            if (armorVisibility < 0.1F)
                            {
                                armorVisibility = 0.1F;
                            }

                            targetDistance *= (double)(0.7F * armorVisibility);
                        }

                        if ((double)candidate.getDistanceToEntity(EntityAINearestAttackableTarget.this.taskOwner) > targetDistance)
                        {
                            return false;
                        }
                    }

                    return EntityAINearestAttackableTarget.this.isSuitableTarget(candidate, false);
                }
            }
        };
    }

    public boolean shouldExecute()
    {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0)
        {
            return false;
        }
        else
        {
            double targetDistance = this.getTargetDistance();
            List<T> list = this.taskOwner.worldObj.<T>getEntitiesWithinAABB(this.targetClass, this.taskOwner.getEntityBoundingBox().expand(targetDistance, 4.0D, targetDistance), Predicates.<T> and (this.targetEntitySelector, EntitySelectors.NOT_SPECTATING));
            Collections.sort(list, this.nearestTargetSorter);

            if (list.isEmpty())
            {
                return false;
            }
            else
            {
                this.targetEntity = (EntityLivingBase)list.get(0);
                return true;
            }
        }
    }

    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    public static class Sorter implements Comparator<Entity>
    {
        private final Entity theEntity;

        public Sorter(Entity theEntityIn)
        {
            this.theEntity = theEntityIn;
        }

        public int compare(Entity first, Entity second)
        {
            double firstDistanceSq = this.theEntity.getDistanceSqToEntity(first);
            double secondDistanceSq = this.theEntity.getDistanceSqToEntity(second);
            return firstDistanceSq < secondDistanceSq ? -1 : (firstDistanceSq > secondDistanceSq ? 1 : 0);
        }
    }
}
