package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;

public class RenderTntMinecart extends RenderMinecart<EntityMinecartTNT>
{
    public RenderTntMinecart(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    protected void renderCartContents(EntityMinecartTNT minecart, float partialTicks, IBlockState state)
    {
        int fuseTicks = minecart.getFuseTicks();

        if (fuseTicks > -1 && (float)fuseTicks - partialTicks + 1.0F < 10.0F)
        {
            float fuseProgress = 1.0F - ((float)fuseTicks - partialTicks + 1.0F) / 10.0F;
            fuseProgress = MathHelper.clamp_float(fuseProgress, 0.0F, 1.0F);
            fuseProgress = fuseProgress * fuseProgress;
            fuseProgress = fuseProgress * fuseProgress;
            float fuseScale = 1.0F + fuseProgress * 0.3F;
            GlStateManager.scale(fuseScale, fuseScale, fuseScale);
        }

        super.renderCartContents(minecart, partialTicks, state);

        if (fuseTicks > -1 && fuseTicks / 5 % 2 == 0)
        {
            BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 772);
            GlStateManager.color(1.0F, 1.0F, 1.0F, (1.0F - ((float)fuseTicks - partialTicks + 1.0F) / 100.0F) * 0.8F);
            GlStateManager.pushMatrix();
            blockRendererDispatcher.renderBlockBrightness(Blocks.tnt.getDefaultState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }
    }
}
