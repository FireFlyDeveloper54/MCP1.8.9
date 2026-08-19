package net.optifine.util;

import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorClass;
import net.optifine.reflect.ReflectorField;

public class ChunkUtils
{
    private static ReflectorClass chunkClass = new ReflectorClass(Chunk.class);
    private static ReflectorField fieldHasEntities = findFieldHasEntities();
    private static ReflectorField fieldPrecipitationHeightMap = new ReflectorField(chunkClass, int[].class, 0);

    public static boolean hasEntities(Chunk chunk)
    {
        return Reflector.getFieldValueBoolean(chunk, fieldHasEntities, true);
    }

    public static int getPrecipitationHeight(Chunk chunk, BlockPos pos)
    {
        int[] aint = (int[])((int[])Reflector.getFieldValue(chunk, fieldPrecipitationHeightMap));

        if (aint != null && aint.length == 256)
        {
            int i = pos.getX() & 15;
            int j = pos.getZ() & 15;
            int k = i | j << 4;
            int l = aint[k];

            if (l >= 0)
            {
                return l;
            }
            else
            {
                BlockPos blockpos = chunk.getPrecipitationHeight(pos);
                return blockpos.getY();
            }
        }
        else
        {
            return -1;
        }
    }

    private static ReflectorField findFieldHasEntities()
    {
        ReflectorField field = new ReflectorField(chunkClass, "hasEntities");

        if (!field.exists())
        {
            Config.warn("Error finding Chunk.hasEntities");
        }

        return field;
    }
}
