package optimization.entityCulling;

import optimization.occlusionCulling.OcclusionCullingInstance;
import net.minecraft.client.Minecraft;
import java.util.HashSet;

public class EntityCulling {
    public static EntityCulling instance = new EntityCulling();
    public OcclusionCullingInstance culling;
    public CullTask cullTask;
    private Thread cullThread;
    protected boolean pressed = false;

	public int renderedBlockEntities = 0;
	public int skippedBlockEntities = 0;
	public int renderedEntities = 0;
	public int skippedEntities = 0;


    public static boolean enabled = true;
    public static int tracingDistance = 128;
    public static int sleepDelay = 10;
    public static int hitboxLimit = 50;
    public static boolean cullArmorStands = true;

    public boolean isEnabled() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return enabled;
        return mc.gameSettings.ofEntityCulling;
    }

    public int getTracingDistance() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return tracingDistance;
        return mc.gameSettings.ofCullTracingDistance;
    }

    public int getSleepDelay() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return sleepDelay;
        return mc.gameSettings.ofCullSleepDelay;
    }

    public int getHitboxLimit() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return hitboxLimit;
        return mc.gameSettings.ofCullHitboxLimit;
    }

    public boolean isCullArmorStands() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return cullArmorStands;
        return mc.gameSettings.ofCullArmorStands;
    }

    public boolean isInterpolateCamera() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return true;
        return mc.gameSettings.ofCullInterpolateCamera;
    }

    public boolean useTileEntityBounds() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return true;
        return mc.gameSettings.ofCullTileBounds;
    }

    public boolean isLoadedChunksOnly() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return true;
        return mc.gameSettings.ofCullLoadedChunks;
    }

	public void onInitialize() {
		instance = this;
        this.rebuildCulling();
        cullTask = new CullTask(culling, new HashSet<String>(java.util.Collections.singletonList("tile.beacon")));

		cullThread = new Thread(cullTask, "CullThread");
        cullThread.setDaemon(true);
		cullThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
                System.out.println("The CullingThread has crashed! Please report the following stacktrace!");
                ex.printStackTrace();
            }
        });
		cullThread.start();
	}

    private void rebuildCulling() {
        int tracing = getTracingDistance();
        if (tracing < 1) {
            tracing = 1;
        }
        if (this.culling == null || this.culling.getReach() != tracing) {
            this.culling = new OcclusionCullingInstance(tracing, new Provider());
            if (this.cullTask != null) {
                this.cullTask.setCulling(this.culling);
            }
        }
    }
    
    public void worldTick() {
        this.rebuildCulling();
        if (cullTask != null) {
            cullTask.captureSnapshot();
            cullTask.requestCull = true;
        }
    }

}