package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiStreamIndicator
{
    private static final ResourceLocation locationStreamIndicator = new ResourceLocation("textures/gui/stream_indicator.png");
    private final Minecraft mc;
    private float streamAlpha = 1.0F;
    private int streamAlphaDelta = 1;

    public GuiStreamIndicator(Minecraft mcIn)
    {
        this.mc = mcIn;
    }

    public void render(int screenX, int screenY)
    {
        if (this.mc.getTwitchStream().isBroadcasting())
        {
            GlStateManager.enableBlend();
            int i = this.mc.getTwitchStream().getViewerCount();

            if (i > 0)
            {
                String s = "" + i;
                int j = this.mc.fontRendererObj.getStringWidth(s);
                int k = 20;
                int l = screenX - j - 1;
                int intValue = screenY + 20 - 1;
                int secondIntValue = screenY + 20 + this.mc.fontRendererObj.FONT_HEIGHT - 1;
                GlStateManager.disableTexture2D();
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                GlStateManager.color(0.0F, 0.0F, 0.0F, (0.65F + 0.35000002F * this.streamAlpha) / 2.0F);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION);
                worldrenderer.pos((double)l, (double)secondIntValue, 0.0D).endVertex();
                worldrenderer.pos((double)screenX, (double)secondIntValue, 0.0D).endVertex();
                worldrenderer.pos((double)screenX, (double)intValue, 0.0D).endVertex();
                worldrenderer.pos((double)l, (double)intValue, 0.0D).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
                this.mc.fontRendererObj.drawString(s, screenX - j, screenY + 20, 16777215);
            }

            this.render(screenX, screenY, this.getBroadcastIconTextureY(), 0);
            this.render(screenX, screenY, this.getMicrophoneIconTextureY(), 17);
        }
    }

    private void render(int screenX, int screenY, int textureY, int xOffset)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.65F + 0.35000002F * this.streamAlpha);
        this.mc.getTextureManager().bindTexture(locationStreamIndicator);
        float f = 150.0F;
        float floatValue = 0.0F;
        float secondFloatValue = (float)textureY * 0.015625F;
        float thirdFloatValue = 1.0F;
        float fourthFloatValue = (float)(textureY + 16) * 0.015625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)(screenX - 16 - xOffset), (double)(screenY + 16), (double)f).tex((double)floatValue, (double)fourthFloatValue).endVertex();
        worldrenderer.pos((double)(screenX - xOffset), (double)(screenY + 16), (double)f).tex((double)thirdFloatValue, (double)fourthFloatValue).endVertex();
        worldrenderer.pos((double)(screenX - xOffset), (double)(screenY + 0), (double)f).tex((double)thirdFloatValue, (double)secondFloatValue).endVertex();
        worldrenderer.pos((double)(screenX - 16 - xOffset), (double)(screenY + 0), (double)f).tex((double)floatValue, (double)secondFloatValue).endVertex();
        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int getBroadcastIconTextureY()
    {
        return this.mc.getTwitchStream().isPaused() ? 16 : 0;
    }

    private int getMicrophoneIconTextureY()
    {
        return this.mc.getTwitchStream().isMicrophoneMuted() ? 48 : 32;
    }

    public void updateStreamAlpha()
    {
        if (this.mc.getTwitchStream().isBroadcasting())
        {
            this.streamAlpha += 0.025F * (float)this.streamAlphaDelta;

            if (this.streamAlpha < 0.0F)
            {
                this.streamAlphaDelta *= -1;
                this.streamAlpha = 0.0F;
            }
            else if (this.streamAlpha > 1.0F)
            {
                this.streamAlphaDelta *= -1;
                this.streamAlpha = 1.0F;
            }
        }
        else
        {
            this.streamAlpha = 1.0F;
            this.streamAlphaDelta = 1;
        }
    }
}
