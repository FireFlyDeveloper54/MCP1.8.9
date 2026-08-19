package net.optifine;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;

public class RandomEntity implements IRandomEntity
{
    private Entity entity;

    public int getId()
    {
        UUID uniqueId = this.entity.getUniqueID();
        long leastSignificantBits = uniqueId.getLeastSignificantBits();
        int positiveId = (int)(leastSignificantBits & 2147483647L);
        return positiveId;
    }

    public BlockPos getSpawnPosition()
    {
        return this.entity.getDataWatcher().spawnPosition;
    }

    public BiomeGenBase getSpawnBiome()
    {
        return this.entity.getDataWatcher().spawnBiome;
    }

    public String getName()
    {
        return this.entity.hasCustomName() ? this.entity.getCustomNameTag() : null;
    }

    public int getHealth()
    {
        if (!(this.entity instanceof EntityLiving))
        {
            return 0;
        }
        else
        {
            EntityLiving entityLiving = (EntityLiving)this.entity;
            return (int)entityLiving.getHealth();
        }
    }

    public int getMaxHealth()
    {
        if (!(this.entity instanceof EntityLiving))
        {
            return 0;
        }
        else
        {
            EntityLiving entityLiving = (EntityLiving)this.entity;
            return (int)entityLiving.getMaxHealth();
        }
    }

    public Entity getEntity()
    {
        return this.entity;
    }

    public void setEntity(Entity entity)
    {
        this.entity = entity;
    }
}
