package net.optifine.texture;

public enum TextureType
{
    TEXTURE_1D(3552),
    TEXTURE_2D(3553),
    TEXTURE_3D(32879),
    TEXTURE_RECTANGLE(34037);

    public static final TextureType[] VALUES = values();
    private int glTextureId;

    private TextureType(int glTextureId)
    {
        this.glTextureId = glTextureId;
    }

    public int getId()
    {
        return this.glTextureId;
    }
}
