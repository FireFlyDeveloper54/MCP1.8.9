package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;

public class RenderHorse extends RenderLiving<EntityHorse>
{
    private static final Map<String, ResourceLocation> horseTextureCache = Maps.<String, ResourceLocation>newHashMap();
    private static final ResourceLocation whiteHorseTextures = new ResourceLocation("textures/entity/horse/horse_white.png");
    private static final ResourceLocation muleTextures = new ResourceLocation("textures/entity/horse/mule.png");
    private static final ResourceLocation donkeyTextures = new ResourceLocation("textures/entity/horse/donkey.png");
    private static final ResourceLocation zombieHorseTextures = new ResourceLocation("textures/entity/horse/horse_zombie.png");
    private static final ResourceLocation skeletonHorseTextures = new ResourceLocation("textures/entity/horse/horse_skeleton.png");

    public RenderHorse(RenderManager rendermanagerIn, ModelHorse model, float shadowSizeIn)
    {
        super(rendermanagerIn, model, shadowSizeIn);
    }

    protected void preRenderCallback(EntityHorse entitylivingbaseIn, float partialTickTime)
    {
        float horseScale = 1.0F;
        int horseType = entitylivingbaseIn.getHorseType();

        if (horseType == 1)
        {
            horseScale *= 0.87F;
        }
        else if (horseType == 2)
        {
            horseScale *= 0.92F;
        }

        GlStateManager.scale(horseScale, horseScale, horseScale);
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);
    }

    protected ResourceLocation getEntityTexture(EntityHorse entity)
    {
        if (!entity.usesLayeredTexture())
        {
            switch (entity.getHorseType())
            {
                case 0:
                default:
                    return whiteHorseTextures;

                case 1:
                    return donkeyTextures;

                case 2:
                    return muleTextures;

                case 3:
                    return zombieHorseTextures;

                case 4:
                    return skeletonHorseTextures;
            }
        }
        else
        {
            return this.getLayeredHorseTexture(entity);
        }
    }

    private ResourceLocation getLayeredHorseTexture(EntityHorse horse)
    {
        String texturePath = horse.getHorseTexture();

        if (!horse.hasValidTexture())
        {
            return null;
        }
        else
        {
            ResourceLocation resourceLocation = horseTextureCache.get(texturePath);

            if (resourceLocation == null)
            {
                resourceLocation = new ResourceLocation(texturePath);
                Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, new LayeredTexture(horse.getVariantTexturePaths()));
                horseTextureCache.put(texturePath, resourceLocation);
            }

            return resourceLocation;
        }
    }
}
