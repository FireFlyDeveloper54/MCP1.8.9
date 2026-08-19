package net.minecraft.item;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;

public class ItemMap extends ItemMapBase
{
    protected ItemMap()
    {
        this.setHasSubtypes(true);
    }

    public static MapData loadMapData(int mapId, World worldIn)
    {
        String mapName = "map_" + mapId;
        MapData mapData = (MapData)worldIn.loadItemData(MapData.class, mapName);

        if (mapData == null)
        {
            mapData = new MapData(mapName);
            worldIn.setItemData(mapName, mapData);
        }

        return mapData;
    }

    public MapData getMapData(ItemStack stack, World worldIn)
    {
        String mapName = "map_" + stack.getMetadata();
        MapData mapData = (MapData)worldIn.loadItemData(MapData.class, mapName);

        if (mapData == null && !worldIn.isRemote)
        {
            stack.setItemDamage(worldIn.getUniqueDataId("map"));
            mapName = "map_" + stack.getMetadata();
            mapData = new MapData(mapName);
            mapData.scale = 3;
            mapData.calculateMapCenter((double)worldIn.getWorldInfo().getSpawnX(), (double)worldIn.getWorldInfo().getSpawnZ(), mapData.scale);
            mapData.dimension = (byte)worldIn.provider.getDimensionId();
            mapData.markDirty();
            worldIn.setItemData(mapName, mapData);
        }

        return mapData;
    }

    public void updateMapData(World worldIn, Entity viewer, MapData data)
    {
        if (worldIn.provider.getDimensionId() == data.dimension && viewer instanceof EntityPlayer)
        {
            int scale = 1 << data.scale;
            int centerX = data.xCenter;
            int centerZ = data.zCenter;
            int mapX = MathHelper.floor_double(viewer.posX - (double)centerX) / scale + 64;
            int mapZ = MathHelper.floor_double(viewer.posZ - (double)centerZ) / scale + 64;
            int mapRadius = 128 / scale;

            if (worldIn.provider.getHasNoSky())
            {
                mapRadius /= 2;
            }

            MapData.MapInfo mapInfo = data.getMapInfo((EntityPlayer)viewer);
            ++mapInfo.step;
            boolean requiresColumnUpdate = false;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            for (int mapPixelX = mapX - mapRadius + 1; mapPixelX < mapX + mapRadius; ++mapPixelX)
            {
                if ((mapPixelX & 15) == (mapInfo.step & 15) || requiresColumnUpdate)
                {
                    requiresColumnUpdate = false;
                    double previousHeightAverage = 0.0D;

                    for (int mapPixelZ = mapZ - mapRadius - 1; mapPixelZ < mapZ + mapRadius; ++mapPixelZ)
                    {
                        if (mapPixelX >= 0 && mapPixelZ >= -1 && mapPixelX < 128 && mapPixelZ < 128)
                        {
                            int deltaX = mapPixelX - mapX;
                            int deltaZ = mapPixelZ - mapZ;
                            boolean outsideUpdateRadius = deltaX * deltaX + deltaZ * deltaZ > (mapRadius - 2) * (mapRadius - 2);
                            int worldX = (centerX / scale + mapPixelX - 64) * scale;
                            int worldZ = (centerZ / scale + mapPixelZ - 64) * scale;
                            Multiset<MapColor> colorCounts = HashMultiset.<MapColor>create();
                            Chunk chunk = worldIn.getChunkFromBlockCoords(mutablePos.set(worldX, 0, worldZ));

                            if (!chunk.isEmpty())
                            {
                                int chunkX = worldX & 15;
                                int chunkZ = worldZ & 15;
                                int liquidDepth = 0;
                                double heightAverage = 0.0D;

                                if (worldIn.provider.getHasNoSky())
                                {
                                    int noSkyNoise = worldX + worldZ * 231871;
                                    noSkyNoise = noSkyNoise * noSkyNoise * 31287121 + noSkyNoise * 11;

                                    if ((noSkyNoise >> 20 & 1) == 0)
                                    {
                                        colorCounts.add(Blocks.dirt.getMapColor(Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT)), 10);
                                    }
                                    else
                                    {
                                        colorCounts.add(Blocks.stone.getMapColor(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE)), 100);
                                    }

                                    heightAverage = 100.0D;
                                }
                                else
                                {
                                    for (int sampleX = 0; sampleX < scale; ++sampleX)
                                    {
                                        for (int sampleZ = 0; sampleZ < scale; ++sampleZ)
                                        {
                                            int height = chunk.getHeightValue(sampleX + chunkX, sampleZ + chunkZ) + 1;
                                            IBlockState blockState = Blocks.air.getDefaultState();

                                            if (height > 1)
                                            {
                                                while (true)
                                                {
                                                    --height;
                                                    blockState = chunk.getBlockState(mutablePos.set(sampleX + chunkX, height, sampleZ + chunkZ));

                                                    if (blockState.getBlock().getMapColor(blockState) != MapColor.airColor || height <= 0)
                                                    {
                                                        break;
                                                    }
                                                }

                                                if (height > 0 && blockState.getBlock().getMaterial().isLiquid())
                                                {
                                                    int liquidY = height - 1;

                                                    while (true)
                                                    {
                                                        Block block = chunk.getBlock(sampleX + chunkX, liquidY--, sampleZ + chunkZ);
                                                        ++liquidDepth;

                                                        if (liquidY <= 0 || !block.getMaterial().isLiquid())
                                                        {
                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                            heightAverage += (double)height / (double)(scale * scale);
                                            colorCounts.add(blockState.getBlock().getMapColor(blockState));
                                        }
                                    }
                                }

                                liquidDepth = liquidDepth / (scale * scale);
                                double heightDelta = (heightAverage - previousHeightAverage) * 4.0D / (double)(scale + 4) + ((double)(mapPixelX + mapPixelZ & 1) - 0.5D) * 0.4D;
                                int shade = 1;

                                if (heightDelta > 0.6D)
                                {
                                    shade = 2;
                                }

                                if (heightDelta < -0.6D)
                                {
                                    shade = 0;
                                }

                                MapColor mapColor = (MapColor)Iterables.getFirst(Multisets.<MapColor>copyHighestCountFirst(colorCounts), MapColor.airColor);

                                if (mapColor == MapColor.waterColor)
                                {
                                    heightDelta = (double)liquidDepth * 0.1D + (double)(mapPixelX + mapPixelZ & 1) * 0.2D;
                                    shade = 1;

                                    if (heightDelta < 0.5D)
                                    {
                                        shade = 2;
                                    }

                                    if (heightDelta > 0.9D)
                                    {
                                        shade = 0;
                                    }
                                }

                                previousHeightAverage = heightAverage;

                                if (mapPixelZ >= 0 && deltaX * deltaX + deltaZ * deltaZ < mapRadius * mapRadius && (!outsideUpdateRadius || (mapPixelX + mapPixelZ & 1) != 0))
                                {
                                    byte existingColor = data.colors[mapPixelX + mapPixelZ * 128];
                                    byte newColor = (byte)(mapColor.colorIndex * 4 + shade);

                                    if (existingColor != newColor)
                                    {
                                        data.colors[mapPixelX + mapPixelZ * 128] = newColor;
                                        data.updateMapData(mapPixelX, mapPixelZ);
                                        requiresColumnUpdate = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!worldIn.isRemote)
        {
            MapData mapData = this.getMapData(stack, worldIn);

            if (entityIn instanceof EntityPlayer)
            {
                EntityPlayer entityPlayer = (EntityPlayer)entityIn;
                mapData.updateVisiblePlayers(entityPlayer, stack);
            }

            if (isSelected)
            {
                this.updateMapData(worldIn, entityIn, mapData);
            }
        }
    }

    public Packet createMapDataPacket(ItemStack stack, World worldIn, EntityPlayer player)
    {
        return this.getMapData(stack, worldIn).getMapPacket(stack, worldIn, player);
    }

    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("map_is_scaling"))
        {
            MapData mapData = Items.filled_map.getMapData(stack, worldIn);
            stack.setItemDamage(worldIn.getUniqueDataId("map"));
            MapData mapdata1 = new MapData("map_" + stack.getMetadata());
            mapdata1.scale = (byte)(mapData.scale + 1);

            if (mapdata1.scale > 4)
            {
                mapdata1.scale = 4;
            }

            mapdata1.calculateMapCenter((double)mapData.xCenter, (double)mapData.zCenter, mapdata1.scale);
            mapdata1.dimension = mapData.dimension;
            mapdata1.markDirty();
            worldIn.setItemData("map_" + stack.getMetadata(), mapdata1);
        }
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        MapData mapData = this.getMapData(stack, playerIn.worldObj);

        if (advanced)
        {
            if (mapData == null)
            {
                tooltip.add("Unknown map");
            }
            else
            {
                tooltip.add("Scaling at 1:" + (1 << mapData.scale));
                tooltip.add("(Level " + mapData.scale + "/" + 4 + ")");
            }
        }
    }
}
