package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

public class C12PacketUpdateSign implements Packet<INetHandlerPlayServer>
{
    private BlockPos pos;
    private IChatComponent[] lines;

    public C12PacketUpdateSign()
    {
    }

    public C12PacketUpdateSign(BlockPos pos, IChatComponent[] lines)
    {
        this.pos = pos;
        this.lines = new IChatComponent[] {lines[0], lines[1], lines[2], lines[3]};
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.pos = buf.readBlockPos();
        this.lines = new IChatComponent[4];

        for (int lineIndex = 0; lineIndex < 4; ++lineIndex)
        {
            String lineJson = buf.readStringFromBuffer(384);
            IChatComponent lineComponent = IChatComponent.Serializer.jsonToComponent(lineJson);
            this.lines[lineIndex] = lineComponent;
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeBlockPos(this.pos);

        for (int lineIndex = 0; lineIndex < 4; ++lineIndex)
        {
            IChatComponent lineComponent = this.lines[lineIndex];
            String lineJson = IChatComponent.Serializer.componentToJson(lineComponent);
            buf.writeString(lineJson);
        }
    }

    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processUpdateSign(this);
    }

    public BlockPos getPosition()
    {
        return this.pos;
    }

    public IChatComponent[] getLines()
    {
        return this.lines;
    }
}
