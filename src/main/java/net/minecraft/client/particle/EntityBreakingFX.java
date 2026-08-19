package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class EntityBreakingFX extends EntityFX
{
    protected EntityBreakingFX(World worldIn, double posXIn, double posYIn, double posZIn, Item itemIn)
    {
        this(worldIn, posXIn, posYIn, posZIn, itemIn, 0);
    }

    protected EntityBreakingFX(World worldIn, double posXIn, double posYIn, double posZIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, Item itemIn, int metadata)
    {
        this(worldIn, posXIn, posYIn, posZIn, itemIn, metadata);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += xSpeedIn;
        this.motionY += ySpeedIn;
        this.motionZ += zSpeedIn;
    }

    protected EntityBreakingFX(World worldIn, double posXIn, double posYIn, double posZIn, Item itemIn, int metadata)
    {
        super(worldIn, posXIn, posYIn, posZIn, 0.0D, 0.0D, 0.0D);
        this.setParticleIcon(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(itemIn, metadata));
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleGravity = Blocks.snow.blockParticleGravity;
        this.particleScale /= 2.0F;
    }

    public int getFXLayer()
    {
        return 1;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float minU = ((float)this.particleTextureIndexX + this.particleTextureJitterX / 4.0F) / 16.0F;
        float maxU = minU + 0.015609375F;
        float minV = ((float)this.particleTextureIndexY + this.particleTextureJitterY / 4.0F) / 16.0F;
        float maxV = minV + 0.015609375F;
        float quadSize = 0.1F * this.particleScale;

        if (this.particleIcon != null)
        {
            minU = this.particleIcon.getInterpolatedU((double)(this.particleTextureJitterX / 4.0F * 16.0F));
            maxU = this.particleIcon.getInterpolatedU((double)((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F));
            minV = this.particleIcon.getInterpolatedV((double)(this.particleTextureJitterY / 4.0F * 16.0F));
            maxV = this.particleIcon.getInterpolatedV((double)((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F));
        }

        float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int packedBrightness = this.getBrightnessForRender(partialTicks);
        int lightmapU = packedBrightness >> 16 & 65535;
        int lightmapV = packedBrightness & 65535;
        worldRendererIn.pos((double)(renderX - rotationX * quadSize - rotationXY * quadSize), (double)(renderY - rotationZ * quadSize), (double)(renderZ - rotationYZ * quadSize - rotationXZ * quadSize)).tex((double)minU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
        worldRendererIn.pos((double)(renderX - rotationX * quadSize + rotationXY * quadSize), (double)(renderY + rotationZ * quadSize), (double)(renderZ - rotationYZ * quadSize + rotationXZ * quadSize)).tex((double)minU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
        worldRendererIn.pos((double)(renderX + rotationX * quadSize + rotationXY * quadSize), (double)(renderY + rotationZ * quadSize), (double)(renderZ + rotationYZ * quadSize + rotationXZ * quadSize)).tex((double)maxU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
        worldRendererIn.pos((double)(renderX + rotationX * quadSize - rotationXY * quadSize), (double)(renderY - rotationZ * quadSize), (double)(renderZ + rotationYZ * quadSize - rotationXZ * quadSize)).tex((double)maxU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            int metadata = parameters.length > 1 ? parameters[1] : 0;
            return new EntityBreakingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Item.getItemById(parameters[0]), metadata);
        }
    }

    public static class SlimeFactory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityBreakingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, Items.slime_ball);
        }
    }

    public static class SnowballFactory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityBreakingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, Items.snowball);
        }
    }
}
