package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.block.BlockBush;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class GeneratorBushFeature extends WorldGenerator
{
    private BlockBush bushBlock;

    public GeneratorBushFeature(BlockBush bushBlockIn)
    {
        this.bushBlock = bushBlockIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (int attempt = 0; attempt < 64; ++attempt)
        {
            BlockPos blockPos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockPos) && (!worldIn.provider.getHasNoSky() || blockPos.getY() < 255) && this.bushBlock.canBlockStay(worldIn, blockPos, this.bushBlock.getDefaultState()))
            {
                worldIn.setBlockState(blockPos, this.bushBlock.getDefaultState(), 2);
            }
        }

        return true;
    }
}
