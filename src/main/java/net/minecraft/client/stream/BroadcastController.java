package net.minecraft.client.stream;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ThreadSafeBoundList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tv.twitch.AuthToken;
import tv.twitch.Core;
import tv.twitch.ErrorCode;
import tv.twitch.MessageLevel;
import tv.twitch.StandardCoreAPI;
import tv.twitch.broadcast.ArchivingState;
import tv.twitch.broadcast.AudioDeviceType;
import tv.twitch.broadcast.AudioParams;
import tv.twitch.broadcast.ChannelInfo;
import tv.twitch.broadcast.DesktopStreamAPI;
import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.GameInfo;
import tv.twitch.broadcast.GameInfoList;
import tv.twitch.broadcast.IStatCallbacks;
import tv.twitch.broadcast.IStreamCallbacks;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.PixelFormat;
import tv.twitch.broadcast.StartFlags;
import tv.twitch.broadcast.StatType;
import tv.twitch.broadcast.Stream;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.StreamInfoForSetting;
import tv.twitch.broadcast.UserInfo;
import tv.twitch.broadcast.VideoParams;

public class BroadcastController
{
    private static final Logger logger = LogManager.getLogger();
    protected final int streamInfoUpdateIntervalSeconds = 30;
    protected final int frameBufferPoolSize = 3;
    private static final ThreadSafeBoundList<String> recentErrors = new ThreadSafeBoundList(String.class, 50);
    private String lastError = null;
    protected BroadcastController.BroadcastListener broadcastListener = null;
    protected String clientId = "";
    protected String unusedStreamName = "";
    protected String unusedStreamTitle = "";
    protected boolean audioEnabled = true;
    protected Core streamCore = null;
    protected Stream theStream = null;
    protected List<FrameBuffer> allocatedFrameBuffers = Lists.<FrameBuffer>newArrayList();
    protected List<FrameBuffer> freeFrameBuffers = Lists.<FrameBuffer>newArrayList();
    protected boolean initialized = false;
    protected boolean loggedIn = false;
    protected boolean shuttingDown = false;
    protected BroadcastController.BroadcastState broadcastState = BroadcastController.BroadcastState.Uninitialized;
    protected String username = null;
    protected VideoParams videoParamaters = null;
    protected AudioParams audioParamaters = null;
    protected IngestList ingestList = new IngestList(new IngestServer[0]);
    protected IngestServer ingestServ = null;
    protected AuthToken authenticationToken = new AuthToken();
    protected ChannelInfo channelInfo = new ChannelInfo();
    protected UserInfo userInfo = new UserInfo();
    protected StreamInfo streamInfo = new StreamInfo();
    protected ArchivingState archivingState = new ArchivingState();
    protected long lastStreamInfoUpdateTime = 0L;
    protected IngestServerTester ingestServTester = null;
    private ErrorCode errorCode;
    protected IStreamCallbacks streamCallback = new IStreamCallbacks()
    {
        public void requestAuthTokenCallback(ErrorCode authError, AuthToken authToken)
        {
            if (ErrorCode.succeeded(authError))
            {
                BroadcastController.this.authenticationToken = authToken;
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.Authenticated);
            }
            else
            {
                BroadcastController.this.authenticationToken.data = "";
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.Initialized);
                String s = ErrorCode.getString(authError);
                BroadcastController.this.logError(String.format("RequestAuthTokenDoneCallback got failure: %s", new Object[] {s}));
            }

            try
            {
                if (BroadcastController.this.broadcastListener != null)
                {
                    BroadcastController.this.broadcastListener.onAuthTokenRequestComplete(authError, authToken);
                }
            }
            catch (Exception exception)
            {
                BroadcastController.this.logError(exception.toString());
            }
        }
        public void loginCallback(ErrorCode loginError, ChannelInfo loginChannelInfo)
        {
            if (ErrorCode.succeeded(loginError))
            {
                BroadcastController.this.channelInfo = loginChannelInfo;
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.LoggedIn);
                BroadcastController.this.loggedIn = true;
            }
            else
            {
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.Initialized);
                BroadcastController.this.loggedIn = false;
                String s = ErrorCode.getString(loginError);
                BroadcastController.this.logError(String.format("LoginCallback got failure: %s", new Object[] {s}));
            }

            try
            {
                if (BroadcastController.this.broadcastListener != null)
                {
                    BroadcastController.this.broadcastListener.onLogin(loginError);
                }
            }
            catch (Exception exception)
            {
                BroadcastController.this.logError(exception.toString());
            }
        }
        public void getIngestServersCallback(ErrorCode ingestError, IngestList newIngestList)
        {
            if (ErrorCode.succeeded(ingestError))
            {
                BroadcastController.this.ingestList = newIngestList;
                BroadcastController.this.ingestServ = BroadcastController.this.ingestList.getDefaultServer();
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.ReceivedIngestServers);

                try
                {
                    if (BroadcastController.this.broadcastListener != null)
                    {
                        BroadcastController.this.broadcastListener.onIngestListReceived(newIngestList);
                    }
                }
                catch (Exception exception)
                {
                    BroadcastController.this.logError(exception.toString());
                }
            }
            else
            {
                String s = ErrorCode.getString(ingestError);
                BroadcastController.this.logError(String.format("IngestListCallback got failure: %s", new Object[] {s}));
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.LoggingIn);
            }
        }
        public void getUserInfoCallback(ErrorCode userInfoError, UserInfo newUserInfo)
        {
            BroadcastController.this.userInfo = newUserInfo;

            if (ErrorCode.failed(userInfoError))
            {
                String s = ErrorCode.getString(userInfoError);
                BroadcastController.this.logError(String.format("UserInfoDoneCallback got failure: %s", new Object[] {s}));
            }
        }
        public void getStreamInfoCallback(ErrorCode streamInfoError, StreamInfo newStreamInfo)
        {
            if (ErrorCode.succeeded(streamInfoError))
            {
                BroadcastController.this.streamInfo = newStreamInfo;

                try
                {
                    if (BroadcastController.this.broadcastListener != null)
                    {
                        BroadcastController.this.broadcastListener.onStreamInfoUpdated(newStreamInfo);
                    }
                }
                catch (Exception exception)
                {
                    BroadcastController.this.logError(exception.toString());
                }
            }
            else
            {
                String s = ErrorCode.getString(streamInfoError);
                BroadcastController.this.logWarning(String.format("StreamInfoDoneCallback got failure: %s", new Object[] {s}));
            }
        }
        public void getArchivingStateCallback(ErrorCode archivingError, ArchivingState newArchivingState)
        {
            BroadcastController.this.archivingState = newArchivingState;

            if (ErrorCode.failed(archivingError))
            {
                ;
            }
        }
        public void runCommercialCallback(ErrorCode commercialError)
        {
            if (ErrorCode.failed(commercialError))
            {
                String s = ErrorCode.getString(commercialError);
                BroadcastController.this.logWarning(String.format("RunCommercialCallback got failure: %s", new Object[] {s}));
            }
        }
        public void setStreamInfoCallback(ErrorCode setStreamInfoError)
        {
            if (ErrorCode.failed(setStreamInfoError))
            {
                String s = ErrorCode.getString(setStreamInfoError);
                BroadcastController.this.logWarning(String.format("SetStreamInfoCallback got failure: %s", new Object[] {s}));
            }
        }
        public void getGameNameListCallback(ErrorCode gameListError, GameInfoList gameNameList)
        {
            if (ErrorCode.failed(gameListError))
            {
                String s = ErrorCode.getString(gameListError);
                BroadcastController.this.logError(String.format("GameNameListCallback got failure: %s", new Object[] {s}));
            }

            try
            {
                if (BroadcastController.this.broadcastListener != null)
                {
                    BroadcastController.this.broadcastListener.onGameNameListReceived(gameListError, gameNameList == null ? new GameInfo[0] : gameNameList.list);
                }
            }
            catch (Exception exception)
            {
                BroadcastController.this.logError(exception.toString());
            }
        }
        public void bufferUnlockCallback(long bufferAddress)
        {
            FrameBuffer framebuffer = FrameBuffer.lookupBuffer(bufferAddress);
            BroadcastController.this.freeFrameBuffers.add(framebuffer);
        }
        public void startCallback(ErrorCode startError)
        {
            if (ErrorCode.succeeded(startError))
            {
                try
                {
                    if (BroadcastController.this.broadcastListener != null)
                    {
                        BroadcastController.this.broadcastListener.onBroadcastStarted();
                    }
                }
                catch (Exception exception1)
                {
                    BroadcastController.this.logError(exception1.toString());
                }

                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.Broadcasting);
            }
            else
            {
                BroadcastController.this.videoParamaters = null;
                BroadcastController.this.audioParamaters = null;
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.ReadyToBroadcast);

                try
                {
                    if (BroadcastController.this.broadcastListener != null)
                    {
                        BroadcastController.this.broadcastListener.onBroadcastStartFailed(startError);
                    }
                }
                catch (Exception exception)
                {
                    BroadcastController.this.logError(exception.toString());
                }

                String s = ErrorCode.getString(startError);
                BroadcastController.this.logError(String.format("startCallback got failure: %s", new Object[] {s}));
            }
        }
        public void stopCallback(ErrorCode stopError)
        {
            if (ErrorCode.succeeded(stopError))
            {
                BroadcastController.this.videoParamaters = null;
                BroadcastController.this.audioParamaters = null;
                BroadcastController.this.freeFrameBuffers();

                try
                {
                    if (BroadcastController.this.broadcastListener != null)
                    {
                        BroadcastController.this.broadcastListener.onBroadcastStopped();
                    }
                }
                catch (Exception exception)
                {
                    BroadcastController.this.logError(exception.toString());
                }

                if (BroadcastController.this.loggedIn)
                {
                    BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.ReadyToBroadcast);
                }
                else
                {
                    BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.Initialized);
                }
            }
            else
            {
                BroadcastController.this.setBroadcastState(BroadcastController.BroadcastState.ReadyToBroadcast);
                String s = ErrorCode.getString(stopError);
                BroadcastController.this.logError(String.format("stopCallback got failure: %s", new Object[] {s}));
            }
        }
        public void sendActionMetaDataCallback(ErrorCode actionMetadataError)
        {
            if (ErrorCode.failed(actionMetadataError))
            {
                String s = ErrorCode.getString(actionMetadataError);
                BroadcastController.this.logError(String.format("sendActionMetaDataCallback got failure: %s", new Object[] {s}));
            }
        }
        public void sendStartSpanMetaDataCallback(ErrorCode startSpanMetadataError)
        {
            if (ErrorCode.failed(startSpanMetadataError))
            {
                String s = ErrorCode.getString(startSpanMetadataError);
                BroadcastController.this.logError(String.format("sendStartSpanMetaDataCallback got failure: %s", new Object[] {s}));
            }
        }
        public void sendEndSpanMetaDataCallback(ErrorCode endSpanMetadataError)
        {
            if (ErrorCode.failed(endSpanMetadataError))
            {
                String s = ErrorCode.getString(endSpanMetadataError);
                BroadcastController.this.logError(String.format("sendEndSpanMetaDataCallback got failure: %s", new Object[] {s}));
            }
        }
    };
    protected IStatCallbacks statCallbacks = new IStatCallbacks()
    {
        public void statCallback(StatType statType, long statValue)
        {
        }
    };

    public void setBroadcastListener(BroadcastController.BroadcastListener broadcastListenerIn)
    {
        this.broadcastListener = broadcastListenerIn;
    }

    public boolean isInitialized()
    {
        return this.initialized;
    }

    public void setClientId(String clientIdIn)
    {
        this.clientId = clientIdIn;
    }

    public StreamInfo getStreamInfo()
    {
        return this.streamInfo;
    }

    public ChannelInfo getChannelInfo()
    {
        return this.channelInfo;
    }

    public boolean isBroadcasting()
    {
        return this.broadcastState == BroadcastController.BroadcastState.Broadcasting || this.broadcastState == BroadcastController.BroadcastState.Paused;
    }

    public boolean isReadyToBroadcast()
    {
        return this.broadcastState == BroadcastController.BroadcastState.ReadyToBroadcast;
    }

    public boolean isIngestTesting()
    {
        return this.broadcastState == BroadcastController.BroadcastState.IngestTesting;
    }

    public boolean isBroadcastPaused()
    {
        return this.broadcastState == BroadcastController.BroadcastState.Paused;
    }

    public boolean isLoggedIn()
    {
        return this.loggedIn;
    }

    public IngestServer getIngestServer()
    {
        return this.ingestServ;
    }

    public void setIngestServer(IngestServer ingestServerSet)
    {
        this.ingestServ = ingestServerSet;
    }

    public IngestList getIngestList()
    {
        return this.ingestList;
    }

    public void setRecordingDeviceVolume(float volume)
    {
        this.theStream.setVolume(AudioDeviceType.TTV_RECORDER_DEVICE, volume);
    }

    public void setPlaybackDeviceVolume(float volume)
    {
        this.theStream.setVolume(AudioDeviceType.TTV_PLAYBACK_DEVICE, volume);
    }

    public IngestServerTester isReady()
    {
        return this.ingestServTester;
    }

    public long getStreamTime()
    {
        return this.theStream.getStreamTime();
    }

    protected boolean isAudioSupported()
    {
        return true;
    }

    public ErrorCode getErrorCode()
    {
        return this.errorCode;
    }

    public BroadcastController()
    {
        this.streamCore = Core.getInstance();

        if (Core.getInstance() == null)
        {
            this.streamCore = new Core(new StandardCoreAPI());
        }

        this.theStream = new Stream(new DesktopStreamAPI());
    }

    protected PixelFormat getPixelFormat()
    {
        return PixelFormat.TTV_PF_RGBA;
    }

    public boolean initialize()
    {
        if (this.initialized)
        {
            return false;
        }
        else
        {
            this.theStream.setStreamCallbacks(this.streamCallback);
            ErrorCode errorcode = this.streamCore.initialize(this.clientId, System.getProperty("java.library.path"));

            if (!this.checkError(errorcode))
            {
                this.theStream.setStreamCallbacks((IStreamCallbacks)null);
                this.errorCode = errorcode;
                return false;
            }
            else
            {
                errorcode = this.streamCore.setTraceLevel(MessageLevel.TTV_ML_ERROR);

                if (!this.checkError(errorcode))
                {
                    this.theStream.setStreamCallbacks((IStreamCallbacks)null);
                    this.streamCore.shutdown();
                    this.errorCode = errorcode;
                    return false;
                }
                else if (ErrorCode.succeeded(errorcode))
                {
                    this.initialized = true;
                    this.setBroadcastState(BroadcastController.BroadcastState.Initialized);
                    return true;
                }
                else
                {
                    this.errorCode = errorcode;
                    this.streamCore.shutdown();
                    return false;
                }
            }
        }
    }

    public boolean shutdown()
    {
        if (!this.initialized)
        {
            return true;
        }
        else if (this.isIngestTesting())
        {
            return false;
        }
        else
        {
            this.shuttingDown = true;
            this.logout();
            this.theStream.setStreamCallbacks((IStreamCallbacks)null);
            this.theStream.setStatCallbacks((IStatCallbacks)null);
            ErrorCode errorcode = this.streamCore.shutdown();
            this.checkError(errorcode);
            this.initialized = false;
            this.shuttingDown = false;
            this.setBroadcastState(BroadcastController.BroadcastState.Uninitialized);
            return true;
        }
    }

    public void statCallback()
    {
        if (this.broadcastState != BroadcastController.BroadcastState.Uninitialized)
        {
            if (this.ingestServTester != null)
            {
                this.ingestServTester.cancel();
            }

            for (; this.ingestServTester != null; this.pollTasks())
            {
                try
                {
                    Thread.sleep(200L);
                }
                catch (Exception exception)
                {
                    this.logError(exception.toString());
                }
            }

            this.shutdown();
        }
    }

    public boolean authenticate(String usernameIn, AuthToken authTokenIn)
    {
        if (this.isIngestTesting())
        {
            return false;
        }
        else
        {
            this.logout();

            if (usernameIn != null && !usernameIn.isEmpty())
            {
                if (authTokenIn != null && authTokenIn.data != null && !authTokenIn.data.isEmpty())
                {
                    this.username = usernameIn;
                    this.authenticationToken = authTokenIn;

                    if (this.isInitialized())
                    {
                        this.setBroadcastState(BroadcastController.BroadcastState.Authenticated);
                    }

                    return true;
                }
                else
                {
                    this.logError("Auth token must be valid");
                    return false;
                }
            }
            else
            {
                this.logError("Username must be valid");
                return false;
            }
        }
    }

    public boolean logout()
    {
        if (this.isIngestTesting())
        {
            return false;
        }
        else
        {
            if (this.isBroadcasting())
            {
                this.theStream.stop(false);
            }

            this.username = "";
            this.authenticationToken = new AuthToken();

            if (!this.loggedIn)
            {
                return false;
            }
            else
            {
                this.loggedIn = false;

                if (!this.shuttingDown)
                {
                    try
                    {
                        if (this.broadcastListener != null)
                        {
                            this.broadcastListener.onLoggedOut();
                        }
                    }
                    catch (Exception exception)
                    {
                        this.logError(exception.toString());
                    }
                }

                this.setBroadcastState(BroadcastController.BroadcastState.Initialized);
                return true;
            }
        }
    }

    public boolean setStreamInfo(String channelName, String gameName, String streamTitle)
    {
        if (!this.loggedIn)
        {
            return false;
        }
        else
        {
            if (channelName == null || channelName.equals(""))
            {
                channelName = this.username;
            }

            if (gameName == null)
            {
                gameName = "";
            }

            if (streamTitle == null)
            {
                streamTitle = "";
            }

            StreamInfoForSetting streaminfoforsetting = new StreamInfoForSetting();
            streaminfoforsetting.streamTitle = streamTitle;
            streaminfoforsetting.gameName = gameName;
            ErrorCode errorcode = this.theStream.setStreamInfo(this.authenticationToken, channelName, streaminfoforsetting);
            this.checkError(errorcode);
            return ErrorCode.succeeded(errorcode);
        }
    }

    public boolean requestCommercial()
    {
        if (!this.isBroadcasting())
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = this.theStream.runCommercial(this.authenticationToken);
            this.checkError(errorcode);
            return ErrorCode.succeeded(errorcode);
        }
    }

    public VideoParams getRecommendedVideoParams(int maxKbps, int targetFps, float bytesPerPixel, float aspectRatio)
    {
        int[] aint = this.theStream.getMaxResolution(maxKbps, targetFps, bytesPerPixel, aspectRatio);
        VideoParams videoparams = new VideoParams();
        videoparams.maxKbps = maxKbps;
        videoparams.encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
        videoparams.pixelFormat = this.getPixelFormat();
        videoparams.targetFps = targetFps;
        videoparams.outputWidth = aint[0];
        videoparams.outputHeight = aint[1];
        videoparams.disableAdaptiveBitrate = false;
        videoparams.verticalFlip = false;
        return videoparams;
    }

    public boolean startBroadcasting(VideoParams videoParams)
    {
        if (videoParams != null && this.isReadyToBroadcast())
        {
            this.videoParamaters = videoParams.clone();
            this.audioParamaters = new AudioParams();
            this.audioParamaters.audioEnabled = this.audioEnabled && this.isAudioSupported();
            this.audioParamaters.enableMicCapture = this.audioParamaters.audioEnabled;
            this.audioParamaters.enablePlaybackCapture = this.audioParamaters.audioEnabled;
            this.audioParamaters.enablePassthroughAudio = false;

            if (!this.allocateFrameBuffers())
            {
                this.videoParamaters = null;
                this.audioParamaters = null;
                return false;
            }
            else
            {
                ErrorCode errorcode = this.theStream.start(videoParams, this.audioParamaters, this.ingestServ, StartFlags.None, true);

                if (ErrorCode.failed(errorcode))
                {
                    this.freeFrameBuffers();
                    String s = ErrorCode.getString(errorcode);
                    this.logError(String.format("Error while starting to broadcast: %s", new Object[] {s}));
                    this.videoParamaters = null;
                    this.audioParamaters = null;
                    return false;
                }
                else
                {
                    this.setBroadcastState(BroadcastController.BroadcastState.Starting);
                    return true;
                }
            }
        }
        else
        {
            return false;
        }
    }

    public boolean stopBroadcasting()
    {
        if (!this.isBroadcasting())
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = this.theStream.stop(true);

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                this.logError(String.format("Error while stopping the broadcast: %s", new Object[] {s}));
                return false;
            }
            else
            {
                this.setBroadcastState(BroadcastController.BroadcastState.Stopping);
                return ErrorCode.succeeded(errorcode);
            }
        }
    }

    public boolean pauseBroadcasting()
    {
        if (!this.isBroadcasting())
        {
            return false;
        }
        else
        {
            ErrorCode errorcode = this.theStream.pauseVideo();

            if (ErrorCode.failed(errorcode))
            {
                this.stopBroadcasting();
                String s = ErrorCode.getString(errorcode);
                this.logError(String.format("Error pausing stream: %s\n", new Object[] {s}));
            }
            else
            {
                this.setBroadcastState(BroadcastController.BroadcastState.Paused);
            }

            return ErrorCode.succeeded(errorcode);
        }
    }

    public boolean resumeBroadcasting()
    {
        if (!this.isBroadcastPaused())
        {
            return false;
        }
        else
        {
            this.setBroadcastState(BroadcastController.BroadcastState.Broadcasting);
            return true;
        }
    }

    public boolean sendActionMetadata(String eventName, long timestamp, String description, String metadataJson)
    {
        ErrorCode errorcode = this.theStream.sendActionMetaData(this.authenticationToken, eventName, timestamp, description, metadataJson);

        if (ErrorCode.failed(errorcode))
        {
            String s = ErrorCode.getString(errorcode);
            this.logError(String.format("Error while sending meta data: %s\n", new Object[] {s}));
            return false;
        }
        else
        {
            return true;
        }
    }

    public long sendStartSpanMetadata(String eventName, long startTimestamp, String description, String metadataJson)
    {
        long i = this.theStream.sendStartSpanMetaData(this.authenticationToken, eventName, startTimestamp, description, metadataJson);

        if (i == -1L)
        {
            this.logError(String.format("Error in SendStartSpanMetaData\n", new Object[0]));
        }

        return i;
    }

    public boolean sendEndSpanMetadata(String eventName, long endTimestamp, long sequenceId, String description, String metadataJson)
    {
        if (sequenceId == -1L)
        {
            this.logError(String.format("Invalid sequence id: %d\n", new Object[] {Long.valueOf(sequenceId)}));
            return false;
        }
        else
        {
            ErrorCode errorcode = this.theStream.sendEndSpanMetaData(this.authenticationToken, eventName, endTimestamp, sequenceId, description, metadataJson);

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                this.logError(String.format("Error in SendStopSpanMetaData: %s\n", new Object[] {s}));
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    protected void setBroadcastState(BroadcastController.BroadcastState newState)
    {
        if (newState != this.broadcastState)
        {
            this.broadcastState = newState;

            try
            {
                if (this.broadcastListener != null)
                {
                    this.broadcastListener.onBroadcastStateChanged(newState);
                }
            }
            catch (Exception exception)
            {
                this.logError(exception.toString());
            }
        }
    }

    public void pollTasks()
    {
        if (this.theStream != null && this.initialized)
        {
            ErrorCode errorcode = this.theStream.pollTasks();
            this.checkError(errorcode);

            if (this.isIngestTesting())
            {
                this.ingestServTester.poll();

                if (this.ingestServTester.isFinished())
                {
                    this.ingestServTester = null;
                    this.setBroadcastState(BroadcastController.BroadcastState.ReadyToBroadcast);
                }
            }

            switch (this.broadcastState)
            {
                case Authenticated:
                    this.setBroadcastState(BroadcastController.BroadcastState.LoggingIn);
                    errorcode = this.theStream.login(this.authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        String thirdStringValue = ErrorCode.getString(errorcode);
                        this.logError(String.format("Error in TTV_Login: %s\n", new Object[] {thirdStringValue}));
                    }

                    break;

                case LoggedIn:
                    this.setBroadcastState(BroadcastController.BroadcastState.FindingIngestServer);
                    errorcode = this.theStream.getIngestServers(this.authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        this.setBroadcastState(BroadcastController.BroadcastState.LoggedIn);
                        String secondStringValue = ErrorCode.getString(errorcode);
                        this.logError(String.format("Error in TTV_GetIngestServers: %s\n", new Object[] {secondStringValue}));
                    }

                    break;

                case ReceivedIngestServers:
                    this.setBroadcastState(BroadcastController.BroadcastState.ReadyToBroadcast);
                    errorcode = this.theStream.getUserInfo(this.authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        String s = ErrorCode.getString(errorcode);
                        this.logError(String.format("Error in TTV_GetUserInfo: %s\n", new Object[] {s}));
                    }

                    this.updateStreamInfo();
                    errorcode = this.theStream.getArchivingState(this.authenticationToken);

                    if (ErrorCode.failed(errorcode))
                    {
                        String stringValue = ErrorCode.getString(errorcode);
                        this.logError(String.format("Error in TTV_GetArchivingState: %s\n", new Object[] {stringValue}));
                    }

                case Starting:
                case Stopping:
                case FindingIngestServer:
                case Authenticating:
                case Initialized:
                case Uninitialized:
                case IngestTesting:
                default:
                    break;

                case Paused:
                case Broadcasting:
                    this.updateStreamInfo();
            }
        }
    }

    protected void updateStreamInfo()
    {
        long i = System.nanoTime();
        long j = (i - this.lastStreamInfoUpdateTime) / 1000000000L;

        if (j >= 30L)
        {
            this.lastStreamInfoUpdateTime = i;
            ErrorCode errorcode = this.theStream.getStreamInfo(this.authenticationToken, this.username);

            if (ErrorCode.failed(errorcode))
            {
                String s = ErrorCode.getString(errorcode);
                this.logError(String.format("Error in TTV_GetStreamInfo: %s", new Object[] {s}));
            }
        }
    }

    public IngestServerTester startIngestTest()
    {
        if (this.isReadyToBroadcast() && this.ingestList != null)
        {
            if (this.isIngestTesting())
            {
                return null;
            }
            else
            {
                this.ingestServTester = new IngestServerTester(this.theStream, this.ingestList);
                this.ingestServTester.startTesting();
                this.setBroadcastState(BroadcastController.BroadcastState.IngestTesting);
                return this.ingestServTester;
            }
        }
        else
        {
            return null;
        }
    }

    protected boolean allocateFrameBuffers()
    {
        for (int i = 0; i < 3; ++i)
        {
            FrameBuffer framebuffer = this.theStream.allocateFrameBuffer(this.videoParamaters.outputWidth * this.videoParamaters.outputHeight * 4);

            if (!framebuffer.getIsValid())
            {
                this.logError(String.format("Error while allocating frame buffer", new Object[0]));
                return false;
            }

            this.allocatedFrameBuffers.add(framebuffer);
            this.freeFrameBuffers.add(framebuffer);
        }

        return true;
    }

    protected void freeFrameBuffers()
    {
        for (int i = 0; i < this.allocatedFrameBuffers.size(); ++i)
        {
            FrameBuffer framebuffer = this.allocatedFrameBuffers.get(i);
            framebuffer.free();
        }

        this.freeFrameBuffers.clear();
        this.allocatedFrameBuffers.clear();
    }

    public FrameBuffer getFreeFrameBuffer()
    {
        if (this.freeFrameBuffers.size() == 0)
        {
            this.logError(String.format("Out of free buffers, this should never happen", new Object[0]));
            return null;
        }
        else
        {
            FrameBuffer framebuffer = this.freeFrameBuffers.get(this.freeFrameBuffers.size() - 1);
            this.freeFrameBuffers.remove(this.freeFrameBuffers.size() - 1);
            return framebuffer;
        }
    }

    public void captureFramebuffer(FrameBuffer frameBuffer)
    {
        try
        {
            this.theStream.captureFrameBuffer_ReadPixels(frameBuffer);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Trying to submit a frame to Twitch");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Broadcast State");
            crashreportcategory.addCrashSection("Last reported errors", Arrays.toString(recentErrors.toArray()));
            crashreportcategory.addCrashSection("Buffer", frameBuffer);
            crashreportcategory.addCrashSection("Free buffer count", Integer.valueOf(this.freeFrameBuffers.size()));
            crashreportcategory.addCrashSection("Capture buffer count", Integer.valueOf(this.allocatedFrameBuffers.size()));
            throw new ReportedException(crashreport);
        }
    }

    public ErrorCode submitStreamFrame(FrameBuffer frame)
    {
        if (this.isBroadcastPaused())
        {
            this.resumeBroadcasting();
        }
        else if (!this.isBroadcasting())
        {
            return ErrorCode.TTV_EC_STREAM_NOT_STARTED;
        }

        ErrorCode errorcode = this.theStream.submitVideoFrame(frame);

        if (errorcode != ErrorCode.TTV_EC_SUCCESS)
        {
            String s = ErrorCode.getString(errorcode);

            if (ErrorCode.succeeded(errorcode))
            {
                this.logWarning(String.format("Warning in SubmitTexturePointer: %s\n", new Object[] {s}));
            }
            else
            {
                this.logError(String.format("Error in SubmitTexturePointer: %s\n", new Object[] {s}));
                this.stopBroadcasting();
            }

            if (this.broadcastListener != null)
            {
                this.broadcastListener.onFrameSubmissionIssue(errorcode);
            }
        }

        return errorcode;
    }

    protected boolean checkError(ErrorCode error)
    {
        if (ErrorCode.failed(error))
        {
            this.logError(ErrorCode.getString(error));
            return false;
        }
        else
        {
            return true;
        }
    }

    protected void logError(String error)
    {
        this.lastError = error;
        recentErrors.add("<Error> " + error);
        logger.error(TwitchStream.STREAM_MARKER, "[Broadcast controller] {}", new Object[] {error});
    }

    protected void logWarning(String warning)
    {
        recentErrors.add("<Warning> " + warning);
        logger.warn(TwitchStream.STREAM_MARKER, "[Broadcast controller] {}", new Object[] {warning});
    }

    public interface BroadcastListener
    {
        void onAuthTokenRequestComplete(ErrorCode authError, AuthToken authToken);

        void onLogin(ErrorCode loginError);

        void onGameNameListReceived(ErrorCode gameListError, GameInfo[] games);

        void onBroadcastStateChanged(BroadcastController.BroadcastState newState);

        void onLoggedOut();

        void onStreamInfoUpdated(StreamInfo streamInfoIn);

        void onIngestListReceived(IngestList ingestListIn);

        void onFrameSubmissionIssue(ErrorCode frameError);

        void onBroadcastStarted();

        void onBroadcastStopped();

        void onBroadcastStartFailed(ErrorCode startError);
    }

    public static enum BroadcastState
    {
        Uninitialized,
        Initialized,
        Authenticating,
        Authenticated,
        LoggingIn,
        LoggedIn,
        FindingIngestServer,
        ReceivedIngestServers,
        ReadyToBroadcast,
        Starting,
        Broadcasting,
        Stopping,
        Paused,
        IngestTesting;
    }
}
