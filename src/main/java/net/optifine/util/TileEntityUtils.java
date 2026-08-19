package net.optifine.util;

import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.optifine.reflect.Reflector;

public class TileEntityUtils
{
    public static String getTileEntityName(IBlockAccess blockAccess, BlockPos blockPos)
    {
        TileEntity tileEntity = blockAccess.getTileEntity(blockPos);
        return getTileEntityName(tileEntity);
    }

    public static String getTileEntityName(TileEntity te)
    {
        if (!(te instanceof IWorldNameable))
        {
            return null;
        }
        else
        {
            IWorldNameable nameable = (IWorldNameable)te;
            updateTileEntityName(te);
            return !nameable.hasCustomName() ? null : nameable.getName();
        }
    }

    public static void updateTileEntityName(TileEntity te)
    {
        BlockPos blockPos = te.getPos();
        String rawName = getTileEntityRawName(te);

        if (rawName == null)
        {
            String serverRawName = getServerTileEntityRawName(blockPos);
            serverRawName = Config.normalize(serverRawName);
            setTileEntityRawName(te, serverRawName);
        }
    }

    public static String getServerTileEntityRawName(BlockPos blockPos)
    {
        TileEntity tileEntity = IntegratedServerUtils.getTileEntity(blockPos);
        return tileEntity == null ? null : getTileEntityRawName(tileEntity);
    }

    public static String getTileEntityRawName(TileEntity te)
    {
        if (te instanceof TileEntityBeacon)
        {
            return (String)Reflector.getFieldValue(te, Reflector.TileEntityBeacon_customName);
        }
        else if (te instanceof TileEntityBrewingStand)
        {
            return (String)Reflector.getFieldValue(te, Reflector.TileEntityBrewingStand_customName);
        }
        else if (te instanceof TileEntityEnchantmentTable)
        {
            return (String)Reflector.getFieldValue(te, Reflector.TileEntityEnchantmentTable_customName);
        }
        else if (te instanceof TileEntityFurnace)
        {
            return (String)Reflector.getFieldValue(te, Reflector.TileEntityFurnace_customName);
        }
        else
        {
            if (te instanceof IWorldNameable)
            {
                IWorldNameable nameable = (IWorldNameable)te;

                if (nameable.hasCustomName())
                {
                    return nameable.getName();
                }
            }

            return null;
        }
    }

    public static boolean setTileEntityRawName(TileEntity te, String name)
    {
        if (te instanceof TileEntityBeacon)
        {
            return Reflector.setFieldValue(te, Reflector.TileEntityBeacon_customName, name);
        }
        else if (te instanceof TileEntityBrewingStand)
        {
            return Reflector.setFieldValue(te, Reflector.TileEntityBrewingStand_customName, name);
        }
        else if (te instanceof TileEntityEnchantmentTable)
        {
            return Reflector.setFieldValue(te, Reflector.TileEntityEnchantmentTable_customName, name);
        }
        else if (te instanceof TileEntityFurnace)
        {
            return Reflector.setFieldValue(te, Reflector.TileEntityFurnace_customName, name);
        }
        else if (te instanceof TileEntityChest)
        {
            ((TileEntityChest)te).setCustomName(name);
            return true;
        }
        else if (te instanceof TileEntityDispenser)
        {
            ((TileEntityDispenser)te).setCustomName(name);
            return true;
        }
        else if (te instanceof TileEntityHopper)
        {
            ((TileEntityHopper)te).setCustomName(name);
            return true;
        }
        else
        {
            return false;
        }
    }
}
