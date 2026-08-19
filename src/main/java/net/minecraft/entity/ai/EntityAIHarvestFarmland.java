package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityAIHarvestFarmland extends EntityAIMoveToBlock
{
    private final EntityVillager theVillager;
    private boolean hasFarmItem;
    private boolean canVillagerFarm;
    private int currentTask;

    public EntityAIHarvestFarmland(EntityVillager theVillagerIn, double speedIn)
    {
        super(theVillagerIn, speedIn, 16);
        this.theVillager = theVillagerIn;
    }

    public boolean shouldExecute()
    {
        if (this.runDelay <= 0)
        {
            if (!this.theVillager.worldObj.getGameRules().getBoolean("mobGriefing"))
            {
                return false;
            }

            this.currentTask = -1;
            this.hasFarmItem = this.theVillager.isFarmItemInInventory();
            this.canVillagerFarm = this.theVillager.wantsMoreFood();
        }

        return super.shouldExecute();
    }

    public boolean continueExecuting()
    {
        return this.currentTask >= 0 && super.continueExecuting();
    }

    public void startExecuting()
    {
        super.startExecuting();
    }

    public void resetTask()
    {
        super.resetTask();
    }

    public void updateTask()
    {
        super.updateTask();
        this.theVillager.getLookHelper().setLookPosition((double)this.destinationBlock.getX() + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)this.destinationBlock.getZ() + 0.5D, 10.0F, (float)this.theVillager.getVerticalFaceSpeed());

        if (this.getIsAboveDestination())
        {
            World world = this.theVillager.worldObj;
            BlockPos blockPos = this.destinationBlock.up();
            IBlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (this.currentTask == 0 && block instanceof BlockCrops && ((Integer)blockState.getValue(BlockCrops.AGE)).intValue() == 7)
            {
                world.destroyBlock(blockPos, true);
            }
            else if (this.currentTask == 1 && block == Blocks.air)
            {
                InventoryBasic inventoryBasic = this.theVillager.getVillagerInventory();

                for (int slotIndex = 0; slotIndex < inventoryBasic.getSizeInventory(); ++slotIndex)
                {
                    ItemStack itemStack = inventoryBasic.getStackInSlot(slotIndex);
                    boolean plantedCrop = false;

                    if (itemStack != null)
                    {
                        if (itemStack.getItem() == Items.wheat_seeds)
                        {
                            world.setBlockState(blockPos, Blocks.wheat.getDefaultState(), 3);
                            plantedCrop = true;
                        }
                        else if (itemStack.getItem() == Items.potato)
                        {
                            world.setBlockState(blockPos, Blocks.potatoes.getDefaultState(), 3);
                            plantedCrop = true;
                        }
                        else if (itemStack.getItem() == Items.carrot)
                        {
                            world.setBlockState(blockPos, Blocks.carrots.getDefaultState(), 3);
                            plantedCrop = true;
                        }
                    }

                    if (plantedCrop)
                    {
                        --itemStack.stackSize;

                        if (itemStack.stackSize <= 0)
                        {
                            inventoryBasic.setInventorySlotContents(slotIndex, (ItemStack)null);
                        }

                        break;
                    }
                }
            }

            this.currentTask = -1;
            this.runDelay = 10;
        }
    }

    protected boolean shouldMoveTo(World worldIn, BlockPos pos)
    {
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block == Blocks.farmland)
        {
            pos = pos.up();
            IBlockState blockState = worldIn.getBlockState(pos);
            block = blockState.getBlock();

            if (block instanceof BlockCrops && ((Integer)blockState.getValue(BlockCrops.AGE)).intValue() == 7 && this.canVillagerFarm && (this.currentTask == 0 || this.currentTask < 0))
            {
                this.currentTask = 0;
                return true;
            }

            if (block == Blocks.air && this.hasFarmItem && (this.currentTask == 1 || this.currentTask < 0))
            {
                this.currentTask = 1;
                return true;
            }
        }

        return false;
    }
}
