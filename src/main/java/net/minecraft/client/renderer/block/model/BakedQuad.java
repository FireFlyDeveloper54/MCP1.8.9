package net.minecraft.client.renderer.block.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.src.Config;
import net.minecraft.util.EnumFacing;
import net.optifine.model.QuadBounds;

public class BakedQuad
{
    protected int[] vertexData;
    protected final int tintIndex;
    protected EnumFacing face;
    protected TextureAtlasSprite sprite;
    private int[] vertexDataSingle = null;
    private QuadBounds quadBounds;
    private boolean quadEmissiveChecked;
    private BakedQuad quadEmissive;

    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn)
    {
        this.vertexData = vertexDataIn;
        this.tintIndex = tintIndexIn;
        this.face = faceIn;
        this.sprite = spriteIn;
        this.fixVertexData();
    }

    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn)
    {
        this.vertexData = vertexDataIn;
        this.tintIndex = tintIndexIn;
        this.face = faceIn;
        this.fixVertexData();
    }

    public TextureAtlasSprite getSprite()
    {
        if (this.sprite == null)
        {
            this.sprite = getSpriteByUv(this.getVertexData());
        }

        return this.sprite;
    }

    public int[] getVertexData()
    {
        this.fixVertexData();
        return this.vertexData;
    }

    public boolean hasTintIndex()
    {
        return this.tintIndex != -1;
    }

    public int getTintIndex()
    {
        return this.tintIndex;
    }

    public EnumFacing getFace()
    {
        if (this.face == null)
        {
            this.face = FaceBakery.getFacingFromVertexData(this.getVertexData());
        }

        return this.face;
    }

    public int[] getVertexDataSingle()
    {
        if (this.vertexDataSingle == null)
        {
            this.vertexDataSingle = makeVertexDataSingle(this.getVertexData(), this.getSprite());
        }

        return this.vertexDataSingle;
    }

    private static int[] makeVertexDataSingle(int[] vertexData, TextureAtlasSprite sprite)
    {
        int[] singleVertexData = (int[])vertexData.clone();
        int vertexStride = singleVertexData.length / 4;

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int vertexOffset = vertexIndex * vertexStride;
            float atlasU = Float.intBitsToFloat(singleVertexData[vertexOffset + 4]);
            float atlasV = Float.intBitsToFloat(singleVertexData[vertexOffset + 4 + 1]);
            float singleU = sprite.toSingleU(atlasU);
            float singleV = sprite.toSingleV(atlasV);
            singleVertexData[vertexOffset + 4] = Float.floatToRawIntBits(singleU);
            singleVertexData[vertexOffset + 4 + 1] = Float.floatToRawIntBits(singleV);
        }

        return singleVertexData;
    }

    private static TextureAtlasSprite getSpriteByUv(int[] vertexData)
    {
        float minU = 1.0F;
        float minV = 1.0F;
        float maxU = 0.0F;
        float maxV = 0.0F;
        int vertexStride = vertexData.length / 4;

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int vertexOffset = vertexIndex * vertexStride;
            float vertexU = Float.intBitsToFloat(vertexData[vertexOffset + 4]);
            float vertexV = Float.intBitsToFloat(vertexData[vertexOffset + 4 + 1]);
            minU = Math.min(minU, vertexU);
            minV = Math.min(minV, vertexV);
            maxU = Math.max(maxU, vertexU);
            maxV = Math.max(maxV, vertexV);
        }

        float centerU = (minU + maxU) / 2.0F;
        float centerV = (minV + maxV) / 2.0F;
        TextureAtlasSprite atlasSprite = Minecraft.getMinecraft().getTextureMapBlocks().getIconByUV((double)centerU, (double)centerV);
        return atlasSprite;
    }

    protected void fixVertexData()
    {
        if (Config.isShaders())
        {
            if (this.vertexData.length == 28)
            {
                this.vertexData = expandVertexData(this.vertexData);
            }
        }
        else if (this.vertexData.length == 56)
        {
            this.vertexData = compactVertexData(this.vertexData);
        }
    }

    private static int[] expandVertexData(int[] vertexData)
    {
        int sourceStride = vertexData.length / 4;
        int expandedStride = sourceStride * 2;
        int[] expandedVertexData = new int[expandedStride * 4];

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            System.arraycopy(vertexData, vertexIndex * sourceStride, expandedVertexData, vertexIndex * expandedStride, sourceStride);
        }

        return expandedVertexData;
    }

    private static int[] compactVertexData(int[] vertexData)
    {
        int sourceStride = vertexData.length / 4;
        int compactStride = sourceStride / 2;
        int[] compactVertexData = new int[compactStride * 4];

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            System.arraycopy(vertexData, vertexIndex * sourceStride, compactVertexData, vertexIndex * compactStride, compactStride);
        }

        return compactVertexData;
    }

    public QuadBounds getQuadBounds()
    {
        if (this.quadBounds == null)
        {
            this.quadBounds = new QuadBounds(this.getVertexData());
        }

        return this.quadBounds;
    }

    public float getMidX()
    {
        QuadBounds quadBounds = this.getQuadBounds();
        return (quadBounds.getMaxX() + quadBounds.getMinX()) / 2.0F;
    }

    public double getMidY()
    {
        QuadBounds quadBounds = this.getQuadBounds();
        return (double)((quadBounds.getMaxY() + quadBounds.getMinY()) / 2.0F);
    }

    public double getMidZ()
    {
        QuadBounds quadBounds = this.getQuadBounds();
        return (double)((quadBounds.getMaxZ() + quadBounds.getMinZ()) / 2.0F);
    }

    public boolean isFaceQuad()
    {
        QuadBounds quadBounds = this.getQuadBounds();
        return quadBounds.isFaceQuad(this.face);
    }

    public boolean isFullQuad()
    {
        QuadBounds quadBounds = this.getQuadBounds();
        return quadBounds.isFullQuad(this.face);
    }

    public boolean isFullFaceQuad()
    {
        return this.isFullQuad() && this.isFaceQuad();
    }

    public BakedQuad getQuadEmissive()
    {
        if (this.quadEmissiveChecked)
        {
            return this.quadEmissive;
        }
        else
        {
            if (this.quadEmissive == null && this.sprite != null && this.sprite.spriteEmissive != null)
            {
                this.quadEmissive = new BreakingFour(this, this.sprite.spriteEmissive);
            }

            this.quadEmissiveChecked = true;
            return this.quadEmissive;
        }
    }

    public String toString()
    {
        return "vertex: " + this.vertexData.length / 7 + ", tint: " + this.tintIndex + ", facing: " + this.face + ", sprite: " + this.sprite;
    }
}
