package net.minecraft.world;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class ChunkCache implements IBlockAccess
{
    protected int chunkX;
    protected int chunkZ;
    protected Chunk[][] chunkArray;
    protected boolean hasExtendedLevels;
    protected World worldObj;

    public ChunkCache(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        this.worldObj = worldIn;
        this.chunkX = posFromIn.getX() - subIn >> 4;
        this.chunkZ = posFromIn.getZ() - subIn >> 4;
        int maxChunkX = posToIn.getX() + subIn >> 4;
        int maxChunkZ = posToIn.getZ() + subIn >> 4;
        this.chunkArray = new Chunk[maxChunkX - this.chunkX + 1][maxChunkZ - this.chunkZ + 1];
        this.hasExtendedLevels = true;

        for (int chunkXIndex = this.chunkX; chunkXIndex <= maxChunkX; ++chunkXIndex)
        {
            for (int chunkZIndex = this.chunkZ; chunkZIndex <= maxChunkZ; ++chunkZIndex)
            {
                this.chunkArray[chunkXIndex - this.chunkX][chunkZIndex - this.chunkZ] = worldIn.getChunkFromChunkCoords(chunkXIndex, chunkZIndex);
            }
        }

        for (int checkChunkX = posFromIn.getX() >> 4; checkChunkX <= posToIn.getX() >> 4; ++checkChunkX)
        {
            for (int checkChunkZ = posFromIn.getZ() >> 4; checkChunkZ <= posToIn.getZ() >> 4; ++checkChunkZ)
            {
                Chunk chunk = this.chunkArray[checkChunkX - this.chunkX][checkChunkZ - this.chunkZ];

                if (chunk != null && !chunk.getAreLevelsEmpty(posFromIn.getY(), posToIn.getY()))
                {
                    this.hasExtendedLevels = false;
                }
            }
        }
    }

    public boolean extendedLevelsInChunkCache()
    {
        return this.hasExtendedLevels;
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        int chunkArrayX = (pos.getX() >> 4) - this.chunkX;
        int chunkArrayZ = (pos.getZ() >> 4) - this.chunkZ;
        return this.chunkArray[chunkArrayX][chunkArrayZ].getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int skyLight = this.getLightForExt(EnumSkyBlock.SKY, pos);
        int blockLight = this.getLightForExt(EnumSkyBlock.BLOCK, pos);

        if (blockLight < lightValue)
        {
            blockLight = lightValue;
        }

        return skyLight << 20 | blockLight << 4;
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int chunkArrayX = (pos.getX() >> 4) - this.chunkX;
            int chunkArrayZ = (pos.getZ() >> 4) - this.chunkZ;

            if (chunkArrayX >= 0 && chunkArrayX < this.chunkArray.length && chunkArrayZ >= 0 && chunkArrayZ < this.chunkArray[chunkArrayX].length)
            {
                Chunk chunk = this.chunkArray[chunkArrayX][chunkArrayZ];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return Blocks.air.getDefaultState();
    }

    public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        return this.worldObj.getBiomeGenForCoords(pos);
    }

    private int getLightForExt(EnumSkyBlock lightType, BlockPos pos)
    {
        if (lightType == EnumSkyBlock.SKY && this.worldObj.provider.getHasNoSky())
        {
            return 0;
        }
        else if (pos.getY() >= 0 && pos.getY() < 256)
        {
            if (this.getBlockState(pos).getBlock().getUseNeighborBrightness())
            {
                int brightestNeighborLight = 0;

                for (EnumFacing facing : EnumFacing.VALUES)
                {
                    int neighborLight = this.getLightFor(lightType, pos.offset(facing));

                    if (neighborLight > brightestNeighborLight)
                    {
                        brightestNeighborLight = neighborLight;
                    }

                    if (brightestNeighborLight >= 15)
                    {
                        return brightestNeighborLight;
                    }
                }

                return brightestNeighborLight;
            }
            else
            {
                int chunkArrayX = (pos.getX() >> 4) - this.chunkX;
                int chunkArrayZ = (pos.getZ() >> 4) - this.chunkZ;
                return this.chunkArray[chunkArrayX][chunkArrayZ].getLightFor(lightType, pos);
            }
        }
        else
        {
            return lightType.defaultLightValue;
        }
    }

    public boolean isAirBlock(BlockPos pos)
    {
        return this.getBlockState(pos).getBlock().getMaterial() == Material.air;
    }

    public int getLightFor(EnumSkyBlock lightType, BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int chunkArrayX = (pos.getX() >> 4) - this.chunkX;
            int chunkArrayZ = (pos.getZ() >> 4) - this.chunkZ;
            return this.chunkArray[chunkArrayX][chunkArrayZ].getLightFor(lightType, pos);
        }
        else
        {
            return lightType.defaultLightValue;
        }
    }

    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        IBlockState blockState = this.getBlockState(pos);
        return blockState.getBlock().getStrongPower(this, pos, blockState, direction);
    }

    public WorldType getWorldType()
    {
        return this.worldObj.getWorldType();
    }
}
