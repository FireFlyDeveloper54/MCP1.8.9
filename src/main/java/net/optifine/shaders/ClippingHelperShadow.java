package net.optifine.shaders;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.MathHelper;

public class ClippingHelperShadow extends ClippingHelper
{
    private static ClippingHelperShadow instance = new ClippingHelperShadow();
    float[] frustumTest = new float[6];
    float[][] shadowClipPlanes = new float[10][4];
    int shadowClipPlaneCount;
    float[] matInvMP = new float[16];
    float[] vecIntersection = new float[4];

    public boolean isBoxInFrustum(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        for (int planeIndex = 0; planeIndex < this.shadowClipPlaneCount; ++planeIndex)
        {
            float[] shadowClipPlane = this.shadowClipPlanes[planeIndex];

            if (this.dot4(shadowClipPlane, x1, y1, z1) <= 0.0D && this.dot4(shadowClipPlane, x2, y1, z1) <= 0.0D && this.dot4(shadowClipPlane, x1, y2, z1) <= 0.0D && this.dot4(shadowClipPlane, x2, y2, z1) <= 0.0D && this.dot4(shadowClipPlane, x1, y1, z2) <= 0.0D && this.dot4(shadowClipPlane, x2, y1, z2) <= 0.0D && this.dot4(shadowClipPlane, x1, y2, z2) <= 0.0D && this.dot4(shadowClipPlane, x2, y2, z2) <= 0.0D)
            {
                return false;
            }
        }

        return true;
    }

    private double dot4(float[] plane, double x, double y, double z)
    {
        return (double)plane[0] * x + (double)plane[1] * y + (double)plane[2] * z + (double)plane[3];
    }

    private double dot3(float[] vecA, float[] vecB)
    {
        return (double)vecA[0] * (double)vecB[0] + (double)vecA[1] * (double)vecB[1] + (double)vecA[2] * (double)vecB[2];
    }

    public static ClippingHelper getInstance()
    {
        instance.init();
        return instance;
    }

    private void normalizePlane(float[] plane)
    {
        float length = MathHelper.sqrt_float(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]);
        plane[0] /= length;
        plane[1] /= length;
        plane[2] /= length;
        plane[3] /= length;
    }

    private void normalize3(float[] plane)
    {
        float length = MathHelper.sqrt_float(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]);

        if (length == 0.0F)
        {
            length = 1.0F;
        }

        plane[0] /= length;
        plane[1] /= length;
        plane[2] /= length;
    }

    private void assignPlane(float[] plane, float a, float b, float c, float planeOffset)
    {
        float length = (float)Math.sqrt((double)(a * a + b * b + c * c));
        plane[0] = a / length;
        plane[1] = b / length;
        plane[2] = c / length;
        plane[3] = planeOffset / length;
    }

    private void copyPlane(float[] dst, float[] src)
    {
        dst[0] = src[0];
        dst[1] = src[1];
        dst[2] = src[2];
        dst[3] = src[3];
    }

    private void cross3(float[] out, float[] a, float[] b)
    {
        out[0] = a[1] * b[2] - a[2] * b[1];
        out[1] = a[2] * b[0] - a[0] * b[2];
        out[2] = a[0] * b[1] - a[1] * b[0];
    }

    private void addShadowClipPlane(float[] plane)
    {
        this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], plane);
    }

    private float length(float x, float y, float z)
    {
        return (float)Math.sqrt((double)(x * x + y * y + z * z));
    }

    private float distance(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        return this.length(x1 - x2, y1 - y2, z1 - z2);
    }

    private void makeShadowPlane(float[] shadowPlane, float[] positivePlane, float[] negativePlane, float[] vecSun)
    {
        this.cross3(this.vecIntersection, positivePlane, negativePlane);
        this.cross3(shadowPlane, this.vecIntersection, vecSun);
        this.normalize3(shadowPlane);
        float positiveNegativeDot = (float)this.dot3(positivePlane, negativePlane);
        float shadowNegativeDot = (float)this.dot3(shadowPlane, negativePlane);
        float shadowNegativeDistance = this.distance(shadowPlane[0], shadowPlane[1], shadowPlane[2], negativePlane[0] * shadowNegativeDot, negativePlane[1] * shadowNegativeDot, negativePlane[2] * shadowNegativeDot);
        float positiveNegativeDistance = this.distance(positivePlane[0], positivePlane[1], positivePlane[2], negativePlane[0] * positiveNegativeDot, negativePlane[1] * positiveNegativeDot, negativePlane[2] * positiveNegativeDot);
        float positivePlaneWeight = shadowNegativeDistance / positiveNegativeDistance;
        float shadowPositiveDot = (float)this.dot3(shadowPlane, positivePlane);
        float shadowPositiveDistance = this.distance(shadowPlane[0], shadowPlane[1], shadowPlane[2], positivePlane[0] * shadowPositiveDot, positivePlane[1] * shadowPositiveDot, positivePlane[2] * shadowPositiveDot);
        float negativePositiveDistance = this.distance(negativePlane[0], negativePlane[1], negativePlane[2], positivePlane[0] * positiveNegativeDot, positivePlane[1] * positiveNegativeDot, positivePlane[2] * positiveNegativeDot);
        float negativePlaneWeight = shadowPositiveDistance / negativePositiveDistance;
        shadowPlane[3] = positivePlane[3] * positivePlaneWeight + negativePlane[3] * negativePlaneWeight;
    }

    public void init()
    {
        float[] projection = this.projectionMatrix;
        float[] modelView = this.modelviewMatrix;
        float[] clipping = this.clippingMatrix;
        System.arraycopy(Shaders.faProjection, 0, projection, 0, 16);
        System.arraycopy(Shaders.faModelView, 0, modelView, 0, 16);
        SMath.multiplyMat4xMat4(clipping, modelView, projection);
        this.assignPlane(this.frustum[0], clipping[3] - clipping[0], clipping[7] - clipping[4], clipping[11] - clipping[8], clipping[15] - clipping[12]);
        this.assignPlane(this.frustum[1], clipping[3] + clipping[0], clipping[7] + clipping[4], clipping[11] + clipping[8], clipping[15] + clipping[12]);
        this.assignPlane(this.frustum[2], clipping[3] + clipping[1], clipping[7] + clipping[5], clipping[11] + clipping[9], clipping[15] + clipping[13]);
        this.assignPlane(this.frustum[3], clipping[3] - clipping[1], clipping[7] - clipping[5], clipping[11] - clipping[9], clipping[15] - clipping[13]);
        this.assignPlane(this.frustum[4], clipping[3] - clipping[2], clipping[7] - clipping[6], clipping[11] - clipping[10], clipping[15] - clipping[14]);
        this.assignPlane(this.frustum[5], clipping[3] + clipping[2], clipping[7] + clipping[6], clipping[11] + clipping[10], clipping[15] + clipping[14]);
        float[] sunVector = Shaders.shadowLightPositionVector;
        float plane0DotSun = (float)this.dot3(this.frustum[0], sunVector);
        float plane1DotSun = (float)this.dot3(this.frustum[1], sunVector);
        float plane2DotSun = (float)this.dot3(this.frustum[2], sunVector);
        float plane3DotSun = (float)this.dot3(this.frustum[3], sunVector);
        float plane4DotSun = (float)this.dot3(this.frustum[4], sunVector);
        float plane5DotSun = (float)this.dot3(this.frustum[5], sunVector);
        this.shadowClipPlaneCount = 0;

        if (plane0DotSun >= 0.0F)
        {
            this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[0]);

            if (plane0DotSun > 0.0F)
            {
                if (plane2DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[0], this.frustum[2], sunVector);
                }

                if (plane3DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[0], this.frustum[3], sunVector);
                }

                if (plane4DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[0], this.frustum[4], sunVector);
                }

                if (plane5DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[0], this.frustum[5], sunVector);
                }
            }
        }

        if (plane1DotSun >= 0.0F)
        {
            this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[1]);

            if (plane1DotSun > 0.0F)
            {
                if (plane2DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[1], this.frustum[2], sunVector);
                }

                if (plane3DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[1], this.frustum[3], sunVector);
                }

                if (plane4DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[1], this.frustum[4], sunVector);
                }

                if (plane5DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[1], this.frustum[5], sunVector);
                }
            }
        }

        if (plane2DotSun >= 0.0F)
        {
            this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[2]);

            if (plane2DotSun > 0.0F)
            {
                if (plane0DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[2], this.frustum[0], sunVector);
                }

                if (plane1DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[2], this.frustum[1], sunVector);
                }

                if (plane4DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[2], this.frustum[4], sunVector);
                }

                if (plane5DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[2], this.frustum[5], sunVector);
                }
            }
        }

        if (plane3DotSun >= 0.0F)
        {
            this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[3]);

            if (plane3DotSun > 0.0F)
            {
                if (plane0DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[3], this.frustum[0], sunVector);
                }

                if (plane1DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[3], this.frustum[1], sunVector);
                }

                if (plane4DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[3], this.frustum[4], sunVector);
                }

                if (plane5DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[3], this.frustum[5], sunVector);
                }
            }
        }

        if (plane4DotSun >= 0.0F)
        {
            this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[4]);

            if (plane4DotSun > 0.0F)
            {
                if (plane0DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[4], this.frustum[0], sunVector);
                }

                if (plane1DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[4], this.frustum[1], sunVector);
                }

                if (plane2DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[4], this.frustum[2], sunVector);
                }

                if (plane3DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[4], this.frustum[3], sunVector);
                }
            }
        }

        if (plane5DotSun >= 0.0F)
        {
            this.copyPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[5]);

            if (plane5DotSun > 0.0F)
            {
                if (plane0DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[5], this.frustum[0], sunVector);
                }

                if (plane1DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[5], this.frustum[1], sunVector);
                }

                if (plane2DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[5], this.frustum[2], sunVector);
                }

                if (plane3DotSun < 0.0F)
                {
                    this.makeShadowPlane(this.shadowClipPlanes[this.shadowClipPlaneCount++], this.frustum[5], this.frustum[3], sunVector);
                }
            }
        }
    }
}
