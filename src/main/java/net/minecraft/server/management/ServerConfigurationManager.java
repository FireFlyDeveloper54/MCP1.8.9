package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ServerConfigurationManager
{
    public static final File FILE_PLAYERBANS = new File("banned-players.json");
    public static final File FILE_IPBANS = new File("banned-ips.json");
    public static final File FILE_OPS = new File("ops.json");
    public static final File FILE_WHITELIST = new File("whitelist.json");
    private static final Logger logger = LogManager.getLogger();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
    private final MinecraftServer mcServer;
    private final List<EntityPlayerMP> playerEntityList = Lists.<EntityPlayerMP>newArrayList();
    private final Map<UUID, EntityPlayerMP> uuidToPlayerMap = Maps.<UUID, EntityPlayerMP>newHashMap();
    private final UserListBans bannedPlayers;
    private final BanList bannedIPs;
    private final UserListOps ops;
    private final UserListWhitelist whiteListedPlayers;
    private final Map<UUID, StatisticsFile> playerStatFiles;
    private IPlayerFileData playerNBTManagerObj;
    private boolean whiteListEnforced;
    protected int maxPlayers;
    private int viewDistance;
    private WorldSettings.GameType gameType;
    private boolean commandsAllowedForAll;
    private int playerPingIndex;

    public ServerConfigurationManager(MinecraftServer server)
    {
        this.bannedPlayers = new UserListBans(FILE_PLAYERBANS);
        this.bannedIPs = new BanList(FILE_IPBANS);
        this.ops = new UserListOps(FILE_OPS);
        this.whiteListedPlayers = new UserListWhitelist(FILE_WHITELIST);
        this.playerStatFiles = Maps.<UUID, StatisticsFile>newHashMap();
        this.mcServer = server;
        this.bannedPlayers.setLanServer(false);
        this.bannedIPs.setLanServer(false);
        this.maxPlayers = 8;
    }

    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn)
    {
        GameProfile gameProfile = playerIn.getGameProfile();
        PlayerProfileCache profileCache = this.mcServer.getPlayerProfileCache();
        GameProfile cachedProfile = profileCache.getProfileByUUID(gameProfile.getId());
        String cachedProfileName = cachedProfile == null ? gameProfile.getName() : cachedProfile.getName();
        profileCache.addEntry(gameProfile);
        NBTTagCompound playerData = this.readPlayerDataFromFile(playerIn);
        playerIn.setWorld(this.mcServer.worldServerForDimension(playerIn.dimension));
        playerIn.theItemInWorldManager.setWorld((WorldServer)playerIn.worldObj);
        String remoteAddress = "local";

        if (netManager.getRemoteAddress() != null)
        {
            remoteAddress = netManager.getRemoteAddress().toString();
        }

        logger.info(playerIn.getName() + "[" + remoteAddress + "] logged in with entity id " + playerIn.getEntityId() + " at (" + playerIn.posX + ", " + playerIn.posY + ", " + playerIn.posZ + ")");
        WorldServer worldServer = this.mcServer.worldServerForDimension(playerIn.dimension);
        WorldInfo worldInfo = worldServer.getWorldInfo();
        BlockPos spawnPos = worldServer.getSpawnPoint();
        this.setPlayerGameTypeBasedOnOther(playerIn, (EntityPlayerMP)null, worldServer);
        NetHandlerPlayServer connection = new NetHandlerPlayServer(this.mcServer, netManager, playerIn);
        connection.sendPacket(new S01PacketJoinGame(playerIn.getEntityId(), playerIn.theItemInWorldManager.getGameType(), worldInfo.isHardcoreModeEnabled(), worldServer.provider.getDimensionId(), worldServer.getDifficulty(), this.getMaxPlayers(), worldInfo.getTerrainType(), worldServer.getGameRules().getBoolean("reducedDebugInfo")));
        connection.sendPacket(new S3FPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServerInstance().getServerModName())));
        connection.sendPacket(new S41PacketServerDifficulty(worldInfo.getDifficulty(), worldInfo.isDifficultyLocked()));
        connection.sendPacket(new S05PacketSpawnPosition(spawnPos));
        connection.sendPacket(new S39PacketPlayerAbilities(playerIn.capabilities));
        connection.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
        playerIn.getStatFile().markAllDirty();
        playerIn.getStatFile().sendAchievements(playerIn);
        this.sendScoreboard((ServerScoreboard)worldServer.getScoreboard(), playerIn);
        this.mcServer.refreshStatusNextTick();
        ChatComponentTranslation joinMessage;

        if (!playerIn.getName().equalsIgnoreCase(cachedProfileName))
        {
            joinMessage = new ChatComponentTranslation("multiplayer.player.joined.renamed", new Object[] {playerIn.getDisplayName(), cachedProfileName});
        }
        else
        {
            joinMessage = new ChatComponentTranslation("multiplayer.player.joined", new Object[] {playerIn.getDisplayName()});
        }

        joinMessage.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        this.sendChatMsg(joinMessage);
        this.playerLoggedIn(playerIn);
        connection.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        this.updateTimeAndWeatherForPlayer(playerIn, worldServer);

        if (this.mcServer.getResourcePackUrl().length() > 0)
        {
            playerIn.loadResourcePack(this.mcServer.getResourcePackUrl(), this.mcServer.getResourcePackHash());
        }

        for (PotionEffect potionEffect : playerIn.getActivePotionEffects())
        {
            connection.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potionEffect));
        }

        playerIn.addSelfToInternalCraftingInventory();

        if (playerData != null && playerData.hasKey("Riding", 10))
        {
            Entity entity = EntityList.createEntityFromNBT(playerData.getCompoundTag("Riding"), worldServer);

            if (entity != null)
            {
                entity.forceSpawn = true;
                worldServer.spawnEntityInWorld(entity);
                playerIn.mountEntity(entity);
                entity.forceSpawn = false;
            }
        }
    }

    protected void sendScoreboard(ServerScoreboard scoreboardIn, EntityPlayerMP playerIn)
    {
        Set<ScoreObjective> sentObjectives = Sets.<ScoreObjective>newHashSet();

        for (ScorePlayerTeam scorePlayerTeam : scoreboardIn.getTeams())
        {
            playerIn.playerNetServerHandler.sendPacket(new S3EPacketTeams(scorePlayerTeam, 0));
        }

        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            ScoreObjective scoreObjective = scoreboardIn.getObjectiveInDisplaySlot(displaySlot);

            if (scoreObjective != null && !sentObjectives.contains(scoreObjective))
            {
                for (Packet packet : scoreboardIn.getCreatePackets(scoreObjective))
                {
                    playerIn.playerNetServerHandler.sendPacket(packet);
                }

                sentObjectives.add(scoreObjective);
            }
        }
    }

    public void setPlayerManager(WorldServer[] worldServers)
    {
        this.playerNBTManagerObj = worldServers[0].getSaveHandler().getPlayerNBTManager();
        worldServers[0].getWorldBorder().addListener(new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
                ServerConfigurationManager.this.sendPacketToAllPlayers(new S44PacketWorldBorder(border, S44PacketWorldBorder.Action.SET_SIZE));
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
                ServerConfigurationManager.this.sendPacketToAllPlayers(new S44PacketWorldBorder(border, S44PacketWorldBorder.Action.LERP_SIZE));
            }
            public void onCenterChanged(WorldBorder border, double x, double z)
            {
                ServerConfigurationManager.this.sendPacketToAllPlayers(new S44PacketWorldBorder(border, S44PacketWorldBorder.Action.SET_CENTER));
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
                ServerConfigurationManager.this.sendPacketToAllPlayers(new S44PacketWorldBorder(border, S44PacketWorldBorder.Action.SET_WARNING_TIME));
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
                ServerConfigurationManager.this.sendPacketToAllPlayers(new S44PacketWorldBorder(border, S44PacketWorldBorder.Action.SET_WARNING_BLOCKS));
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
            }
        });
    }

    public void preparePlayer(EntityPlayerMP playerIn, WorldServer worldIn)
    {
        WorldServer worldServer = playerIn.getServerForPlayer();

        if (worldIn != null)
        {
            worldIn.getPlayerManager().removePlayer(playerIn);
        }

        worldServer.getPlayerManager().addPlayer(playerIn);
        worldServer.theChunkProviderServer.loadChunk((int)playerIn.posX >> 4, (int)playerIn.posZ >> 4);
    }

    public int getEntityViewDistance()
    {
        return PlayerManager.getFurthestViewableBlock(this.getViewDistance());
    }

    public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn)
    {
        NBTTagCompound nBTTagCompound = this.mcServer.worldServers[0].getWorldInfo().getPlayerNBTTagCompound();
        NBTTagCompound nbttagcompound1;

        if (playerIn.getName().equals(this.mcServer.getServerOwner()) && nBTTagCompound != null)
        {
            playerIn.readFromNBT(nBTTagCompound);
            nbttagcompound1 = nBTTagCompound;
            logger.debug("loading single player");
        }
        else
        {
            nbttagcompound1 = this.playerNBTManagerObj.readPlayerData(playerIn);
        }

        return nbttagcompound1;
    }

    protected void writePlayerData(EntityPlayerMP playerIn)
    {
        this.playerNBTManagerObj.writePlayerData(playerIn);
        StatisticsFile statisticsFile = (StatisticsFile)this.playerStatFiles.get(playerIn.getUniqueID());

        if (statisticsFile != null)
        {
            statisticsFile.saveStatFile();
        }
    }

    public void playerLoggedIn(EntityPlayerMP playerIn)
    {
        this.playerEntityList.add(playerIn);
        this.uuidToPlayerMap.put(playerIn.getUniqueID(), playerIn);
        this.sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[] {playerIn}));
        WorldServer worldServer = this.mcServer.worldServerForDimension(playerIn.dimension);
        worldServer.spawnEntityInWorld(playerIn);
        this.preparePlayer(playerIn, (WorldServer)null);

        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            EntityPlayerMP listedPlayer = (EntityPlayerMP)this.playerEntityList.get(playerIndex);
            playerIn.playerNetServerHandler.sendPacket(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[] {listedPlayer}));
        }
    }

    public void serverUpdateMountedMovingPlayer(EntityPlayerMP playerIn)
    {
        playerIn.getServerForPlayer().getPlayerManager().updateMountedMovingPlayer(playerIn);
    }

    public void playerLoggedOut(EntityPlayerMP playerIn)
    {
        playerIn.triggerAchievement(StatList.leaveGameStat);
        this.writePlayerData(playerIn);
        WorldServer worldServer = playerIn.getServerForPlayer();

        if (playerIn.ridingEntity != null)
        {
            worldServer.removePlayerEntityDangerously(playerIn.ridingEntity);
            logger.debug("removing player mount");
        }

        worldServer.removeEntity(playerIn);
        worldServer.getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        UUID uuid = playerIn.getUniqueID();
        EntityPlayerMP mappedPlayer = (EntityPlayerMP)this.uuidToPlayerMap.get(uuid);

        if (mappedPlayer == playerIn)
        {
            this.uuidToPlayerMap.remove(uuid);
            this.playerStatFiles.remove(uuid);
        }

        this.sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.REMOVE_PLAYER, new EntityPlayerMP[] {playerIn}));
    }

    public String allowUserToConnect(SocketAddress address, GameProfile profile)
    {
        if (this.bannedPlayers.isBanned(profile))
        {
            UserListBansEntry userListBansEntry = (UserListBansEntry)this.bannedPlayers.getEntry(profile);
            String message = "You are banned from this server!\nReason: " + userListBansEntry.getBanReason();

            if (userListBansEntry.getBanEndDate() != null)
            {
                message = message + "\nYour ban will be removed on " + dateFormat.format(userListBansEntry.getBanEndDate());
            }

            return message;
        }
        else if (!this.canJoin(profile))
        {
            return "You are not white-listed on this server!";
        }
        else if (this.bannedIPs.isBanned(address))
        {
            IPBanEntry ipBanEntry = this.bannedIPs.getBanEntry(address);
            String ipBanMessage = "Your IP address is banned from this server!\nReason: " + ipBanEntry.getBanReason();

            if (ipBanEntry.getBanEndDate() != null)
            {
                ipBanMessage = ipBanMessage + "\nYour ban will be removed on " + dateFormat.format(ipBanEntry.getBanEndDate());
            }

            return ipBanMessage;
        }
        else
        {
            return this.playerEntityList.size() >= this.maxPlayers && !this.bypassesPlayerLimit(profile) ? "The server is full!" : null;
        }
    }

    public EntityPlayerMP createPlayerForUser(GameProfile profile)
    {
        UUID profileUuid = EntityPlayer.getUUID(profile);
        List<EntityPlayerMP> duplicatePlayers = Lists.<EntityPlayerMP>newArrayList();

        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            EntityPlayerMP onlinePlayer = (EntityPlayerMP)this.playerEntityList.get(playerIndex);

            if (onlinePlayer.getUniqueID().equals(profileUuid))
            {
                duplicatePlayers.add(onlinePlayer);
            }
        }

        EntityPlayerMP mappedPlayer = (EntityPlayerMP)this.uuidToPlayerMap.get(profile.getId());

        if (mappedPlayer != null && !duplicatePlayers.contains(mappedPlayer))
        {
            duplicatePlayers.add(mappedPlayer);
        }

        for (EntityPlayerMP duplicatePlayer : duplicatePlayers)
        {
            duplicatePlayer.playerNetServerHandler.kickPlayerFromServer("You logged in from another location");
        }

        ItemInWorldManager itemManager;

        if (this.mcServer.isDemo())
        {
            itemManager = new DemoWorldManager(this.mcServer.worldServerForDimension(0));
        }
        else
        {
            itemManager = new ItemInWorldManager(this.mcServer.worldServerForDimension(0));
        }

        return new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(0), profile, itemManager);
    }

    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd)
    {
        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerForPlayer().getEntityTracker().untrackEntity(playerIn);
        playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);
        BlockPos bedLocation = playerIn.getBedLocation();
        boolean spawnForced = playerIn.isSpawnForced();
        playerIn.dimension = dimension;
        ItemInWorldManager itemManager;

        if (this.mcServer.isDemo())
        {
            itemManager = new DemoWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
        }
        else
        {
            itemManager = new ItemInWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
        }

        EntityPlayerMP newPlayer = new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(), itemManager);
        newPlayer.playerNetServerHandler = playerIn.playerNetServerHandler;
        newPlayer.clonePlayer(playerIn, conqueredEnd);
        newPlayer.setEntityId(playerIn.getEntityId());
        newPlayer.setCommandStats(playerIn);
        WorldServer worldServer = this.mcServer.worldServerForDimension(playerIn.dimension);
        this.setPlayerGameTypeBasedOnOther(newPlayer, playerIn, worldServer);

        if (bedLocation != null)
        {
            BlockPos bedSpawnPos = EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(playerIn.dimension), bedLocation, spawnForced);

            if (bedSpawnPos != null)
            {
                newPlayer.setLocationAndAngles((double)((float)bedSpawnPos.getX() + 0.5F), (double)((float)bedSpawnPos.getY() + 0.1F), (double)((float)bedSpawnPos.getZ() + 0.5F), 0.0F, 0.0F);
                newPlayer.setSpawnPoint(bedLocation, spawnForced);
            }
            else
            {
                newPlayer.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
            }
        }

        worldServer.theChunkProviderServer.loadChunk((int)newPlayer.posX >> 4, (int)newPlayer.posZ >> 4);

        while (!worldServer.getCollidingBoundingBoxes(newPlayer, newPlayer.getEntityBoundingBox()).isEmpty() && newPlayer.posY < 256.0D)
        {
            newPlayer.setPosition(newPlayer.posX, newPlayer.posY + 1.0D, newPlayer.posZ);
        }

        newPlayer.playerNetServerHandler.sendPacket(new S07PacketRespawn(newPlayer.dimension, newPlayer.worldObj.getDifficulty(), newPlayer.worldObj.getWorldInfo().getTerrainType(), newPlayer.theItemInWorldManager.getGameType()));
        BlockPos worldSpawnPos = worldServer.getSpawnPoint();
        newPlayer.playerNetServerHandler.setPlayerLocation(newPlayer.posX, newPlayer.posY, newPlayer.posZ, newPlayer.rotationYaw, newPlayer.rotationPitch);
        newPlayer.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(worldSpawnPos));
        newPlayer.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));
        this.updateTimeAndWeatherForPlayer(newPlayer, worldServer);
        worldServer.getPlayerManager().addPlayer(newPlayer);
        worldServer.spawnEntityInWorld(newPlayer);
        this.playerEntityList.add(newPlayer);
        this.uuidToPlayerMap.put(newPlayer.getUniqueID(), newPlayer);
        newPlayer.addSelfToInternalCraftingInventory();
        newPlayer.setHealth(newPlayer.getHealth());
        return newPlayer;
    }

    public void transferPlayerToDimension(EntityPlayerMP playerIn, int dimension)
    {
        int oldDimensionId = playerIn.dimension;
        WorldServer oldWorld = this.mcServer.worldServerForDimension(playerIn.dimension);
        playerIn.dimension = dimension;
        WorldServer newWorld = this.mcServer.worldServerForDimension(playerIn.dimension);
        playerIn.playerNetServerHandler.sendPacket(new S07PacketRespawn(playerIn.dimension, playerIn.worldObj.getDifficulty(), playerIn.worldObj.getWorldInfo().getTerrainType(), playerIn.theItemInWorldManager.getGameType()));
        oldWorld.removePlayerEntityDangerously(playerIn);
        playerIn.isDead = false;
        this.transferEntityToWorld(playerIn, oldDimensionId, oldWorld, newWorld);
        this.preparePlayer(playerIn, oldWorld);
        playerIn.playerNetServerHandler.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.theItemInWorldManager.setWorld(newWorld);
        this.updateTimeAndWeatherForPlayer(playerIn, newWorld);
        this.syncPlayerInventory(playerIn);

        for (PotionEffect potionEffect : playerIn.getActivePotionEffects())
        {
            playerIn.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potionEffect));
        }
    }

    public void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn)
    {
        double targetX = entityIn.posX;
        double targetZ = entityIn.posZ;
        double coordinateScale = 8.0D;
        float entityYaw = entityIn.rotationYaw;
        oldWorldIn.theProfiler.startSection("moving");

        if (entityIn.dimension == -1)
        {
            targetX = MathHelper.clamp_double(targetX / coordinateScale, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
            targetZ = MathHelper.clamp_double(targetZ / coordinateScale, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
            entityIn.setLocationAndAngles(targetX, entityIn.posY, targetZ, entityIn.rotationYaw, entityIn.rotationPitch);

            if (entityIn.isEntityAlive())
            {
                oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }
        else if (entityIn.dimension == 0)
        {
            targetX = MathHelper.clamp_double(targetX * coordinateScale, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
            targetZ = MathHelper.clamp_double(targetZ * coordinateScale, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
            entityIn.setLocationAndAngles(targetX, entityIn.posY, targetZ, entityIn.rotationYaw, entityIn.rotationPitch);

            if (entityIn.isEntityAlive())
            {
                oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }
        else
        {
            BlockPos targetPos;

            if (lastDimension == 1)
            {
                targetPos = toWorldIn.getSpawnPoint();
            }
            else
            {
                targetPos = toWorldIn.getSpawnCoordinate();
            }

            targetX = (double)targetPos.getX();
            entityIn.posY = (double)targetPos.getY();
            targetZ = (double)targetPos.getZ();
            entityIn.setLocationAndAngles(targetX, entityIn.posY, targetZ, 90.0F, 0.0F);

            if (entityIn.isEntityAlive())
            {
                oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }
        }

        oldWorldIn.theProfiler.endSection();

        if (lastDimension != 1)
        {
            oldWorldIn.theProfiler.startSection("placing");
            targetX = (double)MathHelper.clamp_int((int)targetX, -29999872, 29999872);
            targetZ = (double)MathHelper.clamp_int((int)targetZ, -29999872, 29999872);

            if (entityIn.isEntityAlive())
            {
                entityIn.setLocationAndAngles(targetX, entityIn.posY, targetZ, entityIn.rotationYaw, entityIn.rotationPitch);
                toWorldIn.getDefaultTeleporter().placeInPortal(entityIn, entityYaw);
                toWorldIn.spawnEntityInWorld(entityIn);
                toWorldIn.updateEntityWithOptionalForce(entityIn, false);
            }

            oldWorldIn.theProfiler.endSection();
        }

        entityIn.setWorld(toWorldIn);
    }

    public void onTick()
    {
        if (++this.playerPingIndex > 600)
        {
            this.sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.UPDATE_LATENCY, this.playerEntityList));
            this.playerPingIndex = 0;
        }
    }

    public void sendPacketToAllPlayers(Packet packetIn)
    {
        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            ((EntityPlayerMP)this.playerEntityList.get(playerIndex)).playerNetServerHandler.sendPacket(packetIn);
        }
    }

    public void sendPacketToAllPlayersInDimension(Packet packetIn, int dimension)
    {
        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP)this.playerEntityList.get(playerIndex);

            if (entityPlayerMP.dimension == dimension)
            {
                entityPlayerMP.playerNetServerHandler.sendPacket(packetIn);
            }
        }
    }

    public void sendMessageToAllTeamMembers(EntityPlayer player, IChatComponent message)
    {
        Team team = player.getTeam();

        if (team != null)
        {
            for (String memberName : team.getMembershipCollection())
            {
                EntityPlayerMP entityPlayerMP = this.getPlayerByUsername(memberName);

                if (entityPlayerMP != null && entityPlayerMP != player)
                {
                    entityPlayerMP.addChatMessage(message);
                }
            }
        }
    }

    public void sendMessageToTeamOrEvryPlayer(EntityPlayer player, IChatComponent message)
    {
        Team team = player.getTeam();

        if (team == null)
        {
            this.sendChatMsg(message);
        }
        else
        {
            for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
            {
                EntityPlayerMP entityPlayerMP = (EntityPlayerMP)this.playerEntityList.get(playerIndex);

                if (entityPlayerMP.getTeam() != team)
                {
                    entityPlayerMP.addChatMessage(message);
                }
            }
        }
    }

    public String getFormattedListOfPlayers(boolean includeUuids)
    {
        String playerList = "";
        List<EntityPlayerMP> players = Lists.newArrayList(this.playerEntityList);

        for (int playerIndex = 0; playerIndex < players.size(); ++playerIndex)
        {
            if (playerIndex > 0)
            {
                playerList = playerList + ", ";
            }

            EntityPlayerMP player = (EntityPlayerMP)players.get(playerIndex);
            playerList = playerList + player.getName();

            if (includeUuids)
            {
                playerList = playerList + " (" + player.getUniqueID().toString() + ")";
            }
        }

        return playerList;
    }

    public String[] getAllUsernames()
    {
        String[] usernames = new String[this.playerEntityList.size()];

        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            usernames[playerIndex] = ((EntityPlayerMP)this.playerEntityList.get(playerIndex)).getName();
        }

        return usernames;
    }

    public GameProfile[] getAllProfiles()
    {
        GameProfile[] profiles = new GameProfile[this.playerEntityList.size()];

        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            profiles[playerIndex] = ((EntityPlayerMP)this.playerEntityList.get(playerIndex)).getGameProfile();
        }

        return profiles;
    }

    public UserListBans getBannedPlayers()
    {
        return this.bannedPlayers;
    }

    public BanList getBannedIPs()
    {
        return this.bannedIPs;
    }

    public void addOp(GameProfile profile)
    {
        this.ops.addEntry(new UserListOpsEntry(profile, this.mcServer.getOpPermissionLevel(), this.ops.bypassesPlayerLimit(profile)));
    }

    public void removeOp(GameProfile profile)
    {
        this.ops.removeEntry(profile);
    }

    public boolean canJoin(GameProfile profile)
    {
        return !this.whiteListEnforced || this.ops.hasEntry(profile) || this.whiteListedPlayers.hasEntry(profile);
    }

    public boolean canSendCommands(GameProfile profile)
    {
        return this.ops.hasEntry(profile) || this.mcServer.isSinglePlayer() && this.mcServer.worldServers[0].getWorldInfo().areCommandsAllowed() && this.mcServer.getServerOwner().equalsIgnoreCase(profile.getName()) || this.commandsAllowedForAll;
    }

    public EntityPlayerMP getPlayerByUsername(String username)
    {
        for (EntityPlayerMP entityPlayerMP : this.playerEntityList)
        {
            if (entityPlayerMP.getName().equalsIgnoreCase(username))
            {
                return entityPlayerMP;
            }
        }

        return null;
    }

    public void sendToAllNear(double x, double y, double z, double radius, int dimension, Packet packetIn)
    {
        this.sendToAllNearExcept((EntityPlayer)null, x, y, z, radius, dimension, packetIn);
    }

    public void sendToAllNearExcept(EntityPlayer excludedPlayer, double x, double y, double z, double radius, int dimension, Packet packetIn)
    {
        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP)this.playerEntityList.get(playerIndex);

            if (entityPlayerMP != excludedPlayer && entityPlayerMP.dimension == dimension)
            {
                double deltaX = x - entityPlayerMP.posX;
                double deltaY = y - entityPlayerMP.posY;
                double deltaZ = z - entityPlayerMP.posZ;

                if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ < radius * radius)
                {
                    entityPlayerMP.playerNetServerHandler.sendPacket(packetIn);
                }
            }
        }
    }

    public void saveAllPlayerData()
    {
        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            this.writePlayerData((EntityPlayerMP)this.playerEntityList.get(playerIndex));
        }
    }

    public void addWhitelistedPlayer(GameProfile profile)
    {
        this.whiteListedPlayers.addEntry(new UserListWhitelistEntry(profile));
    }

    public void removePlayerFromWhitelist(GameProfile profile)
    {
        this.whiteListedPlayers.removeEntry(profile);
    }

    public UserListWhitelist getWhitelistedPlayers()
    {
        return this.whiteListedPlayers;
    }

    public String[] getWhitelistedPlayerNames()
    {
        return this.whiteListedPlayers.getKeys();
    }

    public UserListOps getOppedPlayers()
    {
        return this.ops;
    }

    public String[] getOppedPlayerNames()
    {
        return this.ops.getKeys();
    }

    public void loadWhiteList()
    {
    }

    public void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn)
    {
        WorldBorder worldBorder = this.mcServer.worldServers[0].getWorldBorder();
        playerIn.playerNetServerHandler.sendPacket(new S44PacketWorldBorder(worldBorder, S44PacketWorldBorder.Action.INITIALIZE));
        playerIn.playerNetServerHandler.sendPacket(new S03PacketTimeUpdate(worldIn.getTotalWorldTime(), worldIn.getWorldTime(), worldIn.getGameRules().getBoolean("doDaylightCycle")));

        if (worldIn.isRaining())
        {
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(1, 0.0F));
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(7, worldIn.getRainStrength(1.0F)));
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(8, worldIn.getThunderStrength(1.0F)));
        }
    }

    public void syncPlayerInventory(EntityPlayerMP playerIn)
    {
        playerIn.sendContainerToPlayer(playerIn.inventoryContainer);
        playerIn.setPlayerHealthUpdated();
        playerIn.playerNetServerHandler.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
    }

    public int getCurrentPlayerCount()
    {
        return this.playerEntityList.size();
    }

    public int getMaxPlayers()
    {
        return this.maxPlayers;
    }

    public String[] getAvailablePlayerDat()
    {
        return this.mcServer.worldServers[0].getSaveHandler().getPlayerNBTManager().getAvailablePlayerDat();
    }

    public void setWhiteListEnabled(boolean whitelistEnabled)
    {
        this.whiteListEnforced = whitelistEnabled;
    }

    public List<EntityPlayerMP> getPlayersMatchingAddress(String address)
    {
        List<EntityPlayerMP> list = Lists.<EntityPlayerMP>newArrayList();

        for (EntityPlayerMP entityPlayerMP : this.playerEntityList)
        {
            if (entityPlayerMP.getPlayerIP().equals(address))
            {
                list.add(entityPlayerMP);
            }
        }

        return list;
    }

    public int getViewDistance()
    {
        return this.viewDistance;
    }

    public MinecraftServer getServerInstance()
    {
        return this.mcServer;
    }

    public NBTTagCompound getHostPlayerData()
    {
        return null;
    }

    public void setGameType(WorldSettings.GameType gameType)
    {
        this.gameType = gameType;
    }

    private void setPlayerGameTypeBasedOnOther(EntityPlayerMP player, EntityPlayerMP otherPlayer, World worldIn)
    {
        if (otherPlayer != null)
        {
            player.theItemInWorldManager.setGameType(otherPlayer.theItemInWorldManager.getGameType());
        }
        else if (this.gameType != null)
        {
            player.theItemInWorldManager.setGameType(this.gameType);
        }

        player.theItemInWorldManager.initializeGameType(worldIn.getWorldInfo().getGameType());
    }

    public void setCommandsAllowedForAll(boolean commandsAllowedForAll)
    {
        this.commandsAllowedForAll = commandsAllowedForAll;
    }

    public void removeAllPlayers()
    {
        for (int playerIndex = 0; playerIndex < this.playerEntityList.size(); ++playerIndex)
        {
            ((EntityPlayerMP)this.playerEntityList.get(playerIndex)).playerNetServerHandler.kickPlayerFromServer("Server closed");
        }
    }

    public void sendChatMsgImpl(IChatComponent component, boolean isChat)
    {
        this.mcServer.addChatMessage(component);
        byte chatType = (byte)(isChat ? 1 : 0);
        this.sendPacketToAllPlayers(new S02PacketChat(component, chatType));
    }

    public void sendChatMsg(IChatComponent component)
    {
        this.sendChatMsgImpl(component, true);
    }

    public StatisticsFile getPlayerStatsFile(EntityPlayer playerIn)
    {
        UUID playerUuid = playerIn.getUniqueID();
        StatisticsFile statisticsFile = playerUuid == null ? null : (StatisticsFile)this.playerStatFiles.get(playerUuid);

        if (statisticsFile == null)
        {
            File statsDir = new File(this.mcServer.worldServerForDimension(0).getSaveHandler().getWorldDirectory(), "stats");
            File statsFile = new File(statsDir, playerUuid.toString() + ".json");

            if (!statsFile.exists())
            {
                File legacyStatsFile = new File(statsDir, playerIn.getName() + ".json");

                if (legacyStatsFile.exists() && legacyStatsFile.isFile())
                {
                    legacyStatsFile.renameTo(statsFile);
                }
            }

            statisticsFile = new StatisticsFile(this.mcServer, statsFile);
            statisticsFile.readStatFile();
            this.playerStatFiles.put(playerUuid, statisticsFile);
        }

        return statisticsFile;
    }

    public void setViewDistance(int distance)
    {
        this.viewDistance = distance;

        if (this.mcServer.worldServers != null)
        {
            for (WorldServer worldServer : this.mcServer.worldServers)
            {
                if (worldServer != null)
                {
                    worldServer.getPlayerManager().setPlayerViewRadius(distance);
                }
            }
        }
    }

    public List<EntityPlayerMP> getPlayerList()
    {
        return this.playerEntityList;
    }

    public EntityPlayerMP getPlayerByUUID(UUID playerUUID)
    {
        return (EntityPlayerMP)this.uuidToPlayerMap.get(playerUUID);
    }

    public boolean bypassesPlayerLimit(GameProfile profile)
    {
        return false;
    }
}
