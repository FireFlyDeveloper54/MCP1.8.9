package net.minecraft.client.model;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.model.ModelSprite;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.shaders.SVertexFormat;
import org.lwjgl.opengl.GL11;

public class ModelRenderer
{
    public float textureWidth;
    public float textureHeight;
    private int textureOffsetX;
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    private boolean compiled;
    private int displayList;
    private VertexBuffer vertexBuffer;
    private VertexFormat compiledFormat;
    private int compiledMode;
    private int compiledVertexCount;
    public boolean mirror;
    public boolean showModel;
    public boolean isHidden;
    public List<ModelBox> cubeList;
    public List<ModelRenderer> childModels;
    public final String boxName;
    private ModelBase baseModel;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public List<ModelSprite> spriteList;
    public boolean mirrorV;
    public float scaleX;
    public float scaleY;
    public float scaleZ;
    private int countResetDisplayList;
    private ResourceLocation textureLocation;
    private String id;
    private ModelUpdater modelUpdater;
    private RenderGlobal renderGlobal;

    public ModelRenderer(ModelBase model, String boxNameIn)
    {
        this.spriteList = new ArrayList<ModelSprite>();
        this.mirrorV = false;
        this.scaleX = 1.0F;
        this.scaleY = 1.0F;
        this.scaleZ = 1.0F;
        this.textureLocation = null;
        this.id = null;
        this.renderGlobal = Config.getRenderGlobal();
        this.textureWidth = 64.0F;
        this.textureHeight = 32.0F;
        this.showModel = true;
        this.cubeList = Lists.<ModelBox>newArrayList();
        this.baseModel = model;
        model.boxList.add(this);
        this.boxName = boxNameIn;
        this.setTextureSize(model.textureWidth, model.textureHeight);
    }

    public ModelRenderer(ModelBase model)
    {
        this(model, (String)null);
    }

    public ModelRenderer(ModelBase model, int texOffX, int texOffY)
    {
        this(model);
        this.setTextureOffset(texOffX, texOffY);
    }

    public void addChild(ModelRenderer renderer)
    {
        if (this.childModels == null)
        {
            this.childModels = Lists.<ModelRenderer>newArrayList();
        }

        this.childModels.add(renderer);
    }

    public ModelRenderer setTextureOffset(int x, int y)
    {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public ModelRenderer addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth)
    {
        partName = this.boxName + "." + partName;
        TextureOffset textureOffset = this.baseModel.getTextureOffset(partName);
        this.setTextureOffset(textureOffset.textureOffsetX, textureOffset.textureOffsetY);
        this.cubeList.add((new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F)).setBoxName(partName));
        return this;
    }

    public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth)
    {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
        return this;
    }

    public ModelRenderer addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirror)
    {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F, mirror));
        return this;
    }

    public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor)
    {
        this.cubeList.add(new ModelBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, scaleFactor));
    }

    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn)
    {
        this.rotationPointX = rotationPointXIn;
        this.rotationPointY = rotationPointYIn;
        this.rotationPointZ = rotationPointZIn;
    }

    public void render(float scale)
    {
        if (!this.isHidden && this.showModel)
        {
            this.checkResetDisplayList();

            if (!this.compiled)
            {
                this.compileDisplayList(scale);
            }

            int previousTextureId = 0;

            if (this.textureLocation != null && !this.renderGlobal.renderOverlayDamaged)
            {
                if (this.renderGlobal.renderOverlayEyes)
                {
                    return;
                }

                previousTextureId = GlStateManager.getBoundTexture();
                Config.getTextureManager().bindTexture(this.textureLocation);
            }

            if (this.modelUpdater != null)
            {
                this.modelUpdater.update();
            }

            boolean hasCustomScale = this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F;
            GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
            {
                if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F)
                {
                    if (hasCustomScale)
                    {
                        GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
                    }

                    this.drawCompiled();

                    if (this.childModels != null)
                    {
                        for (int childIndex = 0; childIndex < this.childModels.size(); ++childIndex)
                        {
                            this.childModels.get(childIndex).render(scale);
                        }
                    }

                    if (hasCustomScale)
                    {
                        GlStateManager.scale(1.0F / this.scaleX, 1.0F / this.scaleY, 1.0F / this.scaleZ);
                    }
                }
                else
                {
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

                    if (hasCustomScale)
                    {
                        GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
                    }

                    this.drawCompiled();

                    if (this.childModels != null)
                    {
                        for (int childIndex = 0; childIndex < this.childModels.size(); ++childIndex)
                        {
                            this.childModels.get(childIndex).render(scale);
                        }
                    }

                    if (hasCustomScale)
                    {
                        GlStateManager.scale(1.0F / this.scaleX, 1.0F / this.scaleY, 1.0F / this.scaleZ);
                    }

                    GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
                }
            }
            else
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F)
                {
                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F)
                {
                    GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F)
                {
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                }

                if (hasCustomScale)
                {
                    GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
                }

                this.drawCompiled();

                if (this.childModels != null)
                {
                    for (int childIndex = 0; childIndex < this.childModels.size(); ++childIndex)
                    {
                        this.childModels.get(childIndex).render(scale);
                    }
                }

                GlStateManager.popMatrix();
            }

            GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);

            if (previousTextureId != 0)
            {
                GlStateManager.bindTexture(previousTextureId);
            }
        }
    }

    public void renderWithRotation(float scale)
    {
        if (!this.isHidden && this.showModel)
        {
            this.checkResetDisplayList();

            if (!this.compiled)
            {
                this.compileDisplayList(scale);
            }

            int previousTextureId = 0;

            if (this.textureLocation != null && !this.renderGlobal.renderOverlayDamaged)
            {
                if (this.renderGlobal.renderOverlayEyes)
                {
                    return;
                }

                previousTextureId = GlStateManager.getBoundTexture();
                Config.getTextureManager().bindTexture(this.textureLocation);
            }

            if (this.modelUpdater != null)
            {
                this.modelUpdater.update();
            }

            boolean hasCustomScale = this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

            if (this.rotateAngleY != 0.0F)
            {
                GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if (this.rotateAngleX != 0.0F)
            {
                GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if (this.rotateAngleZ != 0.0F)
            {
                GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }

            if (hasCustomScale)
            {
                GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
            }

            this.drawCompiled();

            if (this.childModels != null)
            {
                for (int childIndex = 0; childIndex < this.childModels.size(); ++childIndex)
                {
                    this.childModels.get(childIndex).render(scale);
                }
            }

            GlStateManager.popMatrix();

            if (previousTextureId != 0)
            {
                GlStateManager.bindTexture(previousTextureId);
            }
        }
    }

    public void postRender(float scale)
    {
        if (!this.isHidden && this.showModel)
        {
            this.checkResetDisplayList();

            if (!this.compiled)
            {
                this.compileDisplayList(scale);
            }

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
            {
                if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F)
                {
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                }
            }
            else
            {
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F)
                {
                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F)
                {
                    GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F)
                {
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    private void compileDisplayList(float scale)
    {
        this.deleteVertexBuffer();

        if (OpenGlHelper.vboSupported && this.spriteList.isEmpty())
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            VertexFormat format = Config.isShaders() ? SVertexFormat.defVertexFormatTextured : DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL;

            if (!this.cubeList.isEmpty())
            {
                worldRenderer.begin(7, format);

                for (int cubeIndex = 0; cubeIndex < this.cubeList.size(); ++cubeIndex)
                {
                    this.cubeList.get(cubeIndex).render(worldRenderer, scale);
                }

                worldRenderer.finishDrawing();
                this.compiledVertexCount = worldRenderer.getVertexCount();
                this.compiledMode = worldRenderer.getDrawMode();
                this.compiledFormat = format;
                this.vertexBuffer = new VertexBuffer(format);
                this.vertexBuffer.bufferData(worldRenderer.getByteBuffer());
                worldRenderer.reset();
            }

            this.compiled = true;
            return;
        }

        if (this.displayList == 0)
        {
            this.displayList = GLAllocation.generateDisplayLists(1);
        }

        GL11.glNewList(this.displayList, GL11.GL_COMPILE);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        if (!this.cubeList.isEmpty())
        {
            worldRenderer.begin(7, Config.isShaders() ? SVertexFormat.defVertexFormatTextured : DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

            for (int cubeIndex = 0; cubeIndex < this.cubeList.size(); ++cubeIndex)
            {
                this.cubeList.get(cubeIndex).render(worldRenderer, scale);
            }

            tessellator.draw();
        }

        for (int spriteIndex = 0; spriteIndex < this.spriteList.size(); ++spriteIndex)
        {
            ModelSprite modelSprite = this.spriteList.get(spriteIndex);
            modelSprite.render(tessellator, scale);
        }

        GL11.glEndList();
        this.compiled = true;
    }

    private void drawCompiled()
    {
        if (this.vertexBuffer != null && this.compiledVertexCount > 0)
        {
            VertexFormat format = this.compiledFormat != null ? this.compiledFormat : DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL;
            this.vertexBuffer.bindBuffer();

            if (Config.isShaders())
            {
                ShadersRender.setupArrayPointersVbo();
            }
            else
            {
                WorldVertexBufferUploader.setupVertexFormat(format, 0L);
            }

            GlStateManager.glDrawArrays(this.compiledMode, 0, this.compiledVertexCount);
            WorldVertexBufferUploader.clearVertexFormat(format);

            if (Config.isShaders())
            {
                org.lwjgl.opengl.GL20.glDisableVertexAttribArray(Shaders.midTexCoordAttrib);
                org.lwjgl.opengl.GL20.glDisableVertexAttribArray(Shaders.tangentAttrib);
                org.lwjgl.opengl.GL20.glDisableVertexAttribArray(Shaders.entityAttrib);
            }

            this.vertexBuffer.unbindBuffer();
        }
        else if (this.displayList != 0)
        {
            GlStateManager.callList(this.displayList);
        }
    }

    private void deleteVertexBuffer()
    {
        if (this.vertexBuffer != null)
        {
            this.vertexBuffer.deleteGlBuffers();
            this.vertexBuffer = null;
        }

        this.compiledVertexCount = 0;
        this.compiledFormat = null;
    }

    public ModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn)
    {
        this.textureWidth = (float)textureWidthIn;
        this.textureHeight = (float)textureHeightIn;
        return this;
    }

    public void addSprite(float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd)
    {
        this.spriteList.add(new ModelSprite(this, this.textureOffsetX, this.textureOffsetY, posX, posY, posZ, sizeX, sizeY, sizeZ, sizeAdd));
    }

    public boolean getCompiled()
    {
        return this.compiled;
    }

    public int getDisplayList()
    {
        return this.displayList;
    }

    private void checkResetDisplayList()
    {
        if (this.countResetDisplayList != Shaders.countResetDisplayLists)
        {
            this.compiled = false;
            this.deleteVertexBuffer();
            this.countResetDisplayList = Shaders.countResetDisplayLists;
        }
    }

    public ResourceLocation getTextureLocation()
    {
        return this.textureLocation;
    }

    public void setTextureLocation(ResourceLocation textureLocation)
    {
        this.textureLocation = textureLocation;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void addBox(int[][] faceUvs, float x, float y, float z, float width, float height, float depth, float scale)
    {
        this.cubeList.add(new ModelBox(this, faceUvs, x, y, z, width, height, depth, scale, this.mirror));
    }

    public ModelRenderer getChild(String childId)
    {
        if (childId == null)
        {
            return null;
        }
        else
        {
            if (this.childModels != null)
            {
                for (int childIndex = 0; childIndex < this.childModels.size(); ++childIndex)
                {
                    ModelRenderer modelRenderer = this.childModels.get(childIndex);

                    if (childId.equals(modelRenderer.getId()))
                    {
                        return modelRenderer;
                    }
                }
            }

            return null;
        }
    }

    public ModelRenderer getChildDeep(String childId)
    {
        if (childId == null)
        {
            return null;
        }
        else
        {
            ModelRenderer directChild = this.getChild(childId);

            if (directChild != null)
            {
                return directChild;
            }
            else
            {
                if (this.childModels != null)
                {
                    for (int childIndex = 0; childIndex < this.childModels.size(); ++childIndex)
                    {
                        ModelRenderer childRenderer = this.childModels.get(childIndex);
                        ModelRenderer nestedChild = childRenderer.getChildDeep(childId);

                        if (nestedChild != null)
                        {
                            return nestedChild;
                        }
                    }
                }

                return null;
            }
        }
    }

    public void setModelUpdater(ModelUpdater modelUpdater)
    {
        this.modelUpdater = modelUpdater;
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("id: " + this.id + ", boxes: " + (this.cubeList != null ? Integer.valueOf(this.cubeList.size()) : null) + ", submodels: " + (this.childModels != null ? Integer.valueOf(this.childModels.size()) : null));
        return stringbuffer.toString();
    }
}
