package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S13PacketDestroyEntities implements Packet<INetHandlerPlayClient>
{
    private int[] entityIDs;

    public S13PacketDestroyEntities()
    {
    }

    public S13PacketDestroyEntities(int... entityIDsIn)
    {
        this.entityIDs = entityIDsIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityIDs = new int[buf.readVarIntFromBuffer()];

        for (int entityIndex = 0; entityIndex < this.entityIDs.length; ++entityIndex)
        {
            this.entityIDs[entityIndex] = buf.readVarIntFromBuffer();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.entityIDs.length);

        for (int entityIndex = 0; entityIndex < this.entityIDs.length; ++entityIndex)
        {
            buf.writeVarIntToBuffer(this.entityIDs[entityIndex]);
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleDestroyEntities(this);
    }

    public int[] getEntityIDs()
    {
        return this.entityIDs;
    }
}
