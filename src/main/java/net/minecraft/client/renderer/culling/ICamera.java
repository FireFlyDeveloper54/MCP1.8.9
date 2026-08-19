package net.minecraft.client.renderer.culling;

import net.minecraft.util.AxisAlignedBB;

public interface ICamera
{
    boolean isBoundingBoxInFrustum(AxisAlignedBB boundingBox);

    void setPosition(double x, double y, double z);
}
