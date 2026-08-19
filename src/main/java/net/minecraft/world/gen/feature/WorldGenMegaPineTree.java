package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class WorldGenMegaPineTree extends WorldGenHugeTrees
{
    private static final IBlockState SPRUCE_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    private static final IBlockState SPRUCE_LEAVES = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private static final IBlockState PODZOL = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
    private boolean useBaseHeight;

    public WorldGenMegaPineTree(boolean notify, boolean useBaseHeight)
    {
        super(notify, 13, 15, SPRUCE_LOG, SPRUCE_LEAVES);
        this.useBaseHeight = useBaseHeight;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight = this.getHeight(rand);

        if (!this.ensureGrowable(worldIn, rand, position, treeHeight))
        {
            return false;
        }
        else
        {
            this.createCrown(worldIn, position.getX(), position.getZ(), position.getY() + treeHeight, 0, rand);

            for (int trunkY = 0; trunkY < treeHeight; ++trunkY)
            {
                Block currentBlock = worldIn.getBlockState(position.up(trunkY)).getBlock();

                if (currentBlock.getMaterial() == Material.air || currentBlock.getMaterial() == Material.leaves)
                {
                    this.setBlockAndNotifyAdequately(worldIn, position.up(trunkY), this.woodMetadata);
                }

                if (trunkY < treeHeight - 1)
                {
                    currentBlock = worldIn.getBlockState(position.add(1, trunkY, 0)).getBlock();

                    if (currentBlock.getMaterial() == Material.air || currentBlock.getMaterial() == Material.leaves)
                    {
                        this.setBlockAndNotifyAdequately(worldIn, position.add(1, trunkY, 0), this.woodMetadata);
                    }

                    currentBlock = worldIn.getBlockState(position.add(1, trunkY, 1)).getBlock();

                    if (currentBlock.getMaterial() == Material.air || currentBlock.getMaterial() == Material.leaves)
                    {
                        this.setBlockAndNotifyAdequately(worldIn, position.add(1, trunkY, 1), this.woodMetadata);
                    }

                    currentBlock = worldIn.getBlockState(position.add(0, trunkY, 1)).getBlock();

                    if (currentBlock.getMaterial() == Material.air || currentBlock.getMaterial() == Material.leaves)
                    {
                        this.setBlockAndNotifyAdequately(worldIn, position.add(0, trunkY, 1), this.woodMetadata);
                    }
                }
            }

            return true;
        }
    }

    private void createCrown(World worldIn, int x, int z, int topY, int baseRadius, Random rand)
    {
        int crownHeight = rand.nextInt(5) + (this.useBaseHeight ? this.baseHeight : 3);
        int previousRadius = 0;

        for (int leafY = topY - crownHeight; leafY <= topY; ++leafY)
        {
            int distanceFromTop = topY - leafY;
            int layerRadius = baseRadius + MathHelper.floor_float((float)distanceFromTop / (float)crownHeight * 3.5F);
            this.growLeavesLayerStrict(worldIn, new BlockPos(x, leafY, z), layerRadius + (distanceFromTop > 0 && layerRadius == previousRadius && (leafY & 1) == 0 ? 1 : 0));
            previousRadius = layerRadius;
        }
    }

    public void generateSaplings(World worldIn, Random rand, BlockPos pos)
    {
        this.generatePodzolCircle(worldIn, pos.west().north());
        this.generatePodzolCircle(worldIn, pos.east(2).north());
        this.generatePodzolCircle(worldIn, pos.west().south(2));
        this.generatePodzolCircle(worldIn, pos.east(2).south(2));

        for (int podzolIndex = 0; podzolIndex < 5; ++podzolIndex)
        {
            int edgeSample = rand.nextInt(64);
            int offsetX = edgeSample % 8;
            int offsetZ = edgeSample / 8;

            if (offsetX == 0 || offsetX == 7 || offsetZ == 0 || offsetZ == 7)
            {
                this.generatePodzolCircle(worldIn, pos.add(-3 + offsetX, 0, -3 + offsetZ));
            }
        }
    }

    private void generatePodzolCircle(World worldIn, BlockPos center)
    {
        for (int offsetX = -2; offsetX <= 2; ++offsetX)
        {
            for (int offsetZ = -2; offsetZ <= 2; ++offsetZ)
            {
                if (Math.abs(offsetX) != 2 || Math.abs(offsetZ) != 2)
                {
                    this.generatePodzolAt(worldIn, center.add(offsetX, 0, offsetZ));
                }
            }
        }
    }

    private void generatePodzolAt(World worldIn, BlockPos pos)
    {
        for (int offsetY = 2; offsetY >= -3; --offsetY)
        {
            BlockPos blockPos = pos.up(offsetY);
            Block existingBlock = worldIn.getBlockState(blockPos).getBlock();

            if (existingBlock == Blocks.grass || existingBlock == Blocks.dirt)
            {
                this.setBlockAndNotifyAdequately(worldIn, blockPos, PODZOL);
                break;
            }

            if (existingBlock.getMaterial() != Material.air && offsetY < 0)
            {
                break;
            }
        }
    }
}
