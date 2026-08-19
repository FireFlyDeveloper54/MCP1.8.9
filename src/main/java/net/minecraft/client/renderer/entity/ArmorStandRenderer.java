package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelArmorStand;
import net.minecraft.client.model.ModelArmorStandArmor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ResourceLocation;

public class ArmorStandRenderer extends RendererLivingEntity<EntityArmorStand>
{
    public static final ResourceLocation TEXTURE_ARMOR_STAND = new ResourceLocation("textures/entity/armorstand/wood.png");

    public ArmorStandRenderer(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelArmorStand(), 0.0F);
        LayerBipedArmor layerBipedArmor = new LayerBipedArmor(this)
        {
            protected void initArmor()
            {
                this.modelLeggings = new ModelArmorStandArmor(0.5F);
                this.modelArmor = new ModelArmorStandArmor(1.0F);
            }
        };
        this.addLayer(layerBipedArmor);
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
    }

    protected ResourceLocation getEntityTexture(EntityArmorStand entity)
    {
        return TEXTURE_ARMOR_STAND;
    }

    public ModelArmorStand getMainModel()
    {
        return (ModelArmorStand)super.getMainModel();
    }

    protected void rotateCorpse(EntityArmorStand bat, float ageInTicks, float rotationYaw, float partialTicks)
    {
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
    }

    protected boolean canRenderName(EntityArmorStand entity)
    {
        return entity.getAlwaysRenderNameTag();
    }
}
