package net.minecraft.world.gen.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class MapGenVillage extends MapGenStructure
{
    public static final List<BiomeGenBase> villageSpawnBiomes = Arrays.<BiomeGenBase>asList(new BiomeGenBase[] {BiomeGenBase.plains, BiomeGenBase.desert, BiomeGenBase.savanna});
    private int terrainType;
    private int spacing;
    private int separation;

    public MapGenVillage()
    {
        this.spacing = 32;
        this.separation = 8;
    }

    public MapGenVillage(Map<String, String> settings)
    {
        this();

        for (Entry<String, String> entry : settings.entrySet())
        {
            if (((String)entry.getKey()).equals("size"))
            {
                this.terrainType = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.terrainType, 0);
            }
            else if (((String)entry.getKey()).equals("distance"))
            {
                this.spacing = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.spacing, this.separation + 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Village";
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
        Random random = this.worldObj.setRandomSeed(regionX, regionZ, 10387312);
        int candidateChunkX = regionX * this.spacing;
        int candidateChunkZ = regionZ * this.spacing;
        candidateChunkX = candidateChunkX + random.nextInt(this.spacing - this.separation);
        candidateChunkZ = candidateChunkZ + random.nextInt(this.spacing - this.separation);

        if (originalChunkX == candidateChunkX && originalChunkZ == candidateChunkZ)
        {
            boolean isValidBiome = this.worldObj.getWorldChunkManager().areBiomesViable(originalChunkX * 16 + 8, originalChunkZ * 16 + 8, 0, villageSpawnBiomes);

            if (isValidBiome)
            {
                return true;
            }
        }

        return false;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenVillage.Start(this.worldObj, this.rand, chunkX, chunkZ, this.terrainType);
    }

    public static class Start extends StructureStart
    {
        private boolean hasMoreThanTwoComponents;

        public Start()
        {
        }

        public Start(World worldIn, Random rand, int x, int z, int size)
        {
            super(x, z);
            List<StructureVillagePieces.PieceWeight> pieceWeights = StructureVillagePieces.getStructureVillageWeightedPieceList(rand, size);
            StructureVillagePieces.Start startPiece = new StructureVillagePieces.Start(worldIn.getWorldChunkManager(), 0, rand, (x << 4) + 2, (z << 4) + 2, pieceWeights, size);
            this.components.add(startPiece);
            startPiece.buildComponent(startPiece, this.components, rand);
            List<StructureComponent> primaryPieces = startPiece.primaryPieces;
            List<StructureComponent> pathPieces = startPiece.pathPieces;

            while (!primaryPieces.isEmpty() || !pathPieces.isEmpty())
            {
                if (primaryPieces.isEmpty())
                {
                    int pieceIndex = rand.nextInt(pathPieces.size());
                    StructureComponent piece = (StructureComponent)pathPieces.remove(pieceIndex);
                    piece.buildComponent(startPiece, this.components, rand);
                }
                else
                {
                    int pieceIndex = rand.nextInt(primaryPieces.size());
                    StructureComponent piece = (StructureComponent)primaryPieces.remove(pieceIndex);
                    piece.buildComponent(startPiece, this.components, rand);
                }
            }

            this.updateBoundingBox();
            int nonRoadComponentCount = 0;

            for (StructureComponent piece : this.components)
            {
                if (!(piece instanceof StructureVillagePieces.Road))
                {
                    ++nonRoadComponentCount;
                }
            }

            this.hasMoreThanTwoComponents = nonRoadComponentCount > 2;
        }

        public boolean isSizeableStructure()
        {
            return this.hasMoreThanTwoComponents;
        }

        public void writeToNBT(NBTTagCompound tagCompound)
        {
            super.writeToNBT(tagCompound);
            tagCompound.setBoolean("Valid", this.hasMoreThanTwoComponents);
        }

        public void readFromNBT(NBTTagCompound tagCompound)
        {
            super.readFromNBT(tagCompound);
            this.hasMoreThanTwoComponents = tagCompound.getBoolean("Valid");
        }
    }
}
