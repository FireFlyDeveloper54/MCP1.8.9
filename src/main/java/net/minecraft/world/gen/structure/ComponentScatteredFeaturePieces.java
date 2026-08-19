package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

public class ComponentScatteredFeaturePieces
{
    public static void registerScatteredFeaturePieces()
    {
        MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.DesertPyramid.class, "TeDP");
        MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.JunglePyramid.class, "TeJP");
        MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.SwampHut.class, "TeSH");
    }

    public static class DesertPyramid extends ComponentScatteredFeaturePieces.Feature
    {
        private boolean[] hasPlacedChest = new boolean[4];
        private static final List<WeightedRandomChestContent> itemsToGenerateInTemple = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 3), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 2, 7, 15), new WeightedRandomChestContent(Items.emerald, 0, 1, 3, 2), new WeightedRandomChestContent(Items.bone, 0, 4, 6, 20), new WeightedRandomChestContent(Items.rotten_flesh, 0, 3, 7, 16), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 3), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1)});

        public DesertPyramid()
        {
        }

        public DesertPyramid(Random rand, int x, int z)
        {
            super(rand, x, 64, z, 21, 15, 21);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
            tagCompound.setBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
            tagCompound.setBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
            tagCompound.setBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasPlacedChest[0] = tagCompound.getBoolean("hasPlacedChest0");
            this.hasPlacedChest[1] = tagCompound.getBoolean("hasPlacedChest1");
            this.hasPlacedChest[2] = tagCompound.getBoolean("hasPlacedChest2");
            this.hasPlacedChest[3] = tagCompound.getBoolean("hasPlacedChest3");
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);

            for (int pyramidLayer = 1; pyramidLayer <= 9; ++pyramidLayer)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, pyramidLayer, pyramidLayer, pyramidLayer, this.scatteredFeatureSizeX - 1 - pyramidLayer, pyramidLayer, this.scatteredFeatureSizeZ - 1 - pyramidLayer, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, pyramidLayer + 1, pyramidLayer, pyramidLayer + 1, this.scatteredFeatureSizeX - 2 - pyramidLayer, pyramidLayer, this.scatteredFeatureSizeZ - 2 - pyramidLayer, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            }

            for (int localX = 0; localX < this.scatteredFeatureSizeX; ++localX)
            {
                for (int localZ = 0; localZ < this.scatteredFeatureSizeZ; ++localZ)
                {
                    int foundationY = -5;
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.sandstone.getDefaultState(), localX, foundationY, localZ, structureBoundingBoxIn);
                }
            }

            int northStairMeta = this.getMetadataWithOffset(Blocks.sandstone_stairs, 3);
            int southStairMeta = this.getMetadataWithOffset(Blocks.sandstone_stairs, 2);
            int westStairMeta = this.getMetadataWithOffset(Blocks.sandstone_stairs, 0);
            int eastStairMeta = this.getMetadataWithOffset(Blocks.sandstone_stairs, 1);
            int orangeClayMeta = ~EnumDyeColor.ORANGE.getDyeDamage() & 15;
            int blueClayMeta = ~EnumDyeColor.BLUE.getDyeDamage() & 15;
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 9, 4, Blocks.sandstone.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 10, 1, 3, 10, 3, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(northStairMeta), 2, 10, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(southStairMeta), 2, 10, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(westStairMeta), 0, 10, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(eastStairMeta), 4, 10, 2, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 5, 0, 0, this.scatteredFeatureSizeX - 1, 9, 4, Blocks.sandstone.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 4, 10, 1, this.scatteredFeatureSizeX - 2, 10, 3, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(northStairMeta), this.scatteredFeatureSizeX - 3, 10, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(southStairMeta), this.scatteredFeatureSizeX - 3, 10, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(westStairMeta), this.scatteredFeatureSizeX - 5, 10, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(eastStairMeta), this.scatteredFeatureSizeX - 1, 10, 2, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 0, 12, 4, 4, Blocks.sandstone.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, 0, 11, 3, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 1, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 2, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 3, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, 3, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 3, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 2, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 1, 1, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 8, 3, 3, Blocks.sandstone.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 2, 8, 2, 2, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, 1, 16, 3, 3, Blocks.sandstone.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, 2, 16, 2, 2, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 4, 5, this.scatteredFeatureSizeX - 6, 4, this.scatteredFeatureSizeZ - 6, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 4, 9, 11, 4, 11, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 8, 8, 3, 8, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, 8, 12, 3, 8, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 12, 8, 3, 12, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 1, 12, 12, 3, 12, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 5, 4, 4, 11, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 5, 1, 5, this.scatteredFeatureSizeX - 2, 4, 11, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 7, 9, 6, 7, 11, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 7, 7, 9, this.scatteredFeatureSizeX - 7, 7, 11, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 5, 9, 5, 7, 11, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 6, 5, 9, this.scatteredFeatureSizeX - 6, 7, 11, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 5, 5, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 5, 6, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 6, 6, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), this.scatteredFeatureSizeX - 6, 5, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), this.scatteredFeatureSizeX - 6, 6, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), this.scatteredFeatureSizeX - 7, 6, 10, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 4, 4, 2, 6, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 3, 4, 4, this.scatteredFeatureSizeX - 3, 6, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(northStairMeta), 2, 4, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(northStairMeta), 2, 3, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(northStairMeta), this.scatteredFeatureSizeX - 3, 4, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(northStairMeta), this.scatteredFeatureSizeX - 3, 3, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 3, 2, 2, 3, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 3, 1, 3, this.scatteredFeatureSizeX - 2, 2, 3, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getDefaultState(), 1, 1, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getDefaultState(), this.scatteredFeatureSizeX - 2, 1, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SAND.getMetadata()), 1, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_slab.getStateFromMeta(BlockStoneSlab.EnumType.SAND.getMetadata()), this.scatteredFeatureSizeX - 2, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(eastStairMeta), 2, 1, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone_stairs.getStateFromMeta(westStairMeta), this.scatteredFeatureSizeX - 3, 1, 2, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 3, 5, 4, 3, 18, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 5, 3, 5, this.scatteredFeatureSizeX - 5, 3, 17, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 5, 4, 2, 16, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, this.scatteredFeatureSizeX - 6, 1, 5, this.scatteredFeatureSizeX - 5, 2, 16, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

            for (int pillarZ = 5; pillarZ <= 17; pillarZ += 2)
            {
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 4, 1, pillarZ, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 4, 2, pillarZ, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), this.scatteredFeatureSizeX - 5, 1, pillarZ, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), this.scatteredFeatureSizeX - 5, 2, pillarZ, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 10, 0, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 10, 0, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 9, 0, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 11, 0, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 8, 0, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 12, 0, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 7, 0, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 13, 0, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 9, 0, 11, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 11, 0, 11, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 10, 0, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 10, 0, 13, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(blueClayMeta), 10, 0, 10, structureBoundingBoxIn);

            for (int sideWallX = 0; sideWallX <= this.scatteredFeatureSizeX - 1; sideWallX += this.scatteredFeatureSizeX - 1)
            {
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 2, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 2, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 2, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 3, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 3, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 4, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), sideWallX, 4, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 4, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 5, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 5, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 5, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 6, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), sideWallX, 6, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 6, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 7, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 7, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), sideWallX, 7, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 8, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 8, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), sideWallX, 8, 3, structureBoundingBoxIn);
            }

            for (int frontColumnX = 2; frontColumnX <= this.scatteredFeatureSizeX - 3; frontColumnX += this.scatteredFeatureSizeX - 3 - 2)
            {
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX - 1, 2, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX, 2, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX + 1, 2, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX - 1, 3, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX, 3, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX + 1, 3, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX - 1, 4, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), frontColumnX, 4, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX + 1, 4, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX - 1, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX + 1, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX - 1, 6, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), frontColumnX, 6, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX + 1, 6, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX - 1, 7, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX, 7, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), frontColumnX + 1, 7, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX - 1, 8, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX, 8, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), frontColumnX + 1, 8, 0, structureBoundingBoxIn);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 4, 0, 12, 6, 0, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 8, 6, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 12, 6, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 9, 5, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, 5, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stained_hardened_clay.getStateFromMeta(orangeClayMeta), 11, 5, 0, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, -14, 8, 12, -11, 12, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, -10, 8, 12, -10, 12, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, -9, 8, 12, -9, 12, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, -8, 8, 12, -1, 12, Blocks.sandstone.getDefaultState(), Blocks.sandstone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, -11, 9, 11, -1, 11, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.stone_pressure_plate.getDefaultState(), 10, -11, 10, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, -13, 9, 11, -13, 11, Blocks.tnt.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 8, -11, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 8, -10, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 7, -10, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 7, -11, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 12, -11, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 12, -10, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 13, -10, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 13, -11, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 10, -11, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 10, -10, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, -10, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, -11, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 10, -11, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 10, -10, 12, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, -10, 13, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, -11, 13, structureBoundingBoxIn);

            for (EnumFacing chestFacing : EnumFacing.Plane.HORIZONTAL)
            {
                if (!this.hasPlacedChest[chestFacing.getHorizontalIndex()])
                {
                    int chestOffsetX = chestFacing.getFrontOffsetX() * 2;
                    int chestOffsetZ = chestFacing.getFrontOffsetZ() * 2;
                    this.hasPlacedChest[chestFacing.getHorizontalIndex()] = this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 10 + chestOffsetX, -11, 10 + chestOffsetZ, WeightedRandomChestContent.addRandomChestContents(itemsToGenerateInTemple, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 2 + randomIn.nextInt(5));
                }
            }

            return true;
        }
    }

    abstract static class Feature extends StructureComponent
    {
        protected int scatteredFeatureSizeX;
        protected int scatteredFeatureSizeY;
        protected int scatteredFeatureSizeZ;
        protected int averageGroundLevel = -1;

        public Feature()
        {
        }

        protected Feature(Random rand, int x, int y, int z, int sizeX, int sizeY, int sizeZ)
        {
            super(0);
            this.scatteredFeatureSizeX = sizeX;
            this.scatteredFeatureSizeY = sizeY;
            this.scatteredFeatureSizeZ = sizeZ;
            this.coordBaseMode = EnumFacing.Plane.HORIZONTAL.random(rand);

            switch (this.coordBaseMode)
            {
                case NORTH:
                case SOUTH:
                    this.boundingBox = new StructureBoundingBox(x, y, z, x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1);
                    break;

                default:
                    this.boundingBox = new StructureBoundingBox(x, y, z, x + sizeZ - 1, y + sizeY - 1, z + sizeX - 1);
            }
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            tagCompound.setInteger("Width", this.scatteredFeatureSizeX);
            tagCompound.setInteger("Height", this.scatteredFeatureSizeY);
            tagCompound.setInteger("Depth", this.scatteredFeatureSizeZ);
            tagCompound.setInteger("HPos", this.averageGroundLevel);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            this.scatteredFeatureSizeX = tagCompound.getInteger("Width");
            this.scatteredFeatureSizeY = tagCompound.getInteger("Height");
            this.scatteredFeatureSizeZ = tagCompound.getInteger("Depth");
            this.averageGroundLevel = tagCompound.getInteger("HPos");
        }

        protected boolean offsetToAverageGroundLevel(World worldIn, StructureBoundingBox structurebb, int yOffset)
        {
            if (this.averageGroundLevel >= 0)
            {
                return true;
            }
            else
            {
                int totalGroundY = 0;
                int blockCount = 0;
                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

                for (int z = this.boundingBox.minZ; z <= this.boundingBox.maxZ; ++z)
                {
                    for (int x = this.boundingBox.minX; x <= this.boundingBox.maxX; ++x)
                    {
                        mutablePos.set(x, 64, z);

                        if (structurebb.isVecInside(mutablePos))
                        {
                            totalGroundY += Math.max(worldIn.getTopSolidOrLiquidBlock(mutablePos).getY(), worldIn.provider.getAverageGroundLevel());
                            ++blockCount;
                        }
                    }
                }

                if (blockCount == 0)
                {
                    return false;
                }
                else
                {
                    this.averageGroundLevel = totalGroundY / blockCount;
                    this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.minY + yOffset, 0);
                    return true;
                }
            }
        }
    }

    public static class JunglePyramid extends ComponentScatteredFeaturePieces.Feature
    {
        private boolean placedMainChest;
        private boolean placedHiddenChest;
        private boolean placedTrap1;
        private boolean placedTrap2;
        private static final List<WeightedRandomChestContent> JUNGLE_TEMPLE_CHEST_CONTENTS = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 3), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 2, 7, 15), new WeightedRandomChestContent(Items.emerald, 0, 1, 3, 2), new WeightedRandomChestContent(Items.bone, 0, 4, 6, 20), new WeightedRandomChestContent(Items.rotten_flesh, 0, 3, 7, 16), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 3), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1)});
        private static final List<WeightedRandomChestContent> JUNGLE_TEMPLE_DISPENSER_CONTENTS = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.arrow, 0, 2, 7, 30)});
        private static ComponentScatteredFeaturePieces.JunglePyramid.Stones junglePyramidsRandomScatteredStones = new ComponentScatteredFeaturePieces.JunglePyramid.Stones();

        public JunglePyramid()
        {
        }

        public JunglePyramid(Random rand, int x, int z)
        {
            super(rand, x, 64, z, 12, 10, 15);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("placedMainChest", this.placedMainChest);
            tagCompound.setBoolean("placedHiddenChest", this.placedHiddenChest);
            tagCompound.setBoolean("placedTrap1", this.placedTrap1);
            tagCompound.setBoolean("placedTrap2", this.placedTrap2);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.placedMainChest = tagCompound.getBoolean("placedMainChest");
            this.placedHiddenChest = tagCompound.getBoolean("placedHiddenChest");
            this.placedTrap1 = tagCompound.getBoolean("placedTrap1");
            this.placedTrap2 = tagCompound.getBoolean("placedTrap2");
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (!this.offsetToAverageGroundLevel(worldIn, structureBoundingBoxIn, 0))
            {
                return false;
            }
            else
            {
                int northStairMeta = this.getMetadataWithOffset(Blocks.stone_stairs, 3);
                int southStairMeta = this.getMetadataWithOffset(Blocks.stone_stairs, 2);
                int westStairMeta = this.getMetadataWithOffset(Blocks.stone_stairs, 0);
                int eastStairMeta = this.getMetadataWithOffset(Blocks.stone_stairs, 1);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 1, 2, 9, 2, 2, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 1, 12, 9, 2, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 1, 3, 2, 2, 11, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 9, 1, 3, 9, 2, 11, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 3, 1, 10, 6, 1, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 3, 13, 10, 6, 13, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, 3, 2, 1, 6, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 10, 3, 2, 10, 6, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 3, 2, 9, 3, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 6, 2, 9, 6, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 3, 7, 3, 8, 7, 11, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 8, 4, 7, 8, 10, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 3, 1, 3, 8, 2, 11);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 4, 3, 6, 7, 3, 9);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 2, 4, 2, 9, 5, 12);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 4, 6, 5, 7, 6, 9);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 5, 7, 6, 6, 7, 8);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 5, 1, 2, 6, 2, 2);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 5, 2, 12, 6, 2, 12);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 5, 5, 1, 6, 5, 1);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 5, 5, 13, 6, 5, 13);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 5, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 10, 5, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 5, 9, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 10, 5, 9, structureBoundingBoxIn);

                for (int edgeZ = 0; edgeZ <= 14; edgeZ += 14)
                {
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 4, edgeZ, 2, 5, edgeZ, false, randomIn, junglePyramidsRandomScatteredStones);
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 4, edgeZ, 4, 5, edgeZ, false, randomIn, junglePyramidsRandomScatteredStones);
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 7, 4, edgeZ, 7, 5, edgeZ, false, randomIn, junglePyramidsRandomScatteredStones);
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 9, 4, edgeZ, 9, 5, edgeZ, false, randomIn, junglePyramidsRandomScatteredStones);
                }

                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 5, 6, 0, 6, 6, 0, false, randomIn, junglePyramidsRandomScatteredStones);

                for (int edgeX = 0; edgeX <= 11; edgeX += 11)
                {
                    for (int postZ = 2; postZ <= 12; postZ += 2)
                    {
                        this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, edgeX, 4, postZ, edgeX, 5, postZ, false, randomIn, junglePyramidsRandomScatteredStones);
                    }

                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, edgeX, 6, 5, edgeX, 6, 5, false, randomIn, junglePyramidsRandomScatteredStones);
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, edgeX, 6, 9, edgeX, 6, 9, false, randomIn, junglePyramidsRandomScatteredStones);
                }

                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 7, 2, 2, 9, 2, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 9, 7, 2, 9, 9, 2, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, 7, 12, 2, 9, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 9, 7, 12, 9, 9, 12, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 9, 4, 4, 9, 4, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 7, 9, 4, 7, 9, 4, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 9, 10, 4, 9, 10, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 7, 9, 10, 7, 9, 10, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 5, 9, 7, 6, 9, 7, false, randomIn, junglePyramidsRandomScatteredStones);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 5, 9, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 6, 9, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(southStairMeta), 5, 9, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(southStairMeta), 6, 9, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 4, 0, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 5, 0, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 6, 0, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 7, 0, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 4, 1, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 4, 2, 9, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 4, 3, 10, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 7, 1, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 7, 2, 9, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(northStairMeta), 7, 3, 10, structureBoundingBoxIn);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 9, 4, 1, 9, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 7, 1, 9, 7, 1, 9, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 4, 1, 10, 7, 2, 10, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 5, 4, 5, 6, 4, 5, false, randomIn, junglePyramidsRandomScatteredStones);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(westStairMeta), 4, 4, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(eastStairMeta), 7, 4, 5, structureBoundingBoxIn);

                for (int stairStep = 0; stairStep < 4; ++stairStep)
                {
                    this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(southStairMeta), 5, 0 - stairStep, 6 + stairStep, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(southStairMeta), 6, 0 - stairStep, 6 + stairStep, structureBoundingBoxIn);
                    this.fillWithAir(worldIn, structureBoundingBoxIn, 5, 0 - stairStep, 7 + stairStep, 6, 0 - stairStep, 9 + stairStep);
                }

                this.fillWithAir(worldIn, structureBoundingBoxIn, 1, -3, 12, 10, -1, 13);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 1, -3, 1, 3, -1, 13);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 1, -3, 1, 9, -1, 5);

                for (int trapPillarZ = 1; trapPillarZ <= 13; trapPillarZ += 2)
                {
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, -3, trapPillarZ, 1, -2, trapPillarZ, false, randomIn, junglePyramidsRandomScatteredStones);
                }

                for (int trapFloorZ = 2; trapFloorZ <= 12; trapFloorZ += 2)
                {
                    this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 1, -1, trapFloorZ, 3, -1, trapFloorZ, false, randomIn, junglePyramidsRandomScatteredStones);
                }

                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 2, -2, 1, 5, -2, 1, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 7, -2, 1, 9, -2, 1, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 6, -3, 1, 6, -3, 1, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 6, -1, 1, 6, -1, 1, false, randomIn, junglePyramidsRandomScatteredStones);
                this.setBlockState(worldIn, Blocks.tripwire_hook.getStateFromMeta(this.getMetadataWithOffset(Blocks.tripwire_hook, EnumFacing.EAST.getHorizontalIndex())).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 1, -3, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire_hook.getStateFromMeta(this.getMetadataWithOffset(Blocks.tripwire_hook, EnumFacing.WEST.getHorizontalIndex())).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 4, -3, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 2, -3, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 3, -3, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 7, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 5, -3, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 4, -3, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 3, -3, 1, structureBoundingBoxIn);

                if (!this.placedTrap1)
                {
                    this.placedTrap1 = this.generateDispenserContents(worldIn, structureBoundingBoxIn, randomIn, 3, -2, 1, EnumFacing.NORTH.getIndex(), JUNGLE_TEMPLE_DISPENSER_CONTENTS, 2);
                }

                this.setBlockState(worldIn, Blocks.vine.getStateFromMeta(15), 3, -2, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire_hook.getStateFromMeta(this.getMetadataWithOffset(Blocks.tripwire_hook, EnumFacing.NORTH.getHorizontalIndex())).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 7, -3, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire_hook.getStateFromMeta(this.getMetadataWithOffset(Blocks.tripwire_hook, EnumFacing.SOUTH.getHorizontalIndex())).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 7, -3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.tripwire.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 8, -3, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 9, -3, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 9, -3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 9, -3, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 9, -2, 4, structureBoundingBoxIn);

                if (!this.placedTrap2)
                {
                    this.placedTrap2 = this.generateDispenserContents(worldIn, structureBoundingBoxIn, randomIn, 9, -2, 3, EnumFacing.WEST.getIndex(), JUNGLE_TEMPLE_DISPENSER_CONTENTS, 2);
                }

                this.setBlockState(worldIn, Blocks.vine.getStateFromMeta(15), 8, -1, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.vine.getStateFromMeta(15), 8, -2, 3, structureBoundingBoxIn);

                if (!this.placedMainChest)
                {
                    this.placedMainChest = this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 8, -3, 3, WeightedRandomChestContent.addRandomChestContents(JUNGLE_TEMPLE_CHEST_CONTENTS, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 2 + randomIn.nextInt(5));
                }

                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 9, -3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 8, -3, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 4, -3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 5, -2, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 5, -1, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 6, -3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 7, -2, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 7, -1, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 8, -3, 5, structureBoundingBoxIn);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 9, -1, 1, 9, -1, 5, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithAir(worldIn, structureBoundingBoxIn, 8, -3, 8, 10, -1, 10);
                this.setBlockState(worldIn, Blocks.stonebrick.getStateFromMeta(BlockStoneBrick.CHISELED_META), 8, -2, 11, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getStateFromMeta(BlockStoneBrick.CHISELED_META), 9, -2, 11, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.stonebrick.getStateFromMeta(BlockStoneBrick.CHISELED_META), 10, -2, 11, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.lever.getStateFromMeta(BlockLever.getMetadataForFacing(EnumFacing.getFront(this.getMetadataWithOffset(Blocks.lever, EnumFacing.NORTH.getIndex())))), 8, -2, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.lever.getStateFromMeta(BlockLever.getMetadataForFacing(EnumFacing.getFront(this.getMetadataWithOffset(Blocks.lever, EnumFacing.NORTH.getIndex())))), 9, -2, 12, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.lever.getStateFromMeta(BlockLever.getMetadataForFacing(EnumFacing.getFront(this.getMetadataWithOffset(Blocks.lever, EnumFacing.NORTH.getIndex())))), 10, -2, 12, structureBoundingBoxIn);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 8, -3, 8, 8, -3, 10, false, randomIn, junglePyramidsRandomScatteredStones);
                this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 10, -3, 8, 10, -3, 10, false, randomIn, junglePyramidsRandomScatteredStones);
                this.setBlockState(worldIn, Blocks.mossy_cobblestone.getDefaultState(), 10, -2, 9, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 8, -2, 9, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 8, -2, 10, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.redstone_wire.getDefaultState(), 10, -1, 9, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sticky_piston.getStateFromMeta(EnumFacing.UP.getIndex()), 9, -2, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sticky_piston.getStateFromMeta(this.getMetadataWithOffset(Blocks.sticky_piston, EnumFacing.WEST.getIndex())), 10, -2, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.sticky_piston.getStateFromMeta(this.getMetadataWithOffset(Blocks.sticky_piston, EnumFacing.WEST.getIndex())), 10, -1, 8, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.unpowered_repeater.getStateFromMeta(this.getMetadataWithOffset(Blocks.unpowered_repeater, EnumFacing.NORTH.getHorizontalIndex())), 10, -2, 10, structureBoundingBoxIn);

                if (!this.placedHiddenChest)
                {
                    this.placedHiddenChest = this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 9, -3, 10, WeightedRandomChestContent.addRandomChestContents(JUNGLE_TEMPLE_CHEST_CONTENTS, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(randomIn)}), 2 + randomIn.nextInt(5));
                }

                return true;
            }
        }

        static class Stones extends StructureComponent.BlockSelector
        {
            private Stones()
            {
            }

            public void selectBlocks(Random rand, int x, int y, int z, boolean isBoundary)
            {
                if (rand.nextFloat() < 0.4F)
                {
                    this.blockstate = Blocks.cobblestone.getDefaultState();
                }
                else
                {
                    this.blockstate = Blocks.mossy_cobblestone.getDefaultState();
                }
            }
        }
    }

    public static class SwampHut extends ComponentScatteredFeaturePieces.Feature
    {
        private boolean hasWitch;

        public SwampHut()
        {
        }

        public SwampHut(Random rand, int x, int z)
        {
            super(rand, x, 64, z, 7, 7, 9);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Witch", this.hasWitch);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.hasWitch = tagCompound.getBoolean("Witch");
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (!this.offsetToAverageGroundLevel(worldIn, structureBoundingBoxIn, 0))
            {
                return false;
            }
            else
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 5, 1, 7, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 2, 5, 4, 7, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 0, 4, 1, 0, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 2, 3, 3, 2, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 3, 1, 3, 6, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 2, 3, 5, 3, 6, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 2, 7, 4, 3, 7, Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.planks.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 2, 1, 3, 2, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 0, 2, 5, 3, 2, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 7, 1, 3, 7, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 0, 7, 5, 3, 7, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 3, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 3, 3, 7, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 3, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 5, 3, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.air.getDefaultState(), 5, 3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.flower_pot.getDefaultState().withProperty(BlockFlowerPot.CONTENTS, BlockFlowerPot.EnumFlowerType.MUSHROOM_RED), 1, 3, 5, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.crafting_table.getDefaultState(), 3, 2, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.cauldron.getDefaultState(), 4, 2, 6, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 2, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 5, 2, 1, structureBoundingBoxIn);
                int northRoofStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
                int eastRoofStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 1);
                int westRoofStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 0);
                int southRoofStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 2);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 1, 6, 4, 1, Blocks.spruce_stairs.getStateFromMeta(northRoofStairMeta), Blocks.spruce_stairs.getStateFromMeta(northRoofStairMeta), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 2, 0, 4, 7, Blocks.spruce_stairs.getStateFromMeta(westRoofStairMeta), Blocks.spruce_stairs.getStateFromMeta(westRoofStairMeta), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 4, 2, 6, 4, 7, Blocks.spruce_stairs.getStateFromMeta(eastRoofStairMeta), Blocks.spruce_stairs.getStateFromMeta(eastRoofStairMeta), false);
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 8, 6, 4, 8, Blocks.spruce_stairs.getStateFromMeta(southRoofStairMeta), Blocks.spruce_stairs.getStateFromMeta(southRoofStairMeta), false);

                for (int supportZ = 2; supportZ <= 7; supportZ += 5)
                {
                    for (int supportX = 1; supportX <= 5; supportX += 4)
                    {
                        this.replaceAirAndLiquidDownwards(worldIn, Blocks.log.getDefaultState(), supportX, -1, supportZ, structureBoundingBoxIn);
                    }
                }

                if (!this.hasWitch)
                {
                    int witchX = this.getXWithOffset(2, 5);
                    int witchY = this.getYWithOffset(2);
                    int witchZ = this.getZWithOffset(2, 5);

                    if (structureBoundingBoxIn.isVecInside(new BlockPos(witchX, witchY, witchZ)))
                    {
                        this.hasWitch = true;
                        EntityWitch entityWitch = new EntityWitch(worldIn);
                        entityWitch.setLocationAndAngles((double)witchX + 0.5D, (double)witchY, (double)witchZ + 0.5D, 0.0F, 0.0F);
                        entityWitch.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(witchX, witchY, witchZ)), (IEntityLivingData)null);
                        worldIn.spawnEntityInWorld(entityWitch);
                    }
                }

                return true;
            }
        }
    }
}
