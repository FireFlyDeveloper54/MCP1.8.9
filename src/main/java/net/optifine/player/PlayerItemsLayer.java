package net.optifine.player;

import java.util.Map;
import java.util.Set;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.src.Config;

public class PlayerItemsLayer implements LayerRenderer
{
    private RenderPlayer renderPlayer = null;

    public PlayerItemsLayer(RenderPlayer renderPlayer)
    {
        this.renderPlayer = renderPlayer;
    }

    public void doRenderLayer(EntityLivingBase entityLiving, float limbSwing, float limbSwingAmount, float partialTicks, float ticksExisted, float headYaw, float rotationPitch, float scale)
    {
        this.renderEquippedItems(entityLiving, scale, partialTicks);
    }

    protected void renderEquippedItems(EntityLivingBase entityLiving, float scale, float partialTicks)
    {
        if (Config.isShowCapes())
        {
            if (!entityLiving.isInvisible())
            {
                if (entityLiving instanceof AbstractClientPlayer)
                {
                    AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)entityLiving;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.enableCull();
                    ModelBiped modelBiped = this.renderPlayer.getMainModel();
                    PlayerConfigurations.renderPlayerItems(modelBiped, abstractClientPlayer, scale, partialTicks);
                    GlStateManager.disableCull();
                }
            }
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

    public static void register(Map renderPlayerMap)
    {
        Set renderPlayerKeys = renderPlayerMap.keySet();
        boolean layerRegistered = false;

        for (Object renderPlayerKey : renderPlayerKeys)
        {
            Object renderPlayerObject = renderPlayerMap.get(renderPlayerKey);

            if (renderPlayerObject instanceof RenderPlayer)
            {
                RenderPlayer renderPlayer = (RenderPlayer)renderPlayerObject;
                renderPlayer.addLayer(new PlayerItemsLayer(renderPlayer));
                layerRegistered = true;
            }
        }

        if (!layerRegistered)
        {
            Config.warn("PlayerItemsLayer not registered");
        }
    }
}
