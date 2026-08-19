package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkProviderDebug implements IChunkProvider
{
    private static final List<IBlockState> debugStates = Lists.<IBlockState>newArrayList();
    private static final int debugColumns;
    private static final int debugRows;
    private final World world;

    public ChunkProviderDebug(World worldIn)
    {
        this.world = worldIn;
    }

    public Chunk provideChunk(int x, int z)
    {
        ChunkPrimer chunkPrimer = new ChunkPrimer();

        for (int localX = 0; localX < 16; ++localX)
        {
            for (int localZ = 0; localZ < 16; ++localZ)
            {
                int blockX = x * 16 + localX;
                int blockZ = z * 16 + localZ;
                chunkPrimer.setBlockState(localX, 60, localZ, Blocks.barrier.getDefaultState());
                IBlockState debugState = getDebugState(blockX, blockZ);

                if (debugState != null)
                {
                    chunkPrimer.setBlockState(localX, 70, localZ, debugState);
                }
            }
        }

        Chunk chunk = new Chunk(this.world, chunkPrimer, x, z);
        chunk.generateSkylightMap();
        BiomeGenBase[] biomes = this.world.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, x * 16, z * 16, 16, 16);
        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex)
        {
            biomeArray[biomeIndex] = (byte)biomes[biomeIndex].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    public static IBlockState getDebugState(int x, int z)
    {
        IBlockState debugState = null;

        if (x > 0 && z > 0 && x % 2 != 0 && z % 2 != 0)
        {
            x = x / 2;
            z = z / 2;

            if (x <= debugColumns && z <= debugRows)
            {
                int stateIndex = MathHelper.abs_int(x * debugColumns + z);

                if (stateIndex < debugStates.size())
                {
                    debugState = (IBlockState)debugStates.get(stateIndex);
                }
            }
        }

        return debugState;
    }

    public boolean chunkExists(int x, int z)
    {
        return true;
    }

    public void populate(IChunkProvider chunkProvider, int x, int z)
    {
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z)
    {
        return false;
    }

    public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback)
    {
        return true;
    }

    public void saveExtraData()
    {
    }

    public boolean unloadQueuedChunks()
    {
        return false;
    }

    public boolean canSave()
    {
        return true;
    }

    public String makeString()
    {
        return "DebugLevelSource";
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        BiomeGenBase biome = this.world.getBiomeGenForCoords(pos);
        return biome.getSpawnableList(creatureType);
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return null;
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }

    static
    {
        for (Block registeredBlock : Block.blockRegistry)
        {
            debugStates.addAll(registeredBlock.getBlockState().getValidStates());
        }

        debugColumns = MathHelper.ceiling_float_int(MathHelper.sqrt_float((float)debugStates.size()));
        debugRows = MathHelper.ceiling_float_int((float)debugStates.size() / (float)debugColumns);
    }
}
