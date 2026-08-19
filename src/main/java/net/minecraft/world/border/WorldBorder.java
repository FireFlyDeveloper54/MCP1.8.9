package net.minecraft.world.border;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

public class WorldBorder
{
    private final List<IBorderListener> listeners = Lists.<IBorderListener>newArrayList();
    private double centerX = 0.0D;
    private double centerZ = 0.0D;
    private double startDiameter = 6.0E7D;
    private double endDiameter;
    private long endTime;
    private long startTime;
    private int worldSize;
    private double damageAmount;
    private double damageBuffer;
    private int warningTime;
    private int warningDistance;

    public WorldBorder()
    {
        this.endDiameter = this.startDiameter;
        this.worldSize = 29999984;
        this.damageAmount = 0.2D;
        this.damageBuffer = 5.0D;
        this.warningTime = 15;
        this.warningDistance = 5;
    }

    public boolean contains(BlockPos pos)
    {
        return (double)(pos.getX() + 1) > this.minX() && (double)pos.getX() < this.maxX() && (double)(pos.getZ() + 1) > this.minZ() && (double)pos.getZ() < this.maxZ();
    }

    public boolean contains(ChunkCoordIntPair range)
    {
        return (double)range.getXEnd() > this.minX() && (double)range.getXStart() < this.maxX() && (double)range.getZEnd() > this.minZ() && (double)range.getZStart() < this.maxZ();
    }

    public boolean contains(AxisAlignedBB bb)
    {
        return bb.maxX > this.minX() && bb.minX < this.maxX() && bb.maxZ > this.minZ() && bb.minZ < this.maxZ();
    }

    public double getClosestDistance(Entity entityIn)
    {
        return this.getClosestDistance(entityIn.posX, entityIn.posZ);
    }

    public double getClosestDistance(double x, double z)
    {
        double distanceToMinZ = z - this.minZ();
        double distanceToMaxZ = this.maxZ() - z;
        double distanceToMinX = x - this.minX();
        double distanceToMaxX = this.maxX() - x;
        double closestXDistance = Math.min(distanceToMinX, distanceToMaxX);
        double closestDistance = Math.min(closestXDistance, distanceToMinZ);
        return Math.min(closestDistance, distanceToMaxZ);
    }

    public EnumBorderStatus getStatus()
    {
        return this.endDiameter < this.startDiameter ? EnumBorderStatus.SHRINKING : (this.endDiameter > this.startDiameter ? EnumBorderStatus.GROWING : EnumBorderStatus.STATIONARY);
    }

    public double minX()
    {
        double borderMinX = this.getCenterX() - this.getDiameter() / 2.0D;

        if (borderMinX < (double)(-this.worldSize))
        {
            borderMinX = (double)(-this.worldSize);
        }

        return borderMinX;
    }

    public double minZ()
    {
        double borderMinZ = this.getCenterZ() - this.getDiameter() / 2.0D;

        if (borderMinZ < (double)(-this.worldSize))
        {
            borderMinZ = (double)(-this.worldSize);
        }

        return borderMinZ;
    }

    public double maxX()
    {
        double borderMaxX = this.getCenterX() + this.getDiameter() / 2.0D;

        if (borderMaxX > (double)this.worldSize)
        {
            borderMaxX = (double)this.worldSize;
        }

        return borderMaxX;
    }

    public double maxZ()
    {
        double borderMaxZ = this.getCenterZ() + this.getDiameter() / 2.0D;

        if (borderMaxZ > (double)this.worldSize)
        {
            borderMaxZ = (double)this.worldSize;
        }

        return borderMaxZ;
    }

    public double getCenterX()
    {
        return this.centerX;
    }

    public double getCenterZ()
    {
        return this.centerZ;
    }

    public void setCenter(double x, double z)
    {
        this.centerX = x;
        this.centerZ = z;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onCenterChanged(this, x, z);
        }
    }

    public double getDiameter()
    {
        if (this.getStatus() != EnumBorderStatus.STATIONARY)
        {
            double progress = (double)((float)(System.currentTimeMillis() - this.startTime) / (float)(this.endTime - this.startTime));

            if (progress < 1.0D)
            {
                return this.startDiameter + (this.endDiameter - this.startDiameter) * progress;
            }

            this.setTransition(this.endDiameter);
        }

        return this.startDiameter;
    }

    public long getTimeUntilTarget()
    {
        return this.getStatus() != EnumBorderStatus.STATIONARY ? this.endTime - System.currentTimeMillis() : 0L;
    }

    public double getTargetSize()
    {
        return this.endDiameter;
    }

    public void setTransition(double newSize)
    {
        this.startDiameter = newSize;
        this.endDiameter = newSize;
        this.endTime = System.currentTimeMillis();
        this.startTime = this.endTime;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onSizeChanged(this, newSize);
        }
    }

    public void setTransition(double oldSize, double newSize, long time)
    {
        this.startDiameter = oldSize;
        this.endDiameter = newSize;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + time;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onTransitionStarted(this, oldSize, newSize, time);
        }
    }

    protected List<IBorderListener> getListeners()
    {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(IBorderListener listener)
    {
        this.listeners.add(listener);
    }

    public void setSize(int size)
    {
        this.worldSize = size;
    }

    public int getSize()
    {
        return this.worldSize;
    }

    public double getDamageBuffer()
    {
        return this.damageBuffer;
    }

    public void setDamageBuffer(double bufferSize)
    {
        this.damageBuffer = bufferSize;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onDamageBufferChanged(this, bufferSize);
        }
    }

    public double getDamageAmount()
    {
        return this.damageAmount;
    }

    public void setDamageAmount(double newAmount)
    {
        this.damageAmount = newAmount;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onDamageAmountChanged(this, newAmount);
        }
    }

    public double getResizeSpeed()
    {
        return this.endTime == this.startTime ? 0.0D : Math.abs(this.startDiameter - this.endDiameter) / (double)(this.endTime - this.startTime);
    }

    public int getWarningTime()
    {
        return this.warningTime;
    }

    public void setWarningTime(int warningTime)
    {
        this.warningTime = warningTime;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onWarningTimeChanged(this, warningTime);
        }
    }

    public int getWarningDistance()
    {
        return this.warningDistance;
    }

    public void setWarningDistance(int warningDistance)
    {
        this.warningDistance = warningDistance;

        for (IBorderListener borderListener : this.getListeners())
        {
            borderListener.onWarningDistanceChanged(this, warningDistance);
        }
    }
}
