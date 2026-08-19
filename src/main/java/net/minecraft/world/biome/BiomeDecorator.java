package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.GeneratorBushFeature;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeDecorator
{
    protected World currentWorld;
    protected Random randomGenerator;
    protected BlockPos chunkPos;
    protected ChunkProviderSettings chunkProviderSettings;
    protected WorldGenerator clayGen = new WorldGenClay(4);
    protected WorldGenerator sandGen = new WorldGenSand(Blocks.sand, 7);
    protected WorldGenerator gravelAsSandGen = new WorldGenSand(Blocks.gravel, 6);
    protected WorldGenerator dirtGen;
    protected WorldGenerator gravelGen;
    protected WorldGenerator graniteGen;
    protected WorldGenerator dioriteGen;
    protected WorldGenerator andesiteGen;
    protected WorldGenerator coalGen;
    protected WorldGenerator ironGen;
    protected WorldGenerator goldGen;
    protected WorldGenerator redstoneGen;
    protected WorldGenerator diamondGen;
    protected WorldGenerator lapisGen;
    protected WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.yellow_flower, BlockFlower.EnumFlowerType.DANDELION);
    protected WorldGenerator mushroomBrownGen = new GeneratorBushFeature(Blocks.brown_mushroom);
    protected WorldGenerator mushroomRedGen = new GeneratorBushFeature(Blocks.red_mushroom);
    protected WorldGenerator bigMushroomGen = new WorldGenBigMushroom();
    protected WorldGenerator reedGen = new WorldGenReed();
    protected WorldGenerator cactusGen = new WorldGenCactus();
    protected WorldGenerator waterlilyGen = new WorldGenWaterlily();
    protected int waterlilyPerChunk;
    protected int treesPerChunk;
    protected int flowersPerChunk = 2;
    protected int grassPerChunk = 1;
    protected int deadBushPerChunk;
    protected int mushroomsPerChunk;
    protected int reedsPerChunk;
    protected int cactiPerChunk;
    protected int gravelPatchesPerChunk = 1;
    protected int sandPatchesPerChunk = 3;
    protected int clayPerChunk = 1;
    protected int bigMushroomsPerChunk;
    public boolean generateLakes = true;

    public void decorate(World worldIn, Random random, BiomeGenBase biome, BlockPos chunkPos)
    {
        if (this.currentWorld != null)
        {
            throw new RuntimeException("Already decorating");
        }
        else
        {
            this.currentWorld = worldIn;
            String generatorOptions = worldIn.getWorldInfo().getGeneratorOptions();

            if (generatorOptions != null)
            {
                this.chunkProviderSettings = ChunkProviderSettings.Factory.jsonToFactory(generatorOptions).build();
            }
            else
            {
                this.chunkProviderSettings = ChunkProviderSettings.Factory.jsonToFactory("").build();
            }

            this.randomGenerator = random;
            this.chunkPos = chunkPos;
            this.dirtGen = new WorldGenMinable(Blocks.dirt.getDefaultState(), this.chunkProviderSettings.dirtSize);
            this.gravelGen = new WorldGenMinable(Blocks.gravel.getDefaultState(), this.chunkProviderSettings.gravelSize);
            this.graniteGen = new WorldGenMinable(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), this.chunkProviderSettings.graniteSize);
            this.dioriteGen = new WorldGenMinable(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), this.chunkProviderSettings.dioriteSize);
            this.andesiteGen = new WorldGenMinable(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), this.chunkProviderSettings.andesiteSize);
            this.coalGen = new WorldGenMinable(Blocks.coal_ore.getDefaultState(), this.chunkProviderSettings.coalSize);
            this.ironGen = new WorldGenMinable(Blocks.iron_ore.getDefaultState(), this.chunkProviderSettings.ironSize);
            this.goldGen = new WorldGenMinable(Blocks.gold_ore.getDefaultState(), this.chunkProviderSettings.goldSize);
            this.redstoneGen = new WorldGenMinable(Blocks.redstone_ore.getDefaultState(), this.chunkProviderSettings.redstoneSize);
            this.diamondGen = new WorldGenMinable(Blocks.diamond_ore.getDefaultState(), this.chunkProviderSettings.diamondSize);
            this.lapisGen = new WorldGenMinable(Blocks.lapis_ore.getDefaultState(), this.chunkProviderSettings.lapisSize);
            this.genDecorations(biome);
            this.currentWorld = null;
            this.randomGenerator = null;
        }
    }

    protected void genDecorations(BiomeGenBase biomeGenBaseIn)
    {
        this.generateOres();

        for (int sandPatchIndex = 0; sandPatchIndex < this.sandPatchesPerChunk; ++sandPatchIndex)
        {
            int sandXOffset = this.randomGenerator.nextInt(16) + 8;
            int sandZOffset = this.randomGenerator.nextInt(16) + 8;
            this.sandGen.generate(this.currentWorld, this.randomGenerator, this.currentWorld.getTopSolidOrLiquidBlock(this.chunkPos.add(sandXOffset, 0, sandZOffset)));
        }

        for (int clayPatchIndex = 0; clayPatchIndex < this.clayPerChunk; ++clayPatchIndex)
        {
            int clayXOffset = this.randomGenerator.nextInt(16) + 8;
            int clayZOffset = this.randomGenerator.nextInt(16) + 8;
            this.clayGen.generate(this.currentWorld, this.randomGenerator, this.currentWorld.getTopSolidOrLiquidBlock(this.chunkPos.add(clayXOffset, 0, clayZOffset)));
        }

        for (int gravelPatchIndex = 0; gravelPatchIndex < this.gravelPatchesPerChunk; ++gravelPatchIndex)
        {
            int gravelXOffset = this.randomGenerator.nextInt(16) + 8;
            int gravelZOffset = this.randomGenerator.nextInt(16) + 8;
            this.gravelAsSandGen.generate(this.currentWorld, this.randomGenerator, this.currentWorld.getTopSolidOrLiquidBlock(this.chunkPos.add(gravelXOffset, 0, gravelZOffset)));
        }

        int treeCount = this.treesPerChunk;

        if (this.randomGenerator.nextInt(10) == 0)
        {
            ++treeCount;
        }

        for (int treeIndex = 0; treeIndex < treeCount; ++treeIndex)
        {
            int treeXOffset = this.randomGenerator.nextInt(16) + 8;
            int treeZOffset = this.randomGenerator.nextInt(16) + 8;
            WorldGenAbstractTree treeGenerator = biomeGenBaseIn.genBigTreeChance(this.randomGenerator);
            treeGenerator.prepareGeneration();
            BlockPos treePos = this.currentWorld.getHeight(this.chunkPos.add(treeXOffset, 0, treeZOffset));

            if (treeGenerator.generate(this.currentWorld, this.randomGenerator, treePos))
            {
                treeGenerator.generateSaplings(this.currentWorld, this.randomGenerator, treePos);
            }
        }

        for (int bigMushroomIndex = 0; bigMushroomIndex < this.bigMushroomsPerChunk; ++bigMushroomIndex)
        {
            int bigMushroomXOffset = this.randomGenerator.nextInt(16) + 8;
            int bigMushroomZOffset = this.randomGenerator.nextInt(16) + 8;
            this.bigMushroomGen.generate(this.currentWorld, this.randomGenerator, this.currentWorld.getHeight(this.chunkPos.add(bigMushroomXOffset, 0, bigMushroomZOffset)));
        }

        for (int flowerIndex = 0; flowerIndex < this.flowersPerChunk; ++flowerIndex)
        {
            int flowerXOffset = this.randomGenerator.nextInt(16) + 8;
            int flowerZOffset = this.randomGenerator.nextInt(16) + 8;
            int flowerHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(flowerXOffset, 0, flowerZOffset)).getY() + 32;

            if (flowerHeightLimit > 0)
            {
                int flowerYOffset = this.randomGenerator.nextInt(flowerHeightLimit);
                BlockPos flowerPos = this.chunkPos.add(flowerXOffset, flowerYOffset, flowerZOffset);
                BlockFlower.EnumFlowerType flowerType = biomeGenBaseIn.pickRandomFlower(this.randomGenerator, flowerPos);
                BlockFlower blockFlower = flowerType.getBlockType().getBlock();

                if (blockFlower.getMaterial() != Material.air)
                {
                    this.yellowFlowerGen.setGeneratedBlock(blockFlower, flowerType);
                    this.yellowFlowerGen.generate(this.currentWorld, this.randomGenerator, flowerPos);
                }
            }
        }

        for (int grassIndex = 0; grassIndex < this.grassPerChunk; ++grassIndex)
        {
            int grassXOffset = this.randomGenerator.nextInt(16) + 8;
            int grassZOffset = this.randomGenerator.nextInt(16) + 8;
            int grassHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(grassXOffset, 0, grassZOffset)).getY() * 2;

            if (grassHeightLimit > 0)
            {
                int grassYOffset = this.randomGenerator.nextInt(grassHeightLimit);
                biomeGenBaseIn.getRandomWorldGenForGrass(this.randomGenerator).generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(grassXOffset, grassYOffset, grassZOffset));
            }
        }

        for (int deadBushIndex = 0; deadBushIndex < this.deadBushPerChunk; ++deadBushIndex)
        {
            int deadBushXOffset = this.randomGenerator.nextInt(16) + 8;
            int deadBushZOffset = this.randomGenerator.nextInt(16) + 8;
            int deadBushHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(deadBushXOffset, 0, deadBushZOffset)).getY() * 2;

            if (deadBushHeightLimit > 0)
            {
                int deadBushYOffset = this.randomGenerator.nextInt(deadBushHeightLimit);
                (new WorldGenDeadBush()).generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(deadBushXOffset, deadBushYOffset, deadBushZOffset));
            }
        }

        for (int waterlilyIndex = 0; waterlilyIndex < this.waterlilyPerChunk; ++waterlilyIndex)
        {
            int waterlilyXOffset = this.randomGenerator.nextInt(16) + 8;
            int waterlilyZOffset = this.randomGenerator.nextInt(16) + 8;
            int waterlilyHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(waterlilyXOffset, 0, waterlilyZOffset)).getY() * 2;

            if (waterlilyHeightLimit > 0)
            {
                int waterlilyYOffset = this.randomGenerator.nextInt(waterlilyHeightLimit);
                BlockPos waterlilyPos;
                BlockPos belowWaterlilyPos;

                for (waterlilyPos = this.chunkPos.add(waterlilyXOffset, waterlilyYOffset, waterlilyZOffset); waterlilyPos.getY() > 0; waterlilyPos = belowWaterlilyPos)
                {
                    belowWaterlilyPos = waterlilyPos.down();

                    if (!this.currentWorld.isAirBlock(belowWaterlilyPos))
                    {
                        break;
                    }
                }

                this.waterlilyGen.generate(this.currentWorld, this.randomGenerator, waterlilyPos);
            }
        }

        for (int mushroomIndex = 0; mushroomIndex < this.mushroomsPerChunk; ++mushroomIndex)
        {
            if (this.randomGenerator.nextInt(4) == 0)
            {
                int brownMushroomXOffset = this.randomGenerator.nextInt(16) + 8;
                int brownMushroomZOffset = this.randomGenerator.nextInt(16) + 8;
                BlockPos brownMushroomPos = this.currentWorld.getHeight(this.chunkPos.add(brownMushroomXOffset, 0, brownMushroomZOffset));
                this.mushroomBrownGen.generate(this.currentWorld, this.randomGenerator, brownMushroomPos);
            }

            if (this.randomGenerator.nextInt(8) == 0)
            {
                int redMushroomXOffset = this.randomGenerator.nextInt(16) + 8;
                int redMushroomZOffset = this.randomGenerator.nextInt(16) + 8;
                int redMushroomHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(redMushroomXOffset, 0, redMushroomZOffset)).getY() * 2;

                if (redMushroomHeightLimit > 0)
                {
                    int redMushroomYOffset = this.randomGenerator.nextInt(redMushroomHeightLimit);
                    BlockPos redMushroomPos = this.chunkPos.add(redMushroomXOffset, redMushroomYOffset, redMushroomZOffset);
                    this.mushroomRedGen.generate(this.currentWorld, this.randomGenerator, redMushroomPos);
                }
            }
        }

        if (this.randomGenerator.nextInt(4) == 0)
        {
            int extraBrownMushroomXOffset = this.randomGenerator.nextInt(16) + 8;
            int extraBrownMushroomZOffset = this.randomGenerator.nextInt(16) + 8;
            int extraBrownMushroomHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(extraBrownMushroomXOffset, 0, extraBrownMushroomZOffset)).getY() * 2;

            if (extraBrownMushroomHeightLimit > 0)
            {
                int extraBrownMushroomYOffset = this.randomGenerator.nextInt(extraBrownMushroomHeightLimit);
                this.mushroomBrownGen.generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(extraBrownMushroomXOffset, extraBrownMushroomYOffset, extraBrownMushroomZOffset));
            }
        }

        if (this.randomGenerator.nextInt(8) == 0)
        {
            int extraRedMushroomXOffset = this.randomGenerator.nextInt(16) + 8;
            int extraRedMushroomZOffset = this.randomGenerator.nextInt(16) + 8;
            int extraRedMushroomHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(extraRedMushroomXOffset, 0, extraRedMushroomZOffset)).getY() * 2;

            if (extraRedMushroomHeightLimit > 0)
            {
                int extraRedMushroomYOffset = this.randomGenerator.nextInt(extraRedMushroomHeightLimit);
                this.mushroomRedGen.generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(extraRedMushroomXOffset, extraRedMushroomYOffset, extraRedMushroomZOffset));
            }
        }

        for (int reedIndex = 0; reedIndex < this.reedsPerChunk; ++reedIndex)
        {
            int reedXOffset = this.randomGenerator.nextInt(16) + 8;
            int reedZOffset = this.randomGenerator.nextInt(16) + 8;
            int reedHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(reedXOffset, 0, reedZOffset)).getY() * 2;

            if (reedHeightLimit > 0)
            {
                int reedYOffset = this.randomGenerator.nextInt(reedHeightLimit);
                this.reedGen.generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(reedXOffset, reedYOffset, reedZOffset));
            }
        }

        for (int extraReedIndex = 0; extraReedIndex < 10; ++extraReedIndex)
        {
            int extraReedXOffset = this.randomGenerator.nextInt(16) + 8;
            int extraReedZOffset = this.randomGenerator.nextInt(16) + 8;
            int extraReedHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(extraReedXOffset, 0, extraReedZOffset)).getY() * 2;

            if (extraReedHeightLimit > 0)
            {
                int extraReedYOffset = this.randomGenerator.nextInt(extraReedHeightLimit);
                this.reedGen.generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(extraReedXOffset, extraReedYOffset, extraReedZOffset));
            }
        }

        if (this.randomGenerator.nextInt(32) == 0)
        {
            int pumpkinXOffset = this.randomGenerator.nextInt(16) + 8;
            int pumpkinZOffset = this.randomGenerator.nextInt(16) + 8;
            int pumpkinHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(pumpkinXOffset, 0, pumpkinZOffset)).getY() * 2;

            if (pumpkinHeightLimit > 0)
            {
                int pumpkinYOffset = this.randomGenerator.nextInt(pumpkinHeightLimit);
                (new WorldGenPumpkin()).generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(pumpkinXOffset, pumpkinYOffset, pumpkinZOffset));
            }
        }

        for (int cactusIndex = 0; cactusIndex < this.cactiPerChunk; ++cactusIndex)
        {
            int cactusXOffset = this.randomGenerator.nextInt(16) + 8;
            int cactusZOffset = this.randomGenerator.nextInt(16) + 8;
            int cactusHeightLimit = this.currentWorld.getHeight(this.chunkPos.add(cactusXOffset, 0, cactusZOffset)).getY() * 2;

            if (cactusHeightLimit > 0)
            {
                int cactusYOffset = this.randomGenerator.nextInt(cactusHeightLimit);
                this.cactusGen.generate(this.currentWorld, this.randomGenerator, this.chunkPos.add(cactusXOffset, cactusYOffset, cactusZOffset));
            }
        }

        if (this.generateLakes)
        {
            for (int waterLakeIndex = 0; waterLakeIndex < 50; ++waterLakeIndex)
            {
                int waterLakeXOffset = this.randomGenerator.nextInt(16) + 8;
                int waterLakeZOffset = this.randomGenerator.nextInt(16) + 8;
                int waterLakeHeightLimit = this.randomGenerator.nextInt(248) + 8;

                if (waterLakeHeightLimit > 0)
                {
                    int waterLakeYOffset = this.randomGenerator.nextInt(waterLakeHeightLimit);
                    BlockPos waterLakePos = this.chunkPos.add(waterLakeXOffset, waterLakeYOffset, waterLakeZOffset);
                    (new WorldGenLiquids(Blocks.flowing_water)).generate(this.currentWorld, this.randomGenerator, waterLakePos);
                }
            }

            for (int lavaLakeIndex = 0; lavaLakeIndex < 20; ++lavaLakeIndex)
            {
                int lavaLakeXOffset = this.randomGenerator.nextInt(16) + 8;
                int lavaLakeZOffset = this.randomGenerator.nextInt(16) + 8;
                int lavaLakeYOffset = this.randomGenerator.nextInt(this.randomGenerator.nextInt(this.randomGenerator.nextInt(240) + 8) + 8);
                BlockPos lavaLakePos = this.chunkPos.add(lavaLakeXOffset, lavaLakeYOffset, lavaLakeZOffset);
                (new WorldGenLiquids(Blocks.flowing_lava)).generate(this.currentWorld, this.randomGenerator, lavaLakePos);
            }
        }
    }

    protected void genStandardOre1(int blockCount, WorldGenerator generator, int minHeight, int maxHeight)
    {
        if (maxHeight < minHeight)
        {
            int originalMinHeight = minHeight;
            minHeight = maxHeight;
            maxHeight = originalMinHeight;
        }
        else if (maxHeight == minHeight)
        {
            if (minHeight < 255)
            {
                ++maxHeight;
            }
            else
            {
                --minHeight;
            }
        }

        for (int oreIndex = 0; oreIndex < blockCount; ++oreIndex)
        {
            BlockPos orePos = this.chunkPos.add(this.randomGenerator.nextInt(16), this.randomGenerator.nextInt(maxHeight - minHeight) + minHeight, this.randomGenerator.nextInt(16));
            generator.generate(this.currentWorld, this.randomGenerator, orePos);
        }
    }

    protected void genStandardOre2(int blockCount, WorldGenerator generator, int centerHeight, int spread)
    {
        for (int oreIndex = 0; oreIndex < blockCount; ++oreIndex)
        {
            BlockPos orePos = this.chunkPos.add(this.randomGenerator.nextInt(16), this.randomGenerator.nextInt(spread) + this.randomGenerator.nextInt(spread) + centerHeight - spread, this.randomGenerator.nextInt(16));
            generator.generate(this.currentWorld, this.randomGenerator, orePos);
        }
    }

    protected void generateOres()
    {
        this.genStandardOre1(this.chunkProviderSettings.dirtCount, this.dirtGen, this.chunkProviderSettings.dirtMinHeight, this.chunkProviderSettings.dirtMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.gravelCount, this.gravelGen, this.chunkProviderSettings.gravelMinHeight, this.chunkProviderSettings.gravelMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.dioriteCount, this.dioriteGen, this.chunkProviderSettings.dioriteMinHeight, this.chunkProviderSettings.dioriteMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.graniteCount, this.graniteGen, this.chunkProviderSettings.graniteMinHeight, this.chunkProviderSettings.graniteMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.andesiteCount, this.andesiteGen, this.chunkProviderSettings.andesiteMinHeight, this.chunkProviderSettings.andesiteMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.coalCount, this.coalGen, this.chunkProviderSettings.coalMinHeight, this.chunkProviderSettings.coalMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.ironCount, this.ironGen, this.chunkProviderSettings.ironMinHeight, this.chunkProviderSettings.ironMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.goldCount, this.goldGen, this.chunkProviderSettings.goldMinHeight, this.chunkProviderSettings.goldMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.redstoneCount, this.redstoneGen, this.chunkProviderSettings.redstoneMinHeight, this.chunkProviderSettings.redstoneMaxHeight);
        this.genStandardOre1(this.chunkProviderSettings.diamondCount, this.diamondGen, this.chunkProviderSettings.diamondMinHeight, this.chunkProviderSettings.diamondMaxHeight);
        this.genStandardOre2(this.chunkProviderSettings.lapisCount, this.lapisGen, this.chunkProviderSettings.lapisCenterHeight, this.chunkProviderSettings.lapisSpread);
    }
}
