package net.minecraft.realms;

import net.minecraft.client.renderer.Tessellator;

public class Tezzelator
{
    public static Tessellator tessellator = Tessellator.getInstance();
    public static final Tezzelator instance = new Tezzelator();

    public void end()
    {
        tessellator.draw();
    }

    public Tezzelator vertex(double x, double y, double z)
    {
        tessellator.getWorldRenderer().pos(x, y, z);
        return this;
    }

    public void color(float red, float green, float blue, float alpha)
    {
        tessellator.getWorldRenderer().color(red, green, blue, alpha);
    }

    public void tex2(short skyLight, short blockLight)
    {
        tessellator.getWorldRenderer().lightmap(skyLight, blockLight);
    }

    public void normal(float x, float y, float z)
    {
        tessellator.getWorldRenderer().normal(x, y, z);
    }

    public void begin(int drawMode, RealmsVertexFormat vertexFormat)
    {
        tessellator.getWorldRenderer().begin(drawMode, vertexFormat.getVertexFormat());
    }

    public void endVertex()
    {
        tessellator.getWorldRenderer().endVertex();
    }

    public void offset(double x, double y, double z)
    {
        tessellator.getWorldRenderer().setTranslation(x, y, z);
    }

    public RealmsBufferBuilder color(int red, int green, int blue, int alpha)
    {
        return new RealmsBufferBuilder(tessellator.getWorldRenderer().color(red, green, blue, alpha));
    }

    public Tezzelator tex(double u, double v)
    {
        tessellator.getWorldRenderer().tex(u, v);
        return this;
    }
}
