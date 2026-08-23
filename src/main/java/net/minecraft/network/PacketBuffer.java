package net.minecraft.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.util.ByteProcessor;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

public class PacketBuffer extends ByteBuf
{
    private final ByteBuf buf;

    public PacketBuffer(ByteBuf wrapped)
    {
        this.buf = wrapped;
    }

    public static int getVarIntSize(int input)
    {
        for (int size = 1; size < 5; ++size)
        {
            if ((input & -1 << size * 7) == 0)
            {
                return size;
            }
        }

        return 5;
    }

    public void writeByteArray(byte[] array)
    {
        this.writeVarIntToBuffer(array.length);
        this.writeBytes(array);
    }

    public byte[] readByteArray()
    {
        byte[] array = new byte[this.readVarIntFromBuffer()];
        this.readBytes(array);
        return array;
    }

    public BlockPos readBlockPos()
    {
        return BlockPos.fromLong(this.readLong());
    }

    public void writeBlockPos(BlockPos pos)
    {
        this.writeLong(pos.toLong());
    }

    public IChatComponent readChatComponent() throws IOException
    {
        return IChatComponent.Serializer.jsonToComponent(this.readStringFromBuffer(32767));
    }

    public void writeChatComponent(IChatComponent component) throws IOException
    {
        this.writeString(IChatComponent.Serializer.componentToJson(component));
    }

    public <T extends Enum<T>> T readEnumValue(Class<T> enumClass)
    {
        return (T)((Enum[])enumClass.getEnumConstants())[this.readVarIntFromBuffer()];
    }

    public void writeEnumValue(Enum<?> value)
    {
        this.writeVarIntToBuffer(value.ordinal());
    }

    public int readVarIntFromBuffer()
    {
        int value = 0;
        int bytesRead = 0;

        while (true)
        {
            byte currentByte = this.readByte();
            value |= (currentByte & 127) << bytesRead++ * 7;

            if (bytesRead > 5)
            {
                throw new RuntimeException("VarInt too big");
            }

            if ((currentByte & 128) != 128)
            {
                break;
            }
        }

        return value;
    }

    public long readVarLong()
    {
        long value = 0L;
        int bytesRead = 0;

        while (true)
        {
            byte currentByte = this.readByte();
            value |= (long)(currentByte & 127) << bytesRead++ * 7;

            if (bytesRead > 10)
            {
                throw new RuntimeException("VarLong too big");
            }

            if ((currentByte & 128) != 128)
            {
                break;
            }
        }

        return value;
    }

    public void writeUuid(UUID uUID)
    {
        this.writeLong(uUID.getMostSignificantBits());
        this.writeLong(uUID.getLeastSignificantBits());
    }

    public UUID readUuid()
    {
        return new UUID(this.readLong(), this.readLong());
    }

    public void writeVarIntToBuffer(int input)
    {
        while ((input & -128) != 0)
        {
            this.writeByte(input & 127 | 128);
            input >>>= 7;
        }

        this.writeByte(input);
    }

    public void writeVarLong(long value)
    {
        while ((value & -128L) != 0L)
        {
            this.writeByte((int)(value & 127L) | 128);
            value >>>= 7;
        }

        this.writeByte((int)value);
    }

    public void writeNBTTagCompoundToBuffer(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            this.writeByte(0);
        }
        else
        {
            try
            {
                CompressedStreamTools.write(nbt, new ByteBufOutputStream(this));
            }
            catch (IOException iOException)
            {
                throw new EncoderException(iOException);
            }
        }
    }

    public NBTTagCompound readNBTTagCompoundFromBuffer() throws IOException
    {
        int readerIndex = this.readerIndex();
        byte firstByte = this.readByte();

        if (firstByte == 0)
        {
            return null;
        }
        else
        {
            this.readerIndex(readerIndex);
            return CompressedStreamTools.read(new ByteBufInputStream(this), new NBTSizeTracker(2097152L));
        }
    }

    public void writeItemStackToBuffer(ItemStack stack)
    {
        if (stack == null)
        {
            this.writeShort(-1);
        }
        else
        {
            this.writeShort(Item.getIdFromItem(stack.getItem()));
            this.writeByte(stack.stackSize);
            this.writeShort(stack.getMetadata());
            NBTTagCompound tag = null;

            if (stack.getItem().isDamageable() || stack.getItem().getShareTag())
            {
                tag = stack.getTagCompound();
            }

            this.writeNBTTagCompoundToBuffer(tag);
        }
    }

    public ItemStack readItemStackFromBuffer() throws IOException
    {
        ItemStack stack = null;
        int itemId = this.readShort();

        if (itemId >= 0)
        {
            int stackSize = this.readByte();
            int metadata = this.readShort();
            stack = new ItemStack(Item.getItemById(itemId), stackSize, metadata);
            stack.setTagCompound(this.readNBTTagCompoundFromBuffer());
        }

        return stack;
    }

    public String readStringFromBuffer(int maxLength)
    {
        int byteLength = this.readVarIntFromBuffer();

        if (byteLength > maxLength * 4)
        {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + byteLength + " > " + maxLength * 4 + ")");
        }
        else if (byteLength < 0)
        {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        else
        {
            String value = this.readSlice(byteLength).toString(Charsets.UTF_8);

            if (value.length() > maxLength)
            {
                throw new DecoderException("The received string length is longer than maximum allowed (" + value.length() + " > " + maxLength + ")");
            }
            else
            {
                return value;
            }
        }
    }

    public PacketBuffer writeString(String string)
    {
        byte[] bytes = string.getBytes(Charsets.UTF_8);

        if (bytes.length > 32767)
        {
            throw new EncoderException("String too big (was " + bytes.length + " bytes encoded, max " + 32767 + ")");
        }
        else
        {
            this.writeVarIntToBuffer(bytes.length);
            this.writeBytes(bytes);
            return this;
        }
    }

    public int capacity()
    {
        return this.buf.capacity();
    }

    public ByteBuf capacity(int newCapacity)
    {
        return this.buf.capacity(newCapacity);
    }

    public int maxCapacity()
    {
        return this.buf.maxCapacity();
    }

    public ByteBufAllocator alloc()
    {
        return this.buf.alloc();
    }

    public ByteOrder order()
    {
        return this.buf.order();
    }

    public ByteBuf order(ByteOrder endianness)
    {
        return this.buf.order(endianness);
    }

    public ByteBuf unwrap()
    {
        return this.buf.unwrap();
    }

    public boolean isDirect()
    {
        return this.buf.isDirect();
    }

    public boolean isReadOnly()
    {
        return this.buf.isReadOnly();
    }

    public ByteBuf asReadOnly()
    {
        return this.buf.asReadOnly();
    }

    public int readerIndex()
    {
        return this.buf.readerIndex();
    }

    public ByteBuf readerIndex(int readerIndex)
    {
        return this.buf.readerIndex(readerIndex);
    }

    public int writerIndex()
    {
        return this.buf.writerIndex();
    }

    public ByteBuf writerIndex(int writerIndex)
    {
        return this.buf.writerIndex(writerIndex);
    }

    public ByteBuf setIndex(int readerIndex, int writerIndex)
    {
        return this.buf.setIndex(readerIndex, writerIndex);
    }

    public int readableBytes()
    {
        return this.buf.readableBytes();
    }

    public int writableBytes()
    {
        return this.buf.writableBytes();
    }

    public int maxWritableBytes()
    {
        return this.buf.maxWritableBytes();
    }

    public boolean isReadable()
    {
        return this.buf.isReadable();
    }

    public boolean isReadable(int size)
    {
        return this.buf.isReadable(size);
    }

    public boolean isWritable()
    {
        return this.buf.isWritable();
    }

    public boolean isWritable(int size)
    {
        return this.buf.isWritable(size);
    }

    public ByteBuf clear()
    {
        return this.buf.clear();
    }

    public ByteBuf markReaderIndex()
    {
        return this.buf.markReaderIndex();
    }

    public ByteBuf resetReaderIndex()
    {
        return this.buf.resetReaderIndex();
    }

    public ByteBuf markWriterIndex()
    {
        return this.buf.markWriterIndex();
    }

    public ByteBuf resetWriterIndex()
    {
        return this.buf.resetWriterIndex();
    }

    public ByteBuf discardReadBytes()
    {
        return this.buf.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes()
    {
        return this.buf.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int minWritableBytes)
    {
        return this.buf.ensureWritable(minWritableBytes);
    }

    public int ensureWritable(int minWritableBytes, boolean force)
    {
        return this.buf.ensureWritable(minWritableBytes, force);
    }

    public boolean getBoolean(int index)
    {
        return this.buf.getBoolean(index);
    }

    public byte getByte(int index)
    {
        return this.buf.getByte(index);
    }

    public short getUnsignedByte(int index)
    {
        return this.buf.getUnsignedByte(index);
    }

    public short getShort(int index)
    {
        return this.buf.getShort(index);
    }

    public short getShortLE(int index)
    {
        return this.buf.getShortLE(index);
    }

    public int getUnsignedShort(int index)
    {
        return this.buf.getUnsignedShort(index);
    }

    public int getUnsignedShortLE(int index)
    {
        return this.buf.getUnsignedShortLE(index);
    }

    public int getMedium(int index)
    {
        return this.buf.getMedium(index);
    }

    public int getMediumLE(int index)
    {
        return this.buf.getMediumLE(index);
    }

    public int getUnsignedMedium(int index)
    {
        return this.buf.getUnsignedMedium(index);
    }

    public int getUnsignedMediumLE(int index)
    {
        return this.buf.getUnsignedMediumLE(index);
    }

    public int getInt(int index)
    {
        return this.buf.getInt(index);
    }

    public int getIntLE(int index)
    {
        return this.buf.getIntLE(index);
    }

    public long getUnsignedInt(int index)
    {
        return this.buf.getUnsignedInt(index);
    }

    public long getUnsignedIntLE(int index)
    {
        return this.buf.getUnsignedIntLE(index);
    }

    public long getLong(int index)
    {
        return this.buf.getLong(index);
    }

    public long getLongLE(int index)
    {
        return this.buf.getLongLE(index);
    }

    public char getChar(int index)
    {
        return this.buf.getChar(index);
    }

    public float getFloat(int index)
    {
        return this.buf.getFloat(index);
    }

    public double getDouble(int index)
    {
        return this.buf.getDouble(index);
    }

    public ByteBuf getBytes(int index, ByteBuf dst)
    {
        return this.buf.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int length)
    {
        return this.buf.getBytes(index, dst, length);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
    {
        return this.buf.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, byte[] dst)
    {
        return this.buf.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
    {
        return this.buf.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, ByteBuffer dst)
    {
        return this.buf.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException
    {
        return this.buf.getBytes(index, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException
    {
        return this.buf.getBytes(index, out, length);
    }

    public int getBytes(int index, FileChannel out, long position, int length) throws IOException
    {
        return this.buf.getBytes(index, out, position, length);
    }

    public CharSequence getCharSequence(int index, int length, Charset charset)
    {
        return this.buf.getCharSequence(index, length, charset);
    }

    public ByteBuf setBoolean(int index, boolean value)
    {
        return this.buf.setBoolean(index, value);
    }

    public ByteBuf setByte(int index, int value)
    {
        return this.buf.setByte(index, value);
    }

    public ByteBuf setShort(int index, int value)
    {
        return this.buf.setShort(index, value);
    }

    public ByteBuf setShortLE(int index, int value)
    {
        return this.buf.setShortLE(index, value);
    }

    public ByteBuf setMedium(int index, int value)
    {
        return this.buf.setMedium(index, value);
    }

    public ByteBuf setMediumLE(int index, int value)
    {
        return this.buf.setMediumLE(index, value);
    }

    public ByteBuf setInt(int index, int value)
    {
        return this.buf.setInt(index, value);
    }

    public ByteBuf setIntLE(int index, int value)
    {
        return this.buf.setIntLE(index, value);
    }

    public ByteBuf setLong(int index, long value)
    {
        return this.buf.setLong(index, value);
    }

    public ByteBuf setLongLE(int index, long value)
    {
        return this.buf.setLongLE(index, value);
    }

    public ByteBuf setChar(int index, int value)
    {
        return this.buf.setChar(index, value);
    }

    public ByteBuf setFloat(int index, float value)
    {
        return this.buf.setFloat(index, value);
    }

    public ByteBuf setDouble(int index, double value)
    {
        return this.buf.setDouble(index, value);
    }

    public ByteBuf setBytes(int index, ByteBuf src)
    {
        return this.buf.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int length)
    {
        return this.buf.setBytes(index, src, length);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
    {
        return this.buf.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, byte[] src)
    {
        return this.buf.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
    {
        return this.buf.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, ByteBuffer src)
    {
        return this.buf.setBytes(index, src);
    }

    public int setBytes(int index, InputStream in, int length) throws IOException
    {
        return this.buf.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException
    {
        return this.buf.setBytes(index, in, length);
    }

    public int setBytes(int index, FileChannel in, long position, int length) throws IOException
    {
        return this.buf.setBytes(index, in, position, length);
    }

    public ByteBuf setZero(int index, int length)
    {
        return this.buf.setZero(index, length);
    }

    public int setCharSequence(int index, CharSequence sequence, Charset charset)
    {
        return this.buf.setCharSequence(index, sequence, charset);
    }

    public boolean readBoolean()
    {
        return this.buf.readBoolean();
    }

    public byte readByte()
    {
        return this.buf.readByte();
    }

    public short readUnsignedByte()
    {
        return this.buf.readUnsignedByte();
    }

    public short readShort()
    {
        return this.buf.readShort();
    }

    public short readShortLE()
    {
        return this.buf.readShortLE();
    }

    public int readUnsignedShort()
    {
        return this.buf.readUnsignedShort();
    }

    public int readUnsignedShortLE()
    {
        return this.buf.readUnsignedShortLE();
    }

    public int readMedium()
    {
        return this.buf.readMedium();
    }

    public int readMediumLE()
    {
        return this.buf.readMediumLE();
    }

    public int readUnsignedMedium()
    {
        return this.buf.readUnsignedMedium();
    }

    public int readUnsignedMediumLE()
    {
        return this.buf.readUnsignedMediumLE();
    }

    public int readInt()
    {
        return this.buf.readInt();
    }

    public int readIntLE()
    {
        return this.buf.readIntLE();
    }

    public long readUnsignedInt()
    {
        return this.buf.readUnsignedInt();
    }

    public long readUnsignedIntLE()
    {
        return this.buf.readUnsignedIntLE();
    }

    public long readLong()
    {
        return this.buf.readLong();
    }

    public long readLongLE()
    {
        return this.buf.readLongLE();
    }

    public char readChar()
    {
        return this.buf.readChar();
    }

    public float readFloat()
    {
        return this.buf.readFloat();
    }

    public double readDouble()
    {
        return this.buf.readDouble();
    }

    public ByteBuf readBytes(int length)
    {
        return this.buf.readBytes(length);
    }

    public ByteBuf readSlice(int length)
    {
        return this.buf.readSlice(length);
    }

    public ByteBuf readRetainedSlice(int length)
    {
        return this.buf.readRetainedSlice(length);
    }

    public ByteBuf readBytes(ByteBuf dst)
    {
        return this.buf.readBytes(dst);
    }

    public ByteBuf readBytes(ByteBuf dst, int length)
    {
        return this.buf.readBytes(dst, length);
    }

    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
    {
        return this.buf.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(byte[] dst)
    {
        return this.buf.readBytes(dst);
    }

    public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
    {
        return this.buf.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(ByteBuffer dst)
    {
        return this.buf.readBytes(dst);
    }

    public ByteBuf readBytes(OutputStream out, int length) throws IOException
    {
        return this.buf.readBytes(out, length);
    }

    public int readBytes(GatheringByteChannel out, int length) throws IOException
    {
        return this.buf.readBytes(out, length);
    }

    public int readBytes(FileChannel out, long position, int length) throws IOException
    {
        return this.buf.readBytes(out, position, length);
    }

    public CharSequence readCharSequence(int length, Charset charset)
    {
        return this.buf.readCharSequence(length, charset);
    }

    public ByteBuf skipBytes(int length)
    {
        return this.buf.skipBytes(length);
    }

    public ByteBuf writeBoolean(boolean value)
    {
        return this.buf.writeBoolean(value);
    }

    public ByteBuf writeByte(int value)
    {
        return this.buf.writeByte(value);
    }

    public ByteBuf writeShort(int value)
    {
        return this.buf.writeShort(value);
    }

    public ByteBuf writeShortLE(int value)
    {
        return this.buf.writeShortLE(value);
    }

    public ByteBuf writeMedium(int value)
    {
        return this.buf.writeMedium(value);
    }

    public ByteBuf writeMediumLE(int value)
    {
        return this.buf.writeMediumLE(value);
    }

    public ByteBuf writeInt(int value)
    {
        return this.buf.writeInt(value);
    }

    public ByteBuf writeIntLE(int value)
    {
        return this.buf.writeIntLE(value);
    }

    public ByteBuf writeLong(long value)
    {
        return this.buf.writeLong(value);
    }

    public ByteBuf writeLongLE(long value)
    {
        return this.buf.writeLongLE(value);
    }

    public ByteBuf writeChar(int value)
    {
        return this.buf.writeChar(value);
    }

    public ByteBuf writeFloat(float value)
    {
        return this.buf.writeFloat(value);
    }

    public ByteBuf writeDouble(double value)
    {
        return this.buf.writeDouble(value);
    }

    public ByteBuf writeBytes(ByteBuf src)
    {
        return this.buf.writeBytes(src);
    }

    public ByteBuf writeBytes(ByteBuf src, int length)
    {
        return this.buf.writeBytes(src, length);
    }

    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
    {
        return this.buf.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(byte[] src)
    {
        return this.buf.writeBytes(src);
    }

    public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
    {
        return this.buf.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(ByteBuffer src)
    {
        return this.buf.writeBytes(src);
    }

    public int writeBytes(InputStream in, int length) throws IOException
    {
        return this.buf.writeBytes(in, length);
    }

    public int writeBytes(ScatteringByteChannel in, int length) throws IOException
    {
        return this.buf.writeBytes(in, length);
    }

    public int writeBytes(FileChannel in, long position, int length) throws IOException
    {
        return this.buf.writeBytes(in, position, length);
    }

    public ByteBuf writeZero(int length)
    {
        return this.buf.writeZero(length);
    }

    public int writeCharSequence(CharSequence sequence, Charset charset)
    {
        return this.buf.writeCharSequence(sequence, charset);
    }

    public int indexOf(int fromIndex, int toIndex, byte value)
    {
        return this.buf.indexOf(fromIndex, toIndex, value);
    }

    public int bytesBefore(byte value)
    {
        return this.buf.bytesBefore(value);
    }

    public int bytesBefore(int length, byte value)
    {
        return this.buf.bytesBefore(length, value);
    }

    public int bytesBefore(int index, int length, byte value)
    {
        return this.buf.bytesBefore(index, length, value);
    }

    public int forEachByte(ByteProcessor processor)
    {
        return this.buf.forEachByte(processor);
    }

    public int forEachByte(int index, int length, ByteProcessor processor)
    {
        return this.buf.forEachByte(index, length, processor);
    }

    public int forEachByteDesc(ByteProcessor processor)
    {
        return this.buf.forEachByteDesc(processor);
    }

    public int forEachByteDesc(int index, int length, ByteProcessor processor)
    {
        return this.buf.forEachByteDesc(index, length, processor);
    }

    public ByteBuf copy()
    {
        return this.buf.copy();
    }

    public ByteBuf copy(int index, int length)
    {
        return this.buf.copy(index, length);
    }

    public ByteBuf slice()
    {
        return this.buf.slice();
    }

    public ByteBuf retainedSlice()
    {
        return this.buf.retainedSlice();
    }

    public ByteBuf slice(int index, int length)
    {
        return this.buf.slice(index, length);
    }

    public ByteBuf retainedSlice(int index, int length)
    {
        return this.buf.retainedSlice(index, length);
    }

    public ByteBuf duplicate()
    {
        return this.buf.duplicate();
    }

    public ByteBuf retainedDuplicate()
    {
        return this.buf.retainedDuplicate();
    }

    public int nioBufferCount()
    {
        return this.buf.nioBufferCount();
    }

    public ByteBuffer nioBuffer()
    {
        return this.buf.nioBuffer();
    }

    public ByteBuffer nioBuffer(int index, int length)
    {
        return this.buf.nioBuffer(index, length);
    }

    public ByteBuffer internalNioBuffer(int index, int length)
    {
        return this.buf.internalNioBuffer(index, length);
    }

    public ByteBuffer[] nioBuffers()
    {
        return this.buf.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int index, int length)
    {
        return this.buf.nioBuffers(index, length);
    }

    public boolean hasArray()
    {
        return this.buf.hasArray();
    }

    public byte[] array()
    {
        return this.buf.array();
    }

    public int arrayOffset()
    {
        return this.buf.arrayOffset();
    }

    public boolean hasMemoryAddress()
    {
        return this.buf.hasMemoryAddress();
    }

    public long memoryAddress()
    {
        return this.buf.memoryAddress();
    }

    public String toString(Charset charset)
    {
        return this.buf.toString(charset);
    }

    public String toString(int index, int length, Charset charset)
    {
        return this.buf.toString(index, length, charset);
    }

    public int hashCode()
    {
        return this.buf.hashCode();
    }

    public boolean equals(Object other)
    {
        return this.buf.equals(other);
    }

    public int compareTo(ByteBuf other)
    {
        return this.buf.compareTo(other);
    }

    public String toString()
    {
        return this.buf.toString();
    }

    public ByteBuf retain(int increment)
    {
        return this.buf.retain(increment);
    }

    public ByteBuf retain()
    {
        return this.buf.retain();
    }

    public ByteBuf touch()
    {
        return this.buf.touch();
    }

    public ByteBuf touch(Object hint)
    {
        return this.buf.touch(hint);
    }

    public int refCnt()
    {
        return this.buf.refCnt();
    }

    public boolean release()
    {
        return this.buf.release();
    }

    public boolean release(int decrement)
    {
        return this.buf.release(decrement);
    }
}
