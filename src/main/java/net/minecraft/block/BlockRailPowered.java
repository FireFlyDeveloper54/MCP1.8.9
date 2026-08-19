package net.minecraft.block;

import com.google.common.base.Predicate;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockRailPowered extends BlockRailBase
{
    public static final PropertyEnum<BlockRailBase.EnumRailDirection> SHAPE = PropertyEnum.<BlockRailBase.EnumRailDirection>create("shape", BlockRailBase.EnumRailDirection.class, new Predicate<BlockRailBase.EnumRailDirection>()
    {
        public boolean apply(BlockRailBase.EnumRailDirection railDirection)
        {
            return railDirection != BlockRailBase.EnumRailDirection.NORTH_EAST && railDirection != BlockRailBase.EnumRailDirection.NORTH_WEST && railDirection != BlockRailBase.EnumRailDirection.SOUTH_EAST && railDirection != BlockRailBase.EnumRailDirection.SOUTH_WEST;
        }
    });
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    protected BlockRailPowered()
    {
        super(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH).withProperty(POWERED, Boolean.valueOf(false)));
    }

    @SuppressWarnings("incomplete-switch")
    protected boolean findPoweredRailSignal(World worldIn, BlockPos pos, IBlockState state, boolean forward, int distance)
    {
        if (distance >= 8)
        {
            return false;
        }
        else
        {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            boolean flag = true;
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(SHAPE);

            switch (blockrailbase$enumraildirection)
            {
                case NORTH_SOUTH:
                    if (forward)
                    {
                        ++k;
                    }
                    else
                    {
                        --k;
                    }

                    break;

                case EAST_WEST:
                    if (forward)
                    {
                        --i;
                    }
                    else
                    {
                        ++i;
                    }

                    break;

                case ASCENDING_EAST:
                    if (forward)
                    {
                        --i;
                    }
                    else
                    {
                        ++i;
                        ++j;
                        flag = false;
                    }

                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
                    break;

                case ASCENDING_WEST:
                    if (forward)
                    {
                        --i;
                        ++j;
                        flag = false;
                    }
                    else
                    {
                        ++i;
                    }

                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
                    break;

                case ASCENDING_NORTH:
                    if (forward)
                    {
                        ++k;
                    }
                    else
                    {
                        --k;
                        ++j;
                        flag = false;
                    }

                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                    break;

                case ASCENDING_SOUTH:
                    if (forward)
                    {
                        ++k;
                        ++j;
                        flag = false;
                    }
                    else
                    {
                        --k;
                    }

                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            BlockPos.MutableBlockPos railPos = new BlockPos.MutableBlockPos(i, j, k);

            if (this.isPoweredAlongTrack(worldIn, railPos, forward, distance, blockrailbase$enumraildirection))
            {
                return true;
            }

            return flag && this.isPoweredAlongTrack(worldIn, railPos.set(i, j - 1, k), forward, distance, blockrailbase$enumraildirection);
        }
    }

    protected boolean isPoweredAlongTrack(World worldIn, BlockPos pos, boolean forward, int distance, BlockRailBase.EnumRailDirection searchDirection)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() != this)
        {
            return false;
        }
        else
        {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)iblockstate.getValue(SHAPE);
            return searchDirection != BlockRailBase.EnumRailDirection.EAST_WEST || blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.NORTH_SOUTH && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_NORTH && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_SOUTH ? (searchDirection != BlockRailBase.EnumRailDirection.NORTH_SOUTH || blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.EAST_WEST && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_EAST && blockrailbase$enumraildirection != BlockRailBase.EnumRailDirection.ASCENDING_WEST ? (((Boolean)iblockstate.getValue(POWERED)).booleanValue() ? (worldIn.isBlockPowered(pos) ? true : this.findPoweredRailSignal(worldIn, pos, iblockstate, forward, distance + 1)) : false) : false) : false;
        }
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        boolean wasPowered = ((Boolean)state.getValue(POWERED)).booleanValue();
        boolean shouldBePowered = worldIn.isBlockPowered(pos) || this.findPoweredRailSignal(worldIn, pos, state, true, 0) || this.findPoweredRailSignal(worldIn, pos, state, false, 0);

        if (shouldBePowered != wasPowered)
        {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(shouldBePowered)), 3);
            worldIn.notifyNeighborsOfStateChange(pos.down(), this);

            if (((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).isAscending())
            {
                worldIn.notifyNeighborsOfStateChange(pos.up(), this);
            }
        }
    }

    public IProperty<BlockRailBase.EnumRailDirection> getShapeProperty()
    {
        return SHAPE;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta & 7)).withProperty(POWERED, Boolean.valueOf((meta & 8) > 0));
    }

    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).getMetadata();

        if (((Boolean)state.getValue(POWERED)).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {SHAPE, POWERED});
    }
}
