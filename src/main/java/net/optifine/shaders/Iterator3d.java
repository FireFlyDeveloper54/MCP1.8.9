package net.optifine.shaders;

import java.util.Iterator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.optifine.BlockPosM;

public class Iterator3d implements Iterator<BlockPos>
{
    private IteratorAxis iteratorAxis;
    private BlockPosM blockPos = new BlockPosM(0, 0, 0);
    private int axis = 0;
    private int kX;
    private int kY;
    private int kZ;
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    public Iterator3d(BlockPos posStart, BlockPos posEnd, int width, int height)
    {
        boolean reverseX = posStart.getX() > posEnd.getX();
        boolean reverseY = posStart.getY() > posEnd.getY();
        boolean reverseZ = posStart.getZ() > posEnd.getZ();
        posStart = this.reverseCoord(posStart, reverseX, reverseY, reverseZ);
        posEnd = this.reverseCoord(posEnd, reverseX, reverseY, reverseZ);
        this.kX = reverseX ? -1 : 1;
        this.kY = reverseY ? -1 : 1;
        this.kZ = reverseZ ? -1 : 1;
        Vec3 delta = new Vec3((double)(posEnd.getX() - posStart.getX()), (double)(posEnd.getY() - posStart.getY()), (double)(posEnd.getZ() - posStart.getZ()));
        Vec3 direction = delta.normalize();
        Vec3 xAxis = new Vec3(1.0D, 0.0D, 0.0D);
        double dotX = direction.dotProduct(xAxis);
        double absDotX = Math.abs(dotX);
        Vec3 yAxis = new Vec3(0.0D, 1.0D, 0.0D);
        double dotY = direction.dotProduct(yAxis);
        double absDotY = Math.abs(dotY);
        Vec3 zAxis = new Vec3(0.0D, 0.0D, 1.0D);
        double dotZ = direction.dotProduct(zAxis);
        double absDotZ = Math.abs(dotZ);

        if (absDotZ >= absDotY && absDotZ >= absDotX)
        {
            this.axis = 2;
            BlockPos axisStart = new BlockPos(posStart.getZ(), posStart.getY() - width, posStart.getX() - height);
            BlockPos axisEnd = new BlockPos(posEnd.getZ(), posStart.getY() + width + 1, posStart.getX() + height + 1);
            int lengthZ = posEnd.getZ() - posStart.getZ();
            double yDelta = (double)(posEnd.getY() - posStart.getY()) / (1.0D * (double)lengthZ);
            double xDelta = (double)(posEnd.getX() - posStart.getX()) / (1.0D * (double)lengthZ);
            this.iteratorAxis = new IteratorAxis(axisStart, axisEnd, yDelta, xDelta);
        }
        else if (absDotY >= absDotX && absDotY >= absDotZ)
        {
            this.axis = 1;
            BlockPos axisStart = new BlockPos(posStart.getY(), posStart.getX() - width, posStart.getZ() - height);
            BlockPos axisEnd = new BlockPos(posEnd.getY(), posStart.getX() + width + 1, posStart.getZ() + height + 1);
            int lengthY = posEnd.getY() - posStart.getY();
            double xDelta = (double)(posEnd.getX() - posStart.getX()) / (1.0D * (double)lengthY);
            double zDelta = (double)(posEnd.getZ() - posStart.getZ()) / (1.0D * (double)lengthY);
            this.iteratorAxis = new IteratorAxis(axisStart, axisEnd, xDelta, zDelta);
        }
        else
        {
            this.axis = 0;
            BlockPos axisStart = new BlockPos(posStart.getX(), posStart.getY() - width, posStart.getZ() - height);
            BlockPos axisEnd = new BlockPos(posEnd.getX(), posStart.getY() + width + 1, posStart.getZ() + height + 1);
            int lengthX = posEnd.getX() - posStart.getX();
            double yDelta = (double)(posEnd.getY() - posStart.getY()) / (1.0D * (double)lengthX);
            double zDelta = (double)(posEnd.getZ() - posStart.getZ()) / (1.0D * (double)lengthX);
            this.iteratorAxis = new IteratorAxis(axisStart, axisEnd, yDelta, zDelta);
        }
    }

    private BlockPos reverseCoord(BlockPos pos, boolean revX, boolean revY, boolean revZ)
    {
        if (revX)
        {
            pos = new BlockPos(-pos.getX(), pos.getY(), pos.getZ());
        }

        if (revY)
        {
            pos = new BlockPos(pos.getX(), -pos.getY(), pos.getZ());
        }

        if (revZ)
        {
            pos = new BlockPos(pos.getX(), pos.getY(), -pos.getZ());
        }

        return pos;
    }

    public boolean hasNext()
    {
        return this.iteratorAxis.hasNext();
    }

    public BlockPos next()
    {
        BlockPos axisPos = this.iteratorAxis.next();

        switch (this.axis)
        {
            case 0:
                this.blockPos.setXyz(axisPos.getX() * this.kX, axisPos.getY() * this.kY, axisPos.getZ() * this.kZ);
                return this.blockPos;

            case 1:
                this.blockPos.setXyz(axisPos.getY() * this.kX, axisPos.getX() * this.kY, axisPos.getZ() * this.kZ);
                return this.blockPos;

            case 2:
                this.blockPos.setXyz(axisPos.getZ() * this.kX, axisPos.getY() * this.kY, axisPos.getX() * this.kZ);
                return this.blockPos;

            default:
                this.blockPos.setXyz(axisPos.getX() * this.kX, axisPos.getY() * this.kY, axisPos.getZ() * this.kZ);
                return this.blockPos;
        }
    }

    public void remove()
    {
        throw new RuntimeException("Not supported");
    }

    public static void main(String[] args)
    {
        BlockPos startPos = new BlockPos(10, 20, 30);
        BlockPos endPos = new BlockPos(30, 40, 20);
        Iterator3d iterator3D = new Iterator3d(startPos, endPos, 1, 1);

        while (iterator3D.hasNext())
        {
            BlockPos nextPos = iterator3D.next();
            System.out.println("" + nextPos);
        }
    }
}
