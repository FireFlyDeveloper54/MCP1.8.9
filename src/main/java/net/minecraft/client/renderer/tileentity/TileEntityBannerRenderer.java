package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class TileEntityBannerRenderer extends TileEntitySpecialRenderer<TileEntityBanner>
{
    private static final Map<String, TileEntityBannerRenderer.TimedBannerTexture> DESIGNS = Maps.<String, TileEntityBannerRenderer.TimedBannerTexture>newHashMap();
    private static final ResourceLocation BANNERTEXTURES = new ResourceLocation("textures/entity/banner_base.png");
    private ModelBanner bannerModel = new ModelBanner();

    public void renderTileEntityAt(TileEntityBanner te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        boolean hasWorld = te.getWorld() != null;
        boolean standingBanner = !hasWorld || te.getBlockType() == Blocks.standing_banner;
        int metadata = hasWorld ? te.getBlockMetadata() : 0;
        long worldTime = hasWorld ? te.getWorld().getTotalWorldTime() : 0L;
        GlStateManager.pushMatrix();
        float modelScale = 0.6666667F;

        if (standingBanner)
        {
            GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F * modelScale, (float)z + 0.5F);
            float standingRotation = (float)(metadata * 360) / 16.0F;
            GlStateManager.rotate(-standingRotation, 0.0F, 1.0F, 0.0F);
            this.bannerModel.bannerStand.showModel = true;
        }
        else
        {
            float wallRotation = 0.0F;

            if (metadata == 2)
            {
                wallRotation = 180.0F;
            }

            if (metadata == 4)
            {
                wallRotation = 90.0F;
            }

            if (metadata == 5)
            {
                wallRotation = -90.0F;
            }

            GlStateManager.translate((float)x + 0.5F, (float)y - 0.25F * modelScale, (float)z + 0.5F);
            GlStateManager.rotate(-wallRotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            this.bannerModel.bannerStand.showModel = false;
        }

        BlockPos pos = te.getPos();
        float wavePhase = (float)(pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13) + (float)worldTime + partialTicks;
        this.bannerModel.bannerSlate.rotateAngleX = (-0.0125F + 0.01F * MathHelper.cos(wavePhase * (float)Math.PI * 0.02F)) * (float)Math.PI;
        GlStateManager.enableRescaleNormal();
        ResourceLocation resourceLocation = this.getBannerTexture(te);

        if (resourceLocation != null)
        {
            this.bindTexture(resourceLocation);
            GlStateManager.pushMatrix();
            GlStateManager.scale(modelScale, -modelScale, -modelScale);
            this.bannerModel.renderBanner();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private ResourceLocation getBannerTexture(TileEntityBanner bannerObj)
    {
        String patternLocation = bannerObj.getPatternResourceLocation();

        if (patternLocation.isEmpty())
        {
            return null;
        }
        else
        {
            TileEntityBannerRenderer.TimedBannerTexture cachedTexture = DESIGNS.get(patternLocation);

            if (cachedTexture == null)
            {
                if (DESIGNS.size() >= 256)
                {
                    long currentTime = System.currentTimeMillis();
                    Iterator<String> iterator = DESIGNS.keySet().iterator();

                    while (iterator.hasNext())
                    {
                        String cacheKey = iterator.next();
                        TileEntityBannerRenderer.TimedBannerTexture staleTexture = DESIGNS.get(cacheKey);

                        if (currentTime - staleTexture.systemTime > 60000L)
                        {
                            Minecraft.getMinecraft().getTextureManager().deleteTexture(staleTexture.bannerTexture);
                            iterator.remove();
                        }
                    }

                    if (DESIGNS.size() >= 256)
                    {
                        return null;
                    }
                }

                List<TileEntityBanner.EnumBannerPattern> patterns = bannerObj.getPatternList();
                List<EnumDyeColor> colors = bannerObj.getColorList();
                List<String> textureLayers = Lists.<String>newArrayList();

                for (TileEntityBanner.EnumBannerPattern pattern : patterns)
                {
                    textureLayers.add("textures/entity/banner/" + pattern.getPatternName() + ".png");
                }

                cachedTexture = new TileEntityBannerRenderer.TimedBannerTexture();
                cachedTexture.bannerTexture = new ResourceLocation(patternLocation);
                Minecraft.getMinecraft().getTextureManager().loadTexture(cachedTexture.bannerTexture, new LayeredColorMaskTexture(BANNERTEXTURES, textureLayers, colors));
                DESIGNS.put(patternLocation, cachedTexture);
            }

            cachedTexture.systemTime = System.currentTimeMillis();
            return cachedTexture.bannerTexture;
        }
    }

    static class TimedBannerTexture
    {
        public long systemTime;
        public ResourceLocation bannerTexture;

        private TimedBannerTexture()
        {
        }
    }
}
