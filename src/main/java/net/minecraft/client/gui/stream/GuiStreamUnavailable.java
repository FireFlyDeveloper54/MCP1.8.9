package net.minecraft.client.gui.stream;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.IStream;
import net.minecraft.client.stream.NullStream;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import tv.twitch.ErrorCode;

public class GuiStreamUnavailable extends GuiScreen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final IChatComponent title;
    private final GuiScreen parentScreen;
    private final GuiStreamUnavailable.Reason reason;
    private final List<ChatComponentTranslation> extraMessages;
    private final List<String> messageLines;

    public GuiStreamUnavailable(GuiScreen parentScreenIn, GuiStreamUnavailable.Reason reasonIn)
    {
        this(parentScreenIn, reasonIn, (List<ChatComponentTranslation>)null);
    }

    public GuiStreamUnavailable(GuiScreen parentScreenIn, GuiStreamUnavailable.Reason reasonIn, List<ChatComponentTranslation> extraMessagesIn)
    {
        this.title = new ChatComponentTranslation("stream.unavailable.title", new Object[0]);
        this.messageLines = Lists.<String>newArrayList();
        this.parentScreen = parentScreenIn;
        this.reason = reasonIn;
        this.extraMessages = extraMessagesIn;
    }

    public void initGui()
    {
        if (this.messageLines.isEmpty())
        {
            this.messageLines.addAll(this.fontRendererObj.listFormattedStringToWidth(this.reason.getMessage().getFormattedText(), (int)((float)this.width * 0.75F)));

            if (this.extraMessages != null)
            {
                this.messageLines.add("");

                for (ChatComponentTranslation extraMessage : this.extraMessages)
                {
                    this.messageLines.add(extraMessage.getUnformattedTextForChat());
                }
            }
        }

        if (this.reason.getActionButtonText() != null)
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 50, 150, 20, I18n.format("gui.cancel", new Object[0])));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 155 + 160, this.height - 50, 150, 20, I18n.format(this.reason.getActionButtonText().getFormattedText(), new Object[0])));
        }
        else
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height - 50, 150, 20, I18n.format("gui.cancel", new Object[0])));
        }
    }

    public void onGuiClosed()
    {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        int messageY = Math.max((int)((double)this.height * 0.85D / 2.0D - (double)((float)(this.messageLines.size() * this.fontRendererObj.FONT_HEIGHT) / 2.0F)), 50);
        this.drawCenteredString(this.fontRendererObj, this.title.getFormattedText(), this.width / 2, messageY - this.fontRendererObj.FONT_HEIGHT * 2, 16777215);

        for (String messageLine : this.messageLines)
        {
            this.drawCenteredString(this.fontRendererObj, messageLine, this.width / 2, messageY, 10526880);
            messageY += this.fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("incomplete-switch")
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                switch (this.reason)
                {
                    case ACCOUNT_NOT_BOUND:
                    case FAILED_TWITCH_AUTH:
                        this.openLink("https://account.mojang.com/me/settings");
                        break;

                    case ACCOUNT_NOT_MIGRATED:
                        this.openLink("https://account.mojang.com/migrate");
                        break;

                    case UNSUPPORTED_OS_MAC:
                        this.openLink("http://www.apple.com/osx/");
                        break;

                    case UNKNOWN:
                    case LIBRARY_FAILURE:
                    case INITIALIZATION_FAILURE:
                        this.openLink("http://bugs.mojang.com/browse/MC");
                }
            }

            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    private void openLink(String url)
    {
        try
        {
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            Object desktopInstance = desktopClass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
            desktopClass.getMethod("browse", new Class[] {URI.class}).invoke(desktopInstance, new Object[] {new URI(url)});
        }
        catch (Throwable throwable)
        {
            LOGGER.error("Couldn\'t open link", throwable);
        }
    }

    public static void show(GuiScreen parentScreenIn)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        IStream stream = minecraft.getTwitchStream();

        if (!OpenGlHelper.framebufferSupported)
        {
            List<ChatComponentTranslation> framebufferMessages = Lists.<ChatComponentTranslation>newArrayList();
            framebufferMessages.add(new ChatComponentTranslation("stream.unavailable.no_fbo.version", new Object[] {GL11.glGetString(GL11.GL_VERSION)}));
            framebufferMessages.add(new ChatComponentTranslation("stream.unavailable.no_fbo.blend", new Object[] {Boolean.valueOf(GLContext.getCapabilities().GL_EXT_blend_func_separate)}));
            framebufferMessages.add(new ChatComponentTranslation("stream.unavailable.no_fbo.arb", new Object[] {Boolean.valueOf(GLContext.getCapabilities().GL_ARB_framebuffer_object)}));
            framebufferMessages.add(new ChatComponentTranslation("stream.unavailable.no_fbo.ext", new Object[] {Boolean.valueOf(GLContext.getCapabilities().GL_EXT_framebuffer_object)}));
            minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.NO_FBO, framebufferMessages));
        }
        else if (stream instanceof NullStream)
        {
            if (((NullStream)stream).getFailureCause().getMessage().contains("Can\'t load AMD 64-bit .dll on a IA 32-bit platform"))
            {
                minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.LIBRARY_ARCH_MISMATCH));
            }
            else
            {
                minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.LIBRARY_FAILURE));
            }
        }
        else if (!stream.isStreamingSupported() && stream.getStreamErrorCode() == ErrorCode.TTV_EC_OS_TOO_OLD)
        {
            switch (Util.getOSType())
            {
                case WINDOWS:
                    minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.UNSUPPORTED_OS_WINDOWS));
                    break;

                case OSX:
                    minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.UNSUPPORTED_OS_MAC));
                    break;

                default:
                    minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.UNSUPPORTED_OS_OTHER));
            }
        }
        else if (!minecraft.getTwitchDetails().containsKey("twitch_access_token"))
        {
            if (minecraft.getSession().getSessionType() == Session.Type.LEGACY)
            {
                minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.ACCOUNT_NOT_MIGRATED));
            }
            else
            {
                minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.ACCOUNT_NOT_BOUND));
            }
        }
        else if (!stream.isAuthenticated())
        {
            switch (stream.getAuthFailureReason())
            {
                case INVALID_TOKEN:
                    minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.FAILED_TWITCH_AUTH));
                    break;

                case ERROR:
                default:
                    minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.FAILED_TWITCH_AUTH_ERROR));
            }
        }
        else if (stream.getStreamErrorCode() != null)
        {
            List<ChatComponentTranslation> initializationFailureMessages = Arrays.<ChatComponentTranslation>asList(new ChatComponentTranslation[] {new ChatComponentTranslation("stream.unavailable.initialization_failure.extra", new Object[]{ErrorCode.getString(stream.getStreamErrorCode())})});
            minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.INITIALIZATION_FAILURE, initializationFailureMessages));
        }
        else
        {
            minecraft.displayGuiScreen(new GuiStreamUnavailable(parentScreenIn, GuiStreamUnavailable.Reason.UNKNOWN));
        }
    }

    public static enum Reason
    {
        NO_FBO(new ChatComponentTranslation("stream.unavailable.no_fbo", new Object[0])),
        LIBRARY_ARCH_MISMATCH(new ChatComponentTranslation("stream.unavailable.library_arch_mismatch", new Object[0])),
        LIBRARY_FAILURE(new ChatComponentTranslation("stream.unavailable.library_failure", new Object[0]), new ChatComponentTranslation("stream.unavailable.report_to_mojang", new Object[0])),
        UNSUPPORTED_OS_WINDOWS(new ChatComponentTranslation("stream.unavailable.not_supported.windows", new Object[0])),
        UNSUPPORTED_OS_MAC(new ChatComponentTranslation("stream.unavailable.not_supported.mac", new Object[0]), new ChatComponentTranslation("stream.unavailable.not_supported.mac.okay", new Object[0])),
        UNSUPPORTED_OS_OTHER(new ChatComponentTranslation("stream.unavailable.not_supported.other", new Object[0])),
        ACCOUNT_NOT_MIGRATED(new ChatComponentTranslation("stream.unavailable.account_not_migrated", new Object[0]), new ChatComponentTranslation("stream.unavailable.account_not_migrated.okay", new Object[0])),
        ACCOUNT_NOT_BOUND(new ChatComponentTranslation("stream.unavailable.account_not_bound", new Object[0]), new ChatComponentTranslation("stream.unavailable.account_not_bound.okay", new Object[0])),
        FAILED_TWITCH_AUTH(new ChatComponentTranslation("stream.unavailable.failed_auth", new Object[0]), new ChatComponentTranslation("stream.unavailable.failed_auth.okay", new Object[0])),
        FAILED_TWITCH_AUTH_ERROR(new ChatComponentTranslation("stream.unavailable.failed_auth_error", new Object[0])),
        INITIALIZATION_FAILURE(new ChatComponentTranslation("stream.unavailable.initialization_failure", new Object[0]), new ChatComponentTranslation("stream.unavailable.report_to_mojang", new Object[0])),
        UNKNOWN(new ChatComponentTranslation("stream.unavailable.unknown", new Object[0]), new ChatComponentTranslation("stream.unavailable.report_to_mojang", new Object[0]));

        private final IChatComponent message;
        private final IChatComponent actionButtonText;

        private Reason(IChatComponent messageIn)
        {
            this(messageIn, (IChatComponent)null);
        }

        private Reason(IChatComponent messageIn, IChatComponent actionButtonTextIn)
        {
            this.message = messageIn;
            this.actionButtonText = actionButtonTextIn;
        }

        public IChatComponent getMessage()
        {
            return this.message;
        }

        public IChatComponent getActionButtonText()
        {
            return this.actionButtonText;
        }
    }
}
