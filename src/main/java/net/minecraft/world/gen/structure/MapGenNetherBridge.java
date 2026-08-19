package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class MapGenNetherBridge extends MapGenStructure
{
    private List<BiomeGenBase.SpawnListEntry> spawnList = Lists.<BiomeGenBase.SpawnListEntry>newArrayList();

    public MapGenNetherBridge()
    {
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new BiomeGenBase.SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
    }

    public String getStructureName()
    {
        return "Fortress";
    }

    public List<BiomeGenBase.SpawnListEntry> getSpawnList()
    {
        return this.spawnList;
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int regionX = chunkX >> 4;
        int regionZ = chunkZ >> 4;
        this.rand.setSeed((long)(regionX ^ regionZ << 4) ^ this.worldObj.getSeed());
        this.rand.nextInt();
        return this.rand.nextInt(3) != 0 ? false : (chunkX != (regionX << 4) + 4 + this.rand.nextInt(8) ? false : chunkZ == (regionZ << 4) + 4 + this.rand.nextInt(8));
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenNetherBridge.Start(this.worldObj, this.rand, chunkX, chunkZ);
    }

    public static class Start extends StructureStart
    {
        public Start()
        {
        }

        public Start(World worldIn, Random rand, int chunkX, int chunkZ)
        {
            super(chunkX, chunkZ);
            StructureNetherBridgePieces.Start startPiece = new StructureNetherBridgePieces.Start(rand, (chunkX << 4) + 2, (chunkZ << 4) + 2);
            this.components.add(startPiece);
            startPiece.buildComponent(startPiece, this.components, rand);
            List<StructureComponent> pendingPieces = startPiece.pendingPieces;

            while (!pendingPieces.isEmpty())
            {
                int pieceIndex = rand.nextInt(pendingPieces.size());
                StructureComponent piece = (StructureComponent)pendingPieces.remove(pieceIndex);
                piece.buildComponent(startPiece, this.components, rand);
            }

            this.updateBoundingBox();
            this.setRandomHeight(worldIn, rand, 48, 70);
        }
    }
}
