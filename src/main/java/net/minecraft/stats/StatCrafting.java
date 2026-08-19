package net.minecraft.stats;

import net.minecraft.item.Item;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.util.IChatComponent;

public class StatCrafting extends StatBase
{
    private final Item item;

    public StatCrafting(String statPrefix, String itemName, IChatComponent statNameIn, Item itemIn)
    {
        super(statPrefix + itemName, statNameIn);
        this.item = itemIn;
        int i = Item.getIdFromItem(itemIn);

        if (i != 0)
        {
            IScoreObjectiveCriteria.INSTANCES.put(statPrefix + i, this.getCriteria());
        }
    }

    public Item getItem()
    {
        return this.item;
    }
}
