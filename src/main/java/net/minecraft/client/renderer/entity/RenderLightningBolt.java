package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.ResourceLocation;

public class RenderLightningBolt extends Render<EntityLightningBolt>
{
    private final Random random = new Random();
    private final double[] segmentOffsetX = new double[8];
    private final double[] segmentOffsetZ = new double[8];

    public RenderLightningBolt(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    public void doRender(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 1);
        double accumulatedOffsetX = 0.0D;
        double accumulatedOffsetZ = 0.0D;
        Random offsetRandom = this.random;
        offsetRandom.setSeed(entity.boltVertex);

        for (int segmentIndex = 7; segmentIndex >= 0; --segmentIndex)
        {
            this.segmentOffsetX[segmentIndex] = accumulatedOffsetX;
            this.segmentOffsetZ[segmentIndex] = accumulatedOffsetZ;
            accumulatedOffsetX += (double)(offsetRandom.nextInt(11) - 5);
            accumulatedOffsetZ += (double)(offsetRandom.nextInt(11) - 5);
        }

        for (int layerIndex = 0; layerIndex < 4; ++layerIndex)
        {
            Random branchRandom = this.random;
            branchRandom.setSeed(entity.boltVertex);

            for (int branchIndex = 0; branchIndex < 3; ++branchIndex)
            {
                int startSegment = 7;
                int endSegment = 0;

                if (branchIndex > 0)
                {
                    startSegment = 7 - branchIndex;
                }

                if (branchIndex > 0)
                {
                    endSegment = startSegment - 2;
                }

                double currentOffsetX = this.segmentOffsetX[startSegment] - accumulatedOffsetX;
                double currentOffsetZ = this.segmentOffsetZ[startSegment] - accumulatedOffsetZ;

                for (int segmentIndex = startSegment; segmentIndex >= endSegment; --segmentIndex)
                {
                    double previousOffsetX = currentOffsetX;
                    double previousOffsetZ = currentOffsetZ;

                    if (branchIndex == 0)
                    {
                        currentOffsetX += (double)(branchRandom.nextInt(11) - 5);
                        currentOffsetZ += (double)(branchRandom.nextInt(11) - 5);
                    }
                    else
                    {
                        currentOffsetX += (double)(branchRandom.nextInt(31) - 15);
                        currentOffsetZ += (double)(branchRandom.nextInt(31) - 15);
                    }

                    worldRenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);
                    double upperRadius = 0.1D + (double)layerIndex * 0.2D;

                    if (branchIndex == 0)
                    {
                        upperRadius *= (double)segmentIndex * 0.1D + 1.0D;
                    }

                    double lowerRadius = 0.1D + (double)layerIndex * 0.2D;

                    if (branchIndex == 0)
                    {
                        lowerRadius *= (double)(segmentIndex - 1) * 0.1D + 1.0D;
                    }

                    for (int cornerIndex = 0; cornerIndex < 5; ++cornerIndex)
                    {
                        double upperCornerX = x + 0.5D - upperRadius;
                        double upperCornerZ = z + 0.5D - upperRadius;

                        if (cornerIndex == 1 || cornerIndex == 2)
                        {
                            upperCornerX += upperRadius * 2.0D;
                        }

                        if (cornerIndex == 2 || cornerIndex == 3)
                        {
                            upperCornerZ += upperRadius * 2.0D;
                        }

                        double lowerCornerX = x + 0.5D - lowerRadius;
                        double lowerCornerZ = z + 0.5D - lowerRadius;

                        if (cornerIndex == 1 || cornerIndex == 2)
                        {
                            lowerCornerX += lowerRadius * 2.0D;
                        }

                        if (cornerIndex == 2 || cornerIndex == 3)
                        {
                            lowerCornerZ += lowerRadius * 2.0D;
                        }

                        worldRenderer.pos(lowerCornerX + currentOffsetX, y + (double)(segmentIndex * 16), lowerCornerZ + currentOffsetZ).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                        worldRenderer.pos(upperCornerX + previousOffsetX, y + (double)((segmentIndex + 1) * 16), upperCornerZ + previousOffsetZ).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    protected ResourceLocation getEntityTexture(EntityLightningBolt entity)
    {
        return null;
    }
}
