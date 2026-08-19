package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundHandler implements IResourceManagerReloadListener, ITickable
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
    private static final ParameterizedType TYPE = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class, SoundList.class};
        }
        public Type getRawType()
        {
            return Map.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
    public static final SoundPoolEntry missing_sound = new SoundPoolEntry(new ResourceLocation("meta:missing_sound"), 0.0D, 0.0D, false);
    private final SoundRegistry sndRegistry = new SoundRegistry();
    private final SoundManager sndManager;
    private final IResourceManager mcResourceManager;
    private final Random random = new Random();

    public SoundHandler(IResourceManager manager, GameSettings gameSettingsIn)
    {
        this.mcResourceManager = manager;
        this.sndManager = new SoundManager(this, gameSettingsIn);
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.sndManager.reloadSoundSystem();
        this.sndRegistry.clearMap();

        for (String resourceDomain : resourceManager.getResourceDomains())
        {
            try
            {
                for (IResource resource : resourceManager.getAllResources(new ResourceLocation(resourceDomain, "sounds.json")))
                {
                    try
                    {
                        Map<String, SoundList> soundMap = this.getSoundMap(resource.getInputStream());

                        for (Entry<String, SoundList> entry : soundMap.entrySet())
                        {
                            this.loadSoundResource(new ResourceLocation(resourceDomain, (String)entry.getKey()), (SoundList)entry.getValue());
                        }
                    }
                    catch (RuntimeException runtimeException)
                    {
                        logger.warn((String)"Invalid sounds.json", (Throwable)runtimeException);
                    }
                }
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }
    }

    protected Map<String, SoundList> getSoundMap(InputStream stream)
    {
        Map map;

        try
        {
            map = (Map)GSON.fromJson((Reader)(new InputStreamReader(stream)), TYPE);
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }

        return map;
    }

    private void loadSoundResource(ResourceLocation location, SoundList sounds)
    {
        boolean isNewSoundEvent = !this.sndRegistry.containsKey(location);
        SoundEventAccessorComposite soundEventAccessorComposite;

        if (!isNewSoundEvent && !sounds.canReplaceExisting())
        {
            soundEventAccessorComposite = (SoundEventAccessorComposite)this.sndRegistry.getObject(location);
        }
        else
        {
            if (!isNewSoundEvent)
            {
                logger.debug("Replaced sound event location {}", new Object[] {location});
            }

            soundEventAccessorComposite = new SoundEventAccessorComposite(location, 1.0D, 1.0D, sounds.getSoundCategory());
            this.sndRegistry.registerSound(soundEventAccessorComposite);
        }

        for (final SoundList.SoundEntry soundEntry : sounds.getSoundList())
        {
            String soundName = soundEntry.getSoundEntryName();
            ResourceLocation resourceLocation = new ResourceLocation(soundName);
            final String soundDomain = soundName.contains(":") ? resourceLocation.getResourceDomain() : location.getResourceDomain();
            ISoundEventAccessor<SoundPoolEntry> soundEventAccessor;

            switch (soundEntry.getSoundEntryType())
            {
                case FILE:
                    ResourceLocation soundFileLocation = new ResourceLocation(soundDomain, "sounds/" + resourceLocation.getResourcePath() + ".ogg");
                    InputStream inputStream = null;

                    try
                    {
                        inputStream = this.mcResourceManager.getResource(soundFileLocation).getInputStream();
                    }
                    catch (FileNotFoundException caughtFileNotFoundException)
                    {
                        logger.warn("File {} does not exist, cannot add it to event {}", new Object[] {soundFileLocation, location});
                        continue;
                    }
                    catch (IOException iOException)
                    {
                        logger.warn((String)("Could not load sound file " + soundFileLocation + ", cannot add it to event " + location), (Throwable)iOException);
                        continue;
                    }
                    finally
                    {
                        IOUtils.closeQuietly(inputStream);
                    }

                    soundEventAccessor = new SoundEventAccessor(new SoundPoolEntry(soundFileLocation, (double)soundEntry.getSoundEntryPitch(), (double)soundEntry.getSoundEntryVolume(), soundEntry.isStreaming()), soundEntry.getSoundEntryWeight());
                    break;

                case SOUND_EVENT:
                    soundEventAccessor = new ISoundEventAccessor<SoundPoolEntry>()
                    {
                        final ResourceLocation soundEventLocation = new ResourceLocation(soundDomain, soundEntry.getSoundEntryName());
                        public int getWeight()
                        {
                            SoundEventAccessorComposite linkedSoundEvent = (SoundEventAccessorComposite)SoundHandler.this.sndRegistry.getObject(this.soundEventLocation);
                            return linkedSoundEvent == null ? 0 : linkedSoundEvent.getWeight();
                        }
                        public SoundPoolEntry cloneEntry()
                        {
                            SoundEventAccessorComposite linkedSoundEvent = (SoundEventAccessorComposite)SoundHandler.this.sndRegistry.getObject(this.soundEventLocation);
                            return linkedSoundEvent == null ? SoundHandler.missing_sound : linkedSoundEvent.cloneEntry();
                        }
                    };

                    break;
                default:
                    throw new IllegalStateException("IN YOU FACE");
            }

            soundEventAccessorComposite.addSoundToEventPool(soundEventAccessor);
        }
    }

    public SoundEventAccessorComposite getSound(ResourceLocation location)
    {
        return (SoundEventAccessorComposite)this.sndRegistry.getObject(location);
    }

    public void playSound(ISound sound)
    {
        this.sndManager.playSound(sound);
    }

    public void playDelayedSound(ISound sound, int delay)
    {
        this.sndManager.playDelayedSound(sound, delay);
    }

    public void setListener(EntityPlayer player, float partialTicks)
    {
        this.sndManager.setListener(player, partialTicks);
    }

    public void pauseSounds()
    {
        this.sndManager.pauseAllSounds();
    }

    public void stopSounds()
    {
        this.sndManager.stopAllSounds();
    }

    public void unloadSounds()
    {
        this.sndManager.unloadSoundSystem();
    }

    public void update()
    {
        this.sndManager.updateAllSounds();
    }

    public void resumeSounds()
    {
        this.sndManager.resumeAllSounds();
    }

    public void setSoundLevel(SoundCategory category, float volume)
    {
        if (category == SoundCategory.MASTER && volume <= 0.0F)
        {
            this.stopSounds();
        }

        this.sndManager.setSoundCategoryVolume(category, volume);
    }

    public void stopSound(ISound sound)
    {
        this.sndManager.stopSound(sound);
    }

    public SoundEventAccessorComposite getRandomSoundFromCategories(SoundCategory... categories)
    {
        List<SoundEventAccessorComposite> matchingSounds = Lists.<SoundEventAccessorComposite>newArrayList();

        for (ResourceLocation resourceLocation : this.sndRegistry.getKeys())
        {
            SoundEventAccessorComposite soundEventAccessorComposite = this.sndRegistry.getObject(resourceLocation);

            if (ArrayUtils.contains(categories, soundEventAccessorComposite.getSoundCategory()))
            {
                matchingSounds.add(soundEventAccessorComposite);
            }
        }

        if (matchingSounds.isEmpty())
        {
            return null;
        }
        else
        {
            return matchingSounds.get(this.random.nextInt(matchingSounds.size()));
        }
    }

    public boolean isSoundPlaying(ISound sound)
    {
        return this.sndManager.isSoundPlaying(sound);
    }
}
