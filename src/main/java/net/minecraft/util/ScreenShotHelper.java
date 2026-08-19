package net.minecraft.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.src.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ScreenShotHelper
{
    private static final Logger logger = LogManager.getLogger();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static IntBuffer pixelBuffer;
    private static int[] pixelValues;

    public static IChatComponent saveScreenshot(File gameDirectory, int width, int height, Framebuffer buffer)
    {
        return saveScreenshot(gameDirectory, (String)null, width, height, buffer);
    }

    public static IChatComponent saveScreenshot(File gameDirectory, String screenshotName, int width, int height, Framebuffer buffer)
    {
        try
        {
            File screenshotDirectory = new File(gameDirectory, "screenshots");
            screenshotDirectory.mkdir();
            Minecraft minecraft = Minecraft.getMinecraft();
            int previousGuiScale = Config.getGameSettings().guiScale;
            ScaledResolution scaledResolution = new ScaledResolution(minecraft);
            int scaleFactor = scaledResolution.getScaleFactor();
            int screenshotScale = Config.getScreenshotSize();
            boolean highResolutionScreenshot = OpenGlHelper.isFramebufferEnabled() && screenshotScale > 1;

            if (highResolutionScreenshot)
            {
                Config.getGameSettings().guiScale = scaleFactor * screenshotScale;
                resize(width * screenshotScale, height * screenshotScale);
                GlStateManager.pushMatrix();
                GlStateManager.clear(16640);
                minecraft.getFramebuffer().bindFramebuffer(true);
                minecraft.entityRenderer.updateCameraAndRender(Config.renderPartialTicks, System.nanoTime());
            }

            if (OpenGlHelper.isFramebufferEnabled())
            {
                width = buffer.framebufferTextureWidth;
                height = buffer.framebufferTextureHeight;
            }

            int pixelCount = width * height;

            if (pixelBuffer == null || pixelBuffer.capacity() < pixelCount)
            {
                pixelBuffer = BufferUtils.createIntBuffer(pixelCount);
                pixelValues = new int[pixelCount];
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            if (OpenGlHelper.isFramebufferEnabled())
            {
                GlStateManager.bindTexture(buffer.framebufferTexture);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)pixelBuffer);
            }
            else
            {
                GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)pixelBuffer);
            }

            pixelBuffer.get(pixelValues);
            TextureUtil.processPixelValues(pixelValues, width, height);
            BufferedImage image = null;

            if (OpenGlHelper.isFramebufferEnabled())
            {
                image = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, 1);
                int framebufferYOffset = buffer.framebufferTextureHeight - buffer.framebufferHeight;

                for (int framebufferY = framebufferYOffset; framebufferY < buffer.framebufferTextureHeight; ++framebufferY)
                {
                    for (int framebufferX = 0; framebufferX < buffer.framebufferWidth; ++framebufferX)
                    {
                        image.setRGB(framebufferX, framebufferY - framebufferYOffset, pixelValues[framebufferY * buffer.framebufferTextureWidth + framebufferX]);
                    }
                }
            }
            else
            {
                image = new BufferedImage(width, height, 1);
                image.setRGB(0, 0, width, height, pixelValues, 0, width);
            }

            if (highResolutionScreenshot)
            {
                minecraft.getFramebuffer().unbindFramebuffer();
                GlStateManager.popMatrix();
                Config.getGameSettings().guiScale = previousGuiScale;
                resize(width, height);
            }

            File screenshotFile;

            if (screenshotName == null)
            {
                screenshotFile = getTimestampedPNGFileForDirectory(screenshotDirectory);
            }
            else
            {
                screenshotFile = new File(screenshotDirectory, screenshotName);
            }

            screenshotFile = screenshotFile.getCanonicalFile();
            ImageIO.write(image, "png", (File)screenshotFile);
            IChatComponent fileComponent = new ChatComponentText(screenshotFile.getName());
            fileComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotFile.getAbsolutePath()));
            fileComponent.getChatStyle().setUnderlined(Boolean.valueOf(true));
            return new ChatComponentTranslation("screenshot.success", new Object[] {fileComponent});
        }
        catch (Exception exception)
        {
            logger.warn((String)"Couldn\'t save screenshot", (Throwable)exception);
            return new ChatComponentTranslation("screenshot.failure", new Object[] {exception.getMessage()});
        }
    }

    private static File getTimestampedPNGFileForDirectory(File gameDirectory)
    {
        String timestamp = dateFormat.format(new Date()).toString();
        int copyIndex = 1;

        while (true)
        {
            File screenshotFile = new File(gameDirectory, timestamp + (copyIndex == 1 ? "" : "_" + copyIndex) + ".png");

            if (!screenshotFile.exists())
            {
                return screenshotFile;
            }

            ++copyIndex;
        }
    }

    private static void resize(int width, int height)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.displayWidth = Math.max(1, width);
        minecraft.displayHeight = Math.max(1, height);

        if (minecraft.currentScreen != null)
        {
            ScaledResolution scaledResolution = new ScaledResolution(minecraft);
            minecraft.currentScreen.onResize(minecraft, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        }

        updateFramebufferSize();
    }

    private static void updateFramebufferSize()
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.getFramebuffer().createBindFramebuffer(minecraft.displayWidth, minecraft.displayHeight);

        if (minecraft.entityRenderer != null)
        {
            minecraft.entityRenderer.updateShaderGroupSize(minecraft.displayWidth, minecraft.displayHeight);
        }
    }
}
