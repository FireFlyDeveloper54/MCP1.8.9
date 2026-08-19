package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemBucket extends Item
{
    private Block containedBlock;

    public ItemBucket(Block containedBlock)
    {
        this.maxStackSize = 1;
        this.containedBlock = containedBlock;
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        boolean isEmptyBucket = this.containedBlock == Blocks.air;
        MovingObjectPosition movingObjectPosition = this.getMovingObjectPositionFromPlayer(worldIn, playerIn, isEmptyBucket);

        if (movingObjectPosition == null)
        {
            return itemStackIn;
        }
        else
        {
            if (movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos targetPos = movingObjectPosition.getBlockPos();

                if (!worldIn.isBlockModifiable(playerIn, targetPos))
                {
                    return itemStackIn;
                }

                if (isEmptyBucket)
                {
                    if (!playerIn.canPlayerEdit(targetPos.offset(movingObjectPosition.sideHit), movingObjectPosition.sideHit, itemStackIn))
                    {
                        return itemStackIn;
                    }

                    IBlockState targetState = worldIn.getBlockState(targetPos);
                    Material material = targetState.getBlock().getMaterial();

                    if (material == Material.water && ((Integer)targetState.getValue(BlockLiquid.LEVEL)) == 0)
                    {
                        worldIn.setBlockToAir(targetPos);
                        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                        return this.fillBucket(itemStackIn, playerIn, Items.water_bucket);
                    }

                    if (material == Material.lava && ((Integer)targetState.getValue(BlockLiquid.LEVEL)) == 0)
                    {
                        worldIn.setBlockToAir(targetPos);
                        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                        return this.fillBucket(itemStackIn, playerIn, Items.lava_bucket);
                    }
                }
                else
                {
                    if (this.containedBlock == Blocks.air)
                    {
                        return new ItemStack(Items.bucket);
                    }

                    BlockPos placePos = targetPos.offset(movingObjectPosition.sideHit);

                    if (!playerIn.canPlayerEdit(placePos, movingObjectPosition.sideHit, itemStackIn))
                    {
                        return itemStackIn;
                    }

                    if (this.tryPlaceContainedLiquid(worldIn, placePos) && !playerIn.capabilities.isCreativeMode)
                    {
                        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                        return new ItemStack(Items.bucket);
                    }
                }
            }

            return itemStackIn;
        }
    }

    private ItemStack fillBucket(ItemStack emptyBuckets, EntityPlayer player, Item fullBucket)
    {
        if (player.capabilities.isCreativeMode)
        {
            return emptyBuckets;
        }
        else if (--emptyBuckets.stackSize <= 0)
        {
            return new ItemStack(fullBucket);
        }
        else
        {
            if (!player.inventory.addItemStackToInventory(new ItemStack(fullBucket)))
            {
                player.dropPlayerItemWithRandomChoice(new ItemStack(fullBucket, 1, 0), false);
            }

            return emptyBuckets;
        }
    }

    public boolean tryPlaceContainedLiquid(World worldIn, BlockPos pos)
    {
        if (this.containedBlock == Blocks.air)
        {
            return false;
        }
        else
        {
            Material material = worldIn.getBlockState(pos).getBlock().getMaterial();
            boolean canReplaceExisting = !material.isSolid();

            if (!worldIn.isAirBlock(pos) && !canReplaceExisting)
            {
                return false;
            }
            else
            {
                if (worldIn.provider.doesWaterVaporize() && this.containedBlock == Blocks.flowing_water)
                {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    worldIn.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "random.fizz", 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

                    for (int smokeIndex = 0; smokeIndex < 8; ++smokeIndex)
                    {
                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                    }
                }
                else
                {
                    if (!worldIn.isRemote && canReplaceExisting && !material.isLiquid())
                    {
                        worldIn.destroyBlock(pos, true);
                    }

                    worldIn.setBlockState(pos, this.containedBlock.getDefaultState(), 3);
                }

                return true;
            }
        }
    }
}
