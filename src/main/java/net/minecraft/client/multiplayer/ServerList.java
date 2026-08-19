package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerList
{
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<ServerData> servers = Lists.<ServerData>newArrayList();

    public ServerList(Minecraft mcIn)
    {
        this.mc = mcIn;
        this.loadServerList();
    }

    public void loadServerList()
    {
        try
        {
            this.servers.clear();
            NBTTagCompound nBTTagCompound = CompressedStreamTools.read(new File(this.mc.mcDataDir, "servers.dat"));

            if (nBTTagCompound == null)
            {
                return;
            }

            NBTTagList nbttaglist = nBTTagCompound.getTagList("servers", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                this.servers.add(ServerData.getServerDataFromNBTCompound(nbttaglist.getCompoundTagAt(i)));
            }
        }
        catch (Exception exception)
        {
            logger.error((String)"Couldn\'t load server list", (Throwable)exception);
        }
    }

    public void saveServerList()
    {
        try
        {
            NBTTagList nBTTagList = new NBTTagList();

            for (ServerData serverData : this.servers)
            {
                nBTTagList.appendTag(serverData.getNBTCompound());
            }

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("servers", nBTTagList);
            CompressedStreamTools.safeWrite(nbttagcompound, new File(this.mc.mcDataDir, "servers.dat"));
        }
        catch (Exception exception)
        {
            logger.error((String)"Couldn\'t save server list", (Throwable)exception);
        }
    }

    public ServerData getServerData(int index)
    {
        return this.servers.get(index);
    }

    public void removeServerData(int index)
    {
        this.servers.remove(index);
    }

    public void addServerData(ServerData server)
    {
        this.servers.add(server);
    }

    public int countServers()
    {
        return this.servers.size();
    }

    public void swapServers(int serverIndex1, int serverIndex2)
    {
        ServerData serverData = this.getServerData(serverIndex1);
        this.servers.set(serverIndex1, this.getServerData(serverIndex2));
        this.servers.set(serverIndex2, serverData);
        this.saveServerList();
    }

    public void setServer(int index, ServerData server)
    {
        this.servers.set(index, server);
    }

    public static void saveSingleServer(ServerData serverData)
    {
        ServerList serverList = new ServerList(Minecraft.getMinecraft());
        serverList.loadServerList();

        for (int i = 0; i < serverList.countServers(); ++i)
        {
            ServerData serverdata = serverList.getServerData(i);

            if (serverdata.serverName.equals(serverData.serverName) && serverdata.serverIP.equals(serverData.serverIP))
            {
                serverList.setServer(i, serverData);
                break;
            }
        }

        serverList.saveServerList();
    }
}
