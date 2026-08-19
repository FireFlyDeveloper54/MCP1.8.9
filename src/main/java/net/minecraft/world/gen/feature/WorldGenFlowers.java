package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenFlowers extends WorldGenerator
{
    private BlockFlower flower;
    private IBlockState generatedBlockState;

    public WorldGenFlowers(BlockFlower flower, BlockFlower.EnumFlowerType flowerType)
    {
        this.setGeneratedBlock(flower, flowerType);
    }

    public void setGeneratedBlock(BlockFlower flower, BlockFlower.EnumFlowerType flowerType)
    {
        this.flower = flower;
        this.generatedBlockState = flower.getDefaultState().withProperty(flower.getTypeProperty(), flowerType);
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (int attempt = 0; attempt < 64; ++attempt)
        {
            BlockPos blockPos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockPos) && (!worldIn.provider.getHasNoSky() || blockPos.getY() < 255) && this.flower.canBlockStay(worldIn, blockPos, this.generatedBlockState))
            {
                worldIn.setBlockState(blockPos, this.generatedBlockState, 2);
            }
        }

        return true;
    }
}
