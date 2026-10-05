package net.minecraft.realms;

import org.lwjgl.opengl.GL;

public final class RealmsCapabilities
{
    public final boolean OpenGL13;
    public final boolean GL_ARB_multitexture;

    public RealmsCapabilities()
    {
        this.OpenGL13 = GL.getCapabilities().OpenGL13;
        this.GL_ARB_multitexture = this.OpenGL13;
    }
}
