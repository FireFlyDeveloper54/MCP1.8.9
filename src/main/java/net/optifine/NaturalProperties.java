package net.optifine;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.src.Config;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class NaturalProperties
{
    public int rotation = 1;
    public boolean flip = false;
    private Map[] quadMaps = new Map[8];

    public NaturalProperties(String type)
    {
        if (type.equals("4"))
        {
            this.rotation = 4;
        }
        else if (type.equals("2"))
        {
            this.rotation = 2;
        }
        else if (type.equals("F"))
        {
            this.flip = true;
        }
        else if (type.equals("4F"))
        {
            this.rotation = 4;
            this.flip = true;
        }
        else if (type.equals("2F"))
        {
            this.rotation = 2;
            this.flip = true;
        }
        else
        {
            Config.warn("NaturalTextures: Unknown type: " + type);
        }
    }

    public boolean isValid()
    {
        return this.rotation != 2 && this.rotation != 4 ? this.flip : true;
    }

    public synchronized BakedQuad getQuad(BakedQuad quadIn, int rotate, boolean flipU)
    {
        int transformIndex = rotate;

        if (flipU)
        {
            transformIndex = rotate | 4;
        }

        if (transformIndex > 0 && transformIndex < this.quadMaps.length)
        {
            Map quadMap = this.quadMaps[transformIndex];

            if (quadMap == null)
            {
                quadMap = new IdentityHashMap(1);
                this.quadMaps[transformIndex] = quadMap;
            }

            BakedQuad transformedQuad = (BakedQuad)quadMap.get(quadIn);

            if (transformedQuad == null)
            {
                transformedQuad = this.makeQuad(quadIn, rotate, flipU);
                quadMap.put(quadIn, transformedQuad);
            }

            return transformedQuad;
        }
        else
        {
            return quadIn;
        }
    }

    private BakedQuad makeQuad(BakedQuad quad, int rotate, boolean flipU)
    {
        int[] vertexData = quad.getVertexData();
        int tintIndex = quad.getTintIndex();
        EnumFacing facing = quad.getFace();
        TextureAtlasSprite sprite = quad.getSprite();

        if (!this.isFullSprite(quad))
        {
            rotate = 0;
        }

        vertexData = this.transformVertexData(vertexData, rotate, flipU);
        BakedQuad transformedQuad = new BakedQuad(vertexData, tintIndex, facing, sprite);
        return transformedQuad;
    }

    private int[] transformVertexData(int[] vertexData, int rotate, boolean flipU)
    {
        int[] transformedData = (int[])vertexData.clone();
        int targetVertex = 4 - rotate;

        if (flipU)
        {
            targetVertex += 3;
        }

        targetVertex = targetVertex % 4;
        int vertexStride = transformedData.length / 4;

        for (int sourceVertex = 0; sourceVertex < 4; ++sourceVertex)
        {
            int sourceOffset = sourceVertex * vertexStride;
            int targetOffset = targetVertex * vertexStride;
            transformedData[targetOffset + 4] = vertexData[sourceOffset + 4];
            transformedData[targetOffset + 4 + 1] = vertexData[sourceOffset + 4 + 1];

            if (flipU)
            {
                --targetVertex;

                if (targetVertex < 0)
                {
                    targetVertex = 3;
                }
            }
            else
            {
                ++targetVertex;

                if (targetVertex > 3)
                {
                    targetVertex = 0;
                }
            }
        }

        return transformedData;
    }

    private boolean isFullSprite(BakedQuad quad)
    {
        TextureAtlasSprite sprite = quad.getSprite();
        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float deltaU = maxU - minU;
        float deltaUMax = deltaU / 256.0F;
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();
        float deltaV = maxV - minV;
        float deltaVMax = deltaV / 256.0F;
        int[] vertexData = quad.getVertexData();
        int vertexStride = vertexData.length / 4;

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int vertexOffset = vertexIndex * vertexStride;
            float vertexU = Float.intBitsToFloat(vertexData[vertexOffset + 4]);
            float vertexV = Float.intBitsToFloat(vertexData[vertexOffset + 4 + 1]);

            if (!this.equalsDelta(vertexU, minU, deltaUMax) && !this.equalsDelta(vertexU, maxU, deltaUMax))
            {
                return false;
            }

            if (!this.equalsDelta(vertexV, minV, deltaVMax) && !this.equalsDelta(vertexV, maxV, deltaVMax))
            {
                return false;
            }
        }

        return true;
    }

    private boolean equalsDelta(float floatValue, float secondFloatValue, float deltaMax)
    {
        float delta = MathHelper.abs(floatValue - secondFloatValue);
        return delta < deltaMax;
    }
}
