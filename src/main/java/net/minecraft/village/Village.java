package net.minecraft.village;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Village
{
    private World worldObj;
    private final List<VillageDoorInfo> villageDoorInfoList = Lists.<VillageDoorInfo>newArrayList();
    private BlockPos centerHelper = BlockPos.ORIGIN;
    private BlockPos center = BlockPos.ORIGIN;
    private int villageRadius;
    private int lastAddDoorTimestamp;
    private int tickCounter;
    private int numVillagers;
    private int noBreedTicks;
    private TreeMap<String, Integer> playerReputation = new TreeMap<String, Integer>();
    private List<Village.VillageAggressor> villageAgressors = Lists.<Village.VillageAggressor>newArrayList();
    private int numIronGolems;

    public Village()
    {
    }

    public Village(World worldIn)
    {
        this.worldObj = worldIn;
    }

    public void setWorld(World worldIn)
    {
        this.worldObj = worldIn;
    }

    public void tick(int tickCounterIn)
    {
        this.tickCounter = tickCounterIn;
        this.removeDeadAndOutOfRangeDoors();
        this.removeDeadAndOldAgressors();

        if (tickCounterIn % 20 == 0)
        {
            this.updateNumVillagers();
        }

        if (tickCounterIn % 30 == 0)
        {
            this.updateNumIronGolems();
        }

        int i = this.numVillagers / 10;

        if (this.numIronGolems < i && this.villageDoorInfoList.size() > 20 && this.worldObj.rand.nextInt(7000) == 0)
        {
            Vec3 localValue = this.findRandomSpawnPos(this.center, 2, 4, 2);

            if (localValue != null)
            {
                EntityIronGolem entityIronGolem = new EntityIronGolem(this.worldObj);
                entityIronGolem.setPosition(localValue.xCoord, localValue.yCoord, localValue.zCoord);
                this.worldObj.spawnEntityInWorld(entityIronGolem);
                ++this.numIronGolems;
            }
        }
    }

    private Vec3 findRandomSpawnPos(BlockPos center, int sizeX, int sizeY, int sizeZ)
    {
        for (int i = 0; i < 10; ++i)
        {
            BlockPos blockPos = center.add(this.worldObj.rand.nextInt(16) - 8, this.worldObj.rand.nextInt(6) - 3, this.worldObj.rand.nextInt(16) - 8);

            if (this.isBlockPosWithinSqVillageRadius(blockPos) && this.isAreaClearAround(new BlockPos(sizeX, sizeY, sizeZ), blockPos))
            {
                return new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
            }
        }

        return null;
    }

    private boolean isAreaClearAround(BlockPos size, BlockPos pos)
    {
        if (!World.doesBlockHaveSolidTopSurface(this.worldObj, pos.down()))
        {
            return false;
        }
        else
        {
            int i = pos.getX() - size.getX() / 2;
            int j = pos.getZ() - size.getZ() / 2;
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int k = i; k < i + size.getX(); ++k)
            {
                for (int l = pos.getY(); l < pos.getY() + size.getY(); ++l)
                {
                    for (int index = j; index < j + size.getZ(); ++index)
                    {
                        if (this.worldObj.getBlockState(blockPos.set(k, l, index)).getBlock().isNormalCube())
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private void updateNumIronGolems()
    {
        List<EntityIronGolem> list = this.worldObj.<EntityIronGolem>getEntitiesWithinAABB(EntityIronGolem.class, new AxisAlignedBB((double)(this.center.getX() - this.villageRadius), (double)(this.center.getY() - 4), (double)(this.center.getZ() - this.villageRadius), (double)(this.center.getX() + this.villageRadius), (double)(this.center.getY() + 4), (double)(this.center.getZ() + this.villageRadius)));
        this.numIronGolems = list.size();
    }

    private void updateNumVillagers()
    {
        List<EntityVillager> list = this.worldObj.<EntityVillager>getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB((double)(this.center.getX() - this.villageRadius), (double)(this.center.getY() - 4), (double)(this.center.getZ() - this.villageRadius), (double)(this.center.getX() + this.villageRadius), (double)(this.center.getY() + 4), (double)(this.center.getZ() + this.villageRadius)));
        this.numVillagers = list.size();

        if (this.numVillagers == 0)
        {
            this.playerReputation.clear();
        }
    }

    public BlockPos getCenter()
    {
        return this.center;
    }

    public int getVillageRadius()
    {
        return this.villageRadius;
    }

    public int getNumVillageDoors()
    {
        return this.villageDoorInfoList.size();
    }

    public int getTicksSinceLastDoorAdding()
    {
        return this.tickCounter - this.lastAddDoorTimestamp;
    }

    public int getNumVillagers()
    {
        return this.numVillagers;
    }

    public boolean isBlockPosWithinSqVillageRadius(BlockPos pos)
    {
        return this.center.distanceSq(pos) < (double)(this.villageRadius * this.villageRadius);
    }

    public List<VillageDoorInfo> getVillageDoorInfoList()
    {
        return this.villageDoorInfoList;
    }

    public VillageDoorInfo getNearestDoor(BlockPos pos)
    {
        VillageDoorInfo villageDoorInfo = null;
        int i = Integer.MAX_VALUE;

        for (VillageDoorInfo villagedoorinfo1 : this.villageDoorInfoList)
        {
            int j = villagedoorinfo1.getDistanceToDoorBlockSq(pos);

            if (j < i)
            {
                villageDoorInfo = villagedoorinfo1;
                i = j;
            }
        }

        return villageDoorInfo;
    }

    public VillageDoorInfo getDoorInfo(BlockPos pos)
    {
        VillageDoorInfo villageDoorInfo = null;
        int i = Integer.MAX_VALUE;

        for (VillageDoorInfo villagedoorinfo1 : this.villageDoorInfoList)
        {
            int j = villagedoorinfo1.getDistanceToDoorBlockSq(pos);

            if (j > 256)
            {
                j = j * 1000;
            }
            else
            {
                j = villagedoorinfo1.getDoorOpeningRestrictionCounter();
            }

            if (j < i)
            {
                villageDoorInfo = villagedoorinfo1;
                i = j;
            }
        }

        return villageDoorInfo;
    }

    public VillageDoorInfo getExistedDoor(BlockPos doorBlock)
    {
        if (this.center.distanceSq(doorBlock) > (double)(this.villageRadius * this.villageRadius))
        {
            return null;
        }
        else
        {
            for (VillageDoorInfo villageDoorInfo : this.villageDoorInfoList)
            {
                if (villageDoorInfo.getDoorBlockPos().getX() == doorBlock.getX() && villageDoorInfo.getDoorBlockPos().getZ() == doorBlock.getZ() && Math.abs(villageDoorInfo.getDoorBlockPos().getY() - doorBlock.getY()) <= 1)
                {
                    return villageDoorInfo;
                }
            }

            return null;
        }
    }

    public void addVillageDoorInfo(VillageDoorInfo doorInfo)
    {
        this.villageDoorInfoList.add(doorInfo);
        this.centerHelper = this.centerHelper.add(doorInfo.getDoorBlockPos());
        this.updateVillageRadiusAndCenter();
        this.lastAddDoorTimestamp = doorInfo.getInsidePosY();
    }

    public boolean isAnnihilated()
    {
        return this.villageDoorInfoList.isEmpty();
    }

    public void addOrRenewAgressor(EntityLivingBase entitylivingbaseIn)
    {
        for (Village.VillageAggressor village$villageaggressor : this.villageAgressors)
        {
            if (village$villageaggressor.agressor == entitylivingbaseIn)
            {
                village$villageaggressor.agressionTime = this.tickCounter;
                return;
            }
        }

        this.villageAgressors.add(new Village.VillageAggressor(entitylivingbaseIn, this.tickCounter));
    }

    public EntityLivingBase findNearestVillageAggressor(EntityLivingBase entitylivingbaseIn)
    {
        double doubleValue = Double.MAX_VALUE;
        Village.VillageAggressor village$villageaggressor = null;

        for (int i = 0; i < this.villageAgressors.size(); ++i)
        {
            Village.VillageAggressor village$villageaggressor1 = (Village.VillageAggressor)this.villageAgressors.get(i);
            double doubleValue2 = village$villageaggressor1.agressor.getDistanceSqToEntity(entitylivingbaseIn);

            if (doubleValue2 <= doubleValue)
            {
                village$villageaggressor = village$villageaggressor1;
                doubleValue = doubleValue2;
            }
        }

        return village$villageaggressor != null ? village$villageaggressor.agressor : null;
    }

    public EntityPlayer getNearestTargetPlayer(EntityLivingBase villageDefender)
    {
        double doubleValue = Double.MAX_VALUE;
        EntityPlayer entityPlayer = null;

        for (String s : this.playerReputation.keySet())
        {
            if (this.isPlayerReputationTooLow(s))
            {
                EntityPlayer entityplayer1 = this.worldObj.getPlayerEntityByName(s);

                if (entityplayer1 != null)
                {
                    double doubleValue2 = entityplayer1.getDistanceSqToEntity(villageDefender);

                    if (doubleValue2 <= doubleValue)
                    {
                        entityPlayer = entityplayer1;
                        doubleValue = doubleValue2;
                    }
                }
            }
        }

        return entityPlayer;
    }

    private void removeDeadAndOldAgressors()
    {
        Iterator<Village.VillageAggressor> iterator = this.villageAgressors.iterator();

        while (iterator.hasNext())
        {
            Village.VillageAggressor village$villageaggressor = (Village.VillageAggressor)iterator.next();

            if (!village$villageaggressor.agressor.isEntityAlive() || Math.abs(this.tickCounter - village$villageaggressor.agressionTime) > 300)
            {
                iterator.remove();
            }
        }
    }

    private void removeDeadAndOutOfRangeDoors()
    {
        boolean flag = false;
        boolean flag1 = this.worldObj.rand.nextInt(50) == 0;
        Iterator<VillageDoorInfo> iterator = this.villageDoorInfoList.iterator();

        while (iterator.hasNext())
        {
            VillageDoorInfo villageDoorInfo = (VillageDoorInfo)iterator.next();

            if (flag1)
            {
                villageDoorInfo.resetDoorOpeningRestrictionCounter();
            }

            if (!this.isWoodDoor(villageDoorInfo.getDoorBlockPos()) || Math.abs(this.tickCounter - villageDoorInfo.getInsidePosY()) > 1200)
            {
                this.centerHelper = this.centerHelper.subtract(villageDoorInfo.getDoorBlockPos());
                flag = true;
                villageDoorInfo.setIsDetachedFromVillageFlag(true);
                iterator.remove();
            }
        }

        if (flag)
        {
            this.updateVillageRadiusAndCenter();
        }
    }

    private boolean isWoodDoor(BlockPos pos)
    {
        Block block = this.worldObj.getBlockState(pos).getBlock();
        return block instanceof BlockDoor ? block.getMaterial() == Material.wood : false;
    }

    private void updateVillageRadiusAndCenter()
    {
        int i = this.villageDoorInfoList.size();

        if (i == 0)
        {
            this.center = new BlockPos(0, 0, 0);
            this.villageRadius = 0;
        }
        else
        {
            this.center = new BlockPos(this.centerHelper.getX() / i, this.centerHelper.getY() / i, this.centerHelper.getZ() / i);
            int j = 0;

            for (VillageDoorInfo villagedoorinfo : this.villageDoorInfoList)
            {
                j = Math.max(villagedoorinfo.getDistanceToDoorBlockSq(this.center), j);
            }

            this.villageRadius = Math.max(32, (int)MathHelper.fastSqrt_double((double)j) + 1);
        }
    }

    public int getReputationForPlayer(String playerName)
    {
        Integer integer = this.playerReputation.get(playerName);
        return integer != null ? integer.intValue() : 0;
    }

    public int setReputationForPlayer(String playerName, int reputationDelta)
    {
        int i = this.getReputationForPlayer(playerName);
        int j = MathHelper.clamp_int(i + reputationDelta, -30, 10);
        this.playerReputation.put(playerName, Integer.valueOf(j));
        return j;
    }

    public boolean isPlayerReputationTooLow(String playerName)
    {
        return this.getReputationForPlayer(playerName) <= -15;
    }

    public void readVillageDataFromNBT(NBTTagCompound compound)
    {
        this.numVillagers = compound.getInteger("PopSize");
        this.villageRadius = compound.getInteger("Radius");
        this.numIronGolems = compound.getInteger("Golems");
        this.lastAddDoorTimestamp = compound.getInteger("Stable");
        this.tickCounter = compound.getInteger("Tick");
        this.noBreedTicks = compound.getInteger("MTick");
        this.center = new BlockPos(compound.getInteger("CX"), compound.getInteger("CY"), compound.getInteger("CZ"));
        this.centerHelper = new BlockPos(compound.getInteger("ACX"), compound.getInteger("ACY"), compound.getInteger("ACZ"));
        NBTTagList nBTTagList = compound.getTagList("Doors", 10);

        for (int i = 0; i < nBTTagList.tagCount(); ++i)
        {
            NBTTagCompound nBTTagCompound = nBTTagList.getCompoundTagAt(i);
            VillageDoorInfo villageDoorInfo = new VillageDoorInfo(new BlockPos(nBTTagCompound.getInteger("X"), nBTTagCompound.getInteger("Y"), nBTTagCompound.getInteger("Z")), nBTTagCompound.getInteger("IDX"), nBTTagCompound.getInteger("IDZ"), nBTTagCompound.getInteger("TS"));
            this.villageDoorInfoList.add(villageDoorInfo);
        }

        NBTTagList nbttaglist1 = compound.getTagList("Players", 10);

        for (int j = 0; j < nbttaglist1.tagCount(); ++j)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist1.getCompoundTagAt(j);

            if (nbttagcompound1.hasKey("UUID"))
            {
                PlayerProfileCache playerProfileCache = MinecraftServer.getServer().getPlayerProfileCache();
                GameProfile gameProfile = playerProfileCache.getProfileByUUID(UUID.fromString(nbttagcompound1.getString("UUID")));

                if (gameProfile != null)
                {
                    this.playerReputation.put(gameProfile.getName(), Integer.valueOf(nbttagcompound1.getInteger("S")));
                }
            }
            else
            {
                this.playerReputation.put(nbttagcompound1.getString("Name"), Integer.valueOf(nbttagcompound1.getInteger("S")));
            }
        }
    }

    public void writeVillageDataToNBT(NBTTagCompound compound)
    {
        compound.setInteger("PopSize", this.numVillagers);
        compound.setInteger("Radius", this.villageRadius);
        compound.setInteger("Golems", this.numIronGolems);
        compound.setInteger("Stable", this.lastAddDoorTimestamp);
        compound.setInteger("Tick", this.tickCounter);
        compound.setInteger("MTick", this.noBreedTicks);
        compound.setInteger("CX", this.center.getX());
        compound.setInteger("CY", this.center.getY());
        compound.setInteger("CZ", this.center.getZ());
        compound.setInteger("ACX", this.centerHelper.getX());
        compound.setInteger("ACY", this.centerHelper.getY());
        compound.setInteger("ACZ", this.centerHelper.getZ());
        NBTTagList nBTTagList = new NBTTagList();

        for (VillageDoorInfo villageDoorInfo : this.villageDoorInfoList)
        {
            NBTTagCompound nBTTagCompound = new NBTTagCompound();
            nBTTagCompound.setInteger("X", villageDoorInfo.getDoorBlockPos().getX());
            nBTTagCompound.setInteger("Y", villageDoorInfo.getDoorBlockPos().getY());
            nBTTagCompound.setInteger("Z", villageDoorInfo.getDoorBlockPos().getZ());
            nBTTagCompound.setInteger("IDX", villageDoorInfo.getInsideOffsetX());
            nBTTagCompound.setInteger("IDZ", villageDoorInfo.getInsideOffsetZ());
            nBTTagCompound.setInteger("TS", villageDoorInfo.getInsidePosY());
            nBTTagList.appendTag(nBTTagCompound);
        }

        compound.setTag("Doors", nBTTagList);
        NBTTagList nbttaglist1 = new NBTTagList();

        for (String s : this.playerReputation.keySet())
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            PlayerProfileCache playerProfileCache = MinecraftServer.getServer().getPlayerProfileCache();
            GameProfile gameProfile = playerProfileCache.getGameProfileForUsername(s);

            if (gameProfile != null)
            {
                nbttagcompound1.setString("UUID", gameProfile.getId().toString());
                nbttagcompound1.setInteger("S", this.playerReputation.get(s).intValue());
                nbttaglist1.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Players", nbttaglist1);
    }

    public void endMatingSeason()
    {
        this.noBreedTicks = this.tickCounter;
    }

    public boolean isMatingSeason()
    {
        return this.noBreedTicks == 0 || this.tickCounter - this.noBreedTicks >= 3600;
    }

    public void setDefaultPlayerReputation(int reputation)
    {
        for (String s : this.playerReputation.keySet())
        {
            this.setReputationForPlayer(s, reputation);
        }
    }

    class VillageAggressor
    {
        public EntityLivingBase agressor;
        public int agressionTime;

        VillageAggressor(EntityLivingBase aggressorIn, int aggressionTimeIn)
        {
            this.agressor = aggressorIn;
            this.agressionTime = aggressionTimeIn;
        }
    }
}
