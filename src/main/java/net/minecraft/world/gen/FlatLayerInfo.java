package net.minecraft.world.gen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class FlatLayerInfo
{
    private final int version;
    private IBlockState layerMaterial;
    private int layerCount;
    private int layerMinimumY;

    public FlatLayerInfo(int height, Block layerMaterialIn)
    {
        this(3, height, layerMaterialIn);
    }

    public FlatLayerInfo(int versionIn, int height, Block layerMaterialIn)
    {
        this.layerCount = 1;
        this.version = versionIn;
        this.layerCount = height;
        this.layerMaterial = layerMaterialIn.getDefaultState();
    }

    public FlatLayerInfo(int versionIn, int height, Block layerMaterialIn, int metadata)
    {
        this(versionIn, height, layerMaterialIn);
        this.layerMaterial = layerMaterialIn.getStateFromMeta(metadata);
    }

    public int getLayerCount()
    {
        return this.layerCount;
    }

    public IBlockState getLayerMaterial()
    {
        return this.layerMaterial;
    }

    private Block getLayerMaterialBlock()
    {
        return this.layerMaterial.getBlock();
    }

    private int getFillBlockMeta()
    {
        return this.layerMaterial.getBlock().getMetaFromState(this.layerMaterial);
    }

    public int getMinY()
    {
        return this.layerMinimumY;
    }

    public void setMinY(int minY)
    {
        this.layerMinimumY = minY;
    }

    public String toString()
    {
        String layerString;

        if (this.version >= 3)
        {
            ResourceLocation resourceLocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.getLayerMaterialBlock());
            layerString = resourceLocation == null ? "null" : resourceLocation.toString();

            if (this.layerCount > 1)
            {
                layerString = this.layerCount + "*" + layerString;
            }
        }
        else
        {
            layerString = Integer.toString(Block.getIdFromBlock(this.getLayerMaterialBlock()));

            if (this.layerCount > 1)
            {
                layerString = this.layerCount + "x" + layerString;
            }
        }

        int metadata = this.getFillBlockMeta();

        if (metadata > 0)
        {
            layerString = layerString + ":" + metadata;
        }

        return layerString;
    }
}
