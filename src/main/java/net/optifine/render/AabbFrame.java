package net.optifine.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.AxisAlignedBB;

public class AabbFrame extends AxisAlignedBB
{
    private int frameCount = -1;
    private boolean inFrustumFully = false;

    public AabbFrame(double doubleValue, double thirdDoubleValue, double fifthDoubleValue, double secondDoubleValue, double fourthDoubleValue, double sixthDoubleValue)
    {
        super(doubleValue, thirdDoubleValue, fifthDoubleValue, secondDoubleValue, fourthDoubleValue, sixthDoubleValue);
    }

    public boolean isBoundingBoxInFrustumFully(ICamera camera, int frameCount)
    {
        if (this.frameCount != frameCount)
        {
            this.inFrustumFully = camera instanceof Frustum ? ((Frustum)camera).isBoxInFrustumFully(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ) : false;
            this.frameCount = frameCount;
        }

        return this.inFrustumFully;
    }
}
