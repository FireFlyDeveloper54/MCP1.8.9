package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeGenSwamp extends BiomeGenBase
{
    protected BiomeGenSwamp(int id)
    {
        super(id);
        this.theBiomeDecorator.treesPerChunk = 2;
        this.theBiomeDecorator.flowersPerChunk = 1;
        this.theBiomeDecorator.deadBushPerChunk = 1;
        this.theBiomeDecorator.mushroomsPerChunk = 8;
        this.theBiomeDecorator.reedsPerChunk = 10;
        this.theBiomeDecorator.clayPerChunk = 1;
        this.theBiomeDecorator.waterlilyPerChunk = 4;
        this.theBiomeDecorator.sandPatchesPerChunk = 0;
        this.theBiomeDecorator.gravelPatchesPerChunk = 0;
        this.theBiomeDecorator.grassPerChunk = 5;
        this.waterColorMultiplier = 14745518;
        this.spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntitySlime.class, 1, 1, 1));
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return this.worldGeneratorSwamp;
    }

    public int getGrassColorAtPos(BlockPos pos)
    {
        double grassNoise = GRASS_COLOR_NOISE.getValue((double)pos.getX() * 0.0225D, (double)pos.getZ() * 0.0225D);
        return grassNoise < -0.1D ? 5011004 : 6975545;
    }

    public int getFoliageColorAtPos(BlockPos pos)
    {
        return 6975545;
    }

    public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos)
    {
        return BlockFlower.EnumFlowerType.BLUE_ORCHID;
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal)
    {
        double terrainNoise = GRASS_COLOR_NOISE.getValue((double)x * 0.25D, (double)z * 0.25D);

        if (terrainNoise > 0.0D)
        {
            int primerZ = x & 15;
            int primerX = z & 15;

            for (int surfaceY = 255; surfaceY >= 0; --surfaceY)
            {
                if (chunkPrimerIn.getBlockState(primerX, surfaceY, primerZ).getBlock().getMaterial() != Material.air)
                {
                    if (surfaceY == 62 && chunkPrimerIn.getBlockState(primerX, surfaceY, primerZ).getBlock() != Blocks.water)
                    {
                        chunkPrimerIn.setBlockState(primerX, surfaceY, primerZ, Blocks.water.getDefaultState());

                        if (terrainNoise < 0.12D)
                        {
                            chunkPrimerIn.setBlockState(primerX, surfaceY + 1, primerZ, Blocks.waterlily.getDefaultState());
                        }
                    }

                    break;
                }
            }
        }

        this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }
}
