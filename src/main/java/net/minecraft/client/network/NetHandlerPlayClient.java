package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiScreenDemo;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.player.inventory.LocalBlockIntercommunication;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S11PacketSpawnExperienceOrb;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S28PacketEffect;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.network.play.server.S31PacketWindowProperty;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S36PacketSignEditorOpen;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.network.play.server.S42PacketCombatEvent;
import net.minecraft.network.play.server.S43PacketCamera;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.network.play.server.S46PacketSetCompressionLevel;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.network.play.server.S49PacketUpdateEntityNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayClient implements INetHandlerPlayClient
{
    private static final Logger logger = LogManager.getLogger();
    private final NetworkManager netManager;
    private final GameProfile profile;
    private final GuiScreen guiScreenServer;
    private Minecraft gameController;
    private WorldClient clientWorldController;
    private boolean doneLoadingTerrain;
    private final Map<UUID, NetworkPlayerInfo> playerInfoMap = Maps.<UUID, NetworkPlayerInfo>newHashMap();
    public int currentServerMaxPlayers = 20;
    private boolean hasReceivedStatistics = false;
    private final Random avRandomizer = new Random();

    public NetHandlerPlayClient(Minecraft mcIn, GuiScreen guiScreenServerIn, NetworkManager networkManagerIn, GameProfile profileIn)
    {
        this.gameController = mcIn;
        this.guiScreenServer = guiScreenServerIn;
        this.netManager = networkManagerIn;
        this.profile = profileIn;
    }

    public void cleanup()
    {
        this.clientWorldController = null;
    }

    public void handleJoinGame(S01PacketJoinGame packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.playerController = new PlayerControllerMP(this.gameController, this);
        this.clientWorldController = new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.gameController.mcProfiler);
        this.gameController.gameSettings.difficulty = packetIn.getDifficulty();
        this.gameController.loadWorld(this.clientWorldController);
        this.gameController.thePlayer.dimension = packetIn.getDimension();
        this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
        this.gameController.thePlayer.setEntityId(packetIn.getEntityId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.gameController.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        this.gameController.playerController.setGameType(packetIn.getGameType());
        this.gameController.gameSettings.sendSettingsToServer();
        this.netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
    }

    public void handleSpawnObject(S0EPacketSpawnObject packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        double seventhDoubleValue = (double)packetIn.getX() / 32.0D;
        double ninthDoubleValue = (double)packetIn.getY() / 32.0D;
        double thirteenthDoubleValue = (double)packetIn.getZ() / 32.0D;
        Entity entity = null;

        if (packetIn.getType() == 10)
        {
            entity = EntityMinecart.getMinecart(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, EntityMinecart.EnumMinecartType.byNetworkID(packetIn.getData()));
        }
        else if (packetIn.getType() == 90)
        {
            Entity entity1 = this.clientWorldController.getEntityByID(packetIn.getData());

            if (entity1 instanceof EntityPlayer)
            {
                entity = new EntityFishHook(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, (EntityPlayer)entity1);
            }

            packetIn.setData(0);
        }
        else if (packetIn.getType() == 60)
        {
            entity = new EntityArrow(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 61)
        {
            entity = new EntitySnowball(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 71)
        {
            entity = new EntityItemFrame(this.clientWorldController, new BlockPos(MathHelper.floor_double(seventhDoubleValue), MathHelper.floor_double(ninthDoubleValue), MathHelper.floor_double(thirteenthDoubleValue)), EnumFacing.getHorizontal(packetIn.getData()));
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 77)
        {
            entity = new EntityLeashKnot(this.clientWorldController, new BlockPos(MathHelper.floor_double(seventhDoubleValue), MathHelper.floor_double(ninthDoubleValue), MathHelper.floor_double(thirteenthDoubleValue)));
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 65)
        {
            entity = new EntityEnderPearl(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 72)
        {
            entity = new EntityEnderEye(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 76)
        {
            entity = new EntityFireworkRocket(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, (ItemStack)null);
        }
        else if (packetIn.getType() == 63)
        {
            entity = new EntityLargeFireball(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, (double)packetIn.getSpeedX() / 8000.0D, (double)packetIn.getSpeedY() / 8000.0D, (double)packetIn.getSpeedZ() / 8000.0D);
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 64)
        {
            entity = new EntitySmallFireball(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, (double)packetIn.getSpeedX() / 8000.0D, (double)packetIn.getSpeedY() / 8000.0D, (double)packetIn.getSpeedZ() / 8000.0D);
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 66)
        {
            entity = new EntityWitherSkull(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, (double)packetIn.getSpeedX() / 8000.0D, (double)packetIn.getSpeedY() / 8000.0D, (double)packetIn.getSpeedZ() / 8000.0D);
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 62)
        {
            entity = new EntityEgg(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 73)
        {
            entity = new EntityPotion(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, packetIn.getData());
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 75)
        {
            entity = new EntityExpBottle(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
            packetIn.setData(0);
        }
        else if (packetIn.getType() == 1)
        {
            entity = new EntityBoat(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 50)
        {
            entity = new EntityTNTPrimed(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, (EntityLivingBase)null);
        }
        else if (packetIn.getType() == 78)
        {
            entity = new EntityArmorStand(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 51)
        {
            entity = new EntityEnderCrystal(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 2)
        {
            entity = new EntityItem(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue);
        }
        else if (packetIn.getType() == 70)
        {
            entity = new EntityFallingBlock(this.clientWorldController, seventhDoubleValue, ninthDoubleValue, thirteenthDoubleValue, Block.getStateById(packetIn.getData() & 65535));
            packetIn.setData(0);
        }

        if (entity != null)
        {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            entity.rotationPitch = (float)(packetIn.getPitch() * 360) / 256.0F;
            entity.rotationYaw = (float)(packetIn.getYaw() * 360) / 256.0F;
            Entity[] aentity = entity.getParts();

            if (aentity != null)
            {
                int i = packetIn.getEntityID() - entity.getEntityId();

                for (int j = 0; j < aentity.length; ++j)
                {
                    aentity[j].setEntityId(aentity[j].getEntityId() + i);
                }
            }

            entity.setEntityId(packetIn.getEntityID());
            this.clientWorldController.addEntityToWorld(packetIn.getEntityID(), entity);

            if (packetIn.getData() > 0)
            {
                if (packetIn.getType() == 60)
                {
                    Entity entity2 = this.clientWorldController.getEntityByID(packetIn.getData());

                    if (entity2 instanceof EntityLivingBase && entity instanceof EntityArrow)
                    {
                        ((EntityArrow)entity).shootingEntity = entity2;
                    }
                }

                entity.setVelocity((double)packetIn.getSpeedX() / 8000.0D, (double)packetIn.getSpeedY() / 8000.0D, (double)packetIn.getSpeedZ() / 8000.0D);
            }
        }
    }

    public void handleSpawnExperienceOrb(S11PacketSpawnExperienceOrb packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = new EntityXPOrb(this.clientWorldController, (double)packetIn.getX() / 32.0D, (double)packetIn.getY() / 32.0D, (double)packetIn.getZ() / 32.0D, packetIn.getXPValue());
        entity.serverPosX = packetIn.getX();
        entity.serverPosY = packetIn.getY();
        entity.serverPosZ = packetIn.getZ();
        entity.rotationYaw = 0.0F;
        entity.rotationPitch = 0.0F;
        entity.setEntityId(packetIn.getEntityID());
        this.clientWorldController.addEntityToWorld(packetIn.getEntityID(), entity);
    }

    public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        double xCoordinate = (double)packetIn.getX() / 32.0D;
        double yCoordinate = (double)packetIn.getY() / 32.0D;
        double zCoordinate = (double)packetIn.getZ() / 32.0D;
        Entity entity = null;

        if (packetIn.getType() == 1)
        {
            entity = new EntityLightningBolt(this.clientWorldController, xCoordinate, yCoordinate, zCoordinate);
        }

        if (entity != null)
        {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            entity.rotationYaw = 0.0F;
            entity.rotationPitch = 0.0F;
            entity.setEntityId(packetIn.getEntityId());
            this.clientWorldController.addWeatherEffect(entity);
        }
    }

    public void handleSpawnPainting(S10PacketSpawnPainting packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPainting entityPainting = new EntityPainting(this.clientWorldController, packetIn.getPosition(), packetIn.getFacing(), packetIn.getTitle());
        this.clientWorldController.addEntityToWorld(packetIn.getEntityID(), entityPainting);
    }

    public void handleEntityVelocity(S12PacketEntityVelocity packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entity != null)
        {
            entity.setVelocity((double)packetIn.getMotionX() / 8000.0D, (double)packetIn.getMotionY() / 8000.0D, (double)packetIn.getMotionZ() / 8000.0D);
        }
    }

    public void handleEntityMetadata(S1CPacketEntityMetadata packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity != null && packetIn.getWatchedObjects() != null)
        {
            entity.getDataWatcher().updateWatchedObjectsFromList(packetIn.getWatchedObjects());
        }
    }

    public void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        double doubleValue = (double)packetIn.getX() / 32.0D;
        double secondDoubleValue = (double)packetIn.getY() / 32.0D;
        double thirdDoubleValue = (double)packetIn.getZ() / 32.0D;
        float f = (float)(packetIn.getYaw() * 360) / 256.0F;
        float floatValue = (float)(packetIn.getPitch() * 360) / 256.0F;
        EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(this.gameController.theWorld, this.getPlayerInfo(packetIn.getPlayer()).getGameProfile());
        entityotherplayermp.prevPosX = entityotherplayermp.lastTickPosX = (double)(entityotherplayermp.serverPosX = packetIn.getX());
        entityotherplayermp.prevPosY = entityotherplayermp.lastTickPosY = (double)(entityotherplayermp.serverPosY = packetIn.getY());
        entityotherplayermp.prevPosZ = entityotherplayermp.lastTickPosZ = (double)(entityotherplayermp.serverPosZ = packetIn.getZ());
        int i = packetIn.getCurrentItemID();

        if (i == 0)
        {
            entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = null;
        }
        else
        {
            entityotherplayermp.inventory.mainInventory[entityotherplayermp.inventory.currentItem] = new ItemStack(Item.getItemById(i), 1, 0);
        }

        entityotherplayermp.setPositionAndRotation(doubleValue, secondDoubleValue, thirdDoubleValue, f, floatValue);
        this.clientWorldController.addEntityToWorld(packetIn.getEntityID(), entityotherplayermp);
        List<DataWatcher.WatchableObject> list = packetIn.getWatchedObjects();

        if (list != null)
        {
            entityotherplayermp.getDataWatcher().updateWatchedObjectsFromList(list);
        }
    }

    public void handleEntityTeleport(S18PacketEntityTeleport packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity != null)
        {
            entity.serverPosX = packetIn.getX();
            entity.serverPosY = packetIn.getY();
            entity.serverPosZ = packetIn.getZ();
            double sixthDoubleValue = (double)entity.serverPosX / 32.0D;
            double eighthDoubleValue = (double)entity.serverPosY / 32.0D;
            double twelfthDoubleValue = (double)entity.serverPosZ / 32.0D;
            float f = (float)(packetIn.getYaw() * 360) / 256.0F;
            float secondFloatValue = (float)(packetIn.getPitch() * 360) / 256.0F;

            if (Math.abs(entity.posX - sixthDoubleValue) < 0.03125D && Math.abs(entity.posY - eighthDoubleValue) < 0.015625D && Math.abs(entity.posZ - twelfthDoubleValue) < 0.03125D)
            {
                entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, f, secondFloatValue, 3, true);
            }
            else
            {
                entity.setPositionAndRotation2(sixthDoubleValue, eighthDoubleValue, twelfthDoubleValue, f, secondFloatValue, 3, true);
            }

            entity.onGround = packetIn.getOnGround();
        }
    }

    public void handleHeldItemChange(S09PacketHeldItemChange packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (packetIn.getHeldItemHotbarIndex() >= 0 && packetIn.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize())
        {
            this.gameController.thePlayer.inventory.currentItem = packetIn.getHeldItemHotbarIndex();
        }
    }

    public void handleEntityMovement(S14PacketEntity packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
        {
            entity.serverPosX += packetIn.getPosX();
            entity.serverPosY += packetIn.getPosY();
            entity.serverPosZ += packetIn.getPosZ();
            double doubleValue = (double)entity.serverPosX / 32.0D;
            double doubleValue2 = (double)entity.serverPosY / 32.0D;
            double doubleValue3 = (double)entity.serverPosZ / 32.0D;
            float f = packetIn.isRotating() ? (float)(packetIn.getYaw() * 360) / 256.0F : entity.rotationYaw;
            float floatValue2 = packetIn.isRotating() ? (float)(packetIn.getPitch() * 360) / 256.0F : entity.rotationPitch;
            entity.setPositionAndRotation2(doubleValue, doubleValue2, doubleValue3, f, floatValue2, 3, false);
            entity.onGround = packetIn.getOnGround();
        }
    }

    public void handleEntityHeadLook(S19PacketEntityHeadLook packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
        {
            float f = (float)(packetIn.getYaw() * 360) / 256.0F;
            entity.setRotationYawHead(f);
        }
    }

    public void handleDestroyEntities(S13PacketDestroyEntities packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        for (int i = 0; i < packetIn.getEntityIDs().length; ++i)
        {
            this.clientWorldController.removeEntityFromWorld(packetIn.getEntityIDs()[i]);
        }
    }

    public void handlePlayerPosLook(S08PacketPlayerPosLook packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayer entityplayer = this.gameController.thePlayer;
        double doubleValue = packetIn.getX();
        double secondDoubleValue = packetIn.getY();
        double thirdDoubleValue = packetIn.getZ();
        float f = packetIn.getYaw();
        float floatValue = packetIn.getPitch();

        if (packetIn.getFlags().contains(S08PacketPlayerPosLook.EnumFlags.X))
        {
            doubleValue += entityplayer.posX;
        }
        else
        {
            entityplayer.motionX = 0.0D;
        }

        if (packetIn.getFlags().contains(S08PacketPlayerPosLook.EnumFlags.Y))
        {
            secondDoubleValue += entityplayer.posY;
        }
        else
        {
            entityplayer.motionY = 0.0D;
        }

        if (packetIn.getFlags().contains(S08PacketPlayerPosLook.EnumFlags.Z))
        {
            thirdDoubleValue += entityplayer.posZ;
        }
        else
        {
            entityplayer.motionZ = 0.0D;
        }

        if (packetIn.getFlags().contains(S08PacketPlayerPosLook.EnumFlags.X_ROT))
        {
            floatValue += entityplayer.rotationPitch;
        }

        if (packetIn.getFlags().contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT))
        {
            f += entityplayer.rotationYaw;
        }

        entityplayer.setPositionAndRotation(doubleValue, secondDoubleValue, thirdDoubleValue, f, floatValue);
        this.netManager.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(entityplayer.posX, entityplayer.getEntityBoundingBox().minY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch, false));

        if (!this.doneLoadingTerrain)
        {
            this.gameController.thePlayer.prevPosX = this.gameController.thePlayer.posX;
            this.gameController.thePlayer.prevPosY = this.gameController.thePlayer.posY;
            this.gameController.thePlayer.prevPosZ = this.gameController.thePlayer.posZ;
            this.doneLoadingTerrain = true;
            this.gameController.displayGuiScreen((GuiScreen)null);
        }
    }

    public void handleMultiBlockChange(S22PacketMultiBlockChange packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        for (S22PacketMultiBlockChange.BlockUpdateData s22packetmultiblockchange$blockupdatedata : packetIn.getChangedBlocks())
        {
            this.clientWorldController.invalidateRegionAndSetBlock(s22packetmultiblockchange$blockupdatedata.getPos(), s22packetmultiblockchange$blockupdatedata.getBlockState());
        }
    }

    public void handleChunkData(S21PacketChunkData packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (packetIn.isFullChunk())
        {
            if (packetIn.getExtractedSize() == 0)
            {
                this.clientWorldController.doPreChunk(packetIn.getChunkX(), packetIn.getChunkZ(), false);
                return;
            }

            this.clientWorldController.doPreChunk(packetIn.getChunkX(), packetIn.getChunkZ(), true);
        }

        this.clientWorldController.invalidateBlockReceiveRegion(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);
        Chunk chunk = this.clientWorldController.getChunkFromChunkCoords(packetIn.getChunkX(), packetIn.getChunkZ());
        chunk.fillChunk(packetIn.getExtractedDataBytes(), packetIn.getExtractedSize(), packetIn.isFullChunk());
        this.clientWorldController.markBlockRangeForRenderUpdate(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);

        if (!packetIn.isFullChunk() || !(this.clientWorldController.provider instanceof WorldProviderSurface))
        {
            chunk.resetRelightChecks();
        }
    }

    public void handleBlockChange(S23PacketBlockChange packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.clientWorldController.invalidateRegionAndSetBlock(packetIn.getBlockPosition(), packetIn.getBlockState());
    }

    public void handleDisconnect(S40PacketDisconnect packetIn)
    {
        this.netManager.closeChannel(packetIn.getReason());
    }

    public void onDisconnect(IChatComponent reason)
    {
        this.gameController.loadWorld((WorldClient)null);

        if (this.guiScreenServer != null)
        {
            this.gameController.displayGuiScreen(new GuiDisconnected(this.guiScreenServer, "disconnect.lost", reason));
        }
        else
        {
            this.gameController.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", reason));
        }
    }

    public void addToSendQueue(Packet packetIn)
    {
        this.netManager.sendPacket(packetIn);
    }

    public void handleCollectItem(S0DPacketCollectItem packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getCollectedItemEntityID());
        EntityLivingBase entityLivingBase = (EntityLivingBase)this.clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entityLivingBase == null)
        {
            entityLivingBase = this.gameController.thePlayer;
        }

        if (entity != null)
        {
            if (entity instanceof EntityXPOrb)
            {
                this.clientWorldController.playSoundAtEntity(entity, "random.orb", 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            else
            {
                this.clientWorldController.playSoundAtEntity(entity, "random.pop", 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            this.gameController.effectRenderer.addEffect(new EntityPickupFX(this.clientWorldController, entity, entityLivingBase, 0.5F));
            this.clientWorldController.removeEntityFromWorld(packetIn.getCollectedItemEntityID());
        }
    }

    public void handleChat(S02PacketChat packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (packetIn.getType() == 2)
        {
            this.gameController.ingameGUI.setRecordPlaying(packetIn.getChatComponent(), false);
        }
        else
        {
            this.gameController.ingameGUI.getChatGUI().printChatMessage(packetIn.getChatComponent());
        }
    }

    public void handleAnimation(S0BPacketAnimation packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entity != null)
        {
            if (packetIn.getAnimationType() == 0)
            {
                EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
                entityLivingBase.swingItem();
            }
            else if (packetIn.getAnimationType() == 1)
            {
                entity.performHurtAnimation();
            }
            else if (packetIn.getAnimationType() == 2)
            {
                EntityPlayer entityPlayer = (EntityPlayer)entity;
                entityPlayer.wakeUpPlayer(false, false, false);
            }
            else if (packetIn.getAnimationType() == 4)
            {
                this.gameController.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT);
            }
            else if (packetIn.getAnimationType() == 5)
            {
                this.gameController.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT_MAGIC);
            }
        }
    }

    public void handleUseBed(S0APacketUseBed packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        packetIn.getPlayer(this.clientWorldController).trySleep(packetIn.getBedPosition());
    }

    public void handleSpawnMob(S0FPacketSpawnMob packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        double fifthDoubleValue = (double)packetIn.getX() / 32.0D;
        double eleventhDoubleValue = (double)packetIn.getY() / 32.0D;
        double fourteenthDoubleValue = (double)packetIn.getZ() / 32.0D;
        float f = (float)(packetIn.getYaw() * 360) / 256.0F;
        float thirdFloatValue = (float)(packetIn.getPitch() * 360) / 256.0F;
        EntityLivingBase entitylivingbase = (EntityLivingBase)EntityList.createEntityByID(packetIn.getEntityType(), this.gameController.theWorld);
        entitylivingbase.serverPosX = packetIn.getX();
        entitylivingbase.serverPosY = packetIn.getY();
        entitylivingbase.serverPosZ = packetIn.getZ();
        entitylivingbase.renderYawOffset = entitylivingbase.rotationYawHead = (float)(packetIn.getHeadPitch() * 360) / 256.0F;
        Entity[] aentity = entitylivingbase.getParts();

        if (aentity != null)
        {
            int i = packetIn.getEntityID() - entitylivingbase.getEntityId();

            for (int j = 0; j < aentity.length; ++j)
            {
                aentity[j].setEntityId(aentity[j].getEntityId() + i);
            }
        }

        entitylivingbase.setEntityId(packetIn.getEntityID());
        entitylivingbase.setPositionAndRotation(fifthDoubleValue, eleventhDoubleValue, fourteenthDoubleValue, f, thirdFloatValue);
        entitylivingbase.motionX = (double)((float)packetIn.getVelocityX() / 8000.0F);
        entitylivingbase.motionY = (double)((float)packetIn.getVelocityY() / 8000.0F);
        entitylivingbase.motionZ = (double)((float)packetIn.getVelocityZ() / 8000.0F);
        this.clientWorldController.addEntityToWorld(packetIn.getEntityID(), entitylivingbase);
        List<DataWatcher.WatchableObject> list = packetIn.getWatcherList();

        if (list != null)
        {
            entitylivingbase.getDataWatcher().updateWatchedObjectsFromList(list);
        }
    }

    public void handleTimeUpdate(S03PacketTimeUpdate packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.theWorld.setTotalWorldTime(packetIn.getTotalWorldTime());
        this.gameController.theWorld.setWorldTime(packetIn.getWorldTime());
    }

    public void handleSpawnPosition(S05PacketSpawnPosition packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.thePlayer.setSpawnPoint(packetIn.getSpawnPos(), true);
        this.gameController.theWorld.getWorldInfo().setSpawn(packetIn.getSpawnPos());
    }

    public void handleEntityAttach(S1BPacketEntityAttach packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());
        Entity entity1 = this.clientWorldController.getEntityByID(packetIn.getVehicleEntityId());

        if (packetIn.getLeash() == 0)
        {
            boolean flag = false;

            if (packetIn.getEntityId() == this.gameController.thePlayer.getEntityId())
            {
                entity = this.gameController.thePlayer;

                if (entity1 instanceof EntityBoat)
                {
                    ((EntityBoat)entity1).setIsBoatEmpty(false);
                }

                flag = entity.ridingEntity == null && entity1 != null;
            }
            else if (entity1 instanceof EntityBoat)
            {
                ((EntityBoat)entity1).setIsBoatEmpty(true);
            }

            if (entity == null)
            {
                return;
            }

            entity.mountEntity(entity1);

            if (flag)
            {
                GameSettings gamesettings = this.gameController.gameSettings;
                this.gameController.ingameGUI.setRecordPlaying(I18n.format("mount.onboard", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindSneak.getKeyCode())}), false);
            }
        }
        else if (packetIn.getLeash() == 1 && entity instanceof EntityLiving)
        {
            if (entity1 != null)
            {
                ((EntityLiving)entity).setLeashedToEntity(entity1, false);
            }
            else
            {
                ((EntityLiving)entity).clearLeashed(false, false);
            }
        }
    }

    public void handleEntityStatus(S19PacketEntityStatus packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
        {
            if (packetIn.getOpCode() == 21)
            {
                this.gameController.getSoundHandler().playSound(new GuardianSound((EntityGuardian)entity));
            }
            else
            {
                entity.handleStatusUpdate(packetIn.getOpCode());
            }
        }
    }

    public void handleUpdateHealth(S06PacketUpdateHealth packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.thePlayer.setPlayerSPHealth(packetIn.getHealth());
        this.gameController.thePlayer.getFoodStats().setFoodLevel(packetIn.getFoodLevel());
        this.gameController.thePlayer.getFoodStats().setFoodSaturationLevel(packetIn.getSaturationLevel());
    }

    public void handleSetExperience(S1FPacketSetExperience packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.thePlayer.setXPStats(packetIn.getExperienceBar(), packetIn.getTotalExperience(), packetIn.getLevel());
    }

    public void handleRespawn(S07PacketRespawn packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (packetIn.getDimensionID() != this.gameController.thePlayer.dimension)
        {
            this.doneLoadingTerrain = false;
            Scoreboard scoreboard = this.clientWorldController.getScoreboard();
            this.clientWorldController = new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, this.gameController.theWorld.getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), this.gameController.mcProfiler);
            this.clientWorldController.setWorldScoreboard(scoreboard);
            this.gameController.loadWorld(this.clientWorldController);
            this.gameController.thePlayer.dimension = packetIn.getDimensionID();
            this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
        }

        this.gameController.setDimensionAndSpawnPlayer(packetIn.getDimensionID());
        this.gameController.playerController.setGameType(packetIn.getGameType());
    }

    public void handleExplosion(S27PacketExplosion packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Explosion explosion = new Explosion(this.gameController.theWorld, (Entity)null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
        explosion.doExplosionB(true);
        this.gameController.thePlayer.motionX += (double)packetIn.getMotionX();
        this.gameController.thePlayer.motionY += (double)packetIn.getMotionY();
        this.gameController.thePlayer.motionZ += (double)packetIn.getMotionZ();
    }

    public void handleOpenWindow(S2DPacketOpenWindow packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayerSP entityPlayerSP = this.gameController.thePlayer;

        if ("minecraft:container".equals(packetIn.getGuiId()))
        {
            entityPlayerSP.displayGUIChest(new InventoryBasic(packetIn.getWindowTitle(), packetIn.getSlotCount()));
            entityPlayerSP.openContainer.windowId = packetIn.getWindowId();
        }
        else if ("minecraft:villager".equals(packetIn.getGuiId()))
        {
            entityPlayerSP.displayVillagerTradeGui(new NpcMerchant(entityPlayerSP, packetIn.getWindowTitle()));
            entityPlayerSP.openContainer.windowId = packetIn.getWindowId();
        }
        else if ("EntityHorse".equals(packetIn.getGuiId()))
        {
            Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());

            if (entity instanceof EntityHorse)
            {
                entityPlayerSP.displayGUIHorse((EntityHorse)entity, new AnimalChest(packetIn.getWindowTitle(), packetIn.getSlotCount()));
                entityPlayerSP.openContainer.windowId = packetIn.getWindowId();
            }
        }
        else if (!packetIn.hasSlots())
        {
            entityPlayerSP.displayGui(new LocalBlockIntercommunication(packetIn.getGuiId(), packetIn.getWindowTitle()));
            entityPlayerSP.openContainer.windowId = packetIn.getWindowId();
        }
        else
        {
            ContainerLocalMenu containerLocalMenu = new ContainerLocalMenu(packetIn.getGuiId(), packetIn.getWindowTitle(), packetIn.getSlotCount());
            entityPlayerSP.displayGUIChest(containerLocalMenu);
            entityPlayerSP.openContainer.windowId = packetIn.getWindowId();
        }
    }

    public void handleSetSlot(S2FPacketSetSlot packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayer entityplayer = this.gameController.thePlayer;

        if (packetIn.getWindowId() == -1)
        {
            entityplayer.inventory.setItemStack(packetIn.getItem());
        }
        else
        {
            boolean flag = false;

            if (this.gameController.currentScreen instanceof GuiContainerCreative)
            {
                GuiContainerCreative guicontainercreative = (GuiContainerCreative)this.gameController.currentScreen;
                flag = guicontainercreative.getSelectedTabIndex() != CreativeTabs.tabInventory.getTabIndex();
            }

            if (packetIn.getWindowId() == 0 && packetIn.getSlot() >= 36 && packetIn.getSlot() < 45)
            {
                ItemStack itemstack = entityplayer.inventoryContainer.getSlot(packetIn.getSlot()).getStack();

                if (packetIn.getItem() != null && (itemstack == null || itemstack.stackSize < packetIn.getItem().stackSize))
                {
                    packetIn.getItem().animationsToGo = 5;
                }

                entityplayer.inventoryContainer.putStackInSlot(packetIn.getSlot(), packetIn.getItem());
            }
            else if (packetIn.getWindowId() == entityplayer.openContainer.windowId && (packetIn.getWindowId() != 0 || !flag))
            {
                entityplayer.openContainer.putStackInSlot(packetIn.getSlot(), packetIn.getItem());
            }
        }
    }

    public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Container container = null;
        EntityPlayer entityplayer = this.gameController.thePlayer;

        if (packetIn.getWindowId() == 0)
        {
            container = entityplayer.inventoryContainer;
        }
        else if (packetIn.getWindowId() == entityplayer.openContainer.windowId)
        {
            container = entityplayer.openContainer;
        }

        if (container != null && !packetIn.wasAccepted())
        {
            this.addToSendQueue(new C0FPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
        }
    }

    public void handleWindowItems(S30PacketWindowItems packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayer entityplayer = this.gameController.thePlayer;

        if (packetIn.getWindowId() == 0)
        {
            entityplayer.inventoryContainer.putStacksInSlots(packetIn.getItemStacks());
        }
        else if (packetIn.getWindowId() == entityplayer.openContainer.windowId)
        {
            entityplayer.openContainer.putStacksInSlots(packetIn.getItemStacks());
        }
    }

    public void handleSignEditorOpen(S36PacketSignEditorOpen packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        TileEntity tileEntity = this.clientWorldController.getTileEntity(packetIn.getSignPosition());

        if (!(tileEntity instanceof TileEntitySign))
        {
            tileEntity = new TileEntitySign();
            tileEntity.setWorldObj(this.clientWorldController);
            tileEntity.setPos(packetIn.getSignPosition());
        }

        this.gameController.thePlayer.openEditSign((TileEntitySign)tileEntity);
    }

    public void handleUpdateSign(S33PacketUpdateSign packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        boolean flag = false;

        if (this.gameController.theWorld.isBlockLoaded(packetIn.getPos()))
        {
            TileEntity tileEntity = this.gameController.theWorld.getTileEntity(packetIn.getPos());

            if (tileEntity instanceof TileEntitySign)
            {
                TileEntitySign tileEntitySign = (TileEntitySign)tileEntity;

                if (tileEntitySign.getIsEditable())
                {
                    System.arraycopy(packetIn.getLines(), 0, tileEntitySign.signText, 0, 4);
                    tileEntitySign.markDirty();
                }

                flag = true;
            }
        }

        if (!flag && this.gameController.thePlayer != null)
        {
            this.gameController.thePlayer.addChatMessage(new ChatComponentText("Unable to locate sign at " + packetIn.getPos().getX() + ", " + packetIn.getPos().getY() + ", " + packetIn.getPos().getZ()));
        }
    }

    public void handleUpdateTileEntity(S35PacketUpdateTileEntity packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (this.gameController.theWorld.isBlockLoaded(packetIn.getPos()))
        {
            TileEntity tileEntity = this.gameController.theWorld.getTileEntity(packetIn.getPos());
            int i = packetIn.getTileEntityType();

            if (i == 1 && tileEntity instanceof TileEntityMobSpawner || i == 2 && tileEntity instanceof TileEntityCommandBlock || i == 3 && tileEntity instanceof TileEntityBeacon || i == 4 && tileEntity instanceof TileEntitySkull || i == 5 && tileEntity instanceof TileEntityFlowerPot || i == 6 && tileEntity instanceof TileEntityBanner)
            {
                tileEntity.readFromNBT(packetIn.getNbtCompound());
            }
        }
    }

    public void handleWindowProperty(S31PacketWindowProperty packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayer entityPlayer = this.gameController.thePlayer;

        if (entityPlayer.openContainer != null && entityPlayer.openContainer.windowId == packetIn.getWindowId())
        {
            entityPlayer.openContainer.updateProgressBar(packetIn.getVarIndex(), packetIn.getVarValue());
        }
    }

    public void handleEntityEquipment(S04PacketEntityEquipment packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityID());

        if (entity != null)
        {
            entity.setCurrentItemOrArmor(packetIn.getEquipmentSlot(), packetIn.getItemStack());
        }
    }

    public void handleCloseWindow(S2EPacketCloseWindow packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.thePlayer.closeScreenAndDropStack();
    }

    public void handleBlockAction(S24PacketBlockAction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.theWorld.addBlockEvent(packetIn.getBlockPosition(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());
    }

    public void handleBlockBreakAnim(S25PacketBlockBreakAnim packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.theWorld.sendBlockBreakProgress(packetIn.getBreakerId(), packetIn.getPosition(), packetIn.getProgress());
    }

    public void handleMapChunkBulk(S26PacketMapChunkBulk packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        for (int i = 0; i < packetIn.getChunkCount(); ++i)
        {
            int j = packetIn.getChunkX(i);
            int k = packetIn.getChunkZ(i);
            this.clientWorldController.doPreChunk(j, k, true);
            this.clientWorldController.invalidateBlockReceiveRegion(j << 4, 0, k << 4, (j << 4) + 15, 256, (k << 4) + 15);
            Chunk chunk = this.clientWorldController.getChunkFromChunkCoords(j, k);
            chunk.fillChunk(packetIn.getChunkBytes(i), packetIn.getChunkSize(i), true);
            this.clientWorldController.markBlockRangeForRenderUpdate(j << 4, 0, k << 4, (j << 4) + 15, 256, (k << 4) + 15);

            if (!(this.clientWorldController.provider instanceof WorldProviderSurface))
            {
                chunk.resetRelightChecks();
            }
        }
    }

    public void handleChangeGameState(S2BPacketChangeGameState packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayer entityplayer = this.gameController.thePlayer;
        int i = packetIn.getGameState();
        float f = packetIn.getValue();
        int j = MathHelper.floor_float(f + 0.5F);

        if (i >= 0 && i < S2BPacketChangeGameState.MESSAGE_NAMES.length && S2BPacketChangeGameState.MESSAGE_NAMES[i] != null)
        {
            entityplayer.addChatComponentMessage(new ChatComponentTranslation(S2BPacketChangeGameState.MESSAGE_NAMES[i], new Object[0]));
        }

        if (i == 1)
        {
            this.clientWorldController.getWorldInfo().setRaining(true);
            this.clientWorldController.setRainStrength(0.0F);
        }
        else if (i == 2)
        {
            this.clientWorldController.getWorldInfo().setRaining(false);
            this.clientWorldController.setRainStrength(1.0F);
        }
        else if (i == 3)
        {
            this.gameController.playerController.setGameType(WorldSettings.GameType.getByID(j));
        }
        else if (i == 4)
        {
            this.gameController.displayGuiScreen(new GuiWinGame());
        }
        else if (i == 5)
        {
            GameSettings gamesettings = this.gameController.gameSettings;

            if (f == 0.0F)
            {
                this.gameController.displayGuiScreen(new GuiScreenDemo());
            }
            else if (f == 101.0F)
            {
                this.gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.movement", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindForward.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindLeft.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindBack.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindRight.getKeyCode())}));
            }
            else if (f == 102.0F)
            {
                this.gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.jump", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindJump.getKeyCode())}));
            }
            else if (f == 103.0F)
            {
                this.gameController.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.inventory", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindInventory.getKeyCode())}));
            }
        }
        else if (i == 6)
        {
            this.clientWorldController.playSound(entityplayer.posX, entityplayer.posY + (double)entityplayer.getEyeHeight(), entityplayer.posZ, "random.successful_hit", 0.18F, 0.45F, false);
        }
        else if (i == 7)
        {
            this.clientWorldController.setRainStrength(f);
        }
        else if (i == 8)
        {
            this.clientWorldController.setThunderStrength(f);
        }
        else if (i == 10)
        {
            this.clientWorldController.spawnParticle(EnumParticleTypes.MOB_APPEARANCE, entityplayer.posX, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
            this.clientWorldController.playSound(entityplayer.posX, entityplayer.posY, entityplayer.posZ, "mob.guardian.curse", 1.0F, 1.0F, false);
        }
    }

    public void handleMaps(S34PacketMaps packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        MapData mapData = ItemMap.loadMapData(packetIn.getMapId(), this.gameController.theWorld);
        packetIn.setMapdataTo(mapData);
        this.gameController.entityRenderer.getMapItemRenderer().updateMapTexture(mapData);
    }

    public void handleEffect(S28PacketEffect packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (packetIn.isSoundServerwide())
        {
            this.gameController.theWorld.playBroadcastSound(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        }
        else
        {
            this.gameController.theWorld.playAuxSFX(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        }
    }

    public void handleStatistics(S37PacketStatistics packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        boolean flag = false;

        for (Entry<StatBase, Integer> entry : packetIn.getStatistics().entrySet())
        {
            StatBase statBase = (StatBase)entry.getKey();
            int i = ((Integer)entry.getValue()).intValue();

            if (statBase.isAchievement() && i > 0)
            {
                if (this.hasReceivedStatistics && this.gameController.thePlayer.getStatFileWriter().readStat(statBase) == 0)
                {
                    Achievement achievement = (Achievement)statBase;
                    this.gameController.guiAchievement.displayAchievement(achievement);

                    if (statBase == AchievementList.openInventory)
                    {
                        this.gameController.gameSettings.showInventoryAchievementHint = false;
                        this.gameController.gameSettings.saveOptions();
                    }
                }

                flag = true;
            }

            this.gameController.thePlayer.getStatFileWriter().unlockAchievement(this.gameController.thePlayer, statBase, i);
        }

        if (!this.hasReceivedStatistics && !flag && this.gameController.gameSettings.showInventoryAchievementHint)
        {
            this.gameController.guiAchievement.displayUnformattedAchievement(AchievementList.openInventory);
        }

        this.hasReceivedStatistics = true;

        if (this.gameController.currentScreen instanceof IProgressMeter)
        {
            ((IProgressMeter)this.gameController.currentScreen).doneLoading();
        }
    }

    public void handleEntityEffect(S1DPacketEntityEffect packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity instanceof EntityLivingBase)
        {
            PotionEffect potionEffect = new PotionEffect(packetIn.getEffectId(), packetIn.getDuration(), packetIn.getAmplifier(), false, packetIn.isShowParticles());
            potionEffect.setPotionDurationMax(packetIn.isMaxDuration());
            ((EntityLivingBase)entity).addPotionEffect(potionEffect);
        }
    }

    public void handleCombatEvent(S42PacketCombatEvent packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
    }

    public void handleServerDifficulty(S41PacketServerDifficulty packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.theWorld.getWorldInfo().setDifficulty(packetIn.getDifficulty());
        this.gameController.theWorld.getWorldInfo().setDifficultyLocked(packetIn.isDifficultyLocked());
    }

    public void handleCamera(S43PacketCamera packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
        {
            this.gameController.setRenderViewEntity(entity);
        }
    }

    public void handleWorldBorder(S44PacketWorldBorder packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        packetIn.applyToWorldBorder(this.clientWorldController.getWorldBorder());
    }

    @SuppressWarnings("incomplete-switch")
    public void handleTitle(S45PacketTitle packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        S45PacketTitle.Type s45packettitle$type = packetIn.getType();
        String s = null;
        String text2 = null;
        String text3 = packetIn.getMessage() != null ? packetIn.getMessage().getFormattedText() : "";

        switch (s45packettitle$type)
        {
            case TITLE:
                s = text3;
                break;

            case SUBTITLE:
                text2 = text3;
                break;

            case RESET:
                this.gameController.ingameGUI.displayTitle("", "", -1, -1, -1);
                this.gameController.ingameGUI.setDefaultTitlesTimes();
                return;
        }

        this.gameController.ingameGUI.displayTitle(s, text2, packetIn.getFadeInTime(), packetIn.getDisplayTime(), packetIn.getFadeOutTime());
    }

    public void handleSetCompressionLevel(S46PacketSetCompressionLevel packetIn)
    {
        if (!this.netManager.isLocalChannel())
        {
            this.netManager.setCompressionTreshold(packetIn.getThreshold());
        }
    }

    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn)
    {
        this.gameController.ingameGUI.getTabList().setHeader(packetIn.getHeader().getFormattedText().length() == 0 ? null : packetIn.getHeader());
        this.gameController.ingameGUI.getTabList().setFooter(packetIn.getFooter().getFormattedText().length() == 0 ? null : packetIn.getFooter());
    }

    public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity instanceof EntityLivingBase)
        {
            ((EntityLivingBase)entity).removePotionEffectClient(packetIn.getEffectId());
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void handlePlayerListItem(S38PacketPlayerListItem packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        for (S38PacketPlayerListItem.AddPlayerData s38packetplayerlistitem$addplayerdata : packetIn.getEntries())
        {
            if (packetIn.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER)
            {
                this.playerInfoMap.remove(s38packetplayerlistitem$addplayerdata.getProfile().getId());
            }
            else
            {
                NetworkPlayerInfo networkplayerinfo = this.playerInfoMap.get(s38packetplayerlistitem$addplayerdata.getProfile().getId());

                if (packetIn.getAction() == S38PacketPlayerListItem.Action.ADD_PLAYER)
                {
                    networkplayerinfo = new NetworkPlayerInfo(s38packetplayerlistitem$addplayerdata);
                    this.playerInfoMap.put(networkplayerinfo.getGameProfile().getId(), networkplayerinfo);
                }

                if (networkplayerinfo != null)
                {
                    switch (packetIn.getAction())
                    {
                        case ADD_PLAYER:
                            networkplayerinfo.setGameType(s38packetplayerlistitem$addplayerdata.getGameMode());
                            networkplayerinfo.setResponseTime(s38packetplayerlistitem$addplayerdata.getPing());
                            break;

                        case UPDATE_GAME_MODE:
                            networkplayerinfo.setGameType(s38packetplayerlistitem$addplayerdata.getGameMode());
                            break;

                        case UPDATE_LATENCY:
                            networkplayerinfo.setResponseTime(s38packetplayerlistitem$addplayerdata.getPing());
                            break;

                        case UPDATE_DISPLAY_NAME:
                            networkplayerinfo.setDisplayName(s38packetplayerlistitem$addplayerdata.getDisplayName());
                    }
                }
            }
        }
    }

    public void handleKeepAlive(S00PacketKeepAlive packetIn)
    {
        this.addToSendQueue(new C00PacketKeepAlive(packetIn.getId()));
    }

    public void handlePlayerAbilities(S39PacketPlayerAbilities packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        EntityPlayer entityPlayer = this.gameController.thePlayer;
        entityPlayer.capabilities.isFlying = packetIn.isFlying();
        entityPlayer.capabilities.isCreativeMode = packetIn.isCreativeMode();
        entityPlayer.capabilities.disableDamage = packetIn.isInvulnerable();
        entityPlayer.capabilities.allowFlying = packetIn.isAllowFlying();
        entityPlayer.capabilities.setFlySpeed(packetIn.getFlySpeed());
        entityPlayer.capabilities.setPlayerWalkSpeed(packetIn.getWalkSpeed());
    }

    public void handleTabComplete(S3APacketTabComplete packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        String[] astring = packetIn.getMatches();

        if (this.gameController.currentScreen instanceof GuiChat)
        {
            GuiChat guiChat = (GuiChat)this.gameController.currentScreen;
            guiChat.onAutocompleteResponse(astring);
        }
    }

    public void handleSoundEffect(S29PacketSoundEffect packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        this.gameController.theWorld.playSound(packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSoundName(), packetIn.getVolume(), packetIn.getPitch(), false);
    }

    public void handleResourcePack(S48PacketResourcePackSend packetIn)
    {
        final String s = packetIn.getURL();
        final String text2 = packetIn.getHash();

        if (s.startsWith("level://"))
        {
            String text3 = s.substring("level://".length());
            File file1 = new File(this.gameController.mcDataDir, "saves");
            File file2 = new File(file1, text3);

            if (file2.isFile())
            {
                this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.ACCEPTED));
                Futures.addCallback(this.gameController.getResourcePackRepository().setResourcePackInstance(file2), new FutureCallback<Object>()
                {
                    public void onSuccess(Object result)
                    {
                        NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    }
                    public void onFailure(Throwable throwable)
                    {
                        NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }
                }, MoreExecutors.directExecutor());
            }
            else
            {
                this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            }
        }
        else
        {
            if (this.gameController.getCurrentServerData() != null && this.gameController.getCurrentServerData().getResourceMode() == ServerData.ServerResourceMode.ENABLED)
            {
                this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.ACCEPTED));
                Futures.addCallback(this.gameController.getResourcePackRepository().downloadResourcePack(s, text2), new FutureCallback<Object>()
                {
                    public void onSuccess(Object result)
                    {
                        NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    }
                    public void onFailure(Throwable throwable)
                    {
                        NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }
                }, MoreExecutors.directExecutor());
            }
            else if (this.gameController.getCurrentServerData() != null && this.gameController.getCurrentServerData().getResourceMode() != ServerData.ServerResourceMode.PROMPT)
            {
                this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.DECLINED));
            }
            else
            {
                this.gameController.addScheduledTask(new Runnable()
                {
                    public void run()
                    {
                        NetHandlerPlayClient.this.gameController.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback()
                        {
                            public void confirmClicked(boolean result, int id)
                            {
                                NetHandlerPlayClient.this.gameController = Minecraft.getMinecraft();

                                if (result)
                                {
                                    if (NetHandlerPlayClient.this.gameController.getCurrentServerData() != null)
                                    {
                                        NetHandlerPlayClient.this.gameController.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.ENABLED);
                                    }

                                    NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.ACCEPTED));
                                    Futures.addCallback(NetHandlerPlayClient.this.gameController.getResourcePackRepository().downloadResourcePack(s, text2), new FutureCallback<Object>()
                                    {
                                        public void onSuccess(Object result)
                                        {
                                            NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                                        }
                                        public void onFailure(Throwable throwable)
                                        {
                                            NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                                        }
                                    }, MoreExecutors.directExecutor());
                                }
                                else
                                {
                                    if (NetHandlerPlayClient.this.gameController.getCurrentServerData() != null)
                                    {
                                        NetHandlerPlayClient.this.gameController.getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.DISABLED);
                                    }

                                    NetHandlerPlayClient.this.netManager.sendPacket(new C19PacketResourcePackStatus(text2, C19PacketResourcePackStatus.Action.DECLINED));
                                }

                                ServerList.saveSingleServer(NetHandlerPlayClient.this.gameController.getCurrentServerData());
                                NetHandlerPlayClient.this.gameController.displayGuiScreen((GuiScreen)null);
                            }
                        }, I18n.format("multiplayer.texturePrompt.line1", new Object[0]), I18n.format("multiplayer.texturePrompt.line2", new Object[0]), 0));
                    }
                });
            }
        }
    }

    public void handleEntityNBT(S49PacketUpdateEntityNBT packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
        {
            entity.clientUpdateEntityNBT(packetIn.getTagCompound());
        }
    }

    public void handleCustomPayload(S3FPacketCustomPayload packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if ("MC|TrList".equals(packetIn.getChannelName()))
        {
            PacketBuffer packetBuffer = packetIn.getBufferData();

            try
            {
                int i = packetBuffer.readInt();
                GuiScreen guiScreen = this.gameController.currentScreen;

                if (guiScreen != null && guiScreen instanceof GuiMerchant && i == this.gameController.thePlayer.openContainer.windowId)
                {
                    IMerchant imerchant = ((GuiMerchant)guiScreen).getMerchant();
                    MerchantRecipeList merchantRecipeList = MerchantRecipeList.readFromBuf(packetBuffer);
                    imerchant.setRecipes(merchantRecipeList);
                }
            }
            catch (IOException iOException)
            {
                logger.error((String)"Couldn\'t load trade info", (Throwable)iOException);
            }
        }
        else if ("MC|Brand".equals(packetIn.getChannelName()))
        {
            this.gameController.thePlayer.setClientBrand(packetIn.getBufferData().readStringFromBuffer(32767));
        }
        else if ("MC|BOpen".equals(packetIn.getChannelName()))
        {
            ItemStack itemStack = this.gameController.thePlayer.getCurrentEquippedItem();

            if (itemStack != null && itemStack.getItem() == Items.written_book)
            {
                this.gameController.displayGuiScreen(new GuiScreenBook(this.gameController.thePlayer, itemStack, false));
            }
        }
    }

    public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Scoreboard scoreboard = this.clientWorldController.getScoreboard();

        if (packetIn.getMode() == 0)
        {
            ScoreObjective scoreObjective = scoreboard.addScoreObjective(packetIn.getObjectiveName(), IScoreObjectiveCriteria.DUMMY);
            scoreObjective.setDisplayName(packetIn.getObjectiveValue());
            scoreObjective.setRenderType(packetIn.getType());
        }
        else
        {
            ScoreObjective scoreobjective1 = scoreboard.getObjective(packetIn.getObjectiveName());

            if (packetIn.getMode() == 1)
            {
                scoreboard.removeObjective(scoreobjective1);
            }
            else if (packetIn.getMode() == 2)
            {
                scoreobjective1.setDisplayName(packetIn.getObjectiveValue());
                scoreobjective1.setRenderType(packetIn.getType());
            }
        }
    }

    public void handleUpdateScore(S3CPacketUpdateScore packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Scoreboard scoreboard = this.clientWorldController.getScoreboard();
        ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.getObjectiveName());

        if (packetIn.getScoreAction() == S3CPacketUpdateScore.Action.CHANGE)
        {
            Score score = scoreboard.getValueFromObjective(packetIn.getPlayerName(), scoreobjective);
            score.setScorePoints(packetIn.getScoreValue());
        }
        else if (packetIn.getScoreAction() == S3CPacketUpdateScore.Action.REMOVE)
        {
            if (StringUtils.isNullOrEmpty(packetIn.getObjectiveName()))
            {
                scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), (ScoreObjective)null);
            }
            else if (scoreobjective != null)
            {
                scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), scoreobjective);
            }
        }
    }

    public void handleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Scoreboard scoreboard = this.clientWorldController.getScoreboard();

        if (packetIn.getScoreName().length() == 0)
        {
            scoreboard.setObjectiveInDisplaySlot(packetIn.getPosition(), (ScoreObjective)null);
        }
        else
        {
            ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.getScoreName());
            scoreboard.setObjectiveInDisplaySlot(packetIn.getPosition(), scoreobjective);
        }
    }

    public void handleTeams(S3EPacketTeams packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Scoreboard scoreboard = this.clientWorldController.getScoreboard();
        ScorePlayerTeam scoreplayerteam;

        if (packetIn.getAction() == 0)
        {
            scoreplayerteam = scoreboard.createTeam(packetIn.getName());
        }
        else
        {
            scoreplayerteam = scoreboard.getTeam(packetIn.getName());

            if (scoreplayerteam == null)
            {
                return;
            }
        }

        if (packetIn.getAction() == 0 || packetIn.getAction() == 2)
        {
            scoreplayerteam.setTeamName(packetIn.getDisplayName());
            scoreplayerteam.setNamePrefix(packetIn.getPrefix());
            scoreplayerteam.setNameSuffix(packetIn.getSuffix());
            scoreplayerteam.setChatFormat(EnumChatFormatting.getByColorIndex(packetIn.getColor()));
            scoreplayerteam.setFriendlyFlags(packetIn.getFriendlyFlags());
            Team.EnumVisible team$enumvisible = Team.EnumVisible.getByName(packetIn.getNameTagVisibility());

            if (team$enumvisible != null)
            {
                scoreplayerteam.setNameTagVisibility(team$enumvisible);
            }
        }

        if (packetIn.getAction() == 0 || packetIn.getAction() == 3)
        {
            for (String s : packetIn.getPlayers())
            {
                scoreboard.addPlayerToTeam(s, packetIn.getName());
            }
        }

        if (packetIn.getAction() == 4)
        {
            for (String stringValue : packetIn.getPlayers())
            {
                scoreboard.removePlayerFromTeam(stringValue, scoreplayerteam);
            }
        }

        if (packetIn.getAction() == 1)
        {
            scoreboard.removeTeam(scoreplayerteam);
        }
    }

    public void handleParticles(S2APacketParticles packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);

        if (packetIn.getParticleCount() == 0)
        {
            double fourthDoubleValue = (double)(packetIn.getParticleSpeed() * packetIn.getXOffset());
            double fifteenthDoubleValue = (double)(packetIn.getParticleSpeed() * packetIn.getYOffset());
            double sixteenthDoubleValue = (double)(packetIn.getParticleSpeed() * packetIn.getZOffset());

            try
            {
                this.clientWorldController.spawnParticle(packetIn.getParticleType(), packetIn.isLongDistance(), packetIn.getXCoordinate(), packetIn.getYCoordinate(), packetIn.getZCoordinate(), fourthDoubleValue, fifteenthDoubleValue, sixteenthDoubleValue, packetIn.getParticleArgs());
            }
            catch (Throwable caughtThrowable)
            {
                logger.warn("Could not spawn particle effect " + packetIn.getParticleType());
            }
        }
        else
        {
            for (int i = 0; i < packetIn.getParticleCount(); ++i)
            {
                double tenthDoubleValue = this.avRandomizer.nextGaussian() * (double)packetIn.getXOffset();
                double doubleValue5 = this.avRandomizer.nextGaussian() * (double)packetIn.getYOffset();
                double doubleValue6 = this.avRandomizer.nextGaussian() * (double)packetIn.getZOffset();
                double seventeenthDoubleValue = this.avRandomizer.nextGaussian() * (double)packetIn.getParticleSpeed();
                double eighteenthDoubleValue = this.avRandomizer.nextGaussian() * (double)packetIn.getParticleSpeed();
                double nineteenthDoubleValue = this.avRandomizer.nextGaussian() * (double)packetIn.getParticleSpeed();

                try
                {
                    this.clientWorldController.spawnParticle(packetIn.getParticleType(), packetIn.isLongDistance(), packetIn.getXCoordinate() + tenthDoubleValue, packetIn.getYCoordinate() + doubleValue5, packetIn.getZCoordinate() + doubleValue6, seventeenthDoubleValue, eighteenthDoubleValue, nineteenthDoubleValue, packetIn.getParticleArgs());
                }
                catch (Throwable caughtThrowable)
                {
                    logger.warn("Could not spawn particle effect " + packetIn.getParticleType());
                    return;
                }
            }
        }
    }

    public void handleEntityProperties(S20PacketEntityProperties packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.gameController);
        Entity entity = this.clientWorldController.getEntityByID(packetIn.getEntityId());

        if (entity != null)
        {
            if (!(entity instanceof EntityLivingBase))
            {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            }
            else
            {
                BaseAttributeMap baseAttributeMap = ((EntityLivingBase)entity).getAttributeMap();

                for (S20PacketEntityProperties.Snapshot s20packetentityproperties$snapshot : packetIn.getSnapshots())
                {
                    IAttributeInstance iattributeinstance = baseAttributeMap.getAttributeInstanceByName(s20packetentityproperties$snapshot.getAttributeName());

                    if (iattributeinstance == null)
                    {
                        iattributeinstance = baseAttributeMap.registerAttribute(new RangedAttribute((IAttribute)null, s20packetentityproperties$snapshot.getAttributeName(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
                    }

                    iattributeinstance.setBaseValue(s20packetentityproperties$snapshot.getBaseValue());
                    iattributeinstance.removeAllModifiers();

                    for (AttributeModifier attributeModifier : s20packetentityproperties$snapshot.getModifiers())
                    {
                        iattributeinstance.applyModifier(attributeModifier);
                    }
                }
            }
        }
    }

    public NetworkManager getNetworkManager()
    {
        return this.netManager;
    }

    public Collection<NetworkPlayerInfo> getPlayerInfoMap()
    {
        return this.playerInfoMap.values();
    }

    public NetworkPlayerInfo getPlayerInfo(UUID playerId)
    {
        return this.playerInfoMap.get(playerId);
    }

    public NetworkPlayerInfo getPlayerInfo(String playerName)
    {
        for (NetworkPlayerInfo networkPlayerInfo : this.playerInfoMap.values())
        {
            if (networkPlayerInfo.getGameProfile().getName().equals(playerName))
            {
                return networkPlayerInfo;
            }
        }

        return null;
    }

    public GameProfile getGameProfile()
    {
        return this.profile;
    }
}
