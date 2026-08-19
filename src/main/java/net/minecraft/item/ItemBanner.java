package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBanner extends ItemBlock
{
    public ItemBanner()
    {
        super(Blocks.standing_banner);
        this.maxStackSize = 16;
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (side == EnumFacing.DOWN)
        {
            return false;
        }
        else if (!worldIn.getBlockState(pos).getBlock().getMaterial().isSolid())
        {
            return false;
        }
        else
        {
            pos = pos.offset(side);

            if (!playerIn.canPlayerEdit(pos, side, stack))
            {
                return false;
            }
            else if (!Blocks.standing_banner.canPlaceBlockAt(worldIn, pos))
            {
                return false;
            }
            else if (worldIn.isRemote)
            {
                return true;
            }
            else
            {
                if (side == EnumFacing.UP)
                {
                    int rotation = MathHelper.floor_double((double)((playerIn.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                    worldIn.setBlockState(pos, Blocks.standing_banner.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(rotation)), 3);
                }
                else
                {
                    worldIn.setBlockState(pos, Blocks.wall_banner.getDefaultState().withProperty(BlockWallSign.FACING, side), 3);
                }

                --stack.stackSize;
                TileEntity tileEntity = worldIn.getTileEntity(pos);

                if (tileEntity instanceof TileEntityBanner)
                {
                    ((TileEntityBanner)tileEntity).setItemValues(stack);
                }

                return true;
            }
        }
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        String translationKey = "item.banner.";
        EnumDyeColor baseColor = this.getBaseColor(stack);
        translationKey = translationKey + baseColor.getUnlocalizedName() + ".name";
        return StatCollector.translateToLocal(translationKey);
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        NBTTagCompound blockEntityTag = stack.getSubCompound("BlockEntityTag", false);

        if (blockEntityTag != null && blockEntityTag.hasKey("Patterns"))
        {
            NBTTagList patternList = blockEntityTag.getTagList("Patterns", 10);

            for (int patternIndex = 0; patternIndex < patternList.tagCount() && patternIndex < 6; ++patternIndex)
            {
                NBTTagCompound patternTag = patternList.getCompoundTagAt(patternIndex);
                EnumDyeColor patternColor = EnumDyeColor.byDyeDamage(patternTag.getInteger("Color"));
                TileEntityBanner.EnumBannerPattern bannerPattern = TileEntityBanner.EnumBannerPattern.getPatternByID(patternTag.getString("Pattern"));

                if (bannerPattern != null)
                {
                    tooltip.add(StatCollector.translateToLocal("item.banner." + bannerPattern.getPatternName() + "." + patternColor.getUnlocalizedName()));
                }
            }
        }
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        if (renderPass == 0)
        {
            return 16777215;
        }
        else
        {
            EnumDyeColor baseColor = this.getBaseColor(stack);
            return baseColor.getMapColor().colorValue;
        }
    }

    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (EnumDyeColor dyeColor : EnumDyeColor.VALUES)
        {
            NBTTagCompound blockEntityTag = new NBTTagCompound();
            TileEntityBanner.setBaseColorAndPatterns(blockEntityTag, dyeColor.getDyeDamage(), (NBTTagList)null);
            NBTTagCompound stackTag = new NBTTagCompound();
            stackTag.setTag("BlockEntityTag", blockEntityTag);
            ItemStack itemStack = new ItemStack(itemIn, 1, dyeColor.getDyeDamage());
            itemStack.setTagCompound(stackTag);
            subItems.add(itemStack);
        }
    }

    public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabDecorations;
    }

    private EnumDyeColor getBaseColor(ItemStack stack)
    {
        NBTTagCompound blockEntityTag = stack.getSubCompound("BlockEntityTag", false);
        EnumDyeColor baseColor = null;

        if (blockEntityTag != null && blockEntityTag.hasKey("Base"))
        {
            baseColor = EnumDyeColor.byDyeDamage(blockEntityTag.getInteger("Base"));
        }
        else
        {
            baseColor = EnumDyeColor.byDyeDamage(stack.getMetadata());
        }

        return baseColor;
    }
}
