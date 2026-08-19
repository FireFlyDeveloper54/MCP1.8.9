package net.optifine;

import java.util.Comparator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;

public class ChunkPosComparator implements Comparator<ChunkCoordIntPair>
{
    private int chunkPosX;
    private int chunkPosZ;
    private double yawRadians;
    private double pitchWeight;

    public ChunkPosComparator(int chunkPosX, int chunkPosZ, double yawRad, double pitchRad)
    {
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        this.yawRadians = yawRad;
        this.pitchWeight = 1.0D - MathHelper.clamp_double(Math.abs(pitchRad) / (Math.PI / 2D), 0.0D, 1.0D);
    }

    public int compare(ChunkCoordIntPair firstChunk, ChunkCoordIntPair secondChunk)
    {
        int firstDistance = this.getDistanceScore(firstChunk);
        int secondDistance = this.getDistanceScore(secondChunk);
        return firstDistance - secondDistance;
    }

    private int getDistanceScore(ChunkCoordIntPair chunk)
    {
        int deltaX = chunk.chunkXPos - this.chunkPosX;
        int deltaZ = chunk.chunkZPos - this.chunkPosZ;
        int distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        double angle = MathHelper.atan2((double)deltaZ, (double)deltaX);
        double angleDelta = Math.abs(angle - this.yawRadians);

        if (angleDelta > Math.PI)
        {
            angleDelta = (Math.PI * 2D) - angleDelta;
        }

        distanceSquared = (int)((double)distanceSquared * 1000.0D * this.pitchWeight * angleDelta * angleDelta);
        return distanceSquared;
    }
}
