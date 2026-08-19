package net.minecraft.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import net.minecraft.network.PacketBuffer;

public class MessageDeserializer2 extends ByteToMessageDecoder
{
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception
    {
        in.markReaderIndex();
        byte[] lengthBytes = new byte[3];

        for (int byteIndex = 0; byteIndex < lengthBytes.length; ++byteIndex)
        {
            if (!in.isReadable())
            {
                in.resetReaderIndex();
                return;
            }

            lengthBytes[byteIndex] = in.readByte();

            if (lengthBytes[byteIndex] >= 0)
            {
                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(lengthBytes));

                try
                {
                    int frameLength = packetbuffer.readVarIntFromBuffer();

                    if (in.readableBytes() >= frameLength)
                    {
                        out.add(in.readBytes(frameLength));
                        return;
                    }

                    in.resetReaderIndex();
                }
                finally
                {
                    packetbuffer.release();
                }

                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}
