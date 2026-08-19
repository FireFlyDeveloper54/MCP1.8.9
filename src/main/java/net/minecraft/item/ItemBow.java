package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;

public class ItemBow extends Item
{
    public static final String[] bowPullIconNameArray = new String[] {"pulling_0", "pulling_1", "pulling_2"};

    public ItemBow()
    {
        this.maxStackSize = 1;
        this.setMaxDamage(384);
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityPlayer playerIn, int timeLeft)
    {
        boolean isCreativeOrInfinite = playerIn.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) > 0;

        if (isCreativeOrInfinite || playerIn.inventory.hasItem(Items.arrow))
        {
            int chargeTicks = this.getMaxItemUseDuration(stack) - timeLeft;
            float arrowVelocity = (float)chargeTicks / 20.0F;
            arrowVelocity = (arrowVelocity * arrowVelocity + arrowVelocity * 2.0F) / 3.0F;

            if ((double)arrowVelocity < 0.1D)
            {
                return;
            }

            if (arrowVelocity > 1.0F)
            {
                arrowVelocity = 1.0F;
            }

            EntityArrow arrow = new EntityArrow(worldIn, playerIn, arrowVelocity * 2.0F);

            if (arrowVelocity == 1.0F)
            {
                arrow.setIsCritical(true);
            }

            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);

            if (powerLevel > 0)
            {
                arrow.setDamage(arrow.getDamage() + (double)powerLevel * 0.5D + 0.5D);
            }

            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);

            if (punchLevel > 0)
            {
                arrow.setKnockbackStrength(punchLevel);
            }

            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0)
            {
                arrow.setFire(100);
            }

            stack.damageItem(1, playerIn);
            worldIn.playSoundAtEntity(playerIn, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + arrowVelocity * 0.5F);

            if (isCreativeOrInfinite)
            {
                arrow.canBePickedUp = 2;
            }
            else
            {
                playerIn.inventory.consumeInventoryItem(Items.arrow);
            }

            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);

            if (!worldIn.isRemote)
            {
                worldIn.spawnEntityInWorld(arrow);
            }
        }
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityPlayer playerIn)
    {
        return stack;
    }

    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BOW;
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        if (playerIn.capabilities.isCreativeMode || playerIn.inventory.hasItem(Items.arrow))
        {
            playerIn.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
        }

        return itemStackIn;
    }

    public int getItemEnchantability()
    {
        return 1;
    }
}
