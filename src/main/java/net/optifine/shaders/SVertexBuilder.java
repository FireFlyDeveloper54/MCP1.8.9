package net.optifine.shaders;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class SVertexBuilder
{
    int vertexSize;
    int offsetNormal;
    int offsetUV;
    int offsetUVCenter;
    boolean hasNormal;
    boolean hasTangent;
    boolean hasUV;
    boolean hasUVCenter;
    long[] entityData = new long[10];
    int entityDataIndex = 0;

    public SVertexBuilder()
    {
        this.entityData[this.entityDataIndex] = 0L;
    }

    public static void initVertexBuilder(WorldRenderer wrr)
    {
        wrr.sVertexBuilder = new SVertexBuilder();
    }

    public void pushEntity(long data)
    {
        ++this.entityDataIndex;
        this.entityData[this.entityDataIndex] = data;
    }

    public void popEntity()
    {
        this.entityData[this.entityDataIndex] = 0L;
        --this.entityDataIndex;
    }

    public static void pushEntity(IBlockState blockState, BlockPos blockPos, IBlockAccess blockAccess, WorldRenderer wrr)
    {
        Block block = blockState.getBlock();
        int blockId;
        int metadata;

        if (blockState instanceof BlockStateBase)
        {
            BlockStateBase blockStateBase = (BlockStateBase)blockState;
            blockId = blockStateBase.getBlockId();
            metadata = blockStateBase.getMetadata();
        }
        else
        {
            blockId = Block.getIdFromBlock(block);
            metadata = block.getMetaFromState(blockState);
        }

        int aliasBlockId = BlockAliases.getBlockAliasId(blockId, metadata);

        if (aliasBlockId >= 0)
        {
            blockId = aliasBlockId;
        }

        int renderType = block.getRenderType();
        int packedBlockId = ((renderType & 65535) << 16) + (blockId & 65535);
        int packedMetadata = metadata & 65535;
        wrr.sVertexBuilder.pushEntity(((long)packedMetadata << 32) + (long)packedBlockId);
    }

    public static void popEntity(WorldRenderer wrr)
    {
        wrr.sVertexBuilder.popEntity();
    }

    public static boolean popEntity(boolean value, WorldRenderer wrr)
    {
        wrr.sVertexBuilder.popEntity();
        return value;
    }

    public static void endSetVertexFormat(WorldRenderer wrr)
    {
        SVertexBuilder vertexBuilder = wrr.sVertexBuilder;
        VertexFormat vertexFormat = wrr.getVertexFormat();
        vertexBuilder.vertexSize = vertexFormat.getNextOffset() / 4;
        vertexBuilder.hasNormal = vertexFormat.hasNormal();
        vertexBuilder.hasTangent = vertexBuilder.hasNormal;
        vertexBuilder.hasUV = vertexFormat.hasUvOffset(0);
        vertexBuilder.offsetNormal = vertexBuilder.hasNormal ? vertexFormat.getNormalOffset() / 4 : 0;
        vertexBuilder.offsetUV = vertexBuilder.hasUV ? vertexFormat.getUvOffsetById(0) / 4 : 0;
        vertexBuilder.offsetUVCenter = 8;
    }

    public static void beginAddVertex(WorldRenderer wrr)
    {
        if (wrr.vertexCount == 0)
        {
            endSetVertexFormat(wrr);
        }
    }

    public static void endAddVertex(WorldRenderer wrr)
    {
        SVertexBuilder vertexBuilder = wrr.sVertexBuilder;

        if (vertexBuilder.vertexSize == 14)
        {
            if (wrr.drawMode == 7 && wrr.vertexCount % 4 == 0)
            {
                vertexBuilder.calcNormal(wrr, wrr.getBufferSize() - 4 * vertexBuilder.vertexSize);
            }

            long entityData = vertexBuilder.entityData[vertexBuilder.entityDataIndex];
            int entityOffset = wrr.getBufferSize() - 14 + 12;
            wrr.rawIntBuffer.put(entityOffset, (int)entityData);
            wrr.rawIntBuffer.put(entityOffset + 1, (int)(entityData >> 32));
        }
    }

    public static void beginAddVertexData(WorldRenderer wrr, int[] data)
    {
        if (wrr.vertexCount == 0)
        {
            endSetVertexFormat(wrr);
        }

        SVertexBuilder vertexBuilder = wrr.sVertexBuilder;

        if (vertexBuilder.vertexSize == 14)
        {
            long entityData = vertexBuilder.entityData[vertexBuilder.entityDataIndex];

            for (int entityOffset = 12; entityOffset + 1 < data.length; entityOffset += 14)
            {
                data[entityOffset] = (int)entityData;
                data[entityOffset + 1] = (int)(entityData >> 32);
            }
        }
    }

    public static void beginAddVertexData(WorldRenderer wrr, ByteBuffer byteBuffer)
    {
        if (wrr.vertexCount == 0)
        {
            endSetVertexFormat(wrr);
        }

        SVertexBuilder vertexBuilder = wrr.sVertexBuilder;

        if (vertexBuilder.vertexSize == 14)
        {
            long entityData = vertexBuilder.entityData[vertexBuilder.entityDataIndex];
            int intLimit = byteBuffer.limit() / 4;

            for (int entityOffset = 12; entityOffset + 1 < intLimit; entityOffset += 14)
            {
                int entityLow = (int)entityData;
                int entityHigh = (int)(entityData >> 32);
                byteBuffer.putInt(entityOffset * 4, entityLow);
                byteBuffer.putInt((entityOffset + 1) * 4, entityHigh);
            }
        }
    }

    public static void endAddVertexData(WorldRenderer wrr)
    {
        SVertexBuilder vertexBuilder = wrr.sVertexBuilder;

        if (vertexBuilder.vertexSize == 14 && wrr.drawMode == 7 && wrr.vertexCount % 4 == 0)
        {
            vertexBuilder.calcNormal(wrr, wrr.getBufferSize() - 4 * vertexBuilder.vertexSize);
        }
    }

    public void calcNormal(WorldRenderer wrr, int baseIndex)
    {
        FloatBuffer floatBuffer = wrr.rawFloatBuffer;
        IntBuffer intBuffer = wrr.rawIntBuffer;
        int bufferSize = wrr.getBufferSize();
        float v0x = floatBuffer.get(baseIndex + 0 * this.vertexSize);
        float v0y = floatBuffer.get(baseIndex + 0 * this.vertexSize + 1);
        float v0z = floatBuffer.get(baseIndex + 0 * this.vertexSize + 2);
        float v0u = floatBuffer.get(baseIndex + 0 * this.vertexSize + this.offsetUV);
        float v0v = floatBuffer.get(baseIndex + 0 * this.vertexSize + this.offsetUV + 1);
        float v1x = floatBuffer.get(baseIndex + 1 * this.vertexSize);
        float v1y = floatBuffer.get(baseIndex + 1 * this.vertexSize + 1);
        float v1z = floatBuffer.get(baseIndex + 1 * this.vertexSize + 2);
        float v1u = floatBuffer.get(baseIndex + 1 * this.vertexSize + this.offsetUV);
        float v1v = floatBuffer.get(baseIndex + 1 * this.vertexSize + this.offsetUV + 1);
        float v2x = floatBuffer.get(baseIndex + 2 * this.vertexSize);
        float v2y = floatBuffer.get(baseIndex + 2 * this.vertexSize + 1);
        float v2z = floatBuffer.get(baseIndex + 2 * this.vertexSize + 2);
        float v2u = floatBuffer.get(baseIndex + 2 * this.vertexSize + this.offsetUV);
        float v2v = floatBuffer.get(baseIndex + 2 * this.vertexSize + this.offsetUV + 1);
        float v3x = floatBuffer.get(baseIndex + 3 * this.vertexSize);
        float v3y = floatBuffer.get(baseIndex + 3 * this.vertexSize + 1);
        float v3z = floatBuffer.get(baseIndex + 3 * this.vertexSize + 2);
        float v3u = floatBuffer.get(baseIndex + 3 * this.vertexSize + this.offsetUV);
        float v3v = floatBuffer.get(baseIndex + 3 * this.vertexSize + this.offsetUV + 1);
        float diagonal02X = v2x - v0x;
        float diagonal02Y = v2y - v0y;
        float diagonal02Z = v2z - v0z;
        float diagonal13X = v3x - v1x;
        float diagonal13Y = v3y - v1y;
        float diagonal13Z = v3z - v1z;
        float normalX = diagonal02Y * diagonal13Z - diagonal13Y * diagonal02Z;
        float normalY = diagonal02Z * diagonal13X - diagonal13Z * diagonal02X;
        float normalZ = diagonal02X * diagonal13Y - diagonal13X * diagonal02Y;
        float lengthSq = normalX * normalX + normalY * normalY + normalZ * normalZ;
        float normalizeScale = (double)lengthSq != 0.0D ? (float)(1.0D / Math.sqrt((double)lengthSq)) : 1.0F;
        normalX = normalX * normalizeScale;
        normalY = normalY * normalizeScale;
        normalZ = normalZ * normalizeScale;
        float edge1X = v1x - v0x;
        float edge1Y = v1y - v0y;
        float edge1Z = v1z - v0z;
        float edge1U = v1u - v0u;
        float edge1V = v1v - v0v;
        float edge2X = v2x - v0x;
        float edge2Y = v2y - v0y;
        float edge2Z = v2z - v0z;
        float edge2U = v2u - v0u;
        float edge2V = v2v - v0v;
        float uvDeterminant = edge1U * edge2V - edge2U * edge1V;
        float inverseDeterminant = uvDeterminant != 0.0F ? 1.0F / uvDeterminant : 1.0F;
        float tangentX = (edge2V * edge1X - edge1V * edge2X) * inverseDeterminant;
        float tangentY = (edge2V * edge1Y - edge1V * edge2Y) * inverseDeterminant;
        float tangentZ = (edge2V * edge1Z - edge1V * edge2Z) * inverseDeterminant;
        float bitangentX = (edge1U * edge2X - edge2U * edge1X) * inverseDeterminant;
        float bitangentY = (edge1U * edge2Y - edge2U * edge1Y) * inverseDeterminant;
        float bitangentZ = (edge1U * edge2Z - edge2U * edge1Z) * inverseDeterminant;
        lengthSq = tangentX * tangentX + tangentY * tangentY + tangentZ * tangentZ;
        normalizeScale = (double)lengthSq != 0.0D ? (float)(1.0D / Math.sqrt((double)lengthSq)) : 1.0F;
        tangentX = tangentX * normalizeScale;
        tangentY = tangentY * normalizeScale;
        tangentZ = tangentZ * normalizeScale;
        lengthSq = bitangentX * bitangentX + bitangentY * bitangentY + bitangentZ * bitangentZ;
        normalizeScale = (double)lengthSq != 0.0D ? (float)(1.0D / Math.sqrt((double)lengthSq)) : 1.0F;
        bitangentX = bitangentX * normalizeScale;
        bitangentY = bitangentY * normalizeScale;
        bitangentZ = bitangentZ * normalizeScale;
        float handednessX = normalZ * tangentY - normalY * tangentZ;
        float handednessY = normalX * tangentZ - normalZ * tangentX;
        float handednessZ = normalY * tangentX - normalX * tangentY;
        float tangentSign = bitangentX * handednessX + bitangentY * handednessY + bitangentZ * handednessZ < 0.0F ? -1.0F : 1.0F;
        int normalXByte = (int)(normalX * 127.0F) & 255;
        int normalYByte = (int)(normalY * 127.0F) & 255;
        int normalZByte = (int)(normalZ * 127.0F) & 255;
        int packedNormal = (normalZByte << 16) + (normalYByte << 8) + normalXByte;
        intBuffer.put(baseIndex + 0 * this.vertexSize + this.offsetNormal, packedNormal);
        intBuffer.put(baseIndex + 1 * this.vertexSize + this.offsetNormal, packedNormal);
        intBuffer.put(baseIndex + 2 * this.vertexSize + this.offsetNormal, packedNormal);
        intBuffer.put(baseIndex + 3 * this.vertexSize + this.offsetNormal, packedNormal);
        int packedTangentXY = ((int)(tangentX * 32767.0F) & 65535) + (((int)(tangentY * 32767.0F) & 65535) << 16);
        int packedTangentZW = ((int)(tangentZ * 32767.0F) & 65535) + (((int)(tangentSign * 32767.0F) & 65535) << 16);
        intBuffer.put(baseIndex + 0 * this.vertexSize + 10, packedTangentXY);
        intBuffer.put(baseIndex + 0 * this.vertexSize + 10 + 1, packedTangentZW);
        intBuffer.put(baseIndex + 1 * this.vertexSize + 10, packedTangentXY);
        intBuffer.put(baseIndex + 1 * this.vertexSize + 10 + 1, packedTangentZW);
        intBuffer.put(baseIndex + 2 * this.vertexSize + 10, packedTangentXY);
        intBuffer.put(baseIndex + 2 * this.vertexSize + 10 + 1, packedTangentZW);
        intBuffer.put(baseIndex + 3 * this.vertexSize + 10, packedTangentXY);
        intBuffer.put(baseIndex + 3 * this.vertexSize + 10 + 1, packedTangentZW);
        float midU = (v0u + v1u + v2u + v3u) / 4.0F;
        float midV = (v0v + v1v + v2v + v3v) / 4.0F;
        floatBuffer.put(baseIndex + 0 * this.vertexSize + 8, midU);
        floatBuffer.put(baseIndex + 0 * this.vertexSize + 8 + 1, midV);
        floatBuffer.put(baseIndex + 1 * this.vertexSize + 8, midU);
        floatBuffer.put(baseIndex + 1 * this.vertexSize + 8 + 1, midV);
        floatBuffer.put(baseIndex + 2 * this.vertexSize + 8, midU);
        floatBuffer.put(baseIndex + 2 * this.vertexSize + 8 + 1, midV);
        floatBuffer.put(baseIndex + 3 * this.vertexSize + 8, midU);
        floatBuffer.put(baseIndex + 3 * this.vertexSize + 8 + 1, midV);
    }

    public static void calcNormalChunkLayer(WorldRenderer wrr)
    {
        if (wrr.getVertexFormat().hasNormal() && wrr.drawMode == 7 && wrr.vertexCount % 4 == 0)
        {
            SVertexBuilder vertexBuilder = wrr.sVertexBuilder;
            endSetVertexFormat(wrr);
            int bufferSize = wrr.vertexCount * vertexBuilder.vertexSize;

            for (int quadBaseIndex = 0; quadBaseIndex < bufferSize; quadBaseIndex += vertexBuilder.vertexSize * 4)
            {
                vertexBuilder.calcNormal(wrr, quadBaseIndex);
            }
        }
    }

    public static void drawArrays(int drawMode, int first, int count, WorldRenderer wrr)
    {
        if (count != 0)
        {
            VertexFormat vertexFormat = wrr.getVertexFormat();
            int vertexStride = vertexFormat.getNextOffset();

            if (vertexStride == 56)
            {
                GL20.glVertexAttribPointer(Shaders.midTexCoordAttrib, 2, GL11.GL_FLOAT, false, vertexStride, 32L);
                GL20.glVertexAttribPointer(Shaders.tangentAttrib, 4, GL11.GL_SHORT, false, vertexStride, 40L);
                GL20.glVertexAttribPointer(Shaders.entityAttrib, 3, GL11.GL_SHORT, false, vertexStride, 48L);
                GL20.glEnableVertexAttribArray(Shaders.midTexCoordAttrib);
                GL20.glEnableVertexAttribArray(Shaders.tangentAttrib);
                GL20.glEnableVertexAttribArray(Shaders.entityAttrib);
                GlStateManager.glDrawArrays(drawMode, first, count);
                GL20.glDisableVertexAttribArray(Shaders.midTexCoordAttrib);
                GL20.glDisableVertexAttribArray(Shaders.tangentAttrib);
                GL20.glDisableVertexAttribArray(Shaders.entityAttrib);
            }
            else
            {
                GlStateManager.glDrawArrays(drawMode, first, count);
            }
        }
    }
}
