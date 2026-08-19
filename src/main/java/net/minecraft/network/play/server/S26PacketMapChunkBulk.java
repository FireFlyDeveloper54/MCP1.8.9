package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.chunk.Chunk;

public class S26PacketMapChunkBulk implements Packet<INetHandlerPlayClient>
{
    private int[] xPositions;
    private int[] zPositions;
    private S21PacketChunkData.Extracted[] chunksData;
    private boolean isOverworld;

    public S26PacketMapChunkBulk()
    {
    }

    public S26PacketMapChunkBulk(List<Chunk> chunks)
    {
        int chunkCount = chunks.size();
        this.xPositions = new int[chunkCount];
        this.zPositions = new int[chunkCount];
        this.chunksData = new S21PacketChunkData.Extracted[chunkCount];
        this.isOverworld = !chunks.get(0).getWorld().provider.getHasNoSky();

        for (int chunkIndex = 0; chunkIndex < chunkCount; ++chunkIndex)
        {
            Chunk chunk = chunks.get(chunkIndex);
            S21PacketChunkData.Extracted extracted = S21PacketChunkData.getExtractedData(chunk, true, this.isOverworld, 65535);
            this.xPositions[chunkIndex] = chunk.xPosition;
            this.zPositions[chunkIndex] = chunk.zPosition;
            this.chunksData[chunkIndex] = extracted;
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.isOverworld = buf.readBoolean();
        int chunkCount = buf.readVarIntFromBuffer();
        this.xPositions = new int[chunkCount];
        this.zPositions = new int[chunkCount];
        this.chunksData = new S21PacketChunkData.Extracted[chunkCount];

        for (int chunkIndex = 0; chunkIndex < chunkCount; ++chunkIndex)
        {
            this.xPositions[chunkIndex] = buf.readInt();
            this.zPositions[chunkIndex] = buf.readInt();
            this.chunksData[chunkIndex] = new S21PacketChunkData.Extracted();
            this.chunksData[chunkIndex].dataSize = buf.readShort() & 65535;
            this.chunksData[chunkIndex].data = new byte[S21PacketChunkData.calculateChunkDataSize(Integer.bitCount(this.chunksData[chunkIndex].dataSize), this.isOverworld, true)];
        }

        for (int chunkIndex = 0; chunkIndex < chunkCount; ++chunkIndex)
        {
            buf.readBytes(this.chunksData[chunkIndex].data);
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeBoolean(this.isOverworld);
        buf.writeVarIntToBuffer(this.chunksData.length);

        for (int chunkIndex = 0; chunkIndex < this.xPositions.length; ++chunkIndex)
        {
            buf.writeInt(this.xPositions[chunkIndex]);
            buf.writeInt(this.zPositions[chunkIndex]);
            buf.writeShort((short)(this.chunksData[chunkIndex].dataSize & 65535));
        }

        for (int chunkIndex = 0; chunkIndex < this.xPositions.length; ++chunkIndex)
        {
            buf.writeBytes(this.chunksData[chunkIndex].data);
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleMapChunkBulk(this);
    }

    public int getChunkX(int chunkIndex)
    {
        return this.xPositions[chunkIndex];
    }

    public int getChunkZ(int chunkIndex)
    {
        return this.zPositions[chunkIndex];
    }

    public int getChunkCount()
    {
        return this.xPositions.length;
    }

    public byte[] getChunkBytes(int chunkIndex)
    {
        return this.chunksData[chunkIndex].data;
    }

    public int getChunkSize(int chunkIndex)
    {
        return this.chunksData[chunkIndex].dataSize;
    }
}
