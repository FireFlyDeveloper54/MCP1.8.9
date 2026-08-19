package net.minecraft.client.renderer.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RenderFallingBlock extends Render<EntityFallingBlock>
{
    public RenderFallingBlock(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    public void doRender(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (entity.getBlock() != null)
        {
            this.bindTexture(TextureMap.locationBlocksTexture);
            IBlockState blockState = entity.getBlock();
            Block block = blockState.getBlock();
            BlockPos blockPos = new BlockPos(entity);
            World world = entity.getWorldObj();

            if (blockState != world.getBlockState(blockPos) && block.getRenderType() != -1)
            {
                if (block.getRenderType() == 3)
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((float)x, (float)y, (float)z);
                    GlStateManager.disableLighting();
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                    worldRenderer.begin(7, DefaultVertexFormats.BLOCK);
                    int blockX = blockPos.getX();
                    int blockY = blockPos.getY();
                    int blockZ = blockPos.getZ();
                    worldRenderer.setTranslation((double)((float)(-blockX) - 0.5F), (double)(-blockY), (double)((float)(-blockZ) - 0.5F));
                    BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                    IBakedModel bakedModel = blockRendererDispatcher.getModelFromBlockState(blockState, world, (BlockPos)null);
                    blockRendererDispatcher.getBlockModelRenderer().renderModel(world, bakedModel, blockState, blockPos, worldRenderer, false);
                    worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
                    tessellator.draw();
                    GlStateManager.enableLighting();
                    GlStateManager.popMatrix();
                    super.doRender(entity, x, y, z, entityYaw, partialTicks);
                }
            }
        }
    }

    protected ResourceLocation getEntityTexture(EntityFallingBlock entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
