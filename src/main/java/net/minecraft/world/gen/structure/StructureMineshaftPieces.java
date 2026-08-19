package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

@SuppressWarnings("incomplete-switch")
public class StructureMineshaftPieces
{
    private static final List<WeightedRandomChestContent> CHEST_CONTENT_WEIGHT_LIST = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 5), new WeightedRandomChestContent(Items.redstone, 0, 4, 9, 5), new WeightedRandomChestContent(Items.dye, EnumDyeColor.BLUE.getDyeDamage(), 4, 9, 5), new WeightedRandomChestContent(Items.diamond, 0, 1, 2, 3), new WeightedRandomChestContent(Items.coal, 0, 3, 8, 10), new WeightedRandomChestContent(Items.bread, 0, 1, 3, 15), new WeightedRandomChestContent(Items.iron_pickaxe, 0, 1, 1, 1), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.rail), 0, 4, 8, 1), new WeightedRandomChestContent(Items.melon_seeds, 0, 2, 4, 10), new WeightedRandomChestContent(Items.pumpkin_seeds, 0, 2, 4, 10), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 3), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1)});

    public static void registerStructurePieces()
    {
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Corridor.class, "MSCorridor");
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Cross.class, "MSCrossing");
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Room.class, "MSRoom");
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Stairs.class, "MSStairs");
    }

    private static StructureComponent createConnectedPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
    {
        int i = random.nextInt(100);

        if (i >= 80)
        {
            StructureBoundingBox structureboundingbox = StructureMineshaftPieces.Cross.createBoundingBox(pieces, random, x, y, z, facing);

            if (structureboundingbox != null)
            {
                return new StructureMineshaftPieces.Cross(depth, random, structureboundingbox, facing);
            }
        }
        else if (i >= 70)
        {
            StructureBoundingBox structureboundingbox1 = StructureMineshaftPieces.Stairs.createBoundingBox(pieces, random, x, y, z, facing);

            if (structureboundingbox1 != null)
            {
                return new StructureMineshaftPieces.Stairs(depth, random, structureboundingbox1, facing);
            }
        }
        else
        {
            StructureBoundingBox structureboundingbox2 = StructureMineshaftPieces.Corridor.createBoundingBox(pieces, random, x, y, z, facing);

            if (structureboundingbox2 != null)
            {
                return new StructureMineshaftPieces.Corridor(depth, random, structureboundingbox2, facing);
            }
        }

        return null;
    }

    private static StructureComponent addPiece(StructureComponent parent, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
    {
        if (depth > 8)
        {
            return null;
        }
        else if (Math.abs(x - parent.getBoundingBox().minX) <= 80 && Math.abs(z - parent.getBoundingBox().minZ) <= 80)
        {
            StructureComponent structurecomponent = createConnectedPiece(pieces, random, x, y, z, facing, depth + 1);

            if (structurecomponent != null)
            {
                pieces.add(structurecomponent);
                structurecomponent.buildComponent(parent, pieces, random);
            }

            return structurecomponent;
        }
        else
        {
            return null;
        }
    }

    public static class Corridor extends StructureComponent
    {
        private boolean hasRails;
        private boolean hasSpiders;
        private boolean spawnerPlaced;
        private int sectionCount;

        public Corridor()
        {
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            tagCompound.setBoolean("hr", this.hasRails);
            tagCompound.setBoolean("sc", this.hasSpiders);
            tagCompound.setBoolean("hps", this.spawnerPlaced);
            tagCompound.setInteger("Num", this.sectionCount);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            this.hasRails = tagCompound.getBoolean("hr");
            this.hasSpiders = tagCompound.getBoolean("sc");
            this.spawnerPlaced = tagCompound.getBoolean("hps");
            this.sectionCount = tagCompound.getInteger("Num");
        }

        public Corridor(int type, Random rand, StructureBoundingBox structurebb, EnumFacing facing)
        {
            super(type);
            this.coordBaseMode = facing;
            this.boundingBox = structurebb;
            this.hasRails = rand.nextInt(3) == 0;
            this.hasSpiders = !this.hasRails && rand.nextInt(23) == 0;

            if (this.coordBaseMode != EnumFacing.NORTH && this.coordBaseMode != EnumFacing.SOUTH)
            {
                this.sectionCount = structurebb.getXSize() / 5;
            }
            else
            {
                this.sectionCount = structurebb.getZSize() / 5;
            }
        }

        public static StructureBoundingBox createBoundingBox(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing)
        {
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(x, y, z, x, y + 2, z);
            int i;

            for (i = rand.nextInt(3) + 2; i > 0; --i)
            {
                int j = i * 5;

                switch (facing)
                {
                    case NORTH:
                        structureboundingbox.maxX = x + 2;
                        structureboundingbox.minZ = z - (j - 1);
                        break;

                    case SOUTH:
                        structureboundingbox.maxX = x + 2;
                        structureboundingbox.maxZ = z + (j - 1);
                        break;

                    case WEST:
                        structureboundingbox.minX = x - (j - 1);
                        structureboundingbox.maxZ = z + 2;
                        break;

                    case EAST:
                        structureboundingbox.maxX = x + (j - 1);
                        structureboundingbox.maxZ = z + 2;
                }

                if (StructureComponent.findIntersecting(pieces, structureboundingbox) == null)
                {
                    break;
                }
            }

            return i > 0 ? structureboundingbox : null;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            int componentDepth = this.getComponentType();
            int branchChoice = rand.nextInt(4);

            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        if (branchChoice <= 1)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, this.coordBaseMode, componentDepth);
                        }
                        else if (branchChoice == 2)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, EnumFacing.WEST, componentDepth);
                        }
                        else
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, EnumFacing.EAST, componentDepth);
                        }

                        break;

                    case SOUTH:
                        if (branchChoice <= 1)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, this.coordBaseMode, componentDepth);
                        }
                        else if (branchChoice == 2)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ - 3, EnumFacing.WEST, componentDepth);
                        }
                        else
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ - 3, EnumFacing.EAST, componentDepth);
                        }

                        break;

                    case WEST:
                        if (branchChoice <= 1)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, this.coordBaseMode, componentDepth);
                        }
                        else if (branchChoice == 2)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, EnumFacing.NORTH, componentDepth);
                        }
                        else
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, EnumFacing.SOUTH, componentDepth);
                        }

                        break;

                    case EAST:
                        if (branchChoice <= 1)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, this.coordBaseMode, componentDepth);
                        }
                        else if (branchChoice == 2)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, EnumFacing.NORTH, componentDepth);
                        }
                        else
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, EnumFacing.SOUTH, componentDepth);
                        }
                }
            }

            if (componentDepth < 8)
            {
                if (this.coordBaseMode != EnumFacing.NORTH && this.coordBaseMode != EnumFacing.SOUTH)
                {
                    for (int branchX = this.boundingBox.minX + 3; branchX + 3 <= this.boundingBox.maxX; branchX += 5)
                    {
                        int sideBranchChoice = rand.nextInt(5);

                        if (sideBranchChoice == 0)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, branchX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, componentDepth + 1);
                        }
                        else if (sideBranchChoice == 1)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, branchX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, componentDepth + 1);
                        }
                    }
                }
                else
                {
                    for (int branchZ = this.boundingBox.minZ + 3; branchZ + 3 <= this.boundingBox.maxZ; branchZ += 5)
                    {
                        int sideBranchChoice = rand.nextInt(5);

                        if (sideBranchChoice == 0)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, branchZ, EnumFacing.WEST, componentDepth + 1);
                        }
                        else if (sideBranchChoice == 1)
                        {
                            StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, branchZ, EnumFacing.EAST, componentDepth + 1);
                        }
                    }
                }
            }
        }

        protected boolean generateChestContents(World worldIn, StructureBoundingBox boundingBoxIn, Random rand, int x, int y, int z, List<WeightedRandomChestContent> listIn, int max)
        {
            BlockPos blockPos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

            if (boundingBoxIn.isVecInside(blockPos) && worldIn.getBlockState(blockPos).getBlock().getMaterial() == Material.air)
            {
                int railMeta = rand.nextBoolean() ? 1 : 0;
                worldIn.setBlockState(blockPos, Blocks.rail.getStateFromMeta(this.getMetadataWithOffset(Blocks.rail, railMeta)), 2);
                EntityMinecartChest entityMinecartChest = new EntityMinecartChest(worldIn, (double)((float)blockPos.getX() + 0.5F), (double)((float)blockPos.getY() + 0.5F), (double)((float)blockPos.getZ() + 0.5F));
                WeightedRandomChestContent.generateChestContents(rand, listIn, entityMinecartChest, max);
                worldIn.spawnEntityInWorld(entityMinecartChest);
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                int corridorEndZ = this.sectionCount * 5 - 1;
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 2, 1, corridorEndZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.randomlyFillWithBlocks(worldIn, structureBoundingBoxIn, randomIn, 0.8F, 0, 2, 0, 2, 2, corridorEndZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

                if (this.hasSpiders)
                {
                    this.randomlyFillWithBlocks(worldIn, structureBoundingBoxIn, randomIn, 0.6F, 0, 0, 0, 2, 1, corridorEndZ, Blocks.web.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                for (int sectionIndex = 0; sectionIndex < this.sectionCount; ++sectionIndex)
                {
                    int supportZ = 2 + sectionIndex * 5;
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, supportZ, 0, 1, supportZ, Blocks.oak_fence.getDefaultState(), Blocks.air.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, supportZ, 2, 1, supportZ, Blocks.oak_fence.getDefaultState(), Blocks.air.getDefaultState(), false);

                    if (randomIn.nextInt(4) == 0)
                    {
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, supportZ, 0, 2, supportZ, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, supportZ, 2, 2, supportZ, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);
                    }
                    else
                    {
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, supportZ, 2, 2, supportZ, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);
                    }

                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 0, 2, supportZ - 1, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 2, 2, supportZ - 1, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 0, 2, supportZ + 1, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 2, 2, supportZ + 1, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.05F, 0, 2, supportZ - 2, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.05F, 2, 2, supportZ - 2, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.05F, 0, 2, supportZ + 2, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.05F, 2, 2, supportZ + 2, Blocks.web.getDefaultState());
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.05F, 1, 2, supportZ - 1, Blocks.torch.getStateFromMeta(EnumFacing.UP.getIndex()));
                    this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.05F, 1, 2, supportZ + 1, Blocks.torch.getStateFromMeta(EnumFacing.UP.getIndex()));

                    if (randomIn.nextInt(100) == 0)
                    {
                        this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 2, 0, supportZ - 1, WeightedRandomChestContent.addRandomChestContents(StructureMineshaftPieces.CHEST_CONTENT_WEIGHT_LIST, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 3 + randomIn.nextInt(4));
                    }

                    if (randomIn.nextInt(100) == 0)
                    {
                        this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 0, 0, supportZ + 1, WeightedRandomChestContent.addRandomChestContents(StructureMineshaftPieces.CHEST_CONTENT_WEIGHT_LIST, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 3 + randomIn.nextInt(4));
                    }

                    if (this.hasSpiders && !this.spawnerPlaced)
                    {
                        int spawnerY = this.getYWithOffset(0);
                        int spawnerLocalZ = supportZ - 1 + randomIn.nextInt(3);
                        int spawnerX = this.getXWithOffset(1, spawnerLocalZ);
                        int spawnerZ = this.getZWithOffset(1, spawnerLocalZ);
                        BlockPos spawnerPos = new BlockPos(spawnerX, spawnerY, spawnerZ);

                        if (structureBoundingBoxIn.isVecInside(spawnerPos))
                        {
                            this.spawnerPlaced = true;
                            worldIn.setBlockState(spawnerPos, Blocks.mob_spawner.getDefaultState(), 2);
                            TileEntity tileEntity = worldIn.getTileEntity(spawnerPos);

                            if (tileEntity instanceof TileEntityMobSpawner)
                            {
                                ((TileEntityMobSpawner)tileEntity).getSpawnerBaseLogic().setEntityName("CaveSpider");
                            }
                        }
                    }
                }

                for (int localX = 0; localX <= 2; ++localX)
                {
                    for (int localZ = 0; localZ <= corridorEndZ; ++localZ)
                    {
                        int floorY = -1;
                        IBlockState floorState = this.getBlockStateFromPos(worldIn, localX, floorY, localZ, structureBoundingBoxIn);

                        if (floorState.getBlock().getMaterial() == Material.air)
                        {
                            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), localX, floorY, localZ, structureBoundingBoxIn);
                        }
                    }
                }

                if (this.hasRails)
                {
                    for (int railZ = 0; railZ <= corridorEndZ; ++railZ)
                    {
                        IBlockState supportState = this.getBlockStateFromPos(worldIn, 1, -1, railZ, structureBoundingBoxIn);

                        if (supportState.getBlock().getMaterial() != Material.air && supportState.getBlock().isFullBlock())
                        {
                            this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.7F, 1, 0, railZ, Blocks.rail.getStateFromMeta(this.getMetadataWithOffset(Blocks.rail, 0)));
                        }
                    }
                }

                return true;
            }
        }
    }

    public static class Cross extends StructureComponent
    {
        private EnumFacing corridorDirection;
        private boolean isMultipleFloors;

        public Cross()
        {
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            tagCompound.setBoolean("tf", this.isMultipleFloors);
            tagCompound.setInteger("D", this.corridorDirection.getHorizontalIndex());
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            this.isMultipleFloors = tagCompound.getBoolean("tf");
            this.corridorDirection = EnumFacing.getHorizontal(tagCompound.getInteger("D"));
        }

        public Cross(int type, Random rand, StructureBoundingBox structurebb, EnumFacing facing)
        {
            super(type);
            this.corridorDirection = facing;
            this.boundingBox = structurebb;
            this.isMultipleFloors = structurebb.getYSize() > 3;
        }

        public static StructureBoundingBox createBoundingBox(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing)
        {
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(x, y, z, x, y + 2, z);

            if (rand.nextInt(4) == 0)
            {
                structureboundingbox.maxY += 4;
            }

            switch (facing)
            {
                case NORTH:
                    structureboundingbox.minX = x - 1;
                    structureboundingbox.maxX = x + 3;
                    structureboundingbox.minZ = z - 4;
                    break;

                case SOUTH:
                    structureboundingbox.minX = x - 1;
                    structureboundingbox.maxX = x + 3;
                    structureboundingbox.maxZ = z + 4;
                    break;

                case WEST:
                    structureboundingbox.minX = x - 4;
                    structureboundingbox.minZ = z - 1;
                    structureboundingbox.maxZ = z + 3;
                    break;

                case EAST:
                    structureboundingbox.maxX = x + 4;
                    structureboundingbox.minZ = z - 1;
                    structureboundingbox.maxZ = z + 3;
            }

            return StructureComponent.findIntersecting(pieces, structureboundingbox) != null ? null : structureboundingbox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            int i = this.getComponentType();

            switch (this.corridorDirection)
            {
                case NORTH:
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
                    break;

                case SOUTH:
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
                    break;

                case WEST:
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                    break;

                case EAST:
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
            }

            if (this.isMultipleFloors)
            {
                if (rand.nextBoolean())
                {
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                }

                if (rand.nextBoolean())
                {
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                }

                if (rand.nextBoolean())
                {
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
                }

                if (rand.nextBoolean())
                {
                    StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                }
            }
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                if (this.isMultipleFloors)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ - 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.maxY - 2, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.maxY - 2, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY + 3, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.minY + 3, this.boundingBox.maxZ - 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.minX + 1, this.boundingBox.maxY, this.boundingBox.minZ + 1, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.minX + 1, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.minZ + 1, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.planks.getDefaultState(), Blocks.air.getDefaultState(), false);

                for (int i = this.boundingBox.minX; i <= this.boundingBox.maxX; ++i)
                {
                    for (int j = this.boundingBox.minZ; j <= this.boundingBox.maxZ; ++j)
                    {
                        if (this.getBlockStateFromPos(worldIn, i, this.boundingBox.minY - 1, j, structureBoundingBoxIn).getBlock().getMaterial() == Material.air)
                        {
                            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), i, this.boundingBox.minY - 1, j, structureBoundingBoxIn);
                        }
                    }
                }

                return true;
            }
        }
    }

    public static class Room extends StructureComponent
    {
        private List<StructureBoundingBox> roomsLinkedToTheRoom = Lists.<StructureBoundingBox>newLinkedList();

        public Room()
        {
        }

        public Room(int type, Random rand, int x, int z)
        {
            super(type);
            this.boundingBox = new StructureBoundingBox(x, 50, z, x + 7 + rand.nextInt(6), 54 + rand.nextInt(6), z + 7 + rand.nextInt(6));
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            int i = this.getComponentType();
            int j = this.boundingBox.getYSize() - 3 - 1;

            if (j <= 0)
            {
                j = 1;
            }

            int k = 0;

            for (k = 0; k < this.boundingBox.getXSize(); k = k + 4)
            {
                k = k + rand.nextInt(this.boundingBox.getXSize());

                if (k + 3 > this.boundingBox.getXSize())
                {
                    break;
                }

                StructureComponent structurecomponent = StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + k, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);

                if (structurecomponent != null)
                {
                    StructureBoundingBox structureboundingbox = structurecomponent.getBoundingBox();
                    this.roomsLinkedToTheRoom.add(new StructureBoundingBox(structureboundingbox.minX, structureboundingbox.minY, this.boundingBox.minZ, structureboundingbox.maxX, structureboundingbox.maxY, this.boundingBox.minZ + 1));
                }
            }

            for (k = 0; k < this.boundingBox.getXSize(); k = k + 4)
            {
                k = k + rand.nextInt(this.boundingBox.getXSize());

                if (k + 3 > this.boundingBox.getXSize())
                {
                    break;
                }

                StructureComponent structurecomponent1 = StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX + k, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);

                if (structurecomponent1 != null)
                {
                    StructureBoundingBox structureboundingbox1 = structurecomponent1.getBoundingBox();
                    this.roomsLinkedToTheRoom.add(new StructureBoundingBox(structureboundingbox1.minX, structureboundingbox1.minY, this.boundingBox.maxZ - 1, structureboundingbox1.maxX, structureboundingbox1.maxY, this.boundingBox.maxZ));
                }
            }

            for (k = 0; k < this.boundingBox.getZSize(); k = k + 4)
            {
                k = k + rand.nextInt(this.boundingBox.getZSize());

                if (k + 3 > this.boundingBox.getZSize())
                {
                    break;
                }

                StructureComponent structurecomponent2 = StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.minZ + k, EnumFacing.WEST, i);

                if (structurecomponent2 != null)
                {
                    StructureBoundingBox structureboundingbox2 = structurecomponent2.getBoundingBox();
                    this.roomsLinkedToTheRoom.add(new StructureBoundingBox(this.boundingBox.minX, structureboundingbox2.minY, structureboundingbox2.minZ, this.boundingBox.minX + 1, structureboundingbox2.maxY, structureboundingbox2.maxZ));
                }
            }

            for (k = 0; k < this.boundingBox.getZSize(); k = k + 4)
            {
                k = k + rand.nextInt(this.boundingBox.getZSize());

                if (k + 3 > this.boundingBox.getZSize())
                {
                    break;
                }

                StructureComponent structurecomponent3 = StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.minZ + k, EnumFacing.EAST, i);

                if (structurecomponent3 != null)
                {
                    StructureBoundingBox structureboundingbox3 = structurecomponent3.getBoundingBox();
                    this.roomsLinkedToTheRoom.add(new StructureBoundingBox(this.boundingBox.maxX - 1, structureboundingbox3.minY, structureboundingbox3.minZ, this.boundingBox.maxX, structureboundingbox3.maxY, structureboundingbox3.maxZ));
                }
            }
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ, Blocks.dirt.getDefaultState(), Blocks.air.getDefaultState(), true);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY + 1, this.boundingBox.minZ, this.boundingBox.maxX, Math.min(this.boundingBox.minY + 3, this.boundingBox.maxY), this.boundingBox.maxZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

                for (StructureBoundingBox structureBoundingBox : this.roomsLinkedToTheRoom)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, structureBoundingBox.minX, structureBoundingBox.maxY - 2, structureBoundingBox.minZ, structureBoundingBox.maxX, structureBoundingBox.maxY, structureBoundingBox.maxZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                this.randomlyRareFillWithBlocks(worldIn, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY + 4, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.air.getDefaultState(), false);
                return true;
            }
        }

        public void offset(int xOffset, int yOffset, int zOffset)
        {
            super.offset(xOffset, yOffset, zOffset);

            for (StructureBoundingBox structureBoundingBox : this.roomsLinkedToTheRoom)
            {
                structureBoundingBox.offset(xOffset, yOffset, zOffset);
            }
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            NBTTagList nBTTagList = new NBTTagList();

            for (StructureBoundingBox structureBoundingBox : this.roomsLinkedToTheRoom)
            {
                nBTTagList.appendTag(structureBoundingBox.toNBTTagIntArray());
            }

            tagCompound.setTag("Entrances", nBTTagList);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            NBTTagList nBTTagList = tagCompound.getTagList("Entrances", 11);

            for (int i = 0; i < nBTTagList.tagCount(); ++i)
            {
                this.roomsLinkedToTheRoom.add(new StructureBoundingBox(nBTTagList.getIntArrayAt(i)));
            }
        }
    }

    public static class Stairs extends StructureComponent
    {
        public Stairs()
        {
        }

        public Stairs(int type, Random rand, StructureBoundingBox structurebb, EnumFacing facing)
        {
            super(type);
            this.coordBaseMode = facing;
            this.boundingBox = structurebb;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
        }

        public static StructureBoundingBox createBoundingBox(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing)
        {
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(x, y - 5, z, x, y + 2, z);

            switch (facing)
            {
                case NORTH:
                    structureboundingbox.maxX = x + 2;
                    structureboundingbox.minZ = z - 8;
                    break;

                case SOUTH:
                    structureboundingbox.maxX = x + 2;
                    structureboundingbox.maxZ = z + 8;
                    break;

                case WEST:
                    structureboundingbox.minX = x - 8;
                    structureboundingbox.maxZ = z + 2;
                    break;

                case EAST:
                    structureboundingbox.maxX = x + 8;
                    structureboundingbox.maxZ = z + 2;
            }

            return StructureComponent.findIntersecting(pieces, structureboundingbox) != null ? null : structureboundingbox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            int i = this.getComponentType();

            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                        break;

                    case SOUTH:
                        StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                        break;

                    case WEST:
                        StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, i);
                        break;

                    case EAST:
                        StructureMineshaftPieces.addPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, i);
                }
            }
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 2, 7, 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 7, 2, 2, 8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

                for (int i = 0; i < 5; ++i)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                return true;
            }
        }
    }
}
