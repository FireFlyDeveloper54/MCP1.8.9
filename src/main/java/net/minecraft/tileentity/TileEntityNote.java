package net.minecraft.tileentity;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class TileEntityNote extends TileEntity
{
    public byte note;
    public boolean previousRedstoneState;

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setByte("note", this.note);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.note = compound.getByte("note");
        this.note = (byte)MathHelper.clamp_int(this.note, 0, 24);
    }

    public void changePitch()
    {
        this.note = (byte)((this.note + 1) % 25);
        this.markDirty();
    }

    public void triggerNote(World worldIn, BlockPos pos)
    {
        if (worldIn.getBlockState(pos.up()).getBlock().getMaterial() == Material.air)
        {
            Material material = worldIn.getBlockState(pos.down()).getBlock().getMaterial();
            int i = 0;

            if (material == Material.rock)
            {
                i = 1;
            }

            if (material == Material.sand)
            {
                i = 2;
            }

            if (material == Material.glass)
            {
                i = 3;
            }

            if (material == Material.wood)
            {
                i = 4;
            }

            worldIn.addBlockEvent(pos, Blocks.noteblock, i, this.note);
        }
    }
}
