package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityGuardian extends EntityMob
{
    private float tailAnimation;
    private float tailAnimationO;
    private float tailAnimationSpeed;
    private float spikesAnimation;
    private float spikesAnimationO;
    private EntityLivingBase targetedEntity;
    private int clientSideAttackTime;
    private boolean wasOnGround;
    private EntityAIWander wander;
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();

    public EntityGuardian(World worldIn)
    {
        super(worldIn);
        this.experienceValue = 10;
        this.setSize(0.85F, 0.85F);
        this.tasks.addTask(4, new EntityGuardian.AIGuardianAttack(this));
        EntityAIMoveTowardsRestriction entityAIMoveTowardsRestriction;
        this.tasks.addTask(5, entityAIMoveTowardsRestriction = new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, this.wander = new EntityAIWander(this, 1.0D, 80));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityGuardian.class, 12.0F, 0.01F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.wander.setMutexBits(3);
        entityAIMoveTowardsRestriction.setMutexBits(3);
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 10, true, false, new EntityGuardian.GuardianTargetSelector(this)));
        this.moveHelper = new EntityGuardian.GuardianMoveHelper(this);
        this.tailAnimationO = this.tailAnimation = this.rand.nextFloat();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        this.setElder(tagCompund.getBoolean("Elder"));
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("Elder", this.isElder());
    }

    protected PathNavigate getNewNavigator(World worldIn)
    {
        return new PathNavigateSwimmer(this, worldIn);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Integer.valueOf(0));
        this.dataWatcher.addObject(17, Integer.valueOf(0));
    }

    private boolean isSyncedFlagSet(int flagId)
    {
        return (this.dataWatcher.getWatchableObjectInt(16) & flagId) != 0;
    }

    private void setSyncedFlag(int flagId, boolean state)
    {
        int i = this.dataWatcher.getWatchableObjectInt(16);

        if (state)
        {
            this.dataWatcher.updateObject(16, Integer.valueOf(i | flagId));
        }
        else
        {
            this.dataWatcher.updateObject(16, Integer.valueOf(i & ~flagId));
        }
    }

    public boolean isMoving()
    {
        return this.isSyncedFlagSet(2);
    }

    private void setMoving(boolean moving)
    {
        this.setSyncedFlag(2, moving);
    }

    public int getAttackDuration()
    {
        return this.isElder() ? 60 : 80;
    }

    public boolean isElder()
    {
        return this.isSyncedFlagSet(4);
    }

    public void setElder(boolean elder)
    {
        this.setSyncedFlag(4, elder);

        if (elder)
        {
            this.setSize(1.9975F, 1.9975F);
            this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
            this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(8.0D);
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(80.0D);
            this.enablePersistence();
            this.wander.setExecutionChance(400);
        }
    }

    public void setElder()
    {
        this.setElder(true);
        this.spikesAnimationO = this.spikesAnimation = 1.0F;
    }

    private void setTargetedEntity(int entityId)
    {
        this.dataWatcher.updateObject(17, Integer.valueOf(entityId));
    }

    public boolean hasTargetedEntity()
    {
        return this.dataWatcher.getWatchableObjectInt(17) != 0;
    }

    public EntityLivingBase getTargetedEntity()
    {
        if (!this.hasTargetedEntity())
        {
            return null;
        }
        else if (this.worldObj.isRemote)
        {
            if (this.targetedEntity != null)
            {
                return this.targetedEntity;
            }
            else
            {
                Entity entity = this.worldObj.getEntityByID(this.dataWatcher.getWatchableObjectInt(17));

                if (entity instanceof EntityLivingBase)
                {
                    this.targetedEntity = (EntityLivingBase)entity;
                    return this.targetedEntity;
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            return this.getAttackTarget();
        }
    }

    public void onDataWatcherUpdate(int dataID)
    {
        super.onDataWatcherUpdate(dataID);

        if (dataID == 16)
        {
            if (this.isElder() && this.width < 1.0F)
            {
                this.setSize(1.9975F, 1.9975F);
            }
        }
        else if (dataID == 17)
        {
            this.clientSideAttackTime = 0;
            this.targetedEntity = null;
        }
    }

    public int getTalkInterval()
    {
        return 160;
    }

    protected String getLivingSound()
    {
        return !this.isInWater() ? "mob.guardian.land.idle" : (this.isElder() ? "mob.guardian.elder.idle" : "mob.guardian.idle");
    }

    protected String getHurtSound()
    {
        return !this.isInWater() ? "mob.guardian.land.hit" : (this.isElder() ? "mob.guardian.elder.hit" : "mob.guardian.hit");
    }

    protected String getDeathSound()
    {
        return !this.isInWater() ? "mob.guardian.land.death" : (this.isElder() ? "mob.guardian.elder.death" : "mob.guardian.death");
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    public float getEyeHeight()
    {
        return this.height * 0.5F;
    }

    public float getBlockPathWeight(BlockPos pos)
    {
        return this.worldObj.getBlockState(pos).getBlock().getMaterial() == Material.water ? 10.0F + this.worldObj.getLightBrightness(pos) - 0.5F : super.getBlockPathWeight(pos);
    }

    public void onLivingUpdate()
    {
        if (this.worldObj.isRemote)
        {
            this.tailAnimationO = this.tailAnimation;

            if (!this.isInWater())
            {
                this.tailAnimationSpeed = 2.0F;

                if (this.motionY > 0.0D && this.wasOnGround && !this.isSilent())
                {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ, "mob.guardian.flop", 1.0F, 1.0F, false);
                }

                this.wasOnGround = this.motionY < 0.0D && this.worldObj.isBlockNormalCube(this.blockSamplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) - 1, MathHelper.floor_double(this.posZ)), false);
            }
            else if (this.isMoving())
            {
                if (this.tailAnimationSpeed < 0.5F)
                {
                    this.tailAnimationSpeed = 4.0F;
                }
                else
                {
                    this.tailAnimationSpeed += (0.5F - this.tailAnimationSpeed) * 0.1F;
                }
            }
            else
            {
                this.tailAnimationSpeed += (0.125F - this.tailAnimationSpeed) * 0.2F;
            }

            this.tailAnimation += this.tailAnimationSpeed;
            this.spikesAnimationO = this.spikesAnimation;

            if (!this.isInWater())
            {
                this.spikesAnimation = this.rand.nextFloat();
            }
            else if (this.isMoving())
            {
                this.spikesAnimation += (0.0F - this.spikesAnimation) * 0.25F;
            }
            else
            {
                this.spikesAnimation += (1.0F - this.spikesAnimation) * 0.06F;
            }

            if (this.isMoving() && this.isInWater())
            {
                Vec3 vec3 = this.getLook(0.0F);

                for (int i = 0; i < 2; ++i)
                {
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width - vec3.xCoord * 1.5D, this.posY + this.rand.nextDouble() * (double)this.height - vec3.yCoord * 1.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width - vec3.zCoord * 1.5D, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }
            }

            if (this.hasTargetedEntity())
            {
                if (this.clientSideAttackTime < this.getAttackDuration())
                {
                    ++this.clientSideAttackTime;
                }

                EntityLivingBase entitylivingbase = this.getTargetedEntity();

                if (entitylivingbase != null)
                {
                    this.getLookHelper().setLookPositionWithEntity(entitylivingbase, 90.0F, 90.0F);
                    this.getLookHelper().onUpdateLook();
                    double seventhDoubleValue = (double)this.getAttackAnimationScale(0.0F);
                    double secondDoubleValue = entitylivingbase.posX - this.posX;
                    double thirdDoubleValue = entitylivingbase.posY + (double)(entitylivingbase.height * 0.5F) - (this.posY + (double)this.getEyeHeight());
                    double fourthDoubleValue = entitylivingbase.posZ - this.posZ;
                    double fifthDoubleValue = MathHelper.length_double(secondDoubleValue, thirdDoubleValue, fourthDoubleValue);
                    secondDoubleValue = secondDoubleValue / fifthDoubleValue;
                    thirdDoubleValue = thirdDoubleValue / fifthDoubleValue;
                    fourthDoubleValue = fourthDoubleValue / fifthDoubleValue;
                    double sixthDoubleValue = this.rand.nextDouble();

                    while (sixthDoubleValue < fifthDoubleValue)
                    {
                        sixthDoubleValue += 1.8D - seventhDoubleValue + this.rand.nextDouble() * (1.7D - seventhDoubleValue);
                        this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + secondDoubleValue * sixthDoubleValue, this.posY + thirdDoubleValue * sixthDoubleValue + (double)this.getEyeHeight(), this.posZ + fourthDoubleValue * sixthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                    }
                }
            }
        }

        if (this.inWater)
        {
            this.setAir(300);
        }
        else if (this.onGround)
        {
            this.motionY += 0.5D;
            this.motionX += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.4F);
            this.motionZ += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.4F);
            this.rotationYaw = this.rand.nextFloat() * 360.0F;
            this.onGround = false;
            this.isAirBorne = true;
        }

        if (this.hasTargetedEntity())
        {
            this.rotationYaw = this.rotationYawHead;
        }

        super.onLivingUpdate();
    }

    public float getTailAnimation(float partialTicks)
    {
        return this.tailAnimationO + (this.tailAnimation - this.tailAnimationO) * partialTicks;
    }

    public float getSpikesAnimation(float partialTicks)
    {
        return this.spikesAnimationO + (this.spikesAnimation - this.spikesAnimationO) * partialTicks;
    }

    public float getAttackAnimationScale(float partialTicks)
    {
        return ((float)this.clientSideAttackTime + partialTicks) / (float)this.getAttackDuration();
    }

    protected void updateAITasks()
    {
        super.updateAITasks();

        if (this.isElder())
        {
            int i = 1200;
            int j = 1200;
            int k = 6000;
            int l = 2;

            if ((this.ticksExisted + this.getEntityId()) % 1200 == 0)
            {
                Potion potion = Potion.digSlowdown;

                for (EntityPlayerMP entityPlayerMP : this.worldObj.getPlayers(EntityPlayerMP.class, new Predicate<EntityPlayerMP>()
                {
                    public boolean apply(EntityPlayerMP player)
                    {
                        return EntityGuardian.this.getDistanceSqToEntity(player) < 2500.0D && player.theItemInWorldManager.survivalOrAdventure();
                    }
                }))
                {
                    if (!entityPlayerMP.isPotionActive(potion) || entityPlayerMP.getActivePotionEffect(potion).getAmplifier() < 2 || entityPlayerMP.getActivePotionEffect(potion).getDuration() < 1200)
                    {
                        entityPlayerMP.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(10, 0.0F));
                        entityPlayerMP.addPotionEffect(new PotionEffect(potion.id, 6000, 2));
                    }
                }
            }

            if (!this.hasHome())
            {
                this.setHomePosAndDistance(new BlockPos(this), 16);
            }
        }
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        int i = this.rand.nextInt(3) + this.rand.nextInt(lootingModifier + 1);

        if (i > 0)
        {
            this.entityDropItem(new ItemStack(Items.prismarine_shard, i, 0), 1.0F);
        }

        if (this.rand.nextInt(3 + lootingModifier) > 1)
        {
            this.entityDropItem(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.getMetadata()), 1.0F);
        }
        else if (this.rand.nextInt(3 + lootingModifier) > 1)
        {
            this.entityDropItem(new ItemStack(Items.prismarine_crystals, 1, 0), 1.0F);
        }

        if (wasRecentlyHit && this.isElder())
        {
            this.entityDropItem(new ItemStack(Blocks.sponge, 1, 1), 1.0F);
        }
    }

    protected void addRandomDrop()
    {
        ItemStack itemStack = ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, EntityFishHook.getFish())).getItemStack(this.rand);
        this.entityDropItem(itemStack, 1.0F);
    }

    protected boolean isValidLightLevel()
    {
        return true;
    }

    public boolean isNotColliding()
    {
        return this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty();
    }

    public boolean getCanSpawnHere()
    {
        return (this.rand.nextInt(20) == 0 || !this.worldObj.canBlockSeeSky(this.blockSamplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)))) && super.getCanSpawnHere();
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!this.isMoving() && !source.isMagicDamage() && source.getSourceOfDamage() instanceof EntityLivingBase)
        {
            EntityLivingBase entityLivingBase = (EntityLivingBase)source.getSourceOfDamage();

            if (!source.isExplosion())
            {
                entityLivingBase.attackEntityFrom(DamageSource.causeThornsDamage(this), 2.0F);
                entityLivingBase.playSound("damage.thorns", 0.5F, 1.0F);
            }
        }

        this.wander.makeUpdate();
        return super.attackEntityFrom(source, amount);
    }

    public int getVerticalFaceSpeed()
    {
        return 180;
    }

    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (this.isServerWorld())
        {
            if (this.isInWater())
            {
                this.moveFlying(strafe, forward, 0.1F);
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.8999999761581421D;
                this.motionY *= 0.8999999761581421D;
                this.motionZ *= 0.8999999761581421D;

                if (!this.isMoving() && this.getAttackTarget() == null)
                {
                    this.motionY -= 0.005D;
                }
            }
            else
            {
                super.moveEntityWithHeading(strafe, forward);
            }
        }
        else
        {
            super.moveEntityWithHeading(strafe, forward);
        }
    }

    static class AIGuardianAttack extends EntityAIBase
    {
        private EntityGuardian theEntity;
        private int tickCounter;

        public AIGuardianAttack(EntityGuardian guardian)
        {
            this.theEntity = guardian;
            this.setMutexBits(3);
        }

        public boolean shouldExecute()
        {
            EntityLivingBase entityLivingBase = this.theEntity.getAttackTarget();
            return entityLivingBase != null && entityLivingBase.isEntityAlive();
        }

        public boolean continueExecuting()
        {
            return super.continueExecuting() && (this.theEntity.isElder() || this.theEntity.getDistanceSqToEntity(this.theEntity.getAttackTarget()) > 9.0D);
        }

        public void startExecuting()
        {
            this.tickCounter = -10;
            this.theEntity.getNavigator().clearPathEntity();
            this.theEntity.getLookHelper().setLookPositionWithEntity(this.theEntity.getAttackTarget(), 90.0F, 90.0F);
            this.theEntity.isAirBorne = true;
        }

        public void resetTask()
        {
            this.theEntity.setTargetedEntity(0);
            this.theEntity.setAttackTarget((EntityLivingBase)null);
            this.theEntity.wander.makeUpdate();
        }

        public void updateTask()
        {
            EntityLivingBase entityLivingBase = this.theEntity.getAttackTarget();
            this.theEntity.getNavigator().clearPathEntity();
            this.theEntity.getLookHelper().setLookPositionWithEntity(entityLivingBase, 90.0F, 90.0F);

            if (!this.theEntity.canEntityBeSeen(entityLivingBase))
            {
                this.theEntity.setAttackTarget((EntityLivingBase)null);
            }
            else
            {
                ++this.tickCounter;

                if (this.tickCounter == 0)
                {
                    this.theEntity.setTargetedEntity(this.theEntity.getAttackTarget().getEntityId());
                    this.theEntity.worldObj.setEntityState(this.theEntity, (byte)21);
                }
                else if (this.tickCounter >= this.theEntity.getAttackDuration())
                {
                    float f = 1.0F;

                    if (this.theEntity.worldObj.getDifficulty() == EnumDifficulty.HARD)
                    {
                        f += 2.0F;
                    }

                    if (this.theEntity.isElder())
                    {
                        f += 2.0F;
                    }

                    entityLivingBase.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this.theEntity, this.theEntity), f);
                    entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this.theEntity), (float)this.theEntity.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
                    this.theEntity.setAttackTarget((EntityLivingBase)null);
                }
                else if (this.tickCounter >= 60 && this.tickCounter % 20 == 0)
                {
                    ;
                }

                super.updateTask();
            }
        }
    }

    static class GuardianMoveHelper extends EntityMoveHelper
    {
        private EntityGuardian entityGuardian;

        public GuardianMoveHelper(EntityGuardian guardian)
        {
            super(guardian);
            this.entityGuardian = guardian;
        }

        public void onUpdateMoveHelper()
        {
            if (this.update && !this.entityGuardian.getNavigator().noPath())
            {
                double xCoordinate = this.posX - this.entityGuardian.posX;
                double yCoordinate = this.posY - this.entityGuardian.posY;
                double zCoordinate = this.posZ - this.entityGuardian.posZ;
                double doubleValue = xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
                doubleValue = (double)MathHelper.sqrt_double(doubleValue);
                yCoordinate = yCoordinate / doubleValue;
                float f = (float)(MathHelper.atan2(zCoordinate, xCoordinate) * 180.0D / Math.PI) - 90.0F;
                this.entityGuardian.rotationYaw = this.limitAngle(this.entityGuardian.rotationYaw, f, 30.0F);
                this.entityGuardian.renderYawOffset = this.entityGuardian.rotationYaw;
                float floatValue2 = (float)(this.speed * this.entityGuardian.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
                this.entityGuardian.setAIMoveSpeed(this.entityGuardian.getAIMoveSpeed() + (floatValue2 - this.entityGuardian.getAIMoveSpeed()) * 0.125F);
                double doubleValue2 = Math.sin((double)(this.entityGuardian.ticksExisted + this.entityGuardian.getEntityId()) * 0.5D) * 0.05D;
                double doubleValue3 = Math.cos((double)(this.entityGuardian.rotationYaw * (float)Math.PI / 180.0F));
                double doubleValue4 = Math.sin((double)(this.entityGuardian.rotationYaw * (float)Math.PI / 180.0F));
                this.entityGuardian.motionX += doubleValue2 * doubleValue3;
                this.entityGuardian.motionZ += doubleValue2 * doubleValue4;
                doubleValue2 = Math.sin((double)(this.entityGuardian.ticksExisted + this.entityGuardian.getEntityId()) * 0.75D) * 0.05D;
                this.entityGuardian.motionY += doubleValue2 * (doubleValue4 + doubleValue3) * 0.25D;
                this.entityGuardian.motionY += (double)this.entityGuardian.getAIMoveSpeed() * yCoordinate * 0.1D;
                EntityLookHelper entityLookHelper = this.entityGuardian.getLookHelper();
                double xCoordinate2 = this.entityGuardian.posX + xCoordinate / doubleValue * 2.0D;
                double yCoordinate2 = (double)this.entityGuardian.getEyeHeight() + this.entityGuardian.posY + yCoordinate / doubleValue * 1.0D;
                double zCoordinate2 = this.entityGuardian.posZ + zCoordinate / doubleValue * 2.0D;
                double doubleValue5 = entityLookHelper.getLookPosX();
                double doubleValue6 = entityLookHelper.getLookPosY();
                double doubleValue7 = entityLookHelper.getLookPosZ();

                if (!entityLookHelper.getIsLooking())
                {
                    doubleValue5 = xCoordinate2;
                    doubleValue6 = yCoordinate2;
                    doubleValue7 = zCoordinate2;
                }

                this.entityGuardian.getLookHelper().setLookPosition(doubleValue5 + (xCoordinate2 - doubleValue5) * 0.125D, doubleValue6 + (yCoordinate2 - doubleValue6) * 0.125D, doubleValue7 + (zCoordinate2 - doubleValue7) * 0.125D, 10.0F, 40.0F);
                this.entityGuardian.setMoving(true);
            }
            else
            {
                this.entityGuardian.setAIMoveSpeed(0.0F);
                this.entityGuardian.setMoving(false);
            }
        }
    }

    static class GuardianTargetSelector implements Predicate<EntityLivingBase>
    {
        private EntityGuardian parentEntity;

        public GuardianTargetSelector(EntityGuardian guardian)
        {
            this.parentEntity = guardian;
        }

        public boolean apply(EntityLivingBase target)
        {
            return (target instanceof EntityPlayer || target instanceof EntitySquid) && target.getDistanceSqToEntity(this.parentEntity) > 9.0D;
        }
    }
}
