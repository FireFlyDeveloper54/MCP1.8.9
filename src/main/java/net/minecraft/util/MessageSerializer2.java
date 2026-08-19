package net.minecraft.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketBuffer;

public class MessageSerializer2 extends MessageToByteEncoder<ByteBuf>
{
    protected void encode(ChannelHandlerContext context, ByteBuf in, ByteBuf out) throws Exception
    {
        int readableBytes = in.readableBytes();
        int varIntSize = PacketBuffer.getVarIntSize(readableBytes);

        if (varIntSize > 3)
        {
            throw new IllegalArgumentException("unable to fit " + readableBytes + " into " + 3);
        }
        else
        {
            PacketBuffer packetbuffer = new PacketBuffer(out);
            packetbuffer.ensureWritable(varIntSize + readableBytes);
            packetbuffer.writeVarIntToBuffer(readableBytes);
            packetbuffer.writeBytes(in, in.readerIndex(), readableBytes);
        }
    }
}
