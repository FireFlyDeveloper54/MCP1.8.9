package net.minecraft.item;

import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;

public class ItemArmor extends Item
{
    private static final int[] maxDamageArray = new int[] {11, 16, 15, 13};
    public static final String[] EMPTY_SLOT_NAMES = new String[] {"minecraft:items/empty_armor_slot_helmet", "minecraft:items/empty_armor_slot_chestplate", "minecraft:items/empty_armor_slot_leggings", "minecraft:items/empty_armor_slot_boots"};
    private static final IBehaviorDispenseItem dispenserBehavior = new BehaviorDefaultDispenseItem()
    {
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
        {
            BlockPos blockPos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();
            AxisAlignedBB targetBox = new AxisAlignedBB((double)x, (double)y, (double)z, (double)(x + 1), (double)(y + 1), (double)(z + 1));
            List<EntityLivingBase> candidates = source.getWorld().<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, targetBox, Predicates.<EntityLivingBase> and (EntitySelectors.NOT_SPECTATING, new EntitySelectors.ArmoredMob(stack)));

            if (candidates.size() > 0)
            {
                EntityLivingBase entityLivingBase = (EntityLivingBase)candidates.get(0);
                int playerSlotOffset = entityLivingBase instanceof EntityPlayer ? 1 : 0;
                int armorPosition = EntityLiving.getArmorPosition(stack);
                ItemStack itemStack = stack.copy();
                itemStack.stackSize = 1;
                entityLivingBase.setCurrentItemOrArmor(armorPosition - playerSlotOffset, itemStack);

                if (entityLivingBase instanceof EntityLiving)
                {
                    ((EntityLiving)entityLivingBase).setEquipmentDropChance(armorPosition, 2.0F);
                }

                --stack.stackSize;
                return stack;
            }
            else
            {
                return super.dispenseStack(source, stack);
            }
        }
    };
    public final int armorType;
    public final int damageReduceAmount;
    public final int renderIndex;
    private final ItemArmor.ArmorMaterial material;

    public ItemArmor(ItemArmor.ArmorMaterial material, int renderIndex, int armorType)
    {
        this.material = material;
        this.armorType = armorType;
        this.renderIndex = renderIndex;
        this.damageReduceAmount = material.getDamageReductionAmount(armorType);
        this.setMaxDamage(material.getDurability(armorType));
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.tabCombat);
        BlockDispenser.dispenseBehaviorRegistry.putObject(this, dispenserBehavior);
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        if (renderPass > 0)
        {
            return 16777215;
        }
        else
        {
            int armorColor = this.getColor(stack);

            if (armorColor < 0)
            {
                armorColor = 16777215;
            }

            return armorColor;
        }
    }

    public int getItemEnchantability()
    {
        return this.material.getEnchantability();
    }

    public ItemArmor.ArmorMaterial getArmorMaterial()
    {
        return this.material;
    }

    public boolean hasColor(ItemStack stack)
    {
        return this.material != ItemArmor.ArmorMaterial.LEATHER ? false : (!stack.hasTagCompound() ? false : (!stack.getTagCompound().hasKey("display", 10) ? false : stack.getTagCompound().getCompoundTag("display").hasKey("color", 3)));
    }

    public int getColor(ItemStack stack)
    {
        if (this.material != ItemArmor.ArmorMaterial.LEATHER)
        {
            return -1;
        }
        else
        {
            NBTTagCompound stackTag = stack.getTagCompound();

            if (stackTag != null)
            {
                NBTTagCompound displayTag = stackTag.getCompoundTag("display");

                if (displayTag != null && displayTag.hasKey("color", 3))
                {
                    return displayTag.getInteger("color");
                }
            }

            return 10511680;
        }
    }

    public void removeColor(ItemStack stack)
    {
        if (this.material == ItemArmor.ArmorMaterial.LEATHER)
        {
            NBTTagCompound stackTag = stack.getTagCompound();

            if (stackTag != null)
            {
                NBTTagCompound displayTag = stackTag.getCompoundTag("display");

                if (displayTag.hasKey("color"))
                {
                    displayTag.removeTag("color");
                }
            }
        }
    }

    public void setColor(ItemStack stack, int color)
    {
        if (this.material != ItemArmor.ArmorMaterial.LEATHER)
        {
            throw new UnsupportedOperationException("Can\'t dye non-leather!");
        }
        else
        {
            NBTTagCompound stackTag = stack.getTagCompound();

            if (stackTag == null)
            {
                stackTag = new NBTTagCompound();
                stack.setTagCompound(stackTag);
            }

            NBTTagCompound displayTag = stackTag.getCompoundTag("display");

            if (!stackTag.hasKey("display", 10))
            {
                stackTag.setTag("display", displayTag);
            }

            displayTag.setInteger("color", color);
        }
    }

    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        return this.material.getRepairItem() == repair.getItem() ? true : super.getIsRepairable(toRepair, repair);
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        int armorSlot = EntityLiving.getArmorPosition(itemStackIn) - 1;
        ItemStack itemStack = playerIn.getCurrentArmor(armorSlot);

        if (itemStack == null)
        {
            playerIn.setCurrentItemOrArmor(armorSlot, itemStackIn.copy());
            itemStackIn.stackSize = 0;
        }

        return itemStackIn;
    }

    public static enum ArmorMaterial
    {
        LEATHER("leather", 5, new int[]{1, 3, 2, 1}, 15),
        CHAIN("chainmail", 15, new int[]{2, 5, 4, 1}, 12),
        IRON("iron", 15, new int[]{2, 6, 5, 2}, 9),
        GOLD("gold", 7, new int[]{2, 5, 3, 1}, 25),
        DIAMOND("diamond", 33, new int[]{3, 8, 6, 3}, 10);

        private final String name;
        private final int maxDamageFactor;
        private final int[] damageReductionAmountArray;
        private final int enchantability;

        private ArmorMaterial(String name, int maxDamage, int[] reductionAmounts, int enchantability)
        {
            this.name = name;
            this.maxDamageFactor = maxDamage;
            this.damageReductionAmountArray = reductionAmounts;
            this.enchantability = enchantability;
        }

        public int getDurability(int armorType)
        {
            return ItemArmor.maxDamageArray[armorType] * this.maxDamageFactor;
        }

        public int getDamageReductionAmount(int armorType)
        {
            return this.damageReductionAmountArray[armorType];
        }

        public int getEnchantability()
        {
            return this.enchantability;
        }

        public Item getRepairItem()
        {
            return this == LEATHER ? Items.leather : (this == CHAIN ? Items.iron_ingot : (this == GOLD ? Items.gold_ingot : (this == IRON ? Items.iron_ingot : (this == DIAMOND ? Items.diamond : null))));
        }

        public String getName()
        {
            return this.name;
        }
    }
}
