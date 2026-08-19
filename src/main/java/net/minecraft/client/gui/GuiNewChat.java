package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiNewChat extends Gui
{
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.<String>newArrayList();
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public GuiNewChat(Minecraft mcIn)
    {
        this.mc = mcIn;
    }

    public void drawChat(int updateCounter)
    {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0)
            {
                if (this.getChatOpen())
                {
                    flag = true;
                }

                float floatValue = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / floatValue);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(floatValue, floatValue, 1.0F);

                for (int intValue = 0; intValue + this.scrollPos < this.drawnChatLines.size() && intValue < i; ++intValue)
                {
                    ChatLine chatline = this.drawnChatLines.get(intValue + this.scrollPos);

                    if (chatline != null)
                    {
                        int secondIntValue = updateCounter - chatline.getUpdatedCounter();

                        if (secondIntValue < 200 || flag)
                        {
                            double doubleValue = (double)secondIntValue / 200.0D;
                            doubleValue = 1.0D - doubleValue;
                            doubleValue = doubleValue * 10.0D;
                            doubleValue = MathHelper.clamp_double(doubleValue, 0.0D, 1.0D);
                            doubleValue = doubleValue * doubleValue;
                            int thirdIntValue = (int)(255.0D * doubleValue);

                            if (flag)
                            {
                                thirdIntValue = 255;
                            }

                            thirdIntValue = (int)((float)thirdIntValue * f);
                            ++j;

                            if (thirdIntValue > 3)
                            {
                                int fourthIntValue = 0;
                                int fifthIntValue = -intValue * 9;
                                drawRect(fourthIntValue, fifthIntValue - 9, fourthIntValue + l + 4, fifthIntValue, thirdIntValue / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                this.mc.fontRendererObj.drawStringWithShadow(s, (float)fourthIntValue, (float)(fifthIntValue - 8), 16777215 + (thirdIntValue << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag)
                {
                    int sixthIntValue = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int seventhIntValue = k * sixthIntValue + k;
                    int eighthIntValue = j * sixthIntValue + j;
                    int ninthIntValue = this.scrollPos * eighthIntValue / k;
                    int tenthIntValue = eighthIntValue * eighthIntValue / seventhIntValue;

                    if (seventhIntValue != eighthIntValue)
                    {
                        int eleventhIntValue = ninthIntValue > 0 ? 170 : 96;
                        int twelfthIntValue = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -ninthIntValue, 2, -ninthIntValue - tenthIntValue, twelfthIntValue + (eleventhIntValue << 24));
                        drawRect(2, -ninthIntValue, 1, -ninthIntValue - tenthIntValue, 13421772 + (eleventhIntValue << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    public void clearChatMessages()
    {
        this.drawnChatLines.clear();
        this.chatLines.clear();
        this.sentMessages.clear();
    }

    public void printChatMessage(IChatComponent chatComponent)
    {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId)
    {
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
        logger.info("[CHAT] " + chatComponent.getUnformattedText());
    }

    private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly)
    {
        if (chatLineId != 0)
        {
            this.deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();

        for (IChatComponent ichatcomponent : list)
        {
            if (flag && this.scrollPos > 0)
            {
                this.isScrolled = true;
                this.scroll(1);
            }

            this.drawnChatLines.add(0, new ChatLine(updateCounter, ichatcomponent, chatLineId));
        }

        while (this.drawnChatLines.size() > 100)
        {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }

        if (!displayOnly)
        {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

            while (this.chatLines.size() > 100)
            {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    public void refreshChat()
    {
        this.drawnChatLines.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i)
        {
            ChatLine chatLine = this.chatLines.get(i);
            this.setChatLine(chatLine.getChatComponent(), chatLine.getChatLineID(), chatLine.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages()
    {
        return this.sentMessages;
    }

    public void addToSentMessages(String message)
    {
        if (this.sentMessages.isEmpty() || !this.sentMessages.get(this.sentMessages.size() - 1).equals(message))
        {
            this.sentMessages.add(message);
        }
    }

    public void resetScroll()
    {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    public void scroll(int amount)
    {
        this.scrollPos += amount;
        int i = this.drawnChatLines.size();

        if (this.scrollPos > i - this.getLineCount())
        {
            this.scrollPos = i - this.getLineCount();
        }

        if (this.scrollPos <= 0)
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    public IChatComponent getChatComponent(int mouseX, int mouseY)
    {
        if (!this.getChatOpen())
        {
            return null;
        }
        else
        {
            ScaledResolution scaledResolution = new ScaledResolution(this.mc);
            int i = scaledResolution.getScaleFactor();
            float f = this.getChatScale();
            int j = mouseX / i - 3;
            int k = mouseY / i - 27;
            j = MathHelper.floor_float((float)j / f);
            k = MathHelper.floor_float((float)k / f);

            if (j >= 0 && k >= 0)
            {
                int l = Math.min(this.getLineCount(), this.drawnChatLines.size());

                if (j <= MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l)
                {
                    int intValue2 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

                    if (intValue2 >= 0 && intValue2 < this.drawnChatLines.size())
                    {
                        ChatLine chatLine = this.drawnChatLines.get(intValue2);
                        int secondIntValue2 = 0;

                        for (IChatComponent ichatcomponent : chatLine.getChatComponent())
                        {
                            if (ichatcomponent instanceof ChatComponentText)
                            {
                                secondIntValue2 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));

                                if (secondIntValue2 > j)
                                {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    public boolean getChatOpen()
    {
        return this.mc.currentScreen instanceof GuiChat;
    }

    public void deleteChatLine(int id)
    {
        Iterator<ChatLine> iterator = this.drawnChatLines.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatLine = (ChatLine)iterator.next();

            if (chatLine.getChatLineID() == id)
            {
                iterator.remove();
            }
        }

        iterator = this.chatLines.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline1 = (ChatLine)iterator.next();

            if (chatline1.getChatLineID() == id)
            {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth()
    {
        return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight()
    {
        return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    public float getChatScale()
    {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float scale)
    {
        int i = 320;
        int j = 40;
        return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
    }

    public static int calculateChatboxHeight(float scale)
    {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
    }

    public int getLineCount()
    {
        return this.getChatHeight() / 9;
    }
}
