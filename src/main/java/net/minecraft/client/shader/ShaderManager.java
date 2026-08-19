package net.minecraft.client.shader;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonBlendingMode;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShaderManager
{
    private static final Logger logger = LogManager.getLogger();
    private static final ShaderDefault defaultShaderUniform = new ShaderDefault();
    private static ShaderManager staticShaderManager = null;
    private static int currentProgram = -1;
    private static boolean shadersInitialized = true;
    private final Map<String, Object> shaderSamplers = Maps.<String, Object>newHashMap();
    private final List<String> samplerNames = Lists.<String>newArrayList();
    private final List<Integer> shaderSamplerLocations = Lists.<Integer>newArrayList();
    private final List<ShaderUniform> shaderUniforms = Lists.<ShaderUniform>newArrayList();
    private final List<Integer> shaderUniformLocations = Lists.<Integer>newArrayList();
    private final Map<String, ShaderUniform> mappedShaderUniforms = Maps.<String, ShaderUniform>newHashMap();
    private final int program;
    private final String programFilename;
    private final boolean useFaceCulling;
    private boolean isDirty;
    private final JsonBlendingMode blendingMode;
    private final List<Integer> attribLocations;
    private final List<String> attributes;
    private final ShaderLoader vertexShaderLoader;
    private final ShaderLoader fragmentShaderLoader;

    public ShaderManager(IResourceManager resourceManager, String programName) throws JsonException, IOException
    {
        JsonParser jsonparser = new JsonParser();
        ResourceLocation resourcelocation = new ResourceLocation("shaders/program/" + programName + ".json");
        this.programFilename = programName;
        InputStream inputstream = null;

        try
        {
            inputstream = resourceManager.getResource(resourcelocation).getInputStream();
            JsonObject jsonobject = jsonparser.parse(IOUtils.toString(inputstream, Charsets.UTF_8)).getAsJsonObject();
            String vertexProgramName = JsonUtils.getString(jsonobject, "vertex");
            String fragmentProgramName = JsonUtils.getString(jsonobject, "fragment");
            JsonArray jsonarray = JsonUtils.getJsonArray(jsonobject, "samplers", (JsonArray)null);

            if (jsonarray != null)
            {
                int samplerIndex = 0;

                for (JsonElement jsonelement : jsonarray)
                {
                    try
                    {
                        this.parseSampler(jsonelement);
                    }
                    catch (Exception exception2)
                    {
                        JsonException jsonexception1 = JsonException.wrap(exception2);
                        jsonexception1.addSection("samplers[" + samplerIndex + "]");
                        throw jsonexception1;
                    }

                    ++samplerIndex;
                }
            }

            JsonArray jsonarray1 = JsonUtils.getJsonArray(jsonobject, "attributes", (JsonArray)null);

            if (jsonarray1 != null)
            {
                int attributeIndex = 0;
                this.attribLocations = Lists.<Integer>newArrayListWithCapacity(jsonarray1.size());
                this.attributes = Lists.<String>newArrayListWithCapacity(jsonarray1.size());

                for (JsonElement jsonelement1 : jsonarray1)
                {
                    try
                    {
                        this.attributes.add(JsonUtils.getString(jsonelement1, "attribute"));
                    }
                    catch (Exception exception1)
                    {
                        JsonException jsonexception2 = JsonException.wrap(exception1);
                        jsonexception2.addSection("attributes[" + attributeIndex + "]");
                        throw jsonexception2;
                    }

                    ++attributeIndex;
                }
            }
            else
            {
                this.attribLocations = null;
                this.attributes = null;
            }

            JsonArray jsonarray2 = JsonUtils.getJsonArray(jsonobject, "uniforms", (JsonArray)null);

            if (jsonarray2 != null)
            {
                int uniformIndex = 0;

                for (JsonElement jsonelement2 : jsonarray2)
                {
                    try
                    {
                        this.parseUniform(jsonelement2);
                    }
                    catch (Exception exception)
                    {
                        JsonException jsonexception3 = JsonException.wrap(exception);
                        jsonexception3.addSection("uniforms[" + uniformIndex + "]");
                        throw jsonexception3;
                    }

                    ++uniformIndex;
                }
            }

            this.blendingMode = JsonBlendingMode.fromJson(JsonUtils.getJsonObject(jsonobject, "blend", (JsonObject)null));
            this.useFaceCulling = JsonUtils.getBoolean(jsonobject, "cull", true);
            this.vertexShaderLoader = ShaderLoader.loadShader(resourceManager, ShaderLoader.ShaderType.VERTEX, vertexProgramName);
            this.fragmentShaderLoader = ShaderLoader.loadShader(resourceManager, ShaderLoader.ShaderType.FRAGMENT, fragmentProgramName);
            this.program = ShaderLinkHelper.getStaticShaderLinkHelper().createProgram();
            ShaderLinkHelper.getStaticShaderLinkHelper().linkProgram(this);
            this.setupUniforms();

            if (this.attributes != null)
            {
                for (String attributeName : this.attributes)
                {
                    int attributeLocation = OpenGlHelper.glGetAttribLocation(this.program, attributeName);
                    this.attribLocations.add(Integer.valueOf(attributeLocation));
                }
            }
        }
        catch (Exception exception3)
        {
            JsonException jsonexception = JsonException.wrap(exception3);
            jsonexception.setFilename(resourcelocation.getResourcePath());
            throw jsonexception;
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }

        this.markDirty();
    }

    public void deleteShader()
    {
        ShaderLinkHelper.getStaticShaderLinkHelper().deleteShader(this);
    }

    public void endShader()
    {
        OpenGlHelper.glUseProgram(0);
        currentProgram = -1;
        staticShaderManager = null;
        shadersInitialized = true;

        for (int samplerIndex = 0; samplerIndex < this.shaderSamplerLocations.size(); ++samplerIndex)
        {
            if (this.shaderSamplers.get(this.samplerNames.get(samplerIndex)) != null)
            {
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + samplerIndex);
                GlStateManager.bindTexture(0);
            }
        }
    }

    public void useShader()
    {
        this.isDirty = false;
        staticShaderManager = this;
        this.blendingMode.apply();

        if (this.program != currentProgram)
        {
            OpenGlHelper.glUseProgram(this.program);
            currentProgram = this.program;
        }

        if (this.useFaceCulling)
        {
            GlStateManager.enableCull();
        }
        else
        {
            GlStateManager.disableCull();
        }

        for (int samplerIndex = 0; samplerIndex < this.shaderSamplerLocations.size(); ++samplerIndex)
        {
            if (this.shaderSamplers.get(this.samplerNames.get(samplerIndex)) != null)
            {
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + samplerIndex);
                GlStateManager.enableTexture2D();
                Object samplerTexture = this.shaderSamplers.get(this.samplerNames.get(samplerIndex));
                int textureId = -1;

                if (samplerTexture instanceof Framebuffer)
                {
                    textureId = ((Framebuffer)samplerTexture).framebufferTexture;
                }
                else if (samplerTexture instanceof ITextureObject)
                {
                    textureId = ((ITextureObject)samplerTexture).getGlTextureId();
                }
                else if (samplerTexture instanceof Integer)
                {
                    textureId = (Integer)samplerTexture;
                }

                if (textureId != -1)
                {
                    GlStateManager.bindTexture(textureId);
                    OpenGlHelper.glUniform1i(OpenGlHelper.glGetUniformLocation(this.program, this.samplerNames.get(samplerIndex)), samplerIndex);
                }
            }
        }

        for (ShaderUniform shaderuniform : this.shaderUniforms)
        {
            shaderuniform.upload();
        }
    }

    public void markDirty()
    {
        this.isDirty = true;
    }

    public ShaderUniform getShaderUniform(String shaderName)
    {
        return this.mappedShaderUniforms.get(shaderName);
    }

    public ShaderUniform getShaderUniformOrDefault(String shaderName)
    {
        ShaderUniform shaderUniform = this.mappedShaderUniforms.get(shaderName);
        return shaderUniform != null ? shaderUniform : defaultShaderUniform;
    }

    private void setupUniforms()
    {
        int samplerIndex = 0;

        for (int samplerListIndex = 0; samplerIndex < this.samplerNames.size(); ++samplerListIndex)
        {
            String samplerName = this.samplerNames.get(samplerIndex);
            int samplerLocation = OpenGlHelper.glGetUniformLocation(this.program, samplerName);

            if (samplerLocation == -1)
            {
                logger.warn("Shader " + this.programFilename + "could not find sampler named " + samplerName + " in the specified shader program.");
                this.shaderSamplers.remove(samplerName);
                this.samplerNames.remove(samplerListIndex);
                --samplerListIndex;
            }
            else
            {
                this.shaderSamplerLocations.add(Integer.valueOf(samplerLocation));
            }

            ++samplerIndex;
        }

        for (ShaderUniform shaderUniform : this.shaderUniforms)
        {
            String uniformName = shaderUniform.getShaderName();
            int uniformLocation = OpenGlHelper.glGetUniformLocation(this.program, uniformName);

            if (uniformLocation == -1)
            {
                logger.warn("Could not find uniform named " + uniformName + " in the specified" + " shader program.");
            }
            else
            {
                this.shaderUniformLocations.add(Integer.valueOf(uniformLocation));
                shaderUniform.setUniformLocation(uniformLocation);
                this.mappedShaderUniforms.put(uniformName, shaderUniform);
            }
        }
    }

    private void parseSampler(JsonElement jsonElement) throws JsonException
    {
        JsonObject jsonobject = JsonUtils.getJsonObject(jsonElement, "sampler");
        String samplerName = JsonUtils.getString(jsonobject, "name");

        if (!JsonUtils.isString(jsonobject, "file"))
        {
            this.shaderSamplers.put(samplerName, (Object)null);
            this.samplerNames.add(samplerName);
        }
        else
        {
            this.samplerNames.add(samplerName);
        }
    }

    public void addSamplerTexture(String samplerName, Object samplerTexture)
    {
        if (this.shaderSamplers.containsKey(samplerName))
        {
            this.shaderSamplers.remove(samplerName);
        }

        this.shaderSamplers.put(samplerName, samplerTexture);
        this.markDirty();
    }

    private void parseUniform(JsonElement jsonElement) throws JsonException
    {
        JsonObject jsonobject = JsonUtils.getJsonObject(jsonElement, "uniform");
        String uniformName = JsonUtils.getString(jsonobject, "name");
        int baseUniformType = ShaderUniform.parseType(JsonUtils.getString(jsonobject, "type"));
        int uniformCount = JsonUtils.getInt(jsonobject, "count");
        float[] uniformValues = new float[Math.max(uniformCount, 16)];
        JsonArray jsonarray = JsonUtils.getJsonArray(jsonobject, "values");

        if (jsonarray.size() != uniformCount && jsonarray.size() > 1)
        {
            throw new JsonException("Invalid amount of values specified (expected " + uniformCount + ", found " + jsonarray.size() + ")");
        }
        else
        {
            int valueIndex = 0;

            for (JsonElement jsonelement : jsonarray)
            {
                try
                {
                    uniformValues[valueIndex] = JsonUtils.getFloat(jsonelement, "value");
                }
                catch (Exception exception)
                {
                    JsonException jsonexception = JsonException.wrap(exception);
                    jsonexception.addSection("values[" + valueIndex + "]");
                    throw jsonexception;
                }

                ++valueIndex;
            }

            if (uniformCount > 1 && jsonarray.size() == 1)
            {
                while (valueIndex < uniformCount)
                {
                    uniformValues[valueIndex] = uniformValues[0];
                    ++valueIndex;
                }
            }

            int componentTypeOffset = uniformCount > 1 && uniformCount <= 4 && baseUniformType < 8 ? uniformCount - 1 : 0;
            ShaderUniform shaderUniform = new ShaderUniform(uniformName, baseUniformType + componentTypeOffset, uniformCount, this);

            if (baseUniformType <= 3)
            {
                shaderUniform.set((int)uniformValues[0], (int)uniformValues[1], (int)uniformValues[2], (int)uniformValues[3]);
            }
            else if (baseUniformType <= 7)
            {
                shaderUniform.setFloatValues(uniformValues[0], uniformValues[1], uniformValues[2], uniformValues[3]);
            }
            else
            {
                shaderUniform.set(uniformValues);
            }

            this.shaderUniforms.add(shaderUniform);
        }
    }

    public ShaderLoader getVertexShaderLoader()
    {
        return this.vertexShaderLoader;
    }

    public ShaderLoader getFragmentShaderLoader()
    {
        return this.fragmentShaderLoader;
    }

    public int getProgram()
    {
        return this.program;
    }
}
