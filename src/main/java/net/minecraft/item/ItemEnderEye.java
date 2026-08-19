package net.minecraft.item;

import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemEnderEye extends Item
{
    public ItemEnderEye()
    {
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        IBlockState frameState = worldIn.getBlockState(pos);

        if (playerIn.canPlayerEdit(pos.offset(side), side, stack) && frameState.getBlock() == Blocks.end_portal_frame && !((Boolean)frameState.getValue(BlockEndPortalFrame.EYE)).booleanValue())
        {
            if (worldIn.isRemote)
            {
                return true;
            }
            else
            {
                worldIn.setBlockState(pos, frameState.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(true)), 2);
                worldIn.updateComparatorOutputLevel(pos, Blocks.end_portal_frame);
                --stack.stackSize;

                for (int particleIndex = 0; particleIndex < 16; ++particleIndex)
                {
                    double particleX = (double)((float)pos.getX() + (5.0F + itemRand.nextFloat() * 6.0F) / 16.0F);
                    double particleY = (double)((float)pos.getY() + 0.8125F);
                    double particleZ = (double)((float)pos.getZ() + (5.0F + itemRand.nextFloat() * 6.0F) / 16.0F);
                    double particleVelocityX = 0.0D;
                    double particleVelocityY = 0.0D;
                    double particleVelocityZ = 0.0D;
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, particleX, particleY, particleZ, particleVelocityX, particleVelocityY, particleVelocityZ, EnumParticleTypes.EMPTY_ARGS);
                }

                EnumFacing frameFacing = (EnumFacing)frameState.getValue(BlockEndPortalFrame.FACING);
                int firstFrameOffset = 0;
                int lastFrameOffset = 0;
                boolean foundFrame = false;
                boolean allFramesFilled = true;
                EnumFacing sideFacing = frameFacing.rotateY();

                for (int frameOffset = -2; frameOffset <= 2; ++frameOffset)
                {
                    BlockPos framePos = pos.offset(sideFacing, frameOffset);
                    IBlockState sideFrameState = worldIn.getBlockState(framePos);

                    if (sideFrameState.getBlock() == Blocks.end_portal_frame)
                    {
                        if (!((Boolean)sideFrameState.getValue(BlockEndPortalFrame.EYE)).booleanValue())
                        {
                            allFramesFilled = false;
                            break;
                        }

                        lastFrameOffset = frameOffset;

                        if (!foundFrame)
                        {
                            firstFrameOffset = frameOffset;
                            foundFrame = true;
                        }
                    }
                }

                if (allFramesFilled && lastFrameOffset == firstFrameOffset + 2)
                {
                    BlockPos scanStartPos = pos.offset(frameFacing, 4);

                    for (int frameOffset = firstFrameOffset; frameOffset <= lastFrameOffset; ++frameOffset)
                    {
                        BlockPos framePos = scanStartPos.offset(sideFacing, frameOffset);
                        IBlockState farFrameState = worldIn.getBlockState(framePos);

                        if (farFrameState.getBlock() != Blocks.end_portal_frame || !((Boolean)farFrameState.getValue(BlockEndPortalFrame.EYE)).booleanValue())
                        {
                            allFramesFilled = false;
                            break;
                        }
                    }

                    for (int sideOffset = firstFrameOffset - 1; sideOffset <= lastFrameOffset + 1; sideOffset += 4)
                    {
                        scanStartPos = pos.offset(sideFacing, sideOffset);

                        for (int forwardOffset = 1; forwardOffset <= 3; ++forwardOffset)
                        {
                            BlockPos framePos = scanStartPos.offset(frameFacing, forwardOffset);
                            IBlockState sideFrameState = worldIn.getBlockState(framePos);

                            if (sideFrameState.getBlock() != Blocks.end_portal_frame || !((Boolean)sideFrameState.getValue(BlockEndPortalFrame.EYE)).booleanValue())
                            {
                                allFramesFilled = false;
                                break;
                            }
                        }
                    }

                    if (allFramesFilled)
                    {
                        for (int portalSideOffset = firstFrameOffset; portalSideOffset <= lastFrameOffset; ++portalSideOffset)
                        {
                            scanStartPos = pos.offset(sideFacing, portalSideOffset);

                            for (int portalForwardOffset = 1; portalForwardOffset <= 3; ++portalForwardOffset)
                            {
                                BlockPos portalPos = scanStartPos.offset(frameFacing, portalForwardOffset);
                                worldIn.setBlockState(portalPos, Blocks.end_portal.getDefaultState(), 2);
                            }
                        }
                    }
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        MovingObjectPosition movingObjectPosition = this.getMovingObjectPositionFromPlayer(worldIn, playerIn, false);

        if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && worldIn.getBlockState(movingObjectPosition.getBlockPos()).getBlock() == Blocks.end_portal_frame)
        {
            return itemStackIn;
        }
        else
        {
            if (!worldIn.isRemote)
            {
                BlockPos strongholdPos = worldIn.getStrongholdPos("Stronghold", new BlockPos(playerIn));

                if (strongholdPos != null)
                {
                    EntityEnderEye enderEye = new EntityEnderEye(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
                    enderEye.moveTowards(strongholdPos);
                    worldIn.spawnEntityInWorld(enderEye);
                    worldIn.playSoundAtEntity(playerIn, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                    worldIn.playAuxSFXAtEntity((EntityPlayer)null, 1002, new BlockPos(playerIn), 0);

                    if (!playerIn.capabilities.isCreativeMode)
                    {
                        --itemStackIn.stackSize;
                    }

                    playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                }
            }

            return itemStackIn;
        }
    }
}
