package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

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
        float ambientLight = 0.4F;
        float diffuseLight = 0.6F;
        float specularLight = 0.0F;
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, (FloatBuffer)setColorBuffer(LIGHT0_POS.xCoord, LIGHT0_POS.yCoord, LIGHT0_POS.zCoord, 0.0D));
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, (FloatBuffer)setColorBuffer(diffuseLight, diffuseLight, diffuseLight, 1.0F));
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, (FloatBuffer)setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, (FloatBuffer)setColorBuffer(specularLight, specularLight, specularLight, 1.0F));
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, (FloatBuffer)setColorBuffer(LIGHT1_POS.xCoord, LIGHT1_POS.yCoord, LIGHT1_POS.zCoord, 0.0D));
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)setColorBuffer(diffuseLight, diffuseLight, diffuseLight, 1.0F));
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_SPECULAR, (FloatBuffer)setColorBuffer(specularLight, specularLight, specularLight, 1.0F));
        GlStateManager.shadeModel(7424);
        GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, (FloatBuffer)setColorBuffer(ambientLight, ambientLight, ambientLight, 1.0F));
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
