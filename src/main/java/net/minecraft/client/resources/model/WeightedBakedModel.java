package net.minecraft.client.resources.model;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;

public class WeightedBakedModel implements IBakedModel
{
    private final int totalWeight;
    private final List<WeightedBakedModel.MyWeighedRandomItem> models;
    private final IBakedModel baseModel;

    public WeightedBakedModel(List<WeightedBakedModel.MyWeighedRandomItem> modelsIn)
    {
        this.models = modelsIn;
        this.totalWeight = WeightedRandom.getTotalWeight(modelsIn);
        this.baseModel = modelsIn.get(0).model;
    }

    public List<BakedQuad> getFaceQuads(EnumFacing facing)
    {
        return this.baseModel.getFaceQuads(facing);
    }

    public List<BakedQuad> getGeneralQuads()
    {
        return this.baseModel.getGeneralQuads();
    }

    public boolean isAmbientOcclusion()
    {
        return this.baseModel.isAmbientOcclusion();
    }

    public boolean isGui3d()
    {
        return this.baseModel.isGui3d();
    }

    public boolean isBuiltInRenderer()
    {
        return this.baseModel.isBuiltInRenderer();
    }

    public TextureAtlasSprite getParticleTexture()
    {
        return this.baseModel.getParticleTexture();
    }

    public ItemCameraTransforms getItemCameraTransforms()
    {
        return this.baseModel.getItemCameraTransforms();
    }

    public IBakedModel getAlternativeModel(long stateSeed)
    {
        WeightedBakedModel.MyWeighedRandomItem weightedModel = (WeightedBakedModel.MyWeighedRandomItem)WeightedRandom.getRandomItem(this.models, Math.abs((int)stateSeed >> 16) % this.totalWeight);
        return weightedModel.model;
    }

    public static class Builder
    {
        private List<WeightedBakedModel.MyWeighedRandomItem> listItems = Lists.<WeightedBakedModel.MyWeighedRandomItem>newArrayList();

        public WeightedBakedModel.Builder add(IBakedModel model, int weight)
        {
            this.listItems.add(new WeightedBakedModel.MyWeighedRandomItem(model, weight));
            return this;
        }

        public WeightedBakedModel build()
        {
            Collections.sort(this.listItems);
            return new WeightedBakedModel(this.listItems);
        }

        public IBakedModel first()
        {
            return this.listItems.get(0).model;
        }
    }

    static class MyWeighedRandomItem extends WeightedRandom.Item implements Comparable<WeightedBakedModel.MyWeighedRandomItem>
    {
        protected final IBakedModel model;

        public MyWeighedRandomItem(IBakedModel modelIn, int weight)
        {
            super(weight);
            this.model = modelIn;
        }

        public int compareTo(WeightedBakedModel.MyWeighedRandomItem otherItem)
        {
            return ComparisonChain.start().compare(otherItem.itemWeight, this.itemWeight).compare(this.getCountQuads(), otherItem.getCountQuads()).result();
        }

        protected int getCountQuads()
        {
            int quadCount = this.model.getGeneralQuads().size();

            for (EnumFacing facing : EnumFacing.VALUES)
            {
                quadCount += this.model.getFaceQuads(facing).size();
            }

            return quadCount;
        }

        public String toString()
        {
            return "MyWeighedRandomItem{weight=" + this.itemWeight + ", model=" + this.model + '}';
        }
    }
}
