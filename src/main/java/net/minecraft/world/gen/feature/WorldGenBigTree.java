package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class WorldGenBigTree extends WorldGenAbstractTree
{
    private Random rand;
    private World world;
    private BlockPos basePos = BlockPos.ORIGIN;
    int heightLimit;
    int height;
    double heightAttenuation = 0.618D;
    double branchSlope = 0.381D;
    double scaleWidth = 1.0D;
    double leafDensity = 1.0D;
    int trunkSize = 1;
    int heightLimitLimit = 12;
    int leafDistanceLimit = 4;
    List<WorldGenBigTree.FoliageCoordinates> foliageCoordinates;

    public WorldGenBigTree(boolean notify)
    {
        super(notify);
    }

    void generateLeafNodeList()
    {
        this.height = (int)((double)this.heightLimit * this.heightAttenuation);

        if (this.height >= this.heightLimit)
        {
            this.height = this.heightLimit - 1;
        }

        double nodeDensity = this.leafDensity * (double)this.heightLimit / 13.0D;
        int nodesPerLayer = (int)(1.382D + nodeDensity * nodeDensity);

        if (nodesPerLayer < 1)
        {
            nodesPerLayer = 1;
        }

        int topY = this.basePos.getY() + this.height;
        int layerOffset = this.heightLimit - this.leafDistanceLimit;
        this.foliageCoordinates = Lists.<WorldGenBigTree.FoliageCoordinates>newArrayList();
        this.foliageCoordinates.add(new WorldGenBigTree.FoliageCoordinates(this.basePos.up(layerOffset), topY));

        for (; layerOffset >= 0; --layerOffset)
        {
            float layerRadius = this.layerSize(layerOffset);

            if (layerRadius >= 0.0F)
            {
                for (int nodeIndex = 0; nodeIndex < nodesPerLayer; ++nodeIndex)
                {
                    double radialDistance = this.scaleWidth * (double)layerRadius * ((double)this.rand.nextFloat() + 0.328D);
                    double angle = (double)(this.rand.nextFloat() * 2.0F) * Math.PI;
                    double offsetX = radialDistance * Math.sin(angle) + 0.5D;
                    double offsetZ = radialDistance * Math.cos(angle) + 0.5D;
                    BlockPos leafNodePos = this.basePos.add(offsetX, (double)(layerOffset - 1), offsetZ);
                    BlockPos leafNodeTopPos = leafNodePos.up(this.leafDistanceLimit);

                    if (this.checkBlockLine(leafNodePos, leafNodeTopPos) == -1)
                    {
                        int deltaXToBase = this.basePos.getX() - leafNodePos.getX();
                        int deltaZToBase = this.basePos.getZ() - leafNodePos.getZ();
                        double projectedBranchBaseY = (double)leafNodePos.getY() - Math.sqrt((double)(deltaXToBase * deltaXToBase + deltaZToBase * deltaZToBase)) * this.branchSlope;
                        int branchBaseY = projectedBranchBaseY > (double)topY ? topY : (int)projectedBranchBaseY;
                        BlockPos branchBasePos = new BlockPos(this.basePos.getX(), branchBaseY, this.basePos.getZ());

                        if (this.checkBlockLine(branchBasePos, leafNodePos) == -1)
                        {
                            this.foliageCoordinates.add(new WorldGenBigTree.FoliageCoordinates(leafNodePos, branchBasePos.getY()));
                        }
                    }
                }
            }
        }
    }

    void generateLeafLayer(BlockPos center, float radius, IBlockState leafState)
    {
        int radiusCeil = (int)((double)radius + 0.618D);

        for (int offsetX = -radiusCeil; offsetX <= radiusCeil; ++offsetX)
        {
            for (int offsetZ = -radiusCeil; offsetZ <= radiusCeil; ++offsetZ)
            {
                double leafOffsetX = (double)Math.abs(offsetX) + 0.5D;
                double leafOffsetZ = (double)Math.abs(offsetZ) + 0.5D;

                if (leafOffsetX * leafOffsetX + leafOffsetZ * leafOffsetZ <= (double)(radius * radius))
                {
                    BlockPos leafPos = center.add(offsetX, 0, offsetZ);
                    Material material = this.world.getBlockState(leafPos).getBlock().getMaterial();

                    if (material == Material.air || material == Material.leaves)
                    {
                        this.setBlockAndNotifyAdequately(this.world, leafPos, leafState);
                    }
                }
            }
        }
    }

    float layerSize(int layer)
    {
        if ((float)layer < (float)this.heightLimit * 0.3F)
        {
            return -1.0F;
        }
        else
        {
            float halfHeight = (float)this.heightLimit / 2.0F;
            float distanceFromCenter = halfHeight - (float)layer;
            float layerRadius = MathHelper.sqrt_float(halfHeight * halfHeight - distanceFromCenter * distanceFromCenter);

            if (distanceFromCenter == 0.0F)
            {
                layerRadius = halfHeight;
            }
            else if (Math.abs(distanceFromCenter) >= halfHeight)
            {
                return 0.0F;
            }

            return layerRadius * 0.5F;
        }
    }

    float leafSize(int layer)
    {
        return layer >= 0 && layer < this.leafDistanceLimit ? (layer != 0 && layer != this.leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
    }

    void generateLeafNode(BlockPos pos)
    {
        for (int layerOffset = 0; layerOffset < this.leafDistanceLimit; ++layerOffset)
        {
            this.generateLeafLayer(pos.up(layerOffset), this.leafSize(layerOffset), Blocks.leaves.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false)));
        }
    }

    void placeLogLine(BlockPos start, BlockPos end, Block logBlock)
    {
        BlockPos deltaPos = end.add(-start.getX(), -start.getY(), -start.getZ());
        int steps = this.getGreatestDistance(deltaPos);
        float stepX = (float)deltaPos.getX() / (float)steps;
        float stepY = (float)deltaPos.getY() / (float)steps;
        float stepZ = (float)deltaPos.getZ() / (float)steps;

        for (int step = 0; step <= steps; ++step)
        {
            BlockPos logPos = start.add((double)(0.5F + (float)step * stepX), (double)(0.5F + (float)step * stepY), (double)(0.5F + (float)step * stepZ));
            BlockLog.EnumAxis logAxis = this.getLogAxis(start, logPos);
            this.setBlockAndNotifyAdequately(this.world, logPos, logBlock.getDefaultState().withProperty(BlockLog.LOG_AXIS, logAxis));
        }
    }

    private int getGreatestDistance(BlockPos posIn)
    {
        int absX = MathHelper.abs_int(posIn.getX());
        int absY = MathHelper.abs_int(posIn.getY());
        int absZ = MathHelper.abs_int(posIn.getZ());
        return absZ > absX && absZ > absY ? absZ : (absY > absX ? absY : absX);
    }

    private BlockLog.EnumAxis getLogAxis(BlockPos start, BlockPos end)
    {
        BlockLog.EnumAxis logAxis = BlockLog.EnumAxis.Y;
        int deltaX = Math.abs(end.getX() - start.getX());
        int deltaZ = Math.abs(end.getZ() - start.getZ());
        int maxHorizontalDelta = Math.max(deltaX, deltaZ);

        if (maxHorizontalDelta > 0)
        {
            if (deltaX == maxHorizontalDelta)
            {
                logAxis = BlockLog.EnumAxis.X;
            }
            else if (deltaZ == maxHorizontalDelta)
            {
                logAxis = BlockLog.EnumAxis.Z;
            }
        }

        return logAxis;
    }

    void generateLeaves()
    {
        for (WorldGenBigTree.FoliageCoordinates foliageCoord : this.foliageCoordinates)
        {
            this.generateLeafNode(foliageCoord);
        }
    }

    boolean leafNodeNeedsBase(int leafY)
    {
        return (double)leafY >= (double)this.heightLimit * 0.2D;
    }

    void generateTrunk()
    {
        BlockPos trunkBase = this.basePos;
        BlockPos trunkTop = this.basePos.up(this.height);
        Block logBlock = Blocks.log;
        this.placeLogLine(trunkBase, trunkTop, logBlock);

        if (this.trunkSize == 2)
        {
            this.placeLogLine(trunkBase.east(), trunkTop.east(), logBlock);
            this.placeLogLine(trunkBase.east().south(), trunkTop.east().south(), logBlock);
            this.placeLogLine(trunkBase.south(), trunkTop.south(), logBlock);
        }
    }

    void generateLeafNodeBases()
    {
        for (WorldGenBigTree.FoliageCoordinates foliageCoord : this.foliageCoordinates)
        {
            int branchBaseY = foliageCoord.getBranchBase();
            BlockPos branchBasePos = new BlockPos(this.basePos.getX(), branchBaseY, this.basePos.getZ());

            if (!branchBasePos.equals(foliageCoord) && this.leafNodeNeedsBase(branchBaseY - this.basePos.getY()))
            {
                this.placeLogLine(branchBasePos, foliageCoord, Blocks.log);
            }
        }
    }

    int checkBlockLine(BlockPos posOne, BlockPos posTwo)
    {
        BlockPos deltaPos = posTwo.add(-posOne.getX(), -posOne.getY(), -posOne.getZ());
        int steps = this.getGreatestDistance(deltaPos);
        float stepX = (float)deltaPos.getX() / (float)steps;
        float stepY = (float)deltaPos.getY() / (float)steps;
        float stepZ = (float)deltaPos.getZ() / (float)steps;

        if (steps == 0)
        {
            return -1;
        }
        else
        {
            for (int step = 0; step <= steps; ++step)
            {
                BlockPos checkPos = posOne.add((double)(0.5F + (float)step * stepX), (double)(0.5F + (float)step * stepY), (double)(0.5F + (float)step * stepZ));

                if (!this.canGrowInto(this.world.getBlockState(checkPos).getBlock()))
                {
                    return step;
                }
            }

            return -1;
        }
    }

    public void prepareGeneration()
    {
        this.leafDistanceLimit = 5;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        this.world = worldIn;
        this.basePos = position;
        this.rand = new Random(rand.nextLong());

        if (this.heightLimit == 0)
        {
            this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        }

        if (!this.validTreeLocation())
        {
            return false;
        }
        else
        {
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateTrunk();
            this.generateLeafNodeBases();
            return true;
        }
    }

    private boolean validTreeLocation()
    {
        Block block = this.world.getBlockState(this.basePos.down()).getBlock();

        if (block != Blocks.dirt && block != Blocks.grass && block != Blocks.farmland)
        {
            return false;
        }
        else
        {
            int obstructionDistance = this.checkBlockLine(this.basePos, this.basePos.up(this.heightLimit - 1));

            if (obstructionDistance == -1)
            {
                return true;
            }
            else if (obstructionDistance < 6)
            {
                return false;
            }
            else
            {
                this.heightLimit = obstructionDistance;
                return true;
            }
        }
    }

    static class FoliageCoordinates extends BlockPos
    {
        private final int branchBase;

        public FoliageCoordinates(BlockPos pos, int branchBase)
        {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.branchBase = branchBase;
        }

        public int getBranchBase()
        {
            return this.branchBase;
        }
    }
}
