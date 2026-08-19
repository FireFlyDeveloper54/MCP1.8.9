package net.minecraft.client.renderer.block.statemap;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class BlockStateMapper
{
    private Map<Block, IStateMapper> blockStateMap = Maps.<Block, IStateMapper>newIdentityHashMap();
    private Set<Block> setBuiltInBlocks = Sets.<Block>newIdentityHashSet();

    public void registerBlockStateMapper(Block block, IStateMapper stateMapper)
    {
        this.blockStateMap.put(block, stateMapper);
    }

    public void registerBuiltInBlocks(Block... blocks)
    {
        Collections.addAll(this.setBuiltInBlocks, blocks);
    }

    public Map<IBlockState, ModelResourceLocation> putAllStateModelLocations()
    {
        Map<IBlockState, ModelResourceLocation> map = Maps.<IBlockState, ModelResourceLocation>newIdentityHashMap();

        for (Block block : Block.blockRegistry)
        {
            if (!this.setBuiltInBlocks.contains(block))
            {
                map.putAll(((IStateMapper)MoreObjects.firstNonNull(this.blockStateMap.get(block), new DefaultStateMapper())).putStateModelLocations(block));
            }
        }

        return map;
    }
}
