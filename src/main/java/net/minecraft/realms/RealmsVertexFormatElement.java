package net.minecraft.realms;

import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class RealmsVertexFormatElement
{
    private VertexFormatElement vertexFormatElement;

    public RealmsVertexFormatElement(VertexFormatElement vIn)
    {
        this.vertexFormatElement = vIn;
    }

    public VertexFormatElement getVertexFormatElement()
    {
        return this.vertexFormatElement;
    }

    public boolean isPosition()
    {
        return this.vertexFormatElement.isPositionElement();
    }

    public int getIndex()
    {
        return this.vertexFormatElement.getIndex();
    }

    public int getByteSize()
    {
        return this.vertexFormatElement.getSize();
    }

    public int getCount()
    {
        return this.vertexFormatElement.getElementCount();
    }

    public int hashCode()
    {
        return this.vertexFormatElement.hashCode();
    }

    public boolean equals(Object other)
    {
        return this.vertexFormatElement.equals(other);
    }

    public String toString()
    {
        return this.vertexFormatElement.toString();
    }
}
