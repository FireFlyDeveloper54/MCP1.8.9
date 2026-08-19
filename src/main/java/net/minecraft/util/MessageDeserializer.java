package net.minecraft.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class MessageDeserializer extends ByteToMessageDecoder
{
    private static final Logger logger = LogManager.getLogger();
    private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_RECEIVED", NetworkManager.logMarkerPackets);
    private final EnumPacketDirection direction;

    public MessageDeserializer(EnumPacketDirection direction)
    {
        this.direction = direction;
    }

    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws IOException, InstantiationException, IllegalAccessException, Exception
    {
        if (in.readableBytes() != 0)
        {
            PacketBuffer packetbuffer = new PacketBuffer(in);
            int packetId = packetbuffer.readVarIntFromBuffer();
            Packet packet = ((EnumConnectionState)context.channel().attr(NetworkManager.attrKeyConnectionState).get()).getPacket(this.direction, packetId);

            if (packet == null)
            {
                throw new IOException("Bad packet id " + packetId);
            }
            else
            {
                packet.readPacketData(packetbuffer);

                if (packetbuffer.readableBytes() > 0)
                {
                    throw new IOException("Packet " + ((EnumConnectionState)context.channel().attr(NetworkManager.attrKeyConnectionState).get()).getId() + "/" + packetId + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + packetbuffer.readableBytes() + " bytes extra whilst reading packet " + packetId);
                }
                else
                {
                    out.add(packet);

                    if (logger.isDebugEnabled())
                    {
                        logger.debug(RECEIVED_PACKET_MARKER, " IN: [{}:{}] {}", new Object[] {context.channel().attr(NetworkManager.attrKeyConnectionState).get(), Integer.valueOf(packetId), packet.getClass().getName()});
                    }
                }
            }
        }
    }
}
