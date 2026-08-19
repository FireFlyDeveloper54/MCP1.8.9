package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockWall;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CraftingManager
{
    private static final CraftingManager instance = new CraftingManager();
    private final List<IRecipe> recipes = Lists.<IRecipe>newArrayList();

    public static CraftingManager getInstance()
    {
        return instance;
    }

    private CraftingManager()
    {
        (new RecipesTools()).addRecipes(this);
        (new RecipesWeapons()).addRecipes(this);
        (new RecipesIngots()).addRecipes(this);
        (new RecipesFood()).addRecipes(this);
        (new RecipesCrafting()).addRecipes(this);
        (new RecipesArmor()).addRecipes(this);
        (new RecipesDyes()).addRecipes(this);
        this.recipes.add(new RecipesArmorDyes());
        this.recipes.add(new RecipeBookCloning());
        this.recipes.add(new RecipesMapCloning());
        this.recipes.add(new RecipesMapExtending());
        this.recipes.add(new RecipeFireworks());
        this.recipes.add(new RecipeRepairItem());
        (new RecipesBanners()).addRecipes(this);
        this.addRecipe(new ItemStack(Items.paper, 3), new Object[] {"###", '#', Items.reeds});
        this.addShapelessRecipe(new ItemStack(Items.book, 1), new Object[] {Items.paper, Items.paper, Items.paper, Items.leather});
        this.addShapelessRecipe(new ItemStack(Items.writable_book, 1), new Object[] {Items.book, new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), Items.feather});
        this.addRecipe(new ItemStack(Blocks.oak_fence, 3), new Object[] {"W#W", "W#W", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.OAK.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.birch_fence, 3), new Object[] {"W#W", "W#W", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.BIRCH.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.spruce_fence, 3), new Object[] {"W#W", "W#W", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.jungle_fence, 3), new Object[] {"W#W", "W#W", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.acacia_fence, 3), new Object[] {"W#W", "W#W", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.dark_oak_fence, 3), new Object[] {"W#W", "W#W", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.cobblestone_wall, 6, BlockWall.EnumType.NORMAL.getMetadata()), new Object[] {"###", "###", '#', Blocks.cobblestone});
        this.addRecipe(new ItemStack(Blocks.cobblestone_wall, 6, BlockWall.EnumType.MOSSY.getMetadata()), new Object[] {"###", "###", '#', Blocks.mossy_cobblestone});
        this.addRecipe(new ItemStack(Blocks.nether_brick_fence, 6), new Object[] {"###", "###", '#', Blocks.nether_brick});
        this.addRecipe(new ItemStack(Blocks.oak_fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.OAK.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.birch_fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.BIRCH.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.spruce_fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.jungle_fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.acacia_fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.dark_oak_fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.jukebox, 1), new Object[] {"###", "#X#", "###", '#', Blocks.planks, 'X', Items.diamond});
        this.addRecipe(new ItemStack(Items.lead, 2), new Object[] {"~~ ", "~O ", "  ~", '~', Items.string, 'O', Items.slime_ball});
        this.addRecipe(new ItemStack(Blocks.noteblock, 1), new Object[] {"###", "#X#", "###", '#', Blocks.planks, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Blocks.bookshelf, 1), new Object[] {"###", "XXX", "###", '#', Blocks.planks, 'X', Items.book});
        this.addRecipe(new ItemStack(Blocks.snow, 1), new Object[] {"##", "##", '#', Items.snowball});
        this.addRecipe(new ItemStack(Blocks.snow_layer, 6), new Object[] {"###", '#', Blocks.snow});
        this.addRecipe(new ItemStack(Blocks.clay, 1), new Object[] {"##", "##", '#', Items.clay_ball});
        this.addRecipe(new ItemStack(Blocks.brick_block, 1), new Object[] {"##", "##", '#', Items.brick});
        this.addRecipe(new ItemStack(Blocks.glowstone, 1), new Object[] {"##", "##", '#', Items.glowstone_dust});
        this.addRecipe(new ItemStack(Blocks.quartz_block, 1), new Object[] {"##", "##", '#', Items.quartz});
        this.addRecipe(new ItemStack(Blocks.wool, 1), new Object[] {"##", "##", '#', Items.string});
        this.addRecipe(new ItemStack(Blocks.tnt, 1), new Object[] {"X#X", "#X#", "X#X", 'X', Items.gunpowder, '#', Blocks.sand});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.COBBLESTONE.getMetadata()), new Object[] {"###", '#', Blocks.cobblestone});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.STONE.getMetadata()), new Object[] {"###", '#', new ItemStack(Blocks.stone, BlockStone.EnumType.STONE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.SAND.getMetadata()), new Object[] {"###", '#', Blocks.sandstone});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.BRICK.getMetadata()), new Object[] {"###", '#', Blocks.brick_block});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), new Object[] {"###", '#', Blocks.stonebrick});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.NETHERBRICK.getMetadata()), new Object[] {"###", '#', Blocks.nether_brick});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, BlockStoneSlab.EnumType.QUARTZ.getMetadata()), new Object[] {"###", '#', Blocks.quartz_block});
        this.addRecipe(new ItemStack(Blocks.stone_slab2, 6, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata()), new Object[] {"###", '#', Blocks.red_sandstone});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 0), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.OAK.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, BlockPlanks.EnumType.BIRCH.getMetadata()), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.BIRCH.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, BlockPlanks.EnumType.SPRUCE.getMetadata()), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, BlockPlanks.EnumType.JUNGLE.getMetadata()), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.ladder, 3), new Object[] {"# #", "###", "# #", '#', Items.stick});
        this.addRecipe(new ItemStack(Items.oak_door, 3), new Object[] {"##", "##", "##", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.OAK.getMetadata())});
        this.addRecipe(new ItemStack(Items.spruce_door, 3), new Object[] {"##", "##", "##", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())});
        this.addRecipe(new ItemStack(Items.birch_door, 3), new Object[] {"##", "##", "##", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.BIRCH.getMetadata())});
        this.addRecipe(new ItemStack(Items.jungle_door, 3), new Object[] {"##", "##", "##", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())});
        this.addRecipe(new ItemStack(Items.acacia_door, 3), new Object[] {"##", "##", "##", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.ACACIA.getMetadata())});
        this.addRecipe(new ItemStack(Items.dark_oak_door, 3), new Object[] {"##", "##", "##", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.trapdoor, 2), new Object[] {"###", "###", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Items.iron_door, 3), new Object[] {"##", "##", "##", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Blocks.iron_trapdoor, 1), new Object[] {"##", "##", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.sign, 3), new Object[] {"###", "###", " X ", '#', Blocks.planks, 'X', Items.stick});
        this.addRecipe(new ItemStack(Items.cake, 1), new Object[] {"AAA", "BEB", "CCC", 'A', Items.milk_bucket, 'B', Items.sugar, 'C', Items.wheat, 'E', Items.egg});
        this.addRecipe(new ItemStack(Items.sugar, 1), new Object[] {"#", '#', Items.reeds});
        this.addRecipe(new ItemStack(Blocks.planks, 4, BlockPlanks.EnumType.OAK.getMetadata()), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, BlockPlanks.EnumType.OAK.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.planks, 4, BlockPlanks.EnumType.SPRUCE.getMetadata()), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.planks, 4, BlockPlanks.EnumType.BIRCH.getMetadata()), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, BlockPlanks.EnumType.BIRCH.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.planks, 4, BlockPlanks.EnumType.JUNGLE.getMetadata()), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4), new Object[] {"#", '#', new ItemStack(Blocks.log2, 1, BlockPlanks.EnumType.ACACIA.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4), new Object[] {"#", '#', new ItemStack(Blocks.log2, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Items.stick, 4), new Object[] {"#", "#", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.torch, 4), new Object[] {"X", "#", 'X', Items.coal, '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.torch, 4), new Object[] {"X", "#", 'X', new ItemStack(Items.coal, 1, 1), '#', Items.stick});
        this.addRecipe(new ItemStack(Items.bowl, 4), new Object[] {"# #", " # ", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Items.glass_bottle, 3), new Object[] {"# #", " # ", '#', Blocks.glass});
        this.addRecipe(new ItemStack(Blocks.rail, 16), new Object[] {"X X", "X#X", "X X", 'X', Items.iron_ingot, '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.golden_rail, 6), new Object[] {"X X", "X#X", "XRX", 'X', Items.gold_ingot, 'R', Items.redstone, '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.activator_rail, 6), new Object[] {"XSX", "X#X", "XSX", 'X', Items.iron_ingot, '#', Blocks.redstone_torch, 'S', Items.stick});
        this.addRecipe(new ItemStack(Blocks.detector_rail, 6), new Object[] {"X X", "X#X", "XRX", 'X', Items.iron_ingot, 'R', Items.redstone, '#', Blocks.stone_pressure_plate});
        this.addRecipe(new ItemStack(Items.minecart, 1), new Object[] {"# #", "###", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.cauldron, 1), new Object[] {"# #", "# #", "###", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.brewing_stand, 1), new Object[] {" B ", "###", '#', Blocks.cobblestone, 'B', Items.blaze_rod});
        this.addRecipe(new ItemStack(Blocks.lit_pumpkin, 1), new Object[] {"A", "B", 'A', Blocks.pumpkin, 'B', Blocks.torch});
        this.addRecipe(new ItemStack(Items.chest_minecart, 1), new Object[] {"A", "B", 'A', Blocks.chest, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.furnace_minecart, 1), new Object[] {"A", "B", 'A', Blocks.furnace, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.tnt_minecart, 1), new Object[] {"A", "B", 'A', Blocks.tnt, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.hopper_minecart, 1), new Object[] {"A", "B", 'A', Blocks.hopper, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.boat, 1), new Object[] {"# #", "###", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Items.bucket, 1), new Object[] {"# #", " # ", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.flower_pot, 1), new Object[] {"# #", " # ", '#', Items.brick});
        this.addShapelessRecipe(new ItemStack(Items.flint_and_steel, 1), new Object[] {new ItemStack(Items.iron_ingot, 1), new ItemStack(Items.flint, 1)});
        this.addRecipe(new ItemStack(Items.bread, 1), new Object[] {"###", '#', Items.wheat});
        this.addRecipe(new ItemStack(Blocks.oak_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.OAK.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.birch_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.BIRCH.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.spruce_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.jungle_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.acacia_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.ACACIA.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Blocks.dark_oak_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 4 + BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4)});
        this.addRecipe(new ItemStack(Items.fishing_rod, 1), new Object[] {"  #", " #X", "# X", '#', Items.stick, 'X', Items.string});
        this.addRecipe(new ItemStack(Items.carrot_on_a_stick, 1), new Object[] {"# ", " X", '#', Items.fishing_rod, 'X', Items.carrot});
        this.addRecipe(new ItemStack(Blocks.stone_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.cobblestone});
        this.addRecipe(new ItemStack(Blocks.brick_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.brick_block});
        this.addRecipe(new ItemStack(Blocks.stone_brick_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.stonebrick});
        this.addRecipe(new ItemStack(Blocks.nether_brick_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.nether_brick});
        this.addRecipe(new ItemStack(Blocks.sandstone_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.sandstone});
        this.addRecipe(new ItemStack(Blocks.red_sandstone_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.red_sandstone});
        this.addRecipe(new ItemStack(Blocks.quartz_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.quartz_block});
        this.addRecipe(new ItemStack(Items.painting, 1), new Object[] {"###", "#X#", "###", '#', Items.stick, 'X', Blocks.wool});
        this.addRecipe(new ItemStack(Items.item_frame, 1), new Object[] {"###", "#X#", "###", '#', Items.stick, 'X', Items.leather});
        this.addRecipe(new ItemStack(Items.golden_apple, 1, 0), new Object[] {"###", "#X#", "###", '#', Items.gold_ingot, 'X', Items.apple});
        this.addRecipe(new ItemStack(Items.golden_apple, 1, 1), new Object[] {"###", "#X#", "###", '#', Blocks.gold_block, 'X', Items.apple});
        this.addRecipe(new ItemStack(Items.golden_carrot, 1, 0), new Object[] {"###", "#X#", "###", '#', Items.gold_nugget, 'X', Items.carrot});
        this.addRecipe(new ItemStack(Items.speckled_melon, 1), new Object[] {"###", "#X#", "###", '#', Items.gold_nugget, 'X', Items.melon});
        this.addRecipe(new ItemStack(Blocks.lever, 1), new Object[] {"X", "#", '#', Blocks.cobblestone, 'X', Items.stick});
        this.addRecipe(new ItemStack(Blocks.tripwire_hook, 2), new Object[] {"I", "S", "#", '#', Blocks.planks, 'S', Items.stick, 'I', Items.iron_ingot});
        this.addRecipe(new ItemStack(Blocks.redstone_torch, 1), new Object[] {"X", "#", '#', Items.stick, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Items.repeater, 1), new Object[] {"#X#", "III", '#', Blocks.redstone_torch, 'X', Items.redstone, 'I', new ItemStack(Blocks.stone, 1, BlockStone.EnumType.STONE.getMetadata())});
        this.addRecipe(new ItemStack(Items.comparator, 1), new Object[] {" # ", "#X#", "III", '#', Blocks.redstone_torch, 'X', Items.quartz, 'I', new ItemStack(Blocks.stone, 1, BlockStone.EnumType.STONE.getMetadata())});
        this.addRecipe(new ItemStack(Items.clock, 1), new Object[] {" # ", "#X#", " # ", '#', Items.gold_ingot, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Items.compass, 1), new Object[] {" # ", "#X#", " # ", '#', Items.iron_ingot, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Items.map, 1), new Object[] {"###", "#X#", "###", '#', Items.paper, 'X', Items.compass});
        this.addRecipe(new ItemStack(Blocks.stone_button, 1), new Object[] {"#", '#', new ItemStack(Blocks.stone, 1, BlockStone.EnumType.STONE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.wooden_button, 1), new Object[] {"#", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.stone_pressure_plate, 1), new Object[] {"##", '#', new ItemStack(Blocks.stone, 1, BlockStone.EnumType.STONE.getMetadata())});
        this.addRecipe(new ItemStack(Blocks.wooden_pressure_plate, 1), new Object[] {"##", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.heavy_weighted_pressure_plate, 1), new Object[] {"##", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Blocks.light_weighted_pressure_plate, 1), new Object[] {"##", '#', Items.gold_ingot});
        this.addRecipe(new ItemStack(Blocks.dispenser, 1), new Object[] {"###", "#X#", "#R#", '#', Blocks.cobblestone, 'X', Items.bow, 'R', Items.redstone});
        this.addRecipe(new ItemStack(Blocks.dropper, 1), new Object[] {"###", "# #", "#R#", '#', Blocks.cobblestone, 'R', Items.redstone});
        this.addRecipe(new ItemStack(Blocks.piston, 1), new Object[] {"TTT", "#X#", "#R#", '#', Blocks.cobblestone, 'X', Items.iron_ingot, 'R', Items.redstone, 'T', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.sticky_piston, 1), new Object[] {"S", "P", 'S', Items.slime_ball, 'P', Blocks.piston});
        this.addRecipe(new ItemStack(Items.bed, 1), new Object[] {"###", "XXX", '#', Blocks.wool, 'X', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.enchanting_table, 1), new Object[] {" B ", "D#D", "###", '#', Blocks.obsidian, 'B', Items.book, 'D', Items.diamond});
        this.addRecipe(new ItemStack(Blocks.anvil, 1), new Object[] {"III", " i ", "iii", 'I', Blocks.iron_block, 'i', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.leather), new Object[] {"##", "##", '#', Items.rabbit_hide});
        this.addShapelessRecipe(new ItemStack(Items.ender_eye, 1), new Object[] {Items.ender_pearl, Items.blaze_powder});
        this.addShapelessRecipe(new ItemStack(Items.fire_charge, 3), new Object[] {Items.gunpowder, Items.blaze_powder, Items.coal});
        this.addShapelessRecipe(new ItemStack(Items.fire_charge, 3), new Object[] {Items.gunpowder, Items.blaze_powder, new ItemStack(Items.coal, 1, 1)});
        this.addRecipe(new ItemStack(Blocks.daylight_detector), new Object[] {"GGG", "QQQ", "WWW", 'G', Blocks.glass, 'Q', Items.quartz, 'W', Blocks.wooden_slab});
        this.addRecipe(new ItemStack(Blocks.hopper), new Object[] {"I I", "ICI", " I ", 'I', Items.iron_ingot, 'C', Blocks.chest});
        this.addRecipe(new ItemStack(Items.armor_stand, 1), new Object[] {"///", " / ", "/_/", '/', Items.stick, '_', new ItemStack(Blocks.stone_slab, 1, BlockStoneSlab.EnumType.STONE.getMetadata())});
        Collections.sort(this.recipes, new Comparator<IRecipe>()
        {
            public int compare(IRecipe firstRecipe, IRecipe secondRecipe)
            {
                return firstRecipe instanceof ShapelessRecipes && secondRecipe instanceof ShapedRecipes ? 1 : (secondRecipe instanceof ShapelessRecipes && firstRecipe instanceof ShapedRecipes ? -1 : (secondRecipe.getRecipeSize() < firstRecipe.getRecipeSize() ? -1 : (secondRecipe.getRecipeSize() > firstRecipe.getRecipeSize() ? 1 : 0)));
            }
        });
    }

    public ShapedRecipes addRecipe(ItemStack stack, Object... recipeComponents)
    {
        String pattern = "";
        int componentIndex = 0;
        int recipeWidth = 0;
        int recipeHeight = 0;

        if (recipeComponents[componentIndex] instanceof String[])
        {
            String[] patternRows = (String[])((String[])recipeComponents[componentIndex++]);

            for (int rowIndex = 0; rowIndex < patternRows.length; ++rowIndex)
            {
                String patternRow = patternRows[rowIndex];
                ++recipeHeight;
                recipeWidth = patternRow.length();
                pattern = pattern + patternRow;
            }
        }
        else
        {
            while (recipeComponents[componentIndex] instanceof String)
            {
                String patternRow = (String)recipeComponents[componentIndex++];
                ++recipeHeight;
                recipeWidth = patternRow.length();
                pattern = pattern + patternRow;
            }
        }

        Map<Character, ItemStack> ingredientMap;

        for (ingredientMap = Maps.<Character, ItemStack>newHashMap(); componentIndex < recipeComponents.length; componentIndex += 2)
        {
            Character ingredientKey = (Character)recipeComponents[componentIndex];
            ItemStack ingredientStack = null;

            if (recipeComponents[componentIndex + 1] instanceof Item)
            {
                ingredientStack = new ItemStack((Item)recipeComponents[componentIndex + 1]);
            }
            else if (recipeComponents[componentIndex + 1] instanceof Block)
            {
                ingredientStack = new ItemStack((Block)recipeComponents[componentIndex + 1], 1, 32767);
            }
            else if (recipeComponents[componentIndex + 1] instanceof ItemStack)
            {
                ingredientStack = (ItemStack)recipeComponents[componentIndex + 1];
            }

            ingredientMap.put(ingredientKey, ingredientStack);
        }

        ItemStack[] recipeItems = new ItemStack[recipeWidth * recipeHeight];

        for (int patternIndex = 0; patternIndex < recipeWidth * recipeHeight; ++patternIndex)
        {
            char ingredientKey = pattern.charAt(patternIndex);

            if (ingredientMap.containsKey(Character.valueOf(ingredientKey)))
            {
                recipeItems[patternIndex] = ((ItemStack)ingredientMap.get(Character.valueOf(ingredientKey))).copy();
            }
            else
            {
                recipeItems[patternIndex] = null;
            }
        }

        ShapedRecipes shapedRecipe = new ShapedRecipes(recipeWidth, recipeHeight, recipeItems, stack);
        this.recipes.add(shapedRecipe);
        return shapedRecipe;
    }

    public void addShapelessRecipe(ItemStack stack, Object... recipeComponents)
    {
        List<ItemStack> recipeItems = Lists.<ItemStack>newArrayList();

        for (Object recipeComponent : recipeComponents)
        {
            if (recipeComponent instanceof ItemStack)
            {
                recipeItems.add(((ItemStack)recipeComponent).copy());
            }
            else if (recipeComponent instanceof Item)
            {
                recipeItems.add(new ItemStack((Item)recipeComponent));
            }
            else
            {
                if (!(recipeComponent instanceof Block))
                {
                    throw new IllegalArgumentException("Invalid shapeless recipe: unknown type " + recipeComponent.getClass().getName() + "!");
                }

                recipeItems.add(new ItemStack((Block)recipeComponent));
            }
        }

        this.recipes.add(new ShapelessRecipes(stack, recipeItems));
    }

    public void addRecipe(IRecipe recipe)
    {
        this.recipes.add(recipe);
    }

    public ItemStack findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn)
    {
        for (IRecipe recipe : this.recipes)
        {
            if (recipe.matches(craftMatrix, worldIn))
            {
                return recipe.getCraftingResult(craftMatrix);
            }
        }

        return null;
    }

    public ItemStack[] getRemainingItems(InventoryCrafting inv, World worldIn)
    {
        for (IRecipe recipe : this.recipes)
        {
            if (recipe.matches(inv, worldIn))
            {
                return recipe.getRemainingItems(inv);
            }
        }

        ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];

        for (int slotIndex = 0; slotIndex < remainingItems.length; ++slotIndex)
        {
            remainingItems[slotIndex] = inv.getStackInSlot(slotIndex);
        }

        return remainingItems;
    }

    public List<IRecipe> getRecipeList()
    {
        return this.recipes;
    }
}
