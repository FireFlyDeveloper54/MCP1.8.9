package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class OpenGlHelper
{
    public static boolean nvidia;
    public static boolean ati;
    public static int GL_FRAMEBUFFER;
    public static int GL_RENDERBUFFER;
    public static int GL_COLOR_ATTACHMENT0;
    public static int GL_DEPTH_ATTACHMENT;
    public static int GL_FRAMEBUFFER_COMPLETE;
    public static int GL_FB_INCOMPLETE_ATTACHMENT;
    public static int GL_FB_INCOMPLETE_MISS_ATTACH;
    public static int GL_FB_INCOMPLETE_DRAW_BUFFER;
    public static int GL_FB_INCOMPLETE_READ_BUFFER;
    public static boolean framebufferSupported;
    public static int GL_LINK_STATUS;
    public static int GL_COMPILE_STATUS;
    public static int GL_VERTEX_SHADER;
    public static int GL_FRAGMENT_SHADER;
    public static int defaultTexUnit;
    public static int lightmapTexUnit;
    public static int GL_TEXTURE2;
    public static int GL_COMBINE;
    public static int GL_INTERPOLATE;
    public static int GL_PRIMARY_COLOR;
    public static int GL_CONSTANT;
    public static int GL_PREVIOUS;
    public static int GL_COMBINE_RGB;
    public static int GL_SOURCE0_RGB;
    public static int GL_SOURCE1_RGB;
    public static int GL_SOURCE2_RGB;
    public static int GL_OPERAND0_RGB;
    public static int GL_OPERAND1_RGB;
    public static int GL_OPERAND2_RGB;
    public static int GL_COMBINE_ALPHA;
    public static int GL_SOURCE0_ALPHA;
    public static int GL_SOURCE1_ALPHA;
    public static int GL_SOURCE2_ALPHA;
    public static int GL_OPERAND0_ALPHA;
    public static int GL_OPERAND1_ALPHA;
    public static int GL_OPERAND2_ALPHA;
    public static boolean extBlendFuncSeparate;
    public static boolean openGL21;
    public static boolean shadersSupported;
    private static String logText = "";
    private static String cpu;
    public static boolean vboSupported;
    public static boolean vboSupportedAti;
    public static int GL_ARRAY_BUFFER;
    public static int GL_STATIC_DRAW;
    public static float lastBrightnessX = 0.0F;
    public static float lastBrightnessY = 0.0F;
    public static boolean openGL31;
    public static boolean vboRegions;
    public static int GL_COPY_READ_BUFFER;
    public static int GL_COPY_WRITE_BUFFER;
    public static final int GL_QUADS = 7;
    public static final int GL_TRIANGLES = 4;
    private static int defaultVertexArray;
    private static int boundVertexArray = -1;

    public static void initializeTextures()
    {
        GLCapabilities capabilities = GL.getCapabilities();

        if (!capabilities.OpenGL33)
        {
            throw new IllegalStateException("OpenGL 3.3 core is required");
        }

        int profileMask = GL11.glGetInteger(GL32.GL_CONTEXT_PROFILE_MASK);

        if ((profileMask & GL32.GL_CONTEXT_CORE_PROFILE_BIT) == 0)
        {
            throw new IllegalStateException("An OpenGL 3.3 core profile context is required");
        }

        Config.initDisplay();
        initDefaultVertexArray();
        CorePipeline.init();

        framebufferSupported = true;
        GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER;
        GL_RENDERBUFFER = GL30.GL_RENDERBUFFER;
        GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0;
        GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT;
        GL_FRAMEBUFFER_COMPLETE = GL30.GL_FRAMEBUFFER_COMPLETE;
        GL_FB_INCOMPLETE_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
        GL_FB_INCOMPLETE_MISS_ATTACH = GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
        GL_FB_INCOMPLETE_DRAW_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
        GL_FB_INCOMPLETE_READ_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;

        GL_LINK_STATUS = GL20.GL_LINK_STATUS;
        GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS;
        GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;
        GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
        shadersSupported = true;
        openGL21 = true;

        defaultTexUnit = GL13.GL_TEXTURE0;
        lightmapTexUnit = GL13.GL_TEXTURE1;
        GL_TEXTURE2 = GL13.GL_TEXTURE2;
        GL_COMBINE = 34160;
        GL_INTERPOLATE = 34165;
        GL_PRIMARY_COLOR = 34167;
        GL_CONSTANT = 34166;
        GL_PREVIOUS = 34168;
        GL_COMBINE_RGB = 34161;
        GL_SOURCE0_RGB = 34176;
        GL_SOURCE1_RGB = 34177;
        GL_SOURCE2_RGB = 34178;
        GL_OPERAND0_RGB = 34192;
        GL_OPERAND1_RGB = 34193;
        GL_OPERAND2_RGB = 34194;
        GL_COMBINE_ALPHA = 34162;
        GL_SOURCE0_ALPHA = 34184;
        GL_SOURCE1_ALPHA = 34185;
        GL_SOURCE2_ALPHA = 34186;
        GL_OPERAND0_ALPHA = 34200;
        GL_OPERAND1_ALPHA = 34201;
        GL_OPERAND2_ALPHA = 34202;

        extBlendFuncSeparate = true;
        openGL31 = true;
        vboRegions = true;
        GL_COPY_READ_BUFFER = GL31.GL_COPY_READ_BUFFER;
        GL_COPY_WRITE_BUFFER = GL31.GL_COPY_WRITE_BUFFER;
        vboSupported = true;
        vboSupportedAti = false;
        GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER;
        GL_STATIC_DRAW = GL15.GL_STATIC_DRAW;

        String vendor = GL11.glGetString(GL11.GL_VENDOR).toLowerCase(Locale.ROOT);
        nvidia = vendor.contains("nvidia");
        ati = vendor.contains("ati") || vendor.contains("amd");
        vboSupportedAti = ati;

        logText = "OpenGL 3.3 core is supported.\n"
                + "Framebuffer objects are supported.\n"
                + "Shaders are supported.\n"
                + "VBOs are supported.\n";

        try
        {
            CentralProcessor processor = (new SystemInfo()).getHardware().getProcessor();
            cpu = (processor.getLogicalProcessorCount() + "x " + processor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        }
        catch (Throwable ignored)
        {
            cpu = null;
        }
    }

    public static boolean areShadersSupported()
    {
        return shadersSupported;
    }

    public static String getLogText()
    {
        return logText;
    }

    public static int glGetProgrami(int program, int pname)
    {
        return GL20.glGetProgrami(program, pname);
    }

    public static void glAttachShader(int program, int shaderIn)
    {
        GL20.glAttachShader(program, shaderIn);
    }

    public static void glDeleteShader(int shader)
    {
        GL20.glDeleteShader(shader);
    }

    public static int glCreateShader(int type)
    {
        return GL20.glCreateShader(type);
    }

    public static void glShaderSource(int shaderIn, ByteBuffer string)
    {
        GL20.glShaderSource(shaderIn, MemoryUtil.memUTF8(string));
    }

    public static void glCompileShader(int shaderIn)
    {
        GL20.glCompileShader(shaderIn);
    }

    public static int glGetShaderi(int shaderIn, int pname)
    {
        return GL20.glGetShaderi(shaderIn, pname);
    }

    public static String glGetShaderInfoLog(int shaderIn, int maxLength)
    {
        return GL20.glGetShaderInfoLog(shaderIn, maxLength);
    }

    public static String glGetProgramInfoLog(int program, int maxLength)
    {
        return GL20.glGetProgramInfoLog(program, maxLength);
    }

    public static void glUseProgram(int program)
    {
        GL20.glUseProgram(program);
        CorePipeline.onProgramBound(program);
    }

    public static int glCreateProgram()
    {
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int program)
    {
        GL20.glDeleteProgram(program);
        CorePipeline.onProgramDeleted(program);
    }

    public static void glLinkProgram(int program)
    {
        GL20.glLinkProgram(program);
        CorePipeline.onProgramLinked(program);
    }

    public static int glGetUniformLocation(int programObj, CharSequence name)
    {
        return GL20.glGetUniformLocation(programObj, name);
    }

    public static void glUniform1(int location, IntBuffer values)
    {
        GL20.glUniform1iv(location, values);
    }

    public static void glUniform1i(int location, int value)
    {
        GL20.glUniform1i(location, value);
    }

    public static void glUniform1(int location, FloatBuffer values)
    {
        GL20.glUniform1fv(location, values);
    }

    public static void glUniform2(int location, IntBuffer values)
    {
        GL20.glUniform2iv(location, values);
    }

    public static void glUniform2(int location, FloatBuffer values)
    {
        GL20.glUniform2fv(location, values);
    }

    public static void glUniform3(int location, IntBuffer values)
    {
        GL20.glUniform3iv(location, values);
    }

    public static void glUniform3(int location, FloatBuffer values)
    {
        GL20.glUniform3fv(location, values);
    }

    public static void glUniform4(int location, IntBuffer values)
    {
        GL20.glUniform4iv(location, values);
    }

    public static void glUniform4(int location, FloatBuffer values)
    {
        GL20.glUniform4fv(location, values);
    }

    public static void glUniformMatrix2(int location, boolean transpose, FloatBuffer matrices)
    {
        GL20.glUniformMatrix2fv(location, transpose, matrices);
    }

    public static void glUniformMatrix3(int location, boolean transpose, FloatBuffer matrices)
    {
        GL20.glUniformMatrix3fv(location, transpose, matrices);
    }

    public static void glUniformMatrix4(int location, boolean transpose, FloatBuffer matrices)
    {
        GL20.glUniformMatrix4fv(location, transpose, matrices);
    }

    public static int glGetAttribLocation(int program, CharSequence name)
    {
        return GL20.glGetAttribLocation(program, name);
    }

    public static int glGenBuffers()
    {
        return GL15.glGenBuffers();
    }

    public static void glBindBuffer(int target, int buffer)
    {
        GL15.glBindBuffer(target, buffer);
    }

    public static void glBufferData(int target, ByteBuffer data, int usage)
    {
        GL15.glBufferData(target, data, usage);
    }

    public static void glDeleteBuffers(int buffer)
    {
        CorePipeline.invalidateVertexFormat();
        GL15.glDeleteBuffers(buffer);
    }

    public static boolean useVbo()
    {
        return vboSupported;
    }

    public static void initDefaultVertexArray()
    {
        if (defaultVertexArray != 0)
        {
            GL30.glDeleteVertexArrays(defaultVertexArray);
            defaultVertexArray = 0;
        }

        defaultVertexArray = GL30.glGenVertexArrays();
        boundVertexArray = -1;
        bindVertexArray(defaultVertexArray);
    }

    public static void bindVertexArray(int vertexArray)
    {
        if (vertexArray != boundVertexArray)
        {
            boundVertexArray = vertexArray;
            GL30.glBindVertexArray(vertexArray);
        }
    }

    public static void bindDefaultVertexArray()
    {
        if (defaultVertexArray != 0)
        {
            bindVertexArray(defaultVertexArray);
        }
    }

    public static int getBoundVertexArray()
    {
        return boundVertexArray;
    }

    public static void invalidateVertexArray(int vertexArray)
    {
        if (boundVertexArray == vertexArray)
        {
            boundVertexArray = -1;
        }
    }

    public static void glBindFramebuffer(int target, int framebufferIn)
    {
        GL30.glBindFramebuffer(target, framebufferIn);
    }

    public static void glBindRenderbuffer(int target, int renderbuffer)
    {
        GL30.glBindRenderbuffer(target, renderbuffer);
    }

    public static void glDeleteRenderbuffers(int renderbuffer)
    {
        GL30.glDeleteRenderbuffers(renderbuffer);
    }

    public static void glDeleteFramebuffers(int framebufferIn)
    {
        GL30.glDeleteFramebuffers(framebufferIn);
    }

    public static int glGenFramebuffers()
    {
        return GL30.glGenFramebuffers();
    }

    public static int glGenRenderbuffers()
    {
        return GL30.glGenRenderbuffers();
    }

    public static void glRenderbufferStorage(int target, int internalFormat, int width, int height)
    {
        GL30.glRenderbufferStorage(target, internalFormat, width, height);
    }

    public static void glFramebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer)
    {
        GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
    }

    public static int glCheckFramebufferStatus(int target)
    {
        return GL30.glCheckFramebufferStatus(target);
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level)
    {
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static void setActiveTexture(int texture)
    {
        GlMatrix.activeTexture(texture - GL13.GL_TEXTURE0);
        GL13.glActiveTexture(texture);
    }

    public static void setClientActiveTexture(int texture)
    {
    }

    public static void setLightmapTextureCoords(int target, float textureX, float textureY)
    {
        if (target == lightmapTexUnit)
        {
            lastBrightnessX = textureX;
            lastBrightnessY = textureY;
        }
    }

    public static void glBlendFunc(int sFactorRGB, int dFactorRGB, int sfactorAlpha, int dfactorAlpha)
    {
        GL14.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
    }

    public static boolean isFramebufferEnabled()
    {
        return Config.isFastRender() ? false : (Config.isAntialiasing() ? false : framebufferSupported && Minecraft.getMinecraft().gameSettings.fboEnable);
    }

    public static void glBufferData(int target, long size, int usage)
    {
        GL15.glBufferData(target, size, usage);
    }

    public static void glBufferSubData(int target, long offset, ByteBuffer data)
    {
        GL15.glBufferSubData(target, offset, data);
    }

    public static void glCopyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size)
    {
        GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    }

    public static String getCpu()
    {
        return cpu == null ? "<unknown>" : cpu;
    }
}
