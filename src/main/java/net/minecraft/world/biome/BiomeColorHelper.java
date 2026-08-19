package net.minecraft.world.biome;

import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BiomeColorHelper
{
    private static final BiomeColorHelper.ColorResolver GRASS_COLOR = new BiomeColorHelper.ColorResolver()
    {
        public int getColorAtPos(BiomeGenBase biome, BlockPos blockPosition)
        {
            return biome.getGrassColorAtPos(blockPosition);
        }
    };
    private static final BiomeColorHelper.ColorResolver FOLIAGE_COLOR = new BiomeColorHelper.ColorResolver()
    {
        public int getColorAtPos(BiomeGenBase biome, BlockPos blockPosition)
        {
            return biome.getFoliageColorAtPos(blockPosition);
        }
    };
    private static final BiomeColorHelper.ColorResolver WATER_COLOR_MULTIPLIER = new BiomeColorHelper.ColorResolver()
    {
        public int getColorAtPos(BiomeGenBase biome, BlockPos blockPosition)
        {
            return biome.waterColorMultiplier;
        }
    };

    private static int getColorAtPos(IBlockAccess blockAccess, BlockPos pos, BiomeColorHelper.ColorResolver colorResolver)
    {
        int redTotal = 0;
        int greenTotal = 0;
        int blueTotal = 0;

        for (BlockPos.MutableBlockPos samplePos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1)))
        {
            int color = colorResolver.getColorAtPos(blockAccess.getBiomeGenForCoords(samplePos), samplePos);
            redTotal += (color & 16711680) >> 16;
            greenTotal += (color & 65280) >> 8;
            blueTotal += color & 255;
        }

        return (redTotal / 9 & 255) << 16 | (greenTotal / 9 & 255) << 8 | blueTotal / 9 & 255;
    }

    public static int getGrassColorAtPos(IBlockAccess blockAccess, BlockPos pos)
    {
        return getColorAtPos(blockAccess, pos, GRASS_COLOR);
    }

    public static int getFoliageColorAtPos(IBlockAccess blockAccess, BlockPos pos)
    {
        return getColorAtPos(blockAccess, pos, FOLIAGE_COLOR);
    }

    public static int getWaterColorAtPos(IBlockAccess blockAccess, BlockPos pos)
    {
        return getColorAtPos(blockAccess, pos, WATER_COLOR_MULTIPLIER);
    }

    interface ColorResolver
    {
        int getColorAtPos(BiomeGenBase biome, BlockPos blockPosition);
    }
}
