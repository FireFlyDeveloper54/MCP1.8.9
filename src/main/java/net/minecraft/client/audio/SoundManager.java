package net.minecraft.client.audio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import io.netty.util.internal.ThreadLocalRandom;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Source;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager
{
    private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
    private static final Logger logger = LogManager.getLogger();
    private final SoundHandler sndHandler;
    private final GameSettings options;
    private SoundManager.SoundSystemStarterThread sndSystem;
    private boolean loaded;
    private int playTime = 0;
    private final BiMap<String, ISound> playingSounds = HashBiMap.<String, ISound>create();
    private final BiMap<ISound, String> invPlayingSounds;
    private Map<ISound, SoundPoolEntry> playingSoundPoolEntries;
    private final Multimap<SoundCategory, String> categorySounds;
    private final List<ITickableSound> tickableSounds;
    private final Map<ISound, Integer> delayedSounds;
    private final Map<String, Integer> playingSoundsStopTime;

    public SoundManager(SoundHandler soundHandlerIn, GameSettings optionsIn)
    {
        this.invPlayingSounds = this.playingSounds.inverse();
        this.playingSoundPoolEntries = Maps.<ISound, SoundPoolEntry>newHashMap();
        this.categorySounds = HashMultimap.<SoundCategory, String>create();
        this.tickableSounds = Lists.<ITickableSound>newArrayList();
        this.delayedSounds = Maps.<ISound, Integer>newHashMap();
        this.playingSoundsStopTime = Maps.<String, Integer>newHashMap();
        this.sndHandler = soundHandlerIn;
        this.options = optionsIn;

        try
        {
            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
        }
        catch (SoundSystemException soundSystemException)
        {
            logger.error(LOG_MARKER, (String)"Error linking with the LibraryJavaSound plug-in", (Throwable)soundSystemException);
        }
    }

    public void reloadSoundSystem()
    {
        this.unloadSoundSystem();
        this.loadSoundSystem();
    }

    private synchronized void loadSoundSystem()
    {
        if (!this.loaded)
        {
            try
            {
                (new Thread(new Runnable()
                {
                    public void run()
                    {
                        SoundSystemConfig.setLogger(new SoundSystemLogger()
                        {
                            public void message(String message, int indent)
                            {
                                if (!message.isEmpty())
                                {
                                    SoundManager.logger.info(message);
                                }
                            }
                            public void importantMessage(String message, int indent)
                            {
                                if (!message.isEmpty())
                                {
                                    SoundManager.logger.warn(message);
                                }
                            }
                            public void errorMessage(String className, String message, int indent)
                            {
                                if (!message.isEmpty())
                                {
                                    SoundManager.logger.error("Error in class \'" + className + "\'");
                                    SoundManager.logger.error(message);
                                }
                            }
                        });
                        SoundManager.this.sndSystem = SoundManager.this.new SoundSystemStarterThread();
                        SoundManager.this.loaded = true;
                        SoundManager.this.sndSystem.setMasterVolume(SoundManager.this.options.getSoundLevel(SoundCategory.MASTER));
                        SoundManager.logger.info(SoundManager.LOG_MARKER, "Sound engine started");
                    }
                }, "Sound Library Loader")).start();
            }
            catch (RuntimeException runtimeexception)
            {
                logger.error(LOG_MARKER, (String)"Error starting SoundSystem. Turning off sounds & music", (Throwable)runtimeexception);
                this.options.setSoundLevel(SoundCategory.MASTER, 0.0F);
                this.options.saveOptions();
            }
        }
    }

    private float getSoundCategoryVolume(SoundCategory category)
    {
        return category != null && category != SoundCategory.MASTER ? this.options.getSoundLevel(category) : 1.0F;
    }

    public void setSoundCategoryVolume(SoundCategory category, float volume)
    {
        if (this.loaded)
        {
            if (category == SoundCategory.MASTER)
            {
                this.sndSystem.setMasterVolume(volume);
            }
            else
            {
                for (String channelName : this.categorySounds.get(category))
                {
                    ISound sound = this.playingSounds.get(channelName);
                    float categoryVolume = this.getNormalizedVolume(sound, this.playingSoundPoolEntries.get(sound), category);

                    if (categoryVolume <= 0.0F)
                    {
                        this.stopSound(sound);
                    }
                    else
                    {
                        this.sndSystem.setVolume(channelName, categoryVolume);
                    }
                }
            }
        }
    }

    public void unloadSoundSystem()
    {
        if (this.loaded)
        {
            this.stopAllSounds();
            this.sndSystem.cleanup();
            this.loaded = false;
        }
    }

    public void stopAllSounds()
    {
        if (this.loaded)
        {
            for (String channelName : this.playingSounds.keySet())
            {
                this.sndSystem.stop(channelName);
            }

            this.playingSounds.clear();
            this.delayedSounds.clear();
            this.tickableSounds.clear();
            this.categorySounds.clear();
            this.playingSoundPoolEntries.clear();
            this.playingSoundsStopTime.clear();
        }
    }

    public void updateAllSounds()
    {
        ++this.playTime;

        for (ITickableSound tickableSound : this.tickableSounds)
        {
            tickableSound.update();

            if (tickableSound.isDonePlaying())
            {
                this.stopSound(tickableSound);
            }
            else
            {
                String channelName = this.invPlayingSounds.get(tickableSound);
                this.sndSystem.setVolume(channelName, this.getNormalizedVolume(tickableSound, this.playingSoundPoolEntries.get(tickableSound), this.sndHandler.getSound(tickableSound.getSoundLocation()).getSoundCategory()));
                this.sndSystem.setPitch(channelName, this.getNormalizedPitch(tickableSound, this.playingSoundPoolEntries.get(tickableSound)));
                this.sndSystem.setPosition(channelName, tickableSound.getXPosF(), tickableSound.getYPosF(), tickableSound.getZPosF());
            }
        }

        Iterator<Entry<String, ISound>> playingSoundIterator = this.playingSounds.entrySet().iterator();

        while (playingSoundIterator.hasNext())
        {
            Entry<String, ISound> entry = playingSoundIterator.next();
            String channelName = entry.getKey();
            ISound sound = entry.getValue();

            if (!this.sndSystem.playing(channelName))
            {
                int stopTime = this.playingSoundsStopTime.get(channelName).intValue();

                if (stopTime <= this.playTime)
                {
                    int repeatDelay = sound.getRepeatDelay();

                    if (sound.canRepeat() && repeatDelay > 0)
                    {
                        this.delayedSounds.put(sound, Integer.valueOf(this.playTime + repeatDelay));
                    }

                    playingSoundIterator.remove();
                    logger.debug(LOG_MARKER, "Removed channel {} because it\'s not playing anymore", new Object[] {channelName});
                    this.sndSystem.removeSource(channelName);
                    this.playingSoundsStopTime.remove(channelName);
                    this.playingSoundPoolEntries.remove(sound);

                    try
                    {
                        this.categorySounds.remove(this.sndHandler.getSound(sound.getSoundLocation()).getSoundCategory(), channelName);
                    }
                    catch (RuntimeException ignored)
                    {
                        ;
                    }

                    if (sound instanceof ITickableSound)
                    {
                        this.tickableSounds.remove(sound);
                    }
                }
            }
        }

        Iterator<Entry<ISound, Integer>> delayedSoundIterator = this.delayedSounds.entrySet().iterator();

        while (delayedSoundIterator.hasNext())
        {
            Entry<ISound, Integer> delayedEntry = delayedSoundIterator.next();

            if (this.playTime >= delayedEntry.getValue().intValue())
            {
                ISound delayedSound = delayedEntry.getKey();

                if (delayedSound instanceof ITickableSound)
                {
                    ((ITickableSound)delayedSound).update();
                }

                this.playSound(delayedSound);
                delayedSoundIterator.remove();
            }
        }
    }

    public boolean isSoundPlaying(ISound sound)
    {
        if (!this.loaded)
        {
            return false;
        }
        else
        {
            String channelName = this.invPlayingSounds.get(sound);
            Integer stopTime = channelName == null ? null : this.playingSoundsStopTime.get(channelName);
            return channelName == null ? false : this.sndSystem.playing(channelName) || stopTime != null && stopTime.intValue() <= this.playTime;
        }
    }

    public void stopSound(ISound sound)
    {
        if (this.loaded)
        {
            String channelName = this.invPlayingSounds.get(sound);

            if (channelName != null)
            {
                this.sndSystem.stop(channelName);
            }
        }
    }

    public void playSound(ISound sound)
    {
        if (this.loaded)
        {
            if (this.sndSystem.getMasterVolume() <= 0.0F)
            {
                logger.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", new Object[] {sound.getSoundLocation()});
            }
            else
            {
                SoundEventAccessorComposite soundEvent = this.sndHandler.getSound(sound.getSoundLocation());

                if (soundEvent == null)
                {
                    logger.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", new Object[] {sound.getSoundLocation()});
                }
                else
                {
                    SoundPoolEntry soundEntry = soundEvent.cloneEntry();

                    if (soundEntry == SoundHandler.missing_sound)
                    {
                        logger.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", new Object[] {soundEvent.getSoundEventLocation()});
                    }
                    else
                    {
                        float sourceVolume = sound.getVolume();
                        float attenuationDistance = 16.0F;

                        if (sourceVolume > 1.0F)
                        {
                            attenuationDistance *= sourceVolume;
                        }

                        SoundCategory soundCategory = soundEvent.getSoundCategory();
                        float normalizedVolume = this.getNormalizedVolume(sound, soundEntry, soundCategory);
                        double normalizedPitch = (double)this.getNormalizedPitch(sound, soundEntry);
                        ResourceLocation soundLocation = soundEntry.getSoundPoolEntryLocation();

                        if (normalizedVolume == 0.0F)
                        {
                            logger.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", new Object[] {soundLocation});
                        }
                        else
                        {
                            boolean repeatImmediately = sound.canRepeat() && sound.getRepeatDelay() == 0;
                            String channelName = MathHelper.getRandomUuid(ThreadLocalRandom.current()).toString();

                            if (soundEntry.isStreamingSound())
                            {
                                this.sndSystem.newStreamingSource(false, channelName, getURLForSoundResource(soundLocation), soundLocation.toString(), repeatImmediately, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), attenuationDistance);
                            }
                            else
                            {
                                this.sndSystem.newSource(false, channelName, getURLForSoundResource(soundLocation), soundLocation.toString(), repeatImmediately, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), attenuationDistance);
                            }

                            logger.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", new Object[] {soundEntry.getSoundPoolEntryLocation(), soundEvent.getSoundEventLocation(), channelName});
                            this.sndSystem.setPitch(channelName, (float)normalizedPitch);
                            this.sndSystem.setVolume(channelName, normalizedVolume);
                            this.sndSystem.play(channelName);
                            this.playingSoundsStopTime.put(channelName, Integer.valueOf(this.playTime + 20));
                            this.playingSounds.put(channelName, sound);
                            this.playingSoundPoolEntries.put(sound, soundEntry);

                            if (soundCategory != SoundCategory.MASTER)
                            {
                                this.categorySounds.put(soundCategory, channelName);
                            }

                            if (sound instanceof ITickableSound)
                            {
                                this.tickableSounds.add((ITickableSound)sound);
                            }
                        }
                    }
                }
            }
        }
    }

    private float getNormalizedPitch(ISound sound, SoundPoolEntry entry)
    {
        return (float)MathHelper.clamp_double((double)sound.getPitch() * entry.getPitch(), 0.5D, 2.0D);
    }

    private float getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category)
    {
        return (float)MathHelper.clamp_double((double)sound.getVolume() * entry.getVolume(), 0.0D, 1.0D) * this.getSoundCategoryVolume(category);
    }

    public void pauseAllSounds()
    {
        for (String channelName : this.playingSounds.keySet())
        {
            logger.debug(LOG_MARKER, "Pausing channel {}", new Object[] {channelName});
            this.sndSystem.pause(channelName);
        }
    }

    public void resumeAllSounds()
    {
        for (String channelName : this.playingSounds.keySet())
        {
            logger.debug(LOG_MARKER, "Resuming channel {}", new Object[] {channelName});
            this.sndSystem.play(channelName);
        }
    }

    public void playDelayedSound(ISound sound, int delay)
    {
        this.delayedSounds.put(sound, Integer.valueOf(this.playTime + delay));
    }

    private static URL getURLForSoundResource(final ResourceLocation soundResource)
    {
        String resourceUrl = "mcsounddomain:" + soundResource.getResourceDomain() + ":" + soundResource.getResourcePath();
        URLStreamHandler streamHandler = new URLStreamHandler()
        {
            protected URLConnection openConnection(final URL url)
            {
                return new URLConnection(url)
                {
                    public void connect() throws IOException
                    {
                    }
                    public InputStream getInputStream() throws IOException
                    {
                        return Minecraft.getMinecraft().getResourceManager().getResource(soundResource).getInputStream();
                    }
                };
            }
        };

        try
        {
            return new URL((URL)null, resourceUrl, streamHandler);
        }
        catch (MalformedURLException caughtMalformedURLException)
        {
            throw new IllegalStateException("Unable to create sound resource URL: " + resourceUrl, caughtMalformedURLException);
        }
    }

    public void setListener(EntityPlayer player, float partialTicks)
    {
        if (this.loaded && player != null)
        {
            float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
            double xCoordinate = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double yCoordinate = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks + (double)player.getEyeHeight();
            double zCoordinate = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            float[] yawSC = new float[2];
            float[] pitchSC = new float[2];
            float[] upPitchSC = new float[2];
            MathHelper.sinCosDeg(yaw + 90.0F, yawSC);
            MathHelper.sinCosDeg(-pitch, pitchSC);
            MathHelper.sinCosDeg(-pitch + 90.0F, upPitchSC);
            float yawSin = yawSC[0];
            float yawCos = yawSC[1];
            float pitchSin = pitchSC[0];
            float pitchCos = pitchSC[1];
            float upPitchSin = upPitchSC[0];
            float upPitchCos = upPitchSC[1];
            float lookX = yawCos * pitchCos;
            float lookZ = yawSin * pitchCos;
            float upX = yawCos * upPitchCos;
            float upZ = yawSin * upPitchCos;
            this.sndSystem.setListenerPosition((float)xCoordinate, (float)yCoordinate, (float)zCoordinate);
            this.sndSystem.setListenerOrientation(lookX, pitchSin, lookZ, upX, upPitchSin, upZ);
        }
    }

    class SoundSystemStarterThread extends SoundSystem
    {
        private SoundSystemStarterThread()
        {
        }

        public boolean playing(String channelName)
        {
            synchronized (SoundSystemConfig.THREAD_SYNC)
            {
                if (this.soundLibrary == null)
                {
                    return false;
                }
                else
                {
                    Source source = (Source)this.soundLibrary.getSources().get(channelName);
                    return source == null ? false : source.playing() || source.paused() || source.preLoad;
                }
            }
        }
    }
}
