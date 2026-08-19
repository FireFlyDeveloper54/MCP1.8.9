package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class StructureOceanMonumentPieces
{
    public static void registerOceanMonumentPieces()
    {
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.MonumentBuilding.class, "OMB");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.MonumentCoreRoom.class, "OMCR");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleXRoom.class, "OMDXR");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleXYRoom.class, "OMDXYR");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleYRoom.class, "OMDYR");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleYZRoom.class, "OMDYZR");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleZRoom.class, "OMDZR");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.EntryRoom.class, "OMEntry");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.Penthouse.class, "OMPenthouse");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.SimpleRoom.class, "OMSimple");
        MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.SimpleTopRoom.class, "OMSimpleT");
    }

    public static class DoubleXRoom extends StructureOceanMonumentPieces.Piece
    {
        public DoubleXRoom()
        {
        }

        public DoubleXRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 2, 1, 1);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            StructureOceanMonumentPieces.RoomDefinition eastRoom = this.roomDefinition.connections[EnumFacing.EAST.getIndex()];
            StructureOceanMonumentPieces.RoomDefinition currentRoom = this.roomDefinition;

            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 8, 0, eastRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, currentRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (currentRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 4, 1, 7, 4, 6, PRISMARINE);
            }

            if (eastRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 8, 4, 1, 14, 4, 6, PRISMARINE);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 3, 0, 15, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 0, 15, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 7, 14, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 2, 0, 15, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 15, 2, 0, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 7, 14, 2, 7, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 1, 0, 15, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 15, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 7, 14, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 0, 10, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 0, 9, 2, 3, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 3, 0, 10, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.setBlockState(worldIn, SEA_LANTERN, 6, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 9, 2, 3, structureBoundingBoxIn);

            if (currentRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, false);
            }

            if (currentRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 7, 4, 2, 7, false);
            }

            if (currentRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, false);
            }

            if (eastRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 11, 1, 0, 12, 2, 0, false);
            }

            if (eastRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 11, 1, 7, 12, 2, 7, false);
            }

            if (eastRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 15, 1, 3, 15, 2, 4, false);
            }

            return true;
        }
    }

    public static class DoubleXYRoom extends StructureOceanMonumentPieces.Piece
    {
        public DoubleXYRoom()
        {
        }

        public DoubleXYRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 2, 2, 1);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            StructureOceanMonumentPieces.RoomDefinition eastRoom = this.roomDefinition.connections[EnumFacing.EAST.getIndex()];
            StructureOceanMonumentPieces.RoomDefinition currentRoom = this.roomDefinition;
            StructureOceanMonumentPieces.RoomDefinition currentUpperRoom = currentRoom.connections[EnumFacing.UP.getIndex()];
            StructureOceanMonumentPieces.RoomDefinition eastUpperRoom = eastRoom.connections[EnumFacing.UP.getIndex()];

            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 8, 0, eastRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, currentRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (currentUpperRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 8, 1, 7, 8, 6, PRISMARINE);
            }

            if (eastUpperRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 8, 8, 1, 14, 8, 6, PRISMARINE);
            }

            for (int wallY = 1; wallY <= 7; ++wallY)
            {
                IBlockState wallBlockState = PRISMARINE_BRICKS;

                if (wallY == 2 || wallY == 6)
                {
                    wallBlockState = PRISMARINE;
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, wallY, 0, 0, wallY, 7, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, wallY, 0, 15, wallY, 7, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, wallY, 0, 15, wallY, 0, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, wallY, 7, 14, wallY, 7, wallBlockState, wallBlockState, false);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 3, 2, 7, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 2, 4, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 5, 4, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 1, 3, 13, 7, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 1, 2, 12, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 11, 1, 5, 12, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 3, 5, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 1, 3, 10, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 7, 2, 10, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, 2, 5, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 5, 2, 10, 7, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, 5, 5, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 5, 5, 10, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 6, 6, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 9, 6, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 6, 6, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 9, 6, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 4, 3, 6, 4, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 4, 3, 10, 4, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.setBlockState(worldIn, SEA_LANTERN, 5, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 5, 4, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 10, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 10, 4, 5, structureBoundingBoxIn);

            if (currentRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, false);
            }

            if (currentRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 7, 4, 2, 7, false);
            }

            if (currentRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, false);
            }

            if (eastRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 11, 1, 0, 12, 2, 0, false);
            }

            if (eastRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 11, 1, 7, 12, 2, 7, false);
            }

            if (eastRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 15, 1, 3, 15, 2, 4, false);
            }

            if (currentUpperRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 5, 0, 4, 6, 0, false);
            }

            if (currentUpperRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 5, 7, 4, 6, 7, false);
            }

            if (currentUpperRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 5, 3, 0, 6, 4, false);
            }

            if (eastUpperRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 11, 5, 0, 12, 6, 0, false);
            }

            if (eastUpperRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 11, 5, 7, 12, 6, 7, false);
            }

            if (eastUpperRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 15, 5, 3, 15, 6, 4, false);
            }

            return true;
        }
    }

    public static class DoubleYRoom extends StructureOceanMonumentPieces.Piece
    {
        public DoubleYRoom()
        {
        }

        public DoubleYRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 1, 2, 1);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            StructureOceanMonumentPieces.RoomDefinition upperRoom = this.roomDefinition.connections[EnumFacing.UP.getIndex()];

            if (upperRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 8, 1, 6, 8, 6, PRISMARINE);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 0, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 4, 0, 7, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 0, 6, 4, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 7, 6, 4, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 4, 1, 2, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 2, 1, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 4, 1, 5, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 4, 2, 6, 4, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 4, 5, 2, 4, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 5, 1, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 4, 5, 5, 4, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 4, 5, 6, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            StructureOceanMonumentPieces.RoomDefinition activeRoom = this.roomDefinition;

            for (int baseY = 1; baseY <= 5; baseY += 4)
            {
                int doorwayZ = 0;

                if (activeRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, baseY, doorwayZ, 2, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, baseY, doorwayZ, 5, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, baseY + 2, doorwayZ, 4, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, baseY, doorwayZ, 7, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, baseY + 1, doorwayZ, 7, baseY + 1, doorwayZ, PRISMARINE, PRISMARINE, false);
                }

                doorwayZ = 7;

                if (activeRoom.hasOpening[EnumFacing.NORTH.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, baseY, doorwayZ, 2, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, baseY, doorwayZ, 5, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, baseY + 2, doorwayZ, 4, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, baseY, doorwayZ, 7, baseY + 2, doorwayZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, baseY + 1, doorwayZ, 7, baseY + 1, doorwayZ, PRISMARINE, PRISMARINE, false);
                }

                int doorwayX = 0;

                if (activeRoom.hasOpening[EnumFacing.WEST.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY, 2, doorwayX, baseY + 2, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY, 5, doorwayX, baseY + 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY + 2, 3, doorwayX, baseY + 2, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY, 0, doorwayX, baseY + 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY + 1, 0, doorwayX, baseY + 1, 7, PRISMARINE, PRISMARINE, false);
                }

                doorwayX = 7;

                if (activeRoom.hasOpening[EnumFacing.EAST.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY, 2, doorwayX, baseY + 2, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY, 5, doorwayX, baseY + 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY + 2, 3, doorwayX, baseY + 2, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY, 0, doorwayX, baseY + 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, doorwayX, baseY + 1, 0, doorwayX, baseY + 1, 7, PRISMARINE, PRISMARINE, false);
                }

                activeRoom = upperRoom;
            }

            return true;
        }
    }

    public static class DoubleYZRoom extends StructureOceanMonumentPieces.Piece
    {
        public DoubleYZRoom()
        {
        }

        public DoubleYZRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 1, 2, 2);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            StructureOceanMonumentPieces.RoomDefinition northRoom = this.roomDefinition.connections[EnumFacing.NORTH.getIndex()];
            StructureOceanMonumentPieces.RoomDefinition currentRoom = this.roomDefinition;
            StructureOceanMonumentPieces.RoomDefinition northUpperRoom = northRoom.connections[EnumFacing.UP.getIndex()];
            StructureOceanMonumentPieces.RoomDefinition currentUpperRoom = currentRoom.connections[EnumFacing.UP.getIndex()];

            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 8, northRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, currentRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (currentUpperRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 8, 1, 6, 8, 7, PRISMARINE);
            }

            if (northUpperRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 8, 8, 6, 8, 14, PRISMARINE);
            }

            for (int wallY = 1; wallY <= 7; ++wallY)
            {
                IBlockState wallBlockState = PRISMARINE_BRICKS;

                if (wallY == 2 || wallY == 6)
                {
                    wallBlockState = PRISMARINE;
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, wallY, 0, 0, wallY, 15, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, wallY, 0, 7, wallY, 15, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, wallY, 0, 6, wallY, 0, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, wallY, 15, 6, wallY, 15, wallBlockState, wallBlockState, false);
            }

            for (int lampY = 1; lampY <= 7; ++lampY)
            {
                IBlockState centerBlockState = DARK_PRISMARINE;

                if (lampY == 2 || lampY == 6)
                {
                    centerBlockState = SEA_LANTERN;
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, lampY, 7, 4, lampY, 8, centerBlockState, centerBlockState, false);
            }

            if (currentRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, false);
            }

            if (currentRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 1, 3, 7, 2, 4, false);
            }

            if (currentRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, false);
            }

            if (northRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 15, 4, 2, 15, false);
            }

            if (northRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 11, 0, 2, 12, false);
            }

            if (northRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 1, 11, 7, 2, 12, false);
            }

            if (currentUpperRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 5, 0, 4, 6, 0, false);
            }

            if (currentUpperRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 5, 3, 7, 6, 4, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 4, 2, 6, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 2, 6, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 5, 6, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (currentUpperRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 5, 3, 0, 6, 4, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 2, 2, 4, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 2, 1, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 5, 1, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (northUpperRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 5, 15, 4, 6, 15, false);
            }

            if (northUpperRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 5, 11, 0, 6, 12, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 10, 2, 4, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 10, 1, 3, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 13, 1, 3, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            if (northUpperRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 5, 11, 7, 6, 12, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 4, 10, 6, 4, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 10, 6, 3, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 13, 6, 3, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            return true;
        }
    }

    public static class DoubleZRoom extends StructureOceanMonumentPieces.Piece
    {
        public DoubleZRoom()
        {
        }

        public DoubleZRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 1, 1, 2);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            StructureOceanMonumentPieces.RoomDefinition northRoom = this.roomDefinition.connections[EnumFacing.NORTH.getIndex()];
            StructureOceanMonumentPieces.RoomDefinition currentRoom = this.roomDefinition;

            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 8, northRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, currentRoom.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (currentRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 4, 1, 6, 4, 7, PRISMARINE);
            }

            if (northRoom.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 4, 8, 6, 4, 14, PRISMARINE);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 0, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 0, 7, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 0, 7, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 15, 6, 3, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 2, 15, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 0, 7, 2, 15, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 2, 0, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 15, 6, 2, 15, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 0, 7, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 7, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 15, 6, 1, 15, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 1, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 1, 6, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 1, 1, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 1, 6, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 13, 1, 1, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 13, 6, 1, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 13, 1, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 13, 6, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 2, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 6, 5, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 9, 2, 3, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 9, 5, 3, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 6, 4, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 9, 4, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 7, 2, 2, 8, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 7, 5, 2, 8, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.setBlockState(worldIn, SEA_LANTERN, 2, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 5, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 2, 2, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, SEA_LANTERN, 5, 2, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 2, 3, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 5, 3, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 2, 3, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 5, 3, 10, structureBoundingBoxIn);

            if (currentRoom.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, false);
            }

            if (currentRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 1, 3, 7, 2, 4, false);
            }

            if (currentRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, false);
            }

            if (northRoom.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 15, 4, 2, 15, false);
            }

            if (northRoom.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 11, 0, 2, 12, false);
            }

            if (northRoom.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 1, 11, 7, 2, 12, false);
            }

            return true;
        }
    }

    public static class EntryRoom extends StructureOceanMonumentPieces.Piece
    {
        public EntryRoom()
        {
        }

        public EntryRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 1, 1, 1);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 2, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 1, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 2, 0, 7, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 7, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 2, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

            if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 7, 4, 2, 7, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 3, 1, 2, 4, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 6, 1, 3, 7, 2, 4, false);
            }

            return true;
        }
    }

    static class FitSimpleRoomHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private FitSimpleRoomHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            return true;
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            roomDefinition.isClaimed = true;
            return new StructureOceanMonumentPieces.SimpleRoom(direction, roomDefinition, random);
        }
    }

    static class FitSimpleRoomTopHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private FitSimpleRoomTopHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            return !roomDefinition.hasOpening[EnumFacing.WEST.getIndex()] && !roomDefinition.hasOpening[EnumFacing.EAST.getIndex()] && !roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()] && !roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()] && !roomDefinition.hasOpening[EnumFacing.UP.getIndex()];
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            roomDefinition.isClaimed = true;
            return new StructureOceanMonumentPieces.SimpleTopRoom(direction, roomDefinition, random);
        }
    }

    public static class MonumentBuilding extends StructureOceanMonumentPieces.Piece
    {
        private StructureOceanMonumentPieces.RoomDefinition entryRoomDefinition;
        private StructureOceanMonumentPieces.RoomDefinition coreRoomDefinition;
        private List<StructureOceanMonumentPieces.Piece> childPieces = Lists.<StructureOceanMonumentPieces.Piece>newArrayList();

        public MonumentBuilding()
        {
        }

        public MonumentBuilding(Random randomIn, int x, int z, EnumFacing coordBaseModeIn)
        {
            super(0);
            this.coordBaseMode = coordBaseModeIn;

            switch (this.coordBaseMode)
            {
                case NORTH:
                case SOUTH:
                    this.boundingBox = new StructureBoundingBox(x, 39, z, x + 58 - 1, 61, z + 58 - 1);
                    break;

                default:
                    this.boundingBox = new StructureBoundingBox(x, 39, z, x + 58 - 1, 61, z + 58 - 1);
            }

            List<StructureOceanMonumentPieces.RoomDefinition> roomDefinitions = this.generateRoomDefinitions(randomIn);
            this.entryRoomDefinition.isClaimed = true;
            this.childPieces.add(new StructureOceanMonumentPieces.EntryRoom(this.coordBaseMode, this.entryRoomDefinition));
            this.childPieces.add(new StructureOceanMonumentPieces.MonumentCoreRoom(this.coordBaseMode, this.coreRoomDefinition, randomIn));
            List<StructureOceanMonumentPieces.MonumentRoomFitHelper> roomFitHelpers = Lists.<StructureOceanMonumentPieces.MonumentRoomFitHelper>newArrayList();
            roomFitHelpers.add(new StructureOceanMonumentPieces.XYDoubleRoomFitHelper());
            roomFitHelpers.add(new StructureOceanMonumentPieces.YZDoubleRoomFitHelper());
            roomFitHelpers.add(new StructureOceanMonumentPieces.ZDoubleRoomFitHelper());
            roomFitHelpers.add(new StructureOceanMonumentPieces.XDoubleRoomFitHelper());
            roomFitHelpers.add(new StructureOceanMonumentPieces.YDoubleRoomFitHelper());
            roomFitHelpers.add(new StructureOceanMonumentPieces.FitSimpleRoomTopHelper());
            roomFitHelpers.add(new StructureOceanMonumentPieces.FitSimpleRoomHelper());
            label319:

            for (StructureOceanMonumentPieces.RoomDefinition roomDefinition : roomDefinitions)
            {
                if (!roomDefinition.isClaimed && !roomDefinition.isSpecial())
                {
                    for (StructureOceanMonumentPieces.MonumentRoomFitHelper roomFitHelper : roomFitHelpers)
                    {
                        if (roomFitHelper.fits(roomDefinition))
                        {
                            this.childPieces.add(roomFitHelper.create(this.coordBaseMode, roomDefinition, randomIn));
                            continue label319;
                        }
                    }
                }
            }

            int baseY = this.boundingBox.minY;
            int offsetX = this.getXWithOffset(9, 22);
            int offsetZ = this.getZWithOffset(9, 22);

            for (StructureOceanMonumentPieces.Piece childPiece : this.childPieces)
            {
                childPiece.getBoundingBox().offset(offsetX, baseY, offsetZ);
            }

            StructureBoundingBox leftWingBoundingBox = StructureBoundingBox.createProper(this.getXWithOffset(1, 1), this.getYWithOffset(1), this.getZWithOffset(1, 1), this.getXWithOffset(23, 21), this.getYWithOffset(8), this.getZWithOffset(23, 21));
            StructureBoundingBox rightWingBoundingBox = StructureBoundingBox.createProper(this.getXWithOffset(34, 1), this.getYWithOffset(1), this.getZWithOffset(34, 1), this.getXWithOffset(56, 21), this.getYWithOffset(8), this.getZWithOffset(56, 21));
            StructureBoundingBox penthouseBoundingBox = StructureBoundingBox.createProper(this.getXWithOffset(22, 22), this.getYWithOffset(13), this.getZWithOffset(22, 22), this.getXWithOffset(35, 35), this.getYWithOffset(17), this.getZWithOffset(35, 35));
            int wingSeed = randomIn.nextInt();
            this.childPieces.add(new StructureOceanMonumentPieces.WingRoom(this.coordBaseMode, leftWingBoundingBox, wingSeed++));
            this.childPieces.add(new StructureOceanMonumentPieces.WingRoom(this.coordBaseMode, rightWingBoundingBox, wingSeed++));
            this.childPieces.add(new StructureOceanMonumentPieces.Penthouse(this.coordBaseMode, penthouseBoundingBox));
        }

        private List<StructureOceanMonumentPieces.RoomDefinition> generateRoomDefinitions(Random random)
        {
            StructureOceanMonumentPieces.RoomDefinition[] roomDefinitions = new StructureOceanMonumentPieces.RoomDefinition[75];

            for (int roomX = 0; roomX < 5; ++roomX)
            {
                for (int roomZ = 0; roomZ < 4; ++roomZ)
                {
                    int bottomRoomY = 0;
                    int roomIndex = getRoomIndex(roomX, bottomRoomY, roomZ);
                    roomDefinitions[roomIndex] = new StructureOceanMonumentPieces.RoomDefinition(roomIndex);
                }
            }

            for (int roomX = 0; roomX < 5; ++roomX)
            {
                for (int roomZ = 0; roomZ < 4; ++roomZ)
                {
                    int middleRoomY = 1;
                    int roomIndex = getRoomIndex(roomX, middleRoomY, roomZ);
                    roomDefinitions[roomIndex] = new StructureOceanMonumentPieces.RoomDefinition(roomIndex);
                }
            }

            for (int roomX = 1; roomX < 4; ++roomX)
            {
                for (int roomZ = 0; roomZ < 2; ++roomZ)
                {
                    int topRoomY = 2;
                    int roomIndex = getRoomIndex(roomX, topRoomY, roomZ);
                    roomDefinitions[roomIndex] = new StructureOceanMonumentPieces.RoomDefinition(roomIndex);
                }
            }

            this.entryRoomDefinition = roomDefinitions[ENTRY_ROOM_INDEX];

            for (int roomX = 0; roomX < 5; ++roomX)
            {
                for (int roomZ = 0; roomZ < 5; ++roomZ)
                {
                    for (int roomY = 0; roomY < 3; ++roomY)
                    {
                        int currentRoomIndex = getRoomIndex(roomX, roomY, roomZ);

                        if (roomDefinitions[currentRoomIndex] != null)
                        {
                            for (EnumFacing direction : EnumFacing.VALUES)
                            {
                                int neighborRoomX = roomX + direction.getFrontOffsetX();
                                int neighborRoomY = roomY + direction.getFrontOffsetY();
                                int neighborRoomZ = roomZ + direction.getFrontOffsetZ();

                                if (neighborRoomX >= 0 && neighborRoomX < 5 && neighborRoomZ >= 0 && neighborRoomZ < 5 && neighborRoomY >= 0 && neighborRoomY < 3)
                                {
                                    int neighborRoomIndex = getRoomIndex(neighborRoomX, neighborRoomY, neighborRoomZ);

                                    if (roomDefinitions[neighborRoomIndex] != null)
                                    {
                                        if (neighborRoomZ != roomZ)
                                        {
                                            roomDefinitions[currentRoomIndex].setConnection(direction.getOpposite(), roomDefinitions[neighborRoomIndex]);
                                        }
                                        else
                                        {
                                            roomDefinitions[currentRoomIndex].setConnection(direction, roomDefinitions[neighborRoomIndex]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            StructureOceanMonumentPieces.RoomDefinition topRoom;
            roomDefinitions[TOP_ROOM_INDEX].setConnection(EnumFacing.UP, topRoom = new StructureOceanMonumentPieces.RoomDefinition(1003));
            StructureOceanMonumentPieces.RoomDefinition leftWingRoom;
            roomDefinitions[LEFT_WING_ROOM_INDEX].setConnection(EnumFacing.SOUTH, leftWingRoom = new StructureOceanMonumentPieces.RoomDefinition(1001));
            StructureOceanMonumentPieces.RoomDefinition rightWingRoom;
            roomDefinitions[RIGHT_WING_ROOM_INDEX].setConnection(EnumFacing.SOUTH, rightWingRoom = new StructureOceanMonumentPieces.RoomDefinition(1002));
            topRoom.isClaimed = true;
            leftWingRoom.isClaimed = true;
            rightWingRoom.isClaimed = true;
            this.entryRoomDefinition.isSource = true;
            this.coreRoomDefinition = roomDefinitions[getRoomIndex(random.nextInt(4), 0, 2)];
            this.coreRoomDefinition.isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.EAST.getIndex()].isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.NORTH.getIndex()].isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.NORTH.getIndex()].isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.UP.getIndex()].isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].isClaimed = true;
            this.coreRoomDefinition.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].isClaimed = true;
            List<StructureOceanMonumentPieces.RoomDefinition> roomDefinitionList = Lists.<StructureOceanMonumentPieces.RoomDefinition>newArrayList();

            for (StructureOceanMonumentPieces.RoomDefinition roomDefinition : roomDefinitions)
            {
                if (roomDefinition != null)
                {
                    roomDefinition.updateOpenings();
                    roomDefinitionList.add(roomDefinition);
                }
            }

            topRoom.updateOpenings();
            Collections.shuffle(roomDefinitionList, random);
            int scanId = 1;

            for (StructureOceanMonumentPieces.RoomDefinition roomDefinition : roomDefinitionList)
            {
                int removedOpenings = 0;
                int attempts = 0;

                while (removedOpenings < 2 && attempts < 5)
                {
                    ++attempts;
                    int directionIndex = random.nextInt(6);

                    if (roomDefinition.hasOpening[directionIndex])
                    {
                        int oppositeDirectionIndex = EnumFacing.getFront(directionIndex).getOpposite().getIndex();
                        roomDefinition.hasOpening[directionIndex] = false;
                        roomDefinition.connections[directionIndex].hasOpening[oppositeDirectionIndex] = false;

                        if (roomDefinition.canReachSource(scanId++) && roomDefinition.connections[directionIndex].canReachSource(scanId++))
                        {
                            ++removedOpenings;
                        }
                        else
                        {
                            roomDefinition.hasOpening[directionIndex] = true;
                            roomDefinition.connections[directionIndex].hasOpening[oppositeDirectionIndex] = true;
                        }
                    }
                }
            }

            roomDefinitionList.add(topRoom);
            roomDefinitionList.add(leftWingRoom);
            roomDefinitionList.add(rightWingRoom);
            return roomDefinitionList;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            int waterHeight = Math.max(worldIn.getSeaLevel(), 64) - this.boundingBox.minY;
            this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 0, 0, 58, waterHeight, 58, false);
            this.generateWing(false, 0, worldIn, randomIn, structureBoundingBoxIn);
            this.generateWing(true, 33, worldIn, randomIn, structureBoundingBoxIn);
            this.generateEntrance(worldIn, randomIn, structureBoundingBoxIn);
            this.generateFrontWall(worldIn, randomIn, structureBoundingBoxIn);
            this.generateCore(worldIn, randomIn, structureBoundingBoxIn);
            this.generateSideWalls(worldIn, randomIn, structureBoundingBoxIn);
            this.generateBackWall(worldIn, randomIn, structureBoundingBoxIn);
            this.generateUpperWall(worldIn, randomIn, structureBoundingBoxIn);

            for (int gridX = 0; gridX < 7; ++gridX)
            {
                int gridZ = 0;

                while (gridZ < 7)
                {
                    if (gridZ == 0 && gridX == 3)
                    {
                        gridZ = 6;
                    }

                    int baseX = gridX * 9;
                    int baseZ = gridZ * 9;

                    for (int localX = 0; localX < 4; ++localX)
                    {
                        for (int localZ = 0; localZ < 4; ++localZ)
                        {
                            this.setBlockState(worldIn, PRISMARINE_BRICKS, baseX + localX, 0, baseZ + localZ, structureBoundingBoxIn);
                            this.replaceAirAndLiquidDownwards(worldIn, PRISMARINE_BRICKS, baseX + localX, -1, baseZ + localZ, structureBoundingBoxIn);
                        }
                    }

                    if (gridX != 0 && gridX != 6)
                    {
                        gridZ += 6;
                    }
                    else
                    {
                        ++gridZ;
                    }
                }
            }

            for (int shellLayer = 0; shellLayer < 5; ++shellLayer)
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, -1 - shellLayer, 0 + shellLayer * 2, -1 - shellLayer, -1 - shellLayer, 23, 58 + shellLayer, false);
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 58 + shellLayer, 0 + shellLayer * 2, -1 - shellLayer, 58 + shellLayer, 23, 58 + shellLayer, false);
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0 - shellLayer, 0 + shellLayer * 2, -1 - shellLayer, 57 + shellLayer, 23, -1 - shellLayer, false);
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0 - shellLayer, 0 + shellLayer * 2, 58 + shellLayer, 57 + shellLayer, 23, 58 + shellLayer, false);
            }

            for (StructureOceanMonumentPieces.Piece childPiece : this.childPieces)
            {
                if (childPiece.getBoundingBox().intersectsWith(structureBoundingBoxIn))
                {
                    childPiece.addComponentParts(worldIn, randomIn, structureBoundingBoxIn);
                }
            }

            return true;
        }

        private void generateWing(boolean mirror, int xOffset, World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, xOffset, 0, xOffset + 23, 20))
            {
                this.fillWithBlocks(worldIn, boundingBox, xOffset + 0, 0, 0, xOffset + 24, 0, 20, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, xOffset + 0, 1, 0, xOffset + 24, 10, 20, false);

                for (int slopeStep = 0; slopeStep < 4; ++slopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, xOffset + slopeStep, slopeStep + 1, slopeStep, xOffset + slopeStep, slopeStep + 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, xOffset + slopeStep + 7, slopeStep + 5, slopeStep + 7, xOffset + slopeStep + 7, slopeStep + 5, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, xOffset + 17 - slopeStep, slopeStep + 5, slopeStep + 7, xOffset + 17 - slopeStep, slopeStep + 5, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, xOffset + 24 - slopeStep, slopeStep + 1, slopeStep, xOffset + 24 - slopeStep, slopeStep + 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, xOffset + slopeStep + 1, slopeStep + 1, slopeStep, xOffset + 23 - slopeStep, slopeStep + 1, slopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, xOffset + slopeStep + 8, slopeStep + 5, slopeStep + 7, xOffset + 16 - slopeStep, slopeStep + 5, slopeStep + 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                this.fillWithBlocks(worldIn, boundingBox, xOffset + 4, 4, 4, xOffset + 6, 4, 20, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, xOffset + 7, 4, 4, xOffset + 17, 4, 6, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, xOffset + 18, 4, 4, xOffset + 20, 4, 20, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, xOffset + 11, 8, 11, xOffset + 13, 8, 20, PRISMARINE, PRISMARINE, false);
                this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, xOffset + 12, 9, 12, boundingBox);
                this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, xOffset + 12, 9, 15, boundingBox);
                this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, xOffset + 12, 9, 18, boundingBox);
                int firstDecorationX = mirror ? xOffset + 19 : xOffset + 5;
                int secondDecorationX = mirror ? xOffset + 5 : xOffset + 19;

                for (int decorationZ = 20; decorationZ >= 5; decorationZ -= 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, firstDecorationX, 5, decorationZ, boundingBox);
                }

                for (int decorationZ = 19; decorationZ >= 7; decorationZ -= 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, secondDecorationX, 5, decorationZ, boundingBox);
                }

                for (int decorationStep = 0; decorationStep < 4; ++decorationStep)
                {
                    int frontDecorationX = mirror ? xOffset + (24 - (17 - decorationStep * 3)) : xOffset + 17 - decorationStep * 3;
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, frontDecorationX, 5, 5, boundingBox);
                }

                this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, secondDecorationX, 5, 5, boundingBox);
                this.fillWithBlocks(worldIn, boundingBox, xOffset + 11, 1, 12, xOffset + 13, 7, 12, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, xOffset + 12, 1, 11, xOffset + 12, 7, 13, PRISMARINE, PRISMARINE, false);
            }
        }

        private void generateEntrance(World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, 22, 5, 35, 17))
            {
                this.fillWithAirOrWater(worldIn, boundingBox, 25, 0, 0, 32, 8, 20, false);

                for (int entrancePillarIndex = 0; entrancePillarIndex < 4; ++entrancePillarIndex)
                {
                    int pillarZ = 5 + entrancePillarIndex * 4;
                    this.fillWithBlocks(worldIn, boundingBox, 24, 2, pillarZ, 24, 4, pillarZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, 22, 4, pillarZ, 23, 4, pillarZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.setBlockState(worldIn, PRISMARINE_BRICKS, 25, 5, pillarZ, boundingBox);
                    this.setBlockState(worldIn, PRISMARINE_BRICKS, 26, 6, pillarZ, boundingBox);
                    this.setBlockState(worldIn, SEA_LANTERN, 26, 5, pillarZ, boundingBox);
                    this.fillWithBlocks(worldIn, boundingBox, 33, 2, pillarZ, 33, 4, pillarZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, 34, 4, pillarZ, 35, 4, pillarZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.setBlockState(worldIn, PRISMARINE_BRICKS, 32, 5, pillarZ, boundingBox);
                    this.setBlockState(worldIn, PRISMARINE_BRICKS, 31, 6, pillarZ, boundingBox);
                    this.setBlockState(worldIn, SEA_LANTERN, 31, 5, pillarZ, boundingBox);
                    this.fillWithBlocks(worldIn, boundingBox, 27, 6, pillarZ, 30, 6, pillarZ, PRISMARINE, PRISMARINE, false);
                }
            }
        }

        private void generateFrontWall(World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, 15, 20, 42, 21))
            {
                this.fillWithBlocks(worldIn, boundingBox, 15, 0, 21, 42, 0, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 26, 1, 21, 31, 3, 21, false);
                this.fillWithBlocks(worldIn, boundingBox, 21, 12, 21, 36, 12, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 17, 11, 21, 40, 11, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 16, 10, 21, 41, 10, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 15, 7, 21, 42, 9, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 16, 6, 21, 41, 6, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 17, 5, 21, 40, 5, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 21, 4, 21, 36, 4, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 22, 3, 21, 26, 3, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 31, 3, 21, 35, 3, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 23, 2, 21, 25, 2, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 32, 2, 21, 34, 2, 21, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 28, 4, 20, 29, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 27, 3, 21, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 30, 3, 21, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 26, 2, 21, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 31, 2, 21, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 25, 1, 21, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 32, 1, 21, boundingBox);

                for (int lowerArchStep = 0; lowerArchStep < 7; ++lowerArchStep)
                {
                    this.setBlockState(worldIn, DARK_PRISMARINE, 28 - lowerArchStep, 6 + lowerArchStep, 21, boundingBox);
                    this.setBlockState(worldIn, DARK_PRISMARINE, 29 + lowerArchStep, 6 + lowerArchStep, 21, boundingBox);
                }

                for (int upperArchStep = 0; upperArchStep < 4; ++upperArchStep)
                {
                    this.setBlockState(worldIn, DARK_PRISMARINE, 28 - upperArchStep, 9 + upperArchStep, 21, boundingBox);
                    this.setBlockState(worldIn, DARK_PRISMARINE, 29 + upperArchStep, 9 + upperArchStep, 21, boundingBox);
                }

                this.setBlockState(worldIn, DARK_PRISMARINE, 28, 12, 21, boundingBox);
                this.setBlockState(worldIn, DARK_PRISMARINE, 29, 12, 21, boundingBox);

                for (int sideAccentIndex = 0; sideAccentIndex < 3; ++sideAccentIndex)
                {
                    this.setBlockState(worldIn, DARK_PRISMARINE, 22 - sideAccentIndex * 2, 8, 21, boundingBox);
                    this.setBlockState(worldIn, DARK_PRISMARINE, 22 - sideAccentIndex * 2, 9, 21, boundingBox);
                    this.setBlockState(worldIn, DARK_PRISMARINE, 35 + sideAccentIndex * 2, 8, 21, boundingBox);
                    this.setBlockState(worldIn, DARK_PRISMARINE, 35 + sideAccentIndex * 2, 9, 21, boundingBox);
                }

                this.fillWithAirOrWater(worldIn, boundingBox, 15, 13, 21, 42, 15, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 15, 1, 21, 15, 6, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 16, 1, 21, 16, 5, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 17, 1, 21, 20, 4, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 21, 1, 21, 21, 3, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 22, 1, 21, 22, 2, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 23, 1, 21, 24, 1, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 42, 1, 21, 42, 6, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 41, 1, 21, 41, 5, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 37, 1, 21, 40, 4, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 36, 1, 21, 36, 3, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 33, 1, 21, 34, 1, 21, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 35, 1, 21, 35, 2, 21, false);
            }
        }

        private void generateCore(World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, 21, 21, 36, 36))
            {
                this.fillWithBlocks(worldIn, boundingBox, 21, 0, 22, 36, 0, 36, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 21, 1, 22, 36, 23, 36, false);

                for (int coreRoofStep = 0; coreRoofStep < 4; ++coreRoofStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, 21 + coreRoofStep, 13 + coreRoofStep, 21 + coreRoofStep, 36 - coreRoofStep, 13 + coreRoofStep, 21 + coreRoofStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, 21 + coreRoofStep, 13 + coreRoofStep, 36 - coreRoofStep, 36 - coreRoofStep, 13 + coreRoofStep, 36 - coreRoofStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, 21 + coreRoofStep, 13 + coreRoofStep, 22 + coreRoofStep, 21 + coreRoofStep, 13 + coreRoofStep, 35 - coreRoofStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, boundingBox, 36 - coreRoofStep, 13 + coreRoofStep, 22 + coreRoofStep, 36 - coreRoofStep, 13 + coreRoofStep, 35 - coreRoofStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                this.fillWithBlocks(worldIn, boundingBox, 25, 16, 25, 32, 16, 32, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 25, 17, 25, 25, 19, 25, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, 32, 17, 25, 32, 19, 25, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, 25, 17, 32, 25, 19, 32, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, 32, 17, 32, 32, 19, 32, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 26, 20, 26, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 27, 21, 27, boundingBox);
                this.setBlockState(worldIn, SEA_LANTERN, 27, 20, 27, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 26, 20, 31, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 27, 21, 30, boundingBox);
                this.setBlockState(worldIn, SEA_LANTERN, 27, 20, 30, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 31, 20, 31, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 30, 21, 30, boundingBox);
                this.setBlockState(worldIn, SEA_LANTERN, 30, 20, 30, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 31, 20, 26, boundingBox);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 30, 21, 27, boundingBox);
                this.setBlockState(worldIn, SEA_LANTERN, 30, 20, 27, boundingBox);
                this.fillWithBlocks(worldIn, boundingBox, 28, 21, 27, 29, 21, 27, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 27, 21, 28, 27, 21, 29, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 28, 21, 30, 29, 21, 30, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 30, 21, 28, 30, 21, 29, PRISMARINE, PRISMARINE, false);
            }
        }

        private void generateSideWalls(World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, 0, 21, 6, 58))
            {
                this.fillWithBlocks(worldIn, boundingBox, 0, 0, 21, 6, 0, 57, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 0, 1, 21, 6, 7, 57, false);
                this.fillWithBlocks(worldIn, boundingBox, 4, 4, 21, 6, 4, 53, PRISMARINE, PRISMARINE, false);

                for (int leftOuterSlopeStep = 0; leftOuterSlopeStep < 4; ++leftOuterSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, leftOuterSlopeStep, leftOuterSlopeStep + 1, 21, leftOuterSlopeStep, leftOuterSlopeStep + 1, 57 - leftOuterSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationZ = 23; decorationZ < 53; decorationZ += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 5, 5, decorationZ, boundingBox);
                }

                this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 5, 5, 52, boundingBox);

                for (int leftInnerSlopeStep = 0; leftInnerSlopeStep < 4; ++leftInnerSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, leftInnerSlopeStep, leftInnerSlopeStep + 1, 21, leftInnerSlopeStep, leftInnerSlopeStep + 1, 57 - leftInnerSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                this.fillWithBlocks(worldIn, boundingBox, 4, 1, 52, 6, 3, 52, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 5, 1, 51, 5, 3, 53, PRISMARINE, PRISMARINE, false);
            }

            if (this.intersectsWith(boundingBox, 51, 21, 58, 58))
            {
                this.fillWithBlocks(worldIn, boundingBox, 51, 0, 21, 57, 0, 57, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 51, 1, 21, 57, 7, 57, false);
                this.fillWithBlocks(worldIn, boundingBox, 51, 4, 21, 53, 4, 53, PRISMARINE, PRISMARINE, false);

                for (int rightSlopeStep = 0; rightSlopeStep < 4; ++rightSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, 57 - rightSlopeStep, rightSlopeStep + 1, 21, 57 - rightSlopeStep, rightSlopeStep + 1, 57 - rightSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationZ = 23; decorationZ < 53; decorationZ += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 52, 5, decorationZ, boundingBox);
                }

                this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 52, 5, 52, boundingBox);
                this.fillWithBlocks(worldIn, boundingBox, 51, 1, 52, 53, 3, 52, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 52, 1, 51, 52, 3, 53, PRISMARINE, PRISMARINE, false);
            }

            if (this.intersectsWith(boundingBox, 0, 51, 57, 57))
            {
                this.fillWithBlocks(worldIn, boundingBox, 7, 0, 51, 50, 0, 57, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 7, 1, 51, 50, 10, 57, false);

                for (int rearSlopeStep = 0; rearSlopeStep < 4; ++rearSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, rearSlopeStep + 1, rearSlopeStep + 1, 57 - rearSlopeStep, 56 - rearSlopeStep, rearSlopeStep + 1, 57 - rearSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
            }
        }

        private void generateBackWall(World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, 7, 21, 13, 50))
            {
                this.fillWithBlocks(worldIn, boundingBox, 7, 0, 21, 13, 0, 50, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 7, 1, 21, 13, 10, 50, false);
                this.fillWithBlocks(worldIn, boundingBox, 11, 8, 21, 13, 8, 53, PRISMARINE, PRISMARINE, false);

                for (int leftBackSlopeStep = 0; leftBackSlopeStep < 4; ++leftBackSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, leftBackSlopeStep + 7, leftBackSlopeStep + 5, 21, leftBackSlopeStep + 7, leftBackSlopeStep + 5, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationZ = 21; decorationZ <= 45; decorationZ += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 12, 9, decorationZ, boundingBox);
                }
            }

            if (this.intersectsWith(boundingBox, 44, 21, 50, 54))
            {
                this.fillWithBlocks(worldIn, boundingBox, 44, 0, 21, 50, 0, 50, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 44, 1, 21, 50, 10, 50, false);
                this.fillWithBlocks(worldIn, boundingBox, 44, 8, 21, 46, 8, 53, PRISMARINE, PRISMARINE, false);

                for (int rightBackSlopeStep = 0; rightBackSlopeStep < 4; ++rightBackSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, 50 - rightBackSlopeStep, rightBackSlopeStep + 5, 21, 50 - rightBackSlopeStep, rightBackSlopeStep + 5, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationZ = 21; decorationZ <= 45; decorationZ += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 45, 9, decorationZ, boundingBox);
                }
            }

            if (this.intersectsWith(boundingBox, 8, 44, 49, 54))
            {
                this.fillWithBlocks(worldIn, boundingBox, 14, 0, 44, 43, 0, 50, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 14, 1, 44, 43, 10, 50, false);

                for (int decorationX = 12; decorationX <= 45; decorationX += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 9, 45, boundingBox);
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 9, 52, boundingBox);

                    if (decorationX == 12 || decorationX == 18 || decorationX == 24 || decorationX == 33 || decorationX == 39 || decorationX == 45)
                    {
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 9, 47, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 9, 50, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 10, 45, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 10, 46, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 10, 51, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 10, 52, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 11, 47, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 11, 50, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 12, 48, boundingBox);
                        this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 12, 49, boundingBox);
                    }
                }

                for (int rearCapStep = 0; rearCapStep < 3; ++rearCapStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, 8 + rearCapStep, 5 + rearCapStep, 54, 49 - rearCapStep, 5 + rearCapStep, 54, PRISMARINE, PRISMARINE, false);
                }

                this.fillWithBlocks(worldIn, boundingBox, 11, 8, 54, 46, 8, 54, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, 14, 8, 44, 43, 8, 53, PRISMARINE, PRISMARINE, false);
            }
        }

        private void generateUpperWall(World worldIn, Random random, StructureBoundingBox boundingBox)
        {
            if (this.intersectsWith(boundingBox, 14, 21, 20, 43))
            {
                this.fillWithBlocks(worldIn, boundingBox, 14, 0, 21, 20, 0, 43, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 14, 1, 22, 20, 14, 43, false);
                this.fillWithBlocks(worldIn, boundingBox, 18, 12, 22, 20, 12, 39, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 18, 12, 21, 20, 12, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

                for (int leftUpperSlopeStep = 0; leftUpperSlopeStep < 4; ++leftUpperSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, leftUpperSlopeStep + 14, leftUpperSlopeStep + 9, 21, leftUpperSlopeStep + 14, leftUpperSlopeStep + 9, 43 - leftUpperSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationZ = 23; decorationZ <= 39; decorationZ += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 19, 13, decorationZ, boundingBox);
                }
            }

            if (this.intersectsWith(boundingBox, 37, 21, 43, 43))
            {
                this.fillWithBlocks(worldIn, boundingBox, 37, 0, 21, 43, 0, 43, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 37, 1, 22, 43, 14, 43, false);
                this.fillWithBlocks(worldIn, boundingBox, 37, 12, 22, 39, 12, 39, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, 37, 12, 21, 39, 12, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

                for (int rightUpperSlopeStep = 0; rightUpperSlopeStep < 4; ++rightUpperSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, 43 - rightUpperSlopeStep, rightUpperSlopeStep + 9, 21, 43 - rightUpperSlopeStep, rightUpperSlopeStep + 9, 43 - rightUpperSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationZ = 23; decorationZ <= 39; decorationZ += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, 38, 13, decorationZ, boundingBox);
                }
            }

            if (this.intersectsWith(boundingBox, 15, 37, 42, 43))
            {
                this.fillWithBlocks(worldIn, boundingBox, 21, 0, 37, 36, 0, 43, PRISMARINE, PRISMARINE, false);
                this.fillWithAirOrWater(worldIn, boundingBox, 21, 1, 37, 36, 14, 43, false);
                this.fillWithBlocks(worldIn, boundingBox, 21, 12, 37, 36, 12, 39, PRISMARINE, PRISMARINE, false);

                for (int rearUpperSlopeStep = 0; rearUpperSlopeStep < 4; ++rearUpperSlopeStep)
                {
                    this.fillWithBlocks(worldIn, boundingBox, 15 + rearUpperSlopeStep, rearUpperSlopeStep + 9, 43 - rearUpperSlopeStep, 42 - rearUpperSlopeStep, rearUpperSlopeStep + 9, 43 - rearUpperSlopeStep, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                for (int decorationX = 21; decorationX <= 36; decorationX += 3)
                {
                    this.setBlockState(worldIn, DECORATIVE_PRISMARINE_BRICKS, decorationX, 13, 38, boundingBox);
                }
            }
        }
    }

    public static class MonumentCoreRoom extends StructureOceanMonumentPieces.Piece
    {
        public MonumentCoreRoom()
        {
        }

        public MonumentCoreRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 2, 2, 2);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 8, 0, 14, 8, 14, PRISMARINE);
            int topWallY = 7;
            IBlockState wallBlockState = PRISMARINE_BRICKS;
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, topWallY, 0, 0, topWallY, 15, wallBlockState, wallBlockState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, topWallY, 0, 15, topWallY, 15, wallBlockState, wallBlockState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, topWallY, 0, 15, topWallY, 0, wallBlockState, wallBlockState, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, topWallY, 15, 14, topWallY, 15, wallBlockState, wallBlockState, false);

            for (int wallY = 1; wallY <= 6; ++wallY)
            {
                wallBlockState = PRISMARINE_BRICKS;

                if (wallY == 2 || wallY == 6)
                {
                    wallBlockState = PRISMARINE;
                }

                for (int sideX = 0; sideX <= 15; sideX += 15)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, sideX, wallY, 0, sideX, wallY, 1, wallBlockState, wallBlockState, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, sideX, wallY, 6, sideX, wallY, 9, wallBlockState, wallBlockState, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, sideX, wallY, 14, sideX, wallY, 15, wallBlockState, wallBlockState, false);
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, wallY, 0, 1, wallY, 0, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, wallY, 0, 9, wallY, 0, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, wallY, 0, 14, wallY, 0, wallBlockState, wallBlockState, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, wallY, 15, 14, wallY, 15, wallBlockState, wallBlockState, false);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 6, 9, 6, 9, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 4, 7, 8, 5, 8, Blocks.gold_block.getDefaultState(), Blocks.gold_block.getDefaultState(), false);

            for (int lampY = 3; lampY <= 6; lampY += 3)
            {
                for (int lampX = 6; lampX <= 9; lampX += 3)
                {
                    this.setBlockState(worldIn, SEA_LANTERN, lampX, lampY, 6, structureBoundingBoxIn);
                    this.setBlockState(worldIn, SEA_LANTERN, lampX, lampY, 9, structureBoundingBoxIn);
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 6, 5, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 9, 5, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 1, 6, 10, 2, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 1, 9, 10, 2, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 5, 6, 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, 5, 9, 2, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 10, 6, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, 10, 9, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 5, 5, 6, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 10, 5, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 2, 5, 10, 6, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 2, 10, 10, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 7, 1, 5, 7, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 7, 1, 10, 7, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 7, 9, 5, 7, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 7, 9, 10, 7, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, 5, 6, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 7, 10, 6, 7, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 7, 5, 14, 7, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 7, 10, 14, 7, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 2, 2, 1, 3, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 2, 3, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 1, 2, 13, 1, 3, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, 2, 12, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 12, 2, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 13, 3, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 1, 12, 13, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, 13, 12, 1, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            return true;
        }
    }

    interface MonumentRoomFitHelper
    {
        boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition);

        StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random);
    }

    public static class Penthouse extends StructureOceanMonumentPieces.Piece
    {
        public Penthouse()
        {
        }

        public Penthouse(EnumFacing coordBaseModeIn, StructureBoundingBox boundingBoxIn)
        {
            super(coordBaseModeIn, boundingBoxIn);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, -1, 2, 11, -1, 11, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, -1, 0, 1, -1, 11, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, -1, 0, 13, -1, 11, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, -1, 0, 11, -1, 1, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, -1, 12, 11, -1, 13, PRISMARINE, PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 0, 0, 13, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 12, 0, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 13, 12, 0, 13, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);

            for (int edgeLanternOffset = 2; edgeLanternOffset <= 11; edgeLanternOffset += 3)
            {
                this.setBlockState(worldIn, SEA_LANTERN, 0, 0, edgeLanternOffset, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 13, 0, edgeLanternOffset, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, edgeLanternOffset, 0, 0, structureBoundingBoxIn);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 3, 4, 0, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 3, 11, 0, 9, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 9, 9, 0, 11, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 5, 0, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 8, 0, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 10, 0, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, PRISMARINE_BRICKS, 3, 0, 10, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 3, 3, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 0, 3, 10, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 10, 7, 0, 10, DARK_PRISMARINE, DARK_PRISMARINE, false);
            int pillarX = 3;

            for (int pillarPass = 0; pillarPass < 2; ++pillarPass)
            {
                for (int pillarZ = 2; pillarZ <= 8; pillarZ += 3)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, pillarX, 0, pillarZ, pillarX, 2, pillarZ, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                pillarX = 10;
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 0, 10, 5, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 10, 8, 2, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, -1, 7, 7, -1, 8, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 6, -1, 3, 7, -1, 4, false);
            this.spawnElderGuardian(worldIn, structureBoundingBoxIn, 6, 1, 6);
            return true;
        }
    }

    public abstract static class Piece extends StructureComponent
    {
        protected static final IBlockState PRISMARINE = Blocks.prismarine.getStateFromMeta(BlockPrismarine.ROUGH_META);
        protected static final IBlockState PRISMARINE_BRICKS = Blocks.prismarine.getStateFromMeta(BlockPrismarine.BRICKS_META);
        protected static final IBlockState DARK_PRISMARINE = Blocks.prismarine.getStateFromMeta(BlockPrismarine.DARK_META);
        protected static final IBlockState DECORATIVE_PRISMARINE_BRICKS = PRISMARINE_BRICKS;
        protected static final IBlockState SEA_LANTERN = Blocks.sea_lantern.getDefaultState();
        protected static final IBlockState WATER = Blocks.water.getDefaultState();
        protected static final int ENTRY_ROOM_INDEX = getRoomIndex(2, 0, 0);
        protected static final int TOP_ROOM_INDEX = getRoomIndex(2, 2, 0);
        protected static final int LEFT_WING_ROOM_INDEX = getRoomIndex(0, 1, 0);
        protected static final int RIGHT_WING_ROOM_INDEX = getRoomIndex(4, 1, 0);
        protected StructureOceanMonumentPieces.RoomDefinition roomDefinition;

        protected static final int getRoomIndex(int x, int y, int z)
        {
            return y * 25 + z * 5 + x;
        }

        public Piece()
        {
            super(0);
        }

        public Piece(int componentTypeIn)
        {
            super(componentTypeIn);
        }

        public Piece(EnumFacing coordBaseModeIn, StructureBoundingBox boundingBoxIn)
        {
            super(1);
            this.coordBaseMode = coordBaseModeIn;
            this.boundingBox = boundingBoxIn;
        }

        protected Piece(int componentTypeIn, EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, int roomXSize, int roomYSize, int roomZSize)
        {
            super(componentTypeIn);
            this.coordBaseMode = coordBaseModeIn;
            this.roomDefinition = roomDefinitionIn;
            int roomIndex = roomDefinitionIn.index;
            int roomX = roomIndex % 5;
            int roomZ = roomIndex / 5 % 5;
            int roomY = roomIndex / 25;

            if (coordBaseModeIn != EnumFacing.NORTH && coordBaseModeIn != EnumFacing.SOUTH)
            {
                this.boundingBox = new StructureBoundingBox(0, 0, 0, roomZSize * 8 - 1, roomYSize * 4 - 1, roomXSize * 8 - 1);
            }
            else
            {
                this.boundingBox = new StructureBoundingBox(0, 0, 0, roomXSize * 8 - 1, roomYSize * 4 - 1, roomZSize * 8 - 1);
            }

            switch (coordBaseModeIn)
            {
                case NORTH:
                    this.boundingBox.offset(roomX * 8, roomY * 4, -(roomZ + roomZSize) * 8 + 1);
                    break;

                case SOUTH:
                    this.boundingBox.offset(roomX * 8, roomY * 4, roomZ * 8);
                    break;

                case WEST:
                    this.boundingBox.offset(-(roomZ + roomZSize) * 8 + 1, roomY * 4, roomX * 8);
                    break;

                default:
                    this.boundingBox.offset(roomZ * 8, roomY * 4, roomX * 8);
            }
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
        }

        protected void fillWithAirOrWater(World worldIn, StructureBoundingBox boundingBox, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean preserveAir)
        {
            for (int currentY = minY; currentY <= maxY; ++currentY)
            {
                for (int currentX = minX; currentX <= maxX; ++currentX)
                {
                    for (int currentZ = minZ; currentZ <= maxZ; ++currentZ)
                    {
                        if (!preserveAir || this.getBlockStateFromPos(worldIn, currentX, currentY, currentZ, boundingBox).getBlock().getMaterial() != Material.air)
                        {
                            if (this.getYWithOffset(currentY) >= worldIn.getSeaLevel())
                            {
                                this.setBlockState(worldIn, Blocks.air.getDefaultState(), currentX, currentY, currentZ, boundingBox);
                            }
                            else
                            {
                                this.setBlockState(worldIn, WATER, currentX, currentY, currentZ, boundingBox);
                            }
                        }
                    }
                }
            }
        }

        protected void generateRoomFloor(World worldIn, StructureBoundingBox boundingBox, int x, int z, boolean hasOpeningDown)
        {
            if (hasOpeningDown)
            {
                this.fillWithBlocks(worldIn, boundingBox, x + 0, 0, z + 0, x + 2, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 5, 0, z + 0, x + 8 - 1, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 3, 0, z + 0, x + 4, 0, z + 2, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 3, 0, z + 5, x + 4, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 3, 0, z + 2, x + 4, 0, z + 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 3, 0, z + 5, x + 4, 0, z + 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 2, 0, z + 3, x + 2, 0, z + 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, boundingBox, x + 5, 0, z + 3, x + 5, 0, z + 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }
            else
            {
                this.fillWithBlocks(worldIn, boundingBox, x + 0, 0, z + 0, x + 8 - 1, 0, z + 8 - 1, PRISMARINE, PRISMARINE, false);
            }
        }

        protected void replaceWaterWithBlock(World worldIn, StructureBoundingBox boundingBox, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState blockState)
        {
            for (int currentY = minY; currentY <= maxY; ++currentY)
            {
                for (int currentX = minX; currentX <= maxX; ++currentX)
                {
                    for (int currentZ = minZ; currentZ <= maxZ; ++currentZ)
                    {
                        if (this.getBlockStateFromPos(worldIn, currentX, currentY, currentZ, boundingBox) == WATER)
                        {
                            this.setBlockState(worldIn, blockState, currentX, currentY, currentZ, boundingBox);
                        }
                    }
                }
            }
        }

        protected boolean intersectsWith(StructureBoundingBox boundingBox, int minX, int minZ, int maxX, int maxZ)
        {
            int worldMinX = this.getXWithOffset(minX, minZ);
            int worldMinZ = this.getZWithOffset(minX, minZ);
            int worldMaxX = this.getXWithOffset(maxX, maxZ);
            int worldMaxZ = this.getZWithOffset(maxX, maxZ);
            return boundingBox.intersectsWith(Math.min(worldMinX, worldMaxX), Math.min(worldMinZ, worldMaxZ), Math.max(worldMinX, worldMaxX), Math.max(worldMinZ, worldMaxZ));
        }

        protected boolean spawnElderGuardian(World worldIn, StructureBoundingBox boundingBox, int x, int y, int z)
        {
            int guardianX = this.getXWithOffset(x, z);
            int guardianY = this.getYWithOffset(y);
            int guardianZ = this.getZWithOffset(x, z);

            if (boundingBox.isVecInside(new BlockPos(guardianX, guardianY, guardianZ)))
            {
                EntityGuardian entityGuardian = new EntityGuardian(worldIn);
                entityGuardian.setElder(true);
                entityGuardian.heal(entityGuardian.getMaxHealth());
                entityGuardian.setLocationAndAngles((double)guardianX + 0.5D, (double)guardianY, (double)guardianZ + 0.5D, 0.0F, 0.0F);
                entityGuardian.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityGuardian)), (IEntityLivingData)null);
                worldIn.spawnEntityInWorld(entityGuardian);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    static class RoomDefinition
    {
        int index;
        StructureOceanMonumentPieces.RoomDefinition[] connections = new StructureOceanMonumentPieces.RoomDefinition[6];
        boolean[] hasOpening = new boolean[6];
        boolean isClaimed;
        boolean isSource;
        int scanIndex;

        public RoomDefinition(int indexIn)
        {
            this.index = indexIn;
        }

        public void setConnection(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            this.connections[direction.getIndex()] = roomDefinition;
            roomDefinition.connections[direction.getOpposite().getIndex()] = this;
        }

        public void updateOpenings()
        {
            for (int directionIndex = 0; directionIndex < 6; ++directionIndex)
            {
                this.hasOpening[directionIndex] = this.connections[directionIndex] != null;
            }
        }

        public boolean canReachSource(int scanId)
        {
            if (this.isSource)
            {
                return true;
            }
            else
            {
                this.scanIndex = scanId;

                for (int directionIndex = 0; directionIndex < 6; ++directionIndex)
                {
                    if (this.connections[directionIndex] != null && this.hasOpening[directionIndex] && this.connections[directionIndex].scanIndex != scanId && this.connections[directionIndex].canReachSource(scanId))
                    {
                        return true;
                    }
                }

                return false;
            }
        }

        public boolean isSpecial()
        {
            return this.index >= 75;
        }

        public int countOpenings()
        {
            int openingCount = 0;

            for (int directionIndex = 0; directionIndex < 6; ++directionIndex)
            {
                if (this.hasOpening[directionIndex])
                {
                    ++openingCount;
                }
            }

            return openingCount;
        }
    }

    public static class SimpleRoom extends StructureOceanMonumentPieces.Piece
    {
        private int roomType;

        public SimpleRoom()
        {
        }

        public SimpleRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 1, 1, 1);
            this.roomType = randomIn.nextInt(3);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (this.roomDefinition.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 4, 1, 6, 4, 6, PRISMARINE);
            }

            boolean hasCentralDecoration = this.roomType != 0 && randomIn.nextBoolean() && !this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()] && !this.roomDefinition.hasOpening[EnumFacing.UP.getIndex()] && this.roomDefinition.countOpenings() > 1;

            if (this.roomType == 0)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 2, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 2, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 2, 2, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 2, 2, 0, PRISMARINE, PRISMARINE, false);
                this.setBlockState(worldIn, SEA_LANTERN, 1, 2, 1, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 0, 7, 1, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 3, 0, 7, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 0, 7, 2, 2, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 0, 6, 2, 0, PRISMARINE, PRISMARINE, false);
                this.setBlockState(worldIn, SEA_LANTERN, 6, 2, 1, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 5, 2, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 5, 2, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 5, 0, 2, 7, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 7, 2, 2, 7, PRISMARINE, PRISMARINE, false);
                this.setBlockState(worldIn, SEA_LANTERN, 1, 2, 6, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 5, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 3, 5, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 5, 7, 2, 7, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 7, 6, 2, 7, PRISMARINE, PRISMARINE, false);
                this.setBlockState(worldIn, SEA_LANTERN, 6, 2, 6, structureBoundingBoxIn);

                if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 0, 4, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 0, 4, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 0, 4, 2, 0, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 1, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 7, 4, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 6, 4, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 7, 4, 2, 7, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 6, 4, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 3, 0, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 3, 1, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 3, 0, 2, 4, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 3, 1, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 3, 7, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
                else
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 3, 3, 7, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 3, 7, 2, 4, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 3, 7, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
            }
            else if (this.roomType == 1)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 2, 2, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 5, 2, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 5, 5, 3, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 2, 5, 3, 2, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.setBlockState(worldIn, SEA_LANTERN, 2, 2, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 2, 2, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 5, 2, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 5, 2, 2, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 1, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 7, 1, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 6, 0, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 7, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 6, 7, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 0, 7, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 1, 7, 3, 1, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.setBlockState(worldIn, PRISMARINE, 1, 2, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 0, 2, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 1, 2, 7, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 0, 2, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 6, 2, 7, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 7, 2, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 6, 2, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE, 7, 2, 1, structureBoundingBoxIn);

                if (!this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 6, 2, 0, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                if (!this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 7, 6, 2, 7, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                if (!this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 1, 0, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 1, 0, 2, 6, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 1, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                if (!this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()])
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 1, 7, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 1, 7, 2, 6, PRISMARINE, PRISMARINE, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 1, 7, 1, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }
            }
            else if (this.roomType == 2)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);

                if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()])
                {
                    this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()])
                {
                    this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 7, 4, 2, 7, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()])
                {
                    this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, false);
                }

                if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()])
                {
                    this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 7, 1, 3, 7, 2, 4, false);
                }
            }

            if (hasCentralDecoration)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 3, 4, 1, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 2, 3, 4, 2, 4, PRISMARINE, PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 3, 3, 4, 3, 4, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            }

            return true;
        }
    }

    public static class SimpleTopRoom extends StructureOceanMonumentPieces.Piece
    {
        public SimpleTopRoom()
        {
        }

        public SimpleTopRoom(EnumFacing coordBaseModeIn, StructureOceanMonumentPieces.RoomDefinition roomDefinitionIn, Random randomIn)
        {
            super(1, coordBaseModeIn, roomDefinitionIn, 1, 1, 1);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.roomDefinition.index / 25 > 0)
            {
                this.generateRoomFloor(worldIn, structureBoundingBoxIn, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
            }

            if (this.roomDefinition.connections[EnumFacing.UP.getIndex()] == null)
            {
                this.replaceWaterWithBlock(worldIn, structureBoundingBoxIn, 1, 4, 1, 6, 4, 6, PRISMARINE);
            }

            for (int spongeX = 1; spongeX <= 6; ++spongeX)
            {
                for (int spongeZ = 1; spongeZ <= 6; ++spongeZ)
                {
                    if (randomIn.nextInt(3) != 0)
                    {
                        int spongeY = 2 + (randomIn.nextInt(4) == 0 ? 0 : 1);
                        this.fillWithBlocks(worldIn, structureBoundingBoxIn, spongeX, spongeY, spongeZ, spongeX, 3, spongeZ, Blocks.sponge.getStateFromMeta(1), Blocks.sponge.getStateFromMeta(1), false);
                    }
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 0, 7, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 6, 1, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 7, 6, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 3, 0, 0, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 0, 7, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 0, 6, 3, 0, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 7, 6, 3, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);

            if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()])
            {
                this.fillWithAirOrWater(worldIn, structureBoundingBoxIn, 3, 1, 0, 4, 2, 0, false);
            }

            return true;
        }
    }

    public static class WingRoom extends StructureOceanMonumentPieces.Piece
    {
        private int wingSide;

        public WingRoom()
        {
        }

        public WingRoom(EnumFacing coordBaseModeIn, StructureBoundingBox boundingBoxIn, int wingSideIn)
        {
            super(coordBaseModeIn, boundingBoxIn);
            this.wingSide = wingSideIn & 1;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.wingSide == 0)
            {
                for (int archStep = 0; archStep < 4; ++archStep)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10 - archStep, 3 - archStep, 20 - archStep, 12 + archStep, 3 - archStep, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 0, 6, 15, 0, 16, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 6, 6, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 16, 0, 6, 16, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 7, 7, 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 1, 7, 15, 1, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 1, 6, 9, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 1, 6, 15, 3, 6, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 7, 9, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 1, 7, 14, 1, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 5, 13, 0, 5, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 0, 7, 12, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 10, 8, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, 0, 10, 14, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);

                for (int lanternZ = 18; lanternZ >= 7; lanternZ -= 3)
                {
                    this.setBlockState(worldIn, SEA_LANTERN, 6, 3, lanternZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, SEA_LANTERN, 16, 3, lanternZ, structureBoundingBoxIn);
                }

                this.setBlockState(worldIn, SEA_LANTERN, 10, 0, 10, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 12, 0, 10, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 10, 0, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 12, 0, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 8, 3, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 14, 3, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 4, 2, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 4, 1, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 4, 0, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 18, 2, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 18, 1, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 18, 0, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 4, 2, 18, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 4, 1, 18, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 4, 0, 18, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 18, 2, 18, structureBoundingBoxIn);
                this.setBlockState(worldIn, SEA_LANTERN, 18, 1, 18, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 18, 0, 18, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 9, 7, 20, structureBoundingBoxIn);
                this.setBlockState(worldIn, PRISMARINE_BRICKS, 13, 7, 20, structureBoundingBoxIn);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 21, 7, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 15, 0, 21, 16, 4, 21, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.spawnElderGuardian(worldIn, structureBoundingBoxIn, 11, 2, 16);
            }
            else if (this.wingSide == 1)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 3, 18, 13, 3, 20, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 18, 9, 2, 18, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 13, 0, 18, 13, 2, 18, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                int pillarX = 9;
                int pillarZ = 20;
                int pillarY = 5;

                for (int pillarIndex = 0; pillarIndex < 2; ++pillarIndex)
                {
                    this.setBlockState(worldIn, PRISMARINE_BRICKS, pillarX, pillarY + 1, pillarZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, SEA_LANTERN, pillarX, pillarY, pillarZ, structureBoundingBoxIn);
                    this.setBlockState(worldIn, PRISMARINE_BRICKS, pillarX, pillarY - 1, pillarZ, structureBoundingBoxIn);
                    pillarX = 13;
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 3, 7, 15, 3, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                pillarX = 10;

                for (int centralColumnIndex = 0; centralColumnIndex < 2; ++centralColumnIndex)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, pillarX, 0, 10, pillarX, 6, 10, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, pillarX, 0, 12, pillarX, 6, 12, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.setBlockState(worldIn, SEA_LANTERN, pillarX, 0, 10, structureBoundingBoxIn);
                    this.setBlockState(worldIn, SEA_LANTERN, pillarX, 0, 12, structureBoundingBoxIn);
                    this.setBlockState(worldIn, SEA_LANTERN, pillarX, 4, 10, structureBoundingBoxIn);
                    this.setBlockState(worldIn, SEA_LANTERN, pillarX, 4, 12, structureBoundingBoxIn);
                    pillarX = 12;
                }

                pillarX = 8;

                for (int sideColumnIndex = 0; sideColumnIndex < 2; ++sideColumnIndex)
                {
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, pillarX, 0, 7, pillarX, 2, 7, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    this.fillWithBlocks(worldIn, structureBoundingBoxIn, pillarX, 0, 14, pillarX, 2, 14, PRISMARINE_BRICKS, PRISMARINE_BRICKS, false);
                    pillarX = 14;
                }

                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 3, 8, 8, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 14, 3, 8, 14, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
                this.spawnElderGuardian(worldIn, structureBoundingBoxIn, 11, 5, 13);
            }

            return true;
        }
    }

    static class XDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private XDoubleRoomFitHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            return roomDefinition.hasOpening[EnumFacing.EAST.getIndex()] && !roomDefinition.connections[EnumFacing.EAST.getIndex()].isClaimed;
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            roomDefinition.isClaimed = true;
            roomDefinition.connections[EnumFacing.EAST.getIndex()].isClaimed = true;
            return new StructureOceanMonumentPieces.DoubleXRoom(direction, roomDefinition, random);
        }
    }

    static class XYDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private XYDoubleRoomFitHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            if (roomDefinition.hasOpening[EnumFacing.EAST.getIndex()] && !roomDefinition.connections[EnumFacing.EAST.getIndex()].isClaimed && roomDefinition.hasOpening[EnumFacing.UP.getIndex()] && !roomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed)
            {
                StructureOceanMonumentPieces.RoomDefinition eastRoom = roomDefinition.connections[EnumFacing.EAST.getIndex()];
                return eastRoom.hasOpening[EnumFacing.UP.getIndex()] && !eastRoom.connections[EnumFacing.UP.getIndex()].isClaimed;
            }
            else
            {
                return false;
            }
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            roomDefinition.isClaimed = true;
            roomDefinition.connections[EnumFacing.EAST.getIndex()].isClaimed = true;
            roomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed = true;
            roomDefinition.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.UP.getIndex()].isClaimed = true;
            return new StructureOceanMonumentPieces.DoubleXYRoom(direction, roomDefinition, random);
        }
    }

    static class YDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private YDoubleRoomFitHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            return roomDefinition.hasOpening[EnumFacing.UP.getIndex()] && !roomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed;
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            roomDefinition.isClaimed = true;
            roomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed = true;
            return new StructureOceanMonumentPieces.DoubleYRoom(direction, roomDefinition, random);
        }
    }

    static class YZDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private YZDoubleRoomFitHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            if (roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()] && !roomDefinition.connections[EnumFacing.NORTH.getIndex()].isClaimed && roomDefinition.hasOpening[EnumFacing.UP.getIndex()] && !roomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed)
            {
                StructureOceanMonumentPieces.RoomDefinition northRoom = roomDefinition.connections[EnumFacing.NORTH.getIndex()];
                return northRoom.hasOpening[EnumFacing.UP.getIndex()] && !northRoom.connections[EnumFacing.UP.getIndex()].isClaimed;
            }
            else
            {
                return false;
            }
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            roomDefinition.isClaimed = true;
            roomDefinition.connections[EnumFacing.NORTH.getIndex()].isClaimed = true;
            roomDefinition.connections[EnumFacing.UP.getIndex()].isClaimed = true;
            roomDefinition.connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].isClaimed = true;
            return new StructureOceanMonumentPieces.DoubleYZRoom(direction, roomDefinition, random);
        }
    }

    static class ZDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper
    {
        private ZDoubleRoomFitHelper()
        {
        }

        public boolean fits(StructureOceanMonumentPieces.RoomDefinition roomDefinition)
        {
            return roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()] && !roomDefinition.connections[EnumFacing.NORTH.getIndex()].isClaimed;
        }

        public StructureOceanMonumentPieces.Piece create(EnumFacing direction, StructureOceanMonumentPieces.RoomDefinition roomDefinition, Random random)
        {
            StructureOceanMonumentPieces.RoomDefinition selectedRoom = roomDefinition;

            if (!roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()] || roomDefinition.connections[EnumFacing.NORTH.getIndex()].isClaimed)
            {
                selectedRoom = roomDefinition.connections[EnumFacing.SOUTH.getIndex()];
            }

            selectedRoom.isClaimed = true;
            selectedRoom.connections[EnumFacing.NORTH.getIndex()].isClaimed = true;
            return new StructureOceanMonumentPieces.DoubleZRoom(direction, selectedRoom, random);
        }
    }
}
