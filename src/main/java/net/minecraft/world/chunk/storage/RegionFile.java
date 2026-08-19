package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.server.MinecraftServer;

public class RegionFile
{
    private static final byte[] emptySector = new byte[4096];
    private final File fileName;
    private RandomAccessFile dataFile;
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private List<Boolean> sectorFree;
    private int sizeDelta;
    private long lastModified;

    public RegionFile(File fileNameIn)
    {
        this.fileName = fileNameIn;
        this.sizeDelta = 0;

        try
        {
            if (fileNameIn.exists())
            {
                this.lastModified = fileNameIn.lastModified();
            }

            this.dataFile = new RandomAccessFile(fileNameIn, "rw");

            if (this.dataFile.length() < 4096L)
            {
                for (int i = 0; i < 1024; ++i)
                {
                    this.dataFile.writeInt(0);
                }

                for (int index2 = 0; index2 < 1024; ++index2)
                {
                    this.dataFile.writeInt(0);
                }

                this.sizeDelta += 8192;
            }

            if ((this.dataFile.length() & 4095L) != 0L)
            {
                for (int innerIndex = 0; (long)innerIndex < (this.dataFile.length() & 4095L); ++innerIndex)
                {
                    this.dataFile.write(0);
                }
            }

            int secondIntValue = (int)this.dataFile.length() / 4096;
            this.sectorFree = Lists.<Boolean>newArrayListWithCapacity(secondIntValue);

            for (int j = 0; j < secondIntValue; ++j)
            {
                this.sectorFree.add(Boolean.valueOf(true));
            }

            this.sectorFree.set(0, Boolean.valueOf(false));
            this.sectorFree.set(1, Boolean.valueOf(false));
            this.dataFile.seek(0L);

            for (int outerIndex = 0; outerIndex < 1024; ++outerIndex)
            {
                int k = this.dataFile.readInt();
                this.offsets[outerIndex] = k;

                if (k != 0 && (k >> 8) + (k & 255) <= this.sectorFree.size())
                {
                    for (int l = 0; l < (k & 255); ++l)
                    {
                        this.sectorFree.set((k >> 8) + l, Boolean.valueOf(false));
                    }
                }
            }

            for (int index3 = 0; index3 < 1024; ++index3)
            {
                int intValue = this.dataFile.readInt();
                this.chunkTimestamps[index3] = intValue;
            }
        }
        catch (IOException iOException)
        {
            net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
        }
    }

    public synchronized DataInputStream getChunkDataInputStream(int x, int z)
    {
        if (this.outOfBounds(x, z))
        {
            return null;
        }
        else
        {
            try
            {
                int i = this.getOffset(x, z);

                if (i == 0)
                {
                    return null;
                }
                else
                {
                    int j = i >> 8;
                    int k = i & 255;

                    if (j + k > this.sectorFree.size())
                    {
                        return null;
                    }
                    else
                    {
                        this.dataFile.seek((long)(j * 4096));
                        int l = this.dataFile.readInt();

                        if (l > 4096 * k)
                        {
                            return null;
                        }
                        else if (l <= 0)
                        {
                            return null;
                        }
                        else
                        {
                            byte byteValue = this.dataFile.readByte();

                            if (byteValue == 1)
                            {
                                byte[] abyte1 = new byte[l - 1];
                                this.dataFile.read(abyte1);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte1))));
                            }
                            else if (byteValue == 2)
                            {
                                byte[] abyte = new byte[l - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
                            }
                            else
                            {
                                return null;
                            }
                        }
                    }
                }
            }
            catch (IOException caughtIoException)
            {
                return null;
            }
        }
    }

    public DataOutputStream getChunkDataOutputStream(int x, int z)
    {
        return this.outOfBounds(x, z) ? null : new DataOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(x, z)));
    }

    protected synchronized void write(int x, int z, byte[] data, int length)
    {
        try
        {
            int i = this.getOffset(x, z);
            int j = i >> 8;
            int k = i & 255;
            int l = (length + 5) / 4096 + 1;

            if (l >= 256)
            {
                return;
            }

            if (j != 0 && k == l)
            {
                this.write(j, data, length);
            }
            else
            {
                for (int index = 0; index < k; ++index)
                {
                    this.sectorFree.set(j + index, Boolean.valueOf(true));
                }

                int thirdIntValue = this.sectorFree.indexOf(Boolean.valueOf(true));
                int secondIntValue2 = 0;

                if (thirdIntValue != -1)
                {
                    for (int nestedIndex = thirdIntValue; nestedIndex < this.sectorFree.size(); ++nestedIndex)
                    {
                        if (secondIntValue2 != 0)
                        {
                            if (((Boolean)this.sectorFree.get(nestedIndex)).booleanValue())
                            {
                                ++secondIntValue2;
                            }
                            else
                            {
                                secondIntValue2 = 0;
                            }
                        }
                        else if (((Boolean)this.sectorFree.get(nestedIndex)).booleanValue())
                        {
                            thirdIntValue = nestedIndex;
                            secondIntValue2 = 1;
                        }

                        if (secondIntValue2 >= l)
                        {
                            break;
                        }
                    }
                }

                if (secondIntValue2 >= l)
                {
                    j = thirdIntValue;
                    this.setOffset(x, z, thirdIntValue << 8 | l);

                    for (int index2 = 0; index2 < l; ++index2)
                    {
                        this.sectorFree.set(j + index2, Boolean.valueOf(false));
                    }

                    this.write(j, data, length);
                }
                else
                {
                    this.dataFile.seek(this.dataFile.length());
                    j = this.sectorFree.size();

                    for (int index3 = 0; index3 < l; ++index3)
                    {
                        this.dataFile.write(emptySector);
                        this.sectorFree.add(Boolean.valueOf(false));
                    }

                    this.sizeDelta += 4096 * l;
                    this.write(j, data, length);
                    this.setOffset(x, z, j << 8 | l);
                }
            }

            this.setChunkTimestamp(x, z, (int)(MinecraftServer.getCurrentTimeMillis() / 1000L));
        }
        catch (IOException iOException)
        {
            net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
        }
    }

    private void write(int sectorNumber, byte[] data, int length) throws IOException
    {
        this.dataFile.seek((long)(sectorNumber * 4096));
        this.dataFile.writeInt(length + 1);
        this.dataFile.writeByte(2);
        this.dataFile.write(data, 0, length);
    }

    private boolean outOfBounds(int x, int z)
    {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    private int getOffset(int x, int z)
    {
        return this.offsets[x + z * 32];
    }

    public boolean isChunkSaved(int x, int z)
    {
        return this.getOffset(x, z) != 0;
    }

    private void setOffset(int x, int z, int offset) throws IOException
    {
        this.offsets[x + z * 32] = offset;
        this.dataFile.seek((long)((x + z * 32) * 4));
        this.dataFile.writeInt(offset);
    }

    private void setChunkTimestamp(int x, int z, int timestamp) throws IOException
    {
        this.chunkTimestamps[x + z * 32] = timestamp;
        this.dataFile.seek((long)(4096 + (x + z * 32) * 4));
        this.dataFile.writeInt(timestamp);
    }

    public void close() throws IOException
    {
        if (this.dataFile != null)
        {
            this.dataFile.close();
        }
    }

    class ChunkBuffer extends ByteArrayOutputStream
    {
        private int chunkX;
        private int chunkZ;

        public ChunkBuffer(int x, int z)
        {
            super(8096);
            this.chunkX = x;
            this.chunkZ = z;
        }

        public void close() throws IOException
        {
            RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
        }
    }
}
