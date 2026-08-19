package net.minecraft.realms;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;

public class RealmsAnvilLevelStorageSource
{
    private ISaveFormat levelStorageSource;

    public RealmsAnvilLevelStorageSource(ISaveFormat levelStorageSourceIn)
    {
        this.levelStorageSource = levelStorageSourceIn;
    }

    public String getName()
    {
        return this.levelStorageSource.getName();
    }

    public boolean levelExists(String levelId)
    {
        return this.levelStorageSource.canLoadWorld(levelId);
    }

    public boolean convertLevel(String levelId, IProgressUpdate progressCallback)
    {
        return this.levelStorageSource.convertMapFormat(levelId, progressCallback);
    }

    public boolean requiresConversion(String levelId)
    {
        return this.levelStorageSource.isOldMapFormat(levelId);
    }

    public boolean isNewLevelIdAcceptable(String levelId)
    {
        return this.levelStorageSource.isNewLevelIdAcceptable(levelId);
    }

    public boolean deleteLevel(String levelId)
    {
        return this.levelStorageSource.deleteWorldDirectory(levelId);
    }

    public boolean isConvertible(String levelId)
    {
        return this.levelStorageSource.isConvertible(levelId);
    }

    public void renameLevel(String levelId, String newName)
    {
        this.levelStorageSource.renameWorld(levelId, newName);
    }

    public void clearAll()
    {
        this.levelStorageSource.flushCache();
    }

    public List<RealmsLevelSummary> getLevelList() throws AnvilConverterException
    {
        List<RealmsLevelSummary> list = Lists.<RealmsLevelSummary>newArrayList();

        for (SaveFormatComparator saveformatcomparator : this.levelStorageSource.getSaveList())
        {
            list.add(new RealmsLevelSummary(saveformatcomparator));
        }

        return list;
    }
}
