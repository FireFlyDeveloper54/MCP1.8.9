package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptingDecoder extends MessageToMessageDecoder<ByteBuf>
{
    private final NettyEncryptionTranslator decryptionCodec;

    public NettyEncryptingDecoder(Cipher cipher)
    {
        this.decryptionCodec = new NettyEncryptionTranslator(cipher);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> output) throws ShortBufferException, Exception
    {
        output.add(this.decryptionCodec.decipher(ctx, input));
    }
}
