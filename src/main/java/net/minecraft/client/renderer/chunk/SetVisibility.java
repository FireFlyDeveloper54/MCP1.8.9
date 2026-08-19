package net.minecraft.client.renderer.chunk;

import java.util.Locale;
import java.util.Set;
import net.minecraft.util.EnumFacing;

public class SetVisibility
{
    private static final int COUNT_FACES = EnumFacing.VALUES.length;
    private long bits;

    public void setManyVisible(Set<EnumFacing> facings)
    {
        for (EnumFacing facing : facings)
        {
            for (EnumFacing otherFacing : facings)
            {
                this.setVisible(facing, otherFacing, true);
            }
        }
    }

    public void setVisible(EnumFacing facing, EnumFacing facing2, boolean visible)
    {
        this.setBit(facing.ordinal() + facing2.ordinal() * COUNT_FACES, visible);
        this.setBit(facing2.ordinal() + facing.ordinal() * COUNT_FACES, visible);
    }

    public void setAllVisible(boolean visible)
    {
        if (visible)
        {
            this.bits = -1L;
        }
        else
        {
            this.bits = 0L;
        }
    }

    public boolean isVisible(EnumFacing facing, EnumFacing facing2)
    {
        return this.getBit(facing.ordinal() + facing2.ordinal() * COUNT_FACES);
    }

    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(' ');

        for (EnumFacing headerFacing : EnumFacing.VALUES)
        {
            stringBuilder.append(' ').append(headerFacing.toString().toUpperCase(Locale.ROOT).charAt(0));
        }

        stringBuilder.append('\n');

        for (EnumFacing rowFacing : EnumFacing.VALUES)
        {
            stringBuilder.append(rowFacing.toString().toUpperCase(Locale.ROOT).charAt(0));

            for (EnumFacing columnFacing : EnumFacing.VALUES)
            {
                if (rowFacing == columnFacing)
                {
                    stringBuilder.append("  ");
                }
                else
                {
                    boolean visible = this.isVisible(rowFacing, columnFacing);
                    stringBuilder.append(' ').append((char)(visible ? 'Y' : 'n'));
                }
            }

            stringBuilder.append('\n');
        }

        return stringBuilder.toString();
    }

    private boolean getBit(int bitIndex)
    {
        return (this.bits & (1L << bitIndex)) != 0L;
    }

    private void setBit(int bitIndex, boolean value)
    {
        if (value)
        {
            this.setBit(bitIndex);
        }
        else
        {
            this.clearBit(bitIndex);
        }
    }

    private void setBit(int bitIndex)
    {
        this.bits |= 1L << bitIndex;
    }

    private void clearBit(int bitIndex)
    {
        this.bits &= ~(1L << bitIndex);
    }
}
