package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.Futures;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.material.Material;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable
{
    private static final Logger logger = LogManager.getLogger();
    public final NetworkManager netManager;
    private final MinecraftServer serverController;
    public EntityPlayerMP playerEntity;
    private int networkTickCount;
    private int lastPositionUpdate;
    private int floatingTickCount;
    private boolean receivedMovePacket;
    private int keepAliveKey;
    private long lastPingTime;
    private long lastSentPingPacket;
    private int chatSpamThresholdCount;
    private int itemDropThreshold;
    private IntHashMap<Short> pendingTransactions = new IntHashMap();
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private boolean hasMoved = true;

    public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn)
    {
        this.serverController = server;
        this.netManager = networkManagerIn;
        networkManagerIn.setNetHandler(this);
        this.playerEntity = playerIn;
        playerIn.playerNetServerHandler = this;
    }

    public void update()
    {
        this.receivedMovePacket = false;
        ++this.networkTickCount;
        this.serverController.theProfiler.startSection("keepAlive");

        if ((long)this.networkTickCount - this.lastSentPingPacket > 40L)
        {
            this.lastSentPingPacket = (long)this.networkTickCount;
            this.lastPingTime = this.currentTimeMillis();
            this.keepAliveKey = (int)this.lastPingTime;
            this.sendPacket(new S00PacketKeepAlive(this.keepAliveKey));
        }

        this.serverController.theProfiler.endSection();

        if (this.chatSpamThresholdCount > 0)
        {
            --this.chatSpamThresholdCount;
        }

        if (this.itemDropThreshold > 0)
        {
            --this.itemDropThreshold;
        }

        if (this.playerEntity.getLastActiveTime() > 0L && this.serverController.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - this.playerEntity.getLastActiveTime() > (long)(this.serverController.getMaxPlayerIdleMinutes() * 1000 * 60))
        {
            this.kickPlayerFromServer("You have been idle for too long!");
        }
    }

    public NetworkManager getNetworkManager()
    {
        return this.netManager;
    }

    public void kickPlayerFromServer(String reason)
    {
        final ChatComponentText chatComponentText = new ChatComponentText(reason);
        this.netManager.sendPacket(new S40PacketDisconnect(chatComponentText), new GenericFutureListener < Future <? super Void >> ()
        {
            public void operationComplete(Future <? super Void > future) throws Exception
            {
                NetHandlerPlayServer.this.netManager.closeChannel(chatComponentText);
            }
        }, new GenericFutureListener[0]);
        this.netManager.disableAutoRead();
        Futures.getUnchecked(this.serverController.addScheduledTask(new Runnable()
        {
            public void run()
            {
                NetHandlerPlayServer.this.netManager.checkDisconnected();
            }
        }));
    }

    public void processInput(C0CPacketInput packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.setEntityActionState(packetIn.getStrafeSpeed(), packetIn.getForwardSpeed(), packetIn.isJumping(), packetIn.isSneaking());
    }

    private boolean containsInvalidValues(C03PacketPlayer packetIn)
    {
        return !Doubles.isFinite(packetIn.getPositionX()) || !Doubles.isFinite(packetIn.getPositionY()) || !Doubles.isFinite(packetIn.getPositionZ()) || !Floats.isFinite(packetIn.getPitch()) || !Floats.isFinite(packetIn.getYaw());
    }

    public void processPlayer(C03PacketPlayer packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.containsInvalidValues(packetIn))
        {
            this.kickPlayerFromServer("Invalid move packet received");
        }
        else
        {
            WorldServer worldServer = this.serverController.worldServerForDimension(this.playerEntity.dimension);
            this.receivedMovePacket = true;

            if (!this.playerEntity.playerConqueredTheEnd)
            {
                double xCoordinate = this.playerEntity.posX;
                double yCoordinate = this.playerEntity.posY;
                double zCoordinate = this.playerEntity.posZ;
                double doubleValue = 0.0D;
                double doubleValue2 = packetIn.getPositionX() - this.lastPosX;
                double doubleValue3 = packetIn.getPositionY() - this.lastPosY;
                double doubleValue4 = packetIn.getPositionZ() - this.lastPosZ;

                if (packetIn.isMoving())
                {
                    doubleValue = doubleValue2 * doubleValue2 + doubleValue3 * doubleValue3 + doubleValue4 * doubleValue4;

                    if (!this.hasMoved && doubleValue < 0.25D)
                    {
                        this.hasMoved = true;
                    }
                }

                if (this.hasMoved)
                {
                    this.lastPositionUpdate = this.networkTickCount;

                    if (this.playerEntity.ridingEntity != null)
                    {
                        float floatValue = this.playerEntity.rotationYaw;
                        float f = this.playerEntity.rotationPitch;
                        this.playerEntity.ridingEntity.updateRiderPosition();
                        double xCoordinate2 = this.playerEntity.posX;
                        double yCoordinate2 = this.playerEntity.posY;
                        double zCoordinate2 = this.playerEntity.posZ;

                        if (packetIn.getRotating())
                        {
                            floatValue = packetIn.getYaw();
                            f = packetIn.getPitch();
                        }

                        this.playerEntity.onGround = packetIn.isOnGround();
                        this.playerEntity.onUpdateEntity();
                        this.playerEntity.setPositionAndRotation(xCoordinate2, yCoordinate2, zCoordinate2, floatValue, f);

                        if (this.playerEntity.ridingEntity != null)
                        {
                            this.playerEntity.ridingEntity.updateRiderPosition();
                        }

                        this.serverController.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);

                        if (this.playerEntity.ridingEntity != null)
                        {
                            if (doubleValue > 4.0D)
                            {
                                Entity entity = this.playerEntity.ridingEntity;
                                this.playerEntity.playerNetServerHandler.sendPacket(new S18PacketEntityTeleport(entity));
                                this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                            }

                            this.playerEntity.ridingEntity.isAirBorne = true;
                        }

                        if (this.hasMoved)
                        {
                            this.lastPosX = this.playerEntity.posX;
                            this.lastPosY = this.playerEntity.posY;
                            this.lastPosZ = this.playerEntity.posZ;
                        }

                        worldServer.updateEntity(this.playerEntity);
                        return;
                    }

                    if (this.playerEntity.isPlayerSleeping())
                    {
                        this.playerEntity.onUpdateEntity();
                        this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        worldServer.updateEntity(this.playerEntity);
                        return;
                    }

                    double fourthDoubleValue = this.playerEntity.posY;
                    this.lastPosX = this.playerEntity.posX;
                    this.lastPosY = this.playerEntity.posY;
                    this.lastPosZ = this.playerEntity.posZ;
                    double xCoordinate3 = this.playerEntity.posX;
                    double yCoordinate4 = this.playerEntity.posY;
                    double zCoordinate3 = this.playerEntity.posZ;
                    float floatValue3 = this.playerEntity.rotationYaw;
                    float floatValue4 = this.playerEntity.rotationPitch;

                    if (packetIn.isMoving() && packetIn.getPositionY() == -999.0D)
                    {
                        packetIn.setMoving(false);
                    }

                    if (packetIn.isMoving())
                    {
                        xCoordinate3 = packetIn.getPositionX();
                        yCoordinate4 = packetIn.getPositionY();
                        zCoordinate3 = packetIn.getPositionZ();

                        if (Math.abs(packetIn.getPositionX()) > 3.0E7D || Math.abs(packetIn.getPositionZ()) > 3.0E7D)
                        {
                            this.kickPlayerFromServer("Illegal position");
                            return;
                        }
                    }

                    if (packetIn.getRotating())
                    {
                        floatValue3 = packetIn.getYaw();
                        floatValue4 = packetIn.getPitch();
                    }

                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, floatValue3, floatValue4);

                    if (!this.hasMoved)
                    {
                        return;
                    }

                    double thirdDoubleValue = xCoordinate3 - this.playerEntity.posX;
                    double yCoordinate5 = yCoordinate4 - this.playerEntity.posY;
                    double zCoordinate4 = zCoordinate3 - this.playerEntity.posZ;
                    double doubleValue5 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                    double doubleValue6 = thirdDoubleValue * thirdDoubleValue + yCoordinate5 * yCoordinate5 + zCoordinate4 * zCoordinate4;

                    if (doubleValue6 - doubleValue5 > 100.0D && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(this.playerEntity.getName())))
                    {
                        logger.warn(this.playerEntity.getName() + " moved too quickly! " + thirdDoubleValue + "," + yCoordinate5 + "," + zCoordinate4 + " (" + thirdDoubleValue + ", " + yCoordinate5 + ", " + zCoordinate4 + ")");
                        this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        return;
                    }

                    float secondFloatValue = 0.0625F;
                    boolean flag = worldServer.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract((double)secondFloatValue, (double)secondFloatValue, (double)secondFloatValue)).isEmpty();

                    if (this.playerEntity.onGround && !packetIn.isOnGround() && yCoordinate5 > 0.0D)
                    {
                        this.playerEntity.jump();
                    }

                    this.playerEntity.moveEntity(thirdDoubleValue, yCoordinate5, zCoordinate4);
                    this.playerEntity.onGround = packetIn.isOnGround();
                    thirdDoubleValue = xCoordinate3 - this.playerEntity.posX;
                    yCoordinate5 = yCoordinate4 - this.playerEntity.posY;

                    if (yCoordinate5 > -0.5D || yCoordinate5 < 0.5D)
                    {
                        yCoordinate5 = 0.0D;
                    }

                    zCoordinate4 = zCoordinate3 - this.playerEntity.posZ;
                    doubleValue6 = thirdDoubleValue * thirdDoubleValue + yCoordinate5 * yCoordinate5 + zCoordinate4 * zCoordinate4;
                    boolean flag1 = false;

                    if (doubleValue6 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.theItemInWorldManager.isCreative())
                    {
                        flag1 = true;
                        logger.warn(this.playerEntity.getName() + " moved wrongly!");
                    }

                    this.playerEntity.setPositionAndRotation(xCoordinate3, yCoordinate4, zCoordinate3, floatValue3, floatValue4);
                    this.playerEntity.addMovementStat(this.playerEntity.posX - xCoordinate, this.playerEntity.posY - yCoordinate, this.playerEntity.posZ - zCoordinate);

                    if (!this.playerEntity.noClip)
                    {
                        boolean flag2 = worldServer.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract((double)secondFloatValue, (double)secondFloatValue, (double)secondFloatValue)).isEmpty();

                        if (flag && (flag1 || !flag2) && !this.playerEntity.isPlayerSleeping())
                        {
                            this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, floatValue3, floatValue4);
                            return;
                        }
                    }

                    AxisAlignedBB axisalignedbb = this.playerEntity.getEntityBoundingBox().expand((double)secondFloatValue, (double)secondFloatValue, (double)secondFloatValue).addCoord(0.0D, -0.55D, 0.0D);

                    if (!this.serverController.isFlightAllowed() && !this.playerEntity.capabilities.allowFlying && !worldServer.checkBlockCollision(axisalignedbb))
                    {
                        if (yCoordinate5 >= -0.03125D)
                        {
                            ++this.floatingTickCount;

                            if (this.floatingTickCount > 80)
                            {
                                logger.warn(this.playerEntity.getName() + " was kicked for floating too long!");
                                this.kickPlayerFromServer("Flying is not enabled on this server");
                                return;
                            }
                        }
                    }
                    else
                    {
                        this.floatingTickCount = 0;
                    }

                    this.playerEntity.onGround = packetIn.isOnGround();
                    this.serverController.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);
                    this.playerEntity.handleFalling(this.playerEntity.posY - fourthDoubleValue, packetIn.isOnGround());
                }
                else if (this.networkTickCount - this.lastPositionUpdate > 20)
                {
                    this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                }
            }
        }
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch)
    {
        this.setPlayerLocation(x, y, z, yaw, pitch, Collections.<S08PacketPlayerPosLook.EnumFlags>emptySet());
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<S08PacketPlayerPosLook.EnumFlags> relativeSet)
    {
        this.hasMoved = false;
        this.lastPosX = x;
        this.lastPosY = y;
        this.lastPosZ = z;

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.X))
        {
            this.lastPosX += this.playerEntity.posX;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Y))
        {
            this.lastPosY += this.playerEntity.posY;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Z))
        {
            this.lastPosZ += this.playerEntity.posZ;
        }

        float f = yaw;
        float floatValue2 = pitch;

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT))
        {
            f = yaw + this.playerEntity.rotationYaw;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.X_ROT))
        {
            floatValue2 = pitch + this.playerEntity.rotationPitch;
        }

        this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, f, floatValue2);
        this.playerEntity.playerNetServerHandler.sendPacket(new S08PacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet));
    }

    public void processPlayerDigging(C07PacketPlayerDigging packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer worldServer = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        BlockPos blockPos = packetIn.getPosition();
        this.playerEntity.markPlayerActive();

        switch (packetIn.getStatus())
        {
            case DROP_ITEM:
                if (!this.playerEntity.isSpectator())
                {
                    this.playerEntity.dropOneItem(false);
                }

                return;

            case DROP_ALL_ITEMS:
                if (!this.playerEntity.isSpectator())
                {
                    this.playerEntity.dropOneItem(true);
                }

                return;

            case RELEASE_USE_ITEM:
                this.playerEntity.stopUsingItem();
                return;

            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                double secondDoubleValue = this.playerEntity.posX - ((double)blockPos.getX() + 0.5D);
                double yCoordinate = this.playerEntity.posY - ((double)blockPos.getY() + 0.5D) + 1.5D;
                double zCoordinate = this.playerEntity.posZ - ((double)blockPos.getZ() + 0.5D);
                double doubleValue = secondDoubleValue * secondDoubleValue + yCoordinate * yCoordinate + zCoordinate * zCoordinate;

                if (doubleValue > 36.0D)
                {
                    return;
                }
                else if (blockPos.getY() >= this.serverController.getBuildLimit())
                {
                    return;
                }
                else
                {
                    if (packetIn.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK)
                    {
                        if (!this.serverController.isBlockProtected(worldServer, blockPos, this.playerEntity) && worldServer.getWorldBorder().contains(blockPos))
                        {
                            this.playerEntity.theItemInWorldManager.onBlockClicked(blockPos, packetIn.getFacing());
                        }
                        else
                        {
                            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldServer, blockPos));
                        }
                    }
                    else
                    {
                        if (packetIn.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)
                        {
                            this.playerEntity.theItemInWorldManager.blockRemoving(blockPos);
                        }
                        else if (packetIn.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                        {
                            this.playerEntity.theItemInWorldManager.cancelDestroyingBlock();
                        }

                        if (worldServer.getBlockState(blockPos).getBlock().getMaterial() != Material.air)
                        {
                            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldServer, blockPos));
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    public void processPlayerBlockPlacement(C08PacketPlayerBlockPlacement packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer worldServer = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        ItemStack itemStack = this.playerEntity.inventory.getCurrentItem();
        boolean flag = false;
        BlockPos blockPos = packetIn.getPosition();
        EnumFacing enumfacing = EnumFacing.getFront(packetIn.getPlacedBlockDirection());
        this.playerEntity.markPlayerActive();

        if (packetIn.getPlacedBlockDirection() == 255)
        {
            if (itemStack == null)
            {
                return;
            }

            this.playerEntity.theItemInWorldManager.tryUseItem(this.playerEntity, worldServer, itemStack);
        }
        else if (blockPos.getY() < this.serverController.getBuildLimit() - 1 || enumfacing != EnumFacing.UP && blockPos.getY() < this.serverController.getBuildLimit())
        {
            if (this.hasMoved && this.playerEntity.getDistanceSq((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) < 64.0D && !this.serverController.isBlockProtected(worldServer, blockPos, this.playerEntity) && worldServer.getWorldBorder().contains(blockPos))
            {
                this.playerEntity.theItemInWorldManager.activateBlockOrUseItem(this.playerEntity, worldServer, itemStack, blockPos, enumfacing, packetIn.getPlacedBlockOffsetX(), packetIn.getPlacedBlockOffsetY(), packetIn.getPlacedBlockOffsetZ());
            }

            flag = true;
        }
        else
        {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("build.tooHigh", new Object[] {Integer.valueOf(this.serverController.getBuildLimit())});
            chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            this.playerEntity.playerNetServerHandler.sendPacket(new S02PacketChat(chatComponentTranslation));
            flag = true;
        }

        if (flag)
        {
            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldServer, blockPos));
            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(worldServer, blockPos.offset(enumfacing)));
        }

        itemStack = this.playerEntity.inventory.getCurrentItem();

        if (itemStack != null && itemStack.stackSize == 0)
        {
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = null;
            itemStack = null;
        }

        if (itemStack == null || itemStack.getMaxItemUseDuration() == 0)
        {
            this.playerEntity.isChangingQuantityOnly = true;
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = ItemStack.copyItemStack(this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem]);
            Slot slot = this.playerEntity.openContainer.getSlotFromInventory(this.playerEntity.inventory, this.playerEntity.inventory.currentItem);
            this.playerEntity.openContainer.detectAndSendChanges();
            this.playerEntity.isChangingQuantityOnly = false;

            if (!ItemStack.areItemStacksEqual(this.playerEntity.inventory.getCurrentItem(), packetIn.getStack()))
            {
                this.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, slot.slotNumber, this.playerEntity.inventory.getCurrentItem()));
            }
        }
    }

    public void handleSpectate(C18PacketSpectate packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.playerEntity.isSpectator())
        {
            Entity entity = null;

            for (WorldServer worldServer : this.serverController.worldServers)
            {
                if (worldServer != null)
                {
                    entity = packetIn.getEntity(worldServer);

                    if (entity != null)
                    {
                        break;
                    }
                }
            }

            if (entity != null)
            {
                this.playerEntity.setSpectatingEntity(this.playerEntity);
                this.playerEntity.mountEntity((Entity)null);

                if (entity.worldObj != this.playerEntity.worldObj)
                {
                    WorldServer worldserver1 = this.playerEntity.getServerForPlayer();
                    WorldServer worldserver2 = (WorldServer)entity.worldObj;
                    this.playerEntity.dimension = entity.dimension;
                    this.sendPacket(new S07PacketRespawn(this.playerEntity.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), this.playerEntity.theItemInWorldManager.getGameType()));
                    worldserver1.removePlayerEntityDangerously(this.playerEntity);
                    this.playerEntity.isDead = false;
                    this.playerEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                    if (this.playerEntity.isEntityAlive())
                    {
                        worldserver1.updateEntityWithOptionalForce(this.playerEntity, false);
                        worldserver2.spawnEntityInWorld(this.playerEntity);
                        worldserver2.updateEntityWithOptionalForce(this.playerEntity, false);
                    }

                    this.playerEntity.setWorld(worldserver2);
                    this.serverController.getConfigurationManager().preparePlayer(this.playerEntity, worldserver1);
                    this.playerEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                    this.playerEntity.theItemInWorldManager.setWorld(worldserver2);
                    this.serverController.getConfigurationManager().updateTimeAndWeatherForPlayer(this.playerEntity, worldserver2);
                    this.serverController.getConfigurationManager().syncPlayerInventory(this.playerEntity);
                }
                else
                {
                    this.playerEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                }
            }
        }
    }

    public void handleResourcePackStatus(C19PacketResourcePackStatus packetIn)
    {
    }

    public void onDisconnect(IChatComponent reason)
    {
        logger.info(this.playerEntity.getName() + " lost connection: " + reason);
        this.serverController.refreshStatusNextTick();
        ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("multiplayer.player.left", new Object[] {this.playerEntity.getDisplayName()});
        chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        this.serverController.getConfigurationManager().sendChatMsg(chatComponentTranslation);
        this.playerEntity.mountEntityAndWakeUp();
        this.serverController.getConfigurationManager().playerLoggedOut(this.playerEntity);

        if (this.serverController.isSinglePlayer() && this.playerEntity.getName().equals(this.serverController.getServerOwner()))
        {
            logger.info("Stopping singleplayer server as player logged out");
            this.serverController.initiateShutdown();
        }
    }

    public void sendPacket(final Packet packetIn)
    {
        if (packetIn instanceof S02PacketChat)
        {
            S02PacketChat s02packetchat = (S02PacketChat)packetIn;
            EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility = this.playerEntity.getChatVisibility();

            if (entityplayer$enumchatvisibility == EntityPlayer.EnumChatVisibility.HIDDEN)
            {
                return;
            }

            if (entityplayer$enumchatvisibility == EntityPlayer.EnumChatVisibility.SYSTEM && !s02packetchat.isChat())
            {
                return;
            }
        }

        try
        {
            this.netManager.sendPacket(packetIn);
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Sending packet");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Packet being sent");
            crashReportCategory.addCrashSectionCallable("Packet class", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return packetIn.getClass().getCanonicalName();
                }
            });
            throw new ReportedException(crashReport);
        }
    }

    public void processHeldItemChange(C09PacketHeldItemChange packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < InventoryPlayer.getHotbarSize())
        {
            this.playerEntity.inventory.currentItem = packetIn.getSlotId();
            this.playerEntity.markPlayerActive();
        }
        else
        {
            logger.warn(this.playerEntity.getName() + " tried to set an invalid carried item");
        }
    }

    public void processChatMessage(C01PacketChatMessage packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("chat.cannotSend", new Object[0]);
            chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            this.sendPacket(new S02PacketChat(chatComponentTranslation));
        }
        else
        {
            this.playerEntity.markPlayerActive();
            String s = packetIn.getMessage();
            s = StringUtils.normalizeSpace(s);

            for (int i = 0; i < s.length(); ++i)
            {
                if (!ChatAllowedCharacters.isAllowedCharacter(s.charAt(i)))
                {
                    this.kickPlayerFromServer("Illegal characters in chat");
                    return;
                }
            }

            if (s.startsWith("/"))
            {
                this.handleSlashCommand(s);
            }
            else
            {
                IChatComponent ichatcomponent = new ChatComponentTranslation("chat.type.text", new Object[] {this.playerEntity.getDisplayName(), s});
                this.serverController.getConfigurationManager().sendChatMsgImpl(ichatcomponent, false);
            }

            this.chatSpamThresholdCount += 20;

            if (this.chatSpamThresholdCount > 200 && !this.serverController.getConfigurationManager().canSendCommands(this.playerEntity.getGameProfile()))
            {
                this.kickPlayerFromServer("disconnect.spam");
            }
        }
    }

    private void handleSlashCommand(String command)
    {
        this.serverController.getCommandManager().executeCommand(this.playerEntity, command);
    }

    public void handleAnimation(C0APacketAnimation packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();
        this.playerEntity.swingItem();
    }

    public void processEntityAction(C0BPacketEntityAction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();

        switch (packetIn.getAction())
        {
            case START_SNEAKING:
                this.playerEntity.setSneaking(true);
                break;

            case STOP_SNEAKING:
                this.playerEntity.setSneaking(false);
                break;

            case START_SPRINTING:
                this.playerEntity.setSprinting(true);
                break;

            case STOP_SPRINTING:
                this.playerEntity.setSprinting(false);
                break;

            case STOP_SLEEPING:
                this.playerEntity.wakeUpPlayer(false, true, true);
                this.hasMoved = false;
                break;

            case RIDING_JUMP:
                if (this.playerEntity.ridingEntity instanceof EntityHorse)
                {
                    ((EntityHorse)this.playerEntity.ridingEntity).setJumpPower(packetIn.getAuxData());
                }

                break;

            case OPEN_INVENTORY:
                if (this.playerEntity.ridingEntity instanceof EntityHorse)
                {
                    ((EntityHorse)this.playerEntity.ridingEntity).openGUI(this.playerEntity);
                }

                break;

            default:
                throw new IllegalArgumentException("Invalid client command!");
        }
    }

    public void processUseEntity(C02PacketUseEntity packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer worldServer = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        Entity entity = packetIn.getEntityFromWorld(worldServer);
        this.playerEntity.markPlayerActive();

        if (entity != null)
        {
            boolean flag = this.playerEntity.canEntityBeSeen(entity);
            double doubleValue = 36.0D;

            if (!flag)
            {
                doubleValue = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(entity) < doubleValue)
            {
                if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT)
                {
                    this.playerEntity.interactWith(entity);
                }
                else if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT_AT)
                {
                    entity.interactAt(this.playerEntity, packetIn.getHitVec());
                }
                else if (packetIn.getAction() == C02PacketUseEntity.Action.ATTACK)
                {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.playerEntity)
                    {
                        this.kickPlayerFromServer("Attempting to attack an invalid entity");
                        this.serverController.logWarning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    public void processClientStatus(C16PacketClientStatus packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();
        C16PacketClientStatus.EnumState c16packetclientstatus$enumstate = packetIn.getStatus();

        switch (c16packetclientstatus$enumstate)
        {
            case PERFORM_RESPAWN:
                if (this.playerEntity.playerConqueredTheEnd)
                {
                    this.playerEntity = this.serverController.getConfigurationManager().recreatePlayerEntity(this.playerEntity, 0, true);
                }
                else if (this.playerEntity.getServerForPlayer().getWorldInfo().isHardcoreModeEnabled())
                {
                    if (this.serverController.isSinglePlayer() && this.playerEntity.getName().equals(this.serverController.getServerOwner()))
                    {
                        this.playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                        this.serverController.deleteWorldAndStopServer();
                    }
                    else
                    {
                        UserListBansEntry userListBansEntry = new UserListBansEntry(this.playerEntity.getGameProfile(), (Date)null, "(You just lost the game)", (Date)null, "Death in Hardcore");
                        this.serverController.getConfigurationManager().getBannedPlayers().addEntry(userListBansEntry);
                        this.playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                    }
                }
                else
                {
                    if (this.playerEntity.getHealth() > 0.0F)
                    {
                        return;
                    }

                    this.playerEntity = this.serverController.getConfigurationManager().recreatePlayerEntity(this.playerEntity, 0, false);
                }

                break;

            case REQUEST_STATS:
                this.playerEntity.getStatFile().sendStats(this.playerEntity);
                break;

            case OPEN_INVENTORY_ACHIEVEMENT:
                this.playerEntity.triggerAchievement(AchievementList.openInventory);
        }
    }

    public void processCloseWindow(C0DPacketCloseWindow packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.closeContainer();
    }

    public void processClickWindow(C0EPacketClickWindow packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();

        if (this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity))
        {
            if (this.playerEntity.isSpectator())
            {
                List<ItemStack> list = Lists.<ItemStack>newArrayListWithCapacity(this.playerEntity.openContainer.inventorySlots.size());

                for (int i = 0; i < this.playerEntity.openContainer.inventorySlots.size(); ++i)
                {
                    list.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(i)).getStack());
                }

                this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, list);
            }
            else
            {
                ItemStack itemstack = this.playerEntity.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getMode(), this.playerEntity);

                if (ItemStack.areItemStacksEqual(packetIn.getClickedItem(), itemstack))
                {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
                    this.playerEntity.isChangingQuantityOnly = true;
                    this.playerEntity.openContainer.detectAndSendChanges();
                    this.playerEntity.updateHeldItem();
                    this.playerEntity.isChangingQuantityOnly = false;
                }
                else
                {
                    this.pendingTransactions.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(packetIn.getActionNumber()));
                    this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
                    this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
                    List<ItemStack> list1 = Lists.<ItemStack>newArrayListWithCapacity(this.playerEntity.openContainer.inventorySlots.size());

                    for (int j = 0; j < this.playerEntity.openContainer.inventorySlots.size(); ++j)
                    {
                        list1.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(j)).getStack());
                    }

                    this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, list1);
                }
            }
        }
    }

    public void processEnchantItem(C11PacketEnchantItem packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();

        if (this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator())
        {
            this.playerEntity.openContainer.enchantItem(this.playerEntity, packetIn.getButton());
            this.playerEntity.openContainer.detectAndSendChanges();
        }
    }

    public void processCreativeInventoryAction(C10PacketCreativeInventoryAction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.playerEntity.theItemInWorldManager.isCreative())
        {
            boolean flag = packetIn.getSlotId() < 0;
            ItemStack itemstack = packetIn.getStack();

            if (itemstack != null && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10))
            {
                NBTTagCompound nBTTagCompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");

                if (nBTTagCompound.hasKey("x") && nBTTagCompound.hasKey("y") && nBTTagCompound.hasKey("z"))
                {
                    BlockPos blockPos = new BlockPos(nBTTagCompound.getInteger("x"), nBTTagCompound.getInteger("y"), nBTTagCompound.getInteger("z"));
                    TileEntity tileEntity = this.playerEntity.worldObj.getTileEntity(blockPos);

                    if (tileEntity != null)
                    {
                        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                        tileEntity.writeToNBT(nbttagcompound1);
                        nbttagcompound1.removeTag("x");
                        nbttagcompound1.removeTag("y");
                        nbttagcompound1.removeTag("z");
                        itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
                    }
                }
            }

            boolean flag1 = packetIn.getSlotId() >= 1 && packetIn.getSlotId() < 36 + InventoryPlayer.getHotbarSize();
            boolean flag2 = itemstack == null || itemstack.getItem() != null;
            boolean flag3 = itemstack == null || itemstack.getMetadata() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;

            if (flag1 && flag2 && flag3)
            {
                if (itemstack == null)
                {
                    this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), (ItemStack)null);
                }
                else
                {
                    this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
                }

                this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
            }
            else if (flag && flag2 && flag3 && this.itemDropThreshold < 200)
            {
                this.itemDropThreshold += 20;
                EntityItem entityItem = this.playerEntity.dropPlayerItemWithRandomChoice(itemstack, true);

                if (entityItem != null)
                {
                    entityItem.setAgeToCreativeDespawnTime();
                }
            }
        }
    }

    public void processConfirmTransaction(C0FPacketConfirmTransaction packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        Short oshort = (Short)this.pendingTransactions.lookup(this.playerEntity.openContainer.windowId);

        if (oshort != null && packetIn.getUid() == oshort.shortValue() && this.playerEntity.openContainer.windowId == packetIn.getWindowId() && !this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator())
        {
            this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
        }
    }

    public void processUpdateSign(C12PacketUpdateSign packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();
        WorldServer worldServer = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        BlockPos blockPos = packetIn.getPosition();

        if (worldServer.isBlockLoaded(blockPos))
        {
            TileEntity tileEntity = worldServer.getTileEntity(blockPos);

            if (!(tileEntity instanceof TileEntitySign))
            {
                return;
            }

            TileEntitySign tileentitysign = (TileEntitySign)tileEntity;

            if (!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != this.playerEntity)
            {
                this.serverController.logWarning("Player " + this.playerEntity.getName() + " just tried to change non-editable sign");
                return;
            }

            IChatComponent[] aichatcomponent = packetIn.getLines();

            for (int i = 0; i < aichatcomponent.length; ++i)
            {
                tileentitysign.signText[i] = new ChatComponentText(EnumChatFormatting.getTextWithoutFormattingCodes(aichatcomponent[i].getUnformattedText()));
            }

            tileentitysign.markDirty();
            worldServer.markBlockForUpdate(blockPos);
        }
    }

    public void processKeepAlive(C00PacketKeepAlive packetIn)
    {
        if (packetIn.getKey() == this.keepAliveKey)
        {
            int i = (int)(this.currentTimeMillis() - this.lastPingTime);
            this.playerEntity.ping = (this.playerEntity.ping * 3 + i) / 4;
        }
    }

    private long currentTimeMillis()
    {
        return System.nanoTime() / 1000000L;
    }

    public void processPlayerAbilities(C13PacketPlayerAbilities packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.capabilities.isFlying = packetIn.isFlying() && this.playerEntity.capabilities.allowFlying;
    }

    public void processTabComplete(C14PacketTabComplete packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        List<String> list = Lists.<String>newArrayList();

        for (String s : this.serverController.getTabCompletions(this.playerEntity, packetIn.getMessage(), packetIn.getTargetBlock()))
        {
            list.add(s);
        }

        this.playerEntity.playerNetServerHandler.sendPacket(new S3APacketTabComplete((String[])list.toArray(new String[list.size()])));
    }

    public void processClientSettings(C15PacketClientSettings packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.handleClientSettings(packetIn);
    }

    public void processVanilla250Packet(C17PacketCustomPayload packetIn)
    {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if ("MC|BEdit".equals(packetIn.getChannelName()))
        {
            PacketBuffer packetbuffer3 = new PacketBuffer(Unpooled.wrappedBuffer((ByteBuf)packetIn.getBufferData()));

            try
            {
                ItemStack itemstack1 = packetbuffer3.readItemStackFromBuffer();

                if (itemstack1 != null)
                {
                    if (!ItemWritableBook.isNBTValid(itemstack1.getTagCompound()))
                    {
                        throw new IOException("Invalid book tag!");
                    }

                    ItemStack itemstack3 = this.playerEntity.inventory.getCurrentItem();

                    if (itemstack3 == null)
                    {
                        return;
                    }

                    if (itemstack1.getItem() == Items.writable_book && itemstack1.getItem() == itemstack3.getItem())
                    {
                        itemstack3.setTagInfo("pages", itemstack1.getTagCompound().getTagList("pages", 8));
                    }

                    return;
                }
            }
            catch (Exception exception3)
            {
                logger.error((String)"Couldn\'t handle book info", (Throwable)exception3);
                return;
            }
            return;
        }
        else if ("MC|BSign".equals(packetIn.getChannelName()))
        {
            PacketBuffer packetBuffer2 = new PacketBuffer(Unpooled.wrappedBuffer((ByteBuf)packetIn.getBufferData()));

            try
            {
                ItemStack itemstack = packetBuffer2.readItemStackFromBuffer();

                if (itemstack != null)
                {
                    if (!ItemEditableBook.validBookTagContents(itemstack.getTagCompound()))
                    {
                        throw new IOException("Invalid book tag!");
                    }

                    ItemStack itemstack2 = this.playerEntity.inventory.getCurrentItem();

                    if (itemstack2 == null)
                    {
                        return;
                    }

                    if (itemstack.getItem() == Items.written_book && itemstack2.getItem() == Items.writable_book)
                    {
                        itemstack2.setTagInfo("author", new NBTTagString(this.playerEntity.getName()));
                        itemstack2.setTagInfo("title", new NBTTagString(itemstack.getTagCompound().getString("title")));
                        itemstack2.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages", 8));
                        itemstack2.setItem(Items.written_book);
                    }

                    return;
                }
            }
            catch (Exception exception4)
            {
                logger.error((String)"Couldn\'t sign book", (Throwable)exception4);
                return;
            }
            return;
        }
        else if ("MC|TrSel".equals(packetIn.getChannelName()))
        {
            try
            {
                int i = packetIn.getBufferData().readInt();
                Container container = this.playerEntity.openContainer;

                if (container instanceof ContainerMerchant)
                {
                    ((ContainerMerchant)container).setCurrentRecipeIndex(i);
                }
            }
            catch (Exception exception2)
            {
                logger.error((String)"Couldn\'t select trade", (Throwable)exception2);
            }
        }
        else if ("MC|AdvCdm".equals(packetIn.getChannelName()))
        {
            if (!this.serverController.isCommandBlockEnabled())
            {
                this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notEnabled", new Object[0]));
            }
            else if (this.playerEntity.canCommandSenderUseCommand(2, "") && this.playerEntity.capabilities.isCreativeMode)
            {
                PacketBuffer packetbuffer = packetIn.getBufferData();

                try
                {
                    int j = packetbuffer.readByte();
                    CommandBlockLogic commandBlockLogic = null;

                    if (j == 0)
                    {
                        TileEntity tileEntity = this.playerEntity.worldObj.getTileEntity(new BlockPos(packetbuffer.readInt(), packetbuffer.readInt(), packetbuffer.readInt()));

                        if (tileEntity instanceof TileEntityCommandBlock)
                        {
                            commandBlockLogic = ((TileEntityCommandBlock)tileEntity).getCommandBlockLogic();
                        }
                    }
                    else if (j == 1)
                    {
                        Entity entity = this.playerEntity.worldObj.getEntityByID(packetbuffer.readInt());

                        if (entity instanceof EntityMinecartCommandBlock)
                        {
                            commandBlockLogic = ((EntityMinecartCommandBlock)entity).getCommandBlockLogic();
                        }
                    }

                    String stringValue = packetbuffer.readStringFromBuffer(packetbuffer.readableBytes());
                    boolean flag = packetbuffer.readBoolean();

                    if (commandBlockLogic != null)
                    {
                        commandBlockLogic.setCommand(stringValue);
                        commandBlockLogic.setTrackOutput(flag);

                        if (!flag)
                        {
                            commandBlockLogic.setLastOutput((IChatComponent)null);
                        }

                        commandBlockLogic.updateCommand();
                        this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.setCommand.success", new Object[] {stringValue}));
                    }
                }
                catch (Exception exception1)
                {
                    logger.error((String)"Couldn\'t set command block", (Throwable)exception1);
                }
            }
            else
            {
                this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notAllowed", new Object[0]));
            }
        }
        else if ("MC|Beacon".equals(packetIn.getChannelName()))
        {
            if (this.playerEntity.openContainer instanceof ContainerBeacon)
            {
                try
                {
                    PacketBuffer packetbuffer1 = packetIn.getBufferData();
                    int k = packetbuffer1.readInt();
                    int l = packetbuffer1.readInt();
                    ContainerBeacon containerBeacon = (ContainerBeacon)this.playerEntity.openContainer;
                    Slot slot = containerBeacon.getSlot(0);

                    if (slot.getHasStack())
                    {
                        slot.decrStackSize(1);
                        IInventory iinventory = containerBeacon.getBeaconInventory();
                        iinventory.setField(1, k);
                        iinventory.setField(2, l);
                        iinventory.markDirty();
                    }
                }
                catch (Exception exception)
                {
                    logger.error((String)"Couldn\'t set beacon", (Throwable)exception);
                }
            }
        }
        else if ("MC|ItemName".equals(packetIn.getChannelName()) && this.playerEntity.openContainer instanceof ContainerRepair)
        {
            ContainerRepair containerRepair = (ContainerRepair)this.playerEntity.openContainer;

            if (packetIn.getBufferData() != null && packetIn.getBufferData().readableBytes() >= 1)
            {
                String s = ChatAllowedCharacters.filterAllowedCharacters(packetIn.getBufferData().readStringFromBuffer(32767));

                if (s.length() <= 30)
                {
                    containerRepair.updateItemName(s);
                }
            }
            else
            {
                containerRepair.updateItemName("");
            }
        }
    }
}
