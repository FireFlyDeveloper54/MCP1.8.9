package net.minecraft.realms;

public final class RealmsGLContext
{
    private RealmsGLContext() {}

    public static RealmsCapabilities getCapabilities()
    {
        return new RealmsCapabilities();
    }
}
