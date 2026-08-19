package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BiomeGenPlains extends BiomeGenBase
{
    protected boolean hasSunflowers;

    protected BiomeGenPlains(int id)
    {
        super(id);
        this.setTemperatureRainfall(0.8F, 0.4F);
        this.setHeight(height_LowPlains);
        this.spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityHorse.class, 5, 2, 6));
        this.theBiomeDecorator.treesPerChunk = -999;
        this.theBiomeDecorator.flowersPerChunk = 4;
        this.theBiomeDecorator.grassPerChunk = 10;
    }

    public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos)
    {
        double grassNoise = GRASS_COLOR_NOISE.getValue((double)pos.getX() / 200.0D, (double)pos.getZ() / 200.0D);

        if (grassNoise < -0.8D)
        {
            int tulipTypeIndex = rand.nextInt(4);

            switch (tulipTypeIndex)
            {
                case 0:
                    return BlockFlower.EnumFlowerType.ORANGE_TULIP;

                case 1:
                    return BlockFlower.EnumFlowerType.RED_TULIP;

                case 2:
                    return BlockFlower.EnumFlowerType.PINK_TULIP;

                case 3:
                default:
                    return BlockFlower.EnumFlowerType.WHITE_TULIP;
            }
        }
        else if (rand.nextInt(3) > 0)
        {
            int flowerTypeIndex = rand.nextInt(3);
            return flowerTypeIndex == 0 ? BlockFlower.EnumFlowerType.POPPY : (flowerTypeIndex == 1 ? BlockFlower.EnumFlowerType.HOUSTONIA : BlockFlower.EnumFlowerType.OXEYE_DAISY);
        }
        else
        {
            return BlockFlower.EnumFlowerType.DANDELION;
        }
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        double grassNoise = GRASS_COLOR_NOISE.getValue((double)(pos.getX() + 8) / 200.0D, (double)(pos.getZ() + 8) / 200.0D);

        if (grassNoise < -0.8D)
        {
            this.theBiomeDecorator.flowersPerChunk = 15;
            this.theBiomeDecorator.grassPerChunk = 5;
        }
        else
        {
            this.theBiomeDecorator.flowersPerChunk = 4;
            this.theBiomeDecorator.grassPerChunk = 10;
            DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);

            for (int attemptIndex = 0; attemptIndex < 7; ++attemptIndex)
            {
                int xOffset = rand.nextInt(16) + 8;
                int zOffset = rand.nextInt(16) + 8;
                int targetY = rand.nextInt(worldIn.getHeight(pos.add(xOffset, 0, zOffset)).getY() + 32);
                DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(xOffset, targetY, zOffset));
            }
        }

        if (this.hasSunflowers)
        {
            DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SUNFLOWER);

            for (int index2 = 0; index2 < 10; ++index2)
            {
                int xOffset = rand.nextInt(16) + 8;
                int zOffset = rand.nextInt(16) + 8;
                int targetY = rand.nextInt(worldIn.getHeight(pos.add(xOffset, 0, zOffset)).getY() + 32);
                DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(xOffset, targetY, zOffset));
            }
        }

        super.decorate(worldIn, rand, pos);
    }

    protected BiomeGenBase createMutatedBiome(int newBiomeId)
    {
        BiomeGenPlains biomeGenPlains = new BiomeGenPlains(newBiomeId);
        biomeGenPlains.setBiomeName("Sunflower Plains");
        biomeGenPlains.hasSunflowers = true;
        biomeGenPlains.setColor(9286496);
        biomeGenPlains.displayColor = 14273354;
        return biomeGenPlains;
    }
}
