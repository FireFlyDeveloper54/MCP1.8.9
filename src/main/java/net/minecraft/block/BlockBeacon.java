package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class BlockBeacon extends BlockContainer
{
    public BlockBeacon()
    {
        super(Material.glass, MapColor.diamondColor);
        this.setHardness(3.0F);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBeacon();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            if (tileEntity instanceof TileEntityBeacon)
            {
                playerIn.displayGUIChest((TileEntityBeacon)tileEntity);
                playerIn.triggerAchievement(StatList.beaconInteractionStat);
            }

            return true;
        }
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean isFullCube()
    {
        return false;
    }

    public int getRenderType()
    {
        return 3;
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (stack.hasDisplayName())
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            if (tileEntity instanceof TileEntityBeacon)
            {
                ((TileEntityBeacon)tileEntity).setName(stack.getDisplayName());
            }
        }
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityBeacon)
        {
            ((TileEntityBeacon)tileEntity).updateBeacon();
            worldIn.addBlockEvent(pos, this, 1, 0);
        }
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    public static void updateColorAsync(final World worldIn, final BlockPos glassPos)
    {
        HttpUtil.DOWNLOAD_EXECUTOR.submit(new Runnable()
        {
            public void run()
            {
                Chunk chunk = worldIn.getChunkFromBlockCoords(glassPos);

                for (int y = glassPos.getY() - 1; y >= 0; --y)
                {
                    final BlockPos blockPos = new BlockPos(glassPos.getX(), y, glassPos.getZ());

                    if (!chunk.canSeeSky(blockPos))
                    {
                        break;
                    }

                    IBlockState iblockstate = worldIn.getBlockState(blockPos);

                    if (iblockstate.getBlock() == Blocks.beacon)
                    {
                        ((WorldServer)worldIn).addScheduledTask(new Runnable()
                        {
                            public void run()
                            {
                                TileEntity tileEntity = worldIn.getTileEntity(blockPos);

                                if (tileEntity instanceof TileEntityBeacon)
                                {
                                    ((TileEntityBeacon)tileEntity).updateBeacon();
                                    worldIn.addBlockEvent(blockPos, Blocks.beacon, 1, 0);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
