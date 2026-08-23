package net.minecraft.client.renderer.entity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.Config;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.shaders.Shaders;
import optimization.entityCulling.access.EntityRendererInter;
import org.lwjgl.opengl.GL11;

public abstract class Render<T extends Entity> implements IEntityRenderer, EntityRendererInter<T>
{
    private static final ResourceLocation shadowTextures = new ResourceLocation("textures/misc/shadow.png");
    protected final RenderManager renderManager;
    public float shadowSize;
    protected float shadowOpaque = 1.0F;
    private Class entityClass = null;
    private ResourceLocation locationTextureCustom = null;

    protected Render(RenderManager renderManager)
    {
        this.renderManager = renderManager;
    }

    public boolean shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ)
    {
        AxisAlignedBB axisAlignedBB = livingEntity.getEntityBoundingBox();

        if (axisAlignedBB.hasNaN() || axisAlignedBB.getAverageEdgeLength() == 0.0D)
        {
            axisAlignedBB = new AxisAlignedBB(livingEntity.posX - 2.0D, livingEntity.posY - 2.0D, livingEntity.posZ - 2.0D, livingEntity.posX + 2.0D, livingEntity.posY + 2.0D, livingEntity.posZ + 2.0D);
        }

        return livingEntity.isInRangeToRender3d(camX, camY, camZ) && (livingEntity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisAlignedBB));
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        this.renderName(entity, x, y, z);
    }

    protected void renderName(T entity, double x, double y, double z)
    {
        if (this.canRenderName(entity))
        {
            this.renderLivingLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, 64);
        }
    }

    protected boolean canRenderName(T entity)
    {
        return entity.getAlwaysRenderNameTagForRender() && entity.hasCustomName();
    }
    @Override
    public boolean shadowShouldShowName(T entity)
    {
        return this.canRenderName(entity);
    }

    @Override
    public void shadowRenderNameTag(T entity, double x, double y, double z)
    {
        this.renderName(entity, x, y, z);
    }


    protected void renderOffsetLivingLabel(T entityIn, double x, double y, double z, String str, float scale, double distanceSq)
    {
        this.renderLivingLabel(entityIn, str, x, y, z, 64);
    }

    protected abstract ResourceLocation getEntityTexture(T entity);

    protected boolean bindEntityTexture(T entity)
    {
        ResourceLocation resourceLocation = this.getEntityTexture(entity);

        if (this.locationTextureCustom != null)
        {
            resourceLocation = this.locationTextureCustom;
        }

        if (resourceLocation == null)
        {
            return false;
        }
        else
        {
            this.bindTexture(resourceLocation);
            return true;
        }
    }

    public void bindTexture(ResourceLocation location)
    {
        this.renderManager.renderEngine.bindTexture(location);
    }

    private void renderEntityOnFire(Entity entity, double x, double y, double z, float partialTicks)
    {
        GlStateManager.disableLighting();
        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite fireLayer0Sprite = textureMap.getAtlasSprite("minecraft:blocks/fire_layer_0");
        TextureAtlasSprite fireLayer1Sprite = textureMap.getAtlasSprite("minecraft:blocks/fire_layer_1");
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        float entityScale = entity.width * 1.4F;
        GlStateManager.scale(entityScale, entityScale, entityScale);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        float halfWidth = 0.5F;
        float horizontalOffset = 0.0F;
        float flameHeight = entity.height / entityScale;
        float entityYOffset = (float)(entity.posY - entity.getEntityBoundingBox().minY);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, -0.3F + (float)((int)flameHeight) * 0.02F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float zLayerOffset = 0.0F;
        int flameLayer = 0;
        boolean multiTexture = Config.isMultiTexture();

        if (multiTexture)
        {
            worldRenderer.setBlockLayer(EnumWorldBlockLayer.SOLID);
        }

        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        while (flameHeight > 0.0F)
        {
            TextureAtlasSprite fireSprite = flameLayer % 2 == 0 ? fireLayer0Sprite : fireLayer1Sprite;
            worldRenderer.setSprite(fireSprite);
            this.bindTexture(TextureMap.locationBlocksTexture);
            float minU = fireSprite.getMinU();
            float minV = fireSprite.getMinV();
            float maxU = fireSprite.getMaxU();
            float maxV = fireSprite.getMaxV();

            if (flameLayer / 2 % 2 == 0)
            {
                float swappedU = maxU;
                maxU = minU;
                minU = swappedU;
            }

            worldRenderer.pos((double)(halfWidth - horizontalOffset), (double)(0.0F - entityYOffset), (double)zLayerOffset).tex((double)maxU, (double)maxV).endVertex();
            worldRenderer.pos((double)(-halfWidth - horizontalOffset), (double)(0.0F - entityYOffset), (double)zLayerOffset).tex((double)minU, (double)maxV).endVertex();
            worldRenderer.pos((double)(-halfWidth - horizontalOffset), (double)(1.4F - entityYOffset), (double)zLayerOffset).tex((double)minU, (double)minV).endVertex();
            worldRenderer.pos((double)(halfWidth - horizontalOffset), (double)(1.4F - entityYOffset), (double)zLayerOffset).tex((double)maxU, (double)minV).endVertex();
            flameHeight -= 0.45F;
            entityYOffset -= 0.45F;
            halfWidth *= 0.9F;
            zLayerOffset += 0.03F;
            ++flameLayer;
        }

        tessellator.draw();

        if (multiTexture)
        {
            worldRenderer.setBlockLayer((EnumWorldBlockLayer)null);
            GlStateManager.bindCurrentTexture();
        }

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    private void renderShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks)
    {
        if (!Config.isShaders() || !Shaders.shouldSkipDefaultShadow)
        {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            this.renderManager.renderEngine.bindTexture(shadowTextures);
            World world = this.getWorldFromRenderManager();
            GlStateManager.depthMask(false);
            float effectiveShadowSize = this.shadowSize;

            if (entityIn instanceof EntityLiving)
            {
                EntityLiving entityLiving = (EntityLiving)entityIn;
                effectiveShadowSize *= entityLiving.getRenderSizeModifier();

                if (entityLiving.isChild())
                {
                    effectiveShadowSize *= 0.5F;
                }
            }

            double interpX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
            double interpY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
            double interpZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
            int minBlockX = MathHelper.floor_double(interpX - (double)effectiveShadowSize);
            int maxBlockX = MathHelper.floor_double(interpX + (double)effectiveShadowSize);
            int minBlockY = MathHelper.floor_double(interpY - (double)effectiveShadowSize);
            int maxBlockY = MathHelper.floor_double(interpY);
            int minBlockZ = MathHelper.floor_double(interpZ - (double)effectiveShadowSize);
            int maxBlockZ = MathHelper.floor_double(interpZ + (double)effectiveShadowSize);
            double renderOffsetX = x - interpX;
            double renderOffsetY = y - interpY;
            double renderOffsetZ = z - interpZ;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(minBlockX, minBlockY, minBlockZ);

            for (int blockZ = minBlockZ; blockZ <= maxBlockZ; ++blockZ)
            {
                for (int blockY = minBlockY; blockY <= maxBlockY; ++blockY)
                {
                    for (int blockX = minBlockX; blockX <= maxBlockX; ++blockX)
                    {
                        blockPos.set(blockX, blockY, blockZ);
                        Block block = world.getBlockState(blockPos.down()).getBlock();

                        if (block.getRenderType() != -1 && world.getLightFromNeighbors(blockPos) > 3)
                        {
                            this.renderShadowBlock(block, x, y, z, blockPos, shadowAlpha, effectiveShadowSize, renderOffsetX, renderOffsetY, renderOffsetZ);
                        }
                    }
                }
            }

            tessellator.draw();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    private World getWorldFromRenderManager()
    {
        return this.renderManager.worldObj;
    }

    private void renderShadowBlock(Block blockIn, double entityX, double entityY, double entityZ, BlockPos pos, float shadowAlpha, float shadowSize, double xOffset, double yOffset, double zOffset)
    {
        if (blockIn.isFullCube())
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            double shadowStrength = ((double)shadowAlpha - (entityY - ((double)pos.getY() + yOffset)) / 2.0D) * 0.5D * (double)this.getWorldFromRenderManager().getLightBrightness(pos);

            if (shadowStrength >= 0.0D)
            {
                if (shadowStrength > 1.0D)
                {
                    shadowStrength = 1.0D;
                }

                double minX = (double)pos.getX() + blockIn.getBlockBoundsMinX() + xOffset;
                double maxX = (double)pos.getX() + blockIn.getBlockBoundsMaxX() + xOffset;
                double surfaceY = (double)pos.getY() + blockIn.getBlockBoundsMinY() + yOffset + 0.015625D;
                double minZ = (double)pos.getZ() + blockIn.getBlockBoundsMinZ() + zOffset;
                double maxZ = (double)pos.getZ() + blockIn.getBlockBoundsMaxZ() + zOffset;
                float minU = (float)((entityX - minX) / 2.0D / (double)shadowSize + 0.5D);
                float maxU = (float)((entityX - maxX) / 2.0D / (double)shadowSize + 0.5D);
                float minV = (float)((entityZ - minZ) / 2.0D / (double)shadowSize + 0.5D);
                float maxV = (float)((entityZ - maxZ) / 2.0D / (double)shadowSize + 0.5D);
                worldRenderer.pos(minX, surfaceY, minZ).tex((double)minU, (double)minV).color(1.0F, 1.0F, 1.0F, (float)shadowStrength).endVertex();
                worldRenderer.pos(minX, surfaceY, maxZ).tex((double)minU, (double)maxV).color(1.0F, 1.0F, 1.0F, (float)shadowStrength).endVertex();
                worldRenderer.pos(maxX, surfaceY, maxZ).tex((double)maxU, (double)maxV).color(1.0F, 1.0F, 1.0F, (float)shadowStrength).endVertex();
                worldRenderer.pos(maxX, surfaceY, minZ).tex((double)maxU, (double)minV).color(1.0F, 1.0F, 1.0F, (float)shadowStrength).endVertex();
            }
        }
    }

    public static void renderOffsetAABB(AxisAlignedBB boundingBox, double x, double y, double z)
    {
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        worldRenderer.setTranslation(x, y, z);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_NORMAL);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        tessellator.draw();
        worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableTexture2D();
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks)
    {
        if (this.renderManager.options != null)
        {
            if (this.renderManager.options.entityShadows && this.shadowSize > 0.0F && !entityIn.isInvisible() && this.renderManager.isRenderShadow())
            {
                double distanceToCamera = this.renderManager.getDistanceToCamera(entityIn.posX, entityIn.posY, entityIn.posZ);
                float shadowAlpha = (float)((1.0D - distanceToCamera / 256.0D) * (double)this.shadowOpaque);

                if (shadowAlpha > 0.0F)
                {
                    this.renderShadow(entityIn, x, y, z, shadowAlpha, partialTicks);
                }
            }

            if (entityIn.canRenderOnFire() && (!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).isSpectator()))
            {
                this.renderEntityOnFire(entityIn, x, y, z, partialTicks);
            }
        }
    }

    public FontRenderer getFontRendererFromRenderManager()
    {
        return this.renderManager.getFontRenderer();
    }

    protected void renderLivingLabel(T entityIn, String str, double x, double y, double z, int maxDistance)
    {
        double distanceSq = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);

        if (distanceSq <= (double)(maxDistance * maxDistance))
        {
            FontRenderer fontRenderer = this.getFontRendererFromRenderManager();
            float baseLabelScale = 1.6F;
            float labelScale = 0.016666668F * baseLabelScale;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x + 0.0F, (float)y + entityIn.height + 0.5F, (float)z);
            GlStateManager.normal(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-labelScale, -labelScale, labelScale);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            int nameYOffset = 0;

            if (str.equals("deadmau5"))
            {
                nameYOffset = -10;
            }

            int halfTextWidth = fontRenderer.getStringWidth(str) / 2;
            GlStateManager.disableTexture2D();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos((double)(-halfTextWidth - 1), (double)(-1 + nameYOffset), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldRenderer.pos((double)(-halfTextWidth - 1), (double)(8 + nameYOffset), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldRenderer.pos((double)(halfTextWidth + 1), (double)(8 + nameYOffset), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldRenderer.pos((double)(halfTextWidth + 1), (double)(-1 + nameYOffset), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            fontRenderer.drawString(str, -fontRenderer.getStringWidth(str) / 2, nameYOffset, 553648127);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontRenderer.drawString(str, -fontRenderer.getStringWidth(str) / 2, nameYOffset, -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    public RenderManager getRenderManager()
    {
        return this.renderManager;
    }

    public boolean isMultipass()
    {
        return false;
    }

    public void renderMultipass(T entityIn, double x, double y, double z, float entityYaw, float partialTicks)
    {
    }

    public Class getEntityClass()
    {
        return this.entityClass;
    }

    public void setEntityClass(Class entityClassIn)
    {
        this.entityClass = entityClassIn;
    }

    public ResourceLocation getLocationTextureCustom()
    {
        return this.locationTextureCustom;
    }

    public void setLocationTextureCustom(ResourceLocation locationTextureCustomIn)
    {
        this.locationTextureCustom = locationTextureCustomIn;
    }

    public static void setModelBipedMain(RenderBiped renderBiped, ModelBiped modelBipedMain)
    {
        renderBiped.modelBipedMain = modelBipedMain;
    }
}
