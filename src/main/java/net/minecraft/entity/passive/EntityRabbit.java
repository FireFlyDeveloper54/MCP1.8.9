package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityRabbit extends EntityAnimal
{
    private EntityRabbit.AIAvoidEntity<EntityWolf> aiAvoidWolves;
    private int jumpTicks = 0;
    private int jumpDuration = 0;
    private boolean rabbitJumping = false;
    private boolean wasOnGround = false;
    private int currentMoveTypeDuration = 0;
    private EntityRabbit.EnumMoveType moveType = EntityRabbit.EnumMoveType.HOP;
    private int carrotTicks = 0;
    private EntityPlayer carrotTemptingPlayer = null;

    public EntityRabbit(World worldIn)
    {
        super(worldIn);
        this.setSize(0.6F, 0.7F);
        this.jumpHelper = new EntityRabbit.RabbitJumpHelper(this);
        this.moveHelper = new EntityRabbit.RabbitMoveHelper(this);
        ((PathNavigateGround)this.getNavigator()).setAvoidsWater(true);
        this.navigator.setHeightRequirement(2.5F);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityRabbit.AIPanic(this, 1.33D));
        this.tasks.addTask(2, new EntityAITempt(this, 1.0D, Items.carrot, false));
        this.tasks.addTask(2, new EntityAITempt(this, 1.0D, Items.golden_carrot, false));
        this.tasks.addTask(2, new EntityAITempt(this, 1.0D, Item.getItemFromBlock(Blocks.yellow_flower), false));
        this.tasks.addTask(3, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(5, new EntityRabbit.AIRaidFarm(this));
        this.tasks.addTask(5, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.aiAvoidWolves = new EntityRabbit.AIAvoidEntity(this, EntityWolf.class, 16.0F, 1.33D, 1.33D);
        this.tasks.addTask(4, this.aiAvoidWolves);
        this.setMovementSpeed(0.0D);
    }

    protected float getJumpUpwardsMotion()
    {
        return this.moveHelper.isUpdating() && this.moveHelper.getY() > this.posY + 0.5D ? 0.5F : this.moveType.getJumpVelocity();
    }

    public void setMoveType(EntityRabbit.EnumMoveType type)
    {
        this.moveType = type;
    }

    public float getJumpCompletion(float partialTicks)
    {
        return this.jumpDuration == 0 ? 0.0F : ((float)this.jumpTicks + partialTicks) / (float)this.jumpDuration;
    }

    public void setMovementSpeed(double newSpeed)
    {
        this.getNavigator().setSpeed(newSpeed);
        this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), newSpeed);
    }

    public void setJumping(boolean jump, EntityRabbit.EnumMoveType moveTypeIn)
    {
        super.setJumping(jump);

        if (!jump)
        {
            if (this.moveType == EntityRabbit.EnumMoveType.ATTACK)
            {
                this.moveType = EntityRabbit.EnumMoveType.HOP;
            }
        }
        else
        {
            this.setMovementSpeed(1.5D * (double)moveTypeIn.getSpeed());
            this.playSound(this.getJumpingSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }

        this.rabbitJumping = jump;
    }

    public void doMovementAction(EntityRabbit.EnumMoveType movetype)
    {
        this.setJumping(true, movetype);
        this.jumpDuration = movetype.getJumpDuration();
        this.jumpTicks = 0;
    }

    public boolean isRabbitJumping()
    {
        return this.rabbitJumping;
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(18, Byte.valueOf((byte)0));
    }

    public void updateAITasks()
    {
        if (this.moveHelper.getSpeed() > 0.8D)
        {
            this.setMoveType(EntityRabbit.EnumMoveType.SPRINT);
        }
        else if (this.moveType != EntityRabbit.EnumMoveType.ATTACK)
        {
            this.setMoveType(EntityRabbit.EnumMoveType.HOP);
        }

        if (this.currentMoveTypeDuration > 0)
        {
            --this.currentMoveTypeDuration;
        }

        if (this.carrotTicks > 0)
        {
            this.carrotTicks -= this.rand.nextInt(3);

            if (this.carrotTicks < 0)
            {
                this.carrotTicks = 0;
            }
        }

        if (this.onGround)
        {
            if (!this.wasOnGround)
            {
                this.setJumping(false, EntityRabbit.EnumMoveType.NONE);
                this.resetJumpControl();
            }

            if (this.getRabbitType() == 99 && this.currentMoveTypeDuration == 0)
            {
                EntityLivingBase entityLivingBase = this.getAttackTarget();

                if (entityLivingBase != null && this.getDistanceSqToEntity(entityLivingBase) < 16.0D)
                {
                    this.calculateRotationYaw(entityLivingBase.posX, entityLivingBase.posZ);
                    this.moveHelper.setMoveTo(entityLivingBase.posX, entityLivingBase.posY, entityLivingBase.posZ, this.moveHelper.getSpeed());
                    this.doMovementAction(EntityRabbit.EnumMoveType.ATTACK);
                    this.wasOnGround = true;
                }
            }

            EntityRabbit.RabbitJumpHelper entityrabbit$rabbitjumphelper = (EntityRabbit.RabbitJumpHelper)this.jumpHelper;

            if (!entityrabbit$rabbitjumphelper.getIsJumping())
            {
                if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0)
                {
                    PathEntity pathEntity = this.navigator.getPath();
                    Vec3 localValue = new Vec3(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());

                    if (pathEntity != null && pathEntity.getCurrentPathIndex() < pathEntity.getCurrentPathLength())
                    {
                        localValue = pathEntity.getPosition(this);
                    }

                    this.calculateRotationYaw(localValue.xCoord, localValue.zCoord);
                    this.doMovementAction(this.moveType);
                }
            }
            else if (!entityrabbit$rabbitjumphelper.getCanJump())
            {
                this.enableJumpControl();
            }
        }

        this.wasOnGround = this.onGround;
    }

    public void spawnRunningParticles()
    {
    }

    private void calculateRotationYaw(double x, double z)
    {
        this.rotationYaw = (float)(MathHelper.atan2(z - this.posZ, x - this.posX) * 180.0D / Math.PI) - 90.0F;
    }

    private void enableJumpControl()
    {
        ((EntityRabbit.RabbitJumpHelper)this.jumpHelper).setCanJump(true);
    }

    private void disableJumpControl()
    {
        ((EntityRabbit.RabbitJumpHelper)this.jumpHelper).setCanJump(false);
    }

    private void updateMoveTypeDuration()
    {
        this.currentMoveTypeDuration = this.getMoveTypeDuration();
    }

    private void resetJumpControl()
    {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }

    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        if (this.jumpTicks != this.jumpDuration)
        {
            if (this.jumpTicks == 0 && !this.worldObj.isRemote)
            {
                this.worldObj.setEntityState(this, (byte)1);
            }

            ++this.jumpTicks;
        }
        else if (this.jumpDuration != 0)
        {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
        }
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("RabbitType", this.getRabbitType());
        tagCompound.setInteger("MoreCarrotTicks", this.carrotTicks);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        this.setRabbitType(tagCompund.getInteger("RabbitType"));
        this.carrotTicks = tagCompund.getInteger("MoreCarrotTicks");
    }

    protected String getJumpingSound()
    {
        return "mob.rabbit.hop";
    }

    protected String getLivingSound()
    {
        return "mob.rabbit.idle";
    }

    protected String getHurtSound()
    {
        return "mob.rabbit.hurt";
    }

    protected String getDeathSound()
    {
        return "mob.rabbit.death";
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        if (this.getRabbitType() == 99)
        {
            this.playSound("mob.attack", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
        }
        else
        {
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
        }
    }

    public int getTotalArmorValue()
    {
        return this.getRabbitType() == 99 ? 8 : super.getTotalArmorValue();
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return this.isEntityInvulnerable(source) ? false : super.attackEntityFrom(source, amount);
    }

    protected void addRandomDrop()
    {
        this.entityDropItem(new ItemStack(Items.rabbit_foot, 1), 0.0F);
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        int i = this.rand.nextInt(2) + this.rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j)
        {
            this.dropItem(Items.rabbit_hide, 1);
        }

        i = this.rand.nextInt(2);

        for (int k = 0; k < i; ++k)
        {
            if (this.isBurning())
            {
                this.dropItem(Items.cooked_rabbit, 1);
            }
            else
            {
                this.dropItem(Items.rabbit, 1);
            }
        }
    }

    private boolean isRabbitBreedingItem(Item itemIn)
    {
        return itemIn == Items.carrot || itemIn == Items.golden_carrot || itemIn == Item.getItemFromBlock(Blocks.yellow_flower);
    }

    public EntityRabbit createChild(EntityAgeable ageable)
    {
        EntityRabbit entityRabbit = new EntityRabbit(this.worldObj);

        if (ageable instanceof EntityRabbit)
        {
            entityRabbit.setRabbitType(this.rand.nextBoolean() ? this.getRabbitType() : ((EntityRabbit)ageable).getRabbitType());
        }

        return entityRabbit;
    }

    public boolean isBreedingItem(ItemStack stack)
    {
        return stack != null && this.isRabbitBreedingItem(stack.getItem());
    }

    public int getRabbitType()
    {
        return this.dataWatcher.getWatchableObjectByte(18);
    }

    public void setRabbitType(int rabbitTypeId)
    {
        if (rabbitTypeId == 99)
        {
            this.tasks.removeTask(this.aiAvoidWolves);
            this.tasks.addTask(4, new EntityRabbit.AIEvilAttack(this));
            this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityWolf.class, true));

            if (!this.hasCustomName())
            {
                this.setCustomNameTag(StatCollector.translateToLocal("entity.KillerBunny.name"));
            }
        }

        this.dataWatcher.updateObject(18, Byte.valueOf((byte)rabbitTypeId));
    }

    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        int i = this.rand.nextInt(6);
        boolean flag = false;

        if (livingdata instanceof EntityRabbit.RabbitTypeData)
        {
            i = ((EntityRabbit.RabbitTypeData)livingdata).typeData;
            flag = true;
        }
        else
        {
            livingdata = new EntityRabbit.RabbitTypeData(i);
        }

        this.setRabbitType(i);

        if (flag)
        {
            this.setGrowingAge(-24000);
        }

        return livingdata;
    }

    private boolean isCarrotEaten()
    {
        return this.carrotTicks == 0;
    }

    protected int getMoveTypeDuration()
    {
        return this.moveType.getDuration();
    }

    protected void createEatingParticles()
    {
        this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 0.0D, 0.0D, 0.0D, new int[] {Block.getStateId(Blocks.carrots.getStateFromMeta(7))});
        this.carrotTicks = 100;
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 1)
        {
            this.createRunningParticles();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    static class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T>
    {
        private EntityRabbit entityInstance;

        public AIAvoidEntity(EntityRabbit rabbit, Class<T> avoidClass, float avoidDistance, double farSpeed, double nearSpeed)
        {
            super(rabbit, avoidClass, avoidDistance, farSpeed, nearSpeed);
            this.entityInstance = rabbit;
        }

        public void updateTask()
        {
            super.updateTask();
        }
    }

    static class AIEvilAttack extends EntityAIAttackOnCollide
    {
        public AIEvilAttack(EntityRabbit rabbit)
        {
            super(rabbit, EntityLivingBase.class, 1.4D, true);
        }

        protected double getAttackReachSqr(EntityLivingBase attackTarget)
        {
            return (double)(4.0F + attackTarget.width);
        }
    }

    static class AIPanic extends EntityAIPanic
    {
        private EntityRabbit theEntity;

        public AIPanic(EntityRabbit rabbit, double speedIn)
        {
            super(rabbit, speedIn);
            this.theEntity = rabbit;
        }

        public void updateTask()
        {
            super.updateTask();
            this.theEntity.setMovementSpeed(this.speed);
        }
    }

    static class AIRaidFarm extends EntityAIMoveToBlock
    {
        private final EntityRabbit rabbit;
        private boolean canRaid;
        private boolean wantsToRaid = false;

        public AIRaidFarm(EntityRabbit rabbitIn)
        {
            super(rabbitIn, 0.699999988079071D, 16);
            this.rabbit = rabbitIn;
        }

        public boolean shouldExecute()
        {
            if (this.runDelay <= 0)
            {
                if (!this.rabbit.worldObj.getGameRules().getBoolean("mobGriefing"))
                {
                    return false;
                }

                this.wantsToRaid = false;
                this.canRaid = this.rabbit.isCarrotEaten();
            }

            return super.shouldExecute();
        }

        public boolean continueExecuting()
        {
            return this.wantsToRaid && super.continueExecuting();
        }

        public void startExecuting()
        {
            super.startExecuting();
        }

        public void resetTask()
        {
            super.resetTask();
        }

        public void updateTask()
        {
            super.updateTask();
            this.rabbit.getLookHelper().setLookPosition((double)this.destinationBlock.getX() + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)this.destinationBlock.getZ() + 0.5D, 10.0F, (float)this.rabbit.getVerticalFaceSpeed());

            if (this.getIsAboveDestination())
            {
                World world = this.rabbit.worldObj;
                BlockPos blockPos = this.destinationBlock.up();
                IBlockState iblockstate = world.getBlockState(blockPos);
                Block block = iblockstate.getBlock();

                if (this.wantsToRaid && block instanceof BlockCarrot && ((Integer)iblockstate.getValue(BlockCarrot.AGE)).intValue() == 7)
                {
                    world.setBlockState(blockPos, Blocks.air.getDefaultState(), 2);
                    world.destroyBlock(blockPos, true);
                    this.rabbit.createEatingParticles();
                }

                this.wantsToRaid = false;
                this.runDelay = 10;
            }
        }

        protected boolean shouldMoveTo(World worldIn, BlockPos pos)
        {
            Block block = worldIn.getBlockState(pos).getBlock();

            if (block == Blocks.farmland)
            {
                pos = pos.up();
                IBlockState iblockstate = worldIn.getBlockState(pos);
                block = iblockstate.getBlock();

                if (block instanceof BlockCarrot && ((Integer)iblockstate.getValue(BlockCarrot.AGE)).intValue() == 7 && this.canRaid && !this.wantsToRaid)
                {
                    this.wantsToRaid = true;
                    return true;
                }
            }

            return false;
        }
    }

    static enum EnumMoveType
    {
        NONE(0.0F, 0.0F, 30, 1),
        HOP(0.8F, 0.2F, 20, 10),
        STEP(1.0F, 0.45F, 14, 14),
        SPRINT(1.75F, 0.4F, 1, 8),
        ATTACK(2.0F, 0.7F, 7, 8);

        private final float speed;
        private final float jumpVelocity;
        private final int duration;
        private final int jumpDuration;

        private EnumMoveType(float typeSpeed, float jumpVelocityIn, int typeDuration, int jumpDurationIn)
        {
            this.speed = typeSpeed;
            this.jumpVelocity = jumpVelocityIn;
            this.duration = typeDuration;
            this.jumpDuration = jumpDurationIn;
        }

        public float getSpeed()
        {
            return this.speed;
        }

        public float getJumpVelocity()
        {
            return this.jumpVelocity;
        }

        public int getDuration()
        {
            return this.duration;
        }

        public int getJumpDuration()
        {
            return this.jumpDuration;
        }
    }

    public class RabbitJumpHelper extends EntityJumpHelper
    {
        private EntityRabbit theEntity;
        private boolean canJump = false;

        public RabbitJumpHelper(EntityRabbit rabbit)
        {
            super(rabbit);
            this.theEntity = rabbit;
        }

        public boolean getIsJumping()
        {
            return this.isJumping;
        }

        public boolean getCanJump()
        {
            return this.canJump;
        }

        public void setCanJump(boolean canJumpIn)
        {
            this.canJump = canJumpIn;
        }

        public void doJump()
        {
            if (this.isJumping)
            {
                this.theEntity.doMovementAction(EntityRabbit.EnumMoveType.STEP);
                this.isJumping = false;
            }
        }
    }

    static class RabbitMoveHelper extends EntityMoveHelper
    {
        private EntityRabbit theEntity;

        public RabbitMoveHelper(EntityRabbit rabbit)
        {
            super(rabbit);
            this.theEntity = rabbit;
        }

        public void onUpdateMoveHelper()
        {
            if (this.theEntity.onGround && !this.theEntity.isRabbitJumping())
            {
                this.theEntity.setMovementSpeed(0.0D);
            }

            super.onUpdateMoveHelper();
        }
    }

    public static class RabbitTypeData implements IEntityLivingData
    {
        public int typeData;

        public RabbitTypeData(int type)
        {
            this.typeData = type;
        }
    }
}
