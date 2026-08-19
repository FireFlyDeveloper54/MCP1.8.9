package net.minecraft.client.network;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OldServerPinger
{
    private static final Splitter PING_RESPONSE_SPLITTER = Splitter.on('\u0000').limit(6);
    private static final Logger logger = LogManager.getLogger();
    private final List<NetworkManager> pingDestinations = Collections.<NetworkManager>synchronizedList(Lists.<NetworkManager>newArrayList());

    public void ping(final ServerData server) throws UnknownHostException
    {
        ServerAddress serveraddress = ServerAddress.fromString(server.serverIP);
        final NetworkManager networkmanager = NetworkManager.createNetworkManagerAndConnect(InetAddress.getByName(serveraddress.getIP()), serveraddress.getPort(), false);
        this.pingDestinations.add(networkmanager);
        server.serverMOTD = "Pinging...";
        server.pingToServer = -1L;
        server.playerList = null;
        networkmanager.setNetHandler(new INetHandlerStatusClient()
        {
            private boolean pingSent = false;
            private boolean receivedStatus = false;
            private long pingSentAt = 0L;
            public void handleServerInfo(S00PacketServerInfo packetIn)
            {
                if (this.receivedStatus)
                {
                    networkmanager.closeChannel(new ChatComponentText("Received unrequested status"));
                }
                else
                {
                    this.receivedStatus = true;
                    ServerStatusResponse serverStatusResponse = packetIn.getResponse();

                    if (serverStatusResponse.getServerDescription() != null)
                    {
                        server.serverMOTD = serverStatusResponse.getServerDescription().getFormattedText();
                    }
                    else
                    {
                        server.serverMOTD = "";
                    }

                    if (serverStatusResponse.getProtocolVersionInfo() != null)
                    {
                        server.gameVersion = serverStatusResponse.getProtocolVersionInfo().getName();
                        server.version = serverStatusResponse.getProtocolVersionInfo().getProtocol();
                    }
                    else
                    {
                        server.gameVersion = "Old";
                        server.version = 0;
                    }

                    if (serverStatusResponse.getPlayerCountData() != null)
                    {
                        server.populationInfo = EnumChatFormatting.GRAY + "" + serverStatusResponse.getPlayerCountData().getOnlinePlayerCount() + "" + EnumChatFormatting.DARK_GRAY + "/" + EnumChatFormatting.GRAY + serverStatusResponse.getPlayerCountData().getMaxPlayers();

                        if (ArrayUtils.isNotEmpty(serverStatusResponse.getPlayerCountData().getPlayers()))
                        {
                            StringBuilder stringBuilder = new StringBuilder();

                            for (GameProfile gameProfile : serverStatusResponse.getPlayerCountData().getPlayers())
                            {
                                if (stringBuilder.length() > 0)
                                {
                                    stringBuilder.append("\n");
                                }

                                stringBuilder.append(gameProfile.getName());
                            }

                            if (serverStatusResponse.getPlayerCountData().getPlayers().length < serverStatusResponse.getPlayerCountData().getOnlinePlayerCount())
                            {
                                if (stringBuilder.length() > 0)
                                {
                                    stringBuilder.append("\n");
                                }

                                stringBuilder.append("... and ").append(serverStatusResponse.getPlayerCountData().getOnlinePlayerCount() - serverStatusResponse.getPlayerCountData().getPlayers().length).append(" more ...");
                            }

                            server.playerList = stringBuilder.toString();
                        }
                    }
                    else
                    {
                        server.populationInfo = EnumChatFormatting.DARK_GRAY + "???";
                    }

                    if (serverStatusResponse.getFavicon() != null)
                    {
                        String s = serverStatusResponse.getFavicon();

                        if (s.startsWith("data:image/png;base64,"))
                        {
                            server.setBase64EncodedIconData(s.substring("data:image/png;base64,".length()));
                        }
                        else
                        {
                            OldServerPinger.logger.error("Invalid server icon (unknown format)");
                        }
                    }
                    else
                    {
                        server.setBase64EncodedIconData((String)null);
                    }

                    this.pingSentAt = Minecraft.getSystemTime();
                    networkmanager.sendPacket(new C01PacketPing(this.pingSentAt));
                    this.pingSent = true;
                }
            }
            public void handlePong(S01PacketPong packetIn)
            {
                long i = this.pingSentAt;
                long j = Minecraft.getSystemTime();
                server.pingToServer = j - i;
                networkmanager.closeChannel(new ChatComponentText("Finished"));
            }
            public void onDisconnect(IChatComponent reason)
            {
                if (!this.pingSent)
                {
                    OldServerPinger.logger.error("Can\'t ping " + server.serverIP + ": " + reason.getUnformattedText());
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can\'t connect to server.";
                    server.populationInfo = "";
                    OldServerPinger.this.tryCompatibilityPing(server);
                }
            }
        });

        try
        {
            networkmanager.sendPacket(new C00Handshake(47, serveraddress.getIP(), serveraddress.getPort(), EnumConnectionState.STATUS));
            networkmanager.sendPacket(new C00PacketServerQuery());
        }
        catch (Throwable throwable)
        {
            logger.error((Object)throwable);
        }
    }

    private void tryCompatibilityPing(final ServerData server)
    {
        final ServerAddress serverAddress = ServerAddress.fromString(server.serverIP);
        ((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group((EventLoopGroup)NetworkManager.CLIENT_NIO_EVENTLOOP.getValue())).handler(new ChannelInitializer<Channel>()
        {
            protected void initChannel(Channel channel) throws Exception
            {
                try
                {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
                }
                catch (ChannelException ignored)
                {
                    ;
                }

                channel.pipeline().addLast(new ChannelHandler[] {new SimpleChannelInboundHandler<ByteBuf>()
                    {
                        public void channelActive(ChannelHandlerContext context) throws Exception
                        {
                            super.channelActive(context);
                            ByteBuf byteBuf = Unpooled.buffer();

                            try
                            {
                                byteBuf.writeByte(254);
                                byteBuf.writeByte(1);
                                byteBuf.writeByte(250);
                                char[] achar = "MC|PingHost".toCharArray();
                                byteBuf.writeShort(achar.length);

                                for (char character : achar)
                                {
                                    byteBuf.writeChar(character);
                                }

                                byteBuf.writeShort(7 + 2 * serverAddress.getIP().length());
                                byteBuf.writeByte(127);
                                achar = serverAddress.getIP().toCharArray();
                                byteBuf.writeShort(achar.length);

                                for (char secondCharacter : achar)
                                {
                                    byteBuf.writeChar(secondCharacter);
                                }

                                byteBuf.writeInt(serverAddress.getPort());
                                context.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                            }
                            finally
                            {
                                byteBuf.release();
                            }
                        }
                        protected void channelRead0(ChannelHandlerContext context, ByteBuf buffer) throws Exception
                        {
                            short short1 = buffer.readUnsignedByte();

                            if (short1 == 255)
                            {
                                int responseByteLength = buffer.readUnsignedShort() * 2;
                                String s = buffer.readSlice(responseByteLength).toString(Charsets.UTF_16BE);
                                String[] astring = (String[])Iterables.toArray(OldServerPinger.PING_RESPONSE_SPLITTER.split(s), String.class);

                                if ("\u00a71".equals(astring[0]))
                                {
                                    int i = MathHelper.parseIntWithDefault(astring[1], 0);
                                    String gameVersion = astring[2];
                                    String serverMotd = astring[3];
                                    int j = MathHelper.parseIntWithDefault(astring[4], -1);
                                    int k = MathHelper.parseIntWithDefault(astring[5], -1);
                                    server.version = -1;
                                    server.gameVersion = gameVersion;
                                    server.serverMOTD = serverMotd;
                                    server.populationInfo = EnumChatFormatting.GRAY + "" + j + "" + EnumChatFormatting.DARK_GRAY + "/" + EnumChatFormatting.GRAY + k;
                                }
                            }

                            context.close();
                        }
                        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception
                        {
                            context.close();
                        }
                    }
                });
            }
        })).channel(NioSocketChannel.class)).connect(serverAddress.getIP(), serverAddress.getPort());
    }

    public void pingPendingNetworks()
    {
        synchronized (this.pingDestinations)
        {
            Iterator<NetworkManager> iterator = this.pingDestinations.iterator();

            while (iterator.hasNext())
            {
                NetworkManager networkManager = (NetworkManager)iterator.next();

                if (networkManager.isChannelOpen())
                {
                    networkManager.processReceivedPackets();
                }
                else
                {
                    iterator.remove();
                    networkManager.checkDisconnected();
                }
            }
        }
    }

    public void clearPendingNetworks()
    {
        synchronized (this.pingDestinations)
        {
            Iterator<NetworkManager> iterator = this.pingDestinations.iterator();

            while (iterator.hasNext())
            {
                NetworkManager networkManager = (NetworkManager)iterator.next();

                if (networkManager.isChannelOpen())
                {
                    iterator.remove();
                    networkManager.closeChannel(new ChatComponentText("Cancelled"));
                }
            }
        }
    }
}
