package net.minecraft.client.renderer.culling;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class ClippingHelperImpl extends ClippingHelper
{
    private static ClippingHelperImpl instance = new ClippingHelperImpl();
    private FloatBuffer projectionMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
    private FloatBuffer modelviewMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);

    public static ClippingHelper getInstance()
    {
        instance.init();
        return instance;
    }

    private void normalize(float[] plane)
    {
        float length = MathHelper.sqrt_float(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]);
        plane[0] /= length;
        plane[1] /= length;
        plane[2] /= length;
        plane[3] /= length;
    }

    public void init()
    {
        this.projectionMatrixBuffer.clear();
        this.modelviewMatrixBuffer.clear();
        GlStateManager.getFloat(2983, this.projectionMatrixBuffer);
        GlStateManager.getFloat(2982, this.modelviewMatrixBuffer);
        float[] projection = this.projectionMatrix;
        float[] modelview = this.modelviewMatrix;
        this.projectionMatrixBuffer.flip().limit(16);
        this.projectionMatrixBuffer.get(projection);
        this.modelviewMatrixBuffer.flip().limit(16);
        this.modelviewMatrixBuffer.get(modelview);
        this.clippingMatrix[0] = modelview[0] * projection[0] + modelview[1] * projection[4] + modelview[2] * projection[8] + modelview[3] * projection[12];
        this.clippingMatrix[1] = modelview[0] * projection[1] + modelview[1] * projection[5] + modelview[2] * projection[9] + modelview[3] * projection[13];
        this.clippingMatrix[2] = modelview[0] * projection[2] + modelview[1] * projection[6] + modelview[2] * projection[10] + modelview[3] * projection[14];
        this.clippingMatrix[3] = modelview[0] * projection[3] + modelview[1] * projection[7] + modelview[2] * projection[11] + modelview[3] * projection[15];
        this.clippingMatrix[4] = modelview[4] * projection[0] + modelview[5] * projection[4] + modelview[6] * projection[8] + modelview[7] * projection[12];
        this.clippingMatrix[5] = modelview[4] * projection[1] + modelview[5] * projection[5] + modelview[6] * projection[9] + modelview[7] * projection[13];
        this.clippingMatrix[6] = modelview[4] * projection[2] + modelview[5] * projection[6] + modelview[6] * projection[10] + modelview[7] * projection[14];
        this.clippingMatrix[7] = modelview[4] * projection[3] + modelview[5] * projection[7] + modelview[6] * projection[11] + modelview[7] * projection[15];
        this.clippingMatrix[8] = modelview[8] * projection[0] + modelview[9] * projection[4] + modelview[10] * projection[8] + modelview[11] * projection[12];
        this.clippingMatrix[9] = modelview[8] * projection[1] + modelview[9] * projection[5] + modelview[10] * projection[9] + modelview[11] * projection[13];
        this.clippingMatrix[10] = modelview[8] * projection[2] + modelview[9] * projection[6] + modelview[10] * projection[10] + modelview[11] * projection[14];
        this.clippingMatrix[11] = modelview[8] * projection[3] + modelview[9] * projection[7] + modelview[10] * projection[11] + modelview[11] * projection[15];
        this.clippingMatrix[12] = modelview[12] * projection[0] + modelview[13] * projection[4] + modelview[14] * projection[8] + modelview[15] * projection[12];
        this.clippingMatrix[13] = modelview[12] * projection[1] + modelview[13] * projection[5] + modelview[14] * projection[9] + modelview[15] * projection[13];
        this.clippingMatrix[14] = modelview[12] * projection[2] + modelview[13] * projection[6] + modelview[14] * projection[10] + modelview[15] * projection[14];
        this.clippingMatrix[15] = modelview[12] * projection[3] + modelview[13] * projection[7] + modelview[14] * projection[11] + modelview[15] * projection[15];
        float[] rightPlane = this.frustum[0];
        rightPlane[0] = this.clippingMatrix[3] - this.clippingMatrix[0];
        rightPlane[1] = this.clippingMatrix[7] - this.clippingMatrix[4];
        rightPlane[2] = this.clippingMatrix[11] - this.clippingMatrix[8];
        rightPlane[3] = this.clippingMatrix[15] - this.clippingMatrix[12];
        this.normalize(rightPlane);
        float[] leftPlane = this.frustum[1];
        leftPlane[0] = this.clippingMatrix[3] + this.clippingMatrix[0];
        leftPlane[1] = this.clippingMatrix[7] + this.clippingMatrix[4];
        leftPlane[2] = this.clippingMatrix[11] + this.clippingMatrix[8];
        leftPlane[3] = this.clippingMatrix[15] + this.clippingMatrix[12];
        this.normalize(leftPlane);
        float[] bottomPlane = this.frustum[2];
        bottomPlane[0] = this.clippingMatrix[3] + this.clippingMatrix[1];
        bottomPlane[1] = this.clippingMatrix[7] + this.clippingMatrix[5];
        bottomPlane[2] = this.clippingMatrix[11] + this.clippingMatrix[9];
        bottomPlane[3] = this.clippingMatrix[15] + this.clippingMatrix[13];
        this.normalize(bottomPlane);
        float[] topPlane = this.frustum[3];
        topPlane[0] = this.clippingMatrix[3] - this.clippingMatrix[1];
        topPlane[1] = this.clippingMatrix[7] - this.clippingMatrix[5];
        topPlane[2] = this.clippingMatrix[11] - this.clippingMatrix[9];
        topPlane[3] = this.clippingMatrix[15] - this.clippingMatrix[13];
        this.normalize(topPlane);
        float[] farPlane = this.frustum[4];
        farPlane[0] = this.clippingMatrix[3] - this.clippingMatrix[2];
        farPlane[1] = this.clippingMatrix[7] - this.clippingMatrix[6];
        farPlane[2] = this.clippingMatrix[11] - this.clippingMatrix[10];
        farPlane[3] = this.clippingMatrix[15] - this.clippingMatrix[14];
        this.normalize(farPlane);
        float[] nearPlane = this.frustum[5];
        nearPlane[0] = this.clippingMatrix[3] + this.clippingMatrix[2];
        nearPlane[1] = this.clippingMatrix[7] + this.clippingMatrix[6];
        nearPlane[2] = this.clippingMatrix[11] + this.clippingMatrix[10];
        nearPlane[3] = this.clippingMatrix[15] + this.clippingMatrix[14];
        this.normalize(nearPlane);
    }
}
