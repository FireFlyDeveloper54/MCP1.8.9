package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRailBase extends Block
{
    protected final boolean isPowered;

    public static boolean isRailBlock(World worldIn, BlockPos pos)
    {
        return isRailBlock(worldIn.getBlockState(pos));
    }

    public static boolean isRailBlock(IBlockState state)
    {
        Block block = state.getBlock();
        return block == Blocks.rail || block == Blocks.golden_rail || block == Blocks.detector_rail || block == Blocks.activator_rail;
    }

    protected BlockRailBase(boolean isPowered)
    {
        super(Material.circuits);
        this.isPowered = isPowered;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        this.setCreativeTab(CreativeTabs.tabTransport);
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end)
    {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getBlock() == this ? (BlockRailBase.EnumRailDirection)iblockstate.getValue(this.getShapeProperty()) : null;

        if (blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending())
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
        }
        else
        {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        }
    }

    public boolean isFullCube()
    {
        return false;
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            state = this.updateRailState(worldIn, pos, state, true);

            if (this.isPowered)
            {
                this.onNeighborBlockChange(worldIn, pos, state, this);
            }
        }
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!worldIn.isRemote)
        {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(this.getShapeProperty());
            boolean flag = false;

            if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()))
            {
                flag = true;
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_EAST && !World.doesBlockHaveSolidTopSurface(worldIn, pos.east()))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_WEST && !World.doesBlockHaveSolidTopSurface(worldIn, pos.west()))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_NORTH && !World.doesBlockHaveSolidTopSurface(worldIn, pos.north()))
            {
                flag = true;
            }
            else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.ASCENDING_SOUTH && !World.doesBlockHaveSolidTopSurface(worldIn, pos.south()))
            {
                flag = true;
            }

            if (flag)
            {
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
            else
            {
                this.onNeighborChangedInternal(worldIn, pos, state, neighborBlock);
            }
        }
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
    }

    protected IBlockState updateRailState(World worldIn, BlockPos pos, IBlockState state, boolean forceUpdate)
    {
        return worldIn.isRemote ? state : (new BlockRailBase.Rail(worldIn, pos, state)).setRailDirection(worldIn.isBlockPowered(pos), forceUpdate).getBlockState();
    }

    public int getMobilityFlag()
    {
        return 0;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);

        if (((BlockRailBase.EnumRailDirection)state.getValue(this.getShapeProperty())).isAscending())
        {
            worldIn.notifyNeighborsOfStateChange(pos.up(), this);
        }

        if (this.isPowered)
        {
            worldIn.notifyNeighborsOfStateChange(pos, this);
            worldIn.notifyNeighborsOfStateChange(pos.down(), this);
        }
    }

    public abstract IProperty<BlockRailBase.EnumRailDirection> getShapeProperty();

    public static enum EnumRailDirection implements IStringSerializable
    {
        NORTH_SOUTH(0, "north_south"),
        EAST_WEST(1, "east_west"),
        ASCENDING_EAST(2, "ascending_east"),
        ASCENDING_WEST(3, "ascending_west"),
        ASCENDING_NORTH(4, "ascending_north"),
        ASCENDING_SOUTH(5, "ascending_south"),
        SOUTH_EAST(6, "south_east"),
        SOUTH_WEST(7, "south_west"),
        NORTH_WEST(8, "north_west"),
        NORTH_EAST(9, "north_east");

        public static final BlockRailBase.EnumRailDirection[] VALUES = values();
        private static final BlockRailBase.EnumRailDirection[] META_LOOKUP = new BlockRailBase.EnumRailDirection[VALUES.length];
        private final int meta;
        private final String name;

        private EnumRailDirection(int meta, String name)
        {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata()
        {
            return this.meta;
        }

        public String toString()
        {
            return this.name;
        }

        public boolean isAscending()
        {
            return this == ASCENDING_NORTH || this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
        }

        public static BlockRailBase.EnumRailDirection byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName()
        {
            return this.name;
        }

        static {
            for (BlockRailBase.EnumRailDirection blockrailbase$enumraildirection : VALUES)
            {
                META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection;
            }
        }
    }

    public class Rail
    {
        private final World world;
        private final BlockPos pos;
        private final BlockRailBase block;
        private IBlockState state;
        private final boolean isPowered;
        private final List<BlockPos> connections = Lists.<BlockPos>newArrayList();

        public Rail(World worldIn, BlockPos pos, IBlockState state)
        {
            this.world = worldIn;
            this.pos = pos;
            this.state = state;
            this.block = (BlockRailBase)state.getBlock();
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(BlockRailBase.this.getShapeProperty());
            this.isPowered = this.block.isPowered;
            this.updateConnections(blockrailbase$enumraildirection);
        }

        private void updateConnections(BlockRailBase.EnumRailDirection railDirection)
        {
            this.connections.clear();

            switch (railDirection)
            {
                case NORTH_SOUTH:
                    this.connections.add(this.pos.north());
                    this.connections.add(this.pos.south());
                    break;

                case EAST_WEST:
                    this.connections.add(this.pos.west());
                    this.connections.add(this.pos.east());
                    break;

                case ASCENDING_EAST:
                    this.connections.add(this.pos.west());
                    this.connections.add(this.pos.east().up());
                    break;

                case ASCENDING_WEST:
                    this.connections.add(this.pos.west().up());
                    this.connections.add(this.pos.east());
                    break;

                case ASCENDING_NORTH:
                    this.connections.add(this.pos.north().up());
                    this.connections.add(this.pos.south());
                    break;

                case ASCENDING_SOUTH:
                    this.connections.add(this.pos.north());
                    this.connections.add(this.pos.south().up());
                    break;

                case SOUTH_EAST:
                    this.connections.add(this.pos.east());
                    this.connections.add(this.pos.south());
                    break;

                case SOUTH_WEST:
                    this.connections.add(this.pos.west());
                    this.connections.add(this.pos.south());
                    break;

                case NORTH_WEST:
                    this.connections.add(this.pos.west());
                    this.connections.add(this.pos.north());
                    break;

                case NORTH_EAST:
                    this.connections.add(this.pos.east());
                    this.connections.add(this.pos.north());
            }
        }

        private void refreshConnectedRails()
        {
            for (int i = 0; i < this.connections.size(); ++i)
            {
                BlockRailBase.Rail blockrailbase$rail = this.findRailAt((BlockPos)this.connections.get(i));

                if (blockrailbase$rail != null && blockrailbase$rail.sameRailLine(this))
                {
                    this.connections.set(i, blockrailbase$rail.pos);
                }
                else
                {
                    this.connections.remove(i--);
                }
            }
        }

        private boolean hasRailAt(BlockPos pos)
        {
            return BlockRailBase.isRailBlock(this.world, pos) || BlockRailBase.isRailBlock(this.world, pos.up()) || BlockRailBase.isRailBlock(this.world, pos.down());
        }

        private BlockRailBase.Rail findRailAt(BlockPos pos)
        {
            IBlockState iblockstate = this.world.getBlockState(pos);

            if (BlockRailBase.isRailBlock(iblockstate))
            {
                return BlockRailBase.this.new Rail(this.world, pos, iblockstate);
            }
            else
            {
                BlockPos neighborPos = pos.up();
                iblockstate = this.world.getBlockState(neighborPos);

                if (BlockRailBase.isRailBlock(iblockstate))
                {
                    return BlockRailBase.this.new Rail(this.world, neighborPos, iblockstate);
                }
                else
                {
                    neighborPos = pos.down();
                    iblockstate = this.world.getBlockState(neighborPos);
                    return BlockRailBase.isRailBlock(iblockstate) ? BlockRailBase.this.new Rail(this.world, neighborPos, iblockstate) : null;
                }
            }
        }

        private boolean sameRailLine(BlockRailBase.Rail rail)
        {
            return this.hasSameXZ(rail.pos);
        }

        private boolean hasSameXZ(BlockPos pos)
        {
            for (int i = 0; i < this.connections.size(); ++i)
            {
                BlockPos blockpos = (BlockPos)this.connections.get(i);

                if (blockpos.getX() == pos.getX() && blockpos.getZ() == pos.getZ())
                {
                    return true;
                }
            }

            return false;
        }

        protected int countAdjacentRails()
        {
            int i = 0;

            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (this.hasRailAt(this.pos.offset(enumfacing)))
                {
                    ++i;
                }
            }

            return i;
        }

        private boolean shouldConnectTo(BlockRailBase.Rail rail)
        {
            return this.sameRailLine(rail) || this.connections.size() != 2;
        }

        private void addConnectedRail(BlockRailBase.Rail rail)
        {
            this.connections.add(rail.pos);
            BlockPos blockpos = this.pos.north();
            BlockPos blockpos1 = this.pos.south();
            BlockPos blockpos2 = this.pos.west();
            BlockPos blockpos3 = this.pos.east();
            boolean flag = this.hasSameXZ(blockpos);
            boolean flag1 = this.hasSameXZ(blockpos1);
            boolean flag2 = this.hasSameXZ(blockpos2);
            boolean flag3 = this.hasSameXZ(blockpos3);
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

            if (flag || flag1)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if (flag2 || flag3)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!this.isPowered)
            {
                if (flag1 && flag3 && !flag && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                }

                if (flag1 && flag2 && !flag && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                }

                if (flag && flag2 && !flag1 && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                }

                if (flag && flag3 && !flag1 && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH)
            {
                if (BlockRailBase.isRailBlock(this.world, blockpos.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }

                if (BlockRailBase.isRailBlock(this.world, blockpos1.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST)
            {
                if (BlockRailBase.isRailBlock(this.world, blockpos3.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }

                if (BlockRailBase.isRailBlock(this.world, blockpos2.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
            }

            if (blockrailbase$enumraildirection == null)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);
            this.world.setBlockState(this.pos, this.state, 3);
        }

        private boolean canConnectTo(BlockPos pos)
        {
            BlockRailBase.Rail blockrailbase$rail = this.findRailAt(pos);

            if (blockrailbase$rail == null)
            {
                return false;
            }
            else
            {
                blockrailbase$rail.refreshConnectedRails();
                return blockrailbase$rail.shouldConnectTo(this);
            }
        }

        public BlockRailBase.Rail setRailDirection(boolean forceUpdate, boolean updateNeighbors)
        {
            BlockPos blockpos = this.pos.north();
            BlockPos blockpos1 = this.pos.south();
            BlockPos blockpos2 = this.pos.west();
            BlockPos blockpos3 = this.pos.east();
            boolean flag = this.canConnectTo(blockpos);
            boolean flag1 = this.canConnectTo(blockpos1);
            boolean flag2 = this.canConnectTo(blockpos2);
            boolean flag3 = this.canConnectTo(blockpos3);
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = null;

            if ((flag || flag1) && !flag2 && !flag3)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if ((flag2 || flag3) && !flag && !flag1)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!this.isPowered)
            {
                if (flag1 && flag3 && !flag && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                }

                if (flag1 && flag2 && !flag && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                }

                if (flag && flag2 && !flag1 && !flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                }

                if (flag && flag3 && !flag1 && !flag2)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                }
            }

            if (blockrailbase$enumraildirection == null)
            {
                if (flag || flag1)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                }

                if (flag2 || flag3)
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.EAST_WEST;
                }

                if (!this.isPowered)
                {
                    if (forceUpdate)
                    {
                        if (flag1 && flag3)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                        }

                        if (flag2 && flag1)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                        }

                        if (flag3 && flag)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                        }

                        if (flag && flag2)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                        }
                    }
                    else
                    {
                        if (flag && flag2)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_WEST;
                        }

                        if (flag3 && flag)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_EAST;
                        }

                        if (flag2 && flag1)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                        }

                        if (flag1 && flag3)
                        {
                            blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                        }
                    }
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH)
            {
                if (BlockRailBase.isRailBlock(this.world, blockpos.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
                }

                if (BlockRailBase.isRailBlock(this.world, blockpos1.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
                }
            }

            if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST)
            {
                if (BlockRailBase.isRailBlock(this.world, blockpos3.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
                }

                if (BlockRailBase.isRailBlock(this.world, blockpos2.up()))
                {
                    blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
                }
            }

            if (blockrailbase$enumraildirection == null)
            {
                blockrailbase$enumraildirection = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            this.updateConnections(blockrailbase$enumraildirection);
            this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);

            if (updateNeighbors || this.world.getBlockState(this.pos) != this.state)
            {
                this.world.setBlockState(this.pos, this.state, 3);

                for (int i = 0; i < this.connections.size(); ++i)
                {
                    BlockRailBase.Rail blockrailbase$rail = this.findRailAt((BlockPos)this.connections.get(i));

                    if (blockrailbase$rail != null)
                    {
                        blockrailbase$rail.refreshConnectedRails();

                        if (blockrailbase$rail.shouldConnectTo(this))
                        {
                            blockrailbase$rail.addConnectedRail(this);
                        }
                    }
                }
            }

            return this;
        }

        public IBlockState getBlockState()
        {
            return this.state;
        }
    }
}
