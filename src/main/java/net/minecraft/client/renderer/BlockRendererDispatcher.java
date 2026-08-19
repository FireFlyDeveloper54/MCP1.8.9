package net.minecraft.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;

public class BlockRendererDispatcher implements IResourceManagerReloadListener
{
    private BlockModelShapes blockModelShapes;
    private final GameSettings gameSettings;
    private final BlockModelRenderer blockModelRenderer = new BlockModelRenderer();
    private final ChestRenderer chestRenderer = new ChestRenderer();
    private final BlockFluidRenderer fluidRenderer = new BlockFluidRenderer();

    public BlockRendererDispatcher(BlockModelShapes blockModelShapesIn, GameSettings gameSettingsIn)
    {
        this.blockModelShapes = blockModelShapesIn;
        this.gameSettings = gameSettingsIn;
    }

    public BlockModelShapes getBlockModelShapes()
    {
        return this.blockModelShapes;
    }

    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess)
    {
        Block block = state.getBlock();
        int renderType = block.getRenderType();

        if (renderType == 3)
        {
            state = block.getActualState(state, blockAccess, pos);
            IBakedModel bakedModel = this.blockModelShapes.getModelForState(state);
            IBakedModel damageModel = (new SimpleBakedModel.Builder(bakedModel, texture)).makeBakedModel();
            this.blockModelRenderer.renderModel(blockAccess, damageModel, state, pos, Tessellator.getInstance().getWorldRenderer());
        }
    }

    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, WorldRenderer worldRendererIn)
    {
        try
        {
            int renderType = state.getBlock().getRenderType();

            if (renderType == -1)
            {
                return false;
            }
            else
            {
                switch (renderType)
                {
                    case 1:
                        return this.fluidRenderer.renderFluid(blockAccess, state, pos, worldRendererIn);

                    case 2:
                        return false;

                    case 3:
                        IBakedModel bakedModel = this.getModelFromBlockState(state, blockAccess, pos);
                        return this.blockModelRenderer.renderModel(blockAccess, bakedModel, state, pos, worldRendererIn);

                    default:
                        return false;
                }
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashReportCategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
            throw new ReportedException(crashReport);
        }
    }

    public BlockModelRenderer getBlockModelRenderer()
    {
        return this.blockModelRenderer;
    }

    private IBakedModel getBakedModel(IBlockState state, BlockPos pos)
    {
        IBakedModel bakedModel = this.blockModelShapes.getModelForState(state);

        if (pos != null && this.gameSettings.allowBlockAlternatives && bakedModel instanceof WeightedBakedModel)
        {
            bakedModel = ((WeightedBakedModel)bakedModel).getAlternativeModel(MathHelper.getPositionRandom(pos));
        }

        return bakedModel;
    }

    public IBakedModel getModelFromBlockState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        Block block = state.getBlock();

        if (worldIn.getWorldType() != WorldType.DEBUG_WORLD)
        {
            try
            {
                state = block.getActualState(state, worldIn, pos);
            }
            catch (Exception exception)
            {
                ;
            }
        }

        IBakedModel bakedModel = this.blockModelShapes.getModelForState(state);

        if (pos != null && this.gameSettings.allowBlockAlternatives && bakedModel instanceof WeightedBakedModel)
        {
            bakedModel = ((WeightedBakedModel)bakedModel).getAlternativeModel(MathHelper.getPositionRandom(pos));
        }

        return bakedModel;
    }

    public void renderBlockBrightness(IBlockState state, float brightness)
    {
        int renderType = state.getBlock().getRenderType();

        if (renderType != -1)
        {
            switch (renderType)
            {
                case 1:
                default:
                    break;

                case 2:
                    this.chestRenderer.renderChestBrightness(state.getBlock(), brightness);
                    break;

                case 3:
                    IBakedModel bakedModel = this.getBakedModel(state, (BlockPos)null);
                    this.blockModelRenderer.renderModelBrightness(bakedModel, state, brightness, true);
            }
        }
    }

    public boolean isRenderTypeChest(Block block, int metadata)
    {
        if (block == null)
        {
            return false;
        }
        else
        {
            int renderType = block.getRenderType();
            return renderType == 3 ? false : renderType == 2;
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.fluidRenderer.initAtlasSprites();
    }
}
