package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class StructureOceanMonument extends MapGenStructure
{
    private int spacing;
    private int separation;
    public static final List<BiomeGenBase> VALID_SURROUNDING_BIOMES = Arrays.<BiomeGenBase>asList(new BiomeGenBase[] {BiomeGenBase.ocean, BiomeGenBase.deepOcean, BiomeGenBase.river, BiomeGenBase.frozenOcean, BiomeGenBase.frozenRiver});
    private static final List<BiomeGenBase.SpawnListEntry> GUARDIAN_SPAWN_LIST = Lists.<BiomeGenBase.SpawnListEntry>newArrayList();

    public StructureOceanMonument()
    {
        this.spacing = 32;
        this.separation = 5;
    }

    public StructureOceanMonument(Map<String, String> settings)
    {
        this();

        for (Entry<String, String> entry : settings.entrySet())
        {
            if (((String)entry.getKey()).equals("spacing"))
            {
                this.spacing = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.spacing, 1);
            }
            else if (((String)entry.getKey()).equals("separation"))
            {
                this.separation = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.separation, 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Monument";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int originalChunkX = chunkX;
        int originalChunkZ = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.spacing - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.spacing - 1;
        }

        int regionX = chunkX / this.spacing;
        int regionZ = chunkZ / this.spacing;
        Random random = this.worldObj.setRandomSeed(regionX, regionZ, 10387313);
        int candidateChunkX = regionX * this.spacing;
        int candidateChunkZ = regionZ * this.spacing;
        candidateChunkX = candidateChunkX + (random.nextInt(this.spacing - this.separation) + random.nextInt(this.spacing - this.separation)) / 2;
        candidateChunkZ = candidateChunkZ + (random.nextInt(this.spacing - this.separation) + random.nextInt(this.spacing - this.separation)) / 2;

        if (originalChunkX == candidateChunkX && originalChunkZ == candidateChunkZ)
        {
            if (this.worldObj.getWorldChunkManager().getBiomeGenerator(new BlockPos(originalChunkX * 16 + 8, 64, originalChunkZ * 16 + 8), (BiomeGenBase)null) != BiomeGenBase.deepOcean)
            {
                return false;
            }

            boolean isSurroundedByValidBiomes = this.worldObj.getWorldChunkManager().areBiomesViable(originalChunkX * 16 + 8, originalChunkZ * 16 + 8, 29, VALID_SURROUNDING_BIOMES);

            if (isSurroundedByValidBiomes)
            {
                return true;
            }
        }

        return false;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new StructureOceanMonument.StartMonument(this.worldObj, this.rand, chunkX, chunkZ);
    }

    public List<BiomeGenBase.SpawnListEntry> getScatteredFeatureSpawnList()
    {
        return GUARDIAN_SPAWN_LIST;
    }

    static
    {
        GUARDIAN_SPAWN_LIST.add(new BiomeGenBase.SpawnListEntry(EntityGuardian.class, 1, 2, 4));
    }

    public static class StartMonument extends StructureStart
    {
        private Set<ChunkCoordIntPair> processedChunks = Sets.<ChunkCoordIntPair>newHashSet();
        private boolean hasGeneratedPieces;

        public StartMonument()
        {
        }

        public StartMonument(World worldIn, Random rand, int chunkX, int chunkZ)
        {
            super(chunkX, chunkZ);
            this.initializeStructurePieces(worldIn, rand, chunkX, chunkZ);
        }

        private void initializeStructurePieces(World worldIn, Random rand, int chunkX, int chunkZ)
        {
            rand.setSeed(worldIn.getSeed());
            long xSeed = rand.nextLong();
            long zSeed = rand.nextLong();
            long chunkSeedX = (long)chunkX * xSeed;
            long chunkSeedZ = (long)chunkZ * zSeed;
            rand.setSeed(chunkSeedX ^ chunkSeedZ ^ worldIn.getSeed());
            int startX = chunkX * 16 + 8 - 29;
            int startZ = chunkZ * 16 + 8 - 29;
            EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(rand);
            this.components.add(new StructureOceanMonumentPieces.MonumentBuilding(rand, startX, startZ, facing));
            this.updateBoundingBox();
            this.hasGeneratedPieces = true;
        }

        public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb)
        {
            if (!this.hasGeneratedPieces)
            {
                this.components.clear();
                this.initializeStructurePieces(worldIn, rand, this.getChunkPosX(), this.getChunkPosZ());
            }

            super.generateStructure(worldIn, rand, structurebb);
        }

        public boolean isChunkInStructure(ChunkCoordIntPair pair)
        {
            return this.processedChunks.contains(pair) ? false : super.isChunkInStructure(pair);
        }

        public void markChunkProcessed(ChunkCoordIntPair pair)
        {
            super.markChunkProcessed(pair);
            this.processedChunks.add(pair);
        }

        public void writeToNBT(NBTTagCompound tagCompound)
        {
            super.writeToNBT(tagCompound);
            NBTTagList nBTTagList = new NBTTagList();

            for (ChunkCoordIntPair chunkCoordIntPair : this.processedChunks)
            {
                NBTTagCompound nBTTagCompound = new NBTTagCompound();
                nBTTagCompound.setInteger("X", chunkCoordIntPair.chunkXPos);
                nBTTagCompound.setInteger("Z", chunkCoordIntPair.chunkZPos);
                nBTTagList.appendTag(nBTTagCompound);
            }

            tagCompound.setTag("Processed", nBTTagList);
        }

        public void readFromNBT(NBTTagCompound tagCompound)
        {
            super.readFromNBT(tagCompound);

            if (tagCompound.hasKey("Processed", 9))
            {
                NBTTagList processedTagList = tagCompound.getTagList("Processed", 10);

                for (int chunkIndex = 0; chunkIndex < processedTagList.tagCount(); ++chunkIndex)
                {
                    NBTTagCompound chunkTag = processedTagList.getCompoundTagAt(chunkIndex);
                    this.processedChunks.add(new ChunkCoordIntPair(chunkTag.getInteger("X"), chunkTag.getInteger("Z")));
                }
            }
        }
    }
}
