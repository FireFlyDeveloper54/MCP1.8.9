package net.minecraft.world.gen.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

public abstract class StructureComponent
{
    protected StructureBoundingBox boundingBox;
    protected EnumFacing coordBaseMode;
    protected int componentType;

    public StructureComponent()
    {
    }

    protected StructureComponent(int type)
    {
        this.componentType = type;
    }

    public NBTTagCompound createStructureBaseNBT()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        nBTTagCompound.setString("id", MapGenStructureIO.getStructureComponentName(this));
        nBTTagCompound.setTag("BB", this.boundingBox.toNBTTagIntArray());
        nBTTagCompound.setInteger("O", this.coordBaseMode == null ? -1 : this.coordBaseMode.getHorizontalIndex());
        nBTTagCompound.setInteger("GD", this.componentType);
        this.writeStructureToNBT(nBTTagCompound);
        return nBTTagCompound;
    }

    protected abstract void writeStructureToNBT(NBTTagCompound tagCompound);

    public void readStructureBaseNBT(World worldIn, NBTTagCompound tagCompound)
    {
        if (tagCompound.hasKey("BB"))
        {
            this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
        }

        int orientationIndex = tagCompound.getInteger("O");
        this.coordBaseMode = orientationIndex == -1 ? null : EnumFacing.getHorizontal(orientationIndex);
        this.componentType = tagCompound.getInteger("GD");
        this.readStructureFromNBT(tagCompound);
    }

    protected abstract void readStructureFromNBT(NBTTagCompound tagCompound);

    public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
    {
    }

    public abstract boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn);

    public StructureBoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    public int getComponentType()
    {
        return this.componentType;
    }

    public static StructureComponent findIntersecting(List<StructureComponent> listIn, StructureBoundingBox boundingboxIn)
    {
        for (StructureComponent structureComponent : listIn)
        {
            if (structureComponent.getBoundingBox() != null && structureComponent.getBoundingBox().intersectsWith(boundingboxIn))
            {
                return structureComponent;
            }
        }

        return null;
    }

    public BlockPos getBoundingBoxCenter()
    {
        return new BlockPos(this.boundingBox.getCenter());
    }

    protected boolean isLiquidInStructureBoundingBox(World worldIn, StructureBoundingBox boundingboxIn)
    {
        int minX = Math.max(this.boundingBox.minX - 1, boundingboxIn.minX);
        int minY = Math.max(this.boundingBox.minY - 1, boundingboxIn.minY);
        int minZ = Math.max(this.boundingBox.minZ - 1, boundingboxIn.minZ);
        int maxX = Math.min(this.boundingBox.maxX + 1, boundingboxIn.maxX);
        int maxY = Math.min(this.boundingBox.maxY + 1, boundingboxIn.maxY);
        int maxZ = Math.min(this.boundingBox.maxZ + 1, boundingboxIn.maxZ);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; ++x)
        {
            for (int z = minZ; z <= maxZ; ++z)
            {
                if (worldIn.getBlockState(mutablePos.set(x, minY, z)).getBlock().getMaterial().isLiquid())
                {
                    return true;
                }

                if (worldIn.getBlockState(mutablePos.set(x, maxY, z)).getBlock().getMaterial().isLiquid())
                {
                    return true;
                }
            }
        }

        for (int x = minX; x <= maxX; ++x)
        {
            for (int y = minY; y <= maxY; ++y)
            {
                if (worldIn.getBlockState(mutablePos.set(x, y, minZ)).getBlock().getMaterial().isLiquid())
                {
                    return true;
                }

                if (worldIn.getBlockState(mutablePos.set(x, y, maxZ)).getBlock().getMaterial().isLiquid())
                {
                    return true;
                }
            }
        }

        for (int z = minZ; z <= maxZ; ++z)
        {
            for (int y = minY; y <= maxY; ++y)
            {
                if (worldIn.getBlockState(mutablePos.set(minX, y, z)).getBlock().getMaterial().isLiquid())
                {
                    return true;
                }

                if (worldIn.getBlockState(mutablePos.set(maxX, y, z)).getBlock().getMaterial().isLiquid())
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected int getXWithOffset(int x, int z)
    {
        if (this.coordBaseMode == null)
        {
            return x;
        }
        else
        {
            switch (this.coordBaseMode)
            {
                case NORTH:
                case SOUTH:
                    return this.boundingBox.minX + x;

                case WEST:
                    return this.boundingBox.maxX - z;

                case EAST:
                    return this.boundingBox.minX + z;

                default:
                    return x;
            }
        }
    }

    protected int getYWithOffset(int y)
    {
        return this.coordBaseMode == null ? y : y + this.boundingBox.minY;
    }

    protected int getZWithOffset(int x, int z)
    {
        if (this.coordBaseMode == null)
        {
            return z;
        }
        else
        {
            switch (this.coordBaseMode)
            {
                case NORTH:
                    return this.boundingBox.maxZ - z;

                case SOUTH:
                    return this.boundingBox.minZ + z;

                case WEST:
                case EAST:
                    return this.boundingBox.minZ + x;

                default:
                    return z;
            }
        }
    }

    protected int getMetadataWithOffset(Block blockIn, int meta)
    {
        if (blockIn == Blocks.rail)
        {
            if (this.coordBaseMode == EnumFacing.WEST || this.coordBaseMode == EnumFacing.EAST)
            {
                if (meta == 1)
                {
                    return 0;
                }

                return 1;
            }
        }
        else if (blockIn instanceof BlockDoor)
        {
            if (this.coordBaseMode == EnumFacing.SOUTH)
            {
                if (meta == 0)
                {
                    return 2;
                }

                if (meta == 2)
                {
                    return 0;
                }
            }
            else
            {
                if (this.coordBaseMode == EnumFacing.WEST)
                {
                    return meta + 1 & 3;
                }

                if (this.coordBaseMode == EnumFacing.EAST)
                {
                    return meta + 3 & 3;
                }
            }
        }
        else if (blockIn != Blocks.stone_stairs && blockIn != Blocks.oak_stairs && blockIn != Blocks.nether_brick_stairs && blockIn != Blocks.stone_brick_stairs && blockIn != Blocks.sandstone_stairs)
        {
            if (blockIn == Blocks.ladder)
            {
                if (this.coordBaseMode == EnumFacing.SOUTH)
                {
                    if (meta == EnumFacing.NORTH.getIndex())
                    {
                        return EnumFacing.SOUTH.getIndex();
                    }

                    if (meta == EnumFacing.SOUTH.getIndex())
                    {
                        return EnumFacing.NORTH.getIndex();
                    }
                }
                else if (this.coordBaseMode == EnumFacing.WEST)
                {
                    if (meta == EnumFacing.NORTH.getIndex())
                    {
                        return EnumFacing.WEST.getIndex();
                    }

                    if (meta == EnumFacing.SOUTH.getIndex())
                    {
                        return EnumFacing.EAST.getIndex();
                    }

                    if (meta == EnumFacing.WEST.getIndex())
                    {
                        return EnumFacing.NORTH.getIndex();
                    }

                    if (meta == EnumFacing.EAST.getIndex())
                    {
                        return EnumFacing.SOUTH.getIndex();
                    }
                }
                else if (this.coordBaseMode == EnumFacing.EAST)
                {
                    if (meta == EnumFacing.NORTH.getIndex())
                    {
                        return EnumFacing.EAST.getIndex();
                    }

                    if (meta == EnumFacing.SOUTH.getIndex())
                    {
                        return EnumFacing.WEST.getIndex();
                    }

                    if (meta == EnumFacing.WEST.getIndex())
                    {
                        return EnumFacing.NORTH.getIndex();
                    }

                    if (meta == EnumFacing.EAST.getIndex())
                    {
                        return EnumFacing.SOUTH.getIndex();
                    }
                }
            }
            else if (blockIn == Blocks.stone_button)
            {
                if (this.coordBaseMode == EnumFacing.SOUTH)
                {
                    if (meta == 3)
                    {
                        return 4;
                    }

                    if (meta == 4)
                    {
                        return 3;
                    }
                }
                else if (this.coordBaseMode == EnumFacing.WEST)
                {
                    if (meta == 3)
                    {
                        return 1;
                    }

                    if (meta == 4)
                    {
                        return 2;
                    }

                    if (meta == 2)
                    {
                        return 3;
                    }

                    if (meta == 1)
                    {
                        return 4;
                    }
                }
                else if (this.coordBaseMode == EnumFacing.EAST)
                {
                    if (meta == 3)
                    {
                        return 2;
                    }

                    if (meta == 4)
                    {
                        return 1;
                    }

                    if (meta == 2)
                    {
                        return 3;
                    }

                    if (meta == 1)
                    {
                        return 4;
                    }
                }
            }
            else if (blockIn != Blocks.tripwire_hook && !(blockIn instanceof BlockDirectional))
            {
                if (blockIn == Blocks.piston || blockIn == Blocks.sticky_piston || blockIn == Blocks.lever || blockIn == Blocks.dispenser)
                {
                    if (this.coordBaseMode == EnumFacing.SOUTH)
                    {
                        if (meta == EnumFacing.NORTH.getIndex() || meta == EnumFacing.SOUTH.getIndex())
                        {
                            return EnumFacing.getFront(meta).getOpposite().getIndex();
                        }
                    }
                    else if (this.coordBaseMode == EnumFacing.WEST)
                    {
                        if (meta == EnumFacing.NORTH.getIndex())
                        {
                            return EnumFacing.WEST.getIndex();
                        }

                        if (meta == EnumFacing.SOUTH.getIndex())
                        {
                            return EnumFacing.EAST.getIndex();
                        }

                        if (meta == EnumFacing.WEST.getIndex())
                        {
                            return EnumFacing.NORTH.getIndex();
                        }

                        if (meta == EnumFacing.EAST.getIndex())
                        {
                            return EnumFacing.SOUTH.getIndex();
                        }
                    }
                    else if (this.coordBaseMode == EnumFacing.EAST)
                    {
                        if (meta == EnumFacing.NORTH.getIndex())
                        {
                            return EnumFacing.EAST.getIndex();
                        }

                        if (meta == EnumFacing.SOUTH.getIndex())
                        {
                            return EnumFacing.WEST.getIndex();
                        }

                        if (meta == EnumFacing.WEST.getIndex())
                        {
                            return EnumFacing.NORTH.getIndex();
                        }

                        if (meta == EnumFacing.EAST.getIndex())
                        {
                            return EnumFacing.SOUTH.getIndex();
                        }
                    }
                }
            }
            else
            {
                EnumFacing horizontalFacing = EnumFacing.getHorizontal(meta);

                if (this.coordBaseMode == EnumFacing.SOUTH)
                {
                    if (horizontalFacing == EnumFacing.SOUTH || horizontalFacing == EnumFacing.NORTH)
                    {
                        return horizontalFacing.getOpposite().getHorizontalIndex();
                    }
                }
                else if (this.coordBaseMode == EnumFacing.WEST)
                {
                    if (horizontalFacing == EnumFacing.NORTH)
                    {
                        return EnumFacing.WEST.getHorizontalIndex();
                    }

                    if (horizontalFacing == EnumFacing.SOUTH)
                    {
                        return EnumFacing.EAST.getHorizontalIndex();
                    }

                    if (horizontalFacing == EnumFacing.WEST)
                    {
                        return EnumFacing.NORTH.getHorizontalIndex();
                    }

                    if (horizontalFacing == EnumFacing.EAST)
                    {
                        return EnumFacing.SOUTH.getHorizontalIndex();
                    }
                }
                else if (this.coordBaseMode == EnumFacing.EAST)
                {
                    if (horizontalFacing == EnumFacing.NORTH)
                    {
                        return EnumFacing.EAST.getHorizontalIndex();
                    }

                    if (horizontalFacing == EnumFacing.SOUTH)
                    {
                        return EnumFacing.WEST.getHorizontalIndex();
                    }

                    if (horizontalFacing == EnumFacing.WEST)
                    {
                        return EnumFacing.NORTH.getHorizontalIndex();
                    }

                    if (horizontalFacing == EnumFacing.EAST)
                    {
                        return EnumFacing.SOUTH.getHorizontalIndex();
                    }
                }
            }
        }
        else if (this.coordBaseMode == EnumFacing.SOUTH)
        {
            if (meta == 2)
            {
                return 3;
            }

            if (meta == 3)
            {
                return 2;
            }
        }
        else if (this.coordBaseMode == EnumFacing.WEST)
        {
            if (meta == 0)
            {
                return 2;
            }

            if (meta == 1)
            {
                return 3;
            }

            if (meta == 2)
            {
                return 0;
            }

            if (meta == 3)
            {
                return 1;
            }
        }
        else if (this.coordBaseMode == EnumFacing.EAST)
        {
            if (meta == 0)
            {
                return 2;
            }

            if (meta == 1)
            {
                return 3;
            }

            if (meta == 2)
            {
                return 1;
            }

            if (meta == 3)
            {
                return 0;
            }
        }

        return meta;
    }

    protected void setBlockState(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn)
    {
        BlockPos blockPos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (boundingboxIn.isVecInside(blockPos))
        {
            worldIn.setBlockState(blockPos, blockstateIn, 2);
        }
    }

    protected IBlockState getBlockStateFromPos(World worldIn, int x, int y, int z, StructureBoundingBox boundingboxIn)
    {
        int worldX = this.getXWithOffset(x, z);
        int worldY = this.getYWithOffset(y);
        int worldZ = this.getZWithOffset(x, z);
        BlockPos blockPos = new BlockPos(worldX, worldY, worldZ);
        return !boundingboxIn.isVecInside(blockPos) ? Blocks.air.getDefaultState() : worldIn.getBlockState(blockPos);
    }

    private IBlockState getBlockStateFromPos(World worldIn, int x, int y, int z, StructureBoundingBox boundingboxIn, BlockPos.MutableBlockPos blockPos)
    {
        blockPos.set(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
        return !boundingboxIn.isVecInside(blockPos) ? Blocks.air.getDefaultState() : worldIn.getBlockState(blockPos);
    }

    protected void fillWithAir(World worldIn, StructureBoundingBox structurebb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        for (int localY = minY; localY <= maxY; ++localY)
        {
            for (int localX = minX; localX <= maxX; ++localX)
            {
                for (int localZ = minZ; localZ <= maxZ; ++localZ)
                {
                    this.setBlockState(worldIn, Blocks.air.getDefaultState(), localX, localY, localZ, structurebb);
                }
            }
        }
    }

    protected void fillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, IBlockState boundaryBlockState, IBlockState insideBlockState, boolean existingOnly)
    {
        BlockPos.MutableBlockPos mutablePos = existingOnly ? new BlockPos.MutableBlockPos() : null;

        for (int localY = yMin; localY <= yMax; ++localY)
        {
            for (int localX = xMin; localX <= xMax; ++localX)
            {
                for (int localZ = zMin; localZ <= zMax; ++localZ)
                {
                    if (!existingOnly || this.getBlockStateFromPos(worldIn, localX, localY, localZ, boundingboxIn, mutablePos).getBlock().getMaterial() != Material.air)
                    {
                        if (localY != yMin && localY != yMax && localX != xMin && localX != xMax && localZ != zMin && localZ != zMax)
                        {
                            this.setBlockState(worldIn, insideBlockState, localX, localY, localZ, boundingboxIn);
                        }
                        else
                        {
                            this.setBlockState(worldIn, boundaryBlockState, localX, localY, localZ, boundingboxIn);
                        }
                    }
                }
            }
        }
    }

    protected void fillWithRandomizedBlocks(World worldIn, StructureBoundingBox boundingboxIn, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean alwaysReplace, Random rand, StructureComponent.BlockSelector blockselector)
    {
        BlockPos.MutableBlockPos mutablePos = alwaysReplace ? new BlockPos.MutableBlockPos() : null;

        for (int localY = minY; localY <= maxY; ++localY)
        {
            for (int localX = minX; localX <= maxX; ++localX)
            {
                for (int localZ = minZ; localZ <= maxZ; ++localZ)
                {
                    if (!alwaysReplace || this.getBlockStateFromPos(worldIn, localX, localY, localZ, boundingboxIn, mutablePos).getBlock().getMaterial() != Material.air)
                    {
                        blockselector.selectBlocks(rand, localX, localY, localZ, localY == minY || localY == maxY || localX == minX || localX == maxX || localZ == minZ || localZ == maxZ);
                        this.setBlockState(worldIn, blockselector.getBlockState(), localX, localY, localZ, boundingboxIn);
                    }
                }
            }
        }
    }

    protected void randomlyFillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, Random rand, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState boundaryBlockState, IBlockState insideBlockState, boolean requireNonAir)
    {
        BlockPos.MutableBlockPos mutablePos = requireNonAir ? new BlockPos.MutableBlockPos() : null;

        for (int localY = minY; localY <= maxY; ++localY)
        {
            for (int localX = minX; localX <= maxX; ++localX)
            {
                for (int localZ = minZ; localZ <= maxZ; ++localZ)
                {
                    if (rand.nextFloat() <= chance && (!requireNonAir || this.getBlockStateFromPos(worldIn, localX, localY, localZ, boundingboxIn, mutablePos).getBlock().getMaterial() != Material.air))
                    {
                        if (localY != minY && localY != maxY && localX != minX && localX != maxX && localZ != minZ && localZ != maxZ)
                        {
                            this.setBlockState(worldIn, insideBlockState, localX, localY, localZ, boundingboxIn);
                        }
                        else
                        {
                            this.setBlockState(worldIn, boundaryBlockState, localX, localY, localZ, boundingboxIn);
                        }
                    }
                }
            }
        }
    }

    protected void randomlyPlaceBlock(World worldIn, StructureBoundingBox boundingboxIn, Random rand, float chance, int x, int y, int z, IBlockState blockstateIn)
    {
        if (rand.nextFloat() < chance)
        {
            this.setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn);
        }
    }

    protected void randomlyRareFillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState blockstateIn, boolean requireNonAir)
    {
        BlockPos.MutableBlockPos mutablePos = requireNonAir ? new BlockPos.MutableBlockPos() : null;
        float sizeX = (float)(maxX - minX + 1);
        float sizeY = (float)(maxY - minY + 1);
        float sizeZ = (float)(maxZ - minZ + 1);
        float centerX = (float)minX + sizeX / 2.0F;
        float centerZ = (float)minZ + sizeZ / 2.0F;

        for (int localY = minY; localY <= maxY; ++localY)
        {
            float normalizedY = (float)(localY - minY) / sizeY;

            for (int localX = minX; localX <= maxX; ++localX)
            {
                float normalizedX = ((float)localX - centerX) / (sizeX * 0.5F);

                for (int localZ = minZ; localZ <= maxZ; ++localZ)
                {
                    float normalizedZ = ((float)localZ - centerZ) / (sizeZ * 0.5F);

                    if (!requireNonAir || this.getBlockStateFromPos(worldIn, localX, localY, localZ, boundingboxIn, mutablePos).getBlock().getMaterial() != Material.air)
                    {
                        float distanceSq = normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ;

                        if (distanceSq <= 1.05F)
                        {
                            this.setBlockState(worldIn, blockstateIn, localX, localY, localZ, boundingboxIn);
                        }
                    }
                }
            }
        }
    }

    protected void clearCurrentPositionBlocksUpwards(World worldIn, int x, int y, int z, StructureBoundingBox structurebb)
    {
        int worldX = this.getXWithOffset(x, z);
        int worldY = this.getYWithOffset(y);
        int worldZ = this.getZWithOffset(x, z);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(worldX, worldY, worldZ);

        if (structurebb.isVecInside(blockPos))
        {
            while (!worldIn.isAirBlock(blockPos) && worldY < 255)
            {
                worldIn.setBlockState(blockPos, Blocks.air.getDefaultState(), 2);
                blockPos.set(worldX, ++worldY, worldZ);
            }
        }
    }

    protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn)
    {
        int worldX = this.getXWithOffset(x, z);
        int worldY = this.getYWithOffset(y);
        int worldZ = this.getZWithOffset(x, z);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(worldX, worldY, worldZ);

        if (boundingboxIn.isVecInside(blockPos))
        {
            while ((worldIn.isAirBlock(blockPos) || worldIn.getBlockState(blockPos).getBlock().getMaterial().isLiquid()) && worldY > 1)
            {
                worldIn.setBlockState(blockPos, blockstateIn, 2);
                --worldY;
                blockPos.set(worldX, worldY, worldZ);
            }
        }
    }

    protected boolean generateChestContents(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, List<WeightedRandomChestContent> listIn, int max)
    {
        BlockPos blockPos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (boundingBoxIn.isVecInside(blockPos) && worldIn.getBlockState(blockPos).getBlock() != Blocks.chest)
        {
            IBlockState chestState = Blocks.chest.getDefaultState();
            worldIn.setBlockState(blockPos, Blocks.chest.correctFacing(worldIn, blockPos, chestState), 2);
            TileEntity tileEntity = worldIn.getTileEntity(blockPos);

            if (tileEntity instanceof TileEntityChest)
            {
                WeightedRandomChestContent.generateChestContents(rand, listIn, (TileEntityChest)tileEntity, max);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean generateDispenserContents(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, int meta, List<WeightedRandomChestContent> listIn, int max)
    {
        BlockPos blockPos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (boundingBoxIn.isVecInside(blockPos) && worldIn.getBlockState(blockPos).getBlock() != Blocks.dispenser)
        {
            worldIn.setBlockState(blockPos, Blocks.dispenser.getStateFromMeta(this.getMetadataWithOffset(Blocks.dispenser, meta)), 2);
            TileEntity tileEntity = worldIn.getTileEntity(blockPos);

            if (tileEntity instanceof TileEntityDispenser)
            {
                WeightedRandomChestContent.generateDispenserContents(rand, listIn, (TileEntityDispenser)tileEntity, max);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    protected void placeDoorCurrentPosition(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, EnumFacing facing)
    {
        BlockPos blockPos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (boundingBoxIn.isVecInside(blockPos))
        {
            ItemDoor.placeDoor(worldIn, blockPos, facing.rotateYCCW(), Blocks.oak_door);
        }
    }

    public void offset(int xOffset, int yOffset, int zOffset)
    {
        this.boundingBox.offset(xOffset, yOffset, zOffset);
    }

    public abstract static class BlockSelector
    {
        protected IBlockState blockstate = Blocks.air.getDefaultState();

        public abstract void selectBlocks(Random rand, int x, int y, int z, boolean isBoundary);

        public IBlockState getBlockState()
        {
            return this.blockstate;
        }
    }
}
