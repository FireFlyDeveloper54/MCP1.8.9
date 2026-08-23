package net.minecraft.client.shader;

import com.google.common.collect.Maps;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import net.minecraft.client.renderer.CorePipeline;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.BufferUtils;

public class ShaderLoader
{
    private final ShaderLoader.ShaderType shaderType;
    private final String shaderFilename;
    private int shader;
    private int shaderAttachCount = 0;

    private ShaderLoader(ShaderLoader.ShaderType type, int shaderId, String filename)
    {
        this.shaderType = type;
        this.shader = shaderId;
        this.shaderFilename = filename;
    }

    public void attachShader(ShaderManager manager)
    {
        ++this.shaderAttachCount;
        OpenGlHelper.glAttachShader(manager.getProgram(), this.shader);
    }

    public void deleteShader(ShaderManager manager)
    {
        --this.shaderAttachCount;

        if (this.shaderAttachCount <= 0)
        {
            OpenGlHelper.glDeleteShader(this.shader);
            this.shaderType.getLoadedShaders().remove(this.shaderFilename);
        }
    }

    public String getShaderFilename()
    {
        return this.shaderFilename;
    }

    public static ShaderLoader loadShader(IResourceManager resourceManager, ShaderLoader.ShaderType type, String filename) throws IOException
    {
        ShaderLoader shaderloader = type.getLoadedShaders().get(filename);

        if (shaderloader == null)
        {
            ResourceLocation resourcelocation = new ResourceLocation("shaders/program/" + filename + type.getShaderExtension());
            BufferedInputStream bufferedinputstream = new BufferedInputStream(resourceManager.getResource(resourcelocation).getInputStream());
            byte[] abyte = toByteArray(bufferedinputstream);
            String source = CorePipeline.rewriteVanillaPostShader(new String(abyte, "UTF-8"), type == ShaderLoader.ShaderType.VERTEX);
            byte[] rewritten = source.getBytes("UTF-8");
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(rewritten.length);
            bytebuffer.put(rewritten);
            bytebuffer.position(0);
            int shaderId = OpenGlHelper.glCreateShader(type.getShaderMode());
            OpenGlHelper.glShaderSource(shaderId, bytebuffer);
            OpenGlHelper.glCompileShader(shaderId);

            if (OpenGlHelper.glGetShaderi(shaderId, OpenGlHelper.GL_COMPILE_STATUS) == 0)
            {
                String infoLog = StringUtils.trim(OpenGlHelper.glGetShaderInfoLog(shaderId, 32768));
                JsonException jsonexception = new JsonException("Couldn\'t compile " + type.getShaderName() + " program: " + infoLog);
                jsonexception.setFilename(resourcelocation.getResourcePath());
                throw jsonexception;
            }

            shaderloader = new ShaderLoader(type, shaderId, filename);
            type.getLoadedShaders().put(filename, shaderloader);
        }

        return shaderloader;
    }

    protected static byte[] toByteArray(BufferedInputStream inputStream) throws IOException
    {
        byte[] abyte;

        try
        {
            abyte = IOUtils.toByteArray((InputStream)inputStream);
        }
        finally
        {
            inputStream.close();
        }

        return abyte;
    }

    public static enum ShaderType
    {
        VERTEX("vertex", ".vsh", OpenGlHelper.GL_VERTEX_SHADER),
        FRAGMENT("fragment", ".fsh", OpenGlHelper.GL_FRAGMENT_SHADER);

        private final String shaderName;
        private final String shaderExtension;
        private final int shaderMode;
        private final Map<String, ShaderLoader> loadedShaders = Maps.<String, ShaderLoader>newHashMap();

        private ShaderType(String shaderNameIn, String shaderExtensionIn, int shaderModeIn)
        {
            this.shaderName = shaderNameIn;
            this.shaderExtension = shaderExtensionIn;
            this.shaderMode = shaderModeIn;
        }

        public String getShaderName()
        {
            return this.shaderName;
        }

        protected String getShaderExtension()
        {
            return this.shaderExtension;
        }

        protected int getShaderMode()
        {
            return this.shaderMode;
        }

        protected Map<String, ShaderLoader> getLoadedShaders()
        {
            return this.loadedShaders;
        }
    }
}
