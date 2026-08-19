package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class GuiUtilRenderComponents
{
    public static String removeTextColorsIfConfigured(String text, boolean forceColors)
    {
        return !forceColors && !Minecraft.getMinecraft().gameSettings.chatColours ? EnumChatFormatting.getTextWithoutFormattingCodes(text) : text;
    }

    public static List<IChatComponent> splitText(IChatComponent textComponent, int maxWidth, FontRenderer fontRenderer, boolean breakAtSpaces, boolean forceColors)
    {
        int i = 0;
        IChatComponent ichatcomponent = new ChatComponentText("");
        List<IChatComponent> list = Lists.<IChatComponent>newArrayList();
        List<IChatComponent> list1 = Lists.<IChatComponent>newArrayList(textComponent);

        for (int j = 0; j < list1.size(); ++j)
        {
            IChatComponent ichatcomponent1 = list1.get(j);
            String s = ichatcomponent1.getUnformattedTextForChat();
            boolean flag = false;

            if (s.contains("\n"))
            {
                int k = s.indexOf(10);
                String stringValue = s.substring(k + 1);
                s = s.substring(0, k + 1);
                ChatComponentText chatcomponenttext = new ChatComponentText(stringValue);
                chatcomponenttext.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());
                list1.add(j + 1, chatcomponenttext);
                flag = true;
            }

            String fourthStringValue = removeTextColorsIfConfigured(ichatcomponent1.getChatStyle().getFormattingCode() + s, forceColors);
            String fifthStringValue = fourthStringValue.endsWith("\n") ? fourthStringValue.substring(0, fourthStringValue.length() - 1) : fourthStringValue;
            int intValue = fontRenderer.getStringWidth(fifthStringValue);
            ChatComponentText chatcomponenttext1 = new ChatComponentText(fifthStringValue);
            chatcomponenttext1.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());

            if (i + intValue > maxWidth)
            {
                String secondStringValue = fontRenderer.trimStringToWidth(fourthStringValue, maxWidth - i, false);
                String thirdStringValue = secondStringValue.length() < fourthStringValue.length() ? fourthStringValue.substring(secondStringValue.length()) : null;

                if (thirdStringValue != null && thirdStringValue.length() > 0)
                {
                    int l = secondStringValue.lastIndexOf(" ");

                    if (l >= 0 && fontRenderer.getStringWidth(fourthStringValue.substring(0, l)) > 0)
                    {
                        secondStringValue = fourthStringValue.substring(0, l);

                        if (breakAtSpaces)
                        {
                            ++l;
                        }

                        thirdStringValue = fourthStringValue.substring(l);
                    }
                    else if (i > 0 && !fourthStringValue.contains(" "))
                    {
                        secondStringValue = "";
                        thirdStringValue = fourthStringValue;
                    }

                    ChatComponentText chatcomponenttext2 = new ChatComponentText(thirdStringValue);
                    chatcomponenttext2.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());
                    list1.add(j + 1, chatcomponenttext2);
                }

                intValue = fontRenderer.getStringWidth(secondStringValue);
                chatcomponenttext1 = new ChatComponentText(secondStringValue);
                chatcomponenttext1.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());
                flag = true;
            }

            if (i + intValue <= maxWidth)
            {
                i += intValue;
                ichatcomponent.appendSibling(chatcomponenttext1);
            }
            else
            {
                flag = true;
            }

            if (flag)
            {
                list.add(ichatcomponent);
                i = 0;
                ichatcomponent = new ChatComponentText("");
            }
        }

        list.add(ichatcomponent);
        return list;
    }
}
