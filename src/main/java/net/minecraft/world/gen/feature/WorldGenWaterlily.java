package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenWaterlily extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (int attemptIndex = 0; attemptIndex < 10; ++attemptIndex)
        {
            int targetX = position.getX() + rand.nextInt(8) - rand.nextInt(8);
            int targetY = position.getY() + rand.nextInt(4) - rand.nextInt(4);
            int targetZ = position.getZ() + rand.nextInt(8) - rand.nextInt(8);
            BlockPos targetPos = new BlockPos(targetX, targetY, targetZ);

            if (worldIn.isAirBlock(targetPos) && Blocks.waterlily.canPlaceBlockAt(worldIn, targetPos))
            {
                worldIn.setBlockState(targetPos, Blocks.waterlily.getDefaultState(), 2);
            }
        }

        return true;
    }
}
