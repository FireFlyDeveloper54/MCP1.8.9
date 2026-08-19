package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Collection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;

public class S34PacketMaps implements Packet<INetHandlerPlayClient>
{
    private int mapId;
    private byte mapScale;
    private Vec4b[] mapVisiblePlayersVec4b;
    private int mapMinX;
    private int mapMinY;
    private int mapMaxX;
    private int mapMaxY;
    private byte[] mapDataBytes;

    public S34PacketMaps()
    {
    }

    public S34PacketMaps(int mapIdIn, byte scale, Collection<Vec4b> visiblePlayers, byte[] colors, int minX, int minY, int maxX, int maxY)
    {
        this.mapId = mapIdIn;
        this.mapScale = scale;
        this.mapVisiblePlayersVec4b = (Vec4b[])visiblePlayers.toArray(new Vec4b[visiblePlayers.size()]);
        this.mapMinX = minX;
        this.mapMinY = minY;
        this.mapMaxX = maxX;
        this.mapMaxY = maxY;
        this.mapDataBytes = new byte[maxX * maxY];

        for (int yOffset = 0; yOffset < maxY; ++yOffset)
        {
            System.arraycopy(colors, minX + (minY + yOffset) * 128, this.mapDataBytes, yOffset * maxX, maxX);
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.mapId = buf.readVarIntFromBuffer();
        this.mapScale = buf.readByte();
        this.mapVisiblePlayersVec4b = new Vec4b[buf.readVarIntFromBuffer()];

        for (int iconIndex = 0; iconIndex < this.mapVisiblePlayersVec4b.length; ++iconIndex)
        {
            short packedIconData = (short)buf.readByte();
            this.mapVisiblePlayersVec4b[iconIndex] = new Vec4b((byte)(packedIconData >> 4 & 15), buf.readByte(), buf.readByte(), (byte)(packedIconData & 15));
        }

        this.mapMaxX = buf.readUnsignedByte();

        if (this.mapMaxX > 0)
        {
            this.mapMaxY = buf.readUnsignedByte();
            this.mapMinX = buf.readUnsignedByte();
            this.mapMinY = buf.readUnsignedByte();
            this.mapDataBytes = buf.readByteArray();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.mapId);
        buf.writeByte(this.mapScale);
        buf.writeVarIntToBuffer(this.mapVisiblePlayersVec4b.length);

        for (Vec4b vec4b : this.mapVisiblePlayersVec4b)
        {
            buf.writeByte((vec4b.getX() & 15) << 4 | vec4b.getW() & 15);
            buf.writeByte(vec4b.getY());
            buf.writeByte(vec4b.getZ());
        }

        buf.writeByte(this.mapMaxX);

        if (this.mapMaxX > 0)
        {
            buf.writeByte(this.mapMaxY);
            buf.writeByte(this.mapMinX);
            buf.writeByte(this.mapMinY);
            buf.writeByteArray(this.mapDataBytes);
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleMaps(this);
    }

    public int getMapId()
    {
        return this.mapId;
    }

    public void setMapdataTo(MapData mapdataIn)
    {
        mapdataIn.scale = this.mapScale;
        mapdataIn.mapDecorations.clear();

        for (int iconIndex = 0; iconIndex < this.mapVisiblePlayersVec4b.length; ++iconIndex)
        {
            Vec4b mapDecoration = this.mapVisiblePlayersVec4b[iconIndex];
            mapdataIn.mapDecorations.put("icon-" + iconIndex, mapDecoration);
        }

        for (int yOffset = 0; yOffset < this.mapMaxY; ++yOffset)
        {
            System.arraycopy(this.mapDataBytes, yOffset * this.mapMaxX, mapdataIn.colors, this.mapMinX + (this.mapMinY + yOffset) * 128, this.mapMaxX);
        }
    }
}
