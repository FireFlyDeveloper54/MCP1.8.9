package net.minecraft.client.model;

import net.minecraft.util.Vec3;

public class PositionTextureVertex
{
    public Vec3 vector3D;
    public float texturePositionX;
    public float texturePositionY;

    public PositionTextureVertex(float x, float y, float z, float textureU, float textureV)
    {
        this(new Vec3((double)x, (double)y, (double)z), textureU, textureV);
    }

    public PositionTextureVertex setTexturePosition(float textureU, float textureV)
    {
        return new PositionTextureVertex(this, textureU, textureV);
    }

    public PositionTextureVertex(PositionTextureVertex textureVertex, float texturePositionXIn, float texturePositionYIn)
    {
        this.vector3D = textureVertex.vector3D;
        this.texturePositionX = texturePositionXIn;
        this.texturePositionY = texturePositionYIn;
    }

    public PositionTextureVertex(Vec3 vector3DIn, float texturePositionXIn, float texturePositionYIn)
    {
        this.vector3D = vector3DIn;
        this.texturePositionX = texturePositionXIn;
        this.texturePositionY = texturePositionYIn;
    }
}
