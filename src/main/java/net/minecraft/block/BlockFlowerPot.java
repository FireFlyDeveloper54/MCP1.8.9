package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFlowerPot extends BlockContainer
{
    public static final PropertyInteger LEGACY_DATA = PropertyInteger.create("legacy_data", 0, 15);
    public static final PropertyEnum<BlockFlowerPot.EnumFlowerType> CONTENTS = PropertyEnum.<BlockFlowerPot.EnumFlowerType>create("contents", BlockFlowerPot.EnumFlowerType.class);

    public BlockFlowerPot()
    {
        super(Material.circuits);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CONTENTS, BlockFlowerPot.EnumFlowerType.EMPTY).withProperty(LEGACY_DATA, Integer.valueOf(0)));
        this.setBlockBoundsForItemRender();
    }

    public String getLocalizedName()
    {
        return StatCollector.translateToLocal("item.flowerPot.name");
    }

    public void setBlockBoundsForItemRender()
    {
        float potWidth = 0.375F;
        float halfWidth = potWidth / 2.0F;
        this.setBlockBounds(0.5F - halfWidth, 0.0F, 0.5F - halfWidth, 0.5F + halfWidth, potWidth, 0.5F + halfWidth);
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public int getRenderType()
    {
        return 3;
    }

    public boolean isFullCube()
    {
        return false;
    }

    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityFlowerPot)
        {
            Item flowerItem = ((TileEntityFlowerPot)tileEntity).getFlowerPotItem();

            if (flowerItem instanceof ItemBlock)
            {
                return Block.getBlockFromItem(flowerItem).colorMultiplier(worldIn, pos, renderPass);
            }
        }

        return 16777215;
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack heldStack = playerIn.inventory.getCurrentItem();

        if (heldStack != null && heldStack.getItem() instanceof ItemBlock)
        {
            TileEntityFlowerPot flowerPot = this.getTileEntity(worldIn, pos);

            if (flowerPot == null)
            {
                return false;
            }
            else if (flowerPot.getFlowerPotItem() != null)
            {
                return false;
            }
            else
            {
                Block heldBlock = Block.getBlockFromItem(heldStack.getItem());

                if (!this.canNotContain(heldBlock, heldStack.getMetadata()))
                {
                    return false;
                }
                else
                {
                    flowerPot.setFlowerPotData(heldStack.getItem(), heldStack.getMetadata());
                    flowerPot.markDirty();
                    worldIn.markBlockForUpdate(pos);
                    playerIn.triggerAchievement(StatList.flowerPottedStat);

                    if (!playerIn.capabilities.isCreativeMode && --heldStack.stackSize <= 0)
                    {
                        playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, (ItemStack)null);
                    }

                    return true;
                }
            }
        }
        else
        {
            return false;
        }
    }

    private boolean canNotContain(Block blockIn, int meta)
    {
        return blockIn != Blocks.yellow_flower && blockIn != Blocks.red_flower && blockIn != Blocks.cactus && blockIn != Blocks.brown_mushroom && blockIn != Blocks.red_mushroom && blockIn != Blocks.sapling && blockIn != Blocks.deadbush ? blockIn == Blocks.tallgrass && meta == BlockTallGrass.EnumType.FERN.getMeta() : true;
    }

    public Item getItem(World worldIn, BlockPos pos)
    {
        TileEntityFlowerPot flowerPot = this.getTileEntity(worldIn, pos);
        return flowerPot != null && flowerPot.getFlowerPotItem() != null ? flowerPot.getFlowerPotItem() : Items.flower_pot;
    }

    public int getDamageValue(World worldIn, BlockPos pos)
    {
        TileEntityFlowerPot flowerPot = this.getTileEntity(worldIn, pos);
        return flowerPot != null && flowerPot.getFlowerPotItem() != null ? flowerPot.getFlowerPotData() : 0;
    }

    public boolean isFlowerPot()
    {
        return true;
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return super.canPlaceBlockAt(worldIn, pos) && World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down()))
        {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntityFlowerPot flowerPot = this.getTileEntity(worldIn, pos);

        if (flowerPot != null && flowerPot.getFlowerPotItem() != null)
        {
            spawnAsEntity(worldIn, pos, new ItemStack(flowerPot.getFlowerPotItem(), 1, flowerPot.getFlowerPotData()));
        }

        super.breakBlock(worldIn, pos, state);
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        super.onBlockHarvested(worldIn, pos, state, player);

        if (player.capabilities.isCreativeMode)
        {
            TileEntityFlowerPot flowerPot = this.getTileEntity(worldIn, pos);

            if (flowerPot != null)
            {
                flowerPot.setFlowerPotData((Item)null, 0);
            }
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.flower_pot;
    }

    private TileEntityFlowerPot getTileEntity(World worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)tileEntity : null;
    }

    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        Block containedBlock = null;
        int containedMeta = 0;

        switch (meta)
        {
            case 1:
                containedBlock = Blocks.red_flower;
                containedMeta = BlockFlower.EnumFlowerType.POPPY.getMeta();
                break;

            case 2:
                containedBlock = Blocks.yellow_flower;
                break;

            case 3:
                containedBlock = Blocks.sapling;
                containedMeta = BlockPlanks.EnumType.OAK.getMetadata();
                break;

            case 4:
                containedBlock = Blocks.sapling;
                containedMeta = BlockPlanks.EnumType.SPRUCE.getMetadata();
                break;

            case 5:
                containedBlock = Blocks.sapling;
                containedMeta = BlockPlanks.EnumType.BIRCH.getMetadata();
                break;

            case 6:
                containedBlock = Blocks.sapling;
                containedMeta = BlockPlanks.EnumType.JUNGLE.getMetadata();
                break;

            case 7:
                containedBlock = Blocks.red_mushroom;
                break;

            case 8:
                containedBlock = Blocks.brown_mushroom;
                break;

            case 9:
                containedBlock = Blocks.cactus;
                break;

            case 10:
                containedBlock = Blocks.deadbush;
                break;

            case 11:
                containedBlock = Blocks.tallgrass;
                containedMeta = BlockTallGrass.EnumType.FERN.getMeta();
                break;

            case 12:
                containedBlock = Blocks.sapling;
                containedMeta = BlockPlanks.EnumType.ACACIA.getMetadata();
                break;

            case 13:
                containedBlock = Blocks.sapling;
                containedMeta = BlockPlanks.EnumType.DARK_OAK.getMetadata();
        }

        return new TileEntityFlowerPot(Item.getItemFromBlock(containedBlock), containedMeta);
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {CONTENTS, LEGACY_DATA});
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(LEGACY_DATA)).intValue();
    }

    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        BlockFlowerPot.EnumFlowerType flowerType = BlockFlowerPot.EnumFlowerType.EMPTY;
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileEntityFlowerPot)
        {
            TileEntityFlowerPot flowerPot = (TileEntityFlowerPot)tileEntity;
            Item flowerItem = flowerPot.getFlowerPotItem();

            if (flowerItem instanceof ItemBlock)
            {
                int flowerMeta = flowerPot.getFlowerPotData();
                Block flowerBlock = Block.getBlockFromItem(flowerItem);

                if (flowerBlock == Blocks.sapling)
                {
                    switch (BlockPlanks.EnumType.byMetadata(flowerMeta))
                    {
                        case OAK:
                            flowerType = BlockFlowerPot.EnumFlowerType.OAK_SAPLING;
                            break;

                        case SPRUCE:
                            flowerType = BlockFlowerPot.EnumFlowerType.SPRUCE_SAPLING;
                            break;

                        case BIRCH:
                            flowerType = BlockFlowerPot.EnumFlowerType.BIRCH_SAPLING;
                            break;

                        case JUNGLE:
                            flowerType = BlockFlowerPot.EnumFlowerType.JUNGLE_SAPLING;
                            break;

                        case ACACIA:
                            flowerType = BlockFlowerPot.EnumFlowerType.ACACIA_SAPLING;
                            break;

                        case DARK_OAK:
                            flowerType = BlockFlowerPot.EnumFlowerType.DARK_OAK_SAPLING;
                            break;

                        default:
                            flowerType = BlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                }
                else if (flowerBlock == Blocks.tallgrass)
                {
                    switch (flowerMeta)
                    {
                        case 0:
                            flowerType = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                            break;

                        case 2:
                            flowerType = BlockFlowerPot.EnumFlowerType.FERN;
                            break;

                        default:
                            flowerType = BlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                }
                else if (flowerBlock == Blocks.yellow_flower)
                {
                    flowerType = BlockFlowerPot.EnumFlowerType.DANDELION;
                }
                else if (flowerBlock == Blocks.red_flower)
                {
                    switch (BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, flowerMeta))
                    {
                        case POPPY:
                            flowerType = BlockFlowerPot.EnumFlowerType.POPPY;
                            break;

                        case BLUE_ORCHID:
                            flowerType = BlockFlowerPot.EnumFlowerType.BLUE_ORCHID;
                            break;

                        case ALLIUM:
                            flowerType = BlockFlowerPot.EnumFlowerType.ALLIUM;
                            break;

                        case HOUSTONIA:
                            flowerType = BlockFlowerPot.EnumFlowerType.HOUSTONIA;
                            break;

                        case RED_TULIP:
                            flowerType = BlockFlowerPot.EnumFlowerType.RED_TULIP;
                            break;

                        case ORANGE_TULIP:
                            flowerType = BlockFlowerPot.EnumFlowerType.ORANGE_TULIP;
                            break;

                        case WHITE_TULIP:
                            flowerType = BlockFlowerPot.EnumFlowerType.WHITE_TULIP;
                            break;

                        case PINK_TULIP:
                            flowerType = BlockFlowerPot.EnumFlowerType.PINK_TULIP;
                            break;

                        case OXEYE_DAISY:
                            flowerType = BlockFlowerPot.EnumFlowerType.OXEYE_DAISY;
                            break;

                        default:
                            flowerType = BlockFlowerPot.EnumFlowerType.EMPTY;
                    }
                }
                else if (flowerBlock == Blocks.red_mushroom)
                {
                    flowerType = BlockFlowerPot.EnumFlowerType.MUSHROOM_RED;
                }
                else if (flowerBlock == Blocks.brown_mushroom)
                {
                    flowerType = BlockFlowerPot.EnumFlowerType.MUSHROOM_BROWN;
                }
                else if (flowerBlock == Blocks.deadbush)
                {
                    flowerType = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                }
                else if (flowerBlock == Blocks.cactus)
                {
                    flowerType = BlockFlowerPot.EnumFlowerType.CACTUS;
                }
            }
        }

        return state.withProperty(CONTENTS, flowerType);
    }

    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT;
    }

    public static enum EnumFlowerType implements IStringSerializable
    {
        EMPTY("empty"),
        POPPY("rose"),
        BLUE_ORCHID("blue_orchid"),
        ALLIUM("allium"),
        HOUSTONIA("houstonia"),
        RED_TULIP("red_tulip"),
        ORANGE_TULIP("orange_tulip"),
        WHITE_TULIP("white_tulip"),
        PINK_TULIP("pink_tulip"),
        OXEYE_DAISY("oxeye_daisy"),
        DANDELION("dandelion"),
        OAK_SAPLING("oak_sapling"),
        SPRUCE_SAPLING("spruce_sapling"),
        BIRCH_SAPLING("birch_sapling"),
        JUNGLE_SAPLING("jungle_sapling"),
        ACACIA_SAPLING("acacia_sapling"),
        DARK_OAK_SAPLING("dark_oak_sapling"),
        MUSHROOM_RED("mushroom_red"),
        MUSHROOM_BROWN("mushroom_brown"),
        DEAD_BUSH("dead_bush"),
        FERN("fern"),
        CACTUS("cactus");

        private final String name;

        private EnumFlowerType(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return this.name;
        }

        public String getName()
        {
            return this.name;
        }
    }
}
