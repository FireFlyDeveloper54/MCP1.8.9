package net.minecraft.client.gui.stream;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.IStream;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import tv.twitch.chat.ChatUserInfo;
import tv.twitch.chat.ChatUserMode;
import tv.twitch.chat.ChatUserSubscription;

public class GuiTwitchUserMode extends GuiScreen
{
    private static final EnumChatFormatting SUBSCRIBER_COLOR = EnumChatFormatting.DARK_GREEN;
    private static final EnumChatFormatting BANNED_COLOR = EnumChatFormatting.RED;
    private static final EnumChatFormatting SPECIAL_USER_COLOR = EnumChatFormatting.DARK_PURPLE;
    private final ChatUserInfo userInfo;
    private final IChatComponent displayNameComponent;
    private final List<IChatComponent> userModeLines = Lists.<IChatComponent>newArrayList();
    private final IStream stream;
    private int modeLineX;

    public GuiTwitchUserMode(IStream streamIn, ChatUserInfo userInfoIn)
    {
        this.stream = streamIn;
        this.userInfo = userInfoIn;
        this.displayNameComponent = new ChatComponentText(userInfoIn.displayName);
        this.userModeLines.addAll(createUserModeLines(userInfoIn.modes, userInfoIn.subscriptions, streamIn));
    }

    public static List<IChatComponent> createUserModeLines(Set<ChatUserMode> modes, Set<ChatUserSubscription> subscriptions, IStream streamIn)
    {
        String s = streamIn == null ? null : streamIn.getChannelName();
        boolean flag = streamIn != null && streamIn.isChannelOwner();
        List<IChatComponent> list = Lists.<IChatComponent>newArrayList();

        for (ChatUserMode chatusermode : modes)
        {
            IChatComponent ichatcomponent = createModeComponent(chatusermode, s, flag);

            if (ichatcomponent != null)
            {
                IChatComponent ichatcomponent1 = new ChatComponentText("- ");
                ichatcomponent1.appendSibling(ichatcomponent);
                list.add(ichatcomponent1);
            }
        }

        for (ChatUserSubscription chatusersubscription : subscriptions)
        {
            IChatComponent ichatcomponent2 = createSubscriptionComponent(chatusersubscription, s, flag);

            if (ichatcomponent2 != null)
            {
                IChatComponent ichatcomponent3 = new ChatComponentText("- ");
                ichatcomponent3.appendSibling(ichatcomponent2);
                list.add(ichatcomponent3);
            }
        }

        return list;
    }

    public static IChatComponent createSubscriptionComponent(ChatUserSubscription subscription, String channelName, boolean ownChannel)
    {
        IChatComponent ichatcomponent = null;

        if (subscription == ChatUserSubscription.TTV_CHAT_USERSUB_SUBSCRIBER)
        {
            if (channelName == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.subscription.subscriber", new Object[0]);
            }
            else if (ownChannel)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.subscription.subscriber.self", new Object[0]);
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.subscription.subscriber.other", new Object[] {channelName});
            }

            ichatcomponent.getChatStyle().setColor(SUBSCRIBER_COLOR);
        }
        else if (subscription == ChatUserSubscription.TTV_CHAT_USERSUB_TURBO)
        {
            ichatcomponent = new ChatComponentTranslation("stream.user.subscription.turbo", new Object[0]);
            ichatcomponent.getChatStyle().setColor(SPECIAL_USER_COLOR);
        }

        return ichatcomponent;
    }

    public static IChatComponent createModeComponent(ChatUserMode mode, String channelName, boolean ownChannel)
    {
        IChatComponent ichatcomponent = null;

        if (mode == ChatUserMode.TTV_CHAT_USERMODE_ADMINSTRATOR)
        {
            ichatcomponent = new ChatComponentTranslation("stream.user.mode.administrator", new Object[0]);
            ichatcomponent.getChatStyle().setColor(SPECIAL_USER_COLOR);
        }
        else if (mode == ChatUserMode.TTV_CHAT_USERMODE_BANNED)
        {
            if (channelName == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.banned", new Object[0]);
            }
            else if (ownChannel)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.banned.self", new Object[0]);
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.banned.other", new Object[] {channelName});
            }

            ichatcomponent.getChatStyle().setColor(BANNED_COLOR);
        }
        else if (mode == ChatUserMode.TTV_CHAT_USERMODE_BROADCASTER)
        {
            if (channelName == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.broadcaster", new Object[0]);
            }
            else if (ownChannel)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.broadcaster.self", new Object[0]);
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.broadcaster.other", new Object[0]);
            }

            ichatcomponent.getChatStyle().setColor(SUBSCRIBER_COLOR);
        }
        else if (mode == ChatUserMode.TTV_CHAT_USERMODE_MODERATOR)
        {
            if (channelName == null)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.moderator", new Object[0]);
            }
            else if (ownChannel)
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.moderator.self", new Object[0]);
            }
            else
            {
                ichatcomponent = new ChatComponentTranslation("stream.user.mode.moderator.other", new Object[] {channelName});
            }

            ichatcomponent.getChatStyle().setColor(SUBSCRIBER_COLOR);
        }
        else if (mode == ChatUserMode.TTV_CHAT_USERMODE_STAFF)
        {
            ichatcomponent = new ChatComponentTranslation("stream.user.mode.staff", new Object[0]);
            ichatcomponent.getChatStyle().setColor(SPECIAL_USER_COLOR);
        }

        return ichatcomponent;
    }

    public void initGui()
    {
        int i = this.width / 3;
        int j = i - 130;
        this.buttonList.add(new GuiButton(1, i * 0 + j / 2, this.height - 70, 130, 20, I18n.format("stream.userinfo.timeout", new Object[0])));
        this.buttonList.add(new GuiButton(0, i * 1 + j / 2, this.height - 70, 130, 20, I18n.format("stream.userinfo.ban", new Object[0])));
        this.buttonList.add(new GuiButton(2, i * 2 + j / 2, this.height - 70, 130, 20, I18n.format("stream.userinfo.mod", new Object[0])));
        this.buttonList.add(new GuiButton(5, i * 0 + j / 2, this.height - 45, 130, 20, I18n.format("gui.cancel", new Object[0])));
        this.buttonList.add(new GuiButton(3, i * 1 + j / 2, this.height - 45, 130, 20, I18n.format("stream.userinfo.unban", new Object[0])));
        this.buttonList.add(new GuiButton(4, i * 2 + j / 2, this.height - 45, 130, 20, I18n.format("stream.userinfo.unmod", new Object[0])));
        int k = 0;

        for (IChatComponent ichatcomponent : this.userModeLines)
        {
            k = Math.max(k, this.fontRendererObj.getStringWidth(ichatcomponent.getFormattedText()));
        }

        this.modeLineX = this.width / 2 - k / 2;
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                this.stream.sendChatMessage("/ban " + this.userInfo.displayName);
            }
            else if (button.id == 3)
            {
                this.stream.sendChatMessage("/unban " + this.userInfo.displayName);
            }
            else if (button.id == 2)
            {
                this.stream.sendChatMessage("/mod " + this.userInfo.displayName);
            }
            else if (button.id == 4)
            {
                this.stream.sendChatMessage("/unmod " + this.userInfo.displayName);
            }
            else if (button.id == 1)
            {
                this.stream.sendChatMessage("/timeout " + this.userInfo.displayName);
            }

            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.displayNameComponent.getUnformattedText(), this.width / 2, 70, 16777215);
        int i = 80;

        for (IChatComponent ichatcomponent : this.userModeLines)
        {
            this.drawString(this.fontRendererObj, ichatcomponent.getFormattedText(), this.modeLineX, i, 16777215);
            i += this.fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
