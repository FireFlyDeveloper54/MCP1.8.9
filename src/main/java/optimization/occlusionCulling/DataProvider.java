package optimization.occlusionCulling;

import optimization.occlusionCulling.util.Vec3d;

public interface DataProvider {


    boolean prepareChunk(int chunkX, int chunkZ);


    boolean isOpaqueFullCube(int x, int y, int z);

    default void cleanup() {
    }

    default void checkingPosition(Vec3d[] targetPoints, int size, Vec3d viewerPosition) {
    }

}
