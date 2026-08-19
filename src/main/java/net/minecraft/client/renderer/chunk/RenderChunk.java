package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.optifine.BlockPosM;
import net.optifine.CustomBlockLayers;
import net.optifine.override.ChunkCacheOF;
import net.optifine.render.AabbFrame;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;

public class RenderChunk
{
    private final World world;
    private final RenderGlobal renderGlobal;
    public static int renderChunksUpdated;
    private BlockPos position;
    public CompiledChunk compiledChunk = CompiledChunk.DUMMY;
    private final ReentrantLock lockCompileTask = new ReentrantLock();
    private final ReentrantLock lockCompiledChunk = new ReentrantLock();
    private ChunkCompileTaskGenerator compileTask = null;
    private final Set<TileEntity> setTileEntities = Sets.<TileEntity>newHashSet();
    private final int index;
    private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final VertexBuffer[] vertexBuffers = new VertexBuffer[EnumWorldBlockLayer.VALUES.length];
    public AxisAlignedBB boundingBox;
    private int frameIndex = -1;
    private boolean needsUpdate = true;
    private EnumMap<EnumFacing, BlockPos> mapEnumFacing = null;
    private BlockPos[] positionOffsets16 = new BlockPos[EnumFacing.VALUES.length];
    public static final EnumWorldBlockLayer[] ENUM_WORLD_BLOCK_LAYERS = EnumWorldBlockLayer.VALUES;
    private final EnumWorldBlockLayer[] blockLayersSingle = new EnumWorldBlockLayer[1];
    private final boolean isMipmaps = Config.isMipmaps();
    private final boolean fixBlockLayer = true;
    private boolean playerUpdate = false;
    public int regionX;
    public int regionZ;
    private final RenderChunk[] renderChunksOffset16 = new RenderChunk[6];
    private boolean renderChunksOffset16Updated = false;
    private Chunk chunk;
    private RenderChunk[] renderChunkNeighbours = new RenderChunk[EnumFacing.VALUES.length];
    private RenderChunk[] renderChunkNeighboursValid = new RenderChunk[EnumFacing.VALUES.length];
    private boolean renderChunkNeighboursUpdated = false;
    private RenderGlobal.ContainerLocalRenderInformation renderInfo = new RenderGlobal.ContainerLocalRenderInformation(this, (EnumFacing)null, 0);
    private BlockPos boundingBoxParentPosition;
    public AabbFrame boundingBoxParent;

    public RenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos blockPosIn, int indexIn)
    {
        this.world = worldIn;
        this.renderGlobal = renderGlobalIn;
        this.index = indexIn;

        if (!blockPosIn.equals(this.getPosition()))
        {
            this.setPosition(blockPosIn);
        }

        if (OpenGlHelper.useVbo())
        {
            for (int layerIndex = 0; layerIndex < ENUM_WORLD_BLOCK_LAYERS.length; ++layerIndex)
            {
                this.vertexBuffers[layerIndex] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            }
        }
    }

    public boolean setFrameIndex(int frameIndexIn)
    {
        if (this.frameIndex == frameIndexIn)
        {
            return false;
        }
        else
        {
            this.frameIndex = frameIndexIn;
            return true;
        }
    }

    public VertexBuffer getVertexBufferByLayer(int layer)
    {
        return this.vertexBuffers[layer];
    }

    public void setPosition(BlockPos pos)
    {
        this.stopCompileTask();
        this.position = pos;
        int regionShift = 8;
        this.regionX = pos.getX() >> regionShift << regionShift;
        this.regionZ = pos.getZ() >> regionShift << regionShift;
        this.boundingBox = new AxisAlignedBB(pos, pos.add(16, 16, 16));
        int parentShift = 5;
        this.boundingBoxParentPosition = new BlockPos(pos.getX() >> parentShift << parentShift, pos.getY() >> parentShift << parentShift, pos.getZ() >> parentShift << parentShift);
        this.initModelviewMatrix();

        Arrays.fill(this.positionOffsets16, null);

        this.renderChunksOffset16Updated = false;
        this.renderChunkNeighboursUpdated = false;

        for (int neighbourIndex = 0; neighbourIndex < this.renderChunkNeighbours.length; ++neighbourIndex)
        {
            RenderChunk renderChunk = this.renderChunkNeighbours[neighbourIndex];

            if (renderChunk != null)
            {
                renderChunk.renderChunkNeighboursUpdated = false;
            }
        }

        this.chunk = null;
        this.boundingBoxParent = null;
    }

    public void resortTransparency(float x, float y, float z, ChunkCompileTaskGenerator generator)
    {
        CompiledChunk compiledChunk = generator.getCompiledChunk();

        if (compiledChunk.getState() != null && !compiledChunk.isLayerEmpty(EnumWorldBlockLayer.TRANSLUCENT))
        {
            WorldRenderer worldRenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT);
            this.preRenderBlocks(worldRenderer, this.position);
            worldRenderer.setVertexState(compiledChunk.getState());
            this.postRenderBlocks(EnumWorldBlockLayer.TRANSLUCENT, x, y, z, worldRenderer, compiledChunk);
        }
    }

    public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator)
    {
        CompiledChunk newCompiledChunk = new CompiledChunk();
        BlockPos chunkStart = new BlockPos(this.position);
        generator.getLock().lock();

        try
        {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING)
            {
                return;
            }

            generator.setCompiledChunk(newCompiledChunk);
        }
        finally
        {
            generator.getLock().unlock();
        }

        VisGraph visGraph = new VisGraph();
        HashSet<TileEntity> tileEntities = Sets.newHashSet();

        if (!this.isChunkRegionEmpty(chunkStart))
        {
            ++renderChunksUpdated;
            ChunkCacheOF chunkCache = this.makeChunkCacheOF(chunkStart);
            chunkCache.renderStart();
            boolean[] usedLayers = new boolean[ENUM_WORLD_BLOCK_LAYERS.length];
            BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            BlockPosM mutablePos = new BlockPosM(chunkStart.getX(), chunkStart.getY(), chunkStart.getZ(), 3);

            for (int blockIndex = 0; blockIndex < 4096; ++blockIndex)
            {
                mutablePos.setXyz(chunkStart.getX() + (blockIndex & 15), chunkStart.getY() + (blockIndex >> 4 & 15), chunkStart.getZ() + (blockIndex >> 8 & 15));
                IBlockState blockState = chunkCache.getBlockState(mutablePos);
                Block block = blockState.getBlock();

                if (block.isOpaqueCube())
                {
                    visGraph.markBlockOpaque(mutablePos);
                }

                
                EnumWorldBlockLayer[] renderLayers;

                
                renderLayers = this.blockLayersSingle;
                renderLayers[0] = block.getBlockLayer();
            

                for (int layerIndex = 0; layerIndex < renderLayers.length; ++layerIndex)
                {
                    EnumWorldBlockLayer renderLayer = renderLayers[layerIndex];

                    
                    
                    renderLayer = this.fixBlockLayer(blockState, renderLayer);
                    int renderLayerIndex = renderLayer.ordinal();

                    if (block.getRenderType() != -1)
                    {
                        WorldRenderer worldRenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(renderLayerIndex);
                        worldRenderer.setBlockLayer(renderLayer);
                        RenderEnv renderEnv = worldRenderer.getRenderEnv(blockState, mutablePos);
                        renderEnv.setRegionRenderCacheBuilder(generator.getRegionRenderCacheBuilder());

                        if (!newCompiledChunk.isLayerStarted(renderLayer))
                        {
                            newCompiledChunk.setLayerStarted(renderLayer);
                            this.preRenderBlocks(worldRenderer, chunkStart);
                        }

                        usedLayers[renderLayerIndex] |= blockRendererDispatcher.renderBlock(blockState, mutablePos, chunkCache, worldRenderer);

                        if (renderEnv.isOverlaysRendered())
                        {
                            this.postRenderOverlays(generator.getRegionRenderCacheBuilder(), newCompiledChunk, usedLayers);
                            renderEnv.setOverlaysRendered(false);
                        }
                    }
                }

                            }

            for (EnumWorldBlockLayer renderLayer : ENUM_WORLD_BLOCK_LAYERS)
            {
                if (usedLayers[renderLayer.ordinal()])
                {
                    newCompiledChunk.setLayerUsed(renderLayer);
                }

                if (newCompiledChunk.isLayerStarted(renderLayer))
                {
                    if (Config.isShaders())
                    {
                        SVertexBuilder.calcNormalChunkLayer(generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(renderLayer));
                    }

                    WorldRenderer worldRenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(renderLayer);
                    this.postRenderBlocks(renderLayer, x, y, z, worldRenderer, newCompiledChunk);

                    if (worldRenderer.animatedSprites != null)
                    {
                        newCompiledChunk.setAnimatedSprites(renderLayer, (BitSet)worldRenderer.animatedSprites.clone());
                    }
                }
                else
                {
                    newCompiledChunk.setAnimatedSprites(renderLayer, (BitSet)null);
                }
            }

            chunkCache.renderFinish();
        }

        newCompiledChunk.setVisibility(visGraph.computeVisibility());
        this.lockCompileTask.lock();

        try
        {
            Set<TileEntity> addedTileEntities = Sets.newHashSet(tileEntities);
            Set<TileEntity> removedTileEntities = Sets.newHashSet(this.setTileEntities);
            addedTileEntities.removeAll(this.setTileEntities);
            removedTileEntities.removeAll(tileEntities);
            this.setTileEntities.clear();
            this.setTileEntities.addAll(tileEntities);
            this.renderGlobal.updateTileEntities(removedTileEntities, addedTileEntities);
        }
        finally
        {
            this.lockCompileTask.unlock();
        }
    }

    protected void finishCompileTask()
    {
        this.lockCompileTask.lock();

        try
        {
            if (this.compileTask != null && this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE)
            {
                this.compileTask.finish();
                this.compileTask = null;
            }
        }
        finally
        {
            this.lockCompileTask.unlock();
        }
    }

    public ReentrantLock getLockCompileTask()
    {
        return this.lockCompileTask;
    }

    public ChunkCompileTaskGenerator makeCompileTaskChunk()
    {
        this.lockCompileTask.lock();
        ChunkCompileTaskGenerator chunkCompileTaskGenerator;

        try
        {
            this.finishCompileTask();
            this.compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK);
            chunkCompileTaskGenerator = this.compileTask;
        }
        finally
        {
            this.lockCompileTask.unlock();
        }

        return chunkCompileTaskGenerator;
    }

    public ChunkCompileTaskGenerator makeCompileTaskTransparency()
    {
        this.lockCompileTask.lock();
        ChunkCompileTaskGenerator transparencyTask;

        try
        {
            if (this.compileTask != null && this.compileTask.getStatus() == ChunkCompileTaskGenerator.Status.PENDING)
            {
                ChunkCompileTaskGenerator pendingTask = null;
                return pendingTask;
            }

            if (this.compileTask != null && this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE)
            {
                this.compileTask.finish();
                this.compileTask = null;
            }

            this.compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY);
            this.compileTask.setCompiledChunk(this.compiledChunk);
            ChunkCompileTaskGenerator compileTask = this.compileTask;
            transparencyTask = compileTask;
        }
        finally
        {
            this.lockCompileTask.unlock();
        }

        return transparencyTask;
    }

    private void preRenderBlocks(WorldRenderer worldRendererIn, BlockPos pos)
    {
        worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);

        if (Config.isRenderRegions())
        {
            int regionShift = 8;
            int regionX = pos.getX() >> regionShift << regionShift;
            int regionY = pos.getY() >> regionShift << regionShift;
            int regionZ = pos.getZ() >> regionShift << regionShift;
            regionX = this.regionX;
            regionZ = this.regionZ;
            worldRendererIn.setTranslation((double)(-regionX), (double)(-regionY), (double)(-regionZ));
        }
        else
        {
            worldRendererIn.setTranslation((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
        }
    }

    private void postRenderBlocks(EnumWorldBlockLayer layer, float x, float y, float z, WorldRenderer worldRendererIn, CompiledChunk compiledChunkIn)
    {
        if (layer == EnumWorldBlockLayer.TRANSLUCENT && !compiledChunkIn.isLayerEmpty(layer))
        {
            worldRendererIn.sortVertexData(x, y, z);
            compiledChunkIn.setState(worldRendererIn.getVertexState());
        }

        worldRendererIn.finishDrawing();
    }

    private void initModelviewMatrix()
    {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        float scale = 1.000001F;
        GlStateManager.translate(-8.0F, -8.0F, -8.0F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.getFloat(2982, this.modelviewMatrix);
        GlStateManager.popMatrix();
    }

    public void multModelviewMatrix()
    {
        GlStateManager.multMatrix(this.modelviewMatrix);
    }

    public CompiledChunk getCompiledChunk()
    {
        return this.compiledChunk;
    }

    public void setCompiledChunk(CompiledChunk compiledChunkIn)
    {
        this.lockCompiledChunk.lock();

        try
        {
            this.compiledChunk = compiledChunkIn;
        }
        finally
        {
            this.lockCompiledChunk.unlock();
        }
    }

    public void stopCompileTask()
    {
        this.finishCompileTask();
        this.compiledChunk = CompiledChunk.DUMMY;
    }

    public void deleteGlResources()
    {
        this.stopCompileTask();

        for (int layerIndex = 0; layerIndex < ENUM_WORLD_BLOCK_LAYERS.length; ++layerIndex)
        {
            if (this.vertexBuffers[layerIndex] != null)
            {
                this.vertexBuffers[layerIndex].deleteGlBuffers();
            }
        }
    }

    public BlockPos getPosition()
    {
        return this.position;
    }

    public void setNeedsUpdate(boolean needsUpdateIn)
    {
        this.needsUpdate = needsUpdateIn;

        if (needsUpdateIn)
        {
            if (this.isWorldPlayerUpdate())
            {
                this.playerUpdate = true;
            }
        }
        else
        {
            this.playerUpdate = false;
        }
    }

    public boolean isNeedsUpdate()
    {
        return this.needsUpdate;
    }

    public BlockPos getBlockPosOffset16(EnumFacing facing)
    {
        return this.getPositionOffset16(facing);
    }

    public BlockPos getPositionOffset16(EnumFacing facing)
    {
        int facingIndex = facing.getIndex();
        BlockPos blockPos = this.positionOffsets16[facingIndex];

        if (blockPos == null)
        {
            blockPos = this.getPosition().offset(facing, 16);
            this.positionOffsets16[facingIndex] = blockPos;
        }

        return blockPos;
    }

    private boolean isWorldPlayerUpdate()
    {
        if (this.world instanceof WorldClient)
        {
            WorldClient worldClient = (WorldClient)this.world;
            return worldClient.isPlayerUpdate();
        }
        else
        {
            return false;
        }
    }

    public boolean isPlayerUpdate()
    {
        return this.playerUpdate;
    }

    protected RegionRenderCache createRegionRenderCache(World world, BlockPos from, BlockPos to, int subChunkAmount)
    {
        return new RegionRenderCache(world, from, to, subChunkAmount);
    }

    private EnumWorldBlockLayer fixBlockLayer(IBlockState blockState, EnumWorldBlockLayer layer)
    {
        if (CustomBlockLayers.isActive())
        {
            EnumWorldBlockLayer customLayer = CustomBlockLayers.getRenderLayer(blockState);

            if (customLayer != null)
            {
                return customLayer;
            }
        }

        if (!this.fixBlockLayer)
        {
            return layer;
        }
        else
        {
            if (this.isMipmaps)
            {
                if (layer == EnumWorldBlockLayer.CUTOUT)
                {
                    Block block = blockState.getBlock();

                    if (block instanceof BlockRedstoneWire)
                    {
                        return layer;
                    }

                    if (block instanceof BlockCactus)
                    {
                        return layer;
                    }

                    return EnumWorldBlockLayer.CUTOUT_MIPPED;
                }
            }
            else if (layer == EnumWorldBlockLayer.CUTOUT_MIPPED)
            {
                return EnumWorldBlockLayer.CUTOUT;
            }

            return layer;
        }
    }

    private void postRenderOverlays(RegionRenderCacheBuilder regionRenderCacheBuilder, CompiledChunk compiledChunk, boolean[] usedLayers)
    {
        this.postRenderOverlay(EnumWorldBlockLayer.CUTOUT, regionRenderCacheBuilder, compiledChunk, usedLayers);
        this.postRenderOverlay(EnumWorldBlockLayer.CUTOUT_MIPPED, regionRenderCacheBuilder, compiledChunk, usedLayers);
        this.postRenderOverlay(EnumWorldBlockLayer.TRANSLUCENT, regionRenderCacheBuilder, compiledChunk, usedLayers);
    }

    private void postRenderOverlay(EnumWorldBlockLayer layer, RegionRenderCacheBuilder regionRenderCacheBuilder, CompiledChunk compiledChunk, boolean[] usedLayers)
    {
        WorldRenderer worldRenderer = regionRenderCacheBuilder.getWorldRendererByLayer(layer);

        if (worldRenderer.isDrawing())
        {
            compiledChunk.setLayerStarted(layer);
            usedLayers[layer.ordinal()] = true;
        }
    }

    private ChunkCacheOF makeChunkCacheOF(BlockPos chunkOrigin)
    {
        BlockPos cacheStart = chunkOrigin.add(-1, -1, -1);
        BlockPos cacheEnd = chunkOrigin.add(16, 16, 16);
        ChunkCache chunkCache = this.createRegionRenderCache(this.world, cacheStart, cacheEnd, 1);

        
        ChunkCacheOF chunkCacheOF = new ChunkCacheOF(chunkCache, cacheStart, cacheEnd, 1);
        return chunkCacheOF;
    }

    public RenderChunk getRenderChunkOffset16(ViewFrustum viewFrustum, EnumFacing facing)
    {
        if (!this.renderChunksOffset16Updated)
        {
            for (int facingIndex = 0; facingIndex < EnumFacing.VALUES.length; ++facingIndex)
            {
                EnumFacing offsetFacing = EnumFacing.VALUES[facingIndex];
                BlockPos blockPos = this.getBlockPosOffset16(offsetFacing);
                this.renderChunksOffset16[facingIndex] = viewFrustum.getRenderChunk(blockPos);
            }

            this.renderChunksOffset16Updated = true;
        }

        return this.renderChunksOffset16[facing.ordinal()];
    }

    public Chunk getChunk()
    {
        return this.getChunk(this.position);
    }

    private Chunk getChunk(BlockPos pos)
    {
        Chunk chunk = this.chunk;

        if (chunk != null && chunk.isLoaded())
        {
            return chunk;
        }
        else
        {
            chunk = this.world.getChunkFromBlockCoords(pos);
            this.chunk = chunk;
            return chunk;
        }
    }

    public boolean isChunkRegionEmpty()
    {
        return this.isChunkRegionEmpty(this.position);
    }

    private boolean isChunkRegionEmpty(BlockPos pos)
    {
        int minY = pos.getY();
        int maxY = minY + 15;
        return this.getChunk(pos).getAreLevelsEmpty(minY, maxY);
    }

    public void setRenderChunkNeighbour(EnumFacing facing, RenderChunk renderChunk)
    {
        this.renderChunkNeighbours[facing.ordinal()] = renderChunk;
        this.renderChunkNeighboursValid[facing.ordinal()] = renderChunk;
    }

    public RenderChunk getRenderChunkNeighbour(EnumFacing facing)
    {
        if (!this.renderChunkNeighboursUpdated)
        {
            this.updateRenderChunkNeighboursValid();
        }

        return this.renderChunkNeighboursValid[facing.ordinal()];
    }

    public RenderGlobal.ContainerLocalRenderInformation getRenderInfo()
    {
        return this.renderInfo;
    }

    private void updateRenderChunkNeighboursValid()
    {
        int chunkX = this.getPosition().getX();
        int chunkZ = this.getPosition().getZ();
        int northIndex = EnumFacing.NORTH.ordinal();
        int southIndex = EnumFacing.SOUTH.ordinal();
        int westIndex = EnumFacing.WEST.ordinal();
        int eastIndex = EnumFacing.EAST.ordinal();
        this.renderChunkNeighboursValid[northIndex] = this.renderChunkNeighbours[northIndex].getPosition().getZ() == chunkZ - 16 ? this.renderChunkNeighbours[northIndex] : null;
        this.renderChunkNeighboursValid[southIndex] = this.renderChunkNeighbours[southIndex].getPosition().getZ() == chunkZ + 16 ? this.renderChunkNeighbours[southIndex] : null;
        this.renderChunkNeighboursValid[westIndex] = this.renderChunkNeighbours[westIndex].getPosition().getX() == chunkX - 16 ? this.renderChunkNeighbours[westIndex] : null;
        this.renderChunkNeighboursValid[eastIndex] = this.renderChunkNeighbours[eastIndex].getPosition().getX() == chunkX + 16 ? this.renderChunkNeighbours[eastIndex] : null;
        this.renderChunkNeighboursUpdated = true;
    }

    public boolean isBoundingBoxInFrustum(ICamera camera, int frameCount)
    {
        return this.getBoundingBoxParent().isBoundingBoxInFrustumFully(camera, frameCount) ? true : camera.isBoundingBoxInFrustum(this.boundingBox);
    }

    public AabbFrame getBoundingBoxParent()
    {
        if (this.boundingBoxParent == null)
        {
            BlockPos blockPos = this.getPosition();
            int blockX = blockPos.getX();
            int blockY = blockPos.getY();
            int blockZ = blockPos.getZ();
            int parentShift = 5;
            BlockPos parentPosition = this.boundingBoxParentPosition;
            int parentX = parentPosition.getX();
            int parentY = parentPosition.getY();
            int parentZ = parentPosition.getZ();

            if (parentX != blockX || parentY != blockY || parentZ != blockZ)
            {
                AabbFrame aabbFrame = this.renderGlobal.getRenderChunk(parentPosition).getBoundingBoxParent();

                if (aabbFrame != null && aabbFrame.minX == (double)parentX && aabbFrame.minY == (double)parentY && aabbFrame.minZ == (double)parentZ)
                {
                    this.boundingBoxParent = aabbFrame;
                }
            }

            if (this.boundingBoxParent == null)
            {
                int parentSize = 1 << parentShift;
                this.boundingBoxParent = new AabbFrame((double)parentX, (double)parentY, (double)parentZ, (double)(parentX + parentSize), (double)(parentY + parentSize), (double)(parentZ + parentSize));
            }
        }

        return this.boundingBoxParent;
    }

    public String toString()
    {
        return "pos: " + this.getPosition() + ", frameIndex: " + this.frameIndex;
    }
}
