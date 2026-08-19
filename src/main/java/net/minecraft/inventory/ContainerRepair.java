package net.minecraft.inventory;

import java.util.Map;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContainerRepair extends Container
{
    private static final Logger logger = LogManager.getLogger();
    private IInventory outputSlot;
    private IInventory inputSlots;
    private World theWorld;
    private BlockPos selfPosition;
    public int maximumCost;
    private int materialCost;
    private String repairedItemName;
    private final EntityPlayer thePlayer;

    public ContainerRepair(InventoryPlayer playerInventory, World worldIn, EntityPlayer player)
    {
        this(playerInventory, worldIn, BlockPos.ORIGIN, player);
    }

    public ContainerRepair(InventoryPlayer playerInventory, final World worldIn, final BlockPos blockPosIn, EntityPlayer player)
    {
        this.outputSlot = new InventoryCraftResult();
        this.inputSlots = new InventoryBasic("Repair", true, 2)
        {
            public void markDirty()
            {
                super.markDirty();
                ContainerRepair.this.onCraftMatrixChanged(this);
            }
        };
        this.selfPosition = blockPosIn;
        this.theWorld = worldIn;
        this.thePlayer = player;
        this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47));
        this.addSlotToContainer(new Slot(this.outputSlot, 2, 134, 47)
        {
            public boolean isItemValid(ItemStack stack)
            {
                return false;
            }
            public boolean canTakeStack(EntityPlayer playerIn)
            {
                return (playerIn.capabilities.isCreativeMode || playerIn.experienceLevel >= ContainerRepair.this.maximumCost) && ContainerRepair.this.maximumCost > 0 && this.getHasStack();
            }
            public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
            {
                if (!playerIn.capabilities.isCreativeMode)
                {
                    playerIn.addExperienceLevel(-ContainerRepair.this.maximumCost);
                }

                ContainerRepair.this.inputSlots.setInventorySlotContents(0, (ItemStack)null);

                if (ContainerRepair.this.materialCost > 0)
                {
                    ItemStack itemStack = ContainerRepair.this.inputSlots.getStackInSlot(1);

                    if (itemStack != null && itemStack.stackSize > ContainerRepair.this.materialCost)
                    {
                        itemStack.stackSize -= ContainerRepair.this.materialCost;
                        ContainerRepair.this.inputSlots.setInventorySlotContents(1, itemStack);
                    }
                    else
                    {
                        ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
                    }
                }
                else
                {
                    ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
                }

                ContainerRepair.this.maximumCost = 0;
                IBlockState iblockstate = worldIn.getBlockState(blockPosIn);

                if (!playerIn.capabilities.isCreativeMode && !worldIn.isRemote && iblockstate.getBlock() == Blocks.anvil && playerIn.getRNG().nextFloat() < 0.12F)
                {
                    int l = ((Integer)iblockstate.getValue(BlockAnvil.DAMAGE)).intValue();
                    ++l;

                    if (l > 2)
                    {
                        worldIn.setBlockToAir(blockPosIn);
                        worldIn.playAuxSFX(1020, blockPosIn, 0);
                    }
                    else
                    {
                        worldIn.setBlockState(blockPosIn, iblockstate.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(l)), 2);
                        worldIn.playAuxSFX(1021, blockPosIn, 0);
                    }
                }
                else if (!worldIn.isRemote)
                {
                    worldIn.playAuxSFX(1021, blockPosIn, 0);
                }
            }
        });

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k)
        {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        super.onCraftMatrixChanged(inventoryIn);

        if (inventoryIn == this.inputSlots)
        {
            this.updateRepairOutput();
        }
    }

    public void updateRepairOutput()
    {
        int i = 0;
        int j = 1;
        int k = 1;
        int l = 1;
        int secondIntValue = 2;
        int eighthIntValue = 1;
        int thirteenthIntValue = 1;
        ItemStack itemstack = this.inputSlots.getStackInSlot(0);
        this.maximumCost = 1;
        int eighteenthIntValue = 0;
        int thirdIntValue = 0;
        int ninthIntValue = 0;

        if (itemstack == null)
        {
            this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
            this.maximumCost = 0;
        }
        else
        {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
            Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            boolean flag = false;
            thirdIntValue = thirdIntValue + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
            this.materialCost = 0;

            if (itemstack2 != null)
            {
                flag = itemstack2.getItem() == Items.enchanted_book && Items.enchanted_book.getEnchantments(itemstack2).tagCount() > 0;

                if (itemstack1.isItemStackDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2))
                {
                    int eleventhIntValue = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);

                    if (eleventhIntValue <= 0)
                    {
                        this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
                        this.maximumCost = 0;
                        return;
                    }

                    int twentyFirstIntValue;

                    for (twentyFirstIntValue = 0; eleventhIntValue > 0 && twentyFirstIntValue < itemstack2.stackSize; ++twentyFirstIntValue)
                    {
                        int twelfthIntValue = itemstack1.getItemDamage() - eleventhIntValue;
                        itemstack1.setItemDamage(twelfthIntValue);
                        ++eighteenthIntValue;
                        eleventhIntValue = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
                    }

                    this.materialCost = twentyFirstIntValue;
                }
                else
                {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable()))
                    {
                        this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
                        this.maximumCost = 0;
                        return;
                    }

                    if (itemstack1.isItemStackDamageable() && !flag)
                    {
                        int fourteenthIntValue = itemstack.getMaxDamage() - itemstack.getItemDamage();
                        int nineteenthIntValue = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
                        int fourthIntValue = nineteenthIntValue + itemstack1.getMaxDamage() * 12 / 100;
                        int tenthIntValue = fourteenthIntValue + fourthIntValue;
                        int fifteenthIntValue = itemstack1.getMaxDamage() - tenthIntValue;

                        if (fifteenthIntValue < 0)
                        {
                            fifteenthIntValue = 0;
                        }

                        if (fifteenthIntValue < itemstack1.getMetadata())
                        {
                            itemstack1.setItemDamage(fifteenthIntValue);
                            eighteenthIntValue += 2;
                        }
                    }

                    Map<Integer, Integer> secondMap = EnchantmentHelper.getEnchantments(itemstack2);
                    for (Integer enchantmentId : secondMap.keySet())
                    {
                        int sixthIntValue = enchantmentId.intValue();
                        Enchantment enchantment = Enchantment.getEnchantmentById(sixthIntValue);

                        if (enchantment != null)
                        {
                            Integer existingLevel = map.get(enchantmentId);
                            int seventeenthIntValue = existingLevel != null ? existingLevel.intValue() : 0;
                            int twentiethIntValue = secondMap.get(enchantmentId).intValue();
                            int seventhIntValue;

                            if (seventeenthIntValue == twentiethIntValue)
                            {
                                ++twentiethIntValue;
                                seventhIntValue = twentiethIntValue;
                            }
                            else
                            {
                                seventhIntValue = Math.max(twentiethIntValue, seventeenthIntValue);
                            }

                            twentiethIntValue = seventhIntValue;
                            boolean flag1 = enchantment.canApply(itemstack);

                            if (this.thePlayer.capabilities.isCreativeMode || itemstack.getItem() == Items.enchanted_book)
                            {
                                flag1 = true;
                            }

                            for (Integer existingEnchantmentId : map.keySet())
                            {
                                int fifthIntValue = existingEnchantmentId.intValue();

                                if (fifthIntValue != sixthIntValue && !enchantment.canApplyTogether(Enchantment.getEnchantmentById(fifthIntValue)))
                                {
                                    flag1 = false;
                                    ++eighteenthIntValue;
                                }
                            }

                            if (flag1)
                            {
                                if (twentiethIntValue > enchantment.getMaxLevel())
                                {
                                    twentiethIntValue = enchantment.getMaxLevel();
                                }

                                map.put(enchantmentId, Integer.valueOf(twentiethIntValue));
                                int twentySecondIntValue = 0;

                                switch (enchantment.getWeight())
                                {
                                    case 1:
                                        twentySecondIntValue = 8;
                                        break;

                                    case 2:
                                        twentySecondIntValue = 4;

                                    case 3:
                                    case 4:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    default:
                                        break;

                                    case 5:
                                        twentySecondIntValue = 2;
                                        break;

                                    case 10:
                                        twentySecondIntValue = 1;
                                }

                                if (flag)
                                {
                                    twentySecondIntValue = Math.max(1, twentySecondIntValue / 2);
                                }

                                eighteenthIntValue += twentySecondIntValue * twentiethIntValue;
                            }
                        }
                    }
                }
            }

            if (StringUtils.isBlank(this.repairedItemName))
            {
                if (itemstack.hasDisplayName())
                {
                    ninthIntValue = 1;
                    eighteenthIntValue += ninthIntValue;
                    itemstack1.clearCustomName();
                }
            }
            else if (!this.repairedItemName.equals(itemstack.getDisplayName()))
            {
                ninthIntValue = 1;
                eighteenthIntValue += ninthIntValue;
                itemstack1.setStackDisplayName(this.repairedItemName);
            }

            this.maximumCost = thirdIntValue + eighteenthIntValue;

            if (eighteenthIntValue <= 0)
            {
                itemstack1 = null;
            }

            if (ninthIntValue == eighteenthIntValue && ninthIntValue > 0 && this.maximumCost >= 40)
            {
                this.maximumCost = 39;
            }

            if (this.maximumCost >= 40 && !this.thePlayer.capabilities.isCreativeMode)
            {
                itemstack1 = null;
            }

            if (itemstack1 != null)
            {
                int sixteenthIntValue = itemstack1.getRepairCost();

                if (itemstack2 != null && sixteenthIntValue < itemstack2.getRepairCost())
                {
                    sixteenthIntValue = itemstack2.getRepairCost();
                }

                sixteenthIntValue = sixteenthIntValue * 2 + 1;
                itemstack1.setRepairCost(sixteenthIntValue);
                EnchantmentHelper.setEnchantments(map, itemstack1);
            }

            this.outputSlot.setInventorySlotContents(0, itemstack1);
            this.detectAndSendChanges();
        }
    }

    public void onCraftGuiOpened(ICrafting listener)
    {
        super.onCraftGuiOpened(listener);
        listener.sendProgressBarUpdate(this, 0, this.maximumCost);
    }

    public void updateProgressBar(int id, int data)
    {
        if (id == 0)
        {
            this.maximumCost = data;
        }
    }

    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.theWorld.isRemote)
        {
            for (int i = 0; i < this.inputSlots.getSizeInventory(); ++i)
            {
                ItemStack itemStack = this.inputSlots.removeStackFromSlot(i);

                if (itemStack != null)
                {
                    playerIn.dropPlayerItemWithRandomChoice(itemStack, false);
                }
            }
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.theWorld.getBlockState(this.selfPosition).getBlock() != Blocks.anvil ? false : playerIn.getDistanceSq((double)this.selfPosition.getX() + 0.5D, (double)this.selfPosition.getY() + 0.5D, (double)this.selfPosition.getZ() + 0.5D) <= 64.0D;
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemStack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemStack = itemstack1.copy();

            if (index == 2)
            {
                if (!this.mergeItemStack(itemstack1, 3, 39, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemStack);
            }
            else if (index != 0 && index != 1)
            {
                if (index >= 3 && index < 39 && !this.mergeItemStack(itemstack1, 0, 2, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 3, 39, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemStack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemStack;
    }

    public void updateItemName(String newName)
    {
        this.repairedItemName = newName;

        if (this.getSlot(2).getHasStack())
        {
            ItemStack itemStack = this.getSlot(2).getStack();

            if (StringUtils.isBlank(newName))
            {
                itemStack.clearCustomName();
            }
            else
            {
                itemStack.setStackDisplayName(this.repairedItemName);
            }
        }

        this.updateRepairOutput();
    }
}
