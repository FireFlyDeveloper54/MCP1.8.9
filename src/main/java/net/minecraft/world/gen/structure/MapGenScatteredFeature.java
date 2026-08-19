package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class MapGenScatteredFeature extends MapGenStructure
{
    private static final List<BiomeGenBase> allowedBiomes = Arrays.<BiomeGenBase>asList(new BiomeGenBase[] {BiomeGenBase.desert, BiomeGenBase.desertHills, BiomeGenBase.jungle, BiomeGenBase.jungleHills, BiomeGenBase.swampland});
    private List<BiomeGenBase.SpawnListEntry> scatteredFeatureSpawnList;
    private int maxDistanceBetweenScatteredFeatures;
    private int minDistanceBetweenScatteredFeatures;

    public MapGenScatteredFeature()
    {
        this.scatteredFeatureSpawnList = Lists.<BiomeGenBase.SpawnListEntry>newArrayList();
        this.maxDistanceBetweenScatteredFeatures = 32;
        this.minDistanceBetweenScatteredFeatures = 8;
        this.scatteredFeatureSpawnList.add(new BiomeGenBase.SpawnListEntry(EntityWitch.class, 1, 1, 1));
    }

    public MapGenScatteredFeature(Map<String, String> configOptions)
    {
        this();

        for (Entry<String, String> entry : configOptions.entrySet())
        {
            if (((String)entry.getKey()).equals("distance"))
            {
                this.maxDistanceBetweenScatteredFeatures = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.maxDistanceBetweenScatteredFeatures, this.minDistanceBetweenScatteredFeatures + 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Temple";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int originalChunkX = chunkX;
        int originalChunkZ = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        int regionX = chunkX / this.maxDistanceBetweenScatteredFeatures;
        int regionZ = chunkZ / this.maxDistanceBetweenScatteredFeatures;
        Random random = this.worldObj.setRandomSeed(regionX, regionZ, 14357617);
        regionX = regionX * this.maxDistanceBetweenScatteredFeatures;
        regionZ = regionZ * this.maxDistanceBetweenScatteredFeatures;
        regionX = regionX + random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        regionZ = regionZ + random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (originalChunkX == regionX && originalChunkZ == regionZ)
        {
            BiomeGenBase biome = this.worldObj.getWorldChunkManager().getBiomeGenerator(new BlockPos(originalChunkX * 16 + 8, 0, originalChunkZ * 16 + 8));

            if (biome == null)
            {
                return false;
            }

            for (BiomeGenBase allowedBiome : allowedBiomes)
            {
                if (biome == allowedBiome)
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenScatteredFeature.Start(this.worldObj, this.rand, chunkX, chunkZ);
    }

    public boolean isSwampHutAt(BlockPos pos)
    {
        StructureStart structureStart = this.findContainingStructure(pos);

        if (structureStart != null && structureStart instanceof MapGenScatteredFeature.Start && !structureStart.components.isEmpty())
        {
            StructureComponent firstComponent = (StructureComponent)structureStart.components.getFirst();
            return firstComponent instanceof ComponentScatteredFeaturePieces.SwampHut;
        }
        else
        {
            return false;
        }
    }

    public List<BiomeGenBase.SpawnListEntry> getScatteredFeatureSpawnList()
    {
        return this.scatteredFeatureSpawnList;
    }

    public static class Start extends StructureStart
    {
        public Start()
        {
        }

        public Start(World worldIn, Random rand, int chunkX, int chunkZ)
        {
            super(chunkX, chunkZ);
            BiomeGenBase biome = worldIn.getBiomeGenForCoords(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));

            if (biome != BiomeGenBase.jungle && biome != BiomeGenBase.jungleHills)
            {
                if (biome == BiomeGenBase.swampland)
                {
                    ComponentScatteredFeaturePieces.SwampHut swampHut = new ComponentScatteredFeaturePieces.SwampHut(rand, chunkX * 16, chunkZ * 16);
                    this.components.add(swampHut);
                }
                else if (biome == BiomeGenBase.desert || biome == BiomeGenBase.desertHills)
                {
                    ComponentScatteredFeaturePieces.DesertPyramid desertPyramid = new ComponentScatteredFeaturePieces.DesertPyramid(rand, chunkX * 16, chunkZ * 16);
                    this.components.add(desertPyramid);
                }
            }
            else
            {
                ComponentScatteredFeaturePieces.JunglePyramid junglePyramid = new ComponentScatteredFeaturePieces.JunglePyramid(rand, chunkX * 16, chunkZ * 16);
                this.components.add(junglePyramid);
            }

            this.updateBoundingBox();
        }
    }
}
