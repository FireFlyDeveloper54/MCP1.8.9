package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockRedstoneOre extends Block
{
    private final boolean isOn;

    public BlockRedstoneOre(boolean isOn)
    {
        super(Material.rock);

        if (isOn)
        {
            this.setTickRandomly(true);
        }

        this.isOn = isOn;
    }

    public int tickRate(World worldIn)
    {
        return 30;
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        this.activate(worldIn, pos);
        super.onBlockClicked(worldIn, pos, playerIn);
    }

    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
    {
        this.activate(worldIn, pos);
        super.onEntityCollidedWithBlock(worldIn, pos, entityIn);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        this.activate(worldIn, pos);
        return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
    }

    private void activate(World worldIn, BlockPos pos)
    {
        this.spawnParticles(worldIn, pos);

        if (this == Blocks.redstone_ore)
        {
            worldIn.setBlockState(pos, Blocks.lit_redstone_ore.getDefaultState());
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this == Blocks.lit_redstone_ore)
        {
            worldIn.setBlockState(pos, Blocks.redstone_ore.getDefaultState());
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.redstone;
    }

    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }

    public int quantityDropped(Random random)
    {
        return 4 + random.nextInt(2);
    }

    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);

        if (this.getItemDropped(state, worldIn.rand, fortune) != Item.getItemFromBlock(this))
        {
            int i = 1 + worldIn.rand.nextInt(5);
            this.dropXpOnBlockBreak(worldIn, pos, i);
        }
    }

    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this.isOn)
        {
            this.spawnParticles(worldIn, pos);
        }
    }

    private void spawnParticles(World worldIn, BlockPos pos)
    {
        Random random = worldIn.rand;
        double doubleValue = 0.0625D;

        for (int i = 0; i < 6; ++i)
        {
            double secondDoubleValue = (double)((float)pos.getX() + random.nextFloat());
            double thirdDoubleValue = (double)((float)pos.getY() + random.nextFloat());
            double fourthDoubleValue = (double)((float)pos.getZ() + random.nextFloat());

            if (i == 0 && !worldIn.getBlockState(pos.up()).getBlock().isOpaqueCube())
            {
                thirdDoubleValue = (double)pos.getY() + doubleValue + 1.0D;
            }

            if (i == 1 && !worldIn.getBlockState(pos.down()).getBlock().isOpaqueCube())
            {
                thirdDoubleValue = (double)pos.getY() - doubleValue;
            }

            if (i == 2 && !worldIn.getBlockState(pos.south()).getBlock().isOpaqueCube())
            {
                fourthDoubleValue = (double)pos.getZ() + doubleValue + 1.0D;
            }

            if (i == 3 && !worldIn.getBlockState(pos.north()).getBlock().isOpaqueCube())
            {
                fourthDoubleValue = (double)pos.getZ() - doubleValue;
            }

            if (i == 4 && !worldIn.getBlockState(pos.east()).getBlock().isOpaqueCube())
            {
                secondDoubleValue = (double)pos.getX() + doubleValue + 1.0D;
            }

            if (i == 5 && !worldIn.getBlockState(pos.west()).getBlock().isOpaqueCube())
            {
                secondDoubleValue = (double)pos.getX() - doubleValue;
            }

            if (secondDoubleValue < (double)pos.getX() || secondDoubleValue > (double)(pos.getX() + 1) || thirdDoubleValue < 0.0D || thirdDoubleValue > (double)(pos.getY() + 1) || fourthDoubleValue < (double)pos.getZ() || fourthDoubleValue > (double)(pos.getZ() + 1))
            {
                worldIn.spawnParticle(EnumParticleTypes.REDSTONE, secondDoubleValue, thirdDoubleValue, fourthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
            }
        }
    }

    protected ItemStack createStackedBlock(IBlockState state)
    {
        return new ItemStack(Blocks.redstone_ore);
    }
}
