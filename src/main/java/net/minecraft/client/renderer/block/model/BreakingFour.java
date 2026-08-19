package net.minecraft.client.renderer.block.model;

import java.util.Arrays;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BreakingFour extends BakedQuad
{
    private final TextureAtlasSprite texture;

    public BreakingFour(BakedQuad quad, TextureAtlasSprite textureIn)
    {
        super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.tintIndex, FaceBakery.getFacingFromVertexData(quad.getVertexData()));
        this.texture = textureIn;
        this.remapQuad();
        this.fixVertexData();
    }

    private void remapQuad()
    {
        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            this.remapVert(vertexIndex);
        }
    }

    private void remapVert(int vertex)
    {
        int vertexStride = this.vertexData.length / 4;
        int vertexOffset = vertexStride * vertex;
        float vertexX = Float.intBitsToFloat(this.vertexData[vertexOffset]);
        float vertexY = Float.intBitsToFloat(this.vertexData[vertexOffset + 1]);
        float vertexZ = Float.intBitsToFloat(this.vertexData[vertexOffset + 2]);
        float mappedU = 0.0F;
        float mappedV = 0.0F;

        switch (this.face)
        {
            case DOWN:
                mappedU = vertexX * 16.0F;
                mappedV = (1.0F - vertexZ) * 16.0F;
                break;

            case UP:
                mappedU = vertexX * 16.0F;
                mappedV = vertexZ * 16.0F;
                break;

            case NORTH:
                mappedU = (1.0F - vertexX) * 16.0F;
                mappedV = (1.0F - vertexY) * 16.0F;
                break;

            case SOUTH:
                mappedU = vertexX * 16.0F;
                mappedV = (1.0F - vertexY) * 16.0F;
                break;

            case WEST:
                mappedU = vertexZ * 16.0F;
                mappedV = (1.0F - vertexY) * 16.0F;
                break;

            case EAST:
                mappedU = (1.0F - vertexZ) * 16.0F;
                mappedV = (1.0F - vertexY) * 16.0F;
        }

        this.vertexData[vertexOffset + 4] = Float.floatToRawIntBits(this.texture.getInterpolatedU((double)mappedU));
        this.vertexData[vertexOffset + 4 + 1] = Float.floatToRawIntBits(this.texture.getInterpolatedV((double)mappedV));
    }
}
