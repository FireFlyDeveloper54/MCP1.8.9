package net.minecraft.stats;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;

public class StatFileWriter
{
    protected final Map<StatBase, TupleIntJsonSerializable> statsData = Maps.<StatBase, TupleIntJsonSerializable>newConcurrentMap();

    public boolean hasAchievementUnlocked(Achievement achievementIn)
    {
        return this.readStat(achievementIn) > 0;
    }

    public boolean canUnlockAchievement(Achievement achievementIn)
    {
        return achievementIn.parentAchievement == null || this.hasAchievementUnlocked(achievementIn.parentAchievement);
    }

    public int getAchievementUnlockDepth(Achievement targetAchievement)
    {
        if (this.hasAchievementUnlocked(targetAchievement))
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (Achievement parentAchievement = targetAchievement.parentAchievement; parentAchievement != null && !this.hasAchievementUnlocked(parentAchievement); ++i)
            {
                parentAchievement = parentAchievement.parentAchievement;
            }

            return i;
        }
    }

    public void increaseStat(EntityPlayer player, StatBase stat, int amount)
    {
        if (!stat.isAchievement() || this.canUnlockAchievement((Achievement)stat))
        {
            this.unlockAchievement(player, stat, this.readStat(stat) + amount);
        }
    }

    public void unlockAchievement(EntityPlayer playerIn, StatBase statIn, int amount)
    {
        TupleIntJsonSerializable tupleIntJsonSerializable = (TupleIntJsonSerializable)this.statsData.get(statIn);

        if (tupleIntJsonSerializable == null)
        {
            tupleIntJsonSerializable = new TupleIntJsonSerializable();
            this.statsData.put(statIn, tupleIntJsonSerializable);
        }

        tupleIntJsonSerializable.setIntegerValue(amount);
    }

    public int readStat(StatBase stat)
    {
        TupleIntJsonSerializable tupleIntJsonSerializable = (TupleIntJsonSerializable)this.statsData.get(stat);
        return tupleIntJsonSerializable == null ? 0 : tupleIntJsonSerializable.getIntegerValue();
    }

    public <T extends IJsonSerializable> T getSerializableValue(StatBase stat)
    {
        TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(stat);
        return (T)(tupleintjsonserializable != null ? tupleintjsonserializable.getJsonSerializableValue() : null);
    }

    public <T extends IJsonSerializable> T setSerializableValue(StatBase stat, T value)
    {
        TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(stat);

        if (tupleintjsonserializable == null)
        {
            tupleintjsonserializable = new TupleIntJsonSerializable();
            this.statsData.put(stat, tupleintjsonserializable);
        }

        tupleintjsonserializable.setJsonSerializableValue(value);
        return (T)value;
    }
}
