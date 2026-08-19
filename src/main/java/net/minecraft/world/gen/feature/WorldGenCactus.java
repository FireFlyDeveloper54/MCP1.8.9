package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenCactus extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (int attemptIndex = 0; attemptIndex < 10; ++attemptIndex)
        {
            BlockPos targetPos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(targetPos))
            {
                int cactusHeight = 1 + rand.nextInt(rand.nextInt(3) + 1);

                for (int heightOffset = 0; heightOffset < cactusHeight; ++heightOffset)
                {
                    if (Blocks.cactus.canBlockStay(worldIn, targetPos))
                    {
                        worldIn.setBlockState(targetPos.up(heightOffset), Blocks.cactus.getDefaultState(), 2);
                    }
                }
            }
        }

        return true;
    }
}
