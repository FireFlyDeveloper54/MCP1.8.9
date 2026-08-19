package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

@SuppressWarnings("incomplete-switch")
public class StructureStrongholdPieces
{
    private static final StructureStrongholdPieces.PieceWeight[] pieceWeightArray = new StructureStrongholdPieces.PieceWeight[] {new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Straight.class, 40, 0), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Prison.class, 5, 5), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.LeftTurn.class, 20, 0), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.RightTurn.class, 20, 0), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.RoomCrossing.class, 10, 6), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.StairsStraight.class, 5, 5), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Stairs.class, 5, 5), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Crossing.class, 5, 4), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.ChestCorridor.class, 5, 4), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Library.class, 10, 2)
        {
            public boolean canSpawnMoreStructuresOfType(int depth)
            {
                return super.canSpawnMoreStructuresOfType(depth) && depth > 4;
            }
        }, new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.PortalRoom.class, 20, 1)
        {
            public boolean canSpawnMoreStructuresOfType(int depth)
            {
                return super.canSpawnMoreStructuresOfType(depth) && depth > 5;
            }
        }
    };
    private static List<StructureStrongholdPieces.PieceWeight> structurePieceList;
    private static Class <? extends StructureStrongholdPieces.Stronghold > strongComponentType;
    static int totalWeight;
    private static final StructureStrongholdPieces.Stones strongholdStones = new StructureStrongholdPieces.Stones();

    public static void registerStrongholdPieces()
    {
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.ChestCorridor.class, "SHCC");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Corridor.class, "SHFC");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Crossing.class, "SH5C");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.LeftTurn.class, "SHLT");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Library.class, "SHLi");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.PortalRoom.class, "SHPR");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Prison.class, "SHPH");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.RightTurn.class, "SHRT");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.RoomCrossing.class, "SHRC");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Stairs.class, "SHSD");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Stairs2.class, "SHStart");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Straight.class, "SHS");
        MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.StairsStraight.class, "SHSSD");
    }

    public static void prepareStructurePieces()
    {
        structurePieceList = Lists.<StructureStrongholdPieces.PieceWeight>newArrayList();

        for (StructureStrongholdPieces.PieceWeight pieceWeight : pieceWeightArray)
        {
            pieceWeight.instancesSpawned = 0;
            structurePieceList.add(pieceWeight);
        }

        strongComponentType = null;
    }

    private static boolean canAddStructurePieces()
    {
        boolean hasAvailableLimitedPiece = false;
        totalWeight = 0;

        for (StructureStrongholdPieces.PieceWeight pieceWeight : structurePieceList)
        {
            if (pieceWeight.instancesLimit > 0 && pieceWeight.instancesSpawned < pieceWeight.instancesLimit)
            {
                hasAvailableLimitedPiece = true;
            }

            totalWeight += pieceWeight.pieceWeight;
        }

        return hasAvailableLimitedPiece;
    }

    private static StructureStrongholdPieces.Stronghold createPiece(Class <? extends StructureStrongholdPieces.Stronghold > pieceClass, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        StructureStrongholdPieces.Stronghold generatedPiece = null;

        if (pieceClass == StructureStrongholdPieces.Straight.class)
        {
            generatedPiece = StructureStrongholdPieces.Straight.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.Prison.class)
        {
            generatedPiece = StructureStrongholdPieces.Prison.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.LeftTurn.class)
        {
            generatedPiece = StructureStrongholdPieces.LeftTurn.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.RightTurn.class)
        {
            generatedPiece = StructureStrongholdPieces.RightTurn.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.RoomCrossing.class)
        {
            generatedPiece = StructureStrongholdPieces.RoomCrossing.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.StairsStraight.class)
        {
            generatedPiece = StructureStrongholdPieces.StairsStraight.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.Stairs.class)
        {
            generatedPiece = StructureStrongholdPieces.Stairs.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.Crossing.class)
        {
            generatedPiece = StructureStrongholdPieces.Crossing.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.ChestCorridor.class)
        {
            generatedPiece = StructureStrongholdPieces.ChestCorridor.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.Library.class)
        {
            generatedPiece = StructureStrongholdPieces.Library.createPiece(pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureStrongholdPieces.PortalRoom.class)
        {
            generatedPiece = StructureStrongholdPieces.PortalRoom.createPiece(pieces, rand, x, y, z, facing, depth);
        }

        return generatedPiece;
    }

    private static StructureStrongholdPieces.Stronghold createNextPiece(StructureStrongholdPieces.Stairs2 start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        if (!canAddStructurePieces())
        {
            return null;
        }
        else
        {
            if (strongComponentType != null)
            {
                StructureStrongholdPieces.Stronghold forcedPiece = createPiece(strongComponentType, pieces, rand, x, y, z, facing, depth);
                strongComponentType = null;

                if (forcedPiece != null)
                {
                    return forcedPiece;
                }
            }

            int attempts = 0;

            while (attempts < 5)
            {
                ++attempts;
                int weightChoice = rand.nextInt(totalWeight);

                for (StructureStrongholdPieces.PieceWeight pieceWeight : structurePieceList)
                {
                    weightChoice -= pieceWeight.pieceWeight;

                    if (weightChoice < 0)
                    {
                        if (!pieceWeight.canSpawnMoreStructuresOfType(depth) || pieceWeight == start.strongholdPieceWeight)
                        {
                            break;
                        }

                        StructureStrongholdPieces.Stronghold generatedPiece = createPiece(pieceWeight.pieceClass, pieces, rand, x, y, z, facing, depth);

                        if (generatedPiece != null)
                        {
                            ++pieceWeight.instancesSpawned;
                            start.strongholdPieceWeight = pieceWeight;

                            if (!pieceWeight.canSpawnMoreStructures())
                            {
                                structurePieceList.remove(pieceWeight);
                            }

                            return generatedPiece;
                        }
                    }
                }
            }

            StructureBoundingBox corridorBox = StructureStrongholdPieces.Corridor.createBoundingBox(pieces, rand, x, y, z, facing);

            if (corridorBox != null && corridorBox.minY > 1)
            {
                return new StructureStrongholdPieces.Corridor(depth, rand, corridorBox, facing);
            }
            else
            {
                return null;
            }
        }
    }

    private static StructureComponent createAndAddPiece(StructureStrongholdPieces.Stairs2 start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        if (depth > 50)
        {
            return null;
        }
        else if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112)
        {
            StructureComponent component = createNextPiece(start, pieces, rand, x, y, z, facing, depth + 1);

            if (component != null)
            {
                pieces.add(component);
                start.secondaryPieces.add(component);
            }

            return component;
        }
        else
        {
            return null;
        }
    }

    public static class ChestCorridor extends StructureStrongholdPieces.Stronghold
    {
        private static final List<WeightedRandomChestContent> strongholdChestContents = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.ender_pearl, 0, 1, 1, 10), new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 3), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 5), new WeightedRandomChestContent(Items.redstone, 0, 4, 9, 5), new WeightedRandomChestContent(Items.bread, 0, 1, 3, 15), new WeightedRandomChestContent(Items.apple, 0, 1, 3, 15), new WeightedRandomChestContent(Items.iron_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_sword, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_chestplate, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_helmet, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_leggings, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_boots, 0, 1, 1, 5), new WeightedRandomChestContent(Items.golden_apple, 0, 1, 1, 1), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 1), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1)});
        private boolean hasMadeChest;

        public ChestCorridor()
        {
        }

        public ChestCorridor(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Chest", this.hasMadeChest);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasMadeChest = tagCompound.getBoolean("Chest");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
        }

        public static StructureStrongholdPieces.ChestCorridor createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.ChestCorridor(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 4, 6, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 1, 0);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 6);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 2, 3, 1, 4, Blocks.stonebrick.getDefaultState(), Blocks.stonebrick.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 1, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 1, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 2, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 2, 4, structureBoundingBoxIn);

                for (int i = 2; i <= 4; ++i)
                {
                    this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 2, 1, i, structureBoundingBoxIn);
                }

                if (!this.hasMadeChest && structureBoundingBoxIn.isVecInside(new BlockPos(this.getXWithOffset(3, 3), this.getYWithOffset(2), this.getZWithOffset(3, 3))))
                {
                    this.hasMadeChest = true;
                    this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 3, 2, 3, WeightedRandomChestContent.addRandomChestContents(strongholdChestContents, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 2 + randomIn.nextInt(2));
                }

                return true;
            }
        }
    }

    public static class Corridor extends StructureStrongholdPieces.Stronghold
    {
        private int corridorLength;

        public Corridor()
        {
        }

        public Corridor(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.corridorLength = facing != EnumFacing.NORTH && facing != EnumFacing.SOUTH ? boundingBox.getXSize() : boundingBox.getZSize();
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("Steps", this.corridorLength);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.corridorLength = tagCompound.getInteger("Steps");
        }

        public static StructureBoundingBox createBoundingBox(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing)
        {
            int i = 3;
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 4, facing);
            StructureComponent intersectingComponent = StructureComponent.findIntersecting(pieces, boundingBox);

            if (intersectingComponent == null)
            {
                return null;
            }
            else
            {
                if (intersectingComponent.getBoundingBox().minY == boundingBox.minY)
                {
                    for (int j = 3; j >= 1; --j)
                    {
                        boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, j - 1, facing);

                        if (!intersectingComponent.getBoundingBox().intersectsWith(boundingBox))
                        {
                            return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, j, facing);
                        }
                    }
                }

                return null;
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
                for (int i = 0; i < this.corridorLength; ++i)
                {
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 0, 0, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 0, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 2, 0, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 0, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 4, 0, i, structureBoundingBoxIn);

                    for (int j = 1; j <= 3; ++j)
                    {
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 0, j, i, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, j, i, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, j, i, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.air.getDefaultState(), 3, j, i, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 4, j, i, structureBoundingBoxIn);
                    }

                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 0, 4, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 4, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 2, 4, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 4, i, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 4, 4, i, structureBoundingBoxIn);
                }

                return true;
            }
        }
    }

    public static class Crossing extends StructureStrongholdPieces.Stronghold
    {
        private boolean leftLow;
        private boolean leftHigh;
        private boolean rightLow;
        private boolean rightHigh;

        public Crossing()
        {
        }

        public Crossing(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
            this.leftLow = rand.nextBoolean();
            this.leftHigh = rand.nextBoolean();
            this.rightLow = rand.nextBoolean();
            this.rightHigh = rand.nextInt(3) > 0;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("leftLow", this.leftLow);
            tagCompound.setBoolean("leftHigh", this.leftHigh);
            tagCompound.setBoolean("rightLow", this.rightLow);
            tagCompound.setBoolean("rightHigh", this.rightHigh);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.leftLow = tagCompound.getBoolean("leftLow");
            this.leftHigh = tagCompound.getBoolean("leftHigh");
            this.rightLow = tagCompound.getBoolean("rightLow");
            this.rightHigh = tagCompound.getBoolean("rightHigh");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            int i = 3;
            int j = 5;

            if (this.coordBaseMode == EnumFacing.WEST || this.coordBaseMode == EnumFacing.NORTH)
            {
                i = 8 - i;
                j = 8 - j;
            }

            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 5, 1);

            if (this.leftLow)
            {
                this.getNextComponentX((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, i, 1);
            }

            if (this.leftHigh)
            {
                this.getNextComponentX((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, j, 7);
            }

            if (this.rightLow)
            {
                this.getNextComponentZ((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, i, 1);
            }

            if (this.rightHigh)
            {
                this.getNextComponentZ((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, j, 7);
            }
        }

        public static StructureStrongholdPieces.Crossing createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -3, 0, 10, 9, 11, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.Crossing(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 9, 8, 10, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 4, 3, 0);

                if (this.leftLow)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 1, 0, 5, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                if (this.rightLow)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 3, 1, 9, 5, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                if (this.leftHigh)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 7, 0, 7, 9, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                if (this.rightHigh)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 5, 7, 9, 7, 9, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 10, 7, 3, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 2, 1, 8, 2, 6, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 5, 4, 4, 9, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 8, 1, 5, 8, 4, 9, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 4, 7, 3, 4, 9, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 3, 5, 3, 3, 6, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 4, 3, 3, 4, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 6, 3, 4, 6, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 5, 1, 7, 7, 1, 8, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 9, 7, 1, 9, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 7, 7, 2, 7, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 7, 4, 5, 9, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 7, 8, 5, 9, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, 7, 7, 5, 9, Blocks.double_stone_slab.getDefaultState(), Blocks.double_stone_slab.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 6, 5, 6, structureBoundingBoxIn);
                return true;
            }
        }
    }

    public static class LeftTurn extends StructureStrongholdPieces.Stronghold
    {
        public LeftTurn()
        {
        }

        public LeftTurn(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            if (this.coordBaseMode != EnumFacing.NORTH && this.coordBaseMode != EnumFacing.EAST)
            {
                this.getNextComponentZ((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
            }
            else
            {
                this.getNextComponentX((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
            }
        }

        public static StructureStrongholdPieces.LeftTurn createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 5, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.LeftTurn(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 4, 4, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 1, 0);

                if (this.coordBaseMode != EnumFacing.NORTH && this.coordBaseMode != EnumFacing.EAST)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 3, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                return true;
            }
        }
    }

    public static class Library extends StructureStrongholdPieces.Stronghold
    {
        private static final List<WeightedRandomChestContent> strongholdLibraryChestContents = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.book, 0, 1, 3, 20), new WeightedRandomChestContent(Items.paper, 0, 2, 7, 20), new WeightedRandomChestContent(Items.map, 0, 1, 1, 1), new WeightedRandomChestContent(Items.compass, 0, 1, 1, 1)});
        private boolean isLargeRoom;

        public Library()
        {
        }

        public Library(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
            this.isLargeRoom = boundingBox.getYSize() > 6;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Tall", this.isLargeRoom);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.isLargeRoom = tagCompound.getBoolean("Tall");
        }

        public static StructureStrongholdPieces.Library createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 14, 11, 15, facing);

            if (!canStrongholdGoDeeper(boundingBox) || StructureComponent.findIntersecting(pieces, boundingBox) != null)
            {
                boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 14, 6, 15, facing);

                if (!canStrongholdGoDeeper(boundingBox) || StructureComponent.findIntersecting(pieces, boundingBox) != null)
                {
                    return null;
                }
            }

            return new StructureStrongholdPieces.Library(depth, rand, boundingBox, facing);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                int i = 11;

                if (!this.isLargeRoom)
                {
                    i = 6;
                }

                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 13, i - 1, 14, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 4, 1, 0);
                this.randomlyFillWithBlocks(worldIn, structureBoundingBoxIn, randomIn, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.web.getDefaultState(), Blocks.web.getDefaultState(), false);
                int j = 1;
                int k = 12;

                for (int l = 1; l <= 13; ++l)
                {
                    if ((l - 1) % 4 == 0)
                    {
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, l, 1, 4, l, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, l, 12, 4, l, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 2, 3, l, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 11, 3, l, structureBoundingBoxIn);

                        if (this.isLargeRoom)
                        {
                            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 6, l, 1, 9, l, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 6, l, 12, 9, l, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                        }
                    }
                    else
                    {
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, l, 1, 4, l, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, l, 12, 4, l, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);

                        if (this.isLargeRoom)
                        {
                            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 6, l, 1, 9, l, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
                            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 6, l, 12, 9, l, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
                        }
                    }
                }

                for (int shelfZ = 3; shelfZ < 12; shelfZ += 2)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, shelfZ, 4, 3, shelfZ, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, shelfZ, 7, 3, shelfZ, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, shelfZ, 10, 3, shelfZ, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
                }

                if (this.isLargeRoom)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 3, 5, 13, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 5, 1, 12, 5, 13, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 1, 9, 5, 2, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 12, 9, 5, 13, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
                    this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 9, 5, 11, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 5, 11, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 9, 5, 10, structureBoundingBoxIn);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 6, 2, 3, 6, 12, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 6, 2, 10, 6, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 6, 2, 9, 6, 2, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 6, 12, 8, 6, 12, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 9, 6, 11, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 8, 6, 11, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 9, 6, 10, structureBoundingBoxIn);
                    int ladderMeta = this.getMetadataWithOffset(Blocks.ladder, 3);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 1, 13, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 2, 13, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 3, 13, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 4, 13, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 5, 13, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 6, 13, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(ladderMeta), 10, 7, 13, structureBoundingBoxIn);
                    int chandelierX = 7;
                    int chandelierZ = 7;
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX - 1, 9, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX, 9, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX - 1, 8, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX, 8, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX - 1, 7, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX, 7, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX - 2, 7, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX + 1, 7, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX - 1, 7, chandelierZ - 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX - 1, 7, chandelierZ + 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX, 7, chandelierZ - 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), chandelierX, 7, chandelierZ + 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.torch.getDefaultState(), chandelierX - 2, 8, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.torch.getDefaultState(), chandelierX + 1, 8, chandelierZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.torch.getDefaultState(), chandelierX - 1, 8, chandelierZ - 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.torch.getDefaultState(), chandelierX - 1, 8, chandelierZ + 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.torch.getDefaultState(), chandelierX, 8, chandelierZ - 1, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.torch.getDefaultState(), chandelierX, 8, chandelierZ + 1, structureBoundingBoxIn);
                }

                this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 3, 3, 5, WeightedRandomChestContent.addRandomChestContents(strongholdLibraryChestContents, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn, 1, 5, 2)}), 1 + randomIn.nextInt(4));

                if (this.isLargeRoom)
                {
                    this.setBlockState(worldIn, Blocks.air.getDefaultState(), 12, 9, 1, structureBoundingBoxIn);
                    this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 12, 8, 1, WeightedRandomChestContent.addRandomChestContents(strongholdLibraryChestContents, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn, 1, 5, 2)}), 1 + randomIn.nextInt(4));
                }

                return true;
            }
        }
    }

    static class PieceWeight
    {
        public Class <? extends StructureStrongholdPieces.Stronghold > pieceClass;
        public final int pieceWeight;
        public int instancesSpawned;
        public int instancesLimit;

        public PieceWeight(Class <? extends StructureStrongholdPieces.Stronghold > pieceClass, int pieceWeight, int instancesLimit)
        {
            this.pieceClass = pieceClass;
            this.pieceWeight = pieceWeight;
            this.instancesLimit = instancesLimit;
        }

        public boolean canSpawnMoreStructuresOfType(int depth)
        {
            return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
        }

        public boolean canSpawnMoreStructures()
        {
            return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
        }
    }

    public static class PortalRoom extends StructureStrongholdPieces.Stronghold
    {
        private boolean hasSpawner;

        public PortalRoom()
        {
        }

        public PortalRoom(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Mob", this.hasSpawner);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasSpawner = tagCompound.getBoolean("Mob");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            if (componentIn != null)
            {
                ((StructureStrongholdPieces.Stairs2)componentIn).strongholdPortalRoom = this;
            }
        }

        public static StructureStrongholdPieces.PortalRoom createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 11, 8, 16, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.PortalRoom(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 10, 7, 15, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, StructureStrongholdPieces.Stronghold.Door.GRATES, 4, 1, 0);
            int ceilingY = 6;
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, ceilingY, 1, 1, ceilingY, 14, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 9, ceilingY, 1, 9, ceilingY, 14, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, ceilingY, 1, 8, ceilingY, 2, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, ceilingY, 14, 8, ceilingY, 14, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 2, 1, 4, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 8, 1, 1, 9, 1, 4, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 1, 1, 3, Blocks.flowing_lava.getDefaultState(), Blocks.flowing_lava.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, 1, 9, 1, 3, Blocks.flowing_lava.getDefaultState(), Blocks.flowing_lava.getDefaultState(), false);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 3, 1, 8, 7, 1, 12, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 9, 6, 1, 11, Blocks.flowing_lava.getDefaultState(), Blocks.flowing_lava.getDefaultState(), false);

            for (int j = 3; j < 14; j += 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, j, 0, 4, j, Blocks.iron_bars.getDefaultState(), Blocks.iron_bars.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 3, j, 10, 4, j, Blocks.iron_bars.getDefaultState(), Blocks.iron_bars.getDefaultState(), false);
            }

            for (int barX = 2; barX < 9; barX += 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, barX, 3, 15, barX, 4, 15, Blocks.iron_bars.getDefaultState(), Blocks.iron_bars.getDefaultState(), false);
            }

            int stairMeta = this.getMetadataWithOffset(Blocks.stone_brick_stairs, 3);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 5, 6, 1, 7, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 2, 6, 6, 2, 7, false, randomIn, StructureStrongholdPieces.strongholdStones);
            this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 3, 7, 6, 3, 7, false, randomIn, StructureStrongholdPieces.strongholdStones);

            for (int stairX = 4; stairX <= 6; ++stairX)
            {
                this.setBlockState(worldIn, Blocks.stone_brick_stairs.getStateFromMeta(stairMeta), stairX, 1, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_brick_stairs.getStateFromMeta(stairMeta), stairX, 2, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_brick_stairs.getStateFromMeta(stairMeta), stairX, 3, 6, structureBoundingBoxIn);
            }

            int northFrameMeta = EnumFacing.NORTH.getHorizontalIndex();
            int southFrameMeta = EnumFacing.SOUTH.getHorizontalIndex();
            int eastFrameMeta = EnumFacing.EAST.getHorizontalIndex();
            int westFrameMeta = EnumFacing.WEST.getHorizontalIndex();

            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case SOUTH:
                        northFrameMeta = EnumFacing.SOUTH.getHorizontalIndex();
                        southFrameMeta = EnumFacing.NORTH.getHorizontalIndex();
                        break;

                    case WEST:
                        northFrameMeta = EnumFacing.WEST.getHorizontalIndex();
                        southFrameMeta = EnumFacing.EAST.getHorizontalIndex();
                        eastFrameMeta = EnumFacing.SOUTH.getHorizontalIndex();
                        westFrameMeta = EnumFacing.NORTH.getHorizontalIndex();
                        break;

                    case EAST:
                        northFrameMeta = EnumFacing.EAST.getHorizontalIndex();
                        southFrameMeta = EnumFacing.WEST.getHorizontalIndex();
                        eastFrameMeta = EnumFacing.SOUTH.getHorizontalIndex();
                        westFrameMeta = EnumFacing.NORTH.getHorizontalIndex();
                }
            }

            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(northFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 4, 3, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(northFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 5, 3, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(northFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 6, 3, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(southFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 4, 3, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(southFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 5, 3, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(southFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 6, 3, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(eastFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 3, 3, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(eastFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 3, 3, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(eastFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 3, 3, 11, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(westFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 7, 3, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(westFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 7, 3, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.end_portal_frame.getStateFromMeta(westFrameMeta).withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(randomIn.nextFloat() > 0.9F)), 7, 3, 11, structureBoundingBoxIn);

            if (!this.hasSpawner)
            {
                int spawnerY = this.getYWithOffset(3);
                BlockPos spawnerPos = new BlockPos(this.getXWithOffset(5, 6), spawnerY, this.getZWithOffset(5, 6));

                if (structureBoundingBoxIn.isVecInside(spawnerPos))
                {
                    this.hasSpawner = true;
                    worldIn.setBlockState(spawnerPos, Blocks.mob_spawner.getDefaultState(), 2);
                    TileEntity tileEntity = worldIn.getTileEntity(spawnerPos);

                    if (tileEntity instanceof TileEntityMobSpawner)
                    {
                        ((TileEntityMobSpawner)tileEntity).getSpawnerBaseLogic().setEntityName("Silverfish");
                    }
                }
            }

            return true;
        }
    }

    public static class Prison extends StructureStrongholdPieces.Stronghold
    {
        public Prison()
        {
        }

        public Prison(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
        }

        public static StructureStrongholdPieces.Prison createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 9, 5, 11, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.Prison(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 8, 4, 10, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 1, 0);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 10, 3, 3, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 3, 1, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 3, 4, 3, 3, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 7, 4, 3, 7, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 9, 4, 3, 9, false, randomIn, StructureStrongholdPieces.strongholdStones);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 4, 4, 3, 6, Blocks.iron_bars.getDefaultState(), Blocks.iron_bars.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 5, 7, 3, 5, Blocks.iron_bars.getDefaultState(), Blocks.iron_bars.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), 4, 3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), 4, 3, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.iron_door.getStateFromMeta(this.getMetadataWithOffset(Blocks.iron_door, 3)), 4, 1, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.iron_door.getStateFromMeta(this.getMetadataWithOffset(Blocks.iron_door, 3) + 8), 4, 2, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.iron_door.getStateFromMeta(this.getMetadataWithOffset(Blocks.iron_door, 3)), 4, 1, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.iron_door.getStateFromMeta(this.getMetadataWithOffset(Blocks.iron_door, 3) + 8), 4, 2, 8, structureBoundingBoxIn);
                return true;
            }
        }
    }

    public static class RightTurn extends StructureStrongholdPieces.LeftTurn
    {
        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            if (this.coordBaseMode != EnumFacing.NORTH && this.coordBaseMode != EnumFacing.EAST)
            {
                this.getNextComponentX((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
            }
            else
            {
                this.getNextComponentZ((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
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
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 4, 4, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 1, 0);

                if (this.coordBaseMode != EnumFacing.NORTH && this.coordBaseMode != EnumFacing.EAST)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 3, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                return true;
            }
        }
    }

    public static class RoomCrossing extends StructureStrongholdPieces.Stronghold
    {
        private static final List<WeightedRandomChestContent> strongholdRoomCrossingChestContents = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 5), new WeightedRandomChestContent(Items.redstone, 0, 4, 9, 5), new WeightedRandomChestContent(Items.coal, 0, 3, 8, 10), new WeightedRandomChestContent(Items.bread, 0, 1, 3, 15), new WeightedRandomChestContent(Items.apple, 0, 1, 3, 15), new WeightedRandomChestContent(Items.iron_pickaxe, 0, 1, 1, 1)});
        protected int roomType;

        public RoomCrossing()
        {
        }

        public RoomCrossing(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
            this.roomType = rand.nextInt(5);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("Type", this.roomType);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.roomType = tagCompound.getInteger("Type");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 4, 1);
            this.getNextComponentX((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 4);
            this.getNextComponentZ((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 4);
        }

        public static StructureStrongholdPieces.RoomCrossing createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 11, 7, 11, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.RoomCrossing(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 10, 6, 10, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 4, 1, 0);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 10, 6, 3, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 4, 0, 3, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 1, 4, 10, 3, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

                switch (this.roomType)
                {
                    case 0:
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 5, 1, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 5, 2, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 5, 3, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 4, 3, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 6, 3, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 5, 3, 4, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 5, 3, 6, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 4, 1, 4, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 4, 1, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 4, 1, 6, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 6, 1, 4, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 6, 1, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 6, 1, 6, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 5, 1, 4, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stone_slab.getDefaultState(), 5, 1, 6, structureBoundingBoxIn);
                        break;

                    case 1:
                        for (int ringOffset = 0; ringOffset < 5; ++ringOffset)
                        {
                            this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 1, 3 + ringOffset, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 7, 1, 3 + ringOffset, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3 + ringOffset, 1, 3, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3 + ringOffset, 1, 7, structureBoundingBoxIn);
                        }

                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 5, 1, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 5, 2, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 5, 3, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.flowing_water.getDefaultState(), 5, 4, 5, structureBoundingBoxIn);
                        break;

                    case 2:
                        for (int i = 1; i <= 9; ++i)
                        {
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 1, 3, i, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 9, 3, i, structureBoundingBoxIn);
                        }

                        for (int j = 1; j <= 9; ++j)
                        {
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), j, 3, 1, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), j, 3, 9, structureBoundingBoxIn);
                        }

                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 5, 1, 4, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 5, 1, 6, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 5, 3, 4, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 5, 3, 6, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 1, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, 1, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 3, 5, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, 3, 5, structureBoundingBoxIn);

                        for (int k = 1; k <= 3; ++k)
                        {
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, k, 4, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, k, 4, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, k, 6, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, k, 6, structureBoundingBoxIn);
                        }

                        this.setBlockState(worldIn, Blocks.torch.getDefaultState(), 5, 3, 5, structureBoundingBoxIn);

                        for (int l = 2; l <= 8; ++l)
                        {
                            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 2, 3, l, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 3, l, structureBoundingBoxIn);

                            if (l <= 3 || l >= 7)
                            {
                                this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 4, 3, l, structureBoundingBoxIn);
                                this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 5, 3, l, structureBoundingBoxIn);
                                this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 6, 3, l, structureBoundingBoxIn);
                            }

                            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 7, 3, l, structureBoundingBoxIn);
                            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 3, l, structureBoundingBoxIn);
                        }

                        this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(this.getMetadataWithOffset(Blocks.ladder, EnumFacing.WEST.getIndex())), 9, 1, 3, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(this.getMetadataWithOffset(Blocks.ladder, EnumFacing.WEST.getIndex())), 9, 2, 3, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(this.getMetadataWithOffset(Blocks.ladder, EnumFacing.WEST.getIndex())), 9, 3, 3, structureBoundingBoxIn);
                        this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 3, 4, 8, WeightedRandomChestContent.addRandomChestContents(strongholdRoomCrossingChestContents, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 1 + randomIn.nextInt(4));
                }

                return true;
            }
        }
    }

    public static class Stairs extends StructureStrongholdPieces.Stronghold
    {
        private boolean source;

        public Stairs()
        {
        }

        public Stairs(int componentType, Random rand, int x, int z)
        {
            super(componentType);
            this.source = true;
            this.coordBaseMode = EnumFacing.Plane.HORIZONTAL.random(rand);
            this.entryDoor = StructureStrongholdPieces.Stronghold.Door.OPENING;

            switch (this.coordBaseMode)
            {
                case NORTH:
                case SOUTH:
                    this.boundingBox = new StructureBoundingBox(x, 64, z, x + 5 - 1, 74, z + 5 - 1);
                    break;

                default:
                    this.boundingBox = new StructureBoundingBox(x, 64, z, x + 5 - 1, 74, z + 5 - 1);
            }
        }

        public Stairs(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.source = false;
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Source", this.source);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.source = tagCompound.getBoolean("Source");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            if (this.source)
            {
                StructureStrongholdPieces.strongComponentType = StructureStrongholdPieces.Crossing.class;
            }

            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
        }

        public static StructureStrongholdPieces.Stairs createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -7, 0, 5, 11, 5, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.Stairs(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 10, 4, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 7, 0);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 4);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 2, 6, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 5, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 6, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 5, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 4, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 5, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 2, 4, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 3, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 3, 4, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 2, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 3, 3, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 2, 2, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 1, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 2, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 1, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 1, 3, structureBoundingBoxIn);
                return true;
            }
        }
    }

    public static class Stairs2 extends StructureStrongholdPieces.Stairs
    {
        public StructureStrongholdPieces.PieceWeight strongholdPieceWeight;
        public StructureStrongholdPieces.PortalRoom strongholdPortalRoom;
        public List<StructureComponent> secondaryPieces = Lists.<StructureComponent>newArrayList();

        public Stairs2()
        {
        }

        public Stairs2(int componentType, Random rand, int x, int z)
        {
            super(0, rand, x, z);
        }

        public BlockPos getBoundingBoxCenter()
        {
            return this.strongholdPortalRoom != null ? this.strongholdPortalRoom.getBoundingBoxCenter() : super.getBoundingBoxCenter();
        }
    }

    public static class StairsStraight extends StructureStrongholdPieces.Stronghold
    {
        public StairsStraight()
        {
        }

        public StairsStraight(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);
        }

        public static StructureStrongholdPieces.StairsStraight createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -7, 0, 5, 11, 8, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.StairsStraight(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 10, 7, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 7, 0);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 7);
                int i = this.getMetadataWithOffset(Blocks.stone_stairs, 2);

                for (int j = 0; j < 6; ++j)
                {
                    this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(i), 1, 6 - j, 1 + j, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(i), 2, 6 - j, 1 + j, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(i), 3, 6 - j, 1 + j, structureBoundingBoxIn);

                    if (j < 5)
                    {
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 1, 5 - j, 1 + j, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 2, 5 - j, 1 + j, structureBoundingBoxIn);
                        this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), 3, 5 - j, 1 + j, structureBoundingBoxIn);
                    }
                }

                return true;
            }
        }
    }

    static class Stones extends StructureComponent.BlockSelector
    {
        private Stones()
        {
        }

        public void selectBlocks(Random rand, int x, int y, int z, boolean isBoundary)
        {
            if (isBoundary)
            {
                float f = rand.nextFloat();

                if (f < 0.2F)
                {
                    this.blockstate = Blocks.stonebrick.getStateFromMeta(BlockStoneBrick.CRACKED_META);
                }
                else if (f < 0.5F)
                {
                    this.blockstate = Blocks.stonebrick.getStateFromMeta(BlockStoneBrick.MOSSY_META);
                }
                else if (f < 0.55F)
                {
                    this.blockstate = Blocks.monster_egg.getStateFromMeta(BlockSilverfish.EnumType.STONEBRICK.getMetadata());
                }
                else
                {
                    this.blockstate = Blocks.stonebrick.getDefaultState();
                }
            }
            else
            {
                this.blockstate = Blocks.air.getDefaultState();
            }
        }
    }

    public static class Straight extends StructureStrongholdPieces.Stronghold
    {
        private boolean expandsX;
        private boolean expandsZ;

        public Straight()
        {
        }

        public Straight(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.entryDoor = this.getRandomDoor(rand);
            this.boundingBox = boundingBox;
            this.expandsX = rand.nextInt(2) == 0;
            this.expandsZ = rand.nextInt(2) == 0;
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Left", this.expandsX);
            tagCompound.setBoolean("Right", this.expandsZ);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.expandsX = tagCompound.getBoolean("Left");
            this.expandsZ = tagCompound.getBoolean("Right");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 1);

            if (this.expandsX)
            {
                this.getNextComponentX((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 2);
            }

            if (this.expandsZ)
            {
                this.getNextComponentZ((StructureStrongholdPieces.Stairs2)componentIn, listIn, rand, 1, 2);
            }
        }

        public static StructureStrongholdPieces.Straight createPiece(List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, facing);
            return canStrongholdGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureStrongholdPieces.Straight(depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn))
            {
                return false;
            }
            else
            {
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 4, 6, true, randomIn, StructureStrongholdPieces.strongholdStones);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoor, 1, 1, 0);
                this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 6);
                this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 1, 2, 1, Blocks.torch.getDefaultState());
                this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 3, 2, 1, Blocks.torch.getDefaultState());
                this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 1, 2, 5, Blocks.torch.getDefaultState());
                this.randomlyPlaceBlock(worldIn, structureBoundingBoxIn, randomIn, 0.1F, 3, 2, 5, Blocks.torch.getDefaultState());

                if (this.expandsX)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 2, 0, 3, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                if (this.expandsZ)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 2, 4, 3, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }

                return true;
            }
        }
    }

    abstract static class Stronghold extends StructureComponent
    {
        protected StructureStrongholdPieces.Stronghold.Door entryDoor = StructureStrongholdPieces.Stronghold.Door.OPENING;

        public Stronghold()
        {
        }

        protected Stronghold(int componentType)
        {
            super(componentType);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            tagCompound.setString("EntryDoor", this.entryDoor.name());
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            this.entryDoor = StructureStrongholdPieces.Stronghold.Door.valueOf(tagCompound.getString("EntryDoor"));
        }

        protected void placeDoor(World worldIn, Random rand, StructureBoundingBox structurebb, StructureStrongholdPieces.Stronghold.Door door, int x, int y, int z)
        {
            switch (door)
            {
                case OPENING:
                default:
                    this.fillWithBlocks(worldIn, structurebb, x, y, z, x + 3 - 1, y + 3 - 1, z, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                    break;

                case WOOD_DOOR:
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 1, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 2, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 2, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 2, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.oak_door.getDefaultState(), x + 1, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.oak_door.getStateFromMeta(8), x + 1, y + 1, z, structurebb);
                    break;

                case GRATES:
                    this.setBlockState(worldIn, Blocks.air.getDefaultState(), x + 1, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.air.getDefaultState(), x + 1, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x + 1, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x + 2, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x + 2, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), x + 2, y, z, structurebb);
                    break;

                case IRON_DOOR:
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 1, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 2, y + 2, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 2, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stonebrick.getDefaultState(), x + 2, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_door.getDefaultState(), x + 1, y, z, structurebb);
                    this.setBlockState(worldIn, Blocks.iron_door.getStateFromMeta(8), x + 1, y + 1, z, structurebb);
                    this.setBlockState(worldIn, Blocks.stone_button.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_button, 4)), x + 2, y + 1, z + 1, structurebb);
                    this.setBlockState(worldIn, Blocks.stone_button.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_button, 3)), x + 2, y + 1, z - 1, structurebb);
            }
        }

        protected StructureStrongholdPieces.Stronghold.Door getRandomDoor(Random rand)
        {
            int doorType = rand.nextInt(5);

            switch (doorType)
            {
                case 0:
                case 1:
                default:
                    return StructureStrongholdPieces.Stronghold.Door.OPENING;

                case 2:
                    return StructureStrongholdPieces.Stronghold.Door.WOOD_DOOR;

                case 3:
                    return StructureStrongholdPieces.Stronghold.Door.GRATES;

                case 4:
                    return StructureStrongholdPieces.Stronghold.Door.IRON_DOOR;
            }
        }

        protected StructureComponent getNextComponentNormal(StructureStrongholdPieces.Stairs2 start, List<StructureComponent> pieces, Random rand, int offsetX, int offsetY)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, this.coordBaseMode, this.getComponentType());

                    case SOUTH:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, this.coordBaseMode, this.getComponentType());

                    case WEST:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, this.coordBaseMode, this.getComponentType());

                    case EAST:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, this.coordBaseMode, this.getComponentType());
                }
            }

            return null;
        }

        protected StructureComponent getNextComponentX(StructureStrongholdPieces.Stairs2 start, List<StructureComponent> pieces, Random rand, int offsetY, int offsetZ)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.WEST, this.getComponentType());

                    case SOUTH:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.WEST, this.getComponentType());

                    case WEST:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());

                    case EAST:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                }
            }

            return null;
        }

        protected StructureComponent getNextComponentZ(StructureStrongholdPieces.Stairs2 start, List<StructureComponent> pieces, Random rand, int offsetY, int offsetX)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, EnumFacing.EAST, this.getComponentType());

                    case SOUTH:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, EnumFacing.EAST, this.getComponentType());

                    case WEST:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());

                    case EAST:
                        return StructureStrongholdPieces.createAndAddPiece(start, pieces, rand, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                }
            }

            return null;
        }

        protected static boolean canStrongholdGoDeeper(StructureBoundingBox boundingBox)
        {
            return boundingBox != null && boundingBox.minY > 10;
        }

        public static enum Door
        {
            OPENING,
            WOOD_DOOR,
            GRATES,
            IRON_DOOR;
        }
    }
}
