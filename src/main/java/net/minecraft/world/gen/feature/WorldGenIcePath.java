package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenIcePath extends WorldGenerator
{
    private Block pathBlock = Blocks.packed_ice;
    private int basePathWidth;

    public WorldGenIcePath(int basePathWidthIn)
    {
        this.basePathWidth = basePathWidthIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        while (worldIn.isAirBlock(position) && position.getY() > 2)
        {
            position = position.down();
        }

        if (worldIn.getBlockState(position).getBlock() != Blocks.snow)
        {
            return false;
        }
        else
        {
            int pathRadius = rand.nextInt(this.basePathWidth - 2) + 2;
            int verticalRadius = 1;
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int blockX = position.getX() - pathRadius; blockX <= position.getX() + pathRadius; ++blockX)
            {
                for (int blockZ = position.getZ() - pathRadius; blockZ <= position.getZ() + pathRadius; ++blockZ)
                {
                    int deltaX = blockX - position.getX();
                    int deltaZ = blockZ - position.getZ();

                    if (deltaX * deltaX + deltaZ * deltaZ <= pathRadius * pathRadius)
                    {
                        for (int blockY = position.getY() - verticalRadius; blockY <= position.getY() + verticalRadius; ++blockY)
                        {
                            blockPos.set(blockX, blockY, blockZ);
                            Block existingBlock = worldIn.getBlockState(blockPos).getBlock();

                            if (existingBlock == Blocks.dirt || existingBlock == Blocks.snow || existingBlock == Blocks.ice)
                            {
                                worldIn.setBlockState(blockPos, this.pathBlock.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}
