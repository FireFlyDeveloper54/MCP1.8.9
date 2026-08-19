package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityLargeExplodeFX extends EntityFX
{
    private static final ResourceLocation EXPLOSION_TEXTURE = new ResourceLocation("textures/entity/explosion.png");
    private static final VertexFormat EXPLOSION_VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.TEX_2S).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
    private int explosionAge;
    private int explosionMaxAge;
    private TextureManager theRenderEngine;
    private float explosionScale;

    protected EntityLargeExplodeFX(TextureManager renderEngine, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double scale, double unusedYSpeed, double unusedZSpeed)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.theRenderEngine = renderEngine;
        this.explosionMaxAge = 6 + this.rand.nextInt(4);
        this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.6F + 0.4F;
        this.explosionScale = 1.0F - (float)scale * 0.5F;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        int textureIndex = (int)(((float)this.explosionAge + partialTicks) * 15.0F / (float)this.explosionMaxAge);

        if (textureIndex <= 15)
        {
            this.theRenderEngine.bindTexture(EXPLOSION_TEXTURE);
            float minU = (float)(textureIndex % 4) / 4.0F;
            float maxU = minU + 0.24975F;
            float minV = (float)(textureIndex / 4) / 4.0F;
            float maxV = minV + 0.24975F;
            float quadSize = 2.0F * this.explosionScale;
            float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
            float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
            float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            worldRendererIn.begin(7, EXPLOSION_VERTEX_FORMAT);
            worldRendererIn.pos((double)(renderX - rotationX * quadSize - rotationXY * quadSize), (double)(renderY - rotationZ * quadSize), (double)(renderZ - rotationYZ * quadSize - rotationXZ * quadSize)).tex((double)maxU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            worldRendererIn.pos((double)(renderX - rotationX * quadSize + rotationXY * quadSize), (double)(renderY + rotationZ * quadSize), (double)(renderZ - rotationYZ * quadSize + rotationXZ * quadSize)).tex((double)maxU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            worldRendererIn.pos((double)(renderX + rotationX * quadSize + rotationXY * quadSize), (double)(renderY + rotationZ * quadSize), (double)(renderZ + rotationYZ * quadSize + rotationXZ * quadSize)).tex((double)minU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            worldRendererIn.pos((double)(renderX + rotationX * quadSize - rotationXY * quadSize), (double)(renderY - rotationZ * quadSize), (double)(renderZ + rotationYZ * quadSize - rotationXZ * quadSize)).tex((double)minU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.enableLighting();
        }
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return 61680;
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.explosionAge;

        if (this.explosionAge == this.explosionMaxAge)
        {
            this.setDead();
        }
    }

    public int getFXLayer()
    {
        return 3;
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityLargeExplodeFX(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
