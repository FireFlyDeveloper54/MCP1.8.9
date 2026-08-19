package net.minecraft.entity.projectile;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityFishHook extends Entity
{
    private static final List<WeightedRandomFishable> JUNK = Arrays.<WeightedRandomFishable>asList(new WeightedRandomFishable[] {(new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).setMaxDamagePercent(0.9F), new WeightedRandomFishable(new ItemStack(Items.leather), 10), new WeightedRandomFishable(new ItemStack(Items.bone), 10), new WeightedRandomFishable(new ItemStack(Items.potionitem), 10), new WeightedRandomFishable(new ItemStack(Items.string), 5), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).setMaxDamagePercent(0.9F), new WeightedRandomFishable(new ItemStack(Items.bowl), 10), new WeightedRandomFishable(new ItemStack(Items.stick), 5), new WeightedRandomFishable(new ItemStack(Items.dye, 10, EnumDyeColor.BLACK.getDyeDamage()), 1), new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10), new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10)});
    private static final List<WeightedRandomFishable> TREASURE = Arrays.<WeightedRandomFishable>asList(new WeightedRandomFishable[] {new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1), new WeightedRandomFishable(new ItemStack(Items.name_tag), 1), new WeightedRandomFishable(new ItemStack(Items.saddle), 1), (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).setMaxDamagePercent(0.25F).setEnchantable(), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).setMaxDamagePercent(0.25F).setEnchantable(), (new WeightedRandomFishable(new ItemStack(Items.book), 1)).setEnchantable()});
    private static final List<WeightedRandomFishable> FISH = Arrays.<WeightedRandomFishable>asList(new WeightedRandomFishable[] {new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.getMetadata()), 60), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.getMetadata()), 25), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.getMetadata()), 2), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.getMetadata()), 13)});
    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private boolean inGround;
    public int shake;
    public EntityPlayer angler;
    private int ticksInGround;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    public Entity caughtEntity;
    private int fishPosRotationIncrements;
    private double fishX;
    private double fishY;
    private double fishZ;
    private double fishYaw;
    private double fishPitch;
    private double clientMotionX;
    private double clientMotionY;
    private double clientMotionZ;
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();

    public static List<WeightedRandomFishable> getFish()
    {
        return FISH;
    }

    public EntityFishHook(World worldIn)
    {
        super(worldIn);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
    }

    public EntityFishHook(World worldIn, double x, double y, double z, EntityPlayer anglerIn)
    {
        this(worldIn);
        this.setPosition(x, y, z);
        this.ignoreFrustumCheck = true;
        this.angler = anglerIn;
        anglerIn.fishEntity = this;
    }

    public EntityFishHook(World worldIn, EntityPlayer fishingPlayer)
    {
        super(worldIn);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.ignoreFrustumCheck = true;
        this.angler = fishingPlayer;
        this.angler.fishEntity = this;
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(fishingPlayer.posX, fishingPlayer.posY + (double)fishingPlayer.getEyeHeight(), fishingPlayer.posZ, fishingPlayer.rotationYaw, fishingPlayer.rotationPitch);
        float[] yawSC = new float[2];
        float[] pitchSC = new float[2];
        MathHelper.sinCosDeg(this.rotationYaw, yawSC);
        MathHelper.sinCosDeg(this.rotationPitch, pitchSC);
        this.posX -= (double)(yawSC[1] * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(yawSC[0] * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        float f = 0.4F;
        this.motionX = (double)(-yawSC[0] * pitchSC[1] * f);
        this.motionZ = (double)(yawSC[1] * pitchSC[1] * f);
        this.motionY = (double)(-pitchSC[0] * f);
        this.handleHookCasting(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
    }

    protected void entityInit()
    {
    }

    public boolean isInRangeToRenderDist(double distance)
    {
        double doubleValue = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(doubleValue))
        {
            doubleValue = 4.0D;
        }

        doubleValue = doubleValue * 64.0D;
        return distance < doubleValue * doubleValue;
    }

    public void handleHookCasting(double motionX, double motionY, double motionZ, float velocity, float inaccuracy)
    {
        float f = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX = motionX / (double)f;
        motionY = motionY / (double)f;
        motionZ = motionZ / (double)f;
        motionX = motionX + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        motionY = motionY + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        motionZ = motionZ + this.rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        motionX = motionX * (double)velocity;
        motionY = motionY * (double)velocity;
        motionZ = motionZ * (double)velocity;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        float floatValue2 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(motionY, (double)floatValue2) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.fishX = x;
        this.fishY = y;
        this.fishZ = z;
        this.fishYaw = (double)yaw;
        this.fishPitch = (double)pitch;
        this.fishPosRotationIncrements = posRotationIncrements;
        this.motionX = this.clientMotionX;
        this.motionY = this.clientMotionY;
        this.motionZ = this.clientMotionZ;
    }

    public void setVelocity(double x, double y, double z)
    {
        this.clientMotionX = this.motionX = x;
        this.clientMotionY = this.motionY = y;
        this.clientMotionZ = this.motionZ = z;
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (this.fishPosRotationIncrements > 0)
        {
            double doubleValue = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
            double secondDoubleValue = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
            double thirdDoubleValue = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
            double fourthDoubleValue = MathHelper.wrapAngleTo180_double(this.fishYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + fourthDoubleValue / (double)this.fishPosRotationIncrements);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
            --this.fishPosRotationIncrements;
            this.setPosition(doubleValue, secondDoubleValue, thirdDoubleValue);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                ItemStack itemstack = this.angler.getCurrentEquippedItem();

                if (this.angler.isDead || !this.angler.isEntityAlive() || itemstack == null || itemstack.getItem() != Items.fishing_rod || this.getDistanceSqToEntity(this.angler) > 1024.0D)
                {
                    this.setDead();
                    this.angler.fishEntity = null;
                    return;
                }

                if (this.caughtEntity != null)
                {
                    if (!this.caughtEntity.isDead)
                    {
                        this.posX = this.caughtEntity.posX;
                        double fifthDoubleValue = (double)this.caughtEntity.height;
                        this.posY = this.caughtEntity.getEntityBoundingBox().minY + fifthDoubleValue * 0.8D;
                        this.posZ = this.caughtEntity.posZ;
                        return;
                    }

                    this.caughtEntity = null;
                }
            }

            if (this.shake > 0)
            {
                --this.shake;
            }

            if (this.inGround)
            {
                if (this.worldObj.getBlockState(this.blockSamplePos.set(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile)
                {
                    ++this.ticksInGround;

                    if (this.ticksInGround == 1200)
                    {
                        this.setDead();
                    }

                    return;
                }

                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
            else
            {
                ++this.ticksInAir;
            }

            Vec3 currentPosition = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 nextPosition = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(currentPosition, nextPosition);

            if (movingobjectposition != null)
            {
                nextPosition = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double sixthDoubleValue = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != this.angler || this.ticksInAir >= 5))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(currentPosition, nextPosition);

                    if (movingobjectposition1 != null)
                    {
                        double seventhDoubleValue = currentPosition.squareDistanceTo(movingobjectposition1.hitVec);

                        if (seventhDoubleValue < sixthDoubleValue || sixthDoubleValue == 0.0D)
                        {
                            entity = entity1;
                            sixthDoubleValue = seventhDoubleValue;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.angler), 0.0F))
                    {
                        this.caughtEntity = movingobjectposition.entityHit;
                    }
                }
                else
                {
                    this.inGround = true;
                }
            }

            if (!this.inGround)
            {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                float floatValue = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

                for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)floatValue) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
                {
                    ;
                }

                while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
                {
                    this.prevRotationPitch += 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw < -180.0F)
                {
                    this.prevRotationYaw -= 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
                {
                    this.prevRotationYaw += 360.0F;
                }

                this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
                this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
                float secondFloatValue = 0.92F;

                if (this.onGround || this.isCollidedHorizontally)
                {
                    secondFloatValue = 0.5F;
                }

                int j = 5;
                double eighthDoubleValue = 0.0D;

                for (int k = 0; k < j; ++k)
                {
                    AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
                    double ninthDoubleValue = axisalignedbb1.maxY - axisalignedbb1.minY;
                    double sixteenthDoubleValue = axisalignedbb1.minY + ninthDoubleValue * (double)k / (double)j;
                    double seventeenthDoubleValue = axisalignedbb1.minY + ninthDoubleValue * (double)(k + 1) / (double)j;
                    AxisAlignedBB axisalignedbb2 = new AxisAlignedBB(axisalignedbb1.minX, sixteenthDoubleValue, axisalignedbb1.minZ, axisalignedbb1.maxX, seventeenthDoubleValue, axisalignedbb1.maxZ);

                    if (this.worldObj.isAABBInMaterial(axisalignedbb2, Material.water))
                    {
                        eighthDoubleValue += 1.0D / (double)j;
                    }
                }

                if (!this.worldObj.isRemote && eighthDoubleValue > 0.0D)
                {
                    WorldServer worldserver = (WorldServer)this.worldObj;
                    int l = 1;
                    BlockPos blockpos = new BlockPos(this.posX, this.posY + 1.0D, this.posZ);

                    if (this.rand.nextFloat() < 0.25F && this.worldObj.isRainingAt(blockpos))
                    {
                        l = 2;
                    }

                    if (this.rand.nextFloat() < 0.5F && !this.worldObj.canSeeSky(blockpos))
                    {
                        --l;
                    }

                    if (this.ticksCatchable > 0)
                    {
                        --this.ticksCatchable;

                        if (this.ticksCatchable <= 0)
                        {
                            this.ticksCaughtDelay = 0;
                            this.ticksCatchableDelay = 0;
                        }
                    }
                    else if (this.ticksCatchableDelay > 0)
                    {
                        this.ticksCatchableDelay -= l;

                        if (this.ticksCatchableDelay <= 0)
                        {
                            this.motionY -= 0.20000000298023224D;
                            this.playSound("random.splash", 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                            float thirdFloatValue = (float)MathHelper.floor_double(this.getEntityBoundingBox().minY);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(thirdFloatValue + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, EnumParticleTypes.EMPTY_ARGS);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(thirdFloatValue + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, EnumParticleTypes.EMPTY_ARGS);
                            this.ticksCatchable = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                        }
                        else
                        {
                            this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                            float fourthFloatValue = this.fishApproachAngle * 0.017453292F;
                            float fifthFloatValue = MathHelper.sin(fourthFloatValue);
                            float sixthFloatValue = MathHelper.cos(fourthFloatValue);
                            double twelfthDoubleValue = this.posX + (double)(fifthFloatValue * (float)this.ticksCatchableDelay * 0.1F);
                            double fourteenthDoubleValue = (double)((float)MathHelper.floor_double(this.getEntityBoundingBox().minY) + 1.0F);
                            double fifteenthDoubleValue = this.posZ + (double)(sixthFloatValue * (float)this.ticksCatchableDelay * 0.1F);
                            Block block1 = worldserver.getBlockState(this.blockSamplePos.set((int)twelfthDoubleValue, (int)fourteenthDoubleValue - 1, (int)fifteenthDoubleValue)).getBlock();

                            if (block1 == Blocks.water || block1 == Blocks.flowing_water)
                            {
                                if (this.rand.nextFloat() < 0.15F)
                                {
                                    worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, twelfthDoubleValue, fourteenthDoubleValue - 0.10000000149011612D, fifteenthDoubleValue, 1, (double)fifthFloatValue, 0.1D, (double)sixthFloatValue, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                                }

                                float eleventhFloatValue = fifthFloatValue * 0.04F;
                                float seventhFloatValue = sixthFloatValue * 0.04F;
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, twelfthDoubleValue, fourteenthDoubleValue, fifteenthDoubleValue, 0, (double)seventhFloatValue, 0.01D, (double)(-eleventhFloatValue), 1.0D, EnumParticleTypes.EMPTY_ARGS);
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, twelfthDoubleValue, fourteenthDoubleValue, fifteenthDoubleValue, 0, (double)(-seventhFloatValue), 0.01D, (double)eleventhFloatValue, 1.0D, EnumParticleTypes.EMPTY_ARGS);
                            }
                        }
                    }
                    else if (this.ticksCaughtDelay > 0)
                    {
                        this.ticksCaughtDelay -= l;
                        float eighthFloatValue = 0.15F;

                        if (this.ticksCaughtDelay < 20)
                        {
                            eighthFloatValue = (float)((double)eighthFloatValue + (double)(20 - this.ticksCaughtDelay) * 0.05D);
                        }
                        else if (this.ticksCaughtDelay < 40)
                        {
                            eighthFloatValue = (float)((double)eighthFloatValue + (double)(40 - this.ticksCaughtDelay) * 0.02D);
                        }
                        else if (this.ticksCaughtDelay < 60)
                        {
                            eighthFloatValue = (float)((double)eighthFloatValue + (double)(60 - this.ticksCaughtDelay) * 0.01D);
                        }

                        if (this.rand.nextFloat() < eighthFloatValue)
                        {
                            float ninthFloatValue = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292F;
                            float tenthFloatValue = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
                            double eleventhDoubleValue = this.posX + (double)(MathHelper.sin(ninthFloatValue) * tenthFloatValue * 0.1F);
                            double thirteenthDoubleValue = (double)((float)MathHelper.floor_double(this.getEntityBoundingBox().minY) + 1.0F);
                            double eighteenthDoubleValue = this.posZ + (double)(MathHelper.cos(ninthFloatValue) * tenthFloatValue * 0.1F);
                            Block block = worldserver.getBlockState(this.blockSamplePos.set((int)eleventhDoubleValue, (int)thirteenthDoubleValue - 1, (int)eighteenthDoubleValue)).getBlock();

                            if (block == Blocks.water || block == Blocks.flowing_water)
                            {
                                worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, eleventhDoubleValue, thirteenthDoubleValue, eighteenthDoubleValue, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                            }
                        }

                        if (this.ticksCaughtDelay <= 0)
                        {
                            this.fishApproachAngle = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                            this.ticksCatchableDelay = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                        }
                    }
                    else
                    {
                        this.ticksCaughtDelay = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                        this.ticksCaughtDelay -= EnchantmentHelper.getLureModifier(this.angler) * 20 * 5;
                    }

                    if (this.ticksCatchable > 0)
                    {
                        this.motionY -= (double)(this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat()) * 0.2D;
                    }
                }

                double tenthDoubleValue = eighthDoubleValue * 2.0D - 1.0D;
                this.motionY += 0.03999999910593033D * tenthDoubleValue;

                if (eighthDoubleValue > 0.0D)
                {
                    secondFloatValue = (float)((double)secondFloatValue * 0.9D);
                    this.motionY *= 0.8D;
                }

                this.motionX *= (double)secondFloatValue;
                this.motionY *= (double)secondFloatValue;
                this.motionZ *= (double)secondFloatValue;
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("xTile", (short)this.xTile);
        tagCompound.setShort("yTile", (short)this.yTile);
        tagCompound.setShort("zTile", (short)this.zTile);
        ResourceLocation resourceLocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.inTile);
        tagCompound.setString("inTile", resourceLocation == null ? "" : resourceLocation.toString());
        tagCompound.setByte("shake", (byte)this.shake);
        tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        this.xTile = tagCompund.getShort("xTile");
        this.yTile = tagCompund.getShort("yTile");
        this.zTile = tagCompund.getShort("zTile");

        if (tagCompund.hasKey("inTile", 8))
        {
            this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        }
        else
        {
            this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        this.shake = tagCompund.getByte("shake") & 255;
        this.inGround = tagCompund.getByte("inGround") == 1;
    }

    public int handleHookRetraction()
    {
        if (this.worldObj.isRemote)
        {
            return 0;
        }
        else
        {
            int i = 0;

            if (this.caughtEntity != null)
            {
                double xCoordinate = this.angler.posX - this.posX;
                double yCoordinate = this.angler.posY - this.posY;
                double zCoordinate = this.angler.posZ - this.posZ;
                double doubleValue = (double)MathHelper.sqrt_double(xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate);
                double doubleValue2 = 0.1D;
                this.caughtEntity.motionX += xCoordinate * doubleValue2;
                this.caughtEntity.motionY += yCoordinate * doubleValue2 + (double)MathHelper.sqrt_double(doubleValue) * 0.08D;
                this.caughtEntity.motionZ += zCoordinate * doubleValue2;
                i = 3;
            }
            else if (this.ticksCatchable > 0)
            {
                EntityItem entityItem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getFishingResult());
                double xCoordinate2 = this.angler.posX - this.posX;
                double yCoordinate2 = this.angler.posY - this.posY;
                double zCoordinate2 = this.angler.posZ - this.posZ;
                double doubleValue3 = (double)MathHelper.sqrt_double(xCoordinate2 * xCoordinate2 + yCoordinate2 * yCoordinate2 + zCoordinate2 * zCoordinate2);
                double doubleValue4 = 0.1D;
                entityItem.motionX = xCoordinate2 * doubleValue4;
                entityItem.motionY = yCoordinate2 * doubleValue4 + (double)MathHelper.sqrt_double(doubleValue3) * 0.08D;
                entityItem.motionZ = zCoordinate2 * doubleValue4;
                this.worldObj.spawnEntityInWorld(entityItem);
                this.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(this.angler.worldObj, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
                i = 1;
            }

            if (this.inGround)
            {
                i = 2;
            }

            this.setDead();
            this.angler.fishEntity = null;
            return i;
        }
    }

    private ItemStack getFishingResult()
    {
        float f = this.worldObj.rand.nextFloat();
        int i = EnchantmentHelper.getLuckOfSeaModifier(this.angler);
        int j = EnchantmentHelper.getLureModifier(this.angler);
        float floatValue2 = 0.1F - (float)i * 0.025F - (float)j * 0.01F;
        float floatValue3 = 0.05F + (float)i * 0.01F - (float)j * 0.01F;
        floatValue2 = MathHelper.clamp_float(floatValue2, 0.0F, 1.0F);
        floatValue3 = MathHelper.clamp_float(floatValue3, 0.0F, 1.0F);

        if (f < floatValue2)
        {
            this.angler.triggerAchievement(StatList.junkFishedStat);
            return ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, JUNK)).getItemStack(this.rand);
        }
        else
        {
            f = f - floatValue2;

            if (f < floatValue3)
            {
                this.angler.triggerAchievement(StatList.treasureFishedStat);
                return ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, TREASURE)).getItemStack(this.rand);
            }
            else
            {
                float floatValue4 = f - floatValue3;
                this.angler.triggerAchievement(StatList.fishCaughtStat);
                return ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, FISH)).getItemStack(this.rand);
            }
        }
    }

    public void setDead()
    {
        super.setDead();

        if (this.angler != null)
        {
            this.angler.fishEntity = null;
        }
    }
}
