package net.minecraft.world.gen.feature;

import com.google.common.base.Predicates;
import java.util.Random;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldGenDesertWells extends WorldGenerator
{
    private static final BlockStateHelper SAND_BLOCK_STATE = BlockStateHelper.forBlock(Blocks.sand).where(BlockSand.VARIANT, Predicates.equalTo(BlockSand.EnumType.SAND));
    private final IBlockState sandstoneSlab = Blocks.stone_slab.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
    private final IBlockState sandstone = Blocks.sandstone.getDefaultState();
    private final IBlockState water = Blocks.flowing_water.getDefaultState();

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        while (worldIn.isAirBlock(position) && position.getY() > 2)
        {
            position = position.down();
        }

        if (!SAND_BLOCK_STATE.apply(worldIn.getBlockState(position)))
        {
            return false;
        }
        else
        {
            for (int supportOffsetX = -2; supportOffsetX <= 2; ++supportOffsetX)
            {
                for (int supportOffsetZ = -2; supportOffsetZ <= 2; ++supportOffsetZ)
                {
                    if (worldIn.isAirBlock(position.add(supportOffsetX, -1, supportOffsetZ)) && worldIn.isAirBlock(position.add(supportOffsetX, -2, supportOffsetZ)))
                    {
                        return false;
                    }
                }
            }

            for (int foundationOffsetY = -1; foundationOffsetY <= 0; ++foundationOffsetY)
            {
                for (int foundationOffsetX = -2; foundationOffsetX <= 2; ++foundationOffsetX)
                {
                    for (int foundationOffsetZ = -2; foundationOffsetZ <= 2; ++foundationOffsetZ)
                    {
                        worldIn.setBlockState(position.add(foundationOffsetX, foundationOffsetY, foundationOffsetZ), this.sandstone, 2);
                    }
                }
            }

            worldIn.setBlockState(position, this.water, 2);

            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
            {
                worldIn.setBlockState(position.offset(facing), this.water, 2);
            }

            for (int offsetX = -2; offsetX <= 2; ++offsetX)
            {
                for (int offsetZ = -2; offsetZ <= 2; ++offsetZ)
                {
                    if (offsetX == -2 || offsetX == 2 || offsetZ == -2 || offsetZ == 2)
                    {
                        worldIn.setBlockState(position.add(offsetX, 1, offsetZ), this.sandstone, 2);
                    }
                }
            }

            worldIn.setBlockState(position.add(2, 1, 0), this.sandstoneSlab, 2);
            worldIn.setBlockState(position.add(-2, 1, 0), this.sandstoneSlab, 2);
            worldIn.setBlockState(position.add(0, 1, 2), this.sandstoneSlab, 2);
            worldIn.setBlockState(position.add(0, 1, -2), this.sandstoneSlab, 2);

            for (int roofOffsetX = -1; roofOffsetX <= 1; ++roofOffsetX)
            {
                for (int roofOffsetZ = -1; roofOffsetZ <= 1; ++roofOffsetZ)
                {
                    if (roofOffsetX == 0 && roofOffsetZ == 0)
                    {
                        worldIn.setBlockState(position.add(roofOffsetX, 4, roofOffsetZ), this.sandstone, 2);
                    }
                    else
                    {
                        worldIn.setBlockState(position.add(roofOffsetX, 4, roofOffsetZ), this.sandstoneSlab, 2);
                    }
                }
            }

            for (int pillarY = 1; pillarY <= 3; ++pillarY)
            {
                worldIn.setBlockState(position.add(-1, pillarY, -1), this.sandstone, 2);
                worldIn.setBlockState(position.add(-1, pillarY, 1), this.sandstone, 2);
                worldIn.setBlockState(position.add(1, pillarY, -1), this.sandstone, 2);
                worldIn.setBlockState(position.add(1, pillarY, 1), this.sandstone, 2);
            }

            return true;
        }
    }
}
