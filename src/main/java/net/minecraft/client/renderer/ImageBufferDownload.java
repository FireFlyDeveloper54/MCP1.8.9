package net.minecraft.client.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

public class ImageBufferDownload implements IImageBuffer
{
    private int[] imageData;
    private int imageWidth;
    private int imageHeight;

    public BufferedImage parseUserSkin(BufferedImage image)
    {
        if (image == null)
        {
            return null;
        }
        else
        {
            this.imageWidth = 64;
            this.imageHeight = 64;
            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            int scale;

            for (scale = 1; this.imageWidth < sourceWidth || this.imageHeight < sourceHeight; scale *= 2)
            {
                this.imageWidth *= 2;
                this.imageHeight *= 2;
            }

            BufferedImage bufferedImage = new BufferedImage(this.imageWidth, this.imageHeight, 2);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.drawImage(image, 0, 0, (ImageObserver)null);

            if (image.getHeight() == 32 * scale)
            {
                graphics.drawImage(bufferedImage, 24 * scale, 48 * scale, 20 * scale, 52 * scale, 4 * scale, 16 * scale, 8 * scale, 20 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 28 * scale, 48 * scale, 24 * scale, 52 * scale, 8 * scale, 16 * scale, 12 * scale, 20 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 20 * scale, 52 * scale, 16 * scale, 64 * scale, 8 * scale, 20 * scale, 12 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 24 * scale, 52 * scale, 20 * scale, 64 * scale, 4 * scale, 20 * scale, 8 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 28 * scale, 52 * scale, 24 * scale, 64 * scale, 0 * scale, 20 * scale, 4 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 32 * scale, 52 * scale, 28 * scale, 64 * scale, 12 * scale, 20 * scale, 16 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 40 * scale, 48 * scale, 36 * scale, 52 * scale, 44 * scale, 16 * scale, 48 * scale, 20 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 44 * scale, 48 * scale, 40 * scale, 52 * scale, 48 * scale, 16 * scale, 52 * scale, 20 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 36 * scale, 52 * scale, 32 * scale, 64 * scale, 48 * scale, 20 * scale, 52 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 40 * scale, 52 * scale, 36 * scale, 64 * scale, 44 * scale, 20 * scale, 48 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 44 * scale, 52 * scale, 40 * scale, 64 * scale, 40 * scale, 20 * scale, 44 * scale, 32 * scale, (ImageObserver)null);
                graphics.drawImage(bufferedImage, 48 * scale, 52 * scale, 44 * scale, 64 * scale, 52 * scale, 20 * scale, 56 * scale, 32 * scale, (ImageObserver)null);
            }

            graphics.dispose();
            this.imageData = ((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData();
            this.setAreaOpaque(0 * scale, 0 * scale, 32 * scale, 16 * scale);
            this.setAreaTransparent(32 * scale, 0 * scale, 64 * scale, 32 * scale);
            this.setAreaOpaque(0 * scale, 16 * scale, 64 * scale, 32 * scale);
            this.setAreaTransparent(0 * scale, 32 * scale, 16 * scale, 48 * scale);
            this.setAreaTransparent(16 * scale, 32 * scale, 40 * scale, 48 * scale);
            this.setAreaTransparent(40 * scale, 32 * scale, 56 * scale, 48 * scale);
            this.setAreaTransparent(0 * scale, 48 * scale, 16 * scale, 64 * scale);
            this.setAreaOpaque(16 * scale, 48 * scale, 48 * scale, 64 * scale);
            this.setAreaTransparent(48 * scale, 48 * scale, 64 * scale, 64 * scale);
            return bufferedImage;
        }
    }

    public void skinAvailable()
    {
    }

    private void setAreaTransparent(int startX, int startY, int endX, int endY)
    {
        if (!this.hasTransparency(startX, startY, endX, endY))
        {
            for (int x = startX; x < endX; ++x)
            {
                for (int y = startY; y < endY; ++y)
                {
                    this.imageData[x + y * this.imageWidth] &= 16777215;
                }
            }
        }
    }

    private void setAreaOpaque(int startX, int startY, int endX, int endY)
    {
        for (int x = startX; x < endX; ++x)
        {
            for (int y = startY; y < endY; ++y)
            {
                this.imageData[x + y * this.imageWidth] |= -16777216;
            }
        }
    }

    private boolean hasTransparency(int startX, int startY, int endX, int endY)
    {
        for (int x = startX; x < endX; ++x)
        {
            for (int y = startY; y < endY; ++y)
            {
                int alpha = this.imageData[x + y * this.imageWidth];

                if ((alpha >> 24 & 255) < 128)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
