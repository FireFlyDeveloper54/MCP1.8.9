package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenDungeons extends WorldGenerator
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String[] SPAWNERTYPES = new String[] {"Skeleton", "Zombie", "Zombie", "Spider"};
    private static final List<WeightedRandomChestContent> CHESTCONTENT = Lists.newArrayList(new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 10), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 4, 10), new WeightedRandomChestContent(Items.bread, 0, 1, 1, 10), new WeightedRandomChestContent(Items.wheat, 0, 1, 4, 10), new WeightedRandomChestContent(Items.gunpowder, 0, 1, 4, 10), new WeightedRandomChestContent(Items.string, 0, 1, 4, 10), new WeightedRandomChestContent(Items.bucket, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_apple, 0, 1, 1, 1), new WeightedRandomChestContent(Items.redstone, 0, 1, 4, 10), new WeightedRandomChestContent(Items.record_13, 0, 1, 1, 4), new WeightedRandomChestContent(Items.record_cat, 0, 1, 1, 4), new WeightedRandomChestContent(Items.name_tag, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 2), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 5), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1)});

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        int roomHeight = 3;
        int xRadius = rand.nextInt(2) + 2;
        int minXOffset = -xRadius - 1;
        int maxXOffset = xRadius + 1;
        int floorYOffset = -1;
        int ceilingYOffset = roomHeight + 1;
        int zRadius = rand.nextInt(2) + 2;
        int minZOffset = -zRadius - 1;
        int maxZOffset = zRadius + 1;
        int doorwayCount = 0;

        for (int xOffset = minXOffset; xOffset <= maxXOffset; ++xOffset)
        {
            for (int yOffset = floorYOffset; yOffset <= ceilingYOffset; ++yOffset)
            {
                for (int zOffset = minZOffset; zOffset <= maxZOffset; ++zOffset)
                {
                    BlockPos blockPos = position.add(xOffset, yOffset, zOffset);
                    Material material = worldIn.getBlockState(blockPos).getBlock().getMaterial();
                    boolean isSolid = material.isSolid();

                    if (yOffset == floorYOffset && !isSolid)
                    {
                        return false;
                    }

                    if (yOffset == ceilingYOffset && !isSolid)
                    {
                        return false;
                    }

                    if ((xOffset == minXOffset || xOffset == maxXOffset || zOffset == minZOffset || zOffset == maxZOffset) && yOffset == 0 && worldIn.isAirBlock(blockPos) && worldIn.isAirBlock(blockPos.up()))
                    {
                        ++doorwayCount;
                    }
                }
            }
        }

        if (doorwayCount >= 1 && doorwayCount <= 5)
        {
            for (int xOffset = minXOffset; xOffset <= maxXOffset; ++xOffset)
            {
                for (int yOffset = roomHeight; yOffset >= floorYOffset; --yOffset)
                {
                    for (int zOffset = minZOffset; zOffset <= maxZOffset; ++zOffset)
                    {
                        BlockPos blockPos = position.add(xOffset, yOffset, zOffset);

                        if (xOffset != minXOffset && yOffset != floorYOffset && zOffset != minZOffset && xOffset != maxXOffset && yOffset != ceilingYOffset && zOffset != maxZOffset)
                        {
                            if (worldIn.getBlockState(blockPos).getBlock() != Blocks.chest)
                            {
                                worldIn.setBlockToAir(blockPos);
                            }
                        }
                        else if (blockPos.getY() >= 0 && !worldIn.getBlockState(blockPos.down()).getBlock().getMaterial().isSolid())
                        {
                            worldIn.setBlockToAir(blockPos);
                        }
                        else if (worldIn.getBlockState(blockPos).getBlock().getMaterial().isSolid() && worldIn.getBlockState(blockPos).getBlock() != Blocks.chest)
                        {
                            if (yOffset == floorYOffset && rand.nextInt(4) != 0)
                            {
                                worldIn.setBlockState(blockPos, Blocks.mossy_cobblestone.getDefaultState(), 2);
                            }
                            else
                            {
                                worldIn.setBlockState(blockPos, Blocks.cobblestone.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }

            for (int chestIndex = 0; chestIndex < 2; ++chestIndex)
            {
                for (int chestAttempt = 0; chestAttempt < 3; ++chestAttempt)
                {
                    int chestX = position.getX() + rand.nextInt(xRadius * 2 + 1) - xRadius;
                    int chestY = position.getY();
                    int chestZ = position.getZ() + rand.nextInt(zRadius * 2 + 1) - zRadius;
                    BlockPos chestPos = new BlockPos(chestX, chestY, chestZ);

                    if (worldIn.isAirBlock(chestPos))
                    {
                        int solidHorizontalNeighbors = 0;

                        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                        {
                            if (worldIn.getBlockState(chestPos.offset(facing)).getBlock().getMaterial().isSolid())
                            {
                                ++solidHorizontalNeighbors;
                            }
                        }

                        if (solidHorizontalNeighbors == 1)
                        {
                            worldIn.setBlockState(chestPos, Blocks.chest.correctFacing(worldIn, chestPos, Blocks.chest.getDefaultState()), 2);
                            List<WeightedRandomChestContent> chestLoot = WeightedRandomChestContent.addRandomChestContents(CHESTCONTENT, new WeightedRandomChestContent[] {Items.enchanted_book.getRandom(rand)});
                            TileEntity chestTileEntity = worldIn.getTileEntity(chestPos);

                            if (chestTileEntity instanceof TileEntityChest)
                            {
                                WeightedRandomChestContent.generateChestContents(rand, chestLoot, (TileEntityChest)chestTileEntity, 8);
                            }

                            break;
                        }
                    }
                }
            }

            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState(), 2);
            TileEntity spawnerTileEntity = worldIn.getTileEntity(position);

            if (spawnerTileEntity instanceof TileEntityMobSpawner)
            {
                ((TileEntityMobSpawner)spawnerTileEntity).getSpawnerBaseLogic().setEntityName(this.pickMobSpawner(rand));
            }
            else
            {
                LOGGER.error("Failed to fetch mob spawner entity at (" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    private String pickMobSpawner(Random random)
    {
        return SPAWNERTYPES[random.nextInt(SPAWNERTYPES.length)];
    }
}
