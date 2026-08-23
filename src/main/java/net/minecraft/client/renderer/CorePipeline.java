package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static int uniformColor;
    private static int uniformUseVertexColor;
    private static int uniformTextureEnabled;
    private static int uniformUseLightmap;
    private static int uniformLighting;
    private static int uniformAlphaFunc;
    private static int uniformAlphaRef;
    private static int uniformFogMode;
    private static int uniformFogColor;
    private static int uniformFogStart;
    private static int uniformFogEnd;
    private static int uniformFogDensity;
    private static int uniformSampler0;
    private static int uniformSampler1;
    private static int uniformOverlay;
    private static int quadIndexBuffer;
    private static int quadIndexCapacity;
    private static int streamVbo;
    private static int streamCapacity;
    private static int lastPackProgram;
    private static final Map<Integer, int[]> packUniforms = new HashMap<Integer, int[]>();
    private static final float[] fogColor = new float[] {0.0F, 0.0F, 0.0F, 1.0F};
    private static final float[] normalMatrix = new float[9];
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

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0)
        {
            throw new IllegalStateException("Default core shader failed to link: " + GL20.glGetProgramInfoLog(program, 4096));
        }

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        uniformModelView = GL20.glGetUniformLocation(program, "ModelViewMat");
        uniformProj = GL20.glGetUniformLocation(program, "ProjMat");
        uniformColor = GL20.glGetUniformLocation(program, "ColorModulator");
        uniformUseVertexColor = GL20.glGetUniformLocation(program, "UseVertexColor");
        uniformTextureEnabled = GL20.glGetUniformLocation(program, "TextureEnabled");
        uniformUseLightmap = GL20.glGetUniformLocation(program, "UseLightmap");
        uniformLighting = GL20.glGetUniformLocation(program, "LightingEnabled");
        uniformAlphaFunc = GL20.glGetUniformLocation(program, "AlphaTestFunc");
        uniformAlphaRef = GL20.glGetUniformLocation(program, "AlphaTestRef");
        uniformFogMode = GL20.glGetUniformLocation(program, "FogMode");
        uniformFogColor = GL20.glGetUniformLocation(program, "FogColor");
        uniformFogStart = GL20.glGetUniformLocation(program, "FogStart");
        uniformFogEnd = GL20.glGetUniformLocation(program, "FogEnd");
        uniformFogDensity = GL20.glGetUniformLocation(program, "FogDensity");
        uniformSampler0 = GL20.glGetUniformLocation(program, "Sampler0");
        uniformSampler1 = GL20.glGetUniformLocation(program, "Sampler1");
        uniformOverlay = GL20.glGetUniformLocation(program, "OverlayColor");
        bindDefault();
        ready = true;
    }

    public static void uploadImmediate(ByteBuffer data)
    {
        init();
        OpenGlHelper.bindDefaultVertexArray();

        if (streamVbo == 0)
        {
            streamVbo = GL15.glGenBuffers();
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, streamVbo);
        int size = data.remaining();

        if (size <= 0)
        {
            return;
        }

        int position = data.position();
        int limit = data.limit();

        if (size > streamCapacity)
        {
            streamCapacity = size;
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STREAM_DRAW);
        }
        else
        {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long)streamCapacity, GL15.GL_STREAM_DRAW);
            data.position(position);
            data.limit(limit);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, data);
        }

        data.position(position);
        data.limit(limit);
    }

    public static void bindDefault()
    {
        GL20.glUseProgram(program);
        lastPackProgram = 0;
        uploadDefaultUniforms(false, false);
    }

    public static void notePackProgram(int programId)
    {
        lastPackProgram = programId;
        uniformsFor(programId);
    }

    public static void prepareDraw(boolean hasColor, boolean hasLightmap)
    {
        init();
        int current = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        if (current == 0 || current == program)
        {
            if (current != program)
            {
                GL20.glUseProgram(program);
            }

            lastPackProgram = 0;
            uploadDefaultUniforms(hasColor, hasLightmap);
        }
        else
        {
            lastPackProgram = current;
            uploadPackMatrices(current);
        }
    }

    public static void draw(int mode, int first, int count)
    {
        if (count <= 0)
        {
            return;
        }

        if (mode == 7)
        {
            int quads = count / 4;
            ensureQuadIndices(quads);
            OpenGlHelper.bindDefaultVertexArray();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadIndexBuffer);
            GL32.glDrawElementsBaseVertex(GL11.GL_TRIANGLES, quads * 6, GL11.GL_UNSIGNED_INT, 0L, first);
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
            GL20.glVertexAttrib2f(ATTR_UV1, 0.0F, 0.0F);
        }

        if (!hasNormal)
        {
            GL20.glDisableVertexAttribArray(ATTR_NORMAL);
            GL20.glVertexAttrib3f(ATTR_NORMAL, GlStateManager.getNormalX(), GlStateManager.getNormalY(), GlStateManager.getNormalZ());
        }

        prepareDraw(hasColor, hasUv1);
    }

    public static void clearVertexFormat(VertexFormat vertexFormat)
    {
        GL20.glDisableVertexAttribArray(ATTR_POSITION);
        GL20.glDisableVertexAttribArray(ATTR_COLOR);
        GL20.glDisableVertexAttribArray(ATTR_UV0);
        GL20.glDisableVertexAttribArray(ATTR_UV1);
        GL20.glDisableVertexAttribArray(ATTR_NORMAL);
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
            rewritten = rewritten + "\n#undef main\nvoid main() {\n  iris_FrontColor = iris_Color;\n  iris_user_main();\n}\n";
        }
        else if (shaderType == SHADER_FRAGMENT)
        {
            rewritten = rewritten.replaceAll("(?<![A-Za-z0-9_])varying\\s+", "in ");
            rewritten = insertAfterVersion(rewritten, FRAGMENT_PRELUDE + "\n#define main iris_user_main\n");
            rewritten = rewritten + "\n#undef main\nvoid main() {\n  iris_FragColor = vec4(1.0);\n  iris_FragData0 = vec4(1.0);\n  iris_user_main();\n  if (iris_FragData0 == vec4(1.0) && iris_FragColor != vec4(1.0)) iris_FragData0 = iris_FragColor;\n}\n";
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
        GL20.glUniformMatrix4fv(uniformModelView, false, GlMatrix.getModelView());
        GL20.glUniformMatrix4fv(uniformProj, false, GlMatrix.getProjection());
        GL20.glUniform4f(uniformColor, GlStateManager.getColorRed(), GlStateManager.getColorGreen(), GlStateManager.getColorBlue(), GlStateManager.getColorAlpha());
        GL20.glUniform1i(uniformUseVertexColor, hasColor ? 1 : 0);
        GL20.glUniform1i(uniformTextureEnabled, GlStateManager.isTexture2DEnabled() ? 1 : 0);
        GL20.glUniform1i(uniformUseLightmap, hasLightmap ? 1 : 0);
        GL20.glUniform1i(uniformLighting, GlStateManager.isLightingEnabled() ? 1 : 0);
        GL20.glUniform1i(uniformAlphaFunc, GlStateManager.isAlphaTestEnabled() ? GlStateManager.getAlphaFunc() : 0);
        GL20.glUniform1f(uniformAlphaRef, GlStateManager.getAlphaRef());
        GL20.glUniform1i(uniformFogMode, GlStateManager.isFogEnabled() ? GlStateManager.getFogMode() : 0);
        GlStateManager.getFogColor(fogColor);
        GL20.glUniform4f(uniformFogColor, fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
        GL20.glUniform1f(uniformFogStart, GlStateManager.getFogStart());
        GL20.glUniform1f(uniformFogEnd, GlStateManager.getFogEnd());
        GL20.glUniform1f(uniformFogDensity, GlStateManager.getFogDensity());

        if (uniformSampler0 >= 0)
        {
            GL20.glUniform1i(uniformSampler0, 0);
        }

        if (uniformSampler1 >= 0)
        {
            GL20.glUniform1i(uniformSampler1, 1);
        }

        if (uniformOverlay >= 0)
        {
            GL20.glUniform4f(uniformOverlay, GlStateManager.getOverlayRed(), GlStateManager.getOverlayGreen(), GlStateManager.getOverlayBlue(), GlStateManager.getOverlayAlpha());
        }
    }

    private static void uploadPackMatrices(int programId)
    {
        int[] uniforms = uniformsFor(programId);

        if (uniforms[0] >= 0)
        {
            GL20.glUniformMatrix4fv(uniforms[0], false, GlMatrix.getModelView());
        }

        if (uniforms[1] >= 0)
        {
            GL20.glUniformMatrix4fv(uniforms[1], false, GlMatrix.getProjection());
        }

        if (uniforms[2] >= 0)
        {
            float[] modelView = GlMatrix.getModelView();
            normalMatrix[0] = modelView[0];
            normalMatrix[1] = modelView[1];
            normalMatrix[2] = modelView[2];
            normalMatrix[3] = modelView[4];
            normalMatrix[4] = modelView[5];
            normalMatrix[5] = modelView[6];
            normalMatrix[6] = modelView[8];
            normalMatrix[7] = modelView[9];
            normalMatrix[8] = modelView[10];
            GL20.glUniformMatrix3fv(uniforms[2], false, normalMatrix);
        }

        GlStateManager.getFogColor(fogColor);

        if (uniforms[3] >= 0)
        {
            GL20.glUniform4f(uniforms[3], fogColor[0], fogColor[1], fogColor[2], fogColor[3]);
        }

        if (uniforms[4] >= 0)
        {
            GL20.glUniform1f(uniforms[4], GlStateManager.getFogDensity());
        }

        if (uniforms[5] >= 0)
        {
            GL20.glUniform1f(uniforms[5], GlStateManager.getFogStart());
        }

        if (uniforms[6] >= 0)
        {
            GL20.glUniform1f(uniforms[6], GlStateManager.getFogEnd());
        }
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
                GL20.glGetUniformLocation(programId, "iris_Fog.end")
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

        OpenGlHelper.bindDefaultVertexArray();
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
        int version = source.indexOf("#version");

        if (version < 0)
        {
            return "#version 330 core\n" + source;
        }

        int end = source.indexOf('\n', version);

        if (end < 0)
        {
            end = source.length();
        }

        return source.substring(0, version) + "#version 330 core" + source.substring(end);
    }

    private static String insertAfterVersion(String source, String insert)
    {
        int version = source.indexOf("#version");
        int end = source.indexOf('\n', version);

        if (end < 0)
        {
            return source + "\n" + insert;
        }

        return source.substring(0, end + 1) + insert + source.substring(end + 1);
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
        "#define gl_ModelViewMatrix iris_ModelViewMat\n" +
        "#define gl_ProjectionMatrix iris_ProjMat\n" +
        "#define gl_ModelViewProjectionMatrix (iris_ProjMat * iris_ModelViewMat)\n" +
        "#define gl_NormalMatrix iris_NormalMat\n" +
        "#define ftransform() (gl_ModelViewProjectionMatrix * gl_Vertex)\n" +
        "struct iris_FogParameters { vec4 color; float density; float start; float end; float scale; };\n" +
        "uniform iris_FogParameters iris_Fog;\n" +
        "#define gl_Fog iris_Fog\n";

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
        "out vec4 iris_FrontColor;\n" +
        "out vec4 iris_TexCoord[8];\n" +
        "out float iris_FogFragCoord;\n" +
        "#define gl_FrontColor iris_FrontColor\n" +
        "#define gl_BackColor iris_FrontColor\n" +
        "#define gl_TexCoord iris_TexCoord\n" +
        "#define gl_FogFragCoord iris_FogFragCoord\n";

    private static final String FRAGMENT_PRELUDE =
        COMMON_DEFINES +
        "in vec4 iris_FrontColor;\n" +
        "in vec4 iris_TexCoord[8];\n" +
        "in float iris_FogFragCoord;\n" +
        "#define gl_Color iris_FrontColor\n" +
        "#define gl_FrontColor iris_FrontColor\n" +
        "#define gl_TexCoord iris_TexCoord\n" +
        "#define gl_FogFragCoord iris_FogFragCoord\n" +
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
        "uniform vec4 ColorModulator;\n" +
        "uniform int UseVertexColor;\n" +
        "uniform int LightingEnabled;\n" +
        "out vec4 vertexColor;\n" +
        "out vec2 texCoord0;\n" +
        "out vec2 texCoord1;\n" +
        "out float vertexFog;\n" +
        "void main() {\n" +
        "  vec4 view = ModelViewMat * vec4(Position, 1.0);\n" +
        "  gl_Position = ProjMat * view;\n" +
        "  vec4 color = (UseVertexColor != 0 ? Color : vec4(1.0)) * ColorModulator;\n" +
        "  if (LightingEnabled != 0) {\n" +
        "    vec3 n = mat3(ModelViewMat) * Normal;\n" +
        "    float ndotl = max(dot(normalize(n), vec3(0.0, 0.0, 1.0)), 0.0);\n" +
        "    color.rgb *= 0.6 + 0.4 * ndotl;\n" +
        "  }\n" +
        "  vertexColor = color;\n" +
        "  texCoord0 = UV0;\n" +
        "  texCoord1 = UV1;\n" +
        "  vertexFog = length(view.xyz);\n" +
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
        "in vec4 vertexColor;\n" +
        "in vec2 texCoord0;\n" +
        "in vec2 texCoord1;\n" +
        "in float vertexFog;\n" +
        "out vec4 fragColor;\n" +
        "void main() {\n" +
        "  vec4 color = vertexColor;\n" +
        "  if (TextureEnabled != 0) color *= texture(Sampler0, texCoord0);\n" +
        "  if (UseLightmap != 0) color *= texture(Sampler1, texCoord1 / 256.0);\n" +
        "  if (OverlayColor.a > 0.0) color.rgb = mix(color.rgb, OverlayColor.rgb, OverlayColor.a);\n" +
        "  if (AlphaTestFunc == 516 && color.a <= AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 518 && color.a < AlphaTestRef) discard;\n" +
        "  else if (AlphaTestFunc == 514 && abs(color.a - AlphaTestRef) > 0.0001) discard;\n" +
        "  else if (AlphaTestFunc == 517 && abs(color.a - AlphaTestRef) <= 0.0001) discard;\n" +
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
