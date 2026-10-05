package net.minecraft.realms;

public final class RealmsARBMultitexture
{
    private RealmsARBMultitexture() {}

    public static void glActiveTextureARB(int texture)
    {
        RealmsGL13.glActiveTexture(texture);
    }
}
