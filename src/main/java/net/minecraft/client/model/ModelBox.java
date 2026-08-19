package net.minecraft.client.model;

import net.minecraft.client.renderer.WorldRenderer;

public class ModelBox
{
    private PositionTextureVertex[] vertexPositions;
    private TexturedQuad[] quadList;
    public final float posX1;
    public final float posY1;
    public final float posZ1;
    public final float posX2;
    public final float posY2;
    public final float posZ2;
    public String boxName;

    public ModelBox(ModelRenderer renderer, int textureX, int textureY, float x, float y, float z, int width, int height, int depth, float scale)
    {
        this(renderer, textureX, textureY, x, y, z, width, height, depth, scale, renderer.mirror);
    }

    public ModelBox(ModelRenderer renderer, int[][] faceUvs, float x, float y, float z, float width, float height, float depth, float scale, boolean mirror)
    {
        this.posX1 = x;
        this.posY1 = y;
        this.posZ1 = z;
        this.posX2 = x + width;
        this.posY2 = y + height;
        this.posZ2 = z + depth;
        this.vertexPositions = new PositionTextureVertex[8];
        this.quadList = new TexturedQuad[6];
        float maxX = x + width;
        float maxY = y + height;
        float maxZ = z + depth;
        x = x - scale;
        y = y - scale;
        z = z - scale;
        maxX = maxX + scale;
        maxY = maxY + scale;
        maxZ = maxZ + scale;

        if (mirror)
        {
            float mirroredMaxX = maxX;
            maxX = x;
            x = mirroredMaxX;
        }

        PositionTextureVertex minXMinYMinZ = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
        PositionTextureVertex maxXMinYMinZ = new PositionTextureVertex(maxX, y, z, 0.0F, 8.0F);
        PositionTextureVertex maxXMaxYMinZ = new PositionTextureVertex(maxX, maxY, z, 8.0F, 8.0F);
        PositionTextureVertex minXMaxYMinZ = new PositionTextureVertex(x, maxY, z, 8.0F, 0.0F);
        PositionTextureVertex minXMinYMaxZ = new PositionTextureVertex(x, y, maxZ, 0.0F, 0.0F);
        PositionTextureVertex maxXMinYMaxZ = new PositionTextureVertex(maxX, y, maxZ, 0.0F, 8.0F);
        PositionTextureVertex maxXMaxYMaxZ = new PositionTextureVertex(maxX, maxY, maxZ, 8.0F, 8.0F);
        PositionTextureVertex minXMaxYMaxZ = new PositionTextureVertex(x, maxY, maxZ, 8.0F, 0.0F);
        this.vertexPositions[0] = minXMinYMinZ;
        this.vertexPositions[1] = maxXMinYMinZ;
        this.vertexPositions[2] = maxXMaxYMinZ;
        this.vertexPositions[3] = minXMaxYMinZ;
        this.vertexPositions[4] = minXMinYMaxZ;
        this.vertexPositions[5] = maxXMinYMaxZ;
        this.vertexPositions[6] = maxXMaxYMaxZ;
        this.vertexPositions[7] = minXMaxYMaxZ;
        this.quadList[0] = this.makeTexturedQuad(new PositionTextureVertex[] {maxXMinYMaxZ, maxXMinYMinZ, maxXMaxYMinZ, maxXMaxYMaxZ}, faceUvs[4], false, renderer.textureWidth, renderer.textureHeight);
        this.quadList[1] = this.makeTexturedQuad(new PositionTextureVertex[] {minXMinYMinZ, minXMinYMaxZ, minXMaxYMaxZ, minXMaxYMinZ}, faceUvs[5], false, renderer.textureWidth, renderer.textureHeight);
        this.quadList[2] = this.makeTexturedQuad(new PositionTextureVertex[] {maxXMinYMaxZ, minXMinYMaxZ, minXMinYMinZ, maxXMinYMinZ}, faceUvs[1], true, renderer.textureWidth, renderer.textureHeight);
        this.quadList[3] = this.makeTexturedQuad(new PositionTextureVertex[] {maxXMaxYMinZ, minXMaxYMinZ, minXMaxYMaxZ, maxXMaxYMaxZ}, faceUvs[0], true, renderer.textureWidth, renderer.textureHeight);
        this.quadList[4] = this.makeTexturedQuad(new PositionTextureVertex[] {maxXMinYMinZ, minXMinYMinZ, minXMaxYMinZ, maxXMaxYMinZ}, faceUvs[2], false, renderer.textureWidth, renderer.textureHeight);
        this.quadList[5] = this.makeTexturedQuad(new PositionTextureVertex[] {minXMinYMaxZ, maxXMinYMaxZ, maxXMaxYMaxZ, minXMaxYMaxZ}, faceUvs[3], false, renderer.textureWidth, renderer.textureHeight);

        if (mirror)
        {
            for (TexturedQuad texturedQuad : this.quadList)
            {
                texturedQuad.flipFace();
            }
        }
    }

    private TexturedQuad makeTexturedQuad(PositionTextureVertex[] vertices, int[] textureCoords, boolean flipTexture, float textureWidth, float textureHeight)
    {
        return textureCoords == null ? null : (flipTexture ? new TexturedQuad(vertices, textureCoords[2], textureCoords[3], textureCoords[0], textureCoords[1], textureWidth, textureHeight) : new TexturedQuad(vertices, textureCoords[0], textureCoords[1], textureCoords[2], textureCoords[3], textureWidth, textureHeight));
    }

    public ModelBox(ModelRenderer renderer, int textureX, int textureY, float x, float y, float z, int width, int height, int depth, float scale, boolean mirror)
    {
        this.posX1 = x;
        this.posY1 = y;
        this.posZ1 = z;
        this.posX2 = x + (float)width;
        this.posY2 = y + (float)height;
        this.posZ2 = z + (float)depth;
        this.vertexPositions = new PositionTextureVertex[8];
        this.quadList = new TexturedQuad[6];
        float maxX = x + (float)width;
        float maxY = y + (float)height;
        float maxZ = z + (float)depth;
        x = x - scale;
        y = y - scale;
        z = z - scale;
        maxX = maxX + scale;
        maxY = maxY + scale;
        maxZ = maxZ + scale;

        if (mirror)
        {
            float mirroredMaxX = maxX;
            maxX = x;
            x = mirroredMaxX;
        }

        PositionTextureVertex minXMinYMinZ = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
        PositionTextureVertex maxXMinYMinZ = new PositionTextureVertex(maxX, y, z, 0.0F, 8.0F);
        PositionTextureVertex maxXMaxYMinZ = new PositionTextureVertex(maxX, maxY, z, 8.0F, 8.0F);
        PositionTextureVertex minXMaxYMinZ = new PositionTextureVertex(x, maxY, z, 8.0F, 0.0F);
        PositionTextureVertex minXMinYMaxZ = new PositionTextureVertex(x, y, maxZ, 0.0F, 0.0F);
        PositionTextureVertex maxXMinYMaxZ = new PositionTextureVertex(maxX, y, maxZ, 0.0F, 8.0F);
        PositionTextureVertex maxXMaxYMaxZ = new PositionTextureVertex(maxX, maxY, maxZ, 8.0F, 8.0F);
        PositionTextureVertex minXMaxYMaxZ = new PositionTextureVertex(x, maxY, maxZ, 8.0F, 0.0F);
        this.vertexPositions[0] = minXMinYMinZ;
        this.vertexPositions[1] = maxXMinYMinZ;
        this.vertexPositions[2] = maxXMaxYMinZ;
        this.vertexPositions[3] = minXMaxYMinZ;
        this.vertexPositions[4] = minXMinYMaxZ;
        this.vertexPositions[5] = maxXMinYMaxZ;
        this.vertexPositions[6] = maxXMaxYMaxZ;
        this.vertexPositions[7] = minXMaxYMaxZ;
        this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] {maxXMinYMaxZ, maxXMinYMinZ, maxXMaxYMinZ, maxXMaxYMaxZ}, textureX + depth + width, textureY + depth, textureX + depth + width + depth, textureY + depth + height, renderer.textureWidth, renderer.textureHeight);
        this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] {minXMinYMinZ, minXMinYMaxZ, minXMaxYMaxZ, minXMaxYMinZ}, textureX, textureY + depth, textureX + depth, textureY + depth + height, renderer.textureWidth, renderer.textureHeight);
        this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] {maxXMinYMaxZ, minXMinYMaxZ, minXMinYMinZ, maxXMinYMinZ}, textureX + depth, textureY, textureX + depth + width, textureY + depth, renderer.textureWidth, renderer.textureHeight);
        this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] {maxXMaxYMinZ, minXMaxYMinZ, minXMaxYMaxZ, maxXMaxYMaxZ}, textureX + depth + width, textureY + depth, textureX + depth + width + width, textureY, renderer.textureWidth, renderer.textureHeight);
        this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] {maxXMinYMinZ, minXMinYMinZ, minXMaxYMinZ, maxXMaxYMinZ}, textureX + depth, textureY + depth, textureX + depth + width, textureY + depth + height, renderer.textureWidth, renderer.textureHeight);
        this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] {minXMinYMaxZ, maxXMinYMaxZ, maxXMaxYMaxZ, minXMaxYMaxZ}, textureX + depth + width + depth, textureY + depth, textureX + depth + width + depth + width, textureY + depth + height, renderer.textureWidth, renderer.textureHeight);

        if (mirror)
        {
            for (int quadIndex = 0; quadIndex < this.quadList.length; ++quadIndex)
            {
                this.quadList[quadIndex].flipFace();
            }
        }
    }

    public void render(WorldRenderer renderer, float scale)
    {
        for (int quadIndex = 0; quadIndex < this.quadList.length; ++quadIndex)
        {
            TexturedQuad texturedQuad = this.quadList[quadIndex];

            if (texturedQuad != null)
            {
                texturedQuad.draw(renderer, scale);
            }
        }
    }

    public ModelBox setBoxName(String name)
    {
        this.boxName = name;
        return this;
    }
}
