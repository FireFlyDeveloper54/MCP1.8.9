package net.minecraft.block;

import com.google.common.base.Predicate;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockPumpkin extends BlockDirectional
{
    private BlockPattern snowmanBasePattern;
    private BlockPattern snowmanPattern;
    private BlockPattern golemBasePattern;
    private BlockPattern golemPattern;
    private static final Predicate<IBlockState> IS_PUMPKIN = new Predicate<IBlockState>()
    {
        public boolean apply(IBlockState state)
        {
            return state != null && (state.getBlock() == Blocks.pumpkin || state.getBlock() == Blocks.lit_pumpkin);
        }
    };

    protected BlockPumpkin()
    {
        super(Material.gourd, MapColor.adobeColor);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        this.trySpawnGolem(worldIn, pos);
    }

    public boolean canDispenserPlace(World worldIn, BlockPos pos)
    {
        return this.getSnowmanBasePattern().match(worldIn, pos) != null || this.getGolemBasePattern().match(worldIn, pos) != null;
    }

    private void trySpawnGolem(World worldIn, BlockPos pos)
    {
        BlockPattern.PatternHelper patternHelper;

        if ((patternHelper = this.getSnowmanPattern().match(worldIn, pos)) != null)
        {
            for (int snowmanBlockIndex = 0; snowmanBlockIndex < this.getSnowmanPattern().getThumbLength(); ++snowmanBlockIndex)
            {
                BlockWorldState snowmanBlockState = patternHelper.translateOffset(0, snowmanBlockIndex, 0);
                worldIn.setBlockState(snowmanBlockState.getPos(), Blocks.air.getDefaultState(), 2);
            }

            EntitySnowman snowman = new EntitySnowman(worldIn);
            BlockPos snowmanPos = patternHelper.translateOffset(0, 2, 0).getPos();
            snowman.setLocationAndAngles((double)snowmanPos.getX() + 0.5D, (double)snowmanPos.getY() + 0.05D, (double)snowmanPos.getZ() + 0.5D, 0.0F, 0.0F);
            worldIn.spawnEntityInWorld(snowman);

            for (int particleIndex = 0; particleIndex < 120; ++particleIndex)
            {
                worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, (double)snowmanPos.getX() + worldIn.rand.nextDouble(), (double)snowmanPos.getY() + worldIn.rand.nextDouble() * 2.5D, (double)snowmanPos.getZ() + worldIn.rand.nextDouble(), 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
            }

            for (int notifyIndex = 0; notifyIndex < this.getSnowmanPattern().getThumbLength(); ++notifyIndex)
            {
                BlockWorldState snowmanBlockState = patternHelper.translateOffset(0, notifyIndex, 0);
                worldIn.notifyNeighborsRespectDebug(snowmanBlockState.getPos(), Blocks.air);
            }
        }
        else if ((patternHelper = this.getGolemPattern().match(worldIn, pos)) != null)
        {
            for (int palmOffset = 0; palmOffset < this.getGolemPattern().getPalmLength(); ++palmOffset)
            {
                for (int thumbOffset = 0; thumbOffset < this.getGolemPattern().getThumbLength(); ++thumbOffset)
                {
                    worldIn.setBlockState(patternHelper.translateOffset(palmOffset, thumbOffset, 0).getPos(), Blocks.air.getDefaultState(), 2);
                }
            }

            BlockPos golemPos = patternHelper.translateOffset(1, 2, 0).getPos();
            EntityIronGolem ironGolem = new EntityIronGolem(worldIn);
            ironGolem.setPlayerCreated(true);
            ironGolem.setLocationAndAngles((double)golemPos.getX() + 0.5D, (double)golemPos.getY() + 0.05D, (double)golemPos.getZ() + 0.5D, 0.0F, 0.0F);
            worldIn.spawnEntityInWorld(ironGolem);

            for (int particleIndex = 0; particleIndex < 120; ++particleIndex)
            {
                worldIn.spawnParticle(EnumParticleTypes.SNOWBALL, (double)golemPos.getX() + worldIn.rand.nextDouble(), (double)golemPos.getY() + worldIn.rand.nextDouble() * 3.9D, (double)golemPos.getZ() + worldIn.rand.nextDouble(), 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
            }

            for (int palmOffset = 0; palmOffset < this.getGolemPattern().getPalmLength(); ++palmOffset)
            {
                for (int thumbOffset = 0; thumbOffset < this.getGolemPattern().getThumbLength(); ++thumbOffset)
                {
                    BlockWorldState golemBlockState = patternHelper.translateOffset(palmOffset, thumbOffset, 0);
                    worldIn.notifyNeighborsRespectDebug(golemBlockState.getPos(), Blocks.air);
                }
            }
        }
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().blockMaterial.isReplaceable() && World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING});
    }

    protected BlockPattern getSnowmanBasePattern()
    {
        if (this.snowmanBasePattern == null)
        {
            this.snowmanBasePattern = FactoryBlockPattern.start().aisle(new String[] {" ", "#", "#"}).where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.snow))).build();
        }

        return this.snowmanBasePattern;
    }

    protected BlockPattern getSnowmanPattern()
    {
        if (this.snowmanPattern == null)
        {
            this.snowmanPattern = FactoryBlockPattern.start().aisle(new String[] {"^", "#", "#"}).where('^', BlockWorldState.hasState(IS_PUMPKIN)).where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.snow))).build();
        }

        return this.snowmanPattern;
    }

    protected BlockPattern getGolemBasePattern()
    {
        if (this.golemBasePattern == null)
        {
            this.golemBasePattern = FactoryBlockPattern.start().aisle(new String[] {"~ ~", "###", "~#~"}).where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.iron_block))).where('~', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.air))).build();
        }

        return this.golemBasePattern;
    }

    protected BlockPattern getGolemPattern()
    {
        if (this.golemPattern == null)
        {
            this.golemPattern = FactoryBlockPattern.start().aisle(new String[] {"~^~", "###", "~#~"}).where('^', BlockWorldState.hasState(IS_PUMPKIN)).where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.iron_block))).where('~', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.air))).build();
        }

        return this.golemPattern;
    }
}
