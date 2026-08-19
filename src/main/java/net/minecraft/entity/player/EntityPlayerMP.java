package net.minecraft.entity.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerHorseInventory;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMapBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.network.play.server.S31PacketWindowProperty;
import net.minecraft.network.play.server.S36PacketSignEditorOpen;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S42PacketCombatEvent;
import net.minecraft.network.play.server.S43PacketCamera;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.JsonSerializableSet;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityPlayerMP extends EntityPlayer implements ICrafting
{
    private static final Logger logger = LogManager.getLogger();
    private String translator = "en_US";
    public NetHandlerPlayServer playerNetServerHandler;
    public final MinecraftServer mcServer;
    public final ItemInWorldManager theItemInWorldManager;
    public double managedPosX;
    public double managedPosZ;
    public final List<ChunkCoordIntPair> loadedChunks = Lists.<ChunkCoordIntPair>newLinkedList();
    private final List<Integer> destroyedItemsNetCache = Lists.<Integer>newLinkedList();
    private final StatisticsFile statsFile;
    private float combinedHealth = Float.MIN_VALUE;
    private float lastHealth = -1.0E8F;
    private int lastFoodLevel = -99999999;
    private boolean wasHungry = true;
    private int lastExperience = -99999999;
    private int respawnInvulnerabilityTicks = 60;
    private EntityPlayer.EnumChatVisibility chatVisibility;
    private boolean chatColours = true;
    private long playerLastActiveTime = System.currentTimeMillis();
    private Entity spectatingEntity = null;
    private int currentWindowId;
    public boolean isChangingQuantityOnly;
    public int ping;
    public boolean playerConqueredTheEnd;

    public EntityPlayerMP(MinecraftServer server, WorldServer worldIn, GameProfile profile, ItemInWorldManager interactionManager)
    {
        super(worldIn, profile);
        interactionManager.thisPlayerMP = this;
        this.theItemInWorldManager = interactionManager;
        BlockPos blockPos = worldIn.getSpawnPoint();

        if (!worldIn.provider.getHasNoSky() && worldIn.getWorldInfo().getGameType() != WorldSettings.GameType.ADVENTURE)
        {
            int i = Math.max(5, server.getSpawnProtectionSize() - 6);
            int j = MathHelper.floor_double(worldIn.getWorldBorder().getClosestDistance((double)blockPos.getX(), (double)blockPos.getZ()));

            if (j < i)
            {
                i = j;
            }

            if (j <= 1)
            {
                i = 1;
            }

            blockPos = worldIn.getTopSolidOrLiquidBlock(blockPos.add(this.rand.nextInt(i * 2) - i, 0, this.rand.nextInt(i * 2) - i));
        }

        this.mcServer = server;
        this.statsFile = server.getConfigurationManager().getPlayerStatsFile(this);
        this.stepHeight = 0.0F;
        this.moveToBlockPosAndAngles(blockPos, 0.0F, 0.0F);

        while (!worldIn.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.posY < 255.0D)
        {
            this.setPosition(this.posX, this.posY + 1.0D, this.posZ);
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("playerGameType", 99))
        {
            if (MinecraftServer.getServer().getForceGamemode())
            {
                this.theItemInWorldManager.setGameType(MinecraftServer.getServer().getGameType());
            }
            else
            {
                this.theItemInWorldManager.setGameType(WorldSettings.GameType.getByID(tagCompund.getInteger("playerGameType")));
            }
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("playerGameType", this.theItemInWorldManager.getGameType().getID());
    }

    public void addExperienceLevel(int levels)
    {
        super.addExperienceLevel(levels);
        this.lastExperience = -1;
    }

    public void removeExperienceLevel(int levels)
    {
        super.removeExperienceLevel(levels);
        this.lastExperience = -1;
    }

    public void addSelfToInternalCraftingInventory()
    {
        this.openContainer.onCraftGuiOpened(this);
    }

    public void sendEnterCombat()
    {
        super.sendEnterCombat();
        this.playerNetServerHandler.sendPacket(new S42PacketCombatEvent(this.getCombatTracker(), S42PacketCombatEvent.Event.ENTER_COMBAT));
    }

    public void sendEndCombat()
    {
        super.sendEndCombat();
        this.playerNetServerHandler.sendPacket(new S42PacketCombatEvent(this.getCombatTracker(), S42PacketCombatEvent.Event.END_COMBAT));
    }

    public void onUpdate()
    {
        this.theItemInWorldManager.updateBlockRemoving();
        --this.respawnInvulnerabilityTicks;

        if (this.hurtResistantTime > 0)
        {
            --this.hurtResistantTime;
        }

        this.openContainer.detectAndSendChanges();

        if (!this.worldObj.isRemote && !this.openContainer.canInteractWith(this))
        {
            this.closeScreen();
            this.openContainer = this.inventoryContainer;
        }

        while (!this.destroyedItemsNetCache.isEmpty())
        {
            int i = Math.min(this.destroyedItemsNetCache.size(), Integer.MAX_VALUE);
            int[] aint = new int[i];
            Iterator<Integer> iterator = this.destroyedItemsNetCache.iterator();
            int j = 0;

            while (iterator.hasNext() && j < i)
            {
                aint[j++] = iterator.next().intValue();
                iterator.remove();
            }

            this.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(aint));
        }

        if (!this.loadedChunks.isEmpty())
        {
            List<Chunk> list = Lists.<Chunk>newArrayList();
            Iterator<ChunkCoordIntPair> iterator1 = this.loadedChunks.iterator();
            List<TileEntity> list1 = Lists.<TileEntity>newArrayList();

            while (iterator1.hasNext() && list.size() < 10)
            {
                ChunkCoordIntPair chunkCoordIntPair = iterator1.next();

                if (chunkCoordIntPair != null)
                {
                    if (this.worldObj.isBlockLoaded(new BlockPos(chunkCoordIntPair.chunkXPos << 4, 0, chunkCoordIntPair.chunkZPos << 4)))
                    {
                        Chunk chunk = this.worldObj.getChunkFromChunkCoords(chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos);

                        if (chunk.isPopulated())
                        {
                            list.add(chunk);
                            list1.addAll(((WorldServer)this.worldObj).getTileEntitiesIn(chunkCoordIntPair.chunkXPos * 16, 0, chunkCoordIntPair.chunkZPos * 16, chunkCoordIntPair.chunkXPos * 16 + 16, 256, chunkCoordIntPair.chunkZPos * 16 + 16));
                            iterator1.remove();
                        }
                    }
                }
                else
                {
                    iterator1.remove();
                }
            }

            if (!list.isEmpty())
            {
                if (list.size() == 1)
                {
                    this.playerNetServerHandler.sendPacket(new S21PacketChunkData((Chunk)list.get(0), true, 65535));
                }
                else
                {
                    this.playerNetServerHandler.sendPacket(new S26PacketMapChunkBulk(list));
                }

                for (TileEntity tileEntity : list1)
                {
                    this.sendTileEntityUpdate(tileEntity);
                }

                for (Chunk chunk1 : list)
                {
                    this.getServerForPlayer().getEntityTracker().sendEntitiesInChunkToPlayer(this, chunk1);
                }
            }
        }

        Entity entity = this.getSpectatingEntity();

        if (entity != this)
        {
            if (!entity.isEntityAlive())
            {
                this.setSpectatingEntity(this);
            }
            else
            {
                this.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                this.mcServer.getConfigurationManager().serverUpdateMountedMovingPlayer(this);

                if (this.isSneaking())
                {
                    this.setSpectatingEntity(this);
                }
            }
        }
    }

    public void onUpdateEntity()
    {
        try
        {
            super.onUpdate();

            for (int i = 0; i < this.inventory.getSizeInventory(); ++i)
            {
                ItemStack itemStack = this.inventory.getStackInSlot(i);

                if (itemStack != null && itemStack.getItem().isMap())
                {
                    Packet packet = ((ItemMapBase)itemStack.getItem()).createMapDataPacket(itemStack, this.worldObj, this);

                    if (packet != null)
                    {
                        this.playerNetServerHandler.sendPacket(packet);
                    }
                }
            }

            if (this.getHealth() != this.lastHealth || this.lastFoodLevel != this.foodStats.getFoodLevel() || this.foodStats.getSaturationLevel() == 0.0F != this.wasHungry)
            {
                this.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(this.getHealth(), this.foodStats.getFoodLevel(), this.foodStats.getSaturationLevel()));
                this.lastHealth = this.getHealth();
                this.lastFoodLevel = this.foodStats.getFoodLevel();
                this.wasHungry = this.foodStats.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionAmount() != this.combinedHealth)
            {
                this.combinedHealth = this.getHealth() + this.getAbsorptionAmount();

                for (ScoreObjective scoreObjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.health))
                {
                    this.getWorldScoreboard().getValueFromObjective(this.getName(), scoreObjective).setScoreFromPlayers(Arrays.<EntityPlayer>asList(new EntityPlayer[] {this}));
                }
            }

            if (this.experienceTotal != this.lastExperience)
            {
                this.lastExperience = this.experienceTotal;
                this.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(this.experience, this.experienceTotal, this.experienceLevel));
            }

            if (this.ticksExisted % 20 * 5 == 0 && !this.getStatFile().hasAchievementUnlocked(AchievementList.exploreAllBiomes))
            {
                this.updateBiomesExplored();
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Ticking player");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Player being ticked");
            this.addEntityCrashInfo(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    protected void updateBiomesExplored()
    {
        BiomeGenBase biomeGenBase = this.worldObj.getBiomeGenForCoords(new BlockPos(MathHelper.floor_double(this.posX), 0, MathHelper.floor_double(this.posZ)));
        String s = biomeGenBase.biomeName;
        JsonSerializableSet jsonSerializableSet = (JsonSerializableSet)this.getStatFile().getSerializableValue(AchievementList.exploreAllBiomes);

        if (jsonSerializableSet == null)
        {
            jsonSerializableSet = (JsonSerializableSet)this.getStatFile().setSerializableValue(AchievementList.exploreAllBiomes, new JsonSerializableSet());
        }

        jsonSerializableSet.add(s);

        if (this.getStatFile().canUnlockAchievement(AchievementList.exploreAllBiomes) && jsonSerializableSet.size() >= BiomeGenBase.explorationBiomesList.size())
        {
            Set<BiomeGenBase> set = Sets.newHashSet(BiomeGenBase.explorationBiomesList);

            for (String visitedBiomeName : jsonSerializableSet)
            {
                Iterator<BiomeGenBase> iterator = set.iterator();

                while (iterator.hasNext())
                {
                    BiomeGenBase biomegenbase1 = (BiomeGenBase)iterator.next();

                    if (biomegenbase1.biomeName.equals(visitedBiomeName))
                    {
                        iterator.remove();
                    }
                }

                if (set.isEmpty())
                {
                    break;
                }
            }

            if (set.isEmpty())
            {
                this.triggerAchievement(AchievementList.exploreAllBiomes);
            }
        }
    }

    public void onDeath(DamageSource cause)
    {
        if (this.worldObj.getGameRules().getBoolean("showDeathMessages"))
        {
            Team team = this.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS)
            {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS)
                {
                    this.mcServer.getConfigurationManager().sendMessageToAllTeamMembers(this, this.getCombatTracker().getDeathMessage());
                }
                else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM)
                {
                    this.mcServer.getConfigurationManager().sendMessageToTeamOrEvryPlayer(this, this.getCombatTracker().getDeathMessage());
                }
            }
            else
            {
                this.mcServer.getConfigurationManager().sendChatMsg(this.getCombatTracker().getDeathMessage());
            }
        }

        if (!this.worldObj.getGameRules().getBoolean("keepInventory"))
        {
            this.inventory.dropAllItems();
        }

        for (ScoreObjective scoreObjective : this.worldObj.getScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.deathCount))
        {
            Score score = this.getWorldScoreboard().getValueFromObjective(this.getName(), scoreObjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (entitylivingbase != null)
        {
            EntityList.EntityEggInfo entitylist$entityegginfo = (EntityList.EntityEggInfo)EntityList.entityEggs.get(Integer.valueOf(EntityList.getEntityID(entitylivingbase)));

            if (entitylist$entityegginfo != null)
            {
                this.triggerAchievement(entitylist$entityegginfo.killedByEntityStat);
            }

            entitylivingbase.addToPlayerScore(this, this.scoreValue);
        }

        this.triggerAchievement(StatList.deathsStat);
        this.resetStat(StatList.timeSinceDeathStat);
        this.getCombatTracker().reset();
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            boolean flag = this.mcServer.isDedicatedServer() && this.canPlayersAttack() && "fall".equals(source.damageType);

            if (!flag && this.respawnInvulnerabilityTicks > 0 && source != DamageSource.outOfWorld)
            {
                return false;
            }
            else
            {
                if (source instanceof EntityDamageSource)
                {
                    Entity entity = source.getEntity();

                    if (entity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entity))
                    {
                        return false;
                    }

                    if (entity instanceof EntityArrow)
                    {
                        EntityArrow entityArrow = (EntityArrow)entity;

                        if (entityArrow.shootingEntity instanceof EntityPlayer && !this.canAttackPlayer((EntityPlayer)entityArrow.shootingEntity))
                        {
                            return false;
                        }
                    }
                }

                return super.attackEntityFrom(source, amount);
            }
        }
    }

    public boolean canAttackPlayer(EntityPlayer other)
    {
        return !this.canPlayersAttack() ? false : super.canAttackPlayer(other);
    }

    private boolean canPlayersAttack()
    {
        return this.mcServer.isPVPEnabled();
    }

    public void travelToDimension(int dimensionId)
    {
        if (this.dimension == 1 && dimensionId == 1)
        {
            this.triggerAchievement(AchievementList.theEnd2);
            this.worldObj.removeEntity(this);
            this.playerConqueredTheEnd = true;
            this.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(4, 0.0F));
        }
        else
        {
            if (this.dimension == 0 && dimensionId == 1)
            {
                this.triggerAchievement(AchievementList.theEnd);
                BlockPos blockPos = this.mcServer.worldServerForDimension(dimensionId).getSpawnCoordinate();

                if (blockPos != null)
                {
                    this.playerNetServerHandler.setPlayerLocation((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 0.0F, 0.0F);
                }

                dimensionId = 1;
            }
            else
            {
                this.triggerAchievement(AchievementList.portal);
            }

            this.mcServer.getConfigurationManager().transferPlayerToDimension(this, dimensionId);
            this.lastExperience = -1;
            this.lastHealth = -1.0F;
            this.lastFoodLevel = -1;
        }
    }

    public boolean isSpectatedByPlayer(EntityPlayerMP player)
    {
        return player.isSpectator() ? this.getSpectatingEntity() == this : (this.isSpectator() ? false : super.isSpectatedByPlayer(player));
    }

    private void sendTileEntityUpdate(TileEntity tileEntity)
    {
        if (tileEntity != null)
        {
            Packet packet = tileEntity.getDescriptionPacket();

            if (packet != null)
            {
                this.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    public void onItemPickup(Entity entity, int quantity)
    {
        super.onItemPickup(entity, quantity);
        this.openContainer.detectAndSendChanges();
    }

    public EntityPlayer.EnumStatus trySleep(BlockPos bedLocation)
    {
        EntityPlayer.EnumStatus entityplayer$enumstatus = super.trySleep(bedLocation);

        if (entityplayer$enumstatus == EntityPlayer.EnumStatus.OK)
        {
            Packet packet = new S0APacketUseBed(this, bedLocation);
            this.getServerForPlayer().getEntityTracker().sendToAllTrackingEntity(this, packet);
            this.playerNetServerHandler.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.playerNetServerHandler.sendPacket(packet);
        }

        return entityplayer$enumstatus;
    }

    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn)
    {
        if (this.isPlayerSleeping())
        {
            this.getServerForPlayer().getEntityTracker().sendToTrackingAndSelf(this, new S0BPacketAnimation(this, 2));
        }

        super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);

        if (this.playerNetServerHandler != null)
        {
            this.playerNetServerHandler.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }

    public void mountEntity(Entity entityIn)
    {
        Entity entity = this.ridingEntity;
        super.mountEntity(entityIn);

        if (entityIn != entity)
        {
            this.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(0, this, this.ridingEntity));
            this.playerNetServerHandler.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
    }

    public void handleFalling(double y, boolean onGround)
    {
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
        int k = MathHelper.floor_double(this.posZ);
        BlockPos blockPos = new BlockPos(i, j, k);
        Block block = this.worldObj.getBlockState(blockPos).getBlock();

        if (block.getMaterial() == Material.air)
        {
            BlockPos blockPosDown = blockPos.down();
            Block block1 = this.worldObj.getBlockState(blockPosDown).getBlock();

            if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate)
            {
                blockPos = blockPosDown;
                block = block1;
            }
        }

        super.updateFallState(y, onGround, block, blockPos);
    }

    public void openEditSign(TileEntitySign signTile)
    {
        signTile.setPlayer(this);
        this.playerNetServerHandler.sendPacket(new S36PacketSignEditorOpen(signTile.getPos()));
    }

    private void getNextWindowId()
    {
        this.currentWindowId = this.currentWindowId % 100 + 1;
    }

    public void displayGui(IInteractionObject guiOwner)
    {
        this.getNextWindowId();
        this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, guiOwner.getGuiID(), guiOwner.getDisplayName()));
        this.openContainer = guiOwner.createContainer(this.inventory, this);
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.onCraftGuiOpened(this);
    }

    public void displayGUIChest(IInventory chestInventory)
    {
        if (this.openContainer != this.inventoryContainer)
        {
            this.closeScreen();
        }

        if (chestInventory instanceof ILockableContainer)
        {
            ILockableContainer ilockablecontainer = (ILockableContainer)chestInventory;

            if (ilockablecontainer.isLocked() && !this.canOpen(ilockablecontainer.getLockCode()) && !this.isSpectator())
            {
                this.playerNetServerHandler.sendPacket(new S02PacketChat(new ChatComponentTranslation("container.isLocked", new Object[] {chestInventory.getDisplayName()}), (byte)2));
                this.playerNetServerHandler.sendPacket(new S29PacketSoundEffect("random.door_close", this.posX, this.posY, this.posZ, 1.0F, 1.0F));
                return;
            }
        }

        this.getNextWindowId();

        if (chestInventory instanceof IInteractionObject)
        {
            this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, ((IInteractionObject)chestInventory).getGuiID(), chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
            this.openContainer = ((IInteractionObject)chestInventory).createContainer(this.inventory, this);
        }
        else
        {
            this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, "minecraft:container", chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
            this.openContainer = new ContainerChest(this.inventory, chestInventory, this);
        }

        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.onCraftGuiOpened(this);
    }

    public void displayVillagerTradeGui(IMerchant villager)
    {
        this.getNextWindowId();
        this.openContainer = new ContainerMerchant(this.inventory, villager, this.worldObj);
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.onCraftGuiOpened(this);
        IInventory iinventory = ((ContainerMerchant)this.openContainer).getMerchantInventory();
        IChatComponent ichatcomponent = villager.getDisplayName();
        this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, "minecraft:villager", ichatcomponent, iinventory.getSizeInventory()));
        MerchantRecipeList merchantRecipeList = villager.getRecipes(this);

        if (merchantRecipeList != null)
        {
            PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
            packetBuffer.writeInt(this.currentWindowId);
            merchantRecipeList.writeToBuf(packetBuffer);
            this.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|TrList", packetBuffer));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory)
    {
        if (this.openContainer != this.inventoryContainer)
        {
            this.closeScreen();
        }

        this.getNextWindowId();
        this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, "EntityHorse", horseInventory.getDisplayName(), horseInventory.getSizeInventory(), horse.getEntityId()));
        this.openContainer = new ContainerHorseInventory(this.inventory, horseInventory, horse, this);
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.onCraftGuiOpened(this);
    }

    public void displayGUIBook(ItemStack bookStack)
    {
        Item item = bookStack.getItem();

        if (item == Items.written_book)
        {
            this.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|BOpen", new PacketBuffer(Unpooled.buffer())));
        }
    }

    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
    {
        if (!(containerToSend.getSlot(slotInd) instanceof SlotCrafting))
        {
            if (!this.isChangingQuantityOnly)
            {
                this.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(containerToSend.windowId, slotInd, stack));
            }
        }
    }

    public void sendContainerToPlayer(Container container)
    {
        this.updateCraftingInventory(container, container.getInventory());
    }

    public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList)
    {
        this.playerNetServerHandler.sendPacket(new S30PacketWindowItems(containerToSend.windowId, itemsList));
        this.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.inventory.getItemStack()));
    }

    public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue)
    {
        this.playerNetServerHandler.sendPacket(new S31PacketWindowProperty(containerIn.windowId, varToUpdate, newValue));
    }

    public void sendAllWindowProperties(Container container, IInventory inventory)
    {
        for (int i = 0; i < inventory.getFieldCount(); ++i)
        {
            this.playerNetServerHandler.sendPacket(new S31PacketWindowProperty(container.windowId, i, inventory.getField(i)));
        }
    }

    public void closeScreen()
    {
        this.playerNetServerHandler.sendPacket(new S2EPacketCloseWindow(this.openContainer.windowId));
        this.closeContainer();
    }

    public void updateHeldItem()
    {
        if (!this.isChangingQuantityOnly)
        {
            this.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, this.inventory.getItemStack()));
        }
    }

    public void closeContainer()
    {
        this.openContainer.onContainerClosed(this);
        this.openContainer = this.inventoryContainer;
    }

    public void setEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking)
    {
        if (this.ridingEntity != null)
        {
            if (strafe >= -1.0F && strafe <= 1.0F)
            {
                this.moveStrafing = strafe;
            }

            if (forward >= -1.0F && forward <= 1.0F)
            {
                this.moveForward = forward;
            }

            this.isJumping = jumping;
            this.setSneaking(sneaking);
        }
    }

    public void addStat(StatBase stat, int amount)
    {
        if (stat != null)
        {
            this.statsFile.increaseStat(this, stat, amount);

            for (ScoreObjective scoreObjective : this.getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria()))
            {
                this.getWorldScoreboard().getValueFromObjective(this.getName(), scoreObjective).increseScore(amount);
            }

            if (this.statsFile.hasUnsentAchievement())
            {
                this.statsFile.sendStats(this);
            }
        }
    }

    public void resetStat(StatBase stat)
    {
        if (stat != null)
        {
            this.statsFile.unlockAchievement(this, stat, 0);

            for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(stat.getCriteria()))
            {
                this.getWorldScoreboard().getValueFromObjective(this.getName(), scoreobjective).setScorePoints(0);
            }

            if (this.statsFile.hasUnsentAchievement())
            {
                this.statsFile.sendStats(this);
            }
        }
    }

    public void mountEntityAndWakeUp()
    {
        if (this.riddenByEntity != null)
        {
            this.riddenByEntity.mountEntity(this);
        }

        if (this.sleeping)
        {
            this.wakeUpPlayer(true, false, false);
        }
    }

    public void setPlayerHealthUpdated()
    {
        this.lastHealth = -1.0E8F;
    }

    public void addChatComponentMessage(IChatComponent chatComponent)
    {
        this.playerNetServerHandler.sendPacket(new S02PacketChat(chatComponent));
    }

    protected void onItemUseFinish()
    {
        this.playerNetServerHandler.sendPacket(new S19PacketEntityStatus(this, (byte)9));
        super.onItemUseFinish();
    }

    public void setItemInUse(ItemStack stack, int duration)
    {
        super.setItemInUse(stack, duration);

        if (stack != null && stack.getItem() != null && stack.getItem().getItemUseAction(stack) == EnumAction.EAT)
        {
            this.getServerForPlayer().getEntityTracker().sendToTrackingAndSelf(this, new S0BPacketAnimation(this, 3));
        }
    }

    public void clonePlayer(EntityPlayer oldPlayer, boolean respawnFromEnd)
    {
        super.clonePlayer(oldPlayer, respawnFromEnd);
        this.lastExperience = -1;
        this.lastHealth = -1.0F;
        this.lastFoodLevel = -1;
        this.destroyedItemsNetCache.addAll(((EntityPlayerMP)oldPlayer).destroyedItemsNetCache);
    }

    protected void onNewPotionEffect(PotionEffect id)
    {
        super.onNewPotionEffect(id);
        this.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(this.getEntityId(), id));
    }

    protected void onChangedPotionEffect(PotionEffect id, boolean reapplyAttributes)
    {
        super.onChangedPotionEffect(id, reapplyAttributes);
        this.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(this.getEntityId(), id));
    }

    protected void onFinishedPotionEffect(PotionEffect effect)
    {
        super.onFinishedPotionEffect(effect);
        this.playerNetServerHandler.sendPacket(new S1EPacketRemoveEntityEffect(this.getEntityId(), effect));
    }

    public void setPositionAndUpdate(double x, double y, double z)
    {
        this.playerNetServerHandler.setPlayerLocation(x, y, z, this.rotationYaw, this.rotationPitch);
    }

    public void onCriticalHit(Entity entityHit)
    {
        this.getServerForPlayer().getEntityTracker().sendToTrackingAndSelf(this, new S0BPacketAnimation(entityHit, 4));
    }

    public void onEnchantmentCritical(Entity entityHit)
    {
        this.getServerForPlayer().getEntityTracker().sendToTrackingAndSelf(this, new S0BPacketAnimation(entityHit, 5));
    }

    public void sendPlayerAbilities()
    {
        if (this.playerNetServerHandler != null)
        {
            this.playerNetServerHandler.sendPacket(new S39PacketPlayerAbilities(this.capabilities));
            this.updatePotionMetadata();
        }
    }

    public WorldServer getServerForPlayer()
    {
        return (WorldServer)this.worldObj;
    }

    public void setGameType(WorldSettings.GameType gameType)
    {
        this.theItemInWorldManager.setGameType(gameType);
        this.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(3, (float)gameType.getID()));

        if (gameType == WorldSettings.GameType.SPECTATOR)
        {
            this.mountEntity((Entity)null);
        }
        else
        {
            this.setSpectatingEntity(this);
        }

        this.sendPlayerAbilities();
        this.markPotionsDirty();
    }

    public boolean isSpectator()
    {
        return this.theItemInWorldManager.getGameType() == WorldSettings.GameType.SPECTATOR;
    }

    public void addChatMessage(IChatComponent component)
    {
        this.playerNetServerHandler.sendPacket(new S02PacketChat(component));
    }

    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        if ("seed".equals(commandName) && !this.mcServer.isDedicatedServer())
        {
            return true;
        }
        else if (!"tell".equals(commandName) && !"help".equals(commandName) && !"me".equals(commandName) && !"trigger".equals(commandName))
        {
            if (this.mcServer.getConfigurationManager().canSendCommands(this.getGameProfile()))
            {
                UserListOpsEntry userListOpsEntry = (UserListOpsEntry)this.mcServer.getConfigurationManager().getOppedPlayers().getEntry(this.getGameProfile());
                return userListOpsEntry != null ? userListOpsEntry.getPermissionLevel() >= permLevel : this.mcServer.getOpPermissionLevel() >= permLevel;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    public String getPlayerIP()
    {
        String s = this.playerNetServerHandler.netManager.getRemoteAddress().toString();
        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));
        return s;
    }

    public void handleClientSettings(C15PacketClientSettings packetIn)
    {
        this.translator = packetIn.getLang();
        this.chatVisibility = packetIn.getChatVisibility();
        this.chatColours = packetIn.isColorsEnabled();
        this.getDataWatcher().updateObject(10, Byte.valueOf((byte)packetIn.getModelPartFlags()));
    }

    public EntityPlayer.EnumChatVisibility getChatVisibility()
    {
        return this.chatVisibility;
    }

    public void loadResourcePack(String url, String hash)
    {
        this.playerNetServerHandler.sendPacket(new S48PacketResourcePackSend(url, hash));
    }

    public BlockPos getPosition()
    {
        return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
    }

    public void markPlayerActive()
    {
        this.playerLastActiveTime = MinecraftServer.getCurrentTimeMillis();
    }

    public StatisticsFile getStatFile()
    {
        return this.statsFile;
    }

    public void removeEntity(Entity entity)
    {
        if (entity instanceof EntityPlayer)
        {
            this.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(new int[] {entity.getEntityId()}));
        }
        else
        {
            this.destroyedItemsNetCache.add(Integer.valueOf(entity.getEntityId()));
        }
    }

    protected void updatePotionMetadata()
    {
        if (this.isSpectator())
        {
            this.resetPotionEffectMetadata();
            this.setInvisible(true);
        }
        else
        {
            super.updatePotionMetadata();
        }

        this.getServerForPlayer().getEntityTracker().updateVisibility(this);
    }

    public Entity getSpectatingEntity()
    {
        return (Entity)(this.spectatingEntity == null ? this : this.spectatingEntity);
    }

    public void setSpectatingEntity(Entity entityToSpectate)
    {
        Entity entity = this.getSpectatingEntity();
        this.spectatingEntity = (Entity)(entityToSpectate == null ? this : entityToSpectate);

        if (entity != this.spectatingEntity)
        {
            this.playerNetServerHandler.sendPacket(new S43PacketCamera(this.spectatingEntity));
            this.setPositionAndUpdate(this.spectatingEntity.posX, this.spectatingEntity.posY, this.spectatingEntity.posZ);
        }
    }

    public void attackTargetEntityWithCurrentItem(Entity targetEntity)
    {
        if (this.theItemInWorldManager.getGameType() == WorldSettings.GameType.SPECTATOR)
        {
            this.setSpectatingEntity(targetEntity);
        }
        else
        {
            super.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    public long getLastActiveTime()
    {
        return this.playerLastActiveTime;
    }

    public IChatComponent getTabListDisplayName()
    {
        return null;
    }
}
