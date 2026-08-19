package net.minecraft.world.gen.structure;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public abstract class MapGenStructure extends MapGenBase
{
    private MapGenStructureData structureData;
    protected Map<Long, StructureStart> structureMap = Maps.<Long, StructureStart>newHashMap();

    public abstract String getStructureName();

    protected final void recursiveGenerate(World worldIn, final int chunkX, final int chunkZ, int originalChunkX, int originalChunkZ, ChunkPrimer chunkPrimerIn)
    {
        this.initializeStructureData(worldIn);

        if (!this.structureMap.containsKey(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ))))
        {
            this.rand.nextInt();

            try
            {
                if (this.canSpawnStructureAtCoords(chunkX, chunkZ))
                {
                    StructureStart structureStart = this.getStructureStart(chunkX, chunkZ);
                    this.structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)), structureStart);
                    this.setStructureStart(chunkX, chunkZ, structureStart);
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception preparing structure feature");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Feature being prepared");
                crashReportCategory.addCrashSectionCallable("Is feature chunk", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return MapGenStructure.this.canSpawnStructureAtCoords(chunkX, chunkZ) ? "True" : "False";
                    }
                });
                crashReportCategory.addCrashSection("Chunk location", chunkX + "," + chunkZ);
                crashReportCategory.addCrashSectionCallable("Chunk pos hash", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
                    }
                });
                crashReportCategory.addCrashSectionCallable("Structure type", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return MapGenStructure.this.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashReport);
            }
        }
    }

    public boolean generateStructure(World worldIn, Random randomIn, ChunkCoordIntPair chunkCoord)
    {
        this.initializeStructureData(worldIn);
        int chunkBlockX = (chunkCoord.chunkXPos << 4) + 8;
        int chunkBlockZ = (chunkCoord.chunkZPos << 4) + 8;
        boolean generatedAnyStructure = false;

        for (StructureStart structureStart : this.structureMap.values())
        {
            if (structureStart.isSizeableStructure() && structureStart.isChunkInStructure(chunkCoord) && structureStart.getBoundingBox().intersectsWith(chunkBlockX, chunkBlockZ, chunkBlockX + 15, chunkBlockZ + 15))
            {
                structureStart.generateStructure(worldIn, randomIn, new StructureBoundingBox(chunkBlockX, chunkBlockZ, chunkBlockX + 15, chunkBlockZ + 15));
                structureStart.markChunkProcessed(chunkCoord);
                generatedAnyStructure = true;
                this.setStructureStart(structureStart.getChunkPosX(), structureStart.getChunkPosZ(), structureStart);
            }
        }

        return generatedAnyStructure;
    }

    public boolean isPositionInAnyStructure(BlockPos pos)
    {
        this.initializeStructureData(this.worldObj);
        return this.findContainingStructure(pos) != null;
    }

    protected StructureStart findContainingStructure(BlockPos pos)
    {
        label24:

        for (StructureStart structureStart : this.structureMap.values())
        {
            if (structureStart.isSizeableStructure() && structureStart.getBoundingBox().isVecInside(pos))
            {
                Iterator<StructureComponent> componentIterator = structureStart.getComponents().iterator();

                while (true)
                {
                    if (!componentIterator.hasNext())
                    {
                        continue label24;
                    }

                    StructureComponent structureComponent = (StructureComponent)componentIterator.next();

                    if (structureComponent.getBoundingBox().isVecInside(pos))
                    {
                        break;
                    }
                }

                return structureStart;
            }
        }

        return null;
    }

    public boolean isPositionInStructure(World worldIn, BlockPos pos)
    {
        this.initializeStructureData(worldIn);

        for (StructureStart structureStart : this.structureMap.values())
        {
            if (structureStart.isSizeableStructure() && structureStart.getBoundingBox().isVecInside(pos))
            {
                return true;
            }
        }

        return false;
    }

    public BlockPos getClosestStrongholdPos(World worldIn, BlockPos pos)
    {
        this.worldObj = worldIn;
        this.initializeStructureData(worldIn);
        this.rand.setSeed(worldIn.getSeed());
        long xSeedMultiplier = this.rand.nextLong();
        long zSeedMultiplier = this.rand.nextLong();
        long chunkSeedX = (long)(pos.getX() >> 4) * xSeedMultiplier;
        long chunkSeedZ = (long)(pos.getZ() >> 4) * zSeedMultiplier;
        this.rand.setSeed(chunkSeedX ^ chunkSeedZ ^ worldIn.getSeed());
        this.recursiveGenerate(worldIn, pos.getX() >> 4, pos.getZ() >> 4, 0, 0, (ChunkPrimer)null);
        double closestDistanceSq = Double.MAX_VALUE;
        BlockPos closestStructurePos = null;

        for (StructureStart structureStart : this.structureMap.values())
        {
            if (structureStart.isSizeableStructure())
            {
                StructureComponent structureComponent = (StructureComponent)structureStart.getComponents().get(0);
                BlockPos centerPos = structureComponent.getBoundingBoxCenter();
                double distanceSq = centerPos.distanceSq(pos);

                if (distanceSq < closestDistanceSq)
                {
                    closestDistanceSq = distanceSq;
                    closestStructurePos = centerPos;
                }
            }
        }

        if (closestStructurePos != null)
        {
            return closestStructurePos;
        }
        else
        {
            List<BlockPos> fallbackCoords = this.getCoordList();

            if (fallbackCoords != null)
            {
                BlockPos closestFallbackPos = null;

                for (BlockPos fallbackPos : fallbackCoords)
                {
                    double distanceSq = fallbackPos.distanceSq(pos);

                    if (distanceSq < closestDistanceSq)
                    {
                        closestDistanceSq = distanceSq;
                        closestFallbackPos = fallbackPos;
                    }
                }

                return closestFallbackPos;
            }
            else
            {
                return null;
            }
        }
    }

    protected List<BlockPos> getCoordList()
    {
        return null;
    }

    private void initializeStructureData(World worldIn)
    {
        if (this.structureData == null)
        {
            this.structureData = (MapGenStructureData)worldIn.loadItemData(MapGenStructureData.class, this.getStructureName());

            if (this.structureData == null)
            {
                this.structureData = new MapGenStructureData(this.getStructureName());
                worldIn.setItemData(this.getStructureName(), this.structureData);
            }
            else
            {
                NBTTagCompound structureDataTag = this.structureData.getTagCompound();

                for (String structureKey : structureDataTag.getKeySet())
                {
                    NBTBase structureTag = structureDataTag.getTag(structureKey);

                    if (structureTag.getId() == 10)
                    {
                        NBTTagCompound startTag = (NBTTagCompound)structureTag;

                        if (startTag.hasKey("ChunkX") && startTag.hasKey("ChunkZ"))
                        {
                            int chunkX = startTag.getInteger("ChunkX");
                            int chunkZ = startTag.getInteger("ChunkZ");
                            StructureStart structureStart = MapGenStructureIO.getStructureStart(startTag, worldIn);

                            if (structureStart != null)
                            {
                                this.structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)), structureStart);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setStructureStart(int chunkX, int chunkZ, StructureStart start)
    {
        this.structureData.writeInstance(start.writeStructureComponentsToNBT(chunkX, chunkZ), chunkX, chunkZ);
        this.structureData.markDirty();
    }

    protected abstract boolean canSpawnStructureAtCoords(int chunkX, int chunkZ);

    protected abstract StructureStart getStructureStart(int chunkX, int chunkZ);
}
