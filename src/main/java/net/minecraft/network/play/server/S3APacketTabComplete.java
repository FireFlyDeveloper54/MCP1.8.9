package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S3APacketTabComplete implements Packet<INetHandlerPlayClient>
{
    private String[] matches;

    public S3APacketTabComplete()
    {
    }

    public S3APacketTabComplete(String[] matchesIn)
    {
        this.matches = matchesIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.matches = new String[buf.readVarIntFromBuffer()];

        for (int matchIndex = 0; matchIndex < this.matches.length; ++matchIndex)
        {
            this.matches[matchIndex] = buf.readStringFromBuffer(32767);
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.matches.length);

        for (String match : this.matches)
        {
            buf.writeString(match);
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleTabComplete(this);
    }

    public String[] getMatches()
    {
        return this.matches;
    }
}
