package net.minecraft.entity;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;


import optimization.entityCulling.EntityCulling;
public abstract class Entity implements ICommandSender
{
    private static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private static int nextEntityID;
    private int entityId;
    public double renderDistanceWeight;
    public boolean preventEntitySpawning;
    public Entity riddenByEntity;
    public Entity ridingEntity;
    public boolean forceSpawn;
    public World worldObj;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public float rotationYaw;
    public float rotationPitch;
    public float prevRotationYaw;
    public float prevRotationPitch;
    private AxisAlignedBB boundingBox;
    public boolean onGround;
    public boolean isCollidedHorizontally;
    public boolean isCollidedVertically;
    public boolean isCollided;
    public boolean velocityChanged;
    protected boolean isInWeb;
    private boolean isOutsideBorder;
    public boolean isDead;
    public float width;
    public float height;
    public float prevDistanceWalkedModified;
    public float distanceWalkedModified;
    public float distanceWalkedOnStepModified;
    public float fallDistance;
    private int nextStepDistance;
    public double lastTickPosX;
    public double lastTickPosY;
    public double lastTickPosZ;
    public float stepHeight;
    public boolean noClip;
    public float entityCollisionReduction;
    protected Random rand;
    public int ticksExisted;
    public int fireResistance;
    private int fire;
    protected boolean inWater;
    public int hurtResistantTime;
    protected boolean firstUpdate;
    protected boolean isImmuneToFire;
    protected DataWatcher dataWatcher;
    private double entityRiderPitchDelta;
    private double entityRiderYawDelta;
    public boolean addedToChunk;
    public int chunkCoordX;
    public int chunkCoordY;
    public int chunkCoordZ;
    public int serverPosX;
    public int serverPosY;
    public int serverPosZ;
    public boolean ignoreFrustumCheck;
    public boolean isAirBorne;
    public int timeUntilPortal;
    protected boolean inPortal;
    protected int portalCounter;
    public int dimension;
    protected BlockPos lastPortalPos;
    protected Vec3 lastPortalVec;
    protected EnumFacing teleportDirection;
    private boolean invulnerable;
    protected UUID entityUniqueID;
    private final CommandResultStats cmdResultStats;
    private BlockPos.MutableBlockPos opaqueBlockCheckPos;
    private BlockPos.MutableBlockPos wetCheckPos;
    private BlockPos.MutableBlockPos collisionMinPos;
    private BlockPos.MutableBlockPos collisionMaxPos;
    private BlockPos.MutableBlockPos collisionCheckPos;
    private BlockPos.MutableBlockPos brightnessCheckPos;
    private BlockPos.MutableBlockPos materialCheckPos;
    private boolean processingBlockCollisions;
    private IChatComponent displayNameCache;
    private long displayNameCacheExpiresAt;

    private boolean culled = false;
    private boolean outOfCamera = false;
    private long cullingTimeout = 0;
    public int getEntityId()
    {
        return this.entityId;
    }

    public boolean isForcedVisible()
    {
        return this.cullingTimeout > System.currentTimeMillis();
    }

    public void setCulled(boolean value)
    {
        this.culled = value;
        if (!value)
        {
            this.cullingTimeout = System.currentTimeMillis() + 1000;
        }
    }

    public boolean isCulled()
    {
        if (!EntityCulling.instance.isEnabled()) return false;
        return this.culled;
    }

    public void setOutOfCamera(boolean value)
    {
        this.outOfCamera = value;
    }

    public boolean isOutOfCamera()
    {
        if (!EntityCulling.instance.isEnabled()) return false;
        return this.outOfCamera;
    }

    public void setEntityId(int id)
    {
        this.entityId = id;
    }

    public void onKillCommand()
    {
        this.setDead();
    }

    public Entity(World worldIn)
    {
        this.entityId = nextEntityID++;
        this.renderDistanceWeight = 1.0D;
        this.boundingBox = ZERO_AABB;
        this.width = 0.6F;
        this.height = 1.8F;
        this.nextStepDistance = 1;
        this.rand = new Random();
        this.fireResistance = 1;
        this.firstUpdate = true;
        this.entityUniqueID = MathHelper.getRandomUuid(this.rand);
        this.cmdResultStats = new CommandResultStats();
        this.worldObj = worldIn;
        this.setPosition(0.0D, 0.0D, 0.0D);

        if (worldIn != null)
        {
            this.dimension = worldIn.provider.getDimensionId();
        }

        this.dataWatcher = new DataWatcher(this);
        this.dataWatcher.addObject(0, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(1, Short.valueOf((short)300));
        this.dataWatcher.addObject(3, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(2, "");
        this.dataWatcher.addObject(4, Byte.valueOf((byte)0));
        this.entityInit();
    }

    protected abstract void entityInit();

    public DataWatcher getDataWatcher()
    {
        return this.dataWatcher;
    }

    public boolean equals(Object other)
    {
        return other instanceof Entity ? ((Entity)other).entityId == this.entityId : false;
    }

    public int hashCode()
    {
        return this.entityId;
    }

    protected void preparePlayerToSpawn()
    {
        if (this.worldObj != null)
        {
            while (this.posY > 0.0D && this.posY < 256.0D)
            {
                this.setPosition(this.posX, this.posY, this.posZ);

                if (this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty())
                {
                    break;
                }

                ++this.posY;
            }

            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.rotationPitch = 0.0F;
        }
    }

    public void setDead()
    {
        this.isDead = true;
    }

    protected void setSize(float width, float height)
    {
        if (width != this.width || height != this.height)
        {
            float f = this.width;
            this.width = width;
            this.height = height;
            this.setEntityBoundingBox(new AxisAlignedBB(this.getEntityBoundingBox().minX, this.getEntityBoundingBox().minY, this.getEntityBoundingBox().minZ, this.getEntityBoundingBox().minX + (double)this.width, this.getEntityBoundingBox().minY + (double)this.height, this.getEntityBoundingBox().minZ + (double)this.width));

            if (this.width > f && !this.firstUpdate && !this.worldObj.isRemote)
            {
                this.moveEntity((double)(f - this.width), 0.0D, (double)(f - this.width));
            }
        }
    }

    protected void setRotation(float yaw, float pitch)
    {
        this.rotationYaw = yaw % 360.0F;
        this.rotationPitch = pitch % 360.0F;
    }

    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float f = this.width / 2.0F;
        float floatValue2 = this.height;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)floatValue2, z + (double)f));
    }

    public void setAngles(float yaw, float pitch)
    {
        float f = this.rotationPitch;
        float floatValue2 = this.rotationYaw;
        this.rotationYaw = (float)((double)this.rotationYaw + (double)yaw * 0.15D);
        this.rotationPitch = (float)((double)this.rotationPitch - (double)pitch * 0.15D);
        this.rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90.0F, 90.0F);
        this.prevRotationPitch += this.rotationPitch - f;
        this.prevRotationYaw += this.rotationYaw - floatValue2;
    }

    public void onUpdate()
    {
        this.onEntityUpdate();
    }

    public void onEntityUpdate()
    {
        this.worldObj.theProfiler.startSection("entityBaseTick");

        if (this.ridingEntity != null && this.ridingEntity.isDead)
        {
            this.ridingEntity = null;
        }

        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer)
        {
            this.worldObj.theProfiler.startSection("portal");
            MinecraftServer minecraftServer = ((WorldServer)this.worldObj).getMinecraftServer();
            int i = this.getMaxInPortalTime();

            if (this.inPortal)
            {
                if (minecraftServer.getAllowNether())
                {
                    if (this.ridingEntity == null && this.portalCounter++ >= i)
                    {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        int j;

                        if (this.worldObj.provider.getDimensionId() == -1)
                        {
                            j = 0;
                        }
                        else
                        {
                            j = -1;
                        }

                        this.travelToDimension(j);
                    }

                    this.inPortal = false;
                }
            }
            else
            {
                if (this.portalCounter > 0)
                {
                    this.portalCounter -= 4;
                }

                if (this.portalCounter < 0)
                {
                    this.portalCounter = 0;
                }
            }

            if (this.timeUntilPortal > 0)
            {
                --this.timeUntilPortal;
            }

            this.worldObj.theProfiler.endSection();
        }

        this.spawnRunningParticles();
        this.handleWaterMovement();

        if (this.worldObj.isRemote)
        {
            this.fire = 0;
        }
        else if (this.fire > 0)
        {
            if (this.isImmuneToFire)
            {
                this.fire -= 4;

                if (this.fire < 0)
                {
                    this.fire = 0;
                }
            }
            else
            {
                if (this.fire % 20 == 0)
                {
                    this.attackEntityFrom(DamageSource.onFire, 1.0F);
                }

                --this.fire;
            }
        }

        if (this.isInLava())
        {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5F;
        }

        if (this.posY < -64.0D)
        {
            this.kill();
        }

        if (!this.worldObj.isRemote)
        {
            this.setFlag(0, this.fire > 0);
        }

        this.firstUpdate = false;
        this.worldObj.theProfiler.endSection();
    }

    public int getMaxInPortalTime()
    {
        return 0;
    }

    protected void setOnFireFromLava()
    {
        if (!this.isImmuneToFire)
        {
            this.attackEntityFrom(DamageSource.lava, 4.0F);
            this.setFire(15);
        }
    }

    public void setFire(int seconds)
    {
        int i = seconds * 20;
        i = EnchantmentProtection.getFireTimeForEntity(this, i);

        if (this.fire < i)
        {
            this.fire = i;
        }
    }

    public void extinguish()
    {
        this.fire = 0;
    }

    protected void kill()
    {
        this.setDead();
    }

    public boolean isOffsetPositionInLiquid(double x, double y, double z)
    {
        AxisAlignedBB axisAlignedBB = this.getEntityBoundingBox().offset(x, y, z);
        return this.isLiquidPresentInAABB(axisAlignedBB);
    }

    private boolean isLiquidPresentInAABB(AxisAlignedBB bb)
    {
        return this.worldObj.getCollidingBoundingBoxes(this, bb).isEmpty() && !this.worldObj.isAnyLiquid(bb);
    }

    public void moveEntity(double x, double y, double z)
    {
        if (this.noClip)
        {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        }
        else
        {
            this.worldObj.theProfiler.startSection("move");
            double doubleValue = this.posX;
            double secondDoubleValue = this.posY;
            double thirdDoubleValue = this.posZ;

            if (this.isInWeb)
            {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double twentySixthDoubleValue = x;
            double fourthDoubleValue = y;
            double fifthDoubleValue = z;
            boolean flag = this.onGround && this.isSneaking() && this instanceof EntityPlayer;

            if (flag)
            {
                double sixthDoubleValue;

                for (sixthDoubleValue = 0.05D; x != 0.0D && this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().offset(x, -1.0D, 0.0D)).isEmpty(); twentySixthDoubleValue = x)
                {
                    if (x < sixthDoubleValue && x >= -sixthDoubleValue)
                    {
                        x = 0.0D;
                    }
                    else if (x > 0.0D)
                    {
                        x -= sixthDoubleValue;
                    }
                    else
                    {
                        x += sixthDoubleValue;
                    }
                }

                for (; z != 0.0D && this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().offset(0.0D, -1.0D, z)).isEmpty(); fifthDoubleValue = z)
                {
                    if (z < sixthDoubleValue && z >= -sixthDoubleValue)
                    {
                        z = 0.0D;
                    }
                    else if (z > 0.0D)
                    {
                        z -= sixthDoubleValue;
                    }
                    else
                    {
                        z += sixthDoubleValue;
                    }
                }

                for (; x != 0.0D && z != 0.0D && this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().offset(x, -1.0D, z)).isEmpty(); fifthDoubleValue = z)
                {
                    if (x < sixthDoubleValue && x >= -sixthDoubleValue)
                    {
                        x = 0.0D;
                    }
                    else if (x > 0.0D)
                    {
                        x -= sixthDoubleValue;
                    }
                    else
                    {
                        x += sixthDoubleValue;
                    }

                    twentySixthDoubleValue = x;

                    if (z < sixthDoubleValue && z >= -sixthDoubleValue)
                    {
                        z = 0.0D;
                    }
                    else if (z > 0.0D)
                    {
                        z -= sixthDoubleValue;
                    }
                    else
                    {
                        z += sixthDoubleValue;
                    }
                }
            }

            List<AxisAlignedBB> list1 = this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().addCoord(x, y, z));
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

            for (AxisAlignedBB axisalignedbb1 : list1)
            {
                y = axisalignedbb1.calculateYOffset(this.getEntityBoundingBox(), y);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            boolean flag1 = this.onGround || fourthDoubleValue != y && fourthDoubleValue < 0.0D;

            for (AxisAlignedBB axisalignedbb2 : list1)
            {
                x = axisalignedbb2.calculateXOffset(this.getEntityBoundingBox(), x);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb13 : list1)
            {
                z = axisalignedbb13.calculateZOffset(this.getEntityBoundingBox(), z);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));

            if (this.stepHeight > 0.0F && flag1 && (twentySixthDoubleValue != x || fifthDoubleValue != z))
            {
                double seventhDoubleValue = x;
                double eighthDoubleValue = y;
                double ninthDoubleValue = z;
                AxisAlignedBB axisalignedbb3 = this.getEntityBoundingBox();
                this.setEntityBoundingBox(axisalignedbb);
                y = (double)this.stepHeight;
                List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().addCoord(twentySixthDoubleValue, y, fifthDoubleValue));
                AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
                AxisAlignedBB axisalignedbb5 = axisalignedbb4.addCoord(twentySixthDoubleValue, 0.0D, fifthDoubleValue);
                double tenthDoubleValue = y;

                for (AxisAlignedBB axisalignedbb6 : list)
                {
                    tenthDoubleValue = axisalignedbb6.calculateYOffset(axisalignedbb5, tenthDoubleValue);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, tenthDoubleValue, 0.0D);
                double eleventhDoubleValue = twentySixthDoubleValue;

                for (AxisAlignedBB axisalignedbb7 : list)
                {
                    eleventhDoubleValue = axisalignedbb7.calculateXOffset(axisalignedbb4, eleventhDoubleValue);
                }

                axisalignedbb4 = axisalignedbb4.offset(eleventhDoubleValue, 0.0D, 0.0D);
                double twelfthDoubleValue = fifthDoubleValue;

                for (AxisAlignedBB axisalignedbb8 : list)
                {
                    twelfthDoubleValue = axisalignedbb8.calculateZOffset(axisalignedbb4, twelfthDoubleValue);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, twelfthDoubleValue);
                AxisAlignedBB axisalignedbb14 = this.getEntityBoundingBox();
                double thirteenthDoubleValue = y;

                for (AxisAlignedBB axisalignedbb9 : list)
                {
                    thirteenthDoubleValue = axisalignedbb9.calculateYOffset(axisalignedbb14, thirteenthDoubleValue);
                }

                axisalignedbb14 = axisalignedbb14.offset(0.0D, thirteenthDoubleValue, 0.0D);
                double fourteenthDoubleValue = twentySixthDoubleValue;

                for (AxisAlignedBB axisalignedbb10 : list)
                {
                    fourteenthDoubleValue = axisalignedbb10.calculateXOffset(axisalignedbb14, fourteenthDoubleValue);
                }

                axisalignedbb14 = axisalignedbb14.offset(fourteenthDoubleValue, 0.0D, 0.0D);
                double fifteenthDoubleValue = fifthDoubleValue;

                for (AxisAlignedBB axisalignedbb11 : list)
                {
                    fifteenthDoubleValue = axisalignedbb11.calculateZOffset(axisalignedbb14, fifteenthDoubleValue);
                }

                axisalignedbb14 = axisalignedbb14.offset(0.0D, 0.0D, fifteenthDoubleValue);
                double sixteenthDoubleValue = eleventhDoubleValue * eleventhDoubleValue + twelfthDoubleValue * twelfthDoubleValue;
                double seventeenthDoubleValue = fourteenthDoubleValue * fourteenthDoubleValue + fifteenthDoubleValue * fifteenthDoubleValue;

                if (sixteenthDoubleValue > seventeenthDoubleValue)
                {
                    x = eleventhDoubleValue;
                    z = twelfthDoubleValue;
                    y = -tenthDoubleValue;
                    this.setEntityBoundingBox(axisalignedbb4);
                }
                else
                {
                    x = fourteenthDoubleValue;
                    z = fifteenthDoubleValue;
                    y = -thirteenthDoubleValue;
                    this.setEntityBoundingBox(axisalignedbb14);
                }

                for (AxisAlignedBB axisalignedbb12 : list)
                {
                    y = axisalignedbb12.calculateYOffset(this.getEntityBoundingBox(), y);
                }

                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

                if (seventhDoubleValue * seventhDoubleValue + ninthDoubleValue * ninthDoubleValue >= x * x + z * z)
                {
                    x = seventhDoubleValue;
                    y = eighthDoubleValue;
                    z = ninthDoubleValue;
                    this.setEntityBoundingBox(axisalignedbb3);
                }
            }

            this.worldObj.theProfiler.endSection();
            this.worldObj.theProfiler.startSection("rest");
            this.resetPositionToBB();
            this.isCollidedHorizontally = twentySixthDoubleValue != x || fifthDoubleValue != z;
            this.isCollidedVertically = fourthDoubleValue != y;
            this.onGround = this.isCollidedVertically && fourthDoubleValue < 0.0D;
            this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
            int i = MathHelper.floor_double(this.posX);
            int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
            int k = MathHelper.floor_double(this.posZ);
            BlockPos blockpos = new BlockPos(i, j, k);
            Block block1 = this.worldObj.getBlockState(blockpos).getBlock();

            if (block1.getMaterial() == Material.air)
            {
                BlockPos blockposDown = blockpos.down();
                Block block = this.worldObj.getBlockState(blockposDown).getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate)
                {
                    block1 = block;
                    blockpos = blockposDown;
                }
            }

            this.updateFallState(y, this.onGround, block1, blockpos);

            if (twentySixthDoubleValue != x)
            {
                this.motionX = 0.0D;
            }

            if (fifthDoubleValue != z)
            {
                this.motionZ = 0.0D;
            }

            if (fourthDoubleValue != y)
            {
                block1.onLanded(this.worldObj, this);
            }

            if (this.canTriggerWalking() && !flag && this.ridingEntity == null)
            {
                double eighteenthDoubleValue = this.posX - doubleValue;
                double nineteenthDoubleValue = this.posY - secondDoubleValue;
                double twentiethDoubleValue = this.posZ - thirdDoubleValue;

                if (block1 != Blocks.ladder)
                {
                    nineteenthDoubleValue = 0.0D;
                }

                if (block1 != null && this.onGround)
                {
                    block1.onEntityCollidedWithBlock(this.worldObj, blockpos, this);
                }

                this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt_double(eighteenthDoubleValue * eighteenthDoubleValue + twentiethDoubleValue * twentiethDoubleValue) * 0.6D);
                this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt_double(eighteenthDoubleValue * eighteenthDoubleValue + nineteenthDoubleValue * nineteenthDoubleValue + twentiethDoubleValue * twentiethDoubleValue) * 0.6D);

                if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && block1.getMaterial() != Material.air)
                {
                    this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater())
                    {
                        float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;

                        if (f > 1.0F)
                        {
                            f = 1.0F;
                        }

                        this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    }

                    this.playStepSound(blockpos, block1);
                }
            }

            try
            {
                this.doBlockCollisions();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = this.isWet();

            if (this.worldObj.isFlammableWithin(this.getEntityBoundingBox().contract(0.001D, 0.001D, 0.001D)))
            {
                this.dealFireDamage(1);

                if (!flag2)
                {
                    ++this.fire;

                    if (this.fire == 0)
                    {
                        this.setFire(8);
                    }
                }
            }
            else if (this.fire <= 0)
            {
                this.fire = -this.fireResistance;
            }

            if (flag2 && this.fire > 0)
            {
                this.playSound("random.fizz", 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                this.fire = -this.fireResistance;
            }

            this.worldObj.theProfiler.endSection();
        }
    }

    private void resetPositionToBB()
    {
        this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        this.posY = this.getEntityBoundingBox().minY;
        this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
    }

    protected String getSwimSound()
    {
        return "game.neutral.swim";
    }

    protected void doBlockCollisions()
    {
        if (this.collisionMinPos == null)
        {
            this.collisionMinPos = new BlockPos.MutableBlockPos();
            this.collisionMaxPos = new BlockPos.MutableBlockPos();
            this.collisionCheckPos = new BlockPos.MutableBlockPos();
        }

        BlockPos.MutableBlockPos blockPos = this.collisionMinPos.set(MathHelper.floor_double(this.getEntityBoundingBox().minX + 0.001D), MathHelper.floor_double(this.getEntityBoundingBox().minY + 0.001D), MathHelper.floor_double(this.getEntityBoundingBox().minZ + 0.001D));
        BlockPos.MutableBlockPos blockpos1 = this.collisionMaxPos.set(MathHelper.floor_double(this.getEntityBoundingBox().maxX - 0.001D), MathHelper.floor_double(this.getEntityBoundingBox().maxY - 0.001D), MathHelper.floor_double(this.getEntityBoundingBox().maxZ - 0.001D));
        int minX = blockPos.getX();
        int minY = blockPos.getY();
        int minZ = blockPos.getZ();
        int maxX = blockpos1.getX();
        int maxY = blockpos1.getY();
        int maxZ = blockpos1.getZ();
        boolean wasProcessing = this.processingBlockCollisions;
        BlockPos.MutableBlockPos checkPos = wasProcessing ? new BlockPos.MutableBlockPos() : this.collisionCheckPos;
        this.processingBlockCollisions = true;

        try
        {
            if (this.worldObj.isAreaLoaded(blockPos, blockpos1))
            {
                for (int i = minX; i <= maxX; ++i)
                {
                    for (int j = minY; j <= maxY; ++j)
                    {
                        for (int k = minZ; k <= maxZ; ++k)
                        {
                            IBlockState iblockstate = this.worldObj.getBlockState(checkPos.set(i, j, k));

                            if (iblockstate.getBlock() == Blocks.air)
                            {
                                continue;
                            }

                            try
                            {
                                iblockstate.getBlock().onEntityCollidedWithBlock(this.worldObj, checkPos, iblockstate, this);
                            }
                            catch (Throwable throwable)
                            {
                                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Colliding entity with block");
                                CrashReportCategory crashReportCategory = crashReport.makeCategory("Block being collided with");
                                CrashReportCategory.addBlockInfo(crashReportCategory, new BlockPos(i, j, k), iblockstate);
                                throw new ReportedException(crashReport);
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            this.processingBlockCollisions = wasProcessing;
        }
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        Block.SoundType block$soundtype = blockIn.stepSound;

        if (this.worldObj.getBlockState(pos.up()).getBlock() == Blocks.snow_layer)
        {
            block$soundtype = Blocks.snow_layer.stepSound;
            this.playSound(block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
        }
        else if (!blockIn.getMaterial().isLiquid())
        {
            this.playSound(block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.15F, block$soundtype.getFrequency());
        }
    }

    public void playSound(String name, float volume, float pitch)
    {
        if (!this.isSilent())
        {
            this.worldObj.playSoundAtEntity(this, name, volume, pitch);
        }
    }

    public boolean isSilent()
    {
        return this.dataWatcher.getWatchableObjectByte(4) == 1;
    }

    public void setSilent(boolean isSilent)
    {
        this.dataWatcher.updateObject(4, Byte.valueOf((byte)(isSilent ? 1 : 0)));
    }

    protected boolean canTriggerWalking()
    {
        return true;
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
        if (onGroundIn)
        {
            if (this.fallDistance > 0.0F)
            {
                if (blockIn != null)
                {
                    blockIn.onFallenUpon(this.worldObj, pos, this, this.fallDistance);
                }
                else
                {
                    this.fall(this.fallDistance, 1.0F);
                }

                this.fallDistance = 0.0F;
            }
        }
        else if (y < 0.0D)
        {
            this.fallDistance = (float)((double)this.fallDistance - y);
        }
    }

    public AxisAlignedBB getCollisionBoundingBox()
    {
        return null;
    }

    protected void dealFireDamage(int amount)
    {
        if (!this.isImmuneToFire)
        {
            this.attackEntityFrom(DamageSource.inFire, (float)amount);
        }
    }

    public final boolean isImmuneToFire()
    {
        return this.isImmuneToFire;
    }

    public void fall(float distance, float damageMultiplier)
    {
        if (this.riddenByEntity != null)
        {
            this.riddenByEntity.fall(distance, damageMultiplier);
        }
    }

    public boolean isWet()
    {
        if (this.inWater)
        {
            return true;
        }

        BlockPos.MutableBlockPos blockpos$mutableblockpos = this.wetCheckPos;

        if (blockpos$mutableblockpos == null)
        {
            blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            this.wetCheckPos = blockpos$mutableblockpos;
        }

        int x = MathHelper.floor_double(this.posX);
        int z = MathHelper.floor_double(this.posZ);

        if (this.worldObj.isRainingAt(blockpos$mutableblockpos.set(x, MathHelper.floor_double(this.posY), z)))
        {
            return true;
        }

        return this.worldObj.isRainingAt(blockpos$mutableblockpos.set(x, MathHelper.floor_double(this.posY + (double)this.height), z));
    }

    public boolean isInWater()
    {
        return this.inWater;
    }

    public boolean handleWaterMovement()
    {
        if (this.worldObj.handleMaterialAcceleration(this.getEntityBoundingBox().expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, this))
        {
            if (!this.inWater && !this.firstUpdate)
            {
                this.resetHeight();
            }

            this.fallDistance = 0.0F;
            this.inWater = true;
            this.fire = 0;
        }
        else
        {
            this.inWater = false;
        }

        return this.inWater;
    }

    protected void resetHeight()
    {
        float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.2F;

        if (f > 1.0F)
        {
            f = 1.0F;
        }

        this.playSound(this.getSplashSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
        float floatValue2 = (float)MathHelper.floor_double(this.getEntityBoundingBox().minY);

        for (int i = 0; (float)i < 1.0F + this.width * 20.0F; ++i)
        {
            float thirdFloatValue = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            float floatValue4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)thirdFloatValue, (double)(floatValue2 + 1.0F), this.posZ + (double)floatValue4, this.motionX, this.motionY - (double)(this.rand.nextFloat() * 0.2F), this.motionZ, EnumParticleTypes.EMPTY_ARGS);
        }

        for (int j = 0; (float)j < 1.0F + this.width * 20.0F; ++j)
        {
            float fourthFloatValue = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            float floatValue6 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
            this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)fourthFloatValue, (double)(floatValue2 + 1.0F), this.posZ + (double)floatValue6, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
        }
    }

    public void spawnRunningParticles()
    {
        if (this.isSprinting() && !this.isInWater())
        {
            this.createRunningParticles();
        }
    }

    protected void createRunningParticles()
    {
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
        int k = MathHelper.floor_double(this.posZ);
        BlockPos blockPos = new BlockPos(i, j, k);
        IBlockState iblockstate = this.worldObj.getBlockState(blockPos);
        Block block = iblockstate.getBlock();

        if (block.getRenderType() != -1)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, new int[] {Block.getStateId(iblockstate)});
        }
    }

    protected String getSplashSound()
    {
        return "game.neutral.swim.splash";
    }

    public boolean isInsideOfMaterial(Material materialIn)
    {
        double yCoordinate = this.posY + (double)this.getEyeHeight();
        BlockPos.MutableBlockPos blockPos = this.materialCheckPos;

        if (blockPos == null)
        {
            blockPos = new BlockPos.MutableBlockPos();
            this.materialCheckPos = blockPos;
        }

        blockPos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(yCoordinate), MathHelper.floor_double(this.posZ));
        IBlockState iblockstate = this.worldObj.getBlockState(blockPos);
        Block block = iblockstate.getBlock();

        if (block.getMaterial() == materialIn)
        {
            float f = BlockLiquid.getLiquidHeightPercent(iblockstate.getBlock().getMetaFromState(iblockstate)) - 0.11111111F;
            float floatValue2 = (float)(blockPos.getY() + 1) - f;
            boolean flag = yCoordinate < (double)floatValue2;
            return !flag && this instanceof EntityPlayer ? false : flag;
        }
        else
        {
            return false;
        }
    }

    public boolean isInLava()
    {
        return this.worldObj.isMaterialInBB(this.getEntityBoundingBox().expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.lava);
    }

    public void moveFlying(float strafe, float forward, float friction)
    {
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F)
        {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F)
            {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float[] yawSC = new float[2];
            MathHelper.sinCosDeg(this.rotationYaw, yawSC);
            float floatValue = yawSC[0];
            float secondFloatValue = yawSC[1];
            this.motionX += (double)(strafe * secondFloatValue - forward * floatValue);
            this.motionZ += (double)(forward * secondFloatValue + strafe * floatValue);
        }
    }

    public int getBrightnessForRender(float partialTicks)
    {
        BlockPos blockPos = this.getBrightnessCheckPos();
        return this.worldObj.isBlockLoaded(blockPos) ? this.worldObj.getCombinedLight(blockPos, 0) : 0;
    }

    public float getBrightness(float partialTicks)
    {
        BlockPos blockPos = this.getBrightnessCheckPos();
        return this.worldObj.isBlockLoaded(blockPos) ? this.worldObj.getLightBrightness(blockPos) : 0.0F;
    }

    private BlockPos.MutableBlockPos getBrightnessCheckPos()
    {
        if (this.brightnessCheckPos == null)
        {
            this.brightnessCheckPos = new BlockPos.MutableBlockPos();
        }

        return this.brightnessCheckPos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY + (double)this.getEyeHeight()), MathHelper.floor_double(this.posZ));
    }

    public void setWorld(World worldIn)
    {
        this.worldObj = worldIn;
    }

    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        this.prevPosX = this.posX = x;
        this.prevPosY = this.posY = y;
        this.prevPosZ = this.posZ = z;
        this.prevRotationYaw = this.rotationYaw = yaw;
        this.prevRotationPitch = this.rotationPitch = pitch;
        double doubleValue = (double)(this.prevRotationYaw - yaw);

        if (doubleValue < -180.0D)
        {
            this.prevRotationYaw += 360.0F;
        }

        if (doubleValue >= 180.0D)
        {
            this.prevRotationYaw -= 360.0F;
        }

        this.setPosition(this.posX, this.posY, this.posZ);
        this.setRotation(yaw, pitch);
    }

    public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn)
    {
        this.setLocationAndAngles((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, rotationYawIn, rotationPitchIn);
    }

    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch)
    {
        this.lastTickPosX = this.prevPosX = this.posX = x;
        this.lastTickPosY = this.prevPosY = this.posY = y;
        this.lastTickPosZ = this.prevPosZ = this.posZ = z;
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    public float getDistanceToEntity(Entity entityIn)
    {
        float f = (float)(this.posX - entityIn.posX);
        float floatValue2 = (float)(this.posY - entityIn.posY);
        float floatValue3 = (float)(this.posZ - entityIn.posZ);
        return MathHelper.sqrt_float(f * f + floatValue2 * floatValue2 + floatValue3 * floatValue3);
    }

    public double getDistanceSq(double x, double y, double z)
    {
        double xCoordinate = this.posX - x;
        double yCoordinate = this.posY - y;
        double zCoordinate = this.posZ - z;
        return xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
    }

    public double getDistanceSq(BlockPos pos)
    {
        return pos.distanceSq(this.posX, this.posY, this.posZ);
    }

    public double getDistanceSqToCenter(BlockPos pos)
    {
        return pos.distanceSqToCenter(this.posX, this.posY, this.posZ);
    }

    public double getDistance(double x, double y, double z)
    {
        double xCoordinate = this.posX - x;
        double yCoordinate = this.posY - y;
        double zCoordinate = this.posZ - z;
        return (double)MathHelper.sqrt_double(xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate);
    }

    public double getDistanceSqToEntity(Entity entityIn)
    {
        double xCoordinate = this.posX - entityIn.posX;
        double yCoordinate = this.posY - entityIn.posY;
        double zCoordinate = this.posZ - entityIn.posZ;
        return xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
    }

    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
    }

    public void applyEntityCollision(Entity entityIn)
    {
        if (entityIn.riddenByEntity != this && entityIn.ridingEntity != this)
        {
            if (!entityIn.noClip && !this.noClip)
            {
                double xCoordinate = entityIn.posX - this.posX;
                double zCoordinate = entityIn.posZ - this.posZ;
                double doubleValue = MathHelper.abs_max(xCoordinate, zCoordinate);

                if (doubleValue >= 0.009999999776482582D)
                {
                    doubleValue = (double)MathHelper.sqrt_double(doubleValue);
                    xCoordinate = xCoordinate / doubleValue;
                    zCoordinate = zCoordinate / doubleValue;
                    double doubleValue2 = 1.0D / doubleValue;

                    if (doubleValue2 > 1.0D)
                    {
                        doubleValue2 = 1.0D;
                    }

                    xCoordinate = xCoordinate * doubleValue2;
                    zCoordinate = zCoordinate * doubleValue2;
                    xCoordinate = xCoordinate * 0.05000000074505806D;
                    zCoordinate = zCoordinate * 0.05000000074505806D;
                    xCoordinate = xCoordinate * (double)(1.0F - this.entityCollisionReduction);
                    zCoordinate = zCoordinate * (double)(1.0F - this.entityCollisionReduction);

                    if (this.riddenByEntity == null)
                    {
                        this.addVelocity(-xCoordinate, 0.0D, -zCoordinate);
                    }

                    if (entityIn.riddenByEntity == null)
                    {
                        entityIn.addVelocity(xCoordinate, 0.0D, zCoordinate);
                    }
                }
            }
        }
    }

    public void addVelocity(double x, double y, double z)
    {
        this.motionX += x;
        this.motionY += y;
        this.motionZ += z;
        this.isAirBorne = true;
    }

    protected void setBeenAttacked()
    {
        this.velocityChanged = true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            this.setBeenAttacked();
            return false;
        }
    }

    public Vec3 getLook(float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
        }
        else
        {
            float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float floatValue2 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
            return this.getVectorForRotation(f, floatValue2);
        }
    }

    protected final Vec3 getVectorForRotation(float pitch, float yaw)
    {

        float[] yawSC = new float[2];
        float[] pitchSC = new float[2];
        MathHelper.sinCosDeg(-yaw, yawSC);
        MathHelper.sinCosDeg(-pitch, pitchSC);
        float f = -yawSC[1];
        float floatValue2 = -yawSC[0];
        float floatValue3 = -pitchSC[1];
        float floatValue4 = pitchSC[0];
        return new Vec3((double)(floatValue2 * floatValue3), (double)floatValue4, (double)(f * floatValue3));
    }

    public Vec3 getPositionEyes(float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return new Vec3(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
        }
        else
        {
            double xCoordinate = this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks;
            double yCoordinate = this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks + (double)this.getEyeHeight();
            double zCoordinate = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks;
            return new Vec3(xCoordinate, yCoordinate, zCoordinate);
        }
    }

    public MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks)
    {
        Vec3 eyePosition = this.getPositionEyes(partialTicks);
        Vec3 lookVector = this.getLook(partialTicks);
        Vec3 reachEnd = eyePosition.addVector(lookVector.xCoord * blockReachDistance, lookVector.yCoord * blockReachDistance, lookVector.zCoord * blockReachDistance);
        return this.worldObj.rayTraceBlocks(eyePosition, reachEnd, false, false, true);
    }

    public boolean canBeCollidedWith()
    {
        return false;
    }

    public boolean canBePushed()
    {
        return false;
    }

    public void addToPlayerScore(Entity entityIn, int amount)
    {
    }

    public boolean isInRangeToRender3d(double x, double y, double z)
    {
        double xCoordinate = this.posX - x;
        double yCoordinate = this.posY - y;
        double zCoordinate = this.posZ - z;
        double doubleValue = xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
        return this.isInRangeToRenderDist(doubleValue);
    }

    public boolean isInRangeToRenderDist(double distance)
    {
        double doubleValue = this.getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(doubleValue))
        {
            doubleValue = 1.0D;
        }

        doubleValue = doubleValue * 64.0D * this.renderDistanceWeight;
        return distance < doubleValue * doubleValue;
    }

    public boolean writeMountToNBT(NBTTagCompound tagCompund)
    {
        String s = this.getEntityString();

        if (!this.isDead && s != null)
        {
            tagCompund.setString("id", s);
            this.writeToNBT(tagCompund);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean writeToNBTOptional(NBTTagCompound tagCompund)
    {
        String s = this.getEntityString();

        if (!this.isDead && s != null && this.riddenByEntity == null)
        {
            tagCompund.setString("id", s);
            this.writeToNBT(tagCompund);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void writeToNBT(NBTTagCompound tagCompund)
    {
        try
        {
            tagCompund.setTag("Pos", this.newDoubleNBTList(new double[] {this.posX, this.posY, this.posZ}));
            tagCompund.setTag("Motion", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
            tagCompund.setTag("Rotation", this.newFloatNBTList(new float[] {this.rotationYaw, this.rotationPitch}));
            tagCompund.setFloat("FallDistance", this.fallDistance);
            tagCompund.setShort("Fire", (short)this.fire);
            tagCompund.setShort("Air", (short)this.getAir());
            tagCompund.setBoolean("OnGround", this.onGround);
            tagCompund.setInteger("Dimension", this.dimension);
            tagCompund.setBoolean("Invulnerable", this.invulnerable);
            tagCompund.setInteger("PortalCooldown", this.timeUntilPortal);
            tagCompund.setLong("UUIDMost", this.getUniqueID().getMostSignificantBits());
            tagCompund.setLong("UUIDLeast", this.getUniqueID().getLeastSignificantBits());

            if (this.getCustomNameTag() != null && this.getCustomNameTag().length() > 0)
            {
                tagCompund.setString("CustomName", this.getCustomNameTag());
                tagCompund.setBoolean("CustomNameVisible", this.getAlwaysRenderNameTag());
            }

            this.cmdResultStats.writeStatsToNBT(tagCompund);

            if (this.isSilent())
            {
                tagCompund.setBoolean("Silent", this.isSilent());
            }

            this.writeEntityToNBT(tagCompund);

            if (this.ridingEntity != null)
            {
                NBTTagCompound nBTTagCompound = new NBTTagCompound();

                if (this.ridingEntity.writeMountToNBT(nBTTagCompound))
                {
                    tagCompund.setTag("Riding", nBTTagCompound);
                }
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Saving entity NBT");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Entity being saved");
            this.addEntityCrashInfo(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    public void readFromNBT(NBTTagCompound tagCompund)
    {
        try
        {
            NBTTagList nBTTagList = tagCompund.getTagList("Pos", 6);
            NBTTagList nbttaglist1 = tagCompund.getTagList("Motion", 6);
            NBTTagList nbttaglist2 = tagCompund.getTagList("Rotation", 5);
            this.motionX = nbttaglist1.getDoubleAt(0);
            this.motionY = nbttaglist1.getDoubleAt(1);
            this.motionZ = nbttaglist1.getDoubleAt(2);

            if (Math.abs(this.motionX) > 10.0D)
            {
                this.motionX = 0.0D;
            }

            if (Math.abs(this.motionY) > 10.0D)
            {
                this.motionY = 0.0D;
            }

            if (Math.abs(this.motionZ) > 10.0D)
            {
                this.motionZ = 0.0D;
            }

            this.prevPosX = this.lastTickPosX = this.posX = nBTTagList.getDoubleAt(0);
            this.prevPosY = this.lastTickPosY = this.posY = nBTTagList.getDoubleAt(1);
            this.prevPosZ = this.lastTickPosZ = this.posZ = nBTTagList.getDoubleAt(2);
            this.prevRotationYaw = this.rotationYaw = nbttaglist2.getFloatAt(0);
            this.prevRotationPitch = this.rotationPitch = nbttaglist2.getFloatAt(1);
            this.setRotationYawHead(this.rotationYaw);
            this.setRenderYawOffset(this.rotationYaw);
            this.fallDistance = tagCompund.getFloat("FallDistance");
            this.fire = tagCompund.getShort("Fire");
            this.setAir(tagCompund.getShort("Air"));
            this.onGround = tagCompund.getBoolean("OnGround");
            this.dimension = tagCompund.getInteger("Dimension");
            this.invulnerable = tagCompund.getBoolean("Invulnerable");
            this.timeUntilPortal = tagCompund.getInteger("PortalCooldown");

            if (tagCompund.hasKey("UUIDMost", 4) && tagCompund.hasKey("UUIDLeast", 4))
            {
                this.entityUniqueID = new UUID(tagCompund.getLong("UUIDMost"), tagCompund.getLong("UUIDLeast"));
            }
            else if (tagCompund.hasKey("UUID", 8))
            {
                this.entityUniqueID = UUID.fromString(tagCompund.getString("UUID"));
            }

            this.setPosition(this.posX, this.posY, this.posZ);
            this.setRotation(this.rotationYaw, this.rotationPitch);

            if (tagCompund.hasKey("CustomName", 8) && tagCompund.getString("CustomName").length() > 0)
            {
                this.setCustomNameTag(tagCompund.getString("CustomName"));
            }

            this.setAlwaysRenderNameTag(tagCompund.getBoolean("CustomNameVisible"));
            this.cmdResultStats.readStatsFromNBT(tagCompund);
            this.setSilent(tagCompund.getBoolean("Silent"));
            this.readEntityFromNBT(tagCompund);

            if (this.shouldSetPosAfterLoading())
            {
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Loading entity NBT");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Entity being loaded");
            this.addEntityCrashInfo(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    protected boolean shouldSetPosAfterLoading()
    {
        return true;
    }

    protected final String getEntityString()
    {
        return EntityList.getEntityString(this);
    }

    protected abstract void readEntityFromNBT(NBTTagCompound tagCompund);

    protected abstract void writeEntityToNBT(NBTTagCompound tagCompound);

    public void onChunkLoad()
    {
    }

    protected NBTTagList newDoubleNBTList(double... numbers)
    {
        NBTTagList nBTTagList = new NBTTagList();

        for (double doubleValue : numbers)
        {
            nBTTagList.appendTag(new NBTTagDouble(doubleValue));
        }

        return nBTTagList;
    }

    protected NBTTagList newFloatNBTList(float... numbers)
    {
        NBTTagList nBTTagList = new NBTTagList();

        for (float f : numbers)
        {
            nBTTagList.appendTag(new NBTTagFloat(f));
        }

        return nBTTagList;
    }

    public EntityItem dropItem(Item itemIn, int size)
    {
        return this.dropItemWithOffset(itemIn, size, 0.0F);
    }

    public EntityItem dropItemWithOffset(Item itemIn, int size, float offsetY)
    {
        return this.entityDropItem(new ItemStack(itemIn, size, 0), offsetY);
    }

    public EntityItem entityDropItem(ItemStack itemStackIn, float offsetY)
    {
        if (itemStackIn.stackSize != 0 && itemStackIn.getItem() != null)
        {
            EntityItem entityItem = new EntityItem(this.worldObj, this.posX, this.posY + (double)offsetY, this.posZ, itemStackIn);
            entityItem.setDefaultPickupDelay();
            this.worldObj.spawnEntityInWorld(entityItem);
            return entityItem;
        }
        else
        {
            return null;
        }
    }

    public boolean isEntityAlive()
    {
        return !this.isDead;
    }

    public boolean isEntityInsideOpaqueBlock()
    {
        if (this.noClip)
        {
            return false;
        }
        else
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.opaqueBlockCheckPos;

            if (blockpos$mutableblockpos == null)
            {
                blockpos$mutableblockpos = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
                this.opaqueBlockCheckPos = blockpos$mutableblockpos;
            }
            else
            {
                blockpos$mutableblockpos.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            }

            for (int i = 0; i < 8; ++i)
            {
                int j = MathHelper.floor_double(this.posY + (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)this.getEyeHeight());
                int k = MathHelper.floor_double(this.posX + (double)(((float)((i >> 1) % 2) - 0.5F) * this.width * 0.8F));
                int l = MathHelper.floor_double(this.posZ + (double)(((float)((i >> 2) % 2) - 0.5F) * this.width * 0.8F));

                if (blockpos$mutableblockpos.getX() != k || blockpos$mutableblockpos.getY() != j || blockpos$mutableblockpos.getZ() != l)
                {
                    blockpos$mutableblockpos.set(k, j, l);

                    if (this.worldObj.getBlockState(blockpos$mutableblockpos).getBlock().isVisuallyOpaque())
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean interactFirst(EntityPlayer playerIn)
    {
        return false;
    }

    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return null;
    }

    public void updateRidden()
    {
        if (this.ridingEntity.isDead)
        {
            this.ridingEntity = null;
        }
        else
        {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onUpdate();

            if (this.ridingEntity != null)
            {
                this.ridingEntity.updateRiderPosition();
                this.entityRiderYawDelta += (double)(this.ridingEntity.rotationYaw - this.ridingEntity.prevRotationYaw);

                for (this.entityRiderPitchDelta += (double)(this.ridingEntity.rotationPitch - this.ridingEntity.prevRotationPitch); this.entityRiderYawDelta >= 180.0D; this.entityRiderYawDelta -= 360.0D)
                {
                    ;
                }

                while (this.entityRiderYawDelta < -180.0D)
                {
                    this.entityRiderYawDelta += 360.0D;
                }

                while (this.entityRiderPitchDelta >= 180.0D)
                {
                    this.entityRiderPitchDelta -= 360.0D;
                }

                while (this.entityRiderPitchDelta < -180.0D)
                {
                    this.entityRiderPitchDelta += 360.0D;
                }

                double twentyThirdDoubleValue = this.entityRiderYawDelta * 0.5D;
                double doubleValue2 = this.entityRiderPitchDelta * 0.5D;
                float f = 10.0F;

                if (twentyThirdDoubleValue > (double)f)
                {
                    twentyThirdDoubleValue = (double)f;
                }

                if (twentyThirdDoubleValue < (double)(-f))
                {
                    twentyThirdDoubleValue = (double)(-f);
                }

                if (doubleValue2 > (double)f)
                {
                    doubleValue2 = (double)f;
                }

                if (doubleValue2 < (double)(-f))
                {
                    doubleValue2 = (double)(-f);
                }

                this.entityRiderYawDelta -= twentyThirdDoubleValue;
                this.entityRiderPitchDelta -= doubleValue2;
            }
        }
    }

    public void updateRiderPosition()
    {
        if (this.riddenByEntity != null)
        {
            this.riddenByEntity.setPosition(this.posX, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ);
        }
    }

    public double getYOffset()
    {
        return 0.0D;
    }

    public double getMountedYOffset()
    {
        return (double)this.height * 0.75D;
    }

    public void mountEntity(Entity entityIn)
    {
        this.entityRiderPitchDelta = 0.0D;
        this.entityRiderYawDelta = 0.0D;

        if (entityIn == null)
        {
            if (this.ridingEntity != null)
            {
                this.setLocationAndAngles(this.ridingEntity.posX, this.ridingEntity.getEntityBoundingBox().minY + (double)this.ridingEntity.height, this.ridingEntity.posZ, this.rotationYaw, this.rotationPitch);
                this.ridingEntity.riddenByEntity = null;
            }

            this.ridingEntity = null;
        }
        else
        {
            if (this.ridingEntity != null)
            {
                this.ridingEntity.riddenByEntity = null;
            }

            if (entityIn != null)
            {
                for (Entity entity = entityIn.ridingEntity; entity != null; entity = entity.ridingEntity)
                {
                    if (entity == this)
                    {
                        return;
                    }
                }
            }

            this.ridingEntity = entityIn;
            entityIn.riddenByEntity = this;
        }
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
        List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().contract(0.03125D, 0.0D, 0.03125D));

        if (!list.isEmpty())
        {
            double twentyFirstDoubleValue = 0.0D;

            for (AxisAlignedBB axisalignedbb : list)
            {
                if (axisalignedbb.maxY > twentyFirstDoubleValue)
                {
                    twentyFirstDoubleValue = axisalignedbb.maxY;
                }
            }

            y = y + (twentyFirstDoubleValue - this.getEntityBoundingBox().minY);
            this.setPosition(x, y, z);
        }
    }

    public float getCollisionBorderSize()
    {
        return 0.1F;
    }

    public Vec3 getLookVec()
    {
        return null;
    }

    public void setPortal(BlockPos pos)
    {
        if (this.timeUntilPortal > 0)
        {
            this.timeUntilPortal = this.getPortalCooldown();
        }
        else
        {
            if (!this.worldObj.isRemote && !pos.equals(this.lastPortalPos))
            {
                this.lastPortalPos = pos;
                BlockPattern.PatternHelper blockpattern$patternhelper = Blocks.portal.createPatternHelper(this.worldObj, pos);
                double twentySecondDoubleValue = blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getPos().getZ() : (double)blockpattern$patternhelper.getPos().getX();
                double twentyFourthDoubleValue = blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? this.posZ : this.posX;
                twentyFourthDoubleValue = Math.abs(MathHelper.getLerpProgress(twentyFourthDoubleValue - (double)(blockpattern$patternhelper.getFinger().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), twentySecondDoubleValue, twentySecondDoubleValue - (double)blockpattern$patternhelper.getPalmLength()));
                double twentyFifthDoubleValue = MathHelper.getLerpProgress(this.posY - 1.0D, (double)blockpattern$patternhelper.getPos().getY(), (double)(blockpattern$patternhelper.getPos().getY() - blockpattern$patternhelper.getThumbLength()));
                this.lastPortalVec = new Vec3(twentyFourthDoubleValue, twentyFifthDoubleValue, 0.0D);
                this.teleportDirection = blockpattern$patternhelper.getFinger();
            }

            this.inPortal = true;
        }
    }

    public int getPortalCooldown()
    {
        return 300;
    }

    public void setVelocity(double x, double y, double z)
    {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }

    public void handleStatusUpdate(byte id)
    {
    }

    public void performHurtAnimation()
    {
    }

    public ItemStack[] getInventory()
    {
        return null;
    }

    public void setCurrentItemOrArmor(int slotIn, ItemStack stack)
    {
    }

    public boolean isBurning()
    {
        boolean flag = this.worldObj != null && this.worldObj.isRemote;
        return !this.isImmuneToFire && (this.fire > 0 || flag && this.getFlag(0));
    }

    public boolean isRiding()
    {
        return this.ridingEntity != null;
    }

    public boolean isSneaking()
    {
        return this.getFlag(1);
    }

    public void setSneaking(boolean sneaking)
    {
        this.setFlag(1, sneaking);
    }

    public boolean isSprinting()
    {
        return this.getFlag(3);
    }

    public void setSprinting(boolean sprinting)
    {
        this.setFlag(3, sprinting);
    }

    public boolean isInvisible()
    {
        return this.getFlag(5);
    }

    public boolean isInvisibleToPlayer(EntityPlayer player)
    {
        return player.isSpectator() ? false : this.isInvisible();
    }

    public void setInvisible(boolean invisible)
    {
        this.setFlag(5, invisible);
    }

    public boolean isEating()
    {
        return this.getFlag(4);
    }

    public void setEating(boolean eating)
    {
        this.setFlag(4, eating);
    }

    protected boolean getFlag(int flag)
    {
        return (this.dataWatcher.getWatchableObjectByte(0) & 1 << flag) != 0;
    }

    protected void setFlag(int flag, boolean set)
    {
        byte byteValue = this.dataWatcher.getWatchableObjectByte(0);

        if (set)
        {
            this.dataWatcher.updateObject(0, Byte.valueOf((byte)(byteValue | 1 << flag)));
        }
        else
        {
            this.dataWatcher.updateObject(0, Byte.valueOf((byte)(byteValue & ~(1 << flag))));
        }
    }

    public int getAir()
    {
        return this.dataWatcher.getWatchableObjectShort(1);
    }

    public void setAir(int air)
    {
        this.dataWatcher.updateObject(1, Short.valueOf((short)air));
    }

    public void onStruckByLightning(EntityLightningBolt lightningBolt)
    {
        this.attackEntityFrom(DamageSource.lightningBolt, 5.0F);
        ++this.fire;

        if (this.fire == 0)
        {
            this.setFire(8);
        }
    }

    public void onKillEntity(EntityLivingBase entityLivingIn)
    {
    }

    protected boolean pushOutOfBlocks(double x, double y, double z)
    {
        BlockPos blockPos = new BlockPos(x, y, z);
        double xCoordinate = x - (double)blockPos.getX();
        double yCoordinate = y - (double)blockPos.getY();
        double zCoordinate = z - (double)blockPos.getZ();
        List<AxisAlignedBB> list = this.worldObj.getCollisionBoxes(this.getEntityBoundingBox());

        if (list.isEmpty() && !this.worldObj.isBlockFullCube(blockPos))
        {
            return false;
        }
        else
        {
            int i = 3;
            double doubleValue = 9999.0D;

            if (!this.worldObj.isBlockFullCube(blockPos.west()) && xCoordinate < doubleValue)
            {
                doubleValue = xCoordinate;
                i = 0;
            }

            if (!this.worldObj.isBlockFullCube(blockPos.east()) && 1.0D - xCoordinate < doubleValue)
            {
                doubleValue = 1.0D - xCoordinate;
                i = 1;
            }

            if (!this.worldObj.isBlockFullCube(blockPos.up()) && 1.0D - yCoordinate < doubleValue)
            {
                doubleValue = 1.0D - yCoordinate;
                i = 3;
            }

            if (!this.worldObj.isBlockFullCube(blockPos.north()) && zCoordinate < doubleValue)
            {
                doubleValue = zCoordinate;
                i = 4;
            }

            if (!this.worldObj.isBlockFullCube(blockPos.south()) && 1.0D - zCoordinate < doubleValue)
            {
                doubleValue = 1.0D - zCoordinate;
                i = 5;
            }

            float f = this.rand.nextFloat() * 0.2F + 0.1F;

            if (i == 0)
            {
                this.motionX = (double)(-f);
            }

            if (i == 1)
            {
                this.motionX = (double)f;
            }

            if (i == 3)
            {
                this.motionY = (double)f;
            }

            if (i == 4)
            {
                this.motionZ = (double)(-f);
            }

            if (i == 5)
            {
                this.motionZ = (double)f;
            }

            return true;
        }
    }

    public void setInWeb()
    {
        this.isInWeb = true;
        this.fallDistance = 0.0F;
    }

    public String getName()
    {
        if (this.hasCustomName())
        {
            return this.getCustomNameTag();
        }
        else
        {
            return StatCollector.translateToLocal(EntityList.getEntityTranslationKey(this));
        }
    }

    public Entity[] getParts()
    {
        return null;
    }

    public boolean isEntityEqual(Entity entityIn)
    {
        return this == entityIn;
    }

    public float getRotationYawHead()
    {
        return 0.0F;
    }

    public void setRotationYawHead(float rotation)
    {
    }

    public void setRenderYawOffset(float offset)
    {
    }

    public boolean canAttackWithItem()
    {
        return true;
    }

    public boolean hitByEntity(Entity entityIn)
    {
        return false;
    }

    public String toString()
    {
        return String.format(Locale.ROOT, "%s[\'%s\'/%d, l=\'%s\', x=%.2f, y=%.2f, z=%.2f]", new Object[] {this.getClass().getSimpleName(), this.getName(), Integer.valueOf(this.entityId), this.worldObj == null ? "~NULL~" : this.worldObj.getWorldInfo().getWorldName(), Double.valueOf(this.posX), Double.valueOf(this.posY), Double.valueOf(this.posZ)});
    }

    public boolean isEntityInvulnerable(DamageSource source)
    {
        return this.invulnerable && source != DamageSource.outOfWorld && !source.isCreativePlayer();
    }

    public void copyLocationAndAnglesFrom(Entity entityIn)
    {
        this.setLocationAndAngles(entityIn.posX, entityIn.posY, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
    }

    public void copyDataFromOld(Entity entityIn)
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        entityIn.writeToNBT(nBTTagCompound);
        this.readFromNBT(nBTTagCompound);
        this.timeUntilPortal = entityIn.timeUntilPortal;
        this.lastPortalPos = entityIn.lastPortalPos;
        this.lastPortalVec = entityIn.lastPortalVec;
        this.teleportDirection = entityIn.teleportDirection;
    }

    public void travelToDimension(int dimensionId)
    {
        if (!this.worldObj.isRemote && !this.isDead)
        {
            this.worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer minecraftServer = MinecraftServer.getServer();
            int i = this.dimension;
            WorldServer worldServer = minecraftServer.worldServerForDimension(i);
            WorldServer worldserver1 = minecraftServer.worldServerForDimension(dimensionId);
            this.dimension = dimensionId;

            if (i == 1 && dimensionId == 1)
            {
                worldserver1 = minecraftServer.worldServerForDimension(0);
                this.dimension = 0;
            }

            this.worldObj.removeEntity(this);
            this.isDead = false;
            this.worldObj.theProfiler.startSection("reposition");
            minecraftServer.getConfigurationManager().transferEntityToWorld(this, i, worldServer, worldserver1);
            this.worldObj.theProfiler.endStartSection("reloading");
            Entity entity = EntityList.createEntityByName(EntityList.getEntityString(this), worldserver1);

            if (entity != null)
            {
                entity.copyDataFromOld(this);

                if (i == 1 && dimensionId == 1)
                {
                    BlockPos blockPos = this.worldObj.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
                    entity.moveToBlockPosAndAngles(blockPos, entity.rotationYaw, entity.rotationPitch);
                }

                worldserver1.spawnEntityInWorld(entity);
            }

            this.isDead = true;
            this.worldObj.theProfiler.endSection();
            worldServer.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            this.worldObj.theProfiler.endSection();
        }
    }

    public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn)
    {
        return blockStateIn.getBlock().getExplosionResistance(this);
    }

    public boolean verifyExplosion(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn, float explosionPower)
    {
        return true;
    }

    public int getMaxFallHeight()
    {
        return 3;
    }

    public Vec3 getLastPortalVec()
    {
        return this.lastPortalVec;
    }

    public EnumFacing getTeleportDirection()
    {
        return this.teleportDirection;
    }

    public boolean doesEntityNotTriggerPressurePlate()
    {
        return false;
    }

    public void addEntityCrashInfo(CrashReportCategory category)
    {
        category.addCrashSectionCallable("Entity Type", new Callable<String>()
        {
            public String call() throws Exception
            {
                return EntityList.getEntityString(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
            }
        });
        category.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
        category.addCrashSectionCallable("Entity Name", new Callable<String>()
        {
            public String call() throws Exception
            {
                return Entity.this.getName();
            }
        });
        category.addCrashSection("Entity\'s Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", new Object[] {Double.valueOf(this.posX), Double.valueOf(this.posY), Double.valueOf(this.posZ)}));
        category.addCrashSection("Entity\'s Block location", CrashReportCategory.getCoordinateInfo((double)MathHelper.floor_double(this.posX), (double)MathHelper.floor_double(this.posY), (double)MathHelper.floor_double(this.posZ)));
        category.addCrashSection("Entity\'s Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", new Object[] {Double.valueOf(this.motionX), Double.valueOf(this.motionY), Double.valueOf(this.motionZ)}));
        category.addCrashSectionCallable("Entity\'s Rider", new Callable<String>()
        {
            public String call() throws Exception
            {
                return Entity.this.riddenByEntity.toString();
            }
        });
        category.addCrashSectionCallable("Entity\'s Vehicle", new Callable<String>()
        {
            public String call() throws Exception
            {
                return Entity.this.ridingEntity.toString();
            }
        });
    }

    public boolean canRenderOnFire()
    {
        return this.isBurning();
    }

    public UUID getUniqueID()
    {
        return this.entityUniqueID;
    }

    public boolean isPushedByWater()
    {
        return true;
    }

    public IChatComponent getDisplayName()
    {
        long now = System.nanoTime();

        if (this.displayNameCache != null && now < this.displayNameCacheExpiresAt)
        {
            return this.displayNameCache;
        }

        ChatComponentText chatcomponenttext = new ChatComponentText(this.getName());
        chatcomponenttext.getChatStyle().setChatHoverEvent(this.getHoverEvent());
        chatcomponenttext.getChatStyle().setInsertion(this.getUniqueID().toString());
        this.displayNameCache = chatcomponenttext;
        this.displayNameCacheExpiresAt = now + 50000000L;
        return chatcomponenttext;
    }

    public void setCustomNameTag(String name)
    {
        this.dataWatcher.updateObject(2, name);
    }

    public String getCustomNameTag()
    {
        return this.dataWatcher.getWatchableObjectString(2);
    }

    public boolean hasCustomName()
    {
        return this.dataWatcher.getWatchableObjectString(2).length() > 0;
    }

    public void setAlwaysRenderNameTag(boolean alwaysRenderNameTag)
    {
        this.dataWatcher.updateObject(3, Byte.valueOf((byte)(alwaysRenderNameTag ? 1 : 0)));
    }

    public boolean getAlwaysRenderNameTag()
    {
        return this.dataWatcher.getWatchableObjectByte(3) == 1;
    }

    public void setPositionAndUpdate(double x, double y, double z)
    {
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
    }

    public boolean getAlwaysRenderNameTagForRender()
    {
        return this.getAlwaysRenderNameTag();
    }

    public void onDataWatcherUpdate(int dataID)
    {
    }

    public EnumFacing getHorizontalFacing()
    {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double)(this.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
    }

    protected HoverEvent getHoverEvent()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        String s = EntityList.getEntityString(this);
        nBTTagCompound.setString("id", this.getUniqueID().toString());

        if (s != null)
        {
            nBTTagCompound.setString("type", s);
        }

        nBTTagCompound.setString("name", this.getName());
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new ChatComponentText(nBTTagCompound.toString()));
    }

    public boolean isSpectatedByPlayer(EntityPlayerMP player)
    {
        return true;
    }

    public AxisAlignedBB getEntityBoundingBox()
    {
        return this.boundingBox;
    }

    public void setEntityBoundingBox(AxisAlignedBB bb)
    {
        this.boundingBox = bb;
    }

    public float getEyeHeight()
    {
        return this.height * 0.85F;
    }

    public boolean isOutsideBorder()
    {
        return this.isOutsideBorder;
    }

    public void setOutsideBorder(boolean outsideBorder)
    {
        this.isOutsideBorder = outsideBorder;
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn)
    {
        return false;
    }

    public void addChatMessage(IChatComponent component)
    {
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return true;
    }

    public BlockPos getPosition()
    {
        return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
    }

    public Vec3 getPositionVector()
    {
        return new Vec3(this.posX, this.posY, this.posZ);
    }

    public World getEntityWorld()
    {
        return this.worldObj;
    }

    public Entity getCommandSenderEntity()
    {
        return this;
    }

    public boolean sendCommandFeedback()
    {
        return false;
    }

    public void setCommandStat(CommandResultStats.Type type, int amount)
    {
        this.cmdResultStats.setCommandStatScore(this, type, amount);
    }

    public CommandResultStats getCommandStats()
    {
        return this.cmdResultStats;
    }

    public void setCommandStats(Entity entityIn)
    {
        this.cmdResultStats.addAllStats(entityIn.getCommandStats());
    }

    public NBTTagCompound getNBTTagCompound()
    {
        return null;
    }

    public void clientUpdateEntityNBT(NBTTagCompound compound)
    {
    }

    public boolean interactAt(EntityPlayer player, Vec3 targetVec3)
    {
        return false;
    }

    public boolean isImmuneToExplosions()
    {
        return false;
    }

    protected void applyEnchantments(EntityLivingBase entityLivingBaseIn, Entity entityIn)
    {
        if (entityIn instanceof EntityLivingBase)
        {
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)entityIn, entityLivingBaseIn);
        }

        EnchantmentHelper.applyArthropodEnchantments(entityLivingBaseIn, entityIn);
    }
}
