package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Vec3;

public class EntityAIAvoidEntity<T extends Entity> extends EntityAIBase
{
    private final Predicate<Entity> canBeSeenSelector;
    protected EntityCreature theEntity;
    private double farSpeed;
    private double nearSpeed;
    protected T closestLivingEntity;
    private float avoidDistance;
    private PathEntity entityPathEntity;
    private PathNavigate entityPathNavigate;
    private Class<T> classToAvoid;
    private Predicate <? super T > avoidTargetSelector;

    public EntityAIAvoidEntity(EntityCreature theEntityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
    {
        this(theEntityIn, classToAvoidIn, Predicates.<T>alwaysTrue(), avoidDistanceIn, farSpeedIn, nearSpeedIn);
    }

    public EntityAIAvoidEntity(EntityCreature theEntityIn, Class<T> classToAvoidIn, Predicate <? super T > avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
    {
        this.canBeSeenSelector = new Predicate<Entity>()
        {
            public boolean apply(Entity entity)
            {
                return entity.isEntityAlive() && EntityAIAvoidEntity.this.theEntity.getEntitySenses().canSee(entity);
            }
        };
        this.theEntity = theEntityIn;
        this.classToAvoid = classToAvoidIn;
        this.avoidTargetSelector = avoidTargetSelectorIn;
        this.avoidDistance = avoidDistanceIn;
        this.farSpeed = farSpeedIn;
        this.nearSpeed = nearSpeedIn;
        this.entityPathNavigate = theEntityIn.getNavigator();
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        List<T> list = this.theEntity.worldObj.<T>getEntitiesWithinAABB(this.classToAvoid, this.theEntity.getEntityBoundingBox().expand((double)this.avoidDistance, 3.0D, (double)this.avoidDistance), Predicates.and(new Predicate[] {EntitySelectors.NOT_SPECTATING, this.canBeSeenSelector, this.avoidTargetSelector}));

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            this.closestLivingEntity = list.get(0);
            Vec3 avoidPos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theEntity, 16, 7, new Vec3(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));

            if (avoidPos == null)
            {
                return false;
            }
            else if (this.closestLivingEntity.getDistanceSq(avoidPos.xCoord, avoidPos.yCoord, avoidPos.zCoord) < this.closestLivingEntity.getDistanceSqToEntity(this.theEntity))
            {
                return false;
            }
            else
            {
                this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(avoidPos.xCoord, avoidPos.yCoord, avoidPos.zCoord);
                return this.entityPathEntity == null ? false : this.entityPathEntity.isDestinationSame(avoidPos);
            }
        }
    }

    public boolean continueExecuting()
    {
        return !this.entityPathNavigate.noPath();
    }

    public void startExecuting()
    {
        this.entityPathNavigate.setPath(this.entityPathEntity, this.farSpeed);
    }

    public void resetTask()
    {
        this.closestLivingEntity = null;
    }

    public void updateTask()
    {
        if (this.theEntity.getDistanceSqToEntity(this.closestLivingEntity) < 49.0D)
        {
            this.theEntity.getNavigator().setSpeed(this.nearSpeed);
        }
        else
        {
            this.theEntity.getNavigator().setSpeed(this.farSpeed);
        }
    }
}
