package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

public class S22PacketMultiBlockChange implements Packet<INetHandlerPlayClient>
{
    private ChunkCoordIntPair chunkPosCoord;
    private S22PacketMultiBlockChange.BlockUpdateData[] changedBlocks;

    public S22PacketMultiBlockChange()
    {
    }

    public S22PacketMultiBlockChange(int changeCount, short[] packedPositions, Chunk chunkIn)
    {
        this.chunkPosCoord = new ChunkCoordIntPair(chunkIn.xPosition, chunkIn.zPosition);
        this.changedBlocks = new S22PacketMultiBlockChange.BlockUpdateData[changeCount];

        for (int blockIndex = 0; blockIndex < this.changedBlocks.length; ++blockIndex)
        {
            this.changedBlocks[blockIndex] = new S22PacketMultiBlockChange.BlockUpdateData(packedPositions[blockIndex], chunkIn);
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.chunkPosCoord = new ChunkCoordIntPair(buf.readInt(), buf.readInt());
        this.changedBlocks = new S22PacketMultiBlockChange.BlockUpdateData[buf.readVarIntFromBuffer()];

        for (int blockIndex = 0; blockIndex < this.changedBlocks.length; ++blockIndex)
        {
            this.changedBlocks[blockIndex] = new S22PacketMultiBlockChange.BlockUpdateData(buf.readShort(), (IBlockState)Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer()));
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(this.chunkPosCoord.chunkXPos);
        buf.writeInt(this.chunkPosCoord.chunkZPos);
        buf.writeVarIntToBuffer(this.changedBlocks.length);

        for (S22PacketMultiBlockChange.BlockUpdateData blockUpdate : this.changedBlocks)
        {
            buf.writeShort(blockUpdate.getPackedChunkPosition());
            buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(blockUpdate.getBlockState()));
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleMultiBlockChange(this);
    }

    public S22PacketMultiBlockChange.BlockUpdateData[] getChangedBlocks()
    {
        return this.changedBlocks;
    }

    public class BlockUpdateData
    {
        private final short chunkPosCrammed;
        private final IBlockState blockState;

        public BlockUpdateData(short packedChunkPosition, IBlockState state)
        {
            this.chunkPosCrammed = packedChunkPosition;
            this.blockState = state;
        }

        public BlockUpdateData(short packedChunkPosition, Chunk chunkIn)
        {
            this.chunkPosCrammed = packedChunkPosition;
            this.blockState = chunkIn.getBlockState(this.getPos());
        }

        public BlockPos getPos()
        {
            return new BlockPos(S22PacketMultiBlockChange.this.chunkPosCoord.getBlock(this.chunkPosCrammed >> 12 & 15, this.chunkPosCrammed & 255, this.chunkPosCrammed >> 8 & 15));
        }

        public short getPackedChunkPosition()
        {
            return this.chunkPosCrammed;
        }

        public IBlockState getBlockState()
        {
            return this.blockState;
        }
    }
}
