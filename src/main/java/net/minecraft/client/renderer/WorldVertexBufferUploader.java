package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.src.Config;
import net.optifine.shaders.SVertexBuilder;

public class WorldVertexBufferUploader
{
    public void draw(WorldRenderer worldRenderer)
    {
        if (worldRenderer.getVertexCount() > 0)
        {
            if (worldRenderer.getDrawMode() == 7 && Config.isQuadsToTriangles())
            {
                worldRenderer.quadsToTriangles();
            }

            VertexFormat vertexFormat = worldRenderer.getVertexFormat();
            ByteBuffer byteBuffer = worldRenderer.getByteBuffer();
            CorePipeline.uploadImmediate(byteBuffer);
            setupVertexFormat(vertexFormat, 0L);

            if (worldRenderer.isMultiTexture())
            {
                worldRenderer.drawMultiTexture();
            }
            else if (Config.isShaders())
            {
                SVertexBuilder.drawArrays(worldRenderer.getDrawMode(), 0, worldRenderer.getVertexCount(), worldRenderer);
            }
            else
            {
                GlStateManager.glDrawArrays(worldRenderer.getDrawMode(), 0, worldRenderer.getVertexCount());
            }

            clearVertexFormat(vertexFormat);
        }

        worldRenderer.reset();
    }

    public static void setupVertexFormat(VertexFormat vertexFormat, ByteBuffer byteBuffer)
    {
        CorePipeline.uploadImmediate(byteBuffer);
        CorePipeline.setupVertexFormat(vertexFormat, 0L);
    }

    public static void setupVertexFormat(VertexFormat vertexFormat, long pointer)
    {
        CorePipeline.setupVertexFormat(vertexFormat, pointer);
    }

    public static void clearVertexFormat(VertexFormat vertexFormat)
    {
        CorePipeline.clearVertexFormat(vertexFormat);
    }
}
