package net.minecraft.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PingResponseHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger logger = LogManager.getLogger();
    private NetworkSystem networkSystem;

    public PingResponseHandler(NetworkSystem networkSystemIn)
    {
        this.networkSystem = networkSystemIn;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ByteBuf bytebuf = (ByteBuf)msg;
        bytebuf.markReaderIndex();
        boolean flag = true;

        try
        {
            if (bytebuf.readUnsignedByte() == 254)
            {
                InetSocketAddress inetsocketaddress = (InetSocketAddress)ctx.channel().remoteAddress();
                MinecraftServer minecraftserver = this.networkSystem.getServer();
                int i = bytebuf.readableBytes();

                switch (i)
                {
                    case 0:
                        logger.debug("Ping: (<1.3.x) from {}:{}", new Object[] {inetsocketaddress.getAddress(), Integer.valueOf(inetsocketaddress.getPort())});
                        String legacyResponse = String.format("%s\u00a7%d\u00a7%d", new Object[] {minecraftserver.getMOTD(), Integer.valueOf(minecraftserver.getCurrentPlayerCount()), Integer.valueOf(minecraftserver.getMaxPlayers())});
                        this.writeAndFlush(ctx, this.getStringBuffer(legacyResponse));
                        break;

                    case 1:
                        if (bytebuf.readUnsignedByte() != 1)
                        {
                            return;
                        }

                        logger.debug("Ping: (1.4-1.5.x) from {}:{}", new Object[] {inetsocketaddress.getAddress(), Integer.valueOf(inetsocketaddress.getPort())});
                        String s = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", new Object[] {Integer.valueOf(127), minecraftserver.getMinecraftVersion(), minecraftserver.getMOTD(), Integer.valueOf(minecraftserver.getCurrentPlayerCount()), Integer.valueOf(minecraftserver.getMaxPlayers())});
                        this.writeAndFlush(ctx, this.getStringBuffer(s));
                        break;

                    default:
                        boolean isValidPingHostRequest = bytebuf.readUnsignedByte() == 1;
                        isValidPingHostRequest = isValidPingHostRequest & bytebuf.readUnsignedByte() == 250;
                        int channelByteLength = bytebuf.readUnsignedShort() * 2;
                        isValidPingHostRequest = isValidPingHostRequest & "MC|PingHost".equals(bytebuf.readSlice(channelByteLength).toString(Charsets.UTF_16BE));
                        int j = bytebuf.readUnsignedShort();
                        isValidPingHostRequest = isValidPingHostRequest & bytebuf.readUnsignedByte() >= 73;
                        int hostByteLength = bytebuf.readUnsignedShort() * 2;
                        bytebuf.skipBytes(hostByteLength);
                        isValidPingHostRequest = isValidPingHostRequest & 3 + hostByteLength + 4 == j;
                        int port = bytebuf.readInt();
                        isValidPingHostRequest = isValidPingHostRequest & (port >= 0 && port <= 65535);
                        isValidPingHostRequest = isValidPingHostRequest & bytebuf.readableBytes() == 0;

                        if (!isValidPingHostRequest)
                        {
                            return;
                        }

                        logger.debug("Ping: (1.6) from {}:{}", new Object[] {inetsocketaddress.getAddress(), Integer.valueOf(inetsocketaddress.getPort())});
                        String ping16Response = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", new Object[] {Integer.valueOf(127), minecraftserver.getMinecraftVersion(), minecraftserver.getMOTD(), Integer.valueOf(minecraftserver.getCurrentPlayerCount()), Integer.valueOf(minecraftserver.getMaxPlayers())});
                        ByteBuf bytebuf1 = this.getStringBuffer(ping16Response);

                        try
                        {
                            this.writeAndFlush(ctx, bytebuf1);
                        }
                        finally
                        {
                            bytebuf1.release();
                        }
                }

                bytebuf.release();
                flag = false;
                return;
            }
        }
        catch (RuntimeException caughtRuntimeException)
        {
            return;
        }
        finally
        {
            if (flag)
            {
                bytebuf.resetReaderIndex();
                ctx.channel().pipeline().remove("legacy_query");
                ctx.fireChannelRead(msg);
            }
        }
    }

    private void writeAndFlush(ChannelHandlerContext ctx, ByteBuf data)
    {
        ctx.pipeline().firstContext().writeAndFlush(data).addListener(ChannelFutureListener.CLOSE);
    }

    private ByteBuf getStringBuffer(String string)
    {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(255);
        char[] achar = string.toCharArray();
        byteBuf.writeShort(achar.length);

        for (char character : achar)
        {
            byteBuf.writeChar(character);
        }

        return byteBuf;
    }
}
