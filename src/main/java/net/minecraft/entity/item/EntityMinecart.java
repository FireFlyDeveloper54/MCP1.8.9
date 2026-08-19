package net.minecraft.entity.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class EntityMinecart extends Entity implements IWorldNameable
{
    private boolean isInReverse;
    private String entityName;
    private static final int[][][] matrix = new int[][][] {{{0, 0, -1}, {0, 0, 1}}, {{ -1, 0, 0}, {1, 0, 0}}, {{ -1, -1, 0}, {1, 0, 0}}, {{ -1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, { -1, 0, 0}}, {{0, 0, -1}, { -1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
    private int turnProgress;
    private double minecartX;
    private double minecartY;
    private double minecartZ;
    private double minecartYaw;
    private double minecartPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private final BlockPos.MutableBlockPos railSamplePos = new BlockPos.MutableBlockPos();

    public EntityMinecart(World worldIn)
    {
        super(worldIn);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.7F);
    }

    public static EntityMinecart getMinecart(World worldIn, double x, double y, double z, EntityMinecart.EnumMinecartType type)
    {
        switch (type)
        {
            case CHEST:
                return new EntityMinecartChest(worldIn, x, y, z);

            case FURNACE:
                return new EntityMinecartFurnace(worldIn, x, y, z);

            case TNT:
                return new EntityMinecartTNT(worldIn, x, y, z);

            case SPAWNER:
                return new EntityMinecartMobSpawner(worldIn, x, y, z);

            case HOPPER:
                return new EntityMinecartHopper(worldIn, x, y, z);

            case COMMAND_BLOCK:
                return new EntityMinecartCommandBlock(worldIn, x, y, z);

            default:
                return new EntityMinecartEmpty(worldIn, x, y, z);
        }
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        this.dataWatcher.addObject(17, Integer.valueOf(0));
        this.dataWatcher.addObject(18, Integer.valueOf(1));
        this.dataWatcher.addObject(19, Float.valueOf(0.0F));
        this.dataWatcher.addObject(20, Integer.valueOf(0));
        this.dataWatcher.addObject(21, Integer.valueOf(6));
        this.dataWatcher.addObject(22, Byte.valueOf((byte)0));
    }

    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
    }

    public AxisAlignedBB getCollisionBoundingBox()
    {
        return null;
    }

    public boolean canBePushed()
    {
        return true;
    }

    public EntityMinecart(World worldIn, double x, double y, double z)
    {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    public double getMountedYOffset()
    {
        return 0.0D;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!this.worldObj.isRemote && !this.isDead)
        {
            if (this.isEntityInvulnerable(source))
            {
                return false;
            }
            else
            {
                this.setRollingDirection(-this.getRollingDirection());
                this.setRollingAmplitude(10);
                this.setBeenAttacked();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean isCreativePlayerAttack = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;

                if (isCreativePlayerAttack || this.getDamage() > 40.0F)
                {
                    if (this.riddenByEntity != null)
                    {
                        this.riddenByEntity.mountEntity((Entity)null);
                    }

                    if (isCreativePlayerAttack && !this.hasCustomName())
                    {
                        this.setDead();
                    }
                    else
                    {
                        this.killMinecart(source);
                    }
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

    public void killMinecart(DamageSource source)
    {
        this.setDead();

        if (this.worldObj.getGameRules().getBoolean("doEntityDrops"))
        {
            ItemStack itemStack = new ItemStack(Items.minecart, 1);

            if (this.entityName != null)
            {
                itemStack.setStackDisplayName(this.entityName);
            }

            this.entityDropItem(itemStack, 0.0F);
        }
    }

    public void performHurtAnimation()
    {
        this.setRollingDirection(-this.getRollingDirection());
        this.setRollingAmplitude(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    public void setDead()
    {
        super.setDead();
    }

    public void onUpdate()
    {
        if (this.getRollingAmplitude() > 0)
        {
            this.setRollingAmplitude(this.getRollingAmplitude() - 1);
        }

        if (this.getDamage() > 0.0F)
        {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.posY < -64.0D)
        {
            this.kill();
        }

        if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer)
        {
            this.worldObj.theProfiler.startSection("portal");
            MinecraftServer minecraftServer = ((WorldServer)this.worldObj).getMinecraftServer();
            int maxPortalTime = this.getMaxInPortalTime();

            if (this.inPortal)
            {
                if (minecraftServer.getAllowNether())
                {
                    if (this.ridingEntity == null && this.portalCounter++ >= maxPortalTime)
                    {
                        this.portalCounter = maxPortalTime;
                        this.timeUntilPortal = this.getPortalCooldown();
                        int targetDimensionId;

                        if (this.worldObj.provider.getDimensionId() == -1)
                        {
                            targetDimensionId = 0;
                        }
                        else
                        {
                            targetDimensionId = -1;
                        }

                        this.travelToDimension(targetDimensionId);
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

        if (this.worldObj.isRemote)
        {
            if (this.turnProgress > 0)
            {
                double interpolatedX = this.posX + (this.minecartX - this.posX) / (double)this.turnProgress;
                double interpolatedY = this.posY + (this.minecartY - this.posY) / (double)this.turnProgress;
                double interpolatedZ = this.posZ + (this.minecartZ - this.posZ) / (double)this.turnProgress;
                double yawDelta = MathHelper.wrapAngleTo180_double(this.minecartYaw - (double)this.rotationYaw);
                this.rotationYaw = (float)((double)this.rotationYaw + yawDelta / (double)this.turnProgress);
                this.rotationPitch = (float)((double)this.rotationPitch + (this.minecartPitch - (double)this.rotationPitch) / (double)this.turnProgress);
                --this.turnProgress;
                this.setPosition(interpolatedX, interpolatedY, interpolatedZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
            else
            {
                this.setPosition(this.posX, this.posY, this.posZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
        }
        else
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY -= 0.03999999910593033D;
            int railX = MathHelper.floor_double(this.posX);
            int railY = MathHelper.floor_double(this.posY);
            int railZ = MathHelper.floor_double(this.posZ);

            if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(railX, railY - 1, railZ)))
            {
                --railY;
            }

            BlockPos railPos = new BlockPos(railX, railY, railZ);
            IBlockState railState = this.worldObj.getBlockState(railPos);

            if (BlockRailBase.isRailBlock(railState))
            {
                this.moveAlongTrack(railPos, railState);

                if (railState.getBlock() == Blocks.activator_rail)
                {
                    this.onActivatorRailPass(railX, railY, railZ, ((Boolean)railState.getValue(BlockRailPowered.POWERED)).booleanValue());
                }
            }
            else
            {
                this.moveDerailedMinecart();
            }

            this.doBlockCollisions();
            this.rotationPitch = 0.0F;
            double deltaX = this.prevPosX - this.posX;
            double deltaZ = this.prevPosZ - this.posZ;

            if (deltaX * deltaX + deltaZ * deltaZ > 0.001D)
            {
                this.rotationYaw = (float)(MathHelper.atan2(deltaZ, deltaX) * 180.0D / Math.PI);

                if (this.isInReverse)
                {
                    this.rotationYaw += 180.0F;
                }
            }

            double yawChange = (double)MathHelper.wrapAngleTo180_float(this.rotationYaw - this.prevRotationYaw);

            if (yawChange < -170.0D || yawChange >= 170.0D)
            {
                this.rotationYaw += 180.0F;
                this.isInReverse = !this.isInReverse;
            }

            this.setRotation(this.rotationYaw, this.rotationPitch);

            for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D)))
            {
                if (entity != this.riddenByEntity && entity.canBePushed() && entity instanceof EntityMinecart)
                {
                    entity.applyEntityCollision(this);
                }
            }

            if (this.riddenByEntity != null && this.riddenByEntity.isDead)
            {
                if (this.riddenByEntity.ridingEntity == this)
                {
                    this.riddenByEntity.ridingEntity = null;
                }

                this.riddenByEntity = null;
            }

            this.handleWaterMovement();
        }
    }

    protected double getMaximumSpeed()
    {
        return 0.4D;
    }

    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower)
    {
    }

    protected void moveDerailedMinecart()
    {
        double maxSpeed = this.getMaximumSpeed();
        this.motionX = MathHelper.clamp_double(this.motionX, -maxSpeed, maxSpeed);
        this.motionZ = MathHelper.clamp_double(this.motionZ, -maxSpeed, maxSpeed);

        if (this.onGround)
        {
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (!this.onGround)
        {
            this.motionX *= 0.949999988079071D;
            this.motionY *= 0.949999988079071D;
            this.motionZ *= 0.949999988079071D;
        }
    }

    @SuppressWarnings("incomplete-switch")
    protected void moveAlongTrack(BlockPos railPos, IBlockState railState)
    {
        this.fallDistance = 0.0F;
        Vec3 previousRailPos = this.getPos(this.posX, this.posY, this.posZ);
        this.posY = (double)railPos.getY();
        boolean railPowered = false;
        boolean railBraking = false;
        BlockRailBase railBlock = (BlockRailBase)railState.getBlock();

        if (railBlock == Blocks.golden_rail)
        {
            railPowered = ((Boolean)railState.getValue(BlockRailPowered.POWERED)).booleanValue();
            railBraking = !railPowered;
        }

        double slopeAcceleration = 0.0078125D;
        BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection)railState.getValue(railBlock.getShapeProperty());

        switch (railDirection)
        {
            case ASCENDING_EAST:
                this.motionX -= slopeAcceleration;
                ++this.posY;
                break;

            case ASCENDING_WEST:
                this.motionX += slopeAcceleration;
                ++this.posY;
                break;

            case ASCENDING_NORTH:
                this.motionZ += slopeAcceleration;
                ++this.posY;
                break;

            case ASCENDING_SOUTH:
                this.motionZ -= slopeAcceleration;
                ++this.posY;
        }

        int[][] railEndpoints = matrix[railDirection.getMetadata()];
        double railDeltaX = (double)(railEndpoints[1][0] - railEndpoints[0][0]);
        double railDeltaZ = (double)(railEndpoints[1][2] - railEndpoints[0][2]);
        double railLength = MathHelper.length_double(railDeltaX, railDeltaZ);
        double motionDotRail = this.motionX * railDeltaX + this.motionZ * railDeltaZ;

        if (motionDotRail < 0.0D)
        {
            railDeltaX = -railDeltaX;
            railDeltaZ = -railDeltaZ;
        }

        double horizontalSpeed = MathHelper.length_double(this.motionX, this.motionZ);

        if (horizontalSpeed > 2.0D)
        {
            horizontalSpeed = 2.0D;
        }

        this.motionX = horizontalSpeed * railDeltaX / railLength;
        this.motionZ = horizontalSpeed * railDeltaZ / railLength;

        if (this.riddenByEntity instanceof EntityLivingBase)
        {
            double riderForwardInput = (double)((EntityLivingBase)this.riddenByEntity).moveForward;

            if (riderForwardInput > 0.0D)
            {
                double riderPushX = -Math.sin((double)(this.riddenByEntity.rotationYaw * (float)Math.PI / 180.0F));
                double riderPushZ = Math.cos((double)(this.riddenByEntity.rotationYaw * (float)Math.PI / 180.0F));
                double motionSq = this.motionX * this.motionX + this.motionZ * this.motionZ;

                if (motionSq < 0.01D)
                {
                    this.motionX += riderPushX * 0.1D;
                    this.motionZ += riderPushZ * 0.1D;
                    railBraking = false;
                }
            }
        }

        if (railBraking)
        {
            double brakingSpeed = MathHelper.length_double(this.motionX, this.motionZ);

            if (brakingSpeed < 0.03D)
            {
                this.motionX *= 0.0D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.0D;
            }
            else
            {
                this.motionX *= 0.5D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.5D;
            }
        }

        double railOffset = 0.0D;
        double startX = (double)railPos.getX() + 0.5D + (double)railEndpoints[0][0] * 0.5D;
        double startZ = (double)railPos.getZ() + 0.5D + (double)railEndpoints[0][2] * 0.5D;
        double endX = (double)railPos.getX() + 0.5D + (double)railEndpoints[1][0] * 0.5D;
        double endZ = (double)railPos.getZ() + 0.5D + (double)railEndpoints[1][2] * 0.5D;
        railDeltaX = endX - startX;
        railDeltaZ = endZ - startZ;

        if (railDeltaX == 0.0D)
        {
            this.posX = (double)railPos.getX() + 0.5D;
            railOffset = this.posZ - (double)railPos.getZ();
        }
        else if (railDeltaZ == 0.0D)
        {
            this.posZ = (double)railPos.getZ() + 0.5D;
            railOffset = this.posX - (double)railPos.getX();
        }
        else
        {
            double offsetFromStartX = this.posX - startX;
            double offsetFromStartZ = this.posZ - startZ;
            railOffset = (offsetFromStartX * railDeltaX + offsetFromStartZ * railDeltaZ) * 2.0D;
        }

        this.posX = startX + railDeltaX * railOffset;
        this.posZ = startZ + railDeltaZ * railOffset;
        this.setPosition(this.posX, this.posY, this.posZ);
        double moveX = this.motionX;
        double moveZ = this.motionZ;

        if (this.riddenByEntity != null)
        {
            moveX *= 0.75D;
            moveZ *= 0.75D;
        }

        double maxSpeed = this.getMaximumSpeed();
        moveX = MathHelper.clamp_double(moveX, -maxSpeed, maxSpeed);
        moveZ = MathHelper.clamp_double(moveZ, -maxSpeed, maxSpeed);
        this.moveEntity(moveX, 0.0D, moveZ);

        if (railEndpoints[0][1] != 0 && MathHelper.floor_double(this.posX) - railPos.getX() == railEndpoints[0][0] && MathHelper.floor_double(this.posZ) - railPos.getZ() == railEndpoints[0][2])
        {
            this.setPosition(this.posX, this.posY + (double)railEndpoints[0][1], this.posZ);
        }
        else if (railEndpoints[1][1] != 0 && MathHelper.floor_double(this.posX) - railPos.getX() == railEndpoints[1][0] && MathHelper.floor_double(this.posZ) - railPos.getZ() == railEndpoints[1][2])
        {
            this.setPosition(this.posX, this.posY + (double)railEndpoints[1][1], this.posZ);
        }

        this.applyDrag();
        Vec3 currentRailPos = this.getPos(this.posX, this.posY, this.posZ);

        if (currentRailPos != null && previousRailPos != null)
        {
            double verticalMotionAdjustment = (previousRailPos.yCoord - currentRailPos.yCoord) * 0.05D;
            horizontalSpeed = MathHelper.length_double(this.motionX, this.motionZ);

            if (horizontalSpeed > 0.0D)
            {
                this.motionX = this.motionX / horizontalSpeed * (horizontalSpeed + verticalMotionAdjustment);
                this.motionZ = this.motionZ / horizontalSpeed * (horizontalSpeed + verticalMotionAdjustment);
            }

            this.setPosition(this.posX, currentRailPos.yCoord, this.posZ);
        }

        int currentRailX = MathHelper.floor_double(this.posX);
        int currentRailZ = MathHelper.floor_double(this.posZ);

        if (currentRailX != railPos.getX() || currentRailZ != railPos.getZ())
        {
            horizontalSpeed = MathHelper.length_double(this.motionX, this.motionZ);
            this.motionX = horizontalSpeed * (double)(currentRailX - railPos.getX());
            this.motionZ = horizontalSpeed * (double)(currentRailZ - railPos.getZ());
        }

        if (railPowered)
        {
            double poweredSpeed = MathHelper.length_double(this.motionX, this.motionZ);

            if (poweredSpeed > 0.01D)
            {
                double poweredAcceleration = 0.06D;
                this.motionX += this.motionX / poweredSpeed * poweredAcceleration;
                this.motionZ += this.motionZ / poweredSpeed * poweredAcceleration;
            }
            else if (railDirection == BlockRailBase.EnumRailDirection.EAST_WEST)
            {
                if (this.worldObj.getBlockState(railPos.west()).getBlock().isNormalCube())
                {
                    this.motionX = 0.02D;
                }
                else if (this.worldObj.getBlockState(railPos.east()).getBlock().isNormalCube())
                {
                    this.motionX = -0.02D;
                }
            }
            else if (railDirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH)
            {
                if (this.worldObj.getBlockState(railPos.north()).getBlock().isNormalCube())
                {
                    this.motionZ = 0.02D;
                }
                else if (this.worldObj.getBlockState(railPos.south()).getBlock().isNormalCube())
                {
                    this.motionZ = -0.02D;
                }
            }
        }
    }

    protected void applyDrag()
    {
        if (this.riddenByEntity != null)
        {
            this.motionX *= 0.996999979019165D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.996999979019165D;
        }
        else
        {
            this.motionX *= 0.9599999785423279D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.9599999785423279D;
        }
    }

    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float halfWidth = this.width / 2.0F;
        float entityHeight = this.height;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double)halfWidth, y, z - (double)halfWidth, x + (double)halfWidth, y + (double)entityHeight, z + (double)halfWidth));
    }

    public Vec3 getPosOffset(double x, double y, double z, double offset)
    {
        int railX = MathHelper.floor_double(x);
        int railY = MathHelper.floor_double(y);
        int railZ = MathHelper.floor_double(z);

        if (BlockRailBase.isRailBlock(this.worldObj, this.railSamplePos.set(railX, railY - 1, railZ)))
        {
            --railY;
        }

        IBlockState railState = this.worldObj.getBlockState(this.railSamplePos.set(railX, railY, railZ));

        if (BlockRailBase.isRailBlock(railState))
        {
            BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection)railState.getValue(((BlockRailBase)railState.getBlock()).getShapeProperty());
            y = (double)railY;

            if (railDirection.isAscending())
            {
                y = (double)(railY + 1);
            }

            int[][] railEndpoints = matrix[railDirection.getMetadata()];
            double railDeltaX = (double)(railEndpoints[1][0] - railEndpoints[0][0]);
            double railDeltaZ = (double)(railEndpoints[1][2] - railEndpoints[0][2]);
            double railLength = MathHelper.length_double(railDeltaX, railDeltaZ);
            railDeltaX = railDeltaX / railLength;
            railDeltaZ = railDeltaZ / railLength;
            x = x + railDeltaX * offset;
            z = z + railDeltaZ * offset;

            if (railEndpoints[0][1] != 0 && MathHelper.floor_double(x) - railX == railEndpoints[0][0] && MathHelper.floor_double(z) - railZ == railEndpoints[0][2])
            {
                y += (double)railEndpoints[0][1];
            }
            else if (railEndpoints[1][1] != 0 && MathHelper.floor_double(x) - railX == railEndpoints[1][0] && MathHelper.floor_double(z) - railZ == railEndpoints[1][2])
            {
                y += (double)railEndpoints[1][1];
            }

            return this.getPos(x, y, z);
        }
        else
        {
            return null;
        }
    }

    public Vec3 getPos(double x, double y, double z)
    {
        int railX = MathHelper.floor_double(x);
        int railY = MathHelper.floor_double(y);
        int railZ = MathHelper.floor_double(z);

        if (BlockRailBase.isRailBlock(this.worldObj, this.railSamplePos.set(railX, railY - 1, railZ)))
        {
            --railY;
        }

        IBlockState railState = this.worldObj.getBlockState(this.railSamplePos.set(railX, railY, railZ));

        if (BlockRailBase.isRailBlock(railState))
        {
            BlockRailBase.EnumRailDirection railDirection = (BlockRailBase.EnumRailDirection)railState.getValue(((BlockRailBase)railState.getBlock()).getShapeProperty());
            int[][] railEndpoints = matrix[railDirection.getMetadata()];
            double railOffset = 0.0D;
            double startX = (double)railX + 0.5D + (double)railEndpoints[0][0] * 0.5D;
            double startY = (double)railY + 0.0625D + (double)railEndpoints[0][1] * 0.5D;
            double startZ = (double)railZ + 0.5D + (double)railEndpoints[0][2] * 0.5D;
            double endX = (double)railX + 0.5D + (double)railEndpoints[1][0] * 0.5D;
            double endY = (double)railY + 0.0625D + (double)railEndpoints[1][1] * 0.5D;
            double endZ = (double)railZ + 0.5D + (double)railEndpoints[1][2] * 0.5D;
            double railDeltaX = endX - startX;
            double railDeltaY = (endY - startY) * 2.0D;
            double railDeltaZ = endZ - startZ;

            if (railDeltaX == 0.0D)
            {
                x = (double)railX + 0.5D;
                railOffset = z - (double)railZ;
            }
            else if (railDeltaZ == 0.0D)
            {
                z = (double)railZ + 0.5D;
                railOffset = x - (double)railX;
            }
            else
            {
                double offsetFromStartX = x - startX;
                double offsetFromStartZ = z - startZ;
                railOffset = (offsetFromStartX * railDeltaX + offsetFromStartZ * railDeltaZ) * 2.0D;
            }

            x = startX + railDeltaX * railOffset;
            y = startY + railDeltaY * railOffset;
            z = startZ + railDeltaZ * railOffset;

            if (railDeltaY < 0.0D)
            {
                ++y;
            }

            if (railDeltaY > 0.0D)
            {
                y += 0.5D;
            }

            return new Vec3(x, y, z);
        }
        else
        {
            return null;
        }
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        if (tagCompund.getBoolean("CustomDisplayTile"))
        {
            int displayData = tagCompund.getInteger("DisplayData");

            if (tagCompund.hasKey("DisplayTile", 8))
            {
                Block block = Block.getBlockFromName(tagCompund.getString("DisplayTile"));

                if (block == null)
                {
                    this.setDisplayTile(Blocks.air.getDefaultState());
                }
                else
                {
                    this.setDisplayTile(block.getStateFromMeta(displayData));
                }
            }
            else
            {
                Block legacyBlock = Block.getBlockById(tagCompund.getInteger("DisplayTile"));

                if (legacyBlock == null)
                {
                    this.setDisplayTile(Blocks.air.getDefaultState());
                }
                else
                {
                    this.setDisplayTile(legacyBlock.getStateFromMeta(displayData));
                }
            }

            this.setDisplayTileOffset(tagCompund.getInteger("DisplayOffset"));
        }

        if (tagCompund.hasKey("CustomName", 8) && tagCompund.getString("CustomName").length() > 0)
        {
            this.entityName = tagCompund.getString("CustomName");
        }
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        if (this.hasDisplayTile())
        {
            tagCompound.setBoolean("CustomDisplayTile", true);
            IBlockState displayTileState = this.getDisplayTile();
            ResourceLocation resourceLocation = (ResourceLocation)Block.blockRegistry.getNameForObject(displayTileState.getBlock());
            tagCompound.setString("DisplayTile", resourceLocation == null ? "" : resourceLocation.toString());
            tagCompound.setInteger("DisplayData", displayTileState.getBlock().getMetaFromState(displayTileState));
            tagCompound.setInteger("DisplayOffset", this.getDisplayTileOffset());
        }

        if (this.entityName != null && this.entityName.length() > 0)
        {
            tagCompound.setString("CustomName", this.entityName);
        }
    }

    public void applyEntityCollision(Entity entityIn)
    {
        if (!this.worldObj.isRemote)
        {
            if (!entityIn.noClip && !this.noClip)
            {
                if (entityIn != this.riddenByEntity)
                {
                    if (entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityPlayer) && !(entityIn instanceof EntityIronGolem) && this.getMinecartType() == EntityMinecart.EnumMinecartType.RIDEABLE && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D && this.riddenByEntity == null && entityIn.ridingEntity == null)
                    {
                        entityIn.mountEntity(this);
                    }

                    double collisionX = entityIn.posX - this.posX;
                    double collisionZ = entityIn.posZ - this.posZ;
                    double collisionDistanceSq = collisionX * collisionX + collisionZ * collisionZ;

                    if (collisionDistanceSq >= 9.999999747378752E-5D)
                    {
                        double collisionDistance = (double)MathHelper.sqrt_double(collisionDistanceSq);
                        collisionX = collisionX / collisionDistance;
                        collisionZ = collisionZ / collisionDistance;
                        double pushScale = 1.0D / collisionDistance;

                        if (pushScale > 1.0D)
                        {
                            pushScale = 1.0D;
                        }

                        collisionX = collisionX * pushScale;
                        collisionZ = collisionZ * pushScale;
                        collisionX = collisionX * 0.10000000149011612D;
                        collisionZ = collisionZ * 0.10000000149011612D;
                        collisionX = collisionX * (double)(1.0F - this.entityCollisionReduction);
                        collisionZ = collisionZ * (double)(1.0F - this.entityCollisionReduction);
                        collisionX = collisionX * 0.5D;
                        collisionZ = collisionZ * 0.5D;

                        if (entityIn instanceof EntityMinecart)
                        {
                            double cartSeparationX = entityIn.posX - this.posX;
                            double cartSeparationZ = entityIn.posZ - this.posZ;
                            Vec3 collisionDirection = (new Vec3(cartSeparationX, 0.0D, cartSeparationZ)).normalize();
                            Vec3 cartForward = (new Vec3((double)MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F), 0.0D, (double)MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F))).normalize();
                            double trackAlignment = Math.abs(collisionDirection.dotProduct(cartForward));

                            if (trackAlignment < 0.800000011920929D)
                            {
                                return;
                            }

                            double combinedMotionX = entityIn.motionX + this.motionX;
                            double combinedMotionZ = entityIn.motionZ + this.motionZ;

                            if (((EntityMinecart)entityIn).getMinecartType() == EntityMinecart.EnumMinecartType.FURNACE && this.getMinecartType() != EntityMinecart.EnumMinecartType.FURNACE)
                            {
                                this.motionX *= 0.20000000298023224D;
                                this.motionZ *= 0.20000000298023224D;
                                this.addVelocity(entityIn.motionX - collisionX, 0.0D, entityIn.motionZ - collisionZ);
                                entityIn.motionX *= 0.949999988079071D;
                                entityIn.motionZ *= 0.949999988079071D;
                            }
                            else if (((EntityMinecart)entityIn).getMinecartType() != EntityMinecart.EnumMinecartType.FURNACE && this.getMinecartType() == EntityMinecart.EnumMinecartType.FURNACE)
                            {
                                entityIn.motionX *= 0.20000000298023224D;
                                entityIn.motionZ *= 0.20000000298023224D;
                                entityIn.addVelocity(this.motionX + collisionX, 0.0D, this.motionZ + collisionZ);
                                this.motionX *= 0.949999988079071D;
                                this.motionZ *= 0.949999988079071D;
                            }
                            else
                            {
                                combinedMotionX = combinedMotionX / 2.0D;
                                combinedMotionZ = combinedMotionZ / 2.0D;
                                this.motionX *= 0.20000000298023224D;
                                this.motionZ *= 0.20000000298023224D;
                                this.addVelocity(combinedMotionX - collisionX, 0.0D, combinedMotionZ - collisionZ);
                                entityIn.motionX *= 0.20000000298023224D;
                                entityIn.motionZ *= 0.20000000298023224D;
                                entityIn.addVelocity(combinedMotionX + collisionX, 0.0D, combinedMotionZ + collisionZ);
                            }
                        }
                        else
                        {
                            this.addVelocity(-collisionX, 0.0D, -collisionZ);
                            entityIn.addVelocity(collisionX / 4.0D, 0.0D, collisionZ / 4.0D);
                        }
                    }
                }
            }
        }
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.minecartX = x;
        this.minecartY = y;
        this.minecartZ = z;
        this.minecartYaw = (double)yaw;
        this.minecartPitch = (double)pitch;
        this.turnProgress = posRotationIncrements + 2;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

    public void setVelocity(double x, double y, double z)
    {
        this.velocityX = this.motionX = x;
        this.velocityY = this.motionY = y;
        this.velocityZ = this.motionZ = z;
    }

    public void setDamage(float damage)
    {
        this.dataWatcher.updateObject(19, Float.valueOf(damage));
    }

    public float getDamage()
    {
        return this.dataWatcher.getWatchableObjectFloat(19);
    }

    public void setRollingAmplitude(int amplitude)
    {
        this.dataWatcher.updateObject(17, Integer.valueOf(amplitude));
    }

    public int getRollingAmplitude()
    {
        return this.dataWatcher.getWatchableObjectInt(17);
    }

    public void setRollingDirection(int direction)
    {
        this.dataWatcher.updateObject(18, Integer.valueOf(direction));
    }

    public int getRollingDirection()
    {
        return this.dataWatcher.getWatchableObjectInt(18);
    }

    public abstract EntityMinecart.EnumMinecartType getMinecartType();

    public IBlockState getDisplayTile()
    {
        return !this.hasDisplayTile() ? this.getDefaultDisplayTile() : Block.getStateById(this.getDataWatcher().getWatchableObjectInt(20));
    }

    public IBlockState getDefaultDisplayTile()
    {
        return Blocks.air.getDefaultState();
    }

    public int getDisplayTileOffset()
    {
        return !this.hasDisplayTile() ? this.getDefaultDisplayTileOffset() : this.getDataWatcher().getWatchableObjectInt(21);
    }

    public int getDefaultDisplayTileOffset()
    {
        return 6;
    }

    public void setDisplayTile(IBlockState displayTile)
    {
        this.getDataWatcher().updateObject(20, Integer.valueOf(Block.getStateId(displayTile)));
        this.setHasDisplayTile(true);
    }

    public void setDisplayTileOffset(int offset)
    {
        this.getDataWatcher().updateObject(21, Integer.valueOf(offset));
        this.setHasDisplayTile(true);
    }

    public boolean hasDisplayTile()
    {
        return this.getDataWatcher().getWatchableObjectByte(22) == 1;
    }

    public void setHasDisplayTile(boolean hasDisplayTile)
    {
        this.getDataWatcher().updateObject(22, Byte.valueOf((byte)(hasDisplayTile ? 1 : 0)));
    }

    public void setCustomNameTag(String name)
    {
        this.entityName = name;
    }

    public String getName()
    {
        return this.entityName != null ? this.entityName : super.getName();
    }

    public boolean hasCustomName()
    {
        return this.entityName != null;
    }

    public String getCustomNameTag()
    {
        return this.entityName;
    }

    public IChatComponent getDisplayName()
    {
        if (this.hasCustomName())
        {
            ChatComponentText chatComponentText = new ChatComponentText(this.entityName);
            chatComponentText.getChatStyle().setChatHoverEvent(this.getHoverEvent());
            chatComponentText.getChatStyle().setInsertion(this.getUniqueID().toString());
            return chatComponentText;
        }
        else
        {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation(this.getName(), new Object[0]);
            chatComponentTranslation.getChatStyle().setChatHoverEvent(this.getHoverEvent());
            chatComponentTranslation.getChatStyle().setInsertion(this.getUniqueID().toString());
            return chatComponentTranslation;
        }
    }

    public static enum EnumMinecartType
    {
        RIDEABLE(0, "MinecartRideable"),
        CHEST(1, "MinecartChest"),
        FURNACE(2, "MinecartFurnace"),
        TNT(3, "MinecartTNT"),
        SPAWNER(4, "MinecartSpawner"),
        HOPPER(5, "MinecartHopper"),
        COMMAND_BLOCK(6, "MinecartCommandBlock");

        private static final Map<Integer, EntityMinecart.EnumMinecartType> ID_LOOKUP = Maps.<Integer, EntityMinecart.EnumMinecartType>newHashMap();
        public static final EntityMinecart.EnumMinecartType[] VALUES = values();
        private final int networkID;
        private final String name;

        private EnumMinecartType(int networkID, String name)
        {
            this.networkID = networkID;
            this.name = name;
        }

        public int getNetworkID()
        {
            return this.networkID;
        }

        public String getName()
        {
            return this.name;
        }

        public static EntityMinecart.EnumMinecartType byNetworkID(int id)
        {
            EntityMinecart.EnumMinecartType minecartType = (EntityMinecart.EnumMinecartType)ID_LOOKUP.get(Integer.valueOf(id));
            return minecartType == null ? RIDEABLE : minecartType;
        }

        static {
            for (EntityMinecart.EnumMinecartType minecartType : VALUES)
            {
                ID_LOOKUP.put(Integer.valueOf(minecartType.getNetworkID()), minecartType);
            }
        }
    }
}
