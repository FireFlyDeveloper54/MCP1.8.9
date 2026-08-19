package net.minecraft.client.resources;

import com.google.gson.JsonParseException;
import java.io.IOException;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourcePackListEntryDefault extends ResourcePackListEntry
{
    private static final Logger logger = LogManager.getLogger();
    private final IResourcePack defaultResourcePack;
    private final ResourceLocation resourcePackIcon;

    public ResourcePackListEntryDefault(GuiScreenResourcePacks resourcePacksGUIIn)
    {
        super(resourcePacksGUIIn);
        this.defaultResourcePack = this.mc.getResourcePackRepository().rprDefaultResourcePack;
        DynamicTexture dynamicTexture;

        try
        {
            dynamicTexture = new DynamicTexture(this.defaultResourcePack.getPackImage());
        }
        catch (IOException caughtIoException)
        {
            dynamicTexture = TextureUtil.missingTexture;
        }

        this.resourcePackIcon = this.mc.getTextureManager().getDynamicTextureLocation("texturepackicon", dynamicTexture);
    }

    protected int getPackFormat()
    {
        return 1;
    }

    protected String getResourcePackDescription()
    {
        try
        {
            PackMetadataSection packmetadatasection = (PackMetadataSection)this.defaultResourcePack.getPackMetadata(this.mc.getResourcePackRepository().rprMetadataSerializer, "pack");

            if (packmetadatasection != null)
            {
                return packmetadatasection.getPackDescription().getFormattedText();
            }
        }
        catch (JsonParseException jsonparseexception)
        {
            logger.error((String)"Couldn\'t load metadata info", (Throwable)jsonparseexception);
        }
        catch (IOException ioexception)
        {
            logger.error((String)"Couldn\'t load metadata info", (Throwable)ioexception);
        }

        return EnumChatFormatting.RED + "Missing " + "pack.mcmeta" + " :(";
    }

    protected boolean canAddToSelected()
    {
        return false;
    }

    protected boolean canRemoveFromSelected()
    {
        return false;
    }

    protected boolean canMoveUp()
    {
        return false;
    }

    protected boolean canMoveDown()
    {
        return false;
    }

    protected String getResourcePackName()
    {
        return "Default";
    }

    protected void bindResourcePackIcon()
    {
        this.mc.getTextureManager().bindTexture(this.resourcePackIcon);
    }

    protected boolean canBeMoved()
    {
        return false;
    }
}
