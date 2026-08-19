package net.minecraft.realms;

import net.minecraft.client.multiplayer.ServerAddress;

public class RealmsServerAddress
{
    private final String host;
    private final int port;

    protected RealmsServerAddress(String hostIn, int portIn)
    {
        this.host = hostIn;
        this.port = portIn;
    }

    public String getHost()
    {
        return this.host;
    }

    public int getPort()
    {
        return this.port;
    }

    public static RealmsServerAddress parseString(String address)
    {
        ServerAddress serveraddress = ServerAddress.fromString(address);
        return new RealmsServerAddress(serveraddress.getIP(), serveraddress.getPort());
    }
}
