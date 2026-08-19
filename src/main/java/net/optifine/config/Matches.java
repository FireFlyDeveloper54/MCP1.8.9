package net.optifine.config;

import net.minecraft.block.state.BlockStateBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.biome.BiomeGenBase;

public class Matches
{
    public static boolean block(BlockStateBase blockStateBase, MatchBlock[] matchBlocks)
    {
        if (matchBlocks == null)
        {
            return true;
        }
        else
        {
            for (int matchBlockIndex = 0; matchBlockIndex < matchBlocks.length; ++matchBlockIndex)
            {
                MatchBlock matchBlock = matchBlocks[matchBlockIndex];

                if (matchBlock.matches(blockStateBase))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean block(int blockId, int metadata, MatchBlock[] matchBlocks)
    {
        if (matchBlocks == null)
        {
            return true;
        }
        else
        {
            for (int matchBlockIndex = 0; matchBlockIndex < matchBlocks.length; ++matchBlockIndex)
            {
                MatchBlock matchBlock = matchBlocks[matchBlockIndex];

                if (matchBlock.matches(blockId, metadata))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean blockId(int blockId, MatchBlock[] matchBlocks)
    {
        if (matchBlocks == null)
        {
            return true;
        }
        else
        {
            for (int matchBlockIndex = 0; matchBlockIndex < matchBlocks.length; ++matchBlockIndex)
            {
                MatchBlock matchBlock = matchBlocks[matchBlockIndex];

                if (matchBlock.getBlockId() == blockId)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean metadata(int metadata, int[] metadatas)
    {
        if (metadatas == null)
        {
            return true;
        }
        else
        {
            for (int metadataIndex = 0; metadataIndex < metadatas.length; ++metadataIndex)
            {
                if (metadatas[metadataIndex] == metadata)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean sprite(TextureAtlasSprite sprite, TextureAtlasSprite[] sprites)
    {
        if (sprites == null)
        {
            return true;
        }
        else
        {
            for (int spriteIndex = 0; spriteIndex < sprites.length; ++spriteIndex)
            {
                if (sprites[spriteIndex] == sprite)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean biome(BiomeGenBase biome, BiomeGenBase[] biomes)
    {
        if (biomes == null)
        {
            return true;
        }
        else
        {
            for (int biomeIndex = 0; biomeIndex < biomes.length; ++biomeIndex)
            {
                if (biomes[biomeIndex] == biome)
                {
                    return true;
                }
            }

            return false;
        }
    }
}
