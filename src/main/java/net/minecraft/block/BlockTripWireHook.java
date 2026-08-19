package net.minecraft.block;

import com.google.common.base.MoreObjects;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTripWireHook extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyBool ATTACHED = PropertyBool.create("attached");
    public static final PropertyBool SUSPENDED = PropertyBool.create("suspended");

    public BlockTripWireHook()
    {
        super(Material.circuits);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false)).withProperty(SUSPENDED, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.tabRedstone);
        this.setTickRandomly(true);
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.withProperty(SUSPENDED, Boolean.valueOf(!World.doesBlockHaveSolidTopSurface(worldIn, pos.down())));
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        return null;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean isFullCube()
    {
        return false;
    }

    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return side.getAxis().isHorizontal() && worldIn.getBlockState(pos.offset(side.getOpposite())).getBlock().isNormalCube();
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
        {
            if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock().isNormalCube())
            {
                return true;
            }
        }

        return false;
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState iblockstate = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false)).withProperty(SUSPENDED, Boolean.valueOf(false));

        if (facing.getAxis().isHorizontal())
        {
            iblockstate = iblockstate.withProperty(FACING, facing);
        }

        return iblockstate;
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        this.updateTripWireState(worldIn, pos, state, false, false, -1, (IBlockState)null);
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (neighborBlock != this)
        {
            if (this.checkForDrop(worldIn, pos, state))
            {
                EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);

                if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getBlock().isNormalCube())
                {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                    worldIn.setBlockToAir(pos);
                }
            }
        }
    }

    public void updateTripWireState(World worldIn, BlockPos pos, IBlockState hookState, boolean skipOwnStateUpdate, boolean notifyNeighbors, int changedWireIndex, IBlockState changedWireState)
    {
        EnumFacing enumfacing = (EnumFacing)hookState.getValue(FACING);
        boolean flag = ((Boolean)hookState.getValue(ATTACHED)).booleanValue();
        boolean flag1 = ((Boolean)hookState.getValue(POWERED)).booleanValue();
        boolean flag2 = !World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
        boolean flag3 = !skipOwnStateUpdate;
        boolean flag4 = false;
        int i = 0;
        IBlockState[] aiblockstate = new IBlockState[42];

        for (int j = 1; j < 42; ++j)
        {
            BlockPos blockPos = pos.offset(enumfacing, j);
            IBlockState iblockstate = worldIn.getBlockState(blockPos);

            if (iblockstate.getBlock() == Blocks.tripwire_hook)
            {
                if (iblockstate.getValue(FACING) == enumfacing.getOpposite())
                {
                    i = j;
                }

                break;
            }

            if (iblockstate.getBlock() != Blocks.tripwire && j != changedWireIndex)
            {
                aiblockstate[j] = null;
                flag3 = false;
            }
            else
            {
                if (j == changedWireIndex)
                {
                    iblockstate = (IBlockState)MoreObjects.firstNonNull(changedWireState, iblockstate);
                }

                boolean flag5 = !((Boolean)iblockstate.getValue(BlockTripWire.DISARMED)).booleanValue();
                boolean flag6 = ((Boolean)iblockstate.getValue(BlockTripWire.POWERED)).booleanValue();
                boolean flag7 = ((Boolean)iblockstate.getValue(BlockTripWire.SUSPENDED)).booleanValue();
                flag3 &= flag7 == flag2;
                flag4 |= flag5 && flag6;
                aiblockstate[j] = iblockstate;

                if (j == changedWireIndex)
                {
                    worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                    flag3 &= flag5;
                }
            }
        }

        flag3 = flag3 & i > 1;
        flag4 = flag4 & flag3;
        IBlockState iblockstate1 = this.getDefaultState().withProperty(ATTACHED, Boolean.valueOf(flag3)).withProperty(POWERED, Boolean.valueOf(flag4));

        if (i > 0)
        {
            BlockPos blockpos1 = pos.offset(enumfacing, i);
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            worldIn.setBlockState(blockpos1, iblockstate1.withProperty(FACING, enumfacing1), 3);
            this.notifyHookNeighbors(worldIn, blockpos1, enumfacing1);
            this.playHookSound(worldIn, blockpos1, flag3, flag4, flag, flag1);
        }

        this.playHookSound(worldIn, pos, flag3, flag4, flag, flag1);

        if (!skipOwnStateUpdate)
        {
            worldIn.setBlockState(pos, iblockstate1.withProperty(FACING, enumfacing), 3);

            if (notifyNeighbors)
            {
                this.notifyHookNeighbors(worldIn, pos, enumfacing);
            }
        }

        if (flag != flag3)
        {
            for (int k = 1; k < i; ++k)
            {
                BlockPos blockpos2 = pos.offset(enumfacing, k);
                IBlockState iblockstate2 = aiblockstate[k];

                if (iblockstate2 != null && worldIn.getBlockState(blockpos2).getBlock() != Blocks.air)
                {
                    worldIn.setBlockState(blockpos2, iblockstate2.withProperty(ATTACHED, Boolean.valueOf(flag3)), 3);
                }
            }
        }
    }

    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        this.updateTripWireState(worldIn, pos, state, false, true, -1, (IBlockState)null);
    }

    private void playHookSound(World worldIn, BlockPos pos, boolean attached, boolean powered, boolean wasAttached, boolean wasPowered)
    {
        if (powered && !wasPowered)
        {
            worldIn.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D, "random.click", 0.4F, 0.6F);
        }
        else if (!powered && wasPowered)
        {
            worldIn.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D, "random.click", 0.4F, 0.5F);
        }
        else if (attached && !wasAttached)
        {
            worldIn.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D, "random.click", 0.4F, 0.7F);
        }
        else if (!attached && wasAttached)
        {
            worldIn.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D, "random.bowhit", 0.4F, 1.2F / (worldIn.rand.nextFloat() * 0.2F + 0.9F));
        }
    }

    private void notifyHookNeighbors(World worldIn, BlockPos pos, EnumFacing facing)
    {
        worldIn.notifyNeighborsOfStateChange(pos, this);
        worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this);
    }

    private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!this.canPlaceBlockAt(worldIn, pos))
        {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
            return false;
        }
        else
        {
            return true;
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
    {
        float f = 0.1875F;

        switch ((EnumFacing)worldIn.getBlockState(pos).getValue(FACING))
        {
            case EAST:
                this.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
                break;

            case WEST:
                this.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
                break;

            case SOUTH:
                this.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
                break;

            case NORTH:
                this.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
        }
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        boolean flag = ((Boolean)state.getValue(ATTACHED)).booleanValue();
        boolean flag1 = ((Boolean)state.getValue(POWERED)).booleanValue();

        if (flag || flag1)
        {
            this.updateTripWireState(worldIn, pos, state, true, false, -1, (IBlockState)null);
        }

        if (flag1)
        {
            worldIn.notifyNeighborsOfStateChange(pos, this);
            worldIn.notifyNeighborsOfStateChange(pos.offset(((EnumFacing)state.getValue(FACING)).getOpposite()), this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return ((Boolean)state.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return !((Boolean)state.getValue(POWERED)).booleanValue() ? 0 : (state.getValue(FACING) == side ? 15 : 0);
    }

    public boolean canProvidePower()
    {
        return true;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(POWERED, Boolean.valueOf((meta & 8) > 0)).withProperty(ATTACHED, Boolean.valueOf((meta & 4) > 0));
    }

    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();

        if (((Boolean)state.getValue(POWERED)).booleanValue())
        {
            i |= 8;
        }

        if (((Boolean)state.getValue(ATTACHED)).booleanValue())
        {
            i |= 4;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING, POWERED, ATTACHED, SUSPENDED});
    }
}
