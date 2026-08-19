package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemPotion extends Item
{
    private Map<Integer, List<PotionEffect>> effectCache = Maps.<Integer, List<PotionEffect>>newHashMap();
    private static final Map<List<PotionEffect>, Integer> SUB_ITEMS_CACHE = Maps.<List<PotionEffect>, Integer>newLinkedHashMap();

    public ItemPotion()
    {
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.tabBrewing);
    }

    public List<PotionEffect> getEffects(ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("CustomPotionEffects", 9))
        {
            List<PotionEffect> customEffects = Lists.<PotionEffect>newArrayList();
            NBTTagList effectTagList = stack.getTagCompound().getTagList("CustomPotionEffects", 10);

            for (int effectIndex = 0; effectIndex < effectTagList.tagCount(); ++effectIndex)
            {
                NBTTagCompound effectTag = effectTagList.getCompoundTagAt(effectIndex);
                PotionEffect potionEffect = PotionEffect.readCustomPotionEffectFromNBT(effectTag);

                if (potionEffect != null)
                {
                    customEffects.add(potionEffect);
                }
            }

            return customEffects;
        }
        else
        {
            List<PotionEffect> cachedEffects = this.effectCache.get(Integer.valueOf(stack.getMetadata()));

            if (cachedEffects == null)
            {
                cachedEffects = PotionHelper.getPotionEffects(stack.getMetadata(), false);
                this.effectCache.put(Integer.valueOf(stack.getMetadata()), cachedEffects);
            }

            return cachedEffects;
        }
    }

    public List<PotionEffect> getEffects(int meta)
    {
        List<PotionEffect> cachedEffects = this.effectCache.get(Integer.valueOf(meta));

        if (cachedEffects == null)
        {
            cachedEffects = PotionHelper.getPotionEffects(meta, false);
            this.effectCache.put(Integer.valueOf(meta), cachedEffects);
        }

        return cachedEffects;
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityPlayer playerIn)
    {
        if (!playerIn.capabilities.isCreativeMode)
        {
            --stack.stackSize;
        }

        if (!worldIn.isRemote)
        {
            List<PotionEffect> effects = this.getEffects(stack);

            if (effects != null)
            {
                for (PotionEffect potionEffect : effects)
                {
                    playerIn.addPotionEffect(new PotionEffect(potionEffect));
                }
            }
        }

        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);

        if (!playerIn.capabilities.isCreativeMode)
        {
            if (stack.stackSize <= 0)
            {
                return new ItemStack(Items.glass_bottle);
            }

            playerIn.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
        }

        return stack;
    }

    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 32;
    }

    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.DRINK;
    }

    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
    {
        if (isSplash(itemStackIn.getMetadata()))
        {
            if (!playerIn.capabilities.isCreativeMode)
            {
                --itemStackIn.stackSize;
            }

            worldIn.playSoundAtEntity(playerIn, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!worldIn.isRemote)
            {
                worldIn.spawnEntityInWorld(new EntityPotion(worldIn, playerIn, itemStackIn));
            }

            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
            return itemStackIn;
        }
        else
        {
            playerIn.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
            return itemStackIn;
        }
    }

    public static boolean isSplash(int meta)
    {
        return (meta & 16384) != 0;
    }

    public int getColorFromDamage(int meta)
    {
        return PotionHelper.getLiquidColor(meta, false);
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        return renderPass > 0 ? 16777215 : this.getColorFromDamage(stack.getMetadata());
    }

    public boolean isEffectInstant(int meta)
    {
        List<PotionEffect> effects = this.getEffects(meta);

        if (effects != null && !effects.isEmpty())
        {
            for (PotionEffect potionEffect : effects)
            {
                if (Potion.potionTypes[potionEffect.getPotionID()].isInstant())
                {
                    return true;
                }
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        if (stack.getMetadata() == 0)
        {
            return StatCollector.translateToLocal("item.emptyPotion.name").trim();
        }
        else
        {
            String namePrefix = "";

            if (isSplash(stack.getMetadata()))
            {
                namePrefix = StatCollector.translateToLocal("potion.prefix.grenade").trim() + " ";
            }

            List<PotionEffect> effects = Items.potionitem.getEffects(stack);

            if (effects != null && !effects.isEmpty())
            {
                String effectName = effects.get(0).getEffectName();
                effectName = effectName + ".postfix";
                return namePrefix + StatCollector.translateToLocal(effectName).trim();
            }
            else
            {
                String potionPrefix = PotionHelper.getPotionPrefix(stack.getMetadata());
                return StatCollector.translateToLocal(potionPrefix).trim() + " " + super.getItemStackDisplayName(stack);
            }
        }
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if (stack.getMetadata() != 0)
        {
            List<PotionEffect> effects = Items.potionitem.getEffects(stack);
            Multimap<String, AttributeModifier> attributeModifiers = HashMultimap.<String, AttributeModifier>create();

            if (effects != null && !effects.isEmpty())
            {
                for (PotionEffect potionEffect : effects)
                {
                    String effectText = StatCollector.translateToLocal(potionEffect.getEffectName()).trim();
                    Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                    Map<IAttribute, AttributeModifier> attributeModifierMap = potion.getAttributeModifierMap();

                    if (attributeModifierMap != null && attributeModifierMap.size() > 0)
                    {
                        for (Entry<IAttribute, AttributeModifier> entry : attributeModifierMap.entrySet())
                        {
                            AttributeModifier attributeModifier = (AttributeModifier)entry.getValue();
                            AttributeModifier adjustedModifier = new AttributeModifier(attributeModifier.getName(), potion.getAttributeModifierAmount(potionEffect.getAmplifier(), attributeModifier), attributeModifier.getOperation());
                            attributeModifiers.put(((IAttribute)entry.getKey()).getAttributeUnlocalizedName(), adjustedModifier);
                        }
                    }

                    if (potionEffect.getAmplifier() > 0)
                    {
                        effectText = effectText + " " + StatCollector.translateToLocal("potion.potency." + potionEffect.getAmplifier()).trim();
                    }

                    if (potionEffect.getDuration() > 20)
                    {
                        effectText = effectText + " (" + Potion.getDurationString(potionEffect) + ")";
                    }

                    if (potion.isBadEffect())
                    {
                        tooltip.add(EnumChatFormatting.RED + effectText);
                    }
                    else
                    {
                        tooltip.add(EnumChatFormatting.GRAY + effectText);
                    }
                }
            }
            else
            {
                String emptyText = StatCollector.translateToLocal("potion.empty").trim();
                tooltip.add(EnumChatFormatting.GRAY + emptyText);
            }

            if (!attributeModifiers.isEmpty())
            {
                tooltip.add("");
                tooltip.add(EnumChatFormatting.DARK_PURPLE + StatCollector.translateToLocal("potion.effects.whenDrank"));

                for (Entry<String, AttributeModifier> attributeEntry : attributeModifiers.entries())
                {
                    AttributeModifier attributeModifier = (AttributeModifier)attributeEntry.getValue();
                    double amount = attributeModifier.getAmount();
                    double formattedAmount;

                    if (attributeModifier.getOperation() != 1 && attributeModifier.getOperation() != 2)
                    {
                        formattedAmount = attributeModifier.getAmount();
                    }
                    else
                    {
                        formattedAmount = attributeModifier.getAmount() * 100.0D;
                    }

                    if (amount > 0.0D)
                    {
                        tooltip.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("attribute.modifier.plus." + attributeModifier.getOperation(), new Object[] {ItemStack.DECIMALFORMAT.format(formattedAmount), StatCollector.translateToLocal("attribute.name." + (String)attributeEntry.getKey())}));
                    }
                    else if (amount < 0.0D)
                    {
                        formattedAmount = formattedAmount * -1.0D;
                        tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("attribute.modifier.take." + attributeModifier.getOperation(), new Object[] {ItemStack.DECIMALFORMAT.format(formattedAmount), StatCollector.translateToLocal("attribute.name." + (String)attributeEntry.getKey())}));
                    }
                }
            }
        }
    }

    public boolean hasEffect(ItemStack stack)
    {
        List<PotionEffect> effects = this.getEffects(stack);
        return effects != null && !effects.isEmpty();
    }

    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        super.getSubItems(itemIn, tab, subItems);

        if (SUB_ITEMS_CACHE.isEmpty())
        {
            for (int basePotionId = 0; basePotionId <= 15; ++basePotionId)
            {
                for (int potionTypeIndex = 0; potionTypeIndex <= 1; ++potionTypeIndex)
                {
                    int potionMetadata;

                    if (potionTypeIndex == 0)
                    {
                        potionMetadata = basePotionId | 8192;
                    }
                    else
                    {
                        potionMetadata = basePotionId | 16384;
                    }

                    for (int modifierIndex = 0; modifierIndex <= 2; ++modifierIndex)
                    {
                        int modifiedMetadata = potionMetadata;

                        if (modifierIndex != 0)
                        {
                            if (modifierIndex == 1)
                            {
                                modifiedMetadata = potionMetadata | 32;
                            }
                            else if (modifierIndex == 2)
                            {
                                modifiedMetadata = potionMetadata | 64;
                            }
                        }

                        List<PotionEffect> effects = PotionHelper.getPotionEffects(modifiedMetadata, false);

                        if (effects != null && !effects.isEmpty())
                        {
                            SUB_ITEMS_CACHE.put(effects, Integer.valueOf(modifiedMetadata));
                        }
                    }
                }
            }
        }

        Iterator<Integer> metadataIterator = SUB_ITEMS_CACHE.values().iterator();

        while (metadataIterator.hasNext())
        {
            int potionMetadata = metadataIterator.next();
            subItems.add(new ItemStack(itemIn, 1, potionMetadata));
        }
    }
}
