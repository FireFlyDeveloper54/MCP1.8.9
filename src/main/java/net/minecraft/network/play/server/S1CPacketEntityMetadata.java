package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.entity.DataWatcher;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S1CPacketEntityMetadata implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private List<DataWatcher.WatchableObject> watchedObjects;

    public S1CPacketEntityMetadata()
    {
    }

    public S1CPacketEntityMetadata(int entityIdIn, DataWatcher watcher, boolean sendAllWatched)
    {
        this.entityId = entityIdIn;

        if (sendAllWatched)
        {
            this.watchedObjects = watcher.getAllWatched();
        }
        else
        {
            this.watchedObjects = watcher.getChanged();
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readVarIntFromBuffer();
        this.watchedObjects = DataWatcher.readWatchedListFromPacketBuffer(buf);
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.entityId);
        DataWatcher.writeWatchedListToPacketBuffer(this.watchedObjects, buf);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityMetadata(this);
    }

    public List<DataWatcher.WatchableObject> getWatchedObjects()
    {
        return this.watchedObjects;
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}
