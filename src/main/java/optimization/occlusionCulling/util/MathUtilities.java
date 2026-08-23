package optimization.occlusionCulling.util;

import net.minecraft.util.MathHelper;

/**
 * Contains MathHelper methods
 */
public final class MathUtilities {

    private MathUtilities() {
    }

    public static int floor(double d) {
        return MathHelper.floor_double(d);
    }

    public static int fastFloor(double d) {
        return MathHelper.floor_double(d);
    }

    public static int ceil(double d) {
        return MathHelper.ceiling_double_int(d);
    }

}
