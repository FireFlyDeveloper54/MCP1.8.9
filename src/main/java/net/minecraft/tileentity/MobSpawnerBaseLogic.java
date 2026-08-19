package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;

public abstract class MobSpawnerBaseLogic
{
    private int spawnDelay = 20;
    private String mobID = "Pig";
    private final List<MobSpawnerBaseLogic.WeightedRandomMinecart> minecartToSpawn = Lists.<MobSpawnerBaseLogic.WeightedRandomMinecart>newArrayList();
    private MobSpawnerBaseLogic.WeightedRandomMinecart randomEntity;
    private double mobRotation;
    private double prevMobRotation;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private Entity cachedEntity;
    private int maxNearbyEntities = 6;
    private int activatingRangeFromPlayer = 16;
    private int spawnRange = 4;

    private String getEntityNameToSpawn()
    {
        if (this.getRandomEntity() == null)
        {
            if (this.mobID != null && this.mobID.equals("Minecart"))
            {
                this.mobID = "MinecartRideable";
            }

            return this.mobID;
        }
        else
        {
            return this.getRandomEntity().entityType;
        }
    }

    public void setEntityName(String name)
    {
        this.mobID = name;
    }

    private boolean isActivated()
    {
        BlockPos blockPos = this.getSpawnerPosition();
        return this.getSpawnerWorld().isAnyPlayerWithinRangeAt((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, (double)this.activatingRangeFromPlayer);
    }

    public void updateSpawner()
    {
        if (this.isActivated())
        {
            BlockPos blockPos = this.getSpawnerPosition();

            if (this.getSpawnerWorld().isRemote)
            {
                double secondDoubleValue = (double)((float)blockPos.getX() + this.getSpawnerWorld().rand.nextFloat());
                double thirdDoubleValue = (double)((float)blockPos.getY() + this.getSpawnerWorld().rand.nextFloat());
                double fourthDoubleValue = (double)((float)blockPos.getZ() + this.getSpawnerWorld().rand.nextFloat());
                this.getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, secondDoubleValue, thirdDoubleValue, fourthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                this.getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, secondDoubleValue, thirdDoubleValue, fourthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);

                if (this.spawnDelay > 0)
                {
                    --this.spawnDelay;
                }

                this.prevMobRotation = this.mobRotation;
                this.mobRotation = (this.mobRotation + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0D;
            }
            else
            {
                if (this.spawnDelay == -1)
                {
                    this.resetTimer();
                }

                if (this.spawnDelay > 0)
                {
                    --this.spawnDelay;
                    return;
                }

                boolean flag = false;

                for (int i = 0; i < this.spawnCount; ++i)
                {
                    Entity entity = EntityList.createEntityByName(this.getEntityNameToSpawn(), this.getSpawnerWorld());

                    if (entity == null)
                    {
                        return;
                    }

                    int j = this.getSpawnerWorld().getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)(blockPos.getY() + 1), (double)(blockPos.getZ() + 1))).expand((double)this.spawnRange, (double)this.spawnRange, (double)this.spawnRange)).size();

                    if (j >= this.maxNearbyEntities)
                    {
                        this.resetTimer();
                        return;
                    }

                    double doubleValue = (double)blockPos.getX() + (this.getSpawnerWorld().rand.nextDouble() - this.getSpawnerWorld().rand.nextDouble()) * (double)this.spawnRange + 0.5D;
                    double yCoordinate2 = (double)(blockPos.getY() + this.getSpawnerWorld().rand.nextInt(3) - 1);
                    double zCoordinate2 = (double)blockPos.getZ() + (this.getSpawnerWorld().rand.nextDouble() - this.getSpawnerWorld().rand.nextDouble()) * (double)this.spawnRange + 0.5D;
                    EntityLiving entityLiving = entity instanceof EntityLiving ? (EntityLiving)entity : null;
                    entity.setLocationAndAngles(doubleValue, yCoordinate2, zCoordinate2, this.getSpawnerWorld().rand.nextFloat() * 360.0F, 0.0F);

                    if (entityLiving == null || entityLiving.getCanSpawnHere() && entityLiving.isNotColliding())
                    {
                        this.spawnNewEntity(entity, true);
                        this.getSpawnerWorld().playAuxSFX(2004, blockPos, 0);

                        if (entityLiving != null)
                        {
                            entityLiving.spawnExplosionParticle();
                        }

                        flag = true;
                    }
                }

                if (flag)
                {
                    this.resetTimer();
                }
            }
        }
    }

    private Entity spawnNewEntity(Entity entityIn, boolean spawn)
    {
        if (this.getRandomEntity() != null)
        {
            NBTTagCompound nBTTagCompound = new NBTTagCompound();
            entityIn.writeToNBTOptional(nBTTagCompound);

            for (String s : this.getRandomEntity().nbtData.getKeySet())
            {
                NBTBase nBTBase = this.getRandomEntity().nbtData.getTag(s);
                nBTTagCompound.setTag(s, nBTBase.copy());
            }

            entityIn.readFromNBT(nBTTagCompound);

            if (entityIn.worldObj != null && spawn)
            {
                entityIn.worldObj.spawnEntityInWorld(entityIn);
            }

            NBTTagCompound nbttagcompound2;

            for (Entity entity = entityIn; nBTTagCompound.hasKey("Riding", 10); nBTTagCompound = nbttagcompound2)
            {
                nbttagcompound2 = nBTTagCompound.getCompoundTag("Riding");
                Entity entity1 = EntityList.createEntityByName(nbttagcompound2.getString("id"), entityIn.worldObj);

                if (entity1 != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    entity1.writeToNBTOptional(nbttagcompound1);

                    for (String text2 : nbttagcompound2.getKeySet())
                    {
                        NBTBase nbtbase1 = nbttagcompound2.getTag(text2);
                        nbttagcompound1.setTag(text2, nbtbase1.copy());
                    }

                    entity1.readFromNBT(nbttagcompound1);
                    entity1.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                    if (entityIn.worldObj != null && spawn)
                    {
                        entityIn.worldObj.spawnEntityInWorld(entity1);
                    }

                    entity.mountEntity(entity1);
                }

                entity = entity1;
            }
        }
        else if (entityIn instanceof EntityLivingBase && entityIn.worldObj != null && spawn)
        {
            if (entityIn instanceof EntityLiving)
            {
                ((EntityLiving)entityIn).onInitialSpawn(entityIn.worldObj.getDifficultyForLocation(new BlockPos(entityIn)), (IEntityLivingData)null);
            }

            entityIn.worldObj.spawnEntityInWorld(entityIn);
        }

        return entityIn;
    }

    private void resetTimer()
    {
        if (this.maxSpawnDelay <= this.minSpawnDelay)
        {
            this.spawnDelay = this.minSpawnDelay;
        }
        else
        {
            int i = this.maxSpawnDelay - this.minSpawnDelay;
            this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(i);
        }

        if (this.minecartToSpawn.size() > 0)
        {
            this.setRandomEntity((MobSpawnerBaseLogic.WeightedRandomMinecart)WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.minecartToSpawn));
        }

        this.broadcastEvent(1);
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        this.mobID = nbt.getString("EntityId");
        this.spawnDelay = nbt.getShort("Delay");
        this.minecartToSpawn.clear();

        if (nbt.hasKey("SpawnPotentials", 9))
        {
            NBTTagList nBTTagList = nbt.getTagList("SpawnPotentials", 10);

            for (int i = 0; i < nBTTagList.tagCount(); ++i)
            {
                this.minecartToSpawn.add(new MobSpawnerBaseLogic.WeightedRandomMinecart(nBTTagList.getCompoundTagAt(i)));
            }
        }

        if (nbt.hasKey("SpawnData", 10))
        {
            this.setRandomEntity(new MobSpawnerBaseLogic.WeightedRandomMinecart(nbt.getCompoundTag("SpawnData"), this.mobID));
        }
        else
        {
            this.setRandomEntity((MobSpawnerBaseLogic.WeightedRandomMinecart)null);
        }

        if (nbt.hasKey("MinSpawnDelay", 99))
        {
            this.minSpawnDelay = nbt.getShort("MinSpawnDelay");
            this.maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
            this.spawnCount = nbt.getShort("SpawnCount");
        }

        if (nbt.hasKey("MaxNearbyEntities", 99))
        {
            this.maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
            this.activatingRangeFromPlayer = nbt.getShort("RequiredPlayerRange");
        }

        if (nbt.hasKey("SpawnRange", 99))
        {
            this.spawnRange = nbt.getShort("SpawnRange");
        }

        if (this.getSpawnerWorld() != null)
        {
            this.cachedEntity = null;
        }
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        String s = this.getEntityNameToSpawn();

        if (!StringUtils.isNullOrEmpty(s))
        {
            nbt.setString("EntityId", s);
            nbt.setShort("Delay", (short)this.spawnDelay);
            nbt.setShort("MinSpawnDelay", (short)this.minSpawnDelay);
            nbt.setShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
            nbt.setShort("SpawnCount", (short)this.spawnCount);
            nbt.setShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
            nbt.setShort("RequiredPlayerRange", (short)this.activatingRangeFromPlayer);
            nbt.setShort("SpawnRange", (short)this.spawnRange);

            if (this.getRandomEntity() != null)
            {
                nbt.setTag("SpawnData", this.getRandomEntity().nbtData.copy());
            }

            if (this.getRandomEntity() != null || this.minecartToSpawn.size() > 0)
            {
                NBTTagList nBTTagList = new NBTTagList();

                if (this.minecartToSpawn.size() > 0)
                {
                    for (MobSpawnerBaseLogic.WeightedRandomMinecart mobspawnerbaselogic$weightedrandomminecart : this.minecartToSpawn)
                    {
                        nBTTagList.appendTag(mobspawnerbaselogic$weightedrandomminecart.toNBT());
                    }
                }
                else
                {
                    nBTTagList.appendTag(this.getRandomEntity().toNBT());
                }

                nbt.setTag("SpawnPotentials", nBTTagList);
            }
        }
    }

    public Entity getCachedEntity(World worldIn)
    {
        if (this.cachedEntity == null)
        {
            Entity entity = EntityList.createEntityByName(this.getEntityNameToSpawn(), worldIn);

            if (entity != null)
            {
                entity = this.spawnNewEntity(entity, false);
                this.cachedEntity = entity;
            }
        }

        return this.cachedEntity;
    }

    public boolean setDelayToMin(int delay)
    {
        if (delay == 1 && this.getSpawnerWorld().isRemote)
        {
            this.spawnDelay = this.minSpawnDelay;
            return true;
        }
        else
        {
            return false;
        }
    }

    private MobSpawnerBaseLogic.WeightedRandomMinecart getRandomEntity()
    {
        return this.randomEntity;
    }

    public void setRandomEntity(MobSpawnerBaseLogic.WeightedRandomMinecart randomEntity)
    {
        this.randomEntity = randomEntity;
    }

    public abstract void broadcastEvent(int id);

    public abstract World getSpawnerWorld();

    public abstract BlockPos getSpawnerPosition();

    public double getMobRotation()
    {
        return this.mobRotation;
    }

    public double getPrevMobRotation()
    {
        return this.prevMobRotation;
    }

    public class WeightedRandomMinecart extends WeightedRandom.Item
    {
        private final NBTTagCompound nbtData;
        private final String entityType;

        public WeightedRandomMinecart(NBTTagCompound tagCompound)
        {
            this(tagCompound.getCompoundTag("Properties"), tagCompound.getString("Type"), tagCompound.getInteger("Weight"));
        }

        public WeightedRandomMinecart(NBTTagCompound tagCompound, String type)
        {
            this(tagCompound, type, 1);
        }

        private WeightedRandomMinecart(NBTTagCompound tagCompound, String type, int weight)
        {
            super(weight);

            if (type.equals("Minecart"))
            {
                if (tagCompound != null)
                {
                    type = EntityMinecart.EnumMinecartType.byNetworkID(tagCompound.getInteger("Type")).getName();
                }
                else
                {
                    type = "MinecartRideable";
                }
            }

            this.nbtData = tagCompound;
            this.entityType = type;
        }

        public NBTTagCompound toNBT()
        {
            NBTTagCompound nBTTagCompound = new NBTTagCompound();
            nBTTagCompound.setTag("Properties", this.nbtData);
            nBTTagCompound.setString("Type", this.entityType);
            nBTTagCompound.setInteger("Weight", this.itemWeight);
            return nBTTagCompound;
        }
    }
}
