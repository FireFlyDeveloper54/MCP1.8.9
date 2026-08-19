package net.minecraft.client.renderer.texture;

import net.minecraft.client.renderer.GlStateManager;
import net.optifine.shaders.MultiTexID;
import net.optifine.shaders.ShadersTex;
import org.lwjgl.opengl.GL11;

public abstract class AbstractTexture implements ITextureObject
{
    protected int glTextureId = -1;
    protected boolean blur;
    protected boolean mipmap;
    protected boolean blurLast;
    protected boolean mipmapLast;
    public MultiTexID multiTex;

    public void setBlurMipmapDirect(boolean blur, boolean mipmap)
    {
        this.blur = blur;
        this.mipmap = mipmap;
        int minFilter = -1;
        int magFilter = -1;

        if (blur)
        {
            minFilter = mipmap ? 9987 : 9729;
            magFilter = 9729;
        }
        else
        {
            minFilter = mipmap ? 9986 : 9728;
            magFilter = 9728;
        }

        GlStateManager.bindTexture(this.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
    }

    public void setBlurMipmap(boolean blur, boolean mipmap)
    {
        this.blurLast = this.blur;
        this.mipmapLast = this.mipmap;
        this.setBlurMipmapDirect(blur, mipmap);
    }

    public void restoreLastBlurMipmap()
    {
        this.setBlurMipmapDirect(this.blurLast, this.mipmapLast);
    }

    public int getGlTextureId()
    {
        if (this.glTextureId == -1)
        {
            this.glTextureId = TextureUtil.glGenTextures();
        }

        return this.glTextureId;
    }

    public void deleteGlTexture()
    {
        ShadersTex.deleteTextures(this, this.glTextureId);

        if (this.glTextureId != -1)
        {
            TextureUtil.deleteTexture(this.glTextureId);
            this.glTextureId = -1;
        }
    }

    public MultiTexID getMultiTexID()
    {
        return ShadersTex.getMultiTexID(this);
    }
}
