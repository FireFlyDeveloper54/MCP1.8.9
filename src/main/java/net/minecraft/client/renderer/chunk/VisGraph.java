package net.minecraft.client.renderer.chunk;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class VisGraph
{
    private static final int X_INDEX_STEP = 1;
    private static final int Z_INDEX_STEP = 16;
    private static final int Y_INDEX_STEP = 256;
    private static final ThreadLocal<int[]> VISIBILITY_QUEUES = new ThreadLocal<int[]>()
    {
        protected int[] initialValue()
        {
            return new int[4096];
        }
    };
    private final BitSet filledBlocks = new BitSet(4096);
    private static final int[] EDGE_INDICES = new int[1352];
    private int emptyBlockCount = 4096;

    public void markBlockOpaque(BlockPos pos)
    {
        this.filledBlocks.set(getIndex(pos), true);
        --this.emptyBlockCount;
    }

    private static int getIndex(BlockPos pos)
    {
        return getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    private static int getIndex(int x, int y, int z)
    {
        return x << 0 | y << 8 | z << 4;
    }

    public SetVisibility computeVisibility()
    {
        SetVisibility setVisibility = new SetVisibility();

        if (4096 - this.emptyBlockCount < 256)
        {
            setVisibility.setAllVisible(true);
        }
        else if (this.emptyBlockCount == 0)
        {
            setVisibility.setAllVisible(false);
        }
        else
        {
            for (int edgeIndex : EDGE_INDICES)
            {
                if (!this.filledBlocks.get(edgeIndex))
                {
                    setVisibility.setManyVisible(this.floodFillVisibleFacings(edgeIndex));
                }
            }
        }

        return setVisibility;
    }

    public Set<EnumFacing> getVisibleFacings(BlockPos pos)
    {
        return this.floodFillVisibleFacings(getIndex(pos));
    }

    private Set<EnumFacing> floodFillVisibleFacings(int startIndex)
    {
        Set<EnumFacing> visibleFacings = EnumSet.<EnumFacing>noneOf(EnumFacing.class);
        int[] visibilityQueue = VISIBILITY_QUEUES.get();
        int queueReadIndex = 0;
        int queueWriteIndex = 0;
        visibilityQueue[queueWriteIndex++] = startIndex;
        this.filledBlocks.set(startIndex, true);

        while (queueReadIndex < queueWriteIndex)
        {
            int currentIndex = visibilityQueue[queueReadIndex++];
            this.addVisibleFacingsFromIndex(currentIndex, visibleFacings);

            for (EnumFacing facing : EnumFacing.VALUES)
            {
                int neighborIndex = this.getNeighborIndex(currentIndex, facing);

                if (neighborIndex >= 0 && !this.filledBlocks.get(neighborIndex))
                {
                    this.filledBlocks.set(neighborIndex, true);
                    visibilityQueue[queueWriteIndex++] = neighborIndex;
                }
            }
        }

        return visibleFacings;
    }

    private void addVisibleFacingsFromIndex(int index, Set<EnumFacing> visibleFacings)
    {
        int x = index >> 0 & 15;

        if (x == 0)
        {
            visibleFacings.add(EnumFacing.WEST);
        }
        else if (x == 15)
        {
            visibleFacings.add(EnumFacing.EAST);
        }

        int y = index >> 8 & 15;

        if (y == 0)
        {
            visibleFacings.add(EnumFacing.DOWN);
        }
        else if (y == 15)
        {
            visibleFacings.add(EnumFacing.UP);
        }

        int z = index >> 4 & 15;

        if (z == 0)
        {
            visibleFacings.add(EnumFacing.NORTH);
        }
        else if (z == 15)
        {
            visibleFacings.add(EnumFacing.SOUTH);
        }
    }

    private int getNeighborIndex(int index, EnumFacing facing)
    {
        switch (facing)
        {
            case DOWN:
                if ((index >> 8 & 15) == 0)
                {
                    return -1;
                }

                return index - Y_INDEX_STEP;

            case UP:
                if ((index >> 8 & 15) == 15)
                {
                    return -1;
                }

                return index + Y_INDEX_STEP;

            case NORTH:
                if ((index >> 4 & 15) == 0)
                {
                    return -1;
                }

                return index - Z_INDEX_STEP;

            case SOUTH:
                if ((index >> 4 & 15) == 15)
                {
                    return -1;
                }

                return index + Z_INDEX_STEP;

            case WEST:
                if ((index >> 0 & 15) == 0)
                {
                    return -1;
                }

                return index - X_INDEX_STEP;

            case EAST:
                if ((index >> 0 & 15) == 15)
                {
                    return -1;
                }

                return index + X_INDEX_STEP;

            default:
                return -1;
        }
    }

    static
    {
        int edgeIndex = 0;

        for (int x = 0; x < 16; ++x)
        {
            for (int y = 0; y < 16; ++y)
            {
                for (int z = 0; z < 16; ++z)
                {
                    if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)
                    {
                        EDGE_INDICES[edgeIndex++] = getIndex(x, y, z);
                    }
                }
            }
        }
    }
}
