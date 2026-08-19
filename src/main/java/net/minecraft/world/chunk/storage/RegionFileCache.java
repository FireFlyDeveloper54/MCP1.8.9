package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RegionFileCache
{
    private static final Map<File, RegionFile> regionsByFilename = Maps.<File, RegionFile>newHashMap();

    public static synchronized RegionFile createOrLoadRegionFile(File worldDir, int chunkX, int chunkZ)
    {
        File file1 = new File(worldDir, "region");
        File file2 = new File(file1, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
        RegionFile regionFile = (RegionFile)regionsByFilename.get(file2);

        if (regionFile != null)
        {
            return regionFile;
        }
        else
        {
            if (!file1.exists())
            {
                file1.mkdirs();
            }

            if (regionsByFilename.size() >= 256)
            {
                clearRegionFileReferences();
            }

            RegionFile regionfile1 = new RegionFile(file2);
            regionsByFilename.put(file2, regionfile1);
            return regionfile1;
        }
    }

    public static synchronized void clearRegionFileReferences()
    {
        for (RegionFile regionFile : regionsByFilename.values())
        {
            try
            {
                if (regionFile != null)
                {
                    regionFile.close();
                }
            }
            catch (IOException iOException)
            {
                net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
            }
        }

        regionsByFilename.clear();
    }

    public static DataInputStream getChunkInputStream(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionFile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        return regionFile.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
    }

    public static DataOutputStream getChunkOutputStream(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionFile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        return regionFile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
    }
}
