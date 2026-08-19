package net.optifine.model;

import net.minecraft.util.EnumFacing;

public class QuadBounds
{
    private float minX = Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE;
    private float minZ = Float.MAX_VALUE;
    private float maxX = -3.4028235E38F;
    private float maxY = -3.4028235E38F;
    private float maxZ = -3.4028235E38F;

    public QuadBounds(int[] vertexData)
    {
        int vertexStride = vertexData.length / 4;

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int vertexOffset = vertexIndex * vertexStride;
            float x = Float.intBitsToFloat(vertexData[vertexOffset + 0]);
            float y = Float.intBitsToFloat(vertexData[vertexOffset + 1]);
            float z = Float.intBitsToFloat(vertexData[vertexOffset + 2]);

            if (this.minX > x)
            {
                this.minX = x;
            }

            if (this.minY > y)
            {
                this.minY = y;
            }

            if (this.minZ > z)
            {
                this.minZ = z;
            }

            if (this.maxX < x)
            {
                this.maxX = x;
            }

            if (this.maxY < y)
            {
                this.maxY = y;
            }

            if (this.maxZ < z)
            {
                this.maxZ = z;
            }
        }
    }

    public float getMinX()
    {
        return this.minX;
    }

    public float getMinY()
    {
        return this.minY;
    }

    public float getMinZ()
    {
        return this.minZ;
    }

    public float getMaxX()
    {
        return this.maxX;
    }

    public float getMaxY()
    {
        return this.maxY;
    }

    public float getMaxZ()
    {
        return this.maxZ;
    }

    public boolean isFaceQuad(EnumFacing face)
    {
        float min;
        float max;
        float expected;

        switch (face)
        {
            case DOWN:
                min = this.getMinY();
                max = this.getMaxY();
                expected = 0.0F;
                break;

            case UP:
                min = this.getMinY();
                max = this.getMaxY();
                expected = 1.0F;
                break;

            case NORTH:
                min = this.getMinZ();
                max = this.getMaxZ();
                expected = 0.0F;
                break;

            case SOUTH:
                min = this.getMinZ();
                max = this.getMaxZ();
                expected = 1.0F;
                break;

            case WEST:
                min = this.getMinX();
                max = this.getMaxX();
                expected = 0.0F;
                break;

            case EAST:
                min = this.getMinX();
                max = this.getMaxX();
                expected = 1.0F;
                break;

            default:
                return false;
        }

        return min == expected && max == expected;
    }

    public boolean isFullQuad(EnumFacing face)
    {
        float minU;
        float maxU;
        float minV;
        float maxV;

        switch (face)
        {
            case DOWN:
            case UP:
                minU = this.getMinX();
                maxU = this.getMaxX();
                minV = this.getMinZ();
                maxV = this.getMaxZ();
                break;

            case NORTH:
            case SOUTH:
                minU = this.getMinX();
                maxU = this.getMaxX();
                minV = this.getMinY();
                maxV = this.getMaxY();
                break;

            case WEST:
            case EAST:
                minU = this.getMinY();
                maxU = this.getMaxY();
                minV = this.getMinZ();
                maxV = this.getMaxZ();
                break;

            default:
                return false;
        }

        return minU == 0.0F && maxU == 1.0F && minV == 0.0F && maxV == 1.0F;
    }
}
