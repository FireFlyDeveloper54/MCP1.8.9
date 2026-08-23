package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.src.Config;
import net.optifine.shaders.SVertexBuilder;
import org.lwjgl.opengl.GL11;

public class WorldVertexBufferUploader
{
    @SuppressWarnings("incomplete-switch")
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
            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            setupVertexFormat(vertexFormat, byteBuffer);

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
                GL11.glDrawArrays(worldRenderer.getDrawMode(), 0, worldRenderer.getVertexCount());
            }

            clearVertexFormat(vertexFormat);
        }

        worldRenderer.reset();
    }

    public static void setupVertexFormat(VertexFormat vertexFormat, ByteBuffer byteBuffer)
    {
        setupVertexFormat(vertexFormat, byteBuffer, -1L);
    }

    public static void setupVertexFormat(VertexFormat vertexFormat, long pointer)
    {
        setupVertexFormat(vertexFormat, null, pointer);
    }

    @SuppressWarnings("incomplete-switch")
    private static void setupVertexFormat(VertexFormat vertexFormat, ByteBuffer byteBuffer, long pointer)
    {
        OpenGlHelper.bindDefaultVertexArray();
        int vertexStride = vertexFormat.getNextOffset();
        List<VertexFormatElement> elements = vertexFormat.getElements();

        for (int elementIndex = 0; elementIndex < elements.size(); ++elementIndex)
        {
            VertexFormatElement vertexFormatElement = elements.get(elementIndex);
            VertexFormatElement.EnumUsage usage = vertexFormatElement.getUsage();
            int glType = vertexFormatElement.getType().getGlConstant();
            int textureIndex = vertexFormatElement.getIndex();
            int offset = vertexFormat.getOffset(elementIndex);

            if (byteBuffer != null)
            {
                byteBuffer.position(offset);
            }

            switch (usage)
            {
                case POSITION:
                    if (byteBuffer != null)
                    {
                        GL11.glVertexPointer(vertexFormatElement.getElementCount(), glType, vertexStride, byteBuffer);
                    }
                    else
                    {
                        GL11.glVertexPointer(vertexFormatElement.getElementCount(), glType, vertexStride, pointer + (long)offset);
                    }

                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    break;

                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + textureIndex);

                    if (byteBuffer != null)
                    {
                        GL11.glTexCoordPointer(vertexFormatElement.getElementCount(), glType, vertexStride, byteBuffer);
                    }
                    else
                    {
                        GL11.glTexCoordPointer(vertexFormatElement.getElementCount(), glType, vertexStride, pointer + (long)offset);
                    }

                    GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;

                case COLOR:
                    if (byteBuffer != null)
                    {
                        GL11.glColorPointer(vertexFormatElement.getElementCount(), glType, vertexStride, byteBuffer);
                    }
                    else
                    {
                        GL11.glColorPointer(vertexFormatElement.getElementCount(), glType, vertexStride, pointer + (long)offset);
                    }

                    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                    break;

                case NORMAL:
                    if (byteBuffer != null)
                    {
                        GL11.glNormalPointer(glType, vertexStride, byteBuffer);
                    }
                    else
                    {
                        GL11.glNormalPointer(glType, vertexStride, pointer + (long)offset);
                    }

                    GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            }
        }
    }

    @SuppressWarnings("incomplete-switch")
    public static void clearVertexFormat(VertexFormat vertexFormat)
    {
        List<VertexFormatElement> elements = vertexFormat.getElements();

        for (int elementIndex = 0; elementIndex < elements.size(); ++elementIndex)
        {
            VertexFormatElement vertexFormatElement = elements.get(elementIndex);
            VertexFormatElement.EnumUsage usage = vertexFormatElement.getUsage();
            int textureIndex = vertexFormatElement.getIndex();

            switch (usage)
            {
                case POSITION:
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    break;

                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + textureIndex);
                    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;

                case COLOR:
                    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    GlStateManager.resetColor();
                    break;

                case NORMAL:
                    GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
            }
        }
    }
}
