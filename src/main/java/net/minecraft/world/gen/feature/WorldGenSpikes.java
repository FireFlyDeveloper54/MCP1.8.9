package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenSpikes extends WorldGenerator
{
    private Block baseBlockRequired;

    public WorldGenSpikes(Block baseBlockRequiredIn)
    {
        this.baseBlockRequired = baseBlockRequiredIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if (worldIn.isAirBlock(position) && worldIn.getBlockState(position.down()).getBlock() == this.baseBlockRequired)
        {
            int spikeHeight = rand.nextInt(32) + 6;
            int spikeRadius = rand.nextInt(4) + 1;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            for (int baseX = position.getX() - spikeRadius; baseX <= position.getX() + spikeRadius; ++baseX)
            {
                for (int baseZ = position.getZ() - spikeRadius; baseZ <= position.getZ() + spikeRadius; ++baseZ)
                {
                    int deltaX = baseX - position.getX();
                    int deltaZ = baseZ - position.getZ();

                    if (deltaX * deltaX + deltaZ * deltaZ <= spikeRadius * spikeRadius + 1 && worldIn.getBlockState(mutablePos.set(baseX, position.getY() - 1, baseZ)).getBlock() != this.baseBlockRequired)
                    {
                        return false;
                    }
                }
            }

            for (int spikeY = position.getY(); spikeY < position.getY() + spikeHeight && spikeY < 256; ++spikeY)
            {
                for (int spikeX = position.getX() - spikeRadius; spikeX <= position.getX() + spikeRadius; ++spikeX)
                {
                    for (int spikeZ = position.getZ() - spikeRadius; spikeZ <= position.getZ() + spikeRadius; ++spikeZ)
                    {
                        int deltaX = spikeX - position.getX();
                        int deltaZ = spikeZ - position.getZ();

                        if (deltaX * deltaX + deltaZ * deltaZ <= spikeRadius * spikeRadius + 1)
                        {
                            worldIn.setBlockState(mutablePos.set(spikeX, spikeY, spikeZ), Blocks.obsidian.getDefaultState(), 2);
                        }
                    }
                }
            }

            Entity entity = new EntityEnderCrystal(worldIn);
            entity.setLocationAndAngles((double)((float)position.getX() + 0.5F), (double)(position.getY() + spikeHeight), (double)((float)position.getZ() + 0.5F), rand.nextFloat() * 360.0F, 0.0F);
            worldIn.spawnEntityInWorld(entity);
            worldIn.setBlockState(position.up(spikeHeight), Blocks.bedrock.getDefaultState(), 2);
            return true;
        }
        else
        {
            return false;
        }
    }
}
