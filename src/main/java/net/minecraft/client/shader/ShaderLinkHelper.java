package net.minecraft.client.shader;

import java.io.IOException;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.util.JsonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShaderLinkHelper
{
    private static final Logger logger = LogManager.getLogger();
    private static ShaderLinkHelper staticShaderLinkHelper;

    public static void setNewStaticShaderLinkHelper()
    {
        staticShaderLinkHelper = new ShaderLinkHelper();
    }

    public static ShaderLinkHelper getStaticShaderLinkHelper()
    {
        return staticShaderLinkHelper;
    }

    public void deleteShader(ShaderManager manager)
    {
        manager.getFragmentShaderLoader().deleteShader(manager);
        manager.getVertexShaderLoader().deleteShader(manager);
        OpenGlHelper.glDeleteProgram(manager.getProgram());
    }

    public int createProgram() throws JsonException
    {
        int programId = OpenGlHelper.glCreateProgram();

        if (programId <= 0)
        {
            throw new JsonException("Could not create shader program (returned program ID " + programId + ")");
        }
        else
        {
            return programId;
        }
    }

    public void linkProgram(ShaderManager manager) throws IOException
    {
        manager.getFragmentShaderLoader().attachShader(manager);
        manager.getVertexShaderLoader().attachShader(manager);
        OpenGlHelper.glLinkProgram(manager.getProgram());
        int linkStatus = OpenGlHelper.glGetProgrami(manager.getProgram(), OpenGlHelper.GL_LINK_STATUS);

        if (linkStatus == 0)
        {
            logger.warn("Error encountered when linking program containing VS " + manager.getVertexShaderLoader().getShaderFilename() + " and FS " + manager.getFragmentShaderLoader().getShaderFilename() + ". Log output:");
            logger.warn(OpenGlHelper.glGetProgramInfoLog(manager.getProgram(), 32768));
        }
    }
}
