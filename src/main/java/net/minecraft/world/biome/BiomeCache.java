package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LongHashMap;

public class BiomeCache
{
    private final WorldChunkManager chunkManager;
    private long lastCleanupTime;
    private LongHashMap<BiomeCache.Block> cacheMap = new LongHashMap();
    private List<BiomeCache.Block> cache = Lists.<BiomeCache.Block>newArrayList();

    public BiomeCache(WorldChunkManager chunkManagerIn)
    {
        this.chunkManager = chunkManagerIn;
    }

    public BiomeCache.Block getBiomeCacheBlock(int x, int z)
    {
        x = x >> 4;
        z = z >> 4;
        long cacheKey = (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
        BiomeCache.Block biomeCacheBlock = (BiomeCache.Block)this.cacheMap.getValueByKey(cacheKey);

        if (biomeCacheBlock == null)
        {
            biomeCacheBlock = new BiomeCache.Block(x, z);
            this.cacheMap.add(cacheKey, biomeCacheBlock);
            this.cache.add(biomeCacheBlock);
        }

        biomeCacheBlock.lastAccessTime = MinecraftServer.getCurrentTimeMillis();
        return biomeCacheBlock;
    }

    public BiomeGenBase getBiomeGenAtWithFallback(int x, int z, BiomeGenBase fallback)
    {
        BiomeGenBase biomegenbase = this.getBiomeCacheBlock(x, z).getBiomeGenAt(x, z);
        return biomegenbase == null ? fallback : biomegenbase;
    }

    public void cleanupCache()
    {
        long currentTime = MinecraftServer.getCurrentTimeMillis();
        long elapsedSinceCleanup = currentTime - this.lastCleanupTime;

        if (elapsedSinceCleanup > 7500L || elapsedSinceCleanup < 0L)
        {
            this.lastCleanupTime = currentTime;

            for (int cacheIndex = 0; cacheIndex < this.cache.size(); ++cacheIndex)
            {
                BiomeCache.Block biomeCacheBlock = (BiomeCache.Block)this.cache.get(cacheIndex);
                long entryAge = currentTime - biomeCacheBlock.lastAccessTime;

                if (entryAge > 30000L || entryAge < 0L)
                {
                    this.cache.remove(cacheIndex--);
                    long cacheKey = (long)biomeCacheBlock.xPosition & 4294967295L | ((long)biomeCacheBlock.zPosition & 4294967295L) << 32;
                    this.cacheMap.remove(cacheKey);
                }
            }
        }
    }

    public BiomeGenBase[] getCachedBiomes(int x, int z)
    {
        return this.getBiomeCacheBlock(x, z).biomes;
    }

    public class Block
    {
        public float[] rainfallValues = new float[256];
        public BiomeGenBase[] biomes = new BiomeGenBase[256];
        public int xPosition;
        public int zPosition;
        public long lastAccessTime;

        public Block(int x, int z)
        {
            this.xPosition = x;
            this.zPosition = z;
            BiomeCache.this.chunkManager.getRainfall(this.rainfallValues, x << 4, z << 4, 16, 16);
            BiomeCache.this.chunkManager.getBiomeGenAt(this.biomes, x << 4, z << 4, 16, 16, false);
        }

        public BiomeGenBase getBiomeGenAt(int x, int z)
        {
            return this.biomes[x & 15 | (z & 15) << 4];
        }
    }
}
