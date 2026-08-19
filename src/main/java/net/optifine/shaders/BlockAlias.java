package net.optifine.shaders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.src.Config;
import net.optifine.config.MatchBlock;

public class BlockAlias
{
    private int blockAliasId;
    private MatchBlock[] matchBlocks;

    public BlockAlias(int blockAliasId, MatchBlock[] matchBlocks)
    {
        this.blockAliasId = blockAliasId;
        this.matchBlocks = matchBlocks;
    }

    public int getBlockAliasId()
    {
        return this.blockAliasId;
    }

    public boolean matches(int id, int metadata)
    {
        for (int matchBlockIndex = 0; matchBlockIndex < this.matchBlocks.length; ++matchBlockIndex)
        {
            MatchBlock matchBlock = this.matchBlocks[matchBlockIndex];

            if (matchBlock.matches(id, metadata))
            {
                return true;
            }
        }

        return false;
    }

    public int[] getMatchBlockIds()
    {
        Set<Integer> blockIdSet = new HashSet();

        for (int matchBlockIndex = 0; matchBlockIndex < this.matchBlocks.length; ++matchBlockIndex)
        {
            MatchBlock matchBlock = this.matchBlocks[matchBlockIndex];
            int blockId = matchBlock.getBlockId();
            blockIdSet.add(Integer.valueOf(blockId));
        }

        Integer[] blockIdObjects = (Integer[])blockIdSet.toArray(new Integer[blockIdSet.size()]);
        int[] blockIds = Config.toPrimitive(blockIdObjects);
        return blockIds;
    }

    public MatchBlock[] getMatchBlocks(int matchBlockId)
    {
        List<MatchBlock> matchingBlocks = new ArrayList();

        for (int matchBlockIndex = 0; matchBlockIndex < this.matchBlocks.length; ++matchBlockIndex)
        {
            MatchBlock matchBlock = this.matchBlocks[matchBlockIndex];

            if (matchBlock.getBlockId() == matchBlockId)
            {
                matchingBlocks.add(matchBlock);
            }
        }

        MatchBlock[] matchingBlockArray = (MatchBlock[])((MatchBlock[])matchingBlocks.toArray(new MatchBlock[matchingBlocks.size()]));
        return matchingBlockArray;
    }

    public String toString()
    {
        return "block." + this.blockAliasId + "=" + Config.arrayToString((Object[])this.matchBlocks);
    }
}
