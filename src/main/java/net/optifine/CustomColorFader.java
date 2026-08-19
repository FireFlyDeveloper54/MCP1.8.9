package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.util.Vec3;

public class CustomColorFader
{
    private Vec3 currentColor = null;
    private long lastUpdateTime = System.currentTimeMillis();

    public Vec3 getColor(double x, double y, double z)
    {
        if (this.currentColor == null)
        {
            this.currentColor = new Vec3(x, y, z);
            return this.currentColor;
        }
        else
        {
            long currentTime = System.currentTimeMillis();
            long elapsedMillis = currentTime - this.lastUpdateTime;

            if (elapsedMillis == 0L)
            {
                return this.currentColor;
            }
            else
            {
                this.lastUpdateTime = currentTime;

                if (Math.abs(x - this.currentColor.xCoord) < 0.004D && Math.abs(y - this.currentColor.yCoord) < 0.004D && Math.abs(z - this.currentColor.zCoord) < 0.004D)
                {
                    return this.currentColor;
                }
                else
                {
                    double blendFactor = (double)elapsedMillis * 0.001D;
                    blendFactor = Config.limit(blendFactor, 0.0D, 1.0D);
                    double deltaX = x - this.currentColor.xCoord;
                    double deltaY = y - this.currentColor.yCoord;
                    double deltaZ = z - this.currentColor.zCoord;
                    double blendedX = this.currentColor.xCoord + deltaX * blendFactor;
                    double blendedY = this.currentColor.yCoord + deltaY * blendFactor;
                    double blendedZ = this.currentColor.zCoord + deltaZ * blendFactor;
                    this.currentColor = new Vec3(blendedX, blendedY, blendedZ);
                    return this.currentColor;
                }
            }
        }
    }
}
