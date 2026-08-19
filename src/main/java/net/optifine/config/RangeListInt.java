package net.optifine.config;

import net.minecraft.src.Config;

public class RangeListInt
{
    private RangeInt[] ranges = new RangeInt[0];

    public RangeListInt()
    {
    }

    public RangeListInt(RangeInt ri)
    {
        this.addRange(ri);
    }

    public void addRange(RangeInt ri)
    {
        this.ranges = (RangeInt[])((RangeInt[])Config.addObjectToArray(this.ranges, ri));
    }

    public boolean isInRange(int val)
    {
        for (int rangeIndex = 0; rangeIndex < this.ranges.length; ++rangeIndex)
        {
            RangeInt rangeInt = this.ranges[rangeIndex];

            if (rangeInt.isInRange(val))
            {
                return true;
            }
        }

        return false;
    }

    public int getCountRanges()
    {
        return this.ranges.length;
    }

    public RangeInt getRange(int rangeIndex)
    {
        return this.ranges[rangeIndex];
    }

    public String toString()
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");

        for (int rangeIndex = 0; rangeIndex < this.ranges.length; ++rangeIndex)
        {
            RangeInt rangeInt = this.ranges[rangeIndex];

            if (rangeIndex > 0)
            {
                stringBuffer.append(", ");
            }

            stringBuffer.append(rangeInt.toString());
        }

        stringBuffer.append("]");
        return stringBuffer.toString();
    }
}
