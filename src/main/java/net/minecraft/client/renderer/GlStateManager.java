package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.src.Config;
import net.optifine.SmartAnimations;
import net.optifine.render.GlAlphaState;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.Shaders;
import net.optifine.util.LockCounter;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class GlStateManager
{
    private static GlStateManager.AlphaState alphaState = new GlStateManager.AlphaState();
    private static GlStateManager.BooleanState lightingState = new GlStateManager.BooleanState(2896);
    private static GlStateManager.BooleanState[] lightState = new GlStateManager.BooleanState[8];
    private static GlStateManager.ColorMaterialState colorMaterialState = new GlStateManager.ColorMaterialState();
    private static GlStateManager.BlendState blendState = new GlStateManager.BlendState();
    private static GlStateManager.DepthState depthState = new GlStateManager.DepthState();
    private static GlStateManager.FogState fogState = new GlStateManager.FogState();
    private static GlStateManager.CullState cullState = new GlStateManager.CullState();
    private static GlStateManager.PolygonOffsetState polygonOffsetState = new GlStateManager.PolygonOffsetState();
    private static GlStateManager.ColorLogicState colorLogicState = new GlStateManager.ColorLogicState();
    private static GlStateManager.TexGenState texGenState = new GlStateManager.TexGenState();
    private static GlStateManager.ClearState clearState = new GlStateManager.ClearState();
    private static GlStateManager.StencilState stencilState = new GlStateManager.StencilState();
    private static GlStateManager.BooleanState normalizeState = new GlStateManager.BooleanState(2977);
    private static int activeTextureUnit = 0;
    private static GlStateManager.TextureState[] textureState = new GlStateManager.TextureState[32];
    private static int activeShadeModel = 7425;
    private static GlStateManager.BooleanState rescaleNormalState = new GlStateManager.BooleanState(32826);
    private static GlStateManager.ColorMask colorMaskState = new GlStateManager.ColorMask();
    private static GlStateManager.Color colorState = new GlStateManager.Color();
    private static float normalX = 0.0F;
    private static float normalY = 0.0F;
    private static float normalZ = 1.0F;
    private static float overlayRed;
    private static float overlayGreen;
    private static float overlayBlue;
    private static float overlayAlpha;
    private static final float[] light0Dir = new float[] {0.2F, 1.0F, -0.7F};
    private static final float[] light1Dir = new float[] {-0.2F, 1.0F, 0.7F};
    private static float lightAmbient = 0.4F;
    private static float lightDiffuse = 0.6F;
    public static boolean clearEnabled = true;
    private static LockCounter alphaLock = new LockCounter();
    private static GlAlphaState alphaLockState = new GlAlphaState();
    private static LockCounter blendLock = new LockCounter();
    private static GlBlendState blendLockState = new GlBlendState();
    public static void pushAttrib()
    {
    }

    public static void popAttrib()
    {
    }

    public static void disableAlpha()
    {
        if (alphaLock.isLocked())
        {
            alphaLockState.setDisabled();
        }
        else
        {
            alphaState.alphaTest.setDisabled();
        }
    }

    public static void enableAlpha()
    {
        if (alphaLock.isLocked())
        {
            alphaLockState.setEnabled();
        }
        else
        {
            alphaState.alphaTest.setEnabled();
        }
    }

    public static void alphaFunc(int func, float ref)
    {
        if (alphaLock.isLocked())
        {
            alphaLockState.setFuncRef(func, ref);
        }
        else
        {
            if (func != alphaState.func || ref != alphaState.ref)
            {
                alphaState.func = func;
                alphaState.ref = ref;
            }
        }
    }

    public static void enableLighting()
    {
        lightingState.setEnabled();
    }

    public static void disableLighting()
    {
        lightingState.setDisabled();
    }

    public static void enableLight(int light)
    {
        lightState[light].setEnabled();
    }

    public static void disableLight(int light)
    {
        lightState[light].setDisabled();
    }

    public static void enableColorMaterial()
    {
        colorMaterialState.colorMaterial.setEnabled();
    }

    public static void disableColorMaterial()
    {
        colorMaterialState.colorMaterial.setDisabled();
    }

    public static void colorMaterial(int face, int mode)
    {
        if (face != colorMaterialState.face || mode != colorMaterialState.mode)
        {
            colorMaterialState.face = face;
            colorMaterialState.mode = mode;
        }
    }

    public static void disableDepth()
    {
        depthState.depthTest.setDisabled();
    }

    public static void enableDepth()
    {
        depthState.depthTest.setEnabled();
    }

    public static void depthFunc(int depthFunc)
    {
        if (depthFunc != depthState.depthFunc)
        {
            depthState.depthFunc = depthFunc;
            GL11.glDepthFunc(depthFunc);
        }
    }

    public static void depthMask(boolean flagIn)
    {
        if (flagIn != depthState.maskEnabled)
        {
            depthState.maskEnabled = flagIn;
            GL11.glDepthMask(flagIn);
        }
    }

    public static void disableBlend()
    {
        if (blendLock.isLocked())
        {
            blendLockState.setDisabled();
        }
        else
        {
            blendState.blend.setDisabled();
        }
    }

    public static void enableBlend()
    {
        if (blendLock.isLocked())
        {
            blendLockState.setEnabled();
        }
        else
        {
            blendState.blend.setEnabled();
        }
    }

    public static void blendFunc(int srcFactor, int dstFactor)
    {
        if (blendLock.isLocked())
        {
            blendLockState.setFactors(srcFactor, dstFactor);
        }
        else
        {
            if (srcFactor != blendState.srcFactor || dstFactor != blendState.dstFactor || srcFactor != blendState.srcFactorAlpha || dstFactor != blendState.dstFactorAlpha)
            {
                blendState.srcFactor = srcFactor;
                blendState.dstFactor = dstFactor;
                blendState.srcFactorAlpha = srcFactor;
                blendState.dstFactorAlpha = dstFactor;

                if (Config.isShaders())
                {
                    Shaders.uniform_blendFunc.setValue(srcFactor, dstFactor, srcFactor, dstFactor);
                }

                GL11.glBlendFunc(srcFactor, dstFactor);
            }
        }
    }

    public static void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha)
    {
        if (blendLock.isLocked())
        {
            blendLockState.setFactors(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
        }
        else
        {
            if (srcFactor != blendState.srcFactor || dstFactor != blendState.dstFactor || srcFactorAlpha != blendState.srcFactorAlpha || dstFactorAlpha != blendState.dstFactorAlpha)
            {
                blendState.srcFactor = srcFactor;
                blendState.dstFactor = dstFactor;
                blendState.srcFactorAlpha = srcFactorAlpha;
                blendState.dstFactorAlpha = dstFactorAlpha;

                if (Config.isShaders())
                {
                    Shaders.uniform_blendFunc.setValue(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
                }

                OpenGlHelper.glBlendFunc(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
            }
        }
    }

    public static void enableFog()
    {
        fogState.fog.setEnabled();
    }

    public static void disableFog()
    {
        fogState.fog.setDisabled();
    }

    public static void setFog(int param)
    {
        if (param != fogState.mode)
        {
            fogState.mode = param;

            if (Config.isShaders())
            {
                Shaders.setFogMode(param);
            }
        }
    }

    public static void setFogDensity(float param)
    {
        if (param < 0.0F)
        {
            param = 0.0F;
        }

        if (param != fogState.density)
        {
            fogState.density = param;

            if (Config.isShaders())
            {
                Shaders.setFogDensity(param);
            }
        }
    }

    public static void setFogStart(float param)
    {
        if (param != fogState.start)
        {
            fogState.start = param;
        }
    }

    public static void setFogEnd(float param)
    {
        if (param != fogState.end)
        {
            fogState.end = param;
        }
    }

    public static void glFog(int pname, FloatBuffer params)
    {
        if (pname == 2918 && params != null && params.remaining() >= 4)
        {
            int position = params.position();
            fogState.color[0] = params.get(position);
            fogState.color[1] = params.get(position + 1);
            fogState.color[2] = params.get(position + 2);
            fogState.color[3] = params.get(position + 3);
        }
    }

    public static void glFogi(int pname, int param)
    {
        if (pname == 2917)
        {
            fogState.mode = param;
        }
    }

    public static void enableCull()
    {
        cullState.cullFace.setEnabled();
    }

    public static void disableCull()
    {
        cullState.cullFace.setDisabled();
    }

    public static void cullFace(int mode)
    {
        if (mode != cullState.mode)
        {
            cullState.mode = mode;
            GL11.glCullFace(mode);
        }
    }

    public static void enablePolygonOffset()
    {
        polygonOffsetState.polygonOffsetFill.setEnabled();
    }

    public static void disablePolygonOffset()
    {
        polygonOffsetState.polygonOffsetFill.setDisabled();
    }

    public static void doPolygonOffset(float factor, float units)
    {
        if (factor != polygonOffsetState.factor || units != polygonOffsetState.units)
        {
            polygonOffsetState.factor = factor;
            polygonOffsetState.units = units;
            GL11.glPolygonOffset(factor, units);
        }
    }

    public static void enableColorLogic()
    {
        colorLogicState.colorLogicOp.setEnabled();
    }

    public static void disableColorLogic()
    {
        colorLogicState.colorLogicOp.setDisabled();
    }

    public static void colorLogicOp(int opcode)
    {
        if (opcode != colorLogicState.opcode)
        {
            colorLogicState.opcode = opcode;
            GL11.glLogicOp(opcode);
        }
    }

    public static void enableTexGenCoord(GlStateManager.TexGen texGen)
    {
        texGenCoord(texGen).textureGen.setEnabled();
    }

    public static void disableTexGenCoord(GlStateManager.TexGen texGen)
    {
        texGenCoord(texGen).textureGen.setDisabled();
    }

    public static void texGen(GlStateManager.TexGen texGen, int param)
    {
        GlStateManager.TexGenCoord texGenCoord = texGenCoord(texGen);

        texGenCoord.param = param;
    }

    public static void texGen(GlStateManager.TexGen texGen, int pname, FloatBuffer params)
    {
    }

    private static GlStateManager.TexGenCoord texGenCoord(GlStateManager.TexGen texGen)
    {
        switch (texGen)
        {
            case S:
                return texGenState.sCoord;

            case T:
                return texGenState.tCoord;

            case R:
                return texGenState.rCoord;

            case Q:
                return texGenState.qCoord;

            default:
                return texGenState.sCoord;
        }
    }

    public static void setActiveTexture(int texture)
    {
        if (activeTextureUnit != texture - OpenGlHelper.defaultTexUnit)
        {
            activeTextureUnit = texture - OpenGlHelper.defaultTexUnit;
            OpenGlHelper.setActiveTexture(texture);
        }
    }

    public static void enableTexture2D()
    {
        textureState[activeTextureUnit].texture2DState.setEnabled();
    }

    public static void disableTexture2D()
    {
        textureState[activeTextureUnit].texture2DState.setDisabled();
    }

    public static int generateTexture()
    {
        return GL11.glGenTextures();
    }

    public static void deleteTexture(int texture)
    {
        if (texture != 0)
        {
            GL11.glDeleteTextures(texture);

            for (GlStateManager.TextureState textureStateEntry : textureState)
            {
                if (textureStateEntry.textureName == texture)
                {
                    textureStateEntry.textureName = 0;
                }
            }
        }
    }

    public static void bindTexture(int texture)
    {
        if (texture != textureState[activeTextureUnit].textureName)
        {
            textureState[activeTextureUnit].textureName = texture;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

            if (SmartAnimations.isActive())
            {
                SmartAnimations.textureRendered(texture);
            }
        }
    }

    public static void enableNormalize()
    {
        normalizeState.setEnabled();
    }

    public static void disableNormalize()
    {
        normalizeState.setDisabled();
    }

    public static void shadeModel(int mode)
    {
        activeShadeModel = mode;
    }

    public static int getShadeModel()
    {
        return activeShadeModel;
    }

    public static void enableRescaleNormal()
    {
        rescaleNormalState.setEnabled();
    }

    public static void disableRescaleNormal()
    {
        rescaleNormalState.setDisabled();
    }

    public static void viewport(int x, int y, int width, int height)
    {
        if (width > 0 && height > 0)
        {
            GL11.glViewport(x, y, width, height);
        }
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha)
    {
        if (red != colorMaskState.red || green != colorMaskState.green || blue != colorMaskState.blue || alpha != colorMaskState.alpha)
        {
            colorMaskState.red = red;
            colorMaskState.green = green;
            colorMaskState.blue = blue;
            colorMaskState.alpha = alpha;
            GL11.glColorMask(red, green, blue, alpha);
        }
    }

    public static void clearDepth(double depth)
    {
        if (depth != clearState.depth)
        {
            clearState.depth = depth;
            GL11.glClearDepth(depth);
        }
    }

    public static void clearColor(float red, float green, float blue, float alpha)
    {
        if (red != clearState.color.red || green != clearState.color.green || blue != clearState.color.blue || alpha != clearState.color.alpha)
        {
            clearState.color.red = red;
            clearState.color.green = green;
            clearState.color.blue = blue;
            clearState.color.alpha = alpha;
            GL11.glClearColor(red, green, blue, alpha);
        }
    }

    public static void clear(int mask)
    {
        if (clearEnabled)
        {
            GL11.glClear(mask);
        }
    }

    public static void matrixMode(int mode)
    {
        GlMatrix.matrixMode(mode);
    }

    public static void loadIdentity()
    {
        GlMatrix.loadIdentity();
    }

    public static void pushMatrix()
    {
        GlMatrix.pushMatrix();
    }

    public static void popMatrix()
    {
        GlMatrix.popMatrix();
    }

    public static void getFloat(int pname, FloatBuffer params)
    {
        GlMatrix.getFloat(pname, params);
    }

    public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        GlMatrix.ortho(left, right, bottom, top, zNear, zFar);
    }

    public static void frustum(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        GlMatrix.frustum(left, right, bottom, top, zNear, zFar);
    }

    public static void rotate(float angle, float x, float y, float z)
    {
        GlMatrix.rotate(angle, x, y, z);
    }

    public static void scale(float x, float y, float z)
    {
        GlMatrix.scale(x, y, z);
    }

    public static void scale(double x, double y, double z)
    {
        GlMatrix.scale((float)x, (float)y, (float)z);
    }

    public static void translate(float x, float y, float z)
    {
        GlMatrix.translate(x, y, z);
    }

    public static void translate(double x, double y, double z)
    {
        GlMatrix.translate((float)x, (float)y, (float)z);
    }

    public static void multMatrix(FloatBuffer matrix)
    {
        GlMatrix.multMatrix(matrix);
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha)
    {
        colorState.red = colorRed;
        colorState.green = colorGreen;
        colorState.blue = colorBlue;
        colorState.alpha = colorAlpha;
    }

    public static void color(float colorRed, float colorGreen, float colorBlue)
    {
        color(colorRed, colorGreen, colorBlue, 1.0F);
    }

    public static void resetColor()
    {
        colorState.red = colorState.green = colorState.blue = colorState.alpha = -1.0F;
    }

    public static void normal(float x, float y, float z)
    {
        normalX = x;
        normalY = y;
        normalZ = z;
    }

    public static float getNormalX()
    {
        return normalX;
    }

    public static float getNormalY()
    {
        return normalY;
    }

    public static float getNormalZ()
    {
        return normalZ;
    }

    public static void glDrawArrays(int mode, int first, int count)
    {
        CorePipeline.draw(mode, first, count);

        if (Config.isShaders())
        {
            int instanceCount = Shaders.activeProgram.getCountInstances();

            if (instanceCount > 1)
            {
                for (int instanceIndex = 1; instanceIndex < instanceCount; ++instanceIndex)
                {
                    Shaders.uniform_instanceId.setValue(instanceIndex);
                    CorePipeline.draw(mode, first, count);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    public static int glGetError()
    {
        return GL11.glGetError();
    }

    public static void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, IntBuffer pixels)
    {
        GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, IntBuffer pixels)
    {
        GL11.glTexSubImage2D(target, level, xOffset, yOffset, width, height, format, type, pixels);
    }

    public static void glCopyTexSubImage2D(int target, int level, int xOffset, int yOffset, int x, int y, int width, int height)
    {
        GL11.glCopyTexSubImage2D(target, level, xOffset, yOffset, x, y, width, height);
    }

    public static void glGetTexImage(int target, int level, int format, int type, IntBuffer pixels)
    {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void glTexParameterf(int target, int pname, float param)
    {
        if (pname == 10242 || pname == 10243 || pname == 32882 || pname == 10241 || pname == 10240)
        {
            GL11.glTexParameteri(target, pname, (int)param);
            return;
        }

        GL11.glTexParameterf(target, pname, param);
    }

    public static void glTexParameteri(int target, int pname, int param)
    {
        GL11.glTexParameteri(target, pname, param);
    }

    public static int glGetTexLevelParameteri(int target, int level, int pname)
    {
        return GL11.glGetTexLevelParameteri(target, level, pname);
    }

    public static int getActiveTextureUnit()
    {
        return OpenGlHelper.defaultTexUnit + activeTextureUnit;
    }

    public static void bindCurrentTexture()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureState[activeTextureUnit].textureName);
    }

    public static int getBoundTexture()
    {
        return textureState[activeTextureUnit].textureName;
    }

    public static void checkBoundTexture()
    {
        if (Config.isMinecraftThread())
        {
            int glActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            int glBoundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            int cachedActiveTexture = getActiveTextureUnit();
            int cachedBoundTexture = getBoundTexture();

            if (cachedBoundTexture > 0)
            {
                if (glActiveTexture != cachedActiveTexture || glBoundTexture != cachedBoundTexture)
                {
                    Config.dbg("checkTexture: act: " + cachedActiveTexture + ", glAct: " + glActiveTexture + ", tex: " + cachedBoundTexture + ", glTex: " + glBoundTexture);
                }
            }
        }
    }

    public static void deleteTextures(IntBuffer textures)
    {
        textures.rewind();

        while (textures.position() < textures.limit())
        {
            int textureId = textures.get();
            deleteTexture(textureId);
        }

        textures.rewind();
    }

    public static boolean isFogEnabled()
    {
        return fogState.fog.currentState;
    }

    public static void setFogEnabled(boolean enabled)
    {
        fogState.fog.setState(enabled);
    }

    public static void lockAlpha(GlAlphaState state)
    {
        if (!alphaLock.isLocked())
        {
            getAlphaState(alphaLockState);
            setAlphaState(state);
            alphaLock.lock();
        }
    }

    public static void unlockAlpha()
    {
        if (alphaLock.unlock())
        {
            setAlphaState(alphaLockState);
        }
    }

    public static void getAlphaState(GlAlphaState state)
    {
        if (alphaLock.isLocked())
        {
            state.setState(alphaLockState);
        }
        else
        {
            state.setState(alphaState.alphaTest.currentState, alphaState.func, alphaState.ref);
        }
    }

    public static void setAlphaState(GlAlphaState state)
    {
        if (alphaLock.isLocked())
        {
            alphaLockState.setState(state);
        }
        else
        {
            alphaState.alphaTest.setState(state.isEnabled());
            alphaFunc(state.getFunc(), state.getRef());
        }
    }

    public static void lockBlend(GlBlendState state)
    {
        if (!blendLock.isLocked())
        {
            getBlendState(blendLockState);
            setBlendState(state);
            blendLock.lock();
        }
    }

    public static void unlockBlend()
    {
        if (blendLock.unlock())
        {
            setBlendState(blendLockState);
        }
    }

    public static void getBlendState(GlBlendState state)
    {
        if (blendLock.isLocked())
        {
            state.setState(blendLockState);
        }
        else
        {
            state.setState(blendState.blend.currentState, blendState.srcFactor, blendState.dstFactor, blendState.srcFactorAlpha, blendState.dstFactorAlpha);
        }
    }

    public static void setBlendState(GlBlendState state)
    {
        if (blendLock.isLocked())
        {
            blendLockState.setState(state);
        }
        else
        {
            blendState.blend.setState(state.isEnabled());

            if (!state.isSeparate())
            {
                blendFunc(state.getSrcFactor(), state.getDstFactor());
            }
            else
            {
                tryBlendFuncSeparate(state.getSrcFactor(), state.getDstFactor(), state.getSrcFactorAlpha(), state.getDstFactorAlpha());
            }
        }
    }

    public static void glMultiDrawArrays(int mode, IntBuffer first, IntBuffer count)
    {
        drawMulti(mode, first, count);

        if (Config.isShaders())
        {
            int instanceCount = Shaders.activeProgram.getCountInstances();

            if (instanceCount > 1)
            {
                for (int instanceIndex = 1; instanceIndex < instanceCount; ++instanceIndex)
                {
                    Shaders.uniform_instanceId.setValue(instanceIndex);
                    drawMulti(mode, first, count);
                }

                Shaders.uniform_instanceId.setValue(0);
            }
        }
    }

    private static void drawMulti(int mode, IntBuffer first, IntBuffer count)
    {
        int firstPosition = first.position();
        int countPosition = count.position();
        int draws = Math.min(first.remaining(), count.remaining());

        for (int i = 0; i < draws; ++i)
        {
            CorePipeline.draw(mode, first.get(), count.get());
        }

        first.position(firstPosition);
        count.position(countPosition);
    }

    public static void setOverlayColor(float red, float green, float blue, float alpha)
    {
        overlayRed = red;
        overlayGreen = green;
        overlayBlue = blue;
        overlayAlpha = alpha;
    }

    public static float getOverlayRed()
    {
        return overlayRed;
    }

    public static float getOverlayGreen()
    {
        return overlayGreen;
    }

    public static float getOverlayBlue()
    {
        return overlayBlue;
    }

    public static float getOverlayAlpha()
    {
        return overlayAlpha;
    }

    public static void setLights(float[] light0, float[] light1, float ambient, float diffuse)
    {
        light0Dir[0] = light0[0];
        light0Dir[1] = light0[1];
        light0Dir[2] = light0[2];
        light1Dir[0] = light1[0];
        light1Dir[1] = light1[1];
        light1Dir[2] = light1[2];
        lightAmbient = ambient;
        lightDiffuse = diffuse;
    }

    public static float getLight0X()
    {
        return light0Dir[0];
    }

    public static float getLight0Y()
    {
        return light0Dir[1];
    }

    public static float getLight0Z()
    {
        return light0Dir[2];
    }

    public static float getLight1X()
    {
        return light1Dir[0];
    }

    public static float getLight1Y()
    {
        return light1Dir[1];
    }

    public static float getLight1Z()
    {
        return light1Dir[2];
    }

    public static float getLightAmbient()
    {
        return lightAmbient;
    }

    public static float getLightDiffuse()
    {
        return lightDiffuse;
    }

    static
    {
        for (int lightIndex = 0; lightIndex < 8; ++lightIndex)
        {
            lightState[lightIndex] = new GlStateManager.BooleanState(16384 + lightIndex);
        }

        for (int textureIndex = 0; textureIndex < textureState.length; ++textureIndex)
        {
            textureState[textureIndex] = new GlStateManager.TextureState();
        }
    }

    static class AlphaState
    {
        public GlStateManager.BooleanState alphaTest;
        public int func;
        public float ref;

        private AlphaState()
        {
            this.alphaTest = new GlStateManager.BooleanState(3008);
            this.func = 519;
            this.ref = -1.0F;
        }
    }

    static class BlendState
    {
        public GlStateManager.BooleanState blend;
        public int srcFactor;
        public int dstFactor;
        public int srcFactorAlpha;
        public int dstFactorAlpha;

        private BlendState()
        {
            this.blend = new GlStateManager.BooleanState(3042);
            this.srcFactor = 1;
            this.dstFactor = 0;
            this.srcFactorAlpha = 1;
            this.dstFactorAlpha = 0;
        }
    }

    public static boolean isFixedFunctionCap(int capability)
    {
        return capability == 3008 || capability == 2896 || capability == 2912 || capability == 2903 || capability == 2977 || capability == 32826 || capability == 3553 || capability == 3168 || capability == 3169 || capability == 3170 || capability == 3171 || capability == 2852 || capability == 2882 || capability >= 16384 && capability <= 16391;
    }

    public static float getColorRed()
    {
        return colorState.red < 0.0F ? 1.0F : colorState.red;
    }

    public static float getColorGreen()
    {
        return colorState.green < 0.0F ? 1.0F : colorState.green;
    }

    public static float getColorBlue()
    {
        return colorState.blue < 0.0F ? 1.0F : colorState.blue;
    }

    public static float getColorAlpha()
    {
        return colorState.alpha < 0.0F ? 1.0F : colorState.alpha;
    }

    public static boolean isTexture2DEnabled()
    {
        return textureState[activeTextureUnit].texture2DState.isEnabled();
    }

    public static boolean isLightingEnabled()
    {
        return lightingState.isEnabled();
    }

    public static int getFogMode()
    {
        return fogState.mode;
    }

    public static float getFogStart()
    {
        return fogState.start;
    }

    public static float getFogEnd()
    {
        return fogState.end;
    }

    public static float getFogDensity()
    {
        return fogState.density;
    }

    public static void getFogColor(float[] out)
    {
        out[0] = fogState.color[0];
        out[1] = fogState.color[1];
        out[2] = fogState.color[2];
        out[3] = fogState.color[3];
    }

    public static boolean isAlphaTestEnabled()
    {
        return alphaState.alphaTest.isEnabled();
    }

    public static int getAlphaFunc()
    {
        return alphaState.func;
    }

    public static float getAlphaRef()
    {
        return alphaState.ref;
    }

    static class BooleanState
    {
        private final int capability;
        private boolean currentState = false;

        public BooleanState(int capabilityIn)
        {
            this.capability = capabilityIn;
        }

        public void setDisabled()
        {
            this.setState(false);
        }

        public void setEnabled()
        {
            this.setState(true);
        }

        public boolean isEnabled()
        {
            return this.currentState;
        }

        public void setState(boolean state)
        {
            if (state != this.currentState)
            {
                this.currentState = state;

                if (isFixedFunctionCap(this.capability))
                {
                    return;
                }

                if (state)
                {
                    GL11.glEnable(this.capability);
                }
                else
                {
                    GL11.glDisable(this.capability);
                }
            }
        }
    }

    static class ClearState
    {
        public double depth;
        public GlStateManager.Color color;
        public int stencil;

        private ClearState()
        {
            this.depth = 1.0D;
            this.color = new GlStateManager.Color(0.0F, 0.0F, 0.0F, 0.0F);
            this.stencil = 0;
        }
    }

    static class Color
    {
        public float red = 1.0F;
        public float green = 1.0F;
        public float blue = 1.0F;
        public float alpha = 1.0F;

        public Color()
        {
        }

        public Color(float redIn, float greenIn, float blueIn, float alphaIn)
        {
            this.red = redIn;
            this.green = greenIn;
            this.blue = blueIn;
            this.alpha = alphaIn;
        }
    }

    static class ColorLogicState
    {
        public GlStateManager.BooleanState colorLogicOp;
        public int opcode;

        private ColorLogicState()
        {
            this.colorLogicOp = new GlStateManager.BooleanState(3058);
            this.opcode = 5379;
        }
    }

    static class ColorMask
    {
        public boolean red;
        public boolean green;
        public boolean blue;
        public boolean alpha;

        private ColorMask()
        {
            this.red = true;
            this.green = true;
            this.blue = true;
            this.alpha = true;
        }
    }

    static class ColorMaterialState
    {
        public GlStateManager.BooleanState colorMaterial;
        public int face;
        public int mode;

        private ColorMaterialState()
        {
            this.colorMaterial = new GlStateManager.BooleanState(2903);
            this.face = 1032;
            this.mode = 5634;
        }
    }

    static class CullState
    {
        public GlStateManager.BooleanState cullFace;
        public int mode;

        private CullState()
        {
            this.cullFace = new GlStateManager.BooleanState(2884);
            this.mode = 1029;
        }
    }

    static class DepthState
    {
        public GlStateManager.BooleanState depthTest;
        public boolean maskEnabled;
        public int depthFunc;

        private DepthState()
        {
            this.depthTest = new GlStateManager.BooleanState(2929);
            this.maskEnabled = true;
            this.depthFunc = 513;
        }
    }

    static class FogState
    {
        public GlStateManager.BooleanState fog;
        public int mode;
        public float density;
        public float start;
        public float end;
        public float[] color;

        private FogState()
        {
            this.fog = new GlStateManager.BooleanState(2912);
            this.mode = 2048;
            this.density = 1.0F;
            this.start = 0.0F;
            this.end = 1.0F;
            this.color = new float[] {0.0F, 0.0F, 0.0F, 0.0F};
        }
    }

    static class PolygonOffsetState
    {
        public GlStateManager.BooleanState polygonOffsetFill;
        public GlStateManager.BooleanState polygonOffsetLine;
        public float factor;
        public float units;

        private PolygonOffsetState()
        {
            this.polygonOffsetFill = new GlStateManager.BooleanState(32823);
            this.polygonOffsetLine = new GlStateManager.BooleanState(10754);
            this.factor = 0.0F;
            this.units = 0.0F;
        }
    }

    static class StencilFunc
    {
        public int func;
        public int ref;
        public int mask;

        private StencilFunc()
        {
            this.func = 519;
            this.ref = 0;
            this.mask = -1;
        }
    }

    static class StencilState
    {
        public GlStateManager.StencilFunc func;
        public int mask;
        public int fail;
        public int zfail;
        public int zpass;

        private StencilState()
        {
            this.func = new GlStateManager.StencilFunc();
            this.mask = -1;
            this.fail = 7680;
            this.zfail = 7680;
            this.zpass = 7680;
        }
    }

    public static enum TexGen
    {
        S,
        T,
        R,
        Q;
    }

    static class TexGenCoord
    {
        public GlStateManager.BooleanState textureGen;
        public int coord;
        public int param = -1;

        public TexGenCoord(int coord, int capability)
        {
            this.coord = coord;
            this.textureGen = new GlStateManager.BooleanState(capability);
        }
    }

    static class TexGenState
    {
        public GlStateManager.TexGenCoord sCoord;
        public GlStateManager.TexGenCoord tCoord;
        public GlStateManager.TexGenCoord rCoord;
        public GlStateManager.TexGenCoord qCoord;

        private TexGenState()
        {
            this.sCoord = new GlStateManager.TexGenCoord(8192, 3168);
            this.tCoord = new GlStateManager.TexGenCoord(8193, 3169);
            this.rCoord = new GlStateManager.TexGenCoord(8194, 3170);
            this.qCoord = new GlStateManager.TexGenCoord(8195, 3171);
        }
    }

    static class TextureState
    {
        public GlStateManager.BooleanState texture2DState;
        public int textureName;

        private TextureState()
        {
            this.texture2DState = new GlStateManager.BooleanState(3553);
            this.textureName = 0;
        }
    }
}
