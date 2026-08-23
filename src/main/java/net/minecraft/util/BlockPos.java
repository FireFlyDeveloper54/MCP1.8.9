package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;

public class BlockPos extends Vec3i
{
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final int NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = 0 + NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    public BlockPos(int x, int y, int z)
    {
        super(x, y, z);
    }

    public BlockPos(double x, double y, double z)
    {
        super(x, y, z);
    }

    public BlockPos(Entity source)
    {
        this(source.posX, source.posY, source.posZ);
    }

    public BlockPos(Vec3 source)
    {
        this(source.xCoord, source.yCoord, source.zCoord);
    }

    public BlockPos(Vec3i source)
    {
        this(source.getX(), source.getY(), source.getZ());
    }

    public BlockPos toImmutable()
    {
        return this;
    }

    public BlockPos add(double x, double y, double z)
    {
        return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double)this.getX() + x, (double)this.getY() + y, (double)this.getZ() + z);
    }

    public BlockPos add(int x, int y, int z)
    {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
    }

    public BlockPos add(Vec3i vec)
    {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ());
    }

    public BlockPos subtract(Vec3i vec)
    {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(this.getX() - vec.getX(), this.getY() - vec.getY(), this.getZ() - vec.getZ());
    }

    public BlockPos up()
    {
        return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
    }

    public BlockPos up(int n)
    {
        return n == 0 ? this : new BlockPos(this.getX(), this.getY() + n, this.getZ());
    }

    public BlockPos down()
    {
        return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
    }

    public BlockPos down(int n)
    {
        return n == 0 ? this : new BlockPos(this.getX(), this.getY() - n, this.getZ());
    }

    public BlockPos north()
    {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
    }

    public BlockPos north(int n)
    {
        return n == 0 ? this : new BlockPos(this.getX(), this.getY(), this.getZ() - n);
    }

    public BlockPos south()
    {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
    }

    public BlockPos south(int n)
    {
        return n == 0 ? this : new BlockPos(this.getX(), this.getY(), this.getZ() + n);
    }

    public BlockPos west()
    {
        return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
    }

    public BlockPos west(int n)
    {
        return n == 0 ? this : new BlockPos(this.getX() - n, this.getY(), this.getZ());
    }

    public BlockPos east()
    {
        return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
    }

    public BlockPos east(int n)
    {
        return n == 0 ? this : new BlockPos(this.getX() + n, this.getY(), this.getZ());
    }

    public BlockPos offset(EnumFacing facing)
    {
        return new BlockPos(this.getX() + facing.getFrontOffsetX(), this.getY() + facing.getFrontOffsetY(), this.getZ() + facing.getFrontOffsetZ());
    }

    public BlockPos offset(EnumFacing facing, int n)
    {
        return n == 0 ? this : new BlockPos(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ() + facing.getFrontOffsetZ() * n);
    }

    public BlockPos crossProduct(Vec3i vec)
    {
        return new BlockPos(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public long toLong()
    {
        return ((long)this.getX() & X_MASK) << X_SHIFT | ((long)this.getY() & Y_MASK) << Y_SHIFT | ((long)this.getZ() & Z_MASK) << 0;
    }

    public static BlockPos fromLong(long serialized)
    {
        int x = (int)(serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int y = (int)(serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int z = (int)(serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new BlockPos(x, y, z);
    }

    public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to)
    {
        final BlockPos minPos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos maxPos = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable<BlockPos>()
        {
            public Iterator<BlockPos> iterator()
            {
                return new AbstractIterator<BlockPos>()
                {
                    private BlockPos lastReturned = null;
                    protected BlockPos computeNext()
                    {
                        if (this.lastReturned == null)
                        {
                            this.lastReturned = minPos;
                            return this.lastReturned;
                        }
                        else if (this.lastReturned.equals(maxPos))
                        {
                            return (BlockPos)this.endOfData();
                        }
                        else
                        {
                            int x = this.lastReturned.getX();
                            int y = this.lastReturned.getY();
                            int z = this.lastReturned.getZ();

                            if (x < maxPos.getX())
                            {
                                ++x;
                            }
                            else if (y < maxPos.getY())
                            {
                                x = minPos.getX();
                                ++y;
                            }
                            else if (z < maxPos.getZ())
                            {
                                x = minPos.getX();
                                y = minPos.getY();
                                ++z;
                            }

                            this.lastReturned = new BlockPos(x, y, z);
                            return this.lastReturned;
                        }
                    }
                };
            }
        };
    }

    public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(BlockPos from, BlockPos to)
    {
        final BlockPos minPos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos maxPos = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable<BlockPos.MutableBlockPos>()
        {
            public Iterator<BlockPos.MutableBlockPos> iterator()
            {
                return new AbstractIterator<BlockPos.MutableBlockPos>()
                {
                    private BlockPos.MutableBlockPos theBlockPos = null;
                    protected BlockPos.MutableBlockPos computeNext()
                    {
                        if (this.theBlockPos == null)
                        {
                            this.theBlockPos = new BlockPos.MutableBlockPos(minPos.getX(), minPos.getY(), minPos.getZ());
                            return this.theBlockPos;
                        }
                        else if (this.theBlockPos.equals(maxPos))
                        {
                            return (BlockPos.MutableBlockPos)this.endOfData();
                        }
                        else
                        {
                            int x = this.theBlockPos.getX();
                            int y = this.theBlockPos.getY();
                            int z = this.theBlockPos.getZ();

                            if (x < maxPos.getX())
                            {
                                ++x;
                            }
                            else if (y < maxPos.getY())
                            {
                                x = minPos.getX();
                                ++y;
                            }
                            else if (z < maxPos.getZ())
                            {
                                x = minPos.getX();
                                y = minPos.getY();
                                ++z;
                            }

                            this.theBlockPos.x = x;
                            this.theBlockPos.y = y;
                            this.theBlockPos.z = z;
                            return this.theBlockPos;
                        }
                    }
                };
            }
        };
    }

    public static class MutableBlockPos extends BlockPos
    {
        private int x;
        private int y;
        private int z;

        public MutableBlockPos()
        {
            this(0, 0, 0);
        }

        public MutableBlockPos(int x_, int y_, int z_)
        {
            super(0, 0, 0);
            this.x = x_;
            this.y = y_;
            this.z = z_;
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

        public BlockPos.MutableBlockPos set(int xIn, int yIn, int zIn)
        {
            this.x = xIn;
            this.y = yIn;
            this.z = zIn;
            return this;
        }

        public BlockPos.MutableBlockPos set(double xIn, double yIn, double zIn)
        {
            return this.set(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
        }

        public BlockPos.MutableBlockPos set(Entity entityIn)
        {
            return this.set(entityIn.posX, entityIn.posY, entityIn.posZ);
        }

        public BlockPos.MutableBlockPos set(Vec3i vec)
        {
            return this.set(vec.getX(), vec.getY(), vec.getZ());
        }

        public BlockPos.MutableBlockPos move(EnumFacing facing)
        {
            return this.move(facing, 1);
        }

        public BlockPos.MutableBlockPos move(EnumFacing facing, int n)
        {
            return this.set(this.x + facing.getFrontOffsetX() * n, this.y + facing.getFrontOffsetY() * n, this.z + facing.getFrontOffsetZ() * n);
        }

        @Override
        public BlockPos toImmutable()
        {
            return new BlockPos(this.x, this.y, this.z);
        }
    }

    public static final class PooledMutableBlockPos extends MutableBlockPos
    {
        private boolean released;
        private static final List<BlockPos.PooledMutableBlockPos> POOL = Lists.<BlockPos.PooledMutableBlockPos>newArrayList();

        private PooledMutableBlockPos(int xIn, int yIn, int zIn)
        {
            super(xIn, yIn, zIn);
        }

        public static BlockPos.PooledMutableBlockPos retain()
        {
            return retain(0, 0, 0);
        }

        public static BlockPos.PooledMutableBlockPos retain(double xIn, double yIn, double zIn)
        {
            return retain(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
        }

        public static BlockPos.PooledMutableBlockPos retain(Entity entityIn)
        {
            return retain(entityIn.posX, entityIn.posY, entityIn.posZ);
        }

        public static BlockPos.PooledMutableBlockPos retain(Vec3i vec)
        {
            return retain(vec.getX(), vec.getY(), vec.getZ());
        }

        public static BlockPos.PooledMutableBlockPos retain(int xIn, int yIn, int zIn)
        {
            synchronized (POOL)
            {
                if (!POOL.isEmpty())
                {
                    BlockPos.PooledMutableBlockPos pooled = POOL.remove(POOL.size() - 1);

                    if (pooled != null && pooled.released)
                    {
                        pooled.released = false;
                        pooled.set(xIn, yIn, zIn);
                        return pooled;
                    }
                }
            }

            return new BlockPos.PooledMutableBlockPos(xIn, yIn, zIn);
        }

        public void release()
        {
            synchronized (POOL)
            {
                if (POOL.size() < 100)
                {
                    POOL.add(this);
                }

                this.released = true;
            }
        }

        public BlockPos.PooledMutableBlockPos set(int xIn, int yIn, int zIn)
        {
            if (this.released)
            {
                this.released = false;
            }

            return (BlockPos.PooledMutableBlockPos)super.set(xIn, yIn, zIn);
        }

        public BlockPos.PooledMutableBlockPos set(double xIn, double yIn, double zIn)
        {
            return (BlockPos.PooledMutableBlockPos)super.set(xIn, yIn, zIn);
        }

        public BlockPos.PooledMutableBlockPos set(Entity entityIn)
        {
            return (BlockPos.PooledMutableBlockPos)super.set(entityIn);
        }

        public BlockPos.PooledMutableBlockPos set(Vec3i vec)
        {
            return (BlockPos.PooledMutableBlockPos)super.set(vec);
        }

        public BlockPos.PooledMutableBlockPos move(EnumFacing facing)
        {
            return (BlockPos.PooledMutableBlockPos)super.move(facing);
        }

        public BlockPos.PooledMutableBlockPos move(EnumFacing facing, int n)
        {
            return (BlockPos.PooledMutableBlockPos)super.move(facing, n);
        }
    }
}
