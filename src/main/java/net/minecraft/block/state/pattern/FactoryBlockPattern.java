package net.minecraft.block.state.pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.state.BlockWorldState;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class FactoryBlockPattern
{
    private static final Joiner COMMA_JOIN = Joiner.on(",");
    private final List<String[]> layers = Lists.<String[]>newArrayList();
    private final Map<Character, Predicate<BlockWorldState>> symbolMap = Maps.<Character, Predicate<BlockWorldState>>newHashMap();
    private int aisleHeight;
    private int rowWidth;

    private FactoryBlockPattern()
    {
        this.symbolMap.put(' ', Predicates.<BlockWorldState>alwaysTrue());
    }

    public FactoryBlockPattern aisle(String... layerRows)
    {
        if (!ArrayUtils.isEmpty((Object[])layerRows) && !StringUtils.isEmpty(layerRows[0]))
        {
            if (this.layers.isEmpty())
            {
                this.aisleHeight = layerRows.length;
                this.rowWidth = layerRows[0].length();
            }

            if (layerRows.length != this.aisleHeight)
            {
                throw new IllegalArgumentException("Expected aisle with height of " + this.aisleHeight + ", but was given one with a height of " + layerRows.length + ")");
            }
            else
            {
                for (String row : layerRows)
                {
                    if (row.length() != this.rowWidth)
                    {
                        throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.rowWidth + ", found one with " + row.length() + ")");
                    }

                    for (char symbol : row.toCharArray())
                    {
                        if (!this.symbolMap.containsKey(Character.valueOf(symbol)))
                        {
                            this.symbolMap.put(Character.valueOf(symbol), (Predicate<BlockWorldState>)null);
                        }
                    }
                }

                this.layers.add(layerRows);
                return this;
            }
        }
        else
        {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
    }

    public static FactoryBlockPattern start()
    {
        return new FactoryBlockPattern();
    }

    public FactoryBlockPattern where(char symbol, Predicate<BlockWorldState> blockMatcher)
    {
        this.symbolMap.put(Character.valueOf(symbol), blockMatcher);
        return this;
    }

    public BlockPattern build()
    {
        return new BlockPattern(this.makePredicateArray());
    }

    private Predicate<BlockWorldState>[][][] makePredicateArray()
    {
        this.checkMissingPredicates();
        Predicate<BlockWorldState>[][][] predicate = (Predicate[][][])((Predicate[][][])Array.newInstance(Predicate.class, new int[] {this.layers.size(), this.aisleHeight, this.rowWidth}));

        for (int layerIndex = 0; layerIndex < this.layers.size(); ++layerIndex)
        {
            for (int rowIndex = 0; rowIndex < this.aisleHeight; ++rowIndex)
            {
                for (int columnIndex = 0; columnIndex < this.rowWidth; ++columnIndex)
                {
                    predicate[layerIndex][rowIndex][columnIndex] = (Predicate)this.symbolMap.get(Character.valueOf(((String[])this.layers.get(layerIndex))[rowIndex].charAt(columnIndex)));
                }
            }
        }

        return predicate;
    }

    private void checkMissingPredicates()
    {
        List<Character> list = Lists.<Character>newArrayList();

        for (Entry<Character, Predicate<BlockWorldState>> entry : this.symbolMap.entrySet())
        {
            if (entry.getValue() == null)
            {
                list.add(entry.getKey());
            }
        }

        if (!list.isEmpty())
        {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOIN.join(list) + " are missing");
        }
    }
}
