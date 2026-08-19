package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TileEntityPistonRenderer extends TileEntitySpecialRenderer<TileEntityPiston>
{
    private final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

    public void renderTileEntityAt(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        BlockPos blockPos = te.getPos();
        IBlockState pistonState = te.getPistonState();
        Block block = pistonState.getBlock();

        if (block.getMaterial() != Material.air && te.getProgress(partialTicks) < 1.0F)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            this.bindTexture(TextureMap.locationBlocksTexture);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();

            if (Minecraft.isAmbientOcclusionEnabled())
            {
                GlStateManager.shadeModel(7425);
            }
            else
            {
                GlStateManager.shadeModel(7424);
            }

            worldRenderer.begin(7, DefaultVertexFormats.BLOCK);
            worldRenderer.setTranslation((double)((float)x - (float)blockPos.getX() + te.getOffsetX(partialTicks)), (double)((float)y - (float)blockPos.getY() + te.getOffsetY(partialTicks)), (double)((float)z - (float)blockPos.getZ() + te.getOffsetZ(partialTicks)));
            World world = this.getWorld();

            if (block == Blocks.piston_head && te.getProgress(partialTicks) < 0.5F)
            {
                pistonState = pistonState.withProperty(BlockPistonExtension.SHORT, Boolean.valueOf(true));
                this.blockRenderer.getBlockModelRenderer().renderModel(world, this.blockRenderer.getModelFromBlockState(pistonState, world, blockPos), pistonState, blockPos, worldRenderer, true);
            }
            else if (te.shouldPistonHeadBeRendered() && !te.isExtending())
            {
                BlockPistonExtension.EnumPistonType pistonType = block == Blocks.sticky_piston ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
                IBlockState pistonHeadState = Blocks.piston_head.getDefaultState().withProperty(BlockPistonExtension.TYPE, pistonType).withProperty(BlockPistonExtension.FACING, pistonState.getValue(BlockPistonBase.FACING));
                pistonHeadState = pistonHeadState.withProperty(BlockPistonExtension.SHORT, Boolean.valueOf(te.getProgress(partialTicks) >= 0.5F));
                this.blockRenderer.getBlockModelRenderer().renderModel(world, this.blockRenderer.getModelFromBlockState(pistonHeadState, world, blockPos), pistonHeadState, blockPos, worldRenderer, true);
                worldRenderer.setTranslation((double)((float)x - (float)blockPos.getX()), (double)((float)y - (float)blockPos.getY()), (double)((float)z - (float)blockPos.getZ()));
                pistonState.withProperty(BlockPistonBase.EXTENDED, Boolean.valueOf(true));
                this.blockRenderer.getBlockModelRenderer().renderModel(world, this.blockRenderer.getModelFromBlockState(pistonState, world, blockPos), pistonState, blockPos, worldRenderer, true);
            }
            else
            {
                this.blockRenderer.getBlockModelRenderer().renderModel(world, this.blockRenderer.getModelFromBlockState(pistonState, world, blockPos), pistonState, blockPos, worldRenderer, false);
            }

            worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
            tessellator.draw();
            RenderHelper.enableStandardItemLighting();
        }
    }
}
