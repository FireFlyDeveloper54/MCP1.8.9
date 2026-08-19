package net.minecraft.block;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockAir extends Block
{
    private static final Map<Block, Integer> mapOriginalOpacity = new IdentityHashMap<Block, Integer>();

    protected BlockAir()
    {
        super(Material.air);
    }

    public int getRenderType()
    {
        return -1;
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return false;
    }

    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
    }

    public boolean isReplaceable(World worldIn, BlockPos pos)
    {
        return true;
    }

    public static void setLightOpacity(Block block, int opacity)
    {
        if (!mapOriginalOpacity.containsKey(block))
        {
            mapOriginalOpacity.put(block, Integer.valueOf(block.lightOpacity));
        }

        block.lightOpacity = opacity;
    }

    public static void restoreLightOpacity(Block block)
    {
        if (mapOriginalOpacity.containsKey(block))
        {
            int originalOpacity = mapOriginalOpacity.get(block).intValue();
            setLightOpacity(block, originalOpacity);
        }
    }
}
