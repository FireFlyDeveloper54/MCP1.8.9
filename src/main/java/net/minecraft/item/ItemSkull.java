package net.minecraft.item;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemSkull extends Item
{
    private static final String[] skullTypes = new String[] {"skeleton", "wither", "zombie", "char", "creeper"};

    public ItemSkull()
    {
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (side == EnumFacing.DOWN)
        {
            return false;
        }
        else
        {
            IBlockState blockState = worldIn.getBlockState(pos);
            Block block = blockState.getBlock();
            boolean isReplaceable = block.isReplaceable(worldIn, pos);

            if (!isReplaceable)
            {
                if (!worldIn.getBlockState(pos).getBlock().getMaterial().isSolid())
                {
                    return false;
                }

                pos = pos.offset(side);
            }

            if (!playerIn.canPlayerEdit(pos, side, stack))
            {
                return false;
            }
            else if (!Blocks.skull.canPlaceBlockAt(worldIn, pos))
            {
                return false;
            }
            else
            {
                if (!worldIn.isRemote)
                {
                    worldIn.setBlockState(pos, Blocks.skull.getDefaultState().withProperty(BlockSkull.FACING, side), 3);
                    int skullRotation = 0;

                    if (side == EnumFacing.UP)
                    {
                        skullRotation = MathHelper.floor_double((double)(playerIn.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
                    }

                    TileEntity tileEntity = worldIn.getTileEntity(pos);

                    if (tileEntity instanceof TileEntitySkull)
                    {
                        TileEntitySkull tileEntitySkull = (TileEntitySkull)tileEntity;

                        if (stack.getMetadata() == 3)
                        {
                            GameProfile gameProfile = null;

                            if (stack.hasTagCompound())
                            {
                                NBTTagCompound stackTag = stack.getTagCompound();

                                if (stackTag.hasKey("SkullOwner", 10))
                                {
                                    gameProfile = NBTUtil.readGameProfileFromNBT(stackTag.getCompoundTag("SkullOwner"));
                                }
                                else if (stackTag.hasKey("SkullOwner", 8) && stackTag.getString("SkullOwner").length() > 0)
                                {
                                    gameProfile = new GameProfile((UUID)null, stackTag.getString("SkullOwner"));
                                }
                            }

                            tileEntitySkull.setPlayerProfile(gameProfile);
                        }
                        else
                        {
                            tileEntitySkull.setType(stack.getMetadata());
                        }

                        tileEntitySkull.setSkullRotation(skullRotation);
                        Blocks.skull.checkWitherSpawn(worldIn, pos, tileEntitySkull);
                    }

                    --stack.stackSize;
                }

                return true;
            }
        }
    }

    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int skullTypeIndex = 0; skullTypeIndex < skullTypes.length; ++skullTypeIndex)
        {
            subItems.add(new ItemStack(itemIn, 1, skullTypeIndex));
        }
    }

    public int getMetadata(int damage)
    {
        return damage;
    }

    public String getUnlocalizedName(ItemStack stack)
    {
        int skullMetadata = stack.getMetadata();

        if (skullMetadata < 0 || skullMetadata >= skullTypes.length)
        {
            skullMetadata = 0;
        }

        return super.getUnlocalizedName() + "." + skullTypes[skullMetadata];
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        if (stack.getMetadata() == 3 && stack.hasTagCompound())
        {
            if (stack.getTagCompound().hasKey("SkullOwner", 8))
            {
                return StatCollector.translateToLocalFormatted("item.skull.player.name", new Object[] {stack.getTagCompound().getString("SkullOwner")});
            }

            if (stack.getTagCompound().hasKey("SkullOwner", 10))
            {
                NBTTagCompound skullOwnerTag = stack.getTagCompound().getCompoundTag("SkullOwner");

                if (skullOwnerTag.hasKey("Name", 8))
                {
                    return StatCollector.translateToLocalFormatted("item.skull.player.name", new Object[] {skullOwnerTag.getString("Name")});
                }
            }
        }

        return super.getItemStackDisplayName(stack);
    }

    public boolean updateItemStackNBT(NBTTagCompound nbt)
    {
        super.updateItemStackNBT(nbt);

        if (nbt.hasKey("SkullOwner", 8) && nbt.getString("SkullOwner").length() > 0)
        {
            GameProfile gameProfile = new GameProfile((UUID)null, nbt.getString("SkullOwner"));
            gameProfile = TileEntitySkull.updateGameprofile(gameProfile);
            nbt.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameProfile));
            return true;
        }
        else
        {
            return false;
        }
    }
}
