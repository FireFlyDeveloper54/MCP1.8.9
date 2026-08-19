package net.minecraft.realms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsConnect
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen onlineScreen;
    private volatile boolean aborted = false;
    private NetworkManager connection;

    public RealmsConnect(RealmsScreen onlineScreen)
    {
        this.onlineScreen = onlineScreen;
    }

    public void connect(final String hostName, final int port)
    {
        Realms.setConnectedToRealms(true);
        (new Thread("Realms-connect-task")
        {
            public void run()
            {
                InetAddress inetaddress = null;

                try
                {
                    inetaddress = InetAddress.getByName(hostName);

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, Minecraft.getMinecraft().gameSettings.isUsingNativeTransport());

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection.setNetHandler(new NetHandlerLoginClient(RealmsConnect.this.connection, Minecraft.getMinecraft(), RealmsConnect.this.onlineScreen.getProxy()));

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection.sendPacket(new C00Handshake(47, hostName, port, EnumConnectionState.LOGIN));

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection.sendPacket(new C00PacketLoginStart(Minecraft.getMinecraft().getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception)
                {
                    Realms.clearResourcePack();

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.LOGGER.error((String)"Couldn\'t connect to world", (Throwable)unknownhostexception);
                    Minecraft.getMinecraft().getResourcePackRepository().clearResourcePack();
                    Realms.setScreen(new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host \'" + hostName + "\'"})));
                }
                catch (Exception exception)
                {
                    Realms.clearResourcePack();

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.LOGGER.error((String)"Couldn\'t connect to world", (Throwable)exception);
                    String disconnectReason = exception.toString();

                    if (inetaddress != null)
                    {
                        String addressText = inetaddress.toString() + ":" + port;
                        disconnectReason = disconnectReason.replace(addressText, "");
                    }

                    Realms.setScreen(new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {disconnectReason})));
                }
            }
        }).start();
    }

    public void abort()
    {
        this.aborted = true;
    }

    public void tick()
    {
        if (this.connection != null)
        {
            if (this.connection.isChannelOpen())
            {
                this.connection.processReceivedPackets();
            }
            else
            {
                this.connection.checkDisconnected();
            }
        }
    }
}
