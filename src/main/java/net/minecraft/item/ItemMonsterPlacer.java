package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemMonsterPlacer extends Item
{
    public ItemMonsterPlacer()
    {
        this.setHasSubtypes(true);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        String displayName = ("" + StatCollector.translateToLocal(this.getUnlocalizedName() + ".name")).trim();
        String entityName = EntityList.getStringFromID(stack.getMetadata());

        if (entityName != null)
        {
            displayName = displayName + " " + StatCollector.translateToLocal("entity." + entityName + ".name");
        }

        return displayName;
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        EntityList.EntityEggInfo entityEggInfo = (EntityList.EntityEggInfo)EntityList.entityEggs.get(Integer.valueOf(stack.getMetadata()));
        return entityEggInfo != null ? (renderPass == 0 ? entityEggInfo.primaryColor : entityEggInfo.secondaryColor) : 16777215;
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else if (!playerIn.canPlayerEdit(pos.offset(side), side, stack))
        {
            return false;
        }
        else
        {
            IBlockState blockState = worldIn.getBlockState(pos);

            if (blockState.getBlock() == Blocks.mob_spawner)
            {
                TileEntity tileEntity = worldIn.getTileEntity(pos);

                if (tileEntity instanceof TileEntityMobSpawner)
                {
                    MobSpawnerBaseLogic mobSpawnerBaseLogic = ((TileEntityMobSpawner)tileEntity).getSpawnerBaseLogic();
                    mobSpawnerBaseLogic.setEntityName(EntityList.getStringFromID(stack.getMetadata()));
                    tileEntity.markDirty();
                    worldIn.markBlockForUpdate(pos);

                    if (!playerIn.capabilities.isCreativeMode)
                    {
                        --stack.stackSize;
                    }

                    return true;
                }
            }

            pos = pos.offset(side);
            double spawnYOffset = 0.0D;

            if (side == EnumFacing.UP && blockState instanceof BlockFence)
            {
                spawnYOffset = 0.5D;
            }

            Entity entity = spawnCreature(worldIn, stack.getMetadata(), (double)pos.getX() + 0.5D, (double)pos.getY() + spawnYOffset, (double)pos.getZ() + 0.5D);

            if (entity != null)
            {
                if (entity instanceof EntityLivingBase && stack.hasDisplayName())
                {
                    entity.setCustomNameTag(stack.getDisplayName());
                }

                if (!playerIn.capabilities.isCreativeMode)
                {
                    --stack.stackSize;
                }
            }

            return true;
        }
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        if (worldIn.isRemote)
        {
            return itemStackIn;
        }
        else
        {
            MovingObjectPosition movingObjectPosition = this.getMovingObjectPositionFromPlayer(worldIn, playerIn, true);

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

                    if (!playerIn.canPlayerEdit(targetPos, movingObjectPosition.sideHit, itemStackIn))
                    {
                        return itemStackIn;
                    }

                    if (worldIn.getBlockState(targetPos).getBlock() instanceof BlockLiquid)
                    {
                        Entity entity = spawnCreature(worldIn, itemStackIn.getMetadata(), (double)targetPos.getX() + 0.5D, (double)targetPos.getY() + 0.5D, (double)targetPos.getZ() + 0.5D);

                        if (entity != null)
                        {
                            if (entity instanceof EntityLivingBase && itemStackIn.hasDisplayName())
                            {
                                ((EntityLiving)entity).setCustomNameTag(itemStackIn.getDisplayName());
                            }

                            if (!playerIn.capabilities.isCreativeMode)
                            {
                                --itemStackIn.stackSize;
                            }

                            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                        }
                    }
                }

                return itemStackIn;
            }
        }
    }

    public static Entity spawnCreature(World worldIn, int entityID, double x, double y, double z)
    {
        if (!EntityList.entityEggs.containsKey(Integer.valueOf(entityID)))
        {
            return null;
        }
        else
        {
            Entity entity = null;

            for (int spawnAttempt = 0; spawnAttempt < 1; ++spawnAttempt)
            {
                entity = EntityList.createEntityByID(entityID, worldIn);

                if (entity instanceof EntityLivingBase)
                {
                    EntityLiving entityLiving = (EntityLiving)entity;
                    entity.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(worldIn.rand.nextFloat() * 360.0F), 0.0F);
                    entityLiving.rotationYawHead = entityLiving.rotationYaw;
                    entityLiving.renderYawOffset = entityLiving.rotationYaw;
                    entityLiving.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityLiving)), (IEntityLivingData)null);
                    worldIn.spawnEntityInWorld(entity);
                    entityLiving.playLivingSound();
                }
            }

            return entity;
        }
    }

    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (EntityList.EntityEggInfo entityEggInfo : EntityList.entityEggs.values())
        {
            subItems.add(new ItemStack(itemIn, 1, entityEggInfo.spawnedID));
        }
    }
}
