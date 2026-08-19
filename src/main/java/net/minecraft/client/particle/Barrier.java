package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class Barrier extends EntityFX
{
    protected Barrier(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, Item itemIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.setParticleIcon(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(itemIn));
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.particleGravity = 0.0F;
        this.particleMaxAge = 80;
    }

    public int getFXLayer()
    {
        return 1;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float minU = this.particleIcon.getMinU();
        float maxU = this.particleIcon.getMaxU();
        float minV = this.particleIcon.getMinV();
        float maxV = this.particleIcon.getMaxV();
        float halfSize = 0.5F;
        float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int packedBrightness = this.getBrightnessForRender(partialTicks);
        int lightmapU = packedBrightness >> 16 & 65535;
        int lightmapV = packedBrightness & 65535;
        worldRendererIn.pos((double)(renderX - rotationX * halfSize - rotationXY * halfSize), (double)(renderY - rotationZ * halfSize), (double)(renderZ - rotationYZ * halfSize - rotationXZ * halfSize)).tex((double)maxU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
        worldRendererIn.pos((double)(renderX - rotationX * halfSize + rotationXY * halfSize), (double)(renderY + rotationZ * halfSize), (double)(renderZ - rotationYZ * halfSize + rotationXZ * halfSize)).tex((double)maxU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
        worldRendererIn.pos((double)(renderX + rotationX * halfSize + rotationXY * halfSize), (double)(renderY + rotationZ * halfSize), (double)(renderZ + rotationYZ * halfSize + rotationXZ * halfSize)).tex((double)minU, (double)minV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
        worldRendererIn.pos((double)(renderX + rotationX * halfSize - rotationXY * halfSize), (double)(renderY - rotationZ * halfSize), (double)(renderZ + rotationYZ * halfSize - rotationXZ * halfSize)).tex((double)minU, (double)maxV).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(lightmapU, lightmapV).endVertex();
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new Barrier(worldIn, xCoordIn, yCoordIn, zCoordIn, Item.getItemFromBlock(Blocks.barrier));
        }
    }
}
