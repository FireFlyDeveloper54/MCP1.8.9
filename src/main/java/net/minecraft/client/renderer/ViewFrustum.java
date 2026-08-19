package net.minecraft.client.renderer;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.optifine.render.VboRegion;

public class ViewFrustum
{
    protected final RenderGlobal renderGlobal;
    protected final World world;
    protected int countChunksY;
    protected int countChunksX;
    protected int countChunksZ;
    public RenderChunk[] renderChunks;
    private Map<ChunkCoordIntPair, VboRegion[]> mapVboRegions = new HashMap<ChunkCoordIntPair, VboRegion[]>();

    public ViewFrustum(World worldIn, int renderDistanceChunks, RenderGlobal renderGlobalIn, IRenderChunkFactory renderChunkFactory)
    {
        this.renderGlobal = renderGlobalIn;
        this.world = worldIn;
        this.setCountChunksXYZ(renderDistanceChunks);
        this.createRenderChunks(renderChunkFactory);
    }

    protected void createRenderChunks(IRenderChunkFactory renderChunkFactory)
    {
        int totalChunks = this.countChunksX * this.countChunksY * this.countChunksZ;
        this.renderChunks = new RenderChunk[totalChunks];
        int renderChunkId = 0;

        for (int xIndex = 0; xIndex < this.countChunksX; ++xIndex)
        {
            for (int yIndex = 0; yIndex < this.countChunksY; ++yIndex)
            {
                for (int zIndex = 0; zIndex < this.countChunksZ; ++zIndex)
                {
                    int renderChunkIndex = (zIndex * this.countChunksY + yIndex) * this.countChunksX + xIndex;
                    BlockPos chunkPos = new BlockPos(xIndex * 16, yIndex * 16, zIndex * 16);
                    this.renderChunks[renderChunkIndex] = renderChunkFactory.makeRenderChunk(this.world, this.renderGlobal, chunkPos, renderChunkId++);

                    if (Config.isVbo() && Config.isRenderRegions())
                    {
                        this.updateVboRegion(this.renderChunks[renderChunkIndex]);
                    }
                }
            }
        }

        for (int renderChunkIndex = 0; renderChunkIndex < this.renderChunks.length; ++renderChunkIndex)
        {
            RenderChunk renderChunk = this.renderChunks[renderChunkIndex];

            for (int facingIndex = 0; facingIndex < EnumFacing.VALUES.length; ++facingIndex)
            {
                EnumFacing facing = EnumFacing.VALUES[facingIndex];
                BlockPos neighborPos = renderChunk.getBlockPosOffset16(facing);
                RenderChunk neighborChunk = this.getRenderChunk(neighborPos);
                renderChunk.setRenderChunkNeighbour(facing, neighborChunk);
            }
        }
    }

    public void deleteGlResources()
    {
        for (RenderChunk renderChunk : this.renderChunks)
        {
            renderChunk.deleteGlResources();
        }

        this.deleteVboRegions();
    }

    protected void setCountChunksXYZ(int renderDistanceChunks)
    {
        int horizontalChunkCount = renderDistanceChunks * 2 + 1;
        this.countChunksX = horizontalChunkCount;
        this.countChunksY = 16;
        this.countChunksZ = horizontalChunkCount;
    }

    public void updateChunkPositions(double viewEntityX, double viewEntityZ)
    {
        int viewBlockX = MathHelper.floor_double(viewEntityX) - 8;
        int viewBlockZ = MathHelper.floor_double(viewEntityZ) - 8;
        int gridSize = this.countChunksX * 16;

        for (int xIndex = 0; xIndex < this.countChunksX; ++xIndex)
        {
            int chunkX = this.getBaseCoordinate(viewBlockX, gridSize, xIndex);

            for (int zIndex = 0; zIndex < this.countChunksZ; ++zIndex)
            {
                int chunkZ = this.getBaseCoordinate(viewBlockZ, gridSize, zIndex);

                for (int yIndex = 0; yIndex < this.countChunksY; ++yIndex)
                {
                    int chunkY = yIndex * 16;
                    RenderChunk renderChunk = this.renderChunks[(zIndex * this.countChunksY + yIndex) * this.countChunksX + xIndex];
                    BlockPos currentPos = renderChunk.getPosition();

                    if (currentPos.getX() != chunkX || currentPos.getY() != chunkY || currentPos.getZ() != chunkZ)
                    {
                        BlockPos newPos = new BlockPos(chunkX, chunkY, chunkZ);

                        if (!newPos.equals(renderChunk.getPosition()))
                        {
                            renderChunk.setPosition(newPos);
                        }
                    }
                }
            }
        }
    }

    private int getBaseCoordinate(int viewCoordinate, int gridSize, int chunkIndex)
    {
        int chunkCoordinate = chunkIndex * 16;
        int wrappedOffset = chunkCoordinate - viewCoordinate + gridSize / 2;

        if (wrappedOffset < 0)
        {
            wrappedOffset -= gridSize - 1;
        }

        return chunkCoordinate - wrappedOffset / gridSize * gridSize;
    }

    public void markBlocksForUpdate(int fromX, int fromY, int fromZ, int toX, int toY, int toZ)
    {
        int fromChunkX = MathHelper.bucketInt(fromX, 16);
        int fromChunkY = MathHelper.bucketInt(fromY, 16);
        int fromChunkZ = MathHelper.bucketInt(fromZ, 16);
        int toChunkX = MathHelper.bucketInt(toX, 16);
        int toChunkY = MathHelper.bucketInt(toY, 16);
        int toChunkZ = MathHelper.bucketInt(toZ, 16);

        for (int chunkX = fromChunkX; chunkX <= toChunkX; ++chunkX)
        {
            int xIndex = chunkX % this.countChunksX;

            if (xIndex < 0)
            {
                xIndex += this.countChunksX;
            }

            for (int chunkY = fromChunkY; chunkY <= toChunkY; ++chunkY)
            {
                int yIndex = chunkY % this.countChunksY;

                if (yIndex < 0)
                {
                    yIndex += this.countChunksY;
                }

                for (int chunkZ = fromChunkZ; chunkZ <= toChunkZ; ++chunkZ)
                {
                    int zIndex = chunkZ % this.countChunksZ;

                    if (zIndex < 0)
                    {
                        zIndex += this.countChunksZ;
                    }

                    int renderChunkIndex = (zIndex * this.countChunksY + yIndex) * this.countChunksX + xIndex;
                    RenderChunk renderChunk = this.renderChunks[renderChunkIndex];
                    renderChunk.setNeedsUpdate(true);
                }
            }
        }
    }

    public RenderChunk getRenderChunk(BlockPos pos)
    {
        int chunkX = pos.getX() >> 4;
        int chunkY = pos.getY() >> 4;
        int chunkZ = pos.getZ() >> 4;

        if (chunkY >= 0 && chunkY < this.countChunksY)
        {
            chunkX = chunkX % this.countChunksX;

            if (chunkX < 0)
            {
                chunkX += this.countChunksX;
            }

            chunkZ = chunkZ % this.countChunksZ;

            if (chunkZ < 0)
            {
                chunkZ += this.countChunksZ;
            }

            int renderChunkIndex = (chunkZ * this.countChunksY + chunkY) * this.countChunksX + chunkX;
            return this.renderChunks[renderChunkIndex];
        }
        else
        {
            return null;
        }
    }

    private void updateVboRegion(RenderChunk renderChunk)
    {
        BlockPos blockPos = renderChunk.getPosition();
        int regionX = blockPos.getX() >> 8 << 8;
        int regionZ = blockPos.getZ() >> 8 << 8;
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(regionX, regionZ);
        EnumWorldBlockLayer[] blockLayers = RenderChunk.ENUM_WORLD_BLOCK_LAYERS;
        VboRegion[] vboRegions = this.mapVboRegions.get(chunkCoordIntPair);

        if (vboRegions == null)
        {
            vboRegions = new VboRegion[blockLayers.length];

            for (int layerIndex = 0; layerIndex < blockLayers.length; ++layerIndex)
            {
                vboRegions[layerIndex] = new VboRegion(blockLayers[layerIndex]);
            }

            this.mapVboRegions.put(chunkCoordIntPair, vboRegions);
        }

        for (int layerIndex = 0; layerIndex < blockLayers.length; ++layerIndex)
        {
            VboRegion vboRegion = vboRegions[layerIndex];

            if (vboRegion != null)
            {
                renderChunk.getVertexBufferByLayer(layerIndex).setVboRegion(vboRegion);
            }
        }
    }

    public void deleteVboRegions()
    {
        for (ChunkCoordIntPair chunkCoordIntPair : this.mapVboRegions.keySet())
        {
            VboRegion[] vboRegions = this.mapVboRegions.get(chunkCoordIntPair);

            for (int layerIndex = 0; layerIndex < vboRegions.length; ++layerIndex)
            {
                VboRegion vboRegion = vboRegions[layerIndex];

                if (vboRegion != null)
                {
                    vboRegion.deleteGlBuffers();
                }

                vboRegions[layerIndex] = null;
            }
        }

        this.mapVboRegions.clear();
    }
}
