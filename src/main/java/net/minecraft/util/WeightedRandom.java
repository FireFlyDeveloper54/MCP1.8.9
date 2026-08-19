package net.minecraft.util;

import java.util.Collection;
import java.util.Random;

public class WeightedRandom
{
    public static int getTotalWeight(Collection <? extends WeightedRandom.Item > collection)
    {
        int totalWeight = 0;

        for (WeightedRandom.Item weightedrandom$item : collection)
        {
            totalWeight += weightedrandom$item.itemWeight;
        }

        return totalWeight;
    }

    public static <T extends WeightedRandom.Item> T getRandomItem(Random random, Collection<T> collection, int totalWeight)
    {
        if (totalWeight <= 0)
        {
            throw new IllegalArgumentException();
        }
        else
        {
            int selectedWeight = random.nextInt(totalWeight);
            return getRandomItem(collection, selectedWeight);
        }
    }

    public static <T extends WeightedRandom.Item> T getRandomItem(Collection<T> collection, int weight)
    {
        for (T t : collection)
        {
            weight -= t.itemWeight;

            if (weight < 0)
            {
                return t;
            }
        }

        return (T)null;
    }

    public static <T extends WeightedRandom.Item> T getRandomItem(Random random, Collection<T> collection)
    {
        return getRandomItem(random, collection, getTotalWeight(collection));
    }

    public static class Item
    {
        protected int itemWeight;

        public Item(int itemWeightIn)
        {
            this.itemWeight = itemWeightIn;
        }
    }
}
