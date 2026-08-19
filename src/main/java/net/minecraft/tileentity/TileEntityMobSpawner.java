package net.minecraft.tileentity;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

public class TileEntityMobSpawner extends TileEntity implements ITickable
{
    private final MobSpawnerBaseLogic spawnerLogic = new MobSpawnerBaseLogic()
    {
        public void broadcastEvent(int id)
        {
            TileEntityMobSpawner.this.worldObj.addBlockEvent(TileEntityMobSpawner.this.pos, Blocks.mob_spawner, id, 0);
        }
        public World getSpawnerWorld()
        {
            return TileEntityMobSpawner.this.worldObj;
        }
        public BlockPos getSpawnerPosition()
        {
            return TileEntityMobSpawner.this.pos;
        }
        public void setRandomEntity(MobSpawnerBaseLogic.WeightedRandomMinecart randomEntity)
        {
            super.setRandomEntity(randomEntity);

            if (this.getSpawnerWorld() != null)
            {
                this.getSpawnerWorld().markBlockForUpdate(TileEntityMobSpawner.this.pos);
            }
        }
    };

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.spawnerLogic.readFromNBT(compound);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        this.spawnerLogic.writeToNBT(compound);
    }

    public void update()
    {
        this.spawnerLogic.updateSpawner();
    }

    public Packet getDescriptionPacket()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        this.writeToNBT(nBTTagCompound);
        nBTTagCompound.removeTag("SpawnPotentials");
        return new S35PacketUpdateTileEntity(this.pos, 1, nBTTagCompound);
    }

    public boolean receiveClientEvent(int id, int type)
    {
        return this.spawnerLogic.setDelayToMin(id) ? true : super.receiveClientEvent(id, type);
    }

    public boolean onlyOpsCanSetNbt()
    {
        return true;
    }

    public MobSpawnerBaseLogic getSpawnerBaseLogic()
    {
        return this.spawnerLogic;
    }
}
