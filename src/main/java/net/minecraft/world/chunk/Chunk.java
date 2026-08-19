package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.lighting.LightingEngine;
import net.minecraft.world.lighting.LightingHooks;
import net.minecraft.world.lighting.WorldChunkSlice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Chunk
{
    private static final Logger logger = LogManager.getLogger();
    private static final EnumFacing[] HORIZONTAL_FACINGS = EnumFacing.Plane.HORIZONTAL.facings();
    private final ExtendedBlockStorage[] storageArrays;
    private final byte[] blockBiomeArray;
    private final int[] precipitationHeightMap;
    private final boolean[] updateSkylightColumns;
    private boolean isChunkLoaded;
    private final World worldObj;
    private final int[] heightMap;
    public final int xPosition;
    public final int zPosition;
    private boolean isGapLightingUpdated;
    private final Map<BlockPos, TileEntity> chunkTileEntityMap;
    private final ClassInheritanceMultiMap<Entity>[] entityLists;
    private boolean isTerrainPopulated;
    private boolean isLightPopulated;
    private boolean isTicked;
    private boolean isModified;
    private boolean hasEntities;
    private long lastSaveTime;
    private int heightMapMinimum;
    private long inhabitedTime;
    private int queuedLightChecks;
    private ConcurrentLinkedQueue<BlockPos> tileEntityPosQueue;
    private final LightingEngine lightingEngine;
    private short[] neighborLightChecks;
    private boolean lightInitialized;

    public Chunk(World worldIn, int x, int z)
    {
        this.storageArrays = new ExtendedBlockStorage[16];
        this.blockBiomeArray = new byte[256];
        this.precipitationHeightMap = new int[256];
        this.updateSkylightColumns = new boolean[256];
        this.chunkTileEntityMap = Maps.<BlockPos, TileEntity>newHashMap();
        this.queuedLightChecks = 4096;
        this.tileEntityPosQueue = Queues.<BlockPos>newConcurrentLinkedQueue();
        this.entityLists = (ClassInheritanceMultiMap[])(new ClassInheritanceMultiMap[16]);
        this.worldObj = worldIn;
        this.xPosition = x;
        this.zPosition = z;
        this.heightMap = new int[256];

        for (int i = 0; i < this.entityLists.length; ++i)
        {
            this.entityLists[i] = new ClassInheritanceMultiMap(Entity.class);
        }

        Arrays.fill((int[])this.precipitationHeightMap, (int) - 999);
        Arrays.fill(this.blockBiomeArray, (byte) - 1);
        this.lightingEngine = worldIn.getLightingEngine();
    }

    public Chunk(World worldIn, ChunkPrimer primer, int x, int z)
    {
        this(worldIn, x, z);
        int i = 256;
        boolean flag = !worldIn.provider.getHasNoSky();

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                for (int l = 0; l < i; ++l)
                {
                    int intValue2 = j * i * 16 | k * i | l;
                    IBlockState iblockstate = primer.getBlockState(intValue2);

                    if (iblockstate.getBlock().getMaterial() != Material.air)
                    {
                        int secondIntValue = l >> 4;

                        if (this.storageArrays[secondIntValue] == null)
                        {
                            this.storageArrays[secondIntValue] = new ExtendedBlockStorage(secondIntValue << 4, flag);
                        }

                        this.storageArrays[secondIntValue].set(j, l & 15, k, iblockstate);
                    }
                }
            }
        }
    }

    public boolean isAtLocation(int x, int z)
    {
        return x == this.xPosition && z == this.zPosition;
    }

    public int getHeight(BlockPos pos)
    {
        return this.getHeightValue(pos.getX() & 15, pos.getZ() & 15);
    }

    public int getHeightValue(int x, int z)
    {
        return this.heightMap[z << 4 | x];
    }

    public int getTopFilledSegment()
    {
        for (int i = this.storageArrays.length - 1; i >= 0; --i)
        {
            if (this.storageArrays[i] != null)
            {
                return this.storageArrays[i].getYLocation();
            }
        }

        return 0;
    }

    public ExtendedBlockStorage[] getBlockStorageArray()
    {
        return this.storageArrays;
    }

    protected void generateHeightMap()
    {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                for (int l = i + 16; l > 0; --l)
                {
                    Block block = this.getBlock0(j, l - 1, k);

                    if (block.getLightOpacity() != 0)
                    {
                        this.heightMap[k << 4 | j] = l;

                        if (l < this.heightMapMinimum)
                        {
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }
            }
        }

        this.isModified = true;
    }

    public void generateSkylightMap()
    {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                this.precipitationHeightMap[j + (k << 4)] = -999;

                for (int l = i + 16; l > 0; --l)
                {
                    if (this.getBlockLightOpacity(j, l - 1, k) != 0)
                    {
                        this.heightMap[k << 4 | j] = l;

                        if (l < this.heightMapMinimum)
                        {
                            this.heightMapMinimum = l;
                        }

                        break;
                    }
                }

                if (!this.worldObj.provider.getHasNoSky())
                {
                    int thirdIntValue = 15;
                    int intValue2 = i + 16 - 1;

                    while (true)
                    {
                        int secondIntValue = this.getBlockLightOpacity(j, intValue2, k);

                        if (secondIntValue == 0 && thirdIntValue != 15)
                        {
                            secondIntValue = 1;
                        }

                        thirdIntValue -= secondIntValue;

                        if (thirdIntValue > 0)
                        {
                            ExtendedBlockStorage extendedBlockStorage = this.storageArrays[intValue2 >> 4];

                            if (extendedBlockStorage != null)
                            {
                                extendedBlockStorage.setExtSkylightValue(j, intValue2 & 15, k, thirdIntValue);
                                this.worldObj.notifyLightSet(new BlockPos((this.xPosition << 4) + j, intValue2, (this.zPosition << 4) + k));
                            }
                        }

                        --intValue2;

                        if (intValue2 <= 0 || thirdIntValue <= 0)
                        {
                            break;
                        }
                    }
                }
            }
        }

        this.isModified = true;
    }

    private void propagateSkylightOcclusion(int x, int z)
    {
        this.updateSkylightColumns[x + z * 16] = true;
        this.isGapLightingUpdated = true;
    }

    private void recheckGaps(boolean onlyOne)
    {
        this.worldObj.theProfiler.startSection("recheckGaps");
        WorldChunkSlice slice = new WorldChunkSlice(this.worldObj, this.xPosition, this.zPosition);

        if (this.worldObj.isAreaLoaded(new BlockPos(this.xPosition * 16 + 8, 0, this.zPosition * 16 + 8), 16))
        {
            for (int x = 0; x < 16; ++x)
            {
                for (int z = 0; z < 16; ++z)
                {
                    if (this.recheckGapsForColumn(slice, x, z) && onlyOne)
                    {
                        this.worldObj.theProfiler.endSection();
                        return;
                    }
                }
            }

            this.isGapLightingUpdated = false;
        }

        this.worldObj.theProfiler.endSection();
    }

    private boolean recheckGapsForColumn(WorldChunkSlice slice, int x, int z)
    {
        int columnIndex = x + z * 16;

        if (!this.updateSkylightColumns[columnIndex])
        {
            return false;
        }

        this.updateSkylightColumns[columnIndex] = false;
        int height = this.getHeightValue(x, z);
        int worldX = this.xPosition * 16 + x;
        int worldZ = this.zPosition * 16 + z;
        int lowestNeighborHeight = this.getLowestNeighborHeight(slice, worldX, worldZ);
        this.recheckNeighborHeights(slice, worldX, worldZ, height, lowestNeighborHeight);
        return true;
    }

    private int getLowestNeighborHeight(WorldChunkSlice slice, int x, int z)
    {
        int lowestHeight = Integer.MAX_VALUE;

        for (EnumFacing facing : HORIZONTAL_FACINGS)
        {
            int neighborX = x + facing.getFrontOffsetX();
            int neighborZ = z + facing.getFrontOffsetZ();
            Chunk chunk = slice.getChunkFromWorldCoords(neighborX, neighborZ);

            if (chunk != null)
            {
                lowestHeight = Math.min(lowestHeight, chunk.getLowestHeight());
            }
        }

        return lowestHeight;
    }

    private void recheckNeighborHeights(WorldChunkSlice slice, int x, int z, int height, int lowestNeighborHeight)
    {
        this.checkSkylightNeighborHeight(slice, x, z, lowestNeighborHeight);

        for (EnumFacing facing : HORIZONTAL_FACINGS)
        {
            this.checkSkylightNeighborHeight(slice, x + facing.getFrontOffsetX(), z + facing.getFrontOffsetZ(), height);
        }
    }

    private void checkSkylightNeighborHeight(WorldChunkSlice slice, int x, int z, int maxValue)
    {
        if (!slice.isLoaded(x, z, 16))
        {
            return;
        }

        Chunk chunk = slice.getChunkFromWorldCoords(x, z);

        if (chunk == null)
        {
            return;
        }

        int height = chunk.getHeightValue(x & 15, z & 15);

        if (height > maxValue)
        {
            this.updateSkylightNeighborHeight(slice, x, z, maxValue, height + 1);
        }
        else if (height < maxValue)
        {
            this.updateSkylightNeighborHeight(slice, x, z, height, maxValue + 1);
        }
    }

    private void updateSkylightNeighborHeight(WorldChunkSlice slice, int x, int z, int startY, int endY)
    {
        if (endY <= startY || !slice.isLoaded(x, z, 16))
        {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);

        for (int y = startY; y < endY; ++y)
        {
            this.worldObj.checkLightFor(EnumSkyBlock.SKY, pos.set(x, y, z));
        }

        this.isModified = true;
    }



    private void relightBlock(int x, int y, int z)
    {
        int oldHeight = this.heightMap[z << 4 | x];
        int newHeight = Math.max(y, oldHeight);

        while (newHeight > 0 && this.getBlockLightOpacity(x, newHeight - 1, z) == 0)
        {
            --newHeight;
        }

        if (newHeight != oldHeight)
        {
            int worldX = x + this.xPosition * 16;
            int worldZ = z + this.zPosition * 16;
            this.worldObj.markBlockRangeForRenderUpdate(worldX, Math.min(newHeight, oldHeight), worldZ, worldX, Math.max(newHeight, oldHeight), worldZ);
            this.heightMap[z << 4 | x] = newHeight;

            if (!this.worldObj.provider.getHasNoSky())
            {
                LightingHooks.relightSkylightColumn(this.worldObj, this, x, z, oldHeight, newHeight);
            }

            if (newHeight < this.heightMapMinimum)
            {
                this.heightMapMinimum = newHeight;
            }

            this.isModified = true;
        }
    }



    public int getBlockLightOpacity(BlockPos pos)
    {
        return this.getBlock(pos).getLightOpacity();
    }

    private int getBlockLightOpacity(int x, int y, int z)
    {
        return this.getBlock0(x, y, z).getLightOpacity();
    }

    private Block getBlock0(int x, int y, int z)
    {
        Block block = Blocks.air;

        if (y >= 0 && y >> 4 < this.storageArrays.length)
        {
            ExtendedBlockStorage extendedBlockStorage = this.storageArrays[y >> 4];

            if (extendedBlockStorage != null)
            {
                block = extendedBlockStorage.getBlockByExtId(x, y & 15, z);
            }
        }

        return block;
    }

    public Block getBlock(final int x, final int y, final int z)
    {
        try
        {
            return this.getBlock0(x & 15, y, z & 15);
        }
        catch (ReportedException reportedException)
        {
            CrashReportCategory crashReportCategory = reportedException.getCrashReport().makeCategory("Block being got");
            crashReportCategory.addCrashSectionCallable("Location", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return CrashReportCategory.getCoordinateInfo(new BlockPos(Chunk.this.xPosition * 16 + x, y, Chunk.this.zPosition * 16 + z));
                }
            });
            throw reportedException;
        }
    }

    public Block getBlock(final BlockPos pos)
    {
        try
        {
            return this.getBlock0(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
        }
        catch (ReportedException reportedException)
        {
            CrashReportCategory crashReportCategory = reportedException.getCrashReport().makeCategory("Block being got");
            crashReportCategory.addCrashSectionCallable("Location", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return CrashReportCategory.getCoordinateInfo(pos);
                }
            });
            throw reportedException;
        }
    }

    public IBlockState getBlockState(final BlockPos pos)
    {
        if (this.worldObj.getWorldType() == WorldType.DEBUG_WORLD)
        {
            IBlockState iblockstate = null;

            if (pos.getY() == 60)
            {
                iblockstate = Blocks.barrier.getDefaultState();
            }

            if (pos.getY() == 70)
            {
                iblockstate = ChunkProviderDebug.getDebugState(pos.getX(), pos.getZ());
            }

            return iblockstate == null ? Blocks.air.getDefaultState() : iblockstate;
        }
        else
        {
            int y = pos.getY();
            if (y >= 0 && y >> 4 < this.storageArrays.length)
            {
                ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];

                if (extendedblockstorage != null)
                {
                    return extendedblockstorage.get(pos.getX() & 15, y & 15, pos.getZ() & 15);
                }
            }

            return Blocks.air.getDefaultState();
        }
    }

    private int getBlockMetadata(int x, int y, int z)
    {
        if (y >> 4 >= this.storageArrays.length)
        {
            return 0;
        }
        else
        {
            ExtendedBlockStorage extendedBlockStorage = this.storageArrays[y >> 4];
            return extendedBlockStorage != null ? extendedBlockStorage.getExtBlockMetadata(x, y & 15, z) : 0;
        }
    }

    public int getBlockMetadata(BlockPos pos)
    {
        return this.getBlockMetadata(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
    }

    public IBlockState setBlockState(BlockPos pos, IBlockState state)
    {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1)
        {
            this.precipitationHeightMap[l] = -999;
        }

        int fifthIntValue = this.heightMap[l];
        IBlockState iblockstate = this.getBlockState(pos);

        if (iblockstate == state)
        {
            return null;
        }
        else
        {
            Block block = state.getBlock();
            Block block1 = iblockstate.getBlock();
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];

            if (extendedblockstorage == null)
            {
                if (block == Blocks.air)
                {
                    return null;
                }

                extendedblockstorage = this.storageArrays[j >> 4] = new ExtendedBlockStorage(j >> 4 << 4, !this.worldObj.provider.getHasNoSky());
                LightingHooks.initSkylightForSection(this.worldObj, this, extendedblockstorage);
            }

            extendedblockstorage.set(i, j & 15, k, state);

            if (block1 != block)
            {
                if (!this.worldObj.isRemote)
                {
                    block1.breakBlock(this.worldObj, pos, iblockstate);
                }
                else if (block1 instanceof ITileEntityProvider)
                {
                    this.worldObj.removeTileEntity(pos);
                }
            }

            if (extendedblockstorage.getBlockByExtId(i, j & 15, k) != block)
            {
                return null;
            }
            else
            {
                int intValue = block.getLightOpacity();
                int secondIntValue = block1.getLightOpacity();

                if (intValue > 0)
                {
                    if (j >= fifthIntValue)
                    {
                        this.relightBlock(i, j + 1, k);
                    }
                }
                else if (j == fifthIntValue - 1)
                {
                    this.relightBlock(i, j, k);
                }

                if (intValue != secondIntValue && (intValue < secondIntValue || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0))
                {
                    this.propagateSkylightOcclusion(i, k);
                }

                if (block1 instanceof ITileEntityProvider)
                {
                    TileEntity tileentity = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileentity != null)
                    {
                        tileentity.updateContainingBlockInfo();
                    }
                }

                if (!this.worldObj.isRemote && block1 != block)
                {
                    block.onBlockAdded(this.worldObj, pos, state);
                }

                if (block instanceof ITileEntityProvider)
                {
                    TileEntity tileentity1 = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);

                    if (tileentity1 == null)
                    {
                        tileentity1 = ((ITileEntityProvider)block).createNewTileEntity(this.worldObj, block.getMetaFromState(state));
                        this.worldObj.setTileEntity(pos, tileentity1);
                    }

                    if (tileentity1 != null)
                    {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                this.isModified = true;
                return iblockstate;
            }
        }
    }

    public int getLightFor(EnumSkyBlock lightType, BlockPos pos)
    {
        this.lightingEngine.processLightUpdatesForType(lightType);
        return this.getCachedLightFor(lightType, pos);
    }

    public int getCachedLightFor(EnumSkyBlock lightType, BlockPos pos)
    {
        int y = pos.getY();

        if (y < 0 || y >= 256)
        {
            return lightType.defaultLightValue;
        }

        int x = pos.getX() & 15;
        int z = pos.getZ() & 15;
        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null)
        {
            return lightType == EnumSkyBlock.SKY && !this.worldObj.provider.getHasNoSky() && this.canSeeSky(pos) ? EnumSkyBlock.SKY.defaultLightValue : 0;
        }

        if (lightType == EnumSkyBlock.SKY)
        {
            return this.worldObj.provider.getHasNoSky() ? 0 : storage.getExtSkylightValue(x, y & 15, z);
        }

        return lightType == EnumSkyBlock.BLOCK ? storage.getExtBlocklightValue(x, y & 15, z) : lightType.defaultLightValue;
    }

    public void setLightFor(EnumSkyBlock lightType, BlockPos pos, int value)
    {
        int y = pos.getY();

        if (y < 0 || y >= 256)
        {
            return;
        }

        int x = pos.getX() & 15;
        int z = pos.getZ() & 15;
        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null)
        {
            storage = this.storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !this.worldObj.provider.getHasNoSky());
            LightingHooks.initSkylightForSection(this.worldObj, this, storage);
        }

        this.isModified = true;

        if (lightType == EnumSkyBlock.SKY)
        {
            if (!this.worldObj.provider.getHasNoSky())
            {
                storage.setExtSkylightValue(x, y & 15, z, value);
            }
        }
        else if (lightType == EnumSkyBlock.BLOCK)
        {
            storage.setExtBlocklightValue(x, y & 15, z, value);
        }
    }

    public int getLightSubtracted(BlockPos pos, int amount)
    {
        this.lightingEngine.processLightUpdates();

        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;

        if (y < 0)
        {
            return 0;
        }

        if (y >= 256)
        {
            return this.worldObj.provider.getHasNoSky() ? 0 : Math.max(0, EnumSkyBlock.SKY.defaultLightValue - amount);
        }

        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null)
        {
            return !this.worldObj.provider.getHasNoSky() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
        }

        int skyLight = this.worldObj.provider.getHasNoSky() ? 0 : storage.getExtSkylightValue(x, y & 15, z) - amount;
        int blockLight = storage.getExtBlocklightValue(x, y & 15, z);
        return blockLight > skyLight ? blockLight : skyLight;
    }



    public void addEntity(Entity entityIn)
    {
        this.hasEntities = true;
        int i = MathHelper.floor_double(entityIn.posX / 16.0D);
        int j = MathHelper.floor_double(entityIn.posZ / 16.0D);

        if (i != this.xPosition || j != this.zPosition)
        {
            logger.warn("Wrong location! (" + i + ", " + j + ") should be (" + this.xPosition + ", " + this.zPosition + "), " + entityIn, new Object[] {entityIn});
            entityIn.setDead();
        }

        int k = MathHelper.floor_double(entityIn.posY / 16.0D);

        if (k < 0)
        {
            k = 0;
        }

        if (k >= this.entityLists.length)
        {
            k = this.entityLists.length - 1;
        }

        entityIn.addedToChunk = true;
        entityIn.chunkCoordX = this.xPosition;
        entityIn.chunkCoordY = k;
        entityIn.chunkCoordZ = this.zPosition;
        this.entityLists[k].add(entityIn);
    }

    public void removeEntity(Entity entityIn)
    {
        this.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
    }

    public void removeEntityAtIndex(Entity entityIn, int ySection)
    {
        if (ySection < 0)
        {
            ySection = 0;
        }

        if (ySection >= this.entityLists.length)
        {
            ySection = this.entityLists.length - 1;
        }

        this.entityLists[ySection].remove(entityIn);
    }

    public boolean canSeeSky(BlockPos pos)
    {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        return j >= this.heightMap[k << 4 | i];
    }

    private TileEntity createNewTileEntity(BlockPos pos)
    {
        Block block = this.getBlock(pos);
        return !block.hasTileEntity() ? null : ((ITileEntityProvider)block).createNewTileEntity(this.worldObj, this.getBlockMetadata(pos));
    }

    public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType createType)
    {
        TileEntity tileEntity = (TileEntity)this.chunkTileEntityMap.get(pos);

        if (tileEntity == null)
        {
            if (createType == Chunk.EnumCreateEntityType.IMMEDIATE)
            {
                tileEntity = this.createNewTileEntity(pos);
                this.worldObj.setTileEntity(pos, tileEntity);
            }
            else if (createType == Chunk.EnumCreateEntityType.QUEUED)
            {
                this.tileEntityPosQueue.add(pos.toImmutable());
            }
        }
        else if (tileEntity.isInvalid())
        {
            this.chunkTileEntityMap.remove(pos);
            return null;
        }

        return tileEntity;
    }

    public void addTileEntity(TileEntity tileEntityIn)
    {
        this.addTileEntity(tileEntityIn.getPos(), tileEntityIn);

        if (this.isChunkLoaded)
        {
            this.worldObj.addTileEntity(tileEntityIn);
        }
    }

    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn)
    {
        pos = pos.toImmutable();
        tileEntityIn.setWorldObj(this.worldObj);
        tileEntityIn.setPos(pos);

        if (this.getBlock(pos) instanceof ITileEntityProvider)
        {
            if (this.chunkTileEntityMap.containsKey(pos))
            {
                ((TileEntity)this.chunkTileEntityMap.get(pos)).invalidate();
            }

            tileEntityIn.validate();
            this.chunkTileEntityMap.put(pos, tileEntityIn);
        }
    }

    public void removeTileEntity(BlockPos pos)
    {
        if (this.isChunkLoaded)
        {
            TileEntity tileEntity = (TileEntity)this.chunkTileEntityMap.remove(pos);

            if (tileEntity != null)
            {
                tileEntity.invalidate();
            }
        }
    }

    public void onChunkLoad()
    {
        this.isChunkLoaded = true;
        this.worldObj.addTileEntities(this.chunkTileEntityMap.values());

        for (int i = 0; i < this.entityLists.length; ++i)
        {
            for (Entity entity : this.entityLists[i])
            {
                entity.onChunkLoad();
            }

            this.worldObj.loadEntities(this.entityLists[i]);
        }

        LightingHooks.scheduleRelightChecksForChunkBoundaries(this.worldObj, this);
    }

    public void onChunkUnload()
    {
        this.isChunkLoaded = false;

        for (TileEntity tileEntity : this.chunkTileEntityMap.values())
        {
            this.worldObj.markTileEntityForRemoval(tileEntity);
        }

        for (int i = 0; i < this.entityLists.length; ++i)
        {
            this.worldObj.unloadEntities(this.entityLists[i]);
        }
    }

    public void setChunkModified()
    {
        this.isModified = true;
    }

    public void getEntitiesWithinAABBForEntity(Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate <? super Entity > filter)
    {
        int i = MathHelper.floor_double((aabb.minY - 2.0D) / 16.0D);
        int j = MathHelper.floor_double((aabb.maxY + 2.0D) / 16.0D);
        i = MathHelper.clamp_int(i, 0, this.entityLists.length - 1);
        j = MathHelper.clamp_int(j, 0, this.entityLists.length - 1);

        for (int k = i; k <= j; ++k)
        {
            if (!this.entityLists[k].isEmpty())
            {
                for (Entity entity : this.entityLists[k])
                {
                    if (entity.getEntityBoundingBox().intersectsWith(aabb) && entity != entityIn)
                    {
                        if (filter == null || filter.apply(entity))
                        {
                            listToFill.add(entity);
                        }

                        Entity[] aentity = entity.getParts();

                        if (aentity != null)
                        {
                            for (int l = 0; l < aentity.length; ++l)
                            {
                                entity = aentity[l];

                                if (entity != entityIn && entity.getEntityBoundingBox().intersectsWith(aabb) && (filter == null || filter.apply(entity)))
                                {
                                    listToFill.add(entity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public <T extends Entity> void getEntitiesOfTypeWithinAAAB(Class <? extends T > entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate <? super T > filter)
    {
        int i = MathHelper.floor_double((aabb.minY - 2.0D) / 16.0D);
        int j = MathHelper.floor_double((aabb.maxY + 2.0D) / 16.0D);
        i = MathHelper.clamp_int(i, 0, this.entityLists.length - 1);
        j = MathHelper.clamp_int(j, 0, this.entityLists.length - 1);

        for (int k = i; k <= j; ++k)
        {
            for (T t : this.entityLists[k].getByClass(entityClass))
            {
                if (t.getEntityBoundingBox().intersectsWith(aabb) && (filter == null || filter.apply(t)))
                {
                    listToFill.add(t);
                }
            }
        }
    }

    public boolean needsSaving(boolean saveAll)
    {
        if (saveAll)
        {
            if (this.hasEntities && this.worldObj.getTotalWorldTime() != this.lastSaveTime || this.isModified)
            {
                return true;
            }
        }
        else if (this.hasEntities && this.worldObj.getTotalWorldTime() >= this.lastSaveTime + 600L)
        {
            return true;
        }

        return this.isModified;
    }

    public Random getRandomWithSeed(long seed)
    {
        return new Random(this.worldObj.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ seed);
    }

    public boolean isEmpty()
    {
        return false;
    }

    public void populateChunk(IChunkProvider chunkProvider, IChunkProvider chunkGenerator, int x, int z)
    {
        boolean flag = chunkProvider.chunkExists(x, z - 1);
        boolean flag1 = chunkProvider.chunkExists(x + 1, z);
        boolean flag2 = chunkProvider.chunkExists(x, z + 1);
        boolean flag3 = chunkProvider.chunkExists(x - 1, z);
        boolean flag4 = chunkProvider.chunkExists(x - 1, z - 1);
        boolean flag5 = chunkProvider.chunkExists(x + 1, z + 1);
        boolean flag6 = chunkProvider.chunkExists(x - 1, z + 1);
        boolean flag7 = chunkProvider.chunkExists(x + 1, z - 1);

        if (flag1 && flag2 && flag5)
        {
            if (!this.isTerrainPopulated)
            {
                chunkProvider.populate(chunkGenerator, x, z);
            }
            else
            {
                chunkProvider.populateChunk(chunkGenerator, this, x, z);
            }
        }

        if (flag3 && flag2 && flag6)
        {
            Chunk chunk = chunkProvider.provideChunk(x - 1, z);

            if (!chunk.isTerrainPopulated)
            {
                chunkProvider.populate(chunkGenerator, x - 1, z);
            }
            else
            {
                chunkProvider.populateChunk(chunkGenerator, chunk, x - 1, z);
            }
        }

        if (flag && flag1 && flag7)
        {
            Chunk chunk1 = chunkProvider.provideChunk(x, z - 1);

            if (!chunk1.isTerrainPopulated)
            {
                chunkProvider.populate(chunkGenerator, x, z - 1);
            }
            else
            {
                chunkProvider.populateChunk(chunkGenerator, chunk1, x, z - 1);
            }
        }

        if (flag4 && flag && flag3)
        {
            Chunk chunk2 = chunkProvider.provideChunk(x - 1, z - 1);

            if (!chunk2.isTerrainPopulated)
            {
                chunkProvider.populate(chunkGenerator, x - 1, z - 1);
            }
            else
            {
                chunkProvider.populateChunk(chunkGenerator, chunk2, x - 1, z - 1);
            }
        }
    }

    public BlockPos getPrecipitationHeight(BlockPos pos)
    {
        int i = pos.getX() & 15;
        int j = pos.getZ() & 15;
        int k = i | j << 4;
        int precipitationHeight = this.precipitationHeightMap[k];

        if (precipitationHeight == -999)
        {
            int l = this.getTopFilledSegment() + 15;
            BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos(pos.getX(), l, pos.getZ());
            int fourthIntValue = -1;

            while (blockpos.getY() > 0 && fourthIntValue == -1)
            {
                Block block = this.getBlock(blockpos);
                Material material = block.getMaterial();

                if (!material.blocksMovement() && !material.isLiquid())
                {
                    blockpos.set(blockpos.getX(), blockpos.getY() - 1, blockpos.getZ());
                }
                else
                {
                    fourthIntValue = blockpos.getY() + 1;
                }
            }

            this.precipitationHeightMap[k] = fourthIntValue;
            precipitationHeight = fourthIntValue;
        }

        return new BlockPos(pos.getX(), precipitationHeight, pos.getZ());
    }

    public void onTick(boolean skipRecheckGaps)
    {
        if (this.isGapLightingUpdated && !this.worldObj.provider.getHasNoSky() && !skipRecheckGaps)
        {
            this.recheckGaps(this.worldObj.isRemote);
        }

        this.isTicked = true;

        if (!this.isLightPopulated && this.isTerrainPopulated)
        {
            this.checkLight();
        }

        while (!this.tileEntityPosQueue.isEmpty())
        {
            BlockPos blockPos = (BlockPos)this.tileEntityPosQueue.poll();

            if (this.getTileEntity(blockPos, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlock(blockPos).hasTileEntity())
            {
                TileEntity tileEntity = this.createNewTileEntity(blockPos);
                this.worldObj.setTileEntity(blockPos, tileEntity);
                this.worldObj.markBlockRangeForRenderUpdate(blockPos, blockPos);
            }
        }
    }

    public boolean isPopulated()
    {
        return this.isTicked && this.isTerrainPopulated && this.isLightPopulated;
    }

    public ChunkCoordIntPair getChunkCoordIntPair()
    {
        return new ChunkCoordIntPair(this.xPosition, this.zPosition);
    }

    public boolean getAreLevelsEmpty(int startY, int endY)
    {
        if (startY < 0)
        {
            startY = 0;
        }

        if (endY >= 256)
        {
            endY = 255;
        }

        for (int i = startY; i <= endY; i += 16)
        {
            ExtendedBlockStorage extendedBlockStorage = this.storageArrays[i >> 4];

            if (extendedBlockStorage != null && !extendedBlockStorage.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays)
    {
        if (this.storageArrays.length != newStorageArrays.length)
        {
            logger.warn("Could not set level chunk sections, array length is " + newStorageArrays.length + " instead of " + this.storageArrays.length);
        }
        else
        {
            System.arraycopy(newStorageArrays, 0, this.storageArrays, 0, this.storageArrays.length);
        }
    }

    public void fillChunk(byte[] data, int availableSections, boolean loadBiomes)
    {
        int i = 0;
        boolean flag = !this.worldObj.provider.getHasNoSky();

        for (int j = 0; j < this.storageArrays.length; ++j)
        {
            if ((availableSections & 1 << j) != 0)
            {
                if (this.storageArrays[j] == null)
                {
                    this.storageArrays[j] = new ExtendedBlockStorage(j << 4, flag);
                }

                char[] achar = this.storageArrays[j].getData();

                for (int k = 0; k < achar.length; ++k)
                {
                    achar[k] = (char)((data[i + 1] & 255) << 8 | data[i] & 255);
                    i += 2;
                }
            }
            else if (loadBiomes && this.storageArrays[j] != null)
            {
                this.storageArrays[j] = null;
            }
        }

        for (int l = 0; l < this.storageArrays.length; ++l)
        {
            if ((availableSections & 1 << l) != 0 && this.storageArrays[l] != null)
            {
                NibbleArray nibbleArray = this.storageArrays[l].getBlocklightArray();
                System.arraycopy(data, i, nibbleArray.getData(), 0, nibbleArray.getData().length);
                i += nibbleArray.getData().length;
            }
        }

        if (flag)
        {
            for (int index = 0; index < this.storageArrays.length; ++index)
            {
                if ((availableSections & 1 << index) != 0 && this.storageArrays[index] != null)
                {
                    NibbleArray nibblearray1 = this.storageArrays[index].getSkylightArray();
                    System.arraycopy(data, i, nibblearray1.getData(), 0, nibblearray1.getData().length);
                    i += nibblearray1.getData().length;
                }
            }
        }

        if (loadBiomes)
        {
            System.arraycopy(data, i, this.blockBiomeArray, 0, this.blockBiomeArray.length);
            int count = i + this.blockBiomeArray.length;
        }

        for (int innerIndex2 = 0; innerIndex2 < this.storageArrays.length; ++innerIndex2)
        {
            if (this.storageArrays[innerIndex2] != null && (availableSections & 1 << innerIndex2) != 0)
            {
                this.storageArrays[innerIndex2].removeInvalidBlocks();
            }
        }

        this.isLightPopulated = true;
        this.lightInitialized = true;
        this.isTerrainPopulated = true;
        this.generateHeightMap();
        LightingHooks.scheduleRelightChecksForChunkBoundaries(this.worldObj, this);

        for (TileEntity tileEntity : this.chunkTileEntityMap.values())
        {
            tileEntity.updateContainingBlockInfo();
        }
    }

    public BiomeGenBase getBiome(BlockPos pos, WorldChunkManager chunkManager)
    {
        int i = pos.getX() & 15;
        int j = pos.getZ() & 15;
        int k = this.blockBiomeArray[j << 4 | i] & 255;

        if (k == 255)
        {
            BiomeGenBase biomeGenBase = chunkManager.getBiomeGenerator(pos, BiomeGenBase.plains);
            k = biomeGenBase.biomeID;
            this.blockBiomeArray[j << 4 | i] = (byte)(k & 255);
        }

        BiomeGenBase biomegenbase1 = BiomeGenBase.getBiome(k);
        return biomegenbase1 == null ? BiomeGenBase.plains : biomegenbase1;
    }

    public byte[] getBiomeArray()
    {
        return this.blockBiomeArray;
    }

    public void setBiomeArray(byte[] biomeArray)
    {
        if (this.blockBiomeArray.length != biomeArray.length)
        {
            logger.warn("Could not set level chunk biomes, array length is " + biomeArray.length + " instead of " + this.blockBiomeArray.length);
        }
        else
        {
            System.arraycopy(biomeArray, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
        }
    }

    public void resetRelightChecks()
    {
        this.queuedLightChecks = 0;
    }

    public void enqueueRelightChecks()
    {
        BlockPos blockpos = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);

        for (int i = 0; i < 8; ++i)
        {
            if (this.queuedLightChecks >= 4096)
            {
                return;
            }

            int j = this.queuedLightChecks % 16;
            int k = this.queuedLightChecks / 16 % 16;
            int l = this.queuedLightChecks / 256;
            ++this.queuedLightChecks;

            for (int intValue = 0; intValue < 16; ++intValue)
            {
                BlockPos blockpos1 = blockpos.add(k, (j << 4) + intValue, l);
                boolean flag = intValue == 0 || intValue == 15 || k == 0 || k == 15 || l == 0 || l == 15;

                if (this.storageArrays[j] == null && flag || this.storageArrays[j] != null && this.storageArrays[j].getBlockByExtId(k, intValue, l).getMaterial() == Material.air)
                {
                    for (EnumFacing enumfacing : EnumFacing.VALUES)
                    {
                        BlockPos blockpos2 = blockpos1.offset(enumfacing);

                        if (this.worldObj.getBlockState(blockpos2).getBlock().getLightValue() > 0)
                        {
                            this.worldObj.checkLight(blockpos2);
                        }
                    }

                    this.worldObj.checkLight(blockpos1);
                }
            }
        }
    }

    public void checkLight()
    {
        this.isTerrainPopulated = true;
        LightingHooks.checkChunkLighting(this, this.worldObj);
    }

    private void recheckAllGaps()
    {
        for (int i = 0; i < this.updateSkylightColumns.length; ++i)
        {
            this.updateSkylightColumns[i] = true;
        }

        this.recheckGaps(false);
    }

    private void checkLightSide(EnumFacing facing)
    {
        if (this.isTerrainPopulated)
        {
            if (facing == EnumFacing.EAST)
            {
                for (int i = 0; i < 16; ++i)
                {
                    this.checkLightColumn(15, i);
                }
            }
            else if (facing == EnumFacing.WEST)
            {
                for (int j = 0; j < 16; ++j)
                {
                    this.checkLightColumn(0, j);
                }
            }
            else if (facing == EnumFacing.SOUTH)
            {
                for (int k = 0; k < 16; ++k)
                {
                    this.checkLightColumn(k, 15);
                }
            }
            else if (facing == EnumFacing.NORTH)
            {
                for (int l = 0; l < 16; ++l)
                {
                    this.checkLightColumn(l, 0);
                }
            }
        }
    }

    private boolean checkLightColumn(int x, int z)
    {
        int i = this.getTopFilledSegment();
        boolean flag = false;
        boolean flag1 = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.xPosition << 4) + x, 0, (this.zPosition << 4) + z);

        for (int j = i + 16 - 1; j > this.worldObj.getSeaLevel() || j > 0 && !flag1; --j)
        {
            blockpos$mutableblockpos.set(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
            int k = this.getBlockLightOpacity(blockpos$mutableblockpos);

            if (k == 255 && blockpos$mutableblockpos.getY() < this.worldObj.getSeaLevel())
            {
                flag1 = true;
            }

            if (!flag && k > 0)
            {
                flag = true;
            }
            else if (flag && k == 0 && !this.worldObj.checkLight(blockpos$mutableblockpos))
            {
                return false;
            }
        }

        for (int l = blockpos$mutableblockpos.getY(); l > 0; --l)
        {
            blockpos$mutableblockpos.set(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());

            if (this.getBlock(blockpos$mutableblockpos).getLightValue() > 0)
            {
                this.worldObj.checkLight(blockpos$mutableblockpos);
            }
        }

        return true;
    }

    public boolean isLoaded()
    {
        return this.isChunkLoaded;
    }

    public void setChunkLoaded(boolean loaded)
    {
        this.isChunkLoaded = loaded;
    }

    public World getWorld()
    {
        return this.worldObj;
    }

    public int[] getHeightMap()
    {
        return this.heightMap;
    }

    public void setHeightMap(int[] newHeightMap)
    {
        if (this.heightMap.length != newHeightMap.length)
        {
            logger.warn("Could not set level chunk heightmap, array length is " + newHeightMap.length + " instead of " + this.heightMap.length);
        }
        else
        {
            System.arraycopy(newHeightMap, 0, this.heightMap, 0, this.heightMap.length);
        }
    }

    public Map<BlockPos, TileEntity> getTileEntityMap()
    {
        return this.chunkTileEntityMap;
    }

    public ClassInheritanceMultiMap<Entity>[] getEntityLists()
    {
        return this.entityLists;
    }

    public boolean isTerrainPopulated()
    {
        return this.isTerrainPopulated;
    }

    public void setTerrainPopulated(boolean terrainPopulated)
    {
        this.isTerrainPopulated = terrainPopulated;
    }

    public boolean isLightPopulated()
    {
        return this.isLightPopulated;
    }

    public void setLightPopulated(boolean lightPopulated)
    {
        this.isLightPopulated = lightPopulated;
    }

    public void setModified(boolean modified)
    {
        this.isModified = modified;
    }

    public void setHasEntities(boolean hasEntitiesIn)
    {
        this.hasEntities = hasEntitiesIn;
    }

    public void setLastSaveTime(long saveTime)
    {
        this.lastSaveTime = saveTime;
    }

    public int getLowestHeight()
    {
        return this.heightMapMinimum;
    }

    public long getInhabitedTime()
    {
        return this.inhabitedTime;
    }

    public void setInhabitedTime(long newInhabitedTime)
    {
        this.inhabitedTime = newInhabitedTime;
    }

    public LightingEngine getLightingEngine()
    {
        return this.lightingEngine;
    }

    public short[] getNeighborLightChecks()
    {
        return this.neighborLightChecks;
    }

    public void setNeighborLightChecks(short[] data)
    {
        this.neighborLightChecks = data;
    }

    public boolean isLightInitialized()
    {
        return this.lightInitialized;
    }

    public void setLightInitialized(boolean initialized)
    {
        this.lightInitialized = initialized;
    }

    public void setSkylightUpdatedPublic()
    {
        this.recheckAllGaps();
    }

    public static enum EnumCreateEntityType
    {
        IMMEDIATE,
        QUEUED,
        CHECK;
    }
}
