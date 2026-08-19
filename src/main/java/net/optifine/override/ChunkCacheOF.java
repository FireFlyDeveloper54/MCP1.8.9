package net.optifine.override;

import java.util.Arrays;
import net.minecraft.block.state.IBlockState;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.DynamicLights;
import net.optifine.util.ArrayCache;

public class ChunkCacheOF implements IBlockAccess
{
    private final ChunkCache chunkCache;
    private final int posX;
    private final int posY;
    private final int posZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int sizeXY;
    private int[] combinedLights;
    private IBlockState[] blockStates;
    private final int arraySize;
    private final boolean dynamicLights = Config.isDynamicLights();
    private static final ArrayCache cacheCombinedLights = new ArrayCache(Integer.TYPE, 16);
    private static final ArrayCache cacheBlockStates = new ArrayCache(IBlockState.class, 16);

    public ChunkCacheOF(ChunkCache chunkCache, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        this.chunkCache = chunkCache;
        int minChunkX = posFromIn.getX() - subIn >> 4;
        int minChunkY = posFromIn.getY() - subIn >> 4;
        int minChunkZ = posFromIn.getZ() - subIn >> 4;
        int maxChunkX = posToIn.getX() + subIn >> 4;
        int maxChunkY = posToIn.getY() + subIn >> 4;
        int maxChunkZ = posToIn.getZ() + subIn >> 4;
        this.sizeX = maxChunkX - minChunkX + 1 << 4;
        this.sizeY = maxChunkY - minChunkY + 1 << 4;
        this.sizeZ = maxChunkZ - minChunkZ + 1 << 4;
        this.sizeXY = this.sizeX * this.sizeY;
        this.arraySize = this.sizeX * this.sizeY * this.sizeZ;
        this.posX = minChunkX << 4;
        this.posY = minChunkY << 4;
        this.posZ = minChunkZ << 4;
    }

    private int getPositionIndex(BlockPos pos)
    {
        int offsetX = pos.getX() - this.posX;

        if (offsetX >= 0 && offsetX < this.sizeX)
        {
            int offsetY = pos.getY() - this.posY;

            if (offsetY >= 0 && offsetY < this.sizeY)
            {
                int offsetZ = pos.getZ() - this.posZ;
                return offsetZ >= 0 && offsetZ < this.sizeZ ? offsetZ * this.sizeXY + offsetY * this.sizeX + offsetX : -1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            return -1;
        }
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int index = this.getPositionIndex(pos);

        if (index >= 0 && index < this.arraySize && this.combinedLights != null)
        {
            int combinedLight = this.combinedLights[index];

            if (combinedLight == -1)
            {
                combinedLight = this.getCombinedLightRaw(pos, lightValue);
                this.combinedLights[index] = combinedLight;
            }

            return combinedLight;
        }
        else
        {
            return this.getCombinedLightRaw(pos, lightValue);
        }
    }

    private int getCombinedLightRaw(BlockPos pos, int lightValue)
    {
        int combinedLight = this.chunkCache.getCombinedLight(pos, lightValue);

        if (this.dynamicLights && !this.getBlockState(pos).getBlock().isOpaqueCube())
        {
            combinedLight = DynamicLights.getCombinedLight(pos, combinedLight);
        }

        return combinedLight;
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        int index = this.getPositionIndex(pos);

        if (index >= 0 && index < this.arraySize && this.blockStates != null)
        {
            IBlockState blockState = this.blockStates[index];

            if (blockState == null)
            {
                blockState = this.chunkCache.getBlockState(pos);
                this.blockStates[index] = blockState;
            }

            return blockState;
        }
        else
        {
            return this.chunkCache.getBlockState(pos);
        }
    }

    public void renderStart()
    {
        if (this.combinedLights == null)
        {
            this.combinedLights = (int[])((int[])cacheCombinedLights.allocate(this.arraySize));
        }

        Arrays.fill((int[])this.combinedLights, (int) - 1);

        if (this.blockStates == null)
        {
            this.blockStates = (IBlockState[])((IBlockState[])cacheBlockStates.allocate(this.arraySize));
        }

        Arrays.fill(this.blockStates, (Object)null);
    }

    public void renderFinish()
    {
        cacheCombinedLights.free(this.combinedLights);
        this.combinedLights = null;
        cacheBlockStates.free(this.blockStates);
        this.blockStates = null;
    }

    public boolean extendedLevelsInChunkCache()
    {
        return this.chunkCache.extendedLevelsInChunkCache();
    }

    public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        return this.chunkCache.getBiomeGenForCoords(pos);
    }

    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return this.chunkCache.getStrongPower(pos, direction);
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        return this.chunkCache.getTileEntity(pos);
    }

    public WorldType getWorldType()
    {
        return this.chunkCache.getWorldType();
    }

    public boolean isAirBlock(BlockPos pos)
    {
        return this.chunkCache.isAirBlock(pos);
    }

    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return _default;
    }
}
