package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiWinGame extends GuiScreen
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("textures/misc/vignette.png");
    private int time;
    private List<String> lines;
    private int totalScrollLength;
    private float scrollSpeed = 0.5F;

    public void updateScreen()
    {
        MusicTicker musicTicker = this.mc.getMusicTicker();
        SoundHandler soundHandler = this.mc.getSoundHandler();

        if (this.time == 0)
        {
            musicTicker.stopCurrentMusic();
            musicTicker.playMusic(MusicTicker.MusicType.CREDITS);
            soundHandler.resumeSounds();
        }

        soundHandler.update();
        ++this.time;
        float f = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;

        if ((float)this.time > f)
        {
            this.sendRespawnPacket();
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            this.sendRespawnPacket();
        }
    }

    private void sendRespawnPacket()
    {
        this.mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
        this.mc.displayGuiScreen((GuiScreen)null);
    }

    public boolean doesGuiPauseGame()
    {
        return true;
    }

    public void initGui()
    {
        if (this.lines == null)
        {
            this.lines = Lists.<String>newArrayList();

            try
            {
                String s = "";
                String text = "" + EnumChatFormatting.WHITE + EnumChatFormatting.OBFUSCATED + EnumChatFormatting.GREEN + EnumChatFormatting.AQUA;
                int i = 274;
                InputStream inputStream = this.mc.getResourceManager().getResource(new ResourceLocation("texts/end.txt")).getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
                Random random = new Random(8124371L);

                while ((s = bufferedReader.readLine()) != null)
                {
                    String text2;
                    String text3;

                    for (s = s.replace("PLAYERNAME", this.mc.getSession().getUsername()); s.contains(text); s = text2 + EnumChatFormatting.WHITE + EnumChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, random.nextInt(4) + 3) + text3)
                    {
                        int j = s.indexOf(text);
                        text2 = s.substring(0, j);
                        text3 = s.substring(j + text.length());
                    }

                    this.lines.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(s, i));
                    this.lines.add("");
                }

                inputStream.close();

                for (int k = 0; k < 8; ++k)
                {
                    this.lines.add("");
                }

                inputStream = this.mc.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));

                while ((s = bufferedReader.readLine()) != null)
                {
                    s = s.replace("PLAYERNAME", this.mc.getSession().getUsername());
                    s = s.replace("\t", "    ");
                    this.lines.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(s, i));
                    this.lines.add("");
                }

                inputStream.close();
                this.totalScrollLength = this.lines.size() * 12;
            }
            catch (Exception exception)
            {
                logger.error((String)"Couldn\'t load credits", (Throwable)exception);
            }
        }
    }

    private void drawBackground(float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        int i = this.width;
        float f = 0.0F - ((float)this.time + partialTicks) * 0.5F * this.scrollSpeed;
        float floatValue = (float)this.height - ((float)this.time + partialTicks) * 0.5F * this.scrollSpeed;
        float thirdFloatValue = 0.015625F;
        float fourthFloatValue = ((float)this.time + partialTicks - 0.0F) * 0.02F;
        float fifthFloatValue = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
        float sixthFloatValue = (fifthFloatValue - 20.0F - ((float)this.time + partialTicks)) * 0.005F;

        if (sixthFloatValue < fourthFloatValue)
        {
            fourthFloatValue = sixthFloatValue;
        }

        if (fourthFloatValue > 1.0F)
        {
            fourthFloatValue = 1.0F;
        }

        fourthFloatValue = fourthFloatValue * fourthFloatValue;
        fourthFloatValue = fourthFloatValue * 96.0F / 255.0F;
        worldrenderer.pos(0.0D, (double)this.height, (double)this.zLevel).tex(0.0D, (double)(f * thirdFloatValue)).color(fourthFloatValue, fourthFloatValue, fourthFloatValue, 1.0F).endVertex();
        worldrenderer.pos((double)i, (double)this.height, (double)this.zLevel).tex((double)((float)i * thirdFloatValue), (double)(f * thirdFloatValue)).color(fourthFloatValue, fourthFloatValue, fourthFloatValue, 1.0F).endVertex();
        worldrenderer.pos((double)i, 0.0D, (double)this.zLevel).tex((double)((float)i * thirdFloatValue), (double)(floatValue * thirdFloatValue)).color(fourthFloatValue, fourthFloatValue, fourthFloatValue, 1.0F).endVertex();
        worldrenderer.pos(0.0D, 0.0D, (double)this.zLevel).tex(0.0D, (double)(floatValue * thirdFloatValue)).color(fourthFloatValue, fourthFloatValue, fourthFloatValue, 1.0F).endVertex();
        tessellator.draw();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawBackground(partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 274;
        int j = this.width / 2 - i / 2;
        int k = this.height + 50;
        float f = -((float)this.time + partialTicks) * this.scrollSpeed;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, f, 0.0F);
        this.mc.getTextureManager().bindTexture(MINECRAFT_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(j, k, 0, 0, 155, 44);
        this.drawTexturedModalRect(j + 155, k, 0, 45, 155, 44);
        int l = k + 200;

        for (int intValue = 0; intValue < this.lines.size(); ++intValue)
        {
            if (intValue == this.lines.size() - 1)
            {
                float secondFloatValue = (float)l + f - (float)(this.height / 2 - 6);

                if (secondFloatValue < 0.0F)
                {
                    GlStateManager.translate(0.0F, -secondFloatValue, 0.0F);
                }
            }

            if ((float)l + f + 12.0F + 8.0F > 0.0F && (float)l + f < (float)this.height)
            {
                String s = this.lines.get(intValue);

                if (s.startsWith("[C]"))
                {
                    this.fontRendererObj.drawStringWithShadow(s.substring(3), (float)(j + (i - this.fontRendererObj.getStringWidth(s.substring(3))) / 2), (float)l, 16777215);
                }
                else
                {
                    this.fontRendererObj.fontRandom.setSeed((long)intValue * 4238972211L + (long)(this.time / 4));
                    this.fontRendererObj.drawStringWithShadow(s, (float)j, (float)l, 16777215);
                }
            }

            l += 12;
        }

        GlStateManager.popMatrix();
        this.mc.getTextureManager().bindTexture(VIGNETTE_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(0, 769);
        int secondIntValue = this.width;
        int thirdIntValue = this.height;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, (double)thirdIntValue, (double)this.zLevel).tex(0.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos((double)secondIntValue, (double)thirdIntValue, (double)this.zLevel).tex(1.0D, 1.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos((double)secondIntValue, 0.0D, (double)this.zLevel).tex(1.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0D, 0.0D, (double)this.zLevel).tex(0.0D, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
