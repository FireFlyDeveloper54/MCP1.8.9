package net.minecraft.client.resources.data;

import java.util.Collections;
import java.util.List;

public class TextureMetadataSection implements IMetadataSection
{
    private final boolean textureBlur;
    private final boolean textureClamp;
    private final List<Integer> listMipmaps;

    public TextureMetadataSection(boolean textureBlurIn, boolean textureClampIn, List<Integer> listMipmapsIn)
    {
        this.textureBlur = textureBlurIn;
        this.textureClamp = textureClampIn;
        this.listMipmaps = listMipmapsIn;
    }

    public boolean getTextureBlur()
    {
        return this.textureBlur;
    }

    public boolean getTextureClamp()
    {
        return this.textureClamp;
    }

    public List<Integer> getListMipmaps()
    {
        return Collections.<Integer>unmodifiableList(this.listMipmaps);
    }
}
