package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelSheep1;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderSheep;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;

public class LayerSheepWool implements LayerRenderer<EntitySheep>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
    private final RenderSheep sheepRenderer;
    public ModelSheep1 sheepModel = new ModelSheep1();

    public LayerSheepWool(RenderSheep sheepRendererIn)
    {
        this.sheepRenderer = sheepRendererIn;
    }

    public void doRenderLayer(EntitySheep entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (!entitylivingbaseIn.getSheared() && !entitylivingbaseIn.isInvisible())
        {
            this.sheepRenderer.bindTexture(TEXTURE);

            if (entitylivingbaseIn.hasCustomName() && "jeb_".equals(entitylivingbaseIn.getCustomNameTag()))
            {
                int colorCycleTicks = 25;
                int colorCycleIndex = entitylivingbaseIn.ticksExisted / colorCycleTicks + entitylivingbaseIn.getEntityId();
                int colorCount = EnumDyeColor.VALUES.length;
                int currentColorIndex = colorCycleIndex % colorCount;
                int nextColorIndex = (colorCycleIndex + 1) % colorCount;
                float colorBlend = ((float)(entitylivingbaseIn.ticksExisted % colorCycleTicks) + partialTicks) / (float)colorCycleTicks;
                float[] currentColor = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(currentColorIndex));
                float[] nextColor = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(nextColorIndex));

                if (Config.isCustomColors())
                {
                    currentColor = CustomColors.getSheepColors(EnumDyeColor.byMetadata(currentColorIndex), currentColor);
                    nextColor = CustomColors.getSheepColors(EnumDyeColor.byMetadata(nextColorIndex), nextColor);
                }

                GlStateManager.color(currentColor[0] * (1.0F - colorBlend) + nextColor[0] * colorBlend, currentColor[1] * (1.0F - colorBlend) + nextColor[1] * colorBlend, currentColor[2] * (1.0F - colorBlend) + nextColor[2] * colorBlend);
            }
            else
            {
                float[] fleeceColor = EntitySheep.getDyeRgb(entitylivingbaseIn.getFleeceColor());

                if (Config.isCustomColors())
                {
                    fleeceColor = CustomColors.getSheepColors(entitylivingbaseIn.getFleeceColor(), fleeceColor);
                }

                GlStateManager.color(fleeceColor[0], fleeceColor[1], fleeceColor[2]);
            }

            this.sheepModel.setModelAttributes(this.sheepRenderer.getMainModel());
            this.sheepModel.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.sheepModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}
