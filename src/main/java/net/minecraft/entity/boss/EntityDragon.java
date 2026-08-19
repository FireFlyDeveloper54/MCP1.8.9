package net.minecraft.entity.boss;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityDragon extends EntityLiving implements IBossDisplayData, IEntityMultiPart, IMob
{
    public double targetX;
    public double targetY;
    public double targetZ;
    public double[][] ringBuffer = new double[64][3];
    public int ringBufferIndex = -1;
    public EntityDragonPart[] dragonPartArray;
    public EntityDragonPart dragonPartHead;
    public EntityDragonPart dragonPartBody;
    public EntityDragonPart dragonPartTail1;
    public EntityDragonPart dragonPartTail2;
    public EntityDragonPart dragonPartTail3;
    public EntityDragonPart dragonPartWing1;
    public EntityDragonPart dragonPartWing2;
    public float prevAnimTime;
    public float animTime;
    public boolean forceNewTarget;
    public boolean slowed;
    private Entity target;
    public int deathTicks;
    public EntityEnderCrystal healingEnderCrystal;

    public EntityDragon(World worldIn)
    {
        super(worldIn);
        this.dragonPartArray = new EntityDragonPart[] {this.dragonPartHead = new EntityDragonPart(this, "head", 6.0F, 6.0F), this.dragonPartBody = new EntityDragonPart(this, "body", 8.0F, 8.0F), this.dragonPartTail1 = new EntityDragonPart(this, "tail", 4.0F, 4.0F), this.dragonPartTail2 = new EntityDragonPart(this, "tail", 4.0F, 4.0F), this.dragonPartTail3 = new EntityDragonPart(this, "tail", 4.0F, 4.0F), this.dragonPartWing1 = new EntityDragonPart(this, "wing", 4.0F, 4.0F), this.dragonPartWing2 = new EntityDragonPart(this, "wing", 4.0F, 4.0F)};
        this.setHealth(this.getMaxHealth());
        this.setSize(16.0F, 8.0F);
        this.noClip = true;
        this.isImmuneToFire = true;
        this.targetY = 100.0D;
        this.ignoreFrustumCheck = true;
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(200.0D);
    }

    protected void entityInit()
    {
        super.entityInit();
    }

    public double[] getMovementOffsets(int offset, float partialTicks)
    {
        if (this.getHealth() <= 0.0F)
        {
            partialTicks = 0.0F;
        }

        partialTicks = 1.0F - partialTicks;
        int i = this.ringBufferIndex - offset * 1 & 63;
        int j = this.ringBufferIndex - offset * 1 - 1 & 63;
        double[] adouble = new double[3];
        double doubleValue = this.ringBuffer[i][0];
        double doubleValue2 = MathHelper.wrapAngleTo180_double(this.ringBuffer[j][0] - doubleValue);
        adouble[0] = doubleValue + doubleValue2 * (double)partialTicks;
        doubleValue = this.ringBuffer[i][1];
        doubleValue2 = this.ringBuffer[j][1] - doubleValue;
        adouble[1] = doubleValue + doubleValue2 * (double)partialTicks;
        adouble[2] = this.ringBuffer[i][2] + (this.ringBuffer[j][2] - this.ringBuffer[i][2]) * (double)partialTicks;
        return adouble;
    }

    public void onLivingUpdate()
    {
        if (this.worldObj.isRemote)
        {
            float f = MathHelper.cos(this.animTime * (float)Math.PI * 2.0F);
            float floatValue = MathHelper.cos(this.prevAnimTime * (float)Math.PI * 2.0F);

            if (floatValue <= -0.3F && f >= -0.3F && !this.isSilent())
            {
                this.worldObj.playSound(this.posX, this.posY, this.posZ, "mob.enderdragon.wings", 5.0F, 0.8F + this.rand.nextFloat() * 0.3F, false);
            }
        }

        this.prevAnimTime = this.animTime;

        if (this.getHealth() <= 0.0F)
        {
            float twentyFirstFloatValue = (this.rand.nextFloat() - 0.5F) * 8.0F;
            float secondFloatValue = (this.rand.nextFloat() - 0.5F) * 4.0F;
            float thirdFloatValue = (this.rand.nextFloat() - 0.5F) * 8.0F;
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (double)twentyFirstFloatValue, this.posY + 2.0D + (double)secondFloatValue, this.posZ + (double)thirdFloatValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }
        else
        {
            this.updateDragonEnderCrystal();
            float fourthFloatValue = 0.2F / (MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ) * 10.0F + 1.0F);
            fourthFloatValue = fourthFloatValue * (float)Math.pow(2.0D, this.motionY);

            if (this.slowed)
            {
                this.animTime += fourthFloatValue * 0.5F;
            }
            else
            {
                this.animTime += fourthFloatValue;
            }

            this.rotationYaw = MathHelper.wrapAngleTo180_float(this.rotationYaw);

            if (this.isAIDisabled())
            {
                this.animTime = 0.5F;
            }
            else
            {
                if (this.ringBufferIndex < 0)
                {
                    for (int i = 0; i < this.ringBuffer.length; ++i)
                    {
                        this.ringBuffer[i][0] = (double)this.rotationYaw;
                        this.ringBuffer[i][1] = this.posY;
                    }
                }

                if (++this.ringBufferIndex == this.ringBuffer.length)
                {
                    this.ringBufferIndex = 0;
                }

                this.ringBuffer[this.ringBufferIndex][0] = (double)this.rotationYaw;
                this.ringBuffer[this.ringBufferIndex][1] = this.posY;

                if (this.worldObj.isRemote)
                {
                    if (this.newPosRotationIncrements > 0)
                    {
                        double doubleValue = this.posX + (this.newPosX - this.posX) / (double)this.newPosRotationIncrements;
                        double secondDoubleValue = this.posY + (this.newPosY - this.posY) / (double)this.newPosRotationIncrements;
                        double thirdDoubleValue = this.posZ + (this.newPosZ - this.posZ) / (double)this.newPosRotationIncrements;
                        double fourthDoubleValue = MathHelper.wrapAngleTo180_double(this.newRotationYaw - (double)this.rotationYaw);
                        this.rotationYaw = (float)((double)this.rotationYaw + fourthDoubleValue / (double)this.newPosRotationIncrements);
                        this.rotationPitch = (float)((double)this.rotationPitch + (this.newRotationPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
                        --this.newPosRotationIncrements;
                        this.setPosition(doubleValue, secondDoubleValue, thirdDoubleValue);
                        this.setRotation(this.rotationYaw, this.rotationPitch);
                    }
                }
                else
                {
                    double fifthDoubleValue = this.targetX - this.posX;
                    double sixthDoubleValue = this.targetY - this.posY;
                    double seventhDoubleValue = this.targetZ - this.posZ;
                    double eighthDoubleValue = fifthDoubleValue * fifthDoubleValue + sixthDoubleValue * sixthDoubleValue + seventhDoubleValue * seventhDoubleValue;

                    if (this.target != null)
                    {
                        this.targetX = this.target.posX;
                        this.targetZ = this.target.posZ;
                        double ninthDoubleValue = this.targetX - this.posX;
                        double tenthDoubleValue = this.targetZ - this.posZ;
                        double eleventhDoubleValue = MathHelper.length_double(ninthDoubleValue, 0.0D, tenthDoubleValue);
                        double twelfthDoubleValue = 0.4000000059604645D + eleventhDoubleValue / 80.0D - 1.0D;

                        if (twelfthDoubleValue > 10.0D)
                        {
                            twelfthDoubleValue = 10.0D;
                        }

                        this.targetY = this.target.getEntityBoundingBox().minY + twelfthDoubleValue;
                    }
                    else
                    {
                        this.targetX += this.rand.nextGaussian() * 2.0D;
                        this.targetZ += this.rand.nextGaussian() * 2.0D;
                    }

                    if (this.forceNewTarget || eighthDoubleValue < 100.0D || eighthDoubleValue > 22500.0D || this.isCollidedHorizontally || this.isCollidedVertically)
                    {
                        this.setNewTarget();
                    }

                    sixthDoubleValue = sixthDoubleValue / (double)MathHelper.sqrt_double(fifthDoubleValue * fifthDoubleValue + seventhDoubleValue * seventhDoubleValue);
                    float fifthFloatValue = 0.6F;
                    sixthDoubleValue = MathHelper.clamp_double(sixthDoubleValue, (double)(-fifthFloatValue), (double)fifthFloatValue);
                    this.motionY += sixthDoubleValue * 0.10000000149011612D;
                    this.rotationYaw = MathHelper.wrapAngleTo180_float(this.rotationYaw);
                    double thirteenthDoubleValue = 180.0D - MathHelper.atan2(fifthDoubleValue, seventhDoubleValue) * 180.0D / Math.PI;
                    double fourteenthDoubleValue = MathHelper.wrapAngleTo180_double(thirteenthDoubleValue - (double)this.rotationYaw);

                    if (fourteenthDoubleValue > 50.0D)
                    {
                        fourteenthDoubleValue = 50.0D;
                    }

                    if (fourteenthDoubleValue < -50.0D)
                    {
                        fourteenthDoubleValue = -50.0D;
                    }

                    Vec3 vec3 = (new Vec3(this.targetX - this.posX, this.targetY - this.posY, this.targetZ - this.posZ)).normalize();
                    double fifteenthDoubleValue = (double)(-MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F));
                    Vec3 localValue = (new Vec3((double)MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F), this.motionY, fifteenthDoubleValue)).normalize();
                    float sixthFloatValue = ((float)localValue.dotProduct(vec3) + 0.5F) / 1.5F;

                    if (sixthFloatValue < 0.0F)
                    {
                        sixthFloatValue = 0.0F;
                    }

                    this.randomYawVelocity *= 0.8F;
                    float seventhFloatValue = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ) * 1.0F + 1.0F;
                    double sixteenthDoubleValue = MathHelper.length_double(this.motionX, this.motionZ) * 1.0D + 1.0D;

                    if (sixteenthDoubleValue > 40.0D)
                    {
                        sixteenthDoubleValue = 40.0D;
                    }

                    this.randomYawVelocity = (float)((double)this.randomYawVelocity + fourteenthDoubleValue * (0.699999988079071D / sixteenthDoubleValue / (double)seventhFloatValue));
                    this.rotationYaw += this.randomYawVelocity * 0.1F;
                    float eighthFloatValue = (float)(2.0D / (sixteenthDoubleValue + 1.0D));
                    float ninthFloatValue = 0.06F;
                    this.moveFlying(0.0F, -1.0F, ninthFloatValue * (sixthFloatValue * eighthFloatValue + (1.0F - eighthFloatValue)));

                    if (this.slowed)
                    {
                        this.moveEntity(this.motionX * 0.800000011920929D, this.motionY * 0.800000011920929D, this.motionZ * 0.800000011920929D);
                    }
                    else
                    {
                        this.moveEntity(this.motionX, this.motionY, this.motionZ);
                    }

                    Vec3 secondVec3 = (new Vec3(this.motionX, this.motionY, this.motionZ)).normalize();
                    float tenthFloatValue = ((float)secondVec3.dotProduct(localValue) + 1.0F) / 2.0F;
                    tenthFloatValue = 0.8F + 0.15F * tenthFloatValue;
                    this.motionX *= (double)tenthFloatValue;
                    this.motionZ *= (double)tenthFloatValue;
                    this.motionY *= 0.9100000262260437D;
                }

                this.renderYawOffset = this.rotationYaw;
                this.dragonPartHead.width = this.dragonPartHead.height = 3.0F;
                this.dragonPartTail1.width = this.dragonPartTail1.height = 2.0F;
                this.dragonPartTail2.width = this.dragonPartTail2.height = 2.0F;
                this.dragonPartTail3.width = this.dragonPartTail3.height = 2.0F;
                this.dragonPartBody.height = 3.0F;
                this.dragonPartBody.width = 5.0F;
                this.dragonPartWing1.height = 2.0F;
                this.dragonPartWing1.width = 4.0F;
                this.dragonPartWing2.height = 3.0F;
                this.dragonPartWing2.width = 4.0F;
                float eleventhFloatValue = (float)(this.getMovementOffsets(5, 1.0F)[1] - this.getMovementOffsets(10, 1.0F)[1]) * 10.0F / 180.0F * (float)Math.PI;
                float twelfthFloatValue = MathHelper.cos(eleventhFloatValue);
                float thirteenthFloatValue = -MathHelper.sin(eleventhFloatValue);
                float fourteenthFloatValue = this.rotationYaw * (float)Math.PI / 180.0F;
                float fifteenthFloatValue = MathHelper.sin(fourteenthFloatValue);
                float sixteenthFloatValue = MathHelper.cos(fourteenthFloatValue);
                this.dragonPartBody.onUpdate();
                this.dragonPartBody.setLocationAndAngles(this.posX + (double)(fifteenthFloatValue * 0.5F), this.posY, this.posZ - (double)(sixteenthFloatValue * 0.5F), 0.0F, 0.0F);
                this.dragonPartWing1.onUpdate();
                this.dragonPartWing1.setLocationAndAngles(this.posX + (double)(sixteenthFloatValue * 4.5F), this.posY + 2.0D, this.posZ + (double)(fifteenthFloatValue * 4.5F), 0.0F, 0.0F);
                this.dragonPartWing2.onUpdate();
                this.dragonPartWing2.setLocationAndAngles(this.posX - (double)(sixteenthFloatValue * 4.5F), this.posY + 2.0D, this.posZ - (double)(fifteenthFloatValue * 4.5F), 0.0F, 0.0F);

                if (!this.worldObj.isRemote && this.hurtTime == 0)
                {
                    this.collideWithEntities(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartWing1.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
                    this.collideWithEntities(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartWing2.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D)));
                    this.attackEntitiesInList(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.dragonPartHead.getEntityBoundingBox().expand(1.0D, 1.0D, 1.0D)));
                }

                double[] adouble1 = this.getMovementOffsets(5, 1.0F);
                double[] adouble = this.getMovementOffsets(0, 1.0F);
                float seventeenthFloatValue = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F - this.randomYawVelocity * 0.01F);
                float eighteenthFloatValue = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F - this.randomYawVelocity * 0.01F);
                this.dragonPartHead.onUpdate();
                this.dragonPartHead.setLocationAndAngles(this.posX + (double)(seventeenthFloatValue * 5.5F * twelfthFloatValue), this.posY + (adouble[1] - adouble1[1]) * 1.0D + (double)(thirteenthFloatValue * 5.5F), this.posZ - (double)(eighteenthFloatValue * 5.5F * twelfthFloatValue), 0.0F, 0.0F);

                for (int j = 0; j < 3; ++j)
                {
                    EntityDragonPart entitydragonpart = null;

                    if (j == 0)
                    {
                        entitydragonpart = this.dragonPartTail1;
                    }

                    if (j == 1)
                    {
                        entitydragonpart = this.dragonPartTail2;
                    }

                    if (j == 2)
                    {
                        entitydragonpart = this.dragonPartTail3;
                    }

                    double[] adouble2 = this.getMovementOffsets(12 + j * 2, 1.0F);
                    float nineteenthFloatValue = this.rotationYaw * (float)Math.PI / 180.0F + this.simplifyAngle(adouble2[0] - adouble1[0]) * (float)Math.PI / 180.0F * 1.0F;
                    float twentiethFloatValue = MathHelper.sin(nineteenthFloatValue);
                    float floatValue21 = MathHelper.cos(nineteenthFloatValue);
                    float floatValue22 = 1.5F;
                    float floatValue23 = (float)(j + 1) * 2.0F;
                    entitydragonpart.onUpdate();
                    entitydragonpart.setLocationAndAngles(this.posX - (double)((fifteenthFloatValue * floatValue22 + twentiethFloatValue * floatValue23) * twelfthFloatValue), this.posY + (adouble2[1] - adouble1[1]) * 1.0D - (double)((floatValue23 + floatValue22) * thirteenthFloatValue) + 1.5D, this.posZ + (double)((sixteenthFloatValue * floatValue22 + floatValue21 * floatValue23) * twelfthFloatValue), 0.0F, 0.0F);
                }

                if (!this.worldObj.isRemote)
                {
                    this.slowed = this.destroyBlocksInAABB(this.dragonPartHead.getEntityBoundingBox()) | this.destroyBlocksInAABB(this.dragonPartBody.getEntityBoundingBox());
                }
            }
        }
    }

    private void updateDragonEnderCrystal()
    {
        if (this.healingEnderCrystal != null)
        {
            if (this.healingEnderCrystal.isDead)
            {
                if (!this.worldObj.isRemote)
                {
                    this.attackEntityFromPart(this.dragonPartHead, DamageSource.setExplosionSource((Explosion)null), 10.0F);
                }

                this.healingEnderCrystal = null;
            }
            else if (this.ticksExisted % 10 == 0 && this.getHealth() < this.getMaxHealth())
            {
                this.setHealth(this.getHealth() + 1.0F);
            }
        }

        if (this.rand.nextInt(10) == 0)
        {
            float f = 32.0F;
            List<EntityEnderCrystal> list = this.worldObj.<EntityEnderCrystal>getEntitiesWithinAABB(EntityEnderCrystal.class, this.getEntityBoundingBox().expand((double)f, (double)f, (double)f));
            EntityEnderCrystal entityEnderCrystal = null;
            double doubleValue = Double.MAX_VALUE;

            for (EntityEnderCrystal entityendercrystal1 : list)
            {
                double doubleValue2 = entityendercrystal1.getDistanceSqToEntity(this);

                if (doubleValue2 < doubleValue)
                {
                    doubleValue = doubleValue2;
                    entityEnderCrystal = entityendercrystal1;
                }
            }

            this.healingEnderCrystal = entityEnderCrystal;
        }
    }

    private void collideWithEntities(List<Entity> entities)
    {
        double doubleValue = (this.dragonPartBody.getEntityBoundingBox().minX + this.dragonPartBody.getEntityBoundingBox().maxX) / 2.0D;
        double doubleValue2 = (this.dragonPartBody.getEntityBoundingBox().minZ + this.dragonPartBody.getEntityBoundingBox().maxZ) / 2.0D;

        for (Entity entity : entities)
        {
            if (entity instanceof EntityLivingBase)
            {
                double xCoordinate = entity.posX - doubleValue;
                double zCoordinate = entity.posZ - doubleValue2;
                double doubleValue3 = xCoordinate * xCoordinate + zCoordinate * zCoordinate;
                entity.addVelocity(xCoordinate / doubleValue3 * 4.0D, 0.20000000298023224D, zCoordinate / doubleValue3 * 4.0D);
            }
        }
    }

    private void attackEntitiesInList(List<Entity> entities)
    {
        for (int i = 0; i < entities.size(); ++i)
        {
            Entity entity = (Entity)entities.get(i);

            if (entity instanceof EntityLivingBase)
            {
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F);
                this.applyEnchantments(this, entity);
            }
        }
    }

    private void setNewTarget()
    {
        this.forceNewTarget = false;
        List<EntityPlayer> list = Lists.newArrayList(this.worldObj.playerEntities);
        Iterator<EntityPlayer> iterator = list.iterator();

        while (iterator.hasNext())
        {
            if (((EntityPlayer)iterator.next()).isSpectator())
            {
                iterator.remove();
            }
        }

        if (this.rand.nextInt(2) == 0 && !list.isEmpty())
        {
            this.target = (Entity)list.get(this.rand.nextInt(list.size()));
        }
        else
        {
            while (true)
            {
                this.targetX = 0.0D;
                this.targetY = (double)(70.0F + this.rand.nextFloat() * 50.0F);
                this.targetZ = 0.0D;
                this.targetX += (double)(this.rand.nextFloat() * 120.0F - 60.0F);
                this.targetZ += (double)(this.rand.nextFloat() * 120.0F - 60.0F);
                double xCoordinate = this.posX - this.targetX;
                double yCoordinate = this.posY - this.targetY;
                double zCoordinate = this.posZ - this.targetZ;
                boolean flag = xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate > 100.0D;

                if (flag)
                {
                    break;
                }
            }

            this.target = null;
        }
    }

    private float simplifyAngle(double angle)
    {
        return (float)MathHelper.wrapAngleTo180_double(angle);
    }

    private boolean destroyBlocksInAABB(AxisAlignedBB aabb)
    {
        int i = MathHelper.floor_double(aabb.minX);
        int j = MathHelper.floor_double(aabb.minY);
        int k = MathHelper.floor_double(aabb.minZ);
        int l = MathHelper.floor_double(aabb.maxX);
        int intValue2 = MathHelper.floor_double(aabb.maxY);
        int secondIntValue2 = MathHelper.floor_double(aabb.maxZ);
        boolean flag = false;
        boolean flag1 = false;

        for (int nestedIndex = i; nestedIndex <= l; ++nestedIndex)
        {
            for (int outerIndex = j; outerIndex <= intValue2; ++outerIndex)
            {
                for (int index = k; index <= secondIntValue2; ++index)
                {
                    BlockPos blockPos = new BlockPos(nestedIndex, outerIndex, index);
                    Block block = this.worldObj.getBlockState(blockPos).getBlock();

                    if (block.getMaterial() != Material.air)
                    {
                        if (block != Blocks.barrier && block != Blocks.obsidian && block != Blocks.end_stone && block != Blocks.bedrock && block != Blocks.command_block && this.worldObj.getGameRules().getBoolean("mobGriefing"))
                        {
                            flag1 = this.worldObj.setBlockToAir(blockPos) || flag1;
                        }
                        else
                        {
                            flag = true;
                        }
                    }
                }
            }
        }

        if (flag1)
        {
            double seventeenthDoubleValue = aabb.minX + (aabb.maxX - aabb.minX) * (double)this.rand.nextFloat();
            double eighteenthDoubleValue = aabb.minY + (aabb.maxY - aabb.minY) * (double)this.rand.nextFloat();
            double nineteenthDoubleValue = aabb.minZ + (aabb.maxZ - aabb.minZ) * (double)this.rand.nextFloat();
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, seventeenthDoubleValue, eighteenthDoubleValue, nineteenthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }

        return flag;
    }

    public boolean attackEntityFromPart(EntityDragonPart dragonPart, DamageSource source, float damage)
    {
        if (dragonPart != this.dragonPartHead)
        {
            damage = damage / 4.0F + 1.0F;
        }

        float f = this.rotationYaw * (float)Math.PI / 180.0F;
        float floatValue2 = MathHelper.sin(f);
        float floatValue3 = MathHelper.cos(f);
        this.targetX = this.posX + (double)(floatValue2 * 5.0F) + (double)((this.rand.nextFloat() - 0.5F) * 2.0F);
        this.targetY = this.posY + (double)(this.rand.nextFloat() * 3.0F) + 1.0D;
        this.targetZ = this.posZ - (double)(floatValue3 * 5.0F) + (double)((this.rand.nextFloat() - 0.5F) * 2.0F);
        this.target = null;

        if (source.getEntity() instanceof EntityPlayer || source.isExplosion())
        {
            this.attackDragonFrom(source, damage);
        }

        return true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage())
        {
            this.attackDragonFrom(source, amount);
        }

        return false;
    }

    protected boolean attackDragonFrom(DamageSource source, float amount)
    {
        return super.attackEntityFrom(source, amount);
    }

    public void onKillCommand()
    {
        this.setDead();
    }

    protected void onDeathUpdate()
    {
        ++this.deathTicks;

        if (this.deathTicks >= 180 && this.deathTicks <= 200)
        {
            float f = (this.rand.nextFloat() - 0.5F) * 8.0F;
            float floatValue2 = (this.rand.nextFloat() - 0.5F) * 4.0F;
            float floatValue3 = (this.rand.nextFloat() - 0.5F) * 8.0F;
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX + (double)f, this.posY + 2.0D + (double)floatValue2, this.posZ + (double)floatValue3, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }

        boolean flag = this.worldObj.getGameRules().getBoolean("doMobLoot");

        if (!this.worldObj.isRemote)
        {
            if (this.deathTicks > 150 && this.deathTicks % 5 == 0 && flag)
            {
                int i = 1000;

                while (i > 0)
                {
                    int k = EntityXPOrb.getXPSplit(i);
                    i -= k;
                    this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, k));
                }
            }

            if (this.deathTicks == 1)
            {
                this.worldObj.playBroadcastSound(1018, new BlockPos(this), 0);
            }
        }

        this.moveEntity(0.0D, 0.10000000149011612D, 0.0D);
        this.renderYawOffset = this.rotationYaw += 20.0F;

        if (this.deathTicks == 200 && !this.worldObj.isRemote)
        {
            if (flag)
            {
                int j = 2000;

                while (j > 0)
                {
                    int l = EntityXPOrb.getXPSplit(j);
                    j -= l;
                    this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, l));
                }
            }

            this.generatePortal(new BlockPos(this.posX, 64.0D, this.posZ));
            this.setDead();
        }
    }

    private void generatePortal(BlockPos pos)
    {
        int i = 4;
        double doubleValue = 12.25D;
        double doubleValue2 = 6.25D;

        for (int j = -1; j <= 32; ++j)
        {
            for (int k = -4; k <= 4; ++k)
            {
                for (int l = -4; l <= 4; ++l)
                {
                    double doubleValue3 = (double)(k * k + l * l);

                    if (doubleValue3 <= 12.25D)
                    {
                        BlockPos blockPos = pos.add(k, j, l);

                        if (j < 0)
                        {
                            if (doubleValue3 <= 6.25D)
                            {
                                this.worldObj.setBlockState(blockPos, Blocks.bedrock.getDefaultState());
                            }
                        }
                        else if (j > 0)
                        {
                            this.worldObj.setBlockState(blockPos, Blocks.air.getDefaultState());
                        }
                        else if (doubleValue3 > 6.25D)
                        {
                            this.worldObj.setBlockState(blockPos, Blocks.bedrock.getDefaultState());
                        }
                        else
                        {
                            this.worldObj.setBlockState(blockPos, Blocks.end_portal.getDefaultState());
                        }
                    }
                }
            }
        }

        this.worldObj.setBlockState(pos, Blocks.bedrock.getDefaultState());
        this.worldObj.setBlockState(pos.up(), Blocks.bedrock.getDefaultState());
        BlockPos blockpos1 = pos.up(2);
        this.worldObj.setBlockState(blockpos1, Blocks.bedrock.getDefaultState());
        this.worldObj.setBlockState(blockpos1.west(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST));
        this.worldObj.setBlockState(blockpos1.east(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST));
        this.worldObj.setBlockState(blockpos1.north(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));
        this.worldObj.setBlockState(blockpos1.south(), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
        this.worldObj.setBlockState(pos.up(3), Blocks.bedrock.getDefaultState());
        this.worldObj.setBlockState(pos.up(4), Blocks.dragon_egg.getDefaultState());
    }

    protected void despawnEntity()
    {
    }

    public Entity[] getParts()
    {
        return this.dragonPartArray;
    }

    public boolean canBeCollidedWith()
    {
        return false;
    }

    public World getWorld()
    {
        return this.worldObj;
    }

    protected String getLivingSound()
    {
        return "mob.enderdragon.growl";
    }

    protected String getHurtSound()
    {
        return "mob.enderdragon.hit";
    }

    protected float getSoundVolume()
    {
        return 5.0F;
    }
}
