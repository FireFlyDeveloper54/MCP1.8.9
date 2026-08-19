package net.minecraft.client.resources.data;

import net.minecraft.util.IChatComponent;

public class PackMetadataSection implements IMetadataSection
{
    private final IChatComponent packDescription;
    private final int packFormat;

    public PackMetadataSection(IChatComponent packDescriptionIn, int packFormatIn)
    {
        this.packDescription = packDescriptionIn;
        this.packFormat = packFormatIn;
    }

    public IChatComponent getPackDescription()
    {
        return this.packDescription;
    }

    public int getPackFormat()
    {
        return this.packFormat;
    }
}
