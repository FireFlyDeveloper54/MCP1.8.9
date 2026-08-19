package net.minecraft.client.stream;

import tv.twitch.ErrorCode;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.chat.ChatUserInfo;

public class NullStream implements IStream
{
    private final Throwable failureCause;

    public NullStream(Throwable failureCauseIn)
    {
        this.failureCause = failureCauseIn;
    }

    public void shutdownStream()
    {
    }

    public void updateStream()
    {
    }

    public void submitStreamFrame()
    {
    }

    public boolean isLoggedIn()
    {
        return false;
    }

    public boolean isReadyToBroadcast()
    {
        return false;
    }

    public boolean isBroadcasting()
    {
        return false;
    }

    public void sendMetadataAction(Metadata metadata, long offsetMillis)
    {
    }

    public void sendMetadataSequence(Metadata metadata, long startOffsetMillis, long endOffsetMillis)
    {
    }

    public boolean isPaused()
    {
        return false;
    }

    public void requestCommercial()
    {
    }

    public void pause()
    {
    }

    public void unpause()
    {
    }

    public void updateStreamVolume()
    {
    }

    public void startBroadcasting()
    {
    }

    public void stopBroadcasting()
    {
    }

    public IngestServer[] getIngestServers()
    {
        return new IngestServer[0];
    }

    public void startIngestTest()
    {
    }

    public IngestServerTester getIngestServerTester()
    {
        return null;
    }

    public boolean isIngestTesting()
    {
        return false;
    }

    public int getViewerCount()
    {
        return 0;
    }

    public boolean isChannelOwner()
    {
        return false;
    }

    public String getChannelName()
    {
        return null;
    }

    public ChatUserInfo getChatUserInfo(String userName)
    {
        return null;
    }

    public void sendChatMessage(String message)
    {
    }

    public boolean isStreamingSupported()
    {
        return false;
    }

    public ErrorCode getStreamErrorCode()
    {
        return null;
    }

    public boolean isAuthenticated()
    {
        return false;
    }

    public void muteMicrophone(boolean muted)
    {
    }

    public boolean isMicrophoneMuted()
    {
        return false;
    }

    public IStream.AuthFailureReason getAuthFailureReason()
    {
        return IStream.AuthFailureReason.ERROR;
    }

    public Throwable getFailureCause()
    {
        return this.failureCause;
    }
}
