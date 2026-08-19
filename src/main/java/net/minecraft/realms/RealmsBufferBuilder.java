package net.minecraft.realms;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class RealmsBufferBuilder
{
    private WorldRenderer worldRenderer;

    public RealmsBufferBuilder(WorldRenderer worldRenderer)
    {
        this.worldRenderer = worldRenderer;
    }

    public RealmsBufferBuilder from(WorldRenderer worldRenderer)
    {
        this.worldRenderer = worldRenderer;
        return this;
    }

    public void sortQuads(float x, float y, float z)
    {
        this.worldRenderer.sortVertexData(x, y, z);
    }

    public void fixupQuadColor(int color)
    {
        this.worldRenderer.putColor4(color);
    }

    public ByteBuffer getBuffer()
    {
        return this.worldRenderer.getByteBuffer();
    }

    public void postNormal(float x, float y, float z)
    {
        this.worldRenderer.putNormal(x, y, z);
    }

    public int getDrawMode()
    {
        return this.worldRenderer.getDrawMode();
    }

    public void offset(double x, double y, double z)
    {
        this.worldRenderer.setTranslation(x, y, z);
    }

    public void restoreState(WorldRenderer.State state)
    {
        this.worldRenderer.setVertexState(state);
    }

    public void endVertex()
    {
        this.worldRenderer.endVertex();
    }

    public RealmsBufferBuilder normal(float x, float y, float z)
    {
        return this.from(this.worldRenderer.normal(x, y, z));
    }

    public void end()
    {
        this.worldRenderer.finishDrawing();
    }

    public void begin(int drawMode, VertexFormat vertexFormat)
    {
        this.worldRenderer.begin(drawMode, vertexFormat);
    }

    public RealmsBufferBuilder color(int red, int green, int blue, int alpha)
    {
        return this.from(this.worldRenderer.color(red, green, blue, alpha));
    }

    public void faceTex2(int brightness1, int brightness2, int brightness3, int brightness4)
    {
        this.worldRenderer.putBrightness4(brightness1, brightness2, brightness3, brightness4);
    }

    public void postProcessFacePosition(double x, double y, double z)
    {
        this.worldRenderer.putPosition(x, y, z);
    }

    public void fixupVertexColor(float red, float green, float blue, int vertexIndex)
    {
        this.worldRenderer.putColorRGB_F(red, green, blue, vertexIndex);
    }

    public RealmsBufferBuilder color(float red, float green, float blue, float alpha)
    {
        return this.from(this.worldRenderer.color(red, green, blue, alpha));
    }

    public RealmsVertexFormat getVertexFormat()
    {
        return new RealmsVertexFormat(this.worldRenderer.getVertexFormat());
    }

    public void faceTint(float red, float green, float blue, int vertexIndex)
    {
        this.worldRenderer.putColorMultiplier(red, green, blue, vertexIndex);
    }

    public RealmsBufferBuilder tex2(int skyLight, int blockLight)
    {
        return this.from(this.worldRenderer.lightmap(skyLight, blockLight));
    }

    public void putBulkData(int[] vertexData)
    {
        this.worldRenderer.addVertexData(vertexData);
    }

    public RealmsBufferBuilder tex(double u, double v)
    {
        return this.from(this.worldRenderer.tex(u, v));
    }

    public int getVertexCount()
    {
        return this.worldRenderer.getVertexCount();
    }

    public void clear()
    {
        this.worldRenderer.reset();
    }

    public RealmsBufferBuilder vertex(double x, double y, double z)
    {
        return this.from(this.worldRenderer.pos(x, y, z));
    }

    public void fixupQuadColor(float red, float green, float blue)
    {
        this.worldRenderer.putColorRGB_F4(red, green, blue);
    }

    public void noColor()
    {
        this.worldRenderer.noColor();
    }
}
