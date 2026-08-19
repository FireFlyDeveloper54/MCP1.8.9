package net.minecraft.client.stream;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tv.twitch.AuthToken;
import tv.twitch.Core;
import tv.twitch.ErrorCode;
import tv.twitch.StandardCoreAPI;
import tv.twitch.chat.Chat;
import tv.twitch.chat.ChatBadgeData;
import tv.twitch.chat.ChatChannelInfo;
import tv.twitch.chat.ChatEmoticonData;
import tv.twitch.chat.ChatEvent;
import tv.twitch.chat.ChatRawMessage;
import tv.twitch.chat.ChatTokenizationOption;
import tv.twitch.chat.ChatTokenizedMessage;
import tv.twitch.chat.ChatUserInfo;
import tv.twitch.chat.IChatAPIListener;
import tv.twitch.chat.IChatChannelListener;
import tv.twitch.chat.StandardChatAPI;

public class ChatController
{
    private static final Logger LOGGER = LogManager.getLogger();
    protected ChatController.ChatListener chatListener = null;
    protected String username = "";
    protected String clientId = "";
    protected String lastError = "";
    protected Core core = null;
    protected Chat chat = null;
    protected ChatController.ChatState chatState = ChatController.ChatState.Uninitialized;
    protected AuthToken authToken = new AuthToken();
    protected HashMap<String, ChatController.ChatChannelListener> channelListeners = new HashMap<String, ChatController.ChatChannelListener>();
    protected int messageBufferLimit = 128;
    protected ChatController.EnumEmoticonMode emoticonMode = ChatController.EnumEmoticonMode.None;
    protected ChatController.EnumEmoticonMode activeEmoticonMode = ChatController.EnumEmoticonMode.None;
    protected ChatEmoticonData emoticonData = null;
    protected int messageFlushInterval = 500;
    protected int userChangeEventInterval = 2000;
    protected IChatAPIListener chatApiListener = new IChatAPIListener()
    {
        public void chatInitializationCallback(ErrorCode result)
        {
            if (ErrorCode.succeeded(result))
            {
                ChatController.this.chat.setMessageFlushInterval(ChatController.this.messageFlushInterval);
                ChatController.this.chat.setUserChangeEventInterval(ChatController.this.userChangeEventInterval);
                ChatController.this.downloadEmoticonData();
                ChatController.this.setState(ChatController.ChatState.Initialized);
            }
            else
            {
                ChatController.this.setState(ChatController.ChatState.Uninitialized);
            }

            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onChatInitialized(result);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }
        }
        public void chatShutdownCallback(ErrorCode result)
        {
            if (ErrorCode.succeeded(result))
            {
                ErrorCode errorcode = ChatController.this.core.shutdown();

                if (ErrorCode.failed(errorcode))
                {
                    String s = ErrorCode.getString(errorcode);
                    ChatController.this.logError(String.format("Error shutting down the Twitch sdk: %s", new Object[] {s}));
                }

                ChatController.this.setState(ChatController.ChatState.Uninitialized);
            }
            else
            {
                ChatController.this.setState(ChatController.ChatState.Initialized);
                ChatController.this.logError(String.format("Error shutting down Twith chat: %s", new Object[] {result}));
            }

            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onChatShutdown(result);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }
        }
        public void chatEmoticonDataDownloadCallback(ErrorCode result)
        {
            if (ErrorCode.succeeded(result))
            {
                ChatController.this.prepareEmoticonData();
            }
        }
    };

    public void setChatListener(ChatController.ChatListener listener)
    {
        this.chatListener = listener;
    }

    public void setAuthToken(AuthToken token)
    {
        this.authToken = token;
    }

    public void setClientId(String clientIdIn)
    {
        this.clientId = clientIdIn;
    }

    public void setUsername(String usernameIn)
    {
        this.username = usernameIn;
    }

    public ChatController.ChatState getState()
    {
        return this.chatState;
    }

    public boolean isChannelConnected(String channelName)
    {
        ChatController.ChatChannelListener channelListener = this.channelListeners.get(channelName);

        if (channelListener == null)
        {
            return false;
        }
        else
        {
            return channelListener.getState() == ChatController.EnumChannelState.Connected;
        }
    }

    public ChatController.EnumChannelState getChannelState(String channelName)
    {
        ChatController.ChatChannelListener channelListener = this.channelListeners.get(channelName);

        if (channelListener == null)
        {
            return ChatController.EnumChannelState.Disconnected;
        }
        else
        {
            return channelListener.getState();
        }
    }

    public ChatController()
    {
        this.core = Core.getInstance();

        if (this.core == null)
        {
            this.core = new Core(new StandardCoreAPI());
        }

        this.chat = new Chat(new StandardChatAPI());
    }

    public boolean initialize()
    {
        if (this.chatState != ChatController.ChatState.Uninitialized)
        {
            return false;
        }
        else
        {
            this.setState(ChatController.ChatState.Initializing);
            ErrorCode errorcode = this.core.initialize(this.clientId, (String)null);

            if (ErrorCode.failed(errorcode))
            {
                this.setState(ChatController.ChatState.Uninitialized);
                String errorMessage = ErrorCode.getString(errorcode);
                this.logError(String.format("Error initializing Twitch sdk: %s", new Object[] {errorMessage}));
                return false;
            }
            else
            {
                this.activeEmoticonMode = this.emoticonMode;
                HashSet<ChatTokenizationOption> hashset = new HashSet<ChatTokenizationOption>();

                switch (this.emoticonMode)
                {
                    case None:
                        hashset.add(ChatTokenizationOption.TTV_CHAT_TOKENIZATION_OPTION_NONE);
                        break;

                    case Url:
                        hashset.add(ChatTokenizationOption.TTV_CHAT_TOKENIZATION_OPTION_EMOTICON_URLS);
                        break;

                    case TextureAtlas:
                        hashset.add(ChatTokenizationOption.TTV_CHAT_TOKENIZATION_OPTION_EMOTICON_TEXTURES);
                }

                errorcode = this.chat.initialize(hashset, this.chatApiListener);

                if (ErrorCode.failed(errorcode))
                {
                    this.core.shutdown();
                    this.setState(ChatController.ChatState.Uninitialized);
                    String s = ErrorCode.getString(errorcode);
                    this.logError(String.format("Error initializing Twitch chat: %s", new Object[] {s}));
                    return false;
                }
                else
                {
                    this.setState(ChatController.ChatState.Initialized);
                    return true;
                }
            }
        }
    }

    public boolean connect(String channelName)
    {
        return this.connectChannel(channelName, false);
    }

    protected boolean connectChannel(String channelName, boolean anonymous)
    {
        if (this.chatState != ChatController.ChatState.Initialized)
        {
            return false;
        }
        else if (this.channelListeners.containsKey(channelName))
        {
            this.logError("Already in channel: " + channelName);
            return false;
        }
        else if (channelName != null && !channelName.equals(""))
        {
            ChatController.ChatChannelListener channelListener = new ChatController.ChatChannelListener(channelName);
            this.channelListeners.put(channelName, channelListener);
            boolean flag = channelListener.connect(anonymous);

            if (!flag)
            {
                this.channelListeners.remove(channelName);
            }

            return flag;
        }
        else
        {
            return false;
        }
    }

    public boolean disconnect(String channelName)
    {
        if (this.chatState != ChatController.ChatState.Initialized)
        {
            return false;
        }
        else
        {
            ChatController.ChatChannelListener channelListener = this.channelListeners.get(channelName);

            if (channelListener == null)
            {
                this.logError("Not in channel: " + channelName);
                return false;
            }

            return channelListener.disconnect();
        }
    }

    public boolean shutdown()
    {
        if (this.chatState != ChatController.ChatState.Initialized)
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = this.chat.shutdown();

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                this.logError(String.format("Error shutting down chat: %s", new Object[] {s}));
                return false;
            }
            else
            {
                this.clearEmoticonData();
                this.setState(ChatController.ChatState.ShuttingDown);
                return true;
            }
        }
    }

    public void shutdownAndWait()
    {
        if (this.getState() != ChatController.ChatState.Uninitialized)
        {
            this.shutdown();

            if (this.getState() == ChatController.ChatState.ShuttingDown)
            {
                while (this.getState() != ChatController.ChatState.Uninitialized)
                {
                    try
                    {
                        Thread.sleep(200L);
                        this.flushEvents();
                    }
                    catch (InterruptedException caughtInterruptedException)
                    {
                        ;
                    }
                }
            }
        }
    }

    public void flushEvents()
    {
        if (this.chatState != ChatController.ChatState.Uninitialized)
        {
            ErrorCode errorcode = this.chat.flushEvents();

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                this.logError(String.format("Error flushing chat events: %s", new Object[] {s}));
            }
        }
    }

    public boolean sendMessage(String channelName, String message)
    {
        if (this.chatState != ChatController.ChatState.Initialized)
        {
            return false;
        }
        else
        {
            ChatController.ChatChannelListener channelListener = this.channelListeners.get(channelName);

            if (channelListener == null)
            {
                this.logError("Not in channel: " + channelName);
                return false;
            }

            return channelListener.sendMessage(message);
        }
    }

    protected void setState(ChatController.ChatState state)
    {
        if (state != this.chatState)
        {
            this.chatState = state;

            try
            {
                if (this.chatListener != null)
                {
                    this.chatListener.onChatStateChanged(state);
                }
            }
            catch (Exception exception)
            {
                this.logError(exception.toString());
            }
        }
    }

    protected void downloadEmoticonData()
    {
        if (this.activeEmoticonMode != ChatController.EnumEmoticonMode.None)
        {
            if (this.emoticonData == null)
            {
                ErrorCode errorcode = this.chat.downloadEmoticonData();

                if (ErrorCode.failed(errorcode))
                {
                    String s = ErrorCode.getString(errorcode);
                    this.logError(String.format("Error trying to download emoticon data: %s", new Object[] {s}));
                }
            }
        }
    }

    protected void prepareEmoticonData()
    {
        if (this.emoticonData == null)
        {
            this.emoticonData = new ChatEmoticonData();
            ErrorCode errorcode = this.chat.getEmoticonData(this.emoticonData);

            if (ErrorCode.succeeded(errorcode))
            {
                try
                {
                    if (this.chatListener != null)
                    {
                        this.chatListener.onEmoticonDataReady();
                    }
                }
                catch (Exception exception)
                {
                    this.logError(exception.toString());
                }
            }
            else
            {
                this.logError("Error preparing emoticon data: " + ErrorCode.getString(errorcode));
            }
        }
    }

    protected void clearEmoticonData()
    {
        if (this.emoticonData != null)
        {
            ErrorCode errorcode = this.chat.clearEmoticonData();

            if (ErrorCode.succeeded(errorcode))
            {
                this.emoticonData = null;

                try
                {
                    if (this.chatListener != null)
                    {
                        this.chatListener.onEmoticonDataCleared();
                    }
                }
                catch (Exception exception)
                {
                    this.logError(exception.toString());
                }
            }
            else
            {
                this.logError("Error clearing emoticon data: " + ErrorCode.getString(errorcode));
            }
        }
    }

    protected void logError(String message)
    {
        LOGGER.error(TwitchStream.STREAM_MARKER, "[Chat controller] {}", new Object[] {message});
    }

    public class ChatChannelListener implements IChatChannelListener
    {
        protected String channelName = null;
        protected boolean anonymous = false;
        protected ChatController.EnumChannelState channelState = ChatController.EnumChannelState.Created;
        protected List<ChatUserInfo> userInfoList = Lists.<ChatUserInfo>newArrayList();
        protected LinkedList<ChatRawMessage> rawMessageBuffer = new LinkedList();
        protected LinkedList<ChatTokenizedMessage> tokenizedMessageBuffer = new LinkedList();
        protected ChatBadgeData badgeData = null;

        public ChatChannelListener(String channelNameIn)
        {
            this.channelName = channelNameIn;
        }

        public ChatController.EnumChannelState getState()
        {
            return this.channelState;
        }

        public boolean connect(boolean anonymous)
        {
            this.anonymous = anonymous;
            ErrorCode errorcode = ErrorCode.TTV_EC_SUCCESS;

            if (anonymous)
            {
                errorcode = ChatController.this.chat.connectAnonymous(this.channelName, this);
            }
            else
            {
                errorcode = ChatController.this.chat.connect(this.channelName, ChatController.this.username, ChatController.this.authToken.data, this);
            }

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                ChatController.this.logError(String.format("Error connecting: %s", new Object[] {s}));
                this.notifyChannelLeft(this.channelName);
                return false;
            }
            else
            {
                this.setState(ChatController.EnumChannelState.Connecting);
                this.downloadBadgeData();
                return true;
            }
        }

        public boolean disconnect()
        {
            switch (this.channelState)
            {
                case Connected:
                case Connecting:
                    ErrorCode errorcode = ChatController.this.chat.disconnect(this.channelName);

                    if (ErrorCode.failed(errorcode))
                    {
                        String s = ErrorCode.getString(errorcode);
                        ChatController.this.logError(String.format("Error disconnecting: %s", new Object[] {s}));
                        return false;
                    }

                    this.setState(ChatController.EnumChannelState.Disconnecting);
                    return true;

                case Created:
                case Disconnected:
                case Disconnecting:
                default:
                    return false;
            }
        }

        protected void setState(ChatController.EnumChannelState state)
        {
            if (state != this.channelState)
            {
                this.channelState = state;
            }
        }

        public void clearMessagesForUser(String username)
        {
            if (ChatController.this.activeEmoticonMode == ChatController.EnumEmoticonMode.None)
            {
                this.rawMessageBuffer.clear();
                this.tokenizedMessageBuffer.clear();
            }
            else
            {
                if (this.rawMessageBuffer.size() > 0)
                {
                    ListIterator<ChatRawMessage> rawMessageIterator = this.rawMessageBuffer.listIterator();

                    while (rawMessageIterator.hasNext())
                    {
                        ChatRawMessage rawMessage = (ChatRawMessage)rawMessageIterator.next();

                        if (rawMessage.userName.equals(username))
                        {
                            rawMessageIterator.remove();
                        }
                    }
                }

                if (this.tokenizedMessageBuffer.size() > 0)
                {
                    ListIterator<ChatTokenizedMessage> tokenizedMessageIterator = this.tokenizedMessageBuffer.listIterator();

                    while (tokenizedMessageIterator.hasNext())
                    {
                        ChatTokenizedMessage tokenizedMessage = (ChatTokenizedMessage)tokenizedMessageIterator.next();

                        if (tokenizedMessage.displayName.equals(username))
                        {
                            tokenizedMessageIterator.remove();
                        }
                    }
                }
            }

            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onUserMessagesCleared(this.channelName, username);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }
        }

        public boolean sendMessage(String message)
        {
            if (this.channelState != ChatController.EnumChannelState.Connected)
            {
                return false;
            }
            else
            {
                ErrorCode errorcode = ChatController.this.chat.sendMessage(this.channelName, message);

                if (ErrorCode.failed(errorcode))
                {
                    String s = ErrorCode.getString(errorcode);
                    ChatController.this.logError(String.format("Error sending chat message: %s", new Object[] {s}));
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }

        protected void downloadBadgeData()
        {
            if (ChatController.this.activeEmoticonMode != ChatController.EnumEmoticonMode.None)
            {
                if (this.badgeData == null)
                {
                    ErrorCode errorcode = ChatController.this.chat.downloadBadgeData(this.channelName);

                    if (ErrorCode.failed(errorcode))
                    {
                        String s = ErrorCode.getString(errorcode);
                        ChatController.this.logError(String.format("Error trying to download badge data: %s", new Object[] {s}));
                    }
                }
            }
        }

        protected void prepareBadgeData()
        {
            if (this.badgeData == null)
            {
                this.badgeData = new ChatBadgeData();
                ErrorCode errorcode = ChatController.this.chat.getBadgeData(this.channelName, this.badgeData);

                if (ErrorCode.succeeded(errorcode))
                {
                    try
                    {
                        if (ChatController.this.chatListener != null)
                        {
                            ChatController.this.chatListener.onBadgeDataReady(this.channelName);
                        }
                    }
                    catch (Exception exception)
                    {
                        ChatController.this.logError(exception.toString());
                    }
                }
                else
                {
                    ChatController.this.logError("Error preparing badge data: " + ErrorCode.getString(errorcode));
                }
            }
        }

        protected void clearBadgeData()
        {
            if (this.badgeData != null)
            {
                ErrorCode errorcode = ChatController.this.chat.clearBadgeData(this.channelName);

                if (ErrorCode.succeeded(errorcode))
                {
                    this.badgeData = null;

                    try
                    {
                        if (ChatController.this.chatListener != null)
                        {
                            ChatController.this.chatListener.onBadgeDataCleared(this.channelName);
                        }
                    }
                    catch (Exception exception)
                    {
                        ChatController.this.logError(exception.toString());
                    }
                }
                else
                {
                    ChatController.this.logError("Error releasing badge data: " + ErrorCode.getString(errorcode));
                }
            }
        }

        protected void notifyChannelJoined(String channelName)
        {
            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onChannelJoined(channelName);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }
        }

        protected void notifyChannelLeft(String channelName)
        {
            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onChannelLeft(channelName);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }
        }

        private void markDisconnected()
        {
            if (this.channelState != ChatController.EnumChannelState.Disconnected)
            {
                this.setState(ChatController.EnumChannelState.Disconnected);
                this.notifyChannelLeft(this.channelName);
                this.clearBadgeData();
            }
        }

        public void chatStatusCallback(String channelName, ErrorCode result)
        {
            if (!ErrorCode.succeeded(result))
            {
                ChatController.this.channelListeners.remove(channelName);
                this.markDisconnected();
            }
        }

        public void chatChannelMembershipCallback(String channelName, ChatEvent event, ChatChannelInfo channelInfo)
        {
            switch (event)
            {
                case TTV_CHAT_JOINED_CHANNEL:
                    this.setState(ChatController.EnumChannelState.Connected);
                    this.notifyChannelJoined(channelName);
                    break;

                case TTV_CHAT_LEFT_CHANNEL:
                    this.markDisconnected();
            }
        }

        public void chatChannelUserChangeCallback(String channelName, ChatUserInfo[] joinedUsers, ChatUserInfo[] leftUsers, ChatUserInfo[] updatedUsers)
        {
            for (int i = 0; i < leftUsers.length; ++i)
            {
                int j = this.userInfoList.indexOf(leftUsers[i]);

                if (j >= 0)
                {
                    this.userInfoList.remove(j);
                }
            }

            for (int k = 0; k < updatedUsers.length; ++k)
            {
                int existingUserIndex = this.userInfoList.indexOf(updatedUsers[k]);

                if (existingUserIndex >= 0)
                {
                    this.userInfoList.remove(existingUserIndex);
                }

                this.userInfoList.add(updatedUsers[k]);
            }

            for (int l = 0; l < joinedUsers.length; ++l)
            {
                this.userInfoList.add(joinedUsers[l]);
            }

            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onUsersChanged(this.channelName, joinedUsers, leftUsers, updatedUsers);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }
        }

        public void chatChannelRawMessageCallback(String channelName, ChatRawMessage[] messages)
        {
            for (int i = 0; i < messages.length; ++i)
            {
                this.rawMessageBuffer.addLast(messages[i]);
            }

            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onRawMessages(this.channelName, messages);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }

            while (this.rawMessageBuffer.size() > ChatController.this.messageBufferLimit)
            {
                this.rawMessageBuffer.removeFirst();
            }
        }

        public void chatChannelTokenizedMessageCallback(String channelName, ChatTokenizedMessage[] messages)
        {
            for (int i = 0; i < messages.length; ++i)
            {
                this.tokenizedMessageBuffer.addLast(messages[i]);
            }

            try
            {
                if (ChatController.this.chatListener != null)
                {
                    ChatController.this.chatListener.onTokenizedMessages(this.channelName, messages);
                }
            }
            catch (Exception exception)
            {
                ChatController.this.logError(exception.toString());
            }

            while (this.tokenizedMessageBuffer.size() > ChatController.this.messageBufferLimit)
            {
                this.tokenizedMessageBuffer.removeFirst();
            }
        }

        public void chatClearCallback(String channelName, String username)
        {
            this.clearMessagesForUser(username);
        }

        public void chatBadgeDataDownloadCallback(String channelName, ErrorCode result)
        {
            if (ErrorCode.succeeded(result))
            {
                this.prepareBadgeData();
            }
        }
    }

    public interface ChatListener
    {
        void onChatInitialized(ErrorCode result);

        void onChatShutdown(ErrorCode result);

        void onEmoticonDataReady();

        void onEmoticonDataCleared();

        void onChatStateChanged(ChatController.ChatState state);

        void onTokenizedMessages(String channelName, ChatTokenizedMessage[] messages);

        void onRawMessages(String channelName, ChatRawMessage[] messages);

        void onUsersChanged(String channelName, ChatUserInfo[] joinedUsers, ChatUserInfo[] leftUsers, ChatUserInfo[] updatedUsers);

        void onChannelJoined(String channelName);

        void onChannelLeft(String channelName);

        void onUserMessagesCleared(String channelName, String username);

        void onBadgeDataReady(String channelName);

        void onBadgeDataCleared(String channelName);
    }

    public static enum ChatState
    {
        Uninitialized,
        Initializing,
        Initialized,
        ShuttingDown;
    }

    public static enum EnumChannelState
    {
        Created,
        Connecting,
        Connected,
        Disconnecting,
        Disconnected;
    }

    public static enum EnumEmoticonMode
    {
        None,
        Url,
        TextureAtlas;
    }
}
