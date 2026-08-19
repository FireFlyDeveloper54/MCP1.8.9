package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class S27PacketExplosion implements Packet<INetHandlerPlayClient>
{
    private double posX;
    private double posY;
    private double posZ;
    private float strength;
    private List<BlockPos> affectedBlockPositions;
    private float motionX;
    private float motionY;
    private float motionZ;

    public S27PacketExplosion()
    {
    }

    public S27PacketExplosion(double x, double y, double z, float strengthIn, List<BlockPos> affectedBlocksIn, Vec3 motion)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.strength = strengthIn;
        this.affectedBlockPositions = Lists.newArrayList(affectedBlocksIn);

        if (motion != null)
        {
            this.motionX = (float)motion.xCoord;
            this.motionY = (float)motion.yCoord;
            this.motionZ = (float)motion.zCoord;
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.posX = (double)buf.readFloat();
        this.posY = (double)buf.readFloat();
        this.posZ = (double)buf.readFloat();
        this.strength = buf.readFloat();
        int affectedBlockCount = buf.readInt();
        this.affectedBlockPositions = Lists.<BlockPos>newArrayListWithCapacity(affectedBlockCount);
        int baseX = (int)this.posX;
        int baseY = (int)this.posY;
        int baseZ = (int)this.posZ;

        for (int blockIndex = 0; blockIndex < affectedBlockCount; ++blockIndex)
        {
            int blockX = buf.readByte() + baseX;
            int blockY = buf.readByte() + baseY;
            int blockZ = buf.readByte() + baseZ;
            this.affectedBlockPositions.add(new BlockPos(blockX, blockY, blockZ));
        }

        this.motionX = buf.readFloat();
        this.motionY = buf.readFloat();
        this.motionZ = buf.readFloat();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeFloat((float)this.posX);
        buf.writeFloat((float)this.posY);
        buf.writeFloat((float)this.posZ);
        buf.writeFloat(this.strength);
        buf.writeInt(this.affectedBlockPositions.size());
        int baseX = (int)this.posX;
        int baseY = (int)this.posY;
        int baseZ = (int)this.posZ;

        for (BlockPos affectedBlockPos : this.affectedBlockPositions)
        {
            int offsetX = affectedBlockPos.getX() - baseX;
            int offsetY = affectedBlockPos.getY() - baseY;
            int offsetZ = affectedBlockPos.getZ() - baseZ;
            buf.writeByte(offsetX);
            buf.writeByte(offsetY);
            buf.writeByte(offsetZ);
        }

        buf.writeFloat(this.motionX);
        buf.writeFloat(this.motionY);
        buf.writeFloat(this.motionZ);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleExplosion(this);
    }

    public float getMotionX()
    {
        return this.motionX;
    }

    public float getMotionY()
    {
        return this.motionY;
    }

    public float getMotionZ()
    {
        return this.motionZ;
    }

    public double getX()
    {
        return this.posX;
    }

    public double getY()
    {
        return this.posY;
    }

    public double getZ()
    {
        return this.posZ;
    }

    public float getStrength()
    {
        return this.strength;
    }

    public List<BlockPos> getAffectedBlockPositions()
    {
        return this.affectedBlockPositions;
    }
}
