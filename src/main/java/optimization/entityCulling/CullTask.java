package optimization.entityCulling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import optimization.occlusionCulling.OcclusionCullingInstance;
import optimization.occlusionCulling.util.Vec3d;

public class CullTask implements Runnable
{
    private static final double MAX_CAMERA_DELTA_SQ = 4.0D;
    private volatile OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getMinecraft();
    private final Set<String> unCullable;
    private volatile Snapshot snapshot = Snapshot.EMPTY;
    public volatile boolean requestCull;
    public volatile long lastTime;
    private final Vec3d camera = new Vec3d(0.0D, 0.0D, 0.0D);
    private final Vec3d aabbMin = new Vec3d(0.0D, 0.0D, 0.0D);
    private final Vec3d aabbMax = new Vec3d(0.0D, 0.0D, 0.0D);

    public CullTask(OcclusionCullingInstance cullingIn, Set<String> unCullableIn)
    {
        this.culling = cullingIn;
        this.unCullable = unCullableIn;
    }

    public void setCulling(OcclusionCullingInstance cullingIn)
    {
        this.culling = cullingIn;
    }

    public void captureSnapshot()
    {
        WorldClient world = this.client.theWorld;
        Entity viewEntity = this.client.getRenderViewEntity();

        if (world == null || this.client.thePlayer == null || this.client.thePlayer.ticksExisted <= 10 || viewEntity == null)
        {
            this.clearSnapshot();
            return;
        }

        float cameraPartialTicks = EntityCulling.instance.isInterpolateCamera() ? this.client.getRenderPartialTicks() : 1.0F;
        Vec3 viewPosition = viewEntity.getPositionEyes(cameraPartialTicks);
        List<EntityData> entities = new ArrayList<EntityData>(world.getLoadedEntityList().size());

        for (Entity entity : world.getLoadedEntityList())
        {
            if (entity == null)
            {
                continue;
            }

            AxisAlignedBB bounds = entity.getEntityBoundingBox();

            if (bounds != null)
            {
                boolean markerArmorStand = entity instanceof EntityArmorStand && ((EntityArmorStand)entity).hasMarker();
                entities.add(new EntityData(entity, bounds, entity == viewEntity, markerArmorStand));
            }
        }

        List<TileEntityData> tileEntities = new ArrayList<TileEntityData>(world.loadedTileEntityList.size());

        for (TileEntity tileEntity : world.loadedTileEntityList)
        {
            if (tileEntity == null || tileEntity.isInvalid() || tileEntity.getBlockType() == null)
            {
                continue;
            }

            BlockPos pos = tileEntity.getPos();
            boolean unCullableTile = this.unCullable.contains(tileEntity.getBlockType().getUnlocalizedName());
            AxisAlignedBB bounds = this.getTileEntityBounds(world, pos);
            tileEntities.add(new TileEntityData(tileEntity, pos, bounds, unCullableTile));
        }

        boolean noCulling = this.client.thePlayer.isSpectator() || this.client.gameSettings.thirdPersonView != 0;
        this.snapshot = new Snapshot(world, entities, tileEntities, viewPosition, noCulling);
    }

    public synchronized void clearSnapshot()
    {
        Snapshot previousSnapshot = this.snapshot;
        this.snapshot = Snapshot.EMPTY;
        this.requestCull = false;

        for (int index = 0; index < previousSnapshot.entities.size(); ++index)
        {
            previousSnapshot.entities.get(index).entity.setCulled(false);
        }

        for (int index = 0; index < previousSnapshot.tileEntities.size(); ++index)
        {
            previousSnapshot.tileEntities.get(index).tileEntity.setCulled(false);
        }
    }

    private synchronized void setCullResult(Snapshot source, Entity entity, boolean culled)
    {
        Snapshot currentSnapshot = this.snapshot;
        boolean canCull = this.canCullFromSnapshot(source, currentSnapshot);
        entity.setCulled(culled && canCull);
    }

    private synchronized void setCullResult(Snapshot source, TileEntity tileEntity, boolean culled)
    {
        Snapshot currentSnapshot = this.snapshot;
        boolean canCull = this.canCullFromSnapshot(source, currentSnapshot);
        tileEntity.setCulled(culled && canCull);
    }

    private boolean canCullFromSnapshot(Snapshot source, Snapshot currentSnapshot)
    {
        if (source.world == null || currentSnapshot.world != source.world || currentSnapshot.noCulling || !EntityCulling.instance.isEnabled())
        {
            return false;
        }

        double dx = source.camera.xCoord - currentSnapshot.camera.xCoord;
        double dy = source.camera.yCoord - currentSnapshot.camera.yCoord;
        double dz = source.camera.zCoord - currentSnapshot.camera.zCoord;
        return dx * dx + dy * dy + dz * dz <= MAX_CAMERA_DELTA_SQ;
    }

    private boolean isSnapshotActive(Snapshot source)
    {
        Snapshot currentSnapshot = this.snapshot;

        if (source.world == null || currentSnapshot.world != source.world || !EntityCulling.instance.isEnabled())
        {
            return false;
        }

        double dx = source.camera.xCoord - currentSnapshot.camera.xCoord;
        double dy = source.camera.yCoord - currentSnapshot.camera.yCoord;
        double dz = source.camera.zCoord - currentSnapshot.camera.zCoord;
        return dx * dx + dy * dy + dz * dz <= MAX_CAMERA_DELTA_SQ;
    }

    private AxisAlignedBB getTileEntityBounds(WorldClient world, BlockPos pos)
    {
        if (EntityCulling.instance.useTileEntityBounds())
            try
            {
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                AxisAlignedBB bounds = block.getSelectedBoundingBox(world, pos);

                if (bounds == null)
                {
                    bounds = block.getCollisionBoundingBox(world, pos, state);
                }

                if (bounds != null && bounds.maxX > bounds.minX && bounds.maxY > bounds.minY && bounds.maxZ > bounds.minZ)
                {
                    return bounds;
                }
            }
            catch (Throwable ignoredException)
            {
            }

        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
    }

    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                Snapshot currentSnapshot = this.snapshot;
                int sleepDelay = EntityCulling.instance.isEnabled() && currentSnapshot.world != null ? EntityCulling.instance.getSleepDelay() : 50;
                Thread.sleep(Math.max(1, sleepDelay));

                OcclusionCullingInstance currentCulling = this.culling;
                currentSnapshot = this.snapshot;

                if (currentCulling == null || currentSnapshot.world == null || !EntityCulling.instance.isEnabled())
                {
                    this.requestCull = false;
                    continue;
                }

                if (!this.requestCull)
                {
                    continue;
                }

                this.requestCull = false;
                Provider provider = (Provider)currentCulling.getProvider();
                provider.bindWorld(currentSnapshot.world);
                this.camera.set(currentSnapshot.camera.xCoord, currentSnapshot.camera.yCoord, currentSnapshot.camera.zCoord);
                currentCulling.resetCache();
                long startTime = System.currentTimeMillis();

                for (int index = 0; index < currentSnapshot.tileEntities.size() && this.isSnapshotActive(currentSnapshot); ++index)
                {
                    TileEntityData data = currentSnapshot.tileEntities.get(index);
                    TileEntity tileEntity = data.tileEntity;

                    if (data.unCullable || tileEntity.isForcedVisible())
                    {
                        continue;
                    }

                    if (currentSnapshot.noCulling || data.distanceSq(this.camera) >= 64.0D * 64.0D)
                    {
                        this.setCullResult(currentSnapshot, tileEntity, false);
                        continue;
                    }

                    this.setBounds(data.bounds);
                    boolean visible = currentCulling.isAABBVisible(this.aabbMin, this.aabbMax, this.camera);
                    this.setCullResult(currentSnapshot, tileEntity, !visible);
                }

                int tracingDistance = EntityCulling.instance.getTracingDistance();
                double maxDistanceSq = (double)tracingDistance * (double)tracingDistance;
                int hitboxLimit = EntityCulling.instance.getHitboxLimit();

                for (int index = 0; index < currentSnapshot.entities.size() && this.isSnapshotActive(currentSnapshot); ++index)
                {
                    EntityData data = currentSnapshot.entities.get(index);
                    Entity entity = data.entity;

                    if (entity.isForcedVisible())
                    {
                        continue;
                    }

                    if (currentSnapshot.noCulling || data.viewEntity || (data.markerArmorStand && EntityCulling.instance.isCullArmorStands()))
                    {
                        this.setCullResult(currentSnapshot, entity, false);
                        continue;
                    }

                    if (data.distanceSq(this.camera) > maxDistanceSq || data.isTooLarge(hitboxLimit))
                    {
                        this.setCullResult(currentSnapshot, entity, false);
                        continue;
                    }

                    this.setBounds(data.bounds);
                    boolean visible = currentCulling.isAABBVisible(this.aabbMin, this.aabbMax, this.camera);
                    this.setCullResult(currentSnapshot, entity, !visible);
                }

                this.lastTime = System.currentTimeMillis() - startTime;
            }
            catch (InterruptedException interruptedException)
            {
                Thread.currentThread().interrupt();
                break;
            }
            catch (Throwable ignoredException)
            {
                // A world object may be removed while an asynchronous visibility pass is running.
            }
        }
    }

    private void setBounds(AxisAlignedBB bounds)
    {
        this.aabbMin.set(bounds.minX, bounds.minY, bounds.minZ);
        this.aabbMax.set(bounds.maxX, bounds.maxY, bounds.maxZ);
    }

    private static final class Snapshot
    {
        private static final Snapshot EMPTY = new Snapshot(null, Collections.<EntityData>emptyList(), Collections.<TileEntityData>emptyList(), new Vec3(0.0D, 0.0D, 0.0D), true);
        private final WorldClient world;
        private final List<EntityData> entities;
        private final List<TileEntityData> tileEntities;
        private final Vec3 camera;
        private final boolean noCulling;

        private Snapshot(WorldClient worldIn, List<EntityData> entitiesIn, List<TileEntityData> tileEntitiesIn, Vec3 cameraIn, boolean noCullingIn)
        {
            this.world = worldIn;
            this.entities = entitiesIn;
            this.tileEntities = tileEntitiesIn;
            this.camera = cameraIn;
            this.noCulling = noCullingIn;
        }
    }

    private static final class EntityData
    {
        private final Entity entity;
        private final AxisAlignedBB bounds;
        private final boolean viewEntity;
        private final boolean markerArmorStand;

        private EntityData(Entity entityIn, AxisAlignedBB boundsIn, boolean viewEntityIn, boolean markerArmorStandIn)
        {
            this.entity = entityIn;
            this.bounds = boundsIn;
            this.viewEntity = viewEntityIn;
            this.markerArmorStand = markerArmorStandIn;
        }

        private double distanceSq(Vec3d camera)
        {
            double dx = (this.bounds.minX + this.bounds.maxX) * 0.5D - camera.x;
            double dy = (this.bounds.minY + this.bounds.maxY) * 0.5D - camera.y;
            double dz = (this.bounds.minZ + this.bounds.maxZ) * 0.5D - camera.z;
            return dx * dx + dy * dy + dz * dz;
        }

        private boolean isTooLarge(int hitboxLimit)
        {
            return this.bounds.maxX - this.bounds.minX > hitboxLimit || this.bounds.maxY - this.bounds.minY > hitboxLimit || this.bounds.maxZ - this.bounds.minZ > hitboxLimit;
        }
    }

    private static final class TileEntityData
    {
        private final TileEntity tileEntity;
        private final BlockPos pos;
        private final AxisAlignedBB bounds;
        private final boolean unCullable;

        private TileEntityData(TileEntity tileEntityIn, BlockPos posIn, AxisAlignedBB boundsIn, boolean unCullableIn)
        {
            this.tileEntity = tileEntityIn;
            this.pos = posIn;
            this.bounds = boundsIn;
            this.unCullable = unCullableIn;
        }

        private double distanceSq(Vec3d camera)
        {
            double dx = this.pos.getX() + 0.5D - camera.x;
            double dy = this.pos.getY() + 0.5D - camera.y;
            double dz = this.pos.getZ() + 0.5D - camera.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
