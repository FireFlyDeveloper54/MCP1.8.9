package net.minecraft.world.gen.structure;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

public abstract class StructureStart
{
    protected LinkedList<StructureComponent> components = new LinkedList();
    protected StructureBoundingBox boundingBox;
    private int chunkPosX;
    private int chunkPosZ;

    public StructureStart()
    {
    }

    public StructureStart(int chunkX, int chunkZ)
    {
        this.chunkPosX = chunkX;
        this.chunkPosZ = chunkZ;
    }

    public StructureBoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    public LinkedList<StructureComponent> getComponents()
    {
        return this.components;
    }

    public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb)
    {
        Iterator<StructureComponent> iterator = this.components.iterator();

        while (iterator.hasNext())
        {
            StructureComponent structureComponent = (StructureComponent)iterator.next();

            if (structureComponent.getBoundingBox().intersectsWith(structurebb) && !structureComponent.addComponentParts(worldIn, rand, structurebb))
            {
                iterator.remove();
            }
        }
    }

    protected void updateBoundingBox()
    {
        this.boundingBox = StructureBoundingBox.getNewBoundingBox();

        for (StructureComponent structureComponent : this.components)
        {
            this.boundingBox.expandTo(structureComponent.getBoundingBox());
        }
    }

    public NBTTagCompound writeStructureComponentsToNBT(int chunkX, int chunkZ)
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        nBTTagCompound.setString("id", MapGenStructureIO.getStructureStartName(this));
        nBTTagCompound.setInteger("ChunkX", chunkX);
        nBTTagCompound.setInteger("ChunkZ", chunkZ);
        nBTTagCompound.setTag("BB", this.boundingBox.toNBTTagIntArray());
        NBTTagList nBTTagList = new NBTTagList();

        for (StructureComponent structureComponent : this.components)
        {
            nBTTagList.appendTag(structureComponent.createStructureBaseNBT());
        }

        nBTTagCompound.setTag("Children", nBTTagList);
        this.writeToNBT(nBTTagCompound);
        return nBTTagCompound;
    }

    public void writeToNBT(NBTTagCompound tagCompound)
    {
    }

    public void readStructureComponentsFromNBT(World worldIn, NBTTagCompound tagCompound)
    {
        this.chunkPosX = tagCompound.getInteger("ChunkX");
        this.chunkPosZ = tagCompound.getInteger("ChunkZ");

        if (tagCompound.hasKey("BB"))
        {
            this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
        }

        NBTTagList childrenTagList = tagCompound.getTagList("Children", 10);

        for (int childIndex = 0; childIndex < childrenTagList.tagCount(); ++childIndex)
        {
            this.components.add(MapGenStructureIO.getStructureComponent(childrenTagList.getCompoundTagAt(childIndex), worldIn));
        }

        this.readFromNBT(tagCompound);
    }

    public void readFromNBT(NBTTagCompound tagCompound)
    {
    }

    protected void markAvailableHeight(World worldIn, Random rand, int verticalOffset)
    {
        int maxHeight = worldIn.getSeaLevel() - verticalOffset;
        int targetY = this.boundingBox.getYSize() + 1;

        if (targetY < maxHeight)
        {
            targetY += rand.nextInt(maxHeight - targetY);
        }

        int yOffset = targetY - this.boundingBox.maxY;
        this.boundingBox.offset(0, yOffset, 0);

        for (StructureComponent structureComponent : this.components)
        {
            structureComponent.offset(0, yOffset, 0);
        }
    }

    protected void setRandomHeight(World worldIn, Random rand, int minY, int maxY)
    {
        int heightRange = maxY - minY + 1 - this.boundingBox.getYSize();
        int targetY = 1;

        if (heightRange > 1)
        {
            targetY = minY + rand.nextInt(heightRange);
        }
        else
        {
            targetY = minY;
        }

        int yOffset = targetY - this.boundingBox.minY;
        this.boundingBox.offset(0, yOffset, 0);

        for (StructureComponent structureComponent : this.components)
        {
            structureComponent.offset(0, yOffset, 0);
        }
    }

    public boolean isSizeableStructure()
    {
        return true;
    }

    public boolean isChunkInStructure(ChunkCoordIntPair pair)
    {
        return true;
    }

    public void markChunkProcessed(ChunkCoordIntPair pair)
    {
    }

    public int getChunkPosX()
    {
        return this.chunkPosX;
    }

    public int getChunkPosZ()
    {
        return this.chunkPosZ;
    }
}
