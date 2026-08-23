package net.minecraft.world.lighting;

import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.concurrent.locks.ReentrantLock;

import static net.minecraft.util.BlockPos.MutableBlockPos;

public class LightingEngine {
    private static final int MAX_SCHEDULED_COUNT = 1 << 22;

    private static final int MAX_LIGHT = 15;
    private static final EnumSkyBlock[] SKY_BLOCK_VALUES = EnumSkyBlock.values();

    private final Thread ownedThread = Thread.currentThread();

    private final World world;

    private final Profiler profiler;


    private final PooledLongQueue[] queuedLightUpdates = new PooledLongQueue[SKY_BLOCK_VALUES.length];


    private final PooledLongQueue[] queuedDarkenings = new PooledLongQueue[MAX_LIGHT + 1];
    private final PooledLongQueue[] queuedBrightenings = new PooledLongQueue[MAX_LIGHT + 1];


    private final PooledLongQueue initialBrightenings;

    private final PooledLongQueue initialDarkenings;

    private boolean updating = false;


    private static final int
            lX = 26,
            lY = 8,
            lZ = 26,
            lL = 4;


    private static final int
            sZ = 0,
            sX = sZ + lZ,
            sY = sX + lX,
            sL = sY + lY;


    private static final long
            mX = (1L << lX) - 1,
            mY = (1L << lY) - 1,
            mZ = (1L << lZ) - 1,
            mL = (1L << lL) - 1,
            mPos = (mY << sY) | (mX << sX) | (mZ << sZ);


    private static final long yCheck = 1L << (sY + lY);

    private static final long[] neighborShifts = new long[6];

    static {
        for (int i = 0; i < 6; ++i) {
            final Vec3i offset = EnumFacing.VALUES[i].getDirectionVec();
            neighborShifts[i] = ((long) offset.getY() << sY) | ((long) offset.getX() << sX) | ((long) offset.getZ() << sZ);
        }
    }


    private static final long mChunk = ((mX >> 4) << (4 + sX)) | ((mZ >> 4) << (4 + sZ));


    private final MutableBlockPos curPos = new MutableBlockPos();
    private Chunk curChunk;
    private long curChunkIdentifier;
    private long curData;

    private boolean isNeighborDataValid = false;

    private final NeighborInfo[] neighborInfos = new NeighborInfo[6];
    private PooledLongQueue.LongQueueIterator queueIt;

    private final ReentrantLock lock = new ReentrantLock();

    public LightingEngine(final World world) {
        this.world = world;
        this.profiler = world.theProfiler;

        PooledLongQueue.Pool pool = new PooledLongQueue.Pool();

        this.initialBrightenings = new PooledLongQueue(pool);
        this.initialDarkenings = new PooledLongQueue(pool);

        for (int i = 0; i < SKY_BLOCK_VALUES.length; ++i) {
            this.queuedLightUpdates[i] = new PooledLongQueue(pool);
        }

        for (int i = 0; i < this.queuedDarkenings.length; ++i) {
            this.queuedDarkenings[i] = new PooledLongQueue(pool);
        }

        for (int i = 0; i < this.queuedBrightenings.length; ++i) {
            this.queuedBrightenings[i] = new PooledLongQueue(pool);
        }

        for (int i = 0; i < this.neighborInfos.length; ++i) {
            this.neighborInfos[i] = new NeighborInfo();
        }
    }

    public void scheduleLightUpdate(final EnumSkyBlock lightType, final BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= 256) {
            return;
        }

        this.acquireLock();

        try {
            this.scheduleLightUpdate(lightType, encodeWorldCoord(pos));
        } finally {
            this.releaseLock();
        }
    }

    private void scheduleLightUpdate(final EnumSkyBlock lightType, final long pos) {
        final PooledLongQueue queue = this.queuedLightUpdates[lightType.ordinal()];

        queue.add(pos);


        if (queue.size() >= MAX_SCHEDULED_COUNT) {
            this.processLightUpdatesForType(lightType);
        }
    }

    public void processLightUpdates() {
        this.processLightUpdatesForType(EnumSkyBlock.SKY);
        this.processLightUpdatesForType(EnumSkyBlock.BLOCK);
    }

    public void processLightUpdatesForType(final EnumSkyBlock lightType) {


        if (this.world.isRemote && !this.isCallingFromMainThread()) {
            return;
        }

        final PooledLongQueue queue = this.queuedLightUpdates[lightType.ordinal()];


        if (queue.isEmpty()) {
            return;
        }

        this.acquireLock();

        boolean processingStarted = false;

        try {
            if (this.updating) {
                return;
            }

            processingStarted = true;
            this.processLightUpdatesForTypeInner(lightType, queue);
        } finally {
            if (processingStarted) {
                this.updating = false;
            }

            this.releaseLock();
        }
    }

    private boolean isCallingFromMainThread() {
        return Thread.currentThread() == this.ownedThread;
    }

    private void acquireLock() {
        if (!this.lock.tryLock()) {
            this.lock.lock();
        }
    }

    private void releaseLock() {
        this.lock.unlock();
    }

    private void processLightUpdatesForTypeInner(final EnumSkyBlock lightType, final PooledLongQueue queue) {

        if (this.updating) {
            throw new IllegalStateException("Already processing updates!");
        }

        this.updating = true;

        this.curChunkIdentifier = -1;

        this.profiler.startSection("lighting");

        this.profiler.startSection("checking");

        this.queueIt = queue.iterator();


        while (this.nextItem()) {
            if (this.curChunk == null) {
                continue;
            }

            final int oldLight = this.getCursorCachedLight(lightType);
            final int newLight = this.calculateNewLightFromCursor(lightType);

            if (oldLight < newLight) {

                this.initialBrightenings.add(((long) newLight << sL) | this.curData);
            } else if (oldLight > newLight) {

                this.initialDarkenings.add(this.curData);
            }
        }

        this.queueIt = this.initialBrightenings.iterator();

        while (this.nextItem()) {
            final int newLight = (int) (this.curData >> sL & mL);

            if (newLight > this.getCursorCachedLight(lightType)) {

                this.enqueueBrightening(this.curPos, this.curData & mPos, newLight, this.curChunk, lightType);
            }
        }

        this.queueIt = this.initialDarkenings.iterator();

        while (this.nextItem()) {
            final int oldLight = this.getCursorCachedLight(lightType);

            if (oldLight != 0) {

                this.enqueueDarkening(this.curPos, this.curData, oldLight, this.curChunk, lightType);
            }
        }

        this.profiler.endSection();


        for (int curLight = MAX_LIGHT; curLight >= 0; --curLight) {
            this.profiler.startSection("darkening");

            this.queueIt = this.queuedDarkenings[curLight].iterator();

            while (this.nextItem()) {
                if (this.getCursorCachedLight(lightType) >= curLight)
                {
                    continue;
                }

                final IBlockState state = LightingEngineHelpers.posToState(this.curPos, this.curChunk);
                final int luminosity = this.getCursorLuminosity(state, lightType);
                final int opacity;

                if (luminosity >= MAX_LIGHT - 1) {
                    opacity = 1;
                } else {
                    opacity = this.getPosOpacity(this.curPos, state);
                }


                if (this.calculateNewLightFromCursor(luminosity, opacity, lightType) < curLight) {

                    int newLight = luminosity;

                    this.fetchNeighborDataFromCursor(lightType);

                    for (NeighborInfo info : this.neighborInfos) {
                        final Chunk nChunk = info.chunk;

                        if (nChunk == null) {
                            continue;
                        }

                        final int nLight = info.light;

                        if (nLight == 0) {
                            continue;
                        }

                        final MutableBlockPos nPos = info.pos;

                        if (curLight - this.getPosOpacity(nPos, LightingEngineHelpers.posToState(nPos, info.section)) >= nLight)
                        {
                            this.enqueueDarkening(nPos, info.key, nLight, nChunk, lightType);
                        }
                        else
                        {

                            newLight = Math.max(newLight, nLight - opacity);
                        }
                    }


                    this.enqueueBrighteningFromCursor(newLight, lightType);
                }
                else
                {
                    this.enqueueBrighteningFromCursor(curLight, lightType);
                }
            }

            this.profiler.endStartSection("brightening");

            this.queueIt = this.queuedBrightenings[curLight].iterator();

            while (this.nextItem()) {
                final int oldLight = this.getCursorCachedLight(lightType);

                if (oldLight == curLight)
                {
                    this.world.notifyLightSet(this.curPos);

                    if (curLight > 1) {
                        this.spreadLightFromCursor(curLight, lightType);
                    }
                }
            }

            this.profiler.endSection();
        }

        this.profiler.endSection();

    }


    private void fetchNeighborDataFromCursor(final EnumSkyBlock lightType) {

        if (this.isNeighborDataValid) {
            return;
        }

        this.isNeighborDataValid = true;

        for (int i = 0; i < this.neighborInfos.length; ++i) {
            NeighborInfo info = this.neighborInfos[i];

            final long nLongPos = info.key = this.curData + neighborShifts[i];

            if ((nLongPos & yCheck) != 0) {
                info.chunk = null;
                info.section = null;
                continue;
            }

            final MutableBlockPos nPos = decodeWorldCoord(info.pos, nLongPos);

            final Chunk nChunk;

            if ((nLongPos & mChunk) == this.curChunkIdentifier) {
                nChunk = info.chunk = this.curChunk;
            } else {
                nChunk = info.chunk = this.getChunk(nPos);
            }

            if (nChunk != null) {
                ExtendedBlockStorage nSection = nChunk.getBlockStorageArray()[nPos.getY() >> 4];

                info.light = getCachedLightFor(nChunk, nSection, nPos, lightType);
                info.section = nSection;
            }
        }
    }


    private static int getCachedLightFor(Chunk chunk, ExtendedBlockStorage storage, BlockPos pos, EnumSkyBlock type) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;

        if (storage == null) {
            if (type == EnumSkyBlock.SKY && !chunk.getWorld().provider.getHasNoSky() && chunk.canSeeSky(pos)) {
                return type.defaultLightValue;
            }
            else {
                return 0;
            }
        }
        else if (type == EnumSkyBlock.SKY) {
            if (chunk.getWorld().provider.getHasNoSky()) {
                return 0;
            }
            else {
                return storage.getExtSkylightValue(i, j & 15, k);
            }
        }
        else {
            if (type == EnumSkyBlock.BLOCK) {
                return storage.getExtBlocklightValue(i, j & 15, k);
            }
            else {
                return type.defaultLightValue;
            }
        }
    }


    private int calculateNewLightFromCursor(final EnumSkyBlock lightType) {
        final IBlockState state = LightingEngineHelpers.posToState(this.curPos, this.curChunk);

        final int luminosity = this.getCursorLuminosity(state, lightType);
        final int opacity;

        if (luminosity >= MAX_LIGHT - 1) {
            opacity = 1;
        } else {
            opacity = this.getPosOpacity(this.curPos, state);
        }

        return this.calculateNewLightFromCursor(luminosity, opacity, lightType);
    }

    private int calculateNewLightFromCursor(final int luminosity, final int opacity, final EnumSkyBlock lightType) {
        if (luminosity >= MAX_LIGHT - opacity) {
            return luminosity;
        }

        int newLight = luminosity;

        this.fetchNeighborDataFromCursor(lightType);

        for (NeighborInfo info : this.neighborInfos) {
            if (info.chunk == null) {
                continue;
            }

            final int nLight = info.light;

            newLight = Math.max(nLight - opacity, newLight);
        }

        return newLight;
    }

    private void spreadLightFromCursor(final int curLight, final EnumSkyBlock lightType) {
        this.fetchNeighborDataFromCursor(lightType);

        for (NeighborInfo info : this.neighborInfos) {
            final Chunk nChunk = info.chunk;

            if (nChunk == null) {
                continue;
            }

            final int newLight = curLight - this.getPosOpacity(info.pos, LightingEngineHelpers.posToState(info.pos, info.section));

            if (newLight > info.light) {
                this.enqueueBrightening(info.pos, info.key, newLight, nChunk, lightType);
            }
        }
    }

    private void enqueueBrighteningFromCursor(final int newLight, final EnumSkyBlock lightType) {
        this.enqueueBrightening(this.curPos, this.curData, newLight, this.curChunk, lightType);
    }


    private void enqueueBrightening(final BlockPos pos, final long longPos, final int newLight, final Chunk chunk, final EnumSkyBlock lightType) {
        this.queuedBrightenings[newLight].add(longPos);

        chunk.setLightFor(lightType, pos, newLight);
    }


    private void enqueueDarkening(final BlockPos pos, final long longPos, final int oldLight, final Chunk chunk, final EnumSkyBlock lightType) {
        this.queuedDarkenings[oldLight].add(longPos);

        chunk.setLightFor(lightType, pos, 0);
    }

    private static MutableBlockPos decodeWorldCoord(final MutableBlockPos pos, final long longPos) {
        final int posX = (int) (longPos >> sX & mX) - (1 << lX - 1);
        final int posY = (int) (longPos >> sY & mY);
        final int posZ = (int) (longPos >> sZ & mZ) - (1 << lZ - 1);

        return pos.set(posX, posY, posZ);
    }

    private static long encodeWorldCoord(final BlockPos pos) {
        return encodeWorldCoord(pos.getX(), pos.getY(), pos.getZ());
    }

    private static long encodeWorldCoord(final long x, final long y, final long z) {
        return (y << sY) | (x + (1 << lX - 1) << sX) | (z + (1 << lZ - 1) << sZ);
    }


    private boolean nextItem() {
        if (!this.queueIt.hasNext()) {
            this.queueIt.finish();
            this.queueIt = null;

            return false;
        }

        this.curData = this.queueIt.next();
        this.isNeighborDataValid = false;

        decodeWorldCoord(this.curPos, this.curData);

        final long chunkIdentifier = this.curData & mChunk;

        if (this.curChunkIdentifier != chunkIdentifier) {
            this.curChunk = this.getChunk(this.curPos);
            this.curChunkIdentifier = chunkIdentifier;
        }

        return true;
    }

    private int getCursorCachedLight(final EnumSkyBlock lightType) {
        return this.curChunk.getCachedLightFor(lightType, this.curPos);
    }


    private int getCursorLuminosity(final IBlockState state, final EnumSkyBlock lightType) {
        if (lightType == EnumSkyBlock.SKY) {
            if (this.curChunk.canSeeSky(this.curPos)) {
                return EnumSkyBlock.SKY.defaultLightValue;
            } else {
                return 0;
            }
        }
        return MathHelper.clamp_int(LightingEngineHelpers.getLightValueForState(state, this.world, this.curPos), 0, MAX_LIGHT);
    }

    private int getPosOpacity(final BlockPos pos, final IBlockState state) {
        return MathHelper.clamp_int(state.getBlock().getLightOpacity(), 1, MAX_LIGHT);
    }

    private Chunk getChunk(final BlockPos pos) {
        return LightingEngineHelpers.getLoadedChunk(this.world.getChunkProvider(), pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static class NeighborInfo {
        Chunk chunk;
        ExtendedBlockStorage section;

        int light;

        long key;

        final MutableBlockPos pos = new MutableBlockPos();
    }
}
