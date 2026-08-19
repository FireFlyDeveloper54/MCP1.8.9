package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenDoublePlant extends WorldGenerator
{
    private BlockDoublePlant.EnumPlantType plantType;

    public void setPlantType(BlockDoublePlant.EnumPlantType plantType)
    {
        this.plantType = plantType;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        boolean generatedAnyPlant = false;

        for (int attempt = 0; attempt < 64; ++attempt)
        {
            BlockPos plantPos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(plantPos) && (!worldIn.provider.getHasNoSky() || plantPos.getY() < 254) && Blocks.double_plant.canPlaceBlockAt(worldIn, plantPos))
            {
                Blocks.double_plant.placeAt(worldIn, plantPos, this.plantType, 2);
                generatedAnyPlant = true;
            }
        }

        return generatedAnyPlant;
    }
}
