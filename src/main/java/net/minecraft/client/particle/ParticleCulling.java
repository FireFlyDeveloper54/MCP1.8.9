package net.minecraft.client.particle;

import net.minecraft.client.renderer.culling.ICamera;

public final class ParticleCulling
{
    private static ICamera camera;

    private ParticleCulling()
    {
    }

    public static void setCamera(ICamera cameraIn)
    {
        camera = cameraIn;
    }

    public static void updateCullState(EntityFX particle)
    {
        ICamera currentCamera = camera;
        particle.setInView(currentCamera == null || currentCamera.isBoundingBoxInFrustum(particle.getEntityBoundingBox()));
    }
}
