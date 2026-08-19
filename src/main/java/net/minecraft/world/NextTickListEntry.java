package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public class NextTickListEntry implements Comparable<NextTickListEntry>
{
    private static long nextTickEntryID;
    private final Block block;
    public final BlockPos position;
    public long scheduledTime;
    public int priority;
    private long tickEntryID;

    public NextTickListEntry(BlockPos positionIn, Block blockIn)
    {
        this.tickEntryID = (long)(nextTickEntryID++);
        this.position = positionIn.toImmutable();
        this.block = blockIn;
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof NextTickListEntry))
        {
            return false;
        }
        else
        {
            NextTickListEntry nextTickEntry = (NextTickListEntry)other;
            return this.position.equals(nextTickEntry.position) && Block.isEqualTo(this.block, nextTickEntry.block);
        }
    }

    public int hashCode()
    {
        return this.position.hashCode();
    }

    public NextTickListEntry setScheduledTime(long scheduledTimeIn)
    {
        this.scheduledTime = scheduledTimeIn;
        return this;
    }

    public void setPriority(int priorityIn)
    {
        this.priority = priorityIn;
    }

    public int compareTo(NextTickListEntry other)
    {
        return this.scheduledTime < other.scheduledTime ? -1 : (this.scheduledTime > other.scheduledTime ? 1 : (this.priority != other.priority ? this.priority - other.priority : (this.tickEntryID < other.tickEntryID ? -1 : (this.tickEntryID > other.tickEntryID ? 1 : 0))));
    }

    public String toString()
    {
        return Block.getIdFromBlock(this.block) + ": " + this.position + ", " + this.scheduledTime + ", " + this.priority + ", " + this.tickEntryID;
    }

    public Block getBlock()
    {
        return this.block;
    }
}
