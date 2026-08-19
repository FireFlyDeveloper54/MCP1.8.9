package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

@SuppressWarnings("incomplete-switch")
public class StructureVillagePieces
{
    public static void registerVillagePieces()
    {
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House1.class, "ViBH");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Field1.class, "ViDF");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Field2.class, "ViF");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Torch.class, "ViL");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Hall.class, "ViPH");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House4Garden.class, "ViSH");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.WoodHut.class, "ViSmH");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Church.class, "ViST");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House2.class, "ViS");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Start.class, "ViStart");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Path.class, "ViSR");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House3.class, "ViTRH");
        MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Well.class, "ViW");
    }

    public static List<StructureVillagePieces.PieceWeight> getStructureVillageWeightedPieceList(Random random, int size)
    {
        List<StructureVillagePieces.PieceWeight> pieceWeights = Lists.<StructureVillagePieces.PieceWeight>newArrayList();
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House4Garden.class, 4, MathHelper.getRandomIntegerInRange(random, 2 + size, 4 + size * 2)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Church.class, 20, MathHelper.getRandomIntegerInRange(random, 0 + size, 1 + size)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House1.class, 20, MathHelper.getRandomIntegerInRange(random, 0 + size, 2 + size)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.WoodHut.class, 3, MathHelper.getRandomIntegerInRange(random, 2 + size, 5 + size * 3)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Hall.class, 15, MathHelper.getRandomIntegerInRange(random, 0 + size, 2 + size)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Field1.class, 3, MathHelper.getRandomIntegerInRange(random, 1 + size, 4 + size)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Field2.class, 3, MathHelper.getRandomIntegerInRange(random, 2 + size, 4 + size * 2)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House2.class, 15, MathHelper.getRandomIntegerInRange(random, 0, 1 + size)));
        pieceWeights.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House3.class, 8, MathHelper.getRandomIntegerInRange(random, 0 + size, 3 + size * 2)));
        Iterator<StructureVillagePieces.PieceWeight> iterator = pieceWeights.iterator();

        while (iterator.hasNext())
        {
            if (((StructureVillagePieces.PieceWeight)iterator.next()).villagePiecesLimit == 0)
            {
                iterator.remove();
            }
        }

        return pieceWeights;
    }

    private static int getTotalWeight(List<StructureVillagePieces.PieceWeight> pieceWeights)
    {
        boolean hasAvailableLimitedPiece = false;
        int totalWeight = 0;

        for (StructureVillagePieces.PieceWeight pieceWeight : pieceWeights)
        {
            if (pieceWeight.villagePiecesLimit > 0 && pieceWeight.villagePiecesSpawned < pieceWeight.villagePiecesLimit)
            {
                hasAvailableLimitedPiece = true;
            }

            totalWeight += pieceWeight.villagePieceWeight;
        }

        return hasAvailableLimitedPiece ? totalWeight : -1;
    }

    private static StructureVillagePieces.Village createPiece(StructureVillagePieces.Start start, StructureVillagePieces.PieceWeight weight, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        Class <? extends StructureVillagePieces.Village > pieceClass = weight.villagePieceClass;
        StructureVillagePieces.Village villagePiece = null;

        if (pieceClass == StructureVillagePieces.House4Garden.class)
        {
            villagePiece = StructureVillagePieces.House4Garden.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.Church.class)
        {
            villagePiece = StructureVillagePieces.Church.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.House1.class)
        {
            villagePiece = StructureVillagePieces.House1.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.WoodHut.class)
        {
            villagePiece = StructureVillagePieces.WoodHut.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.Hall.class)
        {
            villagePiece = StructureVillagePieces.Hall.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.Field1.class)
        {
            villagePiece = StructureVillagePieces.Field1.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.Field2.class)
        {
            villagePiece = StructureVillagePieces.Field2.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.House2.class)
        {
            villagePiece = StructureVillagePieces.House2.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }
        else if (pieceClass == StructureVillagePieces.House3.class)
        {
            villagePiece = StructureVillagePieces.House3.createPiece(start, pieces, rand, x, y, z, facing, depth);
        }

        return villagePiece;
    }

    private static StructureVillagePieces.Village pickVillagePiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        int totalWeight = getTotalWeight(start.structureVillageWeightedPieceList);

        if (totalWeight <= 0)
        {
            return null;
        }
        else
        {
            int attempts = 0;

            while (attempts < 5)
            {
                ++attempts;
                int weightChoice = rand.nextInt(totalWeight);

                for (StructureVillagePieces.PieceWeight pieceWeight : start.structureVillageWeightedPieceList)
                {
                    weightChoice -= pieceWeight.villagePieceWeight;

                    if (weightChoice < 0)
                    {
                        if (!pieceWeight.canSpawnMoreVillagePiecesOfType(depth) || pieceWeight == start.structVillagePieceWeight && start.structureVillageWeightedPieceList.size() > 1)
                        {
                            break;
                        }

                        StructureVillagePieces.Village villagePiece = createPiece(start, pieceWeight, pieces, rand, x, y, z, facing, depth);

                        if (villagePiece != null)
                        {
                            ++pieceWeight.villagePiecesSpawned;
                            start.structVillagePieceWeight = pieceWeight;

                            if (!pieceWeight.canSpawnMoreVillagePieces())
                            {
                                start.structureVillageWeightedPieceList.remove(pieceWeight);
                            }

                            return villagePiece;
                        }
                    }
                }
            }

            StructureBoundingBox torchBox = StructureVillagePieces.Torch.createBoundingBox(start, pieces, rand, x, y, z, facing);

            if (torchBox != null)
            {
                return new StructureVillagePieces.Torch(start, depth, rand, torchBox, facing);
            }
            else
            {
                return null;
            }
        }
    }

    private static StructureComponent createVillagePiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        if (depth > 50)
        {
            return null;
        }
        else if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112)
        {
            StructureComponent component = pickVillagePiece(start, pieces, rand, x, y, z, facing, depth + 1);

            if (component != null)
            {
                int centerX = (component.boundingBox.minX + component.boundingBox.maxX) / 2;
                int centerZ = (component.boundingBox.minZ + component.boundingBox.maxZ) / 2;
                int sizeX = component.boundingBox.maxX - component.boundingBox.minX;
                int sizeZ = component.boundingBox.maxZ - component.boundingBox.minZ;
                int maxSize = sizeX > sizeZ ? sizeX : sizeZ;

                if (start.getWorldChunkManager().areBiomesViable(centerX, centerZ, maxSize / 2 + 4, MapGenVillage.villageSpawnBiomes))
                {
                    pieces.add(component);
                    start.primaryPieces.add(component);
                    return component;
                }
            }

            return null;
        }
        else
        {
            return null;
        }
    }

    private static StructureComponent createVillagePath(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
    {
        if (depth > 3 + start.terrainType)
        {
            return null;
        }
        else if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112)
        {
            StructureBoundingBox pathBox = StructureVillagePieces.Path.createBoundingBox(start, pieces, rand, x, y, z, facing);

            if (pathBox != null && pathBox.minY > 10)
            {
                StructureComponent path = new StructureVillagePieces.Path(start, depth, rand, pathBox, facing);
                int centerX = (path.boundingBox.minX + path.boundingBox.maxX) / 2;
                int centerZ = (path.boundingBox.minZ + path.boundingBox.maxZ) / 2;
                int sizeX = path.boundingBox.maxX - path.boundingBox.minX;
                int sizeZ = path.boundingBox.maxZ - path.boundingBox.minZ;
                int maxSize = sizeX > sizeZ ? sizeX : sizeZ;

                if (start.getWorldChunkManager().areBiomesViable(centerX, centerZ, maxSize / 2 + 4, MapGenVillage.villageSpawnBiomes))
                {
                    pieces.add(path);
                    start.pathPieces.add(path);
                    return path;
                }
            }

            return null;
        }
        else
        {
            return null;
        }
    }

    public static class Church extends StructureVillagePieces.Village
    {
        public Church()
        {
        }

        public Church(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public static StructureVillagePieces.Church createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 5, 12, 9, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.Church(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 12 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 3, 7, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 3, 9, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 3, 0, 8, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 3, 10, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 10, 3, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 10, 3, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 4, 0, 4, 7, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 4, 4, 4, 7, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 8, 3, 4, 8, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 4, 3, 10, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 5, 3, 5, 7, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 9, 0, 4, 9, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 4, 4, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 11, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 11, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 2, 11, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 2, 11, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 1, 1, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 1, 1, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 2, 1, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 3, 1, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 3, 1, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 1, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 1, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 3, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 1)), 1, 2, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 0)), 3, 2, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 3, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 6, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 7, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 6, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 7, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 6, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 7, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 6, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 7, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 3, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 3, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.getOpposite()), 2, 4, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.rotateY()), 1, 4, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.rotateYCCW()), 3, 4, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode), 2, 4, 5, structureBoundingBoxIn);
            int i = this.getMetadataWithOffset(Blocks.ladder, 4);

            for (int j = 1; j <= 9; ++j)
            {
                this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, j, 3, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 2, 0, structureBoundingBoxIn);
            this.placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 2, 1, 0, EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1)));

            if (this.getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
            {
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn);
            }

            for (int l = 0; l < 9; ++l)
            {
                for (int k = 0; k < 5; ++k)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, k, 12, l, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), k, -1, l, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 2, 1, 2, 1);
            return true;
        }

        protected int getVillagerType(int professionId, int variant)
        {
            return 2;
        }
    }

    public static class Field1 extends StructureVillagePieces.Village
    {
        private Block cropTypeA;
        private Block cropTypeB;
        private Block cropTypeC;
        private Block cropTypeD;

        public Field1()
        {
        }

        public Field1(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.cropTypeA = this.getRandomCrop(rand);
            this.cropTypeB = this.getRandomCrop(rand);
            this.cropTypeC = this.getRandomCrop(rand);
            this.cropTypeD = this.getRandomCrop(rand);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("CA", Block.blockRegistry.getIDForObject(this.cropTypeA));
            tagCompound.setInteger("CB", Block.blockRegistry.getIDForObject(this.cropTypeB));
            tagCompound.setInteger("CC", Block.blockRegistry.getIDForObject(this.cropTypeC));
            tagCompound.setInteger("CD", Block.blockRegistry.getIDForObject(this.cropTypeD));
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.cropTypeA = Block.getBlockById(tagCompound.getInteger("CA"));
            this.cropTypeB = Block.getBlockById(tagCompound.getInteger("CB"));
            this.cropTypeC = Block.getBlockById(tagCompound.getInteger("CC"));
            this.cropTypeD = Block.getBlockById(tagCompound.getInteger("CD"));
        }

        private Block getRandomCrop(Random rand)
        {
            switch (rand.nextInt(5))
            {
                case 0:
                    return Blocks.carrots;

                case 1:
                    return Blocks.potatoes;

                default:
                    return Blocks.wheat;
            }
        }

        public static StructureVillagePieces.Field1 createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 13, 4, 9, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.Field1(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 4 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 12, 4, 8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 0, 1, 8, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 10, 0, 1, 11, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 12, 0, 0, 12, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 11, 0, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 8, 11, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, Blocks.water.getDefaultState(), Blocks.water.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 0, 1, 9, 0, 7, Blocks.water.getDefaultState(), Blocks.water.getDefaultState(), false);

            for (int i = 1; i <= 7; ++i)
            {
                this.setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 1, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 2, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 4, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 5, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeC.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 7, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeC.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 8, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeD.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 10, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeD.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 11, 1, i, structureBoundingBoxIn);
            }

            for (int k = 0; k < 9; ++k)
            {
                for (int j = 0; j < 13; ++j)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, j, 4, k, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.dirt.getDefaultState(), j, -1, k, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Field2 extends StructureVillagePieces.Village
    {
        private Block cropTypeA;
        private Block cropTypeB;

        public Field2()
        {
        }

        public Field2(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.cropTypeA = this.getRandomCrop(rand);
            this.cropTypeB = this.getRandomCrop(rand);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("CA", Block.blockRegistry.getIDForObject(this.cropTypeA));
            tagCompound.setInteger("CB", Block.blockRegistry.getIDForObject(this.cropTypeB));
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.cropTypeA = Block.getBlockById(tagCompound.getInteger("CA"));
            this.cropTypeB = Block.getBlockById(tagCompound.getInteger("CB"));
        }

        private Block getRandomCrop(Random rand)
        {
            switch (rand.nextInt(5))
            {
                case 0:
                    return Blocks.carrots;

                case 1:
                    return Blocks.potatoes;

                default:
                    return Blocks.wheat;
            }
        }

        public static StructureVillagePieces.Field2 createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 7, 4, 9, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.Field2(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 4 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 6, 4, 8, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 0, 1, 5, 0, 7, Blocks.farmland.getDefaultState(), Blocks.farmland.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 0, 0, 6, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 5, 0, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 8, 5, 0, 8, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 1, 3, 0, 7, Blocks.water.getDefaultState(), Blocks.water.getDefaultState(), false);

            for (int i = 1; i <= 7; ++i)
            {
                this.setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 1, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeA.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 2, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 4, 1, i, structureBoundingBoxIn);
                this.setBlockState(worldIn, this.cropTypeB.getStateFromMeta(MathHelper.getRandomIntegerInRange(randomIn, 2, 7)), 5, 1, i, structureBoundingBoxIn);
            }

            for (int k = 0; k < 9; ++k)
            {
                for (int j = 0; j < 7; ++j)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, j, 4, k, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.dirt.getDefaultState(), j, -1, k, structureBoundingBoxIn);
                }
            }

            return true;
        }
    }

    public static class Hall extends StructureVillagePieces.Village
    {
        public Hall()
        {
        }

        public Hall(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public static StructureVillagePieces.Hall createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 9, 7, 11, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.Hall(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 7 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 7, 4, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 8, 4, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 6, 8, 0, 10, Blocks.dirt.getDefaultState(), Blocks.dirt.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, 0, 6, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 2, 1, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 6, 8, 1, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 10, 7, 1, 10, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 7, 0, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 3, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 0, 8, 3, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 7, 1, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 5, 7, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 7, 3, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 1, 8, 4, 1, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 4, 8, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 2, 8, 5, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 3, structureBoundingBoxIn);
            int i = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
            int j = this.getMetadataWithOffset(Blocks.oak_stairs, 2);

            for (int k = -1; k <= 2; ++k)
            {
                for (int l = 0; l <= 8; ++l)
                {
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i), l, 4 + k, k, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j), l, 4 + k, 5 - k, structureBoundingBoxIn);
                }
            }

            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 3, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 1, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 2, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.oak_stairs, 3)), 2, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.oak_stairs, 1)), 1, 1, 3, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 0, 1, 7, 0, 3, Blocks.double_stone_slab.getDefaultState(), Blocks.double_stone_slab.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.double_stone_slab.getDefaultState(), 6, 1, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.double_stone_slab.getDefaultState(), 6, 1, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode), 2, 3, 1, structureBoundingBoxIn);
            this.placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 2, 1, 0, EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1)));

            if (this.getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
            {
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 6, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 6, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.getOpposite()), 6, 3, 4, structureBoundingBoxIn);
            this.placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 6, 1, 5, EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1)));

            for (int localZ = 0; localZ < 5; ++localZ)
            {
                for (int localX = 0; localX < 9; ++localX)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, localX, 7, localZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), localX, -1, localZ, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 4, 1, 2, 2);
            return true;
        }

        protected int getVillagerType(int professionId, int variant)
        {
            return professionId == 0 ? 4 : super.getVillagerType(professionId, variant);
        }
    }

    public static class House1 extends StructureVillagePieces.Village
    {
        public House1()
        {
        }

        public House1(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public static StructureVillagePieces.House1 createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 9, 9, 6, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.House1(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 9 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 7, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 8, 0, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 8, 5, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 6, 1, 8, 6, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 7, 2, 8, 7, 3, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            int i = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
            int j = this.getMetadataWithOffset(Blocks.oak_stairs, 2);

            for (int k = -1; k <= 2; ++k)
            {
                for (int l = 0; l <= 8; ++l)
                {
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i), l, 6 + k, k, structureBoundingBoxIn);
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j), l, 6 + k, 5 - k, structureBoundingBoxIn);
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 5, 8, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 1, 0, 8, 1, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 0, 7, 1, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 0, 0, 4, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 5, 0, 4, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 5, 8, 4, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 0, 8, 4, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 2, 1, 0, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 7, 4, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 2, 1, 8, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 4, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 3, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 3, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 3, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 3, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 6, 2, 5, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 7, 4, 1, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 4, 7, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 3, 4, 7, 3, 4, Blocks.bookshelf.getDefaultState(), Blocks.bookshelf.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 7, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.oak_stairs, 0)), 7, 1, 3, structureBoundingBoxIn);
            int chairStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(chairStairMeta), 6, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(chairStairMeta), 5, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(chairStairMeta), 4, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(chairStairMeta), 3, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 6, 1, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 6, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 1, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 4, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.crafting_table.getDefaultState(), 7, 1, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
            this.placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 1, 1, 0, EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1)));

            if (this.getBlockStateFromPos(worldIn, 1, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, 1, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
            {
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 1, 0, -1, structureBoundingBoxIn);
            }

            for (int localZ = 0; localZ < 6; ++localZ)
            {
                for (int localX = 0; localX < 9; ++localX)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, localX, 9, localZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), localX, -1, localZ, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 2, 1, 2, 1);
            return true;
        }

        protected int getVillagerType(int professionId, int variant)
        {
            return 1;
        }
    }

    public static class House2 extends StructureVillagePieces.Village
    {
        private static final List<WeightedRandomChestContent> villageBlacksmithChestContents = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 3), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 5), new WeightedRandomChestContent(Items.bread, 0, 1, 3, 15), new WeightedRandomChestContent(Items.apple, 0, 1, 3, 15), new WeightedRandomChestContent(Items.iron_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_sword, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_chestplate, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_helmet, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_leggings, 0, 1, 1, 5), new WeightedRandomChestContent(Items.iron_boots, 0, 1, 1, 5), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.obsidian), 0, 3, 7, 5), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.sapling), 0, 3, 7, 5), new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 3), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 1), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1)});
        private boolean hasMadeChest;

        public House2()
        {
        }

        public House2(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public static StructureVillagePieces.House2 createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 10, 6, 7, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.House2(start, depth, rand, boundingBox, facing) : null;
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

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 6 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 9, 4, 6, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 9, 0, 6, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 9, 4, 6, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 0, 9, 5, 6, Blocks.stone_slab.getDefaultState(), Blocks.stone_slab.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 8, 5, 5, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 2, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 4, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 3, 4, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 6, 0, 4, 6, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 3, 1, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 2, 3, 3, 2, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 3, 5, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 6, 5, 3, 6, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 1, 0, 5, 3, 0, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 9, 1, 0, 9, 3, 0, Blocks.oak_fence.getDefaultState(), Blocks.oak_fence.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 1, 4, 9, 4, 6, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.flowing_lava.getDefaultState(), 7, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.flowing_lava.getDefaultState(), 8, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), 9, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.iron_bars.getDefaultState(), 9, 2, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 2, 4, 8, 2, 5, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 6, 1, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.furnace.getDefaultState(), 6, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.furnace.getDefaultState(), 6, 3, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.double_stone_slab.getDefaultState(), 8, 1, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), 2, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.oak_stairs, 3)), 2, 1, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.oak_stairs, 1)), 1, 1, 4, structureBoundingBoxIn);

            if (!this.hasMadeChest && structureBoundingBoxIn.isVecInside(new BlockPos(this.getXWithOffset(5, 5), this.getYWithOffset(1), this.getZWithOffset(5, 5))))
            {
                this.hasMadeChest = true;
                this.generateChestContents(worldIn, structureBoundingBoxIn, randomIn, 5, 1, 5, villageBlacksmithChestContents, 3 + randomIn.nextInt(6));
            }

            for (int i = 6; i <= 8; ++i)
            {
                if (this.getBlockStateFromPos(worldIn, i, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, i, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
                {
                    this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), i, 0, -1, structureBoundingBoxIn);
                }
            }

            for (int k = 0; k < 7; ++k)
            {
                for (int j = 0; j < 10; ++j)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, j, 6, k, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j, -1, k, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 7, 1, 1, 1);
            return true;
        }

        protected int getVillagerType(int professionId, int variant)
        {
            return 3;
        }
    }

    public static class House3 extends StructureVillagePieces.Village
    {
        public House3()
        {
        }

        public House3(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public static StructureVillagePieces.House3 createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 9, 7, 12, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.House3(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 7 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 7, 4, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 1, 6, 8, 4, 10, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 5, 8, 0, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 7, 0, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 0, 3, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 8, 0, 0, 8, 3, 10, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 0, 7, 2, 0, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 5, 2, 1, 5, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 2, 0, 6, 2, 3, 10, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 0, 10, 7, 3, 10, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 0, 7, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 2, 5, 2, 3, 5, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 1, 8, 4, 1, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 4, 3, 4, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 5, 2, 8, 5, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 4, 4, structureBoundingBoxIn);
            int i = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
            int j = this.getMetadataWithOffset(Blocks.oak_stairs, 2);

            for (int k = -1; k <= 2; ++k)
            {
                for (int l = 0; l <= 8; ++l)
                {
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(i), l, 4 + k, k, structureBoundingBoxIn);

                    if ((k > -1 || l <= 1) && (k > 0 || l <= 3) && (k > 1 || l <= 4 || l >= 6))
                    {
                        this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(j), l, 4 + k, 5 - k, structureBoundingBoxIn);
                    }
                }
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 4, 5, 3, 4, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 7, 4, 2, 7, 4, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 5, 4, 4, 5, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 6, 5, 4, 6, 5, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 5, 6, 3, 5, 6, 10, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            int rearRoofStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 0);

            for (int roofX = 4; roofX >= 1; --roofX)
            {
                this.setBlockState(worldIn, Blocks.planks.getDefaultState(), roofX, 2 + roofX, 7 - roofX, structureBoundingBoxIn);

                for (int roofZ = 8 - roofX; roofZ <= 10; ++roofZ)
                {
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(rearRoofStairMeta), roofX, 2 + roofX, roofZ, structureBoundingBoxIn);
                }
            }

            int frontRoofStairMeta = this.getMetadataWithOffset(Blocks.oak_stairs, 1);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 6, 6, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 7, 5, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(frontRoofStairMeta), 6, 6, 4, structureBoundingBoxIn);

            for (int roofX = 6; roofX <= 8; ++roofX)
            {
                for (int roofZ = 5; roofZ <= 10; ++roofZ)
                {
                    this.setBlockState(worldIn, Blocks.oak_stairs.getStateFromMeta(frontRoofStairMeta), roofX, 12 - roofX, roofZ, structureBoundingBoxIn);
                }
            }

            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 4, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 6, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 8, 2, 5, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 8, 2, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 8, 2, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 2, 6, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 7, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 8, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 2, 9, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 4, 4, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 5, 4, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 6, 4, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 5, 5, 10, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode), 2, 3, 1, structureBoundingBoxIn);
            this.placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 2, 1, 0, EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1)));
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, -1, 3, 2, -1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

            if (this.getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
            {
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn);
            }

            for (int localZ = 0; localZ < 5; ++localZ)
            {
                for (int localX = 0; localX < 9; ++localX)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, localX, 7, localZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), localX, -1, localZ, structureBoundingBoxIn);
                }
            }

            for (int localZ = 5; localZ < 11; ++localZ)
            {
                for (int localX = 2; localX < 9; ++localX)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, localX, 7, localZ, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), localX, -1, localZ, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 4, 1, 2, 2);
            return true;
        }
    }

    public static class House4Garden extends StructureVillagePieces.Village
    {
        private boolean isRoofAccessible;

        public House4Garden()
        {
        }

        public House4Garden(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.isRoofAccessible = rand.nextBoolean();
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("Terrace", this.isRoofAccessible);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.isRoofAccessible = tagCompound.getBoolean("Terrace");
        }

        public static StructureVillagePieces.House4Garden createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 5, 6, 5, facing);
            return StructureComponent.findIntersecting(pieces, boundingBox) != null ? null : new StructureVillagePieces.House4Garden(start, depth, rand, boundingBox, facing);
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 6 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 0, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 4, 0, 4, 4, 4, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 3, 4, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 0, 3, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 1, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.cobblestone.getDefaultState(), 4, 3, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 4, 3, 3, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 2, 2, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 4, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 1, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 2, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.planks.getDefaultState(), 3, 1, 0, structureBoundingBoxIn);

            if (this.getBlockStateFromPos(worldIn, 2, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, 2, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
            {
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 2, 0, -1, structureBoundingBoxIn);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 3, 3, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);

            if (this.isRoofAccessible)
            {
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 3, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 0, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 5, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 2, 5, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 3, 5, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 4, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 5, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 1, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 2, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 0, 5, 3, structureBoundingBoxIn);
            }

            if (this.isRoofAccessible)
            {
                int i = this.getMetadataWithOffset(Blocks.ladder, 3);
                this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 1, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 2, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 3, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.ladder.getStateFromMeta(i), 3, 4, 3, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode), 2, 3, 1, structureBoundingBoxIn);

            for (int k = 0; k < 5; ++k)
            {
                for (int j = 0; j < 5; ++j)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, j, 6, k, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j, -1, k, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 1, 1, 2, 1);
            return true;
        }
    }

    public static class Path extends StructureVillagePieces.Road
    {
        private int length;

        public Path()
        {
        }

        public Path(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.length = Math.max(boundingBox.getXSize(), boundingBox.getZSize());
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("Length", this.length);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.length = tagCompound.getInteger("Length");
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            boolean sideConnectionCreated = false;

            for (int pathOffset = rand.nextInt(5); pathOffset < this.length - 8; pathOffset += 2 + rand.nextInt(5))
            {
                StructureComponent component = this.getNextComponentNN((StructureVillagePieces.Start)componentIn, listIn, rand, 0, pathOffset);

                if (component != null)
                {
                    pathOffset += Math.max(component.boundingBox.getXSize(), component.boundingBox.getZSize());
                    sideConnectionCreated = true;
                }
            }

            for (int pathOffset = rand.nextInt(5); pathOffset < this.length - 8; pathOffset += 2 + rand.nextInt(5))
            {
                StructureComponent component = this.getNextComponentPP((StructureVillagePieces.Start)componentIn, listIn, rand, 0, pathOffset);

                if (component != null)
                {
                    pathOffset += Math.max(component.boundingBox.getXSize(), component.boundingBox.getZSize());
                    sideConnectionCreated = true;
                }
            }

            if (sideConnectionCreated && rand.nextInt(3) > 0 && this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, this.getComponentType());
                        break;

                    case SOUTH:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.WEST, this.getComponentType());
                        break;

                    case WEST:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                        break;

                    case EAST:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                }
            }

            if (sideConnectionCreated && rand.nextInt(3) > 0 && this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, this.getComponentType());
                        break;

                    case SOUTH:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.EAST, this.getComponentType());
                        break;

                    case WEST:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                        break;

                    case EAST:
                        StructureVillagePieces.createVillagePiece((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                }
            }
        }

        public static StructureBoundingBox createBoundingBox(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing)
        {
            for (int i = 7 * MathHelper.getRandomIntegerInRange(rand, 3, 5); i >= 7; i -= 7)
            {
                StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 3, 3, i, facing);

                if (StructureComponent.findIntersecting(pieces, boundingBox) == null)
                {
                    return boundingBox;
                }
            }

            return null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            IBlockState roadState = this.selectBlock(Blocks.gravel.getDefaultState());
            IBlockState foundationState = this.selectBlock(Blocks.cobblestone.getDefaultState());

            for (int worldX = this.boundingBox.minX; worldX <= this.boundingBox.maxX; ++worldX)
            {
                for (int worldZ = this.boundingBox.minZ; worldZ <= this.boundingBox.maxZ; ++worldZ)
                {
                    BlockPos surfacePos = new BlockPos(worldX, 64, worldZ);

                    if (structureBoundingBoxIn.isVecInside(surfacePos))
                    {
                        surfacePos = worldIn.getTopSolidOrLiquidBlock(surfacePos).down();
                        worldIn.setBlockState(surfacePos, roadState, 2);
                        worldIn.setBlockState(surfacePos.down(), foundationState, 2);
                    }
                }
            }

            return true;
        }
    }

    public static class PieceWeight
    {
        public Class <? extends StructureVillagePieces.Village > villagePieceClass;
        public final int villagePieceWeight;
        public int villagePiecesSpawned;
        public int villagePiecesLimit;

        public PieceWeight(Class <? extends StructureVillagePieces.Village > pieceClass, int weight, int limit)
        {
            this.villagePieceClass = pieceClass;
            this.villagePieceWeight = weight;
            this.villagePiecesLimit = limit;
        }

        public boolean canSpawnMoreVillagePiecesOfType(int depth)
        {
            return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
        }

        public boolean canSpawnMoreVillagePieces()
        {
            return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
        }
    }

    public abstract static class Road extends StructureVillagePieces.Village
    {
        public Road()
        {
        }

        protected Road(StructureVillagePieces.Start start, int type)
        {
            super(start, type);
        }
    }

    public static class Start extends StructureVillagePieces.Well
    {
        public WorldChunkManager worldChunkMngr;
        public boolean inDesert;
        public int terrainType;
        public StructureVillagePieces.PieceWeight structVillagePieceWeight;
        public List<StructureVillagePieces.PieceWeight> structureVillageWeightedPieceList;
        public List<StructureComponent> primaryPieces = Lists.<StructureComponent>newArrayList();
        public List<StructureComponent> pathPieces = Lists.<StructureComponent>newArrayList();

        public Start()
        {
        }

        public Start(WorldChunkManager chunkManagerIn, int componentType, Random rand, int startX, int startZ, List<StructureVillagePieces.PieceWeight> pieceWeights, int terrainType)
        {
            super((StructureVillagePieces.Start)null, 0, rand, startX, startZ);
            this.worldChunkMngr = chunkManagerIn;
            this.structureVillageWeightedPieceList = pieceWeights;
            this.terrainType = terrainType;
            BiomeGenBase biome = chunkManagerIn.getBiomeGenerator(new BlockPos(startX, 0, startZ), BiomeGenBase.DEFAULT_BIOME);
            this.inDesert = biome == BiomeGenBase.desert || biome == BiomeGenBase.desertHills;
            this.setDesertVillage(this.inDesert);
        }

        public WorldChunkManager getWorldChunkManager()
        {
            return this.worldChunkMngr;
        }
    }

    public static class Torch extends StructureVillagePieces.Village
    {
        public Torch()
        {
        }

        public Torch(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
        }

        public static StructureBoundingBox createBoundingBox(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 3, 4, 2, facing);
            return StructureComponent.findIntersecting(pieces, boundingBox) != null ? null : boundingBox;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 4 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 2, 3, 1, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 0, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.wool.getStateFromMeta(EnumDyeColor.WHITE.getDyeDamage()), 1, 3, 0, structureBoundingBoxIn);
            boolean mirrorSideTorches = this.coordBaseMode == EnumFacing.EAST || this.coordBaseMode == EnumFacing.NORTH;
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.rotateY()), mirrorSideTorches ? 2 : 0, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode), 1, 3, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.rotateYCCW()), mirrorSideTorches ? 0 : 2, 3, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, this.coordBaseMode.getOpposite()), 1, 3, -1, structureBoundingBoxIn);
            return true;
        }
    }

    abstract static class Village extends StructureComponent
    {
        protected int groundLevel = -1;
        private int villagersSpawned;
        private boolean isDesertVillage;

        public Village()
        {
        }

        protected Village(StructureVillagePieces.Start start, int type)
        {
            super(type);

            if (start != null)
            {
                this.isDesertVillage = start.inDesert;
            }
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            tagCompound.setInteger("HPos", this.groundLevel);
            tagCompound.setInteger("VCount", this.villagersSpawned);
            tagCompound.setBoolean("Desert", this.isDesertVillage);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            this.groundLevel = tagCompound.getInteger("HPos");
            this.villagersSpawned = tagCompound.getInteger("VCount");
            this.isDesertVillage = tagCompound.getBoolean("Desert");
        }

        protected StructureComponent getNextComponentNN(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int offsetY, int offsetZ)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.WEST, this.getComponentType());

                    case SOUTH:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.minX - 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.WEST, this.getComponentType());

                    case WEST:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());

                    case EAST:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
                }
            }

            return null;
        }

        protected StructureComponent getNextComponentPP(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int offsetY, int offsetZ)
        {
            if (this.coordBaseMode != null)
            {
                switch (this.coordBaseMode)
                {
                    case NORTH:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.EAST, this.getComponentType());

                    case SOUTH:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + offsetY, this.boundingBox.minZ + offsetZ, EnumFacing.EAST, this.getComponentType());

                    case WEST:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());

                    case EAST:
                        return StructureVillagePieces.createVillagePiece(start, pieces, rand, this.boundingBox.minX + offsetZ, this.boundingBox.minY + offsetY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
                }
            }

            return null;
        }

        protected int getAverageGroundLevel(World worldIn, StructureBoundingBox structurebb)
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
                return -1;
            }
            else
            {
                return totalGroundY / blockCount;
            }
        }

        protected static boolean canVillageGoDeeper(StructureBoundingBox boundingBox)
        {
            return boundingBox != null && boundingBox.minY > 10;
        }

        protected void spawnVillagers(World worldIn, StructureBoundingBox structurebb, int x, int y, int z, int count)
        {
            if (this.villagersSpawned < count)
            {
                for (int spawnIndex = this.villagersSpawned; spawnIndex < count; ++spawnIndex)
                {
                    int worldX = this.getXWithOffset(x + spawnIndex, z);
                    int worldY = this.getYWithOffset(y);
                    int worldZ = this.getZWithOffset(x + spawnIndex, z);

                    if (!structurebb.isVecInside(new BlockPos(worldX, worldY, worldZ)))
                    {
                        break;
                    }

                    ++this.villagersSpawned;
                    EntityVillager villager = new EntityVillager(worldIn);
                    villager.setLocationAndAngles((double)worldX + 0.5D, (double)worldY, (double)worldZ + 0.5D, 0.0F, 0.0F);
                    villager.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(villager)), (IEntityLivingData)null);
                    villager.setProfession(this.getVillagerType(spawnIndex, villager.getProfession()));
                    worldIn.spawnEntityInWorld(villager);
                }
            }
        }

        protected int getVillagerType(int professionId, int variant)
        {
            return variant;
        }

        protected IBlockState selectBlock(IBlockState blockState)
        {
            if (this.isDesertVillage)
            {
                if (blockState.getBlock() == Blocks.log || blockState.getBlock() == Blocks.log2)
                {
                    return Blocks.sandstone.getDefaultState();
                }

                if (blockState.getBlock() == Blocks.cobblestone)
                {
                    return Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.DEFAULT.getMetadata());
                }

                if (blockState.getBlock() == Blocks.planks)
                {
                    return Blocks.sandstone.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata());
                }

                if (blockState.getBlock() == Blocks.oak_stairs)
                {
                    return Blocks.sandstone_stairs.getDefaultState().withProperty(BlockStairs.FACING, blockState.getValue(BlockStairs.FACING));
                }

                if (blockState.getBlock() == Blocks.stone_stairs)
                {
                    return Blocks.sandstone_stairs.getDefaultState().withProperty(BlockStairs.FACING, blockState.getValue(BlockStairs.FACING));
                }

                if (blockState.getBlock() == Blocks.gravel)
                {
                    return Blocks.sandstone.getDefaultState();
                }
            }

            return blockState;
        }

        protected void setBlockState(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn)
        {
            IBlockState selectedState = this.selectBlock(blockstateIn);
            super.setBlockState(worldIn, selectedState, x, y, z, boundingboxIn);
        }

        protected void fillWithBlocks(World worldIn, StructureBoundingBox boundingboxIn, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, IBlockState boundaryBlockState, IBlockState insideBlockState, boolean existingOnly)
        {
            IBlockState selectedBoundaryState = this.selectBlock(boundaryBlockState);
            IBlockState selectedInsideState = this.selectBlock(insideBlockState);
            super.fillWithBlocks(worldIn, boundingboxIn, xMin, yMin, zMin, xMax, yMax, zMax, selectedBoundaryState, selectedInsideState, existingOnly);
        }

        protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, int x, int y, int z, StructureBoundingBox boundingboxIn)
        {
            IBlockState selectedState = this.selectBlock(blockstateIn);
            super.replaceAirAndLiquidDownwards(worldIn, selectedState, x, y, z, boundingboxIn);
        }

        protected void setDesertVillage(boolean desertVillage)
        {
            this.isDesertVillage = desertVillage;
        }
    }

    public static class Well extends StructureVillagePieces.Village
    {
        public Well()
        {
        }

        public Well(StructureVillagePieces.Start start, int componentType, Random rand, int x, int z)
        {
            super(start, componentType);
            this.coordBaseMode = EnumFacing.Plane.HORIZONTAL.random(rand);

            switch (this.coordBaseMode)
            {
                case NORTH:
                case SOUTH:
                    this.boundingBox = new StructureBoundingBox(x, 64, z, x + 6 - 1, 78, z + 6 - 1);
                    break;

                default:
                    this.boundingBox = new StructureBoundingBox(x, 64, z, x + 6 - 1, 78, z + 6 - 1);
            }
        }

        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand)
        {
            StructureVillagePieces.createVillagePath((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.WEST, this.getComponentType());
            StructureVillagePieces.createVillagePath((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.EAST, this.getComponentType());
            StructureVillagePieces.createVillagePath((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            StructureVillagePieces.createVillagePath((StructureVillagePieces.Start)componentIn, listIn, rand, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 3, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 4, 12, 4, Blocks.cobblestone.getDefaultState(), Blocks.flowing_water.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 12, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 3, 12, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 2, 12, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 3, 12, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 13, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 14, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 13, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 14, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 13, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 1, 14, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 13, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), 4, 14, 4, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 15, 1, 4, 15, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);

            for (int i = 0; i <= 5; ++i)
            {
                for (int j = 0; j <= 5; ++j)
                {
                    if (j == 0 || j == 5 || i == 0 || i == 5)
                    {
                        this.setBlockState(worldIn, Blocks.gravel.getDefaultState(), j, 11, i, structureBoundingBoxIn);
                        this.clearCurrentPositionBlocksUpwards(worldIn, j, 12, i, structureBoundingBoxIn);
                    }
                }
            }

            return true;
        }
    }

    public static class WoodHut extends StructureVillagePieces.Village
    {
        private boolean isTallHouse;
        private int tablePosition;

        public WoodHut()
        {
        }

        public WoodHut(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox boundingBox, EnumFacing facing)
        {
            super(start, componentType);
            this.coordBaseMode = facing;
            this.boundingBox = boundingBox;
            this.isTallHouse = rand.nextBoolean();
            this.tablePosition = rand.nextInt(3);
        }

        protected void writeStructureToNBT(NBTTagCompound tagCompound)
        {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("T", this.tablePosition);
            tagCompound.setBoolean("C", this.isTallHouse);
        }

        protected void readStructureFromNBT(NBTTagCompound tagCompound)
        {
            super.readStructureFromNBT(tagCompound);
            this.tablePosition = tagCompound.getInteger("T");
            this.isTallHouse = tagCompound.getBoolean("C");
        }

        public static StructureVillagePieces.WoodHut createPiece(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int depth)
        {
            StructureBoundingBox boundingBox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 4, 6, 5, facing);
            return canVillageGoDeeper(boundingBox) && StructureComponent.findIntersecting(pieces, boundingBox) == null ? new StructureVillagePieces.WoodHut(start, depth, rand, boundingBox, facing) : null;
        }

        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn)
        {
            if (this.groundLevel < 0)
            {
                this.groundLevel = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

                if (this.groundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.groundLevel - this.boundingBox.maxY + 6 - 1, 0);
            }

            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 5, 4, Blocks.air.getDefaultState(), Blocks.air.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 3, 0, 4, Blocks.cobblestone.getDefaultState(), Blocks.cobblestone.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 3, Blocks.dirt.getDefaultState(), Blocks.dirt.getDefaultState(), false);

            if (this.isTallHouse)
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 2, 4, 3, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            }
            else
            {
                this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 2, 5, 3, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            }

            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 1, 4, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 4, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 1, 4, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 2, 4, 4, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 4, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 3, 4, 1, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 3, 4, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.log.getDefaultState(), 3, 4, 3, structureBoundingBoxIn);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 3, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 3, 3, 0, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 4, 0, 3, 4, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 4, 3, 3, 4, Blocks.log.getDefaultState(), Blocks.log.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 1, 3, 3, 3, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 2, 3, 0, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 4, 2, 3, 4, Blocks.planks.getDefaultState(), Blocks.planks.getDefaultState(), false);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.glass_pane.getDefaultState(), 3, 2, 2, structureBoundingBoxIn);

            if (this.tablePosition > 0)
            {
                this.setBlockState(worldIn, Blocks.oak_fence.getDefaultState(), this.tablePosition, 1, 3, structureBoundingBoxIn);
                this.setBlockState(worldIn, Blocks.wooden_pressure_plate.getDefaultState(), this.tablePosition, 2, 3, structureBoundingBoxIn);
            }

            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
            this.setBlockState(worldIn, Blocks.air.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
            this.placeDoorCurrentPosition(worldIn, structureBoundingBoxIn, randomIn, 1, 1, 0, EnumFacing.getHorizontal(this.getMetadataWithOffset(Blocks.oak_door, 1)));

            if (this.getBlockStateFromPos(worldIn, 1, 0, -1, structureBoundingBoxIn).getBlock().getMaterial() == Material.air && this.getBlockStateFromPos(worldIn, 1, -1, -1, structureBoundingBoxIn).getBlock().getMaterial() != Material.air)
            {
                this.setBlockState(worldIn, Blocks.stone_stairs.getStateFromMeta(this.getMetadataWithOffset(Blocks.stone_stairs, 3)), 1, 0, -1, structureBoundingBoxIn);
            }

            for (int i = 0; i < 5; ++i)
            {
                for (int j = 0; j < 4; ++j)
                {
                    this.clearCurrentPositionBlocksUpwards(worldIn, j, 6, i, structureBoundingBoxIn);
                    this.replaceAirAndLiquidDownwards(worldIn, Blocks.cobblestone.getDefaultState(), j, -1, i, structureBoundingBoxIn);
                }
            }

            this.spawnVillagers(worldIn, structureBoundingBoxIn, 1, 1, 2, 1);
            return true;
        }
    }
}
