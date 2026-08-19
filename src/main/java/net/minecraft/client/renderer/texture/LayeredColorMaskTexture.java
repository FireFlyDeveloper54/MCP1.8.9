package net.minecraft.client.renderer.texture;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LayeredColorMaskTexture extends AbstractTexture
{
    private static final Logger LOG = LogManager.getLogger();
    private final ResourceLocation textureLocation;
    private final List<String> layerTextureNames;
    private final List<EnumDyeColor> layerDyeColors;

    public LayeredColorMaskTexture(ResourceLocation textureLocationIn, List<String> layerTextureNamesIn, List<EnumDyeColor> layerDyeColorsIn)
    {
        this.textureLocation = textureLocationIn;
        this.layerTextureNames = layerTextureNamesIn;
        this.layerDyeColors = layerDyeColorsIn;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        this.deleteGlTexture();
        BufferedImage compositedImage;

        try
        {
            BufferedImage baseImage = TextureUtil.readBufferedImage(resourceManager.getResource(this.textureLocation).getInputStream());
            int imageType = baseImage.getType();

            if (imageType == 0)
            {
                imageType = 6;
            }

            compositedImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), imageType);
            Graphics graphics = compositedImage.getGraphics();
            graphics.drawImage(baseImage, 0, 0, (ImageObserver)null);

            for (int layerIndex = 0; layerIndex < 17 && layerIndex < this.layerTextureNames.size() && layerIndex < this.layerDyeColors.size(); ++layerIndex)
            {
                String layerTextureName = this.layerTextureNames.get(layerIndex);
                MapColor dyeMapColor = this.layerDyeColors.get(layerIndex).getMapColor();

                if (layerTextureName != null)
                {
                    InputStream inputStream = resourceManager.getResource(new ResourceLocation(layerTextureName)).getInputStream();
                    BufferedImage maskImage = TextureUtil.readBufferedImage(inputStream);

                    if (maskImage.getWidth() == compositedImage.getWidth() && maskImage.getHeight() == compositedImage.getHeight() && maskImage.getType() == 6)
                    {
                        for (int pixelY = 0; pixelY < maskImage.getHeight(); ++pixelY)
                        {
                            for (int pixelX = 0; pixelX < maskImage.getWidth(); ++pixelX)
                            {
                                int maskPixel = maskImage.getRGB(pixelX, pixelY);

                                if ((maskPixel & -16777216) != 0)
                                {
                                    int maskAlpha = (maskPixel & 16711680) << 8 & -16777216;
                                    int basePixel = baseImage.getRGB(pixelX, pixelY);
                                    int tintedPixel = MathHelper.multiplyColor(basePixel, dyeMapColor.colorValue) & 16777215;
                                    maskImage.setRGB(pixelX, pixelY, maskAlpha | tintedPixel);
                                }
                            }
                        }

                        compositedImage.getGraphics().drawImage(maskImage, 0, 0, (ImageObserver)null);
                    }
                }
            }
        }
        catch (IOException ioexception)
        {
            LOG.error((String)"Couldn\'t load layered image", (Throwable)ioexception);
            return;
        }

        if (Config.isShaders())
        {
            ShadersTex.loadSimpleTexture(this.getGlTextureId(), compositedImage, false, false, resourceManager, this.textureLocation, this.getMultiTexID());
        }
        else
        {
            TextureUtil.uploadTextureImage(this.getGlTextureId(), compositedImage);
        }
    }
}
