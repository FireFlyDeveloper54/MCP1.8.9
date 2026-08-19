package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormatOld implements ISaveFormat
{
    private static final Logger logger = LogManager.getLogger();
    protected final File savesDirectory;

    public SaveFormatOld(File savesDirectoryIn)
    {
        if (!savesDirectoryIn.exists())
        {
            savesDirectoryIn.mkdirs();
        }

        this.savesDirectory = savesDirectoryIn;
    }

    public String getName()
    {
        return "Old Format";
    }

    public List<SaveFormatComparator> getSaveList() throws AnvilConverterException
    {
        List<SaveFormatComparator> saveList = Lists.<SaveFormatComparator>newArrayList();

        for (int saveIndex = 0; saveIndex < 5; ++saveIndex)
        {
            String saveName = "World" + (saveIndex + 1);
            WorldInfo worldInfo = this.getWorldInfo(saveName);

            if (worldInfo != null)
            {
                saveList.add(new SaveFormatComparator(saveName, "", worldInfo.getLastTimePlayed(), worldInfo.getSizeOnDisk(), worldInfo.getGameType(), false, worldInfo.isHardcoreModeEnabled(), worldInfo.areCommandsAllowed()));
            }
        }

        return saveList;
    }

    public void flushCache()
    {
    }

    public WorldInfo getWorldInfo(String saveName)
    {
        File saveDirectory = new File(this.savesDirectory, saveName);

        if (!saveDirectory.exists())
        {
            return null;
        }
        else
        {
            File levelDatFile = new File(saveDirectory, "level.dat");

            if (levelDatFile.exists())
            {
                try
                {
                    NBTTagCompound levelCompound = CompressedStreamTools.readCompressed(new FileInputStream(levelDatFile));
                    NBTTagCompound dataCompound = levelCompound.getCompoundTag("Data");
                    return new WorldInfo(dataCompound);
                }
                catch (Exception exception)
                {
                    logger.error((String)("Exception reading " + levelDatFile), (Throwable)exception);
                }
            }

            levelDatFile = new File(saveDirectory, "level.dat_old");

            if (levelDatFile.exists())
            {
                try
                {
                    NBTTagCompound levelCompound = CompressedStreamTools.readCompressed(new FileInputStream(levelDatFile));
                    NBTTagCompound dataCompound = levelCompound.getCompoundTag("Data");
                    return new WorldInfo(dataCompound);
                }
                catch (Exception exception)
                {
                    logger.error((String)("Exception reading " + levelDatFile), (Throwable)exception);
                }
            }

            return null;
        }
    }

    public void renameWorld(String dirName, String newName)
    {
        File saveDirectory = new File(this.savesDirectory, dirName);

        if (saveDirectory.exists())
        {
            File levelDatFile = new File(saveDirectory, "level.dat");

            if (levelDatFile.exists())
            {
                try
                {
                    NBTTagCompound levelCompound = CompressedStreamTools.readCompressed(new FileInputStream(levelDatFile));
                    NBTTagCompound dataCompound = levelCompound.getCompoundTag("Data");
                    dataCompound.setString("LevelName", newName);
                    CompressedStreamTools.writeCompressed(levelCompound, new FileOutputStream(levelDatFile));
                }
                catch (Exception exception)
                {
                    net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
                }
            }
        }
    }

    public boolean isNewLevelIdAcceptable(String saveName)
    {
        File saveDirectory = new File(this.savesDirectory, saveName);

        if (saveDirectory.exists())
        {
            return false;
        }
        else
        {
            try
            {
                saveDirectory.mkdir();
                saveDirectory.delete();
                return true;
            }
            catch (Throwable throwable)
            {
                logger.warn("Couldn\'t make new level", throwable);
                return false;
            }
        }
    }

    public boolean deleteWorldDirectory(String saveName)
    {
        File saveDirectory = new File(this.savesDirectory, saveName);

        if (!saveDirectory.exists())
        {
            return true;
        }
        else
        {
            logger.info("Deleting level " + saveName);

            for (int attempt = 1; attempt <= 5; ++attempt)
            {
                logger.info("Attempt " + attempt + "...");

                if (deleteFiles(saveDirectory.listFiles()))
                {
                    break;
                }

                logger.warn("Unsuccessful in deleting contents.");

                if (attempt < 5)
                {
                    try
                    {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException caughtInterruptedException)
                    {
                        ;
                    }
                }
            }

            return saveDirectory.delete();
        }
    }

    protected static boolean deleteFiles(File[] files)
    {
        for (int fileIndex = 0; fileIndex < files.length; ++fileIndex)
        {
            File file = files[fileIndex];
            logger.debug("Deleting " + file);

            if (file.isDirectory() && !deleteFiles(file.listFiles()))
            {
                logger.warn("Couldn\'t delete directory " + file);
                return false;
            }

            if (!file.delete())
            {
                logger.warn("Couldn\'t delete file " + file);
                return false;
            }
        }

        return true;
    }

    public ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata)
    {
        return new SaveHandler(this.savesDirectory, saveName, storePlayerdata);
    }

    public boolean isConvertible(String saveName)
    {
        return false;
    }

    public boolean isOldMapFormat(String saveName)
    {
        return false;
    }

    public boolean convertMapFormat(String filename, IProgressUpdate progressCallback)
    {
        return false;
    }

    public boolean canLoadWorld(String saveName)
    {
        File saveDirectory = new File(this.savesDirectory, saveName);
        return saveDirectory.isDirectory();
    }
}
