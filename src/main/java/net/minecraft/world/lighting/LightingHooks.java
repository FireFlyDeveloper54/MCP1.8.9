package net.minecraft.world.lighting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@SuppressWarnings("unused")
public final class LightingHooks {
    private static final Logger logger = LogManager.getLogger();
    private static final EnumSkyBlock[] ENUM_SKY_BLOCK_VALUES = EnumSkyBlock.values();

    private static final EnumFacing.AxisDirection[] ENUM_AXIS_DIRECTION_VALUES = EnumFacing.AxisDirection.values();
    private static final EnumFacing[] HORIZONTAL_FACINGS = EnumFacing.Plane.HORIZONTAL.facings();

    private static final int FLAG_COUNT = 32;

    private LightingHooks() {
    }

    public static void relightSkylightColumn(final World world, final Chunk chunk, final int x, final int z, final int height1, final int height2) {
        final int yMin = Math.min(height1, height2);
        final int yMax = Math.max(height1, height2) - 1;

        final ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();

        final int xBase = (chunk.xPosition << 4) + x;
        final int zBase = (chunk.zPosition << 4) + z;

        scheduleRelightChecksForColumn(world, EnumSkyBlock.SKY, xBase, zBase, yMin, yMax);

        if (sections[yMin >> 4] == null && yMin > 0) {
            world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(xBase, yMin - 1, zBase));
        }

        short emptySections = 0;

        for (int sec = yMax >> 4; sec >= yMin >> 4; --sec) {
            if (sections[sec] == null) {
                emptySections |= 1 << sec;
            }
        }

        if (emptySections != 0) {
            for (final EnumFacing dir : HORIZONTAL_FACINGS) {
                final int xOffset = dir.getFrontOffsetX();
                final int zOffset = dir.getFrontOffsetZ();

                final boolean neighborColumnExists = (((x + xOffset) | (z + zOffset)) & 16) == 0

                        || LightingEngineHelpers.getLoadedChunk(world.getChunkProvider(),chunk.xPosition + xOffset, chunk.zPosition + zOffset) != null;

                if (neighborColumnExists) {
                    for (int sec = yMax >> 4; sec >= yMin >> 4; --sec) {
                        if ((emptySections & (1 << sec)) != 0) {
                            scheduleRelightChecksForColumn(world, EnumSkyBlock.SKY, xBase + xOffset, zBase + zOffset, sec << 4, (sec << 4) + 15);
                        }
                    }
                } else {
                    flagChunkBoundaryForUpdate(chunk, emptySections, EnumSkyBlock.SKY, dir, getAxisDirection(dir, x, z), EnumBoundaryFacing.OUT);
                }
            }
        }
    }

    public static void scheduleRelightChecksForArea(final World world, final EnumSkyBlock lightType, final int xMin, final int yMin, final int zMin, final int xMax, final int yMax, final int zMax) {
        for (int x = xMin; x <= xMax; ++x) {
            for (int z = zMin; z <= zMax; ++z) {
                scheduleRelightChecksForColumn(world, lightType, x, z, yMin, yMax);
            }
        }
    }

    private static void scheduleRelightChecksForColumn(final World world, final EnumSkyBlock lightType, final int x, final int z, final int yMin, final int yMax) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = yMin; y <= yMax; ++y) {
            world.checkLightFor(lightType, pos.set(x, y, z));
        }
    }

    public enum EnumBoundaryFacing {
        IN, OUT;

        public EnumBoundaryFacing getOpposite() {
            return this == IN ? OUT : IN;
        }
    }

    public static void flagSecBoundaryForUpdate(final Chunk chunk, final BlockPos pos, final EnumSkyBlock lightType, final EnumFacing dir, final EnumBoundaryFacing boundaryFacing) {
        flagChunkBoundaryForUpdate(chunk, (short) (1 << (pos.getY() >> 4)), lightType, dir, getAxisDirection(dir, pos.getX(), pos.getZ()), boundaryFacing);
    }

    public static void flagChunkBoundaryForUpdate(final Chunk chunk, final short sectionMask, final EnumSkyBlock lightType, final EnumFacing dir, final EnumFacing.AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        initNeighborLightChecks(chunk);
        chunk.getNeighborLightChecks()[getFlagIndex(lightType, dir, axisDirection, boundaryFacing)] |= sectionMask;
        chunk.setChunkModified();
    }

    public static int getFlagIndex(final EnumSkyBlock lightType, final int xOffset, final int zOffset, final EnumFacing.AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        EnumFacing direction;

        if (xOffset < 0) {
            direction = EnumFacing.WEST;
        } else if (xOffset > 0) {
            direction = EnumFacing.EAST;
        } else if (zOffset < 0) {
            direction = EnumFacing.NORTH;
        } else if (zOffset > 0) {
            direction = EnumFacing.SOUTH;
        } else {
            throw new IllegalArgumentException("Boundary direction cannot be zero");
        }

        return getFlagIndex(lightType, direction, axisDirection, boundaryFacing);
    }

    public static int getFlagIndex(final EnumSkyBlock lightType, final EnumFacing dir, final EnumFacing.AxisDirection axisDirection, final EnumBoundaryFacing boundaryFacing) {
        int lightTypeOffset = lightType == EnumSkyBlock.SKY ? 16 : 0;
        int directionOffset = dir.getHorizontalIndex() << 2;
        int axisOffset = axisDirection == EnumFacing.AxisDirection.POSITIVE ? 2 : 0;
        return lightTypeOffset | directionOffset | axisOffset | boundaryFacing.ordinal();
    }

    private static EnumFacing.AxisDirection getAxisDirection(final EnumFacing dir, final int x, final int z) {
        return ((dir.getAxis() == EnumFacing.Axis.X ? z : x) & 15) < 8 ? EnumFacing.AxisDirection.NEGATIVE : EnumFacing.AxisDirection.POSITIVE;
    }

    public static void scheduleRelightChecksForChunkBoundaries(final World world, final Chunk chunk) {
        for (final EnumFacing dir : HORIZONTAL_FACINGS) {
            final int xOffset = dir.getFrontOffsetX();
            final int zOffset = dir.getFrontOffsetZ();

            final Chunk nChunk = LightingEngineHelpers.getLoadedChunk(world.getChunkProvider(),chunk.xPosition + xOffset, chunk.zPosition + zOffset);

            if (nChunk == null) {
                continue;
            }

            for (final EnumSkyBlock lightType : ENUM_SKY_BLOCK_VALUES) {
                for (final EnumFacing.AxisDirection axisDir : ENUM_AXIS_DIRECTION_VALUES) {

                    mergeFlags(lightType, chunk, nChunk, dir, axisDir);
                    mergeFlags(lightType, nChunk, chunk, dir.getOpposite(), axisDir);


                    scheduleRelightChecksForBoundary(world, chunk, nChunk, null, lightType, xOffset, zOffset, axisDir);
                    scheduleRelightChecksForBoundary(world, nChunk, chunk, null, lightType, -xOffset, -zOffset, axisDir);

                    scheduleRelightChecksForBoundary(world, nChunk, null, chunk, lightType, (zOffset != 0 ? axisDir.getOffset() : 0),
                            (xOffset != 0 ? axisDir.getOffset() : 0),
                            dir.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ?
                                    EnumFacing.AxisDirection.NEGATIVE :
                                    EnumFacing.AxisDirection.POSITIVE);
                }
            }
        }
    }

    private static void mergeFlags(final EnumSkyBlock lightType, final Chunk inChunk, final Chunk outChunk, final EnumFacing dir, final EnumFacing.AxisDirection axisDir) {
        if (outChunk.getNeighborLightChecks() == null) {
            return;
        }

        initNeighborLightChecks(inChunk);

        final int inIndex = getFlagIndex(lightType, dir, axisDir, EnumBoundaryFacing.IN);
        final int outIndex = getFlagIndex(lightType, dir.getOpposite(), axisDir, EnumBoundaryFacing.OUT);

        inChunk.getNeighborLightChecks()[inIndex] |= outChunk.getNeighborLightChecks()[outIndex];

    }

    private static void scheduleRelightChecksForBoundary(final World world, final Chunk chunk, Chunk nChunk, Chunk sChunk, final EnumSkyBlock lightType, final int xOffset, final int zOffset, final EnumFacing.AxisDirection axisDir) {
        if (chunk.getNeighborLightChecks() == null) {
            return;
        }

        final int flagIndex = getFlagIndex(lightType, xOffset, zOffset, axisDir, EnumBoundaryFacing.IN);

        final int flags = chunk.getNeighborLightChecks()[flagIndex];

        if (flags == 0) {
            return;
        }

        if (nChunk == null) {
            nChunk = LightingEngineHelpers.getLoadedChunk(world.getChunkProvider(), chunk.xPosition + xOffset, chunk.zPosition + zOffset);

            if (nChunk == null) {
                return;
            }
        }

        if (sChunk == null) {
            sChunk = LightingEngineHelpers.getLoadedChunk(world.getChunkProvider(), chunk.xPosition + (zOffset != 0 ? axisDir.getOffset() : 0), chunk.zPosition + (xOffset != 0 ? axisDir.getOffset() : 0));

            if (sChunk == null) {
                return;
            }
        }

        final int reverseIndex = getFlagIndex(lightType, -xOffset, -zOffset, axisDir, EnumBoundaryFacing.OUT);

        chunk.getNeighborLightChecks()[flagIndex] = 0;

        if (nChunk.getNeighborLightChecks() != null) {
            nChunk.getNeighborLightChecks()[reverseIndex] = 0;
        }

        chunk.setChunkModified();
        nChunk.setChunkModified();


        int xMin = chunk.xPosition << 4;
        int zMin = chunk.zPosition << 4;


        if ((xOffset | zOffset) > 0) {
            xMin += 15 * xOffset;
            zMin += 15 * zOffset;
        }


        if (axisDir == EnumFacing.AxisDirection.POSITIVE) {
            xMin += 8 * (zOffset & 1);
            zMin += 8 * (xOffset & 1);
        }


        final int xMax = xMin + 7 * (zOffset & 1);
        final int zMax = zMin + 7 * (xOffset & 1);

        for (int y = 0; y < 16; ++y) {
            if ((flags & (1 << y)) != 0) {
                scheduleRelightChecksForArea(world, lightType, xMin, y << 4, zMin, xMax, (y << 4) + 15, zMax);
            }
        }
    }

    public static void initNeighborLightChecks(final Chunk chunk) {
        if (chunk.getNeighborLightChecks() == null) {
            chunk.setNeighborLightChecks(new short[FLAG_COUNT]);
        }
    }

    public static final String neighborLightChecksKey = "NeighborLightChecks";

    public static void writeNeighborLightChecksToNBT(final Chunk chunk, final NBTTagCompound nbt) {
        short[] neighborLightChecks = chunk.getNeighborLightChecks();

        if (neighborLightChecks == null) {
            return;
        }

        boolean empty = true;

        final NBTTagList list = new NBTTagList();

        for (final short flags : neighborLightChecks) {
            list.appendTag(new NBTTagShort(flags));

            if (flags != 0) {
                empty = false;
            }
        }

        if (!empty) {
            nbt.setTag(neighborLightChecksKey, list);
        }
    }

    public static void readNeighborLightChecksFromNBT(final Chunk chunk, final NBTTagCompound nbt) {
        if (nbt.hasKey(neighborLightChecksKey, 9)) {
            final NBTTagList list = nbt.getTagList(neighborLightChecksKey, 2);

            if (list.tagCount() == FLAG_COUNT) {
                initNeighborLightChecks(chunk);

                short[] neighborLightChecks = chunk.getNeighborLightChecks();

                for (int i = 0; i < FLAG_COUNT; ++i) {
                    neighborLightChecks[i] = ((NBTTagShort) list.get(i)).getShort();
                }
            } else {
                logger.warn("Chunk field {} had invalid length, ignoring it (chunk coordinates: {} {})", neighborLightChecksKey, chunk.xPosition, chunk.zPosition);
            }
        }
    }

    public static void initChunkLighting(final Chunk chunk, final World world) {
        final int xBase = chunk.xPosition << 4;
        final int zBase = chunk.zPosition << 4;

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(xBase, 0, zBase);

        if (world.isAreaLoaded(pos.add(-16, 0, -16), pos.add(31, 255, 31), false)) {
            final ExtendedBlockStorage[] extendedBlockStorage = chunk.getBlockStorageArray();

            for (int j = 0; j < extendedBlockStorage.length; ++j) {
                final ExtendedBlockStorage storage = extendedBlockStorage[j];

                if (storage == null) {
                    continue;
                }

                int yBase = j * 16;

                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            IBlockState state = storage.get(x, y, z);
                            if (state != null) {
                                int light = LightingEngineHelpers.getLightValueForState(state, world, pos);

                                if (light > 0) {
                                    pos.set(xBase + x, yBase + y, zBase + z);

                                    world.checkLightFor(EnumSkyBlock.BLOCK, pos);
                                }
                            }
                        }
                    }
                }
            }

            if (!world.provider.getHasNoSky()) {
                chunk.setSkylightUpdatedPublic();
            }

            chunk.setLightInitialized(true);
        }
    }

    public static void checkChunkLighting(final Chunk chunk, final World world) {
        if (!chunk.isLightInitialized()) {
            initChunkLighting(chunk, world);

            if (!chunk.isLightInitialized()) {
                return;
            }
        }

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                if (x != 0 || z != 0) {
                    Chunk nChunk = LightingEngineHelpers.getLoadedChunk(world.getChunkProvider(), chunk.xPosition + x, chunk.zPosition + z);

                    if (nChunk == null || !nChunk.isLightInitialized()) {
                        return;
                    }
                }
            }
        }

        chunk.setLightPopulated(true);
    }

    public static void initSkylightForSection(final World world, final Chunk chunk, final ExtendedBlockStorage section) {
        if (!world.provider.getHasNoSky()) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (chunk.getHeightValue(x, z) <= section.getYLocation()) {
                        for (int y = 0; y < 16; ++y) {
                            section.setExtSkylightValue(x, y, z, EnumSkyBlock.SKY.defaultLightValue);
                        }
                    }
                }
            }
        }
    }

    private static short[] getNeighborLightChecks(Chunk chunk) {
        return chunk.getNeighborLightChecks();
    }

    private static void setNeighborLightChecks(Chunk chunk, short[] table) {
        chunk.setNeighborLightChecks(table);
    }

    public static int getCachedLightFor(Chunk chunk, EnumSkyBlock type, BlockPos pos) {
        return chunk.getCachedLightFor(type, pos);
    }

}
