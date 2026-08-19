package net.minecraft.server.management;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class ItemInWorldManager
{
    public World theWorld;
    public EntityPlayerMP thisPlayerMP;
    private WorldSettings.GameType gameType = WorldSettings.GameType.NOT_SET;
    private boolean isDestroyingBlock;
    private int initialDamage;
    private BlockPos currentBlockPos = BlockPos.ORIGIN;
    private int curblockDamage;
    private boolean receivedFinishDiggingPacket;
    private BlockPos delayedBreakPos = BlockPos.ORIGIN;
    private int initialBlockDamage;
    private int durabilityRemainingOnBlock = -1;

    public ItemInWorldManager(World worldIn)
    {
        this.theWorld = worldIn;
    }

    public void setGameType(WorldSettings.GameType type)
    {
        this.gameType = type;
        type.configurePlayerCapabilities(this.thisPlayerMP.capabilities);
        this.thisPlayerMP.sendPlayerAbilities();
        this.thisPlayerMP.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.UPDATE_GAME_MODE, new EntityPlayerMP[] {this.thisPlayerMP}));
    }

    public WorldSettings.GameType getGameType()
    {
        return this.gameType;
    }

    public boolean survivalOrAdventure()
    {
        return this.gameType.isSurvivalOrAdventure();
    }

    public boolean isCreative()
    {
        return this.gameType.isCreative();
    }

    public void initializeGameType(WorldSettings.GameType type)
    {
        if (this.gameType == WorldSettings.GameType.NOT_SET)
        {
            this.gameType = type;
        }

        this.setGameType(this.gameType);
    }

    public void updateBlockRemoving()
    {
        ++this.curblockDamage;

        if (this.receivedFinishDiggingPacket)
        {
            int delayedBreakTicks = this.curblockDamage - this.initialBlockDamage;
            Block block = this.theWorld.getBlockState(this.delayedBreakPos).getBlock();

            if (block.getMaterial() == Material.air)
            {
                this.receivedFinishDiggingPacket = false;
            }
            else
            {
                float delayedBreakProgress = block.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, this.delayedBreakPos) * (float)(delayedBreakTicks + 1);
                int delayedBreakStage = (int)(delayedBreakProgress * 10.0F);

                if (delayedBreakStage != this.durabilityRemainingOnBlock)
                {
                    this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.delayedBreakPos, delayedBreakStage);
                    this.durabilityRemainingOnBlock = delayedBreakStage;
                }

                if (delayedBreakProgress >= 1.0F)
                {
                    this.receivedFinishDiggingPacket = false;
                    this.tryHarvestBlock(this.delayedBreakPos);
                }
            }
        }
        else if (this.isDestroyingBlock)
        {
            Block currentBlock = this.theWorld.getBlockState(this.currentBlockPos).getBlock();

            if (currentBlock.getMaterial() == Material.air)
            {
                this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.currentBlockPos, -1);
                this.durabilityRemainingOnBlock = -1;
                this.isDestroyingBlock = false;
            }
            else
            {
                int currentBreakTicks = this.curblockDamage - this.initialDamage;
                float blockDamage = currentBlock.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, this.delayedBreakPos) * (float)(currentBreakTicks + 1);
                int currentBreakStage = (int)(blockDamage * 10.0F);

                if (currentBreakStage != this.durabilityRemainingOnBlock)
                {
                    this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.currentBlockPos, currentBreakStage);
                    this.durabilityRemainingOnBlock = currentBreakStage;
                }
            }
        }
    }

    public void onBlockClicked(BlockPos pos, EnumFacing side)
    {
        if (this.isCreative())
        {
            if (!this.theWorld.extinguishFire((EntityPlayer)null, pos, side))
            {
                this.tryHarvestBlock(pos);
            }
        }
        else
        {
            Block block = this.theWorld.getBlockState(pos).getBlock();

            if (this.gameType.isAdventure())
            {
                if (this.gameType == WorldSettings.GameType.SPECTATOR)
                {
                    return;
                }

                if (!this.thisPlayerMP.isAllowEdit())
                {
                    ItemStack itemStack = this.thisPlayerMP.getCurrentEquippedItem();

                    if (itemStack == null)
                    {
                        return;
                    }

                    if (!itemStack.canDestroy(block))
                    {
                        return;
                    }
                }
            }

            this.theWorld.extinguishFire((EntityPlayer)null, pos, side);
            this.initialDamage = this.curblockDamage;
            float blockHardness = 1.0F;

            if (block.getMaterial() != Material.air)
            {
                block.onBlockClicked(this.theWorld, pos, this.thisPlayerMP);
                blockHardness = block.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, pos);
            }

            if (block.getMaterial() != Material.air && blockHardness >= 1.0F)
            {
                this.tryHarvestBlock(pos);
            }
            else
            {
                this.isDestroyingBlock = true;
                this.currentBlockPos = pos;
                int breakStage = (int)(blockHardness * 10.0F);
                this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), pos, breakStage);
                this.durabilityRemainingOnBlock = breakStage;
            }
        }
    }

    public void blockRemoving(BlockPos pos)
    {
        if (pos.equals(this.currentBlockPos))
        {
            int breakTicks = this.curblockDamage - this.initialDamage;
            Block block = this.theWorld.getBlockState(pos).getBlock();

            if (block.getMaterial() != Material.air)
            {
                float breakProgress = block.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, pos) * (float)(breakTicks + 1);

                if (breakProgress >= 0.7F)
                {
                    this.isDestroyingBlock = false;
                    this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), pos, -1);
                    this.tryHarvestBlock(pos);
                }
                else if (!this.receivedFinishDiggingPacket)
                {
                    this.isDestroyingBlock = false;
                    this.receivedFinishDiggingPacket = true;
                    this.delayedBreakPos = pos;
                    this.initialBlockDamage = this.initialDamage;
                }
            }
        }
    }

    public void cancelDestroyingBlock()
    {
        this.isDestroyingBlock = false;
        this.theWorld.sendBlockBreakProgress(this.thisPlayerMP.getEntityId(), this.currentBlockPos, -1);
    }

    private boolean removeBlock(BlockPos pos)
    {
        IBlockState blockState = this.theWorld.getBlockState(pos);
        blockState.getBlock().onBlockHarvested(this.theWorld, pos, blockState, this.thisPlayerMP);
        boolean removedBlock = this.theWorld.setBlockToAir(pos);

        if (removedBlock)
        {
            blockState.getBlock().onBlockDestroyedByPlayer(this.theWorld, pos, blockState);
        }

        return removedBlock;
    }

    public boolean tryHarvestBlock(BlockPos pos)
    {
        if (this.gameType.isCreative() && this.thisPlayerMP.getHeldItem() != null && this.thisPlayerMP.getHeldItem().getItem() instanceof ItemSword)
        {
            return false;
        }
        else
        {
            IBlockState blockState = this.theWorld.getBlockState(pos);
            TileEntity tileEntity = this.theWorld.getTileEntity(pos);

            if (this.gameType.isAdventure())
            {
                if (this.gameType == WorldSettings.GameType.SPECTATOR)
                {
                    return false;
                }

                if (!this.thisPlayerMP.isAllowEdit())
                {
                    ItemStack itemStack = this.thisPlayerMP.getCurrentEquippedItem();

                    if (itemStack == null)
                    {
                        return false;
                    }

                    if (!itemStack.canDestroy(blockState.getBlock()))
                    {
                        return false;
                    }
                }
            }

            this.theWorld.playAuxSFXAtEntity(this.thisPlayerMP, 2001, pos, Block.getStateId(blockState));
            boolean removedBlock = this.removeBlock(pos);

            if (this.isCreative())
            {
                this.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(this.theWorld, pos));
            }
            else
            {
                ItemStack heldItem = this.thisPlayerMP.getCurrentEquippedItem();
                boolean canHarvestBlock = this.thisPlayerMP.canHarvestBlock(blockState.getBlock());

                if (heldItem != null)
                {
                    heldItem.onBlockDestroyed(this.theWorld, blockState.getBlock(), pos, this.thisPlayerMP);

                    if (heldItem.stackSize == 0)
                    {
                        this.thisPlayerMP.destroyCurrentEquippedItem();
                    }
                }

                if (removedBlock && canHarvestBlock)
                {
                    blockState.getBlock().harvestBlock(this.theWorld, this.thisPlayerMP, pos, blockState, tileEntity);
                }
            }

            return removedBlock;
        }
    }

    public boolean tryUseItem(EntityPlayer player, World worldIn, ItemStack stack)
    {
        if (this.gameType == WorldSettings.GameType.SPECTATOR)
        {
            return false;
        }
        else
        {
            int originalStackSize = stack.stackSize;
            int originalMetadata = stack.getMetadata();
            ItemStack usedItemStack = stack.useItemRightClick(worldIn, player);

            if (usedItemStack != stack || usedItemStack != null && (usedItemStack.stackSize != originalStackSize || usedItemStack.getMaxItemUseDuration() > 0 || usedItemStack.getMetadata() != originalMetadata))
            {
                player.inventory.mainInventory[player.inventory.currentItem] = usedItemStack;

                if (this.isCreative())
                {
                    usedItemStack.stackSize = originalStackSize;

                    if (usedItemStack.isItemStackDamageable())
                    {
                        usedItemStack.setItemDamage(originalMetadata);
                    }
                }

                if (usedItemStack.stackSize == 0)
                {
                    player.inventory.mainInventory[player.inventory.currentItem] = null;
                }

                if (!player.isUsingItem())
                {
                    ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public boolean activateBlockOrUseItem(EntityPlayer player, World worldIn, ItemStack stack, BlockPos pos, EnumFacing side, float offsetX, float offsetY, float offsetZ)
    {
        if (this.gameType == WorldSettings.GameType.SPECTATOR)
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            if (tileEntity instanceof ILockableContainer)
            {
                Block block = worldIn.getBlockState(pos).getBlock();
                ILockableContainer lockableContainer = (ILockableContainer)tileEntity;

                if (lockableContainer instanceof TileEntityChest && block instanceof BlockChest)
                {
                    lockableContainer = ((BlockChest)block).getLockableContainer(worldIn, pos);
                }

                if (lockableContainer != null)
                {
                    player.displayGUIChest(lockableContainer);
                    return true;
                }
            }
            else if (tileEntity instanceof IInventory)
            {
                player.displayGUIChest((IInventory)tileEntity);
                return true;
            }

            return false;
        }
        else
        {
            if (!player.isSneaking() || player.getHeldItem() == null)
            {
                IBlockState blockState = worldIn.getBlockState(pos);

                if (blockState.getBlock().onBlockActivated(worldIn, pos, blockState, player, side, offsetX, offsetY, offsetZ))
                {
                    return true;
                }
            }

            if (stack == null)
            {
                return false;
            }
            else if (this.isCreative())
            {
                int originalMetadata = stack.getMetadata();
                int originalStackSize = stack.stackSize;
                boolean usedItem = stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
                stack.setItemDamage(originalMetadata);
                stack.stackSize = originalStackSize;
                return usedItem;
            }
            else
            {
                return stack.onItemUse(player, worldIn, pos, side, offsetX, offsetY, offsetZ);
            }
        }
    }

    public void setWorld(WorldServer serverWorld)
    {
        this.theWorld = serverWorld;
    }
}
