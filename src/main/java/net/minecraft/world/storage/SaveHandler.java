package net.minecraft.world.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveHandler implements ISaveHandler, IPlayerFileData
{
    private static final Logger logger = LogManager.getLogger();
    private final File worldDirectory;
    private final File playersDirectory;
    private final File mapDataDir;
    private final long initializationTime = MinecraftServer.getCurrentTimeMillis();
    private final String saveDirectoryName;

    public SaveHandler(File savesDirectory, String directoryName, boolean playersDirectoryIn)
    {
        this.worldDirectory = new File(savesDirectory, directoryName);
        this.worldDirectory.mkdirs();
        this.playersDirectory = new File(this.worldDirectory, "playerdata");
        this.mapDataDir = new File(this.worldDirectory, "data");
        this.mapDataDir.mkdirs();
        this.saveDirectoryName = directoryName;

        if (playersDirectoryIn)
        {
            this.playersDirectory.mkdirs();
        }

        this.setSessionLock();
    }

    private void setSessionLock()
    {
        try
        {
            File sessionLockFile = new File(this.worldDirectory, "session.lock");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(sessionLockFile));

            try
            {
                dataOutputStream.writeLong(this.initializationTime);
            }
            finally
            {
                dataOutputStream.close();
            }
        }
        catch (IOException iOException)
        {
            net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
            throw new RuntimeException("Failed to check session lock, aborting");
        }
    }

    public File getWorldDirectory()
    {
        return this.worldDirectory;
    }

    public void checkSessionLock() throws MinecraftException
    {
        try
        {
            File sessionLockFile = new File(this.worldDirectory, "session.lock");
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(sessionLockFile));

            try
            {
                if (dataInputStream.readLong() != this.initializationTime)
                {
                    throw new MinecraftException("The save is being accessed from another location, aborting");
                }
            }
            finally
            {
                dataInputStream.close();
            }
        }
        catch (IOException caughtIoException)
        {
            throw new MinecraftException("Failed to check session lock, aborting");
        }
    }

    public IChunkLoader getChunkLoader(WorldProvider provider)
    {
        throw new RuntimeException("Old Chunk Storage is no longer supported.");
    }

    public WorldInfo loadWorldInfo()
    {
        File levelDatFile = new File(this.worldDirectory, "level.dat");

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
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }

        levelDatFile = new File(this.worldDirectory, "level.dat_old");

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
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }

        return null;
    }

    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound)
    {
        NBTTagCompound worldCompound = worldInformation.cloneNBTCompound(tagCompound);
        NBTTagCompound levelCompound = new NBTTagCompound();
        levelCompound.setTag("Data", worldCompound);

        try
        {
            File newLevelDatFile = new File(this.worldDirectory, "level.dat_new");
            File oldLevelDatFile = new File(this.worldDirectory, "level.dat_old");
            File levelDatFile = new File(this.worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(levelCompound, new FileOutputStream(newLevelDatFile));

            if (oldLevelDatFile.exists())
            {
                oldLevelDatFile.delete();
            }

            levelDatFile.renameTo(oldLevelDatFile);

            if (levelDatFile.exists())
            {
                levelDatFile.delete();
            }

            newLevelDatFile.renameTo(levelDatFile);

            if (newLevelDatFile.exists())
            {
                newLevelDatFile.delete();
            }
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    public void saveWorldInfo(WorldInfo worldInformation)
    {
        NBTTagCompound worldCompound = worldInformation.getNBTTagCompound();
        NBTTagCompound levelCompound = new NBTTagCompound();
        levelCompound.setTag("Data", worldCompound);

        try
        {
            File newLevelDatFile = new File(this.worldDirectory, "level.dat_new");
            File oldLevelDatFile = new File(this.worldDirectory, "level.dat_old");
            File levelDatFile = new File(this.worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(levelCompound, new FileOutputStream(newLevelDatFile));

            if (oldLevelDatFile.exists())
            {
                oldLevelDatFile.delete();
            }

            levelDatFile.renameTo(oldLevelDatFile);

            if (levelDatFile.exists())
            {
                levelDatFile.delete();
            }

            newLevelDatFile.renameTo(levelDatFile);

            if (newLevelDatFile.exists())
            {
                newLevelDatFile.delete();
            }
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    public void writePlayerData(EntityPlayer player)
    {
        try
        {
            NBTTagCompound playerCompound = new NBTTagCompound();
            player.writeToNBT(playerCompound);
            File temporaryPlayerFile = new File(this.playersDirectory, player.getUniqueID().toString() + ".dat.tmp");
            File playerFile = new File(this.playersDirectory, player.getUniqueID().toString() + ".dat");
            CompressedStreamTools.writeCompressed(playerCompound, new FileOutputStream(temporaryPlayerFile));

            if (playerFile.exists())
            {
                playerFile.delete();
            }

            temporaryPlayerFile.renameTo(playerFile);
        }
        catch (Exception caughtException)
        {
            logger.warn("Failed to save player data for " + player.getName());
        }
    }

    public NBTTagCompound readPlayerData(EntityPlayer player)
    {
        NBTTagCompound playerCompound = null;

        try
        {
            File playerFile = new File(this.playersDirectory, player.getUniqueID().toString() + ".dat");

            if (playerFile.exists() && playerFile.isFile())
            {
                playerCompound = CompressedStreamTools.readCompressed(new FileInputStream(playerFile));
            }
        }
        catch (Exception caughtException)
        {
            logger.warn("Failed to load player data for " + player.getName());
        }

        if (playerCompound != null)
        {
            player.readFromNBT(playerCompound);
        }

        return playerCompound;
    }

    public IPlayerFileData getPlayerNBTManager()
    {
        return this;
    }

    public String[] getAvailablePlayerDat()
    {
        String[] playerFileNames = this.playersDirectory.list();

        if (playerFileNames == null)
        {
            playerFileNames = new String[0];
        }

        for (int index = 0; index < playerFileNames.length; ++index)
        {
            if (playerFileNames[index].endsWith(".dat"))
            {
                playerFileNames[index] = playerFileNames[index].substring(0, playerFileNames[index].length() - 4);
            }
        }

        return playerFileNames;
    }

    public void flush()
    {
    }

    public File getMapFileFromName(String mapName)
    {
        return new File(this.mapDataDir, mapName + ".dat");
    }

    public String getWorldDirectoryName()
    {
        return this.saveDirectoryName;
    }
}
