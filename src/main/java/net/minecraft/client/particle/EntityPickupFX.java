package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.world.World;
import net.optifine.shaders.Program;
import net.optifine.shaders.Shaders;

public class EntityPickupFX extends EntityFX
{
    private Entity entityToPickUp;
    private Entity entityPickingUp;
    private int age;
    private int maxAge;
    private float yOffset;
    private RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

    public EntityPickupFX(World worldIn, Entity entityToPickUp, Entity entityPickingUp, float yOffset)
    {
        super(worldIn, entityToPickUp.posX, entityToPickUp.posY, entityToPickUp.posZ, entityToPickUp.motionX, entityToPickUp.motionY, entityToPickUp.motionZ);
        this.entityToPickUp = entityToPickUp;
        this.entityPickingUp = entityPickingUp;
        this.maxAge = 3;
        this.yOffset = yOffset;
    }

    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        Program program = null;

        if (Config.isShaders())
        {
            program = Shaders.activeProgram;
            Shaders.nextEntity(this.entityToPickUp);
        }

        float pickupProgress = ((float)this.age + partialTicks) / (float)this.maxAge;
        pickupProgress = pickupProgress * pickupProgress;
        double itemX = this.entityToPickUp.posX;
        double itemY = this.entityToPickUp.posY;
        double itemZ = this.entityToPickUp.posZ;
        double targetX = this.entityPickingUp.lastTickPosX + (this.entityPickingUp.posX - this.entityPickingUp.lastTickPosX) * (double)partialTicks;
        double targetY = this.entityPickingUp.lastTickPosY + (this.entityPickingUp.posY - this.entityPickingUp.lastTickPosY) * (double)partialTicks + (double)this.yOffset;
        double targetZ = this.entityPickingUp.lastTickPosZ + (this.entityPickingUp.posZ - this.entityPickingUp.lastTickPosZ) * (double)partialTicks;
        double renderX = itemX + (targetX - itemX) * (double)pickupProgress;
        double renderY = itemY + (targetY - itemY) * (double)pickupProgress;
        double renderZ = itemZ + (targetZ - itemZ) * (double)pickupProgress;
        int packedBrightness = this.getBrightnessForRender(partialTicks);
        int lightmapU = packedBrightness % 65536;
        int lightmapV = packedBrightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightmapU / 1.0F, (float)lightmapV / 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderX = renderX - interpPosX;
        renderY = renderY - interpPosY;
        renderZ = renderZ - interpPosZ;
        this.renderManager.renderEntityWithPosYaw(this.entityToPickUp, (double)((float)renderX), (double)((float)renderY), (double)((float)renderZ), this.entityToPickUp.rotationYaw, partialTicks);

        if (Config.isShaders())
        {
            Shaders.setEntityId((Entity)null);
            Shaders.useProgram(program);
        }
    }

    public void onUpdate()
    {
        ++this.age;

        if (this.age == this.maxAge)
        {
            this.setDead();
        }
    }

    public int getFXLayer()
    {
        return 3;
    }
}
