package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureCompass;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class RenderItemFrame extends Render<EntityItemFrame>
{
    private static final ResourceLocation mapBackgroundTextures = new ResourceLocation("textures/map/map_background.png");
    private final Minecraft mc = Minecraft.getMinecraft();
    private final ModelResourceLocation itemFrameModel = new ModelResourceLocation("item_frame", "normal");
    private final ModelResourceLocation mapModel = new ModelResourceLocation("item_frame", "map");
    private RenderItem itemRenderer;
    private static double itemRenderDistanceSq = 4096.0D;

    public RenderItemFrame(RenderManager renderManagerIn, RenderItem itemRendererIn)
    {
        super(renderManagerIn);
        this.itemRenderer = itemRendererIn;
    }

    public void doRender(EntityItemFrame entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        BlockPos blockPos = entity.getHangingPosition();
        double xCoordinate = (double)blockPos.getX() - entity.posX + x;
        double yCoordinate = (double)blockPos.getY() - entity.posY + y;
        double zCoordinate = (double)blockPos.getZ() - entity.posZ + z;
        GlStateManager.translate(xCoordinate + 0.5D, yCoordinate + 0.5D, zCoordinate + 0.5D);
        GlStateManager.rotate(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        BlockRendererDispatcher blockRendererDispatcher = this.mc.getBlockRendererDispatcher();
        ModelManager modelManager = blockRendererDispatcher.getBlockModelShapes().getModelManager();
        IBakedModel frameModel;

        if (entity.getDisplayedItem() != null && entity.getDisplayedItem().getItem() == Items.filled_map)
        {
            frameModel = modelManager.getModel(this.mapModel);
        }
        else
        {
            frameModel = modelManager.getModel(this.itemFrameModel);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        blockRendererDispatcher.getBlockModelRenderer().renderModelBrightnessColor(frameModel, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.4375F);
        this.renderItem(entity);
        GlStateManager.popMatrix();
        this.renderName(entity, x + (double)((float)entity.facingDirection.getFrontOffsetX() * 0.3F), y - 0.25D, z + (double)((float)entity.facingDirection.getFrontOffsetZ() * 0.3F));
    }

    protected ResourceLocation getEntityTexture(EntityItemFrame entity)
    {
        return null;
    }

    private void renderItem(EntityItemFrame itemFrame)
    {
        ItemStack displayedStack = itemFrame.getDisplayedItem();

        if (displayedStack != null)
        {
            if (!this.isRenderItem(itemFrame))
            {
                return;
            }

            if (!Config.zoomMode)
            {
                Entity entity = this.mc.thePlayer;
                double playerDistanceSq = itemFrame.getDistanceSq(entity.posX, entity.posY, entity.posZ);

                if (playerDistanceSq > 4096.0D)
                {
                    return;
                }
            }

            EntityItem itemEntity = new EntityItem(itemFrame.worldObj, 0.0D, 0.0D, 0.0D, displayedStack);
            Item item = itemEntity.getEntityItem().getItem();
            itemEntity.getEntityItem().stackSize = 1;
            itemEntity.hoverStart = 0.0F;
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            int itemRotation = itemFrame.getRotation();

            if (item instanceof ItemMap)
            {
                itemRotation = itemRotation % 4 * 2;
            }

            GlStateManager.rotate((float)itemRotation * 360.0F / 8.0F, 0.0F, 0.0F, 1.0F);

            {
                if (item instanceof ItemMap)
                {
                    this.renderManager.renderEngine.bindTexture(mapBackgroundTextures);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    float mapScale = 0.0078125F;
                    GlStateManager.scale(mapScale, mapScale, mapScale);
                    GlStateManager.translate(-64.0F, -64.0F, 0.0F);
                    MapData mapData = Items.filled_map.getMapData(itemEntity.getEntityItem(), itemFrame.worldObj);
                    GlStateManager.translate(0.0F, 0.0F, -1.0F);

                    if (mapData != null)
                    {
                        this.mc.entityRenderer.getMapItemRenderer().renderMap(mapData, true);
                    }
                }
                else
                {
                    TextureAtlasSprite compassSprite = null;

                    if (item == Items.compass)
                    {
                        compassSprite = this.mc.getTextureMapBlocks().getAtlasSprite(TextureCompass.locationSprite);
                        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

                        if (compassSprite instanceof TextureCompass)
                        {
                            TextureCompass textureCompass = (TextureCompass)compassSprite;
                            double previousAngle = textureCompass.currentAngle;
                            double previousAngleDelta = textureCompass.angleDelta;
                            textureCompass.currentAngle = 0.0D;
                            textureCompass.angleDelta = 0.0D;
                            textureCompass.updateCompass(itemFrame.worldObj, itemFrame.posX, itemFrame.posZ, (double)MathHelper.wrapAngleTo180_float((float)(180 + itemFrame.facingDirection.getHorizontalIndex() * 90)), false, true);
                            textureCompass.currentAngle = previousAngle;
                            textureCompass.angleDelta = previousAngleDelta;
                        }
                        else
                        {
                            compassSprite = null;
                        }
                    }

                    GlStateManager.scale(0.5F, 0.5F, 0.5F);

                    if (!this.itemRenderer.shouldRenderItemIn3D(itemEntity.getEntityItem()) || item instanceof ItemSkull)
                    {
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    }

                    GlStateManager.pushAttrib();
                    RenderHelper.enableStandardItemLighting();
                    this.itemRenderer.renderItem(itemEntity.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popAttrib();

                    if (compassSprite != null && compassSprite.getFrameCount() > 0)
                    {
                        compassSprite.updateAnimation();
                    }
                }
            }
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    protected void renderName(EntityItemFrame entity, double x, double y, double z)
    {
        if (Minecraft.isGuiEnabled() && entity.getDisplayedItem() != null && entity.getDisplayedItem().hasDisplayName() && this.renderManager.pointedEntity == entity)
        {
            float labelScaleBase = 1.6F;
            float labelScale = 0.016666668F * labelScaleBase;
            double distanceSq = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
            float labelRange = entity.isSneaking() ? 32.0F : 64.0F;

            if (distanceSq < (double)(labelRange * labelRange))
            {
                String displayName = entity.getDisplayedItem().getDisplayName();

                if (entity.isSneaking())
                {
                    FontRenderer fontRenderer = this.getFontRendererFromRenderManager();
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((float)x + 0.0F, (float)y + entity.height + 0.5F, (float)z);
                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(-labelScale, -labelScale, labelScale);
                    GlStateManager.disableLighting();
                    GlStateManager.translate(0.0F, 0.25F / labelScale, 0.0F);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                    int textHalfWidth = fontRenderer.getStringWidth(displayName) / 2;
                    GlStateManager.disableTexture2D();
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    worldRenderer.pos((double)(-textHalfWidth - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldRenderer.pos((double)(-textHalfWidth - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldRenderer.pos((double)(textHalfWidth + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldRenderer.pos((double)(textHalfWidth + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                    GlStateManager.depthMask(true);
                    fontRenderer.drawString(displayName, -fontRenderer.getStringWidth(displayName) / 2, 0, 553648127);
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.popMatrix();
                }
                else
                {
                    this.renderLivingLabel(entity, displayName, x, y, z, 64);
                }
            }
        }
    }

    private boolean isRenderItem(EntityItemFrame itemFrame)
    {
        if (Shaders.isShadowPass)
        {
            return false;
        }
        else
        {
            if (!Config.zoomMode)
            {
                Entity entity = this.mc.getRenderViewEntity();
                double distanceSq = itemFrame.getDistanceSq(entity.posX, entity.posY, entity.posZ);

                if (distanceSq > itemRenderDistanceSq)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public static void updateItemRenderDistance()
    {
        Minecraft minecraft = Config.getMinecraft();
        double clampedFov = (double)Config.limit(minecraft.gameSettings.fovSetting, 1.0F, 120.0F);
        double itemRenderDistance = Math.max(6.0D * (double)minecraft.displayHeight / clampedFov, 16.0D);
        itemRenderDistanceSq = itemRenderDistance * itemRenderDistance;
    }
}
