package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;

public class Teleporter
{
    private final WorldServer worldServerInstance;
    private final Random random;
    private final LongHashMap<Teleporter.PortalPosition> destinationCoordinateCache = new LongHashMap();
    private final List<Long> destinationCoordinateKeys = Lists.<Long>newArrayList();

    public Teleporter(WorldServer worldIn)
    {
        this.worldServerInstance = worldIn;
        this.random = new Random(worldIn.getSeed());
    }

    public void placeInPortal(Entity entityIn, float rotationYaw)
    {
        if (this.worldServerInstance.provider.getDimensionId() != 1)
        {
            if (!this.placeInExistingPortal(entityIn, rotationYaw))
            {
                this.makePortal(entityIn);
                this.placeInExistingPortal(entityIn, rotationYaw);
            }
        }
        else
        {
            int platformX = MathHelper.floor_double(entityIn.posX);
            int platformY = MathHelper.floor_double(entityIn.posY) - 1;
            int platformZ = MathHelper.floor_double(entityIn.posZ);
            int xDirection = 1;
            int zDirection = 0;

            for (int innerIndex = -2; innerIndex <= 2; ++innerIndex)
            {
                for (int nestedIndex = -2; nestedIndex <= 2; ++nestedIndex)
                {
                    for (int outerIndex = -1; outerIndex < 3; ++outerIndex)
                    {
                        int blockX = platformX + nestedIndex * xDirection + innerIndex * zDirection;
                        int blockY = platformY + outerIndex;
                        int blockZ = platformZ + nestedIndex * zDirection - innerIndex * xDirection;
                        boolean needsObsidian = outerIndex < 0;
                        this.worldServerInstance.setBlockState(new BlockPos(blockX, blockY, blockZ), needsObsidian ? Blocks.obsidian.getDefaultState() : Blocks.air.getDefaultState());
                    }
                }
            }

            entityIn.setLocationAndAngles((double)platformX, (double)platformY, (double)platformZ, entityIn.rotationYaw, 0.0F);
            entityIn.motionX = entityIn.motionY = entityIn.motionZ = 0.0D;
        }
    }

    public boolean placeInExistingPortal(Entity entityIn, float rotationYaw)
    {
        int searchRadius = 128;
        double bestDistanceSq = -1.0D;
        int entityBlockX = MathHelper.floor_double(entityIn.posX);
        int entityBlockZ = MathHelper.floor_double(entityIn.posZ);
        boolean cacheMiss = true;
        BlockPos portalPos = BlockPos.ORIGIN;
        long cacheKey = ChunkCoordIntPair.chunkXZ2Int(entityBlockX, entityBlockZ);

        if (this.destinationCoordinateCache.containsItem(cacheKey))
        {
            Teleporter.PortalPosition cachedPortalPosition = (Teleporter.PortalPosition)this.destinationCoordinateCache.getValueByKey(cacheKey);
            bestDistanceSq = 0.0D;
            portalPos = cachedPortalPosition;
            cachedPortalPosition.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
            cacheMiss = false;
        }
        else
        {
            BlockPos entityPos = new BlockPos(entityIn);

            for (int xOffset = -searchRadius; xOffset <= searchRadius; ++xOffset)
            {
                BlockPos belowPos;

                for (int zOffset = -searchRadius; zOffset <= searchRadius; ++zOffset)
                {
                    for (BlockPos scanPos = entityPos.add(xOffset, this.worldServerInstance.getActualHeight() - 1 - entityPos.getY(), zOffset); scanPos.getY() >= 0; scanPos = belowPos)
                    {
                        belowPos = scanPos.down();

                        if (this.worldServerInstance.getBlockState(scanPos).getBlock() == Blocks.portal)
                        {
                            while (this.worldServerInstance.getBlockState(belowPos = scanPos.down()).getBlock() == Blocks.portal)
                            {
                                scanPos = belowPos;
                            }

                            double candidateDistanceSq = scanPos.distanceSq(entityPos);

                            if (bestDistanceSq < 0.0D || candidateDistanceSq < bestDistanceSq)
                            {
                                bestDistanceSq = candidateDistanceSq;
                                portalPos = scanPos;
                            }
                        }
                    }
                }
            }
        }

        if (bestDistanceSq >= 0.0D)
        {
            if (cacheMiss)
            {
                this.destinationCoordinateCache.add(cacheKey, new Teleporter.PortalPosition(portalPos, this.worldServerInstance.getTotalWorldTime()));
                this.destinationCoordinateKeys.add(Long.valueOf(cacheKey));
            }

            double targetX = (double)portalPos.getX() + 0.5D;
            double targetY = (double)portalPos.getY() + 0.5D;
            double targetZ = (double)portalPos.getZ() + 0.5D;
            BlockPattern.PatternHelper blockpattern$patternhelper = Blocks.portal.createPatternHelper(this.worldServerInstance, portalPos);
            boolean portalFacesNegative = blockpattern$patternhelper.getFinger().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
            double portalAxisCoordinate = blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getPos().getZ() : (double)blockpattern$patternhelper.getPos().getX();
            targetY = (double)(blockpattern$patternhelper.getPos().getY() + 1) - entityIn.getLastPortalVec().yCoord * (double)blockpattern$patternhelper.getThumbLength();

            if (portalFacesNegative)
            {
                ++portalAxisCoordinate;
            }

            if (blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X)
            {
                targetZ = portalAxisCoordinate + (1.0D - entityIn.getLastPortalVec().xCoord) * (double)blockpattern$patternhelper.getPalmLength() * (double)blockpattern$patternhelper.getFinger().rotateY().getAxisDirection().getOffset();
            }
            else
            {
                targetX = portalAxisCoordinate + (1.0D - entityIn.getLastPortalVec().xCoord) * (double)blockpattern$patternhelper.getPalmLength() * (double)blockpattern$patternhelper.getFinger().rotateY().getAxisDirection().getOffset();
            }

            float xMotionScale = 0.0F;
            float zMotionScale = 0.0F;
            float xToZMotionScale = 0.0F;
            float zToXMotionScale = 0.0F;

            if (blockpattern$patternhelper.getFinger().getOpposite() == entityIn.getTeleportDirection())
            {
                xMotionScale = 1.0F;
                zMotionScale = 1.0F;
            }
            else if (blockpattern$patternhelper.getFinger().getOpposite() == entityIn.getTeleportDirection().getOpposite())
            {
                xMotionScale = -1.0F;
                zMotionScale = -1.0F;
            }
            else if (blockpattern$patternhelper.getFinger().getOpposite() == entityIn.getTeleportDirection().rotateY())
            {
                xToZMotionScale = 1.0F;
                zToXMotionScale = -1.0F;
            }
            else
            {
                xToZMotionScale = -1.0F;
                zToXMotionScale = 1.0F;
            }

            double previousMotionX = entityIn.motionX;
            double previousMotionZ = entityIn.motionZ;
            entityIn.motionX = previousMotionX * (double)xMotionScale + previousMotionZ * (double)zToXMotionScale;
            entityIn.motionZ = previousMotionX * (double)xToZMotionScale + previousMotionZ * (double)zMotionScale;
            entityIn.rotationYaw = rotationYaw - (float)(entityIn.getTeleportDirection().getOpposite().getHorizontalIndex() * 90) + (float)(blockpattern$patternhelper.getFinger().getHorizontalIndex() * 90);
            entityIn.setLocationAndAngles(targetX, targetY, targetZ, entityIn.rotationYaw, entityIn.rotationPitch);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean makePortal(Entity entityIn)
    {
        int searchRadius = 16;
        double bestDistanceSq = -1.0D;
        int entityBlockX = MathHelper.floor_double(entityIn.posX);
        int entityBlockY = MathHelper.floor_double(entityIn.posY);
        int entityBlockZ = MathHelper.floor_double(entityIn.posZ);
        int bestPortalX = entityBlockX;
        int bestPortalY = entityBlockY;
        int bestPortalZ = entityBlockZ;
        int bestOrientation = 0;
        int randomOrientationStart = this.random.nextInt(4);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int scanX = entityBlockX - searchRadius; scanX <= entityBlockX + searchRadius; ++scanX)
        {
            double deltaX = (double)scanX + 0.5D - entityIn.posX;

            for (int scanZ = entityBlockZ - searchRadius; scanZ <= entityBlockZ + searchRadius; ++scanZ)
            {
                double deltaZ = (double)scanZ + 0.5D - entityIn.posZ;
                label142:

                for (int scanY = this.worldServerInstance.getActualHeight() - 1; scanY >= 0; --scanY)
                {
                    if (this.worldServerInstance.isAirBlock(mutablePos.set(scanX, scanY, scanZ)))
                    {
                        while (scanY > 0 && this.worldServerInstance.isAirBlock(mutablePos.set(scanX, scanY - 1, scanZ)))
                        {
                            --scanY;
                        }

                        for (int orientation = randomOrientationStart; orientation < randomOrientationStart + 4; ++orientation)
                        {
                            int axisXStep = orientation % 2;
                            int axisZStep = 1 - axisXStep;

                            if (orientation % 4 >= 2)
                            {
                                axisXStep = -axisXStep;
                                axisZStep = -axisZStep;
                            }

                            for (int portalWidthOffset = 0; portalWidthOffset < 3; ++portalWidthOffset)
                            {
                                for (int portalDepthOffset = 0; portalDepthOffset < 4; ++portalDepthOffset)
                                {
                                    for (int portalHeightOffset = -1; portalHeightOffset < 4; ++portalHeightOffset)
                                    {
                                        int candidateX = scanX + (portalDepthOffset - 1) * axisXStep + portalWidthOffset * axisZStep;
                                        int candidateY = scanY + portalHeightOffset;
                                        int candidateZ = scanZ + (portalDepthOffset - 1) * axisZStep - portalWidthOffset * axisXStep;
                                        mutablePos.set(candidateX, candidateY, candidateZ);

                                        if (portalHeightOffset < 0 && !this.worldServerInstance.getBlockState(mutablePos).getBlock().getMaterial().isSolid() || portalHeightOffset >= 0 && !this.worldServerInstance.isAirBlock(mutablePos))
                                        {
                                            continue label142;
                                        }
                                    }
                                }
                            }

                            double deltaY = (double)scanY + 0.5D - entityIn.posY;
                            double candidateDistanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

                            if (bestDistanceSq < 0.0D || candidateDistanceSq < bestDistanceSq)
                            {
                                bestDistanceSq = candidateDistanceSq;
                                bestPortalX = scanX;
                                bestPortalY = scanY;
                                bestPortalZ = scanZ;
                                bestOrientation = orientation % 4;
                            }
                        }
                    }
                }
            }
        }

        if (bestDistanceSq < 0.0D)
        {
            for (int fallbackX = entityBlockX - searchRadius; fallbackX <= entityBlockX + searchRadius; ++fallbackX)
            {
                double fallbackDeltaX = (double)fallbackX + 0.5D - entityIn.posX;

                for (int fallbackZ = entityBlockZ - searchRadius; fallbackZ <= entityBlockZ + searchRadius; ++fallbackZ)
                {
                    double fallbackDeltaZ = (double)fallbackZ + 0.5D - entityIn.posZ;
                    label562:

                    for (int fallbackY = this.worldServerInstance.getActualHeight() - 1; fallbackY >= 0; --fallbackY)
                    {
                        if (this.worldServerInstance.isAirBlock(mutablePos.set(fallbackX, fallbackY, fallbackZ)))
                        {
                            while (fallbackY > 0 && this.worldServerInstance.isAirBlock(mutablePos.set(fallbackX, fallbackY - 1, fallbackZ)))
                            {
                                --fallbackY;
                            }

                            for (int fallbackOrientation = randomOrientationStart; fallbackOrientation < randomOrientationStart + 2; ++fallbackOrientation)
                            {
                                int fallbackAxisXStep = fallbackOrientation % 2;
                                int fallbackAxisZStep = 1 - fallbackAxisXStep;

                                for (int fallbackDepthOffset = 0; fallbackDepthOffset < 4; ++fallbackDepthOffset)
                                {
                                    for (int fallbackHeightOffset = -1; fallbackHeightOffset < 4; ++fallbackHeightOffset)
                                    {
                                        int candidateX = fallbackX + (fallbackDepthOffset - 1) * fallbackAxisXStep;
                                        int candidateY = fallbackY + fallbackHeightOffset;
                                        int candidateZ = fallbackZ + (fallbackDepthOffset - 1) * fallbackAxisZStep;
                                        mutablePos.set(candidateX, candidateY, candidateZ);

                                        if (fallbackHeightOffset < 0 && !this.worldServerInstance.getBlockState(mutablePos).getBlock().getMaterial().isSolid() || fallbackHeightOffset >= 0 && !this.worldServerInstance.isAirBlock(mutablePos))
                                        {
                                            continue label562;
                                        }
                                    }
                                }

                                double fallbackDeltaY = (double)fallbackY + 0.5D - entityIn.posY;
                                double fallbackDistanceSq = fallbackDeltaX * fallbackDeltaX + fallbackDeltaY * fallbackDeltaY + fallbackDeltaZ * fallbackDeltaZ;

                                if (bestDistanceSq < 0.0D || fallbackDistanceSq < bestDistanceSq)
                                {
                                    bestDistanceSq = fallbackDistanceSq;
                                    bestPortalX = fallbackX;
                                    bestPortalY = fallbackY;
                                    bestPortalZ = fallbackZ;
                                    bestOrientation = fallbackOrientation % 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        int portalBaseX = bestPortalX;
        int portalBaseY = bestPortalY;
        int portalBaseZ = bestPortalZ;
        int portalAxisXStep = bestOrientation % 2;
        int portalAxisZStep = 1 - portalAxisXStep;

        if (bestOrientation % 4 >= 2)
        {
            portalAxisXStep = -portalAxisXStep;
            portalAxisZStep = -portalAxisZStep;
        }

        if (bestDistanceSq < 0.0D)
        {
            bestPortalY = MathHelper.clamp_int(bestPortalY, 70, this.worldServerInstance.getActualHeight() - 10);
            portalBaseY = bestPortalY;

            for (int sideOffset = -1; sideOffset <= 1; ++sideOffset)
            {
                for (int depthOffset = 1; depthOffset < 3; ++depthOffset)
                {
                    for (int heightOffset = -1; heightOffset < 3; ++heightOffset)
                    {
                        int supportX = portalBaseX + (depthOffset - 1) * portalAxisXStep + sideOffset * portalAxisZStep;
                        int supportY = portalBaseY + heightOffset;
                        int supportZ = portalBaseZ + (depthOffset - 1) * portalAxisZStep - sideOffset * portalAxisXStep;
                        boolean needsObsidian = heightOffset < 0;
                        this.worldServerInstance.setBlockState(new BlockPos(supportX, supportY, supportZ), needsObsidian ? Blocks.obsidian.getDefaultState() : Blocks.air.getDefaultState());
                    }
                }
            }
        }

        IBlockState portalState = Blocks.portal.getDefaultState().withProperty(BlockPortal.AXIS, portalAxisXStep != 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);

        for (int constructionPass = 0; constructionPass < 4; ++constructionPass)
        {
            for (int frameDepthOffset = 0; frameDepthOffset < 4; ++frameDepthOffset)
            {
                for (int frameHeightOffset = -1; frameHeightOffset < 4; ++frameHeightOffset)
                {
                    int frameX = portalBaseX + (frameDepthOffset - 1) * portalAxisXStep;
                    int frameY = portalBaseY + frameHeightOffset;
                    int frameZ = portalBaseZ + (frameDepthOffset - 1) * portalAxisZStep;
                    boolean frameBlock = frameDepthOffset == 0 || frameDepthOffset == 3 || frameHeightOffset == -1 || frameHeightOffset == 3;
                    this.worldServerInstance.setBlockState(new BlockPos(frameX, frameY, frameZ), frameBlock ? Blocks.obsidian.getDefaultState() : portalState, 2);
                }
            }

            for (int notifyDepthOffset = 0; notifyDepthOffset < 4; ++notifyDepthOffset)
            {
                for (int notifyHeightOffset = -1; notifyHeightOffset < 4; ++notifyHeightOffset)
                {
                    int notifyX = portalBaseX + (notifyDepthOffset - 1) * portalAxisXStep;
                    int notifyY = portalBaseY + notifyHeightOffset;
                    int notifyZ = portalBaseZ + (notifyDepthOffset - 1) * portalAxisZStep;
                    BlockPos notifyPos = new BlockPos(notifyX, notifyY, notifyZ);
                    this.worldServerInstance.notifyNeighborsOfStateChange(notifyPos, this.worldServerInstance.getBlockState(notifyPos).getBlock());
                }
            }
        }

        return true;
    }

    public void removeStalePortalLocations(long worldTime)
    {
        if (worldTime % 100L == 0L)
        {
            Iterator<Long> cacheKeyIterator = this.destinationCoordinateKeys.iterator();
            long staleCutoffTime = worldTime - 300L;

            while (cacheKeyIterator.hasNext())
            {
                Long cachedKey = (Long)cacheKeyIterator.next();
                Teleporter.PortalPosition cachedPortalPosition = (Teleporter.PortalPosition)this.destinationCoordinateCache.getValueByKey(cachedKey.longValue());

                if (cachedPortalPosition == null || cachedPortalPosition.lastUpdateTime < staleCutoffTime)
                {
                    cacheKeyIterator.remove();
                    this.destinationCoordinateCache.remove(cachedKey.longValue());
                }
            }
        }
    }

    public class PortalPosition extends BlockPos
    {
        public long lastUpdateTime;

        public PortalPosition(BlockPos pos, long lastUpdate)
        {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.lastUpdateTime = lastUpdate;
        }
    }
}
