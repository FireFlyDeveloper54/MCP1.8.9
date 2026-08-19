package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

@SuppressWarnings("incomplete-switch")
public class StructureNetherBridgePieces
{
    private static final StructureNetherBridgePieces.PieceWeight[] PRIMARY_PIECES = new StructureNetherBridgePieces.PieceWeight[] {new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Straight.class, 30, 0, true), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Crossing3.class, 10, 4), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Crossing.class, 10, 4), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Stairs.class, 10, 3), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Throne.class, 5, 2), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Entrance.class, 5, 1)};
    private static final StructureNetherBridgePieces.PieceWeight[] SECONDARY_PIECES = new StructureNetherBridgePieces.PieceWeight[] {new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Corridor5.class, 25, 0, true), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Crossing2.class, 15, 5), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Corridor2.class, 5, 10), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Corridor.class, 5, 10), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Corridor3.class, 10, 3, true), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.Corridor4.class, 7, 2), new StructureNetherBridgePieces.PieceWeight(StructureNetherBridgePieces.NetherStalkRoom.class, 5, 2)};

    public static void registerNetherFortressPieces()
    {
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Crossing3.class, "NeBCr");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.End.class, "NeBEF");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Straight.class, "NeBS");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Corridor3.class, "NeCCS");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Corridor4.class, "NeCTB");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Entrance.class, "NeCE");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Crossing2.class, "NeSCSC");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Corridor.class, "NeSCLT");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Corridor5.class, "NeSC");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Corridor2.class, "NeSCRT");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.NetherStalkRoom.class, "NeCSR");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Throne.class, "NeMT");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Crossing.class, "NeRC");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Stairs.class, "NeSR");
        MapGenStructureIO.registerStructureComponent(StructureNetherBridgePieces.Start.class, "NeStart");
    }

    private static StructureNetherBridgePieces.Piece createPiece(StructureNetherBridgePieces.PieceWeight pieceWeight, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
    {
        Class <? extends StructureNetherBridgePieces.Piece > pieceClass = pieceWeight.weightClass;
        StructureNetherBridgePieces.Piece bridgePiece = null;

        if (pieceClass == StructureNetherBridgePieces.Straight.class)
        {
            bridgePiece = StructureNetherBridgePieces.Straight.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Crossing3.class)
        {
            bridgePiece = StructureNetherBridgePieces.Crossing3.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Crossing.class)
        {
            bridgePiece = StructureNetherBridgePieces.Crossing.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Stairs.class)
        {
            bridgePiece = StructureNetherBridgePieces.Stairs.createPiece(pieces, random, x, y, z, depth, facing);
        }
        else if (pieceClass == StructureNetherBridgePieces.Throne.class)
        {
            bridgePiece = StructureNetherBridgePieces.Throne.createPiece(pieces, random, x, y, z, depth, facing);
        }
        else if (pieceClass == StructureNetherBridgePieces.Entrance.class)
        {
            bridgePiece = StructureNetherBridgePieces.Entrance.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Corridor5.class)
        {
            bridgePiece = StructureNetherBridgePieces.Corridor5.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Corridor2.class)
        {
            bridgePiece = StructureNetherBridgePieces.Corridor2.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Corridor.class)
        {
            bridgePiece = StructureNetherBridgePieces.Corridor.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Corridor3.class)
        {
            bridgePiece = StructureNetherBridgePieces.Corridor3.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Corridor4.class)
        {
            bridgePiece = StructureNetherBridgePieces.Corridor4.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.Crossing2.class)
        {
            bridgePiece = StructureNetherBridgePieces.Crossing2.createPiece(pieces, random, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureNetherBridgePieces.NetherStalkRoom.class)
        {
            bridgePiece = StructureNetherBridgePieces.NetherStalkRoom.createPiece(pieces, random, x, y, z, facing, depth);
        }

        return bridgePiece;
    }

    public static class Corridor extends StructureNetherBridgePieces.Piece
    {
        private boolean hasChest;

        public Corridor()
        {
        }

        public Corridor(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.hasChest = rand.nextInt(3) == 0;
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasChest = tagCompound.getBoolean("Chest");
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Chest", this.hasChest);
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentX((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, 1, true);
        }

        public static StructureNetherBridgePieces.Corridor createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, 0, 0, 5, 7, 5, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Corridor(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 1, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 4, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 0, 4, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 1, 4, 4, 1, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 3, 4, 4, 3, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 5, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 4, 3, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 4, 1, 4, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 4, 3, 4, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            if (this.hasChest && structureBoundingBoxIn.isVecInside(new BlockPos(this.getXWithOffset(3, 3), this.getYWithOffset(2), this.getZWithOffset(3, 3))))
            {
                this.hasChest = false;
                this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 3, 2, 3, NETHER_BRIDGE_CHEST_CONTENTS, 2 + randomIn.nextInt(4));
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 0, 4, 6, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Corridor2 extends StructureNetherBridgePieces.Piece
    {
        private boolean hasChest;

        public Corridor2()
        {
        }

        public Corridor2(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.hasChest = rand.nextInt(3) == 0;
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasChest = tagCompound.getBoolean("Chest");
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Chest", this.hasChest);
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentZ((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, 1, true);
        }

        public static StructureNetherBridgePieces.Corridor2 createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, 0, 0, 5, 7, 5, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Corridor2(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 1, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 4, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 1, 0, 4, 1, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 3, 0, 4, 3, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 0, 4, 5, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 4, 4, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 4, 1, 4, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 4, 3, 4, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            if (this.hasChest && structureBoundingBoxIn.isVecInside(new BlockPos(this.getXWithOffset(1, 3), this.getYWithOffset(2), this.getZWithOffset(1, 3))))
            {
                this.hasChest = false;
                this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 1, 2, 3, NETHER_BRIDGE_CHEST_CONTENTS, 2 + randomIn.nextInt(4));
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 0, 4, 6, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Corridor3 extends StructureNetherBridgePieces.Piece
    {
        public Corridor3()
        {
        }

        public Corridor3(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 1, 0, true);
        }

        public static StructureNetherBridgePieces.Corridor3 createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -7, 0, 5, 14, 10, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Corridor3(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            int stairMeta = this.getMetadataWithOffset(Blocks.nether_brick_stairs, 2);

            for (int localZ = 0; localZ <= 9; ++localZ)
            {
                int floorY = Math.max(1, 7 - localZ);
                int ceilingY = Math.min(Math.max(floorY + 5, 14 - localZ), 13);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, localZ, 4, floorY, localZ, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, floorY + 1, localZ, 3, ceilingY - 1, localZ, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

                if (localZ <= 6)
                {
                    this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(stairMeta), 1, floorY + 1, localZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(stairMeta), 2, floorY + 1, localZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(stairMeta), 3, floorY + 1, localZ, structureBoundingBoxIn);
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, ceilingY, localZ, 4, ceilingY, localZ, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, floorY + 1, localZ, 0, ceilingY - 1, localZ, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, floorY + 1, localZ, 4, ceilingY - 1, localZ, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

                if ((localZ & 1) == 0)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, floorY + 2, localZ, 0, floorY + 3, localZ, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, floorY + 2, localZ, 4, floorY + 3, localZ, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                }

                for (int localX = 0; localX <= 4; ++localX)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), localX, -1, localZ, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Corridor4 extends StructureNetherBridgePieces.Piece
    {
        public Corridor4()
        {
        }

        public Corridor4(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            int i = 1;

            if (this.coordBaseMode == EnumFacing.WEST || this.coordBaseMode == EnumFacing.NORTH)
            {
                i = 5;
            }

            this.getNextComponentX((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, i, rand.nextInt(8) > 0);
            this.getNextComponentZ((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, i, rand.nextInt(8) > 0);
        }

        public static StructureNetherBridgePieces.Corridor4 createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -3, 0, 0, 9, 7, 9, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Corridor4(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 8, 1, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 8, 5, 8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 0, 8, 6, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 2, 5, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 0, 8, 5, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 0, 1, 4, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 0, 7, 4, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 4, 8, 2, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 4, 2, 2, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 4, 7, 2, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 8, 8, 3, 8, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 6, 0, 3, 7, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 6, 8, 3, 7, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 4, 0, 5, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 4, 8, 5, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 5, 2, 5, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 5, 7, 5, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 5, 1, 5, 5, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 4, 5, 7, 5, 5, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);

            for (int i = 0; i <= 5; ++i)
            {
                for (int j = 0; j <= 8; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), j, -1, i, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Corridor5 extends StructureNetherBridgePieces.Piece
    {
        public Corridor5()
        {
        }

        public Corridor5(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 1, 0, true);
        }

        public static StructureNetherBridgePieces.Corridor5 createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, 0, 0, 5, 7, 5, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Corridor5(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 1, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 4, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 0, 4, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 1, 0, 4, 1, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 3, 0, 4, 3, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 1, 4, 4, 1, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 3, 4, 4, 3, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 0, 4, 6, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Crossing extends StructureNetherBridgePieces.Piece
    {
        public Crossing()
        {
        }

        public Crossing(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 2, 0, false);
            this.getNextComponentX((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, 2, false);
            this.getNextComponentZ((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, 2, false);
        }

        public static StructureNetherBridgePieces.Crossing createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -2, 0, 0, 7, 9, 7, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Crossing(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 6, 1, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 6, 7, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 1, 6, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 6, 1, 6, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 0, 6, 6, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 6, 6, 6, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 6, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 5, 0, 6, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 0, 6, 6, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 5, 6, 6, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 6, 0, 4, 6, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 0, 4, 5, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 6, 6, 4, 6, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 6, 4, 5, 6, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 2, 0, 6, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 2, 0, 5, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 6, 2, 6, 6, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 5, 2, 6, 5, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);

            for (int i = 0; i <= 6; ++i)
            {
                for (int j = 0; j <= 6; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Crossing2 extends StructureNetherBridgePieces.Piece
    {
        public Crossing2()
        {
        }

        public Crossing2(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 1, 0, true);
            this.getNextComponentX((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, 1, true);
            this.getNextComponentZ((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 0, 1, true);
        }

        public static StructureNetherBridgePieces.Crossing2 createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, 0, 0, 5, 7, 5, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Crossing2(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 1, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 4, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 5, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 0, 4, 5, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 4, 0, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 4, 4, 5, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 0, 4, 6, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 4; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Crossing3 extends StructureNetherBridgePieces.Piece
    {
        public Crossing3()
        {
        }

        public Crossing3(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        protected Crossing3(Random rand, int x, int z)
        {
            super(0);
            this.coordBaseMode = EnumFacing.Plane.HORIZONTAL.random(rand);

            switch (this.coordBaseMode)
            {
                case NORTH:
                case SOUTH:
                    this.boundingBox = new StructureBoundingBox(x, 64, z, x + 19 - 1, 73, z + 19 - 1);
                    break;

                default:
                    this.boundingBox = new StructureBoundingBox(x, 64, z, x + 19 - 1, 73, z + 19 - 1);
            }
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 8, 3, false);
            this.getNextComponentX((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 3, 8, false);
            this.getNextComponentZ((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 3, 8, false);
        }

        public static StructureNetherBridgePieces.Crossing3 createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -8, -3, 0, 19, 10, 19, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Crossing3(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 0, 11, 4, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 7, 18, 4, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 0, 10, 7, 18, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 8, 18, 7, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 5, 0, 7, 5, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 5, 11, 7, 5, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 5, 0, 11, 5, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 5, 11, 11, 5, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 7, 7, 5, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 5, 7, 18, 5, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 11, 7, 5, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 5, 11, 18, 5, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 0, 11, 2, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 13, 11, 2, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 0, 0, 11, 1, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 0, 15, 11, 1, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 7; i <= 11; ++i)
            {
                for (int j = 0; j <= 2; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, 18 - j, structureBoundingBoxIn);
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 7, 5, 2, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 2, 7, 18, 2, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 7, 3, 1, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 0, 7, 18, 1, 11, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int k = 0; k <= 2; ++k)
            {
                for (int l = 7; l <= 11; ++l)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), k, -1, l, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), 18 - k, -1, l, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class End extends StructureNetherBridgePieces.Piece
    {
        private int fillSeed;

        public End()
        {
        }

        public End(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.fillSeed = rand.nextInt();
        }

        public static StructureNetherBridgePieces.End createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -3, 0, 5, 10, 8, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.End(depth, random, boundingBox, facing) : null;
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.fillSeed = tagCompound.getInteger("Seed");
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("Seed", this.fillSeed);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            Random random = new Random((long)this.fillSeed);

            for (int localX = 0; localX <= 4; ++localX)
            {
                for (int localY = 3; localY <= 4; ++localY)
                {
                    int randomZLength = random.nextInt(8);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, localX, localY, 0, localX, localY, randomZLength, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                }
            }

            int leftCapLength = random.nextInt(8);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 0, 5, leftCapLength, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            int rightCapLength = random.nextInt(8);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 0, 4, 5, rightCapLength, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int localX = 0; localX <= 4; ++localX)
            {
                int floorZLength = random.nextInt(5);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, localX, 2, 0, localX, 2, floorZLength, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            }

            for (int localX = 0; localX <= 4; ++localX)
            {
                for (int localY = 0; localY <= 1; ++localY)
                {
                    int baseZLength = random.nextInt(3);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, localX, localY, 0, localX, localY, baseZLength, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                }
            }

            return true;
        }
    }

    public static class Entrance extends StructureNetherBridgePieces.Piece
    {
        public Entrance()
        {
        }

        public Entrance(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 5, 3, true);
        }

        public static StructureNetherBridgePieces.Entrance createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -5, -3, 0, 13, 14, 13, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Entrance(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 12, 4, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 12, 13, 12, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 1, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 5, 0, 12, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 11, 4, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 11, 10, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 9, 11, 7, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 0, 4, 12, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 0, 10, 12, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 9, 0, 7, 12, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 11, 2, 10, 12, 10, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 8, 0, 7, 8, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);

            for (int i = 1; i <= 11; i += 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, i, 10, 0, i, 11, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, i, 10, 12, i, 11, 12, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 10, i, 0, 11, i, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 10, i, 12, 11, i, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), i, 13, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), i, 13, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), 0, 13, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), 12, 13, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), i + 1, 13, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), i + 1, 13, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, i + 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 12, 13, i + 1, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 12, 13, 0, structureBoundingBoxIn);

            for (int k = 3; k <= 9; k += 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, k, 1, 8, k, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 7, k, 11, 8, k, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 0, 8, 2, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 4, 12, 2, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 0, 8, 1, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 9, 8, 1, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 4, 3, 1, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 4, 12, 1, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int l = 4; l <= 8; ++l)
            {
                for (int j = 0; j <= 2; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), l, -1, j, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), l, -1, 12 - j, structureBoundingBoxIn);
                }
            }

            for (int edgeX = 0; edgeX <= 2; ++edgeX)
            {
                for (int centerZ = 4; centerZ <= 8; ++centerZ)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), edgeX, -1, centerZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), 12 - edgeX, -1, centerZ, structureBoundingBoxIn);
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, 5, 7, 5, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 6, 6, 4, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), 6, 0, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.flowing_lava.getDefaultState(), 6, 5, 6, structureBoundingBoxIn);
            BlockPos lavaPos = new BlockPos(this.getXWithOffset(6, 6), this.getYWithOffset(5), this.getZWithOffset(6, 6));

            if (structureBoundingBoxIn.isVecInside(lavaPos))
            {
                worldIn.forceBlockUpdateTick(Blocks.flowing_lava, lavaPos, randomIn);
            }

            return true;
        }
    }

    public static class NetherStalkRoom extends StructureNetherBridgePieces.Piece
    {
        public NetherStalkRoom()
        {
        }

        public NetherStalkRoom(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 5, 3, true);
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 5, 11, true);
        }

        public static StructureNetherBridgePieces.NetherStalkRoom createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -5, -3, 0, 13, 14, 13, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.NetherStalkRoom(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 12, 4, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 12, 13, 12, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 1, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 5, 0, 12, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 11, 4, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 11, 10, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 9, 11, 7, 12, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 0, 4, 12, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 0, 10, 12, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 9, 0, 7, 12, 1, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 11, 2, 10, 12, 10, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 1; i <= 11; i += 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, i, 10, 0, i, 11, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, i, 10, 12, i, 11, 12, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 10, i, 0, 11, i, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 10, i, 12, 11, i, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), i, 13, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), i, 13, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), 0, 13, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), 12, 13, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), i + 1, 13, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), i + 1, 13, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, i + 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 12, 13, i + 1, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 0, 13, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 12, 13, 0, structureBoundingBoxIn);

            for (int fenceZ = 3; fenceZ <= 9; fenceZ += 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, fenceZ, 1, 8, fenceZ, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 7, fenceZ, 11, 8, fenceZ, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            }

            int stairMeta = this.getMetadataWithOffset(Blocks.nether_brick_stairs, 3);

            for (int j = 0; j <= 6; ++j)
            {
                int k = j + 4;

                for (int l = 5; l <= 7; ++l)
                {
                    this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(stairMeta), l, 5 + j, k, structureBoundingBoxIn);
                }

                if (k >= 5 && k <= 8)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, k, 7, j + 4, k, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                }
                else if (k >= 9 && k <= 10)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 8, k, 7, j + 4, k, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
                }

                if (j >= 1)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 6 + j, k, 7, 9 + j, k, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
                }
            }

            for (int stairX = 5; stairX <= 7; ++stairX)
            {
                this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(stairMeta), stairX, 12, 11, structureBoundingBoxIn);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 6, 7, 5, 7, 7, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 6, 7, 7, 7, 7, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 13, 12, 7, 13, 12, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 2, 3, 5, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 9, 3, 5, 10, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 4, 2, 5, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 5, 2, 10, 5, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 5, 9, 10, 5, 10, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 5, 4, 10, 5, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            int rightStairMeta = this.getMetadataWithOffset(Blocks.nether_brick_stairs, 0);
            int leftStairMeta = this.getMetadataWithOffset(Blocks.nether_brick_stairs, 1);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(leftStairMeta), 4, 5, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(leftStairMeta), 4, 5, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(leftStairMeta), 4, 5, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(leftStairMeta), 4, 5, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(rightStairMeta), 8, 5, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(rightStairMeta), 8, 5, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(rightStairMeta), 8, 5, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_stairs.getStateFromMeta(rightStairMeta), 8, 5, 10, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 4, 4, 4, 4, 8, Blocks.soul_sand.getDefaultState(), Blocks.soul_sand.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 4, 4, 9, 4, 8, Blocks.soul_sand.getDefaultState(), Blocks.soul_sand.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 5, 4, 4, 5, 8, Blocks.nether_wart.getDefaultState(), Blocks.nether_wart.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 5, 4, 9, 5, 8, Blocks.nether_wart.getDefaultState(), Blocks.nether_wart.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 0, 8, 2, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 4, 12, 2, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 0, 8, 1, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 9, 8, 1, 12, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 4, 3, 1, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 4, 12, 1, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int centerX = 4; centerX <= 8; ++centerX)
            {
                for (int edgeZ = 0; edgeZ <= 2; ++edgeZ)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), centerX, -1, edgeZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), centerX, -1, 12 - edgeZ, structureBoundingBoxIn);
                }
            }

            for (int edgeX = 0; edgeX <= 2; ++edgeX)
            {
                for (int centerZ = 4; centerZ <= 8; ++centerZ)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), edgeX, -1, centerZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), 12 - edgeX, -1, centerZ, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    abstract static class Piece extends StructureComponent
    {
        protected static final List<WeightedRandomChestContent> NETHER_BRIDGE_CHEST_CONTENTS = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 5), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 5), new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 15), new WeightedRandomChestContent(Items.golden_sword, 0, 1, 1, 5), new WeightedRandomChestContent(Items.golden_chestplate, 0, 1, 1, 5), new WeightedRandomChestContent(Items.flint_and_steel, 0, 1, 1, 5), new WeightedRandomChestContent(Items.nether_wart, 0, 3, 7, 5), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 8), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 5), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 3), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.obsidian), 0, 2, 4, 2)});

        public Piece()
        {
        }

        protected Piece(int componentType)
        {
            super(componentType);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
        }

        private int getTotalWeight(List<StructureNetherBridgePieces.PieceWeight> pieceWeights)
        {
            boolean hasAvailableLimitedPiece = false;
            int totalWeight = 0;

            for (StructureNetherBridgePieces.PieceWeight pieceWeight : pieceWeights)
            {
                if (pieceWeight.maxInstances > 0 && pieceWeight.instancesSpawned < pieceWeight.maxInstances)
                {
                    hasAvailableLimitedPiece = true;
                }

                totalWeight += pieceWeight.weight;
            }

            return hasAvailableLimitedPiece ? totalWeight : -1;
        }

        private StructureNetherBridgePieces.Piece selectNextPiece(StructureNetherBridgePieces.Start start, List<StructureNetherBridgePieces.PieceWeight> pieceWeights, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            int totalWeight = this.getTotalWeight(pieceWeights);
            boolean canSelectPiece = totalWeight > 0 && depth <= 30;
            int attempts = 0;

            while (attempts < 5 && canSelectPiece)
            {
                ++attempts;
                int selectedWeight = random.nextInt(totalWeight);

                for (StructureNetherBridgePieces.PieceWeight pieceWeight : pieceWeights)
                {
                    selectedWeight -= pieceWeight.weight;

                    if (selectedWeight < 0)
                    {
                        if (!pieceWeight.canSpawnMoreStructuresOfType(depth) || pieceWeight == start.previousPieceWeight && !pieceWeight.allowInRow)
                        {
                            break;
                        }

                        StructureNetherBridgePieces.Piece piece = StructureNetherBridgePieces.createPiece(pieceWeight, pieces, random, x, y, z, facing, depth);

                        if (piece != null)
                        {
                            ++pieceWeight.instancesSpawned;
                            start.previousPieceWeight = pieceWeight;

                            if (!pieceWeight.canSpawnMoreStructures())
                            {
                                pieceWeights.remove(pieceWeight);
                            }

                            return piece;
                        }
                    }
                }
            }

            return StructureNetherBridgePieces.End.createPiece(pieces, random, x, y, z, facing, depth);
        }

        private StructureComponent tryCreatePiece(StructureNetherBridgePieces.Start start, List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth, boolean useSecondary)
        {
            if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112)
            {
                List<StructureNetherBridgePieces.PieceWeight> pieceWeights = start.primaryWeights;

                if (useSecondary)
                {
                    pieceWeights = start.secondaryWeights;
                }

                StructureComponent piece = this.selectNextPiece(start, pieceWeights, pieces, random, x, y, z, facing, depth + 1);

                if (piece != null)
                {
                    pieces.add(piece);
                    start.pendingPieces.add(piece);
                }

                return piece;
            }
            else
            {
                return StructureNetherBridgePieces.End.createPiece(pieces, random, x, y, z, facing, depth);
            }
        }

        protected StructureComponent getNextComponentNormal(StructureNetherBridgePieces.Start start, List<StructureComponent> pieces, Random random, int offsetX, int offsetY, boolean useSecondary)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, this.coordBaseMode, this.getComponentType(), useSecondary);

                    case SOUTH:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, this.coordBaseMode, this.getComponentType(), useSecondary);

                    case WEST:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, this.coordBaseMode, this.getComponentType(), useSecondary);

                    case EAST:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, this.coordBaseMode, this.getComponentType(), useSecondary);
                }
            }

            return null;
        }

        protected StructureComponent getNextComponentX(StructureNetherBridgePieces.Start start, List<StructureComponent> pieces, Random random, int offsetY, int offsetZ, boolean useSecondary)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.WEST, this.getComponentType(), useSecondary);

                    case SOUTH:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.WEST, this.getComponentType(), useSecondary);

                    case WEST:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType(), useSecondary);

                    case EAST:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType(), useSecondary);
                }
            }

            return null;
        }

        protected StructureComponent getNextComponentZ(StructureNetherBridgePieces.Start start, List<StructureComponent> pieces, Random random, int offsetY, int offsetX, boolean useSecondary)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, EnumFacing.EAST, this.getComponentType(), useSecondary);

                    case SOUTH:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetX, EnumFacing.EAST, this.getComponentType(), useSecondary);

                    case WEST:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType(), useSecondary);

                    case EAST:
                        return this.tryCreatePiece(start, pieces, random, this.boundingBox.minX + offsetX, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType(), useSecondary);
                }
            }

            return null;
        }

        protected static boolean isAboveGround(StructureBoundingBox boundingBox)
        {
            return boundingBox != null && boundingBox.minY > 10;
        }
    }

    static class PieceWeight
    {
        public Class <? extends StructureNetherBridgePieces.Piece > weightClass;
        public final int weight;
        public int instancesSpawned;
        public int maxInstances;
        public boolean allowInRow;

        public PieceWeight(Class <? extends StructureNetherBridgePieces.Piece > pieceClass, int weight, int maxInstances, boolean allowInRow)
        {
            this.weightClass = pieceClass;
            this.weight = weight;
            this.maxInstances = maxInstances;
            this.allowInRow = allowInRow;
        }

        public PieceWeight(Class <? extends StructureNetherBridgePieces.Piece > pieceClass, int weight, int maxInstances)
        {
            this(pieceClass, weight, maxInstances, false);
        }

        public boolean canSpawnMoreStructuresOfType(int depth)
        {
            return this.maxInstances == 0 || this.instancesSpawned < this.maxInstances;
        }

        public boolean canSpawnMoreStructures()
        {
            return this.maxInstances == 0 || this.instancesSpawned < this.maxInstances;
        }
    }

    public static class Stairs extends StructureNetherBridgePieces.Piece
    {
        public Stairs()
        {
        }

        public Stairs(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentZ((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 6, 2, false);
        }

        public static StructureNetherBridgePieces.Stairs createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, int depth, EnumFacing facing)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -2, 0, 0, 7, 11, 7, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Stairs(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 6, 1, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 6, 10, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 1, 8, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 0, 6, 8, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 1, 0, 8, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 1, 6, 8, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 6, 5, 8, 6, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 2, 0, 5, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 2, 6, 5, 2, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 4, 6, 5, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.nether_brick.getDefaultState(), 5, 2, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 2, 5, 4, 3, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 5, 3, 4, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 5, 2, 5, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 1, 6, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, 1, 5, 7, 4, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 8, 2, 6, 8, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 6, 0, 4, 8, 0, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 5, 0, 4, 5, 0, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);

            for (int i = 0; i <= 6; ++i)
            {
                for (int j = 0; j <= 6; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Start extends StructureNetherBridgePieces.Crossing3
    {
        public StructureNetherBridgePieces.PieceWeight previousPieceWeight;
        public List<StructureNetherBridgePieces.PieceWeight> primaryWeights;
        public List<StructureNetherBridgePieces.PieceWeight> secondaryWeights;
        public List<StructureComponent> pendingPieces = Lists.<StructureComponent>newArrayList();

        public Start()
        {
        }

        public Start(Random rand, int x, int z)
        {
            super(rand, x, z);
            this.primaryWeights = Lists.<StructureNetherBridgePieces.PieceWeight>newArrayList();

            for (StructureNetherBridgePieces.PieceWeight pieceWeight : StructureNetherBridgePieces.PRIMARY_PIECES)
            {
                pieceWeight.instancesSpawned = 0;
                this.primaryWeights.add(pieceWeight);
            }

            this.secondaryWeights = Lists.<StructureNetherBridgePieces.PieceWeight>newArrayList();

            for (StructureNetherBridgePieces.PieceWeight pieceWeight : StructureNetherBridgePieces.SECONDARY_PIECES)
            {
                pieceWeight.instancesSpawned = 0;
                this.secondaryWeights.add(pieceWeight);
            }
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
        }
    }

    public static class Straight extends StructureNetherBridgePieces.Piece
    {
        public Straight()
        {
        }

        public Straight(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            this.getNextComponentNormal((StructureNetherBridgePieces.Start)componentIn, listIn, rand, 1, 3, false);
        }

        public static StructureNetherBridgePieces.Straight createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -3, 0, 5, 10, 19, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Straight(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 4, 4, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 0, 3, 7, 18, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 0, 5, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 0, 4, 5, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 4, 2, 5, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 13, 4, 2, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 1, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 15, 4, 1, 18, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);

            for (int i = 0; i <= 4; ++i)
            {
                for (int j = 0; j <= 2; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, 18 - j, structureBoundingBoxIn);
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 4, 1, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 4, 0, 4, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 14, 0, 4, 14, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 17, 0, 4, 17, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 4, 1, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 4, 4, 4, 4, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 14, 4, 4, 14, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 17, 4, 4, 17, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            return true;
        }
    }

    public static class Throne extends StructureNetherBridgePieces.Piece
    {
        private boolean hasSpawner;

        public Throne()
        {
        }

        public Throne(int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasSpawner = tagCompound.getBoolean("Mob");
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Mob", this.hasSpawner);
        }

        public static StructureNetherBridgePieces.Throne createPiece(List<StructureComponent> pieces, Random random, int x, int y, int z, int depth, EnumFacing facing)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -2, 0, 0, 7, 8, 9, facing);
            return isAboveGround(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureNetherBridgePieces.Throne(depth, random, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 6, 7, 7, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 5, 1, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 1, 5, 2, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 2, 5, 3, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 3, 5, 4, 7, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 1, 4, 2, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 0, 5, 4, 2, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 2, 1, 5, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, 2, 5, 5, 3, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 3, 0, 5, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 5, 3, 6, 5, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 8, 5, 5, 8, Blocks.nether_brick.getDefaultState(), Blocks.nether_brick.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 1, 6, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.nether_brick_fence.getDefaultState(), 5, 6, 3, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 3, 0, 6, 8, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 6, 3, 6, 6, 8, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 6, 8, 5, 7, 8, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 8, 8, 4, 8, 8, Blocks.nether_brick_fence.getDefaultState(), Blocks.nether_brick_fence.getDefaultState(), false);

            if (!this.hasSpawner)
            {
                BlockPos spawnerPos = new BlockPos(this.getXWithOffset(3, 5), this.getYWithOffset(5), this.getZWithOffset(3, 5));

                if (structureBoundingBoxIn.isVecInside(spawnerPos))
                {
                    this.hasSpawner = true;
                    worldIn.setBlockState(spawnerPos, Blocks.mob_spawner.getDefaultState(), 2);
                    TileEntity tileEntity = worldIn.getTileEntity(spawnerPos);

                    if (tileEntity instanceof TileEntityMobSpawner)
                    {
                        ((TileEntityMobSpawner)tileEntity).getSpawnerBaseLogic().setEntityName("Blaze");
                    }
                }
            }

            for (int i = 0; i <= 6; ++i)
            {
                for (int j = 0; j <= 6; ++j)
                {
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.nether_brick.getDefaultState(), i, -1, j, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }
}
