package net.optifine;

import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;

public class NextTickHashSet extends TreeSet
{
    private LongHashMap longHashMap = new LongHashMap();
    private int minX = Integer.MIN_VALUE;
    private int minZ = Integer.MIN_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    private static final int UNDEFINED = Integer.MIN_VALUE;

    public NextTickHashSet(Set oldSet)
    {
        for (Object entryObject : oldSet)
        {
            this.add(entryObject);
        }
    }

    public boolean contains(Object obj)
    {
        if (!(obj instanceof NextTickListEntry))
        {
            return false;
        }
        else
        {
            NextTickListEntry tickEntry = (NextTickListEntry)obj;
            Set chunkSet = this.getSubSet(tickEntry, false);
            return chunkSet == null ? false : chunkSet.contains(tickEntry);
        }
    }

    public boolean add(Object obj)
    {
        if (!(obj instanceof NextTickListEntry))
        {
            return false;
        }
        else
        {
            NextTickListEntry tickEntry = (NextTickListEntry)obj;

            if (tickEntry == null)
            {
                return false;
            }
            else
            {
                Set chunkSet = this.getSubSet(tickEntry, true);
                boolean addedToChunkSet = chunkSet.add(tickEntry);
                boolean addedToParentSet = super.add(obj);

                if (addedToChunkSet != addedToParentSet)
                {
                    throw new IllegalStateException("Added: " + addedToChunkSet + ", addedParent: " + addedToParentSet);
                }
                else
                {
                    return addedToParentSet;
                }
            }
        }
    }

    public boolean remove(Object obj)
    {
        if (!(obj instanceof NextTickListEntry))
        {
            return false;
        }
        else
        {
            NextTickListEntry tickEntry = (NextTickListEntry)obj;
            Set chunkSet = this.getSubSet(tickEntry, false);

            if (chunkSet == null)
            {
                return false;
            }
            else
            {
                boolean removedFromChunkSet = chunkSet.remove(tickEntry);
                boolean removedFromParentSet = super.remove(tickEntry);

                if (removedFromChunkSet != removedFromParentSet)
                {
                    throw new IllegalStateException("Added: " + removedFromChunkSet + ", addedParent: " + removedFromParentSet);
                }
                else
                {
                    return removedFromParentSet;
                }
            }
        }
    }

    private Set getSubSet(NextTickListEntry entry, boolean autoCreate)
    {
        if (entry == null)
        {
            return null;
        }
        else
        {
            BlockPos entryPos = entry.position;
            int chunkX = entryPos.getX() >> 4;
            int chunkZ = entryPos.getZ() >> 4;
            return this.getSubSet(chunkX, chunkZ, autoCreate);
        }
    }

    private Set getSubSet(int cx, int cz, boolean autoCreate)
    {
        long chunkKey = ChunkCoordIntPair.chunkXZ2Int(cx, cz);
        HashSet chunkSet = (HashSet)this.longHashMap.getValueByKey(chunkKey);

        if (chunkSet == null && autoCreate)
        {
            chunkSet = new HashSet();
            this.longHashMap.add(chunkKey, chunkSet);
        }

        return chunkSet;
    }

    public Iterator iterator()
    {
        if (this.minX == Integer.MIN_VALUE)
        {
            return super.iterator();
        }
        else if (this.size() <= 0)
        {
            return Collections.emptyList().iterator();
        }
        else
        {
            int minChunkX = this.minX >> 4;
            int minChunkZ = this.minZ >> 4;
            int maxChunkX = this.maxX >> 4;
            int maxChunkZ = this.maxZ >> 4;
            List iterators = new ArrayList();

            for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX)
            {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ)
                {
                    Set chunkSet = this.getSubSet(chunkX, chunkZ, false);

                    if (chunkSet != null)
                    {
                        iterators.add(chunkSet.iterator());
                    }
                }
            }

            if (iterators.size() <= 0)
            {
                return Collections.emptyList().iterator();
            }
            else if (iterators.size() == 1)
            {
                return (Iterator)iterators.get(0);
            }
            else
            {
                return Iterators.concat(iterators.iterator());
            }
        }
    }

    public void setIteratorLimits(int minX, int minZ, int maxX, int maxZ)
    {
        this.minX = Math.min(minX, maxX);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public void clearIteratorLimits()
    {
        this.minX = Integer.MIN_VALUE;
        this.minZ = Integer.MIN_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxZ = Integer.MIN_VALUE;
    }
}
