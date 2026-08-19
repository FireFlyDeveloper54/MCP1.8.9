package net.optifine;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

public class BlockPosM extends BlockPos
{
    private int x;
    private int y;
    private int z;
    private int level;
    private BlockPosM[] facings;
    private boolean needsUpdate;

    public BlockPosM(int x, int y, int z)
    {
        this(x, y, z, 0);
    }

    public BlockPosM(double xIn, double yIn, double zIn)
    {
        this(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
    }

    public BlockPosM(int x, int y, int z, int level)
    {
        super(0, 0, 0);
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    public void setXyz(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.needsUpdate = true;
    }

    public void setXyz(double xIn, double yIn, double zIn)
    {
        this.setXyz(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
    }

    public BlockPosM set(Vec3i vec)
    {
        this.setXyz(vec.getX(), vec.getY(), vec.getZ());
        return this;
    }

    public BlockPosM set(int xIn, int yIn, int zIn)
    {
        this.setXyz(xIn, yIn, zIn);
        return this;
    }

    public BlockPos offsetMutable(EnumFacing facing)
    {
        return this.offset(facing);
    }

    public BlockPos offset(EnumFacing facing)
    {
        if (this.level <= 0)
        {
            return super.offset(facing, 1);
        }
        else
        {
            if (this.facings == null)
            {
                this.facings = new BlockPosM[EnumFacing.VALUES.length];
            }

            if (this.needsUpdate)
            {
                this.update();
            }

            int facingIndex = facing.getIndex();
            BlockPosM cachedPos = this.facings[facingIndex];

            if (cachedPos == null)
            {
                int nextX = this.x + facing.getFrontOffsetX();
                int nextY = this.y + facing.getFrontOffsetY();
                int nextZ = this.z + facing.getFrontOffsetZ();
                cachedPos = new BlockPosM(nextX, nextY, nextZ, this.level - 1);
                this.facings[facingIndex] = cachedPos;
            }

            return cachedPos;
        }
    }

    public BlockPos offset(EnumFacing facing, int n)
    {
        return n == 1 ? this.offset(facing) : super.offset(facing, n);
    }

    private void update()
    {
        for (int facingIndex = 0; facingIndex < 6; ++facingIndex)
        {
            BlockPosM cachedPos = this.facings[facingIndex];

            if (cachedPos != null)
            {
                EnumFacing facing = EnumFacing.VALUES[facingIndex];
                int nextX = this.x + facing.getFrontOffsetX();
                int nextY = this.y + facing.getFrontOffsetY();
                int nextZ = this.z + facing.getFrontOffsetZ();
                cachedPos.setXyz(nextX, nextY, nextZ);
            }
        }

        this.needsUpdate = false;
    }

    @Override
    public BlockPos toImmutable()
    {
        return new BlockPos(this.x, this.y, this.z);
    }

    public static Iterable getAllInBoxMutable(BlockPos from, BlockPos to)
    {
        final BlockPos minPos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos maxPos = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable()
        {
            public Iterator iterator()
            {
                return new AbstractIterator()
                {
                    private BlockPosM mutablePos = null;

                    protected BlockPosM computeNextBlockPos()
                    {
                        if (this.mutablePos == null)
                        {
                            this.mutablePos = new BlockPosM(minPos.getX(), minPos.getY(), minPos.getZ(), 3);
                            return this.mutablePos;
                        }
                        else if (this.mutablePos.equals(maxPos))
                        {
                            return (BlockPosM)this.endOfData();
                        }
                        else
                        {
                            int currentX = this.mutablePos.getX();
                            int currentY = this.mutablePos.getY();
                            int currentZ = this.mutablePos.getZ();

                            if (currentX < maxPos.getX())
                            {
                                ++currentX;
                            }
                            else if (currentY < maxPos.getY())
                            {
                                currentX = minPos.getX();
                                ++currentY;
                            }
                            else if (currentZ < maxPos.getZ())
                            {
                                currentX = minPos.getX();
                                currentY = minPos.getY();
                                ++currentZ;
                            }

                            this.mutablePos.setXyz(currentX, currentY, currentZ);
                            return this.mutablePos;
                        }
                    }

                    protected Object computeNext()
                    {
                        return this.computeNextBlockPos();
                    }
                };
            }
        };
    }
}
