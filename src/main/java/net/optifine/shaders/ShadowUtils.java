package net.optifine.shaders;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class ShadowUtils
{
    public static Iterator<RenderChunk> makeShadowChunkIterator(WorldClient world, double partialTicks, Entity viewEntity, int renderDistanceChunks, ViewFrustum viewFrustum)
    {
        float shadowRenderDistance = Shaders.getShadowRenderDistance();

        if (shadowRenderDistance > 0.0F && shadowRenderDistance < (float)((renderDistanceChunks - 1) * 16))
        {
            int shadowDistanceChunks = MathHelper.ceiling_float_int(shadowRenderDistance / 16.0F) + 1;
            float celestialAngle = world.getCelestialAngleRadians((float)partialTicks);
            float sunPathRotation = Shaders.sunPathRotation * MathHelper.DEG_TO_RAD;
            float sunAngle = celestialAngle > MathHelper.HALF_PI && celestialAngle < 3.0F * MathHelper.HALF_PI ? celestialAngle + MathHelper.PI : celestialAngle;
            float sunVectorX = -MathHelper.sin(sunAngle);
            float sunVectorY = MathHelper.cos(sunAngle) * MathHelper.cos(sunPathRotation);
            float sunVectorZ = -MathHelper.cos(sunAngle) * MathHelper.sin(sunPathRotation);
            BlockPos viewChunkPos = new BlockPos(MathHelper.floor_double(viewEntity.posX) >> 4, MathHelper.floor_double(viewEntity.posY) >> 4, MathHelper.floor_double(viewEntity.posZ) >> 4);
            BlockPos shadowStartChunkPos = viewChunkPos.add((double)(-sunVectorX * (float)shadowDistanceChunks), (double)(-sunVectorY * (float)shadowDistanceChunks), (double)(-sunVectorZ * (float)shadowDistanceChunks));
            BlockPos shadowEndChunkPos = viewChunkPos.add((double)(sunVectorX * (float)renderDistanceChunks), (double)(sunVectorY * (float)renderDistanceChunks), (double)(sunVectorZ * (float)renderDistanceChunks));
            IteratorRenderChunks iteratorRenderChunks = new IteratorRenderChunks(viewFrustum, shadowStartChunkPos, shadowEndChunkPos, shadowDistanceChunks, shadowDistanceChunks);
            return iteratorRenderChunks;
        }
        else
        {
            List<RenderChunk> chunkList = Arrays.<RenderChunk>asList(viewFrustum.renderChunks);
            Iterator<RenderChunk> chunkIterator = chunkList.iterator();
            return chunkIterator;
        }
    }
}
