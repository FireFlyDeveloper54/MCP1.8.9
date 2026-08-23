package net.minecraft.client.renderer.tileentity;

import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class TileEntityBeaconRenderer extends TileEntitySpecialRenderer<TileEntityBeacon>
{
    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

    public void renderTileEntityAt(TileEntityBeacon te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        float beamAlpha = te.shouldBeamRender();

        if ((double)beamAlpha > 0.0D)
        {
            if (Config.isShaders())
            {
                Shaders.beginBeacon();
            }

            GlStateManager.alphaFunc(516, 0.1F);

            if (beamAlpha > 0.0F)
            {
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                GlStateManager.disableFog();
                List<TileEntityBeacon.BeamSegment> beamSegments = te.getBeamSegments();
                int segmentStartY = 0;

                for (int segmentIndex = 0; segmentIndex < beamSegments.size(); ++segmentIndex)
                {
                    TileEntityBeacon.BeamSegment beamSegment = (TileEntityBeacon.BeamSegment)beamSegments.get(segmentIndex);
                    int segmentEndY = segmentStartY + beamSegment.getHeight();
                    this.bindTexture(beaconBeam);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                    GlStateManager.disableLighting();
                    GlStateManager.disableCull();
                    GlStateManager.disableBlend();
                    GlStateManager.depthMask(true);
                    GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
                    double renderTime = (double)te.getWorld().getTotalWorldTime() + (double)partialTicks;
                    double textureScroll = MathHelper.frac(-renderTime * 0.2D - (double)MathHelper.floor_double(-renderTime * 0.1D));
                    float red = beamSegment.getColors()[0];
                    float green = beamSegment.getColors()[1];
                    float blue = beamSegment.getColors()[2];
                    double rotation = renderTime * 0.025D * -1.5D;
                    double beamRadius = 0.2D;
                    double corner1X = 0.5D + Math.cos(rotation + 2.356194490192345D) * beamRadius;
                    double corner1Z = 0.5D + Math.sin(rotation + 2.356194490192345D) * beamRadius;
                    double corner2X = 0.5D + Math.cos(rotation + (Math.PI / 4D)) * beamRadius;
                    double corner2Z = 0.5D + Math.sin(rotation + (Math.PI / 4D)) * beamRadius;
                    double corner3X = 0.5D + Math.cos(rotation + 3.9269908169872414D) * beamRadius;
                    double corner3Z = 0.5D + Math.sin(rotation + 3.9269908169872414D) * beamRadius;
                    double corner4X = 0.5D + Math.cos(rotation + 5.497787143782138D) * beamRadius;
                    double corner4Z = 0.5D + Math.sin(rotation + 5.497787143782138D) * beamRadius;
                    double innerMinV = -1.0D + textureScroll;
                    double innerMaxV = (double)((float)beamSegment.getHeight() * beamAlpha) * 2.5D + innerMinV;
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldRenderer.pos(x + corner1X, y + (double)segmentEndY, z + corner1Z).tex(1.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner1X, y + (double)segmentStartY, z + corner1Z).tex(1.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner2X, y + (double)segmentStartY, z + corner2Z).tex(0.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner2X, y + (double)segmentEndY, z + corner2Z).tex(0.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner4X, y + (double)segmentEndY, z + corner4Z).tex(1.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner4X, y + (double)segmentStartY, z + corner4Z).tex(1.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner3X, y + (double)segmentStartY, z + corner3Z).tex(0.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner3X, y + (double)segmentEndY, z + corner3Z).tex(0.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner2X, y + (double)segmentEndY, z + corner2Z).tex(1.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner2X, y + (double)segmentStartY, z + corner2Z).tex(1.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner4X, y + (double)segmentStartY, z + corner4Z).tex(0.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner4X, y + (double)segmentEndY, z + corner4Z).tex(0.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner3X, y + (double)segmentEndY, z + corner3Z).tex(1.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner3X, y + (double)segmentStartY, z + corner3Z).tex(1.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner1X, y + (double)segmentStartY, z + corner1Z).tex(0.0D, innerMinV).color(red, green, blue, 1.0F).endVertex();
                    worldRenderer.pos(x + corner1X, y + (double)segmentEndY, z + corner1Z).tex(0.0D, innerMaxV).color(red, green, blue, 1.0F).endVertex();
                    tessellator.draw();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.depthMask(false);
                    double outerMinX = 0.2D;
                    double outerMinZ = 0.2D;
                    double outerMaxX = 0.8D;
                    double outerMaxZ = 0.8D;
                    double outerMinV = -1.0D + textureScroll;
                    double outerMaxV = (double)((float)beamSegment.getHeight() * beamAlpha) + outerMinV;
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldRenderer.pos(x + outerMinX, y + (double)segmentEndY, z + outerMinZ).tex(1.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentStartY, z + outerMinZ).tex(1.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentStartY, z + outerMinZ).tex(0.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentEndY, z + outerMinZ).tex(0.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentEndY, z + outerMaxZ).tex(1.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentStartY, z + outerMaxZ).tex(1.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentStartY, z + outerMaxZ).tex(0.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentEndY, z + outerMaxZ).tex(0.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentEndY, z + outerMinZ).tex(1.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentStartY, z + outerMinZ).tex(1.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentStartY, z + outerMaxZ).tex(0.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMaxX, y + (double)segmentEndY, z + outerMaxZ).tex(0.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentEndY, z + outerMaxZ).tex(1.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentStartY, z + outerMaxZ).tex(1.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentStartY, z + outerMinZ).tex(0.0D, outerMinV).color(red, green, blue, 0.125F).endVertex();
                    worldRenderer.pos(x + outerMinX, y + (double)segmentEndY, z + outerMinZ).tex(0.0D, outerMaxV).color(red, green, blue, 0.125F).endVertex();
                    tessellator.draw();
                    GlStateManager.enableLighting();
                    GlStateManager.enableTexture2D();
                    GlStateManager.depthMask(true);
                    segmentStartY = segmentEndY;
                }

                GlStateManager.enableFog();
            }

            if (Config.isShaders())
            {
                Shaders.endBeacon();
            }
        }
    }

    public boolean forceTileEntityRender()
    {
        return true;
    }
}
