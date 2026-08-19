package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilSaveConverter extends SaveFormatOld
{
    private static final Logger logger = LogManager.getLogger();

    public AnvilSaveConverter(File savesDirectoryIn)
    {
        super(savesDirectoryIn);
    }

    public String getName()
    {
        return "Anvil";
    }

    public List<SaveFormatComparator> getSaveList() throws AnvilConverterException
    {
        if (this.savesDirectory != null && this.savesDirectory.exists() && this.savesDirectory.isDirectory())
        {
            List<SaveFormatComparator> saveList = Lists.<SaveFormatComparator>newArrayList();
            File[] saveDirectories = this.savesDirectory.listFiles();

            for (File worldDirectory : saveDirectories)
            {
                if (worldDirectory.isDirectory())
                {
                    String saveName = worldDirectory.getName();
                    WorldInfo worldInfo = this.getWorldInfo(saveName);

                    if (worldInfo != null && (worldInfo.getSaveVersion() == 19132 || worldInfo.getSaveVersion() == 19133))
                    {
                        boolean requiresConversion = worldInfo.getSaveVersion() != this.getSaveVersion();
                        String displayName = worldInfo.getWorldName();

                        if (StringUtils.isEmpty(displayName))
                        {
                            displayName = saveName;
                        }

                        long sizeOnDisk = 0L;
                        saveList.add(new SaveFormatComparator(saveName, displayName, worldInfo.getLastTimePlayed(), sizeOnDisk, worldInfo.getGameType(), requiresConversion, worldInfo.isHardcoreModeEnabled(), worldInfo.areCommandsAllowed()));
                    }
                }
            }

            return saveList;
        }
        else
        {
            throw new AnvilConverterException("Unable to read or access folder where game worlds are saved!");
        }
    }

    protected int getSaveVersion()
    {
        return 19133;
    }

    public void flushCache()
    {
        RegionFileCache.clearRegionFileReferences();
    }

    public ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata)
    {
        return new AnvilSaveHandler(this.savesDirectory, saveName, storePlayerdata);
    }

    public boolean isConvertible(String saveName)
    {
        WorldInfo worldInfo = this.getWorldInfo(saveName);
        return worldInfo != null && worldInfo.getSaveVersion() == 19132;
    }

    public boolean isOldMapFormat(String saveName)
    {
        WorldInfo worldInfo = this.getWorldInfo(saveName);
        return worldInfo != null && worldInfo.getSaveVersion() != this.getSaveVersion();
    }

    public boolean convertMapFormat(String filename, IProgressUpdate progressCallback)
    {
        progressCallback.setLoadingProgress(0);
        List<File> overworldRegionFiles = Lists.<File>newArrayList();
        List<File> netherRegionFiles = Lists.<File>newArrayList();
        List<File> endRegionFiles = Lists.<File>newArrayList();
        File worldDirectory = new File(this.savesDirectory, filename);
        File netherDirectory = new File(worldDirectory, "DIM-1");
        File endDirectory = new File(worldDirectory, "DIM1");
        logger.info("Scanning folders...");
        this.addRegionFilesToCollection(worldDirectory, overworldRegionFiles);

        if (netherDirectory.exists())
        {
            this.addRegionFilesToCollection(netherDirectory, netherRegionFiles);
        }

        if (endDirectory.exists())
        {
            this.addRegionFilesToCollection(endDirectory, endRegionFiles);
        }

        int totalConversionCount = overworldRegionFiles.size() + netherRegionFiles.size() + endRegionFiles.size();
        logger.info("Total conversion count is " + totalConversionCount);
        WorldInfo worldInfo = this.getWorldInfo(filename);
        WorldChunkManager worldChunkManager = null;

        if (worldInfo.getTerrainType() == WorldType.FLAT)
        {
            worldChunkManager = new WorldChunkManagerHell(BiomeGenBase.plains, 0.5F);
        }
        else
        {
            worldChunkManager = new WorldChunkManager(worldInfo.getSeed(), worldInfo.getTerrainType(), worldInfo.getGeneratorOptions());
        }

        this.convertFile(new File(worldDirectory, "region"), overworldRegionFiles, worldChunkManager, 0, totalConversionCount, progressCallback);
        this.convertFile(new File(netherDirectory, "region"), netherRegionFiles, new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F), overworldRegionFiles.size(), totalConversionCount, progressCallback);
        this.convertFile(new File(endDirectory, "region"), endRegionFiles, new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F), overworldRegionFiles.size() + netherRegionFiles.size(), totalConversionCount, progressCallback);
        worldInfo.setSaveVersion(19133);

        if (worldInfo.getTerrainType() == WorldType.DEFAULT_1_1)
        {
            worldInfo.setTerrainType(WorldType.DEFAULT);
        }

        this.createFile(filename);
        ISaveHandler saveHandler = this.getSaveLoader(filename, false);
        saveHandler.saveWorldInfo(worldInfo);
        return true;
    }

    private void createFile(String filename)
    {
        File worldDirectory = new File(this.savesDirectory, filename);

        if (!worldDirectory.exists())
        {
            logger.warn("Unable to create level.dat_mcr backup");
        }
        else
        {
            File levelDatFile = new File(worldDirectory, "level.dat");

            if (!levelDatFile.exists())
            {
                logger.warn("Unable to create level.dat_mcr backup");
            }
            else
            {
                File backupFile = new File(worldDirectory, "level.dat_mcr");

                if (!levelDatFile.renameTo(backupFile))
                {
                    logger.warn("Unable to create level.dat_mcr backup");
                }
            }
        }
    }

    private void convertFile(File targetDirectory, Iterable<File> regionFiles, WorldChunkManager worldChunkManager, int convertedCount, int totalCount, IProgressUpdate progressCallback)
    {
        for (File regionFile : regionFiles)
        {
            this.convertChunks(targetDirectory, regionFile, worldChunkManager, convertedCount, totalCount, progressCallback);
            ++convertedCount;
            int progress = (int)Math.round(100.0D * (double)convertedCount / (double)totalCount);
            progressCallback.setLoadingProgress(progress);
        }
    }

    private void convertChunks(File targetDirectory, File regionFileIn, WorldChunkManager worldChunkManager, int convertedCount, int totalCount, IProgressUpdate progressCallback)
    {
        try
        {
            String regionFileName = regionFileIn.getName();
            RegionFile inputRegionFile = new RegionFile(regionFileIn);
            RegionFile outputRegionFile = new RegionFile(new File(targetDirectory, regionFileName.substring(0, regionFileName.length() - ".mcr".length()) + ".mca"));

            for (int chunkX = 0; chunkX < 32; ++chunkX)
            {
                for (int chunkZ = 0; chunkZ < 32; ++chunkZ)
                {
                    if (inputRegionFile.isChunkSaved(chunkX, chunkZ) && !outputRegionFile.isChunkSaved(chunkX, chunkZ))
                    {
                        DataInputStream inputStream = inputRegionFile.getChunkDataInputStream(chunkX, chunkZ);

                        if (inputStream == null)
                        {
                            logger.warn("Failed to fetch input stream");
                        }
                        else
                        {
                            NBTTagCompound inputRootTag = CompressedStreamTools.read(inputStream);
                            inputStream.close();
                            NBTTagCompound inputLevelTag = inputRootTag.getCompoundTag("Level");
                            ChunkLoader.AnvilConverterData converterData = ChunkLoader.load(inputLevelTag);
                            NBTTagCompound outputRootTag = new NBTTagCompound();
                            NBTTagCompound outputLevelTag = new NBTTagCompound();
                            outputRootTag.setTag("Level", outputLevelTag);
                            ChunkLoader.convertToAnvilFormat(converterData, outputLevelTag, worldChunkManager);
                            DataOutputStream outputStream = outputRegionFile.getChunkDataOutputStream(chunkX, chunkZ);
                            CompressedStreamTools.write(outputRootTag, outputStream);
                            outputStream.close();
                        }
                    }
                }

                int previousProgress = (int)Math.round(100.0D * (double)(convertedCount * 1024) / (double)(totalCount * 1024));
                int currentProgress = (int)Math.round(100.0D * (double)((chunkX + 1) * 32 + convertedCount * 1024) / (double)(totalCount * 1024));

                if (currentProgress > previousProgress)
                {
                    progressCallback.setLoadingProgress(currentProgress);
                }
            }

            inputRegionFile.close();
            outputRegionFile.close();
        }
        catch (IOException exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    private void addRegionFilesToCollection(File worldDir, Collection<File> collection)
    {
        File regionDirectory = new File(worldDir, "region");
        File[] regionFiles = regionDirectory.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String fileName)
            {
                return fileName.endsWith(".mcr");
            }
        });

        if (regionFiles != null)
        {
            Collections.addAll(collection, regionFiles);
        }
    }
}
