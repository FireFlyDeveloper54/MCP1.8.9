package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class NpcMerchant implements IMerchant
{
    private InventoryMerchant theMerchantInventory;
    private EntityPlayer customer;
    private MerchantRecipeList recipeList;
    private IChatComponent displayName;

    public NpcMerchant(EntityPlayer customerIn, IChatComponent displayNameIn)
    {
        this.customer = customerIn;
        this.displayName = displayNameIn;
        this.theMerchantInventory = new InventoryMerchant(customerIn, this);
    }

    public EntityPlayer getCustomer()
    {
        return this.customer;
    }

    public void setCustomer(EntityPlayer customerIn)
    {
    }

    public MerchantRecipeList getRecipes(EntityPlayer player)
    {
        return this.recipeList;
    }

    public void setRecipes(MerchantRecipeList recipeList)
    {
        this.recipeList = recipeList;
    }

    public void useRecipe(MerchantRecipe recipe)
    {
        recipe.incrementToolUses();
    }

    public void verifySellingItem(ItemStack stack)
    {
    }

    public IChatComponent getDisplayName()
    {
        return (IChatComponent)(this.displayName != null ? this.displayName : new ChatComponentTranslation("entity.Villager.name", new Object[0]));
    }
}
