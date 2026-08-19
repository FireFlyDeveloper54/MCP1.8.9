package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderEntityItem extends Render<EntityItem>
{
    private final RenderItem itemRenderer;
    private Random random = new Random();

    public RenderEntityItem(RenderManager renderManagerIn, RenderItem itemRendererIn)
    {
        super(renderManagerIn);
        this.itemRenderer = itemRendererIn;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    private int transformModelCount(EntityItem itemIn, double x, double y, double z, float partialTicks, IBakedModel bakedModel)
    {
        ItemStack itemstack = itemIn.getEntityItem();
        Item item = itemstack.getItem();

        if (item == null)
        {
            return 0;
        }
        else
        {
            boolean gui3d = bakedModel.isGui3d();
            int modelCount = this.getModelCount(itemstack);
            float bobOffset = MathHelper.sin(((float)itemIn.getAge() + partialTicks) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F;
            float groundScaleY = bakedModel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
            GlStateManager.translate((float)x, (float)y + bobOffset + 0.25F * groundScaleY, (float)z);

            if (gui3d || this.renderManager.options != null)
            {
                float rotationAngle = (((float)itemIn.getAge() + partialTicks) / 20.0F + itemIn.hoverStart) * (180F / (float)Math.PI);
                GlStateManager.rotate(rotationAngle, 0.0F, 1.0F, 0.0F);
            }

            if (!gui3d)
            {
                float flatOffsetX = -0.0F * (float)(modelCount - 1) * 0.5F;
                float flatOffsetY = -0.0F * (float)(modelCount - 1) * 0.5F;
                float flatOffsetZ = -0.046875F * (float)(modelCount - 1) * 0.5F;
                GlStateManager.translate(flatOffsetX, flatOffsetY, flatOffsetZ);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            return modelCount;
        }
    }

    private int getModelCount(ItemStack stack)
    {
        int count = 1;

        if (stack.stackSize > 48)
        {
            count = 5;
        }
        else if (stack.stackSize > 32)
        {
            count = 4;
        }
        else if (stack.stackSize > 16)
        {
            count = 3;
        }
        else if (stack.stackSize > 1)
        {
            count = 2;
        }

        return count;
    }

    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        ItemStack itemStack = entity.getEntityItem();
        this.random.setSeed(187L);
        boolean textureBlurred = false;

        if (this.bindEntityTexture(entity))
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
            textureBlurred = true;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        IBakedModel bakedModel = this.itemRenderer.getItemModelMesher().getItemModel(itemStack);
        int modelCount = this.transformModelCount(entity, x, y, z, partialTicks, bakedModel);

        for (int renderIndex = 0; renderIndex < modelCount; ++renderIndex)
        {
            if (bakedModel.isGui3d())
            {
                GlStateManager.pushMatrix();

                if (renderIndex > 0)
                {
                    float offsetX = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float offsetY = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float offsetZ = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translate(offsetX, offsetY, offsetZ);
                }

                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                bakedModel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GROUND);
                this.itemRenderer.renderItem(itemStack, bakedModel);
                GlStateManager.popMatrix();
            }
            else
            {
                GlStateManager.pushMatrix();
                bakedModel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GROUND);
                this.itemRenderer.renderItem(itemStack, bakedModel);
                GlStateManager.popMatrix();
                float groundScaleX = bakedModel.getItemCameraTransforms().ground.scale.x;
                float groundScaleY = bakedModel.getItemCameraTransforms().ground.scale.y;
                float groundScaleZ = bakedModel.getItemCameraTransforms().ground.scale.z;
                GlStateManager.translate(0.0F * groundScaleX, 0.0F * groundScaleY, 0.046875F * groundScaleZ);
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);

        if (textureBlurred)
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityItem entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
