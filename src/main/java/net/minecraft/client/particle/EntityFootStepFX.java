package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFootStepFX extends EntityFX
{
    private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");
    private int footstepAge;
    private int footstepMaxAge;
    private TextureManager currentFootSteps;
    private final BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();

    protected EntityFootStepFX(TextureManager currentFootStepsIn, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.currentFootSteps = currentFootStepsIn;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.footstepMaxAge = 200;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float ageProgress = ((float)this.footstepAge + partialTicks) / (float)this.footstepMaxAge;
        ageProgress = ageProgress * ageProgress;
        float alpha = 2.0F - ageProgress * 2.0F;

        if (alpha > 1.0F)
        {
            alpha = 1.0F;
        }

        alpha = alpha * 0.2F;
        GlStateManager.disableLighting();
        float halfSize = 0.125F;
        float renderX = (float)(this.posX - interpPosX);
        float renderY = (float)(this.posY - interpPosY);
        float renderZ = (float)(this.posZ - interpPosZ);
        this.samplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
        float brightness = this.worldObj.getLightBrightness(this.samplePos);
        this.currentFootSteps.bindTexture(FOOTPRINT_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRendererIn.pos((double)(renderX - halfSize), (double)renderY, (double)(renderZ + halfSize)).tex(0.0D, 1.0D).color(brightness, brightness, brightness, alpha).endVertex();
        worldRendererIn.pos((double)(renderX + halfSize), (double)renderY, (double)(renderZ + halfSize)).tex(1.0D, 1.0D).color(brightness, brightness, brightness, alpha).endVertex();
        worldRendererIn.pos((double)(renderX + halfSize), (double)renderY, (double)(renderZ - halfSize)).tex(1.0D, 0.0D).color(brightness, brightness, brightness, alpha).endVertex();
        worldRendererIn.pos((double)(renderX - halfSize), (double)renderY, (double)(renderZ - halfSize)).tex(0.0D, 0.0D).color(brightness, brightness, brightness, alpha).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    public void onUpdate()
    {
        ++this.footstepAge;

        if (this.footstepAge == this.footstepMaxAge)
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
            return new EntityFootStepFX(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}
