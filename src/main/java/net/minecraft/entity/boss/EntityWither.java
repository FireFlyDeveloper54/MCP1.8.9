package net.minecraft.entity.boss;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityWither extends EntityMob implements IBossDisplayData, IRangedAttackMob
{
    private float[] headXRotations = new float[2];
    private float[] headYRotations = new float[2];
    private float[] prevHeadXRotations = new float[2];
    private float[] prevHeadYRotations = new float[2];
    private int[] nextHeadUpdateTicks = new int[2];
    private int[] idleHeadUpdates = new int[2];
    private int blockBreakCounter;
    private static final Predicate<Entity> attackEntitySelector = new Predicate<Entity>()
    {
        public boolean apply(Entity entity)
        {
            return entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getCreatureAttribute() != EnumCreatureAttribute.UNDEAD;
        }
    };

    public EntityWither(World worldIn)
    {
        super(worldIn);
        this.setHealth(this.getMaxHealth());
        this.setSize(0.9F, 3.5F);
        this.isImmuneToFire = true;
        ((PathNavigateGround)this.getNavigator()).setCanSwim(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIArrowAttack(this, 1.0D, 40, 20.0F));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, false, attackEntitySelector));
        this.experienceValue = 50;
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(17, Integer.valueOf(0));
        this.dataWatcher.addObject(18, Integer.valueOf(0));
        this.dataWatcher.addObject(19, Integer.valueOf(0));
        this.dataWatcher.addObject(20, Integer.valueOf(0));
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Invul", this.getInvulTime());
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        this.setInvulTime(tagCompund.getInteger("Invul"));
    }

    protected String getLivingSound()
    {
        return "mob.wither.idle";
    }

    protected String getHurtSound()
    {
        return "mob.wither.hurt";
    }

    protected String getDeathSound()
    {
        return "mob.wither.death";
    }

    public void onLivingUpdate()
    {
        this.motionY *= 0.6000000238418579D;

        if (!this.worldObj.isRemote && this.getWatchedTargetId(0) > 0)
        {
            Entity entity = this.worldObj.getEntityByID(this.getWatchedTargetId(0));

            if (entity != null)
            {
                if (this.posY < entity.posY || !this.isArmored() && this.posY < entity.posY + 5.0D)
                {
                    if (this.motionY < 0.0D)
                    {
                        this.motionY = 0.0D;
                    }

                    this.motionY += (0.5D - this.motionY) * 0.6000000238418579D;
                }

                double secondDoubleValue = entity.posX - this.posX;
                double zCoordinate = entity.posZ - this.posZ;
                double doubleValue = secondDoubleValue * secondDoubleValue + zCoordinate * zCoordinate;

                if (doubleValue > 9.0D)
                {
                    double doubleValue2 = (double)MathHelper.sqrt_double(doubleValue);
                    this.motionX += (secondDoubleValue / doubleValue2 * 0.5D - this.motionX) * 0.6000000238418579D;
                    this.motionZ += (zCoordinate / doubleValue2 * 0.5D - this.motionZ) * 0.6000000238418579D;
                }
            }
        }

        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.05000000074505806D)
        {
            this.rotationYaw = (float)MathHelper.atan2(this.motionZ, this.motionX) * (180F / (float)Math.PI) - 90.0F;
        }

        super.onLivingUpdate();

        for (int i = 0; i < 2; ++i)
        {
            this.prevHeadYRotations[i] = this.headYRotations[i];
            this.prevHeadXRotations[i] = this.headXRotations[i];
        }

        for (int j = 0; j < 2; ++j)
        {
            int k = this.getWatchedTargetId(j + 1);
            Entity entity1 = null;

            if (k > 0)
            {
                entity1 = this.worldObj.getEntityByID(k);
            }

            if (entity1 != null)
            {
                double doubleValue3 = this.getHeadX(j + 1);
                double doubleValue4 = this.getHeadY(j + 1);
                double doubleValue5 = this.getHeadZ(j + 1);
                double xCoordinate2 = entity1.posX - doubleValue3;
                double yCoordinate = entity1.posY + (double)entity1.getEyeHeight() - doubleValue4;
                double zCoordinate2 = entity1.posZ - doubleValue5;
                double doubleValue6 = (double)MathHelper.sqrt_double(xCoordinate2 * xCoordinate2 + zCoordinate2 * zCoordinate2);
                float f = (float)(MathHelper.atan2(zCoordinate2, xCoordinate2) * 180.0D / Math.PI) - 90.0F;
                float floatValue2 = (float)(-(MathHelper.atan2(yCoordinate, doubleValue6) * 180.0D / Math.PI));
                this.headXRotations[j] = this.rotateTowards(this.headXRotations[j], floatValue2, 40.0F);
                this.headYRotations[j] = this.rotateTowards(this.headYRotations[j], f, 10.0F);
            }
            else
            {
                this.headYRotations[j] = this.rotateTowards(this.headYRotations[j], this.renderYawOffset, 10.0F);
            }
        }

        boolean flag = this.isArmored();

        for (int l = 0; l < 3; ++l)
        {
            double doubleValue7 = this.getHeadX(l);
            double doubleValue8 = this.getHeadY(l);
            double doubleValue9 = this.getHeadZ(l);
            this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, doubleValue7 + this.rand.nextGaussian() * 0.30000001192092896D, doubleValue8 + this.rand.nextGaussian() * 0.30000001192092896D, doubleValue9 + this.rand.nextGaussian() * 0.30000001192092896D, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);

            if (flag && this.worldObj.rand.nextInt(4) == 0)
            {
                this.worldObj.spawnParticle(EnumParticleTypes.SPELL_MOB, doubleValue7 + this.rand.nextGaussian() * 0.30000001192092896D, doubleValue8 + this.rand.nextGaussian() * 0.30000001192092896D, doubleValue9 + this.rand.nextGaussian() * 0.30000001192092896D, 0.699999988079071D, 0.699999988079071D, 0.5D, EnumParticleTypes.EMPTY_ARGS);
            }
        }

        if (this.getInvulTime() > 0)
        {
            for (int index2 = 0; index2 < 3; ++index2)
            {
                this.worldObj.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + this.rand.nextGaussian() * 1.0D, this.posY + (double)(this.rand.nextFloat() * 3.3F), this.posZ + this.rand.nextGaussian() * 1.0D, 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D, EnumParticleTypes.EMPTY_ARGS);
            }
        }
    }

    protected void updateAITasks()
    {
        if (this.getInvulTime() > 0)
        {
            int fourthIntValue = this.getInvulTime() - 1;

            if (fourthIntValue <= 0)
            {
                this.worldObj.newExplosion(this, this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, 7.0F, false, this.worldObj.getGameRules().getBoolean("mobGriefing"));
                this.worldObj.playBroadcastSound(1013, new BlockPos(this), 0);
            }

            this.setInvulTime(fourthIntValue);

            if (this.ticksExisted % 10 == 0)
            {
                this.heal(10.0F);
            }
        }
        else
        {
            super.updateAITasks();

            for (int i = 1; i < 3; ++i)
            {
                if (this.ticksExisted >= this.nextHeadUpdateTicks[i - 1])
                {
                    this.nextHeadUpdateTicks[i - 1] = this.ticksExisted + 10 + this.rand.nextInt(10);

                    if (this.worldObj.getDifficulty() == EnumDifficulty.NORMAL || this.worldObj.getDifficulty() == EnumDifficulty.HARD)
                    {
                        int sixthIntValue = i - 1;
                        int ninthIntValue = this.idleHeadUpdates[i - 1];
                        this.idleHeadUpdates[sixthIntValue] = this.idleHeadUpdates[i - 1] + 1;

                        if (ninthIntValue > 15)
                        {
                            float f = 10.0F;
                            float floatValue = 5.0F;
                            double thirdDoubleValue = MathHelper.getRandomDoubleInRange(this.rand, this.posX - (double)f, this.posX + (double)f);
                            double fourthDoubleValue = MathHelper.getRandomDoubleInRange(this.rand, this.posY - (double)floatValue, this.posY + (double)floatValue);
                            double fifthDoubleValue = MathHelper.getRandomDoubleInRange(this.rand, this.posZ - (double)f, this.posZ + (double)f);
                            this.launchWitherSkullToCoords(i + 1, thirdDoubleValue, fourthDoubleValue, fifthDoubleValue, true);
                            this.idleHeadUpdates[i - 1] = 0;
                        }
                    }

                    int seventhIntValue = this.getWatchedTargetId(i);

                    if (seventhIntValue > 0)
                    {
                        Entity entity = this.worldObj.getEntityByID(seventhIntValue);

                        if (entity != null && entity.isEntityAlive() && this.getDistanceSqToEntity(entity) <= 900.0D && this.canEntityBeSeen(entity))
                        {
                            if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.disableDamage)
                            {
                                this.updateWatchedTargetId(i, 0);
                            }
                            else
                            {
                                this.launchWitherSkullToEntity(i + 1, (EntityLivingBase)entity);
                                this.nextHeadUpdateTicks[i - 1] = this.ticksExisted + 40 + this.rand.nextInt(20);
                                this.idleHeadUpdates[i - 1] = 0;
                            }
                        }
                        else
                        {
                            this.updateWatchedTargetId(i, 0);
                        }
                    }
                    else
                    {
                        List<EntityLivingBase> list = this.worldObj.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(20.0D, 8.0D, 20.0D), Predicates.<EntityLivingBase> and (attackEntitySelector, EntitySelectors.NOT_SPECTATING));

                        for (int fifthIntValue = 0; fifthIntValue < 10 && !list.isEmpty(); ++fifthIntValue)
                        {
                            EntityLivingBase entitylivingbase = (EntityLivingBase)list.get(this.rand.nextInt(list.size()));

                            if (entitylivingbase != this && entitylivingbase.isEntityAlive() && this.canEntityBeSeen(entitylivingbase))
                            {
                                if (entitylivingbase instanceof EntityPlayer)
                                {
                                    if (!((EntityPlayer)entitylivingbase).capabilities.disableDamage)
                                    {
                                        this.updateWatchedTargetId(i, entitylivingbase.getEntityId());
                                    }
                                }
                                else
                                {
                                    this.updateWatchedTargetId(i, entitylivingbase.getEntityId());
                                }

                                break;
                            }

                            list.remove(entitylivingbase);
                        }
                    }
                }
            }

            if (this.getAttackTarget() != null)
            {
                this.updateWatchedTargetId(0, this.getAttackTarget().getEntityId());
            }
            else
            {
                this.updateWatchedTargetId(0, 0);
            }

            if (this.blockBreakCounter > 0)
            {
                --this.blockBreakCounter;

                if (this.blockBreakCounter == 0 && this.worldObj.getGameRules().getBoolean("mobGriefing"))
                {
                    int intValue = MathHelper.floor_double(this.posY);
                    int tenthIntValue = MathHelper.floor_double(this.posX);
                    int secondIntValue = MathHelper.floor_double(this.posZ);
                    boolean flag = false;

                    for (int eighthIntValue = -1; eighthIntValue <= 1; ++eighthIntValue)
                    {
                        for (int eleventhIntValue = -1; eleventhIntValue <= 1; ++eleventhIntValue)
                        {
                            for (int j = 0; j <= 3; ++j)
                            {
                                int thirdIntValue = tenthIntValue + eighthIntValue;
                                int k = intValue + j;
                                int l = secondIntValue + eleventhIntValue;
                                BlockPos blockpos = new BlockPos(thirdIntValue, k, l);
                                Block block = this.worldObj.getBlockState(blockpos).getBlock();

                                if (block.getMaterial() != Material.air && canDestroyBlock(block))
                                {
                                    flag = this.worldObj.destroyBlock(blockpos, true) || flag;
                                }
                            }
                        }
                    }

                    if (flag)
                    {
                        this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1012, new BlockPos(this), 0);
                    }
                }
            }

            if (this.ticksExisted % 20 == 0)
            {
                this.heal(1.0F);
            }
        }
    }

    public static boolean canDestroyBlock(Block block)
    {
        return block != Blocks.bedrock && block != Blocks.end_portal && block != Blocks.end_portal_frame && block != Blocks.command_block && block != Blocks.barrier;
    }

    public void ignite()
    {
        this.setInvulTime(220);
        this.setHealth(this.getMaxHealth() / 3.0F);
    }

    public void setInWeb()
    {
    }

    public int getTotalArmorValue()
    {
        return 4;
    }

    private double getHeadX(int headIndex)
    {
        if (headIndex <= 0)
        {
            return this.posX;
        }
        else
        {
            float f = (this.renderYawOffset + (float)(180 * (headIndex - 1))) / 180.0F * (float)Math.PI;
            float floatValue2 = MathHelper.cos(f);
            return this.posX + (double)floatValue2 * 1.3D;
        }
    }

    private double getHeadY(int headIndex)
    {
        return headIndex <= 0 ? this.posY + 3.0D : this.posY + 2.2D;
    }

    private double getHeadZ(int headIndex)
    {
        if (headIndex <= 0)
        {
            return this.posZ;
        }
        else
        {
            float f = (this.renderYawOffset + (float)(180 * (headIndex - 1))) / 180.0F * (float)Math.PI;
            float floatValue2 = MathHelper.sin(f);
            return this.posZ + (double)floatValue2 * 1.3D;
        }
    }

    private float rotateTowards(float current, float target, float maxChange)
    {
        float f = MathHelper.wrapAngleTo180_float(target - current);

        if (f > maxChange)
        {
            f = maxChange;
        }

        if (f < -maxChange)
        {
            f = -maxChange;
        }

        return current + f;
    }

    private void launchWitherSkullToEntity(int headIndex, EntityLivingBase target)
    {
        this.launchWitherSkullToCoords(headIndex, target.posX, target.posY + (double)target.getEyeHeight() * 0.5D, target.posZ, headIndex == 0 && this.rand.nextFloat() < 0.001F);
    }

    private void launchWitherSkullToCoords(int headIndex, double x, double y, double z, boolean invulnerable)
    {
        this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1014, new BlockPos(this), 0);
        double doubleValue = this.getHeadX(headIndex);
        double doubleValue2 = this.getHeadY(headIndex);
        double doubleValue3 = this.getHeadZ(headIndex);
        double doubleValue4 = x - doubleValue;
        double doubleValue5 = y - doubleValue2;
        double doubleValue6 = z - doubleValue3;
        EntityWitherSkull entityWitherSkull = new EntityWitherSkull(this.worldObj, this, doubleValue4, doubleValue5, doubleValue6);

        if (invulnerable)
        {
            entityWitherSkull.setInvulnerable(true);
        }

        entityWitherSkull.posY = doubleValue2;
        entityWitherSkull.posX = doubleValue;
        entityWitherSkull.posZ = doubleValue3;
        this.worldObj.spawnEntityInWorld(entityWitherSkull);
    }

    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        this.launchWitherSkullToEntity(0, target);
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (source != DamageSource.drown && !(source.getEntity() instanceof EntityWither))
        {
            if (this.getInvulTime() > 0 && source != DamageSource.outOfWorld)
            {
                return false;
            }
            else
            {
                if (this.isArmored())
                {
                    Entity entity = source.getSourceOfDamage();

                    if (entity instanceof EntityArrow)
                    {
                        return false;
                    }
                }

                Entity entity1 = source.getEntity();

                if (entity1 != null && !(entity1 instanceof EntityPlayer) && entity1 instanceof EntityLivingBase && ((EntityLivingBase)entity1).getCreatureAttribute() == this.getCreatureAttribute())
                {
                    return false;
                }
                else
                {
                    if (this.blockBreakCounter <= 0)
                    {
                        this.blockBreakCounter = 20;
                    }

                    for (int i = 0; i < this.idleHeadUpdates.length; ++i)
                    {
                        this.idleHeadUpdates[i] += 3;
                    }

                    return super.attackEntityFrom(source, amount);
                }
            }
        }
        else
        {
            return false;
        }
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        EntityItem entityItem = this.dropItem(Items.nether_star, 1);

        if (entityItem != null)
        {
            entityItem.setNoDespawn();
        }

        if (!this.worldObj.isRemote)
        {
            for (EntityPlayer entityPlayer : this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().expand(50.0D, 100.0D, 50.0D)))
            {
                entityPlayer.triggerAchievement(AchievementList.killWither);
            }
        }
    }

    protected void despawnEntity()
    {
        this.entityAge = 0;
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    public void addPotionEffect(PotionEffect potioneffectIn)
    {
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(300.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6000000238418579D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
    }

    public float getHeadYRotation(int headIndex)
    {
        return this.headYRotations[headIndex];
    }

    public float getHeadXRotation(int headIndex)
    {
        return this.headXRotations[headIndex];
    }

    public int getInvulTime()
    {
        return this.dataWatcher.getWatchableObjectInt(20);
    }

    public void setInvulTime(int invulTime)
    {
        this.dataWatcher.updateObject(20, Integer.valueOf(invulTime));
    }

    public int getWatchedTargetId(int targetOffset)
    {
        return this.dataWatcher.getWatchableObjectInt(17 + targetOffset);
    }

    public void updateWatchedTargetId(int targetOffset, int newId)
    {
        this.dataWatcher.updateObject(17 + targetOffset, Integer.valueOf(newId));
    }

    public boolean isArmored()
    {
        return this.getHealth() <= this.getMaxHealth() / 2.0F;
    }

    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
    }

    public void mountEntity(Entity entityIn)
    {
        this.ridingEntity = null;
    }
}
