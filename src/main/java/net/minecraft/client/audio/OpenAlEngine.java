package net.minecraft.client.audio;

import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.EXTThreadLocalContext;
import org.lwjgl.openal.SOFTReopenDevice;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.PointerBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenAlEngine
{
    private static final int NORMAL_SOURCES = 28;
    private static final int STREAMING_SOURCES = 4;
    private static final int STREAM_BUFFERS = 3;
    private static final int STREAM_BUFFER_BYTES = 131072;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService STREAM_INPUT_CLOSER = Executors.newCachedThreadPool(new ThreadFactory()
    {
        public Thread newThread(Runnable task)
        {
            Thread thread = new Thread(task, "Sound Stream Input Closer");
            thread.setDaemon(true);
            return thread;
        }
    });
    private static final int ALC_CONNECTED = 0x313;
    private long device;
    private long context;
    private ALCCapabilities deviceCaps;
    private ALCapabilities soundCaps;
    private int[] sources;
    private volatile boolean created;
    private volatile boolean streamWorkerRunning;
    private Thread streamWorker;
    private int nextNormalSource;
    private int nextStreamingSource;
    private float masterVolume = 1.0F;
    private String openedDeviceSpecifier;
    private volatile boolean reopenRequested;
    private volatile boolean reopenFailed;
    private final Map<String, Integer> sourceByName = new HashMap<String, Integer>();
    private final Map<Integer, String> nameBySource = new HashMap<Integer, String>();
    private final Map<String, Integer> buffers = new HashMap<String, Integer>();
    private final Set<String> preparedSources = new HashSet<String>();
    private final Map<Integer, StreamingSource> streams = new HashMap<Integer, StreamingSource>();
    private final ConcurrentLinkedQueue<StreamingSource> retiredStreams = new ConcurrentLinkedQueue<StreamingSource>();
    private final FloatBuffer listenerOrientation = org.lwjgl.BufferUtils.createFloatBuffer(6);

    public synchronized void create()
    {
        if (this.created)
        {
            return;
        }

        try
        {
            this.device = ALC10.alcOpenDevice((ByteBuffer)null);

            if (this.device == MemoryUtil.NULL)
            {
                throw new IllegalStateException("Failed to open the default OpenAL device.");
            }

            this.deviceCaps = ALC.createCapabilities(this.device);
            this.context = ALC10.alcCreateContext(this.device, (IntBuffer)null);

            if (this.context == MemoryUtil.NULL)
            {
                ALC10.alcCloseDevice(this.device);
                this.device = MemoryUtil.NULL;
                throw new IllegalStateException("Failed to create OpenAL context.");
            }

            this.makeCurrent();
            this.openedDeviceSpecifier = ALC10.alcGetString(this.device, ALC10.ALC_DEVICE_SPECIFIER);
            AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
            AL10.alListener3f(AL10.AL_VELOCITY, 0.0F, 0.0F, 0.0F);
            this.sources = new int[NORMAL_SOURCES + STREAMING_SOURCES];
            AL10.alGenSources(this.sources);
            checkError("create sound channels");
            this.created = true;
            this.setMasterVolume(this.masterVolume);
            this.releaseCurrent();
            this.streamWorkerRunning = true;
            this.streamWorker = new Thread(new Runnable()
            {
                public void run()
                {
                    OpenAlEngine.this.runStreaming();
                }
            }, "Sound Stream Decoder");
            this.streamWorker.setDaemon(true);
            this.streamWorker.start();
        }
        catch (Throwable throwable)
        {
            this.destroy();
            throw throwable;
        }
    }

    public void destroy()
    {
        Thread worker;

        synchronized (this)
        {
            if (this.device == MemoryUtil.NULL && this.context == MemoryUtil.NULL)
            {
                return;
            }

            this.streamWorkerRunning = false;
            worker = this.streamWorker;
            if (this.soundCaps != null && this.context != MemoryUtil.NULL)
            {
                this.stopAll();
                this.makeCurrent();

                for (Integer buffer : this.buffers.values())
                {
                    AL10.alDeleteBuffers(buffer.intValue());
                }

                if (this.sources != null)
                {
                    for (int source : this.sources)
                    {
                        if (source != 0)
                        {
                            AL10.alDeleteSources(source);
                        }
                    }
                }
            }

            this.buffers.clear();
            this.sources = null;
            this.created = false;
            this.releaseCurrent();
        }

        if (worker != null && worker != Thread.currentThread())
        {
            worker.interrupt();

            try
            {
                worker.join(5000L);
            }
            catch (InterruptedException exception)
            {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (this)
        {
            ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
            if (this.context != MemoryUtil.NULL)
            {
                ALC10.alcDestroyContext(this.context);
            }
            if (this.device != MemoryUtil.NULL)
            {
                ALC10.alcCloseDevice(this.device);
            }
            AL.setCurrentProcess(null);
            AL.setCurrentThread(null);
            this.context = MemoryUtil.NULL;
            this.device = MemoryUtil.NULL;
            this.openedDeviceSpecifier = null;
            this.deviceCaps = null;
            this.reopenRequested = false;
            this.reopenFailed = false;
            this.streamWorker = null;
            this.soundCaps = null;
        }
    }

    public boolean isCreated()
    {
        return this.created;
    }

    public synchronized void makeCurrent()
    {
        if (this.context != MemoryUtil.NULL)
        {
            if (this.deviceCaps.ALC_EXT_thread_local_context)
            {
                EXTThreadLocalContext.alcSetThreadContext(this.context);
            }
            else
            {
                ALC10.alcMakeContextCurrent(this.context);
            }

            if (this.soundCaps == null)
            {
                this.soundCaps = AL.createCapabilities(this.deviceCaps);
            }

            AL.setCurrentThread(this.soundCaps);
        }
    }

    private void releaseCurrent()
    {
        if (this.deviceCaps != null && this.deviceCaps.ALC_EXT_thread_local_context)
        {
            EXTThreadLocalContext.alcSetThreadContext(MemoryUtil.NULL);
        }
        else
        {
            ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
        }
    }

    public synchronized void setMasterVolume(float volume)
    {
        this.masterVolume = MathHelper.clamp_float(volume, 0.0F, 1.0F);

        if (this.created)
        {
            this.makeCurrent();

            try
            {
                AL10.alListenerf(AL10.AL_GAIN, this.masterVolume);
            }
            finally
            {
                this.releaseCurrent();
            }
        }
    }

    public synchronized float getMasterVolume()
    {
        return this.masterVolume;
    }

    public boolean newSource(String name, ResourceLocation location, boolean loop, float x, float y, float z, boolean linear, float maxDistance)
    {
        return this.newSource(name, location, loop, x, y, z, linear, maxDistance, false);
    }

    public synchronized boolean newSource(String name, ResourceLocation location, boolean loop, float x, float y, float z, boolean linear, float maxDistance, boolean streaming)
    {
        if (!this.created)
        {
            return false;
        }

        this.makeCurrent();
        int buffer = streaming ? 0 : this.getBuffer(location);
        int source = this.bindSource(name, streaming);

        if (source < 0)
        {
            return false;
        }

        try
        {
        AL10.alSourceStop(source);
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
        AL10.alSourcei(source, AL10.AL_LOOPING, loop && !streaming ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcef(source, AL10.AL_PITCH, 1.0F);
        AL10.alSourcef(source, AL10.AL_GAIN, 1.0F);
        AL10.alSource3f(source, AL10.AL_VELOCITY, 0.0F, 0.0F, 0.0F);

        if (linear)
        {
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            AL10.alSource3f(source, AL10.AL_POSITION, x, y, z);
            AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 1.0F);
            AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 0.0F);
            AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, Math.max(maxDistance, 1.0F));
        }
        else
        {
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            AL10.alSource3f(source, AL10.AL_POSITION, x, y, z);
            AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 0.0F);
        }

        if (streaming)
        {
            StreamingSource stream = new StreamingSource(name, source, location, loop);
            this.streams.put(Integer.valueOf(source), stream);
            AL10.alGenBuffers(stream.buffers);

            for (int streamBuffer : stream.buffers)
            {
                stream.freeBuffers.addLast(Integer.valueOf(streamBuffer));
            }
        }

        checkError("create sound source");
        if (!streaming)
        {
            this.preparedSources.add(name);
        }
        return true;
        }
        catch (Throwable throwable)
        {
            this.removeSource(name);
            throw throwable;
        }
    }

    public synchronized void setVolume(String name, float volume)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourcef(source.intValue(), AL10.AL_GAIN, MathHelper.clamp_float(volume, 0.0F, 1.0F));
        }
    }

    public synchronized void setPitch(String name, float pitch)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourcef(source.intValue(), AL10.AL_PITCH, MathHelper.clamp_float(pitch, 0.5F, 2.0F));
        }
    }

    public synchronized void setPosition(String name, float x, float y, float z)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSource3f(source.intValue(), AL10.AL_POSITION, x, y, z);
        }
    }

    public synchronized void play(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            StreamingSource stream = this.streams.get(source);

            if (stream != null)
            {
                stream.playRequested = true;
                stream.paused = false;

                if (!stream.pending && AL10.alGetSourcei(source.intValue(), AL10.AL_BUFFERS_QUEUED) > 0)
                {
                    AL10.alSourcePlay(source.intValue());
                }
            }
            else
            {
                int state = AL10.alGetSourcei(source.intValue(), AL10.AL_SOURCE_STATE);

                if (this.preparedSources.remove(name) || state == AL10.AL_PAUSED)
                {
                    AL10.alSourcePlay(source.intValue());
                }
            }
        }
    }

    public synchronized void pause(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            StreamingSource stream = this.streams.get(source);

            if (stream != null)
            {
                stream.paused = true;
            }

            AL10.alSourcePause(source.intValue());
        }
    }

    public synchronized void stop(String name)
    {
        this.removeSource(name);
    }

    public synchronized boolean playing(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source == null)
        {
            return false;
        }

        this.makeCurrent();
        StreamingSource stream = this.streams.get(source);

        if (stream != null)
        {
            return !stream.cancelled && (stream.pending || !stream.endOfStream || AL10.alGetSourcei(source.intValue(), AL10.AL_BUFFERS_QUEUED) > 0);
        }

        int state = AL10.alGetSourcei(source.intValue(), AL10.AL_SOURCE_STATE);
        return state == AL10.AL_PLAYING || state == AL10.AL_PAUSED;
    }

    public synchronized void removeSource(String name)
    {
        this.preparedSources.remove(name);
        Integer source = this.sourceByName.remove(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourceStop(source.intValue());
            StreamingSource stream = this.streams.remove(source);

            if (stream != null)
            {
                stream.cancelled = true;
                stream.cancelInput();
                int queued = AL10.alGetSourcei(source.intValue(), AL10.AL_BUFFERS_QUEUED);

                for (int i = 0; i < queued; ++i)
                {
                    AL10.alSourceUnqueueBuffers(source.intValue());
                }

                for (int buffer : stream.buffers)
                {
                    if (buffer != 0)
                    {
                        AL10.alDeleteBuffers(buffer);
                    }
                }
                this.retiredStreams.add(stream);
            }

            AL10.alSourcei(source.intValue(), AL10.AL_BUFFER, 0);
            this.nameBySource.remove(source);
        }
    }

    public synchronized void stopAll()
    {
        if (!this.created)
        {
            return;
        }

        this.makeCurrent();

        try
        {
            for (String name : new ArrayList<String>(this.sourceByName.keySet()))
            {
                this.removeSource(name);
            }

            this.sourceByName.clear();
            this.nameBySource.clear();
        }
        finally
        {
            this.releaseCurrent();
        }
    }

    public synchronized void setListenerPosition(float x, float y, float z)
    {
        this.makeCurrent();
        AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    }

    public synchronized void setListenerOrientation(float atX, float atY, float atZ, float upX, float upY, float upZ)
    {
        this.makeCurrent();
        this.listenerOrientation.clear();
        this.listenerOrientation.put(atX).put(atY).put(atZ).put(upX).put(upY).put(upZ);
        this.listenerOrientation.flip();
        AL10.alListenerfv(AL10.AL_ORIENTATION, this.listenerOrientation);
    }

    public synchronized String getPlaybackDeviceFingerprint()
    {
        StringBuilder fingerprint = new StringBuilder();
        append(fingerprint, alcString(MemoryUtil.NULL, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER));
        fingerprint.append('|');
        append(fingerprint, alcString(MemoryUtil.NULL, ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER));
        fingerprint.append('|');
        append(fingerprint, this.allDeviceSpecifiers());
        fingerprint.append('|');
        append(fingerprint, this.openedDeviceSpecifier);
        fingerprint.append('|');
        fingerprint.append(this.isOpenedDeviceConnected() ? '1' : '0');
        return fingerprint.toString();
    }

    public synchronized void requestReopenDefaultDevice()
    {
        this.reopenRequested = true;
    }

    public synchronized boolean consumeReopenFailure()
    {
        if (!this.reopenFailed)
        {
            return false;
        }

        this.reopenFailed = false;
        return true;
    }

    public synchronized void processPendingReopen()
    {
        if (!this.reopenRequested)
        {
            return;
        }

        this.reopenRequested = false;
        this.makeCurrent();

        try
        {
            if (!SOFTReopenDevice.alcReopenDeviceSOFT(this.device, (ByteBuffer)null, (IntBuffer)null))
            {
                this.reopenFailed = true;
                return;
            }

            this.openedDeviceSpecifier = ALC10.alcGetString(this.device, ALC10.ALC_DEVICE_SPECIFIER);
        }
        catch (Throwable ignored)
        {
            this.reopenFailed = true;
        }
    }

    private int bindSource(String name, boolean streaming)
    {
        Integer existing = this.sourceByName.get(name);

        if (existing != null)
        {
            this.removeSource(name);
        }

        int start = streaming ? NORMAL_SOURCES : 0;
        int count = streaming ? STREAMING_SOURCES : NORMAL_SOURCES;
        int next = streaming ? this.nextStreamingSource : this.nextNormalSource;
        int selected = -1;

        for (int i = 0; i < count; ++i)
        {
            int index = (next + i) % count;
            int source = this.sources[start + index];
            String oldName = this.nameBySource.get(Integer.valueOf(source));

            if (oldName == null || !this.playing(oldName))
            {
                selected = index;
                break;
            }
        }

        if (selected < 0)
        {
            selected = next;
        }

        int source = this.sources[start + selected];
        String oldName = this.nameBySource.get(Integer.valueOf(source));

        if (oldName != null)
        {
            this.removeSource(oldName);
        }

        if (streaming)
        {
            this.nextStreamingSource = (selected + 1) % count;
        }
        else
        {
            this.nextNormalSource = (selected + 1) % count;
        }

        this.sourceByName.put(name, Integer.valueOf(source));
        this.nameBySource.put(Integer.valueOf(source), name);
        return source;
    }

    private static void checkError(String action)
    {
        int error = AL10.alGetError();

        if (error != AL10.AL_NO_ERROR)
        {
            throw new IllegalStateException("OpenAL error " + error + " while attempting to " + action);
        }
    }

    private void runStreaming()
    {
        try
        {
            while (this.streamWorkerRunning)
            {
                ArrayList<StreamingSource> currentStreams;

                synchronized (this)
                {
                    currentStreams = new ArrayList<StreamingSource>(this.streams.values());
                }

                for (StreamingSource stream : currentStreams)
                {
                    if (!stream.cancelled)
                    {
                        try
                        {
                            this.fillStream(stream);
                        }
                        catch (Throwable throwable)
                        {
                            if (!stream.cancelled)
                            {
                                LOGGER.warn("Unable to play sound file: " + stream.location, throwable);
                            }

                            synchronized (this)
                            {
                                if (this.streams.get(Integer.valueOf(stream.source)) == stream)
                                {
                                    this.removeSource(stream.name);
                                    this.releaseCurrent();
                                }
                            }
                        }
                    }
                }

                this.closeRetiredStreams();

                try
                {
                    Thread.sleep(10L);
                }
                catch (InterruptedException exception)
                {
                    if (!this.streamWorkerRunning)
                    {
                        break;
                    }
                }
            }
        }
        finally
        {
            this.closeRetiredStreams();
            AL.setCurrentThread(null);
        }
    }

    private void closeRetiredStreams()
    {
        StreamingSource stream;

        while ((stream = this.retiredStreams.poll()) != null)
        {
            stream.closeDecoder();
        }
    }

    private void fillStream(StreamingSource stream) throws IOException
    {
        synchronized (this)
        {
            if (!this.created || stream.cancelled)
            {
                return;
            }

            this.makeCurrent();

            try
            {
                int processed = AL10.alGetSourcei(stream.source, AL10.AL_BUFFERS_PROCESSED);

                for (int i = 0; i < processed; ++i)
                {
                    stream.freeBuffers.addLast(Integer.valueOf(AL10.alSourceUnqueueBuffers(stream.source)));
                }

                if (!stream.pending && !stream.paused && stream.playRequested && AL10.alGetSourcei(stream.source, AL10.AL_BUFFERS_QUEUED) > 0 && AL10.alGetSourcei(stream.source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
                {
                    AL10.alSourcePlay(stream.source);
                }
            }
            finally
            {
                this.releaseCurrent();
            }
        }

        while (!stream.cancelled)
        {
            int buffer;

            synchronized (this)
            {
                if (!this.created || stream.cancelled || stream.endOfStream || stream.freeBuffers.isEmpty())
                {
                    return;
                }

                buffer = stream.freeBuffers.removeFirst().intValue();
            }

            if (stream.decoder == null)
            {
                stream.decoder = openStream(stream);
            }

            DecodedAudio audio = stream.decoder.read();

            if (audio == null && stream.loop && stream.hasSamples && !stream.cancelled)
            {
                stream.closeDecoder();
                stream.decoder = openStream(stream);
                audio = stream.decoder.read();
            }

            if (audio == null)
            {
                stream.closeDecoder();

                synchronized (this)
                {
                    stream.endOfStream = true;
                    stream.pending = false;

                    if (this.created && !stream.cancelled)
                    {
                        this.makeCurrent();

                        try
                        {
                            if (!stream.paused && stream.playRequested && AL10.alGetSourcei(stream.source, AL10.AL_BUFFERS_QUEUED) > 0 && AL10.alGetSourcei(stream.source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
                            {
                                AL10.alSourcePlay(stream.source);
                            }
                        }
                        finally
                        {
                            this.releaseCurrent();
                        }
                    }
                }

                return;
            }

            try
            {
                synchronized (this)
                {
                    if (!this.created || stream.cancelled)
                    {
                        return;
                    }

                    this.makeCurrent();

                    try
                    {
                        AL10.alBufferData(buffer, audio.format, audio.samples, audio.sampleRate);
                        AL10.alSourceQueueBuffers(stream.source, buffer);
                        checkError("queue streaming sound buffer");
                        stream.hasSamples = true;

                        if (AL10.alGetSourcei(stream.source, AL10.AL_BUFFERS_QUEUED) >= STREAM_BUFFERS)
                        {
                            stream.pending = false;
                        }

                        if (!stream.pending && !stream.paused && stream.playRequested && AL10.alGetSourcei(stream.source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
                        {
                            AL10.alSourcePlay(stream.source);
                        }
                    }
                    finally
                    {
                        this.releaseCurrent();
                    }
                }
            }
            finally
            {
                MemoryUtil.memFree(audio.samples);
            }
        }
    }

    private int getBuffer(ResourceLocation location)
    {
        String key = location.toString();
        Integer cached = this.buffers.get(key);

        if (cached != null)
        {
            return cached.intValue();
        }

        byte[] bytes = readResource(location);
        DecodedAudio audio = decode(bytes);
        int buffer = 0;

        try
        {
            buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, audio.format, audio.samples, audio.sampleRate);
            checkError("upload sound buffer");
            this.buffers.put(key, Integer.valueOf(buffer));
            return buffer;
        }
        catch (Throwable throwable)
        {
            if (buffer != 0)
            {
                AL10.alDeleteBuffers(buffer);
            }

            throw throwable;
        }
        finally
        {
            MemoryUtil.memFree(audio.samples);
        }
    }

    private static byte[] readResource(ResourceLocation location)
    {
        InputStream stream = null;

        try
        {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;

            while ((read = stream.read(chunk)) >= 0)
            {
                output.write(chunk, 0, read);
            }

            return output.toByteArray();
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Unable to read sound " + location, exception);
        }
        finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
    }

    private static DecodedAudio decode(byte[] bytes)
    {
        if (bytes.length >= 4 && bytes[0] == 'O' && bytes[1] == 'g' && bytes[2] == 'g' && bytes[3] == 'S')
        {
            return decodeOgg(bytes);
        }

        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F')
        {
            return decodeWav(bytes);
        }

        throw new IllegalStateException("Unsupported sound format");
    }

    private static DecodedAudio decodeOgg(byte[] bytes)
    {
        ByteBuffer data = MemoryUtil.memAlloc(bytes.length);
        data.put(bytes).flip();
        MemoryStack stack = MemoryStack.stackPush();
        long decoder = MemoryUtil.NULL;
        ShortBuffer pcm = null;
        boolean decodedSuccessfully = false;

        try
        {
            IntBuffer error = stack.mallocInt(1);
            decoder = STBVorbis.stb_vorbis_open_memory(data, error, null);

            if (decoder == MemoryUtil.NULL)
            {
                throw new IllegalStateException("Failed to decode ogg (error " + error.get(0) + ")");
            }

            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int sampleRate = info.sample_rate();

            if ((channels != 1 && channels != 2) || sampleRate <= 0)
            {
                throw new IllegalStateException("Unsupported ogg channel count or sample rate");
            }

            int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            pcm = MemoryUtil.memAllocShort(Math.max(Math.multiplyExact(samples, channels), 1));
            int decoded = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
            pcm.position(0);
            pcm.limit(Math.max(decoded, 0) * channels);
            int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            decodedSuccessfully = true;
            return new DecodedAudio(format, sampleRate, pcm);
        }
        finally
        {
            if (decoder != MemoryUtil.NULL)
            {
                STBVorbis.stb_vorbis_close(decoder);
            }

            if (!decodedSuccessfully && pcm != null)
            {
                MemoryUtil.memFree(pcm);
            }

            stack.close();
            MemoryUtil.memFree(data);
        }
    }

    private static DecodedAudio decodeWav(byte[] bytes)
    {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        if (buffer.remaining() < 12 || buffer.getInt() != 0x46464952)
        {
            throw new IllegalStateException("Not a RIFF wav");
        }

        buffer.getInt();
        if (buffer.getInt() != 0x45564157)
        {
            throw new IllegalStateException("Not a WAVE file");
        }

        int channels = 0;
        int sampleRate = 0;
        int bits = 0;

        while (buffer.remaining() >= 8)
        {
            int chunkId = buffer.getInt();
            int chunkSize = buffer.getInt();

            if (chunkSize < 0 || chunkSize > buffer.remaining())
            {
                throw new IllegalStateException("Incomplete wav chunk");
            }

            int chunkEnd = buffer.position() + chunkSize;
            if (chunkId == 0x20746D66)
            {
                if (chunkSize < 16)
                {
                    throw new IllegalStateException("Invalid wav format chunk");
                }

                int format = buffer.getShort() & 65535;
                channels = buffer.getShort() & 65535;
                sampleRate = buffer.getInt();
                buffer.getInt();
                buffer.getShort();
                bits = buffer.getShort() & 65535;

                if (format != 1 || (channels != 1 && channels != 2) || sampleRate <= 0 || (bits != 8 && bits != 16))
                {
                    throw new IllegalStateException("Unsupported wav format");
                }
            }
            else if (chunkId == 0x61746164)
            {
                if (channels == 0 || chunkSize % (channels * bits / 8) != 0)
                {
                    throw new IllegalStateException("Invalid wav data chunk");
                }

                int sampleCount = chunkSize / (bits / 8);
                ShortBuffer samples = MemoryUtil.memAllocShort(sampleCount);
                boolean success = false;

                try
                {
                    for (int sample = 0; sample < sampleCount; ++sample)
                    {
                        samples.put(bits == 16 ? buffer.getShort() : (short)(((buffer.get() & 255) - 128) << 8));
                    }

                    samples.flip();
                    success = true;
                    return new DecodedAudio(channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, sampleRate, samples);
                }
                finally
                {
                    if (!success)
                    {
                        MemoryUtil.memFree(samples);
                    }
                }
            }

            buffer.position(chunkEnd);
            if ((chunkSize & 1) != 0 && buffer.hasRemaining())
            {
                buffer.get();
            }
        }

        throw new IllegalStateException("Wav has no data chunk");
    }

    private boolean isOpenedDeviceConnected()
    {
        if (!this.created || this.device == MemoryUtil.NULL)
        {
            return true;
        }

        try
        {
            return ALC10.alcGetInteger(this.device, ALC_CONNECTED) != 0;
        }
        catch (Throwable ignored)
        {
            return true;
        }
    }

    private String allDeviceSpecifiers()
    {
        try
        {
            long pointer = ALC10.nalcGetString(MemoryUtil.NULL, ALC11.ALC_ALL_DEVICES_SPECIFIER);

            if (pointer == MemoryUtil.NULL)
            {
                return "";
            }

            StringBuilder devices = new StringBuilder();

            while (MemoryUtil.memGetByte(pointer) != 0)
            {
                String deviceName = MemoryUtil.memUTF8(pointer);

                if (deviceName == null || deviceName.length() == 0)
                {
                    break;
                }

                if (devices.length() > 0)
                {
                    devices.append(';');
                }

                devices.append(deviceName);
                pointer += deviceName.getBytes(StandardCharsets.UTF_8).length + 1;
            }

            return devices.toString();
        }
        catch (Throwable ignored)
        {
            return "";
        }
    }

    private static String alcString(long device, int parameter)
    {
        try
        {
            return ALC10.alcGetString(device, parameter);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    private static void append(StringBuilder builder, String value)
    {
        if (value != null)
        {
            builder.append(value);
        }
    }

    private static SoundStream openStream(StreamingSource stream) throws IOException
    {
        if (stream.cancelled)
        {
            throw new IOException("Sound stream was cancelled");
        }

        StreamInput streamInput = new StreamInput(Minecraft.getMinecraft().getResourceManager().getResource(stream.location).getInputStream());
        if (!stream.attachInput(streamInput))
        {
            throw new IOException("Sound stream was cancelled");
        }

        BufferedInputStream input = new BufferedInputStream(streamInput);

        try
        {
            input.mark(12);
            byte[] header = new byte[12];
            readFully(input, header, header.length);
            input.reset();

            if (header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S')
            {
                return new OggSoundStream(input);
            }

            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F')
            {
                return new WavSoundStream(input);
            }

            throw new IOException("Unsupported sound format: " + stream.location);
        }
        catch (Throwable throwable)
        {
            try
            {
                input.close();
            }
            catch (IOException ignored)
            {
            }

            stream.detachInput(streamInput);
            throw throwable;
        }
    }

    private static void readFully(InputStream input, byte[] bytes, int length) throws IOException
    {
        int offset = 0;

        while (offset < length)
        {
            int read = input.read(bytes, offset, length - offset);

            if (read < 0)
            {
                throw new IOException("Unexpected end of sound stream");
            }

            offset += read;
        }
    }

    private static void skipFully(InputStream input, long length) throws IOException
    {
        while (length > 0L)
        {
            long skipped = input.skip(length);

            if (skipped == 0L)
            {
                if (input.read() < 0)
                {
                    throw new IOException("Unexpected end of sound stream");
                }

                skipped = 1L;
            }

            length -= skipped;
        }
    }

    private interface SoundStream
    {
        DecodedAudio read() throws IOException;
        void close();
    }

    private static final class StreamInput extends InputStream
    {
        private final InputStream input;
        private final AtomicBoolean closed = new AtomicBoolean();

        StreamInput(InputStream input)
        {
            this.input = input;
        }

        private void checkOpen() throws IOException
        {
            if (this.closed.get())
            {
                throw new IOException("Sound stream input was closed");
            }
        }

        public int read() throws IOException
        {
            this.checkOpen();
            return this.input.read();
        }

        public int read(byte[] bytes, int offset, int length) throws IOException
        {
            this.checkOpen();
            return this.input.read(bytes, offset, length);
        }

        public long skip(long length) throws IOException
        {
            this.checkOpen();
            return this.input.skip(length);
        }

        public int available() throws IOException
        {
            this.checkOpen();
            return this.input.available();
        }

        public void close() throws IOException
        {
            if (this.closed.compareAndSet(false, true))
            {
                this.input.close();
            }
        }

        void cancel()
        {
            if (this.closed.compareAndSet(false, true))
            {
                STREAM_INPUT_CLOSER.execute(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            StreamInput.this.input.close();
                        }
                        catch (IOException ignored)
                        {
                        }
                    }
                });
            }
        }
    }

    private static final class StreamingSource
    {
        final String name;
        final int source;
        final ResourceLocation location;
        final boolean loop;
        final int[] buffers = new int[STREAM_BUFFERS];
        final ArrayDeque<Integer> freeBuffers = new ArrayDeque<Integer>();
        volatile boolean cancelled;
        boolean pending = true;
        boolean endOfStream;
        boolean playRequested;
        boolean paused;
        boolean hasSamples;
        SoundStream decoder;
        private StreamInput input;

        StreamingSource(String name, int source, ResourceLocation location, boolean loop)
        {
            this.name = name;
            this.source = source;
            this.location = location;
            this.loop = loop;
        }

        void closeDecoder()
        {
            if (this.decoder != null)
            {
                this.decoder.close();
                this.decoder = null;
            }

            synchronized (this)
            {
                this.input = null;
            }
        }

        boolean attachInput(StreamInput input)
        {
            synchronized (this)
            {
                if (!this.cancelled)
                {
                    this.input = input;
                    return true;
                }
            }

            input.cancel();
            return false;
        }

        synchronized void detachInput(StreamInput input)
        {
            if (this.input == input)
            {
                this.input = null;
            }
        }

        void cancelInput()
        {
            StreamInput input;

            synchronized (this)
            {
                input = this.input;
                this.input = null;
            }

            if (input != null)
            {
                input.cancel();
            }
        }
    }

    private static final class OggSoundStream implements SoundStream
    {
        private final InputStream input;
        private final byte[] readBuffer = new byte[8192];
        private ByteBuffer data;
        private long decoder;
        private boolean inputEnded;
        private int channels;
        private int sampleRate;
        private DecodedAudio remainingFrame;

        OggSoundStream(InputStream input) throws IOException
        {
            this.input = input;
            this.data = MemoryUtil.memAlloc(16384);
            this.data.limit(0);
            boolean initialized = false;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer used = stack.mallocInt(1);
                IntBuffer error = stack.mallocInt(1);

                while (this.decoder == MemoryUtil.NULL)
                {
                    if (!this.fill())
                    {
                        throw new IOException("Unexpected end of ogg header");
                    }

                    this.decoder = STBVorbis.stb_vorbis_open_pushdata(this.data, used, error, null);

                    if (this.decoder == MemoryUtil.NULL && error.get(0) != STBVorbis.VORBIS_need_more_data)
                    {
                        throw new IOException("Failed to decode ogg header (error " + error.get(0) + ")");
                    }
                }

                this.data.position(this.data.position() + used.get(0));
                STBVorbisInfo info = STBVorbisInfo.malloc(stack);
                STBVorbis.stb_vorbis_get_info(this.decoder, info);
                this.channels = info.channels();
                this.sampleRate = info.sample_rate();

                if ((this.channels != 1 && this.channels != 2) || this.sampleRate <= 0)
                {
                    throw new IOException("Unsupported ogg channel count or sample rate");
                }

                initialized = true;
            }
            finally
            {
                if (!initialized)
                {
                    this.close();
                }
            }
        }

        private boolean fill() throws IOException
        {
            if (this.inputEnded)
            {
                return false;
            }

            this.data.compact();

            if (!this.data.hasRemaining())
            {
                ByteBuffer larger = MemoryUtil.memAlloc(Math.multiplyExact(this.data.capacity(), 2));
                this.data.flip();
                larger.put(this.data);
                MemoryUtil.memFree(this.data);
                this.data = larger;
            }

            int read = this.input.read(this.readBuffer, 0, Math.min(this.readBuffer.length, this.data.remaining()));

            if (read < 0)
            {
                this.inputEnded = true;
            }
            else
            {
                this.data.put(this.readBuffer, 0, read);
            }

            this.data.flip();
            return read >= 0;
        }

        public DecodedAudio read() throws IOException
        {
            ShortBuffer pcm = MemoryUtil.memAllocShort(STREAM_BUFFER_BYTES / 2);
            boolean success = false;

            try
            {
                while (pcm.hasRemaining())
                {
                    if (this.remainingFrame == null)
                    {
                        this.remainingFrame = this.readFrame();

                        if (this.remainingFrame == null)
                        {
                            break;
                        }
                    }

                    ShortBuffer frame = this.remainingFrame.samples;
                    int copied = Math.min(pcm.remaining(), frame.remaining());
                    int frameLimit = frame.limit();
                    frame.limit(frame.position() + copied);

                    try
                    {
                        pcm.put(frame);
                    }
                    finally
                    {
                        frame.limit(frameLimit);
                    }

                    if (!frame.hasRemaining())
                    {
                        MemoryUtil.memFree(frame);
                        this.remainingFrame = null;
                    }
                }

                if (pcm.position() == 0)
                {
                    return null;
                }

                pcm.flip();
                success = true;
                return new DecodedAudio(this.channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, this.sampleRate, pcm);
            }
            finally
            {
                if (!success)
                {
                    MemoryUtil.memFree(pcm);
                }
            }
        }

        private DecodedAudio readFrame() throws IOException
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer channelCount = stack.mallocInt(1);
                IntBuffer sampleCount = stack.mallocInt(1);
                PointerBuffer output = stack.mallocPointer(1);

                while (true)
                {
                    if (!this.data.hasRemaining() && !this.fill())
                    {
                        return null;
                    }

                    int consumed = STBVorbis.stb_vorbis_decode_frame_pushdata(this.decoder, this.data, channelCount, output, sampleCount);
                    this.data.position(this.data.position() + consumed);
                    int error = STBVorbis.stb_vorbis_get_error(this.decoder);

                    if (error != STBVorbis.VORBIS__no_error && error != STBVorbis.VORBIS_need_more_data)
                    {
                        throw new IOException("Failed to decode ogg frame (error " + error + ")");
                    }

                    int samples = sampleCount.get(0);

                    if (samples > 0)
                    {
                        if (channelCount.get(0) != this.channels)
                        {
                            throw new IOException("Ogg channel count changed during playback");
                        }

                        PointerBuffer channelPointers = MemoryUtil.memPointerBuffer(output.get(0), this.channels);
                        FloatBuffer[] channelSamples = new FloatBuffer[this.channels];

                        for (int channel = 0; channel < this.channels; ++channel)
                        {
                            channelSamples[channel] = MemoryUtil.memFloatBuffer(channelPointers.get(channel), samples);
                        }

                        ShortBuffer pcm = MemoryUtil.memAllocShort(samples * this.channels);

                        for (int sample = 0; sample < samples; ++sample)
                        {
                            for (int channel = 0; channel < this.channels; ++channel)
                            {
                                int value = Math.round(channelSamples[channel].get(sample) * 32768.0F);
                                pcm.put((short)Math.max(-32768, Math.min(32767, value)));
                            }
                        }

                        pcm.flip();
                        return new DecodedAudio(this.channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, this.sampleRate, pcm);
                    }

                    if (consumed == 0 && !this.fill())
                    {
                        return null;
                    }
                }
            }
        }

        public void close()
        {
            if (this.remainingFrame != null)
            {
                MemoryUtil.memFree(this.remainingFrame.samples);
                this.remainingFrame = null;
            }

            if (this.decoder != MemoryUtil.NULL)
            {
                STBVorbis.stb_vorbis_close(this.decoder);
                this.decoder = MemoryUtil.NULL;
            }

            if (this.data != null)
            {
                MemoryUtil.memFree(this.data);
                this.data = null;
            }

            try
            {
                this.input.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    private static final class WavSoundStream implements SoundStream
    {
        private final InputStream input;
        private int channels;
        private int sampleRate;
        private int bits;
        private long remaining;

        WavSoundStream(InputStream input) throws IOException
        {
            this.input = input;
            byte[] header = new byte[16];
            readFully(input, header, 12);
            ByteBuffer values = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

            if (values.getInt(0) != 0x46464952 || values.getInt(8) != 0x45564157)
            {
                throw new IOException("Not a WAVE file");
            }

            while (true)
            {
                readFully(input, header, 8);
                int chunkId = values.getInt(0);
                long chunkSize = Integer.toUnsignedLong(values.getInt(4));

                if (chunkId == 0x20746D66)
                {
                    if (chunkSize < 16L)
                    {
                        throw new IOException("Invalid wav format chunk");
                    }

                    readFully(input, header, 16);
                    int format = values.getShort(0) & 65535;
                    this.channels = values.getShort(2) & 65535;
                    this.sampleRate = values.getInt(4);
                    this.bits = values.getShort(14) & 65535;

                    if (format != 1 || (this.channels != 1 && this.channels != 2) || this.sampleRate <= 0 || (this.bits != 8 && this.bits != 16))
                    {
                        throw new IOException("Unsupported wav format");
                    }

                    skipFully(input, chunkSize - 16L + (chunkSize & 1L));
                }
                else if (chunkId == 0x61746164)
                {
                    if (this.channels == 0)
                    {
                        throw new IOException("Wav data precedes format chunk");
                    }

                    this.remaining = chunkSize;
                    break;
                }
                else
                {
                    skipFully(input, chunkSize + (chunkSize & 1L));
                }
            }
        }

        public DecodedAudio read() throws IOException
        {
            if (this.remaining == 0L)
            {
                return null;
            }

            int bytesPerSample = this.bits / 8;
            int length = (int)Math.min(this.remaining, (long)(STREAM_BUFFER_BYTES / 2) * (long)bytesPerSample);

            if (length % (this.channels * bytesPerSample) != 0)
            {
                throw new IOException("Incomplete wav sample frame");
            }

            byte[] bytes = new byte[length];
            readFully(this.input, bytes, length);
            this.remaining -= length;
            ByteBuffer values = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer pcm = MemoryUtil.memAllocShort(length / bytesPerSample);

            while (values.hasRemaining())
            {
                pcm.put(this.bits == 16 ? values.getShort() : (short)(((values.get() & 255) - 128) << 8));
            }

            pcm.flip();
            return new DecodedAudio(this.channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, this.sampleRate, pcm);
        }

        public void close()
        {
            try
            {
                this.input.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    private static final class DecodedAudio
    {
        final int format;
        final int sampleRate;
        final ShortBuffer samples;

        DecodedAudio(int format, int sampleRate, ShortBuffer samples)
        {
            this.format = format;
            this.sampleRate = sampleRate;
            this.samples = samples;
        }
    }
}
