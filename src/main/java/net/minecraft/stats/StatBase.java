package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.event.HoverEvent;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IJsonSerializable;

public class StatBase
{
    public final String statId;
    private final IChatComponent statName;
    public boolean isIndependent;
    private final IStatType type;
    private final IScoreObjectiveCriteria objectiveCriteria;
    private Class <? extends IJsonSerializable > serializableClazz;
    private static NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
    public static IStatType simpleStatType = new IStatType()
    {
        public String format(int number)
        {
            return StatBase.numberFormat.format((long)number);
        }
    };
    private static DecimalFormat decimalFormat = new DecimalFormat("########0.00");
    public static IStatType timeStatType = new IStatType()
    {
        public String format(int number)
        {
            double doubleValue = (double)number / 20.0D;
            double doubleValue2 = doubleValue / 60.0D;
            double doubleValue3 = doubleValue2 / 60.0D;
            double doubleValue4 = doubleValue3 / 24.0D;
            double doubleValue5 = doubleValue4 / 365.0D;
            return doubleValue5 > 0.5D ? StatBase.decimalFormat.format(doubleValue5) + " y" : (doubleValue4 > 0.5D ? StatBase.decimalFormat.format(doubleValue4) + " d" : (doubleValue3 > 0.5D ? StatBase.decimalFormat.format(doubleValue3) + " h" : (doubleValue2 > 0.5D ? StatBase.decimalFormat.format(doubleValue2) + " m" : doubleValue + " s")));
        }
    };
    public static IStatType distanceStatType = new IStatType()
    {
        public String format(int number)
        {
            double doubleValue = (double)number / 100.0D;
            double doubleValue2 = doubleValue / 1000.0D;
            return doubleValue2 > 0.5D ? StatBase.decimalFormat.format(doubleValue2) + " km" : (doubleValue > 0.5D ? StatBase.decimalFormat.format(doubleValue) + " m" : number + " cm");
        }
    };
    public static IStatType divideByTen = new IStatType()
    {
        public String format(int number)
        {
            return StatBase.decimalFormat.format((double)number * 0.1D);
        }
    };

    public StatBase(String statIdIn, IChatComponent statNameIn, IStatType typeIn)
    {
        this.statId = statIdIn;
        this.statName = statNameIn;
        this.type = typeIn;
        this.objectiveCriteria = new ObjectiveStat(this);
        IScoreObjectiveCriteria.INSTANCES.put(this.objectiveCriteria.getName(), this.objectiveCriteria);
    }

    public StatBase(String statIdIn, IChatComponent statNameIn)
    {
        this(statIdIn, statNameIn, simpleStatType);
    }

    public StatBase initIndependentStat()
    {
        this.isIndependent = true;
        return this;
    }

    public StatBase registerStat()
    {
        if (StatList.oneShotStats.containsKey(this.statId))
        {
            throw new RuntimeException("Duplicate stat id: \"" + ((StatBase)StatList.oneShotStats.get(this.statId)).statName + "\" and \"" + this.statName + "\" at id " + this.statId);
        }
        else
        {
            StatList.allStats.add(this);
            StatList.oneShotStats.put(this.statId, this);
            return this;
        }
    }

    public boolean isAchievement()
    {
        return false;
    }

    public String format(int value)
    {
        return this.type.format(value);
    }

    public IChatComponent getStatName()
    {
        IChatComponent ichatcomponent = this.statName.createCopy();
        ichatcomponent.getChatStyle().setColor(EnumChatFormatting.GRAY);
        ichatcomponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new ChatComponentText(this.statId)));
        return ichatcomponent;
    }

    public IChatComponent createChatComponent()
    {
        IChatComponent ichatcomponent = this.getStatName();
        IChatComponent ichatcomponent1 = (new ChatComponentText("[")).appendSibling(ichatcomponent).appendText("]");
        ichatcomponent1.setChatStyle(ichatcomponent.getChatStyle());
        return ichatcomponent1;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other != null && this.getClass() == other.getClass())
        {
            StatBase statBase = (StatBase)other;
            return this.statId.equals(statBase.statId);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.statId.hashCode();
    }

    public String toString()
    {
        return "Stat{id=" + this.statId + ", nameId=" + this.statName + ", awardLocallyOnly=" + this.isIndependent + ", formatter=" + this.type + ", objectiveCriteria=" + this.objectiveCriteria + '}';
    }

    public IScoreObjectiveCriteria getCriteria()
    {
        return this.objectiveCriteria;
    }

    public Class <? extends IJsonSerializable > getSerializableClass()
    {
        return this.serializableClazz;
    }

    public StatBase setSerializableClass(Class <? extends IJsonSerializable > serializableClass)
    {
        this.serializableClazz = serializableClass;
        return this;
    }
}
