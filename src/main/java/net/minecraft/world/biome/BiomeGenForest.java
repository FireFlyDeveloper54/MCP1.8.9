package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;

public class BiomeGenForest extends BiomeGenBase
{
    private int forestType;
    protected static final WorldGenForest SUPER_BIRCH_TREE = new WorldGenForest(false, true);
    protected static final WorldGenForest BIRCH_TREE = new WorldGenForest(false, false);
    protected static final WorldGenCanopyTree ROOF_TREE = new WorldGenCanopyTree(false);
    private static final BlockFlower.EnumFlowerType[] FLOWER_TYPES = BlockFlower.EnumFlowerType.VALUES;

    public BiomeGenForest(int id, int forestTypeIn)
    {
        super(id);
        this.forestType = forestTypeIn;
        this.theBiomeDecorator.treesPerChunk = 10;
        this.theBiomeDecorator.grassPerChunk = 2;

        if (this.forestType == 1)
        {
            this.theBiomeDecorator.treesPerChunk = 6;
            this.theBiomeDecorator.flowersPerChunk = 100;
            this.theBiomeDecorator.grassPerChunk = 1;
        }

        this.setFillerBlockMetadata(5159473);
        this.setTemperatureRainfall(0.7F, 0.8F);

        if (this.forestType == 2)
        {
            this.displayColor = 353825;
            this.color = 3175492;
            this.setTemperatureRainfall(0.6F, 0.6F);
        }

        if (this.forestType == 0)
        {
            this.spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 5, 4, 4));
        }

        if (this.forestType == 3)
        {
            this.theBiomeDecorator.treesPerChunk = -999;
        }
    }

    protected BiomeGenBase setColor(int colorIn, boolean darkenDisplayColor)
    {
        if (this.forestType == 2)
        {
            this.displayColor = 353825;
            this.color = colorIn;

            if (darkenDisplayColor)
            {
                this.displayColor = (this.displayColor & 16711422) >> 1;
            }

            return this;
        }
        else
        {
            return super.setColor(colorIn, darkenDisplayColor);
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return (WorldGenAbstractTree)(this.forestType == 3 && rand.nextInt(3) > 0 ? ROOF_TREE : (this.forestType != 2 && rand.nextInt(5) != 0 ? this.worldGeneratorTrees : BIRCH_TREE));
    }

    public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos)
    {
        if (this.forestType == 1)
        {
            double xCoordinate = MathHelper.clamp_double((1.0D + GRASS_COLOR_NOISE.getValue((double)pos.getX() / 48.0D, (double)pos.getZ() / 48.0D)) / 2.0D, 0.0D, 0.9999D);
            BlockFlower.EnumFlowerType flowerType = FLOWER_TYPES[(int)(xCoordinate * (double)FLOWER_TYPES.length)];
            return flowerType == BlockFlower.EnumFlowerType.BLUE_ORCHID ? BlockFlower.EnumFlowerType.POPPY : flowerType;
        }
        else
        {
            return super.pickRandomFlower(rand, pos);
        }
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        if (this.forestType == 3)
        {
            for (int gridX = 0; gridX < 4; ++gridX)
            {
                for (int gridZ = 0; gridZ < 4; ++gridZ)
                {
                    int xOffset = gridX * 4 + 1 + 8 + rand.nextInt(3);
                    int zOffset = gridZ * 4 + 1 + 8 + rand.nextInt(3);
                    BlockPos treePos = worldIn.getHeight(pos.add(xOffset, 0, zOffset));

                    if (rand.nextInt(20) == 0)
                    {
                        WorldGenBigMushroom bigMushroomGenerator = new WorldGenBigMushroom();
                        bigMushroomGenerator.generate(worldIn, rand, treePos);
                    }
                    else
                    {
                        WorldGenAbstractTree treeGenerator = this.genBigTreeChance(rand);
                        treeGenerator.prepareGeneration();

                        if (treeGenerator.generate(worldIn, rand, treePos))
                        {
                            treeGenerator.generateSaplings(worldIn, rand, treePos);
                        }
                    }
                }
            }
        }

        int doublePlantAttempts = rand.nextInt(5) - 3;

        if (this.forestType == 1)
        {
            doublePlantAttempts += 2;
        }

        for (int doublePlantIndex = 0; doublePlantIndex < doublePlantAttempts; ++doublePlantIndex)
        {
            int plantTypeIndex = rand.nextInt(3);

            if (plantTypeIndex == 0)
            {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SYRINGA);
            }
            else if (plantTypeIndex == 1)
            {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.ROSE);
            }
            else if (plantTypeIndex == 2)
            {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.PAEONIA);
            }

            for (int placementAttempt = 0; placementAttempt < 5; ++placementAttempt)
            {
                int xOffset = rand.nextInt(16) + 8;
                int zOffset = rand.nextInt(16) + 8;
                int targetY = rand.nextInt(worldIn.getHeight(pos.add(xOffset, 0, zOffset)).getY() + 32);

                if (DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, new BlockPos(pos.getX() + xOffset, targetY, pos.getZ() + zOffset)))
                {
                    break;
                }
            }
        }

        super.decorate(worldIn, rand, pos);
    }

    public int getGrassColorAtPos(BlockPos pos)
    {
        int baseColor = super.getGrassColorAtPos(pos);
        return this.forestType == 3 ? (baseColor & 16711422) + 2634762 >> 1 : baseColor;
    }

    protected BiomeGenBase createMutatedBiome(final int newBiomeId)
    {
        if (this.biomeID == BiomeGenBase.forest.biomeID)
        {
            BiomeGenForest biomeGenForest = new BiomeGenForest(newBiomeId, 1);
            biomeGenForest.setHeight(new BiomeGenBase.Height(this.minHeight, this.maxHeight + 0.2F));
            biomeGenForest.setBiomeName("Flower Forest");
            biomeGenForest.setColor(6976549, true);
            biomeGenForest.setFillerBlockMetadata(8233509);
            return biomeGenForest;
        }
        else
        {
            return this.biomeID != BiomeGenBase.birchForest.biomeID && this.biomeID != BiomeGenBase.birchForestHills.biomeID ? new BiomeGenMutated(newBiomeId, this)
            {
                public void decorate(World worldIn, Random rand, BlockPos pos)
                {
                    this.baseBiome.decorate(worldIn, rand, pos);
                }
            }: new BiomeGenMutated(newBiomeId, this)
            {
                public WorldGenAbstractTree genBigTreeChance(Random rand)
                {
                    return rand.nextBoolean() ? BiomeGenForest.SUPER_BIRCH_TREE : BiomeGenForest.BIRCH_TREE;
                }
            };
        }
    }
}
