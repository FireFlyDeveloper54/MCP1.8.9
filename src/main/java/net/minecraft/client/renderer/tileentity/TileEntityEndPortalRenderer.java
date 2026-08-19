package net.minecraft.client.renderer.tileentity;

import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.ResourceLocation;
import net.optifine.shaders.ShadersRender;

public class TileEntityEndPortalRenderer extends TileEntitySpecialRenderer<TileEntityEndPortal>
{
    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    FloatBuffer texGenBuffer = GLAllocation.createDirectFloatBuffer(16);

    public void renderTileEntityAt(TileEntityEndPortal te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (!Config.isShaders() || !ShadersRender.renderEndPortal(te, x, y, z, partialTicks, destroyStage, 0.75F))
        {
            float dispatcherEntityX = (float)this.rendererDispatcher.entityX;
            float dispatcherEntityY = (float)this.rendererDispatcher.entityY;
            float dispatcherEntityZ = (float)this.rendererDispatcher.entityZ;
            GlStateManager.disableLighting();
            RANDOM.setSeed(31100L);
            float portalSurfaceOffset = 0.75F;

            for (int layerIndex = 0; layerIndex < 16; ++layerIndex)
            {
                GlStateManager.pushMatrix();
                float layerDepth = (float)(16 - layerIndex);
                float textureScale = 0.0625F;
                float colorScale = 1.0F / (layerDepth + 1.0F);

                if (layerIndex == 0)
                {
                    this.bindTexture(END_SKY_TEXTURE);
                    colorScale = 0.1F;
                    layerDepth = 65.0F;
                    textureScale = 0.125F;
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                }

                if (layerIndex >= 1)
                {
                    this.bindTexture(END_PORTAL_TEXTURE);
                }

                if (layerIndex == 1)
                {
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(1, 1);
                    textureScale = 0.5F;
                }

                float portalPlaneOffset = (float)(-(y + (double)portalSurfaceOffset));
                float viewerPlaneY = portalPlaneOffset + (float)ActiveRenderInfo.getPosition().yCoord;
                float layerPlaneY = portalPlaneOffset + layerDepth + (float)ActiveRenderInfo.getPosition().yCoord;
                float layerTranslateY = viewerPlaneY / layerPlaneY;
                layerTranslateY = (float)(y + (double)portalSurfaceOffset) + layerTranslateY;
                GlStateManager.translate(dispatcherEntityX, layerTranslateY, dispatcherEntityZ);
                GlStateManager.texGen(GlStateManager.TexGen.S, 9217);
                GlStateManager.texGen(GlStateManager.TexGen.T, 9217);
                GlStateManager.texGen(GlStateManager.TexGen.R, 9217);
                GlStateManager.texGen(GlStateManager.TexGen.Q, 9216);
                GlStateManager.texGen(GlStateManager.TexGen.S, 9473, this.getTexGenBuffer(1.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.texGen(GlStateManager.TexGen.T, 9473, this.getTexGenBuffer(0.0F, 0.0F, 1.0F, 0.0F));
                GlStateManager.texGen(GlStateManager.TexGen.R, 9473, this.getTexGenBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                GlStateManager.texGen(GlStateManager.TexGen.Q, 9474, this.getTexGenBuffer(0.0F, 1.0F, 0.0F, 0.0F));
                GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
                GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
                GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
                GlStateManager.enableTexGenCoord(GlStateManager.TexGen.Q);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5890);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
                GlStateManager.scale(textureScale, textureScale, textureScale);
                GlStateManager.translate(0.5F, 0.5F, 0.0F);
                GlStateManager.rotate((float)(layerIndex * layerIndex * 4321 + layerIndex * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(-0.5F, -0.5F, 0.0F);
                GlStateManager.translate(-dispatcherEntityX, -dispatcherEntityZ, -dispatcherEntityY);
                viewerPlaneY = portalPlaneOffset + (float)ActiveRenderInfo.getPosition().yCoord;
                GlStateManager.translate((float)ActiveRenderInfo.getPosition().xCoord * layerDepth / viewerPlaneY, (float)ActiveRenderInfo.getPosition().zCoord * layerDepth / viewerPlaneY, -dispatcherEntityY);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                float red = (RANDOM.nextFloat() * 0.5F + 0.1F) * colorScale;
                float green = (RANDOM.nextFloat() * 0.5F + 0.4F) * colorScale;
                float blue = (RANDOM.nextFloat() * 0.5F + 0.5F) * colorScale;

                if (layerIndex == 0)
                {
                    red = green = blue = 1.0F * colorScale;
                }

                worldRenderer.pos(x, y + (double)portalSurfaceOffset, z).color(red, green, blue, 1.0F).endVertex();
                worldRenderer.pos(x, y + (double)portalSurfaceOffset, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
                worldRenderer.pos(x + 1.0D, y + (double)portalSurfaceOffset, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
                worldRenderer.pos(x + 1.0D, y + (double)portalSurfaceOffset, z).color(red, green, blue, 1.0F).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                this.bindTexture(END_SKY_TEXTURE);
            }

            GlStateManager.disableBlend();
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.disableTexGenCoord(GlStateManager.TexGen.Q);
            GlStateManager.enableLighting();
        }
    }

    private FloatBuffer getTexGenBuffer(float x, float y, float z, float w)
    {
        this.texGenBuffer.clear();
        this.texGenBuffer.put(x).put(y).put(z).put(w);
        this.texGenBuffer.flip();
        return this.texGenBuffer;
    }
}
