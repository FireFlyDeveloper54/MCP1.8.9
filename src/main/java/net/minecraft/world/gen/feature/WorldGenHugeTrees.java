package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenHugeTrees extends WorldGenAbstractTree
{
    protected final int baseHeight;
    protected final IBlockState woodMetadata;
    protected final IBlockState leavesMetadata;
    protected int extraRandomHeight;

    public WorldGenHugeTrees(boolean notify, int baseHeightIn, int extraRandomHeightIn, IBlockState woodMetadataIn, IBlockState leavesMetadataIn)
    {
        super(notify);
        this.baseHeight = baseHeightIn;
        this.extraRandomHeight = extraRandomHeightIn;
        this.woodMetadata = woodMetadataIn;
        this.leavesMetadata = leavesMetadataIn;
    }

    protected int getHeight(Random rand)
    {
        int height = rand.nextInt(3) + this.baseHeight;

        if (this.extraRandomHeight > 1)
        {
            height += rand.nextInt(this.extraRandomHeight);
        }

        return height;
    }

    private boolean isSpaceAt(World worldIn, BlockPos leavesPos, int height)
    {
        boolean hasSpace = true;

        if (leavesPos.getY() >= 1 && leavesPos.getY() + height + 1 <= 256)
        {
            for (int yOffset = 0; yOffset <= 1 + height; ++yOffset)
            {
                int radius = 2;

                if (yOffset == 0)
                {
                    radius = 1;
                }
                else if (yOffset >= 1 + height - 2)
                {
                    radius = 2;
                }

                for (int xOffset = -radius; xOffset <= radius && hasSpace; ++xOffset)
                {
                    for (int zOffset = -radius; zOffset <= radius && hasSpace; ++zOffset)
                    {
                        if (leavesPos.getY() + yOffset < 0 || leavesPos.getY() + yOffset >= 256 || !this.canGrowInto(worldIn.getBlockState(leavesPos.add(xOffset, yOffset, zOffset)).getBlock()))
                        {
                            hasSpace = false;
                        }
                    }
                }
            }

            return hasSpace;
        }
        else
        {
            return false;
        }
    }

    private boolean ensureDirtsUnderneath(BlockPos pos, World worldIn)
    {
        BlockPos soilPos = pos.down();
        Block soilBlock = worldIn.getBlockState(soilPos).getBlock();

        if ((soilBlock == Blocks.grass || soilBlock == Blocks.dirt) && pos.getY() >= 2)
        {
            this.setDirtAt(worldIn, soilPos);
            this.setDirtAt(worldIn, soilPos.east());
            this.setDirtAt(worldIn, soilPos.south());
            this.setDirtAt(worldIn, soilPos.south().east());
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean ensureGrowable(World worldIn, Random rand, BlockPos treePos, int height)
    {
        return this.isSpaceAt(worldIn, treePos, height) && this.ensureDirtsUnderneath(treePos, worldIn);
    }

    protected void growLeavesLayerStrict(World worldIn, BlockPos layerCenter, int width)
    {
        int radiusSquared = width * width;

        for (int xOffset = -width; xOffset <= width + 1; ++xOffset)
        {
            for (int zOffset = -width; zOffset <= width + 1; ++zOffset)
            {
                int previousXOffset = xOffset - 1;
                int previousZOffset = zOffset - 1;

                if (xOffset * xOffset + zOffset * zOffset <= radiusSquared || previousXOffset * previousXOffset + previousZOffset * previousZOffset <= radiusSquared || xOffset * xOffset + previousZOffset * previousZOffset <= radiusSquared || previousXOffset * previousXOffset + zOffset * zOffset <= radiusSquared)
                {
                    BlockPos blockPos = layerCenter.add(xOffset, 0, zOffset);
                    Material material = worldIn.getBlockState(blockPos).getBlock().getMaterial();

                    if (material == Material.air || material == Material.leaves)
                    {
                        this.setBlockAndNotifyAdequately(worldIn, blockPos, this.leavesMetadata);
                    }
                }
            }
        }
    }

    protected void growLeavesLayer(World worldIn, BlockPos layerCenter, int width)
    {
        int radiusSquared = width * width;

        for (int xOffset = -width; xOffset <= width; ++xOffset)
        {
            for (int zOffset = -width; zOffset <= width; ++zOffset)
            {
                if (xOffset * xOffset + zOffset * zOffset <= radiusSquared)
                {
                    BlockPos blockPos = layerCenter.add(xOffset, 0, zOffset);
                    Material material = worldIn.getBlockState(blockPos).getBlock().getMaterial();

                    if (material == Material.air || material == Material.leaves)
                    {
                        this.setBlockAndNotifyAdequately(worldIn, blockPos, this.leavesMetadata);
                    }
                }
            }
        }
    }
}
