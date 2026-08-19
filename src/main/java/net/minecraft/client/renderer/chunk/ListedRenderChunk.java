package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class ListedRenderChunk extends RenderChunk
{
    private final int baseDisplayList = GLAllocation.generateDisplayLists(RenderChunk.ENUM_WORLD_BLOCK_LAYERS.length);

    public ListedRenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos pos, int indexIn)
    {
        super(worldIn, renderGlobalIn, pos, indexIn);
    }

    public int getDisplayList(EnumWorldBlockLayer layer, CompiledChunk compiledChunk)
    {
        return !compiledChunk.isLayerEmpty(layer) ? this.baseDisplayList + layer.ordinal() : -1;
    }

    public void deleteGlResources()
    {
        super.deleteGlResources();
        GLAllocation.deleteDisplayLists(this.baseDisplayList, RenderChunk.ENUM_WORLD_BLOCK_LAYERS.length);
    }
}
