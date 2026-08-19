package net.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.profiler.Profiler;
import net.minecraft.src.Config;
import net.optifine.util.MemoryMonitor;
import org.lwjgl.opengl.GL11;

public class Lagometer
{
    private static Minecraft mc;
    private static GameSettings gameSettings;
    private static Profiler profiler;
    public static boolean active = false;
    public static Lagometer.TimerNano timerTick = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerScheduledExecutables = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerChunkUpload = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerChunkUpdate = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerVisibility = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerTerrain = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerServer = new Lagometer.TimerNano();
    private static long[] timesFrame = new long[512];
    private static long[] timesTick = new long[512];
    private static long[] timesScheduledExecutables = new long[512];
    private static long[] timesChunkUpload = new long[512];
    private static long[] timesChunkUpdate = new long[512];
    private static long[] timesVisibility = new long[512];
    private static long[] timesTerrain = new long[512];
    private static long[] timesServer = new long[512];
    private static boolean[] gcs = new boolean[512];
    private static int numRecordedFrameTimes = 0;
    private static long prevFrameTimeNano = -1L;
    private static long renderTimeNano = 0L;

    public static void updateLagometer()
    {
        if (mc == null)
        {
            mc = Minecraft.getMinecraft();
            gameSettings = mc.gameSettings;
            profiler = mc.mcProfiler;
        }

        if (gameSettings.showDebugInfo && (gameSettings.ofLagometer || gameSettings.showLagometer))
        {
            active = true;
            long timeNowNano = System.nanoTime();

            if (prevFrameTimeNano == -1L)
            {
                prevFrameTimeNano = timeNowNano;
            }
            else
            {
                int frameIndex = numRecordedFrameTimes & timesFrame.length - 1;
                ++numRecordedFrameTimes;
                boolean gcEvent = MemoryMonitor.isGcEvent();
                timesFrame[frameIndex] = timeNowNano - prevFrameTimeNano - renderTimeNano;
                timesTick[frameIndex] = timerTick.timeNano;
                timesScheduledExecutables[frameIndex] = timerScheduledExecutables.timeNano;
                timesChunkUpload[frameIndex] = timerChunkUpload.timeNano;
                timesChunkUpdate[frameIndex] = timerChunkUpdate.timeNano;
                timesVisibility[frameIndex] = timerVisibility.timeNano;
                timesTerrain[frameIndex] = timerTerrain.timeNano;
                timesServer[frameIndex] = timerServer.timeNano;
                gcs[frameIndex] = gcEvent;
                timerTick.reset();
                timerScheduledExecutables.reset();
                timerVisibility.reset();
                timerChunkUpdate.reset();
                timerChunkUpload.reset();
                timerTerrain.reset();
                timerServer.reset();
                prevFrameTimeNano = System.nanoTime();
            }
        }
        else
        {
            active = false;
            prevFrameTimeNano = -1L;
        }
    }

    public static void showLagometer(ScaledResolution scaledResolution)
    {
        if (gameSettings != null)
        {
            if (gameSettings.ofLagometer || gameSettings.showLagometer)
            {
                long startTimeNano = System.nanoTime();
                GlStateManager.clear(256);
                GlStateManager.matrixMode(5889);
                GlStateManager.pushMatrix();
                GlStateManager.enableColorMaterial();
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0.0D, (double)mc.displayWidth, (double)mc.displayHeight, 0.0D, 1000.0D, 3000.0D);
                GlStateManager.matrixMode(5888);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.translate(0.0F, 0.0F, -2000.0F);
                GL11.glLineWidth(1.0F);
                GlStateManager.disableTexture2D();
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);

                for (int frameIndex = 0; frameIndex < timesFrame.length; ++frameIndex)
                {
                    int shade = (frameIndex - numRecordedFrameTimes & timesFrame.length - 1) * 100 / timesFrame.length;
                    shade = shade + 155;
                    float graphHeight = (float)mc.displayHeight;
                    long unusedTime = 0L;

                    if (gcs[frameIndex])
                    {
                        renderTime(frameIndex, timesFrame[frameIndex], shade, shade / 2, 0, graphHeight, worldRenderer);
                    }
                    else
                    {
                        renderTime(frameIndex, timesFrame[frameIndex], shade, shade, shade, graphHeight, worldRenderer);
                        graphHeight = graphHeight - (float)renderTime(frameIndex, timesServer[frameIndex], shade / 2, shade / 2, shade / 2, graphHeight, worldRenderer);
                        graphHeight = graphHeight - (float)renderTime(frameIndex, timesTerrain[frameIndex], 0, shade, 0, graphHeight, worldRenderer);
                        graphHeight = graphHeight - (float)renderTime(frameIndex, timesVisibility[frameIndex], shade, shade, 0, graphHeight, worldRenderer);
                        graphHeight = graphHeight - (float)renderTime(frameIndex, timesChunkUpdate[frameIndex], shade, 0, 0, graphHeight, worldRenderer);
                        graphHeight = graphHeight - (float)renderTime(frameIndex, timesChunkUpload[frameIndex], shade, 0, shade, graphHeight, worldRenderer);
                        graphHeight = graphHeight - (float)renderTime(frameIndex, timesScheduledExecutables[frameIndex], 0, 0, shade, graphHeight, worldRenderer);
                        float tickGraphHeight = graphHeight - (float)renderTime(frameIndex, timesTick[frameIndex], 0, shade, shade, graphHeight, worldRenderer);
                    }
                }

                renderTimeDivider(0, timesFrame.length, 33333333L, 196, 196, 196, (float)mc.displayHeight, worldRenderer);
                renderTimeDivider(0, timesFrame.length, 16666666L, 196, 196, 196, (float)mc.displayHeight, worldRenderer);
                tessellator.draw();
                GlStateManager.enableTexture2D();
                int sixtyFpsY = mc.displayHeight - 80;
                int thirtyFpsY = mc.displayHeight - 160;
                mc.fontRendererObj.drawString("30", 2, thirtyFpsY + 1, -8947849);
                mc.fontRendererObj.drawString("30", 1, thirtyFpsY, -3881788);
                mc.fontRendererObj.drawString("60", 2, sixtyFpsY + 1, -8947849);
                mc.fontRendererObj.drawString("60", 1, sixtyFpsY, -3881788);
                GlStateManager.matrixMode(5889);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                GlStateManager.popMatrix();
                GlStateManager.enableTexture2D();
                float memoryFade = 1.0F - (float)((double)(System.currentTimeMillis() - MemoryMonitor.getStartTimeMs()) / 1000.0D);
                memoryFade = Config.limit(memoryFade, 0.0F, 1.0F);
                int memoryRed = (int)(170.0F + memoryFade * 85.0F);
                int memoryGreen = (int)(100.0F + memoryFade * 55.0F);
                int memoryBlue = (int)(10.0F + memoryFade * 10.0F);
                int memoryColor = memoryRed << 16 | memoryGreen << 8 | memoryBlue;
                int memoryX = 512 / scaledResolution.getScaleFactor() + 2;
                int memoryY = mc.displayHeight / scaledResolution.getScaleFactor() - 8;
                GuiIngame guiIngame = mc.ingameGUI;
                GuiIngame.drawRect(memoryX - 1, memoryY - 1, memoryX + 50, memoryY + 10, -1605349296);
                mc.fontRendererObj.drawString(" " + MemoryMonitor.getAllocationRateMb() + " MB/s", memoryX, memoryY, memoryColor);
                renderTimeNano = System.nanoTime() - startTimeNano;
            }
        }
    }

    private static long renderTime(int frameNum, long time, int r, int g, int b, float baseHeight, WorldRenderer tessellator)
    {
        long height = time / 200000L;

        if (height < 3L)
        {
            return 0L;
        }
        else
        {
            tessellator.pos((double)((float)frameNum + 0.5F), (double)(baseHeight - (float)height + 0.5F), 0.0D).color(r, g, b, 255).endVertex();
            tessellator.pos((double)((float)frameNum + 0.5F), (double)(baseHeight + 0.5F), 0.0D).color(r, g, b, 255).endVertex();
            return height;
        }
    }

    private static long renderTimeDivider(int frameStart, int frameEnd, long time, int r, int g, int b, float baseHeight, WorldRenderer tessellator)
    {
        long height = time / 200000L;

        if (height < 3L)
        {
            return 0L;
        }
        else
        {
            tessellator.pos((double)((float)frameStart + 0.5F), (double)(baseHeight - (float)height + 0.5F), 0.0D).color(r, g, b, 255).endVertex();
            tessellator.pos((double)((float)frameEnd + 0.5F), (double)(baseHeight - (float)height + 0.5F), 0.0D).color(r, g, b, 255).endVertex();
            return height;
        }
    }

    public static boolean isActive()
    {
        return active;
    }

    public static class TimerNano
    {
        public long timeStartNano = 0L;
        public long timeNano = 0L;

        public void start()
        {
            if (Lagometer.active)
            {
                if (this.timeStartNano == 0L)
                {
                    this.timeStartNano = System.nanoTime();
                }
            }
        }

        public void end()
        {
            if (Lagometer.active)
            {
                if (this.timeStartNano != 0L)
                {
                    this.timeNano += System.nanoTime() - this.timeStartNano;
                    this.timeStartNano = 0L;
                }
            }
        }

        private void reset()
        {
            this.timeNano = 0L;
            this.timeStartNano = 0L;
        }
    }
}
