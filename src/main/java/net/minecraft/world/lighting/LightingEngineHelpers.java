package net.minecraft.world.lighting;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;

public class LightingEngineHelpers {

    private static final IBlockState DEFAULT_BLOCK_STATE = Blocks.air.getDefaultState();

    // Avoids some additional logic in Chunk#getBlockState... 0 is always air
    static IBlockState posToState(final BlockPos pos, final Chunk chunk) {
        int sectionIndex = pos.getY() >> 4;
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        return sectionIndex < 0 || sectionIndex >= sections.length ? DEFAULT_BLOCK_STATE : posToState(pos, sections[sectionIndex]);
    }

    static IBlockState posToState(final BlockPos pos, final ExtendedBlockStorage section) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        if (section != null)
        {
            IBlockState state = section.get(x & 15, y & 15, z & 15);
            if (state != null) {
                return state;
            }
        }

        return DEFAULT_BLOCK_STATE;
    }

    static int getLightValueForState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return state.getBlock().getLightValue();
    }

    public static Chunk getLoadedChunk(IChunkProvider chunkProvider, int x, int z) {
        if (chunkProvider instanceof ChunkProviderServer) {
            return ((ChunkProviderServer) chunkProvider).getLoadedChunk(x, z);
        }
        if (chunkProvider instanceof ChunkProviderClient) {
            return ((ChunkProviderClient) chunkProvider).getLoadedChunk(x, z);
        }

        return chunkProvider.chunkExists(x, z) ? chunkProvider.provideChunk(x, z) : null;
    }
}
