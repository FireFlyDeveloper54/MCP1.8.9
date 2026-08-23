package net.minecraft.client.renderer.block.model;

import net.optifine.model.BlockModelUtils;
import net.optifine.shaders.Shaders;
import net.minecraft.util.vector.Matrix4f;
import net.minecraft.util.vector.Vector3f;
import net.minecraft.util.vector.Vector4f;

import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.src.Config;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

public class FaceBakery
{
    private static final float SCALE_ROTATION_22_5 = 1.0F / (float)Math.cos(0.39269909262657166D) - 1.0F;
    private static final float SCALE_ROTATION_GENERAL = 1.0F / (float)Math.cos((Math.PI / 4D)) - 1.0F;
    private static final Vector3f AXIS_X = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f AXIS_Y = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final Vector3f AXIS_Z = new Vector3f(0.0F, 0.0F, 1.0F);
    private static final Vector3f ROTATION_ORIGIN = new Vector3f(0.5F, 0.5F, 0.5F);
    private static final Vector3f UNIT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
    private static final Vector3f[] FACING_NORMALS = makeFacingNormals();

    private static Vector3f[] makeFacingNormals()
    {
        Vector3f[] facingNormals = new Vector3f[EnumFacing.VALUES.length];

        for (EnumFacing facing : EnumFacing.VALUES)
        {
            Vec3i direction = facing.getDirectionVec();
            facingNormals[facing.getIndex()] = new Vector3f((float)direction.getX(), (float)direction.getY(), (float)direction.getZ());
        }

        return facingNormals;
    }

    public BakedQuad makeBakedQuad(Vector3f posFrom, Vector3f posTo, BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, ModelRotation modelRotationIn, BlockPartRotation partRotation, boolean uvLocked, boolean shade)
    {
        return this.makeBakedQuad(posFrom, posTo, face, sprite, facing, (ITransformation) modelRotationIn, partRotation, uvLocked, shade);
    }

    public BakedQuad makeBakedQuad(Vector3f posFrom, Vector3f posTo, BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, ITransformation modelRotation, BlockPartRotation partRotation, boolean uvLocked, boolean shade)
    {
        int[] vertexData = this.makeQuadVertexData(face, sprite, facing, this.getPositionsDiv16(posFrom, posTo), modelRotation, partRotation, uvLocked, shade);
        EnumFacing quadFacing = getFacingFromVertexData(vertexData);

        if (uvLocked)
        {
            this.lockUv(vertexData, quadFacing, face.blockFaceUV, sprite);
        }

        if (partRotation == null)
        {
            this.applyFacing(vertexData, quadFacing);
        }

        return new BakedQuad(vertexData, face.tintIndex, quadFacing);
    }

    private int[] makeQuadVertexData(BlockPartFace face, TextureAtlasSprite sprite, EnumFacing facing, float[] positions, ITransformation modelRotation, BlockPartRotation partRotation, boolean uvLocked, boolean shade)
    {
        int vertexDataLength = 28;

        if (Config.isShaders())
        {
            vertexDataLength = 56;
        }

        int[] vertexData = new int[vertexDataLength];

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            this.fillVertexData(vertexData, vertexIndex, facing, face, positions, sprite, modelRotation, partRotation, uvLocked, shade);
        }

        return vertexData;
    }

    private int getFaceShadeColor(EnumFacing facing)
    {
        float brightness = getFaceBrightness(facing);
        int shadeComponent = MathHelper.clamp_int((int)(brightness * 255.0F), 0, 255);
        return -16777216 | shadeComponent << 16 | shadeComponent << 8 | shadeComponent;
    }

    public static float getFaceBrightness(EnumFacing facing)
    {
        switch (facing)
        {
            case DOWN:
                if (Config.isShaders())
                {
                    return Shaders.blockLightLevel05;
                }

                return 0.5F;

            case UP:
                return 1.0F;

            case NORTH:
            case SOUTH:
                if (Config.isShaders())
                {
                    return Shaders.blockLightLevel08;
                }

                return 0.8F;

            case WEST:
            case EAST:
                if (Config.isShaders())
                {
                    return Shaders.blockLightLevel06;
                }

                return 0.6F;

            default:
                return 1.0F;
        }
    }

    private float[] getPositionsDiv16(Vector3f posFrom, Vector3f posTo)
    {
        float[] positions = new float[EnumFacing.VALUES.length];
        positions[EnumFaceDirection.Constants.WEST_INDEX] = posFrom.x / 16.0F;
        positions[EnumFaceDirection.Constants.DOWN_INDEX] = posFrom.y / 16.0F;
        positions[EnumFaceDirection.Constants.NORTH_INDEX] = posFrom.z / 16.0F;
        positions[EnumFaceDirection.Constants.EAST_INDEX] = posTo.x / 16.0F;
        positions[EnumFaceDirection.Constants.UP_INDEX] = posTo.y / 16.0F;
        positions[EnumFaceDirection.Constants.SOUTH_INDEX] = posTo.z / 16.0F;
        return positions;
    }

    private void fillVertexData(int[] faceData, int vertexIndex, EnumFacing facing, BlockPartFace face, float[] positions, TextureAtlasSprite sprite, ITransformation modelRotation, BlockPartRotation partRotation, boolean uvLocked, boolean shade)
    {
        EnumFacing rotatedFacing = modelRotation.rotate(facing);
        int shadeColor = shade ? this.getFaceShadeColor(rotatedFacing) : -1;
        EnumFaceDirection.VertexInformation vertexInfo = EnumFaceDirection.getFacing(facing).getVertexInformation(vertexIndex);
        Vector3f position = new Vector3f(positions[vertexInfo.xIndex], positions[vertexInfo.yIndex], positions[vertexInfo.zIndex]);
        this.rotatePart(position, partRotation);
        int storeIndex = this.rotateVertex(position, facing, vertexIndex, modelRotation, uvLocked);
        BlockModelUtils.snapVertexPosition(position);
        this.storeVertexData(faceData, storeIndex, vertexIndex, position, shadeColor, sprite, face.blockFaceUV);
    }

    private void storeVertexData(int[] faceData, int storeIndex, int vertexIndex, Vector3f position, int shadeColor, TextureAtlasSprite sprite, BlockFaceUV faceUV)
    {
        int intsPerVertex = faceData.length / 4;
        int dataOffset = storeIndex * intsPerVertex;
        faceData[dataOffset] = Float.floatToRawIntBits(position.x);
        faceData[dataOffset + 1] = Float.floatToRawIntBits(position.y);
        faceData[dataOffset + 2] = Float.floatToRawIntBits(position.z);
        faceData[dataOffset + 3] = shadeColor;
        faceData[dataOffset + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU((double)faceUV.getVertexU(vertexIndex) * 0.999D + (double)faceUV.getVertexU((vertexIndex + 2) % 4) * 0.001D));
        faceData[dataOffset + 4 + 1] = Float.floatToRawIntBits(sprite.getInterpolatedV((double)faceUV.getVertexV(vertexIndex) * 0.999D + (double)faceUV.getVertexV((vertexIndex + 2) % 4) * 0.001D));
    }

    private void rotatePart(Vector3f position, BlockPartRotation partRotation)
    {
        if (partRotation != null)
        {
            Matrix4f rotationMatrix = this.getMatrixIdentity();
            Vector3f scaleVector = new Vector3f(0.0F, 0.0F, 0.0F);

            switch (partRotation.axis)
            {
                case X:
                    Matrix4f.rotate(partRotation.angle * 0.017453292F, AXIS_X, rotationMatrix, rotationMatrix);
                    scaleVector.set(0.0F, 1.0F, 1.0F);
                    break;

                case Y:
                    Matrix4f.rotate(partRotation.angle * 0.017453292F, AXIS_Y, rotationMatrix, rotationMatrix);
                    scaleVector.set(1.0F, 0.0F, 1.0F);
                    break;

                case Z:
                    Matrix4f.rotate(partRotation.angle * 0.017453292F, AXIS_Z, rotationMatrix, rotationMatrix);
                    scaleVector.set(1.0F, 1.0F, 0.0F);
            }

            if (partRotation.rescale)
            {
                if (Math.abs(partRotation.angle) == 22.5F)
                {
                    scaleVector.scale(SCALE_ROTATION_22_5);
                }
                else
                {
                    scaleVector.scale(SCALE_ROTATION_GENERAL);
                }

                Vector3f.add(scaleVector, UNIT_SCALE, scaleVector);
            }
            else
            {
                scaleVector.set(1.0F, 1.0F, 1.0F);
            }

            this.rotateScale(position, partRotation.origin, rotationMatrix, scaleVector);
        }
    }

    public int rotateVertex(Vector3f position, EnumFacing facing, int vertexIndex, ModelRotation modelRotationIn, boolean uvLocked)
    {
        return this.rotateVertex(position, facing, vertexIndex, (ITransformation)modelRotationIn, uvLocked);
    }

    public int rotateVertex(Vector3f position, EnumFacing facing, int vertexIndex, ITransformation modelRotation, boolean uvLocked)
    {
        if (modelRotation == ModelRotation.X0_Y0)
        {
            return vertexIndex;
        }
        else
        {
            if (modelRotation instanceof ModelRotation)
            {
                this.rotateScale(position, ROTATION_ORIGIN, ((ModelRotation)modelRotation).getMatrix4d(), UNIT_SCALE);
            }
            return modelRotation.rotate(facing, vertexIndex);
        }
    }

    private void rotateScale(Vector3f position, Vector3f rotationOrigin, Matrix4f rotationMatrix, Vector3f scale)
    {
        Vector4f transformedPosition = new Vector4f(position.x - rotationOrigin.x, position.y - rotationOrigin.y, position.z - rotationOrigin.z, 1.0F);
        Matrix4f.transform(rotationMatrix, transformedPosition, transformedPosition);
        transformedPosition.x *= scale.x;
        transformedPosition.y *= scale.y;
        transformedPosition.z *= scale.z;
        position.set(transformedPosition.x + rotationOrigin.x, transformedPosition.y + rotationOrigin.y, transformedPosition.z + rotationOrigin.z);
    }

    private Matrix4f getMatrixIdentity()
    {
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        return identity;
    }

    public static EnumFacing getFacingFromVertexData(int[] faceData)
    {
        int intsPerVertex = faceData.length / 4;
        int thirdVertexOffset = intsPerVertex * 2;
        Vector3f firstVertex = new Vector3f(Float.intBitsToFloat(faceData[0]), Float.intBitsToFloat(faceData[1]), Float.intBitsToFloat(faceData[2]));
        Vector3f secondVertex = new Vector3f(Float.intBitsToFloat(faceData[intsPerVertex]), Float.intBitsToFloat(faceData[intsPerVertex + 1]), Float.intBitsToFloat(faceData[intsPerVertex + 2]));
        Vector3f thirdVertex = new Vector3f(Float.intBitsToFloat(faceData[thirdVertexOffset]), Float.intBitsToFloat(faceData[thirdVertexOffset + 1]), Float.intBitsToFloat(faceData[thirdVertexOffset + 2]));
        Vector3f edgeToFirstVertex = new Vector3f();
        Vector3f edgeToThirdVertex = new Vector3f();
        Vector3f normal = new Vector3f();
        Vector3f.sub(firstVertex, secondVertex, edgeToFirstVertex);
        Vector3f.sub(thirdVertex, secondVertex, edgeToThirdVertex);
        Vector3f.cross(edgeToThirdVertex, edgeToFirstVertex, normal);
        float normalLength = (float)MathHelper.fastSqrt_double((double)(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z));
        normal.x /= normalLength;
        normal.y /= normalLength;
        normal.z /= normalLength;
        EnumFacing closestFacing = null;
        float bestDot = 0.0F;

        for (EnumFacing candidateFacing : EnumFacing.VALUES)
        {
            Vector3f candidateNormal = FACING_NORMALS[candidateFacing.getIndex()];
            float dot = Vector3f.dot(normal, candidateNormal);

            if (dot >= 0.0F && dot > bestDot)
            {
                bestDot = dot;
                closestFacing = candidateFacing;
            }
        }

        if (closestFacing == null)
        {
            return EnumFacing.UP;
        }
        else
        {
            return closestFacing;
        }
    }

    public void lockUv(int[] vertexData, EnumFacing facing, BlockFaceUV faceUV, TextureAtlasSprite sprite)
    {
        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            this.lockVertexUv(vertexIndex, vertexData, facing, faceUV, sprite);
        }
    }

    private void applyFacing(int[] vertexData, EnumFacing facing)
    {
        int[] originalVertexData = new int[vertexData.length];
        System.arraycopy(vertexData, 0, originalVertexData, 0, vertexData.length);
        float[] faceBounds = new float[EnumFacing.VALUES.length];
        faceBounds[EnumFaceDirection.Constants.WEST_INDEX] = 999.0F;
        faceBounds[EnumFaceDirection.Constants.DOWN_INDEX] = 999.0F;
        faceBounds[EnumFaceDirection.Constants.NORTH_INDEX] = 999.0F;
        faceBounds[EnumFaceDirection.Constants.EAST_INDEX] = -999.0F;
        faceBounds[EnumFaceDirection.Constants.UP_INDEX] = -999.0F;
        faceBounds[EnumFaceDirection.Constants.SOUTH_INDEX] = -999.0F;
        int intsPerVertex = vertexData.length / 4;

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int dataOffset = intsPerVertex * vertexIndex;
            float x = Float.intBitsToFloat(originalVertexData[dataOffset]);
            float y = Float.intBitsToFloat(originalVertexData[dataOffset + 1]);
            float z = Float.intBitsToFloat(originalVertexData[dataOffset + 2]);

            if (x < faceBounds[EnumFaceDirection.Constants.WEST_INDEX])
            {
                faceBounds[EnumFaceDirection.Constants.WEST_INDEX] = x;
            }

            if (y < faceBounds[EnumFaceDirection.Constants.DOWN_INDEX])
            {
                faceBounds[EnumFaceDirection.Constants.DOWN_INDEX] = y;
            }

            if (z < faceBounds[EnumFaceDirection.Constants.NORTH_INDEX])
            {
                faceBounds[EnumFaceDirection.Constants.NORTH_INDEX] = z;
            }

            if (x > faceBounds[EnumFaceDirection.Constants.EAST_INDEX])
            {
                faceBounds[EnumFaceDirection.Constants.EAST_INDEX] = x;
            }

            if (y > faceBounds[EnumFaceDirection.Constants.UP_INDEX])
            {
                faceBounds[EnumFaceDirection.Constants.UP_INDEX] = y;
            }

            if (z > faceBounds[EnumFaceDirection.Constants.SOUTH_INDEX])
            {
                faceBounds[EnumFaceDirection.Constants.SOUTH_INDEX] = z;
            }
        }

        EnumFaceDirection faceDirection = EnumFaceDirection.getFacing(facing);

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int dataOffset = intsPerVertex * vertexIndex;
            EnumFaceDirection.VertexInformation vertexInfo = faceDirection.getVertexInformation(vertexIndex);
            float x = faceBounds[vertexInfo.xIndex];
            float y = faceBounds[vertexInfo.yIndex];
            float z = faceBounds[vertexInfo.zIndex];
            vertexData[dataOffset] = Float.floatToRawIntBits(x);
            vertexData[dataOffset + 1] = Float.floatToRawIntBits(y);
            vertexData[dataOffset + 2] = Float.floatToRawIntBits(z);

            for (int sourceVertexIndex = 0; sourceVertexIndex < 4; ++sourceVertexIndex)
            {
                int sourceOffset = intsPerVertex * sourceVertexIndex;
                float sourceX = Float.intBitsToFloat(originalVertexData[sourceOffset]);
                float sourceY = Float.intBitsToFloat(originalVertexData[sourceOffset + 1]);
                float sourceZ = Float.intBitsToFloat(originalVertexData[sourceOffset + 2]);

                if (MathHelper.epsilonEquals(x, sourceX) && MathHelper.epsilonEquals(y, sourceY) && MathHelper.epsilonEquals(z, sourceZ))
                {
                    vertexData[dataOffset + 4] = originalVertexData[sourceOffset + 4];
                    vertexData[dataOffset + 4 + 1] = originalVertexData[sourceOffset + 4 + 1];
                }
            }
        }
    }

    private void lockVertexUv(int vertexIndex, int[] vertexData, EnumFacing facing, BlockFaceUV faceUV, TextureAtlasSprite sprite)
    {
        int intsPerVertex = vertexData.length / 4;
        int dataOffset = intsPerVertex * vertexIndex;
        float x = Float.intBitsToFloat(vertexData[dataOffset]);
        float y = Float.intBitsToFloat(vertexData[dataOffset + 1]);
        float z = Float.intBitsToFloat(vertexData[dataOffset + 2]);

        if (x < -0.1F || x >= 1.1F)
        {
            x -= (float)MathHelper.floor_float(x);
        }

        if (y < -0.1F || y >= 1.1F)
        {
            y -= (float)MathHelper.floor_float(y);
        }

        if (z < -0.1F || z >= 1.1F)
        {
            z -= (float)MathHelper.floor_float(z);
        }

        float lockedU = 0.0F;
        float lockedV = 0.0F;

        switch (facing)
        {
            case DOWN:
                lockedU = x * 16.0F;
                lockedV = (1.0F - z) * 16.0F;
                break;

            case UP:
                lockedU = x * 16.0F;
                lockedV = z * 16.0F;
                break;

            case NORTH:
                lockedU = (1.0F - x) * 16.0F;
                lockedV = (1.0F - y) * 16.0F;
                break;

            case SOUTH:
                lockedU = x * 16.0F;
                lockedV = (1.0F - y) * 16.0F;
                break;

            case WEST:
                lockedU = z * 16.0F;
                lockedV = (1.0F - y) * 16.0F;
                break;

            case EAST:
                lockedU = (1.0F - z) * 16.0F;
                lockedV = (1.0F - y) * 16.0F;
        }

        int rotatedOffset = faceUV.getInverseRotatedVertexIndex(vertexIndex) * intsPerVertex;
        vertexData[rotatedOffset + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU((double)lockedU));
        vertexData[rotatedOffset + 4 + 1] = Float.floatToRawIntBits(sprite.getInterpolatedV((double)lockedV));
    }
}
