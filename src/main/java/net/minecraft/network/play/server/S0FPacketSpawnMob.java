package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;

public class S0FPacketSpawnMob implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private int type;
    private int x;
    private int y;
    private int z;
    private int velocityX;
    private int velocityY;
    private int velocityZ;
    private byte yaw;
    private byte pitch;
    private byte headPitch;
    private DataWatcher dataWatcher;
    private List<DataWatcher.WatchableObject> watcher;

    public S0FPacketSpawnMob()
    {
    }

    public S0FPacketSpawnMob(EntityLivingBase entityIn)
    {
        this.entityId = entityIn.getEntityId();
        this.type = (byte)EntityList.getEntityID(entityIn);
        this.x = MathHelper.floor_double(entityIn.posX * 32.0D);
        this.y = MathHelper.floor_double(entityIn.posY * 32.0D);
        this.z = MathHelper.floor_double(entityIn.posZ * 32.0D);
        this.yaw = (byte)((int)(entityIn.rotationYaw * 256.0F / 360.0F));
        this.pitch = (byte)((int)(entityIn.rotationPitch * 256.0F / 360.0F));
        this.headPitch = (byte)((int)(entityIn.rotationYawHead * 256.0F / 360.0F));
        double velocityLimit = 3.9D;
        double motionX = entityIn.motionX;
        double motionY = entityIn.motionY;
        double motionZ = entityIn.motionZ;

        if (motionX < -velocityLimit)
        {
            motionX = -velocityLimit;
        }

        if (motionY < -velocityLimit)
        {
            motionY = -velocityLimit;
        }

        if (motionZ < -velocityLimit)
        {
            motionZ = -velocityLimit;
        }

        if (motionX > velocityLimit)
        {
            motionX = velocityLimit;
        }

        if (motionY > velocityLimit)
        {
            motionY = velocityLimit;
        }

        if (motionZ > velocityLimit)
        {
            motionZ = velocityLimit;
        }

        this.velocityX = (int)(motionX * 8000.0D);
        this.velocityY = (int)(motionY * 8000.0D);
        this.velocityZ = (int)(motionZ * 8000.0D);
        this.dataWatcher = entityIn.getDataWatcher();
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readVarIntFromBuffer();
        this.type = buf.readByte() & 255;
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.yaw = buf.readByte();
        this.pitch = buf.readByte();
        this.headPitch = buf.readByte();
        this.velocityX = buf.readShort();
        this.velocityY = buf.readShort();
        this.velocityZ = buf.readShort();
        this.watcher = DataWatcher.readWatchedListFromPacketBuffer(buf);
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.entityId);
        buf.writeByte(this.type & 255);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeByte(this.yaw);
        buf.writeByte(this.pitch);
        buf.writeByte(this.headPitch);
        buf.writeShort(this.velocityX);
        buf.writeShort(this.velocityY);
        buf.writeShort(this.velocityZ);
        this.dataWatcher.writeTo(buf);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnMob(this);
    }

    public List<DataWatcher.WatchableObject> getWatcherList()
    {
        if (this.watcher == null)
        {
            this.watcher = this.dataWatcher.getAllWatched();
        }

        return this.watcher;
    }

    public int getEntityID()
    {
        return this.entityId;
    }

    public int getEntityType()
    {
        return this.type;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    public int getVelocityX()
    {
        return this.velocityX;
    }

    public int getVelocityY()
    {
        return this.velocityY;
    }

    public int getVelocityZ()
    {
        return this.velocityZ;
    }

    public byte getYaw()
    {
        return this.yaw;
    }

    public byte getPitch()
    {
        return this.pitch;
    }

    public byte getHeadPitch()
    {
        return this.headPitch;
    }
}
