package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldServer extends World implements IThreadListener
{
    private static final Logger logger = LogManager.getLogger();
    private final MinecraftServer mcServer;
    private final EntityTracker theEntityTracker;
    private final PlayerManager thePlayerManager;
    private final Set<NextTickListEntry> pendingTickListEntriesHashSet = Sets.<NextTickListEntry>newHashSet();
    private final TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet = new TreeSet();
    private final Map<UUID, Entity> entitiesByUuid = Maps.<UUID, Entity>newHashMap();
    public ChunkProviderServer theChunkProviderServer;
    public boolean disableLevelSaving;
    private boolean allPlayersSleeping;
    private int updateEntityTick;
    private final Teleporter worldTeleporter;
    private final SpawnerAnimals mobSpawner = new SpawnerAnimals();
    protected final VillageSiege villageSiege = new VillageSiege(this);
    private WorldServer.ServerBlockEventList[] blockEventQueue = new WorldServer.ServerBlockEventList[] {new WorldServer.ServerBlockEventList(), new WorldServer.ServerBlockEventList()};
    private int blockEventCacheIndex;
    private static final List<WeightedRandomChestContent> bonusChestContent = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.stick, 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.planks), 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log), 0, 1, 3, 10), new WeightedRandomChestContent(Items.stone_axe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_axe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.stone_pickaxe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.apple, 0, 2, 3, 5), new WeightedRandomChestContent(Items.bread, 0, 2, 3, 3), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log2), 0, 1, 3, 10)});
    private List<NextTickListEntry> pendingTickListEntriesThisTick = Lists.<NextTickListEntry>newArrayList();

    public WorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn)
    {
        super(saveHandlerIn, info, WorldProvider.getProviderForDimension(dimensionId), profilerIn, false);
        this.mcServer = server;
        this.theEntityTracker = new EntityTracker(this);
        this.thePlayerManager = new PlayerManager(this);
        this.provider.registerWorld(this);
        this.chunkProvider = this.createChunkProvider();
        this.worldTeleporter = new Teleporter(this);
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.getWorldBorder().setSize(server.getMaxWorldSize());
    }

    public World init()
    {
        this.mapStorage = new MapStorage(this.saveHandler);
        String villageDataName = VillageCollection.fileNameForProvider(this.provider);
        VillageCollection villageCollection = (VillageCollection)this.mapStorage.loadData(VillageCollection.class, villageDataName);

        if (villageCollection == null)
        {
            this.villageCollectionObj = new VillageCollection(this);
            this.mapStorage.setData(villageDataName, this.villageCollectionObj);
        }
        else
        {
            this.villageCollectionObj = villageCollection;
            this.villageCollectionObj.setWorldsForAll(this);
        }

        this.worldScoreboard = new ServerScoreboard(this.mcServer);
        ScoreboardSaveData scoreboardSaveData = (ScoreboardSaveData)this.mapStorage.loadData(ScoreboardSaveData.class, "scoreboard");

        if (scoreboardSaveData == null)
        {
            scoreboardSaveData = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", scoreboardSaveData);
        }

        scoreboardSaveData.setScoreboard(this.worldScoreboard);
        ((ServerScoreboard)this.worldScoreboard).setScoreboardSaveData(scoreboardSaveData);
        this.getWorldBorder().setCenter(this.worldInfo.getBorderCenterX(), this.worldInfo.getBorderCenterZ());
        this.getWorldBorder().setDamageAmount(this.worldInfo.getBorderDamagePerBlock());
        this.getWorldBorder().setDamageBuffer(this.worldInfo.getBorderSafeZone());
        this.getWorldBorder().setWarningDistance(this.worldInfo.getBorderWarningDistance());
        this.getWorldBorder().setWarningTime(this.worldInfo.getBorderWarningTime());

        if (this.worldInfo.getBorderLerpTime() > 0L)
        {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize(), this.worldInfo.getBorderLerpTarget(), this.worldInfo.getBorderLerpTime());
        }
        else
        {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize());
        }

        return this;
    }

    public void tick()
    {
        super.tick();

        if (this.getWorldInfo().isHardcoreModeEnabled() && this.getDifficulty() != EnumDifficulty.HARD)
        {
            this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
        }

        this.provider.getWorldChunkManager().cleanupCache();

        if (this.areAllPlayersAsleep())
        {
            if (this.getGameRules().getBoolean("doDaylightCycle"))
            {
                long nextDayTime = this.worldInfo.getWorldTime() + 24000L;
                this.worldInfo.setWorldTime(nextDayTime - nextDayTime % 24000L);
            }

            this.wakeAllPlayers();
        }

        this.theProfiler.startSection("mobSpawner");

        if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD)
        {
            this.mobSpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
        }

        this.theProfiler.endStartSection("chunkSource");
        this.chunkProvider.unloadQueuedChunks();
        int newSkylightSubtracted = this.calculateSkylightSubtracted(1.0F);

        if (newSkylightSubtracted != this.getSkylightSubtracted())
        {
            this.setSkylightSubtracted(newSkylightSubtracted);
        }

        this.worldInfo.setWorldTotalTime(this.worldInfo.getWorldTotalTime() + 1L);

        if (this.getGameRules().getBoolean("doDaylightCycle"))
        {
            this.worldInfo.setWorldTime(this.worldInfo.getWorldTime() + 1L);
        }

        this.theProfiler.endStartSection("tickPending");
        this.tickUpdates(false);
        this.theProfiler.endStartSection("tickBlocks");
        this.updateBlocks();
        this.theProfiler.endStartSection("chunkMap");
        this.thePlayerManager.updatePlayerInstances();
        this.theProfiler.endStartSection("village");
        this.villageCollectionObj.tick();
        this.villageSiege.tick();
        this.theProfiler.endStartSection("portalForcer");
        this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());
        this.theProfiler.endSection();
        this.sendQueuedBlockEvents();
    }

    public BiomeGenBase.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType creatureType, BlockPos pos)
    {
        List<BiomeGenBase.SpawnListEntry> possibleSpawns = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
        return possibleSpawns != null && !possibleSpawns.isEmpty() ? (BiomeGenBase.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, possibleSpawns) : null;
    }

    public boolean canCreatureTypeSpawnHere(EnumCreatureType creatureType, BiomeGenBase.SpawnListEntry spawnListEntry, BlockPos pos)
    {
        List<BiomeGenBase.SpawnListEntry> possibleSpawns = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
        return possibleSpawns != null && !possibleSpawns.isEmpty() ? possibleSpawns.contains(spawnListEntry) : false;
    }

    public void updateAllPlayersSleepingFlag()
    {
        this.allPlayersSleeping = false;

        if (!this.playerEntities.isEmpty())
        {
            int spectatorCount = 0;
            int sleepingCount = 0;

            for (EntityPlayer entityPlayer : this.playerEntities)
            {
                if (entityPlayer.isSpectator())
                {
                    ++spectatorCount;
                }
                else if (entityPlayer.isPlayerSleeping())
                {
                    ++sleepingCount;
                }
            }

            this.allPlayersSleeping = sleepingCount > 0 && sleepingCount >= this.playerEntities.size() - spectatorCount;
        }
    }

    protected void wakeAllPlayers()
    {
        this.allPlayersSleeping = false;

        for (EntityPlayer entityPlayer : this.playerEntities)
        {
            if (entityPlayer.isPlayerSleeping())
            {
                entityPlayer.wakeUpPlayer(false, false, true);
            }
        }

        this.resetRainAndThunder();
    }

    private void resetRainAndThunder()
    {
        this.worldInfo.setRainTime(0);
        this.worldInfo.setRaining(false);
        this.worldInfo.setThunderTime(0);
        this.worldInfo.setThundering(false);
    }

    public boolean areAllPlayersAsleep()
    {
        if (this.allPlayersSleeping && !this.isRemote)
        {
            for (EntityPlayer entityPlayer : this.playerEntities)
            {
                if (entityPlayer.isSpectator() || !entityPlayer.isPlayerFullyAsleep())
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public void setInitialSpawnLocation()
    {
        if (this.worldInfo.getSpawnY() <= 0)
        {
            this.worldInfo.setSpawnY(this.getSeaLevel() + 1);
        }

        int spawnX = this.worldInfo.getSpawnX();
        int spawnZ = this.worldInfo.getSpawnZ();
        int attempts = 0;
        BlockPos.MutableBlockPos spawnCheckPos = new BlockPos.MutableBlockPos(spawnX, 0, spawnZ);

        while (this.getGroundAboveSeaLevel(spawnCheckPos.set(spawnX, 0, spawnZ)).getMaterial() == Material.air)
        {
            spawnX += this.rand.nextInt(8) - this.rand.nextInt(8);
            spawnZ += this.rand.nextInt(8) - this.rand.nextInt(8);
            ++attempts;

            if (attempts == 10000)
            {
                break;
            }
        }

        this.worldInfo.setSpawnX(spawnX);
        this.worldInfo.setSpawnZ(spawnZ);
    }

    protected void updateBlocks()
    {
        super.updateBlocks();

        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            for (ChunkCoordIntPair activeChunk : this.activeChunkSet)
            {
                this.getChunkFromChunkCoords(activeChunk.chunkXPos, activeChunk.chunkZPos).onTick(false);
            }
        }
        else
        {
            int randomTickedBlocks = 0;
            int randomTickAttempts = 0;

            for (ChunkCoordIntPair activeChunk : this.activeChunkSet)
            {
                int chunkStartX = activeChunk.chunkXPos * 16;
                int chunkStartZ = activeChunk.chunkZPos * 16;
                this.theProfiler.startSection("getChunk");
                Chunk chunk = this.getChunkFromChunkCoords(activeChunk.chunkXPos, activeChunk.chunkZPos);
                this.playMoodSoundAndCheckLight(chunkStartX, chunkStartZ, chunk);
                this.theProfiler.endStartSection("tickChunk");
                chunk.onTick(false);
                this.theProfiler.endStartSection("thunder");

                if (this.rand.nextInt(100000) == 0 && this.isRaining() && this.isThundering())
                {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int thunderRandomBits = this.updateLCG >> 2;
                    BlockPos lightningPos = this.adjustPosToNearbyEntity(new BlockPos(chunkStartX + (thunderRandomBits & 15), 0, chunkStartZ + (thunderRandomBits >> 8 & 15)));

                    if (this.isRainingAt(lightningPos))
                    {
                        this.addWeatherEffect(new EntityLightningBolt(this, (double)lightningPos.getX(), (double)lightningPos.getY(), (double)lightningPos.getZ()));
                    }
                }

                this.theProfiler.endStartSection("iceandsnow");

                if (this.rand.nextInt(16) == 0)
                {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int precipitationRandomBits = this.updateLCG >> 2;
                    BlockPos precipitationPos = this.getPrecipitationHeight(new BlockPos(chunkStartX + (precipitationRandomBits & 15), 0, chunkStartZ + (precipitationRandomBits >> 8 & 15)));
                    BlockPos blockBelowPrecipitation = precipitationPos.down();

                    if (this.canBlockFreezeNoWater(blockBelowPrecipitation))
                    {
                        this.setBlockState(blockBelowPrecipitation, Blocks.ice.getDefaultState());
                    }

                    if (this.isRaining() && this.canSnowAt(precipitationPos, true))
                    {
                        this.setBlockState(precipitationPos, Blocks.snow_layer.getDefaultState());
                    }

                    if (this.isRaining() && this.getBiomeGenForCoords(blockBelowPrecipitation).canRain())
                    {
                        this.getBlockState(blockBelowPrecipitation).getBlock().fillWithRain(this, blockBelowPrecipitation);
                    }
                }

                this.theProfiler.endStartSection("tickBlocks");
                int randomTickSpeed = this.getGameRules().getInt("randomTickSpeed");

                if (randomTickSpeed > 0)
                {
                            BlockPos.MutableBlockPos randomTickPos = new BlockPos.MutableBlockPos();
                    for (ExtendedBlockStorage extendedBlockStorage : chunk.getBlockStorageArray())
                    {
                        if (extendedBlockStorage != null && extendedBlockStorage.getNeedsRandomTick())
                        {
                            for (int randomTickIndex = 0; randomTickIndex < randomTickSpeed; ++randomTickIndex)
                            {
                                this.updateLCG = this.updateLCG * 3 + 1013904223;
                                int randomTickBits = this.updateLCG >> 2;
                                int randomTickX = randomTickBits & 15;
                                int randomTickZ = randomTickBits >> 8 & 15;
                                int randomTickY = randomTickBits >> 16 & 15;
                                ++randomTickAttempts;
                                IBlockState blockState = extendedBlockStorage.get(randomTickX, randomTickY, randomTickZ);
                                Block block = blockState.getBlock();

                                if (block.getTickRandomly())
                                {
                                    ++randomTickedBlocks;
                                    randomTickPos.set(randomTickX + chunkStartX, randomTickY + extendedBlockStorage.getYLocation(), randomTickZ + chunkStartZ);
                                    block.randomTick(this, randomTickPos, blockState, this.rand);
                                }
                            }
                        }
                    }
                }

                this.theProfiler.endSection();
            }
        }
    }

    protected BlockPos adjustPosToNearbyEntity(BlockPos pos)
    {
        BlockPos blockPos = this.getPrecipitationHeight(pos);
        AxisAlignedBB searchBox = (new AxisAlignedBB(blockPos, new BlockPos(blockPos.getX(), this.getHeight(), blockPos.getZ()))).expand(3.0D, 3.0D, 3.0D);
        List<EntityLivingBase> nearbyEntities = this.getEntitiesWithinAABB(EntityLivingBase.class, searchBox, new Predicate<EntityLivingBase>()
        {
            public boolean apply(EntityLivingBase entity)
            {
                return entity != null && entity.isEntityAlive() && WorldServer.this.canSeeSky(entity.getPosition());
            }
        });
        return !nearbyEntities.isEmpty() ? ((EntityLivingBase)nearbyEntities.get(this.rand.nextInt(nearbyEntities.size()))).getPosition() : blockPos;
    }

    public boolean isBlockTickPending(BlockPos pos, Block blockType)
    {
        NextTickListEntry nextTickListEntry = new NextTickListEntry(pos, blockType);
        return this.pendingTickListEntriesThisTick.contains(nextTickListEntry);
    }

    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay)
    {
        this.updateBlockTick(pos, blockIn, delay, 0);
    }

    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority)
    {
        NextTickListEntry nextTickListEntry = new NextTickListEntry(pos, blockIn);
        int checkRadius = 0;

        if (this.scheduledUpdatesAreImmediate && blockIn.getMaterial() != Material.air)
        {
            if (blockIn.requiresUpdates())
            {
                checkRadius = 8;

                if (this.isAreaLoaded(nextTickListEntry.position.add(-checkRadius, -checkRadius, -checkRadius), nextTickListEntry.position.add(checkRadius, checkRadius, checkRadius)))
                {
                    IBlockState blockState = this.getBlockState(nextTickListEntry.position);

                    if (blockState.getBlock().getMaterial() != Material.air && blockState.getBlock() == nextTickListEntry.getBlock())
                    {
                        blockState.getBlock().updateTick(this, nextTickListEntry.position, blockState, this.rand);
                    }
                }

                return;
            }

            delay = 1;
        }

        if (this.isAreaLoaded(pos.add(-checkRadius, -checkRadius, -checkRadius), pos.add(checkRadius, checkRadius, checkRadius)))
        {
            if (blockIn.getMaterial() != Material.air)
            {
                nextTickListEntry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
                nextTickListEntry.setPriority(priority);
            }

            if (!this.pendingTickListEntriesHashSet.contains(nextTickListEntry))
            {
                this.pendingTickListEntriesHashSet.add(nextTickListEntry);
                this.pendingTickListEntriesTreeSet.add(nextTickListEntry);
            }
        }
    }

    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority)
    {
        NextTickListEntry nextTickListEntry = new NextTickListEntry(pos, blockIn);
        nextTickListEntry.setPriority(priority);

        if (blockIn.getMaterial() != Material.air)
        {
            nextTickListEntry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
        }

        if (!this.pendingTickListEntriesHashSet.contains(nextTickListEntry))
        {
            this.pendingTickListEntriesHashSet.add(nextTickListEntry);
            this.pendingTickListEntriesTreeSet.add(nextTickListEntry);
        }
    }

    public void updateEntities()
    {
        if (this.playerEntities.isEmpty())
        {
            if (this.updateEntityTick++ >= 1200)
            {
                return;
            }
        }
        else
        {
            this.resetUpdateEntityTick();
        }

        super.updateEntities();
    }

    public void resetUpdateEntityTick()
    {
        this.updateEntityTick = 0;
    }

    public boolean tickUpdates(boolean runAllPending)
    {
        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            return false;
        }
        else
        {
            int pendingTickCount = this.pendingTickListEntriesTreeSet.size();

            if (pendingTickCount != this.pendingTickListEntriesHashSet.size())
            {
                throw new IllegalStateException("TickNextTick list out of synch");
            }
            else
            {
                if (pendingTickCount > 1000)
                {
                    pendingTickCount = 1000;
                }

                this.theProfiler.startSection("cleaning");

                for (int tickIndex = 0; tickIndex < pendingTickCount; ++tickIndex)
                {
                    NextTickListEntry nextTickEntry = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();

                    if (!runAllPending && nextTickEntry.scheduledTime > this.worldInfo.getWorldTotalTime())
                    {
                        break;
                    }

                    this.pendingTickListEntriesTreeSet.remove(nextTickEntry);
                    this.pendingTickListEntriesHashSet.remove(nextTickEntry);
                    this.pendingTickListEntriesThisTick.add(nextTickEntry);
                }

                this.theProfiler.endSection();
                this.theProfiler.startSection("ticking");
                Iterator<NextTickListEntry> iterator = this.pendingTickListEntriesThisTick.iterator();

                while (iterator.hasNext())
                {
                    NextTickListEntry nextTickEntry = (NextTickListEntry)iterator.next();
                    iterator.remove();
                    int checkRadius = 0;

                    if (this.isAreaLoaded(nextTickEntry.position.add(-checkRadius, -checkRadius, -checkRadius), nextTickEntry.position.add(checkRadius, checkRadius, checkRadius)))
                    {
                        IBlockState blockState = this.getBlockState(nextTickEntry.position);

                        if (blockState.getBlock().getMaterial() != Material.air && Block.isEqualTo(blockState.getBlock(), nextTickEntry.getBlock()))
                        {
                            try
                            {
                                blockState.getBlock().updateTick(this, nextTickEntry.position, blockState, this.rand);
                            }
                            catch (Throwable throwable)
                            {
                                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception while ticking a block");
                                CrashReportCategory crashReportCategory = crashReport.makeCategory("Block being ticked");
                                CrashReportCategory.addBlockInfo(crashReportCategory, nextTickEntry.position, blockState);
                                throw new ReportedException(crashReport);
                            }
                        }
                    }
                    else
                    {
                        this.scheduleUpdate(nextTickEntry.position, nextTickEntry.getBlock(), 0);
                    }
                }

                this.theProfiler.endSection();
                this.pendingTickListEntriesThisTick.clear();
                return !this.pendingTickListEntriesTreeSet.isEmpty();
            }
        }
    }

    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove)
    {
        ChunkCoordIntPair chunkCoordIntPair = chunkIn.getChunkCoordIntPair();
        int minX = (chunkCoordIntPair.chunkXPos << 4) - 2;
        int maxX = minX + 16 + 2;
        int minZ = (chunkCoordIntPair.chunkZPos << 4) - 2;
        int maxZ = minZ + 16 + 2;
        return this.getPendingBlockUpdatesInArea(new StructureBoundingBox(minX, 0, minZ, maxX, 256, maxZ), remove);
    }

    public List<NextTickListEntry> getPendingBlockUpdatesInArea(StructureBoundingBox structureBB, boolean remove)
    {
        List<NextTickListEntry> pendingUpdates = null;

        for (int queueIndex = 0; queueIndex < 2; ++queueIndex)
        {
            Iterator<NextTickListEntry> iterator;

            if (queueIndex == 0)
            {
                iterator = this.pendingTickListEntriesTreeSet.iterator();
            }
            else
            {
                iterator = this.pendingTickListEntriesThisTick.iterator();
            }

            while (iterator.hasNext())
            {
                NextTickListEntry nextTickEntry = (NextTickListEntry)iterator.next();
                BlockPos tickPos = nextTickEntry.position;

                if (tickPos.getX() >= structureBB.minX && tickPos.getX() < structureBB.maxX && tickPos.getZ() >= structureBB.minZ && tickPos.getZ() < structureBB.maxZ)
                {
                    if (remove)
                    {
                        this.pendingTickListEntriesHashSet.remove(nextTickEntry);
                        iterator.remove();
                    }

                    if (pendingUpdates == null)
                    {
                        pendingUpdates = Lists.<NextTickListEntry>newArrayList();
                    }

                    pendingUpdates.add(nextTickEntry);
                }
            }
        }

        return pendingUpdates;
    }

    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate)
    {
        if (!this.canSpawnAnimals() && (entityIn instanceof EntityAnimal || entityIn instanceof EntityWaterMob))
        {
            entityIn.setDead();
        }

        if (!this.canSpawnNPCs() && entityIn instanceof INpc)
        {
            entityIn.setDead();
        }

        super.updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    private boolean canSpawnNPCs()
    {
        return this.mcServer.getCanSpawnNPCs();
    }

    private boolean canSpawnAnimals()
    {
        return this.mcServer.getCanSpawnAnimals();
    }

    protected IChunkProvider createChunkProvider()
    {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        this.theChunkProviderServer = new ChunkProviderServer(this, ichunkloader, this.provider.createChunkGenerator());
        return this.theChunkProviderServer;
    }

    public List<TileEntity> getTileEntitiesIn(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        List<TileEntity> tileEntities = Lists.<TileEntity>newArrayList();

        for (int tileEntityIndex = 0; tileEntityIndex < this.loadedTileEntityList.size(); ++tileEntityIndex)
        {
            TileEntity tileEntity = (TileEntity)this.loadedTileEntityList.get(tileEntityIndex);
            BlockPos blockPos = tileEntity.getPos();

            if (blockPos.getX() >= minX && blockPos.getY() >= minY && blockPos.getZ() >= minZ && blockPos.getX() < maxX && blockPos.getY() < maxY && blockPos.getZ() < maxZ)
            {
                tileEntities.add(tileEntity);
            }
        }

        return tileEntities;
    }

    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos)
    {
        return !this.mcServer.isBlockProtected(this, pos, player) && this.getWorldBorder().contains(pos);
    }

    public void initialize(WorldSettings settings)
    {
        if (!this.worldInfo.isInitialized())
        {
            try
            {
                this.createSpawnPosition(settings);

                if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
                {
                    this.setDebugWorldSettings();
                }

                super.initialize(settings);
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception initializing level");

                try
                {
                    this.addWorldInfoToCrashReport(crashReport);
                }
                catch (Throwable ignored)
                {
                    ;
                }

                throw new ReportedException(crashReport);
            }

            this.worldInfo.setServerInitialized(true);
        }
    }

    private void setDebugWorldSettings()
    {
        this.worldInfo.setMapFeaturesEnabled(false);
        this.worldInfo.setAllowCommands(true);
        this.worldInfo.setRaining(false);
        this.worldInfo.setThundering(false);
        this.worldInfo.setCleanWeatherTime(1000000000);
        this.worldInfo.setWorldTime(6000L);
        this.worldInfo.setGameType(WorldSettings.GameType.SPECTATOR);
        this.worldInfo.setHardcore(false);
        this.worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
        this.worldInfo.setDifficultyLocked(true);
        this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
    }

    private void createSpawnPosition(WorldSettings settings)
    {
        if (!this.provider.canRespawnHere())
        {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
        }
        else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
        }
        else
        {
            this.findingSpawnPoint = true;
            WorldChunkManager worldChunkManager = this.provider.getWorldChunkManager();
            List<BiomeGenBase> spawnBiomes = worldChunkManager.getBiomesToSpawnIn();
            Random random = new Random(this.getSeed());
            BlockPos blockPos = worldChunkManager.findBiomePosition(0, 0, 256, spawnBiomes, random);
            int spawnX = 0;
            int spawnY = this.provider.getAverageGroundLevel();
            int spawnZ = 0;

            if (blockPos != null)
            {
                spawnX = blockPos.getX();
                spawnZ = blockPos.getZ();
            }
            else
            {
                logger.warn("Unable to find spawn biome");
            }

            int attempts = 0;

            while (!this.provider.canCoordinateBeSpawn(spawnX, spawnZ))
            {
                spawnX += random.nextInt(64) - random.nextInt(64);
                spawnZ += random.nextInt(64) - random.nextInt(64);
                ++attempts;

                if (attempts == 1000)
                {
                    break;
                }
            }

            this.worldInfo.setSpawn(new BlockPos(spawnX, spawnY, spawnZ));
            this.findingSpawnPoint = false;

            if (settings.isBonusChestEnabled())
            {
                this.createBonusChest();
            }
        }
    }

    protected void createBonusChest()
    {
        WorldGeneratorBonusChest worldGeneratorBonusChest = new WorldGeneratorBonusChest(bonusChestContent, 10);
        BlockPos.MutableBlockPos heightSamplePos = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 10; ++attempt)
        {
            int chestX = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int chestZ = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            BlockPos blockPos = this.getTopSolidOrLiquidBlock(heightSamplePos.set(chestX, 0, chestZ)).up();

            if (worldGeneratorBonusChest.generate(this, this.rand, blockPos))
            {
                break;
            }
        }
    }

    public BlockPos getSpawnCoordinate()
    {
        return this.provider.getSpawnCoordinate();
    }

    public void saveAllChunks(boolean flush, IProgressUpdate progressCallback) throws MinecraftException
    {
        if (this.chunkProvider.canSave())
        {
            if (progressCallback != null)
            {
                progressCallback.displaySavingString("Saving level");
            }

            this.saveLevel();

            if (progressCallback != null)
            {
                progressCallback.displayLoadingString("Saving chunks");
            }

            this.chunkProvider.saveChunks(flush, progressCallback);

            for (Chunk chunk : Lists.newArrayList(this.theChunkProviderServer.getLoadedChunks()))
            {
                if (chunk != null && !this.thePlayerManager.hasPlayerInstance(chunk.xPosition, chunk.zPosition))
                {
                    this.theChunkProviderServer.dropChunk(chunk.xPosition, chunk.zPosition);
                }
            }
        }
    }

    public void saveChunkData()
    {
        if (this.chunkProvider.canSave())
        {
            this.chunkProvider.saveExtraData();
        }
    }

    protected void saveLevel() throws MinecraftException
    {
        this.checkSessionLock();
        this.worldInfo.setBorderSize(this.getWorldBorder().getDiameter());
        this.worldInfo.getBorderCenterX(this.getWorldBorder().getCenterX());
        this.worldInfo.getBorderCenterZ(this.getWorldBorder().getCenterZ());
        this.worldInfo.setBorderSafeZone(this.getWorldBorder().getDamageBuffer());
        this.worldInfo.setBorderDamagePerBlock(this.getWorldBorder().getDamageAmount());
        this.worldInfo.setBorderWarningDistance(this.getWorldBorder().getWarningDistance());
        this.worldInfo.setBorderWarningTime(this.getWorldBorder().getWarningTime());
        this.worldInfo.setBorderLerpTarget(this.getWorldBorder().getTargetSize());
        this.worldInfo.setBorderLerpTime(this.getWorldBorder().getTimeUntilTarget());
        this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.mcServer.getConfigurationManager().getHostPlayerData());
        this.mapStorage.saveAllData();
    }

    protected void onEntityAdded(Entity entityIn)
    {
        super.onEntityAdded(entityIn);
        this.entitiesById.addKey(entityIn.getEntityId(), entityIn);
        this.entitiesByUuid.put(entityIn.getUniqueID(), entityIn);
        Entity[] entityParts = entityIn.getParts();

        if (entityParts != null)
        {
            for (int partIndex = 0; partIndex < entityParts.length; ++partIndex)
            {
                this.entitiesById.addKey(entityParts[partIndex].getEntityId(), entityParts[partIndex]);
            }
        }
    }

    protected void onEntityRemoved(Entity entityIn)
    {
        super.onEntityRemoved(entityIn);
        this.entitiesById.removeObject(entityIn.getEntityId());
        this.entitiesByUuid.remove(entityIn.getUniqueID());
        Entity[] entityParts = entityIn.getParts();

        if (entityParts != null)
        {
            for (int partIndex = 0; partIndex < entityParts.length; ++partIndex)
            {
                this.entitiesById.removeObject(entityParts[partIndex].getEntityId());
            }
        }
    }

    public boolean addWeatherEffect(Entity entityIn)
    {
        if (super.addWeatherEffect(entityIn))
        {
            this.mcServer.getConfigurationManager().sendToAllNear(entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, this.provider.getDimensionId(), new S2CPacketSpawnGlobalEntity(entityIn));
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setEntityState(Entity entityIn, byte state)
    {
        this.getEntityTracker().sendToTrackingAndSelf(entityIn, new S19PacketEntityStatus(entityIn, state));
    }

    public Explosion newExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking)
    {
        Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, isFlaming, isSmoking);
        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!isSmoking)
        {
            explosion.clearAffectedBlockPositions();
        }

        for (EntityPlayer entityPlayer : this.playerEntities)
        {
            if (entityPlayer.getDistanceSq(x, y, z) < 4096.0D)
            {
                ((EntityPlayerMP)entityPlayer).playerNetServerHandler.sendPacket(new S27PacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(), (Vec3)explosion.getPlayerKnockbackMap().get(entityPlayer)));
            }
        }

        return explosion;
    }

    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam)
    {
        BlockEventData blockEventData = new BlockEventData(pos, blockIn, eventID, eventParam);

        for (BlockEventData blockeventdata1 : this.blockEventQueue[this.blockEventCacheIndex])
        {
            if (blockeventdata1.equals(blockEventData))
            {
                return;
            }
        }

        this.blockEventQueue[this.blockEventCacheIndex].add(blockEventData);
    }

    private void sendQueuedBlockEvents()
    {
        while (!this.blockEventQueue[this.blockEventCacheIndex].isEmpty())
        {
            int activeQueueIndex = this.blockEventCacheIndex;
            this.blockEventCacheIndex ^= 1;

            for (BlockEventData blockEventData : this.blockEventQueue[activeQueueIndex])
            {
                if (this.fireBlockEvent(blockEventData))
                {
                    this.mcServer.getConfigurationManager().sendToAllNear((double)blockEventData.getPosition().getX(), (double)blockEventData.getPosition().getY(), (double)blockEventData.getPosition().getZ(), 64.0D, this.provider.getDimensionId(), new S24PacketBlockAction(blockEventData.getPosition(), blockEventData.getBlock(), blockEventData.getEventID(), blockEventData.getEventParameter()));
                }
            }

            this.blockEventQueue[activeQueueIndex].clear();
        }
    }

    private boolean fireBlockEvent(BlockEventData event)
    {
        IBlockState blockState = this.getBlockState(event.getPosition());
        return blockState.getBlock() == event.getBlock() ? blockState.getBlock().onBlockEventReceived(this, event.getPosition(), blockState, event.getEventID(), event.getEventParameter()) : false;
    }

    public void flush()
    {
        this.saveHandler.flush();
    }

    protected void updateWeather()
    {
        boolean wasRaining = this.isRaining();
        super.updateWeather();

        if (this.prevRainingStrength != this.rainingStrength)
        {
            this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(7, this.rainingStrength), this.provider.getDimensionId());
        }

        if (this.prevThunderingStrength != this.thunderingStrength)
        {
            this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimensionId());
        }

        if (wasRaining != this.isRaining())
        {
            if (wasRaining)
            {
                this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(2, 0.0F));
            }
            else
            {
                this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(1, 0.0F));
            }

            this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(7, this.rainingStrength));
            this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(8, this.thunderingStrength));
        }
    }

    protected int getRenderDistanceChunks()
    {
        return this.mcServer.getConfigurationManager().getViewDistance();
    }

    public MinecraftServer getMinecraftServer()
    {
        return this.mcServer;
    }

    public EntityTracker getEntityTracker()
    {
        return this.theEntityTracker;
    }

    public PlayerManager getPlayerManager()
    {
        return this.thePlayerManager;
    }

    public Teleporter getDefaultTeleporter()
    {
        return this.worldTeleporter;
    }

    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments)
    {
        this.spawnParticle(particleType, false, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
    }

    public void spawnParticle(EnumParticleTypes particleType, boolean longDistance, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int... particleArguments)
    {
        Packet packet = new S2APacketParticles(particleType, longDistance, (float)xCoord, (float)yCoord, (float)zCoord, (float)xOffset, (float)yOffset, (float)zOffset, (float)particleSpeed, numberOfParticles, particleArguments);

        for (int playerIndex = 0; playerIndex < this.playerEntities.size(); ++playerIndex)
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP)this.playerEntities.get(playerIndex);
            BlockPos playerPos = entityPlayerMP.getPosition();
            double distanceSq = playerPos.distanceSq(xCoord, yCoord, zCoord);

            if (distanceSq <= 256.0D || longDistance && distanceSq <= 65536.0D)
            {
                entityPlayerMP.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public Entity getEntityFromUuid(UUID uuid)
    {
        return (Entity)this.entitiesByUuid.get(uuid);
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        return this.mcServer.addScheduledTask(runnableToSchedule);
    }

    public boolean isCallingFromMinecraftThread()
    {
        return this.mcServer.isCallingFromMinecraftThread();
    }

    static class ServerBlockEventList extends ArrayList<BlockEventData>
    {
        private ServerBlockEventList()
        {
        }
    }
}
