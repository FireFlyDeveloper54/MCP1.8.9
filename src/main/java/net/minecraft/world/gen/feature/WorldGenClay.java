package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenClay extends WorldGenerator
{
    private Block clayBlock = Blocks.clay;
    private int numberOfBlocks;

    public WorldGenClay(int numberOfBlocksIn)
    {
        this.numberOfBlocks = numberOfBlocksIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if (worldIn.getBlockState(position).getBlock().getMaterial() != Material.water)
        {
            return false;
        }
        else
        {
            int diskRadius = rand.nextInt(this.numberOfBlocks - 2) + 2;
            int verticalRadius = 1;
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int blockX = position.getX() - diskRadius; blockX <= position.getX() + diskRadius; ++blockX)
            {
                for (int blockZ = position.getZ() - diskRadius; blockZ <= position.getZ() + diskRadius; ++blockZ)
                {
                    int deltaX = blockX - position.getX();
                    int deltaZ = blockZ - position.getZ();

                    if (deltaX * deltaX + deltaZ * deltaZ <= diskRadius * diskRadius)
                    {
                        for (int blockY = position.getY() - verticalRadius; blockY <= position.getY() + verticalRadius; ++blockY)
                        {
                            blockPos.set(blockX, blockY, blockZ);
                            Block existingBlock = worldIn.getBlockState(blockPos).getBlock();

                            if (existingBlock == Blocks.dirt || existingBlock == Blocks.clay)
                            {
                                worldIn.setBlockState(blockPos, this.clayBlock.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}
