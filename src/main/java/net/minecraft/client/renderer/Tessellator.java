package net.minecraft.client.renderer;

import net.optifine.SmartAnimations;
import net.optifine.render.CloudRenderer;

public class Tessellator
{
    private WorldRenderer worldRenderer;
    private WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
    private static final Tessellator instance = new Tessellator(2097152);

    public static Tessellator getInstance()
    {
        return instance;
    }

    public Tessellator(int bufferSize)
    {
        this.worldRenderer = new WorldRenderer(bufferSize);
    }

    public void draw()
    {
        if (this.worldRenderer.animatedSprites != null)
        {
            SmartAnimations.spritesRendered(this.worldRenderer.animatedSprites);
        }

        if (CloudRenderer.captureDraw(this.worldRenderer))
        {
            return;
        }

        this.worldRenderer.finishDrawing();
        this.vboUploader.draw(this.worldRenderer);
    }

    public WorldRenderer getWorldRenderer()
    {
        return this.worldRenderer;
    }
}
