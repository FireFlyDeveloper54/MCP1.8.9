package net.optifine.override;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class PlayerControllerOF extends PlayerControllerMP
{
    private boolean acting = false;
    private BlockPos lastClickBlockPos = null;
    private Entity lastClickEntity = null;

    public PlayerControllerOF(Minecraft mcIn, NetHandlerPlayClient netHandler)
    {
        super(mcIn, netHandler);
    }

    public boolean clickBlock(BlockPos loc, EnumFacing face)
    {
        this.acting = true;
        this.lastClickBlockPos = loc;
        boolean clickedBlock = super.clickBlock(loc, face);
        this.acting = false;
        return clickedBlock;
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing)
    {
        this.acting = true;
        this.lastClickBlockPos = posBlock;
        boolean damagedBlock = super.onPlayerDamageBlock(posBlock, directionFacing);
        this.acting = false;
        return damagedBlock;
    }

    public boolean sendUseItem(EntityPlayer player, World worldIn, ItemStack stack)
    {
        this.acting = true;
        boolean usedItem = super.sendUseItem(player, worldIn, stack);
        this.acting = false;
        return usedItem;
    }

    public boolean onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack stack, BlockPos pos, EnumFacing side, Vec3 hitVec)
    {
        this.acting = true;
        this.lastClickBlockPos = pos;
        boolean rightClickedBlock = super.onPlayerRightClick(player, worldIn, stack, pos, side, hitVec);
        this.acting = false;
        return rightClickedBlock;
    }

    public boolean interactWithEntitySendPacket(EntityPlayer player, Entity target)
    {
        this.lastClickEntity = target;
        return super.interactWithEntitySendPacket(player, target);
    }

    public boolean isPlayerRightClickingOnEntity(EntityPlayer player, Entity target, MovingObjectPosition ray)
    {
        this.lastClickEntity = target;
        return super.isPlayerRightClickingOnEntity(player, target, ray);
    }

    public boolean isActing()
    {
        return this.acting;
    }

    public BlockPos getLastClickBlockPos()
    {
        return this.lastClickBlockPos;
    }

    public Entity getLastClickEntity()
    {
        return this.lastClickEntity;
    }
}
