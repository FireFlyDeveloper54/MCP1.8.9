package net.minecraft.network.play.server;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S3FPacketCustomPayload implements Packet<INetHandlerPlayClient>
{
    private String channel;
    private PacketBuffer data;

    public S3FPacketCustomPayload()
    {
    }

    public S3FPacketCustomPayload(String channelName, PacketBuffer dataIn)
    {
        this.channel = channelName;
        this.data = dataIn;

        if (dataIn.writerIndex() > 1048576)
        {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.channel = buf.readStringFromBuffer(20);
        int payloadLength = buf.readableBytes();

        if (payloadLength >= 0 && payloadLength <= 1048576)
        {
            this.data = new PacketBuffer(buf.readBytes(payloadLength));
        }
        else
        {
            throw new IOException("Payload may not be larger than 1048576 bytes");
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        try
        {
            buf.writeString(this.channel);

            if (this.data != null)
            {
                buf.writeBytes((ByteBuf)this.data);
            }
        }
        finally
        {
            this.releaseData();
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        boolean deferred = false;

        try
        {
            handler.handleCustomPayload(this);
        }
        catch (ThreadQuickExitException exception)
        {
            deferred = true;
            throw exception;
        }
        finally
        {
            if (!deferred && this.data != null)
            {
                this.releaseData();
            }
        }
    }

    private void releaseData()
    {
        if (this.data != null)
        {
            this.data.release();
            this.data = null;
        }
    }

    public String getChannelName()
    {
        return this.channel;
    }

    public PacketBuffer getBufferData()
    {
        return this.data;
    }
}
