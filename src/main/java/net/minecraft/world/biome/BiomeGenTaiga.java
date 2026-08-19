package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeGenTaiga extends BiomeGenBase
{
    private static final WorldGenTaiga1 SPRUCE_TREE = new WorldGenTaiga1();
    private static final WorldGenTaiga2 PINE_TREE = new WorldGenTaiga2(false);
    private static final WorldGenMegaPineTree MEGA_PINE_TREE = new WorldGenMegaPineTree(false, false);
    private static final WorldGenMegaPineTree MEGA_SPRUCE_TREE = new WorldGenMegaPineTree(false, true);
    private static final WorldGenBlockBlob MOSSY_COBBLESTONE_BOULDER = new WorldGenBlockBlob(Blocks.mossy_cobblestone, 0);
    private int taigaType;

    public BiomeGenTaiga(int id, int taigaTypeIn)
    {
        super(id);
        this.taigaType = taigaTypeIn;
        this.spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 8, 4, 4));
        this.theBiomeDecorator.treesPerChunk = 10;

        if (taigaTypeIn != 1 && taigaTypeIn != 2)
        {
            this.theBiomeDecorator.grassPerChunk = 1;
            this.theBiomeDecorator.mushroomsPerChunk = 1;
        }
        else
        {
            this.theBiomeDecorator.grassPerChunk = 7;
            this.theBiomeDecorator.deadBushPerChunk = 1;
            this.theBiomeDecorator.mushroomsPerChunk = 3;
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand)
    {
        return (WorldGenAbstractTree)((this.taigaType == 1 || this.taigaType == 2) && rand.nextInt(3) == 0 ? (this.taigaType != 2 && rand.nextInt(13) != 0 ? MEGA_PINE_TREE : MEGA_SPRUCE_TREE) : (rand.nextInt(3) == 0 ? SPRUCE_TREE : PINE_TREE));
    }

    public WorldGenerator getRandomWorldGenForGrass(Random rand)
    {
        return rand.nextInt(5) > 0 ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN) : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    }

    public void decorate(World worldIn, Random rand, BlockPos pos)
    {
        if (this.taigaType == 1 || this.taigaType == 2)
        {
            int boulderCount = rand.nextInt(3);

            for (int boulderIndex = 0; boulderIndex < boulderCount; ++boulderIndex)
            {
                int boulderXOffset = rand.nextInt(16) + 8;
                int boulderZOffset = rand.nextInt(16) + 8;
                BlockPos boulderPos = worldIn.getHeight(pos.add(boulderXOffset, 0, boulderZOffset));
                MOSSY_COBBLESTONE_BOULDER.generate(worldIn, rand, boulderPos);
            }
        }

        DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.FERN);

        for (int fernIndex = 0; fernIndex < 7; ++fernIndex)
        {
            int fernXOffset = rand.nextInt(16) + 8;
            int fernZOffset = rand.nextInt(16) + 8;
            int fernY = rand.nextInt(worldIn.getHeight(pos.add(fernXOffset, 0, fernZOffset)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(fernXOffset, fernY, fernZOffset));
        }

        super.decorate(worldIn, rand, pos);
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal)
    {
        if (this.taigaType == 1 || this.taigaType == 2)
        {
            this.topBlock = Blocks.grass.getDefaultState();
            this.fillerBlock = Blocks.dirt.getDefaultState();

            if (noiseVal > 1.75D)
            {
                this.topBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
            }
            else if (noiseVal > -0.95D)
            {
                this.topBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
            }
        }

        this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }

    protected BiomeGenBase createMutatedBiome(int newBiomeId)
    {
        return this.biomeID == BiomeGenBase.megaTaiga.biomeID ? (new BiomeGenTaiga(newBiomeId, 2)).setColor(5858897, true).setBiomeName("Mega Spruce Taiga").setFillerBlockMetadata(5159473).setTemperatureRainfall(0.25F, 0.8F).setHeight(new BiomeGenBase.Height(this.minHeight, this.maxHeight)) : super.createMutatedBiome(newBiomeId);
    }
}
