package net.minecraft.client.stream;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.opengl.GL11;
import tv.twitch.AuthToken;
import tv.twitch.ErrorCode;
import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.GameInfo;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.VideoParams;
import tv.twitch.chat.ChatRawMessage;
import tv.twitch.chat.ChatTokenizedMessage;
import tv.twitch.chat.ChatUserInfo;
import tv.twitch.chat.ChatUserMode;
import tv.twitch.chat.ChatUserSubscription;

public class TwitchStream implements BroadcastController.BroadcastListener, ChatController.ChatListener, IngestServerTester.IngestTestListener, IStream
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Marker STREAM_MARKER = MarkerManager.getMarker("STREAM");
    private final BroadcastController broadcastController;
    private final ChatController chatController;
    private String channelName;
    private final Minecraft mc;
    private final IChatComponent twitchComponent = new ChatComponentText("Twitch");
    private final Map<String, ChatUserInfo> chatUsers = Maps.<String, ChatUserInfo>newHashMap();
    private Framebuffer framebuffer;
    private boolean sendMetadata;
    private int targetFPS = 30;
    private long lastFrameTimeNanos = 0L;
    private boolean ingestTestComplete = false;
    private boolean loggedIn;
    private boolean paused;
    private boolean microphoneMuted;
    private IStream.AuthFailureReason authFailureReason = IStream.AuthFailureReason.ERROR;
    private static boolean twitchNativeLibrariesLoaded;

    public TwitchStream(Minecraft mcIn, final Property streamProperty)
    {
        this.mc = mcIn;
        this.broadcastController = new BroadcastController();
        this.chatController = new ChatController();
        this.broadcastController.setBroadcastListener(this);
        this.chatController.setChatListener(this);
        this.broadcastController.setClientId("nmt37qblda36pvonovdkbopzfzw3wlq");
        this.chatController.setClientId("nmt37qblda36pvonovdkbopzfzw3wlq");
        this.twitchComponent.getChatStyle().setColor(EnumChatFormatting.DARK_PURPLE);

        if (streamProperty != null && !Strings.isNullOrEmpty(streamProperty.getValue()) && OpenGlHelper.framebufferSupported)
        {
            Thread thread = new Thread("Twitch authenticator")
            {
                public void run()
                {
                    try
                    {
                        URL url = new URL("https://api.twitch.tv/kraken?oauth_token=" + URLEncoder.encode(streamProperty.getValue(), "UTF-8"));
                        String s = HttpUtil.get(url);
                        JsonObject jsonobject = JsonUtils.getJsonObject((new JsonParser()).parse(s), "Response");
                        JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "token");

                        if (JsonUtils.getBoolean(jsonobject1, "valid"))
                        {
                            String stringValue = JsonUtils.getString(jsonobject1, "user_name");
                            TwitchStream.LOGGER.debug(TwitchStream.STREAM_MARKER, "Authenticated with twitch; username is {}", new Object[] {stringValue});
                            AuthToken authtoken = new AuthToken();
                            authtoken.data = streamProperty.getValue();
                            TwitchStream.this.broadcastController.authenticate(stringValue, authtoken);
                            TwitchStream.this.chatController.setUsername(stringValue);
                            TwitchStream.this.chatController.setAuthToken(authtoken);
                            Runtime.getRuntime().addShutdownHook(new Thread("Twitch shutdown hook")
                            {
                                public void run()
                                {
                                    TwitchStream.this.shutdownStream();
                                }
                            });
                            TwitchStream.this.broadcastController.initialize();
                            TwitchStream.this.chatController.initialize();
                        }
                        else
                        {
                            TwitchStream.this.authFailureReason = IStream.AuthFailureReason.INVALID_TOKEN;
                            TwitchStream.LOGGER.error(TwitchStream.STREAM_MARKER, "Given twitch access token is invalid");
                        }
                    }
                    catch (IOException ioexception)
                    {
                        TwitchStream.this.authFailureReason = IStream.AuthFailureReason.ERROR;
                        TwitchStream.LOGGER.error(TwitchStream.STREAM_MARKER, (String)"Could not authenticate with twitch", (Throwable)ioexception);
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void shutdownStream()
    {
        LOGGER.debug(STREAM_MARKER, "Shutdown streaming");
        this.broadcastController.statCallback();
        this.chatController.shutdownAndWait();
    }

    public void updateStream()
    {
        int i = this.mc.gameSettings.streamChatEnabled;
        boolean flag = this.channelName != null && this.chatController.isChannelConnected(this.channelName);
        boolean flag1 = this.chatController.getState() == ChatController.ChatState.Initialized && (this.channelName == null || this.chatController.getChannelState(this.channelName) == ChatController.EnumChannelState.Disconnected);

        if (i == 2)
        {
            if (flag)
            {
                LOGGER.debug(STREAM_MARKER, "Disconnecting from twitch chat per user options");
                this.chatController.disconnect(this.channelName);
            }
        }
        else if (i == 1)
        {
            if (flag1 && this.broadcastController.isLoggedIn())
            {
                LOGGER.debug(STREAM_MARKER, "Connecting to twitch chat per user options");
                this.connectChat();
            }
        }
        else if (i == 0)
        {
            if (flag && !this.isBroadcasting())
            {
                LOGGER.debug(STREAM_MARKER, "Disconnecting from twitch chat as user is no longer streaming");
                this.chatController.disconnect(this.channelName);
            }
            else if (flag1 && this.isBroadcasting())
            {
                LOGGER.debug(STREAM_MARKER, "Connecting to twitch chat as user is streaming");
                this.connectChat();
            }
        }

        this.broadcastController.pollTasks();
        this.chatController.flushEvents();
    }

    protected void connectChat()
    {
        ChatController.ChatState chatcontroller$chatstate = this.chatController.getState();
        String s = this.broadcastController.getChannelInfo().name;
        this.channelName = s;

        if (chatcontroller$chatstate != ChatController.ChatState.Initialized)
        {
            LOGGER.warn("Invalid twitch chat state {}", new Object[] {chatcontroller$chatstate});
        }
        else if (this.chatController.getChannelState(this.channelName) == ChatController.EnumChannelState.Disconnected)
        {
            this.chatController.connect(s);
        }
        else
        {
            LOGGER.warn("Invalid twitch chat state {}", new Object[] {chatcontroller$chatstate});
        }
    }

    public void submitStreamFrame()
    {
        if (this.broadcastController.isBroadcasting() && !this.broadcastController.isBroadcastPaused())
        {
            long i = System.nanoTime();
            long j = (long)(1000000000 / this.targetFPS);
            long k = i - this.lastFrameTimeNanos;
            boolean flag = k >= j;

            if (flag)
            {
                FrameBuffer framebuffer = this.broadcastController.getFreeFrameBuffer();
                Framebuffer framebuffer1 = this.mc.getFramebuffer();
                this.framebuffer.bindFramebuffer(true);
                GlStateManager.matrixMode(5889);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, (double)this.framebuffer.framebufferWidth, (double)this.framebuffer.framebufferHeight, 0.0D, 1000.0D, 3000.0D);
                GlStateManager.matrixMode(5888);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, 0.0F, -2000.0F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.viewport(0, 0, this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
                GlStateManager.enableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                float f = (float)this.framebuffer.framebufferWidth;
                float floatValue = (float)this.framebuffer.framebufferHeight;
                float secondFloatValue = (float)framebuffer1.framebufferWidth / (float)framebuffer1.framebufferTextureWidth;
                float thirdFloatValue = (float)framebuffer1.framebufferHeight / (float)framebuffer1.framebufferTextureHeight;
                framebuffer1.bindFramebufferTexture();
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 9729.0F);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729.0F);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(0.0D, (double)floatValue, 0.0D).tex(0.0D, (double)thirdFloatValue).endVertex();
                worldrenderer.pos((double)f, (double)floatValue, 0.0D).tex((double)secondFloatValue, (double)thirdFloatValue).endVertex();
                worldrenderer.pos((double)f, 0.0D, 0.0D).tex((double)secondFloatValue, 0.0D).endVertex();
                worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
                framebuffer1.unbindFramebufferTexture();
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5889);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                this.broadcastController.captureFramebuffer(framebuffer);
                this.framebuffer.unbindFramebuffer();
                this.broadcastController.submitStreamFrame(framebuffer);
                this.lastFrameTimeNanos = i;
            }
        }
    }

    public boolean isLoggedIn()
    {
        return this.broadcastController.isLoggedIn();
    }

    public boolean isReadyToBroadcast()
    {
        return this.broadcastController.isReadyToBroadcast();
    }

    public boolean isBroadcasting()
    {
        return this.broadcastController.isBroadcasting();
    }

    public void sendMetadataAction(Metadata metadata, long offsetMillis)
    {
        if (this.isBroadcasting() && this.sendMetadata)
        {
            long i = this.broadcastController.getStreamTime();

            if (!this.broadcastController.sendActionMetadata(metadata.getName(), i + offsetMillis, metadata.getDescription(), metadata.getPayloadJson()))
            {
                LOGGER.warn(STREAM_MARKER, "Couldn\'t send stream metadata action at {}: {}", new Object[] {Long.valueOf(i + offsetMillis), metadata});
            }
            else
            {
                LOGGER.debug(STREAM_MARKER, "Sent stream metadata action at {}: {}", new Object[] {Long.valueOf(i + offsetMillis), metadata});
            }
        }
    }

    public void sendMetadataSequence(Metadata metadata, long startOffsetMillis, long endOffsetMillis)
    {
        if (this.isBroadcasting() && this.sendMetadata)
        {
            long i = this.broadcastController.getStreamTime();
            String s = metadata.getDescription();
            String secondStringValue = metadata.getPayloadJson();
            long j = this.broadcastController.sendStartSpanMetadata(metadata.getName(), i + startOffsetMillis, s, secondStringValue);

            if (j < 0L)
            {
                LOGGER.warn(STREAM_MARKER, "Could not send stream metadata sequence from {} to {}: {}", new Object[] {Long.valueOf(i + startOffsetMillis), Long.valueOf(i + endOffsetMillis), metadata});
            }
            else if (this.broadcastController.sendEndSpanMetadata(metadata.getName(), i + endOffsetMillis, j, s, secondStringValue))
            {
                LOGGER.debug(STREAM_MARKER, "Sent stream metadata sequence from {} to {}: {}", new Object[] {Long.valueOf(i + startOffsetMillis), Long.valueOf(i + endOffsetMillis), metadata});
            }
            else
            {
                LOGGER.warn(STREAM_MARKER, "Half-sent stream metadata sequence from {} to {}: {}", new Object[] {Long.valueOf(i + startOffsetMillis), Long.valueOf(i + endOffsetMillis), metadata});
            }
        }
    }

    public boolean isPaused()
    {
        return this.broadcastController.isBroadcastPaused();
    }

    public void requestCommercial()
    {
        if (this.broadcastController.requestCommercial())
        {
            LOGGER.debug(STREAM_MARKER, "Requested commercial from Twitch");
        }
        else
        {
            LOGGER.warn(STREAM_MARKER, "Could not request commercial from Twitch");
        }
    }

    public void pause()
    {
        this.broadcastController.pauseBroadcasting();
        this.paused = true;
        this.updateStreamVolume();
    }

    public void unpause()
    {
        this.broadcastController.resumeBroadcasting();
        this.paused = false;
        this.updateStreamVolume();
    }

    public void updateStreamVolume()
    {
        if (this.isBroadcasting())
        {
            float f = this.mc.gameSettings.streamGameVolume;
            boolean flag = this.paused || f <= 0.0F;
            this.broadcastController.setPlaybackDeviceVolume(flag ? 0.0F : f);
            this.broadcastController.setRecordingDeviceVolume(this.isMicrophoneMuted() ? 0.0F : this.mc.gameSettings.streamMicVolume);
        }
    }

    public void startBroadcasting()
    {
        GameSettings gamesettings = this.mc.gameSettings;
        VideoParams videoparams = this.broadcastController.getRecommendedVideoParams(formatStreamKbps(gamesettings.streamKbps), formatStreamFps(gamesettings.streamFps), formatStreamBps(gamesettings.streamBytesPerPixel), (float)this.mc.displayWidth / (float)this.mc.displayHeight);

        switch (gamesettings.streamCompression)
        {
            case 0:
                videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_LOW;
                break;

            case 1:
                videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_MEDIUM;
                break;

            case 2:
                videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
        }

        if (this.framebuffer == null)
        {
            this.framebuffer = new Framebuffer(videoparams.outputWidth, videoparams.outputHeight, false);
        }
        else
        {
            this.framebuffer.createBindFramebuffer(videoparams.outputWidth, videoparams.outputHeight);
        }

        if (gamesettings.streamPreferredServer != null && gamesettings.streamPreferredServer.length() > 0)
        {
            for (IngestServer ingestserver : this.getIngestServers())
            {
                if (ingestserver.serverUrl.equals(gamesettings.streamPreferredServer))
                {
                    this.broadcastController.setIngestServer(ingestserver);
                    break;
                }
            }
        }

        this.targetFPS = videoparams.targetFps;
        this.sendMetadata = gamesettings.streamSendMetadata;
        this.broadcastController.startBroadcasting(videoparams);
        LOGGER.info(STREAM_MARKER, "Streaming at {}/{} at {} kbps to {}", new Object[] {Integer.valueOf(videoparams.outputWidth), Integer.valueOf(videoparams.outputHeight), Integer.valueOf(videoparams.maxKbps), this.broadcastController.getIngestServer().serverUrl});
        this.broadcastController.setStreamInfo((String)null, "Minecraft", (String)null);
    }

    public void stopBroadcasting()
    {
        if (this.broadcastController.stopBroadcasting())
        {
            LOGGER.info(STREAM_MARKER, "Stopped streaming to Twitch");
        }
        else
        {
            LOGGER.warn(STREAM_MARKER, "Could not stop streaming to Twitch");
        }
    }

    public void onAuthTokenRequestComplete(ErrorCode authError, AuthToken authToken)
    {
    }

    public void onLogin(ErrorCode loginError)
    {
        if (ErrorCode.succeeded(loginError))
        {
            LOGGER.debug(STREAM_MARKER, "Login attempt successful");
            this.loggedIn = true;
        }
        else
        {
            LOGGER.warn(STREAM_MARKER, "Login attempt unsuccessful: {} (error code {})", new Object[] {ErrorCode.getString(loginError), Integer.valueOf(loginError.getValue())});
            this.loggedIn = false;
        }
    }

    public void onGameNameListReceived(ErrorCode gameListError, GameInfo[] games)
    {
    }

    public void onBroadcastStateChanged(BroadcastController.BroadcastState newState)
    {
        LOGGER.debug(STREAM_MARKER, "Broadcast state changed to {}", new Object[] {newState});

        if (newState == BroadcastController.BroadcastState.Initialized)
        {
            this.broadcastController.setBroadcastState(BroadcastController.BroadcastState.Authenticated);
        }
    }

    public void onLoggedOut()
    {
        LOGGER.info(STREAM_MARKER, "Logged out of twitch");
    }

    public void onStreamInfoUpdated(StreamInfo streamInfo)
    {
        LOGGER.debug(STREAM_MARKER, "Stream info updated; {} viewers on stream ID {}", new Object[] {Integer.valueOf(streamInfo.viewers), Long.valueOf(streamInfo.streamId)});
    }

    public void onIngestListReceived(IngestList ingestList)
    {
    }

    public void onFrameSubmissionIssue(ErrorCode frameError)
    {
        LOGGER.warn(STREAM_MARKER, "Issue submitting frame: {} (Error code {})", new Object[] {ErrorCode.getString(frameError), Integer.valueOf(frameError.getValue())});
        this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText("Issue streaming frame: " + frameError + " (" + ErrorCode.getString(frameError) + ")"), 2);
    }

    public void onBroadcastStarted()
    {
        this.updateStreamVolume();
        LOGGER.info(STREAM_MARKER, "Broadcast to Twitch has started");
    }

    public void onBroadcastStopped()
    {
        LOGGER.info(STREAM_MARKER, "Broadcast to Twitch has stopped");
    }

    public void onBroadcastStartFailed(ErrorCode startError)
    {
        if (startError == ErrorCode.TTV_EC_SOUNDFLOWER_NOT_INSTALLED)
        {
            IChatComponent ichatcomponent = new ChatComponentTranslation("stream.unavailable.soundflower.chat.link", new Object[0]);
            ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://help.mojang.com/customer/portal/articles/1374877-configuring-soundflower-for-streaming-on-apple-computers"));
            ichatcomponent.getChatStyle().setUnderlined(Boolean.valueOf(true));
            IChatComponent ichatcomponent1 = new ChatComponentTranslation("stream.unavailable.soundflower.chat", new Object[] {ichatcomponent});
            ichatcomponent1.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
            this.mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent1);
        }
        else
        {
            IChatComponent ichatcomponent2 = new ChatComponentTranslation("stream.unavailable.unknown.chat", new Object[] {ErrorCode.getString(startError)});
            ichatcomponent2.getChatStyle().setColor(EnumChatFormatting.DARK_RED);
            this.mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent2);
        }
    }

    public void onIngestTestStateChanged(IngestServerTester tester, IngestServerTester.IngestTestState state)
    {
        LOGGER.debug(STREAM_MARKER, "Ingest test state changed to {}", new Object[] {state});

        if (state == IngestServerTester.IngestTestState.Finished)
        {
            this.ingestTestComplete = true;
        }
    }

    public static int formatStreamFps(float sliderValue)
    {
        return MathHelper.floor_float(10.0F + sliderValue * 50.0F);
    }

    public static int formatStreamKbps(float sliderValue)
    {
        return MathHelper.floor_float(230.0F + sliderValue * 3270.0F);
    }

    public static float formatStreamBps(float sliderValue)
    {
        return 0.1F + sliderValue * 0.1F;
    }

    public IngestServer[] getIngestServers()
    {
        return this.broadcastController.getIngestList().getServers();
    }

    public void startIngestTest()
    {
        IngestServerTester ingestservertester = this.broadcastController.startIngestTest();

        if (ingestservertester != null)
        {
            ingestservertester.setListener(this);
        }
    }

    public IngestServerTester getIngestServerTester()
    {
        return this.broadcastController.isReady();
    }

    public boolean isIngestTesting()
    {
        return this.broadcastController.isIngestTesting();
    }

    public int getViewerCount()
    {
        return this.isBroadcasting() ? this.broadcastController.getStreamInfo().viewers : 0;
    }

    public void onChatInitialized(ErrorCode initError)
    {
        if (ErrorCode.failed(initError))
        {
            LOGGER.error(STREAM_MARKER, "Chat failed to initialize");
        }
    }

    public void onChatShutdown(ErrorCode shutdownError)
    {
        if (ErrorCode.failed(shutdownError))
        {
            LOGGER.error(STREAM_MARKER, "Chat failed to shutdown");
        }
    }

    public void onChatStateChanged(ChatController.ChatState chatState)
    {
    }

    public void onRawMessages(String channel, ChatRawMessage[] messages)
    {
        for (ChatRawMessage chatrawmessage : messages)
        {
            this.updateChatUserInfo(chatrawmessage.userName, chatrawmessage);

            if (this.shouldShowChatMessage(chatrawmessage.modes, chatrawmessage.subscriptions, this.mc.gameSettings.streamChatUserFilter))
            {
                IChatComponent ichatcomponent = new ChatComponentText(chatrawmessage.userName);
                IChatComponent ichatcomponent1 = new ChatComponentTranslation("chat.stream." + (chatrawmessage.action ? "emote" : "text"), new Object[] {this.twitchComponent, ichatcomponent, EnumChatFormatting.getTextWithoutFormattingCodes(chatrawmessage.message)});

                if (chatrawmessage.action)
                {
                    ichatcomponent1.getChatStyle().setItalic(Boolean.valueOf(true));
                }

                IChatComponent ichatcomponent2 = new ChatComponentText("");
                ichatcomponent2.appendSibling(new ChatComponentTranslation("stream.userinfo.chatTooltip", new Object[0]));

                for (IChatComponent ichatcomponent3 : GuiTwitchUserMode.createUserModeLines(chatrawmessage.modes, chatrawmessage.subscriptions, (IStream)null))
                {
                    ichatcomponent2.appendText("\n");
                    ichatcomponent2.appendSibling(ichatcomponent3);
                }

                ichatcomponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ichatcomponent2));
                ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.TWITCH_USER_INFO, chatrawmessage.userName));
                this.mc.ingameGUI.getChatGUI().printChatMessage(ichatcomponent1);
            }
        }
    }

    public void onTokenizedMessages(String channel, ChatTokenizedMessage[] messages)
    {
    }

    private void updateChatUserInfo(String userName, ChatRawMessage chatMessage)
    {
        ChatUserInfo chatuserinfo = this.chatUsers.get(userName);

        if (chatuserinfo == null)
        {
            chatuserinfo = new ChatUserInfo();
            chatuserinfo.displayName = userName;
            this.chatUsers.put(userName, chatuserinfo);
        }

        chatuserinfo.subscriptions = chatMessage.subscriptions;
        chatuserinfo.modes = chatMessage.modes;
        chatuserinfo.nameColorARGB = chatMessage.nameColorARGB;
    }

    private boolean shouldShowChatMessage(Set<ChatUserMode> modes, Set<ChatUserSubscription> subscriptions, int filterLevel)
    {
        return modes.contains(ChatUserMode.TTV_CHAT_USERMODE_BANNED) ? false : (modes.contains(ChatUserMode.TTV_CHAT_USERMODE_ADMINSTRATOR) ? true : (modes.contains(ChatUserMode.TTV_CHAT_USERMODE_MODERATOR) ? true : (modes.contains(ChatUserMode.TTV_CHAT_USERMODE_STAFF) ? true : (filterLevel == 0 ? true : (filterLevel == 1 ? subscriptions.contains(ChatUserSubscription.TTV_CHAT_USERSUB_SUBSCRIBER) : false)))));
    }

    public void onUsersChanged(String channel, ChatUserInfo[] joinedUsers, ChatUserInfo[] leftUsers, ChatUserInfo[] updatedUsers)
    {
        for (ChatUserInfo chatuserinfo : leftUsers)
        {
            this.chatUsers.remove(chatuserinfo.displayName);
        }

        for (ChatUserInfo chatuserinfo1 : updatedUsers)
        {
            this.chatUsers.put(chatuserinfo1.displayName, chatuserinfo1);
        }

        for (ChatUserInfo chatuserinfo2 : joinedUsers)
        {
            this.chatUsers.put(chatuserinfo2.displayName, chatuserinfo2);
        }
    }

    public void onChannelJoined(String channel)
    {
        LOGGER.debug(STREAM_MARKER, "Chat connected");
    }

    public void onChannelLeft(String channel)
    {
        LOGGER.debug(STREAM_MARKER, "Chat disconnected");
        this.chatUsers.clear();
    }

    public void onUserMessagesCleared(String channel, String userName)
    {
    }

    public void onEmoticonDataReady()
    {
    }

    public void onEmoticonDataCleared()
    {
    }

    public void onBadgeDataReady(String channel)
    {
    }

    public void onBadgeDataCleared(String channel)
    {
    }

    public boolean isChannelOwner()
    {
        return this.channelName != null && this.channelName.equals(this.broadcastController.getChannelInfo().name);
    }

    public String getChannelName()
    {
        return this.channelName;
    }

    public ChatUserInfo getChatUserInfo(String userName)
    {
        return this.chatUsers.get(userName);
    }

    public void sendChatMessage(String message)
    {
        this.chatController.sendMessage(this.channelName, message);
    }

    public boolean isStreamingSupported()
    {
        return twitchNativeLibrariesLoaded && this.broadcastController.isInitialized();
    }

    public ErrorCode getStreamErrorCode()
    {
        return !twitchNativeLibrariesLoaded ? ErrorCode.TTV_EC_OS_TOO_OLD : this.broadcastController.getErrorCode();
    }

    public boolean isAuthenticated()
    {
        return this.loggedIn;
    }

    public void muteMicrophone(boolean muted)
    {
        this.microphoneMuted = muted;
        this.updateStreamVolume();
    }

    public boolean isMicrophoneMuted()
    {
        boolean flag = this.mc.gameSettings.streamMicToggleBehavior == 1;
        return this.paused || this.mc.gameSettings.streamMicVolume <= 0.0F || flag != this.microphoneMuted;
    }

    public IStream.AuthFailureReason getAuthFailureReason()
    {
        return this.authFailureReason;
    }

    static
    {
        try
        {
            if (Util.getOSType() == Util.EnumOS.WINDOWS)
            {
                System.loadLibrary("avutil-ttv-51");
                System.loadLibrary("swresample-ttv-0");
                System.loadLibrary("libmp3lame-ttv");

                if (System.getProperty("os.arch").contains("64"))
                {
                    System.loadLibrary("libmfxsw64");
                }
                else
                {
                    System.loadLibrary("libmfxsw32");
                }
            }

            twitchNativeLibrariesLoaded = true;
        }
        catch (Throwable caughtThrowable)
        {
            twitchNativeLibrariesLoaded = false;
        }
    }
}
