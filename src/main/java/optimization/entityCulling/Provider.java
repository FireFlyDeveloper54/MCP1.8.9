package optimization.entityCulling;

import optimization.occlusionCulling.DataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.IChunkProvider;

public class Provider implements DataProvider {

    private final Minecraft client = Minecraft.getMinecraft();
    private WorldClient world = null;
    
    public void bindWorld(WorldClient worldIn) {
        this.world = worldIn;
    }

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        WorldClient currentWorld = this.world != null ? this.world : client.theWorld;
        this.world = currentWorld;
        if (currentWorld == null) {
            return false;
        }
        if (!EntityCulling.instance.isLoadedChunksOnly()) {
            return true;
        }

        IChunkProvider provider = currentWorld.getChunkProvider();
        if (provider instanceof ChunkProviderClient) {
            return ((ChunkProviderClient) provider).getLoadedChunk(chunkX, chunkZ) != null;
        }
        return provider.chunkExists(chunkX, chunkZ);
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        WorldClient currentWorld = this.world;
        if (currentWorld == null) {
            return false;
        }

        try {
            BlockPos pos = new BlockPos(x, y, z);
            return currentWorld.getBlockState(pos).getBlock().isOpaqueCube();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void cleanup() {
        world = null;
    }

}
