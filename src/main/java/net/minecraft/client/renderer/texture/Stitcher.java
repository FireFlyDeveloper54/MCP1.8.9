package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.util.MathHelper;

public class Stitcher
{
    private final int mipmapLevelStitcher;
    private final Set<Stitcher.Holder> setStitchHolders = Sets.<Stitcher.Holder>newHashSetWithExpectedSize(256);
    private final List<Stitcher.Slot> stitchSlots = Lists.<Stitcher.Slot>newArrayListWithCapacity(256);
    private int currentWidth;
    private int currentHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final boolean forcePowerOf2;
    private final int maxTileDimension;

    public Stitcher(int maxTextureWidth, int maxTextureHeight, boolean forcePowerOfTwo, int maxTileDimension, int mipmapLevel)
    {
        this.mipmapLevelStitcher = mipmapLevel;
        this.maxWidth = maxTextureWidth;
        this.maxHeight = maxTextureHeight;
        this.forcePowerOf2 = forcePowerOfTwo;
        this.maxTileDimension = maxTileDimension;
    }

    public int getCurrentWidth()
    {
        return this.currentWidth;
    }

    public int getCurrentHeight()
    {
        return this.currentHeight;
    }

    public void addSprite(TextureAtlasSprite sprite)
    {
        Stitcher.Holder holder = new Stitcher.Holder(sprite, this.mipmapLevelStitcher);

        if (this.maxTileDimension > 0)
        {
            holder.limitSizeTo(this.maxTileDimension);
        }

        this.setStitchHolders.add(holder);
    }

    public void doStitch()
    {
        Stitcher.Holder[] holders = this.setStitchHolders.toArray(new Stitcher.Holder[this.setStitchHolders.size()]);
        Arrays.sort((Object[])holders);

        for (Stitcher.Holder holder : holders)
        {
            if (!this.allocateSlot(holder))
            {
                String errorMessage = "Unable to fit: " + holder.getAtlasSprite().getIconName() + ", size: " + holder.getAtlasSprite().getIconWidth() + "x" + holder.getAtlasSprite().getIconHeight() + ", atlas: " + this.currentWidth + "x" + this.currentHeight + ", atlasMax: " + this.maxWidth + "x" + this.maxHeight + " - Maybe try a lower resolution resourcepack?";
                throw new StitcherException(holder, errorMessage);
            }
        }

        if (this.forcePowerOf2)
        {
            this.currentWidth = MathHelper.roundUpToPowerOfTwo(this.currentWidth);
            this.currentHeight = MathHelper.roundUpToPowerOfTwo(this.currentHeight);
        }
    }

    public List<TextureAtlasSprite> getStitchSlots()
    {
        List<Stitcher.Slot> slots = Lists.<Stitcher.Slot>newArrayList();

        for (Stitcher.Slot slot : this.stitchSlots)
        {
            slot.collectAllStitchSlots(slots);
        }

        List<TextureAtlasSprite> sprites = Lists.<TextureAtlasSprite>newArrayList();

        for (Stitcher.Slot slot : slots)
        {
            Stitcher.Holder holder = slot.getStitchHolder();
            TextureAtlasSprite atlasSprite = holder.getAtlasSprite();
            atlasSprite.initSprite(this.currentWidth, this.currentHeight, slot.getOriginX(), slot.getOriginY(), holder.isRotated());
            sprites.add(atlasSprite);
        }

        return sprites;
    }

    private static int getMipmapDimension(int dimension, int mipmapLevel)
    {
        return (dimension >> mipmapLevel) + ((dimension & (1 << mipmapLevel) - 1) == 0 ? 0 : 1) << mipmapLevel;
    }

    private boolean allocateSlot(Stitcher.Holder holder)
    {
        for (int slotIndex = 0; slotIndex < this.stitchSlots.size(); ++slotIndex)
        {
            Stitcher.Slot slot = this.stitchSlots.get(slotIndex);

            if (slot.addSlot(holder))
            {
                return true;
            }

            holder.rotate();

            if (slot.addSlot(holder))
            {
                return true;
            }

            holder.rotate();
        }

        return this.expandAndAllocateSlot(holder);
    }

    private boolean expandAndAllocateSlot(Stitcher.Holder holder)
    {
        int minHolderDimension = Math.min(holder.getWidth(), holder.getHeight());
        boolean emptyAtlas = this.currentWidth == 0 && this.currentHeight == 0;
        boolean expandWidth;

        if (this.forcePowerOf2)
        {
            int roundedWidth = MathHelper.roundUpToPowerOfTwo(this.currentWidth);
            int roundedHeight = MathHelper.roundUpToPowerOfTwo(this.currentHeight);
            int expandedRoundedWidth = MathHelper.roundUpToPowerOfTwo(this.currentWidth + minHolderDimension);
            int expandedRoundedHeight = MathHelper.roundUpToPowerOfTwo(this.currentHeight + minHolderDimension);
            boolean canExpandWidth = expandedRoundedWidth <= this.maxWidth;
            boolean canExpandHeight = expandedRoundedHeight <= this.maxHeight;

            if (!canExpandWidth && !canExpandHeight)
            {
                return false;
            }

            boolean widthWouldChange = roundedWidth != expandedRoundedWidth;
            boolean heightWouldChange = roundedHeight != expandedRoundedHeight;

            if (widthWouldChange ^ heightWouldChange)
            {
                expandWidth = !widthWouldChange;
            }
            else
            {
                expandWidth = canExpandWidth && roundedWidth <= roundedHeight;
            }
        }
        else
        {
            boolean canExpandWidth = this.currentWidth + minHolderDimension <= this.maxWidth;
            boolean canExpandHeight = this.currentHeight + minHolderDimension <= this.maxHeight;

            if (!canExpandWidth && !canExpandHeight)
            {
                return false;
            }

            expandWidth = canExpandWidth && (emptyAtlas || this.currentWidth <= this.currentHeight);
        }

        int maxHolderDimension = Math.max(holder.getWidth(), holder.getHeight());

        if (MathHelper.roundUpToPowerOfTwo((!expandWidth ? this.currentHeight : this.currentWidth) + maxHolderDimension) > (!expandWidth ? this.maxHeight : this.maxWidth))
        {
            return false;
        }
        else
        {
            Stitcher.Slot slot;

            if (expandWidth)
            {
                if (holder.getWidth() > holder.getHeight())
                {
                    holder.rotate();
                }

                if (this.currentHeight == 0)
                {
                    this.currentHeight = holder.getHeight();
                }

                slot = new Stitcher.Slot(this.currentWidth, 0, holder.getWidth(), this.currentHeight);
                this.currentWidth += holder.getWidth();
            }
            else
            {
                slot = new Stitcher.Slot(0, this.currentHeight, this.currentWidth, holder.getHeight());
                this.currentHeight += holder.getHeight();
            }

            slot.addSlot(holder);
            this.stitchSlots.add(slot);
            return true;
        }
    }

    public static class Holder implements Comparable<Stitcher.Holder>
    {
        private final TextureAtlasSprite theTexture;
        private final int width;
        private final int height;
        private final int mipmapLevelHolder;
        private boolean rotated;
        private float scaleFactor = 1.0F;

        public Holder(TextureAtlasSprite texture, int mipmapLevel)
        {
            this.theTexture = texture;
            this.width = texture.getIconWidth();
            this.height = texture.getIconHeight();
            this.mipmapLevelHolder = mipmapLevel;
            this.rotated = Stitcher.getMipmapDimension(this.height, mipmapLevel) > Stitcher.getMipmapDimension(this.width, mipmapLevel);
        }

        public TextureAtlasSprite getAtlasSprite()
        {
            return this.theTexture;
        }

        public int getWidth()
        {
            return this.rotated ? Stitcher.getMipmapDimension((int)((float)this.height * this.scaleFactor), this.mipmapLevelHolder) : Stitcher.getMipmapDimension((int)((float)this.width * this.scaleFactor), this.mipmapLevelHolder);
        }

        public int getHeight()
        {
            return this.rotated ? Stitcher.getMipmapDimension((int)((float)this.width * this.scaleFactor), this.mipmapLevelHolder) : Stitcher.getMipmapDimension((int)((float)this.height * this.scaleFactor), this.mipmapLevelHolder);
        }

        public void rotate()
        {
            this.rotated = !this.rotated;
        }

        public boolean isRotated()
        {
            return this.rotated;
        }

        public void limitSizeTo(int maxDimension)
        {
            if (this.width > maxDimension && this.height > maxDimension)
            {
                this.scaleFactor = (float)maxDimension / (float)Math.min(this.width, this.height);
            }
        }

        public String toString()
        {
            return "Holder{width=" + this.width + ", height=" + this.height + '}';
        }

        public int compareTo(Stitcher.Holder other)
        {
            int comparison;

            if (this.getHeight() == other.getHeight())
            {
                if (this.getWidth() == other.getWidth())
                {
                    if (this.theTexture.getIconName() == null)
                    {
                        return other.theTexture.getIconName() == null ? 0 : -1;
                    }

                    return this.theTexture.getIconName().compareTo(other.theTexture.getIconName());
                }

                comparison = this.getWidth() < other.getWidth() ? 1 : -1;
            }
            else
            {
                comparison = this.getHeight() < other.getHeight() ? 1 : -1;
            }

            return comparison;
        }
    }

    public static class Slot
    {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private List<Stitcher.Slot> subSlots;
        private Stitcher.Holder holder;

        public Slot(int originXIn, int originYIn, int widthIn, int heightIn)
        {
            this.originX = originXIn;
            this.originY = originYIn;
            this.width = widthIn;
            this.height = heightIn;
        }

        public Stitcher.Holder getStitchHolder()
        {
            return this.holder;
        }

        public int getOriginX()
        {
            return this.originX;
        }

        public int getOriginY()
        {
            return this.originY;
        }

        public boolean addSlot(Stitcher.Holder holderIn)
        {
            if (this.holder != null)
            {
                return false;
            }
            else
            {
                int holderWidth = holderIn.getWidth();
                int holderHeight = holderIn.getHeight();

                if (holderWidth <= this.width && holderHeight <= this.height)
                {
                    if (holderWidth == this.width && holderHeight == this.height)
                    {
                        this.holder = holderIn;
                        return true;
                    }
                    else
                    {
                        if (this.subSlots == null)
                        {
                            this.subSlots = Lists.<Stitcher.Slot>newArrayListWithCapacity(1);
                            this.subSlots.add(new Stitcher.Slot(this.originX, this.originY, holderWidth, holderHeight));
                            int remainingWidth = this.width - holderWidth;
                            int remainingHeight = this.height - holderHeight;

                            if (remainingHeight > 0 && remainingWidth > 0)
                            {
                                int verticalSplitScore = Math.max(this.height, remainingWidth);
                                int horizontalSplitScore = Math.max(this.width, remainingHeight);

                                if (verticalSplitScore >= horizontalSplitScore)
                                {
                                    this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + holderHeight, holderWidth, remainingHeight));
                                    this.subSlots.add(new Stitcher.Slot(this.originX + holderWidth, this.originY, remainingWidth, this.height));
                                }
                                else
                                {
                                    this.subSlots.add(new Stitcher.Slot(this.originX + holderWidth, this.originY, remainingWidth, holderHeight));
                                    this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + holderHeight, this.width, remainingHeight));
                                }
                            }
                            else if (remainingWidth == 0)
                            {
                                this.subSlots.add(new Stitcher.Slot(this.originX, this.originY + holderHeight, holderWidth, remainingHeight));
                            }
                            else if (remainingHeight == 0)
                            {
                                this.subSlots.add(new Stitcher.Slot(this.originX + holderWidth, this.originY, remainingWidth, holderHeight));
                            }
                        }

                        for (Stitcher.Slot subSlot : this.subSlots)
                        {
                            if (subSlot.addSlot(holderIn))
                            {
                                return true;
                            }
                        }

                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
        }

        public void collectAllStitchSlots(List<Stitcher.Slot> slots)
        {
            if (this.holder != null)
            {
                slots.add(this);
            }
            else if (this.subSlots != null)
            {
                for (Stitcher.Slot slot : this.subSlots)
                {
                    slot.collectAllStitchSlots(slots);
                }
            }
        }

        public String toString()
        {
            return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + '}';
        }
    }
}
