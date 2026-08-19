package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomItems;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;

public abstract class LayerArmorBase<T extends ModelBase> implements LayerRenderer<EntityLivingBase>
{
    protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    protected T modelLeggings;
    protected T modelArmor;
    private final RendererLivingEntity<?> renderer;
    private float alpha = 1.0F;
    private float colorR = 1.0F;
    private float colorG = 1.0F;
    private float colorB = 1.0F;
    private boolean skipRenderGlint;
    private static final Map<String, ResourceLocation> ARMOR_TEXTURE_RES_MAP = Maps.<String, ResourceLocation>newHashMap();

    public LayerArmorBase(RendererLivingEntity<?> rendererIn)
    {
        this.renderer = rendererIn;
        this.initArmor();
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.renderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 4);
        this.renderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 3);
        this.renderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 2);
        this.renderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, 1);
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

    private void renderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, int armorSlot)
    {
        ItemStack itemStack = this.getCurrentArmor(entitylivingbaseIn, armorSlot);

        if (itemStack != null && itemStack.getItem() instanceof ItemArmor)
        {
            ItemArmor itemArmor = (ItemArmor)itemStack.getItem();
            T armorModel = this.getArmorModel(armorSlot);
            armorModel.setModelAttributes(this.renderer.getMainModel());
            armorModel.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);

            
            this.setModelPartVisible(armorModel, armorSlot);
            boolean leggings = this.isSlotForLeggings(armorSlot);

            if (!Config.isCustomItems() || !CustomItems.bindCustomArmorTexture(itemStack, leggings ? 2 : 1, (String)null))
            {
                
                this.renderer.bindTexture(this.getArmorResource(itemArmor, leggings));
            
            }

            
            switch (itemArmor.getArmorMaterial())
            {
                case LEATHER:
                    int leatherColor = itemArmor.getColor(itemStack);
                    float leatherRed = (float)(leatherColor >> 16 & 255) / 255.0F;
                    float leatherGreen = (float)(leatherColor >> 8 & 255) / 255.0F;
                    float leatherBlue = (float)(leatherColor & 255) / 255.0F;
                    GlStateManager.color(this.colorR * leatherRed, this.colorG * leatherGreen, this.colorB * leatherBlue, this.alpha);
                    armorModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                    if (!Config.isCustomItems() || !CustomItems.bindCustomArmorTexture(itemStack, leggings ? 2 : 1, "overlay"))
                    {
                        this.renderer.bindTexture(this.getArmorResource(itemArmor, leggings, "overlay"));
                    }

                case CHAIN:
                case IRON:
                case GOLD:
                case DIAMOND:
                    GlStateManager.color(this.colorR, this.colorG, this.colorB, this.alpha);
                    armorModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }

            if (!this.skipRenderGlint && itemStack.isItemEnchanted() && (!Config.isCustomItems() || !CustomItems.renderCustomArmorEffect(entitylivingbaseIn, itemStack, armorModel, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale)))
            {
                this.renderGlint(entitylivingbaseIn, armorModel, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
    }

    public ItemStack getCurrentArmor(EntityLivingBase entitylivingbaseIn, int armorSlot)
    {
        return entitylivingbaseIn.getCurrentArmor(armorSlot - 1);
    }

    public T getArmorModel(int armorSlot)
    {
        return (T)(this.isSlotForLeggings(armorSlot) ? this.modelLeggings : this.modelArmor);
    }

    private boolean isSlotForLeggings(int armorSlot)
    {
        return armorSlot == 2;
    }

    private void renderGlint(EntityLivingBase entitylivingbaseIn, T armorModel, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (!Config.isShaders() || !Shaders.isShadowPass)
        {
            float glintTicks = (float)entitylivingbaseIn.ticksExisted + partialTicks;
            this.renderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);

            if (Config.isShaders())
            {
                ShadersRender.renderEnchantedGlintBegin();
            }

            GlStateManager.enableBlend();
            GlStateManager.depthFunc(514);
            GlStateManager.depthMask(false);
            float baseGlintBrightness = 0.5F;
            GlStateManager.color(baseGlintBrightness, baseGlintBrightness, baseGlintBrightness, 1.0F);

            for (int passIndex = 0; passIndex < 2; ++passIndex)
            {
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(768, 1);
                float glintColorScale = 0.76F;
                GlStateManager.color(0.5F * glintColorScale, 0.25F * glintColorScale, 0.8F * glintColorScale, 1.0F);
                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                float glintTextureScale = 0.33333334F;
                GlStateManager.scale(glintTextureScale, glintTextureScale, glintTextureScale);
                GlStateManager.rotate(30.0F - (float)passIndex * 60.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.0F, glintTicks * (0.001F + (float)passIndex * 0.003F) * 20.0F, 0.0F);
                GlStateManager.matrixMode(5888);
                armorModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }

            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.depthFunc(515);
            GlStateManager.disableBlend();

            if (Config.isShaders())
            {
                ShadersRender.renderEnchantedGlintEnd();
            }
        }
    }

    private ResourceLocation getArmorResource(ItemArmor itemArmor, boolean leggings)
    {
        return this.getArmorResource(itemArmor, leggings, (String)null);
    }

    private ResourceLocation getArmorResource(ItemArmor itemArmor, boolean leggings, String overlay)
    {
        String texturePath = "textures/models/armor/" + itemArmor.getArmorMaterial().getName() + "_layer_" + (leggings ? 2 : 1) + (overlay == null ? "" : "_" + overlay) + ".png";
        ResourceLocation resourceLocation = ARMOR_TEXTURE_RES_MAP.get(texturePath);

        if (resourceLocation == null)
        {
            resourceLocation = new ResourceLocation(texturePath);
            ARMOR_TEXTURE_RES_MAP.put(texturePath, resourceLocation);
        }

        return resourceLocation;
    }

    protected abstract void initArmor();

    protected abstract void setModelPartVisible(T model, int armorSlot);

    protected T getArmorModelHook(EntityLivingBase entityLivingBaseIn, ItemStack itemStackIn, int armorSlot, T model)
    {
        return (T)model;
    }

    public ResourceLocation getArmorResource(Entity entity, ItemStack itemStack, int armorSlot, String overlay)
    {
        ItemArmor itemArmor = (ItemArmor)itemStack.getItem();
        String materialName = itemArmor.getArmorMaterial().getName();
        String textureDomain = "minecraft";
        int domainSeparator = materialName.indexOf(58);

        if (domainSeparator != -1)
        {
            textureDomain = materialName.substring(0, domainSeparator);
            materialName = materialName.substring(domainSeparator + 1);
        }

        String texturePath = textureDomain + ":textures/models/armor/" + materialName + "_layer_" + (this.isSlotForLeggings(armorSlot) ? 2 : 1) + (overlay == null ? "" : "_" + overlay) + ".png";
        ResourceLocation resourceLocation = ARMOR_TEXTURE_RES_MAP.get(texturePath);

        if (resourceLocation == null)
        {
            resourceLocation = new ResourceLocation(texturePath);
            ARMOR_TEXTURE_RES_MAP.put(texturePath, resourceLocation);
        }

        return resourceLocation;
    }
}
