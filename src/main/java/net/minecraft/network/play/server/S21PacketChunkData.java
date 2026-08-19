package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class S21PacketChunkData implements Packet<INetHandlerPlayClient>
{
    private int chunkX;
    private int chunkZ;
    private S21PacketChunkData.Extracted extractedData;
    private boolean fullChunk;

    public S21PacketChunkData()
    {
    }

    public S21PacketChunkData(Chunk chunkIn, boolean fullChunk, int sectionMask)
    {
        this.chunkX = chunkIn.xPosition;
        this.chunkZ = chunkIn.zPosition;
        this.fullChunk = fullChunk;
        this.extractedData = getExtractedData(chunkIn, fullChunk, !chunkIn.getWorld().provider.getHasNoSky(), sectionMask);
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
        this.fullChunk = buf.readBoolean();
        this.extractedData = new S21PacketChunkData.Extracted();
        this.extractedData.dataSize = buf.readShort();
        this.extractedData.data = buf.readByteArray();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(this.chunkX);
        buf.writeInt(this.chunkZ);
        buf.writeBoolean(this.fullChunk);
        buf.writeShort((short)(this.extractedData.dataSize & 65535));
        buf.writeByteArray(this.extractedData.data);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleChunkData(this);
    }

    public byte[] getExtractedDataBytes()
    {
        return this.extractedData.data;
    }

    protected static int calculateChunkDataSize(int sectionCount, boolean includeSkyLight, boolean includeBiomeData)
    {
        int blockDataBytes = sectionCount * 2 * 16 * 16 * 16;
        int blockLightBytes = sectionCount * 16 * 16 * 16 / 2;
        int skyLightBytes = includeSkyLight ? sectionCount * 16 * 16 * 16 / 2 : 0;
        int biomeBytes = includeBiomeData ? 256 : 0;
        return blockDataBytes + blockLightBytes + skyLightBytes + biomeBytes;
    }

    public static S21PacketChunkData.Extracted getExtractedData(Chunk chunk, boolean includeBiomeData, boolean includeSkyLight, int sectionMask)
    {
        chunk.getLightingEngine().processLightUpdates();
        ExtendedBlockStorage[] blockStorageArray = chunk.getBlockStorageArray();
        S21PacketChunkData.Extracted extracted = new S21PacketChunkData.Extracted();
        List<ExtendedBlockStorage> sections = Lists.<ExtendedBlockStorage>newArrayListWithCapacity(blockStorageArray.length);

        for (int sectionIndex = 0; sectionIndex < blockStorageArray.length; ++sectionIndex)
        {
            ExtendedBlockStorage storage = blockStorageArray[sectionIndex];

            if (storage != null && (!includeBiomeData || !storage.isEmpty()) && (sectionMask & 1 << sectionIndex) != 0)
            {
                extracted.dataSize |= 1 << sectionIndex;
                sections.add(storage);
            }
        }

        extracted.data = new byte[calculateChunkDataSize(Integer.bitCount(extracted.dataSize), includeSkyLight, includeBiomeData)];
        int writeIndex = 0;

        for (ExtendedBlockStorage storage : sections)
        {
            char[] blockData = storage.getData();

            for (char serializedState : blockData)
            {
                extracted.data[writeIndex++] = (byte)(serializedState & 255);
                extracted.data[writeIndex++] = (byte)(serializedState >> 8 & 255);
            }
        }

        for (ExtendedBlockStorage storage : sections)
        {
            writeIndex = appendByteArray(storage.getBlocklightArray().getData(), extracted.data, writeIndex);
        }

        if (includeSkyLight)
        {
            for (ExtendedBlockStorage storage : sections)
            {
                writeIndex = appendByteArray(storage.getSkylightArray().getData(), extracted.data, writeIndex);
            }
        }

        if (includeBiomeData)
        {
            appendByteArray(chunk.getBiomeArray(), extracted.data, writeIndex);
        }

        return extracted;
    }

    private static int appendByteArray(byte[] source, byte[] target, int writeIndex)
    {
        System.arraycopy(source, 0, target, writeIndex, source.length);
        return writeIndex + source.length;
    }

    public int getChunkX()
    {
        return this.chunkX;
    }

    public int getChunkZ()
    {
        return this.chunkZ;
    }

    public int getExtractedSize()
    {
        return this.extractedData.dataSize;
    }

    public boolean isFullChunk()
    {
        return this.fullChunk;
    }

    public static class Extracted
    {
        public byte[] data;
        public int dataSize;
    }
}
