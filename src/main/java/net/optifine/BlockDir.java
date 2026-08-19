package net.optifine;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public enum BlockDir
{
    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST),
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
    DOWN_NORTH(EnumFacing.DOWN, EnumFacing.NORTH),
    DOWN_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH),
    UP_NORTH(EnumFacing.UP, EnumFacing.NORTH),
    UP_SOUTH(EnumFacing.UP, EnumFacing.SOUTH),
    DOWN_WEST(EnumFacing.DOWN, EnumFacing.WEST),
    DOWN_EAST(EnumFacing.DOWN, EnumFacing.EAST),
    UP_WEST(EnumFacing.UP, EnumFacing.WEST),
    UP_EAST(EnumFacing.UP, EnumFacing.EAST);

    private EnumFacing primaryFacing;
    private EnumFacing secondaryFacing;

    private BlockDir(EnumFacing primaryFacing)
    {
        this.primaryFacing = primaryFacing;
    }

    private BlockDir(EnumFacing primaryFacing, EnumFacing secondaryFacing)
    {
        this.primaryFacing = primaryFacing;
        this.secondaryFacing = secondaryFacing;
    }

    public EnumFacing getPrimaryFacing()
    {
        return this.primaryFacing;
    }

    public EnumFacing getSecondaryFacing()
    {
        return this.secondaryFacing;
    }

    BlockPos offset(BlockPos pos)
    {
        pos = pos.offset(this.primaryFacing, 1);

        if (this.secondaryFacing != null)
        {
            pos = pos.offset(this.secondaryFacing, 1);
        }

        return pos;
    }

    public int getOffsetX()
    {
        int offsetX = this.primaryFacing.getFrontOffsetX();

        if (this.secondaryFacing != null)
        {
            offsetX += this.secondaryFacing.getFrontOffsetX();
        }

        return offsetX;
    }

    public int getOffsetY()
    {
        int offsetY = this.primaryFacing.getFrontOffsetY();

        if (this.secondaryFacing != null)
        {
            offsetY += this.secondaryFacing.getFrontOffsetY();
        }

        return offsetY;
    }

    public int getOffsetZ()
    {
        int offsetZ = this.primaryFacing.getFrontOffsetZ();

        if (this.secondaryFacing != null)
        {
            offsetZ += this.secondaryFacing.getFrontOffsetZ();
        }

        return offsetZ;
    }

    public boolean isDouble()
    {
        return this.secondaryFacing != null;
    }
}
