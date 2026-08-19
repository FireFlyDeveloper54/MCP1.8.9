package net.minecraft.client.model;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.util.Vec3;
import net.optifine.shaders.SVertexFormat;

public class TexturedQuad
{
    public PositionTextureVertex[] vertexPositions;
    public int nVertices;
    private boolean invertNormal;

    public TexturedQuad(PositionTextureVertex[] vertices)
    {
        this.vertexPositions = vertices;
        this.nVertices = vertices.length;
    }

    public TexturedQuad(PositionTextureVertex[] vertices, int texcoordU1, int texcoordV1, int texcoordU2, int texcoordV2, float textureWidth, float textureHeight)
    {
        this(vertices);
        float textureUOffset = 0.0F / textureWidth;
        float textureVOffset = 0.0F / textureHeight;
        vertices[0] = vertices[0].setTexturePosition((float)texcoordU2 / textureWidth - textureUOffset, (float)texcoordV1 / textureHeight + textureVOffset);
        vertices[1] = vertices[1].setTexturePosition((float)texcoordU1 / textureWidth + textureUOffset, (float)texcoordV1 / textureHeight + textureVOffset);
        vertices[2] = vertices[2].setTexturePosition((float)texcoordU1 / textureWidth + textureUOffset, (float)texcoordV2 / textureHeight - textureVOffset);
        vertices[3] = vertices[3].setTexturePosition((float)texcoordU2 / textureWidth - textureUOffset, (float)texcoordV2 / textureHeight - textureVOffset);
    }

    public void flipFace()
    {
        PositionTextureVertex[] reversedVertices = new PositionTextureVertex[this.vertexPositions.length];

        for (int vertexIndex = 0; vertexIndex < this.vertexPositions.length; ++vertexIndex)
        {
            reversedVertices[vertexIndex] = this.vertexPositions[this.vertexPositions.length - vertexIndex - 1];
        }

        this.vertexPositions = reversedVertices;
    }

    public void draw(WorldRenderer renderer, float scale)
    {
        Vec3 firstEdge = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[0].vector3D);
        Vec3 secondEdge = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[2].vector3D);
        Vec3 normal = secondEdge.crossProduct(firstEdge).normalize();
        float normalX = (float)normal.xCoord;
        float normalY = (float)normal.yCoord;
        float normalZ = (float)normal.zCoord;

        if (this.invertNormal)
        {
            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }

        boolean drawImmediately = !renderer.isDrawing();

        if (drawImmediately && Config.isShaders())
        {
            renderer.begin(7, SVertexFormat.defVertexFormatTextured);
        }
        else if (drawImmediately)
        {
            renderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
        }

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            PositionTextureVertex vertex = this.vertexPositions[vertexIndex];
            renderer.pos(vertex.vector3D.xCoord * (double)scale, vertex.vector3D.yCoord * (double)scale, vertex.vector3D.zCoord * (double)scale).tex((double)vertex.texturePositionX, (double)vertex.texturePositionY).normal(normalX, normalY, normalZ).endVertex();
        }

        if (drawImmediately)
        {
            Tessellator.getInstance().draw();
        }
    }
}
