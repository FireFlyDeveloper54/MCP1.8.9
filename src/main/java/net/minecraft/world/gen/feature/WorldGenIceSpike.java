package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class WorldGenIceSpike extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        while (worldIn.isAirBlock(position) && position.getY() > 2)
        {
            position = position.down();
        }

        if (worldIn.getBlockState(position).getBlock() != Blocks.snow)
        {
            return false;
        }
        else
        {
            position = position.up(rand.nextInt(4));
            int spikeHeight = rand.nextInt(4) + 7;
            int baseRadius = spikeHeight / 4 + rand.nextInt(2);

            if (baseRadius > 1 && rand.nextInt(60) == 0)
            {
                position = position.up(10 + rand.nextInt(30));
            }

            for (int heightOffset = 0; heightOffset < spikeHeight; ++heightOffset)
            {
                float layerRadius = (1.0F - (float)heightOffset / (float)spikeHeight) * (float)baseRadius;
                int radiusCeil = MathHelper.ceiling_float_int(layerRadius);

                for (int offsetX = -radiusCeil; offsetX <= radiusCeil; ++offsetX)
                {
                    float normalizedX = (float)MathHelper.abs_int(offsetX) - 0.25F;

                    for (int offsetZ = -radiusCeil; offsetZ <= radiusCeil; ++offsetZ)
                    {
                        float normalizedZ = (float)MathHelper.abs_int(offsetZ) - 0.25F;

                        if ((offsetX == 0 && offsetZ == 0 || normalizedX * normalizedX + normalizedZ * normalizedZ <= layerRadius * layerRadius) && (offsetX != -radiusCeil && offsetX != radiusCeil && offsetZ != -radiusCeil && offsetZ != radiusCeil || rand.nextFloat() <= 0.75F))
                        {
                            Block block = worldIn.getBlockState(position.add(offsetX, heightOffset, offsetZ)).getBlock();

                            if (block.getMaterial() == Material.air || block == Blocks.dirt || block == Blocks.snow || block == Blocks.ice)
                            {
                                this.setBlockAndNotifyAdequately(worldIn, position.add(offsetX, heightOffset, offsetZ), Blocks.packed_ice.getDefaultState());
                            }

                            if (heightOffset != 0 && radiusCeil > 1)
                            {
                                block = worldIn.getBlockState(position.add(offsetX, -heightOffset, offsetZ)).getBlock();

                                if (block.getMaterial() == Material.air || block == Blocks.dirt || block == Blocks.snow || block == Blocks.ice)
                                {
                                    this.setBlockAndNotifyAdequately(worldIn, position.add(offsetX, -heightOffset, offsetZ), Blocks.packed_ice.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }

            int rootRadius = baseRadius - 1;

            if (rootRadius < 0)
            {
                rootRadius = 0;
            }
            else if (rootRadius > 1)
            {
                rootRadius = 1;
            }

            for (int rootOffsetX = -rootRadius; rootOffsetX <= rootRadius; ++rootOffsetX)
            {
                for (int rootOffsetZ = -rootRadius; rootOffsetZ <= rootRadius; ++rootOffsetZ)
                {
                    BlockPos rootPos = position.add(rootOffsetX, -1, rootOffsetZ);
                    int remainingRootLength = 50;

                    if (Math.abs(rootOffsetX) == 1 && Math.abs(rootOffsetZ) == 1)
                    {
                        remainingRootLength = rand.nextInt(5);
                    }

                    while (rootPos.getY() > 50)
                    {
                        Block rootBlock = worldIn.getBlockState(rootPos).getBlock();

                        if (rootBlock.getMaterial() != Material.air && rootBlock != Blocks.dirt && rootBlock != Blocks.snow && rootBlock != Blocks.ice && rootBlock != Blocks.packed_ice)
                        {
                            break;
                        }

                        this.setBlockAndNotifyAdequately(worldIn, rootPos, Blocks.packed_ice.getDefaultState());
                        rootPos = rootPos.down();
                        --remainingRootLength;

                        if (remainingRootLength <= 0)
                        {
                            rootPos = rootPos.down(rand.nextInt(5) + 1);
                            remainingRootLength = rand.nextInt(5);
                        }
                    }
                }
            }

            return true;
        }
    }
}
