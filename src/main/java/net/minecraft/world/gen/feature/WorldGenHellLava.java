package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenHellLava extends WorldGenerator
{
    private final Block lavaBlock;
    private final boolean insideRock;

    public WorldGenHellLava(Block lavaBlockIn, boolean insideRockIn)
    {
        this.lavaBlock = lavaBlockIn;
        this.insideRock = insideRockIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if (worldIn.getBlockState(position.up()).getBlock() != Blocks.netherrack)
        {
            return false;
        }
        else if (worldIn.getBlockState(position).getBlock().getMaterial() != Material.air && worldIn.getBlockState(position).getBlock() != Blocks.netherrack)
        {
            return false;
        }
        else
        {
            int adjacentNetherrackCount = 0;

            if (worldIn.getBlockState(position.west()).getBlock() == Blocks.netherrack)
            {
                ++adjacentNetherrackCount;
            }

            if (worldIn.getBlockState(position.east()).getBlock() == Blocks.netherrack)
            {
                ++adjacentNetherrackCount;
            }

            if (worldIn.getBlockState(position.north()).getBlock() == Blocks.netherrack)
            {
                ++adjacentNetherrackCount;
            }

            if (worldIn.getBlockState(position.south()).getBlock() == Blocks.netherrack)
            {
                ++adjacentNetherrackCount;
            }

            if (worldIn.getBlockState(position.down()).getBlock() == Blocks.netherrack)
            {
                ++adjacentNetherrackCount;
            }

            int adjacentAirCount = 0;

            if (worldIn.isAirBlock(position.west()))
            {
                ++adjacentAirCount;
            }

            if (worldIn.isAirBlock(position.east()))
            {
                ++adjacentAirCount;
            }

            if (worldIn.isAirBlock(position.north()))
            {
                ++adjacentAirCount;
            }

            if (worldIn.isAirBlock(position.south()))
            {
                ++adjacentAirCount;
            }

            if (worldIn.isAirBlock(position.down()))
            {
                ++adjacentAirCount;
            }

            if (!this.insideRock && adjacentNetherrackCount == 4 && adjacentAirCount == 1 || adjacentNetherrackCount == 5)
            {
                worldIn.setBlockState(position, this.lavaBlock.getDefaultState(), 2);
                worldIn.forceBlockUpdateTick(this.lavaBlock, position, rand);
            }

            return true;
        }
    }
}
