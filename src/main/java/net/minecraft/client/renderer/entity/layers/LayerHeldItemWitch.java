package net.minecraft.client.renderer.entity.layers;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderWitch;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class LayerHeldItemWitch implements LayerRenderer<EntityWitch>
{
    private final RenderWitch witchRenderer;

    public LayerHeldItemWitch(RenderWitch witchRendererIn)
    {
        this.witchRenderer = witchRendererIn;
    }

    public void doRenderLayer(EntityWitch entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        ItemStack itemStack = entitylivingbaseIn.getHeldItem();

        if (itemStack != null)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();

            if (this.witchRenderer.getMainModel().isChild)
            {
                GlStateManager.translate(0.0F, 0.625F, 0.0F);
                GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
                float childScale = 0.5F;
                GlStateManager.scale(childScale, childScale, childScale);
            }

            ((ModelWitch)this.witchRenderer.getMainModel()).villagerNose.postRender(0.0625F);
            GlStateManager.translate(-0.0625F, 0.53125F, 0.21875F);
            Item item = itemStack.getItem();
            Minecraft minecraft = Minecraft.getMinecraft();

            if (item instanceof ItemBlock && minecraft.getBlockRendererDispatcher().isRenderTypeChest(Block.getBlockFromItem(item), itemStack.getMetadata()))
            {
                GlStateManager.translate(0.0F, 0.0625F, -0.25F);
                GlStateManager.rotate(30.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-5.0F, 0.0F, 1.0F, 0.0F);
                float chestBlockScale = 0.375F;
                GlStateManager.scale(chestBlockScale, -chestBlockScale, chestBlockScale);
            }
            else if (item == Items.bow)
            {
                GlStateManager.translate(0.0F, 0.125F, -0.125F);
                GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
                float bowScale = 0.625F;
                GlStateManager.scale(bowScale, -bowScale, bowScale);
                GlStateManager.rotate(-100.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-20.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (item.isFull3D())
            {
                if (item.shouldRotateAroundWhenRendering())
                {
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(0.0F, -0.0625F, 0.0F);
                }

                this.witchRenderer.transformHeldFull3DItemLayer();
                GlStateManager.translate(0.0625F, -0.125F, 0.0F);
                float full3DItemScale = 0.625F;
                GlStateManager.scale(full3DItemScale, -full3DItemScale, full3DItemScale);
                GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                GlStateManager.translate(0.1875F, 0.1875F, 0.0F);
                float flatItemScale = 0.875F;
                GlStateManager.scale(flatItemScale, flatItemScale, flatItemScale);
                GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(-60.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-30.0F, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.rotate(-15.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(40.0F, 0.0F, 0.0F, 1.0F);
            minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemStack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
