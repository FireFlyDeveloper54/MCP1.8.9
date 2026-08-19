package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldGenVines extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (; position.getY() < 128; position = position.up())
        {
            if (worldIn.isAirBlock(position))
            {
                for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                {
                    if (Blocks.vine.canPlaceBlockOnSide(worldIn, position, facing))
                    {
                        IBlockState vineState = Blocks.vine.getDefaultState().withProperty(BlockVine.NORTH, Boolean.valueOf(facing == EnumFacing.NORTH)).withProperty(BlockVine.EAST, Boolean.valueOf(facing == EnumFacing.EAST)).withProperty(BlockVine.SOUTH, Boolean.valueOf(facing == EnumFacing.SOUTH)).withProperty(BlockVine.WEST, Boolean.valueOf(facing == EnumFacing.WEST));
                        worldIn.setBlockState(position, vineState, 2);
                        break;
                    }
                }
            }
            else
            {
                position = position.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));
            }
        }

        return true;
    }
}
