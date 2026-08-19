package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S08PacketPlayerPosLook implements Packet<INetHandlerPlayClient>
{
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private Set<S08PacketPlayerPosLook.EnumFlags> flags;

    public S08PacketPlayerPosLook()
    {
    }

    public S08PacketPlayerPosLook(double xIn, double yIn, double zIn, float yawIn, float pitchIn, Set<S08PacketPlayerPosLook.EnumFlags> flagsIn)
    {
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
        this.yaw = yawIn;
        this.pitch = pitchIn;
        this.flags = flagsIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.flags = S08PacketPlayerPosLook.EnumFlags.unpackFlags(buf.readUnsignedByte());
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(S08PacketPlayerPosLook.EnumFlags.packFlags(this.flags));
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handlePlayerPosLook(this);
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public float getYaw()
    {
        return this.yaw;
    }

    public float getPitch()
    {
        return this.pitch;
    }

    public Set<S08PacketPlayerPosLook.EnumFlags> getFlags()
    {
        return this.flags;
    }

    public static enum EnumFlags
    {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private static final S08PacketPlayerPosLook.EnumFlags[] VALUES = values();
        private int bitIndex;

        private EnumFlags(int bitIndexIn)
        {
            this.bitIndex = bitIndexIn;
        }

        private int getMask()
        {
            return 1 << this.bitIndex;
        }

        private boolean isSet(int mask)
        {
            return (mask & this.getMask()) == this.getMask();
        }

        public static Set<S08PacketPlayerPosLook.EnumFlags> unpackFlags(int mask)
        {
            Set<S08PacketPlayerPosLook.EnumFlags> flags = EnumSet.<S08PacketPlayerPosLook.EnumFlags>noneOf(S08PacketPlayerPosLook.EnumFlags.class);

            for (S08PacketPlayerPosLook.EnumFlags enumFlag : VALUES)
            {
                if (enumFlag.isSet(mask))
                {
                    flags.add(enumFlag);
                }
            }

            return flags;
        }

        public static int packFlags(Set<S08PacketPlayerPosLook.EnumFlags> flags)
        {
            int mask = 0;

            for (S08PacketPlayerPosLook.EnumFlags enumFlag : flags)
            {
                mask |= enumFlag.getMask();
            }

            return mask;
        }
    }
}
