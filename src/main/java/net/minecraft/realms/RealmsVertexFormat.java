package net.minecraft.realms;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class RealmsVertexFormat
{
    private VertexFormat vertexFormat;

    public RealmsVertexFormat(VertexFormat vIn)
    {
        this.vertexFormat = vIn;
    }

    public RealmsVertexFormat from(VertexFormat vertexFormat)
    {
        this.vertexFormat = vertexFormat;
        return this;
    }

    public VertexFormat getVertexFormat()
    {
        return this.vertexFormat;
    }

    public void clear()
    {
        this.vertexFormat.clear();
    }

    public int getUvOffset(int id)
    {
        return this.vertexFormat.getUvOffsetById(id);
    }

    public int getElementCount()
    {
        return this.vertexFormat.getElementCount();
    }

    public boolean hasColor()
    {
        return this.vertexFormat.hasColor();
    }

    public boolean hasUv(int id)
    {
        return this.vertexFormat.hasUvOffset(id);
    }

    public RealmsVertexFormatElement getElement(int index)
    {
        return new RealmsVertexFormatElement(this.vertexFormat.getElement(index));
    }

    public RealmsVertexFormat addElement(RealmsVertexFormatElement element)
    {
        return this.from(this.vertexFormat.addElement(element.getVertexFormatElement()));
    }

    public int getColorOffset()
    {
        return this.vertexFormat.getColorOffset();
    }

    public List<RealmsVertexFormatElement> getElements()
    {
        List<RealmsVertexFormatElement> list = new ArrayList();

        for (VertexFormatElement vertexformatelement : this.vertexFormat.getElements())
        {
            list.add(new RealmsVertexFormatElement(vertexformatelement));
        }

        return list;
    }

    public boolean hasNormal()
    {
        return this.vertexFormat.hasNormal();
    }

    public int getVertexSize()
    {
        return this.vertexFormat.getNextOffset();
    }

    public int getOffset(int index)
    {
        return this.vertexFormat.getOffset(index);
    }

    public int getNormalOffset()
    {
        return this.vertexFormat.getNormalOffset();
    }

    public int getIntegerSize()
    {
        return this.vertexFormat.getIntegerSize();
    }

    public boolean equals(Object other)
    {
        return this.vertexFormat.equals(other);
    }

    public int hashCode()
    {
        return this.vertexFormat.hashCode();
    }

    public String toString()
    {
        return this.vertexFormat.toString();
    }
}
