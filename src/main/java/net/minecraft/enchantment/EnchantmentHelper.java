package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.WeightedRandom;

public class EnchantmentHelper
{
    private static final Random enchantmentRand = new Random();
    private static final EnchantmentHelper.ModifierDamage enchantmentModifierDamage = new EnchantmentHelper.ModifierDamage();
    private static final EnchantmentHelper.ModifierLiving enchantmentModifierLiving = new EnchantmentHelper.ModifierLiving();
    private static final EnchantmentHelper.HurtIterator ENCHANTMENT_ITERATOR_HURT = new EnchantmentHelper.HurtIterator();
    private static final EnchantmentHelper.DamageIterator ENCHANTMENT_ITERATOR_DAMAGE = new EnchantmentHelper.DamageIterator();

    public static int getEnchantmentLevel(int enchID, ItemStack stack)
    {
        if (stack == null)
        {
            return 0;
        }
        else
        {
            NBTTagList nBTTagList = stack.getEnchantmentTagList();

            if (nBTTagList == null)
            {
                return 0;
            }
            else
            {
                for (int i = 0; i < nBTTagList.tagCount(); ++i)
                {
                    int j = nBTTagList.getCompoundTagAt(i).getShort("id");
                    int k = nBTTagList.getCompoundTagAt(i).getShort("lvl");

                    if (j == enchID)
                    {
                        return k;
                    }
                }

                return 0;
            }
        }
    }

    public static Map<Integer, Integer> getEnchantments(ItemStack stack)
    {
        Map<Integer, Integer> map = Maps.<Integer, Integer>newLinkedHashMap();
        NBTTagList nBTTagList = stack.getItem() == Items.enchanted_book ? Items.enchanted_book.getEnchantments(stack) : stack.getEnchantmentTagList();

        if (nBTTagList != null)
        {
            for (int i = 0; i < nBTTagList.tagCount(); ++i)
            {
                int j = nBTTagList.getCompoundTagAt(i).getShort("id");
                int k = nBTTagList.getCompoundTagAt(i).getShort("lvl");
                map.put(Integer.valueOf(j), Integer.valueOf(k));
            }
        }

        return map;
    }

    public static void setEnchantments(Map<Integer, Integer> enchMap, ItemStack stack)
    {
        NBTTagList nBTTagList = new NBTTagList();
        for (Integer enchantmentId : enchMap.keySet())
        {
            int i = enchantmentId.intValue();
            Enchantment enchantment = Enchantment.getEnchantmentById(i);

            if (enchantment != null)
            {
                int level = enchMap.get(enchantmentId).intValue();
                NBTTagCompound nBTTagCompound = new NBTTagCompound();
                nBTTagCompound.setShort("id", (short)i);
                nBTTagCompound.setShort("lvl", (short)level);
                nBTTagList.appendTag(nBTTagCompound);

                if (stack.getItem() == Items.enchanted_book)
                {
                    Items.enchanted_book.addEnchantment(stack, new EnchantmentData(enchantment, level));
                }
            }
        }

        if (nBTTagList.tagCount() > 0)
        {
            if (stack.getItem() != Items.enchanted_book)
            {
                stack.setTagInfo("ench", nBTTagList);
            }
        }
        else if (stack.hasTagCompound())
        {
            stack.getTagCompound().removeTag("ench");
        }
    }

    public static int getMaxEnchantmentLevel(int enchID, ItemStack[] stacks)
    {
        if (stacks == null)
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (ItemStack itemStack : stacks)
            {
                int j = getEnchantmentLevel(enchID, itemStack);

                if (j > i)
                {
                    i = j;
                }
            }

            return i;
        }
    }

    private static void applyEnchantmentModifier(EnchantmentHelper.IModifier modifier, ItemStack stack)
    {
        if (stack != null)
        {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            if (nbttaglist != null)
            {
                for (int i = 0; i < nbttaglist.tagCount(); ++i)
                {
                    int j = nbttaglist.getCompoundTagAt(i).getShort("id");
                    int k = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                    if (Enchantment.getEnchantmentById(j) != null)
                    {
                        modifier.calculateModifier(Enchantment.getEnchantmentById(j), k);
                    }
                }
            }
        }
    }

    private static void applyEnchantmentModifierArray(EnchantmentHelper.IModifier modifier, ItemStack[] stacks)
    {
        for (ItemStack itemStack : stacks)
        {
            applyEnchantmentModifier(modifier, itemStack);
        }
    }

    public static int getEnchantmentModifierDamage(ItemStack[] stacks, DamageSource source)
    {
        enchantmentModifierDamage.damageModifier = 0;
        enchantmentModifierDamage.source = source;
        applyEnchantmentModifierArray(enchantmentModifierDamage, stacks);

        if (enchantmentModifierDamage.damageModifier > 25)
        {
            enchantmentModifierDamage.damageModifier = 25;
        }
        else if (enchantmentModifierDamage.damageModifier < 0)
        {
            enchantmentModifierDamage.damageModifier = 0;
        }

        return (enchantmentModifierDamage.damageModifier + 1 >> 1) + enchantmentRand.nextInt((enchantmentModifierDamage.damageModifier >> 1) + 1);
    }

    public static float getModifierForCreature(ItemStack stack, EnumCreatureAttribute creatureAttribute)
    {
        enchantmentModifierLiving.livingModifier = 0.0F;
        enchantmentModifierLiving.entityLiving = creatureAttribute;
        applyEnchantmentModifier(enchantmentModifierLiving, stack);
        return enchantmentModifierLiving.livingModifier;
    }

    public static void applyThornEnchantments(EntityLivingBase user, Entity attacker)
    {
        ENCHANTMENT_ITERATOR_HURT.attacker = attacker;
        ENCHANTMENT_ITERATOR_HURT.user = user;

        if (user != null)
        {
            applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_HURT, user.getInventory());
        }

        if (attacker instanceof EntityPlayer)
        {
            applyEnchantmentModifier(ENCHANTMENT_ITERATOR_HURT, user.getHeldItem());
        }
    }

    public static void applyArthropodEnchantments(EntityLivingBase user, Entity target)
    {
        ENCHANTMENT_ITERATOR_DAMAGE.user = user;
        ENCHANTMENT_ITERATOR_DAMAGE.target = target;

        if (user != null)
        {
            applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_DAMAGE, user.getInventory());
        }

        if (user instanceof EntityPlayer)
        {
            applyEnchantmentModifier(ENCHANTMENT_ITERATOR_DAMAGE, user.getHeldItem());
        }
    }

    public static int getKnockbackModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.knockback.effectId, player.getHeldItem());
    }

    public static int getFireAspectModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.fireAspect.effectId, player.getHeldItem());
    }

    public static int getRespiration(Entity player)
    {
        return getMaxEnchantmentLevel(Enchantment.respiration.effectId, player.getInventory());
    }

    public static int getDepthStriderModifier(Entity player)
    {
        return getMaxEnchantmentLevel(Enchantment.depthStrider.effectId, player.getInventory());
    }

    public static int getEfficiencyModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.efficiency.effectId, player.getHeldItem());
    }

    public static boolean getSilkTouchModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.silkTouch.effectId, player.getHeldItem()) > 0;
    }

    public static int getFortuneModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.fortune.effectId, player.getHeldItem());
    }

    public static int getLuckOfSeaModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.luckOfTheSea.effectId, player.getHeldItem());
    }

    public static int getLureModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.lure.effectId, player.getHeldItem());
    }

    public static int getLootingModifier(EntityLivingBase player)
    {
        return getEnchantmentLevel(Enchantment.looting.effectId, player.getHeldItem());
    }

    public static boolean getAquaAffinityModifier(EntityLivingBase player)
    {
        return getMaxEnchantmentLevel(Enchantment.aquaAffinity.effectId, player.getInventory()) > 0;
    }

    public static ItemStack getEnchantedItem(Enchantment enchantment, EntityLivingBase entityLivingBaseIn)
    {
        for (ItemStack itemstack : entityLivingBaseIn.getInventory())
        {
            if (itemstack != null && getEnchantmentLevel(enchantment.effectId, itemstack) > 0)
            {
                return itemstack;
            }
        }

        return null;
    }

    public static int calcItemStackEnchantability(Random rand, int enchantNum, int power, ItemStack stack)
    {
        Item item = stack.getItem();
        int i = item.getItemEnchantability();

        if (i <= 0)
        {
            return 0;
        }
        else
        {
            if (power > 15)
            {
                power = 15;
            }

            int j = rand.nextInt(8) + 1 + (power >> 1) + rand.nextInt(power + 1);
            return enchantNum == 0 ? Math.max(j / 3, 1) : (enchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, power * 2));
        }
    }

    public static ItemStack addRandomEnchantment(Random random, ItemStack stack, int enchantability)
    {
        List<EnchantmentData> list = buildEnchantmentList(random, stack, enchantability);
        boolean flag = stack.getItem() == Items.book;

        if (flag)
        {
            stack.setItem(Items.enchanted_book);
        }

        if (list != null)
        {
            for (EnchantmentData enchantmentData : list)
            {
                if (flag)
                {
                    Items.enchanted_book.addEnchantment(stack, enchantmentData);
                }
                else
                {
                    stack.addEnchantment(enchantmentData.enchantmentobj, enchantmentData.enchantmentLevel);
                }
            }
        }

        return stack;
    }

    public static List<EnchantmentData> buildEnchantmentList(Random randomIn, ItemStack itemStackIn, int enchantability)
    {
        Item item = itemStackIn.getItem();
        int i = item.getItemEnchantability();

        if (i <= 0)
        {
            return null;
        }
        else
        {
            i = i / 2;
            i = 1 + randomIn.nextInt((i >> 1) + 1) + randomIn.nextInt((i >> 1) + 1);
            int j = i + enchantability;
            float f = (randomIn.nextFloat() + randomIn.nextFloat() - 1.0F) * 0.15F;
            int k = (int)((float)j * (1.0F + f) + 0.5F);

            if (k < 1)
            {
                k = 1;
            }

            List<EnchantmentData> list = null;
            Map<Integer, EnchantmentData> map = mapEnchantmentData(k, itemStackIn);

            if (map != null && !map.isEmpty())
            {
                EnchantmentData enchantmentData = (EnchantmentData)WeightedRandom.getRandomItem(randomIn, map.values());

                if (enchantmentData != null)
                {
                    list = Lists.<EnchantmentData>newArrayList();
                    list.add(enchantmentData);

                    for (int l = k; randomIn.nextInt(50) <= l; l >>= 1)
                    {
                        Iterator<Integer> iterator = map.keySet().iterator();

                        while (iterator.hasNext())
                        {
                            Integer integer = (Integer)iterator.next();
                            boolean flag = true;

                            for (EnchantmentData enchantmentdata1 : list)
                            {
                                if (!enchantmentdata1.enchantmentobj.canApplyTogether(Enchantment.getEnchantmentById(integer.intValue())))
                                {
                                    flag = false;
                                    break;
                                }
                            }

                            if (!flag)
                            {
                                iterator.remove();
                            }
                        }

                        if (!map.isEmpty())
                        {
                            EnchantmentData enchantmentdata2 = (EnchantmentData)WeightedRandom.getRandomItem(randomIn, map.values());
                            list.add(enchantmentdata2);
                        }
                    }
                }
            }

            return list;
        }
    }

    public static Map<Integer, EnchantmentData> mapEnchantmentData(int enchantability, ItemStack stack)
    {
        Item item = stack.getItem();
        Map<Integer, EnchantmentData> map = null;
        boolean flag = stack.getItem() == Items.book;

        for (Enchantment enchantment : Enchantment.enchantmentsBookList)
        {
            if (enchantment != null && (enchantment.type.canEnchantItem(item) || flag))
            {
                for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i)
                {
                    if (enchantability >= enchantment.getMinEnchantability(i) && enchantability <= enchantment.getMaxEnchantability(i))
                    {
                        if (map == null)
                        {
                            map = Maps.<Integer, EnchantmentData>newHashMap();
                        }

                        map.put(Integer.valueOf(enchantment.effectId), new EnchantmentData(enchantment, i));
                    }
                }
            }
        }

        return map;
    }

    static final class DamageIterator implements EnchantmentHelper.IModifier
    {
        public EntityLivingBase user;
        public Entity target;

        private DamageIterator()
        {
        }

        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
        {
            enchantmentIn.onEntityDamaged(this.user, this.target, enchantmentLevel);
        }
    }

    static final class HurtIterator implements EnchantmentHelper.IModifier
    {
        public EntityLivingBase user;
        public Entity attacker;

        private HurtIterator()
        {
        }

        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
        {
            enchantmentIn.onUserHurt(this.user, this.attacker, enchantmentLevel);
        }
    }

    interface IModifier
    {
        void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel);
    }

    static final class ModifierDamage implements EnchantmentHelper.IModifier
    {
        public int damageModifier;
        public DamageSource source;

        private ModifierDamage()
        {
        }

        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
        {
            this.damageModifier += enchantmentIn.calcModifierDamage(enchantmentLevel, this.source);
        }
    }

    static final class ModifierLiving implements EnchantmentHelper.IModifier
    {
        public float livingModifier;
        public EnumCreatureAttribute entityLiving;

        private ModifierLiving()
        {
        }

        public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
        {
            this.livingModifier += enchantmentIn.calcDamageByCreature(enchantmentLevel, this.entityLiving);
        }
    }
}
