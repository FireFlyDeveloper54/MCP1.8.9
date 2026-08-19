package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.lighting.LightingEngine;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;

public abstract class World implements IBlockAccess
{
    private int seaLevel = 63;
    protected boolean scheduledUpdatesAreImmediate;
    public final List<Entity> loadedEntityList = Lists.<Entity>newArrayList();
    protected final List<Entity> unloadedEntityList = Lists.<Entity>newArrayList();
    public final List<TileEntity> loadedTileEntityList = Lists.<TileEntity>newArrayList();
    public final List<TileEntity> tickableTileEntities = Lists.<TileEntity>newArrayList();
    private final List<TileEntity> addedTileEntityList = Lists.<TileEntity>newArrayList();
    private final List<TileEntity> tileEntitiesToBeRemoved = Lists.<TileEntity>newArrayList();
    public final List<EntityPlayer> playerEntities = Lists.<EntityPlayer>newArrayList();
    public final List<Entity> weatherEffects = Lists.<Entity>newArrayList();
    protected final IntHashMap<Entity> entitiesById = new IntHashMap();
    private long cloudColour = 16777215L;
    private int skylightSubtracted;
    protected int updateLCG = (new Random()).nextInt();
    protected final int DIST_HASH_MAGIC = 1013904223;
    protected float prevRainingStrength;
    protected float rainingStrength;
    protected float prevThunderingStrength;
    protected float thunderingStrength;
    private int lastLightningBolt;
    public final Random rand = new Random();
    public final WorldProvider provider;
    protected List<IWorldAccess> worldAccesses = Lists.<IWorldAccess>newArrayList();
    protected IChunkProvider chunkProvider;
    protected final ISaveHandler saveHandler;
    protected WorldInfo worldInfo;
    protected boolean findingSpawnPoint;
    protected MapStorage mapStorage;
    protected VillageCollection villageCollectionObj;
    public final Profiler theProfiler;
    private final Calendar theCalendar = Calendar.getInstance();
    protected Scoreboard worldScoreboard = new Scoreboard();
    public final boolean isRemote;
    protected Set<ChunkCoordIntPair> activeChunkSet = Sets.<ChunkCoordIntPair>newHashSet();
    private int ambientTickCountdown;
    protected boolean spawnHostileMobs;
    protected boolean spawnPeacefulMobs;
    private boolean processingLoadedTiles;
    private final WorldBorder worldBorder;
    private final LightingEngine lightingEngine;

    protected World(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client)
    {
        this.ambientTickCountdown = this.rand.nextInt(12000);
        this.spawnHostileMobs = true;
        this.spawnPeacefulMobs = true;
        this.saveHandler = saveHandlerIn;
        this.theProfiler = profilerIn;
        this.worldInfo = info;
        this.provider = providerIn;
        this.isRemote = client;
        this.worldBorder = providerIn.getWorldBorder();
        this.lightingEngine = new LightingEngine(this);
    }

    public LightingEngine getLightingEngine()
    {
        return this.lightingEngine;
    }

    public World init()
    {
        return this;
    }

    public BiomeGenBase getBiomeGenForCoords(final BlockPos pos)
    {
        if (this.isBlockLoaded(pos))
        {
            Chunk chunk = this.getChunkFromBlockCoords(pos);

            try
            {
                return chunk.getBiome(pos, this.provider.getWorldChunkManager());
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Getting biome");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Coordinates of biome request");
                crashReportCategory.addCrashSectionCallable("Location", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return CrashReportCategory.getCoordinateInfo(pos);
                    }
                });
                throw new ReportedException(crashReport);
            }
        }
        else
        {
            return this.provider.getWorldChunkManager().getBiomeGenerator(pos, BiomeGenBase.plains);
        }
    }

    public WorldChunkManager getWorldChunkManager()
    {
        return this.provider.getWorldChunkManager();
    }

    protected abstract IChunkProvider createChunkProvider();

    public void initialize(WorldSettings settings)
    {
        this.worldInfo.setServerInitialized(true);
    }

    public void setInitialSpawnLocation()
    {
        this.setSpawnPoint(new BlockPos(8, 64, 8));
    }

    public Block getGroundAboveSeaLevel(BlockPos pos)
    {
        int x = pos.getX();
        int y = this.getSeaLevel();
        int z = pos.getZ();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(x, y + 1, z);

        while (!this.isAirBlock(blockPos))
        {
            blockPos.set(x, ++y + 1, z);
        }

        blockPos.set(x, y, z);
        return this.getBlockState(blockPos).getBlock();
    }

    private boolean isValid(BlockPos pos)
    {
        return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 && pos.getY() >= 0 && pos.getY() < 256;
    }

    public boolean isAirBlock(BlockPos pos)
    {
        return this.getBlockState(pos).getBlock().getMaterial() == Material.air;
    }

    public boolean isBlockLoaded(BlockPos pos)
    {
        return this.isBlockLoaded(pos, true);
    }

    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty)
    {
        return !this.isValid(pos) ? false : this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, allowEmpty);
    }

    public boolean isAreaLoaded(BlockPos center, int radius)
    {
        return this.isAreaLoaded(center, radius, true);
    }

    public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty)
    {
        return this.isAreaLoaded(center.getX() - radius, center.getY() - radius, center.getZ() - radius, center.getX() + radius, center.getY() + radius, center.getZ() + radius, allowEmpty);
    }

    public boolean isAreaLoaded(BlockPos from, BlockPos to)
    {
        return this.isAreaLoaded(from, to, true);
    }

    public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty)
    {
        return this.isAreaLoaded(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), allowEmpty);
    }

    public boolean isAreaLoaded(StructureBoundingBox box)
    {
        return this.isAreaLoaded(box, true);
    }

    public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty)
    {
        return this.isAreaLoaded(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, allowEmpty);
    }

    private boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty)
    {
        if (yEnd >= 0 && yStart < 256)
        {
            xStart = xStart >> 4;
            zStart = zStart >> 4;
            xEnd = xEnd >> 4;
            zEnd = zEnd >> 4;

            for (int i = xStart; i <= xEnd; ++i)
            {
                for (int j = zStart; j <= zEnd; ++j)
                {
                    if (!this.isChunkLoaded(i, j, allowEmpty))
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
    {
        return this.chunkProvider.chunkExists(x, z) && (allowEmpty || !this.chunkProvider.provideChunk(x, z).isEmpty());
    }

    public Chunk getChunkFromBlockCoords(BlockPos pos)
    {
        return this.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ)
    {
        return this.chunkProvider.provideChunk(chunkX, chunkZ);
    }

    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags)
    {
        if (!this.isValid(pos))
        {
            return false;
        }
        else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            return false;
        }
        else
        {
            Chunk chunk = this.getChunkFromBlockCoords(pos);
            Block block = newState.getBlock();
            IBlockState iblockstate = chunk.setBlockState(pos, newState);

            if (iblockstate == null)
            {
                return false;
            }
            else
            {
                Block block1 = iblockstate.getBlock();

                if (block.getLightOpacity() != block1.getLightOpacity() || block.getLightValue() != block1.getLightValue())
                {
                    this.theProfiler.startSection("checkLight");
                    this.checkLight(pos);
                    this.theProfiler.endSection();
                }

                if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && chunk.isPopulated())
                {
                    this.markBlockForUpdate(pos);
                }

                if (!this.isRemote && (flags & 1) != 0)
                {
                    this.notifyNeighborsRespectDebug(pos, iblockstate.getBlock());

                    if (block.hasComparatorInputOverride())
                    {
                        this.updateComparatorOutputLevel(pos, block);
                    }
                }

                return true;
            }
        }
    }

    public boolean setBlockToAir(BlockPos pos)
    {
        return this.setBlockState(pos, Blocks.air.getDefaultState(), 3);
    }

    public boolean destroyBlock(BlockPos pos, boolean dropBlock)
    {
        IBlockState iblockstate = this.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block.getMaterial() == Material.air)
        {
            return false;
        }
        else
        {
            this.playAuxSFX(2001, pos, Block.getStateId(iblockstate));

            if (dropBlock)
            {
                block.dropBlockAsItem(this, pos, iblockstate, 0);
            }

            return this.setBlockState(pos, Blocks.air.getDefaultState(), 3);
        }
    }

    public boolean setBlockState(BlockPos pos, IBlockState state)
    {
        return this.setBlockState(pos, state, 3);
    }

    public void markBlockForUpdate(BlockPos pos)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).markBlockForUpdate(pos);
        }
    }

    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType)
    {
        if (this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD)
        {
            this.notifyNeighborsOfStateChange(pos, blockType);
        }
    }

    public void markBlocksDirtyVertical(int twentyNinthIntValue, int number35IntValue, int number32IntValue, int number38IntValue)
    {
        if (number32IntValue > number38IntValue)
        {
            int i = number38IntValue;
            number38IntValue = number32IntValue;
            number32IntValue = i;
        }

        if (!this.provider.getHasNoSky())
        {
            for (int j = number32IntValue; j <= number38IntValue; ++j)
            {
                this.checkLightFor(EnumSkyBlock.SKY, new BlockPos(twentyNinthIntValue, j, number35IntValue));
            }
        }

        this.markBlockRangeForRenderUpdate(twentyNinthIntValue, number32IntValue, number35IntValue, twentyNinthIntValue, number38IntValue, number35IntValue);
    }

    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax)
    {
        this.markBlockRangeForRenderUpdate(rangeMin.getX(), rangeMin.getY(), rangeMin.getZ(), rangeMax.getX(), rangeMax.getY(), rangeMax.getZ());
    }

    public void markBlockRangeForRenderUpdate(int thirtiethIntValue, int number33IntValue, int number36IntValue, int number31IntValue, int number34IntValue, int number37IntValue)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).markBlockRangeForRenderUpdate(thirtiethIntValue, number33IntValue, number36IntValue, number31IntValue, number34IntValue, number37IntValue);
        }
    }

    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType)
    {
        this.notifyBlockOfStateChange(pos.west(), blockType);
        this.notifyBlockOfStateChange(pos.east(), blockType);
        this.notifyBlockOfStateChange(pos.down(), blockType);
        this.notifyBlockOfStateChange(pos.up(), blockType);
        this.notifyBlockOfStateChange(pos.north(), blockType);
        this.notifyBlockOfStateChange(pos.south(), blockType);
    }

    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide)
    {
        if (skipSide != EnumFacing.WEST)
        {
            this.notifyBlockOfStateChange(pos.west(), blockType);
        }

        if (skipSide != EnumFacing.EAST)
        {
            this.notifyBlockOfStateChange(pos.east(), blockType);
        }

        if (skipSide != EnumFacing.DOWN)
        {
            this.notifyBlockOfStateChange(pos.down(), blockType);
        }

        if (skipSide != EnumFacing.UP)
        {
            this.notifyBlockOfStateChange(pos.up(), blockType);
        }

        if (skipSide != EnumFacing.NORTH)
        {
            this.notifyBlockOfStateChange(pos.north(), blockType);
        }

        if (skipSide != EnumFacing.SOUTH)
        {
            this.notifyBlockOfStateChange(pos.south(), blockType);
        }
    }

    public void notifyBlockOfStateChange(BlockPos pos, final Block blockIn)
    {
        if (!this.isRemote)
        {
            IBlockState iblockstate = this.getBlockState(pos);

            try
            {
                iblockstate.getBlock().onNeighborBlockChange(this, pos, iblockstate, blockIn);
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Block being updated");
                crashReportCategory.addCrashSectionCallable("Source block type", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        try
                        {
                            return "ID #" + Block.getIdFromBlock(blockIn) + " (" + blockIn.getUnlocalizedName() + " // " + blockIn.getClass().getCanonicalName() + ")";
                        }
                        catch (Throwable exception)
                        {
                            return "ID #" + Block.getIdFromBlock(blockIn);
                        }
                    }
                });
                CrashReportCategory.addBlockInfo(crashReportCategory, pos, iblockstate);
                throw new ReportedException(crashReport);
            }
        }
    }

    public boolean isBlockTickPending(BlockPos pos, Block blockType)
    {
        return false;
    }

    public boolean canSeeSky(BlockPos pos)
    {
        return this.getChunkFromBlockCoords(pos).canSeeSky(pos);
    }

    public boolean canBlockSeeSky(BlockPos pos)
    {
        if (pos.getY() >= this.getSeaLevel())
        {
            return this.canSeeSky(pos);
        }
        else
        {
            int x = pos.getX();
            int z = pos.getZ();
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(x, this.getSeaLevel(), z);

            if (!this.canSeeSky(blockPos))
            {
                return false;
            }
            else
            {
                for (int y = this.getSeaLevel() - 1; y > pos.getY(); --y)
                {
                    blockPos.set(x, y, z);
                    Block block = this.getBlockState(blockPos).getBlock();

                    if (block.getLightOpacity() > 0 && !block.getMaterial().isLiquid())
                    {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public int getLight(BlockPos pos)
    {
        if (pos.getY() < 0)
        {
            return 0;
        }
        else
        {
            if (pos.getY() >= 256)
            {
                pos = new BlockPos(pos.getX(), 255, pos.getZ());
            }

            return this.getChunkFromBlockCoords(pos).getLightSubtracted(pos, 0);
        }
    }

    public int getLightFromNeighbors(BlockPos pos)
    {
        return this.getLight(pos, true);
    }

    public int getLight(BlockPos pos, boolean checkNeighbors)
    {
        if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000)
        {
            if (checkNeighbors && this.getBlockState(pos).getBlock().getUseNeighborBrightness())
            {
                int intValue = this.getLight(pos.up(), false);
                int i = this.getLight(pos.east(), false);
                int j = this.getLight(pos.west(), false);
                int k = this.getLight(pos.south(), false);
                int l = this.getLight(pos.north(), false);

                if (i > intValue)
                {
                    intValue = i;
                }

                if (j > intValue)
                {
                    intValue = j;
                }

                if (k > intValue)
                {
                    intValue = k;
                }

                if (l > intValue)
                {
                    intValue = l;
                }

                return intValue;
            }
            else if (pos.getY() < 0)
            {
                return 0;
            }
            else
            {
                if (pos.getY() >= 256)
                {
                    pos = new BlockPos(pos.getX(), 255, pos.getZ());
                }

                Chunk chunk = this.getChunkFromBlockCoords(pos);
                return chunk.getLightSubtracted(pos, this.skylightSubtracted);
            }
        }
        else
        {
            return 15;
        }
    }

    public BlockPos getHeight(BlockPos pos)
    {
        int i;

        if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000)
        {
            if (this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, true))
            {
                i = this.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4).getHeightValue(pos.getX() & 15, pos.getZ() & 15);
            }
            else
            {
                i = 0;
            }
        }
        else
        {
            i = this.getSeaLevel() + 1;
        }

        return new BlockPos(pos.getX(), i, pos.getZ());
    }

    public int getChunksLowestHorizon(int x, int z)
    {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000)
        {
            if (!this.isChunkLoaded(x >> 4, z >> 4, true))
            {
                return 0;
            }
            else
            {
                Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
                return chunk.getLowestHeight();
            }
        }
        else
        {
            return this.getSeaLevel() + 1;
        }
    }

    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos)
    {
        if (this.provider.getHasNoSky() && type == EnumSkyBlock.SKY)
        {
            return 0;
        }
        else
        {
            if (pos.getY() < 0)
            {
                pos = new BlockPos(pos.getX(), 0, pos.getZ());
            }

            if (!this.isValid(pos))
            {
                return type.defaultLightValue;
            }
            else if (!this.isBlockLoaded(pos))
            {
                return type.defaultLightValue;
            }
            else if (this.getBlockState(pos).getBlock().getUseNeighborBrightness())
            {
                int intValue = this.getLightFor(type, pos.up());
                int i = this.getLightFor(type, pos.east());
                int j = this.getLightFor(type, pos.west());
                int k = this.getLightFor(type, pos.south());
                int l = this.getLightFor(type, pos.north());

                if (i > intValue)
                {
                    intValue = i;
                }

                if (j > intValue)
                {
                    intValue = j;
                }

                if (k > intValue)
                {
                    intValue = k;
                }

                if (l > intValue)
                {
                    intValue = l;
                }

                return intValue;
            }
            else
            {
                Chunk chunk = this.getChunkFromBlockCoords(pos);
                return chunk.getLightFor(type, pos);
            }
        }
    }

    public int getLightFor(EnumSkyBlock type, BlockPos pos)
    {
        if (pos.getY() < 0)
        {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
        }

        if (!this.isValid(pos))
        {
            return type.defaultLightValue;
        }
        else if (!this.isBlockLoaded(pos))
        {
            return type.defaultLightValue;
        }
        else
        {
            Chunk chunk = this.getChunkFromBlockCoords(pos);
            return chunk.getLightFor(type, pos);
        }
    }

    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue)
    {
        if (this.isValid(pos))
        {
            if (this.isBlockLoaded(pos))
            {
                Chunk chunk = this.getChunkFromBlockCoords(pos);
                chunk.setLightFor(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }

    public void notifyLightSet(BlockPos pos)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).notifyLightSet(pos);
        }
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
        int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);

        if (j < lightValue)
        {
            j = lightValue;
        }

        return i << 20 | j << 4;
    }

    public float getLightBrightness(BlockPos pos)
    {
        return this.provider.getLightBrightnessTable()[this.getLightFromNeighbors(pos)];
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        if (!this.isValid(pos))
        {
            return Blocks.air.getDefaultState();
        }
        else
        {
            Chunk chunk = this.getChunkFromBlockCoords(pos);
            return chunk.getBlockState(pos);
        }
    }

    public boolean isDaytime()
    {
        return this.skylightSubtracted < 4;
    }

    public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end)
    {
        return this.rayTraceBlocks(start, end, false, false, false);
    }

    public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end, boolean stopOnLiquid)
    {
        return this.rayTraceBlocks(start, end, stopOnLiquid, false, false);
    }

    public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
        if (!Double.isNaN(start.xCoord) && !Double.isNaN(start.yCoord) && !Double.isNaN(start.zCoord))
        {
            if (!Double.isNaN(end.xCoord) && !Double.isNaN(end.yCoord) && !Double.isNaN(end.zCoord))
            {
                int i = MathHelper.floor_double(end.xCoord);
                int j = MathHelper.floor_double(end.yCoord);
                int k = MathHelper.floor_double(end.zCoord);
                int l = MathHelper.floor_double(start.xCoord);
                int twentySecondIntValue = MathHelper.floor_double(start.yCoord);
                int twentyFifthIntValue = MathHelper.floor_double(start.zCoord);
                BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos(l, twentySecondIntValue, twentyFifthIntValue);
                IBlockState iblockstate = this.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                BlockPos immutableBlockPos = ignoreBlockWithoutBoundingBox ? blockpos.toImmutable() : null;

                if ((!ignoreBlockWithoutBoundingBox || block.getCollisionBoundingBox(this, immutableBlockPos, iblockstate) != null) && block.canCollideCheck(iblockstate, stopOnLiquid))
                {
                    MovingObjectPosition movingobjectposition = block.collisionRayTrace(this, immutableBlockPos != null ? immutableBlockPos : blockpos.toImmutable(), start, end);

                    if (movingobjectposition != null)
                    {
                        return movingobjectposition;
                    }
                }

                MovingObjectPosition movingobjectposition2 = null;
                int twentySixthIntValue = 200;

                while (twentySixthIntValue-- >= 0)
                {
                    if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord))
                    {
                        return null;
                    }

                    if (l == i && twentySecondIntValue == j && twentyFifthIntValue == k)
                    {
                        return returnLastUncollidableBlock ? movingobjectposition2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double fourthDoubleValue = 999.0D;
                    double fifthDoubleValue = 999.0D;
                    double sixthDoubleValue = 999.0D;

                    if (i > l)
                    {
                        fourthDoubleValue = (double)l + 1.0D;
                    }
                    else if (i < l)
                    {
                        fourthDoubleValue = (double)l + 0.0D;
                    }
                    else
                    {
                        flag2 = false;
                    }

                    if (j > twentySecondIntValue)
                    {
                        fifthDoubleValue = (double)twentySecondIntValue + 1.0D;
                    }
                    else if (j < twentySecondIntValue)
                    {
                        fifthDoubleValue = (double)twentySecondIntValue + 0.0D;
                    }
                    else
                    {
                        flag = false;
                    }

                    if (k > twentyFifthIntValue)
                    {
                        sixthDoubleValue = (double)twentyFifthIntValue + 1.0D;
                    }
                    else if (k < twentyFifthIntValue)
                    {
                        sixthDoubleValue = (double)twentyFifthIntValue + 0.0D;
                    }
                    else
                    {
                        flag1 = false;
                    }

                    double xPlaneT = 999.0D;
                    double yPlaneT = 999.0D;
                    double zPlaneT = 999.0D;
                    double deltaX = end.xCoord - start.xCoord;
                    double deltaY = end.yCoord - start.yCoord;
                    double deltaZ = end.zCoord - start.zCoord;

                    if (flag2)
                    {
                        xPlaneT = (fourthDoubleValue - start.xCoord) / deltaX;
                    }

                    if (flag)
                    {
                        yPlaneT = (fifthDoubleValue - start.yCoord) / deltaY;
                    }

                    if (flag1)
                    {
                        zPlaneT = (sixthDoubleValue - start.zCoord) / deltaZ;
                    }

                    if (xPlaneT == -0.0D)
                    {
                        xPlaneT = -1.0E-4D;
                    }

                    if (yPlaneT == -0.0D)
                    {
                        yPlaneT = -1.0E-4D;
                    }

                    if (zPlaneT == -0.0D)
                    {
                        zPlaneT = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (xPlaneT < yPlaneT && xPlaneT < zPlaneT)
                    {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        start = new Vec3(fourthDoubleValue, start.yCoord + deltaY * xPlaneT, start.zCoord + deltaZ * xPlaneT);
                    }
                    else if (yPlaneT < zPlaneT)
                    {
                        enumfacing = j > twentySecondIntValue ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3(start.xCoord + deltaX * yPlaneT, fifthDoubleValue, start.zCoord + deltaZ * yPlaneT);
                    }
                    else
                    {
                        enumfacing = k > twentyFifthIntValue ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3(start.xCoord + deltaX * zPlaneT, start.yCoord + deltaY * zPlaneT, sixthDoubleValue);
                    }

                    l = MathHelper.floor_double(start.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    twentySecondIntValue = MathHelper.floor_double(start.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    twentyFifthIntValue = MathHelper.floor_double(start.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos.set(l, twentySecondIntValue, twentyFifthIntValue);
                    IBlockState iblockstate1 = this.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();
                    immutableBlockPos = ignoreBlockWithoutBoundingBox ? blockpos.toImmutable() : null;

                    if (!ignoreBlockWithoutBoundingBox || block1.getCollisionBoundingBox(this, immutableBlockPos, iblockstate1) != null)
                    {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid))
                        {
                            MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(this, immutableBlockPos != null ? immutableBlockPos : blockpos.toImmutable(), start, end);

                            if (movingobjectposition1 != null)
                            {
                                return movingobjectposition1;
                            }
                        }
                        else if (returnLastUncollidableBlock)
                        {
                            movingobjectposition2 = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, start, enumfacing, immutableBlockPos != null ? immutableBlockPos : blockpos.toImmutable());
                        }
                    }
                }

                return returnLastUncollidableBlock ? movingobjectposition2 : null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public void playSoundAtEntity(Entity entityIn, String name, float volume, float pitch)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).playSound(name, entityIn.posX, entityIn.posY, entityIn.posZ, volume, pitch);
        }
    }

    public void playSoundToNearExcept(EntityPlayer player, String name, float volume, float pitch)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).playSoundToNearExcept(player, name, player.posX, player.posY, player.posZ, volume, pitch);
        }
    }

    public void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).playSound(soundName, x, y, z, volume, pitch);
        }
    }

    public void playSound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay)
    {
    }

    public void playRecord(BlockPos pos, String name)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).playRecord(name, pos);
        }
    }

    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        this.spawnParticle(particleType.getParticleID(), particleType.getShouldIgnoreRange(), xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
    }

    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        this.spawnParticle(particleType.getParticleID(), particleType.getShouldIgnoreRange() | ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
    }

    private void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).spawnParticle(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
        }
    }

    public boolean addWeatherEffect(Entity entityIn)
    {
        this.weatherEffects.add(entityIn);
        return true;
    }

    public boolean spawnEntityInWorld(Entity entityIn)
    {
        int i = MathHelper.floor_double(entityIn.posX / 16.0D);
        int j = MathHelper.floor_double(entityIn.posZ / 16.0D);
        boolean flag = entityIn.forceSpawn;

        if (entityIn instanceof EntityPlayer)
        {
            flag = true;
        }

        if (!flag && !this.isChunkLoaded(i, j, true))
        {
            return false;
        }
        else
        {
            if (entityIn instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                this.playerEntities.add(entityplayer);
                this.updateAllPlayersSleepingFlag();
            }

            this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
            this.loadedEntityList.add(entityIn);
            this.onEntityAdded(entityIn);
            return true;
        }
    }

    protected void onEntityAdded(Entity entityIn)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).onEntityAdded(entityIn);
        }
    }

    protected void onEntityRemoved(Entity entityIn)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).onEntityRemoved(entityIn);
        }
    }

    public void removeEntity(Entity entityIn)
    {
        if (entityIn.riddenByEntity != null)
        {
            entityIn.riddenByEntity.mountEntity((Entity)null);
        }

        if (entityIn.ridingEntity != null)
        {
            entityIn.mountEntity((Entity)null);
        }

        entityIn.setDead();

        if (entityIn instanceof EntityPlayer)
        {
            this.playerEntities.remove(entityIn);
            this.updateAllPlayersSleepingFlag();
            this.onEntityRemoved(entityIn);
        }
    }

    public void removePlayerEntityDangerously(Entity entityIn)
    {
        entityIn.setDead();

        if (entityIn instanceof EntityPlayer)
        {
            this.playerEntities.remove(entityIn);
            this.updateAllPlayersSleepingFlag();
        }

        int i = entityIn.chunkCoordX;
        int j = entityIn.chunkCoordZ;

        if (entityIn.addedToChunk && this.isChunkLoaded(i, j, true))
        {
            this.getChunkFromChunkCoords(i, j).removeEntity(entityIn);
        }

        this.loadedEntityList.remove(entityIn);
        this.onEntityRemoved(entityIn);
    }

    public void addWorldAccess(IWorldAccess worldAccess)
    {
        this.worldAccesses.add(worldAccess);
    }

    public void removeWorldAccess(IWorldAccess worldAccess)
    {
        this.worldAccesses.remove(worldAccess);
    }

    public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entityIn, AxisAlignedBB bb)
    {
        List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int intValue = MathHelper.floor_double(bb.minZ);
        int secondIntValue = MathHelper.floor_double(bb.maxZ + 1.0D);
        WorldBorder worldborder = this.getWorldBorder();
        boolean flag = entityIn.isOutsideBorder();
        boolean flag1 = this.isInsideBorder(worldborder, entityIn);
        IBlockState iblockstate = Blocks.stone.getDefaultState();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int thirdIntValue = i; thirdIntValue < j; ++thirdIntValue)
        {
            for (int fourthIntValue = intValue; fourthIntValue < secondIntValue; ++fourthIntValue)
            {
                if (this.isBlockLoaded(blockpos$mutableblockpos.set(thirdIntValue, 64, fourthIntValue)))
                {
                    for (int fifthIntValue = k - 1; fifthIntValue < l; ++fifthIntValue)
                    {
                        blockpos$mutableblockpos.set(thirdIntValue, fifthIntValue, fourthIntValue);

                        if (flag && flag1)
                        {
                            entityIn.setOutsideBorder(false);
                        }
                        else if (!flag && !flag1)
                        {
                            entityIn.setOutsideBorder(true);
                        }

                        IBlockState iblockstate1 = iblockstate;

                        if (worldborder.contains(blockpos$mutableblockpos) || !flag1)
                        {
                            iblockstate1 = this.getBlockState(blockpos$mutableblockpos);
                        }

                        iblockstate1.getBlock().addCollisionBoxesToList(this, blockpos$mutableblockpos, iblockstate1, bb, list, entityIn);
                    }
                }
            }
        }

        double thirdDoubleValue = 0.25D;
        List<Entity> list1 = this.getEntitiesWithinAABBExcludingEntity(entityIn, bb.expand(thirdDoubleValue, thirdDoubleValue, thirdDoubleValue));

        for (int sixthIntValue = 0; sixthIntValue < list1.size(); ++sixthIntValue)
        {
            if (entityIn.riddenByEntity != list1 && entityIn.ridingEntity != list1)
            {
                AxisAlignedBB axisalignedbb = list1.get(sixthIntValue).getCollisionBoundingBox();

                if (axisalignedbb != null && axisalignedbb.intersectsWith(bb))
                {
                    list.add(axisalignedbb);
                }

                axisalignedbb = entityIn.getCollisionBox(list1.get(sixthIntValue));

                if (axisalignedbb != null && axisalignedbb.intersectsWith(bb))
                {
                    list.add(axisalignedbb);
                }
            }
        }

        return list;
    }

    public boolean isInsideBorder(WorldBorder worldBorderIn, Entity entityIn)
    {
        double doubleValue = worldBorderIn.minX();
        double doubleValue2 = worldBorderIn.minZ();
        double doubleValue3 = worldBorderIn.maxX();
        double doubleValue4 = worldBorderIn.maxZ();

        if (entityIn.isOutsideBorder())
        {
            ++doubleValue;
            ++doubleValue2;
            --doubleValue3;
            --doubleValue4;
        }
        else
        {
            --doubleValue;
            --doubleValue2;
            ++doubleValue3;
            ++doubleValue4;
        }

        return entityIn.posX > doubleValue && entityIn.posX < doubleValue3 && entityIn.posZ > doubleValue2 && entityIn.posZ < doubleValue4;
    }

    public List<AxisAlignedBB> getCollisionBoxes(AxisAlignedBB bb)
    {
        List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int intValue2 = MathHelper.floor_double(bb.minZ);
        int secondIntValue2 = MathHelper.floor_double(bb.maxZ + 1.0D);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int nestedIndex = i; nestedIndex < j; ++nestedIndex)
        {
            for (int outerIndex = intValue2; outerIndex < secondIntValue2; ++outerIndex)
            {
                if (this.isBlockLoaded(blockpos$mutableblockpos.set(nestedIndex, 64, outerIndex)))
                {
                    for (int index = k - 1; index < l; ++index)
                    {
                        blockpos$mutableblockpos.set(nestedIndex, index, outerIndex);
                        IBlockState iblockstate;

                        if (nestedIndex >= -30000000 && nestedIndex < 30000000 && outerIndex >= -30000000 && outerIndex < 30000000)
                        {
                            iblockstate = this.getBlockState(blockpos$mutableblockpos);
                        }
                        else
                        {
                            iblockstate = Blocks.bedrock.getDefaultState();
                        }

                        iblockstate.getBlock().addCollisionBoxesToList(this, blockpos$mutableblockpos, iblockstate, bb, list, (Entity)null);
                    }
                }
            }
        }

        return list;
    }

    public int calculateSkylightSubtracted(float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        float floatValue2 = 1.0F - (MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.5F);
        floatValue2 = MathHelper.clamp_float(floatValue2, 0.0F, 1.0F);
        floatValue2 = 1.0F - floatValue2;
        floatValue2 = (float)((double)floatValue2 * (1.0D - (double)(this.getRainStrength(partialTicks) * 5.0F) / 16.0D));
        floatValue2 = (float)((double)floatValue2 * (1.0D - (double)(this.getThunderStrength(partialTicks) * 5.0F) / 16.0D));
        floatValue2 = 1.0F - floatValue2;
        return (int)(floatValue2 * 11.0F);
    }

    public float getSunBrightness(float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        float floatValue2 = 1.0F - (MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.2F);
        floatValue2 = MathHelper.clamp_float(floatValue2, 0.0F, 1.0F);
        floatValue2 = 1.0F - floatValue2;
        floatValue2 = (float)((double)floatValue2 * (1.0D - (double)(this.getRainStrength(partialTicks) * 5.0F) / 16.0D));
        floatValue2 = (float)((double)floatValue2 * (1.0D - (double)(this.getThunderStrength(partialTicks) * 5.0F) / 16.0D));
        return floatValue2 * 0.8F + 0.2F;
    }

    public Vec3 getSkyColor(Entity entityIn, float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        float floatValue2 = MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
        floatValue2 = MathHelper.clamp_float(floatValue2, 0.0F, 1.0F);
        int i = MathHelper.floor_double(entityIn.posX);
        int j = MathHelper.floor_double(entityIn.posY);
        int k = MathHelper.floor_double(entityIn.posZ);
        BlockPos blockPos = new BlockPos(i, j, k);
        BiomeGenBase biomeGenBase = this.getBiomeGenForCoords(blockPos);
        float floatValue3 = biomeGenBase.getFloatTemperature(blockPos);
        int l = biomeGenBase.getSkyColorByTemp(floatValue3);
        float floatValue4 = (float)(l >> 16 & 255) / 255.0F;
        float floatValue5 = (float)(l >> 8 & 255) / 255.0F;
        float floatValue6 = (float)(l & 255) / 255.0F;
        floatValue4 = floatValue4 * floatValue2;
        floatValue5 = floatValue5 * floatValue2;
        floatValue6 = floatValue6 * floatValue2;
        float floatValue7 = this.getRainStrength(partialTicks);

        if (floatValue7 > 0.0F)
        {
            float floatValue8 = (floatValue4 * 0.3F + floatValue5 * 0.59F + floatValue6 * 0.11F) * 0.6F;
            float floatValue9 = 1.0F - floatValue7 * 0.75F;
            floatValue4 = floatValue4 * floatValue9 + floatValue8 * (1.0F - floatValue9);
            floatValue5 = floatValue5 * floatValue9 + floatValue8 * (1.0F - floatValue9);
            floatValue6 = floatValue6 * floatValue9 + floatValue8 * (1.0F - floatValue9);
        }

        float floatValue = this.getThunderStrength(partialTicks);

        if (floatValue > 0.0F)
        {
            float floatValue11 = (floatValue4 * 0.3F + floatValue5 * 0.59F + floatValue6 * 0.11F) * 0.2F;
            float floatValue12 = 1.0F - floatValue * 0.75F;
            floatValue4 = floatValue4 * floatValue12 + floatValue11 * (1.0F - floatValue12);
            floatValue5 = floatValue5 * floatValue12 + floatValue11 * (1.0F - floatValue12);
            floatValue6 = floatValue6 * floatValue12 + floatValue11 * (1.0F - floatValue12);
        }

        if (this.lastLightningBolt > 0)
        {
            float floatValue13 = (float)this.lastLightningBolt - partialTicks;

            if (floatValue13 > 1.0F)
            {
                floatValue13 = 1.0F;
            }

            floatValue13 = floatValue13 * 0.45F;
            floatValue4 = floatValue4 * (1.0F - floatValue13) + 0.8F * floatValue13;
            floatValue5 = floatValue5 * (1.0F - floatValue13) + 0.8F * floatValue13;
            floatValue6 = floatValue6 * (1.0F - floatValue13) + 1.0F * floatValue13;
        }

        return new Vec3((double)floatValue4, (double)floatValue5, (double)floatValue6);
    }

    public float getCelestialAngle(float partialTicks)
    {
        return this.provider.calculateCelestialAngle(this.worldInfo.getWorldTime(), partialTicks);
    }

    public int getMoonPhase()
    {
        return this.provider.getMoonPhase(this.worldInfo.getWorldTime());
    }

    public float getCurrentMoonPhaseFactor()
    {
        return WorldProvider.moonPhaseFactors[this.provider.getMoonPhase(this.worldInfo.getWorldTime())];
    }

    public float getCelestialAngleRadians(float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        return f * (float)Math.PI * 2.0F;
    }

    public Vec3 getCloudColour(float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        float floatValue2 = MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
        floatValue2 = MathHelper.clamp_float(floatValue2, 0.0F, 1.0F);
        float floatValue3 = (float)(this.cloudColour >> 16 & 255L) / 255.0F;
        float floatValue4 = (float)(this.cloudColour >> 8 & 255L) / 255.0F;
        float floatValue5 = (float)(this.cloudColour & 255L) / 255.0F;
        float floatValue6 = this.getRainStrength(partialTicks);

        if (floatValue6 > 0.0F)
        {
            float floatValue7 = (floatValue3 * 0.3F + floatValue4 * 0.59F + floatValue5 * 0.11F) * 0.6F;
            float floatValue8 = 1.0F - floatValue6 * 0.95F;
            floatValue3 = floatValue3 * floatValue8 + floatValue7 * (1.0F - floatValue8);
            floatValue4 = floatValue4 * floatValue8 + floatValue7 * (1.0F - floatValue8);
            floatValue5 = floatValue5 * floatValue8 + floatValue7 * (1.0F - floatValue8);
        }

        floatValue3 = floatValue3 * (floatValue2 * 0.9F + 0.1F);
        floatValue4 = floatValue4 * (floatValue2 * 0.9F + 0.1F);
        floatValue5 = floatValue5 * (floatValue2 * 0.85F + 0.15F);
        float floatValue9 = this.getThunderStrength(partialTicks);

        if (floatValue9 > 0.0F)
        {
            float floatValue10 = (floatValue3 * 0.3F + floatValue4 * 0.59F + floatValue5 * 0.11F) * 0.2F;
            float floatValue11 = 1.0F - floatValue9 * 0.95F;
            floatValue3 = floatValue3 * floatValue11 + floatValue10 * (1.0F - floatValue11);
            floatValue4 = floatValue4 * floatValue11 + floatValue10 * (1.0F - floatValue11);
            floatValue5 = floatValue5 * floatValue11 + floatValue10 * (1.0F - floatValue11);
        }

        return new Vec3((double)floatValue3, (double)floatValue4, (double)floatValue5);
    }

    public Vec3 getFogColor(float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        return this.provider.getFogColor(f, partialTicks);
    }

    public BlockPos getPrecipitationHeight(BlockPos pos)
    {
        return this.getChunkFromBlockCoords(pos).getPrecipitationHeight(pos);
    }

    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos)
    {
        Chunk chunk = this.getChunkFromBlockCoords(pos);
        int x = pos.getX();
        int z = pos.getZ();
        int y = chunk.getTopFilledSegment() + 16;
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        while (y >= 0)
        {
            checkPos.set(x, y - 1, z);
            Material material = chunk.getBlock(checkPos).getMaterial();

            if (material.blocksMovement() && material != Material.leaves)
            {
                break;
            }

            --y;
        }

        return new BlockPos(x, y, z);
    }

    public float getStarBrightness(float partialTicks)
    {
        float f = this.getCelestialAngle(partialTicks);
        float floatValue2 = 1.0F - (MathHelper.cos(f * (float)Math.PI * 2.0F) * 2.0F + 0.25F);
        floatValue2 = MathHelper.clamp_float(floatValue2, 0.0F, 1.0F);
        return floatValue2 * floatValue2 * 0.5F;
    }

    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay)
    {
    }

    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority)
    {
    }

    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority)
    {
    }

    public void updateEntities()
    {
        this.theProfiler.startSection("entities");
        this.theProfiler.startSection("global");

        for (int i = 0; i < this.weatherEffects.size(); ++i)
        {
            Entity entity = this.weatherEffects.get(i);

            try
            {
                ++entity.ticksExisted;
                entity.onUpdate();
            }
            catch (Throwable throwable2)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null)
                {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                }
                else
                {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                throw new ReportedException(crashreport);
            }

            if (entity.isDead)
            {
                this.weatherEffects.remove(i--);
            }
        }

        this.theProfiler.endStartSection("remove");

        if (!this.unloadedEntityList.isEmpty())
        {
            this.loadedEntityList.removeAll(this.unloadedEntityList);
        }

        for (int k = 0; k < this.unloadedEntityList.size(); ++k)
        {
            Entity entity1 = this.unloadedEntityList.get(k);
            int j = entity1.chunkCoordX;
            int twentyEighthIntValue = entity1.chunkCoordZ;

            if (entity1.addedToChunk && this.isChunkLoaded(j, twentyEighthIntValue, true))
            {
                this.getChunkFromChunkCoords(j, twentyEighthIntValue).removeEntity(entity1);
            }
        }

        for (int l = 0; l < this.unloadedEntityList.size(); ++l)
        {
            this.onEntityRemoved(this.unloadedEntityList.get(l));
        }

        this.unloadedEntityList.clear();
        this.theProfiler.endStartSection("regular");

        for (int twentyFirstIntValue = 0; twentyFirstIntValue < this.loadedEntityList.size(); ++twentyFirstIntValue)
        {
            Entity entity2 = this.loadedEntityList.get(twentyFirstIntValue);

            if (entity2.ridingEntity != null)
            {
                if (!entity2.ridingEntity.isDead && entity2.ridingEntity.riddenByEntity == entity2)
                {
                    continue;
                }

                entity2.ridingEntity.riddenByEntity = null;
                entity2.ridingEntity = null;
            }

            this.theProfiler.startSection("tick");

            if (!entity2.isDead)
            {
                try
                {
                    this.updateEntity(entity2);
                }
                catch (Throwable throwable1)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory2 = crashreport1.makeCategory("Entity being ticked");
                    entity2.addEntityCrashInfo(crashreportcategory2);
                    throw new ReportedException(crashreport1);
                }
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("remove");

            if (entity2.isDead)
            {
                int twentySeventhIntValue = entity2.chunkCoordX;
                int twentyThirdIntValue = entity2.chunkCoordZ;

                if (entity2.addedToChunk && this.isChunkLoaded(twentySeventhIntValue, twentyThirdIntValue, true))
                {
                    this.getChunkFromChunkCoords(twentySeventhIntValue, twentyThirdIntValue).removeEntity(entity2);
                }

                this.loadedEntityList.remove(twentyFirstIntValue--);
                this.onEntityRemoved(entity2);
            }

            this.theProfiler.endSection();
        }

        this.theProfiler.endStartSection("blockEntities");
        this.processingLoadedTiles = true;
        Iterator<TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext())
        {
            TileEntity tileentity = (TileEntity)iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorldObj())
            {
                BlockPos blockpos = tileentity.getPos();

                if (this.isBlockLoaded(blockpos) && this.worldBorder.contains(blockpos))
                {
                    try
                    {
                        ((ITickable)tileentity).update();
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory1 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory1);
                        throw new ReportedException(crashreport2);
                    }
                }
            }

            if (tileentity.isInvalid())
            {
                iterator.remove();
                this.loadedTileEntityList.remove(tileentity);

                if (this.isBlockLoaded(tileentity.getPos()))
                {
                    this.getChunkFromBlockCoords(tileentity.getPos()).removeTileEntity(tileentity.getPos());
                }
            }
        }

        this.processingLoadedTiles = false;

        if (!this.tileEntitiesToBeRemoved.isEmpty())
        {
            this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
            this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
            this.tileEntitiesToBeRemoved.clear();
        }

        this.theProfiler.endStartSection("pendingBlockEntities");

        if (!this.addedTileEntityList.isEmpty())
        {
            for (int twentyFourthIntValue = 0; twentyFourthIntValue < this.addedTileEntityList.size(); ++twentyFourthIntValue)
            {
                TileEntity tileentity1 = this.addedTileEntityList.get(twentyFourthIntValue);

                if (!tileentity1.isInvalid())
                {
                    if (!this.loadedTileEntityList.contains(tileentity1))
                    {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos()))
                    {
                        this.getChunkFromBlockCoords(tileentity1.getPos()).addTileEntity(tileentity1.getPos(), tileentity1);
                    }

                    this.markBlockForUpdate(tileentity1.getPos());
                }
            }

            this.addedTileEntityList.clear();
        }

        this.theProfiler.endSection();
        this.theProfiler.endSection();
    }

    public boolean addTileEntity(TileEntity tile)
    {
        boolean flag = this.loadedTileEntityList.add(tile);

        if (flag && tile instanceof ITickable)
        {
            this.tickableTileEntities.add(tile);
        }

        return flag;
    }

    public void addTileEntities(Collection<TileEntity> tileEntityCollection)
    {
        if (this.processingLoadedTiles)
        {
            this.addedTileEntityList.addAll(tileEntityCollection);
        }
        else
        {
            for (TileEntity tileEntity : tileEntityCollection)
            {
                this.loadedTileEntityList.add(tileEntity);

                if (tileEntity instanceof ITickable)
                {
                    this.tickableTileEntities.add(tileEntity);
                }
            }
        }
    }

    public void updateEntity(Entity ent)
    {
        this.updateEntityWithOptionalForce(ent, true);
    }

    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate)
    {
        int i = MathHelper.floor_double(entityIn.posX);
        int j = MathHelper.floor_double(entityIn.posZ);
        int k = 32;

        if (!forceUpdate || this.isAreaLoaded(i - k, 0, j - k, i + k, 0, j + k, true))
        {
            entityIn.lastTickPosX = entityIn.posX;
            entityIn.lastTickPosY = entityIn.posY;
            entityIn.lastTickPosZ = entityIn.posZ;
            entityIn.prevRotationYaw = entityIn.rotationYaw;
            entityIn.prevRotationPitch = entityIn.rotationPitch;

            if (forceUpdate && entityIn.addedToChunk)
            {
                ++entityIn.ticksExisted;

                if (entityIn.ridingEntity != null)
                {
                    entityIn.updateRidden();
                }
                else
                {
                    entityIn.onUpdate();
                }
            }

            this.theProfiler.startSection("chunkCheck");

            if (Double.isNaN(entityIn.posX) || Double.isInfinite(entityIn.posX))
            {
                entityIn.posX = entityIn.lastTickPosX;
            }

            if (Double.isNaN(entityIn.posY) || Double.isInfinite(entityIn.posY))
            {
                entityIn.posY = entityIn.lastTickPosY;
            }

            if (Double.isNaN(entityIn.posZ) || Double.isInfinite(entityIn.posZ))
            {
                entityIn.posZ = entityIn.lastTickPosZ;
            }

            if (Double.isNaN((double)entityIn.rotationPitch) || Double.isInfinite((double)entityIn.rotationPitch))
            {
                entityIn.rotationPitch = entityIn.prevRotationPitch;
            }

            if (Double.isNaN((double)entityIn.rotationYaw) || Double.isInfinite((double)entityIn.rotationYaw))
            {
                entityIn.rotationYaw = entityIn.prevRotationYaw;
            }

            int l = MathHelper.floor_double(entityIn.posX / 16.0D);
            int intValue2 = MathHelper.floor_double(entityIn.posY / 16.0D);
            int secondIntValue2 = MathHelper.floor_double(entityIn.posZ / 16.0D);

            if (!entityIn.addedToChunk || entityIn.chunkCoordX != l || entityIn.chunkCoordY != intValue2 || entityIn.chunkCoordZ != secondIntValue2)
            {
                if (entityIn.addedToChunk && this.isChunkLoaded(entityIn.chunkCoordX, entityIn.chunkCoordZ, true))
                {
                    this.getChunkFromChunkCoords(entityIn.chunkCoordX, entityIn.chunkCoordZ).removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
                }

                if (this.isChunkLoaded(l, secondIntValue2, true))
                {
                    entityIn.addedToChunk = true;
                    this.getChunkFromChunkCoords(l, secondIntValue2).addEntity(entityIn);
                }
                else
                {
                    entityIn.addedToChunk = false;
                }
            }

            this.theProfiler.endSection();

            if (forceUpdate && entityIn.addedToChunk && entityIn.riddenByEntity != null)
            {
                if (!entityIn.riddenByEntity.isDead && entityIn.riddenByEntity.ridingEntity == entityIn)
                {
                    this.updateEntity(entityIn.riddenByEntity);
                }
                else
                {
                    entityIn.riddenByEntity.ridingEntity = null;
                    entityIn.riddenByEntity = null;
                }
            }
        }
    }

    public boolean checkNoEntityCollision(AxisAlignedBB bb)
    {
        return this.checkNoEntityCollision(bb, (Entity)null);
    }

    public boolean checkNoEntityCollision(AxisAlignedBB bb, Entity entityIn)
    {
        List<Entity> list = this.getEntitiesWithinAABBExcludingEntity((Entity)null, bb);

        for (int i = 0; i < list.size(); ++i)
        {
            Entity entity = list.get(i);

            if (!entity.isDead && entity.preventEntitySpawning && entity != entityIn && (entityIn == null || entityIn.ridingEntity != entity && entityIn.riddenByEntity != entity))
            {
                return false;
            }
        }

        return true;
    }

    public boolean checkBlockCollision(AxisAlignedBB bb)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY);
        int intValue2 = MathHelper.floor_double(bb.minZ);
        int secondIntValue2 = MathHelper.floor_double(bb.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int nestedIndex = i; nestedIndex <= j; ++nestedIndex)
        {
            for (int outerIndex = k; outerIndex <= l; ++outerIndex)
            {
                for (int index = intValue2; index <= secondIntValue2; ++index)
                {
                    Block block = this.getBlockState(blockpos$mutableblockpos.set(nestedIndex, outerIndex, index)).getBlock();

                    if (block.getMaterial() != Material.air)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isAnyLiquid(AxisAlignedBB bb)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY);
        int intValue2 = MathHelper.floor_double(bb.minZ);
        int secondIntValue2 = MathHelper.floor_double(bb.maxZ);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int nestedIndex = i; nestedIndex <= j; ++nestedIndex)
        {
            for (int outerIndex = k; outerIndex <= l; ++outerIndex)
            {
                for (int index = intValue2; index <= secondIntValue2; ++index)
                {
                    Block block = this.getBlockState(blockpos$mutableblockpos.set(nestedIndex, outerIndex, index)).getBlock();

                    if (block.getMaterial().isLiquid())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isFlammableWithin(AxisAlignedBB bb)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int intValue2 = MathHelper.floor_double(bb.minZ);
        int secondIntValue2 = MathHelper.floor_double(bb.maxZ + 1.0D);

        if (this.isAreaLoaded(i, k, intValue2, j, l, secondIntValue2, true))
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int nestedIndex = i; nestedIndex < j; ++nestedIndex)
            {
                for (int outerIndex = k; outerIndex < l; ++outerIndex)
                {
                    for (int index = intValue2; index < secondIntValue2; ++index)
                    {
                        Block block = this.getBlockState(blockpos$mutableblockpos.set(nestedIndex, outerIndex, index)).getBlock();

                        if (block == Blocks.fire || block == Blocks.flowing_lava || block == Blocks.lava)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int secondIntValue = MathHelper.floor_double(bb.minZ);
        int thirdIntValue = MathHelper.floor_double(bb.maxZ + 1.0D);

        if (!this.isAreaLoaded(i, k, secondIntValue, j, l, thirdIntValue, true))
        {
            return false;
        }
        else
        {
            boolean flag = false;
            Vec3 localValue = Vec3.ZERO;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int fourthIntValue = i; fourthIntValue < j; ++fourthIntValue)
            {
                for (int fifthIntValue = k; fifthIntValue < l; ++fifthIntValue)
                {
                    for (int sixthIntValue = secondIntValue; sixthIntValue < thirdIntValue; ++sixthIntValue)
                    {
                        blockpos$mutableblockpos.set(fourthIntValue, fifthIntValue, sixthIntValue);
                        IBlockState iblockstate = this.getBlockState(blockpos$mutableblockpos);
                        Block block = iblockstate.getBlock();

                        if (block.getMaterial() == materialIn)
                        {
                            double doubleValue = (double)((float)(fifthIntValue + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue()));

                            if ((double)l >= doubleValue)
                            {
                                flag = true;
                                localValue = block.modifyAcceleration(this, blockpos$mutableblockpos, entityIn, localValue);
                            }
                        }
                    }
                }
            }

            if (localValue.lengthVector() > 0.0D && entityIn.isPushedByWater())
            {
                localValue = localValue.normalize();
                double secondDoubleValue = 0.014D;
                entityIn.motionX += localValue.xCoord * secondDoubleValue;
                entityIn.motionY += localValue.yCoord * secondDoubleValue;
                entityIn.motionZ += localValue.zCoord * secondDoubleValue;
            }

            return flag;
        }
    }

    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int intValue2 = MathHelper.floor_double(bb.minZ);
        int secondIntValue2 = MathHelper.floor_double(bb.maxZ + 1.0D);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int nestedIndex = i; nestedIndex < j; ++nestedIndex)
        {
            for (int outerIndex = k; outerIndex < l; ++outerIndex)
            {
                for (int index = intValue2; index < secondIntValue2; ++index)
                {
                    if (this.getBlockState(blockpos$mutableblockpos.set(nestedIndex, outerIndex, index)).getBlock().getMaterial() == materialIn)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isAABBInMaterial(AxisAlignedBB bb, Material materialIn)
    {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int intValue2 = MathHelper.floor_double(bb.minZ);
        int secondIntValue2 = MathHelper.floor_double(bb.maxZ + 1.0D);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int nestedIndex = i; nestedIndex < j; ++nestedIndex)
        {
            for (int outerIndex = k; outerIndex < l; ++outerIndex)
            {
                for (int index = intValue2; index < secondIntValue2; ++index)
                {
                    IBlockState iblockstate = this.getBlockState(blockpos$mutableblockpos.set(nestedIndex, outerIndex, index));
                    Block block = iblockstate.getBlock();

                    if (block.getMaterial() == materialIn)
                    {
                        int intValue3 = ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue();
                        double doubleValue = (double)(outerIndex + 1);

                        if (intValue3 < 8)
                        {
                            doubleValue = (double)(outerIndex + 1) - (double)intValue3 / 8.0D;
                        }

                        if (doubleValue >= bb.minY)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Explosion createExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isSmoking)
    {
        return this.newExplosion(entityIn, x, y, z, strength, false, isSmoking);
    }

    public Explosion newExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking)
    {
        Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, isFlaming, isSmoking);
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        return explosion;
    }

    public float getBlockDensity(Vec3 vec, AxisAlignedBB bb)
    {
        double doubleValue = 1.0D / ((bb.maxX - bb.minX) * 2.0D + 1.0D);
        double doubleValue2 = 1.0D / ((bb.maxY - bb.minY) * 2.0D + 1.0D);
        double doubleValue3 = 1.0D / ((bb.maxZ - bb.minZ) * 2.0D + 1.0D);
        double doubleValue4 = (1.0D - MathHelper.floor_double(1.0D / doubleValue) * doubleValue) / 2.0D;
        double doubleValue5 = (1.0D - MathHelper.floor_double(1.0D / doubleValue3) * doubleValue3) / 2.0D;

        if (doubleValue >= 0.0D && doubleValue2 >= 0.0D && doubleValue3 >= 0.0D)
        {
            int i = 0;
            int j = 0;

            for (float f = 0.0F; f <= 1.0F; f = (float)((double)f + doubleValue))
            {
                for (float floatValue2 = 0.0F; floatValue2 <= 1.0F; floatValue2 = (float)((double)floatValue2 + doubleValue2))
                {
                    for (float floatValue3 = 0.0F; floatValue3 <= 1.0F; floatValue3 = (float)((double)floatValue3 + doubleValue3))
                    {
                        double doubleValue6 = bb.minX + (bb.maxX - bb.minX) * (double)f;
                        double doubleValue7 = bb.minY + (bb.maxY - bb.minY) * (double)floatValue2;
                        double doubleValue8 = bb.minZ + (bb.maxZ - bb.minZ) * (double)floatValue3;

                        if (this.rayTraceBlocks(new Vec3(doubleValue6 + doubleValue4, doubleValue7, doubleValue8 + doubleValue5), vec) == null)
                        {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        }
        else
        {
            return 0.0F;
        }
    }

    public boolean extinguishFire(EntityPlayer player, BlockPos pos, EnumFacing side)
    {
        pos = pos.offset(side);

        if (this.getBlockState(pos).getBlock() == Blocks.fire)
        {
            this.playAuxSFXAtEntity(player, 1004, pos, 0);
            this.setBlockToAir(pos);
            return true;
        }
        else
        {
            return false;
        }
    }

    public String getDebugLoadedEntities()
    {
        return "All: " + this.loadedEntityList.size();
    }

    public String getProviderName()
    {
        return this.chunkProvider.makeString();
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        if (!this.isValid(pos))
        {
            return null;
        }
        else
        {
            TileEntity tileEntity = null;

            if (this.processingLoadedTiles)
            {
                for (int i = 0; i < this.addedTileEntityList.size(); ++i)
                {
                    TileEntity tileentity1 = this.addedTileEntityList.get(i);

                    if (!tileentity1.isInvalid() && tileentity1.getPos().equals(pos))
                    {
                        tileEntity = tileentity1;
                        break;
                    }
                }
            }

            if (tileEntity == null)
            {
                tileEntity = this.getChunkFromBlockCoords(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
            }

            if (tileEntity == null)
            {
                for (int j = 0; j < this.addedTileEntityList.size(); ++j)
                {
                    TileEntity tileentity2 = this.addedTileEntityList.get(j);

                    if (!tileentity2.isInvalid() && tileentity2.getPos().equals(pos))
                    {
                        tileEntity = tileentity2;
                        break;
                    }
                }
            }

            return tileEntity;
        }
    }

    public void setTileEntity(BlockPos pos, TileEntity tileEntityIn)
    {
        if (tileEntityIn != null && !tileEntityIn.isInvalid())
        {
            if (this.processingLoadedTiles)
            {
                tileEntityIn.setPos(pos);
                Iterator<TileEntity> iterator = this.addedTileEntityList.iterator();

                while (iterator.hasNext())
                {
                    TileEntity tileEntity = (TileEntity)iterator.next();

                    if (tileEntity.getPos().equals(pos))
                    {
                        tileEntity.invalidate();
                        iterator.remove();
                    }
                }

                this.addedTileEntityList.add(tileEntityIn);
            }
            else
            {
                this.addTileEntity(tileEntityIn);
                this.getChunkFromBlockCoords(pos).addTileEntity(pos, tileEntityIn);
            }
        }
    }

    public void removeTileEntity(BlockPos pos)
    {
        TileEntity tileEntity = this.getTileEntity(pos);

        if (tileEntity != null && this.processingLoadedTiles)
        {
            tileEntity.invalidate();
            this.addedTileEntityList.remove(tileEntity);
        }
        else
        {
            if (tileEntity != null)
            {
                this.addedTileEntityList.remove(tileEntity);
                this.loadedTileEntityList.remove(tileEntity);
                this.tickableTileEntities.remove(tileEntity);
            }

            this.getChunkFromBlockCoords(pos).removeTileEntity(pos);
        }
    }

    public void markTileEntityForRemoval(TileEntity tileEntityIn)
    {
        this.tileEntitiesToBeRemoved.add(tileEntityIn);
    }

    public boolean isBlockFullCube(BlockPos pos)
    {
        IBlockState iblockstate = this.getBlockState(pos);
        AxisAlignedBB axisAlignedBB = iblockstate.getBlock().getCollisionBoundingBox(this, pos, iblockstate);
        return axisAlignedBB != null && axisAlignedBB.getAverageEdgeLength() >= 1.0D;
    }

    public static boolean doesBlockHaveSolidTopSurface(IBlockAccess blockAccess, BlockPos pos)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block.getMaterial().isOpaque() && block.isFullCube() ? true : (block instanceof BlockStairs ? iblockstate.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP : (block instanceof BlockSlab ? iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP : (block instanceof BlockHopper ? true : (block instanceof BlockSnow ? ((Integer)iblockstate.getValue(BlockSnow.LAYERS)).intValue() == 7 : false))));
    }

    public boolean isBlockNormalCube(BlockPos pos, boolean _default)
    {
        if (!this.isValid(pos))
        {
            return _default;
        }
        else
        {
            Chunk chunk = this.chunkProvider.provideChunk(pos);

            if (chunk.isEmpty())
            {
                return _default;
            }
            else
            {
                Block block = this.getBlockState(pos).getBlock();
                return block.getMaterial().isOpaque() && block.isFullCube();
            }
        }
    }

    public void calculateInitialSkylight()
    {
        int i = this.calculateSkylightSubtracted(1.0F);

        if (i != this.skylightSubtracted)
        {
            this.skylightSubtracted = i;
        }
    }

    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful)
    {
        this.spawnHostileMobs = hostile;
        this.spawnPeacefulMobs = peaceful;
    }

    public void tick()
    {
        this.updateWeather();
    }

    protected void calculateInitialWeather()
    {
        if (this.worldInfo.isRaining())
        {
            this.rainingStrength = 1.0F;

            if (this.worldInfo.isThundering())
            {
                this.thunderingStrength = 1.0F;
            }
        }
    }

    protected void updateWeather()
    {
        if (!this.provider.getHasNoSky())
        {
            if (!this.isRemote)
            {
                int i = this.worldInfo.getCleanWeatherTime();

                if (i > 0)
                {
                    --i;
                    this.worldInfo.setCleanWeatherTime(i);
                    this.worldInfo.setThunderTime(this.worldInfo.isThundering() ? 1 : 2);
                    this.worldInfo.setRainTime(this.worldInfo.isRaining() ? 1 : 2);
                }

                int j = this.worldInfo.getThunderTime();

                if (j <= 0)
                {
                    if (this.worldInfo.isThundering())
                    {
                        this.worldInfo.setThunderTime(this.rand.nextInt(12000) + 3600);
                    }
                    else
                    {
                        this.worldInfo.setThunderTime(this.rand.nextInt(168000) + 12000);
                    }
                }
                else
                {
                    --j;
                    this.worldInfo.setThunderTime(j);

                    if (j <= 0)
                    {
                        this.worldInfo.setThundering(!this.worldInfo.isThundering());
                    }
                }

                this.prevThunderingStrength = this.thunderingStrength;

                if (this.worldInfo.isThundering())
                {
                    this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
                }
                else
                {
                    this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
                }

                this.thunderingStrength = MathHelper.clamp_float(this.thunderingStrength, 0.0F, 1.0F);
                int k = this.worldInfo.getRainTime();

                if (k <= 0)
                {
                    if (this.worldInfo.isRaining())
                    {
                        this.worldInfo.setRainTime(this.rand.nextInt(12000) + 12000);
                    }
                    else
                    {
                        this.worldInfo.setRainTime(this.rand.nextInt(168000) + 12000);
                    }
                }
                else
                {
                    --k;
                    this.worldInfo.setRainTime(k);

                    if (k <= 0)
                    {
                        this.worldInfo.setRaining(!this.worldInfo.isRaining());
                    }
                }

                this.prevRainingStrength = this.rainingStrength;

                if (this.worldInfo.isRaining())
                {
                    this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
                }
                else
                {
                    this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
                }

                this.rainingStrength = MathHelper.clamp_float(this.rainingStrength, 0.0F, 1.0F);
            }
        }
    }

    protected void setActivePlayerChunksAndCheckLight()
    {
        this.activeChunkSet.clear();
        this.theProfiler.startSection("buildList");

        for (int i = 0; i < this.playerEntities.size(); ++i)
        {
            EntityPlayer entityPlayer = this.playerEntities.get(i);
            int j = MathHelper.floor_double(entityPlayer.posX / 16.0D);
            int k = MathHelper.floor_double(entityPlayer.posZ / 16.0D);
            int l = this.getRenderDistanceChunks();

            for (int index2 = -l; index2 <= l; ++index2)
            {
                for (int innerIndex = -l; innerIndex <= l; ++innerIndex)
                {
                    this.activeChunkSet.add(new ChunkCoordIntPair(index2 + j, innerIndex + k));
                }
            }
        }

        this.theProfiler.endSection();

        if (this.ambientTickCountdown > 0)
        {
            --this.ambientTickCountdown;
        }

        this.theProfiler.startSection("playerCheckLight");

        if (!this.playerEntities.isEmpty())
        {
            int count = this.rand.nextInt(this.playerEntities.size());
            EntityPlayer entityplayer1 = this.playerEntities.get(count);
            int fourthIntValue2 = MathHelper.floor_double(entityplayer1.posX) + this.rand.nextInt(11) - 5;
            int intValue = MathHelper.floor_double(entityplayer1.posY) + this.rand.nextInt(11) - 5;
            int intValue2 = MathHelper.floor_double(entityplayer1.posZ) + this.rand.nextInt(11) - 5;
            this.checkLight(new BlockPos(fourthIntValue2, intValue, intValue2));
        }

        this.theProfiler.endSection();
    }

    protected abstract int getRenderDistanceChunks();

    protected void playMoodSoundAndCheckLight(int chunkX, int chunkZ, Chunk chunkIn)
    {
        this.theProfiler.endStartSection("moodSound");

        if (this.ambientTickCountdown == 0 && !this.isRemote)
        {
            this.updateLCG = this.updateLCG * 3 + 1013904223;
            int i = this.updateLCG >> 2;
            int j = i & 15;
            int k = i >> 8 & 15;
            int l = i >> 16 & 255;
            BlockPos blockpos = new BlockPos(j, l, k);
            Block block = chunkIn.getBlock(blockpos);
            j = j + chunkX;
            k = k + chunkZ;

            if (block.getMaterial() == Material.air && this.getLight(blockpos) <= this.rand.nextInt(8) && this.getLightFor(EnumSkyBlock.SKY, blockpos) <= 0)
            {
                EntityPlayer entityPlayer = this.getClosestPlayer((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, 8.0D);

                if (entityPlayer != null && entityPlayer.getDistanceSq((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D) > 4.0D)
                {
                    this.playSoundEffect((double)j + 0.5D, (double)l + 0.5D, (double)k + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + this.rand.nextFloat() * 0.2F);
                    this.ambientTickCountdown = this.rand.nextInt(12000) + 6000;
                }
            }
        }

        this.theProfiler.endStartSection("checkLight");
        chunkIn.enqueueRelightChecks();
    }

    protected void updateBlocks()
    {
        this.setActivePlayerChunksAndCheckLight();
    }

    public void forceBlockUpdateTick(Block blockType, BlockPos pos, Random random)
    {
        this.scheduledUpdatesAreImmediate = true;
        blockType.updateTick(this, pos, this.getBlockState(pos), random);
        this.scheduledUpdatesAreImmediate = false;
    }

    public boolean canBlockFreezeWater(BlockPos pos)
    {
        return this.canBlockFreeze(pos, false);
    }

    public boolean canBlockFreezeNoWater(BlockPos pos)
    {
        return this.canBlockFreeze(pos, true);
    }

    public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj)
    {
        BiomeGenBase biomeGenBase = this.getBiomeGenForCoords(pos);
        float f = biomeGenBase.getFloatTemperature(pos);

        if (f > 0.15F)
        {
            return false;
        }
        else
        {
            if (pos.getY() >= 0 && pos.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
            {
                IBlockState iblockstate = this.getBlockState(pos);
                Block block = iblockstate.getBlock();

                if ((block == Blocks.water || block == Blocks.flowing_water) && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0)
                {
                    if (!noWaterAdj)
                    {
                        return true;
                    }

                    boolean flag = this.isWater(pos.west()) && this.isWater(pos.east()) && this.isWater(pos.north()) && this.isWater(pos.south());

                    if (!flag)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private boolean isWater(BlockPos pos)
    {
        return this.getBlockState(pos).getBlock().getMaterial() == Material.water;
    }

    public boolean canSnowAt(BlockPos pos, boolean checkLight)
    {
        BiomeGenBase biomeGenBase = this.getBiomeGenForCoords(pos);
        float f = biomeGenBase.getFloatTemperature(pos);

        if (f > 0.15F)
        {
            return false;
        }
        else if (!checkLight)
        {
            return true;
        }
        else
        {
            if (pos.getY() >= 0 && pos.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
            {
                Block block = this.getBlockState(pos).getBlock();

                if (block.getMaterial() == Material.air && Blocks.snow_layer.canPlaceBlockAt(this, pos))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean checkLight(BlockPos pos)
    {
        boolean flag = false;

        if (!this.provider.getHasNoSky())
        {
            flag |= this.checkLightFor(EnumSkyBlock.SKY, pos);
        }

        flag = flag | this.checkLightFor(EnumSkyBlock.BLOCK, pos);
        return flag;
    }

    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos)
    {
        if (pos.getY() < 0 || pos.getY() >= 256 || !this.isAreaLoaded(pos, 16, false))
        {
            return false;
        }

        this.lightingEngine.scheduleLightUpdate(lightType, pos);
        return true;
    }

    public boolean tickUpdates(boolean runAllPending)
    {
        return false;
    }

    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove)
    {
        return null;
    }

    public List<NextTickListEntry> getPendingBlockUpdatesInArea(StructureBoundingBox structureBB, boolean remove)
    {
        return null;
    }

    public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb)
    {
        return this.getEntitiesInAABBexcluding(entityIn, bb, EntitySelectors.NOT_SPECTATING);
    }

    public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox, Predicate <? super Entity > predicate)
    {
        List<Entity> list = Lists.<Entity>newArrayList();
        int i = MathHelper.floor_double((boundingBox.minX - 2.0D) / 16.0D);
        int j = MathHelper.floor_double((boundingBox.maxX + 2.0D) / 16.0D);
        int k = MathHelper.floor_double((boundingBox.minZ - 2.0D) / 16.0D);
        int l = MathHelper.floor_double((boundingBox.maxZ + 2.0D) / 16.0D);

        for (int index = i; index <= j; ++index)
        {
            for (int innerIndex = k; innerIndex <= l; ++innerIndex)
            {
                if (this.isChunkLoaded(index, innerIndex, true))
                {
                    this.getChunkFromChunkCoords(index, innerIndex).getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
                }
            }
        }

        return list;
    }

    public <T extends Entity> List<T> getEntities(Class <? extends T > entityType, Predicate <? super T > filter)
    {
        List<T> list = Lists.<T>newArrayList();

        for (Entity entity : this.loadedEntityList)
        {
            if (entityType.isAssignableFrom(entity.getClass()) && filter.apply((T)entity))
            {
                list.add((T)entity);
            }
        }

        return list;
    }

    public <T extends Entity> List<T> getPlayers(Class <? extends T > playerType, Predicate <? super T > filter)
    {
        List<T> list = Lists.<T>newArrayList();

        for (Entity entity : this.playerEntities)
        {
            if (playerType.isAssignableFrom(entity.getClass()) && filter.apply((T)entity))
            {
                list.add((T)entity);
            }
        }

        return list;
    }

    public <T extends Entity> List<T> getEntitiesWithinAABB(Class <? extends T > classEntity, AxisAlignedBB bb)
    {
        return this.<T>getEntitiesWithinAABB(classEntity, bb, EntitySelectors.NOT_SPECTATING);
    }

    public <T extends Entity> List<T> getEntitiesWithinAABB(Class <? extends T > clazz, AxisAlignedBB aabb, Predicate <? super T > filter)
    {
        int i = MathHelper.floor_double((aabb.minX - 2.0D) / 16.0D);
        int j = MathHelper.floor_double((aabb.maxX + 2.0D) / 16.0D);
        int k = MathHelper.floor_double((aabb.minZ - 2.0D) / 16.0D);
        int l = MathHelper.floor_double((aabb.maxZ + 2.0D) / 16.0D);
        List<T> list = Lists.<T>newArrayList();

        for (int index = i; index <= j; ++index)
        {
            for (int innerIndex = k; innerIndex <= l; ++innerIndex)
            {
                if (this.isChunkLoaded(index, innerIndex, true))
                {
                    this.getChunkFromChunkCoords(index, innerIndex).getEntitiesOfTypeWithinAAAB(clazz, aabb, list, filter);
                }
            }
        }

        return list;
    }

    public <T extends Entity> T findNearestEntityWithinAABB(Class <? extends T > entityType, AxisAlignedBB aabb, T closestTo)
    {
        List<T> list = this.<T>getEntitiesWithinAABB(entityType, aabb);
        T t = null;
        double doubleValue = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); ++i)
        {
            T localValue = list.get(i);

            if (localValue != closestTo && EntitySelectors.NOT_SPECTATING.apply(localValue))
            {
                double doubleValue2 = closestTo.getDistanceSqToEntity(localValue);

                if (doubleValue2 <= doubleValue)
                {
                    t = localValue;
                    doubleValue = doubleValue2;
                }
            }
        }

        return t;
    }

    public Entity getEntityByID(int id)
    {
        return (Entity)this.entitiesById.lookup(id);
    }

    public List<Entity> getLoadedEntityList()
    {
        return this.loadedEntityList;
    }

    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity)
    {
        if (this.isBlockLoaded(pos))
        {
            this.getChunkFromBlockCoords(pos).setChunkModified();
        }
    }

    public int countEntities(Class<?> entityType)
    {
        int i = 0;

        for (Entity entity : this.loadedEntityList)
        {
            if ((!(entity instanceof EntityLiving) || !((EntityLiving)entity).isNoDespawnRequired()) && entityType.isAssignableFrom(entity.getClass()))
            {
                ++i;
            }
        }

        return i;
    }

    public void loadEntities(Collection<Entity> entityCollection)
    {
        this.loadedEntityList.addAll(entityCollection);

        for (Entity entity : entityCollection)
        {
            this.onEntityAdded(entity);
        }
    }

    public void unloadEntities(Collection<Entity> entityCollection)
    {
        this.unloadedEntityList.addAll(entityCollection);
    }

    public boolean canBlockBePlaced(Block blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing side, Entity entityIn, ItemStack itemStackIn)
    {
        Block block = this.getBlockState(pos).getBlock();
        AxisAlignedBB axisAlignedBB = skipCollisionCheck ? null : blockIn.getCollisionBoundingBox(this, pos, blockIn.getDefaultState());
        return axisAlignedBB != null && !this.checkNoEntityCollision(axisAlignedBB, entityIn) ? false : (block.getMaterial() == Material.circuits && blockIn == Blocks.anvil ? true : block.getMaterial().isReplaceable() && blockIn.canReplace(this, pos, side, itemStackIn));
    }

    public int getSeaLevel()
    {
        return this.seaLevel;
    }

    public void setSeaLevel(int seaLevel)
    {
        this.seaLevel = seaLevel;
    }

    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        IBlockState iblockstate = this.getBlockState(pos);
        return iblockstate.getBlock().getStrongPower(this, pos, iblockstate, direction);
    }

    public WorldType getWorldType()
    {
        return this.worldInfo.getTerrainType();
    }

    public int getStrongPower(BlockPos pos)
    {
        int i = 0;
        i = Math.max(i, this.getStrongPower(pos.down(), EnumFacing.DOWN));

        if (i >= 15)
        {
            return i;
        }
        else
        {
            i = Math.max(i, this.getStrongPower(pos.up(), EnumFacing.UP));

            if (i >= 15)
            {
                return i;
            }
            else
            {
                i = Math.max(i, this.getStrongPower(pos.north(), EnumFacing.NORTH));

                if (i >= 15)
                {
                    return i;
                }
                else
                {
                    i = Math.max(i, this.getStrongPower(pos.south(), EnumFacing.SOUTH));

                    if (i >= 15)
                    {
                        return i;
                    }
                    else
                    {
                        i = Math.max(i, this.getStrongPower(pos.west(), EnumFacing.WEST));

                        if (i >= 15)
                        {
                            return i;
                        }
                        else
                        {
                            i = Math.max(i, this.getStrongPower(pos.east(), EnumFacing.EAST));
                            return i >= 15 ? i : i;
                        }
                    }
                }
            }
        }
    }

    public boolean isSidePowered(BlockPos pos, EnumFacing side)
    {
        return this.getRedstonePower(pos, side) > 0;
    }

    public int getRedstonePower(BlockPos pos, EnumFacing facing)
    {
        IBlockState iblockstate = this.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block.isNormalCube() ? this.getStrongPower(pos) : block.getWeakPower(this, pos, iblockstate, facing);
    }

    public boolean isBlockPowered(BlockPos pos)
    {
        return this.getRedstonePower(pos.down(), EnumFacing.DOWN) > 0 ? true : (this.getRedstonePower(pos.up(), EnumFacing.UP) > 0 ? true : (this.getRedstonePower(pos.north(), EnumFacing.NORTH) > 0 ? true : (this.getRedstonePower(pos.south(), EnumFacing.SOUTH) > 0 ? true : (this.getRedstonePower(pos.west(), EnumFacing.WEST) > 0 ? true : this.getRedstonePower(pos.east(), EnumFacing.EAST) > 0))));
    }

    public int isBlockIndirectlyGettingPowered(BlockPos pos)
    {
        int i = 0;

        for (EnumFacing enumfacing : EnumFacing.VALUES)
        {
            int j = this.getRedstonePower(pos.offset(enumfacing), enumfacing);

            if (j >= 15)
            {
                return 15;
            }

            if (j > i)
            {
                i = j;
            }
        }

        return i;
    }

    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance)
    {
        return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance);
    }

    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance)
    {
        double doubleValue = -1.0D;
        EntityPlayer entityPlayer = null;

        for (int i = 0; i < this.playerEntities.size(); ++i)
        {
            EntityPlayer entityplayer1 = this.playerEntities.get(i);

            if (EntitySelectors.NOT_SPECTATING.apply(entityplayer1))
            {
                double doubleValue2 = entityplayer1.getDistanceSq(x, y, z);

                if ((distance < 0.0D || doubleValue2 < distance * distance) && (doubleValue == -1.0D || doubleValue2 < doubleValue))
                {
                    doubleValue = doubleValue2;
                    entityPlayer = entityplayer1;
                }
            }
        }

        return entityPlayer;
    }

    public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range)
    {
        for (int i = 0; i < this.playerEntities.size(); ++i)
        {
            EntityPlayer entityPlayer = this.playerEntities.get(i);

            if (EntitySelectors.NOT_SPECTATING.apply(entityPlayer))
            {
                double doubleValue = entityPlayer.getDistanceSq(x, y, z);

                if (range < 0.0D || doubleValue < range * range)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public EntityPlayer getPlayerEntityByName(String name)
    {
        for (int i = 0; i < this.playerEntities.size(); ++i)
        {
            EntityPlayer entityPlayer = this.playerEntities.get(i);

            if (name.equals(entityPlayer.getName()))
            {
                return entityPlayer;
            }
        }

        return null;
    }

    public EntityPlayer getPlayerEntityByUUID(UUID uuid)
    {
        for (int i = 0; i < this.playerEntities.size(); ++i)
        {
            EntityPlayer entityPlayer = this.playerEntities.get(i);

            if (uuid.equals(entityPlayer.getUniqueID()))
            {
                return entityPlayer;
            }
        }

        return null;
    }

    public void sendQuittingDisconnectingPacket()
    {
    }

    public void checkSessionLock() throws MinecraftException
    {
        this.saveHandler.checkSessionLock();
    }

    public void setTotalWorldTime(long worldTime)
    {
        this.worldInfo.setWorldTotalTime(worldTime);
    }

    public long getSeed()
    {
        return this.worldInfo.getSeed();
    }

    public long getTotalWorldTime()
    {
        return this.worldInfo.getWorldTotalTime();
    }

    public long getWorldTime()
    {
        return this.worldInfo.getWorldTime();
    }

    public void setWorldTime(long time)
    {
        this.worldInfo.setWorldTime(time);
    }

    public BlockPos getSpawnPoint()
    {
        BlockPos blockPos = new BlockPos(this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());

        if (!this.getWorldBorder().contains(blockPos))
        {
            blockPos = this.getHeight(new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
        }

        return blockPos;
    }

    public void setSpawnPoint(BlockPos pos)
    {
        this.worldInfo.setSpawn(pos);
    }

    public void joinEntityInSurroundings(Entity entityIn)
    {
        int i = MathHelper.floor_double(entityIn.posX / 16.0D);
        int j = MathHelper.floor_double(entityIn.posZ / 16.0D);
        int k = 2;

        for (int l = i - k; l <= i + k; ++l)
        {
            for (int index = j - k; index <= j + k; ++index)
            {
                this.getChunkFromChunkCoords(l, index);
            }
        }

        if (!this.loadedEntityList.contains(entityIn))
        {
            this.loadedEntityList.add(entityIn);
        }
    }

    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos)
    {
        return true;
    }

    public void setEntityState(Entity entityIn, byte state)
    {
    }

    public IChunkProvider getChunkProvider()
    {
        return this.chunkProvider;
    }

    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam)
    {
        blockIn.onBlockEventReceived(this, pos, this.getBlockState(pos), eventID, eventParam);
    }

    public ISaveHandler getSaveHandler()
    {
        return this.saveHandler;
    }

    public WorldInfo getWorldInfo()
    {
        return this.worldInfo;
    }

    public GameRules getGameRules()
    {
        return this.worldInfo.getGameRulesInstance();
    }

    public void updateAllPlayersSleepingFlag()
    {
    }

    public float getThunderStrength(float delta)
    {
        return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * delta) * this.getRainStrength(delta);
    }

    public void setThunderStrength(float strength)
    {
        this.prevThunderingStrength = strength;
        this.thunderingStrength = strength;
    }

    public float getRainStrength(float delta)
    {
        return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * delta;
    }

    public void setRainStrength(float strength)
    {
        this.prevRainingStrength = strength;
        this.rainingStrength = strength;
    }

    public boolean isThundering()
    {
        return (double)this.getThunderStrength(1.0F) > 0.9D;
    }

    public boolean isRaining()
    {
        return (double)this.getRainStrength(1.0F) > 0.2D;
    }

    public boolean isRainingAt(BlockPos strikePosition)
    {
        if (!this.isRaining())
        {
            return false;
        }
        else if (!this.canSeeSky(strikePosition))
        {
            return false;
        }
        else if (this.getPrecipitationHeight(strikePosition).getY() > strikePosition.getY())
        {
            return false;
        }
        else
        {
            BiomeGenBase biomeGenBase = this.getBiomeGenForCoords(strikePosition);
            return biomeGenBase.getEnableSnow() ? false : (this.canSnowAt(strikePosition, false) ? false : biomeGenBase.canRain());
        }
    }

    public boolean isBlockinHighHumidity(BlockPos pos)
    {
        BiomeGenBase biomeGenBase = this.getBiomeGenForCoords(pos);
        return biomeGenBase.isHighHumidity();
    }

    public MapStorage getMapStorage()
    {
        return this.mapStorage;
    }

    public void setItemData(String dataID, WorldSavedData worldSavedDataIn)
    {
        this.mapStorage.setData(dataID, worldSavedDataIn);
    }

    public WorldSavedData loadItemData(Class <? extends WorldSavedData > clazz, String dataID)
    {
        return this.mapStorage.loadData(clazz, dataID);
    }

    public int getUniqueDataId(String key)
    {
        return this.mapStorage.getUniqueDataId(key);
    }

    public void playBroadcastSound(int soundId, BlockPos pos, int data)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            this.worldAccesses.get(i).broadcastSound(soundId, pos, data);
        }
    }

    public void playAuxSFX(int sfxType, BlockPos pos, int data)
    {
        this.playAuxSFXAtEntity((EntityPlayer)null, sfxType, pos, data);
    }

    public void playAuxSFXAtEntity(EntityPlayer player, int sfxType, BlockPos pos, int data)
    {
        try
        {
            for (int i = 0; i < this.worldAccesses.size(); ++i)
            {
                this.worldAccesses.get(i).playAuxSFX(player, sfxType, pos, data);
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Playing level event");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Level event being played");
            crashReportCategory.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(pos));
            crashReportCategory.addCrashSection("Event source", player);
            crashReportCategory.addCrashSection("Event type", Integer.valueOf(sfxType));
            crashReportCategory.addCrashSection("Event data", Integer.valueOf(data));
            throw new ReportedException(crashReport);
        }
    }

    public int getHeight()
    {
        return 256;
    }

    public int getActualHeight()
    {
        return this.provider.getHasNoSky() ? 128 : 256;
    }

    public Random setRandomSeed(int x, int y, int z)
    {
        long i = (long)x * 341873128712L + (long)y * 132897987541L + this.getWorldInfo().getSeed() + (long)z;
        this.rand.setSeed(i);
        return this.rand;
    }

    public BlockPos getStrongholdPos(String name, BlockPos pos)
    {
        return this.getChunkProvider().getStrongholdGen(this, name, pos);
    }

    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    public double getHorizon()
    {
        return this.worldInfo.getTerrainType() == WorldType.FLAT ? 0.0D : 63.0D;
    }

    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report)
    {
        CrashReportCategory crashReportCategory = report.makeCategoryDepth("Affected level", 1);
        crashReportCategory.addCrashSection("Level name", this.worldInfo == null ? "????" : this.worldInfo.getWorldName());
        crashReportCategory.addCrashSectionCallable("All players", new Callable<String>()
        {
            public String call()
            {
                return World.this.playerEntities.size() + " total; " + World.this.playerEntities.toString();
            }
        });
        crashReportCategory.addCrashSectionCallable("Chunk stats", new Callable<String>()
        {
            public String call()
            {
                return World.this.chunkProvider.makeString();
            }
        });

        try
        {
            this.worldInfo.addToCrashReport(crashReportCategory);
        }
        catch (Throwable throwable)
        {
            crashReportCategory.addCrashSectionThrowable("Level Data Unobtainable", throwable);
        }

        return crashReportCategory;
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        for (int i = 0; i < this.worldAccesses.size(); ++i)
        {
            IWorldAccess iworldaccess = this.worldAccesses.get(i);
            iworldaccess.sendBlockBreakProgress(breakerId, pos, progress);
        }
    }

    public Calendar getCurrentDate()
    {
        if (this.getTotalWorldTime() % 600L == 0L)
        {
            this.theCalendar.setTimeInMillis(MinecraftServer.getCurrentTimeMillis());
        }

        return this.theCalendar;
    }

    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compund)
    {
    }

    public Scoreboard getScoreboard()
    {
        return this.worldScoreboard;
    }

    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn)
    {
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos blockPos = pos.offset(enumfacing);

            if (this.isBlockLoaded(blockPos))
            {
                IBlockState iblockstate = this.getBlockState(blockPos);

                if (Blocks.unpowered_comparator.isAssociated(iblockstate.getBlock()))
                {
                    iblockstate.getBlock().onNeighborBlockChange(this, blockPos, iblockstate, blockIn);
                }
                else if (iblockstate.getBlock().isNormalCube())
                {
                    blockPos = blockPos.offset(enumfacing);
                    iblockstate = this.getBlockState(blockPos);

                    if (Blocks.unpowered_comparator.isAssociated(iblockstate.getBlock()))
                    {
                        iblockstate.getBlock().onNeighborBlockChange(this, blockPos, iblockstate, blockIn);
                    }
                }
            }
        }
    }

    public DifficultyInstance getDifficultyForLocation(BlockPos pos)
    {
        long i = 0L;
        float f = 0.0F;

        if (this.isBlockLoaded(pos))
        {
            f = this.getCurrentMoonPhaseFactor();
            i = this.getChunkFromBlockCoords(pos).getInhabitedTime();
        }

        return new DifficultyInstance(this.getDifficulty(), this.getWorldTime(), i, f);
    }

    public EnumDifficulty getDifficulty()
    {
        return this.getWorldInfo().getDifficulty();
    }

    public int getSkylightSubtracted()
    {
        return this.skylightSubtracted;
    }

    public void setSkylightSubtracted(int newSkylightSubtracted)
    {
        this.skylightSubtracted = newSkylightSubtracted;
    }

    public int getLastLightningBolt()
    {
        return this.lastLightningBolt;
    }

    public void setLastLightningBolt(int lastLightningBoltIn)
    {
        this.lastLightningBolt = lastLightningBoltIn;
    }

    public boolean isFindingSpawnPoint()
    {
        return this.findingSpawnPoint;
    }

    public VillageCollection getVillageCollection()
    {
        return this.villageCollectionObj;
    }

    public WorldBorder getWorldBorder()
    {
        return this.worldBorder;
    }

    public boolean isSpawnChunk(int x, int z)
    {
        BlockPos blockPos = this.getSpawnPoint();
        int i = x * 16 + 8 - blockPos.getX();
        int j = z * 16 + 8 - blockPos.getZ();
        int k = 128;
        return i >= -k && i <= k && j >= -k && j <= k;
    }
}
