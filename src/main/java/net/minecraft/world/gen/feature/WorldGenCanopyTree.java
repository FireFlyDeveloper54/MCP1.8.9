package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WorldGenCanopyTree extends WorldGenAbstractTree
{
    private static final IBlockState DARK_OAK_LOG = Blocks.log2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK);
    private static final IBlockState DARK_OAK_LEAVES = Blocks.leaves2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    public WorldGenCanopyTree(boolean notify)
    {
        super(notify);
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int treeHeight = rand.nextInt(3) + rand.nextInt(2) + 6;
        int baseX = position.getX();
        int baseY = position.getY();
        int baseZ = position.getZ();

        if (baseY >= 1 && baseY + treeHeight + 1 < 256)
        {
            BlockPos groundPos = position.down();
            Block groundBlock = worldIn.getBlockState(groundPos).getBlock();

            if (groundBlock != Blocks.grass && groundBlock != Blocks.dirt)
            {
                return false;
            }
            else if (!this.isSpaceAt(worldIn, position, treeHeight))
            {
                return false;
            }
            else
            {
                this.setDirtAt(worldIn, groundPos);
                this.setDirtAt(worldIn, groundPos.east());
                this.setDirtAt(worldIn, groundPos.south());
                this.setDirtAt(worldIn, groundPos.south().east());
                EnumFacing growthDirection = EnumFacing.Plane.HORIZONTAL.random(rand);
                int leanStartHeight = treeHeight - rand.nextInt(4);
                int remainingLeanSteps = 2 - rand.nextInt(3);
                int trunkX = baseX;
                int trunkZ = baseZ;
                int topY = baseY + treeHeight - 1;

                for (int trunkOffset = 0; trunkOffset < treeHeight; ++trunkOffset)
                {
                    if (trunkOffset >= leanStartHeight && remainingLeanSteps > 0)
                    {
                        trunkX += growthDirection.getFrontOffsetX();
                        trunkZ += growthDirection.getFrontOffsetZ();
                        --remainingLeanSteps;
                    }

                    int trunkY = baseY + trunkOffset;
                    BlockPos trunkPos = new BlockPos(trunkX, trunkY, trunkZ);
                    Material material = worldIn.getBlockState(trunkPos).getBlock().getMaterial();

                    if (material == Material.air || material == Material.leaves)
                    {
                        this.placeLogAt(worldIn, trunkPos);
                        this.placeLogAt(worldIn, trunkPos.east());
                        this.placeLogAt(worldIn, trunkPos.south());
                        this.placeLogAt(worldIn, trunkPos.east().south());
                    }
                }

                for (int canopyOffsetX = -2; canopyOffsetX <= 0; ++canopyOffsetX)
                {
                    for (int canopyOffsetZ = -2; canopyOffsetZ <= 0; ++canopyOffsetZ)
                    {
                        int canopyYOffset = -1;
                        this.placeLeafAt(worldIn, trunkX + canopyOffsetX, topY + canopyYOffset, trunkZ + canopyOffsetZ);
                        this.placeLeafAt(worldIn, 1 + trunkX - canopyOffsetX, topY + canopyYOffset, trunkZ + canopyOffsetZ);
                        this.placeLeafAt(worldIn, trunkX + canopyOffsetX, topY + canopyYOffset, 1 + trunkZ - canopyOffsetZ);
                        this.placeLeafAt(worldIn, 1 + trunkX - canopyOffsetX, topY + canopyYOffset, 1 + trunkZ - canopyOffsetZ);

                        if ((canopyOffsetX > -2 || canopyOffsetZ > -1) && (canopyOffsetX != -1 || canopyOffsetZ != -2))
                        {
                            canopyYOffset = 1;
                            this.placeLeafAt(worldIn, trunkX + canopyOffsetX, topY + canopyYOffset, trunkZ + canopyOffsetZ);
                            this.placeLeafAt(worldIn, 1 + trunkX - canopyOffsetX, topY + canopyYOffset, trunkZ + canopyOffsetZ);
                            this.placeLeafAt(worldIn, trunkX + canopyOffsetX, topY + canopyYOffset, 1 + trunkZ - canopyOffsetZ);
                            this.placeLeafAt(worldIn, 1 + trunkX - canopyOffsetX, topY + canopyYOffset, 1 + trunkZ - canopyOffsetZ);
                        }
                    }
                }

                if (rand.nextBoolean())
                {
                    this.placeLeafAt(worldIn, trunkX, topY + 2, trunkZ);
                    this.placeLeafAt(worldIn, trunkX + 1, topY + 2, trunkZ);
                    this.placeLeafAt(worldIn, trunkX + 1, topY + 2, trunkZ + 1);
                    this.placeLeafAt(worldIn, trunkX, topY + 2, trunkZ + 1);
                }

                for (int leafOffsetX = -3; leafOffsetX <= 4; ++leafOffsetX)
                {
                    for (int leafOffsetZ = -3; leafOffsetZ <= 4; ++leafOffsetZ)
                    {
                        if ((leafOffsetX != -3 || leafOffsetZ != -3) && (leafOffsetX != -3 || leafOffsetZ != 4) && (leafOffsetX != 4 || leafOffsetZ != -3) && (leafOffsetX != 4 || leafOffsetZ != 4) && (Math.abs(leafOffsetX) < 3 || Math.abs(leafOffsetZ) < 3))
                        {
                            this.placeLeafAt(worldIn, trunkX + leafOffsetX, topY, trunkZ + leafOffsetZ);
                        }
                    }
                }

                for (int branchOffsetX = -1; branchOffsetX <= 2; ++branchOffsetX)
                {
                    for (int branchOffsetZ = -1; branchOffsetZ <= 2; ++branchOffsetZ)
                    {
                        if ((branchOffsetX < 0 || branchOffsetX > 1 || branchOffsetZ < 0 || branchOffsetZ > 1) && rand.nextInt(3) <= 0)
                        {
                            int branchLength = rand.nextInt(3) + 2;

                            for (int branchOffsetY = 0; branchOffsetY < branchLength; ++branchOffsetY)
                            {
                                this.placeLogAt(worldIn, new BlockPos(baseX + branchOffsetX, topY - branchOffsetY - 1, baseZ + branchOffsetZ));
                            }

                            for (int smallLeafOffsetX = -1; smallLeafOffsetX <= 1; ++smallLeafOffsetX)
                            {
                                for (int smallLeafOffsetZ = -1; smallLeafOffsetZ <= 1; ++smallLeafOffsetZ)
                                {
                                    this.placeLeafAt(worldIn, trunkX + branchOffsetX + smallLeafOffsetX, topY, trunkZ + branchOffsetZ + smallLeafOffsetZ);
                                }
                            }

                            for (int lowerLeafOffsetX = -2; lowerLeafOffsetX <= 2; ++lowerLeafOffsetX)
                            {
                                for (int lowerLeafOffsetZ = -2; lowerLeafOffsetZ <= 2; ++lowerLeafOffsetZ)
                                {
                                    if (Math.abs(lowerLeafOffsetX) != 2 || Math.abs(lowerLeafOffsetZ) != 2)
                                    {
                                        this.placeLeafAt(worldIn, trunkX + branchOffsetX + lowerLeafOffsetX, topY - 1, trunkZ + branchOffsetZ + lowerLeafOffsetZ);
                                    }
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    private boolean isSpaceAt(World worldIn, BlockPos position, int height)
    {
        int baseX = position.getX();
        int baseY = position.getY();
        int baseZ = position.getZ();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int verticalOffset = 0; verticalOffset <= height + 1; ++verticalOffset)
        {
            int checkRadius = 1;

            if (verticalOffset == 0)
            {
                checkRadius = 0;
            }

            if (verticalOffset >= height - 1)
            {
                checkRadius = 2;
            }

            for (int offsetX = -checkRadius; offsetX <= checkRadius; ++offsetX)
            {
                for (int offsetZ = -checkRadius; offsetZ <= checkRadius; ++offsetZ)
                {
                    if (!this.canGrowInto(worldIn.getBlockState(mutablePos.set(baseX + offsetX, baseY + verticalOffset, baseZ + offsetZ)).getBlock()))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void placeLogAt(World worldIn, BlockPos pos)
    {
        if (this.canGrowInto(worldIn.getBlockState(pos).getBlock()))
        {
            this.setBlockAndNotifyAdequately(worldIn, pos, DARK_OAK_LOG);
        }
    }

    private void placeLeafAt(World worldIn, int x, int y, int z)
    {
        BlockPos leafPos = new BlockPos(x, y, z);
        Block block = worldIn.getBlockState(leafPos).getBlock();

        if (block.getMaterial() == Material.air)
        {
            this.setBlockAndNotifyAdequately(worldIn, leafPos, DARK_OAK_LEAVES);
        }
    }
}
