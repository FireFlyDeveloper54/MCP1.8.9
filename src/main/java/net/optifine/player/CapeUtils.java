package net.optifine.player;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

public class CapeUtils
{
    public static void downloadCape(AbstractClientPlayer player)
    {

        player.setLocationOfCape((ResourceLocation)null);
        player.setElytraOfCape(false);
    }

    public static BufferedImage parseCape(BufferedImage img)
    {
        int imageWidth = 64;
        int imageHeight = 32;
        int sourceWidth = img.getWidth();

        for (int sourceHeight = img.getHeight(); imageWidth < sourceWidth || imageHeight < sourceHeight; imageHeight *= 2)
        {
            imageWidth *= 2;
        }

        BufferedImage capeImage = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics graphics = capeImage.getGraphics();
        graphics.drawImage(img, 0, 0, (ImageObserver)null);
        graphics.dispose();
        return capeImage;
    }

    public static boolean isElytraCape(BufferedImage imageRaw, BufferedImage imageFixed)
    {
        return imageRaw.getWidth() > imageFixed.getHeight();
    }

    public static void reloadCape(AbstractClientPlayer player)
    {
        String playerName = player.getNameClear();
        ResourceLocation capeLocation = new ResourceLocation("capeof/" + playerName);
        TextureManager textureManager = Config.getTextureManager();
        ITextureObject textureObject = textureManager.getTexture(capeLocation);

        if (textureObject instanceof SimpleTexture)
        {
            SimpleTexture simpleTexture = (SimpleTexture)textureObject;
            simpleTexture.deleteGlTexture();
            textureManager.deleteTexture(capeLocation);
        }

        player.setLocationOfCape((ResourceLocation)null);
        player.setElytraOfCape(false);
        downloadCape(player);
    }
}
