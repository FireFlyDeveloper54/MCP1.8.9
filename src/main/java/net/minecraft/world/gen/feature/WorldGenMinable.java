package net.minecraft.world.gen.feature;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class WorldGenMinable extends WorldGenerator
{
    private final IBlockState oreBlock;
    private final int numberOfBlocks;
    private final Predicate<IBlockState> predicate;

    public WorldGenMinable(IBlockState state, int blockCount)
    {
        this(state, blockCount, BlockHelper.forBlock(Blocks.stone));
    }

    public WorldGenMinable(IBlockState state, int blockCount, Predicate<IBlockState> predicateIn)
    {
        this.oreBlock = state;
        this.numberOfBlocks = blockCount;
        this.predicate = predicateIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        float veinAngle = rand.nextFloat() * (float)Math.PI;
        double startX = (double)((float)(position.getX() + 8) + MathHelper.sin(veinAngle) * (float)this.numberOfBlocks / 8.0F);
        double endX = (double)((float)(position.getX() + 8) - MathHelper.sin(veinAngle) * (float)this.numberOfBlocks / 8.0F);
        double startZ = (double)((float)(position.getZ() + 8) + MathHelper.cos(veinAngle) * (float)this.numberOfBlocks / 8.0F);
        double endZ = (double)((float)(position.getZ() + 8) - MathHelper.cos(veinAngle) * (float)this.numberOfBlocks / 8.0F);
        double startY = (double)(position.getY() + rand.nextInt(3) - 2);
        double endY = (double)(position.getY() + rand.nextInt(3) - 2);
        BlockPos.MutableBlockPos orePos = new BlockPos.MutableBlockPos();

        for (int blockIndex = 0; blockIndex < this.numberOfBlocks; ++blockIndex)
        {
            float progress = (float)blockIndex / (float)this.numberOfBlocks;
            double centerX = startX + (endX - startX) * (double)progress;
            double centerY = startY + (endY - startY) * (double)progress;
            double centerZ = startZ + (endZ - startZ) * (double)progress;
            double randomRadius = rand.nextDouble() * (double)this.numberOfBlocks / 16.0D;
            double horizontalDiameter = (double)(MathHelper.sin((float)Math.PI * progress) + 1.0F) * randomRadius + 1.0D;
            double verticalDiameter = (double)(MathHelper.sin((float)Math.PI * progress) + 1.0F) * randomRadius + 1.0D;
            int minX = MathHelper.floor_double(centerX - horizontalDiameter / 2.0D);
            int minY = MathHelper.floor_double(centerY - verticalDiameter / 2.0D);
            int minZ = MathHelper.floor_double(centerZ - horizontalDiameter / 2.0D);
            int maxX = MathHelper.floor_double(centerX + horizontalDiameter / 2.0D);
            int maxY = MathHelper.floor_double(centerY + verticalDiameter / 2.0D);
            int maxZ = MathHelper.floor_double(centerZ + horizontalDiameter / 2.0D);

            for (int oreX = minX; oreX <= maxX; ++oreX)
            {
                double normalizedX = ((double)oreX + 0.5D - centerX) / (horizontalDiameter / 2.0D);

                if (normalizedX * normalizedX < 1.0D)
                {
                    for (int oreY = minY; oreY <= maxY; ++oreY)
                    {
                        double normalizedY = ((double)oreY + 0.5D - centerY) / (verticalDiameter / 2.0D);

                        if (normalizedX * normalizedX + normalizedY * normalizedY < 1.0D)
                        {
                            for (int oreZ = minZ; oreZ <= maxZ; ++oreZ)
                            {
                                double normalizedZ = ((double)oreZ + 0.5D - centerZ) / (horizontalDiameter / 2.0D);

                                if (normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ < 1.0D)
                                {
                                    orePos.set(oreX, oreY, oreZ);

                                    if (this.predicate.apply(worldIn.getBlockState(orePos)))
                                    {
                                        worldIn.setBlockState(orePos, this.oreBlock, 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
