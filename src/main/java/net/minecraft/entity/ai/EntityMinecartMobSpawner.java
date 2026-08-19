package net.minecraft.entity.ai;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityMinecartMobSpawner extends EntityMinecart
{
    private final MobSpawnerBaseLogic mobSpawnerLogic = new MobSpawnerBaseLogic()
    {
        public void broadcastEvent(int id)
        {
            EntityMinecartMobSpawner.this.worldObj.setEntityState(EntityMinecartMobSpawner.this, (byte)id);
        }
        public World getSpawnerWorld()
        {
            return EntityMinecartMobSpawner.this.worldObj;
        }
        public BlockPos getSpawnerPosition()
        {
            return new BlockPos(EntityMinecartMobSpawner.this);
        }
    };

    public EntityMinecartMobSpawner(World worldIn)
    {
        super(worldIn);
    }

    public EntityMinecartMobSpawner(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public EntityMinecart.EnumMinecartType getMinecartType()
    {
        return EntityMinecart.EnumMinecartType.SPAWNER;
    }

    public IBlockState getDefaultDisplayTile()
    {
        return Blocks.mob_spawner.getDefaultState();
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        this.mobSpawnerLogic.readFromNBT(tagCompund);
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        this.mobSpawnerLogic.writeToNBT(tagCompound);
    }

    public void handleStatusUpdate(byte id)
    {
        this.mobSpawnerLogic.setDelayToMin(id);
    }

    public void onUpdate()
    {
        super.onUpdate();
        this.mobSpawnerLogic.updateSpawner();
    }

    public MobSpawnerBaseLogic getSpawnerLogic()
    {
        return this.mobSpawnerLogic;
    }
}
