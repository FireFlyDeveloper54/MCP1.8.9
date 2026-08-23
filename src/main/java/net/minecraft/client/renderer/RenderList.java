package net.minecraft.client.renderer;

import net.minecraft.util.EnumWorldBlockLayer;

public class RenderList extends ChunkRenderContainer
{
    public void renderChunkLayer(EnumWorldBlockLayer layer)
    {
        if (this.initialized)
        {
            GlStateManager.resetColor();
            this.renderChunks.clear();
        }
    }
}
