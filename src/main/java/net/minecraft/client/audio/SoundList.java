package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

public class SoundList
{
    private final List<SoundList.SoundEntry> soundList = Lists.<SoundList.SoundEntry>newArrayList();
    private boolean replaceExisting;
    private SoundCategory category;

    public List<SoundList.SoundEntry> getSoundList()
    {
        return this.soundList;
    }

    public boolean canReplaceExisting()
    {
        return this.replaceExisting;
    }

    public void setReplaceExisting(boolean replaceExistingIn)
    {
        this.replaceExisting = replaceExistingIn;
    }

    public SoundCategory getSoundCategory()
    {
        return this.category;
    }

    public void setSoundCategory(SoundCategory soundCat)
    {
        this.category = soundCat;
    }

    public static class SoundEntry
    {
        private String name;
        private float volume = 1.0F;
        private float pitch = 1.0F;
        private int weight = 1;
        private SoundList.SoundEntry.Type type = SoundList.SoundEntry.Type.FILE;
        private boolean streaming = false;

        public String getSoundEntryName()
        {
            return this.name;
        }

        public void setSoundEntryName(String nameIn)
        {
            this.name = nameIn;
        }

        public float getSoundEntryVolume()
        {
            return this.volume;
        }

        public void setSoundEntryVolume(float volumeIn)
        {
            this.volume = volumeIn;
        }

        public float getSoundEntryPitch()
        {
            return this.pitch;
        }

        public void setSoundEntryPitch(float pitchIn)
        {
            this.pitch = pitchIn;
        }

        public int getSoundEntryWeight()
        {
            return this.weight;
        }

        public void setSoundEntryWeight(int weightIn)
        {
            this.weight = weightIn;
        }

        public SoundList.SoundEntry.Type getSoundEntryType()
        {
            return this.type;
        }

        public void setSoundEntryType(SoundList.SoundEntry.Type typeIn)
        {
            this.type = typeIn;
        }

        public boolean isStreaming()
        {
            return this.streaming;
        }

        public void setStreaming(boolean isStreaming)
        {
            this.streaming = isStreaming;
        }

        public static enum Type
        {
            FILE("file"),
            SOUND_EVENT("event");

            private static final SoundList.SoundEntry.Type[] VALUES = values();
            private static final Map<String, SoundList.SoundEntry.Type> NAME_LOOKUP = Maps.<String, SoundList.SoundEntry.Type>newHashMap();
            private final String typeName;

            private Type(String typeNameIn)
            {
                this.typeName = typeNameIn;
            }

            public static SoundList.SoundEntry.Type getType(String typeName)
            {
                return NAME_LOOKUP.get(typeName);
            }

            static {
                for (SoundList.SoundEntry.Type type : VALUES)
                {
                    NAME_LOOKUP.put(type.typeName, type);
                }
            }
        }
    }
}
