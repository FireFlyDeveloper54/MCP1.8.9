package net.minecraft.client.renderer.culling;

import net.minecraft.util.AxisAlignedBB;

public class Frustum implements ICamera
{
    private ClippingHelper clippingHelper;
    private double xPosition;
    private double yPosition;
    private double zPosition;

    public Frustum()
    {
        this(ClippingHelperImpl.getInstance());
    }

    public Frustum(ClippingHelper clippingHelperIn)
    {
        this.clippingHelper = clippingHelperIn;
    }

    public void setPosition(double x, double y, double z)
    {
        this.xPosition = x;
        this.yPosition = y;
        this.zPosition = z;
    }

    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return this.clippingHelper.isBoxInFrustum(minX - this.xPosition, minY - this.yPosition, minZ - this.zPosition, maxX - this.xPosition, maxY - this.yPosition, maxZ - this.zPosition);
    }

    public boolean isBoundingBoxInFrustum(AxisAlignedBB boundingBox)
    {
        return this.isBoxInFrustum(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
    }

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return this.clippingHelper.isBoxInFrustumFully(minX - this.xPosition, minY - this.yPosition, minZ - this.zPosition, maxX - this.xPosition, maxY - this.yPosition, maxZ - this.zPosition);
    }
}
