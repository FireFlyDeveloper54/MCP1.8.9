package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.optifine.SmartAnimations;
import net.optifine.TextureAnimations;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.NativeMemory;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class GuiOverlayDebug extends Gui
{
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    private String debugOF = null;
    private List<String> debugInfoLeft = null;
    private List<String> debugInfoRight = null;
    private long updateInfoLeftTimeMs = 0L;
    private long updateInfoRightTimeMs = 0L;

    public GuiOverlayDebug(Minecraft mc)
    {
        this.mc = mc;
        this.fontRenderer = mc.fontRendererObj;
    }

    public void renderDebugInfo(ScaledResolution scaledResolutionIn)
    {
        this.mc.mcProfiler.startSection("debug");
        GlStateManager.pushMatrix();
        this.renderDebugInfoLeft();
        this.renderDebugInfoRight(scaledResolutionIn);
        GlStateManager.popMatrix();

        if (this.mc.gameSettings.showLagometer)
        {
            this.renderLagometer();
        }

        this.mc.mcProfiler.endSection();
    }

    private boolean isReducedDebug()
    {
        return this.mc.thePlayer.hasReducedDebug() || this.mc.gameSettings.reducedDebugInfo;
    }

    protected void renderDebugInfoLeft()
    {
        List<String> debugLines = this.debugInfoLeft;

        if (debugLines == null || System.currentTimeMillis() > this.updateInfoLeftTimeMs)
        {
            debugLines = this.call();
            this.debugInfoLeft = debugLines;
            this.updateInfoLeftTimeMs = System.currentTimeMillis() + 100L;
        }

        for (int lineIndex = 0; lineIndex < debugLines.size(); ++lineIndex)
        {
            String line = debugLines.get(lineIndex);

            if (!Strings.isNullOrEmpty(line))
            {
                int lineHeight = this.fontRenderer.FONT_HEIGHT;
                int lineWidth = this.fontRenderer.getStringWidth(line);
                int lineY = 2 + lineHeight * lineIndex;
                drawRect(1, lineY - 1, 2 + lineWidth + 1, lineY + lineHeight - 1, -1873784752);
                this.fontRenderer.drawString(line, 2, lineY, 14737632);
            }
        }
    }

    protected void renderDebugInfoRight(ScaledResolution scaledRes)
    {
        List<String> debugLines = this.debugInfoRight;

        if (debugLines == null || System.currentTimeMillis() > this.updateInfoRightTimeMs)
        {
            debugLines = this.getDebugInfoRight();
            this.debugInfoRight = debugLines;
            this.updateInfoRightTimeMs = System.currentTimeMillis() + 100L;
        }

        for (int lineIndex = 0; lineIndex < debugLines.size(); ++lineIndex)
        {
            String line = debugLines.get(lineIndex);

            if (!Strings.isNullOrEmpty(line))
            {
                int lineHeight = this.fontRenderer.FONT_HEIGHT;
                int lineWidth = this.fontRenderer.getStringWidth(line);
                int lineX = scaledRes.getScaledWidth() - 2 - lineWidth;
                int lineY = 2 + lineHeight * lineIndex;
                drawRect(lineX - 1, lineY - 1, lineX + lineWidth + 1, lineY + lineHeight - 1, -1873784752);
                this.fontRenderer.drawString(line, lineX, lineY, 14737632);
            }
        }
    }

    @SuppressWarnings("incomplete-switch")
    protected List<String> call()
    {
        BlockPos cameraBlockPos = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ);

        if (this.mc.debug != this.debugOF)
        {
            StringBuffer debugText = new StringBuffer(this.mc.debug);
            int minFps = Config.getFpsMin();
            int fpsTextIndex = this.mc.debug.indexOf(" fps ");

            if (fpsTextIndex >= 0)
            {
                debugText.insert(fpsTextIndex, "/" + minFps);
            }

            if (Config.isSmoothFps())
            {
                debugText.append(" sf");
            }

            if (Config.isFastRender())
            {
                debugText.append(" fr");
            }

            if (Config.isAnisotropicFiltering())
            {
                debugText.append(" af");
            }

            if (Config.isAntialiasing())
            {
                debugText.append(" aa");
            }

            if (Config.isRenderRegions())
            {
                debugText.append(" reg");
            }

            if (Config.isShaders())
            {
                debugText.append(" sh");
            }

            this.mc.debug = debugText.toString();
            this.debugOF = this.mc.debug;
        }

        StringBuilder animationStats = new StringBuilder();
        TextureMap textureMap = Config.getTextureMap();
        animationStats.append(", A: ");

        if (SmartAnimations.isActive())
        {
            animationStats.append(textureMap.getCountAnimationsActive() + TextureAnimations.getCountAnimationsActive());
            animationStats.append("/");
        }

        animationStats.append(textureMap.getCountAnimations() + TextureAnimations.getCountAnimations());
        String animationText = animationStats.toString();

        if (this.isReducedDebug())
        {
            return Lists.newArrayList(new String[] {"Minecraft 1.8.9 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities() + animationText, this.mc.theWorld.getProviderName(), "", "Chunk-relative: " + (cameraBlockPos.getX() & 15) + " " + (cameraBlockPos.getY() & 15) + " " + (cameraBlockPos.getZ() & 15)});
        }
        else
        {
            Entity entity = this.mc.getRenderViewEntity();
            EnumFacing facing = entity.getHorizontalFacing();
            String facingDescription = "Invalid";

            switch (facing)
            {
                case NORTH:
                    facingDescription = "Towards negative Z";
                    break;

                case SOUTH:
                    facingDescription = "Towards positive Z";
                    break;

                case WEST:
                    facingDescription = "Towards negative X";
                    break;

                case EAST:
                    facingDescription = "Towards positive X";
            }

            List<String> debugLines = Lists.newArrayList(new String[] {"Minecraft 1.8.9 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities() + animationText, this.mc.theWorld.getProviderName(), "", String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ), "Block: " + cameraBlockPos.getX() + " " + cameraBlockPos.getY() + " " + cameraBlockPos.getZ(), "Chunk: " + (cameraBlockPos.getX() & 15) + " " + (cameraBlockPos.getY() & 15) + " " + (cameraBlockPos.getZ() & 15) + " in " + (cameraBlockPos.getX() >> 4) + " " + (cameraBlockPos.getY() >> 4) + " " + (cameraBlockPos.getZ() >> 4), String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", facing, facingDescription, MathHelper.wrapAngleTo180_float(entity.rotationYaw), MathHelper.wrapAngleTo180_float(entity.rotationPitch))});

            if (this.mc.theWorld != null && this.mc.theWorld.isBlockLoaded(cameraBlockPos))
            {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(cameraBlockPos);
                debugLines.add("Biome: " + chunk.getBiome(cameraBlockPos, this.mc.theWorld.getWorldChunkManager()).biomeName);
                debugLines.add("Light: " + chunk.getLightSubtracted(cameraBlockPos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, cameraBlockPos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, cameraBlockPos) + " block)");
                DifficultyInstance difficultyInstance = this.mc.theWorld.getDifficultyForLocation(cameraBlockPos);

                if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null)
                {
                    EntityPlayerMP entityPlayerMP = this.mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(this.mc.thePlayer.getUniqueID());

                    if (entityPlayerMP != null)
                    {
                        DifficultyInstance serverDifficulty = this.mc.getIntegratedServer().getDifficultyAsync(entityPlayerMP.worldObj, new BlockPos(entityPlayerMP));

                        if (serverDifficulty != null)
                        {
                            difficultyInstance = serverDifficulty;
                        }
                    }
                }

                debugLines.add(String.format(Locale.ROOT, "Local Difficulty: %.2f (Day %d)", difficultyInstance.getAdditionalDifficulty(), this.mc.theWorld.getWorldTime() / 24000L));
            }

            if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive())
            {
                debugLines.add("Shader: " + this.mc.entityRenderer.getShaderGroup().getShaderGroupName());
            }

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null)
            {
                BlockPos targetBlockPos = this.mc.objectMouseOver.getBlockPos();
                debugLines.add("Looking at: " + targetBlockPos.getX() + " " + targetBlockPos.getY() + " " + targetBlockPos.getZ());
            }

            return debugLines;
        }
    }

    protected List<String> getDebugInfoRight()
    {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        List<String> debugLines = Lists.newArrayList(new String[] {"Java: " + System.getProperty("java.version") + " " + (this.mc.isJava64bit() ? 64 : 32) + "bit", String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", usedMemory * 100L / maxMemory, bytesToMb(usedMemory), bytesToMb(maxMemory)), String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", totalMemory * 100L / maxMemory, bytesToMb(totalMemory)), "", "CPU: " + OpenGlHelper.getCpu(), "", "Display: " + Display.getWidth() + "x" + Display.getHeight() + " (" + GL11.glGetString(GL11.GL_VENDOR) + ")", GL11.glGetString(GL11.GL_RENDERER), GL11.glGetString(GL11.GL_VERSION)});
        long nativeBufferAllocated = NativeMemory.getBufferAllocated();
        long nativeBufferMaximum = NativeMemory.getBufferMaximum();
        String gcText = "GC: " + MemoryMonitor.getAllocationRateMb() + "MB/s";

        if (nativeBufferAllocated >= 0L && nativeBufferMaximum >= 0L)
        {
            String nativeMemoryText = "Native: " + bytesToMb(nativeBufferAllocated) + "/" + bytesToMb(nativeBufferMaximum) + "MB";
            debugLines.add(4, nativeMemoryText);
            debugLines.set(5, gcText);
        }
        else
        {
            debugLines.set(4, gcText);
        }

        
        if (this.isReducedDebug())
        {
            return debugLines;
        }
        else
        {
            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null)
            {
                BlockPos targetBlockPos = this.mc.objectMouseOver.getBlockPos();
                IBlockState blockState = this.mc.theWorld.getBlockState(targetBlockPos);

                if (this.mc.theWorld.getWorldType() != WorldType.DEBUG_WORLD)
                {
                    blockState = blockState.getBlock().getActualState(blockState, this.mc.theWorld, targetBlockPos);
                }

                debugLines.add("");
                debugLines.add(String.valueOf(Block.blockRegistry.getNameForObject(blockState.getBlock())));

                for (Entry<IProperty, Comparable> entry : blockState.getProperties().entrySet())
                {
                    String propertyValue = ((Comparable)entry.getValue()).toString();

                    if (entry.getValue() == Boolean.TRUE)
                    {
                        propertyValue = EnumChatFormatting.GREEN + propertyValue;
                    }
                    else if (entry.getValue() == Boolean.FALSE)
                    {
                        propertyValue = EnumChatFormatting.RED + propertyValue;
                    }

                    debugLines.add(((IProperty)entry.getKey()).getName() + ": " + propertyValue);
                }
            }

            return debugLines;
        }
    }

    private void renderLagometer()
    {
    }

    private int getFrameColor(int frameTime, int minFrameTime, int targetFrameTime, int maxFrameTime)
    {
        return frameTime < targetFrameTime ? this.blendColors(-16711936, -256, (float)frameTime / (float)targetFrameTime) : this.blendColors(-256, -65536, (float)(frameTime - targetFrameTime) / (float)(maxFrameTime - targetFrameTime));
    }

    private int blendColors(int colorA, int colorB, float ratio)
    {
        int alphaA = colorA >> 24 & 255;
        int redA = colorA >> 16 & 255;
        int greenA = colorA >> 8 & 255;
        int blueA = colorA & 255;
        int alphaB = colorB >> 24 & 255;
        int redB = colorB >> 16 & 255;
        int greenB = colorB >> 8 & 255;
        int blueB = colorB & 255;
        int alpha = MathHelper.clamp_int((int)((float)alphaA + (float)(alphaB - alphaA) * ratio), 0, 255);
        int red = MathHelper.clamp_int((int)((float)redA + (float)(redB - redA) * ratio), 0, 255);
        int green = MathHelper.clamp_int((int)((float)greenA + (float)(greenB - greenA) * ratio), 0, 255);
        int blue = MathHelper.clamp_int((int)((float)blueA + (float)(blueB - blueA) * ratio), 0, 255);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static long bytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }
}
