package net.minecraft.client.audio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import io.netty.util.internal.ThreadLocalRandom;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class SoundManager
{
    private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
    private static final Logger logger = LogManager.getLogger();
    private final SoundHandler sndHandler;
    private final GameSettings options;
    private volatile OpenAlEngine engine;
    private volatile boolean loaded;
    private int playTime = 0;
    private int deviceCheckTimer;
    private String lastAudioDevice;
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
                        try
                        {
                            OpenAlEngine openAlEngine = new OpenAlEngine();
                            openAlEngine.create();
                            SoundManager.this.engine = openAlEngine;
                            SoundManager.this.loaded = true;
                            SoundManager.this.engine.setMasterVolume(SoundManager.this.options.getSoundLevel(SoundCategory.MASTER));
                            SoundManager.logger.info(SoundManager.LOG_MARKER, "Sound engine started");
                        }
                        catch (Throwable throwable)
                        {
                            SoundManager.logger.error(SoundManager.LOG_MARKER, "Error starting OpenAL. Turning off sounds & music", throwable);
                            SoundManager.this.options.setSoundLevel(SoundCategory.MASTER, 0.0F);
                            SoundManager.this.options.saveOptions();
                        }
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
                this.engine.setMasterVolume(volume);
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
                        this.engine.setVolume(channelName, categoryVolume);
                    }
                }
            }
        }
    }

    public synchronized void unloadSoundSystem()
    {
        if (this.loaded)
        {
            this.stopAllSounds();
            this.engine.destroy();
            this.engine = null;
            this.loaded = false;
            this.lastAudioDevice = null;
        }
    }

    public void stopAllSounds()
    {
        if (this.loaded)
        {
            this.engine.stopAll();
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
        if (!this.loaded)
        {
            return;
        }

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
                this.engine.setVolume(channelName, this.getNormalizedVolume(tickableSound, this.playingSoundPoolEntries.get(tickableSound), this.sndHandler.getSound(tickableSound.getSoundLocation()).getSoundCategory()));
                this.engine.setPitch(channelName, this.getNormalizedPitch(tickableSound, this.playingSoundPoolEntries.get(tickableSound)));
                this.engine.setPosition(channelName, tickableSound.getXPosF(), tickableSound.getYPosF(), tickableSound.getZPosF());
            }
        }

        Iterator<Entry<String, ISound>> playingSoundIterator = this.playingSounds.entrySet().iterator();

        while (playingSoundIterator.hasNext())
        {
            Entry<String, ISound> entry = playingSoundIterator.next();
            String channelName = entry.getKey();
            ISound sound = entry.getValue();

            if (!this.engine.playing(channelName))
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
                    this.engine.removeSource(channelName);
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

        this.checkAudioDevice();
    }

    private void checkAudioDevice()
    {
        if (!this.loaded)
        {
            return;
        }

        ++this.deviceCheckTimer;

        if (this.deviceCheckTimer < 40)
        {
            this.engine.processPendingReopen();
            return;
        }

        this.deviceCheckTimer = 0;
        String currentDevice = this.engine.getPlaybackDeviceFingerprint();

        if (currentDevice == null || currentDevice.length() == 0)
        {
            return;
        }

        if (this.lastAudioDevice == null)
        {
            this.lastAudioDevice = currentDevice;
            return;
        }

        if (!currentDevice.equals(this.lastAudioDevice))
        {
            this.lastAudioDevice = currentDevice;
            logger.info(LOG_MARKER, "Audio device changed to {}, reopening on sound thread", currentDevice);
            this.engine.requestReopenDefaultDevice();
            this.engine.setMasterVolume(this.engine.getMasterVolume());
        }

        this.engine.processPendingReopen();

        if (this.engine.consumeReopenFailure())
        {
            logger.info(LOG_MARKER, "Audio reopen failed, reloading sound engine");
            this.reloadSoundSystem();
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
            return channelName == null ? false : this.engine.playing(channelName) || stopTime != null && stopTime.intValue() <= this.playTime;
        }
    }

    public void stopSound(ISound sound)
    {
        if (this.loaded)
        {
            String channelName = this.invPlayingSounds.get(sound);

            if (channelName != null)
            {
                this.engine.stop(channelName);
            }
        }
    }

    public void playSound(ISound sound)
    {
        if (this.loaded)
        {
            if (this.engine.getMasterVolume() <= 0.0F)
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
                            boolean linear = sound.getAttenuationType() == ISound.AttenuationType.LINEAR;

                            try
                            {
                                if (!this.engine.newSource(channelName, soundLocation, repeatImmediately, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), linear, attenuationDistance))
                                {
                                    return;
                                }
                            }
                            catch (Throwable throwable)
                            {
                                logger.warn(LOG_MARKER, "Unable to play sound file: " + soundLocation, throwable);
                                return;
                            }

                            logger.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", new Object[] {soundEntry.getSoundPoolEntryLocation(), soundEvent.getSoundEventLocation(), channelName});
                            this.engine.setPitch(channelName, (float)normalizedPitch);
                            this.engine.setVolume(channelName, normalizedVolume);
                            this.engine.play(channelName);
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
        if (this.loaded)
        {
            for (String channelName : this.playingSounds.keySet())
            {
                logger.debug(LOG_MARKER, "Pausing channel {}", new Object[] {channelName});
                this.engine.pause(channelName);
            }
        }
    }

    public void resumeAllSounds()
    {
        if (this.loaded)
        {
            for (String channelName : this.playingSounds.keySet())
            {
                logger.debug(LOG_MARKER, "Resuming channel {}", new Object[] {channelName});
                this.engine.play(channelName);
            }
        }
    }

    public void playDelayedSound(ISound sound, int delay)
    {
        this.delayedSounds.put(sound, Integer.valueOf(this.playTime + delay));
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
            this.engine.setListenerPosition((float)xCoordinate, (float)yCoordinate, (float)zCoordinate);
            this.engine.setListenerOrientation(lookX, pitchSin, lookZ, upX, upPitchSin, upZ);
        }
    }
}
