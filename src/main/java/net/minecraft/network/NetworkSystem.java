package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import net.minecraft.client.network.NetHandlerHandshakeMemory;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkSystem
{
    private static final Logger logger = LogManager.getLogger();
    public static final LazyLoadBase<NioEventLoopGroup> eventLoops = new LazyLoadBase<NioEventLoopGroup>()
    {
        protected NioEventLoopGroup load()
        {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyLoadBase<EpollEventLoopGroup> SERVER_EPOLL_EVENTLOOP = new LazyLoadBase<EpollEventLoopGroup>()
    {
        protected EpollEventLoopGroup load()
        {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
        }
    };
    public static final LazyLoadBase<LocalEventLoopGroup> SERVER_LOCAL_EVENTLOOP = new LazyLoadBase<LocalEventLoopGroup>()
    {
        protected LocalEventLoopGroup load()
        {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
        }
    };
    private final MinecraftServer mcServer;
    public volatile boolean isAlive;
    private final List<ChannelFuture> endpoints = Collections.<ChannelFuture>synchronizedList(Lists.<ChannelFuture>newArrayList());
    private final List<NetworkManager> networkManagers = Collections.<NetworkManager>synchronizedList(Lists.<NetworkManager>newArrayList());

    public NetworkSystem(MinecraftServer server)
    {
        this.mcServer = server;
        this.isAlive = true;
    }

    public void addLanEndpoint(InetAddress address, int port) throws IOException
    {
        synchronized (this.endpoints)
        {
            Class <? extends ServerSocketChannel > oclass;
            LazyLoadBase <? extends EventLoopGroup > lazyloadbase;

            if (Epoll.isAvailable() && this.mcServer.shouldUseNativeTransport())
            {
                oclass = EpollServerSocketChannel.class;
                lazyloadbase = SERVER_EPOLL_EVENTLOOP;
                logger.info("Using epoll channel type");
            }
            else
            {
                oclass = NioServerSocketChannel.class;
                lazyloadbase = eventLoops;
                logger.info("Using default channel type");
            }

            this.endpoints.add(((ServerBootstrap)((ServerBootstrap)(new ServerBootstrap()).channel(oclass)).childHandler(new ChannelInitializer<Channel>()
            {
                protected void initChannel(Channel channel) throws Exception
                {
                    try
                    {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
                    }
                    catch (ChannelException caughtChannelException)
                    {
                        ;
                    }

                    channel.pipeline().addLast((String)"timeout", (ChannelHandler)(new ReadTimeoutHandler(30))).addLast((String)"legacy_query", (ChannelHandler)(new PingResponseHandler(NetworkSystem.this))).addLast((String)"splitter", (ChannelHandler)(new MessageDeserializer2())).addLast((String)"decoder", (ChannelHandler)(new MessageDeserializer(EnumPacketDirection.SERVERBOUND))).addLast((String)"prepender", (ChannelHandler)(new MessageSerializer2())).addLast((String)"encoder", (ChannelHandler)(new MessageSerializer(EnumPacketDirection.CLIENTBOUND)));
                    NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    NetworkSystem.this.networkManagers.add(networkmanager);
                    channel.pipeline().addLast((String)"packet_handler", (ChannelHandler)networkmanager);
                    networkmanager.setNetHandler(new NetHandlerHandshakeTCP(NetworkSystem.this.mcServer, networkmanager));
                }
            }).group((EventLoopGroup)lazyloadbase.getValue()).localAddress(address, port)).bind().syncUninterruptibly());
        }
    }

    public SocketAddress addLocalEndpoint()
    {
        ChannelFuture channelFuture;

        synchronized (this.endpoints)
        {
            channelFuture = ((ServerBootstrap)((ServerBootstrap)(new ServerBootstrap()).channel(LocalServerChannel.class)).childHandler(new ChannelInitializer<Channel>()
            {
                protected void initChannel(Channel channel) throws Exception
                {
                    NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    networkmanager.setNetHandler(new NetHandlerHandshakeMemory(NetworkSystem.this.mcServer, networkmanager));
                    NetworkSystem.this.networkManagers.add(networkmanager);
                    channel.pipeline().addLast((String)"packet_handler", (ChannelHandler)networkmanager);
                }
            }).group((EventLoopGroup)eventLoops.getValue()).localAddress(LocalAddress.ANY)).bind().syncUninterruptibly();
            this.endpoints.add(channelFuture);
        }

        return channelFuture.channel().localAddress();
    }

    public void terminateEndpoints()
    {
        this.isAlive = false;

        for (ChannelFuture channelFuture : this.endpoints)
        {
            try
            {
                channelFuture.channel().close().sync();
            }
            catch (InterruptedException caughtInterruptedException)
            {
                logger.error("Interrupted whilst closing channel");
            }
        }
    }

    public void networkTick()
    {
        synchronized (this.networkManagers)
        {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext())
            {
                final NetworkManager networkManager = (NetworkManager)iterator.next();

                if (!networkManager.hasNoChannel())
                {
                    if (!networkManager.isChannelOpen())
                    {
                        iterator.remove();
                        networkManager.checkDisconnected();
                    }
                    else
                    {
                        try
                        {
                            networkManager.processReceivedPackets();
                        }
                        catch (Exception exception)
                        {
                            if (networkManager.isLocalChannel())
                            {
                                CrashReport crashReport = CrashReport.makeCrashReport(exception, "Ticking memory connection");
                                CrashReportCategory crashReportCategory = crashReport.makeCategory("Ticking connection");
                                crashReportCategory.addCrashSectionCallable("Connection", new Callable<String>()
                                {
                                    public String call() throws Exception
                                    {
                                        return networkManager.toString();
                                    }
                                });
                                throw new ReportedException(crashReport);
                            }

                            logger.warn((String)("Failed to handle packet for " + networkManager.getRemoteAddress()), (Throwable)exception);
                            final ChatComponentText chatComponentText = new ChatComponentText("Internal server error");
                            networkManager.sendPacket(new S40PacketDisconnect(chatComponentText), new GenericFutureListener < Future <? super Void >> ()
                            {
                                public void operationComplete(Future <? super Void > future) throws Exception
                                {
                                    networkManager.closeChannel(chatComponentText);
                                }
                            }, new GenericFutureListener[0]);
                            networkManager.disableAutoRead();
                        }
                    }
                }
            }
        }
    }

    public MinecraftServer getServer()
    {
        return this.mcServer;
    }
}
