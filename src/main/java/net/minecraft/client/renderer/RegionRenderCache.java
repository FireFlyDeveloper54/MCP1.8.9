package net.minecraft.client.renderer;

import java.util.ArrayDeque;
import java.util.Arrays;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.optifine.DynamicLights;

public class RegionRenderCache extends ChunkCache
{
    private static final IBlockState DEFAULT_STATE = Blocks.air.getDefaultState();
    private final BlockPos position;
    private int[] combinedLights;
    private IBlockState[] blockStates;
    private static ArrayDeque<int[]> cacheLights = new ArrayDeque();
    private static ArrayDeque<IBlockState[]> cacheStates = new ArrayDeque();
    private static int maxCacheSize = Config.limit(Runtime.getRuntime().availableProcessors(), 1, 32);

    public RegionRenderCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        super(worldIn, posFromIn, posToIn, subIn);
        this.position = posFromIn.subtract(new Vec3i(subIn, subIn, subIn));
        int cacheSize = 8000;
        this.combinedLights = allocateLights(cacheSize);
        Arrays.fill((int[])((int[])this.combinedLights), (int) - 1);
        this.blockStates = allocateStates(cacheSize);
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        int chunkXIndex = (pos.getX() >> 4) - this.chunkX;
        int chunkZIndex = (pos.getZ() >> 4) - this.chunkZ;
        return this.chunkArray[chunkXIndex][chunkZIndex].getTileEntity(pos, Chunk.EnumCreateEntityType.QUEUED);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int positionIndex = this.getPositionIndex(pos);
        int combinedLight = this.combinedLights[positionIndex];

        if (combinedLight == -1)
        {
            combinedLight = super.getCombinedLight(pos, lightValue);

            if (Config.isDynamicLights() && !this.getBlockState(pos).getBlock().isOpaqueCube())
            {
                combinedLight = DynamicLights.getCombinedLight(pos, combinedLight);
            }

            this.combinedLights[positionIndex] = combinedLight;
        }

        return combinedLight;
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        int positionIndex = this.getPositionIndex(pos);
        IBlockState blockState = this.blockStates[positionIndex];

        if (blockState == null)
        {
            blockState = this.getBlockStateRaw(pos);
            this.blockStates[positionIndex] = blockState;
        }

        return blockState;
    }

    private IBlockState getBlockStateRaw(BlockPos pos)
    {
        return super.getBlockState(pos);
    }

    private int getPositionIndex(BlockPos pos)
    {
        int relativeX = pos.getX() - this.position.getX();
        int relativeY = pos.getY() - this.position.getY();
        int relativeZ = pos.getZ() - this.position.getZ();
        return relativeX * 400 + relativeZ * 20 + relativeY;
    }

    public void freeBuffers()
    {
        freeLights(this.combinedLights);
        freeStates(this.blockStates);
    }

    private static int[] allocateLights(int size)
    {
        synchronized (cacheLights)
        {
            int[] lights = (int[])cacheLights.pollLast();

            if (lights == null || lights.length < size)
            {
                lights = new int[size];
            }

            return lights;
        }
    }

    public static void freeLights(int[] lights)
    {
        synchronized (cacheLights)
        {
            if (cacheLights.size() < maxCacheSize)
            {
                cacheLights.add(lights);
            }
        }
    }

    private static IBlockState[] allocateStates(int size)
    {
        synchronized (cacheStates)
        {
            IBlockState[] states = (IBlockState[])cacheStates.pollLast();

            if (states != null && states.length >= size)
            {
                Arrays.fill(states, (Object)null);
            }
            else
            {
                states = new IBlockState[size];
            }

            return states;
        }
    }

    public static void freeStates(IBlockState[] states)
    {
        synchronized (cacheStates)
        {
            if (cacheStates.size() < maxCacheSize)
            {
                cacheStates.add(states);
            }
        }
    }
}
