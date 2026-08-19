package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.src.Config;
import net.minecraft.util.EnumWorldBlockLayer;
import net.optifine.render.VboRegion;
import net.optifine.shaders.ShadersRender;
import org.lwjgl.opengl.GL11;

public class VboRenderList extends ChunkRenderContainer
{
    private double viewEntityX;
    private double viewEntityY;
    private double viewEntityZ;

    public void renderChunkLayer(EnumWorldBlockLayer layer)
    {
        if (this.initialized)
        {
            if (!Config.isRenderRegions())
            {
                for (RenderChunk renderchunk1 : this.renderChunks)
                {
                    VertexBuffer vertexbuffer1 = renderchunk1.getVertexBufferByLayer(layer.ordinal());
                    GlStateManager.pushMatrix();
                    this.preRenderChunk(renderchunk1);
                    renderchunk1.multModelviewMatrix();
                    vertexbuffer1.bindBuffer();
                    this.setupArrayPointers();
                    vertexbuffer1.drawArrays(7);
                    GlStateManager.popMatrix();
                }
            }
            else
            {
                int currentRegionX = Integer.MIN_VALUE;
                int currentRegionZ = Integer.MIN_VALUE;
                VboRegion vboRegion = null;

                for (RenderChunk renderChunk : this.renderChunks)
                {
                    VertexBuffer vertexBuffer = renderChunk.getVertexBufferByLayer(layer.ordinal());
                    VboRegion chunkVboRegion = vertexBuffer.getVboRegion();

                    if (chunkVboRegion != vboRegion || currentRegionX != renderChunk.regionX || currentRegionZ != renderChunk.regionZ)
                    {
                        if (vboRegion != null)
                        {
                            this.drawRegion(currentRegionX, currentRegionZ, vboRegion);
                        }

                        currentRegionX = renderChunk.regionX;
                        currentRegionZ = renderChunk.regionZ;
                        vboRegion = chunkVboRegion;
                    }

                    vertexBuffer.drawArrays(7);
                }

                if (vboRegion != null)
                {
                    this.drawRegion(currentRegionX, currentRegionZ, vboRegion);
                }
            }

            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            GlStateManager.resetColor();
            this.renderChunks.clear();
        }
    }

    public void setupArrayPointers()
    {
        if (Config.isShaders())
        {
            ShadersRender.setupArrayPointersVbo();
        }
        else
        {
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 28, 0L);
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12L);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }
    }

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn)
    {
        this.viewEntityX = viewEntityXIn;
        this.viewEntityY = viewEntityYIn;
        this.viewEntityZ = viewEntityZIn;
        super.initialize(viewEntityXIn, viewEntityYIn, viewEntityZIn);
    }

    private void drawRegion(int regionX, int regionZ, VboRegion vboRegion)
    {
        GlStateManager.pushMatrix();
        this.preRenderRegion(regionX, 0, regionZ);
        vboRegion.finishDraw(this);
        GlStateManager.popMatrix();
    }

    public void preRenderRegion(int regionX, int regionY, int regionZ)
    {
        GlStateManager.translate((float)((double)regionX - this.viewEntityX), (float)((double)regionY - this.viewEntityY), (float)((double)regionZ - this.viewEntityZ));
    }
}
