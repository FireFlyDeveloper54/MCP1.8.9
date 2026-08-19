package net.minecraft.client.renderer;

import java.util.BitSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.optifine.BetterSnow;
import net.optifine.CustomColors;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;

public class BlockModelRenderer
{
    private static float aoLightValueOpaque = 0.2F;
    private static boolean separateAoLightValue = false;
    private static final EnumWorldBlockLayer[] OVERLAY_LAYERS = new EnumWorldBlockLayer[] {EnumWorldBlockLayer.CUTOUT, EnumWorldBlockLayer.CUTOUT_MIPPED, EnumWorldBlockLayer.TRANSLUCENT};

    public BlockModelRenderer()
    {
            }

    public boolean renderModel(IBlockAccess blockAccessIn, IBakedModel modelIn, IBlockState blockStateIn, BlockPos blockPosIn, WorldRenderer worldRendererIn)
    {
        Block block = blockStateIn.getBlock();
        block.setBlockBoundsBasedOnState(blockAccessIn, blockPosIn);
        return this.renderModel(blockAccessIn, modelIn, blockStateIn, blockPosIn, worldRendererIn, true);
    }

    public boolean renderModel(IBlockAccess blockAccessIn, IBakedModel modelIn, IBlockState blockStateIn, BlockPos blockPosIn, WorldRenderer worldRendererIn, boolean checkSides)
    {
        boolean flag = Minecraft.isAmbientOcclusionEnabled() && blockStateIn.getBlock().getLightValue() == 0 && modelIn.isAmbientOcclusion();

        try
        {
            if (Config.isShaders())
            {
                SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccessIn, worldRendererIn);
            }

            RenderEnv renderEnv = worldRendererIn.getRenderEnv(blockStateIn, blockPosIn);
            modelIn = BlockModelCustomizer.getRenderModel(modelIn, blockStateIn, renderEnv);
            boolean flag1 = flag ? this.renderModelSmooth(blockAccessIn, modelIn, blockStateIn, blockPosIn, worldRendererIn, checkSides) : this.renderModelFlat(blockAccessIn, modelIn, blockStateIn, blockPosIn, worldRendererIn, checkSides);

            if (flag1)
            {
                this.renderOverlayModels(blockAccessIn, modelIn, blockStateIn, blockPosIn, worldRendererIn, checkSides, 0L, renderEnv, flag);
            }

            if (Config.isShaders())
            {
                SVertexBuilder.popEntity(worldRendererIn);
            }

            return flag1;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block model");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block model being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, blockPosIn, blockStateIn);
            crashreportcategory.addCrashSection("Using AO", Boolean.valueOf(flag));
            throw new ReportedException(crashreport);
        }
    }

    public boolean renderModelAmbientOcclusion(IBlockAccess blockAccessIn, IBakedModel modelIn, Block blockIn, BlockPos blockPosIn, WorldRenderer worldRendererIn, boolean checkSides)
    {
        IBlockState blockState = blockAccessIn.getBlockState(blockPosIn);
        return this.renderModelSmooth(blockAccessIn, modelIn, blockState, blockPosIn, worldRendererIn, checkSides);
    }

    private boolean renderModelSmooth(IBlockAccess blockAccess, IBakedModel model, IBlockState state, BlockPos pos, WorldRenderer worldRenderer, boolean checkSides)
    {
        boolean rendered = false;
        Block block = state.getBlock();
        RenderEnv renderEnv = worldRenderer.getRenderEnv(state, pos);
        EnumWorldBlockLayer blockLayer = worldRenderer.getBlockLayer();

        for (EnumFacing enumfacing : EnumFacing.VALUES)
        {
            List<BakedQuad> faceQuads = model.getFaceQuads(enumfacing);

            if (!faceQuads.isEmpty())
            {
                BlockPos sidePos = pos.offset(enumfacing);

                if (!checkSides || block.shouldSideBeRendered(blockAccess, sidePos, enumfacing))
                {
                    faceQuads = BlockModelCustomizer.getRenderQuads(faceQuads, blockAccess, state, pos, enumfacing, blockLayer, 0L, renderEnv);
                    this.renderQuadsSmooth(blockAccess, state, pos, worldRenderer, faceQuads, renderEnv);
                    rendered = true;
                }
            }
        }

        List<BakedQuad> generalQuads = model.getGeneralQuads();

        if (generalQuads.size() > 0)
        {
            generalQuads = BlockModelCustomizer.getRenderQuads(generalQuads, blockAccess, state, pos, (EnumFacing)null, blockLayer, 0L, renderEnv);
            this.renderQuadsSmooth(blockAccess, state, pos, worldRenderer, generalQuads, renderEnv);
            rendered = true;
        }

        return rendered;
    }

    public boolean renderModelStandard(IBlockAccess blockAccessIn, IBakedModel modelIn, Block blockIn, BlockPos blockPosIn, WorldRenderer worldRendererIn, boolean checkSides)
    {
        IBlockState blockState = blockAccessIn.getBlockState(blockPosIn);
        return this.renderModelFlat(blockAccessIn, modelIn, blockState, blockPosIn, worldRendererIn, checkSides);
    }

    public boolean renderModelFlat(IBlockAccess blockAccess, IBakedModel model, IBlockState state, BlockPos pos, WorldRenderer worldRenderer, boolean checkSides)
    {
        boolean rendered = false;
        Block block = state.getBlock();
        RenderEnv renderEnv = worldRenderer.getRenderEnv(state, pos);
        EnumWorldBlockLayer blockLayer = worldRenderer.getBlockLayer();

        for (EnumFacing enumfacing : EnumFacing.VALUES)
        {
            List<BakedQuad> faceQuads = model.getFaceQuads(enumfacing);

            if (!faceQuads.isEmpty())
            {
                BlockPos sidePos = pos.offset(enumfacing);

                if (!checkSides || block.shouldSideBeRendered(blockAccess, sidePos, enumfacing))
                {
                    int brightness = block.getMixedBrightnessForBlock(blockAccess, sidePos);
                    faceQuads = BlockModelCustomizer.getRenderQuads(faceQuads, blockAccess, state, pos, enumfacing, blockLayer, 0L, renderEnv);
                    this.renderQuadsFlat(blockAccess, state, pos, enumfacing, brightness, false, worldRenderer, faceQuads, renderEnv);
                    rendered = true;
                }
            }
        }

        List<BakedQuad> generalQuads = model.getGeneralQuads();

        if (generalQuads.size() > 0)
        {
            generalQuads = BlockModelCustomizer.getRenderQuads(generalQuads, blockAccess, state, pos, (EnumFacing)null, blockLayer, 0L, renderEnv);
            this.renderQuadsFlat(blockAccess, state, pos, (EnumFacing)null, -1, true, worldRenderer, generalQuads, renderEnv);
            rendered = true;
        }

        return rendered;
    }

    private void renderQuadsSmooth(IBlockAccess blockAccess, IBlockState state, BlockPos pos, WorldRenderer worldRenderer, List<BakedQuad> quads, RenderEnv renderEnv)
    {
        Block block = state.getBlock();
        float[] quadBounds = renderEnv.getQuadBounds();
        BitSet boundsFlags = renderEnv.getBoundsFlags();
        BlockModelRenderer.AmbientOcclusionFace aoFace = renderEnv.getAoFace();
        double xOffset = (double)pos.getX();
        double yOffset = (double)pos.getY();
        double zOffset = (double)pos.getZ();
        Block.EnumOffsetType offsetType = block.getOffsetType();

        if (offsetType != Block.EnumOffsetType.NONE)
        {
            long positionRandom = MathHelper.getPositionRandom(pos);
            xOffset += ((double)((float)(positionRandom >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
            zOffset += ((double)((float)(positionRandom >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;

            if (offsetType == Block.EnumOffsetType.XYZ)
            {
                yOffset += ((double)((float)(positionRandom >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
            }
        }

        for (BakedQuad bakedQuad : quads)
        {
            this.fillQuadBounds(block, bakedQuad.getVertexData(), bakedQuad.getFace(), quadBounds, boundsFlags);
            aoFace.updateVertexBrightness(blockAccess, block, pos, bakedQuad.getFace(), quadBounds, boundsFlags);

            if (bakedQuad.getSprite().isEmissive)
            {
                aoFace.setMaxBlockLight();
            }

            if (worldRenderer.isMultiTexture())
            {
                worldRenderer.addVertexData(bakedQuad.getVertexDataSingle());
            }
            else
            {
                worldRenderer.addVertexData(bakedQuad.getVertexData());
            }

            worldRenderer.putSprite(bakedQuad.getSprite());
            worldRenderer.putBrightness4(aoFace.vertexBrightness[0], aoFace.vertexBrightness[1], aoFace.vertexBrightness[2], aoFace.vertexBrightness[3]);
            int customColor = CustomColors.getColorMultiplier(bakedQuad, state, blockAccess, pos, renderEnv);

            if (!bakedQuad.hasTintIndex() && customColor == -1)
            {
                if (separateAoLightValue)
                {
                    worldRenderer.putColorMultiplierRgba(1.0F, 1.0F, 1.0F, aoFace.vertexColorMultiplier[0], 4);
                    worldRenderer.putColorMultiplierRgba(1.0F, 1.0F, 1.0F, aoFace.vertexColorMultiplier[1], 3);
                    worldRenderer.putColorMultiplierRgba(1.0F, 1.0F, 1.0F, aoFace.vertexColorMultiplier[2], 2);
                    worldRenderer.putColorMultiplierRgba(1.0F, 1.0F, 1.0F, aoFace.vertexColorMultiplier[3], 1);
                }
                else
                {
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[0], 4);
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[1], 3);
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[2], 2);
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[3], aoFace.vertexColorMultiplier[3], aoFace.vertexColorMultiplier[3], 1);
                }
            }
            else
            {
                int tintColor;

                if (customColor != -1)
                {
                    tintColor = customColor;
                }
                else
                {
                    tintColor = block.colorMultiplier(blockAccess, pos, bakedQuad.getTintIndex());
                }

                if (EntityRenderer.anaglyphEnable)
                {
                    tintColor = TextureUtil.anaglyphColor(tintColor);
                }

                float red = (float)(tintColor >> 16 & 255) / 255.0F;
                float green = (float)(tintColor >> 8 & 255) / 255.0F;
                float blue = (float)(tintColor & 255) / 255.0F;

                if (separateAoLightValue)
                {
                    worldRenderer.putColorMultiplierRgba(red, green, blue, aoFace.vertexColorMultiplier[0], 4);
                    worldRenderer.putColorMultiplierRgba(red, green, blue, aoFace.vertexColorMultiplier[1], 3);
                    worldRenderer.putColorMultiplierRgba(red, green, blue, aoFace.vertexColorMultiplier[2], 2);
                    worldRenderer.putColorMultiplierRgba(red, green, blue, aoFace.vertexColorMultiplier[3], 1);
                }
                else
                {
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[0] * red, aoFace.vertexColorMultiplier[0] * green, aoFace.vertexColorMultiplier[0] * blue, 4);
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[1] * red, aoFace.vertexColorMultiplier[1] * green, aoFace.vertexColorMultiplier[1] * blue, 3);
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[2] * red, aoFace.vertexColorMultiplier[2] * green, aoFace.vertexColorMultiplier[2] * blue, 2);
                    worldRenderer.putColorMultiplier(aoFace.vertexColorMultiplier[3] * red, aoFace.vertexColorMultiplier[3] * green, aoFace.vertexColorMultiplier[3] * blue, 1);
                }
            }

            worldRenderer.putPosition(xOffset, yOffset, zOffset);
        }
    }

    private void fillQuadBounds(Block blockIn, int[] vertexData, EnumFacing facingIn, float[] quadBounds, BitSet boundsFlags)
    {
        float f = 32.0F;
        float floatValue2 = 32.0F;
        float floatValue3 = 32.0F;
        float floatValue4 = -32.0F;
        float floatValue5 = -32.0F;
        float floatValue6 = -32.0F;
        int i = vertexData.length / 4;

        for (int j = 0; j < 4; ++j)
        {
            float floatValue7 = Float.intBitsToFloat(vertexData[j * i]);
            float floatValue8 = Float.intBitsToFloat(vertexData[j * i + 1]);
            float floatValue9 = Float.intBitsToFloat(vertexData[j * i + 2]);
            f = Math.min(f, floatValue7);
            floatValue2 = Math.min(floatValue2, floatValue8);
            floatValue3 = Math.min(floatValue3, floatValue9);
            floatValue4 = Math.max(floatValue4, floatValue7);
            floatValue5 = Math.max(floatValue5, floatValue8);
            floatValue6 = Math.max(floatValue6, floatValue9);
        }

        if (quadBounds != null)
        {
            quadBounds[EnumFacing.WEST.getIndex()] = f;
            quadBounds[EnumFacing.EAST.getIndex()] = floatValue4;
            quadBounds[EnumFacing.DOWN.getIndex()] = floatValue2;
            quadBounds[EnumFacing.UP.getIndex()] = floatValue5;
            quadBounds[EnumFacing.NORTH.getIndex()] = floatValue3;
            quadBounds[EnumFacing.SOUTH.getIndex()] = floatValue6;
            int k = EnumFacing.VALUES.length;
            quadBounds[EnumFacing.WEST.getIndex() + k] = 1.0F - f;
            quadBounds[EnumFacing.EAST.getIndex() + k] = 1.0F - floatValue4;
            quadBounds[EnumFacing.DOWN.getIndex() + k] = 1.0F - floatValue2;
            quadBounds[EnumFacing.UP.getIndex() + k] = 1.0F - floatValue5;
            quadBounds[EnumFacing.NORTH.getIndex() + k] = 1.0F - floatValue3;
            quadBounds[EnumFacing.SOUTH.getIndex() + k] = 1.0F - floatValue6;
        }

        float secondFloatValue = 1.0E-4F;
        float floatValue11 = 0.9999F;

        switch (facingIn)
        {
            case DOWN:
                boundsFlags.set(1, f >= 1.0E-4F || floatValue3 >= 1.0E-4F || floatValue4 <= 0.9999F || floatValue6 <= 0.9999F);
                boundsFlags.set(0, (floatValue2 < 1.0E-4F || blockIn.isFullCube()) && floatValue2 == floatValue5);
                break;

            case UP:
                boundsFlags.set(1, f >= 1.0E-4F || floatValue3 >= 1.0E-4F || floatValue4 <= 0.9999F || floatValue6 <= 0.9999F);
                boundsFlags.set(0, (floatValue5 > 0.9999F || blockIn.isFullCube()) && floatValue2 == floatValue5);
                break;

            case NORTH:
                boundsFlags.set(1, f >= 1.0E-4F || floatValue2 >= 1.0E-4F || floatValue4 <= 0.9999F || floatValue5 <= 0.9999F);
                boundsFlags.set(0, (floatValue3 < 1.0E-4F || blockIn.isFullCube()) && floatValue3 == floatValue6);
                break;

            case SOUTH:
                boundsFlags.set(1, f >= 1.0E-4F || floatValue2 >= 1.0E-4F || floatValue4 <= 0.9999F || floatValue5 <= 0.9999F);
                boundsFlags.set(0, (floatValue6 > 0.9999F || blockIn.isFullCube()) && floatValue3 == floatValue6);
                break;

            case WEST:
                boundsFlags.set(1, floatValue2 >= 1.0E-4F || floatValue3 >= 1.0E-4F || floatValue5 <= 0.9999F || floatValue6 <= 0.9999F);
                boundsFlags.set(0, (f < 1.0E-4F || blockIn.isFullCube()) && f == floatValue4);
                break;

            case EAST:
                boundsFlags.set(1, floatValue2 >= 1.0E-4F || floatValue3 >= 1.0E-4F || floatValue5 <= 0.9999F || floatValue6 <= 0.9999F);
                boundsFlags.set(0, (floatValue4 > 0.9999F || blockIn.isFullCube()) && f == floatValue4);
        }
    }

    private void renderQuadsFlat(IBlockAccess blockAccess, IBlockState state, BlockPos pos, EnumFacing face, int brightness, boolean calculateBrightness, WorldRenderer worldRenderer, List<BakedQuad> quads, RenderEnv renderEnv)
    {
        Block block = state.getBlock();
        BitSet boundsFlags = renderEnv.getBoundsFlags();
        double xOffset = (double)pos.getX();
        double yOffset = (double)pos.getY();
        double zOffset = (double)pos.getZ();
        Block.EnumOffsetType offsetType = block.getOffsetType();

        if (offsetType != Block.EnumOffsetType.NONE)
        {
            int x = pos.getX();
            int z = pos.getZ();
            long positionRandom = (long)(x * 3129871) ^ (long)z * 116129781L;
            positionRandom = positionRandom * positionRandom * 42317861L + positionRandom * 11L;
            xOffset += ((double)((float)(positionRandom >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
            zOffset += ((double)((float)(positionRandom >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;

            if (offsetType == Block.EnumOffsetType.XYZ)
            {
                yOffset += ((double)((float)(positionRandom >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
            }
        }

        for (BakedQuad bakedQuad : quads)
        {
            if (calculateBrightness)
            {
                this.fillQuadBounds(block, bakedQuad.getVertexData(), bakedQuad.getFace(), (float[])null, boundsFlags);
                brightness = boundsFlags.get(0) ? block.getMixedBrightnessForBlock(blockAccess, pos.offset(bakedQuad.getFace())) : block.getMixedBrightnessForBlock(blockAccess, pos);
            }

            if (bakedQuad.getSprite().isEmissive)
            {
                brightness |= 240;
            }

            if (worldRenderer.isMultiTexture())
            {
                worldRenderer.addVertexData(bakedQuad.getVertexDataSingle());
            }
            else
            {
                worldRenderer.addVertexData(bakedQuad.getVertexData());
            }

            worldRenderer.putSprite(bakedQuad.getSprite());
            worldRenderer.putBrightness4(brightness, brightness, brightness, brightness);
            int customColor = CustomColors.getColorMultiplier(bakedQuad, state, blockAccess, pos, renderEnv);

            if (bakedQuad.hasTintIndex() || customColor != -1)
            {
                int tintColor;

                if (customColor != -1)
                {
                    tintColor = customColor;
                }
                else
                {
                    tintColor = block.colorMultiplier(blockAccess, pos, bakedQuad.getTintIndex());
                }

                if (EntityRenderer.anaglyphEnable)
                {
                    tintColor = TextureUtil.anaglyphColor(tintColor);
                }

                float red = (float)(tintColor >> 16 & 255) / 255.0F;
                float green = (float)(tintColor >> 8 & 255) / 255.0F;
                float blue = (float)(tintColor & 255) / 255.0F;
                worldRenderer.putColorMultiplier(red, green, blue, 4);
                worldRenderer.putColorMultiplier(red, green, blue, 3);
                worldRenderer.putColorMultiplier(red, green, blue, 2);
                worldRenderer.putColorMultiplier(red, green, blue, 1);
            }

            worldRenderer.putPosition(xOffset, yOffset, zOffset);
        }
    }

    public void renderModelBrightnessColor(IBakedModel bakedModel, float brightness, float red, float green, float blue)
    {
        for (EnumFacing enumfacing : EnumFacing.VALUES)
        {
            this.renderModelBrightnessColorQuads(brightness, red, green, blue, bakedModel.getFaceQuads(enumfacing));
        }

        this.renderModelBrightnessColorQuads(brightness, red, green, blue, bakedModel.getGeneralQuads());
    }

    public void renderModelBrightness(IBakedModel model, IBlockState state, float brightness, boolean colorize)
    {
        Block block = state.getBlock();
        block.setBlockBoundsForItemRender();
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        int i = block.getRenderColor(block.getStateForEntityRender(state));

        if (EntityRenderer.anaglyphEnable)
        {
            i = TextureUtil.anaglyphColor(i);
        }

        float f = (float)(i >> 16 & 255) / 255.0F;
        float floatValue2 = (float)(i >> 8 & 255) / 255.0F;
        float floatValue3 = (float)(i & 255) / 255.0F;

        if (!colorize)
        {
            GlStateManager.color(brightness, brightness, brightness, 1.0F);
        }

        this.renderModelBrightnessColor(model, brightness, f, floatValue2, floatValue3);
    }

    private void renderModelBrightnessColorQuads(float brightness, float red, float green, float blue, List<BakedQuad> listQuads)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        for (BakedQuad bakedQuad : listQuads)
        {
            worldRenderer.begin(7, DefaultVertexFormats.ITEM);
            worldRenderer.addVertexData(bakedQuad.getVertexData());
            worldRenderer.putSprite(bakedQuad.getSprite());

            if (bakedQuad.hasTintIndex())
            {
                worldRenderer.putColorRGB_F4(red * brightness, green * brightness, blue * brightness);
            }
            else
            {
                worldRenderer.putColorRGB_F4(brightness, brightness, brightness);
            }

            Vec3i normal = bakedQuad.getFace().getDirectionVec();
            worldRenderer.putNormal((float)normal.getX(), (float)normal.getY(), (float)normal.getZ());
            tessellator.draw();
        }
    }

    public static float fixAoLightValue(float lightValue)
    {
        return lightValue == 0.2F ? aoLightValueOpaque : lightValue;
    }

    public static void updateAoLightValue()
    {
        aoLightValueOpaque = 1.0F - Config.getAmbientOcclusionLevel() * 0.8F;
        separateAoLightValue = Config.isShaders() && Shaders.isSeparateAo();
    }

    private void renderOverlayModels(IBlockAccess blockAccess, IBakedModel model, IBlockState state, BlockPos pos, WorldRenderer worldRenderer, boolean checkSides, long randomSeed, RenderEnv renderEnv, boolean useSmoothLighting)
    {
        if (renderEnv.isOverlaysRendered())
        {
            for (int layerIndex = 0; layerIndex < OVERLAY_LAYERS.length; ++layerIndex)
            {
                EnumWorldBlockLayer blockLayer = OVERLAY_LAYERS[layerIndex];
                ListQuadsOverlay overlayQuads = renderEnv.getListQuadsOverlay(blockLayer);

                if (overlayQuads.size() > 0)
                {
                    RegionRenderCacheBuilder renderCacheBuilder = renderEnv.getRegionRenderCacheBuilder();

                    if (renderCacheBuilder != null)
                    {
                        WorldRenderer overlayWorldRenderer = renderCacheBuilder.getWorldRendererByLayer(blockLayer);

                        if (!overlayWorldRenderer.isDrawing())
                        {
                            overlayWorldRenderer.begin(7, DefaultVertexFormats.BLOCK);
                            overlayWorldRenderer.setTranslation(worldRenderer.getXOffset(), worldRenderer.getYOffset(), worldRenderer.getZOffset());
                        }

                        for (int quadIndex = 0; quadIndex < overlayQuads.size(); ++quadIndex)
                        {
                            BakedQuad bakedQuad = overlayQuads.getQuad(quadIndex);
                            List<BakedQuad> singleQuad = overlayQuads.getListQuadsSingle(bakedQuad);
                            IBlockState overlayState = overlayQuads.getBlockState(quadIndex);

                            if (bakedQuad.getQuadEmissive() != null)
                            {
                                overlayQuads.addQuad(bakedQuad.getQuadEmissive(), overlayState);
                            }

                            renderEnv.reset(overlayState, pos);

                            if (useSmoothLighting)
                            {
                                this.renderQuadsSmooth(blockAccess, overlayState, pos, overlayWorldRenderer, singleQuad, renderEnv);
                            }
                            else
                            {
                                int brightness = overlayState.getBlock().getMixedBrightnessForBlock(blockAccess, pos.offset(bakedQuad.getFace()));
                                this.renderQuadsFlat(blockAccess, overlayState, pos, bakedQuad.getFace(), brightness, false, overlayWorldRenderer, singleQuad, renderEnv);
                            }
                        }
                    }

                    overlayQuads.clear();
                }
            }
        }

        if (Config.isBetterSnow() && !renderEnv.isBreakingAnimation() && BetterSnow.shouldRender(blockAccess, state, pos))
        {
            IBakedModel snowModel = BetterSnow.getModelSnowLayer();
            IBlockState snowState = BetterSnow.getStateSnowLayer();
            this.renderModel(blockAccess, snowModel, snowState, pos, worldRenderer, checkSides);
        }
    }

    public static class AmbientOcclusionFace
    {
        private final float[] vertexColorMultiplier;
        private final int[] vertexBrightness;

        public AmbientOcclusionFace()
        {
            this((BlockModelRenderer)null);
        }

        public AmbientOcclusionFace(BlockModelRenderer renderer)
        {
            this.vertexColorMultiplier = new float[4];
            this.vertexBrightness = new int[4];
        }

        public void setMaxBlockLight()
        {
            int i = 240;
            this.vertexBrightness[0] |= i;
            this.vertexBrightness[1] |= i;
            this.vertexBrightness[2] |= i;
            this.vertexBrightness[3] |= i;
            this.vertexColorMultiplier[0] = 1.0F;
            this.vertexColorMultiplier[1] = 1.0F;
            this.vertexColorMultiplier[2] = 1.0F;
            this.vertexColorMultiplier[3] = 1.0F;
        }

        public void updateVertexBrightness(IBlockAccess blockAccessIn, Block blockIn, BlockPos blockPosIn, EnumFacing facingIn, float[] quadBounds, BitSet boundsFlags)
        {
            BlockPos blockPos = boundsFlags.get(0) ? blockPosIn.offset(facingIn) : blockPosIn;
            BlockModelRenderer.EnumNeighborInfo neighborInfo = BlockModelRenderer.EnumNeighborInfo.getNeighbourInfo(facingIn);
            BlockPos blockpos1 = blockPos.offset(neighborInfo.neighborFacings[0]);
            BlockPos blockpos2 = blockPos.offset(neighborInfo.neighborFacings[1]);
            BlockPos blockpos3 = blockPos.offset(neighborInfo.neighborFacings[2]);
            BlockPos blockpos4 = blockPos.offset(neighborInfo.neighborFacings[3]);
            int i = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos1);
            int j = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos2);
            int k = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos3);
            int l = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos4);
            float f = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos1).getBlock().getAmbientOcclusionLightValue());
            float floatValue2 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos2).getBlock().getAmbientOcclusionLightValue());
            float floatValue3 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos3).getBlock().getAmbientOcclusionLightValue());
            float floatValue4 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos4).getBlock().getAmbientOcclusionLightValue());
            boolean flag = blockAccessIn.getBlockState(blockpos1.offset(facingIn)).getBlock().isTranslucent();
            boolean flag1 = blockAccessIn.getBlockState(blockpos2.offset(facingIn)).getBlock().isTranslucent();
            boolean flag2 = blockAccessIn.getBlockState(blockpos3.offset(facingIn)).getBlock().isTranslucent();
            boolean flag3 = blockAccessIn.getBlockState(blockpos4.offset(facingIn)).getBlock().isTranslucent();
            float floatValue5;
            int intValue2;

            if (!flag2 && !flag)
            {
                floatValue5 = f;
                intValue2 = i;
            }
            else
            {
                BlockPos blockpos5 = blockpos1.offset(neighborInfo.neighborFacings[2]);
                floatValue5 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos5).getBlock().getAmbientOcclusionLightValue());
                intValue2 = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos5);
            }

            int sixthIntValue;
            float floatValue6;

            if (!flag3 && !flag)
            {
                floatValue6 = f;
                sixthIntValue = i;
            }
            else
            {
                BlockPos blockpos6 = blockpos1.offset(neighborInfo.neighborFacings[3]);
                floatValue6 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos6).getBlock().getAmbientOcclusionLightValue());
                sixthIntValue = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos6);
            }

            int seventhIntValue;
            float floatValue7;

            if (!flag2 && !flag1)
            {
                floatValue7 = floatValue2;
                seventhIntValue = j;
            }
            else
            {
                BlockPos blockpos7 = blockpos2.offset(neighborInfo.neighborFacings[2]);
                floatValue7 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos7).getBlock().getAmbientOcclusionLightValue());
                seventhIntValue = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos7);
            }

            int eighthIntValue;
            float floatValue8;

            if (!flag3 && !flag1)
            {
                floatValue8 = floatValue2;
                eighthIntValue = j;
            }
            else
            {
                BlockPos blockpos8 = blockpos2.offset(neighborInfo.neighborFacings[3]);
                floatValue8 = BlockModelRenderer.fixAoLightValue(blockAccessIn.getBlockState(blockpos8).getBlock().getAmbientOcclusionLightValue());
                eighthIntValue = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockpos8);
            }

            int fifthIntValue = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockPosIn);

            if (boundsFlags.get(0) || !blockAccessIn.getBlockState(blockPosIn.offset(facingIn)).getBlock().isOpaqueCube())
            {
                fifthIntValue = blockIn.getMixedBrightnessForBlock(blockAccessIn, blockPosIn.offset(facingIn));
            }

            float floatValue = boundsFlags.get(0) ? blockAccessIn.getBlockState(blockPos).getBlock().getAmbientOcclusionLightValue() : blockAccessIn.getBlockState(blockPosIn).getBlock().getAmbientOcclusionLightValue();
            floatValue = BlockModelRenderer.fixAoLightValue(floatValue);
            BlockModelRenderer.VertexTranslations vertexTranslations = BlockModelRenderer.VertexTranslations.getVertexTranslations(facingIn);

            if (boundsFlags.get(1) && neighborInfo.doNonCubicWeighting)
            {
                float floatValue10 = (floatValue4 + f + floatValue6 + floatValue) * 0.25F;
                float floatValue11 = (floatValue3 + f + floatValue5 + floatValue) * 0.25F;
                float floatValue12 = (floatValue3 + floatValue2 + floatValue7 + floatValue) * 0.25F;
                float floatValue13 = (floatValue4 + floatValue2 + floatValue8 + floatValue) * 0.25F;
                float floatValue14 = quadBounds[neighborInfo.vertex0Bounds[0].boundsIndex] * quadBounds[neighborInfo.vertex0Bounds[1].boundsIndex];
                float floatValue15 = quadBounds[neighborInfo.vertex0Bounds[2].boundsIndex] * quadBounds[neighborInfo.vertex0Bounds[3].boundsIndex];
                float floatValue16 = quadBounds[neighborInfo.vertex0Bounds[4].boundsIndex] * quadBounds[neighborInfo.vertex0Bounds[5].boundsIndex];
                float floatValue17 = quadBounds[neighborInfo.vertex0Bounds[6].boundsIndex] * quadBounds[neighborInfo.vertex0Bounds[7].boundsIndex];
                float floatValue18 = quadBounds[neighborInfo.vertex1Bounds[0].boundsIndex] * quadBounds[neighborInfo.vertex1Bounds[1].boundsIndex];
                float floatValue19 = quadBounds[neighborInfo.vertex1Bounds[2].boundsIndex] * quadBounds[neighborInfo.vertex1Bounds[3].boundsIndex];
                float floatValue20 = quadBounds[neighborInfo.vertex1Bounds[4].boundsIndex] * quadBounds[neighborInfo.vertex1Bounds[5].boundsIndex];
                float floatValue21 = quadBounds[neighborInfo.vertex1Bounds[6].boundsIndex] * quadBounds[neighborInfo.vertex1Bounds[7].boundsIndex];
                float floatValue22 = quadBounds[neighborInfo.vertex2Bounds[0].boundsIndex] * quadBounds[neighborInfo.vertex2Bounds[1].boundsIndex];
                float floatValue23 = quadBounds[neighborInfo.vertex2Bounds[2].boundsIndex] * quadBounds[neighborInfo.vertex2Bounds[3].boundsIndex];
                float floatValue24 = quadBounds[neighborInfo.vertex2Bounds[4].boundsIndex] * quadBounds[neighborInfo.vertex2Bounds[5].boundsIndex];
                float floatValue25 = quadBounds[neighborInfo.vertex2Bounds[6].boundsIndex] * quadBounds[neighborInfo.vertex2Bounds[7].boundsIndex];
                float floatValue26 = quadBounds[neighborInfo.vertex3Bounds[0].boundsIndex] * quadBounds[neighborInfo.vertex3Bounds[1].boundsIndex];
                float floatValue27 = quadBounds[neighborInfo.vertex3Bounds[2].boundsIndex] * quadBounds[neighborInfo.vertex3Bounds[3].boundsIndex];
                float floatValue28 = quadBounds[neighborInfo.vertex3Bounds[4].boundsIndex] * quadBounds[neighborInfo.vertex3Bounds[5].boundsIndex];
                float floatValue29 = quadBounds[neighborInfo.vertex3Bounds[6].boundsIndex] * quadBounds[neighborInfo.vertex3Bounds[7].boundsIndex];
                this.vertexColorMultiplier[vertexTranslations.vertex0Index] = floatValue10 * floatValue14 + floatValue11 * floatValue15 + floatValue12 * floatValue16 + floatValue13 * floatValue17;
                this.vertexColorMultiplier[vertexTranslations.vertex1Index] = floatValue10 * floatValue18 + floatValue11 * floatValue19 + floatValue12 * floatValue20 + floatValue13 * floatValue21;
                this.vertexColorMultiplier[vertexTranslations.vertex2Index] = floatValue10 * floatValue22 + floatValue11 * floatValue23 + floatValue12 * floatValue24 + floatValue13 * floatValue25;
                this.vertexColorMultiplier[vertexTranslations.vertex3Index] = floatValue10 * floatValue26 + floatValue11 * floatValue27 + floatValue12 * floatValue28 + floatValue13 * floatValue29;
                int intValue4 = this.getAoBrightness(l, i, sixthIntValue, fifthIntValue);
                int intValue5 = this.getAoBrightness(k, i, intValue2, fifthIntValue);
                int intValue6 = this.getAoBrightness(k, j, seventhIntValue, fifthIntValue);
                int intValue7 = this.getAoBrightness(l, j, eighthIntValue, fifthIntValue);
                this.vertexBrightness[vertexTranslations.vertex0Index] = this.getVertexBrightness(intValue4, intValue5, intValue6, intValue7, floatValue14, floatValue15, floatValue16, floatValue17);
                this.vertexBrightness[vertexTranslations.vertex1Index] = this.getVertexBrightness(intValue4, intValue5, intValue6, intValue7, floatValue18, floatValue19, floatValue20, floatValue21);
                this.vertexBrightness[vertexTranslations.vertex2Index] = this.getVertexBrightness(intValue4, intValue5, intValue6, intValue7, floatValue22, floatValue23, floatValue24, floatValue25);
                this.vertexBrightness[vertexTranslations.vertex3Index] = this.getVertexBrightness(intValue4, intValue5, intValue6, intValue7, floatValue26, floatValue27, floatValue28, floatValue29);
            }
            else
            {
                float floatValue30 = (floatValue4 + f + floatValue6 + floatValue) * 0.25F;
                float floatValue31 = (floatValue3 + f + floatValue5 + floatValue) * 0.25F;
                float floatValue32 = (floatValue3 + floatValue2 + floatValue7 + floatValue) * 0.25F;
                float floatValue33 = (floatValue4 + floatValue2 + floatValue8 + floatValue) * 0.25F;
                this.vertexBrightness[vertexTranslations.vertex0Index] = this.getAoBrightness(l, i, sixthIntValue, fifthIntValue);
                this.vertexBrightness[vertexTranslations.vertex1Index] = this.getAoBrightness(k, i, intValue2, fifthIntValue);
                this.vertexBrightness[vertexTranslations.vertex2Index] = this.getAoBrightness(k, j, seventhIntValue, fifthIntValue);
                this.vertexBrightness[vertexTranslations.vertex3Index] = this.getAoBrightness(l, j, eighthIntValue, fifthIntValue);
                this.vertexColorMultiplier[vertexTranslations.vertex0Index] = floatValue30;
                this.vertexColorMultiplier[vertexTranslations.vertex1Index] = floatValue31;
                this.vertexColorMultiplier[vertexTranslations.vertex2Index] = floatValue32;
                this.vertexColorMultiplier[vertexTranslations.vertex3Index] = floatValue33;
            }
        }

        private int getAoBrightness(int intValue, int secondIntValue, int thirdIntValue, int fourthIntValue)
        {
            if (intValue == 0)
            {
                intValue = fourthIntValue;
            }

            if (secondIntValue == 0)
            {
                secondIntValue = fourthIntValue;
            }

            if (thirdIntValue == 0)
            {
                thirdIntValue = fourthIntValue;
            }

            return intValue + secondIntValue + thirdIntValue + fourthIntValue >> 2 & 16711935;
        }

        private int getVertexBrightness(int brightness1, int brightness2, int brightness3, int brightness4, float weight1, float weight2, float weight3, float weight4)
        {
            int highBits = (int)((float)(brightness1 >> 16 & 255) * weight1 + (float)(brightness2 >> 16 & 255) * weight2 + (float)(brightness3 >> 16 & 255) * weight3 + (float)(brightness4 >> 16 & 255) * weight4) & 255;
            int lowBits = (int)((float)(brightness1 & 255) * weight1 + (float)(brightness2 & 255) * weight2 + (float)(brightness3 & 255) * weight3 + (float)(brightness4 & 255) * weight4) & 255;
            return highBits << 16 | lowBits;
        }
    }

    public static enum EnumNeighborInfo
    {
        DOWN(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.5F, false, new BlockModelRenderer.Orientation[0], new BlockModelRenderer.Orientation[0], new BlockModelRenderer.Orientation[0], new BlockModelRenderer.Orientation[0]),
        UP(new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH}, 1.0F, false, new BlockModelRenderer.Orientation[0], new BlockModelRenderer.Orientation[0], new BlockModelRenderer.Orientation[0], new BlockModelRenderer.Orientation[0]),
        NORTH(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST}, 0.8F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_WEST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_EAST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_EAST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_WEST}),
        SOUTH(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP}, 0.8F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.WEST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.WEST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.EAST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.EAST}),
        WEST(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.SOUTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.SOUTH}),
        EAST(new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.SOUTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.SOUTH});

        protected final EnumFacing[] neighborFacings;
        protected final float shadeWeight;
        protected final boolean doNonCubicWeighting;
        protected final BlockModelRenderer.Orientation[] vertex0Bounds;
        protected final BlockModelRenderer.Orientation[] vertex1Bounds;
        protected final BlockModelRenderer.Orientation[] vertex2Bounds;
        protected final BlockModelRenderer.Orientation[] vertex3Bounds;
        private static final BlockModelRenderer.EnumNeighborInfo[] VALUES = new BlockModelRenderer.EnumNeighborInfo[6];

        private EnumNeighborInfo(EnumFacing[] neighborFacings, float shadeWeight, boolean doNonCubicWeighting, BlockModelRenderer.Orientation[] vertex0Bounds, BlockModelRenderer.Orientation[] vertex1Bounds, BlockModelRenderer.Orientation[] vertex2Bounds, BlockModelRenderer.Orientation[] vertex3Bounds)
        {
            this.neighborFacings = neighborFacings;
            this.shadeWeight = shadeWeight;
            this.doNonCubicWeighting = doNonCubicWeighting;
            this.vertex0Bounds = vertex0Bounds;
            this.vertex1Bounds = vertex1Bounds;
            this.vertex2Bounds = vertex2Bounds;
            this.vertex3Bounds = vertex3Bounds;
        }

        public static BlockModelRenderer.EnumNeighborInfo getNeighbourInfo(EnumFacing facing)
        {
            return VALUES[facing.getIndex()];
        }

        static {
            VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
            VALUES[EnumFacing.UP.getIndex()] = UP;
            VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
            VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
            VALUES[EnumFacing.WEST.getIndex()] = WEST;
            VALUES[EnumFacing.EAST.getIndex()] = EAST;
        }
    }

    public static enum Orientation
    {
        DOWN(EnumFacing.DOWN, false),
        UP(EnumFacing.UP, false),
        NORTH(EnumFacing.NORTH, false),
        SOUTH(EnumFacing.SOUTH, false),
        WEST(EnumFacing.WEST, false),
        EAST(EnumFacing.EAST, false),
        FLIP_DOWN(EnumFacing.DOWN, true),
        FLIP_UP(EnumFacing.UP, true),
        FLIP_NORTH(EnumFacing.NORTH, true),
        FLIP_SOUTH(EnumFacing.SOUTH, true),
        FLIP_WEST(EnumFacing.WEST, true),
        FLIP_EAST(EnumFacing.EAST, true);

        protected final int boundsIndex;

        private Orientation(EnumFacing facing, boolean flipped)
        {
            this.boundsIndex = facing.getIndex() + (flipped ? EnumFacing.VALUES.length : 0);
        }
    }

    static enum VertexTranslations
    {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vertex0Index;
        private final int vertex1Index;
        private final int vertex2Index;
        private final int vertex3Index;
        private static final BlockModelRenderer.VertexTranslations[] VALUES = new BlockModelRenderer.VertexTranslations[6];

        private VertexTranslations(int vertex0Index, int vertex1Index, int vertex2Index, int vertex3Index)
        {
            this.vertex0Index = vertex0Index;
            this.vertex1Index = vertex1Index;
            this.vertex2Index = vertex2Index;
            this.vertex3Index = vertex3Index;
        }

        public static BlockModelRenderer.VertexTranslations getVertexTranslations(EnumFacing facing)
        {
            return VALUES[facing.getIndex()];
        }

        static {
            VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
            VALUES[EnumFacing.UP.getIndex()] = UP;
            VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
            VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
            VALUES[EnumFacing.WEST.getIndex()] = WEST;
            VALUES[EnumFacing.EAST.getIndex()] = EAST;
        }
    }
}
