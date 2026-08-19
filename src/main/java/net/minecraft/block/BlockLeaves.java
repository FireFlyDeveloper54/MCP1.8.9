package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

public abstract class BlockLeaves extends BlockLeavesBase
{
    public static final PropertyBool DECAYABLE = PropertyBool.create("decayable");
    public static final PropertyBool CHECK_DECAY = PropertyBool.create("check_decay");
    int[] surroundings;
    protected int iconIndex;
    protected boolean isTransparent;

    public BlockLeaves()
    {
        super(Material.leaves, false);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(0.2F);
        this.setLightOpacity(1);
        this.setStepSound(soundTypeGrass);
    }

    public int getBlockColor()
    {
        return ColorizerFoliage.getFoliageColor(0.5D, 1.0D);
    }

    public int getRenderColor(IBlockState state)
    {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        return BiomeColorHelper.getFoliageColorAtPos(worldIn, pos);
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = 1;
        int j = i + 1;
        int k = pos.getX();
        int l = pos.getY();
        int zCoordinate = pos.getZ();

        if (worldIn.isAreaLoaded(new BlockPos(k - j, l - j, zCoordinate - j), new BlockPos(k + j, l + j, zCoordinate + j)))
        {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int innerIndex = -i; innerIndex <= i; ++innerIndex)
            {
                for (int nestedIndex = -i; nestedIndex <= i; ++nestedIndex)
                {
                    for (int outerIndex = -i; outerIndex <= i; ++outerIndex)
                    {
                        BlockPos blockPos = blockpos$mutableblockpos.set(k + innerIndex, l + nestedIndex, zCoordinate + outerIndex);
                        IBlockState iblockstate = worldIn.getBlockState(blockPos);

                        if (iblockstate.getBlock().getMaterial() == Material.leaves && !((Boolean)iblockstate.getValue(CHECK_DECAY)).booleanValue())
                        {
                            worldIn.setBlockState(blockPos, iblockstate.withProperty(CHECK_DECAY, Boolean.valueOf(true)), 4);
                        }
                    }
                }
            }
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            if (((Boolean)state.getValue(CHECK_DECAY)).booleanValue() && ((Boolean)state.getValue(DECAYABLE)).booleanValue())
            {
                int i = 4;
                int j = i + 1;
                int k = pos.getX();
                int l = pos.getY();
                int zCoordinate = pos.getZ();
                int secondIntValue2 = 32;
                int thirdIntValue = secondIntValue2 * secondIntValue2;
                int fourthIntValue = secondIntValue2 / 2;

                if (this.surroundings == null)
                {
                    this.surroundings = new int[secondIntValue2 * secondIntValue2 * secondIntValue2];
                }

                if (worldIn.isAreaLoaded(new BlockPos(k - j, l - j, zCoordinate - j), new BlockPos(k + j, l + j, zCoordinate + j)))
                {
                    BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                    for (int index = -i; index <= i; ++index)
                    {
                        for (int index2 = -i; index2 <= i; ++index2)
                        {
                            for (int index3 = -i; index3 <= i; ++index3)
                            {
                                Block block = worldIn.getBlockState(blockpos$mutableblockpos.set(k + index, l + index2, zCoordinate + index3)).getBlock();

                                if (block != Blocks.log && block != Blocks.log2)
                                {
                                    if (block.getMaterial() == Material.leaves)
                                    {
                                        this.surroundings[(index + fourthIntValue) * thirdIntValue + (index2 + fourthIntValue) * secondIntValue2 + index3 + fourthIntValue] = -2;
                                    }
                                    else
                                    {
                                        this.surroundings[(index + fourthIntValue) * thirdIntValue + (index2 + fourthIntValue) * secondIntValue2 + index3 + fourthIntValue] = -1;
                                    }
                                }
                                else
                                {
                                    this.surroundings[(index + fourthIntValue) * thirdIntValue + (index2 + fourthIntValue) * secondIntValue2 + index3 + fourthIntValue] = 0;
                                }
                            }
                        }
                    }

                    for (int index4 = 1; index4 <= 4; ++index4)
                    {
                        for (int index5 = -i; index5 <= i; ++index5)
                        {
                            for (int index6 = -i; index6 <= i; ++index6)
                            {
                                for (int index7 = -i; index7 <= i; ++index7)
                                {
                                    if (this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue] == index4 - 1)
                                    {
                                        if (this.surroundings[(index5 + fourthIntValue - 1) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue] == -2)
                                        {
                                            this.surroundings[(index5 + fourthIntValue - 1) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue] = index4;
                                        }

                                        if (this.surroundings[(index5 + fourthIntValue + 1) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue] == -2)
                                        {
                                            this.surroundings[(index5 + fourthIntValue + 1) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue] = index4;
                                        }

                                        if (this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue - 1) * secondIntValue2 + index7 + fourthIntValue] == -2)
                                        {
                                            this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue - 1) * secondIntValue2 + index7 + fourthIntValue] = index4;
                                        }

                                        if (this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue + 1) * secondIntValue2 + index7 + fourthIntValue] == -2)
                                        {
                                            this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue + 1) * secondIntValue2 + index7 + fourthIntValue] = index4;
                                        }

                                        if (this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + (index7 + fourthIntValue - 1)] == -2)
                                        {
                                            this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + (index7 + fourthIntValue - 1)] = index4;
                                        }

                                        if (this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue + 1] == -2)
                                        {
                                            this.surroundings[(index5 + fourthIntValue) * thirdIntValue + (index6 + fourthIntValue) * secondIntValue2 + index7 + fourthIntValue + 1] = index4;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                int intValue = this.surroundings[fourthIntValue * thirdIntValue + fourthIntValue * secondIntValue2 + fourthIntValue];

                if (intValue >= 0)
                {
                    worldIn.setBlockState(pos, state.withProperty(CHECK_DECAY, Boolean.valueOf(false)), 4);
                }
                else
                {
                    this.destroy(worldIn, pos);
                }
            }
        }
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.isRainingAt(pos.up()) && !World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && rand.nextInt(15) == 1)
        {
            double doubleValue = (double)((float)pos.getX() + rand.nextFloat());
            double secondDoubleValue = (double)pos.getY() - 0.05D;
            double thirdDoubleValue = (double)((float)pos.getZ() + rand.nextFloat());
            worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, doubleValue, secondDoubleValue, thirdDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
        }
    }

    private void destroy(World worldIn, BlockPos pos)
    {
        this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
        worldIn.setBlockToAir(pos);
    }

    public int quantityDropped(Random random)
    {
        return random.nextInt(20) == 0 ? 1 : 0;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Item.getItemFromBlock(Blocks.sapling);
    }

    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        if (!worldIn.isRemote)
        {
            int i = this.getSaplingDropChance(state);

            if (fortune > 0)
            {
                i -= 2 << fortune;

                if (i < 10)
                {
                    i = 10;
                }
            }

            if (worldIn.rand.nextInt(i) == 0)
            {
                Item item = this.getItemDropped(state, worldIn.rand, fortune);
                spawnAsEntity(worldIn, pos, new ItemStack(item, 1, this.damageDropped(state)));
            }

            i = 200;

            if (fortune > 0)
            {
                i -= 10 << fortune;

                if (i < 40)
                {
                    i = 40;
                }
            }

            this.dropApple(worldIn, pos, state, i);
        }
    }

    protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance)
    {
    }

    protected int getSaplingDropChance(IBlockState state)
    {
        return 20;
    }

    public boolean isOpaqueCube()
    {
        return !this.fancyGraphics;
    }

    public void setGraphicsLevel(boolean fancy)
    {
        this.isTransparent = fancy;
        this.fancyGraphics = fancy;
        this.iconIndex = fancy ? 0 : 1;
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return this.isTransparent ? EnumWorldBlockLayer.CUTOUT_MIPPED : EnumWorldBlockLayer.SOLID;
    }

    public boolean isVisuallyOpaque()
    {
        return false;
    }

    public abstract BlockPlanks.EnumType getWoodType(int meta);
}
