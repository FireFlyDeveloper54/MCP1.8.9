package net.optifine;

import java.util.Properties;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

public class CustomLoadingScreen
{
    private ResourceLocation locationTexture;
    private int scaleMode = 0;
    private int scale = 2;
    private boolean center;
    private static final int SCALE_DEFAULT = 2;
    private static final int SCALE_MODE_FIXED = 0;
    private static final int SCALE_MODE_FULL = 1;
    private static final int SCALE_MODE_STRETCH = 2;

    public CustomLoadingScreen(ResourceLocation locationTexture, int scaleMode, int scale, boolean center)
    {
        this.locationTexture = locationTexture;
        this.scaleMode = scaleMode;
        this.scale = scale;
        this.center = center;
    }

    public static CustomLoadingScreen parseScreen(String path, int dimId, Properties props)
    {
        ResourceLocation textureLocation = new ResourceLocation(path);
        int scaleMode = parseScaleMode(getProperty("scaleMode", dimId, props));
        int defaultScale = scaleMode == 0 ? 2 : 1;
        int scale = parseScale(getProperty("scale", dimId, props), defaultScale);
        boolean center = Config.parseBoolean(getProperty("center", dimId, props), false);
        CustomLoadingScreen loadingScreen = new CustomLoadingScreen(textureLocation, scaleMode, scale, center);
        return loadingScreen;
    }

    private static String getProperty(String key, int dim, Properties props)
    {
        if (props == null)
        {
            return null;
        }
        else
        {
            String value = props.getProperty("dim" + dim + "." + key);

            if (value != null)
            {
                return value;
            }
            else
            {
                value = props.getProperty(key);
                return value;
            }
        }
    }

    private static int parseScaleMode(String str)
    {
        if (str == null)
        {
            return 0;
        }
        else
        {
            str = str.toLowerCase().trim();

            if (str.equals("fixed"))
            {
                return 0;
            }
            else if (str.equals("full"))
            {
                return 1;
            }
            else if (str.equals("stretch"))
            {
                return 2;
            }
            else
            {
                CustomLoadingScreens.warn("Invalid scale mode: " + str);
                return 0;
            }
        }
    }

    private static int parseScale(String str, int def)
    {
        if (str == null)
        {
            return def;
        }
        else
        {
            str = str.trim();
            int scale = Config.parseInt(str, -1);

            if (scale < 1)
            {
                CustomLoadingScreens.warn("Invalid scale: " + str);
                return def;
            }
            else
            {
                return scale;
            }
        }
    }

    public void drawBackground(int width, int height)
    {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        Config.getTextureManager().bindTexture(this.locationTexture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        double textureScale = (double)(16 * this.scale);
        double uScale = (double)width / textureScale;
        double vScale = (double)height / textureScale;
        double uOffset = 0.0D;
        double vOffset = 0.0D;

        if (this.center)
        {
            uOffset = (textureScale - (double)width) / (textureScale * 2.0D);
            vOffset = (textureScale - (double)height) / (textureScale * 2.0D);
        }

        switch (this.scaleMode)
        {
            case 1:
                textureScale = (double)Math.max(width, height);
                uScale = (double)(this.scale * width) / textureScale;
                vScale = (double)(this.scale * height) / textureScale;

                if (this.center)
                {
                    uOffset = (double)this.scale * (textureScale - (double)width) / (textureScale * 2.0D);
                    vOffset = (double)this.scale * (textureScale - (double)height) / (textureScale * 2.0D);
                }

                break;

            case 2:
                uScale = (double)this.scale;
                vScale = (double)this.scale;
                uOffset = 0.0D;
                vOffset = 0.0D;
        }

        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRenderer.pos(0.0D, (double)height, 0.0D).tex(uOffset, vOffset + vScale).color(255, 255, 255, 255).endVertex();
        worldRenderer.pos((double)width, (double)height, 0.0D).tex(uOffset + uScale, vOffset + vScale).color(255, 255, 255, 255).endVertex();
        worldRenderer.pos((double)width, 0.0D, 0.0D).tex(uOffset + uScale, vOffset).color(255, 255, 255, 255).endVertex();
        worldRenderer.pos(0.0D, 0.0D, 0.0D).tex(uOffset, vOffset).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
    }
}
