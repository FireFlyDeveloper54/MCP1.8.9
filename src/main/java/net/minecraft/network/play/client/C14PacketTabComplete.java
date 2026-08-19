package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.StringUtils;

public class C14PacketTabComplete implements Packet<INetHandlerPlayServer>
{
    private String message;
    private BlockPos targetBlock;

    public C14PacketTabComplete()
    {
    }

    public C14PacketTabComplete(String msg)
    {
        this(msg, (BlockPos)null);
    }

    public C14PacketTabComplete(String msg, BlockPos target)
    {
        this.message = msg;
        this.targetBlock = target;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.message = buf.readStringFromBuffer(32767);
        boolean hasTargetBlock = buf.readBoolean();

        if (hasTargetBlock)
        {
            this.targetBlock = buf.readBlockPos();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(StringUtils.substring(this.message, 0, 32767));
        boolean hasTargetBlock = this.targetBlock != null;
        buf.writeBoolean(hasTargetBlock);

        if (hasTargetBlock)
        {
            buf.writeBlockPos(this.targetBlock);
        }
    }

    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processTabComplete(this);
    }

    public String getMessage()
    {
        return this.message;
    }

    public BlockPos getTargetBlock()
    {
        return this.targetBlock;
    }
}
