package net.minecraft.client.renderer.vertex;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.CorePipeline;
import net.minecraft.client.renderer.OpenGlHelper;
import net.optifine.render.VboRange;
import net.optifine.render.VboRegion;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class VertexBuffer
{
    private int glBufferId;
    private int vertexArrayId;
    private boolean vertexArrayDirty = true;
    private final VertexFormat vertexFormat;
    private int count;
    private VboRegion vboRegion;
    private VboRange vboRange;
    private int drawMode;

    public VertexBuffer(VertexFormat vertexFormatIn)
    {
        this.vertexFormat = vertexFormatIn;
        this.glBufferId = OpenGlHelper.glGenBuffers();
    }

    public void bindBuffer()
    {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, this.glBufferId);
    }

    public void bufferData(ByteBuffer byteBuffer)
    {
        if (this.vboRegion != null)
        {
            this.vboRegion.bufferData(byteBuffer, this.vboRange);
        }
        else
        {
            this.bindBuffer();
            OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, byteBuffer, 35044);
            this.unbindBuffer();
            this.count = byteBuffer.limit() / this.vertexFormat.getNextOffset();
            this.vertexArrayDirty = true;
        }
    }

    public void drawArrays(int mode)
    {
        if (this.drawMode > 0)
        {
            mode = this.drawMode;
        }

        if (this.vboRegion != null)
        {
            this.vboRegion.drawArrays(mode, this.vboRange);
        }
        else
        {
            net.minecraft.client.renderer.GlStateManager.glDrawArrays(mode, 0, this.count);
        }
    }

    public void unbindBuffer()
    {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
    }

    public void bindDrawState()
    {
        if (this.vboRegion != null)
        {
            return;
        }

        if (this.vertexArrayId == 0)
        {
            this.vertexArrayId = GL30.glGenVertexArrays();
            this.vertexArrayDirty = true;
        }

        OpenGlHelper.bindVertexArray(this.vertexArrayId);

        if (this.vertexArrayDirty)
        {
            this.bindBuffer();
            CorePipeline.bindFormat(this.vertexFormat, 0L);
            CorePipeline.bindQuadIndexBuffer();
            this.vertexArrayDirty = false;
        }
    }

    public void deleteGlBuffers()
    {
        if (this.vertexArrayId != 0)
        {
            OpenGlHelper.invalidateVertexArray(this.vertexArrayId);
            GL30.glDeleteVertexArrays(this.vertexArrayId);
            this.vertexArrayId = 0;
            this.vertexArrayDirty = true;
        }

        if (this.glBufferId >= 0)
        {
            OpenGlHelper.glDeleteBuffers(this.glBufferId);
            this.glBufferId = -1;
        }
    }

    public void setVboRegion(VboRegion vboRegionIn)
    {
        if (vboRegionIn != null)
        {
            this.deleteGlBuffers();
            this.vboRegion = vboRegionIn;
            this.vboRange = new VboRange();
        }
    }

    public VboRegion getVboRegion()
    {
        return this.vboRegion;
    }

    public VboRange getVboRange()
    {
        return this.vboRange;
    }

    public int getDrawMode()
    {
        return this.drawMode;
    }

    public void setDrawMode(int drawModeIn)
    {
        this.drawMode = drawModeIn;
    }
}
