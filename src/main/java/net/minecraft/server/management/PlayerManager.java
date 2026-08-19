package net.minecraft.server.management;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.optifine.ChunkPosComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerManager
{
    private static final Logger pmLogger = LogManager.getLogger();
    private final WorldServer theWorldServer;
    private final List<EntityPlayerMP> players = Lists.<EntityPlayerMP>newArrayList();
    private final LongHashMap<PlayerManager.PlayerInstance> playerInstances = new LongHashMap();
    private final List<PlayerManager.PlayerInstance> playerInstancesToUpdate = Lists.<PlayerManager.PlayerInstance>newArrayList();
    private final List<PlayerManager.PlayerInstance> playerInstanceList = Lists.<PlayerManager.PlayerInstance>newArrayList();
    private int playerViewRadius;
    private long previousTotalWorldTime;
    private final int[][] xzDirectionsConst = new int[][] {{1, 0}, {0, 1}, { -1, 0}, {0, -1}};
    private final Map<EntityPlayerMP, Set<ChunkCoordIntPair>> mapPlayerPendingEntries = new HashMap<EntityPlayerMP, Set<ChunkCoordIntPair>>();

    public PlayerManager(WorldServer serverWorld)
    {
        this.theWorldServer = serverWorld;
        this.setPlayerViewRadius(serverWorld.getMinecraftServer().getConfigurationManager().getViewDistance());
    }

    public WorldServer getWorldServer()
    {
        return this.theWorldServer;
    }

    public void updatePlayerInstances()
    {
        Set<Entry<EntityPlayerMP, Set<ChunkCoordIntPair>>> pendingEntrySet = this.mapPlayerPendingEntries.entrySet();
        Iterator<Entry<EntityPlayerMP, Set<ChunkCoordIntPair>>> pendingIterator = pendingEntrySet.iterator();

        while (pendingIterator.hasNext())
        {
            Entry<EntityPlayerMP, Set<ChunkCoordIntPair>> pendingEntry = pendingIterator.next();
            Set<ChunkCoordIntPair> pendingChunks = pendingEntry.getValue();

            if (!pendingChunks.isEmpty())
            {
                EntityPlayerMP player = pendingEntry.getKey();

                if (player.worldObj != this.theWorldServer)
                {
                    pendingIterator.remove();
                }
                else
                {
                    int chunksToLoad = this.playerViewRadius / 3 + 1;

                    if (!Config.isLazyChunkLoading())
                    {
                        chunksToLoad = this.playerViewRadius * 2 + 1;
                    }

                    for (ChunkCoordIntPair chunkCoordIntPair : this.getNearest(pendingChunks, player, chunksToLoad))
                    {
                        PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos, true);
                        playerInstance.addPlayer(player);
                        pendingChunks.remove(chunkCoordIntPair);
                    }
                }
            }
        }

        long totalWorldTime = this.theWorldServer.getTotalWorldTime();

        if (totalWorldTime - this.previousTotalWorldTime > 8000L)
        {
            this.previousTotalWorldTime = totalWorldTime;

            for (int instanceIndex = 0; instanceIndex < this.playerInstanceList.size(); ++instanceIndex)
            {
                PlayerManager.PlayerInstance playerInstance = this.playerInstanceList.get(instanceIndex);
                playerInstance.onUpdate();
                playerInstance.processChunk();
            }
        }
        else
        {
            for (int updateIndex = 0; updateIndex < this.playerInstancesToUpdate.size(); ++updateIndex)
            {
                PlayerManager.PlayerInstance playerInstance = this.playerInstancesToUpdate.get(updateIndex);
                playerInstance.onUpdate();
            }
        }

        this.playerInstancesToUpdate.clear();

        if (this.players.isEmpty())
        {
            WorldProvider worldProvider = this.theWorldServer.provider;

            if (!worldProvider.canRespawnHere())
            {
                this.theWorldServer.theChunkProviderServer.unloadAllChunks();
            }
        }
    }

    public boolean hasPlayerInstance(int chunkX, int chunkZ)
    {
        long chunkKey = (long)chunkX + 2147483647L | (long)chunkZ + 2147483647L << 32;
        return this.playerInstances.getValueByKey(chunkKey) != null;
    }

    private PlayerManager.PlayerInstance getPlayerInstance(int chunkX, int chunkZ, boolean createIfAbsent)
    {
        long chunkKey = (long)chunkX + 2147483647L | (long)chunkZ + 2147483647L << 32;
        PlayerManager.PlayerInstance playerInstance = (PlayerManager.PlayerInstance)this.playerInstances.getValueByKey(chunkKey);

        if (playerInstance == null && createIfAbsent)
        {
            playerInstance = new PlayerManager.PlayerInstance(chunkX, chunkZ);
            this.playerInstances.add(chunkKey, playerInstance);
            this.playerInstanceList.add(playerInstance);
        }

        return playerInstance;
    }

    public void markBlockForUpdate(BlockPos pos)
    {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkX, chunkZ, false);

        if (playerInstance != null)
        {
            playerInstance.flagChunkForUpdate(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
        }
    }

    public void addPlayer(EntityPlayerMP player)
    {
        int playerChunkX = (int)player.posX >> 4;
        int playerChunkZ = (int)player.posZ >> 4;
        player.managedPosX = player.posX;
        player.managedPosZ = player.posZ;
        int immediateRadius = Math.min(this.playerViewRadius, 8);
        int immediateMinChunkX = playerChunkX - immediateRadius;
        int immediateMaxChunkX = playerChunkX + immediateRadius;
        int immediateMinChunkZ = playerChunkZ - immediateRadius;
        int immediateMaxChunkZ = playerChunkZ + immediateRadius;
        Set<ChunkCoordIntPair> pendingChunks = this.getPendingEntriesSafe(player);

        for (int chunkX = playerChunkX - this.playerViewRadius; chunkX <= playerChunkX + this.playerViewRadius; ++chunkX)
        {
            for (int chunkZ = playerChunkZ - this.playerViewRadius; chunkZ <= playerChunkZ + this.playerViewRadius; ++chunkZ)
            {
                if (chunkX >= immediateMinChunkX && chunkX <= immediateMaxChunkX && chunkZ >= immediateMinChunkZ && chunkZ <= immediateMaxChunkZ)
                {
                    this.getPlayerInstance(chunkX, chunkZ, true).addPlayer(player);
                }
                else
                {
                    pendingChunks.add(new ChunkCoordIntPair(chunkX, chunkZ));
                }
            }
        }

        this.players.add(player);
        this.filterChunkLoadQueue(player);
    }

    public void filterChunkLoadQueue(EntityPlayerMP player)
    {
        List<ChunkCoordIntPair> loadedChunks = Lists.newArrayList(player.loadedChunks);
        int directionIndex = 0;
        int viewRadius = this.playerViewRadius;
        int playerChunkX = (int)player.posX >> 4;
        int playerChunkZ = (int)player.posZ >> 4;
        int chunkOffsetX = 0;
        int chunkOffsetZ = 0;
        ChunkCoordIntPair chunkCoordIntPair = this.getPlayerInstance(playerChunkX, playerChunkZ, true).chunkCoords;
        player.loadedChunks.clear();

        if (loadedChunks.contains(chunkCoordIntPair))
        {
            player.loadedChunks.add(chunkCoordIntPair);
        }

        for (int legLength = 1; legLength <= viewRadius * 2; ++legLength)
        {
            for (int legRepeat = 0; legRepeat < 2; ++legRepeat)
            {
                int[] direction = this.xzDirectionsConst[directionIndex++ % 4];

                for (int step = 0; step < legLength; ++step)
                {
                    chunkOffsetX += direction[0];
                    chunkOffsetZ += direction[1];
                    chunkCoordIntPair = this.getPlayerInstance(playerChunkX + chunkOffsetX, playerChunkZ + chunkOffsetZ, true).chunkCoords;

                    if (loadedChunks.contains(chunkCoordIntPair))
                    {
                        player.loadedChunks.add(chunkCoordIntPair);
                    }
                }
            }
        }

        directionIndex = directionIndex % 4;

        for (int finalStep = 0; finalStep < viewRadius * 2; ++finalStep)
        {
            chunkOffsetX += this.xzDirectionsConst[directionIndex][0];
            chunkOffsetZ += this.xzDirectionsConst[directionIndex][1];
            chunkCoordIntPair = this.getPlayerInstance(playerChunkX + chunkOffsetX, playerChunkZ + chunkOffsetZ, true).chunkCoords;

            if (loadedChunks.contains(chunkCoordIntPair))
            {
                player.loadedChunks.add(chunkCoordIntPair);
            }
        }
    }

    public void removePlayer(EntityPlayerMP player)
    {
        this.mapPlayerPendingEntries.remove(player);
        int playerChunkX = (int)player.managedPosX >> 4;
        int playerChunkZ = (int)player.managedPosZ >> 4;

        for (int chunkX = playerChunkX - this.playerViewRadius; chunkX <= playerChunkX + this.playerViewRadius; ++chunkX)
        {
            for (int chunkZ = playerChunkZ - this.playerViewRadius; chunkZ <= playerChunkZ + this.playerViewRadius; ++chunkZ)
            {
                PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkX, chunkZ, false);

                if (playerInstance != null)
                {
                    playerInstance.removePlayer(player);
                }
            }
        }

        this.players.remove(player);
    }

    private boolean overlaps(int firstChunkX, int firstChunkZ, int secondChunkX, int secondChunkZ, int radius)
    {
        int chunkDeltaX = firstChunkX - secondChunkX;
        int chunkDeltaZ = firstChunkZ - secondChunkZ;
        return chunkDeltaX >= -radius && chunkDeltaX <= radius ? chunkDeltaZ >= -radius && chunkDeltaZ <= radius : false;
    }

    public void updateMountedMovingPlayer(EntityPlayerMP player)
    {
        int playerChunkX = (int)player.posX >> 4;
        int playerChunkZ = (int)player.posZ >> 4;
        double deltaX = player.managedPosX - player.posX;
        double deltaZ = player.managedPosZ - player.posZ;
        double distanceSq = deltaX * deltaX + deltaZ * deltaZ;

        if (distanceSq >= 64.0D)
        {
            int previousChunkX = (int)player.managedPosX >> 4;
            int previousChunkZ = (int)player.managedPosZ >> 4;
            int viewRadius = this.playerViewRadius;
            int chunkDeltaX = playerChunkX - previousChunkX;
            int chunkDeltaZ = playerChunkZ - previousChunkZ;

            if (chunkDeltaX != 0 || chunkDeltaZ != 0)
            {
                Set<ChunkCoordIntPair> pendingChunks = this.getPendingEntriesSafe(player);

                for (int chunkX = playerChunkX - viewRadius; chunkX <= playerChunkX + viewRadius; ++chunkX)
                {
                    for (int chunkZ = playerChunkZ - viewRadius; chunkZ <= playerChunkZ + viewRadius; ++chunkZ)
                    {
                        if (!this.overlaps(chunkX, chunkZ, previousChunkX, previousChunkZ, viewRadius))
                        {
                            if (Config.isLazyChunkLoading())
                            {
                                pendingChunks.add(new ChunkCoordIntPair(chunkX, chunkZ));
                            }
                            else
                            {
                                this.getPlayerInstance(chunkX, chunkZ, true).addPlayer(player);
                            }
                        }

                        if (!this.overlaps(chunkX - chunkDeltaX, chunkZ - chunkDeltaZ, playerChunkX, playerChunkZ, viewRadius))
                        {
                            pendingChunks.remove(new ChunkCoordIntPair(chunkX - chunkDeltaX, chunkZ - chunkDeltaZ));
                            PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkX - chunkDeltaX, chunkZ - chunkDeltaZ, false);

                            if (playerInstance != null)
                            {
                                playerInstance.removePlayer(player);
                            }
                        }
                    }
                }

                this.filterChunkLoadQueue(player);
                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
            }
        }
    }

    public boolean isPlayerWatchingChunk(EntityPlayerMP player, int chunkX, int chunkZ)
    {
        PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkX, chunkZ, false);
        return playerInstance != null && playerInstance.playersWatchingChunk.contains(player) && !player.loadedChunks.contains(playerInstance.chunkCoords);
    }

    public void setPlayerViewRadius(int radius)
    {
        radius = MathHelper.clamp_int(radius, 3, 64);

        if (radius != this.playerViewRadius)
        {
            int radiusDelta = radius - this.playerViewRadius;

            for (EntityPlayerMP player : Lists.newArrayList(this.players))
            {
                int playerChunkX = (int)player.posX >> 4;
                int playerChunkZ = (int)player.posZ >> 4;
                Set<ChunkCoordIntPair> pendingChunks = this.getPendingEntriesSafe(player);

                if (radiusDelta > 0)
                {
                    for (int chunkX = playerChunkX - radius; chunkX <= playerChunkX + radius; ++chunkX)
                    {
                        for (int chunkZ = playerChunkZ - radius; chunkZ <= playerChunkZ + radius; ++chunkZ)
                        {
                            if (Config.isLazyChunkLoading())
                            {
                                pendingChunks.add(new ChunkCoordIntPair(chunkX, chunkZ));
                            }
                            else
                            {
                                PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkX, chunkZ, true);

                                if (!playerInstance.playersWatchingChunk.contains(player))
                                {
                                    playerInstance.addPlayer(player);
                                }
                            }
                        }
                    }
                }
                else
                {
                    for (int chunkX = playerChunkX - this.playerViewRadius; chunkX <= playerChunkX + this.playerViewRadius; ++chunkX)
                    {
                        for (int chunkZ = playerChunkZ - this.playerViewRadius; chunkZ <= playerChunkZ + this.playerViewRadius; ++chunkZ)
                        {
                            if (!this.overlaps(chunkX, chunkZ, playerChunkX, playerChunkZ, radius))
                            {
                                pendingChunks.remove(new ChunkCoordIntPair(chunkX, chunkZ));
                                PlayerManager.PlayerInstance playerInstance = this.getPlayerInstance(chunkX, chunkZ, true);

                                if (playerInstance != null)
                                {
                                    playerInstance.removePlayer(player);
                                }
                            }
                        }
                    }
                }
            }

            this.playerViewRadius = radius;
        }
    }

    public static int getFurthestViewableBlock(int distance)
    {
        return distance * 16 - 16;
    }

    private PriorityQueue<ChunkCoordIntPair> getNearest(Set<ChunkCoordIntPair> pendingEntries, EntityPlayerMP player, int maxCount)
    {
        float yaw;

        for (yaw = player.rotationYaw + 90.0F; yaw <= -180.0F; yaw += 360.0F)
        {
            ;
        }

        while (yaw > 180.0F)
        {
            yaw -= 360.0F;
        }

        double yawRadians = (double)yaw * 0.017453292519943295D;
        double pitch = (double)player.rotationPitch;
        double pitchRadians = pitch * 0.017453292519943295D;
        ChunkPosComparator chunkComparator = new ChunkPosComparator(player.chunkCoordX, player.chunkCoordZ, yawRadians, pitchRadians);
        Comparator<ChunkCoordIntPair> comparator = Collections.<ChunkCoordIntPair>reverseOrder(chunkComparator);
        PriorityQueue<ChunkCoordIntPair> nearestChunks = new PriorityQueue(maxCount, comparator);

        for (ChunkCoordIntPair candidateChunk : pendingEntries)
        {
            if (nearestChunks.size() < maxCount)
            {
                nearestChunks.add(candidateChunk);
            }
            else
            {
                ChunkCoordIntPair furthestChunk = (ChunkCoordIntPair)nearestChunks.peek();

                if (chunkComparator.compare(candidateChunk, furthestChunk) < 0)
                {
                    nearestChunks.remove();
                    nearestChunks.add(candidateChunk);
                }
            }
        }

        return nearestChunks;
    }

    private Set<ChunkCoordIntPair> getPendingEntriesSafe(EntityPlayerMP player)
    {
        Set<ChunkCoordIntPair> pendingChunks = this.mapPlayerPendingEntries.get(player);

        if (pendingChunks != null)
        {
            return pendingChunks;
        }
        else
        {
            int immediateRadius = Math.min(this.playerViewRadius, 8);
            int fullDiameter = this.playerViewRadius * 2 + 1;
            int immediateDiameter = immediateRadius * 2 + 1;
            int estimatedPendingSize = fullDiameter * fullDiameter - immediateDiameter * immediateDiameter;
            estimatedPendingSize = Math.max(estimatedPendingSize, 16);
            HashSet<ChunkCoordIntPair> pendingSet = new HashSet<ChunkCoordIntPair>(estimatedPendingSize);
            this.mapPlayerPendingEntries.put(player, pendingSet);
            return pendingSet;
        }
    }

    class PlayerInstance
    {
        private final List<EntityPlayerMP> playersWatchingChunk = Lists.<EntityPlayerMP>newArrayList();
        private final ChunkCoordIntPair chunkCoords;
        private short[] locationOfBlockChange = new short[64];
        private int numBlocksToUpdate;
        private int flagsYAreasToUpdate;
        private long previousWorldTime;

        public PlayerInstance(int chunkX, int chunkZ)
        {
            this.chunkCoords = new ChunkCoordIntPair(chunkX, chunkZ);
            PlayerManager.this.getWorldServer().theChunkProviderServer.loadChunk(chunkX, chunkZ);
        }

        public void addPlayer(EntityPlayerMP player)
        {
            if (this.playersWatchingChunk.contains(player))
            {
                PlayerManager.pmLogger.debug("Failed to add player. {} already is in chunk {}, {}", new Object[] {player, Integer.valueOf(this.chunkCoords.chunkXPos), Integer.valueOf(this.chunkCoords.chunkZPos)});
            }
            else
            {
                if (this.playersWatchingChunk.isEmpty())
                {
                    this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
                }

                this.playersWatchingChunk.add(player);
                player.loadedChunks.add(this.chunkCoords);
            }
        }

        public void removePlayer(EntityPlayerMP player)
        {
            if (this.playersWatchingChunk.contains(player))
            {
                Chunk chunk = PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos);

                if (chunk.isPopulated())
                {
                    player.playerNetServerHandler.sendPacket(new S21PacketChunkData(chunk, true, 0));
                }

                this.playersWatchingChunk.remove(player);
                player.loadedChunks.remove(this.chunkCoords);

                if (this.playersWatchingChunk.isEmpty())
                {
                    long chunkKey = (long)this.chunkCoords.chunkXPos + 2147483647L | (long)this.chunkCoords.chunkZPos + 2147483647L << 32;
                    this.increaseInhabitedTime(chunk);
                    PlayerManager.this.playerInstances.remove(chunkKey);
                    PlayerManager.this.playerInstanceList.remove(this);

                    if (this.numBlocksToUpdate > 0)
                    {
                        PlayerManager.this.playerInstancesToUpdate.remove(this);
                    }

                    PlayerManager.this.getWorldServer().theChunkProviderServer.dropChunk(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos);
                }
            }
        }

        public void processChunk()
        {
            this.increaseInhabitedTime(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos));
        }

        private void increaseInhabitedTime(Chunk theChunk)
        {
            theChunk.setInhabitedTime(theChunk.getInhabitedTime() + PlayerManager.this.theWorldServer.getTotalWorldTime() - this.previousWorldTime);
            this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
        }

        public void flagChunkForUpdate(int blockX, int blockY, int blockZ)
        {
            if (this.numBlocksToUpdate == 0)
            {
                PlayerManager.this.playerInstancesToUpdate.add(this);
            }

            this.flagsYAreasToUpdate |= 1 << (blockY >> 4);

            if (this.numBlocksToUpdate < 64)
            {
                short packedBlockChange = (short)(blockX << 12 | blockZ << 8 | blockY);

                for (int changeIndex = 0; changeIndex < this.numBlocksToUpdate; ++changeIndex)
                {
                    if (this.locationOfBlockChange[changeIndex] == packedBlockChange)
                    {
                        return;
                    }
                }

                this.locationOfBlockChange[this.numBlocksToUpdate++] = packedBlockChange;
            }
        }

        public void sendToAllPlayersWatchingChunk(Packet thePacket)
        {
            for (int playerIndex = 0; playerIndex < this.playersWatchingChunk.size(); ++playerIndex)
            {
                EntityPlayerMP player = this.playersWatchingChunk.get(playerIndex);

                if (!player.loadedChunks.contains(this.chunkCoords))
                {
                    player.playerNetServerHandler.sendPacket(thePacket);
                }
            }
        }

        public void onUpdate()
        {
            if (this.numBlocksToUpdate != 0)
            {
                if (this.numBlocksToUpdate == 1)
                {
                    int blockX = (this.locationOfBlockChange[0] >> 12 & 15) + this.chunkCoords.chunkXPos * 16;
                    int blockY = this.locationOfBlockChange[0] & 255;
                    int blockZ = (this.locationOfBlockChange[0] >> 8 & 15) + this.chunkCoords.chunkZPos * 16;
                    BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
                    this.sendToAllPlayersWatchingChunk(new S23PacketBlockChange(PlayerManager.this.theWorldServer, blockPos));

                    if (PlayerManager.this.theWorldServer.getBlockState(blockPos).getBlock().hasTileEntity())
                    {
                        this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(blockPos));
                    }
                }
                else if (this.numBlocksToUpdate != 64)
                {
                    this.sendToAllPlayersWatchingChunk(new S22PacketMultiBlockChange(this.numBlocksToUpdate, this.locationOfBlockChange, PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos)));

                    for (int changeIndex = 0; changeIndex < this.numBlocksToUpdate; ++changeIndex)
                    {
                        int blockX = (this.locationOfBlockChange[changeIndex] >> 12 & 15) + this.chunkCoords.chunkXPos * 16;
                        int blockY = this.locationOfBlockChange[changeIndex] & 255;
                        int blockZ = (this.locationOfBlockChange[changeIndex] >> 8 & 15) + this.chunkCoords.chunkZPos * 16;
                        BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);

                        if (PlayerManager.this.theWorldServer.getBlockState(blockPos).getBlock().hasTileEntity())
                        {
                            this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(blockPos));
                        }
                    }
                }
                else
                {
                    int chunkBlockX = this.chunkCoords.chunkXPos * 16;
                    int chunkBlockZ = this.chunkCoords.chunkZPos * 16;
                    this.sendToAllPlayersWatchingChunk(new S21PacketChunkData(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkCoords.chunkXPos, this.chunkCoords.chunkZPos), false, this.flagsYAreasToUpdate));

                    for (int sectionY = 0; sectionY < 16; ++sectionY)
                    {
                        if ((this.flagsYAreasToUpdate & 1 << sectionY) != 0)
                        {
                            int sectionMinY = sectionY << 4;
                            List<TileEntity> tileEntities = PlayerManager.this.theWorldServer.getTileEntitiesIn(chunkBlockX, sectionMinY, chunkBlockZ, chunkBlockX + 16, sectionMinY + 16, chunkBlockZ + 16);

                            for (int tileIndex = 0; tileIndex < tileEntities.size(); ++tileIndex)
                            {
                                this.sendTileToAllPlayersWatchingChunk((TileEntity)tileEntities.get(tileIndex));
                            }
                        }
                    }
                }

                this.numBlocksToUpdate = 0;
                this.flagsYAreasToUpdate = 0;
            }
        }

        private void sendTileToAllPlayersWatchingChunk(TileEntity theTileEntity)
        {
            if (theTileEntity != null)
            {
                Packet packet = theTileEntity.getDescriptionPacket();

                if (packet != null)
                {
                    this.sendToAllPlayersWatchingChunk(packet);
                }
            }
        }
    }
}
