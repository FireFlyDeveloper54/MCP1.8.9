package net.minecraft.village;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;

public class VillageSiege
{
    private World worldObj;
    private boolean hasSetupSiege;
    private int siegeState = -1;
    private int remainingSpawns;
    private int nextSpawnTimer;
    private Village theVillage;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    public VillageSiege(World worldIn)
    {
        this.worldObj = worldIn;
    }

    public void tick()
    {
        if (this.worldObj.isDaytime())
        {
            this.siegeState = 0;
        }
        else if (this.siegeState != 2)
        {
            if (this.siegeState == 0)
            {
                float f = this.worldObj.getCelestialAngle(0.0F);

                if ((double)f < 0.5D || (double)f > 0.501D)
                {
                    return;
                }

                this.siegeState = this.worldObj.rand.nextInt(10) == 0 ? 1 : 2;
                this.hasSetupSiege = false;

                if (this.siegeState == 2)
                {
                    return;
                }
            }

            if (this.siegeState != -1)
            {
                if (!this.hasSetupSiege)
                {
                    if (!this.trySetupSiege())
                    {
                        return;
                    }

                    this.hasSetupSiege = true;
                }

                if (this.nextSpawnTimer > 0)
                {
                    --this.nextSpawnTimer;
                }
                else
                {
                    this.nextSpawnTimer = 2;

                    if (this.remainingSpawns > 0)
                    {
                        this.spawnZombie();
                        --this.remainingSpawns;
                    }
                    else
                    {
                        this.siegeState = 2;
                    }
                }
            }
        }
    }

    private boolean trySetupSiege()
    {
        List<EntityPlayer> list = this.worldObj.playerEntities;
        Iterator<EntityPlayer> iterator = list.iterator();

        while (true)
        {
            if (!iterator.hasNext())
            {
                return false;
            }

            EntityPlayer entityplayer = iterator.next();

            if (!entityplayer.isSpectator())
            {
                this.theVillage = this.worldObj.getVillageCollection().getNearestVillage(new BlockPos(entityplayer), 1);

                if (this.theVillage != null && this.theVillage.getNumVillageDoors() >= 10 && this.theVillage.getTicksSinceLastDoorAdding() >= 20 && this.theVillage.getNumVillagers() >= 20)
                {
                    BlockPos blockpos = this.theVillage.getCenter();
                    float f = (float)this.theVillage.getVillageRadius();
                    boolean flag = false;

                    for (int i = 0; i < 10; ++i)
                    {
                        float spawnAngle = this.worldObj.rand.nextFloat() * (float)Math.PI * 2.0F;
                        this.spawnX = blockpos.getX() + (int)((double)(MathHelper.cos(spawnAngle) * f) * 0.9D);
                        this.spawnY = blockpos.getY();
                        this.spawnZ = blockpos.getZ() + (int)((double)(MathHelper.sin(spawnAngle) * f) * 0.9D);
                        flag = false;

                        for (Village village : this.worldObj.getVillageCollection().getVillageList())
                        {
                            if (village != this.theVillage && village.isBlockPosWithinSqVillageRadius(new BlockPos(this.spawnX, this.spawnY, this.spawnZ)))
                            {
                                flag = true;
                                break;
                            }
                        }

                        if (!flag)
                        {
                            break;
                        }
                    }

                    if (flag)
                    {
                        return false;
                    }

                    Vec3 vec3 = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));

                    if (vec3 != null)
                    {
                        break;
                    }
                }
            }
        }

        this.nextSpawnTimer = 0;
        this.remainingSpawns = 20;
        return true;
    }

    private boolean spawnZombie()
    {
        Vec3 localValue = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));

        if (localValue == null)
        {
            return false;
        }
        else
        {
            EntityZombie entityZombie;

            try
            {
                entityZombie = new EntityZombie(this.worldObj);
                entityZombie.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos(entityZombie)), (IEntityLivingData)null);
                entityZombie.setVillager(false);
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
                return false;
            }

            entityZombie.setLocationAndAngles(localValue.xCoord, localValue.yCoord, localValue.zCoord, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
            this.worldObj.spawnEntityInWorld(entityZombie);
            BlockPos blockPos = this.theVillage.getCenter();
            entityZombie.setHomePosAndDistance(blockPos, this.theVillage.getVillageRadius());
            return true;
        }
    }

    private Vec3 findRandomSpawnPos(BlockPos siegeCenter)
    {
        for (int i = 0; i < 10; ++i)
        {
            BlockPos blockpos = siegeCenter.add(this.worldObj.rand.nextInt(16) - 8, this.worldObj.rand.nextInt(6) - 3, this.worldObj.rand.nextInt(16) - 8);

            if (this.theVillage.isBlockPosWithinSqVillageRadius(blockpos) && SpawnerAnimals.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, this.worldObj, blockpos))
            {
                return new Vec3((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            }
        }

        return null;
    }
}
