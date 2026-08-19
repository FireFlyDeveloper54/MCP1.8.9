package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.util.ResourceLocation;

public class SoundEventAccessorComposite implements ISoundEventAccessor<SoundPoolEntry>
{
    private final List<ISoundEventAccessor<SoundPoolEntry>> soundPool = Lists.<ISoundEventAccessor<SoundPoolEntry>>newArrayList();
    private final Random rnd = new Random();
    private final ResourceLocation soundLocation;
    private final SoundCategory category;
    private double eventPitch;
    private double eventVolume;

    public SoundEventAccessorComposite(ResourceLocation soundLocation, double pitch, double volume, SoundCategory category)
    {
        this.soundLocation = soundLocation;
        this.eventVolume = volume;
        this.eventPitch = pitch;
        this.category = category;
    }

    public int getWeight()
    {
        int totalWeight = 0;

        for (ISoundEventAccessor<SoundPoolEntry> soundEventAccessor : this.soundPool)
        {
            totalWeight += soundEventAccessor.getWeight();
        }

        return totalWeight;
    }

    public SoundPoolEntry cloneEntry()
    {
        int totalWeight = this.getWeight();

        if (!this.soundPool.isEmpty() && totalWeight != 0)
        {
            int randomWeight = this.rnd.nextInt(totalWeight);

            for (ISoundEventAccessor<SoundPoolEntry> soundEventAccessor : this.soundPool)
            {
                randomWeight -= soundEventAccessor.getWeight();

                if (randomWeight < 0)
                {
                    SoundPoolEntry soundPoolEntry = (SoundPoolEntry)soundEventAccessor.cloneEntry();
                    soundPoolEntry.setPitch(soundPoolEntry.getPitch() * this.eventPitch);
                    soundPoolEntry.setVolume(soundPoolEntry.getVolume() * this.eventVolume);
                    return soundPoolEntry;
                }
            }

            return SoundHandler.missing_sound;
        }
        else
        {
            return SoundHandler.missing_sound;
        }
    }

    public void addSoundToEventPool(ISoundEventAccessor<SoundPoolEntry> sound)
    {
        this.soundPool.add(sound);
    }

    public ResourceLocation getSoundEventLocation()
    {
        return this.soundLocation;
    }

    public SoundCategory getSoundCategory()
    {
        return this.category;
    }
}
