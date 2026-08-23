package net.minecraft.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.optifine.DynamicLights;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class ItemRenderer
{
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    private final Minecraft mc;
    private ItemStack itemToRender;
    private float equippedProgress;
    private float prevEquippedProgress;
    private final RenderManager renderManager;
    private final RenderItem itemRenderer;
    private final BlockPos.MutableBlockPos lightmapPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos overlaySamplePos = new BlockPos.MutableBlockPos();
    private int equippedItemSlot = -1;

    public ItemRenderer(Minecraft mcIn)
    {
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.itemRenderer = mcIn.getRenderItem();
    }

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform)
    {
        if (heldStack != null)
        {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();

            if (this.itemRenderer.shouldRenderItemIn3D(heldStack))
            {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);

                if (this.isBlockTranslucent(block) && (!Config.isShaders() || !Shaders.renderItemKeepDepthMask))
                {
                    GlStateManager.depthMask(false);
                }
            }

            this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);

            if (this.isBlockTranslucent(block))
            {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }
    }

    private boolean isBlockTranslucent(Block blockIn)
    {
        return blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
    }

    private void rotateArroundXAndY(float angle, float angleY)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angleY, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void setLightMapFromPlayer(AbstractClientPlayer clientPlayer)
    {
        int combinedLight = this.mc.theWorld.getCombinedLight(this.lightmapPos.set(MathHelper.floor_double(clientPlayer.posX), MathHelper.floor_double(clientPlayer.posY + (double)clientPlayer.getEyeHeight()), MathHelper.floor_double(clientPlayer.posZ)), 0);

        if (Config.isDynamicLights())
        {
            combinedLight = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), combinedLight);
        }

        float lightmapU = (float)(combinedLight & 65535);
        float lightmapV = (float)(combinedLight >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapU, lightmapV);
    }

    private void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks)
    {
        float interpolatedArmPitch = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
        float interpolatedArmYaw = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
        GlStateManager.rotate((entityplayerspIn.rotationPitch - interpolatedArmPitch) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entityplayerspIn.rotationYaw - interpolatedArmYaw) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private float getMapAngleFromPitch(float pitch)
    {
        float angleFactor = 1.0F - pitch / 45.0F + 0.1F;
        angleFactor = MathHelper.clamp_float(angleFactor, 0.0F, 1.0F);
        angleFactor = -MathHelper.cos(angleFactor * (float)Math.PI) * 0.5F + 0.5F;
        return angleFactor;
    }

    private void renderRightArm(RenderPlayer renderPlayerIn)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(54.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(64.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.25F, -0.85F, 0.75F);
        renderPlayerIn.renderRightArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderLeftArm(RenderPlayer renderPlayerIn)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.3F, -1.1F, 0.45F);
        renderPlayerIn.renderLeftArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderPlayerArms(AbstractClientPlayer clientPlayer)
    {
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        Render<AbstractClientPlayer> render = this.renderManager.<AbstractClientPlayer>getEntityRenderObject(this.mc.thePlayer);
        RenderPlayer renderPlayer = (RenderPlayer)render;

        if (!clientPlayer.isInvisible())
        {
            GlStateManager.disableCull();
            this.renderRightArm(renderPlayer);
            this.renderLeftArm(renderPlayer);
            GlStateManager.enableCull();
        }
    }

    private void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress)
    {
        float swingOffsetX = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        float swingOffsetY = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI * 2.0F);
        float swingOffsetZ = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);
        GlStateManager.translate(swingOffsetX, swingOffsetY, swingOffsetZ);
        float mapPitchFactor = this.getMapAngleFromPitch(pitch);
        GlStateManager.translate(0.0F, 0.04F, -0.72F);
        GlStateManager.translate(0.0F, equipmentProgress * -1.2F, 0.0F);
        GlStateManager.translate(0.0F, mapPitchFactor * -0.5F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mapPitchFactor * -85.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        this.renderPlayerArms(clientPlayer);
        float swingSinSquared = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float swingSinSqrt = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(swingSinSquared * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(swingSinSqrt * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(swingSinSqrt * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.38F, 0.38F, 0.38F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-1.0F, -1.0F, 0.0F);
        GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.normal(0.0F, 0.0F, -1.0F);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldRenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldRenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldRenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapData = Items.filled_map.getMapData(this.itemToRender, this.mc.theWorld);

        if (mapData != null)
        {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapData, false);
        }
    }

    private void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress)
    {
        float swingOffsetX = -0.3F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        float swingOffsetY = 0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI * 2.0F);
        float swingOffsetZ = -0.4F * MathHelper.sin(swingProgress * (float)Math.PI);
        GlStateManager.translate(swingOffsetX, swingOffsetY, swingOffsetZ);
        GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float swingSinSquared = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float swingSinSqrt = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(swingSinSqrt * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(swingSinSquared * -20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        GlStateManager.translate(-1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(5.6F, 0.0F, 0.0F);
        Render<AbstractClientPlayer> render = this.renderManager.<AbstractClientPlayer>getEntityRenderObject(this.mc.thePlayer);
        GlStateManager.disableCull();
        RenderPlayer renderPlayer = (RenderPlayer)render;
        renderPlayer.renderRightArm(this.mc.thePlayer);
        GlStateManager.enableCull();
    }

    private void doItemUsedTransformations(float swingProgress)
    {
        float swingOffsetX = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        float swingOffsetY = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI * 2.0F);
        float swingOffsetZ = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);
        GlStateManager.translate(swingOffsetX, swingOffsetY, swingOffsetZ);
    }

    private void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks)
    {
        float useTicksRemaining = (float)clientPlayer.getItemInUseCount() - partialTicks + 1.0F;
        float useProgress = useTicksRemaining / (float)this.itemToRender.getMaxItemUseDuration();
        float drinkBob = MathHelper.abs(MathHelper.cos(useTicksRemaining / 4.0F * (float)Math.PI) * 0.1F);

        if (useProgress >= 0.8F)
        {
            drinkBob = 0.0F;
        }

        GlStateManager.translate(0.0F, drinkBob, 0.0F);
        float drinkPullback = 1.0F - (float)Math.pow((double)useProgress, 27.0D);
        GlStateManager.translate(drinkPullback * 0.6F, drinkPullback * -0.5F, drinkPullback * 0.0F);
        GlStateManager.rotate(drinkPullback * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(drinkPullback * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(drinkPullback * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    private void transformFirstPersonItem(float equipProgress, float swingProgress)
    {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float swingSinSquared = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float swingSinSqrt = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(swingSinSquared * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(swingSinSqrt * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(swingSinSqrt * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    private void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer)
    {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.9F, 0.2F, 0.0F);
        float useTicks = (float)this.itemToRender.getMaxItemUseDuration() - ((float)clientPlayer.getItemInUseCount() - partialTicks + 1.0F);
        float bowPullProgress = useTicks / 20.0F;
        bowPullProgress = (bowPullProgress * bowPullProgress + bowPullProgress * 2.0F) / 3.0F;

        if (bowPullProgress > 1.0F)
        {
            bowPullProgress = 1.0F;
        }

        if (bowPullProgress > 0.1F)
        {
            float bowShake = MathHelper.sin((useTicks - 0.1F) * 1.3F);
            float bowShakeProgress = bowPullProgress - 0.1F;
            float bowShakeOffset = bowShake * bowShakeProgress;
            GlStateManager.translate(bowShakeOffset * 0.0F, bowShakeOffset * 0.01F, bowShakeOffset * 0.0F);
        }

        GlStateManager.translate(bowPullProgress * 0.0F, bowPullProgress * 0.0F, bowPullProgress * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + bowPullProgress * 0.2F);
    }

    private void doBlockTransformations()
    {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    public void renderItemInFirstPerson(float partialTicks)
    {
        if (!Config.isShaders() || !Shaders.isSkipRenderHand())
        {
            float equipProgress = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
            AbstractClientPlayer clientPlayer = this.mc.thePlayer;
            float swingProgress = clientPlayer.getSwingProgress(partialTicks);
            float renderPitch = clientPlayer.prevRotationPitch + (clientPlayer.rotationPitch - clientPlayer.prevRotationPitch) * partialTicks;
            float renderYaw = clientPlayer.prevRotationYaw + (clientPlayer.rotationYaw - clientPlayer.prevRotationYaw) * partialTicks;
            this.rotateArroundXAndY(renderPitch, renderYaw);
            this.setLightMapFromPlayer(clientPlayer);
            this.rotateWithPlayerRotations((EntityPlayerSP)clientPlayer, partialTicks);
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();

            if (this.itemToRender != null)
            {
                if (this.itemToRender.getItem() instanceof ItemMap)
                {
                    this.renderItemMap(clientPlayer, renderPitch, equipProgress, swingProgress);
                }
                else if (clientPlayer.getItemInUseCount() > 0)
                {
                    EnumAction useAction = this.itemToRender.getItemUseAction();

                    switch (useAction)
                    {
                        case NONE:
                            this.transformFirstPersonItem(equipProgress, 0.0F);
                            break;

                        case EAT:
                        case DRINK:
                            this.performDrinking(clientPlayer, partialTicks);
                            this.transformFirstPersonItem(equipProgress, 0.0F);
                            break;

                        case BLOCK:
                            this.transformFirstPersonItem(equipProgress, 0.0F);
                            this.doBlockTransformations();
                            break;

                        case BOW:
                            this.transformFirstPersonItem(equipProgress, 0.0F);
                            this.doBowTransformations(partialTicks, clientPlayer);
                    }
                }
                else
                {
                    this.doItemUsedTransformations(swingProgress);
                    this.transformFirstPersonItem(equipProgress, swingProgress);
                }

                this.renderItem(clientPlayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
            }
            else if (!clientPlayer.isInvisible())
            {
                this.renderPlayerArm(clientPlayer, equipProgress, swingProgress);
            }

            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
        }
    }

    public void renderOverlays(float partialTicks)
    {
        GlStateManager.disableAlpha();

        if (this.mc.thePlayer.isEntityInsideOpaqueBlock())
        {
            EntityPlayer player = this.mc.thePlayer;
            BlockPos blockPos = new BlockPos(player);
            IBlockState blockState = this.mc.theWorld.getBlockState(blockPos);

            for (int sampleIndex = 0; sampleIndex < 8; ++sampleIndex)
            {
                double sampleX = player.posX + (double)(((float)((sampleIndex >> 0) % 2) - 0.5F) * player.width * 0.8F);
                double sampleY = player.posY + (double)(((float)((sampleIndex >> 1) % 2) - 0.5F) * 0.1F);
                double sampleZ = player.posZ + (double)(((float)((sampleIndex >> 2) % 2) - 0.5F) * player.width * 0.8F);
                this.overlaySamplePos.set(MathHelper.floor_double(sampleX), MathHelper.floor_double(sampleY + (double)player.getEyeHeight()), MathHelper.floor_double(sampleZ));
                IBlockState sampleState = this.mc.theWorld.getBlockState(this.overlaySamplePos);

                if (sampleState.getBlock().isVisuallyOpaque())
                {
                    blockState = sampleState;
                    blockPos = new BlockPos(this.overlaySamplePos);
                }
            }

            if (blockState.getBlock().getRenderType() != -1)
            {
                this.renderBlockInHand(partialTicks, this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockState));
            }
        }

        if (!this.mc.thePlayer.isSpectator())
        {
            if (this.mc.thePlayer.isInsideOfMaterial(Material.water))
            {
                this.renderWaterOverlayTexture(partialTicks);
            }

            if (this.mc.thePlayer.isBurning())
            {
                this.renderFireInFirstPerson(partialTicks);
            }
        }

        GlStateManager.enableAlpha();
    }

    private void renderBlockInHand(float partialTicks, TextureAtlasSprite atlas)
    {
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        float overlayShade = 0.1F;
        GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.pushMatrix();
        float minX = -1.0F;
        float maxX = 1.0F;
        float minY = -1.0F;
        float maxY = 1.0F;
        float zDepth = -0.5F;
        float minU = atlas.getMinU();
        float maxU = atlas.getMaxU();
        float minV = atlas.getMinV();
        float maxV = atlas.getMaxV();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos((double)minX, (double)minY, (double)zDepth).tex((double)maxU, (double)maxV).endVertex();
        worldRenderer.pos((double)maxX, (double)minY, (double)zDepth).tex((double)minU, (double)maxV).endVertex();
        worldRenderer.pos((double)maxX, (double)maxY, (double)zDepth).tex((double)minU, (double)minV).endVertex();
        worldRenderer.pos((double)minX, (double)maxY, (double)zDepth).tex((double)maxU, (double)minV).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderWaterOverlayTexture(float partialTicks)
    {
        if (!Config.isShaders() || Shaders.isUnderwaterOverlay())
        {
            this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            float brightness = this.mc.thePlayer.getBrightness(partialTicks);
            GlStateManager.color(brightness, brightness, brightness, 0.5F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            float textureScale = 4.0F;
            float minX = -1.0F;
            float maxX = 1.0F;
            float minY = -1.0F;
            float maxY = 1.0F;
            float zDepth = -0.5F;
            float yawOffset = -this.mc.thePlayer.rotationYaw / 64.0F;
            float pitchOffset = this.mc.thePlayer.rotationPitch / 64.0F;
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos((double)minX, (double)minY, (double)zDepth).tex((double)(textureScale + yawOffset), (double)(textureScale + pitchOffset)).endVertex();
            worldRenderer.pos((double)maxX, (double)minY, (double)zDepth).tex((double)(0.0F + yawOffset), (double)(textureScale + pitchOffset)).endVertex();
            worldRenderer.pos((double)maxX, (double)maxY, (double)zDepth).tex((double)(0.0F + yawOffset), (double)(0.0F + pitchOffset)).endVertex();
            worldRenderer.pos((double)minX, (double)maxY, (double)zDepth).tex((double)(textureScale + yawOffset), (double)(0.0F + pitchOffset)).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }

    private void renderFireInFirstPerson(float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float quadSize = 1.0F;

        for (int sideIndex = 0; sideIndex < 2; ++sideIndex)
        {
            GlStateManager.pushMatrix();
            TextureAtlasSprite fireSprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            float minU = fireSprite.getMinU();
            float maxU = fireSprite.getMaxU();
            float minV = fireSprite.getMinV();
            float maxV = fireSprite.getMaxV();
            float minX = (0.0F - quadSize) / 2.0F;
            float maxX = minX + quadSize;
            float minY = 0.0F - quadSize / 2.0F;
            float maxY = minY + quadSize;
            float zDepth = -0.5F;
            GlStateManager.translate((float)(-(sideIndex * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.rotate((float)(sideIndex * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.setSprite(fireSprite);
            worldRenderer.pos((double)minX, (double)minY, (double)zDepth).tex((double)maxU, (double)maxV).endVertex();
            worldRenderer.pos((double)maxX, (double)minY, (double)zDepth).tex((double)minU, (double)maxV).endVertex();
            worldRenderer.pos((double)maxX, (double)maxY, (double)zDepth).tex((double)minU, (double)minV).endVertex();
            worldRenderer.pos((double)minX, (double)maxY, (double)zDepth).tex((double)maxU, (double)minV).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
    }

    public void updateEquippedItem()
    {
        this.prevEquippedProgress = this.equippedProgress;
        EntityPlayer player = this.mc.thePlayer;
        ItemStack currentStack = player.inventory.getCurrentItem();
        boolean shouldReequip = false;

        if (this.itemToRender != null && currentStack != null)
        {
            if (!this.itemToRender.getIsItemStackEqual(currentStack))
            {
                
                shouldReequip = true;
            }
        }
        else if (this.itemToRender == null && currentStack == null)
        {
            shouldReequip = false;
        }
        else
        {
            shouldReequip = true;
        }

        float maxProgressDelta = 0.4F;
        float targetEquipProgress = shouldReequip ? 0.0F : 1.0F;
        float progressDelta = MathHelper.clamp_float(targetEquipProgress - this.equippedProgress, -maxProgressDelta, maxProgressDelta);
        this.equippedProgress += progressDelta;

        if (this.equippedProgress < 0.1F)
        {
            this.itemToRender = currentStack;
            this.equippedItemSlot = player.inventory.currentItem;

            if (Config.isShaders())
            {
                Shaders.setItemToRenderMain(currentStack);
            }
        }
    }

    public void resetEquippedProgress()
    {
        this.equippedProgress = 0.0F;
    }

    public void resetEquippedProgress2()
    {
        this.equippedProgress = 0.0F;
    }
}
