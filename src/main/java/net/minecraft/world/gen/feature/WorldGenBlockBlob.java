package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenBlockBlob extends WorldGenerator
{
    private final Block blobBlock;
    private final int startRadius;

    public WorldGenBlockBlob(Block blockIn, int startRadius)
    {
        super(false);
        this.blobBlock = blockIn;
        this.startRadius = startRadius;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        while (true)
        {
            label0:
            {
                if (position.getY() > 3)
                {
                    if (worldIn.isAirBlock(position.down()))
                    {
                        break label0;
                    }

                    Block supportBlock = worldIn.getBlockState(position.down()).getBlock();

                    if (supportBlock != Blocks.grass && supportBlock != Blocks.dirt && supportBlock != Blocks.stone)
                    {
                        break label0;
                    }
                }

                if (position.getY() <= 3)
                {
                    return false;
                }

                int blobRadius = this.startRadius;

                for (int blobIndex = 0; blobRadius >= 0 && blobIndex < 3; ++blobIndex)
                {
                    int xRadius = blobRadius + rand.nextInt(2);
                    int yRadius = blobRadius + rand.nextInt(2);
                    int zRadius = blobRadius + rand.nextInt(2);
                    float averageRadius = (float)(xRadius + yRadius + zRadius) * 0.333F + 0.5F;

                    for (BlockPos blockPos : BlockPos.getAllInBoxMutable(position.add(-xRadius, -yRadius, -zRadius), position.add(xRadius, yRadius, zRadius)))
                    {
                        if (blockPos.distanceSq(position) <= (double)(averageRadius * averageRadius))
                        {
                            worldIn.setBlockState(blockPos, this.blobBlock.getDefaultState(), 4);
                        }
                    }

                    position = position.add(-(blobRadius + 1) + rand.nextInt(2 + blobRadius * 2), 0 - rand.nextInt(2), -(blobRadius + 1) + rand.nextInt(2 + blobRadius * 2));
                }

                return true;
            }
            position = position.down();
        }
    }
}
