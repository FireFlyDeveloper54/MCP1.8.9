package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenReed extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (int attempt = 0; attempt < 20; ++attempt)
        {
            BlockPos reedPos = position.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));

            if (worldIn.isAirBlock(reedPos))
            {
                BlockPos groundPos = reedPos.down();

                if (worldIn.getBlockState(groundPos.west()).getBlock().getMaterial() == Material.water || worldIn.getBlockState(groundPos.east()).getBlock().getMaterial() == Material.water || worldIn.getBlockState(groundPos.north()).getBlock().getMaterial() == Material.water || worldIn.getBlockState(groundPos.south()).getBlock().getMaterial() == Material.water)
                {
                    int reedHeight = 2 + rand.nextInt(rand.nextInt(3) + 1);

                    for (int heightOffset = 0; heightOffset < reedHeight; ++heightOffset)
                    {
                        if (Blocks.reeds.canBlockStay(worldIn, reedPos))
                        {
                            worldIn.setBlockState(reedPos.up(heightOffset), Blocks.reeds.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        return true;
    }
}
