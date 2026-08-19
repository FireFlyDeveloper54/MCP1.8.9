package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class MapGenStronghold extends MapGenStructure
{
    private List<BiomeGenBase> allowedBiomes;
    private boolean ranBiomeCheck;
    private ChunkCoordIntPair[] structureCoords;
    private double distance;
    private int spread;

    public MapGenStronghold()
    {
        this.structureCoords = new ChunkCoordIntPair[3];
        this.distance = 32.0D;
        this.spread = 3;
        this.allowedBiomes = Lists.<BiomeGenBase>newArrayList();

        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
        {
            if (biome != null && biome.minHeight > 0.0F)
            {
                this.allowedBiomes.add(biome);
            }
        }
    }

    public MapGenStronghold(Map<String, String> configOptions)
    {
        this();

        for (Entry<String, String> entry : configOptions.entrySet())
        {
            if (((String)entry.getKey()).equals("distance"))
            {
                this.distance = MathHelper.parseDoubleWithDefaultAndMax((String)entry.getValue(), this.distance, 1.0D);
            }
            else if (((String)entry.getKey()).equals("count"))
            {
                this.structureCoords = new ChunkCoordIntPair[MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.structureCoords.length, 1)];
            }
            else if (((String)entry.getKey()).equals("spread"))
            {
                this.spread = MathHelper.parseIntWithDefaultAndMax((String)entry.getValue(), this.spread, 1);
            }
        }
    }

    public String getStructureName()
    {
        return "Stronghold";
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (!this.ranBiomeCheck)
        {
            Random random = new Random();
            random.setSeed(this.worldObj.getSeed());
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int ring = 1;

            for (int index = 0; index < this.structureCoords.length; ++index)
            {
                double radius = (1.25D * (double)ring + random.nextDouble()) * this.distance * (double)ring;
                int strongholdChunkX = (int)Math.round(Math.cos(angle) * radius);
                int strongholdChunkZ = (int)Math.round(Math.sin(angle) * radius);
                BlockPos biomePos = this.worldObj.getWorldChunkManager().findBiomePosition((strongholdChunkX << 4) + 8, (strongholdChunkZ << 4) + 8, 112, this.allowedBiomes, random);

                if (biomePos != null)
                {
                    strongholdChunkX = biomePos.getX() >> 4;
                    strongholdChunkZ = biomePos.getZ() >> 4;
                }

                this.structureCoords[index] = new ChunkCoordIntPair(strongholdChunkX, strongholdChunkZ);
                angle += (Math.PI * 2D) * (double)ring / (double)this.spread;

                if (index == this.spread)
                {
                    ring += 2 + random.nextInt(5);
                    this.spread += 1 + random.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        for (ChunkCoordIntPair strongholdCoord : this.structureCoords)
        {
            if (chunkX == strongholdCoord.chunkXPos && chunkZ == strongholdCoord.chunkZPos)
            {
                return true;
            }
        }

        return false;
    }

    protected List<BlockPos> getCoordList()
    {
        List<BlockPos> coords = Lists.<BlockPos>newArrayList();

        for (ChunkCoordIntPair strongholdCoord : this.structureCoords)
        {
            if (strongholdCoord != null)
            {
                coords.add(strongholdCoord.getCenterBlock(64));
            }
        }

        return coords;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        MapGenStronghold.Start start;

        for (start = new MapGenStronghold.Start(this.worldObj, this.rand, chunkX, chunkZ); start.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2)start.getComponents().get(0)).strongholdPortalRoom == null; start = new MapGenStronghold.Start(this.worldObj, this.rand, chunkX, chunkZ))
        {
            ;
        }

        return start;
    }

    public static class Start extends StructureStart
    {
        public Start()
        {
        }

        public Start(World worldIn, Random rand, int chunkX, int chunkZ)
        {
            super(chunkX, chunkZ);
            StructureStrongholdPieces.prepareStructurePieces();
            StructureStrongholdPieces.Stairs2 startPiece = new StructureStrongholdPieces.Stairs2(0, rand, (chunkX << 4) + 2, (chunkZ << 4) + 2);
            this.components.add(startPiece);
            startPiece.buildComponent(startPiece, this.components, rand);
            List<StructureComponent> pendingPieces = startPiece.secondaryPieces;

            while (!pendingPieces.isEmpty())
            {
                int pieceIndex = rand.nextInt(pendingPieces.size());
                StructureComponent piece = (StructureComponent)pendingPieces.remove(pieceIndex);
                piece.buildComponent(startPiece, this.components, rand);
            }

            this.updateBoundingBox();
            this.markAvailableHeight(worldIn, rand, 10);
        }
    }
}
