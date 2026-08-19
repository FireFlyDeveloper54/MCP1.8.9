package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;

public class StatList
{
    protected static Map<String, StatBase> oneShotStats = Maps.<String, StatBase>newHashMap();
    public static List<StatBase> allStats = Lists.<StatBase>newArrayList();
    public static List<StatBase> generalStats = Lists.<StatBase>newArrayList();
    public static List<StatCrafting> itemStats = Lists.<StatCrafting>newArrayList();
    public static List<StatCrafting> objectMineStats = Lists.<StatCrafting>newArrayList();
    public static StatBase leaveGameStat = (new StatBasic("stat.leaveGame", new ChatComponentTranslation("stat.leaveGame", new Object[0]))).initIndependentStat().registerStat();
    public static StatBase minutesPlayedStat = (new StatBasic("stat.playOneMinute", new ChatComponentTranslation("stat.playOneMinute", new Object[0]), StatBase.timeStatType)).initIndependentStat().registerStat();
    public static StatBase timeSinceDeathStat = (new StatBasic("stat.timeSinceDeath", new ChatComponentTranslation("stat.timeSinceDeath", new Object[0]), StatBase.timeStatType)).initIndependentStat().registerStat();
    public static StatBase distanceWalkedStat = (new StatBasic("stat.walkOneCm", new ChatComponentTranslation("stat.walkOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceCrouchedStat = (new StatBasic("stat.crouchOneCm", new ChatComponentTranslation("stat.crouchOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceSprintedStat = (new StatBasic("stat.sprintOneCm", new ChatComponentTranslation("stat.sprintOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceSwumStat = (new StatBasic("stat.swimOneCm", new ChatComponentTranslation("stat.swimOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceFallenStat = (new StatBasic("stat.fallOneCm", new ChatComponentTranslation("stat.fallOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceClimbedStat = (new StatBasic("stat.climbOneCm", new ChatComponentTranslation("stat.climbOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceFlownStat = (new StatBasic("stat.flyOneCm", new ChatComponentTranslation("stat.flyOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceDoveStat = (new StatBasic("stat.diveOneCm", new ChatComponentTranslation("stat.diveOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceByMinecartStat = (new StatBasic("stat.minecartOneCm", new ChatComponentTranslation("stat.minecartOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceByBoatStat = (new StatBasic("stat.boatOneCm", new ChatComponentTranslation("stat.boatOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceByPigStat = (new StatBasic("stat.pigOneCm", new ChatComponentTranslation("stat.pigOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase distanceByHorseStat = (new StatBasic("stat.horseOneCm", new ChatComponentTranslation("stat.horseOneCm", new Object[0]), StatBase.distanceStatType)).initIndependentStat().registerStat();
    public static StatBase jumpStat = (new StatBasic("stat.jump", new ChatComponentTranslation("stat.jump", new Object[0]))).initIndependentStat().registerStat();
    public static StatBase dropStat = (new StatBasic("stat.drop", new ChatComponentTranslation("stat.drop", new Object[0]))).initIndependentStat().registerStat();
    public static StatBase damageDealtStat = (new StatBasic("stat.damageDealt", new ChatComponentTranslation("stat.damageDealt", new Object[0]), StatBase.divideByTen)).registerStat();
    public static StatBase damageTakenStat = (new StatBasic("stat.damageTaken", new ChatComponentTranslation("stat.damageTaken", new Object[0]), StatBase.divideByTen)).registerStat();
    public static StatBase deathsStat = (new StatBasic("stat.deaths", new ChatComponentTranslation("stat.deaths", new Object[0]))).registerStat();
    public static StatBase mobKillsStat = (new StatBasic("stat.mobKills", new ChatComponentTranslation("stat.mobKills", new Object[0]))).registerStat();
    public static StatBase animalsBredStat = (new StatBasic("stat.animalsBred", new ChatComponentTranslation("stat.animalsBred", new Object[0]))).registerStat();
    public static StatBase playerKillsStat = (new StatBasic("stat.playerKills", new ChatComponentTranslation("stat.playerKills", new Object[0]))).registerStat();
    public static StatBase fishCaughtStat = (new StatBasic("stat.fishCaught", new ChatComponentTranslation("stat.fishCaught", new Object[0]))).registerStat();
    public static StatBase junkFishedStat = (new StatBasic("stat.junkFished", new ChatComponentTranslation("stat.junkFished", new Object[0]))).registerStat();
    public static StatBase treasureFishedStat = (new StatBasic("stat.treasureFished", new ChatComponentTranslation("stat.treasureFished", new Object[0]))).registerStat();
    public static StatBase timesTalkedToVillagerStat = (new StatBasic("stat.talkedToVillager", new ChatComponentTranslation("stat.talkedToVillager", new Object[0]))).registerStat();
    public static StatBase timesTradedWithVillagerStat = (new StatBasic("stat.tradedWithVillager", new ChatComponentTranslation("stat.tradedWithVillager", new Object[0]))).registerStat();
    public static StatBase cakeSlicesEatenStat = (new StatBasic("stat.cakeSlicesEaten", new ChatComponentTranslation("stat.cakeSlicesEaten", new Object[0]))).registerStat();
    public static StatBase cauldronFilledStat = (new StatBasic("stat.cauldronFilled", new ChatComponentTranslation("stat.cauldronFilled", new Object[0]))).registerStat();
    public static StatBase cauldronUsedStat = (new StatBasic("stat.cauldronUsed", new ChatComponentTranslation("stat.cauldronUsed", new Object[0]))).registerStat();
    public static StatBase armorCleanedStat = (new StatBasic("stat.armorCleaned", new ChatComponentTranslation("stat.armorCleaned", new Object[0]))).registerStat();
    public static StatBase bannerCleanedStat = (new StatBasic("stat.bannerCleaned", new ChatComponentTranslation("stat.bannerCleaned", new Object[0]))).registerStat();
    public static StatBase brewingStandInteractionStat = (new StatBasic("stat.brewingstandInteraction", new ChatComponentTranslation("stat.brewingstandInteraction", new Object[0]))).registerStat();
    public static StatBase beaconInteractionStat = (new StatBasic("stat.beaconInteraction", new ChatComponentTranslation("stat.beaconInteraction", new Object[0]))).registerStat();
    public static StatBase dropperInspectedStat = (new StatBasic("stat.dropperInspected", new ChatComponentTranslation("stat.dropperInspected", new Object[0]))).registerStat();
    public static StatBase hopperInspectedStat = (new StatBasic("stat.hopperInspected", new ChatComponentTranslation("stat.hopperInspected", new Object[0]))).registerStat();
    public static StatBase dispenserInspectedStat = (new StatBasic("stat.dispenserInspected", new ChatComponentTranslation("stat.dispenserInspected", new Object[0]))).registerStat();
    public static StatBase noteBlockPlayedStat = (new StatBasic("stat.noteblockPlayed", new ChatComponentTranslation("stat.noteblockPlayed", new Object[0]))).registerStat();
    public static StatBase noteBlockTunedStat = (new StatBasic("stat.noteblockTuned", new ChatComponentTranslation("stat.noteblockTuned", new Object[0]))).registerStat();
    public static StatBase flowerPottedStat = (new StatBasic("stat.flowerPotted", new ChatComponentTranslation("stat.flowerPotted", new Object[0]))).registerStat();
    public static StatBase trappedChestTriggeredStat = (new StatBasic("stat.trappedChestTriggered", new ChatComponentTranslation("stat.trappedChestTriggered", new Object[0]))).registerStat();
    public static StatBase enderChestOpenedStat = (new StatBasic("stat.enderchestOpened", new ChatComponentTranslation("stat.enderchestOpened", new Object[0]))).registerStat();
    public static StatBase itemEnchantedStat = (new StatBasic("stat.itemEnchanted", new ChatComponentTranslation("stat.itemEnchanted", new Object[0]))).registerStat();
    public static StatBase recordPlayedStat = (new StatBasic("stat.recordPlayed", new ChatComponentTranslation("stat.recordPlayed", new Object[0]))).registerStat();
    public static StatBase furnaceInteractionStat = (new StatBasic("stat.furnaceInteraction", new ChatComponentTranslation("stat.furnaceInteraction", new Object[0]))).registerStat();
    public static StatBase craftingTableInteractionStat = (new StatBasic("stat.craftingTableInteraction", new ChatComponentTranslation("stat.workbenchInteraction", new Object[0]))).registerStat();
    public static StatBase chestOpenedStat = (new StatBasic("stat.chestOpened", new ChatComponentTranslation("stat.chestOpened", new Object[0]))).registerStat();
    public static final StatBase[] mineBlockStatArray = new StatBase[4096];
    public static final StatBase[] objectCraftStats = new StatBase[32000];
    public static final StatBase[] objectUseStats = new StatBase[32000];
    public static final StatBase[] objectBreakStats = new StatBase[32000];

    public static void init()
    {
        initMiningStats();
        initStats();
        initItemDepleteStats();
        initCraftableStats();
        AchievementList.init();
        EntityList.initEntityStats();
    }

    private static void initCraftableStats()
    {
        Set<Item> set = Sets.<Item>newHashSet();

        for (IRecipe irecipe : CraftingManager.getInstance().getRecipeList())
        {
            if (irecipe.getRecipeOutput() != null)
            {
                set.add(irecipe.getRecipeOutput().getItem());
            }
        }

        for (ItemStack itemStack : FurnaceRecipes.instance().getSmeltingList().values())
        {
            set.add(itemStack.getItem());
        }

        for (Item item : set)
        {
            if (item != null)
            {
                int i = Item.getIdFromItem(item);
                String s = getItemName(item);

                if (s != null)
                {
                    objectCraftStats[i] = (new StatCrafting("stat.craftItem.", s, new ChatComponentTranslation("stat.craftItem", new Object[] {(new ItemStack(item)).getChatComponent()}), item)).registerStat();
                }
            }
        }

        replaceAllSimilarBlocks(objectCraftStats);
    }

    private static void initMiningStats()
    {
        for (Block block : Block.blockRegistry)
        {
            Item item = Item.getItemFromBlock(block);

            if (item != null)
            {
                int i = Block.getIdFromBlock(block);
                String s = getItemName(item);

                if (s != null && block.getEnableStats())
                {
                    mineBlockStatArray[i] = (new StatCrafting("stat.mineBlock.", s, new ChatComponentTranslation("stat.mineBlock", new Object[] {(new ItemStack(block)).getChatComponent()}), item)).registerStat();
                    objectMineStats.add((StatCrafting)mineBlockStatArray[i]);
                }
            }
        }

        replaceAllSimilarBlocks(mineBlockStatArray);
    }

    private static void initStats()
    {
        for (Item item : Item.itemRegistry)
        {
            if (item != null)
            {
                int i = Item.getIdFromItem(item);
                String s = getItemName(item);

                if (s != null)
                {
                    objectUseStats[i] = (new StatCrafting("stat.useItem.", s, new ChatComponentTranslation("stat.useItem", new Object[] {(new ItemStack(item)).getChatComponent()}), item)).registerStat();

                    if (!(item instanceof ItemBlock))
                    {
                        itemStats.add((StatCrafting)objectUseStats[i]);
                    }
                }
            }
        }

        replaceAllSimilarBlocks(objectUseStats);
    }

    private static void initItemDepleteStats()
    {
        for (Item item : Item.itemRegistry)
        {
            if (item != null)
            {
                int i = Item.getIdFromItem(item);
                String s = getItemName(item);

                if (s != null && item.isDamageable())
                {
                    objectBreakStats[i] = (new StatCrafting("stat.breakItem.", s, new ChatComponentTranslation("stat.breakItem", new Object[] {(new ItemStack(item)).getChatComponent()}), item)).registerStat();
                }
            }
        }

        replaceAllSimilarBlocks(objectBreakStats);
    }

    private static String getItemName(Item item)
    {
        ResourceLocation resourceLocation = (ResourceLocation)Item.itemRegistry.getNameForObject(item);
        return resourceLocation != null ? resourceLocation.toString().replace(':', '.') : null;
    }

    private static void replaceAllSimilarBlocks(StatBase[] statArray)
    {
        mergeStatBases(statArray, Blocks.water, Blocks.flowing_water);
        mergeStatBases(statArray, Blocks.lava, Blocks.flowing_lava);
        mergeStatBases(statArray, Blocks.lit_pumpkin, Blocks.pumpkin);
        mergeStatBases(statArray, Blocks.lit_furnace, Blocks.furnace);
        mergeStatBases(statArray, Blocks.lit_redstone_ore, Blocks.redstone_ore);
        mergeStatBases(statArray, Blocks.powered_repeater, Blocks.unpowered_repeater);
        mergeStatBases(statArray, Blocks.powered_comparator, Blocks.unpowered_comparator);
        mergeStatBases(statArray, Blocks.redstone_torch, Blocks.unlit_redstone_torch);
        mergeStatBases(statArray, Blocks.lit_redstone_lamp, Blocks.redstone_lamp);
        mergeStatBases(statArray, Blocks.double_stone_slab, Blocks.stone_slab);
        mergeStatBases(statArray, Blocks.double_wooden_slab, Blocks.wooden_slab);
        mergeStatBases(statArray, Blocks.double_stone_slab2, Blocks.stone_slab2);
        mergeStatBases(statArray, Blocks.grass, Blocks.dirt);
        mergeStatBases(statArray, Blocks.farmland, Blocks.dirt);
    }

    private static void mergeStatBases(StatBase[] statBaseIn, Block sourceBlock, Block targetBlock)
    {
        int i = Block.getIdFromBlock(sourceBlock);
        int j = Block.getIdFromBlock(targetBlock);

        if (statBaseIn[i] != null && statBaseIn[j] == null)
        {
            statBaseIn[j] = statBaseIn[i];
        }
        else
        {
            allStats.remove(statBaseIn[i]);
            objectMineStats.remove(statBaseIn[i]);
            generalStats.remove(statBaseIn[i]);
            statBaseIn[i] = statBaseIn[j];
        }
    }

    public static StatBase getStatKillEntity(EntityList.EntityEggInfo eggInfo)
    {
        String s = EntityList.getStringFromID(eggInfo.spawnedID);
        return s == null ? null : (new StatBase("stat.killEntity." + s, new ChatComponentTranslation("stat.entityKill", new Object[] {new ChatComponentTranslation("entity." + s + ".name", new Object[0])}))).registerStat();
    }

    public static StatBase getStatEntityKilledBy(EntityList.EntityEggInfo eggInfo)
    {
        String s = EntityList.getStringFromID(eggInfo.spawnedID);
        return s == null ? null : (new StatBase("stat.entityKilledBy." + s, new ChatComponentTranslation("stat.entityKilledBy", new Object[] {new ChatComponentTranslation("entity." + s + ".name", new Object[0])}))).registerStat();
    }

    public static StatBase getOneShotStat(String statId)
    {
        return (StatBase)oneShotStats.get(statId);
    }
}
