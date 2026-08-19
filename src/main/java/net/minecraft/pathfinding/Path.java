package net.minecraft.pathfinding;

public class Path
{
    private PathPoint[] pathPoints = new PathPoint[1024];
    private int count;

    public PathPoint addPoint(PathPoint point)
    {
        if (point.index >= 0)
        {
            throw new IllegalStateException("OW KNOWS!");
        }
        else
        {
            if (this.count == this.pathPoints.length)
            {
                PathPoint[] grownPathPoints = new PathPoint[this.count << 1];
                System.arraycopy(this.pathPoints, 0, grownPathPoints, 0, this.count);
                this.pathPoints = grownPathPoints;
            }

            this.pathPoints[this.count] = point;
            point.index = this.count;
            this.sortBack(this.count++);
            return point;
        }
    }

    public void clearPath()
    {
        this.count = 0;
    }

    public PathPoint dequeue()
    {
        PathPoint point = this.pathPoints[0];
        this.pathPoints[0] = this.pathPoints[--this.count];
        this.pathPoints[this.count] = null;

        if (this.count > 0)
        {
            this.sortForward(0);
        }

        point.index = -1;
        return point;
    }

    public void changeDistance(PathPoint point, float distance)
    {
        float previousDistance = point.distanceToTarget;
        point.distanceToTarget = distance;

        if (distance < previousDistance)
        {
            this.sortBack(point.index);
        }
        else
        {
            this.sortForward(point.index);
        }
    }

    private void sortBack(int index)
    {
        PathPoint point = this.pathPoints[index];
        int parentIndex;

        for (float distance = point.distanceToTarget; index > 0; index = parentIndex)
        {
            parentIndex = index - 1 >> 1;
            PathPoint parent = this.pathPoints[parentIndex];

            if (distance >= parent.distanceToTarget)
            {
                break;
            }

            this.pathPoints[index] = parent;
            parent.index = index;
        }

        this.pathPoints[index] = point;
        point.index = index;
    }

    private void sortForward(int index)
    {
        PathPoint point = this.pathPoints[index];
        float distance = point.distanceToTarget;

        while (true)
        {
            int leftChildIndex = 1 + (index << 1);
            int rightChildIndex = leftChildIndex + 1;

            if (leftChildIndex >= this.count)
            {
                break;
            }

            PathPoint leftChild = this.pathPoints[leftChildIndex];
            float leftDistance = leftChild.distanceToTarget;
            PathPoint rightChild;
            float rightDistance;

            if (rightChildIndex >= this.count)
            {
                rightChild = null;
                rightDistance = Float.POSITIVE_INFINITY;
            }
            else
            {
                rightChild = this.pathPoints[rightChildIndex];
                rightDistance = rightChild.distanceToTarget;
            }

            if (leftDistance < rightDistance)
            {
                if (leftDistance >= distance)
                {
                    break;
                }

                this.pathPoints[index] = leftChild;
                leftChild.index = index;
                index = leftChildIndex;
            }
            else
            {
                if (rightDistance >= distance)
                {
                    break;
                }

                this.pathPoints[index] = rightChild;
                rightChild.index = index;
                index = rightChildIndex;
            }
        }

        this.pathPoints[index] = point;
        point.index = index;
    }

    public boolean isPathEmpty()
    {
        return this.count == 0;
    }
}
