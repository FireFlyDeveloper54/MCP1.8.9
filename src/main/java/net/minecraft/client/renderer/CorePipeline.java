package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.src.Config;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public final class CorePipeline
{
    public static final int ATTR_POSITION = 0;
    public static final int ATTR_COLOR = 1;
    public static final int ATTR_UV0 = 2;
    public static final int ATTR_UV1 = 3;
    public static final int ATTR_NORMAL = 4;
    public static final int SHADER_VERTEX = 0;
    public static final int SHADER_FRAGMENT = 1;
    public static final int SHADER_GEOMETRY = 2;

    private static int program;
    private static int uniformModelView;
    private static int uniformProj;
    private static int uniformTextureMatrix0;
    private static int uniformTextureMatrix1;
    private static int uniformTexGenMode;
    private static int uniformTexGenPlane;
    private static int uniformColor;
    private static int uniformUseVertexColor;
    private static int uniformTextureEnabled;
    private static int uniformUseLightmap;
    private static int uniformLighting;
    private static int uniformLightMask;
    private static int uniformNormalize;
    private static int uniformRescaleNormal;
    private static int uniformNormalScale;
    private static int uniformAlphaFunc;
    private static int uniformAlphaRef;
    private static int uniformFogMode;
    private static int uniformFogDistanceMode;
    private static int uniformFogColor;
    private static int uniformFogStart;
    private static int uniformFogEnd;
    private static int uniformFogDensity;
    private static int uniformSampler0;
    private static int uniformSampler1;
    private static int uniformOverlay;
    private static int uniformOverlayEnabled;
    private static int uniformLight0;
    private static int uniformLight1;
    private static int uniformLightAmbient;
    private static int uniformLightDiffuse;
    private static int uniformNormalMat;
    private static int uniformFlatShading;
    private static int quadIndexBuffer;
    private static int quadIndexCapacity;
    private static int streamVbo;
    private static int streamCapacity;
    private static int streamOffset;
    private static int currentProgram;
    private static int lastPackProgram;
    private static VertexFormat lastFormat;
    private static long lastPointer = -1L;
    private static int lastFormatVao = -1;
    private static int lastFormatVbo = -1;
    private static boolean lastHasColor;
    private static boolean lastHasLightmap;
    private static int lastMvRev = Integer.MIN_VALUE;
    private static int lastProjRev = Integer.MIN_VALUE;
    private static int lastTexRev = Integer.MIN_VALUE;
    private static int lastTextureEnabled = Integer.MIN_VALUE;
    private static int lastLighting = Integer.MIN_VALUE;
    private static int lastLightMask = Integer.MIN_VALUE;
    private static int lastNormalize = Integer.MIN_VALUE;
    private static int lastRescaleNormal = Integer.MIN_VALUE;
    private static int lastAlphaFunc = Integer.MIN_VALUE;
    private static float lastAlphaRef = Float.NaN;
    private static int lastFogMode = Integer.MIN_VALUE;
    private static int lastFogDistanceMode = Integer.MIN_VALUE;
    private static float lastFogStart = Float.NaN;
    private static float lastFogEnd = Float.NaN;
    private static float lastFogDensity = Float.NaN;
    private static float lastColorR = Float.NaN;
    private static float lastColorG = Float.NaN;
    private static float lastColorB = Float.NaN;
    private static float lastColorA = Float.NaN;
    private static float lastOverlayR = Float.NaN;
    private static int lastOverlayEnabled = -1;
    private static float lastOverlayG = Float.NaN;
    private static float lastOverlayB = Float.NaN;
    private static float lastOverlayA = Float.NaN;
    private static float lastLight0X = Float.NaN;
    private static float lastLight0Y = Float.NaN;
    private static float lastLight0Z = Float.NaN;
    private static float lastLight1X = Float.NaN;
    private static float lastLight1Y = Float.NaN;
    private static float lastLight1Z = Float.NaN;
    private static float lastLightAmbient = Float.NaN;
    private static float lastLightDiffuse = Float.NaN;
    private static int lastFlat = Integer.MIN_VALUE;
    private static int lastUseVertexColor = -1;
    private static boolean samplersBound;
    private static final float[] lastFogColor = new float[] {Float.NaN, Float.NaN, Float.NaN, Float.NaN};
    private static final Map<Integer, int[]> packUniforms = new HashMap<Integer, int[]>();
    private static final float[] fogColor = new float[] {0.0F, 0.0F, 0.0F, 1.0F};
    private static final float[] normalMatrix = new float[9];
    private static final float[] texGenPlanes = new float[16];
    private static boolean ready;

    private CorePipeline()
    {
    }

    public static void init()
    {
        if (ready)
        {
            return;
        }

        int vertex = compile(GL20.GL_VERTEX_SHADER, DEFAULT_VERTEX);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, DEFAULT_FRAGMENT);
        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        GL20.glBindAttribLocation(program, ATTR_POSITION, "Position");
        GL20.glBindAttribLocation(program, ATTR_COLOR, "Color");
        GL20.glBindAttribLocation(program, ATTR_UV0, "UV0");
        GL20.glBindAttribLocation(program, ATTR_UV1, "UV1");
        GL20.glBindAttribLocation(program, ATTR_NORMAL, "Normal");
        GL20.glLinkProgram(program);
        onProgramLinked(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0)
        {
            throw new IllegalStateException("Default core shader failed to link: " + GL20.glGetProgramInfoLog(program, 4096));
        }

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        uniformModelView = GL20.glGetUniformLocation(program, "ModelViewMat");
        uniformProj = GL20.glGetUniformLocation(program, "ProjMat");
        uniformTextureMatrix0 = GL20.glGetUniformLocation(program, "TextureMat0");
        uniformTextureMatrix1 = GL20.glGetUniformLocation(program, "TextureMat1");
        uniformTexGenMode = GL20.glGetUniformLocation(program, "TexGenMode");
        uniformTexGenPlane = GL20.glGetUniformLocation(program, "TexGenPlane[0]");
        uniformColor = GL20.glGetUniformLocation(program, "ColorModulator");
        uniformUseVertexColor = GL20.glGetUniformLocation(program, "UseVertexColor");
        uniformTextureEnabled = GL20.glGetUniformLocation(program, "TextureEnabled");
        uniformUseLightmap = GL20.glGetUniformLocation(program, "UseLightmap");
        uniformLighting = GL20.glGetUniformLocation(program, "LightingEnabled");
        uniformLightMask = GL20.glGetUniformLocation(program, "LightMask");
        uniformNormalize = GL20.glGetUniformLocation(program, "NormalizeNormals");
        uniformRescaleNormal = GL20.glGetUniformLocation(program, "RescaleNormals");
        uniformNormalScale = GL20.glGetUniformLocation(program, "NormalScale");
        uniformAlphaFunc = GL20.glGetUniformLocation(program, "AlphaTestFunc");
        uniformAlphaRef = GL20.glGetUniformLocation(program, "AlphaTestRef");
        uniformFogMode = GL20.glGetUniformLocation(program, "FogMode");
        uniformFogDistanceMode = GL20.glGetUniformLocation(program, "FogDistanceMode");
        uniformFogColor = GL20.glGetUniformLocation(program, "FogColor");
        uniformFogStart = GL20.glGetUniformLocation(program, "FogStart");
        uniformFogEnd = GL20.glGetUniformLocation(program, "FogEnd");
        uniformFogDensity = GL20.glGetUniformLocation(program, "FogDensity");
        uniformSampler0 = GL20.glGetUniformLocation(program, "Sampler0");
        uniformSampler1 = GL20.glGetUniformLocation(program, "Sampler1");
        uniformOverlay = GL20.glGetUniformLocation(program, "OverlayColor");
        uniformOverlayEnabled = GL20.glGetUniformLocation(program, "OverlayEnabled");
        uniformLight0 = GL20.glGetUniformLocation(program, "Light0Dir");
        uniformLight1 = GL20.glGetUniformLocation(program, "Light1Dir");
        uniformLightAmbient = GL20.glGetUniformLocation(program, "LightAmbient");
        uniformLightDiffuse = GL20.glGetUniformLocation(program, "LightDiffuse");
        uniformNormalMat = GL20.glGetUniformLocation(program, "NormalMat");
        uniformFlatShading = GL20.glGetUniformLocation(program, "FlatShading");
        bindDefault();
        ensureQuadIndices(32768);
        ready = true;
    }

    public static long uploadImmediate(ByteBuffer data)
    {
        init();
        OpenGlHelper.bindDefaultVertexArray();
        int size = data.remaining();

        if (size <= 0)
        {
            return 0L;
        }

        if (streamVbo == 0)
        {
            streamVbo = GL15.glGenBuffers();
            streamCapacity = 262144;
            streamOffset = 0;
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, streamVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long)streamCapacity, GL15.GL_STREAM_DRAW);
        }
        else
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, streamVbo);
        }

        int aligned = size + 15 & ~15;

        if (size > streamCapacity)
        {
            streamCapacity = size + 15 & ~15;
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STREAM_DRAW);
            streamOffset = aligned;
            return 0L;
        }

        if (streamOffset + size > streamCapacity)
        {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long)streamCapacity, GL15.GL_STREAM_DRAW);
            streamOffset = 0;
        }

        int position = data.position();
        int limit = data.limit();
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long)streamOffset, data);
        data.position(position);
        data.limit(limit);
        long pointer = (long)streamOffset;
        streamOffset += aligned;
        return pointer;
    }

    public static void bindDefault()
    {
        useProgram(program);
        lastPackProgram = 0;
        invalidateUniformCache();
        uploadDefaultUniforms(false, false);
    }

    public static void onProgramBound(int programId)
    {
        if (currentProgram != programId)
        {
            currentProgram = programId;
            invalidateUniformCache();
        }

        lastPackProgram = programId != 0 && programId != program ? programId : 0;
    }

    public static void notePackProgram(int programId)
    {
        onProgramBound(programId);
        uniformsFor(programId);
    }

    public static void onProgramLinked(int programId)
    {
        packUniforms.remove(Integer.valueOf(programId));

        if (currentProgram == programId)
        {
            invalidateUniformCache();
        }
    }

    public static void onProgramDeleted(int programId)
    {
        packUniforms.remove(Integer.valueOf(programId));

        if (lastPackProgram == programId)
        {
            lastPackProgram = 0;
        }

        if (currentProgram == programId)
        {
            currentProgram = 0;
            invalidateUniformCache();
        }
    }

    public static void prepareDraw(boolean hasColor, boolean hasLightmap)
    {
        init();
        refreshConstantAttributes(hasColor, hasLightmap);

        if (currentProgram == 0 || currentProgram == program)
        {
            useProgram(program);
            lastPackProgram = 0;
            uploadDefaultUniforms(hasColor, hasLightmap);
        }
        else
        {
            lastPackProgram = currentProgram;
            uploadPackMatrices(currentProgram);
        }
    }

    public static void draw(int mode, int first, int count)
    {
        if (count <= 0)
        {
            return;
        }

        init();

        if (currentProgram == 0)
        {
            bindDefault();
        }

        if (mode == 7)
        {
            int quads = count / 4;

            if (quads <= 0)
            {
                return;
            }

            ensureQuadIndices(quads);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadIndexBuffer);

            if (first == 0)
            {
                GL11.glDrawElements(GL11.GL_TRIANGLES, quads * 6, GL11.GL_UNSIGNED_INT, 0L);
            }
            else
            {
                GL32.glDrawElementsBaseVertex(GL11.GL_TRIANGLES, quads * 6, GL11.GL_UNSIGNED_INT, 0L, first);
            }
        }
        else if (mode == 9)
        {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, first, count);
        }
        else if (mode == 8)
        {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, first, count);
        }
        else
        {
            GL11.glDrawArrays(mode, first, count);
        }
    }

    public static void setupVertexFormat(VertexFormat vertexFormat, long pointer)
    {
        OpenGlHelper.bindDefaultVertexArray();
        bindFormat(vertexFormat, pointer);
        prepareDraw(lastHasColor, lastHasLightmap);
    }

    public static void bindFormat(VertexFormat vertexFormat, long pointer)
    {
        int vao = OpenGlHelper.getBoundVertexArray();
        int vbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

        if (vertexFormat == lastFormat && pointer == lastPointer && vao == lastFormatVao && vbo == lastFormatVbo)
        {
            return;
        }

        int stride = vertexFormat.getNextOffset();
        List<VertexFormatElement> elements = vertexFormat.getElements();
        boolean hasColor = false;
        boolean hasUv0 = false;
        boolean hasUv1 = false;
        boolean hasNormal = false;

        for (int i = 0; i < elements.size(); ++i)
        {
            VertexFormatElement element = elements.get(i);
            VertexFormatElement.EnumUsage usage = element.getUsage();
            int glType = element.getType().getGlConstant();
            int offset = vertexFormat.getOffset(i);
            int attrib = -1;
            boolean normalized = false;

            switch (usage)
            {
                case POSITION:
                    attrib = ATTR_POSITION;
                    break;
                case COLOR:
                    attrib = ATTR_COLOR;
                    normalized = element.getType() == VertexFormatElement.EnumType.UBYTE || element.getType() == VertexFormatElement.EnumType.BYTE;
                    hasColor = true;
                    break;
                case UV:
                    if (element.getIndex() == 0)
                    {
                        attrib = ATTR_UV0;
                        hasUv0 = true;
                    }
                    else if (element.getIndex() == 1)
                    {
                        attrib = ATTR_UV1;
                        hasUv1 = true;
                    }
                    break;
                case NORMAL:
                    attrib = ATTR_NORMAL;
                    normalized = element.getType() == VertexFormatElement.EnumType.BYTE || element.getType() == VertexFormatElement.EnumType.UBYTE;
                    hasNormal = true;
                    break;
                default:
                    break;
            }

            if (attrib < 0)
            {
                continue;
            }

            GL20.glEnableVertexAttribArray(attrib);
            GL20.glVertexAttribPointer(attrib, element.getElementCount(), glType, normalized, stride, pointer + (long)offset);
        }

        GL20.glEnableVertexAttribArray(ATTR_POSITION);

        if (!hasColor)
        {
            GL20.glDisableVertexAttribArray(ATTR_COLOR);
            GL20.glVertexAttrib4f(ATTR_COLOR, GlStateManager.getColorRed(), GlStateManager.getColorGreen(), GlStateManager.getColorBlue(), GlStateManager.getColorAlpha());
        }

        if (!hasUv0)
        {
            GL20.glDisableVertexAttribArray(ATTR_UV0);
            GL20.glVertexAttrib2f(ATTR_UV0, 0.0F, 0.0F);
        }

        if (!hasUv1)
        {
            GL20.glDisableVertexAttribArray(ATTR_UV1);
            GL20.glVertexAttrib2f(ATTR_UV1, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
        }

        if (!hasNormal)
        {
            GL20.glDisableVertexAttribArray(ATTR_NORMAL);
            GL20.glVertexAttrib3f(ATTR_NORMAL, GlStateManager.getNormalX(), GlStateManager.getNormalY(), GlStateManager.getNormalZ());
        }

        lastFormat = vertexFormat;
        lastPointer = pointer;
        lastFormatVao = vao;
        lastFormatVbo = vbo;
        lastHasColor = hasColor;
        lastHasLightmap = hasUv1;
    }

    public static void bindQuadIndexBuffer()
    {
        if (quadIndexBuffer == 0)
        {
            ensureQuadIndices(1024);
        }

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadIndexBuffer);
    }

    public static void clearVertexFormat(VertexFormat vertexFormat)
    {
    }

    public static void invalidateVertexFormat()
    {
        lastFormat = null;
        lastPointer = -1L;
        lastFormatVao = -1;
        lastFormatVbo = -1;
    }

    private static void refreshConstantAttributes(boolean hasColor, boolean hasLightmap)
    {
        if (!hasColor)
        {
            GL20.glVertexAttrib4f(ATTR_COLOR, GlStateManager.getColorRed(), GlStateManager.getColorGreen(), GlStateManager.getColorBlue(), GlStateManager.getColorAlpha());
        }

        if (!hasLightmap)
        {
            GL20.glVertexAttrib2f(ATTR_UV1, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
        }

        if (GL20.glGetVertexAttribi(ATTR_NORMAL, GL20.GL_VERTEX_ATTRIB_ARRAY_ENABLED) == 0)
        {
            GL20.glVertexAttrib3f(ATTR_NORMAL, GlStateManager.getNormalX(), GlStateManager.getNormalY(), GlStateManager.getNormalZ());
        }
    }

    private static void useProgram(int programId)
    {
        if (currentProgram != programId)
        {
            GL20.glUseProgram(programId);
            currentProgram = programId;
            invalidateUniformCache();
        }
    }

    private static void invalidateUniformCache()
    {
        lastMvRev = Integer.MIN_VALUE;
        lastProjRev = Integer.MIN_VALUE;
        lastTexRev = Integer.MIN_VALUE;
        lastTextureEnabled = Integer.MIN_VALUE;
        lastLighting = Integer.MIN_VALUE;
        lastLightMask = Integer.MIN_VALUE;
        lastNormalize = Integer.MIN_VALUE;
        lastRescaleNormal = Integer.MIN_VALUE;
        lastAlphaFunc = Integer.MIN_VALUE;
        lastAlphaRef = Float.NaN;
        lastFogMode = Integer.MIN_VALUE;
        lastFogDistanceMode = Integer.MIN_VALUE;
        lastFogStart = Float.NaN;
        lastColorR = Float.NaN;
        lastOverlayR = Float.NaN;
        lastOverlayEnabled = -1;
        lastLight0X = Float.NaN;
        lastFlat = Integer.MIN_VALUE;
        samplersBound = false;
        lastUseVertexColor = -1;
        lastFogColor[0] = Float.NaN;
    }

    public static String rewriteVanillaPostShader(String source, boolean vertex)
    {
        String rewritten = source.replace("\r\n", "\n").replace('\r', '\n');
        rewritten = replaceVersion(rewritten);
        rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])attribute\\s+", "in ");
        rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+", vertex ? "out " : "in ");
        rewritten = rewritten.replace("texture2D", "texture");

        if (!vertex)
        {
            rewritten = insertAfterVersion(rewritten, "out vec4 iris_FragColor;\n#define gl_FragColor iris_FragColor\n");
        }

        return rewritten;
    }

    public static String rewritePackShader(String source, int shaderType)
    {
        String rewritten = source.replace("\r\n", "\n").replace('\r', '\n');
        rewritten = replaceVersion(rewritten);
        rewritten = rewritten.replace("gl_FragColor", "iris_FragColor");

        for (int i = 7; i >= 0; --i)
        {
            rewritten = rewritten.replace("gl_FragData[" + i + "]", "iris_FragData" + i);
        }

        if (shaderType == SHADER_VERTEX)
        {
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])attribute\\s+", "in ");
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+", "out ");
            rewritten = insertAfterVersion(rewritten, VERTEX_PRELUDE + "\n#define main iris_user_main\n");
            rewritten = rewritten + "\n#undef main\nvoid main() {\n  iris_initTextureMatrices();\n  iris_v.FrontColor = iris_Color;\n  vec4 iris_eye = iris_ModelViewMat * vec4(iris_Position, 1.0);\n  iris_v.FogFragCoord = iris_FogDistanceMode == 34139 ? length(iris_eye.xyz) : (iris_FogDistanceMode == 9216 ? iris_eye.z : abs(iris_eye.z));\n  iris_user_main();\n}\n";
        }
        else if (shaderType == SHADER_FRAGMENT)
        {
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+", "in ");
            rewritten = insertAfterVersion(rewritten, FRAGMENT_PRELUDE + "\n#define main iris_user_main\n");
            rewritten = rewritten + "\n#undef main\nvoid main() {\n  iris_initTextureMatrices();\n  iris_FragColor = vec4(1.0);\n  iris_FragData0 = vec4(1.0);\n  iris_user_main();\n  if (iris_FragData0 == vec4(1.0) && iris_FragColor != vec4(1.0)) iris_FragData0 = iris_FragColor;\n}\n";
        }
        else if (shaderType == SHADER_GEOMETRY)
        {
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+in\\s+", "in ");
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+out\\s+", "out ");
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+", "out ");
            rewritten = rewritten.replaceAll("gl_PositionIn\\s*\\[([^\\]]+)\\]", "gl_in[$1].gl_Position");
            rewritten = rewritten.replaceAll("gl_FrontColorIn\\s*\\[([^\\]]+)\\]", "iris_v[$1].FrontColor");
            rewritten = rewritten.replaceAll("gl_BackColorIn\\s*\\[([^\\]]+)\\]", "iris_v[$1].FrontColor");
            rewritten = rewritten.replaceAll("gl_FogFragCoordIn\\s*\\[([^\\]]+)\\]", "iris_v[$1].FogFragCoord");
            rewritten = rewritten.replaceAll("gl_TexCoordIn\\s*\\[([^\\]]+)\\]\\s*\\[([^\\]]+)\\]", "iris_v[$1].TexCoord[$2]");
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])gl_VerticesIn(?![A-Za-z0-9_])", "gl_in.length()");
            rewritten = rewritten.replaceAll("(?m)^\\s*#extension\\s+GL_(?:ARB|EXT)_geometry_shader4.*$", "");
            String geomPrelude = geometryPrelude(rewritten);
            rewritten = rewritten.replaceAll("(?m)^[ \\t]*layout\\s*\\([^;]*\\)\\s*in\\s*;[ \\t]*", "");
            rewritten = rewritten.replaceAll("(?m)^[ \\t]*layout\\s*\\([^;]*\\)\\s*out\\s*;[ \\t]*", "");
            rewritten = insertAfterVersion(rewritten, geomPrelude + "\n#define main iris_user_main\n");
            rewritten = rewritten + "\n#undef main\nvoid main() {\n  iris_initTextureMatrices();\n  iris_user_main();\n}\n";
        }
        else
        {
            rewritten = insertAfterVersion(rewritten, COMMON_DEFINES + "\n");
        }

        return rewritten;
    }

    public static void bindPackAttribs(int programId)
    {
        GL20.glBindAttribLocation(programId, ATTR_POSITION, "iris_Position");
        GL20.glBindAttribLocation(programId, ATTR_COLOR, "iris_Color");
        GL20.glBindAttribLocation(programId, ATTR_UV0, "iris_UV0");
        GL20.glBindAttribLocation(programId, ATTR_UV1, "iris_UV1");
        GL20.glBindAttribLocation(programId, ATTR_NORMAL, "iris_Normal");
    }

    private static void uploadDefaultUniforms(boolean hasColor, boolean hasLightmap)
    {
        int mvRev = GlMatrix.getModelViewRevision();
        int projRev = GlMatrix.getProjectionRevision();
        int texRev = GlMatrix.getTextureRevision();

        if (mvRev != lastMvRev)
        {
            lastMvRev = mvRev;
            GL20.glUniformMatrix4fv(uniformModelView, false, GlMatrix.getModelView());

            if (uniformNormalMat >= 0)
            {
                GlMatrix.getNormalMatrix(normalMatrix);
                GL20.glUniformMatrix3fv(uniformNormalMat, false, normalMatrix);
                float scaleLength = (float)Math.sqrt((double)(normalMatrix[6] * normalMatrix[6] + normalMatrix[7] * normalMatrix[7] + normalMatrix[8] * normalMatrix[8]));
                GL20.glUniform1f(uniformNormalScale, scaleLength > 0.0F ? 1.0F / scaleLength : 1.0F);
            }
        }

        if (projRev != lastProjRev)
        {
            lastProjRev = projRev;
            GL20.glUniformMatrix4fv(uniformProj, false, GlMatrix.getProjection());
        }

        if (texRev != lastTexRev)
        {
            lastTexRev = texRev;
            GL20.glUniformMatrix4fv(uniformTextureMatrix0, false, GlMatrix.getTexture(0));
            GL20.glUniformMatrix4fv(uniformTextureMatrix1, false, GlMatrix.getTexture(1));
        }

        GL20.glUniform4i(uniformTexGenMode, GlStateManager.getTexGenMode(0, 0), GlStateManager.getTexGenMode(0, 1), GlStateManager.getTexGenMode(0, 2), GlStateManager.getTexGenMode(0, 3));

        for (int i = 0; i < 4; ++i)
        {
            System.arraycopy(GlStateManager.getTexGenPlane(0, i), 0, texGenPlanes, i * 4, 4);
        }

        GL20.glUniform4fv(uniformTexGenPlane, texGenPlanes);

        float colorR = GlStateManager.getColorRed();
        float colorG = GlStateManager.getColorGreen();
        float colorB = GlStateManager.getColorBlue();
        float colorA = GlStateManager.getColorAlpha();

        if (colorR != lastColorR || colorG != lastColorG || colorB != lastColorB || colorA != lastColorA)
        {
            lastColorR = colorR;
            lastColorG = colorG;
            lastColorB = colorB;
            lastColorA = colorA;
            GL20.glUniform4f(uniformColor, colorR, colorG, colorB, colorA);
        }

        int useVertexColor = hasColor ? 1 : 0;

        if (useVertexColor != lastUseVertexColor)
        {
            lastUseVertexColor = useVertexColor;
            GL20.glUniform1i(uniformUseVertexColor, useVertexColor);
        }

        int textureEnabled = GlStateManager.isTexture2DEnabled(0) ? 1 : 0;

        if (textureEnabled != lastTextureEnabled)
        {
            lastTextureEnabled = textureEnabled;
            GL20.glUniform1i(uniformTextureEnabled, textureEnabled);
        }

        GL20.glUniform1i(uniformUseLightmap, GlStateManager.isTexture2DEnabled(1) ? 1 : 0);
        int lighting = GlStateManager.isLightingEnabled() ? 1 : 0;

        if (lighting != lastLighting)
        {
            lastLighting = lighting;
            GL20.glUniform1i(uniformLighting, lighting);
        }

        int lightMask = (GlStateManager.isLightEnabled(0) ? 1 : 0) | (GlStateManager.isLightEnabled(1) ? 2 : 0);
        int normalize = GlStateManager.isNormalizeEnabled() ? 1 : 0;
        int rescaleNormal = GlStateManager.isRescaleNormalEnabled() ? 1 : 0;

        if (lightMask != lastLightMask)
        {
            lastLightMask = lightMask;
            GL20.glUniform1i(uniformLightMask, lightMask);
        }

        if (normalize != lastNormalize)
        {
            lastNormalize = normalize;
            GL20.glUniform1i(uniformNormalize, normalize);
        }

        if (rescaleNormal != lastRescaleNormal)
        {
            lastRescaleNormal = rescaleNormal;
            GL20.glUniform1i(uniformRescaleNormal, rescaleNormal);
        }

        int alphaFunc = GlStateManager.isAlphaTestEnabled() ? GlStateManager.getAlphaFunc() : 0;
        float alphaRef = GlStateManager.getAlphaRef();

        if (alphaFunc != lastAlphaFunc || alphaRef != lastAlphaRef)
        {
            lastAlphaFunc = alphaFunc;
            lastAlphaRef = alphaRef;
            GL20.glUniform1i(uniformAlphaFunc, alphaFunc);
            GL20.glUniform1f(uniformAlphaRef, alphaRef);
        }

        int fogMode = GlStateManager.isFogEnabled() ? GlStateManager.getFogMode() : 0;
        int fogDistanceMode = GlStateManager.getFogDistanceMode();
        float fogStart = GlStateManager.getFogStart();
        float fogEnd = GlStateManager.getFogEnd();
        float fogDensity = GlStateManager.getFogDensity();
        GlStateManager.getFogColor(fogColor);

        if (fogMode != lastFogMode || fogDistanceMode != lastFogDistanceMode || fogStart != lastFogStart || fogEnd != lastFogEnd || fogDensity != lastFogDensity || fogColor[0] != lastFogColor[0] || fogColor[1] != lastFogColor[1] || fogColor[2] != lastFogColor[2] || fogColor[3] != lastFogColor[3])
        {
            lastFogMode = fogMode;
            lastFogDistanceMode = fogDistanceMode;
            lastFogStart = fogStart;
            lastFogEnd = fogEnd;
            lastFogDensity = fogDensity;
            lastFogColor[0] = fogColor[0];
            lastFogColor[1] = fogColor[1];
            lastFogColor[2] = fogColor[2];
            lastFogColor[3] = fogColor[3];
            GL20.glUniform1i(uniformFogMode, fogMode);
            GL20.glUniform1i(uniformFogDistanceMode, fogDistanceMode);
            GL20.glUniform4f(uniformFogColor, fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
            GL20.glUniform1f(uniformFogStart, fogStart);
            GL20.glUniform1f(uniformFogEnd, fogEnd);
            GL20.glUniform1f(uniformFogDensity, fogDensity);
        }

        if (!samplersBound)
        {
            samplersBound = true;

            if (uniformSampler0 >= 0)
            {
                GL20.glUniform1i(uniformSampler0, 0);
            }

            if (uniformSampler1 >= 0)
            {
                GL20.glUniform1i(uniformSampler1, 1);
            }
        }

        if (uniformOverlay >= 0)
        {
            int overlayEnabled = GlStateManager.isOverlayEnabled() ? 1 : 0;

            if (overlayEnabled != lastOverlayEnabled)
            {
                lastOverlayEnabled = overlayEnabled;
                GL20.glUniform1i(uniformOverlayEnabled, overlayEnabled);
            }

            float overlayR = GlStateManager.getOverlayRed();
            float overlayG = GlStateManager.getOverlayGreen();
            float overlayB = GlStateManager.getOverlayBlue();
            float overlayA = GlStateManager.getOverlayAlpha();

            if (overlayR != lastOverlayR || overlayG != lastOverlayG || overlayB != lastOverlayB || overlayA != lastOverlayA)
            {
                lastOverlayR = overlayR;
                lastOverlayG = overlayG;
                lastOverlayB = overlayB;
                lastOverlayA = overlayA;
                GL20.glUniform4f(uniformOverlay, overlayR, overlayG, overlayB, overlayA);
            }
        }

        if (uniformLight0 >= 0)
        {
            float light0X = GlStateManager.getLight0X();
            float light0Y = GlStateManager.getLight0Y();
            float light0Z = GlStateManager.getLight0Z();
            float light1X = GlStateManager.getLight1X();
            float light1Y = GlStateManager.getLight1Y();
            float light1Z = GlStateManager.getLight1Z();
            float ambient = GlStateManager.getLightAmbient();
            float diffuse = GlStateManager.getLightDiffuse();

            if (light0X != lastLight0X || light0Y != lastLight0Y || light0Z != lastLight0Z || light1X != lastLight1X || light1Y != lastLight1Y || light1Z != lastLight1Z || ambient != lastLightAmbient || diffuse != lastLightDiffuse)
            {
                lastLight0X = light0X;
                lastLight0Y = light0Y;
                lastLight0Z = light0Z;
                lastLight1X = light1X;
                lastLight1Y = light1Y;
                lastLight1Z = light1Z;
                lastLightAmbient = ambient;
                lastLightDiffuse = diffuse;
                GL20.glUniform3f(uniformLight0, light0X, light0Y, light0Z);

                if (uniformLight1 >= 0)
                {
                    GL20.glUniform3f(uniformLight1, light1X, light1Y, light1Z);
                }

                if (uniformLightAmbient >= 0)
                {
                    GL20.glUniform1f(uniformLightAmbient, ambient);
                }

                if (uniformLightDiffuse >= 0)
                {
                    GL20.glUniform1f(uniformLightDiffuse, diffuse);
                }
            }
        }

        if (uniformFlatShading >= 0)
        {
            int flat = GlStateManager.getShadeModel() == 7424 ? 1 : 0;

            if (flat != lastFlat)
            {
                lastFlat = flat;
                GL20.glUniform1i(uniformFlatShading, flat);
            }
        }
    }

    private static void uploadPackMatrices(int programId)
    {
        int[] uniforms = uniformsFor(programId);
        int mvRev = GlMatrix.getModelViewRevision();
        int projRev = GlMatrix.getProjectionRevision();
        int texRev = GlMatrix.getTextureRevision();
        int fogDistanceMode = GlStateManager.getFogDistanceMode();

        if (fogDistanceMode != lastFogDistanceMode)
        {
            lastFogDistanceMode = fogDistanceMode;
            GL20.glUniform1i(uniforms[16], fogDistanceMode);
        }

        if (uniforms[0] >= 0 && mvRev != lastMvRev)
        {
            GL20.glUniformMatrix4fv(uniforms[0], false, GlMatrix.getModelView());
        }

        if (uniforms[1] >= 0 && projRev != lastProjRev)
        {
            GL20.glUniformMatrix4fv(uniforms[1], false, GlMatrix.getProjection());
        }

        if (uniforms[2] >= 0 && mvRev != lastMvRev)
        {
            GlMatrix.getNormalMatrix(normalMatrix);
            GL20.glUniformMatrix3fv(uniforms[2], false, normalMatrix);
        }

        float fogStart = GlStateManager.getFogStart();
        float fogEnd = GlStateManager.getFogEnd();
        float fogDensity = GlStateManager.getFogDensity();
        GlStateManager.getFogColor(fogColor);
        boolean fogChanged = fogStart != lastFogStart || fogEnd != lastFogEnd || fogDensity != lastFogDensity || fogColor[0] != lastFogColor[0] || fogColor[1] != lastFogColor[1] || fogColor[2] != lastFogColor[2] || fogColor[3] != lastFogColor[3];

        if (fogChanged)
        {
            lastFogStart = fogStart;
            lastFogEnd = fogEnd;
            lastFogDensity = fogDensity;
            lastFogColor[0] = fogColor[0];
            lastFogColor[1] = fogColor[1];
            lastFogColor[2] = fogColor[2];
            lastFogColor[3] = fogColor[3];

            if (uniforms[3] >= 0)
            {
                GL20.glUniform4f(uniforms[3], fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
            }

            if (uniforms[4] >= 0)
            {
                GL20.glUniform1f(uniforms[4], fogDensity);
            }

            if (uniforms[5] >= 0)
            {
                GL20.glUniform1f(uniforms[5], fogStart);
            }

            if (uniforms[6] >= 0)
            {
                GL20.glUniform1f(uniforms[6], fogEnd);
            }

            if (uniforms[8] >= 0)
            {
                float span = fogEnd - fogStart;
                GL20.glUniform1f(uniforms[8], Math.abs(span) < 1.0E-6F ? 0.0F : 1.0F / span);
            }
        }

        if (texRev != lastTexRev)
        {
            for (int i = 0; i < 8; ++i)
            {
                int location = uniforms[i == 0 ? 7 : i + 8];

                if (location >= 0)
                {
                    GL20.glUniformMatrix4fv(location, false, GlMatrix.getTexture(i));
                }
            }
        }

        lastMvRev = mvRev;
        lastProjRev = projRev;
        lastTexRev = texRev;
    }

    private static int[] uniformsFor(int programId)
    {
        Integer key = Integer.valueOf(programId);
        int[] uniforms = packUniforms.get(key);

        if (uniforms == null)
        {
            uniforms = new int[] {
                GL20.glGetUniformLocation(programId, "iris_ModelViewMat"),
                GL20.glGetUniformLocation(programId, "iris_ProjMat"),
                GL20.glGetUniformLocation(programId, "iris_NormalMat"),
                GL20.glGetUniformLocation(programId, "iris_Fog.color"),
                GL20.glGetUniformLocation(programId, "iris_Fog.density"),
                GL20.glGetUniformLocation(programId, "iris_Fog.start"),
                GL20.glGetUniformLocation(programId, "iris_Fog.end"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[0]"),
                GL20.glGetUniformLocation(programId, "iris_Fog.scale"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[1]"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[2]"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[3]"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[4]"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[5]"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[6]"),
                GL20.glGetUniformLocation(programId, "iris_TextureMats[7]"),
                GL20.glGetUniformLocation(programId, "iris_FogDistanceMode")
            };
            packUniforms.put(key, uniforms);
        }

        return uniforms;
    }

    private static void ensureQuadIndices(int quads)
    {
        if (quads <= quadIndexCapacity)
        {
            return;
        }

        int capacity = Math.max(quads, quadIndexCapacity * 2 + 64);
        IntBuffer indices = BufferUtils.createIntBuffer(capacity * 6);
        int vertex = 0;

        for (int i = 0; i < capacity; ++i)
        {
            indices.put(vertex);
            indices.put(vertex + 1);
            indices.put(vertex + 2);
            indices.put(vertex);
            indices.put(vertex + 2);
            indices.put(vertex + 3);
            vertex += 4;
        }

        indices.flip();

        if (quadIndexBuffer == 0)
        {
            quadIndexBuffer = GL15.glGenBuffers();
        }

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadIndexBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
        quadIndexCapacity = capacity;
    }

    private static int compile(int type, String source)
    {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0)
        {
            throw new IllegalStateException("Shader compile failed: " + GL20.glGetShaderInfoLog(shader, 4096));
        }

        return shader;
    }

    private static String replaceVersion(String source)
    {
        Matcher directive = Pattern.compile("(?m)^[ \\t]*#version[ \\t]+[0-9]+(?:[ \\t]+(?:core|compatibility|es))?").matcher(maskShaderComments(source));

        if (!directive.find())
        {
            return "#version 330 core\n" + source;
        }

        return source.substring(0, directive.start()) + "#version 330 core" + source.substring(directive.end());
    }

    private static String geometryPrelude(String source)
    {
        int maxVertices = parseMaxVerticesOut(source);
        String inPrimitive = "triangles";
        String outPrimitive = "triangle_strip";
        String inLayout = layoutArguments(source, "in");
        String outLayout = layoutArguments(source, "out");

        if (inLayout != null)
        {
            String parsed = primitiveFromLayout(inLayout, false);

            if (parsed != null)
            {
                inPrimitive = parsed;
            }

            maxVertices = Math.max(maxVertices, maxVerticesFromLayout(outLayout));
        }

        if (outLayout != null)
        {
            String parsed = primitiveFromLayout(outLayout, true);

            if (parsed != null)
            {
                outPrimitive = parsed;
            }

            int layoutMax = maxVerticesFromLayout(outLayout);

            if (layoutMax > 0)
            {
                maxVertices = layoutMax;
            }
        }

        String propertyIn = firstProperty(source, new String[] {"GEOMETRY_IN", "GEOM_IN", "GEOMIN", "IN_PRIMITIVE", "INPUT_PRIMITIVE", "GEOMETRY"});
        String propertyOut = firstProperty(source, new String[] {"GEOMETRY_OUT", "GEOM_OUT", "GEOMOUT", "OUT_PRIMITIVE", "OUTPUT_PRIMITIVE"});

        if (inLayout == null && propertyIn != null)
        {
            String parsed = mapPrimitive(propertyIn, false);

            if (parsed != null)
            {
                inPrimitive = parsed;
            }
        }

        if (outLayout == null && propertyOut != null)
        {
            String parsed = mapPrimitive(propertyOut, true);

            if (parsed != null)
            {
                outPrimitive = parsed;
            }
        }

        return COMMON_DEFINES +
            "layout(" + inPrimitive + ") in;\n" +
            "layout(" + outPrimitive + ", max_vertices=" + maxVertices + ") out;\n" +
            "in iris_Vert {\n" +
            "  vec4 FrontColor;\n" +
            "  vec4 TexCoord[8];\n" +
            "  float FogFragCoord;\n" +
            "} iris_v[];\n" +
            "out iris_Vert {\n" +
            "  vec4 FrontColor;\n" +
            "  vec4 TexCoord[8];\n" +
            "  float FogFragCoord;\n" +
            "} iris_g;\n" +
            "#define gl_FrontColor iris_g.FrontColor\n" +
            "#define gl_BackColor iris_g.FrontColor\n" +
            "#define gl_TexCoord iris_g.TexCoord\n" +
            "#define gl_FogFragCoord iris_g.FogFragCoord\n";
    }

    private static int parseMaxVerticesOut(String source)
    {
        int maxVertices = 3;
        int name = indexOfIgnoreCase(source, "maxVerticesOut");

        if (name < 0)
        {
            name = indexOfIgnoreCase(source, "max_vertices");
        }

        if (name >= 0)
        {
            int equals = source.indexOf('=', name);

            if (equals >= 0)
            {
                int start = equals + 1;

                while (start < source.length() && (source.charAt(start) == ' ' || source.charAt(start) == '\t'))
                {
                    ++start;
                }

                int end = start;

                while (end < source.length() && source.charAt(end) >= '0' && source.charAt(end) <= '9')
                {
                    ++end;
                }

                if (end > start)
                {
                    try
                    {
                        maxVertices = Math.max(1, Integer.parseInt(source.substring(start, end)));
                    }
                    catch (NumberFormatException ignored)
                    {
                    }
                }
            }
        }

        return maxVertices;
    }

    private static String layoutArguments(String source, String qualifier)
    {
        String lower = source.toLowerCase(Locale.ROOT);
        int from = 0;

        while (true)
        {
            int layout = lower.indexOf("layout", from);

            if (layout < 0)
            {
                return null;
            }

            int open = lower.indexOf('(', layout);
            int close = lower.indexOf(')', open);

            if (open < 0 || close < 0)
            {
                return null;
            }

            String after = lower.substring(close + 1).trim();

            if (after.startsWith(qualifier) && (after.length() == qualifier.length() || !isIdentChar(after.charAt(qualifier.length()))))
            {
                return source.substring(open + 1, close);
            }

            from = layout + 6;
        }
    }

    private static String primitiveFromLayout(String args, boolean output)
    {
        String[] parts = args.split(",");

        for (int i = 0; i < parts.length; ++i)
        {
            String token = parts[i].trim();
            int equals = token.indexOf('=');

            if (equals >= 0)
            {
                continue;
            }

            String mapped = mapPrimitive(token, output);

            if (mapped != null)
            {
                return mapped;
            }
        }

        return null;
    }

    private static int maxVerticesFromLayout(String args)
    {
        if (args == null)
        {
            return 0;
        }

        String lower = args.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf("max_vertices");

        if (idx < 0)
        {
            return 0;
        }

        int equals = args.indexOf('=', idx);

        if (equals < 0)
        {
            return 0;
        }

        int start = equals + 1;

        while (start < args.length() && (args.charAt(start) == ' ' || args.charAt(start) == '\t'))
        {
            ++start;
        }

        int end = start;

        while (end < args.length() && args.charAt(end) >= '0' && args.charAt(end) <= '9')
        {
            ++end;
        }

        if (end <= start)
        {
            return 0;
        }

        try
        {
            return Math.max(1, Integer.parseInt(args.substring(start, end)));
        }
        catch (NumberFormatException ignored)
        {
            return 0;
        }
    }

    private static String firstProperty(String source, String[] keys)
    {
        for (int i = 0; i < keys.length; ++i)
        {
            String value = propertyValue(source, keys[i]);

            if (value != null)
            {
                return value;
            }
        }

        return null;
    }

    private static String propertyValue(String source, String key)
    {
        String token = key + ":";
        String upper = source.toUpperCase(Locale.ROOT);
        int idx = 0;

        while (true)
        {
            int found = upper.indexOf(token, idx);

            if (found < 0)
            {
                return null;
            }

            if (found > 0 && isIdentChar(source.charAt(found - 1)))
            {
                idx = found + token.length();
                continue;
            }

            int start = found + token.length();

            while (start < source.length() && (source.charAt(start) == ' ' || source.charAt(start) == '\t'))
            {
                ++start;
            }

            int end = start;

            while (end < source.length() && isIdentChar(source.charAt(end)))
            {
                ++end;
            }

            if (end > start)
            {
                return source.substring(start, end);
            }

            idx = start;
        }
    }

    private static String mapPrimitive(String token, boolean output)
    {
        String name = token.trim().toLowerCase(Locale.ROOT);

        if (name.startsWith("gl_"))
        {
            name = name.substring(3);
        }

        if (name.equals("points") || name.equals("point"))
        {
            return "points";
        }

        if (name.equals("lines_adjacency") || name.equals("line_adjacency"))
        {
            return output ? "line_strip" : "lines_adjacency";
        }

        if (name.equals("triangles_adjacency") || name.equals("triangle_adjacency"))
        {
            return output ? "triangle_strip" : "triangles_adjacency";
        }

        if (name.equals("line_strip") || name.equals("linestrip"))
        {
            return "line_strip";
        }

        if (name.equals("triangle_strip") || name.equals("trianglestrip"))
        {
            return "triangle_strip";
        }

        if (name.equals("triangle_fan") || name.equals("trianglefan"))
        {
            return output ? "triangle_strip" : "triangles";
        }

        if (name.equals("lines") || name.equals("line"))
        {
            return output ? "line_strip" : "lines";
        }

        if (name.equals("triangles") || name.equals("triangle"))
        {
            return output ? "triangle_strip" : "triangles";
        }

        return null;
    }

    private static int indexOfIgnoreCase(String source, String token)
    {
        return source.toLowerCase(Locale.ROOT).indexOf(token.toLowerCase(Locale.ROOT));
    }

    private static boolean isIdentChar(char character)
    {
        return character >= '0' && character <= '9' || character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z' || character == '_';
    }

    private static String insertAfterVersion(String source, String insert)
    {
        String masked = maskShaderComments(source);
        int version = masked.indexOf("#version");
        int end = masked.indexOf('\n', version);

        if (end < 0)
        {
            return source + "\n" + insert;
        }

        int offset = end + 1;
        int insertion = offset;
        int conditionalDepth = 0;
        boolean continuation = false;
        boolean blockComment = endsInBlockComment(source, version, offset, false);

        if (blockComment)
        {
            insertion = source.indexOf("/*", version);
        }

        while (offset < masked.length())
        {
            int lineEnd = masked.indexOf('\n', offset);
            int next = lineEnd < 0 ? masked.length() : lineEnd + 1;
            String line = masked.substring(offset, lineEnd < 0 ? masked.length() : lineEnd).trim();

            if (!line.isEmpty() && !continuation && !line.startsWith("#"))
            {
                break;
            }

            if (!continuation && line.startsWith("#"))
            {
                String directive = line.substring(1).trim();
                int nameEnd = 0;

                while (nameEnd < directive.length() && isIdentChar(directive.charAt(nameEnd)))
                {
                    ++nameEnd;
                }

                String name = directive.substring(0, nameEnd);

                if (name.equals("if") || name.equals("ifdef") || name.equals("ifndef"))
                {
                    ++conditionalDepth;
                }
                else if (name.equals("endif"))
                {
                    --conditionalDepth;
                }
            }

            continuation = line.endsWith("\\");
            blockComment = endsInBlockComment(source, offset, next, blockComment);

            if (conditionalDepth == 0 && !continuation && !blockComment)
            {
                insertion = next;
            }

            offset = next;
        }

        String prefix = source.substring(0, insertion);
        return prefix + (prefix.endsWith("\n") ? "" : "\n") + insert + source.substring(insertion);
    }

    private static boolean endsInBlockComment(String source, int start, int end, boolean block)
    {
        boolean line = false;

        for (int i = start; i < end; ++i)
        {
            char character = source.charAt(i);
            char next = i + 1 < end ? source.charAt(i + 1) : '\0';

            if (line)
            {
                line = character != '\n';
            }
            else if (block)
            {
                if (character == '*' && next == '/')
                {
                    block = false;
                    ++i;
                }
            }
            else if (character == '/' && (next == '/' || next == '*'))
            {
                line = next == '/';
                block = next == '*';
                ++i;
            }
        }

        return block;
    }

    private static String maskShaderComments(String source)
    {
        StringBuilder masked = new StringBuilder(source.length());
        boolean block = false;
        boolean line = false;

        for (int i = 0; i < source.length(); ++i)
        {
            char character = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';

            if (line)
            {
                if (character == '\n')
                {
                    line = false;
                    masked.append(character);
                }
                else
                {
                    masked.append(' ');
                }
            }
            else if (block)
            {
                if (character == '*' && next == '/')
                {
                    block = false;
                    masked.append("  ");
                    ++i;
                }
                else
                {
                    masked.append(character == '\n' ? '\n' : ' ');
                }
            }
            else if (character == '/' && (next == '/' || next == '*'))
            {
                line = next == '/';
                block = next == '*';
                masked.append("  ");
                ++i;
            }
            else
            {
                masked.append(character);
            }
        }

        return masked.toString();
    }

    private static final String COMMON_DEFINES =
        "#define texture2D texture\n" +
        "#define texture3D texture\n" +
        "#define texture2DLod textureLod\n" +
        "#define texture3DLod textureLod\n" +
        "#define texture2DGradARB textureGrad\n" +
        "#define texture2DProj textureProj\n" +
        "#define shadow2D(sampler, coord) vec4(texture(sampler, coord))\n" +
        "#define shadow2DLod(sampler, coord, lod) vec4(textureLod(sampler, coord, lod))\n" +
        "uniform mat4 iris_ModelViewMat;\n" +
        "uniform mat4 iris_ProjMat;\n" +
        "uniform mat3 iris_NormalMat;\n" +
        "uniform mat4 iris_TextureMats[8];\n" +
        "mat4 iris_TexMatInv[8];\n" +
        "void iris_initTextureMatrices() {\n" +
        "  for (int iris_i = 0; iris_i < 8; ++iris_i) iris_TexMatInv[iris_i] = inverse(iris_TextureMats[iris_i]);\n" +
        "}\n" +
        "#define gl_ModelViewMatrix iris_ModelViewMat\n" +
        "#define gl_ProjectionMatrix iris_ProjMat\n" +
        "#define gl_ModelViewProjectionMatrix (iris_ProjMat * iris_ModelViewMat)\n" +
        "#define gl_NormalMatrix iris_NormalMat\n" +
        "#define gl_TextureMatrix iris_TextureMats\n" +
        "#define gl_ModelViewMatrixInverse inverse(iris_ModelViewMat)\n" +
        "#define gl_ProjectionMatrixInverse inverse(iris_ProjMat)\n" +
        "#define gl_ModelViewProjectionMatrixInverse inverse(iris_ProjMat * iris_ModelViewMat)\n" +
        "#define gl_TextureMatrixInverse iris_TexMatInv\n" +
        "#define ftransform() (gl_ModelViewProjectionMatrix * gl_Vertex)\n" +
        "struct iris_FogParameters { vec4 color; float density; float start; float end; float scale; };\n" +
        "uniform iris_FogParameters iris_Fog;\n" +
        "uniform int iris_FogDistanceMode;\n" +
        "#define gl_Fog iris_Fog\n" +
        "#define gl_MultiTexCoord2 vec4(0.0, 0.0, 0.0, 1.0)\n" +
        "#define gl_MultiTexCoord3 vec4(0.0, 0.0, 0.0, 1.0)\n" +
        "#define gl_MultiTexCoord4 vec4(0.0, 0.0, 0.0, 1.0)\n" +
        "#define gl_MultiTexCoord5 vec4(0.0, 0.0, 0.0, 1.0)\n" +
        "#define gl_MultiTexCoord6 vec4(0.0, 0.0, 0.0, 1.0)\n" +
        "#define gl_MultiTexCoord7 vec4(0.0, 0.0, 0.0, 1.0)\n";

    private static final String VERTEX_PRELUDE =
        COMMON_DEFINES +
        "layout(location=0) in vec3 iris_Position;\n" +
        "layout(location=1) in vec4 iris_Color;\n" +
        "layout(location=2) in vec2 iris_UV0;\n" +
        "layout(location=3) in vec2 iris_UV1;\n" +
        "layout(location=4) in vec3 iris_Normal;\n" +
        "#define gl_Vertex vec4(iris_Position, 1.0)\n" +
        "#define gl_Color iris_Color\n" +
        "#define gl_MultiTexCoord0 vec4(iris_UV0, 0.0, 1.0)\n" +
        "#define gl_MultiTexCoord1 vec4(iris_UV1, 0.0, 1.0)\n" +
        "#define gl_Normal iris_Normal\n" +
        "out iris_Vert {\n" +
        "  vec4 FrontColor;\n" +
        "  vec4 TexCoord[8];\n" +
        "  float FogFragCoord;\n" +
        "} iris_v;\n" +
        "#define gl_FrontColor iris_v.FrontColor\n" +
        "#define gl_BackColor iris_v.FrontColor\n" +
        "#define gl_TexCoord iris_v.TexCoord\n" +
        "#define gl_FogFragCoord iris_v.FogFragCoord\n";

    private static final String FRAGMENT_PRELUDE =
        COMMON_DEFINES +
        "in iris_Vert {\n" +
        "  vec4 FrontColor;\n" +
        "  vec4 TexCoord[8];\n" +
        "  float FogFragCoord;\n" +
        "} iris_v;\n" +
        "#define gl_Color iris_v.FrontColor\n" +
        "#define gl_FrontColor iris_v.FrontColor\n" +
        "#define gl_TexCoord iris_v.TexCoord\n" +
        "#define gl_FogFragCoord iris_v.FogFragCoord\n" +
        "layout(location=0) out vec4 iris_FragData0;\n" +
        "layout(location=1) out vec4 iris_FragData1;\n" +
        "layout(location=2) out vec4 iris_FragData2;\n" +
        "layout(location=3) out vec4 iris_FragData3;\n" +
        "layout(location=4) out vec4 iris_FragData4;\n" +
        "layout(location=5) out vec4 iris_FragData5;\n" +
        "layout(location=6) out vec4 iris_FragData6;\n" +
        "layout(location=7) out vec4 iris_FragData7;\n" +
        "vec4 iris_FragColor;\n";

    private static final String DEFAULT_VERTEX =
        "#version 330 core\n" +
        "layout(location=0) in vec3 Position;\n" +
        "layout(location=1) in vec4 Color;\n" +
        "layout(location=2) in vec2 UV0;\n" +
        "layout(location=3) in vec2 UV1;\n" +
        "layout(location=4) in vec3 Normal;\n" +
        "uniform mat4 ModelViewMat;\n" +
        "uniform mat4 ProjMat;\n" +
        "uniform mat4 TextureMat0;\n" +
        "uniform mat4 TextureMat1;\n" +
        "uniform ivec4 TexGenMode;\n" +
        "uniform vec4 TexGenPlane[4];\n" +
        "uniform vec4 ColorModulator;\n" +
        "uniform int UseVertexColor;\n" +
        "uniform int LightingEnabled;\n" +
        "uniform int LightMask;\n" +
        "uniform int NormalizeNormals;\n" +
        "uniform int RescaleNormals;\n" +
        "uniform float NormalScale;\n" +
        "uniform vec3 Light0Dir;\n" +
        "uniform vec3 Light1Dir;\n" +
        "uniform float LightAmbient;\n" +
        "uniform float LightDiffuse;\n" +
        "uniform mat3 NormalMat;\n" +
        "uniform int FogDistanceMode;\n" +
        "out vec4 vertexColor;\n" +
        "flat out vec4 flatColor;\n" +
        "out vec4 texCoord0;\n" +
        "out vec4 texCoord1;\n" +
        "out float vertexFog;\n" +
        "void main() {\n" +
        "  vec4 view = ModelViewMat * vec4(Position, 1.0);\n" +
        "  gl_Position = ProjMat * view;\n" +
        "  vec4 color = UseVertexColor != 0 ? Color : ColorModulator;\n" +
        "  vec3 normal = NormalMat * Normal;\n" +
        "  if (NormalizeNormals != 0) normal = normalize(normal);\n" +
        "  else if (RescaleNormals != 0) normal *= NormalScale;\n" +
        "  if (LightingEnabled != 0) {\n" +
        "    float d0 = (LightMask & 1) != 0 ? max(dot(normal, normalize(Light0Dir)), 0.0) : 0.0;\n" +
        "    float d1 = (LightMask & 2) != 0 ? max(dot(normal, normalize(Light1Dir)), 0.0) : 0.0;\n" +
        "    color.rgb = clamp(color.rgb * (LightAmbient + LightDiffuse * (d0 + d1)), 0.0, 1.0);\n" +
        "  }\n" +
        "  vertexColor = color;\n" +
        "  flatColor = color;\n" +
        "  vec4 coord = vec4(UV0, 0.0, 1.0);\n" +
        "  vec3 reflection = reflect(normalize(view.xyz), normal);\n" +
        "  float sphereScale = 2.0 * length(vec3(reflection.xy, reflection.z + 1.0));\n" +
        "  for (int i = 0; i < 4; ++i) {\n" +
        "    if (TexGenMode[i] == 9217) coord[i] = dot(TexGenPlane[i], vec4(Position, 1.0));\n" +
        "    else if (TexGenMode[i] == 9216) coord[i] = dot(TexGenPlane[i], view);\n" +
        "    else if (TexGenMode[i] == 9218 && i < 2) coord[i] = reflection[i] / max(sphereScale, 0.00000001) + 0.5;\n" +
        "    else if (TexGenMode[i] == 34065 && i < 3) coord[i] = normal[i];\n" +
        "    else if (TexGenMode[i] == 34066 && i < 3) coord[i] = reflection[i];\n" +
        "  }\n" +
        "  texCoord0 = TextureMat0 * coord;\n" +
        "  texCoord1 = TextureMat1 * vec4(UV1, 0.0, 1.0);\n" +
        "  vertexFog = FogDistanceMode == 34139 ? length(view.xyz) : (FogDistanceMode == 9216 ? view.z : abs(view.z));\n" +
        "}\n";

    private static final String DEFAULT_FRAGMENT =
        "#version 330 core\n" +
        "uniform sampler2D Sampler0;\n" +
        "uniform sampler2D Sampler1;\n" +
        "uniform int TextureEnabled;\n" +
        "uniform int UseLightmap;\n" +
        "uniform int AlphaTestFunc;\n" +
        "uniform float AlphaTestRef;\n" +
        "uniform int FogMode;\n" +
        "uniform vec4 FogColor;\n" +
        "uniform float FogStart;\n" +
        "uniform float FogEnd;\n" +
        "uniform float FogDensity;\n" +
        "uniform vec4 OverlayColor;\n" +
        "uniform int OverlayEnabled;\n" +
        "uniform int FlatShading;\n" +
        "in vec4 vertexColor;\n" +
        "flat in vec4 flatColor;\n" +
        "in vec4 texCoord0;\n" +
        "in vec4 texCoord1;\n" +
        "in float vertexFog;\n" +
        "out vec4 fragColor;\n" +
        "void main() {\n" +
        "  vec4 color = FlatShading != 0 ? flatColor : vertexColor;\n" +
        "  if (TextureEnabled != 0) {\n" +
        "    vec4 base = textureProj(Sampler0, texCoord0);\n" +
        "    color *= base;\n" +
        "    if (OverlayEnabled != 0) color.a = base.a;\n" +
        "  }\n" +
        "  if (OverlayEnabled != 0) color.rgb = mix(color.rgb, OverlayColor.rgb, OverlayColor.a);\n" +
        "  if (UseLightmap != 0) {\n" +
        "    vec4 lightmap = textureProj(Sampler1, texCoord1);\n" +
        "    color.rgb *= lightmap.rgb;\n" +
        "    if (OverlayEnabled == 0) color.a *= lightmap.a;\n" +
        "  }\n" +
        "  if (AlphaTestFunc == 516 && color.a <= AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 518 && color.a < AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 514 && color.a != AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 517 && color.a == AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 513 && color.a >= AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 515 && color.a > AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 512) discard;\n" +
        "  if (FogMode == 9729) {\n" +
        "    float f = clamp((FogEnd - vertexFog) / max(FogEnd - FogStart, 0.0001), 0.0, 1.0);\n" +
        "    color.rgb = mix(FogColor.rgb, color.rgb, f);\n" +
        "  } else if (FogMode == 2048) {\n" +
        "    color.rgb = mix(FogColor.rgb, color.rgb, clamp(exp(-FogDensity * vertexFog), 0.0, 1.0));\n" +
        "  } else if (FogMode == 2049) {\n" +
        "    color.rgb = mix(FogColor.rgb, color.rgb, clamp(exp(-FogDensity * FogDensity * vertexFog * vertexFog), 0.0, 1.0));\n" +
        "  }\n" +
        "  fragColor = color;\n" +
        "}\n";
}
