package optimization.entityCulling;

import optimization.occlusionCulling.OcclusionCullingInstance;
import optimization.occlusionCulling.util.Vec3d;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CullTask implements Runnable {
    private volatile OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getMinecraft();
    private final Set<String> unCullable;
    public boolean requestCull = false;
    public long lastTime = 0;

    private final Vec3d lastPos = new Vec3d(0, 0, 0);
    private final Vec3d aabbMin = new Vec3d(0, 0, 0);
    private final Vec3d aabbMax = new Vec3d(0, 0, 0);
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    public CullTask(OcclusionCullingInstance culling, Set<String> unCullable) {
        this.culling = culling;
        this.unCullable = unCullable;
    }

    public void setCulling(OcclusionCullingInstance culling) {
        this.culling = culling;
    }

    public void captureSnapshot() {
        WorldClient world = client.theWorld;
        Entity viewEntity = client.getRenderViewEntity();

        if (world == null || client.thePlayer == null || client.thePlayer.ticksExisted <= 10 || viewEntity == null) {
            this.snapshot = Snapshot.EMPTY;
            return;
        }

        ArrayList<Entity> entities = new ArrayList<Entity>(world.getLoadedEntityList());
        ArrayList<TileEntity> tiles = new ArrayList<TileEntity>(world.loadedTileEntityList);
        Vec3 camera = viewEntity.getPositionEyes(EntityCulling.instance.isInterpolateCamera() ? 1.0F : 0.0F);
        boolean noCulling = client.thePlayer.isSpectator() || client.gameSettings.thirdPersonView != 0;
        this.snapshot = new Snapshot(world, entities, tiles, camera, noCulling);
    }

    private void setTileEntityBounds(WorldClient world, TileEntity tileEntity, BlockPos pos) {
        if (EntityCulling.instance.useTileEntityBounds() && world != null) {
            try {
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                AxisAlignedBB bounds = block.getSelectedBoundingBox(world, pos);
                if (bounds == null) {
                    bounds = block.getCollisionBoundingBox(world, pos, state);
                }
                if (bounds != null) {
                    aabbMin.set(bounds.minX, bounds.minY, bounds.minZ);
                    aabbMax.set(bounds.maxX, bounds.maxY, bounds.maxZ);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }

        aabbMin.set(pos.getX(), pos.getY(), pos.getZ());
        aabbMax.set(pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
    }

    private boolean isSkippableArmorstand(Entity entity) {
        if (!EntityCulling.instance.isCullArmorStands()) return false;
        return entity instanceof EntityArmorStand && ((EntityArmorStand) entity).hasMarker();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int sleepDelay = EntityCulling.instance.getSleepDelay();
                if (sleepDelay < 1) {
                    sleepDelay = 1;
                }
                Thread.sleep(sleepDelay);

                OcclusionCullingInstance currentCulling = this.culling;
                Snapshot currentSnapshot = this.snapshot;
                if (currentCulling == null || currentSnapshot.world == null) {
                    continue;
                }

                if (EntityCulling.instance.isEnabled()) {
                    Vec3 cameraMC = currentSnapshot.camera;
                    if (requestCull || !(cameraMC.xCoord == lastPos.x && cameraMC.yCoord == lastPos.y && cameraMC.zCoord == lastPos.z)) {
                        long start = System.currentTimeMillis();
                        requestCull = false;
                        lastPos.set(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord);
                        Vec3d camera = lastPos;
                        currentCulling.resetCache();
                        boolean noCulling = currentSnapshot.noCulling;
                        for (int tileIndex = 0; tileIndex < currentSnapshot.tiles.size(); ++tileIndex) {
                            TileEntity entry = currentSnapshot.tiles.get(tileIndex);
                            if (entry == null || entry.getBlockType() == null) {
                                continue;
                            }

                            if (unCullable.contains(entry.getBlockType().getUnlocalizedName())) {
                                continue;
                            }

                            if (!entry.isForcedVisible()) {
                                if (noCulling) {
                                    entry.setCulled(false);
                                    continue;
                                }
                                BlockPos pos = entry.getPos();
                                if (pos.distanceSq(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord) < 64 * 64) {
                                    setTileEntityBounds(currentSnapshot.world, entry, pos);
                                    boolean visible = currentCulling.isAABBVisible(aabbMin, aabbMax, camera);
                                    entry.setCulled(!visible);
                                } else {
                                    entry.setCulled(false);
                                }
                            } else {
                                entry.setCulled(false);
                            }
                        }
                        for (int entityIndex = 0; entityIndex < currentSnapshot.entities.size(); ++entityIndex) {
                            Entity entity = currentSnapshot.entities.get(entityIndex);
                            if (entity == null) {
                                continue;
                            }
                            if (!entity.isForcedVisible()) {
                                if (noCulling || isSkippableArmorstand(entity)) {
                                    entity.setCulled(false);
                                    continue;
                                }
                                int tracingDistance = EntityCulling.instance.getTracingDistance();
                                double dx = entity.posX - cameraMC.xCoord;
                                double dy = entity.posY - cameraMC.yCoord;
                                double dz = entity.posZ - cameraMC.zCoord;
                                if (dx * dx + dy * dy + dz * dz > tracingDistance * tracingDistance) {
                                    entity.setCulled(false);
                                    continue;
                                }
                                int hitboxLimit = EntityCulling.instance.getHitboxLimit();
                                AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
                                if (boundingBox.maxX - boundingBox.minX > hitboxLimit || boundingBox.maxY - boundingBox.minY > hitboxLimit || boundingBox.maxZ - boundingBox.minZ > hitboxLimit) {
                                    entity.setCulled(false);
                                    continue;
                                }
                                aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                                aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                                boolean visible = currentCulling.isAABBVisible(aabbMin, aabbMax, camera);
                                entity.setCulled(!visible);
                            } else {
                                entity.setCulled(false);
                            }
                        }
                        lastTime = (System.currentTimeMillis() - start);
                    }
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable ignored) {
            }
        }
    }

    private static final class Snapshot {
        static final Snapshot EMPTY = new Snapshot(null, Collections.<Entity>emptyList(), Collections.<TileEntity>emptyList(), new Vec3(0.0D, 0.0D, 0.0D), true);

        final WorldClient world;
        final List<Entity> entities;
        final List<TileEntity> tiles;
        final Vec3 camera;
        final boolean noCulling;

        Snapshot(WorldClient world, List<Entity> entities, List<TileEntity> tiles, Vec3 camera, boolean noCulling) {
            this.world = world;
            this.entities = entities;
            this.tiles = tiles;
            this.camera = camera;
            this.noCulling = noCulling;
        }
    }
}
