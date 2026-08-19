package net.minecraft.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class MessageSerializer extends MessageToByteEncoder<Packet>
{
    private static final Logger logger = LogManager.getLogger();
    private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT", NetworkManager.logMarkerPackets);
    private final EnumPacketDirection direction;

    public MessageSerializer(EnumPacketDirection direction)
    {
        this.direction = direction;
    }

    protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf out) throws IOException, Exception
    {
        Integer packetId = ((EnumConnectionState)context.channel().attr(NetworkManager.attrKeyConnectionState).get()).getPacketId(this.direction, packet);

        if (logger.isDebugEnabled())
        {
            logger.debug(RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", new Object[] {context.channel().attr(NetworkManager.attrKeyConnectionState).get(), packetId, packet.getClass().getName()});
        }

        if (packetId == null)
        {
            throw new IOException("Can\'t serialize unregistered packet");
        }
        else
        {
            PacketBuffer packetBuffer = new PacketBuffer(out);
            packetBuffer.writeVarIntToBuffer(packetId.intValue());

            try
            {
                packet.writePacketData(packetBuffer);
            }
            catch (Throwable throwable)
            {
                logger.error((Object)throwable);
            }
        }
    }
}
