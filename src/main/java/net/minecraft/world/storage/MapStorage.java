package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.WorldSavedData;

public class MapStorage
{
    private ISaveHandler saveHandler;
    protected Map<String, WorldSavedData> loadedDataMap = Maps.<String, WorldSavedData>newHashMap();
    private List<WorldSavedData> loadedDataList = Lists.<WorldSavedData>newArrayList();
    private Map<String, Short> idCounts = Maps.<String, Short>newHashMap();

    public MapStorage(ISaveHandler saveHandlerIn)
    {
        this.saveHandler = saveHandlerIn;
        this.loadIdCounts();
    }

    public WorldSavedData loadData(Class <? extends WorldSavedData > clazz, String dataIdentifier)
    {
        WorldSavedData worldSavedData = (WorldSavedData)this.loadedDataMap.get(dataIdentifier);

        if (worldSavedData != null)
        {
            return worldSavedData;
        }
        else
        {
            if (this.saveHandler != null)
            {
                try
                {
                    File file1 = this.saveHandler.getMapFileFromName(dataIdentifier);

                    if (file1 != null && file1.exists())
                    {
                        try
                        {
                            worldSavedData = (WorldSavedData)clazz.getConstructor(new Class[] {String.class}).newInstance(new Object[] {dataIdentifier});
                        }
                        catch (Exception exception)
                        {
                            throw new RuntimeException("Failed to instantiate " + clazz.toString(), exception);
                        }

                        FileInputStream fileinputstream = new FileInputStream(file1);
                        NBTTagCompound nBTTagCompound = CompressedStreamTools.readCompressed(fileinputstream);
                        fileinputstream.close();
                        worldSavedData.readFromNBT(nBTTagCompound.getCompoundTag("data"));
                    }
                }
                catch (Exception exception1)
                {
                    net.minecraft.src.Config.warn(exception1.getClass().getName() + ": " + exception1.getMessage(), exception1);
                }
            }

            if (worldSavedData != null)
            {
                this.loadedDataMap.put(dataIdentifier, worldSavedData);
                this.loadedDataList.add(worldSavedData);
            }

            return worldSavedData;
        }
    }

    public void setData(String dataIdentifier, WorldSavedData data)
    {
        if (this.loadedDataMap.containsKey(dataIdentifier))
        {
            this.loadedDataList.remove(this.loadedDataMap.remove(dataIdentifier));
        }

        this.loadedDataMap.put(dataIdentifier, data);
        this.loadedDataList.add(data);
    }

    public void saveAllData()
    {
        for (int i = 0; i < this.loadedDataList.size(); ++i)
        {
            WorldSavedData worldSavedData = (WorldSavedData)this.loadedDataList.get(i);

            if (worldSavedData.isDirty())
            {
                this.saveData(worldSavedData);
                worldSavedData.setDirty(false);
            }
        }
    }

    private void saveData(WorldSavedData data)
    {
        if (this.saveHandler != null)
        {
            try
            {
                File file1 = this.saveHandler.getMapFileFromName(data.mapName);

                if (file1 != null)
                {
                    NBTTagCompound nBTTagCompound = new NBTTagCompound();
                    data.writeToNBT(nBTTagCompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("data", nBTTagCompound);
                    FileOutputStream fileOutputStream = new FileOutputStream(file1);
                    CompressedStreamTools.writeCompressed(nbttagcompound1, fileOutputStream);
                    fileOutputStream.close();
                }
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }
    }

    private void loadIdCounts()
    {
        try
        {
            this.idCounts.clear();

            if (this.saveHandler == null)
            {
                return;
            }

            File file1 = this.saveHandler.getMapFileFromName("idcounts");

            if (file1 != null && file1.exists())
            {
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file1));
                NBTTagCompound nBTTagCompound = CompressedStreamTools.read(dataInputStream);
                dataInputStream.close();

                for (String s : nBTTagCompound.getKeySet())
                {
                    NBTBase nBTBase = nBTTagCompound.getTag(s);

                    if (nBTBase instanceof NBTTagShort)
                    {
                        NBTTagShort nBTTagShort = (NBTTagShort)nBTBase;
                        short short1 = nBTTagShort.getShort();
                        this.idCounts.put(s, Short.valueOf(short1));
                    }
                }
            }
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    public int getUniqueDataId(String key)
    {
        Short oshort = (Short)this.idCounts.get(key);

        if (oshort == null)
        {
            oshort = Short.valueOf((short)0);
        }
        else
        {
            oshort = Short.valueOf((short)(oshort.shortValue() + 1));
        }

        this.idCounts.put(key, oshort);

        if (this.saveHandler == null)
        {
            return oshort.shortValue();
        }
        else
        {
            try
            {
                File file1 = this.saveHandler.getMapFileFromName("idcounts");

                if (file1 != null)
                {
                    NBTTagCompound nBTTagCompound = new NBTTagCompound();

                    for (String s : this.idCounts.keySet())
                    {
                        short short1 = ((Short)this.idCounts.get(s)).shortValue();
                        nBTTagCompound.setShort(s, short1);
                    }

                    DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));
                    CompressedStreamTools.write(nBTTagCompound, dataoutputstream);
                    dataoutputstream.close();
                }
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }

            return oshort.shortValue();
        }
    }
}
