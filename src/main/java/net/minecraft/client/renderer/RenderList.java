package net.minecraft.client.renderer;

import java.nio.IntBuffer;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.src.Config;
import net.minecraft.util.EnumWorldBlockLayer;
import org.lwjgl.opengl.GL11;

public class RenderList extends ChunkRenderContainer
{
    private double viewEntityX;
    private double viewEntityY;
    private double viewEntityZ;
    IntBuffer bufferLists = GLAllocation.createDirectIntBuffer(16);

    public void renderChunkLayer(EnumWorldBlockLayer layer)
    {
        if (this.initialized)
        {
            if (!Config.isRenderRegions())
            {
                for (RenderChunk renderchunk1 : this.renderChunks)
                {
                    ListedRenderChunk listedrenderchunk1 = (ListedRenderChunk)renderchunk1;
                    GlStateManager.pushMatrix();
                    this.preRenderChunk(renderchunk1);
                    GL11.glCallList(listedrenderchunk1.getDisplayList(layer, listedrenderchunk1.getCompiledChunk()));
                    GlStateManager.popMatrix();
                }
            }
            else
            {
                int currentRegionX = Integer.MIN_VALUE;
                int currentRegionZ = Integer.MIN_VALUE;

                for (RenderChunk renderChunk : this.renderChunks)
                {
                    ListedRenderChunk listedRenderChunk = (ListedRenderChunk)renderChunk;

                    if (currentRegionX != renderChunk.regionX || currentRegionZ != renderChunk.regionZ)
                    {
                        if (this.bufferLists.position() > 0)
                        {
                            this.drawRegion(currentRegionX, currentRegionZ, this.bufferLists);
                        }

                        currentRegionX = renderChunk.regionX;
                        currentRegionZ = renderChunk.regionZ;
                    }

                    if (this.bufferLists.position() >= this.bufferLists.capacity())
                    {
                        IntBuffer intBuffer = GLAllocation.createDirectIntBuffer(this.bufferLists.capacity() * 2);
                        this.bufferLists.flip();
                        intBuffer.put(this.bufferLists);
                        this.bufferLists = intBuffer;
                    }

                    this.bufferLists.put(listedRenderChunk.getDisplayList(layer, listedRenderChunk.getCompiledChunk()));
                }

                if (this.bufferLists.position() > 0)
                {
                    this.drawRegion(currentRegionX, currentRegionZ, this.bufferLists);
                }
            }

            if (Config.isMultiTexture())
            {
                GlStateManager.bindCurrentTexture();
            }

            GlStateManager.resetColor();
            this.renderChunks.clear();
        }
    }

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn)
    {
        this.viewEntityX = viewEntityXIn;
        this.viewEntityY = viewEntityYIn;
        this.viewEntityZ = viewEntityZIn;
        super.initialize(viewEntityXIn, viewEntityYIn, viewEntityZIn);
    }

    private void drawRegion(int regionX, int regionZ, IntBuffer displayListBuffer)
    {
        GlStateManager.pushMatrix();
        this.preRenderRegion(regionX, 0, regionZ);
        displayListBuffer.flip();
        GlStateManager.callLists(displayListBuffer);
        displayListBuffer.clear();
        GlStateManager.popMatrix();
    }

    public void preRenderRegion(int regionX, int regionY, int regionZ)
    {
        GlStateManager.translate((float)((double)regionX - this.viewEntityX), (float)((double)regionY - this.viewEntityY), (float)((double)regionZ - this.viewEntityZ));
    }
}
