package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptingEncoder extends MessageToByteEncoder<ByteBuf>
{
    private final NettyEncryptionTranslator encryptionCodec;

    public NettyEncryptingEncoder(Cipher cipher)
    {
        this.encryptionCodec = new NettyEncryptionTranslator(cipher);
    }

    protected void encode(ChannelHandlerContext ctx, ByteBuf input, ByteBuf output) throws ShortBufferException, Exception
    {
        this.encryptionCodec.cipher(input, output);
    }
}
