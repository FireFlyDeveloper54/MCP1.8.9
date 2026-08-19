package net.minecraft.client.resources.model;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.renderer.block.model.ITransformation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public enum ModelRotation implements ITransformation
{
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private static final ModelRotation[] VALUES = values();
    private static final Map<Integer, ModelRotation> mapRotations = Maps.<Integer, ModelRotation>newHashMap();
    private final int combinedXY;
    private final Matrix4f matrix4d;
    private final int quartersX;
    private final int quartersY;

    private static int combineXY(int xRotation, int yRotation)
    {
        return xRotation * 360 + yRotation;
    }

    private ModelRotation(int xRotation, int yRotation)
    {
        this.combinedXY = combineXY(xRotation, yRotation);
        this.matrix4d = new Matrix4f();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        Matrix4f.rotate((float)(-xRotation) * 0.017453292F, new Vector3f(1.0F, 0.0F, 0.0F), matrix4f, matrix4f);
        this.quartersX = MathHelper.abs_int(xRotation / 90);
        Matrix4f matrix4f1 = new Matrix4f();
        matrix4f1.setIdentity();
        Matrix4f.rotate((float)(-yRotation) * 0.017453292F, new Vector3f(0.0F, 1.0F, 0.0F), matrix4f1, matrix4f1);
        this.quartersY = MathHelper.abs_int(yRotation / 90);
        Matrix4f.mul(matrix4f1, matrix4f, this.matrix4d);
    }

    public Matrix4f getMatrix4d()
    {
        return this.matrix4d;
    }

    public EnumFacing rotateFace(EnumFacing facing)
    {
        EnumFacing rotatedFacing = facing;

        for (int quarterIndex = 0; quarterIndex < this.quartersX; ++quarterIndex)
        {
            rotatedFacing = rotatedFacing.rotateAround(EnumFacing.Axis.X);
        }

        if (rotatedFacing.getAxis() != EnumFacing.Axis.Y)
        {
            for (int quarterIndex = 0; quarterIndex < this.quartersY; ++quarterIndex)
            {
                rotatedFacing = rotatedFacing.rotateAround(EnumFacing.Axis.Y);
            }
        }

        return rotatedFacing;
    }

    public int rotateVertex(EnumFacing facing, int vertexIndex)
    {
        int rotatedVertexIndex = vertexIndex;

        if (facing.getAxis() == EnumFacing.Axis.X)
        {
            rotatedVertexIndex = (vertexIndex + this.quartersX) % 4;
        }

        EnumFacing rotatedFacing = facing;

        for (int quarterIndex = 0; quarterIndex < this.quartersX; ++quarterIndex)
        {
            rotatedFacing = rotatedFacing.rotateAround(EnumFacing.Axis.X);
        }

        if (rotatedFacing.getAxis() == EnumFacing.Axis.Y)
        {
            rotatedVertexIndex = (rotatedVertexIndex + this.quartersY) % 4;
        }

        return rotatedVertexIndex;
    }

    public static ModelRotation getModelRotation(int xRotation, int yRotation)
    {
        return mapRotations.get(Integer.valueOf(combineXY(MathHelper.normalizeAngle(xRotation, 360), MathHelper.normalizeAngle(yRotation, 360))));
    }

    public javax.vecmath.Matrix4f getMatrix()
    {
        Matrix4f src = this.getMatrix4d();
        return new javax.vecmath.Matrix4f(
                src.m00, src.m10, src.m20, src.m30,
                src.m01, src.m11, src.m21, src.m31,
                src.m02, src.m12, src.m22, src.m32,
                src.m03, src.m13, src.m23, src.m33);
    }

    public EnumFacing rotate(EnumFacing facing)
    {
        return this.rotateFace(facing);
    }

    public int rotate(EnumFacing facing, int vertexIndex)
    {
        return this.rotateVertex(facing, vertexIndex);
    }

    static {
        for (ModelRotation modelrotation : VALUES)
        {
            mapRotations.put(Integer.valueOf(modelrotation.combinedXY), modelrotation);
        }
    }
}
