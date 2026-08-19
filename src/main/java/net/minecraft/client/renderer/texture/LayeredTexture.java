package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LayeredTexture extends AbstractTexture
{
    private static final Logger logger = LogManager.getLogger();
    public final List<String> layeredTextureNames;
    private ResourceLocation textureLocation;

    public LayeredTexture(String... textureNames)
    {
        this.layeredTextureNames = Lists.newArrayList(textureNames);

        if (textureNames.length > 0 && textureNames[0] != null)
        {
            this.textureLocation = new ResourceLocation(textureNames[0]);
        }
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        this.deleteGlTexture();
        BufferedImage layeredImage = null;

        try
        {
            for (String textureName : this.layeredTextureNames)
            {
                if (textureName != null)
                {
                    InputStream inputStream = resourceManager.getResource(new ResourceLocation(textureName)).getInputStream();
                    BufferedImage layerImage = TextureUtil.readBufferedImage(inputStream);

                    if (layeredImage == null)
                    {
                        layeredImage = new BufferedImage(layerImage.getWidth(), layerImage.getHeight(), 2);
                    }

                    layeredImage.getGraphics().drawImage(layerImage, 0, 0, (ImageObserver)null);
                }
            }
        }
        catch (IOException ioexception)
        {
            logger.error((String)"Couldn\'t load layered image", (Throwable)ioexception);
            return;
        }

        if (Config.isShaders())
        {
            ShadersTex.loadSimpleTexture(this.getGlTextureId(), layeredImage, false, false, resourceManager, this.textureLocation, this.getMultiTexID());
        }
        else
        {
            TextureUtil.uploadTextureImage(this.getGlTextureId(), layeredImage);
        }
    }
}
