package net.minecraft.client.stream;

import tv.twitch.ErrorCode;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.chat.ChatUserInfo;

public interface IStream
{
    void shutdownStream();

    void updateStream();

    void submitStreamFrame();

    boolean isLoggedIn();

    boolean isReadyToBroadcast();

    boolean isBroadcasting();

    void sendMetadataAction(Metadata metadata, long offsetMillis);

    void sendMetadataSequence(Metadata metadata, long startOffsetMillis, long endOffsetMillis);

    boolean isPaused();

    void requestCommercial();

    void pause();

    void unpause();

    void updateStreamVolume();

    void startBroadcasting();

    void stopBroadcasting();

    IngestServer[] getIngestServers();

    void startIngestTest();

    IngestServerTester getIngestServerTester();

    boolean isIngestTesting();

    int getViewerCount();

    boolean isChannelOwner();

    String getChannelName();

    ChatUserInfo getChatUserInfo(String userName);

    void sendChatMessage(String message);

    boolean isStreamingSupported();

    ErrorCode getStreamErrorCode();

    boolean isAuthenticated();

    void muteMicrophone(boolean muted);

    boolean isMicrophoneMuted();

    IStream.AuthFailureReason getAuthFailureReason();

    public static enum AuthFailureReason
    {
        ERROR,
        INVALID_TOKEN;
    }
}
