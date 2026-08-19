package net.optifine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.Matches;
import net.optifine.model.BlockModelUtils;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.render.RenderEnv;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.TileEntityUtils;

public class ConnectedTextures
{
    private static Map[] spriteQuadMaps = null;
    private static Map[] spriteQuadFullMaps = null;
    private static Map[][] spriteQuadCompactMaps = (Map[][])null;
    private static ConnectedProperties[][] blockProperties = (ConnectedProperties[][])null;
    private static ConnectedProperties[][] tileProperties = (ConnectedProperties[][])null;
    private static boolean multipass = false;
    protected static final int UNKNOWN = -1;
    protected static final int Y_NEG_DOWN = 0;
    protected static final int Y_POS_UP = 1;
    protected static final int Z_NEG_NORTH = 2;
    protected static final int Z_POS_SOUTH = 3;
    protected static final int X_NEG_WEST = 4;
    protected static final int X_POS_EAST = 5;
    private static final int Y_AXIS = 0;
    private static final int Z_AXIS = 1;
    private static final int X_AXIS = 2;
    public static final IBlockState AIR_DEFAULT_STATE = Blocks.air.getDefaultState();
    private static TextureAtlasSprite emptySprite = null;
    private static final BlockDir[] SIDES_Y_NEG_DOWN = new BlockDir[] {BlockDir.WEST, BlockDir.EAST, BlockDir.NORTH, BlockDir.SOUTH};
    private static final BlockDir[] SIDES_Y_POS_UP = new BlockDir[] {BlockDir.WEST, BlockDir.EAST, BlockDir.SOUTH, BlockDir.NORTH};
    private static final BlockDir[] SIDES_Z_NEG_NORTH = new BlockDir[] {BlockDir.EAST, BlockDir.WEST, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_Z_POS_SOUTH = new BlockDir[] {BlockDir.WEST, BlockDir.EAST, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_X_NEG_WEST = new BlockDir[] {BlockDir.NORTH, BlockDir.SOUTH, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_X_POS_EAST = new BlockDir[] {BlockDir.SOUTH, BlockDir.NORTH, BlockDir.DOWN, BlockDir.UP};
    private static final BlockDir[] SIDES_Z_NEG_NORTH_Z_AXIS = new BlockDir[] {BlockDir.WEST, BlockDir.EAST, BlockDir.UP, BlockDir.DOWN};
    private static final BlockDir[] SIDES_X_POS_EAST_X_AXIS = new BlockDir[] {BlockDir.NORTH, BlockDir.SOUTH, BlockDir.UP, BlockDir.DOWN};
    private static final BlockDir[] EDGES_Y_NEG_DOWN = new BlockDir[] {BlockDir.NORTH_EAST, BlockDir.NORTH_WEST, BlockDir.SOUTH_EAST, BlockDir.SOUTH_WEST};
    private static final BlockDir[] EDGES_Y_POS_UP = new BlockDir[] {BlockDir.SOUTH_EAST, BlockDir.SOUTH_WEST, BlockDir.NORTH_EAST, BlockDir.NORTH_WEST};
    private static final BlockDir[] EDGES_Z_NEG_NORTH = new BlockDir[] {BlockDir.DOWN_WEST, BlockDir.DOWN_EAST, BlockDir.UP_WEST, BlockDir.UP_EAST};
    private static final BlockDir[] EDGES_Z_POS_SOUTH = new BlockDir[] {BlockDir.DOWN_EAST, BlockDir.DOWN_WEST, BlockDir.UP_EAST, BlockDir.UP_WEST};
    private static final BlockDir[] EDGES_X_NEG_WEST = new BlockDir[] {BlockDir.DOWN_SOUTH, BlockDir.DOWN_NORTH, BlockDir.UP_SOUTH, BlockDir.UP_NORTH};
    private static final BlockDir[] EDGES_X_POS_EAST = new BlockDir[] {BlockDir.DOWN_NORTH, BlockDir.DOWN_SOUTH, BlockDir.UP_NORTH, BlockDir.UP_SOUTH};
    private static final BlockDir[] EDGES_Z_NEG_NORTH_Z_AXIS = new BlockDir[] {BlockDir.UP_EAST, BlockDir.UP_WEST, BlockDir.DOWN_EAST, BlockDir.DOWN_WEST};
    private static final BlockDir[] EDGES_X_POS_EAST_X_AXIS = new BlockDir[] {BlockDir.UP_SOUTH, BlockDir.UP_NORTH, BlockDir.DOWN_SOUTH, BlockDir.DOWN_NORTH};
    public static final TextureAtlasSprite SPRITE_DEFAULT = new TextureAtlasSprite("<default>");

    public static BakedQuad[] getConnectedTexture(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, BakedQuad quad, RenderEnv renderEnv)
    {
        TextureAtlasSprite sprite = quad.getSprite();

        if (sprite == null)
        {
            return renderEnv.getArrayQuadsCtm(quad);
        }
        else
        {
            Block block = blockState.getBlock();

            if (skipConnectedTexture(blockAccess, blockState, blockPos, quad, renderEnv))
            {
                quad = getQuad(emptySprite, quad);
                return renderEnv.getArrayQuadsCtm(quad);
            }
            else
            {
                EnumFacing facing = quad.getFace();
                BakedQuad[] connectedQuads = getConnectedTextureMultiPass(blockAccess, blockState, blockPos, facing, quad, renderEnv);
                return connectedQuads;
            }
        }
    }

    private static boolean skipConnectedTexture(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, BakedQuad quad, RenderEnv renderEnv)
    {
        Block block = blockState.getBlock();

        if (block instanceof BlockPane)
        {
            TextureAtlasSprite sprite = quad.getSprite();

            if (sprite.getIconName().startsWith("minecraft:blocks/glass_pane_top"))
            {
                IBlockState offsetState = blockAccess.getBlockState(blockPos.offset(quad.getFace()));
                return offsetState == blockState;
            }
        }

        if (block instanceof BlockPane)
        {
            EnumFacing facing = quad.getFace();

            if (facing != EnumFacing.UP && facing != EnumFacing.DOWN)
            {
                return false;
            }

            if (!quad.isFaceQuad())
            {
                return false;
            }

            BlockPos offsetPos = blockPos.offset(quad.getFace());
            IBlockState offsetState = blockAccess.getBlockState(offsetPos);

            if (offsetState.getBlock() != block)
            {
                return false;
            }

            if (block == Blocks.stained_glass_pane && offsetState.getValue(BlockStainedGlassPane.COLOR) != blockState.getValue(BlockStainedGlassPane.COLOR))
            {
                return false;
            }

            offsetState = offsetState.getBlock().getActualState(offsetState, blockAccess, offsetPos);
            double midX = (double)quad.getMidX();

            if (midX < 0.4D)
            {
                if (((Boolean)offsetState.getValue(BlockPane.WEST)).booleanValue())
                {
                    return true;
                }
            }
            else if (midX > 0.6D)
            {
                if (((Boolean)offsetState.getValue(BlockPane.EAST)).booleanValue())
                {
                    return true;
                }
            }
            else
            {
                double midZ = quad.getMidZ();

                if (midZ < 0.4D)
                {
                    if (((Boolean)offsetState.getValue(BlockPane.NORTH)).booleanValue())
                    {
                        return true;
                    }
                }
                else
                {
                    if (midZ <= 0.6D)
                    {
                        return true;
                    }

                    if (((Boolean)offsetState.getValue(BlockPane.SOUTH)).booleanValue())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected static BakedQuad[] getQuads(TextureAtlasSprite sprite, BakedQuad quadIn, RenderEnv renderEnv)
    {
        if (sprite == null)
        {
            return null;
        }
        else if (sprite == SPRITE_DEFAULT)
        {
            return renderEnv.getArrayQuadsCtm(quadIn);
        }
        else
        {
            BakedQuad bakedQuad = getQuad(sprite, quadIn);
            BakedQuad[] quads = renderEnv.getArrayQuadsCtm(bakedQuad);
            return quads;
        }
    }

    private static synchronized BakedQuad getQuad(TextureAtlasSprite sprite, BakedQuad quadIn)
    {
        if (spriteQuadMaps == null)
        {
            return quadIn;
        }
        else
        {
            int spriteIndex = sprite.getIndexInMap();

            if (spriteIndex >= 0 && spriteIndex < spriteQuadMaps.length)
            {
                Map quadMap = spriteQuadMaps[spriteIndex];

                if (quadMap == null)
                {
                    quadMap = new IdentityHashMap(1);
                    spriteQuadMaps[spriteIndex] = quadMap;
                }

                BakedQuad bakedQuad = (BakedQuad)quadMap.get(quadIn);

                if (bakedQuad == null)
                {
                    bakedQuad = makeSpriteQuad(quadIn, sprite);
                    quadMap.put(quadIn, bakedQuad);
                }

                return bakedQuad;
            }
            else
            {
                return quadIn;
            }
        }
    }

    private static synchronized BakedQuad getQuadFull(TextureAtlasSprite sprite, BakedQuad quadIn, int tintIndex)
    {
        if (spriteQuadFullMaps == null)
        {
            return null;
        }
        else if (sprite == null)
        {
            return null;
        }
        else
        {
            int spriteIndex = sprite.getIndexInMap();

            if (spriteIndex >= 0 && spriteIndex < spriteQuadFullMaps.length)
            {
                Map faceQuadMap = spriteQuadFullMaps[spriteIndex];

                if (faceQuadMap == null)
                {
                    faceQuadMap = new EnumMap(EnumFacing.class);
                    spriteQuadFullMaps[spriteIndex] = faceQuadMap;
                }

                EnumFacing facing = quadIn.getFace();
                BakedQuad bakedQuad = (BakedQuad)faceQuadMap.get(facing);

                if (bakedQuad == null)
                {
                    bakedQuad = BlockModelUtils.makeBakedQuad(facing, sprite, tintIndex);
                    faceQuadMap.put(facing, bakedQuad);
                }

                return bakedQuad;
            }
            else
            {
                return null;
            }
        }
    }

    private static BakedQuad makeSpriteQuad(BakedQuad quad, TextureAtlasSprite sprite)
    {
        int[] vertexData = (int[])quad.getVertexData().clone();
        TextureAtlasSprite sourceSprite = quad.getSprite();

        for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
        {
            fixVertex(vertexData, vertexIndex, sourceSprite, sprite);
        }

        BakedQuad bakedQuad = new BakedQuad(vertexData, quad.getTintIndex(), quad.getFace(), sprite);
        return bakedQuad;
    }

    private static void fixVertex(int[] data, int vertex, TextureAtlasSprite spriteFrom, TextureAtlasSprite spriteTo)
    {
        int vertexStride = data.length / 4;
        int vertexOffset = vertexStride * vertex;
        float vertexU = Float.intBitsToFloat(data[vertexOffset + 4]);
        float vertexV = Float.intBitsToFloat(data[vertexOffset + 4 + 1]);
        double spriteU16 = spriteFrom.getSpriteU16(vertexU);
        double spriteV16 = spriteFrom.getSpriteV16(vertexV);
        data[vertexOffset + 4] = Float.floatToRawIntBits(spriteTo.getInterpolatedU(spriteU16));
        data[vertexOffset + 4 + 1] = Float.floatToRawIntBits(spriteTo.getInterpolatedV(spriteV16));
    }

    private static BakedQuad[] getConnectedTextureMultiPass(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, EnumFacing side, BakedQuad quad, RenderEnv renderEnv)
    {
        BakedQuad[] connectedQuads = getConnectedTextureSingle(blockAccess, blockState, blockPos, side, quad, true, 0, renderEnv);

        if (!multipass)
        {
            return connectedQuads;
        }
        else if (connectedQuads.length == 1 && connectedQuads[0] == quad)
        {
            return connectedQuads;
        }
        else
        {
            List<BakedQuad> multipassQuads = renderEnv.getListQuadsCtmMultipass(connectedQuads);

            for (int quadIndex = 0; quadIndex < multipassQuads.size(); ++quadIndex)
            {
                BakedQuad originalQuad = (BakedQuad)multipassQuads.get(quadIndex);
                BakedQuad currentQuad = originalQuad;

                for (int passIndex = 0; passIndex < 3; ++passIndex)
                {
                    BakedQuad[] passQuads = getConnectedTextureSingle(blockAccess, blockState, blockPos, side, currentQuad, false, passIndex + 1, renderEnv);

                    if (passQuads.length != 1 || passQuads[0] == currentQuad)
                    {
                        break;
                    }

                    currentQuad = passQuads[0];
                }

                multipassQuads.set(quadIndex, currentQuad);
            }

            for (int quadIndex = 0; quadIndex < connectedQuads.length; ++quadIndex)
            {
                connectedQuads[quadIndex] = (BakedQuad)multipassQuads.get(quadIndex);
            }

            return connectedQuads;
        }
    }

    public static BakedQuad[] getConnectedTextureSingle(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, EnumFacing facing, BakedQuad quad, boolean checkBlocks, int pass, RenderEnv renderEnv)
    {
        Block block = blockState.getBlock();

        if (!(blockState instanceof BlockStateBase))
        {
            return renderEnv.getArrayQuadsCtm(quad);
        }
        else
        {
            BlockStateBase blockStateBase = (BlockStateBase)blockState;
            TextureAtlasSprite sprite = quad.getSprite();

            if (tileProperties != null)
            {
                int spriteIndex = sprite.getIndexInMap();

                if (spriteIndex >= 0 && spriteIndex < tileProperties.length)
                {
                    ConnectedProperties[] tileConnectedProperties = tileProperties[spriteIndex];

                    if (tileConnectedProperties != null)
                    {
                        int side = getSide(facing);

                        for (int propertyIndex = 0; propertyIndex < tileConnectedProperties.length; ++propertyIndex)
                        {
                            ConnectedProperties connectedProperties = tileConnectedProperties[propertyIndex];

                            if (connectedProperties != null && connectedProperties.matchesBlockId(blockStateBase.getBlockId()))
                            {
                                BakedQuad[] connectedQuads = getConnectedTexture(connectedProperties, blockAccess, blockStateBase, blockPos, side, quad, pass, renderEnv);

                                if (connectedQuads != null)
                                {
                                    return connectedQuads;
                                }
                            }
                        }
                    }
                }
            }

            if (blockProperties != null && checkBlocks)
            {
                int blockId = renderEnv.getBlockId();

                if (blockId >= 0 && blockId < blockProperties.length)
                {
                    ConnectedProperties[] blockConnectedProperties = blockProperties[blockId];

                    if (blockConnectedProperties != null)
                    {
                        int side = getSide(facing);

                        for (int propertyIndex = 0; propertyIndex < blockConnectedProperties.length; ++propertyIndex)
                        {
                            ConnectedProperties connectedProperties = blockConnectedProperties[propertyIndex];

                            if (connectedProperties != null && connectedProperties.matchesIcon(sprite))
                            {
                                BakedQuad[] connectedQuads = getConnectedTexture(connectedProperties, blockAccess, blockStateBase, blockPos, side, quad, pass, renderEnv);

                                if (connectedQuads != null)
                                {
                                    return connectedQuads;
                                }
                            }
                        }
                    }
                }
            }

            return renderEnv.getArrayQuadsCtm(quad);
        }
    }

    public static int getSide(EnumFacing facing)
    {
        if (facing == null)
        {
            return -1;
        }
        else
        {
            switch (facing)
            {
                case DOWN:
                    return 0;

                case UP:
                    return 1;

                case EAST:
                    return 5;

                case WEST:
                    return 4;

                case NORTH:
                    return 2;

                case SOUTH:
                    return 3;

                default:
                    return -1;
            }
        }
    }

    private static EnumFacing getFacing(int side)
    {
        switch (side)
        {
            case 0:
                return EnumFacing.DOWN;

            case 1:
                return EnumFacing.UP;

            case 2:
                return EnumFacing.NORTH;

            case 3:
                return EnumFacing.SOUTH;

            case 4:
                return EnumFacing.WEST;

            case 5:
                return EnumFacing.EAST;

            default:
                return EnumFacing.UP;
        }
    }

    private static BakedQuad[] getConnectedTexture(ConnectedProperties cp, IBlockAccess blockAccess, BlockStateBase blockState, BlockPos blockPos, int side, BakedQuad quad, int pass, RenderEnv renderEnv)
    {
        int axis = 0;
        int metadata = blockState.getMetadata();
        int matchMetadata = metadata;
        Block block = blockState.getBlock();

        if (block instanceof BlockRotatedPillar)
        {
            axis = getWoodAxis(side, metadata);

            if (cp.getMetadataMax() <= 3)
            {
                matchMetadata = metadata & 3;
            }
        }

        if (block instanceof BlockQuartz)
        {
            axis = getQuartzAxis(side, metadata);

            if (cp.getMetadataMax() <= 2 && matchMetadata > 2)
            {
                matchMetadata = 2;
            }
        }

        if (!cp.matchesBlock(blockState.getBlockId(), matchMetadata))
        {
            return null;
        }
        else
        {
            if (side >= 0 && cp.faces != 63)
            {
                int faceSide = side;

                if (axis != 0)
                {
                    faceSide = fixSideByAxis(side, axis);
                }

                if ((1 << faceSide & cp.faces) == 0)
                {
                    return null;
                }
            }

            int blockY = blockPos.getY();

            if (cp.heights != null && !cp.heights.isInRange(blockY))
            {
                return null;
            }
            else
            {
                if (cp.biomes != null)
                {
                    BiomeGenBase biome = blockAccess.getBiomeGenForCoords(blockPos);

                    if (!cp.matchesBiome(biome))
                    {
                        return null;
                    }
                }

                if (cp.nbtName != null)
                {
                    String tileEntityName = TileEntityUtils.getTileEntityName(blockAccess, blockPos);

                    if (!cp.nbtName.matchesValue(tileEntityName))
                    {
                        return null;
                    }
                }

                TextureAtlasSprite sprite = quad.getSprite();

                switch (cp.method)
                {
                    case 1:
                        return getQuads(getConnectedTextureCtm(cp, blockAccess, blockState, blockPos, axis, side, sprite, metadata, renderEnv), quad, renderEnv);

                    case 2:
                        return getQuads(getConnectedTextureHorizontal(cp, blockAccess, blockState, blockPos, axis, side, sprite, metadata), quad, renderEnv);

                    case 3:
                        return getQuads(getConnectedTextureTop(cp, blockAccess, blockState, blockPos, axis, side, sprite, metadata), quad, renderEnv);

                    case 4:
                        return getQuads(getConnectedTextureRandom(cp, blockAccess, blockState, blockPos, side), quad, renderEnv);

                    case 5:
                        return getQuads(getConnectedTextureRepeat(cp, blockPos, side), quad, renderEnv);

                    case 6:
                        return getQuads(getConnectedTextureVertical(cp, blockAccess, blockState, blockPos, axis, side, sprite, metadata), quad, renderEnv);

                    case 7:
                        return getQuads(getConnectedTextureFixed(cp), quad, renderEnv);

                    case 8:
                        return getQuads(getConnectedTextureHorizontalVertical(cp, blockAccess, blockState, blockPos, axis, side, sprite, metadata), quad, renderEnv);

                    case 9:
                        return getQuads(getConnectedTextureVerticalHorizontal(cp, blockAccess, blockState, blockPos, axis, side, sprite, metadata), quad, renderEnv);

                    case 10:
                        if (pass == 0)
                        {
                            return getConnectedTextureCtmCompact(cp, blockAccess, blockState, blockPos, axis, side, quad, metadata, renderEnv);
                        }

                    default:
                        return null;

                    case 11:
                        return getConnectedTextureOverlay(cp, blockAccess, blockState, blockPos, axis, side, quad, metadata, renderEnv);

                    case 12:
                        return getConnectedTextureOverlayFixed(cp, quad, renderEnv);

                    case 13:
                        return getConnectedTextureOverlayRandom(cp, blockAccess, blockState, blockPos, side, quad, renderEnv);

                    case 14:
                        return getConnectedTextureOverlayRepeat(cp, blockPos, side, quad, renderEnv);

                    case 15:
                        return getConnectedTextureOverlayCtm(cp, blockAccess, blockState, blockPos, axis, side, quad, metadata, renderEnv);
                }
            }
        }
    }

    private static int fixSideByAxis(int side, int vertAxis)
    {
        switch (vertAxis)
        {
            case 0:
                return side;

            case 1:
                switch (side)
                {
                    case 0:
                        return 2;

                    case 1:
                        return 3;

                    case 2:
                        return 1;

                    case 3:
                        return 0;

                    default:
                        return side;
                }

            case 2:
                switch (side)
                {
                    case 0:
                        return 4;

                    case 1:
                        return 5;

                    case 2:
                    case 3:
                    default:
                        return side;

                    case 4:
                        return 1;

                    case 5:
                        return 0;
                }

            default:
                return side;
        }
    }

    private static int getWoodAxis(int side, int metadata)
    {
        int axisBits = (metadata & 12) >> 2;

        switch (axisBits)
        {
            case 1:
                return 2;

            case 2:
                return 1;

            default:
                return 0;
        }
    }

    private static int getQuartzAxis(int side, int metadata)
    {
        switch (metadata)
        {
            case 3:
                return 2;

            case 4:
                return 1;

            default:
                return 0;
        }
    }

    private static TextureAtlasSprite getConnectedTextureRandom(ConnectedProperties cp, IBlockAccess blockAccess, BlockStateBase blockState, BlockPos blockPos, int side)
    {
        if (cp.tileIcons.length == 1)
        {
            return cp.tileIcons[0];
        }
        else
        {
            int symmetrySide = side / cp.symmetry * cp.symmetry;

            if (cp.linked)
            {
                BlockPos linkedPos = blockPos.down();

                for (IBlockState linkedState = blockAccess.getBlockState(linkedPos); linkedState.getBlock() == blockState.getBlock(); linkedState = blockAccess.getBlockState(linkedPos))
                {
                    blockPos = linkedPos;
                    linkedPos = linkedPos.down();

                    if (linkedPos.getY() < 0)
                    {
                        break;
                    }
                }
            }

            int random = Config.getRandom(blockPos, symmetrySide) & Integer.MAX_VALUE;

            for (int loopIndex = 0; loopIndex < cp.randomLoops; ++loopIndex)
            {
                random = Config.intHash(random);
            }

            int tileIndex = 0;

            if (cp.weights == null)
            {
                tileIndex = random % cp.tileIcons.length;
            }
            else
            {
                int weightIndex = random % cp.sumAllWeights;
                int[] sumWeights = cp.sumWeights;

                for (int tileIndexCandidate = 0; tileIndexCandidate < sumWeights.length; ++tileIndexCandidate)
                {
                    if (weightIndex < sumWeights[tileIndexCandidate])
                    {
                        tileIndex = tileIndexCandidate;
                        break;
                    }
                }
            }

            return cp.tileIcons[tileIndex];
        }
    }

    private static TextureAtlasSprite getConnectedTextureFixed(ConnectedProperties cp)
    {
        return cp.tileIcons[0];
    }

    private static TextureAtlasSprite getConnectedTextureRepeat(ConnectedProperties cp, BlockPos blockPos, int side)
    {
        if (cp.tileIcons.length == 1)
        {
            return cp.tileIcons[0];
        }
        else
        {
            int blockX = blockPos.getX();
            int blockY = blockPos.getY();
            int blockZ = blockPos.getZ();
            int tileX = 0;
            int tileY = 0;

            switch (side)
            {
                case 0:
                    tileX = blockX;
                    tileY = -blockZ - 1;
                    break;

                case 1:
                    tileX = blockX;
                    tileY = blockZ;
                    break;

                case 2:
                    tileX = -blockX - 1;
                    tileY = -blockY;
                    break;

                case 3:
                    tileX = blockX;
                    tileY = -blockY;
                    break;

                case 4:
                    tileX = blockZ;
                    tileY = -blockY;
                    break;

                case 5:
                    tileX = -blockZ - 1;
                    tileY = -blockY;
            }

            tileX = tileX % cp.width;
            tileY = tileY % cp.height;

            if (tileX < 0)
            {
                tileX += cp.width;
            }

            if (tileY < 0)
            {
                tileY += cp.height;
            }

            int tileIndex = tileY * cp.width + tileX;
            return cp.tileIcons[tileIndex];
        }
    }

    private static TextureAtlasSprite getConnectedTextureCtm(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata, RenderEnv renderEnv)
    {
        int tileIndex = getConnectedTextureCtmIndex(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata, renderEnv);
        return cp.tileIcons[tileIndex];
    }

    private static synchronized BakedQuad[] getConnectedTextureCtmCompact(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, BakedQuad quad, int metadata, RenderEnv renderEnv)
    {
        TextureAtlasSprite sprite = quad.getSprite();
        int tileIndex = getConnectedTextureCtmIndex(cp, blockAccess, blockState, blockPos, vertAxis, side, sprite, metadata, renderEnv);
        return ConnectedTexturesCompact.getConnectedTextureCtmCompact(tileIndex, cp, side, quad, renderEnv);
    }

    private static BakedQuad[] getConnectedTextureOverlay(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, BakedQuad quad, int metadata, RenderEnv renderEnv)
    {
        if (!quad.isFullQuad())
        {
            return null;
        }
        else
        {
            TextureAtlasSprite sprite = quad.getSprite();
            BlockDir[] sideDirections = getSideDirections(side, vertAxis);
            boolean[] borderFlags = renderEnv.getBorderFlags();

            for (int directionIndex = 0; directionIndex < 4; ++directionIndex)
            {
                borderFlags[directionIndex] = isNeighbourOverlay(cp, blockAccess, blockState, sideDirections[directionIndex].offset(blockPos), side, sprite, metadata);
            }

            ListQuadsOverlay overlayQuads = renderEnv.getListQuadsOverlay(cp.layer);
            Object dirEdges;

            try
            {
                if (!borderFlags[0] || !borderFlags[1] || !borderFlags[2] || !borderFlags[3])
                {
                    if (borderFlags[0] && borderFlags[1] && borderFlags[2])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[5], quad, cp.tintIndex), cp.tintBlockState);
                        dirEdges = null;
                        return (BakedQuad[])dirEdges;
                    }

                    if (borderFlags[0] && borderFlags[2] && borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[6], quad, cp.tintIndex), cp.tintBlockState);
                        dirEdges = null;
                        return (BakedQuad[])dirEdges;
                    }

                    if (borderFlags[1] && borderFlags[2] && borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[12], quad, cp.tintIndex), cp.tintBlockState);
                        dirEdges = null;
                        return (BakedQuad[])dirEdges;
                    }

                    if (borderFlags[0] && borderFlags[1] && borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[13], quad, cp.tintIndex), cp.tintBlockState);
                        dirEdges = null;
                        return (BakedQuad[])dirEdges;
                    }

                    BlockDir[] edgeDirections = getEdgeDirections(side, vertAxis);
                    boolean[] edgeFlags = renderEnv.getBorderFlags2();

                    for (int directionIndex = 0; directionIndex < 4; ++directionIndex)
                    {
                        edgeFlags[directionIndex] = isNeighbourOverlay(cp, blockAccess, blockState, edgeDirections[directionIndex].offset(blockPos), side, sprite, metadata);
                    }

                    if (borderFlags[1] && borderFlags[2])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[3], quad, cp.tintIndex), cp.tintBlockState);

                        if (edgeFlags[3])
                        {
                            overlayQuads.addQuad(getQuadFull(cp.tileIcons[16], quad, cp.tintIndex), cp.tintBlockState);
                        }

                        Object object4 = null;
                        return (BakedQuad[])object4;
                    }

                    if (borderFlags[0] && borderFlags[2])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[4], quad, cp.tintIndex), cp.tintBlockState);

                        if (edgeFlags[2])
                        {
                            overlayQuads.addQuad(getQuadFull(cp.tileIcons[14], quad, cp.tintIndex), cp.tintBlockState);
                        }

                        Object object3 = null;
                        return (BakedQuad[])object3;
                    }

                    if (borderFlags[1] && borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[10], quad, cp.tintIndex), cp.tintBlockState);

                        if (edgeFlags[1])
                        {
                            overlayQuads.addQuad(getQuadFull(cp.tileIcons[2], quad, cp.tintIndex), cp.tintBlockState);
                        }

                        Object object2 = null;
                        return (BakedQuad[])object2;
                    }

                    if (borderFlags[0] && borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[11], quad, cp.tintIndex), cp.tintBlockState);

                        if (edgeFlags[0])
                        {
                            overlayQuads.addQuad(getQuadFull(cp.tileIcons[0], quad, cp.tintIndex), cp.tintBlockState);
                        }

                        Object object1 = null;
                        return (BakedQuad[])object1;
                    }

                    boolean[] matchingFlags = renderEnv.getBorderFlags3();

                    for (int directionIndex = 0; directionIndex < 4; ++directionIndex)
                    {
                        matchingFlags[directionIndex] = isNeighbourMatching(cp, blockAccess, blockState, sideDirections[directionIndex].offset(blockPos), side, sprite, metadata);
                    }

                    if (borderFlags[0])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[9], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (borderFlags[1])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[7], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (borderFlags[2])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[1], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[15], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (edgeFlags[0] && (matchingFlags[1] || matchingFlags[2]) && !borderFlags[1] && !borderFlags[2])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[0], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (edgeFlags[1] && (matchingFlags[0] || matchingFlags[2]) && !borderFlags[0] && !borderFlags[2])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[2], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (edgeFlags[2] && (matchingFlags[1] || matchingFlags[3]) && !borderFlags[1] && !borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[14], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    if (edgeFlags[3] && (matchingFlags[0] || matchingFlags[3]) && !borderFlags[0] && !borderFlags[3])
                    {
                        overlayQuads.addQuad(getQuadFull(cp.tileIcons[16], quad, cp.tintIndex), cp.tintBlockState);
                    }

                    Object object5 = null;
                    return (BakedQuad[])object5;
                }

                overlayQuads.addQuad(getQuadFull(cp.tileIcons[8], quad, cp.tintIndex), cp.tintBlockState);
                dirEdges = null;
            }
            finally
            {
                if (overlayQuads.size() > 0)
                {
                    renderEnv.setOverlaysRendered(true);
                }
            }

            return (BakedQuad[])dirEdges;
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayFixed(ConnectedProperties cp, BakedQuad quad, RenderEnv renderEnv)
    {
        if (!quad.isFullQuad())
        {
            return null;
        }
        else
        {
            ListQuadsOverlay overlayQuads = renderEnv.getListQuadsOverlay(cp.layer);
            Object object;

            try
            {
                TextureAtlasSprite sprite = getConnectedTextureFixed(cp);

                if (sprite != null)
                {
                    overlayQuads.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
                }

                object = null;
            }
            finally
            {
                if (overlayQuads.size() > 0)
                {
                    renderEnv.setOverlaysRendered(true);
                }
            }

            return (BakedQuad[])object;
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayRandom(ConnectedProperties cp, IBlockAccess blockAccess, BlockStateBase blockState, BlockPos blockPos, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        if (!quad.isFullQuad())
        {
            return null;
        }
        else
        {
            ListQuadsOverlay overlayQuads = renderEnv.getListQuadsOverlay(cp.layer);
            Object object;

            try
            {
                TextureAtlasSprite sprite = getConnectedTextureRandom(cp, blockAccess, blockState, blockPos, side);

                if (sprite != null)
                {
                    overlayQuads.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
                }

                object = null;
            }
            finally
            {
                if (overlayQuads.size() > 0)
                {
                    renderEnv.setOverlaysRendered(true);
                }
            }

            return (BakedQuad[])object;
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayRepeat(ConnectedProperties cp, BlockPos blockPos, int side, BakedQuad quad, RenderEnv renderEnv)
    {
        if (!quad.isFullQuad())
        {
            return null;
        }
        else
        {
            ListQuadsOverlay overlayQuads = renderEnv.getListQuadsOverlay(cp.layer);
            Object object;

            try
            {
                TextureAtlasSprite sprite = getConnectedTextureRepeat(cp, blockPos, side);

                if (sprite != null)
                {
                    overlayQuads.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
                }

                object = null;
            }
            finally
            {
                if (overlayQuads.size() > 0)
                {
                    renderEnv.setOverlaysRendered(true);
                }
            }

            return (BakedQuad[])object;
        }
    }

    private static BakedQuad[] getConnectedTextureOverlayCtm(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, BakedQuad quad, int metadata, RenderEnv renderEnv)
    {
        if (!quad.isFullQuad())
        {
            return null;
        }
        else
        {
            ListQuadsOverlay overlayQuads = renderEnv.getListQuadsOverlay(cp.layer);
            Object object;

            try
            {
                TextureAtlasSprite sprite = getConnectedTextureCtm(cp, blockAccess, blockState, blockPos, vertAxis, side, quad.getSprite(), metadata, renderEnv);

                if (sprite != null)
                {
                    overlayQuads.addQuad(getQuadFull(sprite, quad, cp.tintIndex), cp.tintBlockState);
                }

                object = null;
            }
            finally
            {
                if (overlayQuads.size() > 0)
                {
                    renderEnv.setOverlaysRendered(true);
                }
            }

            return (BakedQuad[])object;
        }
    }

    private static BlockDir[] getSideDirections(int side, int vertAxis)
    {
        switch (side)
        {
            case 0:
                return SIDES_Y_NEG_DOWN;

            case 1:
                return SIDES_Y_POS_UP;

            case 2:
                if (vertAxis == 1)
                {
                    return SIDES_Z_NEG_NORTH_Z_AXIS;
                }

                return SIDES_Z_NEG_NORTH;

            case 3:
                return SIDES_Z_POS_SOUTH;

            case 4:
                return SIDES_X_NEG_WEST;

            case 5:
                if (vertAxis == 2)
                {
                    return SIDES_X_POS_EAST_X_AXIS;
                }

                return SIDES_X_POS_EAST;

            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    private static BlockDir[] getEdgeDirections(int side, int vertAxis)
    {
        switch (side)
        {
            case 0:
                return EDGES_Y_NEG_DOWN;

            case 1:
                return EDGES_Y_POS_UP;

            case 2:
                if (vertAxis == 1)
                {
                    return EDGES_Z_NEG_NORTH_Z_AXIS;
                }

                return EDGES_Z_NEG_NORTH;

            case 3:
                return EDGES_Z_POS_SOUTH;

            case 4:
                return EDGES_X_NEG_WEST;

            case 5:
                if (vertAxis == 2)
                {
                    return EDGES_X_POS_EAST_X_AXIS;
                }

                return EDGES_X_POS_EAST;

            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    protected static Map[][] getSpriteQuadCompactMaps()
    {
        return spriteQuadCompactMaps;
    }

    private static int getConnectedTextureCtmIndex(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata, RenderEnv renderEnv)
    {
        boolean[] borderFlags = renderEnv.getBorderFlags();

        switch (side)
        {
            case 0:
                borderFlags[0] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borderFlags[1] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borderFlags[2] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                borderFlags[3] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);

                if (cp.innerSeams)
                {
                    BlockPos downSeamPos = blockPos.down();
                    borderFlags[0] = borderFlags[0] && !isNeighbour(cp, blockAccess, blockState, downSeamPos.west(), side, icon, metadata);
                    borderFlags[1] = borderFlags[1] && !isNeighbour(cp, blockAccess, blockState, downSeamPos.east(), side, icon, metadata);
                    borderFlags[2] = borderFlags[2] && !isNeighbour(cp, blockAccess, blockState, downSeamPos.north(), side, icon, metadata);
                    borderFlags[3] = borderFlags[3] && !isNeighbour(cp, blockAccess, blockState, downSeamPos.south(), side, icon, metadata);
                }

                break;

            case 1:
                borderFlags[0] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borderFlags[1] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borderFlags[2] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                borderFlags[3] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);

                if (cp.innerSeams)
                {
                    BlockPos upSeamPos = blockPos.up();
                    borderFlags[0] = borderFlags[0] && !isNeighbour(cp, blockAccess, blockState, upSeamPos.west(), side, icon, metadata);
                    borderFlags[1] = borderFlags[1] && !isNeighbour(cp, blockAccess, blockState, upSeamPos.east(), side, icon, metadata);
                    borderFlags[2] = borderFlags[2] && !isNeighbour(cp, blockAccess, blockState, upSeamPos.south(), side, icon, metadata);
                    borderFlags[3] = borderFlags[3] && !isNeighbour(cp, blockAccess, blockState, upSeamPos.north(), side, icon, metadata);
                }

                break;

            case 2:
                borderFlags[0] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borderFlags[1] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borderFlags[2] = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                borderFlags[3] = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);

                if (cp.innerSeams)
                {
                    BlockPos northSeamPos = blockPos.north();
                    borderFlags[0] = borderFlags[0] && !isNeighbour(cp, blockAccess, blockState, northSeamPos.east(), side, icon, metadata);
                    borderFlags[1] = borderFlags[1] && !isNeighbour(cp, blockAccess, blockState, northSeamPos.west(), side, icon, metadata);
                    borderFlags[2] = borderFlags[2] && !isNeighbour(cp, blockAccess, blockState, northSeamPos.down(), side, icon, metadata);
                    borderFlags[3] = borderFlags[3] && !isNeighbour(cp, blockAccess, blockState, northSeamPos.up(), side, icon, metadata);
                }

                if (vertAxis == 1)
                {
                    switchValues(0, 1, borderFlags);
                    switchValues(2, 3, borderFlags);
                }

                break;

            case 3:
                borderFlags[0] = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                borderFlags[1] = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                borderFlags[2] = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                borderFlags[3] = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);

                if (cp.innerSeams)
                {
                    BlockPos southSeamPos = blockPos.south();
                    borderFlags[0] = borderFlags[0] && !isNeighbour(cp, blockAccess, blockState, southSeamPos.west(), side, icon, metadata);
                    borderFlags[1] = borderFlags[1] && !isNeighbour(cp, blockAccess, blockState, southSeamPos.east(), side, icon, metadata);
                    borderFlags[2] = borderFlags[2] && !isNeighbour(cp, blockAccess, blockState, southSeamPos.down(), side, icon, metadata);
                    borderFlags[3] = borderFlags[3] && !isNeighbour(cp, blockAccess, blockState, southSeamPos.up(), side, icon, metadata);
                }

                break;

            case 4:
                borderFlags[0] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                borderFlags[1] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                borderFlags[2] = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                borderFlags[3] = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);

                if (cp.innerSeams)
                {
                    BlockPos westSeamPos = blockPos.west();
                    borderFlags[0] = borderFlags[0] && !isNeighbour(cp, blockAccess, blockState, westSeamPos.north(), side, icon, metadata);
                    borderFlags[1] = borderFlags[1] && !isNeighbour(cp, blockAccess, blockState, westSeamPos.south(), side, icon, metadata);
                    borderFlags[2] = borderFlags[2] && !isNeighbour(cp, blockAccess, blockState, westSeamPos.down(), side, icon, metadata);
                    borderFlags[3] = borderFlags[3] && !isNeighbour(cp, blockAccess, blockState, westSeamPos.up(), side, icon, metadata);
                }

                break;

            case 5:
                borderFlags[0] = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                borderFlags[1] = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                borderFlags[2] = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                borderFlags[3] = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);

                if (cp.innerSeams)
                {
                    BlockPos eastSeamPos = blockPos.east();
                    borderFlags[0] = borderFlags[0] && !isNeighbour(cp, blockAccess, blockState, eastSeamPos.south(), side, icon, metadata);
                    borderFlags[1] = borderFlags[1] && !isNeighbour(cp, blockAccess, blockState, eastSeamPos.north(), side, icon, metadata);
                    borderFlags[2] = borderFlags[2] && !isNeighbour(cp, blockAccess, blockState, eastSeamPos.down(), side, icon, metadata);
                    borderFlags[3] = borderFlags[3] && !isNeighbour(cp, blockAccess, blockState, eastSeamPos.up(), side, icon, metadata);
                }

                if (vertAxis == 2)
                {
                    switchValues(0, 1, borderFlags);
                    switchValues(2, 3, borderFlags);
                }
        }

        int ctmIndex = 0;

        if (borderFlags[0] & !borderFlags[1] & !borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 3;
        }
        else if (!borderFlags[0] & borderFlags[1] & !borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 1;
        }
        else if (!borderFlags[0] & !borderFlags[1] & borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 12;
        }
        else if (!borderFlags[0] & !borderFlags[1] & !borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 36;
        }
        else if (borderFlags[0] & borderFlags[1] & !borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 2;
        }
        else if (!borderFlags[0] & !borderFlags[1] & borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 24;
        }
        else if (borderFlags[0] & !borderFlags[1] & borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 15;
        }
        else if (borderFlags[0] & !borderFlags[1] & !borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 39;
        }
        else if (!borderFlags[0] & borderFlags[1] & borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 13;
        }
        else if (!borderFlags[0] & borderFlags[1] & !borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 37;
        }
        else if (!borderFlags[0] & borderFlags[1] & borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 25;
        }
        else if (borderFlags[0] & !borderFlags[1] & borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 27;
        }
        else if (borderFlags[0] & borderFlags[1] & !borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 38;
        }
        else if (borderFlags[0] & borderFlags[1] & borderFlags[2] & !borderFlags[3])
        {
            ctmIndex = 14;
        }
        else if (borderFlags[0] & borderFlags[1] & borderFlags[2] & borderFlags[3])
        {
            ctmIndex = 26;
        }

        if (ctmIndex == 0)
        {
            return ctmIndex;
        }
        else if (!Config.isConnectedTexturesFancy())
        {
            return ctmIndex;
        }
        else
        {
            switch (side)
            {
                case 0:
                    borderFlags[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().north(), side, icon, metadata);
                    borderFlags[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().north(), side, icon, metadata);
                    borderFlags[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().south(), side, icon, metadata);
                    borderFlags[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().south(), side, icon, metadata);

                    if (cp.innerSeams)
                    {
                        BlockPos downCornerPos = blockPos.down();
                        borderFlags[0] = borderFlags[0] || isNeighbour(cp, blockAccess, blockState, downCornerPos.east().north(), side, icon, metadata);
                        borderFlags[1] = borderFlags[1] || isNeighbour(cp, blockAccess, blockState, downCornerPos.west().north(), side, icon, metadata);
                        borderFlags[2] = borderFlags[2] || isNeighbour(cp, blockAccess, blockState, downCornerPos.east().south(), side, icon, metadata);
                        borderFlags[3] = borderFlags[3] || isNeighbour(cp, blockAccess, blockState, downCornerPos.west().south(), side, icon, metadata);
                    }

                    break;

                case 1:
                    borderFlags[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().south(), side, icon, metadata);
                    borderFlags[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().south(), side, icon, metadata);
                    borderFlags[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().north(), side, icon, metadata);
                    borderFlags[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().north(), side, icon, metadata);

                    if (cp.innerSeams)
                    {
                        BlockPos upCornerPos = blockPos.up();
                        borderFlags[0] = borderFlags[0] || isNeighbour(cp, blockAccess, blockState, upCornerPos.east().south(), side, icon, metadata);
                        borderFlags[1] = borderFlags[1] || isNeighbour(cp, blockAccess, blockState, upCornerPos.west().south(), side, icon, metadata);
                        borderFlags[2] = borderFlags[2] || isNeighbour(cp, blockAccess, blockState, upCornerPos.east().north(), side, icon, metadata);
                        borderFlags[3] = borderFlags[3] || isNeighbour(cp, blockAccess, blockState, upCornerPos.west().north(), side, icon, metadata);
                    }

                    break;

                case 2:
                    borderFlags[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().down(), side, icon, metadata);
                    borderFlags[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().down(), side, icon, metadata);
                    borderFlags[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().up(), side, icon, metadata);
                    borderFlags[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().up(), side, icon, metadata);

                    if (cp.innerSeams)
                    {
                        BlockPos northCornerPos = blockPos.north();
                        borderFlags[0] = borderFlags[0] || isNeighbour(cp, blockAccess, blockState, northCornerPos.west().down(), side, icon, metadata);
                        borderFlags[1] = borderFlags[1] || isNeighbour(cp, blockAccess, blockState, northCornerPos.east().down(), side, icon, metadata);
                        borderFlags[2] = borderFlags[2] || isNeighbour(cp, blockAccess, blockState, northCornerPos.west().up(), side, icon, metadata);
                        borderFlags[3] = borderFlags[3] || isNeighbour(cp, blockAccess, blockState, northCornerPos.east().up(), side, icon, metadata);
                    }

                    if (vertAxis == 1)
                    {
                        switchValues(0, 3, borderFlags);
                        switchValues(1, 2, borderFlags);
                    }

                    break;

                case 3:
                    borderFlags[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().down(), side, icon, metadata);
                    borderFlags[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().down(), side, icon, metadata);
                    borderFlags[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.east().up(), side, icon, metadata);
                    borderFlags[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.west().up(), side, icon, metadata);

                    if (cp.innerSeams)
                    {
                        BlockPos southCornerPos = blockPos.south();
                        borderFlags[0] = borderFlags[0] || isNeighbour(cp, blockAccess, blockState, southCornerPos.east().down(), side, icon, metadata);
                        borderFlags[1] = borderFlags[1] || isNeighbour(cp, blockAccess, blockState, southCornerPos.west().down(), side, icon, metadata);
                        borderFlags[2] = borderFlags[2] || isNeighbour(cp, blockAccess, blockState, southCornerPos.east().up(), side, icon, metadata);
                        borderFlags[3] = borderFlags[3] || isNeighbour(cp, blockAccess, blockState, southCornerPos.west().up(), side, icon, metadata);
                    }

                    break;

                case 4:
                    borderFlags[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.down().south(), side, icon, metadata);
                    borderFlags[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.down().north(), side, icon, metadata);
                    borderFlags[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.up().south(), side, icon, metadata);
                    borderFlags[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.up().north(), side, icon, metadata);

                    if (cp.innerSeams)
                    {
                        BlockPos westCornerPos = blockPos.west();
                        borderFlags[0] = borderFlags[0] || isNeighbour(cp, blockAccess, blockState, westCornerPos.down().south(), side, icon, metadata);
                        borderFlags[1] = borderFlags[1] || isNeighbour(cp, blockAccess, blockState, westCornerPos.down().north(), side, icon, metadata);
                        borderFlags[2] = borderFlags[2] || isNeighbour(cp, blockAccess, blockState, westCornerPos.up().south(), side, icon, metadata);
                        borderFlags[3] = borderFlags[3] || isNeighbour(cp, blockAccess, blockState, westCornerPos.up().north(), side, icon, metadata);
                    }

                    break;

                case 5:
                    borderFlags[0] = !isNeighbour(cp, blockAccess, blockState, blockPos.down().north(), side, icon, metadata);
                    borderFlags[1] = !isNeighbour(cp, blockAccess, blockState, blockPos.down().south(), side, icon, metadata);
                    borderFlags[2] = !isNeighbour(cp, blockAccess, blockState, blockPos.up().north(), side, icon, metadata);
                    borderFlags[3] = !isNeighbour(cp, blockAccess, blockState, blockPos.up().south(), side, icon, metadata);

                    if (cp.innerSeams)
                    {
                        BlockPos eastCornerPos = blockPos.east();
                        borderFlags[0] = borderFlags[0] || isNeighbour(cp, blockAccess, blockState, eastCornerPos.down().north(), side, icon, metadata);
                        borderFlags[1] = borderFlags[1] || isNeighbour(cp, blockAccess, blockState, eastCornerPos.down().south(), side, icon, metadata);
                        borderFlags[2] = borderFlags[2] || isNeighbour(cp, blockAccess, blockState, eastCornerPos.up().north(), side, icon, metadata);
                        borderFlags[3] = borderFlags[3] || isNeighbour(cp, blockAccess, blockState, eastCornerPos.up().south(), side, icon, metadata);
                    }

                    if (vertAxis == 2)
                    {
                        switchValues(0, 3, borderFlags);
                        switchValues(1, 2, borderFlags);
                    }
            }

            if (ctmIndex == 13 && borderFlags[0])
            {
                ctmIndex = 4;
            }
            else if (ctmIndex == 15 && borderFlags[1])
            {
                ctmIndex = 5;
            }
            else if (ctmIndex == 37 && borderFlags[2])
            {
                ctmIndex = 16;
            }
            else if (ctmIndex == 39 && borderFlags[3])
            {
                ctmIndex = 17;
            }
            else if (ctmIndex == 14 && borderFlags[0] && borderFlags[1])
            {
                ctmIndex = 7;
            }
            else if (ctmIndex == 25 && borderFlags[0] && borderFlags[2])
            {
                ctmIndex = 6;
            }
            else if (ctmIndex == 27 && borderFlags[3] && borderFlags[1])
            {
                ctmIndex = 19;
            }
            else if (ctmIndex == 38 && borderFlags[3] && borderFlags[2])
            {
                ctmIndex = 18;
            }
            else if (ctmIndex == 14 && !borderFlags[0] && borderFlags[1])
            {
                ctmIndex = 31;
            }
            else if (ctmIndex == 25 && borderFlags[0] && !borderFlags[2])
            {
                ctmIndex = 30;
            }
            else if (ctmIndex == 27 && !borderFlags[3] && borderFlags[1])
            {
                ctmIndex = 41;
            }
            else if (ctmIndex == 38 && borderFlags[3] && !borderFlags[2])
            {
                ctmIndex = 40;
            }
            else if (ctmIndex == 14 && borderFlags[0] && !borderFlags[1])
            {
                ctmIndex = 29;
            }
            else if (ctmIndex == 25 && !borderFlags[0] && borderFlags[2])
            {
                ctmIndex = 28;
            }
            else if (ctmIndex == 27 && borderFlags[3] && !borderFlags[1])
            {
                ctmIndex = 43;
            }
            else if (ctmIndex == 38 && !borderFlags[3] && borderFlags[2])
            {
                ctmIndex = 42;
            }
            else if (ctmIndex == 26 && borderFlags[0] && borderFlags[1] && borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 46;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && borderFlags[1] && borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 9;
            }
            else if (ctmIndex == 26 && borderFlags[0] && !borderFlags[1] && borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 21;
            }
            else if (ctmIndex == 26 && borderFlags[0] && borderFlags[1] && !borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 8;
            }
            else if (ctmIndex == 26 && borderFlags[0] && borderFlags[1] && borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 20;
            }
            else if (ctmIndex == 26 && borderFlags[0] && borderFlags[1] && !borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 11;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && !borderFlags[1] && borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 22;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && borderFlags[1] && !borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 23;
            }
            else if (ctmIndex == 26 && borderFlags[0] && !borderFlags[1] && borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 10;
            }
            else if (ctmIndex == 26 && borderFlags[0] && !borderFlags[1] && !borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 34;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && borderFlags[1] && borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 35;
            }
            else if (ctmIndex == 26 && borderFlags[0] && !borderFlags[1] && !borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 32;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && borderFlags[1] && !borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 33;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && !borderFlags[1] && borderFlags[2] && !borderFlags[3])
            {
                ctmIndex = 44;
            }
            else if (ctmIndex == 26 && !borderFlags[0] && !borderFlags[1] && !borderFlags[2] && borderFlags[3])
            {
                ctmIndex = 45;
            }

            return ctmIndex;
        }
    }

    private static void switchValues(int firstIndex, int secondIndex, boolean[] values)
    {
        boolean temp = values[firstIndex];
        values[firstIndex] = values[secondIndex];
        values[secondIndex] = temp;
    }

    private static boolean isNeighbourOverlay(ConnectedProperties cp, IBlockAccess iblockaccess, IBlockState blockState, BlockPos blockPos, int side, TextureAtlasSprite icon, int metadata)
    {
        IBlockState neighbourState = iblockaccess.getBlockState(blockPos);

        if (!isFullCubeModel(neighbourState))
        {
            return false;
        }
        else
        {
            if (cp.connectBlocks != null)
            {
                BlockStateBase neighbourStateBase = (BlockStateBase)neighbourState;

                if (!Matches.block(neighbourStateBase.getBlockId(), neighbourStateBase.getMetadata(), cp.connectBlocks))
                {
                    return false;
                }
            }

            if (cp.connectTileIcons != null)
            {
                TextureAtlasSprite neighbourIcon = getNeighbourIcon(iblockaccess, blockState, blockPos, neighbourState, side);

                if (!Config.isSameOne(neighbourIcon, cp.connectTileIcons))
                {
                    return false;
                }
            }

            IBlockState outwardState = iblockaccess.getBlockState(blockPos.offset(getFacing(side)));
            return outwardState.getBlock().isOpaqueCube() ? false : (side == 1 && outwardState.getBlock() == Blocks.snow_layer ? false : !isNeighbour(cp, iblockaccess, blockState, blockPos, neighbourState, side, icon, metadata));
        }
    }

    private static boolean isFullCubeModel(IBlockState state)
    {
        if (state.getBlock().isFullCube())
        {
            return true;
        }
        else
        {
            Block block = state.getBlock();
            return block instanceof BlockGlass ? true : block instanceof BlockStainedGlass;
        }
    }

    private static boolean isNeighbourMatching(ConnectedProperties cp, IBlockAccess iblockaccess, IBlockState blockState, BlockPos blockPos, int side, TextureAtlasSprite icon, int metadata)
    {
        IBlockState neighbourState = iblockaccess.getBlockState(blockPos);

        if (neighbourState == AIR_DEFAULT_STATE)
        {
            return false;
        }
        else
        {
            if (cp.matchBlocks != null && neighbourState instanceof BlockStateBase)
            {
                BlockStateBase neighbourStateBase = (BlockStateBase)neighbourState;

                if (!cp.matchesBlock(neighbourStateBase.getBlockId(), neighbourStateBase.getMetadata()))
                {
                    return false;
                }
            }

            if (cp.matchTileIcons != null)
            {
                TextureAtlasSprite neighbourIcon = getNeighbourIcon(iblockaccess, blockState, blockPos, neighbourState, side);

                if (neighbourIcon != icon)
                {
                    return false;
                }
            }

            IBlockState outwardState = iblockaccess.getBlockState(blockPos.offset(getFacing(side)));
            return outwardState.getBlock().isOpaqueCube() ? false : side != 1 || outwardState.getBlock() != Blocks.snow_layer;
        }
    }

    private static boolean isNeighbour(ConnectedProperties cp, IBlockAccess iblockaccess, IBlockState blockState, BlockPos blockPos, int side, TextureAtlasSprite icon, int metadata)
    {
        IBlockState neighbourState = iblockaccess.getBlockState(blockPos);
        return isNeighbour(cp, iblockaccess, blockState, blockPos, neighbourState, side, icon, metadata);
    }

    private static boolean isNeighbour(ConnectedProperties cp, IBlockAccess iblockaccess, IBlockState blockState, BlockPos blockPos, IBlockState neighbourState, int side, TextureAtlasSprite icon, int metadata)
    {
        if (blockState == neighbourState)
        {
            return true;
        }
        else if (cp.connect == 2)
        {
            if (neighbourState == null)
            {
                return false;
            }
            else if (neighbourState == AIR_DEFAULT_STATE)
            {
                return false;
            }
        else
        {
            TextureAtlasSprite neighbourIcon = getNeighbourIcon(iblockaccess, blockState, blockPos, neighbourState, side);
            return neighbourIcon == icon;
        }
        }
        else if (cp.connect == 3)
        {
            return neighbourState == null ? false : (neighbourState == AIR_DEFAULT_STATE ? false : neighbourState.getBlock().getMaterial() == blockState.getBlock().getMaterial());
        }
        else if (!(neighbourState instanceof BlockStateBase))
        {
            return false;
        }
        else
        {
            BlockStateBase neighbourStateBase = (BlockStateBase)neighbourState;
            Block block = neighbourStateBase.getBlock();
            int neighbourMetadata = neighbourStateBase.getMetadata();
            return block == blockState.getBlock() && neighbourMetadata == metadata;
        }
    }

    private static TextureAtlasSprite getNeighbourIcon(IBlockAccess iblockaccess, IBlockState blockState, BlockPos blockPos, IBlockState neighbourState, int side)
    {
        neighbourState = neighbourState.getBlock().getActualState(neighbourState, iblockaccess, blockPos);
        IBakedModel bakedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(neighbourState);

        if (bakedModel == null)
        {
            return null;
        }
        else
        {
            
            EnumFacing facing = getFacing(side);
            List faceQuads = bakedModel.getFaceQuads(facing);

            if (faceQuads == null)
            {
                return null;
            }
            else
            {
                if (Config.isBetterGrass())
                {
                    faceQuads = BetterGrass.getFaceQuads(iblockaccess, neighbourState, blockPos, facing, faceQuads);
                }

                if (faceQuads.size() > 0)
                {
                    BakedQuad faceQuad = (BakedQuad)faceQuads.get(0);
                    return faceQuad.getSprite();
                }
                else
                {
                    List generalQuads = bakedModel.getGeneralQuads();

                    if (generalQuads == null)
                    {
                        return null;
                    }
                    else
                    {
                        for (int quadIndex = 0; quadIndex < generalQuads.size(); ++quadIndex)
                        {
                            BakedQuad bakedQuad = (BakedQuad)generalQuads.get(quadIndex);

                            if (bakedQuad.getFace() == facing)
                            {
                                return bakedQuad.getSprite();
                            }
                        }

                        return null;
                    }
                }
            }
        }
    }

    private static TextureAtlasSprite getConnectedTextureHorizontal(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata)
    {
        boolean connectedLeft;
        boolean connectedRight;
        connectedLeft = false;
        connectedRight = false;
        label0:

        switch (vertAxis)
        {
            case 0:
                switch (side)
                {
                    case 0:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label0;

                    case 1:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label0;

                    case 2:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        break label0;

                    case 3:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label0;

                    case 4:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        break label0;

                    case 5:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);

                    default:
                        break label0;
                }

            case 1:
                switch (side)
                {
                    case 0:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        break label0;

                    case 1:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label0;

                    case 2:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label0;

                    case 3:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                        break label0;

                    case 4:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                        break label0;

                    case 5:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);

                    default:
                        break label0;
                }

            case 2:
                switch (side)
                {
                    case 0:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        break;

                    case 1:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        break;

                    case 2:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                        break;

                    case 3:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                        break;

                    case 4:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                        break;

                    case 5:
                        connectedLeft = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                        connectedRight = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                }
        }

        int tileIndex = 3;

        if (connectedLeft)
        {
            if (connectedRight)
            {
                tileIndex = 1;
            }
            else
            {
                tileIndex = 2;
            }
        }
        else if (connectedRight)
        {
            tileIndex = 0;
        }
        else
        {
            tileIndex = 3;
        }

        return cp.tileIcons[tileIndex];
    }

    private static TextureAtlasSprite getConnectedTextureVertical(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata)
    {
        boolean connectedTop = false;
        boolean connectedBottom = false;

        switch (vertAxis)
        {
            case 0:
                if (side == 1)
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                }
                else if (side == 0)
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                }
                else
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                }

                break;

            case 1:
                if (side == 3)
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                }
                else if (side == 2)
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                }
                else
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.north(), side, icon, metadata);
                }

                break;

            case 2:
                if (side == 5)
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                }
                else if (side == 4)
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.down(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                }
                else
                {
                    connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.west(), side, icon, metadata);
                    connectedBottom = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
                }
        }

        int tileIndex = 3;

        if (connectedTop)
        {
            if (connectedBottom)
            {
                tileIndex = 1;
            }
            else
            {
                tileIndex = 2;
            }
        }
        else if (connectedBottom)
        {
            tileIndex = 0;
        }
        else
        {
            tileIndex = 3;
        }

        return cp.tileIcons[tileIndex];
    }

    private static TextureAtlasSprite getConnectedTextureHorizontalVertical(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata)
    {
        TextureAtlasSprite[] tileIcons = cp.tileIcons;
        TextureAtlasSprite horizontalSprite = getConnectedTextureHorizontal(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);

        if (horizontalSprite != null && horizontalSprite != icon && horizontalSprite != tileIcons[3])
        {
            return horizontalSprite;
        }
        else
        {
            TextureAtlasSprite verticalSprite = getConnectedTextureVertical(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);
            return verticalSprite == tileIcons[0] ? tileIcons[4] : (verticalSprite == tileIcons[1] ? tileIcons[5] : (verticalSprite == tileIcons[2] ? tileIcons[6] : verticalSprite));
        }
    }

    private static TextureAtlasSprite getConnectedTextureVerticalHorizontal(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata)
    {
        TextureAtlasSprite[] tileIcons = cp.tileIcons;
        TextureAtlasSprite verticalSprite = getConnectedTextureVertical(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);

        if (verticalSprite != null && verticalSprite != icon && verticalSprite != tileIcons[3])
        {
            return verticalSprite;
        }
        else
        {
            TextureAtlasSprite horizontalSprite = getConnectedTextureHorizontal(cp, blockAccess, blockState, blockPos, vertAxis, side, icon, metadata);
            return horizontalSprite == tileIcons[0] ? tileIcons[4] : (horizontalSprite == tileIcons[1] ? tileIcons[5] : (horizontalSprite == tileIcons[2] ? tileIcons[6] : horizontalSprite));
        }
    }

    private static TextureAtlasSprite getConnectedTextureTop(ConnectedProperties cp, IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, int vertAxis, int side, TextureAtlasSprite icon, int metadata)
    {
        boolean connectedTop = false;

        switch (vertAxis)
        {
            case 0:
                if (side == 1 || side == 0)
                {
                    return null;
                }

                connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.up(), side, icon, metadata);
                break;

            case 1:
                if (side == 3 || side == 2)
                {
                    return null;
                }

                connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.south(), side, icon, metadata);
                break;

            case 2:
                if (side == 5 || side == 4)
                {
                    return null;
                }

                connectedTop = isNeighbour(cp, blockAccess, blockState, blockPos.east(), side, icon, metadata);
        }

        if (connectedTop)
        {
            return cp.tileIcons[0];
        }
        else
        {
            return null;
        }
    }

    public static void updateIcons(TextureMap textureMap)
    {
        blockProperties = (ConnectedProperties[][])null;
        tileProperties = (ConnectedProperties[][])null;
        spriteQuadMaps = null;
        spriteQuadCompactMaps = (Map[][])null;

        if (Config.isConnectedTextures())
        {
            IResourcePack[] resourcePacks = Config.getResourcePacks();

            for (int packIndex = resourcePacks.length - 1; packIndex >= 0; --packIndex)
            {
                IResourcePack resourcePack = resourcePacks[packIndex];
                updateIcons(textureMap, resourcePack);
            }

            updateIcons(textureMap, Config.getDefaultResourcePack());
            ResourceLocation emptySpriteLocation = new ResourceLocation("mcpatcher/ctm/default/empty");
            emptySprite = textureMap.registerSprite(emptySpriteLocation);
            spriteQuadMaps = new Map[textureMap.getCountRegisteredSprites() + 1];
            spriteQuadFullMaps = new Map[textureMap.getCountRegisteredSprites() + 1];
            spriteQuadCompactMaps = new Map[textureMap.getCountRegisteredSprites() + 1][];

            if (blockProperties.length <= 0)
            {
                blockProperties = (ConnectedProperties[][])null;
            }

            if (tileProperties.length <= 0)
            {
                tileProperties = (ConnectedProperties[][])null;
            }
        }
    }

    private static void updateIconEmpty(TextureMap textureMap)
    {
    }

    public static void updateIcons(TextureMap textureMap, IResourcePack rp)
    {
        String[] propertyPaths = ResUtils.collectFiles(rp, "mcpatcher/ctm/", ".properties", getDefaultCtmPaths());
        Arrays.sort((Object[])propertyPaths);
        List tilePropertyList = makePropertyList(tileProperties);
        List blockPropertyList = makePropertyList(blockProperties);

        for (int propertyIndex = 0; propertyIndex < propertyPaths.length; ++propertyIndex)
        {
            String propertyPath = propertyPaths[propertyIndex];
            Config.dbg("ConnectedTextures: " + propertyPath);

            try
            {
                ResourceLocation propertyLocation = new ResourceLocation(propertyPath);
                InputStream inputStream = rp.getInputStream(propertyLocation);

                if (inputStream == null)
                {
                    Config.warn("ConnectedTextures file not found: " + propertyPath);
                }
                else
                {
                    Properties properties = new PropertiesOrdered();
                    properties.load(inputStream);
                    inputStream.close();
                    ConnectedProperties connectedProperties = new ConnectedProperties(properties, propertyPath);

                    if (connectedProperties.isValid(propertyPath))
                    {
                        connectedProperties.updateIcons(textureMap);
                        addToTileList(connectedProperties, tilePropertyList);
                        addToBlockList(connectedProperties, blockPropertyList);
                    }
                }
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                Config.warn("ConnectedTextures file not found: " + propertyPath);
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }

        blockProperties = propertyListToArray(blockPropertyList);
        tileProperties = propertyListToArray(tilePropertyList);
        multipass = detectMultipass();
        Config.dbg("Multipass connected textures: " + multipass);
    }

    private static List makePropertyList(ConnectedProperties[][] propsArr)
    {
        List propertyList = new ArrayList();

        if (propsArr != null)
        {
            for (int propertyIndex = 0; propertyIndex < propsArr.length; ++propertyIndex)
            {
                ConnectedProperties[] connectedProperties = propsArr[propertyIndex];
                List propertiesForId = null;

                if (connectedProperties != null)
                {
                    propertiesForId = new ArrayList(Arrays.asList(connectedProperties));
                }

                propertyList.add(propertiesForId);
            }
        }

        return propertyList;
    }

    private static boolean detectMultipass()
    {
        List connectedPropertyList = new ArrayList();

        for (int propertyIndex = 0; propertyIndex < tileProperties.length; ++propertyIndex)
        {
            ConnectedProperties[] connectedProperties = tileProperties[propertyIndex];

            if (connectedProperties != null)
            {
                connectedPropertyList.addAll(Arrays.asList(connectedProperties));
            }
        }

        for (int propertyIndex = 0; propertyIndex < blockProperties.length; ++propertyIndex)
        {
            ConnectedProperties[] connectedProperties = blockProperties[propertyIndex];

            if (connectedProperties != null)
            {
                connectedPropertyList.addAll(Arrays.asList(connectedProperties));
            }
        }

        ConnectedProperties[] connectedPropertiesArray = (ConnectedProperties[])((ConnectedProperties[])connectedPropertyList.toArray(new ConnectedProperties[connectedPropertyList.size()]));
        Set matchTileIcons = new HashSet();
        Set tileIcons = new HashSet();

        for (int propertyIndex = 0; propertyIndex < connectedPropertiesArray.length; ++propertyIndex)
        {
            ConnectedProperties connectedProperties = connectedPropertiesArray[propertyIndex];

            if (connectedProperties.matchTileIcons != null)
            {
                matchTileIcons.addAll(Arrays.asList(connectedProperties.matchTileIcons));
            }

            if (connectedProperties.tileIcons != null)
            {
                tileIcons.addAll(Arrays.asList(connectedProperties.tileIcons));
            }
        }

        matchTileIcons.retainAll(tileIcons);
        return !matchTileIcons.isEmpty();
    }

    private static ConnectedProperties[][] propertyListToArray(List propertyList)
    {
        ConnectedProperties[][] propertiesArray = new ConnectedProperties[propertyList.size()][];

        for (int propertyIndex = 0; propertyIndex < propertyList.size(); ++propertyIndex)
        {
            List sublist = (List)propertyList.get(propertyIndex);

            if (sublist != null)
            {
                ConnectedProperties[] connectedProperties = (ConnectedProperties[])((ConnectedProperties[])sublist.toArray(new ConnectedProperties[sublist.size()]));
                propertiesArray[propertyIndex] = connectedProperties;
            }
        }

        return propertiesArray;
    }

    private static void addToTileList(ConnectedProperties cp, List tileList)
    {
        if (cp.matchTileIcons != null)
        {
            for (int iconIndex = 0; iconIndex < cp.matchTileIcons.length; ++iconIndex)
            {
                TextureAtlasSprite matchTileIcon = cp.matchTileIcons[iconIndex];

                if (!(matchTileIcon instanceof TextureAtlasSprite))
                {
                    Config.warn("TextureAtlasSprite is not TextureAtlasSprite: " + matchTileIcon + ", name: " + matchTileIcon.getIconName());
                }
                else
                {
                    int spriteIndex = matchTileIcon.getIndexInMap();

                    if (spriteIndex < 0)
                    {
                        Config.warn("Invalid tile ID: " + spriteIndex + ", icon: " + matchTileIcon.getIconName());
                    }
                    else
                    {
                        addToList(cp, tileList, spriteIndex);
                    }
                }
            }
        }
    }

    private static void addToBlockList(ConnectedProperties cp, List blockList)
    {
        if (cp.matchBlocks != null)
        {
            for (int matchBlockIndex = 0; matchBlockIndex < cp.matchBlocks.length; ++matchBlockIndex)
            {
                int blockId = cp.matchBlocks[matchBlockIndex].getBlockId();

                if (blockId < 0)
                {
                    Config.warn("Invalid block ID: " + blockId);
                }
                else
                {
                    addToList(cp, blockList, blockId);
                }
            }
        }
    }

    private static void addToList(ConnectedProperties cp, List lists, int id)
    {
        while (id >= lists.size())
        {
            lists.add(null);
        }

        List propertiesForId = (List)lists.get(id);

        if (propertiesForId == null)
        {
            propertiesForId = new ArrayList();
            lists.set(id, propertiesForId);
        }

        propertiesForId.add(cp);
    }

    private static String[] getDefaultCtmPaths()
    {
        List defaultCtmPaths = new ArrayList();
        String defaultCtmBasePath = "mcpatcher/ctm/default/";

        if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/glass.png")))
        {
            defaultCtmPaths.add(defaultCtmBasePath + "glass.properties");
            defaultCtmPaths.add(defaultCtmBasePath + "glasspane.properties");
        }

        if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/bookshelf.png")))
        {
            defaultCtmPaths.add(defaultCtmBasePath + "bookshelf.properties");
        }

        if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/sandstone_normal.png")))
        {
            defaultCtmPaths.add(defaultCtmBasePath + "sandstone.properties");
        }

        String[] colorNames = new String[] {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"};

        for (int colorIndex = 0; colorIndex < colorNames.length; ++colorIndex)
        {
            String colorName = colorNames[colorIndex];

            if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/glass_" + colorName + ".png")))
            {
                defaultCtmPaths.add(defaultCtmBasePath + colorIndex + "_glass_" + colorName + "/glass_" + colorName + ".properties");
                defaultCtmPaths.add(defaultCtmBasePath + colorIndex + "_glass_" + colorName + "/glass_pane_" + colorName + ".properties");
            }
        }

        String[] defaultCtmPathArray = (String[])((String[])defaultCtmPaths.toArray(new String[defaultCtmPaths.size()]));
        return defaultCtmPathArray;
    }
}
