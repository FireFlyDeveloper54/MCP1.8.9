package net.minecraft.client.shader;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonException;
import org.lwjgl.util.vector.Matrix4f;

public class Shader
{
    private final ShaderManager manager;
    public final Framebuffer framebufferIn;
    public final Framebuffer framebufferOut;
    private final List<Object> listAuxFramebuffers = Lists.<Object>newArrayList();
    private final List<String> listAuxNames = Lists.<String>newArrayList();
    private final List<Integer> listAuxWidths = Lists.<Integer>newArrayList();
    private final List<Integer> listAuxHeights = Lists.<Integer>newArrayList();
    private Matrix4f projectionMatrix;

    public Shader(IResourceManager resourceManager, String programName, Framebuffer framebufferIn, Framebuffer framebufferOut) throws JsonException, IOException
    {
        this.manager = new ShaderManager(resourceManager, programName);
        this.framebufferIn = framebufferIn;
        this.framebufferOut = framebufferOut;
    }

    public void deleteShader()
    {
        this.manager.deleteShader();
    }

    public void addAuxFramebuffer(String name, Object framebufferObject, int width, int height)
    {
        this.listAuxNames.add(this.listAuxNames.size(), name);
        this.listAuxFramebuffers.add(this.listAuxFramebuffers.size(), framebufferObject);
        this.listAuxWidths.add(this.listAuxWidths.size(), Integer.valueOf(width));
        this.listAuxHeights.add(this.listAuxHeights.size(), Integer.valueOf(height));
    }

    private void preLoadShader()
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableColorMaterial();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(0);
    }

    public void setProjectionMatrix(Matrix4f projectionMatrixIn)
    {
        this.projectionMatrix = projectionMatrixIn;
    }

    public void loadShader(float time)
    {
        this.preLoadShader();
        this.framebufferIn.unbindFramebuffer();
        float framebufferWidth = (float)this.framebufferOut.framebufferTextureWidth;
        float framebufferHeight = (float)this.framebufferOut.framebufferTextureHeight;
        GlStateManager.viewport(0, 0, (int)framebufferWidth, (int)framebufferHeight);
        this.manager.addSamplerTexture("DiffuseSampler", this.framebufferIn);

        for (int auxIndex = 0; auxIndex < this.listAuxFramebuffers.size(); ++auxIndex)
        {
            float auxWidth = this.listAuxWidths.get(auxIndex);
            float auxHeight = this.listAuxHeights.get(auxIndex);
            this.manager.addSamplerTexture(this.listAuxNames.get(auxIndex), this.listAuxFramebuffers.get(auxIndex));
            this.manager.getShaderUniformOrDefault("AuxSize" + auxIndex).set(auxWidth, auxHeight);
        }

        this.manager.getShaderUniformOrDefault("ProjMat").set(this.projectionMatrix);
        this.manager.getShaderUniformOrDefault("InSize").set((float)this.framebufferIn.framebufferTextureWidth, (float)this.framebufferIn.framebufferTextureHeight);
        this.manager.getShaderUniformOrDefault("OutSize").set(framebufferWidth, framebufferHeight);
        this.manager.getShaderUniformOrDefault("Time").set(time);
        Minecraft minecraft = Minecraft.getMinecraft();
        this.manager.getShaderUniformOrDefault("ScreenSize").set((float)minecraft.displayWidth, (float)minecraft.displayHeight);
        this.manager.useShader();
        this.framebufferOut.framebufferClear();
        this.framebufferOut.bindFramebuffer(false);
        GlStateManager.depthMask(false);
        GlStateManager.colorMask(true, true, true, true);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(0.0D, (double)framebufferHeight, 500.0D).color(255, 255, 255, 255).endVertex();
        worldRenderer.pos((double)framebufferWidth, (double)framebufferHeight, 500.0D).color(255, 255, 255, 255).endVertex();
        worldRenderer.pos((double)framebufferWidth, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
        worldRenderer.pos(0.0D, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
        this.manager.endShader();
        this.framebufferOut.unbindFramebuffer();
        this.framebufferIn.unbindFramebufferTexture();

        for (Object object : this.listAuxFramebuffers)
        {
            if (object instanceof Framebuffer)
            {
                ((Framebuffer)object).unbindFramebufferTexture();
            }
        }
    }

    public ShaderManager getShaderManager()
    {
        return this.manager;
    }
}
