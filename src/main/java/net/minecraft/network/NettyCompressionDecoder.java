package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class NettyCompressionDecoder extends ByteToMessageDecoder
{
    private final Inflater inflater;
    private int treshold;

    public NettyCompressionDecoder(int treshold)
    {
        this.treshold = treshold;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> output) throws DataFormatException, Exception
    {
        if (input.readableBytes() != 0)
        {
            PacketBuffer packetbuffer = new PacketBuffer(input);
            int i = packetbuffer.readVarIntFromBuffer();

            if (i == 0)
            {
                output.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            }
            else
            {
                if (i < this.treshold)
                {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.treshold);
                }

                if (i > 2097152)
                {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + 2097152);
                }

                byte[] abyte = new byte[packetbuffer.readableBytes()];
                packetbuffer.readBytes(abyte);
                this.inflater.setInput(abyte);
                byte[] abyte1 = new byte[i];
                this.inflater.inflate(abyte1);
                output.add(Unpooled.wrappedBuffer(abyte1));
                this.inflater.reset();
            }
        }
    }

    public void setCompressionTreshold(int treshold)
    {
        this.treshold = treshold;
    }
}
