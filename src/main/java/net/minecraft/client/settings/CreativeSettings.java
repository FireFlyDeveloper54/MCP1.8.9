package net.minecraft.client.settings;

import java.io.File;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreativeSettings
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final File dataFile;
    private final HotbarSnapshot[] hotbarSnapshots = new HotbarSnapshot[9];

    public CreativeSettings(File gameDirectory)
    {
        this.dataFile = new File(gameDirectory, "hotbar.nbt");

        for (int slot = 0; slot < this.hotbarSnapshots.length; ++slot)
        {
            this.hotbarSnapshots[slot] = new HotbarSnapshot();
        }

        this.read();
    }

    public void read()
    {
        if (!this.dataFile.isFile())
        {
            return;
        }

        try
        {
            NBTTagCompound rootTag = CompressedStreamTools.read(this.dataFile);

            if (rootTag == null)
            {
                return;
            }

            for (int slot = 0; slot < this.hotbarSnapshots.length; ++slot)
            {
                this.hotbarSnapshots[slot].fromTag(rootTag.getTagList(String.valueOf(slot), 10));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to load creative hotbar snapshots", exception);
        }
    }

    public void write()
    {
        try
        {
            NBTTagCompound rootTag = new NBTTagCompound();

            for (int slot = 0; slot < this.hotbarSnapshots.length; ++slot)
            {
                rootTag.setTag(String.valueOf(slot), this.hotbarSnapshots[slot].createTag());
            }

            CompressedStreamTools.write(rootTag, this.dataFile);
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to save creative hotbar snapshots", exception);
        }
    }

    public HotbarSnapshot getHotbarSnapshot(int slot)
    {
        return this.hotbarSnapshots[slot];
    }
}
