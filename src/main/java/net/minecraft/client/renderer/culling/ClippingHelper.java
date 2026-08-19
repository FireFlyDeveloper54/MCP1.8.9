package net.minecraft.client.renderer.culling;

public class ClippingHelper
{
    public float[][] frustum = new float[6][4];
    public float[] projectionMatrix = new float[16];
    public float[] modelviewMatrix = new float[16];
    public float[] clippingMatrix = new float[16];
    public boolean disabled = false;

    private float dot(float[] plane, float x, float y, float z)
    {
        return plane[0] * x + plane[1] * y + plane[2] * z + plane[3];
    }

    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        if (this.disabled)
        {
            return true;
        }
        else
        {
            float minXF = (float)minX;
            float minYF = (float)minY;
            float minZF = (float)minZ;
            float maxXF = (float)maxX;
            float maxYF = (float)maxY;
            float maxZF = (float)maxZ;

            for (int planeIndex = 0; planeIndex < 6; ++planeIndex)
            {
                float[] plane = this.frustum[planeIndex];

                if (this.dot(plane, minXF, minYF, minZF) <= 0.0F && this.dot(plane, maxXF, minYF, minZF) <= 0.0F && this.dot(plane, minXF, maxYF, minZF) <= 0.0F && this.dot(plane, maxXF, maxYF, minZF) <= 0.0F && this.dot(plane, minXF, minYF, maxZF) <= 0.0F && this.dot(plane, maxXF, minYF, maxZF) <= 0.0F && this.dot(plane, minXF, maxYF, maxZF) <= 0.0F && this.dot(plane, maxXF, maxYF, maxZF) <= 0.0F)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        if (this.disabled)
        {
            return true;
        }
        else
        {
            float minXF = (float)minX;
            float minYF = (float)minY;
            float minZF = (float)minZ;
            float maxXF = (float)maxX;
            float maxYF = (float)maxY;
            float maxZF = (float)maxZ;

            for (int planeIndex = 0; planeIndex < 6; ++planeIndex)
            {
                float[] plane = this.frustum[planeIndex];

                if (planeIndex < 4)
                {
                    if (this.dot(plane, minXF, minYF, minZF) <= 0.0F || this.dot(plane, maxXF, minYF, minZF) <= 0.0F || this.dot(plane, minXF, maxYF, minZF) <= 0.0F || this.dot(plane, maxXF, maxYF, minZF) <= 0.0F || this.dot(plane, minXF, minYF, maxZF) <= 0.0F || this.dot(plane, maxXF, minYF, maxZF) <= 0.0F || this.dot(plane, minXF, maxYF, maxZF) <= 0.0F || this.dot(plane, maxXF, maxYF, maxZF) <= 0.0F)
                    {
                        return false;
                    }
                }
                else if (this.dot(plane, minXF, minYF, minZF) <= 0.0F && this.dot(plane, maxXF, minYF, minZF) <= 0.0F && this.dot(plane, minXF, maxYF, minZF) <= 0.0F && this.dot(plane, maxXF, maxYF, minZF) <= 0.0F && this.dot(plane, minXF, minYF, maxZF) <= 0.0F && this.dot(plane, maxXF, minYF, maxZF) <= 0.0F && this.dot(plane, minXF, maxYF, maxZF) <= 0.0F && this.dot(plane, maxXF, maxYF, maxZF) <= 0.0F)
                {
                    return false;
                }
            }

            return true;
        }
    }
}
