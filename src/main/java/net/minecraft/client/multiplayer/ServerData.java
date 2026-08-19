package net.minecraft.client.multiplayer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class ServerData
{
    public String serverName;
    public String serverIP;
    public String populationInfo;
    public String serverMOTD;
    public long pingToServer;
    public int version = 47;
    public String gameVersion = "1.8.9";
    public boolean hasPinged;
    public String playerList;
    private ServerData.ServerResourceMode resourceMode = ServerData.ServerResourceMode.PROMPT;
    private String serverIcon;
    private boolean lanServer;

    public ServerData(String name, String ip, boolean isLan)
    {
        this.serverName = name;
        this.serverIP = ip;
        this.lanServer = isLan;
    }

    public NBTTagCompound getNBTCompound()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        nBTTagCompound.setString("name", this.serverName);
        nBTTagCompound.setString("ip", this.serverIP);

        if (this.serverIcon != null)
        {
            nBTTagCompound.setString("icon", this.serverIcon);
        }

        if (this.resourceMode == ServerData.ServerResourceMode.ENABLED)
        {
            nBTTagCompound.setBoolean("acceptTextures", true);
        }
        else if (this.resourceMode == ServerData.ServerResourceMode.DISABLED)
        {
            nBTTagCompound.setBoolean("acceptTextures", false);
        }

        return nBTTagCompound;
    }

    public ServerData.ServerResourceMode getResourceMode()
    {
        return this.resourceMode;
    }

    public void setResourceMode(ServerData.ServerResourceMode mode)
    {
        this.resourceMode = mode;
    }

    public static ServerData getServerDataFromNBTCompound(NBTTagCompound nbtCompound)
    {
        ServerData serverData = new ServerData(nbtCompound.getString("name"), nbtCompound.getString("ip"), false);

        if (nbtCompound.hasKey("icon", 8))
        {
            serverData.setBase64EncodedIconData(nbtCompound.getString("icon"));
        }

        if (nbtCompound.hasKey("acceptTextures", 1))
        {
            if (nbtCompound.getBoolean("acceptTextures"))
            {
                serverData.setResourceMode(ServerData.ServerResourceMode.ENABLED);
            }
            else
            {
                serverData.setResourceMode(ServerData.ServerResourceMode.DISABLED);
            }
        }
        else
        {
            serverData.setResourceMode(ServerData.ServerResourceMode.PROMPT);
        }

        return serverData;
    }

    public String getBase64EncodedIconData()
    {
        return this.serverIcon;
    }

    public void setBase64EncodedIconData(String icon)
    {
        this.serverIcon = icon;
    }

    public boolean isOnLAN()
    {
        return this.lanServer;
    }

    public void copyFrom(ServerData serverDataIn)
    {
        this.serverIP = serverDataIn.serverIP;
        this.serverName = serverDataIn.serverName;
        this.setResourceMode(serverDataIn.getResourceMode());
        this.serverIcon = serverDataIn.serverIcon;
        this.lanServer = serverDataIn.lanServer;
    }

    public static enum ServerResourceMode
    {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        public static final ServerResourceMode[] VALUES = values();
        private final IChatComponent motd;

        private ServerResourceMode(String name)
        {
            this.motd = new ChatComponentTranslation("addServer.resourcePack." + name, new Object[0]);
        }

        public IChatComponent getMotd()
        {
            return this.motd;
        }
    }
}
