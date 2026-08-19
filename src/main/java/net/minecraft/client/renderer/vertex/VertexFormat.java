package net.minecraft.client.renderer.vertex;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VertexFormat
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<VertexFormatElement> elements;
    private final List<Integer> offsets;
    private int nextOffset;
    private int colorElementOffset;
    private List<Integer> uvOffsetsById;
    private int normalElementOffset;

    public VertexFormat(VertexFormat vertexFormatIn)
    {
        this();

        for (int elementIndex = 0; elementIndex < vertexFormatIn.getElementCount(); ++elementIndex)
        {
            this.addElement(vertexFormatIn.getElement(elementIndex));
        }

        this.nextOffset = vertexFormatIn.getNextOffset();
    }

    public VertexFormat()
    {
        this.elements = Lists.<VertexFormatElement>newArrayList();
        this.offsets = Lists.<Integer>newArrayList();
        this.nextOffset = 0;
        this.colorElementOffset = -1;
        this.uvOffsetsById = Lists.<Integer>newArrayList();
        this.normalElementOffset = -1;
    }

    public void clear()
    {
        this.elements.clear();
        this.offsets.clear();
        this.colorElementOffset = -1;
        this.uvOffsetsById.clear();
        this.normalElementOffset = -1;
        this.nextOffset = 0;
    }

    @SuppressWarnings("incomplete-switch")
    public VertexFormat addElement(VertexFormatElement element)
    {
        if (element.isPositionElement() && this.hasPosition())
        {
            LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
            return this;
        }
        else
        {
            this.elements.add(element);
            this.offsets.add(Integer.valueOf(this.nextOffset));

            switch (element.getUsage())
            {
                case NORMAL:
                    this.normalElementOffset = this.nextOffset;
                    break;

                case COLOR:
                    this.colorElementOffset = this.nextOffset;
                    break;

                case UV:
                    this.uvOffsetsById.add(element.getIndex(), Integer.valueOf(this.nextOffset));
            }

            this.nextOffset += element.getSize();
            return this;
        }
    }

    public boolean hasNormal()
    {
        return this.normalElementOffset >= 0;
    }

    public int getNormalOffset()
    {
        return this.normalElementOffset;
    }

    public boolean hasColor()
    {
        return this.colorElementOffset >= 0;
    }

    public int getColorOffset()
    {
        return this.colorElementOffset;
    }

    public boolean hasUvOffset(int id)
    {
        return this.uvOffsetsById.size() - 1 >= id;
    }

    public int getUvOffsetById(int id)
    {
        return this.uvOffsetsById.get(id).intValue();
    }

    public String toString()
    {
        String description = "format: " + this.elements.size() + " elements: ";

        for (int elementIndex = 0; elementIndex < this.elements.size(); ++elementIndex)
        {
            description = description + this.elements.get(elementIndex).toString();

            if (elementIndex != this.elements.size() - 1)
            {
                description = description + " ";
            }
        }

        return description;
    }

    private boolean hasPosition()
    {
        int elementIndex = 0;

        for (int elementCount = this.elements.size(); elementIndex < elementCount; ++elementIndex)
        {
            VertexFormatElement vertexFormatElement = this.elements.get(elementIndex);

            if (vertexFormatElement.isPositionElement())
            {
                return true;
            }
        }

        return false;
    }

    public int getIntegerSize()
    {
        return this.getNextOffset() / 4;
    }

    public int getNextOffset()
    {
        return this.nextOffset;
    }

    public List<VertexFormatElement> getElements()
    {
        return this.elements;
    }

    public int getElementCount()
    {
        return this.elements.size();
    }

    public VertexFormatElement getElement(int index)
    {
        return this.elements.get(index);
    }

    public int getOffset(int index)
    {
        return this.offsets.get(index).intValue();
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other != null && this.getClass() == other.getClass())
        {
            VertexFormat vertexFormat = (VertexFormat)other;
            return this.nextOffset != vertexFormat.nextOffset ? false : (!this.elements.equals(vertexFormat.elements) ? false : this.offsets.equals(vertexFormat.offsets));
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int hash = this.elements.hashCode();
        hash = 31 * hash + this.offsets.hashCode();
        hash = 31 * hash + this.nextOffset;
        return hash;
    }
}
