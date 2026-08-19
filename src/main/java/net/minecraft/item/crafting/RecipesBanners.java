package net.minecraft.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.world.World;

public class RecipesBanners
{
    void addRecipes(CraftingManager craftingManager)
    {
        for (EnumDyeColor dyeColor : EnumDyeColor.VALUES)
        {
            craftingManager.addRecipe(new ItemStack(Items.banner, 1, dyeColor.getDyeDamage()), new Object[] {"###", "###", " | ", '#', new ItemStack(Blocks.wool, 1, dyeColor.getMetadata()), '|', Items.stick});
        }

        craftingManager.addRecipe(new RecipesBanners.RecipeDuplicatePattern());
        craftingManager.addRecipe(new RecipesBanners.RecipeAddPattern());
    }

    static class RecipeAddPattern implements IRecipe
    {
        private RecipeAddPattern()
        {
        }

        public boolean matches(InventoryCrafting inv, World worldIn)
        {
            boolean foundBanner = false;

            for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
            {
                ItemStack itemStack = inv.getStackInSlot(slotIndex);

                if (itemStack != null && itemStack.getItem() == Items.banner)
                {
                    if (foundBanner)
                    {
                        return false;
                    }

                    if (TileEntityBanner.getPatterns(itemStack) >= 6)
                    {
                        return false;
                    }

                    foundBanner = true;
                }
            }

            if (!foundBanner)
            {
                return false;
            }
            else
            {
                return this.getMatchingPattern(inv) != null;
            }
        }

        public ItemStack getCraftingResult(InventoryCrafting inv)
        {
            ItemStack bannerStack = null;

            for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
            {
                ItemStack slotStack = inv.getStackInSlot(slotIndex);

                if (slotStack != null && slotStack.getItem() == Items.banner)
                {
                    bannerStack = slotStack.copy();
                    bannerStack.stackSize = 1;
                    break;
                }
            }

            TileEntityBanner.EnumBannerPattern bannerPattern = this.getMatchingPattern(inv);

            if (bannerPattern != null)
            {
                int dyeMetadata = 0;

                for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
                {
                    ItemStack slotStack = inv.getStackInSlot(slotIndex);

                    if (slotStack != null && slotStack.getItem() == Items.dye)
                    {
                        dyeMetadata = slotStack.getMetadata();
                        break;
                    }
                }

                NBTTagCompound blockEntityTag = bannerStack.getSubCompound("BlockEntityTag", true);
                NBTTagList patternList = null;

                if (blockEntityTag.hasKey("Patterns", 9))
                {
                    patternList = blockEntityTag.getTagList("Patterns", 10);
                }
                else
                {
                    patternList = new NBTTagList();
                    blockEntityTag.setTag("Patterns", patternList);
                }

                NBTTagCompound patternTag = new NBTTagCompound();
                patternTag.setString("Pattern", bannerPattern.getPatternID());
                patternTag.setInteger("Color", dyeMetadata);
                patternList.appendTag(patternTag);
            }

            return bannerStack;
        }

        public int getRecipeSize()
        {
            return 10;
        }

        public ItemStack getRecipeOutput()
        {
            return null;
        }

        public ItemStack[] getRemainingItems(InventoryCrafting inv)
        {
            ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];

            for (int slotIndex = 0; slotIndex < remainingItems.length; ++slotIndex)
            {
                ItemStack itemStack = inv.getStackInSlot(slotIndex);

                if (itemStack != null && itemStack.getItem().hasContainerItem())
                {
                    remainingItems[slotIndex] = new ItemStack(itemStack.getItem().getContainerItem());
                }
            }

            return remainingItems;
        }

        private TileEntityBanner.EnumBannerPattern getMatchingPattern(InventoryCrafting inv)
        {
            for (TileEntityBanner.EnumBannerPattern bannerPattern : TileEntityBanner.EnumBannerPattern.VALUES)
            {
                if (bannerPattern.hasValidCrafting())
                {
                    boolean patternMatches = true;

                    if (bannerPattern.hasCraftingStack())
                    {
                        boolean foundSpecialItem = false;
                        boolean foundDye = false;

                        for (int slotIndex = 0; slotIndex < inv.getSizeInventory() && patternMatches; ++slotIndex)
                        {
                            ItemStack slotStack = inv.getStackInSlot(slotIndex);

                            if (slotStack != null && slotStack.getItem() != Items.banner)
                            {
                                if (slotStack.getItem() == Items.dye)
                                {
                                    if (foundDye)
                                    {
                                        patternMatches = false;
                                        break;
                                    }

                                    foundDye = true;
                                }
                                else
                                {
                                    if (foundSpecialItem || !slotStack.isItemEqual(bannerPattern.getCraftingStack()))
                                    {
                                        patternMatches = false;
                                        break;
                                    }

                                    foundSpecialItem = true;
                                }
                            }
                        }

                        if (!foundSpecialItem)
                        {
                            patternMatches = false;
                        }
                    }
                    else if (inv.getSizeInventory() == bannerPattern.getCraftingLayers().length * bannerPattern.getCraftingLayers()[0].length())
                    {
                        int dyeMetadata = -1;

                        for (int slotIndex = 0; slotIndex < inv.getSizeInventory() && patternMatches; ++slotIndex)
                        {
                            int row = slotIndex / 3;
                            int column = slotIndex % 3;
                            ItemStack slotStack = inv.getStackInSlot(slotIndex);

                            if (slotStack != null && slotStack.getItem() != Items.banner)
                            {
                                if (slotStack.getItem() != Items.dye)
                                {
                                    patternMatches = false;
                                    break;
                                }

                                if (dyeMetadata != -1 && dyeMetadata != slotStack.getMetadata())
                                {
                                    patternMatches = false;
                                    break;
                                }

                                if (bannerPattern.getCraftingLayers()[row].charAt(column) == 32)
                                {
                                    patternMatches = false;
                                    break;
                                }

                                dyeMetadata = slotStack.getMetadata();
                            }
                            else if (bannerPattern.getCraftingLayers()[row].charAt(column) != 32)
                            {
                                patternMatches = false;
                                break;
                            }
                        }
                    }
                    else
                    {
                        patternMatches = false;
                    }

                    if (patternMatches)
                    {
                        return bannerPattern;
                    }
                }
            }

            return null;
        }
    }

    static class RecipeDuplicatePattern implements IRecipe
    {
        private RecipeDuplicatePattern()
        {
        }

        public boolean matches(InventoryCrafting inv, World worldIn)
        {
            ItemStack patternedBannerStack = null;
            ItemStack blankBannerStack = null;

            for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
            {
                ItemStack slotStack = inv.getStackInSlot(slotIndex);

                if (slotStack != null)
                {
                    if (slotStack.getItem() != Items.banner)
                    {
                        return false;
                    }

                    if (patternedBannerStack != null && blankBannerStack != null)
                    {
                        return false;
                    }

                    int baseColor = TileEntityBanner.getBaseColor(slotStack);
                    boolean hasPattern = TileEntityBanner.getPatterns(slotStack) > 0;

                    if (patternedBannerStack != null)
                    {
                        if (hasPattern)
                        {
                            return false;
                        }

                        if (baseColor != TileEntityBanner.getBaseColor(patternedBannerStack))
                        {
                            return false;
                        }

                        blankBannerStack = slotStack;
                    }
                    else if (blankBannerStack != null)
                    {
                        if (!hasPattern)
                        {
                            return false;
                        }

                        if (baseColor != TileEntityBanner.getBaseColor(blankBannerStack))
                        {
                            return false;
                        }

                        patternedBannerStack = slotStack;
                    }
                    else if (hasPattern)
                    {
                        patternedBannerStack = slotStack;
                    }
                    else
                    {
                        blankBannerStack = slotStack;
                    }
                }
            }

            return patternedBannerStack != null && blankBannerStack != null;
        }

        public ItemStack getCraftingResult(InventoryCrafting inv)
        {
            for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
            {
                ItemStack itemStack = inv.getStackInSlot(slotIndex);

                if (itemStack != null && TileEntityBanner.getPatterns(itemStack) > 0)
                {
                    ItemStack copiedBannerStack = itemStack.copy();
                    copiedBannerStack.stackSize = 1;
                    return copiedBannerStack;
                }
            }

            return null;
        }

        public int getRecipeSize()
        {
            return 2;
        }

        public ItemStack getRecipeOutput()
        {
            return null;
        }

        public ItemStack[] getRemainingItems(InventoryCrafting inv)
        {
            ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];

            for (int slotIndex = 0; slotIndex < remainingItems.length; ++slotIndex)
            {
                ItemStack itemStack = inv.getStackInSlot(slotIndex);

                if (itemStack != null)
                {
                    if (itemStack.getItem().hasContainerItem())
                    {
                        remainingItems[slotIndex] = new ItemStack(itemStack.getItem().getContainerItem());
                    }
                    else if (itemStack.hasTagCompound() && TileEntityBanner.getPatterns(itemStack) > 0)
                    {
                        remainingItems[slotIndex] = itemStack.copy();
                        remainingItems[slotIndex].stackSize = 1;
                    }
                }
            }

            return remainingItems;
        }
    }
}
