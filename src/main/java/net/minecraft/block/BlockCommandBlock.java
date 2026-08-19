package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockCommandBlock extends BlockContainer
{
    public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");

    public BlockCommandBlock()
    {
        super(Material.iron, MapColor.adobeColor);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TRIGGERED, Boolean.valueOf(false)));
    }

    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityCommandBlock();
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!worldIn.isRemote)
        {
            boolean powered = worldIn.isBlockPowered(pos);
            boolean wasTriggered = ((Boolean)state.getValue(TRIGGERED)).booleanValue();

            if (powered && !wasTriggered)
            {
                worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(true)), 4);
                worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            }
            else if (!powered && wasTriggered)
            {
                worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(false)), 4);
            }
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityCommandBlock)
        {
            ((TileEntityCommandBlock)tileEntity).getCommandBlockLogic().trigger(worldIn);
            worldIn.updateComparatorOutputLevel(pos, this);
        }
    }

    public int tickRate(World worldIn)
    {
        return 1;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof TileEntityCommandBlock ? ((TileEntityCommandBlock)tileEntity).getCommandBlockLogic().tryOpenEditCommandBlock(playerIn) : false;
    }

    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    public int getComparatorInputOverride(World worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof TileEntityCommandBlock ? ((TileEntityCommandBlock)tileEntity).getCommandBlockLogic().getSuccessCount() : 0;
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityCommandBlock)
        {
            CommandBlockLogic commandBlockLogic = ((TileEntityCommandBlock)tileEntity).getCommandBlockLogic();

            if (stack.hasDisplayName())
            {
                commandBlockLogic.setName(stack.getDisplayName());
            }

            if (!worldIn.isRemote)
            {
                commandBlockLogic.setTrackOutput(worldIn.getGameRules().getBoolean("sendCommandFeedback"));
            }
        }
    }

    public int quantityDropped(Random random)
    {
        return 0;
    }

    public int getRenderType()
    {
        return 3;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TRIGGERED, Boolean.valueOf((meta & 1) > 0));
    }

    public int getMetaFromState(IBlockState state)
    {
        int metadata = 0;

        if (((Boolean)state.getValue(TRIGGERED)).booleanValue())
        {
            metadata |= 1;
        }

        return metadata;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {TRIGGERED});
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(TRIGGERED, Boolean.valueOf(false));
    }
}
