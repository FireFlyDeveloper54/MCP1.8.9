package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenAbstractTree extends WorldGenerator
{
    public WorldGenAbstractTree(boolean notify)
    {
        super(notify);
    }

    protected boolean canGrowInto(Block blockIn)
    {
        Material material = blockIn.getMaterial();
        return material == Material.air || material == Material.leaves || blockIn == Blocks.grass || blockIn == Blocks.dirt || blockIn == Blocks.log || blockIn == Blocks.log2 || blockIn == Blocks.sapling || blockIn == Blocks.vine;
    }

    public void generateSaplings(World worldIn, Random rand, BlockPos pos)
    {
    }

    protected void setDirtAt(World worldIn, BlockPos pos)
    {
        if (worldIn.getBlockState(pos).getBlock() != Blocks.dirt)
        {
            this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.dirt.getDefaultState());
        }
    }
}
