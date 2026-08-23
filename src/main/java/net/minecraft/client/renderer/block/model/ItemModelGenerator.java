package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.vector.Vector3f;

public class ItemModelGenerator
{
    public static final List<String> LAYERS = Lists.newArrayList(new String[] {"layer0", "layer1", "layer2", "layer3", "layer4"});

    public ModelBlock makeItemModel(TextureMap textureMapIn, ModelBlock blockModel)
    {
        Map<String, String> textures = Maps.<String, String>newHashMap();
        List<BlockPart> blockParts = Lists.<BlockPart>newArrayList();

        for (int layerIndex = 0; layerIndex < LAYERS.size(); ++layerIndex)
        {
            String layerName = LAYERS.get(layerIndex);

            if (!blockModel.isTexturePresent(layerName))
            {
                break;
            }

            String textureName = blockModel.resolveTextureName(layerName);
            textures.put(layerName, textureName);
            TextureAtlasSprite textureAtlasSprite = textureMapIn.getAtlasSprite((new ResourceLocation(textureName)).toString());
            blockParts.addAll(this.getBlockParts(layerIndex, layerName, textureAtlasSprite));
        }

        if (blockParts.isEmpty())
        {
            return null;
        }
        else
        {
            textures.put("particle", blockModel.isTexturePresent("particle") ? blockModel.resolveTextureName("particle") : textures.get("layer0"));
            return new ModelBlock(blockParts, textures, false, false, blockModel.getAllTransforms());
        }
    }

    private List<BlockPart> getBlockParts(int tintIndex, String textureName, TextureAtlasSprite sprite)
    {
        Map<EnumFacing, BlockPartFace> faces = Maps.<EnumFacing, BlockPartFace>newHashMap();
        faces.put(EnumFacing.SOUTH, new BlockPartFace((EnumFacing)null, tintIndex, textureName, new BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0)));
        faces.put(EnumFacing.NORTH, new BlockPartFace((EnumFacing)null, tintIndex, textureName, new BlockFaceUV(new float[] {16.0F, 0.0F, 0.0F, 16.0F}, 0)));
        List<BlockPart> blockParts = Lists.<BlockPart>newArrayList();
        blockParts.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), faces, (BlockPartRotation)null, true));
        blockParts.addAll(this.getSpanParts(sprite, textureName, tintIndex));
        return blockParts;
    }

    private List<BlockPart> getSpanParts(TextureAtlasSprite sprite, String textureName, int tintIndex)
    {
        float spriteWidth = (float)sprite.getIconWidth();
        float spriteHeight = (float)sprite.getIconHeight();
        List<BlockPart> blockParts = Lists.<BlockPart>newArrayList();

        for (ItemModelGenerator.Span span : this.getSpans(sprite))
        {
            float partMinX = 0.0F;
            float partMinY = 0.0F;
            float partMaxX = 0.0F;
            float partMaxY = 0.0F;
            float uvMinU = 0.0F;
            float uvMaxU = 0.0F;
            float uvMinV = 0.0F;
            float uvMaxV = 0.0F;
            float uvScaleU = 0.0F;
            float uvScaleV = 0.0F;
            float spanMin = (float)span.getMin();
            float spanMax = (float)span.getMax();
            float spanAnchor = (float)span.getAnchor();
            ItemModelGenerator.SpanFacing spanFacing = span.getFacing();

            switch (spanFacing)
            {
                case UP:
                    uvMinU = spanMin;
                    partMinX = spanMin;
                    partMaxX = uvMaxU = spanMax + 1.0F;
                    uvMinV = spanAnchor;
                    partMinY = spanAnchor;
                    uvMaxV = spanAnchor;
                    partMaxY = spanAnchor;
                    uvScaleU = 16.0F / spriteWidth;
                    uvScaleV = 16.0F / (spriteHeight - 1.0F);
                    break;

                case DOWN:
                    uvMaxV = spanAnchor;
                    uvMinV = spanAnchor;
                    uvMinU = spanMin;
                    partMinX = spanMin;
                    partMaxX = uvMaxU = spanMax + 1.0F;
                    partMinY = spanAnchor + 1.0F;
                    partMaxY = spanAnchor + 1.0F;
                    uvScaleU = 16.0F / spriteWidth;
                    uvScaleV = 16.0F / (spriteHeight - 1.0F);
                    break;

                case LEFT:
                    uvMinU = spanAnchor;
                    partMinX = spanAnchor;
                    uvMaxU = spanAnchor;
                    partMaxX = spanAnchor;
                    uvMaxV = spanMin;
                    partMinY = spanMin;
                    partMaxY = uvMinV = spanMax + 1.0F;
                    uvScaleU = 16.0F / (spriteWidth - 1.0F);
                    uvScaleV = 16.0F / spriteHeight;
                    break;

                case RIGHT:
                    uvMaxU = spanAnchor;
                    uvMinU = spanAnchor;
                    partMinX = spanAnchor + 1.0F;
                    partMaxX = spanAnchor + 1.0F;
                    uvMaxV = spanMin;
                    partMinY = spanMin;
                    partMaxY = uvMinV = spanMax + 1.0F;
                    uvScaleU = 16.0F / (spriteWidth - 1.0F);
                    uvScaleV = 16.0F / spriteHeight;
            }

            float partScaleX = 16.0F / spriteWidth;
            float partScaleY = 16.0F / spriteHeight;
            partMinX = partMinX * partScaleX;
            partMaxX = partMaxX * partScaleX;
            partMinY = partMinY * partScaleY;
            partMaxY = partMaxY * partScaleY;
            partMinY = 16.0F - partMinY;
            partMaxY = 16.0F - partMaxY;
            uvMinU = uvMinU * uvScaleU;
            uvMaxU = uvMaxU * uvScaleU;
            uvMinV = uvMinV * uvScaleV;
            uvMaxV = uvMaxV * uvScaleV;
            Map<EnumFacing, BlockPartFace> faces = Maps.<EnumFacing, BlockPartFace>newHashMap();
            faces.put(spanFacing.getFacing(), new BlockPartFace((EnumFacing)null, tintIndex, textureName, new BlockFaceUV(new float[] {uvMinU, uvMinV, uvMaxU, uvMaxV}, 0)));

            switch (spanFacing)
            {
                case UP:
                    blockParts.add(new BlockPart(new Vector3f(partMinX, partMinY, 7.5F), new Vector3f(partMaxX, partMinY, 8.5F), faces, (BlockPartRotation)null, true));
                    break;

                case DOWN:
                    blockParts.add(new BlockPart(new Vector3f(partMinX, partMaxY, 7.5F), new Vector3f(partMaxX, partMaxY, 8.5F), faces, (BlockPartRotation)null, true));
                    break;

                case LEFT:
                    blockParts.add(new BlockPart(new Vector3f(partMinX, partMinY, 7.5F), new Vector3f(partMinX, partMaxY, 8.5F), faces, (BlockPartRotation)null, true));
                    break;

                case RIGHT:
                    blockParts.add(new BlockPart(new Vector3f(partMaxX, partMinY, 7.5F), new Vector3f(partMaxX, partMaxY, 8.5F), faces, (BlockPartRotation)null, true));
            }
        }

        return blockParts;
    }

    private List<ItemModelGenerator.Span> getSpans(TextureAtlasSprite sprite)
    {
        int width = sprite.getIconWidth();
        int height = sprite.getIconHeight();
        List<ItemModelGenerator.Span> spans = Lists.<ItemModelGenerator.Span>newArrayList();

        for (int frameIndex = 0; frameIndex < sprite.getFrameCount(); ++frameIndex)
        {
            int[] pixels = sprite.getFrameTextureData(frameIndex)[0];

            for (int pixelY = 0; pixelY < height; ++pixelY)
            {
                for (int pixelX = 0; pixelX < width; ++pixelX)
                {
                    boolean opaque = !this.isTransparent(pixels, pixelX, pixelY, width, height);
                    this.checkTransition(ItemModelGenerator.SpanFacing.UP, spans, pixels, pixelX, pixelY, width, height, opaque);
                    this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, spans, pixels, pixelX, pixelY, width, height, opaque);
                    this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, spans, pixels, pixelX, pixelY, width, height, opaque);
                    this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, spans, pixels, pixelX, pixelY, width, height, opaque);
                }
            }
        }

        return spans;
    }

    private void checkTransition(ItemModelGenerator.SpanFacing spanFacing, List<ItemModelGenerator.Span> spans, int[] pixels, int x, int y, int width, int height, boolean opaque)
    {
        boolean hasTransparentNeighbor = this.isTransparent(pixels, x + spanFacing.getXOffset(), y + spanFacing.getYOffset(), width, height) && opaque;

        if (hasTransparentNeighbor)
        {
            this.createOrExpandSpan(spans, spanFacing, x, y);
        }
    }

    private void createOrExpandSpan(List<ItemModelGenerator.Span> spans, ItemModelGenerator.SpanFacing spanFacing, int x, int y)
    {
        ItemModelGenerator.Span matchingSpan = null;

        for (ItemModelGenerator.Span span : spans)
        {
            if (span.getFacing() == spanFacing)
            {
                int anchor = spanFacing.isHorizontal() ? y : x;

                if (span.getAnchor() == anchor)
                {
                    matchingSpan = span;
                    break;
                }
            }
        }

        int anchor = spanFacing.isHorizontal() ? y : x;
        int spanPosition = spanFacing.isHorizontal() ? x : y;

        if (matchingSpan == null)
        {
            spans.add(new ItemModelGenerator.Span(spanFacing, spanPosition, anchor));
        }
        else
        {
            matchingSpan.expand(spanPosition);
        }
    }

    private boolean isTransparent(int[] pixels, int x, int y, int width, int height)
    {
        return x >= 0 && y >= 0 && x < width && y < height ? (pixels[y * width + x] >> 24 & 255) == 0 : true;
    }

    static class Span
    {
        private final ItemModelGenerator.SpanFacing spanFacing;
        private int min;
        private int max;
        private final int anchor;

        public Span(ItemModelGenerator.SpanFacing spanFacingIn, int min, int anchor)
        {
            this.spanFacing = spanFacingIn;
            this.min = min;
            this.max = min;
            this.anchor = anchor;
        }

        public void expand(int value)
        {
            if (value < this.min)
            {
                this.min = value;
            }
            else if (value > this.max)
            {
                this.max = value;
            }
        }

        public ItemModelGenerator.SpanFacing getFacing()
        {
            return this.spanFacing;
        }

        public int getMin()
        {
            return this.min;
        }

        public int getMax()
        {
            return this.max;
        }

        public int getAnchor()
        {
            return this.anchor;
        }
    }

    static enum SpanFacing
    {
        UP(EnumFacing.UP, 0, -1),
        DOWN(EnumFacing.DOWN, 0, 1),
        LEFT(EnumFacing.EAST, -1, 0),
        RIGHT(EnumFacing.WEST, 1, 0);

        private final EnumFacing facing;
        private final int xOffset;
        private final int yOffset;

        private SpanFacing(EnumFacing facing, int xOffset, int yOffset)
        {
            this.facing = facing;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public EnumFacing getFacing()
        {
            return this.facing;
        }

        public int getXOffset()
        {
            return this.xOffset;
        }

        public int getYOffset()
        {
            return this.yOffset;
        }

        private boolean isHorizontal()
        {
            return this == DOWN || this == UP;
        }
    }
}
