package net.minecraft.client.renderer;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.optifine.CustomColors;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;

public class BlockFluidRenderer
{
    private TextureAtlasSprite[] atlasSpritesLava = new TextureAtlasSprite[2];
    private TextureAtlasSprite[] atlasSpritesWater = new TextureAtlasSprite[2];

    public BlockFluidRenderer()
    {
        this.initAtlasSprites();
    }

    protected void initAtlasSprites()
    {
        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        this.atlasSpritesLava[0] = textureMap.getAtlasSprite("minecraft:blocks/lava_still");
        this.atlasSpritesLava[1] = textureMap.getAtlasSprite("minecraft:blocks/lava_flow");
        this.atlasSpritesWater[0] = textureMap.getAtlasSprite("minecraft:blocks/water_still");
        this.atlasSpritesWater[1] = textureMap.getAtlasSprite("minecraft:blocks/water_flow");
    }

    public boolean renderFluid(IBlockAccess blockAccess, IBlockState blockStateIn, BlockPos blockPosIn, WorldRenderer worldRendererIn)
    {
        boolean renderedAnyFace;

        try
        {
            if (Config.isShaders())
            {
                SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
            }

            BlockLiquid blockLiquid = (BlockLiquid)blockStateIn.getBlock();
            blockLiquid.setBlockBoundsBasedOnState(blockAccess, blockPosIn);
            TextureAtlasSprite[] fluidSprites = blockLiquid.getMaterial() == Material.lava ? this.atlasSpritesLava : this.atlasSpritesWater;
            RenderEnv renderEnv = worldRendererIn.getRenderEnv(blockStateIn, blockPosIn);
            int fluidColor = CustomColors.getFluidColor(blockAccess, blockStateIn, blockPosIn, renderEnv);
            float red = (float)(fluidColor >> 16 & 255) / 255.0F;
            float green = (float)(fluidColor >> 8 & 255) / 255.0F;
            float blue = (float)(fluidColor & 255) / 255.0F;
            boolean renderTop = blockLiquid.shouldSideBeRendered(blockAccess, blockPosIn.up(), EnumFacing.UP);
            boolean renderBottom = blockLiquid.shouldSideBeRendered(blockAccess, blockPosIn.down(), EnumFacing.DOWN);
            boolean[] renderSide = renderEnv.getBorderFlags();
            renderSide[0] = blockLiquid.shouldSideBeRendered(blockAccess, blockPosIn.north(), EnumFacing.NORTH);
            renderSide[1] = blockLiquid.shouldSideBeRendered(blockAccess, blockPosIn.south(), EnumFacing.SOUTH);
            renderSide[2] = blockLiquid.shouldSideBeRendered(blockAccess, blockPosIn.west(), EnumFacing.WEST);
            renderSide[3] = blockLiquid.shouldSideBeRendered(blockAccess, blockPosIn.east(), EnumFacing.EAST);

            if (renderTop || renderBottom || renderSide[0] || renderSide[1] || renderSide[2] || renderSide[3])
            {
                renderedAnyFace = false;
                float fullBrightness = 1.0F;
                Material fluidMaterial = blockLiquid.getMaterial();
                float heightNorthWest = this.getFluidHeight(blockAccess, blockPosIn, fluidMaterial);
                float heightSouthWest = this.getFluidHeight(blockAccess, blockPosIn.south(), fluidMaterial);
                float heightSouthEast = this.getFluidHeight(blockAccess, blockPosIn.east().south(), fluidMaterial);
                float heightNorthEast = this.getFluidHeight(blockAccess, blockPosIn.east(), fluidMaterial);
                double xCoordinate = (double)blockPosIn.getX();
                double yCoordinate = (double)blockPosIn.getY();
                double zCoordinate = (double)blockPosIn.getZ();
                float renderEpsilon = 0.001F;

                if (renderTop)
                {
                    renderedAnyFace = true;
                    TextureAtlasSprite topSprite = fluidSprites[0];
                    float flowDirection = (float)BlockLiquid.getFlowDirection(blockAccess, blockPosIn, fluidMaterial);

                    if (flowDirection > -999.0F)
                    {
                        topSprite = fluidSprites[1];
                    }

                    worldRendererIn.setSprite(topSprite);
                    heightNorthWest -= renderEpsilon;
                    heightSouthWest -= renderEpsilon;
                    heightSouthEast -= renderEpsilon;
                    heightNorthEast -= renderEpsilon;
                    float topUNorthWest;
                    float topUSouthWest;
                    float topUSouthEast;
                    float topUNorthEast;
                    float topVNorthWest;
                    float topVSouthWest;
                    float topVSouthEast;
                    float topVNorthEast;

                    if (flowDirection < -999.0F)
                    {
                        topUNorthWest = topSprite.getInterpolatedU(0.0D);
                        topVNorthWest = topSprite.getInterpolatedV(0.0D);
                        topUSouthWest = topUNorthWest;
                        topVSouthWest = topSprite.getInterpolatedV(16.0D);
                        topUSouthEast = topSprite.getInterpolatedU(16.0D);
                        topVSouthEast = topVSouthWest;
                        topUNorthEast = topUSouthEast;
                        topVNorthEast = topVNorthWest;
                    }
                    else
                    {
                        float flowSin = MathHelper.sin(flowDirection) * 0.25F;
                        float flowCos = MathHelper.cos(flowDirection) * 0.25F;
                        topUNorthWest = topSprite.getInterpolatedU((double)(8.0F + (-flowCos - flowSin) * 16.0F));
                        topVNorthWest = topSprite.getInterpolatedV((double)(8.0F + (-flowCos + flowSin) * 16.0F));
                        topUSouthWest = topSprite.getInterpolatedU((double)(8.0F + (-flowCos + flowSin) * 16.0F));
                        topVSouthWest = topSprite.getInterpolatedV((double)(8.0F + (flowCos + flowSin) * 16.0F));
                        topUSouthEast = topSprite.getInterpolatedU((double)(8.0F + (flowCos + flowSin) * 16.0F));
                        topVSouthEast = topSprite.getInterpolatedV((double)(8.0F + (flowCos - flowSin) * 16.0F));
                        topUNorthEast = topSprite.getInterpolatedU((double)(8.0F + (flowCos - flowSin) * 16.0F));
                        topVNorthEast = topSprite.getInterpolatedV((double)(8.0F + (-flowCos - flowSin) * 16.0F));
                    }

                    int topBrightness = blockLiquid.getMixedBrightnessForBlock(blockAccess, blockPosIn);
                    int topLightU = topBrightness >> 16 & 65535;
                    int topLightV = topBrightness & 65535;
                    float topRed = fullBrightness * red;
                    float topGreen = fullBrightness * green;
                    float topBlue = fullBrightness * blue;
                    worldRendererIn.pos(xCoordinate + 0.0D, yCoordinate + (double)heightNorthWest, zCoordinate + 0.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUNorthWest, (double)topVNorthWest).lightmap(topLightU, topLightV).endVertex();
                    worldRendererIn.pos(xCoordinate + 0.0D, yCoordinate + (double)heightSouthWest, zCoordinate + 1.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUSouthWest, (double)topVSouthWest).lightmap(topLightU, topLightV).endVertex();
                    worldRendererIn.pos(xCoordinate + 1.0D, yCoordinate + (double)heightSouthEast, zCoordinate + 1.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUSouthEast, (double)topVSouthEast).lightmap(topLightU, topLightV).endVertex();
                    worldRendererIn.pos(xCoordinate + 1.0D, yCoordinate + (double)heightNorthEast, zCoordinate + 0.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUNorthEast, (double)topVNorthEast).lightmap(topLightU, topLightV).endVertex();

                    if (blockLiquid.shouldRenderSides(blockAccess, blockPosIn.up()))
                    {
                        worldRendererIn.pos(xCoordinate + 0.0D, yCoordinate + (double)heightNorthWest, zCoordinate + 0.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUNorthWest, (double)topVNorthWest).lightmap(topLightU, topLightV).endVertex();
                        worldRendererIn.pos(xCoordinate + 1.0D, yCoordinate + (double)heightNorthEast, zCoordinate + 0.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUNorthEast, (double)topVNorthEast).lightmap(topLightU, topLightV).endVertex();
                        worldRendererIn.pos(xCoordinate + 1.0D, yCoordinate + (double)heightSouthEast, zCoordinate + 1.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUSouthEast, (double)topVSouthEast).lightmap(topLightU, topLightV).endVertex();
                        worldRendererIn.pos(xCoordinate + 0.0D, yCoordinate + (double)heightSouthWest, zCoordinate + 1.0D).color(topRed, topGreen, topBlue, 1.0F).tex((double)topUSouthWest, (double)topVSouthWest).lightmap(topLightU, topLightV).endVertex();
                    }
                }

                if (renderBottom)
                {
                    worldRendererIn.setSprite(fluidSprites[0]);
                    float bottomMinU = fluidSprites[0].getMinU();
                    float bottomMaxU = fluidSprites[0].getMaxU();
                    float bottomMinV = fluidSprites[0].getMinV();
                    float bottomMaxV = fluidSprites[0].getMaxV();
                    int bottomBrightness = blockLiquid.getMixedBrightnessForBlock(blockAccess, blockPosIn.down());
                    int bottomLightU = bottomBrightness >> 16 & 65535;
                    int bottomLightV = bottomBrightness & 65535;
                    float bottomFaceBrightness = FaceBakery.getFaceBrightness(EnumFacing.DOWN);
                    worldRendererIn.pos(xCoordinate, yCoordinate, zCoordinate + 1.0D).color(red * bottomFaceBrightness, green * bottomFaceBrightness, blue * bottomFaceBrightness, 1.0F).tex((double)bottomMinU, (double)bottomMaxV).lightmap(bottomLightU, bottomLightV).endVertex();
                    worldRendererIn.pos(xCoordinate, yCoordinate, zCoordinate).color(red * bottomFaceBrightness, green * bottomFaceBrightness, blue * bottomFaceBrightness, 1.0F).tex((double)bottomMinU, (double)bottomMinV).lightmap(bottomLightU, bottomLightV).endVertex();
                    worldRendererIn.pos(xCoordinate + 1.0D, yCoordinate, zCoordinate).color(red * bottomFaceBrightness, green * bottomFaceBrightness, blue * bottomFaceBrightness, 1.0F).tex((double)bottomMaxU, (double)bottomMinV).lightmap(bottomLightU, bottomLightV).endVertex();
                    worldRendererIn.pos(xCoordinate + 1.0D, yCoordinate, zCoordinate + 1.0D).color(red * bottomFaceBrightness, green * bottomFaceBrightness, blue * bottomFaceBrightness, 1.0F).tex((double)bottomMaxU, (double)bottomMaxV).lightmap(bottomLightU, bottomLightV).endVertex();
                    renderedAnyFace = true;
                }

                for (int sideIndex = 0; sideIndex < 4; ++sideIndex)
                {
                    int sideXOffset = 0;
                    int sideZOffset = 0;

                    if (sideIndex == 0)
                    {
                        --sideZOffset;
                    }

                    if (sideIndex == 1)
                    {
                        ++sideZOffset;
                    }

                    if (sideIndex == 2)
                    {
                        --sideXOffset;
                    }

                    if (sideIndex == 3)
                    {
                        ++sideXOffset;
                    }

                    BlockPos sidePos = blockPosIn.add(sideXOffset, 0, sideZOffset);
                    TextureAtlasSprite sideSprite = fluidSprites[1];
                    worldRendererIn.setSprite(sideSprite);

                    if (renderSide[sideIndex])
                    {
                        float sideTopHeightStart;
                        float sideTopHeightEnd;
                        double sideXStart;
                        double sideZStart;
                        double sideXEnd;
                        double sideZEnd;

                        if (sideIndex == 0)
                        {
                            sideTopHeightStart = heightNorthWest;
                            sideTopHeightEnd = heightNorthEast;
                            sideXStart = xCoordinate;
                            sideXEnd = xCoordinate + 1.0D;
                            sideZStart = zCoordinate + (double)renderEpsilon;
                            sideZEnd = zCoordinate + (double)renderEpsilon;
                        }
                        else if (sideIndex == 1)
                        {
                            sideTopHeightStart = heightSouthEast;
                            sideTopHeightEnd = heightSouthWest;
                            sideXStart = xCoordinate + 1.0D;
                            sideXEnd = xCoordinate;
                            sideZStart = zCoordinate + 1.0D - (double)renderEpsilon;
                            sideZEnd = zCoordinate + 1.0D - (double)renderEpsilon;
                        }
                        else if (sideIndex == 2)
                        {
                            sideTopHeightStart = heightSouthWest;
                            sideTopHeightEnd = heightNorthWest;
                            sideXStart = xCoordinate + (double)renderEpsilon;
                            sideXEnd = xCoordinate + (double)renderEpsilon;
                            sideZStart = zCoordinate + 1.0D;
                            sideZEnd = zCoordinate;
                        }
                        else
                        {
                            sideTopHeightStart = heightNorthEast;
                            sideTopHeightEnd = heightSouthEast;
                            sideXStart = xCoordinate + 1.0D - (double)renderEpsilon;
                            sideXEnd = xCoordinate + 1.0D - (double)renderEpsilon;
                            sideZStart = zCoordinate;
                            sideZEnd = zCoordinate + 1.0D;
                        }

                        renderedAnyFace = true;
                        float sideMinU = sideSprite.getInterpolatedU(0.0D);
                        float sideMaxU = sideSprite.getInterpolatedU(8.0D);
                        float sideTopVStart = sideSprite.getInterpolatedV((double)((1.0F - sideTopHeightStart) * 16.0F * 0.5F));
                        float sideTopVEnd = sideSprite.getInterpolatedV((double)((1.0F - sideTopHeightEnd) * 16.0F * 0.5F));
                        float sideBottomV = sideSprite.getInterpolatedV(8.0D);
                        int sideBrightness = blockLiquid.getMixedBrightnessForBlock(blockAccess, sidePos);
                        int sideLightU = sideBrightness >> 16 & 65535;
                        int sideLightV = sideBrightness & 65535;
                        float sideFaceBrightness = sideIndex < 2 ? FaceBakery.getFaceBrightness(EnumFacing.NORTH) : FaceBakery.getFaceBrightness(EnumFacing.WEST);
                        float sideRed = fullBrightness * sideFaceBrightness * red;
                        float sideGreen = fullBrightness * sideFaceBrightness * green;
                        float sideBlue = fullBrightness * sideFaceBrightness * blue;
                        worldRendererIn.pos(sideXStart, yCoordinate + (double)sideTopHeightStart, sideZStart).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMinU, (double)sideTopVStart).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXEnd, yCoordinate + (double)sideTopHeightEnd, sideZEnd).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMaxU, (double)sideTopVEnd).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXEnd, yCoordinate + 0.0D, sideZEnd).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMaxU, (double)sideBottomV).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXStart, yCoordinate + 0.0D, sideZStart).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMinU, (double)sideBottomV).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXStart, yCoordinate + 0.0D, sideZStart).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMinU, (double)sideBottomV).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXEnd, yCoordinate + 0.0D, sideZEnd).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMaxU, (double)sideBottomV).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXEnd, yCoordinate + (double)sideTopHeightEnd, sideZEnd).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMaxU, (double)sideTopVEnd).lightmap(sideLightU, sideLightV).endVertex();
                        worldRendererIn.pos(sideXStart, yCoordinate + (double)sideTopHeightStart, sideZStart).color(sideRed, sideGreen, sideBlue, 1.0F).tex((double)sideMinU, (double)sideTopVStart).lightmap(sideLightU, sideLightV).endVertex();
                    }
                }

                worldRendererIn.setSprite((TextureAtlasSprite)null);
                return renderedAnyFace;
            }

            renderedAnyFace = false;
        }
        finally
        {
            if (Config.isShaders())
            {
                SVertexBuilder.popEntity(worldRendererIn);
            }
        }

        return renderedAnyFace;
    }

    private float getFluidHeight(IBlockAccess blockAccess, BlockPos blockPosIn, Material blockMaterial)
    {
        int heightSampleWeight = 0;
        float accumulatedHeight = 0.0F;

        for (int sampleIndex = 0; sampleIndex < 4; ++sampleIndex)
        {
            BlockPos samplePos = blockPosIn.add(-(sampleIndex & 1), 0, -(sampleIndex >> 1 & 1));

            if (blockAccess.getBlockState(samplePos.up()).getBlock().getMaterial() == blockMaterial)
            {
                return 1.0F;
            }

            IBlockState sampleState = blockAccess.getBlockState(samplePos);
            Material material = sampleState.getBlock().getMaterial();

            if (material != blockMaterial)
            {
                if (!material.isSolid())
                {
                    ++accumulatedHeight;
                    ++heightSampleWeight;
                }
            }
            else
            {
                int liquidLevel = ((Integer)sampleState.getValue(BlockLiquid.LEVEL)).intValue();

                if (liquidLevel >= 8 || liquidLevel == 0)
                {
                    accumulatedHeight += BlockLiquid.getLiquidHeightPercent(liquidLevel) * 10.0F;
                    heightSampleWeight += 10;
                }

                accumulatedHeight += BlockLiquid.getLiquidHeightPercent(liquidLevel);
                ++heightSampleWeight;
            }
        }

        return 1.0F - accumulatedHeight / (float)heightSampleWeight;
    }
}
