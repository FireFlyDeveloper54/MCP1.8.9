package net.minecraft.tileentity;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityCommandBlock extends TileEntity
{
    private final CommandBlockLogic commandBlockLogic = new CommandBlockLogic()
    {
        public BlockPos getPosition()
        {
            return TileEntityCommandBlock.this.pos;
        }
        public Vec3 getPositionVector()
        {
            return new Vec3((double)TileEntityCommandBlock.this.pos.getX() + 0.5D, (double)TileEntityCommandBlock.this.pos.getY() + 0.5D, (double)TileEntityCommandBlock.this.pos.getZ() + 0.5D);
        }
        public World getEntityWorld()
        {
            return TileEntityCommandBlock.this.getWorld();
        }
        public void setCommand(String command)
        {
            super.setCommand(command);
            TileEntityCommandBlock.this.markDirty();
        }
        public void updateCommand()
        {
            TileEntityCommandBlock.this.getWorld().markBlockForUpdate(TileEntityCommandBlock.this.pos);
        }
        public int getCommandBlockType()
        {
            return 0;
        }
        public void writeToPacketBuffer(ByteBuf buffer)
        {
            buffer.writeInt(TileEntityCommandBlock.this.pos.getX());
            buffer.writeInt(TileEntityCommandBlock.this.pos.getY());
            buffer.writeInt(TileEntityCommandBlock.this.pos.getZ());
        }
        public Entity getCommandSenderEntity()
        {
            return null;
        }
    };

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        this.commandBlockLogic.writeDataToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.commandBlockLogic.readDataFromNBT(compound);
    }

    public Packet getDescriptionPacket()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        this.writeToNBT(nBTTagCompound);
        return new S35PacketUpdateTileEntity(this.pos, 2, nBTTagCompound);
    }

    public boolean onlyOpsCanSetNbt()
    {
        return true;
    }

    public CommandBlockLogic getCommandBlockLogic()
    {
        return this.commandBlockLogic;
    }

    public CommandResultStats getCommandResultStats()
    {
        return this.commandBlockLogic.getCommandResultStats();
    }
}
