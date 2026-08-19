package net.minecraft.world.biome;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeEndDecorator extends BiomeDecorator
{
    protected WorldGenerator spikeGen = new WorldGenSpikes(Blocks.end_stone);

    protected void genDecorations(BiomeGenBase biomeGenBaseIn)
    {
        this.generateOres();

        if (this.randomGenerator.nextInt(5) == 0)
        {
            int xOffset = this.randomGenerator.nextInt(16) + 8;
            int zOffset = this.randomGenerator.nextInt(16) + 8;
            this.spikeGen.generate(this.currentWorld, this.randomGenerator, this.currentWorld.getTopSolidOrLiquidBlock(this.chunkPos.add(xOffset, 0, zOffset)));
        }

        if (this.chunkPos.getX() == 0 && this.chunkPos.getZ() == 0)
        {
            EntityDragon entityDragon = new EntityDragon(this.currentWorld);
            entityDragon.setLocationAndAngles(0.0D, 128.0D, 0.0D, this.randomGenerator.nextFloat() * 360.0F, 0.0F);
            this.currentWorld.spawnEntityInWorld(entityDragon);
        }
    }
}
