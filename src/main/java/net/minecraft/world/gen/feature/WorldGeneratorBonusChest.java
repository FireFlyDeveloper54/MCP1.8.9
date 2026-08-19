package net.minecraft.world.gen.feature;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;

public class WorldGeneratorBonusChest extends WorldGenerator
{
    private final List<WeightedRandomChestContent> chestItems;
    private final int itemsToGenerateInBonusChest;

    public WorldGeneratorBonusChest(List<WeightedRandomChestContent> chestItemsIn, int itemsToGenerateInBonusChestIn)
    {
        this.chestItems = chestItemsIn;
        this.itemsToGenerateInBonusChest = itemsToGenerateInBonusChestIn;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        Block block;

        while (((block = worldIn.getBlockState(position).getBlock()).getMaterial() == Material.air || block.getMaterial() == Material.leaves) && position.getY() > 1)
        {
            position = position.down();
        }

        if (position.getY() < 1)
        {
            return false;
        }
        else
        {
            position = position.up();

            for (int attempt = 0; attempt < 4; ++attempt)
            {
                BlockPos chestPos = position.add(rand.nextInt(4) - rand.nextInt(4), rand.nextInt(3) - rand.nextInt(3), rand.nextInt(4) - rand.nextInt(4));

                if (worldIn.isAirBlock(chestPos) && World.doesBlockHaveSolidTopSurface(worldIn, chestPos.down()))
                {
                    worldIn.setBlockState(chestPos, Blocks.chest.getDefaultState(), 2);
                    TileEntity tileEntity = worldIn.getTileEntity(chestPos);

                    if (tileEntity instanceof TileEntityChest)
                    {
                        WeightedRandomChestContent.generateChestContents(rand, this.chestItems, (TileEntityChest)tileEntity, this.itemsToGenerateInBonusChest);
                    }

                    BlockPos eastTorchPos = chestPos.east();
                    BlockPos westTorchPos = chestPos.west();
                    BlockPos northTorchPos = chestPos.north();
                    BlockPos southTorchPos = chestPos.south();

                    if (worldIn.isAirBlock(westTorchPos) && World.doesBlockHaveSolidTopSurface(worldIn, westTorchPos.down()))
                    {
                        worldIn.setBlockState(westTorchPos, Blocks.torch.getDefaultState(), 2);
                    }

                    if (worldIn.isAirBlock(eastTorchPos) && World.doesBlockHaveSolidTopSurface(worldIn, eastTorchPos.down()))
                    {
                        worldIn.setBlockState(eastTorchPos, Blocks.torch.getDefaultState(), 2);
                    }

                    if (worldIn.isAirBlock(northTorchPos) && World.doesBlockHaveSolidTopSurface(worldIn, northTorchPos.down()))
                    {
                        worldIn.setBlockState(northTorchPos, Blocks.torch.getDefaultState(), 2);
                    }

                    if (worldIn.isAirBlock(southTorchPos) && World.doesBlockHaveSolidTopSurface(worldIn, southTorchPos.down()))
                    {
                        worldIn.setBlockState(southTorchPos, Blocks.torch.getDefaultState(), 2);
                    }

                    return true;
                }
            }

            return false;
        }
    }
}
