package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Explosion
{
    private final boolean isFlaming;
    private final boolean isSmoking;
    private final Random explosionRNG;
    private final World worldObj;
    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;
    private final Entity exploder;
    private final float explosionSize;
    private final List<BlockPos> affectedBlockPositions;
    private final Map<EntityPlayer, Vec3> playerKnockbackMap;

    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions)
    {
        this(worldIn, entityIn, x, y, z, size, false, true, affectedPositions);
    }

    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking, List<BlockPos> affectedPositions)
    {
        this(worldIn, entityIn, x, y, z, size, flaming, smoking);
        this.affectedBlockPositions.addAll(affectedPositions);
    }

    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking)
    {
        this.explosionRNG = new Random();
        this.affectedBlockPositions = Lists.<BlockPos>newArrayList();
        this.playerKnockbackMap = Maps.<EntityPlayer, Vec3>newHashMap();
        this.worldObj = worldIn;
        this.exploder = entityIn;
        this.explosionSize = size;
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        this.isFlaming = flaming;
        this.isSmoking = smoking;
    }

    public void doExplosionA()
    {
        Set<BlockPos> set = Sets.<BlockPos>newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                for (int l = 0; l < 16; ++l)
                {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15)
                    {
                        double doubleValue = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double doubleValue2 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double doubleValue3 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                        double doubleValue4 = MathHelper.length_double(doubleValue, doubleValue2, doubleValue3);
                        doubleValue = doubleValue / doubleValue4;
                        doubleValue2 = doubleValue2 / doubleValue4;
                        doubleValue3 = doubleValue3 / doubleValue4;
                        float f = this.explosionSize * (0.7F + this.worldObj.rand.nextFloat() * 0.6F);
                        double doubleValue5 = this.explosionX;
                        double doubleValue6 = this.explosionY;
                        double doubleValue7 = this.explosionZ;

                        for (float floatValue2 = 0.3F; f > 0.0F; f -= 0.22500001F)
                        {
                            BlockPos blockPos = new BlockPos(doubleValue5, doubleValue6, doubleValue7);
                            IBlockState iblockstate = this.worldObj.getBlockState(blockPos);

                            if (iblockstate.getBlock().getMaterial() != Material.air)
                            {
                                float floatValue3 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.worldObj, blockPos, iblockstate) : iblockstate.getBlock().getExplosionResistance((Entity)null);
                                f -= (floatValue3 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (this.exploder == null || this.exploder.verifyExplosion(this, this.worldObj, blockPos, iblockstate, f)))
                            {
                                set.add(blockPos);
                            }

                            doubleValue5 += doubleValue * 0.30000001192092896D;
                            doubleValue6 += doubleValue2 * 0.30000001192092896D;
                            doubleValue7 += doubleValue3 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.affectedBlockPositions.addAll(set);
        float floatValue4 = this.explosionSize * 2.0F;
        int thirdIntValue = MathHelper.floor_double(this.explosionX - (double)floatValue4 - 1.0D);
        int fourthIntValue = MathHelper.floor_double(this.explosionX + (double)floatValue4 + 1.0D);
        int intValue2 = MathHelper.floor_double(this.explosionY - (double)floatValue4 - 1.0D);
        int intValue3 = MathHelper.floor_double(this.explosionY + (double)floatValue4 + 1.0D);
        int intValue4 = MathHelper.floor_double(this.explosionZ - (double)floatValue4 - 1.0D);
        int secondIntValue = MathHelper.floor_double(this.explosionZ + (double)floatValue4 + 1.0D);
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)thirdIntValue, (double)intValue2, (double)intValue4, (double)fourthIntValue, (double)intValue3, (double)secondIntValue));
        Vec3 localValue = new Vec3(this.explosionX, this.explosionY, this.explosionZ);

        for (int index = 0; index < list.size(); ++index)
        {
            Entity entity = (Entity)list.get(index);

            if (!entity.isImmuneToExplosions())
            {
                double doubleValue8 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)floatValue4;

                if (doubleValue8 <= 1.0D)
                {
                    double xCoordinate = entity.posX - this.explosionX;
                    double yCoordinate = entity.posY + (double)entity.getEyeHeight() - this.explosionY;
                    double zCoordinate = entity.posZ - this.explosionZ;
                    double doubleValue9 = (double)MathHelper.sqrt_double(xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate);

                    if (doubleValue9 != 0.0D)
                    {
                        xCoordinate = xCoordinate / doubleValue9;
                        yCoordinate = yCoordinate / doubleValue9;
                        zCoordinate = zCoordinate / doubleValue9;
                        double doubleValue10 = (double)this.worldObj.getBlockDensity(localValue, entity.getEntityBoundingBox());
                        double doubleValue11 = (1.0D - doubleValue8) * doubleValue10;
                        entity.attackEntityFrom(DamageSource.setExplosionSource(this), (float)((int)((doubleValue11 * doubleValue11 + doubleValue11) / 2.0D * 8.0D * (double)floatValue4 + 1.0D)));
                        double doubleValue12 = EnchantmentProtection.getBlastDamageReduction(entity, doubleValue11);
                        entity.motionX += xCoordinate * doubleValue12;
                        entity.motionY += yCoordinate * doubleValue12;
                        entity.motionZ += zCoordinate * doubleValue12;

                        if (entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.disableDamage)
                        {
                            this.playerKnockbackMap.put((EntityPlayer)entity, new Vec3(xCoordinate * doubleValue11, yCoordinate * doubleValue11, zCoordinate * doubleValue11));
                        }
                    }
                }
            }
        }
    }

    public void doExplosionB(boolean spawnParticles)
    {
        this.worldObj.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.explosionSize >= 2.0F && this.isSmoking)
        {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }
        else
        {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }

        if (this.isSmoking)
        {
            for (BlockPos blockpos : this.affectedBlockPositions)
            {
                Block block = this.worldObj.getBlockState(blockpos).getBlock();

                if (spawnParticles)
                {
                    double thirdDoubleValue = (double)((float)blockpos.getX() + this.worldObj.rand.nextFloat());
                    double fourthDoubleValue = (double)((float)blockpos.getY() + this.worldObj.rand.nextFloat());
                    double fifthDoubleValue = (double)((float)blockpos.getZ() + this.worldObj.rand.nextFloat());
                    double sixthDoubleValue = thirdDoubleValue - this.explosionX;
                    double seventhDoubleValue = fourthDoubleValue - this.explosionY;
                    double eighthDoubleValue = fifthDoubleValue - this.explosionZ;
                    double doubleValue = (double)MathHelper.sqrt_double(sixthDoubleValue * sixthDoubleValue + seventhDoubleValue * seventhDoubleValue + eighthDoubleValue * eighthDoubleValue);
                    sixthDoubleValue = sixthDoubleValue / doubleValue;
                    seventhDoubleValue = seventhDoubleValue / doubleValue;
                    eighthDoubleValue = eighthDoubleValue / doubleValue;
                    double secondDoubleValue = 0.5D / (doubleValue / (double)this.explosionSize + 0.1D);
                    secondDoubleValue = secondDoubleValue * (double)(this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3F);
                    sixthDoubleValue = sixthDoubleValue * secondDoubleValue;
                    seventhDoubleValue = seventhDoubleValue * secondDoubleValue;
                    eighthDoubleValue = eighthDoubleValue * secondDoubleValue;
                    this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (thirdDoubleValue + this.explosionX * 1.0D) / 2.0D, (fourthDoubleValue + this.explosionY * 1.0D) / 2.0D, (fifthDoubleValue + this.explosionZ * 1.0D) / 2.0D, sixthDoubleValue, seventhDoubleValue, eighthDoubleValue, EnumParticleTypes.EMPTY_ARGS);
                    this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, thirdDoubleValue, fourthDoubleValue, fifthDoubleValue, sixthDoubleValue, seventhDoubleValue, eighthDoubleValue, EnumParticleTypes.EMPTY_ARGS);
                }

                if (block.getMaterial() != Material.air)
                {
                    if (block.canDropFromExplosion(this))
                    {
                        block.dropBlockAsItemWithChance(this.worldObj, blockpos, this.worldObj.getBlockState(blockpos), 1.0F / this.explosionSize, 0);
                    }

                    this.worldObj.setBlockState(blockpos, Blocks.air.getDefaultState(), 3);
                    block.onBlockDestroyedByExplosion(this.worldObj, blockpos, this);
                }
            }
        }

        if (this.isFlaming)
        {
            for (BlockPos blockpos1 : this.affectedBlockPositions)
            {
                if (this.worldObj.getBlockState(blockpos1).getBlock().getMaterial() == Material.air && this.worldObj.getBlockState(blockpos1.down()).getBlock().isFullBlock() && this.explosionRNG.nextInt(3) == 0)
                {
                    this.worldObj.setBlockState(blockpos1, Blocks.fire.getDefaultState());
                }
            }
        }
    }

    public Map<EntityPlayer, Vec3> getPlayerKnockbackMap()
    {
        return this.playerKnockbackMap;
    }

    public EntityLivingBase getExplosivePlacedBy()
    {
        return this.exploder == null ? null : (this.exploder instanceof EntityTNTPrimed ? ((EntityTNTPrimed)this.exploder).getTntPlacedBy() : (this.exploder instanceof EntityLivingBase ? (EntityLivingBase)this.exploder : null));
    }

    public void clearAffectedBlockPositions()
    {
        this.affectedBlockPositions.clear();
    }

    public List<BlockPos> getAffectedBlockPositions()
    {
        return this.affectedBlockPositions;
    }
}
