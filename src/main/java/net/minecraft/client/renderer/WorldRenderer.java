package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.optifine.SmartAnimations;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;

public class WorldRenderer
{
    private ByteBuffer byteBuffer;
    public IntBuffer rawIntBuffer;
    private ShortBuffer rawShortBuffer;
    public FloatBuffer rawFloatBuffer;
    public int vertexCount;
    private VertexFormatElement vertexFormatElement;
    private int vertexFormatIndex;
    private boolean noColor;
    public int drawMode;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private VertexFormat vertexFormat;
    private boolean isDrawing;
    private EnumWorldBlockLayer blockLayer = null;
    private boolean[] drawnIcons = new boolean[256];
    private TextureAtlasSprite[] quadSprites = null;
    private TextureAtlasSprite[] quadSpritesPrev = null;
    private TextureAtlasSprite quadSprite = null;
    public SVertexBuilder sVertexBuilder;
    public RenderEnv renderEnv = null;
    public BitSet animatedSprites = null;
    public BitSet animatedSpritesCached = new BitSet();
    private boolean modeTriangles = false;
    private ByteBuffer byteBufferTriangles;
    private float[] quadSortDistances = new float[0];
    private int[] quadSortIndices = new int[0];
    private int[] quadSortIndicesTemp = new int[0];
    private BitSet quadSortVisited = new BitSet();
    private int[] quadSortSwapBuffer = new int[0];
    private TextureAtlasSprite[] quadSortSprites = null;

    public WorldRenderer(int bufferSizeIn)
    {
        this.byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
        this.rawIntBuffer = this.byteBuffer.asIntBuffer();
        this.rawShortBuffer = this.byteBuffer.asShortBuffer();
        this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
        SVertexBuilder.initVertexBuilder(this);
    }

    private void growBuffer(int size)
    {
        if (size > this.rawIntBuffer.remaining())
        {
            int i = this.byteBuffer.capacity();
            int j = i % 2097152;
            int k = j + (((this.rawIntBuffer.position() + size) * 4 - j) / 2097152 + 1) * 2097152;
            // Buffer growth (rare, no logging in hot path)
            int l = this.rawIntBuffer.position();
            ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(k);
            this.byteBuffer.position(0);
            bytebuffer.put(this.byteBuffer);
            bytebuffer.rewind();
            this.byteBuffer = bytebuffer;
            this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
            this.rawIntBuffer = this.byteBuffer.asIntBuffer();
            this.rawIntBuffer.position(l);
            this.rawShortBuffer = this.byteBuffer.asShortBuffer();
            this.rawShortBuffer.position(l << 1);

            if (this.quadSprites != null)
            {
                TextureAtlasSprite[] atextureatlassprite = this.quadSprites;
                int intValue2 = this.getBufferQuadSize();
                this.quadSprites = new TextureAtlasSprite[intValue2];
                System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, Math.min(atextureatlassprite.length, this.quadSprites.length));
                this.quadSpritesPrev = null;
            }
        }
    }

    public void sortVertexData(float x, float y, float z)
    {
        int quadCount = this.vertexCount / 4;
        int quadStride = this.vertexFormat.getNextOffset();
        this.ensureQuadSortStorage(quadCount, quadStride);
        float[] distances = this.quadSortDistances;
        int[] sortIndices = this.quadSortIndices;

        for (int quadIndex = 0; quadIndex < quadCount; ++quadIndex)
        {
            distances[quadIndex] = getDistanceSq(this.rawFloatBuffer, (float)((double)x + this.xOffset), (float)((double)y + this.yOffset), (float)((double)z + this.zOffset), this.vertexFormat.getIntegerSize(), quadIndex * quadStride);
            sortIndices[quadIndex] = quadIndex;
        }

        this.sortQuadIndicesByDistance(sortIndices, this.quadSortIndicesTemp, distances, quadCount);
        BitSet visited = this.quadSortVisited;
        visited.clear();
        int[] swapBuffer = this.quadSortSwapBuffer;

        for (int targetIndex = 0; (targetIndex = visited.nextClearBit(targetIndex)) < quadCount; ++targetIndex)
        {
            int sourceIndex = sortIndices[targetIndex];

            if (sourceIndex != targetIndex)
            {
                this.rawIntBuffer.limit(sourceIndex * quadStride + quadStride);
                this.rawIntBuffer.position(sourceIndex * quadStride);
                this.rawIntBuffer.get(swapBuffer);
                int currentIndex = sourceIndex;

                for (int nextIndex = sortIndices[sourceIndex]; currentIndex != targetIndex; nextIndex = sortIndices[nextIndex])
                {
                    this.rawIntBuffer.limit(nextIndex * quadStride + quadStride);
                    this.rawIntBuffer.position(nextIndex * quadStride);
                    IntBuffer intbuffer = this.rawIntBuffer.slice();
                    this.rawIntBuffer.limit(currentIndex * quadStride + quadStride);
                    this.rawIntBuffer.position(currentIndex * quadStride);
                    this.rawIntBuffer.put(intbuffer);
                    visited.set(currentIndex);
                    currentIndex = nextIndex;
                }

                this.rawIntBuffer.limit(targetIndex * quadStride + quadStride);
                this.rawIntBuffer.position(targetIndex * quadStride);
                this.rawIntBuffer.put(swapBuffer);
            }

            visited.set(targetIndex);
        }

        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(this.getBufferSize());

        if (this.quadSprites != null)
        {
            TextureAtlasSprite[] sortedSprites = this.quadSortSprites;

            for (int spriteIndex = 0; spriteIndex < quadCount; ++spriteIndex)
            {
                sortedSprites[spriteIndex] = this.quadSprites[sortIndices[spriteIndex]];
            }

            System.arraycopy(sortedSprites, 0, this.quadSprites, 0, quadCount);
            Arrays.fill(sortedSprites, 0, quadCount, (TextureAtlasSprite)null);
        }
    }

    private void ensureQuadSortStorage(int quadCount, int quadStride)
    {
        if (this.quadSortDistances.length < quadCount)
        {
            this.quadSortDistances = new float[quadCount];
            this.quadSortIndices = new int[quadCount];
            this.quadSortIndicesTemp = new int[quadCount];
        }

        if (this.quadSortSwapBuffer.length < quadStride)
        {
            this.quadSortSwapBuffer = new int[quadStride];
        }

        if (this.quadSprites != null && (this.quadSortSprites == null || this.quadSortSprites.length < quadCount))
        {
            this.quadSortSprites = new TextureAtlasSprite[quadCount];
        }
    }

    private void sortQuadIndicesByDistance(int[] indices, int[] tempIndices, float[] distances, int length)
    {
        for (int width = 1; width < length; width <<= 1)
        {
            for (int start = 0; start < length; start += width << 1)
            {
                int middle = Math.min(start + width, length);
                int end = Math.min(start + (width << 1), length);
                this.mergeQuadSortRanges(indices, tempIndices, distances, start, middle, end);
            }

            int[] swap = indices;
            indices = tempIndices;
            tempIndices = swap;
        }

        if (indices != this.quadSortIndices)
        {
            System.arraycopy(indices, 0, this.quadSortIndices, 0, length);
        }
    }

    private void mergeQuadSortRanges(int[] indices, int[] tempIndices, float[] distances, int start, int middle, int end)
    {
        int leftIndex = start;
        int rightIndex = middle;

        for (int outputIndex = start; outputIndex < end; ++outputIndex)
        {
            if (leftIndex < middle && (rightIndex >= end || Float.compare(distances[indices[rightIndex]], distances[indices[leftIndex]]) <= 0))
            {
                tempIndices[outputIndex] = indices[leftIndex++];
            }
            else
            {
                tempIndices[outputIndex] = indices[rightIndex++];
            }
        }
    }

    public WorldRenderer.State getVertexState()
    {
        this.rawIntBuffer.rewind();
        int i = this.getBufferSize();
        this.rawIntBuffer.limit(i);
        int[] aint = new int[i];
        this.rawIntBuffer.get(aint);
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(i);
        TextureAtlasSprite[] atextureatlassprite = null;

        if (this.quadSprites != null)
        {
            int j = this.vertexCount / 4;
            atextureatlassprite = new TextureAtlasSprite[j];
            System.arraycopy(this.quadSprites, 0, atextureatlassprite, 0, j);
        }

        return new WorldRenderer.State(aint, new VertexFormat(this.vertexFormat), atextureatlassprite);
    }

    public int getBufferSize()
    {
        return this.vertexCount * this.vertexFormat.getIntegerSize();
    }

    private static float getDistanceSq(FloatBuffer buffer, float x, float y, float z, int vertexSize, int offset)
    {
        float f = buffer.get(offset + vertexSize * 0 + 0);
        float floatValue2 = buffer.get(offset + vertexSize * 0 + 1);
        float floatValue3 = buffer.get(offset + vertexSize * 0 + 2);
        float floatValue4 = buffer.get(offset + vertexSize * 1 + 0);
        float floatValue5 = buffer.get(offset + vertexSize * 1 + 1);
        float floatValue6 = buffer.get(offset + vertexSize * 1 + 2);
        float floatValue7 = buffer.get(offset + vertexSize * 2 + 0);
        float floatValue8 = buffer.get(offset + vertexSize * 2 + 1);
        float floatValue9 = buffer.get(offset + vertexSize * 2 + 2);
        float floatValue10 = buffer.get(offset + vertexSize * 3 + 0);
        float floatValue11 = buffer.get(offset + vertexSize * 3 + 1);
        float floatValue12 = buffer.get(offset + vertexSize * 3 + 2);
        float floatValue13 = (f + floatValue4 + floatValue7 + floatValue10) * 0.25F - x;
        float floatValue14 = (floatValue2 + floatValue5 + floatValue8 + floatValue11) * 0.25F - y;
        float floatValue15 = (floatValue3 + floatValue6 + floatValue9 + floatValue12) * 0.25F - z;
        return floatValue13 * floatValue13 + floatValue14 * floatValue14 + floatValue15 * floatValue15;
    }

    public void setVertexState(WorldRenderer.State state)
    {
        this.rawIntBuffer.clear();
        this.growBuffer(state.getRawBuffer().length);
        this.rawIntBuffer.put(state.getRawBuffer());
        this.vertexCount = state.getVertexCount();
        this.vertexFormat = new VertexFormat(state.getVertexFormat());

        if (state.stateQuadSprites != null)
        {
            if (this.quadSprites == null)
            {
                this.quadSprites = this.quadSpritesPrev;
            }

            if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize())
            {
                this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
            }

            TextureAtlasSprite[] atextureatlassprite = state.stateQuadSprites;
            System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, atextureatlassprite.length);
        }
        else
        {
            if (this.quadSprites != null)
            {
                this.quadSpritesPrev = this.quadSprites;
            }

            this.quadSprites = null;
        }
    }

    public void cancelDrawing()
    {
        this.isDrawing = false;
        this.reset();
    }

    public void reset()
    {
        this.vertexCount = 0;
        this.vertexFormatElement = null;
        this.vertexFormatIndex = 0;
        this.quadSprite = null;

        if (SmartAnimations.isActive())
        {
            if (this.animatedSprites == null)
            {
                this.animatedSprites = this.animatedSpritesCached;
            }

            this.animatedSprites.clear();
        }
        else if (this.animatedSprites != null)
        {
            this.animatedSprites = null;
        }

        this.modeTriangles = false;
    }

    public void begin(int glMode, VertexFormat format)
    {
        if (this.isDrawing)
        {
            throw new IllegalStateException("Already building!");
        }
        else
        {
            this.isDrawing = true;
            this.reset();
            this.drawMode = glMode;
            this.vertexFormat = format;
            this.vertexFormatElement = format.getElement(this.vertexFormatIndex);
            this.noColor = false;
            this.byteBuffer.limit(this.byteBuffer.capacity());

            if (Config.isShaders())
            {
                SVertexBuilder.endSetVertexFormat(this);
            }

            if (Config.isMultiTexture())
            {
                if (this.blockLayer != null)
                {
                    if (this.quadSprites == null)
                    {
                        this.quadSprites = this.quadSpritesPrev;
                    }

                    if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize())
                    {
                        this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
                    }
                }
            }
            else
            {
                if (this.quadSprites != null)
                {
                    this.quadSpritesPrev = this.quadSprites;
                }

                this.quadSprites = null;
            }
        }
    }

    public WorldRenderer tex(double u, double v)
    {
        if (this.quadSprite != null && this.quadSprites != null)
        {
            u = (double)this.quadSprite.toSingleU((float)u);
            v = (double)this.quadSprite.toSingleV((float)v);
            this.quadSprites[this.vertexCount / 4] = this.quadSprite;
        }

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType())
        {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float)u);
                this.byteBuffer.putFloat(i + 4, (float)v);
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int)u);
                this.byteBuffer.putInt(i + 4, (int)v);
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short)((int)v));
                this.byteBuffer.putShort(i + 2, (short)((int)u));
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte)((int)v));
                this.byteBuffer.put(i + 1, (byte)((int)u));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public WorldRenderer lightmap(int skyLight, int blockLight)
    {
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType())
        {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float)skyLight);
                this.byteBuffer.putFloat(i + 4, (float)blockLight);
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, skyLight);
                this.byteBuffer.putInt(i + 4, blockLight);
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short)blockLight);
                this.byteBuffer.putShort(i + 2, (short)skyLight);
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte)blockLight);
                this.byteBuffer.put(i + 1, (byte)skyLight);
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void putBrightness4(int brightness1, int brightness2, int brightness3, int brightness4)
    {
        int i = (this.vertexCount - 4) * this.vertexFormat.getIntegerSize() + this.vertexFormat.getUvOffsetById(1) / 4;
        int j = this.vertexFormat.getNextOffset() >> 2;
        this.rawIntBuffer.put(i, brightness1);
        this.rawIntBuffer.put(i + j, brightness2);
        this.rawIntBuffer.put(i + j * 2, brightness3);
        this.rawIntBuffer.put(i + j * 3, brightness4);
    }

    public void putPosition(double x, double y, double z)
    {
        int i = this.vertexFormat.getIntegerSize();
        int j = (this.vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k)
        {
            int l = j + k * i;
            int intValue2 = l + 1;
            int secondIntValue2 = intValue2 + 1;
            this.rawIntBuffer.put(l, Float.floatToRawIntBits((float)(x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l))));
            this.rawIntBuffer.put(intValue2, Float.floatToRawIntBits((float)(y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(intValue2))));
            this.rawIntBuffer.put(secondIntValue2, Float.floatToRawIntBits((float)(z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(secondIntValue2))));
        }
    }

    public int getColorIndex(int vertexIndex)
    {
        return ((this.vertexCount - vertexIndex) * this.vertexFormat.getNextOffset() + this.vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int vertexIndex)
    {
        int i = this.getColorIndex(vertexIndex);
        int j = -1;

        if (!this.noColor)
        {
            j = this.rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
            {
                int k = (int)((float)(j & 255) * red);
                int l = (int)((float)(j >> 8 & 255) * green);
                int intValue = (int)((float)(j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | intValue << 16 | l << 8 | k;
            }
            else
            {
                int secondIntValue = (int)((float)(j >> 24 & 255) * red);
                int thirdIntValue = (int)((float)(j >> 16 & 255) * green);
                int fourthIntValue = (int)((float)(j >> 8 & 255) * blue);
                j = j & 255;
                j = j | secondIntValue << 24 | thirdIntValue << 16 | fourthIntValue << 8;
            }
        }

        this.rawIntBuffer.put(i, j);
    }

    private void putColor(int argb, int vertexIndex)
    {
        int i = this.getColorIndex(vertexIndex);
        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        int intValue2 = argb >> 24 & 255;
        this.putColorRGBA(i, j, k, l, intValue2);
    }

    public void putColorRGB_F(float red, float green, float blue, int vertexIndex)
    {
        int i = this.getColorIndex(vertexIndex);
        int j = MathHelper.clamp_int((int)(red * 255.0F), 0, 255);
        int k = MathHelper.clamp_int((int)(green * 255.0F), 0, 255);
        int l = MathHelper.clamp_int((int)(blue * 255.0F), 0, 255);
        this.putColorRGBA(i, j, k, l, 255);
    }

    public void putColorRGBA(int index, int red, int green, int blue, int alpha)
    {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            this.rawIntBuffer.put(index, alpha << 24 | blue << 16 | green << 8 | red);
        }
        else
        {
            this.rawIntBuffer.put(index, red << 24 | green << 16 | blue << 8 | alpha);
        }
    }

    public void noColor()
    {
        this.noColor = true;
    }

    public WorldRenderer color(float red, float green, float blue, float alpha)
    {
        return this.color((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
    }

    public WorldRenderer color(int red, int green, int blue, int alpha)
    {
        if (this.noColor)
        {
            return this;
        }
        else
        {
            int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

            switch (this.vertexFormatElement.getType())
            {
                case FLOAT:
                    this.byteBuffer.putFloat(i, (float)red / 255.0F);
                    this.byteBuffer.putFloat(i + 4, (float)green / 255.0F);
                    this.byteBuffer.putFloat(i + 8, (float)blue / 255.0F);
                    this.byteBuffer.putFloat(i + 12, (float)alpha / 255.0F);
                    break;

                case UINT:
                case INT:
                    this.byteBuffer.putFloat(i, (float)red);
                    this.byteBuffer.putFloat(i + 4, (float)green);
                    this.byteBuffer.putFloat(i + 8, (float)blue);
                    this.byteBuffer.putFloat(i + 12, (float)alpha);
                    break;

                case USHORT:
                case SHORT:
                    this.byteBuffer.putShort(i, (short)red);
                    this.byteBuffer.putShort(i + 2, (short)green);
                    this.byteBuffer.putShort(i + 4, (short)blue);
                    this.byteBuffer.putShort(i + 6, (short)alpha);
                    break;

                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
                    {
                        this.byteBuffer.put(i, (byte)red);
                        this.byteBuffer.put(i + 1, (byte)green);
                        this.byteBuffer.put(i + 2, (byte)blue);
                        this.byteBuffer.put(i + 3, (byte)alpha);
                    }
                    else
                    {
                        this.byteBuffer.put(i, (byte)alpha);
                        this.byteBuffer.put(i + 1, (byte)blue);
                        this.byteBuffer.put(i + 2, (byte)green);
                        this.byteBuffer.put(i + 3, (byte)red);
                    }
            }

            this.nextVertexFormatIndex();
            return this;
        }
    }

    public void addVertexData(int[] vertexData)
    {
        if (Config.isShaders())
        {
            SVertexBuilder.beginAddVertexData(this, vertexData);
        }

        this.growBuffer(vertexData.length);
        this.rawIntBuffer.position(this.getBufferSize());
        this.rawIntBuffer.put(vertexData);
        this.vertexCount += vertexData.length / this.vertexFormat.getIntegerSize();

        if (Config.isShaders())
        {
            SVertexBuilder.endAddVertexData(this);
        }
    }

    public void endVertex()
    {
        ++this.vertexCount;
        this.growBuffer(this.vertexFormat.getIntegerSize());
        this.vertexFormatIndex = 0;
        this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);

        if (Config.isShaders())
        {
            SVertexBuilder.endAddVertex(this);
        }
    }

    public WorldRenderer pos(double x, double y, double z)
    {
        if (Config.isShaders())
        {
            SVertexBuilder.beginAddVertex(this);
        }

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType())
        {
            case FLOAT:
                this.byteBuffer.putFloat(i, (float)(x + this.xOffset));
                this.byteBuffer.putFloat(i + 4, (float)(y + this.yOffset));
                this.byteBuffer.putFloat(i + 8, (float)(z + this.zOffset));
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, Float.floatToRawIntBits((float)(x + this.xOffset)));
                this.byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float)(y + this.yOffset)));
                this.byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float)(z + this.zOffset)));
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short)((int)(x + this.xOffset)));
                this.byteBuffer.putShort(i + 2, (short)((int)(y + this.yOffset)));
                this.byteBuffer.putShort(i + 4, (short)((int)(z + this.zOffset)));
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte)((int)(x + this.xOffset)));
                this.byteBuffer.put(i + 1, (byte)((int)(y + this.yOffset)));
                this.byteBuffer.put(i + 2, (byte)((int)(z + this.zOffset)));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void putNormal(float x, float y, float z)
    {
        int i = (byte)((int)(x * 127.0F)) & 255;
        int j = (byte)((int)(y * 127.0F)) & 255;
        int k = (byte)((int)(z * 127.0F)) & 255;
        int l = i | j << 8 | k << 16;
        int intValue2 = this.vertexFormat.getNextOffset() >> 2;
        int secondIntValue2 = (this.vertexCount - 4) * intValue2 + this.vertexFormat.getNormalOffset() / 4;
        this.rawIntBuffer.put(secondIntValue2, l);
        this.rawIntBuffer.put(secondIntValue2 + intValue2, l);
        this.rawIntBuffer.put(secondIntValue2 + intValue2 * 2, l);
        this.rawIntBuffer.put(secondIntValue2 + intValue2 * 3, l);
    }

    private void nextVertexFormatIndex()
    {
        ++this.vertexFormatIndex;
        this.vertexFormatIndex %= this.vertexFormat.getElementCount();
        this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);

        if (this.vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING)
        {
            this.nextVertexFormatIndex();
        }
    }

    public WorldRenderer normal(float x, float y, float z)
    {
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

        switch (this.vertexFormatElement.getType())
        {
            case FLOAT:
                this.byteBuffer.putFloat(i, x);
                this.byteBuffer.putFloat(i + 4, y);
                this.byteBuffer.putFloat(i + 8, z);
                break;

            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int)x);
                this.byteBuffer.putInt(i + 4, (int)y);
                this.byteBuffer.putInt(i + 8, (int)z);
                break;

            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short)((int)(x * 32767.0F) & 65535));
                this.byteBuffer.putShort(i + 2, (short)((int)(y * 32767.0F) & 65535));
                this.byteBuffer.putShort(i + 4, (short)((int)(z * 32767.0F) & 65535));
                break;

            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte)((int)(x * 127.0F) & 255));
                this.byteBuffer.put(i + 1, (byte)((int)(y * 127.0F) & 255));
                this.byteBuffer.put(i + 2, (byte)((int)(z * 127.0F) & 255));
        }

        this.nextVertexFormatIndex();
        return this;
    }

    public void setTranslation(double x, double y, double z)
    {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    public void finishDrawing()
    {
        if (!this.isDrawing)
        {
            throw new IllegalStateException("Not building!");
        }
        else
        {
            this.isDrawing = false;
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.getBufferSize() * 4);
        }
    }

    public ByteBuffer getByteBuffer()
    {
        return this.modeTriangles ? this.byteBufferTriangles : this.byteBuffer;
    }

    public VertexFormat getVertexFormat()
    {
        return this.vertexFormat;
    }

    public int getVertexCount()
    {
        return this.modeTriangles ? this.vertexCount / 4 * 6 : this.vertexCount;
    }

    public int getDrawMode()
    {
        return this.modeTriangles ? 4 : this.drawMode;
    }

    public void putColor4(int argb)
    {
        for (int i = 0; i < 4; ++i)
        {
            this.putColor(argb, i + 1);
        }
    }

    public void putColorRGB_F4(float red, float green, float blue)
    {
        for (int i = 0; i < 4; ++i)
        {
            this.putColorRGB_F(red, green, blue, i + 1);
        }
    }

    public void putSprite(TextureAtlasSprite sprite)
    {
        if (this.animatedSprites != null && sprite != null && sprite.getAnimationIndex() >= 0)
        {
            this.animatedSprites.set(sprite.getAnimationIndex());
        }

        if (this.quadSprites != null)
        {
            int i = this.vertexCount / 4;
            this.quadSprites[i - 1] = sprite;
        }
    }

    public void setSprite(TextureAtlasSprite sprite)
    {
        if (this.animatedSprites != null && sprite != null && sprite.getAnimationIndex() >= 0)
        {
            this.animatedSprites.set(sprite.getAnimationIndex());
        }

        if (this.quadSprites != null)
        {
            this.quadSprite = sprite;
        }
    }

    public boolean isMultiTexture()
    {
        return this.quadSprites != null;
    }

    public void drawMultiTexture()
    {
        if (this.quadSprites != null)
        {
            int i = Config.getMinecraft().getTextureMapBlocks().getCountRegisteredSprites();

            if (this.drawnIcons.length <= i)
            {
                this.drawnIcons = new boolean[i + 1];
            }

            Arrays.fill(this.drawnIcons, false);
            int j = 0;
            int k = -1;
            int l = this.vertexCount / 4;

            for (int index = 0; index < l; ++index)
            {
                TextureAtlasSprite textureAtlasSprite = this.quadSprites[index];

                if (textureAtlasSprite != null)
                {
                    int secondIntValue2 = textureAtlasSprite.getIndexInMap();

                    if (!this.drawnIcons[secondIntValue2])
                    {
                        if (textureAtlasSprite == TextureUtils.iconGrassSideOverlay)
                        {
                            if (k < 0)
                            {
                                k = index;
                            }
                        }
                        else
                        {
                            index = this.drawForIcon(textureAtlasSprite, index) - 1;
                            ++j;

                            if (this.blockLayer != EnumWorldBlockLayer.TRANSLUCENT)
                            {
                                this.drawnIcons[secondIntValue2] = true;
                            }
                        }
                    }
                }
            }

            if (k >= 0)
            {
                this.drawForIcon(TextureUtils.iconGrassSideOverlay, k);
                ++j;
            }

            if (j > 0)
            {
                ;
            }
        }
    }

    private int drawForIcon(TextureAtlasSprite sprite, int startQuad)
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.glSpriteTextureId);
        int i = -1;
        int j = -1;
        int k = this.vertexCount / 4;

        for (int l = startQuad; l < k; ++l)
        {
            TextureAtlasSprite textureAtlasSprite = this.quadSprites[l];

            if (textureAtlasSprite == sprite)
            {
                if (j < 0)
                {
                    j = l;
                }
            }
            else if (j >= 0)
            {
                this.draw(j, l);

                if (this.blockLayer == EnumWorldBlockLayer.TRANSLUCENT)
                {
                    return l;
                }

                j = -1;

                if (i < 0)
                {
                    i = l;
                }
            }
        }

        if (j >= 0)
        {
            this.draw(j, k);
        }

        if (i < 0)
        {
            i = k;
        }

        return i;
    }

    private void draw(int startQuad, int endQuad)
    {
        int i = endQuad - startQuad;

        if (i > 0)
        {
            int j = startQuad * 4;
            int k = i * 4;
            GlStateManager.glDrawArrays(this.drawMode, j, k);
        }
    }

    public void setBlockLayer(EnumWorldBlockLayer blockLayer)
    {
        this.blockLayer = blockLayer;

        if (blockLayer == null)
        {
            if (this.quadSprites != null)
            {
                this.quadSpritesPrev = this.quadSprites;
            }

            this.quadSprites = null;
            this.quadSprite = null;
        }
    }

    private int getBufferQuadSize()
    {
        int i = this.rawIntBuffer.capacity() * 4 / (this.vertexFormat.getIntegerSize() * 4);
        return i;
    }

    public RenderEnv getRenderEnv(IBlockState state, BlockPos pos)
    {
        if (this.renderEnv == null)
        {
            this.renderEnv = new RenderEnv(state, pos);
            return this.renderEnv;
        }
        else
        {
            this.renderEnv.reset(state, pos);
            return this.renderEnv;
        }
    }

    public boolean isDrawing()
    {
        return this.isDrawing;
    }

    public double getXOffset()
    {
        return this.xOffset;
    }

    public double getYOffset()
    {
        return this.yOffset;
    }

    public double getZOffset()
    {
        return this.zOffset;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return this.blockLayer;
    }

    public void putColorMultiplierRgba(float red, float green, float blue, float alpha, int vertexIndex)
    {
        int i = this.getColorIndex(vertexIndex);
        int j = -1;

        if (!this.noColor)
        {
            j = this.rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
            {
                int k = (int)((float)(j & 255) * red);
                int l = (int)((float)(j >> 8 & 255) * green);
                int intValue2 = (int)((float)(j >> 16 & 255) * blue);
                int secondIntValue2 = (int)((float)(j >> 24 & 255) * alpha);
                j = secondIntValue2 << 24 | intValue2 << 16 | l << 8 | k;
            }
            else
            {
                int thirdIntValue2 = (int)((float)(j >> 24 & 255) * red);
                int fourthIntValue2 = (int)((float)(j >> 16 & 255) * green);
                int intValue3 = (int)((float)(j >> 8 & 255) * blue);
                int intValue4 = (int)((float)(j & 255) * alpha);
                j = thirdIntValue2 << 24 | fourthIntValue2 << 16 | intValue3 << 8 | intValue4;
            }
        }

        this.rawIntBuffer.put(i, j);
    }

    public void quadsToTriangles()
    {
        if (this.drawMode == 7)
        {
            if (this.byteBufferTriangles == null)
            {
                this.byteBufferTriangles = GLAllocation.createDirectByteBuffer(this.byteBuffer.capacity() * 2);
            }

            if (this.byteBufferTriangles.capacity() < this.byteBuffer.capacity() * 2)
            {
                this.byteBufferTriangles = GLAllocation.createDirectByteBuffer(this.byteBuffer.capacity() * 2);
            }

            int i = this.vertexFormat.getNextOffset();
            int j = this.byteBuffer.limit();
            this.byteBuffer.rewind();
            this.byteBufferTriangles.clear();

            for (int k = 0; k < this.vertexCount; k += 4)
            {
                this.byteBuffer.limit((k + 3) * i);
                this.byteBuffer.position(k * i);
                this.byteBufferTriangles.put(this.byteBuffer);
                this.byteBuffer.limit((k + 1) * i);
                this.byteBuffer.position(k * i);
                this.byteBufferTriangles.put(this.byteBuffer);
                this.byteBuffer.limit((k + 2 + 2) * i);
                this.byteBuffer.position((k + 2) * i);
                this.byteBufferTriangles.put(this.byteBuffer);
            }

            this.byteBuffer.limit(j);
            this.byteBuffer.rewind();
            this.byteBufferTriangles.flip();
            this.modeTriangles = true;
        }
    }

    public boolean isColorDisabled()
    {
        return this.noColor;
    }

    public class State
    {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;
        private TextureAtlasSprite[] stateQuadSprites;

        public State(int[] rawBuffer, VertexFormat vertexFormat, TextureAtlasSprite[] quadSprites)
        {
            this.stateRawBuffer = rawBuffer;
            this.stateVertexFormat = vertexFormat;
            this.stateQuadSprites = quadSprites;
        }

        public State(int[] buffer, VertexFormat format)
        {
            this.stateRawBuffer = buffer;
            this.stateVertexFormat = format;
        }

        public int[] getRawBuffer()
        {
            return this.stateRawBuffer;
        }

        public int getVertexCount()
        {
            return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
        }

        public VertexFormat getVertexFormat()
        {
            return this.stateVertexFormat;
        }
    }
}
