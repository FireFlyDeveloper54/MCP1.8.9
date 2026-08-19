package net.minecraft.village;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSavedData;

public class VillageCollection extends WorldSavedData
{
    private World worldObj;
    private final List<BlockPos> villagerPositionsList = Lists.<BlockPos>newArrayList();
    private final List<VillageDoorInfo> newDoors = Lists.<VillageDoorInfo>newArrayList();
    private final List<Village> villageList = Lists.<Village>newArrayList();
    private int tickCounter;

    public VillageCollection(String name)
    {
        super(name);
    }

    public VillageCollection(World worldIn)
    {
        super(fileNameForProvider(worldIn.provider));
        this.worldObj = worldIn;
        this.markDirty();
    }

    public void setWorldsForAll(World worldIn)
    {
        this.worldObj = worldIn;

        for (Village village : this.villageList)
        {
            village.setWorld(worldIn);
        }
    }

    public void addToVillagerPositionList(BlockPos pos)
    {
        if (this.villagerPositionsList.size() <= 64)
        {
            if (!this.positionInList(pos))
            {
                this.villagerPositionsList.add(pos);
            }
        }
    }

    public void tick()
    {
        ++this.tickCounter;

        for (Village village : this.villageList)
        {
            village.tick(this.tickCounter);
        }

        this.removeAnnihilatedVillages();
        this.dropOldestVillagerPosition();
        this.addNewDoorsToVillageOrCreateVillage();

        if (this.tickCounter % 400 == 0)
        {
            this.markDirty();
        }
    }

    private void removeAnnihilatedVillages()
    {
        Iterator<Village> iterator = this.villageList.iterator();

        while (iterator.hasNext())
        {
            Village village = (Village)iterator.next();

            if (village.isAnnihilated())
            {
                iterator.remove();
                this.markDirty();
            }
        }
    }

    public List<Village> getVillageList()
    {
        return this.villageList;
    }

    public Village getNearestVillage(BlockPos doorBlock, int radius)
    {
        Village village = null;
        double nearestDistanceSq = 3.4028234663852886E38D;

        for (Village village1 : this.villageList)
        {
            double villageDistanceSq = village1.getCenter().distanceSq(doorBlock);

            if (villageDistanceSq < nearestDistanceSq)
            {
                float maxDistance = (float)(radius + village1.getVillageRadius());

                if (villageDistanceSq <= (double)(maxDistance * maxDistance))
                {
                    village = village1;
                    nearestDistanceSq = villageDistanceSq;
                }
            }
        }

        return village;
    }

    private void dropOldestVillagerPosition()
    {
        if (!this.villagerPositionsList.isEmpty())
        {
            this.addDoorsAround((BlockPos)this.villagerPositionsList.remove(0));
        }
    }

    private void addNewDoorsToVillageOrCreateVillage()
    {
        for (int i = 0; i < this.newDoors.size(); ++i)
        {
            VillageDoorInfo villageDoorInfo = (VillageDoorInfo)this.newDoors.get(i);
            Village village = this.getNearestVillage(villageDoorInfo.getDoorBlockPos(), 32);

            if (village == null)
            {
                village = new Village(this.worldObj);
                this.villageList.add(village);
                this.markDirty();
            }

            village.addVillageDoorInfo(villageDoorInfo);
        }

        this.newDoors.clear();
    }

    private void addDoorsAround(BlockPos central)
    {
        int xRadius = 16;
        int yRadius = 4;
        int zRadius = 16;

        for (int xOffset = -xRadius; xOffset < xRadius; ++xOffset)
        {
            for (int yOffset = -yRadius; yOffset < yRadius; ++yOffset)
            {
                for (int zOffset = -zRadius; zOffset < zRadius; ++zOffset)
                {
                    BlockPos blockPos = central.add(xOffset, yOffset, zOffset);

                    if (this.isWoodDoor(blockPos))
                    {
                        VillageDoorInfo villageDoorInfo = this.checkDoorExistence(blockPos);

                        if (villageDoorInfo == null)
                        {
                            this.addToNewDoorsList(blockPos);
                        }
                        else
                        {
                            villageDoorInfo.setLastActivityTimestamp(this.tickCounter);
                        }
                    }
                }
            }
        }
    }

    private VillageDoorInfo checkDoorExistence(BlockPos doorBlock)
    {
        for (VillageDoorInfo villageDoorInfo : this.newDoors)
        {
            if (villageDoorInfo.getDoorBlockPos().getX() == doorBlock.getX() && villageDoorInfo.getDoorBlockPos().getZ() == doorBlock.getZ() && Math.abs(villageDoorInfo.getDoorBlockPos().getY() - doorBlock.getY()) <= 1)
            {
                return villageDoorInfo;
            }
        }

        for (Village village : this.villageList)
        {
            VillageDoorInfo villagedoorinfo1 = village.getExistedDoor(doorBlock);

            if (villagedoorinfo1 != null)
            {
                return villagedoorinfo1;
            }
        }

        return null;
    }

    private void addToNewDoorsList(BlockPos doorBlock)
    {
        EnumFacing enumfacing = BlockDoor.getFacing(this.worldObj, doorBlock);
        EnumFacing enumfacing1 = enumfacing.getOpposite();
        int i = this.countBlocksCanSeeSky(doorBlock, enumfacing, 5);
        int j = this.countBlocksCanSeeSky(doorBlock, enumfacing1, i + 1);

        if (i != j)
        {
            this.newDoors.add(new VillageDoorInfo(doorBlock, i < j ? enumfacing : enumfacing1, this.tickCounter));
        }
    }

    private int countBlocksCanSeeSky(BlockPos centerPos, EnumFacing direction, int limitation)
    {
        int i = 0;

        for (int j = 1; j <= 5; ++j)
        {
            if (this.worldObj.canSeeSky(centerPos.offset(direction, j)))
            {
                ++i;

                if (i >= limitation)
                {
                    return i;
                }
            }
        }

        return i;
    }

    private boolean positionInList(BlockPos pos)
    {
        for (BlockPos blockPos : this.villagerPositionsList)
        {
            if (blockPos.equals(pos))
            {
                return true;
            }
        }

        return false;
    }

    private boolean isWoodDoor(BlockPos doorPos)
    {
        Block block = this.worldObj.getBlockState(doorPos).getBlock();
        return block instanceof BlockDoor ? block.getMaterial() == Material.wood : false;
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        this.tickCounter = nbt.getInteger("Tick");
        NBTTagList nBTTagList = nbt.getTagList("Villages", 10);

        for (int i = 0; i < nBTTagList.tagCount(); ++i)
        {
            NBTTagCompound nBTTagCompound = nBTTagList.getCompoundTagAt(i);
            Village village = new Village();
            village.readVillageDataFromNBT(nBTTagCompound);
            this.villageList.add(village);
        }
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("Tick", this.tickCounter);
        NBTTagList nBTTagList = new NBTTagList();

        for (Village village : this.villageList)
        {
            NBTTagCompound nBTTagCompound = new NBTTagCompound();
            village.writeVillageDataToNBT(nBTTagCompound);
            nBTTagList.appendTag(nBTTagCompound);
        }

        nbt.setTag("Villages", nBTTagList);
    }

    public static String fileNameForProvider(WorldProvider provider)
    {
        return "villages" + provider.getInternalNameSuffix();
    }
}
