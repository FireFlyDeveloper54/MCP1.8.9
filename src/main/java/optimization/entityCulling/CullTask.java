package optimization.entityCulling;

import optimization.occlusionCulling.OcclusionCullingInstance;
import optimization.occlusionCulling.util.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;

public class CullTask implements Runnable {
    private final OcclusionCullingInstance culling;
    private final Minecraft client = Minecraft.getMinecraft();
    private final Set<String> unCullable;
    public boolean requestCull = false;
    public long lastTime = 0;

    // reused preallocated vars
    private final Vec3d lastPos = new Vec3d(0, 0, 0);
    private final Vec3d aabbMin = new Vec3d(0, 0, 0);
    private final Vec3d aabbMax = new Vec3d(0, 0, 0);

    public CullTask(OcclusionCullingInstance culling, Set<String> unCullable) {
        this.culling = culling;
        this.unCullable = unCullable;
    }

    // 1.8 doesnt know where the heck the camera is... what?!?
    private Vec3 getCameraPos() {
        return client.getRenderViewEntity().getPositionEyes(0);
    }

    private boolean isSkippableArmorstand(Entity entity) {
        if (!EntityCulling.instance.isCullArmorStands()) return false;
        return entity instanceof EntityArmorStand && ((EntityArmorStand) entity).hasMarker();
    }

    @Override
    public void run() {
        try {
            int sleepDelay = EntityCulling.instance.getSleepDelay();
            Thread.sleep(sleepDelay);

            if (EntityCulling.instance.isEnabled() && client.theWorld != null && client.thePlayer != null && client.thePlayer.ticksExisted > 10 && client.getRenderViewEntity() != null) {
                Vec3 cameraMC = getCameraPos();
                if (requestCull || !(cameraMC.xCoord == lastPos.x && cameraMC.yCoord == lastPos.y && cameraMC.zCoord == lastPos.z)) {
                    long start = System.currentTimeMillis();
                    requestCull = false;
                    lastPos.set(cameraMC.xCoord, cameraMC.yCoord, cameraMC.zCoord);
                    Vec3d camera = lastPos;
                    culling.resetCache();
                    boolean noCulling = client.thePlayer.isSpectator() || client.gameSettings.thirdPersonView != 0;
                    Iterator<TileEntity> iterator = client.theWorld.loadedTileEntityList.iterator();
                    TileEntity entry;
                    while (iterator.hasNext()) {
                        try {
                            entry = iterator.next();
                        } catch (NullPointerException | ConcurrentModificationException ex) {
                            break;
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
                                aabbMin.set(pos.getX(), pos.getY(), pos.getZ());
                                aabbMax.set(pos.getX() + 1d, pos.getY() + 1d, pos.getZ() + 1d);
                                boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                                entry.setCulled(!visible);
                            }

                        } else {
                            entry.setCulled(false);
                        }
                    }
                    Entity entity;
                    Iterator<Entity> iterable = client.theWorld.getLoadedEntityList().iterator();
                    while (iterable.hasNext()) {
                        try {
                            entity = iterable.next();
                        } catch (NullPointerException | ConcurrentModificationException ex) {
                            break;
                        }
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
                            boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
                            entity.setCulled(!visible);
                        } else {
                            entity.setCulled(false);
                        }
                    }
                    lastTime = (System.currentTimeMillis() - start);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}