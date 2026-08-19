package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldGenGlowStone1 extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if (!worldIn.isAirBlock(position))
        {
            return false;
        }
        else if (worldIn.getBlockState(position.up()).getBlock() != Blocks.netherrack)
        {
            return false;
        }
        else
        {
            worldIn.setBlockState(position, Blocks.glowstone.getDefaultState(), 2);

            for (int attempt = 0; attempt < 1500; ++attempt)
            {
                BlockPos candidatePos = position.add(rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));

                if (worldIn.getBlockState(candidatePos).getBlock().getMaterial() == Material.air)
                {
                    int adjacentGlowstoneCount = 0;

                    for (EnumFacing facing : EnumFacing.VALUES)
                    {
                        if (worldIn.getBlockState(candidatePos.offset(facing)).getBlock() == Blocks.glowstone)
                        {
                            ++adjacentGlowstoneCount;
                        }

                        if (adjacentGlowstoneCount > 1)
                        {
                            break;
                        }
                    }

                    if (adjacentGlowstoneCount == 1)
                    {
                        worldIn.setBlockState(candidatePos, Blocks.glowstone.getDefaultState(), 2);
                    }
                }
            }

            return true;
        }
    }
}
