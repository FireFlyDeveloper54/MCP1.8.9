package net.minecraft.client.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.lwjgl.openal.SOFTReopenDevice;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class OpenAlEngine
{
    private static final int MAX_SOURCES = 32;
    private static final int ALC_CONNECTED = 0x313;
    private long device;
    private long context;
    private ALCCapabilities deviceCaps;
    private static final ThreadLocal<Boolean> threadCaps = new ThreadLocal<Boolean>();
    private int[] sources;
    private boolean created;
    private float masterVolume = 1.0F;
    private String openedDeviceSpecifier;
    private volatile boolean reopenRequested;
    private volatile boolean reopenFailed;
    private final Map<String, Integer> sourceByName = new HashMap<String, Integer>();
    private final Map<Integer, String> nameBySource = new HashMap<Integer, String>();
    private final Map<String, Integer> buffers = new HashMap<String, Integer>();
    private final FloatBuffer listenerOrientation = MemoryUtil.memAllocFloat(6);

    public void create()
    {
        if (this.created)
        {
            return;
        }

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

        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(this.deviceCaps);
        threadCaps.set(Boolean.TRUE);
        this.openedDeviceSpecifier = ALC10.alcGetString(this.device, ALC10.ALC_DEVICE_SPECIFIER);
        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
        AL10.alListener3f(AL10.AL_VELOCITY, 0.0F, 0.0F, 0.0F);
        this.sources = new int[MAX_SOURCES];
        AL10.alGenSources(this.sources);
        this.created = true;
        this.setMasterVolume(this.masterVolume);
        ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
    }

    public void destroy()
    {
        if (!this.created)
        {
            return;
        }

        this.makeCurrent();
        this.stopAll();

        for (Integer buffer : this.buffers.values())
        {
            AL10.alDeleteBuffers(buffer.intValue());
        }

        this.buffers.clear();
        this.sourceByName.clear();
        this.nameBySource.clear();
        AL10.alDeleteSources(this.sources);
        this.sources = null;
        ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
        ALC10.alcDestroyContext(this.context);
        ALC10.alcCloseDevice(this.device);
        AL.setCurrentProcess(null);
        this.context = MemoryUtil.NULL;
        this.device = MemoryUtil.NULL;
        this.openedDeviceSpecifier = null;
        this.created = false;
        this.deviceCaps = null;
        this.reopenRequested = false;
        this.reopenFailed = false;
        threadCaps.remove();
    }

    public boolean isCreated()
    {
        return this.created;
    }

    public void makeCurrent()
    {
        if (this.created && this.context != MemoryUtil.NULL)
        {
            ALC10.alcMakeContextCurrent(this.context);

            if (threadCaps.get() != Boolean.TRUE)
            {
                AL.createCapabilities(this.deviceCaps);
                threadCaps.set(Boolean.TRUE);
            }
        }
    }

    public void setMasterVolume(float volume)
    {
        this.masterVolume = MathHelper.clamp_float(volume, 0.0F, 1.0F);

        if (this.created)
        {
            this.makeCurrent();
            AL10.alListenerf(AL10.AL_GAIN, this.masterVolume);
        }
    }

    public float getMasterVolume()
    {
        return this.masterVolume;
    }

    public boolean newSource(String name, ResourceLocation location, boolean loop, float x, float y, float z, boolean linear, float maxDistance)
    {
        this.makeCurrent();
        int source = this.bindSource(name);

        if (source < 0)
        {
            return false;
        }

        int buffer = this.getBuffer(location);
        AL10.alSourceStop(source);
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
        AL10.alSourcei(source, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
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
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSource3f(source, AL10.AL_POSITION, 0.0F, 0.0F, 0.0F);
            AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 0.0F);
        }

        return true;
    }

    public void setVolume(String name, float volume)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourcef(source.intValue(), AL10.AL_GAIN, MathHelper.clamp_float(volume, 0.0F, 1.0F));
        }
    }

    public void setPitch(String name, float pitch)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourcef(source.intValue(), AL10.AL_PITCH, MathHelper.clamp_float(pitch, 0.5F, 2.0F));
        }
    }

    public void setPosition(String name, float x, float y, float z)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSource3f(source.intValue(), AL10.AL_POSITION, x, y, z);
        }
    }

    public void play(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourcePlay(source.intValue());
        }
    }

    public void pause(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourcePause(source.intValue());
        }
    }

    public void stop(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourceStop(source.intValue());
        }
    }

    public boolean playing(String name)
    {
        Integer source = this.sourceByName.get(name);

        if (source == null)
        {
            return false;
        }

        this.makeCurrent();
        int state = AL10.alGetSourcei(source.intValue(), AL10.AL_SOURCE_STATE);
        return state == AL10.AL_PLAYING || state == AL10.AL_PAUSED;
    }

    public void removeSource(String name)
    {
        Integer source = this.sourceByName.remove(name);

        if (source != null)
        {
            this.makeCurrent();
            AL10.alSourceStop(source.intValue());
            AL10.alSourcei(source.intValue(), AL10.AL_BUFFER, 0);
            this.nameBySource.remove(source);
        }
    }

    public void stopAll()
    {
        if (!this.created)
        {
            return;
        }

        this.makeCurrent();
        Iterator<Integer> iterator = this.sourceByName.values().iterator();

        while (iterator.hasNext())
        {
            int source = iterator.next().intValue();
            AL10.alSourceStop(source);
            AL10.alSourcei(source, AL10.AL_BUFFER, 0);
        }

        this.sourceByName.clear();
        this.nameBySource.clear();
    }

    public void setListenerPosition(float x, float y, float z)
    {
        this.makeCurrent();
        AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    }

    public void setListenerOrientation(float atX, float atY, float atZ, float upX, float upY, float upZ)
    {
        this.makeCurrent();
        this.listenerOrientation.clear();
        this.listenerOrientation.put(atX).put(atY).put(atZ).put(upX).put(upY).put(upZ);
        this.listenerOrientation.flip();
        AL10.alListenerfv(AL10.AL_ORIENTATION, this.listenerOrientation);
    }

    public String getPlaybackDeviceFingerprint()
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

    public void requestReopenDefaultDevice()
    {
        this.reopenRequested = true;
    }

    public boolean consumeReopenFailure()
    {
        if (!this.reopenFailed)
        {
            return false;
        }

        this.reopenFailed = false;
        return true;
    }

    public void processPendingReopen()
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

    private int bindSource(String name)
    {
        Integer existing = this.sourceByName.get(name);

        if (existing != null)
        {
            return existing.intValue();
        }

        for (int i = 0; i < this.sources.length; ++i)
        {
            int source = this.sources[i];

            if (!this.nameBySource.containsKey(Integer.valueOf(source)))
            {
                this.sourceByName.put(name, Integer.valueOf(source));
                this.nameBySource.put(Integer.valueOf(source), name);
                return source;
            }
        }

        return -1;
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
        int buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, audio.format, audio.samples, audio.sampleRate);
        MemoryUtil.memFree(audio.samples);
        this.buffers.put(key, Integer.valueOf(buffer));
        return buffer;
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

        try
        {
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(data, error, null);

            if (decoder == MemoryUtil.NULL)
            {
                throw new IllegalStateException("Failed to decode ogg (error " + error.get(0) + ")");
            }

            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int sampleRate = info.sample_rate();
            int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            ShortBuffer pcm = MemoryUtil.memAllocShort(Math.max(samples * channels, 1));
            int decoded = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
            pcm.position(0);
            pcm.limit(Math.max(decoded, 0) * channels);
            STBVorbis.stb_vorbis_close(decoder);
            int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            return new DecodedAudio(format, sampleRate, pcm);
        }
        finally
        {
            stack.close();
            MemoryUtil.memFree(data);
        }
    }

    private static DecodedAudio decodeWav(byte[] bytes)
    {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        if (buffer.getInt() != 0x46464952)
        {
            throw new IllegalStateException("Not a RIFF wav");
        }

        buffer.getInt();

        if (buffer.getInt() != 0x45564157)
        {
            throw new IllegalStateException("Not a WAVE file");
        }

        int channels = 1;
        int sampleRate = 44100;
        int bits = 16;
        ShortBuffer samples = null;

        while (buffer.remaining() >= 8)
        {
            int chunkId = buffer.getInt();
            int chunkSize = buffer.getInt();

            if (chunkSize < 0 || chunkSize > buffer.remaining())
            {
                break;
            }

            int chunkEnd = buffer.position() + chunkSize;

            if (chunkId == 0x20746D66)
            {
                int format = buffer.getShort() & 0xFFFF;
                channels = buffer.getShort() & 0xFFFF;
                sampleRate = buffer.getInt();
                buffer.getInt();
                buffer.getShort();
                bits = buffer.getShort() & 0xFFFF;

                if (format != 1)
                {
                    throw new IllegalStateException("Only PCM wav is supported");
                }
            }
            else if (chunkId == 0x61746164)
            {
                if (bits == 16)
                {
                    samples = MemoryUtil.memAllocShort(chunkSize / 2);

                    for (int i = 0; i < chunkSize / 2; ++i)
                    {
                        samples.put(buffer.getShort());
                    }

                    samples.flip();
                }
                else if (bits == 8)
                {
                    samples = MemoryUtil.memAllocShort(chunkSize);

                    for (int i = 0; i < chunkSize; ++i)
                    {
                        samples.put((short)((buffer.get() & 0xFF) << 8));
                    }

                    samples.flip();
                }
                else
                {
                    throw new IllegalStateException("Unsupported wav bit depth " + bits);
                }
            }

            buffer.position(chunkEnd + (chunkSize & 1));
        }

        if (samples == null)
        {
            throw new IllegalStateException("Wav has no data chunk");
        }

        int format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        return new DecodedAudio(format, sampleRate, samples);
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
