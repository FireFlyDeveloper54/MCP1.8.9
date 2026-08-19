package net.minecraft.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public class CompressedStreamTools
{
    public static NBTTagCompound readCompressed(InputStream is) throws IOException
    {
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
        NBTTagCompound nbttagcompound;

        try
        {
            nbttagcompound = read(datainputstream, NBTSizeTracker.INFINITE);
        }
        finally
        {
            datainputstream.close();
        }

        return nbttagcompound;
    }

    public static void writeCompressed(NBTTagCompound tagCompound, OutputStream outputStream) throws IOException
    {
        DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));

        try
        {
            write(tagCompound, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }
    }

    public static void safeWrite(NBTTagCompound tagCompound, File file) throws IOException
    {
        File file1 = new File(file.getAbsolutePath() + "_tmp");

        if (file1.exists())
        {
            file1.delete();
        }

        write(tagCompound, file1);

        if (file.exists())
        {
            file.delete();
        }

        if (file.exists())
        {
            throw new IOException("Failed to delete " + file);
        }
        else
        {
            file1.renameTo(file);
        }
    }

    public static void write(NBTTagCompound tagCompound, File file) throws IOException
    {
        DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));

        try
        {
            write(tagCompound, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }
    }

    public static NBTTagCompound read(File file) throws IOException
    {
        if (!file.exists())
        {
            return null;
        }
        else
        {
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
            NBTTagCompound nbttagcompound;

            try
            {
                nbttagcompound = read(datainputstream, NBTSizeTracker.INFINITE);
            }
            finally
            {
                datainputstream.close();
            }

            return nbttagcompound;
        }
    }

    public static NBTTagCompound read(DataInputStream inputStream) throws IOException
    {
        return read(inputStream, NBTSizeTracker.INFINITE);
    }

    public static NBTTagCompound read(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        NBTBase nbtbase = readTag(input, 0, sizeTracker);

        if (nbtbase instanceof NBTTagCompound)
        {
            return (NBTTagCompound)nbtbase;
        }
        else
        {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(NBTTagCompound tagCompound, DataOutput output) throws IOException
    {
        writeTag(tagCompound, output);
    }

    private static void writeTag(NBTBase tag, DataOutput output) throws IOException
    {
        output.writeByte(tag.getId());

        if (tag.getId() != 0)
        {
            output.writeUTF("");
            tag.write(output);
        }
    }

    private static NBTBase readTag(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        byte byteValue = input.readByte();

        if (byteValue == 0)
        {
            return new NBTTagEnd();
        }
        else
        {
            input.readUTF();
            NBTBase nbtbase = NBTBase.createNewByType(byteValue);

            try
            {
                nbtbase.read(input, depth, sizeTracker);
                return nbtbase;
            }
            catch (IOException ioexception)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
                crashreportcategory.addCrashSection("Tag name", "[UNNAMED TAG]");
                crashreportcategory.addCrashSection("Tag type", Byte.valueOf(byteValue));
                throw new ReportedException(crashreport);
            }
        }
    }
}
