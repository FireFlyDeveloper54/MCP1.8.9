package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityEnderman extends EntityMob
{
    private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", 0.15000000596046448D, 0)).setSaved(false);
    private static final Set<Block> carriableBlocks = Sets.<Block>newIdentityHashSet();
    private boolean isAggressive;
    private final BlockPos.MutableBlockPos daylightCheckPos = new BlockPos.MutableBlockPos();

    public EntityEnderman(World worldIn)
    {
        super(worldIn);
        this.setSize(0.6F, 2.9F);
        this.stepHeight = 1.0F;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0D, false));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(10, new EntityEnderman.AIPlaceBlock(this));
        this.tasks.addTask(11, new EntityEnderman.AITakeBlock(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityEnderman.AIFindPlayer(this));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityEndermite.class, 10, true, false, new Predicate<EntityEndermite>()
        {
            public boolean apply(EntityEndermite endermite)
            {
                return endermite.isSpawnedByPlayer();
            }
        }));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(7.0D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64.0D);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Short.valueOf((short)0));
        this.dataWatcher.addObject(17, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(18, Byte.valueOf((byte)0));
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        IBlockState iblockstate = this.getHeldBlockState();
        tagCompound.setShort("carried", (short)Block.getIdFromBlock(iblockstate.getBlock()));
        tagCompound.setShort("carriedData", (short)iblockstate.getBlock().getMetaFromState(iblockstate));
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        IBlockState iblockstate;

        if (tagCompund.hasKey("carried", 8))
        {
            iblockstate = Block.getBlockFromName(tagCompund.getString("carried")).getStateFromMeta(tagCompund.getShort("carriedData") & 65535);
        }
        else
        {
            iblockstate = Block.getBlockById(tagCompund.getShort("carried")).getStateFromMeta(tagCompund.getShort("carriedData") & 65535);
        }

        this.setHeldBlockState(iblockstate);
    }

    private boolean shouldAttackPlayer(EntityPlayer player)
    {
        ItemStack itemStack = player.inventory.armorInventory[3];

        if (itemStack != null && itemStack.getItem() == Item.getItemFromBlock(Blocks.pumpkin))
        {
            return false;
        }
        else
        {
            Vec3 localValue = player.getLook(1.0F).normalize();
            Vec3 secondLocalValue = new Vec3(this.posX - player.posX, this.getEntityBoundingBox().minY + (double)(this.height / 2.0F) - (player.posY + (double)player.getEyeHeight()), this.posZ - player.posZ);
            double doubleValue = secondLocalValue.lengthVector();
            secondLocalValue = secondLocalValue.normalize();
            double doubleValue2 = localValue.dotProduct(secondLocalValue);
            return doubleValue2 > 1.0D - 0.025D / doubleValue ? player.canEntityBeSeen(this) : false;
        }
    }

    public float getEyeHeight()
    {
        return 2.55F;
    }

    public void onLivingUpdate()
    {
        if (this.worldObj.isRemote)
        {
            for (int i = 0; i < 2; ++i)
            {
                this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D, EnumParticleTypes.EMPTY_ARGS);
            }
        }

        this.isJumping = false;
        super.onLivingUpdate();
    }

    protected void updateAITasks()
    {
        if (this.isWet())
        {
            this.attackEntityFrom(DamageSource.drown, 1.0F);
        }

        if (this.isScreaming() && !this.isAggressive && this.rand.nextInt(100) == 0)
        {
            this.setScreaming(false);
        }

        if (this.worldObj.isDaytime())
        {
            float f = this.getBrightness(1.0F);

            if (f > 0.5F && this.worldObj.canSeeSky(this.daylightCheckPos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))) && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F)
            {
                this.setAttackTarget((EntityLivingBase)null);
                this.setScreaming(false);
                this.isAggressive = false;
                this.teleportRandomly();
            }
        }

        super.updateAITasks();
    }

    protected boolean teleportRandomly()
    {
        double xCoordinate = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double yCoordinate = this.posY + (double)(this.rand.nextInt(64) - 32);
        double zCoordinate = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        return this.teleportTo(xCoordinate, yCoordinate, zCoordinate);
    }

    protected boolean teleportToEntity(Entity entityIn)
    {
        Vec3 localValue = new Vec3(this.posX - entityIn.posX, this.getEntityBoundingBox().minY + (double)(this.height / 2.0F) - entityIn.posY + (double)entityIn.getEyeHeight(), this.posZ - entityIn.posZ);
        localValue = localValue.normalize();
        double doubleValue = 16.0D;
        double xCoordinate = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - localValue.xCoord * doubleValue;
        double yCoordinate = this.posY + (double)(this.rand.nextInt(16) - 8) - localValue.yCoord * doubleValue;
        double zCoordinate = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - localValue.zCoord * doubleValue;
        return this.teleportTo(xCoordinate, yCoordinate, zCoordinate);
    }

    protected boolean teleportTo(double x, double y, double z)
    {
        double doubleValue = this.posX;
        double secondDoubleValue = this.posY;
        double thirdDoubleValue = this.posZ;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        boolean flag = false;
        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);

        if (this.worldObj.isBlockLoaded(blockpos))
        {
            boolean flag1 = false;

            while (!flag1 && blockpos.getY() > 0)
            {
                BlockPos blockpos1 = blockpos.down();
                Block block = this.worldObj.getBlockState(blockpos1).getBlock();

                if (block.getMaterial().blocksMovement())
                {
                    flag1 = true;
                }
                else
                {
                    --this.posY;
                    blockpos = blockpos1;
                }
            }

            if (flag1)
            {
                super.setPositionAndUpdate(this.posX, this.posY, this.posZ);

                if (this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.worldObj.isAnyLiquid(this.getEntityBoundingBox()))
                {
                    flag = true;
                }
            }
        }

        if (!flag)
        {
            this.setPosition(doubleValue, secondDoubleValue, thirdDoubleValue);
            return false;
        }
        else
        {
            int i = 128;

            for (int j = 0; j < i; ++j)
            {
                double fourthDoubleValue = (double)j / ((double)i - 1.0D);
                float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float floatValue = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float secondFloatValue = (this.rand.nextFloat() - 0.5F) * 0.2F;
                double fifthDoubleValue = doubleValue + (this.posX - doubleValue) * fourthDoubleValue + (this.rand.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                double sixthDoubleValue = secondDoubleValue + (this.posY - secondDoubleValue) * fourthDoubleValue + this.rand.nextDouble() * (double)this.height;
                double seventhDoubleValue = thirdDoubleValue + (this.posZ - thirdDoubleValue) * fourthDoubleValue + (this.rand.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, fifthDoubleValue, sixthDoubleValue, seventhDoubleValue, (double)f, (double)floatValue, (double)secondFloatValue, EnumParticleTypes.EMPTY_ARGS);
            }

            this.worldObj.playSoundEffect(doubleValue, secondDoubleValue, thirdDoubleValue, "mob.endermen.portal", 1.0F, 1.0F);
            this.playSound("mob.endermen.portal", 1.0F, 1.0F);
            return true;
        }
    }

    protected String getLivingSound()
    {
        return this.isScreaming() ? "mob.endermen.scream" : "mob.endermen.idle";
    }

    protected String getHurtSound()
    {
        return "mob.endermen.hit";
    }

    protected String getDeathSound()
    {
        return "mob.endermen.death";
    }

    protected Item getDropItem()
    {
        return Items.ender_pearl;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        Item item = this.getDropItem();

        if (item != null)
        {
            int i = this.rand.nextInt(2 + lootingModifier);

            for (int j = 0; j < i; ++j)
            {
                this.dropItem(item, 1);
            }
        }
    }

    public void setHeldBlockState(IBlockState state)
    {
        this.dataWatcher.updateObject(16, Short.valueOf((short)(Block.getStateId(state) & 65535)));
    }

    public IBlockState getHeldBlockState()
    {
        return Block.getStateById(this.dataWatcher.getWatchableObjectShort(16) & 65535);
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (source.getEntity() == null || !(source.getEntity() instanceof EntityEndermite))
            {
                if (!this.worldObj.isRemote)
                {
                    this.setScreaming(true);
                }

                if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityPlayer)
                {
                    if (source.getEntity() instanceof EntityPlayerMP && ((EntityPlayerMP)source.getEntity()).theItemInWorldManager.isCreative())
                    {
                        this.setScreaming(false);
                    }
                    else
                    {
                        this.isAggressive = true;
                    }
                }

                if (source instanceof EntityDamageSourceIndirect)
                {
                    this.isAggressive = false;

                    for (int i = 0; i < 64; ++i)
                    {
                        if (this.teleportRandomly())
                        {
                            return true;
                        }
                    }

                    return false;
                }
            }

            boolean flag = super.attackEntityFrom(source, amount);

            if (source.isUnblockable() && this.rand.nextInt(10) != 0)
            {
                this.teleportRandomly();
            }

            return flag;
        }
    }

    public boolean isScreaming()
    {
        return this.dataWatcher.getWatchableObjectByte(18) > 0;
    }

    public void setScreaming(boolean screaming)
    {
        this.dataWatcher.updateObject(18, Byte.valueOf((byte)(screaming ? 1 : 0)));
    }

    static
    {
        carriableBlocks.add(Blocks.grass);
        carriableBlocks.add(Blocks.dirt);
        carriableBlocks.add(Blocks.sand);
        carriableBlocks.add(Blocks.gravel);
        carriableBlocks.add(Blocks.yellow_flower);
        carriableBlocks.add(Blocks.red_flower);
        carriableBlocks.add(Blocks.brown_mushroom);
        carriableBlocks.add(Blocks.red_mushroom);
        carriableBlocks.add(Blocks.tnt);
        carriableBlocks.add(Blocks.cactus);
        carriableBlocks.add(Blocks.clay);
        carriableBlocks.add(Blocks.pumpkin);
        carriableBlocks.add(Blocks.melon_block);
        carriableBlocks.add(Blocks.mycelium);
    }

    static class AIFindPlayer extends EntityAINearestAttackableTarget
    {
        private EntityPlayer player;
        private int aggroTime;
        private int teleportTime;
        private EntityEnderman enderman;

        public AIFindPlayer(EntityEnderman endermanIn)
        {
            super(endermanIn, EntityPlayer.class, true);
            this.enderman = endermanIn;
        }

        public boolean shouldExecute()
        {
            double doubleValue = this.getTargetDistance();
            List<EntityPlayer> list = this.taskOwner.worldObj.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.taskOwner.getEntityBoundingBox().expand(doubleValue, 4.0D, doubleValue), this.targetEntitySelector);
            Collections.sort(list, this.nearestTargetSorter);

            if (list.isEmpty())
            {
                return false;
            }
            else
            {
                this.player = (EntityPlayer)list.get(0);
                return true;
            }
        }

        public void startExecuting()
        {
            this.aggroTime = 5;
            this.teleportTime = 0;
        }

        public void resetTask()
        {
            this.player = null;
            this.enderman.setScreaming(false);
            IAttributeInstance iattributeinstance = this.enderman.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(EntityEnderman.attackingSpeedBoostModifier);
            super.resetTask();
        }

        public boolean continueExecuting()
        {
            if (this.player != null)
            {
                if (!this.enderman.shouldAttackPlayer(this.player))
                {
                    return false;
                }
                else
                {
                    this.enderman.isAggressive = true;
                    this.enderman.faceEntity(this.player, 10.0F, 10.0F);
                    return true;
                }
            }
            else
            {
                return super.continueExecuting();
            }
        }

        public void updateTask()
        {
            if (this.player != null)
            {
                if (--this.aggroTime <= 0)
                {
                    this.targetEntity = this.player;
                    this.player = null;
                    super.startExecuting();
                    this.enderman.playSound("mob.endermen.stare", 1.0F, 1.0F);
                    this.enderman.setScreaming(true);
                    IAttributeInstance iattributeinstance = this.enderman.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
                    iattributeinstance.applyModifier(EntityEnderman.attackingSpeedBoostModifier);
                }
            }
            else
            {
                if (this.targetEntity != null)
                {
                    if (this.targetEntity instanceof EntityPlayer && this.enderman.shouldAttackPlayer((EntityPlayer)this.targetEntity))
                    {
                        if (this.targetEntity.getDistanceSqToEntity(this.enderman) < 16.0D)
                        {
                            this.enderman.teleportRandomly();
                        }

                        this.teleportTime = 0;
                    }
                    else if (this.targetEntity.getDistanceSqToEntity(this.enderman) > 256.0D && this.teleportTime++ >= 30 && this.enderman.teleportToEntity(this.targetEntity))
                    {
                        this.teleportTime = 0;
                    }
                }

                super.updateTask();
            }
        }
    }

    static class AIPlaceBlock extends EntityAIBase
    {
        private EntityEnderman enderman;

        public AIPlaceBlock(EntityEnderman endermanIn)
        {
            this.enderman = endermanIn;
        }

        public boolean shouldExecute()
        {
            return !this.enderman.worldObj.getGameRules().getBoolean("mobGriefing") ? false : (this.enderman.getHeldBlockState().getBlock().getMaterial() == Material.air ? false : this.enderman.getRNG().nextInt(2000) == 0);
        }

        public void updateTask()
        {
            Random random = this.enderman.getRNG();
            World world = this.enderman.worldObj;
            int i = MathHelper.floor_double(this.enderman.posX - 1.0D + random.nextDouble() * 2.0D);
            int j = MathHelper.floor_double(this.enderman.posY + random.nextDouble() * 2.0D);
            int k = MathHelper.floor_double(this.enderman.posZ - 1.0D + random.nextDouble() * 2.0D);
            BlockPos blockPos = new BlockPos(i, j, k);
            Block block = world.getBlockState(blockPos).getBlock();
            Block block1 = world.getBlockState(blockPos.down()).getBlock();

            if (this.canPlaceBlockAt(world, blockPos, this.enderman.getHeldBlockState().getBlock(), block, block1))
            {
                world.setBlockState(blockPos, this.enderman.getHeldBlockState(), 3);
                this.enderman.setHeldBlockState(Blocks.air.getDefaultState());
            }
        }

        private boolean canPlaceBlockAt(World worldIn, BlockPos pos, Block heldBlock, Block targetBlock, Block belowBlock)
        {
            return !heldBlock.canPlaceBlockAt(worldIn, pos) ? false : (targetBlock.getMaterial() != Material.air ? false : (belowBlock.getMaterial() == Material.air ? false : belowBlock.isFullCube()));
        }
    }

    static class AITakeBlock extends EntityAIBase
    {
        private EntityEnderman enderman;

        public AITakeBlock(EntityEnderman endermanIn)
        {
            this.enderman = endermanIn;
        }

        public boolean shouldExecute()
        {
            return !this.enderman.worldObj.getGameRules().getBoolean("mobGriefing") ? false : (this.enderman.getHeldBlockState().getBlock().getMaterial() != Material.air ? false : this.enderman.getRNG().nextInt(20) == 0);
        }

        public void updateTask()
        {
            Random random = this.enderman.getRNG();
            World world = this.enderman.worldObj;
            int i = MathHelper.floor_double(this.enderman.posX - 2.0D + random.nextDouble() * 4.0D);
            int j = MathHelper.floor_double(this.enderman.posY + random.nextDouble() * 3.0D);
            int k = MathHelper.floor_double(this.enderman.posZ - 2.0D + random.nextDouble() * 4.0D);
            BlockPos blockPos = new BlockPos(i, j, k);
            IBlockState iblockstate = world.getBlockState(blockPos);
            Block block = iblockstate.getBlock();

            if (EntityEnderman.carriableBlocks.contains(block))
            {
                this.enderman.setHeldBlockState(iblockstate);
                world.setBlockState(blockPos, Blocks.air.getDefaultState());
            }
        }
    }
}
