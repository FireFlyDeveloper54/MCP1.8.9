package net.optifine.util;

import java.util.ArrayList;

public class CompactArrayList
{
    private ArrayList list;
    private int initialCapacity;
    private float loadFactor;
    private int countValid;

    public CompactArrayList()
    {
        this(10, 0.75F);
    }

    public CompactArrayList(int initialCapacity)
    {
        this(initialCapacity, 0.75F);
    }

    public CompactArrayList(int initialCapacity, float loadFactor)
    {
        this.list = null;
        this.initialCapacity = 0;
        this.loadFactor = 1.0F;
        this.countValid = 0;
        this.list = new ArrayList(initialCapacity);
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
    }

    public void add(int index, Object element)
    {
        if (element != null)
        {
            ++this.countValid;
        }

        this.list.add(index, element);
    }

    public boolean add(Object element)
    {
        if (element != null)
        {
            ++this.countValid;
        }

        return this.list.add(element);
    }

    public Object set(int index, Object element)
    {
        Object previousElement = this.list.set(index, element);

        if (element != previousElement)
        {
            if (previousElement == null)
            {
                ++this.countValid;
            }

            if (element == null)
            {
                --this.countValid;
            }
        }

        return previousElement;
    }

    public Object remove(int index)
    {
        Object removedElement = this.list.remove(index);

        if (removedElement != null)
        {
            --this.countValid;
        }

        return removedElement;
    }

    public void clear()
    {
        this.list.clear();
        this.countValid = 0;
    }

    public void compact()
    {
        if (this.countValid <= 0 && this.list.size() <= 0)
        {
            this.clear();
        }
        else if (this.list.size() > this.initialCapacity)
        {
            float compactLoadFactor = (float)this.countValid * 1.0F / (float)this.list.size();

            if (compactLoadFactor <= this.loadFactor)
            {
                int targetIndex = 0;

                for (int sourceIndex = 0; sourceIndex < this.list.size(); ++sourceIndex)
                {
                    Object element = this.list.get(sourceIndex);

                    if (element != null)
                    {
                        if (sourceIndex != targetIndex)
                        {
                            this.list.set(targetIndex, element);
                        }

                        ++targetIndex;
                    }
                }

                for (int removeIndex = this.list.size() - 1; removeIndex >= targetIndex; --removeIndex)
                {
                    this.list.remove(removeIndex);
                }
            }
        }
    }

    public boolean contains(Object elem)
    {
        return this.list.contains(elem);
    }

    public Object get(int index)
    {
        return this.list.get(index);
    }

    public boolean isEmpty()
    {
        return this.list.isEmpty();
    }

    public int size()
    {
        return this.list.size();
    }

    public int getCountValid()
    {
        return this.countValid;
    }
}
