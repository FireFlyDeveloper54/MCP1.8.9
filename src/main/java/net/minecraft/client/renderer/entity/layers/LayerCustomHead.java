package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StringUtils;

public class LayerCustomHead implements LayerRenderer<EntityLivingBase>
{
    private final ModelRenderer modelRenderer;

    public LayerCustomHead(ModelRenderer modelRendererIn)
    {
        this.modelRenderer = modelRendererIn;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        ItemStack itemStack = entitylivingbaseIn.getCurrentArmor(3);

        if (itemStack != null && itemStack.getItem() != null)
        {
            Item item = itemStack.getItem();
            Minecraft minecraft = Minecraft.getMinecraft();
            GlStateManager.pushMatrix();

            if (entitylivingbaseIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            boolean hasVillagerHeadShape = entitylivingbaseIn instanceof EntityVillager || entitylivingbaseIn instanceof EntityZombie && ((EntityZombie)entitylivingbaseIn).isVillager();

            if (!hasVillagerHeadShape && entitylivingbaseIn.isChild())
            {
                float adultHeadScale = 2.0F;
                float childHeadScale = 1.4F;
                GlStateManager.scale(childHeadScale / adultHeadScale, childHeadScale / adultHeadScale, childHeadScale / adultHeadScale);
                GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            }

            this.modelRenderer.postRender(0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (item instanceof ItemBlock)
            {
                float blockScale = 0.625F;
                GlStateManager.translate(0.0F, -0.25F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(blockScale, -blockScale, -blockScale);

                if (hasVillagerHeadShape)
                {
                    GlStateManager.translate(0.0F, 0.1875F, 0.0F);
                }

                minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemStack, ItemCameraTransforms.TransformType.HEAD);
            }
            else if (item == Items.skull)
            {
                float skullScale = 1.1875F;
                GlStateManager.scale(skullScale, -skullScale, -skullScale);

                if (hasVillagerHeadShape)
                {
                    GlStateManager.translate(0.0F, 0.0625F, 0.0F);
                }

                GameProfile gameProfile = null;

                if (itemStack.hasTagCompound())
                {
                    NBTTagCompound tagCompound = itemStack.getTagCompound();

                    if (tagCompound.hasKey("SkullOwner", 10))
                    {
                        gameProfile = NBTUtil.readGameProfileFromNBT(tagCompound.getCompoundTag("SkullOwner"));
                    }
                    else if (tagCompound.hasKey("SkullOwner", 8))
                    {
                        String ownerName = tagCompound.getString("SkullOwner");

                        if (!StringUtils.isNullOrEmpty(ownerName))
                        {
                            gameProfile = TileEntitySkull.updateGameprofile(new GameProfile((UUID)null, ownerName));
                            tagCompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameProfile));
                        }
                    }
                }

                TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F, itemStack.getMetadata(), gameProfile, -1);
            }

            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}
