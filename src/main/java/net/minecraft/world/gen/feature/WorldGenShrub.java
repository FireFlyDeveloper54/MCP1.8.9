package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenShrub extends WorldGenTrees
{
    private final IBlockState leavesMetadata;
    private final IBlockState woodMetadata;

    public WorldGenShrub(IBlockState woodMetadataIn, IBlockState leavesMetadataIn)
    {
        super(false);
        this.woodMetadata = woodMetadataIn;
        this.leavesMetadata = leavesMetadataIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        Block currentBlock;

        while (((currentBlock = worldIn.getBlockState(position).getBlock()).getMaterial() == Material.air || currentBlock.getMaterial() == Material.leaves) && position.getY() > 0)
        {
            position = position.down();
        }

        Block soilBlock = worldIn.getBlockState(position).getBlock();

        if (soilBlock == Blocks.dirt || soilBlock == Blocks.grass)
        {
            position = position.up();
            this.setBlockAndNotifyAdequately(worldIn, position, this.woodMetadata);
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int leafY = position.getY(); leafY <= position.getY() + 2; ++leafY)
            {
                int layerOffset = leafY - position.getY();
                int layerRadius = 2 - layerOffset;

                for (int leafX = position.getX() - layerRadius; leafX <= position.getX() + layerRadius; ++leafX)
                {
                    int leafXOffset = leafX - position.getX();

                    for (int leafZ = position.getZ() - layerRadius; leafZ <= position.getZ() + layerRadius; ++leafZ)
                    {
                        int leafZOffset = leafZ - position.getZ();

                        if (Math.abs(leafXOffset) != layerRadius || Math.abs(leafZOffset) != layerRadius || rand.nextInt(2) != 0)
                        {
                            blockPos.set(leafX, leafY, leafZ);

                            if (!worldIn.getBlockState(blockPos).getBlock().isFullBlock())
                            {
                                this.setBlockAndNotifyAdequately(worldIn, blockPos, this.leavesMetadata);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
