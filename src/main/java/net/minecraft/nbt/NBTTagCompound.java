package net.minecraft.nbt;

import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public class NBTTagCompound extends NBTBase
{
    private Map<String, NBTBase> tagMap = Maps.<String, NBTBase>newHashMap();

    void write(DataOutput output) throws IOException
    {
        for (String s : this.tagMap.keySet())
        {
            NBTBase nbtbase = (NBTBase)this.tagMap.get(s);
            writeEntry(s, nbtbase, output);
        }

        output.writeByte(0);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.read(384L);

        if (depth > 512)
        {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        else
        {
            this.tagMap.clear();
            byte byteValue;

            while ((byteValue = readType(input, sizeTracker)) != 0)
            {
                String s = readKey(input, sizeTracker);
                sizeTracker.read((long)(224 + 16 * s.length()));
                NBTBase nbtbase = readNBT(byteValue, s, input, depth + 1, sizeTracker);

                if (this.tagMap.put(s, nbtbase) != null)
                {
                    sizeTracker.read(288L);
                }
            }
        }
    }

    public Set<String> getKeySet()
    {
        return this.tagMap.keySet();
    }

    public byte getId()
    {
        return (byte)10;
    }

    public void setTag(String key, NBTBase value)
    {
        this.tagMap.put(key, value);
    }

    public void setByte(String key, byte value)
    {
        this.tagMap.put(key, new NBTTagByte(value));
    }

    public void setShort(String key, short value)
    {
        this.tagMap.put(key, new NBTTagShort(value));
    }

    public void setInteger(String key, int value)
    {
        this.tagMap.put(key, new NBTTagInt(value));
    }

    public void setLong(String key, long value)
    {
        this.tagMap.put(key, new NBTTagLong(value));
    }

    public void setFloat(String key, float value)
    {
        this.tagMap.put(key, new NBTTagFloat(value));
    }

    public void setDouble(String key, double value)
    {
        this.tagMap.put(key, new NBTTagDouble(value));
    }

    public void setString(String key, String value)
    {
        this.tagMap.put(key, new NBTTagString(value));
    }

    public void setByteArray(String key, byte[] value)
    {
        this.tagMap.put(key, new NBTTagByteArray(value));
    }

    public void setIntArray(String key, int[] value)
    {
        this.tagMap.put(key, new NBTTagIntArray(value));
    }

    public void setBoolean(String key, boolean value)
    {
        this.setByte(key, (byte)(value ? 1 : 0));
    }

    public NBTBase getTag(String key)
    {
        return (NBTBase)this.tagMap.get(key);
    }

    public byte getTagId(String key)
    {
        NBTBase nBTBase = (NBTBase)this.tagMap.get(key);
        return nBTBase != null ? nBTBase.getId() : 0;
    }

    public boolean hasKey(String key)
    {
        return this.tagMap.containsKey(key);
    }

    public boolean hasKey(String key, int type)
    {
        int i = this.getTagId(key);

        if (i == type)
        {
            return true;
        }
        else if (type != 99)
        {
            if (i > 0)
            {
                ;
            }

            return false;
        }
        else
        {
            return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
        }
    }

    public byte getByte(String key)
    {
        try
        {
            return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getByte();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return (byte)0;
        }
    }

    public short getShort(String key)
    {
        try
        {
            return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getShort();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return (short)0;
        }
    }

    public int getInteger(String key)
    {
        try
        {
            return !this.hasKey(key, 99) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getInt();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return 0;
        }
    }

    public long getLong(String key)
    {
        try
        {
            return !this.hasKey(key, 99) ? 0L : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getLong();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return 0L;
        }
    }

    public float getFloat(String key)
    {
        try
        {
            return !this.hasKey(key, 99) ? 0.0F : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getFloat();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return 0.0F;
        }
    }

    public double getDouble(String key)
    {
        try
        {
            return !this.hasKey(key, 99) ? 0.0D : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).getDouble();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return 0.0D;
        }
    }

    public String getString(String key)
    {
        try
        {
            return !this.hasKey(key, 8) ? "" : ((NBTBase)this.tagMap.get(key)).getString();
        }
        catch (ClassCastException caughtClassCastException)
        {
            return "";
        }
    }

    public byte[] getByteArray(String key)
    {
        try
        {
            return !this.hasKey(key, 7) ? new byte[0] : ((NBTTagByteArray)this.tagMap.get(key)).getByteArray();
        }
        catch (ClassCastException classCastException)
        {
            throw new ReportedException(this.createCrashReport(key, 7, classCastException));
        }
    }

    public int[] getIntArray(String key)
    {
        try
        {
            return !this.hasKey(key, 11) ? new int[0] : ((NBTTagIntArray)this.tagMap.get(key)).getIntArray();
        }
        catch (ClassCastException classCastException)
        {
            throw new ReportedException(this.createCrashReport(key, 11, classCastException));
        }
    }

    public NBTTagCompound getCompoundTag(String key)
    {
        try
        {
            return !this.hasKey(key, 10) ? new NBTTagCompound() : (NBTTagCompound)this.tagMap.get(key);
        }
        catch (ClassCastException classCastException)
        {
            throw new ReportedException(this.createCrashReport(key, 10, classCastException));
        }
    }

    public NBTTagList getTagList(String key, int type)
    {
        try
        {
            if (this.getTagId(key) != 9)
            {
                return new NBTTagList();
            }
            else
            {
                NBTTagList nBTTagList = (NBTTagList)this.tagMap.get(key);
                return nBTTagList.tagCount() > 0 && nBTTagList.getTagType() != type ? new NBTTagList() : nBTTagList;
            }
        }
        catch (ClassCastException classCastException)
        {
            throw new ReportedException(this.createCrashReport(key, 9, classCastException));
        }
    }

    public boolean getBoolean(String key)
    {
        return this.getByte(key) != 0;
    }

    public void removeTag(String key)
    {
        this.tagMap.remove(key);
    }

    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder("{");

        for (Entry<String, NBTBase> entry : this.tagMap.entrySet())
        {
            if (stringBuilder.length() != 1)
            {
                stringBuilder.append(',');
            }

            stringBuilder.append((String)entry.getKey()).append(':').append(entry.getValue());
        }

        return stringBuilder.append('}').toString();
    }

    public boolean hasNoTags()
    {
        return this.tagMap.isEmpty();
    }

    private CrashReport createCrashReport(final String key, final int expectedType, ClassCastException ex)
    {
        CrashReport crashReport = CrashReport.makeCrashReport(ex, "Reading NBT data");
        CrashReportCategory crashReportCategory = crashReport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashReportCategory.addCrashSectionCallable("Tag type found", new Callable<String>()
        {
            public String call() throws Exception
            {
                return NBTBase.NBT_TYPES[((NBTBase)NBTTagCompound.this.tagMap.get(key)).getId()];
            }
        });
        crashReportCategory.addCrashSectionCallable("Tag type expected", new Callable<String>()
        {
            public String call() throws Exception
            {
                return NBTBase.NBT_TYPES[expectedType];
            }
        });
        crashReportCategory.addCrashSection("Tag name", key);
        return crashReport;
    }

    public NBTBase copy()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();

        for (String s : this.tagMap.keySet())
        {
            nBTTagCompound.setTag(s, ((NBTBase)this.tagMap.get(s)).copy());
        }

        return nBTTagCompound;
    }

    public boolean equals(Object other)
    {
        if (super.equals(other))
        {
            NBTTagCompound nBTTagCompound = (NBTTagCompound)other;
            return this.tagMap.entrySet().equals(nBTTagCompound.tagMap.entrySet());
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ this.tagMap.hashCode();
    }

    private static void writeEntry(String name, NBTBase data, DataOutput output) throws IOException
    {
        output.writeByte(data.getId());

        if (data.getId() != 0)
        {
            output.writeUTF(name);
            data.write(output);
        }
    }

    private static byte readType(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        return input.readByte();
    }

    private static String readKey(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        return input.readUTF();
    }

    static NBTBase readNBT(byte id, String key, DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        NBTBase nbtbase = NBTBase.createNewByType(id);

        try
        {
            nbtbase.read(input, depth, sizeTracker);
            return nbtbase;
        }
        catch (IOException ioexception)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag name", key);
            crashreportcategory.addCrashSection("Tag type", Byte.valueOf(id));
            throw new ReportedException(crashreport);
        }
    }

    public void merge(NBTTagCompound other)
    {
        for (String s : other.tagMap.keySet())
        {
            NBTBase nBTBase = (NBTBase)other.tagMap.get(s);

            if (nBTBase.getId() == 10)
            {
                if (this.hasKey(s, 10))
                {
                    NBTTagCompound nBTTagCompound = this.getCompoundTag(s);
                    nBTTagCompound.merge((NBTTagCompound)nBTBase);
                }
                else
                {
                    this.setTag(s, nBTBase.copy());
                }
            }
            else
            {
                this.setTag(s, nBTBase.copy());
            }
        }
    }
}
