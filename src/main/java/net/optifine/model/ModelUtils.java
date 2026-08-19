package net.optifine.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.src.Config;
import net.minecraft.util.EnumFacing;

public class ModelUtils
{
    public static void dbgModel(IBakedModel model)
    {
        if (model != null)
        {
            Config.dbg("Model: " + model + ", ao: " + model.isAmbientOcclusion() + ", gui3d: " + model.isGui3d() + ", builtIn: " + model.isBuiltInRenderer() + ", particle: " + model.getParticleTexture());
            EnumFacing[] facings = EnumFacing.VALUES;

            for (int faceIndex = 0; faceIndex < facings.length; ++faceIndex)
            {
                EnumFacing facing = facings[faceIndex];
                List faceQuads = model.getFaceQuads(facing);
                dbgQuads(facing.getName(), faceQuads, "  ");
            }

            List generalQuads = model.getGeneralQuads();
            dbgQuads("General", generalQuads, "  ");
        }
    }

    private static void dbgQuads(String name, List quads, String prefix)
    {
        for (Object o : quads)
        {
            BakedQuad bakedQuad = (BakedQuad) o;
            dbgQuad(name, bakedQuad, prefix);
        }
    }

    public static void dbgQuad(String name, BakedQuad quad, String prefix)
    {
        Config.dbg(prefix + "Quad: " + quad.getClass().getName() + ", type: " + name + ", face: " + quad.getFace() + ", tint: " + quad.getTintIndex() + ", sprite: " + quad.getSprite());
        dbgVertexData(quad.getVertexData(), "  " + prefix);
    }

    public static void dbgVertexData(int[] vd, String prefix)
    {
        int vertexStride = vd.length / 4;
        Config.dbg(prefix + "Length: " + vd.length + ", step: " + vertexStride);

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            int vertexOffset = vertexIndex * vertexStride;
            float x = Float.intBitsToFloat(vd[vertexOffset + 0]);
            float y = Float.intBitsToFloat(vd[vertexOffset + 1]);
            float z = Float.intBitsToFloat(vd[vertexOffset + 2]);
            int color = vd[vertexOffset + 3];
            float u = Float.intBitsToFloat(vd[vertexOffset + 4]);
            float v = Float.intBitsToFloat(vd[vertexOffset + 5]);
            Config.dbg(prefix + vertexIndex + " xyz: " + x + "," + y + "," + z + " col: " + color + " u,v: " + u + "," + v);
        }
    }

    public static IBakedModel duplicateModel(IBakedModel model)
    {
        List generalQuads = duplicateQuadList(model.getGeneralQuads());
        EnumFacing[] facings = EnumFacing.VALUES;
        List faceQuads = new ArrayList();

        for (int faceIndex = 0; faceIndex < facings.length; ++faceIndex)
        {
            EnumFacing facing = facings[faceIndex];
            List originalFaceQuads = model.getFaceQuads(facing);
            List duplicatedFaceQuads = duplicateQuadList(originalFaceQuads);
            faceQuads.add(duplicatedFaceQuads);
        }

        SimpleBakedModel simpleBakedModel = new SimpleBakedModel(generalQuads, faceQuads, model.isAmbientOcclusion(), model.isGui3d(), model.getParticleTexture(), model.getItemCameraTransforms());
        return simpleBakedModel;
    }

    public static List duplicateQuadList(List lists)
    {
        List duplicatedQuads = new ArrayList();

        for (Object o : lists)
        {
            BakedQuad bakedQuad = (BakedQuad) o;
            BakedQuad duplicatedQuad = duplicateQuad(bakedQuad);
            duplicatedQuads.add(duplicatedQuad);
        }

        return duplicatedQuads;
    }

    public static BakedQuad duplicateQuad(BakedQuad quad)
    {
        BakedQuad bakedQuad = new BakedQuad((int[])quad.getVertexData().clone(), quad.getTintIndex(), quad.getFace(), quad.getSprite());
        return bakedQuad;
    }
}
