package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class WorldGenMegaJungle extends WorldGenHugeTrees
{
    public WorldGenMegaJungle(boolean notify, int baseHeightIn, int extraRandomHeightIn, IBlockState woodMetadataIn, IBlockState leavesMetadataIn)
    {
        super(notify, baseHeightIn, extraRandomHeightIn, woodMetadataIn, leavesMetadataIn);
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
            this.createCrown(worldIn, position.up(treeHeight), 2);

            for (int branchY = position.getY() + treeHeight - 2 - rand.nextInt(4); branchY > position.getY() + treeHeight / 2; branchY -= 2 + rand.nextInt(4))
            {
                float branchAngle = rand.nextFloat() * (float)Math.PI * 2.0F;
                int branchX = position.getX() + (int)(0.5F + MathHelper.cos(branchAngle) * 4.0F);
                int branchZ = position.getZ() + (int)(0.5F + MathHelper.sin(branchAngle) * 4.0F);

                for (int branchStep = 0; branchStep < 5; ++branchStep)
                {
                    branchX = position.getX() + (int)(1.5F + MathHelper.cos(branchAngle) * (float)branchStep);
                    branchZ = position.getZ() + (int)(1.5F + MathHelper.sin(branchAngle) * (float)branchStep);
                    this.setBlockAndNotifyAdequately(worldIn, new BlockPos(branchX, branchY - 3 + branchStep / 2, branchZ), this.woodMetadata);
                }

                int branchLeafRadius = 1 + rand.nextInt(2);
                int branchLeafTopY = branchY;

                for (int leafY = branchY - branchLeafRadius; leafY <= branchLeafTopY; ++leafY)
                {
                    int leafYOffset = leafY - branchLeafTopY;
                    this.growLeavesLayer(worldIn, new BlockPos(branchX, leafY, branchZ), 1 - leafYOffset);
                }
            }

            for (int trunkY = 0; trunkY < treeHeight; ++trunkY)
            {
                BlockPos trunkPos = position.up(trunkY);

                if (this.canGrowInto(worldIn.getBlockState(trunkPos).getBlock()))
                {
                    this.setBlockAndNotifyAdequately(worldIn, trunkPos, this.woodMetadata);

                    if (trunkY > 0)
                    {
                        this.addVine(worldIn, rand, trunkPos.west(), BlockVine.EAST);
                        this.addVine(worldIn, rand, trunkPos.north(), BlockVine.SOUTH);
                    }
                }

                if (trunkY < treeHeight - 1)
                {
                    BlockPos eastTrunkPos = trunkPos.east();

                    if (this.canGrowInto(worldIn.getBlockState(eastTrunkPos).getBlock()))
                    {
                        this.setBlockAndNotifyAdequately(worldIn, eastTrunkPos, this.woodMetadata);

                        if (trunkY > 0)
                        {
                            this.addVine(worldIn, rand, eastTrunkPos.east(), BlockVine.WEST);
                            this.addVine(worldIn, rand, eastTrunkPos.north(), BlockVine.SOUTH);
                        }
                    }

                    BlockPos southeastTrunkPos = trunkPos.south().east();

                    if (this.canGrowInto(worldIn.getBlockState(southeastTrunkPos).getBlock()))
                    {
                        this.setBlockAndNotifyAdequately(worldIn, southeastTrunkPos, this.woodMetadata);

                        if (trunkY > 0)
                        {
                            this.addVine(worldIn, rand, southeastTrunkPos.east(), BlockVine.WEST);
                            this.addVine(worldIn, rand, southeastTrunkPos.south(), BlockVine.NORTH);
                        }
                    }

                    BlockPos southTrunkPos = trunkPos.south();

                    if (this.canGrowInto(worldIn.getBlockState(southTrunkPos).getBlock()))
                    {
                        this.setBlockAndNotifyAdequately(worldIn, southTrunkPos, this.woodMetadata);

                        if (trunkY > 0)
                        {
                            this.addVine(worldIn, rand, southTrunkPos.west(), BlockVine.EAST);
                            this.addVine(worldIn, rand, southTrunkPos.south(), BlockVine.NORTH);
                        }
                    }
                }
            }

            return true;
        }
    }

    private void addVine(World worldIn, Random rand, BlockPos pos, PropertyBool side)
    {
        if (rand.nextInt(3) > 0 && worldIn.isAirBlock(pos))
        {
            this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.vine.getDefaultState().withProperty(side, Boolean.valueOf(true)));
        }
    }

    private void createCrown(World worldIn, BlockPos pos, int radius)
    {
        int crownDepth = 2;

        for (int layerOffset = -crownDepth; layerOffset <= 0; ++layerOffset)
        {
            this.growLeavesLayerStrict(worldIn, pos.up(layerOffset), radius + 1 - layerOffset);
        }
    }
}
