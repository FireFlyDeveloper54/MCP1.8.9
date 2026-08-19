package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAIFindEntityNearestPlayer extends EntityAIBase
{
    private static final Logger LOGGER = LogManager.getLogger();
    private EntityLiving entityLiving;
    private final Predicate<Entity> predicate;
    private final EntityAINearestAttackableTarget.Sorter sorter;
    private EntityLivingBase entityTarget;

    public EntityAIFindEntityNearestPlayer(EntityLiving entityLivingIn)
    {
        this.entityLiving = entityLivingIn;

        if (entityLivingIn instanceof EntityCreature)
        {
            LOGGER.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
        }

        this.predicate = new Predicate<Entity>()
        {
            public boolean apply(Entity candidate)
            {
                if (!(candidate instanceof EntityPlayer))
                {
                    return false;
                }
                else if (((EntityPlayer)candidate).capabilities.disableDamage)
                {
                    return false;
                }
                else
                {
                    double maxTargetRange = EntityAIFindEntityNearestPlayer.this.maxTargetRange();

                    if (candidate.isSneaking())
                    {
                        maxTargetRange *= 0.800000011920929D;
                    }

                    if (candidate.isInvisible())
                    {
                        float armorVisibility = ((EntityPlayer)candidate).getArmorVisibility();

                        if (armorVisibility < 0.1F)
                        {
                            armorVisibility = 0.1F;
                        }

                        maxTargetRange *= (double)(0.7F * armorVisibility);
                    }

                    return (double)candidate.getDistanceToEntity(EntityAIFindEntityNearestPlayer.this.entityLiving) > maxTargetRange ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearestPlayer.this.entityLiving, (EntityLivingBase)candidate, false, true);
                }
            }
        };
        this.sorter = new EntityAINearestAttackableTarget.Sorter(entityLivingIn);
    }

    public boolean shouldExecute()
    {
        double targetRange = this.maxTargetRange();
        List<EntityPlayer> list = this.entityLiving.worldObj.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.entityLiving.getEntityBoundingBox().expand(targetRange, 4.0D, targetRange), this.predicate);
        Collections.sort(list, this.sorter);

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            this.entityTarget = (EntityLivingBase)list.get(0);
            return true;
        }
    }

    public boolean continueExecuting()
    {
        EntityLivingBase entityLivingBase = this.entityLiving.getAttackTarget();

        if (entityLivingBase == null)
        {
            return false;
        }
        else if (!entityLivingBase.isEntityAlive())
        {
            return false;
        }
        else if (entityLivingBase instanceof EntityPlayer && ((EntityPlayer)entityLivingBase).capabilities.disableDamage)
        {
            return false;
        }
        else
        {
            Team team = this.entityLiving.getTeam();
            Team team1 = entityLivingBase.getTeam();

            if (team != null && team1 == team)
            {
                return false;
            }
            else
            {
                double targetRange = this.maxTargetRange();
                return this.entityLiving.getDistanceSqToEntity(entityLivingBase) > targetRange * targetRange ? false : !(entityLivingBase instanceof EntityPlayerMP) || !((EntityPlayerMP)entityLivingBase).theItemInWorldManager.isCreative();
            }
        }
    }

    public void startExecuting()
    {
        this.entityLiving.setAttackTarget(this.entityTarget);
        super.startExecuting();
    }

    public void resetTask()
    {
        this.entityLiving.setAttackTarget((EntityLivingBase)null);
        super.startExecuting();
    }

    protected double maxTargetRange()
    {
        IAttributeInstance iattributeinstance = this.entityLiving.getEntityAttribute(SharedMonsterAttributes.followRange);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }
}
