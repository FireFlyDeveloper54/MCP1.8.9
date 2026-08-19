package net.optifine;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.src.Config;
import net.minecraft.util.EnumWorldBlockLayer;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.shaders.BlockAliases;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;

public class CustomBlockLayers
{
    private static EnumWorldBlockLayer[] renderLayers = null;
    public static boolean active = false;

    public static EnumWorldBlockLayer getRenderLayer(IBlockState blockState)
    {
        if (renderLayers == null)
        {
            return null;
        }
        else if (blockState.getBlock().isOpaqueCube())
        {
            return null;
        }
        else if (!(blockState instanceof BlockStateBase))
        {
            return null;
        }
        else
        {
            BlockStateBase blockStateBase = (BlockStateBase)blockState;
            int blockId = blockStateBase.getBlockId();
            return blockId > 0 && blockId < renderLayers.length ? renderLayers[blockId] : null;
        }
    }

    public static void update()
    {
        renderLayers = null;
        active = false;
        List<EnumWorldBlockLayer> blockLayers = new ArrayList();
        String blockPropertiesPath = "optifine/block.properties";
        Properties properties = ResUtils.readProperties(blockPropertiesPath, "CustomBlockLayers");

        if (properties != null)
        {
            readLayers(blockPropertiesPath, properties, blockLayers);
        }

        if (Config.isShaders())
        {
            PropertiesOrdered propertiesOrdered = BlockAliases.getBlockLayerPropertes();

            if (propertiesOrdered != null)
            {
                String shaderPropertiesPath = "shaders/block.properties";
                readLayers(shaderPropertiesPath, propertiesOrdered, blockLayers);
            }
        }

        if (!((List)blockLayers).isEmpty())
        {
            renderLayers = (EnumWorldBlockLayer[])blockLayers.toArray(new EnumWorldBlockLayer[blockLayers.size()]);
            active = true;
        }
    }

    private static void readLayers(String pathProps, Properties props, List<EnumWorldBlockLayer> list)
    {
        Config.dbg("CustomBlockLayers: " + pathProps);
        readLayer("solid", EnumWorldBlockLayer.SOLID, props, list);
        readLayer("cutout", EnumWorldBlockLayer.CUTOUT, props, list);
        readLayer("cutout_mipped", EnumWorldBlockLayer.CUTOUT_MIPPED, props, list);
        readLayer("translucent", EnumWorldBlockLayer.TRANSLUCENT, props, list);
    }

    private static void readLayer(String name, EnumWorldBlockLayer layer, Properties props, List<EnumWorldBlockLayer> listLayers)
    {
        String propertyKey = "layer." + name;
        String propertyValue = props.getProperty(propertyKey);

        if (propertyValue != null)
        {
            ConnectedParser connectedParser = new ConnectedParser("CustomBlockLayers");
            MatchBlock[] matchBlocks = connectedParser.parseMatchBlocks(propertyValue);

            if (matchBlocks != null)
            {
                for (int matchBlockIndex = 0; matchBlockIndex < matchBlocks.length; ++matchBlockIndex)
                {
                    MatchBlock matchBlock = matchBlocks[matchBlockIndex];
                    int blockId = matchBlock.getBlockId();

                    if (blockId > 0)
                    {
                        while (listLayers.size() < blockId + 1)
                        {
                            listLayers.add(null);
                        }

                        if (listLayers.get(blockId) != null)
                        {
                            Config.warn("CustomBlockLayers: Block layer is already set, block: " + blockId + ", layer: " + name);
                        }

                        listLayers.set(blockId, layer);
                    }
                }
            }
        }
    }

    public static boolean isActive()
    {
        return active;
    }
}
