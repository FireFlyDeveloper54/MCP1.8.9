package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import net.minecraft.util.Vec3;

public class RenderHelper
{
    private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
    private static final Vec3 LIGHT0_POS = (new Vec3(0.20000000298023224D, 1.0D, -0.699999988079071D)).normalize();
    private static final Vec3 LIGHT1_POS = (new Vec3(-0.20000000298023224D, 1.0D, 0.699999988079071D)).normalize();

    public static void disableStandardItemLighting()
    {
        GlStateManager.disableLighting();
        GlStateManager.disableLight(0);
        GlStateManager.disableLight(1);
        GlStateManager.disableColorMaterial();
    }

    public static void enableStandardItemLighting()
    {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        GlStateManager.shadeModel(7424);
    }

    private static FloatBuffer setColorBuffer(double red, double green, double blue, double alpha)
    {
        return setColorBuffer((float)red, (float)green, (float)blue, (float)alpha);
    }

    private static FloatBuffer setColorBuffer(float red, float green, float blue, float alpha)
    {
        colorBuffer.clear();
        colorBuffer.put(red).put(green).put(blue).put(alpha);
        colorBuffer.flip();
        return colorBuffer;
    }

    public static void enableGUIStandardItemLighting()
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
        enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
