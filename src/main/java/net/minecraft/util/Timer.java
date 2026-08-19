package net.minecraft.util;

import net.minecraft.client.Minecraft;

public class Timer
{
    float ticksPerSecond;
    private double lastHRTime;
    public int elapsedTicks;
    public float renderPartialTicks;
    public float timerSpeed = 1.0F;
    public float elapsedPartialTicks;
    private long lastSyncSysClock;
    private long lastSyncHRClock;
    private long counter;
    private double timeSyncAdjustment = 1.0D;

    public Timer(float tps)
    {
        this.ticksPerSecond = tps;
        this.lastSyncSysClock = Minecraft.getSystemTime();
        this.lastSyncHRClock = System.nanoTime() / 1000000L;
    }

    public void updateTimer()
    {
        long currentSysClock = Minecraft.getSystemTime();
        long elapsedSysClock = currentSysClock - this.lastSyncSysClock;
        long currentHrClock = System.nanoTime() / 1000000L;
        double currentHrTime = (double)currentHrClock / 1000.0D;

        if (elapsedSysClock <= 1000L && elapsedSysClock >= 0L)
        {
            this.counter += elapsedSysClock;

            if (this.counter > 1000L)
            {
                long elapsedHrClock = currentHrClock - this.lastSyncHRClock;
                double syncRatio = (double)this.counter / (double)elapsedHrClock;
                this.timeSyncAdjustment += (syncRatio - this.timeSyncAdjustment) * 0.20000000298023224D;
                this.lastSyncHRClock = currentHrClock;
                this.counter = 0L;
            }

            if (this.counter < 0L)
            {
                this.lastSyncHRClock = currentHrClock;
            }
        }
        else
        {
            this.lastHRTime = currentHrTime;
        }

        this.lastSyncSysClock = currentSysClock;
        double elapsedHrTime = (currentHrTime - this.lastHRTime) * this.timeSyncAdjustment;
        this.lastHRTime = currentHrTime;
        elapsedHrTime = MathHelper.clamp_double(elapsedHrTime, 0.0D, 1.0D);
        this.elapsedPartialTicks = (float)((double)this.elapsedPartialTicks + elapsedHrTime * (double)this.timerSpeed * (double)this.ticksPerSecond);
        this.elapsedTicks = (int)this.elapsedPartialTicks;
        this.elapsedPartialTicks -= (float)this.elapsedTicks;

        if (this.elapsedTicks > 10)
        {
            this.elapsedTicks = 10;
        }

        this.renderPartialTicks = this.elapsedPartialTicks;
    }
}
