package net.optifine.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.BreakingFour;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.src.Config;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;

public class BlockModelUtils
{
    private static final float VERTEX_COORD_ACCURACY = 1.0E-6F;

    public static IBakedModel makeModelCube(String spriteName, int tintIndex)
    {
        TextureAtlasSprite textureAtlasSprite = Config.getMinecraft().getTextureMapBlocks().getAtlasSprite(spriteName);
        return makeModelCube(textureAtlasSprite, tintIndex);
    }

    public static IBakedModel makeModelCube(TextureAtlasSprite sprite, int tintIndex)
    {
        List generalQuads = new ArrayList();
        EnumFacing[] facings = EnumFacing.VALUES;
        List<List<BakedQuad>> faceQuads = new ArrayList();

        for (int faceIndex = 0; faceIndex < facings.length; ++faceIndex)
        {
            EnumFacing facing = facings[faceIndex];
            List faceQuadList = new ArrayList();
            faceQuadList.add(makeBakedQuad(facing, sprite, tintIndex));
            faceQuads.add(faceQuadList);
        }

        IBakedModel bakedModel = new SimpleBakedModel(generalQuads, faceQuads, true, true, sprite, ItemCameraTransforms.DEFAULT);
        return bakedModel;
    }

    public static IBakedModel joinModelsCube(IBakedModel modelBase, IBakedModel modelAdd)
    {
        List<BakedQuad> generalQuads = new ArrayList();
        generalQuads.addAll(modelBase.getGeneralQuads());
        generalQuads.addAll(modelAdd.getGeneralQuads());
        EnumFacing[] facings = EnumFacing.VALUES;
        List faceQuads = new ArrayList();

        for (int faceIndex = 0; faceIndex < facings.length; ++faceIndex)
        {
            EnumFacing facing = facings[faceIndex];
            List faceQuadList = new ArrayList();
            faceQuadList.addAll(modelBase.getFaceQuads(facing));
            faceQuadList.addAll(modelAdd.getFaceQuads(facing));
            faceQuads.add(faceQuadList);
        }

        boolean ambientOcclusion = modelBase.isAmbientOcclusion();
        boolean builtInRenderer = modelBase.isBuiltInRenderer();
        TextureAtlasSprite particleTexture = modelBase.getParticleTexture();
        ItemCameraTransforms cameraTransforms = modelBase.getItemCameraTransforms();
        IBakedModel bakedModel = new SimpleBakedModel(generalQuads, faceQuads, ambientOcclusion, builtInRenderer, particleTexture, cameraTransforms);
        return bakedModel;
    }

    public static BakedQuad makeBakedQuad(EnumFacing facing, TextureAtlasSprite sprite, int tintIndex)
    {
        Vector3f fromPosition = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f toPosition = new Vector3f(16.0F, 16.0F, 16.0F);
        BlockFaceUV faceUv = new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0);
        BlockPartFace partFace = new BlockPartFace(facing, tintIndex, "#" + facing.getName(), faceUv);
        ModelRotation modelRotation = ModelRotation.X0_Y0;
        BlockPartRotation partRotation = null;
        boolean uvLocked = false;
        boolean shade = true;
        FaceBakery faceBakery = new FaceBakery();
        BakedQuad bakedQuad = faceBakery.makeBakedQuad(fromPosition, toPosition, partFace, sprite, facing, modelRotation, partRotation, uvLocked, shade);
        return bakedQuad;
    }

    public static IBakedModel makeModel(String modelName, String spriteOldName, String spriteNewName)
    {
        TextureMap textureMap = Config.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite oldSprite = textureMap.getSpriteSafe(spriteOldName);
        TextureAtlasSprite newSprite = textureMap.getSpriteSafe(spriteNewName);
        return makeModel(modelName, oldSprite, newSprite);
    }

    public static IBakedModel makeModel(String modelName, TextureAtlasSprite spriteOld, TextureAtlasSprite spriteNew)
    {
        if (spriteOld != null && spriteNew != null)
        {
            ModelManager modelManager = Config.getModelManager();

            if (modelManager == null)
            {
                return null;
            }
            else
            {
                ModelResourceLocation modelResourceLocation = new ModelResourceLocation(modelName, "normal");
                IBakedModel bakedModel = modelManager.getModel(modelResourceLocation);

                if (bakedModel != null && bakedModel != modelManager.getMissingModel())
                {
                    IBakedModel duplicateModel = ModelUtils.duplicateModel(bakedModel);
                    EnumFacing[] facings = EnumFacing.VALUES;

                    for (int faceIndex = 0; faceIndex < facings.length; ++faceIndex)
                    {
                        EnumFacing facing = facings[faceIndex];
                        List<BakedQuad> faceQuads = duplicateModel.getFaceQuads(facing);
                        replaceTexture(faceQuads, spriteOld, spriteNew);
                    }

                    List<BakedQuad> generalQuads = duplicateModel.getGeneralQuads();
                    replaceTexture(generalQuads, spriteOld, spriteNew);
                    return duplicateModel;
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            return null;
        }
    }

    private static void replaceTexture(List<BakedQuad> quads, TextureAtlasSprite spriteOld, TextureAtlasSprite spriteNew)
    {
        List<BakedQuad> replacedQuads = new ArrayList();

        for (BakedQuad bakedQuad : quads)
        {
            if (bakedQuad.getSprite() == spriteOld)
            {
                bakedQuad = new BreakingFour(bakedQuad, spriteNew);
            }

            replacedQuads.add(bakedQuad);
        }

        quads.clear();
        quads.addAll(replacedQuads);
    }

    public static void snapVertexPosition(Vector3f pos)
    {
        pos.setX(snapVertexCoord(pos.getX()));
        pos.setY(snapVertexCoord(pos.getY()));
        pos.setZ(snapVertexCoord(pos.getZ()));
    }

    private static float snapVertexCoord(float x)
    {
        return x > -1.0E-6F && x < 1.0E-6F ? 0.0F : (x > 0.999999F && x < 1.000001F ? 1.0F : x);
    }

    public static AxisAlignedBB getOffsetBoundingBox(AxisAlignedBB aabb, Block.EnumOffsetType offsetType, BlockPos pos)
    {
        int x = pos.getX();
        int z = pos.getZ();
        long offsetSeed = (long)(x * 3129871) ^ (long)z * 116129781L;
        offsetSeed = offsetSeed * offsetSeed * 42317861L + offsetSeed * 11L;
        double offsetX = ((double)((float)(offsetSeed >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
        double offsetZ = ((double)((float)(offsetSeed >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
        double offsetY = 0.0D;

        if (offsetType == Block.EnumOffsetType.XYZ)
        {
            offsetY = ((double)((float)(offsetSeed >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
        }

        return aabb.offset(offsetX, offsetY, offsetZ);
    }
}
