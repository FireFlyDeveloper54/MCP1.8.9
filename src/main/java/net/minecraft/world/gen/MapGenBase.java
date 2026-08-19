package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;

public class MapGenBase
{
    protected int range = 8;
    protected Random rand = new Random();
    protected World worldObj;

    public void generate(IChunkProvider chunkProviderIn, World worldIn, int x, int z, ChunkPrimer chunkPrimerIn)
    {
        int generationRange = this.range;
        this.worldObj = worldIn;
        this.rand.setSeed(worldIn.getSeed());
        long chunkSeedMultiplierX = this.rand.nextLong();
        long chunkSeedMultiplierZ = this.rand.nextLong();

        for (int chunkX = x - generationRange; chunkX <= x + generationRange; ++chunkX)
        {
            for (int chunkZ = z - generationRange; chunkZ <= z + generationRange; ++chunkZ)
            {
                long chunkSeedX = (long)chunkX * chunkSeedMultiplierX;
                long chunkSeedZ = (long)chunkZ * chunkSeedMultiplierZ;
                this.rand.setSeed(chunkSeedX ^ chunkSeedZ ^ worldIn.getSeed());
                this.recursiveGenerate(worldIn, chunkX, chunkZ, x, z, chunkPrimerIn);
            }
        }
    }

    protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalChunkX, int originalChunkZ, ChunkPrimer chunkPrimerIn)
    {
    }
}
