package net.minecraft.client.resources.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class BuiltInModel implements IBakedModel
{
    private ItemCameraTransforms cameraTransforms;

    public BuiltInModel(ItemCameraTransforms cameraTransformsIn)
    {
        this.cameraTransforms = cameraTransformsIn;
    }

    public List<BakedQuad> getFaceQuads(EnumFacing facing)
    {
        return null;
    }

    public List<BakedQuad> getGeneralQuads()
    {
        return null;
    }

    public boolean isAmbientOcclusion()
    {
        return false;
    }

    public boolean isGui3d()
    {
        return true;
    }

    public boolean isBuiltInRenderer()
    {
        return true;
    }

    public TextureAtlasSprite getParticleTexture()
    {
        return null;
    }

    public ItemCameraTransforms getItemCameraTransforms()
    {
        return this.cameraTransforms;
    }
}
