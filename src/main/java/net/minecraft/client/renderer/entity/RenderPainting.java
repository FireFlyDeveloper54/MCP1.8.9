package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderPainting extends Render<EntityPainting>
{
    private static final ResourceLocation KRISTOFFER_PAINTING_TEXTURE = new ResourceLocation("textures/painting/paintings_kristoffer_zetterstrand.png");
    private final BlockPos.MutableBlockPos lightmapPos = new BlockPos.MutableBlockPos();

    public RenderPainting(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    public void doRender(EntityPainting entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.enableRescaleNormal();
        this.bindEntityTexture(entity);
        EntityPainting.EnumArt art = entity.art;
        float modelScale = 0.0625F;
        GlStateManager.scale(modelScale, modelScale, modelScale);
        this.renderPainting(entity, art.sizeX, art.sizeY, art.offsetX, art.offsetY);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityPainting entity)
    {
        return KRISTOFFER_PAINTING_TEXTURE;
    }

    private void renderPainting(EntityPainting painting, int width, int height, int textureU, int textureV)
    {
        float paintingLeft = (float)(-width) / 2.0F;
        float paintingBottom = (float)(-height) / 2.0F;
        float halfThickness = 0.5F;
        float backMinU = 0.75F;
        float backMaxU = 0.8125F;
        float backMinV = 0.0F;
        float backMaxV = 0.0625F;
        float horizontalEdgeMinU = 0.75F;
        float horizontalEdgeMaxU = 0.8125F;
        float horizontalEdgeMinV = 0.001953125F;
        float horizontalEdgeMaxV = 0.001953125F;
        float verticalEdgeMinU = 0.7519531F;
        float verticalEdgeMaxU = 0.7519531F;
        float verticalEdgeMinV = 0.0F;
        float verticalEdgeMaxV = 0.0625F;

        for (int tileX = 0; tileX < width / 16; ++tileX)
        {
            for (int tileY = 0; tileY < height / 16; ++tileY)
            {
                float tileRight = paintingLeft + (float)((tileX + 1) * 16);
                float tileLeft = paintingLeft + (float)(tileX * 16);
                float tileTop = paintingBottom + (float)((tileY + 1) * 16);
                float tileBottom = paintingBottom + (float)(tileY * 16);
                this.setLightmap(painting, (tileRight + tileLeft) / 2.0F, (tileTop + tileBottom) / 2.0F);
                float tileArtMaxU = (float)(textureU + width - tileX * 16) / 256.0F;
                float tileArtMinU = (float)(textureU + width - (tileX + 1) * 16) / 256.0F;
                float tileArtMaxV = (float)(textureV + height - tileY * 16) / 256.0F;
                float tileArtMinV = (float)(textureV + height - (tileY + 1) * 16) / 256.0F;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
                worldRenderer.pos((double)tileRight, (double)tileBottom, (double)(-halfThickness)).tex((double)tileArtMinU, (double)tileArtMaxV).normal(0.0F, 0.0F, -1.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileBottom, (double)(-halfThickness)).tex((double)tileArtMaxU, (double)tileArtMaxV).normal(0.0F, 0.0F, -1.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileTop, (double)(-halfThickness)).tex((double)tileArtMaxU, (double)tileArtMinV).normal(0.0F, 0.0F, -1.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileTop, (double)(-halfThickness)).tex((double)tileArtMinU, (double)tileArtMinV).normal(0.0F, 0.0F, -1.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileTop, (double)halfThickness).tex((double)backMinU, (double)backMinV).normal(0.0F, 0.0F, 1.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileTop, (double)halfThickness).tex((double)backMaxU, (double)backMinV).normal(0.0F, 0.0F, 1.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileBottom, (double)halfThickness).tex((double)backMaxU, (double)backMaxV).normal(0.0F, 0.0F, 1.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileBottom, (double)halfThickness).tex((double)backMinU, (double)backMaxV).normal(0.0F, 0.0F, 1.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileTop, (double)(-halfThickness)).tex((double)horizontalEdgeMinU, (double)horizontalEdgeMinV).normal(0.0F, 1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileTop, (double)(-halfThickness)).tex((double)horizontalEdgeMaxU, (double)horizontalEdgeMinV).normal(0.0F, 1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileTop, (double)halfThickness).tex((double)horizontalEdgeMaxU, (double)horizontalEdgeMaxV).normal(0.0F, 1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileTop, (double)halfThickness).tex((double)horizontalEdgeMinU, (double)horizontalEdgeMaxV).normal(0.0F, 1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileBottom, (double)halfThickness).tex((double)horizontalEdgeMinU, (double)horizontalEdgeMinV).normal(0.0F, -1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileBottom, (double)halfThickness).tex((double)horizontalEdgeMaxU, (double)horizontalEdgeMinV).normal(0.0F, -1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileBottom, (double)(-halfThickness)).tex((double)horizontalEdgeMaxU, (double)horizontalEdgeMaxV).normal(0.0F, -1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileBottom, (double)(-halfThickness)).tex((double)horizontalEdgeMinU, (double)horizontalEdgeMaxV).normal(0.0F, -1.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileTop, (double)halfThickness).tex((double)verticalEdgeMaxU, (double)verticalEdgeMinV).normal(-1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileBottom, (double)halfThickness).tex((double)verticalEdgeMaxU, (double)verticalEdgeMaxV).normal(-1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileBottom, (double)(-halfThickness)).tex((double)verticalEdgeMinU, (double)verticalEdgeMaxV).normal(-1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileRight, (double)tileTop, (double)(-halfThickness)).tex((double)verticalEdgeMinU, (double)verticalEdgeMinV).normal(-1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileTop, (double)(-halfThickness)).tex((double)verticalEdgeMaxU, (double)verticalEdgeMinV).normal(1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileBottom, (double)(-halfThickness)).tex((double)verticalEdgeMaxU, (double)verticalEdgeMaxV).normal(1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileBottom, (double)halfThickness).tex((double)verticalEdgeMinU, (double)verticalEdgeMaxV).normal(1.0F, 0.0F, 0.0F).endVertex();
                worldRenderer.pos((double)tileLeft, (double)tileTop, (double)halfThickness).tex((double)verticalEdgeMinU, (double)verticalEdgeMinV).normal(1.0F, 0.0F, 0.0F).endVertex();
                tessellator.draw();
            }
        }
    }

    private void setLightmap(EntityPainting painting, float offsetX, float offsetY)
    {
        int blockX = MathHelper.floor_double(painting.posX);
        int blockY = MathHelper.floor_double(painting.posY + (double)(offsetY / 16.0F));
        int blockZ = MathHelper.floor_double(painting.posZ);
        EnumFacing facing = painting.facingDirection;

        if (facing == EnumFacing.NORTH)
        {
            blockX = MathHelper.floor_double(painting.posX + (double)(offsetX / 16.0F));
        }

        if (facing == EnumFacing.WEST)
        {
            blockZ = MathHelper.floor_double(painting.posZ - (double)(offsetX / 16.0F));
        }

        if (facing == EnumFacing.SOUTH)
        {
            blockX = MathHelper.floor_double(painting.posX - (double)(offsetX / 16.0F));
        }

        if (facing == EnumFacing.EAST)
        {
            blockZ = MathHelper.floor_double(painting.posZ + (double)(offsetX / 16.0F));
        }

        int packedLight = this.renderManager.worldObj.getCombinedLight(this.lightmapPos.set(blockX, blockY, blockZ), 0);
        int lightU = packedLight % 65536;
        int lightV = packedLight / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightU, (float)lightV);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
    }
}
