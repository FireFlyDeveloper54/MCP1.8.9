package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;

public class BlockFire extends Block
{
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
    public static final PropertyBool FLIP = PropertyBool.create("flip");
    public static final PropertyBool ALT = PropertyBool.create("alt");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyInteger UPPER = PropertyInteger.create("upper", 0, 2);
    private final Map<Block, Integer> encouragements = Maps.<Block, Integer>newIdentityHashMap();
    private final Map<Block, Integer> flammabilities = Maps.<Block, Integer>newIdentityHashMap();

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && !Blocks.fire.canCatchFire(worldIn, pos.down()))
        {
            boolean flag = (i + j + k & 1) == 1;
            boolean flag1 = (i / 2 + j / 2 + k / 2 & 1) == 1;
            int l = 0;

            if (this.canCatchFire(worldIn, pos.up()))
            {
                l = flag ? 1 : 2;
            }

            return state.withProperty(NORTH, Boolean.valueOf(this.canCatchFire(worldIn, pos.north()))).withProperty(EAST, Boolean.valueOf(this.canCatchFire(worldIn, pos.east()))).withProperty(SOUTH, Boolean.valueOf(this.canCatchFire(worldIn, pos.south()))).withProperty(WEST, Boolean.valueOf(this.canCatchFire(worldIn, pos.west()))).withProperty(UPPER, Integer.valueOf(l)).withProperty(FLIP, Boolean.valueOf(flag1)).withProperty(ALT, Boolean.valueOf(flag));
        }
        else
        {
            return this.getDefaultState();
        }
    }

    protected BlockFire()
    {
        super(Material.fire);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(FLIP, Boolean.valueOf(false)).withProperty(ALT, Boolean.valueOf(false)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)).withProperty(UPPER, Integer.valueOf(0)));
        this.setTickRandomly(true);
    }

    public static void init()
    {
        Blocks.fire.setFireInfo(Blocks.planks, 5, 20);
        Blocks.fire.setFireInfo(Blocks.double_wooden_slab, 5, 20);
        Blocks.fire.setFireInfo(Blocks.wooden_slab, 5, 20);
        Blocks.fire.setFireInfo(Blocks.oak_fence_gate, 5, 20);
        Blocks.fire.setFireInfo(Blocks.spruce_fence_gate, 5, 20);
        Blocks.fire.setFireInfo(Blocks.birch_fence_gate, 5, 20);
        Blocks.fire.setFireInfo(Blocks.jungle_fence_gate, 5, 20);
        Blocks.fire.setFireInfo(Blocks.dark_oak_fence_gate, 5, 20);
        Blocks.fire.setFireInfo(Blocks.acacia_fence_gate, 5, 20);
        Blocks.fire.setFireInfo(Blocks.oak_fence, 5, 20);
        Blocks.fire.setFireInfo(Blocks.spruce_fence, 5, 20);
        Blocks.fire.setFireInfo(Blocks.birch_fence, 5, 20);
        Blocks.fire.setFireInfo(Blocks.jungle_fence, 5, 20);
        Blocks.fire.setFireInfo(Blocks.dark_oak_fence, 5, 20);
        Blocks.fire.setFireInfo(Blocks.acacia_fence, 5, 20);
        Blocks.fire.setFireInfo(Blocks.oak_stairs, 5, 20);
        Blocks.fire.setFireInfo(Blocks.birch_stairs, 5, 20);
        Blocks.fire.setFireInfo(Blocks.spruce_stairs, 5, 20);
        Blocks.fire.setFireInfo(Blocks.jungle_stairs, 5, 20);
        Blocks.fire.setFireInfo(Blocks.log, 5, 5);
        Blocks.fire.setFireInfo(Blocks.log2, 5, 5);
        Blocks.fire.setFireInfo(Blocks.leaves, 30, 60);
        Blocks.fire.setFireInfo(Blocks.leaves2, 30, 60);
        Blocks.fire.setFireInfo(Blocks.bookshelf, 30, 20);
        Blocks.fire.setFireInfo(Blocks.tnt, 15, 100);
        Blocks.fire.setFireInfo(Blocks.tallgrass, 60, 100);
        Blocks.fire.setFireInfo(Blocks.double_plant, 60, 100);
        Blocks.fire.setFireInfo(Blocks.yellow_flower, 60, 100);
        Blocks.fire.setFireInfo(Blocks.red_flower, 60, 100);
        Blocks.fire.setFireInfo(Blocks.deadbush, 60, 100);
        Blocks.fire.setFireInfo(Blocks.wool, 30, 60);
        Blocks.fire.setFireInfo(Blocks.vine, 15, 100);
        Blocks.fire.setFireInfo(Blocks.coal_block, 5, 5);
        Blocks.fire.setFireInfo(Blocks.hay_block, 60, 20);
        Blocks.fire.setFireInfo(Blocks.carpet, 60, 20);
    }

    public void setFireInfo(Block blockIn, int encouragement, int flammability)
    {
        this.encouragements.put(blockIn, Integer.valueOf(encouragement));
        this.flammabilities.put(blockIn, Integer.valueOf(flammability));
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

    public int quantityDropped(Random random)
    {
        return 0;
    }

    public int tickRate(World worldIn)
    {
        return 30;
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.getGameRules().getBoolean("doFireTick"))
        {
            if (!this.canPlaceBlockAt(worldIn, pos))
            {
                worldIn.setBlockToAir(pos);
            }

            Block block = worldIn.getBlockState(pos.down()).getBlock();
            boolean flag = block == Blocks.netherrack;

            if (worldIn.provider instanceof WorldProviderEnd && block == Blocks.bedrock)
            {
                flag = true;
            }

            if (!flag && worldIn.isRaining() && this.canDie(worldIn, pos))
            {
                worldIn.setBlockToAir(pos);
            }
            else
            {
                int i = ((Integer)state.getValue(AGE)).intValue();

                if (i < 15)
                {
                    state = state.withProperty(AGE, Integer.valueOf(i + rand.nextInt(3) / 2));
                    worldIn.setBlockState(pos, state, 4);
                }

                worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn) + rand.nextInt(10));

                if (!flag)
                {
                    if (!this.canNeighborCatchFire(worldIn, pos))
                    {
                        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) || i > 3)
                        {
                            worldIn.setBlockToAir(pos);
                        }

                        return;
                    }

                    if (!this.canCatchFire(worldIn, pos.down()) && i == 15 && rand.nextInt(4) == 0)
                    {
                        worldIn.setBlockToAir(pos);
                        return;
                    }
                }

                boolean flag1 = worldIn.isBlockinHighHumidity(pos);
                int j = 0;

                if (flag1)
                {
                    j = -50;
                }

                this.catchOnFire(worldIn, pos.east(), 300 + j, rand, i);
                this.catchOnFire(worldIn, pos.west(), 300 + j, rand, i);
                this.catchOnFire(worldIn, pos.down(), 250 + j, rand, i);
                this.catchOnFire(worldIn, pos.up(), 250 + j, rand, i);
                this.catchOnFire(worldIn, pos.north(), 300 + j, rand, i);
                this.catchOnFire(worldIn, pos.south(), 300 + j, rand, i);

                for (int k = -1; k <= 1; ++k)
                {
                    for (int l = -1; l <= 1; ++l)
                    {
                        for (int index = -1; index <= 4; ++index)
                        {
                            if (k != 0 || index != 0 || l != 0)
                            {
                                int secondIntValue2 = 100;

                                if (index > 1)
                                {
                                    secondIntValue2 += (index - 1) * 100;
                                }

                                BlockPos blockpos = pos.add(k, index, l);
                                int thirdIntValue = this.getNeighborEncouragement(worldIn, blockpos);

                                if (thirdIntValue > 0)
                                {
                                    int fourthIntValue = (thirdIntValue + 40 + worldIn.getDifficulty().getDifficultyId() * 7) / (i + 30);

                                    if (flag1)
                                    {
                                        fourthIntValue /= 2;
                                    }

                                    if (fourthIntValue > 0 && rand.nextInt(secondIntValue2) <= fourthIntValue && (!worldIn.isRaining() || !this.canDie(worldIn, blockpos)))
                                    {
                                        int intValue2 = i + rand.nextInt(5) / 4;

                                        if (intValue2 > 15)
                                        {
                                            intValue2 = 15;
                                        }

                                        worldIn.setBlockState(blockpos, state.withProperty(AGE, Integer.valueOf(intValue2)), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean canDie(World worldIn, BlockPos pos)
    {
        return worldIn.isRainingAt(pos) || worldIn.isRainingAt(pos.west()) || worldIn.isRainingAt(pos.east()) || worldIn.isRainingAt(pos.north()) || worldIn.isRainingAt(pos.south());
    }

    public boolean requiresUpdates()
    {
        return false;
    }

    private int getFlammability(Block blockIn)
    {
        Integer integer = this.flammabilities.get(blockIn);
        return integer == null ? 0 : integer.intValue();
    }

    private int getEncouragement(Block blockIn)
    {
        Integer integer = this.encouragements.get(blockIn);
        return integer == null ? 0 : integer.intValue();
    }

    private void catchOnFire(World worldIn, BlockPos pos, int chance, Random random, int age)
    {
        int i = this.getFlammability(worldIn.getBlockState(pos).getBlock());

        if (random.nextInt(chance) < i)
        {
            IBlockState iblockstate = worldIn.getBlockState(pos);

            if (random.nextInt(age + 10) < 5 && !worldIn.isRainingAt(pos))
            {
                int j = age + random.nextInt(5) / 4;

                if (j > 15)
                {
                    j = 15;
                }

                worldIn.setBlockState(pos, this.getDefaultState().withProperty(AGE, Integer.valueOf(j)), 3);
            }
            else
            {
                worldIn.setBlockToAir(pos);
            }

            if (iblockstate.getBlock() == Blocks.tnt)
            {
                Blocks.tnt.onBlockDestroyedByPlayer(worldIn, pos, iblockstate.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
            }
        }
    }

    private boolean canNeighborCatchFire(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : EnumFacing.VALUES)
        {
            if (this.canCatchFire(worldIn, pos.offset(enumfacing)))
            {
                return true;
            }
        }

        return false;
    }

    private int getNeighborEncouragement(World worldIn, BlockPos pos)
    {
        if (!worldIn.isAirBlock(pos))
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (EnumFacing enumfacing : EnumFacing.VALUES)
            {
                i = Math.max(this.getEncouragement(worldIn.getBlockState(pos.offset(enumfacing)).getBlock()), i);
            }

            return i;
        }
    }

    public boolean isCollidable()
    {
        return false;
    }

    public boolean canCatchFire(IBlockAccess worldIn, BlockPos pos)
    {
        return this.getEncouragement(worldIn.getBlockState(pos).getBlock()) > 0;
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) || this.canNeighborCatchFire(worldIn, pos);
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && !this.canNeighborCatchFire(worldIn, pos))
        {
            worldIn.setBlockToAir(pos);
        }
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (worldIn.provider.getDimensionId() > 0 || !Blocks.portal.trySpawnPortal(worldIn, pos))
        {
            if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && !this.canNeighborCatchFire(worldIn, pos))
            {
                worldIn.setBlockToAir(pos);
            }
            else
            {
                worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn) + worldIn.rand.nextInt(10));
            }
        }
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (rand.nextInt(24) == 0)
        {
            worldIn.playSound((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), "fire.fire", 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }

        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && !Blocks.fire.canCatchFire(worldIn, pos.down()))
        {
            if (Blocks.fire.canCatchFire(worldIn, pos.west()))
            {
                for (int j = 0; j < 2; ++j)
                {
                    double twelfthDoubleValue = (double)pos.getX() + rand.nextDouble() * 0.10000000149011612D;
                    double seventeenthDoubleValue = (double)pos.getY() + rand.nextDouble();
                    double sixthDoubleValue = (double)pos.getZ() + rand.nextDouble();
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, twelfthDoubleValue, seventeenthDoubleValue, sixthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }
            }

            if (Blocks.fire.canCatchFire(worldIn, pos.east()))
            {
                for (int k = 0; k < 2; ++k)
                {
                    double thirteenthDoubleValue = (double)(pos.getX() + 1) - rand.nextDouble() * 0.10000000149011612D;
                    double eighteenthDoubleValue = (double)pos.getY() + rand.nextDouble();
                    double seventhDoubleValue = (double)pos.getZ() + rand.nextDouble();
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, thirteenthDoubleValue, eighteenthDoubleValue, seventhDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }
            }

            if (Blocks.fire.canCatchFire(worldIn, pos.north()))
            {
                for (int l = 0; l < 2; ++l)
                {
                    double fourteenthDoubleValue = (double)pos.getX() + rand.nextDouble();
                    double thirdDoubleValue = (double)pos.getY() + rand.nextDouble();
                    double eighthDoubleValue = (double)pos.getZ() + rand.nextDouble() * 0.10000000149011612D;
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, fourteenthDoubleValue, thirdDoubleValue, eighthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }
            }

            if (Blocks.fire.canCatchFire(worldIn, pos.south()))
            {
                for (int index = 0; index < 2; ++index)
                {
                    double fifteenthDoubleValue = (double)pos.getX() + rand.nextDouble();
                    double fourthDoubleValue = (double)pos.getY() + rand.nextDouble();
                    double ninthDoubleValue = (double)(pos.getZ() + 1) - rand.nextDouble() * 0.10000000149011612D;
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, fifteenthDoubleValue, fourthDoubleValue, ninthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }
            }

            if (Blocks.fire.canCatchFire(worldIn, pos.up()))
            {
                for (int innerIndex2 = 0; innerIndex2 < 2; ++innerIndex2)
                {
                    double sixteenthDoubleValue = (double)pos.getX() + rand.nextDouble();
                    double fifthDoubleValue = (double)(pos.getY() + 1) - rand.nextDouble() * 0.10000000149011612D;
                    double tenthDoubleValue = (double)pos.getZ() + rand.nextDouble();
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, sixteenthDoubleValue, fifthDoubleValue, tenthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }
            }
        }
        else
        {
            for (int i = 0; i < 3; ++i)
            {
                double doubleValue = (double)pos.getX() + rand.nextDouble();
                double secondDoubleValue = (double)pos.getY() + rand.nextDouble() * 0.5D + 0.5D;
                double eleventhDoubleValue = (double)pos.getZ() + rand.nextDouble();
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, doubleValue, secondDoubleValue, eleventhDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
            }
        }
    }

    public MapColor getMapColor(IBlockState state)
    {
        return MapColor.tntColor;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(AGE)).intValue();
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {AGE, NORTH, EAST, SOUTH, WEST, UPPER, FLIP, ALT});
    }
}
