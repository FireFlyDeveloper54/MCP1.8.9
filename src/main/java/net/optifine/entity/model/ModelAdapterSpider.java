package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.monster.EntitySpider;

public class ModelAdapterSpider extends ModelAdapter
{
    public ModelAdapterSpider()
    {
        super(EntitySpider.class, "spider", 1.0F);
    }

    protected ModelAdapterSpider(Class entityClass, String name, float shadowSize)
    {
        super(entityClass, name, shadowSize);
    }

    public ModelBase makeModel()
    {
        return new ModelSpider();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelSpider))
        {
            return null;
        }
        else
        {
            ModelSpider modelSpider = (ModelSpider)model;
            return modelPart.equals("head") ? modelSpider.spiderHead : (modelPart.equals("neck") ? modelSpider.spiderNeck : (modelPart.equals("body") ? modelSpider.spiderBody : (modelPart.equals("leg1") ? modelSpider.spiderLeg1 : (modelPart.equals("leg2") ? modelSpider.spiderLeg2 : (modelPart.equals("leg3") ? modelSpider.spiderLeg3 : (modelPart.equals("leg4") ? modelSpider.spiderLeg4 : (modelPart.equals("leg5") ? modelSpider.spiderLeg5 : (modelPart.equals("leg6") ? modelSpider.spiderLeg6 : (modelPart.equals("leg7") ? modelSpider.spiderLeg7 : (modelPart.equals("leg8") ? modelSpider.spiderLeg8 : null))))))))));
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "neck", "body", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7", "leg8"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderSpider renderSpider = new RenderSpider(renderManager);
        renderSpider.mainModel = modelBase;
        renderSpider.shadowSize = shadowSize;
        return renderSpider;
    }
}
