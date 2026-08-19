package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityDiggingFX extends EntityFX
{
    private IBlockState sourceState;
    private BlockPos sourcePos;

    protected EntityDiggingFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.sourceState = state;
        this.setParticleIcon(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state));
        this.particleGravity = state.getBlock().blockParticleGravity;
        this.particleRed = this.particleGreen = this.particleBlue = 0.6F;
        this.particleScale /= 2.0F;
    }

    public EntityDiggingFX setBlockPos(BlockPos pos)
    {
        this.sourcePos = pos;

        if (this.sourceState.getBlock() == Blocks.grass)
        {
            return this;
        }
        else
        {
            int color = this.sourceState.getBlock().colorMultiplier(this.worldObj, pos);
            this.particleRed *= (float)(color >> 16 & 255) / 255.0F;
            this.particleGreen *= (float)(color >> 8 & 255) / 255.0F;
            this.particleBlue *= (float)(color & 255) / 255.0F;
            return this;
        }
    }

    public EntityDiggingFX applyRenderColor()
    {
        this.sourcePos = new BlockPos(this.posX, this.posY, this.posZ);
        Block block = this.sourceState.getBlock();

        if (block == Blocks.grass)
        {
            return this;
        }
        else
        {
            int color = block.getRenderColor(this.sourceState);
            this.particleRed *= (float)(color >> 16 & 255) / 255.0F;
            this.particleGreen *= (float)(color >> 8 & 255) / 255.0F;
            this.particleBlue *= (float)(color & 255) / 255.0F;
            return this;
        }
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

    public int getBrightnessForRender(float partialTicks)
    {
        int particleBrightness = super.getBrightnessForRender(partialTicks);
        int worldBrightness = 0;

        if (this.worldObj.isBlockLoaded(this.sourcePos))
        {
            worldBrightness = this.worldObj.getCombinedLight(this.sourcePos, 0);
        }

        return particleBrightness == 0 ? worldBrightness : particleBrightness;
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return (new EntityDiggingFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Block.getStateById(parameters[0]))).applyRenderColor();
        }
    }
}
