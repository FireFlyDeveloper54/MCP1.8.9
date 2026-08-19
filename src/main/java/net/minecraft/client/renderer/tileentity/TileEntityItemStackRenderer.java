package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;

public class TileEntityItemStackRenderer
{
    public static TileEntityItemStackRenderer instance = new TileEntityItemStackRenderer();
    private TileEntityChest chest = new TileEntityChest(0);
    private TileEntityChest trappedChest = new TileEntityChest(1);
    private TileEntityEnderChest enderChest = new TileEntityEnderChest();
    private TileEntityBanner banner = new TileEntityBanner();
    private TileEntitySkull skull = new TileEntitySkull();

    public void renderByItem(ItemStack itemStackIn)
    {
        if (itemStackIn.getItem() == Items.banner)
        {
            this.banner.setItemValues(itemStackIn);
            TileEntityRendererDispatcher.instance.renderTileEntityAt(this.banner, 0.0D, 0.0D, 0.0D, 0.0F);
        }
        else if (itemStackIn.getItem() == Items.skull)
        {
            GameProfile gameProfile = null;

            if (itemStackIn.hasTagCompound())
            {
                NBTTagCompound nBTTagCompound = itemStackIn.getTagCompound();

                if (nBTTagCompound.hasKey("SkullOwner", 10))
                {
                    gameProfile = NBTUtil.readGameProfileFromNBT(nBTTagCompound.getCompoundTag("SkullOwner"));
                }
                else if (nBTTagCompound.hasKey("SkullOwner", 8) && nBTTagCompound.getString("SkullOwner").length() > 0)
                {
                    gameProfile = new GameProfile((UUID)null, nBTTagCompound.getString("SkullOwner"));
                    gameProfile = TileEntitySkull.updateGameprofile(gameProfile);
                    nBTTagCompound.removeTag("SkullOwner");
                    nBTTagCompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameProfile));
                }
            }

            if (TileEntitySkullRenderer.instance != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-0.5F, 0.0F, -0.5F);
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                GlStateManager.disableCull();
                TileEntitySkullRenderer.instance.renderSkull(0.0F, 0.0F, 0.0F, EnumFacing.UP, 0.0F, itemStackIn.getMetadata(), gameProfile, -1);
                GlStateManager.enableCull();
                GlStateManager.popMatrix();
            }
        }
        else
        {
            Block block = Block.getBlockFromItem(itemStackIn.getItem());

            if (block == Blocks.ender_chest)
            {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(this.enderChest, 0.0D, 0.0D, 0.0D, 0.0F);
            }
            else if (block == Blocks.trapped_chest)
            {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(this.trappedChest, 0.0D, 0.0D, 0.0D, 0.0F);
            }
            else
            {
                TileEntityRendererDispatcher.instance.renderTileEntityAt(this.chest, 0.0D, 0.0D, 0.0D, 0.0F);
            }
        }
    }
}
