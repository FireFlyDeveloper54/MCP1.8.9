package net.optifine.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class IteratorCache
{
    private static Deque<IteratorCache.IteratorReusable<Object>> dequeIterators = new ArrayDeque();

    public static Iterator<Object> getReadOnly(List list)
    {
        synchronized (dequeIterators)
        {
            IteratorCache.IteratorReusable<Object> iteratorReusable = (IteratorCache.IteratorReusable)dequeIterators.pollFirst();

            if (iteratorReusable == null)
            {
                iteratorReusable = new IteratorCache.IteratorReadOnly();
            }

            iteratorReusable.setList(list);
            return iteratorReusable;
        }
    }

    private static void finished(IteratorCache.IteratorReusable<Object> iterator)
    {
        synchronized (dequeIterators)
        {
            if (dequeIterators.size() <= 1000)
            {
                iterator.setList(null);
                dequeIterators.addLast(iterator);
            }
        }
    }

    static
    {
        for (int iteratorIndex = 0; iteratorIndex < 1000; ++iteratorIndex)
        {
            IteratorCache.IteratorReadOnly readOnlyIterator = new IteratorCache.IteratorReadOnly();
            dequeIterators.add(readOnlyIterator);
        }
    }

    public static class IteratorReadOnly implements IteratorCache.IteratorReusable<Object>
    {
        private List<Object> list;
        private int index;
        private boolean hasNext;

        public void setList(List<Object> list)
        {
            if (this.hasNext)
            {
                throw new RuntimeException("Iterator still used, oldList: " + this.list + ", newList: " + list);
            }
            else
            {
                this.list = list;
                this.index = 0;
                this.hasNext = list != null && this.index < list.size();
            }
        }

        public Object next()
        {
            if (!this.hasNext)
            {
                return null;
            }
            else
            {
                Object object = this.list.get(this.index);
                ++this.index;
                this.hasNext = this.index < this.list.size();
                return object;
            }
        }

        public boolean hasNext()
        {
            if (!this.hasNext)
            {
                IteratorCache.finished(this);
                return false;
            }
            else
            {
                return this.hasNext;
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException("remove");
        }
    }

    public interface IteratorReusable<E> extends Iterator<E>
    {
        void setList(List<E> list);
    }
}
