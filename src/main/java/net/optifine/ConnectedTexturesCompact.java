package net.optifine;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.optifine.render.RenderEnv;

public class ConnectedTexturesCompact
{
    private static final int COMPACT_NONE = 0;
    private static final int COMPACT_ALL = 1;
    private static final int COMPACT_V = 2;
    private static final int COMPACT_H = 3;
    private static final int COMPACT_HV = 4;

    public static BakedQuad[] getConnectedTextureCtmCompact(int ctmIndex, ConnectedProperties cp, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        if (cp.ctmTileIndexes != null && ctmIndex >= 0 && ctmIndex < cp.ctmTileIndexes.length)
        {
            int tileIndex = cp.ctmTileIndexes[ctmIndex];

            if (tileIndex >= 0 && tileIndex <= cp.tileIcons.length)
            {
                return getQuadsCompact(tileIndex, cp.tileIcons, quad, renderEnv);
            }
        }

        switch (ctmIndex)
        {
            case 1:
                return getQuadsCompactH(0, 3, cp.tileIcons, side, quad, renderEnv);

            case 2:
                return getQuadsCompact(3, cp.tileIcons, quad, renderEnv);

            case 3:
                return getQuadsCompactH(3, 0, cp.tileIcons, side, quad, renderEnv);

            case 4:
                return getQuadsCompact4(0, 3, 2, 4, cp.tileIcons, side, quad, renderEnv);

            case 5:
                return getQuadsCompact4(3, 0, 4, 2, cp.tileIcons, side, quad, renderEnv);

            case 6:
                return getQuadsCompact4(2, 4, 2, 4, cp.tileIcons, side, quad, renderEnv);

            case 7:
                return getQuadsCompact4(3, 3, 4, 4, cp.tileIcons, side, quad, renderEnv);

            case 8:
                return getQuadsCompact4(4, 1, 4, 4, cp.tileIcons, side, quad, renderEnv);

            case 9:
                return getQuadsCompact4(4, 4, 4, 1, cp.tileIcons, side, quad, renderEnv);

            case 10:
                return getQuadsCompact4(1, 4, 1, 4, cp.tileIcons, side, quad, renderEnv);

            case 11:
                return getQuadsCompact4(1, 1, 4, 4, cp.tileIcons, side, quad, renderEnv);

            case 12:
                return getQuadsCompactV(0, 2, cp.tileIcons, side, quad, renderEnv);

            case 13:
                return getQuadsCompact4(0, 3, 2, 1, cp.tileIcons, side, quad, renderEnv);

            case 14:
                return getQuadsCompactV(3, 1, cp.tileIcons, side, quad, renderEnv);

            case 15:
                return getQuadsCompact4(3, 0, 1, 2, cp.tileIcons, side, quad, renderEnv);

            case 16:
                return getQuadsCompact4(2, 4, 0, 3, cp.tileIcons, side, quad, renderEnv);

            case 17:
                return getQuadsCompact4(4, 2, 3, 0, cp.tileIcons, side, quad, renderEnv);

            case 18:
                return getQuadsCompact4(4, 4, 3, 3, cp.tileIcons, side, quad, renderEnv);

            case 19:
                return getQuadsCompact4(4, 2, 4, 2, cp.tileIcons, side, quad, renderEnv);

            case 20:
                return getQuadsCompact4(1, 4, 4, 4, cp.tileIcons, side, quad, renderEnv);

            case 21:
                return getQuadsCompact4(4, 4, 1, 4, cp.tileIcons, side, quad, renderEnv);

            case 22:
                return getQuadsCompact4(4, 4, 1, 1, cp.tileIcons, side, quad, renderEnv);

            case 23:
                return getQuadsCompact4(4, 1, 4, 1, cp.tileIcons, side, quad, renderEnv);

            case 24:
                return getQuadsCompact(2, cp.tileIcons, quad, renderEnv);

            case 25:
                return getQuadsCompactH(2, 1, cp.tileIcons, side, quad, renderEnv);

            case 26:
                return getQuadsCompact(1, cp.tileIcons, quad, renderEnv);

            case 27:
                return getQuadsCompactH(1, 2, cp.tileIcons, side, quad, renderEnv);

            case 28:
                return getQuadsCompact4(2, 4, 2, 1, cp.tileIcons, side, quad, renderEnv);

            case 29:
                return getQuadsCompact4(3, 3, 1, 4, cp.tileIcons, side, quad, renderEnv);

            case 30:
                return getQuadsCompact4(2, 1, 2, 4, cp.tileIcons, side, quad, renderEnv);

            case 31:
                return getQuadsCompact4(3, 3, 4, 1, cp.tileIcons, side, quad, renderEnv);

            case 32:
                return getQuadsCompact4(1, 1, 1, 4, cp.tileIcons, side, quad, renderEnv);

            case 33:
                return getQuadsCompact4(1, 1, 4, 1, cp.tileIcons, side, quad, renderEnv);

            case 34:
                return getQuadsCompact4(4, 1, 1, 4, cp.tileIcons, side, quad, renderEnv);

            case 35:
                return getQuadsCompact4(1, 4, 4, 1, cp.tileIcons, side, quad, renderEnv);

            case 36:
                return getQuadsCompactV(2, 0, cp.tileIcons, side, quad, renderEnv);

            case 37:
                return getQuadsCompact4(2, 1, 0, 3, cp.tileIcons, side, quad, renderEnv);

            case 38:
                return getQuadsCompactV(1, 3, cp.tileIcons, side, quad, renderEnv);

            case 39:
                return getQuadsCompact4(1, 2, 3, 0, cp.tileIcons, side, quad, renderEnv);

            case 40:
                return getQuadsCompact4(4, 1, 3, 3, cp.tileIcons, side, quad, renderEnv);

            case 41:
                return getQuadsCompact4(1, 2, 4, 2, cp.tileIcons, side, quad, renderEnv);

            case 42:
                return getQuadsCompact4(1, 4, 3, 3, cp.tileIcons, side, quad, renderEnv);

            case 43:
                return getQuadsCompact4(4, 2, 1, 2, cp.tileIcons, side, quad, renderEnv);

            case 44:
                return getQuadsCompact4(1, 4, 1, 1, cp.tileIcons, side, quad, renderEnv);

            case 45:
                return getQuadsCompact4(4, 1, 1, 1, cp.tileIcons, side, quad, renderEnv);

            case 46:
                return getQuadsCompact(4, cp.tileIcons, quad, renderEnv);

            default:
                return getQuadsCompact(0, cp.tileIcons, quad, renderEnv);
        }
    }

    private static BakedQuad[] getQuadsCompactH(int indexLeft, int indexRight, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        return getQuadsCompact(ConnectedTexturesCompact.Dir.LEFT, indexLeft, ConnectedTexturesCompact.Dir.RIGHT, indexRight, sprites, side, quad, renderEnv);
    }

    private static BakedQuad[] getQuadsCompactV(int indexUp, int indexDown, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        return getQuadsCompact(ConnectedTexturesCompact.Dir.UP, indexUp, ConnectedTexturesCompact.Dir.DOWN, indexDown, sprites, side, quad, renderEnv);
    }

    private static BakedQuad[] getQuadsCompact4(int upLeft, int upRight, int downLeft, int downRight, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        return upLeft == upRight ? (downLeft == downRight ? getQuadsCompact(ConnectedTexturesCompact.Dir.UP, upLeft, ConnectedTexturesCompact.Dir.DOWN, downLeft, sprites, side, quad, renderEnv) : getQuadsCompact(ConnectedTexturesCompact.Dir.UP, upLeft, ConnectedTexturesCompact.Dir.DOWN_LEFT, downLeft, ConnectedTexturesCompact.Dir.DOWN_RIGHT, downRight, sprites, side, quad, renderEnv)) : (downLeft == downRight ? getQuadsCompact(ConnectedTexturesCompact.Dir.UP_LEFT, upLeft, ConnectedTexturesCompact.Dir.UP_RIGHT, upRight, ConnectedTexturesCompact.Dir.DOWN, downLeft, sprites, side, quad, renderEnv) : (upLeft == downLeft ? (upRight == downRight ? getQuadsCompact(ConnectedTexturesCompact.Dir.LEFT, upLeft, ConnectedTexturesCompact.Dir.RIGHT, upRight, sprites, side, quad, renderEnv) : getQuadsCompact(ConnectedTexturesCompact.Dir.LEFT, upLeft, ConnectedTexturesCompact.Dir.UP_RIGHT, upRight, ConnectedTexturesCompact.Dir.DOWN_RIGHT, downRight, sprites, side, quad, renderEnv)) : (upRight == downRight ? getQuadsCompact(ConnectedTexturesCompact.Dir.UP_LEFT, upLeft, ConnectedTexturesCompact.Dir.DOWN_LEFT, downLeft, ConnectedTexturesCompact.Dir.RIGHT, upRight, sprites, side, quad, renderEnv) : getQuadsCompact(ConnectedTexturesCompact.Dir.UP_LEFT, upLeft, ConnectedTexturesCompact.Dir.UP_RIGHT, upRight, ConnectedTexturesCompact.Dir.DOWN_LEFT, downLeft, ConnectedTexturesCompact.Dir.DOWN_RIGHT, downRight, sprites, side, quad, renderEnv))));
    }

    private static BakedQuad[] getQuadsCompact(int index, TextureAtlasSprite[] sprites, BakedQuad quad, RenderEnv renderEnv)
    {
        TextureAtlasSprite sprite = sprites[index];
        return ConnectedTextures.getQuads(sprite, quad, renderEnv);
    }

    private static BakedQuad[] getQuadsCompact(ConnectedTexturesCompact.Dir thirdDir, int index1, ConnectedTexturesCompact.Dir fifthDir, int index2, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        BakedQuad firstQuad = getQuadCompact(sprites[index1], thirdDir, side, quad, renderEnv);
        BakedQuad secondQuad = getQuadCompact(sprites[index2], fifthDir, side, quad, renderEnv);
        return renderEnv.getArrayQuadsCtm(firstQuad, secondQuad);
    }

    private static BakedQuad[] getQuadsCompact(ConnectedTexturesCompact.Dir fourthDir, int index1, ConnectedTexturesCompact.Dir sixthDir, int index2, ConnectedTexturesCompact.Dir ninthDir, int index3, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        BakedQuad firstQuad = getQuadCompact(sprites[index1], fourthDir, side, quad, renderEnv);
        BakedQuad secondQuad = getQuadCompact(sprites[index2], sixthDir, side, quad, renderEnv);
        BakedQuad thirdQuad = getQuadCompact(sprites[index3], ninthDir, side, quad, renderEnv);
        return renderEnv.getArrayQuadsCtm(firstQuad, secondQuad, thirdQuad);
    }

    private static BakedQuad[] getQuadsCompact(ConnectedTexturesCompact.Dir secondDir, int index1, ConnectedTexturesCompact.Dir seventhDir, int index2, ConnectedTexturesCompact.Dir eighthDir, int index3, ConnectedTexturesCompact.Dir tenthDir, int index4, TextureAtlasSprite[] sprites, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        BakedQuad firstQuad = getQuadCompact(sprites[index1], secondDir, side, quad, renderEnv);
        BakedQuad secondQuad = getQuadCompact(sprites[index2], seventhDir, side, quad, renderEnv);
        BakedQuad thirdQuad = getQuadCompact(sprites[index3], eighthDir, side, quad, renderEnv);
        BakedQuad fourthQuad = getQuadCompact(sprites[index4], tenthDir, side, quad, renderEnv);
        return renderEnv.getArrayQuadsCtm(firstQuad, secondQuad, thirdQuad, fourthQuad);
    }

    private static BakedQuad getQuadCompact(TextureAtlasSprite sprite, ConnectedTexturesCompact.Dir dir, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        switch (dir)
        {
            case UP:
                return getQuadCompact(sprite, dir, 0, 0, 16, 8, side, quad, renderEnv);

            case UP_RIGHT:
                return getQuadCompact(sprite, dir, 8, 0, 16, 8, side, quad, renderEnv);

            case RIGHT:
                return getQuadCompact(sprite, dir, 8, 0, 16, 16, side, quad, renderEnv);

            case DOWN_RIGHT:
                return getQuadCompact(sprite, dir, 8, 8, 16, 16, side, quad, renderEnv);

            case DOWN:
                return getQuadCompact(sprite, dir, 0, 8, 16, 16, side, quad, renderEnv);

            case DOWN_LEFT:
                return getQuadCompact(sprite, dir, 0, 8, 8, 16, side, quad, renderEnv);

            case LEFT:
                return getQuadCompact(sprite, dir, 0, 0, 8, 16, side, quad, renderEnv);

            case UP_LEFT:
                return getQuadCompact(sprite, dir, 0, 0, 8, 8, side, quad, renderEnv);

            default:
                return quad;
        }
    }

    private static BakedQuad getQuadCompact(TextureAtlasSprite sprite, ConnectedTexturesCompact.Dir dir, int x1, int y1, int x2, int y2, int side, BakedQuad quadIn, RenderEnv renderEnv)
    {
        Map[][] spriteQuadMaps = ConnectedTextures.getSpriteQuadCompactMaps();

        if (spriteQuadMaps == null)
        {
            return quadIn;
        }
        else
        {
            int spriteIndex = sprite.getIndexInMap();

            if (spriteIndex >= 0 && spriteIndex < spriteQuadMaps.length)
            {
                Map[] directionMaps = spriteQuadMaps[spriteIndex];

                if (directionMaps == null)
                {
                    directionMaps = new Map[ConnectedTexturesCompact.Dir.VALUES.length];
                    spriteQuadMaps[spriteIndex] = directionMaps;
                }

                Map<BakedQuad, BakedQuad> quadMap = directionMaps[dir.ordinal()];

                if (quadMap == null)
                {
                    quadMap = new IdentityHashMap(1);
                    directionMaps[dir.ordinal()] = quadMap;
                }

                BakedQuad compactQuad = (BakedQuad)quadMap.get(quadIn);

                if (compactQuad == null)
                {
                    compactQuad = makeSpriteQuadCompact(quadIn, sprite, side, x1, y1, x2, y2);
                    quadMap.put(quadIn, compactQuad);
                }

                return compactQuad;
            }
            else
            {
                return quadIn;
            }
        }
    }

    private static BakedQuad makeSpriteQuadCompact(BakedQuad quad, TextureAtlasSprite sprite, int side, int x1, int y1, int x2, int y2)
    {
        int[] vertexData = (int[])quad.getVertexData().clone();
        TextureAtlasSprite sourceSprite = quad.getSprite();

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            fixVertexCompact(vertexData, vertexIndex, sourceSprite, sprite, side, x1, y1, x2, y2);
        }

        BakedQuad compactQuad = new BakedQuad(vertexData, quad.getTintIndex(), quad.getFace(), sprite);
        return compactQuad;
    }

    private static void fixVertexCompact(int[] data, int vertex, TextureAtlasSprite spriteFrom, TextureAtlasSprite spriteTo, int side, int x1, int y1, int x2, int y2)
    {
        int vertexStride = data.length / 4;
        int vertexOffset = vertexStride * vertex;
        float vertexU = Float.intBitsToFloat(data[vertexOffset + 4]);
        float vertexV = Float.intBitsToFloat(data[vertexOffset + 4 + 1]);
        double spriteU16 = spriteFrom.getSpriteU16(vertexU);
        double spriteV16 = spriteFrom.getSpriteV16(vertexV);
        float posX = Float.intBitsToFloat(data[vertexOffset + 0]);
        float posY = Float.intBitsToFloat(data[vertexOffset + 1]);
        float posZ = Float.intBitsToFloat(data[vertexOffset + 2]);
        float sideU;
        float sideV;

        switch (side)
        {
            case 0:
                sideU = posX;
                sideV = 1.0F - posZ;
                break;

            case 1:
                sideU = posX;
                sideV = posZ;
                break;

            case 2:
                sideU = 1.0F - posX;
                sideV = 1.0F - posY;
                break;

            case 3:
                sideU = posX;
                sideV = 1.0F - posY;
                break;

            case 4:
                sideU = posZ;
                sideV = 1.0F - posY;
                break;

            case 5:
                sideU = 1.0F - posZ;
                sideV = 1.0F - posY;
                break;

            default:
                return;
        }

        float maxU16 = 15.968F;
        float maxV16 = 15.968F;

        if (spriteU16 < (double)x1)
        {
            sideU = (float)((double)sideU + ((double)x1 - spriteU16) / (double)maxU16);
            spriteU16 = (double)x1;
        }

        if (spriteU16 > (double)x2)
        {
            sideU = (float)((double)sideU - (spriteU16 - (double)x2) / (double)maxU16);
            spriteU16 = (double)x2;
        }

        if (spriteV16 < (double)y1)
        {
            sideV = (float)((double)sideV + ((double)y1 - spriteV16) / (double)maxV16);
            spriteV16 = (double)y1;
        }

        if (spriteV16 > (double)y2)
        {
            sideV = (float)((double)sideV - (spriteV16 - (double)y2) / (double)maxV16);
            spriteV16 = (double)y2;
        }

        switch (side)
        {
            case 0:
                posX = sideU;
                posZ = 1.0F - sideV;
                break;

            case 1:
                posX = sideU;
                posZ = sideV;
                break;

            case 2:
                posX = 1.0F - sideU;
                posY = 1.0F - sideV;
                break;

            case 3:
                posX = sideU;
                posY = 1.0F - sideV;
                break;

            case 4:
                posZ = sideU;
                posY = 1.0F - sideV;
                break;

            case 5:
                posZ = 1.0F - sideU;
                posY = 1.0F - sideV;
                break;

            default:
                return;
        }

        data[vertexOffset + 4] = Float.floatToRawIntBits(spriteTo.getInterpolatedU(spriteU16));
        data[vertexOffset + 4 + 1] = Float.floatToRawIntBits(spriteTo.getInterpolatedV(spriteV16));
        data[vertexOffset + 0] = Float.floatToRawIntBits(posX);
        data[vertexOffset + 1] = Float.floatToRawIntBits(posY);
        data[vertexOffset + 2] = Float.floatToRawIntBits(posZ);
    }

    private static enum Dir
    {
        UP,
        UP_RIGHT,
        RIGHT,
        DOWN_RIGHT,
        DOWN,
        DOWN_LEFT,
        LEFT,
        UP_LEFT;

        public static final ConnectedTexturesCompact.Dir[] VALUES = values();
    }
}
