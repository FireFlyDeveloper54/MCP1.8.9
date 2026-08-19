package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import optimization.FastTrig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class RenderMinecart<T extends EntityMinecart> extends Render<T>
{
    private static final ResourceLocation minecartTextures = new ResourceLocation("textures/entity/minecart.png");
    protected ModelBase modelMinecart = new ModelMinecart();

    public RenderMinecart(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        this.bindEntityTexture(entity);
        long renderOffsetSeed = (long)entity.getEntityId() * 493286711L;
        renderOffsetSeed = renderOffsetSeed * renderOffsetSeed * 4392167121L + renderOffsetSeed * 98761L;
        float jitterX = (((float)(renderOffsetSeed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float jitterY = (((float)(renderOffsetSeed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float jitterZ = (((float)(renderOffsetSeed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        GlStateManager.translate(jitterX, jitterY, jitterZ);
        double xCoordinate = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double yCoordinate = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double zCoordinate = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
        double railOffsetDistance = 0.30000001192092896D;
        Vec3 railPosition = entity.getPos(xCoordinate, yCoordinate, zCoordinate);
        float cartPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        if (railPosition != null)
        {
            Vec3 forwardRailPosition = entity.getPosOffset(xCoordinate, yCoordinate, zCoordinate, railOffsetDistance);
            Vec3 backwardRailPosition = entity.getPosOffset(xCoordinate, yCoordinate, zCoordinate, -railOffsetDistance);

            if (forwardRailPosition == null)
            {
                forwardRailPosition = railPosition;
            }

            if (backwardRailPosition == null)
            {
                backwardRailPosition = railPosition;
            }

            x += railPosition.xCoord - xCoordinate;
            y += (forwardRailPosition.yCoord + backwardRailPosition.yCoord) / 2.0D - yCoordinate;
            z += railPosition.zCoord - zCoordinate;
            Vec3 railDirection = backwardRailPosition.addVector(-forwardRailPosition.xCoord, -forwardRailPosition.yCoord, -forwardRailPosition.zCoord);

            if (railDirection.lengthVector() != 0.0D)
            {
                railDirection = railDirection.normalize();
                entityYaw = (float)(FastTrig.atan2(railDirection.zCoord, railDirection.xCoord) * 180.0D / Math.PI);
                cartPitch = (float)(Math.atan(railDirection.yCoord) * 73.0D);
            }
        }

        GlStateManager.translate((float)x, (float)y + 0.375F, (float)z);
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-cartPitch, 0.0F, 0.0F, 1.0F);
        float rollingAmplitude = (float)entity.getRollingAmplitude() - partialTicks;
        float damage = entity.getDamage() - partialTicks;

        if (damage < 0.0F)
        {
            damage = 0.0F;
        }

        if (rollingAmplitude > 0.0F)
        {
            GlStateManager.rotate(MathHelper.sin(rollingAmplitude) * rollingAmplitude * damage / 10.0F * (float)entity.getRollingDirection(), 1.0F, 0.0F, 0.0F);
        }

        int displayTileOffset = entity.getDisplayTileOffset();
        IBlockState displayTileState = entity.getDisplayTile();

        if (displayTileState.getBlock().getRenderType() != -1)
        {
            GlStateManager.pushMatrix();
            this.bindTexture(TextureMap.locationBlocksTexture);
            float displayTileScale = 0.75F;
            GlStateManager.scale(displayTileScale, displayTileScale, displayTileScale);
            GlStateManager.translate(-0.5F, (float)(displayTileOffset - 8) / 16.0F, 0.5F);
            this.renderCartContents(entity, partialTicks, displayTileState);
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindEntityTexture(entity);
        }

        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        this.modelMinecart.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(T entity)
    {
        return minecartTextures;
    }

    protected void renderCartContents(T minecart, float partialTicks, IBlockState state)
    {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, minecart.getBrightness(partialTicks));
        GlStateManager.popMatrix();
    }
}
