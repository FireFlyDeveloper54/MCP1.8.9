package net.minecraft.realms;

import java.nio.IntBuffer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public final class RealmsGL11
{
    private RealmsGL11() {}

    public static void glColor4f(float red, float green, float blue, float alpha)
    {
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glPushMatrix() { GlStateManager.pushMatrix(); }
    public static void glPopMatrix() { GlStateManager.popMatrix(); }
    public static void glRotatef(float angle, float x, float y, float z) { GlStateManager.rotate(angle, x, y, z); }
    public static void glScalef(float x, float y, float z) { GlStateManager.scale(x, y, z); }
    public static void glTranslatef(float x, float y, float z) { GlStateManager.translate(x, y, z); }
    public static void glBlendFunc(int source, int destination) { GlStateManager.blendFunc(source, destination); }

    public static void glEnable(int capability) { setEnabled(capability, true); }
    public static void glDisable(int capability) { setEnabled(capability, false); }

    private static void setEnabled(int capability, boolean enabled)
    {
        switch (capability)
        {
            case GL11.GL_TEXTURE_2D:
                if (enabled) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
                return;
            case GL11.GL_BLEND:
                if (enabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
                return;
            case 3008:
                if (enabled) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha();
                return;
            case GL11.GL_DEPTH_TEST:
                if (enabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
                return;
            case GL11.GL_CULL_FACE:
                if (enabled) GlStateManager.enableCull(); else GlStateManager.disableCull();
                return;
            case 2896:
                if (enabled) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
                return;
            case 2912:
                if (enabled) GlStateManager.enableFog(); else GlStateManager.disableFog();
                return;
            default:
                if (GlStateManager.isFixedFunctionCap(capability))
                {
                    throw new IllegalArgumentException("Unsupported Realms capability: " + capability);
                }
                if (enabled) GL11.glEnable(capability); else GL11.glDisable(capability);
        }
    }

    public static void glBindTexture(int target, int texture)
    {
        if (target == GL11.GL_TEXTURE_2D) GlStateManager.bindTexture(texture); else GL11.glBindTexture(target, texture);
    }

    public static int glGenTextures() { return GlStateManager.generateTexture(); }
    public static void glDeleteTextures(int texture) { GlStateManager.deleteTexture(texture); }
    public static void glTexParameteri(int target, int parameter, int value) { GL11.glTexParameteri(target, parameter, value); }

    public static void glTexImage2D(int target, int level, int internalFormat, int width, int height,
                                  int border, int format, int type, IntBuffer pixels)
    {
        GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }
}
