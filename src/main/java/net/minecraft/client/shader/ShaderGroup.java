package net.minecraft.client.shader;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.vector.Matrix4f;

public class ShaderGroup
{
    private Framebuffer mainFramebuffer;
    private IResourceManager resourceManager;
    private String shaderGroupName;
    private final List<Shader> listShaders = Lists.<Shader>newArrayList();
    private final Map<String, Framebuffer> mapFramebuffers = Maps.<String, Framebuffer>newHashMap();
    private final List<Framebuffer> listFramebuffers = Lists.<Framebuffer>newArrayList();
    private Matrix4f projectionMatrix;
    private int mainFramebufferWidth;
    private int mainFramebufferHeight;
    private float time;
    private float lastTime;

    public ShaderGroup(TextureManager textureManager, IResourceManager resourceManager, Framebuffer mainFramebuffer, ResourceLocation location) throws JsonException, IOException, JsonSyntaxException
    {
        this.resourceManager = resourceManager;
        this.mainFramebuffer = mainFramebuffer;
        this.time = 0.0F;
        this.lastTime = 0.0F;
        this.mainFramebufferWidth = mainFramebuffer.framebufferWidth;
        this.mainFramebufferHeight = mainFramebuffer.framebufferHeight;
        this.shaderGroupName = location.toString();
        this.resetProjectionMatrix();
        this.parseGroup(textureManager, location);
    }

    public void parseGroup(TextureManager textureManager, ResourceLocation location) throws JsonException, IOException, JsonSyntaxException
    {
        JsonParser jsonparser = new JsonParser();
        InputStream inputstream = null;

        try
        {
            IResource iresource = this.resourceManager.getResource(location);
            inputstream = iresource.getInputStream();
            JsonObject jsonobject = jsonparser.parse(IOUtils.toString(inputstream, Charsets.UTF_8)).getAsJsonObject();

            if (JsonUtils.isJsonArray(jsonobject, "targets"))
            {
                JsonArray jsonarray = jsonobject.getAsJsonArray("targets");
                int targetIndex = 0;

                for (JsonElement jsonelement : jsonarray)
                {
                    try
                    {
                        this.initTarget(jsonelement);
                    }
                    catch (Exception exception1)
                    {
                        JsonException jsonexception1 = JsonException.wrap(exception1);
                        jsonexception1.addSection("targets[" + targetIndex + "]");
                        throw jsonexception1;
                    }

                    ++targetIndex;
                }
            }

            if (JsonUtils.isJsonArray(jsonobject, "passes"))
            {
                JsonArray jsonarray1 = jsonobject.getAsJsonArray("passes");
                int passIndex = 0;

                for (JsonElement jsonelement1 : jsonarray1)
                {
                    try
                    {
                        this.parsePass(textureManager, jsonelement1);
                    }
                    catch (Exception exception)
                    {
                        JsonException jsonexception2 = JsonException.wrap(exception);
                        jsonexception2.addSection("passes[" + passIndex + "]");
                        throw jsonexception2;
                    }

                    ++passIndex;
                }
            }
        }
        catch (Exception exception2)
        {
            JsonException jsonexception = JsonException.wrap(exception2);
            jsonexception.setFilename(location.getResourcePath());
            throw jsonexception;
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }
    }

    private void initTarget(JsonElement targetElement) throws JsonException
    {
        if (JsonUtils.isString(targetElement))
        {
            this.addFramebuffer(targetElement.getAsString(), this.mainFramebufferWidth, this.mainFramebufferHeight);
        }
        else
        {
            JsonObject jsonobject = JsonUtils.getJsonObject(targetElement, "target");
            String targetName = JsonUtils.getString(jsonobject, "name");
            int targetWidth = JsonUtils.getInt(jsonobject, "width", this.mainFramebufferWidth);
            int targetHeight = JsonUtils.getInt(jsonobject, "height", this.mainFramebufferHeight);

            if (this.mapFramebuffers.containsKey(targetName))
            {
                throw new JsonException(targetName + " is already defined");
            }

            this.addFramebuffer(targetName, targetWidth, targetHeight);
        }
    }

    private void parsePass(TextureManager textureManager, JsonElement passElement) throws JsonException, IOException
    {
        JsonObject jsonobject = JsonUtils.getJsonObject(passElement, "pass");
        String programName = JsonUtils.getString(jsonobject, "name");
        String inputTargetName = JsonUtils.getString(jsonobject, "intarget");
        String outputTargetName = JsonUtils.getString(jsonobject, "outtarget");
        Framebuffer inputFramebuffer = this.getFramebuffer(inputTargetName);
        Framebuffer outputFramebuffer = this.getFramebuffer(outputTargetName);

        if (inputFramebuffer == null)
        {
            throw new JsonException("Input target \'" + inputTargetName + "\' does not exist");
        }
        else if (outputFramebuffer == null)
        {
            throw new JsonException("Output target \'" + outputTargetName + "\' does not exist");
        }
        else
        {
            Shader shader = this.addShader(programName, inputFramebuffer, outputFramebuffer);
            JsonArray jsonarray = JsonUtils.getJsonArray(jsonobject, "auxtargets", (JsonArray)null);

            if (jsonarray != null)
            {
                int auxIndex = 0;

                for (JsonElement jsonelement : jsonarray)
                {
                    try
                    {
                        JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonelement, "auxtarget");
                        String auxName = JsonUtils.getString(jsonobject1, "name");
                        String auxId = JsonUtils.getString(jsonobject1, "id");
                        Framebuffer auxFramebuffer = this.getFramebuffer(auxId);

                        if (auxFramebuffer == null)
                        {
                            ResourceLocation textureLocation = new ResourceLocation("textures/effect/" + auxId + ".png");

                            try
                            {
                                this.resourceManager.getResource(textureLocation);
                            }
                            catch (FileNotFoundException caughtFileNotFoundException)
                            {
                                throw new JsonException("Render target or texture \'" + auxId + "\' does not exist");
                            }

                            textureManager.bindTexture(textureLocation);
                            ITextureObject textureObject = textureManager.getTexture(textureLocation);
                            int auxWidth = JsonUtils.getInt(jsonobject1, "width");
                            int auxHeight = JsonUtils.getInt(jsonobject1, "height");
                            boolean bilinear = JsonUtils.getBoolean(jsonobject1, "bilinear");

                            if (bilinear)
                            {
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                            }
                            else
                            {
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                            }

                            shader.addAuxFramebuffer(auxName, Integer.valueOf(textureObject.getGlTextureId()), auxWidth, auxHeight);
                        }
                        else
                        {
                            shader.addAuxFramebuffer(auxName, auxFramebuffer, auxFramebuffer.framebufferTextureWidth, auxFramebuffer.framebufferTextureHeight);
                        }
                    }
                    catch (Exception exception1)
                    {
                        JsonException jsonexception = JsonException.wrap(exception1);
                        jsonexception.addSection("auxtargets[" + auxIndex + "]");
                        throw jsonexception;
                    }

                    ++auxIndex;
                }
            }

            JsonArray jsonarray1 = JsonUtils.getJsonArray(jsonobject, "uniforms", (JsonArray)null);

            if (jsonarray1 != null)
            {
                int uniformIndex = 0;

                for (JsonElement jsonelement1 : jsonarray1)
                {
                    try
                    {
                        this.initUniform(jsonelement1);
                    }
                    catch (Exception exception)
                    {
                        JsonException jsonexception1 = JsonException.wrap(exception);
                        jsonexception1.addSection("uniforms[" + uniformIndex + "]");
                        throw jsonexception1;
                    }

                    ++uniformIndex;
                }
            }
        }
    }

    private void initUniform(JsonElement uniformElement) throws JsonException
    {
        JsonObject jsonobject = JsonUtils.getJsonObject(uniformElement, "uniform");
        String uniformName = JsonUtils.getString(jsonobject, "name");
        ShaderUniform shaderUniform = this.listShaders.get(this.listShaders.size() - 1).getShaderManager().getShaderUniform(uniformName);

        if (shaderUniform == null)
        {
            throw new JsonException("Uniform \'" + uniformName + "\' does not exist");
        }
        else
        {
            float[] uniformValues = new float[4];
            int valueCount = 0;

            for (JsonElement jsonelement : JsonUtils.getJsonArray(jsonobject, "values"))
            {
                try
                {
                    uniformValues[valueCount] = JsonUtils.getFloat(jsonelement, "value");
                }
                catch (Exception exception)
                {
                    JsonException jsonexception = JsonException.wrap(exception);
                    jsonexception.addSection("values[" + valueCount + "]");
                    throw jsonexception;
                }

                ++valueCount;
            }

            switch (valueCount)
            {
                case 0:
                default:
                    break;

                case 1:
                    shaderUniform.set(uniformValues[0]);
                    break;

                case 2:
                    shaderUniform.set(uniformValues[0], uniformValues[1]);
                    break;

                case 3:
                    shaderUniform.set(uniformValues[0], uniformValues[1], uniformValues[2]);
                    break;

                case 4:
                    shaderUniform.set(uniformValues[0], uniformValues[1], uniformValues[2], uniformValues[3]);
            }
        }
    }

    public Framebuffer getFramebufferRaw(String name)
    {
        return this.mapFramebuffers.get(name);
    }

    public void addFramebuffer(String name, int width, int height)
    {
        Framebuffer framebuffer = new Framebuffer(width, height, true);
        framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.mapFramebuffers.put(name, framebuffer);

        if (width == this.mainFramebufferWidth && height == this.mainFramebufferHeight)
        {
            this.listFramebuffers.add(framebuffer);
        }
    }

    public void deleteShaderGroup()
    {
        for (Framebuffer framebuffer : this.mapFramebuffers.values())
        {
            framebuffer.deleteFramebuffer();
        }

        for (Shader shader : this.listShaders)
        {
            shader.deleteShader();
        }

        this.listShaders.clear();
    }

    public Shader addShader(String name, Framebuffer framebufferIn, Framebuffer framebufferOut) throws JsonException, IOException
    {
        Shader shader = new Shader(this.resourceManager, name, framebufferIn, framebufferOut);
        this.listShaders.add(this.listShaders.size(), shader);
        return shader;
    }

    private void resetProjectionMatrix()
    {
        this.projectionMatrix = new Matrix4f();
        this.projectionMatrix.setIdentity();
        this.projectionMatrix.m00 = 2.0F / (float)this.mainFramebuffer.framebufferTextureWidth;
        this.projectionMatrix.m11 = 2.0F / (float)(-this.mainFramebuffer.framebufferTextureHeight);
        this.projectionMatrix.m22 = -0.0020001999F;
        this.projectionMatrix.m33 = 1.0F;
        this.projectionMatrix.m03 = -1.0F;
        this.projectionMatrix.m13 = 1.0F;
        this.projectionMatrix.m23 = -1.0001999F;
    }

    public void createBindFramebuffers(int width, int height)
    {
        this.mainFramebufferWidth = this.mainFramebuffer.framebufferTextureWidth;
        this.mainFramebufferHeight = this.mainFramebuffer.framebufferTextureHeight;
        this.resetProjectionMatrix();

        for (Shader shader : this.listShaders)
        {
            shader.setProjectionMatrix(this.projectionMatrix);
        }

        for (Framebuffer framebuffer : this.listFramebuffers)
        {
            framebuffer.createBindFramebuffer(width, height);
        }
    }

    public void loadShaderGroup(float partialTicks)
    {
        if (partialTicks < this.lastTime)
        {
            this.time += 1.0F - this.lastTime;
            this.time += partialTicks;
        }
        else
        {
            this.time += partialTicks - this.lastTime;
        }

        for (this.lastTime = partialTicks; this.time > 20.0F; this.time -= 20.0F)
        {
            ;
        }

        for (Shader shader : this.listShaders)
        {
            shader.loadShader(this.time / 20.0F);
        }
    }

    public final String getShaderGroupName()
    {
        return this.shaderGroupName;
    }

    private Framebuffer getFramebuffer(String name)
    {
        return name == null ? null : (name.equals("minecraft:main") ? this.mainFramebuffer : this.mapFramebuffers.get(name));
    }
}
