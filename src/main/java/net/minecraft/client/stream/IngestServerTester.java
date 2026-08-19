package net.minecraft.client.stream;

import com.google.common.collect.Lists;
import java.util.List;
import tv.twitch.AuthToken;
import tv.twitch.ErrorCode;
import tv.twitch.broadcast.ArchivingState;
import tv.twitch.broadcast.AudioParams;
import tv.twitch.broadcast.ChannelInfo;
import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.GameInfoList;
import tv.twitch.broadcast.IStatCallbacks;
import tv.twitch.broadcast.IStreamCallbacks;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.PixelFormat;
import tv.twitch.broadcast.RTMPState;
import tv.twitch.broadcast.StartFlags;
import tv.twitch.broadcast.StatType;
import tv.twitch.broadcast.Stream;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.UserInfo;
import tv.twitch.broadcast.VideoParams;

public class IngestServerTester
{
    protected IngestServerTester.IngestTestListener listener = null;
    protected Stream stream = null;
    protected IngestList ingestList = null;
    protected IngestServerTester.IngestTestState state = IngestServerTester.IngestTestState.Uninitalized;
    protected long serverTestDurationMs = 8000L;
    protected long unusedServerWaitTimeMs = 2000L;
    protected long bytesSent = 0L;
    protected RTMPState rtmpState = RTMPState.Invalid;
    protected VideoParams videoParams = null;
    protected AudioParams audioParameters = null;
    protected long stateStartTimeMs = 0L;
    protected List<FrameBuffer> frameBuffers = null;
    protected boolean currentServerSucceeded = false;
    protected IStreamCallbacks previousStreamCallbacks = null;
    protected IStatCallbacks previousStatCallbacks = null;
    protected IngestServer currentServer = null;
    protected boolean cancelRequested = false;
    protected boolean stopAfterStart = false;
    protected int currentServerIndex = -1;
    protected int nextFrameBufferIndex = 0;
    protected long lastBytesSent = 0L;
    protected float totalProgress = 0.0F;
    protected float serverProgress = 0.0F;
    protected boolean streamStarted = false;
    protected boolean startPending = false;
    protected boolean stopPending = false;
    protected IStreamCallbacks ingestStreamCallbacks = new IStreamCallbacks()
    {
        public void requestAuthTokenCallback(ErrorCode authError, AuthToken authToken)
        {
        }
        public void loginCallback(ErrorCode loginError, ChannelInfo channelInfo)
        {
        }
        public void getIngestServersCallback(ErrorCode ingestError, IngestList ingestListIn)
        {
        }
        public void getUserInfoCallback(ErrorCode userInfoError, UserInfo userInfo)
        {
        }
        public void getStreamInfoCallback(ErrorCode streamInfoError, StreamInfo streamInfo)
        {
        }
        public void getArchivingStateCallback(ErrorCode archivingError, ArchivingState archivingState)
        {
        }
        public void runCommercialCallback(ErrorCode commercialError)
        {
        }
        public void setStreamInfoCallback(ErrorCode setStreamInfoError)
        {
        }
        public void getGameNameListCallback(ErrorCode gameListError, GameInfoList gameNameList)
        {
        }
        public void bufferUnlockCallback(long bufferAddress)
        {
        }
        public void startCallback(ErrorCode startError)
        {
            IngestServerTester.this.startPending = false;

            if (ErrorCode.succeeded(startError))
            {
                IngestServerTester.this.streamStarted = true;
                IngestServerTester.this.stateStartTimeMs = System.currentTimeMillis();
                IngestServerTester.this.setState(IngestServerTester.IngestTestState.ConnectingToServer);
            }
            else
            {
                IngestServerTester.this.currentServerSucceeded = false;
                IngestServerTester.this.setState(IngestServerTester.IngestTestState.DoneTestingServer);
            }
        }
        public void stopCallback(ErrorCode stopError)
        {
            if (ErrorCode.failed(stopError))
            {
                System.out.println("IngestTester.stopCallback failed to stop - " + IngestServerTester.this.currentServer.serverName + ": " + stopError.toString());
            }

            IngestServerTester.this.stopPending = false;
            IngestServerTester.this.streamStarted = false;
            IngestServerTester.this.setState(IngestServerTester.IngestTestState.DoneTestingServer);
            IngestServerTester.this.currentServer = null;

            if (IngestServerTester.this.cancelRequested)
            {
                IngestServerTester.this.setState(IngestServerTester.IngestTestState.Cancelling);
            }
        }
        public void sendActionMetaDataCallback(ErrorCode actionMetadataError)
        {
        }
        public void sendStartSpanMetaDataCallback(ErrorCode startSpanMetadataError)
        {
        }
        public void sendEndSpanMetaDataCallback(ErrorCode endSpanMetadataError)
        {
        }
    };
    protected IStatCallbacks ingestStatCallbacks = new IStatCallbacks()
    {
        public void statCallback(StatType statType, long statValue)
        {
            switch (statType)
            {
                case TTV_ST_RTMPSTATE:
                    IngestServerTester.this.rtmpState = RTMPState.lookupValue((int)statValue);
                    break;

                case TTV_ST_RTMPDATASENT:
                    IngestServerTester.this.bytesSent = statValue;
            }
        }
    };

    public void setListener(IngestServerTester.IngestTestListener listenerIn)
    {
        this.listener = listenerIn;
    }

    public IngestServer getCurrentServer()
    {
        return this.currentServer;
    }

    public int getCurrentServerIndex()
    {
        return this.currentServerIndex;
    }

    public boolean isFinished()
    {
        return this.state == IngestServerTester.IngestTestState.Finished || this.state == IngestServerTester.IngestTestState.Cancelled || this.state == IngestServerTester.IngestTestState.Failed;
    }

    public float getServerProgress()
    {
        return this.serverProgress;
    }

    public IngestServerTester(Stream streamIn, IngestList ingestListIn)
    {
        this.stream = streamIn;
        this.ingestList = ingestListIn;
    }

    public void startTesting()
    {
        if (this.state == IngestServerTester.IngestTestState.Uninitalized)
        {
            this.currentServerIndex = 0;
            this.cancelRequested = false;
            this.stopAfterStart = false;
            this.streamStarted = false;
            this.startPending = false;
            this.stopPending = false;
            this.previousStatCallbacks = this.stream.getStatCallbacks();
            this.stream.setStatCallbacks(this.ingestStatCallbacks);
            this.previousStreamCallbacks = this.stream.getStreamCallbacks();
            this.stream.setStreamCallbacks(this.ingestStreamCallbacks);
            this.videoParams = new VideoParams();
            this.videoParams.targetFps = 60;
            this.videoParams.maxKbps = 3500;
            this.videoParams.outputWidth = 1280;
            this.videoParams.outputHeight = 720;
            this.videoParams.pixelFormat = PixelFormat.TTV_PF_BGRA;
            this.videoParams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
            this.videoParams.disableAdaptiveBitrate = true;
            this.videoParams.verticalFlip = false;
            this.stream.getDefaultParams(this.videoParams);
            this.audioParameters = new AudioParams();
            this.audioParameters.audioEnabled = false;
            this.audioParameters.enableMicCapture = false;
            this.audioParameters.enablePlaybackCapture = false;
            this.audioParameters.enablePassthroughAudio = false;
            this.frameBuffers = Lists.<FrameBuffer>newArrayList();
            int i = 3;

            for (int j = 0; j < i; ++j)
            {
                FrameBuffer framebuffer = this.stream.allocateFrameBuffer(this.videoParams.outputWidth * this.videoParams.outputHeight * 4);

                if (!framebuffer.getIsValid())
                {
                    this.cleanup();
                    this.setState(IngestServerTester.IngestTestState.Failed);
                    return;
                }

                this.frameBuffers.add(framebuffer);
                this.stream.randomizeFrameBuffer(framebuffer);
            }

            this.setState(IngestServerTester.IngestTestState.Starting);
            this.stateStartTimeMs = System.currentTimeMillis();
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void poll()
    {
        if (!this.isFinished() && this.state != IngestServerTester.IngestTestState.Uninitalized)
        {
            if (!this.startPending && !this.stopPending)
            {
                switch (this.state)
                {
                    case Starting:
                    case DoneTestingServer:
                        if (this.currentServer != null)
                        {
                            if (this.stopAfterStart || !this.currentServerSucceeded)
                            {
                                this.currentServer.bitrateKbps = 0.0F;
                            }

                            this.stopTestingServer(this.currentServer);
                        }
                        else
                        {
                            this.stateStartTimeMs = 0L;
                            this.stopAfterStart = false;
                            this.currentServerSucceeded = true;

                            if (this.state != IngestServerTester.IngestTestState.Starting)
                            {
                                ++this.currentServerIndex;
                            }

                            if (this.currentServerIndex < this.ingestList.getServers().length)
                            {
                                this.currentServer = this.ingestList.getServers()[this.currentServerIndex];
                                this.startTestingServer(this.currentServer);
                            }
                            else
                            {
                                this.setState(IngestServerTester.IngestTestState.Finished);
                            }
                        }

                        break;

                    case ConnectingToServer:
                    case TestingServer:
                        this.submitTestFrame(this.currentServer);
                        break;

                    case Cancelling:
                        this.setState(IngestServerTester.IngestTestState.Cancelled);
                }

                this.updateProgress();

                if (this.state == IngestServerTester.IngestTestState.Cancelled || this.state == IngestServerTester.IngestTestState.Finished)
                {
                    this.cleanup();
                }
            }
        }
    }

    public void cancel()
    {
        if (!this.isFinished() && !this.cancelRequested)
        {
            this.cancelRequested = true;

            if (this.currentServer != null)
            {
                this.currentServer.bitrateKbps = 0.0F;
            }
        }
    }

    protected boolean startTestingServer(IngestServer ingestServer)
    {
        this.currentServerSucceeded = true;
        this.bytesSent = 0L;
        this.rtmpState = RTMPState.Idle;
        this.currentServer = ingestServer;
        this.startPending = true;
        this.setState(IngestServerTester.IngestTestState.ConnectingToServer);
        ErrorCode errorcode = this.stream.start(this.videoParams, this.audioParameters, ingestServer, StartFlags.TTV_Start_BandwidthTest, true);

        if (ErrorCode.failed(errorcode))
        {
            this.startPending = false;
            this.currentServerSucceeded = false;
            this.setState(IngestServerTester.IngestTestState.DoneTestingServer);
            return false;
        }
        else
        {
            this.lastBytesSent = this.bytesSent;
            ingestServer.bitrateKbps = 0.0F;
            this.nextFrameBufferIndex = 0;
            return true;
        }
    }

    protected void stopTestingServer(IngestServer ingestServer)
    {
        if (this.startPending)
        {
            this.stopAfterStart = true;
        }
        else if (this.streamStarted)
        {
            this.stopPending = true;
            ErrorCode errorcode = this.stream.stop(true);

            if (ErrorCode.failed(errorcode))
            {
                this.ingestStreamCallbacks.stopCallback(ErrorCode.TTV_EC_SUCCESS);
                System.out.println("Stop failed: " + errorcode.toString());
            }

            this.stream.pollStats();
        }
        else
        {
            this.ingestStreamCallbacks.stopCallback(ErrorCode.TTV_EC_SUCCESS);
        }
    }

    protected long getCurrentStateElapsedMs()
    {
        return System.currentTimeMillis() - this.stateStartTimeMs;
    }

    protected void updateProgress()
    {
        float f = (float)this.getCurrentStateElapsedMs();

        switch (this.state)
        {
            case Starting:
            case ConnectingToServer:
            case Uninitalized:
            case Finished:
            case Cancelled:
            case Failed:
                this.serverProgress = 0.0F;
                break;

            case DoneTestingServer:
                this.serverProgress = 1.0F;
                break;

            case TestingServer:
            case Cancelling:
            default:
                this.serverProgress = f / (float)this.serverTestDurationMs;
        }

        switch (this.state)
        {
            case Finished:
            case Cancelled:
            case Failed:
                this.totalProgress = 1.0F;
                break;

            default:
                this.totalProgress = (float)this.currentServerIndex / (float)this.ingestList.getServers().length;
                this.totalProgress += this.serverProgress / (float)this.ingestList.getServers().length;
        }
    }

    protected boolean submitTestFrame(IngestServer ingestServer)
    {
        if (!this.stopAfterStart && !this.cancelRequested && this.getCurrentStateElapsedMs() < this.serverTestDurationMs)
        {
            if (!this.startPending && !this.stopPending)
            {
                ErrorCode errorcode = this.stream.submitVideoFrame(this.frameBuffers.get(this.nextFrameBufferIndex));

                if (ErrorCode.failed(errorcode))
                {
                    this.currentServerSucceeded = false;
                    this.setState(IngestServerTester.IngestTestState.DoneTestingServer);
                    return false;
                }
                else
                {
                    this.nextFrameBufferIndex = (this.nextFrameBufferIndex + 1) % this.frameBuffers.size();
                    this.stream.pollStats();

                    if (this.rtmpState == RTMPState.SendVideo)
                    {
                        this.setState(IngestServerTester.IngestTestState.TestingServer);
                        long i = this.getCurrentStateElapsedMs();

                        if (i > 0L && this.bytesSent > this.lastBytesSent)
                        {
                            ingestServer.bitrateKbps = (float)(this.bytesSent * 8L) / (float)this.getCurrentStateElapsedMs();
                            this.lastBytesSent = this.bytesSent;
                        }
                    }

                    return true;
                }
            }
            else
            {
                return true;
            }
        }
        else
        {
            this.setState(IngestServerTester.IngestTestState.DoneTestingServer);
            return true;
        }
    }

    protected void cleanup()
    {
        this.currentServer = null;

        if (this.frameBuffers != null)
        {
            for (int i = 0; i < this.frameBuffers.size(); ++i)
            {
                this.frameBuffers.get(i).free();
            }

            this.frameBuffers = null;
        }

        if (this.stream.getStatCallbacks() == this.ingestStatCallbacks)
        {
            this.stream.setStatCallbacks(this.previousStatCallbacks);
            this.previousStatCallbacks = null;
        }

        if (this.stream.getStreamCallbacks() == this.ingestStreamCallbacks)
        {
            this.stream.setStreamCallbacks(this.previousStreamCallbacks);
            this.previousStreamCallbacks = null;
        }
    }

    protected void setState(IngestServerTester.IngestTestState newState)
    {
        if (newState != this.state)
        {
            this.state = newState;

            if (this.listener != null)
            {
                this.listener.onIngestTestStateChanged(this, newState);
            }
        }
    }

    public interface IngestTestListener
    {
        void onIngestTestStateChanged(IngestServerTester tester, IngestServerTester.IngestTestState stateIn);
    }

    public static enum IngestTestState
    {
        Uninitalized,
        Starting,
        ConnectingToServer,
        TestingServer,
        DoneTestingServer,
        Finished,
        Cancelling,
        Cancelled,
        Failed;
    }
}
